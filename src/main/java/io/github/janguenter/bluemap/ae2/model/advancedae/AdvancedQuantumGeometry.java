/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.model.advancedae;

import io.github.janguenter.bluemap.ae2.model.Direction6;
import io.github.janguenter.bluemap.ae2.model.advancedae.AdvancedQuantumSnapshot.Offset;
import io.github.janguenter.bluemap.ae2.model.advancedae.AdvancedQuantumSnapshot.VisualMode;
import io.github.janguenter.bluemap.ae2.profile.advancedae.AdvancedAe1612Catalog.QuantumKind;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Exact static port of Advanced AE's connected quantum-computer face construction. */
public final class AdvancedQuantumGeometry {

    public static final String INTERNAL_FACE_TEXTURE =
            "advanced_ae:block/crafting/quantum_internal_formed_face";
    public static final String INTERNAL_SIDE_TEXTURE =
            "advanced_ae:block/crafting/quantum_internal_formed_sides";
    public static final String STRUCTURE_FACE_TEXTURE =
            "advanced_ae:block/crafting/quantum_structure_formed_face";
    public static final String STRUCTURE_SIDE_TEXTURE =
            "advanced_ae:block/crafting/quantum_structure_formed_sides";

    private AdvancedQuantumGeometry() {
    }

    public static List<Quad> forSnapshot(AdvancedQuantumSnapshot rawSnapshot) {
        AdvancedQuantumSnapshot snapshot = Objects.requireNonNull(
                rawSnapshot,
                "rawSnapshot"
        ).staticProjection();
        VisualMode mode = snapshot.visualMode();
        if (!mode.usesConnectedGeometry()) {
            return List.of();
        }

        List<Quad> output = new ArrayList<>(54);
        for (Direction6 face : Direction6.values()) {
            if (snapshot.hasCompatibleNeighbor(Offset.direct(face))) {
                continue;
            }
            Surface faceSurface = mode == VisualMode.CONNECTED_STRUCTURE
                    ? Surface.STRUCTURE_FACE : Surface.INTERNAL_FACE;
            Surface sideSurface = mode == VisualMode.CONNECTED_STRUCTURE
                    ? Surface.STRUCTURE_SIDE : Surface.INTERNAL_SIDE;
            output.add(new Quad(
                    face,
                    faceSurface,
                    Corner.FULL,
                    -1,
                    vertices(face, Corner.FULL, -1, false)
            ));
            for (Corner corner : Corner.quadrants()) {
                Check check = check(face, corner);
                int tile = sideTile(
                        snapshot.hasCompatibleNeighbor(check.first()),
                        snapshot.hasCompatibleNeighbor(check.second()),
                        snapshot.hasCompatibleNeighbor(check.diagonal())
                );
                if (tile < 0) {
                    continue;
                }
                output.add(new Quad(
                        face,
                        sideSurface,
                        corner,
                        tile,
                        vertices(face, corner, tile, false)
                ));
                if (mode == VisualMode.CONNECTED_STRUCTURE) {
                    output.add(new Quad(
                            face.opposite(),
                            Surface.STRUCTURE_SIDE_INNER,
                            corner,
                            tile,
                            vertices(face, corner, tile, true)
                    ));
                }
            }
        }
        return List.copyOf(output);
    }

    /** Exact source tile: empty, inner corner, vertical arm, horizontal arm, or omitted. */
    public static int sideTile(boolean first, boolean second, boolean diagonal) {
        if (!first && !second) {
            return 0;
        }
        if (first && second && !diagonal) {
            return 1;
        }
        if (!first && second) {
            return 2;
        }
        if (first && !second) {
            return 3;
        }
        return -1;
    }

    /** Exact static material selected for one role/surface pair. */
    public static Material material(QuantumKind kind, Surface surface) {
        Objects.requireNonNull(kind, "kind");
        Objects.requireNonNull(surface, "surface");
        boolean structureSurface = surface == Surface.STRUCTURE_FACE
                || surface == Surface.STRUCTURE_SIDE
                || surface == Surface.STRUCTURE_SIDE_INNER;
        if (kind.isStructure() != structureSurface) {
            throw new IllegalArgumentException("quantum role and connected surface disagree");
        }
        String texture = switch (surface) {
            case INTERNAL_FACE -> INTERNAL_FACE_TEXTURE;
            case INTERNAL_SIDE -> INTERNAL_SIDE_TEXTURE;
            case STRUCTURE_FACE -> STRUCTURE_FACE_TEXTURE;
            case STRUCTURE_SIDE, STRUCTURE_SIDE_INNER -> STRUCTURE_SIDE_TEXTURE;
        };
        RenderLayer renderLayer = surface == Surface.STRUCTURE_FACE
                ? RenderLayer.TRANSLUCENT : RenderLayer.CUTOUT;
        return new Material(texture, renderLayer, false, false, false, 0);
    }

    public static Check check(Direction6 face, Corner corner) {
        Objects.requireNonNull(face, "face");
        if (corner == null || corner == Corner.FULL) {
            throw new IllegalArgumentException("a connected side check requires a quadrant");
        }
        boolean upper = corner == Corner.LEFT_UP || corner == Corner.RIGHT_UP;
        boolean left = corner == Corner.LEFT_UP || corner == Corner.LEFT_DOWN;
        int vertical = upper ? 1 : -1;
        return switch (face) {
            case WEST, EAST -> {
                int x = face.stepX();
                int z = left ? x : -x;
                yield new Check(
                        new Offset(0, 0, z),
                        new Offset(0, vertical, 0),
                        new Offset(0, vertical, z)
                );
            }
            case NORTH, SOUTH -> {
                int z = face.stepZ();
                int x = left ? -z : z;
                yield new Check(
                        new Offset(x, 0, 0),
                        new Offset(0, vertical, 0),
                        new Offset(x, vertical, 0)
                );
            }
            case DOWN, UP -> {
                int y = face.stepY();
                int z = left ? 1 : -1;
                int x = upper ? -y : y;
                yield new Check(
                        new Offset(0, 0, z),
                        new Offset(x, 0, 0),
                        new Offset(x, 0, z)
                );
            }
        };
    }

    private static List<Vertex> vertices(
            Direction6 face,
            Corner corner,
            int tile,
            boolean inward
    ) {
        List<Position> full = facePositions(face);
        List<Position> positions = switch (corner) {
            case FULL -> full;
            case LEFT_UP -> List.of(
                    full.get(0),
                    midpoint(full.get(0), full.get(1)),
                    center(full),
                    midpoint(full.get(3), full.get(0))
            );
            case RIGHT_UP -> List.of(
                    midpoint(full.get(0), full.get(3)),
                    center(full),
                    midpoint(full.get(2), full.get(3)),
                    full.get(3)
            );
            case LEFT_DOWN -> List.of(
                    midpoint(full.get(0), full.get(1)),
                    full.get(1),
                    midpoint(full.get(1), full.get(2)),
                    center(full)
            );
            case RIGHT_DOWN -> List.of(
                    center(full),
                    midpoint(full.get(1), full.get(2)),
                    full.get(2),
                    midpoint(full.get(2), full.get(3))
            );
        };
        if (inward) {
            positions = List.of(
                    positions.get(3),
                    positions.get(2),
                    positions.get(1),
                    positions.get(0)
            );
        }
        List<Uv> uv = corner == Corner.FULL ? fullUv() : quadrantUv(corner, tile);
        List<Vertex> result = new ArrayList<>(4);
        for (int index = 0; index < 4; index++) {
            Position position = positions.get(index);
            Uv coordinate = uv.get(index);
            result.add(new Vertex(
                    position.x16(),
                    position.y16(),
                    position.z16(),
                    coordinate.u16(),
                    coordinate.v16()
            ));
        }
        return List.copyOf(result);
    }

    private static List<Position> facePositions(Direction6 face) {
        return switch (face) {
            case DOWN -> List.of(
                    new Position(0, 0, 16), new Position(0, 0, 0),
                    new Position(16, 0, 0), new Position(16, 0, 16)
            );
            case UP -> List.of(
                    new Position(16, 16, 16), new Position(16, 16, 0),
                    new Position(0, 16, 0), new Position(0, 16, 16)
            );
            case NORTH -> List.of(
                    new Position(16, 16, 0), new Position(16, 0, 0),
                    new Position(0, 0, 0), new Position(0, 16, 0)
            );
            case SOUTH -> List.of(
                    new Position(0, 16, 16), new Position(0, 0, 16),
                    new Position(16, 0, 16), new Position(16, 16, 16)
            );
            case WEST -> List.of(
                    new Position(0, 16, 0), new Position(0, 0, 0),
                    new Position(0, 0, 16), new Position(0, 16, 16)
            );
            case EAST -> List.of(
                    new Position(16, 16, 16), new Position(16, 0, 16),
                    new Position(16, 0, 0), new Position(16, 16, 0)
            );
        };
    }

    private static List<Uv> fullUv() {
        return List.of(new Uv(0, 0), new Uv(0, 16), new Uv(16, 16), new Uv(16, 0));
    }

    private static List<Uv> quadrantUv(Corner corner, int tile) {
        if (tile < 0 || tile > 3) {
            throw new IllegalArgumentException("side tile must be in [0, 3]");
        }
        double u0 = tile == 1 || tile == 3 ? 8 : 0;
        double u1 = tile == 1 || tile == 3 ? 16 : 8;
        double v0 = tile == 2 || tile == 3 ? 8 : 0;
        double v1 = tile == 2 || tile == 3 ? 16 : 8;
        return switch (corner) {
            case LEFT_UP -> List.of(
                    new Uv(u0, v0), new Uv(u0, v1),
                    new Uv(u1, v1), new Uv(u1, v0)
            );
            case RIGHT_UP -> List.of(
                    new Uv(u1, v0), new Uv(u1, v1),
                    new Uv(u0, v1), new Uv(u0, v0)
            );
            case LEFT_DOWN -> List.of(
                    new Uv(u0, v1), new Uv(u0, v0),
                    new Uv(u1, v0), new Uv(u1, v1)
            );
            case RIGHT_DOWN -> List.of(
                    new Uv(u1, v1), new Uv(u1, v0),
                    new Uv(u0, v0), new Uv(u0, v1)
            );
            case FULL -> throw new IllegalArgumentException("full face has no quadrant tile");
        };
    }

    private static Position midpoint(Position first, Position second) {
        return new Position(
                (first.x16() + second.x16()) / 2,
                (first.y16() + second.y16()) / 2,
                (first.z16() + second.z16()) / 2
        );
    }

    private static Position center(List<Position> positions) {
        return new Position(
                positions.stream().mapToDouble(Position::x16).average().orElseThrow(),
                positions.stream().mapToDouble(Position::y16).average().orElseThrow(),
                positions.stream().mapToDouble(Position::z16).average().orElseThrow()
        );
    }

    public enum Surface {
        INTERNAL_FACE,
        INTERNAL_SIDE,
        STRUCTURE_FACE,
        STRUCTURE_SIDE,
        STRUCTURE_SIDE_INNER
    }

    public enum RenderLayer {
        CUTOUT,
        TRANSLUCENT
    }

    public enum Corner {
        FULL,
        LEFT_UP,
        RIGHT_UP,
        LEFT_DOWN,
        RIGHT_DOWN;

        public static List<Corner> quadrants() {
            return List.of(LEFT_UP, RIGHT_UP, LEFT_DOWN, RIGHT_DOWN);
        }
    }

    public record Check(Offset first, Offset second, Offset diagonal) {

        public Check {
            Objects.requireNonNull(first, "first");
            Objects.requireNonNull(second, "second");
            Objects.requireNonNull(diagonal, "diagonal");
        }
    }

    public record Vertex(double x16, double y16, double z16, double u16, double v16) {

        public Vertex {
            requireUnit("x16", x16);
            requireUnit("y16", y16);
            requireUnit("z16", z16);
            requireUnit("u16", u16);
            requireUnit("v16", v16);
        }
    }

    /** Static texture and bake flags; powered overlays are deliberately not selected. */
    public record Material(
            String texture,
            RenderLayer renderLayer,
            boolean emissive,
            boolean ambientOcclusion,
            boolean usesBlockLight,
            int animationFrame
    ) {

        public Material {
            Objects.requireNonNull(texture, "texture");
            Objects.requireNonNull(renderLayer, "renderLayer");
            if (emissive || ambientOcclusion || usesBlockLight || animationFrame != 0) {
                throw new IllegalArgumentException("Advanced AE static material must be unlit");
            }
        }
    }

    public record Quad(
            Direction6 face,
            Surface surface,
            Corner corner,
            int tile,
            List<Vertex> vertices
    ) {

        public Quad {
            Objects.requireNonNull(face, "face");
            Objects.requireNonNull(surface, "surface");
            Objects.requireNonNull(corner, "corner");
            vertices = List.copyOf(Objects.requireNonNull(vertices, "vertices"));
            if (vertices.size() != 4
                    || corner == Corner.FULL && tile != -1
                    || corner != Corner.FULL && (tile < 0 || tile > 3)) {
                throw new IllegalArgumentException("invalid Advanced AE quantum quad");
            }
        }
    }

    private record Position(double x16, double y16, double z16) {
    }

    private record Uv(double u16, double v16) {
    }

    private static void requireUnit(String name, double value) {
        if (!Double.isFinite(value) || value < 0 || value > 16) {
            throw new IllegalArgumentException(name + " must be in [0, 16]");
        }
    }
}
