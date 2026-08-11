/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.profile.megacells;

import io.github.janguenter.bluemap.ae2.model.CraftingBlockKind;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MegaCells4110ProfileTest {

    @Test
    void exactArtifactAndSourceCorrelationAreLocked() {
        assertEquals("megacells", MegaCells4110Profile.PROFILE_ID);
        assertEquals("4.11.0", MegaCells4110ArtifactIdentity.VERSION);
        assertEquals(1_137_276L, MegaCells4110ArtifactIdentity.JAR_BYTES);
        assertEquals(
                "a386bbf12afb11729b0dcf77f64221893d250f22e6185a4d728b9799b230bc55",
                MegaCells4110ArtifactIdentity.JAR_SHA256
        );
        assertEquals(
                "f46ab8f9a2c14f0de3e09241241b49fb2d06a81a",
                MegaCells4110ArtifactIdentity.SOURCE_COMMIT
        );
        assertEquals(941, MegaCells4110ArtifactIdentity.CORRELATED_PAYLOAD_ENTRIES);
    }

    @Test
    void eightCraftingBlocksMapToExistingGeometryKindsAndExactBlockEntities() {
        assertEquals(Map.of(
                "megacells:mega_crafting_unit", CraftingBlockKind.UNIT,
                "megacells:mega_crafting_accelerator", CraftingBlockKind.ACCELERATOR,
                "megacells:1m_crafting_storage", CraftingBlockKind.STORAGE_1K,
                "megacells:4m_crafting_storage", CraftingBlockKind.STORAGE_4K,
                "megacells:16m_crafting_storage", CraftingBlockKind.STORAGE_16K,
                "megacells:64m_crafting_storage", CraftingBlockKind.STORAGE_64K,
                "megacells:256m_crafting_storage", CraftingBlockKind.STORAGE_256K,
                "megacells:mega_crafting_monitor", CraftingBlockKind.MONITOR
        ), MegaCells4110Profile.craftingBlockKinds());
        assertEquals(8, MegaCells4110Profile.craftingBlockEntityIds().size());
        assertEquals(
                "megacells:mega_crafting_unit",
                MegaCells4110Profile.expectedCraftingBlockEntityId(
                        "megacells:mega_crafting_accelerator"
                )
        );
        assertEquals(
                "megacells:mega_crafting_storage",
                MegaCells4110Profile.expectedCraftingBlockEntityId(
                        "megacells:256m_crafting_storage"
                )
        );
        assertNull(MegaCells4110Profile.craftingKind("megacells:mega_energy_cell"));
    }

    @Test
    void exactResourcePartitionsAreClosedAndDisjoint() {
        assertEquals(28, MegaCells4110Profile.craftingRequiredResources().size());
        assertEquals(43, MegaCells4110Profile.cellDockRequiredResources().size());
        assertEquals(11, MegaCells4110Profile.genericPartRequiredResources().size());
        assertEquals(7, MegaCells4110Profile.dependentAe2RequiredResources().size());
        assertEquals(82, MegaCells4110Profile.allOwnRequiredResources().size());
        assertEquals(
                73_998L,
                MegaCells4110Profile.allOwnRequiredResourceSizes().values().stream()
                        .mapToLong(Long::longValue)
                        .sum()
        );
        assertTrue(MegaCells4110Profile.allOwnRequiredResources().keySet().stream()
                .allMatch(path -> path.startsWith("assets/megacells/")));
        assertTrue(MegaCells4110Profile.dependentAe2RequiredResources().keySet().stream()
                .allMatch(path -> path.startsWith("assets/ae2/")));
    }

    @Test
    void threeGenericPartsHaveExactStaticOfflineDefinitions() {
        assertEquals(3, MegaCells4110PartCatalog.ids().size());
        assertEquals(
                3,
                MegaCells4110PartCatalog.require("megacells:decompression_module")
                        .cableConnectionLength()
        );
        assertEquals(
                java.util.List.of(
                        "megacells:part/mega_interface",
                        "ae2:part/interface_off"
                ),
                MegaCells4110PartCatalog.require("megacells:cable_mega_interface")
                        .staticOfflineModels()
        );
        assertTrue(MegaCells4110PartCatalog.definitions().stream()
                .noneMatch(MegaCells4110PartCatalog.Definition::persistedSpin));
    }

    @Test
    void exposedProfileCollectionsAreImmutable() {
        assertThrows(
                UnsupportedOperationException.class,
                () -> MegaCells4110Profile.craftingBlockKinds().clear()
        );
        assertThrows(
                UnsupportedOperationException.class,
                () -> MegaCells4110Profile.allOwnRequiredResources().clear()
        );
        assertThrows(
                UnsupportedOperationException.class,
                () -> MegaCells4110PartCatalog.ids().clear()
        );
    }
}
