/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.model;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QuartzGlassGeometryTest {

    @Test
    void snapshotAcceptsExactlyBothNativeVariantsAndSixConnectionBits() {
        for (String block : List.of("ae2:quartz_glass", "ae2:quartz_vibrant_glass")) {
            QuartzGlassSnapshot snapshot = QuartzGlassSnapshot.isolated(block);
            assertEquals(block, snapshot.blockId());
            assertEquals(0, snapshot.connectionMask());
        }
        assertThrows(
                IllegalArgumentException.class,
                () -> QuartzGlassSnapshot.isolated("example:glass")
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> new QuartzGlassSnapshot("ae2:quartz_glass", 64)
        );
    }

    @Test
    void allSixtyFourConnectionMasksUseTheExactSixFaceLocalOrders() {
        Direction6[][] localEdges = {
                {Direction6.SOUTH, Direction6.EAST, Direction6.NORTH, Direction6.WEST},
                {Direction6.SOUTH, Direction6.WEST, Direction6.NORTH, Direction6.EAST},
                {Direction6.UP, Direction6.WEST, Direction6.DOWN, Direction6.EAST},
                {Direction6.UP, Direction6.EAST, Direction6.DOWN, Direction6.WEST},
                {Direction6.UP, Direction6.SOUTH, Direction6.DOWN, Direction6.NORTH},
                {Direction6.UP, Direction6.NORTH, Direction6.DOWN, Direction6.SOUTH}
        };
        for (int connections = 0; connections < 64; connections++) {
            QuartzGlassSnapshot snapshot = new QuartzGlassSnapshot(
                    "ae2:quartz_glass",
                    connections
            );
            int expectedBaseQuads = 0;
            int expectedFrameQuads = 0;
            for (Direction6 face : Direction6.values()) {
                int expected = 0;
                for (int edge = 0; edge < 4; edge++) {
                    if ((connections & localEdges[face.ordinal()][edge].maskBit()) == 0) {
                        expected |= 1 << edge;
                    }
                }
                assertEquals(expected, snapshot.frameMask(face));
                if (!snapshot.isConnected(face)) {
                    expectedBaseQuads++;
                    if (expected != 0) {
                        expectedFrameQuads++;
                    }
                }
            }
            for (String block : List.of(
                    "ae2:quartz_glass",
                    "ae2:quartz_vibrant_glass"
            )) {
                List<QuartzGlassGeometry.Quad> quads =
                        QuartzGlassGeometry.forSnapshot(
                                new QuartzGlassSnapshot(block, connections),
                                0,
                                0,
                                0
                        );
                assertEquals(
                        expectedBaseQuads,
                        quads.stream()
                                .filter(quad -> quad.layer()
                                        == QuartzGlassGeometry.Layer.BASE)
                                .count()
                );
                assertEquals(
                        expectedFrameQuads,
                        quads.stream()
                                .filter(quad -> quad.layer()
                                        == QuartzGlassGeometry.Layer.FRAME)
                                .count()
                );
                int expectedQuads = expectedBaseQuads + expectedFrameQuads;
                assertEquals(expectedQuads, quads.size());
                int emittedTriangles = quads.stream()
                        .mapToInt(quad -> quad.vertices().size() - 2)
                        .sum();
                assertEquals(expectedQuads * 2, emittedTriangles);
            }
        }
    }

    @Test
    void connectedFacesDisappearAndMaskZeroNeverRequestsFrameTexture() {
        QuartzGlassSnapshot isolated = QuartzGlassSnapshot.isolated("ae2:quartz_glass");
        List<QuartzGlassGeometry.Quad> isolatedQuads = QuartzGlassGeometry.forSnapshot(
                isolated,
                0,
                0,
                0
        );
        assertEquals(12, isolatedQuads.size());
        assertEquals(6, isolatedQuads.stream()
                .filter(quad -> quad.layer() == QuartzGlassGeometry.Layer.FRAME)
                .count());
        assertTrue(isolatedQuads.stream()
                .filter(quad -> quad.layer() == QuartzGlassGeometry.Layer.FRAME)
                .allMatch(quad -> quad.textureIndex() == 15));

        QuartzGlassSnapshot surrounded = new QuartzGlassSnapshot(
                "ae2:quartz_vibrant_glass",
                QuartzGlassSnapshot.ALL_CONNECTIONS
        );
        assertTrue(QuartzGlassGeometry.forSnapshot(surrounded, 0, 0, 0).isEmpty());

        QuartzGlassSnapshot plane = new QuartzGlassSnapshot(
                "ae2:quartz_glass",
                Direction6.NORTH.maskBit()
                        | Direction6.SOUTH.maskBit()
                        | Direction6.WEST.maskBit()
                        | Direction6.EAST.maskBit()
        );
        assertEquals(0, plane.frameMask(Direction6.UP));
        assertFalse(QuartzGlassGeometry.forSnapshot(plane, 0, 0, 0).stream()
                .anyMatch(quad -> quad.face() == Direction6.UP
                        && quad.layer() == QuartzGlassGeometry.Layer.FRAME));

        QuartzGlassSnapshot oneVisibleFace = new QuartzGlassSnapshot(
                "ae2:quartz_glass",
                QuartzGlassSnapshot.ALL_CONNECTIONS & ~Direction6.UP.maskBit()
        );
        assertEquals(5, Integer.bitCount(oneVisibleFace.connectionMask()));
        assertEquals(0, oneVisibleFace.frameMask(Direction6.UP));
        List<QuartzGlassGeometry.Quad> oneVisibleQuads =
                QuartzGlassGeometry.forSnapshot(oneVisibleFace, 0, 0, 0);
        assertEquals(1, oneVisibleQuads.size());
        assertEquals(Direction6.UP, oneVisibleQuads.getFirst().face());
        assertEquals(QuartzGlassGeometry.Layer.BASE, oneVisibleQuads.getFirst().layer());
        assertEquals(2, oneVisibleQuads.getFirst().vertices().size() - 2);
    }

    @Test
    void exactRenderHelperCornersHaveOutwardWindingAndAe2UvOrder() {
        QuartzGlassSnapshot isolated = QuartzGlassSnapshot.isolated("ae2:quartz_glass");
        List<QuartzGlassGeometry.Quad> baseQuads = QuartzGlassGeometry.forSnapshot(
                isolated,
                0,
                0,
                0
        ).stream().filter(quad -> quad.layer() == QuartzGlassGeometry.Layer.BASE).toList();

        for (QuartzGlassGeometry.Quad quad : baseQuads) {
            QuartzGlassGeometry.Vertex a = quad.vertices().get(0);
            QuartzGlassGeometry.Vertex b = quad.vertices().get(1);
            QuartzGlassGeometry.Vertex c = quad.vertices().get(2);
            double[] normal = cross(a, b, c);
            double outward = normal[0] * quad.face().stepX()
                    + normal[1] * quad.face().stepY()
                    + normal[2] * quad.face().stepZ();
            assertTrue(outward > 0, quad.face().name());
            assertEquals(0D, a.u16());
            assertEquals(0D, a.v16());
            assertEquals(0D, b.u16());
            assertEquals(c.v16(), b.v16());
            assertEquals(c.u16(), quad.vertices().get(3).u16());
            assertEquals(0D, quad.vertices().get(3).v16());
        }

        assertEquals(
                List.of(
                        new QuartzGlassGeometry.Vertex(0, 0, 16, 0, 0),
                        new QuartzGlassGeometry.Vertex(0, 0, 0, 0, 13),
                        new QuartzGlassGeometry.Vertex(16, 0, 0, 14, 13),
                        new QuartzGlassGeometry.Vertex(16, 0, 16, 14, 0)
                ),
                baseQuads.get(Direction6.DOWN.ordinal()).vertices()
        );
    }

    @Test
    void positionSeedAndLegacyRandomMatchPinnedPositiveNegativeAndExtremeVectors() {
        assertSelection(0, 0, 0, 0L, 2, 14D, 13D);
        assertSelection(5, 70, -4, 100_881_384_580_813L, 0, 15D, 15D);
        assertSelection(-31, 73, 19, 33_070_963_730_244L, 0, 15D, 15D);
        assertSelection(
                Integer.MAX_VALUE,
                Integer.MIN_VALUE,
                Integer.MAX_VALUE,
                71_483_002_586_549L,
                1,
                15.5D,
                15.5D
        );
        assertSelection(
                Integer.MIN_VALUE,
                Integer.MAX_VALUE,
                Integer.MIN_VALUE,
                -33_307_851_324_795L,
                3,
                13D,
                14D
        );
    }

    @Test
    void everyFaceOfOneBlockReusesOneTextureSelection() {
        List<QuartzGlassGeometry.Quad> base = QuartzGlassGeometry.forSnapshot(
                QuartzGlassSnapshot.isolated("ae2:quartz_vibrant_glass"),
                208,
                100,
                288
        ).stream().filter(quad -> quad.layer() == QuartzGlassGeometry.Layer.BASE).toList();

        assertEquals(6, base.size());
        assertEquals(1, base.stream().map(QuartzGlassGeometry.Quad::textureIndex).distinct().count());
        assertEquals(1, base.stream().map(quad -> uvSignature(quad.vertices())).distinct().count());
    }

    private static void assertSelection(
            int x,
            int y,
            int z,
            long seed,
            int texture,
            double uMax,
            double vMax
    ) {
        assertEquals(seed, QuartzGlassGeometry.positionSeed(x, y, z));
        QuartzGlassGeometry.TextureSelection selection =
                QuartzGlassGeometry.textureSelection(x, y, z);
        assertEquals(texture, selection.textureIndex());
        assertEquals(uMax, selection.uMax16());
        assertEquals(vMax, selection.vMax16());
    }

    private static double[] cross(
            QuartzGlassGeometry.Vertex a,
            QuartzGlassGeometry.Vertex b,
            QuartzGlassGeometry.Vertex c
    ) {
        double abX = b.x16() - a.x16();
        double abY = b.y16() - a.y16();
        double abZ = b.z16() - a.z16();
        double acX = c.x16() - a.x16();
        double acY = c.y16() - a.y16();
        double acZ = c.z16() - a.z16();
        return new double[]{
                abY * acZ - abZ * acY,
                abZ * acX - abX * acZ,
                abX * acY - abY * acX
        };
    }

    private static List<Double> uvSignature(List<QuartzGlassGeometry.Vertex> vertices) {
        return vertices.stream()
                .flatMap(vertex -> Arrays.stream(new Double[]{vertex.u16(), vertex.v16()}))
                .toList();
    }
}
