/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.model;

import java.util.Objects;

/** Persisted static rendering state for one supported AE2 face part. */
public record FacePartSnapshot(
        String id,
        int spin,
        Integer p2pFrequency,
        String cellItemId
) {

    public static final String TERMINAL = "ae2:terminal";

    public FacePartSnapshot {
        Objects.requireNonNull(id, "id");
        if (spin < 0 || spin > 3) {
            throw new IllegalArgumentException("part spin must be in [0, 3]");
        }
        if (p2pFrequency != null
                && (p2pFrequency < 0 || p2pFrequency > 0xffff)) {
            throw new IllegalArgumentException("P2P frequency must be in [0, 65535]");
        }
        if (cellItemId != null
                && !cellItemId.matches("[a-z0-9_.-]+:[a-z0-9/._-]+")) {
            throw new IllegalArgumentException("cell item ID must be a resource location");
        }
    }

    /** Compatibility constructor for native structural parts without a saved cell. */
    public FacePartSnapshot(String id, int spin, Integer p2pFrequency) {
        this(id, spin, p2pFrequency, null);
    }

    /** Compatibility constructor for the M2 terminal-only projection. */
    public FacePartSnapshot(String id, int spin) {
        this(id, spin, null, null);
    }

    public static FacePartSnapshot withoutSpin(String id) {
        return new FacePartSnapshot(id, 0, null, null);
    }

    public static FacePartSnapshot p2p(String id, short persistedFrequency) {
        return new FacePartSnapshot(id, 0, persistedFrequency & 0xffff, null);
    }
}
