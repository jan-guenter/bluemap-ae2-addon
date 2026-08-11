/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Dependency-free port of AE2 19.2.17's formed crafting-cube geometry. */
public final class CraftingGeometry {

    private static final Corner[] CORNERS = {
        new Corner(Direction6.UP, Direction6.EAST, Direction6.NORTH),
        new Corner(Direction6.UP, Direction6.EAST, Direction6.SOUTH),
        new Corner(Direction6.UP, Direction6.WEST, Direction6.NORTH),
        new Corner(Direction6.UP, Direction6.WEST, Direction6.SOUTH),
        new Corner(Direction6.DOWN, Direction6.EAST, Direction6.NORTH),
        new Corner(Direction6.DOWN, Direction6.EAST, Direction6.SOUTH),
        new Corner(Direction6.DOWN, Direction6.WEST, Direction6.NORTH),
        new Corner(Direction6.DOWN, Direction6.WEST, Direction6.SOUTH)
    };

    private CraftingGeometry() {
    }

    public static List<Quad> forSnapshot(CraftingSnapshot snapshot) {
        Objects.requireNonNull(snapshot, "snapshot");
        List<Quad> output = new ArrayList<>(60);
        for (Direction6 side : Direction6.values()) {
            if (snapshot.isConnected(side)) {
                continue;
            }
            addRing(output, snapshot, side);
            Bounds inner = innerBounds(snapshot, side);
            addInner(output, snapshot, side, inner);
        }
        return List.copyOf(output);
    }

    private static void addRing(
            List<Quad> output,
            CraftingSnapshot snapshot,
            Direction6 side
    ) {
        for (Corner corner : CORNERS) {
            if (snapshot.isConnected(corner.vertical())
                    || snapshot.isConnected(corner.horizontal())
                    || snapshot.isConnected(corner.depth())) {
                continue;
            }
            if (!corner.touches(side)) {
                continue;
            }
            double x1 = corner.horizontal() == Direction6.WEST ? 0D : 13D;
            double y1 = corner.vertical() == Direction6.DOWN ? 0D : 13D;
            double z1 = corner.depth() == Direction6.NORTH ? 0D : 13D;
            addFace(output, side, Layer.RING_CORNER, new Bounds(
                    x1,
                    y1,
                    z1,
                    x1 + 3D,
                    y1 + 3D,
                    z1 + 3D
            ));
        }

        for (Direction6 edge : Direction6.values()) {
            if (edge == side || edge == side.opposite() || snapshot.isConnected(edge)) {
                continue;
            }
            double x1 = 0D;
            double y1 = 0D;
            double z1 = 0D;
            double x2 = 16D;
            double y2 = 16D;
            double z2 = 16D;
            switch (edge) {
                case DOWN -> y2 = 3D;
                case UP -> y1 = 13D;
                case WEST -> x2 = 3D;
                case EAST -> x1 = 13D;
                case NORTH -> z2 = 3D;
                case SOUTH -> z1 = 13D;
            }

            Direction6 perpendicular = cross(edge, side);
            for (Direction6 cornerCandidate : new Direction6[]{
                perpendicular,
                perpendicular.opposite()
            }) {
                if (!snapshot.isConnected(cornerCandidate)) {
                    switch (cornerCandidate) {
                        case DOWN -> y1 = 3D;
                        case UP -> y2 = 13D;
                        case NORTH -> z1 = 3D;
                        case SOUTH -> z2 = 13D;
                        case WEST -> x1 = 3D;
                        case EAST -> x2 = 13D;
                    }
                }
            }

            Layer layer = isVerticalRingTexture(side, edge)
                    ? Layer.RING_SIDE_VERTICAL
                    : Layer.RING_SIDE_HORIZONTAL;
            addFace(output, side, layer, new Bounds(x1, y1, z1, x2, y2, z2));
        }
    }

    private static boolean isVerticalRingTexture(Direction6 side, Direction6 edge) {
        if (!isYAxis(side)) {
            return !isYAxis(edge);
        }
        return edge == Direction6.EAST || edge == Direction6.WEST;
    }

    private static Bounds innerBounds(CraftingSnapshot snapshot, Direction6 side) {
        double x1 = snapshot.isConnected(Direction6.WEST) ? 0D : 2.99D;
        double x2 = snapshot.isConnected(Direction6.EAST) ? 16D : 13.01D;
        double y1 = snapshot.isConnected(Direction6.DOWN) ? 0D : 2.99D;
        double y2 = snapshot.isConnected(Direction6.UP) ? 16D : 13.01D;
        double z1 = snapshot.isConnected(Direction6.NORTH) ? 0D : 2.99D;
        double z2 = snapshot.isConnected(Direction6.SOUTH) ? 16D : 13.01D;
        switch (side) {
            case DOWN, UP -> {
                y1 = 0D;
                y2 = 16D;
            }
            case NORTH, SOUTH -> {
                z1 = 0D;
                z2 = 16D;
            }
            case WEST, EAST -> {
                x1 = 0D;
                x2 = 16D;
            }
        }
        return new Bounds(x1, y1, z1, x2, y2, z2);
    }

    private static void addInner(
            List<Quad> output,
            CraftingSnapshot snapshot,
            Direction6 side,
            Bounds bounds
    ) {
        switch (snapshot.kind()) {
            case UNIT -> addFace(output, side, Layer.UNIT_BASE, bounds);
            case ACCELERATOR -> addLightInner(
                    output,
                    side,
                    bounds,
                    Layer.ACCELERATOR_LIGHT
            );
            case STORAGE_1K -> addLightInner(
                    output,
                    side,
                    bounds,
                    Layer.STORAGE_1K_LIGHT
            );
            case STORAGE_4K -> addLightInner(
                    output,
                    side,
                    bounds,
                    Layer.STORAGE_4K_LIGHT
            );
            case STORAGE_16K -> addLightInner(
                    output,
                    side,
                    bounds,
                    Layer.STORAGE_16K_LIGHT
            );
            case STORAGE_64K -> addLightInner(
                    output,
                    side,
                    bounds,
                    Layer.STORAGE_64K_LIGHT
            );
            case STORAGE_256K -> addLightInner(
                    output,
                    side,
                    bounds,
                    Layer.STORAGE_256K_LIGHT
            );
            case MONITOR -> addMonitorInner(output, snapshot, side, bounds);
        }
    }

    private static void addLightInner(
            List<Quad> output,
            Direction6 side,
            Bounds bounds,
            Layer lightLayer
    ) {
        addFace(output, side, Layer.LIGHT_BASE, bounds);
        addFace(output, side, lightLayer, bounds);
    }

    private static void addMonitorInner(
            List<Quad> output,
            CraftingSnapshot snapshot,
            Direction6 side,
            Bounds bounds
    ) {
        if (side != snapshot.facing()) {
            addFace(output, side, Layer.UNIT_BASE, bounds);
            return;
        }
        addFace(output, side, Layer.MONITOR_BASE, bounds);
        addFace(output, side, Layer.MONITOR_LIGHT_BRIGHT, bounds);
        addFace(output, side, Layer.MONITOR_LIGHT_MEDIUM, bounds);
        addFace(output, side, Layer.MONITOR_LIGHT_DARK, bounds);
    }

    private static void addFace(
            List<Quad> output,
            Direction6 face,
            Layer layer,
            Bounds bounds
    ) {
        Uv uv = standardUv(face, bounds);
        Position[] positions = positions(face, bounds);
        List<Vertex> vertices;
        if (isYAxis(face)) {
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

    private static Position[] positions(Direction6 face, Bounds bounds) {
        return switch (face) {
            case DOWN -> new Position[]{
                new Position(bounds.x1(), bounds.y1(), bounds.z2()),
                new Position(bounds.x1(), bounds.y1(), bounds.z1()),
                new Position(bounds.x2(), bounds.y1(), bounds.z1()),
                new Position(bounds.x2(), bounds.y1(), bounds.z2())
            };
            case UP -> new Position[]{
                new Position(bounds.x1(), bounds.y2(), bounds.z1()),
                new Position(bounds.x1(), bounds.y2(), bounds.z2()),
                new Position(bounds.x2(), bounds.y2(), bounds.z2()),
                new Position(bounds.x2(), bounds.y2(), bounds.z1())
            };
            case NORTH -> new Position[]{
                new Position(bounds.x2(), bounds.y2(), bounds.z1()),
                new Position(bounds.x2(), bounds.y1(), bounds.z1()),
                new Position(bounds.x1(), bounds.y1(), bounds.z1()),
                new Position(bounds.x1(), bounds.y2(), bounds.z1())
            };
            case SOUTH -> new Position[]{
                new Position(bounds.x1(), bounds.y2(), bounds.z2()),
                new Position(bounds.x1(), bounds.y1(), bounds.z2()),
                new Position(bounds.x2(), bounds.y1(), bounds.z2()),
                new Position(bounds.x2(), bounds.y2(), bounds.z2())
            };
            case WEST -> new Position[]{
                new Position(bounds.x1(), bounds.y2(), bounds.z1()),
                new Position(bounds.x1(), bounds.y1(), bounds.z1()),
                new Position(bounds.x1(), bounds.y1(), bounds.z2()),
                new Position(bounds.x1(), bounds.y2(), bounds.z2())
            };
            case EAST -> new Position[]{
                new Position(bounds.x2(), bounds.y2(), bounds.z2()),
                new Position(bounds.x2(), bounds.y1(), bounds.z2()),
                new Position(bounds.x2(), bounds.y1(), bounds.z1()),
                new Position(bounds.x2(), bounds.y2(), bounds.z1())
            };
        };
    }

    private static Uv standardUv(Direction6 face, Bounds bounds) {
        double v1 = isYAxis(face) ? bounds.z1() : 16D - bounds.y1();
        double v2 = isYAxis(face) ? bounds.z2() : 16D - bounds.y2();
        double u1;
        double u2;
        switch (face) {
            case DOWN, UP, SOUTH -> {
                u1 = bounds.x1();
                u2 = bounds.x2();
            }
            case NORTH -> {
                u1 = 16D - bounds.x2();
                u2 = 16D - bounds.x1();
            }
            case WEST -> {
                u1 = bounds.z1();
                u2 = bounds.z2();
            }
            case EAST -> {
                u1 = 16D - bounds.z2();
                u2 = 16D - bounds.z1();
            }
            default -> throw new IllegalStateException("Unhandled face " + face);
        }
        return new Uv(u1, v1, u2, v2);
    }

    private static Direction6 cross(Direction6 forward, Direction6 axis) {
        int x = forward.stepY() * axis.stepZ() - forward.stepZ() * axis.stepY();
        int y = forward.stepZ() * axis.stepX() - forward.stepX() * axis.stepZ();
        int z = forward.stepX() * axis.stepY() - forward.stepY() * axis.stepX();
        for (Direction6 direction : Direction6.values()) {
            if (direction.stepX() == x
                    && direction.stepY() == y
                    && direction.stepZ() == z) {
                return direction;
            }
        }
        throw new IllegalArgumentException("parallel directions cannot define a ring edge");
    }

    private static boolean isYAxis(Direction6 direction) {
        return direction == Direction6.DOWN || direction == Direction6.UP;
    }

    public enum Layer {
        RING_CORNER(false, Tint.NONE),
        RING_SIDE_HORIZONTAL(false, Tint.NONE),
        RING_SIDE_VERTICAL(false, Tint.NONE),
        UNIT_BASE(false, Tint.NONE),
        LIGHT_BASE(false, Tint.NONE),
        ACCELERATOR_LIGHT(true, Tint.NONE),
        STORAGE_1K_LIGHT(true, Tint.NONE),
        STORAGE_4K_LIGHT(true, Tint.NONE),
        STORAGE_16K_LIGHT(true, Tint.NONE),
        STORAGE_64K_LIGHT(true, Tint.NONE),
        STORAGE_256K_LIGHT(true, Tint.NONE),
        MONITOR_BASE(false, Tint.NONE),
        MONITOR_LIGHT_BRIGHT(true, Tint.BRIGHT),
        MONITOR_LIGHT_MEDIUM(true, Tint.MEDIUM),
        MONITOR_LIGHT_DARK(true, Tint.DARK);

        private final boolean emissiveWhenPowered;
        private final Tint tint;

        Layer(boolean emissiveWhenPowered, Tint tint) {
            this.emissiveWhenPowered = emissiveWhenPowered;
            this.tint = tint;
        }

        public boolean emissiveWhenPowered() {
            return emissiveWhenPowered;
        }

        public Tint tint() {
            return tint;
        }
    }

    public enum Tint {
        NONE,
        BRIGHT,
        MEDIUM,
        DARK
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

    private record Corner(
            Direction6 vertical,
            Direction6 horizontal,
            Direction6 depth
    ) {

        private boolean touches(Direction6 side) {
            return side == vertical || side == horizontal || side == depth;
        }
    }

    private record Bounds(
            double x1,
            double y1,
            double z1,
            double x2,
            double y2,
            double z2
    ) {
    }

    private record Position(double x16, double y16, double z16) {

        private Vertex vertex(double u16, double v16) {
            return new Vertex(x16, y16, z16, u16, v16);
        }
    }

    private record Uv(double u1, double v1, double u2, double v2) {
    }

    private static void requireCoordinate(String name, double value) {
        if (!Double.isFinite(value) || value < 0D || value > 16D) {
            throw new IllegalArgumentException(name + " must be in [0, 16]");
        }
    }
}
