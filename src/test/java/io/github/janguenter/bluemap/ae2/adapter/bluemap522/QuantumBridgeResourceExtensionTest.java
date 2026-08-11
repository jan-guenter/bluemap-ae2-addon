/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.adapter.bluemap522;

import de.bluecolored.bluemap.core.resources.adapter.ResourcesGson;
import de.bluecolored.bluemap.core.resources.pack.PackVersion;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.util.Key;
import de.bluecolored.bluemap.core.world.BlockProperties;
import de.bluecolored.bluemap.core.world.BlockState;
import io.github.janguenter.bluemap.ae2.activation.CraftingRouteActivation;
import io.github.janguenter.bluemap.ae2.activation.DriveRouteActivation;
import io.github.janguenter.bluemap.ae2.activation.ExtendedAeDriveRouteActivation;
import io.github.janguenter.bluemap.ae2.activation.ProfileActivation;
import io.github.janguenter.bluemap.ae2.activation.QuantumBridgeRouteActivation;
import io.github.janguenter.bluemap.ae2.activation.QuartzGlassRouteActivation;
import io.github.janguenter.bluemap.ae2.profile.Ae219217CraftingProfile;
import io.github.janguenter.bluemap.ae2.profile.Ae219217DriveProfile;
import io.github.janguenter.bluemap.ae2.profile.Ae219217Profile;
import io.github.janguenter.bluemap.ae2.profile.Ae219217QuantumBridgeProfile;
import io.github.janguenter.bluemap.ae2.profile.Ae219217QuartzGlassProfile;
import io.github.janguenter.bluemap.ae2.profile.ExactArtifactDetector;
import io.github.janguenter.bluemap.ae2.profile.ProfileDisablement;
import io.github.janguenter.bluemap.ae2.profile.extendedae.ExtendedAe2233Profile;
import io.github.janguenter.bluemap.ae2.profile.extendedae.ExtendedAeArtifactDetector;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QuantumBridgeResourceExtensionTest {

    private static final Key CABLE_BUS = Key.parse(Ae219217Profile.CABLE_BUS_BLOCK);
    private static final Key CABLE_BUS_SYNTHETIC = Key.parse(
            Ae219217Profile.SYNTHETIC_BLOCK_STATE
    );
    private static final Key DRIVE = Key.parse(Ae219217DriveProfile.DRIVE_BLOCK);
    private static final Key DRIVE_SYNTHETIC = Key.parse(
            Ae219217DriveProfile.SYNTHETIC_BLOCK_STATE
    );
    private static final Key EXTENDED_DRIVE = Key.parse(ExtendedAe2233Profile.BLOCK);
    private static final Key EXTENDED_DRIVE_SYNTHETIC = Key.parse(
            ExtendedAe2233Profile.SYNTHETIC_BLOCK_STATE
    );
    private static final Key QUARTZ_GLASS = Key.parse(
            Ae219217QuartzGlassProfile.QUARTZ_GLASS_BLOCK
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
    private static final Key LINK = Key.parse(
            Ae219217QuantumBridgeProfile.QUANTUM_LINK_BLOCK
    );
    private static final Key RING = Key.parse(
            Ae219217QuantumBridgeProfile.QUANTUM_RING_BLOCK
    );
    private static final Key QUANTUM_SYNTHETIC = Key.parse(
            Ae219217QuantumBridgeProfile.SYNTHETIC_BLOCK_STATE
    );

    @Test
    void routingAndPartialShapePropertiesAreIndependentFromAcceptedRoutes() {
        ProfileActivation core = new ProfileActivation();
        DriveRouteActivation drive = new DriveRouteActivation();
        ExtendedAeDriveRouteActivation extended = new ExtendedAeDriveRouteActivation();
        QuartzGlassRouteActivation glass = new QuartzGlassRouteActivation();
        CraftingRouteActivation crafting = new CraftingRouteActivation();
        QuantumBridgeRouteActivation quantum = new QuantumBridgeRouteActivation();
        Ae2ResourceExtension extension = new Ae2ResourceExtension(
                null,
                core,
                drive,
                extended,
                glass,
                crafting,
                quantum
        );

        core.activate();
        drive.activate();
        extended.activate();
        glass.activate();
        crafting.activate();
        assertEquals(LINK, extension.getBlockStateKey(LINK));
        assertEquals(RING, extension.getBlockStateKey(RING));

        quantum.activate();
        assertEquals(QUANTUM_SYNTHETIC, extension.getBlockStateKey(LINK));
        assertEquals(QUANTUM_SYNTHETIC, extension.getBlockStateKey(RING));
        BlockProperties.Builder builder = BlockProperties.builder()
                .culling(true)
                .occluding(true)
                .cullingIdentical(true);
        extension.getBlockProperties(exactState(LINK, true, false), builder);
        BlockProperties properties = builder.build();
        assertFalse(properties.isCulling());
        assertFalse(properties.isOccluding());
        assertFalse(properties.getCullingIdentical());

        quantum.disable(QuantumBridgeRouteActivation.Reason.RENDER_CALLBACK_FAILED);
        assertEquals(LINK, extension.getBlockStateKey(LINK));
        assertEquals(RING, extension.getBlockStateKey(RING));
        assertEquals(CABLE_BUS_SYNTHETIC, extension.getBlockStateKey(CABLE_BUS));
        assertEquals(DRIVE_SYNTHETIC, extension.getBlockStateKey(DRIVE));
        assertEquals(
                EXTENDED_DRIVE_SYNTHETIC,
                extension.getBlockStateKey(EXTENDED_DRIVE)
        );
        assertEquals(
                QUARTZ_GLASS_SYNTHETIC,
                extension.getBlockStateKey(QUARTZ_GLASS)
        );
        assertEquals(CRAFTING_SYNTHETIC, extension.getBlockStateKey(CRAFTING_UNIT));
    }

    @Test
    void exactStateAndSyntheticDispatchContractsAreClosed() {
        for (Key block : List.of(LINK, RING)) {
            for (boolean formed : List.of(false, true)) {
                for (boolean waterlogged : List.of(false, true)) {
                    BlockState state = exactState(block, formed, waterlogged);
                    assertTrue(Ae2ResourceExtension.isExactQuantumBridgeState(state));
                    assertEquals(
                            formed,
                            Ae2ResourceExtension.isExactFormedQuantumBridgeState(state)
                    );
                }
            }
        }
        assertFalse(Ae2ResourceExtension.isExactQuantumBridgeState(
                BlockState.fromString("ae2:quantum_link[formed=true]")
        ));
        assertFalse(Ae2ResourceExtension.isExactQuantumBridgeState(
                BlockState.fromString(
                        "ae2:quantum_ring[formed=true,future=false,waterlogged=false]"
                )
        ));
        assertFalse(Ae2ResourceExtension.isExactQuantumBridgeState(
                BlockState.fromString("ae2:quantum_ring[formed=yes,waterlogged=false]")
        ));
        assertFalse(Ae2ResourceExtension.isExactQuantumBridgeState(
                BlockState.fromString("example:quantum_ring[formed=true,waterlogged=false]")
        ));

        assertTrue(BlueMap522Adapter.install());
        assertTrue(Ae2ResourceExtension.isExpectedQuantumBridgeSyntheticBlockState(
                syntheticQuantumState()
        ));
        assertFalse(Ae2ResourceExtension.isExpectedQuantumBridgeSyntheticBlockState(
                parse("""
                        {"variants":{"":{"renderer":"bluemap:default",
                                           "model":"bluemap:block/missing"}}}
                        """)
        ));
    }

    @Test
    void operatorDisablementPrecedesTheLazyProfileProbeAndPreservesPeers()
            throws Exception {
        String property = ProfileDisablement.SYSTEM_PROPERTY;
        String previous = System.getProperty(property);
        try {
            System.setProperty(property, Ae219217QuantumBridgeProfile.PROFILE_ID);
            LoadFixture fixture = loadFixture(() -> {
                throw new AssertionError("operator disablement must precede profile access");
            });

            fixture.extension().loadResources(List.of());

            assertAcceptedRoutesActive(fixture);
            assertTrue(fixture.quantum().isDisabled());
            assertEquals("quantum-bridge-disabled-by-operator", fixture.quantum().reason());
        } finally {
            restoreProperty(property, previous);
        }
    }

    @Test
    void profileLinkageFailureIsContainedToTheQuantumLoadRoute() throws Exception {
        LoadFixture fixture = loadFixture(() -> {
            throw new NoClassDefFoundError("injected-quantum-profile");
        });

        assertDoesNotThrow(() -> fixture.extension().loadResources(List.of()));

        assertAcceptedRoutesActive(fixture);
        assertTrue(fixture.quantum().isDisabled());
        assertEquals(
                "quantum-bridge-resource-load-callback-failed",
                fixture.quantum().reason()
        );
        assertEquals(LINK, fixture.extension().getBlockStateKey(LINK));
    }

    @Test
    void resourceLinkageFailureIsContainedToTheQuantumBakeRoute() throws Exception {
        ResourcePack resourcePack = M3eQuantumBridgeResourceModelsTest.exactResources();
        Ae2ResourceExtensionTest.putValidM2Resources(resourcePack);
        ProfileActivation core = new ProfileActivation();
        QuantumBridgeRouteActivation quantum = new QuantumBridgeRouteActivation();
        core.activate();
        quantum.activate();
        Ae2ResourceExtension extension = extension(
                resourcePack,
                core,
                new DriveRouteActivation(),
                new ExtendedAeDriveRouteActivation(),
                new QuartzGlassRouteActivation(),
                new CraftingRouteActivation(),
                quantum,
                () -> true,
                ignored -> {
                    throw new NoClassDefFoundError("injected-quantum-resources");
                }
        );

        assertDoesNotThrow(extension::bake);

        assertTrue(core.isActive(), core.reason());
        assertTrue(quantum.isDisabled());
        assertEquals(
                "quantum-bridge-resource-bake-callback-failed",
                quantum.reason()
        );
        assertEquals(CABLE_BUS_SYNTHETIC, extension.getBlockStateKey(CABLE_BUS));
        assertEquals(LINK, extension.getBlockStateKey(LINK));
    }

    private static LoadFixture loadFixture(
            Ae2ResourceExtension.QuantumBridgeProfileProbe quantumProfileProbe
    ) throws Exception {
        assertTrue(BlueMap522Adapter.install());
        ResourcePack resourcePack = new ResourcePack(new PackVersion(34, 0));
        putSyntheticStates(resourcePack);
        ProfileActivation core = new ProfileActivation();
        DriveRouteActivation drive = new DriveRouteActivation();
        ExtendedAeDriveRouteActivation extended = new ExtendedAeDriveRouteActivation();
        QuartzGlassRouteActivation glass = new QuartzGlassRouteActivation();
        CraftingRouteActivation crafting = new CraftingRouteActivation();
        QuantumBridgeRouteActivation quantum = new QuantumBridgeRouteActivation();
        Ae2ResourceExtension extension = extension(
                resourcePack,
                core,
                drive,
                extended,
                glass,
                crafting,
                quantum,
                quantumProfileProbe,
                M3eQuantumBridgeResourceModels::resourcesSupported
        );
        return new LoadFixture(
                extension,
                core,
                drive,
                extended,
                glass,
                crafting,
                quantum
        );
    }

    private static Ae2ResourceExtension extension(
            ResourcePack resourcePack,
            ProfileActivation core,
            DriveRouteActivation drive,
            ExtendedAeDriveRouteActivation extended,
            QuartzGlassRouteActivation glass,
            CraftingRouteActivation crafting,
            QuantumBridgeRouteActivation quantum,
            Ae2ResourceExtension.QuantumBridgeProfileProbe quantumProfileProbe,
            Ae2ResourceExtension.QuantumBridgeResourceProbe quantumResourceProbe
    ) {
        return new Ae2ResourceExtension(
                resourcePack,
                core,
                drive,
                extended,
                glass,
                crafting,
                quantum,
                ignored -> new ExactArtifactDetector.Detection(
                        true,
                        Ae219217Profile.EXACT_REASON
                ),
                ignored -> new ExtendedAeArtifactDetector.Detection(
                        true,
                        ExtendedAe2233Profile.EXACT_REASON
                ),
                () -> true,
                () -> true,
                quantumProfileProbe,
                quantumResourceProbe
        );
    }

    private static void assertAcceptedRoutesActive(LoadFixture fixture) {
        assertTrue(fixture.core().isActive(), fixture.core().reason());
        assertTrue(fixture.drive().isActive(), fixture.drive().reason());
        assertTrue(fixture.extended().isActive(), fixture.extended().reason());
        assertTrue(fixture.glass().isActive(), fixture.glass().reason());
        assertTrue(fixture.crafting().isActive(), fixture.crafting().reason());
        assertEquals(
                CABLE_BUS_SYNTHETIC,
                fixture.extension().getBlockStateKey(CABLE_BUS)
        );
        assertEquals(DRIVE_SYNTHETIC, fixture.extension().getBlockStateKey(DRIVE));
        assertEquals(
                EXTENDED_DRIVE_SYNTHETIC,
                fixture.extension().getBlockStateKey(EXTENDED_DRIVE)
        );
        assertEquals(
                QUARTZ_GLASS_SYNTHETIC,
                fixture.extension().getBlockStateKey(QUARTZ_GLASS)
        );
        assertEquals(
                CRAFTING_SYNTHETIC,
                fixture.extension().getBlockStateKey(CRAFTING_UNIT)
        );
    }

    private static BlockState exactState(Key block, boolean formed, boolean waterlogged) {
        return BlockState.fromString(
                block.getFormatted() + "[formed=" + formed
                        + ",waterlogged=" + waterlogged + "]"
        );
    }

    private static void putSyntheticStates(ResourcePack resourcePack) {
        resourcePack.getBlockStates().put(CABLE_BUS_SYNTHETIC, parse("""
                {"variants":{"":{"renderer":"bluemap_ae2:fluix_glass_cable",
                                   "model":"bluemap:block/missing"}}}
                """));
        resourcePack.getBlockStates().put(DRIVE_SYNTHETIC, parse("""
                {"variants":{"":{"renderer":"bluemap_ae2:drive",
                                   "model":"bluemap:block/missing"}}}
                """));
        resourcePack.getBlockStates().put(EXTENDED_DRIVE_SYNTHETIC, parse("""
                {"variants":{"":{"renderer":"bluemap_ae2:extendedae_ex_drive",
                                   "model":"bluemap:block/missing"}}}
                """));
        resourcePack.getBlockStates().put(QUARTZ_GLASS_SYNTHETIC, parse("""
                {"variants":{"":{"renderer":"bluemap_ae2:quartz_glass",
                                   "model":"bluemap:block/missing"}}}
                """));
        resourcePack.getBlockStates().put(CRAFTING_SYNTHETIC, parse("""
                {"variants":{"":{"renderer":"bluemap_ae2:crafting",
                                   "model":"bluemap:block/missing"}}}
                """));
        resourcePack.getBlockStates().put(QUANTUM_SYNTHETIC, syntheticQuantumState());
    }

    private static de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.BlockState
            syntheticQuantumState() {
        return parse("""
                {"variants":{"":{"renderer":"bluemap_ae2:quantum_bridge",
                                   "model":"bluemap:block/missing"}}}
                """);
    }

    private static de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.BlockState
            parse(String json) {
        return ResourcesGson.INSTANCE.fromJson(
                json,
                de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.BlockState.class
        );
    }

    private static void restoreProperty(String property, String previous) {
        if (previous == null) {
            System.clearProperty(property);
        } else {
            System.setProperty(property, previous);
        }
    }

    private record LoadFixture(
            Ae2ResourceExtension extension,
            ProfileActivation core,
            DriveRouteActivation drive,
            ExtendedAeDriveRouteActivation extended,
            QuartzGlassRouteActivation glass,
            CraftingRouteActivation crafting,
            QuantumBridgeRouteActivation quantum
    ) {
    }
}
