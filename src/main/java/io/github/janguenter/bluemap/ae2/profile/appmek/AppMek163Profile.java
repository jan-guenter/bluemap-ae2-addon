/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.profile.appmek;

import io.github.janguenter.bluemap.ae2.profile.ExactResourceManifest;

import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/** Exact ATM 1.2.0 identity and native-Drive resource contract for AppMek 1.6.3. */
public final class AppMek163Profile {

    public static final String PROFILE_ID = "appmek";
    public static final String MOD_ID = "appmek";
    public static final String VERSION = "1.6.3";
    public static final String ARTIFACT = "Applied-Mekanistics-1.6.3.jar";
    public static final long JAR_BYTES = 149_709L;
    public static final String JAR_SHA1 =
            "bec4a47269ec23bca2329742e13409bfde69c5c3";
    public static final String JAR_SHA256 =
            "8946fea39451dbce8e709dedbef40a52ba337bdf7a25ac0c4b503800b1bf0773";
    public static final String JAR_SHA512 =
            "1a693c3c05862805bd88cf1265fd4b9b98b2da4efe986c9ff26efa0e675df143"
                    + "73983ffdf7ededb26f07864b6702fabc299ac1a06fb8fa35de0b341b596dbf9b";
    public static final String SOURCE_REPOSITORY =
            "https://github.com/AppliedEnergistics/Applied-Mekanistics.git";
    public static final String SOURCE_TAG = "1.6.3";
    public static final String SOURCE_COMMIT =
            "137f24bb9a46775ddd5a620055270b5e8a540f5a";
    public static final String SOURCE_CORRELATION =
            "t4-tag-commit-byte-correlated-runtime-resources";
    public static final String REQUIRED_AE2_VERSION_RANGE = "[19.2.10,20.0.0)";
    public static final String REQUIRED_MEKANISM_VERSION_RANGE = "[10.7.14,11-)";

    public static final String MEKANISM_MOD_ID = "mekanism";
    public static final String MEKANISM_VERSION = "10.7.19";
    public static final String MEKANISM_ARTIFACT = "Mekanism-1.21.1-10.7.19.85.jar";
    public static final long MEKANISM_JAR_BYTES = 11_976_009L;
    public static final String MEKANISM_JAR_SHA1 =
            "b78945c40cfe7640408f3fd1e44da385a8c8b805";
    public static final String MEKANISM_JAR_SHA256 =
            "004dbc9f3106f4d192aeaa1ee1190dd16ec9ca8059ed3d093b80034f4c574f43";
    public static final String MEKANISM_JAR_SHA512 =
            "66745825330a98f3e4a5ea3a44aff8b00870f715c144edc38dd2f61b424058960"
                    + "0b58ae89efac3b54dbb5aa430b1059dac1a28fd71cac5cc9002bbeb5ba3f22b";
    public static final String MEKANISM_SOURCE_TAG = "v1.21.1-10.7.19.85";
    public static final String MEKANISM_SOURCE_COMMIT =
            "a00109e4856fd38b9c5b3dd7f22ce4a59cd65a80";

    public static final String DRIVE_EXACT_REASON =
            "exact-atm-1.2.0-appmek-1.6.3-mekanism-10.7.19-drive-cells";
    public static final int DRIVE_RESOURCE_COUNT = 6;
    public static final long DRIVE_RESOURCE_BYTES = 3_611L;

    /** Versioned semantic form used for resolved, parent-applied model gates. */
    public static final String MODEL_SEMANTIC_ALGORITHM =
            "bluemap-ae2-resolved-model-elements-faces-v1";
    /** Versioned semantic form used for decoded texture gates. */
    public static final String TEXTURE_SEMANTIC_ALGORITHM =
            "bluemap-ae2-decoded-texture-key-size-animation-argb-v1";
    public static final String DRIVE_TEXTURE = "appmek:block/drive/drive_cells";
    public static final String DRIVE_TEXTURE_SEMANTIC_SHA256 =
            "cea8a99feaebd750fd6e2e0c1cb2d23e973dfe98717664a8458f998d1d910c03";
    public static final Map<String, String> DRIVE_MODEL_SEMANTIC_SHA256 = Map.of(
            "appmek:block/drive/cells/chemical_storage_cell_1k",
            "03de2e1576548b970a9f7198585343f856e8f1adc2e024f4027fe2a3ccf7d573",
            "appmek:block/drive/cells/chemical_storage_cell_4k",
            "28399315b2edef915600234f5a4044144fa2bc6c112cdff3446efc302723a710",
            "appmek:block/drive/cells/chemical_storage_cell_16k",
            "a58174fbf0bd325cf16cdd58c8258633afa091c6440a503a9511ae4c61f7ed55",
            "appmek:block/drive/cells/chemical_storage_cell_64k",
            "96d1fbadad1da33257f53c743fba966eea5521508b48d4472e8755892dd66fb7",
            "appmek:block/drive/cells/chemical_storage_cell_256k",
            "2e52758406d987b21f79f626418ba187c7f9e052158fd7a12d0fcb2c8d2f8d84"
    );

    private static final Pattern SHA256 = Pattern.compile("[0-9a-f]{64}");
    private static final ExactResourceManifest.Data DRIVE = ExactResourceManifest.load(
            AppMek163Profile.class,
            "/bluemap-ae2/profiles/appmek/1.6.3/drive-required-resources.tsv",
            "eb224e499a4f5dc4762447f06defb782159c55abca43df00170e1bde65e8b1f2",
            Set.of(
                    "assets/appmek/models/block/drive/cells/chemical_storage_cell_1k.json",
                    "assets/appmek/models/block/drive/cells/chemical_storage_cell_4k.json",
                    "assets/appmek/models/block/drive/cells/chemical_storage_cell_16k.json",
                    "assets/appmek/models/block/drive/cells/chemical_storage_cell_64k.json",
                    "assets/appmek/models/block/drive/cells/chemical_storage_cell_256k.json",
                    "assets/appmek/textures/block/drive/drive_cells.png"
            ),
            DRIVE_RESOURCE_COUNT,
            DRIVE_RESOURCE_BYTES
    );

    private AppMek163Profile() {
    }

    public static Map<String, String> driveRequiredResources() {
        return DRIVE.digests();
    }

    public static Map<String, Long> driveRequiredResourceSizes() {
        return DRIVE.sizes();
    }

    public static boolean acceptsAppMekArtifact(long bytes, String sha256) {
        return accepts(bytes, sha256, JAR_BYTES, JAR_SHA256);
    }

    public static boolean acceptsMekanismArtifact(long bytes, String sha256) {
        return accepts(bytes, sha256, MEKANISM_JAR_BYTES, MEKANISM_JAR_SHA256);
    }

    private static boolean accepts(
            long bytes,
            String sha256,
            long expectedBytes,
            String expectedSha256
    ) {
        return bytes == expectedBytes
                && sha256 != null
                && SHA256.matcher(sha256).matches()
                && expectedSha256.equals(sha256);
    }
}
