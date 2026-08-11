/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.model;

import java.util.Objects;
import java.util.Optional;

/** Persisted, static Cell Dock projection: installed side, spin and optional saved cell. */
public record MegaCellDockSnapshot(
        Direction6 side,
        int spin,
        Optional<MegaCellDockCellDefinition> cell
) {

    public MegaCellDockSnapshot {
        Objects.requireNonNull(side, "side");
        cell = Objects.requireNonNull(cell, "cell");
        if (spin < 0 || spin > 3) {
            throw new IllegalArgumentException("Cell Dock spin must be in [0, 3]");
        }
    }

    public static MegaCellDockSnapshot empty(Direction6 side, int spin) {
        return new MegaCellDockSnapshot(side, spin, Optional.empty());
    }

    public static MegaCellDockSnapshot occupied(
            Direction6 side,
            int spin,
            MegaCellDockCellDefinition cell
    ) {
        return new MegaCellDockSnapshot(side, spin, Optional.of(cell));
    }

    public boolean isOccupied() {
        return cell.isPresent();
    }

    public PartOrientation bodyOrientation() {
        return PartOrientation.forPart(side, spin);
    }
}
