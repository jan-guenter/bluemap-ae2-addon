/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.adapter.bluemap522;

import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.util.Key;
import io.github.janguenter.bluemap.ae2.model.NativeEndpointCatalog;
import io.github.janguenter.bluemap.ae2.model.NativeStructuralPartCatalog;
import io.github.janguenter.bluemap.ae2.profile.Ae219217NativeStructuralProfile;
import io.github.janguenter.bluemap.ae2.profile.Ae219217QuartzGlassProfile;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Exact semantic model/texture gate for the native structural route. */
final class NativeStructuralResourceModels {

    private static final Set<Key> REQUIRED_TEXTURES = buildRequiredTextures();

    private NativeStructuralResourceModels() {
    }

    static boolean exactRouteContractAvailable() {
        if (Ae219217NativeStructuralProfile.SCHEMA_VERSION != 10
                || !"ae2-cable-bus-structural".equals(
                        Ae219217NativeStructuralProfile.PROFILE_ID
                )
                || Ae219217NativeStructuralProfile.FACE_PART_COUNT != 29
                || Ae219217NativeStructuralProfile.SPIN_CAPABLE_PART_COUNT != 9
                || Ae219217NativeStructuralProfile.ORIENTATION_STATE_COUNT != 336
                || Ae219217NativeStructuralProfile.SMART_CORE_PART_COUNT != 2
                || Ae219217NativeStructuralProfile.PLANE_CONNECTION_MASK_COUNT != 16
                || Ae219217NativeStructuralProfile.NATIVE_ENDPOINT_COUNT != 30
                || Ae219217NativeStructuralProfile.ENDPOINT_STATE_SCHEMA_COUNT != 30
                || Ae219217NativeStructuralProfile.ENDPOINT_STATE_COMBINATION_COUNT != 534
                || Ae219217NativeStructuralProfile
                        .ENDPOINT_STATE_SIDE_COMBINATION_COUNT != 3_204
                || Ae219217NativeStructuralProfile
                        .KNOWN_UNSUPPORTED_COMPATIBLE_ENDPOINT_COUNT != 67
                || Ae219217NativeStructuralProfile.FACADE_MASK_COUNT != 64
                || Ae219217NativeStructuralProfile
                        .FACADE_WHITELIST_BLOCK_COUNT != 24
                || Ae219217NativeStructuralProfile
                        .FACADE_WHITELIST_NEUTRAL_STATE_COUNT != 24
                || Ae219217NativeStructuralProfile
                        .FACADE_WHITELIST_STATE_SCHEMA_COUNT != 24
                || Ae219217NativeStructuralProfile
                        .FACADE_WHITELIST_STATE_COMBINATION_COUNT != 554
                || Ae219217NativeStructuralProfile
                        .FACADE_WHITELIST_SOLID_RENDER_TRUE_STATE_COUNT != 551
                || Ae219217NativeStructuralProfile
                        .FACADE_WHITELIST_SAME_STATE_SKIP_RENDERING_TRUE_STATE_COUNT != 3
                || Ae219217NativeStructuralProfile
                        .FACADE_WHITELIST_SAME_STATE_SKIP_RENDERING_COUNT != 24
                || Ae219217NativeStructuralProfile
                        .FACADE_ORDINARY_SKIP_RENDERING_CONTROL_COUNT != 3
                || Ae219217NativeStructuralProfile
                        .QUARTZ_FACADE_DEPENDENCY_TEXTURE_COUNT != 19
                || Ae219217NativeStructuralProfile.TRANSITIVE_JSON_RESOURCE_COUNT != 43
                || Ae219217NativeStructuralProfile.PNG_RESOURCE_COUNT != 56
                || Ae219217NativeStructuralProfile.REQUIRED_RESOURCE_COUNT != 99
                || Ae219217NativeStructuralProfile.requiredResources().size() != 99
                || Ae219217NativeStructuralProfile.requiredResourceSizes().size() != 99
                || !"untinted-or-one-distinct-nonnegative-source-tint-index;"
                        .concat("untinted-layers-may-coexist;shared-tinted-layers-use-")
                        .concat("host-block-color-calculator;mixed-nonnegative-tint-")
                        .concat("indices-atomic-original-resource-fallback")
                        .equals(Ae219217NativeStructuralProfile.FACADE_TINT_POLICY)
                || !"source-QuadReInterpolator-nominal-face-2d-dx-dy-bilinear;"
                        .concat("admitted-quad-projection-requires-exact-complete-")
                        .concat("InterpHelper-grid;post-clamp-and-corner-kick-target-uses-")
                        .concat("projected-dx-dy;noncompatible-projected-quads-atomic-")
                        .concat("original-resource-fallback")
                        .equals(Ae219217NativeStructuralProfile
                                .FACADE_UV_REINTERPOLATION_POLICY)
                || !"exact-minecraft-stone-four-alternative-geometry-and-material-"
                        .concat("host-position-projection-retains-frozen-M2-non-pixel-")
                        .concat("identical-randomized-uv-boundary;all-other-weighted-sets-")
                        .concat("require-every-alternative-collapse-to-one-bounded-static-")
                        .concat("geometry-material-uv-descriptor;otherwise-atomic-original-")
                        .concat("resource-fallback")
                        .equals(Ae219217NativeStructuralProfile
                                .FACADE_WEIGHTED_VARIANT_POLICY)
                || !"exact-24-explicit-whitelist-same-state-table;ae2-quartz-glass-"
                        .concat("cross-family-render-shape-rule;exact-gallery-controls-")
                        .concat("glass-true-oak-log-false-oak-leaves-false;other-ordinary-")
                        .concat("tag-materials-use-bounded-BlueMap-cullingIdentical-same-")
                        .concat("state-host-projection")
                        .equals(Ae219217NativeStructuralProfile
                                .FACADE_SKIP_RENDERING_POLICY)
                || !"all-24-explicit-whitelist-families-require-exact-complete-"
                        .concat("persisted-property-key-set-and-value-domains;13-vanilla-")
                        .concat("families-preserve-valid-state;11-ae2-native-families-")
                        .concat("apply-declared-static-normalization;extra-missing-or-invalid-")
                        .concat("properties-atomic-original-resource-fallback")
                        .equals(Ae219217NativeStructuralProfile
                                .FACADE_WHITELIST_STATE_POLICY)
                || !"solidRender-and-same-state-skipRendering-family-invariant-across-all-"
                        .concat("554-valid-explicit-whitelist-states;neutral-default-row-")
                        .concat("booleans-apply-to-whole-family;classification-drift-atomic-")
                        .concat("original-resource-fallback")
                        .equals(Ae219217NativeStructuralProfile
                                .FACADE_WHITELIST_STATE_CLASSIFICATION_POLICY)
                || !"optional-c-glass-blocks-and-ordinary-FacadeItem-eligible-states-"
                        .concat("require-one-live-unrotated-0-to-16-six-face-full-cube-")
                        .concat("witness;bounded-additional-static-elements-and-multipart-")
                        .concat("source-quads-subject-to-uv-tint-weighted-and-semantic-")
                        .concat("resource-gates;otherwise-valid-complex-static-models-atomic-")
                        .concat("original-resource-fallback")
                        .equals(Ae219217NativeStructuralProfile
                                .FACADE_ORDINARY_MATERIAL_POLICY)
                || !"true-for-any-two-ae2-QuartzGlassBlock-families-with-equal-render-"
                        .concat("shape")
                        .equals(Ae219217NativeStructuralProfile
                                .FACADE_QUARTZ_SKIP_RENDERING_POLICY)
                || !"exact-signed-permutation-quarter-turn-blockstate-variant-and-uvlock-"
                        .concat("coordinate-transforms;avoids-host-float-matrix-drift-before-")
                        .concat("source-exact-InterpHelper-grid")
                        .equals(Ae219217NativeStructuralProfile
                                .FACADE_CARDINAL_VARIANT_TRANSFORM_POLICY)
                || !"minecraft-AABB-normalizes-each-generated-strip-endpoint-pair-with-"
                        .concat("min-max;transparent-inset-plus-boundary-reaching-cutout-")
                        .concat("may-reverse-endpoints-and-must-produce-the-normalized-")
                        .concat("strip-not-a-degenerate-strip")
                        .equals(Ae219217NativeStructuralProfile
                                .FACADE_CUTOUT_STRIP_AABB_POLICY)
                || !"malformed-known-extension-observation-atomic-original-resource-"
                        .concat("fallback")
                        .equals(Ae219217NativeStructuralProfile
                                .MALFORMED_KNOWN_EXTENSION_ENDPOINT_POLICY)
                || !"BlueMap-ResourceModelRenderer-source-faceDir-rotated-by-"
                        .concat("blockstate-variant-only;element-rotation-affects-vertices-")
                        .concat("not-AO-direction;runtime-uses-layer-lightFace-not-quad-")
                        .concat("nominal-face")
                        .equals(Ae219217NativeStructuralProfile
                                .FACADE_AMBIENT_OCCLUSION_DIRECTION_POLICY)
                || !"BlueMap-map-color-illumination-uses-original-center-and-outward-"
                        .concat("world-light-only;element-lightEmission-affects-triangle-")
                        .concat("blocklight-not-map-color-brightness")
                        .equals(Ae219217NativeStructuralProfile
                                .STRUCTURAL_MAP_COLOR_ILLUMINATION_POLICY)
                || !"resolved-parent-applied-elements-faces-ao-shade-light-uv-"
                        .concat("texture-cull-rotation-tint-float-bits-sha256-v1")
                        .equals(Ae219217NativeStructuralProfile
                                .LIVE_MODEL_SEMANTIC_SIGNATURE_ALGORITHM)
                || !"decoded-width-height-argb-scanline-animation-meta-sha256-v1"
                        .equals(Ae219217NativeStructuralProfile
                                .LIVE_TEXTURE_SEMANTIC_SIGNATURE_ALGORITHM)) {
            return false;
        }
        if (!Ae219217QuartzGlassProfile.PROFILE_ID.equals(
                Ae219217NativeStructuralProfile
                        .QUARTZ_FACADE_DEPENDENCY_PROFILE_ID
        ) || !Ae219217QuartzGlassProfile.RESOURCE_MANIFEST_SHA256.equals(
                Ae219217NativeStructuralProfile
                        .QUARTZ_FACADE_DEPENDENCY_RESOURCE_MANIFEST_SHA256
        ) || !Ae219217QuartzGlassProfile.TEXTURE_MANIFEST_SHA256.equals(
                Ae219217NativeStructuralProfile
                        .QUARTZ_FACADE_DEPENDENCY_TEXTURE_MANIFEST_SHA256
        ) || M3cQuartzGlassResourceModels.requiredTextures().size() != 19
                || !M3cQuartzGlassResourceModels.requiredTextures().equals(
                        Ae219217QuartzGlassProfile.textures().stream()
                                .map(Key::parse)
                                .collect(java.util.stream.Collectors.toSet())
                )) {
            return false;
        }
        if (!Ae219217NativeStructuralProfile.nativeFacePartIds().equals(
                NativeStructuralPartCatalog.definitions().stream()
                        .map(NativeStructuralPartCatalog.Definition::id)
                        .toList()
        ) || !Ae219217NativeStructuralProfile.nativeEndpointIds().equals(
                NativeEndpointCatalog.definitions().stream()
                        .map(NativeEndpointCatalog.Definition::blockId)
                        .toList()
        ) || !List.copyOf(Ae219217NativeStructuralProfile
                .nativeEndpointStateSchemas().keySet()).equals(
                        Ae219217NativeStructuralProfile.nativeEndpointIds()
        ) || !List.copyOf(Ae219217NativeStructuralProfile
                .facadeWhitelistNeutralStates().keySet()).equals(
                        Ae219217NativeStructuralProfile.facadeWhitelistBlockIds()
        ) || !List.copyOf(Ae219217NativeStructuralProfile
                .facadeWhitelistStateSchemas().keySet()).equals(
                        Ae219217NativeStructuralProfile.facadeWhitelistBlockIds()
        ) || Ae219217NativeStructuralProfile.facadeWhitelistStateSchemas()
                .values().stream().flatMap(schema -> schema.values().stream())
                .anyMatch(List::isEmpty)
                || !List.copyOf(Ae219217NativeStructuralProfile
                .facadeWhitelistSameStateSkipRendering().keySet()).equals(
                        Ae219217NativeStructuralProfile.facadeWhitelistBlockIds()
        ) || !Ae219217NativeStructuralProfile
                .facadeWhitelistSameStateSkipRendering().entrySet().stream()
                .filter(Map.Entry::getValue)
                .map(Map.Entry::getKey)
                .collect(java.util.stream.Collectors.toUnmodifiableSet())
                .equals(Set.of(
                        "ae2:quartz_glass",
                        "ae2:quartz_vibrant_glass",
                        "minecraft:honey_block"
                ))
        ) {
            return false;
        }
        if (!Ae219217NativeStructuralProfile.spinCapablePartIds().equals(
                NativeStructuralPartCatalog.definitions().stream()
                        .filter(NativeStructuralPartCatalog.Definition::persistedSpin)
                        .map(NativeStructuralPartCatalog.Definition::id)
                        .collect(java.util.stream.Collectors.toUnmodifiableSet())
        ) || !Ae219217NativeStructuralProfile.denseCapablePartIds().equals(
                Set.of(NativeStructuralPartCatalog.CABLE_ANCHOR)
        ) || !Ae219217NativeStructuralProfile.smartCorePartIds().equals(
                NativeStructuralPartCatalog.definitions().stream()
                        .filter(NativeStructuralPartCatalog.Definition::requestsSmartCore)
                        .map(NativeStructuralPartCatalog.Definition::id)
                        .collect(java.util.stream.Collectors.toUnmodifiableSet())
        ) || !Ae219217NativeStructuralProfile.planeConnectionMaskBits().equals(
                Map.of("left", 1, "down", 2, "right", 4, "up", 8)
        ) || !Ae219217NativeStructuralProfile
                .facadeOrdinarySkipRenderingControls().equals(Map.of(
                        "minecraft:glass", true,
                        "minecraft:oak_log", false,
                        "minecraft:oak_leaves", false
                ))
                || Double.compare(
                        Ae219217NativeStructuralProfile
                                .FACADE_CORNER_KICK_RUNTIME_EPSILON_SIXTEENTHS,
                        1.0E-5D * 16D
                ) != 0
                || Double.compare(
                        Ae219217NativeStructuralProfile
                                .FACADE_CORNER_KICK_SOURCE_EPSILON_BLOCKS,
                        1.0E-5D
                ) != 0
                || Double.compare(
                        Ae219217NativeStructuralProfile
                                .FACADE_CORNER_KICK_ANALYZER_EPSILON_BLOCKS,
                        1.0E-5D
                ) != 0
                || !List.copyOf(Ae219217NativeStructuralProfile
                        .nativeFacePartCollisionBoxes().keySet()).equals(
                        Ae219217NativeStructuralProfile.nativeFacePartIds()
        ) || !List.copyOf(Ae219217NativeStructuralProfile
                .nativeFacePartCollisionModes().keySet()).equals(
                        Ae219217NativeStructuralProfile.nativeFacePartIds()
        ) || !List.copyOf(Ae219217NativeStructuralProfile
                .knownUnsupportedCompatibleEndpointBlockEntityIds().keySet()).equals(
                        Ae219217NativeStructuralProfile
                                .knownUnsupportedCompatibleEndpointIds()
        ) || !Ae219217NativeStructuralProfile
                .KNOWN_UNSUPPORTED_COMPATIBLE_ENDPOINT_POLICY.equals(
                        Ae219217NativeStructuralProfile
                                .knownUnsupportedCompatibleEndpointPolicy(
                                        Ae219217NativeStructuralProfile
                                                .knownUnsupportedCompatibleEndpointIds()
                                                .getFirst()
                                )
        ) || !Ae219217NativeStructuralProfile.MALFORMED_NATIVE_ENDPOINT_POLICY.equals(
                Ae219217NativeStructuralProfile.malformedNativeEndpointPolicy(
                        Ae219217NativeStructuralProfile.nativeEndpointIds().getFirst()
                )
        )) {
            return false;
        }

        LinkedHashMap<String, List<String>> modelLayers = new LinkedHashMap<>();
        for (NativeStructuralPartCatalog.Definition definition
                : NativeStructuralPartCatalog.definitions()) {
            modelLayers.put(definition.id(), definition.modelPaths());
        }
        if (!Ae219217NativeStructuralProfile.nativeFacePartModels().equals(modelLayers)) {
            return false;
        }
        for (NativeEndpointCatalog.Definition definition
                : NativeEndpointCatalog.definitions()) {
            if (!definition.cableType().name().equals(
                    Ae219217NativeStructuralProfile.endpointCableType(definition.blockId())
            ) || !definition.blockEntityId().equals(
                    Ae219217NativeStructuralProfile.endpointBlockEntityId(
                            definition.blockId()
                    )
            ) || !definition.sidePolicy().name().equals(
                    Ae219217NativeStructuralProfile.endpointSideRule(
                            definition.blockId()
                    )
            ) || Ae219217NativeStructuralProfile.endpointStateSchema(
                    definition.blockId()
            ) == null
                    || Ae219217NativeStructuralProfile.endpointStateSchema(
                            definition.blockId()
                    ).values().stream().anyMatch(List::isEmpty)
            ) {
                return false;
            }
        }
        return REQUIRED_TEXTURES.size() == 56;
    }

    static Set<Key> requiredTextures() {
        return REQUIRED_TEXTURES;
    }

    static boolean resourcesSupported(ResourcePack resourcePack) {
        if (resourcePack == null || !exactRouteContractAvailable()) {
            return false;
        }
        return NativeStructuralSemanticResources.supports(resourcePack)
                && Ae219217NativeStructuralProfile
                        .QUARTZ_FACADE_DEPENDENCY_TEXTURE_SEMANTIC_SIGNATURE_SHA256
                        .equals(NativeStructuralSemanticResources.textureSignature(
                                resourcePack,
                                M3cQuartzGlassResourceModels.requiredTextures()
                        ));
    }

    static List<String> renderedModelPaths(
            NativeStructuralPartCatalog.Definition definition,
            boolean sameFaceFacade
    ) {
        return switch (definition.kind()) {
            case ANCHOR -> List.of(sameFaceFacade
                    ? "ae2:part/cable_anchor_short" : "ae2:part/cable_anchor");
            case PLANE -> List.of("ae2:part/transition_plane_off");
            case P2P -> List.of(
                    "ae2:part/p2p/p2p_tunnel_status_off",
                    definition.modelPaths().get(2)
            );
            case STATIC, REPORTING, CELL_DOCK -> definition.modelPaths();
        };
    }

    private static Set<Key> buildRequiredTextures() {
        LinkedHashSet<Key> textures = new LinkedHashSet<>();
        for (String resource : Ae219217NativeStructuralProfile.requiredResources().keySet()) {
            if (!resource.startsWith("assets/") || !resource.endsWith(".png")) {
                continue;
            }
            String relative = resource.substring("assets/".length(), resource.length() - 4);
            int separator = relative.indexOf("/textures/");
            if (separator <= 0) {
                throw new IllegalStateException("invalid structural texture path " + resource);
            }
            String namespace = relative.substring(0, separator);
            String path = relative.substring(separator + "/textures/".length());
            textures.add(Key.parse(namespace + ":" + path));
        }
        return java.util.Collections.unmodifiableSet(textures);
    }
}
