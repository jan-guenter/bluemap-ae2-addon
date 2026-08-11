/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.model.extendedae;

import io.github.janguenter.bluemap.ae2.model.Direction6;
import io.github.janguenter.bluemap.ae2.profile.extendedae.ExtendedAe2235Catalog;
import io.github.janguenter.bluemap.ae2.profile.extendedae.ExtendedAe2235Catalog.PlaneDefinition;

import java.util.List;
import java.util.Objects;

/** Canonical native plane plate geometry reused by both exact ExtendedAE parts. */
public final class ExtendedAePlaneGeometry {

    private ExtendedAePlaneGeometry() {
    }

    public static Geometry forSnapshot(ExtendedAePlaneSnapshot rawSnapshot) {
        ExtendedAePlaneSnapshot snapshot = Objects.requireNonNull(
                rawSnapshot,
                "rawSnapshot"
        ).staticProjection();
        PlaneDefinition definition = ExtendedAe2235Catalog.requirePlaneDefinition(
                snapshot.partId()
        );
        Bounds plate = plateBounds(snapshot.connectionMask());
        return new Geometry(
                snapshot.installedFace(),
                snapshot.connectionMask(),
                plate,
                definition.staticFrontTexture(),
                definition.sideTexture(),
                definition.backTexture(),
                definition.cableConnectionLength(),
                List.of(
                        new Surface(Direction6.NORTH, definition.staticFrontTexture()),
                        new Surface(Direction6.SOUTH, definition.backTexture()),
                        new Surface(Direction6.DOWN, definition.sideTexture()),
                        new Surface(Direction6.UP, definition.sideTexture()),
                        new Surface(Direction6.WEST, definition.sideTexture()),
                        new Surface(Direction6.EAST, definition.sideTexture())
                )
        );
    }

    /**
     * PlaneBakedModel-local plate in sixteenths. Mask bits are logical front-view
     * left=1, down=2, right=4 and up=8.
     */
    public static Bounds plateBounds(int mask) {
        if (mask < 0 || mask >= ExtendedAe2235Catalog.PLANE_CONNECTION_MASK_COUNT) {
            throw new IllegalArgumentException("plane connection mask must be in [0, 15]");
        }
        double minX = connected(mask, PlaneNeighbor.RIGHT) ? 0 : 1;
        double maxX = connected(mask, PlaneNeighbor.LEFT) ? 16 : 15;
        double minY = connected(mask, PlaneNeighbor.DOWN) ? 0 : 1;
        double maxY = connected(mask, PlaneNeighbor.UP) ? 16 : 15;
        return new Bounds(minX, minY, 0, maxX, maxY, 1);
    }

    public static boolean connected(int mask, PlaneNeighbor neighbor) {
        Objects.requireNonNull(neighbor, "neighbor");
        if (mask < 0 || mask >= ExtendedAe2235Catalog.PLANE_CONNECTION_MASK_COUNT) {
            throw new IllegalArgumentException("plane connection mask must be in [0, 15]");
        }
        return (mask & neighbor.maskBit()) != 0;
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

    /** Canonical model bounds before the installed-face transformation. */
    public record Bounds(
            double minX16,
            double minY16,
            double minZ16,
            double maxX16,
            double maxY16,
            double maxZ16
    ) {

        public Bounds {
            if (minX16 < 0 || minY16 < 0 || minZ16 < 0
                    || maxX16 > 16 || maxY16 > 16 || maxZ16 > 16
                    || minX16 >= maxX16 || minY16 >= maxY16 || minZ16 >= maxZ16) {
                throw new IllegalArgumentException("invalid plane bounds");
            }
        }
    }

    public record Surface(Direction6 canonicalFace, String texture) {

        public Surface {
            Objects.requireNonNull(canonicalFace, "canonicalFace");
            Objects.requireNonNull(texture, "texture");
        }
    }

    public record Geometry(
            Direction6 installedFace,
            int connectionMask,
            Bounds canonicalPlate,
            String frontTexture,
            String sideTexture,
            String backTexture,
            int cableConnectionLength,
            List<Surface> surfaces
    ) {

        public Geometry {
            Objects.requireNonNull(installedFace, "installedFace");
            Objects.requireNonNull(canonicalPlate, "canonicalPlate");
            Objects.requireNonNull(frontTexture, "frontTexture");
            Objects.requireNonNull(sideTexture, "sideTexture");
            Objects.requireNonNull(backTexture, "backTexture");
            surfaces = List.copyOf(surfaces);
            if (connectionMask < 0
                    || connectionMask >= ExtendedAe2235Catalog.PLANE_CONNECTION_MASK_COUNT
                    || cableConnectionLength != ExtendedAe2235Catalog.PLANE_CABLE_CONNECTION_LENGTH
                    || surfaces.size() != Direction6.values().length) {
                throw new IllegalArgumentException("invalid ExtendedAE plane geometry");
            }
        }
    }
}
