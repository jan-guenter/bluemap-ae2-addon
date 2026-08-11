/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.model;

import io.github.janguenter.bluemap.ae2.profile.extendedae.ExtendedAe2233Profile;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExtendedAeDriveCellCatalogTest {

    @Test
    void containsExactlyNativeAe2PlusThreePinnedExtendedAeCells() {
        assertEquals(26, ExtendedAeDriveCellCatalog.definitions().size());
        assertEquals(26, ExtendedAeDriveCellCatalog.ids().size());
        assertTrue(ExtendedAeDriveCellCatalog.ids().containsAll(DriveCellCatalog.ids()));
        assertEquals(
                Set.of(
                        "extendedae:infinity_water_cell",
                        "extendedae:infinity_cobblestone_cell",
                        "extendedae:void_cell"
                ),
                ExtendedAeDriveCellCatalog.ids().stream()
                        .filter(id -> id.startsWith("extendedae:"))
                        .collect(java.util.stream.Collectors.toSet())
        );
        assertEquals(15, ExtendedAeDriveCellCatalog.occupiedModels().size());
        assertEquals(
                ExtendedAe2233Profile.supportedItemIds(),
                ExtendedAeDriveCellCatalog.ids()
        );
        ExtendedAeDriveCellCatalog.definitions().forEach(definition -> assertEquals(
                ExtendedAe2233Profile.supportedCellModels().get(definition.itemId()),
                definition.modelId()
        ));
    }

    @Test
    void unknownPackAndExtensionRegistrationsRemainAtomicFallbackCandidates() {
        for (String unsupported : Set.of(
                "kubejs:lava_cell",
                "megacells:item_storage_cell_1m",
                "extendedae:future_cell"
        )) {
            assertTrue(ExtendedAeDriveCellCatalog.find(unsupported).isEmpty());
            assertThrows(
                    IllegalArgumentException.class,
                    () -> ExtendedAeDriveCellCatalog.require(unsupported)
            );
        }
    }

    @Test
    void definitionRejectsUnpinnedNamespacesAndModelFamilies() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new ExtendedAeDriveCellDefinition(
                        "kubejs:test_cell",
                        "extendedae:block/drive/void_cell"
                )
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> new ExtendedAeDriveCellDefinition(
                        "extendedae:test_cell",
                        "kubejs:block/drive/test_cell"
                )
        );
    }
}
