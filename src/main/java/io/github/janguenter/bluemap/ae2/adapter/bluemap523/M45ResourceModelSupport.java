/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.adapter.bluemap523;

import de.bluecolored.bluemap.core.map.TextureGallery;
import de.bluecolored.bluemap.core.map.hires.MaxCapacityReachedException;
import de.bluecolored.bluemap.core.map.hires.RenderSettings;
import de.bluecolored.bluemap.core.map.hires.TileModelView;
import de.bluecolored.bluemap.core.map.hires.block.ResourceModelRenderer;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.Variant;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.model.Model;
import de.bluecolored.bluemap.core.resources.ResourcePath;
import de.bluecolored.bluemap.core.util.Key;
import de.bluecolored.bluemap.core.util.math.Color;
import de.bluecolored.bluemap.core.world.BlockState;
import de.bluecolored.bluemap.core.world.block.BlockNeighborhood;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Atomic stock/explicit-variant rendering shared by the M4/M5 dispatcher. */
final class M45ResourceModelSupport {

    private final ResourcePack resourcePack;
    private final ResourceModelRenderer renderer;

    M45ResourceModelSupport(
            ResourcePack resourcePack,
            TextureGallery textureGallery,
            RenderSettings renderSettings
    ) {
        this.resourcePack = Objects.requireNonNull(resourcePack, "resourcePack");
        this.renderer = new ResourceModelRenderer(
                resourcePack,
                Objects.requireNonNull(textureGallery, "textureGallery"),
                Objects.requireNonNull(renderSettings, "renderSettings")
        );
    }

    boolean renderOriginal(
            BlockNeighborhood block,
            TileModelView tileModel,
            Color blockColor,
            int renderStart
    ) {
        try {
            var resource = resourcePack.getBlockStates().get(block.getBlockState().getId());
            if (renderResource(resource, block.getBlockState(), block, tileModel, blockColor)) {
                return true;
            }
            var missing = resourcePack.getBlockStates().get(ResourcePack.MISSING_BLOCK_STATE);
            return renderResource(missing, BlockState.MISSING, block, tileModel, blockColor);
        } catch (MaxCapacityReachedException exception) {
            throw exception;
        } catch (RuntimeException | LinkageError exception) {
            tileModel.initialize(renderStart).reset();
            blockColor.set(0F, 0F, 0F, 0F, true);
            return false;
        }
    }

    boolean renderVariant(
            BlockNeighborhood block,
            Variant variant,
            TileModelView tileModel,
            Color blockColor
    ) {
        if (variant == null
                || variant.getModel().getResource(resourcePack.getModels()::get) == null) {
            return false;
        }
        Color rendered = new Color().set(0F, 0F, 0F, 0F, true);
        renderer.render(block, variant, tileModel.initialize(), rendered);
        blockColor.set(rendered);
        return true;
    }

    boolean renderModel(
            BlockNeighborhood block,
            String modelResource,
            int xRotation,
            int yRotation,
            TileModelView tileModel,
            Color blockColor
    ) {
        Key model = modelKey(modelResource);
        return renderVariant(
                block,
                new Variant(new ResourcePath<Model>(model), xRotation, yRotation, 0),
                tileModel,
                blockColor
        );
    }

    private boolean renderResource(
            de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.BlockState resource,
            BlockState state,
            BlockNeighborhood block,
            TileModelView tileModel,
            Color blockColor
    ) {
        if (resource == null) {
            return false;
        }
        int start = tileModel.getStart();
        List<Variant> variants = new ArrayList<>();
        resource.forEach(state, block.getX(), block.getY(), block.getZ(), variants::add);
        if (variants.isEmpty()) {
            return false;
        }
        blockColor.set(0F, 0F, 0F, 0F, true);
        float opacity = 0F;
        boolean renderedAny = false;
        for (Variant variant : variants) {
            if (variant.getModel().getResource(resourcePack.getModels()::get) == null) {
                continue;
            }
            Color rendered = new Color().set(0F, 0F, 0F, 0F, true);
            renderer.render(block, variant, tileModel.initialize(), rendered);
            renderedAny = true;
            opacity = Math.max(opacity, rendered.a);
            blockColor.add(rendered.premultiplied());
        }
        if (!renderedAny) {
            tileModel.initialize(start);
            return false;
        }
        if (blockColor.a > 0F) {
            blockColor.flatten().straight();
            blockColor.a = opacity;
        }
        tileModel.initialize(start);
        return true;
    }

    private static Key modelKey(String resource) {
        String prefix = "assets/";
        String marker = "/models/";
        String suffix = ".json";
        int markerIndex = resource == null ? -1 : resource.indexOf(marker);
        if (markerIndex <= prefix.length()
                || !resource.startsWith(prefix)
                || !resource.endsWith(suffix)) {
            throw new IllegalArgumentException("invalid exact model resource path");
        }
        String namespace = resource.substring(prefix.length(), markerIndex);
        String path = resource.substring(
                markerIndex + marker.length(),
                resource.length() - suffix.length()
        );
        return Key.parse(namespace + ':' + path);
    }
}
