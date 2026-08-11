/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

/** Strict decoder for the bounded AE2 19.2.17 drive projection and block state. */
public final class DriveDecoder {

    private static final Pattern RESOURCE_LOCATION = Pattern.compile(
            "[a-z0-9_.-]+:[a-z0-9/._-]+"
    );
    private final DriveCellRouteAccess routeAccess;

    public DriveDecoder() {
        this(DriveCellRouteAccess.NONE);
    }

    public DriveDecoder(DriveCellRouteAccess routeAccess) {
        this.routeAccess = java.util.Objects.requireNonNull(routeAccess, "routeAccess");
    }

    public DriveDecodeResult decode(
            DriveInventoryProjection inventory,
            Direction6 facing,
            int spin
    ) {
        if (inventory == null) {
            return DriveDecodeResult.fallback(DriveDecodeResult.Status.MISSING_INVENTORY);
        }
        if (facing == null) {
            return DriveDecodeResult.fallback(DriveDecodeResult.Status.INVALID_FACING);
        }
        if (spin < 0 || spin > 3) {
            return DriveDecodeResult.fallback(DriveDecodeResult.Status.INVALID_SPIN);
        }

        List<Optional<DriveCellDefinition>> cells = new ArrayList<>(
                DriveInventoryProjection.SLOT_COUNT
        );
        for (DriveInventoryProjection.Slot slot : inventory.slots()) {
            if (slot.isEmpty()) {
                if (slot.count() != 0) {
                    return DriveDecodeResult.fallback(
                            DriveDecodeResult.Status.INVALID_CELL_COUNT
                    );
                }
                cells.add(Optional.empty());
                continue;
            }
            if (slot.count() != 1) {
                return DriveDecodeResult.fallback(
                        DriveDecodeResult.Status.INVALID_CELL_COUNT
                );
            }
            if (!RESOURCE_LOCATION.matcher(slot.itemId()).matches()) {
                return DriveDecodeResult.fallback(
                        DriveDecodeResult.Status.INVALID_CELL_ID
                );
            }
            DriveCellDefinition definition = DriveCellCatalog.find(
                    slot.itemId(),
                    routeAccess
            ).orElse(null);
            if (definition == null) {
                return DriveDecodeResult.fallback(
                        DriveDecodeResult.Status.UNSUPPORTED_CELL_ID
                );
            }
            cells.add(Optional.of(definition));
        }

        return DriveDecodeResult.supported(new DriveSnapshot(
                cells,
                facing,
                spin,
                PartOrientation.forPart(facing, spin)
        ));
    }
}
