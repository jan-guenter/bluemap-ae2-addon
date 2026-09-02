/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.api;

import io.github.janguenter.bluemap.ae2.model.DriveCellCatalog;
import io.github.janguenter.bluemap.ae2.model.NativeStructuralPartCatalog;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Public, data-only registration point for soft-dependent BlueMap add-ons.
 * Registration is atomic and remains open only until BlueMap starts creating
 * resource-pack extensions.
 */
public final class Ae2ExtensionRegistry {

    private static final Set<String> RESERVED_ROUTES = Set.of(
            "ae2", "ae2-drive", "extendedae-drive", "ae2-quartz-glass",
            "ae2-crafting", "ae2-quantum-bridge", "ae2-m3-completion",
            "ae2-cable-bus-structural", "appflux", "merequester", "expandedae",
            "megacells", "advanced-ae-quantum", "advanced-ae-athena",
            "extendedae-matrix", "extendedae-planes", "appmek-drive-cells"
    );
    private static final RegistrationStore STORE = new RegistrationStore(
            NativeStructuralPartCatalog::isKnownId,
            DriveCellCatalog::isKnownItemId,
            RESERVED_ROUTES
    );

    private Ae2ExtensionRegistry() {
    }

    public static ExtensionRoute register(ExtensionDefinition definition) {
        return STORE.register(definition);
    }

    /** Host-only bridge used by the AE2 adapter after all add-on entrypoints ran. */
    public static final class Host {

        private static final String AE2_ADAPTER =
                "io.github.janguenter.bluemap.ae2.adapter.bluemap523.BlueMap523Adapter";
        private static final String M45_ADAPTER =
                "io.github.janguenter.bluemap.ae2.adapter.bluemap523.M45Adapter";
        private static final String CABLE_BUS_RENDERER =
                "io.github.janguenter.bluemap.ae2.adapter.bluemap523.CableBusRenderer";
        private static final String DRIVE_ROUTE_ACCESS =
                "io.github.janguenter.bluemap.ae2.adapter.bluemap523."
                        + "ExtensionDriveCellRouteAccess";

        private Host() {
        }

        /**
         * Issues an opaque capability only to the exact bundled host classes.
         * A dependent add-on cannot freeze registration or disable a peer route.
         */
        public static HostAccess acquireAccess() {
            Class<?> caller = StackWalker.getInstance(
                    StackWalker.Option.RETAIN_CLASS_REFERENCE
            ).getCallerClass();
            if (caller.getClassLoader() != Ae2ExtensionRegistry.class.getClassLoader()) {
                throw new SecurityException("AE2 registry host class loader mismatch");
            }
            return switch (caller.getName()) {
                case AE2_ADAPTER, M45_ADAPTER ->
                        new HostAccess(true, false);
                case CABLE_BUS_RENDERER, DRIVE_ROUTE_ACCESS ->
                        new HostAccess(false, true);
                default -> throw new SecurityException("not an AE2 registry host");
            };
        }

        public static void freezeForResourceRendering(HostAccess access) {
            requireAccess(access, true);
            STORE.freeze();
        }

        public static boolean frozen() {
            return STORE.frozen();
        }

        public static Optional<RegisteredPart> part(String partId) {
            RegistrationStore.RegisteredPart registered =
                    STORE.snapshot().parts().get(partId);
            return registered == null ? Optional.empty() : Optional.of(new RegisteredPart(
                    registered.routeId(), registered.definition()
            ));
        }

        public static Optional<RegisteredCell> nativeDriveCell(String itemId) {
            RegistrationStore.RegisteredCell registered =
                    STORE.snapshot().cells().get(itemId);
            return registered == null ? Optional.empty() : Optional.of(new RegisteredCell(
                    registered.routeId(), registered.definition()
            ));
        }

        public static List<RegisteredPart> parts() {
            return STORE.snapshot().partDefinitions().stream()
                    .map(part -> new RegisteredPart(part.routeId(), part.definition()))
                    .toList();
        }

        public static Set<String> partIds() {
            return STORE.snapshot().partIds();
        }

        public static boolean routeActive(String routeId) {
            RegistrationStore.Route route = STORE.snapshot().routes().get(routeId);
            return route != null && route.state() == ExtensionRouteState.ACTIVE;
        }

        public static boolean routeRegistered(String routeId) {
            return STORE.snapshot().routes().containsKey(routeId);
        }

        public static void disableRoute(String routeId, HostAccess access) {
            requireAccess(access, false);
            RegistrationStore.Route route = STORE.snapshot().routes().get(routeId);
            if (route != null) {
                route.disable();
            }
        }

        private static void requireAccess(HostAccess access, boolean freeze) {
            Objects.requireNonNull(access, "access");
            if (freeze ? !access.freeze() : !access.disable()) {
                throw new SecurityException("AE2 registry host capability mismatch");
            }
        }
    }

    /** Opaque authority held only by the bundled AE2 adapter. */
    public static final class HostAccess {

        private final boolean freeze;
        private final boolean disable;

        private HostAccess(boolean freeze, boolean disable) {
            this.freeze = freeze;
            this.disable = disable;
        }

        private boolean freeze() {
            return freeze;
        }

        private boolean disable() {
            return disable;
        }
    }

    /** Frozen public part data paired with its owning route. */
    public record RegisteredPart(String routeId, CableBusPartDefinition definition) {
    }

    /** Frozen public native-Drive cell data paired with its owning route. */
    public record RegisteredCell(String routeId, NativeDriveCellDefinition definition) {
    }
}
