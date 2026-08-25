/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.api;

import java.util.Objects;

/**
 * State handle returned to the add-on that owns a registered data bundle.
 * No renderer, resource callback, or other executable hook crosses this API.
 */
public final class ExtensionRoute {

    private final RegistrationStore.Route route;

    ExtensionRoute(RegistrationStore.Route route) {
        this.route = Objects.requireNonNull(route, "route");
    }

    public String routeId() {
        return route.routeId();
    }

    public ExtensionRouteState state() {
        return route.state();
    }

    /** Marks this route active after its owner has passed its own exact resource gate. */
    public void activate() {
        route.activate();
    }

    /** Returns a non-disabled route to its fail-closed inactive state. */
    public void deactivate() {
        route.deactivate();
    }

    /** Permanently disables this route for the current JVM. */
    public void disable() {
        route.disable();
    }
}
