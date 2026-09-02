/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.adapter.bluemap523;

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
import de.bluecolored.bluemap.core.util.math.MatrixM4f;
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
import io.github.janguenter.bluemap.ae2.activation.M3CompletionRouteActivation;
import io.github.janguenter.bluemap.ae2.activation.ProfileActivation;
import io.github.janguenter.bluemap.ae2.diagnostics.BoundedDiagnostics;
import io.github.janguenter.bluemap.ae2.model.CableColor;
import io.github.janguenter.bluemap.ae2.model.Direction6;
import io.github.janguenter.bluemap.ae2.model.PaintGeometry;
import io.github.janguenter.bluemap.ae2.model.PaintSnapshot;
import io.github.janguenter.bluemap.ae2.profile.Ae219217M3CompletionProfile;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class M3CompletionRendererTest {

    private static final int X = 17;
    private static final int Y = 74;
    private static final int Z = -9;
    private static final Position CENTER = new Position(X, Y, Z);
    private static final Key WRONG_ENTITY = Key.parse("test:wrong_entity");

    @Test
    void paintUsesPersistedOrderTintLightAndAoWithoutAmbientOcclusion()
            throws Exception {
        Map<Position, BlockState> states = new HashMap<>();
        states.put(CENTER, BlockState.fromString(
                "ae2:paint[facing=up,light_level=1]"
        ));
        states.put(CENTER.plus(0, 1, 0), BlockState.fromString("minecraft:stone"));
        states.put(CENTER.plus(-1, 0, 0), BlockState.fromString("minecraft:stone"));
        Fixture fixture = fixture(
                states,
                Map.of(CENTER, paintEntity()),
                true
        );

        RenderResult result = render(fixture);

        assertEquals(4, result.model().size());
        assertEquals(1, result.model().addInvocations());
        for (int triangle = 0; triangle < 2; triangle++) {
            FaceData face = result.model().face(triangle);
            assertArrayEquals(new float[]{1F, 1F, 1F}, face.aos(), 0F);
            assertEquals(7, face.sunlight());
            assertEquals(4, face.blocklight());
        }
        for (int triangle = 2; triangle < 4; triangle++) {
            FaceData face = result.model().face(triangle);
            assertArrayEquals(new float[]{1F, 1F, 1F}, face.aos(), 0F);
            assertEquals(15, face.sunlight());
            assertEquals(15, face.blocklight());
        }
        assertTrue(fixture.completion().isActive());
        assertTrue(fixture.diagnostics().isEmpty());
    }

    @Test
    void primitiveLightUsesCenterOutwardMaximumAndProducesHostMapColor()
            throws Exception {
        byte[] dots = paintDots(Direction6.DOWN, CableColor.RED, false);
        Map<Position, BlockState> states = new HashMap<>();
        states.put(CENTER, BlockState.fromString(
                "ae2:paint[facing=down,light_level=0]"
        ));
        states.put(CENTER.plus(0, -1, 0), BlockState.fromString("minecraft:stone"));
        Map<Position, LightLevels> lights = Map.of(
                CENTER, new LightLevels(3, 2),
                CENTER.plus(0, 1, 0), new LightLevels(12, 9)
        );
        Fixture fixture = fixture(
                states,
                Map.of(CENTER, paintEntity(dots)),
                true,
                TEST_SETTINGS,
                lights
        );

        RenderResult result = render(fixture);

        assertEquals(2, result.model().size());
        for (FaceData face : result.model().faces()) {
            assertEquals(12, face.sunlight());
            assertEquals(9, face.blocklight());
        }
        Color expected = expectedPaintMapColor(fixture.resourcePack(), dots, 12F / 15F);
        assertEquals(expected.r, result.mapColor().r, 0.000001F);
        assertEquals(expected.g, result.mapColor().g, 0.000001F);
        assertEquals(expected.b, result.mapColor().b, 0.000001F);
        assertEquals(expected.a, result.mapColor().a, 0.000001F);
        assertTrue(fixture.completion().isActive());
        assertTrue(fixture.diagnostics().isEmpty());
    }

    @Test
    void primitiveTopOnlyAndDarkCaveSuppressionDoNotDisableTheRoute()
            throws Exception {
        BlockState pylon = BlockState.fromString(
                "ae2:spatial_pylon[powered_on=false]"
        );
        Map<Position, BlockState> states = Map.of(CENTER, pylon);
        Map<Position, BlockEntity> entities = Map.of(
                CENTER,
                entity(Key.parse("ae2:spatial_pylon"), CENTER)
        );
        Fixture topOnly = fixture(
                states,
                entities,
                true,
                settings(true, false),
                Map.of()
        );
        assertEquals(4, render(topOnly).model().size());
        assertTrue(topOnly.completion().isActive());
        assertTrue(topOnly.diagnostics().isEmpty());

        Fixture cave = fixture(
                states,
                entities,
                true,
                settings(false, true),
                darkLights()
        );
        RenderResult hidden = render(cave);
        assertEquals(0, hidden.model().size());
        assertEquals(0F, hidden.mapColor().a, 0F);
        assertTrue(cave.completion().isActive());
        assertTrue(cave.diagnostics().isEmpty());
    }

    @Test
    void westChestAppliesTheExactNinetyDegreeCenterTransform() throws Exception {
        BlockEntity chestEntity = entity(Key.parse("ae2:sky_chest"), CENTER);
        Fixture south = fixture(
                Map.of(CENTER, BlockState.fromString(
                        "ae2:sky_stone_chest[facing=south,waterlogged=false]"
                )),
                Map.of(CENTER, chestEntity),
                true
        );
        Fixture west = fixture(
                Map.of(CENTER, BlockState.fromString(
                        "ae2:sky_stone_chest[facing=west,waterlogged=false]"
                )),
                Map.of(CENTER, chestEntity),
                true
        );

        float[] southPositions = render(south).model().face(0).positions();
        float[] westPositions = render(west).model().face(0).positions();
        for (int vertex = 0; vertex < 3; vertex++) {
            int offset = vertex * 3;
            assertEquals(
                    1F - southPositions[offset + 2],
                    westPositions[offset],
                    0.000001F
            );
            assertEquals(
                    southPositions[offset + 1],
                    westPositions[offset + 1],
                    0.000001F
            );
            assertEquals(
                    southPositions[offset],
                    westPositions[offset + 2],
                    0.000001F
            );
        }
    }

    @Test
    void bothClosedChestsEmitExactCustomCountAndMalformedFallbackTextures()
            throws Exception {
        for (String block : List.of(
                Ae219217M3CompletionProfile.SKY_STONE_CHEST_BLOCK,
                Ae219217M3CompletionProfile.SMOOTH_SKY_STONE_CHEST_BLOCK
        )) {
            BlockState state = BlockState.fromString(
                    block + "[facing=west,waterlogged=false]"
            );
            Key expectedCustomTexture = M3CompletionResourceModels.chestTexture(block);
            Key expectedFallbackTexture = Key.parse(
                    block.equals(Ae219217M3CompletionProfile.SKY_STONE_CHEST_BLOCK)
                            ? Ae219217M3CompletionProfile.SKY_STONE_FALLBACK_TEXTURE
                            : Ae219217M3CompletionProfile.SMOOTH_SKY_STONE_FALLBACK_TEXTURE
            );
            Fixture custom = fixture(
                    Map.of(CENTER, state),
                    Map.of(CENTER, entity(
                            Key.parse(Ae219217M3CompletionProfile
                                    .SKY_STONE_CHEST_BLOCK_ENTITY_ID),
                            CENTER
                    )),
                    true
            );
            RecordingTileModel customModel = render(custom).model();
            assertEquals(36, customModel.size());
            assertTrue(customModel.faces().stream().allMatch(face ->
                    face.material() == custom.gallery().get(expectedCustomTexture)));

            Fixture malformed = fixture(
                    Map.of(CENTER, state),
                    Map.of(CENTER, entity(WRONG_ENTITY, CENTER)),
                    true
            );
            RecordingTileModel fallback = render(malformed).model();
            assertEquals(12, fallback.size());
            assertTrue(fallback.faces().stream().allMatch(face ->
                    face.material() == malformed.gallery().get(expectedFallbackTexture)));
            assertEquals(
                    List.of(BoundedDiagnostics.Event
                            .M3_COMPLETION_MALFORMED_BLOCK_DATA),
                    malformed.diagnostics()
            );
            assertTrue(malformed.completion().isActive());

            Fixture nullId = fixture(
                    Map.of(CENTER, state),
                    Map.of(CENTER, entity(null, CENTER)),
                    true
            );
            RecordingTileModel nullIdFallback = render(nullId).model();
            assertEquals(12, nullIdFallback.size());
            assertTrue(nullIdFallback.faces().stream().allMatch(face ->
                    face.material() == nullId.gallery().get(expectedFallbackTexture)));
            assertEquals(
                    List.of(BoundedDiagnostics.Event
                            .M3_COMPLETION_MALFORMED_BLOCK_DATA),
                    nullId.diagnostics()
            );
            assertTrue(nullId.completion().isActive());
        }
    }

    @Test
    void crankAndInscriberUseOneAtomicHostReservationAtExactCounts()
            throws Exception {
        Map<BlockState, Integer> cases = Map.of(
                BlockState.fromString("ae2:crank[facing=up]"), 34,
                BlockState.fromString(
                        "ae2:inscriber[facing=north,spin=2,waterlogged=false]"
                ), 78
        );
        for (Map.Entry<BlockState, Integer> entry : cases.entrySet()) {
            String entityId = entry.getKey().getId().getFormatted();
            Fixture fixture = fixture(
                    Map.of(CENTER, entry.getKey()),
                    Map.of(CENTER, entity(Key.parse(entityId), CENTER)),
                    true
            );
            RecordingTileModel model = render(fixture).model();
            assertEquals(entry.getValue(), model.size(), entityId);
            assertEquals(1, model.addInvocations(), entityId);
        }
    }

    @Test
    void crankAndInscriberShellPreserveHostAoWhileManualStampsRemainUnshaded()
            throws Exception {
        for (BlockState state : List.of(
                BlockState.fromString("ae2:crank[facing=up]"),
                BlockState.fromString(
                        "ae2:inscriber[facing=north,spin=0,waterlogged=false]"
                )
        )) {
            Map<Position, BlockState> states = new HashMap<>();
            states.put(CENTER, state);
            int occludingAxes = state.getId().getFormatted().equals("ae2:crank")
                    ? 1 : 2;
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        int changedAxes = Math.abs(dx) + Math.abs(dy) + Math.abs(dz);
                        if (changedAxes >= occludingAxes) {
                            states.put(CENTER.plus(dx, dy, dz),
                                    BlockState.fromString("minecraft:stone"));
                        }
                    }
                }
            }
            Fixture fixture = fixture(
                    states,
                    Map.of(CENTER, entity(state.getId(), CENTER)),
                    true
            );

            RecordingTileModel model = render(fixture).model();
            int shellMaterial = fixture.gallery().get(Key.parse(
                    state.getId().getFormatted().equals("ae2:crank")
                            ? Ae219217M3CompletionProfile.CRANK_TEXTURE
                            : Ae219217M3CompletionProfile.INSCRIBER_TEXTURE
            ));
            List<FaceData> shell = model.faces().stream()
                    .filter(face -> face.material() == shellMaterial)
                    .toList();
            assertTrue(!shell.isEmpty());
            assertTrue(shell.stream().flatMapToDouble(face ->
                    java.util.stream.DoubleStream.of(
                            face.aos()[0], face.aos()[1], face.aos()[2]
                    )).anyMatch(ao -> ao < 1D), state.getId().getFormatted());

            if (state.getId().getFormatted().equals("ae2:inscriber")) {
                int stampMaterial = fixture.gallery().get(
                        M3CompletionResourceModels.INSCRIBER_INSIDE_TEXTURE
                );
                List<FaceData> stamps = model.faces().stream()
                        .filter(face -> face.material() == stampMaterial)
                        .toList();
                assertEquals(12, stamps.size());
                assertTrue(stamps.stream().allMatch(face ->
                        java.util.Arrays.equals(
                                new float[]{1F, 1F, 1F}, face.aos()
                        )));
            }
        }
    }

    @Test
    void crankAndInscriberCapacityFailureCannotLeaveAnIntraBlockPrefix()
            throws Exception {
        for (BlockState state : List.of(
                BlockState.fromString("ae2:crank[facing=east]"),
                BlockState.fromString(
                        "ae2:inscriber[facing=down,spin=1,waterlogged=true]"
                )
        )) {
            Fixture fixture = fixture(
                    Map.of(CENTER, state),
                    Map.of(CENTER, entity(state.getId(), CENTER)),
                    true
            );
            RecordingTileModel model = new RecordingTileModel();
            model.add(1);
            model.setMaterialIndex(0, 719);
            model.resetAddInvocations();
            model.failCapacityOnNextAdd();

            assertThrows(
                    MaxCapacityReachedException.class,
                    () -> fixture.renderer().render(
                            fixture.neighborhood(),
                            null,
                            new TileModelView(model),
                            new Color()
                    )
            );
            assertEquals(1, model.size());
            assertEquals(719, model.face(0).material());
            assertEquals(1, model.addInvocations());
            assertTrue(fixture.completion().isActive());
        }
    }

    @Test
    void spatialPylonInfersIsolatedAndOppositeAxisLines()
            throws Exception {
        BlockState pylon = BlockState.fromString(
                "ae2:spatial_pylon[powered_on=true]"
        );
        Fixture isolated = fixture(
                Map.of(CENTER, pylon),
                Map.of(CENTER, entity(Key.parse("ae2:spatial_pylon"), CENTER)),
                true
        );
        RecordingTileModel isolatedModel = render(isolated).model();
        assertEquals(24, isolatedModel.size());
        assertEquals(2, distinctMaterials(isolatedModel));

        Map<Position, BlockState> formedStates = new HashMap<>();
        Map<Position, BlockEntity> formedEntities = new HashMap<>();
        formedStates.put(CENTER, pylon);
        formedEntities.put(CENTER, entity(Key.parse("ae2:spatial_pylon"), CENTER));
        for (Direction6 direction : List.of(Direction6.WEST, Direction6.EAST)) {
            Position position = CENTER.plus(
                    direction.stepX(), direction.stepY(), direction.stepZ()
            );
            formedStates.put(position, pylon);
            formedEntities.put(position, entity(Key.parse("ae2:spatial_pylon"), position));
        }
        Fixture formed = fixture(formedStates, formedEntities, true);
        RecordingTileModel formedModel = render(formed).model();
        assertEquals(24, formedModel.size());
        assertEquals(4, distinctMaterials(formedModel));
        assertTrue(formedModel.faces().stream().allMatch(face ->
                face.sunlight() != 15 || face.blocklight() != 15));

    }

    @Test
    void boundedBendAndBranchComponentsRenderEveryMemberUnformed()
            throws Exception {
        assertAmbiguousPylonComponent(Set.of(
                new Position(0, 0, 0),
                new Position(1, 0, 0),
                new Position(0, 0, 1)
        ));
        assertAmbiguousPylonComponent(Set.of(
                new Position(0, 0, 0),
                new Position(-1, 0, 0),
                new Position(1, 0, 0),
                new Position(0, 1, 0)
        ));
    }

    @Test
    void longStraightPylonLineBypassesTheNeighborhoodModuloCache()
            throws Exception {
        Set<Position> line = new LinkedHashSet<>();
        for (int x = 0; x < 32; x++) {
            line.add(new Position(x, 0, 0));
        }
        for (Position target : List.of(
                new Position(0, 0, 0),
                new Position(15, 0, 0),
                new Position(31, 0, 0)
        )) {
            Fixture fixture = pylonComponentFixture(line, target);
            RecordingTileModel model = render(fixture).model();
            assertEquals(24, model.size(), target.toString());
            assertEquals(4, distinctMaterials(model), target.toString());
            assertTrue(fixture.diagnostics().isEmpty(), target.toString());
            assertTrue(fixture.completion().isActive(), target.toString());
        }
    }

    @Test
    void missingMalformedAndCappedPylonComponentsStillUseStockFallback()
            throws Exception {
        BlockState pylon = BlockState.fromString(
                "ae2:spatial_pylon[powered_on=false]"
        );
        Position east = CENTER.plus(1, 0, 0);

        Fixture missing = fixture(
                Map.of(CENTER, pylon, east, BlockState.MISSING),
                Map.of(CENTER, entity(Key.parse("ae2:spatial_pylon"), CENTER)),
                true
        );
        assertIncompletePylonFallback(missing);

        Fixture malformed = fixture(
                Map.of(CENTER, pylon, east, pylon),
                Map.of(CENTER, entity(Key.parse("ae2:spatial_pylon"), CENTER)),
                true
        );
        assertIncompletePylonFallback(malformed);

        Fixture nullIdNeighbor = fixture(
                Map.of(CENTER, pylon, east, pylon),
                Map.of(
                        CENTER,
                        entity(Key.parse("ae2:spatial_pylon"), CENTER),
                        east,
                        entity(null, east)
                ),
                true
        );
        assertIncompletePylonFallback(nullIdNeighbor);

        Position eastTwo = CENTER.plus(2, 0, 0);
        Fixture distantNullId = fixture(
                Map.of(CENTER, pylon, east, pylon, eastTwo, pylon),
                Map.of(
                        CENTER,
                        entity(Key.parse("ae2:spatial_pylon"), CENTER),
                        east,
                        entity(Key.parse("ae2:spatial_pylon"), east),
                        eastTwo,
                        entity(null, eastTwo)
                ),
                true
        );
        assertIncompletePylonFallback(distantNullId);

        Map<Position, BlockState> cappedStates = new HashMap<>();
        Map<Position, BlockEntity> cappedEntities = new HashMap<>();
        for (int offset = 0;
                offset <= Ae219217M3CompletionProfile
                        .SPATIAL_PYLON_COMPONENT_MAX_BLOCKS;
                offset++) {
            Position position = CENTER.plus(offset, 0, 0);
            cappedStates.put(position, pylon);
            cappedEntities.put(
                    position,
                    entity(Key.parse("ae2:spatial_pylon"), position)
            );
        }
        assertIncompletePylonFallback(fixture(cappedStates, cappedEntities, true));
    }

    @Test
    void unexpectedEmissionFailureRollsBackAndDisablesOnlyCompletionRoute()
            throws Exception {
        BlockState chest = BlockState.fromString(
                "ae2:sky_stone_chest[facing=south,waterlogged=false]"
        );
        Fixture fixture = fixture(
                Map.of(CENTER, chest),
                Map.of(CENTER, entity(Key.parse("ae2:sky_chest"), CENTER)),
                true
        );
        RecordingTileModel model = new RecordingTileModel();
        model.add(1);
        model.setMaterialIndex(0, 719);
        model.failPositionOnNextWrite();

        fixture.renderer().render(
                fixture.neighborhood(),
                null,
                new TileModelView(model),
                new Color()
        );

        assertEquals(13, model.size());
        assertEquals(719, model.face(0).material());
        assertTrue(fixture.profile().isActive());
        assertTrue(fixture.completion().isDisabled());
        assertEquals(
                "m3-completion-render-callback-failed",
                fixture.completion().reason()
        );
        assertEquals(
                List.of(BoundedDiagnostics.Event.M3_COMPLETION_RENDER_FAILED),
                fixture.diagnostics()
        );
    }

    private static Fixture fixture(
            Map<Position, BlockState> states,
            Map<Position, BlockEntity> entities,
            boolean resourcesSupported
    ) throws Exception {
        return fixture(
                states,
                entities,
                resourcesSupported,
                TEST_SETTINGS,
                Map.of()
        );
    }

    private static Fixture fixture(
            Map<Position, BlockState> states,
            Map<Position, BlockEntity> entities,
            boolean resourcesSupported,
            RenderSettings renderSettings,
            Map<Position, LightLevels> lights
    ) throws Exception {
        ResourcePack resourcePack = M3CompletionResourceModelsTest.exactResources();
        putStockResources(resourcePack);
        TextureGallery gallery = new TextureGallery();
        gallery.put(resourcePack.getTextures());
        BlockNeighborhood neighborhood = new BlockNeighborhood(
                new TestBlockAccess(states, entities, lights),
                resourcePack,
                renderSettings,
                DimensionType.OVERWORLD
        );
        neighborhood.set(X, Y, Z);
        ProfileActivation profile = new ProfileActivation();
        M3CompletionRouteActivation completion = new M3CompletionRouteActivation();
        List<BoundedDiagnostics.Event> diagnostics = new ArrayList<>();
        profile.activate();
        completion.activate();
        return new Fixture(
                resourcePack,
                gallery,
                neighborhood,
                profile,
                completion,
                diagnostics,
                new M3CompletionRenderer(
                        resourcePack,
                        gallery,
                        renderSettings,
                        profile,
                        completion,
                        ignored -> resourcesSupported,
                        diagnostics::add
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

    private static Color expectedPaintMapColor(
            ResourcePack resourcePack,
            byte[] dots,
            float illumination
    ) {
        PaintGeometry.Quad quad = PaintGeometry.forSnapshot(
                PaintSnapshot.decode(dots)
        ).getFirst();
        Color layer = new Color().set(
                resourcePack.getTextures().get(
                        M3CompletionResourceModels.paintTexture(quad)
                ).getColorPremultiplied()
        );
        layer.r *= ((quad.rgb() >> 16) & 0xff) / 255F * illumination;
        layer.g *= ((quad.rgb() >> 8) & 0xff) / 255F * illumination;
        layer.b *= (quad.rgb() & 0xff) / 255F * illumination;
        float opacity = layer.a;
        Color expected = new Color().set(0F, 0F, 0F, 0F, true).add(layer);
        expected.flatten().straight();
        expected.a = opacity;
        return expected;
    }

    private static int distinctMaterials(RecordingTileModel model) {
        return (int) model.faces().stream().map(FaceData::material).distinct().count();
    }

    private static void assertAmbiguousPylonComponent(Set<Position> component)
            throws Exception {
        for (Position target : component) {
            Fixture fixture = pylonComponentFixture(component, target);
            RecordingTileModel model = render(fixture).model();
            int base = fixture.gallery().get(Key.parse(
                    Ae219217M3CompletionProfile.PYLON_BASE_TEXTURE
            ));
            int dim = fixture.gallery().get(Key.parse(
                    Ae219217M3CompletionProfile.PYLON_DIM_TEXTURE
            ));
            assertEquals(24, model.size(), target.toString());
            assertEquals(
                    12,
                    model.faces().stream().filter(face -> face.material() == base).count(),
                    target.toString()
            );
            assertEquals(
                    12,
                    model.faces().stream().filter(face -> face.material() == dim).count(),
                    target.toString()
            );
            assertEquals(
                    List.of(BoundedDiagnostics.Event.M3_COMPLETION_INVALID_TOPOLOGY),
                    fixture.diagnostics(),
                    target.toString()
            );
            assertTrue(fixture.completion().isActive(), target.toString());
        }
    }

    private static Fixture pylonComponentFixture(
            Set<Position> component,
            Position target
    ) throws Exception {
        BlockState pylon = BlockState.fromString(
                "ae2:spatial_pylon[powered_on=false]"
        );
        Map<Position, BlockState> states = new HashMap<>();
        Map<Position, BlockEntity> entities = new HashMap<>();
        for (Position position : component) {
            Position translated = CENTER.plus(
                    position.x() - target.x(),
                    position.y() - target.y(),
                    position.z() - target.z()
            );
            states.put(translated, pylon);
            entities.put(
                    translated,
                    entity(Key.parse("ae2:spatial_pylon"), translated)
            );
        }
        return fixture(states, entities, true);
    }

    private static void assertIncompletePylonFallback(Fixture fixture) {
        assertEquals(0, render(fixture).model().size());
        assertEquals(
                List.of(BoundedDiagnostics.Event.M3_COMPLETION_UNSUPPORTED_NEIGHBOR_DATA),
                fixture.diagnostics()
        );
        assertTrue(fixture.completion().isActive());
    }

    private static Ae2PaintBlockEntityData paintEntity() throws IOException {
        byte[] dots = new byte[PaintSnapshot.MAX_PERSISTED_BYTES];
        dots[0] = 2;
        dots[1] = 0x21;
        dots[2] = (byte) (Direction6.UP.ordinal() | CableColor.RED.ordinal() << 3);
        dots[3] = (byte) 0xf3;
        dots[4] = (byte) (Direction6.WEST.ordinal()
                | CableColor.CYAN.ordinal() << 3 | 0x80);
        return paintEntity(dots);
    }

    private static byte[] paintDots(
            Direction6 backingSide,
            CableColor color,
            boolean lumen
    ) {
        byte[] dots = new byte[PaintSnapshot.MAX_PERSISTED_BYTES];
        dots[0] = 1;
        dots[1] = 0x21;
        dots[2] = (byte) (backingSide.ordinal()
                | color.ordinal() << 3 | (lumen ? 0x80 : 0));
        return dots;
    }

    private static Ae2PaintBlockEntityData paintEntity(byte[] dots) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (NBTWriter writer = new NBTWriter(bytes)) {
            writer.beginCompound();
            writer.name("id").value("ae2:paint");
            writer.name("x").value(X);
            writer.name("y").value(Y);
            writer.name("z").value(Z);
            writer.name("dots").value(dots);
            writer.endCompound();
        }
        return MCAUtil.addCommonNbtSettings(new BlueNBT()).read(
                new ByteArrayInputStream(bytes.toByteArray()),
                Ae2PaintBlockEntityData.class
        );
    }

    private static Map<Position, LightLevels> darkLights() {
        Map<Position, LightLevels> lights = new HashMap<>();
        LightLevels dark = new LightLevels(0, 0);
        lights.put(CENTER, dark);
        for (Direction6 direction : Direction6.values()) {
            lights.put(CENTER.plus(
                    direction.stepX(), direction.stepY(), direction.stepZ()
            ), dark);
        }
        return Map.copyOf(lights);
    }

    private static void putStockResources(ResourcePack resourcePack) {
        putOccludingCube(resourcePack);
        putStockState(
                resourcePack,
                "ae2:sky_stone_chest",
                Ae219217M3CompletionProfile.SKY_STONE_FALLBACK_TEXTURE,
                6
        );
        putStockState(
                resourcePack,
                "ae2:smooth_sky_stone_chest",
                Ae219217M3CompletionProfile.SMOOTH_SKY_STONE_FALLBACK_TEXTURE,
                6
        );
        putStockState(resourcePack, "ae2:crank", Ae219217M3CompletionProfile.CRANK_TEXTURE, 16);
        putStockState(
                resourcePack,
                "ae2:inscriber",
                Ae219217M3CompletionProfile.INSCRIBER_TEXTURE,
                33
        );
        putStockState(resourcePack, "ae2:paint", Ae219217M3CompletionProfile.PAINT1_TEXTURE, 0);
        putStockState(
                resourcePack,
                "ae2:spatial_pylon",
                Ae219217M3CompletionProfile.PYLON_BASE_TEXTURE,
                0
        );
    }

    private static void putOccludingCube(ResourcePack resourcePack) {
        EnumMap<Direction, Face> faces = new EnumMap<>(Direction.class);
        for (Direction direction : Direction.values()) {
            faces.put(direction, new Face(
                    new Vector4f(0F, 0F, 16F, 16F),
                    new TextureVariable(new ResourcePath<Texture>(
                            Ae219217M3CompletionProfile.SKY_STONE_FALLBACK_TEXTURE
                    )),
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
        Key modelKey = Key.parse("test:block/occluding_stone");
        resourcePack.getModels().put(modelKey, model);
        Variant variant = new Variant(new ResourcePath<Model>(modelKey));
        resourcePack.getBlockStates().put(
                Key.parse("minecraft:stone"),
                new de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.BlockState(
                        new Variants(new VariantSet[0], new VariantSet(variant))
                )
        );
    }

    private static void putStockState(
            ResourcePack resourcePack,
            String block,
            String texture,
            int faceCount
    ) {
        List<Element> elements = new ArrayList<>();
        int remaining = faceCount;
        while (remaining > 0) {
            int count = Math.min(remaining, Direction.values().length);
            EnumMap<Direction, Face> faces = new EnumMap<>(Direction.class);
            for (int face = 0; face < count; face++) {
                Direction direction = Direction.values()[face];
                faces.put(direction, new Face(
                        new Vector4f(0F, 0F, 16F, 16F),
                        new TextureVariable(new ResourcePath<Texture>(texture)),
                        null,
                        0,
                        -1
                ));
            }
            elements.add(new Element(
                    new Vector3f(1F, 1F, 1F),
                    new Vector3f(15F, 15F, 15F),
                    faces
            ));
            remaining -= count;
        }
        Model model = new Model(elements.toArray(Element[]::new));
        Key modelKey = Key.parse("test:block/stock/" + block.substring(4));
        resourcePack.getModels().put(modelKey, model);
        Variant variant = new Variant(new ResourcePath<Model>(modelKey));
        resourcePack.getBlockStates().put(
                Key.parse(block),
                new de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.BlockState(
                        new Variants(new VariantSet[0], new VariantSet(variant))
                )
        );
    }

    private static TestBlockEntity entity(Key id, Position position) {
        return new TestBlockEntity(id, position.x(), position.y(), position.z());
    }

    private static final RenderSettings TEST_SETTINGS = settings(false, false);

    private static RenderSettings settings(boolean topOnly, boolean cave) {
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
                return false;
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
        private final Map<Position, LightLevels> lights;
        private int x;
        private int y;
        private int z;

        private TestBlockAccess(
                Map<Position, BlockState> states,
                Map<Position, BlockEntity> entities,
                Map<Position, LightLevels> lights
        ) {
            this.states = states;
            this.entities = entities;
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
            return new TestBlockAccess(states, entities, lights);
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
            LightLevels light = lights.getOrDefault(
                    new Position(x, y, z),
                    new LightLevels(7, 4)
            );
            return new LightData(light.sunlight(), light.blocklight());
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

        private List<FaceData> faces() {
            return faces;
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
        public RecordingTileModel transform(
                int start,
                int count,
                MatrixM4f transform
        ) {
            super.transform(start, count, transform);
            int end = start + count;
            for (int face = start; face < end; face++) {
                float[] positions = face(face).positions;
                if (positions == null) {
                    continue;
                }
                for (int vertex = 0; vertex < 3; vertex++) {
                    int offset = vertex * 3;
                    float x = positions[offset];
                    float y = positions[offset + 1];
                    float z = positions[offset + 2];
                    positions[offset] = transform.m00 * x + transform.m01 * y
                            + transform.m02 * z + transform.m03;
                    positions[offset + 1] = transform.m10 * x + transform.m11 * y
                            + transform.m12 * z + transform.m13;
                    positions[offset + 2] = transform.m20 * x + transform.m21 * y
                            + transform.m22 * z + transform.m23;
                }
            }
            return this;
        }

        @Override
        public RecordingTileModel setAOs(int face, float first, float second, float third) {
            super.setAOs(face, first, second, third);
            face(face).aos = new float[]{first, second, third};
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
        private float[] aos;
        private int sunlight;
        private int blocklight;
        private int material;

        private float[] positions() {
            return positions;
        }

        private float[] aos() {
            return aos;
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

    private record TestBlockEntity(Key id, int x, int y, int z) implements BlockEntity {
        @Override
        public Key getId() {
            return id;
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
        public boolean isKeepPacked() {
            return false;
        }
    }

    private record Fixture(
            ResourcePack resourcePack,
            TextureGallery gallery,
            BlockNeighborhood neighborhood,
            ProfileActivation profile,
            M3CompletionRouteActivation completion,
            List<BoundedDiagnostics.Event> diagnostics,
            M3CompletionRenderer renderer
    ) {
    }

    private record RenderResult(RecordingTileModel model, Color mapColor) {
    }

    private record LightLevels(int sunlight, int blocklight) {
    }

    private record Position(int x, int y, int z) {
        private Position plus(int dx, int dy, int dz) {
            return new Position(x + dx, y + dy, z + dz);
        }
    }
}
