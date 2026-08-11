/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.model;

import io.github.janguenter.bluemap.ae2.profile.extendedae.ExtendedAe2235Catalog;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/** Closed AE2 19.2.17 native non-cable face-part rendering catalog. */
public final class NativeStructuralPartCatalog {

    public static final String CABLE_ANCHOR = "ae2:cable_anchor";
    public static final String ANNIHILATION_PLANE = "ae2:annihilation_plane";
    public static final String FORMATION_PLANE = "ae2:formation_plane";

    private static final Map<String, Definition> DEFINITIONS = build();
    private static final Map<String, Definition> EXTENSION_DEFINITIONS = buildExtensions();
    private static final Set<String> IDS = Set.copyOf(DEFINITIONS.keySet());

    private NativeStructuralPartCatalog() {
    }

    public static Set<String> ids() {
        return IDS;
    }

    public static List<Definition> definitions() {
        return List.copyOf(DEFINITIONS.values());
    }

    public static Optional<Definition> find(String id) {
        return Optional.ofNullable(DEFINITIONS.get(id));
    }

    /** Core plus exact extension definitions; route activation is checked by the caller. */
    public static Optional<Definition> findAny(String id) {
        Definition definition = DEFINITIONS.get(id);
        return Optional.ofNullable(
                definition == null ? EXTENSION_DEFINITIONS.get(id) : definition
        );
    }

    public static Set<String> extensionIds() {
        return EXTENSION_DEFINITIONS.keySet();
    }

    public static Definition require(String id) {
        Definition definition = DEFINITIONS.get(id);
        if (definition == null) {
            definition = EXTENSION_DEFINITIONS.get(id);
        }
        if (definition == null) {
            throw new IllegalArgumentException("unsupported native face part: " + id);
        }
        return definition;
    }

    private static Map<String, Definition> build() {
        LinkedHashMap<String, Definition> values = new LinkedHashMap<>();
        add(values, "ae2:quartz_fiber", Kind.STATIC, false, 16, 2, 14,
                "ae2:part/quartz_fiber");
        add(values, "ae2:toggle_bus", Kind.STATIC, false, 5, 2, 14,
                "ae2:part/toggle_bus_base", "ae2:part/toggle_bus_status_off");
        add(values, "ae2:inverted_toggle_bus", Kind.STATIC, false, 5, 2, 14,
                "ae2:part/inverted_toggle_bus_base", "ae2:part/toggle_bus_status_off");
        add(values, CABLE_ANCHOR, Kind.ANCHOR, false, 0, 7, 9,
                "ae2:part/cable_anchor", "ae2:part/cable_anchor_short");
        add(values, "ae2:monitor", Kind.REPORTING, true, 3, 2, 14,
                "ae2:part/monitor_base", "ae2:part/monitor_bright_off");
        add(values, "ae2:semi_dark_monitor", Kind.REPORTING, true, 3, 2, 14,
                "ae2:part/monitor_base", "ae2:part/monitor_medium_off");
        add(values, "ae2:dark_monitor", Kind.REPORTING, true, 3, 2, 14,
                "ae2:part/monitor_base", "ae2:part/monitor_dark_off");
        add(values, "ae2:storage_bus", Kind.STATIC, false, 4, 2, 14,
                "ae2:part/storage_bus_base", "ae2:part/storage_bus_off");
        add(values, "ae2:import_bus", Kind.STATIC, false, 5, 2, 14,
                "ae2:part/import_bus_base", "ae2:part/import_bus_off");
        add(values, "ae2:export_bus", Kind.STATIC, false, 5, 2, 14,
                "ae2:part/export_bus_base", "ae2:part/export_bus_off");
        add(values, "ae2:level_emitter", Kind.STATIC, false, 16, 6, 10,
                "ae2:part/level_emitter_base_off", "ae2:part/level_emitter_status_off");
        add(values, "ae2:energy_level_emitter", Kind.STATIC, false, 16, 6, 10,
                "ae2:part/level_emitter_base_off", "ae2:part/level_emitter_status_off");
        add(values, ANNIHILATION_PLANE, Kind.PLANE, false, 1, 1, 15,
                "ae2:part/transition_plane_off", "ae2:part/annihilation_plane");
        add(values, FORMATION_PLANE, Kind.PLANE, false, 1, 1, 15,
                "ae2:part/transition_plane_off", "ae2:part/formation_plane");
        add(values, "ae2:pattern_encoding_terminal", Kind.REPORTING, true, 3, 2, 14,
                "ae2:part/display_base", "ae2:part/display_status_off",
                "ae2:part/pattern_encoding_terminal_off");
        add(values, "ae2:crafting_terminal", Kind.REPORTING, true, 3, 2, 14,
                "ae2:part/display_base", "ae2:part/display_status_off",
                "ae2:part/crafting_terminal_off");
        add(values, "ae2:terminal", Kind.REPORTING, true, 3, 2, 14,
                "ae2:part/display_base", "ae2:part/display_status_off",
                "ae2:part/terminal_off");
        add(values, "ae2:storage_monitor", Kind.REPORTING, true, 3, 2, 14,
                "ae2:part/display_base", "ae2:part/display_status_off",
                "ae2:part/storage_monitor_off");
        add(values, "ae2:conversion_monitor", Kind.REPORTING, true, 3, 2, 14,
                "ae2:part/display_base", "ae2:part/display_status_off",
                "ae2:part/conversion_monitor_off");
        add(values, "ae2:cable_pattern_provider", Kind.STATIC, false, 4, 2, 14,
                "ae2:part/pattern_provider_base", "ae2:part/interface_off");
        add(values, "ae2:cable_interface", Kind.STATIC, false, 4, 2, 14,
                "ae2:part/interface_base", "ae2:part/interface_off");
        add(values, "ae2:pattern_access_terminal", Kind.REPORTING, true, 3, 2, 14,
                "ae2:part/display_base", "ae2:part/display_status_off",
                "ae2:part/pattern_access_terminal_off");
        add(values, "ae2:cable_energy_acceptor", Kind.STATIC, false, 2, 2, 14,
                "ae2:part/energy_acceptor");
        addP2p(values, "ae2:me_p2p_tunnel", "ae2:part/p2p/p2p_tunnel_me");
        addP2p(values, "ae2:redstone_p2p_tunnel", "ae2:part/p2p/p2p_tunnel_redstone");
        addP2p(values, "ae2:item_p2p_tunnel", "ae2:part/p2p/p2p_tunnel_items");
        addP2p(values, "ae2:fluid_p2p_tunnel", "ae2:part/p2p/p2p_tunnel_fluids");
        addP2p(values, "ae2:fe_p2p_tunnel", "ae2:part/p2p/p2p_tunnel_fe");
        addP2p(values, "ae2:light_p2p_tunnel", "ae2:part/p2p/p2p_tunnel_light");
        if (values.size() != 29) {
            throw new IllegalStateException("native face-part catalog must contain 29 entries");
        }
        return Collections.unmodifiableMap(values);
    }

    private static Map<String, Definition> buildExtensions() {
        LinkedHashMap<String, Definition> values = new LinkedHashMap<>();
        addExtension(values, "appflux:part_flux_accessor", "appflux",
                Kind.STATIC, false, 4, 2, 14,
                "appflux:part/flux_accessor");
        addExtension(values, "merequester:requester_terminal", "merequester",
                Kind.REPORTING, true, 3, 2, 14,
                "ae2:part/display_base", "merequester:part/requester_terminal_off",
                "ae2:part/display_status_off");
        addExtension(values, "expandedae:exp_pattern_provider_part", "expandedae",
                Kind.STATIC, false, 4, 2, 14,
                "expandedae:part/exp_pattern_provider_base", "ae2:part/interface_off");
        addExtension(values, "expandedae:exp_encoding_terminal", "expandedae",
                Kind.REPORTING, true, 3, 2, 14,
                "ae2:part/display_base", "expandedae:part/exp_encoding_terminal_off",
                "ae2:part/display_status_off");
        addExtension(values, "megacells:decompression_module", "megacells",
                Kind.STATIC, false, 3, 3, 13,
                "megacells:part/decompression_module");
        addExtension(values, "megacells:cable_mega_interface", "megacells",
                Kind.STATIC, false, 4, 2, 14,
                "megacells:part/mega_interface", "ae2:part/interface_off");
        addExtension(values, "megacells:cable_mega_pattern_provider", "megacells",
                Kind.STATIC, false, 4, 2, 14,
                "megacells:part/mega_pattern_provider", "ae2:part/interface_off");
        addExtension(values, "megacells:cell_dock", "megacells",
                Kind.CELL_DOCK, true, 4, 2, 14,
                "megacells:part/cell_dock");
        ExtendedAe2235Catalog.planeDefinitions().values().forEach(plane ->
                addExtension(values, plane.partId(), "extendedae-planes",
                        Kind.PLANE, plane.supportsSpin(), plane.cableConnectionLength(), 1, 15,
                        "ae2:part/transition_plane_off", plane.staticFrontTexture())
        );
        if (values.size() != 10) {
            throw new IllegalStateException("extension face-part catalog must contain 10 entries");
        }
        return Collections.unmodifiableMap(values);
    }

    private static void addP2p(
            Map<String, Definition> values,
            String id,
            String frontModel
    ) {
        add(values, id, Kind.P2P, false, 1, 2, 14,
                "ae2:part/p2p/p2p_tunnel_status_off",
                "ae2:part/p2p/p2p_tunnel_frequency", frontModel);
    }

    private static void add(
            Map<String, Definition> values,
            String id,
            Kind kind,
            boolean persistedSpin,
            int cableConnectionLength,
            double facadeCutoutMin16,
            double facadeCutoutMax16,
            String... modelPaths
    ) {
        Definition previous = values.put(id, new Definition(
                id,
                kind,
                persistedSpin,
                cableConnectionLength,
                facadeCutoutMin16,
                facadeCutoutMax16,
                List.of(modelPaths),
                null
        ));
        if (previous != null) {
            throw new IllegalStateException("duplicate native face part " + id);
        }
    }

    private static void addExtension(
            Map<String, Definition> values,
            String id,
            String routeId,
            Kind kind,
            boolean persistedSpin,
            int cableConnectionLength,
            double facadeCutoutMin16,
            double facadeCutoutMax16,
            String... modelPaths
    ) {
        Definition previous = values.put(id, new Definition(
                id,
                kind,
                persistedSpin,
                cableConnectionLength,
                facadeCutoutMin16,
                facadeCutoutMax16,
                List.of(modelPaths),
                routeId
        ));
        if (previous != null || DEFINITIONS.containsKey(id)) {
            throw new IllegalStateException("duplicate extension face part " + id);
        }
    }

    public enum Kind {
        STATIC,
        REPORTING,
        ANCHOR,
        PLANE,
        P2P,
        CELL_DOCK
    }

    public record Definition(
            String id,
            Kind kind,
            boolean persistedSpin,
            int cableConnectionLength,
            double facadeCutoutMin16,
            double facadeCutoutMax16,
            List<String> modelPaths,
            String extensionRouteId
    ) {

        public Definition {
            modelPaths = List.copyOf(modelPaths);
            if (cableConnectionLength < 0 || cableConnectionLength > 16) {
                throw new IllegalArgumentException("invalid cable connection length");
            }
            if (facadeCutoutMin16 < 0 || facadeCutoutMax16 > 16
                    || facadeCutoutMin16 >= facadeCutoutMax16) {
                throw new IllegalArgumentException("invalid facade cutout");
            }
            if (extensionRouteId != null
                    && !extensionRouteId.matches("[a-z0-9][a-z0-9._-]*")) {
                throw new IllegalArgumentException("invalid extension route ID");
            }
        }

        public boolean isExtension() {
            return extensionRouteId != null;
        }

        public boolean supportsDenseCenter() {
            return CABLE_ANCHOR.equals(id);
        }

        public boolean requestsSmartCore() {
            return "ae2:level_emitter".equals(id)
                    || "ae2:energy_level_emitter".equals(id);
        }
    }
}
