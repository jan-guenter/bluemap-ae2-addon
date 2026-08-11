/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.model;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

/** Immutable cable identity and the exact rendered type of each cable neighbor. */
public record CableBusSnapshot(
        CableDefinition cable,
        Map<Direction6, CableFamily> connectionTypes,
        Map<Direction6, FacePartSnapshot> faceParts,
        Map<Direction6, FacadeSnapshot> facades
) {

    public static final String FLUIX_GLASS_CABLE = Ae2CableCatalog.FLUIX_GLASS_CABLE;
    public static final int MAX_CONNECTION_MASK = (1 << Direction6.values().length) - 1;

    public CableBusSnapshot {
        Objects.requireNonNull(cable, "cable");
        Objects.requireNonNull(connectionTypes, "connectionTypes");
        Objects.requireNonNull(faceParts, "faceParts");
        Objects.requireNonNull(facades, "facades");
        connectionTypes = immutableDirections(connectionTypes, "connection");
        faceParts = immutableDirections(faceParts, "face-part");
        facades = immutableDirections(facades, "facade");
    }

    /** Compatibility constructor for the M0/M1 cable-only model and tests. */
    public CableBusSnapshot(
            CableDefinition cable,
            Map<Direction6, CableFamily> connectionTypes
    ) {
        this(cable, connectionTypes, Map.of(), Map.of());
    }

    public static CableBusSnapshot isolated(CableDefinition cable) {
        return new CableBusSnapshot(cable, Map.of(), Map.of(), Map.of());
    }

    public static CableBusSnapshot fluixGlassCable() {
        return isolated(Ae2CableCatalog.require(FLUIX_GLASS_CABLE));
    }

    public String centerPartId() {
        return cable.id();
    }

    public boolean connects(Direction6 direction) {
        Objects.requireNonNull(direction, "direction");
        return connectionTypes.containsKey(direction);
    }

    public CableFamily connectionType(Direction6 direction) {
        Objects.requireNonNull(direction, "direction");
        return connectionTypes.get(direction);
    }

    public int connectionMask() {
        int mask = 0;
        for (Direction6 direction : connectionTypes.keySet()) {
            mask |= direction.maskBit();
        }
        return mask;
    }

    public CableBusSnapshot withConnection(
            Direction6 direction,
            CableFamily renderedType
    ) {
        Objects.requireNonNull(direction, "direction");
        EnumMap<Direction6, CableFamily> updated = new EnumMap<>(Direction6.class);
        updated.putAll(connectionTypes);
        if (renderedType == null) {
            updated.remove(direction);
        } else {
            updated.put(direction, renderedType);
        }
        return new CableBusSnapshot(cable, updated, faceParts, facades);
    }

    /** Compatibility helper for the original M0 same-family topology tests. */
    public CableBusSnapshot withConnection(Direction6 direction, boolean connected) {
        return withConnection(direction, connected ? cable.family() : null);
    }

    /** Compatibility helper for the original M0 mask fixtures. */
    public CableBusSnapshot withConnectionMask(int newConnectionMask) {
        validateMask(newConnectionMask);
        CableBusSnapshot result = isolated(cable);
        for (Direction6 direction : Direction6.values()) {
            if ((newConnectionMask & direction.maskBit()) != 0) {
                result = result.withConnection(direction, cable.family());
            }
        }
        return result;
    }

    public static void validateMask(int mask) {
        if (mask < 0 || mask > MAX_CONNECTION_MASK) {
            throw new IllegalArgumentException("connection mask must be in [0, 63]");
        }
    }

    public boolean hasFacePart(Direction6 direction) {
        return faceParts.containsKey(Objects.requireNonNull(direction, "direction"));
    }

    public boolean hasFacade(Direction6 direction) {
        return facades.containsKey(Objects.requireNonNull(direction, "direction"));
    }

    private static <T> Map<Direction6, T> immutableDirections(
            Map<Direction6, T> source,
            String label
    ) {
        EnumMap<Direction6, T> copy = new EnumMap<>(Direction6.class);
        copy.putAll(source);
        if (copy.containsKey(null) || copy.containsValue(null)) {
            throw new IllegalArgumentException(label + " entries must be non-null");
        }
        return Collections.unmodifiableMap(copy);
    }
}
