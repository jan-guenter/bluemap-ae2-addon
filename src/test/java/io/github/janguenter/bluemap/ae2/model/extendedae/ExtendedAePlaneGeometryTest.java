/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.model.extendedae;

import io.github.janguenter.bluemap.ae2.model.Direction6;
import io.github.janguenter.bluemap.ae2.model.extendedae.ExtendedAePlaneGeometry.Bounds;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExtendedAePlaneGeometryTest {

    @Test
    void appliesAllFourExactNativeMaskBits() {
        assertEquals(new Bounds(1, 1, 0, 15, 15, 1),
                ExtendedAePlaneGeometry.plateBounds(0));
        assertEquals(new Bounds(0, 0, 0, 16, 16, 1),
                ExtendedAePlaneGeometry.plateBounds(15));
        assertEquals(new Bounds(1, 1, 0, 16, 15, 1),
                ExtendedAePlaneGeometry.plateBounds(1));
        assertEquals(new Bounds(1, 0, 0, 15, 15, 1),
                ExtendedAePlaneGeometry.plateBounds(2));
        assertEquals(new Bounds(0, 1, 0, 15, 15, 1),
                ExtendedAePlaneGeometry.plateBounds(4));
        assertEquals(new Bounds(1, 1, 0, 15, 16, 1),
                ExtendedAePlaneGeometry.plateBounds(8));
        assertThrows(IllegalArgumentException.class, () ->
                ExtendedAePlaneGeometry.plateBounds(16));
    }

    @Test
    void projectsBothPlanesOffWithoutInventingSpin() {
        ExtendedAePlaneSnapshot raw = new ExtendedAePlaneSnapshot(
                "extendedae:active_formation_plane",
                Direction6.NORTH,
                true,
                true,
                5
        );
        ExtendedAePlaneSnapshot projected = raw.staticProjection();
        assertFalse(projected.active());
        assertFalse(projected.powered());
        assertEquals(5, projected.connectionMask());
        assertTrue(Arrays.stream(ExtendedAePlaneSnapshot.class.getRecordComponents())
                .noneMatch(component -> component.getName().equals("spin")));

        ExtendedAePlaneGeometry.Geometry geometry = ExtendedAePlaneGeometry.forSnapshot(raw);
        assertEquals("extendedae:part/active_formation_plane", geometry.frontTexture());
        assertEquals("ae2:part/plane_sides", geometry.sideTexture());
        assertEquals("ae2:part/transition_plane_back", geometry.backTexture());
        assertEquals(1, geometry.cableConnectionLength());
        assertEquals(6, geometry.surfaces().size());
    }

    @Test
    void rejectsUnknownPartsAndInvalidMasks() {
        assertThrows(IllegalArgumentException.class, () -> new ExtendedAePlaneSnapshot(
                "extendedae:not_plane",
                Direction6.UP,
                false,
                false,
                0
        ));
        assertThrows(IllegalArgumentException.class, () -> new ExtendedAePlaneSnapshot(
                "extendedae:smart_annihilation_plane",
                Direction6.UP,
                false,
                false,
                -1
        ));
    }
}
