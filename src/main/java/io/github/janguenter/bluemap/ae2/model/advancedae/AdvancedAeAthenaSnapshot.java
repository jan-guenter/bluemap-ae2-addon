/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.model.advancedae;

import io.github.janguenter.bluemap.ae2.model.Direction6;
import io.github.janguenter.bluemap.ae2.profile.advancedae.AdvancedAe1612Catalog;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/** Exact same-BlockState neighborhood for one Advanced AE quantum-alloy CTM block. */
public record AdvancedAeAthenaSnapshot(String blockId, Set<Offset> matchingNeighbors) {

    public static final String WHOLE_BLOCK_STATE_IDENTITY_POLICY =
            "neighbor-BlockState-equals-center-BlockState";
    public static final String MISSING_OR_MALFORMED_NEIGHBOR_POLICY =
            "not-matching-no-connection";

    public AdvancedAeAthenaSnapshot {
        if (!AdvancedAe1612Catalog.QUANTUM_ALLOY_BLOCK.equals(blockId)) {
            throw new IllegalArgumentException("unsupported Athena CTM block");
        }
        matchingNeighbors = Set.copyOf(Objects.requireNonNull(
                matchingNeighbors,
                "matchingNeighbors"
        ));
    }

    public boolean matches(Offset offset) {
        return matchingNeighbors.contains(Objects.requireNonNull(offset, "offset"));
    }

    /**
     * Builds the already-compared whole-state match set. Only Boolean.TRUE connects;
     * missing and malformed/null observations are intentionally false.
     */
    public static AdvancedAeAthenaSnapshot observedWholeStateMatches(
            String blockId,
            Map<Offset, Boolean> wholeStateMatches
    ) {
        Objects.requireNonNull(wholeStateMatches, "wholeStateMatches");
        Set<Offset> matching = new LinkedHashSet<>();
        wholeStateMatches.forEach((offset, matches) -> {
            if (offset != null && Boolean.TRUE.equals(matches)) {
                matching.add(offset);
            }
        });
        return new AdvancedAeAthenaSnapshot(blockId, matching);
    }

    public boolean faceBlocked(Direction6 face) {
        Objects.requireNonNull(face, "face");
        return matches(new Offset(face.stepX(), face.stepY(), face.stepZ()));
    }

    /** One non-origin cell in the face culling plus planar CTM neighborhood. */
    public record Offset(int x, int y, int z) {

        public Offset {
            if (x < -1 || x > 1 || y < -1 || y > 1 || z < -1 || z > 1
                    || x == 0 && y == 0 && z == 0) {
                throw new IllegalArgumentException("Athena offset must be a non-origin local cell");
            }
        }

        public Offset plus(Offset other) {
            Objects.requireNonNull(other, "other");
            return new Offset(x + other.x, y + other.y, z + other.z);
        }
    }
}
