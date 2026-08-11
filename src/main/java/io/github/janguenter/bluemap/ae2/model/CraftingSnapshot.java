/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.model;

import java.util.Objects;
import java.util.Set;

/** Immutable persisted state and direct-neighbor topology for one formed block. */
public record CraftingSnapshot(
        CraftingBlockKind kind,
        boolean powered,
        Direction6 facing,
        int spin,
        CableColor paintedColor,
        Set<Direction6> connections
) {

    public CraftingSnapshot {
        Objects.requireNonNull(kind, "kind");
        Objects.requireNonNull(facing, "facing");
        Objects.requireNonNull(paintedColor, "paintedColor");
        connections = Set.copyOf(Objects.requireNonNull(connections, "connections"));
        if (spin < 0 || spin > 3) {
            throw new IllegalArgumentException("spin must be in [0, 3]");
        }
    }

    public boolean isConnected(Direction6 direction) {
        return connections.contains(Objects.requireNonNull(direction, "direction"));
    }
}
