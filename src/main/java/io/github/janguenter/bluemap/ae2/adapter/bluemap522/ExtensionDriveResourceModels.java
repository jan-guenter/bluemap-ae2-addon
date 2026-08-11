/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.adapter.bluemap522;

import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.model.Model;
import io.github.janguenter.bluemap.ae2.model.DriveCellDefinition;
import io.github.janguenter.bluemap.ae2.model.DriveCellOwner;
import io.github.janguenter.bluemap.ae2.model.ExtendedAeDriveCellDefinition;
import io.github.janguenter.bluemap.ae2.model.MegaCellDockCellCatalog;

/** Route-local structural check for exact AppliedFlux and MEGA Cells chassis models. */
final class ExtensionDriveResourceModels {

    private ExtensionDriveResourceModels() {
    }

    static boolean supported(ResourcePack resourcePack, DriveCellDefinition definition) {
        return supported(
                resourcePack,
                definition.itemId(),
                definition.modelId(),
                definition.owner()
        );
    }

    static boolean supported(
            ResourcePack resourcePack,
            ExtendedAeDriveCellDefinition definition
    ) {
        return supported(
                resourcePack,
                definition.itemId(),
                definition.modelId(),
                definition.owner()
        );
    }

    private static boolean supported(
            ResourcePack resourcePack,
            String itemId,
            String modelId,
            DriveCellOwner owner
    ) {
        if (resourcePack == null || !owner.requiresExtensionRoute()) {
            return false;
        }
        Model model = resourcePack.getModels().get(M3DriveResourceModels.model(modelId));
        if (model == null || model.getParent() != null || model.getElements() == null
                || model.isAmbientocclusion()) {
            return false;
        }
        return switch (owner) {
            case APPLIED_FLUX -> model.getElements().length == 1;
            case MEGA_CELLS -> MegaCellDockCellCatalog.require(itemId)
                    .chassisKind().nominalTriangles() == model.getElements().length * 6;
            case AE2, EXTENDED_AE -> false;
        };
    }
}
