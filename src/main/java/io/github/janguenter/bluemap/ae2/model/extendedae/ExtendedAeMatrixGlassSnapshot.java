/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.model.extendedae;

import io.github.janguenter.bluemap.ae2.model.Direction6;
import io.github.janguenter.bluemap.ae2.profile.extendedae.ExtendedAe2235Catalog.MatrixKind;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/** Exact 3x3x3 appearance scan and static state for one Assembler Matrix Glass. */
public record ExtendedAeMatrixGlassSnapshot(
        ExtendedAeMatrixSnapshot state,
        int blockX,
        int blockY,
        int blockZ,
        Set<Offset> sameGlassAppearances
) {

    private static final Offset ORIGIN = new Offset(0, 0, 0);
    private static final Set<Offset> SCAN_OFFSETS = buildScanOffsets();

    public ExtendedAeMatrixGlassSnapshot {
        Objects.requireNonNull(state, "state");
        Objects.requireNonNull(sameGlassAppearances, "sameGlassAppearances");
        if (state.kind() != MatrixKind.GLASS) {
            throw new IllegalArgumentException("matrix glass snapshot requires the glass role");
        }
        Set<Offset> copy = Set.copyOf(sameGlassAppearances);
        if (!copy.contains(ORIGIN)) {
            throw new IllegalArgumentException("the exact appearance scan must contain its origin");
        }
        sameGlassAppearances = copy;
    }

    /** Builds a complete observation from the matching non-origin cells. */
    public static ExtendedAeMatrixGlassSnapshot observed(
            boolean formed,
            boolean powered,
            int blockX,
            int blockY,
            int blockZ,
            Set<Offset> matchingNeighbors
    ) {
        Objects.requireNonNull(matchingNeighbors, "matchingNeighbors");
        Set<Offset> appearances = new LinkedHashSet<>(matchingNeighbors);
        appearances.add(ORIGIN);
        return new ExtendedAeMatrixGlassSnapshot(
                ExtendedAeMatrixSnapshot.of(MatrixKind.GLASS, formed, powered),
                blockX,
                blockY,
                blockZ,
                appearances
        );
    }

    /**
     * Classifies source getAppearance results by exact matrix-glass block ID. Blockstate
     * properties are intentionally irrelevant; missing/null/unknown observations do not connect.
     */
    public static ExtendedAeMatrixGlassSnapshot fromAppearanceBlockIds(
            boolean formed,
            boolean powered,
            int blockX,
            int blockY,
            int blockZ,
            Map<Offset, String> appearanceBlockIds
    ) {
        Objects.requireNonNull(appearanceBlockIds, "appearanceBlockIds");
        Set<Offset> matching = new LinkedHashSet<>();
        for (Offset offset : SCAN_OFFSETS) {
            if ("extendedae:assembler_matrix_glass".equals(appearanceBlockIds.get(offset))) {
                matching.add(offset);
            }
        }
        return observed(formed, powered, blockX, blockY, blockZ, matching);
    }

    public ExtendedAeMatrixGlassSnapshot staticProjection() {
        ExtendedAeMatrixSnapshot projected = state.staticProjection();
        return projected == state
                ? this
                : new ExtendedAeMatrixGlassSnapshot(
                        projected,
                        blockX,
                        blockY,
                        blockZ,
                        sameGlassAppearances
                );
    }

    public boolean matches(Offset offset) {
        return sameGlassAppearances.contains(Objects.requireNonNull(offset, "offset"));
    }

    public boolean faceBlocked(Direction6 face) {
        return matches(Offset.direct(face));
    }

    public int faceTextureIndex() {
        return Math.abs((blockX ^ blockY ^ blockZ) % 3);
    }

    /** All 27 exact offsets visited by the source model-data scan, including the origin. */
    public static Set<Offset> scanOffsets() {
        return SCAN_OFFSETS;
    }

    private static Set<Offset> buildScanOffsets() {
        Set<Offset> offsets = new LinkedHashSet<>();
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    offsets.add(new Offset(x, y, z));
                }
            }
        }
        if (offsets.size() != 27 || !offsets.contains(ORIGIN)) {
            throw new IllegalStateException("invalid matrix-glass appearance scan domain");
        }
        return Collections.unmodifiableSet(offsets);
    }

    /** One cell in the source renderer's complete 3x3x3 appearance scan. */
    public record Offset(int x, int y, int z) {

        public Offset {
            if (x < -1 || x > 1 || y < -1 || y > 1 || z < -1 || z > 1) {
                throw new IllegalArgumentException("matrix-glass offset must be a 3x3x3 cell");
            }
        }

        public static Offset direct(Direction6 face) {
            Objects.requireNonNull(face, "face");
            return new Offset(face.stepX(), face.stepY(), face.stepZ());
        }
    }
}
