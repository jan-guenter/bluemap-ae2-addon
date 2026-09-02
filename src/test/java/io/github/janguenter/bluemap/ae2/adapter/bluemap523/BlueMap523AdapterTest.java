/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.adapter.bluemap523;

import de.bluecolored.bluemap.core.map.hires.block.BlockRendererType;
import de.bluecolored.bluemap.core.util.Key;
import de.bluecolored.bluemap.core.util.Keyed;
import de.bluecolored.bluemap.core.util.Registry;
import de.bluecolored.bluemap.core.world.mca.blockentity.BlockEntityType;
import io.github.janguenter.bluemap.addon.adapter.api.bluemap523.RegistryGuard;
import io.github.janguenter.bluemap.ae2.activation.QuantumBridgeRouteActivation;
import io.github.janguenter.bluemap.ae2.profile.Ae219217CraftingProfile;
import io.github.janguenter.bluemap.ae2.profile.Ae219217DriveProfile;
import io.github.janguenter.bluemap.ae2.profile.Ae219217Profile;
import io.github.janguenter.bluemap.ae2.profile.extendedae.ExtendedAe2233Profile;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BlueMap523AdapterTest {

    @Test
    void installsTheDtoBeforeTheFirstBlueNbtRetentionProbe() {
        assertTrue(BlueMap523Adapter.install());

        BlockEntityType type = BlockEntityType.REGISTRY.get(
                Key.parse(Ae219217Profile.CABLE_BUS_BLOCK)
        );
        assertEquals(Ae2CableBusBlockEntityData.class, type.getBlockEntityClass());
        assertFalse(BlueMap523Adapter.activationForTesting().isActive());

        assertTrue(BlueMap523Adapter.probeBlockEntityRetention());
        assertTrue(BlueMap523Adapter.probeNativeStructuralBlockEntityRetention());

        BlockEntityType driveType = BlockEntityType.REGISTRY.get(
                Key.parse(Ae219217DriveProfile.DRIVE_BLOCK)
        );
        assertEquals(Ae2DriveBlockEntityData.class, driveType.getBlockEntityClass());
        assertFalse(BlueMap523Adapter.driveActivationForTesting().isActive());
        assertTrue(BlueMap523Adapter.probeDriveBlockEntityRetention());

        BlockEntityType extendedDriveType = BlockEntityType.REGISTRY.get(
                Key.parse(ExtendedAe2233Profile.BLOCK)
        );
        assertEquals(
                ExtendedAeDriveBlockEntityData.class,
                extendedDriveType.getBlockEntityClass()
        );
        assertFalse(BlueMap523Adapter.extendedDriveActivationForTesting().isActive());
        assertTrue(BlueMap523Adapter.probeExtendedDriveBlockEntityRetention());

        assertFalse(BlueMap523Adapter.quartzGlassActivationForTesting().isActive());

        BlockEntityType craftingMonitorType = BlockEntityType.REGISTRY.get(
                Key.parse(Ae219217CraftingProfile.CRAFTING_MONITOR_BLOCK)
        );
        assertEquals(
                Ae2CraftingMonitorBlockEntityData.class,
                craftingMonitorType.getBlockEntityClass()
        );
        assertFalse(BlueMap523Adapter.craftingActivationForTesting().isActive());
        assertTrue(BlueMap523Adapter.probeCraftingMonitorBlockEntityRetention());

        assertFalse(BlueMap523Adapter.quantumBridgeActivationForTesting().isActive());
    }

    @Test
    void registryInsertionIsAcceptedOnlyByIdentityReadBack() {
        Registry<TestEntry> registry = new Registry<>();
        TestEntry candidate = new TestEntry(Key.parse("test:candidate"));
        TestEntry collision = new TestEntry(candidate.getKey());

        assertTrue(RegistryGuard.canRegister(registry, candidate));
        assertTrue(RegistryGuard.register(registry, candidate));
        assertSame(candidate, registry.get(candidate.getKey()));
        assertFalse(RegistryGuard.canRegister(registry, collision));
        assertFalse(RegistryGuard.register(registry, collision));
        assertSame(candidate, registry.get(candidate.getKey()));
    }

    @Test
    void quantumRendererRegistryCollisionDisablesOnlyTheCandidateRoute() {
        Registry<BlockRendererType> registry = new Registry<>();
        BlockRendererType collision = new BlockRendererType.Impl(
                Key.parse(M3eQuantumBridgeResourceModels.SYNTHETIC_BLOCK_STATE),
                (resourcePack, textureGallery, renderSettings) -> null
        );
        assertTrue(RegistryGuard.register(registry, collision));

        QuantumBridgeRouteActivation activation =
                new QuantumBridgeRouteActivation();
        activation.activate();
        assertFalse(BlueMap523Adapter.registerQuantumBridgeRendererExact(
                registry,
                activation
        ));

        assertTrue(activation.isDisabled());
        assertEquals("quantum-bridge-registry-collision", activation.reason());
        assertSame(
                collision,
                registry.get(Key.parse(
                        M3eQuantumBridgeResourceModels.SYNTHETIC_BLOCK_STATE
                ))
        );
    }

    private record TestEntry(Key key) implements Keyed {
        @Override
        public Key getKey() {
            return key;
        }
    }
}
