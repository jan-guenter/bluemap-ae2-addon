/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.model;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Lazy exact M4/M5 Drive mappings, isolated from static core-catalog initialization. */
final class ExtensionDriveCellCatalog {

    private static final List<String> APPFLUX_TIERS = List.of(
            "1k", "4k", "16k", "64k", "256k",
            "1m", "4m", "16m", "64m", "256m"
    );

    private ExtensionDriveCellCatalog() {
    }

    static Optional<DriveCellDefinition> findNative(
            DriveCellOwner owner,
            String itemId
    ) {
        return Optional.ofNullable(nativeDefinitionsMap(owner).get(itemId));
    }

    static Optional<ExtendedAeDriveCellDefinition> findExtended(
            DriveCellOwner owner,
            String itemId
    ) {
        DriveCellDefinition nativeDefinition = nativeDefinitionsMap(owner).get(itemId);
        if (nativeDefinition == null) {
            return Optional.empty();
        }
        return Optional.of(new ExtendedAeDriveCellDefinition(
                nativeDefinition.itemId(),
                nativeDefinition.modelId()
        ));
    }

    static List<DriveCellDefinition> nativeDefinitions(DriveCellOwner owner) {
        return List.copyOf(nativeDefinitionsMap(owner).values());
    }

    static List<ExtendedAeDriveCellDefinition> extendedDefinitions(
            DriveCellOwner owner
    ) {
        return nativeDefinitions(owner).stream()
                .map(definition -> new ExtendedAeDriveCellDefinition(
                        definition.itemId(),
                        definition.modelId()
                ))
                .toList();
    }

    private static Map<String, DriveCellDefinition> nativeDefinitionsMap(
            DriveCellOwner owner
    ) {
        return switch (owner) {
            case APPLIED_FLUX -> AppFluxHolder.DEFINITIONS;
            case MEGA_CELLS -> MegaCellsHolder.DEFINITIONS;
            case AE2, EXTENDED_AE -> throw new IllegalArgumentException(
                    "owner does not use an extension Drive route"
            );
        };
    }

    private static Map<String, DriveCellDefinition> buildAppFluxDefinitions() {
        Map<String, DriveCellDefinition> result = new LinkedHashMap<>();
        for (String tier : APPFLUX_TIERS) {
            String model = "appflux:block/drive/fe_" + tier + "_cell";
            add(result, new DriveCellDefinition("appflux:fe_" + tier + "_cell", model));
            add(result, new DriveCellDefinition(
                    "appflux:fe_" + tier + "_portable_cell",
                    model
            ));
        }
        if (result.size() != 20
                || result.values().stream().map(DriveCellDefinition::modelId)
                .distinct().count() != 10) {
            throw new IllegalStateException("invalid exact AppliedFlux Drive-cell catalog");
        }
        return Collections.unmodifiableMap(result);
    }

    private static Map<String, DriveCellDefinition> buildMegaCellsDefinitions() {
        Map<String, DriveCellDefinition> result = new LinkedHashMap<>();
        for (MegaCellDockCellDefinition definition
                : MegaCellDockCellCatalog.definitions()) {
            add(result, new DriveCellDefinition(
                    definition.itemId(),
                    definition.modelId()
            ));
        }
        if (result.size() != MegaCellDockCellCatalog.ITEM_COUNT
                || result.values().stream().map(DriveCellDefinition::modelId)
                .distinct().count() != MegaCellDockCellCatalog.MODEL_COUNT) {
            throw new IllegalStateException("invalid exact MEGA Cells Drive-cell catalog");
        }
        return Collections.unmodifiableMap(result);
    }

    private static void add(
            Map<String, DriveCellDefinition> target,
            DriveCellDefinition definition
    ) {
        if (target.put(definition.itemId(), definition) != null) {
            throw new IllegalStateException(
                    "duplicate extension Drive-cell item ID " + definition.itemId()
            );
        }
    }

    private static final class AppFluxHolder {
        private static final Map<String, DriveCellDefinition> DEFINITIONS =
                buildAppFluxDefinitions();

        private AppFluxHolder() {
        }
    }

    private static final class MegaCellsHolder {
        private static final Map<String, DriveCellDefinition> DEFINITIONS =
                buildMegaCellsDefinitions();

        private MegaCellsHolder() {
        }
    }
}
