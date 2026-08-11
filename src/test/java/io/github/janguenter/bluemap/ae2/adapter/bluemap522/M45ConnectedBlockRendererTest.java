/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.adapter.bluemap522;

import com.flowpowered.math.vector.Vector3f;
import com.flowpowered.math.vector.Vector4f;
import de.bluecolored.bluemap.core.map.TextureGallery;
import de.bluecolored.bluemap.core.map.hires.ArrayTileModel;
import de.bluecolored.bluemap.core.map.hires.RenderSettings;
import de.bluecolored.bluemap.core.map.hires.TileModelView;
import de.bluecolored.bluemap.core.map.mask.Mask;
import de.bluecolored.bluemap.core.resources.ResourcePath;
import de.bluecolored.bluemap.core.resources.pack.PackVersion;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.Variant;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.VariantSet;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.Variants;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.model.Element;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.model.Face;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.model.Model;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.model.TextureVariable;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.texture.Texture;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.texture.AnimationMeta;
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
import io.github.janguenter.bluemap.ae2.activation.ExtensionRouteActivation;
import io.github.janguenter.bluemap.ae2.model.advancedae.AdvancedAeAthenaGeometry;
import io.github.janguenter.bluemap.ae2.model.advancedae.AdvancedQuantumGeometry;
import io.github.janguenter.bluemap.ae2.model.extendedae.ExtendedAeMatrixGlassGeometry;
import io.github.janguenter.bluemap.ae2.profile.advancedae.AdvancedAe1612Catalog;
import io.github.janguenter.bluemap.ae2.profile.extendedae.ExtendedAe2235Catalog;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class M45ConnectedBlockRendererTest {

    private static final int X = 17;
    private static final int Y = 84;
    private static final int Z = -9;
    private static final Position CENTER = new Position(X, Y, Z);
    private static final Key ORIGINAL = Key.parse("test:block/m45_original");
    private static final String ADVANCED_STRUCTURE =
            AdvancedAe1612Catalog.structureBlockIds().iterator().next();
    private static final String MATRIX_GLASS = ExtendedAe2235Catalog.matrixDefinitions()
            .values().stream()
            .filter(definition -> definition.kind()
                    == ExtendedAe2235Catalog.MatrixKind.GLASS)
            .findFirst()
            .orElseThrow()
            .blockId();

    @Test
    void recognizesOnlyTheExactAdvancedAthenaAndMatrixBlockCatalogs()
            throws Exception {
        Fixture fixture = fixture(scenarios().getFirst().exactState(),
                scenarios().getFirst().blockEntityId());

        for (String blockId : AdvancedAe1612Catalog.quantumBlockIds()) {
            assertTrue(fixture.renderer().handles(blockId));
        }
        assertTrue(fixture.renderer().handles(AdvancedAe1612Catalog.QUANTUM_ALLOY_BLOCK));
        for (String blockId : ExtendedAe2235Catalog.matrixBlockIds()) {
            assertTrue(fixture.renderer().handles(blockId));
        }
        assertFalse(fixture.renderer().handles("minecraft:stone"));
    }

    @Test
    void allThreeConnectedRoutesEmitCustomGeometryForExactPersistedInput()
            throws Exception {
        for (Scenario scenario : scenarios()) {
            Fixture fixture = fixture(scenario.exactState(), scenario.blockEntityId());
            RecordingTileModel model = render(fixture, false);

            assertTrue(model.size() > 12, scenario.routeId());
            assertNotEquals(12, model.size(), scenario.routeId());
            assertEquals(1, model.addInvocations(), scenario.routeId());
            assertTrue(fixture.runtime().active(scenario.routeId()), scenario.routeId());
            assertTrue(fixture.runtime().routes().stream()
                    .allMatch(ExtensionRouteActivation::isActive));
        }
    }

    @Test
    void malformedPersistedInputFallsBackAtomicallyWithoutDisablingAnyRoute()
            throws Exception {
        for (Scenario scenario : scenarios()) {
            Fixture fixture = fixture(scenario.malformedState(), scenario.blockEntityId());
            RecordingTileModel model = render(fixture, false);

            assertEquals(12, model.size(), scenario.routeId());
            assertTrue(fixture.runtime().routes().stream()
                    .allMatch(ExtensionRouteActivation::isActive), scenario.routeId());
        }
    }

    @Test
    void callbackFailureDisablesOnlyItsOwningRouteThenRendersStock()
            throws Exception {
        for (Scenario scenario : scenarios()) {
            Fixture fixture = fixture(scenario.exactState(), scenario.blockEntityId());
            RecordingTileModel model = render(fixture, true);

            assertEquals(12, model.size(), scenario.routeId());
            assertTrue(model.addInvocations() > 1, scenario.routeId());
            ExtensionRouteActivation failed = fixture.runtime().route(scenario.routeId());
            assertTrue(failed.isDisabled(), scenario.routeId());
            assertEquals(
                    ExtensionRouteActivation.Reason.RENDER_CALLBACK_FAILED,
                    failed.snapshot().reason(),
                    scenario.routeId()
            );
            assertEquals("render-callback-failed", failed.snapshot().detail());
            assertEquals(
                    7,
                    fixture.runtime().routes().stream()
                            .filter(ExtensionRouteActivation::isActive)
                            .count(),
                    scenario.routeId()
            );
            assertTrue(fixture.runtime().routes().stream()
                    .filter(route -> !route.routeId().equals(scenario.routeId()))
                    .allMatch(ExtensionRouteActivation::isActive), scenario.routeId());
        }
    }

    @Test
    void inactiveOwningRouteDelegatesWithoutChangingActivePeers() throws Exception {
        for (Scenario scenario : scenarios()) {
            Fixture fixture = fixture(scenario.exactState(), scenario.blockEntityId());
            fixture.runtime().route(scenario.routeId()).inactive(
                    ExtensionRouteActivation.Reason.AWAITING_EXACT_PROFILE,
                    "awaiting-exact-profile"
            );
            RecordingTileModel model = render(fixture, false);

            assertEquals(12, model.size(), scenario.routeId());
            assertFalse(fixture.runtime().active(scenario.routeId()));
            assertEquals(
                    7,
                    fixture.runtime().routes().stream()
                            .filter(ExtensionRouteActivation::isActive)
                            .count()
            );
        }
    }

    private static List<Scenario> scenarios() {
        return List.of(
                new Scenario(
                        M45Runtime.ADVANCED_QUANTUM,
                        BlockState.fromString(ADVANCED_STRUCTURE
                                + "[formed=true,powered=true,multiblocked=true,light_level=15]"),
                        BlockState.fromString(ADVANCED_STRUCTURE
                                + "[formed=true,powered=false,multiblocked=true,light_level=16]"),
                        AdvancedAe1612Catalog.QUANTUM_BLOCK_ENTITY
                ),
                new Scenario(
                        M45Runtime.ADVANCED_ATHENA,
                        BlockState.fromString(AdvancedAe1612Catalog.QUANTUM_ALLOY_BLOCK),
                        BlockState.fromString(AdvancedAe1612Catalog.QUANTUM_ALLOY_BLOCK
                                + "[future=false]"),
                        null
                ),
                new Scenario(
                        M45Runtime.EXTENDED_MATRIX,
                        BlockState.fromString(MATRIX_GLASS
                                + "[formed=true,powered=true]"),
                        BlockState.fromString(MATRIX_GLASS
                                + "[formed=true,powered=false,future=false]"),
                        MATRIX_GLASS
                )
        );
    }

    private static Fixture fixture(BlockState center, String blockEntityId)
            throws Exception {
        ResourcePack resourcePack = new ResourcePack(new PackVersion(34, 0));
        putTexture(resourcePack, ORIGINAL);
        for (String texture : List.of(
                AdvancedQuantumGeometry.INTERNAL_FACE_TEXTURE,
                AdvancedQuantumGeometry.INTERNAL_SIDE_TEXTURE,
                AdvancedQuantumGeometry.STRUCTURE_FACE_TEXTURE,
                AdvancedQuantumGeometry.STRUCTURE_SIDE_TEXTURE,
                ExtendedAeMatrixGlassGeometry.SIDE_TEXTURE
        )) {
            putTexture(resourcePack, Key.parse(texture));
        }
        for (AdvancedAeAthenaGeometry.Texture texture
                : AdvancedAeAthenaGeometry.Texture.values()) {
            putAthenaTexture(resourcePack, Key.parse(texture.textureId()));
        }
        assertTrue(M45AthenaTextures.bake(resourcePack));
        for (String texture : ExtendedAeMatrixGlassGeometry.FACE_TEXTURES) {
            putTexture(resourcePack, Key.parse(texture));
        }
        putStockState(resourcePack, center.getId());

        TextureGallery gallery = new TextureGallery();
        gallery.put(resourcePack.getTextures());
        Map<Position, BlockState> states = new HashMap<>();
        states.put(CENTER, center);
        Map<Position, BlockEntity> entities = new HashMap<>();
        if (blockEntityId != null) {
            entities.put(CENTER, new TestBlockEntity(
                    Key.parse(blockEntityId), X, Y, Z
            ));
        }
        BlockNeighborhood neighborhood = new BlockNeighborhood(
                new TestBlockAccess(states, entities),
                resourcePack,
                SETTINGS,
                DimensionType.OVERWORLD
        );
        neighborhood.set(X, Y, Z);

        M45Runtime runtime = new M45Runtime();
        runtime.routes().forEach(route -> route.activate("exact-profile"));
        return new Fixture(
                neighborhood,
                runtime,
                new M45ConnectedBlockRenderer(
                        resourcePack,
                        gallery,
                        SETTINGS,
                        runtime
                )
        );
    }

    private static RecordingTileModel render(Fixture fixture, boolean failPosition) {
        RecordingTileModel model = new RecordingTileModel();
        if (failPosition) {
            model.failPositionOnNextWrite();
        }
        fixture.renderer().render(
                fixture.neighborhood(),
                new TileModelView(model),
                new Color(),
                0
        );
        return model;
    }

    private static void putStockState(ResourcePack resourcePack, Key blockId) {
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
        Key modelKey = Key.parse("test:block/m45_original_model");
        resourcePack.getModels().put(modelKey, model);
        Variant variant = new Variant(new ResourcePath<Model>(modelKey));
        resourcePack.getBlockStates().put(
                blockId,
                new de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.BlockState(
                        new Variants(new VariantSet[0], new VariantSet(variant))
                )
        );
    }

    private static void putTexture(ResourcePack resourcePack, Key key) throws IOException {
        BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        image.setRGB(0, 0, 0xFFFFFFFF);
        resourcePack.getTextures().put(key, Texture.from(key, image));
    }

    private static void putAthenaTexture(ResourcePack resourcePack, Key key)
            throws IOException {
        BufferedImage image = new BufferedImage(16, 32, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                image.setRGB(x, y, 0xFFFFFFFF);
            }
        }
        resourcePack.getTextures().put(key, Texture.from(
                key,
                image,
                new AnimationMeta(true, 1, 1, 1, List.of(
                        new AnimationMeta.FrameMeta(0, 32),
                        new AnimationMeta.FrameMeta(1, 4)
                ))
        ));
    }

    private static final RenderSettings SETTINGS = new RenderSettings() {
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

    private static final class TestBlockAccess implements BlockAccess {

        private final Map<Position, BlockState> states;
        private final Map<Position, BlockEntity> entities;
        private int x;
        private int y;
        private int z;

        private TestBlockAccess(
                Map<Position, BlockState> states,
                Map<Position, BlockEntity> entities
        ) {
            this.states = states;
            this.entities = entities;
        }

        @Override
        public void set(int newX, int newY, int newZ) {
            x = newX;
            y = newY;
            z = newZ;
        }

        @Override
        public BlockAccess copy() {
            return new TestBlockAccess(states, entities);
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
            return new LightData(7, 4);
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

        private int addInvocations;
        private boolean failPosition;

        private RecordingTileModel() {
            super(1024);
        }

        private int addInvocations() {
            return addInvocations;
        }

        private void failPositionOnNextWrite() {
            failPosition = true;
        }

        @Override
        public int add(int count) {
            addInvocations++;
            return super.add(count);
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
                throw new IllegalStateException("injected M4/M5 emission failure");
            }
            super.setPositions(face, x1, y1, z1, x2, y2, z2, x3, y3, z3);
            return this;
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

    private record Position(int x, int y, int z) {
    }

    private record Scenario(
            String routeId,
            BlockState exactState,
            BlockState malformedState,
            String blockEntityId
    ) {
    }

    private record Fixture(
            BlockNeighborhood neighborhood,
            M45Runtime runtime,
            M45ConnectedBlockRenderer renderer
    ) {
    }
}
