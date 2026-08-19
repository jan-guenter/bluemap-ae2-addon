/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.adapter.bluemap522;

import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import io.github.janguenter.bluemap.ae2.model.DriveCellOwner;

/** Injectable route-local resource predicate; production uses the exact live gate. */
@FunctionalInterface
interface ExtensionDriveResourceValidator {

    boolean supported(
            ResourcePack resourcePack,
            String itemId,
            String modelId,
            DriveCellOwner owner
    );
}
