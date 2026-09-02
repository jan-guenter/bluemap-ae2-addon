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
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.Variant;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.texture.Texture;
import de.bluecolored.bluemap.core.util.Key;
import de.bluecolored.bluemap.core.util.math.Color;
import de.bluecolored.bluemap.core.util.math.MatrixM4f;
import de.bluecolored.bluemap.core.world.BlockEntity;
import de.bluecolored.bluemap.core.world.BlockState;
import de.bluecolored.bluemap.core.world.LightData;
import de.bluecolored.bluemap.core.world.block.BlockNeighborhood;
import de.bluecolored.bluemap.core.world.block.ExtendedBlock;
import io.github.janguenter.bluemap.ae2.activation.M3CompletionRouteActivation;
import io.github.janguenter.bluemap.ae2.activation.ProfileActivation;
import io.github.janguenter.bluemap.ae2.diagnostics.BoundedDiagnostics;
import io.github.janguenter.bluemap.ae2.model.Direction6;
import io.github.janguenter.bluemap.ae2.model.M3CompletionBlockKind;
import io.github.janguenter.bluemap.ae2.model.MachineGeometry;
import io.github.janguenter.bluemap.ae2.model.PaintGeometry;
import io.github.janguenter.bluemap.ae2.model.PaintSnapshot;
import io.github.janguenter.bluemap.ae2.model.PartOrientation;
import io.github.janguenter.bluemap.ae2.model.SkyChestGeometry;
import io.github.janguenter.bluemap.ae2.model.SpatialPylonGeometry;
import io.github.janguenter.bluemap.ae2.model.SpatialPylonSnapshot;
import io.github.janguenter.bluemap.ae2.profile.Ae219217M3CompletionProfile;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/** One exact-profile dispatcher for the bounded AE2 19.2.17 M3 completion route. */
final class M3CompletionRenderer implements BlockRenderer {

    private static final float SIXTEENTH = 1F / 16F;
    private static final int CRANK_MAX_TRIANGLES = 34;
    private static final int INSCRIBER_MAX_TRIANGLES = 78;
    private static final MatrixM4f IDENTITY = new MatrixM4f().identity();
    private static final Set<String> AIR_IDS = Set.of(
            "minecraft:air",
            "minecraft:cave_air",
            "minecraft:void_air"
    );

    private final ResourcePack resourcePack;
    private final TextureGallery textureGallery;
    private final RenderSettings renderSettings;
    private final ProfileActivation profileActivation;
    private final M3CompletionRouteActivation completionActivation;
    private final DriveRenderSupport renderSupport;
    private final ResourceValidator resourceValidator;
    private final DiagnosticReporter diagnosticReporter;
    private Boolean resourcesSupported;

    M3CompletionRenderer(
            ResourcePack resourcePack,
            TextureGallery textureGallery,
            RenderSettings renderSettings,
            ProfileActivation profileActivation,
            M3CompletionRouteActivation completionActivation
    ) {
        this(
                resourcePack,
                textureGallery,
                renderSettings,
                profileActivation,
                completionActivation,
                M3CompletionResourceModels::resourcesSupported,
                BoundedDiagnostics::report
        );
    }

    M3CompletionRenderer(
            ResourcePack resourcePack,
            TextureGallery textureGallery,
            RenderSettings renderSettings,
            ProfileActivation profileActivation,
            M3CompletionRouteActivation completionActivation,
            ResourceValidator resourceValidator,
            DiagnosticReporter diagnosticReporter
    ) {
        this.resourcePack = resourcePack;
        this.textureGallery = textureGallery;
        this.renderSettings = renderSettings;
        this.profileActivation = profileActivation;
        this.completionActivation = completionActivation;
        this.resourceValidator = resourceValidator;
        this.diagnosticReporter = diagnosticReporter;
        this.renderSupport = new DriveRenderSupport(
                resourcePack,
                textureGallery,
                renderSettings,
                Key.parse("ae2:block/crank")
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
        if (!profileActivation.isActive() || !completionActivation.isActive()) {
            renderOriginalSafely(block, tileModel, blockColor, renderStart);
            return;
        }

        try {
            M3CompletionBlockKind kind = exactKind(block.getBlockState());
            if (kind == null) {
                fallback(
                        BoundedDiagnostics.Event.M3_COMPLETION_UNSUPPORTED_BLOCK_STATE,
                        block,
                        tileModel,
                        blockColor,
                        renderStart
                );
                return;
            }
            if (!hasExactResources()) {
                fallback(
                        BoundedDiagnostics.Event.M3_COMPLETION_REQUIRED_RESOURCES_MISMATCH,
                        block,
                        tileModel,
                        blockColor,
                        renderStart
                );
                return;
            }

            RenderStatus status = switch (kind) {
                case PAINT -> renderPaint(block, tileModel, blockColor, renderStart);
                case SKY_STONE_CHEST, SMOOTH_SKY_STONE_CHEST ->
                        renderChest(kind, block, tileModel, blockColor, renderStart);
                case CRANK -> renderCrank(block, tileModel, blockColor, renderStart);
                case INSCRIBER -> renderInscriber(block, tileModel, blockColor, renderStart);
                case SPATIAL_PYLON -> renderSpatialPylon(
                        block, tileModel, blockColor, renderStart
                );
            };
            if (status != RenderStatus.RENDERED) {
                fallback(eventFor(status), block, tileModel, blockColor, renderStart);
            }
        } catch (MaxCapacityReachedException exception) {
            throw exception;
        } catch (RuntimeException | LinkageError exception) {
            completionActivation.disable(
                    M3CompletionRouteActivation.Reason.RENDER_CALLBACK_FAILED
            );
            fallback(
                    BoundedDiagnostics.Event.M3_COMPLETION_RENDER_FAILED,
                    block,
                    tileModel,
                    blockColor,
                    renderStart
            );
        }
    }

    private RenderStatus renderPaint(
            BlockNeighborhood block,
            TileModelView tileModel,
            Color blockColor,
            int renderStart
    ) {
        Ae2PaintBlockEntityData data = block.getBlockEntity()
                instanceof Ae2PaintBlockEntityData paint ? paint : null;
        if (data == null || data.getPaint() == null) {
            return RenderStatus.MALFORMED;
        }
        PaintSnapshot snapshot = data.getPaint();
        String lightLevel = block.getBlockState().getProperties().get("light_level");
        if (!Integer.toString(snapshot.expectedLightLevelProperty()).equals(lightLevel)) {
            return RenderStatus.MALFORMED;
        }
        for (var splotch : snapshot.splotches()) {
            ExtendedBlock backing = neighbor(block, splotch.backingSide());
            ExtendedBlock visible = neighbor(block, splotch.visibleFace());
            if (missing(backing) || missing(visible)
                    || AIR_IDS.contains(backing.getBlockState().getId().getFormatted())) {
                return RenderStatus.NEIGHBOR_UNAVAILABLE;
            }
        }

        List<PrimitiveQuad> primitives = new ArrayList<>(snapshot.splotches().size());
        for (PaintGeometry.Quad quad : PaintGeometry.forSnapshot(snapshot)) {
            primitives.add(new PrimitiveQuad(
                    quad.face(),
                    M3CompletionResourceModels.paintTexture(quad),
                    quad.rgb(),
                    quad.emissive(),
                    quad.vertices().stream().map(vertex -> new Vertex(
                            vertex.x16(), vertex.y16(), vertex.z16(),
                            vertex.u16(), vertex.v16()
                    )).toList()
            ));
        }
        return emitPrimitives(
                primitives, IDENTITY, block, tileModel, blockColor, renderStart
        ) ? RenderStatus.RENDERED : RenderStatus.RESOURCES_MISSING;
    }

    private RenderStatus renderChest(
            M3CompletionBlockKind kind,
            BlockNeighborhood block,
            TileModelView tileModel,
            Color blockColor,
            int renderStart
    ) {
        if (!hasBlockEntity(block, Ae219217M3CompletionProfile.SKY_STONE_CHEST_BLOCK_ENTITY_ID)) {
            return RenderStatus.MALFORMED;
        }
        Direction6 facing = facing(block.getBlockState());
        if (facing == null || facing == Direction6.UP || facing == Direction6.DOWN) {
            return RenderStatus.MALFORMED;
        }
        String blockId = kind == M3CompletionBlockKind.SKY_STONE_CHEST
                ? Ae219217M3CompletionProfile.SKY_STONE_CHEST_BLOCK
                : Ae219217M3CompletionProfile.SMOOTH_SKY_STONE_CHEST_BLOCK;
        Key texture = M3CompletionResourceModels.chestTexture(blockId);
        List<PrimitiveQuad> primitives = SkyChestGeometry.closed().stream()
                .map(quad -> new PrimitiveQuad(
                        quad.face(),
                        texture,
                        0xffffff,
                        false,
                        quad.vertices().stream().map(vertex -> new Vertex(
                                vertex.x16(), vertex.y16(), vertex.z16(),
                                vertex.u16(), vertex.v16()
                        )).toList()
                )).toList();
        MatrixM4f orientation = orientation(chestOrientation(facing));
        return emitPrimitives(
                primitives, orientation, block, tileModel, blockColor, renderStart
        ) ? RenderStatus.RENDERED : RenderStatus.RESOURCES_MISSING;
    }

    private RenderStatus renderCrank(
            BlockNeighborhood block,
            TileModelView tileModel,
            Color blockColor,
            int renderStart
    ) {
        if (!hasBlockEntity(block, Ae219217M3CompletionProfile.CRANK_BLOCK_ENTITY_ID)) {
            return RenderStatus.MALFORMED;
        }
        Direction6 facing = facing(block.getBlockState());
        if (facing == null) {
            return RenderStatus.MALFORMED;
        }
        PartOrientation orientation = PartOrientation.forPart(facing, 0);
        ReservedTileModel reserved = new ReservedTileModel(
                tileModel.getTileModel(),
                CRANK_MAX_TRIANGLES
        );
        try {
            float opacity = 0F;
            blockColor.set(0F, 0F, 0F, 0F, true);
            Color base = renderSupport.renderModelPreservingAmbientOcclusion(
                    block, M3CompletionResourceModels.CRANK_BASE, orientation,
                    reserved
            );
            opacity = DriveRenderSupport.addMapColor(blockColor, base, opacity);
            Color handle = renderSupport.renderModelPreservingAmbientOcclusion(
                    block, M3CompletionResourceModels.CRANK_HANDLE, orientation,
                    reserved
            );
            opacity = DriveRenderSupport.addMapColor(blockColor, handle, opacity);
            DriveRenderSupport.finishMapColor(blockColor, opacity);
            reserved.commit();
            tileModel.initialize(renderStart);
            return RenderStatus.RENDERED;
        } catch (RuntimeException | LinkageError exception) {
            reserved.rollback();
            throw exception;
        }
    }

    private RenderStatus renderInscriber(
            BlockNeighborhood block,
            TileModelView tileModel,
            Color blockColor,
            int renderStart
    ) {
        if (!hasBlockEntity(block, Ae219217M3CompletionProfile.INSCRIBER_BLOCK_ENTITY_ID)) {
            return RenderStatus.MALFORMED;
        }
        Direction6 facing = facing(block.getBlockState());
        int spin = spin(block.getBlockState());
        if (facing == null || spin < 0 || spin > 3) {
            return RenderStatus.MALFORMED;
        }
        PartOrientation orientation = PartOrientation.forPart(facing, spin);
        MatrixM4f matrix = orientation(orientation);
        List<PrimitiveQuad> stamps = MachineGeometry.neutralInscriberStamps().stream()
                .map(quad -> new PrimitiveQuad(
                        quad.face(),
                        M3CompletionResourceModels.INSCRIBER_INSIDE_TEXTURE,
                        0xffffff,
                        false,
                        quad.vertices().stream().map(vertex -> new Vertex(
                                vertex.x16(), vertex.y16(), vertex.z16(),
                                vertex.u16(), vertex.v16()
                        )).toList()
                )).toList();
        RenderPlan stampPlan = planPrimitives(stamps, matrix, block);
        if (stampPlan == null) {
            return RenderStatus.RESOURCES_MISSING;
        }

        ReservedTileModel reserved = new ReservedTileModel(
                tileModel.getTileModel(),
                INSCRIBER_MAX_TRIANGLES
        );
        try {
            blockColor.set(0F, 0F, 0F, 0F, true);
            float opacity = 0F;
            Color shell = renderSupport.renderModelPreservingAmbientOcclusion(
                    block, M3CompletionResourceModels.INSCRIBER, orientation,
                    reserved
            );
            opacity = DriveRenderSupport.addMapColor(blockColor, shell, opacity);
            Color stampColor = emitPlan(stampPlan, matrix, reserved);
            opacity = DriveRenderSupport.addMapColor(blockColor, stampColor, opacity);
            DriveRenderSupport.finishMapColor(blockColor, opacity);
            reserved.commit();
            tileModel.initialize(renderStart);
            return RenderStatus.RENDERED;
        } catch (RuntimeException | LinkageError exception) {
            reserved.rollback();
            throw exception;
        }
    }

    private RenderStatus renderSpatialPylon(
            BlockNeighborhood block,
            TileModelView tileModel,
            Color blockColor,
            int renderStart
    ) {
        if (!hasBlockEntity(block, Ae219217M3CompletionProfile.SPATIAL_PYLON_BLOCK_ENTITY_ID)) {
            return RenderStatus.MALFORMED;
        }
        EnumSet<Direction6> pylonNeighbors = EnumSet.noneOf(Direction6.class);
        for (Direction6 direction : Direction6.values()) {
            ExtendedBlock adjacent = neighbor(block, direction);
            if (missing(adjacent)) {
                return RenderStatus.NEIGHBOR_UNAVAILABLE;
            }
            BlockState state = adjacent.getBlockState();
            if (Ae219217M3CompletionProfile.SPATIAL_PYLON_BLOCK.equals(
                    state.getId().getFormatted()
            )) {
                if (exactKind(state) != M3CompletionBlockKind.SPATIAL_PYLON
                        || !hasBlockEntity(
                                adjacent,
                                Ae219217M3CompletionProfile.SPATIAL_PYLON_BLOCK_ENTITY_ID
                        )) {
                    return RenderStatus.NEIGHBOR_UNAVAILABLE;
                }
                pylonNeighbors.add(direction);
            }
        }
        SpatialPylonSnapshot snapshot = SpatialPylonSnapshot.infer(pylonNeighbors)
                .orElse(null);
        if (snapshot == null) {
            diagnosticReporter.report(
                    BoundedDiagnostics.Event.M3_COMPLETION_INVALID_TOPOLOGY
            );
            snapshot = unformedSpatialPylon();
        } else if (snapshot.formed()) {
            SpatialPylonTopologyResolver.Status lineStatus =
                    SpatialPylonTopologyResolver.resolve(
                            block,
                            snapshot.axis()
                    );
            if (lineStatus == SpatialPylonTopologyResolver.Status.INCOMPLETE) {
                return RenderStatus.NEIGHBOR_UNAVAILABLE;
            }
            if (lineStatus == SpatialPylonTopologyResolver.Status.BRANCHED) {
                diagnosticReporter.report(
                        BoundedDiagnostics.Event.M3_COMPLETION_INVALID_TOPOLOGY
                );
                snapshot = unformedSpatialPylon();
            }
        }
        List<PrimitiveQuad> primitives = SpatialPylonGeometry.forSnapshot(snapshot).stream()
                .map(quad -> new PrimitiveQuad(
                        quad.face(),
                        M3CompletionResourceModels.pylonTexture(quad),
                        0xffffff,
                        false,
                        quad.vertices().stream().map(vertex -> new Vertex(
                                vertex.x16(), vertex.y16(), vertex.z16(),
                                vertex.u16(), vertex.v16()
                        )).toList()
                )).toList();
        return emitPrimitives(
                primitives, IDENTITY, block, tileModel, blockColor, renderStart
        ) ? RenderStatus.RENDERED : RenderStatus.RESOURCES_MISSING;
    }

    private static SpatialPylonSnapshot unformedSpatialPylon() {
        return new SpatialPylonSnapshot(
                SpatialPylonSnapshot.Axis.X,
                SpatialPylonSnapshot.AxisPosition.NONE
        );
    }

    private boolean emitPrimitives(
            List<PrimitiveQuad> primitives,
            MatrixM4f orientation,
            BlockNeighborhood block,
            TileModelView tileModel,
            Color blockColor,
            int renderStart
    ) {
        RenderPlan plan = planPrimitives(primitives, orientation, block);
        if (plan == null) {
            return false;
        }
        blockColor.set(emitPlan(plan, orientation, tileModel.getTileModel()));
        tileModel.initialize(renderStart);
        return true;
    }

    private RenderPlan planPrimitives(
            List<PrimitiveQuad> primitives,
            MatrixM4f orientation,
            BlockNeighborhood block
    ) {
        List<PlannedQuad> planned = new ArrayList<>(primitives.size());
        LightData centerLight = block.getLightData();
        for (PrimitiveQuad primitive : primitives) {
            Direction6 worldFace = rotate(orientation, primitive.face());
            if (renderSettings.isRenderTopOnly() && worldFace != Direction6.UP) {
                continue;
            }
            ExtendedBlock outward = neighbor(block, worldFace);
            if (missing(outward)) {
                return null;
            }
            LightData outwardLight = outward.getLightData();
            LightLevels worldLight = new LightLevels(
                    Math.max(centerLight.getSkyLight(), outwardLight.getSkyLight()),
                    Math.max(centerLight.getBlockLight(), outwardLight.getBlockLight())
            );
            if (isHiddenCave(block, worldLight)) {
                continue;
            }
            Texture texture = resourcePack.getTextures().get(primitive.texture());
            if (texture == null) {
                return null;
            }
            planned.add(new PlannedQuad(
                    primitive,
                    worldFace,
                    textureGallery.get(primitive.texture()),
                    texture,
                    primitive.emissive() ? new LightLevels(15, 15) : worldLight
            ));
        }
        return new RenderPlan(List.copyOf(planned), mapColor(planned));
    }

    private Color emitPlan(RenderPlan plan, MatrixM4f orientation, TileModel model) {
        TileModelView view = new TileModelView(model);
        int triangle = view.add(plan.quads().size() * 2);
        for (PlannedQuad planned : plan.quads()) {
            List<Vertex> vertices = planned.primitive().vertices();
            setTriangle(model, triangle, vertices.get(0), vertices.get(1), vertices.get(2));
            setAttributes(model, triangle, planned);
            triangle++;
            setTriangle(model, triangle, vertices.get(0), vertices.get(2), vertices.get(3));
            setAttributes(model, triangle, planned);
            triangle++;
        }
        view.transform(orientation);
        return plan.mapColor();
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

    private static void setAttributes(TileModel model, int triangle, PlannedQuad planned) {
        float red = ((planned.primitive().rgb() >> 16) & 0xff) / 255F;
        float green = ((planned.primitive().rgb() >> 8) & 0xff) / 255F;
        float blue = (planned.primitive().rgb() & 0xff) / 255F;
        model.setMaterialIndex(triangle, planned.material());
        model.setColor(triangle, red, green, blue);
        model.setSunlight(triangle, planned.light().sunlight());
        model.setBlocklight(triangle, planned.light().blocklight());
        model.setAOs(triangle, 1F, 1F, 1F);
    }

    private Color mapColor(List<PlannedQuad> quads) {
        Color result = new Color().set(0F, 0F, 0F, 0F, true);
        float opacity = 0F;
        for (PlannedQuad planned : quads) {
            if (planned.worldFace() != Direction6.UP) {
                continue;
            }
            Color layer = new Color().set(planned.texture().getColorPremultiplied());
            float red = ((planned.primitive().rgb() >> 16) & 0xff) / 255F;
            float green = ((planned.primitive().rgb() >> 8) & 0xff) / 255F;
            float blue = (planned.primitive().rgb() & 0xff) / 255F;
            float illumination = combinedLight(planned.light());
            layer.r *= red * illumination;
            layer.g *= green * illumination;
            layer.b *= blue * illumination;
            opacity = Math.max(opacity, layer.a);
            result.add(layer);
        }
        if (result.a > 0F) {
            result.flatten().straight();
            result.a = opacity;
        }
        return result;
    }

    private float combinedLight(LightLevels light) {
        float combined = Math.max(light.sunlight() / 15F, light.blocklight() / 15F);
        return (1F - renderSettings.getAmbientLight()) * combined
                + renderSettings.getAmbientLight();
    }

    private boolean isHiddenCave(BlockNeighborhood block, LightLevels light) {
        return block.isRemoveIfCave()
                && (renderSettings.isCaveDetectionUsesBlockLight()
                        ? Math.max(light.blocklight(), light.sunlight())
                        : light.sunlight()) == 0;
    }

    private boolean hasExactResources() {
        if (resourcesSupported == null) {
            resourcesSupported = resourceValidator.resourcesSupported(resourcePack);
        }
        return resourcesSupported;
    }

    static M3CompletionBlockKind exactKind(BlockState state) {
        if (state == null || BlockState.MISSING.equals(state)) {
            return null;
        }
        String id = state.getId().getFormatted();
        Map<String, String> properties = state.getProperties();
        if (Ae219217M3CompletionProfile.PAINT_BLOCK.equals(id)) {
            return properties.keySet().equals(Set.of("facing", "light_level"))
                    && validFacing(properties.get("facing"))
                    && Set.of("0", "1", "2").contains(properties.get("light_level"))
                    ? M3CompletionBlockKind.PAINT : null;
        }
        if (Ae219217M3CompletionProfile.SKY_STONE_CHEST_BLOCK.equals(id)
                || Ae219217M3CompletionProfile.SMOOTH_SKY_STONE_CHEST_BLOCK.equals(id)) {
            boolean exact = properties.keySet().equals(Set.of("facing", "waterlogged"))
                    && validHorizontalFacing(properties.get("facing"))
                    && validBoolean(properties.get("waterlogged"));
            if (!exact) {
                return null;
            }
            return Ae219217M3CompletionProfile.SKY_STONE_CHEST_BLOCK.equals(id)
                    ? M3CompletionBlockKind.SKY_STONE_CHEST
                    : M3CompletionBlockKind.SMOOTH_SKY_STONE_CHEST;
        }
        if (Ae219217M3CompletionProfile.CRANK_BLOCK.equals(id)) {
            return properties.keySet().equals(Set.of("facing"))
                    && validFacing(properties.get("facing"))
                    ? M3CompletionBlockKind.CRANK : null;
        }
        if (Ae219217M3CompletionProfile.INSCRIBER_BLOCK.equals(id)) {
            return properties.keySet().equals(Set.of("facing", "spin", "waterlogged"))
                    && validFacing(properties.get("facing"))
                    && Set.of("0", "1", "2", "3").contains(properties.get("spin"))
                    && validBoolean(properties.get("waterlogged"))
                    ? M3CompletionBlockKind.INSCRIBER : null;
        }
        if (Ae219217M3CompletionProfile.SPATIAL_PYLON_BLOCK.equals(id)) {
            return properties.keySet().equals(Set.of("powered_on"))
                    && validBoolean(properties.get("powered_on"))
                    ? M3CompletionBlockKind.SPATIAL_PYLON : null;
        }
        return null;
    }

    private static boolean validFacing(String value) {
        if (value == null) {
            return false;
        }
        try {
            Direction6.valueOf(value.toUpperCase(Locale.ROOT));
            return true;
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }

    private static boolean validHorizontalFacing(String value) {
        return validFacing(value) && !"up".equals(value) && !"down".equals(value);
    }

    private static boolean validBoolean(String value) {
        return "true".equals(value) || "false".equals(value);
    }

    private static Direction6 facing(BlockState state) {
        String value = state.getProperties().get("facing");
        if (!validFacing(value)) {
            return null;
        }
        return Direction6.valueOf(value.toUpperCase(Locale.ROOT));
    }

    private static int spin(BlockState state) {
        String value = state.getProperties().get("spin");
        if (value == null) {
            return -1;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            return -1;
        }
    }

    private static PartOrientation chestOrientation(Direction6 facing) {
        float y = switch (facing) {
            case SOUTH -> 0F;
            case WEST -> 90F;
            case NORTH -> 180F;
            case EAST -> 270F;
            default -> throw new IllegalArgumentException("chest facing must be horizontal");
        };
        return new PartOrientation(0F, y, 0F);
    }

    private static MatrixM4f orientation(PartOrientation orientation) {
        return DriveRenderSupport.orientedVariant(
                M3CompletionResourceModels.INSCRIBER,
                orientation
        ).getTransformMatrix();
    }

    private static Direction6 rotate(MatrixM4f matrix, Direction6 direction) {
        int x = Math.round(matrix.m00 * direction.stepX()
                + matrix.m01 * direction.stepY()
                + matrix.m02 * direction.stepZ());
        int y = Math.round(matrix.m10 * direction.stepX()
                + matrix.m11 * direction.stepY()
                + matrix.m12 * direction.stepZ());
        int z = Math.round(matrix.m20 * direction.stepX()
                + matrix.m21 * direction.stepY()
                + matrix.m22 * direction.stepZ());
        for (Direction6 candidate : Direction6.values()) {
            if (candidate.stepX() == x && candidate.stepY() == y && candidate.stepZ() == z) {
                return candidate;
            }
        }
        throw new IllegalArgumentException("orientation did not preserve a cardinal face");
    }

    private static boolean hasBlockEntity(BlockNeighborhood block, String expectedId) {
        BlockEntity entity = block.getBlockEntity();
        Key entityId = entity == null ? null : entity.getId();
        return entityId != null && expectedId.equals(entityId.getFormatted());
    }

    private static boolean hasBlockEntity(ExtendedBlock block, String expectedId) {
        BlockEntity entity = block.getBlockEntity();
        Key entityId = entity == null ? null : entity.getId();
        return entityId != null && expectedId.equals(entityId.getFormatted());
    }

    private static boolean missing(ExtendedBlock block) {
        return block == null || BlockState.MISSING.equals(block.getBlockState());
    }

    private static ExtendedBlock neighbor(BlockNeighborhood block, Direction6 direction) {
        return block.getNeighborBlock(direction.stepX(), direction.stepY(), direction.stepZ());
    }

    private static BoundedDiagnostics.Event eventFor(RenderStatus status) {
        return switch (status) {
            case MALFORMED -> BoundedDiagnostics.Event.M3_COMPLETION_MALFORMED_BLOCK_DATA;
            case NEIGHBOR_UNAVAILABLE ->
                    BoundedDiagnostics.Event.M3_COMPLETION_UNSUPPORTED_NEIGHBOR_DATA;
            case RESOURCES_MISSING ->
                    BoundedDiagnostics.Event.M3_COMPLETION_REQUIRED_RESOURCES_MISMATCH;
            case RENDERED -> throw new IllegalArgumentException("rendered status is not a failure");
        };
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
        renderSupport.renderOriginalSafely(
                block,
                tileModel,
                blockColor,
                renderStart,
                BoundedDiagnostics.Event.M3_COMPLETION_RENDER_FAILED
        );
    }

    private static float units(double coordinate16) {
        return (float) (coordinate16 * SIXTEENTH);
    }

    private enum RenderStatus {
        RENDERED,
        MALFORMED,
        NEIGHBOR_UNAVAILABLE,
        RESOURCES_MISSING
    }

    private record Vertex(double x16, double y16, double z16, double u16, double v16) {
        private Vertex {
            if (!Double.isFinite(x16) || !Double.isFinite(y16) || !Double.isFinite(z16)
                    || !Double.isFinite(u16) || !Double.isFinite(v16)) {
                throw new IllegalArgumentException("primitive vertex must be finite");
            }
        }
    }

    private record PrimitiveQuad(
            Direction6 face,
            Key texture,
            int rgb,
            boolean emissive,
            List<Vertex> vertices
    ) {
        private PrimitiveQuad {
            face = java.util.Objects.requireNonNull(face, "face");
            texture = java.util.Objects.requireNonNull(texture, "texture");
            vertices = List.copyOf(java.util.Objects.requireNonNull(vertices, "vertices"));
            if ((rgb & 0xff000000) != 0 || vertices.size() != 4) {
                throw new IllegalArgumentException("invalid primitive quad");
            }
        }
    }

    private record LightLevels(int sunlight, int blocklight) {
    }

    private record PlannedQuad(
            PrimitiveQuad primitive,
            Direction6 worldFace,
            int material,
            Texture texture,
            LightLevels light
    ) {
    }

    private record RenderPlan(List<PlannedQuad> quads, Color mapColor) {
    }

    @FunctionalInterface
    interface ResourceValidator {
        boolean resourcesSupported(ResourcePack resourcePack);
    }

    @FunctionalInterface
    interface DiagnosticReporter {
        void report(BoundedDiagnostics.Event event);
    }
}
