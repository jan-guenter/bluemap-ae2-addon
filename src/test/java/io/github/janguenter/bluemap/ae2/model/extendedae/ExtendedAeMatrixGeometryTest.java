/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.model.extendedae;

import io.github.janguenter.bluemap.ae2.model.Direction6;
import io.github.janguenter.bluemap.ae2.model.extendedae.ExtendedAeMatrixGlassGeometry.Corner;
import io.github.janguenter.bluemap.ae2.model.extendedae.ExtendedAeMatrixGlassSnapshot.Offset;
import io.github.janguenter.bluemap.ae2.model.extendedae.ExtendedAeMatrixSnapshot.FrameShape;
import io.github.janguenter.bluemap.ae2.profile.extendedae.ExtendedAe2235Catalog.MatrixKind;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExtendedAeMatrixGeometryTest {

    @Test
    void preservesMatrixStateButForcesPowerOff() {
        ExtendedAeMatrixSnapshot frame = new ExtendedAeMatrixSnapshot(
                MatrixKind.FRAME,
                true,
                true,
                FrameShape.COLUMN_X
        );
        ExtendedAeMatrixSnapshot projected = frame.staticProjection();
        assertTrue(projected.formed());
        assertFalse(projected.powered());
        assertEquals(FrameShape.COLUMN_X, projected.frameShape());
        assertEquals(
                "assets/extendedae/models/block/assembler_matrix/frame_column_off.json",
                projected.staticModelSelection().modelResource()
        );
        assertEquals(90, projected.staticModelSelection().xRotation());
        assertEquals(90, projected.staticModelSelection().yRotation());
        assertEquals(90, new ExtendedAeMatrixSnapshot(
                MatrixKind.FRAME, false, false, FrameShape.COLUMN_Z
        ).staticModelSelection().xRotation());
        assertThrows(IllegalArgumentException.class, () -> new ExtendedAeMatrixSnapshot(
                MatrixKind.WALL, false, false, FrameShape.COLUMN_Y
        ));
    }

    @Test
    void scansAllTwentySevenAppearanceCellsAndSelectsCoordinateFace() {
        assertEquals(27, ExtendedAeMatrixGlassSnapshot.scanOffsets().size());
        assertTrue(ExtendedAeMatrixGlassSnapshot.scanOffsets().contains(new Offset(0, 0, 0)));
        assertEquals(0, snapshot(0, 0, 0, Set.of()).faceTextureIndex());
        assertEquals(1, snapshot(-1, 0, 0, Set.of()).faceTextureIndex());
        assertEquals(2, snapshot(Integer.MIN_VALUE, 0, 0, Set.of()).faceTextureIndex());
        assertThrows(IllegalArgumentException.class, () -> new Offset(2, 0, 0));

        Map<Offset, String> observations = new LinkedHashMap<>();
        observations.put(Offset.direct(Direction6.EAST),
                "extendedae:assembler_matrix_glass");
        observations.put(Offset.direct(Direction6.WEST),
                "extendedae:assembler_matrix_wall");
        observations.put(Offset.direct(Direction6.NORTH), null);
        ExtendedAeMatrixGlassSnapshot classified =
                ExtendedAeMatrixGlassSnapshot.fromAppearanceBlockIds(
                        true, false, 0, 0, 0, observations
                );
        assertTrue(classified.faceBlocked(Direction6.EAST));
        assertFalse(classified.faceBlocked(Direction6.WEST));
        assertFalse(classified.faceBlocked(Direction6.NORTH));
    }

    @Test
    void portsExactDynamicGlassQuadsAndCulling() {
        var isolated = ExtendedAeMatrixGlassGeometry.forSnapshot(
                snapshot(0, 0, 0, Set.of())
        );
        assertEquals(30, isolated.size());
        assertEquals(ExtendedAeMatrixGlassGeometry.RenderLayer.CUTOUT,
                isolated.getFirst().material().renderLayer());
        assertFalse(isolated.getFirst().material().emissive());
        assertFalse(isolated.getFirst().material().ambientOcclusion());
        assertFalse(isolated.getFirst().material().usesBlockLight());
        assertEquals(0, isolated.getFirst().material().animationFrame());
        ExtendedAeMatrixGlassSnapshot east = snapshot(
                0, 0, 0, Set.of(Offset.direct(Direction6.EAST))
        );
        assertTrue(ExtendedAeMatrixGlassGeometry.forSnapshot(east).stream()
                .noneMatch(quad -> quad.face() == Direction6.EAST));

        Set<Offset> surrounded = new LinkedHashSet<>(
                ExtendedAeMatrixGlassSnapshot.scanOffsets()
        );
        surrounded.remove(new Offset(0, 0, 0));
        assertTrue(ExtendedAeMatrixGlassGeometry.forSnapshot(
                snapshot(0, 0, 0, surrounded)
        ).isEmpty());
    }

    @Test
    void portsExactSideIndicesAndVerticalFaceCoordinates() {
        assertEquals(0, ExtendedAeMatrixGlassGeometry.sideTile(false, false, false));
        assertEquals(0, ExtendedAeMatrixGlassGeometry.sideTile(false, false, true));
        assertEquals(1, ExtendedAeMatrixGlassGeometry.sideTile(true, true, false));
        assertEquals(-1, ExtendedAeMatrixGlassGeometry.sideTile(true, true, true));
        assertEquals(2, ExtendedAeMatrixGlassGeometry.sideTile(false, true, false));
        assertEquals(3, ExtendedAeMatrixGlassGeometry.sideTile(true, false, false));

        ExtendedAeMatrixGlassGeometry.Check up = ExtendedAeMatrixGlassGeometry.check(
                Direction6.UP,
                Corner.LEFT_UP
        );
        assertEquals(new Offset(0, 0, 1), up.first());
        assertEquals(new Offset(-1, 0, 0), up.second());
        assertEquals(new Offset(-1, 0, 1), up.diagonal());
        assertThrows(IllegalArgumentException.class, () ->
                ExtendedAeMatrixGlassGeometry.check(Direction6.UP, Corner.FULL));
    }

    private static ExtendedAeMatrixGlassSnapshot snapshot(
            int x,
            int y,
            int z,
            Set<Offset> matchingNeighbors
    ) {
        return ExtendedAeMatrixGlassSnapshot.observed(
                true,
                true,
                x,
                y,
                z,
                matchingNeighbors
        );
    }
}
