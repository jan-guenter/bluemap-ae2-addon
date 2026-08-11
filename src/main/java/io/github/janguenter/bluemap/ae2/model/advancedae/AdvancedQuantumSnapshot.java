/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.model.advancedae;

import io.github.janguenter.bluemap.ae2.profile.advancedae.AdvancedAe1612Catalog;
import io.github.janguenter.bluemap.ae2.profile.advancedae.AdvancedAe1612Catalog.QuantumKind;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/** Immutable persisted state and exact 3x3x3 appearance neighborhood for one quantum block. */
public record AdvancedQuantumSnapshot(
        QuantumKind kind,
        boolean formed,
        boolean powered,
        boolean multiblocked,
        int lightLevel,
        int blockX,
        int blockY,
        int blockZ,
        Map<Offset, QuantumKind> neighbors
) {

    private static final Set<Offset> SCAN_OFFSETS = buildScanOffsets();

    public AdvancedQuantumSnapshot {
        Objects.requireNonNull(kind, "kind");
        Objects.requireNonNull(neighbors, "neighbors");
        if (lightLevel < 0 || lightLevel > 15) {
            throw new IllegalArgumentException("lightLevel must be in [0, 15]");
        }
        Map<Offset, QuantumKind> copy = new LinkedHashMap<>();
        neighbors.forEach((offset, neighborKind) -> {
            Objects.requireNonNull(offset, "neighbor offset");
            Objects.requireNonNull(neighborKind, "neighbor kind");
            if (copy.put(offset, neighborKind) != null) {
                throw new IllegalArgumentException("duplicate quantum neighbor offset");
            }
        });
        neighbors = Map.copyOf(copy);
    }

    /** Exact static BlueMap projection: no active animation, light, or emissive state. */
    public AdvancedQuantumSnapshot staticProjection() {
        if (!powered && lightLevel == 0) {
            return this;
        }
        return new AdvancedQuantumSnapshot(
                kind,
                formed,
                false,
                multiblocked,
                0,
                blockX,
                blockY,
                blockZ,
                neighbors
        );
    }

    /**
     * Classifies raw appearance block IDs from a complete host scan. Missing, null and
     * non-catalog observations remain disconnected rather than widening the topology.
     */
    public static AdvancedQuantumSnapshot observed(
            QuantumKind kind,
            boolean formed,
            boolean powered,
            boolean multiblocked,
            int lightLevel,
            int blockX,
            int blockY,
            int blockZ,
            Map<Offset, String> appearanceBlockIds
    ) {
        Objects.requireNonNull(appearanceBlockIds, "appearanceBlockIds");
        Map<Offset, QuantumKind> classified = new LinkedHashMap<>();
        for (Offset offset : SCAN_OFFSETS) {
            QuantumKind neighbor = AdvancedAe1612Catalog.quantumKindOrNull(
                    appearanceBlockIds.get(offset)
            );
            if (neighbor != null) {
                classified.put(offset, neighbor);
            }
        }
        return new AdvancedQuantumSnapshot(
                kind,
                formed,
                powered,
                multiblocked,
                lightLevel,
                blockX,
                blockY,
                blockZ,
                classified
        );
    }

    /** Explicit non-live visual values used after the static projection. */
    public StaticVisualState staticVisualState() {
        AdvancedQuantumSnapshot snapshot = staticProjection();
        return new StaticVisualState(
                snapshot.formed,
                snapshot.multiblocked,
                false,
                0,
                false,
                0
        );
    }

    public VisualMode visualMode() {
        if (kind.isStructure()) {
            return VisualMode.CONNECTED_STRUCTURE;
        }
        if (!formed) {
            return VisualMode.JSON_UNFORMED;
        }
        if (kind == QuantumKind.CORE && !multiblocked) {
            return VisualMode.JSON_CORE_STANDALONE;
        }
        return VisualMode.CONNECTED_INTERNAL;
    }

    /** Exact JSON selection for unformed/standalone states; empty means connected geometry. */
    public Optional<String> staticJsonModelResource() {
        return visualMode().usesConnectedGeometry()
                ? Optional.empty()
                : Optional.of(AdvancedAe1612Catalog.unformedModelResource(kind));
    }

    public boolean hasCompatibleNeighbor(Offset offset) {
        Objects.requireNonNull(offset, "offset");
        QuantumKind neighbor = neighbors.get(offset);
        return switch (visualMode()) {
            case CONNECTED_INTERNAL -> neighbor != null && !neighbor.isStructure();
            case CONNECTED_STRUCTURE -> neighbor == QuantumKind.STRUCTURE;
            case JSON_UNFORMED, JSON_CORE_STANDALONE -> false;
        };
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
        if (offsets.size() != 27 || !offsets.contains(new Offset(0, 0, 0))) {
            throw new IllegalStateException("invalid quantum appearance scan domain");
        }
        return Set.copyOf(offsets);
    }

    public enum VisualMode {
        JSON_UNFORMED,
        JSON_CORE_STANDALONE,
        CONNECTED_INTERNAL,
        CONNECTED_STRUCTURE;

        public boolean usesConnectedGeometry() {
            return this == CONNECTED_INTERNAL || this == CONNECTED_STRUCTURE;
        }
    }

    /** One cell in the exact client renderer's complete 3x3x3 scan. */
    public record Offset(int x, int y, int z) {

        public Offset {
            if (x < -1 || x > 1 || y < -1 || y > 1 || z < -1 || z > 1) {
                throw new IllegalArgumentException("quantum offset must be a 3x3x3 cell");
            }
        }

        public static Offset direct(io.github.janguenter.bluemap.ae2.model.Direction6 face) {
            Objects.requireNonNull(face, "face");
            return new Offset(face.stepX(), face.stepY(), face.stepZ());
        }
    }

    /** Deterministic projection metadata separate from persisted blockstate fields. */
    public record StaticVisualState(
            boolean formed,
            boolean multiblocked,
            boolean powered,
            int lightLevel,
            boolean emissive,
            int animationFrame
    ) {

        public StaticVisualState {
            if (powered || lightLevel != 0 || emissive || animationFrame != 0) {
                throw new IllegalArgumentException("Advanced AE static visual state must be off");
            }
        }
    }
}
