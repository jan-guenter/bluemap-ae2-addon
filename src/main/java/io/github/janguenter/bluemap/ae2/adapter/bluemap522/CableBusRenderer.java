/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.adapter.bluemap522;

import com.flowpowered.math.vector.Vector3i;
import de.bluecolored.bluemap.core.map.TextureGallery;
import de.bluecolored.bluemap.core.map.hires.MaxCapacityReachedException;
import de.bluecolored.bluemap.core.map.hires.RenderSettings;
import de.bluecolored.bluemap.core.map.hires.TileModel;
import de.bluecolored.bluemap.core.map.hires.TileModelView;
import de.bluecolored.bluemap.core.map.hires.block.BlockRenderer;
import de.bluecolored.bluemap.core.map.hires.block.ResourceModelRenderer;
import de.bluecolored.bluemap.core.resources.ResourcePath;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.Variant;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.texture.Texture;
import de.bluecolored.bluemap.core.util.Key;
import de.bluecolored.bluemap.core.util.math.Color;
import de.bluecolored.bluemap.core.util.math.VectorM3f;
import de.bluecolored.bluemap.core.world.BlockEntity;
import de.bluecolored.bluemap.core.world.BlockState;
import de.bluecolored.bluemap.core.world.LightData;
import de.bluecolored.bluemap.core.world.block.BlockNeighborhood;
import io.github.janguenter.bluemap.ae2.api.Ae2ExtensionRegistry;
import de.bluecolored.bluemap.core.world.block.ExtendedBlock;
import io.github.janguenter.bluemap.ae2.activation.ExtensionRouteActivation;
import io.github.janguenter.bluemap.ae2.activation.ProfileActivation;
import io.github.janguenter.bluemap.ae2.activation.NativeStructuralRouteActivation;
import io.github.janguenter.bluemap.ae2.diagnostics.BoundedDiagnostics;
import io.github.janguenter.bluemap.ae2.model.Ae2CableCatalog;
import io.github.janguenter.bluemap.ae2.model.CableBusDecodeResult;
import io.github.janguenter.bluemap.ae2.model.CableBusDecoder;
import io.github.janguenter.bluemap.ae2.model.CableBusSnapshot;
import io.github.janguenter.bluemap.ae2.model.CableColor;
import io.github.janguenter.bluemap.ae2.model.CableFamily;
import io.github.janguenter.bluemap.ae2.model.CableGeometry;
import io.github.janguenter.bluemap.ae2.model.Direction6;
import io.github.janguenter.bluemap.ae2.model.FacePartSnapshot;
import io.github.janguenter.bluemap.ae2.model.FacadeGeometry;
import io.github.janguenter.bluemap.ae2.model.PartOrientation;
import io.github.janguenter.bluemap.ae2.model.NativeEndpointCatalog;
import io.github.janguenter.bluemap.ae2.model.NativePartCollisionGeometry;
import io.github.janguenter.bluemap.ae2.model.NativePartGeometry;
import io.github.janguenter.bluemap.ae2.model.NativeStructuralCableBusDecoder;
import io.github.janguenter.bluemap.ae2.model.NativeStructuralDecodeResult;
import io.github.janguenter.bluemap.ae2.model.NativeStructuralPartCatalog;
import io.github.janguenter.bluemap.ae2.model.NativeStructuralSnapshot;
import io.github.janguenter.bluemap.ae2.profile.Ae219217Profile;
import io.github.janguenter.bluemap.ae2.profile.Ae219217NativeStructuralProfile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Exact-profile renderer for the bounded AE2 19.2.17 M2 cable-bus slice. */
final class CableBusRenderer implements BlockRenderer {

    private static final float SIXTEENTH = 1F / 16F;
    private static final Key CABLE_BUS = Key.parse(Ae219217Profile.CABLE_BUS_BLOCK);
    private final ResourcePack resourcePack;
    private final TextureGallery textureGallery;
    private final RenderSettings renderSettings;
    private final ProfileActivation activation;
    private final NativeStructuralRouteActivation nativeStructuralActivation;
    private final M45Runtime m45Runtime;
    private final ResourceModelRenderer stockRenderer;
    private final MegaCellDockRenderSupport megaCellDockRenderSupport;
    private final CableBusDecoder decoder = new CableBusDecoder();
    private final NativeStructuralCableBusDecoder nativeStructuralDecoder;
    private Boolean terminalResourcesSupported;
    private Key resolvedStoneTexture;
    private boolean stoneTextureResolved;

    CableBusRenderer(
            ResourcePack resourcePack,
            TextureGallery textureGallery,
            RenderSettings renderSettings,
            ProfileActivation activation
    ) {
        this(
                resourcePack,
                textureGallery,
                renderSettings,
                activation,
                new NativeStructuralRouteActivation(),
                M45Adapter.runtime()
        );
    }

    private void renderNativeStructural(
            BlockNeighborhood block,
            TileModelView tileModel,
            Color blockColor,
            int renderStart
    ) {
        try {
            if (!Ae2ResourceExtension.isExactCableBusState(block.getBlockState())) {
                fallback(
                        BoundedDiagnostics.Event.NATIVE_STRUCTURAL_UNSUPPORTED_BLOCK_STATE,
                        block,
                        tileModel,
                        blockColor,
                        renderStart
                );
                return;
            }
            Ae2CableBusBlockEntityData blockEntity = exactCableBusBlockEntity(block);
            if (blockEntity == null) {
                fallback(
                        BoundedDiagnostics.Event.NATIVE_STRUCTURAL_MALFORMED_BLOCK_DATA,
                        block,
                        tileModel,
                        blockColor,
                        renderStart
                );
                return;
            }

            NativeStructuralDecodeResult decoded = decodeNative(blockEntity);
            if (!decoded.isSupported()) {
                fallback(
                        nativeEventFor(decoded.status()),
                        block,
                        tileModel,
                        blockColor,
                        renderStart
                );
                return;
            }
            NativeStructuralSnapshot snapshot = decoded.supportedSnapshot().orElseThrow();
            for (Direction6 direction : Direction6.values()) {
                NativeNeighborConnection connection = nativeNeighborConnection(
                        block,
                        direction,
                        snapshot
                );
                if (connection.unknown()) {
                    fallback(
                            BoundedDiagnostics.Event.NATIVE_STRUCTURAL_UNSUPPORTED_NEIGHBOR_DATA,
                            block,
                            tileModel,
                            blockColor,
                            renderStart
                    );
                    return;
                }
                snapshot = snapshot.withConnection(direction, connection.connection());
            }

            for (Map.Entry<Direction6, FacePartSnapshot> entry
                    : snapshot.faceParts().entrySet()) {
                NativeStructuralPartCatalog.Definition definition =
                        NativeStructuralPartCatalog.require(entry.getValue().id());
                if (definition.kind() != NativeStructuralPartCatalog.Kind.PLANE) {
                    continue;
                }
                Integer mask;
                try {
                    mask = resolvePlaneMask(
                            block,
                            entry.getKey(),
                            entry.getValue().id()
                    );
                } catch (RuntimeException | LinkageError exception) {
                    throw partFailure(definition, exception);
                }
                if (mask == null) {
                    fallback(
                            BoundedDiagnostics.Event.NATIVE_STRUCTURAL_UNSUPPORTED_NEIGHBOR_DATA,
                            block,
                            tileModel,
                            blockColor,
                            renderStart
                    );
                    return;
                }
                snapshot = snapshot.withPlaneConnectionMask(entry.getKey(), mask);
            }

            Map<Direction6, NativeFacadeResourceModels.FacadeMaterial> facadeMaterials =
                    resolveFacadeMaterials(snapshot, block);
            if (facadeMaterials == null) {
                fallback(
                        BoundedDiagnostics.Event.NATIVE_STRUCTURAL_UNSUPPORTED_FACADE_STATE,
                        block,
                        tileModel,
                        blockColor,
                        renderStart
                );
                return;
            }

            List<CableGeometry.Quad> cableGeometry = CableGeometry.forNativeSnapshot(snapshot);
            if (!hasNativeSelectedResources(snapshot, cableGeometry, facadeMaterials)) {
                fallback(
                        BoundedDiagnostics.Event.NATIVE_STRUCTURAL_REQUIRED_RESOURCES_MISMATCH,
                        block,
                        tileModel,
                        blockColor,
                        renderStart
                );
                return;
            }

            if (snapshot.hasCenter()) {
                Map<Direction6, CableFamily> effectiveConnections =
                        new java.util.EnumMap<>(Direction6.class);
                snapshot.connections().forEach((direction, connection) ->
                        effectiveConnections.put(direction, connection.effectiveFamily()));
                CableBusSnapshot cableSnapshot = new CableBusSnapshot(
                        snapshot.cable(),
                        effectiveConnections,
                        snapshot.faceParts(),
                        snapshot.facades()
                );
                renderGeometry(
                        cableSnapshot,
                        cableGeometry,
                        block,
                        tileModel,
                        blockColor,
                        renderStart
                );
            } else {
                blockColor.set(0F, 0F, 0F, 0F, true);
                tileModel.initialize(renderStart);
            }
            renderNativePartsAndFacades(
                    snapshot,
                    facadeMaterials,
                    block,
                    tileModel,
                    blockColor,
                    renderStart
            );
        } catch (MaxCapacityReachedException exception) {
            throw exception;
        } catch (ExtensionPartRouteFailure failure) {
            disableExtensionPartRoute(failure.routeId());
            fallback(
                    BoundedDiagnostics.Event.NATIVE_STRUCTURAL_RENDER_FAILED,
                    block,
                    tileModel,
                    blockColor,
                    renderStart
            );
        } catch (RuntimeException | LinkageError exception) {
            nativeStructuralActivation.disable(
                    NativeStructuralRouteActivation.Reason.RENDER_CALLBACK_FAILED
            );
            M45Adapter.blockExtendedPlanesIfNativeStructuralInactive(
                    m45Runtime,
                    false
            );
            fallback(
                    BoundedDiagnostics.Event.NATIVE_STRUCTURAL_RENDER_FAILED,
                    block,
                    tileModel,
                    blockColor,
                    renderStart
            );
        }
    }

    CableBusRenderer(
            ResourcePack resourcePack,
            TextureGallery textureGallery,
            RenderSettings renderSettings,
            ProfileActivation activation,
            NativeStructuralRouteActivation nativeStructuralActivation
    ) {
        this(
                resourcePack,
                textureGallery,
                renderSettings,
                activation,
                nativeStructuralActivation,
                M45Adapter.runtime()
        );
    }

    CableBusRenderer(
            ResourcePack resourcePack,
            TextureGallery textureGallery,
            RenderSettings renderSettings,
            ProfileActivation activation,
            NativeStructuralRouteActivation nativeStructuralActivation,
            M45Runtime m45Runtime
    ) {
        this.resourcePack = resourcePack;
        this.textureGallery = textureGallery;
        this.renderSettings = renderSettings;
        this.activation = activation;
        this.nativeStructuralActivation = nativeStructuralActivation;
        this.m45Runtime = java.util.Objects.requireNonNull(m45Runtime, "m45Runtime");
        this.nativeStructuralDecoder = new NativeStructuralCableBusDecoder(
                routeId -> this.m45Runtime.contains(routeId)
                        ? this.m45Runtime.active(routeId)
                        : Ae2ExtensionRegistry.Host.routeActive(routeId)
        );
        this.stockRenderer = new ResourceModelRenderer(
                resourcePack,
                textureGallery,
                renderSettings
        );
        this.megaCellDockRenderSupport = new MegaCellDockRenderSupport(
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
        if (!activation.isActive()) {
            renderOriginalSafely(block, tileModel, blockColor, renderStart);
            return;
        }
        if (nativeStructuralActivation.isActive()) {
            renderNativeStructural(block, tileModel, blockColor, renderStart);
            return;
        }

        try {
            if (!Ae2ResourceExtension.isExactCableBusState(block.getBlockState())) {
                fallback(
                        BoundedDiagnostics.Event.UNSUPPORTED_BLOCK_STATE,
                        block,
                        tileModel,
                        blockColor,
                        renderStart
                );
                return;
            }

            Ae2CableBusBlockEntityData blockEntity = block.getBlockEntity()
                    instanceof Ae2CableBusBlockEntityData data ? data : null;
            if (blockEntity == null) {
                fallback(
                        BoundedDiagnostics.Event.MALFORMED_BLOCK_DATA,
                        block,
                        tileModel,
                        blockColor,
                        renderStart
                );
                return;
            }

            CableBusDecodeResult decoded = decoder.decode(
                    blockEntity.getCable(),
                    blockEntity.getFaceParts(),
                    blockEntity.getFacades()
            );
            if (!decoded.isSupported()) {
                fallback(
                        eventFor(decoded.status()),
                        block,
                        tileModel,
                        blockColor,
                        renderStart
                );
                return;
            }

            CableBusSnapshot snapshot = decoded.supportedSnapshot().orElseThrow();
            for (Direction6 direction : Direction6.values()) {
                NeighborConnection connection = neighborConnection(
                        block,
                        direction,
                        snapshot
                );
                if (connection.state() == NeighborState.UNKNOWN) {
                    fallback(
                            BoundedDiagnostics.Event.UNSUPPORTED_NEIGHBOR_DATA,
                            block,
                            tileModel,
                            blockColor,
                            renderStart
                    );
                    return;
                }
                snapshot = snapshot.withConnection(
                        direction,
                        connection.renderedType()
                );
            }

            List<CableGeometry.Quad> geometry = CableGeometry.forSnapshot(snapshot);
            if (!hasSelectedTextures(snapshot, geometry)
                    || (!snapshot.faceParts().isEmpty() && !hasTerminalResources())
                    || (!snapshot.facades().isEmpty() && stoneTexture() == null)) {
                fallback(
                        BoundedDiagnostics.Event.TEXTURE_MISSING,
                        block,
                        tileModel,
                        blockColor,
                        renderStart
                );
                return;
            }

            renderGeometry(
                    snapshot,
                    geometry,
                    block,
                    tileModel,
                    blockColor,
                    renderStart
            );
            renderFacePartsAndFacades(
                    snapshot,
                    block,
                    tileModel,
                    blockColor,
                    renderStart
            );
        } catch (MaxCapacityReachedException exception) {
            throw exception;
        } catch (RuntimeException | LinkageError exception) {
            activation.disable(ProfileActivation.Reason.RENDER_CALLBACK_FAILED);
            M45Adapter.blockRoutesIfCoreInactive(m45Runtime, false);
            fallback(
                    BoundedDiagnostics.Event.RENDER_FAILED,
                    block,
                    tileModel,
                    blockColor,
                    renderStart
            );
        }
    }

    private NativeNeighborConnection nativeNeighborConnection(
            BlockNeighborhood block,
            Direction6 direction,
            NativeStructuralSnapshot local
    ) {
        if (!local.hasCenter() || local.hasFacePart(direction)
                || local.hasFacade(direction)) {
            return NativeNeighborConnection.disconnected();
        }
        ExtendedBlock neighbor = neighbor(block, direction);
        BlockState neighborState = neighbor.getBlockState();
        if (BlockState.MISSING.equals(neighborState)) {
            return NativeNeighborConnection.unknownConnection();
        }
        String neighborId = neighborState.getId().getFormatted();
        if (!CABLE_BUS.equals(neighborState.getId())) {
            NativeEndpointCatalog.Definition endpoint = NativeEndpointCatalog.find(
                    neighborId
            ).orElse(null);
            if (endpoint == null) {
                return switch (classifyUnsupportedEndpoint(neighborId, neighbor)) {
                    case EXACT_COMPATIBLE, MALFORMED ->
                            NativeNeighborConnection.unknownConnection();
                    case UNRELATED -> NativeNeighborConnection.disconnected();
                };
            }
            NativeEndpointTopologyResolver.Status topology =
                    NativeEndpointTopologyResolver.resolve(
                            neighbor,
                            endpoint,
                            direction.opposite()
                    );
            if (topology == NativeEndpointTopologyResolver.Status.UNKNOWN) {
                return NativeNeighborConnection.unknownConnection();
            }
            if (topology == NativeEndpointTopologyResolver.Status.DISCONNECTED) {
                return NativeNeighborConnection.disconnected();
            }
            CableFamily effective = CableFamily.minimum(
                    local.cable().family(),
                    endpoint.cableType()
            );
            return NativeNeighborConnection.connected(
                    NativeStructuralSnapshot.Connection.endpoint(
                            endpoint.blockId(),
                            endpoint.cableType(),
                            effective
                    )
            );
        }

        Ae2CableBusBlockEntityData data = exactCableBusBlockEntity(neighbor);
        if (!Ae2ResourceExtension.isExactCableBusState(neighborState) || data == null) {
            return NativeNeighborConnection.unknownConnection();
        }
        NativeStructuralDecodeResult decoded = decodeNative(data);
        if (!decoded.isSupported()) {
            return NativeNeighborConnection.unknownConnection();
        }
        NativeStructuralSnapshot adjacent = decoded.supportedSnapshot().orElseThrow();
        Direction6 opposite = direction.opposite();
        if (!adjacent.hasCenter() || adjacent.hasFacePart(opposite)
                || adjacent.hasFacade(opposite)) {
            return NativeNeighborConnection.disconnected();
        }
        if (!local.cable().color().connectsTo(adjacent.cable().color())) {
            return NativeNeighborConnection.disconnected();
        }
        CableFamily effective = CableFamily.minimum(
                local.cable().family(),
                adjacent.cable().family()
        );
        return NativeNeighborConnection.connected(
                NativeStructuralSnapshot.Connection.cableBus(
                        adjacent.cable().family(),
                        effective
                )
        );
    }

    static UnsupportedEndpointObservation classifyUnsupportedEndpoint(
            String blockId,
            ExtendedBlock block
    ) {
        String expectedBlockEntityId = Ae219217NativeStructuralProfile
                .knownUnsupportedCompatibleEndpointBlockEntityId(blockId);
        if (expectedBlockEntityId == null) {
            return UnsupportedEndpointObservation.UNRELATED;
        }
        BlockEntity entity = block == null ? null : block.getBlockEntity();
        if (entity == null || entity.getId() == null
                || !expectedBlockEntityId.equals(entity.getId().getFormatted())) {
            return UnsupportedEndpointObservation.MALFORMED;
        }
        return UnsupportedEndpointObservation.EXACT_COMPATIBLE;
    }

    private static Ae2CableBusBlockEntityData exactCableBusBlockEntity(
            ExtendedBlock block
    ) {
        BlockEntity entity = block == null ? null : block.getBlockEntity();
        return entity instanceof Ae2CableBusBlockEntityData data
                && CABLE_BUS.equals(data.getId()) ? data : null;
    }

    private NativeStructuralDecodeResult decodeNative(
            Ae2CableBusBlockEntityData blockEntity
    ) {
        return nativeStructuralDecoder.decode(
                blockEntity.getCable(),
                M45FacePartSanitizer.sanitize(
                        blockEntity.getFaceParts(),
                        m45Runtime
                ),
                blockEntity.getFacades()
        );
    }

    private Integer resolvePlaneMask(
            BlockNeighborhood block,
            Direction6 installedFace,
            String planeId
    ) {
        NativePartGeometry.FaceBasis basis = NativePartGeometry.faceBasis(installedFace);
        Direction6[] directions = {
                basis.up(),
                basis.right(),
                basis.up().opposite(),
                basis.right().opposite()
        };
        NativePartGeometry.PlaneNeighbor[] neighbors = {
                NativePartGeometry.PlaneNeighbor.UP,
                NativePartGeometry.PlaneNeighbor.RIGHT,
                NativePartGeometry.PlaneNeighbor.DOWN,
                NativePartGeometry.PlaneNeighbor.LEFT
        };
        int mask = 0;
        for (int index = 0; index < directions.length; index++) {
            Direction6 offset = directions[index];
            ExtendedBlock adjacent = neighbor(
                    block,
                    offset.stepX(),
                    offset.stepY(),
                    offset.stepZ()
            );
            BlockState state = adjacent.getBlockState();
            if (BlockState.MISSING.equals(state)) {
                return null;
            }
            if (!CABLE_BUS.equals(state.getId())) {
                continue;
            }
            Ae2CableBusBlockEntityData data = exactCableBusBlockEntity(adjacent);
            if (!Ae2ResourceExtension.isExactCableBusState(state) || data == null) {
                return null;
            }
            NativeStructuralDecodeResult decoded = decodeNative(data);
            if (!decoded.isSupported()) {
                return null;
            }
            FacePartSnapshot part = decoded.supportedSnapshot().orElseThrow()
                    .faceParts().get(installedFace);
            if (part != null && planeId.equals(part.id())) {
                mask |= neighbors[index].maskBit();
            }
        }
        return mask;
    }

    private Map<Direction6, NativeFacadeResourceModels.FacadeMaterial>
            resolveFacadeMaterials(
                    NativeStructuralSnapshot snapshot,
                    BlockNeighborhood block
            ) {
        java.util.EnumMap<Direction6, NativeFacadeResourceModels.FacadeMaterial> result =
                new java.util.EnumMap<>(Direction6.class);
        for (Map.Entry<Direction6, io.github.janguenter.bluemap.ae2.model.FacadeSnapshot>
                entry : snapshot.facades().entrySet()) {
            NativeFacadeResourceModels.QuartzFacadeAppearance quartzAppearance = null;
            if (NativeFacadeResourceModels.isNativeQuartzFacade(
                    Key.parse(entry.getValue().blockId())
            )) {
                quartzAppearance = quartzFacadeAppearance(entry.getKey(), block);
                if (quartzAppearance == null) {
                    return null;
                }
            }
            NativeFacadeResourceModels.FacadeMaterial material =
                    NativeFacadeResourceModels.resolve(
                    resourcePack,
                    entry.getValue(),
                    block.getX(),
                    block.getY(),
                    block.getZ(),
                    quartzAppearance
            );
            if (material == null) {
                return null;
            }
            result.put(entry.getKey(), material);
        }
        for (Map.Entry<Direction6, NativeFacadeResourceModels.FacadeMaterial>
                entry : result.entrySet()) {
            for (NativeFacadeResourceModels.FacadeLayer layer
                    : entry.getValue().layers()) {
                if (layer.cullFace() != null && facadeNeighborAppearance(
                        neighbor(block, layer.cullFace()),
                        layer.cullFace().opposite(),
                        entry.getKey()
                ) == null) {
                    return null;
                }
            }
        }
        return Map.copyOf(result);
    }

    private NativeFacadeResourceModels.QuartzFacadeAppearance quartzFacadeAppearance(
            Direction6 renderingFacadeDirection,
            BlockNeighborhood block
    ) {
        int suppressedFaces = 0;
        java.util.EnumMap<Direction6, Integer> frameMasks =
                new java.util.EnumMap<>(Direction6.class);
        for (Direction6 sourceFace : Direction6.values()) {
            Boolean suppress = quartzAppearanceAt(
                    neighbor(block, sourceFace),
                    sourceFace.opposite(),
                    renderingFacadeDirection
            );
            if (suppress == null) {
                return null;
            }
            if (suppress) {
                suppressedFaces |= sourceFace.maskBit();
            }
            int frameMask = 0;
            Direction6[] edges = quartzFaceEdges(sourceFace);
            for (int index = 0; index < edges.length; index++) {
                Boolean connected = quartzAppearanceAt(
                        neighbor(block, edges[index]),
                        sourceFace,
                        renderingFacadeDirection
                );
                if (connected == null) {
                    return null;
                }
                if (!connected) {
                    frameMask |= 1 << index;
                }
            }
            frameMasks.put(sourceFace, frameMask);
        }
        return new NativeFacadeResourceModels.QuartzFacadeAppearance(
                suppressedFaces,
                frameMasks
        );
    }

    private Boolean quartzAppearanceAt(
            ExtendedBlock adjacent,
            Direction6 queriedSide,
            Direction6 renderingFacadeDirection
    ) {
        BlockState appearance = facadeNeighborAppearance(
                adjacent,
                queriedSide,
                renderingFacadeDirection
        );
        if (appearance == null) {
            return null;
        }
        if (!NativeFacadeResourceModels.isNativeQuartzFacade(appearance.getId())) {
            return Boolean.FALSE;
        }
        return appearance.getProperties().isEmpty() ? Boolean.TRUE : null;
    }

    private BlockState facadeNeighborAppearance(
            ExtendedBlock adjacent,
            Direction6 queriedSide,
            Direction6 renderingFacadeDirection
    ) {
        BlockState state = adjacent.getBlockState();
        if (BlockState.MISSING.equals(state)) {
            return null;
        }
        if (!CABLE_BUS.equals(state.getId())) {
            return Ae219217NativeStructuralProfile.isKnownUnsupportedCompatibleEndpoint(
                    state.getId().getFormatted()
            ) ? null : state;
        }
        Ae2CableBusBlockEntityData data = exactCableBusBlockEntity(adjacent);
        if (!Ae2ResourceExtension.isExactCableBusState(state) || data == null) {
            return null;
        }
        NativeStructuralDecodeResult decoded = decodeNative(data);
        if (!decoded.isSupported()) {
            return null;
        }
        Map<Direction6, io.github.janguenter.bluemap.ae2.model.FacadeSnapshot> facades =
                decoded.supportedSnapshot().orElseThrow().facades();
        io.github.janguenter.bluemap.ae2.model.FacadeSnapshot appearance = null;
        if (queriedSide.opposite() != renderingFacadeDirection) {
            appearance = facades.get(queriedSide);
        }
        if (appearance == null) {
            appearance = facades.get(renderingFacadeDirection);
        }
        return appearance == null ? state : new BlockState(
                Key.parse(appearance.blockId()),
                appearance.properties()
        );
    }

    private static Direction6[] quartzFaceEdges(Direction6 face) {
        return switch (face) {
            case DOWN -> new Direction6[]{
                    Direction6.SOUTH, Direction6.EAST,
                    Direction6.NORTH, Direction6.WEST
            };
            case UP -> new Direction6[]{
                    Direction6.SOUTH, Direction6.WEST,
                    Direction6.NORTH, Direction6.EAST
            };
            case NORTH -> new Direction6[]{
                    Direction6.UP, Direction6.WEST,
                    Direction6.DOWN, Direction6.EAST
            };
            case SOUTH -> new Direction6[]{
                    Direction6.UP, Direction6.EAST,
                    Direction6.DOWN, Direction6.WEST
            };
            case WEST -> new Direction6[]{
                    Direction6.UP, Direction6.SOUTH,
                    Direction6.DOWN, Direction6.NORTH
            };
            case EAST -> new Direction6[]{
                    Direction6.UP, Direction6.NORTH,
                    Direction6.DOWN, Direction6.SOUTH
            };
        };
    }

    private boolean hasNativeSelectedResources(
            NativeStructuralSnapshot snapshot,
            List<CableGeometry.Quad> cableGeometry,
            Map<Direction6, NativeFacadeResourceModels.FacadeMaterial> facadeMaterials
    ) {
        if (snapshot.hasCenter()) {
            CableBusSnapshot cableSnapshot = new CableBusSnapshot(
                    snapshot.cable(),
                    Map.of(),
                    snapshot.faceParts(),
                    snapshot.facades()
            );
            if (!hasSelectedTextures(cableSnapshot, cableGeometry)) {
                return false;
            }
        }
        for (Map.Entry<Direction6, FacePartSnapshot> entry
                : snapshot.faceParts().entrySet()) {
            NativeStructuralPartCatalog.Definition definition =
                    NativeStructuralPartCatalog.require(entry.getValue().id());
            try {
                for (String modelPath : NativeStructuralResourceModels.renderedModelPaths(
                        definition,
                        snapshot.hasFacade(entry.getKey())
                )) {
                    if (resolvedNativePartModel(modelPath) == null) {
                        return missingSelectedPartResource(
                                definition,
                                m45Runtime,
                                "missing selected model " + modelPath,
                                this::disableExtensionPartRoute
                        );
                    }
                }
                if (definition.kind() == NativeStructuralPartCatalog.Kind.PLANE) {
                    for (String texture : List.of(
                            NativePartGeometry.planeFrontTexture(entry.getValue().id()),
                            NativePartGeometry.PLANE_SIDE_TEXTURE,
                            NativePartGeometry.PLANE_BACK_TEXTURE
                    )) {
                        if (resourcePack.getTextures().get(Key.parse(texture)) == null) {
                            return false;
                        }
                    }
                } else if (definition.kind() == NativeStructuralPartCatalog.Kind.P2P
                        && resourcePack.getTextures().get(
                                Key.parse(NativePartGeometry.P2P_FREQUENCY_TEXTURE)
                        ) == null) {
                    return missingSelectedPartResource(
                            definition,
                            m45Runtime,
                            "missing neutral P2P frequency texture",
                            this::disableExtensionPartRoute
                    );
                } else if (definition.kind() == NativeStructuralPartCatalog.Kind.CELL_DOCK
                        && !megaCellDockRenderSupport.resourcesSupported(entry.getValue())) {
                    return false;
                }
            } catch (RuntimeException | LinkageError exception) {
                throw partFailure(definition, exception);
            }
        }
        for (Direction6 direction : snapshot.facades().keySet()) {
            if (!snapshot.hasFacePart(direction)
                    && resolvedNativePartModel("ae2:part/cable_anchor_short") == null) {
                return false;
            }
        }
        for (NativeFacadeResourceModels.FacadeMaterial material
                : facadeMaterials.values()) {
            for (NativeFacadeResourceModels.FacadeLayer layer : material.layers()) {
                if (resourcePack.getTextures().get(layer.texture()) == null) {
                    return false;
                }
            }
        }
        return true;
    }

    static boolean missingSelectedPartResource(
            NativeStructuralPartCatalog.Definition definition,
            M45Runtime runtime,
            String detail,
            java.util.function.Consumer<String> routeDisabler
    ) {
        if (definition.isExtension()
                && !runtime.contains(definition.extensionRouteId())) {
            routeDisabler.accept(definition.extensionRouteId());
            throw partFailure(definition, new IllegalStateException(detail));
        }
        return false;
    }

    private static BoundedDiagnostics.Event nativeEventFor(
            NativeStructuralDecodeResult.Status status
    ) {
        return switch (status) {
            case UNSUPPORTED_CENTER_PART ->
                    BoundedDiagnostics.Event.NATIVE_STRUCTURAL_UNSUPPORTED_CENTER_PART;
            case UNSUPPORTED_FACE_PART, INVALID_FACE_PART_SPIN,
                    INVALID_P2P_FREQUENCY, UNSUPPORTED_FACE_PART_TOPOLOGY ->
                    BoundedDiagnostics.Event.NATIVE_STRUCTURAL_UNSUPPORTED_FACE_PART;
            case UNSUPPORTED_FACADE_LAYOUT ->
                    BoundedDiagnostics.Event.NATIVE_STRUCTURAL_UNSUPPORTED_FACADE_LAYOUT;
            default -> BoundedDiagnostics.Event.NATIVE_STRUCTURAL_MALFORMED_BLOCK_DATA;
        };
    }

    private void renderGeometry(
            CableBusSnapshot snapshot,
            List<CableGeometry.Quad> geometry,
            BlockNeighborhood block,
            TileModelView tileModel,
            Color blockColor,
            int renderStart
    ) {
        float colorOpacity = 0F;
        Color topColor = new Color();
        blockColor.set(0F, 0F, 0F, 0F, true);

        for (CableGeometry.Quad quad : geometry) {
            if (renderSettings.isRenderTopOnly() && quad.face() != Direction6.UP) {
                continue;
            }

            LightLevels light = lightFor(block, quad.face());
            if (isHiddenCave(block, light)) {
                continue;
            }

            Key textureKey = textureFor(snapshot, quad);
            Rgb tint = tintFor(snapshot.cable().color(), quad.tintRole());
            emitQuad(quad, textureKey, tint, light, block, tileModel);

            if (quad.face() == Direction6.UP) {
                Texture texture = resourcePack.getTextures().get(textureKey);
                if (texture != null) {
                    topColor.set(texture.getColorPremultiplied());
                    topColor.r *= tint.red();
                    topColor.g *= tint.green();
                    topColor.b *= tint.blue();
                    float combinedLight = quad.emissive()
                            ? 1F : combinedLight(light);
                    topColor.r *= combinedLight;
                    topColor.g *= combinedLight;
                    topColor.b *= combinedLight;
                    colorOpacity = Math.max(colorOpacity, topColor.a);
                    blockColor.add(topColor);
                }
            }
        }

        if (blockColor.a > 0F) {
            blockColor.flatten().straight();
            blockColor.a = colorOpacity;
        }
        tileModel.initialize(renderStart);
    }

    private void renderFacePartsAndFacades(
            CableBusSnapshot snapshot,
            BlockNeighborhood block,
            TileModelView tileModel,
            Color blockColor,
            int renderStart
    ) {
        float colorOpacity = blockColor.a;
        if (blockColor.a > 0F) {
            blockColor.premultiplied();
        }

        for (Map.Entry<Direction6, FacePartSnapshot> entry
                : snapshot.faceParts().entrySet()) {
            Color partColor = renderTerminal(
                    entry.getKey(),
                    entry.getValue().spin(),
                    snapshot.cable().color(),
                    block,
                    tileModel.getTileModel()
            );
            colorOpacity = addMapColor(blockColor, partColor, colorOpacity);
        }

        if (!snapshot.facades().isEmpty()) {
            Key facadeTexture = stoneTexture();
            for (Direction6 direction : snapshot.facades().keySet()) {
                Color facadeColor = renderFacade(
                        direction,
                        facadeTexture,
                        block,
                        tileModel.getTileModel()
                );
                colorOpacity = addMapColor(blockColor, facadeColor, colorOpacity);
            }
        }

        if (blockColor.a > 0F) {
            blockColor.flatten().straight();
            blockColor.a = colorOpacity;
        }
        tileModel.initialize(renderStart);
    }

    private void renderNativePartsAndFacades(
            NativeStructuralSnapshot snapshot,
            Map<Direction6, NativeFacadeResourceModels.FacadeMaterial> facadeMaterials,
            BlockNeighborhood block,
            TileModelView tileModel,
            Color blockColor,
            int renderStart
    ) {
        float colorOpacity = blockColor.a;
        if (blockColor.a > 0F) {
            blockColor.premultiplied();
        }

        for (Map.Entry<Direction6, FacePartSnapshot> entry
                : snapshot.faceParts().entrySet()) {
            NativeStructuralPartCatalog.Definition definition =
                    NativeStructuralPartCatalog.require(entry.getValue().id());
            Color partColor;
            try {
                partColor = renderNativePart(
                        entry.getKey(),
                        entry.getValue(),
                        snapshot.hasFacade(entry.getKey()),
                        snapshot.planeConnectionMasks().getOrDefault(entry.getKey(), 0),
                        snapshot.renderColor(),
                        block,
                        tileModel.getTileModel()
                );
            } catch (MaxCapacityReachedException exception) {
                throw exception;
            } catch (RuntimeException | LinkageError exception) {
                throw partFailure(definition, exception);
            }
            colorOpacity = addMapColor(blockColor, partColor, colorOpacity);
        }

        int facadeMask = 0;
        for (Map.Entry<Direction6, NativeFacadeResourceModels.FacadeMaterial> entry
                : facadeMaterials.entrySet()) {
            if (entry.getValue().opaque()) {
                facadeMask |= entry.getKey().maskBit();
            }
        }
        for (Map.Entry<Direction6, io.github.janguenter.bluemap.ae2.model.FacadeSnapshot>
                entry : snapshot.facades().entrySet()) {
            Direction6 direction = entry.getKey();
            if (!snapshot.hasFacePart(direction)) {
                Color stiltColor = renderPartModelLayers(
                        direction,
                        0,
                        List.of("ae2:part/cable_anchor_short"),
                        snapshot.renderColor(),
                        block,
                        tileModel.getTileModel()
                );
                colorOpacity = addMapColor(blockColor, stiltColor, colorOpacity);
            }

            NativeFacadeResourceModels.FacadeMaterial material =
                    facadeMaterials.get(direction);
            FacadeGeometry.Bounds cutout = NativePartCollisionGeometry.cutout(
                    snapshot,
                    direction,
                    facadeMask,
                    !material.opaque()
            );
            Color facadeColor = renderNativeFacade(
                    direction,
                    material,
                    cutout,
                    facadeMask,
                    block,
                    tileModel.getTileModel()
            );
            colorOpacity = addMapColor(blockColor, facadeColor, colorOpacity);
        }

        if (blockColor.a > 0F) {
            blockColor.flatten().straight();
            blockColor.a = colorOpacity;
        }
        tileModel.initialize(renderStart);
    }

    private Color renderNativePart(
            Direction6 direction,
            FacePartSnapshot part,
            boolean sameFaceFacade,
            int planeMask,
            CableColor cableColor,
            BlockNeighborhood block,
            TileModel model
    ) {
        NativeStructuralPartCatalog.Definition definition =
                NativeStructuralPartCatalog.require(part.id());
        Color result = renderPartModelLayers(
                direction,
                part.spin(),
                NativeStructuralResourceModels.renderedModelPaths(
                        definition,
                        sameFaceFacade
                ),
                cableColor,
                block,
                model
        );
        float opacity = result.a;
        if (result.a > 0F) {
            result.premultiplied();
        }
        if (definition.kind() == NativeStructuralPartCatalog.Kind.CELL_DOCK) {
            opacity = addMapColor(
                    result,
                    megaCellDockRenderSupport.renderDynamic(
                            direction,
                            part,
                            block,
                            model
                    ),
                    opacity
            );
        }
        List<NativePartGeometry.Quad> dynamic = switch (definition.kind()) {
            case PLANE -> NativePartGeometry.plane(part.id(), direction, planeMask);
            case P2P -> NativePartGeometry.p2p(direction, part.p2pFrequency());
            case STATIC, REPORTING, ANCHOR, CELL_DOCK -> List.of();
        };
        for (NativePartGeometry.Quad quad : dynamic) {
            if (renderSettings.isRenderTopOnly() && quad.face() != Direction6.UP) {
                continue;
            }
            LightLevels light = lightFor(block, quad.face());
            if (isHiddenCave(block, light)) {
                continue;
            }
            Key texture = Key.parse(quad.texture());
            emitNativePartQuad(quad, texture, light, block, model);
            if (quad.face() == Direction6.UP) {
                opacity = addTextureMapColor(
                        result,
                        texture,
                        new Rgb(
                                quad.tint().red(),
                                quad.tint().green(),
                                quad.tint().blue()
                        ),
                        quad.emissive() ? 1F : combinedLight(light),
                        opacity
                );
            }
        }
        if (result.a > 0F) {
            result.flatten().straight();
            result.a = opacity;
        }
        return result;
    }

    private Color renderPartModelLayers(
            Direction6 direction,
            int spin,
            List<String> modelPaths,
            CableColor cableColor,
            BlockNeighborhood block,
            TileModel model
    ) {
        Color result = new Color().set(0F, 0F, 0F, 0F, true);
        float opacity = 0F;
        PartOrientation orientation = PartOrientation.forPart(direction, spin);
        for (String modelPath : modelPaths) {
            Variant variant = new Variant(
                    new ResourcePath<>(modelPath),
                    orientation.x(),
                    orientation.y(),
                    orientation.z()
            );
            TileModelView partView = new TileModelView(model);
            Color layerColor = new Color().set(0F, 0F, 0F, 0F, true);
            stockRenderer.render(block, variant, partView, layerColor);
            applyNativePartLayerColors(
                    partView,
                    modelPath,
                    cableColor,
                    variant,
                    block
            );
            layerColor = nativePartLayerMapColor(
                    modelPath,
                    cableColor,
                    variant,
                    block
            );
            opacity = addMapColor(result, layerColor, opacity);
        }
        if (result.a > 0F) {
            result.flatten().straight();
            result.a = opacity;
        }
        return result;
    }

    private void applyNativePartLayerColors(
            TileModelView partView,
            String modelPath,
            CableColor cableColor,
            Variant variant,
            BlockNeighborhood block
    ) {
        de.bluecolored.bluemap.core.resources.pack.resourcepack.model.Model resource =
                resolvedNativePartModel(modelPath);
        if (resource == null || partView.getSize() == 0) {
            return;
        }
        List<Integer> tintIndexes = emittedNativePartTintIndexes(
                resource,
                variant,
                block
        );
        if (partView.getSize() != tintIndexes.size() * 2) {
            throw new IllegalStateException(
                    "native part source/emitted face mismatch for " + modelPath
            );
        }
        TileModel model = partView.getTileModel();
        int triangle = partView.getStart();
        for (int tintIndex : tintIndexes) {
            Rgb tint = nativePartTint(tintIndex, cableColor);
            setFaceColor(model, triangle, triangle + 2, tint);
            triangle += 2;
        }
    }

    private Color nativePartLayerMapColor(
            String modelPath,
            CableColor cableColor,
            Variant variant,
            BlockNeighborhood block
    ) {
        de.bluecolored.bluemap.core.resources.pack.resourcepack.model.Model resource =
                resolvedNativePartModel(modelPath);
        if (resource == null) {
            throw new IllegalStateException(
                    "preflighted native part model disappeared: " + modelPath
            );
        }

        Color result = new Color().set(0F, 0F, 0F, 0F, true);
        float opacity = 0F;
        for (de.bluecolored.bluemap.core.resources.pack.resourcepack.model.Element element
                : resource.getElements()) {
            if (element == null) {
                continue;
            }
            for (de.bluecolored.bluemap.core.util.Direction direction
                    : de.bluecolored.bluemap.core.util.Direction.values()) {
                de.bluecolored.bluemap.core.resources.pack.resourcepack.model.Face face =
                        element.getFaces().get(direction);
                if (face == null || !nativePartFaceIsEmitted(
                        block,
                        element,
                        direction,
                        face,
                        variant
                ) || nativePartFaceNormal(element, direction, variant).y < 0.01F) {
                    continue;
                }
                ResourcePath<Texture> texture = face.getTexture().getTexturePath(
                        resource.getTextures()::get
                );
                if (texture == null || texture.getResource(
                        resourcePack.getTextures()::get
                ) == null) {
                    throw new IllegalStateException(
                            "preflighted native part texture disappeared: " + modelPath
                    );
                }
                LightLevels light = nativePartLight(block, direction, variant);
                opacity = addTextureMapColor(
                        result,
                        Key.parse(texture.getFormatted()),
                        nativePartTint(face.getTintindex(), cableColor),
                        combinedLight(light),
                        opacity
                );
            }
        }
        if (result.a > 0F) {
            result.flatten().straight();
            result.a = opacity;
        }
        return result;
    }

    private de.bluecolored.bluemap.core.resources.pack.resourcepack.model.Model
            resolvedNativePartModel(String modelPath) {
        de.bluecolored.bluemap.core.resources.pack.resourcepack.model.Model resource =
                resourcePack.getModels().get(new ResourcePath<>(modelPath));
        if (resource == null) {
            return null;
        }
        resource.applyParent(resourcePack.getModels());
        if (resource.getElements() == null) {
            return null;
        }
        resource.calculateProperties(resourcePack.getTextures());
        return resource;
    }

    private List<Integer> emittedNativePartTintIndexes(
            de.bluecolored.bluemap.core.resources.pack.resourcepack.model.Model resource,
            Variant variant,
            BlockNeighborhood block
    ) {
        List<Integer> tintIndexes = new ArrayList<>();
        for (de.bluecolored.bluemap.core.resources.pack.resourcepack.model.Element element
                : resource.getElements()) {
            if (element == null) {
                continue;
            }
            for (de.bluecolored.bluemap.core.util.Direction direction
                    : de.bluecolored.bluemap.core.util.Direction.values()) {
                de.bluecolored.bluemap.core.resources.pack.resourcepack.model.Face face =
                        element.getFaces().get(direction);
                if (face != null && nativePartFaceIsEmitted(
                        block,
                        element,
                        direction,
                        face,
                        variant
                )) {
                    tintIndexes.add(face.getTintindex());
                }
            }
        }
        return List.copyOf(tintIndexes);
    }

    private boolean nativePartFaceIsEmitted(
            BlockNeighborhood block,
            de.bluecolored.bluemap.core.resources.pack.resourcepack.model.Element element,
            de.bluecolored.bluemap.core.util.Direction direction,
            de.bluecolored.bluemap.core.resources.pack.resourcepack.model.Face face,
            Variant variant
    ) {
        LightLevels light = nativePartLight(block, direction, variant);
        if (isHiddenCave(block, light)) {
            return false;
        }

        VectorM3f normal = nativePartFaceNormal(element, direction, variant);
        if (renderSettings.isRenderTopOnly() && normal.y < 0.01F) {
            return false;
        }

        if (face.getCullface() == null) {
            return true;
        }
        ExtendedBlock cullNeighbor = rotationRelativeBlock(
                block,
                face.getCullface(),
                variant
        );
        return !cullNeighbor.getProperties().isCulling()
                && (!cullNeighbor.getProperties().getCullingIdentical()
                        || !cullNeighbor.getBlockState().equals(block.getBlockState()));
    }

    private static LightLevels nativePartLight(
            BlockNeighborhood block,
            de.bluecolored.bluemap.core.util.Direction direction,
            Variant variant
    ) {
        ExtendedBlock lightNeighbor = rotationRelativeBlock(block, direction, variant);
        LightData blockLight = block.getLightData();
        LightData faceLight = lightNeighbor.getLightData();
        return new LightLevels(
                Math.max(blockLight.getSkyLight(), faceLight.getSkyLight()),
                Math.max(blockLight.getBlockLight(), faceLight.getBlockLight())
        );
    }

    private static VectorM3f nativePartFaceNormal(
            de.bluecolored.bluemap.core.resources.pack.resourcepack.model.Element element,
            de.bluecolored.bluemap.core.util.Direction direction,
            Variant variant
    ) {
        Vector3i directionVector = direction.toVector();
        VectorM3f normal = new VectorM3f(
                directionVector.getX(),
                directionVector.getY(),
                directionVector.getZ()
        );
        normal.rotateAndScale(element.getRotation().getMatrix());
        if (variant.isTransformed()) {
            normal.rotateAndScale(variant.getTransformMatrix());
        }
        return normal;
    }

    private static ExtendedBlock rotationRelativeBlock(
            BlockNeighborhood block,
            de.bluecolored.bluemap.core.util.Direction direction,
            Variant variant
    ) {
        Vector3i source = direction.toVector();
        VectorM3f transformed = new VectorM3f(
                source.getX(),
                source.getY(),
                source.getZ()
        );
        if (variant.isTransformed()) {
            transformed.rotateAndScale(variant.getTransformMatrix());
        }
        return block.getNeighborBlock(
                Math.round(transformed.x),
                Math.round(transformed.y),
                Math.round(transformed.z)
        );
    }

    private static Rgb average(Rgb first, Rgb second) {
        return new Rgb(
                (first.red() + second.red()) * 0.5F,
                (first.green() + second.green()) * 0.5F,
                (first.blue() + second.blue()) * 0.5F
        );
    }

    private static Rgb nativePartTint(int tintIndex, CableColor cableColor) {
        return switch (tintIndex) {
            case 1 -> rgb(cableColor.darkRgb());
            case 2 -> rgb(cableColor.mediumRgb());
            case 3 -> rgb(cableColor.brightRgb());
            case 4 -> average(
                    rgb(cableColor.mediumRgb()),
                    rgb(cableColor.brightRgb())
            );
            default -> new Rgb(1F, 1F, 1F);
        };
    }

    private void emitNativePartQuad(
            NativePartGeometry.Quad quad,
            Key texture,
            LightLevels light,
            BlockNeighborhood block,
            TileModel model
    ) {
        List<CableGeometry.Vertex> vertices = quad.vertices();
        int first = model.add(2);
        int second = first + 1;
        setTriangle(model, first, vertices.get(0), vertices.get(1), vertices.get(2));
        setTriangle(model, second, vertices.get(0), vertices.get(2), vertices.get(3));
        int material = textureGallery.get(texture);
        model.setMaterialIndex(first, material);
        model.setMaterialIndex(second, material);
        model.setColor(first, quad.tint().red(), quad.tint().green(), quad.tint().blue());
        model.setColor(second, quad.tint().red(), quad.tint().green(), quad.tint().blue());
        int sunlight = quad.emissive() ? 15 : light.sunlight();
        int blocklight = quad.emissive() ? 15 : light.blocklight();
        model.setSunlight(first, sunlight);
        model.setSunlight(second, sunlight);
        model.setBlocklight(first, blocklight);
        model.setBlocklight(second, blocklight);
        if (quad.ambientOcclusion()) {
            float firstAo = testAo(vertices.get(0), quad.face(), block);
            float secondAo = testAo(vertices.get(1), quad.face(), block);
            float thirdAo = testAo(vertices.get(2), quad.face(), block);
            float fourthAo = testAo(vertices.get(3), quad.face(), block);
            model.setAOs(first, firstAo, secondAo, thirdAo);
            model.setAOs(second, firstAo, thirdAo, fourthAo);
        } else {
            model.setAOs(first, 1F, 1F, 1F);
            model.setAOs(second, 1F, 1F, 1F);
        }
    }

    private Color renderNativeFacade(
            Direction6 facadeDirection,
            NativeFacadeResourceModels.FacadeMaterial material,
            FacadeGeometry.Bounds cutout,
            int facadeMask,
            BlockNeighborhood block,
            TileModel model
    ) {
        Color result = new Color().set(0F, 0F, 0F, 0F, true);
        float opacity = 0F;
        BlockState facadeState = material.blockState();
        for (NativeFacadeResourceModels.FacadeLayer layer : material.layers()) {
            if (layer.cullFace() != null) {
                BlockState appearance = facadeNeighborAppearance(
                        neighbor(block, layer.cullFace()),
                        layer.cullFace().opposite(),
                        facadeDirection
                );
                if (appearance == null) {
                    throw new IllegalStateException(
                            "facade neighbor appearance changed after preflight"
                    );
                }
                if (skipsFacadeRendering(resourcePack, facadeState, appearance)) {
                    continue;
                }
            }
            for (FacadeGeometry.Quad quad : FacadeGeometry.clip(
                    layer.nominalFace(),
                    layer.sourceVertices(),
                    facadeDirection,
                    cutout,
                    facadeMask,
                    !material.opaque()
            )) {
                if (renderSettings.isRenderTopOnly() && layer.sourceNormalY() < 0.01F) {
                    continue;
                }
                LightLevels light = lightFor(block, layer.lightFace());
                if (isHiddenCave(block, light)) {
                    continue;
                }
                Rgb tint = facadeTint(material, layer, block);
                emitNativeFacadeQuad(
                        quad,
                        layer,
                        tint,
                        light,
                        block,
                        model
                );
                if (layer.sourceNormalY() > 0.01F) {
                    opacity = addTextureMapColor(
                            result,
                            layer.texture(),
                            tint,
                            combinedLight(light),
                            opacity
                    );
                }
            }
        }
        if (result.a > 0F) {
            result.flatten().straight();
            result.a = opacity;
        }
        return result;
    }

    private void emitNativeFacadeQuad(
            FacadeGeometry.Quad quad,
            NativeFacadeResourceModels.FacadeLayer layer,
            Rgb tint,
            LightLevels light,
            BlockNeighborhood block,
            TileModel model
    ) {
        List<CableGeometry.Vertex> vertices = quad.vertices();
        int first = model.add(2);
        int second = first + 1;
        setTriangle(model, first, vertices.get(0), vertices.get(1), vertices.get(2));
        setTriangle(model, second, vertices.get(0), vertices.get(2), vertices.get(3));
        int materialIndex = textureGallery.get(layer.texture());
        model.setMaterialIndex(first, materialIndex);
        model.setMaterialIndex(second, materialIndex);
        model.setColor(first, tint.red(), tint.green(), tint.blue());
        model.setColor(second, tint.red(), tint.green(), tint.blue());
        model.setSunlight(first, light.sunlight());
        model.setSunlight(second, light.sunlight());
        int blocklight = Math.max(light.blocklight(), layer.lightEmission());
        model.setBlocklight(first, blocklight);
        model.setBlocklight(second, blocklight);
        if (layer.ambientOcclusion()) {
            float firstAo = testAo(vertices.get(0), layer.lightFace(), block);
            float secondAo = testAo(vertices.get(1), layer.lightFace(), block);
            float thirdAo = testAo(vertices.get(2), layer.lightFace(), block);
            float fourthAo = testAo(vertices.get(3), layer.lightFace(), block);
            model.setAOs(first, firstAo, secondAo, thirdAo);
            model.setAOs(second, firstAo, thirdAo, fourthAo);
        } else {
            model.setAOs(first, 1F, 1F, 1F);
            model.setAOs(second, 1F, 1F, 1F);
        }
    }

    private Rgb facadeTint(
            NativeFacadeResourceModels.FacadeMaterial material,
            NativeFacadeResourceModels.FacadeLayer layer,
            BlockNeighborhood block
    ) {
        if (layer.tintIndex() < 0) {
            return new Rgb(1F, 1F, 1F);
        }
        Color tint = new Color();
        stockRenderer.getBlockColorCalculator().getBlockColor(
                block,
                material.blockState(),
                tint
        );
        return new Rgb(tint.r, tint.g, tint.b);
    }

    private Color renderTerminal(
            Direction6 direction,
            int spin,
            CableColor cableColor,
            BlockNeighborhood block,
            TileModel model
    ) {
        Color result = new Color().set(0F, 0F, 0F, 0F, true);
        float opacity = 0F;
        for (String modelPath : List.of(
                M2ResourceModels.DISPLAY_BASE.getFormatted(),
                M2ResourceModels.TERMINAL_OFF.getFormatted(),
                M2ResourceModels.DISPLAY_STATUS_OFF.getFormatted()
        )) {
            PartOrientation orientation = PartOrientation.forPart(direction, spin);
            Variant variant = new Variant(
                    new ResourcePath<>(modelPath),
                    orientation.x(),
                    orientation.y(),
                    orientation.z()
            );
            TileModelView partView = new TileModelView(model);
            Color partColor = new Color().set(0F, 0F, 0F, 0F, true);
            stockRenderer.render(block, variant, partView, partColor);

            if (M2ResourceModels.TERMINAL_OFF.getFormatted().equals(modelPath)) {
                applyTerminalLayerColors(partView, cableColor);
                partColor = terminalLayerMapColor(
                        direction,
                        partView.getSize(),
                        cableColor,
                        block
                );
            }
            opacity = addMapColor(result, partColor, opacity);
        }
        if (result.a > 0F) {
            result.flatten().straight();
            result.a = opacity;
        }
        return result;
    }

    private static void applyTerminalLayerColors(
            TileModelView partView,
            CableColor cableColor
    ) {
        if (partView.getSize() != 0 && partView.getSize() != 6) {
            throw new IllegalStateException("terminal off layers emitted an unexpected size");
        }
        if (partView.getSize() == 0) {
            return;
        }
        TileModel model = partView.getTileModel();
        int start = partView.getStart();
        setFaceColor(model, start, start + 2, rgb(cableColor.brightRgb()));
        setFaceColor(model, start + 2, start + 4, rgb(cableColor.mediumRgb()));
        setFaceColor(model, start + 4, start + 6, rgb(cableColor.darkRgb()));
    }

    private Color terminalLayerMapColor(
            Direction6 direction,
            int emittedTriangles,
            CableColor cableColor,
            BlockNeighborhood block
    ) {
        Color result = new Color().set(0F, 0F, 0F, 0F, true);
        if (direction != Direction6.UP || emittedTriangles == 0) {
            return result;
        }
        LightLevels light = lightFor(block, Direction6.UP);
        float illumination = combinedLight(light);
        float opacity = 0F;
        opacity = addTextureMapColor(
                result,
                Key.parse("ae2:part/terminal_bright"),
                rgb(cableColor.brightRgb()),
                illumination,
                opacity
        );
        opacity = addTextureMapColor(
                result,
                Key.parse("ae2:part/terminal_medium"),
                rgb(cableColor.mediumRgb()),
                illumination,
                opacity
        );
        opacity = addTextureMapColor(
                result,
                Key.parse("ae2:part/terminal_dark"),
                rgb(cableColor.darkRgb()),
                illumination,
                opacity
        );
        if (result.a > 0F) {
            result.flatten().straight();
            result.a = opacity;
        }
        return result;
    }

    private Color renderFacade(
            Direction6 facadeDirection,
            Key textureKey,
            BlockNeighborhood block,
            TileModel model
    ) {
        Color result = new Color().set(0F, 0F, 0F, 0F, true);
        float opacity = 0F;
        for (FacadeGeometry.Quad quad : FacadeGeometry.ring(facadeDirection)) {
            if (renderSettings.isRenderTopOnly() && quad.face() != Direction6.UP) {
                continue;
            }
            if (quad.face() == facadeDirection
                    && cullsFacadeOutward(neighbor(block, facadeDirection))) {
                continue;
            }
            LightLevels light = lightFor(block, quad.face());
            if (isHiddenCave(block, light)) {
                continue;
            }
            emitFacadeQuad(quad, textureKey, light, block, model);
            if (quad.face() == Direction6.UP) {
                opacity = addTextureMapColor(
                        result,
                        textureKey,
                        new Rgb(1F, 1F, 1F),
                        combinedLight(light),
                        opacity
                );
            }
        }
        if (result.a > 0F) {
            result.flatten().straight();
            result.a = opacity;
        }
        return result;
    }

    private void emitFacadeQuad(
            FacadeGeometry.Quad quad,
            Key texture,
            LightLevels light,
            BlockNeighborhood block,
            TileModel model
    ) {
        List<CableGeometry.Vertex> vertices = quad.vertices();
        int first = model.add(2);
        int second = first + 1;
        setTriangle(model, first, vertices.get(0), vertices.get(1), vertices.get(2));
        setTriangle(model, second, vertices.get(0), vertices.get(2), vertices.get(3));
        int material = textureGallery.get(texture);
        model.setMaterialIndex(first, material);
        model.setMaterialIndex(second, material);
        model.setColor(first, 1F, 1F, 1F);
        model.setColor(second, 1F, 1F, 1F);
        model.setSunlight(first, light.sunlight());
        model.setSunlight(second, light.sunlight());
        model.setBlocklight(first, light.blocklight());
        model.setBlocklight(second, light.blocklight());
        float firstAo = testAo(vertices.get(0), quad.face(), block);
        float secondAo = testAo(vertices.get(1), quad.face(), block);
        float thirdAo = testAo(vertices.get(2), quad.face(), block);
        float fourthAo = testAo(vertices.get(3), quad.face(), block);
        model.setAOs(first, firstAo, secondAo, thirdAo);
        model.setAOs(second, firstAo, thirdAo, fourthAo);
    }

    private static void setFaceColor(
            TileModel model,
            int start,
            int end,
            Rgb color
    ) {
        for (int face = start; face < end; face++) {
            model.setColor(face, color.red(), color.green(), color.blue());
        }
    }

    private float addTextureMapColor(
            Color target,
            Key textureKey,
            Rgb tint,
            float illumination,
            float opacity
    ) {
        Texture texture = resourcePack.getTextures().get(textureKey);
        if (texture == null) {
            throw new IllegalStateException("preflighted texture disappeared");
        }
        Color color = new Color().set(texture.getColorPremultiplied());
        color.r *= tint.red() * illumination;
        color.g *= tint.green() * illumination;
        color.b *= tint.blue() * illumination;
        target.add(color);
        return Math.max(opacity, color.a);
    }

    private static float addMapColor(Color target, Color addition, float opacity) {
        if (addition.a <= 0F) {
            return opacity;
        }
        opacity = Math.max(opacity, addition.a);
        target.add(addition.premultiplied());
        return opacity;
    }

    private boolean hasTerminalResources() {
        if (terminalResourcesSupported == null) {
            boolean textures = M2ResourceModels.TERMINAL_TEXTURES.stream()
                    .allMatch(key -> resourcePack.getTextures().get(key) != null);
            terminalResourcesSupported = textures
                    && M2ResourceModels.terminalModelsSupported(resourcePack);
        }
        return terminalResourcesSupported;
    }

    private Key stoneTexture() {
        if (!stoneTextureResolved) {
            resolvedStoneTexture = M2ResourceModels.resolveStoneTexture(resourcePack);
            stoneTextureResolved = true;
        }
        return resolvedStoneTexture;
    }

    private static boolean isPlainStone(BlockState state) {
        return M2ResourceModels.STONE.equals(state.getId())
                && state.getProperties().isEmpty();
    }

    private static boolean cullsFacadeOutward(ExtendedBlock outward) {
        return outward.getProperties().isCulling()
                || (outward.getProperties().getCullingIdentical()
                && isPlainStone(outward.getBlockState()));
    }

    static boolean skipsFacadeRendering(
            ResourcePack resourcePack,
            BlockState facadeState,
            BlockState adjacentAppearance
    ) {
        if (NativeFacadeResourceModels.isNativeQuartzFacade(facadeState.getId())) {
            return NativeFacadeResourceModels.isNativeQuartzFacade(
                    adjacentAppearance.getId()
            );
        }
        if (!facadeState.equals(adjacentAppearance)) {
            return false;
        }
        Boolean explicit = Ae219217NativeStructuralProfile
                .facadeWhitelistSameStateSkipRendering(
                        facadeState.getId().getFormatted()
                );
        if (explicit != null) {
            return explicit;
        }
        Boolean ordinaryControl = Ae219217NativeStructuralProfile
                .facadeOrdinarySkipRenderingControl(
                        facadeState.getId().getFormatted()
                );
        if (ordinaryControl != null) {
            return ordinaryControl;
        }
        return resourcePack.getBlockProperties(facadeState).getCullingIdentical();
    }

    private static Rgb rgb(int value) {
        return new Rgb(
                ((value >> 16) & 0xff) / 255F,
                ((value >> 8) & 0xff) / 255F,
                (value & 0xff) / 255F
        );
    }

    private void emitQuad(
            CableGeometry.Quad quad,
            Key texture,
            Rgb tint,
            LightLevels light,
            BlockNeighborhood block,
            TileModelView tileModel
    ) {
        List<CableGeometry.Vertex> vertices = quad.vertices();
        if (vertices.size() != 4) {
            throw new IllegalStateException("Cable geometry quad is not four-sided");
        }

        tileModel.initialize();
        tileModel.add(2);
        TileModel model = tileModel.getTileModel();
        int first = tileModel.getStart();
        int second = first + 1;
        setTriangle(model, first, vertices.get(0), vertices.get(1), vertices.get(2));
        setTriangle(model, second, vertices.get(0), vertices.get(2), vertices.get(3));

        int material = textureGallery.get(texture);
        model.setMaterialIndex(first, material);
        model.setMaterialIndex(second, material);
        model.setColor(first, tint.red(), tint.green(), tint.blue());
        model.setColor(second, tint.red(), tint.green(), tint.blue());
        int sunlight = quad.emissive() ? 15 : light.sunlight();
        int blocklight = quad.emissive() ? 15 : light.blocklight();
        model.setSunlight(first, sunlight);
        model.setSunlight(second, sunlight);
        model.setBlocklight(first, blocklight);
        model.setBlocklight(second, blocklight);

        float firstAo = testAo(vertices.get(0), quad.face(), block);
        float secondAo = testAo(vertices.get(1), quad.face(), block);
        float thirdAo = testAo(vertices.get(2), quad.face(), block);
        float fourthAo = testAo(vertices.get(3), quad.face(), block);
        model.setAOs(first, firstAo, secondAo, thirdAo);
        model.setAOs(second, firstAo, thirdAo, fourthAo);
    }

    private static void setTriangle(
            TileModel model,
            int face,
            CableGeometry.Vertex first,
            CableGeometry.Vertex second,
            CableGeometry.Vertex third
    ) {
        model.setPositions(
                face,
                units(first.x16()), units(first.y16()), units(first.z16()),
                units(second.x16()), units(second.y16()), units(second.z16()),
                units(third.x16()), units(third.y16()), units(third.z16())
        );
        model.setUvs(
                face,
                units(first.u16()), units(first.v16()),
                units(second.u16()), units(second.v16()),
                units(third.u16()), units(third.v16())
        );
    }

    private NeighborConnection neighborConnection(
            BlockNeighborhood block,
            Direction6 direction,
            CableBusSnapshot local
    ) {
        if (local.hasFacePart(direction)) {
            return NeighborConnection.disconnected();
        }
        ExtendedBlock neighbor = neighbor(block, direction);
        BlockState neighborState = neighbor.getBlockState();
        if (BlockState.MISSING.equals(neighborState)) {
            return NeighborConnection.unknown();
        }
        if (!CABLE_BUS.equals(neighborState.getId())) {
            if (neighborState.isAir()) {
                return NeighborConnection.disconnected();
            }
            if (Ae219217Profile.MOD_ID.equals(neighborState.getId().getNamespace())
                    || neighbor.getBlockEntity() != null) {
                return NeighborConnection.unknown();
            }
            return NeighborConnection.disconnected();
        }
        if (!Ae2ResourceExtension.isExactCableBusState(neighborState)) {
            return NeighborConnection.unknown();
        }
        if (!(neighbor.getBlockEntity() instanceof Ae2CableBusBlockEntityData data)) {
            return NeighborConnection.unknown();
        }

        Direction6 opposite = direction.opposite();
        Object oppositePart = data.getFaceParts().get(opposite);
        if (oppositePart != null) {
            CableBusDecodeResult partDecoded = decoder.decode(
                    data.getCable(),
                    Map.of(opposite, oppositePart),
                    Map.of()
            );
            return partDecoded.isSupported()
                    ? NeighborConnection.disconnected()
                    : NeighborConnection.unknown();
        }
        if (data.getFacades().containsKey(opposite)) {
            return NeighborConnection.unknown();
        }

        CableBusDecodeResult decoded = decoder.decode(
                data.getCable(),
                false
        );
        if (!decoded.isSupported()) {
            return NeighborConnection.unknown();
        }
        CableBusSnapshot adjacent = decoded.supportedSnapshot().orElseThrow();
        if (!local.cable().color().connectsTo(adjacent.cable().color())) {
            return NeighborConnection.disconnected();
        }
        return NeighborConnection.connected(CableFamily.minimum(
                local.cable().family(),
                adjacent.cable().family()
        ));
    }

    private static BoundedDiagnostics.Event eventFor(CableBusDecodeResult.Status status) {
        return switch (status) {
            case UNSUPPORTED_CENTER_PART -> BoundedDiagnostics.Event.UNSUPPORTED_CENTER_PART;
            case UNSUPPORTED_FACE_PART, INVALID_FACE_PART_SPIN,
                    UNSUPPORTED_FACE_PART_TOPOLOGY ->
                    BoundedDiagnostics.Event.UNSUPPORTED_FACE_PART;
            case UNSUPPORTED_FACADE_STATE ->
                    BoundedDiagnostics.Event.UNSUPPORTED_FACADE_STATE;
            case UNSUPPORTED_FACADE_LAYOUT ->
                    BoundedDiagnostics.Event.UNSUPPORTED_FACADE_LAYOUT;
            case UNSUPPORTED_ATTACHMENTS_OR_FACADES ->
                    BoundedDiagnostics.Event.UNSUPPORTED_ATTACHMENTS_OR_FACADES;
            default -> BoundedDiagnostics.Event.MALFORMED_BLOCK_DATA;
        };
    }

    private static Key textureFor(
            CableBusSnapshot snapshot,
            CableGeometry.Quad quad
    ) {
        String texture = switch (quad.textureRole()) {
            case CORE -> snapshot.cable().coreTexture(effectiveCoreFamily(snapshot));
            case CONNECTION -> snapshot.cable().connectionTexture(
                    quad.materialFamily()
            );
            case SMART_CHANNELS_ODD -> quad.materialFamily().isDense()
                    ? Ae2CableCatalog.DENSE_SMART_CHANNELS_OFF_ODD
                    : Ae2CableCatalog.SMART_CHANNELS_OFF_ODD;
            case SMART_CHANNELS_EVEN -> quad.materialFamily().isDense()
                    ? Ae2CableCatalog.DENSE_SMART_CHANNELS_OFF_EVEN
                    : Ae2CableCatalog.SMART_CHANNELS_OFF_EVEN;
        };
        return Key.parse(texture);
    }

    private static CableFamily effectiveCoreFamily(CableBusSnapshot snapshot) {
        CableFamily localFamily = snapshot.cable().family();
        if (localFamily == CableFamily.GLASS
                && snapshot.faceParts().values().stream()
                .anyMatch(part -> NativeStructuralPartCatalog.require(part.id())
                        .requestsSmartCore())) {
            return CableFamily.COVERED;
        }
        return localFamily;
    }

    private boolean hasSelectedTextures(
            CableBusSnapshot snapshot,
            List<CableGeometry.Quad> geometry
    ) {
        for (CableGeometry.Quad quad : geometry) {
            if (resourcePack.getTextures().get(textureFor(snapshot, quad)) == null) {
                return false;
            }
        }
        return true;
    }

    private static Rgb tintFor(CableColor color, CableGeometry.TintRole role) {
        int rgb = switch (role) {
            case WHITE -> 0xffffff;
            case DARK -> color.darkRgb();
            case BRIGHT -> color.brightRgb();
        };
        return new Rgb(
                ((rgb >> 16) & 0xff) / 255F,
                ((rgb >> 8) & 0xff) / 255F,
                (rgb & 0xff) / 255F
        );
    }

    private float combinedLight(LightLevels light) {
        float combined = Math.max(
                light.sunlight() / 15F,
                light.blocklight() / 15F
        );
        return (1F - renderSettings.getAmbientLight()) * combined
                + renderSettings.getAmbientLight();
    }

    private static LightLevels lightFor(BlockNeighborhood block, Direction6 direction) {
        LightData own = block.getLightData();
        LightData adjacent = neighbor(block, direction).getLightData();
        return new LightLevels(
                Math.max(own.getSkyLight(), adjacent.getSkyLight()),
                Math.max(own.getBlockLight(), adjacent.getBlockLight())
        );
    }

    private boolean isHiddenCave(BlockNeighborhood block, LightLevels light) {
        return block.isRemoveIfCave()
                && (renderSettings.isCaveDetectionUsesBlockLight()
                        ? Math.max(light.blocklight(), light.sunlight())
                        : light.sunlight()) == 0;
    }

    private static ExtendedBlock neighbor(BlockNeighborhood block, Direction6 direction) {
        return block.getNeighborBlock(
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

    private static float testAo(
            CableGeometry.Vertex vertex,
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
        if (coordinate16 >= 16) {
            return 1;
        }
        if (coordinate16 <= 0) {
            return -1;
        }
        return 0;
    }

    private static float units(double coordinate16) {
        return (float) (coordinate16 * SIXTEENTH);
    }

    private void fallback(
            BoundedDiagnostics.Event event,
            BlockNeighborhood block,
            TileModelView tileModel,
            Color blockColor,
            int renderStart
    ) {
        if (event != null) {
            BoundedDiagnostics.report(event);
        }
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
            BoundedDiagnostics.report(BoundedDiagnostics.Event.RENDER_FAILED);
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

    private static RuntimeException partFailure(
            NativeStructuralPartCatalog.Definition definition,
            Throwable cause
    ) {
        if (definition.isExtension()) {
            return new ExtensionPartRouteFailure(definition.extensionRouteId(), cause);
        }
        return new IllegalStateException("native face-part callback failed", cause);
    }

    private void disableExtensionPartRoute(String routeId) {
        if (m45Runtime.contains(routeId)) {
            m45Runtime.route(routeId).disable(
                    ExtensionRouteActivation.Reason.RENDER_CALLBACK_FAILED,
                    "extension-part-render-failed"
            );
        } else {
            Ae2ExtensionRegistry.Host.disableRoute(
                    routeId,
                    Ae2ExtensionRegistry.Host.acquireAccess()
            );
        }
    }

    private static final class ExtensionPartRouteFailure extends RuntimeException {

        private static final long serialVersionUID = 1L;
        private final String routeId;

        private ExtensionPartRouteFailure(String routeId, Throwable cause) {
            super("extension face-part callback failed", cause);
            this.routeId = routeId;
        }

        private String routeId() {
            return routeId;
        }
    }

    private enum NeighborState {
        CONNECTED,
        DISCONNECTED,
        UNKNOWN
    }

    enum UnsupportedEndpointObservation {
        EXACT_COMPATIBLE,
        MALFORMED,
        UNRELATED
    }

    private record NeighborConnection(NeighborState state, CableFamily renderedType) {

        private static NeighborConnection connected(CableFamily renderedType) {
            return new NeighborConnection(NeighborState.CONNECTED, renderedType);
        }

        private static NeighborConnection disconnected() {
            return new NeighborConnection(NeighborState.DISCONNECTED, null);
        }

        private static NeighborConnection unknown() {
            return new NeighborConnection(NeighborState.UNKNOWN, null);
        }
    }

    private record NativeNeighborConnection(
            boolean unknown,
            NativeStructuralSnapshot.Connection connection
    ) {

        private static NativeNeighborConnection connected(
                NativeStructuralSnapshot.Connection connection
        ) {
            return new NativeNeighborConnection(false, connection);
        }

        private static NativeNeighborConnection disconnected() {
            return new NativeNeighborConnection(false, null);
        }

        private static NativeNeighborConnection unknownConnection() {
            return new NativeNeighborConnection(true, null);
        }
    }

    private record LightLevels(int sunlight, int blocklight) {
    }

    private record Rgb(float red, float green, float blue) {
    }
}
