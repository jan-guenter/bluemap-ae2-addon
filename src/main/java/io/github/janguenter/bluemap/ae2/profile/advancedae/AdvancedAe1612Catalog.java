/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.profile.advancedae;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Closed quantum-computer and Athena world-visual catalog from Advanced AE 1.6.12. */
public final class AdvancedAe1612Catalog {

    public static final String QUANTUM_BLOCK_ENTITY = "advanced_ae:quantum_core";
    public static final String QUANTUM_ALLOY_BLOCK = "advanced_ae:quantum_alloy_block";
    public static final String FORMED_PROPERTY = "formed";
    public static final String POWERED_PROPERTY = "powered";
    public static final String MULTIBLOCKED_PROPERTY = "multiblocked";
    public static final String LIGHT_LEVEL_PROPERTY = "light_level";
    public static final String STATIC_POLICY =
            "static-unpowered-light-zero-non-emissive-frame-zero";
    public static final String MACHINE_CONTENT_POLICY = "not-rendered";
    public static final String QUANTUM_APPEARANCE_POLICY =
            "complete-3x3x3-getAppearance-north-center-state;classify-exact-eight-"
                    + "AAECraftingUnitBlock-ids;structure-connects-only-structure;internal-"
                    + "connects-any-non-structure-role;missing-unknown-malformed-incompatible";

    private static final List<String> BOOLEAN_DOMAIN = List.of("false", "true");
    private static final List<String> LIGHT_LEVEL_DOMAIN = List.of(
            "0", "1", "2", "3", "4", "5", "6", "7",
            "8", "9", "10", "11", "12", "13", "14", "15"
    );
    private static final Map<String, List<String>> QUANTUM_PERSISTED_STATE_DOMAINS =
            buildQuantumPersistedStateDomains();

    private static final Map<String, QuantumDefinition> QUANTUM_DEFINITIONS =
            buildQuantumDefinitions();
    private static final Set<String> INTERNAL_BLOCKS = buildInternalBlocks();
    private static final List<String> QUANTUM_TEXTURE_RESOURCES = List.of(
            "assets/advanced_ae/textures/block/crafting/data_entangler.png",
            "assets/advanced_ae/textures/block/crafting/quantum_accelerator.png",
            "assets/advanced_ae/textures/block/crafting/quantum_core.png",
            "assets/advanced_ae/textures/block/crafting/quantum_core_nucleus.png",
            "assets/advanced_ae/textures/block/crafting/quantum_core_nucleus.png.mcmeta",
            "assets/advanced_ae/textures/block/crafting/quantum_core_out.png",
            "assets/advanced_ae/textures/block/crafting/quantum_internal_formed_face.png",
            "assets/advanced_ae/textures/block/crafting/quantum_internal_formed_sides.png",
            "assets/advanced_ae/textures/block/crafting/quantum_internal_powered_animation.png",
            "assets/advanced_ae/textures/block/crafting/quantum_internal_powered_animation.png.mcmeta",
            "assets/advanced_ae/textures/block/crafting/quantum_internal_powered_animation_tb.png",
            "assets/advanced_ae/textures/block/crafting/quantum_internal_powered_animation_tb.png.mcmeta",
            "assets/advanced_ae/textures/block/crafting/quantum_internal_powered_sides.png",
            "assets/advanced_ae/textures/block/crafting/quantum_internal_powered_sides.png.mcmeta",
            "assets/advanced_ae/textures/block/crafting/quantum_multi_threader.png",
            "assets/advanced_ae/textures/block/crafting/quantum_storage_128.png",
            "assets/advanced_ae/textures/block/crafting/quantum_storage_256.png",
            "assets/advanced_ae/textures/block/crafting/quantum_structure.png",
            "assets/advanced_ae/textures/block/crafting/quantum_structure_formed_face.png",
            "assets/advanced_ae/textures/block/crafting/quantum_structure_formed_sides.png",
            "assets/advanced_ae/textures/block/crafting/quantum_structure_powered_sides.png",
            "assets/advanced_ae/textures/block/crafting/quantum_structure_powered_sides.png.mcmeta",
            "assets/advanced_ae/textures/block/crafting/quantum_structure_powered_sides_glowmask.png",
            "assets/advanced_ae/textures/block/crafting/quantum_unit.png"
    );

    private AdvancedAe1612Catalog() {
    }

    public static Map<String, QuantumDefinition> quantumDefinitions() {
        return QUANTUM_DEFINITIONS;
    }

    public static Set<String> quantumBlockIds() {
        return QUANTUM_DEFINITIONS.keySet();
    }

    public static Set<String> internalBlockIds() {
        return INTERNAL_BLOCKS;
    }

    public static Set<String> structureBlockIds() {
        return Set.of("advanced_ae:quantum_structure");
    }

    public static List<String> quantumTextureResources() {
        return QUANTUM_TEXTURE_RESOURCES;
    }

    /** Exact properties serialized by every one of the eight quantum block roles. */
    public static Map<String, List<String>> quantumPersistedStateDomains() {
        return QUANTUM_PERSISTED_STATE_DOMAINS;
    }

    public static QuantumDefinition requireQuantumDefinition(String blockId) {
        QuantumDefinition definition = QUANTUM_DEFINITIONS.get(blockId);
        if (definition == null) {
            throw new IllegalArgumentException("unsupported Advanced AE quantum block");
        }
        return definition;
    }

    public static QuantumDefinition requireQuantumDefinition(QuantumKind kind) {
        if (kind == null) {
            throw new IllegalArgumentException("unsupported Advanced AE quantum role");
        }
        return QUANTUM_DEFINITIONS.values().stream()
                .filter(definition -> definition.kind() == kind)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "unsupported Advanced AE quantum role"
                ));
    }

    /** Base JSON model used by every unformed role and by a standalone static core. */
    public static String unformedModelResource(QuantumKind kind) {
        if (kind == QuantumKind.STRUCTURE) {
            throw new IllegalArgumentException("quantum structure has no unformed JSON model");
        }
        return requireQuantumDefinition(kind).modelResources().getFirst();
    }

    /** Returns null for a missing, unknown or malformed appearance block ID. */
    public static QuantumKind quantumKindOrNull(String appearanceBlockId) {
        QuantumDefinition definition = QUANTUM_DEFINITIONS.get(appearanceBlockId);
        return definition == null ? null : definition.kind();
    }

    private static Map<String, QuantumDefinition> buildQuantumDefinitions() {
        Map<String, QuantumDefinition> result = new LinkedHashMap<>();
        add(result, QuantumKind.UNIT, "quantum_unit", List.of(
                model("crafting/quantum_unit"),
                model("crafting/quantum_unit_formed")
        ));
        add(result, QuantumKind.CORE, "quantum_core", List.of(
                model("quantum_core"),
                model("quantum_core_formed"),
                model("quantum_core_formed_on")
        ));
        add(result, QuantumKind.STORAGE_128, "quantum_storage_128", List.of(
                model("crafting/quantum_storage_128"),
                model("crafting/quantum_storage_128_formed")
        ));
        add(result, QuantumKind.STORAGE_256, "quantum_storage_256", List.of(
                model("crafting/quantum_storage_256"),
                model("crafting/quantum_storage_256_formed")
        ));
        add(result, QuantumKind.DATA_ENTANGLER, "data_entangler", List.of(
                model("crafting/data_entangler"),
                model("crafting/data_entangler_formed")
        ));
        add(result, QuantumKind.ACCELERATOR, "quantum_accelerator", List.of(
                model("crafting/quantum_accelerator"),
                model("crafting/quantum_accelerator_formed")
        ));
        add(result, QuantumKind.MULTI_THREADER, "quantum_multi_threader", List.of(
                model("crafting/quantum_multi_threader"),
                model("crafting/quantum_multi_threader_formed")
        ));
        add(result, QuantumKind.STRUCTURE, "quantum_structure", List.of(
                model("crafting/quantum_structure_formed")
        ));
        if (result.size() != 8) {
            throw new IllegalStateException("invalid exact Advanced AE quantum catalog");
        }
        return Collections.unmodifiableMap(result);
    }

    private static Map<String, List<String>> buildQuantumPersistedStateDomains() {
        Map<String, List<String>> result = new LinkedHashMap<>();
        result.put(FORMED_PROPERTY, BOOLEAN_DOMAIN);
        result.put(POWERED_PROPERTY, BOOLEAN_DOMAIN);
        result.put(MULTIBLOCKED_PROPERTY, BOOLEAN_DOMAIN);
        result.put(LIGHT_LEVEL_PROPERTY, LIGHT_LEVEL_DOMAIN);
        return Collections.unmodifiableMap(result);
    }

    private static Set<String> buildInternalBlocks() {
        Set<String> result = new LinkedHashSet<>(QUANTUM_DEFINITIONS.keySet());
        if (!result.remove("advanced_ae:quantum_structure") || result.size() != 7) {
            throw new IllegalStateException("invalid Advanced AE internal-role catalog");
        }
        return Collections.unmodifiableSet(result);
    }

    private static void add(
            Map<String, QuantumDefinition> definitions,
            QuantumKind kind,
            String path,
            List<String> models
    ) {
        String blockId = "advanced_ae:" + path;
        QuantumDefinition previous = definitions.put(blockId, new QuantumDefinition(
                blockId,
                kind,
                QUANTUM_BLOCK_ENTITY,
                "assets/advanced_ae/blockstates/" + path + ".json",
                models
        ));
        if (previous != null) {
            throw new IllegalStateException("duplicate Advanced AE quantum block");
        }
    }

    private static String model(String path) {
        return "assets/advanced_ae/models/block/" + path + ".json";
    }

    public enum QuantumKind {
        UNIT,
        CORE,
        STORAGE_128,
        STORAGE_256,
        DATA_ENTANGLER,
        ACCELERATOR,
        MULTI_THREADER,
        STRUCTURE;

        public boolean isStructure() {
            return this == STRUCTURE;
        }
    }

    /** Exact registry, block-entity and source-model identity for one quantum block. */
    public record QuantumDefinition(
            String blockId,
            QuantumKind kind,
            String blockEntityId,
            String blockstateResource,
            List<String> modelResources
    ) {

        public QuantumDefinition {
            if (!blockId.startsWith("advanced_ae:")
                    || !QUANTUM_BLOCK_ENTITY.equals(blockEntityId)
                    || !blockstateResource.startsWith("assets/advanced_ae/blockstates/")
                    || modelResources.isEmpty()) {
                throw new IllegalArgumentException("invalid Advanced AE quantum definition");
            }
            modelResources = List.copyOf(modelResources);
        }

        public Map<String, List<String>> persistedStateDomains() {
            return QUANTUM_PERSISTED_STATE_DOMAINS;
        }

        public Set<String> persistedStateProperties() {
            return QUANTUM_PERSISTED_STATE_DOMAINS.keySet();
        }

        /** Rejects missing, extra, null or out-of-domain persisted properties. */
        public boolean acceptsPersistedState(Map<String, String> state) {
            if (state == null || !state.keySet().equals(persistedStateProperties())) {
                return false;
            }
            return persistedStateDomains().entrySet().stream().allMatch(entry ->
                    entry.getValue().contains(state.get(entry.getKey()))
            );
        }
    }
}
