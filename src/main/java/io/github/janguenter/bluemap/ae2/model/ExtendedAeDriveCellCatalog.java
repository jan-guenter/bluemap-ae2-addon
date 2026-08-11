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

/** Closed M3b catalog: 23 pinned AE2 cells plus three pinned ExtendedAE cells. */
public final class ExtendedAeDriveCellCatalog {

    public static final String BASE_MODEL =
            "extendedae:block/extended_drive/extended_me_drive_base";
    public static final String INFINITY_WATER_MODEL =
            "extendedae:block/drive/infinity_water_cell";
    public static final String INFINITY_COBBLESTONE_MODEL =
            "extendedae:block/drive/infinity_cobblestone_cell";
    public static final String VOID_MODEL = "extendedae:block/drive/void_cell";

    private static final List<ExtendedAeDriveCellDefinition> DEFINITIONS = buildDefinitions();
    private static final Map<String, ExtendedAeDriveCellDefinition> BY_ID = indexById();
    private static final Set<String> IDS = Collections.unmodifiableSet(
            new LinkedHashSet<>(BY_ID.keySet())
    );
    private static final Set<String> OCCUPIED_MODELS = buildOccupiedModels();

    private ExtendedAeDriveCellCatalog() {
    }

    public static Optional<ExtendedAeDriveCellDefinition> find(String itemId) {
        return Optional.ofNullable(BY_ID.get(itemId));
    }

    /** Resolves M4/M5 cells only while their independently exact route is active. */
    public static Optional<ExtendedAeDriveCellDefinition> find(
            String itemId,
            DriveCellRouteAccess routeAccess
    ) {
        ExtendedAeDriveCellDefinition core = BY_ID.get(itemId);
        if (core != null) {
            return Optional.of(core);
        }
        Objects.requireNonNull(routeAccess, "routeAccess");
        DriveCellOwner owner = extensionOwner(itemId);
        if (owner == null || !routeActive(routeAccess, owner)) {
            return Optional.empty();
        }
        try {
            return ExtensionDriveCellCatalog.findExtended(owner, itemId);
        } catch (RuntimeException | LinkageError exception) {
            disableSafely(routeAccess, owner);
            return Optional.empty();
        }
    }

    public static ExtendedAeDriveCellDefinition require(String itemId) {
        ExtendedAeDriveCellDefinition definition = BY_ID.get(itemId);
        if (definition == null) {
            throw new IllegalArgumentException(
                    "unsupported M3b Extended Drive cell item ID: " + itemId
            );
        }
        return definition;
    }

    public static ExtendedAeDriveCellDefinition require(
            String itemId,
            DriveCellRouteAccess routeAccess
    ) {
        return find(itemId, routeAccess).orElseThrow(() -> new IllegalArgumentException(
                "unsupported or inactive Extended Drive cell item ID: " + itemId
        ));
    }

    public static List<ExtendedAeDriveCellDefinition> definitions() {
        return DEFINITIONS;
    }

    public static Set<String> ids() {
        return IDS;
    }

    /** Fifteen exact occupied models: twelve AE2 and three ExtendedAE. */
    public static Set<String> occupiedModels() {
        return OCCUPIED_MODELS;
    }

    public static List<ExtendedAeDriveCellDefinition> extensionDefinitions(
            DriveCellOwner owner
    ) {
        if (owner == null || !owner.requiresExtensionRoute()) {
            throw new IllegalArgumentException("owner does not use an extension route");
        }
        return ExtensionDriveCellCatalog.extendedDefinitions(owner);
    }

    public static List<ExtendedAeDriveCellDefinition> allDefinitions() {
        List<ExtendedAeDriveCellDefinition> result = new ArrayList<>(26 + 20 + 67);
        result.addAll(DEFINITIONS);
        result.addAll(extensionDefinitions(DriveCellOwner.APPLIED_FLUX));
        result.addAll(extensionDefinitions(DriveCellOwner.MEGA_CELLS));
        return List.copyOf(result);
    }

    public static Set<String> extensionOccupiedModels(DriveCellOwner owner) {
        LinkedHashSet<String> result = new LinkedHashSet<>();
        extensionDefinitions(owner).stream()
                .map(ExtendedAeDriveCellDefinition::modelId)
                .forEach(result::add);
        return Collections.unmodifiableSet(result);
    }

    private static List<ExtendedAeDriveCellDefinition> buildDefinitions() {
        List<ExtendedAeDriveCellDefinition> definitions = new ArrayList<>(26);
        for (DriveCellDefinition definition : DriveCellCatalog.definitions()) {
            definitions.add(new ExtendedAeDriveCellDefinition(
                    definition.itemId(),
                    definition.modelId()
            ));
        }
        definitions.add(definition(
                "extendedae:infinity_water_cell",
                INFINITY_WATER_MODEL
        ));
        definitions.add(definition(
                "extendedae:infinity_cobblestone_cell",
                INFINITY_COBBLESTONE_MODEL
        ));
        definitions.add(definition("extendedae:void_cell", VOID_MODEL));
        return List.copyOf(definitions);
    }

    private static ExtendedAeDriveCellDefinition definition(String itemId, String modelId) {
        return new ExtendedAeDriveCellDefinition(itemId, modelId);
    }

    private static Map<String, ExtendedAeDriveCellDefinition> indexById() {
        Map<String, ExtendedAeDriveCellDefinition> definitions = new LinkedHashMap<>();
        for (ExtendedAeDriveCellDefinition definition : DEFINITIONS) {
            if (definitions.put(definition.itemId(), definition) != null) {
                throw new IllegalStateException(
                        "duplicate M3b Extended Drive cell item ID " + definition.itemId()
                );
            }
        }
        return Collections.unmodifiableMap(definitions);
    }

    private static Set<String> buildOccupiedModels() {
        Set<String> models = new LinkedHashSet<>();
        for (ExtendedAeDriveCellDefinition definition : DEFINITIONS) {
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
