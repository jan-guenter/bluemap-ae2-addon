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

/** Evidence-locked AE2 19.2.17 catalog for native cable-bus structural rendering. */
public final class Ae219217NativeStructuralProfile {

    public static final int SCHEMA_VERSION = 10;
    public static final String PROFILE_ID = "ae2-cable-bus-structural";
    public static final String MOD_ID = "ae2";
    public static final String VERSION = "19.2.17";
    public static final String PACK_NAME = "All the Mons";
    public static final String PACK_VERSION = "1.2.0";
    public static final String PACK_COMMIT =
            "c7bb230f21d14d26859d0b92548f089b3a493ad9";
    public static final String MINECRAFT_VERSION = "1.21.1";
    public static final String NEOFORGE_VERSION = "21.1.248";
    public static final String RESOURCE_ROOT =
            "/bluemap-ae2/profiles/ae2/19.2.17/routes/cable-bus-structural/";
    public static final String JAR_SHA256 =
            "460d779a0609b81409907d9956de8f6f70a1b0912257e3e5c3c7e75ac9630e95";
    public static final long JAR_BYTES = 8_230_896L;
    public static final String SOURCE_COMMIT =
            "79ee2c704ad62941a426c26b1cb1f76ef5b2ee5a";
    public static final String SOURCES_JAR_SHA256 =
            "d2f451203cb61c2d21fae52c683083d2f72441ca7d26725f4df5934290492e6a";

    public static final int FACE_COUNT = 6;
    public static final int SPIN_COUNT = 4;
    public static final int MAX_FACADE_SCAN = 6;
    public static final int MAX_PART_SLOTS = 6;
    public static final int FACADE_MASK_COUNT = 64;
    public static final int FACADE_WHITELIST_BLOCK_COUNT = 24;
    public static final int FACADE_WHITELIST_OPTIONAL_TAG_COUNT = 1;
    public static final int FACADE_WHITELIST_NEUTRAL_STATE_COUNT = 24;
    public static final int FACADE_WHITELIST_STATE_SCHEMA_COUNT = 24;
    public static final int FACADE_WHITELIST_STATE_COMBINATION_COUNT = 554;
    public static final int FACADE_WHITELIST_SOLID_RENDER_TRUE_STATE_COUNT = 551;
    public static final int
            FACADE_WHITELIST_SAME_STATE_SKIP_RENDERING_TRUE_STATE_COUNT = 3;
    public static final int FACADE_WHITELIST_SAME_STATE_SKIP_RENDERING_COUNT = 24;
    public static final int FACADE_ORDINARY_SKIP_RENDERING_CONTROL_COUNT = 3;
    public static final int NATIVE_FACADE_NEUTRAL_MATERIAL_COUNT = 11;
    public static final int QUARTZ_FACADE_DEPENDENCY_TEXTURE_COUNT = 19;
    public static final int FACE_PART_COUNT = 29;
    public static final int SPIN_CAPABLE_PART_COUNT = 9;
    public static final int SMART_CORE_PART_COUNT = 2;
    public static final int ORIENTATION_STATE_COUNT = 336;
    public static final int PLANE_CONNECTION_MASK_COUNT = 16;
    public static final int NATIVE_ENDPOINT_COUNT = 30;
    public static final int ENDPOINT_STATE_SCHEMA_COUNT = 30;
    public static final int ENDPOINT_STATE_COMBINATION_COUNT = 534;
    public static final int ENDPOINT_STATE_SIDE_COMBINATION_COUNT = 3_204;
    public static final int KNOWN_UNSUPPORTED_COMPATIBLE_ENDPOINT_COUNT = 67;
    public static final int ENDPOINT_SIDE_RULE_KIND_COUNT = 8;
    public static final int SMART_ENDPOINT_COUNT = 18;
    public static final int COVERED_ENDPOINT_COUNT = 9;
    public static final int DENSE_SMART_ENDPOINT_COUNT = 3;
    public static final int DIRECT_NEUTRAL_RESOURCE_COUNT = 41;
    public static final int TRANSITIVE_JSON_RESOURCE_COUNT = 43;
    public static final int PNG_RESOURCE_COUNT = 56;
    public static final int REQUIRED_RESOURCE_COUNT = 99;
    public static final long REQUIRED_RESOURCE_BYTES = 51_306L;

    public static final String TRANSIENT_POLICY = "static-off-inactive-unlocked";
    public static final String FACADE_POLICY =
            "all-six-face-masks-per-instance-valid-static-block-state-material";
    public static final String FALLBACK_POLICY =
            "missing-malformed-or-capped-atomic-original-resource-fallback";
    public static final String FACADE_DIRECTIONAL_SHADE_POLICY =
            "source-shade-bit-semantic-locked-host-prbm-has-no-per-quad-shade-channel";
    public static final String FACADE_AMBIENT_OCCLUSION_DIRECTION_POLICY =
            "BlueMap-ResourceModelRenderer-source-faceDir-rotated-by-blockstate-variant-" +
                    "only;element-rotation-affects-vertices-not-AO-direction;runtime-uses-" +
                    "layer-lightFace-not-quad-nominal-face";
    public static final String STRUCTURAL_MAP_COLOR_ILLUMINATION_POLICY =
            "BlueMap-map-color-illumination-uses-original-center-and-outward-world-light-" +
                    "only;element-lightEmission-affects-triangle-blocklight-not-map-color-" +
                    "brightness";
    public static final String FACADE_SUPPORT_SET_POLICY =
            "all-24-explicit-whitelist-families-plus-bounded-static-full-cube-witness-" +
                    "material-lane";
    public static final String FACADE_ORDINARY_MATERIAL_POLICY =
            "optional-c-glass-blocks-and-ordinary-FacadeItem-eligible-states-require-" +
                    "one-live-unrotated-0-to-16-six-face-full-cube-witness;bounded-additional-" +
                    "static-elements-and-multipart-source-quads-subject-to-uv-tint-weighted-" +
                    "and-semantic-resource-gates;otherwise-valid-complex-static-models-" +
                    "atomic-original-resource-fallback";
    public static final String FACADE_TINT_POLICY =
            "untinted-or-one-distinct-nonnegative-source-tint-index;" +
                    "untinted-layers-may-coexist;shared-tinted-layers-use-host-block-color-" +
                    "calculator;mixed-nonnegative-tint-indices-atomic-original-resource-fallback";
    public static final String FACADE_UV_REINTERPOLATION_POLICY =
            "source-QuadReInterpolator-nominal-face-2d-dx-dy-bilinear;admitted-quad-" +
                    "projection-requires-exact-complete-InterpHelper-grid;post-clamp-and-corner-" +
                    "kick-target-uses-projected-dx-dy;noncompatible-projected-quads-atomic-" +
                    "original-resource-fallback";
    public static final String FACADE_CARDINAL_VARIANT_TRANSFORM_POLICY =
            "exact-signed-permutation-quarter-turn-blockstate-variant-and-uvlock-" +
                    "coordinate-transforms;avoids-host-float-matrix-drift-before-source-" +
                    "exact-InterpHelper-grid";
    public static final String FACADE_WEIGHTED_VARIANT_POLICY =
            "exact-minecraft-stone-four-alternative-geometry-and-material-host-position-" +
                    "projection-retains-frozen-M2-non-pixel-identical-randomized-uv-boundary;" +
                    "all-other-weighted-sets-require-every-alternative-collapse-to-one-bounded-" +
                    "static-geometry-material-uv-descriptor;otherwise-atomic-original-resource-" +
                    "fallback";
    public static final String FACADE_SKIP_RENDERING_POLICY =
            "exact-24-explicit-whitelist-same-state-table;ae2-quartz-glass-cross-family-" +
                    "render-shape-rule;exact-gallery-controls-glass-true-oak-log-false-oak-" +
                    "leaves-false;other-ordinary-tag-materials-use-bounded-BlueMap-" +
                    "cullingIdentical-same-state-host-projection";
    public static final String FACADE_WHITELIST_STATE_POLICY =
            "all-24-explicit-whitelist-families-require-exact-complete-persisted-" +
                    "property-key-set-and-value-domains;13-vanilla-families-preserve-valid-" +
                    "state;11-ae2-native-families-apply-declared-static-normalization;extra-" +
                    "missing-or-invalid-properties-atomic-original-resource-fallback";
    public static final String FACADE_WHITELIST_STATE_CLASSIFICATION_POLICY =
            "solidRender-and-same-state-skipRendering-family-invariant-across-all-554-" +
                    "valid-explicit-whitelist-states;neutral-default-row-booleans-apply-to-" +
                    "whole-family;classification-drift-atomic-original-resource-fallback";
    public static final String FACADE_QUARTZ_SKIP_RENDERING_POLICY =
            "true-for-any-two-ae2-QuartzGlassBlock-families-with-equal-render-shape";
    public static final String FACADE_CUTOUT_STRIP_AABB_POLICY =
            "minecraft-AABB-normalizes-each-generated-strip-endpoint-pair-with-min-max;" +
                    "transparent-inset-plus-boundary-reaching-cutout-may-reverse-endpoints-and-" +
                    "must-produce-the-normalized-strip-not-a-degenerate-strip";
    public static final double FACADE_CORNER_KICK_SOURCE_EPSILON_BLOCKS = 0.00001D;
    public static final double FACADE_CORNER_KICK_RUNTIME_EPSILON_SIXTEENTHS = 0.00016D;
    public static final double FACADE_CORNER_KICK_ANALYZER_EPSILON_BLOCKS = 0.00001D;
    public static final String NATIVE_FACADE_NEUTRAL_SCOPE =
            "11-ae2-native-neutral-resource-pins-not-the-complete-facade-support-set";
    public static final String KNOWN_UNSUPPORTED_COMPATIBLE_ENDPOINT_POLICY =
            "unknown-atomic-original-resource-fallback";
    public static final String MALFORMED_NATIVE_ENDPOINT_POLICY =
            "malformed-native-endpoint-atomic-original-resource-fallback";
    public static final String MALFORMED_KNOWN_EXTENSION_ENDPOINT_POLICY =
            "malformed-known-extension-observation-atomic-original-resource-fallback";
    public static final String PLANE_COORDINATE_SPACE_POLICY =
            "mask-bits-are-PlaneConnections-front-view-logical;" +
                    "renderedGeometryBoundBits-are-PlaneBakedModel-visual-local-before-" +
                    "QuadRotator-installed-world-transform;collisionBoundBitsByInstalledFace-" +
                    "are-BusCollisionHelper-installed-face-local;never-reuse-bounds-across-" +
                    "coordinate-spaces";
    public static final String PART_COLLISION_UNIT = "sixteenths";
    public static final String FACADE_CUTOUT_POLICY =
            "union-of-every-installed-part-box-intersecting-current-facade-slab";
    public static final String FACADE_WHITELIST_RESOURCE_SHA256 =
            "4ff52f9d8670417406c29430f754305198ba8ab855ca34336962d6d24cf49f82";
    public static final String RESOURCE_MANIFEST_SHA256 =
            "ae89e4fc3356503cc76ea92ac9cb11ade296551c9cca85cd583ffddbbe35bd76";
    public static final String RESOURCE_SIZES_MANIFEST_SHA256 =
            "a79e93baef3f5d923730686fcc4de05ec30c8b7765aef8b32aaf871f9c4f3869";
    public static final String LIVE_MODEL_SEMANTIC_SIGNATURE_SHA256 =
            "aefa42ad8427e8f2ac5b9f1c88807c978617d6ff70768a32223616b970b54251";
    public static final String LIVE_TEXTURE_SEMANTIC_SIGNATURE_SHA256 =
            "1bee2b2917edf3d1eb9ee24505f47a7377665da753f107ec1af9170d783bc833";
    public static final String QUARTZ_FACADE_DEPENDENCY_PROFILE_ID =
            "ae2-quartz-glass";
    public static final String QUARTZ_FACADE_DEPENDENCY_PROFILE_SHA256 =
            "548e5bc00ef07c6d6b93b346422b596882ec11ca03de006065fa45fecb991200";
    public static final String QUARTZ_FACADE_DEPENDENCY_RESOURCE_MANIFEST_SHA256 =
            "b51c708e7c4d26093c1b6f85b88d0be50572d3cfa76dbf802720f6ad79c7a7fa";
    public static final String QUARTZ_FACADE_DEPENDENCY_TEXTURE_MANIFEST_SHA256 =
            "65005c9b76800cdeba5c4598472a44dea131c9974672f89bf421452755fefb6a";
    public static final String QUARTZ_FACADE_DEPENDENCY_TEXTURE_SEMANTIC_SIGNATURE_SHA256 =
            "c51ced2667879b8b298400c81805cf7d4459b5ac88c36350bca7bb6ca2bfef50";
    public static final String LIVE_MODEL_SEMANTIC_SIGNATURE_ALGORITHM =
            "resolved-parent-applied-elements-faces-ao-shade-light-uv-texture-cull-" +
                    "rotation-tint-float-bits-sha256-v1";
    public static final String LIVE_TEXTURE_SEMANTIC_SIGNATURE_ALGORITHM =
            "decoded-width-height-argb-scanline-animation-meta-sha256-v1";
    public static final String PROFILE_SHA256 =
            "f6fa515b4e17205a019d57f253d5e71017ea20e75b8f0c333aa587afd0d0f353";

    public static final List<String> NATIVE_FACE_PART_IDS = List.of(
            "ae2:quartz_fiber",
            "ae2:toggle_bus",
            "ae2:inverted_toggle_bus",
            "ae2:cable_anchor",
            "ae2:monitor",
            "ae2:semi_dark_monitor",
            "ae2:dark_monitor",
            "ae2:storage_bus",
            "ae2:import_bus",
            "ae2:export_bus",
            "ae2:level_emitter",
            "ae2:energy_level_emitter",
            "ae2:annihilation_plane",
            "ae2:formation_plane",
            "ae2:pattern_encoding_terminal",
            "ae2:crafting_terminal",
            "ae2:terminal",
            "ae2:storage_monitor",
            "ae2:conversion_monitor",
            "ae2:cable_pattern_provider",
            "ae2:cable_interface",
            "ae2:pattern_access_terminal",
            "ae2:cable_energy_acceptor",
            "ae2:me_p2p_tunnel",
            "ae2:redstone_p2p_tunnel",
            "ae2:item_p2p_tunnel",
            "ae2:fluid_p2p_tunnel",
            "ae2:fe_p2p_tunnel",
            "ae2:light_p2p_tunnel"
    );

    public static final Set<String> SPIN_CAPABLE_PART_IDS = Set.of(
            "ae2:monitor",
            "ae2:semi_dark_monitor",
            "ae2:dark_monitor",
            "ae2:pattern_encoding_terminal",
            "ae2:crafting_terminal",
            "ae2:terminal",
            "ae2:storage_monitor",
            "ae2:conversion_monitor",
            "ae2:pattern_access_terminal"
    );

    public static final Set<String> DENSE_CAPABLE_PART_IDS = Set.of("ae2:cable_anchor");

    public static final Set<String> SMART_CORE_PART_IDS = Set.of(
            "ae2:level_emitter",
            "ae2:energy_level_emitter"
    );

    public static final List<String> FACADE_WHITELIST_BLOCK_IDS = List.of(
            "ae2:quartz_glass",
            "ae2:quartz_vibrant_glass",
            "minecraft:chiseled_bookshelf",
            "minecraft:jukebox",
            "minecraft:furnace",
            "minecraft:blast_furnace",
            "minecraft:dropper",
            "minecraft:dispenser",
            "minecraft:crafter",
            "minecraft:barrel",
            "minecraft:bee_nest",
            "minecraft:beehive",
            "minecraft:sculk_catalyst",
            "minecraft:soul_sand",
            "minecraft:honey_block",
            "ae2:controller",
            "ae2:1k_crafting_storage",
            "ae2:4k_crafting_storage",
            "ae2:16k_crafting_storage",
            "ae2:64k_crafting_storage",
            "ae2:256k_crafting_storage",
            "ae2:crafting_monitor",
            "ae2:crafting_unit",
            "ae2:crafting_accelerator"
    );
    public static final List<String> FACADE_WHITELIST_OPTIONAL_TAGS =
            List.of("c:glass_blocks");
    public static final Map<String, NeutralFacadeMaterial>
            NATIVE_FACADE_NEUTRAL_MATERIALS = buildNativeFacadeNeutralMaterials();
    public static final Map<String, FacadeNeutralState>
            FACADE_WHITELIST_NEUTRAL_STATES = buildFacadeWhitelistNeutralStates();
    public static final Map<String, Map<String, List<String>>>
            FACADE_WHITELIST_STATE_SCHEMAS = buildFacadeWhitelistStateSchemas();
    public static final Map<String, String> FACADE_WHITELIST_BLOCKSTATE_SHA256 =
            buildFacadeWhitelistBlockstateSha256();
    public static final Map<String, Boolean>
            FACADE_WHITELIST_SAME_STATE_SKIP_RENDERING =
                    buildFacadeWhitelistSameStateSkipRendering();
    public static final Map<String, Boolean> FACADE_ORDINARY_SKIP_RENDERING_CONTROLS =
            buildFacadeOrdinarySkipRenderingControls();

    public static final Map<String, Integer> PLANE_CONNECTION_MASK_BITS =
            buildPlaneConnectionMaskBits();
    public static final Map<String, Map<String, String>>
            PLANE_COLLISION_BOUND_BITS_BY_FACE = buildPlaneCollisionBoundBitsByFace();
    public static final Map<String, String> PLANE_RENDER_BOUND_BITS = Map.of(
            "minX", "right",
            "maxX", "left",
            "minY", "down",
            "maxY", "up"
    );
    public static final Map<String, List<PartBox>> NATIVE_FACE_PART_COLLISION_BOXES =
            buildNativeFacePartCollisionBoxes();
    public static final Map<String, String> NATIVE_FACE_PART_COLLISION_MODES =
            buildNativeFacePartCollisionModes();

    public static final List<String> NATIVE_ENDPOINT_IDS = List.of(
            "ae2:inscriber",
            "ae2:wireless_access_point",
            "ae2:charger",
            "ae2:quantum_ring",
            "ae2:quantum_link",
            "ae2:spatial_pylon",
            "ae2:spatial_io_port",
            "ae2:spatial_anchor",
            "ae2:controller",
            "ae2:drive",
            "ae2:chest",
            "ae2:interface",
            "ae2:io_port",
            "ae2:energy_acceptor",
            "ae2:crystal_resonance_generator",
            "ae2:vibration_chamber",
            "ae2:growth_accelerator",
            "ae2:energy_cell",
            "ae2:dense_energy_cell",
            "ae2:creative_energy_cell",
            "ae2:crafting_unit",
            "ae2:crafting_accelerator",
            "ae2:1k_crafting_storage",
            "ae2:4k_crafting_storage",
            "ae2:16k_crafting_storage",
            "ae2:64k_crafting_storage",
            "ae2:256k_crafting_storage",
            "ae2:crafting_monitor",
            "ae2:pattern_provider",
            "ae2:molecular_assembler"
    );

    public static final List<String> KNOWN_UNSUPPORTED_COMPATIBLE_ENDPOINT_IDS =
            buildKnownUnsupportedCompatibleEndpointIds();

    public static final Map<String, List<String>> NATIVE_FACE_PART_MODELS =
            buildNativeFacePartModels();
    public static final Map<String, String> NATIVE_FACE_PART_GROUPS =
            nativeFacePartGroups();
    public static final Map<String, String> NATIVE_ENDPOINT_CABLE_TYPES =
            buildNativeEndpointCableTypes();
    public static final Map<String, String> NATIVE_ENDPOINT_CLASSES =
            nativeEndpointClasses();
    public static final Map<String, String> NATIVE_ENDPOINT_BLOCK_ENTITY_IDS =
            buildNativeEndpointBlockEntityIds();
    public static final Map<String, String> NATIVE_ENDPOINT_SIDE_RULES =
            buildNativeEndpointSideRules();
    public static final Map<String, Map<String, List<String>>> NATIVE_ENDPOINT_STATE_SCHEMAS =
            buildNativeEndpointStateSchemas();
    public static final Map<String, String> NATIVE_ENDPOINT_BLOCKSTATE_SHA256 =
            buildNativeEndpointBlockstateSha256();
    public static final Map<String, Integer> NATIVE_ENDPOINT_SIDE_RULE_COUNTS =
            buildNativeEndpointSideRuleCounts();
    public static final Map<String, String>
            KNOWN_UNSUPPORTED_COMPATIBLE_ENDPOINT_BLOCK_ENTITY_IDS =
            buildKnownUnsupportedCompatibleEndpointBlockEntityIds();
    public static final Map<String, Integer>
            KNOWN_UNSUPPORTED_COMPATIBLE_ENDPOINT_ARTIFACT_COUNTS = Map.of(
                    "expandedae-2.1.1", 24,
                    "megacells-4.11.0", 11,
                    "advanced_ae-1.6.12-1.21.1", 12,
                    "extendedae-1.21-2.2.35-neoforge", 20
            );

    private static final String CHECKSUM_MANIFEST =
            RESOURCE_ROOT + "required-resources.sha256";
    private static final String SIZE_MANIFEST = RESOURCE_ROOT + "required-resources.tsv";
    private static final ManifestData MANIFEST = loadRequiredResources();

    private Ae219217NativeStructuralProfile() {
    }

    /** Ordered AEParts catalog for every native non-center face part. */
    public static List<String> nativeFacePartIds() {
        return NATIVE_FACE_PART_IDS;
    }

    /** Ordered native block endpoint catalog expanded to concrete registry IDs. */
    public static List<String> nativeEndpointIds() {
        return NATIVE_ENDPOINT_IDS;
    }

    /** Closed exact extension grid-node hosts that force UNKNOWN until M4/M5. */
    public static List<String> knownUnsupportedCompatibleEndpointIds() {
        return KNOWN_UNSUPPORTED_COMPATIBLE_ENDPOINT_IDS;
    }

    /** The sole face part that AE2 permits on dense center cables. */
    public static Set<String> denseCapablePartIds() {
        return DENSE_CAPABLE_PART_IDS;
    }

    /** The nine AbstractReportingPart descendants with persisted byte spin. */
    public static Set<String> spinCapablePartIds() {
        return SPIN_CAPABLE_PART_IDS;
    }

    /** The two level emitters whose desired SMART connection promotes a glass core. */
    public static Set<String> smartCorePartIds() {
        return SMART_CORE_PART_IDS;
    }

    /** Exact AE2 explicit facade eligibility exceptions in generated-tag order. */
    public static List<String> facadeWhitelistBlockIds() {
        return FACADE_WHITELIST_BLOCK_IDS;
    }

    public static List<String> facadeWhitelistOptionalTags() {
        return FACADE_WHITELIST_OPTIONAL_TAGS;
    }

    /** Eleven native whitelisted families pinned to the accepted neutral policy. */
    public static Map<String, NeutralFacadeMaterial> nativeFacadeNeutralMaterials() {
        return NATIVE_FACADE_NEUTRAL_MATERIALS;
    }

    /** Exact neutral/default state and Minecraft isSolidRender for all 24 explicit IDs. */
    public static Map<String, FacadeNeutralState> facadeWhitelistNeutralStates() {
        return FACADE_WHITELIST_NEUTRAL_STATES;
    }

    /** Complete persisted BlockState key/value domains for all 24 explicit IDs. */
    public static Map<String, Map<String, List<String>>> facadeWhitelistStateSchemas() {
        return FACADE_WHITELIST_STATE_SCHEMAS;
    }

    /** Returns null for a block outside the exact explicit-whitelist table. */
    public static Map<String, List<String>> facadeWhitelistStateSchema(String blockId) {
        return FACADE_WHITELIST_STATE_SCHEMAS.get(blockId);
    }

    /** Exact pinned blockstate-resource digest backing every explicit schema. */
    public static Map<String, String> facadeWhitelistBlockstateSha256() {
        return FACADE_WHITELIST_BLOCKSTATE_SHA256;
    }

    /** Returns null for a block outside the exact explicit-whitelist table. */
    public static String facadeWhitelistBlockstateSha256(String blockId) {
        return FACADE_WHITELIST_BLOCKSTATE_SHA256.get(blockId);
    }

    /** Exact same-state BlockState.skipRendering result for all 24 explicit IDs. */
    public static Map<String, Boolean> facadeWhitelistSameStateSkipRendering() {
        return FACADE_WHITELIST_SAME_STATE_SKIP_RENDERING;
    }

    /** Returns null for a block outside the exact explicit-whitelist table. */
    public static Boolean facadeWhitelistSameStateSkipRendering(String blockId) {
        return FACADE_WHITELIST_SAME_STATE_SKIP_RENDERING.get(blockId);
    }

    /** Exact gallery controls outside the explicit whitelist. */
    public static Map<String, Boolean> facadeOrdinarySkipRenderingControls() {
        return FACADE_ORDINARY_SKIP_RENDERING_CONTROLS;
    }

    /** Returns null for an ordinary/tag material outside the exact control set. */
    public static Boolean facadeOrdinarySkipRenderingControl(String blockId) {
        return FACADE_ORDINARY_SKIP_RENDERING_CONTROLS.get(blockId);
    }

    /** Logical front-view PlaneConnections bits: left=1, down=2, right=4 and up=8. */
    public static Map<String, Integer> planeConnectionMaskBits() {
        return PLANE_CONNECTION_MASK_BITS;
    }

    /** BusCollisionHelper installed-face-local bounds keyed by exact lowercase face. */
    public static Map<String, Map<String, String>> planeCollisionBoundBitsByFace() {
        return PLANE_COLLISION_BOUND_BITS_BY_FACE;
    }

    /** PlaneBakedModel visual-local bounds before QuadRotator's installed-world transform. */
    public static Map<String, String> planeRenderBoundBits() {
        return PLANE_RENDER_BOUND_BITS;
    }

    /** Canonical collision boxes in installed-face-local sixteenths. */
    public static Map<String, List<PartBox>> nativeFacePartCollisionBoxes() {
        return NATIVE_FACE_PART_COLLISION_BOXES;
    }

    /** Static, facade-conditioned or plane-mask collision policy per ordered part. */
    public static Map<String, String> nativeFacePartCollisionModes() {
        return NATIVE_FACE_PART_COLLISION_MODES;
    }

    /** Canonical boxes; cable-anchor and plane callers must use their dynamic helpers. */
    public static List<PartBox> facePartCollisionBoxes(String id) {
        return NATIVE_FACE_PART_COLLISION_BOXES.get(id);
    }

    public static List<PartBox> cableAnchorCollisionBoxes(boolean sameSideFacade) {
        return sameSideFacade
                ? boxes(7, 7, 10, 9, 9, 14)
                : boxes(7, 7, 10, 9, 9, 16);
    }

    public static List<PartBox> planeCollisionBoxes(String installedFace, int mask) {
        Map<String, String> boundBits = PLANE_COLLISION_BOUND_BITS_BY_FACE.get(installedFace);
        if (boundBits == null) {
            throw new IllegalArgumentException(
                    "plane installed face must be exact lowercase down/up/north/south/west/east"
            );
        }
        if (mask < 0 || mask >= PLANE_CONNECTION_MASK_COUNT) {
            throw new IllegalArgumentException("plane connection mask must be in [0, 15]");
        }
        int minX = planeCollisionBoundExtends(boundBits, "minX", mask) ? 0 : 1;
        int minY = planeCollisionBoundExtends(boundBits, "minY", mask) ? 0 : 1;
        int maxX = planeCollisionBoundExtends(boundBits, "maxX", mask) ? 16 : 15;
        int maxY = planeCollisionBoundExtends(boundBits, "maxY", mask) ? 16 : 15;
        return List.of(
                new PartBox(5, 5, 14, 11, 11, 15),
                new PartBox(minX, minY, 15, maxX, maxY, 16)
        );
    }

    /** Exact static off/inactive/unlocked model layers by native face-part ID. */
    public static Map<String, List<String>> nativeFacePartModels() {
        return NATIVE_FACE_PART_MODELS;
    }

    /** Exact source-derived endpoint AECableType names by native block ID. */
    public static Map<String, String> nativeEndpointCableTypes() {
        return NATIVE_ENDPOINT_CABLE_TYPES;
    }

    /** Exact serialized block-entity registry ID required for each endpoint block. */
    public static Map<String, String> nativeEndpointBlockEntityIds() {
        return NATIVE_ENDPOINT_BLOCK_ENTITY_IDS;
    }

    /** Exact persisted-side/state/topology rule name for each endpoint block. */
    public static Map<String, String> nativeEndpointSideRules() {
        return NATIVE_ENDPOINT_SIDE_RULES;
    }

    /** Complete persisted BlockState key/value domains for all 30 native endpoints. */
    public static Map<String, Map<String, List<String>>> nativeEndpointStateSchemas() {
        return NATIVE_ENDPOINT_STATE_SCHEMAS;
    }

    /** Exact runtime blockstate-resource digest backing each endpoint schema audit. */
    public static Map<String, String> nativeEndpointBlockstateSha256() {
        return NATIVE_ENDPOINT_BLOCKSTATE_SHA256;
    }

    /** Frozen totals for the eight endpoint side-rule kinds. */
    public static Map<String, Integer> nativeEndpointSideRuleCounts() {
        return NATIVE_ENDPOINT_SIDE_RULE_COUNTS;
    }

    /** Exact serialized BE IDs for known pinned extension grid-node hosts. */
    public static Map<String, String> knownUnsupportedCompatibleEndpointBlockEntityIds() {
        return KNOWN_UNSUPPORTED_COMPATIBLE_ENDPOINT_BLOCK_ENTITY_IDS;
    }

    /** Exact 99-resource model/texture closure supplied by the installed AE2 JAR. */
    public static Map<String, String> requiredResources() {
        return MANIFEST.digests();
    }

    /** Uncompressed byte sizes bound by the independent deterministic TSV. */
    public static Map<String, Long> requiredResourceSizes() {
        return MANIFEST.sizes();
    }

    public static boolean isNativeFacePart(String id) {
        return NATIVE_FACE_PART_MODELS.containsKey(id);
    }

    public static boolean isNativeEndpoint(String id) {
        return NATIVE_ENDPOINT_CABLE_TYPES.containsKey(id);
    }

    public static boolean isKnownUnsupportedCompatibleEndpoint(String id) {
        return KNOWN_UNSUPPORTED_COMPATIBLE_ENDPOINT_BLOCK_ENTITY_IDS.containsKey(id);
    }

    public static boolean supportsDenseCenter(String id) {
        return DENSE_CAPABLE_PART_IDS.contains(id);
    }

    public static boolean supportsSpin(String id) {
        return SPIN_CAPABLE_PART_IDS.contains(id);
    }

    public static boolean requestsSmartCore(String id) {
        return SMART_CORE_PART_IDS.contains(id);
    }

    public static String endpointCableType(String id) {
        return NATIVE_ENDPOINT_CABLE_TYPES.get(id);
    }

    public static String endpointBlockEntityId(String id) {
        return NATIVE_ENDPOINT_BLOCK_ENTITY_IDS.get(id);
    }

    public static String endpointSideRule(String id) {
        return NATIVE_ENDPOINT_SIDE_RULES.get(id);
    }

    public static Map<String, List<String>> endpointStateSchema(String id) {
        return NATIVE_ENDPOINT_STATE_SCHEMAS.get(id);
    }

    public static String knownUnsupportedCompatibleEndpointBlockEntityId(String id) {
        return KNOWN_UNSUPPORTED_COMPATIBLE_ENDPOINT_BLOCK_ENTITY_IDS.get(id);
    }

    /** UNKNOWN branch for exact pinned extension grid-node hosts only. */
    public static String knownUnsupportedCompatibleEndpointPolicy(String id) {
        return isKnownUnsupportedCompatibleEndpoint(id)
                ? KNOWN_UNSUPPORTED_COMPATIBLE_ENDPOINT_POLICY : null;
    }

    /** Atomic fallback branch for malformed observations of native endpoints. */
    public static String malformedNativeEndpointPolicy(String id) {
        return isNativeEndpoint(id) ? MALFORMED_NATIVE_ENDPOINT_POLICY : null;
    }

    private static Map<String, List<String>> buildNativeFacePartModels() {
        Map<String, List<String>> models = new LinkedHashMap<>();
        putModels(models, "ae2:quartz_fiber", "ae2:part/quartz_fiber");
        putModels(models, "ae2:toggle_bus", "ae2:part/toggle_bus_base",
                "ae2:part/toggle_bus_status_off");
        putModels(models, "ae2:inverted_toggle_bus", "ae2:part/inverted_toggle_bus_base",
                "ae2:part/toggle_bus_status_off");
        putModels(models, "ae2:cable_anchor", "ae2:part/cable_anchor",
                "ae2:part/cable_anchor_short");
        putModels(models, "ae2:monitor", "ae2:part/monitor_base",
                "ae2:part/monitor_bright_off");
        putModels(models, "ae2:semi_dark_monitor", "ae2:part/monitor_base",
                "ae2:part/monitor_medium_off");
        putModels(models, "ae2:dark_monitor", "ae2:part/monitor_base",
                "ae2:part/monitor_dark_off");
        putModels(models, "ae2:storage_bus", "ae2:part/storage_bus_base",
                "ae2:part/storage_bus_off");
        putModels(models, "ae2:import_bus", "ae2:part/import_bus_base",
                "ae2:part/import_bus_off");
        putModels(models, "ae2:export_bus", "ae2:part/export_bus_base",
                "ae2:part/export_bus_off");
        putModels(models, "ae2:level_emitter", "ae2:part/level_emitter_base_off",
                "ae2:part/level_emitter_status_off");
        putModels(models, "ae2:energy_level_emitter", "ae2:part/level_emitter_base_off",
                "ae2:part/level_emitter_status_off");
        putModels(models, "ae2:annihilation_plane", "ae2:part/transition_plane_off",
                "ae2:part/annihilation_plane");
        putModels(models, "ae2:formation_plane", "ae2:part/transition_plane_off",
                "ae2:part/formation_plane");
        putModels(models, "ae2:pattern_encoding_terminal", "ae2:part/display_base",
                "ae2:part/display_status_off", "ae2:part/pattern_encoding_terminal_off");
        putModels(models, "ae2:crafting_terminal", "ae2:part/display_base",
                "ae2:part/display_status_off", "ae2:part/crafting_terminal_off");
        putModels(models, "ae2:terminal", "ae2:part/display_base",
                "ae2:part/display_status_off", "ae2:part/terminal_off");
        putModels(models, "ae2:storage_monitor", "ae2:part/display_base",
                "ae2:part/display_status_off", "ae2:part/storage_monitor_off");
        putModels(models, "ae2:conversion_monitor", "ae2:part/display_base",
                "ae2:part/display_status_off", "ae2:part/conversion_monitor_off");
        putModels(models, "ae2:cable_pattern_provider", "ae2:part/pattern_provider_base",
                "ae2:part/interface_off");
        putModels(models, "ae2:cable_interface", "ae2:part/interface_base",
                "ae2:part/interface_off");
        putModels(models, "ae2:pattern_access_terminal", "ae2:part/display_base",
                "ae2:part/display_status_off", "ae2:part/pattern_access_terminal_off");
        putModels(models, "ae2:cable_energy_acceptor", "ae2:part/energy_acceptor");
        putP2pModels(models, "ae2:me_p2p_tunnel", "ae2:part/p2p/p2p_tunnel_me");
        putP2pModels(models, "ae2:redstone_p2p_tunnel",
                "ae2:part/p2p/p2p_tunnel_redstone");
        putP2pModels(models, "ae2:item_p2p_tunnel", "ae2:part/p2p/p2p_tunnel_items");
        putP2pModels(models, "ae2:fluid_p2p_tunnel", "ae2:part/p2p/p2p_tunnel_fluids");
        putP2pModels(models, "ae2:fe_p2p_tunnel", "ae2:part/p2p/p2p_tunnel_fe");
        putP2pModels(models, "ae2:light_p2p_tunnel", "ae2:part/p2p/p2p_tunnel_light");
        return Collections.unmodifiableMap(models);
    }

    private static Map<String, Integer> buildPlaneConnectionMaskBits() {
        Map<String, Integer> bits = new LinkedHashMap<>();
        bits.put("left", 1);
        bits.put("down", 2);
        bits.put("right", 4);
        bits.put("up", 8);
        return Collections.unmodifiableMap(bits);
    }

    private static Map<String, Map<String, String>> buildPlaneCollisionBoundBitsByFace() {
        Map<String, Map<String, String>> values = new LinkedHashMap<>();
        values.put("down", planeCollisionBoundBits("right", "left", "down", "up"));
        values.put("up", planeCollisionBoundBits("left", "right", "up", "down"));
        values.put("north", planeCollisionBoundBits("left", "right", "down", "up"));
        values.put("south", planeCollisionBoundBits("left", "right", "down", "up"));
        values.put("west", planeCollisionBoundBits("right", "left", "down", "up"));
        values.put("east", planeCollisionBoundBits("right", "left", "down", "up"));
        return Collections.unmodifiableMap(values);
    }

    private static Map<String, String> planeCollisionBoundBits(
            String minX,
            String maxX,
            String minY,
            String maxY
    ) {
        Map<String, String> values = new LinkedHashMap<>();
        values.put("minX", minX);
        values.put("maxX", maxX);
        values.put("minY", minY);
        values.put("maxY", maxY);
        return Collections.unmodifiableMap(values);
    }

    private static boolean planeCollisionBoundExtends(
            Map<String, String> boundBits,
            String bound,
            int mask
    ) {
        Integer bit = PLANE_CONNECTION_MASK_BITS.get(boundBits.get(bound));
        if (bit == null) {
            throw new IllegalStateException("invalid generated plane collision bound table");
        }
        return (mask & bit) != 0;
    }

    private static Map<String, NeutralFacadeMaterial> buildNativeFacadeNeutralMaterials() {
        Map<String, NeutralFacadeMaterial> values = new LinkedHashMap<>();
        putNativeFacadeMaterial(values, "ae2:quartz_glass", Map.of(),
                "facade-aware-connected-quartz-glass-static", "ae2:block/quartz_glass",
                "9c331aa0f423a364e136b731195caf168df6496a90a065f9699e5e8e37e70d50",
                false, 0, 0);
        putNativeFacadeMaterial(values, "ae2:quartz_vibrant_glass", Map.of(),
                "facade-aware-connected-quartz-glass-static", "ae2:block/quartz_glass",
                "e3b2b20544e578ff4b9d908ca1e7d281ecc46ddd8f0ee496ad53e2e344e17a99",
                false, 15, 0);
        putNativeFacadeMaterial(values, "ae2:controller",
                Map.of("state", "offline", "type", "block"),
                "controller-offline-block", "ae2:block/controller/controller_block_offline",
                "693d04c733b47e4159052d0843256fa7520bbc1984b6d9e454bec976a73d2ca8",
                true, 0, 0);
        putNativeFacadeMaterial(values, "ae2:1k_crafting_storage",
                Map.of("formed", "false", "powered", "false"),
                "crafting-storage-1k-unformed", "ae2:block/crafting/1k_storage",
                "9a1f6383cd3b54a8361cefc46740ddbee587ce79baefccb6ad6de6355833a603",
                true, 0, 0);
        putNativeFacadeMaterial(values, "ae2:4k_crafting_storage",
                Map.of("formed", "false", "powered", "false"),
                "crafting-storage-4k-unformed", "ae2:block/crafting/4k_storage",
                "dd4210a4c0fc5b0eb7f524571f20b7e1a92c438bc68df7324cb26c939c726abc",
                true, 0, 0);
        putNativeFacadeMaterial(values, "ae2:16k_crafting_storage",
                Map.of("formed", "false", "powered", "false"),
                "crafting-storage-16k-unformed", "ae2:block/crafting/16k_storage",
                "8e04febb39f74e1bb1061f9fee979be9cc4923bf14cc5a5d619cf6e681d506a4",
                true, 0, 0);
        putNativeFacadeMaterial(values, "ae2:64k_crafting_storage",
                Map.of("formed", "false", "powered", "false"),
                "crafting-storage-64k-unformed", "ae2:block/crafting/64k_storage",
                "d8a1b0f2f21c2f05cd959f03213d0434c6bb41e27d5591d0c3c532aea142eb7f",
                true, 0, 0);
        putNativeFacadeMaterial(values, "ae2:256k_crafting_storage",
                Map.of("formed", "false", "powered", "false"),
                "crafting-storage-256k-unformed", "ae2:block/crafting/256k_storage",
                "3458c6e521a76f7a0761c7efe956cc587826cfdd40d1f7c6284100990fb68905",
                true, 0, 0);
        putNativeFacadeMaterial(values, "ae2:crafting_monitor",
                Map.of("facing", "north", "formed", "false",
                        "powered", "false", "spin", "0"),
                "crafting-monitor-unformed-north", "ae2:block/crafting/monitor",
                "157e2a326b835180b369874b5f6978fab7c6796293945f85a971ac3f5b1cf2b7",
                true, 0, 0);
        putNativeFacadeMaterial(values, "ae2:crafting_unit",
                Map.of("formed", "false", "powered", "false"),
                "crafting-unit-unformed", "ae2:block/crafting/unit",
                "b33f03d38953281265d6196e2a9f2494974275901b570f390ebf40fa3a338ece",
                true, 0, 0);
        putNativeFacadeMaterial(values, "ae2:crafting_accelerator",
                Map.of("formed", "false", "powered", "false"),
                "crafting-accelerator-unformed", "ae2:block/crafting/accelerator",
                "f2b8fd7efa88b37968f55d8169eee48d84c1c673b5b2201719037771d5e18918",
                true, 0, 0);
        return Collections.unmodifiableMap(values);
    }

    private static void putNativeFacadeMaterial(
            Map<String, NeutralFacadeMaterial> values,
            String blockId,
            Map<String, String> properties,
            String materialFamily,
            String sourceModel,
            String blockstateSha256,
            boolean solidRender,
            int blockStateLightEmission,
            int facadeQuadLightEmission
    ) {
        values.put(blockId, new NeutralFacadeMaterial(
                properties, materialFamily, sourceModel, blockstateSha256,
                solidRender, blockStateLightEmission, facadeQuadLightEmission,
                validNativeFacadePropertyValues(blockId),
                nativeFacadeNormalization(blockId)
        ));
    }

    private static Map<String, List<String>> validNativeFacadePropertyValues(
            String blockId
    ) {
        if (blockId.equals("ae2:quartz_glass")
                || blockId.equals("ae2:quartz_vibrant_glass")) {
            return Map.of();
        }
        if (blockId.equals("ae2:controller")) {
            return Map.of(
                    "state", List.of("offline", "online", "conflicted"),
                    "type", List.of(
                            "block", "column_x", "column_y", "column_z",
                            "inside_a", "inside_b"
                    )
            );
        }
        if (blockId.equals("ae2:crafting_monitor")) {
            return Map.of(
                    "facing", List.of("down", "up", "north", "south", "west", "east"),
                    "formed", List.of("false", "true"),
                    "powered", List.of("false", "true"),
                    "spin", List.of("0", "1", "2", "3")
            );
        }
        return Map.of(
                "formed", List.of("false", "true"),
                "powered", List.of("false", "true")
        );
    }

    private static Map<String, String> nativeFacadeNormalization(String blockId) {
        if (blockId.equals("ae2:quartz_glass")
                || blockId.equals("ae2:quartz_vibrant_glass")) {
            return Map.of();
        }
        if (blockId.equals("ae2:controller")) {
            return Map.of("state", "offline", "type", "block");
        }
        if (blockId.equals("ae2:crafting_monitor")) {
            return Map.of(
                    "facing", "preserve",
                    "formed", "false",
                    "powered", "false",
                    "spin", "0"
            );
        }
        return Map.of("formed", "false", "powered", "false");
    }

    private static Map<String, FacadeNeutralState> buildFacadeWhitelistNeutralStates() {
        Map<String, FacadeNeutralState> values = new LinkedHashMap<>();
        for (String blockId : FACADE_WHITELIST_BLOCK_IDS) {
            NeutralFacadeMaterial nativeMaterial = NATIVE_FACADE_NEUTRAL_MATERIALS.get(
                    blockId
            );
            if (nativeMaterial != null) {
                values.put(blockId, new FacadeNeutralState(
                        nativeMaterial.properties(), nativeMaterial.solidRender()
                ));
            } else {
                values.put(blockId, vanillaFacadeWhitelistNeutralState(blockId));
            }
        }
        return Collections.unmodifiableMap(values);
    }

    private static Map<String, Map<String, List<String>>>
            buildFacadeWhitelistStateSchemas() {
        List<String> booleans = List.of("false", "true");
        List<String> directions = List.of("down", "up", "north", "south", "west", "east");
        List<String> horizontalDirections = List.of("north", "south", "west", "east");
        Map<String, Map<String, List<String>>> schemas = new LinkedHashMap<>();
        putStateSchema(schemas, "ae2:quartz_glass");
        putStateSchema(schemas, "ae2:quartz_vibrant_glass");
        putStateSchema(schemas, "minecraft:chiseled_bookshelf",
                property("facing", horizontalDirections),
                property("slot_0_occupied", booleans),
                property("slot_1_occupied", booleans),
                property("slot_2_occupied", booleans),
                property("slot_3_occupied", booleans),
                property("slot_4_occupied", booleans),
                property("slot_5_occupied", booleans));
        putStateSchema(schemas, "minecraft:jukebox", property("has_record", booleans));
        putStateSchema(schemas, "minecraft:furnace",
                property("facing", horizontalDirections), property("lit", booleans));
        putStateSchema(schemas, "minecraft:blast_furnace",
                property("facing", horizontalDirections), property("lit", booleans));
        putStateSchema(schemas, "minecraft:dropper",
                property("facing", directions), property("triggered", booleans));
        putStateSchema(schemas, "minecraft:dispenser",
                property("facing", directions), property("triggered", booleans));
        putStateSchema(schemas, "minecraft:crafter",
                property("crafting", booleans),
                property("orientation", "down_east", "down_north", "down_south",
                        "down_west", "up_east", "up_north", "up_south", "up_west",
                        "west_up", "east_up", "north_up", "south_up"),
                property("triggered", booleans));
        putStateSchema(schemas, "minecraft:barrel",
                property("facing", directions), property("open", booleans));
        putStateSchema(schemas, "minecraft:bee_nest",
                property("facing", horizontalDirections),
                property("honey_level", "0", "1", "2", "3", "4", "5"));
        putStateSchema(schemas, "minecraft:beehive",
                property("facing", horizontalDirections),
                property("honey_level", "0", "1", "2", "3", "4", "5"));
        putStateSchema(schemas, "minecraft:sculk_catalyst", property("bloom", booleans));
        putStateSchema(schemas, "minecraft:soul_sand");
        putStateSchema(schemas, "minecraft:honey_block");
        putStateSchema(schemas, "ae2:controller",
                property("state", "offline", "online", "conflicted"),
                property("type", "block", "column_x", "column_y", "column_z",
                        "inside_a", "inside_b"));
        for (String blockId : List.of(
                "ae2:1k_crafting_storage",
                "ae2:4k_crafting_storage",
                "ae2:16k_crafting_storage",
                "ae2:64k_crafting_storage",
                "ae2:256k_crafting_storage"
        )) {
            putStateSchema(schemas, blockId,
                    property("formed", booleans), property("powered", booleans));
        }
        putStateSchema(schemas, "ae2:crafting_monitor",
                property("facing", directions), property("formed", booleans),
                property("powered", booleans), property("spin", "0", "1", "2", "3"));
        putStateSchema(schemas, "ae2:crafting_unit",
                property("formed", booleans), property("powered", booleans));
        putStateSchema(schemas, "ae2:crafting_accelerator",
                property("formed", booleans), property("powered", booleans));
        Map<String, Map<String, List<String>>> ordered = new LinkedHashMap<>();
        for (String blockId : FACADE_WHITELIST_BLOCK_IDS) {
            ordered.put(blockId, schemas.get(blockId));
        }
        return Collections.unmodifiableMap(ordered);
    }

    private static Map<String, String> buildFacadeWhitelistBlockstateSha256() {
        Map<String, String> digests = new LinkedHashMap<>();
        for (String blockId : FACADE_WHITELIST_BLOCK_IDS) {
            NeutralFacadeMaterial nativeMaterial = NATIVE_FACADE_NEUTRAL_MATERIALS.get(
                    blockId
            );
            digests.put(blockId, nativeMaterial == null
                    ? vanillaFacadeWhitelistBlockstateSha256(blockId)
                    : nativeMaterial.blockstateSha256());
        }
        return Collections.unmodifiableMap(digests);
    }

    private static String vanillaFacadeWhitelistBlockstateSha256(String blockId) {
        return switch (blockId) {
            case "minecraft:chiseled_bookshelf" ->
                    "7f3f363d1e155d92d08916d8f08de670e269ae4a05fce0844c8bcd6930e8d098";
            case "minecraft:jukebox" ->
                    "8002563a048d4a5afb22d44692ca1a38e114ef95a3ccd24f02b9e0fd02b693d5";
            case "minecraft:furnace" ->
                    "aedb43571027a5dea15ba9cbfc05f0327af3048de70b72c3cd67c851839bb284";
            case "minecraft:blast_furnace" ->
                    "265ec5f30fa65bdaff6867bbad8de73e0a1b21ea12a33da5b771f889e4ac7dcc";
            case "minecraft:dropper" ->
                    "c763060c1946a3031cdf6e68ab98db7d81d83c364b0ad4da54fdf055225753c3";
            case "minecraft:dispenser" ->
                    "fc1ba39eb47f31285b5d1c9f729fabf5ad9832d8b8f6b1f510d3c870f6e6bfd8";
            case "minecraft:crafter" ->
                    "dfa8af74cd96d1d6f2086a63fab3402864497d7f436ba469b34be58924f1edfa";
            case "minecraft:barrel" ->
                    "d8e00576b5f85f83a42b7b31dc177e0add02cf2204fa441c0fe31cbd6d70dcca";
            case "minecraft:bee_nest" ->
                    "09ee024cc05e40767c3e88776e336396a71b146a7fb93f64ba1860aa6a107853";
            case "minecraft:beehive" ->
                    "c4c438bb21bf78f5bdc8835daf852c2f2040e3f96872fe4488d2710a6abfc8ae";
            case "minecraft:sculk_catalyst" ->
                    "0e6c7b956647211dea0d7ce46e9e111296dd985c51d58a3c094630850b764504";
            case "minecraft:soul_sand" ->
                    "6a0ea83a331843c30e21f8d7ea9252c429c4093b6222b60e62e8ab47ca802ef8";
            case "minecraft:honey_block" ->
                    "780ffcffff91d90efe172f2f1f200a06dcbe885fe5316d16bebf72bae2ef7c44";
            default -> throw new IllegalArgumentException(
                    "not a vanilla explicit facade whitelist ID: " + blockId
            );
        };
    }

    private static Map<String, Boolean> buildFacadeWhitelistSameStateSkipRendering() {
        Map<String, Boolean> values = new LinkedHashMap<>();
        for (String blockId : FACADE_WHITELIST_BLOCK_IDS) {
            values.put(blockId, blockId.equals("ae2:quartz_glass")
                    || blockId.equals("ae2:quartz_vibrant_glass")
                    || blockId.equals("minecraft:honey_block"));
        }
        return Collections.unmodifiableMap(values);
    }

    private static Map<String, Boolean> buildFacadeOrdinarySkipRenderingControls() {
        Map<String, Boolean> values = new LinkedHashMap<>();
        values.put("minecraft:glass", true);
        values.put("minecraft:oak_log", false);
        values.put("minecraft:oak_leaves", false);
        return Collections.unmodifiableMap(values);
    }

    private static FacadeNeutralState vanillaFacadeWhitelistNeutralState(String blockId) {
        return switch (blockId) {
            case "minecraft:chiseled_bookshelf" -> new FacadeNeutralState(Map.of(
                    "facing", "north",
                    "slot_0_occupied", "false",
                    "slot_1_occupied", "false",
                    "slot_2_occupied", "false",
                    "slot_3_occupied", "false",
                    "slot_4_occupied", "false",
                    "slot_5_occupied", "false"
            ), true);
            case "minecraft:jukebox" -> new FacadeNeutralState(
                    Map.of("has_record", "false"), true
            );
            case "minecraft:furnace", "minecraft:blast_furnace" ->
                    new FacadeNeutralState(
                            Map.of("facing", "north", "lit", "false"), true
                    );
            case "minecraft:dropper", "minecraft:dispenser" ->
                    new FacadeNeutralState(
                            Map.of("facing", "north", "triggered", "false"), true
                    );
            case "minecraft:crafter" -> new FacadeNeutralState(Map.of(
                    "crafting", "false",
                    "orientation", "north_up",
                    "triggered", "false"
            ), true);
            case "minecraft:barrel" -> new FacadeNeutralState(
                    Map.of("facing", "north", "open", "false"), true
            );
            case "minecraft:bee_nest", "minecraft:beehive" ->
                    new FacadeNeutralState(
                            Map.of("facing", "north", "honey_level", "0"), true
                    );
            case "minecraft:sculk_catalyst" -> new FacadeNeutralState(
                    Map.of("bloom", "false"), true
            );
            case "minecraft:soul_sand" -> new FacadeNeutralState(Map.of(), true);
            case "minecraft:honey_block" -> new FacadeNeutralState(Map.of(), false);
            default -> throw new IllegalArgumentException(
                    "not a vanilla explicit facade whitelist ID: " + blockId
            );
        };
    }

    private static Map<String, List<PartBox>> buildNativeFacePartCollisionBoxes() {
        Map<String, List<PartBox>> values = new LinkedHashMap<>();
        values.put("ae2:quartz_fiber", boxes(6, 6, 10, 10, 10, 16));
        values.put("ae2:toggle_bus", boxes(6, 6, 11, 10, 10, 16));
        values.put("ae2:inverted_toggle_bus", boxes(6, 6, 11, 10, 10, 16));
        values.put("ae2:cable_anchor", cableAnchorCollisionBoxes(false));
        List<PartBox> reporting = boxes(
                2, 2, 14, 14, 14, 16,
                4, 4, 13, 12, 12, 14
        );
        putCollisionBoxes(values, reporting, "ae2:monitor", "ae2:semi_dark_monitor",
                "ae2:dark_monitor");
        values.put("ae2:storage_bus", boxes(
                3, 3, 15, 13, 13, 16,
                2, 2, 14, 14, 14, 15,
                5, 5, 12, 11, 11, 14
        ));
        values.put("ae2:import_bus", boxes(
                6, 6, 11, 10, 10, 13,
                5, 5, 13, 11, 11, 14,
                4, 4, 14, 12, 12, 16
        ));
        values.put("ae2:export_bus", boxes(
                4, 4, 12, 12, 12, 14,
                5, 5, 14, 11, 11, 15,
                6, 6, 15, 10, 10, 16,
                6, 6, 11, 10, 10, 12
        ));
        List<PartBox> emitter = boxes(7, 7, 11, 9, 9, 16);
        putCollisionBoxes(values, emitter, "ae2:level_emitter",
                "ae2:energy_level_emitter");
        // Mask zero is installed-face invariant.
        List<PartBox> plane = planeCollisionBoxes("north", 0);
        putCollisionBoxes(values, plane, "ae2:annihilation_plane", "ae2:formation_plane");
        putCollisionBoxes(values, reporting, "ae2:pattern_encoding_terminal",
                "ae2:crafting_terminal", "ae2:terminal", "ae2:storage_monitor",
                "ae2:conversion_monitor");
        List<PartBox> provider = boxes(
                2, 2, 14, 14, 14, 16,
                5, 5, 12, 11, 11, 14
        );
        putCollisionBoxes(values, provider, "ae2:cable_pattern_provider",
                "ae2:cable_interface");
        values.put("ae2:pattern_access_terminal", reporting);
        values.put("ae2:cable_energy_acceptor", boxes(
                2, 2, 14, 14, 14, 16,
                4, 4, 12, 12, 12, 14
        ));
        List<PartBox> p2p = boxes(
                5, 5, 12, 11, 11, 13,
                3, 3, 13, 13, 13, 14,
                2, 2, 14, 14, 14, 16
        );
        putCollisionBoxes(values, p2p, "ae2:me_p2p_tunnel",
                "ae2:redstone_p2p_tunnel", "ae2:item_p2p_tunnel",
                "ae2:fluid_p2p_tunnel", "ae2:fe_p2p_tunnel",
                "ae2:light_p2p_tunnel");
        return orderedPartMap(values);
    }

    private static Map<String, String> buildNativeFacePartCollisionModes() {
        Map<String, String> modes = new LinkedHashMap<>();
        for (String id : NATIVE_FACE_PART_IDS) {
            modes.put(id, "static");
        }
        modes.put("ae2:cable_anchor", "same-side-facade-conditioned");
        modes.put("ae2:annihilation_plane", "plane-connection-mask");
        modes.put("ae2:formation_plane", "plane-connection-mask");
        return orderedPartMap(modes);
    }

    private static List<PartBox> boxes(int... coordinates) {
        if (coordinates.length == 0 || coordinates.length % 6 != 0) {
            throw new IllegalArgumentException("part boxes require six coordinates each");
        }
        java.util.ArrayList<PartBox> values = new java.util.ArrayList<>();
        for (int index = 0; index < coordinates.length; index += 6) {
            values.add(new PartBox(
                    coordinates[index],
                    coordinates[index + 1],
                    coordinates[index + 2],
                    coordinates[index + 3],
                    coordinates[index + 4],
                    coordinates[index + 5]
            ));
        }
        return List.copyOf(values);
    }

    private static void putCollisionBoxes(
            Map<String, List<PartBox>> values,
            List<PartBox> boxes,
            String... ids
    ) {
        for (String id : ids) {
            values.put(id, boxes);
        }
    }

    private static <T> Map<String, T> orderedPartMap(Map<String, T> values) {
        Map<String, T> result = new LinkedHashMap<>();
        for (String id : NATIVE_FACE_PART_IDS) {
            result.put(id, values.get(id));
        }
        return Collections.unmodifiableMap(result);
    }

    private static void putModels(
            Map<String, List<String>> models,
            String id,
            String... layers
    ) {
        models.put(id, List.of(layers));
    }

    private static void putP2pModels(
            Map<String, List<String>> models,
            String id,
            String front
    ) {
        putModels(models, id,
                "ae2:part/p2p/p2p_tunnel_status_off",
                "ae2:part/p2p/p2p_tunnel_frequency",
                front);
    }

    private static Map<String, String> nativeFacePartGroups() {
        Map<String, String> groups = new LinkedHashMap<>();
        putGroup(groups, "network", "ae2:quartz_fiber", "ae2:cable_energy_acceptor");
        putGroup(groups, "redstone", "ae2:toggle_bus", "ae2:inverted_toggle_bus");
        putGroup(groups, "structural", "ae2:cable_anchor");
        putGroup(groups, "panel", "ae2:monitor", "ae2:semi_dark_monitor",
                "ae2:dark_monitor");
        putGroup(groups, "bus", "ae2:storage_bus", "ae2:import_bus", "ae2:export_bus");
        putGroup(groups, "emitter", "ae2:level_emitter", "ae2:energy_level_emitter");
        putGroup(groups, "plane", "ae2:annihilation_plane", "ae2:formation_plane");
        putGroup(groups, "terminal", "ae2:pattern_encoding_terminal",
                "ae2:crafting_terminal", "ae2:terminal", "ae2:pattern_access_terminal");
        putGroup(groups, "monitor", "ae2:storage_monitor", "ae2:conversion_monitor");
        putGroup(groups, "service", "ae2:cable_pattern_provider", "ae2:cable_interface");
        putGroup(groups, "p2p", "ae2:me_p2p_tunnel", "ae2:redstone_p2p_tunnel",
                "ae2:item_p2p_tunnel", "ae2:fluid_p2p_tunnel", "ae2:fe_p2p_tunnel",
                "ae2:light_p2p_tunnel");
        return Collections.unmodifiableMap(groups);
    }

    private static void putGroup(Map<String, String> groups, String group, String... ids) {
        for (String id : ids) {
            groups.put(id, group);
        }
    }

    private static Map<String, String> buildNativeEndpointCableTypes() {
        Map<String, String> types = new LinkedHashMap<>();
        putEndpointTypes(types, "COVERED", "ae2:inscriber", "ae2:charger",
                "ae2:energy_acceptor", "ae2:vibration_chamber", "ae2:growth_accelerator",
                "ae2:energy_cell", "ae2:dense_energy_cell", "ae2:creative_energy_cell",
                "ae2:molecular_assembler");
        putEndpointTypes(types, "DENSE_SMART", "ae2:quantum_ring", "ae2:quantum_link",
                "ae2:controller");
        for (String id : NATIVE_ENDPOINT_IDS) {
            types.putIfAbsent(id, "SMART");
        }
        return orderedEndpointMap(types);
    }

    private static void putEndpointTypes(
            Map<String, String> types,
            String type,
            String... ids
    ) {
        for (String id : ids) {
            types.put(id, type);
        }
    }

    private static Map<String, String> nativeEndpointClasses() {
        Map<String, String> classes = new LinkedHashMap<>();
        classes.put("ae2:inscriber", "appeng.blockentity.misc.InscriberBlockEntity");
        classes.put("ae2:wireless_access_point",
                "appeng.blockentity.networking.WirelessAccessPointBlockEntity");
        classes.put("ae2:charger", "appeng.blockentity.misc.ChargerBlockEntity");
        classes.put("ae2:quantum_ring", "appeng.blockentity.qnb.QuantumBridgeBlockEntity");
        classes.put("ae2:quantum_link", "appeng.blockentity.qnb.QuantumBridgeBlockEntity");
        classes.put("ae2:spatial_pylon",
                "appeng.blockentity.spatial.SpatialPylonBlockEntity");
        classes.put("ae2:spatial_io_port",
                "appeng.blockentity.spatial.SpatialIOPortBlockEntity");
        classes.put("ae2:spatial_anchor",
                "appeng.blockentity.spatial.SpatialAnchorBlockEntity");
        classes.put("ae2:controller", "appeng.blockentity.networking.ControllerBlockEntity");
        classes.put("ae2:drive", "appeng.blockentity.storage.DriveBlockEntity");
        classes.put("ae2:chest", "appeng.blockentity.storage.MEChestBlockEntity");
        classes.put("ae2:interface", "appeng.blockentity.misc.InterfaceBlockEntity");
        classes.put("ae2:io_port", "appeng.blockentity.storage.IOPortBlockEntity");
        classes.put("ae2:energy_acceptor",
                "appeng.blockentity.networking.EnergyAcceptorBlockEntity");
        classes.put("ae2:crystal_resonance_generator",
                "appeng.blockentity.networking.CrystalResonanceGeneratorBlockEntity");
        classes.put("ae2:vibration_chamber",
                "appeng.blockentity.misc.VibrationChamberBlockEntity");
        classes.put("ae2:growth_accelerator",
                "appeng.blockentity.misc.GrowthAcceleratorBlockEntity");
        classes.put("ae2:energy_cell", "appeng.blockentity.networking.EnergyCellBlockEntity");
        classes.put("ae2:dense_energy_cell",
                "appeng.blockentity.networking.EnergyCellBlockEntity");
        classes.put("ae2:creative_energy_cell",
                "appeng.blockentity.networking.CreativeEnergyCellBlockEntity");
        putSharedClass(classes, "appeng.blockentity.crafting.CraftingBlockEntity",
                "ae2:crafting_unit", "ae2:crafting_accelerator", "ae2:1k_crafting_storage",
                "ae2:4k_crafting_storage", "ae2:16k_crafting_storage",
                "ae2:64k_crafting_storage", "ae2:256k_crafting_storage");
        classes.put("ae2:crafting_monitor",
                "appeng.blockentity.crafting.CraftingMonitorBlockEntity");
        classes.put("ae2:pattern_provider",
                "appeng.blockentity.crafting.PatternProviderBlockEntity");
        classes.put("ae2:molecular_assembler",
                "appeng.blockentity.crafting.MolecularAssemblerBlockEntity");
        return Collections.unmodifiableMap(classes);
    }

    private static Map<String, String> buildNativeEndpointBlockEntityIds() {
        Map<String, String> ids = new LinkedHashMap<>();
        ids.put("ae2:inscriber", "ae2:inscriber");
        ids.put("ae2:wireless_access_point", "ae2:wireless_access_point");
        ids.put("ae2:charger", "ae2:charger");
        ids.put("ae2:quantum_ring", "ae2:quantum_ring");
        ids.put("ae2:quantum_link", "ae2:quantum_ring");
        ids.put("ae2:spatial_pylon", "ae2:spatial_pylon");
        ids.put("ae2:spatial_io_port", "ae2:spatial_io_port");
        ids.put("ae2:spatial_anchor", "ae2:spatial_anchor");
        ids.put("ae2:controller", "ae2:controller");
        ids.put("ae2:drive", "ae2:drive");
        ids.put("ae2:chest", "ae2:chest");
        ids.put("ae2:interface", "ae2:interface");
        ids.put("ae2:io_port", "ae2:io_port");
        ids.put("ae2:energy_acceptor", "ae2:energy_acceptor");
        ids.put("ae2:crystal_resonance_generator",
                "ae2:crystal_resonance_generator");
        ids.put("ae2:vibration_chamber", "ae2:vibration_chamber");
        ids.put("ae2:growth_accelerator", "ae2:growth_accelerator");
        ids.put("ae2:energy_cell", "ae2:energy_cell");
        ids.put("ae2:dense_energy_cell", "ae2:dense_energy_cell");
        ids.put("ae2:creative_energy_cell", "ae2:creative_energy_cell");
        ids.put("ae2:crafting_unit", "ae2:crafting_unit");
        ids.put("ae2:crafting_accelerator", "ae2:crafting_unit");
        ids.put("ae2:1k_crafting_storage", "ae2:crafting_storage");
        ids.put("ae2:4k_crafting_storage", "ae2:crafting_storage");
        ids.put("ae2:16k_crafting_storage", "ae2:crafting_storage");
        ids.put("ae2:64k_crafting_storage", "ae2:crafting_storage");
        ids.put("ae2:256k_crafting_storage", "ae2:crafting_storage");
        ids.put("ae2:crafting_monitor", "ae2:crafting_monitor");
        ids.put("ae2:pattern_provider", "ae2:pattern_provider");
        ids.put("ae2:molecular_assembler", "ae2:molecular_assembler");
        return orderedEndpointMap(ids);
    }

    private static Map<String, String> buildNativeEndpointSideRules() {
        Map<String, String> rules = new LinkedHashMap<>();
        putEndpointTypes(rules, "ALL", "ae2:spatial_io_port", "ae2:spatial_anchor",
                "ae2:controller", "ae2:chest", "ae2:interface", "ae2:io_port",
                "ae2:energy_acceptor", "ae2:vibration_chamber", "ae2:energy_cell",
                "ae2:dense_energy_cell", "ae2:creative_energy_cell",
                "ae2:molecular_assembler");
        putEndpointTypes(rules, "BACK", "ae2:wireless_access_point",
                "ae2:crystal_resonance_generator");
        putEndpointTypes(rules, "NO_FRONT", "ae2:inscriber", "ae2:charger",
                "ae2:drive");
        putEndpointTypes(rules, "FRONT_BACK", "ae2:growth_accelerator");
        putEndpointTypes(rules, "PUSH_DIRECTION", "ae2:pattern_provider");
        putEndpointTypes(rules, "FORMED_CRAFTING", "ae2:crafting_unit",
                "ae2:crafting_accelerator", "ae2:1k_crafting_storage",
                "ae2:4k_crafting_storage", "ae2:16k_crafting_storage",
                "ae2:64k_crafting_storage", "ae2:256k_crafting_storage",
                "ae2:crafting_monitor");
        putEndpointTypes(rules, "FORMED_QUANTUM", "ae2:quantum_ring",
                "ae2:quantum_link");
        putEndpointTypes(rules, "VALID_STRAIGHT_PYLON", "ae2:spatial_pylon");
        return orderedEndpointMap(rules);
    }

    private static Map<String, Map<String, List<String>>> buildNativeEndpointStateSchemas() {
        List<String> directions = List.of("down", "up", "north", "south", "west", "east");
        List<String> booleans = List.of("false", "true");
        List<String> spins = List.of("0", "1", "2", "3");
        Map<String, Map<String, List<String>>> schemas = new LinkedHashMap<>();
        putStateSchema(schemas, "ae2:inscriber",
                property("facing", directions), property("spin", spins),
                property("waterlogged", booleans));
        putStateSchema(schemas, "ae2:wireless_access_point",
                property("facing", directions),
                property("state", "off", "on", "has_channel"),
                property("waterlogged", booleans));
        putStateSchema(schemas, "ae2:charger",
                property("facing", directions), property("spin", spins));
        putStateSchema(schemas, "ae2:quantum_ring",
                property("formed", booleans), property("waterlogged", booleans));
        putStateSchema(schemas, "ae2:quantum_link",
                property("formed", booleans), property("waterlogged", booleans));
        putStateSchema(schemas, "ae2:spatial_pylon", property("powered_on", booleans));
        putStateSchema(schemas, "ae2:spatial_io_port",
                property("facing", directions), property("powered", booleans),
                property("spin", spins));
        putStateSchema(schemas, "ae2:spatial_anchor",
                property("facing", directions), property("powered", booleans));
        putStateSchema(schemas, "ae2:controller",
                property("state", "offline", "online", "conflicted"),
                property("type", "block", "column_x", "column_y", "column_z",
                        "inside_a", "inside_b"));
        putStateSchema(schemas, "ae2:drive",
                property("facing", directions), property("spin", spins));
        putStateSchema(schemas, "ae2:chest",
                property("facing", directions), property("lights_on", booleans),
                property("spin", spins));
        putStateSchema(schemas, "ae2:interface");
        putStateSchema(schemas, "ae2:io_port",
                property("facing", directions), property("powered", booleans),
                property("spin", spins));
        putStateSchema(schemas, "ae2:energy_acceptor");
        putStateSchema(schemas, "ae2:crystal_resonance_generator",
                property("facing", directions), property("waterlogged", booleans));
        putStateSchema(schemas, "ae2:vibration_chamber",
                property("active", booleans), property("facing", directions),
                property("spin", spins));
        putStateSchema(schemas, "ae2:growth_accelerator",
                property("facing", directions), property("powered", booleans));
        putStateSchema(schemas, "ae2:energy_cell",
                property("fullness", "0", "1", "2", "3", "4"));
        putStateSchema(schemas, "ae2:dense_energy_cell",
                property("fullness", "0", "1", "2", "3", "4"));
        putStateSchema(schemas, "ae2:creative_energy_cell");
        for (String id : List.of(
                "ae2:crafting_unit", "ae2:crafting_accelerator",
                "ae2:1k_crafting_storage", "ae2:4k_crafting_storage",
                "ae2:16k_crafting_storage", "ae2:64k_crafting_storage",
                "ae2:256k_crafting_storage")) {
            putStateSchema(schemas, id,
                    property("formed", booleans), property("powered", booleans));
        }
        putStateSchema(schemas, "ae2:crafting_monitor",
                property("facing", directions), property("formed", booleans),
                property("powered", booleans), property("spin", spins));
        putStateSchema(schemas, "ae2:pattern_provider",
                property("push_direction", "down", "up", "north", "south", "west",
                        "east", "all"));
        putStateSchema(schemas, "ae2:molecular_assembler", property("powered", booleans));
        return orderedEndpointNestedMap(schemas);
    }

    private static void putStateSchema(
            Map<String, Map<String, List<String>>> schemas,
            String id,
            PropertyDomain... domains
    ) {
        Map<String, List<String>> properties = new LinkedHashMap<>();
        for (PropertyDomain domain : domains) {
            properties.put(domain.name(), domain.values());
        }
        schemas.put(id, Collections.unmodifiableMap(properties));
    }

    private static PropertyDomain property(String name, String... values) {
        return new PropertyDomain(name, List.of(values));
    }

    private static PropertyDomain property(String name, List<String> values) {
        return new PropertyDomain(name, values);
    }

    private static Map<String, Map<String, List<String>>> orderedEndpointNestedMap(
            Map<String, Map<String, List<String>>> values
    ) {
        Map<String, Map<String, List<String>>> result = new LinkedHashMap<>();
        for (String id : NATIVE_ENDPOINT_IDS) {
            result.put(id, values.get(id));
        }
        return Collections.unmodifiableMap(result);
    }

    private static Map<String, String> buildNativeEndpointBlockstateSha256() {
        Map<String, String> digests = new LinkedHashMap<>();
        digests.put("ae2:inscriber", "4ec6c21834e68f179c252bf22aeb8f8f67d57ef057eb8bde57f65f576e0885f2");
        digests.put("ae2:wireless_access_point", "05c09f9e0bcb7a09ee8f2566a2eb3f885549df2765f624c7c44fed87eee6cf6e");
        digests.put("ae2:charger", "83ebcbf59495865f7302e58292f81b83231016b1fce15515ccc10cb158f73d76");
        digests.put("ae2:quantum_ring", "3db38f2e82cd1a9e1e2e45cb078d09f0d01507750cd895615bb9a7f722f27c50");
        digests.put("ae2:quantum_link", "156f0aeafca2763f1e3fccadd342c08da7870bcb3aa8f176127a2a3502b3aa7d");
        digests.put("ae2:spatial_pylon", "a3c18208840e313823afc7198e8d74da9b1e65e78dffdc6327f53d2b70e678c9");
        digests.put("ae2:spatial_io_port", "fd2ff71aef6d77ea08dcd5aa80d7972f802a7d2ecf788cf45dbc26ade51fd542");
        digests.put("ae2:spatial_anchor", "38019a1eda66fef56bf493d818fe3452cbd8367f57fecf29acbe80f7d430837f");
        digests.put("ae2:controller", "693d04c733b47e4159052d0843256fa7520bbc1984b6d9e454bec976a73d2ca8");
        digests.put("ae2:drive", "b69d86cf730369715ad51f23793efb9b6910ec9760d4ab40029e128046d204ce");
        digests.put("ae2:chest", "c628dab804fe28fa813fef46ddcf2e4f5f13192e63cea2a7f8b8dcc3d0810ed0");
        digests.put("ae2:interface", "1bc532291c1343d076662eb69d6913953b27f91ce5d722a5b78c6095f56167ee");
        digests.put("ae2:io_port", "601dccfb290cfd7f70c2f1e0662082e4f17c10ecfec3857b55f96db13113dbcf");
        digests.put("ae2:energy_acceptor", "ee3ffe5a1fc5269a13b4474bd23ef8f98869a528bda2287ba849ca4fd4f14a7c");
        digests.put("ae2:crystal_resonance_generator",
                "11d0a847d7abfb1db1acb8a748a3203aa7af9b76ff4c194c288ddc29d131229d");
        digests.put("ae2:vibration_chamber", "6555d07d339d0fd2af34f5b7f4fbf574313df8701544bdb2e4189a17dcc3038c");
        digests.put("ae2:growth_accelerator", "57cd5e8741a98c81b4db43bd796beaae8e9f1f227c9eeac03164b6552e8f1212");
        digests.put("ae2:energy_cell", "2e285ec4568671ea1185c70c6f38ab3a943cf24dcdd7847fe0886871409ea0fa");
        digests.put("ae2:dense_energy_cell", "357108af0d785e58fea6240d4cba13e81b686caadaab974fcf30c0ea99ca616d");
        digests.put("ae2:creative_energy_cell", "e924240fd1c63be2a7033f764213c9d0f3d8cab2269d7e73dc1f7abadb18de80");
        digests.put("ae2:crafting_unit", "b33f03d38953281265d6196e2a9f2494974275901b570f390ebf40fa3a338ece");
        digests.put("ae2:crafting_accelerator", "f2b8fd7efa88b37968f55d8169eee48d84c1c673b5b2201719037771d5e18918");
        digests.put("ae2:1k_crafting_storage", "9a1f6383cd3b54a8361cefc46740ddbee587ce79baefccb6ad6de6355833a603");
        digests.put("ae2:4k_crafting_storage", "dd4210a4c0fc5b0eb7f524571f20b7e1a92c438bc68df7324cb26c939c726abc");
        digests.put("ae2:16k_crafting_storage", "8e04febb39f74e1bb1061f9fee979be9cc4923bf14cc5a5d619cf6e681d506a4");
        digests.put("ae2:64k_crafting_storage", "d8a1b0f2f21c2f05cd959f03213d0434c6bb41e27d5591d0c3c532aea142eb7f");
        digests.put("ae2:256k_crafting_storage", "3458c6e521a76f7a0761c7efe956cc587826cfdd40d1f7c6284100990fb68905");
        digests.put("ae2:crafting_monitor", "157e2a326b835180b369874b5f6978fab7c6796293945f85a971ac3f5b1cf2b7");
        digests.put("ae2:pattern_provider", "1b8e3a67480db0dec346477a67e026798b7287db7b48b4242f58d405035b0b83");
        digests.put("ae2:molecular_assembler", "136857cc899a24bcca0b730790da3128a74c9e8196028a264e32e5e1582183a0");
        return orderedEndpointMap(digests);
    }

    private static List<String> buildKnownUnsupportedCompatibleEndpointIds() {
        java.util.ArrayList<String> ids = new java.util.ArrayList<>();
        Collections.addAll(ids,
                "expandedae:exp_pattern_provider",
                "expandedae:exp_io_port",
                "expandedae:colorable_drive",
                "expandedae:exp_crafting_unit");
        for (String tier : List.of("2", "4", "8", "16", "32", "64", "128", "256",
                "512", "1k", "2k", "4k", "8k", "16k", "32k", "64k", "128k",
                "256k", "512k", "1m")) {
            ids.add("expandedae:exp_crafting_accelerator_" + tier);
        }
        Collections.addAll(ids,
                "megacells:mega_energy_cell",
                "megacells:mega_crafting_unit",
                "megacells:mega_crafting_accelerator",
                "megacells:1m_crafting_storage",
                "megacells:4m_crafting_storage",
                "megacells:16m_crafting_storage",
                "megacells:64m_crafting_storage",
                "megacells:256m_crafting_storage",
                "megacells:mega_crafting_monitor",
                "megacells:mega_interface",
                "megacells:mega_pattern_provider",
                "advanced_ae:quantum_unit",
                "advanced_ae:quantum_core",
                "advanced_ae:data_entangler",
                "advanced_ae:quantum_storage_128",
                "advanced_ae:quantum_storage_256",
                "advanced_ae:quantum_accelerator",
                "advanced_ae:quantum_multi_threader",
                "advanced_ae:quantum_structure",
                "advanced_ae:adv_pattern_provider",
                "advanced_ae:small_adv_pattern_provider",
                "advanced_ae:reaction_chamber",
                "advanced_ae:quantum_crafter");
        for (String path : List.of(
                "crystal_assembler", "ex_pattern_provider", "ex_interface",
                "wireless_connect", "ex_drive", "ex_molecular_assembler",
                "ex_inscriber", "ex_charger", "crystal_fixer", "caner",
                "ex_io_port", "circuit_cutter", "oversize_interface",
                "assembler_matrix_frame", "assembler_matrix_wall",
                "assembler_matrix_glass", "assembler_matrix_pattern",
                "assembler_matrix_crafter", "assembler_matrix_speed", "wireless_hub")) {
            ids.add("extendedae:" + path);
        }
        return List.copyOf(ids);
    }

    private static Map<String, String>
            buildKnownUnsupportedCompatibleEndpointBlockEntityIds() {
        Map<String, String> ids = new LinkedHashMap<>();
        putSameBlockEntityId(ids, "expandedae:exp_pattern_provider",
                "expandedae:exp_io_port", "expandedae:colorable_drive");
        for (String id : KNOWN_UNSUPPORTED_COMPATIBLE_ENDPOINT_IDS.subList(3, 24)) {
            ids.put(id, "expandedae:exp_cpus");
        }
        ids.put("megacells:mega_energy_cell", "megacells:mega_energy_cell");
        putSharedBlockEntityId(ids, "megacells:mega_crafting_unit",
                "megacells:mega_crafting_unit", "megacells:mega_crafting_accelerator");
        putSharedBlockEntityId(ids, "megacells:mega_crafting_storage",
                "megacells:1m_crafting_storage", "megacells:4m_crafting_storage",
                "megacells:16m_crafting_storage", "megacells:64m_crafting_storage",
                "megacells:256m_crafting_storage");
        putSameBlockEntityId(ids, "megacells:mega_crafting_monitor",
                "megacells:mega_interface", "megacells:mega_pattern_provider");
        putSharedBlockEntityId(ids, "advanced_ae:quantum_core",
                "advanced_ae:quantum_unit", "advanced_ae:quantum_core",
                "advanced_ae:data_entangler", "advanced_ae:quantum_storage_128",
                "advanced_ae:quantum_storage_256", "advanced_ae:quantum_accelerator",
                "advanced_ae:quantum_multi_threader", "advanced_ae:quantum_structure");
        putSameBlockEntityId(ids, "advanced_ae:adv_pattern_provider",
                "advanced_ae:small_adv_pattern_provider", "advanced_ae:reaction_chamber");
        ids.put("advanced_ae:quantum_crafter", "advanced_ae:quantum_craft");
        for (String id : KNOWN_UNSUPPORTED_COMPATIBLE_ENDPOINT_IDS.subList(47, 67)) {
            ids.put(id, id);
        }
        return orderedKnownUnsupportedEndpointMap(ids);
    }

    private static void putSameBlockEntityId(Map<String, String> values, String... ids) {
        for (String id : ids) {
            values.put(id, id);
        }
    }

    private static void putSharedBlockEntityId(
            Map<String, String> values,
            String blockEntityId,
            String... ids
    ) {
        for (String id : ids) {
            values.put(id, blockEntityId);
        }
    }

    private static Map<String, String> orderedKnownUnsupportedEndpointMap(
            Map<String, String> values
    ) {
        Map<String, String> result = new LinkedHashMap<>();
        for (String id : KNOWN_UNSUPPORTED_COMPATIBLE_ENDPOINT_IDS) {
            result.put(id, values.get(id));
        }
        return Collections.unmodifiableMap(result);
    }

    private static Map<String, Integer> buildNativeEndpointSideRuleCounts() {
        Map<String, Integer> counts = new LinkedHashMap<>();
        for (String rule : List.of("ALL", "BACK", "NO_FRONT", "FRONT_BACK",
                "PUSH_DIRECTION", "FORMED_CRAFTING", "FORMED_QUANTUM",
                "VALID_STRAIGHT_PYLON")) {
            counts.put(rule, (int) NATIVE_ENDPOINT_SIDE_RULES.values().stream()
                    .filter(rule::equals)
                    .count());
        }
        return Collections.unmodifiableMap(counts);
    }

    private static void putSharedClass(
            Map<String, String> classes,
            String className,
            String... ids
    ) {
        for (String id : ids) {
            classes.put(id, className);
        }
    }

    private static Map<String, String> orderedEndpointMap(Map<String, String> values) {
        Map<String, String> result = new LinkedHashMap<>();
        for (String id : NATIVE_ENDPOINT_IDS) {
            result.put(id, values.get(id));
        }
        return Collections.unmodifiableMap(result);
    }

    private static ManifestData loadRequiredResources() {
        byte[] checksums = readResource(CHECKSUM_MANIFEST, "checksum manifest");
        byte[] sizesRaw = readResource(SIZE_MANIFEST, "size manifest");
        requireDigest("checksum manifest", checksums, RESOURCE_MANIFEST_SHA256);
        requireDigest("size manifest", sizesRaw, RESOURCE_SIZES_MANIFEST_SHA256);

        Map<String, String> digests = parseChecksums(checksums);
        Map<String, SizeDigest> sizesAndDigests = parseSizes(sizesRaw);
        Map<String, Long> sizes = new LinkedHashMap<>();
        sizesAndDigests.forEach((path, value) -> {
            if (!value.digest().equals(digests.get(path))) {
                throw new IllegalStateException("native structural manifests disagree");
            }
            sizes.put(path, value.size());
        });
        validateManifest(digests, sizes);
        return new ManifestData(
                Collections.unmodifiableMap(digests),
                Collections.unmodifiableMap(sizes)
        );
    }

    private static byte[] readResource(String path, String label) {
        try (InputStream input = Ae219217NativeStructuralProfile.class.getResourceAsStream(path)) {
            if (input == null) {
                throw new IllegalStateException("missing native structural " + label);
            }
            return input.readAllBytes();
        } catch (IOException exception) {
            throw new IllegalStateException("failed to read native structural " + label, exception);
        }
    }

    private static Map<String, String> parseChecksums(byte[] raw) {
        Map<String, String> values = new LinkedHashMap<>();
        readLines(raw, line -> {
            String[] fields = line.split("  ", -1);
            if (fields.length != 2
                    || !fields[0].matches("[0-9a-f]{64}")
                    || fields[1].isEmpty()) {
                throw new IllegalStateException("malformed native structural checksum manifest");
            }
            putSorted(values, fields[1], fields[0], "checksum");
        });
        return values;
    }

    private static Map<String, SizeDigest> parseSizes(byte[] raw) {
        Map<String, SizeDigest> values = new LinkedHashMap<>();
        readLines(raw, line -> {
            String[] fields = line.split("\\t", -1);
            if (fields.length != 3
                    || fields[0].isEmpty()
                    || !fields[1].matches("[1-9][0-9]*")
                    || !fields[2].matches("[0-9a-f]{64}")) {
                throw new IllegalStateException("malformed native structural size manifest");
            }
            long size;
            try {
                size = Long.parseLong(fields[1]);
            } catch (NumberFormatException exception) {
                throw new IllegalStateException("invalid native structural resource size", exception);
            }
            putSorted(values, fields[0], new SizeDigest(size, fields[2]), "size");
        });
        return values;
    }

    private static <T> void putSorted(
            Map<String, T> values,
            String path,
            T value,
            String label
    ) {
        if (!values.isEmpty()) {
            String previous = values.keySet().stream().reduce((first, second) -> second).orElseThrow();
            if (previous.compareTo(path) >= 0) {
                throw new IllegalStateException("unsorted native structural " + label + " manifest");
            }
        }
        if (values.put(path, value) != null) {
            throw new IllegalStateException("duplicate native structural " + label + " path");
        }
    }

    private static void readLines(byte[] raw, LineConsumer consumer) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                new ByteArrayInputStream(raw), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.isBlank()) {
                    consumer.accept(line);
                }
            }
        } catch (IOException exception) {
            throw new IllegalStateException("failed to parse native structural manifest", exception);
        }
    }

    private static void validateManifest(
            Map<String, String> digests,
            Map<String, Long> sizes
    ) {
        long jsonCount = digests.keySet().stream().filter(path -> path.endsWith(".json")).count();
        long pngCount = digests.keySet().stream().filter(path -> path.endsWith(".png")).count();
        if (digests.size() != REQUIRED_RESOURCE_COUNT
                || sizes.size() != REQUIRED_RESOURCE_COUNT
                || !digests.keySet().equals(sizes.keySet())
                || jsonCount != TRANSITIVE_JSON_RESOURCE_COUNT
                || pngCount != PNG_RESOURCE_COUNT
                || sizes.values().stream().mapToLong(Long::longValue).sum()
                        != REQUIRED_RESOURCE_BYTES
                || digests.keySet().stream().anyMatch(path -> !path.startsWith("assets/ae2/"))) {
            throw new IllegalStateException("invalid native structural resource closure");
        }
        if (!new LinkedHashSet<>(NATIVE_FACE_PART_IDS).equals(NATIVE_FACE_PART_MODELS.keySet())
                || !NATIVE_FACE_PART_MODELS.keySet().equals(NATIVE_FACE_PART_GROUPS.keySet())
                || NATIVE_FACE_PART_IDS.size() != FACE_PART_COUNT
                || SPIN_CAPABLE_PART_IDS.size() != SPIN_CAPABLE_PART_COUNT
                || SMART_CORE_PART_IDS.size() != SMART_CORE_PART_COUNT
                || !NATIVE_FACE_PART_IDS.containsAll(SMART_CORE_PART_IDS)
                || FACADE_WHITELIST_BLOCK_IDS.size() != FACADE_WHITELIST_BLOCK_COUNT
                || new LinkedHashSet<>(FACADE_WHITELIST_BLOCK_IDS).size()
                        != FACADE_WHITELIST_BLOCK_COUNT
                || FACADE_WHITELIST_OPTIONAL_TAGS.size()
                        != FACADE_WHITELIST_OPTIONAL_TAG_COUNT
                || NATIVE_FACADE_NEUTRAL_MATERIALS.size()
                        != NATIVE_FACADE_NEUTRAL_MATERIAL_COUNT
                || !FACADE_WHITELIST_BLOCK_IDS.containsAll(
                        NATIVE_FACADE_NEUTRAL_MATERIALS.keySet())
                || FACADE_WHITELIST_NEUTRAL_STATES.size()
                        != FACADE_WHITELIST_NEUTRAL_STATE_COUNT
                || !new LinkedHashSet<>(FACADE_WHITELIST_BLOCK_IDS).equals(
                        FACADE_WHITELIST_NEUTRAL_STATES.keySet())
                || !facadeWhitelistStateSchemasValid()
                || !FACADE_WHITELIST_NEUTRAL_STATES.entrySet().stream()
                        .filter(entry -> !entry.getValue().solidRender())
                        .map(Map.Entry::getKey)
                        .toList()
                        .equals(List.of(
                                "ae2:quartz_glass",
                                "ae2:quartz_vibrant_glass",
                                "minecraft:honey_block"
                        ))
                || FACADE_WHITELIST_SAME_STATE_SKIP_RENDERING.size()
                        != FACADE_WHITELIST_SAME_STATE_SKIP_RENDERING_COUNT
                || !new LinkedHashSet<>(FACADE_WHITELIST_BLOCK_IDS).equals(
                        FACADE_WHITELIST_SAME_STATE_SKIP_RENDERING.keySet())
                || !FACADE_WHITELIST_SAME_STATE_SKIP_RENDERING.entrySet().stream()
                        .filter(Map.Entry::getValue)
                        .map(Map.Entry::getKey)
                        .toList()
                        .equals(List.of(
                                "ae2:quartz_glass",
                                "ae2:quartz_vibrant_glass",
                                "minecraft:honey_block"
                        ))
                || FACADE_ORDINARY_SKIP_RENDERING_CONTROLS.size()
                        != FACADE_ORDINARY_SKIP_RENDERING_CONTROL_COUNT
                || !FACADE_ORDINARY_SKIP_RENDERING_CONTROLS.equals(Map.of(
                        "minecraft:glass", true,
                        "minecraft:oak_log", false,
                        "minecraft:oak_leaves", false
                ))
                || FACADE_CORNER_KICK_RUNTIME_EPSILON_SIXTEENTHS
                        != FACADE_CORNER_KICK_SOURCE_EPSILON_BLOCKS * 16D
                || FACADE_CORNER_KICK_ANALYZER_EPSILON_BLOCKS
                        != FACADE_CORNER_KICK_SOURCE_EPSILON_BLOCKS
                || PLANE_CONNECTION_MASK_BITS.size() != 4
                || PLANE_CONNECTION_MASK_BITS.values().stream()
                        .mapToInt(Integer::intValue).sum() != PLANE_CONNECTION_MASK_COUNT - 1
                || !new LinkedHashSet<>(NATIVE_FACE_PART_IDS)
                        .equals(NATIVE_FACE_PART_COLLISION_BOXES.keySet())
                || !NATIVE_FACE_PART_COLLISION_BOXES.keySet().equals(
                        NATIVE_FACE_PART_COLLISION_MODES.keySet())
                || NATIVE_FACE_PART_COLLISION_BOXES.values().stream()
                        .anyMatch(List::isEmpty)
                || NATIVE_ENDPOINT_IDS.size() != NATIVE_ENDPOINT_COUNT
                || KNOWN_UNSUPPORTED_COMPATIBLE_ENDPOINT_IDS.size()
                        != KNOWN_UNSUPPORTED_COMPATIBLE_ENDPOINT_COUNT
                || new LinkedHashSet<>(KNOWN_UNSUPPORTED_COMPATIBLE_ENDPOINT_IDS).size()
                        != KNOWN_UNSUPPORTED_COMPATIBLE_ENDPOINT_COUNT
                || !new LinkedHashSet<>(KNOWN_UNSUPPORTED_COMPATIBLE_ENDPOINT_IDS).equals(
                        KNOWN_UNSUPPORTED_COMPATIBLE_ENDPOINT_BLOCK_ENTITY_IDS.keySet())
                || KNOWN_UNSUPPORTED_COMPATIBLE_ENDPOINT_ARTIFACT_COUNTS.values().stream()
                        .mapToInt(Integer::intValue).sum()
                        != KNOWN_UNSUPPORTED_COMPATIBLE_ENDPOINT_COUNT
                || KNOWN_UNSUPPORTED_COMPATIBLE_ENDPOINT_IDS.stream()
                        .anyMatch(NATIVE_ENDPOINT_IDS::contains)
                || !new LinkedHashSet<>(NATIVE_ENDPOINT_IDS)
                        .equals(NATIVE_ENDPOINT_CABLE_TYPES.keySet())
                || !NATIVE_ENDPOINT_CABLE_TYPES.keySet().equals(NATIVE_ENDPOINT_CLASSES.keySet())
                || !NATIVE_ENDPOINT_CABLE_TYPES.keySet().equals(
                        NATIVE_ENDPOINT_BLOCK_ENTITY_IDS.keySet())
                || !NATIVE_ENDPOINT_CABLE_TYPES.keySet().equals(
                        NATIVE_ENDPOINT_SIDE_RULES.keySet())
                || NATIVE_ENDPOINT_STATE_SCHEMAS.size() != ENDPOINT_STATE_SCHEMA_COUNT
                || stateCombinationCount(NATIVE_ENDPOINT_STATE_SCHEMAS)
                        != ENDPOINT_STATE_COMBINATION_COUNT
                || ENDPOINT_STATE_COMBINATION_COUNT * FACE_COUNT
                        != ENDPOINT_STATE_SIDE_COMBINATION_COUNT
                || !NATIVE_ENDPOINT_CABLE_TYPES.keySet().equals(
                        NATIVE_ENDPOINT_STATE_SCHEMAS.keySet())
                || !NATIVE_ENDPOINT_CABLE_TYPES.keySet().equals(
                        NATIVE_ENDPOINT_BLOCKSTATE_SHA256.keySet())
                || NATIVE_ENDPOINT_STATE_SCHEMAS.values().stream()
                        .flatMap(schema -> schema.entrySet().stream())
                        .anyMatch(entry -> entry.getKey().isBlank()
                                || entry.getValue().isEmpty()
                                || entry.getValue().stream().anyMatch(String::isBlank)
                                || new LinkedHashSet<>(entry.getValue()).size()
                                        != entry.getValue().size())
                || NATIVE_ENDPOINT_BLOCKSTATE_SHA256.values().stream()
                        .anyMatch(digest -> !digest.matches("[0-9a-f]{64}"))
                || NATIVE_ENDPOINT_SIDE_RULE_COUNTS.size() != ENDPOINT_SIDE_RULE_KIND_COUNT
                || NATIVE_ENDPOINT_SIDE_RULE_COUNTS.values().stream()
                        .mapToInt(Integer::intValue).sum() != NATIVE_ENDPOINT_COUNT) {
            throw new IllegalStateException("invalid native structural catalog closure");
        }
    }

    private static boolean facadeWhitelistStateSchemasValid() {
        if (FACADE_WHITELIST_STATE_SCHEMAS.size()
                != FACADE_WHITELIST_STATE_SCHEMA_COUNT
                || !new LinkedHashSet<>(FACADE_WHITELIST_BLOCK_IDS).equals(
                        FACADE_WHITELIST_STATE_SCHEMAS.keySet())
                || !FACADE_WHITELIST_STATE_SCHEMAS.keySet().equals(
                        FACADE_WHITELIST_BLOCKSTATE_SHA256.keySet())) {
            return false;
        }
        int combinationCount = 0;
        int solidRenderTrueStateCount = 0;
        int skipRenderingTrueStateCount = 0;
        for (String blockId : FACADE_WHITELIST_BLOCK_IDS) {
            Map<String, List<String>> schema = FACADE_WHITELIST_STATE_SCHEMAS.get(blockId);
            FacadeNeutralState neutral = FACADE_WHITELIST_NEUTRAL_STATES.get(blockId);
            String digest = FACADE_WHITELIST_BLOCKSTATE_SHA256.get(blockId);
            if (schema == null || neutral == null || digest == null
                    || !digest.matches("[0-9a-f]{64}")
                    || !schema.keySet().equals(neutral.properties().keySet())) {
                return false;
            }
            for (Map.Entry<String, List<String>> entry : schema.entrySet()) {
                if (entry.getKey().isBlank()
                        || entry.getValue().isEmpty()
                        || entry.getValue().stream().anyMatch(String::isBlank)
                        || new LinkedHashSet<>(entry.getValue()).size()
                                != entry.getValue().size()
                        || !entry.getValue().contains(
                                neutral.properties().get(entry.getKey()))) {
                    return false;
                }
            }
            NeutralFacadeMaterial nativeMaterial = NATIVE_FACADE_NEUTRAL_MATERIALS.get(
                    blockId
            );
            if (nativeMaterial != null
                    && (!schema.equals(nativeMaterial.validPropertyValues())
                    || !digest.equals(nativeMaterial.blockstateSha256()))) {
                return false;
            }
            int familyCombinationCount = 1;
            for (List<String> values : schema.values()) {
                familyCombinationCount = Math.multiplyExact(
                        familyCombinationCount, values.size()
                );
            }
            combinationCount = Math.addExact(combinationCount, familyCombinationCount);
            if (neutral.solidRender()) {
                solidRenderTrueStateCount = Math.addExact(
                        solidRenderTrueStateCount, familyCombinationCount
                );
            }
            if (FACADE_WHITELIST_SAME_STATE_SKIP_RENDERING.get(blockId)) {
                skipRenderingTrueStateCount = Math.addExact(
                        skipRenderingTrueStateCount, familyCombinationCount
                );
            }
        }
        return combinationCount == FACADE_WHITELIST_STATE_COMBINATION_COUNT
                && solidRenderTrueStateCount
                        == FACADE_WHITELIST_SOLID_RENDER_TRUE_STATE_COUNT
                && skipRenderingTrueStateCount
                        == FACADE_WHITELIST_SAME_STATE_SKIP_RENDERING_TRUE_STATE_COUNT;
    }

    private static int stateCombinationCount(
            Map<String, Map<String, List<String>>> schemas
    ) {
        int result = 0;
        for (Map<String, List<String>> schema : schemas.values()) {
            int combinations = 1;
            for (List<String> values : schema.values()) {
                combinations = Math.multiplyExact(combinations, values.size());
            }
            result = Math.addExact(result, combinations);
        }
        return result;
    }

    private static void requireDigest(String label, byte[] content, String expected) {
        if (!sha256(content).equals(expected)) {
            throw new IllegalStateException("native structural " + label + " changed");
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

    @FunctionalInterface
    private interface LineConsumer {
        void accept(String line);
    }

    private record SizeDigest(long size, String digest) {
    }

    private record ManifestData(Map<String, String> digests, Map<String, Long> sizes) {
    }

    private record PropertyDomain(String name, List<String> values) {
        private PropertyDomain {
            values = List.copyOf(values);
        }
    }

    /** Installed-face-local visual collision box in exact sixteenth coordinates. */
    public record PartBox(
            int minX,
            int minY,
            int minZ,
            int maxX,
            int maxY,
            int maxZ
    ) {
        public PartBox {
            if (minX < 0 || minY < 0 || minZ < 0
                    || maxX > 16 || maxY > 16 || maxZ > 16
                    || minX >= maxX || minY >= maxY || minZ >= maxZ) {
                throw new IllegalArgumentException("invalid part collision box");
            }
        }
    }

    /** Exact neutral/static facade state for one native whitelisted material family. */
    public record NeutralFacadeMaterial(
            Map<String, String> properties,
            String materialFamily,
            String sourceModel,
            String blockstateSha256,
            boolean solidRender,
            int blockStateLightEmission,
            int facadeQuadLightEmission,
            Map<String, List<String>> validPropertyValues,
            Map<String, String> normalization
    ) {
        public NeutralFacadeMaterial {
            properties = Map.copyOf(properties);
            Map<String, List<String>> copiedValues = new LinkedHashMap<>();
            validPropertyValues.forEach((key, values) ->
                    copiedValues.put(key, List.copyOf(values)));
            validPropertyValues = Collections.unmodifiableMap(copiedValues);
            normalization = Map.copyOf(normalization);
            if (materialFamily == null || sourceModel == null
                    || blockstateSha256 == null || blockstateSha256.length() != 64
                    || blockStateLightEmission < 0 || blockStateLightEmission > 15
                    || facadeQuadLightEmission < 0 || facadeQuadLightEmission > 15
                    || !validPropertyValues.keySet().equals(normalization.keySet())) {
                throw new IllegalArgumentException("invalid neutral facade material");
            }
        }
    }

    /** Pinned neutral/default state and exact Minecraft solid-render classification. */
    public record FacadeNeutralState(Map<String, String> properties, boolean solidRender) {
        public FacadeNeutralState {
            properties = Map.copyOf(properties);
        }
    }
}
