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

/** Evidence-locked AE2 19.2.17 constants for the disjoint M3e quantum route. */
public final class Ae219217QuantumBridgeProfile {

    public static final String PROFILE_ID = "ae2-quantum-bridge";
    public static final String MOD_ID = "ae2";
    public static final String VERSION = "19.2.17";
    public static final String MINECRAFT_VERSION = "1.21.1";
    public static final String NEOFORGE_VERSION = "21.1.234";
    public static final String EXACT_REASON = "exact-19.2.17-formed-quantum-bridge";
    public static final String JAR_SHA1 = "49c18d6a4af487957d7e5a6ad5dcbf71090b8e14";
    public static final String JAR_SHA256 =
            "460d779a0609b81409907d9956de8f6f70a1b0912257e3e5c3c7e75ac9630e95";
    public static final long JAR_BYTES = 8_230_896L;
    public static final String SOURCE_COMMIT =
            "79ee2c704ad62941a426c26b1cb1f76ef5b2ee5a";
    public static final String SOURCES_JAR_SHA256 =
            "d2f451203cb61c2d21fae52c683083d2f72441ca7d26725f4df5934290492e6a";
    public static final long SOURCES_JAR_BYTES = 3_814_167L;

    public static final String QUANTUM_LINK_BLOCK = "ae2:quantum_link";
    public static final String QUANTUM_RING_BLOCK = "ae2:quantum_ring";
    public static final Set<String> BLOCKS = Collections.unmodifiableSet(
            new LinkedHashSet<>(List.of(QUANTUM_LINK_BLOCK, QUANTUM_RING_BLOCK))
    );
    public static final String SYNTHETIC_BLOCK_STATE = "bluemap_ae2:quantum_bridge";
    public static final String BLOCK_ENTITY_ID = "ae2:quantum_ring";

    public static final String QUANTUM_LINK_TEXTURE = "ae2:block/quantum_link";
    public static final String QUANTUM_RING_TEXTURE = "ae2:block/quantum_ring";
    public static final String QUANTUM_RING_LIGHT_TEXTURE =
            "ae2:block/quantum_ring_light";
    public static final String QUANTUM_RING_LIGHT_CORNER_TEXTURE =
            "ae2:block/quantum_ring_light_corner";
    public static final String GLASS_TRANSPARENT_TEXTURE =
            "ae2:part/cable/glass/transparent";
    public static final String COVERED_TRANSPARENT_TEXTURE =
            "ae2:part/cable/covered/transparent";

    public static final String RESOURCE_MANIFEST_SHA256 =
            "717eed1ada75fb43c1324792c147cd8c2308d8c73ee82bf52d8de6bad4f74ed9";
    public static final String SOURCE_TEXTURE_MANIFEST_SHA256 =
            "47afa14a8397a0adba9f92663cd2ae08776fc2f0abec6361e5e728cfdba110ae";
    public static final String EMITTED_OFF_TEXTURE_MANIFEST_SHA256 =
            "2905881b9f5ad2f0ac8fc84c825c2d659779f710a27dae9270ce6d741b5e4cdc";

    private static final String RESOURCE_MANIFEST =
            "/bluemap-ae2/profiles/ae2/19.2.17/routes/quantum-bridge/"
                    + "required-resources.tsv";
    private static final List<String> SOURCE_TEXTURES = List.of(
            QUANTUM_LINK_TEXTURE,
            QUANTUM_RING_TEXTURE,
            QUANTUM_RING_LIGHT_TEXTURE,
            QUANTUM_RING_LIGHT_CORNER_TEXTURE,
            GLASS_TRANSPARENT_TEXTURE,
            COVERED_TRANSPARENT_TEXTURE
    );
    private static final List<String> EMITTED_OFF_TEXTURES = List.of(
            QUANTUM_LINK_TEXTURE,
            QUANTUM_RING_TEXTURE,
            GLASS_TRANSPARENT_TEXTURE,
            COVERED_TRANSPARENT_TEXTURE
    );
    private static final ManifestData MANIFEST = loadRequiredResources();

    private Ae219217QuantumBridgeProfile() {
    }

    /** The exact 13-resource M3e source closure, including two shared cable sprites. */
    public static Map<String, String> requiredResources() {
        return MANIFEST.digests();
    }

    /** Uncompressed byte sizes bound into the exact M3e resource manifest. */
    public static Map<String, Long> requiredResourceSizes() {
        return MANIFEST.sizes();
    }

    /** All six source sprites audited for the exact client dynamic model. */
    public static List<String> sourceTextures() {
        return SOURCE_TEXTURES;
    }

    /** The four sprites emitted by the static-off, power-unknown projection. */
    public static List<String> emittedOffTextures() {
        return EMITTED_OFF_TEXTURES;
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
                    throw new IllegalStateException("malformed exact M3e resource manifest");
                }
                String path = fields[0];
                if (previousPath != null && previousPath.compareTo(path) >= 0) {
                    throw new IllegalStateException("unsorted exact M3e resource manifest");
                }
                long size;
                try {
                    size = Long.parseLong(fields[1]);
                } catch (NumberFormatException exception) {
                    throw new IllegalStateException(
                            "invalid exact M3e resource size",
                            exception
                    );
                }
                if (digests.put(path, fields[2]) != null || sizes.put(path, size) != null) {
                    throw new IllegalStateException("duplicate exact M3e resource path");
                }
                previousPath = path;
            }
        } catch (IOException exception) {
            throw new IllegalStateException("failed to parse exact M3e resource manifest", exception);
        }

        validateManifest(digests, sizes);
        return new ManifestData(
                Collections.unmodifiableMap(digests),
                Collections.unmodifiableMap(sizes)
        );
    }

    private static byte[] readManifest() {
        try (InputStream input = Ae219217QuantumBridgeProfile.class.getResourceAsStream(
                RESOURCE_MANIFEST
        )) {
            if (input == null) {
                throw new IllegalStateException("missing exact M3e resource manifest");
            }
            return input.readAllBytes();
        } catch (IOException exception) {
            throw new IllegalStateException("failed to read exact M3e resource manifest", exception);
        }
    }

    private static void validateManifest(
            Map<String, String> digests,
            Map<String, Long> sizes
    ) {
        Set<String> expectedPaths = new LinkedHashSet<>(List.of(
                "assets/ae2/blockstates/quantum_link.json",
                "assets/ae2/blockstates/quantum_ring.json",
                "assets/ae2/models/block/qnb/link.json",
                "assets/ae2/models/block/qnb/qnb_formed.json",
                "assets/ae2/models/block/qnb/ring.json",
                "assets/ae2/textures/block/quantum_link.png",
                "assets/ae2/textures/block/quantum_ring.png",
                "assets/ae2/textures/block/quantum_ring_light.png",
                "assets/ae2/textures/block/quantum_ring_light.png.mcmeta",
                "assets/ae2/textures/block/quantum_ring_light_corner.png",
                "assets/ae2/textures/block/quantum_ring_light_corner.png.mcmeta",
                "assets/ae2/textures/part/cable/covered/transparent.png",
                "assets/ae2/textures/part/cable/glass/transparent.png"
        ));
        if (digests.size() != 13
                || sizes.size() != 13
                || !digests.keySet().equals(expectedPaths)
                || !sizes.keySet().equals(expectedPaths)
                || sizes.values().stream().mapToLong(Long::longValue).sum() != 3_798L) {
            throw new IllegalStateException("invalid exact M3e resource closure");
        }

        requireDigest(
                "source texture manifest",
                textureManifest(digests, sizes, SOURCE_TEXTURES),
                SOURCE_TEXTURE_MANIFEST_SHA256
        );
        requireDigest(
                "emitted-off texture manifest",
                textureManifest(digests, sizes, EMITTED_OFF_TEXTURES),
                EMITTED_OFF_TEXTURE_MANIFEST_SHA256
        );
    }

    private static byte[] textureManifest(
            Map<String, String> digests,
            Map<String, Long> sizes,
            List<String> textures
    ) {
        Set<String> paths = new LinkedHashSet<>();
        textures.stream().map(Ae219217QuantumBridgeProfile::textureResourcePath)
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
            throw new IllegalStateException("exact M3e " + label + " changed");
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
