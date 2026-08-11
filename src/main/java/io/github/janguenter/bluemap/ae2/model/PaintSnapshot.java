/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Strict bounded projection of AE2's padded {@code dots} byte array. */
public record PaintSnapshot(List<PaintSplotch> splotches) {

    public static final int MAX_SPLOTCHES = 21;
    public static final int MAX_PERSISTED_BYTES = 256;

    public PaintSnapshot {
        splotches = List.copyOf(Objects.requireNonNull(splotches, "splotches"));
        if (splotches.isEmpty() || splotches.size() > MAX_SPLOTCHES) {
            throw new IllegalArgumentException("paint must contain between 1 and 21 splotches");
        }
    }

    /**
     * Decodes the exact two-byte records and accepts AE2's zero-filled Netty
     * backing-array tail. Non-zero trailing bytes fail closed.
     */
    public static PaintSnapshot decode(byte[] persisted) {
        Objects.requireNonNull(persisted, "persisted");
        if (persisted.length < 3 || persisted.length > MAX_PERSISTED_BYTES) {
            throw new IllegalArgumentException("paint payload has invalid length");
        }
        int count = persisted[0];
        if (count < 1 || count > MAX_SPLOTCHES) {
            throw new IllegalArgumentException("paint payload has invalid count");
        }
        int used = 1 + count * 2;
        if (persisted.length < used) {
            throw new IllegalArgumentException("paint payload is truncated");
        }
        if (persisted.length != used && persisted.length != MAX_PERSISTED_BYTES) {
            throw new IllegalArgumentException("paint payload has non-source padding length");
        }

        List<PaintSplotch> splotches = new ArrayList<>(count);
        for (int index = 0; index < count; index++) {
            int offset = 1 + index * 2;
            int position = persisted[offset];
            int encoded = persisted[offset + 1];
            int sideOrdinal = encoded & 0x07;
            int colorOrdinal = encoded >> 3 & 0x0f;
            if (sideOrdinal >= Direction6.values().length) {
                throw new IllegalArgumentException("paint payload has invalid side");
            }
            splotches.add(new PaintSplotch(
                    position,
                    Direction6.values()[sideOrdinal],
                    CableColor.values()[colorOrdinal],
                    (encoded >> 7 & 1) != 0
            ));
        }
        for (int index = used; index < persisted.length; index++) {
            if (persisted[index] != 0) {
                throw new IllegalArgumentException("paint payload has non-zero padding");
            }
        }
        return new PaintSnapshot(splotches);
    }

    public int expectedLightLevelProperty() {
        int lumen = 0;
        for (PaintSplotch splotch : splotches) {
            if (splotch.lumen() && ++lumen == 2) {
                return 2;
            }
        }
        return lumen;
    }
}
