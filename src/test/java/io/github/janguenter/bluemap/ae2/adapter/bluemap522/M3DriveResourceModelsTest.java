/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.adapter.bluemap522;

import com.flowpowered.math.vector.Vector3f;
import com.flowpowered.math.vector.Vector4f;
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
import io.github.janguenter.bluemap.ae2.model.DriveCellCatalog;
import io.github.janguenter.bluemap.ae2.profile.Ae219217DriveProfile;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class M3DriveResourceModelsTest {

    @Test
    void locksOneCanonicalExactSignatureForEverySelectedDriveModel() throws Exception {
        ResourcePack resourcePack = exactResources();

        assertTrue(M3DriveResourceModels.texturesSupported(resourcePack));
        assertEquals(
                Ae219217DriveProfile.models(),
                M3DriveResourceModels.expectedModelSignatures().keySet()
        );
        assertTrue(M3DriveResourceModels.expectedModelSignatures().values().stream()
                .allMatch(signature -> signature.matches("[0-9a-f]{64}")));
        for (String model : Ae219217DriveProfile.models()) {
            assertEquals(
                    M3DriveResourceModels.expectedModelSignatures().get(model),
                    M3DriveResourceModels.semanticSignature(
                            resourcePack.getModels().get(Key.parse(model))
                    ),
                    model
            );
            assertTrue(M3DriveResourceModels.exactModel(
                    resourcePack.getModels().get(Key.parse(model)),
                    model
            ), model);
        }
        assertTrue(M3DriveResourceModels.resourcesSupported(resourcePack));
        assertThrows(
                UnsupportedOperationException.class,
                () -> M3DriveResourceModels.expectedModelSignatures().clear()
        );
    }

    @Test
    void rejectsMissingModelsAndNonOpaqueLedMaterial() throws Exception {
        ResourcePack missingModel = exactResources();
        missingModel.getModels().remove(Key.parse(DriveCellCatalog.GENERIC_CELL_MODEL));
        assertFalse(M3DriveResourceModels.resourcesSupported(missingModel));

        ResourcePack translucentLed = exactResources();
        putTexture(translucentLed, M3DriveResourceModels.LED_TEXTURE, 0x80FFFFFF);
        assertFalse(M3DriveResourceModels.texturesSupported(translucentLed));
        assertFalse(M3DriveResourceModels.resourcesSupported(translucentLed));
    }

    @Test
    void rejectsStructurallyChangedBaseAndCellModels() throws Exception {
        ResourcePack changedBase = exactResources();
        changedBase.getModels().put(
                Key.parse(DriveCellCatalog.BASE_MODEL),
                new Model(new Element[0])
        );
        assertFalse(M3DriveResourceModels.resourcesSupported(changedBase));

        ResourcePack changedCell = exactResources();
        changedCell.getModels().put(
                Key.parse(DriveCellCatalog.GENERIC_CELL_MODEL),
                new Model(
                        Map.of(),
                        new Element[]{new Element(
                                Vector3f.ZERO,
                                new Vector3f(7, 2, 2),
                                cellFaces()
                        )},
                        false
                )
        );
        assertFalse(M3DriveResourceModels.resourcesSupported(changedCell));

        Model canonical = exactCellModel();
        Model geometryChanged = new Model(
                Map.of(),
                new Element[]{new Element(
                        Vector3f.ZERO,
                        new Vector3f(7, 2, 2),
                        cellFaces()
                )},
                false
        );
        EnumMap<Direction, Face> uvChangedFaces = cellFaces();
        uvChangedFaces.put(
                Direction.NORTH,
                new Face(
                        new Vector4f(1, 2, 3, 4),
                        new TextureVariable(new ResourcePath<Texture>(
                                "ae2:block/drive/drive_cells"
                        )),
                        Direction.NORTH,
                        0,
                        -1
                )
        );
        Model uvChanged = new Model(
                Map.of(),
                new Element[]{new Element(
                        Vector3f.ZERO,
                        new Vector3f(6, 2, 2),
                        uvChangedFaces
                )},
                false
        );
        EnumMap<Direction, Face> cullChangedFaces = cellFaces();
        cullChangedFaces.put(
                Direction.NORTH,
                face("ae2:block/drive/drive_cells", null)
        );
        Model cullChanged = new Model(
                Map.of(),
                new Element[]{new Element(
                        Vector3f.ZERO,
                        new Vector3f(6, 2, 2),
                        cullChangedFaces
                )},
                false
        );
        assertNotEquals(
                M3DriveResourceModels.semanticSignature(canonical),
                M3DriveResourceModels.semanticSignature(geometryChanged)
        );
        assertNotEquals(
                M3DriveResourceModels.semanticSignature(canonical),
                M3DriveResourceModels.semanticSignature(uvChanged)
        );
        assertNotEquals(
                M3DriveResourceModels.semanticSignature(canonical),
                M3DriveResourceModels.semanticSignature(cullChanged)
        );
    }

    static ResourcePack exactResources() throws IOException {
        ResourcePack resourcePack = new ResourcePack(new PackVersion(34, 0));
        for (Key texture : M3DriveResourceModels.requiredTextures()) {
            putTexture(resourcePack, texture, 0xFFFFFFFF);
        }
        ExactDriveModelFixtures.models().forEach(
                (model, value) -> resourcePack.getModels().put(Key.parse(model), value)
        );
        return resourcePack;
    }

    private static Model exactCellModel() {
        return new Model(
                Map.of(),
                new Element[]{new Element(
                        Vector3f.ZERO,
                        new Vector3f(6, 2, 2),
                        cellFaces()
                )},
                false
        );
    }

    private static EnumMap<Direction, Face> cellFaces() {
        EnumMap<Direction, Face> faces = new EnumMap<>(Direction.class);
        for (Direction direction : List.of(Direction.NORTH, Direction.UP, Direction.DOWN)) {
            faces.put(
                    direction,
                    face("ae2:block/drive/drive_cells", Direction.NORTH)
            );
        }
        return faces;
    }

    private static Face face(String texture, Direction cullface) {
        return new Face(
                new Vector4f(0, 0, 16, 16),
                new TextureVariable(new ResourcePath<Texture>(texture)),
                cullface,
                0,
                -1
        );
    }

    private static void putTexture(ResourcePack pack, Key key, int argb) throws IOException {
        BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        image.setRGB(0, 0, argb);
        pack.getTextures().put(key, Texture.from(key, image));
    }
}
