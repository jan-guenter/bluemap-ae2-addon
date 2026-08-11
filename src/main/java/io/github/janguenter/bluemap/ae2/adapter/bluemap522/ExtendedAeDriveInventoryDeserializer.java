/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.adapter.bluemap522;

import de.bluecolored.bluenbt.NBTReader;
import de.bluecolored.bluenbt.TagType;
import de.bluecolored.bluenbt.TypeDeserializer;
import io.github.janguenter.bluemap.ae2.model.ExtendedAeDriveInventoryProjection;

import java.io.IOException;
import java.util.Arrays;

/** Strict bounded projection of ExtendedAE's direct {@code inv.item0..item19} compounds. */
final class ExtendedAeDriveInventoryDeserializer
        implements TypeDeserializer<ExtendedAeDriveInventoryProjection> {

    static final int MAX_ITEM_ID_CHARS = 256;
    static final int MAX_DIRECT_FIELD_NAME_CHARS = 64;

    @Override
    public ExtendedAeDriveInventoryProjection read(NBTReader reader) throws IOException {
        try {
            if (reader.peek() != TagType.COMPOUND) {
                BoundedNbtSkipper.skip(reader);
                throw BoundedNbtSkipper.rejected();
            }
            return readInventory(reader);
        } catch (IOException | RuntimeException exception) {
            throw BoundedNbtSkipper.rejected(exception);
        }
    }

    private static ExtendedAeDriveInventoryProjection readInventory(NBTReader reader)
            throws IOException {
        ExtendedAeDriveInventoryProjection.Slot[] slots =
                new ExtendedAeDriveInventoryProjection.Slot[
                        ExtendedAeDriveInventoryProjection.SLOT_COUNT
                ];
        boolean[] seen = new boolean[ExtendedAeDriveInventoryProjection.SLOT_COUNT];
        BoundedNbtSkipper.Budget budget = new BoundedNbtSkipper.Budget();
        budget.visit();
        reader.beginCompound();
        while (reader.peek() != TagType.END) {
            String name = reader.name();
            if (name.length() > MAX_DIRECT_FIELD_NAME_CHARS) {
                throw BoundedNbtSkipper.rejected();
            }
            int slot = slotIndex(name);
            if (slot < 0 || seen[slot]) {
                throw BoundedNbtSkipper.rejected();
            }
            seen[slot] = true;
            budget.visit();
            slots[slot] = readSlot(reader);
        }
        reader.endCompound();

        for (int slot = 0; slot < ExtendedAeDriveInventoryProjection.SLOT_COUNT; slot++) {
            if (!seen[slot]) {
                throw BoundedNbtSkipper.rejected();
            }
        }
        return new ExtendedAeDriveInventoryProjection(Arrays.asList(slots));
    }

    private static ExtendedAeDriveInventoryProjection.Slot readSlot(NBTReader reader)
            throws IOException {
        if (reader.peek() != TagType.COMPOUND) {
            BoundedNbtSkipper.skip(reader);
            throw BoundedNbtSkipper.rejected();
        }

        BoundedNbtSkipper.Budget budget = new BoundedNbtSkipper.Budget();
        budget.visit();
        reader.beginCompound();
        String id = null;
        int count = 1;
        boolean foundId = false;
        boolean foundCount = false;
        boolean foundComponents = false;
        while (reader.peek() != TagType.END) {
            String name = reader.name();
            if (name.length() > MAX_DIRECT_FIELD_NAME_CHARS) {
                throw BoundedNbtSkipper.rejected();
            }
            budget.visit();
            switch (name) {
                case "id" -> {
                    if (foundId || reader.peek() != TagType.STRING) {
                        throw BoundedNbtSkipper.rejected();
                    }
                    foundId = true;
                    id = reader.nextString();
                    if (id.length() > MAX_ITEM_ID_CHARS) {
                        throw BoundedNbtSkipper.rejected();
                    }
                }
                case "count" -> {
                    if (foundCount || reader.peek() != TagType.INT) {
                        throw BoundedNbtSkipper.rejected();
                    }
                    foundCount = true;
                    count = reader.nextInt();
                }
                case "components" -> {
                    if (foundComponents || reader.peek() != TagType.COMPOUND) {
                        throw BoundedNbtSkipper.rejected();
                    }
                    foundComponents = true;
                    BoundedNbtSkipper.skip(reader, budget, 1);
                }
                default -> throw BoundedNbtSkipper.rejected();
            }
        }
        reader.endCompound();

        if (!foundId) {
            if (foundCount || foundComponents) {
                throw BoundedNbtSkipper.rejected();
            }
            return ExtendedAeDriveInventoryProjection.Slot.empty();
        }
        return ExtendedAeDriveInventoryProjection.Slot.occupied(id, count);
    }

    private static int slotIndex(String name) {
        if (!name.startsWith("item")) {
            return -1;
        }
        String suffix = name.substring(4);
        if (suffix.length() == 1) {
            char digit = suffix.charAt(0);
            return digit >= '0' && digit <= '9' ? digit - '0' : -1;
        }
        if (suffix.length() == 2
                && suffix.charAt(0) == '1'
                && suffix.charAt(1) >= '0'
                && suffix.charAt(1) <= '9') {
            return 10 + suffix.charAt(1) - '0';
        }
        return -1;
    }
}
