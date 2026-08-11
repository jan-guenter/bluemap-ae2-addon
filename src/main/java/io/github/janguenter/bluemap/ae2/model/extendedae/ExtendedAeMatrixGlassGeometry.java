/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.model.extendedae;

import io.github.janguenter.bluemap.ae2.model.Direction6;
import io.github.janguenter.bluemap.ae2.model.extendedae.ExtendedAeMatrixGlassSnapshot.Offset;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Exact dependency-free port of ExtendedAE's dynamic Assembler Matrix Glass quads. */
public final class ExtendedAeMatrixGlassGeometry {

    public static final String SIDE_TEXTURE =
            "extendedae:block/assembler_matrix/glass/sides";
    public static final List<String> FACE_TEXTURES = List.of(
            "extendedae:block/assembler_matrix/glass/face_a",
            "extendedae:block/assembler_matrix/glass/face_b",
            "extendedae:block/assembler_matrix/glass/face_c"
    );

    private ExtendedAeMatrixGlassGeometry() {
    }

    public static List<Quad> forSnapshot(ExtendedAeMatrixGlassSnapshot rawSnapshot) {
        ExtendedAeMatrixGlassSnapshot snapshot = Objects.requireNonNull(
                rawSnapshot,
                "rawSnapshot"
        ).staticProjection();
        List<Quad> output = new ArrayList<>(30);
        for (Direction6 face : Direction6.values()) {
            if (snapshot.faceBlocked(face)) {
                continue;
            }
            int faceIndex = snapshot.faceTextureIndex();
            output.add(new Quad(
                    face,
                    Surface.FACE,
                    Corner.FULL,
                    faceIndex,
                    FACE_TEXTURES.get(faceIndex),
                    vertices(face, Corner.FULL, -1)
            ));
            for (Corner corner : Corner.quadrants()) {
                Check check = check(face, corner);
                int tile = sideTile(
                        snapshot.matches(check.first()),
                        snapshot.matches(check.second()),
                        snapshot.matches(check.diagonal())
                );
                if (tile >= 0) {
                    output.add(new Quad(
                            face,
                            Surface.SIDE,
                            corner,
                            tile,
                            SIDE_TEXTURE,
                            vertices(face, corner, tile)
                    ));
                }
            }
        }
        return List.copyOf(output);
    }

    /** Exact source index: empty, inner corner, vertical arm, horizontal arm, or omitted. */
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

    /** Three exact appearance cells queried for one source corner index. */
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

    private static List<Vertex> vertices(Direction6 face, Corner corner, int tile) {
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
        FACE,
        SIDE
    }

    public enum RenderLayer {
        CUTOUT
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

    public record Quad(
            Direction6 face,
            Surface surface,
            Corner corner,
            int textureIndex,
            String texture,
            List<Vertex> vertices
    ) {

        public Quad {
            Objects.requireNonNull(face, "face");
            Objects.requireNonNull(surface, "surface");
            Objects.requireNonNull(corner, "corner");
            Objects.requireNonNull(texture, "texture");
            vertices = List.copyOf(vertices);
            if (vertices.size() != 4
                    || surface == Surface.FACE && (corner != Corner.FULL
                    || textureIndex < 0 || textureIndex >= FACE_TEXTURES.size())
                    || surface == Surface.SIDE && (corner == Corner.FULL
                    || textureIndex < 0 || textureIndex > 3)) {
                throw new IllegalArgumentException("invalid matrix-glass quad");
            }
        }

        public Material material() {
            return new Material(texture, RenderLayer.CUTOUT, false, false, false, 0);
        }
    }

    /** Static glass material flags copied from the exact dynamic baked model. */
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
                throw new IllegalArgumentException("matrix-glass static material must be unlit");
            }
        }
    }

    public record Vertex(double x16, double y16, double z16, double u16, double v16) {
    }

    private record Position(double x16, double y16, double z16) {
    }

    private record Uv(double u16, double v16) {
    }
}
