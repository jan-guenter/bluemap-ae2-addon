/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.profile.megacells;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/** Closed static/offline catalog for MEGA Cells' three generic cable-bus parts. */
public final class MegaCells4110PartCatalog {

    public static final String DECOMPRESSION_MODULE = "megacells:decompression_module";
    public static final String CABLE_MEGA_INTERFACE = "megacells:cable_mega_interface";
    public static final String CABLE_MEGA_PATTERN_PROVIDER =
            "megacells:cable_mega_pattern_provider";

    private static final Map<String, Definition> DEFINITIONS = buildDefinitions();
    private static final Set<String> IDS = Collections.unmodifiableSet(
            new LinkedHashSet<>(DEFINITIONS.keySet())
    );

    private MegaCells4110PartCatalog() {
    }

    public static List<Definition> definitions() {
        return List.copyOf(DEFINITIONS.values());
    }

    public static Set<String> ids() {
        return IDS;
    }

    public static Optional<Definition> find(String id) {
        return Optional.ofNullable(DEFINITIONS.get(id));
    }

    public static Definition require(String id) {
        Definition definition = DEFINITIONS.get(id);
        if (definition == null) {
            throw new IllegalArgumentException("unsupported MEGA Cells generic part: " + id);
        }
        return definition;
    }

    private static Map<String, Definition> buildDefinitions() {
        Map<String, Definition> values = new LinkedHashMap<>();
        add(values, new Definition(
                DECOMPRESSION_MODULE,
                false,
                3,
                3,
                13,
                List.of("megacells:part/decompression_module")
        ));
        add(values, new Definition(
                CABLE_MEGA_INTERFACE,
                false,
                4,
                2,
                14,
                List.of("megacells:part/mega_interface", "ae2:part/interface_off")
        ));
        add(values, new Definition(
                CABLE_MEGA_PATTERN_PROVIDER,
                false,
                4,
                2,
                14,
                List.of("megacells:part/mega_pattern_provider", "ae2:part/interface_off")
        ));
        if (values.size() != 3) {
            throw new IllegalStateException("MEGA Cells generic part catalog must have 3 entries");
        }
        return Collections.unmodifiableMap(values);
    }

    private static void add(Map<String, Definition> values, Definition definition) {
        if (values.put(definition.id(), definition) != null) {
            throw new IllegalStateException("duplicate MEGA Cells generic part ID");
        }
    }

    public record Definition(
            String id,
            boolean persistedSpin,
            int cableConnectionLength,
            double facadeCutoutMin16,
            double facadeCutoutMax16,
            List<String> staticOfflineModels
    ) {

        public Definition {
            Objects.requireNonNull(id, "id");
            staticOfflineModels = List.copyOf(Objects.requireNonNull(
                    staticOfflineModels,
                    "staticOfflineModels"
            ));
            if (!id.matches("megacells:[a-z0-9_]+")) {
                throw new IllegalArgumentException("invalid MEGA Cells generic part ID");
            }
            if (cableConnectionLength < 0 || cableConnectionLength > 16) {
                throw new IllegalArgumentException("invalid cable connection length");
            }
            if (facadeCutoutMin16 < 0 || facadeCutoutMax16 > 16
                    || facadeCutoutMin16 >= facadeCutoutMax16) {
                throw new IllegalArgumentException("invalid facade cutout");
            }
            if (staticOfflineModels.isEmpty()) {
                throw new IllegalArgumentException("generic part requires a model");
            }
        }
    }
}
