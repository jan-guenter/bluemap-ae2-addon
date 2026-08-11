/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MegaCellDockCellCatalogTest {

    @Test
    void exactCatalogLocksSixtySevenItemsToThirtySevenModels() {
        assertEquals(67, MegaCellDockCellCatalog.definitions().size());
        assertEquals(67, MegaCellDockCellCatalog.ids().size());
        assertEquals(37, MegaCellDockCellCatalog.models().size());
        assertEquals(
                "megacells:block/drive/cells/256m_experience_cell",
                MegaCellDockCellCatalog.require("megacells:experience_storage_cell_256m")
                        .modelId()
        );
        assertEquals(
                MegaCellDockCellCatalog.require("megacells:item_storage_cell_1m").modelId(),
                MegaCellDockCellCatalog.require("megacells:portable_item_cell_1m").modelId()
        );
    }

    @Test
    void soulCellsAreStandardWithoutInventedPortableRegistrations() {
        assertEquals(
                MegaCellDockCellDefinition.ChassisKind.STANDARD,
                MegaCellDockCellCatalog.require("megacells:soul_storage_cell_64m")
                        .chassisKind()
        );
        assertFalse(MegaCellDockCellCatalog.find("megacells:portable_soul_cell_64m")
                .isPresent());
    }

    @Test
    void twoSpecialCellsUseTheirExactMiscChassis() {
        assertEquals(
                MegaCellDockCellDefinition.ChassisKind.MISC,
                MegaCellDockCellCatalog.require("megacells:bulk_item_cell").chassisKind()
        );
        assertEquals(
                "megacells:block/drive/cells/radioactive_chemical_cell",
                MegaCellDockCellCatalog.require("megacells:radioactive_chemical_cell")
                        .modelId()
        );
    }

    @Test
    void catalogIsImmutableAndUnknownItemsFailClosed() {
        assertThrows(UnsupportedOperationException.class, MegaCellDockCellCatalog.ids()::clear);
        assertThrows(
                UnsupportedOperationException.class,
                MegaCellDockCellCatalog.definitions()::clear
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> MegaCellDockCellCatalog.require("ae2:item_storage_cell_1k")
        );
    }
}
