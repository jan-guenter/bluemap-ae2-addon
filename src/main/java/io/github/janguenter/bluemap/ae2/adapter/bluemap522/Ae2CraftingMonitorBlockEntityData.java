/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.adapter.bluemap522;

import de.bluecolored.bluemap.core.world.mca.blockentity.MCABlockEntity;
import de.bluecolored.bluenbt.NBTDeserializer;
import de.bluecolored.bluenbt.NBTName;
import de.bluecolored.bluenbt.NBTPostDeserialize;
import io.github.janguenter.bluemap.ae2.model.CableColor;

import java.io.IOException;

/** BlueNBT DTO retaining only the monitor's optional persisted paint ordinal. */
public final class Ae2CraftingMonitorBlockEntityData extends MCABlockEntity {

    private transient MonitorPaintedColorDeserializer.Session paintSession =
            MonitorPaintedColorDeserializer.beginObject();

    @NBTDeserializer(MonitorPaintedColorDeserializer.class)
    @NBTName("paintedColor")
    private Integer paintedColor;

    public Ae2CraftingMonitorBlockEntityData() {
    }

    public CableColor getPaintedColor() {
        if (paintedColor == null) {
            throw new IllegalStateException("painted color was not finalized");
        }
        return CableColor.values()[paintedColor];
    }

    boolean retainsProbeFields() {
        return paintedColor != null && paintedColor == CableColor.PURPLE.ordinal();
    }

    @NBTPostDeserialize
    private void finalizePaintedColor() throws IOException {
        try {
            paintedColor = paintSession.finish(paintedColor);
        } finally {
            MonitorPaintedColorDeserializer.endObject(paintSession);
            paintSession = null;
        }
    }
}
