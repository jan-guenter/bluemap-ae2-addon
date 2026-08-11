/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.model.advancedae;

import io.github.janguenter.bluemap.ae2.model.Direction6;
import io.github.janguenter.bluemap.ae2.model.advancedae.AdvancedAeAthenaSnapshot.Offset;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Dependency-free port of Athena 4.0.6's five-texture connected-block projection. */
public final class AdvancedAeAthenaGeometry {

    private AdvancedAeAthenaGeometry() {
    }

    public static List<Quad> forSnapshot(AdvancedAeAthenaSnapshot snapshot) {
        Objects.requireNonNull(snapshot, "snapshot");
        List<Quad> output = new ArrayList<>(24);
        for (Direction6 face : Direction6.values()) {
            if (snapshot.faceBlocked(face)) {
                continue;
            }
            FaceBasis basis = faceBasis(face);
            boolean up = snapshot.matches(basis.up());
            boolean down = snapshot.matches(basis.down());
            boolean left = snapshot.matches(basis.left());
            boolean right = snapshot.matches(basis.right());
            boolean upLeft = snapshot.matches(basis.up().plus(basis.left()));
            boolean upRight = snapshot.matches(basis.up().plus(basis.right()));
            boolean downLeft = snapshot.matches(basis.down().plus(basis.left()));
            boolean downRight = snapshot.matches(basis.down().plus(basis.right()));
            if (up && down && left && right
                    && upLeft && upRight && downLeft && downRight) {
                output.add(new Quad(face, Corner.FULL, Texture.EMPTY, Region.FULL));
                continue;
            }
            output.add(new Quad(
                    face,
                    Corner.LEFT_UP,
                    texture(up, left, upLeft),
                    Region.LEFT_UP
            ));
            output.add(new Quad(
                    face,
                    Corner.RIGHT_UP,
                    texture(up, right, upRight),
                    Region.RIGHT_UP
            ));
            output.add(new Quad(
                    face,
                    Corner.LEFT_DOWN,
                    texture(down, left, downLeft),
                    Region.LEFT_DOWN
            ));
            output.add(new Quad(
                    face,
                    Corner.RIGHT_DOWN,
                    texture(down, right, downRight),
                    Region.RIGHT_DOWN
            ));
        }
        return List.copyOf(output);
    }

    public static Texture texture(boolean vertical, boolean horizontal, boolean diagonal) {
        if (vertical && horizontal) {
            return diagonal ? Texture.EMPTY : Texture.CENTER;
        }
        if (vertical) {
            return Texture.VERTICAL;
        }
        if (horizontal) {
            return Texture.HORIZONTAL;
        }
        return Texture.PARTICLE;
    }

    public static FaceBasis faceBasis(Direction6 face) {
        return switch (Objects.requireNonNull(face, "face")) {
            case UP -> basis(0, 0, -1, -1, 0, 0);
            case DOWN -> basis(0, 0, 1, -1, 0, 0);
            case NORTH -> basis(0, 1, 0, 1, 0, 0);
            case SOUTH -> basis(0, 1, 0, -1, 0, 0);
            case WEST -> basis(0, 1, 0, 0, 0, -1);
            case EAST -> basis(0, 1, 0, 0, 0, 1);
        };
    }

    private static FaceBasis basis(
            int upX,
            int upY,
            int upZ,
            int leftX,
            int leftY,
            int leftZ
    ) {
        Offset up = new Offset(upX, upY, upZ);
        Offset left = new Offset(leftX, leftY, leftZ);
        return new FaceBasis(
                up,
                new Offset(-upX, -upY, -upZ),
                left,
                new Offset(-leftX, -leftY, -leftZ)
        );
    }

    public enum Texture {
        PARTICLE("advanced_ae:block/quantum_alloy_block"),
        EMPTY("advanced_ae:block/quantum_alloy_block_empty"),
        CENTER("advanced_ae:block/quantum_alloy_block_center"),
        VERTICAL("advanced_ae:block/quantum_alloy_block_v"),
        HORIZONTAL("advanced_ae:block/quantum_alloy_block_h");

        private final String textureId;

        Texture(String textureId) {
            this.textureId = textureId;
        }

        public String textureId() {
            return textureId;
        }
    }

    public enum Corner {
        FULL,
        LEFT_UP,
        RIGHT_UP,
        LEFT_DOWN,
        RIGHT_DOWN
    }

    /** Exact Athena left/right/top/bottom face fractions. */
    public enum Region {
        FULL(0, 1, 1, 0),
        LEFT_UP(0, 0.5, 1, 0.5),
        RIGHT_UP(0.5, 1, 1, 0.5),
        LEFT_DOWN(0, 0.5, 0.5, 0),
        RIGHT_DOWN(0.5, 1, 0.5, 0);

        private final double left;
        private final double right;
        private final double top;
        private final double bottom;

        Region(double left, double right, double top, double bottom) {
            this.left = left;
            this.right = right;
            this.top = top;
            this.bottom = bottom;
        }

        public double left() {
            return left;
        }

        public double right() {
            return right;
        }

        public double top() {
            return top;
        }

        public double bottom() {
            return bottom;
        }
    }

    public record FaceBasis(Offset up, Offset down, Offset left, Offset right) {

        public FaceBasis {
            Objects.requireNonNull(up, "up");
            Objects.requireNonNull(down, "down");
            Objects.requireNonNull(left, "left");
            Objects.requireNonNull(right, "right");
        }
    }

    public record Quad(Direction6 face, Corner corner, Texture texture, Region region) {

        public Quad {
            Objects.requireNonNull(face, "face");
            Objects.requireNonNull(corner, "corner");
            Objects.requireNonNull(texture, "texture");
            Objects.requireNonNull(region, "region");
            if ((corner == Corner.FULL) ^ (region == Region.FULL)) {
                throw new IllegalArgumentException("Athena full-face corner and region must agree");
            }
        }
    }
}
