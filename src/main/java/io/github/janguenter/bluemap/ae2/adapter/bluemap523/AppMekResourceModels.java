/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.adapter.bluemap523;

import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.model.Model;
import de.bluecolored.bluemap.core.util.Key;
import io.github.janguenter.bluemap.ae2.model.DriveCellCatalog;
import io.github.janguenter.bluemap.ae2.model.DriveCellDefinition;
import io.github.janguenter.bluemap.ae2.model.DriveCellOwner;
import io.github.janguenter.bluemap.ae2.profile.appmek.AppMek163Profile;

import java.util.Set;

/** Live resolved-resource gate for exact AppMek Drive chassis resources. */
final class AppMekResourceModels {

    static final Key DRIVE_TEXTURE = Key.parse(AppMek163Profile.DRIVE_TEXTURE);

    private static final String DRIVE_TEXTURE_SIGNATURE =
            AppMek163Profile.DRIVE_TEXTURE_SEMANTIC_SHA256;

    private AppMekResourceModels() {
    }

    static boolean driveSupported(ResourcePack resourcePack) {
        return driveSupported(resourcePack, DRIVE_TEXTURE_SIGNATURE);
    }

    static boolean driveSupported(
            ResourcePack resourcePack,
            String expectedTextureSignature
    ) {
        try {
            if (!exactTexture(resourcePack, expectedTextureSignature)) {
                return false;
            }
            for (DriveCellDefinition definition : DriveCellCatalog.extensionDefinitions(
                    DriveCellOwner.APPLIED_MEKANISTICS
            )) {
                Model model = resourcePack.getModels().get(
                        M3DriveResourceModels.model(definition.modelId())
                );
                if (!AppMekDriveModelContract.supported(model, definition.modelId())) {
                    return false;
                }
            }
            return true;
        } catch (RuntimeException exception) {
            return false;
        }
    }

    static boolean driveModelSupported(
            ResourcePack resourcePack,
            Model model,
            String modelId
    ) {
        try {
            return exactTexture(resourcePack, DRIVE_TEXTURE_SIGNATURE)
                    && AppMekDriveModelContract.supported(model, modelId);
        } catch (RuntimeException exception) {
            return false;
        }
    }

    static boolean driveModelSupported(Model model, String modelId) {
        try {
            return AppMekDriveModelContract.supported(model, modelId);
        } catch (RuntimeException exception) {
            return false;
        }
    }

    static String driveTextureSignature(ResourcePack resourcePack) {
        return NativeStructuralSemanticResources.textureSignature(
                resourcePack,
                Set.of(DRIVE_TEXTURE)
        );
    }

    private static boolean exactTexture(
            ResourcePack resourcePack,
            String expectedSignature
    ) {
        return expectedSignature.equals(driveTextureSignature(resourcePack));
    }
}
