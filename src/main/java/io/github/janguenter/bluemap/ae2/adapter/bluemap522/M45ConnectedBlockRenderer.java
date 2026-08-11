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
import de.bluecolored.bluemap.core.world.BlockEntity;
import de.bluecolored.bluemap.core.world.BlockState;
import de.bluecolored.bluemap.core.world.block.BlockNeighborhood;
import de.bluecolored.bluemap.core.world.block.ExtendedBlock;
import io.github.janguenter.bluemap.ae2.activation.ExtensionRouteActivation;
import io.github.janguenter.bluemap.ae2.model.Direction6;
import io.github.janguenter.bluemap.ae2.model.advancedae.AdvancedAeAthenaGeometry;
import io.github.janguenter.bluemap.ae2.model.advancedae.AdvancedAeAthenaSnapshot;
import io.github.janguenter.bluemap.ae2.model.advancedae.AdvancedQuantumGeometry;
import io.github.janguenter.bluemap.ae2.model.advancedae.AdvancedQuantumSnapshot;
import io.github.janguenter.bluemap.ae2.model.extendedae.ExtendedAeMatrixGlassGeometry;
import io.github.janguenter.bluemap.ae2.model.extendedae.ExtendedAeMatrixGlassSnapshot;
import io.github.janguenter.bluemap.ae2.model.extendedae.ExtendedAeMatrixSnapshot;
import io.github.janguenter.bluemap.ae2.profile.advancedae.AdvancedAe1612Catalog;
import io.github.janguenter.bluemap.ae2.profile.advancedae.AdvancedAe1612Catalog.QuantumDefinition;
import io.github.janguenter.bluemap.ae2.profile.extendedae.ExtendedAe2235Catalog;
import io.github.janguenter.bluemap.ae2.profile.extendedae.ExtendedAe2235Catalog.MatrixDefinition;
import io.github.janguenter.bluemap.ae2.profile.extendedae.ExtendedAe2235Catalog.MatrixKind;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/** Exact static Advanced AE and ExtendedAE connected-model renderer. */
final class M45ConnectedBlockRenderer {

    private static final Set<String> BOOLEAN_VALUES = Set.of("false", "true");
    private static final Set<String> ADVANCED_STATE_KEYS = Set.of(
            AdvancedAe1612Catalog.FORMED_PROPERTY,
            AdvancedAe1612Catalog.POWERED_PROPERTY,
            AdvancedAe1612Catalog.MULTIBLOCKED_PROPERTY,
            AdvancedAe1612Catalog.LIGHT_LEVEL_PROPERTY
    );
    private static final Set<String> MATRIX_STATE_KEYS = Set.of(
            ExtendedAe2235Catalog.FORMED_PROPERTY,
            ExtendedAe2235Catalog.POWERED_PROPERTY
    );
    private static final Set<String> MATRIX_FRAME_STATE_KEYS = Set.of(
            ExtendedAe2235Catalog.FORMED_PROPERTY,
            ExtendedAe2235Catalog.POWERED_PROPERTY,
            ExtendedAe2235Catalog.SHAPE_PROPERTY
    );

    private final M45Runtime runtime;
    private final M45ResourceModelSupport stock;
    private final M45MeshEmitter emitter;

    M45ConnectedBlockRenderer(
            ResourcePack resourcePack,
            TextureGallery textureGallery,
            RenderSettings renderSettings,
            M45Runtime runtime
    ) {
        this.runtime = Objects.requireNonNull(runtime, "runtime");
        this.stock = new M45ResourceModelSupport(
                resourcePack,
                textureGallery,
                renderSettings
        );
        this.emitter = new M45MeshEmitter(resourcePack, textureGallery, renderSettings);
    }

    boolean handles(String blockId) {
        return AdvancedAe1612Catalog.quantumBlockIds().contains(blockId)
                || AdvancedAe1612Catalog.QUANTUM_ALLOY_BLOCK.equals(blockId)
                || ExtendedAe2235Catalog.matrixBlockIds().contains(blockId);
    }

    void render(
            BlockNeighborhood block,
            TileModelView tileModel,
            Color blockColor,
            int renderStart
    ) {
        String blockId = block.getBlockState().getId().getFormatted();
        if (AdvancedAe1612Catalog.quantumBlockIds().contains(blockId)) {
            renderAdvanced(block, tileModel, blockColor, renderStart);
        } else if (AdvancedAe1612Catalog.QUANTUM_ALLOY_BLOCK.equals(blockId)) {
            renderAthena(block, tileModel, blockColor, renderStart);
        } else if (ExtendedAe2235Catalog.matrixBlockIds().contains(blockId)) {
            renderMatrix(block, tileModel, blockColor, renderStart);
        } else {
            stock.renderOriginal(block, tileModel, blockColor, renderStart);
        }
    }

    private void renderAdvanced(
            BlockNeighborhood block,
            TileModelView tileModel,
            Color blockColor,
            int renderStart
    ) {
        String routeId = M45Runtime.ADVANCED_QUANTUM;
        if (!runtime.active(routeId)) {
            stock.renderOriginal(block, tileModel, blockColor, renderStart);
            return;
        }
        ExtensionRouteActivation route = runtime.route(routeId);
        try {
            BlockState state = block.getBlockState();
            QuantumDefinition definition = AdvancedAe1612Catalog.requireQuantumDefinition(
                    state.getId().getFormatted()
            );
            AdvancedState persisted = advancedState(state);
            if (persisted == null
                    || !exactBlockEntity(block, definition.blockEntityId())) {
                fallback(block, tileModel, blockColor, renderStart);
                return;
            }
            Map<AdvancedQuantumSnapshot.Offset, String> appearances =
                    advancedAppearances(block);
            if (appearances == null) {
                fallback(block, tileModel, blockColor, renderStart);
                return;
            }
            AdvancedQuantumSnapshot snapshot = AdvancedQuantumSnapshot.observed(
                    definition.kind(),
                    persisted.formed(),
                    persisted.powered(),
                    persisted.multiblocked(),
                    persisted.lightLevel(),
                    block.getX(), block.getY(), block.getZ(),
                    appearances
            ).staticProjection();
            String jsonModel = snapshot.staticJsonModelResource().orElse(null);
            boolean rendered = jsonModel == null
                    ? emitter.render(
                            block,
                            advancedQuads(snapshot),
                            tileModel,
                            blockColor
                    )
                    : stock.renderModel(
                            block,
                            jsonModel,
                            0,
                            0,
                            tileModel,
                            blockColor
                    );
            if (!rendered) {
                fallback(block, tileModel, blockColor, renderStart);
            }
        } catch (MaxCapacityReachedException exception) {
            throw exception;
        } catch (RuntimeException | LinkageError exception) {
            disableAndFallback(route, block, tileModel, blockColor, renderStart);
        }
    }

    private void renderAthena(
            BlockNeighborhood block,
            TileModelView tileModel,
            Color blockColor,
            int renderStart
    ) {
        String routeId = M45Runtime.ADVANCED_ATHENA;
        if (!runtime.active(routeId)) {
            stock.renderOriginal(block, tileModel, blockColor, renderStart);
            return;
        }
        ExtensionRouteActivation route = runtime.route(routeId);
        try {
            BlockState center = block.getBlockState();
            if (!center.getProperties().isEmpty()) {
                fallback(block, tileModel, blockColor, renderStart);
                return;
            }
            Map<AdvancedAeAthenaSnapshot.Offset, Boolean> matches =
                    athenaMatches(block, center);
            if (matches == null) {
                fallback(block, tileModel, blockColor, renderStart);
                return;
            }
            AdvancedAeAthenaSnapshot snapshot =
                    AdvancedAeAthenaSnapshot.observedWholeStateMatches(
                            center.getId().getFormatted(),
                            matches
                    );
            if (!emitter.render(
                    block,
                    athenaQuads(snapshot),
                    tileModel,
                    blockColor
            )) {
                fallback(block, tileModel, blockColor, renderStart);
            }
        } catch (MaxCapacityReachedException exception) {
            throw exception;
        } catch (RuntimeException | LinkageError exception) {
            disableAndFallback(route, block, tileModel, blockColor, renderStart);
        }
    }

    private void renderMatrix(
            BlockNeighborhood block,
            TileModelView tileModel,
            Color blockColor,
            int renderStart
    ) {
        String routeId = M45Runtime.EXTENDED_MATRIX;
        if (!runtime.active(routeId)) {
            stock.renderOriginal(block, tileModel, blockColor, renderStart);
            return;
        }
        ExtensionRouteActivation route = runtime.route(routeId);
        try {
            BlockState state = block.getBlockState();
            MatrixDefinition definition = ExtendedAe2235Catalog.requireMatrixDefinition(
                    state.getId().getFormatted()
            );
            ExtendedAeMatrixSnapshot snapshot = matrixState(definition, state);
            if (snapshot == null || !exactBlockEntity(block, definition.blockEntityId())) {
                fallback(block, tileModel, blockColor, renderStart);
                return;
            }
            boolean rendered;
            if (definition.kind() == MatrixKind.GLASS) {
                Set<ExtendedAeMatrixGlassSnapshot.Offset> matching =
                        matrixGlassMatches(block);
                rendered = matching != null && emitter.render(
                        block,
                        matrixGlassQuads(ExtendedAeMatrixGlassSnapshot.observed(
                                snapshot.formed(),
                                snapshot.powered(),
                                block.getX(), block.getY(), block.getZ(),
                                matching
                        )),
                        tileModel,
                        blockColor
                );
            } else {
                ExtendedAeMatrixSnapshot.ModelSelection selection =
                        snapshot.staticProjection().staticModelSelection();
                rendered = stock.renderModel(
                        block,
                        selection.modelResource(),
                        selection.xRotation(),
                        selection.yRotation(),
                        tileModel,
                        blockColor
                );
            }
            if (!rendered) {
                fallback(block, tileModel, blockColor, renderStart);
            }
        } catch (MaxCapacityReachedException exception) {
            throw exception;
        } catch (RuntimeException | LinkageError exception) {
            disableAndFallback(route, block, tileModel, blockColor, renderStart);
        }
    }

    private static AdvancedState advancedState(BlockState state) {
        Map<String, String> properties = state.getProperties();
        if (!properties.keySet().equals(ADVANCED_STATE_KEYS)
                || !BOOLEAN_VALUES.contains(properties.get(
                        AdvancedAe1612Catalog.FORMED_PROPERTY
                ))
                || !BOOLEAN_VALUES.contains(properties.get(
                        AdvancedAe1612Catalog.POWERED_PROPERTY
                ))
                || !BOOLEAN_VALUES.contains(properties.get(
                        AdvancedAe1612Catalog.MULTIBLOCKED_PROPERTY
                ))) {
            return null;
        }
        int lightLevel;
        try {
            lightLevel = Integer.parseInt(properties.get(
                    AdvancedAe1612Catalog.LIGHT_LEVEL_PROPERTY
            ));
        } catch (NumberFormatException exception) {
            return null;
        }
        if (lightLevel < 0 || lightLevel > 15) {
            return null;
        }
        return new AdvancedState(
                Boolean.parseBoolean(properties.get(AdvancedAe1612Catalog.FORMED_PROPERTY)),
                Boolean.parseBoolean(properties.get(AdvancedAe1612Catalog.POWERED_PROPERTY)),
                Boolean.parseBoolean(properties.get(
                        AdvancedAe1612Catalog.MULTIBLOCKED_PROPERTY
                )),
                lightLevel
        );
    }

    private static ExtendedAeMatrixSnapshot matrixState(
            MatrixDefinition definition,
            BlockState state
    ) {
        Map<String, String> properties = state.getProperties();
        Set<String> expected = definition.kind() == MatrixKind.FRAME
                ? MATRIX_FRAME_STATE_KEYS : MATRIX_STATE_KEYS;
        if (!properties.keySet().equals(expected)
                || !BOOLEAN_VALUES.contains(properties.get(
                        ExtendedAe2235Catalog.FORMED_PROPERTY
                ))
                || !BOOLEAN_VALUES.contains(properties.get(
                        ExtendedAe2235Catalog.POWERED_PROPERTY
                ))) {
            return null;
        }
        ExtendedAeMatrixSnapshot.FrameShape shape;
        try {
            shape = definition.kind() == MatrixKind.FRAME
                    ? ExtendedAeMatrixSnapshot.FrameShape.fromSerializedName(
                            properties.get(ExtendedAe2235Catalog.SHAPE_PROPERTY)
                    )
                    : ExtendedAeMatrixSnapshot.FrameShape.BLOCK;
        } catch (IllegalArgumentException exception) {
            return null;
        }
        return new ExtendedAeMatrixSnapshot(
                definition.kind(),
                Boolean.parseBoolean(properties.get(
                        ExtendedAe2235Catalog.FORMED_PROPERTY
                )),
                Boolean.parseBoolean(properties.get(
                        ExtendedAe2235Catalog.POWERED_PROPERTY
                )),
                shape
        );
    }

    private static Map<AdvancedQuantumSnapshot.Offset, String> advancedAppearances(
            BlockNeighborhood block
    ) {
        Map<AdvancedQuantumSnapshot.Offset, String> result = new LinkedHashMap<>();
        for (AdvancedQuantumSnapshot.Offset offset : AdvancedQuantumSnapshot.scanOffsets()) {
            BlockState state = stateAt(block, offset.x(), offset.y(), offset.z());
            if (BlockState.MISSING.equals(state)) {
                return null;
            }
            result.put(offset, state.getId().getFormatted());
        }
        return Map.copyOf(result);
    }

    private static Map<AdvancedAeAthenaSnapshot.Offset, Boolean> athenaMatches(
            BlockNeighborhood block,
            BlockState center
    ) {
        Map<AdvancedAeAthenaSnapshot.Offset, Boolean> result = new LinkedHashMap<>();
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    if (x == 0 && y == 0 && z == 0) {
                        continue;
                    }
                    BlockState state = stateAt(block, x, y, z);
                    if (BlockState.MISSING.equals(state)) {
                        return null;
                    }
                    result.put(
                            new AdvancedAeAthenaSnapshot.Offset(x, y, z),
                            center.equals(state)
                    );
                }
            }
        }
        return Map.copyOf(result);
    }

    private static Set<ExtendedAeMatrixGlassSnapshot.Offset> matrixGlassMatches(
            BlockNeighborhood block
    ) {
        Set<ExtendedAeMatrixGlassSnapshot.Offset> result = new LinkedHashSet<>();
        for (ExtendedAeMatrixGlassSnapshot.Offset offset
                : ExtendedAeMatrixGlassSnapshot.scanOffsets()) {
            BlockState state = stateAt(block, offset.x(), offset.y(), offset.z());
            if (BlockState.MISSING.equals(state)) {
                return null;
            }
            if ("extendedae:assembler_matrix_glass".equals(
                    state.getId().getFormatted()
            )) {
                result.add(offset);
            }
        }
        return Set.copyOf(result);
    }

    private static List<M45MeshEmitter.Quad> advancedQuads(
            AdvancedQuantumSnapshot snapshot
    ) {
        List<M45MeshEmitter.Quad> result = new ArrayList<>();
        for (AdvancedQuantumGeometry.Quad quad
                : AdvancedQuantumGeometry.forSnapshot(snapshot)) {
            AdvancedQuantumGeometry.Material material =
                    AdvancedQuantumGeometry.material(snapshot.kind(), quad.surface());
            result.add(new M45MeshEmitter.Quad(
                    quad.face(),
                    quad.face(),
                    Key.parse(material.texture()),
                    quad.vertices().stream().map(vertex -> new M45MeshEmitter.Vertex(
                            vertex.x16(), vertex.y16(), vertex.z16(),
                            vertex.u16(), vertex.v16()
                    )).toList(),
                    material.emissive(),
                    material.ambientOcclusion(),
                    quad.surface() != AdvancedQuantumGeometry.Surface.STRUCTURE_SIDE_INNER
            ));
        }
        return List.copyOf(result);
    }

    private static List<M45MeshEmitter.Quad> athenaQuads(
            AdvancedAeAthenaSnapshot snapshot
    ) {
        return AdvancedAeAthenaGeometry.forSnapshot(snapshot).stream()
                .map(quad -> M45MeshEmitter.Quad.outward(
                        quad.face(),
                        M45AthenaTextures.staticFrame(quad.texture().textureId()),
                        athenaVertices(quad.face(), quad.region()),
                        false,
                        true
                ))
                .toList();
    }

    private static List<M45MeshEmitter.Quad> matrixGlassQuads(
            ExtendedAeMatrixGlassSnapshot snapshot
    ) {
        return ExtendedAeMatrixGlassGeometry.forSnapshot(snapshot).stream()
                .map(quad -> {
                    ExtendedAeMatrixGlassGeometry.Material material = quad.material();
                    return M45MeshEmitter.Quad.outward(
                            quad.face(),
                            Key.parse(material.texture()),
                            quad.vertices().stream().map(vertex ->
                                    new M45MeshEmitter.Vertex(
                                            vertex.x16(), vertex.y16(), vertex.z16(),
                                            vertex.u16(), vertex.v16()
                                    )).toList(),
                            material.emissive(),
                            material.ambientOcclusion()
                    );
                })
                .toList();
    }

    private static List<M45MeshEmitter.Vertex> athenaVertices(
            Direction6 face,
            AdvancedAeAthenaGeometry.Region region
    ) {
        List<Position> full = facePositions(face);
        double left = region.left();
        double right = region.right();
        double top = region.top();
        double bottom = region.bottom();
        return List.of(
                athenaVertex(full, left, top),
                athenaVertex(full, left, bottom),
                athenaVertex(full, right, bottom),
                athenaVertex(full, right, top)
        );
    }

    private static M45MeshEmitter.Vertex athenaVertex(
            List<Position> full,
            double horizontal,
            double vertical
    ) {
        Position top = interpolate(full.get(0), full.get(3), horizontal);
        Position bottom = interpolate(full.get(1), full.get(2), horizontal);
        Position position = interpolate(bottom, top, vertical);
        return new M45MeshEmitter.Vertex(
                position.x16(), position.y16(), position.z16(),
                horizontal * 16, (1 - vertical) * 16
        );
    }

    private static Position interpolate(Position first, Position second, double amount) {
        return new Position(
                first.x16() + (second.x16() - first.x16()) * amount,
                first.y16() + (second.y16() - first.y16()) * amount,
                first.z16() + (second.z16() - first.z16()) * amount
        );
    }

    private static List<Position> facePositions(Direction6 face) {
        return switch (face) {
            case DOWN -> List.of(
                    new Position(0, 0, 16), new Position(0, 0, 0),
                    new Position(16, 0, 0), new Position(16, 0, 16)
            );
            case UP -> List.of(
                    new Position(16, 16, 16), new Position(16, 16, 0),
                    new Position(0, 16, 0), new Position(0, 16, 16)
            );
            case NORTH -> List.of(
                    new Position(16, 16, 0), new Position(16, 0, 0),
                    new Position(0, 0, 0), new Position(0, 16, 0)
            );
            case SOUTH -> List.of(
                    new Position(0, 16, 16), new Position(0, 0, 16),
                    new Position(16, 0, 16), new Position(16, 16, 16)
            );
            case WEST -> List.of(
                    new Position(0, 16, 0), new Position(0, 0, 0),
                    new Position(0, 0, 16), new Position(0, 16, 16)
            );
            case EAST -> List.of(
                    new Position(16, 16, 16), new Position(16, 0, 16),
                    new Position(16, 0, 0), new Position(16, 16, 0)
            );
        };
    }

    private static BlockState stateAt(BlockNeighborhood block, int x, int y, int z) {
        if (x == 0 && y == 0 && z == 0) {
            return block.getBlockState();
        }
        return block.getNeighborBlock(x, y, z).getBlockState();
    }

    private static boolean exactBlockEntity(BlockNeighborhood block, String expectedId) {
        BlockEntity blockEntity = block.getBlockEntity();
        return blockEntity != null
                && blockEntity.getId() != null
                && Key.parse(expectedId).equals(blockEntity.getId());
    }

    private void disableAndFallback(
            ExtensionRouteActivation route,
            BlockNeighborhood block,
            TileModelView tileModel,
            Color blockColor,
            int renderStart
    ) {
        route.disable(
                ExtensionRouteActivation.Reason.RENDER_CALLBACK_FAILED,
                "render-callback-failed"
        );
        fallback(block, tileModel, blockColor, renderStart);
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

    private record AdvancedState(
            boolean formed,
            boolean powered,
            boolean multiblocked,
            int lightLevel
    ) {
    }

    private record Position(double x16, double y16, double z16) {
    }
}
