/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.model;

import java.util.List;
import java.util.Objects;

/** Exact dependency-free Cell Dock dynamic transforms and offline LED geometry. */
public final class MegaCellDockGeometry {

    public static final int OFFLINE_UNKNOWN_LED_RGB = 0x000000;
    public static final int BODY_NOMINAL_TRIANGLES = 50;
    public static final int LED_NOMINAL_TRIANGLES = 10;

    private static final Position CENTER = new Position(0.5, 0.5, 0.5);
    private static final Position CELL_TRANSLATION =
            new Position(-3.0 / 16.0, 5.0 / 16.0, -4.0 / 16.0);
    private static final Position SECOND_LED_TRANSLATION =
            new Position(-8.0 / 16.0, -3.0 / 16.0, -8.0 / 16.0);
    private static final Direction6[][] SPIN_DIRECTIONS = {
            {Direction6.NORTH, Direction6.WEST, Direction6.SOUTH, Direction6.EAST},
            {Direction6.NORTH, Direction6.EAST, Direction6.SOUTH, Direction6.WEST},
            {Direction6.UP, Direction6.WEST, Direction6.DOWN, Direction6.EAST},
            {Direction6.UP, Direction6.EAST, Direction6.DOWN, Direction6.WEST},
            {Direction6.UP, Direction6.SOUTH, Direction6.DOWN, Direction6.NORTH},
            {Direction6.UP, Direction6.NORTH, Direction6.DOWN, Direction6.SOUTH}
    };
    private static final List<LedQuad> OFFLINE_LED = buildOfflineLed();

    private MegaCellDockGeometry() {
    }

    /** Exact AE2 {@code SpinMapping.getUpFromSpin} table. */
    public static Direction6 upFromSpin(Direction6 side, int spin) {
        Objects.requireNonNull(side, "side");
        return SPIN_DIRECTIONS[side.ordinal()][requireSpin(spin)];
    }

    /** {@code T(.5) * BlockOrientation.get(upFromSpin(side, spin), side) * T(cell)}. */
    public static Transform cellTransform(Direction6 side, int spin) {
        Direction6 up = upFromSpin(side, spin);
        return new Transform(CENTER, new Orientation(up, side), CELL_TRANSLATION);
    }

    /** The first LED is rendered in the same transformed space as the cell chassis. */
    public static Transform firstLedTransform(Direction6 side, int spin) {
        return cellTransform(side, spin);
    }

    /** {@code T(.5) * BlockOrientation.get(side, spin) * T(-8/16,-3/16,-8/16)}. */
    public static Transform secondLedTransform(Direction6 side, int spin) {
        Direction6 up = upFromSpin(side, spin);
        return new Transform(CENTER, new Orientation(side, up), SECOND_LED_TRANSLATION);
    }

    /** Five exact position-color quads; the LED intentionally has no back face. */
    public static List<LedQuad> offlineUnknownLed() {
        return OFFLINE_LED;
    }

    public static int nominalTriangleCount(MegaCellDockSnapshot snapshot) {
        Objects.requireNonNull(snapshot, "snapshot");
        return snapshot.cell()
                .map(cell -> BODY_NOMINAL_TRIANGLES
                        + cell.chassisKind().nominalTriangles()
                        + 2 * LED_NOMINAL_TRIANGLES)
                .orElse(BODY_NOMINAL_TRIANGLES);
    }

    public static MegaCellDockModel model(MegaCellDockSnapshot snapshot) {
        return MegaCellDockModel.from(snapshot);
    }

    private static List<LedQuad> buildOfflineLed() {
        double left = 5.0 / 16.0;
        double right = 4.0 / 16.0;
        double top = 1.0 / 16.0;
        double bottom = -0.001 / 16.0;
        double front = -0.001 / 16.0;
        double back = 0.999 / 16.0;
        return List.of(
                quad(Direction6.NORTH,
                        point(right, top, front), point(left, top, front),
                        point(left, bottom, front), point(right, bottom, front)),
                quad(Direction6.EAST,
                        point(left, top, front), point(left, top, back),
                        point(left, bottom, back), point(left, bottom, front)),
                quad(Direction6.WEST,
                        point(right, top, back), point(right, top, front),
                        point(right, bottom, front), point(right, bottom, back)),
                quad(Direction6.UP,
                        point(right, top, back), point(left, top, back),
                        point(left, top, front), point(right, top, front)),
                quad(Direction6.DOWN,
                        point(right, bottom, front), point(left, bottom, front),
                        point(left, bottom, back), point(right, bottom, back))
        );
    }

    private static LedQuad quad(Direction6 face, Position... positions) {
        return new LedQuad(face, List.of(positions));
    }

    private static Position point(double x, double y, double z) {
        return new Position(x, y, z);
    }

    private static int requireSpin(int spin) {
        if (spin < 0 || spin > 3) {
            throw new IllegalArgumentException("Cell Dock spin must be in [0, 3]");
        }
        return spin;
    }

    /** Operations apply in field order: center translation, rotation, local translation. */
    public record Transform(
            Position centerTranslation,
            Orientation orientation,
            Position localTranslation
    ) {

        public Transform {
            Objects.requireNonNull(centerTranslation, "centerTranslation");
            Objects.requireNonNull(orientation, "orientation");
            Objects.requireNonNull(localTranslation, "localTranslation");
        }
    }

    /** Exact arguments to AE2 {@code BlockOrientation.get(front, top)}. */
    public record Orientation(Direction6 front, Direction6 top) {

        public Orientation {
            Objects.requireNonNull(front, "front");
            Objects.requireNonNull(top, "top");
            int dot = front.stepX() * top.stepX()
                    + front.stepY() * top.stepY()
                    + front.stepZ() * top.stepZ();
            if (dot != 0) {
                throw new IllegalArgumentException("front and top must be perpendicular");
            }
        }
    }

    /** Position in block units. */
    public record Position(double x, double y, double z) {

        public Position {
            requireFinite("x", x);
            requireFinite("y", y);
            requireFinite("z", z);
        }
    }

    public record LedQuad(Direction6 face, List<Position> vertices) {

        public LedQuad {
            Objects.requireNonNull(face, "face");
            vertices = List.copyOf(Objects.requireNonNull(vertices, "vertices"));
            if (vertices.size() != 4) {
                throw new IllegalArgumentException("an LED quad must have four vertices");
            }
        }
    }

    private static void requireFinite(String name, double value) {
        if (!Double.isFinite(value)) {
            throw new IllegalArgumentException(name + " must be finite");
        }
    }
}
