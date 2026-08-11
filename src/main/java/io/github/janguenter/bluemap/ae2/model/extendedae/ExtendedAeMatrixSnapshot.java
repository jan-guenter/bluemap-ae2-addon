/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.model.extendedae;

import io.github.janguenter.bluemap.ae2.profile.extendedae.ExtendedAe2235Catalog.MatrixKind;

import java.util.Objects;

/** Persisted matrix role and blockstate projected into the deterministic static view. */
public record ExtendedAeMatrixSnapshot(
        MatrixKind kind,
        boolean formed,
        boolean powered,
        FrameShape frameShape
) {

    public ExtendedAeMatrixSnapshot {
        Objects.requireNonNull(kind, "kind");
        Objects.requireNonNull(frameShape, "frameShape");
        if (kind != MatrixKind.FRAME && frameShape != FrameShape.BLOCK) {
            throw new IllegalArgumentException("only the matrix frame has a shape property");
        }
    }

    public static ExtendedAeMatrixSnapshot of(
            MatrixKind kind,
            boolean formed,
            boolean powered
    ) {
        return new ExtendedAeMatrixSnapshot(kind, formed, powered, FrameShape.BLOCK);
    }

    /** Live power is intentionally normalized off; structural formation and shape survive. */
    public ExtendedAeMatrixSnapshot staticProjection() {
        return powered
                ? new ExtendedAeMatrixSnapshot(kind, formed, false, frameShape)
                : this;
    }

    /** Exact off-model and JSON rotation selected by the static renderer. */
    public ModelSelection staticModelSelection() {
        ExtendedAeMatrixSnapshot snapshot = staticProjection();
        if (snapshot.kind != MatrixKind.FRAME) {
            String path = snapshot.kind == MatrixKind.GLASS
                    ? "assets/extendedae/models/block/assembler_matrix_glass.json"
                    : "assets/extendedae/models/block/assembler_matrix/"
                    + snapshot.kind.name().toLowerCase(java.util.Locale.ROOT) + ".json";
            return new ModelSelection(path, 0, 0);
        }
        return switch (snapshot.frameShape) {
            case BLOCK -> new ModelSelection(
                    "assets/extendedae/models/block/assembler_matrix/frame_block_off.json",
                    0,
                    0
            );
            case COLUMN_X -> new ModelSelection(
                    "assets/extendedae/models/block/assembler_matrix/frame_column_off.json",
                    90,
                    90
            );
            case COLUMN_Y -> new ModelSelection(
                    "assets/extendedae/models/block/assembler_matrix/frame_column_off.json",
                    0,
                    0
            );
            case COLUMN_Z -> new ModelSelection(
                    "assets/extendedae/models/block/assembler_matrix/frame_column_off.json",
                    90,
                    0
            );
        };
    }

    public enum FrameShape {
        BLOCK("block"),
        COLUMN_X("column_x"),
        COLUMN_Y("column_y"),
        COLUMN_Z("column_z");

        private final String serializedName;

        FrameShape(String serializedName) {
            this.serializedName = serializedName;
        }

        public String serializedName() {
            return serializedName;
        }

        public static FrameShape fromSerializedName(String value) {
            for (FrameShape shape : values()) {
                if (shape.serializedName.equals(value)) {
                    return shape;
                }
            }
            throw new IllegalArgumentException("unsupported matrix frame shape");
        }
    }

    public record ModelSelection(String modelResource, int xRotation, int yRotation) {

        public ModelSelection {
            if (!modelResource.startsWith("assets/extendedae/models/block/")
                    || xRotation % 90 != 0
                    || yRotation % 90 != 0) {
                throw new IllegalArgumentException("invalid ExtendedAE matrix model selection");
            }
        }
    }
}
