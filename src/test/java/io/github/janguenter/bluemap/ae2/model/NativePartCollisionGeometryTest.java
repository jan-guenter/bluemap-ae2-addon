/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.model;

import io.github.janguenter.bluemap.ae2.profile.Ae219217NativeStructuralProfile;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NativePartCollisionGeometryTest {

    private static final CableDefinition CENTER = Ae2CableCatalog.require(
            "ae2:fluix_covered_cable"
    );
    private static final FacadeSnapshot STONE = new FacadeSnapshot(
            "minecraft:stone",
            Map.of()
    );
    private static final List<String> PLANE_IDS = List.of(
            NativeStructuralPartCatalog.ANNIHILATION_PLANE,
            NativeStructuralPartCatalog.FORMATION_PLANE
    );

    @Test
    void allNativeCatalogBoxesOrientBoundedlyOnAllSixFaces() {
        int states = 0;
        for (NativeStructuralPartCatalog.Definition definition
                : NativeStructuralPartCatalog.definitions()) {
            for (Direction6 direction : Direction6.values()) {
                List<FacadeGeometry.Bounds> boxes = NativePartCollisionGeometry.boxes(
                        direction,
                        definition.id(),
                        0,
                        false
                );
                assertTrue(boxes.stream().allMatch(NativePartCollisionGeometryTest::bounded));
                states++;
            }
        }
        assertEquals(174, states);
    }

    @Test
    void exactOutwardIntersectingBoxesDriveRepresentativeFacadeCutouts() {
        assertTangents("ae2:quartz_fiber", 6, 10);
        assertTangents("ae2:toggle_bus", 6, 10);
        assertTangents("ae2:inverted_toggle_bus", 6, 10);
        assertTangents("ae2:import_bus", 4, 12);
        assertTangents("ae2:export_bus", 6, 10);
        assertTangents("ae2:level_emitter", 7, 9);
        assertTangents("ae2:energy_level_emitter", 7, 9);
    }

    @Test
    void sameFaceAnchorUsesShortBoxAndCreatesNoFacadeHole() {
        NativeStructuralSnapshot snapshot = snapshot(
                Direction6.UP,
                FacePartSnapshot.withoutSpin(NativeStructuralPartCatalog.CABLE_ANCHOR),
                Direction6.UP
        );
        assertEquals(
                new FacadeGeometry.Bounds(7, 10, 7, 9, 14, 9),
                NativePartCollisionGeometry.boxes(
                        Direction6.UP,
                        NativeStructuralPartCatalog.CABLE_ANCHOR,
                        0,
                        true
                ).getFirst()
        );
        assertNull(NativePartCollisionGeometry.cutout(
                snapshot,
                Direction6.UP,
                Direction6.UP.maskBit(),
                false
        ));
    }

    @Test
    void bothPlaneTypesUseExactFaceAwareCollisionBoundsForEveryMask() {
        int states = 0;
        for (String planeId : PLANE_IDS) {
            for (Direction6 installedFace : Direction6.values()) {
                for (int mask = 0; mask < 16; mask++) {
                    String context = planeId + " face=" + installedFace
                            + " mask=" + mask;
                    List<FacadeGeometry.Bounds> boxes =
                            NativePartCollisionGeometry.boxes(
                                    installedFace,
                                    planeId,
                                    mask,
                                    false
                            );
                    assertEquals(2, boxes.size(), context);
                    assertEquals(expectedPlaneSupport(installedFace), boxes.get(0), context);
                    assertEquals(expectedPlaneSlab(installedFace, mask), boxes.get(1), context);
                    states++;
                }
            }
        }
        assertEquals(192, states);
    }

    @Test
    void allSixteenPlaneMasksUseUpFaceCollisionPolarityForSameFaceCutout() {
        for (String planeId : PLANE_IDS) {
            for (int mask = 0; mask < 16; mask++) {
                NativeStructuralSnapshot snapshot = snapshot(
                        Direction6.UP,
                        FacePartSnapshot.withoutSpin(planeId),
                        Direction6.UP
                ).withPlaneConnectionMask(Direction6.UP, mask);
                FacadeGeometry.Bounds cutout = NativePartCollisionGeometry.cutout(
                        snapshot,
                        Direction6.UP,
                        Direction6.UP.maskBit(),
                        false
                );
                String context = planeId + " mask=" + mask;
                assertEquals(edgeMin(mask, 1), cutout.minX(), context);
                assertEquals(edgeMin(mask, 8), cutout.minZ(), context);
                assertEquals(edgeMax(mask, 4), cutout.maxX(), context);
                assertEquals(edgeMax(mask, 2), cutout.maxZ(), context);
            }
        }
    }

    @Test
    void northPlaneMaskEightCutsUpFacadeButOppositeMaskTwoDoesNot() {
        for (String planeId : PLANE_IDS) {
            NativeStructuralSnapshot base = snapshot(
                    Direction6.NORTH,
                    FacePartSnapshot.withoutSpin(planeId),
                    Direction6.UP
            );
            assertNull(NativePartCollisionGeometry.cutout(
                    base.withPlaneConnectionMask(Direction6.NORTH, 0),
                    Direction6.UP,
                    Direction6.UP.maskBit(),
                    false
            ), planeId);
            assertNull(NativePartCollisionGeometry.cutout(
                    base.withPlaneConnectionMask(Direction6.NORTH, 2),
                    Direction6.UP,
                    Direction6.UP.maskBit(),
                    false
            ), planeId);

            FacadeGeometry.Bounds cutout = NativePartCollisionGeometry.cutout(
                    base.withPlaneConnectionMask(Direction6.NORTH, 8),
                    Direction6.UP,
                    Direction6.UP.maskBit(),
                    false
            );
            assertEquals(
                    new FacadeGeometry.Bounds(1, 1, 0, 15, 16, 1),
                    cutout,
                    planeId
            );
        }
    }

    @Test
    void rejectsInvalidRuntimeAndProfilePlaneBindings() {
        assertThrows(NullPointerException.class, () -> NativePartCollisionGeometry.boxes(
                null,
                NativeStructuralPartCatalog.ANNIHILATION_PLANE,
                0,
                false
        ));
        assertThrows(IllegalArgumentException.class, () -> NativePartCollisionGeometry.boxes(
                Direction6.NORTH,
                NativeStructuralPartCatalog.ANNIHILATION_PLANE,
                -1,
                false
        ));
        assertThrows(IllegalArgumentException.class, () -> NativePartCollisionGeometry.boxes(
                Direction6.NORTH,
                NativeStructuralPartCatalog.ANNIHILATION_PLANE,
                16,
                false
        ));
        assertThrows(IllegalArgumentException.class,
                () -> Ae219217NativeStructuralProfile.planeCollisionBoxes(null, 0));
        assertThrows(IllegalArgumentException.class,
                () -> Ae219217NativeStructuralProfile.planeCollisionBoxes("UP", 0));
        assertThrows(IllegalArgumentException.class,
                () -> Ae219217NativeStructuralProfile.planeCollisionBoxes("diagonal", 0));
        assertThrows(IllegalArgumentException.class,
                () -> Ae219217NativeStructuralProfile.planeCollisionBoxes("north", -1));
        assertThrows(IllegalArgumentException.class,
                () -> Ae219217NativeStructuralProfile.planeCollisionBoxes("north", 16));
    }

    private static void assertTangents(String partId, double minimum, double maximum) {
        NativeStructuralSnapshot snapshot = snapshot(
                Direction6.UP,
                FacePartSnapshot.withoutSpin(partId),
                Direction6.UP
        );
        FacadeGeometry.Bounds cutout = NativePartCollisionGeometry.cutout(
                snapshot,
                Direction6.UP,
                Direction6.UP.maskBit(),
                false
        );
        assertEquals(minimum, cutout.minX(), partId);
        assertEquals(minimum, cutout.minZ(), partId);
        assertEquals(maximum, cutout.maxX(), partId);
        assertEquals(maximum, cutout.maxZ(), partId);
    }

    private static NativeStructuralSnapshot snapshot(
            Direction6 partFace,
            FacePartSnapshot part,
            Direction6 facadeFace
    ) {
        return NativeStructuralSnapshot.decoded(
                CENTER,
                Map.of(partFace, part),
                Map.of(facadeFace, STONE)
        );
    }

    private static boolean bounded(FacadeGeometry.Bounds bounds) {
        return bounds.minX() >= 0 && bounds.minY() >= 0 && bounds.minZ() >= 0
                && bounds.maxX() <= 16 && bounds.maxY() <= 16
                && bounds.maxZ() <= 16
                && bounds.minX() < bounds.maxX()
                && bounds.minY() < bounds.maxY()
                && bounds.minZ() < bounds.maxZ();
    }

    private static FacadeGeometry.Bounds expectedPlaneSupport(Direction6 installedFace) {
        return switch (installedFace) {
            case DOWN -> new FacadeGeometry.Bounds(5, 1, 5, 11, 2, 11);
            case UP -> new FacadeGeometry.Bounds(5, 14, 5, 11, 15, 11);
            case NORTH -> new FacadeGeometry.Bounds(5, 5, 1, 11, 11, 2);
            case SOUTH -> new FacadeGeometry.Bounds(5, 5, 14, 11, 11, 15);
            case WEST -> new FacadeGeometry.Bounds(1, 5, 5, 2, 11, 11);
            case EAST -> new FacadeGeometry.Bounds(14, 5, 5, 15, 11, 11);
        };
    }

    private static FacadeGeometry.Bounds expectedPlaneSlab(
            Direction6 installedFace,
            int mask
    ) {
        return switch (installedFace) {
            case DOWN -> new FacadeGeometry.Bounds(
                    edgeMin(mask, 4), 0, edgeMin(mask, 8),
                    edgeMax(mask, 1), 1, edgeMax(mask, 2)
            );
            case UP -> new FacadeGeometry.Bounds(
                    edgeMin(mask, 1), 15, edgeMin(mask, 8),
                    edgeMax(mask, 4), 16, edgeMax(mask, 2)
            );
            case NORTH -> new FacadeGeometry.Bounds(
                    edgeMin(mask, 4), edgeMin(mask, 2), 0,
                    edgeMax(mask, 1), edgeMax(mask, 8), 1
            );
            case SOUTH -> new FacadeGeometry.Bounds(
                    edgeMin(mask, 1), edgeMin(mask, 2), 15,
                    edgeMax(mask, 4), edgeMax(mask, 8), 16
            );
            case WEST -> new FacadeGeometry.Bounds(
                    0, edgeMin(mask, 2), edgeMin(mask, 1),
                    1, edgeMax(mask, 8), edgeMax(mask, 4)
            );
            case EAST -> new FacadeGeometry.Bounds(
                    15, edgeMin(mask, 2), edgeMin(mask, 4),
                    16, edgeMax(mask, 8), edgeMax(mask, 1)
            );
        };
    }

    private static double edgeMin(int mask, int bit) {
        return (mask & bit) == 0 ? 1 : 0;
    }

    private static double edgeMax(int mask, int bit) {
        return (mask & bit) == 0 ? 15 : 16;
    }
}
