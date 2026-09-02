/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.adapter.bluemap523;

import de.bluecolored.bluemap.core.world.BlockEntity;
import de.bluecolored.bluemap.core.world.mca.MCAUtil;
import de.bluecolored.bluemap.core.world.mca.blockentity.MCABlockEntity;
import de.bluecolored.bluenbt.BlueNBT;
import de.bluecolored.bluenbt.NBTWriter;
import de.bluecolored.bluenbt.TagType;
import io.github.janguenter.bluemap.ae2.model.Direction6;
import io.github.janguenter.bluemap.ae2.model.DriveDecodeResult;
import io.github.janguenter.bluemap.ae2.model.DriveDecoder;
import io.github.janguenter.bluemap.ae2.model.DriveInventoryProjection;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Ae2DriveBlockEntityDataDeserializationTest {

    @Test
    void exactTenSlotFixtureRetainsOnlyIdsAndCounts() throws Exception {
        byte[] nbt = inventory((writer, slot) -> {
            if (slot == 0) {
                writer.name("id").value("ae2:item_storage_cell_1k");
                writer.name("count").value(1);
                writer.name("components").beginCompound();
                writer.name("test:nested").beginList(3, TagType.INT);
                writer.value(1);
                writer.value(2);
                writer.value(3);
                writer.endList();
                writer.name("test:bytes").value(new byte[]{1, 2, 3});
                writer.endCompound();
            } else if (slot == 9) {
                writer.name("id").value("ae2:color_applicator");
            }
        });

        Ae2DriveBlockEntityData data = readDto(nbt);

        assertEquals("ae2:drive", data.getId().toString());
        assertEquals(11, data.getX());
        assertEquals(67, data.getY());
        assertEquals(-29, data.getZ());
        assertEquals(
                DriveInventoryProjection.Slot.occupied("ae2:item_storage_cell_1k"),
                data.getInventory().slot(0)
        );
        assertTrue(data.getInventory().slot(1).isEmpty());
        assertEquals(
                DriveInventoryProjection.Slot.occupied("ae2:color_applicator"),
                data.getInventory().slot(9)
        );
        assertTrue(new DriveDecoder().decode(
                data.getInventory(),
                Direction6.NORTH,
                0
        ).isSupported());
    }

    @Test
    void unknownCellIdIsRetainedForAtomicDecoderFallback() throws Exception {
        Ae2DriveBlockEntityData data = readDto(inventory((writer, slot) -> {
            if (slot == 4) {
                writer.name("id").value("example:future_cell");
            }
        }));

        DriveDecodeResult result = new DriveDecoder().decode(
                data.getInventory(),
                Direction6.SOUTH,
                3
        );

        assertFalse(result.isSupported());
        assertEquals(DriveDecodeResult.Status.UNSUPPORTED_CELL_ID, result.status());
    }

    @Test
    void incompleteDuplicateAndUnknownDirectInventoryFieldsFailClosed()
            throws Exception {
        byte[] missingSlot = blockEntity(writer -> {
            writer.name("inv").beginCompound();
            for (int slot = 0; slot < 9; slot++) {
                writeEmptySlot(writer, slot);
            }
            writer.endCompound();
        });
        byte[] duplicateSlot = blockEntity(writer -> {
            writer.name("inv").beginCompound();
            for (int slot = 0; slot < DriveInventoryProjection.SLOT_COUNT; slot++) {
                writeEmptySlot(writer, slot);
            }
            writeEmptySlot(writer, 0);
            writer.endCompound();
        });
        byte[] unknownField = blockEntity(writer -> {
            writer.name("inv").beginCompound();
            for (int slot = 0; slot < DriveInventoryProjection.SLOT_COUNT; slot++) {
                writeEmptySlot(writer, slot);
            }
            writer.name("future").beginCompound();
            writer.endCompound();
            writer.endCompound();
        });

        assertRejectedAndResolverFallsBack(missingSlot);
        assertThrows(IOException.class, () -> readDto(duplicateSlot));
        assertThrows(IOException.class, () -> readDto(unknownField));
    }

    @Test
    void wrongSlotShapeAndOverlongIdFailClosed() throws Exception {
        byte[] wrongCount = inventory((writer, slot) -> {
            if (slot == 0) {
                writer.name("id").value("ae2:item_storage_cell_1k");
                writer.name("count").value((byte) 1);
            }
        });
        byte[] overlongId = inventory((writer, slot) -> {
            if (slot == 0) {
                writer.name("id").value("a".repeat(
                        Ae2DriveInventoryDeserializer.MAX_ITEM_ID_CHARS + 1
                ));
            }
        });
        byte[] componentWithoutId = inventory((writer, slot) -> {
            if (slot == 0) {
                writer.name("components").beginCompound();
                writer.endCompound();
            }
        });

        assertThrows(IOException.class, () -> readDto(wrongCount));
        assertThrows(IOException.class, () -> readDto(overlongId));
        assertThrows(IOException.class, () -> readDto(componentWithoutId));
    }

    @Test
    void componentPayloadUsesTheExistingBoundedTraversal() throws Exception {
        byte[] exact = inventory((writer, slot) -> {
            if (slot == 0) {
                writer.name("id").value("ae2:item_storage_cell_1k");
                writer.name("components").beginCompound();
                writer.name("test:list").beginList(
                        BoundedNbtSkipper.MAX_LIST_ENTRIES,
                        TagType.INT
                );
                for (int index = 0; index < BoundedNbtSkipper.MAX_LIST_ENTRIES; index++) {
                    writer.value(index);
                }
                writer.endList();
                writer.endCompound();
            }
        });
        byte[] excessive = inventory((writer, slot) -> {
            if (slot == 0) {
                writer.name("id").value("ae2:item_storage_cell_1k");
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

        assertTrue(new DriveDecoder().decode(
                readDto(exact).getInventory(),
                Direction6.UP,
                2
        ).isSupported());
        assertThrows(IOException.class, () -> readDto(excessive));
    }

    private static void assertRejectedAndResolverFallsBack(byte[] nbt) throws IOException {
        assertThrows(IOException.class, () -> readDto(nbt));

        assertTrue(BlueMap523Adapter.install());
        BlockEntity resolved = freshBlueNbt().read(
                new ByteArrayInputStream(nbt),
                BlockEntity.class
        );
        MCABlockEntity base = assertInstanceOf(MCABlockEntity.class, resolved);
        assertFalse(base instanceof Ae2DriveBlockEntityData);
        assertEquals("ae2:drive", base.getId().toString());
        assertEquals(11, base.getX());
        assertEquals(67, base.getY());
        assertEquals(-29, base.getZ());
    }

    private static Ae2DriveBlockEntityData readDto(byte[] nbt) throws IOException {
        return freshBlueNbt().read(
                new ByteArrayInputStream(nbt),
                Ae2DriveBlockEntityData.class
        );
    }

    private static BlueNBT freshBlueNbt() {
        return MCAUtil.addCommonNbtSettings(new BlueNBT());
    }

    private static byte[] inventory(SlotWriter body) throws IOException {
        return blockEntity(writer -> {
            writer.name("inv").beginCompound();
            for (int slot = 0; slot < DriveInventoryProjection.SLOT_COUNT; slot++) {
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
            writer.name("id").value("ae2:drive");
            writer.name("x").value(11);
            writer.name("y").value(67);
            writer.name("z").value(-29);
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
