/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.model;

import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SpatialPylonGeometryTest {

    @Test
    void localTopologyPolicyCoversEveryAxisAndRejectsAmbiguity() {
        for (Direction6 direction : Direction6.values()) {
            SpatialPylonSnapshot single = SpatialPylonSnapshot.infer(Set.of(direction)).orElseThrow();
            assertEquals(SpatialPylonSnapshot.Axis.of(direction), single.axis());
            assertEquals(direction.stepX() > 0 || direction.stepY() > 0 || direction.stepZ() > 0
                            ? SpatialPylonSnapshot.AxisPosition.START
                            : SpatialPylonSnapshot.AxisPosition.END,
                    single.axisPosition());

            SpatialPylonSnapshot middle = SpatialPylonSnapshot.infer(
                    Set.of(direction, direction.opposite())
            ).orElseThrow();
            assertEquals(SpatialPylonSnapshot.AxisPosition.MIDDLE, middle.axisPosition());
        }
        assertEquals(SpatialPylonSnapshot.AxisPosition.NONE,
                SpatialPylonSnapshot.infer(Set.of()).orElseThrow().axisPosition());
        assertTrue(SpatialPylonSnapshot.infer(Set.of(Direction6.UP, Direction6.NORTH)).isEmpty());
        assertTrue(SpatialPylonSnapshot.infer(EnumSet.of(
                Direction6.WEST, Direction6.EAST, Direction6.UP
        )).isEmpty());
    }

    @Test
    void isolatedAndFormedStatesAlwaysEmitTwoNonEmissiveCubeLayers() {
        SpatialPylonSnapshot isolated = SpatialPylonSnapshot.infer(Set.of()).orElseThrow();
        List<SpatialPylonGeometry.Quad> neutral = SpatialPylonGeometry.forSnapshot(isolated);
        assertEquals(12, neutral.size());
        assertEquals(24, neutral.size() * 2);
        assertTrue(neutral.stream().filter(q -> q.layer() == SpatialPylonGeometry.Layer.OUTER)
                .allMatch(q -> q.texture() == SpatialPylonGeometry.Texture.BASE));
        assertTrue(neutral.stream().filter(q -> q.layer() == SpatialPylonGeometry.Layer.INNER)
                .allMatch(q -> q.texture() == SpatialPylonGeometry.Texture.DIM));

        SpatialPylonSnapshot formed = SpatialPylonSnapshot.infer(
                Set.of(Direction6.WEST, Direction6.EAST)
        ).orElseThrow();
        List<SpatialPylonGeometry.Quad> offline = SpatialPylonGeometry.forSnapshot(formed);
        assertTrue(offline.stream().filter(q -> q.layer() == SpatialPylonGeometry.Layer.INNER)
                .allMatch(q -> q.texture().name().startsWith("RED")));
        assertFalse(offline.stream().anyMatch(q -> q.texture().name().startsWith("DIM")));
    }

    @Test
    void textureSelectionAndUvTransformsMatchTheSourceAxisRules() {
        SpatialPylonSnapshot xStart = SpatialPylonSnapshot.infer(Set.of(Direction6.EAST)).orElseThrow();
        List<SpatialPylonGeometry.Quad> quads = SpatialPylonGeometry.forSnapshot(xStart);
        SpatialPylonGeometry.Quad outerEast = quad(quads,
                SpatialPylonGeometry.Layer.OUTER, Direction6.EAST);
        SpatialPylonGeometry.Quad outerNorth = quad(quads,
                SpatialPylonGeometry.Layer.OUTER, Direction6.NORTH);
        SpatialPylonGeometry.Quad innerNorth = quad(quads,
                SpatialPylonGeometry.Layer.INNER, Direction6.NORTH);
        assertEquals(SpatialPylonGeometry.Texture.BASE, outerEast.texture());
        assertEquals(SpatialPylonGeometry.Texture.BASE_END, outerNorth.texture());
        assertEquals(SpatialPylonGeometry.Texture.RED_END, innerNorth.texture());
        assertEquals(0, outerNorth.vertices().get(0).u16(), 0.0001);
        assertEquals(0, outerNorth.vertices().get(0).v16(), 0.0001);
        assertEquals(0, outerNorth.vertices().get(3).u16(), 0.0001);
        assertEquals(16, outerNorth.vertices().get(3).v16(), 0.0001);
    }

    private static SpatialPylonGeometry.Quad quad(
            List<SpatialPylonGeometry.Quad> quads,
            SpatialPylonGeometry.Layer layer,
            Direction6 face
    ) {
        return quads.stream()
                .filter(quad -> quad.layer() == layer && quad.face() == face)
                .findFirst().orElseThrow();
    }
}
