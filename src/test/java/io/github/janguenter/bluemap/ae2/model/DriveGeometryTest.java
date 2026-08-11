/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.model;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DriveGeometryTest {

    private static final double EPSILON = 1.0E-12;

    private static final List<DriveGeometry.SlotOrigin> EXPECTED_ORIGINS = List.of(
            origin(9, 13, 1),
            origin(1, 13, 1),
            origin(9, 10, 1),
            origin(1, 10, 1),
            origin(9, 7, 1),
            origin(1, 7, 1),
            origin(9, 4, 1),
            origin(1, 4, 1),
            origin(9, 1, 1),
            origin(1, 1, 1)
    );

    @Test
    void matchesAllTenExactRowMajorSlotOriginsInSixteenths() {
        for (int slot = 0; slot < DriveInventoryProjection.SLOT_COUNT; slot++) {
            assertEquals(EXPECTED_ORIGINS.get(slot), DriveGeometry.slotOrigin(slot));
        }
    }

    @Test
    void eachOfflineUnknownLedHasTheExactFiveFacesAndNoBack() {
        for (int slot = 0; slot < DriveInventoryProjection.SLOT_COUNT; slot++) {
            List<DriveGeometry.LedQuad> quads = DriveGeometry.offlineUnknownLed(slot);
            assertEquals(5, quads.size());
            assertEquals(
                    EnumSet.of(
                            Direction6.NORTH,
                            Direction6.EAST,
                            Direction6.WEST,
                            Direction6.UP,
                            Direction6.DOWN
                    ),
                    quads.stream().map(DriveGeometry.LedQuad::face)
                            .collect(java.util.stream.Collectors.toSet())
            );
        }
        assertEquals(0x000000, DriveGeometry.OFFLINE_UNKNOWN_LED_RGB);
    }

    @Test
    void slotZeroVerticesMatchTheExactAe2LedArray() {
        DriveGeometry.SlotOrigin origin = DriveGeometry.slotOrigin(0);
        List<DriveGeometry.LedQuad> quads = DriveGeometry.offlineUnknownLed(0);
        List<List<DriveGeometry.Position>> expected = List.of(
                points(origin,
                        point(4, 1, -0.001), point(5, 1, -0.001),
                        point(5, -0.001, -0.001), point(4, -0.001, -0.001)),
                points(origin,
                        point(5, 1, -0.001), point(5, 1, 0.999),
                        point(5, -0.001, 0.999), point(5, -0.001, -0.001)),
                points(origin,
                        point(4, 1, 0.999), point(4, 1, -0.001),
                        point(4, -0.001, -0.001), point(4, -0.001, 0.999)),
                points(origin,
                        point(4, 1, 0.999), point(5, 1, 0.999),
                        point(5, 1, -0.001), point(4, 1, -0.001)),
                points(origin,
                        point(4, -0.001, -0.001), point(5, -0.001, -0.001),
                        point(5, -0.001, 0.999), point(4, -0.001, 0.999))
        );

        for (int index = 0; index < expected.size(); index++) {
            assertPositions(expected.get(index), quads.get(index).vertices());
        }
    }

    @Test
    void allSlotsAreExactTranslationsOfSlotZeroAndHaveOutwardWinding() {
        DriveGeometry.SlotOrigin baseOrigin = DriveGeometry.slotOrigin(0);
        List<DriveGeometry.LedQuad> base = DriveGeometry.offlineUnknownLed(0);
        for (int slot = 0; slot < DriveInventoryProjection.SLOT_COUNT; slot++) {
            DriveGeometry.SlotOrigin origin = DriveGeometry.slotOrigin(slot);
            List<DriveGeometry.LedQuad> translated = DriveGeometry.offlineUnknownLed(slot);
            for (int quadIndex = 0; quadIndex < base.size(); quadIndex++) {
                DriveGeometry.LedQuad baseQuad = base.get(quadIndex);
                DriveGeometry.LedQuad translatedQuad = translated.get(quadIndex);
                assertEquals(baseQuad.face(), translatedQuad.face());
                for (int vertex = 0; vertex < 4; vertex++) {
                    DriveGeometry.Position basePosition = baseQuad.vertices().get(vertex);
                    DriveGeometry.Position position = translatedQuad.vertices().get(vertex);
                    assertEquals(
                            basePosition.x16() + origin.x16() - baseOrigin.x16(),
                            position.x16(),
                            EPSILON
                    );
                    assertEquals(
                            basePosition.y16() + origin.y16() - baseOrigin.y16(),
                            position.y16(),
                            EPSILON
                    );
                    assertEquals(
                            basePosition.z16() + origin.z16() - baseOrigin.z16(),
                            position.z16(),
                            EPSILON
                    );
                }
                assertOutwardWinding(translatedQuad);
            }
        }
    }

    @Test
    void cachedGeometryAndCopiedQuadVerticesAreImmutable() {
        List<DriveGeometry.LedQuad> led = DriveGeometry.offlineUnknownLed(0);
        assertThrows(UnsupportedOperationException.class, led::clear);
        assertThrows(UnsupportedOperationException.class, led.get(0).vertices()::clear);

        ArrayList<DriveGeometry.Position> mutable = new ArrayList<>(led.get(0).vertices());
        DriveGeometry.LedQuad copied = new DriveGeometry.LedQuad(Direction6.NORTH, mutable);
        mutable.clear();
        assertEquals(4, copied.vertices().size());
        assertThrows(UnsupportedOperationException.class, copied.vertices()::clear);
    }

    @Test
    void rejectsInvalidSlotsCoordinatesAndQuadShapes() {
        assertThrows(IllegalArgumentException.class, () -> DriveGeometry.slotOrigin(-1));
        assertThrows(IllegalArgumentException.class, () -> DriveGeometry.slotOrigin(10));
        assertThrows(IllegalArgumentException.class, () -> DriveGeometry.offlineUnknownLed(-1));
        assertThrows(IllegalArgumentException.class, () -> DriveGeometry.offlineUnknownLed(10));
        assertThrows(
                IllegalArgumentException.class,
                () -> new DriveGeometry.Position(Double.NaN, 0, 0)
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> new DriveGeometry.SlotOrigin(0, Double.POSITIVE_INFINITY, 0)
        );
        assertThrows(
                NullPointerException.class,
                () -> new DriveGeometry.LedQuad(null, List.of())
        );
        assertThrows(
                NullPointerException.class,
                () -> new DriveGeometry.LedQuad(Direction6.NORTH, null)
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> new DriveGeometry.LedQuad(Direction6.NORTH, List.of())
        );
    }

    private static void assertOutwardWinding(DriveGeometry.LedQuad quad) {
        DriveGeometry.Position first = quad.vertices().get(0);
        DriveGeometry.Position second = quad.vertices().get(1);
        DriveGeometry.Position third = quad.vertices().get(2);
        Vector edgeA = subtract(second, first);
        Vector edgeB = subtract(third, first);
        Vector normal = cross(edgeA, edgeB);
        double outward = normal.x() * quad.face().stepX()
                + normal.y() * quad.face().stepY()
                + normal.z() * quad.face().stepZ();
        assertTrue(outward > EPSILON, quad.face().name());
    }

    private static void assertPositions(
            List<DriveGeometry.Position> expected,
            List<DriveGeometry.Position> actual
    ) {
        assertEquals(expected.size(), actual.size());
        for (int index = 0; index < expected.size(); index++) {
            assertEquals(expected.get(index).x16(), actual.get(index).x16(), EPSILON);
            assertEquals(expected.get(index).y16(), actual.get(index).y16(), EPSILON);
            assertEquals(expected.get(index).z16(), actual.get(index).z16(), EPSILON);
        }
    }

    private static List<DriveGeometry.Position> points(
            DriveGeometry.SlotOrigin origin,
            DriveGeometry.Position... relative
    ) {
        return java.util.Arrays.stream(relative)
                .map(position -> point(
                        position.x16() + origin.x16(),
                        position.y16() + origin.y16(),
                        position.z16() + origin.z16()
                ))
                .toList();
    }

    private static DriveGeometry.Position point(double x16, double y16, double z16) {
        return new DriveGeometry.Position(x16, y16, z16);
    }

    private static DriveGeometry.SlotOrigin origin(double x16, double y16, double z16) {
        return new DriveGeometry.SlotOrigin(x16, y16, z16);
    }

    private static Vector subtract(
            DriveGeometry.Position first,
            DriveGeometry.Position second
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

    private record Vector(double x, double y, double z) {
    }
}
