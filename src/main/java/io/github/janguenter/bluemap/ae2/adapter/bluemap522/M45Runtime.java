/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.adapter.bluemap522;

import io.github.janguenter.bluemap.ae2.activation.ExtensionRouteActivation;

import java.util.List;
import java.util.Map;

/** Shared activation state for exact ATM 1.2.0 extension routes. */
final class M45Runtime {

    static final String APPFLUX = "appflux";
    static final String ME_REQUESTER = "merequester";
    static final String EXPANDED_AE = "expandedae";
    static final String MEGA_CELLS = "megacells";
    static final String ADVANCED_QUANTUM = "advanced-ae-quantum";
    static final String ADVANCED_ATHENA = "advanced-ae-athena";
    static final String EXTENDED_MATRIX = "extendedae-matrix";
    static final String EXTENDED_PLANES = "extendedae-planes";
    static final String APPMEK_DRIVE_CELLS = "appmek-drive-cells";

    private final Map<String, ExtensionRouteActivation> routes;

    M45Runtime() {
        this.routes = Map.ofEntries(
                routeEntry(APPFLUX),
                routeEntry(ME_REQUESTER),
                routeEntry(EXPANDED_AE),
                routeEntry(MEGA_CELLS),
                routeEntry(ADVANCED_QUANTUM),
                routeEntry(ADVANCED_ATHENA),
                routeEntry(EXTENDED_MATRIX),
                routeEntry(EXTENDED_PLANES),
                routeEntry(APPMEK_DRIVE_CELLS)
        );
    }

    ExtensionRouteActivation route(String routeId) {
        ExtensionRouteActivation route = routes.get(routeId);
        if (route == null) {
            throw new IllegalArgumentException("unknown extension route " + routeId);
        }
        return route;
    }

    boolean active(String routeId) {
        return route(routeId).isActive();
    }

    boolean contains(String routeId) {
        return routes.containsKey(routeId);
    }

    List<ExtensionRouteActivation> routes() {
        return List.copyOf(routes.values());
    }

    void blockActiveRoutesIfCoreInactive(boolean coreActive) {
        if (coreActive) {
            return;
        }
        routes.values().forEach(route -> {
            if (route.isActive()) {
                route.inactive(
                        ExtensionRouteActivation.Reason.BLOCKED_BY_CORE,
                        "ae2-core-inactive"
                );
            }
        });
    }

    void blockExtendedPlanesIfNativeStructuralInactive(boolean nativeStructuralActive) {
        if (nativeStructuralActive) {
            return;
        }
        ExtensionRouteActivation route = route(EXTENDED_PLANES);
        if (route.isActive()) {
            route.inactive(
                    ExtensionRouteActivation.Reason.BLOCKED_BY_CORE,
                    "native-structural-core-inactive"
            );
        }
    }

    void blockAppMekDriveCellsIfNativeDriveInactive(boolean nativeDriveActive) {
        if (nativeDriveActive) {
            return;
        }
        ExtensionRouteActivation route = route(APPMEK_DRIVE_CELLS);
        if (route.isActive()) {
            route.inactive(
                    ExtensionRouteActivation.Reason.BLOCKED_BY_CORE,
                    "native-drive-core-inactive"
            );
        }
    }

    private static Map.Entry<String, ExtensionRouteActivation> routeEntry(String routeId) {
        return Map.entry(routeId, new ExtensionRouteActivation(routeId));
    }
}
