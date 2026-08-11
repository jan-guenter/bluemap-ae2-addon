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

    /** Route-local failure hook; the core-only implementation deliberately does nothing. */
    default void disable(DriveCellOwner owner) {
        Objects.requireNonNull(owner, "owner");
    }
}
