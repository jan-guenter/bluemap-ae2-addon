/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.adapter.bluemap523;

import de.bluecolored.bluemap.core.map.TextureGallery;
import de.bluecolored.bluemap.core.map.hires.RenderSettings;
import de.bluecolored.bluemap.core.map.hires.TileModel;
import de.bluecolored.bluemap.core.map.hires.TileModelView;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.texture.Texture;
import de.bluecolored.bluemap.core.util.Key;
import de.bluecolored.bluemap.core.util.math.Color;
import de.bluecolored.bluemap.core.world.BlockState;
import de.bluecolored.bluemap.core.world.LightData;
import de.bluecolored.bluemap.core.world.block.BlockNeighborhood;
import de.bluecolored.bluemap.core.world.block.ExtendedBlock;
import io.github.janguenter.bluemap.ae2.model.CableColor;
import io.github.janguenter.bluemap.ae2.model.CraftingGeometry;
import io.github.janguenter.bluemap.ae2.model.CraftingSnapshot;
import io.github.janguenter.bluemap.ae2.model.Direction6;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/** Host adapter shared by exact AE2-family crafting-cube profiles. */
final class CraftingMeshEmitter {

    private static final float SIXTEENTH = 1F / 16F;

    private final ResourcePack resourcePack;
    private final TextureGallery textureGallery;
    private final RenderSettings renderSettings;

    CraftingMeshEmitter(
            ResourcePack resourcePack,
            TextureGallery textureGallery,
            RenderSettings renderSettings
    ) {
        this.resourcePack = Objects.requireNonNull(resourcePack, "resourcePack");
        this.textureGallery = Objects.requireNonNull(textureGallery, "textureGallery");
        this.renderSettings = Objects.requireNonNull(renderSettings, "renderSettings");
    }

    /** Plans atomically and emits the mesh; returns false before writing on resource drift. */
    boolean render(
            BlockNeighborhood block,
            CraftingSnapshot snapshot,
            Function<CraftingGeometry.Quad, Key> textureResolver,
            TileModelView tileModel,
            Color blockColor
    ) {
        Objects.requireNonNull(block, "block");
        Objects.requireNonNull(snapshot, "snapshot");
        Objects.requireNonNull(textureResolver, "textureResolver");
        Objects.requireNonNull(tileModel, "tileModel");
        Objects.requireNonNull(blockColor, "blockColor");

        BlockState center = block.getBlockState();
        LightData centerLight = block.getLightData();
        List<PlannedQuad> planned = new ArrayList<>(60);
        for (CraftingGeometry.Quad quad : CraftingGeometry.forSnapshot(snapshot)) {
            ExtendedBlock outward = neighbor(block, quad.face());
            BlockState outwardState = outward.getBlockState();
            if (BlockState.MISSING.equals(outwardState)) {
                return false;
            }
            if (outward.getProperties().isCulling()
                    || (outward.getProperties().getCullingIdentical()
                    && center.equals(outwardState))
                    || renderSettings.isRenderTopOnly() && quad.face() != Direction6.UP) {
                continue;
            }

            LightData outwardLight = outward.getLightData();
            LightLevels worldLight = new LightLevels(
                    Math.max(centerLight.getSkyLight(), outwardLight.getSkyLight()),
                    Math.max(centerLight.getBlockLight(), outwardLight.getBlockLight())
            );
            if (isHiddenCave(block, worldLight)) {
                continue;
            }
            LightLevels light = quad.layer().emissiveWhenPowered() && snapshot.powered()
                    ? new LightLevels(15, 15) : worldLight;
            Key textureKey = textureResolver.apply(quad);
            Texture texture = textureKey == null
                    ? null : resourcePack.getTextures().get(textureKey);
            if (texture == null || ResourcePack.MISSING_TEXTURE.equals(textureKey)) {
                return false;
            }
            Rgb tint = tint(quad.layer().tint(), snapshot.paintedColor());
            planned.add(new PlannedQuad(
                    quad,
                    textureGallery.get(textureKey),
                    texture,
                    light,
                    tint
            ));
        }

        int start = tileModel.getStart();
        int triangle = tileModel.add(planned.size() * 2);
        TileModel model = tileModel.getTileModel();
        for (PlannedQuad entry : planned) {
            List<CraftingGeometry.Vertex> vertices = entry.quad().vertices();
            setTriangle(model, triangle, vertices.get(0), vertices.get(1), vertices.get(2));
            setAttributes(model, triangle++, entry);
            setTriangle(model, triangle, vertices.get(0), vertices.get(2), vertices.get(3));
            setAttributes(model, triangle++, entry);
        }
        blockColor.set(mapColor(planned));
        tileModel.initialize(start);
        return true;
    }

    private static void setTriangle(
            TileModel model,
            int triangle,
            CraftingGeometry.Vertex first,
            CraftingGeometry.Vertex second,
            CraftingGeometry.Vertex third
    ) {
        model.setPositions(
                triangle,
                units(first.x16()), units(first.y16()), units(first.z16()),
                units(second.x16()), units(second.y16()), units(second.z16()),
                units(third.x16()), units(third.y16()), units(third.z16())
        );
        model.setUvs(
                triangle,
                units(first.u16()), units(first.v16()),
                units(second.u16()), units(second.v16()),
                units(third.u16()), units(third.v16())
        );
    }

    private static void setAttributes(TileModel model, int triangle, PlannedQuad entry) {
        model.setMaterialIndex(triangle, entry.material());
        model.setColor(triangle, entry.tint().red(), entry.tint().green(), entry.tint().blue());
        model.setSunlight(triangle, entry.light().sunlight());
        model.setBlocklight(triangle, entry.light().blocklight());
        model.setAOs(triangle, 1F, 1F, 1F);
    }

    private Color mapColor(List<PlannedQuad> planned) {
        Color result = new Color().set(0F, 0F, 0F, 0F, true);
        float opacity = 0F;
        for (PlannedQuad entry : planned) {
            if (entry.quad().face() != Direction6.UP) {
                continue;
            }
            Color layer = new Color().set(entry.texture().getColorPremultiplied());
            layer.r *= entry.tint().red();
            layer.g *= entry.tint().green();
            layer.b *= entry.tint().blue();
            float illumination = combinedLight(entry.light());
            layer.r *= illumination;
            layer.g *= illumination;
            layer.b *= illumination;
            opacity = Math.max(opacity, layer.a);
            result.add(layer);
        }
        if (result.a > 0F) {
            result.flatten().straight();
            result.a = opacity;
        }
        return result;
    }

    private boolean isHiddenCave(BlockNeighborhood block, LightLevels light) {
        return block.isRemoveIfCave()
                && (renderSettings.isCaveDetectionUsesBlockLight()
                ? Math.max(light.blocklight(), light.sunlight())
                : light.sunlight()) == 0;
    }

    private float combinedLight(LightLevels light) {
        float combined = Math.max(light.sunlight() / 15F, light.blocklight() / 15F);
        return (1F - renderSettings.getAmbientLight()) * combined
                + renderSettings.getAmbientLight();
    }

    private static ExtendedBlock neighbor(BlockNeighborhood block, Direction6 direction) {
        return block.getNeighborBlock(
                direction.stepX(), direction.stepY(), direction.stepZ()
        );
    }

    private static Rgb tint(CraftingGeometry.Tint tint, CableColor color) {
        int rgb = switch (tint) {
            case NONE -> 0xffffff;
            case BRIGHT -> color.brightRgb();
            case MEDIUM -> color.mediumRgb();
            case DARK -> color.darkRgb();
        };
        return new Rgb(
                ((rgb >>> 16) & 0xff) / 255F,
                ((rgb >>> 8) & 0xff) / 255F,
                (rgb & 0xff) / 255F
        );
    }

    private static float units(double units16) {
        return (float) units16 * SIXTEENTH;
    }

    private record PlannedQuad(
            CraftingGeometry.Quad quad,
            int material,
            Texture texture,
            LightLevels light,
            Rgb tint
    ) {
    }

    private record LightLevels(int sunlight, int blocklight) {
    }

    private record Rgb(float red, float green, float blue) {
    }
}
