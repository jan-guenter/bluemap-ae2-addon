/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Exact canonical drive slot origins and static unpowered LED geometry. */
public final class DriveGeometry {

    public static final int OFFLINE_UNKNOWN_LED_RGB = 0x000000;

    private static final double LED_LEFT_16 = 5;
    private static final double LED_RIGHT_16 = 4;
    private static final double LED_TOP_16 = 1;
    private static final double LED_BOTTOM_16 = -0.001;
    private static final double LED_FRONT_16 = -0.001;
    private static final double LED_BACK_16 = 0.999;
    private static final List<SlotOrigin> SLOT_ORIGINS = buildSlotOrigins();
    private static final List<List<LedQuad>> SLOT_LEDS = buildSlotLeds();

    private DriveGeometry() {
    }

    public static SlotOrigin slotOrigin(int slot) {
        return SLOT_ORIGINS.get(requireSlot(slot));
    }

    /** Five canonical color-only quads; AE2 deliberately omits the LED's back face. */
    public static List<LedQuad> offlineUnknownLed(int slot) {
        return SLOT_LEDS.get(requireSlot(slot));
    }

    private static List<SlotOrigin> buildSlotOrigins() {
        List<SlotOrigin> origins = new ArrayList<>(DriveInventoryProjection.SLOT_COUNT);
        for (int row = 0; row < DriveInventoryProjection.ROWS; row++) {
            for (int column = 0; column < DriveInventoryProjection.COLUMNS; column++) {
                origins.add(new SlotOrigin(
                        9 - column * 8,
                        13 - row * 3,
                        1
                ));
            }
        }
        return List.copyOf(origins);
    }

    private static List<List<LedQuad>> buildSlotLeds() {
        List<List<LedQuad>> slots = new ArrayList<>(DriveInventoryProjection.SLOT_COUNT);
        for (SlotOrigin origin : SLOT_ORIGINS) {
            slots.add(List.of(
                    quad(origin, Direction6.NORTH,
                            point(LED_RIGHT_16, LED_TOP_16, LED_FRONT_16),
                            point(LED_LEFT_16, LED_TOP_16, LED_FRONT_16),
                            point(LED_LEFT_16, LED_BOTTOM_16, LED_FRONT_16),
                            point(LED_RIGHT_16, LED_BOTTOM_16, LED_FRONT_16)),
                    quad(origin, Direction6.EAST,
                            point(LED_LEFT_16, LED_TOP_16, LED_FRONT_16),
                            point(LED_LEFT_16, LED_TOP_16, LED_BACK_16),
                            point(LED_LEFT_16, LED_BOTTOM_16, LED_BACK_16),
                            point(LED_LEFT_16, LED_BOTTOM_16, LED_FRONT_16)),
                    quad(origin, Direction6.WEST,
                            point(LED_RIGHT_16, LED_TOP_16, LED_BACK_16),
                            point(LED_RIGHT_16, LED_TOP_16, LED_FRONT_16),
                            point(LED_RIGHT_16, LED_BOTTOM_16, LED_FRONT_16),
                            point(LED_RIGHT_16, LED_BOTTOM_16, LED_BACK_16)),
                    quad(origin, Direction6.UP,
                            point(LED_RIGHT_16, LED_TOP_16, LED_BACK_16),
                            point(LED_LEFT_16, LED_TOP_16, LED_BACK_16),
                            point(LED_LEFT_16, LED_TOP_16, LED_FRONT_16),
                            point(LED_RIGHT_16, LED_TOP_16, LED_FRONT_16)),
                    quad(origin, Direction6.DOWN,
                            point(LED_RIGHT_16, LED_BOTTOM_16, LED_FRONT_16),
                            point(LED_LEFT_16, LED_BOTTOM_16, LED_FRONT_16),
                            point(LED_LEFT_16, LED_BOTTOM_16, LED_BACK_16),
                            point(LED_RIGHT_16, LED_BOTTOM_16, LED_BACK_16))
            ));
        }
        return List.copyOf(slots);
    }

    private static LedQuad quad(
            SlotOrigin origin,
            Direction6 face,
            Position first,
            Position second,
            Position third,
            Position fourth
    ) {
        return new LedQuad(face, List.of(
                first.translate(origin),
                second.translate(origin),
                third.translate(origin),
                fourth.translate(origin)
        ));
    }

    private static Position point(double x16, double y16, double z16) {
        return new Position(x16, y16, z16);
    }

    private static int requireSlot(int slot) {
        if (slot < 0 || slot >= DriveInventoryProjection.SLOT_COUNT) {
            throw new IllegalArgumentException("drive slot must be in [0, 9]");
        }
        return slot;
    }

    /** Position in sixteenths of one block, before the full-block orientation. */
    public record Position(double x16, double y16, double z16) {

        public Position {
            requireFinite("x16", x16);
            requireFinite("y16", y16);
            requireFinite("z16", z16);
        }

        private Position translate(SlotOrigin origin) {
            return new Position(
                    x16 + origin.x16(),
                    y16 + origin.y16(),
                    z16 + origin.z16()
            );
        }
    }

    /** Translation from AE2's slot-zero chassis model, in sixteenths. */
    public record SlotOrigin(double x16, double y16, double z16) {

        public SlotOrigin {
            requireFinite("x16", x16);
            requireFinite("y16", y16);
            requireFinite("z16", z16);
        }
    }

    public record LedQuad(Direction6 face, List<Position> vertices) {

        public LedQuad {
            Objects.requireNonNull(face, "face");
            vertices = List.copyOf(Objects.requireNonNull(vertices, "vertices"));
            if (vertices.size() != 4) {
                throw new IllegalArgumentException("an LED quad must contain exactly four vertices");
            }
        }
    }

    private static void requireFinite(String name, double value) {
        if (!Double.isFinite(value)) {
            throw new IllegalArgumentException(name + " must be finite");
        }
    }
}
