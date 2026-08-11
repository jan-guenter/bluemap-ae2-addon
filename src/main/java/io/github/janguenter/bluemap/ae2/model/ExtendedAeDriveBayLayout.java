/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.model;

import java.util.Objects;

/** Exact two-sided mapping from persisted slot index to one local ten-slot face. */
public final class ExtendedAeDriveBayLayout {

    private ExtendedAeDriveBayLayout() {
    }

    public static Bay bay(int inventorySlot, Direction6 facing, int spin) {
        Objects.requireNonNull(facing, "facing");
        if (inventorySlot < 0
                || inventorySlot >= ExtendedAeDriveInventoryProjection.SLOT_COUNT) {
            throw new IllegalArgumentException("Extended Drive slot must be in [0, 19]");
        }
        if (spin < 0 || spin > 3) {
            throw new IllegalArgumentException("Extended Drive spin must be in [0, 3]");
        }
        boolean rear = inventorySlot >= ExtendedAeDriveInventoryProjection.SLOTS_PER_SIDE;
        int localSlot = rear
                ? inventorySlot - ExtendedAeDriveInventoryProjection.SLOTS_PER_SIDE
                : inventorySlot;
        Direction6 bayFacing = rear ? facing.opposite() : facing;
        ExtendedAeDriveInventoryProjection.Side side = rear
                ? ExtendedAeDriveInventoryProjection.Side.REAR
                : ExtendedAeDriveInventoryProjection.Side.FRONT;
        return new Bay(
                inventorySlot,
                localSlot,
                side,
                PartOrientation.forPart(bayFacing, spin)
        );
    }

    public record Bay(
            int inventorySlot,
            int localSlot,
            ExtendedAeDriveInventoryProjection.Side side,
            PartOrientation orientation
    ) {

        public Bay {
            Objects.requireNonNull(side, "side");
            Objects.requireNonNull(orientation, "orientation");
            if (inventorySlot < 0
                    || inventorySlot >= ExtendedAeDriveInventoryProjection.SLOT_COUNT) {
                throw new IllegalArgumentException("inventory slot must be in [0, 19]");
            }
            if (localSlot < 0
                    || localSlot >= ExtendedAeDriveInventoryProjection.SLOTS_PER_SIDE) {
                throw new IllegalArgumentException("local slot must be in [0, 9]");
            }
        }
    }
}
