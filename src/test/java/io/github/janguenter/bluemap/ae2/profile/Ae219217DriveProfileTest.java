/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.profile;

import io.github.janguenter.bluemap.ae2.model.DriveCellCatalog;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Ae219217DriveProfileTest {

    @Test
    void exactDriveAdapterConstantsDelegateToTheNeutralCatalog() {
        assertEquals("ae2:drive", Ae219217DriveProfile.DRIVE_BLOCK);
        assertEquals("bluemap_ae2:drive", Ae219217DriveProfile.SYNTHETIC_BLOCK_STATE);
        assertEquals(DriveCellCatalog.ids(), Ae219217DriveProfile.itemIds());
        assertEquals(23, Ae219217DriveProfile.itemIds().size());
        assertEquals(DriveCellCatalog.occupiedModels(), Ae219217DriveProfile.occupiedModels());
        assertEquals(12, Ae219217DriveProfile.occupiedModels().size());
        assertEquals(14, Ae219217DriveProfile.models().size());
        assertTrue(Ae219217DriveProfile.models().contains(DriveCellCatalog.BASE_MODEL));
        assertTrue(Ae219217DriveProfile.models().contains(DriveCellCatalog.EMPTY_CELL_MODEL));
        assertEquals(10, Ae219217DriveProfile.textures().size());
        assertEquals(10, Set.copyOf(Ae219217DriveProfile.textures()).size());
    }

    @Test
    void exactDriveCollectionsAreImmutable() {
        assertThrows(
                UnsupportedOperationException.class,
                () -> Ae219217DriveProfile.itemIds().clear()
        );
        assertThrows(
                UnsupportedOperationException.class,
                () -> Ae219217DriveProfile.occupiedModels().clear()
        );
        assertThrows(
                UnsupportedOperationException.class,
                () -> Ae219217DriveProfile.models().clear()
        );
        assertThrows(
                UnsupportedOperationException.class,
                () -> Ae219217DriveProfile.textures().clear()
        );
    }
}
