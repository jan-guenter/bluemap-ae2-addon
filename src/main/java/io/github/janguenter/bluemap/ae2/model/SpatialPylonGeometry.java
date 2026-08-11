/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Dependency-free port of AE2 19.2.17's two-layer spatial-pylon cube. */
public final class SpatialPylonGeometry {

    private SpatialPylonGeometry() {
    }

    /** Always twelve quads / 24 non-emissive triangles. */
    public static List<Quad> forSnapshot(SpatialPylonSnapshot snapshot) {
        Objects.requireNonNull(snapshot, "snapshot");
        List<Quad> output = new ArrayList<>(12);
        for (Layer layer : Layer.values()) {
            for (Direction6 face : Direction6.values()) {
                UvTransform transform = uvTransform(snapshot, face);
                output.add(new Quad(
                        layer,
                        face,
                        texture(snapshot, layer, face),
                        faceVertices(face, transform)
                ));
            }
        }
        return List.copyOf(output);
    }

    private static Texture texture(
            SpatialPylonSnapshot snapshot,
            Layer layer,
            Direction6 face
    ) {
        if (!snapshot.formed()) {
            return layer == Layer.OUTER ? Texture.BASE : Texture.DIM;
        }
        boolean cap = snapshot.axis().contains(face);
        if (layer == Layer.OUTER) {
            if (cap) {
                return Texture.BASE;
            }
            return snapshot.axisPosition() == SpatialPylonSnapshot.AxisPosition.MIDDLE
                    ? Texture.BASE_SPANNED : Texture.BASE_END;
        }
        if (cap) {
            return Texture.RED;
        }
        return snapshot.axisPosition() == SpatialPylonSnapshot.AxisPosition.MIDDLE
                ? Texture.RED_SPANNED : Texture.RED_END;
    }

    private static UvTransform uvTransform(
            SpatialPylonSnapshot snapshot,
            Direction6 face
    ) {
        if (!snapshot.formed()) {
            return UvTransform.IDENTITY;
        }
        int rotation = 0;
        boolean flipV = false;
        switch (snapshot.axis()) {
            case Y -> {
                if (snapshot.axisPosition() == SpatialPylonSnapshot.AxisPosition.END
                        && face != Direction6.UP && face != Direction6.DOWN) {
                    flipV = true;
                }
            }
            case X -> {
                if (face == Direction6.NORTH || face == Direction6.SOUTH) {
                    rotation = 1;
                } else if (face == Direction6.UP || face == Direction6.DOWN) {
                    rotation = 3;
                }
                if (snapshot.axisPosition() == SpatialPylonSnapshot.AxisPosition.START) {
                    flipV = face == Direction6.UP
                            || face == Direction6.DOWN
                            || face == Direction6.NORTH;
                } else if (snapshot.axisPosition() == SpatialPylonSnapshot.AxisPosition.END) {
                    flipV = face == Direction6.SOUTH;
                }
            }
            case Z -> {
                if (face == Direction6.WEST || face == Direction6.EAST) {
                    rotation = 1;
                }
                if (snapshot.axisPosition() == SpatialPylonSnapshot.AxisPosition.START) {
                    flipV = face == Direction6.UP || face == Direction6.EAST;
                } else if (snapshot.axisPosition() == SpatialPylonSnapshot.AxisPosition.END) {
                    flipV = face == Direction6.DOWN || face == Direction6.WEST;
                }
            }
        }
        return new UvTransform(rotation, flipV);
    }

    private static List<Vertex> faceVertices(Direction6 face, UvTransform transform) {
        Position[] positions = positions(face);
        float u1 = 0F;
        float u2 = 1F;
        float v1 = face == Direction6.DOWN || face == Direction6.UP ? 0F : 1F;
        float v2 = face == Direction6.DOWN || face == Direction6.UP ? 1F : 0F;
        if (transform.flipV()) {
            float swap = v1;
            v1 = v2;
            v2 = swap;
        }
        float[][] base = face == Direction6.DOWN || face == Direction6.UP
                ? new float[][]{{u1, v1}, {u1, v2}, {u2, v2}, {u2, v1}}
                : new float[][]{{u1, v2}, {u1, v1}, {u2, v1}, {u2, v2}};
        float[][] rotated = new float[4][2];
        for (int index = 0; index < 4; index++) {
            int target = (index + 4 - transform.rotation()) % 4;
            rotated[target] = base[index];
        }

        List<Vertex> vertices = new ArrayList<>(4);
        for (int index = 0; index < 4; index++) {
            vertices.add(new Vertex(
                    positions[index].x() * 16F,
                    positions[index].y() * 16F,
                    positions[index].z() * 16F,
                    rotated[index][0] * 16F,
                    rotated[index][1] * 16F
            ));
        }
        return List.copyOf(vertices);
    }

    private static Position[] positions(Direction6 face) {
        return switch (face) {
            case DOWN -> new Position[]{
                    new Position(0, 0, 1), new Position(0, 0, 0),
                    new Position(1, 0, 0), new Position(1, 0, 1)
            };
            case UP -> new Position[]{
                    new Position(0, 1, 0), new Position(0, 1, 1),
                    new Position(1, 1, 1), new Position(1, 1, 0)
            };
            case NORTH -> new Position[]{
                    new Position(1, 1, 0), new Position(1, 0, 0),
                    new Position(0, 0, 0), new Position(0, 1, 0)
            };
            case SOUTH -> new Position[]{
                    new Position(0, 1, 1), new Position(0, 0, 1),
                    new Position(1, 0, 1), new Position(1, 1, 1)
            };
            case WEST -> new Position[]{
                    new Position(0, 1, 0), new Position(0, 0, 0),
                    new Position(0, 0, 1), new Position(0, 1, 1)
            };
            case EAST -> new Position[]{
                    new Position(1, 1, 1), new Position(1, 0, 1),
                    new Position(1, 0, 0), new Position(1, 1, 0)
            };
        };
    }

    public enum Layer {
        OUTER,
        INNER
    }

    public enum Texture {
        BASE,
        BASE_END,
        BASE_SPANNED,
        DIM,
        DIM_END,
        DIM_SPANNED,
        RED,
        RED_END,
        RED_SPANNED
    }

    public record Vertex(double x16, double y16, double z16, double u16, double v16) {

        public Vertex {
            requireUnitCoordinate(x16);
            requireUnitCoordinate(y16);
            requireUnitCoordinate(z16);
            requireUnitCoordinate(u16);
            requireUnitCoordinate(v16);
        }
    }

    public record Quad(
            Layer layer,
            Direction6 face,
            Texture texture,
            List<Vertex> vertices
    ) {

        public Quad {
            Objects.requireNonNull(layer, "layer");
            Objects.requireNonNull(face, "face");
            Objects.requireNonNull(texture, "texture");
            vertices = List.copyOf(Objects.requireNonNull(vertices, "vertices"));
            if (vertices.size() != 4) {
                throw new IllegalArgumentException("a pylon quad must contain four vertices");
            }
        }
    }

    private record Position(float x, float y, float z) {
    }

    private record UvTransform(int rotation, boolean flipV) {
        private static final UvTransform IDENTITY = new UvTransform(0, false);

        private UvTransform {
            if (rotation < 0 || rotation > 3) {
                throw new IllegalArgumentException("rotation must be in [0, 3]");
            }
        }
    }

    private static void requireUnitCoordinate(double value) {
        if (!Double.isFinite(value) || value < 0D || value > 16D) {
            throw new IllegalArgumentException("coordinate must be in [0, 16]");
        }
    }
}
