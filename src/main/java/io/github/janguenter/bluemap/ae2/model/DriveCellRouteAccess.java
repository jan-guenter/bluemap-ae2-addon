/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.model;

import java.util.Objects;

/** Dynamic, independently fail-closed access to exact extension-owned cells. */
@FunctionalInterface
public interface DriveCellRouteAccess {

    DriveCellRouteAccess NONE = owner -> false;

    boolean isActive(DriveCellOwner owner);

    /** Data-only route lookup used by a frozen soft-dependent registration. */
    default boolean isActive(String routeId) {
        Objects.requireNonNull(routeId, "routeId");
        return false;
    }

    /** Route-local failure hook; the core-only implementation deliberately does nothing. */
    default void disable(DriveCellOwner owner) {
        Objects.requireNonNull(owner, "owner");
    }

    /** Route-local failure hook for a frozen soft-dependent registration. */
    default void disable(String routeId) {
        Objects.requireNonNull(routeId, "routeId");
    }

    /** Blocks routes whose renderer depends on the now-inactive native Drive. */
    default void blockIfNativeDriveInactive() {
        // Core-only callers have no optional routes to reconcile.
    }
}
