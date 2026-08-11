/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Exact thin four-strip ring used by the bounded terminal/stone facade lane. */
public final class FacadeGeometry {

    public static final double THICKNESS_16 = 0.968D;
    public static final double HOLE_MIN_16 = 2D;
    public static final double HOLE_MAX_16 = 14D;
    private static final double CORNER_KICK_EPSILON = 1.0E-5D * 16D;

    private static final List<List<Quad>> RINGS = buildRings();

    private FacadeGeometry() {
    }

    public static List<Quad> ring(Direction6 direction) {
        return RINGS.get(Objects.requireNonNull(direction, "direction").ordinal());
    }

    /**
     * Builds one source-faithful thin facade slab, optionally clipped around a
     * same-face part. Perpendicular facade faces suppress overlapping side
     * quads, matching AE2's opaque facade face stripping for all 64 masks.
     */
    public static List<Quad> layout(
            Direction6 direction,
            Double holeMin16,
            Double holeMax16,
            int facadeMask
    ) {
        return layout(direction, holeMin16, holeMax16, facadeMask, false);
    }

    /**
     * Builds the native facade geometry after AE2's bounding-box clamp,
     * opaque-face stripping, and inner-corner kick transforms.
     */
    public static List<Quad> layout(
            Direction6 direction,
            Double holeMin16,
            Double holeMax16,
            int facadeMask,
            boolean transparent
    ) {
        Objects.requireNonNull(direction, "direction");
        CableBusSnapshot.validateMask(facadeMask);
        if ((holeMin16 == null) != (holeMax16 == null)) {
            throw new IllegalArgumentException("facade hole bounds must both be present");
        }
        if (holeMin16 != null && (holeMin16 < 0 || holeMax16 > 16
                || holeMin16 >= holeMax16)) {
            throw new IllegalArgumentException("invalid facade hole bounds");
        }

        Box fullBounds = slab(direction);
        Box facadeBox = transparent
                ? insetTransparentBounds(fullBounds, direction, facadeMask)
                : fullBounds;
        List<Box> boxes;
        if (holeMin16 == null) {
            boxes = List.of(facadeBox);
        } else {
            boxes = ringAroundHole(facadeBox, direction, holeMin16, holeMax16);
        }
        List<Quad> output = new ArrayList<>(boxes.size() * 6);
        for (Box box : boxes) {
            addNativeBox(output, box, direction, facadeMask, fullBounds);
        }
        return List.copyOf(output);
    }

    /**
     * Applies AE2's facade clamp pipeline to one already-baked source quad.
     * The source vertex order, UVs and nominal face are retained through the
     * same clamp, face-strip, corner-kick and bilinear re-interpolation steps.
     */
    public static List<Quad> clip(
            Direction6 nominalFace,
            List<CableGeometry.Vertex> sourceVertices,
            Direction6 facadeDirection,
            Bounds cutout,
            int facadeMask,
            boolean transparent
    ) {
        Objects.requireNonNull(nominalFace, "nominalFace");
        Objects.requireNonNull(sourceVertices, "sourceVertices");
        Objects.requireNonNull(facadeDirection, "facadeDirection");
        CableBusSnapshot.validateMask(facadeMask);
        if (sourceVertices.size() != 4) {
            throw new IllegalArgumentException("source facade quad needs four vertices");
        }

        Box fullBounds = slab(facadeDirection);
        Box facadeBox = transparent
                ? insetTransparentBounds(fullBounds, facadeDirection, facadeMask)
                : fullBounds;
        List<Box> boxes = cutout == null
                ? List.of(facadeBox)
                : ringAroundHole(facadeBox, facadeDirection, cutout.box());
        List<Quad> output = new ArrayList<>(boxes.size());
        for (Box box : boxes) {
            List<Position> clamped = sourceVertices.stream()
                    .map(vertex -> clamp(vertex, box))
                    .toList();
            if (degenerate(clamped, nominalFace)
                    || shouldStrip(
                            nominalFace,
                            clamped,
                            facadeDirection,
                            facadeMask,
                            fullBounds
                    )) {
                continue;
            }
            List<CableGeometry.Vertex> transformed = new ArrayList<>(4);
            for (Position position : clamped) {
                Position kicked = kickCorner(
                        position,
                        nominalFace,
                        facadeDirection,
                        facadeMask,
                        fullBounds
                );
                UvPoint uv = interpolateSourceUv(
                        nominalFace,
                        sourceVertices,
                        kicked
                );
                transformed.add(new CableGeometry.Vertex(
                        kicked.x(), kicked.y(), kicked.z(), uv.u16(), uv.v16()
                ));
            }
            output.add(new Quad(nominalFace, transformed));
        }
        return List.copyOf(output);
    }

    /** Exact thin facade bounds in block-local sixteenths. */
    public static Bounds slabBounds(Direction6 direction) {
        return Bounds.from(slab(Objects.requireNonNull(direction, "direction")));
    }

    /** Actual facade clamp bounds after transparent-edge insets. */
    public static Bounds facadeBounds(
            Direction6 direction,
            int facadeMask,
            boolean transparent
    ) {
        Objects.requireNonNull(direction, "direction");
        CableBusSnapshot.validateMask(facadeMask);
        Box full = slab(direction);
        return Bounds.from(transparent
                ? insetTransparentBounds(full, direction, facadeMask) : full);
    }

    private static Position clamp(CableGeometry.Vertex vertex, Box bounds) {
        return new Position(
                clamp(vertex.x16(), bounds.minX(), bounds.maxX()),
                clamp(vertex.y16(), bounds.minY(), bounds.maxY()),
                clamp(vertex.z16(), bounds.minZ(), bounds.maxZ())
        );
    }

    private static double clamp(double value, double minimum, double maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }

    private static boolean degenerate(List<Position> positions, Direction6 face) {
        int firstAxis = tangentAxisA(face);
        int secondAxis = tangentAxisB(face);
        return allSame(positions, firstAxis) || allSame(positions, secondAxis);
    }

    private static boolean allSame(List<Position> positions, int axis) {
        double first = coordinate(positions.getFirst(), axis);
        return positions.stream().allMatch(position ->
                Double.compare(coordinate(position, axis), first) == 0);
    }

    private static UvPoint interpolateSourceUv(
            Direction6 face,
            List<CableGeometry.Vertex> source,
            Position target
    ) {
        int firstAxis = tangentAxisA(face);
        int secondAxis = tangentAxisB(face);
        CableGeometry.Vertex origin = source.getFirst();
        double x0 = coordinate(origin, firstAxis);
        double y0 = coordinate(origin, secondAxis);
        int p10 = -1;
        int p01 = -1;
        int p11 = -1;
        for (int index = 1; index < source.size(); index++) {
            CableGeometry.Vertex vertex = source.get(index);
            double x = coordinate(vertex, firstAxis);
            double y = coordinate(vertex, secondAxis);
            if (Double.compare(y0, y) == 0) {
                p10 = index;
            } else if (Double.compare(x0, x) == 0) {
                p01 = index;
            } else {
                p11 = index;
            }
        }
        if (p10 < 0 || p01 < 0 || p11 < 0) {
            throw new IllegalArgumentException("source facade quad cannot be interpolated");
        }
        double x1 = coordinate(source.get(p10), firstAxis);
        double y1 = coordinate(source.get(p01), secondAxis);
        if (Double.compare(x0, x1) == 0 || Double.compare(y0, y1) == 0) {
            throw new IllegalArgumentException("source facade quad cannot be interpolated");
        }
        double ratioX = (coordinate(target, firstAxis) - x0) / (x1 - x0);
        double ratioY = (coordinate(target, secondAxis) - y0) / (y1 - y0);
        return new UvPoint(
                bilinear(source, CableGeometry.Vertex::u16, p10, p01, p11,
                        ratioX, ratioY),
                bilinear(source, CableGeometry.Vertex::v16, p10, p01, p11,
                        ratioX, ratioY)
        );
    }

    private static double bilinear(
            List<CableGeometry.Vertex> source,
            java.util.function.ToDoubleFunction<CableGeometry.Vertex> value,
            int p10,
            int p01,
            int p11,
            double ratioX,
            double ratioY
    ) {
        double bottom = value.applyAsDouble(source.getFirst()) * (1D - ratioX)
                + value.applyAsDouble(source.get(p10)) * ratioX;
        double top = value.applyAsDouble(source.get(p01)) * (1D - ratioX)
                + value.applyAsDouble(source.get(p11)) * ratioX;
        return bottom * (1D - ratioY) + top * ratioY;
    }

    private static int tangentAxisA(Direction6 face) {
        return switch (face) {
            case DOWN, UP, NORTH, SOUTH -> 0;
            case WEST, EAST -> 2;
        };
    }

    private static int tangentAxisB(Direction6 face) {
        return switch (face) {
            case DOWN, UP -> 2;
            case NORTH, SOUTH, WEST, EAST -> 1;
        };
    }

    private static double coordinate(CableGeometry.Vertex vertex, int axis) {
        return switch (axis) {
            case 0 -> vertex.x16();
            case 1 -> vertex.y16();
            case 2 -> vertex.z16();
            default -> throw new IllegalArgumentException("invalid axis");
        };
    }

    private static double coordinate(Position position, int axis) {
        return switch (axis) {
            case 0 -> position.x();
            case 1 -> position.y();
            case 2 -> position.z();
            default -> throw new IllegalArgumentException("invalid axis");
        };
    }

    /** Reinterpolates a clipped/kicked vertex against an original full-face UV quad. */
    public static UvPoint interpolateFullFaceUv(
            Direction6 face,
            CableGeometry.Vertex vertex,
            List<UvPoint> fullFaceUvs
    ) {
        Objects.requireNonNull(face, "face");
        Objects.requireNonNull(vertex, "vertex");
        Objects.requireNonNull(fullFaceUvs, "fullFaceUvs");
        if (fullFaceUvs.size() != 4) {
            throw new IllegalArgumentException("full face needs four UV points");
        }
        List<Position> full = nativePositions(
                face,
                new Box(0, 0, 0, 16, 16, 16)
        );
        Position origin = full.get(0);
        Position firstAxis = full.get(1);
        Position secondAxis = full.get(3);
        double firstLengthSquared = distanceSquared(origin, firstAxis);
        double secondLengthSquared = distanceSquared(origin, secondAxis);
        double first = dot(vertex, origin, firstAxis) / firstLengthSquared;
        double second = dot(vertex, origin, secondAxis) / secondLengthSquared;
        UvPoint originUv = fullFaceUvs.get(0);
        UvPoint firstUv = fullFaceUvs.get(1);
        UvPoint secondUv = fullFaceUvs.get(3);
        return new UvPoint(
                originUv.u16()
                        + first * (firstUv.u16() - originUv.u16())
                        + second * (secondUv.u16() - originUv.u16()),
                originUv.v16()
                        + first * (firstUv.v16() - originUv.v16())
                        + second * (secondUv.v16() - originUv.v16())
        );
    }

    private static double distanceSquared(Position first, Position second) {
        double x = second.x() - first.x();
        double y = second.y() - first.y();
        double z = second.z() - first.z();
        return x * x + y * y + z * z;
    }

    private static double dot(
            CableGeometry.Vertex vertex,
            Position origin,
            Position axisPoint
    ) {
        return (vertex.x16() - origin.x()) * (axisPoint.x() - origin.x())
                + (vertex.y16() - origin.y()) * (axisPoint.y() - origin.y())
                + (vertex.z16() - origin.z()) * (axisPoint.z() - origin.z());
    }

    private static Box insetTransparentBounds(
            Box source,
            Direction6 facadeDirection,
            int facadeMask
    ) {
        double minX = source.minX();
        double minY = source.minY();
        double minZ = source.minZ();
        double maxX = source.maxX();
        double maxY = source.maxY();
        double maxZ = source.maxZ();
        for (Direction6 edge : Direction6.values()) {
            if (sameAxis(edge, facadeDirection)
                    || (facadeMask & edge.maskBit()) == 0) {
                continue;
            }
            switch (edge) {
                case DOWN -> minY += THICKNESS_16;
                case UP -> maxY -= THICKNESS_16;
                case NORTH -> minZ += THICKNESS_16;
                case SOUTH -> maxZ -= THICKNESS_16;
                case WEST -> minX += THICKNESS_16;
                case EAST -> maxX -= THICKNESS_16;
            }
        }
        return new Box(minX, minY, minZ, maxX, maxY, maxZ);
    }

    private static List<Box> ringAroundHole(
            Box box,
            Direction6 direction,
            double holeMin,
            double holeMax
    ) {
        Box hole = switch (direction) {
            case DOWN, UP -> new Box(
                    holeMin, 0, holeMin, holeMax, 16, holeMax
            );
            case NORTH, SOUTH -> new Box(
                    holeMin, holeMin, 0, holeMax, holeMax, 16
            );
            case WEST, EAST -> new Box(
                    0, holeMin, holeMin, 16, holeMax, holeMax
            );
        };
        return ringAroundHole(box, direction, hole);
    }

    private static List<Box> ringAroundHole(
            Box box,
            Direction6 direction,
            Box hole
    ) {
        return switch (direction) {
            case DOWN, UP -> List.of(
                    new Box(
                            box.minX(), box.minY(), box.minZ(),
                            hole.minX(), box.maxY(), box.maxZ()
                    ),
                    new Box(
                            hole.maxX(), box.minY(), box.minZ(),
                            box.maxX(), box.maxY(), box.maxZ()
                    ),
                    new Box(
                            hole.minX(), box.minY(), box.minZ(),
                            hole.maxX(), box.maxY(), hole.minZ()
                    ),
                    new Box(
                            hole.minX(), box.minY(), hole.maxZ(),
                            hole.maxX(), box.maxY(), box.maxZ()
                    )
            );
            case NORTH, SOUTH -> List.of(
                    new Box(
                            box.minX(), box.minY(), box.minZ(),
                            box.maxX(), hole.minY(), box.maxZ()
                    ),
                    new Box(
                            box.minX(), hole.maxY(), box.minZ(),
                            box.maxX(), box.maxY(), box.maxZ()
                    ),
                    new Box(
                            box.minX(), hole.minY(), box.minZ(),
                            hole.minX(), hole.maxY(), box.maxZ()
                    ),
                    new Box(
                            hole.maxX(), hole.minY(), box.minZ(),
                            box.maxX(), hole.maxY(), box.maxZ()
                    )
            );
            case WEST, EAST -> List.of(
                    new Box(
                            box.minX(), box.minY(), box.minZ(),
                            box.maxX(), hole.minY(), box.maxZ()
                    ),
                    new Box(
                            box.minX(), hole.maxY(), box.minZ(),
                            box.maxX(), box.maxY(), box.maxZ()
                    ),
                    new Box(
                            box.minX(), hole.minY(), box.minZ(),
                            box.maxX(), hole.maxY(), hole.minZ()
                    ),
                    new Box(
                            box.minX(), hole.minY(), hole.maxZ(),
                            box.maxX(), hole.maxY(), box.maxZ()
                    )
            );
        };
    }

    private static void addNativeBox(
            List<Quad> output,
            Box box,
            Direction6 facadeDirection,
            int facadeMask,
            Box fullBounds
    ) {
        List<UvPoint> defaultUvs = List.of(
                new UvPoint(0, 16),
                new UvPoint(16, 16),
                new UvPoint(16, 0),
                new UvPoint(0, 0)
        );
        for (Direction6 face : Direction6.values()) {
            List<Position> positions = nativePositions(face, box);
            if (shouldStrip(
                    face,
                    positions,
                    facadeDirection,
                    facadeMask,
                    fullBounds
            )) {
                continue;
            }
            List<Position> kicked = new ArrayList<>(positions.size());
            for (Position position : positions) {
                kicked.add(kickCorner(
                        position,
                        face,
                        facadeDirection,
                        facadeMask,
                        fullBounds
                ));
            }
            List<CableGeometry.Vertex> vertices = new ArrayList<>(4);
            for (Position position : kicked) {
                CableGeometry.Vertex withoutUv = new CableGeometry.Vertex(
                        position.x(), position.y(), position.z(), 0, 0
                );
                UvPoint uv = interpolateFullFaceUv(face, withoutUv, defaultUvs);
                vertices.add(new CableGeometry.Vertex(
                        position.x(), position.y(), position.z(), uv.u16(), uv.v16()
                ));
            }
            output.add(new Quad(face, vertices));
        }
    }

    private static boolean shouldStrip(
            Direction6 face,
            List<Position> positions,
            Direction6 facadeDirection,
            int facadeMask,
            Box fullBounds
    ) {
        if (sameAxis(face, facadeDirection)
                || (facadeMask & face.maskBit()) == 0) {
            return false;
        }
        double boundary = boundary(fullBounds, face);
        return positions.stream().allMatch(position ->
                Double.compare(coordinate(position, face), boundary) == 0);
    }

    private static Position kickCorner(
            Position source,
            Direction6 nominalFace,
            Direction6 facadeDirection,
            int facadeMask,
            Box fullBounds
    ) {
        if (sameAxis(nominalFace, facadeDirection)) {
            return source;
        }
        double x = source.x();
        double y = source.y();
        double z = source.z();
        for (Direction6 edge : Direction6.values()) {
            if (sameAxis(edge, facadeDirection)
                    || sameAxis(edge, nominalFace)
                    || (facadeMask & edge.maskBit()) == 0) {
                continue;
            }
            if (atBoundary(x, y, z, fullBounds, facadeDirection.opposite())
                    && atBoundary(x, y, z, fullBounds, nominalFace)
                    && atBoundary(x, y, z, fullBounds, edge)) {
                x -= edge.stepX() * THICKNESS_16;
                y -= edge.stepY() * THICKNESS_16;
                z -= edge.stepZ() * THICKNESS_16;
            }
        }
        return new Position(x, y, z);
    }

    private static boolean atBoundary(
            double x,
            double y,
            double z,
            Box bounds,
            Direction6 direction
    ) {
        double coordinate = direction.stepX() != 0 ? x
                : direction.stepY() != 0 ? y : z;
        return Math.abs(coordinate - boundary(bounds, direction))
                < CORNER_KICK_EPSILON;
    }

    private static double boundary(Box box, Direction6 direction) {
        return switch (direction) {
            case DOWN -> box.minY();
            case UP -> box.maxY();
            case NORTH -> box.minZ();
            case SOUTH -> box.maxZ();
            case WEST -> box.minX();
            case EAST -> box.maxX();
        };
    }

    private static double coordinate(Position position, Direction6 direction) {
        return direction.stepX() != 0 ? position.x()
                : direction.stepY() != 0 ? position.y() : position.z();
    }

    private static boolean sameAxis(Direction6 first, Direction6 second) {
        return first.stepX() != 0 && second.stepX() != 0
                || first.stepY() != 0 && second.stepY() != 0
                || first.stepZ() != 0 && second.stepZ() != 0;
    }

    private static List<Position> nativePositions(Direction6 face, Box box) {
        return switch (face) {
            case DOWN -> List.of(
                    new Position(box.minX(), box.minY(), box.minZ()),
                    new Position(box.maxX(), box.minY(), box.minZ()),
                    new Position(box.maxX(), box.minY(), box.maxZ()),
                    new Position(box.minX(), box.minY(), box.maxZ())
            );
            case UP -> List.of(
                    new Position(box.minX(), box.maxY(), box.maxZ()),
                    new Position(box.maxX(), box.maxY(), box.maxZ()),
                    new Position(box.maxX(), box.maxY(), box.minZ()),
                    new Position(box.minX(), box.maxY(), box.minZ())
            );
            case NORTH -> List.of(
                    new Position(box.maxX(), box.minY(), box.minZ()),
                    new Position(box.minX(), box.minY(), box.minZ()),
                    new Position(box.minX(), box.maxY(), box.minZ()),
                    new Position(box.maxX(), box.maxY(), box.minZ())
            );
            case SOUTH -> List.of(
                    new Position(box.minX(), box.minY(), box.maxZ()),
                    new Position(box.maxX(), box.minY(), box.maxZ()),
                    new Position(box.maxX(), box.maxY(), box.maxZ()),
                    new Position(box.minX(), box.maxY(), box.maxZ())
            );
            case WEST -> List.of(
                    new Position(box.minX(), box.minY(), box.minZ()),
                    new Position(box.minX(), box.minY(), box.maxZ()),
                    new Position(box.minX(), box.maxY(), box.maxZ()),
                    new Position(box.minX(), box.maxY(), box.minZ())
            );
            case EAST -> List.of(
                    new Position(box.maxX(), box.minY(), box.maxZ()),
                    new Position(box.maxX(), box.minY(), box.minZ()),
                    new Position(box.maxX(), box.maxY(), box.minZ()),
                    new Position(box.maxX(), box.maxY(), box.maxZ())
            );
        };
    }

    private static List<List<Quad>> buildRings() {
        List<List<Quad>> rings = new ArrayList<>(Direction6.values().length);
        for (Direction6 direction : Direction6.values()) {
            rings.add(buildRing(direction));
        }
        return List.copyOf(rings);
    }

    private static List<Quad> buildRing(Direction6 direction) {
        List<Box> boxes = switch (direction) {
            case DOWN -> yRing(0, THICKNESS_16);
            case UP -> yRing(16 - THICKNESS_16, 16);
            case NORTH -> zRing(0, THICKNESS_16);
            case SOUTH -> zRing(16 - THICKNESS_16, 16);
            case WEST -> xRing(0, THICKNESS_16);
            case EAST -> xRing(16 - THICKNESS_16, 16);
        };
        List<Quad> quads = new ArrayList<>(24);
        for (Box box : boxes) {
            addBox(quads, box);
        }
        return List.copyOf(quads);
    }

    private static List<Box> yRing(double minY, double maxY) {
        return yRing(minY, maxY, HOLE_MIN_16, HOLE_MAX_16);
    }

    private static List<Box> yRing(
            double minY,
            double maxY,
            double holeMin,
            double holeMax
    ) {
        return List.of(
                new Box(0, minY, 0, holeMin, maxY, 16),
                new Box(holeMax, minY, 0, 16, maxY, 16),
                new Box(holeMin, minY, 0, holeMax, maxY, holeMin),
                new Box(holeMin, minY, holeMax, holeMax, maxY, 16)
        );
    }

    private static List<Box> zRing(double minZ, double maxZ) {
        return zRing(minZ, maxZ, HOLE_MIN_16, HOLE_MAX_16);
    }

    private static List<Box> zRing(
            double minZ,
            double maxZ,
            double holeMin,
            double holeMax
    ) {
        return List.of(
                new Box(0, 0, minZ, 16, holeMin, maxZ),
                new Box(0, holeMax, minZ, 16, 16, maxZ),
                new Box(0, holeMin, minZ, holeMin, holeMax, maxZ),
                new Box(holeMax, holeMin, minZ, 16, holeMax, maxZ)
        );
    }

    private static List<Box> xRing(double minX, double maxX) {
        return xRing(minX, maxX, HOLE_MIN_16, HOLE_MAX_16);
    }

    private static List<Box> xRing(
            double minX,
            double maxX,
            double holeMin,
            double holeMax
    ) {
        return List.of(
                new Box(minX, 0, 0, maxX, holeMin, 16),
                new Box(minX, holeMax, 0, maxX, 16, 16),
                new Box(minX, holeMin, 0, maxX, holeMax, holeMin),
                new Box(minX, holeMin, holeMax, maxX, holeMax, 16)
        );
    }

    private static Box slab(Direction6 direction) {
        return switch (direction) {
            case DOWN -> new Box(0, 0, 0, 16, THICKNESS_16, 16);
            case UP -> new Box(0, 16 - THICKNESS_16, 0, 16, 16, 16);
            case NORTH -> new Box(0, 0, 0, 16, 16, THICKNESS_16);
            case SOUTH -> new Box(0, 0, 16 - THICKNESS_16, 16, 16, 16);
            case WEST -> new Box(0, 0, 0, THICKNESS_16, 16, 16);
            case EAST -> new Box(16 - THICKNESS_16, 0, 0, 16, 16, 16);
        };
    }

    private static void addBox(List<Quad> output, Box box) {
        addBox(output, box, null, 0);
    }

    private static void addBox(
            List<Quad> output,
            Box box,
            Direction6 facadeDirection,
            int facadeMask
    ) {
        for (Direction6 face : Direction6.values()) {
            if (facadeDirection != null && face != facadeDirection
                    && face != facadeDirection.opposite()
                    && (facadeMask & face.maskBit()) != 0) {
                continue;
            }
            List<Position> positions = positions(face, box);
            UvRect uv = standardUv(face, box);
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
            output.add(new Quad(face, vertices));
        }
    }

    private static UvRect standardUv(Direction6 face, Box box) {
        double v1;
        double v2;
        if (face == Direction6.DOWN || face == Direction6.UP) {
            v1 = box.minZ();
            v2 = box.maxZ();
        } else {
            v1 = 16 - box.minY();
            v2 = 16 - box.maxY();
        }
        return switch (face) {
            case DOWN, UP, SOUTH -> new UvRect(box.minX(), v1, box.maxX(), v2);
            case NORTH -> new UvRect(16 - box.maxX(), v1, 16 - box.minX(), v2);
            case WEST -> new UvRect(box.minZ(), v1, box.maxZ(), v2);
            case EAST -> new UvRect(16 - box.maxZ(), v1, 16 - box.minZ(), v2);
        };
    }

    private static UvPoint[] uvPoints(Direction6 face, UvRect uv) {
        return switch (face) {
            case DOWN, UP -> new UvPoint[]{
                    new UvPoint(uv.u1(), uv.v1()),
                    new UvPoint(uv.u1(), uv.v2()),
                    new UvPoint(uv.u2(), uv.v2()),
                    new UvPoint(uv.u2(), uv.v1())
            };
            case NORTH, SOUTH, WEST, EAST -> new UvPoint[]{
                    new UvPoint(uv.u1(), uv.v2()),
                    new UvPoint(uv.u1(), uv.v1()),
                    new UvPoint(uv.u2(), uv.v1()),
                    new UvPoint(uv.u2(), uv.v2())
            };
        };
    }

    private static List<Position> positions(Direction6 face, Box box) {
        return switch (face) {
            case DOWN -> List.of(
                    new Position(box.minX(), box.minY(), box.maxZ()),
                    new Position(box.minX(), box.minY(), box.minZ()),
                    new Position(box.maxX(), box.minY(), box.minZ()),
                    new Position(box.maxX(), box.minY(), box.maxZ())
            );
            case UP -> List.of(
                    new Position(box.minX(), box.maxY(), box.minZ()),
                    new Position(box.minX(), box.maxY(), box.maxZ()),
                    new Position(box.maxX(), box.maxY(), box.maxZ()),
                    new Position(box.maxX(), box.maxY(), box.minZ())
            );
            case NORTH -> List.of(
                    new Position(box.maxX(), box.minY(), box.minZ()),
                    new Position(box.minX(), box.minY(), box.minZ()),
                    new Position(box.minX(), box.maxY(), box.minZ()),
                    new Position(box.maxX(), box.maxY(), box.minZ())
            );
            case SOUTH -> List.of(
                    new Position(box.minX(), box.minY(), box.maxZ()),
                    new Position(box.maxX(), box.minY(), box.maxZ()),
                    new Position(box.maxX(), box.maxY(), box.maxZ()),
                    new Position(box.minX(), box.maxY(), box.maxZ())
            );
            case WEST -> List.of(
                    new Position(box.minX(), box.minY(), box.minZ()),
                    new Position(box.minX(), box.minY(), box.maxZ()),
                    new Position(box.minX(), box.maxY(), box.maxZ()),
                    new Position(box.minX(), box.maxY(), box.minZ())
            );
            case EAST -> List.of(
                    new Position(box.maxX(), box.minY(), box.maxZ()),
                    new Position(box.maxX(), box.minY(), box.minZ()),
                    new Position(box.maxX(), box.maxY(), box.minZ()),
                    new Position(box.maxX(), box.maxY(), box.maxZ())
            );
        };
    }

    public record Quad(Direction6 face, List<CableGeometry.Vertex> vertices) {
        public Quad {
            Objects.requireNonNull(face, "face");
            vertices = List.copyOf(Objects.requireNonNull(vertices, "vertices"));
            if (vertices.size() != 4) {
                throw new IllegalArgumentException("facade quad must have four vertices");
            }
        }
    }

    private record Box(
            double minX,
            double minY,
            double minZ,
            double maxX,
            double maxY,
            double maxZ
    ) {
        private Box {
            double normalizedMinX = Math.min(minX, maxX);
            double normalizedMinY = Math.min(minY, maxY);
            double normalizedMinZ = Math.min(minZ, maxZ);
            double normalizedMaxX = Math.max(minX, maxX);
            double normalizedMaxY = Math.max(minY, maxY);
            double normalizedMaxZ = Math.max(minZ, maxZ);
            minX = normalizedMinX;
            minY = normalizedMinY;
            minZ = normalizedMinZ;
            maxX = normalizedMaxX;
            maxY = normalizedMaxY;
            maxZ = normalizedMaxZ;
        }
    }

    private record Position(double x, double y, double z) {
    }

    private record UvRect(double u1, double v1, double u2, double v2) {
    }

    public record UvPoint(double u16, double v16) {
        public UvPoint {
            if (!Double.isFinite(u16) || !Double.isFinite(v16)) {
                throw new IllegalArgumentException("facade UV must be finite");
            }
        }

        private double u() {
            return u16;
        }

        private double v() {
            return v16;
        }
    }

    /** Axis-aligned block-local bounds in sixteenths. */
    public record Bounds(
            double minX,
            double minY,
            double minZ,
            double maxX,
            double maxY,
            double maxZ
    ) {
        public Bounds {
            if (!Double.isFinite(minX) || !Double.isFinite(minY)
                    || !Double.isFinite(minZ) || !Double.isFinite(maxX)
                    || !Double.isFinite(maxY) || !Double.isFinite(maxZ)
                    || minX > maxX || minY > maxY || minZ > maxZ) {
                throw new IllegalArgumentException("invalid facade bounds");
            }
        }

        public boolean intersects(Bounds other) {
            Objects.requireNonNull(other, "other");
            return maxX > other.minX && minX < other.maxX
                    && maxY > other.minY && minY < other.maxY
                    && maxZ > other.minZ && minZ < other.maxZ;
        }

        public Bounds union(Bounds other) {
            Objects.requireNonNull(other, "other");
            return new Bounds(
                    Math.min(minX, other.minX),
                    Math.min(minY, other.minY),
                    Math.min(minZ, other.minZ),
                    Math.max(maxX, other.maxX),
                    Math.max(maxY, other.maxY),
                    Math.max(maxZ, other.maxZ)
            );
        }

        private static Bounds from(Box box) {
            return new Bounds(
                    box.minX(), box.minY(), box.minZ(),
                    box.maxX(), box.maxY(), box.maxZ()
            );
        }

        private Box box() {
            return new Box(minX, minY, minZ, maxX, maxY, maxZ);
        }
    }
}
