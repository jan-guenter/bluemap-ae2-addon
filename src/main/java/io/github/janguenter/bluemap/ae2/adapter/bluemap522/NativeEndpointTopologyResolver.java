/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.adapter.bluemap522;

import de.bluecolored.bluemap.core.util.Key;
import de.bluecolored.bluemap.core.world.BlockEntity;
import de.bluecolored.bluemap.core.world.BlockState;
import de.bluecolored.bluemap.core.world.block.ExtendedBlock;
import io.github.janguenter.bluemap.ae2.model.Direction6;
import io.github.janguenter.bluemap.ae2.model.NativeEndpointCatalog;
import io.github.janguenter.bluemap.ae2.model.QuantumBridgeSnapshot;
import io.github.janguenter.bluemap.ae2.model.SpatialPylonSnapshot;
import io.github.janguenter.bluemap.ae2.profile.Ae219217NativeStructuralProfile;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/** Exact persisted structural side exposure for the closed native endpoint catalog. */
final class NativeEndpointTopologyResolver {

    private static final Set<String> BOOLEANS = Set.of("false", "true");
    private static final Set<String> SPINS = Set.of("0", "1", "2", "3");
    private static final Set<String> WIRELESS_STATES = Set.of(
            "off", "on", "has_channel"
    );
    private static final Set<String> PUSH_DIRECTIONS = Set.of(
            "all", "down", "up", "north", "south", "west", "east"
    );

    private NativeEndpointTopologyResolver() {
    }

    static Status resolve(
            ExtendedBlock endpoint,
            NativeEndpointCatalog.Definition definition,
            Direction6 endpointSide
    ) {
        if (endpoint == null || definition == null || endpointSide == null) {
            return Status.UNKNOWN;
        }
        BlockState state = endpoint.getBlockState();
        if (state == null || BlockState.MISSING.equals(state)
                || !definition.blockId().equals(state.getId().getFormatted())
                || !hasExpectedBlockEntity(endpoint, definition.blockEntityId())) {
            return Status.UNKNOWN;
        }
        if (!matchesStateSchema(
                state,
                Ae219217NativeStructuralProfile.endpointStateSchema(
                        definition.blockId()
                )
        )) {
            return Status.UNKNOWN;
        }

        return switch (definition.sidePolicy()) {
            case ALL -> Status.CONNECTED;
            case BACK -> orientedSide(
                    definition.blockId(),
                    state,
                    endpointSide,
                    OrientedRule.BACK_ONLY
            );
            case NO_FRONT -> orientedSide(
                    definition.blockId(),
                    state,
                    endpointSide,
                    OrientedRule.EXCEPT_FRONT
            );
            case FRONT_BACK -> orientedSide(
                    definition.blockId(),
                    state,
                    endpointSide,
                    OrientedRule.FRONT_BACK
            );
            case FORMED_CRAFTING -> crafting(state);
            case FORMED_QUANTUM -> quantum(endpoint, state, endpointSide);
            case VALID_STRAIGHT_PYLON -> spatialPylon(endpoint, state);
            case PUSH_DIRECTION -> patternProvider(state, endpointSide);
        };
    }

    private static Status orientedSide(
            String blockId,
            BlockState state,
            Direction6 endpointSide,
            OrientedRule rule
    ) {
        if (!isExactOrientedState(blockId, state)) {
            return Status.UNKNOWN;
        }
        Direction6 front = parseDirection(state.getProperties().get("facing"));
        if (front == null) {
            return Status.UNKNOWN;
        }
        boolean exposed = switch (rule) {
            case BACK_ONLY -> endpointSide == front.opposite();
            case EXCEPT_FRONT -> endpointSide != front;
            case FRONT_BACK -> endpointSide == front || endpointSide == front.opposite();
        };
        return exposed ? Status.CONNECTED : Status.DISCONNECTED;
    }

    private static boolean isExactOrientedState(String id, BlockState state) {
        Map<String, String> properties = state.getProperties();
        return switch (id) {
            case "ae2:wireless_access_point" -> properties.keySet().equals(
                    Set.of("facing", "state", "waterlogged")
            ) && validDirection(properties.get("facing"))
                    && WIRELESS_STATES.contains(properties.get("state"))
                    && BOOLEANS.contains(properties.get("waterlogged"));
            case "ae2:crystal_resonance_generator" -> properties.keySet().equals(
                    Set.of("facing", "waterlogged")
            ) && validDirection(properties.get("facing"))
                    && BOOLEANS.contains(properties.get("waterlogged"));
            case "ae2:inscriber" -> properties.keySet().equals(
                    Set.of("facing", "spin", "waterlogged")
            ) && validDirection(properties.get("facing"))
                    && SPINS.contains(properties.get("spin"))
                    && BOOLEANS.contains(properties.get("waterlogged"));
            case "ae2:charger", "ae2:drive" -> properties.keySet().equals(
                    Set.of("facing", "spin")
            ) && validDirection(properties.get("facing"))
                    && SPINS.contains(properties.get("spin"));
            case "ae2:growth_accelerator" -> properties.keySet().equals(
                    Set.of("facing", "powered")
            ) && validDirection(properties.get("facing"))
                    && BOOLEANS.contains(properties.get("powered"));
            default -> false;
        };
    }

    private static Status crafting(BlockState state) {
        if (!Ae2ResourceExtension.isExactCraftingNeighborState(state)) {
            return Status.UNKNOWN;
        }
        return Ae2ResourceExtension.isExactFormedCraftingState(state)
                ? Status.CONNECTED : Status.DISCONNECTED;
    }

    private static Status quantum(
            ExtendedBlock endpoint,
            BlockState state,
            Direction6 endpointSide
    ) {
        if (!Ae2ResourceExtension.isExactQuantumBridgeState(state)) {
            return Status.UNKNOWN;
        }
        if (!Ae2ResourceExtension.isExactFormedQuantumBridgeState(state)) {
            return Status.DISCONNECTED;
        }
        QuantumBridgeTopologyResolver.Result topology =
                QuantumBridgeTopologyResolver.resolve(endpoint, state);
        if (!topology.supported()) {
            return Status.UNKNOWN;
        }
        QuantumBridgeSnapshot snapshot = topology.snapshot();
        boolean exposed = switch (snapshot.role()) {
            case EDGE_RING -> true;
            // These sides are occupied by required bridge members in every
            // complete 3x3. Replacing one with a cable would unform the bridge.
            case LINK, CORNER_RING -> false;
        };
        return exposed ? Status.CONNECTED : Status.DISCONNECTED;
    }

    private static Status spatialPylon(ExtendedBlock endpoint, BlockState state) {
        if (M3CompletionRenderer.exactKind(state)
                != io.github.janguenter.bluemap.ae2.model.M3CompletionBlockKind
                        .SPATIAL_PYLON) {
            return Status.UNKNOWN;
        }
        EnumSet<Direction6> neighbors = EnumSet.noneOf(Direction6.class);
        for (Direction6 direction : Direction6.values()) {
            ExtendedBlock adjacent = atOffset(endpoint, direction);
            BlockState adjacentState = adjacent.getBlockState();
            if (BlockState.MISSING.equals(adjacentState)) {
                return Status.UNKNOWN;
            }
            if (!"ae2:spatial_pylon".equals(adjacentState.getId().getFormatted())) {
                continue;
            }
            if (M3CompletionRenderer.exactKind(adjacentState)
                    != io.github.janguenter.bluemap.ae2.model.M3CompletionBlockKind
                            .SPATIAL_PYLON
                    || !hasExpectedBlockEntity(adjacent, "ae2:spatial_pylon")) {
                return Status.UNKNOWN;
            }
            neighbors.add(direction);
        }
        SpatialPylonSnapshot snapshot = SpatialPylonSnapshot.infer(neighbors).orElse(null);
        if (snapshot == null || !snapshot.formed()) {
            return Status.DISCONNECTED;
        }
        SpatialPylonTopologyResolver.Status status =
                SpatialPylonTopologyResolver.resolve(endpoint, snapshot.axis());
        return switch (status) {
            case STRAIGHT -> Status.CONNECTED;
            case BRANCHED -> Status.DISCONNECTED;
            case INCOMPLETE -> Status.UNKNOWN;
        };
    }

    private static Status patternProvider(BlockState state, Direction6 endpointSide) {
        Map<String, String> properties = state.getProperties();
        if (!properties.keySet().equals(Set.of("push_direction"))
                || !PUSH_DIRECTIONS.contains(properties.get("push_direction"))) {
            return Status.UNKNOWN;
        }
        String pushDirection = properties.get("push_direction");
        return "all".equals(pushDirection)
                || endpointSide != parseDirection(pushDirection)
                ? Status.CONNECTED : Status.DISCONNECTED;
    }

    private static ExtendedBlock atOffset(ExtendedBlock block, Direction6 direction) {
        ExtendedBlock result = block.copy();
        result.set(
                block.getX() + direction.stepX(),
                block.getY() + direction.stepY(),
                block.getZ() + direction.stepZ()
        );
        return result;
    }

    private static boolean hasExpectedBlockEntity(ExtendedBlock block, String expectedId) {
        BlockEntity entity = block.getBlockEntity();
        Key entityId = entity == null ? null : entity.getId();
        return entityId != null && expectedId.equals(entityId.getFormatted());
    }

    private static boolean matchesStateSchema(
            BlockState state,
            Map<String, java.util.List<String>> schema
    ) {
        if (schema == null || !state.getProperties().keySet().equals(schema.keySet())) {
            return false;
        }
        for (Map.Entry<String, java.util.List<String>> entry : schema.entrySet()) {
            if (!entry.getValue().contains(state.getProperties().get(entry.getKey()))) {
                return false;
            }
        }
        return true;
    }

    private static boolean validDirection(String value) {
        return parseDirection(value) != null;
    }

    private static Direction6 parseDirection(String value) {
        if (value == null || "all".equals(value)) {
            return null;
        }
        try {
            return Direction6.valueOf(value.toUpperCase(java.util.Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    enum Status {
        CONNECTED,
        DISCONNECTED,
        UNKNOWN
    }

    private enum OrientedRule {
        BACK_ONLY,
        EXCEPT_FRONT,
        FRONT_BACK
    }
}
