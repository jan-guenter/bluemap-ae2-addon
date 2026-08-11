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
import io.github.janguenter.bluemap.ae2.activation.ProfileActivation;
import io.github.janguenter.bluemap.ae2.activation.QuantumBridgeRouteActivation;
import io.github.janguenter.bluemap.ae2.diagnostics.BoundedDiagnostics;
import io.github.janguenter.bluemap.ae2.model.Direction6;
import io.github.janguenter.bluemap.ae2.model.QuantumBridgeGeometry;
import io.github.janguenter.bluemap.ae2.model.QuantumBridgeSnapshot;
import org.junit.jupiter.api.Test;

import java.io.IOException;
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

class QuantumBridgeRendererTest {

    private static final int X = 11;
    private static final int Y = 82;
    private static final int Z = -7;
    private static final Position CENTER = new Position(X, Y, Z);
    private static final Key ORIGINAL = Key.parse("test:block/quantum_original");
    private static final Key OPAQUE = Key.parse("test:opaque");
    private static final Key WRONG_ENTITY = Key.parse("test:wrong_entity");

    @Test
    void linkCornerAndEdgeUseExactCountsOneAllocationAndFourOffMaterials()
            throws Exception {
        Map<QuantumBridgeSnapshot.Role, Integer> expected = Map.of(
                QuantumBridgeSnapshot.Role.LINK, 108,
                QuantumBridgeSnapshot.Role.CORNER_RING, 36,
                QuantumBridgeSnapshot.Role.EDGE_RING, 36
        );
        for (QuantumBridgeSnapshot.Role role : QuantumBridgeSnapshot.Role.values()) {
            Fixture fixture = fixture(new BridgeData(role, false));
            RecordingTileModel model = render(fixture).model();

            assertEquals(expected.get(role), model.size(), role.name());
            assertEquals(1, model.addInvocations(), role.name());
            for (int triangle = 0; triangle < model.size(); triangle++) {
                FaceData face = model.face(triangle);
                assertArrayEquals(new float[]{1F, 1F, 1F}, face.color(), 0F);
                assertArrayEquals(new float[]{1F, 1F, 1F}, face.aos(), 0F);
                assertEquals(7, face.sunlight());
                assertEquals(4, face.blocklight());
                assertFalse(isLightMaterial(fixture, face.material()));
            }
        }
    }

    @Test
    void exactWaterloggedTrueStateUsesTheSameGeometryWithoutManualFluid()
            throws Exception {
        Fixture dry = fixture(new BridgeData(QuantumBridgeSnapshot.Role.LINK, false));
        Fixture wet = fixture(new BridgeData(QuantumBridgeSnapshot.Role.LINK, true));

        RecordingTileModel dryModel = render(dry).model();
        RecordingTileModel wetModel = render(wet).model();

        assertEquals(108, wetModel.size());
        assertSamePrimaryMesh(dryModel, wetModel);
        assertTrue(wet.quantumActivation().isActive());
    }

    @Test
    void completePlaneRequiresEveryExactStateAndGenericBlockEntity()
            throws Exception {
        BridgeData missing = new BridgeData(QuantumBridgeSnapshot.Role.LINK, false);
        Position member = missing.link().plus(1, 1, 0);
        missing.states().put(member, BlockState.MISSING);
        assertInvalidTopology(missing, BoundedDiagnostics.Event
                .QUANTUM_BRIDGE_UNSUPPORTED_NEIGHBOR_DATA);

        BridgeData malformed = new BridgeData(QuantumBridgeSnapshot.Role.LINK, false);
        malformed.states().put(
                malformed.link().plus(1, 1, 0),
                BlockState.fromString(
                        M3eQuantumBridgeResourceModels.RING_BLOCK
                                + "[formed=true,future=false,waterlogged=false]"
                )
        );
        assertInvalidTopology(
                malformed,
                BoundedDiagnostics.Event.QUANTUM_BRIDGE_INVALID_TOPOLOGY
        );

        BridgeData missingEntity = new BridgeData(
                QuantumBridgeSnapshot.Role.LINK,
                false
        );
        missingEntity.entities().remove(missingEntity.link().plus(-1, -1, 0));
        assertInvalidTopology(
                missingEntity,
                BoundedDiagnostics.Event.QUANTUM_BRIDGE_INVALID_TOPOLOGY
        );

        BridgeData wrongEntity = new BridgeData(QuantumBridgeSnapshot.Role.LINK, false);
        Position wrong = wrongEntity.link().plus(-1, 1, 0);
        wrongEntity.entities().put(wrong, entity(WRONG_ENTITY, wrong));
        assertInvalidTopology(
                wrongEntity,
                BoundedDiagnostics.Event.QUANTUM_BRIDGE_INVALID_TOPOLOGY
        );
    }

    @Test
    void exteriorSlabsRejectCrossedOrAdjacentSecondNativePlaneAtomically()
            throws Exception {
        BridgeData crossed = new BridgeData(QuantumBridgeSnapshot.Role.LINK, false);
        Position extra = crossed.link().plus(0, 0, 1);
        crossed.states().put(extra, exactState(false, false));
        crossed.entities().put(extra, entity(
                Key.parse(M3eQuantumBridgeResourceModels.BLOCK_ENTITY_ID),
                extra
        ));

        assertInvalidTopology(
                crossed,
                BoundedDiagnostics.Event.QUANTUM_BRIDGE_INVALID_TOPOLOGY
        );

        BridgeData adjacent = new BridgeData(QuantumBridgeSnapshot.Role.LINK, false);
        Position offEdge = adjacent.link().plus(2, 0, 0);
        adjacent.states().put(offEdge, exactState(true, false));
        adjacent.entities().put(offEdge, entity(
                Key.parse(M3eQuantumBridgeResourceModels.BLOCK_ENTITY_ID),
                offEdge
        ));

        assertInvalidTopology(
                adjacent,
                BoundedDiagnostics.Event.QUANTUM_BRIDGE_INVALID_TOPOLOGY
        );
    }

    @Test
    void knownNonnativeExteriorIsDisconnectedAndGeneralQuadsAreNotCulled()
            throws Exception {
        BridgeData data = new BridgeData(QuantumBridgeSnapshot.Role.EDGE_RING, false);
        data.states().put(CENTER.plus(0, 0, 1), BlockState.fromString(
                OPAQUE.getFormatted()
        ));
        Fixture fixture = fixture(data);
        RecordingTileModel model = render(fixture).model();

        assertEquals(36, model.size());
        assertTrue(model.faces().stream().anyMatch(face -> hasDarkenedAo(face.aos())));
        assertTrue(fixture.quantumActivation().isActive());
        assertTrue(fixture.diagnostics().isEmpty());
    }

    @Test
    void unformedAndMalformedCenterStatesDelegateWithoutDisablingRoute()
            throws Exception {
        BridgeData unformed = new BridgeData(QuantumBridgeSnapshot.Role.LINK, false);
        unformed.states().put(
                CENTER,
                BlockState.fromString(
                        M3eQuantumBridgeResourceModels.LINK_BLOCK
                                + "[formed=false,waterlogged=false]"
                )
        );
        Fixture ordinary = fixture(unformed);
        assertOriginalOnly(ordinary, render(ordinary).model());
        assertTrue(ordinary.diagnostics().isEmpty());

        BridgeData malformed = new BridgeData(QuantumBridgeSnapshot.Role.LINK, false);
        malformed.states().put(
                CENTER,
                BlockState.fromString(
                        M3eQuantumBridgeResourceModels.LINK_BLOCK
                                + "[formed=true,waterlogged=false,future=false]"
                )
        );
        Fixture rejected = fixture(malformed);
        assertOriginalOnly(rejected, render(rejected).model());
        assertEquals(
                List.of(BoundedDiagnostics.Event.QUANTUM_BRIDGE_UNSUPPORTED_BLOCK_STATE),
                rejected.diagnostics()
        );
        assertTrue(rejected.quantumActivation().isActive());
    }

    @Test
    void topOnlyCaveAndOutwardLightFollowBlueMapWorldSemantics()
            throws Exception {
        BridgeData data = new BridgeData(QuantumBridgeSnapshot.Role.LINK, false);
        Fixture topOnly = fixture(
                data,
                TOP_ONLY_SETTINGS,
                true,
                Map.of(CENTER.plus(0, 1, 0), new LightLevels(12, 9))
        );
        RenderResult top = render(topOnly);
        assertEquals(18, top.model().size());
        for (FaceData face : top.model().faces()) {
            assertEquals(12, face.sunlight());
            assertEquals(9, face.blocklight());
        }
        Color expected = expectedLinkTopMapColor(topOnly, 12F / 15F);
        assertEquals(expected.r, top.mapColor().r, 0.000001F);
        assertEquals(expected.g, top.mapColor().g, 0.000001F);
        assertEquals(expected.b, top.mapColor().b, 0.000001F);
        assertEquals(expected.a, top.mapColor().a, 0.000001F);

        Fixture cave = fixture(
                new BridgeData(QuantumBridgeSnapshot.Role.LINK, false),
                DARK_CAVE_SETTINGS,
                true,
                darkLights()
        );
        assertEquals(0, render(cave).model().size());
    }

    @Test
    void capacityPropagatesAndEmissionFailureDisablesOnlyQuantumRoute()
            throws Exception {
        Fixture capacity = fixture(new BridgeData(QuantumBridgeSnapshot.Role.LINK, false));
        RecordingTileModel capacityModel = new RecordingTileModel();
        capacityModel.add(1);
        capacityModel.setMaterialIndex(0, 777);
        capacityModel.resetAddInvocations();
        capacityModel.failCapacityOnNextAdd();
        Color color = new Color().set(0.1F, 0.2F, 0.3F, 0.4F, false);

        assertThrows(
                MaxCapacityReachedException.class,
                () -> capacity.renderer().render(
                        capacity.neighborhood(),
                        null,
                        new TileModelView(capacityModel),
                        color
                )
        );
        assertEquals(1, capacityModel.size());
        assertEquals(777, capacityModel.face(0).material());
        assertEquals(1, capacityModel.addInvocations());
        assertTrue(capacity.quantumActivation().isActive());

        Fixture failed = fixture(new BridgeData(QuantumBridgeSnapshot.Role.LINK, false));
        RecordingTileModel failedModel = new RecordingTileModel();
        failedModel.add(1);
        failedModel.setMaterialIndex(0, 777);
        failedModel.failPositionOnNextWrite();
        failed.renderer().render(
                failed.neighborhood(),
                null,
                new TileModelView(failedModel),
                new Color()
        );

        assertTrue(failedModel.size() > 1);
        assertEquals(777, failedModel.face(0).material());
        assertOriginalMaterials(failed, failedModel, 1);
        assertTrue(failed.profileActivation().isActive());
        assertTrue(failed.quantumActivation().isDisabled());
        assertEquals(
                "quantum-bridge-render-callback-failed",
                failed.quantumActivation().reason()
        );
    }

    @Test
    void resourceMismatchFallsBackWithoutTouchingAcceptedRoutes() throws Exception {
        Fixture fixture = fixture(
                new BridgeData(QuantumBridgeSnapshot.Role.LINK, false),
                TEST_SETTINGS,
                false,
                Map.of()
        );

        assertOriginalOnly(fixture, render(fixture).model());
        assertTrue(fixture.profileActivation().isActive());
        assertTrue(fixture.quantumActivation().isActive());
        assertEquals(
                List.of(BoundedDiagnostics.Event
                        .QUANTUM_BRIDGE_REQUIRED_RESOURCES_MISMATCH),
                fixture.diagnostics()
        );
    }

    private static void assertInvalidTopology(
            BridgeData data,
            BoundedDiagnostics.Event event
    ) throws Exception {
        Fixture fixture = fixture(data);
        assertOriginalOnly(fixture, render(fixture).model());
        assertEquals(List.of(event), fixture.diagnostics());
        assertTrue(fixture.profileActivation().isActive());
        assertTrue(fixture.quantumActivation().isActive());
    }

    private static Fixture fixture(BridgeData data) throws Exception {
        return fixture(data, TEST_SETTINGS, true, Map.of());
    }

    private static Fixture fixture(
            BridgeData data,
            RenderSettings settings,
            boolean resourcesSupported,
            Map<Position, LightLevels> lights
    ) throws Exception {
        ResourcePack resourcePack = M3eQuantumBridgeResourceModelsTest.exactResources();
        M3eQuantumBridgeResourceModelsTest.putTexture(
                resourcePack,
                ORIGINAL,
                0xFF224466
        );
        putStockResources(resourcePack);
        TextureGallery gallery = new TextureGallery();
        gallery.put(resourcePack.getTextures());
        BlockNeighborhood neighborhood = new BlockNeighborhood(
                new TestBlockAccess(data.states(), data.entities(), lights),
                resourcePack,
                settings,
                DimensionType.OVERWORLD
        );
        neighborhood.set(X, Y, Z);

        ProfileActivation profileActivation = new ProfileActivation();
        QuantumBridgeRouteActivation quantumActivation =
                new QuantumBridgeRouteActivation();
        List<BoundedDiagnostics.Event> diagnostics = new ArrayList<>();
        profileActivation.activate();
        quantumActivation.activate();
        return new Fixture(
                resourcePack,
                gallery,
                neighborhood,
                profileActivation,
                quantumActivation,
                diagnostics,
                new QuantumBridgeRenderer(
                        resourcePack,
                        gallery,
                        settings,
                        profileActivation,
                        quantumActivation,
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

    private static Color expectedLinkTopMapColor(Fixture fixture, float illumination) {
        QuantumBridgeSnapshot snapshot = new QuantumBridgeSnapshot(
                QuantumBridgeSnapshot.Role.LINK,
                java.util.Set.of(
                        Direction6.DOWN,
                        Direction6.UP,
                        Direction6.WEST,
                        Direction6.EAST
                ),
                false
        );
        Color expected = new Color().set(0F, 0F, 0F, 0F, true);
        float opacity = 0F;
        for (QuantumBridgeGeometry.Quad quad : QuantumBridgeGeometry.forSnapshot(snapshot)) {
            if (quad.face() != Direction6.UP) {
                continue;
            }
            Color layer = new Color().set(
                    fixture.resourcePack().getTextures().get(
                            M3eQuantumBridgeResourceModels.texture(quad)
                    ).getColorPremultiplied()
            );
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

    private static boolean isLightMaterial(Fixture fixture, int material) {
        return material == fixture.gallery().get(Key.parse(
                M3eQuantumBridgeResourceModels.RING_LIGHT_TEXTURE
        )) || material == fixture.gallery().get(Key.parse(
                M3eQuantumBridgeResourceModels.RING_LIGHT_CORNER_TEXTURE
        ));
    }

    private static boolean hasDarkenedAo(float[] values) {
        for (float value : values) {
            if (value < 1F) {
                return true;
            }
        }
        return false;
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
        assertTrue(model.size() > 0);
        assertOriginalMaterials(fixture, model, 0);
    }

    private static Map<Position, LightLevels> darkLights() {
        LightLevels dark = new LightLevels(0, 0);
        return Map.of(
                CENTER, dark,
                CENTER.plus(-1, 0, 0), dark,
                CENTER.plus(1, 0, 0), dark,
                CENTER.plus(0, -1, 0), dark,
                CENTER.plus(0, 1, 0), dark,
                CENTER.plus(0, 0, -1), dark,
                CENTER.plus(0, 0, 1), dark
        );
    }

    private static void assertOriginalMaterials(
            Fixture fixture,
            RecordingTileModel model,
            int start
    ) {
        int original = fixture.gallery().get(ORIGINAL);
        for (int triangle = start; triangle < model.size(); triangle++) {
            assertEquals(original, model.face(triangle).material());
        }
    }

    private static BlockState exactState(boolean ring, boolean waterlogged) {
        String id = ring
                ? M3eQuantumBridgeResourceModels.RING_BLOCK
                : M3eQuantumBridgeResourceModels.LINK_BLOCK;
        return BlockState.fromString(
                id + "[formed=true,waterlogged=" + waterlogged + "]"
        );
    }

    private static TestBlockEntity entity(Key id, Position position) {
        return new TestBlockEntity(id, position.x(), position.y(), position.z());
    }

    private static void putStockResources(ResourcePack resourcePack) {
        putStockState(
                resourcePack,
                Key.parse(M3eQuantumBridgeResourceModels.LINK_BLOCK),
                new Vector3f(2F, 2F, 2F),
                new Vector3f(14F, 14F, 14F),
                "test:block/quantum_link_original_model"
        );
        putStockState(
                resourcePack,
                Key.parse(M3eQuantumBridgeResourceModels.RING_BLOCK),
                new Vector3f(2F, 2F, 2F),
                new Vector3f(14F, 14F, 14F),
                "test:block/quantum_ring_original_model"
        );
        putStockState(
                resourcePack,
                OPAQUE,
                Vector3f.ZERO,
                new Vector3f(16F, 16F, 16F),
                "test:block/opaque_model"
        );
    }

    private static void putStockState(
            ResourcePack resourcePack,
            Key block,
            Vector3f from,
            Vector3f to,
            String modelId
    ) {
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
        Model model = new Model(new Element(from, to, faces));
        model.calculateProperties(resourcePack.getTextures());
        Key modelKey = Key.parse(modelId);
        resourcePack.getModels().put(modelKey, model);
        Variant variant = new Variant(new ResourcePath<Model>(modelKey));
        de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.BlockState state =
                new de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.BlockState(
                        new Variants(new VariantSet[0], new VariantSet(variant))
                );
        resourcePack.getBlockStates().put(block, state);
    }

    private static final RenderSettings TEST_SETTINGS = settings(false, false);
    private static final RenderSettings TOP_ONLY_SETTINGS = settings(true, false);
    private static final RenderSettings DARK_CAVE_SETTINGS = settings(false, true);

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

    private static final class BridgeData {
        private final Position link;
        private final Map<Position, BlockState> states = new HashMap<>();
        private final Map<Position, BlockEntity> entities = new HashMap<>();

        private BridgeData(QuantumBridgeSnapshot.Role role, boolean centerWaterlogged) {
            link = switch (role) {
                case LINK -> CENTER;
                case CORNER_RING -> CENTER.plus(1, 1, 0);
                case EDGE_RING -> CENTER.plus(0, 1, 0);
            };
            Key entityId = Key.parse(M3eQuantumBridgeResourceModels.BLOCK_ENTITY_ID);
            for (int x = -1; x <= 1; x++) {
                for (int y = -1; y <= 1; y++) {
                    Position position = link.plus(x, y, 0);
                    boolean ring = x != 0 || y != 0;
                    states.put(
                            position,
                            exactState(ring, position.equals(CENTER) && centerWaterlogged)
                    );
                    entities.put(position, entity(entityId, position));
                }
            }
        }

        private Position link() {
            return link;
        }

        private Map<Position, BlockState> states() {
            return states;
        }

        private Map<Position, BlockEntity> entities() {
            return entities;
        }
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
            ProfileActivation profileActivation,
            QuantumBridgeRouteActivation quantumActivation,
            List<BoundedDiagnostics.Event> diagnostics,
            QuantumBridgeRenderer renderer
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
