/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.adapter.bluemap523;

import com.flowpowered.math.vector.Vector3f;
import com.flowpowered.math.vector.Vector4f;
import de.bluecolored.bluemap.core.map.TextureGallery;
import de.bluecolored.bluemap.core.map.hires.ArrayTileModel;
import de.bluecolored.bluemap.core.map.hires.RenderSettings;
import de.bluecolored.bluemap.core.map.mask.Mask;
import de.bluecolored.bluemap.core.resources.ResourcePath;
import de.bluecolored.bluemap.core.resources.pack.PackVersion;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.model.Element;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.model.Face;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.model.Model;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.model.TextureVariable;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.texture.Texture;
import de.bluecolored.bluemap.core.util.Direction;
import de.bluecolored.bluemap.core.util.Key;
import de.bluecolored.bluemap.core.util.math.MatrixM4f;
import de.bluecolored.bluemap.core.world.BlockEntity;
import de.bluecolored.bluemap.core.world.BlockState;
import de.bluecolored.bluemap.core.world.DimensionType;
import de.bluecolored.bluemap.core.world.LightData;
import de.bluecolored.bluemap.core.world.biome.Biome;
import de.bluecolored.bluemap.core.world.block.BlockAccess;
import de.bluecolored.bluemap.core.world.block.BlockNeighborhood;
import io.github.janguenter.bluemap.ae2.model.Direction6;
import io.github.janguenter.bluemap.ae2.model.FacePartSnapshot;
import io.github.janguenter.bluemap.ae2.model.MegaCellDockCellCatalog;
import io.github.janguenter.bluemap.ae2.model.MegaCellDockCellDefinition;
import io.github.janguenter.bluemap.ae2.model.MegaCellDockGeometry;
import io.github.janguenter.bluemap.ae2.profile.megacells.MegaCells4110Profile;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MegaCellDockRenderSupportTest {

    private static final double EPSILON = 1.0E-6;
    private static final String BULK_CELL = "megacells:bulk_item_cell";
    private static final Key CELL_TEXTURE =
            Key.parse("megacells:block/drive/cells/misc_cell");
    private static final Key LED_TEXTURE =
            Key.parse("megacells:block/drive/cells/standard_cell");

    @Test
    void resolvesOnlyEmptyOrClosedKnownMegaCells() {
        FacePartSnapshot empty = part(null);
        FacePartSnapshot occupied = part(BULK_CELL);
        FacePartSnapshot unknown = part("ae2:item_storage_cell_1k");

        assertEquals(
                MegaCellDockRenderSupport.Status.EMPTY,
                MegaCellDockRenderSupport.resolve(empty).status()
        );
        assertEquals(
                MegaCellDockCellCatalog.require(BULK_CELL),
                MegaCellDockRenderSupport.resolve(occupied).cell()
        );
        assertEquals(
                MegaCellDockRenderSupport.Status.UNSUPPORTED,
                MegaCellDockRenderSupport.resolve(unknown).status()
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> MegaCellDockRenderSupport.resolve(
                        new FacePartSnapshot("ae2:terminal", 0, null, null)
                )
        );
    }

    @Test
    void sourceAndBlueMapMatricesAreEquivalentForEveryPersistedOrientation() {
        List<MegaCellDockGeometry.Position> samples = List.of(
                new MegaCellDockGeometry.Position(0, 0, 0),
                new MegaCellDockGeometry.Position(6.0 / 16, 2.0 / 16, 2.0 / 16),
                new MegaCellDockGeometry.Position(4.0 / 16, 1.0 / 16, -0.001 / 16)
        );
        for (Direction6 side : Direction6.values()) {
            for (int spin = 0; spin < 4; spin++) {
                assertEquivalent(MegaCellDockGeometry.cellTransform(side, spin), samples);
                assertEquivalent(
                        MegaCellDockGeometry.secondLedTransform(side, spin),
                        samples
                );
            }
        }
    }

    @Test
    void preflightRejectsUnknownMissingAndStructurallyWrongCellResources()
            throws Exception {
        Fixture exact = fixture(true);
        assertTrue(exact.support().resourcesSupported(part(null)));
        assertTrue(exact.support().resourcesSupported(part(BULK_CELL)));
        assertFalse(exact.support().resourcesSupported(part("test:unknown_cell")));

        Fixture missingTexture = fixture(true);
        missingTexture.resourcePack().getTextures().remove(CELL_TEXTURE);
        assertFalse(missingTexture.support().resourcesSupported(part(BULK_CELL)));

        Fixture wrongModel = fixture(false);
        assertFalse(wrongModel.support().resourcesSupported(part(BULK_CELL)));
    }

    @Test
    void occupiedDockEmitsOneChassisAndExactlyTwoBlackOfflineLeds()
            throws Exception {
        Fixture fixture = fixture(true);
        RecordingTileModel model = new RecordingTileModel();

        fixture.support().renderDynamic(
                Direction6.NORTH,
                part(BULK_CELL),
                fixture.block(),
                model
        );

        assertEquals(26, model.size());
        assertEquals(20, model.blackFaces());

        RecordingTileModel empty = new RecordingTileModel();
        fixture.support().renderDynamic(
                Direction6.NORTH,
                part(null),
                fixture.block(),
                empty
        );
        assertEquals(0, empty.size());
        assertThrows(
                MegaCellDockRenderSupport.RouteFailure.class,
                () -> fixture.support().renderDynamic(
                        Direction6.NORTH,
                        part("ae2:item_storage_cell_1k"),
                        fixture.block(),
                        new RecordingTileModel()
                )
        );
    }

    private static void assertEquivalent(
            MegaCellDockGeometry.Transform transform,
            List<MegaCellDockGeometry.Position> samples
    ) {
        MatrixM4f source = MegaCellDockRenderSupport.sourceTransform(transform);
        var offset = MegaCellDockRenderSupport.postVariantTranslation(transform);
        var orientation = MegaCellDockRenderSupport.orientationFor(transform.orientation());
        MatrixM4f variant = new de.bluecolored.bluemap.core.resources.pack.resourcepack
                .blockstate.Variant(
                        ResourcePack.MISSING_BLOCK_MODEL,
                        orientation.x(),
                        orientation.y(),
                        orientation.z()
                ).getTransformMatrix();
        for (MegaCellDockGeometry.Position sample : samples) {
            var expected = apply(source, sample);
            var centered = apply(variant, sample);
            assertPosition(
                    expected,
                    centered.x() + offset.x(),
                    centered.y() + offset.y(),
                    centered.z() + offset.z()
            );
        }
    }

    private static MegaCellDockGeometry.Position apply(
            MatrixM4f matrix,
            MegaCellDockGeometry.Position point
    ) {
        return new MegaCellDockGeometry.Position(
                matrix.m00 * point.x() + matrix.m01 * point.y()
                        + matrix.m02 * point.z() + matrix.m03,
                matrix.m10 * point.x() + matrix.m11 * point.y()
                        + matrix.m12 * point.z() + matrix.m13,
                matrix.m20 * point.x() + matrix.m21 * point.y()
                        + matrix.m22 * point.z() + matrix.m23
        );
    }

    private static void assertPosition(
            MegaCellDockGeometry.Position actual,
            double x,
            double y,
            double z
    ) {
        assertEquals(x, actual.x(), EPSILON);
        assertEquals(y, actual.y(), EPSILON);
        assertEquals(z, actual.z(), EPSILON);
    }

    private static FacePartSnapshot part(String cellItemId) {
        return new FacePartSnapshot(
                MegaCells4110Profile.CELL_DOCK_PART,
                0,
                null,
                cellItemId
        );
    }

    private static Fixture fixture(boolean exactModel) throws IOException {
        ResourcePack resourcePack = new ResourcePack(new PackVersion(34, 0));
        putTexture(resourcePack, CELL_TEXTURE);
        putTexture(resourcePack, LED_TEXTURE);
        MegaCellDockCellDefinition cell = MegaCellDockCellCatalog.require(BULK_CELL);
        resourcePack.getModels().put(
                Key.parse(cell.modelId()),
                exactModel ? miscCellModel() : new Model(new Element[0])
        );
        TextureGallery gallery = new TextureGallery();
        gallery.put(resourcePack.getTextures());
        MegaCellDockRenderSupport support = new MegaCellDockRenderSupport(
                resourcePack,
                gallery,
                TEST_SETTINGS
        );
        BlockNeighborhood block = new BlockNeighborhood(
                new AirBlockAccess(),
                resourcePack,
                TEST_SETTINGS,
                DimensionType.OVERWORLD
        );
        block.set(0, 64, 0);
        return new Fixture(resourcePack, support, block);
    }

    private static Model miscCellModel() {
        EnumMap<Direction, Face> faces = new EnumMap<>(Direction.class);
        for (Direction direction : List.of(Direction.DOWN, Direction.NORTH, Direction.UP)) {
            faces.put(direction, new Face(
                    new Vector4f(0, 0, 6, 2),
                    new TextureVariable(new ResourcePath<Texture>(CELL_TEXTURE)),
                    Direction.NORTH,
                    0,
                    -1
            ));
        }
        return new Model(
                Map.of(),
                new Element[]{new Element(
                        Vector3f.ZERO,
                        new Vector3f(6, 2, 2),
                        faces
                )},
                false
        );
    }

    private static void putTexture(ResourcePack resourcePack, Key key) throws IOException {
        BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        image.setRGB(0, 0, 0xFFFFFFFF);
        resourcePack.getTextures().put(key, Texture.from(key, image));
    }

    private static final RenderSettings TEST_SETTINGS = new RenderSettings() {
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

    private static final class AirBlockAccess implements BlockAccess {

        private int x;
        private int y;
        private int z;

        @Override
        public void set(int newX, int newY, int newZ) {
            x = newX;
            y = newY;
            z = newZ;
        }

        @Override
        public BlockAccess copy() {
            AirBlockAccess copy = new AirBlockAccess();
            copy.set(x, y, z);
            return copy;
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
            return BlockState.AIR;
        }

        @Override
        public LightData getLightData() {
            return new LightData(15, 0);
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

        private int blackFaces;

        private RecordingTileModel() {
            super(64);
        }

        @Override
        public RecordingTileModel setColor(int face, float red, float green, float blue) {
            if (red == 0F && green == 0F && blue == 0F) {
                blackFaces++;
            }
            super.setColor(face, red, green, blue);
            return this;
        }

        int blackFaces() {
            return blackFaces;
        }
    }

    private record Fixture(
            ResourcePack resourcePack,
            MegaCellDockRenderSupport support,
            BlockNeighborhood block
    ) {
    }
}
