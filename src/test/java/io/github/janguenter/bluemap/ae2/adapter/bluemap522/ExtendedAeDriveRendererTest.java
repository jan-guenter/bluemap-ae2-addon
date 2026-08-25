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
import io.github.janguenter.bluemap.ae2.activation.ExtendedAeDriveRouteActivation;
import io.github.janguenter.bluemap.ae2.activation.ProfileActivation;
import io.github.janguenter.bluemap.ae2.model.Direction6;
import io.github.janguenter.bluemap.ae2.model.DriveCellDefinition;
import io.github.janguenter.bluemap.ae2.model.DriveCellOwner;
import io.github.janguenter.bluemap.ae2.model.DriveCellRouteAccess;
import io.github.janguenter.bluemap.ae2.model.ExtendedAeDriveBayLayout;
import io.github.janguenter.bluemap.ae2.model.ExtendedAeDriveInventoryProjection;
import io.github.janguenter.bluemap.ae2.model.PartOrientation;
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

class ExtendedAeDriveRendererTest {

    private static final int X = -6;
    private static final int Y = 81;
    private static final int Z = 12;
    private static final Key ORIGINAL = Key.parse("test:block/extended_drive_original");

    @Test
    void rendersExactBasePlusSixteenTrianglesPerOccupiedSlot() throws Exception {
        Map<Integer, String> allCells = new HashMap<>();
        for (int slot = 0; slot < ExtendedAeDriveInventoryProjection.SLOT_COUNT; slot++) {
            allCells.put(slot, "extendedae:void_cell");
        }
        Fixture fixture = fixture(allCells, "north", 0, settings(false, false), 15, Map.of());

        RecordingTileModel model = render(fixture);

        assertEquals(116 + 16 * 20, model.size());
        for (int face = 0; face < model.size(); face++) {
            assertArrayEquals(new float[]{1F, 1F, 1F}, model.face(face).aos(), 0F);
        }
        assertEquals(20 * 10, blackFullbrightTriangles(model));
        assertTrue(fixture.profileActivation().isActive());
        assertTrue(fixture.extendedActivation().isActive());
    }

    @Test
    void rearSlotUsesOppositeFacingSameSpinAndLocalSlotTranslation() throws Exception {
        Fixture fixture = fixture(
                Map.of(10, "extendedae:infinity_water_cell"),
                "east",
                1,
                settings(false, false),
                15,
                Map.of()
        );

        RecordingTileModel model = render(fixture);

        assertEquals(132, model.size());
        ExtendedAeDriveBayLayout.Bay rear = ExtendedAeDriveBayLayout.bay(
                10,
                Direction6.EAST,
                1
        );
        assertEquals(PartOrientation.forPart(Direction6.WEST, 1), rear.orientation());
        MatrixM4f orientation = new Variant(
                M3bExtendedAeDriveResourceModels.DRIVE_BASE,
                rear.orientation().x(),
                rear.orientation().y(),
                rear.orientation().z()
        ).getTransformMatrix();
        assertArrayEquals(transform(orientation, new float[]{
                9F / 16F, 13F / 16F, 1F / 16F,
                15F / 16F, 13F / 16F, 1F / 16F,
                15F / 16F, 13F / 16F, 3F / 16F
        }), model.face(116).positions(), 0.000001F);
        assertArrayEquals(transform(orientation, new float[]{
                13F / 16F, 14F / 16F, 0.999F / 16F,
                14F / 16F, 14F / 16F, 0.999F / 16F,
                14F / 16F, 12.999F / 16F, 0.999F / 16F
        }), model.face(122).positions(), 0.000001F);
    }

    @Test
    void unknownPackCellUsesOnlyWholeOriginalResourceFallback() throws Exception {
        Fixture fixture = fixture(
                Map.of(12, "kubejs:lava_cell"),
                "north",
                0,
                settings(false, false),
                15,
                Map.of()
        );

        RecordingTileModel model = render(fixture);

        assertEquals(12, model.size());
        int original = fixture.gallery().get(ORIGINAL);
        for (int face = 0; face < model.size(); face++) {
            assertEquals(original, model.face(face).material());
        }
        assertTrue(fixture.profileActivation().isActive());
        assertTrue(fixture.nativeActivation().isActive());
        assertTrue(fixture.extendedActivation().isActive());
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
            assertEquals(132, render(active).size(), definition.itemId());
            assertTrue(activeRuntime.active(routeId(owner)), definition.itemId());

            M45Runtime inactiveRuntime = new M45Runtime();
            Fixture inactive = extensionFixture(definition, inactiveRuntime, true);
            assertOriginalOnly(render(inactive), inactive, definition.itemId());
            assertFalse(
                    inactiveRuntime.route(routeId(owner)).isDisabled(),
                    definition.itemId()
            );

            M45Runtime missingRuntime = activeRuntime(owner);
            Fixture missing = extensionFixture(definition, missingRuntime, false);
            assertOriginalOnly(render(missing), missing, definition.itemId());
            assertTrue(missingRuntime.route(routeId(owner)).isDisabled(), definition.itemId());
            assertTrue(missing.profileActivation().isActive());
            assertTrue(missing.nativeActivation().isActive());
            assertTrue(missing.extendedActivation().isActive());
        }
    }

    @Test
    void runtimeFailureDisablesOnlyExtendedRouteAndResetsPartialOutput()
            throws Exception {
        Fixture fixture = fixture(
                Map.of(0, "extendedae:void_cell"),
                "north",
                0,
                settings(false, false),
                15,
                Map.of()
        );
        RecordingTileModel model = new RecordingTileModel();
        model.failWithRuntimeOnAddInvocation(2);

        fixture.renderer().render(
                fixture.neighborhood(),
                null,
                new TileModelView(model),
                new Color()
        );

        assertEquals(12, model.size());
        assertTrue(fixture.profileActivation().isActive());
        assertTrue(fixture.nativeActivation().isActive());
        assertTrue(fixture.extendedActivation().isDisabled());
        assertEquals(
                "extended-drive-render-callback-failed",
                fixture.extendedActivation().reason()
        );
    }

    @Test
    void maxCapacityRemainsHostControlFlowWithoutDisablingAnyRoute() throws Exception {
        Fixture fixture = fixture(
                Map.of(19, "extendedae:void_cell"),
                "south",
                2,
                settings(false, false),
                15,
                Map.of()
        );
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
        assertTrue(fixture.nativeActivation().isActive());
        assertTrue(fixture.extendedActivation().isActive());
    }

    @Test
    void darkRemovedCaveSuppressesBothFacesAndAllHandEmittedLeds()
            throws Exception {
        Fixture fixture = fixture(
                Map.of(
                        0, "extendedae:void_cell",
                        10, "extendedae:infinity_cobblestone_cell"
                ),
                "north",
                0,
                settings(false, true),
                0,
                Map.of()
        );

        RecordingTileModel model = render(fixture);

        assertEquals(0, model.size());
        assertTrue(fixture.extendedActivation().isActive());
    }

    @Test
    void rearCaveBoundaryUsesTheOppositeFacingNeighborForLedVisibility()
            throws Exception {
        Position southNeighbor = new Position(X, Y, Z + 1);
        Fixture fixture = fixture(
                Map.of(10, "extendedae:void_cell"),
                "north",
                0,
                settings(false, true),
                0,
                Map.of(southNeighbor, new LightLevels(15, 0))
        );

        RecordingTileModel model = render(fixture);

        assertEquals(2, blackFullbrightTriangles(model));
        assertTrue(model.size() > 2);
    }

    @Test
    void topOnlyFiltersLedsByEachSidesRotatedNormal() throws Exception {
        Fixture fixture = fixture(
                Map.of(
                        0, "extendedae:void_cell",
                        10, "extendedae:void_cell"
                ),
                "north",
                0,
                settings(true, false),
                15,
                Map.of()
        );

        RecordingTileModel model = render(fixture);

        assertEquals(4, blackFullbrightTriangles(model));
    }

    @Test
    void exactStateRequiresOnlyThePinnedIdFacingAndSpinProperties() {
        assertTrue(ExtendedAeDriveRenderer.isExactState(exactState("up", 3)));
        assertFalse(ExtendedAeDriveRenderer.isExactState(
                BlockState.fromString("extendedae:ex_drive[facing=north]")
        ));
        assertFalse(ExtendedAeDriveRenderer.isExactState(
                BlockState.fromString(
                        "extendedae:ex_drive[facing=north,powered=true,spin=0]"
                )
        ));
        assertFalse(ExtendedAeDriveRenderer.isExactState(
                BlockState.fromString("ae2:drive[facing=north,spin=0]")
        ));
    }

    private static Fixture fixture(
            Map<Integer, String> cells,
            String facing,
            int spin,
            RenderSettings renderSettings,
            int skyLight,
            Map<Position, LightLevels> lightOverrides
    ) throws Exception {
        return fixture(
                cells,
                facing,
                spin,
                renderSettings,
                skyLight,
                lightOverrides,
                M3bExtendedAeDriveResourceModelsTest.exactResources(),
                null
        );
    }

    private static Fixture fixture(
            Map<Integer, String> cells,
            String facing,
            int spin,
            RenderSettings renderSettings,
            int skyLight,
            Map<Position, LightLevels> lightOverrides,
            ResourcePack resourcePack,
            DriveCellRouteAccess cellRoutes
    ) throws Exception {
        putTexture(resourcePack, ORIGINAL, 0xFF336699);
        putOriginalCube(resourcePack);
        TextureGallery gallery = new TextureGallery();
        gallery.put(resourcePack.getTextures());

        Position center = new Position(X, Y, Z);
        Map<Position, BlockState> states = new HashMap<>();
        Map<Position, BlockEntity> blockEntities = new HashMap<>();
        states.put(center, exactState(facing, spin));
        blockEntities.put(center, drive(cells));
        BlockNeighborhood neighborhood = new BlockNeighborhood(
                new TestBlockAccess(
                        states,
                        blockEntities,
                        skyLight,
                        0,
                        lightOverrides
                ),
                resourcePack,
                renderSettings,
                DimensionType.OVERWORLD
        );
        neighborhood.set(X, Y, Z);

        ProfileActivation profileActivation = new ProfileActivation();
        DriveRouteActivation nativeActivation = new DriveRouteActivation();
        ExtendedAeDriveRouteActivation extendedActivation =
                new ExtendedAeDriveRouteActivation();
        profileActivation.activate();
        nativeActivation.activate();
        extendedActivation.activate();
        ExtendedAeDriveRenderer renderer = cellRoutes == null
                ? new ExtendedAeDriveRenderer(
                        resourcePack,
                        gallery,
                        renderSettings,
                        profileActivation,
                        extendedActivation
                )
                : new ExtendedAeDriveRenderer(
                        resourcePack,
                        gallery,
                        renderSettings,
                        profileActivation,
                        extendedActivation,
                        M3bExtendedAeDriveResourceModels::resourcesSupported,
                        cellRoutes
                );
        return new Fixture(
                gallery,
                neighborhood,
                profileActivation,
                nativeActivation,
                extendedActivation,
                renderer
        );
    }

    private static Fixture extensionFixture(
            DriveCellDefinition definition,
            M45Runtime runtime,
            boolean includeModel
    ) throws Exception {
        ResourcePack resourcePack = M3bExtendedAeDriveResourceModelsTest.exactResources();
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
        assertTrue(M3bExtendedAeDriveResourceModels.resourcesSupported(resourcePack));
        return fixture(
                Map.of(0, definition.itemId()),
                "north",
                0,
                settings(false, false),
                15,
                Map.of(),
                resourcePack,
                new ExtensionDriveCellRouteAccess(runtime)
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
            case AE2, EXTENDED_AE, APPLIED_MEKANISTICS, EXTERNAL ->
                    throw new IllegalArgumentException(
                            "owner is unsupported by the Extended Drive route"
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

    private static ExtendedAeDriveBlockEntityData drive(Map<Integer, String> cells)
            throws ReflectiveOperationException {
        ExtendedAeDriveInventoryProjection inventory =
                ExtendedAeDriveInventoryProjection.empty();
        for (Map.Entry<Integer, String> cell : cells.entrySet()) {
            inventory = inventory.withSlot(
                    cell.getKey(),
                    ExtendedAeDriveInventoryProjection.Slot.occupied(cell.getValue())
            );
        }
        ExtendedAeDriveBlockEntityData data = new ExtendedAeDriveBlockEntityData();
        Field field = ExtendedAeDriveBlockEntityData.class.getDeclaredField("inv");
        field.setAccessible(true);
        field.set(data, inventory);
        return data;
    }

    private static BlockState exactState(String facing, int spin) {
        return BlockState.fromString(
                "extendedae:ex_drive[facing=" + facing + ",spin=" + spin + "]"
        );
    }

    private static int blackFullbrightTriangles(RecordingTileModel model) {
        int count = 0;
        for (int face = 0; face < model.size(); face++) {
            FaceData data = model.face(face);
            if (data.blocklight() == 15
                    && data.sunlight() == 15
                    && data.color() != null
                    && data.color()[0] == 0F
                    && data.color()[1] == 0F
                    && data.color()[2] == 0F) {
                count++;
            }
        }
        return count;
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

    private static void putOriginalCube(ResourcePack resourcePack) {
        EnumMap<Direction, Face> faces = new EnumMap<>(Direction.class);
        for (Direction direction : Direction.values()) {
            faces.put(direction, new Face(
                    new Vector4f(0, 0, 16, 16),
                    new TextureVariable(new ResourcePath<Texture>(ORIGINAL)),
                    direction,
                    0,
                    -1
            ));
        }
        Model model = new Model(new Element(
                Vector3f.ZERO,
                new Vector3f(16, 16, 16),
                faces
        ));
        Key modelKey = Key.parse("test:block/extended_drive_original_model");
        resourcePack.getModels().put(modelKey, model);
        Variant variant = new Variant(new ResourcePath<Model>(modelKey));
        resourcePack.getBlockStates().put(
                Key.parse("extendedae:ex_drive"),
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

    private static RenderSettings settings(boolean topOnly, boolean removeCaves) {
        return new RenderSettings() {
            @Override
            public int getRemoveCavesBelowY() {
                return removeCaves ? Y + 1 : Integer.MIN_VALUE;
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
                return 0;
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
            super(512);
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
            face(face).positions = new float[]{
                    x1, y1, z1,
                    x2, y2, z2,
                    x3, y3, z3
            };
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
                face(face).positions = ExtendedAeDriveRendererTest.transform(
                        matrix,
                        face(face).positions
                );
            }
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
            DriveRouteActivation nativeActivation,
            ExtendedAeDriveRouteActivation extendedActivation,
            ExtendedAeDriveRenderer renderer
    ) {
    }

    private record Position(int x, int y, int z) {
    }

    private record LightLevels(int skyLight, int blockLight) {
    }
}
