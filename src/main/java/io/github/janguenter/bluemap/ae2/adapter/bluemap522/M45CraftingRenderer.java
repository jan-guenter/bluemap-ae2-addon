/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.adapter.bluemap522;

import de.bluecolored.bluemap.core.map.TextureGallery;
import de.bluecolored.bluemap.core.map.hires.MaxCapacityReachedException;
import de.bluecolored.bluemap.core.map.hires.RenderSettings;
import de.bluecolored.bluemap.core.map.hires.TileModelView;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.util.Key;
import de.bluecolored.bluemap.core.util.math.Color;
import de.bluecolored.bluemap.core.world.BlockState;
import de.bluecolored.bluemap.core.world.block.BlockNeighborhood;
import de.bluecolored.bluemap.core.world.block.ExtendedBlock;
import io.github.janguenter.bluemap.ae2.activation.ExtensionRouteActivation;
import io.github.janguenter.bluemap.ae2.model.CableColor;
import io.github.janguenter.bluemap.ae2.model.CraftingBlockKind;
import io.github.janguenter.bluemap.ae2.model.CraftingSnapshot;
import io.github.janguenter.bluemap.ae2.model.Direction6;
import io.github.janguenter.bluemap.ae2.profile.megacells.MegaCells4110Profile;

import java.util.EnumSet;
import java.util.Locale;
import java.util.Objects;

/** Exact formed-crafting projection shared by Expanded AE and MEGA Cells. */
final class M45CraftingRenderer {

    private final M45Runtime runtime;
    private final java.util.function.BooleanSupplier coreCraftingActive;
    private final CraftingMeshEmitter emitter;
    private final M45ResourceModelSupport stock;

    M45CraftingRenderer(
            ResourcePack resourcePack,
            TextureGallery textureGallery,
            RenderSettings renderSettings,
            M45Runtime runtime
    ) {
        this(
                resourcePack,
                textureGallery,
                renderSettings,
                runtime,
                BlueMap522Adapter::coreCraftingActiveForM45
        );
    }

    M45CraftingRenderer(
            ResourcePack resourcePack,
            TextureGallery textureGallery,
            RenderSettings renderSettings,
            M45Runtime runtime,
            java.util.function.BooleanSupplier coreCraftingActive
    ) {
        this.runtime = Objects.requireNonNull(runtime, "runtime");
        this.coreCraftingActive = Objects.requireNonNull(
                coreCraftingActive,
                "coreCraftingActive"
        );
        this.emitter = new CraftingMeshEmitter(resourcePack, textureGallery, renderSettings);
        this.stock = new M45ResourceModelSupport(
                resourcePack,
                textureGallery,
                renderSettings
        );
    }

    void render(
            BlockNeighborhood block,
            TileModelView tileModel,
            Color blockColor,
            int renderStart
    ) {
        String blockId = block.getBlockState().getId().getFormatted();
        String routeId = M45CraftingCatalog.route(blockId);
        if (routeId == null || !runtime.active(routeId)) {
            stock.renderOriginal(block, tileModel, blockColor, renderStart);
            return;
        }
        ExtensionRouteActivation route = runtime.route(routeId);
        try {
            BlockState state = block.getBlockState();
            if (!M45CraftingCatalog.isExactState(state)) {
                fallback(block, tileModel, blockColor, renderStart);
                return;
            }
            if (!Boolean.parseBoolean(state.getProperties().get("formed"))) {
                stock.renderOriginal(block, tileModel, blockColor, renderStart);
                return;
            }

            CraftingBlockKind kind = M45CraftingCatalog.kind(blockId);
            CenterOrientation orientation = centerOrientation(block, kind, state);
            if (orientation == null) {
                fallback(block, tileModel, blockColor, renderStart);
                return;
            }
            EnumSet<Direction6> connections = connections(block);
            if (connections == null) {
                fallback(block, tileModel, blockColor, renderStart);
                return;
            }
            CraftingSnapshot snapshot = new CraftingSnapshot(
                    kind,
                    Boolean.parseBoolean(state.getProperties().get("powered")),
                    orientation.facing(),
                    orientation.spin(),
                    orientation.color(),
                    connections
            );
            if (!emitter.render(
                    block,
                    snapshot,
                    quad -> M45CraftingCatalog.texture(blockId, quad),
                    tileModel,
                    blockColor
            )) {
                fallback(block, tileModel, blockColor, renderStart);
            }
        } catch (MaxCapacityReachedException exception) {
            throw exception;
        } catch (RuntimeException | LinkageError exception) {
            route.disable(
                    ExtensionRouteActivation.Reason.RENDER_CALLBACK_FAILED,
                    "render-callback-failed"
            );
            fallback(block, tileModel, blockColor, renderStart);
        }
    }

    private static CenterOrientation centerOrientation(
            BlockNeighborhood block,
            CraftingBlockKind kind,
            BlockState state
    ) {
        if (kind != CraftingBlockKind.MONITOR) {
            return new CenterOrientation(Direction6.NORTH, 0, CableColor.TRANSPARENT);
        }
        if (!MegaCells4110Profile.CRAFTING_MONITOR.equals(
                state.getId().getFormatted()
        ) || !(block.getBlockEntity() instanceof Ae2CraftingMonitorBlockEntityData monitor)
                || monitor.getId() == null
                || !Key.parse(MegaCells4110Profile.CRAFTING_MONITOR_BLOCK_ENTITY)
                        .equals(monitor.getId())) {
            return null;
        }
        return new CenterOrientation(
                Direction6.valueOf(
                        state.getProperties().get("facing").toUpperCase(Locale.ROOT)
                ),
                Integer.parseInt(state.getProperties().get("spin")),
                monitor.getPaintedColor()
        );
    }

    private EnumSet<Direction6> connections(BlockNeighborhood block) {
        EnumSet<Direction6> result = EnumSet.noneOf(Direction6.class);
        for (Direction6 direction : Direction6.values()) {
            ExtendedBlock neighbor = block.getNeighborBlock(
                    direction.stepX(), direction.stepY(), direction.stepZ()
            );
            BlockState state = neighbor.getBlockState();
            if (BlockState.MISSING.equals(state)) {
                return null;
            }
            String id = state.getId().getFormatted();
            if (M45CraftingCatalog.isKnownCraftingBlock(id)) {
                String ownerRoute = M45CraftingCatalog.route(id);
                boolean ownerActive = ownerRoute == null
                        ? coreCraftingActive.getAsBoolean()
                        : runtime.active(ownerRoute);
                if (!ownerActive) {
                    return null;
                }
                if (!M45CraftingCatalog.isExactState(state)) {
                    return null;
                }
                result.add(direction);
            }
        }
        return result;
    }

    private void fallback(
            BlockNeighborhood block,
            TileModelView tileModel,
            Color blockColor,
            int renderStart
    ) {
        tileModel.initialize(renderStart).reset();
        blockColor.set(0F, 0F, 0F, 0F, true);
        stock.renderOriginal(block, tileModel, blockColor, renderStart);
    }

    private record CenterOrientation(Direction6 facing, int spin, CableColor color) {
    }
}
