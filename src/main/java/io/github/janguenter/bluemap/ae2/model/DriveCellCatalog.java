/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/** Closed exact catalog of the 23 AE2 19.2.17 items accepted by a native drive. */
public final class DriveCellCatalog {

    public static final String BASE_MODEL = "ae2:block/drive/drive_base";
    public static final String EMPTY_CELL_MODEL = "ae2:block/drive/drive_cell_empty";
    public static final String GENERIC_CELL_MODEL = "ae2:block/drive/drive_cell";

    private static final List<DriveCellDefinition> DEFINITIONS = buildDefinitions();
    private static final Map<String, DriveCellDefinition> BY_ID = indexById();
    private static final Set<String> IDS = Collections.unmodifiableSet(
            new LinkedHashSet<>(BY_ID.keySet())
    );
    private static final Set<String> OCCUPIED_MODELS = buildOccupiedModels();

    private DriveCellCatalog() {
    }

    public static Optional<DriveCellDefinition> find(String itemId) {
        return Optional.ofNullable(BY_ID.get(itemId));
    }

    /**
     * Resolves an exact extension cell only while that cell owner's route is active.
     * Core AE2 lookup never depends on extension catalog initialization.
     */
    public static Optional<DriveCellDefinition> find(
            String itemId,
            DriveCellRouteAccess routeAccess
    ) {
        DriveCellDefinition core = BY_ID.get(itemId);
        if (core != null) {
            return Optional.of(core);
        }
        Objects.requireNonNull(routeAccess, "routeAccess");
        DriveCellOwner owner = extensionOwner(itemId);
        if (owner == null || !routeActive(routeAccess, owner)) {
            return Optional.empty();
        }
        try {
            return ExtensionDriveCellCatalog.findNative(owner, itemId);
        } catch (RuntimeException | LinkageError exception) {
            disableSafely(routeAccess, owner);
            return Optional.empty();
        }
    }

    public static DriveCellDefinition require(String itemId) {
        DriveCellDefinition definition = BY_ID.get(itemId);
        if (definition == null) {
            throw new IllegalArgumentException("unsupported AE2 drive-cell item ID: " + itemId);
        }
        return definition;
    }

    public static DriveCellDefinition require(
            String itemId,
            DriveCellRouteAccess routeAccess
    ) {
        return find(itemId, routeAccess).orElseThrow(() -> new IllegalArgumentException(
                "unsupported or inactive Drive-cell item ID: " + itemId
        ));
    }

    public static List<DriveCellDefinition> definitions() {
        return DEFINITIONS;
    }

    public static Set<String> ids() {
        return IDS;
    }

    /** The 12 occupied-slot models: 11 registrations and AE2's generic default. */
    public static Set<String> occupiedModels() {
        return OCCUPIED_MODELS;
    }

    public static List<DriveCellDefinition> extensionDefinitions(DriveCellOwner owner) {
        if (owner == null || !owner.requiresExtensionRoute()) {
            throw new IllegalArgumentException("owner does not use an extension route");
        }
        return ExtensionDriveCellCatalog.nativeDefinitions(owner);
    }

    public static List<DriveCellDefinition> allDefinitions() {
        List<DriveCellDefinition> result = new ArrayList<>(23 + 20 + 67 + 10);
        result.addAll(DEFINITIONS);
        result.addAll(extensionDefinitions(DriveCellOwner.APPLIED_FLUX));
        result.addAll(extensionDefinitions(DriveCellOwner.MEGA_CELLS));
        result.addAll(extensionDefinitions(DriveCellOwner.APPLIED_MEKANISTICS));
        return List.copyOf(result);
    }

    public static Set<String> extensionOccupiedModels(DriveCellOwner owner) {
        LinkedHashSet<String> result = new LinkedHashSet<>();
        extensionDefinitions(owner).stream()
                .map(DriveCellDefinition::modelId)
                .forEach(result::add);
        return Collections.unmodifiableSet(result);
    }

    private static List<DriveCellDefinition> buildDefinitions() {
        List<DriveCellDefinition> definitions = new ArrayList<>(23);
        addCapacityPair(definitions, "item", "1k", "1k_item_cell");
        addCapacityPair(definitions, "item", "4k", "4k_item_cell");
        addCapacityPair(definitions, "item", "16k", "16k_item_cell");
        addCapacityPair(definitions, "item", "64k", "64k_item_cell");
        addCapacityPair(definitions, "item", "256k", "256k_item_cell");
        addCapacityPair(definitions, "fluid", "1k", "1k_fluid_cell");
        addCapacityPair(definitions, "fluid", "4k", "4k_fluid_cell");
        addCapacityPair(definitions, "fluid", "16k", "16k_fluid_cell");
        addCapacityPair(definitions, "fluid", "64k", "64k_fluid_cell");
        addCapacityPair(definitions, "fluid", "256k", "256k_fluid_cell");
        definitions.add(definition(
                "ae2:creative_storage_cell",
                "ae2:block/drive/cells/creative_cell"
        ));

        // Both are native IBasicCellItem implementations without an explicit
        // StorageCellModels registration, so AE2 selects its generic chassis.
        definitions.add(definition("ae2:matter_cannon", GENERIC_CELL_MODEL));
        definitions.add(definition("ae2:color_applicator", GENERIC_CELL_MODEL));
        return List.copyOf(definitions);
    }

    private static void addCapacityPair(
            List<DriveCellDefinition> definitions,
            String storageKind,
            String capacity,
            String modelName
    ) {
        String model = "ae2:block/drive/cells/" + modelName;
        definitions.add(definition(
                "ae2:" + storageKind + "_storage_cell_" + capacity,
                model
        ));
        definitions.add(definition(
                "ae2:portable_" + storageKind + "_cell_" + capacity,
                model
        ));
    }

    private static DriveCellDefinition definition(String itemId, String modelId) {
        return new DriveCellDefinition(itemId, modelId);
    }

    private static Map<String, DriveCellDefinition> indexById() {
        Map<String, DriveCellDefinition> definitions = new LinkedHashMap<>();
        for (DriveCellDefinition definition : DEFINITIONS) {
            if (definitions.put(definition.itemId(), definition) != null) {
                throw new IllegalStateException(
                        "duplicate AE2 drive-cell item ID " + definition.itemId()
                );
            }
        }
        return Collections.unmodifiableMap(definitions);
    }

    private static Set<String> buildOccupiedModels() {
        Set<String> models = new LinkedHashSet<>();
        for (DriveCellDefinition definition : DEFINITIONS) {
            models.add(definition.modelId());
        }
        return Collections.unmodifiableSet(models);
    }

    private static DriveCellOwner extensionOwner(String itemId) {
        try {
            DriveCellOwner owner = DriveCellOwner.fromItemId(itemId);
            return owner.requiresExtensionRoute() ? owner : null;
        } catch (IllegalArgumentException | NullPointerException exception) {
            return null;
        }
    }

    private static boolean routeActive(
            DriveCellRouteAccess routeAccess,
            DriveCellOwner owner
    ) {
        try {
            return routeAccess.isActive(owner);
        } catch (RuntimeException | LinkageError exception) {
            disableSafely(routeAccess, owner);
            return false;
        }
    }

    private static void disableSafely(
            DriveCellRouteAccess routeAccess,
            DriveCellOwner owner
    ) {
        try {
            routeAccess.disable(owner);
        } catch (RuntimeException | LinkageError ignored) {
            // A broken optional route hook cannot make the accepted core catalog unavailable.
        }
    }
}
