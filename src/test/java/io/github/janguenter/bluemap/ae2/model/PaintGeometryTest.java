/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PaintGeometryTest {

    @Test
    void emitsOneSourceExactQuadPerSplotchWithIncrementingOffsets() {
        PaintSnapshot snapshot = new PaintSnapshot(List.of(
                new PaintSplotch(0, Direction6.UP, CableColor.RED, false),
                new PaintSplotch((byte) 0xff, Direction6.DOWN, CableColor.BLUE, true)
        ));

        List<PaintGeometry.Quad> quads = PaintGeometry.forSnapshot(snapshot);

        assertEquals(2, quads.size());
        PaintGeometry.Quad upper = quads.get(0);
        assertEquals(Direction6.DOWN, upper.face());
        assertEquals(CableColor.RED.mediumRgb(), upper.rgb());
        assertFalse(upper.emissive());
        assertEquals(15.984, upper.vertices().get(0).y16(), 0.0001);
        assertEquals(0, upper.vertices().get(0).u16(), 0.0001);
        assertEquals(0, upper.vertices().get(0).v16(), 0.0001);

        PaintGeometry.Quad lower = quads.get(1);
        assertEquals(Direction6.UP, lower.face());
        assertEquals(CableColor.BLUE.brightRgb(), lower.rgb());
        assertTrue(lower.emissive());
        assertEquals(0.032, lower.vertices().get(0).y16(), 0.0001);
        assertEquals(4, quads.get(0).vertices().size());
    }

    @Test
    void clampsCentersAndUsesTheExactThreeTextureSeed() {
        PaintSplotch low = new PaintSplotch((byte) 0x00, Direction6.NORTH,
                CableColor.WHITE, false);
        PaintSplotch high = new PaintSplotch((byte) 0xff, Direction6.SOUTH,
                CableColor.GREEN, true);
        List<PaintGeometry.Quad> quads = PaintGeometry.forSnapshot(
                new PaintSnapshot(List.of(low, high))
        );

        assertEquals(low.seed() % 3, quads.get(0).textureIndex());
        assertEquals(high.seed() % 3, quads.get(1).textureIndex());
        for (PaintGeometry.Quad quad : quads) {
            for (PaintGeometry.Vertex vertex : quad.vertices()) {
                assertTrue(vertex.x16() >= 0 && vertex.x16() <= 16);
                assertTrue(vertex.y16() >= 0 && vertex.y16() <= 16);
                assertTrue(vertex.z16() >= 0 && vertex.z16() <= 16);
            }
        }
    }
}
