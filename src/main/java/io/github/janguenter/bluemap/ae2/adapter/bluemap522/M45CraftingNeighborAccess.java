/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.adapter.bluemap522;

import de.bluecolored.bluemap.core.world.BlockState;

import java.util.Objects;

/** Route-aware bridge from native AE2 crafting topology to exact extension blocks. */
final class M45CraftingNeighborAccess {

    private final M45Runtime runtime;

    M45CraftingNeighborAccess(M45Runtime runtime) {
        this.runtime = Objects.requireNonNull(runtime, "runtime");
    }

    boolean isExactActiveNeighbor(BlockState state) {
        if (state == null || state.getId() == null) {
            return false;
        }
        String routeId = M45CraftingCatalog.route(state.getId().getFormatted());
        return routeId != null
                && runtime.active(routeId)
                && M45CraftingCatalog.isExactState(state);
    }
}
