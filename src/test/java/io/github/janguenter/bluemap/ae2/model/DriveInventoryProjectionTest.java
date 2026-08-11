/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.model;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DriveInventoryProjectionTest {

    @Test
    void emptyProjectionHasExactlyTenCanonicalEmptySlots() {
        DriveInventoryProjection inventory = DriveInventoryProjection.empty();

        assertEquals(5, DriveInventoryProjection.ROWS);
        assertEquals(2, DriveInventoryProjection.COLUMNS);
        assertEquals(10, DriveInventoryProjection.SLOT_COUNT);
        assertEquals(10, inventory.slots().size());
        for (int slot = 0; slot < DriveInventoryProjection.SLOT_COUNT; slot++) {
            assertEquals(DriveInventoryProjection.Slot.empty(), inventory.slot(slot));
            assertTrue(inventory.slot(slot).isEmpty());
            assertEquals(0, inventory.slot(slot).count());
        }
    }

    @Test
    void rowMajorIndicesMatchTheExactFiveByTwoLayout() {
        int expected = 0;
        for (int row = 0; row < DriveInventoryProjection.ROWS; row++) {
            for (int column = 0; column < DriveInventoryProjection.COLUMNS; column++) {
                assertEquals(expected, DriveInventoryProjection.slotIndex(row, column));
                expected++;
            }
        }
    }

    @Test
    void withSlotReturnsAnIndependentImmutableProjection() {
        DriveInventoryProjection empty = DriveInventoryProjection.empty();
        DriveInventoryProjection.Slot cell = DriveInventoryProjection.Slot.occupied(
                "ae2:item_storage_cell_1k"
        );
        DriveInventoryProjection occupied = empty.withSlot(9, cell);

        assertTrue(empty.slot(9).isEmpty());
        assertEquals(cell, occupied.slot(9));
        assertFalse(occupied.slot(9).isEmpty());
        assertEquals(1, occupied.slot(9).count());
        assertNotSame(empty.slots(), occupied.slots());
        assertThrows(UnsupportedOperationException.class, occupied.slots()::clear);
    }

    @Test
    void constructorDefensivelyCopiesTheExactTenSlots() {
        ArrayList<DriveInventoryProjection.Slot> mutable = new ArrayList<>(
                Collections.nCopies(
                        DriveInventoryProjection.SLOT_COUNT,
                        DriveInventoryProjection.Slot.empty()
                )
        );
        DriveInventoryProjection inventory = new DriveInventoryProjection(mutable);

        mutable.set(0, DriveInventoryProjection.Slot.occupied("ae2:matter_cannon"));
        assertTrue(inventory.slot(0).isEmpty());
        assertThrows(UnsupportedOperationException.class, inventory.slots()::clear);
    }

    @Test
    void projectionAndIndexHelpersRejectNonTenSlotShapesAndBadIndices() {
        assertThrows(NullPointerException.class, () -> new DriveInventoryProjection(null));
        assertThrows(
                IllegalArgumentException.class,
                () -> new DriveInventoryProjection(List.of())
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> new DriveInventoryProjection(Collections.nCopies(11,
                        DriveInventoryProjection.Slot.empty()))
        );
        ArrayList<DriveInventoryProjection.Slot> containsNull = new ArrayList<>(
                Collections.nCopies(DriveInventoryProjection.SLOT_COUNT,
                        DriveInventoryProjection.Slot.empty())
        );
        containsNull.set(4, null);
        assertThrows(
                NullPointerException.class,
                () -> new DriveInventoryProjection(containsNull)
        );
        assertThrows(IllegalArgumentException.class, () -> DriveInventoryProjection.slotIndex(-1, 0));
        assertThrows(IllegalArgumentException.class, () -> DriveInventoryProjection.slotIndex(5, 0));
        assertThrows(IllegalArgumentException.class, () -> DriveInventoryProjection.slotIndex(0, -1));
        assertThrows(IllegalArgumentException.class, () -> DriveInventoryProjection.slotIndex(0, 2));
        assertThrows(IllegalArgumentException.class, () -> DriveInventoryProjection.empty().slot(-1));
        assertThrows(IllegalArgumentException.class, () -> DriveInventoryProjection.empty().slot(10));
        assertThrows(
                NullPointerException.class,
                () -> DriveInventoryProjection.empty().withSlot(0, null)
        );
    }

    @Test
    void rawSlotRetainsInvalidCountsForStrictDecoderClassification() {
        DriveInventoryProjection.Slot emptyWithCount = new DriveInventoryProjection.Slot(null, 1);
        DriveInventoryProjection.Slot occupiedWithCount =
                DriveInventoryProjection.Slot.occupied("ae2:item_storage_cell_1k", 2);

        assertTrue(emptyWithCount.isEmpty());
        assertEquals(1, emptyWithCount.count());
        assertFalse(occupiedWithCount.isEmpty());
        assertEquals(2, occupiedWithCount.count());
        assertThrows(
                NullPointerException.class,
                () -> DriveInventoryProjection.Slot.occupied(null)
        );
    }
}
