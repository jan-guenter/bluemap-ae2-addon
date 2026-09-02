/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.adapter.bluemap523;

import de.bluecolored.bluemap.core.util.Key;
import de.bluecolored.bluemap.core.world.BlockEntity;
import de.bluecolored.bluemap.core.world.BlockState;
import de.bluecolored.bluemap.core.world.block.ExtendedBlock;
import io.github.janguenter.bluemap.ae2.model.Direction6;
import io.github.janguenter.bluemap.ae2.model.QuantumBridgeSnapshot;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/** Bounded, fail-closed topology resolver shared by native quantum consumers. */
final class QuantumBridgeTopologyResolver {

    private static final Key EXPECTED_BLOCK_ENTITY = Key.parse(
            M3eQuantumBridgeResourceModels.BLOCK_ENTITY_ID
    );

    private QuantumBridgeTopologyResolver() {
    }

    static Result resolve(ExtendedBlock block, BlockState center) {
        EnumSet<Direction6> connections = EnumSet.noneOf(Direction6.class);
        Map<Direction6, ExtendedBlock> directNeighbors = new EnumMap<>(Direction6.class);
        for (Direction6 direction : Direction6.values()) {
            ExtendedBlock adjacent = neighbor(block, Offset.of(direction));
            directNeighbors.put(direction, adjacent);
            BlockState state = adjacent.getBlockState();
            if (BlockState.MISSING.equals(state)) {
                return Result.rejected(Status.NEIGHBOR_UNAVAILABLE);
            }
            if (Ae2ResourceExtension.isQuantumBridgeId(state.getId())) {
                if (!Ae2ResourceExtension.isExactFormedQuantumBridgeState(state)) {
                    return Result.rejected(Status.INVALID);
                }
                connections.add(direction);
            }
        }

        InferredRole inferred = inferRole(center, connections);
        if (inferred == null) {
            return Result.rejected(Status.INVALID);
        }
        Status complete = validateCompletePlane(block, inferred);
        if (complete != Status.SUPPORTED) {
            return Result.rejected(complete);
        }
        return Result.supported(
                new QuantumBridgeSnapshot(
                        inferred.role(),
                        connections,
                        Boolean.parseBoolean(center.getProperties().get("waterlogged"))
                ),
                directNeighbors
        );
    }

    private static InferredRole inferRole(
            BlockState center,
            Set<Direction6> connections
    ) {
        boolean link = M3eQuantumBridgeResourceModels.LINK_BLOCK.equals(
                center.getId().getFormatted()
        );
        if (link) {
            if (connections.size() != 4 || oppositePairs(connections) != 2) {
                return null;
            }
            Set<Axis> axes = axes(connections);
            if (axes.size() != 2) {
                return null;
            }
            return new InferredRole(
                    QuantumBridgeSnapshot.Role.LINK,
                    Offset.ZERO,
                    plane(axes)
            );
        }

        if (connections.size() == 2
                && oppositePairs(connections) == 0
                && axes(connections).size() == 2) {
            Offset linkOffset = Offset.ZERO;
            for (Direction6 direction : connections) {
                linkOffset = linkOffset.plus(Offset.of(direction));
            }
            return new InferredRole(
                    QuantumBridgeSnapshot.Role.CORNER_RING,
                    linkOffset,
                    plane(axes(connections))
            );
        }

        if (connections.size() == 3 && oppositePairs(connections) == 1) {
            Direction6 singleton = null;
            for (Direction6 direction : connections) {
                if (!connections.contains(direction.opposite())) {
                    if (singleton != null) {
                        return null;
                    }
                    singleton = direction;
                }
            }
            Set<Axis> axes = axes(connections);
            if (singleton == null || axes.size() != 2) {
                return null;
            }
            return new InferredRole(
                    QuantumBridgeSnapshot.Role.EDGE_RING,
                    Offset.of(singleton),
                    plane(axes)
            );
        }
        return null;
    }

    private static Status validateCompletePlane(
            ExtendedBlock block,
            InferredRole inferred
    ) {
        for (int first = -1; first <= 1; first++) {
            for (int second = -1; second <= 1; second++) {
                Offset relativeToLink = inferred.plane().offset(first, second);
                Offset relativeToCenter = inferred.linkOffset().plus(relativeToLink);
                ExtendedBlock member = neighbor(block, relativeToCenter);
                BlockState state = member.getBlockState();
                if (BlockState.MISSING.equals(state)) {
                    return Status.NEIGHBOR_UNAVAILABLE;
                }
                String expectedId = first == 0 && second == 0
                        ? M3eQuantumBridgeResourceModels.LINK_BLOCK
                        : M3eQuantumBridgeResourceModels.RING_BLOCK;
                if (!expectedId.equals(state.getId().getFormatted())
                        || !Ae2ResourceExtension.isExactFormedQuantumBridgeState(state)
                        || !hasExpectedBlockEntity(member)) {
                    return Status.INVALID;
                }
            }
        }

        for (int first = -1; first <= 1; first++) {
            for (int second = -1; second <= 1; second++) {
                Offset planePosition = inferred.plane().offset(first, second);
                for (Direction6 direction : Direction6.values()) {
                    Offset exterior = planePosition.plus(Offset.of(direction));
                    if (inferred.plane().contains(exterior)) {
                        continue;
                    }
                    BlockState state = neighbor(
                            block,
                            inferred.linkOffset().plus(exterior)
                    ).getBlockState();
                    if (BlockState.MISSING.equals(state)) {
                        return Status.NEIGHBOR_UNAVAILABLE;
                    }
                    if (Ae2ResourceExtension.isQuantumBridgeId(state.getId())) {
                        return Status.INVALID;
                    }
                }
            }
        }
        return Status.SUPPORTED;
    }

    private static boolean hasExpectedBlockEntity(ExtendedBlock block) {
        BlockEntity entity = block.getBlockEntity();
        return entity != null && EXPECTED_BLOCK_ENTITY.equals(entity.getId());
    }

    private static int oppositePairs(Set<Direction6> directions) {
        int pairs = 0;
        for (Direction6 direction : directions) {
            if (directions.contains(direction.opposite())) {
                pairs++;
            }
        }
        return pairs / 2;
    }

    private static Set<Axis> axes(Set<Direction6> directions) {
        EnumSet<Axis> axes = EnumSet.noneOf(Axis.class);
        for (Direction6 direction : directions) {
            axes.add(Axis.of(direction));
        }
        return axes;
    }

    private static Plane plane(Set<Axis> axes) {
        Axis[] values = axes.toArray(Axis[]::new);
        if (values.length != 2) {
            throw new IllegalArgumentException("a quantum bridge must use exactly two axes");
        }
        return new Plane(values[0], values[1]);
    }

    private static ExtendedBlock neighbor(ExtendedBlock block, Offset offset) {
        ExtendedBlock result = block.copy();
        result.set(
                block.getX() + offset.x(),
                block.getY() + offset.y(),
                block.getZ() + offset.z()
        );
        return result;
    }

    enum Status {
        SUPPORTED,
        NEIGHBOR_UNAVAILABLE,
        INVALID
    }

    record Result(
            Status status,
            QuantumBridgeSnapshot snapshot,
            Map<Direction6, ExtendedBlock> directNeighbors
    ) {
        private static Result supported(
                QuantumBridgeSnapshot snapshot,
                Map<Direction6, ExtendedBlock> directNeighbors
        ) {
            return new Result(Status.SUPPORTED, snapshot, Map.copyOf(directNeighbors));
        }

        private static Result rejected(Status status) {
            return new Result(status, null, Map.of());
        }

        boolean supported() {
            return status == Status.SUPPORTED;
        }
    }

    private enum Axis {
        X,
        Y,
        Z;

        private static Axis of(Direction6 direction) {
            if (direction.stepX() != 0) {
                return X;
            }
            if (direction.stepY() != 0) {
                return Y;
            }
            return Z;
        }

        private Offset offset(int distance) {
            return switch (this) {
                case X -> new Offset(distance, 0, 0);
                case Y -> new Offset(0, distance, 0);
                case Z -> new Offset(0, 0, distance);
            };
        }
    }

    private record Offset(int x, int y, int z) {
        private static final Offset ZERO = new Offset(0, 0, 0);

        private static Offset of(Direction6 direction) {
            return new Offset(direction.stepX(), direction.stepY(), direction.stepZ());
        }

        private Offset plus(Offset other) {
            return new Offset(x + other.x, y + other.y, z + other.z);
        }
    }

    private record Plane(Axis first, Axis second) {
        private Offset offset(int firstDistance, int secondDistance) {
            return first.offset(firstDistance).plus(second.offset(secondDistance));
        }

        private boolean contains(Offset offset) {
            int firstCoordinate = coordinate(offset, first);
            int secondCoordinate = coordinate(offset, second);
            Axis normal = java.util.Arrays.stream(Axis.values())
                    .filter(axis -> axis != first && axis != second)
                    .findFirst()
                    .orElseThrow();
            return coordinate(offset, normal) == 0
                    && firstCoordinate >= -1 && firstCoordinate <= 1
                    && secondCoordinate >= -1 && secondCoordinate <= 1;
        }

        private static int coordinate(Offset offset, Axis axis) {
            return switch (axis) {
                case X -> offset.x();
                case Y -> offset.y();
                case Z -> offset.z();
            };
        }
    }

    private record InferredRole(
            QuantumBridgeSnapshot.Role role,
            Offset linkOffset,
            Plane plane
    ) {
    }
}
