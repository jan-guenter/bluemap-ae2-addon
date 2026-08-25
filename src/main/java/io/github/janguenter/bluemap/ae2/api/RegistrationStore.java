/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.api;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

final class RegistrationStore {

    private static final int MAX_ROUTES = 64;
    private static final int MAX_PARTS = 256;
    private static final int MAX_CELLS = 512;
    private static final Set<String> RESERVED_DRIVE_OWNERS = Set.of(
            "ae2", "extendedae", "appflux", "megacells", "appmek"
    );

    private final Predicate<String> reservedPartId;
    private final Predicate<String> reservedItemId;
    private final Set<String> reservedRouteIds;
    private final Map<String, Route> routes = new LinkedHashMap<>();
    private final Map<String, RegisteredPart> parts = new LinkedHashMap<>();
    private final Map<String, RegisteredCell> cells = new LinkedHashMap<>();
    private volatile Snapshot frozenSnapshot = Snapshot.empty();
    private volatile boolean frozen;

    RegistrationStore(
            Predicate<String> reservedPartId,
            Predicate<String> reservedItemId,
            Set<String> reservedRouteIds
    ) {
        this.reservedPartId = Objects.requireNonNull(reservedPartId, "reservedPartId");
        this.reservedItemId = Objects.requireNonNull(reservedItemId, "reservedItemId");
        this.reservedRouteIds = Set.copyOf(reservedRouteIds);
    }

    synchronized ExtensionRoute register(ExtensionDefinition definition) {
        Objects.requireNonNull(definition, "definition");
        if (frozen) {
            throw new IllegalStateException("AE2 extension registration is frozen");
        }
        if (reservedRouteIds.contains(definition.routeId())
                || routes.containsKey(definition.routeId())) {
            throw new IllegalArgumentException("duplicate or reserved route ID");
        }
        if (routes.size() == MAX_ROUTES
                || parts.size() + definition.cableBusParts().size() > MAX_PARTS
                || cells.size() + definition.nativeDriveCells().size() > MAX_CELLS) {
            throw new IllegalStateException("AE2 extension registration limit reached");
        }

        LinkedHashMap<String, RegisteredPart> newParts = new LinkedHashMap<>();
        for (CableBusPartDefinition part : definition.cableBusParts()) {
            validateOwner(definition.ownerNamespace(), part.partId(), "part");
            validatePartModels(definition.ownerNamespace(), part);
            if (reservedPartId.test(part.partId()) || parts.containsKey(part.partId())
                    || newParts.put(part.partId(), new RegisteredPart(
                            definition.routeId(), part
                    )) != null) {
                throw new IllegalArgumentException("duplicate or reserved part ID");
            }
        }

        LinkedHashMap<String, RegisteredCell> newCells = new LinkedHashMap<>();
        if (!definition.nativeDriveCells().isEmpty()
                && RESERVED_DRIVE_OWNERS.contains(definition.ownerNamespace())) {
            throw new IllegalArgumentException("reserved native Drive owner namespace");
        }
        for (NativeDriveCellDefinition cell : definition.nativeDriveCells()) {
            validateOwner(definition.ownerNamespace(), cell.itemId(), "item");
            validateOwner(definition.ownerNamespace(), cell.modelId(), "cell model");
            String expectedPrefix = definition.ownerNamespace() + ":block/drive/";
            if (!cell.modelId().startsWith(expectedPrefix)
                    || cell.modelId().length() == expectedPrefix.length()) {
                throw new IllegalArgumentException("cell model is outside the owner Drive path");
            }
            if (reservedItemId.test(cell.itemId()) || cells.containsKey(cell.itemId())
                    || newCells.put(cell.itemId(), new RegisteredCell(
                            definition.routeId(), cell
                    )) != null) {
                throw new IllegalArgumentException("duplicate or reserved Drive item ID");
            }
        }

        Route route = new Route(definition.routeId());
        routes.put(definition.routeId(), route);
        parts.putAll(newParts);
        cells.putAll(newCells);
        return new ExtensionRoute(route);
    }

    synchronized void freeze() {
        if (frozen) {
            return;
        }
        frozenSnapshot = new Snapshot(
                Map.copyOf(routes),
                Map.copyOf(parts),
                Map.copyOf(cells)
        );
        frozen = true;
    }

    boolean frozen() {
        return frozen;
    }

    Snapshot snapshot() {
        return frozenSnapshot;
    }

    private static void validateOwner(String owner, String id, String kind) {
        if (!owner.equals(Identifiers.namespace(id))) {
            throw new IllegalArgumentException(kind + " namespace does not match route owner");
        }
    }

    private static void validatePartModels(
            String owner,
            CableBusPartDefinition part
    ) {
        int firstOwnerLayer = part.kind() == CableBusPartKind.P2P ? 2 : 0;
        for (int index = firstOwnerLayer; index < part.modelPaths().size(); index++) {
            validateOwner(owner, part.modelPaths().get(index), "part model");
        }
    }

    record RegisteredPart(String routeId, CableBusPartDefinition definition) {
    }

    record RegisteredCell(String routeId, NativeDriveCellDefinition definition) {
    }

    record Snapshot(
            Map<String, Route> routes,
            Map<String, RegisteredPart> parts,
            Map<String, RegisteredCell> cells
    ) {

        Snapshot {
            routes = Map.copyOf(routes);
            parts = Map.copyOf(parts);
            cells = Map.copyOf(cells);
        }

        static Snapshot empty() {
            return new Snapshot(Map.of(), Map.of(), Map.of());
        }

        List<RegisteredPart> partDefinitions() {
            return List.copyOf(parts.values());
        }

        Set<String> partIds() {
            return Set.copyOf(new LinkedHashSet<>(parts.keySet()));
        }
    }

    static final class Route {

        private final String routeId;
        private final AtomicReference<ExtensionRouteState> state =
                new AtomicReference<>(ExtensionRouteState.INACTIVE);

        Route(String routeId) {
            this.routeId = routeId;
        }

        String routeId() {
            return routeId;
        }

        ExtensionRouteState state() {
            return state.get();
        }

        void activate() {
            state.updateAndGet(current -> current == ExtensionRouteState.DISABLED
                    ? current : ExtensionRouteState.ACTIVE);
        }

        void deactivate() {
            state.updateAndGet(current -> current == ExtensionRouteState.DISABLED
                    ? current : ExtensionRouteState.INACTIVE);
        }

        void disable() {
            state.set(ExtensionRouteState.DISABLED);
        }
    }
}
