/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.profile.appflux;

import io.github.janguenter.bluemap.ae2.profile.ExactResourceManifest;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

/** Exact ATM 1.2.0 identity and resource contract for AppliedFlux M4 support. */
public final class AppFlux215Profile {

    public static final String PROFILE_ID = "appflux";
    public static final String MOD_ID = "appflux";
    public static final String VERSION = "1.21-2.1.5-neoforge";
    public static final String MINECRAFT_VERSION = "1.21.1";
    public static final String NEOFORGE_VERSION = "21.1.248";
    public static final String ARTIFACT = "AppliedFlux-1.21-2.1.5-neoforge.jar";
    public static final long JAR_BYTES = 345_117L;
    public static final String JAR_SHA1 =
            "a98eeadf414e6b3f6878324a3fbdee3fa5fcdadf";
    public static final String JAR_SHA256 =
            "57e6a2c0f38e660c9e8416f9081d8c515f5ad096d6793d7b7f039e8e210d245b";
    public static final String JAR_SHA512 =
            "27bb367ad2f6695a485e11bf6e9567a86eb2d817194f5bdb381d1e493c345e00"
                    + "55eefb727308ca28fd27cf170d4303788c22681c349065515841cfe7a8c4b01b";
    public static final String SOURCE_REPOSITORY =
            "https://github.com/GlodBlock/ExtendedAE.git";
    public static final String SOURCE_BRANCH = "appflux/1.21.1-neoforge";
    public static final String SOURCE_COMMIT =
            "474bd48230de391bca29b0bfd9d6bd5410c4ec79";
    public static final String SOURCE_CORRELATION =
            "official-version-branch-reference-not-immutable";
    public static final String EXACT_REASON = "exact-atm-1.2.0-appflux-2.1.5";
    public static final String RESOURCE_MANIFEST_SHA256 =
            "64e9a03558a0c281b7ad7620826fca1a765d0f4d6d49f160e017f046999eb351";
    public static final int RESOURCE_COUNT = 19;
    public static final long RESOURCE_BYTES = 10_540L;

    private static final Pattern SHA256 = Pattern.compile("[0-9a-f]{64}");
    private static final String RESOURCE_MANIFEST =
            "/bluemap-ae2/profiles/appflux/1.21-2.1.5-neoforge/"
                    + "required-resources.tsv";
    private static final Set<String> EXPECTED_RESOURCE_PATHS = expectedResourcePaths();
    private static final ExactResourceManifest.Data MANIFEST = ExactResourceManifest.load(
            AppFlux215Profile.class,
            RESOURCE_MANIFEST,
            RESOURCE_MANIFEST_SHA256,
            EXPECTED_RESOURCE_PATHS,
            RESOURCE_COUNT,
            RESOURCE_BYTES
    );

    private AppFlux215Profile() {
    }

    public static Map<String, String> requiredResources() {
        return MANIFEST.digests();
    }

    public static Map<String, Long> requiredResourceSizes() {
        return MANIFEST.sizes();
    }

    /** Returns true only for the exact ATM 1.2.0 artifact identity. */
    public static boolean acceptsArtifact(long bytes, String sha256) {
        return bytes == JAR_BYTES
                && sha256 != null
                && SHA256.matcher(sha256).matches()
                && JAR_SHA256.equals(sha256);
    }

    /** Fails closed instead of accepting a same-version or similarly named artifact. */
    public static void requireExactArtifact(long bytes, String sha256) {
        if (!acceptsArtifact(bytes, sha256)) {
            throw new IllegalArgumentException("unsupported AppliedFlux artifact");
        }
    }

    private static Set<String> expectedResourcePaths() {
        Set<String> result = new LinkedHashSet<>();
        AppFlux215Catalog.stockBlockModels().forEach((blockId, modelId) -> {
            result.add(blockstatePath(blockId));
            result.add(modelPath(modelId));
        });
        AppFlux215Catalog.driveCellModels().values().stream()
                .distinct()
                .map(AppFlux215Profile::modelPath)
                .forEach(result::add);
        result.add(modelPath(AppFlux215Catalog.FLUX_ACCESSOR_PART_MODEL));
        Collections.addAll(
                result,
                "assets/appflux/textures/block/charged_redstone_block.png",
                "assets/appflux/textures/block/drive/fe_cell.png",
                "assets/appflux/textures/block/flux_accessor.png",
                "assets/appflux/textures/part/flux_accessor.png"
        );
        if (result.size() != RESOURCE_COUNT) {
            throw new IllegalStateException("invalid exact AppliedFlux resource path set");
        }
        return Collections.unmodifiableSet(result);
    }

    private static String blockstatePath(String blockId) {
        Objects.requireNonNull(blockId, "blockId");
        return "assets/appflux/blockstates/" + blockId.substring("appflux:".length())
                + ".json";
    }

    private static String modelPath(String modelId) {
        Objects.requireNonNull(modelId, "modelId");
        return "assets/appflux/models/" + modelId.substring("appflux:".length())
                + ".json";
    }
}
