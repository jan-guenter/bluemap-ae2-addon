/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.model;

import io.github.janguenter.bluemap.ae2.profile.appflux.AppFlux215Catalog;
import io.github.janguenter.bluemap.ae2.profile.appflux.AppFlux215Profile;
import io.github.janguenter.bluemap.ae2.profile.megacells.MegaCells4110Profile;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExtensionDriveCellCatalogTest {

    private static final DriveCellRouteAccess APPFLUX_ONLY =
            owner -> owner == DriveCellOwner.APPLIED_FLUX;
    private static final DriveCellRouteAccess MEGA_ONLY =
            owner -> owner == DriveCellOwner.MEGA_CELLS;

    @Test
    void preservesTheAcceptedCoreCatalogViews() {
        assertEquals(23, DriveCellCatalog.definitions().size());
        assertEquals(23, DriveCellCatalog.ids().size());
        assertEquals(12, DriveCellCatalog.occupiedModels().size());
        assertEquals(26, ExtendedAeDriveCellCatalog.definitions().size());
        assertEquals(26, ExtendedAeDriveCellCatalog.ids().size());
        assertEquals(15, ExtendedAeDriveCellCatalog.occupiedModels().size());
    }

    @Test
    void mapsAllTwentyAppliedFluxItemsToTheTenExactModels() {
        Map<String, String> nativeModels = DriveCellCatalog.extensionDefinitions(
                DriveCellOwner.APPLIED_FLUX
        ).stream().collect(Collectors.toUnmodifiableMap(
                DriveCellDefinition::itemId,
                DriveCellDefinition::modelId
        ));

        assertEquals(AppFlux215Catalog.driveCellModels(), nativeModels);
        assertEquals(20, nativeModels.size());
        assertEquals(10, Set.copyOf(nativeModels.values()).size());
        assertEquals(
                nativeModels,
                ExtendedAeDriveCellCatalog.extensionDefinitions(
                        DriveCellOwner.APPLIED_FLUX
                ).stream().collect(Collectors.toUnmodifiableMap(
                        ExtendedAeDriveCellDefinition::itemId,
                        ExtendedAeDriveCellDefinition::modelId
                ))
        );
        assertTrue(nativeModels.values().stream().allMatch(model ->
                AppFlux215Profile.requiredResources().containsKey(modelPath(model))
        ));
    }

    @Test
    void mapsAllSixtySevenMegaCellsItemsToTheThirtySevenExactModels() {
        Map<String, String> expected = MegaCellDockCellCatalog.definitions().stream()
                .collect(Collectors.toUnmodifiableMap(
                        MegaCellDockCellDefinition::itemId,
                        MegaCellDockCellDefinition::modelId
                ));
        Map<String, String> nativeModels = DriveCellCatalog.extensionDefinitions(
                DriveCellOwner.MEGA_CELLS
        ).stream().collect(Collectors.toUnmodifiableMap(
                DriveCellDefinition::itemId,
                DriveCellDefinition::modelId
        ));

        assertEquals(expected, nativeModels);
        assertEquals(67, nativeModels.size());
        assertEquals(37, Set.copyOf(nativeModels.values()).size());
        assertEquals(
                nativeModels,
                ExtendedAeDriveCellCatalog.extensionDefinitions(
                        DriveCellOwner.MEGA_CELLS
                ).stream().collect(Collectors.toUnmodifiableMap(
                        ExtendedAeDriveCellDefinition::itemId,
                        ExtendedAeDriveCellDefinition::modelId
                ))
        );
        assertTrue(nativeModels.values().stream().allMatch(model ->
                MegaCells4110Profile.cellDockRequiredResources().containsKey(
                        modelPath(model)
                )
        ));
    }

    @Test
    void inactiveAndMismatchedOwnersRemainWholeDriveFallbackCandidates() {
        String appFlux = "appflux:fe_256m_cell";
        String mega = "megacells:item_storage_cell_1m";

        assertTrue(DriveCellCatalog.find(appFlux).isEmpty());
        assertTrue(DriveCellCatalog.find(mega).isEmpty());
        assertTrue(DriveCellCatalog.find(appFlux, DriveCellRouteAccess.NONE).isEmpty());
        assertTrue(DriveCellCatalog.find(mega, APPFLUX_ONLY).isEmpty());
        assertTrue(ExtendedAeDriveCellCatalog.find(appFlux).isEmpty());
        assertTrue(ExtendedAeDriveCellCatalog.find(mega, APPFLUX_ONLY).isEmpty());

        assertEquals(
                "appflux:block/drive/fe_256m_cell",
                DriveCellCatalog.require(appFlux, APPFLUX_ONLY).modelId()
        );
        assertEquals(
                "megacells:block/drive/cells/1m_item_cell",
                ExtendedAeDriveCellCatalog.require(mega, MEGA_ONLY).modelId()
        );
    }

    @Test
    void decoderActivationIsOwnerLocalForBothDriveTypes() {
        DriveInventoryProjection nativeInventory = DriveInventoryProjection.empty()
                .withSlot(0, DriveInventoryProjection.Slot.occupied(
                        "appflux:fe_1m_portable_cell"
                ))
                .withSlot(1, DriveInventoryProjection.Slot.occupied(
                        "megacells:bulk_item_cell"
                ));
        ExtendedAeDriveInventoryProjection extendedInventory =
                ExtendedAeDriveInventoryProjection.empty()
                        .withSlot(0, ExtendedAeDriveInventoryProjection.Slot.occupied(
                                "appflux:fe_1m_portable_cell"
                        ))
                        .withSlot(10, ExtendedAeDriveInventoryProjection.Slot.occupied(
                                "megacells:bulk_item_cell"
                        ));

        assertFalse(new DriveDecoder(APPFLUX_ONLY).decode(
                nativeInventory,
                Direction6.NORTH,
                0
        ).isSupported());
        assertFalse(new ExtendedAeDriveDecoder(MEGA_ONLY).decode(
                extendedInventory,
                Direction6.NORTH,
                0
        ).isSupported());

        DriveCellRouteAccess both = owner -> owner.requiresExtensionRoute();
        assertTrue(new DriveDecoder(both).decode(
                nativeInventory,
                Direction6.NORTH,
                0
        ).isSupported());
        assertTrue(new ExtendedAeDriveDecoder(both).decode(
                extendedInventory,
                Direction6.NORTH,
                0
        ).isSupported());
    }

    @Test
    void onlyTheTwoExactExtensionNamespacesAndModelFamiliesAreAdmitted() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new DriveCellDefinition(
                        "appflux:test",
                        "megacells:block/drive/cells/test"
                )
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> new ExtendedAeDriveCellDefinition(
                        "megacells:test",
                        "appflux:block/drive/test"
                )
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> DriveCellCatalog.extensionDefinitions(DriveCellOwner.AE2)
        );
    }

    private static String modelPath(String modelId) {
        int separator = modelId.indexOf(':');
        return "assets/" + modelId.substring(0, separator) + "/models/"
                + modelId.substring(separator + 1) + ".json";
    }
}
