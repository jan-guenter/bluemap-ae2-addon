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
import io.github.janguenter.bluemap.ae2.activation.CraftingRouteActivation;
import io.github.janguenter.bluemap.ae2.activation.ProfileActivation;
import io.github.janguenter.bluemap.ae2.diagnostics.BoundedDiagnostics;
import io.github.janguenter.bluemap.ae2.model.CableColor;
import io.github.janguenter.bluemap.ae2.model.CraftingBlockKind;
import io.github.janguenter.bluemap.ae2.model.CraftingGeometry;
import io.github.janguenter.bluemap.ae2.model.CraftingSnapshot;
import io.github.janguenter.bluemap.ae2.model.Direction6;
import io.github.janguenter.bluemap.ae2.profile.Ae219217CraftingProfile;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Exact-profile renderer for AE2 19.2.17 formed crafting-cube blocks. */
final class CraftingRenderer implements BlockRenderer {

    private static final float SIXTEENTH = 1F / 16F;

    private final ResourcePack resourcePack;
    private final TextureGallery textureGallery;
    private final RenderSettings renderSettings;
    private final ProfileActivation profileActivation;
    private final CraftingRouteActivation craftingActivation;
    private final ResourceModelRenderer stockRenderer;
    private final ResourceValidator resourceValidator;
    private final DiagnosticReporter diagnosticReporter;
    private final M45CraftingNeighborAccess extensionNeighbors;
    private Boolean resourcesSupported;

    CraftingRenderer(
            ResourcePack resourcePack,
            TextureGallery textureGallery,
            RenderSettings renderSettings,
            ProfileActivation profileActivation,
            CraftingRouteActivation craftingActivation
    ) {
        this(
                resourcePack,
                textureGallery,
                renderSettings,
                profileActivation,
                craftingActivation,
                M3dCraftingResourceModels::resourcesSupported,
                BoundedDiagnostics::report,
                new M45CraftingNeighborAccess(M45Adapter.runtime())
        );
    }

    CraftingRenderer(
            ResourcePack resourcePack,
            TextureGallery textureGallery,
            RenderSettings renderSettings,
            ProfileActivation profileActivation,
            CraftingRouteActivation craftingActivation,
            ResourceValidator resourceValidator
    ) {
        this(
                resourcePack,
                textureGallery,
                renderSettings,
                profileActivation,
                craftingActivation,
                resourceValidator,
                BoundedDiagnostics::report,
                new M45CraftingNeighborAccess(new M45Runtime())
        );
    }

    CraftingRenderer(
            ResourcePack resourcePack,
            TextureGallery textureGallery,
            RenderSettings renderSettings,
            ProfileActivation profileActivation,
            CraftingRouteActivation craftingActivation,
            ResourceValidator resourceValidator,
            DiagnosticReporter diagnosticReporter
    ) {
        this(
                resourcePack,
                textureGallery,
                renderSettings,
                profileActivation,
                craftingActivation,
                resourceValidator,
                diagnosticReporter,
                new M45CraftingNeighborAccess(new M45Runtime())
        );
    }

    CraftingRenderer(
            ResourcePack resourcePack,
            TextureGallery textureGallery,
            RenderSettings renderSettings,
            ProfileActivation profileActivation,
            CraftingRouteActivation craftingActivation,
            ResourceValidator resourceValidator,
            DiagnosticReporter diagnosticReporter,
            M45CraftingNeighborAccess extensionNeighbors
    ) {
        this.resourcePack = resourcePack;
        this.textureGallery = textureGallery;
        this.renderSettings = renderSettings;
        this.profileActivation = profileActivation;
        this.craftingActivation = craftingActivation;
        this.resourceValidator = resourceValidator;
        this.diagnosticReporter = diagnosticReporter;
        this.extensionNeighbors = java.util.Objects.requireNonNull(
                extensionNeighbors,
                "extensionNeighbors"
        );
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
        if (!profileActivation.isActive() || !craftingActivation.isActive()) {
            renderOriginalSafely(block, tileModel, blockColor, renderStart);
            return;
        }

        try {
            BlockState center = block.getBlockState();
            if (!Ae2ResourceExtension.isExactCraftingNeighborState(center)) {
                fallback(
                        BoundedDiagnostics.Event.CRAFTING_UNSUPPORTED_BLOCK_STATE,
                        block,
                        tileModel,
                        blockColor,
                        renderStart
                );
                return;
            }
            if (!Ae2ResourceExtension.isExactFormedCraftingState(center)) {
                // Unformed blocks keep their exact ordinary JSON model. They
                // are not an error and must not consume a fallback diagnostic.
                renderOriginalSafely(block, tileModel, blockColor, renderStart);
                return;
            }

            CenterSnapshot centerSnapshot = snapshotCenter(block, center);
            if (centerSnapshot == null) {
                fallback(
                        BoundedDiagnostics.Event.CRAFTING_MALFORMED_BLOCK_DATA,
                        block,
                        tileModel,
                        blockColor,
                        renderStart
                );
                return;
            }
            NeighborhoodResult neighborhood = snapshotNeighborhood(block);
            if (!neighborhood.supported()) {
                fallback(
                        neighborhood.status() == NeighborhoodStatus.UNSUPPORTED_COMPATIBLE
                                ? BoundedDiagnostics.Event
                                        .CRAFTING_UNSUPPORTED_COMPATIBLE_NEIGHBOR
                                : BoundedDiagnostics.Event
                                        .CRAFTING_UNSUPPORTED_NEIGHBOR_DATA,
                        block,
                        tileModel,
                        blockColor,
                        renderStart
                );
                return;
            }
            if (!hasExactResources()) {
                fallback(
                        BoundedDiagnostics.Event.CRAFTING_REQUIRED_RESOURCES_MISMATCH,
                        block,
                        tileModel,
                        blockColor,
                        renderStart
                );
                return;
            }

            CraftingSnapshot snapshot = new CraftingSnapshot(
                    centerSnapshot.kind(),
                    centerSnapshot.powered(),
                    centerSnapshot.facing(),
                    centerSnapshot.spin(),
                    centerSnapshot.paintedColor(),
                    neighborhood.connections()
            );
            RenderPlan plan = plan(block, center, snapshot, neighborhood.neighbors());
            if (plan == null) {
                fallback(
                        BoundedDiagnostics.Event.CRAFTING_REQUIRED_RESOURCES_MISMATCH,
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
            craftingActivation.disable(
                    CraftingRouteActivation.Reason.RENDER_CALLBACK_FAILED
            );
            fallback(
                    BoundedDiagnostics.Event.CRAFTING_RENDER_FAILED,
                    block,
                    tileModel,
                    blockColor,
                    renderStart
            );
        }
    }

    private static CenterSnapshot snapshotCenter(
            BlockNeighborhood block,
            BlockState center
    ) {
        CraftingBlockKind kind = Ae219217CraftingProfile.kindForBlock(
                center.getId().getFormatted()
        );
        if (kind == null) {
            return null;
        }
        boolean powered = Boolean.parseBoolean(center.getProperties().get("powered"));
        if (kind != CraftingBlockKind.MONITOR) {
            return new CenterSnapshot(
                    kind,
                    powered,
                    Direction6.NORTH,
                    0,
                    CableColor.TRANSPARENT
            );
        }

        Ae2CraftingMonitorBlockEntityData data = block.getBlockEntity()
                instanceof Ae2CraftingMonitorBlockEntityData monitor ? monitor : null;
        if (data == null) {
            return null;
        }
        String facingValue = center.getProperties().get("facing");
        String spinValue = center.getProperties().get("spin");
        return new CenterSnapshot(
                kind,
                powered,
                Direction6.valueOf(facingValue.toUpperCase(Locale.ROOT)),
                Integer.parseInt(spinValue),
                data.getPaintedColor()
        );
    }

    private NeighborhoodResult snapshotNeighborhood(BlockNeighborhood block) {
        EnumSet<Direction6> connections = EnumSet.noneOf(Direction6.class);
        Map<Direction6, NeighborSnapshot> neighbors = new EnumMap<>(Direction6.class);
        for (Direction6 direction : Direction6.values()) {
            ExtendedBlock adjacent = neighbor(block, direction);
            BlockState state = adjacent.getBlockState();
            if (BlockState.MISSING.equals(state)) {
                return NeighborhoodResult.rejected(NeighborhoodStatus.MALFORMED_OR_MISSING);
            }
            String blockId = state.getId().getFormatted();
            if (Ae219217CraftingProfile.unsupportedCompatibleConnectorIds()
                    .contains(blockId)) {
                if (!extensionNeighbors.isExactActiveNeighbor(state)) {
                    return NeighborhoodResult.rejected(
                            NeighborhoodStatus.UNSUPPORTED_COMPATIBLE
                    );
                }
                connections.add(direction);
            } else if (Ae219217CraftingProfile.kindForBlock(blockId) != null) {
                if (!Ae2ResourceExtension.isExactCraftingNeighborState(state)) {
                    return NeighborhoodResult.rejected(
                            NeighborhoodStatus.MALFORMED_OR_MISSING
                    );
                }
                connections.add(direction);
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
        return NeighborhoodResult.supported(connections, neighbors);
    }

    private RenderPlan plan(
            BlockNeighborhood block,
            BlockState center,
            CraftingSnapshot snapshot,
            Map<Direction6, NeighborSnapshot> neighbors
    ) {
        LightData centerLight = block.getLightData();
        List<PlannedQuad> quads = new ArrayList<>(60);
        for (CraftingGeometry.Quad quad : CraftingGeometry.forSnapshot(snapshot)) {
            NeighborSnapshot outward = neighbors.get(quad.face());
            if (cullsOutward(center, outward)
                    || (renderSettings.isRenderTopOnly() && quad.face() != Direction6.UP)) {
                continue;
            }
            LightLevels worldLight = new LightLevels(
                    Math.max(centerLight.getSkyLight(), outward.sunlight()),
                    Math.max(centerLight.getBlockLight(), outward.blocklight())
            );
            if (isHiddenCave(block, worldLight)) {
                continue;
            }
            LightLevels emittedLight = quad.layer().emissiveWhenPowered()
                    && snapshot.powered() ? new LightLevels(15, 15) : worldLight;
            Key textureKey = M3dCraftingResourceModels.texture(quad);
            Texture texture = resourcePack.getTextures().get(textureKey);
            if (texture == null) {
                return null;
            }
            Tint tint = tint(quad.layer().tint(), snapshot.paintedColor());
            quads.add(new PlannedQuad(
                    quad,
                    textureGallery.get(textureKey),
                    texture,
                    emittedLight,
                    tint
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
            List<CraftingGeometry.Vertex> vertices = planned.quad().vertices();
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
        model.setColor(triangle, planned.tint().red(), planned.tint().green(), planned.tint().blue());
        model.setSunlight(triangle, planned.light().sunlight());
        model.setBlocklight(triangle, planned.light().blocklight());
        model.setAOs(triangle, 1F, 1F, 1F);
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

    private Color mapColor(List<PlannedQuad> quads) {
        Color result = new Color().set(0F, 0F, 0F, 0F, true);
        float opacity = 0F;
        for (PlannedQuad planned : quads) {
            if (planned.quad().face() != Direction6.UP) {
                continue;
            }
            Color layer = new Color().set(planned.texture().getColorPremultiplied());
            layer.r *= planned.tint().red();
            layer.g *= planned.tint().green();
            layer.b *= planned.tint().blue();
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
            diagnosticReporter.report(BoundedDiagnostics.Event.CRAFTING_RENDER_FAILED);
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

    private static Tint tint(CraftingGeometry.Tint tint, CableColor color) {
        int rgb = switch (tint) {
            case NONE -> 0xFFFFFF;
            case BRIGHT -> color.brightRgb();
            case MEDIUM -> color.mediumRgb();
            case DARK -> color.darkRgb();
        };
        return new Tint(
                ((rgb >>> 16) & 0xFF) / 255F,
                ((rgb >>> 8) & 0xFF) / 255F,
                (rgb & 0xFF) / 255F
        );
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

    @FunctionalInterface
    interface DiagnosticReporter {
        void report(BoundedDiagnostics.Event event);
    }

    private enum NeighborhoodStatus {
        SUPPORTED,
        MALFORMED_OR_MISSING,
        UNSUPPORTED_COMPATIBLE
    }

    private record CenterSnapshot(
            CraftingBlockKind kind,
            boolean powered,
            Direction6 facing,
            int spin,
            CableColor paintedColor
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

    private record NeighborhoodResult(
            NeighborhoodStatus status,
            java.util.Set<Direction6> connections,
            Map<Direction6, NeighborSnapshot> neighbors
    ) {

        private static NeighborhoodResult supported(
                java.util.Set<Direction6> connections,
                Map<Direction6, NeighborSnapshot> neighbors
        ) {
            return new NeighborhoodResult(
                    NeighborhoodStatus.SUPPORTED,
                    java.util.Set.copyOf(connections),
                    Map.copyOf(neighbors)
            );
        }

        private static NeighborhoodResult rejected(NeighborhoodStatus status) {
            return new NeighborhoodResult(status, java.util.Set.of(), Map.of());
        }

        private boolean supported() {
            return status == NeighborhoodStatus.SUPPORTED;
        }
    }

    private record LightLevels(int sunlight, int blocklight) {
    }

    private record Tint(float red, float green, float blue) {
    }

    private record PlannedQuad(
            CraftingGeometry.Quad quad,
            int material,
            Texture texture,
            LightLevels light,
            Tint tint
    ) {
    }

    private record RenderPlan(List<PlannedQuad> quads, Color mapColor) {
    }
}
