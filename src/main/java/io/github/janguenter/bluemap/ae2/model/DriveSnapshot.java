/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.model;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/** Immutable resolved drive cells and exact AE2 full-block orientation. */
public record DriveSnapshot(
        List<Optional<DriveCellDefinition>> cells,
        Direction6 facing,
        int spin,
        PartOrientation orientation
) {

    public DriveSnapshot {
        Objects.requireNonNull(cells, "cells");
        Objects.requireNonNull(facing, "facing");
        Objects.requireNonNull(orientation, "orientation");
        if (cells.size() != DriveInventoryProjection.SLOT_COUNT) {
            throw new IllegalArgumentException("drive snapshot must contain exactly 10 cells");
        }
        cells = List.copyOf(cells);
        if (spin < 0 || spin > 3) {
            throw new IllegalArgumentException("drive spin must be in [0, 3]");
        }
        if (!orientation.equals(PartOrientation.forPart(facing, spin))) {
            throw new IllegalArgumentException("drive orientation does not match facing and spin");
        }
    }

    public Optional<DriveCellDefinition> cell(int slot) {
        if (slot < 0 || slot >= DriveInventoryProjection.SLOT_COUNT) {
            throw new IllegalArgumentException("drive slot must be in [0, 9]");
        }
        return cells.get(slot);
    }

    public boolean isOccupied(int slot) {
        return cell(slot).isPresent();
    }

    public long occupiedCount() {
        return cells.stream().filter(Optional::isPresent).count();
    }
}
