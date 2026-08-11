/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SkyChestGeometryTest {

    @Test
    void closedChestIsThreeSixFaceCubesInTesrOrder() {
        List<SkyChestGeometry.Quad> quads = SkyChestGeometry.closed();
        assertEquals(18, quads.size());
        assertEquals(36, quads.size() * 2);
        assertEquals(SkyChestGeometry.Part.LID, quads.get(0).part());
        assertEquals(SkyChestGeometry.Part.LOCK, quads.get(6).part());
        assertEquals(SkyChestGeometry.Part.BOTTOM, quads.get(12).part());
        assertEquals(Direction6.DOWN, quads.get(0).face());
        assertEquals(Direction6.SOUTH, quads.get(5).face());
    }

    @Test
    void exactLayerBoundsAnd64PixelUvMappingAreRetained() {
        List<SkyChestGeometry.Quad> quads = SkyChestGeometry.closed();
        double minY = quads.stream().flatMap(quad -> quad.vertices().stream())
                .mapToDouble(SkyChestGeometry.Vertex::y16).min().orElseThrow();
        double maxY = quads.stream().flatMap(quad -> quad.vertices().stream())
                .mapToDouble(SkyChestGeometry.Vertex::y16).max().orElseThrow();
        assertEquals(0, minY);
        assertEquals(15, maxY);

        SkyChestGeometry.Quad lidDown = quads.get(0);
        assertEquals(7, lidDown.vertices().get(0).u16(), 0.0001);
        assertEquals(0, lidDown.vertices().get(0).v16(), 0.0001);
        assertEquals(3.5, lidDown.vertices().get(1).u16(), 0.0001);
        assertTrue(quads.stream().flatMap(quad -> quad.vertices().stream())
                .allMatch(vertex -> vertex.u16() >= 0 && vertex.u16() <= 16
                        && vertex.v16() >= 0 && vertex.v16() <= 16));
    }
}
