/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Exact closed-lid ModelPart geometry shared by both AE2 Sky Stone chests. */
public final class SkyChestGeometry {

    private static final float TEXTURE_SIZE = 64F;
    private static final Direction6[] CUBE_FACE_ORDER = {
        Direction6.DOWN,
        Direction6.UP,
        Direction6.WEST,
        Direction6.NORTH,
        Direction6.EAST,
        Direction6.SOUTH
    };

    private static final List<Quad> CLOSED = build();

    private SkyChestGeometry() {
    }

    /** Eighteen quads / 36 triangles in AE2's lid, lock, bottom render order. */
    public static List<Quad> closed() {
        return CLOSED;
    }

    private static List<Quad> build() {
        List<Quad> output = new ArrayList<>(18);
        // SkyChestTESR renders the static closed lid, lock, then bottom.
        addCube(output, Part.LID, 0, 0,
                1, 10, 1, 14, 5, 14);
        addCube(output, Part.LOCK, 0, 0,
                7, 8, 15, 2, 4, 1);
        addCube(output, Part.BOTTOM, 0, 19,
                1, 0, 1, 14, 10, 14);
        return List.copyOf(output);
    }

    private static void addCube(
            List<Quad> output,
            Part part,
            float textureU,
            float textureV,
            float x,
            float y,
            float z,
            float dx,
            float dy,
            float dz
    ) {
        Position v0 = new Position(x, y, z);
        Position v1 = new Position(x + dx, y, z);
        Position v2 = new Position(x + dx, y + dy, z);
        Position v3 = new Position(x, y + dy, z);
        Position v4 = new Position(x, y, z + dz);
        Position v5 = new Position(x + dx, y, z + dz);
        Position v6 = new Position(x + dx, y + dy, z + dz);
        Position v7 = new Position(x, y + dy, z + dz);

        float u0 = textureU;
        float u1 = textureU + dz;
        float u2 = textureU + dz + dx;
        float u3 = textureU + dz + dx + dx;
        float u4 = textureU + dz + dx + dz;
        float u5 = textureU + dz + dx + dz + dx;
        float vv0 = textureV;
        float vv1 = textureV + dz;
        float vv2 = textureV + dz + dy;

        for (Direction6 face : CUBE_FACE_ORDER) {
            switch (face) {
                case DOWN -> add(output, part, face,
                        new Position[]{v5, v4, v0, v1}, u1, vv0, u2, vv1);
                case UP -> add(output, part, face,
                        new Position[]{v2, v3, v7, v6}, u2, vv1, u3, vv0);
                case WEST -> add(output, part, face,
                        new Position[]{v0, v4, v7, v3}, u0, vv1, u1, vv2);
                case NORTH -> add(output, part, face,
                        new Position[]{v1, v0, v3, v2}, u1, vv1, u2, vv2);
                case EAST -> add(output, part, face,
                        new Position[]{v5, v1, v2, v6}, u2, vv1, u4, vv2);
                case SOUTH -> add(output, part, face,
                        new Position[]{v4, v5, v6, v7}, u4, vv1, u5, vv2);
            }
        }
    }

    private static void add(
            List<Quad> output,
            Part part,
            Direction6 face,
            Position[] positions,
            float u1,
            float v1,
            float u2,
            float v2
    ) {
        // ModelPart.Polygon assigns (u2,v1), (u1,v1), (u1,v2), (u2,v2).
        output.add(new Quad(part, face, List.of(
                vertex(positions[0], u2, v1),
                vertex(positions[1], u1, v1),
                vertex(positions[2], u1, v2),
                vertex(positions[3], u2, v2)
        )));
    }

    private static Vertex vertex(Position position, float textureU, float textureV) {
        // The renderer divides x/y/z by 16 and texture pixels by 64. Storing
        // UVs in sixteenth units means one texture pixel is one quarter unit.
        return new Vertex(
                position.x(),
                position.y(),
                position.z(),
                textureU * 16F / TEXTURE_SIZE,
                textureV * 16F / TEXTURE_SIZE
        );
    }

    public enum Part {
        LID,
        LOCK,
        BOTTOM
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

    public record Quad(Part part, Direction6 face, List<Vertex> vertices) {

        public Quad {
            Objects.requireNonNull(part, "part");
            Objects.requireNonNull(face, "face");
            vertices = List.copyOf(Objects.requireNonNull(vertices, "vertices"));
            if (vertices.size() != 4) {
                throw new IllegalArgumentException("a chest quad must contain four vertices");
            }
        }
    }

    private record Position(float x, float y, float z) {
    }

    private static void requireFinite(double value) {
        if (!Double.isFinite(value)) {
            throw new IllegalArgumentException("coordinate must be finite");
        }
    }
}
