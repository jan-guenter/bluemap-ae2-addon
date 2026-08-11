/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.model;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PaintSnapshotTest {

    @Test
    void decodesExactSignedRecordsAndLightProjection() {
        byte[] bytes = durable(
                record(0x21, Direction6.UP, CableColor.RED, false),
                record(0xf3, Direction6.WEST, CableColor.CYAN, true),
                record(0x84, Direction6.SOUTH, CableColor.WHITE, true)
        );

        PaintSnapshot snapshot = PaintSnapshot.decode(bytes);

        assertEquals(3, snapshot.splotches().size());
        PaintSplotch first = snapshot.splotches().get(0);
        assertEquals(0x21, first.signedPosition());
        assertEquals(Direction6.UP, first.backingSide());
        assertEquals(CableColor.RED, first.color());
        assertEquals(1F / 15F, first.x());
        assertEquals(2F / 15F, first.y());
        assertEquals(2, snapshot.expectedLightLevelProperty());
        assertEquals(256, bytes.length);
        for (int index = 7; index < bytes.length; index++) {
            assertEquals(0, bytes[index], "ordinary AE2 save tail must be zero padded");
        }
    }

    @Test
    void compactSyntheticFormIsAcceptedButIntermediatePaddingIsNot() {
        byte[] durable = durable(record(0x21, Direction6.DOWN, CableColor.BLUE, false));
        assertEquals(1, PaintSnapshot.decode(Arrays.copyOf(durable, 3)).splotches().size());
        assertThrows(IllegalArgumentException.class,
                () -> PaintSnapshot.decode(Arrays.copyOf(durable, 4)));
    }

    @Test
    void malformedCountsSidesLengthsAndPaddingFailClosed() {
        byte[] zeroCount = new byte[PaintSnapshot.MAX_PERSISTED_BYTES];
        byte[] tooMany = zeroCount.clone();
        tooMany[0] = 22;
        byte[] badSide = durable(record(0, Direction6.DOWN, CableColor.WHITE, false));
        badSide[2] = 6;
        byte[] truncated = {2, 0, 0};
        byte[] nonZeroPadding = durable(record(0, Direction6.DOWN, CableColor.WHITE, false));
        nonZeroPadding[200] = 1;

        for (byte[] invalid : java.util.List.of(
                zeroCount, tooMany, badSide, truncated, nonZeroPadding
        )) {
            assertThrows(IllegalArgumentException.class, () -> PaintSnapshot.decode(invalid));
        }
    }

    @Test
    void lumenThresholdMatchesExactBlockPropertyContract() {
        assertEquals(0, PaintSnapshot.decode(durable(
                record(0, Direction6.DOWN, CableColor.WHITE, false)
        )).expectedLightLevelProperty());
        assertEquals(1, PaintSnapshot.decode(durable(
                record(0, Direction6.DOWN, CableColor.WHITE, true)
        )).expectedLightLevelProperty());
    }

    private static byte[] durable(byte[]... records) {
        byte[] output = new byte[PaintSnapshot.MAX_PERSISTED_BYTES];
        output[0] = (byte) records.length;
        int offset = 1;
        for (byte[] record : records) {
            output[offset++] = record[0];
            output[offset++] = record[1];
        }
        return output;
    }

    private static byte[] record(
            int position,
            Direction6 side,
            CableColor color,
            boolean lumen
    ) {
        return new byte[]{
            (byte) position,
            (byte) (side.ordinal() | color.ordinal() << 3 | (lumen ? 0x80 : 0))
        };
    }
}
