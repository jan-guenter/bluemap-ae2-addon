/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.profile.megacells;

import io.github.janguenter.bluemap.ae2.model.CraftingBlockKind;
import io.github.janguenter.bluemap.ae2.model.MegaCellDockCellCatalog;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Evidence-locked M5 profile foundation for MEGA Cells 4.11.0. */
public final class MegaCells4110Profile {

    public static final String PROFILE_ID = "megacells";
    public static final String EXACT_REASON = "exact-4.11.0-m5-static-world-projection";
    public static final String DEPENDENT_AE2_VERSION = "19.2.17";
    public static final String DEPENDENT_AE2_JAR_SHA256 =
            "460d779a0609b81409907d9956de8f6f70a1b0912257e3e5c3c7e75ac9630e95";
    public static final String SYNTHETIC_CRAFTING_BLOCK_STATE =
            "bluemap_ae2:megacells_crafting";
    public static final String CELL_DOCK_PART = "megacells:cell_dock";
    public static final String CELL_DOCK_BODY_MODEL = "megacells:part/cell_dock";
    public static final String CELL_DOCK_LED_POLICY = "static-offline-unknown";
    public static final String UNKNOWN_CELL_POLICY =
            "atomic-whole-cable-bus-original-resource-fallback";
    public static final String STATIC_PART_POLICY = "static-offline-unknown";

    public static final String CRAFTING_UNIT = "megacells:mega_crafting_unit";
    public static final String CRAFTING_ACCELERATOR =
            "megacells:mega_crafting_accelerator";
    public static final String CRAFTING_STORAGE_1M = "megacells:1m_crafting_storage";
    public static final String CRAFTING_STORAGE_4M = "megacells:4m_crafting_storage";
    public static final String CRAFTING_STORAGE_16M = "megacells:16m_crafting_storage";
    public static final String CRAFTING_STORAGE_64M = "megacells:64m_crafting_storage";
    public static final String CRAFTING_STORAGE_256M = "megacells:256m_crafting_storage";
    public static final String CRAFTING_MONITOR = "megacells:mega_crafting_monitor";

    public static final String CRAFTING_UNIT_BLOCK_ENTITY =
            "megacells:mega_crafting_unit";
    public static final String CRAFTING_STORAGE_BLOCK_ENTITY =
            "megacells:mega_crafting_storage";
    public static final String CRAFTING_MONITOR_BLOCK_ENTITY =
            "megacells:mega_crafting_monitor";

    public static final boolean FULL_SOLID = true;
    public static final boolean OCCLUDING = true;
    public static final Set<String> CRAFTING_STATE_PROPERTIES = Set.of("formed", "powered");
    public static final Set<String> MONITOR_STATE_PROPERTIES =
            Set.of("formed", "powered", "facing", "spin");
    public static final Set<String> CELL_DOCK_PERSISTED_FIELDS = Set.of("cell", "spin");
    public static final Set<String> CELL_DOCK_IGNORED_FIELDS = Set.of(
            "priority",
            "cellId",
            "cellStatus"
    );

    private static final Map<String, CraftingBlockKind> CRAFTING_BLOCK_KINDS =
            buildCraftingBlockKinds();
    private static final Map<String, String> CRAFTING_BLOCK_ENTITY_IDS =
            buildCraftingBlockEntityIds();
    public static final Set<String> CRAFTING_BLOCKS = Collections.unmodifiableSet(
            new LinkedHashSet<>(CRAFTING_BLOCK_KINDS.keySet())
    );
    private static final List<String> CRAFTING_TEXTURES = List.of(
            "megacells:block/crafting/ring_corner",
            "megacells:block/crafting/ring_side_hor",
            "megacells:block/crafting/ring_side_ver",
            "megacells:block/crafting/unit_base",
            "megacells:block/crafting/light_base",
            "megacells:block/crafting/accelerator_light",
            "megacells:block/crafting/1m_storage_light",
            "megacells:block/crafting/4m_storage_light",
            "megacells:block/crafting/16m_storage_light",
            "megacells:block/crafting/64m_storage_light",
            "megacells:block/crafting/256m_storage_light",
            "megacells:block/crafting/monitor_base"
    );
    private static final List<String> DEPENDENT_AE2_MONITOR_TEXTURES = List.of(
            "ae2:block/crafting/monitor_light_dark",
            "ae2:block/crafting/monitor_light_medium",
            "ae2:block/crafting/monitor_light_bright"
    );

    private MegaCells4110Profile() {
    }

    public static Map<String, CraftingBlockKind> craftingBlockKinds() {
        return CRAFTING_BLOCK_KINDS;
    }

    public static CraftingBlockKind craftingKind(String blockId) {
        return CRAFTING_BLOCK_KINDS.get(blockId);
    }

    public static Map<String, String> craftingBlockEntityIds() {
        return CRAFTING_BLOCK_ENTITY_IDS;
    }

    public static String expectedCraftingBlockEntityId(String blockId) {
        return CRAFTING_BLOCK_ENTITY_IDS.get(blockId);
    }

    public static List<String> craftingTextures() {
        return CRAFTING_TEXTURES;
    }

    public static List<String> dependentAe2MonitorTextures() {
        return DEPENDENT_AE2_MONITOR_TEXTURES;
    }

    public static Map<String, String> craftingRequiredResources() {
        return MegaCells4110ResourceManifest.digests(
                MegaCells4110ResourceManifest.Partition.CRAFTING
        );
    }

    public static Map<String, String> cellDockRequiredResources() {
        return MegaCells4110ResourceManifest.digests(
                MegaCells4110ResourceManifest.Partition.CELL_DOCK
        );
    }

    public static Map<String, String> genericPartRequiredResources() {
        return MegaCells4110ResourceManifest.digests(
                MegaCells4110ResourceManifest.Partition.GENERIC_PARTS
        );
    }

    public static Map<String, String> dependentAe2RequiredResources() {
        return MegaCells4110ResourceManifest.digests(
                MegaCells4110ResourceManifest.Partition.DEPENDENT_AE2
        );
    }

    public static Map<String, String> allOwnRequiredResources() {
        return MegaCells4110ResourceManifest.allOwnResourceDigests();
    }

    public static Map<String, Long> allOwnRequiredResourceSizes() {
        return MegaCells4110ResourceManifest.allOwnResourceSizes();
    }

    public static Set<String> supportedCellItems() {
        return MegaCellDockCellCatalog.ids();
    }

    public static Set<String> cellChassisModels() {
        return MegaCellDockCellCatalog.models();
    }

    public static List<MegaCells4110PartCatalog.Definition> genericParts() {
        return MegaCells4110PartCatalog.definitions();
    }

    private static Map<String, CraftingBlockKind> buildCraftingBlockKinds() {
        Map<String, CraftingBlockKind> values = new LinkedHashMap<>();
        values.put(CRAFTING_UNIT, CraftingBlockKind.UNIT);
        values.put(CRAFTING_ACCELERATOR, CraftingBlockKind.ACCELERATOR);
        values.put(CRAFTING_STORAGE_1M, CraftingBlockKind.STORAGE_1K);
        values.put(CRAFTING_STORAGE_4M, CraftingBlockKind.STORAGE_4K);
        values.put(CRAFTING_STORAGE_16M, CraftingBlockKind.STORAGE_16K);
        values.put(CRAFTING_STORAGE_64M, CraftingBlockKind.STORAGE_64K);
        values.put(CRAFTING_STORAGE_256M, CraftingBlockKind.STORAGE_256K);
        values.put(CRAFTING_MONITOR, CraftingBlockKind.MONITOR);
        return Collections.unmodifiableMap(values);
    }

    private static Map<String, String> buildCraftingBlockEntityIds() {
        Map<String, String> values = new LinkedHashMap<>();
        values.put(CRAFTING_UNIT, CRAFTING_UNIT_BLOCK_ENTITY);
        values.put(CRAFTING_ACCELERATOR, CRAFTING_UNIT_BLOCK_ENTITY);
        values.put(CRAFTING_STORAGE_1M, CRAFTING_STORAGE_BLOCK_ENTITY);
        values.put(CRAFTING_STORAGE_4M, CRAFTING_STORAGE_BLOCK_ENTITY);
        values.put(CRAFTING_STORAGE_16M, CRAFTING_STORAGE_BLOCK_ENTITY);
        values.put(CRAFTING_STORAGE_64M, CRAFTING_STORAGE_BLOCK_ENTITY);
        values.put(CRAFTING_STORAGE_256M, CRAFTING_STORAGE_BLOCK_ENTITY);
        values.put(CRAFTING_MONITOR, CRAFTING_MONITOR_BLOCK_ENTITY);
        if (!values.keySet().equals(CRAFTING_BLOCK_KINDS.keySet())) {
            throw new IllegalStateException("MEGA Cells crafting identity maps diverged");
        }
        return Collections.unmodifiableMap(values);
    }
}
