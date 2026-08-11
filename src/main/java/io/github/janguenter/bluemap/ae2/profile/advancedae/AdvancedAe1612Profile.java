/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.profile.advancedae;

import io.github.janguenter.bluemap.ae2.profile.ExactResourceManifest;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/** Exact ATM 1.2.0 Advanced AE identity and quantum-computer resource closure. */
public final class AdvancedAe1612Profile {

    public static final String PROFILE_ID = "advanced-ae-quantum";
    public static final String MOD_ID = "advanced_ae";
    public static final String VERSION = "1.6.12-1.21.1";
    public static final String MINECRAFT_VERSION = "1.21.1";
    public static final String NEOFORGE_VERSION = "21.1.248";
    public static final String ARTIFACT = "AdvancedAE-1.6.12-1.21.1.jar";
    public static final long JAR_BYTES = 4_791_255L;
    public static final String JAR_SHA1 = "9358ccfa5477c7ab1c5ffab6c831e105fe46ecc3";
    public static final String JAR_SHA256 =
            "a01d9718667ac13899013e91c5b0b7708b9b9db1da9b8e380772dde54bbe8f41";
    public static final String JAR_SHA512 =
            "ab61c57355649a967a0bcf6b9413cd6b62728d26e914543b3231eea33bde5571"
                    + "536bd589ae1ac026d46799711508c942284c3419e19ff5d5bf80f1045442f33a";
    public static final String EXACT_REASON = "exact-atm-1.2.0-advanced-ae-1.6.12";
    public static final String SOURCE_CORRELATION =
            "exact-jar-authoritative-no-artifact-correlated-immutable-source";
    public static final String RESOURCE_MANIFEST_SHA256 =
            "727e64ee6da127b637e7ae7ee8b99a0bcc87eb047b2f0b15467be6a4fc9c800f";
    public static final int RESOURCE_COUNT = 48;
    public static final int BLOCKSTATE_RESOURCE_COUNT = 8;
    public static final int MODEL_RESOURCE_COUNT = 16;
    public static final int TEXTURE_RESOURCE_COUNT = 24;
    public static final long RESOURCE_BYTES = 24_053L;

    private static final Pattern SHA256 = Pattern.compile("[0-9a-f]{64}");
    private static final String RESOURCE_MANIFEST =
            "/bluemap-ae2/profiles/advancedae/1.6.12/quantum-required-resources.tsv";
    private static final Set<String> EXPECTED_RESOURCE_PATHS = expectedResourcePaths();
    private static final ExactResourceManifest.Data MANIFEST = ExactResourceManifest.load(
            AdvancedAe1612Profile.class,
            RESOURCE_MANIFEST,
            RESOURCE_MANIFEST_SHA256,
            EXPECTED_RESOURCE_PATHS,
            RESOURCE_COUNT,
            RESOURCE_BYTES
    );

    private AdvancedAe1612Profile() {
    }

    public static Map<String, String> requiredResources() {
        return MANIFEST.digests();
    }

    public static Map<String, Long> requiredResourceSizes() {
        return MANIFEST.sizes();
    }

    public static boolean acceptsArtifact(long bytes, String sha256) {
        return bytes == JAR_BYTES
                && sha256 != null
                && SHA256.matcher(sha256).matches()
                && JAR_SHA256.equals(sha256);
    }

    public static void requireExactArtifact(long bytes, String sha256) {
        if (!acceptsArtifact(bytes, sha256)) {
            throw new IllegalArgumentException("unsupported Advanced AE artifact");
        }
    }

    private static Set<String> expectedResourcePaths() {
        Set<String> result = new LinkedHashSet<>();
        AdvancedAe1612Catalog.quantumDefinitions().values().forEach(definition -> {
            result.add(definition.blockstateResource());
            result.addAll(definition.modelResources());
        });
        result.addAll(AdvancedAe1612Catalog.quantumTextureResources());
        long blockstates = result.stream().filter(path -> path.contains("/blockstates/"))
                .count();
        long models = result.stream().filter(path -> path.contains("/models/")).count();
        long textures = result.stream().filter(path -> path.contains("/textures/")).count();
        if (result.size() != RESOURCE_COUNT
                || blockstates != BLOCKSTATE_RESOURCE_COUNT
                || models != MODEL_RESOURCE_COUNT
                || textures != TEXTURE_RESOURCE_COUNT) {
            throw new IllegalStateException("invalid Advanced AE quantum resource path set");
        }
        return Collections.unmodifiableSet(result);
    }
}
