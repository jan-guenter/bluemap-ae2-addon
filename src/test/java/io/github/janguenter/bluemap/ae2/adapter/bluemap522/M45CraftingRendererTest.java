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
import io.github.janguenter.bluemap.ae2.activation.ExtensionRouteActivation;
import io.github.janguenter.bluemap.ae2.model.CableColor;
import io.github.janguenter.bluemap.ae2.profile.expandedae.ExpandedAe211Catalog;
import io.github.janguenter.bluemap.ae2.profile.megacells.MegaCells4110Profile;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class M45CraftingRendererTest {

    private static final int X = 31;
    private static final int Y = 90;
    private static final int Z = -14;
    private static final Position CENTER = new Position(X, Y, Z);
    private static final Position EAST = new Position(X + 1, Y, Z);
    private static final Key STOCK_TEXTURE = Key.parse("test:m45_crafting_stock");
    private static final String EXPANDED_UNIT = ExpandedAe211Catalog.CRAFTING_UNIT_BLOCK;
    private static final BlockState EXPANDED_CENTER = BlockState.fromString(
            EXPANDED_UNIT + "[formed=true,powered=false]"
    );
    private static final BlockState MEGA_UNIT = BlockState.fromString(
            MegaCells4110Profile.CRAFTING_UNIT + "[formed=true,powered=false]"
    );

    @Test
    void activeCrossOwnerConnectsWhileInactiveOrMalformedOwnerFallsBack()
            throws Exception {
        Fixture isolated = fixture(EXPANDED_CENTER, null, Map.of(), true);
        int isolatedTriangles = render(isolated, false).size();
        assertTrue(isolatedTriangles > 12);

        Fixture active = fixture(EXPANDED_CENTER, null, Map.of(EAST, MEGA_UNIT), true);
        int connectedTriangles = render(active, false).size();
        assertTrue(connectedTriangles > 12);
        assertTrue(connectedTriangles < isolatedTriangles);

        Fixture inactive = fixture(EXPANDED_CENTER, null, Map.of(EAST, MEGA_UNIT), true);
        inactive.runtime().route(M45Runtime.MEGA_CELLS).inactive(
                ExtensionRouteActivation.Reason.AWAITING_EXACT_PROFILE,
                "awaiting-exact-profile"
        );
        assertEquals(12, render(inactive, false).size());
        assertTrue(inactive.runtime().active(M45Runtime.EXPANDED_AE));

        BlockState malformed = BlockState.fromString(
                MegaCells4110Profile.CRAFTING_UNIT
                        + "[formed=true,powered=false,future=false]"
        );
        Fixture malformedNeighbor = fixture(
                EXPANDED_CENTER,
                null,
                Map.of(EAST, malformed),
                true
        );
        assertEquals(12, render(malformedNeighbor, false).size());
        assertTrue(malformedNeighbor.runtime().active(M45Runtime.EXPANDED_AE));
    }

    @Test
    void nativeNeighborRequiresTheCoreCraftingCapability() throws Exception {
        BlockState nativeNeighbor = BlockState.fromString(
                "ae2:crafting_unit[formed=true,powered=false]"
        );
        Fixture active = fixture(
                EXPANDED_CENTER,
                null,
                Map.of(EAST, nativeNeighbor),
                true
        );
        assertTrue(render(active, false).size() > 12);

        Fixture inactive = fixture(
                EXPANDED_CENTER,
                null,
                Map.of(EAST, nativeNeighbor),
                false
        );
        assertEquals(12, render(inactive, false).size());
        assertTrue(inactive.runtime().active(M45Runtime.EXPANDED_AE));
    }

    @Test
    void megaMonitorRequiresItsExactDtoAndRendersWhenRetained() throws Exception {
        BlockState monitorState = BlockState.fromString(
                MegaCells4110Profile.CRAFTING_MONITOR
                        + "[formed=true,powered=false,facing=south,spin=3]"
        );
        Fixture retained = fixture(
                monitorState,
                monitor(CableColor.CYAN),
                Map.of(),
                true
        );
        assertTrue(render(retained, false).size() > 12);
        assertTrue(retained.runtime().active(M45Runtime.MEGA_CELLS));

        Fixture absent = fixture(monitorState, null, Map.of(), true);
        assertEquals(12, render(absent, false).size());
        assertTrue(absent.runtime().active(M45Runtime.MEGA_CELLS));
    }

    @Test
    void emissionFailureDisablesOnlyTheOwningRouteAndFallsBackAtomically()
            throws Exception {
        Fixture fixture = fixture(EXPANDED_CENTER, null, Map.of(), true);

        assertEquals(12, render(fixture, true).size());
        assertTrue(fixture.runtime().route(M45Runtime.EXPANDED_AE).isDisabled());
        assertEquals(
                ExtensionRouteActivation.Reason.RENDER_CALLBACK_FAILED,
                fixture.runtime().route(M45Runtime.EXPANDED_AE).snapshot().reason()
        );
        assertEquals(
                7,
                fixture.runtime().routes().stream()
                        .filter(ExtensionRouteActivation::isActive)
                        .count()
        );
    }

    private static Fixture fixture(
            BlockState center,
            BlockEntity centerEntity,
            Map<Position, BlockState> neighbors,
            boolean coreCraftingActive
    ) throws Exception {
        ResourcePack pack = resources(center.getId());
        TextureGallery gallery = new TextureGallery();
        gallery.put(pack.getTextures());
        Map<Position, BlockState> states = new HashMap<>(neighbors);
        states.put(CENTER, center);
        Map<Position, BlockEntity> entities = centerEntity == null
                ? Map.of() : Map.of(CENTER, centerEntity);
        BlockNeighborhood neighborhood = new BlockNeighborhood(
                new TestBlockAccess(states, entities),
                pack,
                SETTINGS,
                DimensionType.OVERWORLD
        );
        neighborhood.set(X, Y, Z);
        M45Runtime runtime = new M45Runtime();
        runtime.routes().forEach(route -> route.activate("exact-profile"));
        return new Fixture(
                neighborhood,
                runtime,
                new M45CraftingRenderer(
                        pack,
                        gallery,
                        SETTINGS,
                        runtime,
                        () -> coreCraftingActive
                )
        );
    }

    private static ResourcePack resources(Key centerId) throws IOException {
        ResourcePack pack = new ResourcePack(new PackVersion(34, 0));
        putTexture(pack, STOCK_TEXTURE);
        for (String texture : List.of(
                "expandedae:block/crafting/ring_corner",
                "expandedae:block/crafting/ring_side_hor",
                "expandedae:block/crafting/ring_side_ver",
                "expandedae:block/crafting/unit_base",
                "expandedae:block/crafting/light_base"
        )) {
            putTexture(pack, Key.parse(texture));
        }
        for (String texture : MegaCells4110Profile.craftingTextures()) {
            putTexture(pack, Key.parse(texture));
        }
        for (String texture : MegaCells4110Profile.dependentAe2MonitorTextures()) {
            putTexture(pack, Key.parse(texture));
        }
        putStockState(pack, centerId);
        return pack;
    }

    private static RecordingTileModel render(Fixture fixture, boolean fail) {
        RecordingTileModel model = new RecordingTileModel();
        model.failNextPosition = fail;
        fixture.renderer().render(
                fixture.neighborhood(),
                new TileModelView(model),
                new Color(),
                0
        );
        return model;
    }

    private static void putStockState(ResourcePack pack, Key blockId) {
        EnumMap<Direction, Face> faces = new EnumMap<>(Direction.class);
        for (Direction direction : Direction.values()) {
            faces.put(direction, new Face(
                    new Vector4f(0F, 0F, 16F, 16F),
                    new TextureVariable(new ResourcePath<Texture>(STOCK_TEXTURE)),
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
        model.calculateProperties(pack.getTextures());
        Key modelKey = Key.parse("test:m45_crafting_stock_model");
        pack.getModels().put(modelKey, model);
        pack.getBlockStates().put(
                blockId,
                new de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.BlockState(
                        new Variants(
                                new VariantSet[0],
                                new VariantSet(new Variant(new ResourcePath<Model>(modelKey)))
                        )
                )
        );
    }

    private static void putTexture(ResourcePack pack, Key key) throws IOException {
        BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        image.setRGB(0, 0, 0xffffffff);
        pack.getTextures().put(key, Texture.from(key, image));
    }

    private static Ae2CraftingMonitorBlockEntityData monitor(CableColor color)
            throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (NBTWriter writer = new NBTWriter(bytes)) {
            writer.beginCompound();
            writer.name("id").value(MegaCells4110Profile.CRAFTING_MONITOR_BLOCK_ENTITY);
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

        private boolean failNextPosition;

        private RecordingTileModel() {
            super(1024);
        }

        @Override
        public RecordingTileModel setPositions(
                int face,
                float x1, float y1, float z1,
                float x2, float y2, float z2,
                float x3, float y3, float z3
        ) {
            if (failNextPosition) {
                failNextPosition = false;
                throw new IllegalStateException("injected M4/M5 crafting failure");
            }
            super.setPositions(face, x1, y1, z1, x2, y2, z2, x3, y3, z3);
            return this;
        }
    }

    private record Position(int x, int y, int z) {
    }

    private record Fixture(
            BlockNeighborhood neighborhood,
            M45Runtime runtime,
            M45CraftingRenderer renderer
    ) {
    }
}
