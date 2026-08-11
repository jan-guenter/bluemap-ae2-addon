/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.profile;

import io.github.janguenter.bluemap.ae2.model.Ae2CableCatalog;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Evidence-locked identity and resources for the active AE2 19.2.17 profile. */
public final class Ae219217Profile {

    public static final String PROFILE_ID = "ae2";
    public static final String MOD_ID = "ae2";
    public static final String VERSION = "19.2.17";
    public static final String MINECRAFT_VERSION = "1.21.1";
    public static final String NEOFORGE_VERSION = "21.1.234";
    public static final String TRANSIENT_POLICY = "idle-off-unknown";
    public static final String EXACT_REASON = "exact-19.2.17";
    public static final String JAR_SHA1 = "49c18d6a4af487957d7e5a6ad5dcbf71090b8e14";
    public static final String JAR_SHA256 =
            "460d779a0609b81409907d9956de8f6f70a1b0912257e3e5c3c7e75ac9630e95";
    public static final long JAR_BYTES = 8_230_896L;
    public static final String CABLE_BUS_BLOCK = "ae2:cable_bus";
    public static final String SYNTHETIC_BLOCK_STATE = "bluemap_ae2:fluix_glass_cable";
    public static final String CORE_TEXTURE = "ae2:part/cable/core/glass/transparent";
    public static final String CONNECTION_TEXTURE = "ae2:part/cable/glass/transparent";
    public static final String TERMINAL_PART = "ae2:terminal";
    public static final String FACADE_BLOCK = "minecraft:stone";
    public static final String DRIVE_BLOCK = "ae2:drive";
    public static final String DRIVE_SYNTHETIC_BLOCK_STATE = "bluemap_ae2:drive";
    public static final String DRIVE_BASE_MODEL = "ae2:block/drive/drive_base";
    public static final String DRIVE_EMPTY_CELL_MODEL =
            "ae2:block/drive/drive_cell_empty";
    public static final String DRIVE_GENERIC_CELL_MODEL = "ae2:block/drive/drive_cell";
    public static final String DRIVE_LED_POLICY = "static-offline-unknown";
    public static final String DRIVE_UNKNOWN_CELL_POLICY =
            "atomic-whole-block-original-resource-fallback";
    public static final int DRIVE_SLOT_COUNT = 10;

    private static final Set<String> SUPPORTED_CENTER_PARTS = Ae2CableCatalog.ids();
    private static final List<String> CORE_TEXTURES = buildCoreTextures();
    private static final List<String> DRIVE_TEXTURES = List.of(
            "ae2:block/drive/drive_cells",
            "ae2:block/drive/drive_front",
            "ae2:block/drive/drive_inside",
            "ae2:block/drive/drive_inside_bottom",
            "ae2:block/drive/drive_inside_top",
            "ae2:block/generics/back",
            "ae2:block/generics/bottom",
            "ae2:block/generics/front",
            "ae2:block/generics/side",
            "ae2:block/generics/top"
    );
    private static final List<String> TEXTURES = buildTextures();
    private static final Map<String, String> EXPLICIT_DRIVE_CELL_MODELS =
            buildExplicitDriveCellModels();
    private static final List<String> GENERIC_DRIVE_CELL_IDS = List.of(
            "ae2:matter_cannon",
            "ae2:color_applicator"
    );
    private static final Map<String, String> DRIVE_CELL_MODELS = buildDriveCellModels();
    private static final Set<String> DRIVE_RESOURCE_PATHS = Set.of(
            "assets/ae2/blockstates/drive.json",
            "assets/ae2/models/block/drive.json",
            "assets/ae2/models/block/drive/drive_base.json",
            "assets/ae2/models/block/drive/drive_cell.json",
            "assets/ae2/models/block/drive/drive_cell_empty.json",
            "assets/ae2/models/block/drive/cells/1k_item_cell.json",
            "assets/ae2/models/block/drive/cells/4k_item_cell.json",
            "assets/ae2/models/block/drive/cells/16k_item_cell.json",
            "assets/ae2/models/block/drive/cells/64k_item_cell.json",
            "assets/ae2/models/block/drive/cells/256k_item_cell.json",
            "assets/ae2/models/block/drive/cells/1k_fluid_cell.json",
            "assets/ae2/models/block/drive/cells/4k_fluid_cell.json",
            "assets/ae2/models/block/drive/cells/16k_fluid_cell.json",
            "assets/ae2/models/block/drive/cells/64k_fluid_cell.json",
            "assets/ae2/models/block/drive/cells/256k_fluid_cell.json",
            "assets/ae2/models/block/drive/cells/creative_cell.json",
            "assets/ae2/textures/block/drive/drive_cells.png",
            "assets/ae2/textures/block/drive/drive_front.png",
            "assets/ae2/textures/block/drive/drive_inside.png",
            "assets/ae2/textures/block/drive/drive_inside_bottom.png",
            "assets/ae2/textures/block/drive/drive_inside_top.png",
            "assets/ae2/textures/block/generics/back.png",
            "assets/ae2/textures/block/generics/bottom.png",
            "assets/ae2/textures/block/generics/front.png",
            "assets/ae2/textures/block/generics/side.png",
            "assets/ae2/textures/block/generics/top.png"
    );
    private static final String RESOURCE_MANIFEST =
            "/bluemap-ae2/profiles/ae2/19.2.17/required-resources.sha256";
    private static final Map<String, String> REQUIRED_RESOURCES = loadRequiredResources();
    private static final ResourcePartitions RESOURCE_PARTITIONS = partitionResources();

    private Ae219217Profile() {
    }

    public static Set<String> supportedCenterParts() {
        return SUPPORTED_CENTER_PARTS;
    }

    public static List<String> textures() {
        return TEXTURES;
    }

    public static List<String> coreTextures() {
        return CORE_TEXTURES;
    }

    public static List<String> driveTextures() {
        return DRIVE_TEXTURES;
    }

    public static Map<String, String> requiredResources() {
        return REQUIRED_RESOURCES;
    }

    public static Map<String, String> coreRequiredResources() {
        return RESOURCE_PARTITIONS.core();
    }

    public static Map<String, String> driveRequiredResources() {
        return RESOURCE_PARTITIONS.drive();
    }

    public static Map<String, String> explicitDriveCellModels() {
        return EXPLICIT_DRIVE_CELL_MODELS;
    }

    public static List<String> genericDriveCellIds() {
        return GENERIC_DRIVE_CELL_IDS;
    }

    public static Map<String, String> driveCellModels() {
        return DRIVE_CELL_MODELS;
    }

    private static List<String> buildCoreTextures() {
        List<String> textures = new ArrayList<>(Ae2CableCatalog.textures());
        textures.addAll(List.of(
                "ae2:part/monitor_sides",
                "ae2:part/monitor_sides_status",
                "ae2:part/monitor_back",
                "ae2:part/monitor_front",
                "ae2:part/monitor_sides_status_off",
                "ae2:part/terminal_bright",
                "ae2:part/terminal_medium",
                "ae2:part/terminal_dark"
        ));
        return List.copyOf(textures);
    }

    private static List<String> buildTextures() {
        List<String> textures = new ArrayList<>(CORE_TEXTURES);
        textures.addAll(DRIVE_TEXTURES);
        return List.copyOf(textures);
    }

    private static Map<String, String> buildExplicitDriveCellModels() {
        Map<String, String> models = new LinkedHashMap<>();
        addDriveCellTiers(models, "item_storage_cell_", "item");
        addDriveCellTiers(models, "fluid_storage_cell_", "fluid");
        models.put("ae2:creative_storage_cell", "ae2:block/drive/cells/creative_cell");
        addDriveCellTiers(models, "portable_item_cell_", "item");
        addDriveCellTiers(models, "portable_fluid_cell_", "fluid");
        if (models.size() != 21 || Set.copyOf(models.values()).size() != 11) {
            throw new IllegalStateException("invalid exact AE2 drive cell catalog");
        }
        return Collections.unmodifiableMap(models);
    }

    private static void addDriveCellTiers(
            Map<String, String> models,
            String itemPrefix,
            String modelKind
    ) {
        for (String tier : List.of("1k", "4k", "16k", "64k", "256k")) {
            models.put(
                    "ae2:" + itemPrefix + tier,
                    "ae2:block/drive/cells/" + tier + "_" + modelKind + "_cell"
            );
        }
    }

    private static Map<String, String> buildDriveCellModels() {
        Map<String, String> models = new LinkedHashMap<>(EXPLICIT_DRIVE_CELL_MODELS);
        for (String itemId : GENERIC_DRIVE_CELL_IDS) {
            models.put(itemId, DRIVE_GENERIC_CELL_MODEL);
        }
        if (models.size() != 23 || Set.copyOf(models.values()).size() != 12) {
            throw new IllegalStateException("invalid exact AE2 supported drive catalog");
        }
        return Collections.unmodifiableMap(models);
    }

    private static Map<String, String> loadRequiredResources() {
        InputStream input = Ae219217Profile.class.getResourceAsStream(RESOURCE_MANIFEST);
        if (input == null) {
            throw new IllegalStateException("missing exact AE2 resource manifest");
        }
        Map<String, String> resources = new LinkedHashMap<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                input,
                StandardCharsets.UTF_8
        ))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                int separator = line.indexOf("  ");
                if (separator != 64 || line.length() <= separator + 2) {
                    throw new IllegalStateException("malformed exact AE2 resource manifest");
                }
                String digest = line.substring(0, separator);
                String path = line.substring(separator + 2);
                if (!digest.matches("[0-9a-f]{64}")
                        || resources.put(path, digest) != null) {
                    throw new IllegalStateException("invalid exact AE2 resource manifest");
                }
            }
        } catch (IOException exception) {
            throw new IllegalStateException("failed to read exact AE2 resource manifest", exception);
        }
        return Collections.unmodifiableMap(resources);
    }

    private static ResourcePartitions partitionResources() {
        Map<String, String> core = new LinkedHashMap<>();
        Map<String, String> drive = new LinkedHashMap<>();
        REQUIRED_RESOURCES.forEach((path, digest) -> {
            if (DRIVE_RESOURCE_PATHS.contains(path)) {
                drive.put(path, digest);
            } else {
                core.put(path, digest);
            }
        });
        if (core.size() != 170 || drive.size() != 26
                || !drive.keySet().equals(DRIVE_RESOURCE_PATHS)) {
            throw new IllegalStateException("invalid exact AE2 resource partitions");
        }
        return new ResourcePartitions(
                Collections.unmodifiableMap(core),
                Collections.unmodifiableMap(drive)
        );
    }

    private record ResourcePartitions(
            Map<String, String> core,
            Map<String, String> drive
    ) {
    }
}
