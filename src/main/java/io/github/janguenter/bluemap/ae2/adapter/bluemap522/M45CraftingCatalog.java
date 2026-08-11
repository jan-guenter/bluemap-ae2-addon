/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.adapter.bluemap522;

import de.bluecolored.bluemap.core.util.Key;
import de.bluecolored.bluemap.core.world.BlockState;
import io.github.janguenter.bluemap.ae2.model.CraftingBlockKind;
import io.github.janguenter.bluemap.ae2.model.CraftingGeometry;
import io.github.janguenter.bluemap.ae2.profile.Ae219217CraftingProfile;
import io.github.janguenter.bluemap.ae2.profile.expandedae.ExpandedAe211Catalog;
import io.github.janguenter.bluemap.ae2.profile.megacells.MegaCells4110Profile;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/** Closed union and texture projection for the two M4/M5 crafting extensions. */
final class M45CraftingCatalog {

    private static final Set<String> ALL_CRAFTING_BLOCKS = allCraftingBlocks();
    private static final Set<String> BOOLEAN_VALUES = Set.of("false", "true");
    private static final Set<String> FACING_VALUES = Set.of(
            "down", "up", "north", "south", "west", "east"
    );
    private static final Set<String> SPIN_VALUES = Set.of("0", "1", "2", "3");

    private M45CraftingCatalog() {
    }

    static Set<String> extensionBlocks() {
        LinkedHashSet<String> result = new LinkedHashSet<>(
                ExpandedAe211Catalog.craftingBlockKinds().keySet()
        );
        result.addAll(MegaCells4110Profile.CRAFTING_BLOCKS);
        return Set.copyOf(result);
    }

    static boolean isKnownCraftingBlock(String blockId) {
        return ALL_CRAFTING_BLOCKS.contains(blockId);
    }

    static CraftingBlockKind kind(String blockId) {
        CraftingBlockKind kind = ExpandedAe211Catalog.kindForCraftingBlock(blockId);
        return kind == null ? MegaCells4110Profile.craftingKind(blockId) : kind;
    }

    static String route(String blockId) {
        if (ExpandedAe211Catalog.craftingBlockKinds().containsKey(blockId)) {
            return M45Runtime.EXPANDED_AE;
        }
        if (MegaCells4110Profile.CRAFTING_BLOCKS.contains(blockId)) {
            return M45Runtime.MEGA_CELLS;
        }
        return null;
    }

    static boolean isExactState(BlockState state) {
        if (state == null || state.getId() == null) {
            return false;
        }
        String id = state.getId().getFormatted();
        CraftingBlockKind kind = kind(id);
        if (kind == null) {
            return Ae219217CraftingProfile.kindForBlock(id) != null
                    && Ae2ResourceExtension.isExactCraftingNeighborState(state);
        }
        Map<String, String> properties = state.getProperties();
        Set<String> expected = kind == CraftingBlockKind.MONITOR
                ? MegaCells4110Profile.MONITOR_STATE_PROPERTIES
                : MegaCells4110Profile.CRAFTING_STATE_PROPERTIES;
        if (!properties.keySet().equals(expected)
                || !BOOLEAN_VALUES.contains(properties.get("formed"))
                || !BOOLEAN_VALUES.contains(properties.get("powered"))) {
            return false;
        }
        return kind != CraftingBlockKind.MONITOR
                || FACING_VALUES.contains(properties.get("facing"))
                && SPIN_VALUES.contains(properties.get("spin"));
    }

    static Key texture(String blockId, CraftingGeometry.Quad quad) {
        if (ExpandedAe211Catalog.craftingDefinitions().containsKey(blockId)) {
            return expandedTexture(blockId, quad.layer());
        }
        if (MegaCells4110Profile.CRAFTING_BLOCKS.contains(blockId)) {
            return megaTexture(quad.layer());
        }
        return null;
    }

    private static Key expandedTexture(String blockId, CraftingGeometry.Layer layer) {
        ExpandedAe211Catalog.CraftingDefinition definition =
                ExpandedAe211Catalog.craftingDefinitions().get(blockId);
        String id = switch (layer) {
            case RING_CORNER -> "expandedae:block/crafting/ring_corner";
            case RING_SIDE_HORIZONTAL -> "expandedae:block/crafting/ring_side_hor";
            case RING_SIDE_VERTICAL -> "expandedae:block/crafting/ring_side_ver";
            case UNIT_BASE -> definition.dynamicBaseTexture();
            case LIGHT_BASE -> "expandedae:block/crafting/light_base";
            case ACCELERATOR_LIGHT -> definition.dynamicLightTexture().orElse(null);
            default -> null;
        };
        return id == null ? null : Key.parse(id);
    }

    private static Key megaTexture(CraftingGeometry.Layer layer) {
        String id = switch (layer) {
            case RING_CORNER -> "megacells:block/crafting/ring_corner";
            case RING_SIDE_HORIZONTAL -> "megacells:block/crafting/ring_side_hor";
            case RING_SIDE_VERTICAL -> "megacells:block/crafting/ring_side_ver";
            case UNIT_BASE -> "megacells:block/crafting/unit_base";
            case LIGHT_BASE -> "megacells:block/crafting/light_base";
            case ACCELERATOR_LIGHT -> "megacells:block/crafting/accelerator_light";
            case STORAGE_1K_LIGHT -> "megacells:block/crafting/1m_storage_light";
            case STORAGE_4K_LIGHT -> "megacells:block/crafting/4m_storage_light";
            case STORAGE_16K_LIGHT -> "megacells:block/crafting/16m_storage_light";
            case STORAGE_64K_LIGHT -> "megacells:block/crafting/64m_storage_light";
            case STORAGE_256K_LIGHT -> "megacells:block/crafting/256m_storage_light";
            case MONITOR_BASE -> "megacells:block/crafting/monitor_base";
            case MONITOR_LIGHT_DARK -> "ae2:block/crafting/monitor_light_dark";
            case MONITOR_LIGHT_MEDIUM -> "ae2:block/crafting/monitor_light_medium";
            case MONITOR_LIGHT_BRIGHT -> "ae2:block/crafting/monitor_light_bright";
        };
        return Key.parse(id);
    }

    private static Set<String> allCraftingBlocks() {
        LinkedHashSet<String> result = new LinkedHashSet<>(
                Ae219217CraftingProfile.BLOCKS
        );
        result.addAll(ExpandedAe211Catalog.craftingBlockKinds().keySet());
        result.addAll(MegaCells4110Profile.CRAFTING_BLOCKS);
        int expected = Ae219217CraftingProfile.BLOCKS.size() + 21 + 8;
        if (result.size() != expected) {
            throw new IllegalStateException("AE2-family crafting block IDs overlap");
        }
        return Set.copyOf(result);
    }
}
