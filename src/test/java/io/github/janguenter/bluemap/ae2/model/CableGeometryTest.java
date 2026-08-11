/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.model;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CableGeometryTest {

    private static final int MIN = 6;
    private static final int MAX = 10;
    private static final String EXPECTED_CATALOG_SIGNATURE =
            "587a467c82bc2ebef57c0a8211fddf7214a341837a24974d31a529313fc13606";

    @Test
    void directionOrderAndBitsMatchTheMinecraftDirectionContract() {
        assertEquals(
                List.of(
                        Direction6.DOWN,
                        Direction6.UP,
                        Direction6.NORTH,
                        Direction6.SOUTH,
                        Direction6.WEST,
                        Direction6.EAST
                ),
                List.of(Direction6.values())
        );
        for (Direction6 direction : Direction6.values()) {
            assertEquals(1 << direction.ordinal(), direction.maskBit());
            assertEquals(direction, direction.opposite().opposite());
            assertEquals(0, direction.stepX() + direction.opposite().stepX());
            assertEquals(0, direction.stepY() + direction.opposite().stepY());
            assertEquals(0, direction.stepZ() + direction.opposite().stepZ());
        }
    }

    @Test
    void allSixtyFourMasksHaveTheExactTopologySize() {
        for (int mask = 0; mask < 64; mask++) {
            int connections = Integer.bitCount(mask);
            int expected = isOppositePair(mask) ? 4 : 6 + 5 * connections;
            List<CableGeometry.Quad> quads = CableGeometry.forMask(mask);

            assertEquals(expected, quads.size(), "mask " + mask);
            for (CableGeometry.Quad quad : quads) {
                assertEquals(4, quad.vertices().size());
                for (CableGeometry.Vertex vertex : quad.vertices()) {
                    assertSixteenth(vertex.x16());
                    assertSixteenth(vertex.y16());
                    assertSixteenth(vertex.z16());
                    assertSixteenth(vertex.u16());
                    assertSixteenth(vertex.v16());
                }
            }
        }
    }

    @Test
    void isolatedCoreUsesExactBoundsVertexOrderAndStandardUvs() {
        List<CableGeometry.Quad> quads = CableGeometry.forMask(0);

        assertEquals(6, quads.size());
        CableGeometry.Quad down = quads.get(0);
        assertEquals(Direction6.DOWN, down.face());
        assertEquals(CableGeometry.TextureRole.CORE, down.textureRole());
        assertEquals(
                List.of(
                        new CableGeometry.Vertex(MIN, MIN, MAX, MIN, MIN),
                        new CableGeometry.Vertex(MIN, MIN, MIN, MIN, MAX),
                        new CableGeometry.Vertex(MAX, MIN, MIN, MAX, MAX),
                        new CableGeometry.Vertex(MAX, MIN, MAX, MAX, MIN)
                ),
                down.vertices()
        );
    }

    @Test
    void oneArmKeepsItsInternalFaceAndOmitsOnlyItsOutwardCap() {
        List<CableGeometry.Quad> quads = CableGeometry.forMask(Direction6.EAST.maskBit());
        List<CableGeometry.Quad> arm = quads.subList(6, quads.size());

        assertEquals(
                List.of(
                        Direction6.DOWN,
                        Direction6.UP,
                        Direction6.NORTH,
                        Direction6.SOUTH,
                        Direction6.WEST
                ),
                arm.stream().map(CableGeometry.Quad::face).toList()
        );
        assertTrue(arm.stream().allMatch(
                quad -> quad.textureRole() == CableGeometry.TextureRole.CONNECTION
        ));
        assertFalse(arm.stream().anyMatch(quad -> quad.face() == Direction6.EAST));

        CableGeometry.Quad north = arm.get(2);
        assertEquals(
                List.of(
                        new CableGeometry.Vertex(16, MAX, MIN, 0, MIN),
                        new CableGeometry.Vertex(16, MIN, MIN, 0, MAX),
                        new CableGeometry.Vertex(MAX, MIN, MIN, MIN, MAX),
                        new CableGeometry.Vertex(MAX, MAX, MIN, MIN, MIN)
                ),
                north.vertices()
        );
    }

    @Test
    void exactlyOppositeConnectionsUseOneUncappedFullSpanPrism() {
        int mask = Direction6.WEST.maskBit() | Direction6.EAST.maskBit();
        List<CableGeometry.Quad> quads = CableGeometry.forMask(mask);

        assertEquals(
                List.of(Direction6.DOWN, Direction6.UP, Direction6.NORTH, Direction6.SOUTH),
                quads.stream().map(CableGeometry.Quad::face).toList()
        );
        assertTrue(quads.stream().allMatch(
                quad -> quad.textureRole() == CableGeometry.TextureRole.CONNECTION
        ));
        assertTrue(quads.stream()
                .flatMap(quad -> quad.vertices().stream())
                .anyMatch(vertex -> vertex.x16() == 0));
        assertTrue(quads.stream()
                .flatMap(quad -> quad.vertices().stream())
                .anyMatch(vertex -> vertex.x16() == 16));
    }

    @Test
    void returnedCatalogAndQuadsAreDeeplyImmutable() {
        List<CableGeometry.Quad> quads = CableGeometry.forMask(0);

        assertThrows(UnsupportedOperationException.class, () -> quads.clear());
        assertThrows(UnsupportedOperationException.class, () -> quads.get(0).vertices().clear());
        assertThrows(IllegalArgumentException.class, () -> CableGeometry.forMask(-1));
        assertThrows(IllegalArgumentException.class, () -> CableGeometry.forMask(64));
        assertEquals(new CableGeometry.TemplateStats(66, 624, 2496),
                CableGeometry.templateStats());
    }

    @Test
    void everyMaskContributesToOneLockedDeterministicCatalogSignature() {
        assertEquals(EXPECTED_CATALOG_SIGNATURE, CableGeometry.catalogSignature());
        assertEquals(
                "0e7edb4b7bb2bfefbbab989ff8faffd952c2a8c120bc3434d5664d3162771c15",
                CableGeometry.signatureForMask(0)
        );
        assertEquals(
                "6add45309a5fe558eec5441b2ee091adf76a9db4ddd29b1bba13391e284ecdce",
                CableGeometry.signatureForMask(
                        Direction6.WEST.maskBit() | Direction6.EAST.maskBit()
                )
        );
        assertEquals(
                "76c230e93727660e559cc15e329549502d05436eeb30abcf795edde99b4dc3c6",
                CableGeometry.signatureForMask(63)
        );
    }

    @Test
    void concurrentCatalogReadsRemainDeterministic() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(8);
        try {
            List<Callable<String>> tasks = new ArrayList<>();
            for (int task = 0; task < 32; task++) {
                tasks.add(() -> {
                    for (int mask = 0; mask < 64; mask++) {
                        CableGeometry.forMask(mask).forEach(quad -> quad.vertices().size());
                        CableGeometry.signatureForMask(mask);
                    }
                    return CableGeometry.catalogSignature();
                });
            }
            List<Future<String>> futures = executor.invokeAll(tasks);
            for (Future<String> future : futures) {
                assertEquals(EXPECTED_CATALOG_SIGNATURE, future.get());
            }
        } finally {
            executor.shutdownNow();
        }
    }

    private static boolean isOppositePair(int mask) {
        if (Integer.bitCount(mask) != 2) {
            return false;
        }
        for (Direction6 direction : Direction6.values()) {
            if (mask == (direction.maskBit() | direction.opposite().maskBit())) {
                return true;
            }
        }
        return false;
    }

    private static void assertSixteenth(double value) {
        assertTrue(value >= -0.01 && value <= 16.01);
    }
}
