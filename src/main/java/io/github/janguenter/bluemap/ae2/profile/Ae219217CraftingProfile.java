/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.profile;

import io.github.janguenter.bluemap.ae2.model.CraftingBlockKind;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Evidence-locked AE2 19.2.17 constants for the disjoint M3d crafting route. */
public final class Ae219217CraftingProfile {

    public static final String PROFILE_ID = "ae2-crafting";
    public static final String MOD_ID = "ae2";
    public static final String VERSION = "19.2.17";
    public static final String MINECRAFT_VERSION = "1.21.1";
    public static final String NEOFORGE_VERSION = "21.1.234";
    public static final String EXACT_REASON = "exact-19.2.17-formed-crafting";
    public static final String JAR_SHA1 = "49c18d6a4af487957d7e5a6ad5dcbf71090b8e14";
    public static final String JAR_SHA256 =
            "460d779a0609b81409907d9956de8f6f70a1b0912257e3e5c3c7e75ac9630e95";
    public static final long JAR_BYTES = 8_230_896L;

    public static final String CRAFTING_UNIT_BLOCK = "ae2:crafting_unit";
    public static final String CRAFTING_ACCELERATOR_BLOCK = "ae2:crafting_accelerator";
    public static final String CRAFTING_STORAGE_1K_BLOCK = "ae2:1k_crafting_storage";
    public static final String CRAFTING_STORAGE_4K_BLOCK = "ae2:4k_crafting_storage";
    public static final String CRAFTING_STORAGE_16K_BLOCK = "ae2:16k_crafting_storage";
    public static final String CRAFTING_STORAGE_64K_BLOCK = "ae2:64k_crafting_storage";
    public static final String CRAFTING_STORAGE_256K_BLOCK = "ae2:256k_crafting_storage";
    public static final String CRAFTING_MONITOR_BLOCK = "ae2:crafting_monitor";
    public static final String SYNTHETIC_BLOCK_STATE = "bluemap_ae2:crafting";
    public static final boolean FULL_SOLID = true;
    public static final boolean OCCLUDING = true;

    public static final String RING_CORNER_TEXTURE = "ae2:block/crafting/ring_corner";
    public static final String RING_SIDE_HORIZONTAL_TEXTURE =
            "ae2:block/crafting/ring_side_hor";
    public static final String RING_SIDE_VERTICAL_TEXTURE =
            "ae2:block/crafting/ring_side_ver";
    public static final String UNIT_BASE_TEXTURE = "ae2:block/crafting/unit_base";
    public static final String LIGHT_BASE_TEXTURE = "ae2:block/crafting/light_base";
    public static final String ACCELERATOR_LIGHT_TEXTURE =
            "ae2:block/crafting/accelerator_light";
    public static final String STORAGE_1K_LIGHT_TEXTURE =
            "ae2:block/crafting/1k_storage_light";
    public static final String STORAGE_4K_LIGHT_TEXTURE =
            "ae2:block/crafting/4k_storage_light";
    public static final String STORAGE_16K_LIGHT_TEXTURE =
            "ae2:block/crafting/16k_storage_light";
    public static final String STORAGE_64K_LIGHT_TEXTURE =
            "ae2:block/crafting/64k_storage_light";
    public static final String STORAGE_256K_LIGHT_TEXTURE =
            "ae2:block/crafting/256k_storage_light";
    public static final String MONITOR_BASE_TEXTURE = "ae2:block/crafting/monitor_base";
    public static final String MONITOR_LIGHT_DARK_TEXTURE =
            "ae2:block/crafting/monitor_light_dark";
    public static final String MONITOR_LIGHT_MEDIUM_TEXTURE =
            "ae2:block/crafting/monitor_light_medium";
    public static final String MONITOR_LIGHT_BRIGHT_TEXTURE =
            "ae2:block/crafting/monitor_light_bright";

    public static final String RESOURCE_MANIFEST_SHA256 =
            "dc474ba6ce7c4c2d53778827b1c1f9b4994594ea984ed7a2cbd62c40e1bc1183";
    public static final String TEXTURE_MANIFEST_SHA256 =
            "a9a2a1ed912f562362d581cbd219b40afd4c884452a0c64cee3d015dfdc81620";

    private static final String RESOURCE_MANIFEST =
            "/bluemap-ae2/profiles/ae2/19.2.17/routes/crafting/"
                    + "required-resources.tsv";
    private static final Map<String, CraftingBlockKind> BLOCK_KINDS = buildBlockKinds();
    public static final Set<String> BLOCKS = Collections.unmodifiableSet(
            new LinkedHashSet<>(BLOCK_KINDS.keySet())
    );
    private static final Set<String> UNSUPPORTED_COMPATIBLE_CONNECTOR_IDS =
            buildUnsupportedCompatibleConnectorIds();
    private static final List<String> TEXTURES = List.of(
            RING_CORNER_TEXTURE,
            RING_SIDE_HORIZONTAL_TEXTURE,
            RING_SIDE_VERTICAL_TEXTURE,
            UNIT_BASE_TEXTURE,
            LIGHT_BASE_TEXTURE,
            ACCELERATOR_LIGHT_TEXTURE,
            STORAGE_1K_LIGHT_TEXTURE,
            STORAGE_4K_LIGHT_TEXTURE,
            STORAGE_16K_LIGHT_TEXTURE,
            STORAGE_64K_LIGHT_TEXTURE,
            STORAGE_256K_LIGHT_TEXTURE,
            MONITOR_BASE_TEXTURE,
            MONITOR_LIGHT_DARK_TEXTURE,
            MONITOR_LIGHT_MEDIUM_TEXTURE,
            MONITOR_LIGHT_BRIGHT_TEXTURE
    );
    private static final ManifestData MANIFEST = loadRequiredResources();

    private Ae219217CraftingProfile() {
    }

    public static Map<String, CraftingBlockKind> blockKinds() {
        return BLOCK_KINDS;
    }

    /** Returns the closed native kind, or {@code null} for a non-native block ID. */
    public static CraftingBlockKind kindForBlock(String blockId) {
        return BLOCK_KINDS.get(blockId);
    }

    /**
     * Exact pack-pinned extension IDs that are real AE2 crafting connectors but are not
     * rendered by the native M3d route.
     */
    public static Set<String> unsupportedCompatibleConnectorIds() {
        return UNSUPPORTED_COMPATIBLE_CONNECTOR_IDS;
    }

    /** The exact, disjoint 30-resource M3d world-render partition. */
    public static Map<String, String> requiredResources() {
        return MANIFEST.digests();
    }

    public static Map<String, Long> requiredResourceSizes() {
        return MANIFEST.sizes();
    }

    public static List<String> textures() {
        return TEXTURES;
    }

    private static Map<String, CraftingBlockKind> buildBlockKinds() {
        Map<String, CraftingBlockKind> result = new LinkedHashMap<>();
        result.put(CRAFTING_UNIT_BLOCK, CraftingBlockKind.UNIT);
        result.put(CRAFTING_ACCELERATOR_BLOCK, CraftingBlockKind.ACCELERATOR);
        result.put(CRAFTING_STORAGE_1K_BLOCK, CraftingBlockKind.STORAGE_1K);
        result.put(CRAFTING_STORAGE_4K_BLOCK, CraftingBlockKind.STORAGE_4K);
        result.put(CRAFTING_STORAGE_16K_BLOCK, CraftingBlockKind.STORAGE_16K);
        result.put(CRAFTING_STORAGE_64K_BLOCK, CraftingBlockKind.STORAGE_64K);
        result.put(CRAFTING_STORAGE_256K_BLOCK, CraftingBlockKind.STORAGE_256K);
        result.put(CRAFTING_MONITOR_BLOCK, CraftingBlockKind.MONITOR);
        return Collections.unmodifiableMap(result);
    }

    private static Set<String> buildUnsupportedCompatibleConnectorIds() {
        Set<String> result = new LinkedHashSet<>(List.of(
                "megacells:mega_crafting_unit",
                "megacells:mega_crafting_accelerator",
                "megacells:mega_crafting_monitor",
                "megacells:1m_crafting_storage",
                "megacells:4m_crafting_storage",
                "megacells:16m_crafting_storage",
                "megacells:64m_crafting_storage",
                "megacells:256m_crafting_storage",
                "expandedae:exp_crafting_unit"
        ));
        for (String tier : List.of(
                "2", "4", "8", "16", "32", "64", "128", "256", "512",
                "1k", "2k", "4k", "8k", "16k", "32k", "64k", "128k",
                "256k", "512k", "1m"
        )) {
            result.add("expandedae:exp_crafting_accelerator_" + tier);
        }
        return Collections.unmodifiableSet(result);
    }

    private static ManifestData loadRequiredResources() {
        byte[] raw = readManifest();
        requireDigest("resource manifest", raw, RESOURCE_MANIFEST_SHA256);

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
                    throw new IllegalStateException("malformed exact M3d resource manifest");
                }
                String path = fields[0];
                if (previousPath != null && previousPath.compareTo(path) >= 0) {
                    throw new IllegalStateException("unsorted exact M3d resource manifest");
                }
                long size;
                try {
                    size = Long.parseLong(fields[1]);
                } catch (NumberFormatException exception) {
                    throw new IllegalStateException(
                            "invalid exact M3d resource size",
                            exception
                    );
                }
                if (digests.put(path, fields[2]) != null || sizes.put(path, size) != null) {
                    throw new IllegalStateException("duplicate exact M3d resource path");
                }
                previousPath = path;
            }
        } catch (IOException exception) {
            throw new IllegalStateException("failed to parse exact M3d resource manifest", exception);
        }

        validateManifest(digests, sizes);
        return new ManifestData(
                Collections.unmodifiableMap(digests),
                Collections.unmodifiableMap(sizes)
        );
    }

    private static byte[] readManifest() {
        try (InputStream input = Ae219217CraftingProfile.class.getResourceAsStream(
                RESOURCE_MANIFEST
        )) {
            if (input == null) {
                throw new IllegalStateException("missing exact M3d resource manifest");
            }
            return input.readAllBytes();
        } catch (IOException exception) {
            throw new IllegalStateException("failed to read exact M3d resource manifest", exception);
        }
    }

    private static void validateManifest(
            Map<String, String> digests,
            Map<String, Long> sizes
    ) {
        Set<String> expectedPaths = new LinkedHashSet<>();
        BLOCKS.stream()
                .map(Ae219217CraftingProfile::blockstateResourcePath)
                .sorted()
                .forEach(expectedPaths::add);
        List.of(
                "1k_storage_formed",
                "4k_storage_formed",
                "16k_storage_formed",
                "64k_storage_formed",
                "256k_storage_formed",
                "accelerator_formed",
                "unit_formed"
        ).stream()
                .map(model -> "assets/ae2/models/block/crafting/" + model + ".json")
                .sorted()
                .forEach(expectedPaths::add);
        TEXTURES.stream()
                .map(Ae219217CraftingProfile::textureResourcePath)
                .sorted()
                .forEach(expectedPaths::add);

        if (digests.size() != 30
                || sizes.size() != 30
                || !digests.keySet().equals(expectedPaths)
                || !sizes.keySet().equals(expectedPaths)
                || sizes.values().stream().mapToLong(Long::longValue).sum() != 6_177L) {
            throw new IllegalStateException("invalid exact M3d resource closure");
        }

        requireDigest(
                "texture manifest",
                resourceManifest(digests, sizes, true),
                TEXTURE_MANIFEST_SHA256
        );
    }

    private static String blockstateResourcePath(String blockId) {
        int separator = blockId.indexOf(':');
        return "assets/" + blockId.substring(0, separator) + "/blockstates/"
                + blockId.substring(separator + 1) + ".json";
    }

    private static String textureResourcePath(String texture) {
        int separator = texture.indexOf(':');
        return "assets/" + texture.substring(0, separator) + "/textures/"
                + texture.substring(separator + 1) + ".png";
    }

    private static byte[] resourceManifest(
            Map<String, String> digests,
            Map<String, Long> sizes,
            boolean texturesOnly
    ) {
        StringBuilder result = new StringBuilder();
        digests.forEach((path, digest) -> {
            if (!texturesOnly || path.contains("/textures/")) {
                result.append(path)
                        .append('\t')
                        .append(sizes.get(path))
                        .append('\t')
                        .append(digest)
                        .append('\n');
            }
        });
        return result.toString().getBytes(StandardCharsets.UTF_8);
    }

    private static void requireDigest(String label, byte[] content, String expected) {
        if (!sha256(content).equals(expected)) {
            throw new IllegalStateException("exact M3d " + label + " changed");
        }
    }

    private static String sha256(byte[] content) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(content);
            return java.util.HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 unavailable", exception);
        }
    }

    private record ManifestData(Map<String, String> digests, Map<String, Long> sizes) {
    }
}
