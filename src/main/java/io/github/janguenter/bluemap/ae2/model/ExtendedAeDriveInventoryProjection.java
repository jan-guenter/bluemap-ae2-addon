/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Immutable twenty-slot projection retained from an ExtendedAE Extended Drive. */
public record ExtendedAeDriveInventoryProjection(List<Slot> slots) {

    public static final int ROWS = DriveInventoryProjection.ROWS;
    public static final int COLUMNS = DriveInventoryProjection.COLUMNS;
    public static final int SLOTS_PER_SIDE = ROWS * COLUMNS;
    public static final int SLOT_COUNT = SLOTS_PER_SIDE * Side.values().length;

    public ExtendedAeDriveInventoryProjection {
        Objects.requireNonNull(slots, "slots");
        if (slots.size() != SLOT_COUNT) {
            throw new IllegalArgumentException(
                    "Extended Drive inventory must contain exactly 20 slots"
            );
        }
        slots = List.copyOf(slots);
    }

    public static ExtendedAeDriveInventoryProjection empty() {
        return new ExtendedAeDriveInventoryProjection(
                java.util.Collections.nCopies(SLOT_COUNT, Slot.empty())
        );
    }

    public Slot slot(int index) {
        return slots.get(requireSlot(index));
    }

    public ExtendedAeDriveInventoryProjection withSlot(int index, Slot slot) {
        Objects.requireNonNull(slot, "slot");
        ArrayList<Slot> updated = new ArrayList<>(slots);
        updated.set(requireSlot(index), slot);
        return new ExtendedAeDriveInventoryProjection(updated);
    }

    public static int slotIndex(int row, int column, Side side) {
        Objects.requireNonNull(side, "side");
        if (row < 0 || row >= ROWS) {
            throw new IllegalArgumentException("Extended Drive row must be in [0, 4]");
        }
        if (column < 0 || column >= COLUMNS) {
            throw new IllegalArgumentException("Extended Drive column must be in [0, 1]");
        }
        return side.offset() + row * COLUMNS + column;
    }

    private static int requireSlot(int slot) {
        if (slot < 0 || slot >= SLOT_COUNT) {
            throw new IllegalArgumentException("Extended Drive slot must be in [0, 19]");
        }
        return slot;
    }

    public enum Side {
        FRONT(0),
        REAR(SLOTS_PER_SIDE);

        private final int offset;

        Side(int offset) {
            this.offset = offset;
        }

        int offset() {
            return offset;
        }
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
