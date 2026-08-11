/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.model;

import java.util.Objects;
import java.util.Set;

/** Immutable, client-independent role inferred from one settled native 3x3 bridge. */
public record QuantumBridgeSnapshot(
        Role role,
        Set<Direction6> connections,
        boolean waterlogged
) {

    public QuantumBridgeSnapshot {
        Objects.requireNonNull(role, "role");
        connections = Set.copyOf(Objects.requireNonNull(connections, "connections"));
        if (!validConnections(role, connections)) {
            throw new IllegalArgumentException("connections do not match quantum-bridge role");
        }
    }

    /** Live power is not persisted; the bounded world route never emits a light overlay. */
    public boolean emitsPoweredOverlay() {
        return false;
    }

    private static boolean validConnections(Role role, Set<Direction6> connections) {
        return switch (role) {
            case LINK -> connections.size() == 4 && oppositePairs(connections) == 2;
            case CORNER_RING -> connections.size() == 2
                    && oppositePairs(connections) == 0
                    && differentAxes(connections);
            case EDGE_RING -> connections.size() == 3
                    && oppositePairs(connections) == 1;
        };
    }

    private static int oppositePairs(Set<Direction6> directions) {
        int pairs = 0;
        for (Direction6 direction : directions) {
            if (directions.contains(direction.opposite())) {
                pairs++;
            }
        }
        return pairs / 2;
    }

    private static boolean differentAxes(Set<Direction6> directions) {
        Direction6 first = directions.iterator().next();
        return directions.stream().anyMatch(direction -> !sameAxis(first, direction));
    }

    private static boolean sameAxis(Direction6 first, Direction6 second) {
        return first == second || first == second.opposite();
    }

    public enum Role {
        LINK,
        CORNER_RING,
        EDGE_RING
    }
}
