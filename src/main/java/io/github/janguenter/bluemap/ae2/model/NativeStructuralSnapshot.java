/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.model;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/** Immutable complete structural state for one native AE2 cable bus. */
public record NativeStructuralSnapshot(
        CableDefinition cable,
        Map<Direction6, Connection> connections,
        Map<Direction6, FacePartSnapshot> faceParts,
        Map<Direction6, FacadeSnapshot> facades,
        Map<Direction6, Integer> planeConnectionMasks
) {

    public NativeStructuralSnapshot {
        connections = immutableDirections(connections, "connection");
        faceParts = immutableDirections(faceParts, "face-part");
        facades = immutableDirections(facades, "facade");
        planeConnectionMasks = immutableDirections(
                planeConnectionMasks,
                "plane-connection-mask"
        );
        if (cable == null && faceParts.isEmpty()) {
            throw new IllegalArgumentException("a structural cable bus must contain a center or part");
        }
        for (Map.Entry<Direction6, Integer> entry : planeConnectionMasks.entrySet()) {
            if (entry.getValue() < 0 || entry.getValue() > 15) {
                throw new IllegalArgumentException("plane connection mask must be in [0, 15]");
            }
            FacePartSnapshot part = faceParts.get(entry.getKey());
            if (part == null || NativeStructuralPartCatalog.require(part.id()).kind()
                    != NativeStructuralPartCatalog.Kind.PLANE) {
                throw new IllegalArgumentException("plane mask without a plane part");
            }
        }
    }

    public static NativeStructuralSnapshot decoded(
            CableDefinition cable,
            Map<Direction6, FacePartSnapshot> faceParts,
            Map<Direction6, FacadeSnapshot> facades
    ) {
        return new NativeStructuralSnapshot(cable, Map.of(), faceParts, facades, Map.of());
    }

    public Optional<CableDefinition> center() {
        return Optional.ofNullable(cable);
    }

    public boolean hasCenter() {
        return cable != null;
    }

    public CableColor renderColor() {
        return cable == null ? CableColor.TRANSPARENT : cable.color();
    }

    public boolean hasFacePart(Direction6 direction) {
        return faceParts.containsKey(Objects.requireNonNull(direction, "direction"));
    }

    public boolean hasFacade(Direction6 direction) {
        return facades.containsKey(Objects.requireNonNull(direction, "direction"));
    }

    public NativeStructuralSnapshot withConnection(
            Direction6 direction,
            Connection connection
    ) {
        Objects.requireNonNull(direction, "direction");
        EnumMap<Direction6, Connection> updated = new EnumMap<>(Direction6.class);
        updated.putAll(connections);
        if (connection == null) {
            updated.remove(direction);
        } else {
            updated.put(direction, connection);
        }
        return new NativeStructuralSnapshot(
                cable,
                updated,
                faceParts,
                facades,
                planeConnectionMasks
        );
    }

    public NativeStructuralSnapshot withPlaneConnectionMask(
            Direction6 direction,
            int mask
    ) {
        if (mask < 0 || mask > 15) {
            throw new IllegalArgumentException("plane connection mask must be in [0, 15]");
        }
        EnumMap<Direction6, Integer> updated = new EnumMap<>(Direction6.class);
        updated.putAll(planeConnectionMasks);
        updated.put(Objects.requireNonNull(direction, "direction"), mask);
        return new NativeStructuralSnapshot(cable, connections, faceParts, facades, updated);
    }

    private static <T> Map<Direction6, T> immutableDirections(
            Map<Direction6, T> source,
            String label
    ) {
        Objects.requireNonNull(source, label);
        EnumMap<Direction6, T> copy = new EnumMap<>(Direction6.class);
        copy.putAll(source);
        if (copy.containsKey(null) || copy.containsValue(null)) {
            throw new IllegalArgumentException(label + " entries must be non-null");
        }
        return Collections.unmodifiableMap(copy);
    }

    public enum ConnectionKind {
        CABLE_BUS,
        NATIVE_ENDPOINT
    }

    public record Connection(
            ConnectionKind kind,
            CableFamily declaredFamily,
            CableFamily effectiveFamily,
            String endpointId,
            boolean collar
    ) {

        public Connection {
            Objects.requireNonNull(kind, "kind");
            Objects.requireNonNull(declaredFamily, "declaredFamily");
            Objects.requireNonNull(effectiveFamily, "effectiveFamily");
            if (kind == ConnectionKind.CABLE_BUS && (endpointId != null || collar)) {
                throw new IllegalArgumentException("cable-bus connections have no endpoint collar");
            }
            if (kind == ConnectionKind.NATIVE_ENDPOINT && endpointId == null) {
                throw new IllegalArgumentException("native endpoint connection requires an ID");
            }
        }

        public static Connection cableBus(
                CableFamily adjacentFamily,
                CableFamily effectiveFamily
        ) {
            return new Connection(
                    ConnectionKind.CABLE_BUS,
                    adjacentFamily,
                    effectiveFamily,
                    null,
                    false
            );
        }

        public static Connection endpoint(
                String endpointId,
                CableFamily declaredFamily,
                CableFamily effectiveFamily
        ) {
            return new Connection(
                    ConnectionKind.NATIVE_ENDPOINT,
                    declaredFamily,
                    effectiveFamily,
                    endpointId,
                    effectiveFamily != CableFamily.GLASS && !effectiveFamily.isDense()
            );
        }
    }
}
