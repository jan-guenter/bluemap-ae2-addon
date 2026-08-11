/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

/** Strict decoder for the pinned ExtendedAE 2.2.33 twenty-slot projection. */
public final class ExtendedAeDriveDecoder {

    private static final Pattern RESOURCE_LOCATION = Pattern.compile(
            "[a-z0-9_.-]+:[a-z0-9/._-]+"
    );
    private final DriveCellRouteAccess routeAccess;

    public ExtendedAeDriveDecoder() {
        this(DriveCellRouteAccess.NONE);
    }

    public ExtendedAeDriveDecoder(DriveCellRouteAccess routeAccess) {
        this.routeAccess = java.util.Objects.requireNonNull(routeAccess, "routeAccess");
    }

    public ExtendedAeDriveDecodeResult decode(
            ExtendedAeDriveInventoryProjection inventory,
            Direction6 facing,
            int spin
    ) {
        if (inventory == null) {
            return ExtendedAeDriveDecodeResult.fallback(
                    ExtendedAeDriveDecodeResult.Status.MISSING_INVENTORY
            );
        }
        if (facing == null) {
            return ExtendedAeDriveDecodeResult.fallback(
                    ExtendedAeDriveDecodeResult.Status.INVALID_FACING
            );
        }
        if (spin < 0 || spin > 3) {
            return ExtendedAeDriveDecodeResult.fallback(
                    ExtendedAeDriveDecodeResult.Status.INVALID_SPIN
            );
        }

        List<Optional<ExtendedAeDriveCellDefinition>> cells = new ArrayList<>(
                ExtendedAeDriveInventoryProjection.SLOT_COUNT
        );
        for (ExtendedAeDriveInventoryProjection.Slot slot : inventory.slots()) {
            if (slot.isEmpty()) {
                if (slot.count() != 0) {
                    return ExtendedAeDriveDecodeResult.fallback(
                            ExtendedAeDriveDecodeResult.Status.INVALID_CELL_COUNT
                    );
                }
                cells.add(Optional.empty());
                continue;
            }
            if (slot.count() != 1) {
                return ExtendedAeDriveDecodeResult.fallback(
                        ExtendedAeDriveDecodeResult.Status.INVALID_CELL_COUNT
                );
            }
            if (!RESOURCE_LOCATION.matcher(slot.itemId()).matches()) {
                return ExtendedAeDriveDecodeResult.fallback(
                        ExtendedAeDriveDecodeResult.Status.INVALID_CELL_ID
                );
            }
            ExtendedAeDriveCellDefinition definition = ExtendedAeDriveCellCatalog.find(
                    slot.itemId(),
                    routeAccess
            ).orElse(null);
            if (definition == null) {
                return ExtendedAeDriveDecodeResult.fallback(
                        ExtendedAeDriveDecodeResult.Status.UNSUPPORTED_CELL_ID
                );
            }
            cells.add(Optional.of(definition));
        }

        return ExtendedAeDriveDecodeResult.supported(new ExtendedAeDriveSnapshot(
                cells,
                facing,
                spin,
                PartOrientation.forPart(facing, spin),
                PartOrientation.forPart(facing.opposite(), spin)
        ));
    }
}
