/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.profile.extendedae;

import io.github.janguenter.bluemap.ae2.profile.Ae219217Profile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Evidence-locked identity and resource contract for ExtendedAE 2.2.33's Ex Drive. */
public final class ExtendedAe2233Profile {

    public static final String PROFILE_ID = "extendedae";
    public static final String MOD_ID = "extendedae";
    public static final String VERSION = "1.21-2.2.33-neoforge";
    public static final String MINECRAFT_VERSION = "1.21.1";
    public static final String NEOFORGE_VERSION = "21.1.234";
    public static final String EXACT_REASON = "exact-1.21-2.2.33-neoforge";
    public static final String JAR_SHA1 = "e87867bffee36a28f9f4493f7bb7e7a5109a480f";
    public static final String JAR_SHA256 =
            "6652ed1ea4b71f585d48c05a195a77594a7a2bd1ecea0fc805db2122aafad734";
    public static final String JAR_SHA512 =
            "a61c6f606b5d0a27857b55b8fc6a670352d91f19d2a2dadd2d650f08ae6682f4"
                    + "37e7b18a80c5e26122bbf7b70b007851f1aaa90442fce16d8729c71c1ec10225";
    public static final long JAR_BYTES = 5_573_972L;

    public static final String BLOCK = "extendedae:ex_drive";
    public static final String SYNTHETIC_BLOCK_STATE = "bluemap_ae2:extendedae_ex_drive";
    public static final String BASE_MODEL =
            "extendedae:block/extended_drive/extended_me_drive_base";
    public static final String EMPTY_CELL_MODEL = Ae219217Profile.DRIVE_EMPTY_CELL_MODEL;
    public static final String GENERIC_CELL_MODEL = Ae219217Profile.DRIVE_GENERIC_CELL_MODEL;
    /** BlueMap material proxy for the source renderer's textureless POSITION_COLOR LEDs. */
    public static final String LED_TEXTURE = "ae2:block/drive/drive_front";
    public static final String LED_POLICY = "static-offline-unknown";
    public static final String UNKNOWN_CELL_POLICY =
            "atomic-whole-block-original-resource-fallback";
    public static final int SLOT_COUNT = 20;
    public static final int SIDE_SLOT_COUNT = 10;
    public static final int SLOT_ROWS = 5;
    public static final int SLOT_COLUMNS = 2;

    /** SHA-256 of sorted {@code path<TAB>bytes<TAB>sha256<LF>} rows. */
    public static final String RESOURCE_MANIFEST_SHA256 =
            "5e72f79f45a3b120a89cf8b7a1fa15ce41bebaae62a63c6f3305ef40bd5d24ee";
    public static final String DEPENDENT_AE2_RESOURCE_MANIFEST_SHA256 =
            "a8d10416d0fce66d8a91ce9e0dc93a83d2f552da8762a0a90e183dc58f6745cf";

    private static final String RESOURCE_MANIFEST =
            "/bluemap-ae2/profiles/extendedae/1.21-2.2.33-neoforge/"
                    + "required-resources.tsv";
    private static final List<String> TEXTURES = List.of(
            "extendedae:block/drive/infinity_cell",
            "extendedae:block/drive/void_cell",
            "extendedae:block/extended_drive/drive_inside",
            "extendedae:block/extended_drive/ex_drive_bottom",
            "extendedae:block/extended_drive/ex_drive_side",
            "extendedae:block/extended_drive/ex_drive_top",
            "extendedae:block/generics/front",
            "extendedae:block/generics/side"
    );
    private static final Map<String, String> BUILT_IN_CELL_MODELS = Map.of(
            "extendedae:infinity_water_cell",
            "extendedae:block/drive/infinity_water_cell",
            "extendedae:infinity_cobblestone_cell",
            "extendedae:block/drive/infinity_cobblestone_cell",
            "extendedae:void_cell",
            "extendedae:block/drive/void_cell"
    );
    private static final ManifestData MANIFEST = loadRequiredResources();
    private static final Map<String, String> SUPPORTED_CELL_MODELS = buildSupportedCellModels();
    private static final Set<String> SUPPORTED_ITEM_IDS =
            Collections.unmodifiableSet(new LinkedHashSet<>(SUPPORTED_CELL_MODELS.keySet()));

    private ExtendedAe2233Profile() {
    }

    /** The disjoint, exact 15-resource ExtendedAE partition. */
    public static Map<String, String> requiredResources() {
        return MANIFEST.digests();
    }

    /** Byte sizes bound into the same exact 15-resource manifest. */
    public static Map<String, Long> requiredResourceSizes() {
        return MANIFEST.sizes();
    }

    public static List<String> textures() {
        return TEXTURES;
    }

    /** The already pinned AE2 M3a Drive partition required by the effective route. */
    public static Map<String, String> dependentAe2RequiredResources() {
        return Ae219217Profile.driveRequiredResources();
    }

    public static List<String> dependentAe2Textures() {
        return Ae219217Profile.driveTextures();
    }

    public static Map<String, String> builtInCellModels() {
        return BUILT_IN_CELL_MODELS;
    }

    /** Closed 23 native-AE2 plus three built-in ExtendedAE item-to-model catalog. */
    public static Map<String, String> supportedCellModels() {
        return SUPPORTED_CELL_MODELS;
    }

    public static Set<String> supportedItemIds() {
        return SUPPORTED_ITEM_IDS;
    }

    private static Map<String, String> buildSupportedCellModels() {
        Map<String, String> models = new LinkedHashMap<>(Ae219217Profile.driveCellModels());
        BUILT_IN_CELL_MODELS.forEach((itemId, model) -> {
            if (models.put(itemId, model) != null) {
                throw new IllegalStateException("overlapping exact ExtendedAE cell item ID");
            }
        });
        if (models.size() != 26 || Set.copyOf(models.values()).size() != 15) {
            throw new IllegalStateException("invalid exact ExtendedAE supported cell catalog");
        }
        return Collections.unmodifiableMap(models);
    }

    private static ManifestData loadRequiredResources() {
        InputStream input = ExtendedAe2233Profile.class.getResourceAsStream(RESOURCE_MANIFEST);
        if (input == null) {
            throw new IllegalStateException("missing exact ExtendedAE resource manifest");
        }
        Map<String, String> digests = new LinkedHashMap<>();
        Map<String, Long> sizes = new LinkedHashMap<>();
        String previousPath = null;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                input,
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
                    throw new IllegalStateException(
                            "malformed exact ExtendedAE resource manifest"
                    );
                }
                String path = fields[0];
                if (previousPath != null && previousPath.compareTo(path) >= 0) {
                    throw new IllegalStateException(
                            "unsorted exact ExtendedAE resource manifest"
                    );
                }
                long size;
                try {
                    size = Long.parseLong(fields[1]);
                } catch (NumberFormatException exception) {
                    throw new IllegalStateException(
                            "invalid exact ExtendedAE resource size",
                            exception
                    );
                }
                if (digests.put(path, fields[2]) != null || sizes.put(path, size) != null) {
                    throw new IllegalStateException(
                            "duplicate exact ExtendedAE resource manifest path"
                    );
                }
                previousPath = path;
            }
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "failed to read exact ExtendedAE resource manifest",
                    exception
            );
        }
        if (digests.size() != 15 || sizes.size() != 15) {
            throw new IllegalStateException("invalid exact ExtendedAE resource count");
        }
        return new ManifestData(
                Collections.unmodifiableMap(digests),
                Collections.unmodifiableMap(sizes)
        );
    }

    private record ManifestData(Map<String, String> digests, Map<String, Long> sizes) {
    }
}
