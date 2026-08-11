/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.model;

import java.util.Objects;

/** Exact AE2 19.2.17 {@code BlockOrientation} angles for a face part. */
public record PartOrientation(float x, float y, float z) {

    private static final PartOrientation[][] ORIENTATIONS = build();

    public static PartOrientation forPart(Direction6 face, int spin) {
        Objects.requireNonNull(face, "face");
        if (spin < 0 || spin > 3) {
            throw new IllegalArgumentException("part spin must be in [0, 3]");
        }
        return ORIENTATIONS[face.ordinal()][spin];
    }

    private static PartOrientation[][] build() {
        return new PartOrientation[][]{
                row(90, 0, 0, 270, 180, 90),
                row(270, 0, 180, 90, 0, 270),
                row(0, 0, 0, 270, 180, 90),
                row(0, 180, 0, 90, 180, 270),
                row(0, 270, 0, 270, 180, 90),
                row(0, 90, 0, 270, 180, 90)
        };
    }

    private static PartOrientation[] row(
            float x,
            float y,
            float spin0,
            float spin1,
            float spin2,
            float spin3
    ) {
        return new PartOrientation[]{
                new PartOrientation(x, y, spin0),
                new PartOrientation(x, y, spin1),
                new PartOrientation(x, y, spin2),
                new PartOrientation(x, y, spin3)
        };
    }
}
