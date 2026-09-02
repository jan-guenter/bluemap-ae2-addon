/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.adapter.bluemap523;

import de.bluecolored.bluemap.core.world.mca.MCAUtil;
import de.bluecolored.bluenbt.BlueNBT;
import de.bluecolored.bluenbt.NBTWriter;
import io.github.janguenter.bluemap.ae2.model.CableColor;
import io.github.janguenter.bluemap.ae2.model.Direction6;
import io.github.janguenter.bluemap.ae2.model.PaintSnapshot;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class Ae2PaintBlockEntityDataDeserializationTest {

    @Test
    void exactDurable256BytePayloadIsRetainedWithoutGenericObjectGraph() throws Exception {
        byte[] dots = durableDots();
        Ae2PaintBlockEntityData data = readDto(blockEntity(writer -> {
            writer.name("dots").value(dots);
            writer.name("unrelated").beginCompound();
            writer.name("nested").value("ignored");
            writer.endCompound();
        }));

        assertEquals(256, dots.length);
        assertEquals(2, data.getPaint().splotches().size());
        assertEquals(Direction6.UP, data.getPaint().splotches().get(0).backingSide());
        assertEquals(CableColor.RED, data.getPaint().splotches().get(0).color());
        assertEquals("ae2:paint", data.getId().toString());
        assertEquals(7, data.getX());
        assertEquals(83, data.getY());
        assertEquals(-13, data.getZ());
    }

    @Test
    void exactCompactSyntheticPayloadIsAlsoAccepted() throws Exception {
        Ae2PaintBlockEntityData data = readDto(blockEntity(
                writer -> writer.name("dots").value(Arrays.copyOf(durableDots(), 5))
        ));
        assertEquals(2, data.getPaint().splotches().size());
    }

    @Test
    void missingWrongTypedDuplicateAndMalformedPayloadsFailClosed() throws Exception {
        byte[] missing = blockEntity(writer -> writer.name("other").value(1));
        byte[] wrongType = blockEntity(writer -> writer.name("dots").value(1));
        byte[] duplicate = blockEntity(writer -> {
            writer.name("dots").value(durableDots());
            writer.name("dots").value(durableDots());
        });
        byte[] invalidSideDots = durableDots();
        invalidSideDots[2] = 6;
        byte[] invalidSide = blockEntity(writer -> writer.name("dots").value(invalidSideDots));
        byte[] badPaddingDots = durableDots();
        badPaddingDots[255] = 1;
        byte[] badPadding = blockEntity(writer -> writer.name("dots").value(badPaddingDots));

        for (byte[] invalid : java.util.List.of(
                missing, wrongType, duplicate, invalidSide, badPadding
        )) {
            assertThrows(IOException.class, () -> readDto(invalid));
        }
    }

    private static Ae2PaintBlockEntityData readDto(byte[] nbt) throws IOException {
        return MCAUtil.addCommonNbtSettings(new BlueNBT()).read(
                new ByteArrayInputStream(nbt),
                Ae2PaintBlockEntityData.class
        );
    }

    private static byte[] durableDots() {
        byte[] dots = new byte[PaintSnapshot.MAX_PERSISTED_BYTES];
        dots[0] = 2;
        dots[1] = 0x21;
        dots[2] = (byte) (Direction6.UP.ordinal() | CableColor.RED.ordinal() << 3);
        dots[3] = (byte) 0xf3;
        dots[4] = (byte) (Direction6.WEST.ordinal()
                | CableColor.CYAN.ordinal() << 3 | 0x80);
        return dots;
    }

    private static byte[] blockEntity(WriterAction body) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (NBTWriter writer = new NBTWriter(bytes)) {
            writer.beginCompound();
            writer.name("id").value("ae2:paint");
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
