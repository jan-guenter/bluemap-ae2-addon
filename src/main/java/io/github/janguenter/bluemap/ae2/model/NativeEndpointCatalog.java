/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.model;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Collections;

/** Closed native AE2 19.2.17 adjacent node-host endpoint catalog. */
public final class NativeEndpointCatalog {

    private static final Map<String, Definition> DEFINITIONS = build();

    private NativeEndpointCatalog() {
    }

    public static Set<String> ids() {
        return Set.copyOf(DEFINITIONS.keySet());
    }

    public static List<Definition> definitions() {
        return List.copyOf(DEFINITIONS.values());
    }

    public static Optional<Definition> find(String id) {
        return Optional.ofNullable(DEFINITIONS.get(id));
    }

    private static Map<String, Definition> build() {
        LinkedHashMap<String, Definition> values = new LinkedHashMap<>();
        add(values, "ae2:inscriber", CableFamily.COVERED, "ae2:inscriber",
                SidePolicy.NO_FRONT);
        add(values, "ae2:wireless_access_point", CableFamily.SMART,
                "ae2:wireless_access_point", SidePolicy.BACK);
        add(values, "ae2:charger", CableFamily.COVERED, "ae2:charger",
                SidePolicy.NO_FRONT);
        add(values, "ae2:quantum_ring", CableFamily.DENSE_SMART, "ae2:quantum_ring",
                SidePolicy.FORMED_QUANTUM);
        add(values, "ae2:quantum_link", CableFamily.DENSE_SMART, "ae2:quantum_ring",
                SidePolicy.FORMED_QUANTUM);
        add(values, "ae2:spatial_pylon", CableFamily.SMART, "ae2:spatial_pylon",
                SidePolicy.VALID_STRAIGHT_PYLON);
        add(values, "ae2:spatial_io_port", CableFamily.SMART, "ae2:spatial_io_port",
                SidePolicy.ALL);
        add(values, "ae2:spatial_anchor", CableFamily.SMART, "ae2:spatial_anchor",
                SidePolicy.ALL);
        add(values, "ae2:controller", CableFamily.DENSE_SMART, "ae2:controller",
                SidePolicy.ALL);
        add(values, "ae2:drive", CableFamily.SMART, "ae2:drive",
                SidePolicy.NO_FRONT);
        add(values, "ae2:chest", CableFamily.SMART, "ae2:chest", SidePolicy.ALL);
        add(values, "ae2:interface", CableFamily.SMART, "ae2:interface",
                SidePolicy.ALL);
        add(values, "ae2:io_port", CableFamily.SMART, "ae2:io_port", SidePolicy.ALL);
        add(values, "ae2:energy_acceptor", CableFamily.COVERED, "ae2:energy_acceptor",
                SidePolicy.ALL);
        add(values, "ae2:crystal_resonance_generator", CableFamily.SMART,
                "ae2:crystal_resonance_generator", SidePolicy.BACK);
        add(values, "ae2:vibration_chamber", CableFamily.COVERED,
                "ae2:vibration_chamber", SidePolicy.ALL);
        add(values, "ae2:growth_accelerator", CableFamily.COVERED,
                "ae2:growth_accelerator", SidePolicy.FRONT_BACK);
        add(values, "ae2:energy_cell", CableFamily.COVERED, "ae2:energy_cell",
                SidePolicy.ALL);
        add(values, "ae2:dense_energy_cell", CableFamily.COVERED,
                "ae2:dense_energy_cell", SidePolicy.ALL);
        add(values, "ae2:creative_energy_cell", CableFamily.COVERED,
                "ae2:creative_energy_cell", SidePolicy.ALL);
        add(values, "ae2:crafting_unit", CableFamily.SMART, "ae2:crafting_unit",
                SidePolicy.FORMED_CRAFTING);
        add(values, "ae2:crafting_accelerator", CableFamily.SMART,
                "ae2:crafting_unit", SidePolicy.FORMED_CRAFTING);
        add(values, "ae2:1k_crafting_storage", CableFamily.SMART,
                "ae2:crafting_storage", SidePolicy.FORMED_CRAFTING);
        add(values, "ae2:4k_crafting_storage", CableFamily.SMART,
                "ae2:crafting_storage", SidePolicy.FORMED_CRAFTING);
        add(values, "ae2:16k_crafting_storage", CableFamily.SMART,
                "ae2:crafting_storage", SidePolicy.FORMED_CRAFTING);
        add(values, "ae2:64k_crafting_storage", CableFamily.SMART,
                "ae2:crafting_storage", SidePolicy.FORMED_CRAFTING);
        add(values, "ae2:256k_crafting_storage", CableFamily.SMART,
                "ae2:crafting_storage", SidePolicy.FORMED_CRAFTING);
        add(values, "ae2:crafting_monitor", CableFamily.SMART,
                "ae2:crafting_monitor", SidePolicy.FORMED_CRAFTING);
        add(values, "ae2:pattern_provider", CableFamily.SMART,
                "ae2:pattern_provider", SidePolicy.PUSH_DIRECTION);
        add(values, "ae2:molecular_assembler", CableFamily.COVERED,
                "ae2:molecular_assembler", SidePolicy.ALL);
        if (values.size() != 30) {
            throw new IllegalStateException("native endpoint catalog must contain 30 entries");
        }
        return Collections.unmodifiableMap(values);
    }

    private static void add(
            Map<String, Definition> values,
            String id,
            CableFamily cableType,
            String blockEntityId,
            SidePolicy sidePolicy
    ) {
        if (values.put(
                id,
                new Definition(id, cableType, blockEntityId, sidePolicy)
        ) != null) {
            throw new IllegalStateException("duplicate native endpoint " + id);
        }
    }

    public enum SidePolicy {
        ALL,
        BACK,
        NO_FRONT,
        FRONT_BACK,
        PUSH_DIRECTION,
        FORMED_CRAFTING,
        FORMED_QUANTUM,
        VALID_STRAIGHT_PYLON
    }

    public record Definition(
            String blockId,
            CableFamily cableType,
            String blockEntityId,
            SidePolicy sidePolicy
    ) {
    }
}
