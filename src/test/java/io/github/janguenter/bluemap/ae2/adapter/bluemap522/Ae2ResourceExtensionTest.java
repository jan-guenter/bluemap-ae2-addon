/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.adapter.bluemap522;

import de.bluecolored.bluemap.core.resources.adapter.ResourcesGson;
import de.bluecolored.bluemap.core.resources.pack.PackVersion;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.model.Model;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.texture.Texture;
import de.bluecolored.bluemap.core.util.Key;
import de.bluecolored.bluemap.core.world.BlockProperties;
import de.bluecolored.bluemap.core.world.BlockState;
import io.github.janguenter.bluemap.ae2.activation.CraftingRouteActivation;
import io.github.janguenter.bluemap.ae2.activation.DriveRouteActivation;
import io.github.janguenter.bluemap.ae2.activation.ExtendedAeDriveRouteActivation;
import io.github.janguenter.bluemap.ae2.activation.ProfileActivation;
import io.github.janguenter.bluemap.ae2.activation.QuartzGlassRouteActivation;
import io.github.janguenter.bluemap.ae2.model.DriveCellCatalog;
import io.github.janguenter.bluemap.ae2.profile.Ae219217CraftingProfile;
import io.github.janguenter.bluemap.ae2.profile.Ae219217DriveProfile;
import io.github.janguenter.bluemap.ae2.profile.Ae219217Profile;
import io.github.janguenter.bluemap.ae2.profile.Ae219217QuartzGlassProfile;
import io.github.janguenter.bluemap.ae2.profile.ExactArtifactDetector;
import io.github.janguenter.bluemap.ae2.profile.ProfileDisablement;
import io.github.janguenter.bluemap.ae2.profile.extendedae.ExtendedAe2233Profile;
import io.github.janguenter.bluemap.ae2.profile.extendedae.ExtendedAeArtifactDetector;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Ae2ResourceExtensionTest {

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
    private static final Key QUARTZ_GLASS = Key.parse("ae2:quartz_glass");
    private static final Key QUARTZ_VIBRANT_GLASS = Key.parse(
            "ae2:quartz_vibrant_glass"
    );
    private static final Key QUARTZ_GLASS_SYNTHETIC = Key.parse(
            Ae219217QuartzGlassProfile.SYNTHETIC_BLOCK_STATE
    );
    private static final Key CRAFTING_UNIT = Key.parse(
            Ae219217CraftingProfile.CRAFTING_UNIT_BLOCK
    );
    private static final Key CRAFTING_SYNTHETIC = Key.parse(
            Ae219217CraftingProfile.SYNTHETIC_BLOCK_STATE
    );

    @Test
    void routesOnlyWhileActiveAndKeepsEveryCableBusNonOccluding() {
        ProfileActivation activation = new ProfileActivation();
        Ae2ResourceExtension extension = new Ae2ResourceExtension(null, activation);

        assertEquals(CABLE_BUS, extension.getBlockStateKey(CABLE_BUS));
        activation.activate();
        assertEquals(SYNTHETIC, extension.getBlockStateKey(CABLE_BUS));
        assertEquals(
                Key.parse("minecraft:stone"),
                extension.getBlockStateKey(Key.parse("minecraft:stone"))
        );

        BlockProperties.Builder builder = BlockProperties.builder()
                .culling(true)
                .occluding(true)
                .cullingIdentical(true);
        extension.getBlockProperties(exactState(), builder);
        BlockProperties properties = builder.build();
        assertFalse(properties.isCulling());
        assertFalse(properties.isOccluding());
        assertFalse(properties.getCullingIdentical());
    }

    @Test
    void driveRoutingIsIndependentAndKeepsAtomicFallbackPropertiesSafe() {
        ProfileActivation activation = new ProfileActivation();
        DriveRouteActivation driveActivation = new DriveRouteActivation();
        Ae2ResourceExtension extension = new Ae2ResourceExtension(
                null,
                activation,
                driveActivation
        );

        activation.activate();
        assertEquals(DRIVE, extension.getBlockStateKey(DRIVE));
        driveActivation.activate();
        assertEquals(DRIVE_SYNTHETIC, extension.getBlockStateKey(DRIVE));
        assertEquals(SYNTHETIC, extension.getBlockStateKey(CABLE_BUS));

        BlockProperties.Builder builder = BlockProperties.builder()
                .culling(true)
                .occluding(true)
                .cullingIdentical(true);
        extension.getBlockProperties(exactDriveState("north", 0), builder);
        BlockProperties properties = builder.build();
        assertFalse(properties.isCulling());
        assertFalse(properties.isOccluding());
        assertFalse(properties.getCullingIdentical());

        driveActivation.disable(DriveRouteActivation.Reason.RENDER_CALLBACK_FAILED);
        assertEquals(DRIVE, extension.getBlockStateKey(DRIVE));
        assertEquals(SYNTHETIC, extension.getBlockStateKey(CABLE_BUS));
    }

    @Test
    void extendedDriveRoutingIsIndependentFromTheAcceptedCoreAndNativeDrive() {
        ProfileActivation activation = new ProfileActivation();
        DriveRouteActivation driveActivation = new DriveRouteActivation();
        ExtendedAeDriveRouteActivation extendedActivation =
                new ExtendedAeDriveRouteActivation();
        Ae2ResourceExtension extension = new Ae2ResourceExtension(
                null,
                activation,
                driveActivation,
                extendedActivation
        );

        activation.activate();
        driveActivation.activate();
        assertEquals(EXTENDED_DRIVE, extension.getBlockStateKey(EXTENDED_DRIVE));
        extendedActivation.activate();
        assertEquals(
                EXTENDED_DRIVE_SYNTHETIC,
                extension.getBlockStateKey(EXTENDED_DRIVE)
        );
        assertEquals(DRIVE_SYNTHETIC, extension.getBlockStateKey(DRIVE));
        assertEquals(SYNTHETIC, extension.getBlockStateKey(CABLE_BUS));

        BlockProperties.Builder builder = BlockProperties.builder()
                .culling(true)
                .occluding(true)
                .cullingIdentical(true);
        extension.getBlockProperties(exactExtendedDriveState("south", 3), builder);
        BlockProperties properties = builder.build();
        assertFalse(properties.isCulling());
        assertFalse(properties.isOccluding());
        assertFalse(properties.getCullingIdentical());

        extendedActivation.disable(
                ExtendedAeDriveRouteActivation.Reason.RENDER_CALLBACK_FAILED
        );
        assertEquals(EXTENDED_DRIVE, extension.getBlockStateKey(EXTENDED_DRIVE));
        assertEquals(DRIVE_SYNTHETIC, extension.getBlockStateKey(DRIVE));
        assertEquals(SYNTHETIC, extension.getBlockStateKey(CABLE_BUS));
    }

    @Test
    void bothQuartzGlassVariantsShareOneRouteIndependentFromEveryAcceptedRoute() {
        ProfileActivation activation = new ProfileActivation();
        DriveRouteActivation driveActivation = new DriveRouteActivation();
        ExtendedAeDriveRouteActivation extendedActivation =
                new ExtendedAeDriveRouteActivation();
        QuartzGlassRouteActivation glassActivation = new QuartzGlassRouteActivation();
        Ae2ResourceExtension extension = new Ae2ResourceExtension(
                null,
                activation,
                driveActivation,
                extendedActivation,
                glassActivation
        );

        activation.activate();
        driveActivation.activate();
        extendedActivation.activate();
        assertEquals(QUARTZ_GLASS, extension.getBlockStateKey(QUARTZ_GLASS));
        glassActivation.activate();
        assertEquals(
                QUARTZ_GLASS_SYNTHETIC,
                extension.getBlockStateKey(QUARTZ_GLASS)
        );
        assertEquals(
                QUARTZ_GLASS_SYNTHETIC,
                extension.getBlockStateKey(QUARTZ_VIBRANT_GLASS)
        );

        BlockProperties.Builder builder = BlockProperties.builder()
                .culling(true)
                .occluding(true)
                .cullingIdentical(true);
        extension.getBlockProperties(BlockState.fromString("ae2:quartz_glass"), builder);
        BlockProperties properties = builder.build();
        assertFalse(properties.isCulling());
        assertFalse(properties.isOccluding());
        assertFalse(properties.getCullingIdentical());

        glassActivation.disable(
                QuartzGlassRouteActivation.Reason.RENDER_CALLBACK_FAILED
        );
        assertEquals(QUARTZ_GLASS, extension.getBlockStateKey(QUARTZ_GLASS));
        assertEquals(SYNTHETIC, extension.getBlockStateKey(CABLE_BUS));
        assertEquals(DRIVE_SYNTHETIC, extension.getBlockStateKey(DRIVE));
        assertEquals(
                EXTENDED_DRIVE_SYNTHETIC,
                extension.getBlockStateKey(EXTENDED_DRIVE)
        );
    }

    @Test
    void craftingRoutingIsIndependentAndPreservesExactFullSolidProperties() {
        ProfileActivation activation = new ProfileActivation();
        DriveRouteActivation driveActivation = new DriveRouteActivation();
        ExtendedAeDriveRouteActivation extendedActivation =
                new ExtendedAeDriveRouteActivation();
        QuartzGlassRouteActivation glassActivation = new QuartzGlassRouteActivation();
        CraftingRouteActivation craftingActivation = new CraftingRouteActivation();
        Ae2ResourceExtension extension = new Ae2ResourceExtension(
                null,
                activation,
                driveActivation,
                extendedActivation,
                glassActivation,
                craftingActivation
        );

        activation.activate();
        driveActivation.activate();
        extendedActivation.activate();
        glassActivation.activate();
        assertEquals(CRAFTING_UNIT, extension.getBlockStateKey(CRAFTING_UNIT));
        craftingActivation.activate();
        for (String block : Ae219217CraftingProfile.BLOCKS) {
            assertEquals(
                    CRAFTING_SYNTHETIC,
                    extension.getBlockStateKey(Key.parse(block)),
                    block
            );
        }

        BlockProperties.Builder builder = BlockProperties.builder()
                .culling(false)
                .occluding(false)
                .cullingIdentical(true);
        extension.getBlockProperties(
                BlockState.fromString(
                        "ae2:crafting_unit[formed=true,powered=false]"
                ),
                builder
        );
        BlockProperties properties = builder.build();
        assertTrue(properties.isCulling());
        assertTrue(properties.isOccluding());
        assertFalse(properties.getCullingIdentical());

        craftingActivation.disable(
                CraftingRouteActivation.Reason.RENDER_CALLBACK_FAILED
        );
        assertEquals(CRAFTING_UNIT, extension.getBlockStateKey(CRAFTING_UNIT));
        assertEquals(SYNTHETIC, extension.getBlockStateKey(CABLE_BUS));
        assertEquals(DRIVE_SYNTHETIC, extension.getBlockStateKey(DRIVE));
        assertEquals(
                EXTENDED_DRIVE_SYNTHETIC,
                extension.getBlockStateKey(EXTENDED_DRIVE)
        );
        assertEquals(
                QUARTZ_GLASS_SYNTHETIC,
                extension.getBlockStateKey(QUARTZ_GLASS)
        );
    }

    @Test
    void acceptsOnlyTheExactCurrentCableBusBlockState() {
        assertTrue(Ae2ResourceExtension.isExactCableBusState(exactState()));
        assertFalse(Ae2ResourceExtension.isExactCableBusState(BlockState.fromString(
                "ae2:cable_bus[light_level=1,waterlogged=false]"
        )));
        assertFalse(Ae2ResourceExtension.isExactCableBusState(BlockState.fromString(
                "ae2:cable_bus[light_level=0,waterlogged=true]"
        )));
        assertFalse(Ae2ResourceExtension.isExactCableBusState(BlockState.fromString(
                "ae2:cable_bus"
        )));
        assertFalse(Ae2ResourceExtension.isExactCableBusState(BlockState.fromString(
                "ae2:cable_bus[future=false,light_level=0,waterlogged=false]"
        )));
    }

    @Test
    void acceptsExactlyTheTwentyFourDriveFacingSpinStates() {
        for (String facing : List.of("down", "up", "north", "south", "west", "east")) {
            for (int spin = 0; spin < 4; spin++) {
                assertTrue(Ae2ResourceExtension.isExactDriveState(
                        exactDriveState(facing, spin)
                ));
            }
        }
        assertFalse(Ae2ResourceExtension.isExactDriveState(BlockState.fromString(
                "ae2:drive[facing=north,spin=4]"
        )));
        assertFalse(Ae2ResourceExtension.isExactDriveState(BlockState.fromString(
                "ae2:drive[facing=north]"
        )));
        assertFalse(Ae2ResourceExtension.isExactDriveState(BlockState.fromString(
                "ae2:drive[facing=north,future=false,spin=0]"
        )));

        for (String facing : List.of("down", "up", "north", "south", "west", "east")) {
            for (int spin = 0; spin < 4; spin++) {
                assertTrue(Ae2ResourceExtension.isExactExtendedDriveState(
                        exactExtendedDriveState(facing, spin)
                ));
            }
        }
        assertFalse(Ae2ResourceExtension.isExactExtendedDriveState(BlockState.fromString(
                "extendedae:ex_drive[facing=north,spin=4]"
        )));
        assertFalse(Ae2ResourceExtension.isExactExtendedDriveState(BlockState.fromString(
                "extendedae:ex_drive[facing=north]"
        )));
        assertFalse(Ae2ResourceExtension.isExactExtendedDriveState(BlockState.fromString(
                "extendedae:ex_drive[facing=north,future=false,spin=0]"
        )));
    }

    @Test
    void acceptsExactlyTheTwoPropertyFreeNativeQuartzGlassStates() {
        assertTrue(Ae2ResourceExtension.isExactQuartzGlassState(
                BlockState.fromString("ae2:quartz_glass")
        ));
        assertTrue(Ae2ResourceExtension.isExactQuartzGlassState(
                BlockState.fromString("ae2:quartz_vibrant_glass")
        ));
        assertFalse(Ae2ResourceExtension.isExactQuartzGlassState(
                BlockState.fromString("ae2:quartz_glass[future=false]")
        ));
        assertFalse(Ae2ResourceExtension.isExactQuartzGlassState(
                BlockState.fromString("example:quartz_glass")
        ));
    }

    @Test
    void acceptsOnlyExactCraftingPropertiesIncludingAllMonitorOrientations() {
        for (String block : Ae219217CraftingProfile.BLOCKS) {
            if (Ae219217CraftingProfile.CRAFTING_MONITOR_BLOCK.equals(block)) {
                continue;
            }
            for (boolean formed : List.of(false, true)) {
                for (boolean powered : List.of(false, true)) {
                    BlockState state = BlockState.fromString(
                            block + "[formed=" + formed
                                    + ",powered=" + powered + "]"
                    );
                    assertTrue(Ae2ResourceExtension.isExactCraftingNeighborState(state));
                    assertEquals(
                            formed,
                            Ae2ResourceExtension.isExactFormedCraftingState(state)
                    );
                }
            }
        }

        for (String facing : List.of("down", "up", "north", "south", "west", "east")) {
            for (int spin = 0; spin < 4; spin++) {
                BlockState state = BlockState.fromString(
                        "ae2:crafting_monitor[facing=" + facing
                                + ",formed=true,powered=false,spin=" + spin + "]"
                );
                assertTrue(Ae2ResourceExtension.isExactCraftingNeighborState(state));
                assertTrue(Ae2ResourceExtension.isExactFormedCraftingState(state));
            }
        }
        assertFalse(Ae2ResourceExtension.isExactCraftingNeighborState(
                BlockState.fromString("ae2:crafting_unit[formed=true]")
        ));
        assertFalse(Ae2ResourceExtension.isExactCraftingNeighborState(
                BlockState.fromString(
                        "ae2:crafting_monitor[facing=north,formed=true,"
                                + "powered=false,spin=4]"
                )
        ));
        assertFalse(Ae2ResourceExtension.isExactCraftingNeighborState(
                BlockState.fromString(
                        "ae2:crafting_unit[formed=true,powered=false,future=false]"
                )
        ));
    }

    @Test
    void validatesSyntheticDispatchStructurallyAndByRendererIdentity() {
        assertTrue(BlueMap522Adapter.install());
        String exact = """
                {"variants":{"":{"renderer":"bluemap_ae2:fluix_glass_cable",
                                   "model":"bluemap:block/missing"}}}
                """;

        assertTrue(Ae2ResourceExtension.isExpectedSyntheticBlockState(parse(exact)));
        assertFalse(Ae2ResourceExtension.isExpectedSyntheticBlockState(parse(
                exact.replace("bluemap:block/missing", "minecraft:block/stone")
        )));
        assertFalse(Ae2ResourceExtension.isExpectedSyntheticBlockState(parse(
                exact.replace("bluemap_ae2:fluix_glass_cable", "bluemap:default")
        )));

        String drive = """
                {"variants":{"":{"renderer":"bluemap_ae2:drive",
                                   "model":"bluemap:block/missing"}}}
                """;
        assertTrue(Ae2ResourceExtension.isExpectedDriveSyntheticBlockState(parse(drive)));
        assertFalse(Ae2ResourceExtension.isExpectedDriveSyntheticBlockState(parse(
                drive.replace("bluemap_ae2:drive", "bluemap:default")
        )));

        String extendedDrive = """
                {"variants":{"":{"renderer":"bluemap_ae2:extendedae_ex_drive",
                                   "model":"bluemap:block/missing"}}}
                """;
        assertTrue(Ae2ResourceExtension.isExpectedExtendedDriveSyntheticBlockState(
                parse(extendedDrive)
        ));
        assertFalse(Ae2ResourceExtension.isExpectedExtendedDriveSyntheticBlockState(
                parse(extendedDrive.replace(
                        "bluemap_ae2:extendedae_ex_drive",
                        "bluemap:default"
                ))
        ));

        String quartzGlass = """
                {"variants":{"":{"renderer":"bluemap_ae2:quartz_glass",
                                   "model":"bluemap:block/missing"}}}
                """;
        assertTrue(Ae2ResourceExtension.isExpectedQuartzGlassSyntheticBlockState(
                parse(quartzGlass)
        ));
        assertFalse(Ae2ResourceExtension.isExpectedQuartzGlassSyntheticBlockState(
                parse(quartzGlass.replace("bluemap_ae2:quartz_glass", "bluemap:default"))
        ));

        String crafting = """
                {"variants":{"":{"renderer":"bluemap_ae2:crafting",
                                   "model":"bluemap:block/missing"}}}
                """;
        assertTrue(Ae2ResourceExtension.isExpectedCraftingSyntheticBlockState(
                parse(crafting)
        ));
        assertFalse(Ae2ResourceExtension.isExpectedCraftingSyntheticBlockState(
                parse(crafting.replace("bluemap_ae2:crafting", "bluemap:default"))
        ));
    }

    @Test
    void craftingProfileLinkageFailureLeavesEveryAcceptedRouteRoutable()
            throws Exception {
        assertTrue(BlueMap522Adapter.install());
        ResourcePack resourcePack = new ResourcePack(new PackVersion(34, 0));
        putSyntheticStates(resourcePack);
        ProfileActivation activation = new ProfileActivation();
        DriveRouteActivation driveActivation = new DriveRouteActivation();
        ExtendedAeDriveRouteActivation extendedActivation =
                new ExtendedAeDriveRouteActivation();
        QuartzGlassRouteActivation glassActivation = new QuartzGlassRouteActivation();
        CraftingRouteActivation craftingActivation = new CraftingRouteActivation();
        Ae2ResourceExtension extension = new Ae2ResourceExtension(
                resourcePack,
                activation,
                driveActivation,
                extendedActivation,
                glassActivation,
                craftingActivation,
                ignored -> new ExactArtifactDetector.Detection(
                        true,
                        Ae219217Profile.EXACT_REASON
                ),
                ignored -> new ExtendedAeArtifactDetector.Detection(
                        true,
                        ExtendedAe2233Profile.EXACT_REASON
                ),
                () -> {
                    throw new NoClassDefFoundError("injected-crafting-profile");
                }
        );

        assertDoesNotThrow(() -> extension.loadResources(List.of()));

        assertTrue(activation.isActive(), activation.reason());
        assertTrue(driveActivation.isActive(), driveActivation.reason());
        assertTrue(extendedActivation.isActive(), extendedActivation.reason());
        assertTrue(glassActivation.isActive(), glassActivation.reason());
        assertTrue(craftingActivation.isDisabled());
        assertEquals(
                "crafting-resource-load-callback-failed",
                craftingActivation.reason()
        );
        assertEquals(SYNTHETIC, extension.getBlockStateKey(CABLE_BUS));
        assertEquals(DRIVE_SYNTHETIC, extension.getBlockStateKey(DRIVE));
        assertEquals(
                EXTENDED_DRIVE_SYNTHETIC,
                extension.getBlockStateKey(EXTENDED_DRIVE)
        );
        assertEquals(
                QUARTZ_GLASS_SYNTHETIC,
                extension.getBlockStateKey(QUARTZ_GLASS)
        );
        assertEquals(CRAFTING_UNIT, extension.getBlockStateKey(CRAFTING_UNIT));
    }

    @Test
    void operatorDisablementAvoidsCraftingProfileAccessAndPreservesPeers()
            throws Exception {
        String property = ProfileDisablement.SYSTEM_PROPERTY;
        String previous = System.getProperty(property);
        try {
            System.setProperty(property, Ae219217CraftingProfile.PROFILE_ID);
            assertTrue(BlueMap522Adapter.install());
            ResourcePack resourcePack = new ResourcePack(new PackVersion(34, 0));
            putSyntheticStates(resourcePack);
            ProfileActivation activation = new ProfileActivation();
            DriveRouteActivation driveActivation = new DriveRouteActivation();
            ExtendedAeDriveRouteActivation extendedActivation =
                    new ExtendedAeDriveRouteActivation();
            QuartzGlassRouteActivation glassActivation =
                    new QuartzGlassRouteActivation();
            CraftingRouteActivation craftingActivation =
                    new CraftingRouteActivation();
            int[] profileCalls = {0};
            Ae2ResourceExtension extension = new Ae2ResourceExtension(
                    resourcePack,
                    activation,
                    driveActivation,
                    extendedActivation,
                    glassActivation,
                    craftingActivation,
                    ignored -> new ExactArtifactDetector.Detection(
                            true,
                            Ae219217Profile.EXACT_REASON
                    ),
                    ignored -> new ExtendedAeArtifactDetector.Detection(
                            true,
                            ExtendedAe2233Profile.EXACT_REASON
                    ),
                    () -> {
                        profileCalls[0]++;
                        return true;
                    }
            );

            extension.loadResources(List.of());

            assertEquals(0, profileCalls[0]);
            assertTrue(activation.isActive(), activation.reason());
            assertTrue(driveActivation.isActive(), driveActivation.reason());
            assertTrue(extendedActivation.isActive(), extendedActivation.reason());
            assertTrue(glassActivation.isActive(), glassActivation.reason());
            assertTrue(craftingActivation.isDisabled());
            assertEquals("crafting-disabled-by-operator", craftingActivation.reason());
        } finally {
            if (previous == null) {
                System.clearProperty(property);
            } else {
                System.setProperty(property, previous);
            }
        }
    }

    @Test
    void craftingDtoRetentionFailureDisablesOnlyCraftingRoute()
            throws Exception {
        assertTrue(BlueMap522Adapter.install());
        ResourcePack resourcePack = new ResourcePack(new PackVersion(34, 0));
        putSyntheticStates(resourcePack);
        ProfileActivation activation = new ProfileActivation();
        DriveRouteActivation driveActivation = new DriveRouteActivation();
        ExtendedAeDriveRouteActivation extendedActivation =
                new ExtendedAeDriveRouteActivation();
        QuartzGlassRouteActivation glassActivation = new QuartzGlassRouteActivation();
        CraftingRouteActivation craftingActivation = new CraftingRouteActivation();
        Ae2ResourceExtension extension = new Ae2ResourceExtension(
                resourcePack,
                activation,
                driveActivation,
                extendedActivation,
                glassActivation,
                craftingActivation,
                ignored -> new ExactArtifactDetector.Detection(
                        true,
                        Ae219217Profile.EXACT_REASON
                ),
                ignored -> new ExtendedAeArtifactDetector.Detection(
                        true,
                        ExtendedAe2233Profile.EXACT_REASON
                ),
                () -> true,
                () -> false
        );

        extension.loadResources(List.of());

        assertTrue(activation.isActive(), activation.reason());
        assertTrue(driveActivation.isActive(), driveActivation.reason());
        assertTrue(extendedActivation.isActive(), extendedActivation.reason());
        assertTrue(glassActivation.isActive(), glassActivation.reason());
        assertTrue(craftingActivation.isDisabled());
        assertEquals(
                "crafting-bluenbt-retention-probe-failed",
                craftingActivation.reason()
        );
        assertEquals(SYNTHETIC, extension.getBlockStateKey(CABLE_BUS));
        assertEquals(DRIVE_SYNTHETIC, extension.getBlockStateKey(DRIVE));
        assertEquals(
                EXTENDED_DRIVE_SYNTHETIC,
                extension.getBlockStateKey(EXTENDED_DRIVE)
        );
        assertEquals(
                QUARTZ_GLASS_SYNTHETIC,
                extension.getBlockStateKey(QUARTZ_GLASS)
        );
        assertEquals(CRAFTING_UNIT, extension.getBlockStateKey(CRAFTING_UNIT));
    }

    @Test
    void missingQuartzGlassSyntheticCannotDisableAcceptedRoutes() throws Exception {
        assertTrue(BlueMap522Adapter.install());
        ResourcePack resourcePack = new ResourcePack(new PackVersion(34, 0));
        resourcePack.getBlockStates().put(SYNTHETIC, syntheticCableState());
        resourcePack.getBlockStates().put(DRIVE_SYNTHETIC, syntheticDriveState());
        ProfileActivation activation = new ProfileActivation();
        DriveRouteActivation driveActivation = new DriveRouteActivation();
        ExtendedAeDriveRouteActivation extendedActivation =
                new ExtendedAeDriveRouteActivation();
        QuartzGlassRouteActivation glassActivation = new QuartzGlassRouteActivation();
        Ae2ResourceExtension extension = new Ae2ResourceExtension(
                resourcePack,
                activation,
                driveActivation,
                extendedActivation,
                glassActivation,
                ignored -> new ExactArtifactDetector.Detection(
                        true,
                        Ae219217Profile.EXACT_REASON
                ),
                ignored -> new ExtendedAeArtifactDetector.Detection(
                        false,
                        "extendedae-artifact-not-found"
                )
        );

        extension.loadResources(List.of());

        assertTrue(activation.isActive(), activation.reason());
        assertTrue(driveActivation.isActive(), driveActivation.reason());
        assertFalse(glassActivation.isActive());
        assertFalse(glassActivation.isDisabled());
        assertEquals(
                "quartz-glass-synthetic-blockstate-missing",
                glassActivation.reason()
        );
        assertEquals(SYNTHETIC, extension.getBlockStateKey(CABLE_BUS));
        assertEquals(DRIVE_SYNTHETIC, extension.getBlockStateKey(DRIVE));
        assertEquals(QUARTZ_GLASS, extension.getBlockStateKey(QUARTZ_GLASS));
    }

    @Test
    void quartzGlassTextureCollectionIsDisjointAndDoesNotBroadenCoreGate() {
        ProfileActivation activation = new ProfileActivation();
        QuartzGlassRouteActivation glassActivation = new QuartzGlassRouteActivation();
        Ae2ResourceExtension extension = new Ae2ResourceExtension(
                null,
                activation,
                new DriveRouteActivation(),
                new ExtendedAeDriveRouteActivation(),
                glassActivation
        );

        activation.activate();
        java.util.Set<Key> core = extension.collectUsedTextureKeys();
        assertEquals(149, core.size());
        assertTrue(M3cQuartzGlassResourceModels.requiredTextures().stream()
                .noneMatch(core::contains));

        glassActivation.activate();
        java.util.Set<Key> union = extension.collectUsedTextureKeys();
        assertEquals(168, union.size());
        assertTrue(union.containsAll(core));
        assertTrue(union.containsAll(M3cQuartzGlassResourceModels.requiredTextures()));
    }

    @Test
    void snapshotsOneShotRootsBeforeBothExactArtifactDetectors() throws Exception {
        assertTrue(BlueMap522Adapter.install());
        ResourcePack resourcePack = new ResourcePack(new PackVersion(34, 0));
        putSyntheticStates(resourcePack);
        ProfileActivation activation = new ProfileActivation();
        DriveRouteActivation driveActivation = new DriveRouteActivation();
        ExtendedAeDriveRouteActivation extendedActivation =
                new ExtendedAeDriveRouteActivation();
        Ae2ResourceExtension extension = new Ae2ResourceExtension(
                resourcePack,
                activation,
                driveActivation,
                extendedActivation,
                roots -> {
                    roots.forEach(ignored -> { });
                    return new ExactArtifactDetector.Detection(
                            true,
                            Ae219217Profile.EXACT_REASON
                    );
                },
                roots -> {
                    roots.forEach(ignored -> { });
                    return new ExtendedAeArtifactDetector.Detection(
                            true,
                            ExtendedAe2233Profile.EXACT_REASON
                    );
                }
        );

        extension.loadResources(new OneShotRoots());

        assertTrue(activation.isActive(), activation.reason());
        assertTrue(driveActivation.isActive(), driveActivation.reason());
        assertTrue(extendedActivation.isActive(), extendedActivation.reason());
        assertEquals(SYNTHETIC, extension.getBlockStateKey(CABLE_BUS));
        assertEquals(DRIVE_SYNTHETIC, extension.getBlockStateKey(DRIVE));
        assertEquals(
                EXTENDED_DRIVE_SYNTHETIC,
                extension.getBlockStateKey(EXTENDED_DRIVE)
        );
    }

    @Test
    void missingExtendedSyntheticCannotDisableAcceptedRoutes() throws Exception {
        assertTrue(BlueMap522Adapter.install());
        ResourcePack resourcePack = new ResourcePack(new PackVersion(34, 0));
        resourcePack.getBlockStates().put(SYNTHETIC, syntheticCableState());
        resourcePack.getBlockStates().put(DRIVE_SYNTHETIC, syntheticDriveState());
        ProfileActivation activation = new ProfileActivation();
        DriveRouteActivation driveActivation = new DriveRouteActivation();
        ExtendedAeDriveRouteActivation extendedActivation =
                new ExtendedAeDriveRouteActivation();
        Ae2ResourceExtension extension = new Ae2ResourceExtension(
                resourcePack,
                activation,
                driveActivation,
                extendedActivation,
                ignored -> new ExactArtifactDetector.Detection(
                        true,
                        Ae219217Profile.EXACT_REASON
                ),
                ignored -> new ExtendedAeArtifactDetector.Detection(
                        true,
                        ExtendedAe2233Profile.EXACT_REASON
                )
        );

        extension.loadResources(List.of());

        assertTrue(activation.isActive(), activation.reason());
        assertTrue(driveActivation.isActive(), driveActivation.reason());
        assertFalse(extendedActivation.isActive());
        assertFalse(extendedActivation.isDisabled());
        assertEquals(
                "extended-drive-synthetic-blockstate-missing",
                extendedActivation.reason()
        );
        assertEquals(SYNTHETIC, extension.getBlockStateKey(CABLE_BUS));
        assertEquals(DRIVE_SYNTHETIC, extension.getBlockStateKey(DRIVE));
        assertEquals(EXTENDED_DRIVE, extension.getBlockStateKey(EXTENDED_DRIVE));
    }

    @Test
    void extendedArtifactDetectorFailureIsContainedToTheExtendedRoute()
            throws Exception {
        assertTrue(BlueMap522Adapter.install());
        ResourcePack resourcePack = new ResourcePack(new PackVersion(34, 0));
        putSyntheticStates(resourcePack);
        ProfileActivation activation = new ProfileActivation();
        DriveRouteActivation driveActivation = new DriveRouteActivation();
        ExtendedAeDriveRouteActivation extendedActivation =
                new ExtendedAeDriveRouteActivation();
        Ae2ResourceExtension extension = new Ae2ResourceExtension(
                resourcePack,
                activation,
                driveActivation,
                extendedActivation,
                ignored -> new ExactArtifactDetector.Detection(
                        true,
                        Ae219217Profile.EXACT_REASON
                ),
                ignored -> {
                    throw new IllegalStateException("injected");
                }
        );

        assertDoesNotThrow(() -> extension.loadResources(List.of()));

        assertTrue(activation.isActive(), activation.reason());
        assertTrue(driveActivation.isActive(), driveActivation.reason());
        assertTrue(extendedActivation.isDisabled());
        assertEquals(
                "extended-drive-resource-load-callback-failed",
                extendedActivation.reason()
        );
        assertEquals(SYNTHETIC, extension.getBlockStateKey(CABLE_BUS));
        assertEquals(DRIVE_SYNTHETIC, extension.getBlockStateKey(DRIVE));
        assertEquals(EXTENDED_DRIVE, extension.getBlockStateKey(EXTENDED_DRIVE));
    }

    @Test
    void requiresTheCompleteInstalledM2TextureClosure() throws IOException {
        ResourcePack resourcePack = new ResourcePack(new PackVersion(34, 0));
        List<Key> textures = new ArrayList<>();
        Ae219217Profile.coreTextures().stream().map(Key::parse).forEach(textures::add);
        M2ResourceModels.requiredTextures().stream()
                .filter(key -> !textures.contains(key))
                .forEach(textures::add);

        assertFalse(Ae2ResourceExtension.hasRequiredTextures(resourcePack));
        for (int index = 0; index < textures.size() - 1; index++) {
            putTexture(resourcePack, textures.get(index));
        }
        assertFalse(Ae2ResourceExtension.hasRequiredTextures(resourcePack));
        putTexture(resourcePack, textures.get(textures.size() - 1));
        assertTrue(Ae2ResourceExtension.hasRequiredTextures(resourcePack));
    }

    @Test
    void driveTextureCollectionCannotBroadenTheAcceptedCoreGate() {
        ProfileActivation activation = new ProfileActivation();
        DriveRouteActivation driveActivation = new DriveRouteActivation();
        Ae2ResourceExtension extension = new Ae2ResourceExtension(
                null,
                activation,
                driveActivation
        );

        activation.activate();
        java.util.Set<Key> core = extension.collectUsedTextureKeys();
        assertEquals(149, core.size());
        assertTrue(core.contains(M2ResourceModels.STONE_TEXTURE));
        assertTrue(Ae219217Profile.coreTextures().stream()
                .map(Key::parse)
                .allMatch(core::contains));
        assertTrue(M3DriveResourceModels.requiredTextures().stream()
                .noneMatch(core::contains));

        driveActivation.activate();
        java.util.Set<Key> union = extension.collectUsedTextureKeys();
        assertEquals(159, union.size());
        assertTrue(union.containsAll(core));
        assertTrue(union.containsAll(M3DriveResourceModels.requiredTextures()));
    }

    @Test
    void extendedDriveTextureCollectionIsAnIndependentThirteenTextureClosure() {
        ProfileActivation activation = new ProfileActivation();
        DriveRouteActivation driveActivation = new DriveRouteActivation();
        ExtendedAeDriveRouteActivation extendedActivation =
                new ExtendedAeDriveRouteActivation();
        Ae2ResourceExtension extension = new Ae2ResourceExtension(
                null,
                activation,
                driveActivation,
                extendedActivation
        );

        activation.activate();
        extendedActivation.activate();
        java.util.Set<Key> extendedOnly = extension.collectUsedTextureKeys();
        assertEquals(162, extendedOnly.size());
        assertTrue(extendedOnly.containsAll(
                M3bExtendedAeDriveResourceModels.requiredTextures()
        ));
        assertTrue(M3DriveResourceModels.requiredTextures().stream()
                .anyMatch(texture -> !extendedOnly.contains(texture)));

        driveActivation.activate();
        java.util.Set<Key> union = extension.collectUsedTextureKeys();
        assertEquals(167, union.size());
        assertTrue(union.containsAll(extendedOnly));
        assertTrue(union.containsAll(M3DriveResourceModels.requiredTextures()));
    }

    @Test
    void missingDriveSyntheticLeavesTheAcceptedCoreRouteActive() throws Exception {
        assertTrue(BlueMap522Adapter.install());
        ResourcePack resourcePack = new ResourcePack(new PackVersion(34, 0));
        resourcePack.getBlockStates().put(SYNTHETIC, parse("""
                {"variants":{"":{"renderer":"bluemap_ae2:fluix_glass_cable",
                                   "model":"bluemap:block/missing"}}}
                """));
        ProfileActivation activation = new ProfileActivation();
        DriveRouteActivation driveActivation = new DriveRouteActivation();
        Ae2ResourceExtension extension = new Ae2ResourceExtension(
                resourcePack,
                activation,
                driveActivation,
                ignored -> new ExactArtifactDetector.Detection(
                        true,
                        Ae219217Profile.EXACT_REASON
                )
        );

        extension.loadResources(List.of());

        assertTrue(activation.isActive(), activation.reason());
        assertFalse(driveActivation.isActive());
        assertFalse(driveActivation.isDisabled());
        assertEquals(
                "drive-synthetic-blockstate-missing",
                driveActivation.reason()
        );
        assertEquals(SYNTHETIC, extension.getBlockStateKey(CABLE_BUS));
        assertEquals(DRIVE, extension.getBlockStateKey(DRIVE));
    }

    @Test
    void missingDriveModelAtBakeLeavesAcceptedM2CoreRouteActive() throws Exception {
        ResourcePack resourcePack = M3DriveResourceModelsTest.exactResources();
        putValidM2Resources(resourcePack);
        assertTrue(Ae2ResourceExtension.hasRequiredTextures(resourcePack));
        assertTrue(M2ResourceModels.terminalModelsSupported(resourcePack));
        assertEquals(
                M2ResourceModels.STONE_TEXTURE,
                M2ResourceModels.resolveStoneTexture(resourcePack)
        );
        resourcePack.getModels().remove(Key.parse(DriveCellCatalog.GENERIC_CELL_MODEL));

        ProfileActivation activation = new ProfileActivation();
        DriveRouteActivation driveActivation = new DriveRouteActivation();
        activation.activate();
        driveActivation.activate();
        Ae2ResourceExtension extension = new Ae2ResourceExtension(
                resourcePack,
                activation,
                driveActivation
        );

        extension.bake();

        assertTrue(activation.isActive(), activation.reason());
        assertFalse(driveActivation.isActive());
        assertFalse(driveActivation.isDisabled());
        assertEquals("drive-required-resources-mismatch", driveActivation.reason());
        assertEquals(SYNTHETIC, extension.getBlockStateKey(CABLE_BUS));
        assertEquals(DRIVE, extension.getBlockStateKey(DRIVE));
    }

    @Test
    void missingExtendedModelAtBakeLeavesCoreAndNativeDriveActive()
            throws Exception {
        ResourcePack resourcePack = M3bExtendedAeDriveResourceModelsTest.exactResources();
        putValidM2Resources(resourcePack);
        resourcePack.getModels().remove(
                M3bExtendedAeDriveResourceModels.DRIVE_BASE
        );

        ProfileActivation activation = new ProfileActivation();
        DriveRouteActivation driveActivation = new DriveRouteActivation();
        ExtendedAeDriveRouteActivation extendedActivation =
                new ExtendedAeDriveRouteActivation();
        activation.activate();
        driveActivation.activate();
        extendedActivation.activate();
        Ae2ResourceExtension extension = new Ae2ResourceExtension(
                resourcePack,
                activation,
                driveActivation,
                extendedActivation
        );

        extension.bake();

        assertTrue(activation.isActive(), activation.reason());
        assertTrue(driveActivation.isActive(), driveActivation.reason());
        assertFalse(extendedActivation.isActive());
        assertFalse(extendedActivation.isDisabled());
        assertEquals(
                "extended-drive-required-resources-mismatch",
                extendedActivation.reason()
        );
        assertEquals(SYNTHETIC, extension.getBlockStateKey(CABLE_BUS));
        assertEquals(DRIVE_SYNTHETIC, extension.getBlockStateKey(DRIVE));
        assertEquals(EXTENDED_DRIVE, extension.getBlockStateKey(EXTENDED_DRIVE));
    }

    @Test
    void missingCraftingResourcesAtBakeLeaveAllAcceptedRoutesActive()
            throws Exception {
        ResourcePack resourcePack = M3bExtendedAeDriveResourceModelsTest
                .exactResources();
        ResourcePack glassResources = M3cQuartzGlassResourceModelsTest.exactResources();
        for (Key texture : M3cQuartzGlassResourceModels.requiredTextures()) {
            resourcePack.getTextures().put(
                    texture,
                    glassResources.getTextures().get(texture)
            );
        }
        putValidM2Resources(resourcePack);

        ProfileActivation activation = new ProfileActivation();
        DriveRouteActivation driveActivation = new DriveRouteActivation();
        ExtendedAeDriveRouteActivation extendedActivation =
                new ExtendedAeDriveRouteActivation();
        QuartzGlassRouteActivation glassActivation = new QuartzGlassRouteActivation();
        CraftingRouteActivation craftingActivation = new CraftingRouteActivation();
        activation.activate();
        driveActivation.activate();
        extendedActivation.activate();
        glassActivation.activate();
        craftingActivation.activate();
        Ae2ResourceExtension extension = new Ae2ResourceExtension(
                resourcePack,
                activation,
                driveActivation,
                extendedActivation,
                glassActivation,
                craftingActivation
        );

        extension.bake();

        assertTrue(activation.isActive(), activation.reason());
        assertTrue(driveActivation.isActive(), driveActivation.reason());
        assertTrue(extendedActivation.isActive(), extendedActivation.reason());
        assertTrue(glassActivation.isActive(), glassActivation.reason());
        assertFalse(craftingActivation.isActive());
        assertFalse(craftingActivation.isDisabled());
        assertEquals(
                "crafting-required-resources-mismatch",
                craftingActivation.reason()
        );
    }

    @Test
    void uncheckedResourceLoadFailureDisablesTheRouteWithoutEscaping() {
        ProfileActivation activation = new ProfileActivation();
        Ae2ResourceExtension extension = new Ae2ResourceExtension(
                null,
                activation,
                ignored -> {
                    throw new IllegalStateException("injected");
                }
        );

        assertDoesNotThrow(() -> extension.loadResources(List.of()));
        assertTrue(activation.isDisabled());
        assertEquals("resource-load-callback-failed", activation.reason());
    }

    @Test
    void uncheckedBakeFailureDisablesTheRouteWithoutEscaping() {
        ProfileActivation activation = new ProfileActivation();
        activation.activate();
        Ae2ResourceExtension extension = new Ae2ResourceExtension(null, activation);

        assertDoesNotThrow(extension::bake);
        assertTrue(activation.isDisabled());
        assertEquals("resource-bake-callback-failed", activation.reason());
    }

    private static BlockState exactState() {
        return BlockState.fromString(
                "ae2:cable_bus[light_level=0,waterlogged=false]"
        );
    }

    private static BlockState exactDriveState(String facing, int spin) {
        return BlockState.fromString(
                "ae2:drive[facing=" + facing + ",spin=" + spin + "]"
        );
    }

    private static BlockState exactExtendedDriveState(String facing, int spin) {
        return BlockState.fromString(
                "extendedae:ex_drive[facing=" + facing + ",spin=" + spin + "]"
        );
    }

    private static void putSyntheticStates(ResourcePack resourcePack) {
        resourcePack.getBlockStates().put(SYNTHETIC, syntheticCableState());
        resourcePack.getBlockStates().put(DRIVE_SYNTHETIC, syntheticDriveState());
        resourcePack.getBlockStates().put(
                EXTENDED_DRIVE_SYNTHETIC,
                syntheticExtendedDriveState()
        );
        resourcePack.getBlockStates().put(
                QUARTZ_GLASS_SYNTHETIC,
                syntheticQuartzGlassState()
        );
        resourcePack.getBlockStates().put(
                CRAFTING_SYNTHETIC,
                syntheticCraftingState()
        );
    }

    private static de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.BlockState
            syntheticCableState() {
        return parse("""
                {"variants":{"":{"renderer":"bluemap_ae2:fluix_glass_cable",
                                   "model":"bluemap:block/missing"}}}
                """);
    }

    private static de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.BlockState
            syntheticDriveState() {
        return parse("""
                {"variants":{"":{"renderer":"bluemap_ae2:drive",
                                   "model":"bluemap:block/missing"}}}
                """);
    }

    private static de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.BlockState
            syntheticExtendedDriveState() {
        return parse("""
                {"variants":{"":{"renderer":"bluemap_ae2:extendedae_ex_drive",
                                   "model":"bluemap:block/missing"}}}
                """);
    }

    private static de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.BlockState
            syntheticQuartzGlassState() {
        return parse("""
                {"variants":{"":{"renderer":"bluemap_ae2:quartz_glass",
                                   "model":"bluemap:block/missing"}}}
                """);
    }

    private static de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.BlockState
            syntheticCraftingState() {
        return parse("""
                {"variants":{"":{"renderer":"bluemap_ae2:crafting",
                                   "model":"bluemap:block/missing"}}}
                """);
    }

    private static void putTexture(ResourcePack resourcePack, Key key) throws IOException {
        BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        image.setRGB(0, 0, 0xFFFFFFFF);
        resourcePack.getTextures().put(key, Texture.from(key, image));
    }

    static void putValidM2Resources(ResourcePack resourcePack) throws IOException {
        for (String texture : Ae219217Profile.coreTextures()) {
            putTexture(resourcePack, Key.parse(texture));
        }
        Map<Key, String> models = Map.of(
                Key.parse("ae2:part/display_base"), """
                        {"textures":{"sides":"ae2:part/monitor_sides",
                          "sidesStatus":"ae2:part/monitor_sides_status",
                          "back":"ae2:part/monitor_back","front":"ae2:part/monitor_front"},
                         "elements":[
                          {"from":[2,2,0],"to":[14,14,2],"faces":{
                           "down":{"texture":"#sides"},"up":{"texture":"#sides"},
                           "south":{"texture":"#back"},"east":{"texture":"#sides"},
                           "north":{"texture":"#front"},"west":{"texture":"#sides"}}},
                          {"from":[4,4,2],"to":[12,12,3],"faces":{
                           "down":{"texture":"#sidesStatus"},
                           "up":{"texture":"#sidesStatus"},
                           "south":{"texture":"#back"},
                           "east":{"texture":"#sidesStatus"},
                           "west":{"texture":"#sidesStatus"}}}]}
                        """,
                Key.parse("ae2:part/display_off"), """
                        {"textures":{"lightsBright":"ae2:part/terminal_bright",
                          "lightsMedium":"ae2:part/terminal_medium",
                          "lightsDark":"ae2:part/terminal_dark"},
                         "elements":[
                          {"from":[2,2,0],"to":[14,14,2],"faces":{"north":{
                           "texture":"#lightsBright","tintindex":3}}},
                          {"from":[2,2,0],"to":[14,14,2],"faces":{"north":{
                           "texture":"#lightsMedium","tintindex":2}}},
                          {"from":[2,2,0],"to":[14,14,2],"faces":{"north":{
                           "texture":"#lightsDark","tintindex":1}}}]}
                        """,
                Key.parse("ae2:part/terminal_off"), """
                        {"parent":"ae2:part/display_off",
                         "textures":{"lightsBright":"ae2:part/terminal_bright",
                          "lightsMedium":"ae2:part/terminal_medium",
                          "lightsDark":"ae2:part/terminal_dark"}}
                        """,
                Key.parse("ae2:part/display_status_off"), """
                        {"textures":{"indicator":"ae2:part/monitor_sides_status_off"},
                         "elements":[
                          {"from":[7,11,2],"to":[9,12,3],"faces":{
                           "south":{"texture":"#indicator"},"up":{"texture":"#indicator"}}},
                          {"from":[7,4,2],"to":[9,5,3],"faces":{
                           "south":{"texture":"#indicator"},"down":{"texture":"#indicator"}}},
                          {"from":[4,7,2],"to":[5,9,3],"faces":{
                           "south":{"texture":"#indicator"},"west":{"texture":"#indicator"}}},
                          {"from":[11,7,2],"to":[12,9,3],"faces":{
                           "east":{"texture":"#indicator"},"south":{"texture":"#indicator"}}}]}
                        """,
                Key.parse("minecraft:block/stone"), fullCubeModelJson(),
                Key.parse("minecraft:block/stone_mirrored"), fullCubeModelJson()
        );
        for (Map.Entry<Key, String> entry : models.entrySet()) {
            resourcePack.getModels().put(
                    entry.getKey(),
                    ResourcesGson.INSTANCE.fromJson(entry.getValue(), Model.class)
            );
        }
        resourcePack.getModels().get(Key.parse("ae2:part/terminal_off"))
                .applyParent(resourcePack.getModels());
        resourcePack.getBlockStates().put(
                M2ResourceModels.STONE,
                parse("""
                        {"variants":{"": [
                          {"model":"minecraft:block/stone"},
                          {"model":"minecraft:block/stone","y":180},
                          {"model":"minecraft:block/stone_mirrored"},
                          {"model":"minecraft:block/stone_mirrored","y":180}
                        ]}}
                        """)
        );
        for (Key texture : M2ResourceModels.requiredTextures()) {
            putTexture(resourcePack, texture);
        }
    }

    private static String fullCubeModelJson() {
        return """
                {"textures":{"all":"minecraft:block/stone"},"elements":[{
                 "from":[0,0,0],"to":[16,16,16],"faces":{
                  "down":{"texture":"#all","cullface":"down"},
                  "up":{"texture":"#all","cullface":"up"},
                  "north":{"texture":"#all","cullface":"north"},
                  "south":{"texture":"#all","cullface":"south"},
                  "west":{"texture":"#all","cullface":"west"},
                  "east":{"texture":"#all","cullface":"east"}}}]}
                """;
    }

    private static de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.BlockState
            parse(String json) {
        return ResourcesGson.INSTANCE.fromJson(
                json,
                de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.BlockState.class
        );
    }

    private static final class OneShotRoots implements Iterable<Path> {

        private boolean iterated;

        @Override
        public Iterator<Path> iterator() {
            if (iterated) {
                throw new IllegalStateException("resource roots were iterated twice");
            }
            iterated = true;
            return List.<Path>of().iterator();
        }
    }
}
