/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.model;

import java.util.Objects;

/** Exact AE2 19.2.17 cable families and cable-to-cable minimum-type rules. */
public enum CableFamily {

    GLASS("glass_cable", "glass", "glass", Variant.GLASS, Size.NORMAL),
    COVERED("covered_cable", "covered", "covered", Variant.COVERED, Size.NORMAL),
    SMART("smart_cable", "smart", "covered", Variant.SMART, Size.NORMAL),
    DENSE_COVERED(
            "covered_dense_cable",
            "dense_covered",
            "dense_smart",
            Variant.COVERED,
            Size.DENSE
    ),
    DENSE_SMART(
            "smart_dense_cable",
            "dense_smart",
            "dense_smart",
            Variant.SMART,
            Size.DENSE
    );

    private final String idSuffix;
    private final String connectionTextureFolder;
    private final String coreTextureFolder;
    private final Variant variant;
    private final Size size;

    CableFamily(
            String idSuffix,
            String connectionTextureFolder,
            String coreTextureFolder,
            Variant variant,
            Size size
    ) {
        this.idSuffix = idSuffix;
        this.connectionTextureFolder = connectionTextureFolder;
        this.coreTextureFolder = coreTextureFolder;
        this.variant = variant;
        this.size = size;
    }

    public String idSuffix() {
        return idSuffix;
    }

    public String connectionTextureFolder() {
        return connectionTextureFolder;
    }

    public String coreTextureFolder() {
        return coreTextureFolder;
    }

    public boolean isSmart() {
        return variant == Variant.SMART;
    }

    public boolean isDense() {
        return size == Size.DENSE;
    }

    /**
     * Mirrors AE2's independent minimum of cable variant and cable size.
     * The local cable then maps this effective type to its visible half-arm.
     */
    public static CableFamily minimum(CableFamily first, CableFamily second) {
        Objects.requireNonNull(first, "first");
        Objects.requireNonNull(second, "second");
        Variant variant = first.variant.ordinal() < second.variant.ordinal()
                ? first.variant : second.variant;
        Size size = first.size.ordinal() < second.size.ordinal()
                ? first.size : second.size;
        return from(variant, size);
    }

    private static CableFamily from(Variant variant, Size size) {
        if (size == Size.NORMAL) {
            return switch (variant) {
                case GLASS -> GLASS;
                case COVERED -> COVERED;
                case SMART -> SMART;
            };
        }
        return switch (variant) {
            case COVERED -> DENSE_COVERED;
            case SMART -> DENSE_SMART;
            case GLASS -> throw new IllegalArgumentException("AE2 has no dense glass cable");
        };
    }

    private enum Variant {
        GLASS,
        COVERED,
        SMART
    }

    private enum Size {
        NORMAL,
        DENSE
    }
}
