/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.model;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/** Locally inferred static/offline spatial-pylon display state. */
public record SpatialPylonSnapshot(Axis axis, AxisPosition axisPosition) {

    public SpatialPylonSnapshot {
        Objects.requireNonNull(axis, "axis");
        Objects.requireNonNull(axisPosition, "axisPosition");
    }

    /**
     * Applies the bounded local policy: isolated NONE; one neighbor START or
     * END by sign; two opposite neighbors MIDDLE; every other topology rejects.
     */
    public static Optional<SpatialPylonSnapshot> infer(Set<Direction6> neighbors) {
        Objects.requireNonNull(neighbors, "neighbors");
        Set<Direction6> copy = Set.copyOf(neighbors);
        if (copy.isEmpty()) {
            return Optional.of(new SpatialPylonSnapshot(Axis.X, AxisPosition.NONE));
        }
        if (copy.size() == 1) {
            Direction6 direction = copy.iterator().next();
            return Optional.of(new SpatialPylonSnapshot(
                    Axis.of(direction),
                    positive(direction) ? AxisPosition.START : AxisPosition.END
            ));
        }
        if (copy.size() == 2) {
            Direction6 first = copy.iterator().next();
            if (copy.contains(first.opposite())) {
                return Optional.of(new SpatialPylonSnapshot(
                        Axis.of(first),
                        AxisPosition.MIDDLE
                ));
            }
        }
        return Optional.empty();
    }

    public boolean formed() {
        return axisPosition != AxisPosition.NONE;
    }

    private static boolean positive(Direction6 direction) {
        return direction.stepX() > 0 || direction.stepY() > 0 || direction.stepZ() > 0;
    }

    public enum Axis {
        X,
        Y,
        Z;

        public static Axis of(Direction6 direction) {
            Objects.requireNonNull(direction, "direction");
            if (direction.stepX() != 0) {
                return X;
            }
            if (direction.stepY() != 0) {
                return Y;
            }
            return Z;
        }

        public boolean contains(Direction6 direction) {
            return this == of(direction);
        }
    }

    public enum AxisPosition {
        NONE,
        START,
        MIDDLE,
        END
    }
}
