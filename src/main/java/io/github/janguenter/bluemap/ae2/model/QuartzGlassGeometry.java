/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Dependency-free AE2 19.2.17 connected-quartz-glass geometry and RNG. */
public final class QuartzGlassGeometry {

    private static final long RANDOM_MULTIPLIER = 0x5DEECE66DL;
    private static final long RANDOM_ADDEND = 0xBL;
    private static final long RANDOM_MASK = (1L << 48) - 1;

    private QuartzGlassGeometry() {
    }

    public static List<Quad> forSnapshot(
            QuartzGlassSnapshot snapshot,
            int x,
            int y,
            int z
    ) {
        Objects.requireNonNull(snapshot, "snapshot");
        TextureSelection selection = textureSelection(x, y, z);
        List<Quad> output = new ArrayList<>(12);
        for (Direction6 face : Direction6.values()) {
            if (snapshot.isConnected(face)) {
                continue;
            }
            output.add(new Quad(
                    face,
                    Layer.BASE,
                    selection.textureIndex(),
                    vertices(face, selection.uMax16(), selection.vMax16())
            ));
            int frameMask = snapshot.frameMask(face);
            if (frameMask != 0) {
                output.add(new Quad(
                        face,
                        Layer.FRAME,
                        frameMask,
                        vertices(face, 16D, 16D)
                ));
            }
        }
        return List.copyOf(output);
    }

    public static TextureSelection textureSelection(int x, int y, int z) {
        LegacyRandom random = new LegacyRandom(positionSeed(x, y, z));
        int randomOffset = random.nextInt4();
        int vOffset = random.nextInt4();
        int textureIndex = (randomOffset + random.nextInt4()) % 4;
        double divisor = textureIndex < 2 ? 2D : 1D;
        return new TextureSelection(
                textureIndex,
                16D - randomOffset / divisor,
                16D - vOffset / divisor
        );
    }

    /** Exact Minecraft 1.21.1 block-position seed, including Java overflow. */
    public static long positionSeed(int x, int y, int z) {
        long seed = (long) (x * 3_129_871) ^ (long) z * 116_129_781L ^ y;
        seed = seed * seed * 42_317_861L + seed * 11L;
        return seed >> 16;
    }

    private static List<Vertex> vertices(Direction6 face, double uMax16, double vMax16) {
        Position[] corners = switch (face) {
            case DOWN -> new Position[]{
                    new Position(0, 0, 16),
                    new Position(0, 0, 0),
                    new Position(16, 0, 0),
                    new Position(16, 0, 16)
            };
            case UP -> new Position[]{
                    new Position(16, 16, 16),
                    new Position(16, 16, 0),
                    new Position(0, 16, 0),
                    new Position(0, 16, 16)
            };
            case NORTH -> new Position[]{
                    new Position(16, 16, 0),
                    new Position(16, 0, 0),
                    new Position(0, 0, 0),
                    new Position(0, 16, 0)
            };
            case SOUTH -> new Position[]{
                    new Position(0, 16, 16),
                    new Position(0, 0, 16),
                    new Position(16, 0, 16),
                    new Position(16, 16, 16)
            };
            case WEST -> new Position[]{
                    new Position(0, 16, 0),
                    new Position(0, 0, 0),
                    new Position(0, 0, 16),
                    new Position(0, 16, 16)
            };
            case EAST -> new Position[]{
                    new Position(16, 16, 16),
                    new Position(16, 0, 16),
                    new Position(16, 0, 0),
                    new Position(16, 16, 0)
            };
        };
        return List.of(
                corners[0].vertex(0, 0),
                corners[1].vertex(0, vMax16),
                corners[2].vertex(uMax16, vMax16),
                corners[3].vertex(uMax16, 0)
        );
    }

    public enum Layer {
        BASE,
        FRAME
    }

    public record TextureSelection(int textureIndex, double uMax16, double vMax16) {

        public TextureSelection {
            if (textureIndex < 0 || textureIndex > 3) {
                throw new IllegalArgumentException("textureIndex must be in [0, 3]");
            }
            requireUv("uMax16", uMax16);
            requireUv("vMax16", vMax16);
        }
    }

    public record Vertex(double x16, double y16, double z16, double u16, double v16) {

        public Vertex {
            requireCoordinate("x16", x16);
            requireCoordinate("y16", y16);
            requireCoordinate("z16", z16);
            requireUv("u16", u16);
            requireUv("v16", v16);
        }
    }

    public record Quad(Direction6 face, Layer layer, int textureIndex, List<Vertex> vertices) {

        public Quad {
            Objects.requireNonNull(face, "face");
            Objects.requireNonNull(layer, "layer");
            vertices = List.copyOf(Objects.requireNonNull(vertices, "vertices"));
            if (vertices.size() != 4) {
                throw new IllegalArgumentException("a quad must contain four vertices");
            }
            if (layer == Layer.BASE && (textureIndex < 0 || textureIndex > 3)) {
                throw new IllegalArgumentException("base texture index must be in [0, 3]");
            }
            if (layer == Layer.FRAME && (textureIndex < 1 || textureIndex > 15)) {
                throw new IllegalArgumentException("frame texture mask must be in [1, 15]");
            }
        }
    }

    private record Position(double x16, double y16, double z16) {

        private Vertex vertex(double u16, double v16) {
            return new Vertex(x16, y16, z16, u16, v16);
        }
    }

    private static void requireCoordinate(String name, double value) {
        if (!Double.isFinite(value) || value < 0D || value > 16D) {
            throw new IllegalArgumentException(name + " must be in [0, 16]");
        }
    }

    private static void requireUv(String name, double value) {
        if (!Double.isFinite(value) || value < 0D || value > 16D) {
            throw new IllegalArgumentException(name + " must be in [0, 16]");
        }
    }

    private static final class LegacyRandom {

        private long seed;

        private LegacyRandom(long seed) {
            this.seed = (seed ^ RANDOM_MULTIPLIER) & RANDOM_MASK;
        }

        private int nextInt4() {
            return (int) ((4L * next(31)) >> 31);
        }

        private int next(int bits) {
            seed = (seed * RANDOM_MULTIPLIER + RANDOM_ADDEND) & RANDOM_MASK;
            return (int) (seed >>> (48 - bits));
        }
    }
}
