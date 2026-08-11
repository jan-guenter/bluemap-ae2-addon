/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.model;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FacadeGeometryTest {

    private static final double EPSILON = 1.0E-9;

    @Test
    void allSixRingsContainTwentyFourQuadsAndFortyEightTriangles() {
        for (Direction6 direction : Direction6.values()) {
            List<FacadeGeometry.Quad> quads = FacadeGeometry.ring(direction);
            assertEquals(24, quads.size(), direction.name());
            assertEquals(48, quads.stream().mapToInt(ignored -> 2).sum(), direction.name());
            assertEquals(
                    4,
                    quads.stream().filter(quad -> quad.face() == direction).count(),
                    direction.name()
            );
        }
    }

    @Test
    void everyDirectionUsesTheExactThicknessAndTwelveByTwelveOpening() {
        for (Direction6 direction : Direction6.values()) {
            List<FacadeGeometry.Quad> ring = FacadeGeometry.ring(direction);
            List<CableGeometry.Vertex> vertices = vertices(ring);

            double expectedMinimum = isNegative(direction)
                    ? 0 : 16 - FacadeGeometry.THICKNESS_16;
            double expectedMaximum = isNegative(direction)
                    ? FacadeGeometry.THICKNESS_16 : 16;
            assertEquals(expectedMinimum, minimumNormal(vertices, direction), EPSILON);
            assertEquals(expectedMaximum, maximumNormal(vertices, direction), EPSILON);

            assertEquals(0, minimumTangentA(vertices, direction), EPSILON);
            assertEquals(16, maximumTangentA(vertices, direction), EPSILON);
            assertEquals(0, minimumTangentB(vertices, direction), EPSILON);
            assertEquals(16, maximumTangentB(vertices, direction), EPSILON);

            List<FacadeGeometry.Quad> outward = ring.stream()
                    .filter(quad -> quad.face() == direction)
                    .toList();
            assertEquals(112, projectedArea(outward, direction), EPSILON);
            for (FacadeGeometry.Quad quad : outward) {
                Bounds bounds = tangentBounds(quad.vertices(), direction);
                assertTrue(
                        bounds.maxA() <= FacadeGeometry.HOLE_MIN_16 + EPSILON
                                || bounds.minA() >= FacadeGeometry.HOLE_MAX_16 - EPSILON
                                || bounds.maxB() <= FacadeGeometry.HOLE_MIN_16 + EPSILON
                                || bounds.minB() >= FacadeGeometry.HOLE_MAX_16 - EPSILON,
                        direction + " outer quad crosses the clipped opening"
                );
            }
            assertHoleBoundary(vertices, direction);
        }
    }

    @Test
    void everyQuadHasOutwardWindingAndBoundedUvs() {
        for (Direction6 direction : Direction6.values()) {
            for (FacadeGeometry.Quad quad : FacadeGeometry.ring(direction)) {
                List<CableGeometry.Vertex> vertices = quad.vertices();
                CableGeometry.Vertex first = vertices.get(0);
                CableGeometry.Vertex second = vertices.get(1);
                CableGeometry.Vertex third = vertices.get(2);
                Vector edgeA = subtract(second, first);
                Vector edgeB = subtract(third, first);
                Vector normal = cross(edgeA, edgeB);
                double outwardDot = normal.x() * quad.face().stepX()
                        + normal.y() * quad.face().stepY()
                        + normal.z() * quad.face().stepZ();
                assertTrue(outwardDot > EPSILON, direction + " / " + quad.face());

                for (CableGeometry.Vertex vertex : vertices) {
                    assertTrue(Double.isFinite(vertex.u16()));
                    assertTrue(Double.isFinite(vertex.v16()));
                    assertTrue(vertex.u16() >= -EPSILON && vertex.u16() <= 16 + EPSILON);
                    assertTrue(vertex.v16() >= -EPSILON && vertex.v16() <= 16 + EPSILON);
                }
            }
        }
    }

    @Test
    void exhaustsAll64OpaqueAdjacencyMasksForSlabsAndPartCutouts() {
        int states = 0;
        for (int mask = 0; mask < 64; mask++) {
            for (Direction6 direction : Direction6.values()) {
                List<FacadeGeometry.Quad> slab = FacadeGeometry.layout(
                        direction,
                        null,
                        null,
                        mask
                );
                int perpendicular = 0;
                int strippedRingBoundaryQuads = 0;
                for (Direction6 candidate : Direction6.values()) {
                    if (candidate != direction && candidate != direction.opposite()
                            && (mask & candidate.maskBit()) != 0) {
                        perpendicular++;
                        strippedRingBoundaryQuads += strippedRingBoundaryQuads(
                                direction,
                                candidate
                        );
                    }
                }
                assertEquals(6 - perpendicular, slab.size(),
                        "slab mask=" + mask + ", direction=" + direction);

                List<FacadeGeometry.Quad> clipped = FacadeGeometry.layout(
                        direction,
                        2D,
                        14D,
                        mask
                );
                assertEquals(24 - strippedRingBoundaryQuads, clipped.size(),
                        "ring mask=" + mask + ", direction=" + direction);
                assertEquals(
                        4,
                        clipped.stream().filter(quad -> quad.face() == direction).count()
                );
                assertEquals(
                        4,
                        clipped.stream().filter(quad ->
                                quad.face() == direction.opposite()).count()
                );
                assertEquals(
                        16 - strippedRingBoundaryQuads,
                        clipped.stream().filter(quad ->
                                quad.face() != direction
                                        && quad.face() != direction.opposite()).count()
                );
                states++;
            }
        }
        assertEquals(384, states);
    }

    private static int strippedRingBoundaryQuads(
            Direction6 facadeDirection,
            Direction6 edge
    ) {
        return switch (facadeDirection) {
            case DOWN, UP -> switch (edge) {
                case WEST, EAST -> 1;
                case NORTH, SOUTH -> 3;
                default -> throw new IllegalArgumentException("edge is not perpendicular");
            };
            case NORTH, SOUTH -> switch (edge) {
                case DOWN, UP -> 1;
                case WEST, EAST -> 3;
                default -> throw new IllegalArgumentException("edge is not perpendicular");
            };
            case WEST, EAST -> switch (edge) {
                case DOWN, UP -> 1;
                case NORTH, SOUTH -> 3;
                default -> throw new IllegalArgumentException("edge is not perpendicular");
            };
        };
    }

    @Test
    void cachedRingsQuadsAndVertexListsAreImmutable() {
        List<FacadeGeometry.Quad> ring = FacadeGeometry.ring(Direction6.NORTH);
        assertThrows(UnsupportedOperationException.class, ring::clear);
        assertThrows(UnsupportedOperationException.class, ring.get(0).vertices()::clear);

        ArrayList<CableGeometry.Vertex> mutableVertices = new ArrayList<>(
                ring.get(0).vertices()
        );
        FacadeGeometry.Quad copied = new FacadeGeometry.Quad(
                Direction6.NORTH,
                mutableVertices
        );
        mutableVertices.clear();
        assertEquals(4, copied.vertices().size());
        assertThrows(UnsupportedOperationException.class, copied.vertices()::clear);
    }

    @Test
    void transparentFacadeInsetsAgainstPerpendicularOpaqueFacade() {
        int westMask = Direction6.WEST.maskBit();
        FacadeGeometry.Bounds bounds = FacadeGeometry.facadeBounds(
                Direction6.UP,
                westMask,
                true
        );
        assertEquals(FacadeGeometry.THICKNESS_16, bounds.minX(), EPSILON);
        assertEquals(16D, bounds.maxX(), EPSILON);
        assertEquals(16D - FacadeGeometry.THICKNESS_16, bounds.minY(), EPSILON);

        List<FacadeGeometry.Quad> output = FacadeGeometry.clip(
                Direction6.NORTH,
                fullNorthFace(0D),
                Direction6.UP,
                null,
                westMask,
                true
        );
        assertEquals(1, output.size());
        assertEquals(
                FacadeGeometry.THICKNESS_16,
                output.getFirst().vertices().stream()
                        .mapToDouble(CableGeometry.Vertex::x16)
                        .min().orElseThrow(),
                EPSILON
        );
    }

    @Test
    void opaqueCornerKickerConvertsSourceToleranceToSixteenthsAndReinterpolatesUv() {
        double nearWest = 8.0E-5D;
        List<FacadeGeometry.Quad> output = FacadeGeometry.clip(
                Direction6.NORTH,
                fullNorthFace(nearWest),
                Direction6.UP,
                null,
                Direction6.WEST.maskBit(),
                false
        );
        assertEquals(1, output.size());
        CableGeometry.Vertex kicked = output.getFirst().vertices().stream()
                .filter(vertex -> Math.abs(
                        vertex.y16() - (16D - FacadeGeometry.THICKNESS_16)
                ) < EPSILON)
                .filter(vertex -> vertex.x16() < 1D)
                .findFirst()
                .orElseThrow();
        assertEquals(
                nearWest + FacadeGeometry.THICKNESS_16,
                kicked.x16(),
                EPSILON
        );
        assertEquals(
                16D * (kicked.x16() - nearWest) / (16D - nearWest),
                kicked.u16(),
                1.0E-7D
        );

        double outsideWest = 3.2E-4D;
        List<FacadeGeometry.Quad> outside = FacadeGeometry.clip(
                Direction6.NORTH,
                fullNorthFace(outsideWest),
                Direction6.UP,
                null,
                Direction6.WEST.maskBit(),
                false
        );
        CableGeometry.Vertex unchanged = outside.getFirst().vertices().stream()
                .filter(vertex -> Math.abs(
                        vertex.y16() - (16D - FacadeGeometry.THICKNESS_16)
                ) < EPSILON)
                .filter(vertex -> vertex.x16() < 1D)
                .findFirst()
                .orElseThrow();
        assertEquals(outsideWest, unchanged.x16(), EPSILON);
    }

    @Test
    void slantedQuadUsesNominalFaceProjectionAfterClampMovesItOffPlane() {
        List<CableGeometry.Vertex> source = List.of(
                new CableGeometry.Vertex(12, 2, 2, 0, 16),
                new CableGeometry.Vertex(4, 2, 6, 16, 16),
                new CableGeometry.Vertex(4, 14, 6, 16, 0),
                new CableGeometry.Vertex(12, 14, 2, 0, 0)
        );

        List<FacadeGeometry.Quad> output = FacadeGeometry.clip(
                Direction6.NORTH,
                source,
                Direction6.NORTH,
                null,
                0,
                false
        );

        assertEquals(1, output.size());
        assertEquals(
                List.of(
                        new Uv(0, 16),
                        new Uv(16, 16),
                        new Uv(16, 0),
                        new Uv(0, 0)
                ),
                output.getFirst().vertices().stream()
                        .map(vertex -> new Uv(vertex.u16(), vertex.v16()))
                        .toList()
        );
        assertTrue(output.getFirst().vertices().stream().allMatch(vertex ->
                equal(vertex.z16(), FacadeGeometry.THICKNESS_16)
        ));
    }

    @Test
    void faceStripperUsesExactClampedBoundaryEquality() {
        double nearNorth = 5.0E-10D;
        List<CableGeometry.Vertex> source = List.of(
                new CableGeometry.Vertex(16, 0, nearNorth, 16, 16),
                new CableGeometry.Vertex(0, 0, nearNorth, 0, 16),
                new CableGeometry.Vertex(0, 16, nearNorth, 0, 0),
                new CableGeometry.Vertex(16, 16, nearNorth, 16, 0)
        );

        List<FacadeGeometry.Quad> output = FacadeGeometry.clip(
                Direction6.NORTH,
                source,
                Direction6.UP,
                null,
                Direction6.NORTH.maskBit(),
                false
        );

        assertEquals(1, output.size());
        assertTrue(output.getFirst().vertices().stream().allMatch(vertex ->
                equal(vertex.z16(), nearNorth)
        ));

        double nearWidth = 5.0E-10D;
        List<FacadeGeometry.Quad> nondegenerate = FacadeGeometry.clip(
                Direction6.NORTH,
                List.of(
                        new CableGeometry.Vertex(nearWidth, 0, 0, 16, 16),
                        new CableGeometry.Vertex(0, 0, 0, 0, 16),
                        new CableGeometry.Vertex(0, 16, 0, 0, 0),
                        new CableGeometry.Vertex(nearWidth, 16, 0, 16, 0)
                ),
                Direction6.NORTH,
                null,
                0,
                false
        );
        assertEquals(1, nondegenerate.size());
    }

    @Test
    void transparentInsetNormalizesReversedBoundaryPlaneCutoutStrips() {
        List<CableGeometry.Vertex> fullUp = List.of(
                new CableGeometry.Vertex(0, 16, 0, 0, 16),
                new CableGeometry.Vertex(0, 16, 16, 0, 0),
                new CableGeometry.Vertex(16, 16, 16, 16, 0),
                new CableGeometry.Vertex(16, 16, 0, 16, 16)
        );

        List<FacadeGeometry.Quad> output = FacadeGeometry.clip(
                Direction6.UP,
                fullUp,
                Direction6.UP,
                new FacadeGeometry.Bounds(0, 0, 1, 15, 16, 15),
                Direction6.WEST.maskBit(),
                true
        );

        assertEquals(4, output.size());
        assertTrue(output.stream().anyMatch(quad ->
                equal(quad.vertices().stream()
                                .mapToDouble(CableGeometry.Vertex::x16)
                                .min().orElseThrow(),
                        0D)
                        && equal(quad.vertices().stream()
                                .mapToDouble(CableGeometry.Vertex::x16)
                                .max().orElseThrow(),
                        FacadeGeometry.THICKNESS_16)
        ));
    }

    @Test
    void rejectsMissingDirectionAndInvalidQuadVertexCounts() {
        assertThrows(NullPointerException.class, () -> FacadeGeometry.ring(null));
        assertThrows(
                IllegalArgumentException.class,
                () -> new FacadeGeometry.Quad(Direction6.NORTH, List.of())
        );
    }

    private static List<CableGeometry.Vertex> vertices(List<FacadeGeometry.Quad> quads) {
        return quads.stream().flatMap(quad -> quad.vertices().stream()).toList();
    }

    private static List<CableGeometry.Vertex> fullNorthFace(double west) {
        return List.of(
                new CableGeometry.Vertex(16, 0, 0, 16, 16),
                new CableGeometry.Vertex(west, 0, 0, 0, 16),
                new CableGeometry.Vertex(west, 16, 0, 0, 0),
                new CableGeometry.Vertex(16, 16, 0, 16, 0)
        );
    }

    private static boolean isNegative(Direction6 direction) {
        return direction == Direction6.DOWN
                || direction == Direction6.NORTH
                || direction == Direction6.WEST;
    }

    private static double minimumNormal(
            List<CableGeometry.Vertex> vertices,
            Direction6 direction
    ) {
        return vertices.stream().mapToDouble(vertex -> normal(vertex, direction))
                .min().orElseThrow();
    }

    private static double maximumNormal(
            List<CableGeometry.Vertex> vertices,
            Direction6 direction
    ) {
        return vertices.stream().mapToDouble(vertex -> normal(vertex, direction))
                .max().orElseThrow();
    }

    private static double minimumTangentA(
            List<CableGeometry.Vertex> vertices,
            Direction6 direction
    ) {
        return vertices.stream().mapToDouble(vertex -> tangentA(vertex, direction))
                .min().orElseThrow();
    }

    private static double maximumTangentA(
            List<CableGeometry.Vertex> vertices,
            Direction6 direction
    ) {
        return vertices.stream().mapToDouble(vertex -> tangentA(vertex, direction))
                .max().orElseThrow();
    }

    private static double minimumTangentB(
            List<CableGeometry.Vertex> vertices,
            Direction6 direction
    ) {
        return vertices.stream().mapToDouble(vertex -> tangentB(vertex, direction))
                .min().orElseThrow();
    }

    private static double maximumTangentB(
            List<CableGeometry.Vertex> vertices,
            Direction6 direction
    ) {
        return vertices.stream().mapToDouble(vertex -> tangentB(vertex, direction))
                .max().orElseThrow();
    }

    private static double projectedArea(
            List<FacadeGeometry.Quad> quads,
            Direction6 direction
    ) {
        return quads.stream().mapToDouble(quad -> {
            Bounds bounds = tangentBounds(quad.vertices(), direction);
            return (bounds.maxA() - bounds.minA()) * (bounds.maxB() - bounds.minB());
        }).sum();
    }

    private static Bounds tangentBounds(
            List<CableGeometry.Vertex> vertices,
            Direction6 direction
    ) {
        return new Bounds(
                minimumTangentA(vertices, direction),
                maximumTangentA(vertices, direction),
                minimumTangentB(vertices, direction),
                maximumTangentB(vertices, direction)
        );
    }

    private static void assertHoleBoundary(
            List<CableGeometry.Vertex> vertices,
            Direction6 direction
    ) {
        assertTrue(vertices.stream().anyMatch(vertex ->
                equal(tangentA(vertex, direction), FacadeGeometry.HOLE_MIN_16)));
        assertTrue(vertices.stream().anyMatch(vertex ->
                equal(tangentA(vertex, direction), FacadeGeometry.HOLE_MAX_16)));
        assertTrue(vertices.stream().anyMatch(vertex ->
                equal(tangentB(vertex, direction), FacadeGeometry.HOLE_MIN_16)));
        assertTrue(vertices.stream().anyMatch(vertex ->
                equal(tangentB(vertex, direction), FacadeGeometry.HOLE_MAX_16)));
    }

    private static boolean equal(double first, double second) {
        return Math.abs(first - second) <= EPSILON;
    }

    private static double normal(CableGeometry.Vertex vertex, Direction6 direction) {
        return switch (direction) {
            case DOWN, UP -> vertex.y16();
            case NORTH, SOUTH -> vertex.z16();
            case WEST, EAST -> vertex.x16();
        };
    }

    private static double tangentA(CableGeometry.Vertex vertex, Direction6 direction) {
        return switch (direction) {
            case DOWN, UP, NORTH, SOUTH -> vertex.x16();
            case WEST, EAST -> vertex.z16();
        };
    }

    private static double tangentB(CableGeometry.Vertex vertex, Direction6 direction) {
        return switch (direction) {
            case DOWN, UP -> vertex.z16();
            case NORTH, SOUTH, WEST, EAST -> vertex.y16();
        };
    }

    private static Vector subtract(
            CableGeometry.Vertex first,
            CableGeometry.Vertex second
    ) {
        return new Vector(
                first.x16() - second.x16(),
                first.y16() - second.y16(),
                first.z16() - second.z16()
        );
    }

    private static Vector cross(Vector first, Vector second) {
        return new Vector(
                first.y() * second.z() - first.z() * second.y(),
                first.z() * second.x() - first.x() * second.z(),
                first.x() * second.y() - first.y() * second.x()
        );
    }

    private record Bounds(double minA, double maxA, double minB, double maxB) {
    }

    private record Vector(double x, double y, double z) {
    }

    private record Uv(double u, double v) {
    }
}
