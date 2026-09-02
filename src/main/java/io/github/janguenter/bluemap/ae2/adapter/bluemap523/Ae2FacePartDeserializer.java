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

/** Retains a part ID, spin/frequency, and a bounded Cell Dock item identity. */
final class Ae2FacePartDeserializer implements TypeDeserializer<Object> {

    static final int MAX_PART_ID_CHARS = 256;
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
            if (!"id".equals(name) && !"spin".equals(name) && !"freq".equals(name)
                    && !"cell".equals(name)) {
                BoundedNbtSkipper.skip(reader, budget, 1);
                continue;
            }
            budget.visit();
            if (retained.containsKey(name)) {
                throw BoundedNbtSkipper.rejected();
            }
            if ("id".equals(name)) {
                if (reader.peek() == TagType.STRING) {
                    String id = reader.nextString();
                    if (id.length() > MAX_PART_ID_CHARS) {
                        throw BoundedNbtSkipper.rejected();
                    }
                    retained.put(name, id);
                } else {
                    BoundedNbtSkipper.skip(reader, budget, 1);
                    retained.put(name, INVALID);
                }
            } else if ("spin".equals(name) && reader.peek() == TagType.BYTE) {
                retained.put(name, reader.nextByte());
            } else if ("freq".equals(name) && reader.peek() == TagType.SHORT) {
                retained.put(name, reader.nextShort());
            } else if ("cell".equals(name) && reader.peek() == TagType.COMPOUND) {
                retained.put(name, readCell(reader, budget));
            } else {
                BoundedNbtSkipper.skip(reader, budget, 1);
                retained.put(name, INVALID);
            }
        }
        reader.endCompound();
        return Map.copyOf(retained);
    }

    private static Object readCell(
            NBTReader reader,
            BoundedNbtSkipper.Budget budget
    ) throws IOException {
        reader.beginCompound();
        String id = null;
        boolean foundAny = false;
        boolean foundId = false;
        boolean foundCount = false;
        boolean foundComponents = false;
        boolean valid = true;
        while (reader.peek() != TagType.END) {
            String name = reader.name();
            foundAny = true;
            if (name.length() > MAX_DIRECT_FIELD_NAME_CHARS) {
                throw BoundedNbtSkipper.rejected();
            }
            budget.visit();
            switch (name) {
                case "id" -> {
                    if (foundId || reader.peek() != TagType.STRING) {
                        BoundedNbtSkipper.skip(reader, budget, 1);
                        valid = false;
                    } else {
                        foundId = true;
                        id = reader.nextString();
                        if (id.length() > MAX_PART_ID_CHARS) {
                            throw BoundedNbtSkipper.rejected();
                        }
                    }
                }
                case "count" -> {
                    if (foundCount || reader.peek() != TagType.INT) {
                        BoundedNbtSkipper.skip(reader, budget, 1);
                        valid = false;
                    } else {
                        foundCount = true;
                        if (reader.nextInt() <= 0) {
                            valid = false;
                        }
                    }
                }
                case "components" -> {
                    if (foundComponents || reader.peek() != TagType.COMPOUND) {
                        BoundedNbtSkipper.skip(reader, budget, 1);
                        valid = false;
                    } else {
                        foundComponents = true;
                        BoundedNbtSkipper.skip(reader, budget, 1);
                    }
                }
                default -> BoundedNbtSkipper.skip(reader, budget, 1);
            }
        }
        reader.endCompound();
        if (valid && foundId) {
            return Map.of("id", id);
        }
        // ItemStack.saveOptional serializes an empty stack as an exact empty compound.
        // Keep that distinct from a malformed non-empty stack so the structural
        // decoder can project an empty Cell Dock without accepting hostile data.
        return valid && !foundAny ? Map.of() : INVALID;
    }
}
