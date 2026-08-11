/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.adapter.bluemap522;

import com.flowpowered.math.vector.Vector3f;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.model.Element;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.model.Model;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.texture.Texture;
import de.bluecolored.bluemap.core.util.Key;
import io.github.janguenter.bluemap.ae2.model.DriveCellCatalog;
import io.github.janguenter.bluemap.ae2.model.ExtendedAeDriveCellCatalog;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class M3bExtendedAeDriveResourceModelsTest {

    @Test
    void locksEveryExtendedModelAndRequiredCoreCellModel() throws Exception {
        ResourcePack resourcePack = exactResources();

        assertEquals(13, M3bExtendedAeDriveResourceModels.requiredTextures().size());
        assertEquals(
                SetFixtures.EXTENDED_MODELS,
                M3bExtendedAeDriveResourceModels.expectedExtendedModelSignatures().keySet()
        );
        for (Map.Entry<String, String> expected
                : M3bExtendedAeDriveResourceModels
                        .expectedExtendedModelSignatures().entrySet()) {
            Model model = resourcePack.getModels().get(Key.parse(expected.getKey()));
            assertEquals(
                    expected.getValue(),
                    M3DriveResourceModels.semanticSignature(model),
                    expected.getKey()
            );
        }
        Model base = resourcePack.getModels().get(Key.parse(
                ExtendedAeDriveCellCatalog.BASE_MODEL
        ));
        assertEquals(18, base.getElements().length);
        assertEquals(58, java.util.Arrays.stream(base.getElements())
                .mapToInt(element -> element.getFaces().size())
                .sum());
        assertTrue(M3bExtendedAeDriveResourceModels.resourcesSupported(resourcePack));
        assertThrows(
                UnsupportedOperationException.class,
                () -> M3bExtendedAeDriveResourceModels.requiredTextures().clear()
        );
    }

    @Test
    void ignoresNativeBaseButRequiresEverySharedCellModel() throws Exception {
        ResourcePack withoutNativeBase = exactResources();
        withoutNativeBase.getModels().remove(Key.parse(DriveCellCatalog.BASE_MODEL));
        assertTrue(M3bExtendedAeDriveResourceModels.resourcesSupported(withoutNativeBase));

        ResourcePack withoutCell = exactResources();
        withoutCell.getModels().remove(Key.parse(DriveCellCatalog.GENERIC_CELL_MODEL));
        assertFalse(M3bExtendedAeDriveResourceModels.resourcesSupported(withoutCell));
    }

    @Test
    void rejectsGeometryUvTextureAndOpacityChanges() throws Exception {
        ResourcePack changedBase = exactResources();
        changedBase.getModels().put(
                Key.parse(ExtendedAeDriveCellCatalog.BASE_MODEL),
                new Model(new Element[0])
        );
        assertFalse(M3bExtendedAeDriveResourceModels.resourcesSupported(changedBase));

        ResourcePack changedCell = exactResources();
        Model canonical = changedCell.getModels().get(Key.parse(
                ExtendedAeDriveCellCatalog.INFINITY_WATER_MODEL
        ));
        Model geometryChanged = new Model(new Element(
                Vector3f.ZERO,
                new Vector3f(7, 2, 2),
                canonical.getElements()[0].getFaces()
        ));
        changedCell.getModels().put(
                Key.parse(ExtendedAeDriveCellCatalog.INFINITY_WATER_MODEL),
                geometryChanged
        );
        assertNotEquals(
                M3DriveResourceModels.semanticSignature(canonical),
                M3DriveResourceModels.semanticSignature(geometryChanged)
        );
        assertFalse(M3bExtendedAeDriveResourceModels.resourcesSupported(changedCell));

        ResourcePack missingTexture = exactResources();
        missingTexture.getTextures().remove(Key.parse(
                "extendedae:block/drive/infinity_cell"
        ));
        assertFalse(M3bExtendedAeDriveResourceModels.resourcesSupported(missingTexture));

        ResourcePack translucentLed = exactResources();
        putTexture(
                translucentLed,
                M3bExtendedAeDriveResourceModels.LED_TEXTURE,
                0x80FFFFFF
        );
        assertFalse(M3bExtendedAeDriveResourceModels.resourcesSupported(translucentLed));
    }

    static ResourcePack exactResources() throws IOException {
        ResourcePack resourcePack = M3DriveResourceModelsTest.exactResources();
        for (Key texture : M3bExtendedAeDriveResourceModels.requiredTextures()) {
            putTexture(resourcePack, texture, 0xFFFFFFFF);
        }
        ExactExtendedAeDriveModelFixtures.models().forEach(
                (model, value) -> resourcePack.getModels().put(Key.parse(model), value)
        );
        return resourcePack;
    }

    private static void putTexture(ResourcePack pack, Key key, int argb) throws IOException {
        BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        image.setRGB(0, 0, argb);
        pack.getTextures().put(key, Texture.from(key, image));
    }

    private static final class SetFixtures {
        private static final java.util.Set<String> EXTENDED_MODELS = java.util.Set.of(
                ExtendedAeDriveCellCatalog.BASE_MODEL,
                ExtendedAeDriveCellCatalog.INFINITY_WATER_MODEL,
                ExtendedAeDriveCellCatalog.INFINITY_COBBLESTONE_MODEL,
                ExtendedAeDriveCellCatalog.VOID_MODEL
        );

        private SetFixtures() {
        }
    }
}
