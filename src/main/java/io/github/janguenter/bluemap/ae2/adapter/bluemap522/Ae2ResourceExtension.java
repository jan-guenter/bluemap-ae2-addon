/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.adapter.bluemap522;

import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePackExtension;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.VariantSet;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.Variants;
import de.bluecolored.bluemap.core.util.Key;
import de.bluecolored.bluemap.core.world.BlockProperties;
import de.bluecolored.bluemap.core.world.BlockState;
import io.github.janguenter.bluemap.ae2.activation.CraftingRouteActivation;
import io.github.janguenter.bluemap.ae2.activation.DriveRouteActivation;
import io.github.janguenter.bluemap.ae2.activation.ExtendedAeDriveRouteActivation;
import io.github.janguenter.bluemap.ae2.activation.M3CompletionRouteActivation;
import io.github.janguenter.bluemap.ae2.activation.NativeStructuralRouteActivation;
import io.github.janguenter.bluemap.ae2.activation.ProfileActivation;
import io.github.janguenter.bluemap.ae2.activation.QuantumBridgeRouteActivation;
import io.github.janguenter.bluemap.ae2.activation.QuartzGlassRouteActivation;
import io.github.janguenter.bluemap.ae2.diagnostics.BoundedDiagnostics;
import io.github.janguenter.bluemap.ae2.model.Direction6;
import io.github.janguenter.bluemap.ae2.profile.Ae219217CraftingProfile;
import io.github.janguenter.bluemap.ae2.profile.Ae219217DriveProfile;
import io.github.janguenter.bluemap.ae2.profile.Ae219217NativeStructuralProfile;
import io.github.janguenter.bluemap.ae2.profile.Ae219217Profile;
import io.github.janguenter.bluemap.ae2.profile.Ae219217QuartzGlassProfile;
import io.github.janguenter.bluemap.ae2.profile.ExactArtifactDetector;
import io.github.janguenter.bluemap.ae2.profile.ProfileDisablement;
import io.github.janguenter.bluemap.ae2.profile.extendedae.ExtendedAe2233Profile;
import io.github.janguenter.bluemap.ae2.profile.extendedae.ExtendedAeArtifactDetector;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Exact AE2 profile activation and inactive-by-default blockstate routing. */
final class Ae2ResourceExtension implements ResourcePackExtension {

    private static final int MAX_RESOURCE_ROOTS = 4_096;
    private static final Key CABLE_BUS = Key.parse(Ae219217Profile.CABLE_BUS_BLOCK);
    private static final Key SYNTHETIC = Key.parse(Ae219217Profile.SYNTHETIC_BLOCK_STATE);
    private static final Key DRIVE = Key.parse(Ae219217DriveProfile.DRIVE_BLOCK);
    private static final Key DRIVE_SYNTHETIC = Key.parse(
            Ae219217DriveProfile.SYNTHETIC_BLOCK_STATE
    );
    private static final Key EXTENDED_DRIVE = Key.parse(ExtendedAe2233Profile.BLOCK);
    private static final Key EXTENDED_DRIVE_SYNTHETIC = Key.parse(
            ExtendedAe2233Profile.SYNTHETIC_BLOCK_STATE
    );
    private static final Key QUARTZ_GLASS_SYNTHETIC = Key.parse(
            Ae219217QuartzGlassProfile.SYNTHETIC_BLOCK_STATE
    );
    private static final Set<Key> QUARTZ_GLASS_BLOCKS = Set.of(
            Key.parse(Ae219217QuartzGlassProfile.QUARTZ_GLASS_BLOCK),
            Key.parse(Ae219217QuartzGlassProfile.VIBRANT_GLASS_BLOCK)
    );
    private static final Key CRAFTING_SYNTHETIC = Key.parse(
            Ae219217CraftingProfile.SYNTHETIC_BLOCK_STATE
    );
    private static final Set<Key> CRAFTING_BLOCKS = Set.of(
            Key.parse(Ae219217CraftingProfile.CRAFTING_UNIT_BLOCK),
            Key.parse(Ae219217CraftingProfile.CRAFTING_ACCELERATOR_BLOCK),
            Key.parse(Ae219217CraftingProfile.CRAFTING_STORAGE_1K_BLOCK),
            Key.parse(Ae219217CraftingProfile.CRAFTING_STORAGE_4K_BLOCK),
            Key.parse(Ae219217CraftingProfile.CRAFTING_STORAGE_16K_BLOCK),
            Key.parse(Ae219217CraftingProfile.CRAFTING_STORAGE_64K_BLOCK),
            Key.parse(Ae219217CraftingProfile.CRAFTING_STORAGE_256K_BLOCK),
            Key.parse(Ae219217CraftingProfile.CRAFTING_MONITOR_BLOCK)
    );
    private static final Key QUANTUM_BRIDGE_SYNTHETIC = Key.parse(
            M3eQuantumBridgeResourceModels.SYNTHETIC_BLOCK_STATE
    );
    private static final Set<Key> QUANTUM_BRIDGE_BLOCKS = Set.of(
            Key.parse(M3eQuantumBridgeResourceModels.LINK_BLOCK),
            Key.parse(M3eQuantumBridgeResourceModels.RING_BLOCK)
    );
    private static final String M3_COMPLETION_PROFILE_ID = "ae2-m3-completion";
    private static final Map<Key, Key> M3_COMPLETION_SYNTHETIC_BLOCKS = Map.ofEntries(
            Map.entry(Key.parse("ae2:paint"), Key.parse("bluemap_ae2:paint")),
            Map.entry(
                    Key.parse("ae2:sky_stone_chest"),
                    Key.parse("bluemap_ae2:sky_stone_chest")
            ),
            Map.entry(
                    Key.parse("ae2:smooth_sky_stone_chest"),
                    Key.parse("bluemap_ae2:sky_stone_chest")
            ),
            Map.entry(Key.parse("ae2:crank"), Key.parse("bluemap_ae2:crank")),
            Map.entry(Key.parse("ae2:inscriber"), Key.parse("bluemap_ae2:inscriber")),
            Map.entry(
                    Key.parse("ae2:spatial_pylon"),
                    Key.parse("bluemap_ae2:spatial_pylon")
            )
    );
    private static final Set<Key> M3_COMPLETION_SYNTHETIC_STATES = Set.copyOf(
            M3_COMPLETION_SYNTHETIC_BLOCKS.values()
    );
    private static final Set<Key> REQUIRED_TEXTURES = buildRequiredTextures();
    private static final Set<String> DRIVE_PROPERTY_KEYS = Set.of("facing", "spin");
    private static final Set<String> DRIVE_FACINGS = java.util.Arrays.stream(Direction6.values())
            .map(direction -> direction.name().toLowerCase(java.util.Locale.ROOT))
            .collect(java.util.stream.Collectors.toUnmodifiableSet());
    private static final Set<String> DRIVE_SPINS = Set.of("0", "1", "2", "3");
    private static final Set<String> CRAFTING_PROPERTY_KEYS = Set.of(
            "formed",
            "powered"
    );
    private static final Set<String> CRAFTING_MONITOR_PROPERTY_KEYS = Set.of(
            "facing",
            "formed",
            "powered",
            "spin"
    );
    private static final Set<String> BOOLEAN_VALUES = Set.of("false", "true");
    private static final Set<String> QUANTUM_BRIDGE_PROPERTY_KEYS = Set.of(
            "formed",
            "waterlogged"
    );
    private static final Map<String, String> EXACT_CABLE_BUS_PROPERTIES = Map.of(
            "light_level", "0",
            "waterlogged", "false"
    );

    private final ResourcePack resourcePack;
    private final ProfileActivation activation;
    private final DriveRouteActivation driveActivation;
    private final ExtendedAeDriveRouteActivation extendedDriveActivation;
    private final QuartzGlassRouteActivation quartzGlassActivation;
    private final CraftingRouteActivation craftingActivation;
    private final QuantumBridgeRouteActivation quantumBridgeActivation;
    private final M3CompletionRouteActivation m3CompletionActivation;
    private final NativeStructuralRouteActivation nativeStructuralActivation;
    private final ArtifactDetector artifactDetector;
    private final ExtendedArtifactDetector extendedArtifactDetector;
    private final CraftingProfileProbe craftingProfileProbe;
    private final CraftingRetentionProbe craftingRetentionProbe;
    private final QuantumBridgeProfileProbe quantumBridgeProfileProbe;
    private final QuantumBridgeResourceProbe quantumBridgeResourceProbe;
    private final M3CompletionProfileProbe m3CompletionProfileProbe;
    private final M3CompletionRetentionProbe m3CompletionRetentionProbe;
    private final M3CompletionResourceProbe m3CompletionResourceProbe;
    private final NativeStructuralProfileProbe nativeStructuralProfileProbe;
    private final NativeStructuralRetentionProbe nativeStructuralRetentionProbe;
    private final NativeStructuralResourceProbe nativeStructuralResourceProbe;

    Ae2ResourceExtension(ResourcePack resourcePack, ProfileActivation activation) {
        this(
                resourcePack,
                activation,
                new DriveRouteActivation(),
                new ExtendedAeDriveRouteActivation(),
                new QuartzGlassRouteActivation(),
                new CraftingRouteActivation()
        );
    }

    Ae2ResourceExtension(
            ResourcePack resourcePack,
            ProfileActivation activation,
            DriveRouteActivation driveActivation
    ) {
        this(
                resourcePack,
                activation,
                driveActivation,
                new ExtendedAeDriveRouteActivation(),
                new QuartzGlassRouteActivation(),
                new CraftingRouteActivation()
        );
    }

    Ae2ResourceExtension(
            ResourcePack resourcePack,
            ProfileActivation activation,
            DriveRouteActivation driveActivation,
            ExtendedAeDriveRouteActivation extendedDriveActivation
    ) {
        this(
                resourcePack,
                activation,
                driveActivation,
                extendedDriveActivation,
                new QuartzGlassRouteActivation(),
                new CraftingRouteActivation()
        );
    }

    Ae2ResourceExtension(
            ResourcePack resourcePack,
            ProfileActivation activation,
            DriveRouteActivation driveActivation,
            ExtendedAeDriveRouteActivation extendedDriveActivation,
            QuartzGlassRouteActivation quartzGlassActivation
    ) {
        this(
                resourcePack,
                activation,
                driveActivation,
                extendedDriveActivation,
                quartzGlassActivation,
                new CraftingRouteActivation()
        );
    }

    Ae2ResourceExtension(
            ResourcePack resourcePack,
            ProfileActivation activation,
            DriveRouteActivation driveActivation,
            ExtendedAeDriveRouteActivation extendedDriveActivation,
            QuartzGlassRouteActivation quartzGlassActivation,
            CraftingRouteActivation craftingActivation
    ) {
        this(
                resourcePack,
                activation,
                driveActivation,
                extendedDriveActivation,
                quartzGlassActivation,
                craftingActivation,
                new ExactArtifactDetector()::detect,
                new ExtendedAeArtifactDetector()::detect
        );
    }

    Ae2ResourceExtension(
            ResourcePack resourcePack,
            ProfileActivation activation,
            DriveRouteActivation driveActivation,
            ExtendedAeDriveRouteActivation extendedDriveActivation,
            QuartzGlassRouteActivation quartzGlassActivation,
            CraftingRouteActivation craftingActivation,
            QuantumBridgeRouteActivation quantumBridgeActivation
    ) {
        this(
                resourcePack,
                activation,
                driveActivation,
                extendedDriveActivation,
                quartzGlassActivation,
                craftingActivation,
                quantumBridgeActivation,
                new M3CompletionRouteActivation()
        );
    }

    Ae2ResourceExtension(
            ResourcePack resourcePack,
            ProfileActivation activation,
            DriveRouteActivation driveActivation,
            ExtendedAeDriveRouteActivation extendedDriveActivation,
            QuartzGlassRouteActivation quartzGlassActivation,
            CraftingRouteActivation craftingActivation,
            QuantumBridgeRouteActivation quantumBridgeActivation,
            M3CompletionRouteActivation m3CompletionActivation
    ) {
        this(
                resourcePack,
                activation,
                driveActivation,
                extendedDriveActivation,
                quartzGlassActivation,
                craftingActivation,
                quantumBridgeActivation,
                m3CompletionActivation,
                new ExactArtifactDetector()::detect,
                new ExtendedAeArtifactDetector()::detect,
                Ae2ResourceExtension::exactCraftingProfileAvailable,
                BlueMap522Adapter::probeCraftingMonitorBlockEntityRetention,
                M3eQuantumBridgeResourceModels::exactRouteContractAvailable,
                M3eQuantumBridgeResourceModels::resourcesSupported,
                M3CompletionResourceModels::exactRouteContractAvailable,
                BlueMap522Adapter::probeM3CompletionPaintRetention,
                M3CompletionResourceModels::resourcesSupported
        );
    }

    Ae2ResourceExtension(
            ResourcePack resourcePack,
            ProfileActivation activation,
            DriveRouteActivation driveActivation,
            ExtendedAeDriveRouteActivation extendedDriveActivation,
            QuartzGlassRouteActivation quartzGlassActivation,
            CraftingRouteActivation craftingActivation,
            QuantumBridgeRouteActivation quantumBridgeActivation,
            M3CompletionRouteActivation m3CompletionActivation,
            NativeStructuralRouteActivation nativeStructuralActivation
    ) {
        this(
                resourcePack,
                activation,
                driveActivation,
                extendedDriveActivation,
                quartzGlassActivation,
                craftingActivation,
                quantumBridgeActivation,
                m3CompletionActivation,
                new ExactArtifactDetector()::detect,
                new ExtendedAeArtifactDetector()::detect,
                Ae2ResourceExtension::exactCraftingProfileAvailable,
                BlueMap522Adapter::probeCraftingMonitorBlockEntityRetention,
                M3eQuantumBridgeResourceModels::exactRouteContractAvailable,
                M3eQuantumBridgeResourceModels::resourcesSupported,
                M3CompletionResourceModels::exactRouteContractAvailable,
                BlueMap522Adapter::probeM3CompletionPaintRetention,
                M3CompletionResourceModels::resourcesSupported,
                nativeStructuralActivation,
                NativeStructuralResourceModels::exactRouteContractAvailable,
                BlueMap522Adapter::probeNativeStructuralBlockEntityRetention,
                NativeStructuralResourceModels::resourcesSupported
        );
    }

    Ae2ResourceExtension(
            ResourcePack resourcePack,
            ProfileActivation activation,
            ArtifactDetector artifactDetector
    ) {
        this(
                resourcePack,
                activation,
                new DriveRouteActivation(),
                new ExtendedAeDriveRouteActivation(),
                new QuartzGlassRouteActivation(),
                new CraftingRouteActivation(),
                artifactDetector,
                new ExtendedAeArtifactDetector()::detect
        );
    }

    Ae2ResourceExtension(
            ResourcePack resourcePack,
            ProfileActivation activation,
            DriveRouteActivation driveActivation,
            ArtifactDetector artifactDetector
    ) {
        this(
                resourcePack,
                activation,
                driveActivation,
                new ExtendedAeDriveRouteActivation(),
                new QuartzGlassRouteActivation(),
                new CraftingRouteActivation(),
                artifactDetector,
                new ExtendedAeArtifactDetector()::detect
        );
    }

    Ae2ResourceExtension(
            ResourcePack resourcePack,
            ProfileActivation activation,
            DriveRouteActivation driveActivation,
            ExtendedAeDriveRouteActivation extendedDriveActivation,
            ArtifactDetector artifactDetector,
            ExtendedArtifactDetector extendedArtifactDetector
    ) {
        this(
                resourcePack,
                activation,
                driveActivation,
                extendedDriveActivation,
                new QuartzGlassRouteActivation(),
                new CraftingRouteActivation(),
                artifactDetector,
                extendedArtifactDetector
        );
    }

    Ae2ResourceExtension(
            ResourcePack resourcePack,
            ProfileActivation activation,
            DriveRouteActivation driveActivation,
            ExtendedAeDriveRouteActivation extendedDriveActivation,
            QuartzGlassRouteActivation quartzGlassActivation,
            ArtifactDetector artifactDetector,
            ExtendedArtifactDetector extendedArtifactDetector
    ) {
        this(
                resourcePack,
                activation,
                driveActivation,
                extendedDriveActivation,
                quartzGlassActivation,
                new CraftingRouteActivation(),
                artifactDetector,
                extendedArtifactDetector
        );
    }

    Ae2ResourceExtension(
            ResourcePack resourcePack,
            ProfileActivation activation,
            DriveRouteActivation driveActivation,
            ExtendedAeDriveRouteActivation extendedDriveActivation,
            QuartzGlassRouteActivation quartzGlassActivation,
            CraftingRouteActivation craftingActivation,
            ArtifactDetector artifactDetector,
            ExtendedArtifactDetector extendedArtifactDetector
    ) {
        this(
                resourcePack,
                activation,
                driveActivation,
                extendedDriveActivation,
                quartzGlassActivation,
                craftingActivation,
                artifactDetector,
                extendedArtifactDetector,
                Ae2ResourceExtension::exactCraftingProfileAvailable
        );
    }

    Ae2ResourceExtension(
            ResourcePack resourcePack,
            ProfileActivation activation,
            DriveRouteActivation driveActivation,
            ExtendedAeDriveRouteActivation extendedDriveActivation,
            QuartzGlassRouteActivation quartzGlassActivation,
            CraftingRouteActivation craftingActivation,
            ArtifactDetector artifactDetector,
            ExtendedArtifactDetector extendedArtifactDetector,
            CraftingProfileProbe craftingProfileProbe
    ) {
        this(
                resourcePack,
                activation,
                driveActivation,
                extendedDriveActivation,
                quartzGlassActivation,
                craftingActivation,
                artifactDetector,
                extendedArtifactDetector,
                craftingProfileProbe,
                BlueMap522Adapter::probeCraftingMonitorBlockEntityRetention
        );
    }

    Ae2ResourceExtension(
            ResourcePack resourcePack,
            ProfileActivation activation,
            DriveRouteActivation driveActivation,
            ExtendedAeDriveRouteActivation extendedDriveActivation,
            QuartzGlassRouteActivation quartzGlassActivation,
            CraftingRouteActivation craftingActivation,
            ArtifactDetector artifactDetector,
            ExtendedArtifactDetector extendedArtifactDetector,
            CraftingProfileProbe craftingProfileProbe,
            CraftingRetentionProbe craftingRetentionProbe
    ) {
        this(
                resourcePack,
                activation,
                driveActivation,
                extendedDriveActivation,
                quartzGlassActivation,
                craftingActivation,
                new QuantumBridgeRouteActivation(),
                artifactDetector,
                extendedArtifactDetector,
                craftingProfileProbe,
                craftingRetentionProbe,
                M3eQuantumBridgeResourceModels::exactRouteContractAvailable,
                M3eQuantumBridgeResourceModels::resourcesSupported
        );
    }

    Ae2ResourceExtension(
            ResourcePack resourcePack,
            ProfileActivation activation,
            DriveRouteActivation driveActivation,
            ExtendedAeDriveRouteActivation extendedDriveActivation,
            QuartzGlassRouteActivation quartzGlassActivation,
            CraftingRouteActivation craftingActivation,
            QuantumBridgeRouteActivation quantumBridgeActivation,
            ArtifactDetector artifactDetector,
            ExtendedArtifactDetector extendedArtifactDetector,
            CraftingProfileProbe craftingProfileProbe,
            CraftingRetentionProbe craftingRetentionProbe,
            QuantumBridgeProfileProbe quantumBridgeProfileProbe
    ) {
        this(
                resourcePack,
                activation,
                driveActivation,
                extendedDriveActivation,
                quartzGlassActivation,
                craftingActivation,
                quantumBridgeActivation,
                artifactDetector,
                extendedArtifactDetector,
                craftingProfileProbe,
                craftingRetentionProbe,
                quantumBridgeProfileProbe,
                M3eQuantumBridgeResourceModels::resourcesSupported
        );
    }

    Ae2ResourceExtension(
            ResourcePack resourcePack,
            ProfileActivation activation,
            DriveRouteActivation driveActivation,
            ExtendedAeDriveRouteActivation extendedDriveActivation,
            QuartzGlassRouteActivation quartzGlassActivation,
            CraftingRouteActivation craftingActivation,
            QuantumBridgeRouteActivation quantumBridgeActivation,
            ArtifactDetector artifactDetector,
            ExtendedArtifactDetector extendedArtifactDetector,
            CraftingProfileProbe craftingProfileProbe,
            CraftingRetentionProbe craftingRetentionProbe,
            QuantumBridgeProfileProbe quantumBridgeProfileProbe,
            QuantumBridgeResourceProbe quantumBridgeResourceProbe
    ) {
        this(
                resourcePack,
                activation,
                driveActivation,
                extendedDriveActivation,
                quartzGlassActivation,
                craftingActivation,
                quantumBridgeActivation,
                new M3CompletionRouteActivation(),
                artifactDetector,
                extendedArtifactDetector,
                craftingProfileProbe,
                craftingRetentionProbe,
                quantumBridgeProfileProbe,
                quantumBridgeResourceProbe,
                M3CompletionResourceModels::exactRouteContractAvailable,
                BlueMap522Adapter::probeM3CompletionPaintRetention,
                M3CompletionResourceModels::resourcesSupported
        );
    }

    Ae2ResourceExtension(
            ResourcePack resourcePack,
            ProfileActivation activation,
            DriveRouteActivation driveActivation,
            ExtendedAeDriveRouteActivation extendedDriveActivation,
            QuartzGlassRouteActivation quartzGlassActivation,
            CraftingRouteActivation craftingActivation,
            QuantumBridgeRouteActivation quantumBridgeActivation,
            M3CompletionRouteActivation m3CompletionActivation,
            ArtifactDetector artifactDetector,
            ExtendedArtifactDetector extendedArtifactDetector,
            CraftingProfileProbe craftingProfileProbe,
            CraftingRetentionProbe craftingRetentionProbe,
            QuantumBridgeProfileProbe quantumBridgeProfileProbe,
            QuantumBridgeResourceProbe quantumBridgeResourceProbe,
            M3CompletionProfileProbe m3CompletionProfileProbe,
            M3CompletionRetentionProbe m3CompletionRetentionProbe,
            M3CompletionResourceProbe m3CompletionResourceProbe
    ) {
        this(
                resourcePack,
                activation,
                driveActivation,
                extendedDriveActivation,
                quartzGlassActivation,
                craftingActivation,
                quantumBridgeActivation,
                m3CompletionActivation,
                artifactDetector,
                extendedArtifactDetector,
                craftingProfileProbe,
                craftingRetentionProbe,
                quantumBridgeProfileProbe,
                quantumBridgeResourceProbe,
                m3CompletionProfileProbe,
                m3CompletionRetentionProbe,
                m3CompletionResourceProbe,
                new NativeStructuralRouteActivation(),
                () -> false,
                () -> false,
                candidate -> false
        );
    }

    Ae2ResourceExtension(
            ResourcePack resourcePack,
            ProfileActivation activation,
            DriveRouteActivation driveActivation,
            ExtendedAeDriveRouteActivation extendedDriveActivation,
            QuartzGlassRouteActivation quartzGlassActivation,
            CraftingRouteActivation craftingActivation,
            QuantumBridgeRouteActivation quantumBridgeActivation,
            M3CompletionRouteActivation m3CompletionActivation,
            ArtifactDetector artifactDetector,
            ExtendedArtifactDetector extendedArtifactDetector,
            CraftingProfileProbe craftingProfileProbe,
            CraftingRetentionProbe craftingRetentionProbe,
            QuantumBridgeProfileProbe quantumBridgeProfileProbe,
            QuantumBridgeResourceProbe quantumBridgeResourceProbe,
            M3CompletionProfileProbe m3CompletionProfileProbe,
            M3CompletionRetentionProbe m3CompletionRetentionProbe,
            M3CompletionResourceProbe m3CompletionResourceProbe,
            NativeStructuralRouteActivation nativeStructuralActivation,
            NativeStructuralProfileProbe nativeStructuralProfileProbe,
            NativeStructuralRetentionProbe nativeStructuralRetentionProbe,
            NativeStructuralResourceProbe nativeStructuralResourceProbe
    ) {
        this.resourcePack = resourcePack;
        this.activation = activation;
        this.driveActivation = driveActivation;
        this.extendedDriveActivation = extendedDriveActivation;
        this.quartzGlassActivation = quartzGlassActivation;
        this.craftingActivation = craftingActivation;
        this.quantumBridgeActivation = quantumBridgeActivation;
        this.m3CompletionActivation = m3CompletionActivation;
        this.nativeStructuralActivation = nativeStructuralActivation;
        this.artifactDetector = artifactDetector;
        this.extendedArtifactDetector = extendedArtifactDetector;
        this.craftingProfileProbe = craftingProfileProbe;
        this.craftingRetentionProbe = craftingRetentionProbe;
        this.quantumBridgeProfileProbe = quantumBridgeProfileProbe;
        this.quantumBridgeResourceProbe = quantumBridgeResourceProbe;
        this.m3CompletionProfileProbe = m3CompletionProfileProbe;
        this.m3CompletionRetentionProbe = m3CompletionRetentionProbe;
        this.m3CompletionResourceProbe = m3CompletionResourceProbe;
        this.nativeStructuralProfileProbe = nativeStructuralProfileProbe;
        this.nativeStructuralRetentionProbe = nativeStructuralRetentionProbe;
        this.nativeStructuralResourceProbe = nativeStructuralResourceProbe;
    }

    @Override
    public void loadResources(Iterable<Path> roots) throws InterruptedException {
        try {
            loadResourcesChecked(snapshotRoots(roots));
        } catch (InterruptedException exception) {
            throw exception;
        } catch (RuntimeException | LinkageError exception) {
            disableForResourceCallbackFailure(
                    ProfileActivation.Reason.RESOURCE_LOAD_CALLBACK_FAILED
            );
        }
    }

    private void loadResourcesChecked(List<Path> roots) throws InterruptedException {
        if (activation.isDisabled()) {
            return;
        }
        if (ProfileDisablement.current().isDisabled(Ae219217Profile.PROFILE_ID)) {
            activation.disable(ProfileActivation.Reason.OPERATOR_DISABLED);
            BoundedDiagnostics.report(BoundedDiagnostics.Event.PROFILE_DISABLED);
            return;
        }

        ExactArtifactDetector.Detection detection;
        try {
            detection = artifactDetector.detect(roots);
        } catch (IOException exception) {
            activation.inactive(ProfileActivation.Reason.ARTIFACT_READ_FAILED);
            BoundedDiagnostics.report(BoundedDiagnostics.Event.ARTIFACT_READ_FAILED);
            return;
        }
        if (!detection.exact()) {
            activation.inactive(reasonForDetection(detection.reason()));
            reportDetectionFailure(detection.reason());
            return;
        }

        de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.BlockState synthetic =
                resourcePack.getBlockStates().get(SYNTHETIC);
        if (synthetic == null) {
            activation.inactive(ProfileActivation.Reason.SYNTHETIC_BLOCK_STATE_MISSING);
            BoundedDiagnostics.report(BoundedDiagnostics.Event.REQUIRED_RESOURCES_MISMATCH);
            return;
        }
        if (!isExpectedSyntheticBlockState(synthetic)) {
            activation.inactive(ProfileActivation.Reason.SYNTHETIC_BLOCK_STATE_INVALID);
            BoundedDiagnostics.report(BoundedDiagnostics.Event.REQUIRED_RESOURCES_MISMATCH);
            return;
        }

        // This is deliberately the first BlueNBT use by the add-on. It runs
        // only after all add-on entrypoints have had an opportunity to register.
        if (!BlueMap522Adapter.probeBlockEntityRetention()) {
            activation.disable(ProfileActivation.Reason.BLUENBT_RETENTION_PROBE_FAILED);
            BoundedDiagnostics.report(BoundedDiagnostics.Event.RETENTION_PROBE_FAILED);
            return;
        }

        activation.activate();
        BoundedDiagnostics.report(BoundedDiagnostics.Event.PROFILE_ACTIVATED);
        prepareDriveRouteSafely();
        prepareQuartzGlassRouteSafely();
        prepareCraftingRouteSafely();
        prepareQuantumBridgeRouteSafely();
        prepareM3CompletionRouteSafely();
        prepareNativeStructuralRouteSafely();
        prepareExtendedDriveRouteSafely(roots);
    }

    @Override
    public Set<Key> collectUsedTextureKeys() {
        if (!activation.isActive()) {
            return Set.of();
        }
        java.util.LinkedHashSet<Key> textures = new java.util.LinkedHashSet<>(
                REQUIRED_TEXTURES
        );
        if (driveActivation.isActive()) {
            textures.addAll(M3DriveResourceModels.requiredTextures());
        }
        if (extendedDriveActivation.isActive()) {
            textures.addAll(M3bExtendedAeDriveResourceModels.requiredTextures());
        }
        if (quartzGlassActivation.isActive()) {
            try {
                textures.addAll(M3cQuartzGlassResourceModels.requiredTextures());
            } catch (RuntimeException | LinkageError exception) {
                quartzGlassActivation.disable(
                        QuartzGlassRouteActivation.Reason.RESOURCE_LOAD_CALLBACK_FAILED
                );
                BoundedDiagnostics.report(
                        BoundedDiagnostics.Event.QUARTZ_GLASS_RESOURCE_CALLBACK_FAILED
                );
            }
        }
        if (craftingActivation.isActive()) {
            try {
                textures.addAll(M3dCraftingResourceModels.requiredTextures());
            } catch (RuntimeException | LinkageError exception) {
                craftingActivation.disable(
                        CraftingRouteActivation.Reason.RESOURCE_LOAD_CALLBACK_FAILED
                );
                BoundedDiagnostics.report(
                        BoundedDiagnostics.Event.CRAFTING_RESOURCE_CALLBACK_FAILED
                );
            }
        }
        if (quantumBridgeActivation.isActive()) {
            try {
                textures.addAll(M3eQuantumBridgeResourceModels.requiredTextures());
            } catch (RuntimeException | LinkageError exception) {
                quantumBridgeActivation.disable(
                        QuantumBridgeRouteActivation.Reason.RESOURCE_LOAD_CALLBACK_FAILED
                );
                BoundedDiagnostics.report(
                        BoundedDiagnostics.Event.QUANTUM_BRIDGE_RESOURCE_CALLBACK_FAILED
                );
            }
        }
        if (m3CompletionActivation.isActive()) {
            try {
                textures.addAll(M3CompletionResourceModels.requiredTextures());
            } catch (RuntimeException | LinkageError exception) {
                m3CompletionActivation.disable(
                        M3CompletionRouteActivation.Reason
                                .RESOURCE_LOAD_CALLBACK_FAILED
                );
                BoundedDiagnostics.report(
                        BoundedDiagnostics.Event.M3_COMPLETION_RESOURCE_CALLBACK_FAILED
                );
            }
        }
        if (nativeStructuralActivation.isActive()) {
            try {
                textures.addAll(NativeStructuralResourceModels.requiredTextures());
                textures.addAll(M3cQuartzGlassResourceModels.requiredTextures());
            } catch (RuntimeException | LinkageError exception) {
                nativeStructuralActivation.disable(
                        NativeStructuralRouteActivation.Reason
                                .RESOURCE_LOAD_CALLBACK_FAILED
                );
                BoundedDiagnostics.report(
                        BoundedDiagnostics.Event
                                .NATIVE_STRUCTURAL_RESOURCE_CALLBACK_FAILED
                );
            }
        }
        return Set.copyOf(textures);
    }

    @Override
    public void bake() {
        if (activation.isActive()) {
            try {
                if (!hasRequiredTextures(resourcePack)) {
                    activation.inactive(ProfileActivation.Reason.REQUIRED_TEXTURE_MISSING);
                    BoundedDiagnostics.report(BoundedDiagnostics.Event.TEXTURE_MISSING);
                } else if (!M2ResourceModels.terminalModelsSupported(resourcePack)
                        || M2ResourceModels.resolveStoneTexture(resourcePack) == null) {
                    activation.inactive(ProfileActivation.Reason.REQUIRED_RESOURCES_MISMATCH);
                    BoundedDiagnostics.report(
                            BoundedDiagnostics.Event.REQUIRED_RESOURCES_MISMATCH
                    );
                }
            } catch (RuntimeException | LinkageError exception) {
                disableForResourceCallbackFailure(
                        ProfileActivation.Reason.RESOURCE_BAKE_CALLBACK_FAILED
                );
            }
        }
        bakeDriveRouteSafely();
        bakeExtendedDriveRouteSafely();
        bakeQuartzGlassRouteSafely();
        bakeCraftingRouteSafely();
        bakeQuantumBridgeRouteSafely();
        bakeM3CompletionRouteSafely();
        bakeNativeStructuralRouteSafely();
        // BlueMap does not define resource-extension bake order. Reconcile the
        // optional M4/M5 routes here as well as in their own bake callback so a
        // late core-resource failure cannot leave an extension route active.
        M45Adapter.reconcileCoreAfterBake(
                activation.isActive(),
                nativeStructuralActivation.isActive(),
                driveActivation.isActive()
        );
    }

    @Override
    public Key getBlockStateKey(Key key) {
        if (activation.isActive() && CABLE_BUS.equals(key)) {
            return SYNTHETIC;
        }
        if (activation.isActive() && driveActivation.isActive() && DRIVE.equals(key)) {
            return DRIVE_SYNTHETIC;
        }
        if (activation.isActive()
                && extendedDriveActivation.isActive()
                && EXTENDED_DRIVE.equals(key)) {
            return EXTENDED_DRIVE_SYNTHETIC;
        }
        if (activation.isActive()
                && quartzGlassActivation.isActive()
                && QUARTZ_GLASS_BLOCKS.contains(key)) {
            return QUARTZ_GLASS_SYNTHETIC;
        }
        if (activation.isActive()
                && craftingActivation.isActive()
                && CRAFTING_BLOCKS.contains(key)) {
            return CRAFTING_SYNTHETIC;
        }
        if (activation.isActive()
                && quantumBridgeActivation.isActive()
                && QUANTUM_BRIDGE_BLOCKS.contains(key)) {
            return QUANTUM_BRIDGE_SYNTHETIC;
        }
        if (activation.isActive() && m3CompletionActivation.isActive()) {
            Key synthetic = M3_COMPLETION_SYNTHETIC_BLOCKS.get(key);
            if (synthetic != null) {
                return synthetic;
            }
        }
        return key;
    }

    @Override
    public void getBlockProperties(
            BlockState blockState,
            BlockProperties.Builder propertiesBuilder
    ) {
        if (activation.isActive() && CABLE_BUS.equals(blockState.getId())) {
            // The synthetic missing-model cube is dispatch-only. Cable buses
            // and their stock fallback must never inherit full-cube occlusion.
            propertiesBuilder
                    .culling(false)
                    .occluding(false)
                    .cullingIdentical(false);
        } else if (activation.isActive() && driveActivation.isActive()
                && DRIVE.equals(blockState.getId())) {
            // The exact client chassis is full-cube occluding, but M3a keeps
            // dispatch and stock fallback safe until a conditional strategy is proven.
            propertiesBuilder
                    .culling(false)
                    .occluding(false)
                    .cullingIdentical(false);
        } else if (activation.isActive() && extendedDriveActivation.isActive()
                && EXTENDED_DRIVE.equals(blockState.getId())) {
            propertiesBuilder
                    .culling(false)
                    .occluding(false)
                    .cullingIdentical(false);
        } else if (activation.isActive() && quartzGlassActivation.isActive()
                && isQuartzGlassId(blockState.getId())) {
            propertiesBuilder
                    .culling(false)
                    .occluding(false)
                    .cullingIdentical(false);
        } else if (activation.isActive() && craftingActivation.isActive()
                && isCraftingId(blockState.getId())) {
            // All exact crafting blocks use forceSolidOn. Preserve full-block
            // culling/occlusion even though the formed model uses CUTOUT.
            propertiesBuilder
                    .culling(Ae219217CraftingProfile.FULL_SOLID)
                    .occluding(Ae219217CraftingProfile.OCCLUDING)
                    .cullingIdentical(false);
        } else if (activation.isActive() && quantumBridgeActivation.isActive()
                && isQuantumBridgeId(blockState.getId())) {
            propertiesBuilder
                    .culling(false)
                    .occluding(false)
                    .cullingIdentical(false);
        } else if (activation.isActive() && m3CompletionActivation.isActive()
                && isM3CompletionId(blockState.getId())) {
            // Every custom projection is non-culling. This also keeps its
            // atomic stock fallback from hiding neighboring geometry.
            propertiesBuilder
                    .culling(false)
                    .occluding(false)
                    .cullingIdentical(false);
        }
    }

    static boolean isExactCableBusState(BlockState blockState) {
        return CABLE_BUS.equals(blockState.getId())
                && EXACT_CABLE_BUS_PROPERTIES.equals(blockState.getProperties());
    }

    static boolean isExactDriveState(BlockState blockState) {
        if (!DRIVE.equals(blockState.getId())
                || !DRIVE_PROPERTY_KEYS.equals(blockState.getProperties().keySet())) {
            return false;
        }
        return DRIVE_FACINGS.contains(blockState.getProperties().get("facing"))
                && DRIVE_SPINS.contains(blockState.getProperties().get("spin"));
    }

    static boolean isExactExtendedDriveState(BlockState blockState) {
        return ExtendedAeDriveRenderer.isExactState(blockState);
    }

    static boolean isQuartzGlassId(Key blockId) {
        return QUARTZ_GLASS_BLOCKS.contains(blockId);
    }

    static boolean isExactQuartzGlassState(BlockState blockState) {
        return blockState != null
                && isQuartzGlassId(blockState.getId())
                && blockState.getProperties().isEmpty();
    }

    static boolean isCraftingId(Key blockId) {
        return CRAFTING_BLOCKS.contains(blockId);
    }

    static boolean isExactCraftingNeighborState(BlockState blockState) {
        if (blockState == null || !isCraftingId(blockState.getId())) {
            return false;
        }
        Map<String, String> properties = blockState.getProperties();
        boolean monitor = Ae219217CraftingProfile.CRAFTING_MONITOR_BLOCK.equals(
                blockState.getId().getFormatted()
        );
        Set<String> expectedKeys = monitor
                ? CRAFTING_MONITOR_PROPERTY_KEYS
                : CRAFTING_PROPERTY_KEYS;
        if (!expectedKeys.equals(properties.keySet())
                || !BOOLEAN_VALUES.contains(properties.get("formed"))
                || !BOOLEAN_VALUES.contains(properties.get("powered"))) {
            return false;
        }
        return !monitor || DRIVE_FACINGS.contains(properties.get("facing"))
                && DRIVE_SPINS.contains(properties.get("spin"));
    }

    static boolean isExactFormedCraftingState(BlockState blockState) {
        return isExactCraftingNeighborState(blockState)
                && "true".equals(blockState.getProperties().get("formed"));
    }

    static boolean isQuantumBridgeId(Key blockId) {
        return QUANTUM_BRIDGE_BLOCKS.contains(blockId);
    }

    static boolean isExactQuantumBridgeState(BlockState blockState) {
        if (blockState == null
                || !isQuantumBridgeId(blockState.getId())
                || !QUANTUM_BRIDGE_PROPERTY_KEYS.equals(
                        blockState.getProperties().keySet()
                )) {
            return false;
        }
        return BOOLEAN_VALUES.contains(blockState.getProperties().get("formed"))
                && BOOLEAN_VALUES.contains(blockState.getProperties().get("waterlogged"));
    }

    static boolean isExactFormedQuantumBridgeState(BlockState blockState) {
        return isExactQuantumBridgeState(blockState)
                && "true".equals(blockState.getProperties().get("formed"));
    }

    static boolean isM3CompletionId(Key blockId) {
        return M3_COMPLETION_SYNTHETIC_BLOCKS.containsKey(blockId);
    }

    static boolean isExactM3CompletionState(BlockState blockState) {
        return M3CompletionRenderer.exactKind(blockState) != null;
    }

    static boolean hasRequiredTextures(ResourcePack resourcePack) {
        for (Key texture : REQUIRED_TEXTURES) {
            if (ResourcePack.MISSING_TEXTURE.equals(texture)
                    || resourcePack.getTextures().get(texture) == null) {
                return false;
            }
        }
        return true;
    }

    static boolean isExpectedSyntheticBlockState(
            de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.BlockState resource
    ) {
        if (resource == null || resource.getMultipart() != null) {
            return false;
        }
        Variants variants = resource.getVariants();
        if (variants == null
                || variants.getVariants().length != 0
                || variants.getDefaultVariant() == null) {
            return false;
        }
        VariantSet defaultVariant = variants.getDefaultVariant();
        return defaultVariant.getVariants().length == 1
                && BlueMap522Adapter.isExpectedSyntheticVariant(
                        defaultVariant.getVariants()[0]
                );
    }

    static boolean isExpectedDriveSyntheticBlockState(
            de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.BlockState resource
    ) {
        if (resource == null || resource.getMultipart() != null) {
            return false;
        }
        Variants variants = resource.getVariants();
        if (variants == null
                || variants.getVariants().length != 0
                || variants.getDefaultVariant() == null) {
            return false;
        }
        VariantSet defaultVariant = variants.getDefaultVariant();
        return defaultVariant.getVariants().length == 1
                && BlueMap522Adapter.isExpectedDriveSyntheticVariant(
                        defaultVariant.getVariants()[0]
                );
    }

    static boolean isExpectedExtendedDriveSyntheticBlockState(
            de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.BlockState resource
    ) {
        if (resource == null || resource.getMultipart() != null) {
            return false;
        }
        Variants variants = resource.getVariants();
        if (variants == null
                || variants.getVariants().length != 0
                || variants.getDefaultVariant() == null) {
            return false;
        }
        VariantSet defaultVariant = variants.getDefaultVariant();
        return defaultVariant.getVariants().length == 1
                && BlueMap522Adapter.isExpectedExtendedDriveSyntheticVariant(
                        defaultVariant.getVariants()[0]
                );
    }

    static boolean isExpectedQuartzGlassSyntheticBlockState(
            de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.BlockState resource
    ) {
        if (resource == null || resource.getMultipart() != null) {
            return false;
        }
        Variants variants = resource.getVariants();
        if (variants == null
                || variants.getVariants().length != 0
                || variants.getDefaultVariant() == null) {
            return false;
        }
        VariantSet defaultVariant = variants.getDefaultVariant();
        return defaultVariant.getVariants().length == 1
                && BlueMap522Adapter.isExpectedQuartzGlassSyntheticVariant(
                        defaultVariant.getVariants()[0]
                );
    }

    static boolean isExpectedCraftingSyntheticBlockState(
            de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.BlockState
                    resource
    ) {
        if (resource == null || resource.getMultipart() != null) {
            return false;
        }
        Variants variants = resource.getVariants();
        if (variants == null
                || variants.getVariants().length != 0
                || variants.getDefaultVariant() == null) {
            return false;
        }
        VariantSet defaultVariant = variants.getDefaultVariant();
        return defaultVariant.getVariants().length == 1
                && BlueMap522Adapter.isExpectedCraftingSyntheticVariant(
                        defaultVariant.getVariants()[0]
                );
    }

    static boolean isExpectedQuantumBridgeSyntheticBlockState(
            de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.BlockState
                    resource
    ) {
        if (resource == null || resource.getMultipart() != null) {
            return false;
        }
        Variants variants = resource.getVariants();
        if (variants == null
                || variants.getVariants().length != 0
                || variants.getDefaultVariant() == null) {
            return false;
        }
        VariantSet defaultVariant = variants.getDefaultVariant();
        return defaultVariant.getVariants().length == 1
                && BlueMap522Adapter.isExpectedQuantumBridgeSyntheticVariant(
                        defaultVariant.getVariants()[0]
                );
    }

    static boolean isExpectedM3CompletionSyntheticBlockState(
            de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.BlockState
                    resource
    ) {
        if (resource == null || resource.getMultipart() != null) {
            return false;
        }
        Variants variants = resource.getVariants();
        if (variants == null
                || variants.getVariants().length != 0
                || variants.getDefaultVariant() == null) {
            return false;
        }
        VariantSet defaultVariant = variants.getDefaultVariant();
        return defaultVariant.getVariants().length == 1
                && BlueMap522Adapter.isExpectedM3CompletionSyntheticVariant(
                        defaultVariant.getVariants()[0]
                );
    }

    private static Set<Key> buildRequiredTextures() {
        java.util.LinkedHashSet<Key> textures = Ae219217Profile.coreTextures().stream()
                .map(Key::parse)
                .collect(java.util.stream.Collectors.toCollection(
                        java.util.LinkedHashSet::new
                ));
        textures.addAll(M2ResourceModels.requiredTextures());
        return Set.copyOf(textures);
    }

    private void prepareDriveRouteSafely() {
        if (driveActivation.isDisabled()) {
            return;
        }
        try {
            de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.BlockState
                    synthetic = resourcePack.getBlockStates().get(DRIVE_SYNTHETIC);
            if (synthetic == null) {
                driveActivation.inactive(
                        DriveRouteActivation.Reason.SYNTHETIC_BLOCK_STATE_MISSING
                );
                BoundedDiagnostics.report(
                        BoundedDiagnostics.Event.DRIVE_REQUIRED_RESOURCES_MISMATCH
                );
                return;
            }
            if (!isExpectedDriveSyntheticBlockState(synthetic)) {
                driveActivation.inactive(
                        DriveRouteActivation.Reason.SYNTHETIC_BLOCK_STATE_INVALID
                );
                BoundedDiagnostics.report(
                        BoundedDiagnostics.Event.DRIVE_REQUIRED_RESOURCES_MISMATCH
                );
                return;
            }
            if (!BlueMap522Adapter.probeDriveBlockEntityRetention()) {
                driveActivation.disable(
                        DriveRouteActivation.Reason.BLUENBT_RETENTION_PROBE_FAILED
                );
                BoundedDiagnostics.report(
                        BoundedDiagnostics.Event.DRIVE_RETENTION_PROBE_FAILED
                );
                return;
            }
            driveActivation.activate();
            BoundedDiagnostics.report(BoundedDiagnostics.Event.DRIVE_ROUTE_ACTIVATED);
        } catch (RuntimeException | LinkageError exception) {
            driveActivation.disable(
                    DriveRouteActivation.Reason.RESOURCE_LOAD_CALLBACK_FAILED
            );
            BoundedDiagnostics.report(
                    BoundedDiagnostics.Event.DRIVE_RESOURCE_CALLBACK_FAILED
            );
        }
    }

    private void prepareExtendedDriveRouteSafely(List<Path> roots)
            throws InterruptedException {
        if (extendedDriveActivation.isDisabled()) {
            return;
        }
        if (ProfileDisablement.current().isDisabled(ExtendedAe2233Profile.PROFILE_ID)) {
            extendedDriveActivation.disable(
                    ExtendedAeDriveRouteActivation.Reason.OPERATOR_DISABLED
            );
            BoundedDiagnostics.report(BoundedDiagnostics.Event.EXTENDED_PROFILE_DISABLED);
            return;
        }

        try {
            ExtendedAeArtifactDetector.Detection detection =
                    extendedArtifactDetector.detect(roots);
            if (!detection.exact()) {
                extendedDriveActivation.inactive(
                        extendedReasonForDetection(detection.reason())
                );
                reportExtendedDetectionFailure(detection.reason());
                return;
            }

            de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.BlockState
                    synthetic = resourcePack.getBlockStates().get(
                            EXTENDED_DRIVE_SYNTHETIC
                    );
            if (synthetic == null) {
                extendedDriveActivation.inactive(
                        ExtendedAeDriveRouteActivation.Reason
                                .SYNTHETIC_BLOCK_STATE_MISSING
                );
                BoundedDiagnostics.report(
                        BoundedDiagnostics.Event.EXTENDED_DRIVE_REQUIRED_RESOURCES_MISMATCH
                );
                return;
            }
            if (!isExpectedExtendedDriveSyntheticBlockState(synthetic)) {
                extendedDriveActivation.inactive(
                        ExtendedAeDriveRouteActivation.Reason
                                .SYNTHETIC_BLOCK_STATE_INVALID
                );
                BoundedDiagnostics.report(
                        BoundedDiagnostics.Event.EXTENDED_DRIVE_REQUIRED_RESOURCES_MISMATCH
                );
                return;
            }
            if (!BlueMap522Adapter.probeExtendedDriveBlockEntityRetention()) {
                extendedDriveActivation.disable(
                        ExtendedAeDriveRouteActivation.Reason
                                .BLUENBT_RETENTION_PROBE_FAILED
                );
                BoundedDiagnostics.report(
                        BoundedDiagnostics.Event.EXTENDED_DRIVE_RETENTION_PROBE_FAILED
                );
                return;
            }
            extendedDriveActivation.activate(detection.reason());
            BoundedDiagnostics.report(
                    BoundedDiagnostics.Event.EXTENDED_DRIVE_ROUTE_ACTIVATED
            );
        } catch (IOException exception) {
            extendedDriveActivation.inactive(
                    ExtendedAeDriveRouteActivation.Reason.ARTIFACT_READ_FAILED
            );
            BoundedDiagnostics.report(
                    BoundedDiagnostics.Event.EXTENDED_ARTIFACT_READ_FAILED
            );
        } catch (RuntimeException | LinkageError exception) {
            extendedDriveActivation.disable(
                    ExtendedAeDriveRouteActivation.Reason.RESOURCE_LOAD_CALLBACK_FAILED
            );
            BoundedDiagnostics.report(
                    BoundedDiagnostics.Event.EXTENDED_DRIVE_RESOURCE_CALLBACK_FAILED
            );
        }
    }

    private void prepareQuartzGlassRouteSafely() {
        if (quartzGlassActivation.isDisabled()) {
            return;
        }
        try {
            if (ProfileDisablement.current().isDisabled(
                    Ae219217QuartzGlassProfile.PROFILE_ID
            )) {
                quartzGlassActivation.disable(
                        QuartzGlassRouteActivation.Reason.OPERATOR_DISABLED
                );
                BoundedDiagnostics.report(
                        BoundedDiagnostics.Event.QUARTZ_GLASS_PROFILE_DISABLED
                );
                return;
            }
            de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.BlockState
                    synthetic = resourcePack.getBlockStates().get(QUARTZ_GLASS_SYNTHETIC);
            if (synthetic == null) {
                quartzGlassActivation.inactive(
                        QuartzGlassRouteActivation.Reason.SYNTHETIC_BLOCK_STATE_MISSING
                );
                BoundedDiagnostics.report(
                        BoundedDiagnostics.Event.QUARTZ_GLASS_REQUIRED_RESOURCES_MISMATCH
                );
                return;
            }
            if (!isExpectedQuartzGlassSyntheticBlockState(synthetic)) {
                quartzGlassActivation.inactive(
                        QuartzGlassRouteActivation.Reason.SYNTHETIC_BLOCK_STATE_INVALID
                );
                BoundedDiagnostics.report(
                        BoundedDiagnostics.Event.QUARTZ_GLASS_REQUIRED_RESOURCES_MISMATCH
                );
                return;
            }
            quartzGlassActivation.activate();
            BoundedDiagnostics.report(
                    BoundedDiagnostics.Event.QUARTZ_GLASS_ROUTE_ACTIVATED
            );
        } catch (RuntimeException | LinkageError exception) {
            quartzGlassActivation.disable(
                    QuartzGlassRouteActivation.Reason.RESOURCE_LOAD_CALLBACK_FAILED
            );
            BoundedDiagnostics.report(
                    BoundedDiagnostics.Event.QUARTZ_GLASS_RESOURCE_CALLBACK_FAILED
            );
        }
    }

    private void prepareCraftingRouteSafely() {
        if (craftingActivation.isDisabled()) {
            return;
        }
        try {
            if (ProfileDisablement.current().isDisabled(
                    Ae219217CraftingProfile.PROFILE_ID
            )) {
                craftingActivation.disable(
                        CraftingRouteActivation.Reason.OPERATOR_DISABLED
                );
                BoundedDiagnostics.report(
                        BoundedDiagnostics.Event.CRAFTING_PROFILE_DISABLED
                );
                return;
            }
            if (!craftingProfileProbe.exact()) {
                craftingActivation.inactive(
                        CraftingRouteActivation.Reason.REQUIRED_RESOURCES_MISMATCH
                );
                BoundedDiagnostics.report(
                        BoundedDiagnostics.Event.CRAFTING_REQUIRED_RESOURCES_MISMATCH
                );
                return;
            }
            de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.BlockState
                    synthetic = resourcePack.getBlockStates().get(CRAFTING_SYNTHETIC);
            if (synthetic == null) {
                craftingActivation.inactive(
                        CraftingRouteActivation.Reason.SYNTHETIC_BLOCK_STATE_MISSING
                );
                BoundedDiagnostics.report(
                        BoundedDiagnostics.Event.CRAFTING_REQUIRED_RESOURCES_MISMATCH
                );
                return;
            }
            if (!isExpectedCraftingSyntheticBlockState(synthetic)) {
                craftingActivation.inactive(
                        CraftingRouteActivation.Reason.SYNTHETIC_BLOCK_STATE_INVALID
                );
                BoundedDiagnostics.report(
                        BoundedDiagnostics.Event.CRAFTING_REQUIRED_RESOURCES_MISMATCH
                );
                return;
            }
            if (!craftingRetentionProbe.retained()) {
                craftingActivation.disable(
                        CraftingRouteActivation.Reason.BLUENBT_RETENTION_PROBE_FAILED
                );
                BoundedDiagnostics.report(
                        BoundedDiagnostics.Event.CRAFTING_RETENTION_PROBE_FAILED
                );
                return;
            }
            craftingActivation.activate();
            BoundedDiagnostics.report(BoundedDiagnostics.Event.CRAFTING_ROUTE_ACTIVATED);
        } catch (RuntimeException | LinkageError exception) {
            craftingActivation.disable(
                    CraftingRouteActivation.Reason.RESOURCE_LOAD_CALLBACK_FAILED
            );
            BoundedDiagnostics.report(
                    BoundedDiagnostics.Event.CRAFTING_RESOURCE_CALLBACK_FAILED
            );
        }
    }

    private void prepareQuantumBridgeRouteSafely() {
        if (quantumBridgeActivation.isDisabled()) {
            return;
        }
        try {
            if (ProfileDisablement.current().isDisabled(
                    M3eQuantumBridgeResourceModels.PROFILE_ID
            )) {
                quantumBridgeActivation.disable(
                        QuantumBridgeRouteActivation.Reason.OPERATOR_DISABLED
                );
                BoundedDiagnostics.report(
                        BoundedDiagnostics.Event.QUANTUM_BRIDGE_PROFILE_DISABLED
                );
                return;
            }
            if (!quantumBridgeProfileProbe.exact()) {
                quantumBridgeActivation.inactive(
                        QuantumBridgeRouteActivation.Reason.REQUIRED_RESOURCES_MISMATCH
                );
                BoundedDiagnostics.report(
                        BoundedDiagnostics.Event.QUANTUM_BRIDGE_REQUIRED_RESOURCES_MISMATCH
                );
                return;
            }
            de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.BlockState
                    synthetic = resourcePack.getBlockStates().get(
                            QUANTUM_BRIDGE_SYNTHETIC
                    );
            if (synthetic == null) {
                quantumBridgeActivation.inactive(
                        QuantumBridgeRouteActivation.Reason
                                .SYNTHETIC_BLOCK_STATE_MISSING
                );
                BoundedDiagnostics.report(
                        BoundedDiagnostics.Event.QUANTUM_BRIDGE_REQUIRED_RESOURCES_MISMATCH
                );
                return;
            }
            if (!isExpectedQuantumBridgeSyntheticBlockState(synthetic)) {
                quantumBridgeActivation.inactive(
                        QuantumBridgeRouteActivation.Reason
                                .SYNTHETIC_BLOCK_STATE_INVALID
                );
                BoundedDiagnostics.report(
                        BoundedDiagnostics.Event.QUANTUM_BRIDGE_REQUIRED_RESOURCES_MISMATCH
                );
                return;
            }
            quantumBridgeActivation.activate();
            BoundedDiagnostics.report(
                    BoundedDiagnostics.Event.QUANTUM_BRIDGE_ROUTE_ACTIVATED
            );
        } catch (RuntimeException | LinkageError exception) {
            quantumBridgeActivation.disable(
                    QuantumBridgeRouteActivation.Reason.RESOURCE_LOAD_CALLBACK_FAILED
            );
            BoundedDiagnostics.report(
                    BoundedDiagnostics.Event.QUANTUM_BRIDGE_RESOURCE_CALLBACK_FAILED
            );
        }
    }

    private void prepareM3CompletionRouteSafely() {
        if (m3CompletionActivation.isDisabled()) {
            return;
        }
        try {
            if (ProfileDisablement.current().isDisabled(M3_COMPLETION_PROFILE_ID)) {
                m3CompletionActivation.disable(
                        M3CompletionRouteActivation.Reason.OPERATOR_DISABLED
                );
                BoundedDiagnostics.report(
                        BoundedDiagnostics.Event.M3_COMPLETION_PROFILE_DISABLED
                );
                return;
            }
            if (!m3CompletionProfileProbe.exact()) {
                m3CompletionActivation.inactive(
                        M3CompletionRouteActivation.Reason.REQUIRED_RESOURCES_MISMATCH
                );
                BoundedDiagnostics.report(
                        BoundedDiagnostics.Event.M3_COMPLETION_REQUIRED_RESOURCES_MISMATCH
                );
                return;
            }
            for (Key syntheticKey : M3_COMPLETION_SYNTHETIC_STATES) {
                de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.BlockState
                        synthetic = resourcePack.getBlockStates().get(syntheticKey);
                if (synthetic == null) {
                    m3CompletionActivation.inactive(
                            M3CompletionRouteActivation.Reason
                                    .SYNTHETIC_BLOCK_STATE_MISSING
                    );
                    BoundedDiagnostics.report(
                            BoundedDiagnostics.Event
                                    .M3_COMPLETION_REQUIRED_RESOURCES_MISMATCH
                    );
                    return;
                }
                if (!isExpectedM3CompletionSyntheticBlockState(synthetic)) {
                    m3CompletionActivation.inactive(
                            M3CompletionRouteActivation.Reason
                                    .SYNTHETIC_BLOCK_STATE_INVALID
                    );
                    BoundedDiagnostics.report(
                            BoundedDiagnostics.Event
                                    .M3_COMPLETION_REQUIRED_RESOURCES_MISMATCH
                    );
                    return;
                }
            }
            if (!m3CompletionRetentionProbe.retained()) {
                m3CompletionActivation.disable(
                        M3CompletionRouteActivation.Reason.RETENTION_PROBE_FAILED
                );
                BoundedDiagnostics.report(
                        BoundedDiagnostics.Event.M3_COMPLETION_RETENTION_PROBE_FAILED
                );
                return;
            }
            m3CompletionActivation.activate();
            BoundedDiagnostics.report(
                    BoundedDiagnostics.Event.M3_COMPLETION_ROUTE_ACTIVATED
            );
        } catch (RuntimeException | LinkageError exception) {
            m3CompletionActivation.disable(
                    M3CompletionRouteActivation.Reason.RESOURCE_LOAD_CALLBACK_FAILED
            );
            BoundedDiagnostics.report(
                    BoundedDiagnostics.Event.M3_COMPLETION_RESOURCE_CALLBACK_FAILED
            );
        }
    }

    private void prepareNativeStructuralRouteSafely() {
        if (nativeStructuralActivation.isDisabled()) {
            return;
        }
        try {
            if (ProfileDisablement.current().isDisabled(
                    Ae219217NativeStructuralProfile.PROFILE_ID
            )) {
                nativeStructuralActivation.disable(
                        NativeStructuralRouteActivation.Reason.OPERATOR_DISABLED
                );
                BoundedDiagnostics.report(
                        BoundedDiagnostics.Event.NATIVE_STRUCTURAL_PROFILE_DISABLED
                );
                return;
            }
            if (!nativeStructuralProfileProbe.exact()) {
                nativeStructuralActivation.inactive(
                        NativeStructuralRouteActivation.Reason
                                .REQUIRED_RESOURCES_MISMATCH
                );
                BoundedDiagnostics.report(
                        BoundedDiagnostics.Event
                                .NATIVE_STRUCTURAL_REQUIRED_RESOURCES_MISMATCH
                );
                return;
            }
            if (!nativeStructuralRetentionProbe.retained()) {
                nativeStructuralActivation.disable(
                        NativeStructuralRouteActivation.Reason.RETENTION_PROBE_FAILED
                );
                BoundedDiagnostics.report(
                        BoundedDiagnostics.Event
                                .NATIVE_STRUCTURAL_RETENTION_PROBE_FAILED
                );
                return;
            }
            nativeStructuralActivation.activate();
            BoundedDiagnostics.report(
                    BoundedDiagnostics.Event.NATIVE_STRUCTURAL_ROUTE_ACTIVATED
            );
        } catch (RuntimeException | LinkageError exception) {
            nativeStructuralActivation.disable(
                    NativeStructuralRouteActivation.Reason
                            .RESOURCE_LOAD_CALLBACK_FAILED
            );
            BoundedDiagnostics.report(
                    BoundedDiagnostics.Event.NATIVE_STRUCTURAL_RESOURCE_CALLBACK_FAILED
            );
        }
    }

    private static boolean exactCraftingProfileAvailable() {
        return Ae219217CraftingProfile.BLOCKS.size() == 8
                && Ae219217CraftingProfile.textures().size() == 15
                && Ae219217CraftingProfile.requiredResources().size() == 30
                && Ae219217CraftingProfile.requiredResourceSizes().size() == 30
                && Ae219217CraftingProfile.unsupportedCompatibleConnectorIds().size() == 29;
    }

    private void bakeDriveRouteSafely() {
        if (!activation.isActive() || !driveActivation.isActive()) {
            return;
        }
        try {
            if (!M3DriveResourceModels.resourcesSupported(resourcePack)) {
                driveActivation.inactive(
                        DriveRouteActivation.Reason.REQUIRED_RESOURCES_MISMATCH
                );
                BoundedDiagnostics.report(
                        BoundedDiagnostics.Event.DRIVE_REQUIRED_RESOURCES_MISMATCH
                );
            }
        } catch (RuntimeException | LinkageError exception) {
            driveActivation.disable(
                    DriveRouteActivation.Reason.RESOURCE_BAKE_CALLBACK_FAILED
            );
            BoundedDiagnostics.report(
                    BoundedDiagnostics.Event.DRIVE_RESOURCE_CALLBACK_FAILED
            );
        }
    }

    private void bakeExtendedDriveRouteSafely() {
        if (!activation.isActive() || !extendedDriveActivation.isActive()) {
            return;
        }
        try {
            if (!M3bExtendedAeDriveResourceModels.resourcesSupported(resourcePack)) {
                extendedDriveActivation.inactive(
                        ExtendedAeDriveRouteActivation.Reason.REQUIRED_RESOURCES_MISMATCH
                );
                BoundedDiagnostics.report(
                        BoundedDiagnostics.Event.EXTENDED_DRIVE_REQUIRED_RESOURCES_MISMATCH
                );
            }
        } catch (RuntimeException | LinkageError exception) {
            extendedDriveActivation.disable(
                    ExtendedAeDriveRouteActivation.Reason.RESOURCE_BAKE_CALLBACK_FAILED
            );
            BoundedDiagnostics.report(
                    BoundedDiagnostics.Event.EXTENDED_DRIVE_RESOURCE_CALLBACK_FAILED
            );
        }
    }

    private void bakeQuartzGlassRouteSafely() {
        if (!activation.isActive() || !quartzGlassActivation.isActive()) {
            return;
        }
        try {
            if (!M3cQuartzGlassResourceModels.resourcesSupported(resourcePack)) {
                quartzGlassActivation.inactive(
                        QuartzGlassRouteActivation.Reason.REQUIRED_RESOURCES_MISMATCH
                );
                BoundedDiagnostics.report(
                        BoundedDiagnostics.Event.QUARTZ_GLASS_REQUIRED_RESOURCES_MISMATCH
                );
            }
        } catch (RuntimeException | LinkageError exception) {
            quartzGlassActivation.disable(
                    QuartzGlassRouteActivation.Reason.RESOURCE_BAKE_CALLBACK_FAILED
            );
            BoundedDiagnostics.report(
                    BoundedDiagnostics.Event.QUARTZ_GLASS_RESOURCE_CALLBACK_FAILED
            );
        }
    }

    private void bakeCraftingRouteSafely() {
        if (!activation.isActive() || !craftingActivation.isActive()) {
            return;
        }
        try {
            if (!M3dCraftingResourceModels.resourcesSupported(resourcePack)) {
                craftingActivation.inactive(
                        CraftingRouteActivation.Reason.REQUIRED_RESOURCES_MISMATCH
                );
                BoundedDiagnostics.report(
                        BoundedDiagnostics.Event.CRAFTING_REQUIRED_RESOURCES_MISMATCH
                );
            }
        } catch (RuntimeException | LinkageError exception) {
            craftingActivation.disable(
                    CraftingRouteActivation.Reason.RESOURCE_BAKE_CALLBACK_FAILED
            );
            BoundedDiagnostics.report(
                    BoundedDiagnostics.Event.CRAFTING_RESOURCE_CALLBACK_FAILED
            );
        }
    }

    private void bakeQuantumBridgeRouteSafely() {
        if (!activation.isActive() || !quantumBridgeActivation.isActive()) {
            return;
        }
        try {
            if (!quantumBridgeResourceProbe.supported(resourcePack)) {
                quantumBridgeActivation.inactive(
                        QuantumBridgeRouteActivation.Reason.REQUIRED_RESOURCES_MISMATCH
                );
                BoundedDiagnostics.report(
                        BoundedDiagnostics.Event.QUANTUM_BRIDGE_REQUIRED_RESOURCES_MISMATCH
                );
            }
        } catch (RuntimeException | LinkageError exception) {
            quantumBridgeActivation.disable(
                    QuantumBridgeRouteActivation.Reason.RESOURCE_BAKE_CALLBACK_FAILED
            );
            BoundedDiagnostics.report(
                    BoundedDiagnostics.Event.QUANTUM_BRIDGE_RESOURCE_CALLBACK_FAILED
            );
        }
    }

    private void bakeM3CompletionRouteSafely() {
        if (!activation.isActive() || !m3CompletionActivation.isActive()) {
            return;
        }
        try {
            if (!m3CompletionResourceProbe.supported(resourcePack)) {
                m3CompletionActivation.inactive(
                        M3CompletionRouteActivation.Reason.REQUIRED_RESOURCES_MISMATCH
                );
                BoundedDiagnostics.report(
                        BoundedDiagnostics.Event.M3_COMPLETION_REQUIRED_RESOURCES_MISMATCH
                );
            }
        } catch (RuntimeException | LinkageError exception) {
            m3CompletionActivation.disable(
                    M3CompletionRouteActivation.Reason.RESOURCE_BAKE_CALLBACK_FAILED
            );
            BoundedDiagnostics.report(
                    BoundedDiagnostics.Event.M3_COMPLETION_RESOURCE_CALLBACK_FAILED
            );
        }
    }

    private void bakeNativeStructuralRouteSafely() {
        if (!activation.isActive() || !nativeStructuralActivation.isActive()) {
            return;
        }
        try {
            if (!nativeStructuralResourceProbe.supported(resourcePack)) {
                nativeStructuralActivation.inactive(
                        NativeStructuralRouteActivation.Reason
                                .REQUIRED_RESOURCES_MISMATCH
                );
                BoundedDiagnostics.report(
                        BoundedDiagnostics.Event
                                .NATIVE_STRUCTURAL_REQUIRED_RESOURCES_MISMATCH
                );
            }
        } catch (RuntimeException | LinkageError exception) {
            nativeStructuralActivation.disable(
                    NativeStructuralRouteActivation.Reason
                            .RESOURCE_BAKE_CALLBACK_FAILED
            );
            BoundedDiagnostics.report(
                    BoundedDiagnostics.Event.NATIVE_STRUCTURAL_RESOURCE_CALLBACK_FAILED
            );
        }
    }

    private static void reportDetectionFailure(String reason) {
        switch (reason) {
            case "ae2-artifact-not-found" -> BoundedDiagnostics.report(
                    BoundedDiagnostics.Event.ARTIFACT_NOT_FOUND
            );
            case "multiple-ae2-artifacts" -> BoundedDiagnostics.report(
                    BoundedDiagnostics.Event.MULTIPLE_ARTIFACTS
            );
            default -> BoundedDiagnostics.report(
                    BoundedDiagnostics.Event.UNSUPPORTED_ARTIFACT
            );
        }
    }

    private static void reportExtendedDetectionFailure(String reason) {
        switch (reason) {
            case "extendedae-artifact-not-found" -> BoundedDiagnostics.report(
                    BoundedDiagnostics.Event.EXTENDED_ARTIFACT_NOT_FOUND
            );
            case "multiple-extendedae-artifacts" -> BoundedDiagnostics.report(
                    BoundedDiagnostics.Event.MULTIPLE_EXTENDED_ARTIFACTS
            );
            default -> BoundedDiagnostics.report(
                    BoundedDiagnostics.Event.UNSUPPORTED_EXTENDED_ARTIFACT
            );
        }
    }

    private static ProfileActivation.Reason reasonForDetection(String reason) {
        return switch (reason) {
            case "ae2-artifact-not-found" -> ProfileActivation.Reason.ARTIFACT_NOT_FOUND;
            case "multiple-ae2-artifacts" -> ProfileActivation.Reason.MULTIPLE_ARTIFACTS;
            case "unsupported-ae2-artifact" ->
                    ProfileActivation.Reason.UNSUPPORTED_ARTIFACT;
            default -> ProfileActivation.Reason.ARTIFACT_IDENTITY_REJECTED;
        };
    }

    private static ExtendedAeDriveRouteActivation.Reason extendedReasonForDetection(
            String reason
    ) {
        return switch (reason) {
            case "extendedae-artifact-not-found" ->
                    ExtendedAeDriveRouteActivation.Reason.EXTENDEDAE_ARTIFACT_NOT_FOUND;
            case "multiple-extendedae-artifacts", "unsupported-extendedae-artifact" ->
                    ExtendedAeDriveRouteActivation.Reason.EXTENDEDAE_ARTIFACT_MISMATCH;
            default -> ExtendedAeDriveRouteActivation.Reason.EXTENDEDAE_ARTIFACT_MISMATCH;
        };
    }

    private void disableForResourceCallbackFailure(ProfileActivation.Reason reason) {
        activation.disable(reason);
        BoundedDiagnostics.report(BoundedDiagnostics.Event.RESOURCE_CALLBACK_FAILED);
    }

    private static List<Path> snapshotRoots(Iterable<Path> roots)
            throws InterruptedException {
        if (roots == null) {
            throw new NullPointerException("roots");
        }
        List<Path> snapshot = new ArrayList<>();
        for (Path root : roots) {
            if (Thread.interrupted()) {
                throw new InterruptedException("Interrupted while snapshotting resource roots");
            }
            if (snapshot.size() >= MAX_RESOURCE_ROOTS) {
                throw new IllegalArgumentException("Too many BlueMap resource roots");
            }
            snapshot.add(root);
        }
        return Collections.unmodifiableList(snapshot);
    }

    @FunctionalInterface
    interface ArtifactDetector {
        ExactArtifactDetector.Detection detect(Iterable<Path> roots)
                throws IOException, InterruptedException;
    }

    @FunctionalInterface
    interface ExtendedArtifactDetector {
        ExtendedAeArtifactDetector.Detection detect(Iterable<Path> roots)
                throws IOException, InterruptedException;
    }

    @FunctionalInterface
    interface CraftingProfileProbe {
        boolean exact();
    }

    @FunctionalInterface
    interface CraftingRetentionProbe {
        boolean retained();
    }

    @FunctionalInterface
    interface QuantumBridgeProfileProbe {
        boolean exact();
    }

    @FunctionalInterface
    interface QuantumBridgeResourceProbe {
        boolean supported(ResourcePack candidate);
    }

    @FunctionalInterface
    interface M3CompletionProfileProbe {
        boolean exact();
    }

    @FunctionalInterface
    interface M3CompletionRetentionProbe {
        boolean retained();
    }

    @FunctionalInterface
    interface M3CompletionResourceProbe {
        boolean supported(ResourcePack candidate);
    }

    @FunctionalInterface
    interface NativeStructuralProfileProbe {
        boolean exact();
    }

    @FunctionalInterface
    interface NativeStructuralRetentionProbe {
        boolean retained();
    }

    @FunctionalInterface
    interface NativeStructuralResourceProbe {
        boolean supported(ResourcePack candidate);
    }
}
