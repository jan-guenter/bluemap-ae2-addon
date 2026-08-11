/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.model;

import io.github.janguenter.bluemap.ae2.profile.Ae219217NativeStructuralProfile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Exact AE2 19.2.17 face-part boxes used by FacadeBuilder's cutout union. */
public final class NativePartCollisionGeometry {

    private NativePartCollisionGeometry() {
    }

    public static FacadeGeometry.Bounds cutout(
            NativeStructuralSnapshot snapshot,
            Direction6 facadeDirection,
            int facadeMask,
            boolean transparent
    ) {
        Objects.requireNonNull(snapshot, "snapshot");
        Objects.requireNonNull(facadeDirection, "facadeDirection");
        FacadeGeometry.Bounds facadeBounds = FacadeGeometry.facadeBounds(
                facadeDirection,
                facadeMask,
                transparent
        );
        FacadeGeometry.Bounds union = null;
        for (Map.Entry<Direction6, FacePartSnapshot> entry
                : snapshot.faceParts().entrySet()) {
            int planeMask = snapshot.planeConnectionMasks().getOrDefault(
                    entry.getKey(),
                    0
            );
            for (FacadeGeometry.Bounds bounds : boxes(
                    entry.getKey(),
                    entry.getValue().id(),
                    planeMask,
                    snapshot.hasFacade(entry.getKey())
            )) {
                if (!bounds.intersects(facadeBounds)) {
                    continue;
                }
                union = union == null ? bounds : union.union(bounds);
            }
        }
        return union;
    }

    public static List<FacadeGeometry.Bounds> boxes(
            Direction6 installedFace,
            String partId,
            int planeMask,
            boolean sameFaceFacade
    ) {
        Objects.requireNonNull(installedFace, "installedFace");
        NativeStructuralPartCatalog.Definition definition =
                NativeStructuralPartCatalog.require(partId);
        if (planeMask < 0 || planeMask > 15) {
            throw new IllegalArgumentException("plane mask must be in [0, 15]");
        }
        List<Ae219217NativeStructuralProfile.PartBox> profileBoxes = switch (
                definition.kind()
        ) {
            case ANCHOR -> Ae219217NativeStructuralProfile
                    .cableAnchorCollisionBoxes(sameFaceFacade);
            case PLANE -> Ae219217NativeStructuralProfile.planeCollisionBoxes(
                    installedFaceName(installedFace),
                    planeMask
            );
            case STATIC, REPORTING, P2P, CELL_DOCK -> definition.isExtension()
                    ? extensionCollisionBoxes(definition)
                    : Ae219217NativeStructuralProfile.facePartCollisionBoxes(partId);
        };
        if (profileBoxes == null || profileBoxes.isEmpty()) {
            throw new IllegalArgumentException(
                    "missing native part collision boxes for " + partId
            );
        }
        List<Box> canonical = profileBoxes.stream()
                .map(box -> new Box(
                        box.minX(), box.minY(), box.minZ(),
                        box.maxX(), box.maxY(), box.maxZ()
                ))
                .toList();
        return canonical.stream()
                .map(box -> orient(installedFace, box))
                .toList();
    }

    private static List<Ae219217NativeStructuralProfile.PartBox>
            extensionCollisionBoxes(
                    NativeStructuralPartCatalog.Definition definition
            ) {
        return List.of(new Ae219217NativeStructuralProfile.PartBox(
                integralSixteenth(definition.facadeCutoutMin16()),
                integralSixteenth(definition.facadeCutoutMin16()),
                16 - definition.cableConnectionLength(),
                integralSixteenth(definition.facadeCutoutMax16()),
                integralSixteenth(definition.facadeCutoutMax16()),
                16
        ));
    }

    private static int integralSixteenth(double value) {
        int result = (int) value;
        if (result != value) {
            throw new IllegalArgumentException(
                    "extension collision bound is not an integral sixteenth"
            );
        }
        return result;
    }

    private static String installedFaceName(Direction6 installedFace) {
        return switch (installedFace) {
            case DOWN -> "down";
            case UP -> "up";
            case NORTH -> "north";
            case SOUTH -> "south";
            case WEST -> "west";
            case EAST -> "east";
        };
    }

    private static FacadeGeometry.Bounds orient(Direction6 installedFace, Box box) {
        List<Position> corners = new ArrayList<>(8);
        for (double x : List.of(box.minX(), box.maxX())) {
            for (double y : List.of(box.minY(), box.maxY())) {
                for (double z : List.of(box.minZ(), box.maxZ())) {
                    corners.add(transform(installedFace, x, y, z));
                }
            }
        }
        return new FacadeGeometry.Bounds(
                corners.stream().mapToDouble(Position::x).min().orElseThrow(),
                corners.stream().mapToDouble(Position::y).min().orElseThrow(),
                corners.stream().mapToDouble(Position::z).min().orElseThrow(),
                corners.stream().mapToDouble(Position::x).max().orElseThrow(),
                corners.stream().mapToDouble(Position::y).max().orElseThrow(),
                corners.stream().mapToDouble(Position::z).max().orElseThrow()
        );
    }

    private static Position transform(
            Direction6 installedFace,
            double localX,
            double localY,
            double localZ
    ) {
        CollisionBasis basis = collisionBasis(installedFace);
        return new Position(
                8 + (localX - 8) * basis.x().stepX()
                        + (localY - 8) * basis.y().stepX()
                        + (localZ - 8) * installedFace.stepX(),
                8 + (localX - 8) * basis.x().stepY()
                        + (localY - 8) * basis.y().stepY()
                        + (localZ - 8) * installedFace.stepY(),
                8 + (localX - 8) * basis.x().stepZ()
                        + (localY - 8) * basis.y().stepZ()
                        + (localZ - 8) * installedFace.stepZ()
        );
    }

    private static CollisionBasis collisionBasis(Direction6 installedFace) {
        return switch (installedFace) {
            case DOWN -> new CollisionBasis(Direction6.EAST, Direction6.NORTH);
            case UP -> new CollisionBasis(Direction6.EAST, Direction6.SOUTH);
            case EAST -> new CollisionBasis(Direction6.SOUTH, Direction6.UP);
            case WEST -> new CollisionBasis(Direction6.NORTH, Direction6.UP);
            case NORTH -> new CollisionBasis(Direction6.WEST, Direction6.UP);
            case SOUTH -> new CollisionBasis(Direction6.EAST, Direction6.UP);
        };
    }

    private record Box(
            double minX,
            double minY,
            double minZ,
            double maxX,
            double maxY,
            double maxZ
    ) {
    }

    private record Position(double x, double y, double z) {
    }

    private record CollisionBasis(Direction6 x, Direction6 y) {
    }
}
