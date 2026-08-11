/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.model;

import java.util.Objects;

/** One exact AE2 19.2.17 persisted paint record. */
public record PaintSplotch(
        int signedPosition,
        Direction6 backingSide,
        CableColor color,
        boolean lumen
) {

    public PaintSplotch {
        if (signedPosition < Byte.MIN_VALUE || signedPosition > Byte.MAX_VALUE) {
            throw new IllegalArgumentException("signedPosition must be a byte");
        }
        Objects.requireNonNull(backingSide, "backingSide");
        Objects.requireNonNull(color, "color");
        if (color == CableColor.TRANSPARENT) {
            throw new IllegalArgumentException("paint cannot use transparent AEColor");
        }
    }

    public float x() {
        return (signedPosition & 0x0f) / 15F;
    }

    public float y() {
        return (signedPosition >> 4 & 0x0f) / 15F;
    }

    public int encodedValue() {
        return backingSide.ordinal()
                | color.ordinal() << 3
                | (lumen ? 0x80 : 0);
    }

    /** Mirrors AE2's signed-position plus unsigned reconstructed value seed. */
    public int seed() {
        return Math.abs(signedPosition + encodedValue());
    }

    public int textureIndex() {
        return seed() % 3;
    }

    public Direction6 visibleFace() {
        return backingSide.opposite();
    }

    public int rgb() {
        return lumen ? color.brightRgb() : color.mediumRgb();
    }
}
