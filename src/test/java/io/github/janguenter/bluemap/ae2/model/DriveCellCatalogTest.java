/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.model;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DriveCellCatalogTest {

    private static final String GENERIC = DriveCellCatalog.GENERIC_CELL_MODEL;

    private static final Map<String, String> EXPECTED = Map.ofEntries(
            entry("ae2:item_storage_cell_1k", cellModel("1k_item_cell")),
            entry("ae2:portable_item_cell_1k", cellModel("1k_item_cell")),
            entry("ae2:item_storage_cell_4k", cellModel("4k_item_cell")),
            entry("ae2:portable_item_cell_4k", cellModel("4k_item_cell")),
            entry("ae2:item_storage_cell_16k", cellModel("16k_item_cell")),
            entry("ae2:portable_item_cell_16k", cellModel("16k_item_cell")),
            entry("ae2:item_storage_cell_64k", cellModel("64k_item_cell")),
            entry("ae2:portable_item_cell_64k", cellModel("64k_item_cell")),
            entry("ae2:item_storage_cell_256k", cellModel("256k_item_cell")),
            entry("ae2:portable_item_cell_256k", cellModel("256k_item_cell")),
            entry("ae2:fluid_storage_cell_1k", cellModel("1k_fluid_cell")),
            entry("ae2:portable_fluid_cell_1k", cellModel("1k_fluid_cell")),
            entry("ae2:fluid_storage_cell_4k", cellModel("4k_fluid_cell")),
            entry("ae2:portable_fluid_cell_4k", cellModel("4k_fluid_cell")),
            entry("ae2:fluid_storage_cell_16k", cellModel("16k_fluid_cell")),
            entry("ae2:portable_fluid_cell_16k", cellModel("16k_fluid_cell")),
            entry("ae2:fluid_storage_cell_64k", cellModel("64k_fluid_cell")),
            entry("ae2:portable_fluid_cell_64k", cellModel("64k_fluid_cell")),
            entry("ae2:fluid_storage_cell_256k", cellModel("256k_fluid_cell")),
            entry("ae2:portable_fluid_cell_256k", cellModel("256k_fluid_cell")),
            entry("ae2:creative_storage_cell", cellModel("creative_cell")),
            entry("ae2:matter_cannon", GENERIC),
            entry("ae2:color_applicator", GENERIC)
    );

    @Test
    void containsExactlyTheTwentyThreeNativeIdsAndTheirExactModels() {
        Map<String, String> actual = DriveCellCatalog.definitions().stream()
                .collect(Collectors.toMap(
                        DriveCellDefinition::itemId,
                        DriveCellDefinition::modelId
                ));

        assertEquals(EXPECTED, actual);
        assertEquals(23, DriveCellCatalog.definitions().size());
        assertEquals(EXPECTED.keySet(), DriveCellCatalog.ids());
    }

    @Test
    void exposesElevenRegisteredModelsAndTheOneGenericOccupiedModel() {
        Set<String> explicitModels = EXPECTED.entrySet().stream()
                .filter(entry -> !GENERIC.equals(entry.getValue()))
                .map(Map.Entry::getValue)
                .collect(Collectors.toSet());

        assertEquals(11, explicitModels.size());
        assertEquals(12, DriveCellCatalog.occupiedModels().size());
        assertTrue(DriveCellCatalog.occupiedModels().containsAll(explicitModels));
        assertTrue(DriveCellCatalog.occupiedModels().contains(GENERIC));
        assertEquals("ae2:block/drive/drive_base", DriveCellCatalog.BASE_MODEL);
        assertEquals("ae2:block/drive/drive_cell_empty", DriveCellCatalog.EMPTY_CELL_MODEL);
    }

    @Test
    void genericFallbackIsClosedToTheTwoAuditedNativeItems() {
        Set<String> genericIds = DriveCellCatalog.definitions().stream()
                .filter(definition -> GENERIC.equals(definition.modelId()))
                .map(DriveCellDefinition::itemId)
                .collect(Collectors.toSet());

        assertEquals(Set.of("ae2:matter_cannon", "ae2:color_applicator"), genericIds);
        assertTrue(DriveCellCatalog.find("ae2:future_cell").isEmpty());
        assertTrue(DriveCellCatalog.find("extension:storage_cell_1k").isEmpty());
        assertThrows(
                IllegalArgumentException.class,
                () -> DriveCellCatalog.require("ae2:future_cell")
        );
    }

    @Test
    void lookupsAndCatalogViewsAreStableAndImmutable() {
        for (Map.Entry<String, String> expected : EXPECTED.entrySet()) {
            DriveCellDefinition definition = DriveCellCatalog.require(expected.getKey());
            assertEquals(expected.getKey(), definition.itemId());
            assertEquals(expected.getValue(), definition.modelId());
            assertEquals(definition, DriveCellCatalog.find(expected.getKey()).orElseThrow());
        }
        assertThrows(UnsupportedOperationException.class, DriveCellCatalog.definitions()::clear);
        assertThrows(UnsupportedOperationException.class, DriveCellCatalog.ids()::clear);
        assertThrows(UnsupportedOperationException.class, DriveCellCatalog.occupiedModels()::clear);
    }

    @Test
    void definitionsRejectWrongNamespacesAndModelFamilies() {
        assertThrows(
                NullPointerException.class,
                () -> new DriveCellDefinition(null, GENERIC)
        );
        assertThrows(
                NullPointerException.class,
                () -> new DriveCellDefinition("ae2:test", null)
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> new DriveCellDefinition("extension:test", GENERIC)
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> new DriveCellDefinition("ae2:test", "extension:block/drive/test")
        );
    }

    private static Map.Entry<String, String> entry(String itemId, String modelId) {
        return Map.entry(itemId, modelId);
    }

    private static String cellModel(String name) {
        return "ae2:block/drive/cells/" + name;
    }
}
