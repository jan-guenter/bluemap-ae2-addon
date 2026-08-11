/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.model;

import io.github.janguenter.bluemap.ae2.profile.Ae219217QuartzGlassProfile;

import java.util.Objects;

/** Immutable exact-state projection of one native AE2 quartz-glass block. */
public record QuartzGlassSnapshot(String blockId, int connectionMask) {

    public static final int ALL_CONNECTIONS = (1 << Direction6.values().length) - 1;

    public QuartzGlassSnapshot {
        Objects.requireNonNull(blockId, "blockId");
        if (!Ae219217QuartzGlassProfile.QUARTZ_GLASS_BLOCK.equals(blockId)
                && !Ae219217QuartzGlassProfile.VIBRANT_GLASS_BLOCK.equals(blockId)) {
            throw new IllegalArgumentException("unsupported quartz-glass block id");
        }
        if ((connectionMask & ~ALL_CONNECTIONS) != 0) {
            throw new IllegalArgumentException("connectionMask must contain exactly six bits");
        }
    }

    public static QuartzGlassSnapshot isolated(String blockId) {
        return new QuartzGlassSnapshot(blockId, 0);
    }

    public boolean isConnected(Direction6 direction) {
        Objects.requireNonNull(direction, "direction");
        return (connectionMask & direction.maskBit()) != 0;
    }

    public QuartzGlassSnapshot withConnection(Direction6 direction) {
        Objects.requireNonNull(direction, "direction");
        return new QuartzGlassSnapshot(blockId, connectionMask | direction.maskBit());
    }

    /** Returns AE2's face-local four-edge mask in up/right/down/left bit order. */
    public int frameMask(Direction6 face) {
        Objects.requireNonNull(face, "face");
        Direction6[] edges = switch (face) {
            case DOWN -> new Direction6[]{
                    Direction6.SOUTH, Direction6.EAST, Direction6.NORTH, Direction6.WEST
            };
            case UP -> new Direction6[]{
                    Direction6.SOUTH, Direction6.WEST, Direction6.NORTH, Direction6.EAST
            };
            case NORTH -> new Direction6[]{
                    Direction6.UP, Direction6.WEST, Direction6.DOWN, Direction6.EAST
            };
            case SOUTH -> new Direction6[]{
                    Direction6.UP, Direction6.EAST, Direction6.DOWN, Direction6.WEST
            };
            case WEST -> new Direction6[]{
                    Direction6.UP, Direction6.SOUTH, Direction6.DOWN, Direction6.NORTH
            };
            case EAST -> new Direction6[]{
                    Direction6.UP, Direction6.NORTH, Direction6.DOWN, Direction6.SOUTH
            };
        };

        int mask = 0;
        for (int index = 0; index < edges.length; index++) {
            if (!isConnected(edges[index])) {
                mask |= 1 << index;
            }
        }
        return mask;
    }
}
