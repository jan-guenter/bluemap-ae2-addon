/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.adapter.bluemap523;

import de.bluecolored.bluemap.core.world.mca.MCAUtil;
import de.bluecolored.bluenbt.BlueNBT;
import de.bluecolored.bluenbt.NBTWriter;
import de.bluecolored.bluenbt.TagType;
import io.github.janguenter.bluemap.ae2.model.Direction6;
import io.github.janguenter.bluemap.ae2.model.ExtendedAeDriveDecodeResult;
import io.github.janguenter.bluemap.ae2.model.ExtendedAeDriveDecoder;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExtendedAeDriveBlockEntityDataDeserializationTest {

    @Test
    void exactTwentySlotFixtureRetainsSingleAndDoubleDigitSlots() throws Exception {
        ExtendedAeDriveBlockEntityData data = readDto(inventory((writer, slot) -> {
            switch (slot) {
                case 0 -> {
                    writer.name("id").value("ae2:item_storage_cell_1k");
                    writer.name("count").value(1);
                    writer.name("components").beginCompound();
                    writer.name("test:list").beginList(2, TagType.INT);
                    writer.value(1);
                    writer.value(2);
                    writer.endList();
                    writer.endCompound();
                }
                case 9 -> writer.name("id").value("ae2:matter_cannon");
                case 10 -> writer.name("id").value("extendedae:infinity_water_cell");
                case 19 -> writer.name("id").value("extendedae:void_cell");
                default -> {
                }
            }
        }));

        assertEquals("extendedae:ex_drive", data.getId().toString());
        assertEquals("ae2:item_storage_cell_1k", data.getInventory().slot(0).itemId());
        assertTrue(data.getInventory().slot(1).isEmpty());
        assertEquals("ae2:matter_cannon", data.getInventory().slot(9).itemId());
        assertEquals("extendedae:infinity_water_cell", data.getInventory().slot(10).itemId());
        assertEquals("extendedae:void_cell", data.getInventory().slot(19).itemId());
        assertTrue(data.retainsProbeFields());
        assertTrue(new ExtendedAeDriveDecoder().decode(
                data.getInventory(),
                Direction6.NORTH,
                0
        ).isSupported());
    }

    @Test
    void unknownCellIsRetainedForAtomicDecoderFallback() throws Exception {
        ExtendedAeDriveBlockEntityData data = readDto(inventory((writer, slot) -> {
            if (slot == 12) {
                writer.name("id").value("kubejs:lava_cell");
            }
        }));

        ExtendedAeDriveDecodeResult result = new ExtendedAeDriveDecoder().decode(
                data.getInventory(),
                Direction6.SOUTH,
                2
        );

        assertFalse(result.isSupported());
        assertEquals(ExtendedAeDriveDecodeResult.Status.UNSUPPORTED_CELL_ID, result.status());
    }

    @Test
    void incompleteDuplicateAndNonCanonicalSlotNamesFailClosed() throws Exception {
        byte[] missing = blockEntity(writer -> {
            writer.name("inv").beginCompound();
            for (int slot = 0; slot < 19; slot++) {
                writeEmptySlot(writer, slot);
            }
            writer.endCompound();
        });
        byte[] duplicate = blockEntity(writer -> {
            writer.name("inv").beginCompound();
            for (int slot = 0; slot < 20; slot++) {
                writeEmptySlot(writer, slot);
            }
            writeEmptySlot(writer, 10);
            writer.endCompound();
        });
        byte[] leadingZero = blockEntity(writer -> {
            writer.name("inv").beginCompound();
            for (int slot = 0; slot < 20; slot++) {
                writeEmptySlot(writer, slot);
            }
            writer.name("item01").beginCompound();
            writer.endCompound();
            writer.endCompound();
        });

        assertThrows(IOException.class, () -> readDto(missing));
        assertThrows(IOException.class, () -> readDto(duplicate));
        assertThrows(IOException.class, () -> readDto(leadingZero));
    }

    @Test
    void wrongCountAndExcessiveComponentsUseBoundedFailure() throws Exception {
        byte[] wrongCount = inventory((writer, slot) -> {
            if (slot == 10) {
                writer.name("id").value("extendedae:void_cell");
                writer.name("count").value((byte) 1);
            }
        });
        byte[] excessive = inventory((writer, slot) -> {
            if (slot == 10) {
                writer.name("id").value("extendedae:void_cell");
                writer.name("components").beginCompound();
                writer.name("test:list").beginList(
                        BoundedNbtSkipper.MAX_LIST_ENTRIES + 1,
                        TagType.INT
                );
                for (int index = 0; index <= BoundedNbtSkipper.MAX_LIST_ENTRIES; index++) {
                    writer.value(index);
                }
                writer.endList();
                writer.endCompound();
            }
        });

        assertThrows(IOException.class, () -> readDto(wrongCount));
        assertThrows(IOException.class, () -> readDto(excessive));
    }

    private static ExtendedAeDriveBlockEntityData readDto(byte[] nbt) throws IOException {
        BlueNBT blueNbt = MCAUtil.addCommonNbtSettings(new BlueNBT());
        return blueNbt.read(
                new ByteArrayInputStream(nbt),
                ExtendedAeDriveBlockEntityData.class
        );
    }

    private static byte[] inventory(SlotWriter body) throws IOException {
        return blockEntity(writer -> {
            writer.name("inv").beginCompound();
            for (int slot = 0; slot < 20; slot++) {
                writer.name("item" + slot).beginCompound();
                body.write(writer, slot);
                writer.endCompound();
            }
            writer.endCompound();
        });
    }

    private static byte[] blockEntity(WriterAction body) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (NBTWriter writer = new NBTWriter(bytes)) {
            writer.beginCompound();
            writer.name("id").value("extendedae:ex_drive");
            writer.name("x").value(-7);
            writer.name("y").value(83);
            writer.name("z").value(14);
            body.write(writer);
            writer.endCompound();
        }
        return bytes.toByteArray();
    }

    private static void writeEmptySlot(NBTWriter writer, int slot) throws IOException {
        writer.name("item" + slot).beginCompound();
        writer.endCompound();
    }

    @FunctionalInterface
    private interface WriterAction {
        void write(NBTWriter writer) throws IOException;
    }

    @FunctionalInterface
    private interface SlotWriter {
        void write(NBTWriter writer, int slot) throws IOException;
    }
}
