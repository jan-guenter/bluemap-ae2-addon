/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.adapter.bluemap522;

import de.bluecolored.bluenbt.NBTReader;
import de.bluecolored.bluenbt.TagType;
import de.bluecolored.bluenbt.TypeDeserializer;
import io.github.janguenter.bluemap.ae2.model.CableColor;

import java.io.IOException;

/** Strict byte-only reader with per-object duplicate detection. */
final class MonitorPaintedColorDeserializer implements TypeDeserializer<Integer> {

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
    public Integer read(NBTReader reader) throws IOException {
        Session session = CURRENT.get();
        if (session == null) {
            throw BoundedNbtSkipper.rejected();
        }
        try {
            session.occurrences++;
            if (reader.peek() != TagType.BYTE) {
                BoundedNbtSkipper.skip(reader);
                throw BoundedNbtSkipper.rejected();
            }
            int ordinal = reader.nextByte();
            if (ordinal < 0 || ordinal >= CableColor.values().length) {
                throw BoundedNbtSkipper.rejected();
            }
            return ordinal;
        } catch (IOException | RuntimeException | LinkageError exception) {
            endObject(session);
            throw exception;
        }
    }

    static final class Session {

        private int occurrences;

        int finish(Integer ordinal) throws IOException {
            if (occurrences > 1) {
                throw BoundedNbtSkipper.rejected();
            }
            return ordinal == null ? CableColor.TRANSPARENT.ordinal() : ordinal;
        }
    }
}
