/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.profile;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Ae219217QuantumBridgeProfileTest {

    @Test
    void publicRouteIdentityAndEvidenceAreExact() {
        assertEquals("ae2-quantum-bridge", Ae219217QuantumBridgeProfile.PROFILE_ID);
        assertEquals(
                Set.of("ae2:quantum_link", "ae2:quantum_ring"),
                Ae219217QuantumBridgeProfile.BLOCKS
        );
        assertEquals(
                "bluemap_ae2:quantum_bridge",
                Ae219217QuantumBridgeProfile.SYNTHETIC_BLOCK_STATE
        );
        assertEquals("ae2:quantum_ring", Ae219217QuantumBridgeProfile.BLOCK_ENTITY_ID);
        assertEquals(Ae219217Profile.JAR_SHA256, Ae219217QuantumBridgeProfile.JAR_SHA256);
        assertEquals(
                "79ee2c704ad62941a426c26b1cb1f76ef5b2ee5a",
                Ae219217QuantumBridgeProfile.SOURCE_COMMIT
        );
        assertEquals(
                "d2f451203cb61c2d21fae52c683083d2f72441ca7d26725f4df5934290492e6a",
                Ae219217QuantumBridgeProfile.SOURCES_JAR_SHA256
        );
    }

    @Test
    void exactResourcePartitionIsClosedAndDisjoint() {
        assertEquals(13, Ae219217QuantumBridgeProfile.requiredResources().size());
        assertEquals(13, Ae219217QuantumBridgeProfile.requiredResourceSizes().size());
        assertEquals(
                Ae219217QuantumBridgeProfile.requiredResources().keySet(),
                Ae219217QuantumBridgeProfile.requiredResourceSizes().keySet()
        );
        assertEquals(
                3_798L,
                Ae219217QuantumBridgeProfile.requiredResourceSizes().values().stream()
                        .mapToLong(Long::longValue)
                        .sum()
        );
        assertTrue(Ae219217QuantumBridgeProfile.requiredResources().keySet().stream()
                .allMatch(path -> path.startsWith("assets/ae2/")));
        Set<String> sharedMainResources = new java.util.HashSet<>(
                Ae219217QuantumBridgeProfile.requiredResources().keySet()
        );
        sharedMainResources.retainAll(Ae219217Profile.requiredResources().keySet());
        assertEquals(Set.of(
                "assets/ae2/textures/part/cable/covered/transparent.png",
                "assets/ae2/textures/part/cable/glass/transparent.png"
        ), sharedMainResources);
        assertTrue(Ae219217QuartzGlassProfile.requiredResources().keySet().stream()
                .noneMatch(Ae219217QuantumBridgeProfile.requiredResources()::containsKey));
        assertTrue(Ae219217CraftingProfile.requiredResources().keySet().stream()
                .noneMatch(Ae219217QuantumBridgeProfile.requiredResources()::containsKey));
    }

    @Test
    void sourceAndStaticOffTextureSetsRemainDistinct() {
        assertEquals(6, Ae219217QuantumBridgeProfile.sourceTextures().size());
        assertEquals(6, Set.copyOf(Ae219217QuantumBridgeProfile.sourceTextures()).size());
        assertEquals(4, Ae219217QuantumBridgeProfile.emittedOffTextures().size());
        assertEquals(4, Set.copyOf(Ae219217QuantumBridgeProfile.emittedOffTextures()).size());
        assertTrue(Ae219217QuantumBridgeProfile.sourceTextures().containsAll(
                Ae219217QuantumBridgeProfile.emittedOffTextures()
        ));
        assertFalse(Ae219217QuantumBridgeProfile.emittedOffTextures().contains(
                Ae219217QuantumBridgeProfile.QUANTUM_RING_LIGHT_TEXTURE
        ));
        assertFalse(Ae219217QuantumBridgeProfile.emittedOffTextures().contains(
                Ae219217QuantumBridgeProfile.QUANTUM_RING_LIGHT_CORNER_TEXTURE
        ));
        assertEquals(
                List.of(
                        Ae219217QuantumBridgeProfile.QUANTUM_LINK_TEXTURE,
                        Ae219217QuantumBridgeProfile.QUANTUM_RING_TEXTURE,
                        Ae219217QuantumBridgeProfile.GLASS_TRANSPARENT_TEXTURE,
                        Ae219217QuantumBridgeProfile.COVERED_TRANSPARENT_TEXTURE
                ),
                Ae219217QuantumBridgeProfile.emittedOffTextures()
        );
    }

    @Test
    void exposedCollectionsAreImmutable() {
        assertThrows(
                UnsupportedOperationException.class,
                () -> Ae219217QuantumBridgeProfile.BLOCKS.clear()
        );
        assertThrows(
                UnsupportedOperationException.class,
                () -> Ae219217QuantumBridgeProfile.requiredResources().clear()
        );
        assertThrows(
                UnsupportedOperationException.class,
                () -> Ae219217QuantumBridgeProfile.requiredResourceSizes().clear()
        );
        assertThrows(
                UnsupportedOperationException.class,
                () -> Ae219217QuantumBridgeProfile.sourceTextures().clear()
        );
        assertThrows(
                UnsupportedOperationException.class,
                () -> Ae219217QuantumBridgeProfile.emittedOffTextures().clear()
        );
    }
}
