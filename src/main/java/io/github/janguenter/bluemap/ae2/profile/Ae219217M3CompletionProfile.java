/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.profile;

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

/** Evidence-locked AE2 19.2.17 constants for the combined remaining-M3 route. */
public final class Ae219217M3CompletionProfile {

    public static final String PROFILE_ID = "ae2-m3-completion";
    public static final String MOD_ID = "ae2";
    public static final String VERSION = "19.2.17";
    public static final String MINECRAFT_VERSION = "1.21.1";
    public static final String NEOFORGE_VERSION = "21.1.234";
    public static final String EXACT_REASON =
            "exact-19.2.17-m3-remaining-static-projection";
    public static final String JAR_SHA1 = "49c18d6a4af487957d7e5a6ad5dcbf71090b8e14";
    public static final String JAR_SHA256 =
            "460d779a0609b81409907d9956de8f6f70a1b0912257e3e5c3c7e75ac9630e95";
    public static final long JAR_BYTES = 8_230_896L;
    public static final String SOURCE_COMMIT =
            "79ee2c704ad62941a426c26b1cb1f76ef5b2ee5a";
    public static final String SOURCES_JAR_SHA256 =
            "d2f451203cb61c2d21fae52c683083d2f72441ca7d26725f4df5934290492e6a";
    public static final long SOURCES_JAR_BYTES = 3_814_167L;

    public static final String PAINT_BLOCK = "ae2:paint";
    public static final String SKY_STONE_CHEST_BLOCK = "ae2:sky_stone_chest";
    public static final String SMOOTH_SKY_STONE_CHEST_BLOCK =
            "ae2:smooth_sky_stone_chest";
    public static final String CRANK_BLOCK = "ae2:crank";
    public static final String INSCRIBER_BLOCK = "ae2:inscriber";
    public static final String SPATIAL_PYLON_BLOCK = "ae2:spatial_pylon";
    public static final Set<String> BLOCKS = Collections.unmodifiableSet(
            new LinkedHashSet<>(List.of(
                    PAINT_BLOCK,
                    SKY_STONE_CHEST_BLOCK,
                    SMOOTH_SKY_STONE_CHEST_BLOCK,
                    CRANK_BLOCK,
                    INSCRIBER_BLOCK,
                    SPATIAL_PYLON_BLOCK
            ))
    );

    public static final String PAINT_SYNTHETIC_BLOCK_STATE = "bluemap_ae2:paint";
    public static final String SKY_STONE_CHEST_SYNTHETIC_BLOCK_STATE =
            "bluemap_ae2:sky_stone_chest";
    public static final String CRANK_SYNTHETIC_BLOCK_STATE = "bluemap_ae2:crank";
    public static final String INSCRIBER_SYNTHETIC_BLOCK_STATE =
            "bluemap_ae2:inscriber";
    public static final String SPATIAL_PYLON_SYNTHETIC_BLOCK_STATE =
            "bluemap_ae2:spatial_pylon";
    public static final Map<String, String> SYNTHETIC_BLOCK_STATES =
            syntheticBlockStates();

    public static final String PAINT_BLOCK_ENTITY_ID = "ae2:paint";
    public static final String SKY_STONE_CHEST_BLOCK_ENTITY_ID = "ae2:sky_chest";
    public static final String CRANK_BLOCK_ENTITY_ID = "ae2:crank";
    public static final String INSCRIBER_BLOCK_ENTITY_ID = "ae2:inscriber";
    public static final String SPATIAL_PYLON_BLOCK_ENTITY_ID = "ae2:spatial_pylon";
    public static final Map<String, String> BLOCK_ENTITY_IDS = blockEntityIds();

    public static final String CRANK_TEXTURE = "ae2:block/crank";
    public static final String INSCRIBER_TEXTURE = "ae2:block/inscriber";
    public static final String INSCRIBER_INSIDE_TEXTURE =
            "ae2:block/inscriber_inside";
    public static final String PAINT1_TEXTURE = "ae2:block/paint1";
    public static final String PAINT2_TEXTURE = "ae2:block/paint2";
    public static final String PAINT3_TEXTURE = "ae2:block/paint3";
    public static final String SKY_BLOCK_CHEST_TEXTURE = "ae2:block/skyblockchest";
    public static final String SKY_CHEST_TEXTURE = "ae2:block/skychest";
    public static final String PYLON_BASE_TEXTURE =
            "ae2:block/spatial_pylon/base";
    public static final String PYLON_BASE_END_TEXTURE =
            "ae2:block/spatial_pylon/base_end";
    public static final String PYLON_BASE_SPANNED_TEXTURE =
            "ae2:block/spatial_pylon/base_spanned";
    public static final String PYLON_DIM_TEXTURE = "ae2:block/spatial_pylon/dim";
    public static final String PYLON_DIM_END_TEXTURE =
            "ae2:block/spatial_pylon/dim_end";
    public static final String PYLON_DIM_SPANNED_TEXTURE =
            "ae2:block/spatial_pylon/dim_spanned";
    public static final String PYLON_RED_TEXTURE = "ae2:block/spatial_pylon/red";
    public static final String PYLON_RED_END_TEXTURE =
            "ae2:block/spatial_pylon/red_end";
    public static final String PYLON_RED_SPANNED_TEXTURE =
            "ae2:block/spatial_pylon/red_spanned";
    public static final String SKY_STONE_FALLBACK_TEXTURE =
            "ae2:block/sky_stone_block";
    public static final String SMOOTH_SKY_STONE_FALLBACK_TEXTURE =
            "ae2:block/smooth_sky_stone_block";

    public static final String PAINT_DOTS_NBT_KEY = "dots";
    public static final int MIN_PAINT_SPLOTCHES = 1;
    public static final int MAX_PAINT_SPLOTCHES = 21;
    public static final int PAINT_TRIANGLES_PER_SPLOTCH = 2;
    public static final int SPATIAL_PYLON_TRIANGLES = 24;
    public static final int SKY_STONE_CHEST_TRIANGLES = 36;
    public static final int CRANK_TRIANGLES = 34;
    public static final int INSCRIBER_TRIANGLES = 78;
    public static final String POWER_POLICY = "static-offline-unknown";
    public static final int SPATIAL_PYLON_COMPONENT_MAX_BLOCKS = 256;
    public static final String AMBIGUOUS_POLICY =
            "bounded-locally-invalid-component-unformed-base-plus-dim";
    public static final String INCOMPLETE_COMPONENT_POLICY =
            "atomic-original-resource-fallback";

    public static final String RESOURCE_MANIFEST_SHA256 =
            "3faf7f29e2878f5525541bad855cbc66b6d45786dc8fc6ee29a6fbbf4878cca1";
    public static final String SOURCE_TEXTURE_MANIFEST_SHA256 =
            "030ebfafeeef07005946fcf5abf7b28365ec02001273b4bedcaa26b41f8de395";
    public static final String EMITTED_STATIC_TEXTURE_MANIFEST_SHA256 =
            "4652a3110adac720845b559b990dabd32e55887d43bc113f85856052bd0a8a05";
    public static final String FALLBACK_TEXTURE_MANIFEST_SHA256 =
            "aaff6681328dfc441a01f5a014182e914a82598395b7a594809b4652281a1146";

    private static final String RESOURCE_MANIFEST =
            "/bluemap-ae2/profiles/ae2/19.2.17/routes/m3-completion/"
                    + "required-resources.tsv";
    private static final List<String> SOURCE_TEXTURES = List.of(
            CRANK_TEXTURE,
            INSCRIBER_TEXTURE,
            INSCRIBER_INSIDE_TEXTURE,
            PAINT1_TEXTURE,
            PAINT2_TEXTURE,
            PAINT3_TEXTURE,
            SKY_BLOCK_CHEST_TEXTURE,
            SKY_CHEST_TEXTURE,
            PYLON_BASE_TEXTURE,
            PYLON_BASE_END_TEXTURE,
            PYLON_BASE_SPANNED_TEXTURE,
            PYLON_DIM_TEXTURE,
            PYLON_DIM_END_TEXTURE,
            PYLON_DIM_SPANNED_TEXTURE,
            PYLON_RED_TEXTURE,
            PYLON_RED_END_TEXTURE,
            PYLON_RED_SPANNED_TEXTURE
    );
    private static final List<String> EMITTED_STATIC_TEXTURES = List.of(
            CRANK_TEXTURE,
            INSCRIBER_TEXTURE,
            INSCRIBER_INSIDE_TEXTURE,
            PAINT1_TEXTURE,
            PAINT2_TEXTURE,
            PAINT3_TEXTURE,
            SKY_BLOCK_CHEST_TEXTURE,
            SKY_CHEST_TEXTURE,
            PYLON_BASE_TEXTURE,
            PYLON_BASE_END_TEXTURE,
            PYLON_BASE_SPANNED_TEXTURE,
            PYLON_DIM_TEXTURE,
            PYLON_RED_TEXTURE,
            PYLON_RED_END_TEXTURE,
            PYLON_RED_SPANNED_TEXTURE
    );
    private static final List<String> FALLBACK_ONLY_TEXTURES = List.of(
            SKY_STONE_FALLBACK_TEXTURE,
            SMOOTH_SKY_STONE_FALLBACK_TEXTURE
    );
    private static final ManifestData MANIFEST = loadRequiredResources();

    private Ae219217M3CompletionProfile() {
    }

    /** The exact 33-resource M3f closure, disjoint from accepted profile resources. */
    public static Map<String, String> requiredResources() {
        return MANIFEST.digests();
    }

    /** Uncompressed byte sizes bound into the exact M3f resource manifest. */
    public static Map<String, Long> requiredResourceSizes() {
        return MANIFEST.sizes();
    }

    /** The 17 sprites used by the exact source client render paths. */
    public static List<String> sourceTextures() {
        return SOURCE_TEXTURES;
    }

    /** The 15 sprites emitted by this bounded static projection. */
    public static List<String> emittedStaticTextures() {
        return EMITTED_STATIC_TEXTURES;
    }

    /** Two stock-model sprites retained solely for per-block atomic fallback. */
    public static List<String> fallbackOnlyTextures() {
        return FALLBACK_ONLY_TEXTURES;
    }

    private static Map<String, String> syntheticBlockStates() {
        Map<String, String> states = new LinkedHashMap<>();
        states.put(PAINT_BLOCK, PAINT_SYNTHETIC_BLOCK_STATE);
        states.put(SKY_STONE_CHEST_BLOCK, SKY_STONE_CHEST_SYNTHETIC_BLOCK_STATE);
        states.put(SMOOTH_SKY_STONE_CHEST_BLOCK, SKY_STONE_CHEST_SYNTHETIC_BLOCK_STATE);
        states.put(CRANK_BLOCK, CRANK_SYNTHETIC_BLOCK_STATE);
        states.put(INSCRIBER_BLOCK, INSCRIBER_SYNTHETIC_BLOCK_STATE);
        states.put(SPATIAL_PYLON_BLOCK, SPATIAL_PYLON_SYNTHETIC_BLOCK_STATE);
        return Collections.unmodifiableMap(states);
    }

    private static Map<String, String> blockEntityIds() {
        Map<String, String> ids = new LinkedHashMap<>();
        ids.put(PAINT_BLOCK, PAINT_BLOCK_ENTITY_ID);
        ids.put(SKY_STONE_CHEST_BLOCK, SKY_STONE_CHEST_BLOCK_ENTITY_ID);
        ids.put(SMOOTH_SKY_STONE_CHEST_BLOCK, SKY_STONE_CHEST_BLOCK_ENTITY_ID);
        ids.put(CRANK_BLOCK, CRANK_BLOCK_ENTITY_ID);
        ids.put(INSCRIBER_BLOCK, INSCRIBER_BLOCK_ENTITY_ID);
        ids.put(SPATIAL_PYLON_BLOCK, SPATIAL_PYLON_BLOCK_ENTITY_ID);
        return Collections.unmodifiableMap(ids);
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
                    throw new IllegalStateException("malformed exact M3f resource manifest");
                }
                String path = fields[0];
                if (previousPath != null && previousPath.compareTo(path) >= 0) {
                    throw new IllegalStateException("unsorted exact M3f resource manifest");
                }
                long size;
                try {
                    size = Long.parseLong(fields[1]);
                } catch (NumberFormatException exception) {
                    throw new IllegalStateException("invalid exact M3f resource size", exception);
                }
                if (digests.put(path, fields[2]) != null || sizes.put(path, size) != null) {
                    throw new IllegalStateException("duplicate exact M3f resource path");
                }
                previousPath = path;
            }
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "failed to parse exact M3f resource manifest",
                    exception
            );
        }

        validateManifest(digests, sizes);
        return new ManifestData(
                Collections.unmodifiableMap(digests),
                Collections.unmodifiableMap(sizes)
        );
    }

    private static byte[] readManifest() {
        try (InputStream input = Ae219217M3CompletionProfile.class.getResourceAsStream(
                RESOURCE_MANIFEST
        )) {
            if (input == null) {
                throw new IllegalStateException("missing exact M3f resource manifest");
            }
            return input.readAllBytes();
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "failed to read exact M3f resource manifest",
                    exception
            );
        }
    }

    private static void validateManifest(
            Map<String, String> digests,
            Map<String, Long> sizes
    ) {
        Set<String> expectedPaths = new LinkedHashSet<>(List.of(
                "assets/ae2/blockstates/crank.json",
                "assets/ae2/blockstates/inscriber.json",
                "assets/ae2/blockstates/paint.json",
                "assets/ae2/blockstates/sky_stone_chest.json",
                "assets/ae2/blockstates/smooth_sky_stone_chest.json",
                "assets/ae2/blockstates/spatial_pylon.json",
                "assets/ae2/models/block/crank.json",
                "assets/ae2/models/block/crank_base.json",
                "assets/ae2/models/block/crank_handle.json",
                "assets/ae2/models/block/inscriber.json",
                "assets/ae2/models/block/paint.json",
                "assets/ae2/models/block/sky_stone_block.json",
                "assets/ae2/models/block/smooth_sky_stone_block.json",
                "assets/ae2/models/block/spatial_pylon.json",
                "assets/ae2/textures/block/crank.png",
                "assets/ae2/textures/block/inscriber.png",
                "assets/ae2/textures/block/inscriber_inside.png",
                "assets/ae2/textures/block/paint1.png",
                "assets/ae2/textures/block/paint2.png",
                "assets/ae2/textures/block/paint3.png",
                "assets/ae2/textures/block/sky_stone_block.png",
                "assets/ae2/textures/block/skyblockchest.png",
                "assets/ae2/textures/block/skychest.png",
                "assets/ae2/textures/block/smooth_sky_stone_block.png",
                "assets/ae2/textures/block/spatial_pylon/base.png",
                "assets/ae2/textures/block/spatial_pylon/base_end.png",
                "assets/ae2/textures/block/spatial_pylon/base_spanned.png",
                "assets/ae2/textures/block/spatial_pylon/dim.png",
                "assets/ae2/textures/block/spatial_pylon/dim_end.png",
                "assets/ae2/textures/block/spatial_pylon/dim_spanned.png",
                "assets/ae2/textures/block/spatial_pylon/red.png",
                "assets/ae2/textures/block/spatial_pylon/red_end.png",
                "assets/ae2/textures/block/spatial_pylon/red_spanned.png"
        ));
        if (digests.size() != 33
                || sizes.size() != 33
                || !digests.keySet().equals(expectedPaths)
                || !sizes.keySet().equals(expectedPaths)
                || sizes.values().stream().mapToLong(Long::longValue).sum() != 22_491L) {
            throw new IllegalStateException("invalid exact M3f resource closure");
        }

        requireDigest(
                "source texture manifest",
                textureManifest(digests, sizes, SOURCE_TEXTURES),
                SOURCE_TEXTURE_MANIFEST_SHA256
        );
        requireDigest(
                "emitted-static texture manifest",
                textureManifest(digests, sizes, EMITTED_STATIC_TEXTURES),
                EMITTED_STATIC_TEXTURE_MANIFEST_SHA256
        );
        requireDigest(
                "fallback texture manifest",
                textureManifest(digests, sizes, FALLBACK_ONLY_TEXTURES),
                FALLBACK_TEXTURE_MANIFEST_SHA256
        );
    }

    private static byte[] textureManifest(
            Map<String, String> digests,
            Map<String, Long> sizes,
            List<String> textures
    ) {
        Set<String> paths = new LinkedHashSet<>();
        textures.stream().map(Ae219217M3CompletionProfile::textureResourcePath)
                .forEach(paths::add);
        StringBuilder result = new StringBuilder();
        digests.forEach((path, digest) -> {
            if (paths.contains(path)) {
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

    private static String textureResourcePath(String texture) {
        int separator = texture.indexOf(':');
        return "assets/" + texture.substring(0, separator) + "/textures/"
                + texture.substring(separator + 1) + ".png";
    }

    private static void requireDigest(String label, byte[] content, String expected) {
        if (!sha256(content).equals(expected)) {
            throw new IllegalStateException("exact M3f " + label + " changed");
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
