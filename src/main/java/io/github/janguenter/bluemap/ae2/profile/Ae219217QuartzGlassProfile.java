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

/** Evidence-locked AE2 19.2.17 constants for the disjoint M3c quartz-glass route. */
public final class Ae219217QuartzGlassProfile {

    public static final String PROFILE_ID = "ae2-quartz-glass";
    public static final String MOD_ID = "ae2";
    public static final String VERSION = "19.2.17";
    public static final String MINECRAFT_VERSION = "1.21.1";
    public static final String NEOFORGE_VERSION = "21.1.234";
    public static final String EXACT_REASON = "exact-19.2.17-quartz-glass";
    public static final String JAR_SHA1 = "49c18d6a4af487957d7e5a6ad5dcbf71090b8e14";
    public static final String JAR_SHA256 =
            "460d779a0609b81409907d9956de8f6f70a1b0912257e3e5c3c7e75ac9630e95";
    public static final long JAR_BYTES = 8_230_896L;

    public static final String QUARTZ_GLASS_BLOCK = "ae2:quartz_glass";
    public static final String VIBRANT_GLASS_BLOCK = "ae2:quartz_vibrant_glass";
    public static final Set<String> BLOCKS = Collections.unmodifiableSet(
            new LinkedHashSet<>(List.of(QUARTZ_GLASS_BLOCK, VIBRANT_GLASS_BLOCK))
    );
    public static final String SYNTHETIC_BLOCK_STATE = "bluemap_ae2:quartz_glass";

    public static final String RESOURCE_MANIFEST_SHA256 =
            "b51c708e7c4d26093c1b6f85b88d0be50572d3cfa76dbf802720f6ad79c7a7fa";
    public static final String TEXTURE_MANIFEST_SHA256 =
            "65005c9b76800cdeba5c4598472a44dea131c9974672f89bf421452755fefb6a";
    public static final String CANONICAL_RESOURCE_PARTITION_SHA256 =
            "3704e90b1c8ec9ee5a7d7215995869500b50c9b61a797584f6732713dab7103d";
    public static final String CANONICAL_TEXTURE_PARTITION_SHA256 =
            "f9373f23e0924f6f2e7315ffb42f4d12b01d6d81b4996651917b865166d04e15";

    private static final String RESOURCE_MANIFEST =
            "/bluemap-ae2/profiles/ae2/19.2.17/routes/quartz-glass/"
                    + "required-resources.tsv";
    private static final List<String> TEXTURES = List.of(
            "ae2:block/glass/quartz_glass_a",
            "ae2:block/glass/quartz_glass_b",
            "ae2:block/glass/quartz_glass_c",
            "ae2:block/glass/quartz_glass_d",
            "ae2:block/glass/quartz_glass_frame0001",
            "ae2:block/glass/quartz_glass_frame0010",
            "ae2:block/glass/quartz_glass_frame0011",
            "ae2:block/glass/quartz_glass_frame0100",
            "ae2:block/glass/quartz_glass_frame0101",
            "ae2:block/glass/quartz_glass_frame0110",
            "ae2:block/glass/quartz_glass_frame0111",
            "ae2:block/glass/quartz_glass_frame1000",
            "ae2:block/glass/quartz_glass_frame1001",
            "ae2:block/glass/quartz_glass_frame1010",
            "ae2:block/glass/quartz_glass_frame1011",
            "ae2:block/glass/quartz_glass_frame1100",
            "ae2:block/glass/quartz_glass_frame1101",
            "ae2:block/glass/quartz_glass_frame1110",
            "ae2:block/glass/quartz_glass_frame1111"
    );
    private static final ManifestData MANIFEST = loadRequiredResources();

    private Ae219217QuartzGlassProfile() {
    }

    /** The exact, disjoint 22-resource M3c world-render partition. */
    public static Map<String, String> requiredResources() {
        return MANIFEST.digests();
    }

    /** Uncompressed byte sizes bound into the exact M3c resource manifest. */
    public static Map<String, Long> requiredResourceSizes() {
        return MANIFEST.sizes();
    }

    public static List<String> textures() {
        return TEXTURES;
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
                    throw new IllegalStateException("malformed exact M3c resource manifest");
                }
                String path = fields[0];
                if (previousPath != null && previousPath.compareTo(path) >= 0) {
                    throw new IllegalStateException("unsorted exact M3c resource manifest");
                }
                long size;
                try {
                    size = Long.parseLong(fields[1]);
                } catch (NumberFormatException exception) {
                    throw new IllegalStateException(
                            "invalid exact M3c resource size",
                            exception
                    );
                }
                if (digests.put(path, fields[2]) != null || sizes.put(path, size) != null) {
                    throw new IllegalStateException("duplicate exact M3c resource path");
                }
                previousPath = path;
            }
        } catch (IOException exception) {
            throw new IllegalStateException("failed to parse exact M3c resource manifest", exception);
        }

        validateManifest(digests, sizes);
        return new ManifestData(
                Collections.unmodifiableMap(digests),
                Collections.unmodifiableMap(sizes)
        );
    }

    private static byte[] readManifest() {
        try (InputStream input = Ae219217QuartzGlassProfile.class.getResourceAsStream(
                RESOURCE_MANIFEST
        )) {
            if (input == null) {
                throw new IllegalStateException("missing exact M3c resource manifest");
            }
            return input.readAllBytes();
        } catch (IOException exception) {
            throw new IllegalStateException("failed to read exact M3c resource manifest", exception);
        }
    }

    private static void validateManifest(
            Map<String, String> digests,
            Map<String, Long> sizes
    ) {
        Set<String> expectedPaths = new LinkedHashSet<>();
        expectedPaths.add("assets/ae2/blockstates/quartz_glass.json");
        expectedPaths.add("assets/ae2/blockstates/quartz_vibrant_glass.json");
        expectedPaths.add("assets/ae2/models/block/quartz_glass.json");
        TEXTURES.forEach(texture -> expectedPaths.add(textureResourcePath(texture)));
        if (digests.size() != 22
                || sizes.size() != 22
                || !digests.keySet().equals(expectedPaths)
                || !sizes.keySet().equals(expectedPaths)
                || sizes.values().stream().mapToLong(Long::longValue).sum() != 4_187L) {
            throw new IllegalStateException("invalid exact M3c resource closure");
        }

        requireDigest(
                "texture manifest",
                resourceManifest(digests, sizes, true),
                TEXTURE_MANIFEST_SHA256
        );
        requireDigest(
                "canonical resource partition",
                canonicalManifest(digests, sizes, false),
                CANONICAL_RESOURCE_PARTITION_SHA256
        );
        requireDigest(
                "canonical texture partition",
                canonicalManifest(digests, sizes, true),
                CANONICAL_TEXTURE_PARTITION_SHA256
        );
    }

    private static String textureResourcePath(String texture) {
        int separator = texture.indexOf(':');
        return "assets/" + texture.substring(0, separator) + "/textures/"
                + texture.substring(separator + 1) + ".png";
    }

    private static byte[] canonicalManifest(
            Map<String, String> digests,
            Map<String, Long> sizes,
            boolean texturesOnly
    ) {
        StringBuilder result = new StringBuilder();
        digests.forEach((path, digest) -> {
            if (!texturesOnly || path.contains("/textures/")) {
                result.append(digest)
                        .append('\t')
                        .append(sizes.get(path))
                        .append('\t')
                        .append(path)
                        .append('\n');
            }
        });
        return result.toString().getBytes(StandardCharsets.UTF_8);
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
            throw new IllegalStateException("exact M3c " + label + " changed");
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
