/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/** Dependency-free port of AE2 19.2.17's formed quantum-bridge cube geometry. */
public final class QuantumBridgeGeometry {

    private static final float LINK_GLASS_THICKNESS = 0.11F * 16F;
    private static final float LINK_GLASS_PULL = 0.141F * 16F;
    private static final float COVERED_THICKNESS = 0.188F * 16F;
    private static final float LINK_COVERED_PULL = 0.1875F * 16F;
    private static final float CORNER_COVERED_PULL = 0.05F * 16F;
    private static final Direction6[] ARM_ORDER = {
        Direction6.WEST,
        Direction6.EAST,
        Direction6.NORTH,
        Direction6.SOUTH,
        Direction6.DOWN,
        Direction6.UP
    };

    private QuantumBridgeGeometry() {
    }

    public static List<Quad> forSnapshot(QuantumBridgeSnapshot snapshot) {
        Objects.requireNonNull(snapshot, "snapshot");
        List<Quad> output = new ArrayList<>(54);
        switch (snapshot.role()) {
            case LINK -> {
                addArms(
                        output,
                        snapshot.connections(),
                        LINK_GLASS_THICKNESS,
                        LINK_GLASS_PULL,
                        Layer.GLASS
                );
                addArms(
                        output,
                        snapshot.connections(),
                        COVERED_THICKNESS,
                        LINK_COVERED_PULL,
                        Layer.COVERED
                );
                addCube(output, Layer.LINK, new Bounds(2F, 2F, 2F, 14F, 14F, 14F));
            }
            case CORNER_RING -> {
                addArms(
                        output,
                        snapshot.connections(),
                        COVERED_THICKNESS,
                        CORNER_COVERED_PULL,
                        Layer.COVERED
                );
                addCube(output, Layer.RING, new Bounds(2F, 2F, 2F, 14F, 14F, 14F));
            }
            case EDGE_RING -> {
                addCube(output, Layer.RING, new Bounds(0F, 2F, 2F, 16F, 14F, 14F));
                addCube(output, Layer.RING, new Bounds(2F, 0F, 2F, 14F, 16F, 14F));
                addCube(output, Layer.RING, new Bounds(2F, 2F, 0F, 14F, 14F, 16F));
            }
        }
        return List.copyOf(output);
    }

    private static void addArms(
            List<Quad> output,
            Set<Direction6> connections,
            float thickness,
            float pull,
            Layer layer
    ) {
        float low = 8F - thickness;
        float high = 8F + thickness;
        float inwardLow = low - pull;
        float inwardHigh = high + pull;
        for (Direction6 direction : ARM_ORDER) {
            if (!connections.contains(direction)) {
                continue;
            }
            Bounds bounds = switch (direction) {
                case WEST -> new Bounds(0F, low, low, inwardLow, high, high);
                case EAST -> new Bounds(inwardHigh, low, low, 16F, high, high);
                case NORTH -> new Bounds(low, low, 0F, high, high, inwardLow);
                case SOUTH -> new Bounds(low, low, inwardHigh, high, high, 16F);
                case DOWN -> new Bounds(low, 0F, low, high, inwardLow, high);
                case UP -> new Bounds(low, inwardHigh, low, high, 16F, high);
            };
            addCube(output, layer, bounds);
        }
    }

    private static void addCube(List<Quad> output, Layer layer, Bounds bounds) {
        Bounds normalized = bounds.normalized();
        for (Direction6 face : Direction6.values()) {
            Uv uv = standardUv(face, normalized);
            Position[] positions = positions(face, normalized);
            List<Vertex> vertices;
            if (face == Direction6.DOWN || face == Direction6.UP) {
                vertices = List.of(
                        positions[0].vertex(uv.u1(), uv.v1()),
                        positions[1].vertex(uv.u1(), uv.v2()),
                        positions[2].vertex(uv.u2(), uv.v2()),
                        positions[3].vertex(uv.u2(), uv.v1())
                );
            } else {
                vertices = List.of(
                        positions[0].vertex(uv.u1(), uv.v2()),
                        positions[1].vertex(uv.u1(), uv.v1()),
                        positions[2].vertex(uv.u2(), uv.v1()),
                        positions[3].vertex(uv.u2(), uv.v2())
                );
            }
            output.add(new Quad(face, layer, vertices));
        }
    }

    private static Position[] positions(Direction6 face, Bounds bounds) {
        return switch (face) {
            case DOWN -> square(
                    face,
                    bounds.x1(), bounds.z1(), bounds.x2(), bounds.z2(), bounds.y1()
            );
            case UP -> square(
                    face,
                    bounds.x1(), 1F - bounds.z2(),
                    bounds.x2(), 1F - bounds.z1(),
                    1F - bounds.y2()
            );
            case NORTH -> square(
                    face,
                    1F - bounds.x2(), bounds.y1(),
                    1F - bounds.x1(), bounds.y2(),
                    bounds.z1()
            );
            case SOUTH -> square(
                    face,
                    bounds.x1(), bounds.y1(), bounds.x2(), bounds.y2(),
                    1F - bounds.z2()
            );
            case WEST -> square(
                    face,
                    bounds.z1(), bounds.y1(), bounds.z2(), bounds.y2(), bounds.x1()
            );
            case EAST -> square(
                    face,
                    1F - bounds.z2(), bounds.y1(),
                    1F - bounds.z1(), bounds.y2(),
                    1F - bounds.x2()
            );
        };
    }

    private static Position[] square(
            Direction6 face,
            float left,
            float bottom,
            float right,
            float top,
            float depth
    ) {
        if (Math.abs(depth) < 0.00001F) {
            depth = 0F;
        }
        return switch (face) {
            case UP -> {
                depth = 1F - depth;
                top = 1F - top;
                bottom = 1F - bottom;
                yield downPositions(left, bottom, right, top, depth);
            }
            case DOWN -> downPositions(left, bottom, right, top, depth);
            case EAST -> {
                depth = 1F - depth;
                left = 1F - left;
                right = 1F - right;
                yield westPositions(left, bottom, right, top, depth);
            }
            case WEST -> westPositions(left, bottom, right, top, depth);
            case SOUTH -> {
                depth = 1F - depth;
                left = 1F - left;
                right = 1F - right;
                yield northPositions(left, bottom, right, top, depth);
            }
            case NORTH -> northPositions(left, bottom, right, top, depth);
        };
    }

    private static Position[] downPositions(
            float left,
            float bottom,
            float right,
            float top,
            float depth
    ) {
        return new Position[]{
            new Position(left, depth, top),
            new Position(left, depth, bottom),
            new Position(right, depth, bottom),
            new Position(right, depth, top)
        };
    }

    private static Position[] westPositions(
            float left,
            float bottom,
            float right,
            float top,
            float depth
    ) {
        return new Position[]{
            new Position(depth, top, left),
            new Position(depth, bottom, left),
            new Position(depth, bottom, right),
            new Position(depth, top, right)
        };
    }

    private static Position[] northPositions(
            float left,
            float bottom,
            float right,
            float top,
            float depth
    ) {
        return new Position[]{
            new Position(1F - left, top, depth),
            new Position(1F - left, bottom, depth),
            new Position(1F - right, bottom, depth),
            new Position(1F - right, top, depth)
        };
    }

    private static Uv standardUv(Direction6 face, Bounds bounds) {
        float v1 = face == Direction6.DOWN || face == Direction6.UP
                ? bounds.z1() : 1F - bounds.y1();
        float v2 = face == Direction6.DOWN || face == Direction6.UP
                ? bounds.z2() : 1F - bounds.y2();
        return switch (face) {
            case DOWN, UP, SOUTH -> new Uv(bounds.x1(), v1, bounds.x2(), v2);
            case NORTH -> new Uv(1F - bounds.x2(), v1, 1F - bounds.x1(), v2);
            case WEST -> new Uv(bounds.z1(), v1, bounds.z2(), v2);
            case EAST -> new Uv(1F - bounds.z2(), v1, 1F - bounds.z1(), v2);
        };
    }

    public enum Layer {
        LINK,
        RING,
        GLASS,
        COVERED
    }

    public record Vertex(double x16, double y16, double z16, double u16, double v16) {

        public Vertex {
            requireCoordinate("x16", x16);
            requireCoordinate("y16", y16);
            requireCoordinate("z16", z16);
            requireCoordinate("u16", u16);
            requireCoordinate("v16", v16);
        }
    }

    public record Quad(Direction6 face, Layer layer, List<Vertex> vertices) {

        public Quad {
            Objects.requireNonNull(face, "face");
            Objects.requireNonNull(layer, "layer");
            vertices = List.copyOf(Objects.requireNonNull(vertices, "vertices"));
            if (vertices.size() != 4) {
                throw new IllegalArgumentException("a quad must contain four vertices");
            }
        }
    }

    private record Bounds(
            float x1,
            float y1,
            float z1,
            float x2,
            float y2,
            float z2
    ) {
        private Bounds normalized() {
            return new Bounds(
                    x1 / 16F,
                    y1 / 16F,
                    z1 / 16F,
                    x2 / 16F,
                    y2 / 16F,
                    z2 / 16F
            );
        }
    }

    private record Position(float x, float y, float z) {
        private Vertex vertex(float u, float v) {
            return new Vertex(
                    x * 16F,
                    y * 16F,
                    z * 16F,
                    u * 16F,
                    v * 16F
            );
        }
    }

    private record Uv(float u1, float v1, float u2, float v2) {
    }

    private static void requireCoordinate(String name, double value) {
        if (!Double.isFinite(value) || value < 0D || value > 16D) {
            throw new IllegalArgumentException(name + " must be in [0, 16]");
        }
    }
}
