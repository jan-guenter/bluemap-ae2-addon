/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.adapter.bluemap522;

import com.flowpowered.math.vector.Vector3f;
import com.flowpowered.math.vector.Vector4f;
import de.bluecolored.bluemap.core.map.TextureGallery;
import de.bluecolored.bluemap.core.map.hires.ArrayTileModel;
import de.bluecolored.bluemap.core.map.hires.MaxCapacityReachedException;
import de.bluecolored.bluemap.core.map.hires.RenderSettings;
import de.bluecolored.bluemap.core.map.hires.TileModelView;
import de.bluecolored.bluemap.core.map.mask.Mask;
import de.bluecolored.bluemap.core.resources.ResourcePath;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.Variant;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.VariantSet;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.Variants;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.model.Element;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.model.Face;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.model.Model;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.model.TextureVariable;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.texture.Texture;
import de.bluecolored.bluemap.core.util.Direction;
import de.bluecolored.bluemap.core.util.Key;
import de.bluecolored.bluemap.core.util.math.Color;
import de.bluecolored.bluemap.core.world.BlockEntity;
import de.bluecolored.bluemap.core.world.BlockState;
import de.bluecolored.bluemap.core.world.DimensionType;
import de.bluecolored.bluemap.core.world.LightData;
import de.bluecolored.bluemap.core.world.biome.Biome;
import de.bluecolored.bluemap.core.world.block.BlockAccess;
import de.bluecolored.bluemap.core.world.block.BlockNeighborhood;
import de.bluecolored.bluemap.core.world.mca.MCAUtil;
import de.bluecolored.bluenbt.BlueNBT;
import de.bluecolored.bluenbt.NBTWriter;
import io.github.janguenter.bluemap.ae2.activation.CraftingRouteActivation;
import io.github.janguenter.bluemap.ae2.activation.ProfileActivation;
import io.github.janguenter.bluemap.ae2.diagnostics.BoundedDiagnostics;
import io.github.janguenter.bluemap.ae2.model.CableColor;
import io.github.janguenter.bluemap.ae2.model.CraftingBlockKind;
import io.github.janguenter.bluemap.ae2.model.CraftingGeometry;
import io.github.janguenter.bluemap.ae2.model.CraftingSnapshot;
import io.github.janguenter.bluemap.ae2.model.Direction6;
import io.github.janguenter.bluemap.ae2.profile.Ae219217CraftingProfile;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CraftingRendererTest {

    private static final int X = 11;
    private static final int Y = 82;
    private static final int Z = -7;
    private static final Position CENTER = new Position(X, Y, Z);
    private static final Key ORIGINAL = Key.parse("test:block/crafting_original");
    private static final Key OPAQUE = Key.parse("test:opaque");
    private static final Key NONNATIVE = Key.parse("test:nonnative");
    private static final float EPSILON = 0.000001F;

    @Test
    void allEightIsolatedKindsUseTheExactSourceDerivedTriangleCounts()
            throws Exception {
        Map<CraftingBlockKind, Integer> expected = Map.of(
                CraftingBlockKind.UNIT, 108,
                CraftingBlockKind.ACCELERATOR, 120,
                CraftingBlockKind.STORAGE_1K, 120,
                CraftingBlockKind.STORAGE_4K, 120,
                CraftingBlockKind.STORAGE_16K, 120,
                CraftingBlockKind.STORAGE_64K, 120,
                CraftingBlockKind.STORAGE_256K, 120,
                CraftingBlockKind.MONITOR, 114
        );

        for (CraftingBlockKind kind : CraftingBlockKind.values()) {
            Fixture fixture = fixture(exactState(kind, true, false, "north", 0));
            RecordingTileModel model = render(fixture).model();

            assertEquals(expected.get(kind), model.size(), kind.name());
            assertEquals(1, model.addInvocations(), kind.name());
            for (int triangle = 0; triangle < model.size(); triangle++) {
                assertArrayEquals(
                        new float[]{1F, 1F, 1F},
                        model.face(triangle).aos(),
                        0F,
                        kind.name()
                );
            }
        }
    }

    @Test
    void poweredStateMakesOnlyTheSpecificLightOverlayFullbright()
            throws Exception {
        Fixture off = fixture(exactState(
                CraftingBlockKind.ACCELERATOR,
                true,
                false,
                "north",
                0
        ));
        Fixture on = fixture(exactState(
                CraftingBlockKind.ACCELERATOR,
                true,
                true,
                "north",
                0
        ));
        RecordingTileModel offModel = render(off).model();
        RecordingTileModel onModel = render(on).model();
        int overlay = on.gallery().get(Key.parse(
                Ae219217CraftingProfile.ACCELERATOR_LIGHT_TEXTURE
        ));
        int base = on.gallery().get(Key.parse(
                Ae219217CraftingProfile.LIGHT_BASE_TEXTURE
        ));

        assertEquals(offModel.size(), onModel.size());
        assertTrue(assertLightForMaterial(offModel, overlay, 3, 2));
        assertTrue(assertLightForMaterial(onModel, overlay, 15, 15));
        assertTrue(assertLightForMaterial(onModel, base, 3, 2));
    }

    @Test
    void monitorUsesStrictDtoTintsAndItsPrimaryMeshIsSpinInvariant()
            throws Exception {
        BlockState spinZero = exactState(
                CraftingBlockKind.MONITOR,
                true,
                true,
                "south",
                0
        );
        BlockState spinThree = exactState(
                CraftingBlockKind.MONITOR,
                true,
                true,
                "south",
                3
        );
        Fixture first = fixture(spinZero, monitor(CableColor.CYAN), Map.of());
        Fixture second = fixture(spinThree, monitor(CableColor.CYAN), Map.of());
        RecordingTileModel firstModel = render(first).model();
        RecordingTileModel secondModel = render(second).model();

        assertSamePrimaryMesh(firstModel, secondModel);
        assertTint(
                first,
                firstModel,
                Ae219217CraftingProfile.MONITOR_LIGHT_BRIGHT_TEXTURE,
                CableColor.CYAN.brightRgb()
        );
        assertTint(
                first,
                firstModel,
                Ae219217CraftingProfile.MONITOR_LIGHT_MEDIUM_TEXTURE,
                CableColor.CYAN.mediumRgb()
        );
        assertTint(
                first,
                firstModel,
                Ae219217CraftingProfile.MONITOR_LIGHT_DARK_TEXTURE,
                CableColor.CYAN.darkRgb()
        );
    }

    @Test
    void everyExactUnformedKindDelegatesToItsStockModelWithoutDisablingRoute()
            throws Exception {
        for (CraftingBlockKind kind : CraftingBlockKind.values()) {
            Fixture fixture = fixture(exactState(kind, false, true, "east", 2));
            RecordingTileModel model = render(fixture).model();

            assertOriginalOnly(fixture, model);
            assertTrue(fixture.craftingActivation().isActive(), kind.name());
            assertTrue(fixture.diagnostics().isEmpty(), kind.name());
        }
    }

    @Test
    void nativeUnformedNeighborConnectsButOrdinaryNonnativeDoesNot()
            throws Exception {
        BlockState center = exactState(CraftingBlockKind.UNIT, true, false, "north", 0);
        Fixture connected = fixture(center, null, Map.of(
                offset(Direction6.EAST),
                exactState(CraftingBlockKind.STORAGE_1K, false, true, "north", 0)
        ));
        RecordingTileModel connectedModel = render(connected).model();
        assertFalse(hasBoundaryPlane(connectedModel, Direction6.EAST));

        Fixture nonnative = fixture(center, null, Map.of(
                offset(Direction6.EAST),
                BlockState.fromString(NONNATIVE.getFormatted())
        ));
        RecordingTileModel nonnativeModel = render(nonnative).model();
        assertEquals(108, nonnativeModel.size());
        assertTrue(hasBoundaryPlane(nonnativeModel, Direction6.EAST));
    }

    @Test
    void malformedCenterAndUnavailableMonitorDtoUseStockWithoutDisablingRoute()
            throws Exception {
        Fixture malformed = fixture(
                BlockState.fromString(
                        "ae2:crafting_unit[formed=true,powered=false,future=false]"
                ),
                null,
                Map.of()
        );
        assertOriginalOnly(malformed, render(malformed).model());
        assertEquals(
                List.of(BoundedDiagnostics.Event.CRAFTING_UNSUPPORTED_BLOCK_STATE),
                malformed.diagnostics()
        );
        assertTrue(malformed.craftingActivation().isActive());

        Fixture monitorWithoutDto = fixture(
                exactState(CraftingBlockKind.MONITOR, true, false, "north", 0),
                null,
                Map.of()
        );
        assertOriginalOnly(monitorWithoutDto, render(monitorWithoutDto).model());
        assertEquals(
                List.of(BoundedDiagnostics.Event.CRAFTING_MALFORMED_BLOCK_DATA),
                monitorWithoutDto.diagnostics()
        );
        assertTrue(monitorWithoutDto.profileActivation().isActive());
        assertTrue(monitorWithoutDto.craftingActivation().isActive());
    }

    @Test
    void opaqueNeighborCullsFaceAndUnsafeTopologyFallsBackAtomically()
            throws Exception {
        BlockState center = exactState(CraftingBlockKind.UNIT, true, false, "north", 0);
        Fixture opaque = fixture(center, null, Map.of(
                offset(Direction6.UP),
                BlockState.fromString(OPAQUE.getFormatted())
        ));
        RecordingTileModel opaqueModel = render(opaque).model();
        assertFalse(hasBoundaryPlane(opaqueModel, Direction6.UP));
        assertEquals(90, opaqueModel.size());

        for (BlockState unsafe : List.of(
                BlockState.MISSING,
                BlockState.fromString(
                        "ae2:crafting_unit[formed=true,powered=false,future=false]"
                ),
                BlockState.fromString("megacells:mega_crafting_unit"),
                BlockState.fromString("expandedae:exp_crafting_accelerator_1m")
        )) {
            Fixture fixture = fixture(center, null, Map.of(
                    offset(Direction6.NORTH), unsafe
            ));
            RecordingTileModel model = render(fixture).model();

            assertOriginalMaterials(fixture, model);
            assertTrue(model.size() > 0);
            assertTrue(fixture.profileActivation().isActive());
            assertTrue(fixture.craftingActivation().isActive());
        }
    }

    @Test
    void exactActiveExpandedAndMegaNeighborsConnectSymmetrically() throws Exception {
        BlockState center = exactState(CraftingBlockKind.UNIT, true, false, "north", 0);
        for (Map.Entry<String, String> extension : Map.of(
                "expandedae:exp_crafting_accelerator_1m", M45Runtime.EXPANDED_AE,
                "megacells:mega_crafting_unit", M45Runtime.MEGA_CELLS
        ).entrySet()) {
            BlockState exact = BlockState.fromString(
                    extension.getKey() + "[formed=true,powered=false]"
            );
            M45Runtime active = new M45Runtime();
            active.route(extension.getValue()).activate("exact-profile");
            Fixture connected = fixture(
                    center,
                    null,
                    Map.of(offset(Direction6.NORTH), exact),
                    TEST_SETTINGS,
                    3,
                    2,
                    true,
                    Map.of(),
                    new M45CraftingNeighborAccess(active)
            );
            assertFalse(hasBoundaryPlane(render(connected).model(), Direction6.NORTH));
            assertTrue(connected.diagnostics().isEmpty());

            Fixture inactive = fixture(
                    center,
                    null,
                    Map.of(offset(Direction6.NORTH), exact),
                    TEST_SETTINGS,
                    3,
                    2,
                    true,
                    Map.of(),
                    new M45CraftingNeighborAccess(new M45Runtime())
            );
            assertOriginalMaterials(inactive, render(inactive).model());
            assertEquals(
                    List.of(BoundedDiagnostics.Event
                            .CRAFTING_UNSUPPORTED_COMPATIBLE_NEIGHBOR),
                    inactive.diagnostics()
            );

            Fixture malformed = fixture(
                    center,
                    null,
                    Map.of(
                            offset(Direction6.NORTH),
                            BlockState.fromString(extension.getKey() + "[formed=true]")
                    ),
                    TEST_SETTINGS,
                    3,
                    2,
                    true,
                    Map.of(),
                    new M45CraftingNeighborAccess(active)
            );
            assertOriginalMaterials(malformed, render(malformed).model());
        }
    }

    @Test
    void topOnlyCaveAndMapColorMatchBlueMapWorldLightSemantics()
            throws Exception {
        BlockState center = exactState(CraftingBlockKind.UNIT, true, false, "north", 0);
        Fixture topOnly = fixture(center, null, Map.of(), TOP_ONLY_SETTINGS, 3, 2, true);
        RenderResult top = render(topOnly);
        assertEquals(18, top.model().size());
        for (int triangle = 0; triangle < top.model().size(); triangle++) {
            float[] positions = top.model().face(triangle).positions();
            assertEquals(1F, positions[1], 0F);
            assertEquals(1F, positions[4], 0F);
            assertEquals(1F, positions[7], 0F);
        }
        Color expected = expectedUnitTopMapColor(topOnly, 3F / 15F);
        assertEquals(expected.r, top.mapColor().r, EPSILON);
        assertEquals(expected.g, top.mapColor().g, EPSILON);
        assertEquals(expected.b, top.mapColor().b, EPSILON);
        assertEquals(expected.a, top.mapColor().a, EPSILON);

        Fixture cave = fixture(center, null, Map.of(), DARK_CAVE_SETTINGS, 0, 0, true);
        assertEquals(0, render(cave).model().size());

        BlockState powered = exactState(
                CraftingBlockKind.ACCELERATOR,
                true,
                true,
                "north",
                0
        );
        Fixture poweredCave = fixture(
                powered,
                null,
                Map.of(),
                DARK_CAVE_BLOCKLIGHT_SETTINGS,
                0,
                0,
                true
        );
        assertEquals(0, render(poweredCave).model().size());
    }

    @Test
    void outwardNeighborLightWinsForFaceEmissionAndTopMapColor()
            throws Exception {
        BlockState center = exactState(CraftingBlockKind.UNIT, true, false, "north", 0);
        Fixture fixture = fixture(
                center,
                null,
                Map.of(),
                TOP_ONLY_SETTINGS,
                3,
                2,
                true,
                Map.of(offset(Direction6.UP), new LightLevels(12, 9))
        );

        RenderResult result = render(fixture);

        assertEquals(18, result.model().size());
        for (int triangle = 0; triangle < result.model().size(); triangle++) {
            assertEquals(12, result.model().face(triangle).sunlight());
            assertEquals(9, result.model().face(triangle).blocklight());
        }
        Color expected = expectedUnitTopMapColor(fixture, 12F / 15F);
        assertEquals(expected.r, result.mapColor().r, EPSILON);
        assertEquals(expected.g, result.mapColor().g, EPSILON);
        assertEquals(expected.b, result.mapColor().b, EPSILON);
        assertEquals(expected.a, result.mapColor().a, EPSILON);
    }

    @Test
    void capacityUsesOneAtomicReservationAndPropagatesWithoutFallback()
            throws Exception {
        Fixture fixture = fixture(exactState(
                CraftingBlockKind.STORAGE_256K,
                true,
                true,
                "north",
                0
        ));
        RecordingTileModel model = new RecordingTileModel();
        model.add(1);
        model.setMaterialIndex(0, 777);
        model.resetAddInvocations();
        model.failCapacityOnNextAdd();
        Color color = new Color().set(0.1F, 0.2F, 0.3F, 0.4F, false);

        assertThrows(
                MaxCapacityReachedException.class,
                () -> fixture.renderer().render(
                        fixture.neighborhood(),
                        null,
                        new TileModelView(model),
                        color
                )
        );

        assertEquals(1, model.size());
        assertEquals(777, model.face(0).material());
        assertEquals(1, model.addInvocations());
        assertEquals(0.1F, color.r, 0F);
        assertTrue(fixture.profileActivation().isActive());
        assertTrue(fixture.craftingActivation().isActive());
    }

    @Test
    void emissionFailureRollsBackAndDisablesOnlyCraftingBeforeStockFallback()
            throws Exception {
        Fixture fixture = fixture(exactState(
                CraftingBlockKind.UNIT,
                true,
                false,
                "north",
                0
        ));
        RecordingTileModel model = new RecordingTileModel();
        model.add(1);
        model.setMaterialIndex(0, 777);
        model.failPositionOnNextWrite();

        fixture.renderer().render(
                fixture.neighborhood(),
                null,
                new TileModelView(model),
                new Color()
        );

        assertEquals(13, model.size());
        assertEquals(777, model.face(0).material());
        int original = fixture.gallery().get(ORIGINAL);
        for (int triangle = 1; triangle < model.size(); triangle++) {
            assertEquals(original, model.face(triangle).material());
        }
        assertTrue(fixture.profileActivation().isActive());
        assertTrue(fixture.craftingActivation().isDisabled());
        assertEquals(
                "crafting-render-callback-failed",
                fixture.craftingActivation().reason()
        );
    }

    @Test
    void inactiveOrResourceRejectedCraftingDelegatesWithoutTouchingCore()
            throws Exception {
        BlockState center = exactState(CraftingBlockKind.UNIT, true, false, "north", 0);
        Fixture inactive = fixture(center);
        inactive.craftingActivation().inactive(
                CraftingRouteActivation.Reason.AWAITING_EXACT_PROFILE
        );
        assertOriginalOnly(inactive, render(inactive).model());
        assertTrue(inactive.profileActivation().isActive());

        Fixture rejected = fixture(
                center,
                null,
                Map.of(),
                TEST_SETTINGS,
                3,
                2,
                false
        );
        assertOriginalOnly(rejected, render(rejected).model());
        assertTrue(rejected.profileActivation().isActive());
        assertTrue(rejected.craftingActivation().isActive());
    }

    private static Fixture fixture(BlockState center) throws Exception {
        BlockEntity entity = center.getId().getFormatted().equals(
                Ae219217CraftingProfile.CRAFTING_MONITOR_BLOCK
        ) ? monitor(CableColor.TRANSPARENT) : null;
        return fixture(center, entity, Map.of());
    }

    private static Fixture fixture(
            BlockState center,
            BlockEntity entity,
            Map<Position, BlockState> overrides
    ) throws Exception {
        return fixture(center, entity, overrides, TEST_SETTINGS, 3, 2, true);
    }

    private static Fixture fixture(
            BlockState center,
            BlockEntity entity,
            Map<Position, BlockState> overrides,
            RenderSettings settings,
            int defaultSkyLight,
            int defaultBlockLight,
            boolean resourcesSupported
    ) throws Exception {
        return fixture(
                center,
                entity,
                overrides,
                settings,
                defaultSkyLight,
                defaultBlockLight,
                resourcesSupported,
                Map.of()
        );
    }

    private static Fixture fixture(
            BlockState center,
            BlockEntity entity,
            Map<Position, BlockState> overrides,
            RenderSettings settings,
            int defaultSkyLight,
            int defaultBlockLight,
            boolean resourcesSupported,
            Map<Position, LightLevels> lights
    ) throws Exception {
        return fixture(
                center,
                entity,
                overrides,
                settings,
                defaultSkyLight,
                defaultBlockLight,
                resourcesSupported,
                lights,
                new M45CraftingNeighborAccess(new M45Runtime())
        );
    }

    private static Fixture fixture(
            BlockState center,
            BlockEntity entity,
            Map<Position, BlockState> overrides,
            RenderSettings settings,
            int defaultSkyLight,
            int defaultBlockLight,
            boolean resourcesSupported,
            Map<Position, LightLevels> lights,
            M45CraftingNeighborAccess extensionNeighbors
    ) throws Exception {
        ResourcePack resourcePack = M3dCraftingResourceModelsTest.exactResources();
        M3dCraftingResourceModelsTest.putTexture(
                resourcePack,
                ORIGINAL,
                0xFF224466
        );
        putStockAndOpaqueResources(resourcePack);
        TextureGallery gallery = new TextureGallery();
        gallery.put(resourcePack.getTextures());

        Map<Position, BlockState> states = new HashMap<>(overrides);
        states.put(CENTER, center);
        Map<Position, BlockEntity> entities = entity == null
                ? Map.of()
                : Map.of(CENTER, entity);
        BlockNeighborhood neighborhood = new BlockNeighborhood(
                new TestBlockAccess(
                        states,
                        entities,
                        defaultSkyLight,
                        defaultBlockLight,
                        lights
                ),
                resourcePack,
                settings,
                DimensionType.OVERWORLD
        );
        neighborhood.set(X, Y, Z);

        ProfileActivation profileActivation = new ProfileActivation();
        CraftingRouteActivation craftingActivation = new CraftingRouteActivation();
        List<BoundedDiagnostics.Event> diagnostics = new ArrayList<>();
        profileActivation.activate();
        craftingActivation.activate();
        return new Fixture(
                resourcePack,
                gallery,
                neighborhood,
                profileActivation,
                craftingActivation,
                diagnostics,
                new CraftingRenderer(
                        resourcePack,
                        gallery,
                        settings,
                        profileActivation,
                        craftingActivation,
                        ignored -> resourcesSupported,
                        diagnostics::add,
                        extensionNeighbors
                )
        );
    }

    private static RenderResult render(Fixture fixture) {
        RecordingTileModel model = new RecordingTileModel();
        Color mapColor = new Color();
        fixture.renderer().render(
                fixture.neighborhood(),
                null,
                new TileModelView(model),
                mapColor
        );
        return new RenderResult(model, mapColor);
    }

    private static boolean assertLightForMaterial(
            RecordingTileModel model,
            int material,
            int sunlight,
            int blocklight
    ) {
        boolean found = false;
        for (int triangle = 0; triangle < model.size(); triangle++) {
            FaceData face = model.face(triangle);
            if (face.material() == material) {
                found = true;
                assertEquals(sunlight, face.sunlight());
                assertEquals(blocklight, face.blocklight());
            }
        }
        return found;
    }

    private static void assertTint(
            Fixture fixture,
            RecordingTileModel model,
            String texture,
            int rgb
    ) {
        int material = fixture.gallery().get(Key.parse(texture));
        boolean found = false;
        float[] expected = {
            ((rgb >>> 16) & 0xFF) / 255F,
            ((rgb >>> 8) & 0xFF) / 255F,
            (rgb & 0xFF) / 255F
        };
        for (int triangle = 0; triangle < model.size(); triangle++) {
            FaceData face = model.face(triangle);
            if (face.material() == material) {
                found = true;
                assertArrayEquals(expected, face.color(), EPSILON);
                assertEquals(15, face.sunlight());
                assertEquals(15, face.blocklight());
            }
        }
        assertTrue(found, texture);
    }

    private static void assertSamePrimaryMesh(
            RecordingTileModel first,
            RecordingTileModel second
    ) {
        assertEquals(first.size(), second.size());
        for (int triangle = 0; triangle < first.size(); triangle++) {
            assertArrayEquals(
                    first.face(triangle).positions(),
                    second.face(triangle).positions(),
                    0F
            );
            assertArrayEquals(
                    first.face(triangle).uvs(),
                    second.face(triangle).uvs(),
                    0F
            );
            assertEquals(first.face(triangle).material(), second.face(triangle).material());
        }
    }

    private static void assertOriginalOnly(Fixture fixture, RecordingTileModel model) {
        assertEquals(12, model.size());
        assertOriginalMaterials(fixture, model);
    }

    private static void assertOriginalMaterials(
            Fixture fixture,
            RecordingTileModel model
    ) {
        int original = fixture.gallery().get(ORIGINAL);
        for (int triangle = 0; triangle < model.size(); triangle++) {
            assertEquals(original, model.face(triangle).material());
        }
    }

    private static Color expectedUnitTopMapColor(Fixture fixture, float illumination) {
        CraftingSnapshot snapshot = new CraftingSnapshot(
                CraftingBlockKind.UNIT,
                false,
                Direction6.NORTH,
                0,
                CableColor.TRANSPARENT,
                Set.of()
        );
        Color expected = new Color().set(0F, 0F, 0F, 0F, true);
        float opacity = 0F;
        for (CraftingGeometry.Quad quad : CraftingGeometry.forSnapshot(snapshot)) {
            if (quad.face() != Direction6.UP) {
                continue;
            }
            Texture texture = fixture.resourcePack().getTextures().get(
                    M3dCraftingResourceModels.texture(quad)
            );
            Color layer = new Color().set(texture.getColorPremultiplied());
            layer.r *= illumination;
            layer.g *= illumination;
            layer.b *= illumination;
            opacity = Math.max(opacity, layer.a);
            expected.add(layer);
        }
        expected.flatten().straight();
        expected.a = opacity;
        return expected;
    }

    private static boolean hasBoundaryPlane(
            RecordingTileModel model,
            Direction6 direction
    ) {
        float boundary = direction.stepX() + direction.stepY() + direction.stepZ() > 0
                ? 1F : 0F;
        for (int triangle = 0; triangle < model.size(); triangle++) {
            float[] positions = model.face(triangle).positions();
            boolean matches = true;
            for (int vertex = 0; vertex < positions.length; vertex += 3) {
                float coordinate = direction.stepX() != 0
                        ? positions[vertex]
                        : direction.stepY() != 0
                                ? positions[vertex + 1]
                                : positions[vertex + 2];
                matches &= coordinate == boundary;
            }
            if (matches) {
                return true;
            }
        }
        return false;
    }

    private static BlockState exactState(
            CraftingBlockKind kind,
            boolean formed,
            boolean powered,
            String facing,
            int spin
    ) {
        String id = blockId(kind);
        if (kind == CraftingBlockKind.MONITOR) {
            return BlockState.fromString(id
                    + "[facing=" + facing
                    + ",formed=" + formed
                    + ",powered=" + powered
                    + ",spin=" + spin + "]");
        }
        return BlockState.fromString(id
                + "[formed=" + formed
                + ",powered=" + powered + "]");
    }

    private static String blockId(CraftingBlockKind kind) {
        return switch (kind) {
            case UNIT -> Ae219217CraftingProfile.CRAFTING_UNIT_BLOCK;
            case ACCELERATOR -> Ae219217CraftingProfile.CRAFTING_ACCELERATOR_BLOCK;
            case STORAGE_1K -> Ae219217CraftingProfile.CRAFTING_STORAGE_1K_BLOCK;
            case STORAGE_4K -> Ae219217CraftingProfile.CRAFTING_STORAGE_4K_BLOCK;
            case STORAGE_16K -> Ae219217CraftingProfile.CRAFTING_STORAGE_16K_BLOCK;
            case STORAGE_64K -> Ae219217CraftingProfile.CRAFTING_STORAGE_64K_BLOCK;
            case STORAGE_256K -> Ae219217CraftingProfile.CRAFTING_STORAGE_256K_BLOCK;
            case MONITOR -> Ae219217CraftingProfile.CRAFTING_MONITOR_BLOCK;
        };
    }

    private static Ae2CraftingMonitorBlockEntityData monitor(CableColor color)
            throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (NBTWriter writer = new NBTWriter(bytes)) {
            writer.beginCompound();
            writer.name("id").value(Ae219217CraftingProfile.CRAFTING_MONITOR_BLOCK);
            writer.name("x").value(X);
            writer.name("y").value(Y);
            writer.name("z").value(Z);
            writer.name("paintedColor").value((byte) color.ordinal());
            writer.endCompound();
        }
        BlueNBT blueNbt = MCAUtil.addCommonNbtSettings(new BlueNBT());
        return blueNbt.read(
                new ByteArrayInputStream(bytes.toByteArray()),
                Ae2CraftingMonitorBlockEntityData.class
        );
    }

    private static void putStockAndOpaqueResources(ResourcePack resourcePack) {
        EnumMap<Direction, Face> faces = new EnumMap<>(Direction.class);
        for (Direction direction : Direction.values()) {
            faces.put(direction, new Face(
                    new Vector4f(0F, 0F, 16F, 16F),
                    new TextureVariable(new ResourcePath<Texture>(ORIGINAL)),
                    direction,
                    0,
                    -1
            ));
        }
        Model model = new Model(new Element(
                Vector3f.ZERO,
                new Vector3f(16F, 16F, 16F),
                faces
        ));
        model.calculateProperties(resourcePack.getTextures());
        Key modelKey = Key.parse("test:block/crafting_original_model");
        resourcePack.getModels().put(modelKey, model);
        Variant variant = new Variant(new ResourcePath<Model>(modelKey));
        de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.BlockState state =
                new de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.BlockState(
                        new Variants(new VariantSet[0], new VariantSet(variant))
                );
        for (CraftingBlockKind kind : CraftingBlockKind.values()) {
            resourcePack.getBlockStates().put(Key.parse(blockId(kind)), state);
        }
        resourcePack.getBlockStates().put(OPAQUE, state);
    }

    private static Position offset(Direction6 direction) {
        return new Position(
                X + direction.stepX(),
                Y + direction.stepY(),
                Z + direction.stepZ()
        );
    }

    private static final RenderSettings TEST_SETTINGS = settings(false, false, false);
    private static final RenderSettings TOP_ONLY_SETTINGS = settings(true, false, false);
    private static final RenderSettings DARK_CAVE_SETTINGS = settings(false, true, false);
    private static final RenderSettings DARK_CAVE_BLOCKLIGHT_SETTINGS =
            settings(false, true, true);

    private static RenderSettings settings(
            boolean topOnly,
            boolean cave,
            boolean caveDetectionUsesBlockLight
    ) {
        return new RenderSettings() {
            @Override
            public int getRemoveCavesBelowY() {
                return cave ? Y + 1 : Integer.MIN_VALUE;
            }

            @Override
            public int getCaveDetectionOceanFloor() {
                return 0;
            }

            @Override
            public boolean isCaveDetectionUsesBlockLight() {
                return caveDetectionUsesBlockLight;
            }

            @Override
            public float getAmbientLight() {
                return 0F;
            }

            @Override
            public boolean isRenderEdges() {
                return false;
            }

            @Override
            public Mask getRenderMask() {
                return Mask.ALL;
            }

            @Override
            public boolean isSaveHiresLayer() {
                return false;
            }

            @Override
            public boolean isRenderTopOnly() {
                return topOnly;
            }
        };
    }

    private static final class TestBlockAccess implements BlockAccess {

        private final Map<Position, BlockState> states;
        private final Map<Position, BlockEntity> entities;
        private final int defaultSkyLight;
        private final int defaultBlockLight;
        private final Map<Position, LightLevels> lights;
        private int x;
        private int y;
        private int z;

        private TestBlockAccess(
                Map<Position, BlockState> states,
                Map<Position, BlockEntity> entities,
                int defaultSkyLight,
                int defaultBlockLight,
                Map<Position, LightLevels> lights
        ) {
            this.states = states;
            this.entities = entities;
            this.defaultSkyLight = defaultSkyLight;
            this.defaultBlockLight = defaultBlockLight;
            this.lights = lights;
        }

        @Override
        public void set(int newX, int newY, int newZ) {
            x = newX;
            y = newY;
            z = newZ;
        }

        @Override
        public BlockAccess copy() {
            return new TestBlockAccess(
                    states,
                    entities,
                    defaultSkyLight,
                    defaultBlockLight,
                    lights
            );
        }

        @Override
        public int getX() {
            return x;
        }

        @Override
        public int getY() {
            return y;
        }

        @Override
        public int getZ() {
            return z;
        }

        @Override
        public BlockState getBlockState() {
            return states.getOrDefault(new Position(x, y, z), BlockState.AIR);
        }

        @Override
        public LightData getLightData() {
            LightLevels override = lights.get(new Position(x, y, z));
            return override == null
                    ? new LightData(defaultSkyLight, defaultBlockLight)
                    : new LightData(override.sunlight(), override.blocklight());
        }

        @Override
        public Biome getBiome() {
            return Biome.DEFAULT;
        }

        @Override
        public BlockEntity getBlockEntity() {
            return entities.get(new Position(x, y, z));
        }

        @Override
        public boolean hasOceanFloorY() {
            return false;
        }

        @Override
        public int getOceanFloorY() {
            return 0;
        }
    }

    private static final class RecordingTileModel extends ArrayTileModel {

        private final List<FaceData> faces = new ArrayList<>();
        private int addInvocations;
        private boolean failCapacity;
        private boolean failPosition;

        private RecordingTileModel() {
            super(256);
        }

        private FaceData face(int face) {
            return faces.get(face);
        }

        private int addInvocations() {
            return addInvocations;
        }

        private void resetAddInvocations() {
            addInvocations = 0;
        }

        private void failCapacityOnNextAdd() {
            failCapacity = true;
        }

        private void failPositionOnNextWrite() {
            failPosition = true;
        }

        @Override
        public int add(int count) {
            addInvocations++;
            if (failCapacity) {
                failCapacity = false;
                throw new MaxCapacityReachedException("injected capacity");
            }
            int start = super.add(count);
            while (faces.size() < size()) {
                faces.add(new FaceData());
            }
            return start;
        }

        @Override
        public RecordingTileModel reset(int size) {
            super.reset(size);
            while (faces.size() > size) {
                faces.removeLast();
            }
            return this;
        }

        @Override
        public RecordingTileModel setPositions(
                int face,
                float x1, float y1, float z1,
                float x2, float y2, float z2,
                float x3, float y3, float z3
        ) {
            if (failPosition) {
                failPosition = false;
                throw new IllegalStateException("injected emission failure");
            }
            super.setPositions(face, x1, y1, z1, x2, y2, z2, x3, y3, z3);
            face(face).positions = new float[]{
                x1, y1, z1,
                x2, y2, z2,
                x3, y3, z3
            };
            return this;
        }

        @Override
        public RecordingTileModel setUvs(
                int face,
                float u1, float v1,
                float u2, float v2,
                float u3, float v3
        ) {
            super.setUvs(face, u1, v1, u2, v2, u3, v3);
            face(face).uvs = new float[]{u1, v1, u2, v2, u3, v3};
            return this;
        }

        @Override
        public RecordingTileModel setAOs(
                int face,
                float first,
                float second,
                float third
        ) {
            super.setAOs(face, first, second, third);
            face(face).aos = new float[]{first, second, third};
            return this;
        }

        @Override
        public RecordingTileModel setColor(int face, float red, float green, float blue) {
            super.setColor(face, red, green, blue);
            face(face).color = new float[]{red, green, blue};
            return this;
        }

        @Override
        public RecordingTileModel setSunlight(int face, int sunlight) {
            super.setSunlight(face, sunlight);
            face(face).sunlight = sunlight;
            return this;
        }

        @Override
        public RecordingTileModel setBlocklight(int face, int blocklight) {
            super.setBlocklight(face, blocklight);
            face(face).blocklight = blocklight;
            return this;
        }

        @Override
        public RecordingTileModel setMaterialIndex(int face, int material) {
            super.setMaterialIndex(face, material);
            face(face).material = material;
            return this;
        }
    }

    private static final class FaceData {

        private float[] positions;
        private float[] uvs;
        private float[] aos;
        private float[] color;
        private int sunlight;
        private int blocklight;
        private int material;

        private float[] positions() {
            return positions;
        }

        private float[] uvs() {
            return uvs;
        }

        private float[] aos() {
            return aos;
        }

        private float[] color() {
            return color;
        }

        private int sunlight() {
            return sunlight;
        }

        private int blocklight() {
            return blocklight;
        }

        private int material() {
            return material;
        }
    }

    private record Fixture(
            ResourcePack resourcePack,
            TextureGallery gallery,
            BlockNeighborhood neighborhood,
            ProfileActivation profileActivation,
            CraftingRouteActivation craftingActivation,
            List<BoundedDiagnostics.Event> diagnostics,
            CraftingRenderer renderer
    ) {
    }

    private record RenderResult(RecordingTileModel model, Color mapColor) {
    }

    private record LightLevels(int sunlight, int blocklight) {
    }

    private record Position(int x, int y, int z) {
    }
}
