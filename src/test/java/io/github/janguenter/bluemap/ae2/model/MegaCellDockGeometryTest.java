/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.model;

import org.junit.jupiter.api.Test;

import java.util.EnumSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MegaCellDockGeometryTest {

    private static final double EPSILON = 1.0E-12;

    @Test
    void exactSpinMappingAndBothTransformOrdersCoverAllTwentyFourOrientations() {
        assertEquals(Direction6.NORTH, MegaCellDockGeometry.upFromSpin(Direction6.DOWN, 0));
        assertEquals(Direction6.WEST, MegaCellDockGeometry.upFromSpin(Direction6.DOWN, 1));
        assertEquals(Direction6.NORTH, MegaCellDockGeometry.upFromSpin(Direction6.EAST, 1));
        assertEquals(Direction6.SOUTH, MegaCellDockGeometry.upFromSpin(Direction6.EAST, 3));

        for (Direction6 side : Direction6.values()) {
            for (int spin = 0; spin < 4; spin++) {
                Direction6 up = MegaCellDockGeometry.upFromSpin(side, spin);
                MegaCellDockGeometry.Transform cell =
                        MegaCellDockGeometry.cellTransform(side, spin);
                assertEquals(new MegaCellDockGeometry.Orientation(up, side), cell.orientation());
                assertPosition(cell.centerTranslation(), 0.5, 0.5, 0.5);
                assertPosition(cell.localTranslation(), -3.0 / 16, 5.0 / 16, -4.0 / 16);

                MegaCellDockGeometry.Transform second =
                        MegaCellDockGeometry.secondLedTransform(side, spin);
                assertEquals(new MegaCellDockGeometry.Orientation(side, up), second.orientation());
                assertPosition(second.localTranslation(), -8.0 / 16, -3.0 / 16, -8.0 / 16);
            }
        }
    }

    @Test
    void exactLedHasFiveFacesNoBackAndOfflineBlackPolicy() {
        assertEquals(5, MegaCellDockGeometry.offlineUnknownLed().size());
        assertEquals(
                EnumSet.of(
                        Direction6.NORTH,
                        Direction6.EAST,
                        Direction6.WEST,
                        Direction6.UP,
                        Direction6.DOWN
                ),
                MegaCellDockGeometry.offlineUnknownLed().stream()
                        .map(MegaCellDockGeometry.LedQuad::face)
                        .collect(java.util.stream.Collectors.toSet())
        );
        assertEquals(0x000000, MegaCellDockGeometry.OFFLINE_UNKNOWN_LED_RGB);
        assertPosition(
                MegaCellDockGeometry.offlineUnknownLed().get(0).vertices().get(0),
                4.0 / 16,
                1.0 / 16,
                -0.001 / 16
        );
    }

    @Test
    void emptyStandardAndMiscModelsLockExactNominalTriangleCounts() {
        MegaCellDockSnapshot empty = MegaCellDockSnapshot.empty(Direction6.NORTH, 0);
        MegaCellDockSnapshot standard = MegaCellDockSnapshot.occupied(
                Direction6.UP,
                2,
                MegaCellDockCellCatalog.require("megacells:item_storage_cell_1m")
        );
        MegaCellDockSnapshot misc = MegaCellDockSnapshot.occupied(
                Direction6.WEST,
                3,
                MegaCellDockCellCatalog.require("megacells:bulk_item_cell")
        );
        assertEquals(50, MegaCellDockGeometry.nominalTriangleCount(empty));
        assertEquals(82, MegaCellDockGeometry.nominalTriangleCount(standard));
        assertEquals(76, MegaCellDockGeometry.nominalTriangleCount(misc));

        MegaCellDockModel model = MegaCellDockGeometry.model(standard);
        assertTrue(model.cellChassis().isPresent());
        assertEquals(2, model.offlineLeds().size());
        assertEquals(82, model.nominalTriangleCount());
        assertEquals("megacells:part/cell_dock", model.bodyModelId());
    }

    @Test
    void snapshotAndGeometryRejectMalformedState() {
        assertThrows(
                IllegalArgumentException.class,
                () -> MegaCellDockSnapshot.empty(Direction6.NORTH, -1)
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> MegaCellDockGeometry.upFromSpin(Direction6.NORTH, 4)
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> new MegaCellDockGeometry.Orientation(Direction6.NORTH, Direction6.SOUTH)
        );
        assertThrows(
                UnsupportedOperationException.class,
                () -> MegaCellDockGeometry.offlineUnknownLed().clear()
        );
        assertThrows(
                UnsupportedOperationException.class,
                () -> MegaCellDockGeometry.offlineUnknownLed().get(0).vertices().clear()
        );
    }

    private static void assertPosition(
            MegaCellDockGeometry.Position actual,
            double x,
            double y,
            double z
    ) {
        assertEquals(x, actual.x(), EPSILON);
        assertEquals(y, actual.y(), EPSILON);
        assertEquals(z, actual.z(), EPSILON);
    }
}
