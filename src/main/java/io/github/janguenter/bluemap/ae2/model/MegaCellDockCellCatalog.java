/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.model;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/** Closed 67-item to 37-chassis catalog registered by MEGA Cells 4.11.0. */
public final class MegaCellDockCellCatalog {

    public static final int ITEM_COUNT = 67;
    public static final int MODEL_COUNT = 37;
    public static final int CATALOG_BYTES = 5_827;
    public static final String CATALOG_SHA256 =
            "6bd906cf6346041718f4390e9fc2b27c55f2e8ca10938996b63107d54accf6e8";

    private static final String CATALOG_RESOURCE =
            "/bluemap-ae2/profiles/megacells/4.11.0/cell-models.tsv";
    private static final List<String> TIERS = List.of("1m", "4m", "16m", "64m", "256m");
    private static final List<String> PORTABLE_TYPES =
            List.of("item", "fluid", "chemical", "mana", "source", "experience");
    private static final List<MegaCellDockCellDefinition> DEFINITIONS = loadDefinitions();
    private static final Map<String, MegaCellDockCellDefinition> BY_ID = indexById();
    private static final Set<String> IDS = Collections.unmodifiableSet(
            new LinkedHashSet<>(BY_ID.keySet())
    );
    private static final Set<String> MODELS = buildModels();

    private MegaCellDockCellCatalog() {
    }

    public static List<MegaCellDockCellDefinition> definitions() {
        return DEFINITIONS;
    }

    public static Set<String> ids() {
        return IDS;
    }

    public static Set<String> models() {
        return MODELS;
    }

    public static Optional<MegaCellDockCellDefinition> find(String itemId) {
        return Optional.ofNullable(BY_ID.get(itemId));
    }

    public static MegaCellDockCellDefinition require(String itemId) {
        MegaCellDockCellDefinition definition = BY_ID.get(itemId);
        if (definition == null) {
            throw new IllegalArgumentException("unsupported MEGA Cells cell item ID: " + itemId);
        }
        return definition;
    }

    private static List<MegaCellDockCellDefinition> loadDefinitions() {
        byte[] raw = readCatalog();
        if (raw.length != CATALOG_BYTES || !sha256(raw).equals(CATALOG_SHA256)) {
            throw new IllegalStateException("exact MEGA Cells cell catalog changed");
        }
        List<MegaCellDockCellDefinition> definitions = new ArrayList<>(ITEM_COUNT);
        String previousId = null;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                new ByteArrayInputStream(raw),
                StandardCharsets.UTF_8
        ))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                String[] fields = line.split("\\t", -1);
                if (fields.length != 3) {
                    throw new IllegalStateException("malformed exact MEGA Cells cell catalog");
                }
                if (previousId != null && previousId.compareTo(fields[0]) >= 0) {
                    throw new IllegalStateException("unsorted exact MEGA Cells cell catalog");
                }
                definitions.add(new MegaCellDockCellDefinition(
                        fields[0],
                        fields[1],
                        MegaCellDockCellDefinition.ChassisKind.fromSerializedName(fields[2])
                ));
                previousId = fields[0];
            }
        } catch (IOException exception) {
            throw new IllegalStateException("failed to parse exact MEGA Cells cell catalog", exception);
        }

        List<MegaCellDockCellDefinition> immutable = List.copyOf(definitions);
        if (!immutable.equals(expectedDefinitions())) {
            throw new IllegalStateException("invalid exact MEGA Cells cell catalog contents");
        }
        return immutable;
    }

    private static byte[] readCatalog() {
        try (InputStream input = MegaCellDockCellCatalog.class.getResourceAsStream(
                CATALOG_RESOURCE
        )) {
            if (input == null) {
                throw new IllegalStateException("missing exact MEGA Cells cell catalog");
            }
            return input.readAllBytes();
        } catch (IOException exception) {
            throw new IllegalStateException("failed to read exact MEGA Cells cell catalog", exception);
        }
    }

    private static List<MegaCellDockCellDefinition> expectedDefinitions() {
        List<MegaCellDockCellDefinition> expected = new ArrayList<>(ITEM_COUNT);
        for (String tier : TIERS) {
            for (String type : PORTABLE_TYPES) {
                String model = model(tier, type);
                expected.add(standard("megacells:" + type + "_storage_cell_" + tier, model));
                expected.add(standard("megacells:portable_" + type + "_cell_" + tier, model));
            }
            expected.add(standard(
                    "megacells:soul_storage_cell_" + tier,
                    model(tier, "soul")
            ));
        }
        expected.add(misc("megacells:bulk_item_cell", model("bulk_item_cell")));
        expected.add(misc(
                "megacells:radioactive_chemical_cell",
                model("radioactive_chemical_cell")
        ));
        return expected.stream()
                .sorted(java.util.Comparator.comparing(MegaCellDockCellDefinition::itemId))
                .toList();
    }

    private static MegaCellDockCellDefinition standard(String id, String model) {
        return new MegaCellDockCellDefinition(
                id,
                model,
                MegaCellDockCellDefinition.ChassisKind.STANDARD
        );
    }

    private static MegaCellDockCellDefinition misc(String id, String model) {
        return new MegaCellDockCellDefinition(
                id,
                model,
                MegaCellDockCellDefinition.ChassisKind.MISC
        );
    }

    private static String model(String tier, String type) {
        return model(tier + "_" + type + "_cell");
    }

    private static String model(String name) {
        return "megacells:block/drive/cells/" + name;
    }

    private static Map<String, MegaCellDockCellDefinition> indexById() {
        Map<String, MegaCellDockCellDefinition> values = new LinkedHashMap<>();
        for (MegaCellDockCellDefinition definition : DEFINITIONS) {
            if (values.put(definition.itemId(), definition) != null) {
                throw new IllegalStateException(
                        "duplicate MEGA Cells cell item ID " + definition.itemId()
                );
            }
        }
        if (values.size() != ITEM_COUNT) {
            throw new IllegalStateException("MEGA Cells cell catalog must contain 67 items");
        }
        return Collections.unmodifiableMap(values);
    }

    private static Set<String> buildModels() {
        Set<String> values = new LinkedHashSet<>();
        for (MegaCellDockCellDefinition definition : DEFINITIONS) {
            values.add(definition.modelId());
        }
        if (values.size() != MODEL_COUNT) {
            throw new IllegalStateException("MEGA Cells cell catalog must contain 37 models");
        }
        return Collections.unmodifiableSet(values);
    }

    private static String sha256(byte[] bytes) {
        try {
            return java.util.HexFormat.of().formatHex(
                    MessageDigest.getInstance("SHA-256").digest(bytes)
            );
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }
}
