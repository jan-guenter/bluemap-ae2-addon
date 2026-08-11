/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.adapter.bluemap522;

import de.bluecolored.bluenbt.NBTReader;
import de.bluecolored.bluenbt.TagType;
import de.bluecolored.bluenbt.TypeDeserializer;
import io.github.janguenter.bluemap.ae2.model.PaintSnapshot;

import java.io.IOException;
import java.util.Arrays;

/** Strict bounded reader for AE2's padded paint {@code dots} byte array. */
final class Ae2PaintDotsDeserializer implements TypeDeserializer<PaintSnapshot> {

    private static final ThreadLocal<Session> CURRENT = new ThreadLocal<>();

    static Session beginObject() {
        Session session = new Session();
        CURRENT.set(session);
        return session;
    }

    static void endObject(Session session) {
        if (CURRENT.get() == session) {
            CURRENT.remove();
        }
    }

    @Override
    public PaintSnapshot read(NBTReader reader) throws IOException {
        Session session = CURRENT.get();
        if (session == null) {
            throw BoundedNbtSkipper.rejected();
        }
        try {
            session.occurrences++;
            if (reader.peek() != TagType.BYTE_ARRAY) {
                BoundedNbtSkipper.skip(reader);
                throw BoundedNbtSkipper.rejected();
            }
            byte[] buffer = new byte[PaintSnapshot.MAX_PERSISTED_BYTES];
            int length = reader.nextByteArray(buffer);
            if (length < 0 || length > buffer.length) {
                throw BoundedNbtSkipper.rejected();
            }
            try {
                return PaintSnapshot.decode(Arrays.copyOf(buffer, length));
            } catch (IllegalArgumentException exception) {
                throw BoundedNbtSkipper.rejected(exception);
            }
        } catch (IOException | RuntimeException | LinkageError exception) {
            endObject(session);
            throw exception;
        }
    }

    static final class Session {

        private int occurrences;

        PaintSnapshot finish(PaintSnapshot snapshot) throws IOException {
            if (occurrences != 1 || snapshot == null) {
                throw BoundedNbtSkipper.rejected();
            }
            return snapshot;
        }
    }
}
