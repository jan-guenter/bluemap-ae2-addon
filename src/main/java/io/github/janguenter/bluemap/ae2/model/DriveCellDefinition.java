/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.model;

import java.util.Objects;

/** Exact accepted drive-cell item identity and its selected occupied-slot model. */
public record DriveCellDefinition(String itemId, String modelId) {

    public DriveCellDefinition {
        Objects.requireNonNull(itemId, "itemId");
        Objects.requireNonNull(modelId, "modelId");
        DriveCellOwner owner = DriveCellOwner.fromItemId(itemId);
        if (owner == DriveCellOwner.EXTENDED_AE) {
            throw new IllegalArgumentException(
                    "native Drive cells cannot use the extendedae namespace"
            );
        }
        String prefix = owner.namespace() + ":block/drive/";
        if (!modelId.startsWith(prefix) || modelId.length() <= prefix.length()) {
            throw new IllegalArgumentException(
                    "drive-cell model must match its exact owner namespace"
            );
        }
    }

    public DriveCellOwner owner() {
        return DriveCellOwner.fromItemId(itemId);
    }
}
