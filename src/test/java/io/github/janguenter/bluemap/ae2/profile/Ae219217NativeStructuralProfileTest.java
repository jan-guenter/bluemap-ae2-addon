/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.profile;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Ae219217NativeStructuralProfileTest {

    @Test
    void publicRouteIdentityAndPoliciesAreExact() {
        assertEquals(10, Ae219217NativeStructuralProfile.SCHEMA_VERSION);
        assertEquals(
                "ae2-cable-bus-structural",
                Ae219217NativeStructuralProfile.PROFILE_ID
        );
        assertEquals(
                "/bluemap-ae2/profiles/ae2/19.2.17/routes/cable-bus-structural/",
                Ae219217NativeStructuralProfile.RESOURCE_ROOT
        );
        assertEquals("All the Mons", Ae219217NativeStructuralProfile.PACK_NAME);
        assertEquals("1.2.0", Ae219217NativeStructuralProfile.PACK_VERSION);
        assertEquals(
                "c7bb230f21d14d26859d0b92548f089b3a493ad9",
                Ae219217NativeStructuralProfile.PACK_COMMIT
        );
        assertEquals("1.21.1", Ae219217NativeStructuralProfile.MINECRAFT_VERSION);
        assertEquals("21.1.248", Ae219217NativeStructuralProfile.NEOFORGE_VERSION);
        assertEquals(Ae219217Profile.JAR_SHA256,
                Ae219217NativeStructuralProfile.JAR_SHA256);
        assertEquals(
                "79ee2c704ad62941a426c26b1cb1f76ef5b2ee5a",
                Ae219217NativeStructuralProfile.SOURCE_COMMIT
        );
        assertEquals(
                "static-off-inactive-unlocked",
                Ae219217NativeStructuralProfile.TRANSIENT_POLICY
        );
        assertEquals(
                "all-six-face-masks-per-instance-valid-static-block-state-material",
                Ae219217NativeStructuralProfile.FACADE_POLICY
        );
        assertEquals(
                "missing-malformed-or-capped-atomic-original-resource-fallback",
                Ae219217NativeStructuralProfile.FALLBACK_POLICY
        );
        assertEquals(
                "malformed-known-extension-observation-atomic-original-resource-fallback",
                Ae219217NativeStructuralProfile.MALFORMED_KNOWN_EXTENSION_ENDPOINT_POLICY
        );
    }

    @Test
    void nativeFacePartCatalogIsOrderedClosedAndDenseOnlyForAnchor() {
        assertEquals(29, Ae219217NativeStructuralProfile.NATIVE_FACE_PART_IDS.size());
        assertEquals(
                Ae219217NativeStructuralProfile.FACE_PART_COUNT,
                Set.copyOf(Ae219217NativeStructuralProfile.NATIVE_FACE_PART_IDS).size()
        );
        assertEquals("ae2:quartz_fiber",
                Ae219217NativeStructuralProfile.NATIVE_FACE_PART_IDS.get(0));
        assertEquals("ae2:light_p2p_tunnel",
                Ae219217NativeStructuralProfile.NATIVE_FACE_PART_IDS.get(28));
        assertEquals(
                new LinkedHashSet<>(Ae219217NativeStructuralProfile.NATIVE_FACE_PART_IDS),
                Ae219217NativeStructuralProfile.NATIVE_FACE_PART_MODELS.keySet()
        );
        assertEquals(
                Set.of("ae2:cable_anchor"),
                Ae219217NativeStructuralProfile.DENSE_CAPABLE_PART_IDS
        );
        for (String id : Ae219217NativeStructuralProfile.NATIVE_FACE_PART_IDS) {
            assertTrue(Ae219217NativeStructuralProfile.isNativeFacePart(id));
            assertEquals(
                    id.equals("ae2:cable_anchor"),
                    Ae219217NativeStructuralProfile.supportsDenseCenter(id)
            );
        }
        assertFalse(Ae219217NativeStructuralProfile.isNativeFacePart("ae2:unknown"));
    }

    @Test
    void exactSpinCatalogProducesAll336OrientationStates() {
        assertEquals(9, Ae219217NativeStructuralProfile.SPIN_CAPABLE_PART_IDS.size());
        assertEquals(Set.of(
                "ae2:monitor",
                "ae2:semi_dark_monitor",
                "ae2:dark_monitor",
                "ae2:pattern_encoding_terminal",
                "ae2:crafting_terminal",
                "ae2:terminal",
                "ae2:storage_monitor",
                "ae2:conversion_monitor",
                "ae2:pattern_access_terminal"
        ), Ae219217NativeStructuralProfile.SPIN_CAPABLE_PART_IDS);
        int spinIgnored = Ae219217NativeStructuralProfile.FACE_PART_COUNT
                - Ae219217NativeStructuralProfile.SPIN_CAPABLE_PART_COUNT;
        assertEquals(
                336,
                spinIgnored * Ae219217NativeStructuralProfile.FACE_COUNT
                        + Ae219217NativeStructuralProfile.SPIN_CAPABLE_PART_COUNT
                        * Ae219217NativeStructuralProfile.FACE_COUNT
                        * Ae219217NativeStructuralProfile.SPIN_COUNT
        );
        assertEquals(336, Ae219217NativeStructuralProfile.ORIENTATION_STATE_COUNT);
    }

    @Test
    void exactDesiredCoreOverridesAndPlaneCutoutMaskAreSourceBound() {
        assertEquals(Set.of(
                "ae2:level_emitter",
                "ae2:energy_level_emitter"
        ), Ae219217NativeStructuralProfile.SMART_CORE_PART_IDS);
        assertEquals(2, Ae219217NativeStructuralProfile.SMART_CORE_PART_COUNT);
        assertTrue(Ae219217NativeStructuralProfile.requestsSmartCore(
                "ae2:level_emitter"));
        assertFalse(Ae219217NativeStructuralProfile.requestsSmartCore("ae2:terminal"));
        assertEquals(Map.of(
                "left", 1,
                "down", 2,
                "right", 4,
                "up", 8
        ), Ae219217NativeStructuralProfile.PLANE_CONNECTION_MASK_BITS);
        assertEquals(16, Ae219217NativeStructuralProfile.PLANE_CONNECTION_MASK_COUNT);
        assertEquals(Map.of(
                "down", Map.of(
                        "minX", "right", "maxX", "left", "minY", "down", "maxY", "up"
                ),
                "up", Map.of(
                        "minX", "left", "maxX", "right", "minY", "up", "maxY", "down"
                ),
                "north", Map.of(
                        "minX", "left", "maxX", "right", "minY", "down", "maxY", "up"
                ),
                "south", Map.of(
                        "minX", "left", "maxX", "right", "minY", "down", "maxY", "up"
                ),
                "west", Map.of(
                        "minX", "right", "maxX", "left", "minY", "down", "maxY", "up"
                ),
                "east", Map.of(
                        "minX", "right", "maxX", "left", "minY", "down", "maxY", "up"
                )
        ), Ae219217NativeStructuralProfile.planeCollisionBoundBitsByFace());
        assertEquals(Map.of(
                "minX", "right", "maxX", "left", "minY", "down", "maxY", "up"
        ), Ae219217NativeStructuralProfile.planeRenderBoundBits());
        assertEquals(
                "mask-bits-are-PlaneConnections-front-view-logical;" +
                        "renderedGeometryBoundBits-are-PlaneBakedModel-visual-local-before-" +
                        "QuadRotator-installed-world-transform;" +
                        "collisionBoundBitsByInstalledFace-are-BusCollisionHelper-" +
                        "installed-face-local;never-reuse-bounds-across-coordinate-spaces",
                Ae219217NativeStructuralProfile.PLANE_COORDINATE_SPACE_POLICY
        );
    }

    @Test
    void exactPartCollisionBoxesDriveFacadeCutoutUnion() {
        assertEquals(
                new LinkedHashSet<>(Ae219217NativeStructuralProfile.NATIVE_FACE_PART_IDS),
                Ae219217NativeStructuralProfile.NATIVE_FACE_PART_COLLISION_BOXES.keySet()
        );
        assertEquals(List.of(new Ae219217NativeStructuralProfile.PartBox(
                6, 6, 10, 10, 10, 16
        )), Ae219217NativeStructuralProfile.facePartCollisionBoxes("ae2:quartz_fiber"));
        assertEquals(List.of(new Ae219217NativeStructuralProfile.PartBox(
                7, 7, 10, 9, 9, 14
        )), Ae219217NativeStructuralProfile.cableAnchorCollisionBoxes(true));
        assertEquals(List.of(
                new Ae219217NativeStructuralProfile.PartBox(5, 5, 14, 11, 11, 15),
                new Ae219217NativeStructuralProfile.PartBox(1, 0, 15, 16, 15, 16)
        ), Ae219217NativeStructuralProfile.planeCollisionBoxes("down", 3));
        assertEquals(List.of(
                new Ae219217NativeStructuralProfile.PartBox(5, 5, 14, 11, 11, 15),
                new Ae219217NativeStructuralProfile.PartBox(0, 0, 15, 16, 16, 16)
        ), Ae219217NativeStructuralProfile.planeCollisionBoxes("east", 15));
        assertThrows(IllegalArgumentException.class,
                () -> Ae219217NativeStructuralProfile.planeCollisionBoxes(null, 0));
        assertThrows(IllegalArgumentException.class,
                () -> Ae219217NativeStructuralProfile.planeCollisionBoxes("UP", 0));
        assertThrows(IllegalArgumentException.class,
                () -> Ae219217NativeStructuralProfile.planeCollisionBoxes("diagonal", 0));
        assertThrows(IllegalArgumentException.class,
                () -> Ae219217NativeStructuralProfile.planeCollisionBoxes("north", -1));
        assertThrows(IllegalArgumentException.class,
                () -> Ae219217NativeStructuralProfile.planeCollisionBoxes("north", 16));
        assertEquals(
                "union-of-every-installed-part-box-intersecting-current-facade-slab",
                Ae219217NativeStructuralProfile.FACADE_CUTOUT_POLICY
        );
    }

    @Test
    void allSixFacesAndSixteenPlaneMasksUseExactCollisionHelperAxes() {
        Map<String, ExpectedPlaneCollisionBits> expected = Map.of(
                "down", new ExpectedPlaneCollisionBits(4, 1, 2, 8),
                "up", new ExpectedPlaneCollisionBits(1, 4, 8, 2),
                "north", new ExpectedPlaneCollisionBits(1, 4, 2, 8),
                "south", new ExpectedPlaneCollisionBits(1, 4, 2, 8),
                "west", new ExpectedPlaneCollisionBits(4, 1, 2, 8),
                "east", new ExpectedPlaneCollisionBits(4, 1, 2, 8)
        );
        for (Map.Entry<String, ExpectedPlaneCollisionBits> entry : expected.entrySet()) {
            for (int mask = 0; mask < 16; mask++) {
                ExpectedPlaneCollisionBits bits = entry.getValue();
                List<Ae219217NativeStructuralProfile.PartBox> expectedBoxes = List.of(
                        new Ae219217NativeStructuralProfile.PartBox(
                                5, 5, 14, 11, 11, 15
                        ),
                        new Ae219217NativeStructuralProfile.PartBox(
                                (mask & bits.minX()) == 0 ? 1 : 0,
                                (mask & bits.minY()) == 0 ? 1 : 0,
                                15,
                                (mask & bits.maxX()) == 0 ? 15 : 16,
                                (mask & bits.maxY()) == 0 ? 15 : 16,
                                16
                        )
                );
                assertEquals(
                        expectedBoxes,
                        Ae219217NativeStructuralProfile.planeCollisionBoxes(
                                entry.getKey(),
                                mask
                        ),
                        entry.getKey() + " mask=" + mask
                );
            }
        }
    }

    @Test
    void exactFacadeWhitelistAndNativeNeutralFamiliesAreSourceBound() {
        assertEquals(24, Ae219217NativeStructuralProfile.facadeWhitelistBlockIds().size());
        assertEquals(List.of("c:glass_blocks"),
                Ae219217NativeStructuralProfile.facadeWhitelistOptionalTags());
        assertEquals(List.of(
                "ae2:quartz_glass",
                "ae2:quartz_vibrant_glass",
                "ae2:controller",
                "ae2:1k_crafting_storage",
                "ae2:4k_crafting_storage",
                "ae2:16k_crafting_storage",
                "ae2:64k_crafting_storage",
                "ae2:256k_crafting_storage",
                "ae2:crafting_monitor",
                "ae2:crafting_unit",
                "ae2:crafting_accelerator"
        ), List.copyOf(Ae219217NativeStructuralProfile
                .nativeFacadeNeutralMaterials().keySet()));
        assertEquals(Ae219217NativeStructuralProfile.FACADE_WHITELIST_BLOCK_IDS,
                List.copyOf(Ae219217NativeStructuralProfile
                        .facadeWhitelistNeutralStates().keySet()));
        assertEquals(Ae219217NativeStructuralProfile.FACADE_WHITELIST_BLOCK_IDS,
                List.copyOf(Ae219217NativeStructuralProfile
                        .facadeWhitelistStateSchemas().keySet()));
        assertEquals(Ae219217NativeStructuralProfile.FACADE_WHITELIST_BLOCK_IDS,
                List.copyOf(Ae219217NativeStructuralProfile
                        .facadeWhitelistBlockstateSha256().keySet()));
        assertEquals(List.of("down", "up", "north", "south", "west", "east"),
                Ae219217NativeStructuralProfile
                        .facadeWhitelistStateSchema("minecraft:dispenser").get("facing"));
        assertEquals(List.of(
                "down_east", "down_north", "down_south", "down_west",
                "up_east", "up_north", "up_south", "up_west",
                "west_up", "east_up", "north_up", "south_up"
        ), Ae219217NativeStructuralProfile
                .facadeWhitelistStateSchema("minecraft:crafter").get("orientation"));
        assertEquals(Map.of(), Ae219217NativeStructuralProfile
                .facadeWhitelistStateSchema("minecraft:honey_block"));
        assertNull(Ae219217NativeStructuralProfile
                .facadeWhitelistStateSchema("minecraft:glass"));
        assertEquals(
                "780ffcffff91d90efe172f2f1f200a06dcbe885fe5316d16bebf72bae2ef7c44",
                Ae219217NativeStructuralProfile
                        .facadeWhitelistBlockstateSha256("minecraft:honey_block")
        );
        assertNull(Ae219217NativeStructuralProfile
                .facadeWhitelistBlockstateSha256("minecraft:glass"));
        assertEquals(
                "all-24-explicit-whitelist-families-require-exact-complete-persisted-" +
                        "property-key-set-and-value-domains;13-vanilla-families-preserve-" +
                        "valid-state;11-ae2-native-families-apply-declared-static-" +
                        "normalization;extra-missing-or-invalid-properties-atomic-" +
                        "original-resource-fallback",
                Ae219217NativeStructuralProfile.FACADE_WHITELIST_STATE_POLICY
        );
        assertEquals(
                "solidRender-and-same-state-skipRendering-family-invariant-across-all-" +
                        "554-valid-explicit-whitelist-states;neutral-default-row-booleans-" +
                        "apply-to-whole-family;classification-drift-atomic-original-" +
                        "resource-fallback",
                Ae219217NativeStructuralProfile
                        .FACADE_WHITELIST_STATE_CLASSIFICATION_POLICY
        );
        List<Integer> facadeStateCardinalities = Ae219217NativeStructuralProfile
                .facadeWhitelistStateSchemas().values().stream()
                .map(Ae219217NativeStructuralProfileTest::stateCardinality)
                .toList();
        assertEquals(List.of(
                1, 1, 256, 2, 8, 8, 12, 12, 48, 12, 24, 24, 2, 1, 1,
                18, 4, 4, 4, 4, 4, 96, 4, 4
        ), facadeStateCardinalities);
        assertEquals(
                Ae219217NativeStructuralProfile.FACADE_WHITELIST_STATE_COMBINATION_COUNT,
                facadeStateCardinalities.stream().mapToInt(Integer::intValue).sum()
        );
        assertEquals(551,
                Ae219217NativeStructuralProfile
                        .FACADE_WHITELIST_SOLID_RENDER_TRUE_STATE_COUNT);
        assertEquals(3,
                Ae219217NativeStructuralProfile
                        .FACADE_WHITELIST_SAME_STATE_SKIP_RENDERING_TRUE_STATE_COUNT);
        assertEquals(List.of(
                "ae2:quartz_glass",
                "ae2:quartz_vibrant_glass",
                "minecraft:honey_block"
        ), Ae219217NativeStructuralProfile.facadeWhitelistNeutralStates()
                .entrySet().stream()
                .filter(entry -> !entry.getValue().solidRender())
                .map(Map.Entry::getKey)
                .toList());
        assertEquals(Map.of(), Ae219217NativeStructuralProfile
                .facadeWhitelistNeutralStates().get("minecraft:soul_sand").properties());
        assertTrue(Ae219217NativeStructuralProfile.facadeWhitelistNeutralStates()
                .get("minecraft:soul_sand").solidRender());
        assertFalse(Ae219217NativeStructuralProfile.facadeWhitelistNeutralStates()
                .get("minecraft:honey_block").solidRender());
        assertEquals(24,
                Ae219217NativeStructuralProfile
                        .facadeWhitelistSameStateSkipRendering().size());
        assertEquals(List.of(
                "ae2:quartz_glass",
                "ae2:quartz_vibrant_glass",
                "minecraft:honey_block"
        ), Ae219217NativeStructuralProfile.facadeWhitelistSameStateSkipRendering()
                .entrySet().stream()
                .filter(Map.Entry::getValue)
                .map(Map.Entry::getKey)
                .toList());
        assertTrue(Ae219217NativeStructuralProfile
                .facadeWhitelistSameStateSkipRendering("minecraft:honey_block"));
        assertFalse(Ae219217NativeStructuralProfile
                .facadeWhitelistSameStateSkipRendering("minecraft:soul_sand"));
        assertNull(Ae219217NativeStructuralProfile
                .facadeWhitelistSameStateSkipRendering("minecraft:glass"));
        assertEquals(Map.of(
                "minecraft:glass", true,
                "minecraft:oak_log", false,
                "minecraft:oak_leaves", false
        ), Ae219217NativeStructuralProfile.facadeOrdinarySkipRenderingControls());
        assertTrue(Ae219217NativeStructuralProfile
                .facadeOrdinarySkipRenderingControl("minecraft:glass"));
        assertFalse(Ae219217NativeStructuralProfile
                .facadeOrdinarySkipRenderingControl("minecraft:oak_leaves"));
        assertNull(Ae219217NativeStructuralProfile
                .facadeOrdinarySkipRenderingControl("minecraft:stone"));
        assertEquals(Map.of(
                "crafting", "false",
                "orientation", "north_up",
                "triggered", "false"
        ), Ae219217NativeStructuralProfile.facadeWhitelistNeutralStates()
                .get("minecraft:crafter").properties());
        assertEquals(Map.of("state", "offline", "type", "block"),
                Ae219217NativeStructuralProfile.nativeFacadeNeutralMaterials()
                        .get("ae2:controller").properties());
        assertEquals(Map.of(
                "facing", "north",
                "formed", "false",
                "powered", "false",
                "spin", "0"
        ),
                Ae219217NativeStructuralProfile.nativeFacadeNeutralMaterials()
                        .get("ae2:crafting_monitor").properties());
        assertEquals("facade-aware-connected-quartz-glass-static",
                Ae219217NativeStructuralProfile.nativeFacadeNeutralMaterials()
                        .get("ae2:quartz_vibrant_glass").materialFamily());
        assertEquals(
                "source-shade-bit-semantic-locked-host-prbm-has-no-per-quad-shade-channel",
                Ae219217NativeStructuralProfile.FACADE_DIRECTIONAL_SHADE_POLICY
        );
        assertEquals(
                "BlueMap-ResourceModelRenderer-source-faceDir-rotated-by-blockstate-" +
                        "variant-only;element-rotation-affects-vertices-not-AO-direction;" +
                        "runtime-uses-layer-lightFace-not-quad-nominal-face",
                Ae219217NativeStructuralProfile
                        .FACADE_AMBIENT_OCCLUSION_DIRECTION_POLICY
        );
        assertEquals(
                "BlueMap-map-color-illumination-uses-original-center-and-outward-world-" +
                        "light-only;element-lightEmission-affects-triangle-blocklight-not-" +
                        "map-color-brightness",
                Ae219217NativeStructuralProfile.STRUCTURAL_MAP_COLOR_ILLUMINATION_POLICY
        );
        assertEquals(
                "untinted-or-one-distinct-nonnegative-source-tint-index;" +
                        "untinted-layers-may-coexist;shared-tinted-layers-use-host-block-" +
                        "color-calculator;mixed-nonnegative-tint-indices-atomic-original-" +
                        "resource-fallback",
                Ae219217NativeStructuralProfile.FACADE_TINT_POLICY
        );
        assertEquals(
                "optional-c-glass-blocks-and-ordinary-FacadeItem-eligible-states-require-" +
                        "one-live-unrotated-0-to-16-six-face-full-cube-witness;bounded-" +
                        "additional-static-elements-and-multipart-source-quads-subject-to-" +
                        "uv-tint-weighted-and-semantic-resource-gates;otherwise-valid-" +
                        "complex-static-models-atomic-original-resource-fallback",
                Ae219217NativeStructuralProfile.FACADE_ORDINARY_MATERIAL_POLICY
        );
        assertEquals(
                "source-QuadReInterpolator-nominal-face-2d-dx-dy-bilinear;" +
                        "admitted-quad-projection-requires-exact-complete-InterpHelper-grid;" +
                        "post-clamp-and-corner-kick-target-uses-projected-dx-dy;" +
                        "noncompatible-projected-quads-atomic-original-resource-fallback",
                Ae219217NativeStructuralProfile.FACADE_UV_REINTERPOLATION_POLICY
        );
        assertEquals(
                "exact-signed-permutation-quarter-turn-blockstate-variant-and-uvlock-" +
                        "coordinate-transforms;avoids-host-float-matrix-drift-before-" +
                        "source-exact-InterpHelper-grid",
                Ae219217NativeStructuralProfile.FACADE_CARDINAL_VARIANT_TRANSFORM_POLICY
        );
        assertEquals(
                "exact-minecraft-stone-four-alternative-geometry-and-material-host-" +
                        "position-projection-retains-frozen-M2-non-pixel-identical-randomized-" +
                        "uv-boundary;all-other-weighted-sets-require-every-alternative-collapse-" +
                        "to-one-bounded-static-geometry-material-uv-descriptor;otherwise-atomic-" +
                        "original-resource-fallback",
                Ae219217NativeStructuralProfile.FACADE_WEIGHTED_VARIANT_POLICY
        );
        assertEquals(
                "exact-24-explicit-whitelist-same-state-table;ae2-quartz-glass-cross-" +
                        "family-render-shape-rule;exact-gallery-controls-glass-true-oak-log-" +
                        "false-oak-leaves-false;other-ordinary-tag-materials-use-bounded-" +
                        "BlueMap-cullingIdentical-same-state-host-projection",
                Ae219217NativeStructuralProfile.FACADE_SKIP_RENDERING_POLICY
        );
        assertEquals(0.00001D,
                Ae219217NativeStructuralProfile
                        .FACADE_CORNER_KICK_SOURCE_EPSILON_BLOCKS);
        assertEquals(0.00016D,
                Ae219217NativeStructuralProfile
                        .FACADE_CORNER_KICK_RUNTIME_EPSILON_SIXTEENTHS);
        assertEquals(0.00001D,
                Ae219217NativeStructuralProfile
                        .FACADE_CORNER_KICK_ANALYZER_EPSILON_BLOCKS);
        assertEquals(
                "minecraft-AABB-normalizes-each-generated-strip-endpoint-pair-with-min-max;" +
                        "transparent-inset-plus-boundary-reaching-cutout-may-reverse-" +
                        "endpoints-and-must-produce-the-normalized-strip-not-a-degenerate-" +
                        "strip",
                Ae219217NativeStructuralProfile.FACADE_CUTOUT_STRIP_AABB_POLICY
        );
        assertEquals(15, Ae219217NativeStructuralProfile.nativeFacadeNeutralMaterials()
                .get("ae2:quartz_vibrant_glass").blockStateLightEmission());
        assertEquals(0, Ae219217NativeStructuralProfile.nativeFacadeNeutralMaterials()
                .get("ae2:quartz_vibrant_glass").facadeQuadLightEmission());
        assertFalse(Ae219217NativeStructuralProfile.nativeFacadeNeutralMaterials()
                .get("ae2:quartz_vibrant_glass").solidRender());
        assertEquals(
                "11-ae2-native-neutral-resource-pins-not-the-complete-facade-support-set",
                Ae219217NativeStructuralProfile.NATIVE_FACADE_NEUTRAL_SCOPE
        );
        assertEquals(Map.of(
                "facing", "preserve",
                "formed", "false",
                "powered", "false",
                "spin", "0"
        ), Ae219217NativeStructuralProfile.nativeFacadeNeutralMaterials()
                .get("ae2:crafting_monitor").normalization());
        assertEquals(List.of("down", "up", "north", "south", "west", "east"),
                Ae219217NativeStructuralProfile.nativeFacadeNeutralMaterials()
                        .get("ae2:crafting_monitor").validPropertyValues().get("facing"));
        assertEquals(Map.of("state", "offline", "type", "block"),
                Ae219217NativeStructuralProfile.nativeFacadeNeutralMaterials()
                        .get("ae2:controller").normalization());
        assertEquals(19,
                Ae219217NativeStructuralProfile.QUARTZ_FACADE_DEPENDENCY_TEXTURE_COUNT);
        assertEquals(
                "c51ced2667879b8b298400c81805cf7d4459b5ac88c36350bca7bb6ca2bfef50",
                Ae219217NativeStructuralProfile
                        .QUARTZ_FACADE_DEPENDENCY_TEXTURE_SEMANTIC_SIGNATURE_SHA256
        );
    }

    @Test
    void staticModelLayersCloseTo41DirectRoots() {
        Set<String> roots = new LinkedHashSet<>();
        Ae219217NativeStructuralProfile.NATIVE_FACE_PART_MODELS.values()
                .forEach(roots::addAll);
        assertEquals(41, roots.size());
        assertEquals(List.of(
                "ae2:part/cable_anchor",
                "ae2:part/cable_anchor_short"
        ), Ae219217NativeStructuralProfile.NATIVE_FACE_PART_MODELS.get(
                "ae2:cable_anchor"
        ));
        assertEquals(List.of(
                "ae2:part/transition_plane_off",
                "ae2:part/annihilation_plane"
        ), Ae219217NativeStructuralProfile.NATIVE_FACE_PART_MODELS.get(
                "ae2:annihilation_plane"
        ));
        assertEquals(List.of(
                "ae2:part/p2p/p2p_tunnel_status_off",
                "ae2:part/p2p/p2p_tunnel_frequency",
                "ae2:part/p2p/p2p_tunnel_me"
        ), Ae219217NativeStructuralProfile.NATIVE_FACE_PART_MODELS.get(
                "ae2:me_p2p_tunnel"
        ));
    }

    @Test
    void endpointCatalogIsExactAndCableTypesHaveExpectedTotals() {
        assertEquals(30, Ae219217NativeStructuralProfile.NATIVE_ENDPOINT_IDS.size());
        assertEquals(
                new LinkedHashSet<>(Ae219217NativeStructuralProfile.NATIVE_ENDPOINT_IDS),
                Ae219217NativeStructuralProfile.NATIVE_ENDPOINT_CABLE_TYPES.keySet()
        );
        Map<String, String> types = Ae219217NativeStructuralProfile
                .NATIVE_ENDPOINT_CABLE_TYPES;
        assertEquals(18, types.values().stream().filter("SMART"::equals).count());
        assertEquals(9, types.values().stream().filter("COVERED"::equals).count());
        assertEquals(3, types.values().stream().filter("DENSE_SMART"::equals).count());
        assertEquals(
                new LinkedHashSet<>(Ae219217NativeStructuralProfile.NATIVE_ENDPOINT_IDS),
                Ae219217NativeStructuralProfile.NATIVE_ENDPOINT_BLOCK_ENTITY_IDS.keySet()
        );
        assertEquals(
                new LinkedHashSet<>(Ae219217NativeStructuralProfile.NATIVE_ENDPOINT_IDS),
                Ae219217NativeStructuralProfile.NATIVE_ENDPOINT_SIDE_RULES.keySet()
        );
        assertEquals(Map.of(
                "ALL", 12,
                "BACK", 2,
                "NO_FRONT", 3,
                "FRONT_BACK", 1,
                "PUSH_DIRECTION", 1,
                "FORMED_CRAFTING", 8,
                "FORMED_QUANTUM", 2,
                "VALID_STRAIGHT_PYLON", 1
        ), Ae219217NativeStructuralProfile.NATIVE_ENDPOINT_SIDE_RULE_COUNTS);
        assertEquals(
                Ae219217NativeStructuralProfile.ENDPOINT_SIDE_RULE_KIND_COUNT,
                Ae219217NativeStructuralProfile.NATIVE_ENDPOINT_SIDE_RULE_COUNTS.size()
        );
        assertEquals(
                new LinkedHashSet<>(Ae219217NativeStructuralProfile.NATIVE_ENDPOINT_IDS),
                Ae219217NativeStructuralProfile.nativeEndpointStateSchemas().keySet()
        );
        assertEquals(
                new LinkedHashSet<>(Ae219217NativeStructuralProfile.NATIVE_ENDPOINT_IDS),
                Ae219217NativeStructuralProfile.nativeEndpointBlockstateSha256().keySet()
        );
        assertEquals(List.of("facing", "state", "waterlogged"),
                Ae219217NativeStructuralProfile.endpointStateSchema(
                        "ae2:wireless_access_point"
                ).keySet().stream().toList());
        assertEquals(List.of("facing", "waterlogged"),
                Ae219217NativeStructuralProfile.endpointStateSchema(
                        "ae2:crystal_resonance_generator"
                ).keySet().stream().toList());
        assertEquals(Map.of("powered", List.of("false", "true")),
                Ae219217NativeStructuralProfile.endpointStateSchema(
                        "ae2:molecular_assembler"
                ));
        assertEquals(Map.of(), Ae219217NativeStructuralProfile.endpointStateSchema(
                "ae2:interface"
        ));
        List<Integer> endpointStateCardinalities = Ae219217NativeStructuralProfile
                .nativeEndpointStateSchemas().values().stream()
                .map(Ae219217NativeStructuralProfileTest::stateCardinality)
                .toList();
        assertEquals(List.of(
                48, 36, 24, 4, 4, 2, 48, 12, 18, 24, 48, 1, 48, 1, 12,
                48, 12, 5, 5, 1, 4, 4, 4, 4, 4, 4, 4, 96, 7, 2
        ), endpointStateCardinalities);
        assertEquals(
                Ae219217NativeStructuralProfile.ENDPOINT_STATE_COMBINATION_COUNT,
                endpointStateCardinalities.stream().mapToInt(Integer::intValue).sum()
        );
        assertEquals(
                Ae219217NativeStructuralProfile.ENDPOINT_STATE_SIDE_COMBINATION_COUNT,
                Ae219217NativeStructuralProfile.ENDPOINT_STATE_COMBINATION_COUNT
                        * Ae219217NativeStructuralProfile.FACE_COUNT
        );
        assertNull(Ae219217NativeStructuralProfile.endpointStateSchema("ae2:unknown"));
        assertEquals("ae2:quantum_ring",
                Ae219217NativeStructuralProfile.endpointBlockEntityId("ae2:quantum_link"));
        assertEquals("ae2:crafting_unit",
                Ae219217NativeStructuralProfile.endpointBlockEntityId(
                        "ae2:crafting_accelerator"
                ));
        assertEquals("ae2:crafting_storage",
                Ae219217NativeStructuralProfile.endpointBlockEntityId(
                        "ae2:256k_crafting_storage"
                ));
        assertEquals("FORMED_QUANTUM",
                Ae219217NativeStructuralProfile.endpointSideRule("ae2:quantum_ring"));
        assertEquals("PUSH_DIRECTION",
                Ae219217NativeStructuralProfile.endpointSideRule("ae2:pattern_provider"));
        assertEquals("COVERED",
                Ae219217NativeStructuralProfile.endpointCableType("ae2:inscriber"));
        assertEquals("DENSE_SMART",
                Ae219217NativeStructuralProfile.endpointCableType("ae2:quantum_link"));
        assertEquals("SMART",
                Ae219217NativeStructuralProfile.endpointCableType("ae2:spatial_pylon"));
        assertNull(Ae219217NativeStructuralProfile.endpointCableType("ae2:unknown"));
        assertNull(Ae219217NativeStructuralProfile.endpointBlockEntityId("ae2:unknown"));
        assertNull(Ae219217NativeStructuralProfile.endpointSideRule("ae2:unknown"));
        assertFalse(Ae219217NativeStructuralProfile.isNativeEndpoint("ae2:cable_bus"));
    }

    @Test
    void pinnedExtensionGridNodeHostsAreClosedUnknownFallbacks() {
        assertEquals(67, Ae219217NativeStructuralProfile
                .KNOWN_UNSUPPORTED_COMPATIBLE_ENDPOINT_IDS.size());
        assertEquals(Map.of(
                "expandedae-2.1.1", 24,
                "megacells-4.11.0", 11,
                "advanced_ae-1.6.12-1.21.1", 12,
                "extendedae-1.21-2.2.35-neoforge", 20
        ), Ae219217NativeStructuralProfile
                .KNOWN_UNSUPPORTED_COMPATIBLE_ENDPOINT_ARTIFACT_COUNTS);
        assertEquals("expandedae:exp_pattern_provider",
                Ae219217NativeStructuralProfile
                        .KNOWN_UNSUPPORTED_COMPATIBLE_ENDPOINT_IDS.get(0));
        assertEquals("extendedae:wireless_hub",
                Ae219217NativeStructuralProfile
                        .KNOWN_UNSUPPORTED_COMPATIBLE_ENDPOINT_IDS.get(66));
        assertEquals("expandedae:exp_cpus",
                Ae219217NativeStructuralProfile
                        .knownUnsupportedCompatibleEndpointBlockEntityId(
                                "expandedae:exp_crafting_accelerator_1m"
                        ));
        assertEquals("advanced_ae:quantum_craft",
                Ae219217NativeStructuralProfile
                        .knownUnsupportedCompatibleEndpointBlockEntityId(
                                "advanced_ae:quantum_crafter"
                        ));
        assertTrue(Ae219217NativeStructuralProfile
                .isKnownUnsupportedCompatibleEndpoint("megacells:mega_energy_cell"));
        assertEquals("unknown-atomic-original-resource-fallback",
                Ae219217NativeStructuralProfile
                        .knownUnsupportedCompatibleEndpointPolicy(
                                "megacells:mega_energy_cell"
                        ));
        assertEquals("malformed-native-endpoint-atomic-original-resource-fallback",
                Ae219217NativeStructuralProfile.malformedNativeEndpointPolicy(
                        "ae2:drive"
                ));
        assertNull(Ae219217NativeStructuralProfile
                .knownUnsupportedCompatibleEndpointPolicy("ae2:drive"));
        assertNull(Ae219217NativeStructuralProfile
                .malformedNativeEndpointPolicy("megacells:mega_energy_cell"));
        assertFalse(Ae219217NativeStructuralProfile
                .isKnownUnsupportedCompatibleEndpoint("minecraft:furnace"));
    }

    @Test
    void resourceClosureAndGeneratedProfileAreDigestLocked()
            throws IOException, NoSuchAlgorithmException {
        assertEquals(
                "aefa42ad8427e8f2ac5b9f1c88807c978617d6ff70768a32223616b970b54251",
                Ae219217NativeStructuralProfile.LIVE_MODEL_SEMANTIC_SIGNATURE_SHA256
        );
        assertEquals(
                "1bee2b2917edf3d1eb9ee24505f47a7377665da753f107ec1af9170d783bc833",
                Ae219217NativeStructuralProfile.LIVE_TEXTURE_SEMANTIC_SIGNATURE_SHA256
        );
        assertEquals(99, Ae219217NativeStructuralProfile.requiredResources().size());
        assertEquals(99, Ae219217NativeStructuralProfile.requiredResourceSizes().size());
        assertEquals(
                Ae219217NativeStructuralProfile.requiredResources().keySet(),
                Ae219217NativeStructuralProfile.requiredResourceSizes().keySet()
        );
        assertEquals(
                51_306L,
                Ae219217NativeStructuralProfile.requiredResourceSizes().values().stream()
                        .mapToLong(Long::longValue)
                        .sum()
        );
        assertEquals(
                43,
                Ae219217NativeStructuralProfile.requiredResources().keySet().stream()
                        .filter(path -> path.endsWith(".json"))
                        .count()
        );
        assertEquals(
                56,
                Ae219217NativeStructuralProfile.requiredResources().keySet().stream()
                        .filter(path -> path.endsWith(".png"))
                        .count()
        );
        String path = Ae219217NativeStructuralProfile.RESOURCE_ROOT + "profile.json";
        try (InputStream input = Ae219217NativeStructuralProfile.class
                .getResourceAsStream(path)) {
            assertEquals(
                    Ae219217NativeStructuralProfile.PROFILE_SHA256,
                    java.util.HexFormat.of().formatHex(
                            MessageDigest.getInstance("SHA-256")
                                    .digest(input.readAllBytes())
                    )
            );
        }
    }

    @Test
    void exposedCollectionsAreImmutable() {
        assertThrows(UnsupportedOperationException.class,
                () -> Ae219217NativeStructuralProfile.NATIVE_FACE_PART_IDS.clear());
        assertThrows(UnsupportedOperationException.class,
                () -> Ae219217NativeStructuralProfile.SPIN_CAPABLE_PART_IDS.clear());
        assertThrows(UnsupportedOperationException.class,
                () -> Ae219217NativeStructuralProfile.DENSE_CAPABLE_PART_IDS.clear());
        assertThrows(UnsupportedOperationException.class,
                () -> Ae219217NativeStructuralProfile.SMART_CORE_PART_IDS.clear());
        assertThrows(UnsupportedOperationException.class,
                () -> Ae219217NativeStructuralProfile.PLANE_CONNECTION_MASK_BITS.clear());
        assertThrows(UnsupportedOperationException.class,
                () -> Ae219217NativeStructuralProfile
                        .PLANE_COLLISION_BOUND_BITS_BY_FACE.clear());
        assertThrows(UnsupportedOperationException.class,
                () -> Ae219217NativeStructuralProfile
                        .PLANE_COLLISION_BOUND_BITS_BY_FACE.get("down").clear());
        assertThrows(UnsupportedOperationException.class,
                () -> Ae219217NativeStructuralProfile.NATIVE_FACE_PART_COLLISION_BOXES.clear());
        assertThrows(UnsupportedOperationException.class,
                () -> Ae219217NativeStructuralProfile.NATIVE_FACE_PART_COLLISION_MODES.clear());
        assertThrows(UnsupportedOperationException.class,
                () -> Ae219217NativeStructuralProfile.FACADE_WHITELIST_NEUTRAL_STATES.clear());
        assertThrows(UnsupportedOperationException.class,
                () -> Ae219217NativeStructuralProfile.FACADE_WHITELIST_STATE_SCHEMAS.clear());
        assertThrows(UnsupportedOperationException.class,
                () -> Ae219217NativeStructuralProfile.FACADE_WHITELIST_STATE_SCHEMAS
                        .get("minecraft:crafter").clear());
        assertThrows(UnsupportedOperationException.class,
                () -> Ae219217NativeStructuralProfile.FACADE_WHITELIST_STATE_SCHEMAS
                        .get("minecraft:crafter").get("orientation").clear());
        assertThrows(UnsupportedOperationException.class,
                () -> Ae219217NativeStructuralProfile.FACADE_WHITELIST_BLOCKSTATE_SHA256
                        .clear());
        assertThrows(UnsupportedOperationException.class,
                () -> Ae219217NativeStructuralProfile
                        .FACADE_WHITELIST_SAME_STATE_SKIP_RENDERING.clear());
        assertThrows(UnsupportedOperationException.class,
                () -> Ae219217NativeStructuralProfile
                        .FACADE_ORDINARY_SKIP_RENDERING_CONTROLS.clear());
        assertThrows(UnsupportedOperationException.class,
                () -> Ae219217NativeStructuralProfile.NATIVE_FACE_PART_MODELS.clear());
        assertThrows(UnsupportedOperationException.class,
                () -> Ae219217NativeStructuralProfile.NATIVE_FACE_PART_MODELS
                        .get("ae2:terminal").clear());
        assertThrows(UnsupportedOperationException.class,
                () -> Ae219217NativeStructuralProfile.NATIVE_ENDPOINT_CABLE_TYPES.clear());
        assertThrows(UnsupportedOperationException.class,
                () -> Ae219217NativeStructuralProfile.NATIVE_ENDPOINT_BLOCK_ENTITY_IDS.clear());
        assertThrows(UnsupportedOperationException.class,
                () -> Ae219217NativeStructuralProfile.NATIVE_ENDPOINT_SIDE_RULES.clear());
        assertThrows(UnsupportedOperationException.class,
                () -> Ae219217NativeStructuralProfile.NATIVE_ENDPOINT_STATE_SCHEMAS.clear());
        assertThrows(UnsupportedOperationException.class,
                () -> Ae219217NativeStructuralProfile.NATIVE_ENDPOINT_STATE_SCHEMAS
                        .get("ae2:inscriber").clear());
        assertThrows(UnsupportedOperationException.class,
                () -> Ae219217NativeStructuralProfile.NATIVE_ENDPOINT_STATE_SCHEMAS
                        .get("ae2:inscriber").get("facing").clear());
        assertThrows(UnsupportedOperationException.class,
                () -> Ae219217NativeStructuralProfile.NATIVE_ENDPOINT_SIDE_RULE_COUNTS.clear());
        assertThrows(UnsupportedOperationException.class,
                () -> Ae219217NativeStructuralProfile
                        .KNOWN_UNSUPPORTED_COMPATIBLE_ENDPOINT_IDS.clear());
        assertThrows(UnsupportedOperationException.class,
                () -> Ae219217NativeStructuralProfile
                        .KNOWN_UNSUPPORTED_COMPATIBLE_ENDPOINT_BLOCK_ENTITY_IDS.clear());
        assertThrows(UnsupportedOperationException.class,
                () -> Ae219217NativeStructuralProfile.requiredResources().clear());
        assertThrows(UnsupportedOperationException.class,
                () -> Ae219217NativeStructuralProfile.requiredResourceSizes().clear());
    }

    private static int stateCardinality(Map<String, List<String>> schema) {
        return schema.values().stream()
                .mapToInt(List::size)
                .reduce(1, Math::multiplyExact);
    }

    private record ExpectedPlaneCollisionBits(int minX, int maxX, int minY, int maxY) {
    }
}
