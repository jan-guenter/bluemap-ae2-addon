/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.model;

import java.util.List;
import java.util.Objects;

/** Source-exact static geometry not represented by AE2's ordinary JSON models. */
public final class MachineGeometry {

    private static final float TWO_PIXELS = 2F / 16F;
    private static final float HIGH = 1F - TWO_PIXELS;
    private static final float TOP_MIDDLE = 0.52F;
    private static final float BOTTOM_MIDDLE = 0.48F;
    private static final float PRESS = 0.2F;
    private static final float BASE = 0.4F;
    private static final float INNER_V = 0.125F;
    private static final float OUTER_V = INNER_V - (PRESS - BASE);

    private static final List<Quad> NEUTRAL_INSCRIBER_STAMPS = List.of(
            quad(Direction6.DOWN,
                    vertex(TWO_PIXELS, TOP_MIDDLE + PRESS, TWO_PIXELS, 0.875F, 0.125F),
                    vertex(HIGH, TOP_MIDDLE + PRESS, TWO_PIXELS, 0.125F, 0.125F),
                    vertex(HIGH, TOP_MIDDLE + PRESS, HIGH, 0.125F, 0.875F),
                    vertex(TWO_PIXELS, TOP_MIDDLE + PRESS, HIGH, 0.875F, 0.875F)),
            quad(Direction6.NORTH,
                    vertex(TWO_PIXELS, TOP_MIDDLE + BASE, TWO_PIXELS, 0.125F, OUTER_V),
                    vertex(HIGH, TOP_MIDDLE + BASE, TWO_PIXELS, 0.875F, OUTER_V),
                    vertex(HIGH, TOP_MIDDLE + PRESS, TWO_PIXELS, 0.875F, INNER_V),
                    vertex(TWO_PIXELS, TOP_MIDDLE + PRESS, TWO_PIXELS, 0.125F, INNER_V)),
            quad(Direction6.SOUTH,
                    vertex(TWO_PIXELS, TOP_MIDDLE + BASE, HIGH, 0.125F, OUTER_V),
                    vertex(TWO_PIXELS, TOP_MIDDLE + PRESS, HIGH, 0.125F, INNER_V),
                    vertex(HIGH, TOP_MIDDLE + PRESS, HIGH, 0.875F, INNER_V),
                    vertex(HIGH, TOP_MIDDLE + BASE, HIGH, 0.875F, OUTER_V)),
            quad(Direction6.UP,
                    vertex(HIGH, BOTTOM_MIDDLE - PRESS, TWO_PIXELS, 0.875F, 0.125F),
                    vertex(TWO_PIXELS, BOTTOM_MIDDLE - PRESS, TWO_PIXELS, 0.125F, 0.125F),
                    vertex(TWO_PIXELS, BOTTOM_MIDDLE - PRESS, HIGH, 0.125F, 0.875F),
                    vertex(HIGH, BOTTOM_MIDDLE - PRESS, HIGH, 0.875F, 0.875F)),
            quad(Direction6.NORTH,
                    vertex(HIGH, BOTTOM_MIDDLE - BASE, TWO_PIXELS, 0.125F, OUTER_V),
                    vertex(TWO_PIXELS, BOTTOM_MIDDLE - BASE, TWO_PIXELS, 0.875F, OUTER_V),
                    vertex(TWO_PIXELS, BOTTOM_MIDDLE - PRESS, TWO_PIXELS, 0.875F, INNER_V),
                    vertex(HIGH, BOTTOM_MIDDLE - PRESS, TWO_PIXELS, 0.125F, INNER_V)),
            quad(Direction6.SOUTH,
                    vertex(TWO_PIXELS, BOTTOM_MIDDLE - PRESS, HIGH, 0.875F, INNER_V),
                    vertex(TWO_PIXELS, BOTTOM_MIDDLE - BASE, HIGH, 0.875F, OUTER_V),
                    vertex(HIGH, BOTTOM_MIDDLE - BASE, HIGH, 0.125F, OUTER_V),
                    vertex(HIGH, BOTTOM_MIDDLE - PRESS, HIGH, 0.125F, INNER_V))
    );

    private MachineGeometry() {
    }

    /** Six quads / twelve triangles with no items and animation progress zero. */
    public static List<Quad> neutralInscriberStamps() {
        return NEUTRAL_INSCRIBER_STAMPS;
    }

    private static Quad quad(
            Direction6 face,
            Vertex first,
            Vertex second,
            Vertex third,
            Vertex fourth
    ) {
        return new Quad(face, List.of(first, second, third, fourth));
    }

    private static Vertex vertex(float x, float y, float z, float u, float v) {
        return new Vertex(x * 16F, y * 16F, z * 16F, u * 16F, v * 16F);
    }

    public record Vertex(double x16, double y16, double z16, double u16, double v16) {

        public Vertex {
            requireFinite(x16);
            requireFinite(y16);
            requireFinite(z16);
            requireFinite(u16);
            requireFinite(v16);
        }
    }

    public record Quad(Direction6 face, List<Vertex> vertices) {

        public Quad {
            Objects.requireNonNull(face, "face");
            vertices = List.copyOf(Objects.requireNonNull(vertices, "vertices"));
            if (vertices.size() != 4) {
                throw new IllegalArgumentException("an inscriber quad must contain four vertices");
            }
        }
    }

    private static void requireFinite(double value) {
        if (!Double.isFinite(value)) {
            throw new IllegalArgumentException("coordinate must be finite");
        }
    }
}
