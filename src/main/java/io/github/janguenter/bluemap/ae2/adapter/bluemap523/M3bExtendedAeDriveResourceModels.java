/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.adapter.bluemap523;

import de.bluecolored.bluemap.core.resources.ResourcePath;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.model.Model;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.texture.Texture;
import de.bluecolored.bluemap.core.util.Key;
import io.github.janguenter.bluemap.ae2.model.DriveCellCatalog;
import io.github.janguenter.bluemap.ae2.model.ExtendedAeDriveCellCatalog;
import io.github.janguenter.bluemap.ae2.profile.extendedae.ExtendedAe2233Profile;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/** Structural gates for the exact AE2 19.2.17 plus ExtendedAE 2.2.33 M3b models. */
final class M3bExtendedAeDriveResourceModels {

    static final ResourcePath<Model> DRIVE_BASE = model(
            ExtendedAeDriveCellCatalog.BASE_MODEL
    );
    static final Key LED_TEXTURE = Key.parse(ExtendedAe2233Profile.LED_TEXTURE);

    private static final Set<String> CORE_CELL_MODELS = buildCoreCellModels();
    private static final Set<Key> REQUIRED_TEXTURES = Set.of(
            Key.parse("ae2:block/generics/bottom"),
            Key.parse("ae2:block/generics/top"),
            Key.parse("ae2:block/drive/drive_front"),
            Key.parse("ae2:block/drive/drive_inside_top"),
            Key.parse("ae2:block/drive/drive_cells"),
            Key.parse("extendedae:block/generics/front"),
            Key.parse("extendedae:block/generics/side"),
            Key.parse("extendedae:block/extended_drive/ex_drive_bottom"),
            Key.parse("extendedae:block/extended_drive/ex_drive_side"),
            Key.parse("extendedae:block/extended_drive/ex_drive_top"),
            Key.parse("extendedae:block/extended_drive/drive_inside"),
            Key.parse("extendedae:block/drive/infinity_cell"),
            Key.parse("extendedae:block/drive/void_cell")
    );
    // Independently derived from the pinned ExtendedAE JAR JSON using the same
    // post-parent semantic fields signed by M3DriveResourceModels.
    private static final Map<String, String> EXPECTED_EXTENDED_MODEL_SIGNATURES = Map.of(
            ExtendedAeDriveCellCatalog.BASE_MODEL,
            "c018510d8f114e8043367d97c33a638570847498a31ae18b2606ddb5eb6a42ec",
            ExtendedAeDriveCellCatalog.INFINITY_WATER_MODEL,
            "773942565e562f6c543f4de6b37b616044c8ab37c48c9a2c936a4f1b6b067ddb",
            ExtendedAeDriveCellCatalog.INFINITY_COBBLESTONE_MODEL,
            "74207c4535d09caa833f3aa4ef0cd86a44b51db067f3889888c11a8000da0662",
            ExtendedAeDriveCellCatalog.VOID_MODEL,
            "00f6834bc29220dc2897fbea8e1e99fc83b06af0cdf9540e021d0736115a5b55"
    );

    private M3bExtendedAeDriveResourceModels() {
    }

    static ResourcePath<Model> model(String path) {
        return new ResourcePath<>(path);
    }

    static Set<Key> requiredTextures() {
        return REQUIRED_TEXTURES;
    }

    static Map<String, String> expectedExtendedModelSignatures() {
        return EXPECTED_EXTENDED_MODEL_SIGNATURES;
    }

    static boolean resourcesSupported(ResourcePack resourcePack) {
        if (resourcePack == null || !texturesSupported(resourcePack)) {
            return false;
        }
        for (String modelPath : CORE_CELL_MODELS) {
            Model model = resourcePack.getModels().get(model(modelPath));
            if (!M3DriveResourceModels.exactModel(model, modelPath)) {
                return false;
            }
        }
        for (Map.Entry<String, String> expected
                : EXPECTED_EXTENDED_MODEL_SIGNATURES.entrySet()) {
            Model model = resourcePack.getModels().get(model(expected.getKey()));
            if (!exactExtendedModel(model, expected.getValue())) {
                return false;
            }
        }
        return true;
    }

    static boolean texturesSupported(ResourcePack resourcePack) {
        for (Key key : REQUIRED_TEXTURES) {
            if (ResourcePack.MISSING_TEXTURE.equals(key)
                    || resourcePack.getTextures().get(key) == null) {
                return false;
            }
        }
        Texture led = resourcePack.getTextures().get(LED_TEXTURE);
        return led != null
                && !led.isHalfTransparent()
                && led.getAnimation() == null
                && led.getColorStraight().a >= 1F;
    }

    private static boolean exactExtendedModel(Model model, String expectedSignature) {
        return model != null
                && model.getParent() == null
                && model.getElements() != null
                && expectedSignature.equals(M3DriveResourceModels.semanticSignature(model));
    }

    private static Set<String> buildCoreCellModels() {
        Set<String> models = new LinkedHashSet<>();
        models.add(DriveCellCatalog.EMPTY_CELL_MODEL);
        models.addAll(DriveCellCatalog.occupiedModels());
        return Set.copyOf(models);
    }
}
