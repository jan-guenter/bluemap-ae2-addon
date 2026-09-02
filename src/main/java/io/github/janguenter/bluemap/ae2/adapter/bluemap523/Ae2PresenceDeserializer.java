/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.adapter.bluemap523;

import de.bluecolored.bluenbt.NBTReader;
import de.bluecolored.bluenbt.TypeDeserializer;

import java.io.IOException;

/** Discards a face-part or facade payload and retains only that it was present. */
final class Ae2PresenceDeserializer implements TypeDeserializer<Object> {

    private static final Object PRESENT = Boolean.TRUE;

    @Override
    public Object read(NBTReader reader) throws IOException {
        try {
            BoundedNbtSkipper.skip(reader);
            return PRESENT;
        } catch (IOException | RuntimeException exception) {
            throw BoundedNbtSkipper.rejected(exception);
        }
    }
}
