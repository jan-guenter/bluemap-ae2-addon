/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.model;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/** Immutable resolved Extended Drive cells and exact two-sided orientation. */
public record ExtendedAeDriveSnapshot(
        List<Optional<ExtendedAeDriveCellDefinition>> cells,
        Direction6 facing,
        int spin,
        PartOrientation frontOrientation,
        PartOrientation rearOrientation
) {

    public ExtendedAeDriveSnapshot {
        Objects.requireNonNull(cells, "cells");
        Objects.requireNonNull(facing, "facing");
        Objects.requireNonNull(frontOrientation, "frontOrientation");
        Objects.requireNonNull(rearOrientation, "rearOrientation");
        if (cells.size() != ExtendedAeDriveInventoryProjection.SLOT_COUNT) {
            throw new IllegalArgumentException(
                    "Extended Drive snapshot must contain exactly 20 cells"
            );
        }
        cells = List.copyOf(cells);
        if (spin < 0 || spin > 3) {
            throw new IllegalArgumentException("Extended Drive spin must be in [0, 3]");
        }
        if (!frontOrientation.equals(PartOrientation.forPart(facing, spin))) {
            throw new IllegalArgumentException(
                    "front orientation does not match facing and spin"
            );
        }
        if (!rearOrientation.equals(PartOrientation.forPart(facing.opposite(), spin))) {
            throw new IllegalArgumentException(
                    "rear orientation does not match opposite facing and spin"
            );
        }
    }

    public Optional<ExtendedAeDriveCellDefinition> cell(int slot) {
        if (slot < 0 || slot >= ExtendedAeDriveInventoryProjection.SLOT_COUNT) {
            throw new IllegalArgumentException("Extended Drive slot must be in [0, 19]");
        }
        return cells.get(slot);
    }

    public ExtendedAeDriveBayLayout.Bay bay(int slot) {
        return ExtendedAeDriveBayLayout.bay(slot, facing, spin);
    }

    public long occupiedCount() {
        return cells.stream().filter(Optional::isPresent).count();
    }
}
