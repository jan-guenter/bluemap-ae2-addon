/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.adapter.bluemap522;

import de.bluecolored.bluemap.core.world.mca.blockentity.MCABlockEntity;
import de.bluecolored.bluenbt.NBTDeserializer;
import de.bluecolored.bluenbt.NBTName;
import de.bluecolored.bluenbt.NBTPostDeserialize;
import io.github.janguenter.bluemap.ae2.model.PaintSnapshot;

import java.io.IOException;

/** BlueNBT DTO retaining only the bounded exact paint-splotch projection. */
public final class Ae2PaintBlockEntityData extends MCABlockEntity {

    private transient Ae2PaintDotsDeserializer.Session dotsSession =
            Ae2PaintDotsDeserializer.beginObject();

    @NBTDeserializer(Ae2PaintDotsDeserializer.class)
    @NBTName("dots")
    private PaintSnapshot paint;

    public Ae2PaintBlockEntityData() {
    }

    public PaintSnapshot getPaint() {
        return paint;
    }

    boolean retainsProbeFields() {
        return paint != null
                && paint.splotches().size() == 2
                && paint.splotches().get(0).signedPosition() == 0x21
                && paint.splotches().get(1).lumen();
    }

    @NBTPostDeserialize
    private void finalizeDots() throws IOException {
        try {
            paint = dotsSession.finish(paint);
        } finally {
            Ae2PaintDotsDeserializer.endObject(dotsSession);
            dotsSession = null;
        }
    }
}
