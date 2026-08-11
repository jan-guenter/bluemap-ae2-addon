/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.profile.appflux;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AppFlux215ProfileTest {

    @Test
    void exactArtifactAndResourceClosureAreFailClosed() {
        assertEquals(19, AppFlux215Profile.requiredResources().size());
        assertEquals(
                10_540L,
                AppFlux215Profile.requiredResourceSizes().values().stream()
                        .mapToLong(Long::longValue)
                        .sum()
        );
        assertTrue(AppFlux215Profile.acceptsArtifact(
                AppFlux215Profile.JAR_BYTES,
                AppFlux215Profile.JAR_SHA256
        ));
        assertFalse(AppFlux215Profile.acceptsArtifact(
                AppFlux215Profile.JAR_BYTES + 1,
                AppFlux215Profile.JAR_SHA256
        ));
        assertFalse(AppFlux215Profile.acceptsArtifact(
                AppFlux215Profile.JAR_BYTES,
                AppFlux215Profile.JAR_SHA256.toUpperCase(java.util.Locale.ROOT)
        ));
        assertThrows(
                IllegalArgumentException.class,
                () -> AppFlux215Profile.requireExactArtifact(
                        AppFlux215Profile.JAR_BYTES,
                        "0".repeat(64)
                )
        );
    }

    @Test
    void genericDriveAndPartCatalogIsClosed() {
        assertEquals(2, AppFlux215Catalog.stockBlockModels().size());
        assertEquals(20, AppFlux215Catalog.driveCellModels().size());
        assertEquals(10, AppFlux215Catalog.normalCellIds().size());
        assertEquals(10, AppFlux215Catalog.portableCellIds().size());
        assertEquals(10, Set.copyOf(AppFlux215Catalog.driveCellModels().values()).size());
        assertEquals(
                "appflux:block/drive/fe_256m_cell",
                AppFlux215Catalog.driveCellModels().get("appflux:fe_256m_cell")
        );
        assertEquals(
                "appflux:block/drive/fe_256m_cell",
                AppFlux215Catalog.driveCellModels().get(
                        "appflux:fe_256m_portable_cell"
                )
        );
        assertEquals(
                Set.of("fast"),
                AppFlux215Catalog.fluxAccessorPart().visuallyIgnoredNbtKeys()
        );
        assertFalse(AppFlux215Catalog.fluxAccessorPart().supportsSpin());
        assertFalse(AppFlux215Catalog.FAST_AFFECTS_WORLD_GEOMETRY);
    }

    @Test
    void exposedContractsAreImmutable() {
        assertThrows(
                UnsupportedOperationException.class,
                () -> AppFlux215Profile.requiredResources().clear()
        );
        assertThrows(
                UnsupportedOperationException.class,
                () -> AppFlux215Catalog.driveCellModels().clear()
        );
        assertThrows(
                UnsupportedOperationException.class,
                () -> AppFlux215Catalog.normalCellIds().clear()
        );
    }
}
