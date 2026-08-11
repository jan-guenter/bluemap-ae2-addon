/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.profile;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Ae219217QuartzGlassProfileTest {

    @Test
    void publicRouteIdentityIsExact() {
        assertEquals("ae2-quartz-glass", Ae219217QuartzGlassProfile.PROFILE_ID);
        assertEquals(
                Set.of("ae2:quartz_glass", "ae2:quartz_vibrant_glass"),
                Ae219217QuartzGlassProfile.BLOCKS
        );
        assertEquals(
                "bluemap_ae2:quartz_glass",
                Ae219217QuartzGlassProfile.SYNTHETIC_BLOCK_STATE
        );
        assertEquals(Ae219217Profile.JAR_SHA256, Ae219217QuartzGlassProfile.JAR_SHA256);
    }

    @Test
    void exactResourcePartitionIsClosedAndDisjoint() {
        assertEquals(22, Ae219217QuartzGlassProfile.requiredResources().size());
        assertEquals(22, Ae219217QuartzGlassProfile.requiredResourceSizes().size());
        assertEquals(
                Ae219217QuartzGlassProfile.requiredResources().keySet(),
                Ae219217QuartzGlassProfile.requiredResourceSizes().keySet()
        );
        assertEquals(
                4_187L,
                Ae219217QuartzGlassProfile.requiredResourceSizes().values().stream()
                        .mapToLong(Long::longValue)
                        .sum()
        );
        assertTrue(Ae219217QuartzGlassProfile.requiredResources().keySet().stream()
                .allMatch(path -> path.startsWith("assets/ae2/")));
        assertTrue(Ae219217Profile.requiredResources().keySet().stream()
                .noneMatch(Ae219217QuartzGlassProfile.requiredResources()::containsKey));
        assertFalse(Ae219217QuartzGlassProfile.requiredResources().containsKey(
                "assets/ae2/textures/block/glass/quartz_glass_item.png"
        ));
    }

    @Test
    void exactTextureSetHasFourBasesAndFifteenNonzeroFrames() {
        assertEquals(19, Ae219217QuartzGlassProfile.textures().size());
        assertEquals(19, Set.copyOf(Ae219217QuartzGlassProfile.textures()).size());
        assertEquals(
                "ae2:block/glass/quartz_glass_a",
                Ae219217QuartzGlassProfile.textures().getFirst()
        );
        assertEquals(
                "ae2:block/glass/quartz_glass_frame1111",
                Ae219217QuartzGlassProfile.textures().getLast()
        );
        assertTrue(Ae219217QuartzGlassProfile.textures().stream()
                .noneMatch(texture -> texture.endsWith("frame0000")));
    }

    @Test
    void exposedCollectionsAreImmutable() {
        assertThrows(
                UnsupportedOperationException.class,
                () -> Ae219217QuartzGlassProfile.BLOCKS.clear()
        );
        assertThrows(
                UnsupportedOperationException.class,
                () -> Ae219217QuartzGlassProfile.requiredResources().clear()
        );
        assertThrows(
                UnsupportedOperationException.class,
                () -> Ae219217QuartzGlassProfile.requiredResourceSizes().clear()
        );
        assertThrows(
                UnsupportedOperationException.class,
                () -> Ae219217QuartzGlassProfile.textures().clear()
        );
    }
}
