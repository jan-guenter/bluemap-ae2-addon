/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.profile;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Ae219217M3CompletionProfileTest {

    @Test
    void publicRouteIdentityAndEvidenceAreExact() {
        assertEquals("ae2-m3-completion", Ae219217M3CompletionProfile.PROFILE_ID);
        assertEquals(Set.of(
                "ae2:paint",
                "ae2:sky_stone_chest",
                "ae2:smooth_sky_stone_chest",
                "ae2:crank",
                "ae2:inscriber",
                "ae2:spatial_pylon"
        ), Ae219217M3CompletionProfile.BLOCKS);
        assertEquals(Map.of(
                "ae2:paint", "bluemap_ae2:paint",
                "ae2:sky_stone_chest", "bluemap_ae2:sky_stone_chest",
                "ae2:smooth_sky_stone_chest", "bluemap_ae2:sky_stone_chest",
                "ae2:crank", "bluemap_ae2:crank",
                "ae2:inscriber", "bluemap_ae2:inscriber",
                "ae2:spatial_pylon", "bluemap_ae2:spatial_pylon"
        ), Ae219217M3CompletionProfile.SYNTHETIC_BLOCK_STATES);
        assertEquals(Map.of(
                "ae2:paint", "ae2:paint",
                "ae2:sky_stone_chest", "ae2:sky_chest",
                "ae2:smooth_sky_stone_chest", "ae2:sky_chest",
                "ae2:crank", "ae2:crank",
                "ae2:inscriber", "ae2:inscriber",
                "ae2:spatial_pylon", "ae2:spatial_pylon"
        ), Ae219217M3CompletionProfile.BLOCK_ENTITY_IDS);
        assertEquals(Ae219217Profile.JAR_SHA256, Ae219217M3CompletionProfile.JAR_SHA256);
        assertEquals(
                "79ee2c704ad62941a426c26b1cb1f76ef5b2ee5a",
                Ae219217M3CompletionProfile.SOURCE_COMMIT
        );
        assertEquals(
                "d2f451203cb61c2d21fae52c683083d2f72441ca7d26725f4df5934290492e6a",
                Ae219217M3CompletionProfile.SOURCES_JAR_SHA256
        );
    }

    @Test
    void exactResourcePartitionIsClosedAndDisjoint() {
        assertEquals(33, Ae219217M3CompletionProfile.requiredResources().size());
        assertEquals(33, Ae219217M3CompletionProfile.requiredResourceSizes().size());
        assertEquals(
                Ae219217M3CompletionProfile.requiredResources().keySet(),
                Ae219217M3CompletionProfile.requiredResourceSizes().keySet()
        );
        assertEquals(
                22_491L,
                Ae219217M3CompletionProfile.requiredResourceSizes().values().stream()
                        .mapToLong(Long::longValue)
                        .sum()
        );
        assertTrue(Ae219217M3CompletionProfile.requiredResources().keySet().stream()
                .allMatch(path -> path.startsWith("assets/ae2/")));

        Set<String> acceptedResources = new HashSet<>();
        acceptedResources.addAll(Ae219217Profile.requiredResources().keySet());
        acceptedResources.addAll(Ae219217QuartzGlassProfile.requiredResources().keySet());
        acceptedResources.addAll(Ae219217CraftingProfile.requiredResources().keySet());
        acceptedResources.addAll(Ae219217QuantumBridgeProfile.requiredResources().keySet());
        assertTrue(Ae219217M3CompletionProfile.requiredResources().keySet().stream()
                .noneMatch(acceptedResources::contains));
    }

    @Test
    void texturePartitionsExcludeOnlinePylonAndStockFallbackFromEmission() {
        assertEquals(17, Ae219217M3CompletionProfile.sourceTextures().size());
        assertEquals(17, Set.copyOf(Ae219217M3CompletionProfile.sourceTextures()).size());
        assertEquals(15, Ae219217M3CompletionProfile.emittedStaticTextures().size());
        assertEquals(15, Set.copyOf(
                Ae219217M3CompletionProfile.emittedStaticTextures()
        ).size());
        assertEquals(2, Ae219217M3CompletionProfile.fallbackOnlyTextures().size());
        assertTrue(Ae219217M3CompletionProfile.sourceTextures().containsAll(
                Ae219217M3CompletionProfile.emittedStaticTextures()
        ));
        assertFalse(Ae219217M3CompletionProfile.emittedStaticTextures().contains(
                Ae219217M3CompletionProfile.PYLON_DIM_END_TEXTURE
        ));
        assertFalse(Ae219217M3CompletionProfile.emittedStaticTextures().contains(
                Ae219217M3CompletionProfile.PYLON_DIM_SPANNED_TEXTURE
        ));
        assertTrue(Ae219217M3CompletionProfile.emittedStaticTextures().containsAll(List.of(
                Ae219217M3CompletionProfile.PYLON_RED_TEXTURE,
                Ae219217M3CompletionProfile.PYLON_RED_END_TEXTURE,
                Ae219217M3CompletionProfile.PYLON_RED_SPANNED_TEXTURE
        )));
        assertTrue(Ae219217M3CompletionProfile.fallbackOnlyTextures().stream()
                .noneMatch(Ae219217M3CompletionProfile.sourceTextures()::contains));
    }

    @Test
    void boundedStaticPoliciesStayExplicit() {
        assertEquals(1, Ae219217M3CompletionProfile.MIN_PAINT_SPLOTCHES);
        assertEquals(21, Ae219217M3CompletionProfile.MAX_PAINT_SPLOTCHES);
        assertEquals(2, Ae219217M3CompletionProfile.PAINT_TRIANGLES_PER_SPLOTCH);
        assertEquals(24, Ae219217M3CompletionProfile.SPATIAL_PYLON_TRIANGLES);
        assertEquals(36, Ae219217M3CompletionProfile.SKY_STONE_CHEST_TRIANGLES);
        assertEquals(34, Ae219217M3CompletionProfile.CRANK_TRIANGLES);
        assertEquals(78, Ae219217M3CompletionProfile.INSCRIBER_TRIANGLES);
        assertEquals("static-offline-unknown", Ae219217M3CompletionProfile.POWER_POLICY);
        assertEquals(256, Ae219217M3CompletionProfile.SPATIAL_PYLON_COMPONENT_MAX_BLOCKS);
        assertEquals(
                "bounded-locally-invalid-component-unformed-base-plus-dim",
                Ae219217M3CompletionProfile.AMBIGUOUS_POLICY
        );
        assertEquals(
                "atomic-original-resource-fallback",
                Ae219217M3CompletionProfile.INCOMPLETE_COMPONENT_POLICY
        );
    }

    @Test
    void exposedCollectionsAreImmutable() {
        assertThrows(
                UnsupportedOperationException.class,
                () -> Ae219217M3CompletionProfile.BLOCKS.clear()
        );
        assertThrows(
                UnsupportedOperationException.class,
                () -> Ae219217M3CompletionProfile.SYNTHETIC_BLOCK_STATES.clear()
        );
        assertThrows(
                UnsupportedOperationException.class,
                () -> Ae219217M3CompletionProfile.BLOCK_ENTITY_IDS.clear()
        );
        assertThrows(
                UnsupportedOperationException.class,
                () -> Ae219217M3CompletionProfile.requiredResources().clear()
        );
        assertThrows(
                UnsupportedOperationException.class,
                () -> Ae219217M3CompletionProfile.requiredResourceSizes().clear()
        );
        assertThrows(
                UnsupportedOperationException.class,
                () -> Ae219217M3CompletionProfile.sourceTextures().clear()
        );
        assertThrows(
                UnsupportedOperationException.class,
                () -> Ae219217M3CompletionProfile.emittedStaticTextures().clear()
        );
        assertThrows(
                UnsupportedOperationException.class,
                () -> Ae219217M3CompletionProfile.fallbackOnlyTextures().clear()
        );
    }
}
