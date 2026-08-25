/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.api;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RegistrationStoreTest {

    private static final String ROUTE = "arseng-2.1.1-beta";

    @Test
    void registersAndFreezesTheExactArsEnergistiqueDataAtomically() {
        RegistrationStore store = store();

        ExtensionRoute route = store.register(arsDefinition());
        assertEquals(ExtensionRouteState.INACTIVE, route.state());
        assertTrue(store.snapshot().routes().isEmpty());

        route.activate();
        store.freeze();

        assertTrue(store.frozen());
        assertEquals(3, store.snapshot().parts().size());
        assertEquals(5, store.snapshot().cells().size());
        assertEquals(ExtensionRouteState.ACTIVE, route.state());
        assertThrows(IllegalStateException.class, () -> store.register(otherDefinition()));

        route.deactivate();
        assertEquals(ExtensionRouteState.INACTIVE, route.state());
        route.activate();
        route.disable();
        route.activate();
        assertEquals(ExtensionRouteState.DISABLED, route.state());
    }

    @Test
    void rejectsDuplicateAndReservedIdsWithoutLeavingAPartialRoute() {
        RegistrationStore store = store();
        ExtensionDefinition duplicatePart = new ExtensionDefinition(
                ROUTE,
                "arseng",
                List.of(sourceAcceptor(), sourceAcceptor()),
                List.of()
        );
        assertThrows(IllegalArgumentException.class, () -> store.register(duplicatePart));
        assertTrue(store.snapshot().routes().isEmpty());

        assertThrows(IllegalArgumentException.class, () -> store.register(
                new ExtensionDefinition(
                        "reserved-route",
                        "arseng",
                        List.of(sourceAcceptor()),
                        List.of()
                )
        ));
        assertThrows(IllegalArgumentException.class, () -> store.register(
                new ExtensionDefinition(
                        ROUTE,
                        "arseng",
                        List.of(new CableBusPartDefinition(
                                "arseng:reserved_part",
                                CableBusPartKind.STATIC,
                                2,
                                2D,
                                14D,
                                List.of("arseng:part/source_acceptor")
                        )),
                        List.of()
                )
        ));
        assertThrows(IllegalArgumentException.class, () -> store.register(
                new ExtensionDefinition(
                        ROUTE,
                        "arseng",
                        List.of(),
                        List.of(new NativeDriveCellDefinition(
                                "arseng:reserved_cell",
                                "arseng:block/drive/cells/1k_source_cell"
                        ))
                )
        ));

        ExtensionDefinition duplicateCell = new ExtensionDefinition(
                ROUTE,
                "arseng",
                List.of(),
                List.of(
                        new NativeDriveCellDefinition(
                                "arseng:source_storage_cell_1k",
                                "arseng:block/drive/cells/1k_source_cell"
                        ),
                        new NativeDriveCellDefinition(
                                "arseng:source_storage_cell_1k",
                                "arseng:block/drive/cells/1k_source_cell"
                        )
                )
        );
        assertThrows(IllegalArgumentException.class, () -> store.register(duplicateCell));

        ExtensionRoute route = store.register(arsDefinition());
        assertEquals(ROUTE, route.routeId());
        assertThrows(IllegalArgumentException.class, () -> store.register(arsDefinition()));
        assertThrows(IllegalArgumentException.class, () -> store.register(
                new ExtensionDefinition(
                        "arseng-peer",
                        "arseng",
                        List.of(sourceAcceptor()),
                        List.of()
                )
        ));
        assertThrows(IllegalArgumentException.class, () -> store.register(
                new ExtensionDefinition(
                        "arseng-drive-peer",
                        "arseng",
                        List.of(),
                        List.of(new NativeDriveCellDefinition(
                                "arseng:source_storage_cell_1k",
                                "arseng:block/drive/cells/1k_source_cell"
                        ))
                )
        ));
    }

    @Test
    void emptyFreezeRetainsAnEmptyCatalogAndRejectsLateRegistration() {
        RegistrationStore store = store();

        store.freeze();

        assertTrue(store.frozen());
        assertTrue(store.snapshot().routes().isEmpty());
        assertTrue(store.snapshot().parts().isEmpty());
        assertTrue(store.snapshot().cells().isEmpty());
        assertThrows(IllegalStateException.class, () -> store.register(arsDefinition()));
    }

    @Test
    void ownerNamespacesAndP2pNeutralLayersAreClosed() {
        assertThrows(IllegalArgumentException.class, () -> new CableBusPartDefinition(
                "arseng:bad_p2p",
                CableBusPartKind.P2P,
                1,
                2D,
                14D,
                List.of(
                        "arseng:part/not_the_neutral_status",
                        "ae2:part/p2p/p2p_tunnel_frequency",
                        "arseng:part/source_p2p_tunnel"
                )
        ));

        RegistrationStore store = store();
        ExtensionDefinition wrongNamespace = new ExtensionDefinition(
                ROUTE,
                "arseng",
                List.of(),
                List.of(new NativeDriveCellDefinition(
                        "other:source_storage_cell_1k",
                        "other:block/drive/cells/1k_source_cell"
                ))
        );
        assertThrows(IllegalArgumentException.class, () -> store.register(wrongNamespace));
        assertThrows(IllegalArgumentException.class, () -> store.register(
                new ExtensionDefinition(
                        "appflux-peer",
                        "appflux",
                        List.of(),
                        List.of(new NativeDriveCellDefinition(
                                "appflux:new_cell",
                                "appflux:block/drive/cells/new_cell"
                        ))
                )
        ));
        assertFalse(store.frozen());
    }

    @Test
    void disablingOneRouteDoesNotChangeAnotherRoute() {
        RegistrationStore store = store();
        ExtensionRoute first = store.register(arsDefinition());
        ExtensionRoute second = store.register(otherDefinition());
        first.activate();
        second.activate();
        store.freeze();

        first.disable();

        assertEquals(ExtensionRouteState.DISABLED, first.state());
        assertEquals(ExtensionRouteState.ACTIVE, second.state());
    }

    private static RegistrationStore store() {
        return new RegistrationStore(
                "arseng:reserved_part"::equals,
                "arseng:reserved_cell"::equals,
                Set.of("reserved-route")
        );
    }

    private static ExtensionDefinition arsDefinition() {
        return new ExtensionDefinition(
                ROUTE,
                "arseng",
                List.of(
                        sourceAcceptor(),
                        p2p("source", "arseng:part/source_p2p_tunnel"),
                        p2p("spell", "arseng:part/spell_p2p_tunnel")
                ),
                List.of("1k", "4k", "16k", "64k", "256k").stream()
                        .map(tier -> new NativeDriveCellDefinition(
                                "arseng:source_storage_cell_" + tier,
                                "arseng:block/drive/cells/" + tier + "_source_cell"
                        ))
                        .toList()
        );
    }

    private static ExtensionDefinition otherDefinition() {
        return new ExtensionDefinition(
                "other-1",
                "other",
                List.of(new CableBusPartDefinition(
                        "other:part",
                        CableBusPartKind.STATIC,
                        2,
                        2D,
                        14D,
                        List.of("other:part/model")
                )),
                List.of()
        );
    }

    private static CableBusPartDefinition sourceAcceptor() {
        return new CableBusPartDefinition(
                "arseng:cable_source_acceptor",
                CableBusPartKind.STATIC,
                2,
                2D,
                14D,
                List.of("arseng:part/source_acceptor")
        );
    }

    private static CableBusPartDefinition p2p(String name, String front) {
        return new CableBusPartDefinition(
                "arseng:" + name + "_p2p_tunnel",
                CableBusPartKind.P2P,
                1,
                2D,
                14D,
                List.of(
                        "ae2:part/p2p/p2p_tunnel_status_off",
                        "ae2:part/p2p/p2p_tunnel_frequency",
                        front
                )
        );
    }
}
