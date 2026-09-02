/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.adapter.bluemap523;

import de.bluecolored.bluenbt.NBTReader;
import de.bluecolored.bluenbt.TagType;
import de.bluecolored.bluenbt.TypeDeserializer;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/** Strict bounded projection of a facade's persisted block-state compound. */
final class Ae2FacadeDeserializer implements TypeDeserializer<Object> {

    static final int MAX_BLOCK_ID_CHARS = 256;
    static final int MAX_PROPERTY_COUNT = 64;
    static final int MAX_PROPERTY_KEY_CHARS = 128;
    static final int MAX_PROPERTY_VALUE_CHARS = 256;
    static final int MAX_PROPERTY_TEXT_CHARS = 4_096;
    static final int MAX_DIRECT_FIELD_NAME_CHARS = 64;

    private static final Object INVALID = Boolean.FALSE;

    @Override
    public Object read(NBTReader reader) throws IOException {
        try {
            if (reader.peek() != TagType.COMPOUND) {
                BoundedNbtSkipper.skip(reader);
                return INVALID;
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
        Map<String, Object> retained = new LinkedHashMap<>();

        while (reader.peek() != TagType.END) {
            String name = reader.name();
            if (name.length() > MAX_DIRECT_FIELD_NAME_CHARS) {
                throw BoundedNbtSkipper.rejected();
            }
            if (!"Name".equals(name) && !"Properties".equals(name)) {
                BoundedNbtSkipper.skip(reader, budget, 1);
                throw BoundedNbtSkipper.rejected();
            }
            budget.visit();
            if (retained.containsKey(name)) {
                throw BoundedNbtSkipper.rejected();
            }
            if ("Name".equals(name)) {
                retained.put(name, readName(reader, budget));
            } else {
                retained.put(name, readProperties(reader, budget));
            }
        }
        reader.endCompound();
        return Map.copyOf(retained);
    }

    private static Object readName(
            NBTReader reader,
            BoundedNbtSkipper.Budget budget
    ) throws IOException {
        if (reader.peek() != TagType.STRING) {
            BoundedNbtSkipper.skip(reader, budget, 1);
            return INVALID;
        }
        String value = reader.nextString();
        if (value.length() > MAX_BLOCK_ID_CHARS) {
            throw BoundedNbtSkipper.rejected();
        }
        return value;
    }

    private static Object readProperties(
            NBTReader reader,
            BoundedNbtSkipper.Budget budget
    ) throws IOException {
        if (reader.peek() != TagType.COMPOUND) {
            BoundedNbtSkipper.skip(reader, budget, 1);
            return INVALID;
        }
        reader.beginCompound();
        Map<String, String> properties = new LinkedHashMap<>();
        int textChars = 0;
        while (reader.peek() != TagType.END) {
            budget.visit();
            String key = reader.name();
            if (properties.size() >= MAX_PROPERTY_COUNT
                    || key.length() > MAX_PROPERTY_KEY_CHARS
                    || properties.containsKey(key)) {
                throw BoundedNbtSkipper.rejected();
            }
            if (reader.peek() != TagType.STRING) {
                BoundedNbtSkipper.skip(reader, budget, 1);
                throw BoundedNbtSkipper.rejected();
            }
            String value = reader.nextString();
            if (value.length() > MAX_PROPERTY_VALUE_CHARS) {
                throw BoundedNbtSkipper.rejected();
            }
            textChars += key.length() + value.length();
            if (textChars > MAX_PROPERTY_TEXT_CHARS) {
                throw BoundedNbtSkipper.rejected();
            }
            properties.put(key, value);
        }
        reader.endCompound();
        return Map.copyOf(properties);
    }
}
