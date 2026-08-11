/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.model;

import io.github.janguenter.bluemap.ae2.profile.extendedae.ExtendedAe2235Catalog;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Exact built-in geometry omitted by BlueMap's ordinary JSON model loader. */
public final class NativePartGeometry {

    public static final String PLANE_SIDE_TEXTURE = "ae2:part/plane_sides";
    public static final String PLANE_BACK_TEXTURE = "ae2:part/transition_plane_back";
    public static final String P2P_FREQUENCY_TEXTURE = "ae2:part/p2p_tunnel_frequency";

    private static final int[][] P2P_QUAD_OFFSETS = {
            {3, 11, 2},
            {11, 11, 2},
            {3, 3, 2},
            {11, 3, 2}
    };

    private NativePartGeometry() {
    }

    public static List<Quad> plane(String partId, Direction6 face, int mask) {
        if (mask < 0 || mask > 15) {
            throw new IllegalArgumentException("plane mask must be in [0, 15]");
        }
        String front = planeFrontTexture(partId);
        int minX = (mask & PlaneNeighbor.RIGHT.maskBit()) != 0 ? 0 : 1;
        int maxX = (mask & PlaneNeighbor.LEFT.maskBit()) != 0 ? 16 : 15;
        int minY = (mask & PlaneNeighbor.DOWN.maskBit()) != 0 ? 0 : 1;
        int maxY = (mask & PlaneNeighbor.UP.maskBit()) != 0 ? 16 : 15;
        return orientedBox(
                face,
                minX,
                minY,
                0,
                maxX,
                maxY,
                1,
                direction -> switch (direction) {
                    case NORTH -> front;
                    case SOUTH -> PLANE_BACK_TEXTURE;
                    default -> PLANE_SIDE_TEXTURE;
                },
                new Rgb(1F, 1F, 1F),
                false,
                false
        );
    }

    public static String planeFrontTexture(String partId) {
        if (NativeStructuralPartCatalog.ANNIHILATION_PLANE.equals(partId)) {
            return "ae2:part/annihilation_plane";
        }
        if (NativeStructuralPartCatalog.FORMATION_PLANE.equals(partId)) {
            return "ae2:part/formation_plane";
        }
        if (ExtendedAe2235Catalog.planePartIds().contains(partId)) {
            return ExtendedAe2235Catalog.requirePlaneDefinition(partId)
                    .staticFrontTexture();
        }
        throw new IllegalArgumentException("unsupported plane part " + partId);
    }

    public static List<Quad> p2p(Direction6 face, int unsignedFrequency) {
        if (unsignedFrequency < 0 || unsignedFrequency > 0xffff) {
            throw new IllegalArgumentException("P2P frequency must be in [0, 65535]");
        }
        CableColor[] colors = new CableColor[4];
        for (int index = 0; index < colors.length; index++) {
            int nibble = unsignedFrequency >> (4 * (3 - index)) & 0xF;
            colors[index] = CableColor.values()[nibble];
        }

        List<Quad> output = new ArrayList<>(16 * 6);
        for (int[] offset : P2P_QUAD_OFFSETS) {
            for (int index = 0; index < 4; index++) {
                int startX = index % 2;
                int startY = 1 - index / 2;
                output.addAll(orientedBox(
                        face,
                        offset[0] + startX,
                        offset[1] + startY,
                        offset[2],
                        offset[0] + startX + 1,
                        offset[1] + startY + 1,
                        offset[2] + 1,
                        ignored -> P2P_FREQUENCY_TEXTURE,
                        inactiveP2pColor(colors[index]),
                        false,
                        false
                ));
            }
        }
        return List.copyOf(output);
    }

    public static FaceBasis faceBasis(Direction6 face) {
        return switch (Objects.requireNonNull(face, "face")) {
            case UP -> new FaceBasis(Direction6.EAST, Direction6.NORTH);
            case DOWN -> new FaceBasis(Direction6.WEST, Direction6.NORTH);
            case NORTH -> new FaceBasis(Direction6.WEST, Direction6.UP);
            case SOUTH -> new FaceBasis(Direction6.EAST, Direction6.UP);
            case WEST -> new FaceBasis(Direction6.SOUTH, Direction6.UP);
            case EAST -> new FaceBasis(Direction6.NORTH, Direction6.UP);
        };
    }

    private static Rgb inactiveP2pColor(CableColor color) {
        int rgb = color.darkRgb();
        float scale = 0.3F / 255F;
        return new Rgb(
                ((rgb >> 16) & 0xff) * scale,
                ((rgb >> 8) & 0xff) * scale,
                (rgb & 0xff) * scale
        );
    }

    private static List<Quad> orientedBox(
            Direction6 installedFace,
            double x1,
            double y1,
            double z1,
            double x2,
            double y2,
            double z2,
            TextureSelector textures,
            Rgb tint,
            boolean emissive,
            boolean ambientOcclusion
    ) {
        List<Quad> output = new ArrayList<>(6);
        for (Direction6 canonicalFace : Direction6.values()) {
            List<CableGeometry.Vertex> vertices = canonicalVertices(
                    canonicalFace,
                    x1,
                    y1,
                    z1,
                    x2,
                    y2,
                    z2
            );
            List<CableGeometry.Vertex> transformed = vertices.stream()
                    .map(vertex -> transform(installedFace, vertex))
                    .toList();
            output.add(new Quad(
                    transformDirection(installedFace, canonicalFace),
                    textures.texture(canonicalFace),
                    tint,
                    emissive,
                    ambientOcclusion,
                    transformed
            ));
        }
        return List.copyOf(output);
    }

    private static CableGeometry.Vertex transform(
            Direction6 installedFace,
            CableGeometry.Vertex vertex
    ) {
        FaceBasis basis = faceBasis(installedFace);
        Direction6 left = basis.right().opposite();
        double x = 8
                + (vertex.x16() - 8) * left.stepX()
                + (vertex.y16() - 8) * basis.up().stepX()
                + (8 - vertex.z16()) * installedFace.stepX();
        double y = 8
                + (vertex.x16() - 8) * left.stepY()
                + (vertex.y16() - 8) * basis.up().stepY()
                + (8 - vertex.z16()) * installedFace.stepY();
        double z = 8
                + (vertex.x16() - 8) * left.stepZ()
                + (vertex.y16() - 8) * basis.up().stepZ()
                + (8 - vertex.z16()) * installedFace.stepZ();
        return new CableGeometry.Vertex(x, y, z, vertex.u16(), vertex.v16());
    }

    private static Direction6 transformDirection(
            Direction6 installedFace,
            Direction6 canonicalFace
    ) {
        FaceBasis basis = faceBasis(installedFace);
        Direction6 left = basis.right().opposite();
        int x = canonicalFace.stepX() * left.stepX()
                + canonicalFace.stepY() * basis.up().stepX()
                - canonicalFace.stepZ() * installedFace.stepX();
        int y = canonicalFace.stepX() * left.stepY()
                + canonicalFace.stepY() * basis.up().stepY()
                - canonicalFace.stepZ() * installedFace.stepY();
        int z = canonicalFace.stepX() * left.stepZ()
                + canonicalFace.stepY() * basis.up().stepZ()
                - canonicalFace.stepZ() * installedFace.stepZ();
        for (Direction6 candidate : Direction6.values()) {
            if (candidate.stepX() == x && candidate.stepY() == y
                    && candidate.stepZ() == z) {
                return candidate;
            }
        }
        throw new IllegalStateException("part face transform did not produce a direction");
    }

    private static List<CableGeometry.Vertex> canonicalVertices(
            Direction6 face,
            double x1,
            double y1,
            double z1,
            double x2,
            double y2,
            double z2
    ) {
        List<Position> positions = switch (face) {
            case DOWN -> List.of(
                    new Position(x1, y1, z2), new Position(x1, y1, z1),
                    new Position(x2, y1, z1), new Position(x2, y1, z2));
            case UP -> List.of(
                    new Position(x1, y2, z1), new Position(x1, y2, z2),
                    new Position(x2, y2, z2), new Position(x2, y2, z1));
            case NORTH -> List.of(
                    new Position(x2, y2, z1), new Position(x2, y1, z1),
                    new Position(x1, y1, z1), new Position(x1, y2, z1));
            case SOUTH -> List.of(
                    new Position(x1, y2, z2), new Position(x1, y1, z2),
                    new Position(x2, y1, z2), new Position(x2, y2, z2));
            case WEST -> List.of(
                    new Position(x1, y2, z1), new Position(x1, y1, z1),
                    new Position(x1, y1, z2), new Position(x1, y2, z2));
            case EAST -> List.of(
                    new Position(x2, y2, z2), new Position(x2, y1, z2),
                    new Position(x2, y1, z1), new Position(x2, y2, z1));
        };
        UvRect uv = standardUv(face, x1, y1, z1, x2, y2, z2);
        UvPoint[] points = uvPoints(face, uv);
        List<CableGeometry.Vertex> vertices = new ArrayList<>(4);
        for (int index = 0; index < 4; index++) {
            Position position = positions.get(index);
            vertices.add(new CableGeometry.Vertex(
                    position.x(),
                    position.y(),
                    position.z(),
                    points[index].u(),
                    points[index].v()
            ));
        }
        return List.copyOf(vertices);
    }

    private static UvRect standardUv(
            Direction6 face,
            double x1,
            double y1,
            double z1,
            double x2,
            double y2,
            double z2
    ) {
        double v1 = face == Direction6.DOWN || face == Direction6.UP
                ? z1 : 16 - y1;
        double v2 = face == Direction6.DOWN || face == Direction6.UP
                ? z2 : 16 - y2;
        return switch (face) {
            case DOWN, UP, SOUTH -> new UvRect(x1, v1, x2, v2);
            case NORTH -> new UvRect(16 - x2, v1, 16 - x1, v2);
            case WEST -> new UvRect(z1, v1, z2, v2);
            case EAST -> new UvRect(16 - z2, v1, 16 - z1, v2);
        };
    }

    private static UvPoint[] uvPoints(Direction6 face, UvRect uv) {
        if (face == Direction6.DOWN || face == Direction6.UP) {
            return new UvPoint[]{
                    new UvPoint(uv.u1(), uv.v1()), new UvPoint(uv.u1(), uv.v2()),
                    new UvPoint(uv.u2(), uv.v2()), new UvPoint(uv.u2(), uv.v1())
            };
        }
        return new UvPoint[]{
                new UvPoint(uv.u1(), uv.v2()), new UvPoint(uv.u1(), uv.v1()),
                new UvPoint(uv.u2(), uv.v1()), new UvPoint(uv.u2(), uv.v2())
        };
    }

    public enum PlaneNeighbor {
        UP(8),
        RIGHT(4),
        DOWN(2),
        LEFT(1);

        private final int maskBit;

        PlaneNeighbor(int maskBit) {
            this.maskBit = maskBit;
        }

        public int maskBit() {
            return maskBit;
        }
    }

    public record FaceBasis(Direction6 right, Direction6 up) {
    }

    public record Rgb(float red, float green, float blue) {
        public Rgb {
            if (red < 0 || red > 1 || green < 0 || green > 1 || blue < 0 || blue > 1) {
                throw new IllegalArgumentException("RGB channels must be in [0, 1]");
            }
        }
    }

    public record Quad(
            Direction6 face,
            String texture,
            Rgb tint,
            boolean emissive,
            boolean ambientOcclusion,
            List<CableGeometry.Vertex> vertices
    ) {
        public Quad {
            Objects.requireNonNull(face, "face");
            Objects.requireNonNull(texture, "texture");
            Objects.requireNonNull(tint, "tint");
            vertices = List.copyOf(vertices);
            if (vertices.size() != 4) {
                throw new IllegalArgumentException("part quad must contain four vertices");
            }
        }
    }

    @FunctionalInterface
    private interface TextureSelector {
        String texture(Direction6 face);
    }

    private record Position(double x, double y, double z) {
    }

    private record UvRect(double u1, double v1, double u2, double v2) {
    }

    private record UvPoint(double u, double v) {
    }
}
