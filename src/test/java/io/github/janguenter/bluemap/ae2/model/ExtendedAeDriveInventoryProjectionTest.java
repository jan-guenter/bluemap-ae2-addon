/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.model;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExtendedAeDriveInventoryProjectionTest {

    @Test
    void exactLayoutContainsTenFrontThenTenRearSlots() {
        ExtendedAeDriveInventoryProjection inventory =
                ExtendedAeDriveInventoryProjection.empty();

        assertEquals(20, ExtendedAeDriveInventoryProjection.SLOT_COUNT);
        assertEquals(10, ExtendedAeDriveInventoryProjection.SLOTS_PER_SIDE);
        for (int slot = 0; slot < ExtendedAeDriveInventoryProjection.SLOT_COUNT; slot++) {
            assertTrue(inventory.slot(slot).isEmpty());
        }
        for (int row = 0; row < ExtendedAeDriveInventoryProjection.ROWS; row++) {
            for (int column = 0;
                 column < ExtendedAeDriveInventoryProjection.COLUMNS;
                 column++) {
                int local = row * ExtendedAeDriveInventoryProjection.COLUMNS + column;
                assertEquals(
                        local,
                        ExtendedAeDriveInventoryProjection.slotIndex(
                                row,
                                column,
                                ExtendedAeDriveInventoryProjection.Side.FRONT
                        )
                );
                assertEquals(
                        local + 10,
                        ExtendedAeDriveInventoryProjection.slotIndex(
                                row,
                                column,
                                ExtendedAeDriveInventoryProjection.Side.REAR
                        )
                );
            }
        }
    }

    @Test
    void immutableProjectionAndIndexBoundsFailClosed() {
        ExtendedAeDriveInventoryProjection updated =
                ExtendedAeDriveInventoryProjection.empty().withSlot(
                        19,
                        ExtendedAeDriveInventoryProjection.Slot.occupied(
                                "extendedae:void_cell"
                        )
                );

        assertEquals("extendedae:void_cell", updated.slot(19).itemId());
        assertThrows(UnsupportedOperationException.class, updated.slots()::clear);
        assertThrows(
                IllegalArgumentException.class,
                () -> new ExtendedAeDriveInventoryProjection(List.of())
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> new ExtendedAeDriveInventoryProjection(Collections.nCopies(
                        21,
                        ExtendedAeDriveInventoryProjection.Slot.empty()
                ))
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> ExtendedAeDriveInventoryProjection.empty().slot(20)
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> ExtendedAeDriveInventoryProjection.slotIndex(
                        5,
                        0,
                        ExtendedAeDriveInventoryProjection.Side.FRONT
                )
        );
    }
}
