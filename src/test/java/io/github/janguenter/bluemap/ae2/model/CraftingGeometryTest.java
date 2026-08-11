/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.model;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CraftingGeometryTest {

    private static final double EPSILON = 1.0E-9;

    @Test
    void isolatedKindsMatchTheExactSourceDerivedTriangleCountsAndLayers() {
        assertTriangles(CraftingBlockKind.UNIT, 108);
        assertTriangles(CraftingBlockKind.ACCELERATOR, 120);
        assertTriangles(CraftingBlockKind.STORAGE_1K, 120);
        assertTriangles(CraftingBlockKind.STORAGE_4K, 120);
        assertTriangles(CraftingBlockKind.STORAGE_16K, 120);
        assertTriangles(CraftingBlockKind.STORAGE_64K, 120);
        assertTriangles(CraftingBlockKind.STORAGE_256K, 120);
        assertTriangles(CraftingBlockKind.MONITOR, 114);

        for (CraftingBlockKind kind : CraftingBlockKind.values()) {
            List<CraftingGeometry.Quad> quads = geometry(kind, Set.of());
            assertEquals(24, countRingCorners(quads));
            assertEquals(24, countRingSides(quads));
        }
    }

    @Test
    void allSixtyFourConnectionMasksHaveOutwardWindingAndNoSharedFace() {
        for (CraftingBlockKind kind : CraftingBlockKind.values()) {
            for (int mask = 0; mask < 64; mask++) {
                int currentMask = mask;
                Set<Direction6> connections = connections(mask);
                List<CraftingGeometry.Quad> first = geometry(kind, connections);
                List<CraftingGeometry.Quad> second = geometry(kind, connections);
                assertEquals(first, second, () -> kind + " mask " + currentMask);
                assertEquals(
                        expectedLayerCounts(kind, Direction6.NORTH, connections),
                        layerCounts(first),
                        () -> kind + " source-count oracle mask " + currentMask
                );
                for (CraftingGeometry.Quad quad : first) {
                    assertFalse(
                            connections.contains(quad.face()),
                            () -> "shared face emitted for " + kind + " mask "
                                    + currentMask
                    );
                    assertOutward(quad);
                }
            }
        }
    }

    @Test
    void isolatedFractionalInnerBoundsPreserveExactPositionsAndUvs() {
        CraftingGeometry.Quad northInner = geometry(
                CraftingBlockKind.UNIT,
                Set.of()
        ).stream()
                .filter(quad -> quad.face() == Direction6.NORTH)
                .filter(quad -> quad.layer() == CraftingGeometry.Layer.UNIT_BASE)
                .findFirst()
                .orElseThrow();

        assertArrayEquals(new double[]{
            13.01, 13.01, 0,
            13.01, 2.99, 0,
            2.99, 2.99, 0,
            2.99, 13.01, 0
        }, positions(northInner), EPSILON);
        assertArrayEquals(new double[]{
            2.99, 2.99,
            2.99, 13.01,
            13.01, 13.01,
            13.01, 2.99
        }, uvs(northInner), EPSILON);
    }

    @Test
    void exactCubeBuilderFacePositionsUvsAndTriangleSplitArePreserved() {
        for (Direction6 face : Direction6.values()) {
            EnumSet<Direction6> connections = EnumSet.allOf(Direction6.class);
            connections.remove(face);
            connections.remove(face.opposite());
            CraftingGeometry.Quad quad = geometry(
                    CraftingBlockKind.UNIT,
                    connections
            ).stream().filter(candidate -> candidate.face() == face).findFirst().orElseThrow();

            assertEquals(CraftingGeometry.Layer.UNIT_BASE, quad.layer());
            assertEquals(4, quad.vertices().size());
            assertArrayEquals(
                    expectedFullFacePositions(face),
                    positions(quad),
                    EPSILON,
                    face.name()
            );
            assertArrayEquals(
                    new double[]{0, 0, 0, 16, 16, 16, 16, 0},
                    uvs(quad),
                    EPSILON,
                    face.name()
            );
            assertOutward(quad);
        }
    }

    @Test
    void oneConnectionRemovesItsFaceCapsAndStripesAndExtendsInnerBounds() {
        List<CraftingGeometry.Quad> isolated = geometry(
                CraftingBlockKind.UNIT,
                Set.of()
        );
        List<CraftingGeometry.Quad> east = geometry(
                CraftingBlockKind.UNIT,
                Set.of(Direction6.EAST)
        );

        assertTrue(isolated.stream().anyMatch(quad -> quad.face() == Direction6.EAST));
        assertFalse(east.stream().anyMatch(quad -> quad.face() == Direction6.EAST));
        assertTrue(east.size() < isolated.size());

        CraftingGeometry.Quad northInner = east.stream()
                .filter(quad -> quad.face() == Direction6.NORTH)
                .filter(quad -> quad.layer() == CraftingGeometry.Layer.UNIT_BASE)
                .findFirst()
                .orElseThrow();
        assertTrue(northInner.vertices().stream().anyMatch(vertex -> vertex.x16() == 16D));
        assertTrue(northInner.vertices().stream().anyMatch(vertex -> vertex.x16() == 2.99D));
        assertFalse(east.stream().anyMatch(quad -> quad.face() == Direction6.NORTH
                && quad.layer() != CraftingGeometry.Layer.UNIT_BASE
                && quad.vertices().stream().allMatch(vertex -> vertex.x16() >= 13D)));
    }

    @Test
    void poweredStateChangesOnlyEmissionPolicyOutsideNeutralGeometry() {
        for (CraftingBlockKind kind : CraftingBlockKind.values()) {
            CraftingSnapshot off = snapshot(
                    kind,
                    false,
                    Direction6.SOUTH,
                    3,
                    CableColor.CYAN,
                    Set.of(Direction6.DOWN, Direction6.WEST)
            );
            CraftingSnapshot on = snapshot(
                    kind,
                    true,
                    Direction6.SOUTH,
                    3,
                    CableColor.CYAN,
                    Set.of(Direction6.DOWN, Direction6.WEST)
            );
            assertEquals(CraftingGeometry.forSnapshot(off), CraftingGeometry.forSnapshot(on));
        }
        assertTrue(CraftingGeometry.Layer.ACCELERATOR_LIGHT.emissiveWhenPowered());
        assertTrue(CraftingGeometry.Layer.MONITOR_LIGHT_BRIGHT.emissiveWhenPowered());
        assertFalse(CraftingGeometry.Layer.LIGHT_BASE.emissiveWhenPowered());
        assertFalse(CraftingGeometry.Layer.UNIT_BASE.emissiveWhenPowered());
    }

    @Test
    void monitorAllColorsFacingsAndSpinsKeepSpinInvariantPrimaryGeometry() {
        for (CableColor color : CableColor.values()) {
            for (Direction6 facing : Direction6.values()) {
                List<CraftingGeometry.Quad> baseline = CraftingGeometry.forSnapshot(snapshot(
                        CraftingBlockKind.MONITOR,
                        true,
                        facing,
                        0,
                        color,
                        Set.of()
                ));
                assertEquals(57, baseline.size());
                assertEquals(4, baseline.stream()
                        .filter(quad -> quad.face() == facing)
                        .filter(quad -> !isRing(quad.layer()))
                        .count());
                for (Direction6 side : Direction6.values()) {
                    if (side != facing) {
                        assertEquals(1, baseline.stream()
                                .filter(quad -> quad.face() == side)
                                .filter(quad -> quad.layer() == CraftingGeometry.Layer.UNIT_BASE)
                                .count());
                    }
                }
                for (int spin = 1; spin < 4; spin++) {
                    List<CraftingGeometry.Quad> spun = CraftingGeometry.forSnapshot(snapshot(
                            CraftingBlockKind.MONITOR,
                            true,
                            facing,
                            spin,
                            color,
                            Set.of()
                    ));
                    assertEquals(baseline, spun);
                }
            }
        }
    }

    @Test
    void monitorLayerOrderMatchesClientAndDisplayItemIsAbsent() {
        List<CraftingGeometry.Layer> frontLayers = geometry(
                CraftingBlockKind.MONITOR,
                Set.of()
        ).stream()
                .filter(quad -> quad.face() == Direction6.NORTH)
                .map(CraftingGeometry.Quad::layer)
                .toList();

        assertEquals(List.of(
                CraftingGeometry.Layer.RING_CORNER,
                CraftingGeometry.Layer.RING_CORNER,
                CraftingGeometry.Layer.RING_CORNER,
                CraftingGeometry.Layer.RING_CORNER,
                CraftingGeometry.Layer.RING_SIDE_HORIZONTAL,
                CraftingGeometry.Layer.RING_SIDE_HORIZONTAL,
                CraftingGeometry.Layer.RING_SIDE_VERTICAL,
                CraftingGeometry.Layer.RING_SIDE_VERTICAL,
                CraftingGeometry.Layer.MONITOR_BASE,
                CraftingGeometry.Layer.MONITOR_LIGHT_BRIGHT,
                CraftingGeometry.Layer.MONITOR_LIGHT_MEDIUM,
                CraftingGeometry.Layer.MONITOR_LIGHT_DARK
        ), frontLayers);
        assertFalse(Arrays.stream(CraftingGeometry.Layer.values())
                .anyMatch(layer -> layer.name().contains("ITEM")));
    }

    private static void assertTriangles(CraftingBlockKind kind, int expected) {
        assertEquals(expected, geometry(kind, Set.of()).size() * 2, kind.name());
    }

    private static long countRingCorners(List<CraftingGeometry.Quad> quads) {
        return quads.stream()
                .filter(quad -> quad.layer() == CraftingGeometry.Layer.RING_CORNER)
                .count();
    }

    private static long countRingSides(List<CraftingGeometry.Quad> quads) {
        return quads.stream().filter(quad -> quad.layer()
                == CraftingGeometry.Layer.RING_SIDE_HORIZONTAL
                || quad.layer() == CraftingGeometry.Layer.RING_SIDE_VERTICAL).count();
    }

    private static boolean isRing(CraftingGeometry.Layer layer) {
        return layer == CraftingGeometry.Layer.RING_CORNER
                || layer == CraftingGeometry.Layer.RING_SIDE_HORIZONTAL
                || layer == CraftingGeometry.Layer.RING_SIDE_VERTICAL;
    }

    private static Map<CraftingGeometry.Layer, Long> layerCounts(
            List<CraftingGeometry.Quad> quads
    ) {
        Map<CraftingGeometry.Layer, Long> counts = new EnumMap<>(
                CraftingGeometry.Layer.class
        );
        for (CraftingGeometry.Quad quad : quads) {
            counts.merge(quad.layer(), 1L, Long::sum);
        }
        return counts;
    }

    private static Map<CraftingGeometry.Layer, Long> expectedLayerCounts(
            CraftingBlockKind kind,
            Direction6 monitorFacing,
            Set<Direction6> connections
    ) {
        Map<CraftingGeometry.Layer, Long> counts = new EnumMap<>(
                CraftingGeometry.Layer.class
        );
        Direction6[][] corners = {
            {Direction6.UP, Direction6.EAST, Direction6.NORTH},
            {Direction6.UP, Direction6.EAST, Direction6.SOUTH},
            {Direction6.UP, Direction6.WEST, Direction6.NORTH},
            {Direction6.UP, Direction6.WEST, Direction6.SOUTH},
            {Direction6.DOWN, Direction6.EAST, Direction6.NORTH},
            {Direction6.DOWN, Direction6.EAST, Direction6.SOUTH},
            {Direction6.DOWN, Direction6.WEST, Direction6.NORTH},
            {Direction6.DOWN, Direction6.WEST, Direction6.SOUTH}
        };
        for (Direction6 face : Direction6.values()) {
            if (connections.contains(face)) {
                continue;
            }
            for (Direction6[] corner : corners) {
                if (Arrays.asList(corner).contains(face)
                        && Arrays.stream(corner).noneMatch(connections::contains)) {
                    counts.merge(CraftingGeometry.Layer.RING_CORNER, 1L, Long::sum);
                }
            }
            for (Direction6 edge : Direction6.values()) {
                if (edge == face
                        || edge == face.opposite()
                        || connections.contains(edge)) {
                    continue;
                }
                CraftingGeometry.Layer layer = expectedVerticalStripe(face, edge)
                        ? CraftingGeometry.Layer.RING_SIDE_VERTICAL
                        : CraftingGeometry.Layer.RING_SIDE_HORIZONTAL;
                counts.merge(layer, 1L, Long::sum);
            }
            addExpectedInnerLayers(counts, kind, face, monitorFacing);
        }
        return counts;
    }

    private static boolean expectedVerticalStripe(Direction6 face, Direction6 edge) {
        boolean faceVerticalAxis = face == Direction6.UP || face == Direction6.DOWN;
        boolean edgeVerticalAxis = edge == Direction6.UP || edge == Direction6.DOWN;
        return !faceVerticalAxis && !edgeVerticalAxis
                || faceVerticalAxis && (edge == Direction6.EAST || edge == Direction6.WEST);
    }

    private static void addExpectedInnerLayers(
            Map<CraftingGeometry.Layer, Long> counts,
            CraftingBlockKind kind,
            Direction6 face,
            Direction6 monitorFacing
    ) {
        switch (kind) {
            case UNIT -> counts.merge(CraftingGeometry.Layer.UNIT_BASE, 1L, Long::sum);
            case ACCELERATOR -> addExpectedLight(
                    counts,
                    CraftingGeometry.Layer.ACCELERATOR_LIGHT
            );
            case STORAGE_1K -> addExpectedLight(
                    counts,
                    CraftingGeometry.Layer.STORAGE_1K_LIGHT
            );
            case STORAGE_4K -> addExpectedLight(
                    counts,
                    CraftingGeometry.Layer.STORAGE_4K_LIGHT
            );
            case STORAGE_16K -> addExpectedLight(
                    counts,
                    CraftingGeometry.Layer.STORAGE_16K_LIGHT
            );
            case STORAGE_64K -> addExpectedLight(
                    counts,
                    CraftingGeometry.Layer.STORAGE_64K_LIGHT
            );
            case STORAGE_256K -> addExpectedLight(
                    counts,
                    CraftingGeometry.Layer.STORAGE_256K_LIGHT
            );
            case MONITOR -> {
                if (face == monitorFacing) {
                    counts.merge(CraftingGeometry.Layer.MONITOR_BASE, 1L, Long::sum);
                    counts.merge(
                            CraftingGeometry.Layer.MONITOR_LIGHT_BRIGHT,
                            1L,
                            Long::sum
                    );
                    counts.merge(
                            CraftingGeometry.Layer.MONITOR_LIGHT_MEDIUM,
                            1L,
                            Long::sum
                    );
                    counts.merge(
                            CraftingGeometry.Layer.MONITOR_LIGHT_DARK,
                            1L,
                            Long::sum
                    );
                } else {
                    counts.merge(CraftingGeometry.Layer.UNIT_BASE, 1L, Long::sum);
                }
            }
        }
    }

    private static void addExpectedLight(
            Map<CraftingGeometry.Layer, Long> counts,
            CraftingGeometry.Layer overlay
    ) {
        counts.merge(CraftingGeometry.Layer.LIGHT_BASE, 1L, Long::sum);
        counts.merge(overlay, 1L, Long::sum);
    }

    private static List<CraftingGeometry.Quad> geometry(
            CraftingBlockKind kind,
            Set<Direction6> connections
    ) {
        return CraftingGeometry.forSnapshot(snapshot(
                kind,
                false,
                Direction6.NORTH,
                0,
                CableColor.TRANSPARENT,
                connections
        ));
    }

    private static CraftingSnapshot snapshot(
            CraftingBlockKind kind,
            boolean powered,
            Direction6 facing,
            int spin,
            CableColor color,
            Set<Direction6> connections
    ) {
        return new CraftingSnapshot(kind, powered, facing, spin, color, connections);
    }

    private static Set<Direction6> connections(int mask) {
        EnumSet<Direction6> connections = EnumSet.noneOf(Direction6.class);
        for (Direction6 direction : Direction6.values()) {
            if ((mask & direction.maskBit()) != 0) {
                connections.add(direction);
            }
        }
        return connections;
    }

    private static void assertOutward(CraftingGeometry.Quad quad) {
        List<CraftingGeometry.Vertex> vertices = quad.vertices();
        double[] firstNormal = normal(vertices.get(0), vertices.get(1), vertices.get(2));
        double[] secondNormal = normal(vertices.get(0), vertices.get(2), vertices.get(3));
        assertTrue(dot(firstNormal, quad.face()) > 0D, quad.toString());
        assertTrue(dot(secondNormal, quad.face()) > 0D, quad.toString());
        assertNotEquals(0D, lengthSquared(firstNormal));
        assertNotEquals(0D, lengthSquared(secondNormal));
    }

    private static double[] normal(
            CraftingGeometry.Vertex a,
            CraftingGeometry.Vertex b,
            CraftingGeometry.Vertex c
    ) {
        double abx = b.x16() - a.x16();
        double aby = b.y16() - a.y16();
        double abz = b.z16() - a.z16();
        double acx = c.x16() - a.x16();
        double acy = c.y16() - a.y16();
        double acz = c.z16() - a.z16();
        return new double[]{
            aby * acz - abz * acy,
            abz * acx - abx * acz,
            abx * acy - aby * acx
        };
    }

    private static double dot(double[] normal, Direction6 face) {
        return normal[0] * face.stepX()
                + normal[1] * face.stepY()
                + normal[2] * face.stepZ();
    }

    private static double lengthSquared(double[] normal) {
        return normal[0] * normal[0]
                + normal[1] * normal[1]
                + normal[2] * normal[2];
    }

    private static double[] positions(CraftingGeometry.Quad quad) {
        double[] output = new double[12];
        for (int vertex = 0; vertex < 4; vertex++) {
            CraftingGeometry.Vertex value = quad.vertices().get(vertex);
            output[vertex * 3] = value.x16();
            output[vertex * 3 + 1] = value.y16();
            output[vertex * 3 + 2] = value.z16();
        }
        return output;
    }

    private static double[] uvs(CraftingGeometry.Quad quad) {
        double[] output = new double[8];
        for (int vertex = 0; vertex < 4; vertex++) {
            CraftingGeometry.Vertex value = quad.vertices().get(vertex);
            output[vertex * 2] = value.u16();
            output[vertex * 2 + 1] = value.v16();
        }
        return output;
    }

    private static double[] expectedFullFacePositions(Direction6 face) {
        return switch (face) {
            case DOWN -> new double[]{0, 0, 16, 0, 0, 0, 16, 0, 0, 16, 0, 16};
            case UP -> new double[]{0, 16, 0, 0, 16, 16, 16, 16, 16, 16, 16, 0};
            case NORTH -> new double[]{
                16, 16, 0, 16, 0, 0, 0, 0, 0, 0, 16, 0
            };
            case SOUTH -> new double[]{
                0, 16, 16, 0, 0, 16, 16, 0, 16, 16, 16, 16
            };
            case WEST -> new double[]{0, 16, 0, 0, 0, 0, 0, 0, 16, 0, 16, 16};
            case EAST -> new double[]{
                16, 16, 16, 16, 0, 16, 16, 0, 0, 16, 16, 0
            };
        };
    }
}
