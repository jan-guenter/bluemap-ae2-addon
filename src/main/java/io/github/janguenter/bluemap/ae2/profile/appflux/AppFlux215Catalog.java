/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.profile.appflux;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Closed world-render catalog proven for AppliedFlux 1.21-2.1.5-neoforge. */
public final class AppFlux215Catalog {

    public static final String CHARGED_REDSTONE_BLOCK =
            "appflux:charged_redstone_block";
    public static final String FLUX_ACCESSOR_BLOCK = "appflux:flux_accessor";
    public static final String FLUX_ACCESSOR_PART = "appflux:part_flux_accessor";
    public static final String FLUX_ACCESSOR_PART_MODEL =
            "appflux:part/flux_accessor";
    public static final String FAST_NBT_KEY = "fast";
    public static final boolean FAST_AFFECTS_WORLD_GEOMETRY = false;
    public static final String PART_STATUS_POLICY = "static-neutral-no-status-layer";

    private static final List<String> TIERS = List.of(
            "1k", "4k", "16k", "64k", "256k",
            "1m", "4m", "16m", "64m", "256m"
    );
    private static final Map<String, String> STOCK_BLOCK_MODELS = Map.of(
            CHARGED_REDSTONE_BLOCK,
            "appflux:block/charged_redstone_block",
            FLUX_ACCESSOR_BLOCK,
            "appflux:block/flux_accessor"
    );
    private static final Map<String, String> DRIVE_CELL_MODELS = buildCellModels();
    private static final Set<String> NORMAL_CELL_IDS = buildCellIds(false);
    private static final Set<String> PORTABLE_CELL_IDS = buildCellIds(true);
    private static final PartDefinition PART = new PartDefinition(
            FLUX_ACCESSOR_PART,
            FLUX_ACCESSOR_PART_MODEL,
            false,
            PART_STATUS_POLICY,
            Set.of(FAST_NBT_KEY)
    );

    private AppFlux215Catalog() {
    }

    public static List<String> tiers() {
        return TIERS;
    }

    /** Blocks whose exact world appearance is already ordinary resource-pack JSON. */
    public static Map<String, String> stockBlockModels() {
        return STOCK_BLOCK_MODELS;
    }

    /** Exact normal and portable FE item IDs mapped to the ten registered Drive models. */
    public static Map<String, String> driveCellModels() {
        return DRIVE_CELL_MODELS;
    }

    public static Set<String> normalCellIds() {
        return NORMAL_CELL_IDS;
    }

    public static Set<String> portableCellIds() {
        return PORTABLE_CELL_IDS;
    }

    public static PartDefinition fluxAccessorPart() {
        return PART;
    }

    private static Map<String, String> buildCellModels() {
        Map<String, String> result = new LinkedHashMap<>();
        for (String tier : TIERS) {
            String model = "appflux:block/drive/fe_" + tier + "_cell";
            result.put("appflux:fe_" + tier + "_cell", model);
            result.put("appflux:fe_" + tier + "_portable_cell", model);
        }
        if (result.size() != 20 || new LinkedHashSet<>(result.values()).size() != 10) {
            throw new IllegalStateException("invalid exact AppliedFlux Drive-cell catalog");
        }
        return Collections.unmodifiableMap(result);
    }

    private static Set<String> buildCellIds(boolean portable) {
        Set<String> result = new LinkedHashSet<>();
        for (String tier : TIERS) {
            String suffix = portable ? "_portable_cell" : "_cell";
            result.add("appflux:fe_" + tier + suffix);
        }
        if (result.size() != 10 || !DRIVE_CELL_MODELS.keySet().containsAll(result)) {
            throw new IllegalStateException("invalid exact AppliedFlux cell family");
        }
        return Collections.unmodifiableSet(result);
    }

    /** Exact generic face-part registration and deliberately ignored visual state. */
    public record PartDefinition(
            String itemId,
            String modelId,
            boolean supportsSpin,
            String statusPolicy,
            Set<String> visuallyIgnoredNbtKeys
    ) {

        public PartDefinition {
            if (!itemId.startsWith("appflux:")
                    || !modelId.startsWith("appflux:")
                    || statusPolicy.isBlank()
                    || visuallyIgnoredNbtKeys.isEmpty()) {
                throw new IllegalArgumentException("invalid exact AppliedFlux part definition");
            }
            visuallyIgnoredNbtKeys = Set.copyOf(visuallyIgnoredNbtKeys);
        }
    }
}
