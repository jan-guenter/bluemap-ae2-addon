/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.model;

import java.util.Objects;

/** Exact M3b cell item identity and its selected occupied-slot model. */
public record ExtendedAeDriveCellDefinition(String itemId, String modelId) {

    public ExtendedAeDriveCellDefinition {
        Objects.requireNonNull(itemId, "itemId");
        Objects.requireNonNull(modelId, "modelId");
        DriveCellOwner owner = DriveCellOwner.fromItemId(itemId);
        String prefix = owner.namespace() + ":block/drive/";
        if (!hasModelPrefix(modelId, prefix)) {
            throw new IllegalArgumentException(
                    "Extended Drive cell model must match its exact owner namespace"
            );
        }
    }

    public DriveCellOwner owner() {
        return DriveCellOwner.fromItemId(itemId);
    }

    private static boolean hasModelPrefix(String model, String prefix) {
        return model.startsWith(prefix) && model.length() > prefix.length();
    }
}
