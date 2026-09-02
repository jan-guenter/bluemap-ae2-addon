/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.adapter.bluemap523;

import de.bluecolored.bluemap.core.util.Key;
import de.bluecolored.bluemap.core.world.BlockEntity;
import de.bluecolored.bluemap.core.world.BlockState;
import de.bluecolored.bluemap.core.world.block.ExtendedBlock;
import io.github.janguenter.bluemap.ae2.model.Direction6;
import io.github.janguenter.bluemap.ae2.model.M3CompletionBlockKind;
import io.github.janguenter.bluemap.ae2.model.SpatialPylonSnapshot;
import io.github.janguenter.bluemap.ae2.profile.Ae219217M3CompletionProfile;

/** Uncached bounded component resolver shared by spatial-pylon consumers. */
final class SpatialPylonTopologyResolver {

    private SpatialPylonTopologyResolver() {
    }

    static Status resolve(
            ExtendedBlock block,
            SpatialPylonSnapshot.Axis axis
    ) {
        ExtendedBlock cursor = block.copy();
        cursor.set(block.getX(), block.getY(), block.getZ());
        Status centerStatus = inspectPerpendicularPylons(
                cursor,
                block.getX(),
                block.getY(),
                block.getZ(),
                axis
        );
        if (centerStatus == Status.BRANCHED) {
            return centerStatus;
        }

        boolean incomplete = centerStatus == Status.INCOMPLETE;
        int componentBlocks = 1;
        for (Direction6 direction : Direction6.values()) {
            if (!axis.contains(direction)) {
                continue;
            }
            for (int distance = 1; ; distance++) {
                long x = (long) block.getX() + (long) direction.stepX() * distance;
                long y = (long) block.getY() + (long) direction.stepY() * distance;
                long z = (long) block.getZ() + (long) direction.stepZ() * distance;
                Cell cell = inspectPylonCell(cursor, x, y, z);
                if (cell == Cell.NOT_PYLON) {
                    break;
                }
                if (cell == Cell.INCOMPLETE) {
                    incomplete = true;
                    break;
                }
                if (componentBlocks
                        >= Ae219217M3CompletionProfile
                                .SPATIAL_PYLON_COMPONENT_MAX_BLOCKS) {
                    incomplete = true;
                    break;
                }
                componentBlocks++;
                Status perpendicularStatus = inspectPerpendicularPylons(
                        cursor,
                        (int) x,
                        (int) y,
                        (int) z,
                        axis
                );
                if (perpendicularStatus == Status.BRANCHED) {
                    return perpendicularStatus;
                }
                incomplete |= perpendicularStatus == Status.INCOMPLETE;
            }
        }
        return incomplete ? Status.INCOMPLETE : Status.STRAIGHT;
    }

    private static Status inspectPerpendicularPylons(
            ExtendedBlock cursor,
            int x,
            int y,
            int z,
            SpatialPylonSnapshot.Axis axis
    ) {
        boolean incomplete = false;
        for (Direction6 direction : Direction6.values()) {
            if (axis.contains(direction)) {
                continue;
            }
            Cell cell = inspectPylonCell(
                    cursor,
                    (long) x + direction.stepX(),
                    (long) y + direction.stepY(),
                    (long) z + direction.stepZ()
            );
            if (cell == Cell.PYLON) {
                return Status.BRANCHED;
            }
            incomplete |= cell == Cell.INCOMPLETE;
        }
        return incomplete ? Status.INCOMPLETE : Status.STRAIGHT;
    }

    private static Cell inspectPylonCell(
            ExtendedBlock cursor,
            long x,
            long y,
            long z
    ) {
        if (x < Integer.MIN_VALUE || x > Integer.MAX_VALUE
                || y < Integer.MIN_VALUE || y > Integer.MAX_VALUE
                || z < Integer.MIN_VALUE || z > Integer.MAX_VALUE) {
            return Cell.INCOMPLETE;
        }
        cursor.set((int) x, (int) y, (int) z);
        if (missing(cursor)) {
            return Cell.INCOMPLETE;
        }
        BlockState state = cursor.getBlockState();
        if (!Ae219217M3CompletionProfile.SPATIAL_PYLON_BLOCK.equals(
                state.getId().getFormatted()
        )) {
            return Cell.NOT_PYLON;
        }
        return M3CompletionRenderer.exactKind(state)
                == M3CompletionBlockKind.SPATIAL_PYLON
                && hasBlockEntity(
                        cursor,
                        Ae219217M3CompletionProfile.SPATIAL_PYLON_BLOCK_ENTITY_ID
                ) ? Cell.PYLON : Cell.INCOMPLETE;
    }

    private static boolean hasBlockEntity(ExtendedBlock block, String expectedId) {
        BlockEntity entity = block.getBlockEntity();
        Key entityId = entity == null ? null : entity.getId();
        return entityId != null && expectedId.equals(entityId.getFormatted());
    }

    private static boolean missing(ExtendedBlock block) {
        return block == null || BlockState.MISSING.equals(block.getBlockState());
    }

    enum Status {
        STRAIGHT,
        BRANCHED,
        INCOMPLETE
    }

    private enum Cell {
        NOT_PYLON,
        PYLON,
        INCOMPLETE
    }
}
