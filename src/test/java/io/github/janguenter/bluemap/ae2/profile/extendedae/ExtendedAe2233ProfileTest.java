/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.profile.extendedae;

import io.github.janguenter.bluemap.ae2.profile.Ae219217Profile;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExtendedAe2233ProfileTest {

    @Test
    void exactIdentityAndLayoutAreLocked() {
        assertEquals("extendedae", ExtendedAe2233Profile.PROFILE_ID);
        assertEquals("extendedae:ex_drive", ExtendedAe2233Profile.BLOCK);
        assertEquals(
                "bluemap_ae2:extendedae_ex_drive",
                ExtendedAe2233Profile.SYNTHETIC_BLOCK_STATE
        );
        assertEquals(20, ExtendedAe2233Profile.SLOT_COUNT);
        assertEquals(10, ExtendedAe2233Profile.SIDE_SLOT_COUNT);
        assertEquals(5, ExtendedAe2233Profile.SLOT_ROWS);
        assertEquals(2, ExtendedAe2233Profile.SLOT_COLUMNS);
        assertEquals("ae2:block/drive/drive_front", ExtendedAe2233Profile.LED_TEXTURE);
        assertEquals("static-offline-unknown", ExtendedAe2233Profile.LED_POLICY);
    }

    @Test
    void ownResourcePartitionIsExactAndDisjointFromAe2() {
        assertEquals(15, ExtendedAe2233Profile.requiredResources().size());
        assertEquals(15, ExtendedAe2233Profile.requiredResourceSizes().size());
        assertEquals(8, ExtendedAe2233Profile.textures().size());
        assertEquals(8, Set.copyOf(ExtendedAe2233Profile.textures()).size());
        assertEquals(
                ExtendedAe2233Profile.requiredResources().keySet(),
                ExtendedAe2233Profile.requiredResourceSizes().keySet()
        );
        assertTrue(ExtendedAe2233Profile.requiredResources().keySet().stream()
                .allMatch(path -> path.startsWith("assets/extendedae/")));
        assertTrue(Ae219217Profile.requiredResources().keySet().stream()
                .noneMatch(ExtendedAe2233Profile.requiredResources()::containsKey));
    }

    @Test
    void dependentAe2PartitionAndClosedCatalogRemainSeparate() {
        assertEquals(
                Ae219217Profile.driveRequiredResources(),
                ExtendedAe2233Profile.dependentAe2RequiredResources()
        );
        assertEquals(26, ExtendedAe2233Profile.dependentAe2RequiredResources().size());
        assertEquals(
                Ae219217Profile.driveTextures(),
                ExtendedAe2233Profile.dependentAe2Textures()
        );
        assertEquals(3, ExtendedAe2233Profile.builtInCellModels().size());
        assertEquals(26, ExtendedAe2233Profile.supportedItemIds().size());
        assertEquals(26, ExtendedAe2233Profile.supportedCellModels().size());
        assertEquals(15, Set.copyOf(
                ExtendedAe2233Profile.supportedCellModels().values()
        ).size());
        assertEquals(
                "extendedae:block/drive/infinity_water_cell",
                ExtendedAe2233Profile.supportedCellModels().get(
                        "extendedae:infinity_water_cell"
                )
        );
        assertTrue(ExtendedAe2233Profile.supportedItemIds().containsAll(
                Ae219217Profile.driveCellModels().keySet()
        ));
        assertTrue(ExtendedAe2233Profile.supportedItemIds().stream()
                .noneMatch(item -> item.startsWith("kubejs:") || item.startsWith("megacells:")));
    }

    @Test
    void exposedCollectionsAreImmutable() {
        assertThrows(
                UnsupportedOperationException.class,
                () -> ExtendedAe2233Profile.requiredResources().clear()
        );
        assertThrows(
                UnsupportedOperationException.class,
                () -> ExtendedAe2233Profile.requiredResourceSizes().clear()
        );
        assertThrows(
                UnsupportedOperationException.class,
                () -> ExtendedAe2233Profile.textures().clear()
        );
        assertThrows(
                UnsupportedOperationException.class,
                () -> ExtendedAe2233Profile.supportedCellModels().clear()
        );
        assertThrows(
                UnsupportedOperationException.class,
                () -> ExtendedAe2233Profile.supportedItemIds().clear()
        );
    }
}
