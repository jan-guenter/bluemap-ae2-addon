/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.profile;

import io.github.janguenter.bluemap.ae2.model.CraftingBlockKind;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Ae219217CraftingProfileTest {

    @Test
    void classLoadsAndExactNativeIdentityIsClosed() {
        assertEquals(30, Ae219217CraftingProfile.requiredResources().size());
        assertEquals("ae2-crafting", Ae219217CraftingProfile.PROFILE_ID);
        assertEquals("bluemap_ae2:crafting", Ae219217CraftingProfile.SYNTHETIC_BLOCK_STATE);
        assertEquals(8, Ae219217CraftingProfile.BLOCKS.size());
        assertEquals(
                Map.of(
                        "ae2:crafting_unit", CraftingBlockKind.UNIT,
                        "ae2:crafting_accelerator", CraftingBlockKind.ACCELERATOR,
                        "ae2:1k_crafting_storage", CraftingBlockKind.STORAGE_1K,
                        "ae2:4k_crafting_storage", CraftingBlockKind.STORAGE_4K,
                        "ae2:16k_crafting_storage", CraftingBlockKind.STORAGE_16K,
                        "ae2:64k_crafting_storage", CraftingBlockKind.STORAGE_64K,
                        "ae2:256k_crafting_storage", CraftingBlockKind.STORAGE_256K,
                        "ae2:crafting_monitor", CraftingBlockKind.MONITOR
                ),
                Ae219217CraftingProfile.blockKinds()
        );
        assertTrue(Ae219217CraftingProfile.FULL_SOLID);
        assertTrue(Ae219217CraftingProfile.OCCLUDING);
        assertEquals(
                CraftingBlockKind.MONITOR,
                Ae219217CraftingProfile.kindForBlock("ae2:crafting_monitor")
        );
        assertEquals(
                CraftingBlockKind.STORAGE_256K,
                Ae219217CraftingProfile.kindForBlock("ae2:256k_crafting_storage")
        );
        assertNull(Ae219217CraftingProfile.kindForBlock("megacells:mega_crafting_unit"));
    }

    @Test
    void exactResourcePartitionIsClosedAndDisjoint() {
        assertEquals(30, Ae219217CraftingProfile.requiredResources().size());
        assertEquals(30, Ae219217CraftingProfile.requiredResourceSizes().size());
        assertEquals(
                Ae219217CraftingProfile.requiredResources().keySet(),
                Ae219217CraftingProfile.requiredResourceSizes().keySet()
        );
        assertEquals(
                6_177L,
                Ae219217CraftingProfile.requiredResourceSizes().values().stream()
                        .mapToLong(Long::longValue)
                        .sum()
        );
        assertEquals(15, Ae219217CraftingProfile.textures().size());
        assertEquals(15, Set.copyOf(Ae219217CraftingProfile.textures()).size());
        assertTrue(Ae219217Profile.requiredResources().keySet().stream()
                .noneMatch(Ae219217CraftingProfile.requiredResources()::containsKey));
        assertTrue(Ae219217QuartzGlassProfile.requiredResources().keySet().stream()
                .noneMatch(Ae219217CraftingProfile.requiredResources()::containsKey));
    }

    @Test
    void exactPackCompatibleButUnsupportedConnectorSetIsClosed() {
        Set<String> connectors = Ae219217CraftingProfile
                .unsupportedCompatibleConnectorIds();
        assertEquals(Set.of(
                "megacells:mega_crafting_unit",
                "megacells:mega_crafting_accelerator",
                "megacells:mega_crafting_monitor",
                "megacells:1m_crafting_storage",
                "megacells:4m_crafting_storage",
                "megacells:16m_crafting_storage",
                "megacells:64m_crafting_storage",
                "megacells:256m_crafting_storage",
                "expandedae:exp_crafting_unit",
                "expandedae:exp_crafting_accelerator_2",
                "expandedae:exp_crafting_accelerator_4",
                "expandedae:exp_crafting_accelerator_8",
                "expandedae:exp_crafting_accelerator_16",
                "expandedae:exp_crafting_accelerator_32",
                "expandedae:exp_crafting_accelerator_64",
                "expandedae:exp_crafting_accelerator_128",
                "expandedae:exp_crafting_accelerator_256",
                "expandedae:exp_crafting_accelerator_512",
                "expandedae:exp_crafting_accelerator_1k",
                "expandedae:exp_crafting_accelerator_2k",
                "expandedae:exp_crafting_accelerator_4k",
                "expandedae:exp_crafting_accelerator_8k",
                "expandedae:exp_crafting_accelerator_16k",
                "expandedae:exp_crafting_accelerator_32k",
                "expandedae:exp_crafting_accelerator_64k",
                "expandedae:exp_crafting_accelerator_128k",
                "expandedae:exp_crafting_accelerator_256k",
                "expandedae:exp_crafting_accelerator_512k",
                "expandedae:exp_crafting_accelerator_1m"
        ), connectors);
        assertEquals(8, connectors.stream().filter(id -> id.startsWith("megacells:")).count());
        assertEquals(21, connectors.stream().filter(id -> id.startsWith("expandedae:")).count());
        assertTrue(connectors.contains("megacells:mega_crafting_monitor"));
        assertTrue(connectors.contains("expandedae:exp_crafting_unit"));
        assertTrue(connectors.contains("expandedae:exp_crafting_accelerator_1m"));
        assertTrue(connectors.stream().noneMatch(id -> id.startsWith("advanced_ae:")));
        assertTrue(connectors.stream().noneMatch(id -> id.startsWith("extendedae:")));
    }

    @Test
    void exposedCollectionsAreImmutable() {
        assertThrows(
                UnsupportedOperationException.class,
                () -> Ae219217CraftingProfile.BLOCKS.clear()
        );
        assertThrows(
                UnsupportedOperationException.class,
                () -> Ae219217CraftingProfile.blockKinds().put(
                        "example:block",
                        CraftingBlockKind.UNIT
                )
        );
        assertThrows(
                UnsupportedOperationException.class,
                () -> Ae219217CraftingProfile.unsupportedCompatibleConnectorIds().clear()
        );
        assertThrows(
                UnsupportedOperationException.class,
                () -> Ae219217CraftingProfile.requiredResources().clear()
        );
        assertThrows(
                UnsupportedOperationException.class,
                () -> Ae219217CraftingProfile.requiredResourceSizes().clear()
        );
        assertThrows(
                UnsupportedOperationException.class,
                () -> Ae219217CraftingProfile.textures().clear()
        );
    }
}
