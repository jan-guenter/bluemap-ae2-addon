/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MachineGeometryTest {

    @Test
    void neutralInscriberAddsExactlySixItemFreeStaticStampQuads() {
        List<MachineGeometry.Quad> quads = MachineGeometry.neutralInscriberStamps();
        assertEquals(6, quads.size());
        assertEquals(12, quads.size() * 2);
        assertEquals(List.of(
                Direction6.DOWN, Direction6.NORTH, Direction6.SOUTH,
                Direction6.UP, Direction6.NORTH, Direction6.SOUTH
        ), quads.stream().map(MachineGeometry.Quad::face).toList());
        assertEquals(11.52, quads.get(0).vertices().get(0).y16(), 0.0001);
        assertEquals(4.48, quads.get(3).vertices().get(0).y16(), 0.0001);
        assertEquals(1.28, quads.get(4).vertices().get(0).y16(), 0.0001);
        assertEquals(5.2, quads.get(1).vertices().get(0).v16(), 0.0001);
    }
}
