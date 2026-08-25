/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.model;

import java.util.Objects;

/** Exact accepted drive-cell item identity and its selected occupied-slot model. */
public record DriveCellDefinition(
        String itemId,
        String modelId,
        String externalRouteId
) {

    public DriveCellDefinition(String itemId, String modelId) {
        this(itemId, modelId, null);
    }

    public DriveCellDefinition {
        Objects.requireNonNull(itemId, "itemId");
        Objects.requireNonNull(modelId, "modelId");
        if (!itemId.matches("[a-z0-9_.-]+:[a-z0-9/._-]+")
                || itemId.length() > 256
                || !modelId.matches("[a-z0-9_.-]+:[a-z0-9/._-]+")
                || modelId.length() > 256) {
            throw new IllegalArgumentException("invalid Drive-cell item or model ID");
        }
        String namespace = itemId.substring(0, itemId.indexOf(':'));
        String prefix = namespace + ":block/drive/";
        if (!modelId.startsWith(prefix) || modelId.length() <= prefix.length()) {
            throw new IllegalArgumentException(
                    "drive-cell model must match its exact owner namespace"
            );
        }
        DriveCellOwner builtInOwner = null;
        try {
            builtInOwner = DriveCellOwner.fromItemId(itemId);
        } catch (IllegalArgumentException ignored) {
            // A public external registration may use another validated namespace.
        }
        if (externalRouteId == null) {
            if (builtInOwner == null) {
                throw new IllegalArgumentException("unsupported Drive-cell owner namespace");
            }
            DriveCellOwner owner = builtInOwner;
            if (owner == DriveCellOwner.EXTENDED_AE) {
                throw new IllegalArgumentException(
                        "native Drive cells cannot use the extendedae namespace"
                );
            }
        } else {
            if (!externalRouteId.matches("[a-z0-9][a-z0-9._-]*")
                    || externalRouteId.length() > 128) {
                throw new IllegalArgumentException("invalid external Drive route ID");
            }
            if (builtInOwner != null) {
                throw new IllegalArgumentException(
                        "external Drive cells cannot reuse a built-in owner namespace"
                );
            }
        }
    }

    public DriveCellOwner owner() {
        return externalRouteId == null
                ? DriveCellOwner.fromItemId(itemId) : DriveCellOwner.EXTERNAL;
    }

    public boolean requiresExtensionRoute() {
        return externalRouteId != null || owner().requiresExtensionRoute();
    }
}
