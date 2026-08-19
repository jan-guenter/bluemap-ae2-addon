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
import de.bluecolored.bluemap.core.util.math.MatrixM4f;
import de.bluecolored.bluemap.core.world.BlockEntity;
import de.bluecolored.bluemap.core.world.BlockState;
import de.bluecolored.bluemap.core.world.DimensionType;
import de.bluecolored.bluemap.core.world.LightData;
import de.bluecolored.bluemap.core.world.biome.Biome;
import de.bluecolored.bluemap.core.world.block.BlockAccess;
import de.bluecolored.bluemap.core.world.block.BlockNeighborhood;
import io.github.janguenter.bluemap.ae2.activation.DriveRouteActivation;
import io.github.janguenter.bluemap.ae2.activation.ExtensionRouteActivation;
import io.github.janguenter.bluemap.ae2.activation.ProfileActivation;
import io.github.janguenter.bluemap.ae2.model.Direction6;
import io.github.janguenter.bluemap.ae2.model.DriveCellDefinition;
import io.github.janguenter.bluemap.ae2.model.DriveCellCatalog;
import io.github.janguenter.bluemap.ae2.model.DriveCellOwner;
import io.github.janguenter.bluemap.ae2.model.DriveCellRouteAccess;
import io.github.janguenter.bluemap.ae2.model.DriveInventoryProjection;
import io.github.janguenter.bluemap.ae2.model.PartOrientation;
import io.github.janguenter.bluemap.ae2.profile.Ae219217DriveProfile;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DriveRendererTest {

    private static final int X = 5;
    private static final int Y = 70;
    private static final int Z = -4;
    private static final Key ORIGINAL = Key.parse("test:block/drive_original");

    @Test
    void oneOccupiedCellAddsOneChassisAndExactlyFiveBlackFullbrightLedQuads()
            throws Exception {
        Fixture fixture = fixture("ae2:item_storage_cell_1k");
        RecordingTileModel model = render(fixture);

        assertEquals(106, model.size());
        for (int face = 0; face < 96; face++) {
            assertArrayEquals(new float[]{1F, 1F, 1F}, model.face(face).aos(), 0F);
        }
        int ledMaterial = fixture.gallery().get(M3DriveResourceModels.LED_TEXTURE);
        for (int face = 96; face < 106; face++) {
            assertEquals(ledMaterial, model.face(face).material());
            assertArrayEquals(new float[]{0F, 0F, 0F}, model.face(face).color(), 0F);
            assertArrayEquals(new float[]{1F, 1F, 1F}, model.face(face).aos(), 0F);
            assertEquals(15, model.face(face).sunlight());
            assertEquals(15, model.face(face).blocklight());
        }
        assertArrayEquals(new float[]{
                13F / 16F, 14F / 16F, 0.999F / 16F,
                14F / 16F, 14F / 16F, 0.999F / 16F,
                14F / 16F, 12.999F / 16F, 0.999F / 16F
        }, model.face(96).positions(), 0.000001F);
        assertTrue(fixture.profileActivation().isActive());
        assertTrue(fixture.driveActivation().isActive());
    }

    @Test
    void unknownCellUsesOnlyTheWholeOriginalResourceFallback() throws Exception {
        Fixture fixture = fixture("example:future_cell");

        RecordingTileModel model = render(fixture);

        assertEquals(12, model.size());
        int original = fixture.gallery().get(ORIGINAL);
        for (int face = 0; face < model.size(); face++) {
            assertEquals(original, model.face(face).material());
        }
        assertTrue(fixture.profileActivation().isActive());
        assertTrue(fixture.driveActivation().isActive());
    }

    @Test
    void appFluxAndMegaCellsRespectIndependentRoutesAndAtomicFallback()
            throws Exception {
        for (DriveCellOwner owner : List.of(
                DriveCellOwner.APPLIED_FLUX,
                DriveCellOwner.MEGA_CELLS
        )) {
            DriveCellDefinition definition =
                    ExtensionDriveRendererTestSupport.representative(owner);

            M45Runtime activeRuntime = activeRuntime(owner);
            Fixture active = extensionFixture(definition, activeRuntime, true);
            assertEquals(106, render(active).size(), definition.itemId());
            assertTrue(activeRuntime.active(routeId(owner)), definition.itemId());

            M45Runtime inactiveRuntime = new M45Runtime();
            Fixture inactive = extensionFixture(definition, inactiveRuntime, true);
            assertOriginalOnly(render(inactive), inactive, definition.itemId());
            assertFalse(inactiveRuntime.route(routeId(owner)).isDisabled(), definition.itemId());

            M45Runtime missingRuntime = activeRuntime(owner);
            Fixture missing = extensionFixture(definition, missingRuntime, false);
            assertOriginalOnly(render(missing), missing, definition.itemId());
            assertTrue(missingRuntime.route(routeId(owner)).isDisabled(), definition.itemId());
            assertTrue(missing.profileActivation().isActive());
            assertTrue(missing.driveActivation().isActive());
        }
    }

    @Test
    void allTenAppMekCellsFillTheTenNativeDriveSlots() throws Exception {
        List<DriveCellDefinition> definitions = DriveCellCatalog.extensionDefinitions(
                DriveCellOwner.APPLIED_MEKANISTICS
        );
        DriveInventoryProjection inventory = DriveInventoryProjection.empty();
        for (int slot = 0; slot < definitions.size(); slot++) {
            inventory = inventory.withSlot(
                    slot,
                    DriveInventoryProjection.Slot.occupied(
                            definitions.get(slot).itemId()
                    )
            );
        }
        M45Runtime runtime = activeRuntime(DriveCellOwner.APPLIED_MEKANISTICS);

        Fixture fixture = appMekFixture(
                inventory,
                "north",
                0,
                runtime,
                ExtensionDriveResourceModels::supported
        );

        assertEquals(250, render(fixture).size());
        assertTrue(runtime.active(M45Runtime.APPMEK_DRIVE_CELLS));
        assertTrue(fixture.driveActivation().isActive());
    }

    @Test
    void threeRepresentativeAppMekOrientationsTransformTheSameChassis()
            throws Exception {
        String itemId = "appmek:chemical_storage_cell_1k";
        M45Runtime referenceRuntime = activeRuntime(
                DriveCellOwner.APPLIED_MEKANISTICS
        );
        RecordingTileModel reference = render(appMekFixture(
                inventory(itemId),
                "north",
                0,
                referenceRuntime,
                ExtensionDriveResourceModels::supported
        ));
        float[] localChassisFace = reference.face(90).positions();

        for (OrientationCase orientation : List.of(
                new OrientationCase(Direction6.EAST, 0),
                new OrientationCase(Direction6.UP, 1),
                new OrientationCase(Direction6.SOUTH, 3)
        )) {
            M45Runtime runtime = activeRuntime(DriveCellOwner.APPLIED_MEKANISTICS);
            RecordingTileModel rendered = render(appMekFixture(
                    inventory(itemId),
                    orientation.facing().name().toLowerCase(java.util.Locale.ROOT),
                    orientation.spin(),
                    runtime,
                    ExtensionDriveResourceModels::supported
            ));
            PartOrientation angles = PartOrientation.forPart(
                    orientation.facing(),
                    orientation.spin()
            );
            MatrixM4f matrix = new Variant(
                    M3DriveResourceModels.DRIVE_BASE,
                    angles.x(),
                    angles.y(),
                    angles.z()
            ).getTransformMatrix();
            assertEquals(106, rendered.size(), orientation.toString());
            assertArrayEquals(
                    transform(matrix, localChassisFace),
                    rendered.face(90).positions(),
                    0.000001F,
                    orientation.toString()
            );
        }
    }

    @Test
    void appMekValidatorFalseFallsBackWithoutDisableButThrowDisablesOnlyAppMek()
            throws Exception {
        String itemId = "appmek:chemical_storage_cell_1k";

        M45Runtime mismatchRuntime = activeRuntime(
                DriveCellOwner.APPLIED_MEKANISTICS
        );
        Fixture mismatch = appMekFixture(
                inventory(itemId),
                "north",
                0,
                mismatchRuntime,
                (pack, id, model, owner) -> false
        );
        assertOriginalOnly(render(mismatch), mismatch, "semantic mismatch");
        assertTrue(mismatchRuntime.active(M45Runtime.APPMEK_DRIVE_CELLS));

        M45Runtime failureRuntime = activeRuntime(
                DriveCellOwner.APPLIED_MEKANISTICS
        );
        Fixture failure = appMekFixture(
                inventory(itemId),
                "north",
                0,
                failureRuntime,
                (pack, id, model, owner) -> {
                    throw new IllegalStateException("injected selected-model callback");
                }
        );
        assertOriginalOnly(render(failure), failure, "callback failure");
        assertTrue(failureRuntime.route(M45Runtime.APPMEK_DRIVE_CELLS).isDisabled());
        assertEquals(
                ExtensionRouteActivation.Reason.RENDER_CALLBACK_FAILED,
                failureRuntime.route(M45Runtime.APPMEK_DRIVE_CELLS).snapshot().reason()
        );
        assertFalse(failureRuntime.route(M45Runtime.APPFLUX).isDisabled());

        Fixture later = appMekFixture(
                inventory(itemId),
                "north",
                0,
                failureRuntime,
                ExtensionDriveResourceModels::supported
        );
        assertOriginalOnly(render(later), later, "later disabled AppMek host");

        ResourcePack nativeResources = M3DriveResourceModelsTest.exactResources();
        Fixture nativePeer = fixture(
                "ae2:item_storage_cell_1k",
                "north",
                0,
                TEST_RENDER_SETTINGS,
                15,
                0,
                Map.of(),
                nativeResources,
                new ExtensionDriveCellRouteAccess(failureRuntime)
        );
        assertEquals(106, render(nativePeer).size());
    }

    @Test
    void nativeDriveCallbackFailureImmediatelyBlocksDependentAppMekRoute()
            throws Exception {
        M45Runtime runtime = activeRuntime(DriveCellOwner.APPLIED_MEKANISTICS);
        Fixture fixture = appMekFixture(
                inventory("appmek:chemical_storage_cell_1k"),
                "north",
                0,
                runtime,
                ExtensionDriveResourceModels::supported
        );
        RecordingTileModel model = new RecordingTileModel();
        model.failWithRuntimeOnAddInvocation(2);

        fixture.renderer().render(
                fixture.neighborhood(),
                null,
                new TileModelView(model),
                new Color()
        );

        assertOriginalOnly(model, fixture, "native Drive callback failure");
        assertTrue(fixture.driveActivation().isDisabled());
        assertFalse(runtime.active(M45Runtime.APPMEK_DRIVE_CELLS));
        assertFalse(runtime.route(M45Runtime.APPMEK_DRIVE_CELLS).isDisabled());
        assertEquals(
                ExtensionRouteActivation.Reason.BLOCKED_BY_CORE,
                runtime.route(M45Runtime.APPMEK_DRIVE_CELLS).snapshot().reason()
        );
    }

    @Test
    void nonIdentityOrientationRotatesTheTranslatedChassisAndLedAsOneBlock()
            throws Exception {
        Fixture fixture = fixture("ae2:item_storage_cell_1k", "east", 0);

        RecordingTileModel model = render(fixture);

        assertEquals(106, model.size());
        MatrixM4f orientation = new Variant(
                M3DriveResourceModels.DRIVE_BASE,
                0,
                90,
                0
        ).getTransformMatrix();
        assertArrayEquals(transform(orientation, new float[]{
                9F / 16F, 13F / 16F, 1F / 16F,
                15F / 16F, 13F / 16F, 1F / 16F,
                15F / 16F, 13F / 16F, 3F / 16F
        }), model.face(90).positions(), 0.000001F);
        assertArrayEquals(transform(orientation, new float[]{
                13F / 16F, 14F / 16F, 0.999F / 16F,
                14F / 16F, 14F / 16F, 0.999F / 16F,
                14F / 16F, 12.999F / 16F, 0.999F / 16F
        }), model.face(96).positions(), 0.000001F);
    }

    @Test
    void darkRemovedCaveSuppressesResourceModelsAndAllHandEmittedLeds()
            throws Exception {
        Fixture fixture = fixture(
                "ae2:item_storage_cell_1k",
                "north",
                0,
                DARK_CAVE_RENDER_SETTINGS,
                0,
                0
        );

        RecordingTileModel model = render(fixture);

        assertEquals(0, model.size());
        assertTrue(fixture.profileActivation().isActive());
        assertTrue(fixture.driveActivation().isActive());
    }

    @Test
    void caveBoundaryRendersOnlyLedFaceWhoseRotatedNeighborIsSkylit()
            throws Exception {
        MatrixM4f orientation = new Variant(
                M3DriveResourceModels.DRIVE_BASE,
                0,
                90,
                0
        ).getTransformMatrix();
        Position northNeighbor = offset(
                new Position(X, Y, Z),
                orientation,
                Direction6.NORTH
        );
        Fixture fixture = fixture(
                "ae2:item_storage_cell_1k",
                "east",
                0,
                DARK_CAVE_RENDER_SETTINGS,
                0,
                0,
                Map.of(northNeighbor, new LightLevels(15, 0))
        );

        RecordingTileModel model = render(fixture);

        int emittedLedTriangles = 0;
        for (int face = 0; face < model.size(); face++) {
            FaceData data = model.face(face);
            if (data.blocklight() == 15
                    && data.color() != null
                    && data.color()[0] == 0F
                    && data.color()[1] == 0F
                    && data.color()[2] == 0F) {
                emittedLedTriangles++;
            }
        }
        assertEquals(2, emittedLedTriangles);
        assertTrue(model.size() > emittedLedTriangles);
    }

    @Test
    void rendererFailureDisablesOnlyDriveAndResetsPartialOutputBeforeFallback()
            throws Exception {
        Fixture fixture = fixture("ae2:item_storage_cell_1k");
        RecordingTileModel model = new RecordingTileModel();
        model.failWithRuntimeOnAddInvocation(2);

        fixture.renderer().render(
                fixture.neighborhood(),
                null,
                new TileModelView(model),
                new Color()
        );

        assertEquals(12, model.size());
        int original = fixture.gallery().get(ORIGINAL);
        for (int face = 0; face < model.size(); face++) {
            assertEquals(original, model.face(face).material());
        }
        assertTrue(fixture.profileActivation().isActive());
        assertTrue(fixture.driveActivation().isDisabled());
        assertEquals("drive-render-callback-failed", fixture.driveActivation().reason());
    }

    @Test
    void maxCapacityRemainsHostControlFlowAndDoesNotDisableEitherRoute()
            throws Exception {
        Fixture fixture = fixture("ae2:item_storage_cell_1k");
        RecordingTileModel model = new RecordingTileModel();
        model.failWithCapacityOnAddInvocation(1);

        assertThrows(
                MaxCapacityReachedException.class,
                () -> fixture.renderer().render(
                        fixture.neighborhood(),
                        null,
                        new TileModelView(model),
                        new Color()
                )
        );
        assertTrue(fixture.profileActivation().isActive());
        assertTrue(fixture.driveActivation().isActive());
    }

    @Test
    void topOnlyUsesTheOrientationRotatedLedNormal() {
        Variant identity = new Variant(M3DriveResourceModels.DRIVE_BASE);

        assertTrue(DriveRenderer.ledVisible(
                true,
                identity.getTransformMatrix(),
                Direction6.UP
        ));
        for (Direction6 face : List.of(
                Direction6.DOWN,
                Direction6.NORTH,
                Direction6.WEST,
                Direction6.EAST
        )) {
            assertFalse(DriveRenderer.ledVisible(
                    true,
                    identity.getTransformMatrix(),
                    face
            ));
        }
        for (Direction6 face : Direction6.values()) {
            assertTrue(DriveRenderer.ledVisible(
                    false,
                    identity.getTransformMatrix(),
                    face
            ));
        }

        Variant frontRotatedUp = new Variant(M3DriveResourceModels.DRIVE_BASE, 270, 0, 0);
        assertTrue(DriveRenderer.ledVisible(
                true,
                frontRotatedUp.getTransformMatrix(),
                Direction6.NORTH
        ));
    }

    private static Fixture fixture(String cellId) throws Exception {
        return fixture(cellId, "north", 0);
    }

    private static Fixture fixture(String cellId, String facing, int spin) throws Exception {
        return fixture(cellId, facing, spin, TEST_RENDER_SETTINGS, 15, 0);
    }

    private static Fixture fixture(
            String cellId,
            String facing,
            int spin,
            RenderSettings renderSettings,
            int skyLight,
            int blockLight
    ) throws Exception {
        return fixture(
                cellId,
                facing,
                spin,
                renderSettings,
                skyLight,
                blockLight,
                Map.of()
        );
    }

    private static Fixture fixture(
            String cellId,
            String facing,
            int spin,
            RenderSettings renderSettings,
            int skyLight,
            int blockLight,
            Map<Position, LightLevels> lightOverrides
    ) throws Exception {
        return fixture(
                cellId,
                facing,
                spin,
                renderSettings,
                skyLight,
                blockLight,
                lightOverrides,
                M3DriveResourceModelsTest.exactResources(),
                null
        );
    }

    private static Fixture fixture(
            String cellId,
            String facing,
            int spin,
            RenderSettings renderSettings,
            int skyLight,
            int blockLight,
            Map<Position, LightLevels> lightOverrides,
            ResourcePack resourcePack,
            DriveCellRouteAccess cellRoutes
    ) throws Exception {
        return fixtureInventory(
                inventory(cellId),
                facing,
                spin,
                renderSettings,
                skyLight,
                blockLight,
                lightOverrides,
                resourcePack,
                cellRoutes,
                ExtensionDriveResourceModels::supported
        );
    }

    private static Fixture fixtureInventory(
            DriveInventoryProjection inventory,
            String facing,
            int spin,
            RenderSettings renderSettings,
            int skyLight,
            int blockLight,
            Map<Position, LightLevels> lightOverrides,
            ResourcePack resourcePack,
            DriveCellRouteAccess cellRoutes,
            ExtensionDriveResourceValidator extensionResourceValidator
    ) throws Exception {
        putTexture(resourcePack, ORIGINAL, 0xFF336699);
        putOriginalCube(resourcePack);
        TextureGallery gallery = new TextureGallery();
        gallery.put(resourcePack.getTextures());

        Position center = new Position(X, Y, Z);
        Map<Position, BlockState> states = new HashMap<>();
        Map<Position, BlockEntity> blockEntities = new HashMap<>();
        states.put(center, exactState(facing, spin));
        blockEntities.put(center, drive(inventory));
        BlockNeighborhood neighborhood = new BlockNeighborhood(
                new TestBlockAccess(
                        states,
                        blockEntities,
                        skyLight,
                        blockLight,
                        lightOverrides
                ),
                resourcePack,
                renderSettings,
                DimensionType.OVERWORLD
        );
        neighborhood.set(X, Y, Z);

        ProfileActivation profileActivation = new ProfileActivation();
        DriveRouteActivation driveActivation = new DriveRouteActivation();
        profileActivation.activate();
        driveActivation.activate();
        DriveRenderer renderer = cellRoutes == null
                ? new DriveRenderer(
                        resourcePack,
                        gallery,
                        renderSettings,
                        profileActivation,
                        driveActivation
                )
                : new DriveRenderer(
                        resourcePack,
                        gallery,
                        renderSettings,
                        profileActivation,
                        driveActivation,
                        M3DriveResourceModels::resourcesSupported,
                        cellRoutes,
                        extensionResourceValidator
                );
        return new Fixture(
                gallery,
                neighborhood,
                profileActivation,
                driveActivation,
                renderer
        );
    }

    private static Fixture extensionFixture(
            DriveCellDefinition definition,
            M45Runtime runtime,
            boolean includeModel
    ) throws Exception {
        ResourcePack resourcePack = M3DriveResourceModelsTest.exactResources();
        if (includeModel) {
            ExtensionDriveRendererTestSupport.putExactShapeModel(
                    resourcePack,
                    definition
            );
            assertTrue(
                    ExtensionDriveResourceModels.supported(resourcePack, definition),
                    definition.itemId()
            );
        }
        assertTrue(M3DriveResourceModels.resourcesSupported(resourcePack));
        return fixture(
                definition.itemId(),
                "north",
                0,
                TEST_RENDER_SETTINGS,
                15,
                0,
                Map.of(),
                resourcePack,
                new ExtensionDriveCellRouteAccess(runtime)
        );
    }

    private static Fixture appMekFixture(
            DriveInventoryProjection inventory,
            String facing,
            int spin,
            M45Runtime runtime,
            ExtensionDriveResourceValidator extensionResourceValidator
    ) throws Exception {
        ResourcePack resourcePack = AppMekExternalResourceTestSupport.exactResources();
        assertTrue(M3DriveResourceModels.resourcesSupported(resourcePack));
        return fixtureInventory(
                inventory,
                facing,
                spin,
                TEST_RENDER_SETTINGS,
                15,
                0,
                Map.of(),
                resourcePack,
                new ExtensionDriveCellRouteAccess(runtime),
                extensionResourceValidator
        );
    }

    private static M45Runtime activeRuntime(DriveCellOwner owner) {
        M45Runtime runtime = new M45Runtime();
        runtime.route(routeId(owner)).activate("exact-profile");
        return runtime;
    }

    private static String routeId(DriveCellOwner owner) {
        return switch (owner) {
            case APPLIED_FLUX -> M45Runtime.APPFLUX;
            case MEGA_CELLS -> M45Runtime.MEGA_CELLS;
            case APPLIED_MEKANISTICS -> M45Runtime.APPMEK_DRIVE_CELLS;
            case AE2, EXTENDED_AE -> throw new IllegalArgumentException(
                    "owner has no extension route"
            );
        };
    }

    private static void assertOriginalOnly(
            RecordingTileModel model,
            Fixture fixture,
            String message
    ) {
        assertEquals(12, model.size(), message);
        int original = fixture.gallery().get(ORIGINAL);
        for (int face = 0; face < model.size(); face++) {
            assertEquals(original, model.face(face).material(), message);
        }
    }

    private static RecordingTileModel render(Fixture fixture) {
        RecordingTileModel model = new RecordingTileModel();
        fixture.renderer().render(
                fixture.neighborhood(),
                null,
                new TileModelView(model),
                new Color()
        );
        return model;
    }

    private static Ae2DriveBlockEntityData drive(String cellId)
            throws ReflectiveOperationException {
        return drive(inventory(cellId));
    }

    private static DriveInventoryProjection inventory(String cellId) {
        return DriveInventoryProjection.empty().withSlot(
                0,
                DriveInventoryProjection.Slot.occupied(cellId)
        );
    }

    private static Ae2DriveBlockEntityData drive(DriveInventoryProjection inventory)
            throws ReflectiveOperationException {
        Ae2DriveBlockEntityData data = new Ae2DriveBlockEntityData();
        Field field = Ae2DriveBlockEntityData.class.getDeclaredField("inv");
        field.setAccessible(true);
        field.set(data, inventory);
        return data;
    }

    private static BlockState exactState() {
        return exactState("north", 0);
    }

    private static BlockState exactState(String facing, int spin) {
        return BlockState.fromString(
                "ae2:drive[facing=" + facing + ",spin=" + spin + "]"
        );
    }

    private static float[] transform(MatrixM4f matrix, float[] positions) {
        float[] transformed = positions.clone();
        for (int vertex = 0; vertex < transformed.length; vertex += 3) {
            float x = positions[vertex];
            float y = positions[vertex + 1];
            float z = positions[vertex + 2];
            transformed[vertex] = matrix.m00 * x + matrix.m01 * y
                    + matrix.m02 * z + matrix.m03;
            transformed[vertex + 1] = matrix.m10 * x + matrix.m11 * y
                    + matrix.m12 * z + matrix.m13;
            transformed[vertex + 2] = matrix.m20 * x + matrix.m21 * y
                    + matrix.m22 * z + matrix.m23;
        }
        return transformed;
    }

    private static Position offset(
            Position center,
            MatrixM4f orientation,
            Direction6 direction
    ) {
        int dx = Math.round(orientation.m00 * direction.stepX()
                + orientation.m01 * direction.stepY()
                + orientation.m02 * direction.stepZ());
        int dy = Math.round(orientation.m10 * direction.stepX()
                + orientation.m11 * direction.stepY()
                + orientation.m12 * direction.stepZ());
        int dz = Math.round(orientation.m20 * direction.stepX()
                + orientation.m21 * direction.stepY()
                + orientation.m22 * direction.stepZ());
        return new Position(center.x() + dx, center.y() + dy, center.z() + dz);
    }

    private static void putOriginalCube(ResourcePack resourcePack) {
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
        Key modelKey = Key.parse("test:block/drive_original_model");
        resourcePack.getModels().put(modelKey, model);
        Variant variant = new Variant(new ResourcePath<Model>(modelKey));
        resourcePack.getBlockStates().put(
                Key.parse(Ae219217DriveProfile.DRIVE_BLOCK),
                new de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.BlockState(
                        new Variants(new VariantSet[0], new VariantSet(variant))
                )
        );
    }

    private static void putTexture(ResourcePack pack, Key key, int argb) throws IOException {
        BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        image.setRGB(0, 0, argb);
        pack.getTextures().put(key, Texture.from(key, image));
    }

    private static final RenderSettings TEST_RENDER_SETTINGS = new RenderSettings() {
        @Override
        public int getRemoveCavesBelowY() {
            return Integer.MIN_VALUE;
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
            return false;
        }
    };

    private static final RenderSettings DARK_CAVE_RENDER_SETTINGS = new RenderSettings() {
        @Override
        public int getRemoveCavesBelowY() {
            return Y + 1;
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
            return false;
        }
    };

    private static final class TestBlockAccess implements BlockAccess {
        private final Map<Position, BlockState> states;
        private final Map<Position, BlockEntity> blockEntities;
        private final int skyLight;
        private final int blockLight;
        private final Map<Position, LightLevels> lightOverrides;
        private int x;
        private int y;
        private int z;

        private TestBlockAccess(
                Map<Position, BlockState> states,
                Map<Position, BlockEntity> blockEntities,
                int skyLight,
                int blockLight,
                Map<Position, LightLevels> lightOverrides
        ) {
            this.states = states;
            this.blockEntities = blockEntities;
            this.skyLight = skyLight;
            this.blockLight = blockLight;
            this.lightOverrides = lightOverrides;
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
                    blockEntities,
                    skyLight,
                    blockLight,
                    lightOverrides
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
            LightLevels override = lightOverrides.get(new Position(x, y, z));
            return override == null
                    ? new LightData(skyLight, blockLight)
                    : new LightData(override.skyLight(), override.blockLight());
        }

        @Override
        public Biome getBiome() {
            return Biome.DEFAULT;
        }

        @Override
        public BlockEntity getBlockEntity() {
            return blockEntities.get(new Position(x, y, z));
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
        private int addInvocation;
        private int runtimeFailureInvocation = -1;
        private int capacityFailureInvocation = -1;

        private RecordingTileModel() {
            super(128);
        }

        void failWithRuntimeOnAddInvocation(int invocation) {
            addInvocation = 0;
            runtimeFailureInvocation = invocation;
            capacityFailureInvocation = -1;
        }

        void failWithCapacityOnAddInvocation(int invocation) {
            addInvocation = 0;
            runtimeFailureInvocation = -1;
            capacityFailureInvocation = invocation;
        }

        FaceData face(int face) {
            return faces.get(face);
        }

        @Override
        public int add(int count) {
            addInvocation++;
            if (addInvocation == capacityFailureInvocation) {
                capacityFailureInvocation = -1;
                throw new MaxCapacityReachedException("injected capacity");
            }
            if (addInvocation == runtimeFailureInvocation) {
                runtimeFailureInvocation = -1;
                throw new IllegalStateException("injected emission failure");
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
            super.setPositions(face, x1, y1, z1, x2, y2, z2, x3, y3, z3);
            face(face).positions = new float[]{x1, y1, z1, x2, y2, z2, x3, y3, z3};
            return this;
        }

        @Override
        public RecordingTileModel translate(
                int start,
                int count,
                float deltaX,
                float deltaY,
                float deltaZ
        ) {
            super.translate(start, count, deltaX, deltaY, deltaZ);
            int end = start + count;
            for (int face = start; face < end; face++) {
                float[] positions = face(face).positions;
                for (int vertex = 0; vertex < positions.length; vertex += 3) {
                    positions[vertex] += deltaX;
                    positions[vertex + 1] += deltaY;
                    positions[vertex + 2] += deltaZ;
                }
            }
            return this;
        }

        @Override
        public RecordingTileModel transform(int start, int count, MatrixM4f matrix) {
            super.transform(start, count, matrix);
            int end = start + count;
            for (int face = start; face < end; face++) {
                face(face).positions = DriveRendererTest.transform(
                        matrix,
                        face(face).positions
                );
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
        private float[] aos;
        private float[] color;
        private int sunlight;
        private int blocklight;
        private int material;

        float[] positions() {
            return positions;
        }

        float[] aos() {
            return aos;
        }

        float[] color() {
            return color;
        }

        int sunlight() {
            return sunlight;
        }

        int blocklight() {
            return blocklight;
        }

        int material() {
            return material;
        }
    }

    private record Fixture(
            TextureGallery gallery,
            BlockNeighborhood neighborhood,
            ProfileActivation profileActivation,
            DriveRouteActivation driveActivation,
            DriveRenderer renderer
    ) {
    }

    private record Position(int x, int y, int z) {
    }

    private record LightLevels(int skyLight, int blockLight) {
    }

    private record OrientationCase(Direction6 facing, int spin) {
    }
}
