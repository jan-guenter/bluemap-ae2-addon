/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.adapter.bluemap523;

import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePackExtension;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.VariantSet;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.Variants;
import de.bluecolored.bluemap.core.util.Key;
import de.bluecolored.bluemap.core.world.BlockProperties;
import de.bluecolored.bluemap.core.world.BlockState;
import io.github.janguenter.bluemap.ae2.activation.ExtensionRouteActivation;
import io.github.janguenter.bluemap.ae2.profile.Ae219217NativeStructuralProfile;
import io.github.janguenter.bluemap.ae2.profile.ExactModArtifactDetector;
import io.github.janguenter.bluemap.ae2.profile.ProfileDisablement;
import io.github.janguenter.bluemap.ae2.profile.advancedae.AdvancedAe1612AthenaProfile;
import io.github.janguenter.bluemap.ae2.profile.advancedae.AdvancedAe1612Catalog;
import io.github.janguenter.bluemap.ae2.profile.advancedae.AdvancedAe1612Profile;
import io.github.janguenter.bluemap.ae2.profile.advancedae.Athena406ArtifactIdentity;
import io.github.janguenter.bluemap.ae2.profile.appmek.AppMek163Profile;
import io.github.janguenter.bluemap.ae2.profile.appflux.AppFlux215Profile;
import io.github.janguenter.bluemap.ae2.profile.expandedae.ExpandedAe211Catalog;
import io.github.janguenter.bluemap.ae2.profile.expandedae.ExpandedAe211Profile;
import io.github.janguenter.bluemap.ae2.profile.extendedae.ExtendedAe2235Catalog;
import io.github.janguenter.bluemap.ae2.profile.extendedae.ExtendedAe2235Profile;
import io.github.janguenter.bluemap.ae2.profile.megacells.MegaCells4110ArtifactIdentity;
import io.github.janguenter.bluemap.ae2.profile.megacells.MegaCells4110Profile;
import io.github.janguenter.bluemap.ae2.profile.merequester.MeRequester143Catalog;
import io.github.janguenter.bluemap.ae2.profile.merequester.MeRequester143Profile;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/** Exact ATM 1.2.0 resource routing for independently isolated extensions. */
final class M45ResourceExtension implements ResourcePackExtension {

    static final Key SYNTHETIC = Key.parse("bluemap_ae2:m45");
    private static final int MAX_RESOURCE_ROOTS = 4_096;
    private static final Set<Key> CUSTOM_BLOCKS = customBlocks();
    private static final Set<Key> Z_BLOCKS = Set.of(
            Key.parse(MeRequester143Catalog.REQUESTER_BLOCK),
            Key.parse(ExpandedAe211Catalog.IO_PORT_BLOCK)
    );

    private final ResourcePack resourcePack;
    private final M45Runtime runtime;
    private final java.util.function.BooleanSupplier coreActive;
    private final java.util.function.BooleanSupplier nativeStructuralActive;
    private final java.util.function.BooleanSupplier nativeDriveActive;
    private final java.util.function.BooleanSupplier nativeDrivePending;
    private final Map<String, RouteProfile> profiles;

    M45ResourceExtension(ResourcePack resourcePack, M45Runtime runtime) {
        this(
                resourcePack,
                runtime,
                BlueMap523Adapter::coreProfileActiveForM45,
                BlueMap523Adapter::nativeStructuralActiveForM45,
                BlueMap523Adapter::nativeDriveActiveForM45,
                BlueMap523Adapter::nativeDrivePendingForM45
        );
    }

    M45ResourceExtension(
            ResourcePack resourcePack,
            M45Runtime runtime,
            java.util.function.BooleanSupplier coreActive
    ) {
        this(resourcePack, runtime, coreActive, () -> true, () -> true, () -> false);
    }

    M45ResourceExtension(
            ResourcePack resourcePack,
            M45Runtime runtime,
            java.util.function.BooleanSupplier coreActive,
            java.util.function.BooleanSupplier nativeStructuralActive
    ) {
        this(resourcePack, runtime, coreActive, nativeStructuralActive,
                () -> true, () -> false);
    }

    M45ResourceExtension(
            ResourcePack resourcePack,
            M45Runtime runtime,
            java.util.function.BooleanSupplier coreActive,
            java.util.function.BooleanSupplier nativeStructuralActive,
            java.util.function.BooleanSupplier nativeDriveActive,
            java.util.function.BooleanSupplier nativeDrivePending
    ) {
        this.resourcePack = Objects.requireNonNull(resourcePack, "resourcePack");
        this.runtime = Objects.requireNonNull(runtime, "runtime");
        this.coreActive = Objects.requireNonNull(coreActive, "coreActive");
        this.nativeStructuralActive = Objects.requireNonNull(
                nativeStructuralActive,
                "nativeStructuralActive"
        );
        this.nativeDriveActive = Objects.requireNonNull(
                nativeDriveActive,
                "nativeDriveActive"
        );
        this.nativeDrivePending = Objects.requireNonNull(
                nativeDrivePending,
                "nativeDrivePending"
        );
        this.profiles = Map.ofEntries(
                Map.entry(M45Runtime.APPFLUX, profile(
                        AppFlux215Profile.MOD_ID,
                        AppFlux215Profile.VERSION,
                        AppFlux215Profile.JAR_BYTES,
                        AppFlux215Profile.JAR_SHA256,
                        AppFlux215Profile.EXACT_REASON,
                        AppFlux215Profile.requiredResources()
                )),
                Map.entry(M45Runtime.ME_REQUESTER, profile(
                        MeRequester143Profile.MOD_ID,
                        MeRequester143Profile.VERSION,
                        MeRequester143Profile.JAR_BYTES,
                        MeRequester143Profile.JAR_SHA256,
                        MeRequester143Profile.EXACT_REASON,
                        MeRequester143Profile.requiredResources()
                )),
                Map.entry(M45Runtime.EXPANDED_AE, profile(
                        ExpandedAe211Profile.MOD_ID,
                        ExpandedAe211Profile.VERSION,
                        ExpandedAe211Profile.JAR_BYTES,
                        ExpandedAe211Profile.JAR_SHA256,
                        ExpandedAe211Profile.EXACT_REASON,
                        ExpandedAe211Profile.requiredResources()
                )),
                Map.entry(M45Runtime.MEGA_CELLS, profile(
                        MegaCells4110ArtifactIdentity.MOD_ID,
                        MegaCells4110ArtifactIdentity.VERSION,
                        MegaCells4110ArtifactIdentity.JAR_BYTES,
                        MegaCells4110ArtifactIdentity.JAR_SHA256,
                        MegaCells4110Profile.EXACT_REASON,
                        mergeResources(
                                MegaCells4110Profile.allOwnRequiredResources(),
                                MegaCells4110Profile.dependentAe2RequiredResources()
                        )
                )),
                Map.entry(M45Runtime.ADVANCED_QUANTUM, profile(
                        AdvancedAe1612Profile.MOD_ID,
                        AdvancedAe1612Profile.VERSION,
                        AdvancedAe1612Profile.JAR_BYTES,
                        AdvancedAe1612Profile.JAR_SHA256,
                        AdvancedAe1612Profile.EXACT_REASON,
                        AdvancedAe1612Profile.requiredResources()
                )),
                Map.entry(M45Runtime.ADVANCED_ATHENA, combinedProfile(
                        "exact-atm-1.2.0-advanced-ae-1.6.12-athena-4.0.6",
                        AdvancedAe1612AthenaProfile.requiredResources(),
                        identity(
                                AdvancedAe1612Profile.MOD_ID,
                                AdvancedAe1612Profile.VERSION,
                                AdvancedAe1612Profile.JAR_BYTES,
                                AdvancedAe1612Profile.JAR_SHA256,
                                AdvancedAe1612Profile.EXACT_REASON
                        ),
                        identity(
                                Athena406ArtifactIdentity.MOD_ID,
                                Athena406ArtifactIdentity.VERSION,
                                Athena406ArtifactIdentity.JAR_BYTES,
                                Athena406ArtifactIdentity.JAR_SHA256,
                                "exact-atm-1.2.0-athena-4.0.6"
                        )
                )),
                Map.entry(M45Runtime.EXTENDED_MATRIX, profile(
                        ExtendedAe2235Profile.MOD_ID,
                        ExtendedAe2235Profile.VERSION,
                        ExtendedAe2235Profile.JAR_BYTES,
                        ExtendedAe2235Profile.JAR_SHA256,
                        ExtendedAe2235Profile.EXACT_REASON,
                        extendedMatrixResources()
                )),
                Map.entry(M45Runtime.EXTENDED_PLANES, profile(
                        ExtendedAe2235Profile.MOD_ID,
                        ExtendedAe2235Profile.VERSION,
                        ExtendedAe2235Profile.JAR_BYTES,
                        ExtendedAe2235Profile.JAR_SHA256,
                        ExtendedAe2235Profile.EXACT_REASON,
                        extendedPlaneResources()
                )),
                Map.entry(M45Runtime.APPMEK_DRIVE_CELLS, combinedProfile(
                        AppMek163Profile.DRIVE_EXACT_REASON,
                        AppMek163Profile.driveRequiredResources(),
                        identity(
                                AppMek163Profile.MOD_ID,
                                AppMek163Profile.VERSION,
                                AppMek163Profile.JAR_BYTES,
                                AppMek163Profile.JAR_SHA256,
                                AppMek163Profile.DRIVE_EXACT_REASON
                        ),
                        identity(
                                AppMek163Profile.MEKANISM_MOD_ID,
                                AppMek163Profile.MEKANISM_VERSION,
                                AppMek163Profile.MEKANISM_JAR_BYTES,
                                AppMek163Profile.MEKANISM_JAR_SHA256,
                                "exact-atm-1.2.0-mekanism-10.7.19"
                        )
                ))
        );
    }

    @Override
    public void loadResources(Iterable<Path> roots) throws InterruptedException {
        List<Path> snapshot;
        try {
            snapshot = snapshotRoots(roots);
        } catch (RuntimeException exception) {
            disableAll(
                    ExtensionRouteActivation.Reason.RESOURCE_LOAD_CALLBACK_FAILED,
                    "resource-root-snapshot-failed"
            );
            return;
        }
        for (Map.Entry<String, RouteProfile> entry : profiles.entrySet()) {
            try {
                prepareRoute(entry.getKey(), entry.getValue(), snapshot);
            } catch (RuntimeException | LinkageError exception) {
                runtime.route(entry.getKey()).disable(
                        ExtensionRouteActivation.Reason.RESOURCE_LOAD_CALLBACK_FAILED,
                        "resource-load-callback-failed"
                );
            }
        }
    }

    @Override
    public Set<Key> collectUsedTextureKeys() {
        LinkedHashSet<Key> result = new LinkedHashSet<>();
        profiles.forEach((routeId, profile) -> {
            if (runtime.active(routeId)) {
                try {
                    result.addAll(resourceKeys(profile.resources(), "/textures/", ".png"));
                } catch (RuntimeException | LinkageError exception) {
                    runtime.route(routeId).disable(
                            ExtensionRouteActivation.Reason.RESOURCE_LOAD_CALLBACK_FAILED,
                            "texture-key-callback-failed"
                    );
                }
            }
        });
        return Set.copyOf(result);
    }

    @Override
    public void bake() {
        profiles.forEach((routeId, profile) -> {
            ExtensionRouteActivation route = runtime.route(routeId);
            if (!route.isActive()) {
                return;
            }
            try {
                if (!coreActive.getAsBoolean()) {
                    route.inactive(
                            ExtensionRouteActivation.Reason.BLOCKED_BY_CORE,
                            "ae2-core-inactive"
                    );
                } else if (M45Runtime.EXTENDED_PLANES.equals(routeId)
                        && !nativeStructuralActive.getAsBoolean()) {
                    route.inactive(
                            ExtensionRouteActivation.Reason.BLOCKED_BY_CORE,
                            "native-structural-core-inactive"
                    );
                } else if (M45Runtime.APPMEK_DRIVE_CELLS.equals(routeId)
                        && !nativeDriveActive.getAsBoolean()
                        && !nativeDrivePending.getAsBoolean()) {
                    route.inactive(
                            ExtensionRouteActivation.Reason.BLOCKED_BY_CORE,
                            "native-drive-core-inactive"
                    );
                } else if (!resourcesPresent(profile.resources())) {
                    route.inactive(
                            ExtensionRouteActivation.Reason.REQUIRED_RESOURCES_MISMATCH,
                            "required-resources-mismatch"
                    );
                } else if (M45Runtime.ADVANCED_ATHENA.equals(routeId)
                        && !M45AthenaTextures.bake(resourcePack)) {
                    route.inactive(
                            ExtensionRouteActivation.Reason.REQUIRED_RESOURCES_MISMATCH,
                            "static-frame-zero-textures-mismatch"
                    );
                } else if (M45Runtime.APPMEK_DRIVE_CELLS.equals(routeId)
                        && !AppMekResourceModels.driveSupported(resourcePack)) {
                    route.inactive(
                            ExtensionRouteActivation.Reason.REQUIRED_RESOURCES_MISMATCH,
                            "resolved-drive-resources-mismatch"
                    );
                }
            } catch (RuntimeException | LinkageError exception) {
                route.disable(
                        ExtensionRouteActivation.Reason.RESOURCE_BAKE_CALLBACK_FAILED,
                        "resource-bake-callback-failed"
                );
            }
        });
    }

    @Override
    public Key getBlockStateKey(Key key) {
        if (key != null && (CUSTOM_BLOCKS.contains(key) || Z_BLOCKS.contains(key))) {
            String routeId = routeForBlock(key.getFormatted());
            if (routeId != null && runtime.active(routeId)) {
                return SYNTHETIC;
            }
        }
        return key;
    }

    @Override
    public void getBlockProperties(
            BlockState blockState,
            BlockProperties.Builder propertiesBuilder
    ) {
        String id = blockState.getId().getFormatted();
        String routeId = routeForBlock(id);
        if (routeId == null || !runtime.active(routeId)) {
            return;
        }
        if (CUSTOM_BLOCKS.contains(blockState.getId())
                || Z_BLOCKS.contains(blockState.getId())) {
            boolean fullSolid = isFullSolidCustomBlock(id);
            propertiesBuilder
                    .culling(fullSolid)
                    .occluding(fullSolid)
                    .cullingIdentical(false);
        }
    }

    private void prepareRoute(
            String routeId,
            RouteProfile profile,
            List<Path> roots
    ) throws InterruptedException {
        ExtensionRouteActivation route = runtime.route(routeId);
        if (route.isDisabled()) {
            return;
        }
        ProfileDisablement disablement = ProfileDisablement.current();
        if (disablement.isDisabled(routeId)
                || disablement.isDisabled(operatorProfileId(routeId))) {
            route.disable(
                    ExtensionRouteActivation.Reason.OPERATOR_DISABLED,
                    "disabled-by-operator"
            );
            return;
        }
        for (ExactModArtifactDetector detector : profile.detectors()) {
            ExactModArtifactDetector.Detection detection;
            try {
                detection = detector.detect(roots);
            } catch (IOException exception) {
                route.inactive(
                        ExtensionRouteActivation.Reason.ARTIFACT_READ_FAILED,
                        "artifact-read-failed"
                );
                return;
            } catch (RuntimeException | LinkageError exception) {
                route.disable(
                        ExtensionRouteActivation.Reason.RESOURCE_LOAD_CALLBACK_FAILED,
                        "artifact-callback-failed"
                );
                return;
            }
            if (!detection.exact()) {
                route.inactive(reason(detection.failure()), detail(detection.failure()));
                return;
            }
        }
        if (requiresSynthetic(routeId) && !syntheticSupported()) {
            route.inactive(
                    ExtensionRouteActivation.Reason.SYNTHETIC_BLOCK_STATE_INVALID,
                    "synthetic-block-state-invalid"
            );
            return;
        }
        if (M45Runtime.MEGA_CELLS.equals(routeId)
                && !M45Adapter.probeMegaMonitorRetention()) {
            route.disable(
                    ExtensionRouteActivation.Reason.RETENTION_PROBE_FAILED,
                    "retention-probe-failed"
            );
            return;
        }
        route.activate(profile.exactReason());
    }

    private boolean syntheticSupported() {
        de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.BlockState state =
                resourcePack.getBlockStates().get(SYNTHETIC);
        if (state == null) {
            return false;
        }
        Variants variants = state.getVariants();
        if (variants == null
                || variants.getVariants().length != 0
                || variants.getDefaultVariant() == null) {
            return false;
        }
        VariantSet defaultVariant = variants.getDefaultVariant();
        return defaultVariant.getVariants().length == 1
                && M45Adapter.isExpectedSyntheticVariant(defaultVariant.getVariants()[0]);
    }

    private boolean resourcesPresent(Map<String, String> resources) {
        for (String path : resources.keySet()) {
            if (path.contains("/textures/") && path.endsWith(".png")) {
                if (resourcePack.getTextures().get(resourceKey(path, "/textures/", ".png"))
                        == null) {
                    return false;
                }
            } else if (path.contains("/models/") && path.endsWith(".json")) {
                if (resourcePack.getModels().get(resourceKey(path, "/models/", ".json"))
                        == null) {
                    return false;
                }
            } else if (path.contains("/blockstates/") && path.endsWith(".json")) {
                if (resourcePack.getBlockStates().get(
                        resourceKey(path, "/blockstates/", ".json")
                ) == null) {
                    return false;
                }
            }
        }
        return true;
    }

    private static Set<Key> resourceKeys(
            Map<String, String> resources,
            String category,
            String suffix
    ) {
        LinkedHashSet<Key> result = new LinkedHashSet<>();
        resources.keySet().stream()
                .filter(path -> path.contains(category) && path.endsWith(suffix))
                .map(path -> resourceKey(path, category, suffix))
                .forEach(result::add);
        return result;
    }

    private static Key resourceKey(String path, String category, String suffix) {
        int namespaceStart = "assets/".length();
        int categoryStart = path.indexOf(category);
        if (!path.startsWith("assets/") || categoryStart <= namespaceStart
                || !path.endsWith(suffix)) {
            throw new IllegalArgumentException("invalid exact resource path " + path);
        }
        String namespace = path.substring(namespaceStart, categoryStart);
        String value = path.substring(categoryStart + category.length(),
                path.length() - suffix.length());
        return Key.parse(namespace + ":" + value);
    }

    private static String routeForBlock(String id) {
        String crafting = M45CraftingCatalog.route(id);
        if (crafting != null) {
            return crafting;
        }
        if (AdvancedAe1612Catalog.quantumBlockIds().contains(id)) {
            return M45Runtime.ADVANCED_QUANTUM;
        }
        if (ExtendedAe2235Catalog.matrixBlockIds().contains(id)) {
            return M45Runtime.EXTENDED_MATRIX;
        }
        return switch (id) {
            case MeRequester143Catalog.REQUESTER_BLOCK -> M45Runtime.ME_REQUESTER;
            case ExpandedAe211Catalog.IO_PORT_BLOCK -> M45Runtime.EXPANDED_AE;
            case AdvancedAe1612Catalog.QUANTUM_ALLOY_BLOCK ->
                    M45Runtime.ADVANCED_ATHENA;
            default -> null;
        };
    }

    private static boolean requiresSynthetic(String routeId) {
        return M45Runtime.ME_REQUESTER.equals(routeId)
                || M45Runtime.EXPANDED_AE.equals(routeId)
                || M45Runtime.MEGA_CELLS.equals(routeId)
                || M45Runtime.ADVANCED_QUANTUM.equals(routeId)
                || M45Runtime.ADVANCED_ATHENA.equals(routeId)
                || M45Runtime.EXTENDED_MATRIX.equals(routeId);
    }

    private static ExtensionRouteActivation.Reason reason(
            ExactModArtifactDetector.Failure failure
    ) {
        return switch (failure) {
            case NOT_FOUND -> ExtensionRouteActivation.Reason.ARTIFACT_NOT_FOUND;
            case MULTIPLE_ARTIFACTS -> ExtensionRouteActivation.Reason.MULTIPLE_ARTIFACTS;
            case MISMATCH -> ExtensionRouteActivation.Reason.ARTIFACT_MISMATCH;
            case NONE -> throw new IllegalArgumentException("exact detection has no failure");
        };
    }

    private static String detail(ExactModArtifactDetector.Failure failure) {
        return failure.name().toLowerCase(java.util.Locale.ROOT).replace('_', '-');
    }

    private static RouteProfile profile(
            String modId,
            String version,
            long bytes,
            String sha256,
            String exactReason,
            Map<String, String> resources
    ) {
        ExactModArtifactDetector.Identity identity = new ExactModArtifactDetector.Identity(
                modId, version, bytes, sha256, exactReason
        );
        return combinedProfile(exactReason, resources, identity);
    }

    private static RouteProfile combinedProfile(
            String exactReason,
            Map<String, String> resources,
            ExactModArtifactDetector.Identity... identities
    ) {
        return new RouteProfile(
                exactReason,
                java.util.Arrays.stream(identities)
                        .map(ExactModArtifactDetector::new)
                        .toList(),
                Map.copyOf(resources)
        );
    }

    private static ExactModArtifactDetector.Identity identity(
            String modId,
            String version,
            long bytes,
            String sha256,
            String exactReason
    ) {
        return new ExactModArtifactDetector.Identity(
                modId, version, bytes, sha256, exactReason
        );
    }

    private static List<Path> snapshotRoots(Iterable<Path> roots) {
        Objects.requireNonNull(roots, "roots");
        List<Path> result = new ArrayList<>();
        for (Path root : roots) {
            if (result.size() >= MAX_RESOURCE_ROOTS) {
                throw new IllegalArgumentException("too many resource roots");
            }
            result.add(root);
        }
        return List.copyOf(result);
    }

    private static Set<Key> customBlocks() {
        LinkedHashSet<Key> result = M45CraftingCatalog.extensionBlocks().stream()
                .map(Key::parse)
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
        AdvancedAe1612Catalog.quantumBlockIds().stream().map(Key::parse)
                .forEach(result::add);
        result.add(Key.parse(AdvancedAe1612Catalog.QUANTUM_ALLOY_BLOCK));
        ExtendedAe2235Catalog.matrixBlockIds().stream().map(Key::parse)
                .forEach(result::add);
        return Set.copyOf(result);
    }

    private static boolean isFullSolidCustomBlock(String blockId) {
        if (M45CraftingCatalog.extensionBlocks().contains(blockId)
                || Z_BLOCKS.contains(Key.parse(blockId))
                || AdvancedAe1612Catalog.QUANTUM_ALLOY_BLOCK.equals(blockId)) {
            return true;
        }
        if (AdvancedAe1612Catalog.quantumBlockIds().contains(blockId)) {
            return !"advanced_ae:quantum_core".equals(blockId)
                    && !"advanced_ae:quantum_structure".equals(blockId);
        }
        if (ExtendedAe2235Catalog.matrixBlockIds().contains(blockId)) {
            return !"extendedae:assembler_matrix_glass".equals(blockId);
        }
        return false;
    }

    private static String operatorProfileId(String routeId) {
        if (M45Runtime.APPMEK_DRIVE_CELLS.equals(routeId)) {
            return AppMek163Profile.PROFILE_ID;
        }
        return M45Runtime.EXTENDED_MATRIX.equals(routeId)
                || M45Runtime.EXTENDED_PLANES.equals(routeId)
                ? ExtendedAe2235Profile.PROFILE_ID
                : routeId;
    }

    private static Map<String, String> extendedMatrixResources() {
        return filterResources(
                ExtendedAe2235Profile.m5RequiredResources(),
                path -> path.contains("assembler_matrix")
        );
    }

    private static Map<String, String> extendedPlaneResources() {
        Map<String, String> own = filterResources(
                ExtendedAe2235Profile.m5RequiredResources(),
                path -> path.contains("/part/active_formation_plane")
                        || path.contains("/part/smart_annihilation_plane")
        );
        Map<String, String> ae2 = filterResources(
                Ae219217NativeStructuralProfile.requiredResources(),
                path -> path.endsWith("/textures/part/plane_sides.png")
                        || path.endsWith("/textures/part/transition_plane_back.png")
        );
        return mergeResources(own, ae2);
    }

    private static Map<String, String> filterResources(
            Map<String, String> resources,
            java.util.function.Predicate<String> filter
    ) {
        LinkedHashMap<String, String> result = new LinkedHashMap<>();
        resources.forEach((path, digest) -> {
            if (filter.test(path)) {
                result.put(path, digest);
            }
        });
        if (result.isEmpty()) {
            throw new IllegalStateException("empty exact route resource partition");
        }
        return Map.copyOf(result);
    }

    private static Map<String, String> mergeResources(
            Map<String, String> first,
            Map<String, String> second
    ) {
        LinkedHashMap<String, String> result = new LinkedHashMap<>(first);
        second.forEach((path, digest) -> {
            String previous = result.putIfAbsent(path, digest);
            if (previous != null && !previous.equals(digest)) {
                throw new IllegalStateException("conflicting exact route resource digest");
            }
        });
        return Map.copyOf(result);
    }

    private void disableAll(ExtensionRouteActivation.Reason reason, String detail) {
        runtime.routes().forEach(route -> route.disable(reason, detail));
    }

    private record RouteProfile(
            String exactReason,
            List<ExactModArtifactDetector> detectors,
            Map<String, String> resources
    ) {

        private RouteProfile {
            Objects.requireNonNull(exactReason, "exactReason");
            detectors = List.copyOf(detectors);
            resources = Map.copyOf(resources);
            if (detectors.isEmpty() || resources.isEmpty()) {
                throw new IllegalArgumentException("route profile must be closed");
            }
        }
    }
}
