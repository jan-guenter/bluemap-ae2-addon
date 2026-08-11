/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.profile.expandedae;

import io.github.janguenter.bluemap.ae2.profile.ExactResourceManifest;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/** Exact ATM 1.2.0 identity and visual-resource contract for Expanded AE M4. */
public final class ExpandedAe211Profile {

    public static final String PROFILE_ID = "expandedae";
    public static final String MOD_ID = "expandedae";
    public static final String VERSION = "2.1.1";
    public static final String MINECRAFT_VERSION = "1.21.1";
    public static final String NEOFORGE_VERSION = "21.1.248";
    public static final String ARTIFACT = "expandedae-2.1.1.jar";
    public static final long JAR_BYTES = 496_713L;
    public static final String JAR_SHA1 =
            "c4db013f83e569b016da329b3ddc9c14acc75d7d";
    public static final String JAR_SHA256 =
            "f39c0eb9c6271f54a44ffee092a29520f53000d1005849e6afada3ad9dffba14";
    public static final String JAR_SHA512 =
            "5d6b0c7430d6f1f2bdb2cb38832ee27d0b28402d16171a9fe746d0275ba54c28"
                    + "8405b64b9ad269c010aadd729e82ddeb61b9550c0361c6e1ece2c0bdc77a4b23";
    public static final String SOURCE_CORRELATION =
            "exact-jar-bytecode-only-no-matching-immutable-source-tag";
    public static final String EXACT_REASON = "exact-atm-1.2.0-expandedae-2.1.1";
    public static final String RESOURCE_MANIFEST_SHA256 =
            "4b144ee2867c96e09b6c1872e3123b218f243df427c5b535714156c6194e1501";
    public static final int RESOURCE_COUNT = 142;
    public static final int BLOCKSTATE_RESOURCE_COUNT = 23;
    public static final int MODEL_RESOURCE_COUNT = 49;
    public static final int TEXTURE_RESOURCE_COUNT = 70;
    public static final long RESOURCE_BYTES = 46_365L;

    private static final Pattern SHA256 = Pattern.compile("[0-9a-f]{64}");
    private static final String RESOURCE_MANIFEST =
            "/bluemap-ae2/profiles/expandedae/2.1.1/required-resources.tsv";
    private static final Set<String> EXPECTED_RESOURCE_PATHS = expectedResourcePaths();
    private static final ExactResourceManifest.Data MANIFEST = ExactResourceManifest.load(
            ExpandedAe211Profile.class,
            RESOURCE_MANIFEST,
            RESOURCE_MANIFEST_SHA256,
            EXPECTED_RESOURCE_PATHS,
            RESOURCE_COUNT,
            RESOURCE_BYTES
    );

    private ExpandedAe211Profile() {
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
            throw new IllegalArgumentException("unsupported Expanded AE artifact");
        }
    }

    private static Set<String> expectedResourcePaths() {
        Set<String> result = new LinkedHashSet<>();
        ExpandedAe211Catalog.craftingDefinitions().values().forEach(definition -> {
            result.add(blockstatePath(definition.blockId()));
            result.add(modelPath(definition.unformedModel()));
            result.add(modelPath(definition.formedModel()));
            result.add(texturePath(definition.unformedTexture()));
        });
        Collections.addAll(
                result,
                "assets/expandedae/blockstates/exp_io_port.json",
                "assets/expandedae/blockstates/exp_pattern_provider.json",
                "assets/expandedae/models/block/exp_io_port.json",
                "assets/expandedae/models/block/exp_io_port_on.json",
                "assets/expandedae/models/block/exp_pattern_provider_all.json",
                "assets/expandedae/models/block/exp_pattern_provider_oriented.json",
                "assets/expandedae/models/part/exp_encoding_terminal_off.json",
                "assets/expandedae/models/part/exp_encoding_terminal_on.json",
                "assets/expandedae/models/part/exp_pattern_provider_base.json"
        );
        ExpandedAe211Catalog.dynamicCraftingTextures().stream()
                .map(ExpandedAe211Profile::texturePath)
                .forEach(result::add);
        Collections.addAll(
                result,
                "assets/expandedae/textures/block/crafting/"
                        + "exp_crafting_accelerator_1m.png.mcmeta",
                "assets/expandedae/textures/block/crafting/"
                        + "exp_crafting_accelerator_1m_light.png.mcmeta",
                "assets/expandedae/textures/block/exp_io_port_front.png",
                "assets/expandedae/textures/block/exp_io_port_front_off.png",
                "assets/expandedae/textures/block/exp_io_port_side.png",
                "assets/expandedae/textures/block/exp_io_port_side_off.png",
                "assets/expandedae/textures/block/exp_io_port_top.png",
                "assets/expandedae/textures/block/exp_io_port_top_off.png",
                "assets/expandedae/textures/block/exp_pattern_provider.png",
                "assets/expandedae/textures/block/exp_pattern_provider_alternate.png",
                "assets/expandedae/textures/block/exp_pattern_provider_alternate_arrow.png",
                "assets/expandedae/textures/block/exp_pattern_provider_alternate_front.png",
                "assets/expandedae/textures/block/generics/back.png",
                "assets/expandedae/textures/block/generics/bottom.png",
                "assets/expandedae/textures/block/generics/front.png",
                "assets/expandedae/textures/block/generics/side.png",
                "assets/expandedae/textures/block/generics/top.png",
                "assets/expandedae/textures/part/exp_encoding_terminal.png",
                "assets/expandedae/textures/part/exp_encoding_terminal_bright.png",
                "assets/expandedae/textures/part/exp_encoding_terminal_dark.png",
                "assets/expandedae/textures/part/exp_encoding_terminal_medium.png",
                "assets/expandedae/textures/part/exp_pattern_provider.png",
                "assets/expandedae/textures/part/exp_pattern_provider_back.png",
                "assets/expandedae/textures/part/exp_pattern_provider_sides.png"
        );
        validateExpectedResourcePaths(result);
        return Collections.unmodifiableSet(result);
    }

    private static void validateExpectedResourcePaths(Set<String> paths) {
        long blockstates = paths.stream().filter(path -> path.contains("/blockstates/")).count();
        long models = paths.stream().filter(path -> path.contains("/models/")).count();
        long textures = paths.stream().filter(path -> path.contains("/textures/")).count();
        if (paths.size() != RESOURCE_COUNT
                || blockstates != BLOCKSTATE_RESOURCE_COUNT
                || models != MODEL_RESOURCE_COUNT
                || textures != TEXTURE_RESOURCE_COUNT) {
            throw new IllegalStateException("invalid exact Expanded AE resource path set");
        }
    }

    private static String blockstatePath(String blockId) {
        return "assets/expandedae/blockstates/"
                + blockId.substring("expandedae:".length()) + ".json";
    }

    private static String modelPath(String modelId) {
        return "assets/expandedae/models/"
                + modelId.substring("expandedae:".length()) + ".json";
    }

    private static String texturePath(String textureId) {
        return "assets/expandedae/textures/"
                + textureId.substring("expandedae:".length()) + ".png";
    }
}
