/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.model;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QuantumBridgeGeometryTest {

    private static final double EPSILON = 1.0E-5;

    @Test
    void exactStaticRolesMatchSourceCubeAndTriangleCounts() {
        List<QuantumBridgeGeometry.Quad> link = geometry(
                QuantumBridgeSnapshot.Role.LINK,
                Set.of(
                        Direction6.NORTH,
                        Direction6.SOUTH,
                        Direction6.WEST,
                        Direction6.EAST
                )
        );
        List<QuantumBridgeGeometry.Quad> corner = geometry(
                QuantumBridgeSnapshot.Role.CORNER_RING,
                Set.of(Direction6.EAST, Direction6.UP)
        );
        List<QuantumBridgeGeometry.Quad> edge = geometry(
                QuantumBridgeSnapshot.Role.EDGE_RING,
                Set.of(Direction6.EAST, Direction6.WEST, Direction6.UP)
        );

        assertEquals(54, link.size());
        assertEquals(108, link.size() * 2);
        assertEquals(24, count(link, QuantumBridgeGeometry.Layer.GLASS));
        assertEquals(24, count(link, QuantumBridgeGeometry.Layer.COVERED));
        assertEquals(6, count(link, QuantumBridgeGeometry.Layer.LINK));

        assertEquals(18, corner.size());
        assertEquals(36, corner.size() * 2);
        assertEquals(12, count(corner, QuantumBridgeGeometry.Layer.COVERED));
        assertEquals(6, count(corner, QuantumBridgeGeometry.Layer.RING));

        assertEquals(18, edge.size());
        assertEquals(36, edge.size() * 2);
        assertEquals(18, count(edge, QuantumBridgeGeometry.Layer.RING));
    }

    @Test
    void exactFractionalArmBoundsAndSourceOrderArePreserved() {
        List<QuantumBridgeGeometry.Quad> quads = geometry(
                QuantumBridgeSnapshot.Role.LINK,
                Set.of(
                        Direction6.NORTH,
                        Direction6.SOUTH,
                        Direction6.WEST,
                        Direction6.EAST
                )
        );

        assertBounds(quads.subList(0, 6), 0, 6.24, 6.24, 3.984, 9.76, 9.76);
        assertBounds(quads.subList(6, 12), 12.016, 6.24, 6.24, 16, 9.76, 9.76);
        assertBounds(quads.subList(12, 18), 6.24, 6.24, 0, 9.76, 9.76, 3.984);
        assertBounds(quads.subList(18, 24), 6.24, 6.24, 12.016, 9.76, 9.76, 16);

        assertBounds(quads.subList(24, 30), 0, 4.992, 4.992, 1.992, 11.008, 11.008);
        assertBounds(quads.subList(30, 36), 14.008, 4.992, 4.992, 16, 11.008, 11.008);
        assertBounds(quads.subList(48, 54), 2, 2, 2, 14, 14, 14);

        List<QuantumBridgeGeometry.Quad> corner = geometry(
                QuantumBridgeSnapshot.Role.CORNER_RING,
                Set.of(Direction6.EAST, Direction6.UP)
        );
        assertBounds(corner.subList(0, 6), 11.808, 4.992, 4.992, 16, 11.008, 11.008);
        assertBounds(corner.subList(6, 12), 4.992, 11.808, 4.992, 11.008, 16, 11.008);
    }

    @Test
    void sourceFloatOperationOrderPinsCriticalArmAndUvBits() {
        List<QuantumBridgeGeometry.Quad> link = geometry(
                QuantumBridgeSnapshot.Role.LINK,
                Set.of(
                        Direction6.NORTH,
                        Direction6.SOUTH,
                        Direction6.WEST,
                        Direction6.EAST
                )
        );
        List<QuantumBridgeGeometry.Quad> glassWest = link.subList(0, 6);
        List<QuantumBridgeGeometry.Quad> glassEast = link.subList(6, 12);
        List<QuantumBridgeGeometry.Quad> coveredWest = link.subList(24, 30);
        List<QuantumBridgeGeometry.Quad> coveredEast = link.subList(30, 36);

        assertBoundsBits(
                glassWest,
                0x00000000, 0x3ec7ae14, 0x3ec7ae14,
                0x3e7ef9da, 0x3f1c28f6, 0x3f1c28f6
        );
        assertBoundsBits(
                glassEast,
                0x3f40418a, 0x3ec7ae14, 0x3ec7ae14,
                0x3f800000, 0x3f1c28f6, 0x3f1c28f6
        );
        assertBoundsBits(
                coveredWest,
                0x00000000, 0x3e9fbe77, 0x3e9fbe77,
                0x3dfef9dc, 0x3f3020c4, 0x3f3020c4
        );
        assertBoundsBits(
                coveredEast,
                0x3f6020c4, 0x3e9fbe77, 0x3e9fbe77,
                0x3f800000, 0x3f3020c4, 0x3f3020c4
        );

        List<QuantumBridgeGeometry.Quad> cornerPositive = geometry(
                QuantumBridgeSnapshot.Role.CORNER_RING,
                Set.of(Direction6.EAST, Direction6.UP)
        );
        assertBoundsBits(
                cornerPositive.subList(0, 6),
                0x3f3ced91, 0x3e9fbe77, 0x3e9fbe77,
                0x3f800000, 0x3f3020c4, 0x3f3020c4
        );
        List<QuantumBridgeGeometry.Quad> cornerNegative = geometry(
                QuantumBridgeSnapshot.Role.CORNER_RING,
                Set.of(Direction6.WEST, Direction6.DOWN)
        );
        assertBoundsBits(
                cornerNegative.subList(0, 6),
                0x00000000, 0x3e9fbe77, 0x3e9fbe77,
                0x3e8624dd, 0x3f3020c4, 0x3f3020c4
        );

        assertUvTupleBits(
                glassWest,
                Direction6.DOWN,
                0x00000000, 0x3ec7ae14, 0x3e7ef9da, 0x3f1c28f6
        );
        assertUvTupleBits(
                glassWest,
                Direction6.UP,
                0x00000000, 0x3ec7ae14, 0x3e7ef9da, 0x3f1c28f6
        );
        assertUvTupleBits(
                glassWest,
                Direction6.NORTH,
                0x3f40418a, 0x3f1c28f6, 0x3f800000, 0x3ec7ae14
        );
        assertUvTupleBits(
                glassWest,
                Direction6.SOUTH,
                0x00000000, 0x3f1c28f6, 0x3e7ef9da, 0x3ec7ae14
        );
        assertUvTupleBits(
                glassWest,
                Direction6.WEST,
                0x3ec7ae14, 0x3f1c28f6, 0x3f1c28f6, 0x3ec7ae14
        );
        assertUvTupleBits(
                glassWest,
                Direction6.EAST,
                0x3ec7ae14, 0x3f1c28f6, 0x3f1c28f6, 0x3ec7ae14
        );

        assertUvTupleBits(
                coveredWest,
                Direction6.DOWN,
                0x00000000, 0x3e9fbe77, 0x3dfef9dc, 0x3f3020c4
        );
        assertUvTupleBits(
                coveredWest,
                Direction6.UP,
                0x00000000, 0x3e9fbe77, 0x3dfef9dc, 0x3f3020c4
        );
        assertUvTupleBits(
                coveredWest,
                Direction6.NORTH,
                0x3f6020c4, 0x3f3020c4, 0x3f800000, 0x3e9fbe78
        );
        assertUvTupleBits(
                coveredWest,
                Direction6.SOUTH,
                0x00000000, 0x3f3020c4, 0x3dfef9dc, 0x3e9fbe78
        );
        assertUvTupleBits(
                coveredWest,
                Direction6.WEST,
                0x3e9fbe77, 0x3f3020c4, 0x3f3020c4, 0x3e9fbe78
        );
        assertUvTupleBits(
                coveredWest,
                Direction6.EAST,
                0x3e9fbe78, 0x3f3020c4, 0x3f3020c4, 0x3e9fbe78
        );
    }

    @Test
    void everyCubeFaceHasExactStandardUvAndOutwardWinding() {
        for (QuantumBridgeSnapshot.Role role : QuantumBridgeSnapshot.Role.values()) {
            for (QuantumBridgeGeometry.Quad quad : geometry(role, connections(role))) {
                assertEquals(4, quad.vertices().size());
                assertOutward(quad);
                assertStandardUv(quad);
            }
        }
    }

    @Test
    void waterloggingDoesNotChangeGeometryAndPowerOverlayIsAlwaysAbsent() {
        QuantumBridgeSnapshot dry = new QuantumBridgeSnapshot(
                QuantumBridgeSnapshot.Role.EDGE_RING,
                connections(QuantumBridgeSnapshot.Role.EDGE_RING),
                false
        );
        QuantumBridgeSnapshot wet = new QuantumBridgeSnapshot(
                QuantumBridgeSnapshot.Role.EDGE_RING,
                connections(QuantumBridgeSnapshot.Role.EDGE_RING),
                true
        );

        assertEquals(
                QuantumBridgeGeometry.forSnapshot(dry),
                QuantumBridgeGeometry.forSnapshot(wet)
        );
        assertFalse(dry.emitsPoweredOverlay());
        assertFalse(wet.emitsPoweredOverlay());
        assertEquals(396, 108 + 8 * 36);
    }

    @Test
    void impossibleRoleConnectionsAreRejectedBeforeGeometry() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new QuantumBridgeSnapshot(
                        QuantumBridgeSnapshot.Role.LINK,
                        Set.of(Direction6.NORTH, Direction6.SOUTH),
                        false
                )
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> new QuantumBridgeSnapshot(
                        QuantumBridgeSnapshot.Role.CORNER_RING,
                        Set.of(Direction6.NORTH, Direction6.SOUTH),
                        false
                )
        );
    }

    private static List<QuantumBridgeGeometry.Quad> geometry(
            QuantumBridgeSnapshot.Role role,
            Set<Direction6> connections
    ) {
        return QuantumBridgeGeometry.forSnapshot(
                new QuantumBridgeSnapshot(role, connections, false)
        );
    }

    private static Set<Direction6> connections(QuantumBridgeSnapshot.Role role) {
        return switch (role) {
            case LINK -> Set.of(
                    Direction6.NORTH,
                    Direction6.SOUTH,
                    Direction6.WEST,
                    Direction6.EAST
            );
            case CORNER_RING -> Set.of(Direction6.EAST, Direction6.UP);
            case EDGE_RING -> Set.of(Direction6.EAST, Direction6.WEST, Direction6.UP);
        };
    }

    private static long count(
            List<QuantumBridgeGeometry.Quad> quads,
            QuantumBridgeGeometry.Layer layer
    ) {
        return quads.stream().filter(quad -> quad.layer() == layer).count();
    }

    private static void assertBounds(
            List<QuantumBridgeGeometry.Quad> cube,
            double x1,
            double y1,
            double z1,
            double x2,
            double y2,
            double z2
    ) {
        double[] actual = {
            cube.stream().flatMap(quad -> quad.vertices().stream())
                    .mapToDouble(QuantumBridgeGeometry.Vertex::x16).min().orElseThrow(),
            cube.stream().flatMap(quad -> quad.vertices().stream())
                    .mapToDouble(QuantumBridgeGeometry.Vertex::y16).min().orElseThrow(),
            cube.stream().flatMap(quad -> quad.vertices().stream())
                    .mapToDouble(QuantumBridgeGeometry.Vertex::z16).min().orElseThrow(),
            cube.stream().flatMap(quad -> quad.vertices().stream())
                    .mapToDouble(QuantumBridgeGeometry.Vertex::x16).max().orElseThrow(),
            cube.stream().flatMap(quad -> quad.vertices().stream())
                    .mapToDouble(QuantumBridgeGeometry.Vertex::y16).max().orElseThrow(),
            cube.stream().flatMap(quad -> quad.vertices().stream())
                    .mapToDouble(QuantumBridgeGeometry.Vertex::z16).max().orElseThrow()
        };
        assertArrayEquals(new double[]{x1, y1, z1, x2, y2, z2}, actual, EPSILON);
    }

    private static void assertBoundsBits(
            List<QuantumBridgeGeometry.Quad> cube,
            int x1,
            int y1,
            int z1,
            int x2,
            int y2,
            int z2
    ) {
        java.util.Set<Integer> xBits = bits(
                cube,
                QuantumBridgeGeometry.Vertex::x16
        );
        java.util.Set<Integer> yBits = bits(
                cube,
                QuantumBridgeGeometry.Vertex::y16
        );
        java.util.Set<Integer> zBits = bits(
                cube,
                QuantumBridgeGeometry.Vertex::z16
        );
        assertTrue(xBits.containsAll(Set.of(x1, x2)), "x source-bound bits");
        assertTrue(yBits.containsAll(Set.of(y1, y2)), "y source-bound bits");
        assertTrue(zBits.containsAll(Set.of(z1, z2)), "z source-bound bits");
    }

    private static java.util.Set<Integer> bits(
            List<QuantumBridgeGeometry.Quad> cube,
            java.util.function.ToDoubleFunction<QuantumBridgeGeometry.Vertex> coordinate
    ) {
        return cube.stream()
                .flatMap(quad -> quad.vertices().stream())
                .mapToInt(vertex -> normalizedBits(coordinate.applyAsDouble(vertex)))
                .boxed()
                .collect(java.util.stream.Collectors.toUnmodifiableSet());
    }

    private static void assertUvTupleBits(
            List<QuantumBridgeGeometry.Quad> cube,
            Direction6 face,
            int u1,
            int v1,
            int u2,
            int v2
    ) {
        QuantumBridgeGeometry.Quad quad = cube.stream()
                .filter(candidate -> candidate.face() == face)
                .findFirst()
                .orElseThrow();
        List<QuantumBridgeGeometry.Vertex> vertices = quad.vertices();
        int[] actual;
        if (face == Direction6.DOWN || face == Direction6.UP) {
            actual = new int[]{
                normalizedBits(vertices.get(0).u16()),
                normalizedBits(vertices.get(0).v16()),
                normalizedBits(vertices.get(2).u16()),
                normalizedBits(vertices.get(2).v16())
            };
        } else {
            actual = new int[]{
                normalizedBits(vertices.get(0).u16()),
                normalizedBits(vertices.get(1).v16()),
                normalizedBits(vertices.get(2).u16()),
                normalizedBits(vertices.get(0).v16())
            };
        }
        assertArrayEquals(new int[]{u1, v1, u2, v2}, actual, face.name());
    }

    private static int normalizedBits(double value16) {
        return Float.floatToRawIntBits((float) value16 / 16F);
    }

    private static void assertOutward(QuantumBridgeGeometry.Quad quad) {
        QuantumBridgeGeometry.Vertex first = quad.vertices().get(0);
        QuantumBridgeGeometry.Vertex second = quad.vertices().get(1);
        QuantumBridgeGeometry.Vertex third = quad.vertices().get(2);
        double ax = second.x16() - first.x16();
        double ay = second.y16() - first.y16();
        double az = second.z16() - first.z16();
        double bx = third.x16() - first.x16();
        double by = third.y16() - first.y16();
        double bz = third.z16() - first.z16();
        double dot = (ay * bz - az * by) * quad.face().stepX()
                + (az * bx - ax * bz) * quad.face().stepY()
                + (ax * by - ay * bx) * quad.face().stepZ();
        assertEquals(true, dot > 0D, quad.face().name());
    }

    private static void assertStandardUv(QuantumBridgeGeometry.Quad quad) {
        double x1 = quad.vertices().stream().mapToDouble(
                QuantumBridgeGeometry.Vertex::x16
        ).min().orElseThrow();
        double x2 = quad.vertices().stream().mapToDouble(
                QuantumBridgeGeometry.Vertex::x16
        ).max().orElseThrow();
        double y1 = quad.vertices().stream().mapToDouble(
                QuantumBridgeGeometry.Vertex::y16
        ).min().orElseThrow();
        double y2 = quad.vertices().stream().mapToDouble(
                QuantumBridgeGeometry.Vertex::y16
        ).max().orElseThrow();
        double z1 = quad.vertices().stream().mapToDouble(
                QuantumBridgeGeometry.Vertex::z16
        ).min().orElseThrow();
        double z2 = quad.vertices().stream().mapToDouble(
                QuantumBridgeGeometry.Vertex::z16
        ).max().orElseThrow();
        double v1 = quad.face() == Direction6.DOWN || quad.face() == Direction6.UP
                ? z1 : 16D - y1;
        double v2 = quad.face() == Direction6.DOWN || quad.face() == Direction6.UP
                ? z2 : 16D - y2;
        double u1 = switch (quad.face()) {
            case DOWN, UP, SOUTH -> x1;
            case NORTH -> 16D - x2;
            case WEST -> z1;
            case EAST -> 16D - z2;
        };
        double u2 = switch (quad.face()) {
            case DOWN, UP, SOUTH -> x2;
            case NORTH -> 16D - x1;
            case WEST -> z2;
            case EAST -> 16D - z1;
        };
        double[] expected = quad.face() == Direction6.DOWN || quad.face() == Direction6.UP
                ? new double[]{u1, v1, u1, v2, u2, v2, u2, v1}
                : new double[]{u1, v2, u1, v1, u2, v1, u2, v2};
        double[] actual = quad.vertices().stream()
                .flatMapToDouble(vertex -> java.util.stream.DoubleStream.of(
                        vertex.u16(),
                        vertex.v16()
                ))
                .toArray();
        assertArrayEquals(expected, actual, EPSILON, quad.face().name());
    }
}
