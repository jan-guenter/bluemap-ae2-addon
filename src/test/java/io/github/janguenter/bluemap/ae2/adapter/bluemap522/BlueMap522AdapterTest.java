/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.adapter.bluemap522;

import de.bluecolored.bluemap.core.map.hires.block.BlockRendererType;
import de.bluecolored.bluemap.core.util.Key;
import de.bluecolored.bluemap.core.util.Keyed;
import de.bluecolored.bluemap.core.util.Registry;
import de.bluecolored.bluemap.core.world.mca.blockentity.BlockEntityType;
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

class BlueMap522AdapterTest {

    @Test
    void installsTheDtoBeforeTheFirstBlueNbtRetentionProbe() {
        assertTrue(BlueMap522Adapter.install());

        BlockEntityType type = BlockEntityType.REGISTRY.get(
                Key.parse(Ae219217Profile.CABLE_BUS_BLOCK)
        );
        assertEquals(Ae2CableBusBlockEntityData.class, type.getBlockEntityClass());
        assertFalse(BlueMap522Adapter.activationForTesting().isActive());
        assertEquals(
                "awaiting-exact-ae2-profile",
                BlueMap522Adapter.activationForTesting().reason()
        );

        assertTrue(BlueMap522Adapter.probeBlockEntityRetention());
        assertTrue(BlueMap522Adapter.probeNativeStructuralBlockEntityRetention());

        BlockEntityType driveType = BlockEntityType.REGISTRY.get(
                Key.parse(Ae219217DriveProfile.DRIVE_BLOCK)
        );
        assertEquals(Ae2DriveBlockEntityData.class, driveType.getBlockEntityClass());
        assertFalse(BlueMap522Adapter.driveActivationForTesting().isActive());
        assertEquals(
                "awaiting-exact-ae2-drive-profile",
                BlueMap522Adapter.driveActivationForTesting().reason()
        );
        assertTrue(BlueMap522Adapter.probeDriveBlockEntityRetention());

        BlockEntityType extendedDriveType = BlockEntityType.REGISTRY.get(
                Key.parse(ExtendedAe2233Profile.BLOCK)
        );
        assertEquals(
                ExtendedAeDriveBlockEntityData.class,
                extendedDriveType.getBlockEntityClass()
        );
        assertFalse(BlueMap522Adapter.extendedDriveActivationForTesting().isActive());
        assertEquals(
                "awaiting-exact-extended-drive-profile",
                BlueMap522Adapter.extendedDriveActivationForTesting().reason()
        );
        assertTrue(BlueMap522Adapter.probeExtendedDriveBlockEntityRetention());

        assertFalse(BlueMap522Adapter.quartzGlassActivationForTesting().isActive());
        assertEquals(
                "awaiting-exact-quartz-glass-profile",
                BlueMap522Adapter.quartzGlassActivationForTesting().reason()
        );

        BlockEntityType craftingMonitorType = BlockEntityType.REGISTRY.get(
                Key.parse(Ae219217CraftingProfile.CRAFTING_MONITOR_BLOCK)
        );
        assertEquals(
                Ae2CraftingMonitorBlockEntityData.class,
                craftingMonitorType.getBlockEntityClass()
        );
        assertFalse(BlueMap522Adapter.craftingActivationForTesting().isActive());
        assertEquals(
                "awaiting-exact-ae2-crafting-profile",
                BlueMap522Adapter.craftingActivationForTesting().reason()
        );
        assertTrue(BlueMap522Adapter.probeCraftingMonitorBlockEntityRetention());

        assertFalse(BlueMap522Adapter.quantumBridgeActivationForTesting().isActive());
        assertEquals(
                "awaiting-exact-ae2-quantum-bridge-profile",
                BlueMap522Adapter.quantumBridgeActivationForTesting().reason()
        );
    }

    @Test
    void registryInsertionIsAcceptedOnlyByIdentityReadBack() {
        Registry<TestEntry> registry = new Registry<>();
        TestEntry candidate = new TestEntry(Key.parse("test:candidate"));
        TestEntry collision = new TestEntry(candidate.getKey());

        assertTrue(BlueMap522Adapter.canRegisterExact(registry, candidate));
        assertTrue(BlueMap522Adapter.registerExact(registry, candidate));
        assertSame(candidate, registry.get(candidate.getKey()));
        assertFalse(BlueMap522Adapter.canRegisterExact(registry, collision));
        assertFalse(BlueMap522Adapter.registerExact(registry, collision));
        assertSame(candidate, registry.get(candidate.getKey()));
    }

    @Test
    void quantumRendererRegistryCollisionDisablesOnlyTheCandidateRoute() {
        Registry<BlockRendererType> registry = new Registry<>();
        BlockRendererType collision = new BlockRendererType.Impl(
                Key.parse(M3eQuantumBridgeResourceModels.SYNTHETIC_BLOCK_STATE),
                (resourcePack, textureGallery, renderSettings) -> null
        );
        assertTrue(BlueMap522Adapter.registerExact(registry, collision));

        QuantumBridgeRouteActivation activation =
                new QuantumBridgeRouteActivation();
        activation.activate();
        assertFalse(BlueMap522Adapter.registerQuantumBridgeRendererExact(
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
