/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.profile.extendedae;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Closed Assembler Matrix and plane visual catalog from ExtendedAE 2.2.35. */
public final class ExtendedAe2235Catalog {

    public static final String FORMED_PROPERTY = "formed";
    public static final String POWERED_PROPERTY = "powered";
    public static final String SHAPE_PROPERTY = "shape";
    public static final String STATIC_MATRIX_POLICY = "static-unpowered-preserve-formed-and-shape";
    public static final String STATIC_PLANE_POLICY = "static-off-no-spin-preserve-connections";
    public static final String MATRIX_GLASS_APPEARANCE_POLICY =
            "complete-3x3x3-getAppearance-north-center-state;match-appearance-block-id-"
                    + "extendedae:assembler_matrix_glass-not-whole-state;missing-unknown-"
                    + "malformed-incompatible";
    public static final String PLANE_HOST_GEOMETRY_POLICY =
            "reuse-existing-native-cable-bus-center-connections-and-collars;add-native-"
                    + "plane-mask-plate;exact-cable-connection-length-one";
    public static final String PLANE_SIDE_TEXTURE = "ae2:part/plane_sides";
    public static final String PLANE_BACK_TEXTURE = "ae2:part/transition_plane_back";
    public static final int PLANE_CABLE_CONNECTION_LENGTH = 1;
    public static final int PLANE_CONNECTION_MASK_COUNT = 16;

    private static final List<String> BOOLEAN_DOMAIN = List.of("false", "true");
    private static final List<String> FRAME_SHAPE_DOMAIN = List.of(
            "block", "column_x", "column_y", "column_z"
    );
    private static final Map<String, List<String>> MATRIX_PERSISTED_STATE_DOMAINS =
            buildMatrixPersistedStateDomains(false);
    private static final Map<String, List<String>> FRAME_PERSISTED_STATE_DOMAINS =
            buildMatrixPersistedStateDomains(true);

    private static final Map<String, MatrixDefinition> MATRIX_DEFINITIONS =
            buildMatrixDefinitions();
    private static final Map<String, PlaneDefinition> PLANE_DEFINITIONS =
            buildPlaneDefinitions();
    private static final List<String> MATRIX_TEXTURE_RESOURCES = List.of(
            texture("block/assembler_matrix/crafter_core.png"),
            texture("block/assembler_matrix/frame_block_off.png"),
            texture("block/assembler_matrix/frame_block_on.png"),
            texture("block/assembler_matrix/frame_column_off.png"),
            texture("block/assembler_matrix/frame_column_on.png"),
            texture("block/assembler_matrix/glass/face_a.png"),
            texture("block/assembler_matrix/glass/face_b.png"),
            texture("block/assembler_matrix/glass/face_c.png"),
            texture("block/assembler_matrix/glass/full.png"),
            texture("block/assembler_matrix/glass/sides.png"),
            texture("block/assembler_matrix/pattern_core.png"),
            texture("block/assembler_matrix/speed_core.png"),
            texture("block/assembler_matrix/wall_block.png")
    );

    private ExtendedAe2235Catalog() {
    }

    public static Map<String, MatrixDefinition> matrixDefinitions() {
        return MATRIX_DEFINITIONS;
    }

    public static Set<String> matrixBlockIds() {
        return MATRIX_DEFINITIONS.keySet();
    }

    public static MatrixDefinition requireMatrixDefinition(String blockId) {
        MatrixDefinition definition = MATRIX_DEFINITIONS.get(blockId);
        if (definition == null) {
            throw new IllegalArgumentException("unsupported ExtendedAE matrix block");
        }
        return definition;
    }

    public static List<String> matrixTextureResources() {
        return MATRIX_TEXTURE_RESOURCES;
    }

    public static Map<String, PlaneDefinition> planeDefinitions() {
        return PLANE_DEFINITIONS;
    }

    public static Set<String> planePartIds() {
        return PLANE_DEFINITIONS.keySet();
    }

    public static PlaneDefinition requirePlaneDefinition(String partId) {
        PlaneDefinition definition = PLANE_DEFINITIONS.get(partId);
        if (definition == null) {
            throw new IllegalArgumentException("unsupported ExtendedAE plane part");
        }
        return definition;
    }

    private static Map<String, MatrixDefinition> buildMatrixDefinitions() {
        Map<String, MatrixDefinition> result = new LinkedHashMap<>();
        addMatrix(result, MatrixKind.FRAME, "frame", List.of(
                matrixModel("frame_block_off"),
                matrixModel("frame_block_on"),
                matrixModel("frame_column_off"),
                matrixModel("frame_column_on")
        ), Set.of(FORMED_PROPERTY, POWERED_PROPERTY, SHAPE_PROPERTY), false);
        addMatrix(result, MatrixKind.WALL, "wall", List.of(
                matrixModel("wall")
        ), Set.of(FORMED_PROPERTY, POWERED_PROPERTY), false);
        addMatrix(result, MatrixKind.GLASS, "glass", List.of(
                "assets/extendedae/models/block/assembler_matrix_glass.json"
        ), Set.of(FORMED_PROPERTY, POWERED_PROPERTY), true);
        addMatrix(result, MatrixKind.PATTERN, "pattern", List.of(
                matrixModel("pattern")
        ), Set.of(FORMED_PROPERTY, POWERED_PROPERTY), false);
        addMatrix(result, MatrixKind.CRAFTER, "crafter", List.of(
                matrixModel("crafter")
        ), Set.of(FORMED_PROPERTY, POWERED_PROPERTY), false);
        addMatrix(result, MatrixKind.SPEED, "speed", List.of(
                matrixModel("speed")
        ), Set.of(FORMED_PROPERTY, POWERED_PROPERTY), false);
        if (result.size() != 6) {
            throw new IllegalStateException("invalid exact ExtendedAE matrix catalog");
        }
        return Collections.unmodifiableMap(result);
    }

    private static Map<String, List<String>> buildMatrixPersistedStateDomains(
            boolean includeShape
    ) {
        Map<String, List<String>> result = new LinkedHashMap<>();
        result.put(FORMED_PROPERTY, BOOLEAN_DOMAIN);
        result.put(POWERED_PROPERTY, BOOLEAN_DOMAIN);
        if (includeShape) {
            result.put(SHAPE_PROPERTY, FRAME_SHAPE_DOMAIN);
        }
        return Collections.unmodifiableMap(result);
    }

    private static Map<String, List<String>> persistedStateDomains(MatrixKind kind) {
        return kind == MatrixKind.FRAME
                ? FRAME_PERSISTED_STATE_DOMAINS
                : MATRIX_PERSISTED_STATE_DOMAINS;
    }

    private static void addMatrix(
            Map<String, MatrixDefinition> definitions,
            MatrixKind kind,
            String path,
            List<String> models,
            Set<String> stateProperties,
            boolean connectedGeometry
    ) {
        String blockId = "extendedae:assembler_matrix_" + path;
        MatrixDefinition definition = new MatrixDefinition(
                blockId,
                kind,
                blockId,
                "assets/extendedae/blockstates/assembler_matrix_" + path + ".json",
                models,
                stateProperties,
                connectedGeometry
        );
        if (definitions.put(blockId, definition) != null) {
            throw new IllegalStateException("duplicate ExtendedAE matrix block");
        }
    }

    private static Map<String, PlaneDefinition> buildPlaneDefinitions() {
        Map<String, PlaneDefinition> result = new LinkedHashMap<>();
        addPlane(result, PlaneKind.ACTIVE_FORMATION, "active_formation_plane");
        addPlane(result, PlaneKind.SMART_ANNIHILATION, "smart_annihilation_plane");
        if (result.size() != 2) {
            throw new IllegalStateException("invalid exact ExtendedAE plane catalog");
        }
        return Collections.unmodifiableMap(result);
    }

    private static void addPlane(
            Map<String, PlaneDefinition> definitions,
            PlaneKind kind,
            String path
    ) {
        String partId = "extendedae:" + path;
        PlaneDefinition definition = new PlaneDefinition(
                partId,
                kind,
                "assets/extendedae/models/part/" + path + ".json",
                "assets/extendedae/models/part/" + path + "_on.json",
                texture("part/" + path + ".png"),
                texture("part/" + path + "_on.png"),
                texture("part/" + path + "_on.png.mcmeta"),
                "extendedae:part/" + path,
                "extendedae:part/" + path + "_on",
                PLANE_SIDE_TEXTURE,
                PLANE_BACK_TEXTURE,
                false,
                PLANE_CABLE_CONNECTION_LENGTH
        );
        if (definitions.put(partId, definition) != null) {
            throw new IllegalStateException("duplicate ExtendedAE plane part");
        }
    }

    private static String matrixModel(String path) {
        return "assets/extendedae/models/block/assembler_matrix/" + path + ".json";
    }

    private static String texture(String path) {
        return "assets/extendedae/textures/" + path;
    }

    public enum MatrixKind {
        FRAME,
        WALL,
        GLASS,
        PATTERN,
        CRAFTER,
        SPEED
    }

    public enum PlaneKind {
        ACTIVE_FORMATION,
        SMART_ANNIHILATION
    }

    /** Exact registry, block-entity and model identity for one matrix block. */
    public record MatrixDefinition(
            String blockId,
            MatrixKind kind,
            String blockEntityId,
            String blockstateResource,
            List<String> modelResources,
            Set<String> stateProperties,
            boolean connectedGeometry
    ) {

        public MatrixDefinition {
            if (!blockId.startsWith("extendedae:assembler_matrix_")
                    || !blockId.equals(blockEntityId)
                    || !blockstateResource.startsWith("assets/extendedae/blockstates/")
                    || modelResources.isEmpty()
                    || !stateProperties.equals(
                            ExtendedAe2235Catalog.persistedStateDomains(kind).keySet()
                    )) {
                throw new IllegalArgumentException("invalid ExtendedAE matrix definition");
            }
            modelResources = List.copyOf(modelResources);
            stateProperties = Set.copyOf(stateProperties);
        }

        public Map<String, List<String>> persistedStateDomains() {
            return ExtendedAe2235Catalog.persistedStateDomains(kind);
        }

        /** Rejects missing, extra, null or out-of-domain persisted properties. */
        public boolean acceptsPersistedState(Map<String, String> state) {
            if (state == null || !state.keySet().equals(stateProperties)) {
                return false;
            }
            return persistedStateDomains().entrySet().stream().allMatch(entry ->
                    entry.getValue().contains(state.get(entry.getKey()))
            );
        }
    }

    /** Exact off/on resource and native plane-geometry contract for one part. */
    public record PlaneDefinition(
            String partId,
            PlaneKind kind,
            String offModelResource,
            String onModelResource,
            String offFrontResource,
            String onFrontResource,
            String onFrontMetadataResource,
            String offFrontTexture,
            String onFrontTexture,
            String sideTexture,
            String backTexture,
            boolean supportsSpin,
            int cableConnectionLength
    ) {

        public PlaneDefinition {
            if (!partId.startsWith("extendedae:")
                    || !offModelResource.startsWith("assets/extendedae/models/part/")
                    || !onModelResource.startsWith("assets/extendedae/models/part/")
                    || !offFrontResource.startsWith("assets/extendedae/textures/part/")
                    || !onFrontResource.startsWith("assets/extendedae/textures/part/")
                    || !onFrontMetadataResource.endsWith(".png.mcmeta")
                    || !offFrontTexture.startsWith("extendedae:part/")
                    || !onFrontTexture.endsWith("_on")
                    || !PLANE_SIDE_TEXTURE.equals(sideTexture)
                    || !PLANE_BACK_TEXTURE.equals(backTexture)
                    || supportsSpin
                    || cableConnectionLength != PLANE_CABLE_CONNECTION_LENGTH) {
                throw new IllegalArgumentException("invalid ExtendedAE plane definition");
            }
        }

        public String staticModelResource() {
            return offModelResource;
        }

        public String staticFrontTexture() {
            return offFrontTexture;
        }
    }
}
