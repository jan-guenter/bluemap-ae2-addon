/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.model;

import java.util.Objects;

/** Exact owner of one closed Drive-cell registration. */
public enum DriveCellOwner {
    AE2("ae2", false),
    EXTENDED_AE("extendedae", false),
    APPLIED_FLUX("appflux", true),
    MEGA_CELLS("megacells", true),
    APPLIED_MEKANISTICS("appmek", true),
    EXTERNAL("", true);

    private final String namespace;
    private final boolean extensionRoute;

    DriveCellOwner(String namespace, boolean extensionRoute) {
        this.namespace = namespace;
        this.extensionRoute = extensionRoute;
    }

    public String namespace() {
        return namespace;
    }

    public boolean requiresExtensionRoute() {
        return extensionRoute;
    }

    public static DriveCellOwner fromItemId(String itemId) {
        Objects.requireNonNull(itemId, "itemId");
        if (!itemId.matches("[a-z0-9_.-]+:[a-z0-9/._-]+")) {
            throw new IllegalArgumentException("invalid Drive-cell item ID");
        }
        int separator = itemId.indexOf(':');
        String itemNamespace = itemId.substring(0, separator);
        for (DriveCellOwner owner : values()) {
            if (!owner.namespace.isEmpty() && owner.namespace.equals(itemNamespace)) {
                return owner;
            }
        }
        throw new IllegalArgumentException("unsupported Drive-cell owner namespace");
    }
}
