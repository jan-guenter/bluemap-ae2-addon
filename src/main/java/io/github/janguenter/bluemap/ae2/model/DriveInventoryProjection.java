/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Immutable ten-slot projection retained from an AE2 drive inventory. */
public record DriveInventoryProjection(List<Slot> slots) {

    public static final int ROWS = 5;
    public static final int COLUMNS = 2;
    public static final int SLOT_COUNT = ROWS * COLUMNS;

    public DriveInventoryProjection {
        Objects.requireNonNull(slots, "slots");
        if (slots.size() != SLOT_COUNT) {
            throw new IllegalArgumentException("drive inventory must contain exactly 10 slots");
        }
        slots = List.copyOf(slots);
    }

    public static DriveInventoryProjection empty() {
        return new DriveInventoryProjection(
                java.util.Collections.nCopies(SLOT_COUNT, Slot.empty())
        );
    }

    public Slot slot(int index) {
        return slots.get(requireSlot(index));
    }

    public DriveInventoryProjection withSlot(int index, Slot slot) {
        Objects.requireNonNull(slot, "slot");
        ArrayList<Slot> updated = new ArrayList<>(slots);
        updated.set(requireSlot(index), slot);
        return new DriveInventoryProjection(updated);
    }

    public static int slotIndex(int row, int column) {
        if (row < 0 || row >= ROWS) {
            throw new IllegalArgumentException("drive row must be in [0, 4]");
        }
        if (column < 0 || column >= COLUMNS) {
            throw new IllegalArgumentException("drive column must be in [0, 1]");
        }
        return row * COLUMNS + column;
    }

    private static int requireSlot(int slot) {
        if (slot < 0 || slot >= SLOT_COUNT) {
            throw new IllegalArgumentException("drive slot must be in [0, 9]");
        }
        return slot;
    }

    /** Raw slot projection. A null item ID is the canonical empty-slot marker. */
    public record Slot(String itemId, int count) {

        public static Slot empty() {
            return new Slot(null, 0);
        }

        public static Slot occupied(String itemId) {
            return occupied(itemId, 1);
        }

        public static Slot occupied(String itemId, int count) {
            return new Slot(Objects.requireNonNull(itemId, "itemId"), count);
        }

        public boolean isEmpty() {
            return itemId == null;
        }
    }
}
