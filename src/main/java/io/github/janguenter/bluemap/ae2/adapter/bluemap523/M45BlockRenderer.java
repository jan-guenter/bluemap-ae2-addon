/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.adapter.bluemap523;

import de.bluecolored.bluemap.core.map.TextureGallery;
import de.bluecolored.bluemap.core.map.hires.MaxCapacityReachedException;
import de.bluecolored.bluemap.core.map.hires.RenderSettings;
import de.bluecolored.bluemap.core.map.hires.TileModelView;
import de.bluecolored.bluemap.core.map.hires.block.BlockRenderer;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.Variant;
import de.bluecolored.bluemap.core.util.math.Color;
import de.bluecolored.bluemap.core.world.block.BlockNeighborhood;
import io.github.janguenter.bluemap.ae2.activation.ExtensionRouteActivation;
import io.github.janguenter.bluemap.ae2.profile.expandedae.ExpandedAe211Catalog;
import io.github.janguenter.bluemap.ae2.profile.merequester.MeRequester143Catalog;

import java.util.Objects;

/** Single dispatch renderer for independently isolated M4/M5 whole-block routes. */
final class M45BlockRenderer implements BlockRenderer {

    private final M45Runtime runtime;
    private final M45ResourceModelSupport stock;
    private final M45CraftingRenderer crafting;
    private final M45ConnectedBlockRenderer connected;

    M45BlockRenderer(
            ResourcePack resourcePack,
            TextureGallery textureGallery,
            RenderSettings renderSettings,
            M45Runtime runtime
    ) {
        this.runtime = Objects.requireNonNull(runtime, "runtime");
        this.stock = new M45ResourceModelSupport(resourcePack, textureGallery, renderSettings);
        this.crafting = new M45CraftingRenderer(
                resourcePack,
                textureGallery,
                renderSettings,
                runtime
        );
        this.connected = new M45ConnectedBlockRenderer(
                resourcePack,
                textureGallery,
                renderSettings,
                runtime
        );
    }

    @Override
    public void render(
            BlockNeighborhood block,
            Variant ignoredVariant,
            TileModelView tileModel,
            Color blockColor
    ) {
        int renderStart = tileModel.getStart();
        String blockId = block.getBlockState().getId().getFormatted();
        if (connected.handles(blockId)) {
            connected.render(block, tileModel, blockColor, renderStart);
            return;
        }
        if (M45CraftingCatalog.extensionBlocks().contains(blockId)) {
            crafting.render(block, tileModel, blockColor, renderStart);
            return;
        }
        String zRoute = zRoute(blockId);
        if (zRoute != null) {
            renderZNormalized(zRoute, block, tileModel, blockColor, renderStart);
            return;
        }
        stock.renderOriginal(block, tileModel, blockColor, renderStart);
    }

    private void renderZNormalized(
            String routeId,
            BlockNeighborhood block,
            TileModelView tileModel,
            Color blockColor,
            int renderStart
    ) {
        if (!runtime.active(routeId)) {
            stock.renderOriginal(block, tileModel, blockColor, renderStart);
            return;
        }
        ExtensionRouteActivation route = runtime.route(routeId);
        try {
            Variant variant = Ae2ZVariantResolver.resolve(block.getBlockState());
            if (variant == null || !stock.renderVariant(block, variant, tileModel, blockColor)) {
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

    private static String zRoute(String blockId) {
        return switch (blockId) {
            case MeRequester143Catalog.REQUESTER_BLOCK -> M45Runtime.ME_REQUESTER;
            case ExpandedAe211Catalog.IO_PORT_BLOCK -> M45Runtime.EXPANDED_AE;
            default -> null;
        };
    }
}
