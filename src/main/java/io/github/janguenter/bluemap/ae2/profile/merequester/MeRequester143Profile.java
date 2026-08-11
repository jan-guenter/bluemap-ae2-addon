/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.profile.merequester;

import io.github.janguenter.bluemap.ae2.profile.ExactResourceManifest;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/** Exact ATM 1.2.0 identity and resource contract for ME Requester M4 support. */
public final class MeRequester143Profile {

    public static final String PROFILE_ID = "merequester";
    public static final String MOD_ID = "merequester";
    public static final String VERSION = "1.21.1-1.4.3";
    public static final String MINECRAFT_VERSION = "1.21.1";
    public static final String NEOFORGE_VERSION = "21.1.248";
    public static final String ARTIFACT =
            "merequester-neoforge-1.21.1-1.4.3.jar";
    public static final long JAR_BYTES = 184_517L;
    public static final String JAR_SHA1 =
            "b0a801e9b7af930da5f58176156d58c26f6232b1";
    public static final String JAR_SHA256 =
            "68f3c861a802d48afeb6e3a48e8ee4f8633904340ac3f89f17493dc84490e385";
    public static final String JAR_SHA512 =
            "4411670eaea8403414c773646cb9498291e843ba995100c4fca4ab53d5ab3aa0c"
                    + "bf913921403c8af1d32b67ab940bac8c8a16a910069f6f7e2902e42e11f66b7";
    public static final String SOURCE_REPOSITORY =
            "https://github.com/AlmostReliable/merequester.git";
    public static final String SOURCE_TAG = "v1.21.1-neoforge-1.4.3";
    public static final String SOURCE_COMMIT =
            "fce75946bbb00c7a4705cb114d1423a284fb112a";
    public static final String SOURCE_CORRELATION =
            "exact-tag-and-byte-identical-release-asset";
    public static final String EXACT_REASON = "exact-atm-1.2.0-merequester-1.4.3";
    public static final String RESOURCE_MANIFEST_SHA256 =
            "87c29f3e83e7bc8013f960cdcb79898ed640762481ed579a5e80bfc5323fd4c5";
    public static final int RESOURCE_COUNT = 12;
    public static final long RESOURCE_BYTES = 9_295L;

    private static final Pattern SHA256 = Pattern.compile("[0-9a-f]{64}");
    private static final String RESOURCE_MANIFEST =
            "/bluemap-ae2/profiles/merequester/1.21.1-1.4.3/"
                    + "required-resources.tsv";
    private static final Set<String> EXPECTED_RESOURCE_PATHS = expectedResourcePaths();
    private static final ExactResourceManifest.Data MANIFEST = ExactResourceManifest.load(
            MeRequester143Profile.class,
            RESOURCE_MANIFEST,
            RESOURCE_MANIFEST_SHA256,
            EXPECTED_RESOURCE_PATHS,
            RESOURCE_COUNT,
            RESOURCE_BYTES
    );

    private MeRequester143Profile() {
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
            throw new IllegalArgumentException("unsupported ME Requester artifact");
        }
    }

    private static Set<String> expectedResourcePaths() {
        Set<String> result = new LinkedHashSet<>();
        Collections.addAll(
                result,
                "assets/merequester/blockstates/requester.json",
                "assets/merequester/models/block/requester.json",
                "assets/merequester/models/block/requester_active.json",
                "assets/merequester/models/part/requester_terminal_off.json",
                "assets/merequester/models/part/requester_terminal_on.json",
                "assets/merequester/textures/block/requester.png",
                "assets/merequester/textures/block/requester_active.png",
                "assets/merequester/textures/block/requester_active.png.mcmeta",
                "assets/merequester/textures/part/requester_terminal.png",
                "assets/merequester/textures/part/requester_terminal_bright.png",
                "assets/merequester/textures/part/requester_terminal_dark.png",
                "assets/merequester/textures/part/requester_terminal_medium.png"
        );
        if (result.size() != RESOURCE_COUNT) {
            throw new IllegalStateException("invalid exact ME Requester resource path set");
        }
        return Collections.unmodifiableSet(result);
    }
}
