/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.model;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DriveDecoderTest {

    private final DriveDecoder decoder = new DriveDecoder();

    @Test
    void acceptsEveryExactCatalogItemInEverySlotPosition() {
        int definitionIndex = 0;
        for (DriveCellDefinition definition : DriveCellCatalog.definitions()) {
            int slot = definitionIndex % DriveInventoryProjection.SLOT_COUNT;
            DriveInventoryProjection inventory = DriveInventoryProjection.empty()
                    .withSlot(slot, DriveInventoryProjection.Slot.occupied(definition.itemId()));

            DriveDecodeResult result = decoder.decode(inventory, Direction6.NORTH, 0);

            assertTrue(result.isSupported(), definition.itemId());
            DriveSnapshot snapshot = result.supportedSnapshot().orElseThrow();
            assertEquals(definition, snapshot.cell(slot).orElseThrow());
            assertEquals(1, snapshot.occupiedCount());
            definitionIndex++;
        }
    }

    @Test
    void resolvesAFullMixedDriveWithoutMutatingItsInput() {
        DriveInventoryProjection inventory = DriveInventoryProjection.empty();
        for (int slot = 0; slot < DriveInventoryProjection.SLOT_COUNT; slot++) {
            DriveCellDefinition definition = DriveCellCatalog.definitions().get(slot * 2);
            inventory = inventory.withSlot(
                    slot,
                    DriveInventoryProjection.Slot.occupied(definition.itemId())
            );
        }

        DriveSnapshot snapshot = decoder.decode(inventory, Direction6.WEST, 3)
                .supportedSnapshot().orElseThrow();

        assertEquals(10, snapshot.occupiedCount());
        for (int slot = 0; slot < DriveInventoryProjection.SLOT_COUNT; slot++) {
            assertEquals(
                    DriveCellCatalog.definitions().get(slot * 2),
                    snapshot.cell(slot).orElseThrow()
            );
            assertEquals(1, inventory.slot(slot).count());
        }
    }

    @Test
    void acceptsTheExactTwentyFourFacingAndSpinOrientations() {
        for (Direction6 facing : Direction6.values()) {
            for (int spin = 0; spin < 4; spin++) {
                DriveDecodeResult result = decoder.decode(
                        DriveInventoryProjection.empty(),
                        facing,
                        spin
                );

                DriveSnapshot snapshot = result.supportedSnapshot().orElseThrow();
                assertEquals(facing, snapshot.facing(), facing + " spin " + spin);
                assertEquals(spin, snapshot.spin(), facing + " spin " + spin);
                assertEquals(
                        PartOrientation.forPart(facing, spin),
                        snapshot.orientation(),
                        facing + " spin " + spin
                );
            }
        }
    }

    @Test
    void enforcesCanonicalEmptyAndExactOneCountForOccupiedSlots() {
        for (int invalidCount : List.of(Integer.MIN_VALUE, -1, 0, 2, 64, Integer.MAX_VALUE)) {
            assertStatus(
                    DriveInventoryProjection.empty().withSlot(
                            0,
                            DriveInventoryProjection.Slot.occupied(
                                    "ae2:item_storage_cell_1k",
                                    invalidCount
                            )
                    ),
                    Direction6.NORTH,
                    0,
                    DriveDecodeResult.Status.INVALID_CELL_COUNT
            );
        }
        for (int invalidCount : List.of(Integer.MIN_VALUE, -1, 1, 2, Integer.MAX_VALUE)) {
            assertStatus(
                    DriveInventoryProjection.empty().withSlot(
                            9,
                            new DriveInventoryProjection.Slot(null, invalidCount)
                    ),
                    Direction6.NORTH,
                    0,
                    DriveDecodeResult.Status.INVALID_CELL_COUNT
            );
        }
    }

    @Test
    void rejectsMalformedResourceLocationsBeforeCatalogLookup() {
        for (String invalidId : List.of(
                "",
                "ae2",
                "ae2:",
                ":cell",
                "AE2:item_storage_cell_1k",
                "ae2:Item_storage_cell_1k",
                "ae2:item storage cell",
                "ae2:item?cell"
        )) {
            assertStatus(
                    inventoryWith(invalidId),
                    Direction6.NORTH,
                    0,
                    DriveDecodeResult.Status.INVALID_CELL_ID
            );
        }
    }

    @Test
    void validUnknownIdsDoNotReceiveTheGenericModel() {
        for (String unknownId : List.of(
                "ae2:future_storage_cell",
                "test:item_storage_cell_1k",
                "megacells:item_storage_cell_1m"
        )) {
            assertStatus(
                    inventoryWith(unknownId),
                    Direction6.NORTH,
                    0,
                    DriveDecodeResult.Status.UNSUPPORTED_CELL_ID
            );
        }
    }

    @Test
    void rejectsMissingInventoryFacingAndOutOfRangeSpin() {
        assertStatus(
                null,
                Direction6.NORTH,
                0,
                DriveDecodeResult.Status.MISSING_INVENTORY
        );
        assertStatus(
                DriveInventoryProjection.empty(),
                null,
                0,
                DriveDecodeResult.Status.INVALID_FACING
        );
        assertStatus(
                DriveInventoryProjection.empty(),
                Direction6.NORTH,
                -1,
                DriveDecodeResult.Status.INVALID_SPIN
        );
        assertStatus(
                DriveInventoryProjection.empty(),
                Direction6.NORTH,
                4,
                DriveDecodeResult.Status.INVALID_SPIN
        );
    }

    @Test
    void snapshotIsResolvedImmutableAndDefensivelyCopied() {
        ArrayList<Optional<DriveCellDefinition>> cells = new ArrayList<>(
                Collections.nCopies(DriveInventoryProjection.SLOT_COUNT, Optional.empty())
        );
        DriveCellDefinition cell = DriveCellCatalog.require("ae2:creative_storage_cell");
        cells.set(3, Optional.of(cell));
        DriveSnapshot snapshot = new DriveSnapshot(
                cells,
                Direction6.SOUTH,
                2,
                PartOrientation.forPart(Direction6.SOUTH, 2)
        );

        cells.clear();
        assertEquals(cell, snapshot.cell(3).orElseThrow());
        assertEquals(1, snapshot.occupiedCount());
        assertFalse(snapshot.isOccupied(2));
        assertTrue(snapshot.isOccupied(3));
        assertThrows(UnsupportedOperationException.class, snapshot.cells()::clear);
        assertThrows(IllegalArgumentException.class, () -> snapshot.cell(-1));
        assertThrows(IllegalArgumentException.class, () -> snapshot.cell(10));
    }

    @Test
    void snapshotRejectsInvalidShapesNullsAndMismatchedOrientation() {
        List<Optional<DriveCellDefinition>> cells = Collections.nCopies(
                DriveInventoryProjection.SLOT_COUNT,
                Optional.empty()
        );
        assertThrows(
                NullPointerException.class,
                () -> new DriveSnapshot(null, Direction6.NORTH, 0,
                        PartOrientation.forPart(Direction6.NORTH, 0))
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> new DriveSnapshot(List.of(), Direction6.NORTH, 0,
                        PartOrientation.forPart(Direction6.NORTH, 0))
        );
        assertThrows(
                NullPointerException.class,
                () -> new DriveSnapshot(cells, null, 0,
                        PartOrientation.forPart(Direction6.NORTH, 0))
        );
        assertThrows(
                NullPointerException.class,
                () -> new DriveSnapshot(cells, Direction6.NORTH, 0, null)
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> new DriveSnapshot(cells, Direction6.NORTH, -1,
                        PartOrientation.forPart(Direction6.NORTH, 0))
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> new DriveSnapshot(cells, Direction6.NORTH, 0,
                        PartOrientation.forPart(Direction6.SOUTH, 0))
        );
    }

    @Test
    void decodeResultEnforcesSupportedSnapshotInvariant() {
        DriveSnapshot snapshot = decoder.decode(
                DriveInventoryProjection.empty(),
                Direction6.NORTH,
                0
        ).supportedSnapshot().orElseThrow();

        assertThrows(
                IllegalArgumentException.class,
                () -> new DriveDecodeResult(DriveDecodeResult.Status.SUPPORTED, null)
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> new DriveDecodeResult(
                        DriveDecodeResult.Status.UNSUPPORTED_CELL_ID,
                        snapshot
                )
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> DriveDecodeResult.fallback(DriveDecodeResult.Status.SUPPORTED)
        );
    }

    private static DriveInventoryProjection inventoryWith(String itemId) {
        return DriveInventoryProjection.empty().withSlot(
                0,
                DriveInventoryProjection.Slot.occupied(itemId)
        );
    }

    private void assertStatus(
            DriveInventoryProjection inventory,
            Direction6 facing,
            int spin,
            DriveDecodeResult.Status expected
    ) {
        DriveDecodeResult result = decoder.decode(inventory, facing, spin);
        assertFalse(result.isSupported());
        assertTrue(result.supportedSnapshot().isEmpty());
        assertEquals(expected, result.status());
    }
}
