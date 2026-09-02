/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.adapter.bluemap523;

import de.bluecolored.bluenbt.NBTReader;
import de.bluecolored.bluenbt.TagType;
import de.bluecolored.bluenbt.TypeDeserializer;

import java.io.IOException;
import java.util.Map;

/** Retains only the direct center-part ID while streaming past all other data. */
final class Ae2CablePartDeserializer implements TypeDeserializer<Object> {

    static final int MAX_CENTER_ID_CHARS = 256;
    static final int MAX_DIRECT_FIELD_NAME_CHARS = 64;

    private static final Object NOT_A_COMPOUND = Boolean.FALSE;
    private static final Object ID_NOT_A_STRING = Boolean.FALSE;

    @Override
    public Object read(NBTReader reader) throws IOException {
        try {
            if (reader.peek() != TagType.COMPOUND) {
                BoundedNbtSkipper.skip(reader);
                return NOT_A_COMPOUND;
            }
            return readCompound(reader);
        } catch (IOException | RuntimeException exception) {
            throw BoundedNbtSkipper.rejected(exception);
        }
    }

    private static Object readCompound(NBTReader reader) throws IOException {
        BoundedNbtSkipper.Budget budget = new BoundedNbtSkipper.Budget();
        budget.visit();
        reader.beginCompound();

        boolean foundId = false;
        Object id = null;
        while (reader.peek() != TagType.END) {
            String name = reader.name();
            if (name.length() > MAX_DIRECT_FIELD_NAME_CHARS) {
                throw BoundedNbtSkipper.rejected();
            }
            if (!"id".equals(name)) {
                BoundedNbtSkipper.skip(reader, budget, 1);
                continue;
            }
            budget.visit();
            if (foundId) {
                throw BoundedNbtSkipper.rejected();
            }
            foundId = true;
            if (reader.peek() == TagType.STRING) {
                String value = reader.nextString();
                if (value.length() > MAX_CENTER_ID_CHARS) {
                    throw BoundedNbtSkipper.rejected();
                }
                id = value;
            } else {
                BoundedNbtSkipper.skip(reader, budget, 1);
                id = ID_NOT_A_STRING;
            }
        }
        reader.endCompound();

        return foundId ? Map.of("id", id) : Map.of();
    }
}
