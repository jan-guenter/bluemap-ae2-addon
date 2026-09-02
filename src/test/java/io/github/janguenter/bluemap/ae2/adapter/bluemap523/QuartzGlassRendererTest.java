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
import io.github.janguenter.bluemap.ae2.activation.QuartzGlassRouteActivation;
import io.github.janguenter.bluemap.ae2.model.Direction6;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;
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

class QuartzGlassRendererTest {

    private static final int X = 5;
    private static final int Y = 70;
    private static final int Z = -4;
    private static final Position CENTER = new Position(X, Y, Z);
    private static final Key ORIGINAL = Key.parse("test:block/quartz_glass_original");
    private static final Key OPAQUE = Key.parse("test:opaque");
    private static final Key NONNATIVE = Key.parse("test:nonnative");
    private static final Key BASE_A = Key.parse("ae2:block/glass/quartz_glass_a");
    private static final Key FRAME_1111 = Key.parse(
            "ae2:block/glass/quartz_glass_frame1111"
    );

    @Test
    void isolatedGlassUsesExactBaseFrameGeometryLightAoAndHostMapColor()
            throws Exception {
        Fixture fixture = fixture(Map.of(), TEST_SETTINGS, Map.of(
                offset(Direction6.UP), new LightLevels(5, 4)
        ));
        RenderResult result = render(fixture);

        assertEquals(24, result.model().size());
        assertEquals(1, result.model().addInvocations());
        int baseMaterial = fixture.gallery().get(BASE_A);
        int frameMaterial = fixture.gallery().get(FRAME_1111);
        assertEquals(baseMaterial, result.model().face(0).material());
        assertEquals(frameMaterial, result.model().face(2).material());
        assertArrayEquals(
                new float[]{0F, 0F, 0F, 15F / 16F, 15F / 16F, 15F / 16F},
                result.model().face(0).uvs(),
                0F
        );
        for (int triangle = 0; triangle < result.model().size(); triangle++) {
            assertArrayEquals(
                    new float[]{1F, 1F, 1F},
                    result.model().face(triangle).aos(),
                    0F
            );
        }
        assertEquals(5, result.model().face(4).sunlight());
        assertEquals(4, result.model().face(4).blocklight());

        Color expected = expectedTopMapColor(fixture, 5F / 15F);
        assertEquals(expected.r, result.mapColor().r, 0.000001F);
        assertEquals(expected.g, result.mapColor().g, 0.000001F);
        assertEquals(expected.b, result.mapColor().b, 0.000001F);
        assertEquals(expected.a, result.mapColor().a, 0.000001F);
    }

    @Test
    void ordinaryAndVibrantGlassCrossConnectAndSuppressTheirSharedFace()
            throws Exception {
        Fixture fixture = fixture(Map.of(
                offset(Direction6.EAST), BlockState.fromString(
                        "ae2:quartz_vibrant_glass"
                )
        ));

        RecordingTileModel model = render(fixture).model();

        assertEquals(20, model.size());
        assertFalse(hasBoundaryPlane(model, Direction6.EAST));
        assertTrue(fixture.glassActivation().isActive());
    }

    @Test
    void diagonalGlassIsNeverReadAsAConnection() throws Exception {
        Fixture fixture = fixture(Map.of(
                new Position(X + 1, Y + 1, Z),
                BlockState.fromString("ae2:quartz_vibrant_glass")
        ));

        assertEquals(24, render(fixture).model().size());
    }

    @Test
    void sixDirectMixedNeighborsFullyEncloseTheCenter() throws Exception {
        Map<Position, BlockState> states = new HashMap<>();
        for (Direction6 direction : Direction6.values()) {
            states.put(
                    offset(direction),
                    BlockState.fromString(direction.ordinal() % 2 == 0
                            ? "ae2:quartz_glass"
                            : "ae2:quartz_vibrant_glass")
            );
        }
        Fixture fixture = fixture(states);

        assertEquals(0, render(fixture).model().size());
    }

    @Test
    void missingAndMalformedNativeNeighborsUseAtomicOriginalFallback()
            throws Exception {
        for (BlockState neighbor : List.of(
                BlockState.MISSING,
                BlockState.fromString("ae2:quartz_glass[future=false]")
        )) {
            Fixture fixture = fixture(Map.of(offset(Direction6.NORTH), neighbor));

            RecordingTileModel model = render(fixture).model();

            assertTrue(model.size() > 0);
            int original = fixture.gallery().get(ORIGINAL);
            for (int triangle = 0; triangle < model.size(); triangle++) {
                assertEquals(original, model.face(triangle).material());
            }
            assertTrue(fixture.profileActivation().isActive());
            assertTrue(fixture.glassActivation().isActive());
        }
    }

    @Test
    void knownNonnativeNeighborIsDisconnectedWhileOpaqueNeighborCullsOneFace()
            throws Exception {
        Fixture nonnative = fixture(Map.of(
                offset(Direction6.EAST),
                BlockState.fromString(NONNATIVE.getFormatted())
        ));
        assertEquals(24, render(nonnative).model().size());

        Fixture opaque = fixture(Map.of(
                offset(Direction6.UP),
                BlockState.fromString(OPAQUE.getFormatted())
        ));
        assertTrue(opaque.resourcePack().getBlockProperties(
                BlockState.fromString(OPAQUE.getFormatted())
        ).isCulling());
        RecordingTileModel model = render(opaque).model();
        assertEquals(20, model.size());
        assertFalse(hasBoundaryPlane(model, Direction6.UP));
    }

    @Test
    void topOnlyAndCaveRemovalMatchBlueMapFaceLightPolicy() throws Exception {
        Fixture topOnly = fixture(Map.of(), TOP_ONLY_SETTINGS, Map.of());
        RecordingTileModel topModel = render(topOnly).model();
        assertEquals(4, topModel.size());
        for (int triangle = 0; triangle < topModel.size(); triangle++) {
            float[] positions = topModel.face(triangle).positions();
            assertEquals(1F, positions[1]);
            assertEquals(1F, positions[4]);
            assertEquals(1F, positions[7]);
        }

        Fixture cave = fixture(Map.of(), DARK_CAVE_SETTINGS, Map.of(), 0, 0, true);
        assertEquals(0, render(cave).model().size());
        assertTrue(cave.glassActivation().isActive());
    }

    @Test
    void vibrantEmissionClampsLightAndControlsBlocklightAwareCaveRemoval()
            throws Exception {
        Map<Position, BlockState> vibrantCenter = Map.of(
                CENTER,
                BlockState.fromString("ae2:quartz_vibrant_glass")
        );
        Fixture vibrant = fixture(
                vibrantCenter,
                TEST_SETTINGS,
                Map.of(),
                0,
                0,
                true
        );
        RenderResult vibrantResult = render(vibrant);
        assertEquals(24, vibrantResult.model().size());
        for (int triangle = 0; triangle < vibrantResult.model().size(); triangle++) {
            assertEquals(0, vibrantResult.model().face(triangle).sunlight());
            assertEquals(15, vibrantResult.model().face(triangle).blocklight());
        }
        Color expectedFullbright = expectedTopMapColor(vibrant, 1F);
        assertEquals(expectedFullbright.r, vibrantResult.mapColor().r, 0.000001F);
        assertEquals(expectedFullbright.g, vibrantResult.mapColor().g, 0.000001F);
        assertEquals(expectedFullbright.b, vibrantResult.mapColor().b, 0.000001F);
        assertEquals(expectedFullbright.a, vibrantResult.mapColor().a, 0.000001F);

        Fixture blocklightAwareCave = fixture(
                vibrantCenter,
                DARK_CAVE_BLOCKLIGHT_SETTINGS,
                Map.of(),
                0,
                0,
                true
        );
        assertEquals(24, render(blocklightAwareCave).model().size());

        Fixture skyOnlyCave = fixture(
                vibrantCenter,
                DARK_CAVE_SETTINGS,
                Map.of(),
                0,
                0,
                true
        );
        assertEquals(0, render(skyOnlyCave).model().size());

        Fixture ordinary = fixture(
                Map.of(),
                TEST_SETTINGS,
                Map.of(),
                0,
                0,
                true
        );
        RecordingTileModel ordinaryModel = render(ordinary).model();
        assertEquals(24, ordinaryModel.size());
        for (int triangle = 0; triangle < ordinaryModel.size(); triangle++) {
            assertEquals(0, ordinaryModel.face(triangle).blocklight());
        }
        Fixture ordinaryCave = fixture(
                Map.of(),
                DARK_CAVE_BLOCKLIGHT_SETTINGS,
                Map.of(),
                0,
                0,
                true
        );
        assertEquals(0, render(ordinaryCave).model().size());
    }

    @Test
    void capacityIsOneAtomicHostControlFlowReservation() throws Exception {
        Fixture fixture = fixture(Map.of());
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
        assertEquals(0.1F, color.r);
        assertTrue(fixture.profileActivation().isActive());
        assertTrue(fixture.glassActivation().isActive());
    }

    @Test
    void emissionFailureRollsBackThenDisablesOnlyGlassAndRendersOriginal()
            throws Exception {
        Fixture fixture = fixture(Map.of());
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
        assertTrue(fixture.glassActivation().isDisabled());
        assertEquals(
                "quartz-glass-render-callback-failed",
                fixture.glassActivation().reason()
        );
    }

    @Test
    void inactiveOrResourceRejectedRouteDelegatesWithoutChangingActivationPeers()
            throws Exception {
        Fixture inactive = fixture(Map.of());
        inactive.glassActivation().inactive(
                QuartzGlassRouteActivation.Reason.AWAITING_EXACT_PROFILE
        );
        assertEquals(12, render(inactive).model().size());
        assertTrue(inactive.profileActivation().isActive());

        Fixture rejected = fixture(Map.of(), TEST_SETTINGS, Map.of(), 3, 2, false);
        assertEquals(12, render(rejected).model().size());
        assertTrue(rejected.profileActivation().isActive());
        assertTrue(rejected.glassActivation().isActive());
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

    private static Color expectedTopMapColor(Fixture fixture, float illumination) {
        Color base = new Color().set(
                fixture.resourcePack().getTextures().get(BASE_A)
                        .getColorPremultiplied()
        );
        Color frame = new Color().set(
                fixture.resourcePack().getTextures().get(FRAME_1111)
                        .getColorPremultiplied()
        );
        float opacity = Math.max(base.a, frame.a);
        for (Color layer : List.of(base, frame)) {
            layer.r *= illumination;
            layer.g *= illumination;
            layer.b *= illumination;
        }
        Color expected = new Color().set(0F, 0F, 0F, 0F, true);
        expected.add(base);
        expected.add(frame);
        expected.flatten().straight();
        expected.a = opacity;
        return expected;
    }

    private static Fixture fixture(Map<Position, BlockState> overrides) throws Exception {
        return fixture(overrides, TEST_SETTINGS, Map.of());
    }

    private static Fixture fixture(
            Map<Position, BlockState> overrides,
            RenderSettings settings,
            Map<Position, LightLevels> lights
    ) throws Exception {
        return fixture(overrides, settings, lights, 3, 2, true);
    }

    private static Fixture fixture(
            Map<Position, BlockState> overrides,
            RenderSettings settings,
            Map<Position, LightLevels> lights,
            int defaultSkyLight,
            int defaultBlockLight,
            boolean resourcesSupported
    ) throws Exception {
        ResourcePack resourcePack = M3cQuartzGlassResourceModelsTest.exactResources();
        M3cQuartzGlassResourceModelsTest.putTexture(resourcePack, BASE_A, 0x80336699);
        M3cQuartzGlassResourceModelsTest.putTexture(resourcePack, FRAME_1111, 0x80CC3300);
        M3cQuartzGlassResourceModelsTest.putTexture(resourcePack, ORIGINAL, 0xFF224466);
        putOriginalAndOpaqueResources(resourcePack);
        TextureGallery gallery = new TextureGallery();
        gallery.put(resourcePack.getTextures());

        Map<Position, BlockState> states = new HashMap<>(overrides);
        states.putIfAbsent(CENTER, BlockState.fromString("ae2:quartz_glass"));
        BlockNeighborhood neighborhood = new BlockNeighborhood(
                new TestBlockAccess(states, defaultSkyLight, defaultBlockLight, lights),
                resourcePack,
                settings,
                DimensionType.OVERWORLD
        );
        neighborhood.set(X, Y, Z);

        ProfileActivation profileActivation = new ProfileActivation();
        QuartzGlassRouteActivation glassActivation = new QuartzGlassRouteActivation();
        profileActivation.activate();
        glassActivation.activate();
        return new Fixture(
                resourcePack,
                gallery,
                neighborhood,
                profileActivation,
                glassActivation,
                new QuartzGlassRenderer(
                        resourcePack,
                        gallery,
                        settings,
                        profileActivation,
                        glassActivation,
                        ignored -> resourcesSupported
                )
        );
    }

    private static void putOriginalAndOpaqueResources(ResourcePack resourcePack) {
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
        Key modelKey = Key.parse("test:block/quartz_glass_original_model");
        resourcePack.getModels().put(modelKey, model);
        Variant variant = new Variant(new ResourcePath<Model>(modelKey));
        de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.BlockState state =
                new de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.BlockState(
                        new Variants(new VariantSet[0], new VariantSet(variant))
                );
        resourcePack.getBlockStates().put(Key.parse("ae2:quartz_glass"), state);
        resourcePack.getBlockStates().put(Key.parse("ae2:quartz_vibrant_glass"), state);
        resourcePack.getBlockStates().put(OPAQUE, state);
    }

    private static Position offset(Direction6 direction) {
        return new Position(
                X + direction.stepX(),
                Y + direction.stepY(),
                Z + direction.stepZ()
        );
    }

    private static final RenderSettings TEST_SETTINGS = settings(false, false);
    private static final RenderSettings TOP_ONLY_SETTINGS = settings(true, false);
    private static final RenderSettings DARK_CAVE_SETTINGS = settings(false, true);
    private static final RenderSettings DARK_CAVE_BLOCKLIGHT_SETTINGS =
            settings(false, true, true);

    private static RenderSettings settings(boolean topOnly, boolean cave) {
        return settings(topOnly, cave, false);
    }

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
        private final int defaultSkyLight;
        private final int defaultBlockLight;
        private final Map<Position, LightLevels> lights;
        private int x;
        private int y;
        private int z;

        private TestBlockAccess(
                Map<Position, BlockState> states,
                int defaultSkyLight,
                int defaultBlockLight,
                Map<Position, LightLevels> lights
        ) {
            this.states = states;
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
                    : new LightData(override.skyLight(), override.blockLight());
        }

        @Override
        public Biome getBiome() {
            return Biome.DEFAULT;
        }

        @Override
        public BlockEntity getBlockEntity() {
            return null;
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
            super(128);
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
            face(face).positions = new float[]{x1, y1, z1, x2, y2, z2, x3, y3, z3};
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
        private float[] uvs;
        private float[] aos;
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
            QuartzGlassRouteActivation glassActivation,
            QuartzGlassRenderer renderer
    ) {
    }

    private record RenderResult(RecordingTileModel model, Color mapColor) {
    }

    private record Position(int x, int y, int z) {
    }

    private record LightLevels(int skyLight, int blockLight) {
    }
}
