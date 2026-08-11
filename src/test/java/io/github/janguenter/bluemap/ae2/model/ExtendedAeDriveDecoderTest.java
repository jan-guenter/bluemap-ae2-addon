/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExtendedAeDriveDecoderTest {

    private final ExtendedAeDriveDecoder decoder = new ExtendedAeDriveDecoder();

    @Test
    void acceptsAllTwentySixExactCellsAcrossBothSides() {
        int index = 0;
        for (ExtendedAeDriveCellDefinition definition
                : ExtendedAeDriveCellCatalog.definitions()) {
            int slot = index % ExtendedAeDriveInventoryProjection.SLOT_COUNT;
            ExtendedAeDriveInventoryProjection inventory =
                    ExtendedAeDriveInventoryProjection.empty().withSlot(
                            slot,
                            ExtendedAeDriveInventoryProjection.Slot.occupied(
                                    definition.itemId()
                            )
                    );

            ExtendedAeDriveSnapshot snapshot = decoder.decode(
                    inventory,
                    Direction6.WEST,
                    3
            ).supportedSnapshot().orElseThrow();

            assertEquals(definition, snapshot.cell(slot).orElseThrow());
            assertEquals(1, snapshot.occupiedCount());
            index++;
        }
    }

    @Test
    void rearSlotsUseOppositeFacingWithTheSameSpinAndLocalIndices() {
        for (Direction6 facing : Direction6.values()) {
            for (int spin = 0; spin < 4; spin++) {
                ExtendedAeDriveSnapshot snapshot = decoder.decode(
                        ExtendedAeDriveInventoryProjection.empty(),
                        facing,
                        spin
                ).supportedSnapshot().orElseThrow();

                assertEquals(
                        PartOrientation.forPart(facing, spin),
                        snapshot.frontOrientation()
                );
                assertEquals(
                        PartOrientation.forPart(facing.opposite(), spin),
                        snapshot.rearOrientation()
                );
                assertEquals(0, snapshot.bay(0).localSlot());
                assertEquals(9, snapshot.bay(9).localSlot());
                assertEquals(0, snapshot.bay(10).localSlot());
                assertEquals(9, snapshot.bay(19).localSlot());
                assertEquals(
                        ExtendedAeDriveInventoryProjection.Side.FRONT,
                        snapshot.bay(9).side()
                );
                assertEquals(
                        ExtendedAeDriveInventoryProjection.Side.REAR,
                        snapshot.bay(10).side()
                );
            }
        }
    }

    @Test
    void unknownCellIdsAndInvalidCountsFailTheWholeProjection() {
        for (String unknown : List.of(
                "kubejs:lava_cell",
                "megacells:item_storage_cell_1m",
                "extendedae:future_cell"
        )) {
            assertStatus(
                    ExtendedAeDriveInventoryProjection.empty().withSlot(
                            19,
                            ExtendedAeDriveInventoryProjection.Slot.occupied(unknown)
                    ),
                    ExtendedAeDriveDecodeResult.Status.UNSUPPORTED_CELL_ID
            );
        }
        assertStatus(
                ExtendedAeDriveInventoryProjection.empty().withSlot(
                        10,
                        ExtendedAeDriveInventoryProjection.Slot.occupied(
                                "extendedae:void_cell",
                                2
                        )
                ),
                ExtendedAeDriveDecodeResult.Status.INVALID_CELL_COUNT
        );
    }

    @Test
    void missingInputsAndMalformedIdsFailClosed() {
        ExtendedAeDriveDecodeResult missing = decoder.decode(null, Direction6.NORTH, 0);
        assertEquals(ExtendedAeDriveDecodeResult.Status.MISSING_INVENTORY, missing.status());
        assertEquals(
                ExtendedAeDriveDecodeResult.Status.INVALID_FACING,
                decoder.decode(ExtendedAeDriveInventoryProjection.empty(), null, 0).status()
        );
        assertEquals(
                ExtendedAeDriveDecodeResult.Status.INVALID_SPIN,
                decoder.decode(
                        ExtendedAeDriveInventoryProjection.empty(),
                        Direction6.NORTH,
                        4
                ).status()
        );
        assertEquals(
                ExtendedAeDriveDecodeResult.Status.INVALID_CELL_ID,
                decoder.decode(
                        ExtendedAeDriveInventoryProjection.empty().withSlot(
                                0,
                                ExtendedAeDriveInventoryProjection.Slot.occupied(
                                        "ExtendedAE:void_cell"
                                )
                        ),
                        Direction6.NORTH,
                        0
                ).status()
        );
    }

    private void assertStatus(
            ExtendedAeDriveInventoryProjection inventory,
            ExtendedAeDriveDecodeResult.Status status
    ) {
        ExtendedAeDriveDecodeResult result = decoder.decode(
                inventory,
                Direction6.NORTH,
                0
        );
        assertFalse(result.isSupported());
        assertTrue(result.supportedSnapshot().isEmpty());
        assertEquals(status, result.status());
    }
}
