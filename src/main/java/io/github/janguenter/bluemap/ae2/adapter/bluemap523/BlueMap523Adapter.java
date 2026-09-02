/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.adapter.bluemap523;

import de.bluecolored.bluemap.core.map.hires.block.BlockRendererType;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.Variant;
import de.bluecolored.bluemap.core.util.Key;
import de.bluecolored.bluemap.core.util.Registry;
import de.bluecolored.bluemap.core.world.BlockEntity;
import de.bluecolored.bluemap.core.world.mca.MCAUtil;
import de.bluecolored.bluemap.core.world.mca.blockentity.BlockEntityType;
import de.bluecolored.bluenbt.NBTWriter;
import io.github.janguenter.bluemap.addon.adapter.api.bluemap523.BlueMapRuntimeCompatibility;
import io.github.janguenter.bluemap.addon.adapter.api.bluemap523.RegistryGuard;
import io.github.janguenter.bluemap.addon.adapter.api.bluemap523.ResourceExtensionType;
import io.github.janguenter.bluemap.ae2.activation.CraftingRouteActivation;
import io.github.janguenter.bluemap.ae2.activation.DriveRouteActivation;
import io.github.janguenter.bluemap.ae2.activation.ExtendedAeDriveRouteActivation;
import io.github.janguenter.bluemap.ae2.activation.M3CompletionRouteActivation;
import io.github.janguenter.bluemap.ae2.activation.NativeStructuralRouteActivation;
import io.github.janguenter.bluemap.ae2.activation.ProfileActivation;
import io.github.janguenter.bluemap.ae2.activation.QuantumBridgeRouteActivation;
import io.github.janguenter.bluemap.ae2.activation.QuartzGlassRouteActivation;
import io.github.janguenter.bluemap.ae2.api.Ae2ExtensionRegistry;
import io.github.janguenter.bluemap.ae2.diagnostics.BoundedDiagnostics;
import io.github.janguenter.bluemap.ae2.profile.Ae219217CraftingProfile;
import io.github.janguenter.bluemap.ae2.profile.Ae219217DriveProfile;
import io.github.janguenter.bluemap.ae2.profile.Ae219217Profile;
import io.github.janguenter.bluemap.ae2.profile.Ae219217QuartzGlassProfile;
import io.github.janguenter.bluemap.ae2.profile.extendedae.ExtendedAe2233Profile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/** All direct BlueMap 5.23 internal-ABI integration lives in this package. */
public final class BlueMap523Adapter {

    private static final Key CABLE_BUS_BLOCK_ENTITY = Key.parse(Ae219217Profile.CABLE_BUS_BLOCK);
    private static final Key DRIVE_BLOCK_ENTITY = Key.parse(Ae219217DriveProfile.DRIVE_BLOCK);
    private static final Key EXTENDED_DRIVE_BLOCK_ENTITY = Key.parse(
            ExtendedAe2233Profile.BLOCK
    );
    private static final Key RENDERER_KEY = Key.parse(
            "bluemap_ae2:fluix_glass_cable"
    );
    private static final Key DRIVE_RENDERER_KEY = Key.parse("bluemap_ae2:drive");
    private static final Key EXTENDED_DRIVE_RENDERER_KEY = Key.parse(
            ExtendedAe2233Profile.SYNTHETIC_BLOCK_STATE
    );
    private static final Key QUARTZ_GLASS_RENDERER_KEY = Key.parse(
            Ae219217QuartzGlassProfile.SYNTHETIC_BLOCK_STATE
    );
    private static final Key CRAFTING_MONITOR_BLOCK_ENTITY = Key.parse(
            Ae219217CraftingProfile.CRAFTING_MONITOR_BLOCK
    );
    private static final Key CRAFTING_RENDERER_KEY = Key.parse(
            Ae219217CraftingProfile.SYNTHETIC_BLOCK_STATE
    );
    private static final Key QUANTUM_BRIDGE_RENDERER_KEY = Key.parse(
            M3eQuantumBridgeResourceModels.SYNTHETIC_BLOCK_STATE
    );
    private static final Key M3_COMPLETION_PAINT_BLOCK_ENTITY = Key.parse("ae2:paint");
    private static final Key M3_COMPLETION_RENDERER_KEY = Key.parse(
            "bluemap_ae2:m3_completion"
    );
    private static final Key RESOURCE_EXTENSION_KEY = Key.parse(
            "bluemap_ae2:ae2_19_2_17"
    );
    private static final ProfileActivation ACTIVATION = new ProfileActivation();
    private static final DriveRouteActivation DRIVE_ACTIVATION = new DriveRouteActivation();
    private static final ExtendedAeDriveRouteActivation EXTENDED_DRIVE_ACTIVATION =
            new ExtendedAeDriveRouteActivation();
    private static final QuartzGlassRouteActivation QUARTZ_GLASS_ACTIVATION =
            new QuartzGlassRouteActivation();
    private static final CraftingRouteActivation CRAFTING_ACTIVATION =
            new CraftingRouteActivation();
    private static final QuantumBridgeRouteActivation QUANTUM_BRIDGE_ACTIVATION =
            new QuantumBridgeRouteActivation();
    private static final M3CompletionRouteActivation M3_COMPLETION_ACTIVATION =
            new M3CompletionRouteActivation();
    private static final NativeStructuralRouteActivation NATIVE_STRUCTURAL_ACTIVATION =
            new NativeStructuralRouteActivation();
    private static final BlockEntityType BLOCK_ENTITY_TYPE = new BlockEntityType.Impl(
            CABLE_BUS_BLOCK_ENTITY,
            Ae2CableBusBlockEntityData.class
    );
    private static final BlockEntityType DRIVE_BLOCK_ENTITY_TYPE = new BlockEntityType.Impl(
            DRIVE_BLOCK_ENTITY,
            Ae2DriveBlockEntityData.class
    );
    private static final BlockEntityType EXTENDED_DRIVE_BLOCK_ENTITY_TYPE =
            new BlockEntityType.Impl(
                    EXTENDED_DRIVE_BLOCK_ENTITY,
                    ExtendedAeDriveBlockEntityData.class
            );
    private static final BlockEntityType CRAFTING_MONITOR_BLOCK_ENTITY_TYPE =
            new BlockEntityType.Impl(
                    CRAFTING_MONITOR_BLOCK_ENTITY,
                    Ae2CraftingMonitorBlockEntityData.class
            );
    private static final BlockEntityType M3_COMPLETION_PAINT_BLOCK_ENTITY_TYPE =
            new BlockEntityType.Impl(
                    M3_COMPLETION_PAINT_BLOCK_ENTITY,
                    Ae2PaintBlockEntityData.class
            );
    private static final BlockRendererType RENDERER_TYPE = new BlockRendererType.Impl(
            RENDERER_KEY,
            (resourcePack, textureGallery, renderSettings) -> new CableBusRenderer(
                    resourcePack,
                    textureGallery,
                    renderSettings,
                    ACTIVATION,
                    NATIVE_STRUCTURAL_ACTIVATION
            )
    );
    private static final BlockRendererType DRIVE_RENDERER_TYPE = new BlockRendererType.Impl(
            DRIVE_RENDERER_KEY,
            (resourcePack, textureGallery, renderSettings) -> new DriveRenderer(
                    resourcePack,
                    textureGallery,
                    renderSettings,
                    ACTIVATION,
                    DRIVE_ACTIVATION
            )
    );
    private static final BlockRendererType EXTENDED_DRIVE_RENDERER_TYPE =
            new BlockRendererType.Impl(
                    EXTENDED_DRIVE_RENDERER_KEY,
                    (resourcePack, textureGallery, renderSettings) ->
                            new ExtendedAeDriveRenderer(
                                    resourcePack,
                                    textureGallery,
                                    renderSettings,
                                    ACTIVATION,
                                    EXTENDED_DRIVE_ACTIVATION
                            )
            );
    private static final BlockRendererType QUARTZ_GLASS_RENDERER_TYPE =
            new BlockRendererType.Impl(
                    QUARTZ_GLASS_RENDERER_KEY,
                    (resourcePack, textureGallery, renderSettings) ->
                            new QuartzGlassRenderer(
                                    resourcePack,
                                    textureGallery,
                                    renderSettings,
                                    ACTIVATION,
                                    QUARTZ_GLASS_ACTIVATION
                            )
            );
    private static final BlockRendererType CRAFTING_RENDERER_TYPE =
            new BlockRendererType.Impl(
                    CRAFTING_RENDERER_KEY,
                    (resourcePack, textureGallery, renderSettings) ->
                            new CraftingRenderer(
                                    resourcePack,
                                    textureGallery,
                                    renderSettings,
                                    ACTIVATION,
                                    CRAFTING_ACTIVATION
                            )
            );
    private static final BlockRendererType QUANTUM_BRIDGE_RENDERER_TYPE =
            new BlockRendererType.Impl(
                    QUANTUM_BRIDGE_RENDERER_KEY,
                    (resourcePack, textureGallery, renderSettings) ->
                            new QuantumBridgeRenderer(
                                    resourcePack,
                                    textureGallery,
                                    renderSettings,
                                    ACTIVATION,
                                    QUANTUM_BRIDGE_ACTIVATION
                            )
            );
    private static final BlockRendererType M3_COMPLETION_RENDERER_TYPE =
            new BlockRendererType.Impl(
                    M3_COMPLETION_RENDERER_KEY,
                    (resourcePack, textureGallery, renderSettings) ->
                            new M3CompletionRenderer(
                                    resourcePack,
                                    textureGallery,
                                    renderSettings,
                                    ACTIVATION,
                                    M3_COMPLETION_ACTIVATION
                            )
            );
    private static final ResourcePack.Extension<Ae2ResourceExtension> RESOURCE_EXTENSION_TYPE =
            new ResourceExtensionType<>(
                    RESOURCE_EXTENSION_KEY,
                    BlueMap523Adapter::createResourceExtension
            );

    private static boolean hooksInstalled;

    private BlueMap523Adapter() {
    }

    /**
     * Registers the exact adapter hooks. The DTO is inserted before any code
     * is allowed to make first use of BlueNBT's resolver.
     *
     * @return {@code true} when every required M0-M2 core registry key is owned;
     *         optional Drive routes report and disable themselves independently
     */
    public static synchronized boolean install() {
        if (!BlueMapRuntimeCompatibility.matchesCurrent()) {
            ACTIVATION.disable(ProfileActivation.Reason.UNSUPPORTED_BLUEMAP_RUNTIME);
            return false;
        }
        if (hooksInstalled) {
            return ownsRequiredCoreRegistryKeys();
        }

        if (!RegistryGuard.canRegister(BlockEntityType.REGISTRY, BLOCK_ENTITY_TYPE)
                || !RegistryGuard.canRegister(BlockRendererType.REGISTRY, RENDERER_TYPE)
                || !RegistryGuard.canRegister(ResourcePack.Extension.REGISTRY, RESOURCE_EXTENSION_TYPE)) {
            return disableForCollision();
        }

        boolean driveDtoEligible = RegistryGuard.canRegister(
                BlockEntityType.REGISTRY,
                DRIVE_BLOCK_ENTITY_TYPE
        );
        boolean driveRendererEligible = RegistryGuard.canRegister(
                BlockRendererType.REGISTRY,
                DRIVE_RENDERER_TYPE
        );
        if (!driveDtoEligible || !driveRendererEligible) {
            disableDriveForCollision();
        }
        boolean extendedDriveDtoEligible = RegistryGuard.canRegister(
                BlockEntityType.REGISTRY,
                EXTENDED_DRIVE_BLOCK_ENTITY_TYPE
        );
        boolean extendedDriveRendererEligible = RegistryGuard.canRegister(
                BlockRendererType.REGISTRY,
                EXTENDED_DRIVE_RENDERER_TYPE
        );
        if (!extendedDriveDtoEligible || !extendedDriveRendererEligible) {
            disableExtendedDriveForCollision();
        }
        boolean quartzGlassRendererEligible = RegistryGuard.canRegister(
                BlockRendererType.REGISTRY,
                QUARTZ_GLASS_RENDERER_TYPE
        );
        if (!quartzGlassRendererEligible) {
            disableQuartzGlassForCollision();
        }
        boolean craftingMonitorDtoEligible = RegistryGuard.canRegister(
                BlockEntityType.REGISTRY,
                CRAFTING_MONITOR_BLOCK_ENTITY_TYPE
        );
        boolean craftingRendererEligible = RegistryGuard.canRegister(
                BlockRendererType.REGISTRY,
                CRAFTING_RENDERER_TYPE
        );
        if (!craftingMonitorDtoEligible || !craftingRendererEligible) {
            disableCraftingForCollision();
        }
        boolean quantumBridgeRendererEligible = RegistryGuard.canRegister(
                BlockRendererType.REGISTRY,
                QUANTUM_BRIDGE_RENDERER_TYPE
        );
        if (!quantumBridgeRendererEligible) {
            disableQuantumBridgeForCollision();
        }
        boolean m3CompletionPaintDtoEligible = RegistryGuard.canRegister(
                BlockEntityType.REGISTRY,
                M3_COMPLETION_PAINT_BLOCK_ENTITY_TYPE
        );
        boolean m3CompletionRendererEligible = RegistryGuard.canRegister(
                BlockRendererType.REGISTRY,
                M3_COMPLETION_RENDERER_TYPE
        );
        if (!m3CompletionPaintDtoEligible || !m3CompletionRendererEligible) {
            disableM3CompletionForCollision();
        }

        // BlueNBT snapshots registered DTO classes on first deserialization.
        // Keep this registration first and perform no deserialization here.
        if (!RegistryGuard.register(BlockEntityType.REGISTRY, BLOCK_ENTITY_TYPE)) {
            return disableForCollision();
        }
        boolean driveDtoOwned = driveDtoEligible
                && RegistryGuard.register(BlockEntityType.REGISTRY, DRIVE_BLOCK_ENTITY_TYPE);
        if (driveDtoEligible && !driveDtoOwned) {
            disableDriveForCollision();
        }
        boolean extendedDriveDtoOwned = extendedDriveDtoEligible
                && RegistryGuard.register(
                        BlockEntityType.REGISTRY,
                        EXTENDED_DRIVE_BLOCK_ENTITY_TYPE
                );
        if (extendedDriveDtoEligible && !extendedDriveDtoOwned) {
            disableExtendedDriveForCollision();
        }
        boolean craftingMonitorDtoOwned = craftingMonitorDtoEligible
                && RegistryGuard.register(
                        BlockEntityType.REGISTRY,
                        CRAFTING_MONITOR_BLOCK_ENTITY_TYPE
                );
        if (craftingMonitorDtoEligible && !craftingMonitorDtoOwned) {
            disableCraftingForCollision();
        }
        boolean m3CompletionPaintDtoOwned = m3CompletionPaintDtoEligible
                && RegistryGuard.register(
                        BlockEntityType.REGISTRY,
                        M3_COMPLETION_PAINT_BLOCK_ENTITY_TYPE
                );
        if (m3CompletionPaintDtoEligible && !m3CompletionPaintDtoOwned) {
            disableM3CompletionForCollision();
        }
        if (!RegistryGuard.register(BlockRendererType.REGISTRY, RENDERER_TYPE)) {
            return disableForCollision();
        }
        boolean driveRendererOwned = driveRendererEligible
                && RegistryGuard.register(BlockRendererType.REGISTRY, DRIVE_RENDERER_TYPE);
        if (driveRendererEligible && !driveRendererOwned) {
            disableDriveForCollision();
        }
        boolean extendedDriveRendererOwned = extendedDriveRendererEligible
                && RegistryGuard.register(
                        BlockRendererType.REGISTRY,
                        EXTENDED_DRIVE_RENDERER_TYPE
                );
        if (extendedDriveRendererEligible && !extendedDriveRendererOwned) {
            disableExtendedDriveForCollision();
        }
        boolean quartzGlassRendererOwned = quartzGlassRendererEligible
                && RegistryGuard.register(
                        BlockRendererType.REGISTRY,
                        QUARTZ_GLASS_RENDERER_TYPE
                );
        if (quartzGlassRendererEligible && !quartzGlassRendererOwned) {
            disableQuartzGlassForCollision();
        }
        boolean craftingRendererOwned = craftingRendererEligible
                && RegistryGuard.register(BlockRendererType.REGISTRY, CRAFTING_RENDERER_TYPE);
        if (craftingRendererEligible && !craftingRendererOwned) {
            disableCraftingForCollision();
        }
        boolean quantumBridgeRendererOwned = quantumBridgeRendererEligible
                && registerQuantumBridgeRendererExact(
                        BlockRendererType.REGISTRY,
                        QUANTUM_BRIDGE_ACTIVATION
                );
        boolean m3CompletionRendererOwned = m3CompletionRendererEligible
                && registerM3CompletionRendererExact(
                        BlockRendererType.REGISTRY,
                        M3_COMPLETION_ACTIVATION
                );
        if (!RegistryGuard.register(ResourcePack.Extension.REGISTRY, RESOURCE_EXTENSION_TYPE)) {
            return disableForCollision();
        }

        // M4/M5 routes are optional and independently fail closed. Their
        // registry collision must never disable the exact AE2 core route.
        M45Adapter.install();

        hooksInstalled = true;
        ACTIVATION.inactive(ProfileActivation.Reason.AWAITING_EXACT_PROFILE);
        if (driveDtoOwned && driveRendererOwned && !DRIVE_ACTIVATION.isDisabled()) {
            DRIVE_ACTIVATION.inactive(DriveRouteActivation.Reason.AWAITING_EXACT_PROFILE);
        }
        if (extendedDriveDtoOwned
                && extendedDriveRendererOwned
                && !EXTENDED_DRIVE_ACTIVATION.isDisabled()) {
            EXTENDED_DRIVE_ACTIVATION.inactive(
                    ExtendedAeDriveRouteActivation.Reason.AWAITING_EXACT_PROFILE
            );
        }
        if (quartzGlassRendererOwned && !QUARTZ_GLASS_ACTIVATION.isDisabled()) {
            QUARTZ_GLASS_ACTIVATION.inactive(
                    QuartzGlassRouteActivation.Reason.AWAITING_EXACT_PROFILE
            );
        }
        if (craftingMonitorDtoOwned
                && craftingRendererOwned
                && !CRAFTING_ACTIVATION.isDisabled()) {
            CRAFTING_ACTIVATION.inactive(
                    CraftingRouteActivation.Reason.AWAITING_EXACT_PROFILE
            );
        }
        if (quantumBridgeRendererOwned && !QUANTUM_BRIDGE_ACTIVATION.isDisabled()) {
            QUANTUM_BRIDGE_ACTIVATION.inactive(
                    QuantumBridgeRouteActivation.Reason.AWAITING_EXACT_PROFILE
            );
        }
        if (m3CompletionPaintDtoOwned
                && m3CompletionRendererOwned
                && !M3_COMPLETION_ACTIVATION.isDisabled()) {
            M3_COMPLETION_ACTIVATION.inactive(
                    M3CompletionRouteActivation.Reason.AWAITING_EXACT_PROFILE
            );
        }
        return true;
    }

    static ProfileActivation activationForTesting() {
        return ACTIVATION;
    }

    static DriveRouteActivation driveActivationForTesting() {
        return DRIVE_ACTIVATION;
    }

    static ExtendedAeDriveRouteActivation extendedDriveActivationForTesting() {
        return EXTENDED_DRIVE_ACTIVATION;
    }

    static QuartzGlassRouteActivation quartzGlassActivationForTesting() {
        return QUARTZ_GLASS_ACTIVATION;
    }

    static CraftingRouteActivation craftingActivationForTesting() {
        return CRAFTING_ACTIVATION;
    }

    static QuantumBridgeRouteActivation quantumBridgeActivationForTesting() {
        return QUANTUM_BRIDGE_ACTIVATION;
    }

    static M3CompletionRouteActivation m3CompletionActivationForTesting() {
        return M3_COMPLETION_ACTIVATION;
    }

    static M45Runtime m45RuntimeForTesting() {
        return M45Adapter.runtime();
    }

    static boolean coreProfileActiveForM45() {
        return ACTIVATION.isActive();
    }

    static boolean nativeStructuralActiveForM45() {
        return NATIVE_STRUCTURAL_ACTIVATION.isActive();
    }

    static boolean nativeDriveActiveForM45() {
        return DRIVE_ACTIVATION.isActive();
    }

    static boolean nativeDrivePendingForM45() {
        DriveRouteActivation.Reason reason = DRIVE_ACTIVATION.snapshot().reason();
        return reason == DriveRouteActivation.Reason.NOT_INSTALLED
                || reason == DriveRouteActivation.Reason.AWAITING_EXACT_PROFILE;
    }

    static boolean coreCraftingActiveForM45() {
        return CRAFTING_ACTIVATION.isActive();
    }

    static boolean isExpectedSyntheticVariant(Variant variant) {
        return variant != null
                && variant.getRenderer() == RENDERER_TYPE
                && ResourcePack.MISSING_BLOCK_MODEL.equals(variant.getModel())
                && !variant.isTransformed()
                && !variant.isUvlock()
                && Double.compare(variant.getWeight(), 1D) == 0;
    }

    static boolean isExpectedDriveSyntheticVariant(Variant variant) {
        return variant != null
                && variant.getRenderer() == DRIVE_RENDERER_TYPE
                && ResourcePack.MISSING_BLOCK_MODEL.equals(variant.getModel())
                && !variant.isTransformed()
                && !variant.isUvlock()
                && Double.compare(variant.getWeight(), 1D) == 0;
    }

    static boolean isExpectedExtendedDriveSyntheticVariant(Variant variant) {
        return variant != null
                && variant.getRenderer() == EXTENDED_DRIVE_RENDERER_TYPE
                && ResourcePack.MISSING_BLOCK_MODEL.equals(variant.getModel())
                && !variant.isTransformed()
                && !variant.isUvlock()
                && Double.compare(variant.getWeight(), 1D) == 0;
    }

    static boolean isExpectedQuartzGlassSyntheticVariant(Variant variant) {
        return variant != null
                && variant.getRenderer() == QUARTZ_GLASS_RENDERER_TYPE
                && ResourcePack.MISSING_BLOCK_MODEL.equals(variant.getModel())
                && !variant.isTransformed()
                && !variant.isUvlock()
                && Double.compare(variant.getWeight(), 1D) == 0;
    }

    static boolean isExpectedCraftingSyntheticVariant(Variant variant) {
        return variant != null
                && variant.getRenderer() == CRAFTING_RENDERER_TYPE
                && ResourcePack.MISSING_BLOCK_MODEL.equals(variant.getModel())
                && !variant.isTransformed()
                && !variant.isUvlock()
                && Double.compare(variant.getWeight(), 1D) == 0;
    }

    static boolean isExpectedQuantumBridgeSyntheticVariant(Variant variant) {
        return variant != null
                && variant.getRenderer() == QUANTUM_BRIDGE_RENDERER_TYPE
                && ResourcePack.MISSING_BLOCK_MODEL.equals(variant.getModel())
                && !variant.isTransformed()
                && !variant.isUvlock()
                && Double.compare(variant.getWeight(), 1D) == 0;
    }

    static boolean isExpectedM3CompletionSyntheticVariant(Variant variant) {
        return variant != null
                && variant.getRenderer() == M3_COMPLETION_RENDERER_TYPE
                && ResourcePack.MISSING_BLOCK_MODEL.equals(variant.getModel())
                && !variant.isTransformed()
                && !variant.isUvlock()
                && Double.compare(variant.getWeight(), 1D) == 0;
    }

    static boolean probeBlockEntityRetention() {
        try {
            BlockEntity parsed = MCAUtil.BLUENBT.read(
                    new ByteArrayInputStream(createProbeNbt()),
                    BlockEntity.class
            );
            return parsed instanceof Ae2CableBusBlockEntityData data
                    && CABLE_BUS_BLOCK_ENTITY.equals(data.getId())
                    && data.getX() == 17
                    && data.getY() == -23
                    && data.getZ() == 41
                    && data.hasAttachmentsOrFacades()
                    && data.retainsProbeFields();
        } catch (IOException | RuntimeException exception) {
            return false;
        }
    }

    static boolean probeNativeStructuralBlockEntityRetention() {
        try {
            BlockEntity parsed = MCAUtil.BLUENBT.read(
                    new ByteArrayInputStream(createNativeStructuralProbeNbt()),
                    BlockEntity.class
            );
            return parsed instanceof Ae2CableBusBlockEntityData data
                    && CABLE_BUS_BLOCK_ENTITY.equals(data.getId())
                    && data.getX() == 17
                    && data.getY() == -23
                    && data.getZ() == 41
                    && data.retainsNativeStructuralProbeFields();
        } catch (IOException | RuntimeException | LinkageError exception) {
            return false;
        }
    }

    static boolean probeDriveBlockEntityRetention() {
        try {
            BlockEntity parsed = MCAUtil.BLUENBT.read(
                    new ByteArrayInputStream(createDriveProbeNbt()),
                    BlockEntity.class
            );
            return parsed instanceof Ae2DriveBlockEntityData data
                    && DRIVE_BLOCK_ENTITY.equals(data.getId())
                    && data.getX() == -31
                    && data.getY() == 73
                    && data.getZ() == 19
                    && data.retainsProbeFields();
        } catch (IOException | RuntimeException exception) {
            return false;
        }
    }

    static boolean probeExtendedDriveBlockEntityRetention() {
        try {
            BlockEntity parsed = MCAUtil.BLUENBT.read(
                    new ByteArrayInputStream(createExtendedDriveProbeNbt()),
                    BlockEntity.class
            );
            return parsed instanceof ExtendedAeDriveBlockEntityData data
                    && EXTENDED_DRIVE_BLOCK_ENTITY.equals(data.getId())
                    && data.getX() == 61
                    && data.getY() == -11
                    && data.getZ() == 29
                    && data.retainsProbeFields();
        } catch (IOException | RuntimeException exception) {
            return false;
        }
    }

    static boolean probeCraftingMonitorBlockEntityRetention() {
        try {
            BlockEntity parsed = MCAUtil.BLUENBT.read(
                    new ByteArrayInputStream(createCraftingMonitorProbeNbt()),
                    BlockEntity.class
            );
            return parsed instanceof Ae2CraftingMonitorBlockEntityData data
                    && CRAFTING_MONITOR_BLOCK_ENTITY.equals(data.getId())
                    && data.getX() == 43
                    && data.getY() == 101
                    && data.getZ() == -37
                    && data.retainsProbeFields();
        } catch (IOException | RuntimeException exception) {
            return false;
        }
    }

    static boolean probeM3CompletionPaintRetention() {
        try {
            BlockEntity parsed = MCAUtil.BLUENBT.read(
                    new ByteArrayInputStream(createM3CompletionPaintProbeNbt()),
                    BlockEntity.class
            );
            return parsed instanceof Ae2PaintBlockEntityData data
                    && M3_COMPLETION_PAINT_BLOCK_ENTITY.equals(data.getId())
                    && data.getX() == -47
                    && data.getY() == 83
                    && data.getZ() == 59
                    && data.retainsProbeFields();
        } catch (IOException | RuntimeException | LinkageError exception) {
            return false;
        }
    }

    static boolean registerQuantumBridgeRendererExact(
            Registry<BlockRendererType> registry,
            QuantumBridgeRouteActivation activation
    ) {
        if (RegistryGuard.register(registry, QUANTUM_BRIDGE_RENDERER_TYPE)) {
            return true;
        }
        disableQuantumBridgeForCollision(activation);
        return false;
    }

    static boolean registerM3CompletionRendererExact(
            Registry<BlockRendererType> registry,
            M3CompletionRouteActivation activation
    ) {
        if (RegistryGuard.register(registry, M3_COMPLETION_RENDERER_TYPE)) {
            return true;
        }
        disableM3CompletionForCollision(activation);
        return false;
    }

    private static Ae2ResourceExtension createResourceExtension(ResourcePack pack) {
        Ae2ExtensionRegistry.Host.freezeForResourceRendering(
                Ae2ExtensionRegistry.Host.acquireAccess()
        );
        return new Ae2ResourceExtension(
                pack,
                ACTIVATION,
                DRIVE_ACTIVATION,
                EXTENDED_DRIVE_ACTIVATION,
                QUARTZ_GLASS_ACTIVATION,
                CRAFTING_ACTIVATION,
                QUANTUM_BRIDGE_ACTIVATION,
                M3_COMPLETION_ACTIVATION,
                NATIVE_STRUCTURAL_ACTIVATION
        );
    }

    private static boolean ownsRequiredCoreRegistryKeys() {
        return BlockEntityType.REGISTRY.get(BLOCK_ENTITY_TYPE.getKey()) == BLOCK_ENTITY_TYPE
                && BlockRendererType.REGISTRY.get(RENDERER_TYPE.getKey()) == RENDERER_TYPE
                && ResourcePack.Extension.REGISTRY.get(RESOURCE_EXTENSION_TYPE.getKey())
                        == RESOURCE_EXTENSION_TYPE;
    }

    private static boolean disableForCollision() {
        ACTIVATION.disable(ProfileActivation.Reason.REGISTRY_COLLISION);
        BoundedDiagnostics.report(BoundedDiagnostics.Event.REGISTRY_COLLISION);
        return false;
    }

    private static void disableDriveForCollision() {
        DRIVE_ACTIVATION.disable(DriveRouteActivation.Reason.REGISTRY_COLLISION);
        BoundedDiagnostics.report(BoundedDiagnostics.Event.DRIVE_REGISTRY_COLLISION);
    }

    private static void disableExtendedDriveForCollision() {
        EXTENDED_DRIVE_ACTIVATION.disable(
                ExtendedAeDriveRouteActivation.Reason.REGISTRY_COLLISION
        );
        BoundedDiagnostics.report(
                BoundedDiagnostics.Event.EXTENDED_DRIVE_REGISTRY_COLLISION
        );
    }

    private static void disableQuartzGlassForCollision() {
        QUARTZ_GLASS_ACTIVATION.disable(
                QuartzGlassRouteActivation.Reason.REGISTRY_COLLISION
        );
        BoundedDiagnostics.report(
                BoundedDiagnostics.Event.QUARTZ_GLASS_REGISTRY_COLLISION
        );
    }

    private static void disableCraftingForCollision() {
        CRAFTING_ACTIVATION.disable(
                CraftingRouteActivation.Reason.REGISTRY_COLLISION
        );
        BoundedDiagnostics.report(BoundedDiagnostics.Event.CRAFTING_REGISTRY_COLLISION);
    }

    private static void disableQuantumBridgeForCollision() {
        disableQuantumBridgeForCollision(QUANTUM_BRIDGE_ACTIVATION);
    }

    private static void disableQuantumBridgeForCollision(
            QuantumBridgeRouteActivation activation
    ) {
        activation.disable(
                QuantumBridgeRouteActivation.Reason.REGISTRY_COLLISION
        );
        BoundedDiagnostics.report(
                BoundedDiagnostics.Event.QUANTUM_BRIDGE_REGISTRY_COLLISION
        );
    }

    private static void disableM3CompletionForCollision() {
        disableM3CompletionForCollision(M3_COMPLETION_ACTIVATION);
    }

    private static void disableM3CompletionForCollision(
            M3CompletionRouteActivation activation
    ) {
        activation.disable(M3CompletionRouteActivation.Reason.REGISTRY_COLLISION);
        BoundedDiagnostics.report(
                BoundedDiagnostics.Event.M3_COMPLETION_REGISTRY_COLLISION
        );
    }

    private static byte[] createProbeNbt() throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (NBTWriter writer = new NBTWriter(bytes)) {
            writer.beginCompound();
            writer.name("id").value(Ae219217Profile.CABLE_BUS_BLOCK);
            writer.name("x").value(17);
            writer.name("y").value(-23);
            writer.name("z").value(41);
            writer.name("hasRedstone").value(2);
            writeIdCompound(writer, "cable", "ae2:fluix_glass_cable");
            writePartCompound(writer, "down", "ae2:terminal", 0);
            writePartCompound(writer, "up", "ae2:terminal", 1);
            writePartCompound(writer, "north", "ae2:terminal", 2);
            writePartCompound(writer, "south", "ae2:terminal", 3);
            writePartCompound(writer, "west", "ae2:terminal", 0);
            writePartCompound(writer, "east", "ae2:terminal", 1);
            writeNameCompound(writer, "facadeDown", "minecraft:stone");
            writeNameCompound(writer, "facadeUp", "minecraft:stone");
            writeNameCompound(writer, "facadeNorth", "minecraft:stone");
            writeNameCompound(writer, "facadeSouth", "minecraft:stone");
            writeNameCompound(writer, "facadeWest", "minecraft:stone");
            writeNameCompound(writer, "facadeEast", "minecraft:stone");
            writer.endCompound();
        }
        return bytes.toByteArray();
    }

    private static byte[] createDriveProbeNbt() throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (NBTWriter writer = new NBTWriter(bytes)) {
            writer.beginCompound();
            writer.name("id").value(Ae219217DriveProfile.DRIVE_BLOCK);
            writer.name("x").value(-31);
            writer.name("y").value(73);
            writer.name("z").value(19);
            writer.name("inv").beginCompound();
            for (int slot = 0; slot < 10; slot++) {
                writer.name("item" + slot).beginCompound();
                if (slot == 0) {
                    writer.name("id").value("ae2:item_storage_cell_1k");
                    writer.name("count").value(1);
                    writer.name("components").beginCompound();
                    writer.name("minecraft:custom_name").value("retention-probe");
                    writer.endCompound();
                } else if (slot == 9) {
                    writer.name("id").value("ae2:matter_cannon");
                }
                writer.endCompound();
            }
            writer.endCompound();
            writer.endCompound();
        }
        return bytes.toByteArray();
    }

    private static byte[] createNativeStructuralProbeNbt() throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (NBTWriter writer = new NBTWriter(bytes)) {
            writer.beginCompound();
            writer.name("id").value(Ae219217Profile.CABLE_BUS_BLOCK);
            writer.name("x").value(17);
            writer.name("y").value(-23);
            writer.name("z").value(41);
            writer.name("hasRedstone").value(2);
            writeIdCompound(writer, "cable", "ae2:fluix_glass_cable");
            writePartCompound(writer, "down", "ae2:terminal", 0);
            writer.name("up").beginCompound();
            writer.name("id").value("ae2:me_p2p_tunnel");
            writer.name("freq").value((short) -1);
            writer.endCompound();
            writer.name("facadeNorth").beginCompound();
            writer.name("Name").value("minecraft:oak_log");
            writer.name("Properties").beginCompound();
            writer.name("axis").value("x");
            writer.endCompound();
            writer.endCompound();
            writer.endCompound();
        }
        return bytes.toByteArray();
    }

    private static byte[] createExtendedDriveProbeNbt() throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (NBTWriter writer = new NBTWriter(bytes)) {
            writer.beginCompound();
            writer.name("id").value(ExtendedAe2233Profile.BLOCK);
            writer.name("x").value(61);
            writer.name("y").value(-11);
            writer.name("z").value(29);
            writer.name("inv").beginCompound();
            for (int slot = 0; slot < ExtendedAe2233Profile.SLOT_COUNT; slot++) {
                writer.name("item" + slot).beginCompound();
                if (slot == 0) {
                    writer.name("id").value("ae2:item_storage_cell_1k");
                    writer.name("count").value(1);
                    writer.name("components").beginCompound();
                    writer.name("minecraft:custom_name").value("retention-probe");
                    writer.endCompound();
                } else if (slot == 9) {
                    writer.name("id").value("ae2:matter_cannon");
                } else if (slot == 10) {
                    writer.name("id").value("extendedae:infinity_water_cell");
                } else if (slot == 19) {
                    writer.name("id").value("extendedae:void_cell");
                }
                writer.endCompound();
            }
            writer.endCompound();
            writer.endCompound();
        }
        return bytes.toByteArray();
    }

    private static byte[] createCraftingMonitorProbeNbt() throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (NBTWriter writer = new NBTWriter(bytes)) {
            writer.beginCompound();
            writer.name("id").value(Ae219217CraftingProfile.CRAFTING_MONITOR_BLOCK);
            writer.name("x").value(43);
            writer.name("y").value(101);
            writer.name("z").value(-37);
            writer.name("paintedColor").value((byte) 11);
            writer.endCompound();
        }
        return bytes.toByteArray();
    }

    private static byte[] createM3CompletionPaintProbeNbt() throws IOException {
        byte[] dots = new byte[256];
        dots[0] = 2;
        dots[1] = 0x21;
        dots[2] = 0x10;
        dots[3] = 0x43;
        dots[4] = (byte) 0x8d;

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (NBTWriter writer = new NBTWriter(bytes)) {
            writer.beginCompound();
            writer.name("id").value("ae2:paint");
            writer.name("x").value(-47);
            writer.name("y").value(83);
            writer.name("z").value(59);
            writer.name("dots").value(dots);
            writer.endCompound();
        }
        return bytes.toByteArray();
    }

    private static void writeIdCompound(NBTWriter writer, String field, String id)
            throws IOException {
        writer.name(field).beginCompound();
        writer.name("id").value(id);
        writer.endCompound();
    }

    private static void writeNameCompound(NBTWriter writer, String field, String id)
            throws IOException {
        writer.name(field).beginCompound();
        writer.name("Name").value(id);
        writer.endCompound();
    }

    private static void writePartCompound(
            NBTWriter writer,
            String field,
            String id,
            int spin
    ) throws IOException {
        writer.name(field).beginCompound();
        writer.name("id").value(id);
        writer.name("spin").value((byte) spin);
        writer.endCompound();
    }
}
