/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.adapter.bluemap523;

import de.bluecolored.bluemap.core.world.mca.blockentity.MCABlockEntity;
import de.bluecolored.bluenbt.NBTDeserializer;
import io.github.janguenter.bluemap.ae2.model.ExtendedAeDriveInventoryProjection;

/** BlueNBT DTO retaining only the bounded twenty-slot Extended Drive inventory. */
public final class ExtendedAeDriveBlockEntityData extends MCABlockEntity {

    @NBTDeserializer(ExtendedAeDriveInventoryDeserializer.class)
    private ExtendedAeDriveInventoryProjection inv;

    public ExtendedAeDriveBlockEntityData() {
    }

    public ExtendedAeDriveInventoryProjection getInventory() {
        return inv;
    }

    boolean retainsProbeFields() {
        return inv != null
                && inv.slots().size() == ExtendedAeDriveInventoryProjection.SLOT_COUNT
                && "ae2:item_storage_cell_1k".equals(inv.slot(0).itemId())
                && inv.slot(0).count() == 1
                && inv.slot(1).isEmpty()
                && "ae2:matter_cannon".equals(inv.slot(9).itemId())
                && inv.slot(9).count() == 1
                && "extendedae:infinity_water_cell".equals(inv.slot(10).itemId())
                && inv.slot(10).count() == 1
                && "extendedae:void_cell".equals(inv.slot(19).itemId())
                && inv.slot(19).count() == 1;
    }
}
