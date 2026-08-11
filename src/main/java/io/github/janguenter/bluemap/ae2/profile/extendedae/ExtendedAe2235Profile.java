/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.profile.extendedae;

import io.github.janguenter.bluemap.ae2.profile.ExactResourceManifest;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Pattern;

/** Exact ATM 1.2.0 ExtendedAE identity folded over the proven M3b Drive closure. */
public final class ExtendedAe2235Profile {

    public static final String PROFILE_ID = ExtendedAe2233Profile.PROFILE_ID;
    public static final String MOD_ID = ExtendedAe2233Profile.MOD_ID;
    public static final String VERSION = ExtendedAe2235ArtifactIdentity.VERSION;
    public static final String MINECRAFT_VERSION =
            ExtendedAe2235ArtifactIdentity.MINECRAFT_VERSION;
    public static final String NEOFORGE_VERSION =
            ExtendedAe2235ArtifactIdentity.NEOFORGE_VERSION;
    public static final String EXACT_REASON = ExtendedAe2235ArtifactIdentity.EXACT_REASON;
    public static final String ARTIFACT = "ExtendedAE-1.21-2.2.35-neoforge.jar";
    public static final long JAR_BYTES = ExtendedAe2235ArtifactIdentity.JAR_BYTES;
    public static final String JAR_SHA1 = ExtendedAe2235ArtifactIdentity.JAR_SHA1;
    public static final String JAR_SHA256 = ExtendedAe2235ArtifactIdentity.JAR_SHA256;
    public static final String JAR_SHA512 = ExtendedAe2235ArtifactIdentity.JAR_SHA512;
    public static final String SOURCE_TAG = ExtendedAe2235ArtifactIdentity.SOURCE_TAG;
    public static final String SOURCE_COMMIT = ExtendedAe2235ArtifactIdentity.SOURCE_COMMIT;
    public static final String DRIVE_RESOURCE_PROOF =
            "all-15-m3b-resources-byte-identical-in-exact-2.2.35-artifact";
    public static final String DRIVE_RESOURCE_MANIFEST_SHA256 =
            ExtendedAe2233Profile.RESOURCE_MANIFEST_SHA256;
    public static final int DRIVE_RESOURCE_COUNT = 15;
    public static final long DRIVE_RESOURCE_BYTES = 13_242L;
    public static final String M5_RESOURCE_MANIFEST_SHA256 =
            "518a608d2b60eba9ed084fd66d017bcfefa1de2f090d5a9ea13a6433b76bc0b1";
    public static final int M5_RESOURCE_COUNT = 38;
    public static final int M5_BLOCKSTATE_RESOURCE_COUNT = 6;
    public static final int M5_MODEL_RESOURCE_COUNT = 13;
    public static final int M5_TEXTURE_RESOURCE_COUNT = 19;
    public static final long M5_RESOURCE_BYTES = 7_172L;
    public static final int ALL_RESOURCE_COUNT = 53;
    public static final long ALL_RESOURCE_BYTES = 20_414L;
    /** SHA-256 of the sorted combined path/size/digest rows, not a third resource file. */
    public static final String ALL_RESOURCE_CLOSURE_SHA256 =
            "9b7a212beddd3ca7e9921d7d4563dfbd452255cd6b0dad8ac0464fd8cffc4c65";

    private static final Pattern SHA256 = Pattern.compile("[0-9a-f]{64}");
    private static final String M5_RESOURCE_MANIFEST =
            "/bluemap-ae2/profiles/extendedae/1.21-2.2.35-neoforge/"
                    + "m5-required-resources.tsv";
    private static final Set<String> EXPECTED_M5_PATHS = expectedM5Paths();
    private static final ExactResourceManifest.Data M5_MANIFEST = ExactResourceManifest.load(
            ExtendedAe2235Profile.class,
            M5_RESOURCE_MANIFEST,
            M5_RESOURCE_MANIFEST_SHA256,
            EXPECTED_M5_PATHS,
            M5_RESOURCE_COUNT,
            M5_RESOURCE_BYTES
    );
    private static final Map<String, String> ALL_RESOURCES = combine(
            ExtendedAe2233Profile.requiredResources(),
            M5_MANIFEST.digests()
    );
    private static final Map<String, Long> ALL_RESOURCE_SIZES = combine(
            ExtendedAe2233Profile.requiredResourceSizes(),
            M5_MANIFEST.sizes()
    );

    static {
        if (ExtendedAe2233Profile.requiredResources().size() != DRIVE_RESOURCE_COUNT
                || ExtendedAe2233Profile.requiredResourceSizes().values().stream()
                .mapToLong(Long::longValue).sum() != DRIVE_RESOURCE_BYTES
                || ALL_RESOURCES.size() != ALL_RESOURCE_COUNT
                || ALL_RESOURCE_SIZES.values().stream().mapToLong(Long::longValue).sum()
                != ALL_RESOURCE_BYTES
                || !ALL_RESOURCE_CLOSURE_SHA256.equals(combinedClosureDigest())) {
            throw new IllegalStateException("invalid exact ExtendedAE 2.2.35 folded closure");
        }
    }

    private ExtendedAe2235Profile() {
    }

    /** The new disjoint 38-resource Assembler Matrix and plane partition. */
    public static Map<String, String> m5RequiredResources() {
        return M5_MANIFEST.digests();
    }

    public static Map<String, Long> m5RequiredResourceSizes() {
        return M5_MANIFEST.sizes();
    }

    /** The 15 byte-identical Drive resources proven inside this same 2.2.35 artifact. */
    public static Map<String, String> driveRequiredResources() {
        return ExtendedAe2233Profile.requiredResources();
    }

    public static Map<String, Long> driveRequiredResourceSizes() {
        return ExtendedAe2233Profile.requiredResourceSizes();
    }

    /** Folded current-version closure; the two exact partitions are required to be disjoint. */
    public static Map<String, String> allRequiredResources() {
        return ALL_RESOURCES;
    }

    public static Map<String, Long> allRequiredResourceSizes() {
        return ALL_RESOURCE_SIZES;
    }

    public static boolean acceptsArtifact(long bytes, String sha256) {
        return bytes == JAR_BYTES
                && sha256 != null
                && SHA256.matcher(sha256).matches()
                && JAR_SHA256.equals(sha256);
    }

    public static void requireExactArtifact(long bytes, String sha256) {
        if (!acceptsArtifact(bytes, sha256)) {
            throw new IllegalArgumentException("unsupported ExtendedAE artifact");
        }
    }

    private static Set<String> expectedM5Paths() {
        Set<String> result = new LinkedHashSet<>();
        ExtendedAe2235Catalog.matrixDefinitions().values().forEach(definition -> {
            result.add(definition.blockstateResource());
            result.addAll(definition.modelResources());
        });
        result.addAll(ExtendedAe2235Catalog.matrixTextureResources());
        ExtendedAe2235Catalog.planeDefinitions().values().forEach(definition -> {
            result.add(definition.offModelResource());
            result.add(definition.onModelResource());
            result.add(definition.offFrontResource());
            result.add(definition.onFrontResource());
            result.add(definition.onFrontMetadataResource());
        });
        long blockstates = result.stream().filter(path -> path.contains("/blockstates/"))
                .count();
        long models = result.stream().filter(path -> path.contains("/models/")).count();
        long textures = result.stream().filter(path -> path.contains("/textures/")).count();
        if (result.size() != M5_RESOURCE_COUNT
                || blockstates != M5_BLOCKSTATE_RESOURCE_COUNT
                || models != M5_MODEL_RESOURCE_COUNT
                || textures != M5_TEXTURE_RESOURCE_COUNT) {
            throw new IllegalStateException("invalid ExtendedAE M5 resource path set");
        }
        return Collections.unmodifiableSet(result);
    }

    private static <T> Map<String, T> combine(Map<String, T> first, Map<String, T> second) {
        TreeMap<String, T> sorted = new TreeMap<>(first);
        second.forEach((path, value) -> {
            if (sorted.put(path, value) != null) {
                throw new IllegalStateException("overlapping ExtendedAE resource partitions");
            }
        });
        return Collections.unmodifiableMap(new LinkedHashMap<>(sorted));
    }

    private static String combinedClosureDigest() {
        StringBuilder rows = new StringBuilder();
        ALL_RESOURCES.forEach((path, digest) -> rows.append(path)
                .append('\t')
                .append(ALL_RESOURCE_SIZES.get(path))
                .append('\t')
                .append(digest)
                .append('\n'));
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(
                    rows.toString().getBytes(StandardCharsets.UTF_8)
            );
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 unavailable", exception);
        }
    }
}
