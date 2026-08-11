/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Dependency-free port of AE2 19.2.17's paint-splotch quads. */
public final class PaintGeometry {

    private static final float BUFFER = 0.1F;

    private PaintGeometry() {
    }

    public static List<Quad> forSnapshot(PaintSnapshot snapshot) {
        Objects.requireNonNull(snapshot, "snapshot");
        List<Quad> quads = new ArrayList<>(snapshot.splotches().size());
        float offset = 0.001F;
        for (PaintSplotch splotch : snapshot.splotches()) {
            float x = clamp(splotch.x());
            float y = clamp(splotch.y());
            Direction6 face = splotch.visibleFace();
            float plane = switch (splotch.backingSide()) {
                case UP, EAST, SOUTH -> 1F - offset;
                default -> offset;
            };
            Bounds bounds = switch (splotch.backingSide()) {
                case UP, DOWN -> new Bounds(x - BUFFER, plane, y - BUFFER,
                        x + BUFFER, plane, y + BUFFER);
                case EAST, WEST -> new Bounds(plane, x - BUFFER, y - BUFFER,
                        plane, x + BUFFER, y + BUFFER);
                case SOUTH, NORTH -> new Bounds(x - BUFFER, y - BUFFER, plane,
                        x + BUFFER, y + BUFFER, plane);
            };
            quads.add(new Quad(
                    face,
                    splotch.textureIndex(),
                    splotch.rgb(),
                    splotch.lumen(),
                    faceVertices(face, bounds)
            ));
            offset += 0.001F;
        }
        return List.copyOf(quads);
    }

    private static float clamp(float value) {
        return Math.max(BUFFER, Math.min(1F - BUFFER, value));
    }

    private static List<Vertex> faceVertices(Direction6 face, Bounds bounds) {
        Position[] positions = switch (face) {
            case DOWN -> square(face, bounds.x1(), bounds.z1(), bounds.x2(), bounds.z2(), bounds.y1());
            case UP -> square(face, bounds.x1(), 1F - bounds.z2(), bounds.x2(),
                    1F - bounds.z1(), 1F - bounds.y2());
            case NORTH -> square(face, 1F - bounds.x2(), bounds.y1(),
                    1F - bounds.x1(), bounds.y2(), bounds.z1());
            case SOUTH -> square(face, bounds.x1(), bounds.y1(), bounds.x2(),
                    bounds.y2(), 1F - bounds.z2());
            case WEST -> square(face, bounds.z1(), bounds.y1(), bounds.z2(),
                    bounds.y2(), bounds.x1());
            case EAST -> square(face, 1F - bounds.z2(), bounds.y1(),
                    1F - bounds.z1(), bounds.y2(), 1F - bounds.x2());
        };
        float[][] uv = face == Direction6.DOWN || face == Direction6.UP
                ? new float[][]{{0, 0}, {0, 1}, {1, 1}, {1, 0}}
                : new float[][]{{0, 1}, {0, 0}, {1, 0}, {1, 1}};
        List<Vertex> vertices = new ArrayList<>(4);
        for (int index = 0; index < 4; index++) {
            vertices.add(new Vertex(
                    positions[index].x() * 16F,
                    positions[index].y() * 16F,
                    positions[index].z() * 16F,
                    uv[index][0] * 16F,
                    uv[index][1] * 16F
            ));
        }
        return List.copyOf(vertices);
    }

    private static Position[] square(
            Direction6 face,
            float left,
            float bottom,
            float right,
            float top,
            float depth
    ) {
        return switch (face) {
            case DOWN -> new Position[]{
                    new Position(left, depth, top),
                    new Position(left, depth, bottom),
                    new Position(right, depth, bottom),
                    new Position(right, depth, top)
            };
            case UP -> {
                depth = 1F - depth;
                top = 1F - top;
                bottom = 1F - bottom;
                yield new Position[]{
                        new Position(left, depth, top),
                        new Position(left, depth, bottom),
                        new Position(right, depth, bottom),
                        new Position(right, depth, top)
                };
            }
            case WEST -> new Position[]{
                    new Position(depth, top, left),
                    new Position(depth, bottom, left),
                    new Position(depth, bottom, right),
                    new Position(depth, top, right)
            };
            case EAST -> {
                depth = 1F - depth;
                left = 1F - left;
                right = 1F - right;
                yield new Position[]{
                        new Position(depth, top, left),
                        new Position(depth, bottom, left),
                        new Position(depth, bottom, right),
                        new Position(depth, top, right)
                };
            }
            case NORTH -> new Position[]{
                    new Position(1F - left, top, depth),
                    new Position(1F - left, bottom, depth),
                    new Position(1F - right, bottom, depth),
                    new Position(1F - right, top, depth)
            };
            case SOUTH -> {
                depth = 1F - depth;
                left = 1F - left;
                right = 1F - right;
                yield new Position[]{
                        new Position(1F - left, top, depth),
                        new Position(1F - left, bottom, depth),
                        new Position(1F - right, bottom, depth),
                        new Position(1F - right, top, depth)
                };
            }
        };
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

    public record Quad(
            Direction6 face,
            int textureIndex,
            int rgb,
            boolean emissive,
            List<Vertex> vertices
    ) {

        public Quad {
            Objects.requireNonNull(face, "face");
            if (textureIndex < 0 || textureIndex > 2) {
                throw new IllegalArgumentException("textureIndex must be in [0, 2]");
            }
            if ((rgb & 0xff000000) != 0) {
                throw new IllegalArgumentException("rgb must be a 24-bit color");
            }
            vertices = List.copyOf(Objects.requireNonNull(vertices, "vertices"));
            if (vertices.size() != 4) {
                throw new IllegalArgumentException("a paint quad must contain four vertices");
            }
        }
    }

    private record Bounds(float x1, float y1, float z1, float x2, float y2, float z2) {
    }

    private record Position(float x, float y, float z) {
    }

    private static void requireFinite(double value) {
        if (!Double.isFinite(value)) {
            throw new IllegalArgumentException("coordinate must be finite");
        }
    }
}
