/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.model;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AppMekDriveCellCatalogTest {

    @Test
    void tenExactItemsShareFiveTierModelsOnlyInNativeDrive() {
        List<DriveCellDefinition> definitions = DriveCellCatalog.extensionDefinitions(
                DriveCellOwner.APPLIED_MEKANISTICS
        );
        assertEquals(10, definitions.size());

        Map<String, List<DriveCellDefinition>> byModel = definitions.stream()
                .collect(Collectors.groupingBy(DriveCellDefinition::modelId));
        assertEquals(5, byModel.size());
        for (String tier : List.of("1k", "4k", "16k", "64k", "256k")) {
            String model = "appmek:block/drive/cells/chemical_storage_cell_" + tier;
            assertEquals(
                    List.of(
                            "appmek:chemical_storage_cell_" + tier,
                            "appmek:portable_chemical_cell_" + tier
                    ),
                    byModel.get(model).stream()
                            .map(DriveCellDefinition::itemId)
                            .sorted()
                            .toList()
            );
        }

        DriveCellRouteAccess active = owner ->
                owner == DriveCellOwner.APPLIED_MEKANISTICS;
        for (DriveCellDefinition definition : definitions) {
            assertEquals(
                    definition,
                    DriveCellCatalog.require(definition.itemId(), active)
            );
            assertTrue(ExtendedAeDriveCellCatalog.find(
                    definition.itemId(),
                    active
            ).isEmpty());
            assertTrue(DriveCellCatalog.find(
                    definition.itemId(),
                    DriveCellRouteAccess.NONE
            ).isEmpty());
        }
    }

    @Test
    void futureAppMekCellRemainsUnknown() {
        DriveCellRouteAccess active = owner ->
                owner == DriveCellOwner.APPLIED_MEKANISTICS;
        assertTrue(DriveCellCatalog.find(
                "appmek:future_chemical_cell",
                active
        ).isEmpty());
    }
}
