/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.adapter.bluemap523;

import de.bluecolored.bluemap.core.map.TextureGallery;
import de.bluecolored.bluemap.core.map.hires.MaxCapacityReachedException;
import de.bluecolored.bluemap.core.map.hires.RenderSettings;
import de.bluecolored.bluemap.core.map.hires.TileModel;
import de.bluecolored.bluemap.core.map.hires.TileModelView;
import de.bluecolored.bluemap.core.map.hires.block.BlockRenderer;
import de.bluecolored.bluemap.core.map.hires.block.ResourceModelRenderer;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.Variant;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.texture.Texture;
import de.bluecolored.bluemap.core.util.Key;
import de.bluecolored.bluemap.core.util.math.Color;
import de.bluecolored.bluemap.core.world.BlockState;
import de.bluecolored.bluemap.core.world.LightData;
import de.bluecolored.bluemap.core.world.block.BlockNeighborhood;
import de.bluecolored.bluemap.core.world.block.ExtendedBlock;
import io.github.janguenter.bluemap.ae2.activation.ProfileActivation;
import io.github.janguenter.bluemap.ae2.activation.QuartzGlassRouteActivation;
import io.github.janguenter.bluemap.ae2.diagnostics.BoundedDiagnostics;
import io.github.janguenter.bluemap.ae2.model.Direction6;
import io.github.janguenter.bluemap.ae2.model.QuartzGlassGeometry;
import io.github.janguenter.bluemap.ae2.model.QuartzGlassSnapshot;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/** Exact-profile renderer for AE2 19.2.17 connected quartz glass. */
final class QuartzGlassRenderer implements BlockRenderer {

    private static final float SIXTEENTH = 1F / 16F;
    private static final Key VIBRANT_GLASS = Key.parse("ae2:quartz_vibrant_glass");

    private final ResourcePack resourcePack;
    private final TextureGallery textureGallery;
    private final RenderSettings renderSettings;
    private final ProfileActivation profileActivation;
    private final QuartzGlassRouteActivation glassActivation;
    private final ResourceModelRenderer stockRenderer;
    private final ResourceValidator resourceValidator;
    private Boolean resourcesSupported;

    QuartzGlassRenderer(
            ResourcePack resourcePack,
            TextureGallery textureGallery,
            RenderSettings renderSettings,
            ProfileActivation profileActivation,
            QuartzGlassRouteActivation glassActivation
    ) {
        this(
                resourcePack,
                textureGallery,
                renderSettings,
                profileActivation,
                glassActivation,
                M3cQuartzGlassResourceModels::resourcesSupported
        );
    }

    QuartzGlassRenderer(
            ResourcePack resourcePack,
            TextureGallery textureGallery,
            RenderSettings renderSettings,
            ProfileActivation profileActivation,
            QuartzGlassRouteActivation glassActivation,
            ResourceValidator resourceValidator
    ) {
        this.resourcePack = resourcePack;
        this.textureGallery = textureGallery;
        this.renderSettings = renderSettings;
        this.profileActivation = profileActivation;
        this.glassActivation = glassActivation;
        this.resourceValidator = resourceValidator;
        this.stockRenderer = new ResourceModelRenderer(
                resourcePack,
                textureGallery,
                renderSettings
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
        if (!profileActivation.isActive() || !glassActivation.isActive()) {
            renderOriginalSafely(block, tileModel, blockColor, renderStart);
            return;
        }

        try {
            BlockState center = block.getBlockState();
            if (!Ae2ResourceExtension.isExactQuartzGlassState(center)) {
                fallback(
                        BoundedDiagnostics.Event.QUARTZ_GLASS_UNSUPPORTED_BLOCK_STATE,
                        block,
                        tileModel,
                        blockColor,
                        renderStart
                );
                return;
            }

            NeighborhoodSnapshot neighborhood = snapshotNeighborhood(block, center);
            if (neighborhood == null) {
                fallback(
                        BoundedDiagnostics.Event.QUARTZ_GLASS_UNSUPPORTED_NEIGHBOR_DATA,
                        block,
                        tileModel,
                        blockColor,
                        renderStart
                );
                return;
            }
            if (!hasExactResources()) {
                fallback(
                        BoundedDiagnostics.Event.QUARTZ_GLASS_REQUIRED_RESOURCES_MISMATCH,
                        block,
                        tileModel,
                        blockColor,
                        renderStart
                );
                return;
            }

            RenderPlan plan = plan(block, center, neighborhood);
            if (plan == null) {
                fallback(
                        BoundedDiagnostics.Event.QUARTZ_GLASS_REQUIRED_RESOURCES_MISMATCH,
                        block,
                        tileModel,
                        blockColor,
                        renderStart
                );
                return;
            }
            emit(plan, tileModel, blockColor, renderStart);
        } catch (MaxCapacityReachedException exception) {
            throw exception;
        } catch (RuntimeException | LinkageError exception) {
            glassActivation.disable(
                    QuartzGlassRouteActivation.Reason.RENDER_CALLBACK_FAILED
            );
            fallback(
                    BoundedDiagnostics.Event.QUARTZ_GLASS_RENDER_FAILED,
                    block,
                    tileModel,
                    blockColor,
                    renderStart
            );
        }
    }

    private NeighborhoodSnapshot snapshotNeighborhood(
            BlockNeighborhood block,
            BlockState center
    ) {
        QuartzGlassSnapshot glass = QuartzGlassSnapshot.isolated(
                center.getId().getFormatted()
        );
        Map<Direction6, NeighborSnapshot> neighbors = new EnumMap<>(Direction6.class);
        for (Direction6 direction : Direction6.values()) {
            ExtendedBlock adjacent = neighbor(block, direction);
            BlockState state = adjacent.getBlockState();
            if (BlockState.MISSING.equals(state)) {
                return null;
            }
            boolean nativeGlassId = Ae2ResourceExtension.isQuartzGlassId(state.getId());
            if (nativeGlassId && !Ae2ResourceExtension.isExactQuartzGlassState(state)) {
                return null;
            }
            if (nativeGlassId) {
                glass = glass.withConnection(direction);
            }
            LightData light = adjacent.getLightData();
            neighbors.put(direction, new NeighborSnapshot(
                    state,
                    adjacent.getProperties().isCulling(),
                    adjacent.getProperties().getCullingIdentical(),
                    light.getSkyLight(),
                    light.getBlockLight()
            ));
        }
        return new NeighborhoodSnapshot(glass, Map.copyOf(neighbors));
    }

    private RenderPlan plan(
            BlockNeighborhood block,
            BlockState center,
            NeighborhoodSnapshot neighborhood
    ) {
        LightData centerLight = block.getLightData();
        int centerBlocklight = VIBRANT_GLASS.equals(center.getId())
                ? 15
                : centerLight.getBlockLight();
        List<PlannedQuad> quads = new ArrayList<>(12);
        for (QuartzGlassGeometry.Quad quad : QuartzGlassGeometry.forSnapshot(
                neighborhood.glass(),
                block.getX(),
                block.getY(),
                block.getZ()
        )) {
            NeighborSnapshot outward = neighborhood.neighbors().get(quad.face());
            if (cullsOutward(center, outward)
                    || (renderSettings.isRenderTopOnly() && quad.face() != Direction6.UP)) {
                continue;
            }
            LightLevels light = new LightLevels(
                    Math.max(centerLight.getSkyLight(), outward.sunlight()),
                    Math.max(centerBlocklight, outward.blocklight())
            );
            if (isHiddenCave(block, light)) {
                continue;
            }
            Key textureKey = M3cQuartzGlassResourceModels.texture(quad);
            Texture texture = resourcePack.getTextures().get(textureKey);
            if (texture == null) {
                return null;
            }
            quads.add(new PlannedQuad(
                    quad,
                    textureGallery.get(textureKey),
                    texture,
                    light
            ));
        }
        return new RenderPlan(List.copyOf(quads), mapColor(quads));
    }

    private void emit(
            RenderPlan plan,
            TileModelView tileModel,
            Color blockColor,
            int renderStart
    ) {
        int triangle = tileModel.add(plan.quads().size() * 2);
        TileModel model = tileModel.getTileModel();
        for (PlannedQuad planned : plan.quads()) {
            QuartzGlassGeometry.Quad quad = planned.quad();
            List<QuartzGlassGeometry.Vertex> vertices = quad.vertices();
            setTriangle(model, triangle, vertices.get(0), vertices.get(1), vertices.get(2));
            setAttributes(model, triangle, planned);
            triangle++;
            setTriangle(model, triangle, vertices.get(0), vertices.get(2), vertices.get(3));
            setAttributes(model, triangle, planned);
            triangle++;
        }
        blockColor.set(plan.mapColor());
        tileModel.initialize(renderStart);
    }

    private static void setAttributes(TileModel model, int triangle, PlannedQuad planned) {
        model.setMaterialIndex(triangle, planned.material());
        model.setColor(triangle, 1F, 1F, 1F);
        model.setSunlight(triangle, planned.light().sunlight());
        model.setBlocklight(triangle, planned.light().blocklight());
        model.setAOs(triangle, 1F, 1F, 1F);
    }

    private static void setTriangle(
            TileModel model,
            int triangle,
            QuartzGlassGeometry.Vertex first,
            QuartzGlassGeometry.Vertex second,
            QuartzGlassGeometry.Vertex third
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

    private Color mapColor(List<PlannedQuad> quads) {
        Color result = new Color().set(0F, 0F, 0F, 0F, true);
        float opacity = 0F;
        for (PlannedQuad planned : quads) {
            if (planned.quad().face() != Direction6.UP) {
                continue;
            }
            Color layer = new Color().set(planned.texture().getColorPremultiplied());
            float illumination = combinedLight(planned.light());
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

    private boolean hasExactResources() {
        if (resourcesSupported == null) {
            resourcesSupported = resourceValidator.resourcesSupported(resourcePack);
        }
        return resourcesSupported;
    }

    private static boolean cullsOutward(BlockState center, NeighborSnapshot outward) {
        return outward.culling()
                || (outward.cullingIdentical() && center.equals(outward.state()));
    }

    private float combinedLight(LightLevels light) {
        float combined = Math.max(
                light.sunlight() / 15F,
                light.blocklight() / 15F
        );
        return (1F - renderSettings.getAmbientLight()) * combined
                + renderSettings.getAmbientLight();
    }

    private boolean isHiddenCave(BlockNeighborhood block, LightLevels light) {
        return block.isRemoveIfCave()
                && (renderSettings.isCaveDetectionUsesBlockLight()
                        ? Math.max(light.blocklight(), light.sunlight())
                        : light.sunlight()) == 0;
    }

    private void fallback(
            BoundedDiagnostics.Event event,
            BlockNeighborhood block,
            TileModelView tileModel,
            Color blockColor,
            int renderStart
    ) {
        BoundedDiagnostics.report(event);
        tileModel.initialize(renderStart).reset();
        blockColor.set(0F, 0F, 0F, 0F, true);
        renderOriginalSafely(block, tileModel, blockColor, renderStart);
    }

    private void renderOriginalSafely(
            BlockNeighborhood block,
            TileModelView tileModel,
            Color blockColor,
            int renderStart
    ) {
        try {
            de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.BlockState original =
                    resourcePack.getBlockStates().get(block.getBlockState().getId());
            if (renderResource(original, block.getBlockState(), block, tileModel, blockColor)) {
                return;
            }
            de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.BlockState missing =
                    resourcePack.getBlockStates().get(ResourcePack.MISSING_BLOCK_STATE);
            renderResource(missing, BlockState.MISSING, block, tileModel, blockColor);
        } catch (MaxCapacityReachedException exception) {
            throw exception;
        } catch (RuntimeException | LinkageError exception) {
            tileModel.initialize(renderStart).reset();
            blockColor.set(0F, 0F, 0F, 0F, true);
            BoundedDiagnostics.report(BoundedDiagnostics.Event.QUARTZ_GLASS_RENDER_FAILED);
        }
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
        int modelStart = tileModel.getStart();
        List<Variant> variants = new ArrayList<>();
        resource.forEach(state, block.getX(), block.getY(), block.getZ(), variants::add);
        if (variants.isEmpty()) {
            return false;
        }

        float colorOpacity = 0F;
        blockColor.set(0F, 0F, 0F, 0F, true);
        boolean rendered = false;
        for (Variant variant : variants) {
            if (variant.getModel().getResource(resourcePack.getModels()::get) == null) {
                continue;
            }
            Color variantColor = new Color().set(0F, 0F, 0F, 0F, true);
            stockRenderer.render(block, variant, tileModel.initialize(), variantColor);
            rendered = true;
            colorOpacity = Math.max(colorOpacity, variantColor.a);
            blockColor.add(variantColor.premultiplied());
        }
        if (!rendered) {
            tileModel.initialize(modelStart);
            return false;
        }
        if (blockColor.a > 0F) {
            blockColor.flatten().straight();
            blockColor.a = colorOpacity;
        }
        tileModel.initialize(modelStart);
        return true;
    }

    private static ExtendedBlock neighbor(BlockNeighborhood block, Direction6 direction) {
        return block.getNeighborBlock(
                direction.stepX(),
                direction.stepY(),
                direction.stepZ()
        );
    }

    private static float units(double value16) {
        return (float) value16 * SIXTEENTH;
    }

    @FunctionalInterface
    interface ResourceValidator {
        boolean resourcesSupported(ResourcePack resourcePack);
    }

    private record NeighborhoodSnapshot(
            QuartzGlassSnapshot glass,
            Map<Direction6, NeighborSnapshot> neighbors
    ) {
    }

    private record NeighborSnapshot(
            BlockState state,
            boolean culling,
            boolean cullingIdentical,
            int sunlight,
            int blocklight
    ) {
    }

    private record LightLevels(int sunlight, int blocklight) {
    }

    private record PlannedQuad(
            QuartzGlassGeometry.Quad quad,
            int material,
            Texture texture,
            LightLevels light
    ) {
    }

    private record RenderPlan(List<PlannedQuad> quads, Color mapColor) {
    }
}
