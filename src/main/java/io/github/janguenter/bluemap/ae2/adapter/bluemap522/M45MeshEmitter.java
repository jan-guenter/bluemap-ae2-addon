/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.adapter.bluemap522;

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
import io.github.janguenter.bluemap.ae2.model.Direction6;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Atomic host emission shared by exact connected-model M4/M5 geometry. */
final class M45MeshEmitter {

    private static final float SIXTEENTH = 1F / 16F;

    private final ResourcePack resourcePack;
    private final TextureGallery textureGallery;
    private final RenderSettings renderSettings;

    M45MeshEmitter(
            ResourcePack resourcePack,
            TextureGallery textureGallery,
            RenderSettings renderSettings
    ) {
        this.resourcePack = Objects.requireNonNull(resourcePack, "resourcePack");
        this.textureGallery = Objects.requireNonNull(textureGallery, "textureGallery");
        this.renderSettings = Objects.requireNonNull(renderSettings, "renderSettings");
    }

    /** Plans every material/light/culling decision before appending any triangle. */
    boolean render(
            BlockNeighborhood block,
            List<Quad> source,
            TileModelView tileModel,
            Color blockColor
    ) {
        Objects.requireNonNull(block, "block");
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(tileModel, "tileModel");
        Objects.requireNonNull(blockColor, "blockColor");

        LightData ownLight = block.getLightData();
        BlockState ownState = block.getBlockState();
        List<PlannedQuad> planned = new ArrayList<>(source.size());
        for (Quad quad : source) {
            if (renderSettings.isRenderTopOnly() && quad.normalFace() != Direction6.UP) {
                continue;
            }
            ExtendedBlock outward = neighbor(block, quad.lightFace());
            BlockState outwardState = outward.getBlockState();
            if (BlockState.MISSING.equals(outwardState)) {
                return false;
            }
            if (quad.cullOutward() && (outward.getProperties().isCulling()
                    || outward.getProperties().getCullingIdentical()
                    && ownState.equals(outwardState))) {
                continue;
            }
            LightData outwardLight = outward.getLightData();
            LightLevels light = quad.emissive()
                    ? new LightLevels(15, 15)
                    : new LightLevels(
                            Math.max(ownLight.getSkyLight(), outwardLight.getSkyLight()),
                            Math.max(ownLight.getBlockLight(), outwardLight.getBlockLight())
                    );
            if (hiddenCave(block, light)) {
                continue;
            }
            Texture texture = resourcePack.getTextures().get(quad.texture());
            if (texture == null || ResourcePack.MISSING_TEXTURE.equals(quad.texture())) {
                return false;
            }
            planned.add(new PlannedQuad(
                    quad,
                    textureGallery.get(quad.texture()),
                    texture,
                    light,
                    ambientOcclusion(block, quad)
            ));
        }

        int start = tileModel.getStart();
        int triangle = tileModel.add(planned.size() * 2);
        TileModel model = tileModel.getTileModel();
        for (PlannedQuad entry : planned) {
            List<Vertex> vertices = entry.quad().vertices();
            setTriangle(model, triangle, vertices.get(0), vertices.get(1), vertices.get(2));
            setAttributes(
                    model,
                    triangle++,
                    entry,
                    entry.ao().get(0), entry.ao().get(1), entry.ao().get(2)
            );
            setTriangle(model, triangle, vertices.get(0), vertices.get(2), vertices.get(3));
            setAttributes(
                    model,
                    triangle++,
                    entry,
                    entry.ao().get(0), entry.ao().get(2), entry.ao().get(3)
            );
        }
        blockColor.set(mapColor(planned));
        tileModel.initialize(start);
        return true;
    }

    private static List<Float> ambientOcclusion(BlockNeighborhood block, Quad quad) {
        if (!quad.ambientOcclusion()) {
            return List.of(1F, 1F, 1F, 1F);
        }
        return quad.vertices().stream()
                .map(vertex -> testAo(vertex, quad.lightFace(), block))
                .toList();
    }

    private static float testAo(
            Vertex vertex,
            Direction6 direction,
            BlockNeighborhood block
    ) {
        int x = boundaryOffset(vertex.x16());
        int y = boundaryOffset(vertex.y16());
        int z = boundaryOffset(vertex.z16());
        int occluding = 0;
        if (x * direction.stepX() + y * direction.stepY() > 0
                && neighbor(block, x, y, 0).getProperties().isOccluding()) {
            occluding++;
        }
        if (x * direction.stepX() + z * direction.stepZ() > 0
                && neighbor(block, x, 0, z).getProperties().isOccluding()) {
            occluding++;
        }
        if (y * direction.stepY() + z * direction.stepZ() > 0
                && neighbor(block, 0, y, z).getProperties().isOccluding()) {
            occluding++;
        }
        if (x * direction.stepX() + y * direction.stepY()
                + z * direction.stepZ() > 0
                && neighbor(block, x, y, z).getProperties().isOccluding()) {
            occluding++;
        }
        return 1F - Math.min(occluding, 3) * 0.25F;
    }

    private static int boundaryOffset(double coordinate16) {
        if (coordinate16 >= 16) {
            return 1;
        }
        if (coordinate16 <= 0) {
            return -1;
        }
        return 0;
    }

    private static void setTriangle(
            TileModel model,
            int triangle,
            Vertex first,
            Vertex second,
            Vertex third
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

    private static void setAttributes(
            TileModel model,
            int triangle,
            PlannedQuad entry,
            float firstAo,
            float secondAo,
            float thirdAo
    ) {
        model.setMaterialIndex(triangle, entry.material());
        model.setColor(triangle, 1F, 1F, 1F);
        model.setSunlight(triangle, entry.light().sunlight());
        model.setBlocklight(triangle, entry.light().blocklight());
        model.setAOs(triangle, firstAo, secondAo, thirdAo);
    }

    private Color mapColor(List<PlannedQuad> planned) {
        Color result = new Color().set(0F, 0F, 0F, 0F, true);
        float opacity = 0F;
        for (PlannedQuad entry : planned) {
            if (entry.quad().normalFace() != Direction6.UP) {
                continue;
            }
            Color layer = new Color().set(entry.texture().getColorPremultiplied());
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

    private boolean hiddenCave(BlockNeighborhood block, LightLevels light) {
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
        return neighbor(block, direction.stepX(), direction.stepY(), direction.stepZ());
    }

    private static ExtendedBlock neighbor(BlockNeighborhood block, int x, int y, int z) {
        return block.getNeighborBlock(x, y, z);
    }

    private static float units(double coordinate16) {
        return (float) coordinate16 * SIXTEENTH;
    }

    record Vertex(double x16, double y16, double z16, double u16, double v16) {

        Vertex {
            requireUnit("x16", x16);
            requireUnit("y16", y16);
            requireUnit("z16", z16);
            requireUnit("u16", u16);
            requireUnit("v16", v16);
        }

        private static void requireUnit(String name, double value) {
            if (!Double.isFinite(value) || value < 0 || value > 16) {
                throw new IllegalArgumentException(name + " must be in [0, 16]");
            }
        }
    }

    record Quad(
            Direction6 normalFace,
            Direction6 lightFace,
            Key texture,
            List<Vertex> vertices,
            boolean emissive,
            boolean ambientOcclusion,
            boolean cullOutward
    ) {

        Quad {
            Objects.requireNonNull(normalFace, "normalFace");
            Objects.requireNonNull(lightFace, "lightFace");
            Objects.requireNonNull(texture, "texture");
            vertices = List.copyOf(Objects.requireNonNull(vertices, "vertices"));
            if (vertices.size() != 4) {
                throw new IllegalArgumentException("a surface quad must have four vertices");
            }
        }

        static Quad outward(
                Direction6 face,
                Key texture,
                List<Vertex> vertices,
                boolean emissive,
                boolean ambientOcclusion
        ) {
            return new Quad(
                    face, face, texture, vertices,
                    emissive, ambientOcclusion, true
            );
        }
    }

    private record PlannedQuad(
            Quad quad,
            int material,
            Texture texture,
            LightLevels light,
            List<Float> ao
    ) {
    }

    private record LightLevels(int sunlight, int blocklight) {
    }
}
