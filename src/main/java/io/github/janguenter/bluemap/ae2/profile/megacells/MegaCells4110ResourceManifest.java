/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.profile.megacells;

import io.github.janguenter.bluemap.ae2.model.MegaCellDockCellCatalog;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Exact, disjoint resource partitions used by the MEGA Cells 4.11.0 profile. */
public final class MegaCells4110ResourceManifest {

    public static final int OWN_RESOURCE_COUNT = 82;
    public static final long OWN_RESOURCE_BYTES = 73_998L;

    private static final String RESOURCE_ROOT =
            "/bluemap-ae2/profiles/megacells/4.11.0/";
    private static final Map<Partition, ManifestData> MANIFESTS = loadManifests();
    private static final ManifestData ALL_OWN_RESOURCES = buildAllOwnResources();

    private MegaCells4110ResourceManifest() {
    }

    public static Map<String, String> digests(Partition partition) {
        return MANIFESTS.get(partition).digests();
    }

    public static Map<String, Long> sizes(Partition partition) {
        return MANIFESTS.get(partition).sizes();
    }

    public static Map<String, String> allOwnResourceDigests() {
        return ALL_OWN_RESOURCES.digests();
    }

    public static Map<String, Long> allOwnResourceSizes() {
        return ALL_OWN_RESOURCES.sizes();
    }

    private static Map<Partition, ManifestData> loadManifests() {
        Map<Partition, ManifestData> values = new EnumMap<>(Partition.class);
        for (Partition partition : Partition.values()) {
            values.put(partition, loadManifest(partition));
        }
        return Collections.unmodifiableMap(values);
    }

    private static ManifestData loadManifest(Partition partition) {
        byte[] raw = readResource(partition.fileName());
        if (!sha256(raw).equals(partition.manifestSha256())) {
            throw new IllegalStateException(
                    "exact MEGA Cells " + partition.name() + " manifest changed"
            );
        }

        Map<String, String> digests = new LinkedHashMap<>();
        Map<String, Long> sizes = new LinkedHashMap<>();
        String previousPath = null;
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
                if (fields.length != 3
                        || fields[0].isEmpty()
                        || !fields[1].matches("[1-9][0-9]*")
                        || !fields[2].matches("[0-9a-f]{64}")) {
                    throw new IllegalStateException("malformed exact MEGA Cells manifest");
                }
                String path = fields[0];
                if (previousPath != null && previousPath.compareTo(path) >= 0) {
                    throw new IllegalStateException("unsorted exact MEGA Cells manifest");
                }
                long size;
                try {
                    size = Long.parseLong(fields[1]);
                } catch (NumberFormatException exception) {
                    throw new IllegalStateException("invalid MEGA Cells resource size", exception);
                }
                if (digests.put(path, fields[2]) != null || sizes.put(path, size) != null) {
                    throw new IllegalStateException("duplicate MEGA Cells resource path");
                }
                previousPath = path;
            }
        } catch (IOException exception) {
            throw new IllegalStateException("failed to parse MEGA Cells resource manifest", exception);
        }

        if (digests.size() != partition.pathCount()
                || sizes.size() != partition.pathCount()
                || sizes.values().stream().mapToLong(Long::longValue).sum()
                != partition.totalBytes()
                || !digests.keySet().equals(expectedPaths(partition))) {
            throw new IllegalStateException(
                    "invalid exact MEGA Cells " + partition.name() + " resource closure"
            );
        }
        return new ManifestData(
                Collections.unmodifiableMap(digests),
                Collections.unmodifiableMap(sizes)
        );
    }

    private static byte[] readResource(String fileName) {
        try (InputStream input = MegaCells4110ResourceManifest.class.getResourceAsStream(
                RESOURCE_ROOT + fileName
        )) {
            if (input == null) {
                throw new IllegalStateException("missing exact MEGA Cells resource manifest");
            }
            return input.readAllBytes();
        } catch (IOException exception) {
            throw new IllegalStateException("failed to read MEGA Cells resource manifest", exception);
        }
    }

    private static Set<String> expectedPaths(Partition partition) {
        return switch (partition) {
            case CRAFTING -> craftingPaths();
            case CELL_DOCK -> cellDockPaths();
            case GENERIC_PARTS -> genericPartPaths();
            case DEPENDENT_AE2 -> dependentAe2Paths();
        };
    }

    private static Set<String> craftingPaths() {
        Set<String> paths = new LinkedHashSet<>();
        for (String block : List.of(
                "16m_crafting_storage",
                "1m_crafting_storage",
                "256m_crafting_storage",
                "4m_crafting_storage",
                "64m_crafting_storage",
                "mega_crafting_accelerator",
                "mega_crafting_monitor",
                "mega_crafting_unit"
        )) {
            paths.add("assets/megacells/blockstates/" + block + ".json");
        }
        for (String model : List.of(
                "16m_storage_formed",
                "1m_storage_formed",
                "256m_storage_formed",
                "4m_storage_formed",
                "64m_storage_formed",
                "accelerator_formed",
                "monitor_formed",
                "unit_formed"
        )) {
            paths.add("assets/megacells/models/block/crafting/" + model + ".json");
        }
        for (String texture : List.of(
                "16m_storage_light",
                "1m_storage_light",
                "256m_storage_light",
                "4m_storage_light",
                "64m_storage_light",
                "accelerator_light",
                "light_base",
                "monitor_base",
                "ring_corner",
                "ring_side_hor",
                "ring_side_ver",
                "unit_base"
        )) {
            paths.add("assets/megacells/textures/block/crafting/" + texture + ".png");
        }
        return paths;
    }

    private static Set<String> cellDockPaths() {
        Set<String> paths = new LinkedHashSet<>();
        MegaCellDockCellCatalog.models().stream()
                .map(MegaCells4110ResourceManifest::modelResourcePath)
                .sorted()
                .forEach(paths::add);
        paths.add("assets/megacells/models/part/cell_dock.json");
        paths.add("assets/megacells/textures/block/drive/cells/misc_cell.png");
        paths.add("assets/megacells/textures/block/drive/cells/standard_cell.png");
        paths.add("assets/megacells/textures/block/drive/cells/standard_cell_tiers.png");
        paths.add("assets/megacells/textures/part/cell_dock.png");
        paths.add("assets/megacells/textures/part/cell_dock_side.png");
        return paths;
    }

    private static String modelResourcePath(String modelId) {
        int separator = modelId.indexOf(':');
        return "assets/" + modelId.substring(0, separator) + "/models/"
                + modelId.substring(separator + 1) + ".json";
    }

    private static Set<String> genericPartPaths() {
        return Set.of(
                "assets/megacells/models/part/decompression_module.json",
                "assets/megacells/models/part/mega_interface.json",
                "assets/megacells/models/part/mega_pattern_provider.json",
                "assets/megacells/textures/part/decompression_module.png",
                "assets/megacells/textures/part/decompression_module_side.png",
                "assets/megacells/textures/part/mega_interface.png",
                "assets/megacells/textures/part/mega_interface_back.png",
                "assets/megacells/textures/part/mega_monitor_sides.png",
                "assets/megacells/textures/part/mega_monitor_sides_status.png",
                "assets/megacells/textures/part/mega_pattern_provider.png",
                "assets/megacells/textures/part/mega_pattern_provider_back.png"
        );
    }

    private static Set<String> dependentAe2Paths() {
        return Set.of(
                "assets/ae2/models/part/interface_base.json",
                "assets/ae2/models/part/interface_off.json",
                "assets/ae2/textures/block/crafting/monitor_light_bright.png",
                "assets/ae2/textures/block/crafting/monitor_light_dark.png",
                "assets/ae2/textures/block/crafting/monitor_light_medium.png",
                "assets/ae2/textures/part/monitor_sides_status.png",
                "assets/ae2/textures/part/monitor_sides_status_off.png"
        );
    }

    private static ManifestData buildAllOwnResources() {
        Map<String, String> digests = new LinkedHashMap<>();
        Map<String, Long> sizes = new LinkedHashMap<>();
        for (Partition partition : List.of(
                Partition.CRAFTING,
                Partition.CELL_DOCK,
                Partition.GENERIC_PARTS
        )) {
            ManifestData data = MANIFESTS.get(partition);
            data.digests().forEach((path, digest) -> {
                if (digests.put(path, digest) != null || sizes.put(path, data.sizes().get(path)) != null) {
                    throw new IllegalStateException("overlapping MEGA Cells own resource partitions");
                }
            });
        }
        if (digests.size() != OWN_RESOURCE_COUNT
                || sizes.values().stream().mapToLong(Long::longValue).sum()
                != OWN_RESOURCE_BYTES) {
            throw new IllegalStateException("invalid combined MEGA Cells resource closure");
        }
        return new ManifestData(
                Collections.unmodifiableMap(digests),
                Collections.unmodifiableMap(sizes)
        );
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

    public enum Partition {
        CRAFTING(
                "required-crafting-resources.tsv",
                28,
                4_905L,
                "e3655fdfc8905f7f640c567a76ac5e3dfc11325da7ab3d04fc6c5f5d13a902d5"
        ),
        CELL_DOCK(
                "required-cell-dock-resources.tsv",
                43,
                64_165L,
                "c4dd824ba7ac773289ef74a83a7316123fb46a99a8616372da233cb188b42f25"
        ),
        GENERIC_PARTS(
                "required-generic-part-resources.tsv",
                11,
                4_928L,
                "67286ce5df3fe3fdee769b236efefc2e0aa4b07f1f82a441329b67697e68996d"
        ),
        DEPENDENT_AE2(
                "required-dependent-ae2-resources.tsv",
                7,
                2_979L,
                "627c7dc0fae67cbc58d1481130215fdc43c89ffbdbd422b2fa1bd7434a9f8c02"
        );

        private final String fileName;
        private final int pathCount;
        private final long totalBytes;
        private final String manifestSha256;

        Partition(String fileName, int pathCount, long totalBytes, String manifestSha256) {
            this.fileName = fileName;
            this.pathCount = pathCount;
            this.totalBytes = totalBytes;
            this.manifestSha256 = manifestSha256;
        }

        public String fileName() {
            return fileName;
        }

        public int pathCount() {
            return pathCount;
        }

        public long totalBytes() {
            return totalBytes;
        }

        public String manifestSha256() {
            return manifestSha256;
        }
    }

    private record ManifestData(Map<String, String> digests, Map<String, Long> sizes) {
    }
}
