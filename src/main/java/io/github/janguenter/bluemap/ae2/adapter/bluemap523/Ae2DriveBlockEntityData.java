/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.adapter.bluemap523;

import de.bluecolored.bluemap.core.world.mca.blockentity.MCABlockEntity;
import de.bluecolored.bluenbt.NBTDeserializer;
import io.github.janguenter.bluemap.ae2.model.DriveInventoryProjection;

/** BlueNBT DTO retaining only the bounded ten-slot AE2 drive inventory projection. */
public final class Ae2DriveBlockEntityData extends MCABlockEntity {

    @NBTDeserializer(Ae2DriveInventoryDeserializer.class)
    private DriveInventoryProjection inv;

    public Ae2DriveBlockEntityData() {
    }

    public DriveInventoryProjection getInventory() {
        return inv;
    }

    boolean retainsProbeFields() {
        return inv != null
                && inv.slots().size() == DriveInventoryProjection.SLOT_COUNT
                && "ae2:item_storage_cell_1k".equals(inv.slot(0).itemId())
                && inv.slot(0).count() == 1
                && inv.slot(1).isEmpty()
                && "ae2:matter_cannon".equals(inv.slot(9).itemId())
                && inv.slot(9).count() == 1;
    }
}
