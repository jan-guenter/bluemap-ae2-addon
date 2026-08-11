/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.adapter.bluemap522;

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
import io.github.janguenter.bluemap.ae2.activation.QuantumBridgeRouteActivation;
import io.github.janguenter.bluemap.ae2.diagnostics.BoundedDiagnostics;
import io.github.janguenter.bluemap.ae2.model.Direction6;
import io.github.janguenter.bluemap.ae2.model.QuantumBridgeGeometry;
import io.github.janguenter.bluemap.ae2.model.QuantumBridgeSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Exact-profile renderer for AE2 19.2.17 settled native quantum bridges. */
final class QuantumBridgeRenderer implements BlockRenderer {

    private final ResourcePack resourcePack;
    private final TextureGallery textureGallery;
    private final RenderSettings renderSettings;
    private final ProfileActivation profileActivation;
    private final QuantumBridgeRouteActivation quantumBridgeActivation;
    private final ResourceModelRenderer stockRenderer;
    private final ResourceValidator resourceValidator;
    private final DiagnosticReporter diagnosticReporter;
    private Boolean resourcesSupported;

    QuantumBridgeRenderer(
            ResourcePack resourcePack,
            TextureGallery textureGallery,
            RenderSettings renderSettings,
            ProfileActivation profileActivation,
            QuantumBridgeRouteActivation quantumBridgeActivation
    ) {
        this(
                resourcePack,
                textureGallery,
                renderSettings,
                profileActivation,
                quantumBridgeActivation,
                M3eQuantumBridgeResourceModels::resourcesSupported,
                BoundedDiagnostics::report
        );
    }

    QuantumBridgeRenderer(
            ResourcePack resourcePack,
            TextureGallery textureGallery,
            RenderSettings renderSettings,
            ProfileActivation profileActivation,
            QuantumBridgeRouteActivation quantumBridgeActivation,
            ResourceValidator resourceValidator,
            DiagnosticReporter diagnosticReporter
    ) {
        this.resourcePack = resourcePack;
        this.textureGallery = textureGallery;
        this.renderSettings = renderSettings;
        this.profileActivation = profileActivation;
        this.quantumBridgeActivation = quantumBridgeActivation;
        this.resourceValidator = resourceValidator;
        this.diagnosticReporter = diagnosticReporter;
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
        if (!profileActivation.isActive() || !quantumBridgeActivation.isActive()) {
            renderOriginalSafely(block, tileModel, blockColor, renderStart);
            return;
        }

        try {
            BlockState center = block.getBlockState();
            if (!Ae2ResourceExtension.isExactQuantumBridgeState(center)) {
                fallback(
                        BoundedDiagnostics.Event.QUANTUM_BRIDGE_UNSUPPORTED_BLOCK_STATE,
                        block,
                        tileModel,
                        blockColor,
                        renderStart
                );
                return;
            }
            if (!Ae2ResourceExtension.isExactFormedQuantumBridgeState(center)) {
                // Unformed blocks retain their ordinary source resource.
                renderOriginalSafely(block, tileModel, blockColor, renderStart);
                return;
            }

            QuantumBridgeTopologyResolver.Result topology =
                    QuantumBridgeTopologyResolver.resolve(block, center);
            if (!topology.supported()) {
                fallback(
                        topology.status()
                                == QuantumBridgeTopologyResolver.Status
                                        .NEIGHBOR_UNAVAILABLE
                                ? BoundedDiagnostics.Event
                                        .QUANTUM_BRIDGE_UNSUPPORTED_NEIGHBOR_DATA
                                : BoundedDiagnostics.Event
                                        .QUANTUM_BRIDGE_INVALID_TOPOLOGY,
                        block,
                        tileModel,
                        blockColor,
                        renderStart
                );
                return;
            }
            if (!hasExactResources()) {
                fallback(
                        BoundedDiagnostics.Event
                                .QUANTUM_BRIDGE_REQUIRED_RESOURCES_MISMATCH,
                        block,
                        tileModel,
                        blockColor,
                        renderStart
                );
                return;
            }

            RenderPlan plan = plan(block, topology.snapshot(), topology.directNeighbors());
            if (plan == null) {
                fallback(
                        BoundedDiagnostics.Event
                                .QUANTUM_BRIDGE_REQUIRED_RESOURCES_MISMATCH,
                        block,
                        tileModel,
                        blockColor,
                        renderStart
                );
                return;
            }
            emit(plan, block, tileModel, blockColor, renderStart);
        } catch (MaxCapacityReachedException exception) {
            throw exception;
        } catch (RuntimeException | LinkageError exception) {
            quantumBridgeActivation.disable(
                    QuantumBridgeRouteActivation.Reason.RENDER_CALLBACK_FAILED
            );
            fallback(
                    BoundedDiagnostics.Event.QUANTUM_BRIDGE_RENDER_FAILED,
                    block,
                    tileModel,
                    blockColor,
                    renderStart
            );
        }
    }

    private RenderPlan plan(
            BlockNeighborhood block,
            QuantumBridgeSnapshot snapshot,
            Map<Direction6, ExtendedBlock> directNeighbors
    ) {
        LightData centerLight = block.getLightData();
        List<PlannedQuad> quads = new ArrayList<>(54);
        for (QuantumBridgeGeometry.Quad quad : QuantumBridgeGeometry.forSnapshot(snapshot)) {
            if (renderSettings.isRenderTopOnly() && quad.face() != Direction6.UP) {
                continue;
            }
            LightData outward = directNeighbors.get(quad.face()).getLightData();
            LightLevels light = new LightLevels(
                    Math.max(centerLight.getSkyLight(), outward.getSkyLight()),
                    Math.max(centerLight.getBlockLight(), outward.getBlockLight())
            );
            if (isHiddenCave(block, light)) {
                continue;
            }
            Key textureKey = M3eQuantumBridgeResourceModels.texture(quad);
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
            BlockNeighborhood block,
            TileModelView tileModel,
            Color blockColor,
            int renderStart
    ) {
        int triangle = tileModel.add(plan.quads().size() * 2);
        TileModel model = tileModel.getTileModel();
        for (PlannedQuad planned : plan.quads()) {
            List<QuantumBridgeGeometry.Vertex> vertices = planned.quad().vertices();
            setTriangle(model, triangle, vertices.get(0), vertices.get(1), vertices.get(2));
            setAttributes(
                    model,
                    triangle,
                    planned,
                    testAo(vertices.get(0), planned.quad().face(), block),
                    testAo(vertices.get(1), planned.quad().face(), block),
                    testAo(vertices.get(2), planned.quad().face(), block)
            );
            triangle++;
            setTriangle(model, triangle, vertices.get(0), vertices.get(2), vertices.get(3));
            setAttributes(
                    model,
                    triangle,
                    planned,
                    testAo(vertices.get(0), planned.quad().face(), block),
                    testAo(vertices.get(2), planned.quad().face(), block),
                    testAo(vertices.get(3), planned.quad().face(), block)
            );
            triangle++;
        }
        blockColor.set(plan.mapColor());
        tileModel.initialize(renderStart);
    }

    private static void setAttributes(
            TileModel model,
            int triangle,
            PlannedQuad planned,
            float firstAo,
            float secondAo,
            float thirdAo
    ) {
        model.setMaterialIndex(triangle, planned.material());
        model.setColor(triangle, 1F, 1F, 1F);
        model.setSunlight(triangle, planned.light().sunlight());
        model.setBlocklight(triangle, planned.light().blocklight());
        model.setAOs(triangle, firstAo, secondAo, thirdAo);
    }

    private static void setTriangle(
            TileModel model,
            int triangle,
            QuantumBridgeGeometry.Vertex first,
            QuantumBridgeGeometry.Vertex second,
            QuantumBridgeGeometry.Vertex third
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

    private static float testAo(
            QuantumBridgeGeometry.Vertex vertex,
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

    static int boundaryOffset(double coordinate16) {
        if (coordinate16 >= 16D) {
            return 1;
        }
        if (coordinate16 <= 0D) {
            return -1;
        }
        return 0;
    }

    private void fallback(
            BoundedDiagnostics.Event event,
            BlockNeighborhood block,
            TileModelView tileModel,
            Color blockColor,
            int renderStart
    ) {
        diagnosticReporter.report(event);
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
            diagnosticReporter.report(BoundedDiagnostics.Event.QUANTUM_BRIDGE_RENDER_FAILED);
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
        return neighbor(
                block,
                direction.stepX(),
                direction.stepY(),
                direction.stepZ()
        );
    }

    private static ExtendedBlock neighbor(
            BlockNeighborhood block,
            int x,
            int y,
            int z
    ) {
        return block.getNeighborBlock(x, y, z);
    }

    private static float units(double value16) {
        return (float) value16 / 16F;
    }

    @FunctionalInterface
    interface ResourceValidator {
        boolean resourcesSupported(ResourcePack resourcePack);
    }

    @FunctionalInterface
    interface DiagnosticReporter {
        void report(BoundedDiagnostics.Event event);
    }

    private record LightLevels(int sunlight, int blocklight) {
    }

    private record PlannedQuad(
            QuantumBridgeGeometry.Quad quad,
            int material,
            Texture texture,
            LightLevels light
    ) {
    }

    private record RenderPlan(List<PlannedQuad> quads, Color mapColor) {
    }
}
