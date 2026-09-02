/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.adapter.bluemap523;

import de.bluecolored.bluemap.core.world.BlockEntity;
import de.bluecolored.bluemap.core.world.mca.MCAUtil;
import de.bluecolored.bluemap.core.world.mca.blockentity.MCABlockEntity;
import de.bluecolored.bluenbt.BlueNBT;
import de.bluecolored.bluenbt.NBTWriter;
import io.github.janguenter.bluemap.ae2.model.CableColor;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Ae2CraftingMonitorBlockEntityDataDeserializationTest {

    @Test
    void absentPaintUsesExactTransparentOrdinal() throws Exception {
        Ae2CraftingMonitorBlockEntityData data = readDto(blockEntity(writer -> {
            writer.name("unrelated").beginCompound();
            writer.name("nested").value("ignored");
            writer.endCompound();
        }));

        assertEquals(CableColor.TRANSPARENT, data.getPaintedColor());
        assertEquals("ae2:crafting_monitor", data.getId().toString());
        assertEquals(7, data.getX());
        assertEquals(83, data.getY());
        assertEquals(-13, data.getZ());
    }

    @Test
    void everyExactByteOrdinalMapsToTheExistingAeColorMirror() throws Exception {
        for (CableColor expected : CableColor.values()) {
            Ae2CraftingMonitorBlockEntityData data = readDto(blockEntity(
                    writer -> writer.name("paintedColor").value(
                            (byte) expected.ordinal()
                    )
            ));
            assertEquals(expected, data.getPaintedColor());
        }
        assertEquals(16, CableColor.TRANSPARENT.ordinal());
    }

    @Test
    void invalidWrongTypedAndDuplicatePaintFieldsFailClosed() throws Exception {
        byte[] negative = blockEntity(
                writer -> writer.name("paintedColor").value((byte) -1)
        );
        byte[] tooLarge = blockEntity(
                writer -> writer.name("paintedColor").value((byte) 17)
        );
        byte[] wrongType = blockEntity(
                writer -> writer.name("paintedColor").value(3)
        );
        byte[] duplicate = blockEntity(writer -> {
            writer.name("paintedColor").value((byte) 2);
            writer.name("paintedColor").value((byte) 3);
        });

        for (byte[] invalid : java.util.List.of(
                negative,
                tooLarge,
                wrongType,
                duplicate
        )) {
            assertRejectedAndResolverFallsBack(invalid);
        }
    }

    @Test
    void adapterProbeProvesRegistrationBeforeResolverFreeze() {
        assertTrue(BlueMap523Adapter.install());
        assertTrue(BlueMap523Adapter.probeCraftingMonitorBlockEntityRetention());
    }

    private static void assertRejectedAndResolverFallsBack(byte[] nbt) throws IOException {
        assertThrows(IOException.class, () -> readDto(nbt));

        assertTrue(BlueMap523Adapter.install());
        BlockEntity resolved = freshBlueNbt().read(
                new ByteArrayInputStream(nbt),
                BlockEntity.class
        );
        MCABlockEntity base = assertInstanceOf(MCABlockEntity.class, resolved);
        assertFalse(base instanceof Ae2CraftingMonitorBlockEntityData);
        assertEquals("ae2:crafting_monitor", base.getId().toString());
    }

    private static Ae2CraftingMonitorBlockEntityData readDto(byte[] nbt)
            throws IOException {
        return freshBlueNbt().read(
                new ByteArrayInputStream(nbt),
                Ae2CraftingMonitorBlockEntityData.class
        );
    }

    private static BlueNBT freshBlueNbt() {
        return MCAUtil.addCommonNbtSettings(new BlueNBT());
    }

    private static byte[] blockEntity(WriterAction body) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (NBTWriter writer = new NBTWriter(bytes)) {
            writer.beginCompound();
            writer.name("id").value("ae2:crafting_monitor");
            writer.name("x").value(7);
            writer.name("y").value(83);
            writer.name("z").value(-13);
            body.write(writer);
            writer.endCompound();
        }
        return bytes.toByteArray();
    }

    @FunctionalInterface
    private interface WriterAction {
        void write(NBTWriter writer) throws IOException;
    }
}
