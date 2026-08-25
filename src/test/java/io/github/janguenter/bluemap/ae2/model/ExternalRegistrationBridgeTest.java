/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.model;

import io.github.janguenter.bluemap.ae2.api.Ae2ExtensionRegistry;
import io.github.janguenter.bluemap.ae2.api.CableBusPartDefinition;
import io.github.janguenter.bluemap.ae2.api.CableBusPartKind;
import io.github.janguenter.bluemap.ae2.api.NativeDriveCellDefinition;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExternalRegistrationBridgeTest {

    @Test
    void convertsPublicP2pDataWithoutAddingExecutableHooks() {
        NativeStructuralPartCatalog.Definition definition =
                NativeStructuralPartCatalog.external(
                        new Ae2ExtensionRegistry.RegisteredPart(
                                "arseng-2.1.1-beta",
                                new CableBusPartDefinition(
                                        "arseng:source_p2p_tunnel",
                                        CableBusPartKind.P2P,
                                        1,
                                        2D,
                                        14D,
                                        List.of(
                                                "ae2:part/p2p/p2p_tunnel_status_off",
                                                "ae2:part/p2p/p2p_tunnel_frequency",
                                                "arseng:part/source_p2p_tunnel"
                                        )
                                )
                        )
                );

        assertEquals(NativeStructuralPartCatalog.Kind.P2P, definition.kind());
        assertFalse(definition.persistedSpin());
        assertEquals("arseng-2.1.1-beta", definition.extensionRouteId());
        assertEquals(3, definition.modelPaths().size());
    }

    @Test
    void resolvesAndDisablesOnlyTheExternalDriveRoute() {
        String routeId = "arseng-2.1.1-beta";
        Ae2ExtensionRegistry.RegisteredCell registered =
                new Ae2ExtensionRegistry.RegisteredCell(
                        routeId,
                        new NativeDriveCellDefinition(
                                "arseng:source_storage_cell_1k",
                                "arseng:block/drive/cells/1k_source_cell"
                        )
                );
        AtomicInteger disabled = new AtomicInteger();
        DriveCellRouteAccess active = new DriveCellRouteAccess() {
            @Override
            public boolean isActive(DriveCellOwner owner) {
                return false;
            }

            @Override
            public boolean isActive(String candidate) {
                return routeId.equals(candidate);
            }

            @Override
            public void disable(String candidate) {
                if (routeId.equals(candidate)) {
                    disabled.incrementAndGet();
                }
            }
        };

        DriveCellDefinition definition = DriveCellCatalog.resolveExternal(
                registered,
                active
        ).orElseThrow();
        assertEquals(DriveCellOwner.EXTERNAL, definition.owner());
        assertEquals(routeId, definition.externalRouteId());
        assertTrue(definition.requiresExtensionRoute());
        assertEquals(0, disabled.get());

        DriveCellRouteAccess broken = new DriveCellRouteAccess() {
            @Override
            public boolean isActive(DriveCellOwner owner) {
                return false;
            }

            @Override
            public boolean isActive(String candidate) {
                throw new IllegalStateException("injected route-state failure");
            }

            @Override
            public void disable(String candidate) {
                disabled.incrementAndGet();
            }
        };
        assertTrue(DriveCellCatalog.resolveExternal(registered, broken).isEmpty());
        assertEquals(1, disabled.get());
    }
}
