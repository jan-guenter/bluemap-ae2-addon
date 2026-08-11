/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CableGeometryM1Test {

    @Test
    void isolatedFamiliesUseTheThreeExactCoreBounds() {
        assertBounds(isolated("ae2:red_glass_cable"), 6, 10);
        assertBounds(isolated("ae2:red_covered_cable"), 5, 11);
        assertBounds(isolated("ae2:red_smart_cable"), 5, 11);
        assertBounds(isolated("ae2:red_covered_dense_cable"), 3, 13);
        assertBounds(isolated("ae2:red_smart_dense_cable"), 3, 13);
    }

    @Test
    void oneArmUsesVisibleFamilyGeometryAndSmartOffLayers() {
        CableBusSnapshot smart = isolated("ae2:blue_smart_cable")
                .withConnection(Direction6.EAST, CableFamily.SMART);
        List<CableGeometry.Quad> smartQuads = CableGeometry.forSnapshot(smart);
        assertEquals(21, smartQuads.size());
        assertEquals(5, count(smartQuads, CableGeometry.TextureRole.CONNECTION));
        assertEquals(5, count(smartQuads, CableGeometry.TextureRole.SMART_CHANNELS_ODD));
        assertEquals(5, count(smartQuads, CableGeometry.TextureRole.SMART_CHANNELS_EVEN));
        assertTrue(smartQuads.stream()
                .filter(quad -> quad.textureRole().name().startsWith("SMART_CHANNELS"))
                .allMatch(CableGeometry.Quad::emissive));

        CableBusSnapshot downgraded = isolated("ae2:blue_smart_dense_cable")
                .withConnection(Direction6.EAST, CableFamily.COVERED);
        List<CableGeometry.Quad> coveredQuads = CableGeometry.forSnapshot(downgraded);
        assertEquals(11, coveredQuads.size());
        assertTrue(coveredQuads.stream().skip(6)
                .allMatch(quad -> quad.materialFamily() == CableFamily.COVERED));
        assertFalse(coveredQuads.stream().anyMatch(CableGeometry.Quad::emissive));
    }

    @Test
    void straightOptimizationRequiresOppositeLocalEffectiveTypes() {
        for (CableFamily family : CableFamily.values()) {
            CableBusSnapshot straight = CableBusSnapshot.isolated(definition(family))
                    .withConnection(Direction6.WEST, family)
                    .withConnection(Direction6.EAST, family);
            int expected = switch (family) {
                case GLASS -> 4;
                case COVERED, DENSE_COVERED -> 6;
                case SMART, DENSE_SMART -> 18;
            };
            assertEquals(expected, CableGeometry.forSnapshot(straight).size());
        }

        CableBusSnapshot mixed = isolated("ae2:fluix_smart_cable")
                .withConnection(Direction6.WEST, CableFamily.COVERED)
                .withConnection(Direction6.EAST, CableFamily.SMART);
        assertEquals(26, CableGeometry.forSnapshot(mixed).size());
    }

    @Test
    void denseStraightUsesTheExactFacadeZFightExtension() {
        CableBusSnapshot straight = isolated("ae2:fluix_covered_dense_cable")
                .withConnection(Direction6.NORTH, CableFamily.DENSE_COVERED)
                .withConnection(Direction6.SOUTH, CableFamily.DENSE_COVERED);
        List<CableGeometry.Vertex> vertices = CableGeometry.forSnapshot(straight)
                .stream().flatMap(quad -> quad.vertices().stream()).toList();
        assertTrue(vertices.stream().anyMatch(vertex -> vertex.z16() == -0.01));
        assertTrue(vertices.stream().anyMatch(vertex -> vertex.z16() == 16.01));
    }

    @Test
    void coveredAndDenseStraightUvsIncludeTheExactCustomRectsAndRotations() {
        CableBusSnapshot covered = isolated("ae2:fluix_covered_cable")
                .withConnection(Direction6.WEST, CableFamily.COVERED)
                .withConnection(Direction6.EAST, CableFamily.COVERED);
        List<CableGeometry.Quad> coveredQuads = CableGeometry.forSnapshot(covered);
        assertEquals(
                List.of(new Uv(0, 5), new Uv(0, 11), new Uv(5, 11), new Uv(5, 5)),
                uvs(coveredQuads.get(0))
        );
        assertEquals(
                List.of(new Uv(5, 5), new Uv(5, 11), new Uv(0, 11), new Uv(0, 5)),
                uvs(coveredQuads.get(2))
        );

        CableBusSnapshot dense = isolated("ae2:fluix_covered_dense_cable")
                .withConnection(Direction6.NORTH, CableFamily.DENSE_COVERED)
                .withConnection(Direction6.SOUTH, CableFamily.DENSE_COVERED);
        List<CableGeometry.Quad> denseQuads = CableGeometry.forSnapshot(dense);
        assertEquals(
                List.of(new Uv(3, 3), new Uv(3, 13), new Uv(0, 13), new Uv(0, 3)),
                uvs(denseQuads.get(5))
        );
    }

    @Test
    void templatesAreFixedBoundedAndReturnedCompositionsAreImmutable() {
        assertEquals(
                new CableGeometry.TemplateStats(66, 624, 2496),
                CableGeometry.templateStats()
        );
        List<CableGeometry.Quad> geometry = CableGeometry.forSnapshot(
                isolated("ae2:orange_smart_dense_cable")
                        .withConnection(Direction6.DOWN, CableFamily.DENSE_SMART)
                        .withConnection(Direction6.UP, CableFamily.DENSE_SMART)
        );
        assertThrows(UnsupportedOperationException.class, geometry::clear);
        assertThrows(UnsupportedOperationException.class,
                () -> geometry.get(0).vertices().clear());
    }

    private static CableBusSnapshot isolated(String id) {
        return CableBusSnapshot.isolated(Ae2CableCatalog.require(id));
    }

    private static CableDefinition definition(CableFamily family) {
        return Ae2CableCatalog.definitions().stream()
                .filter(value -> value.family() == family
                        && value.color() == CableColor.TRANSPARENT)
                .findFirst()
                .orElseThrow();
    }

    private static long count(
            List<CableGeometry.Quad> quads,
            CableGeometry.TextureRole role
    ) {
        return quads.stream().filter(quad -> quad.textureRole() == role).count();
    }

    private static void assertBounds(
            CableBusSnapshot snapshot,
            double minimum,
            double maximum
    ) {
        List<CableGeometry.Vertex> vertices = CableGeometry.forSnapshot(snapshot)
                .stream().flatMap(quad -> quad.vertices().stream()).toList();
        assertEquals(minimum, vertices.stream().mapToDouble(CableGeometry.Vertex::x16)
                .min().orElseThrow());
        assertEquals(minimum, vertices.stream().mapToDouble(CableGeometry.Vertex::y16)
                .min().orElseThrow());
        assertEquals(minimum, vertices.stream().mapToDouble(CableGeometry.Vertex::z16)
                .min().orElseThrow());
        assertEquals(maximum, vertices.stream().mapToDouble(CableGeometry.Vertex::x16)
                .max().orElseThrow());
        assertEquals(maximum, vertices.stream().mapToDouble(CableGeometry.Vertex::y16)
                .max().orElseThrow());
        assertEquals(maximum, vertices.stream().mapToDouble(CableGeometry.Vertex::z16)
                .max().orElseThrow());
    }

    private static List<Uv> uvs(CableGeometry.Quad quad) {
        return quad.vertices().stream()
                .map(vertex -> new Uv(vertex.u16(), vertex.v16()))
                .toList();
    }

    private record Uv(double u, double v) {
    }
}
