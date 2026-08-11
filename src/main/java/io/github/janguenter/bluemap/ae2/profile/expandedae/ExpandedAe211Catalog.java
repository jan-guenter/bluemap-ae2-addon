/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.profile.expandedae;

import io.github.janguenter.bluemap.ae2.model.CraftingBlockKind;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/** Closed world-render and state catalog proven from Expanded AE 2.1.1's exact JAR. */
public final class ExpandedAe211Catalog {

    public static final String PATTERN_PROVIDER_BLOCK =
            "expandedae:exp_pattern_provider";
    public static final String IO_PORT_BLOCK = "expandedae:exp_io_port";
    public static final String COLORABLE_DRIVE_BLOCK = "expandedae:colorable_drive";
    public static final String CRAFTING_UNIT_BLOCK = "expandedae:exp_crafting_unit";
    public static final String PATTERN_PROVIDER_BLOCK_ENTITY =
            "expandedae:exp_pattern_provider";
    public static final String IO_PORT_BLOCK_ENTITY = "expandedae:exp_io_port";
    public static final String COLORABLE_DRIVE_BLOCK_ENTITY =
            "expandedae:colorable_drive";
    public static final String CRAFTING_BLOCK_ENTITY = "expandedae:exp_cpus";
    public static final String PATTERN_PROVIDER_PART =
            "expandedae:exp_pattern_provider_part";
    public static final String ENCODING_TERMINAL_PART =
            "expandedae:exp_encoding_terminal";
    public static final String FORMED_PROPERTY = "formed";
    public static final String PUSH_DIRECTION_PROPERTY = "push_direction";
    public static final String FACING_PROPERTY = "facing";
    public static final String POWERED_PROPERTY = "powered";
    public static final String SPIN_PROPERTY = "spin";
    public static final String COLOR_PROPERTY = "color";
    public static final String COLOR_NBT_KEY = "color";
    public static final int IO_PORT_VARIANT_COUNT = 48;
    public static final int MIN_SPIN = 0;
    public static final int MAX_SPIN = 3;
    public static final int MIN_COLOR = 0;
    public static final int MAX_COLOR = 16;
    public static final String COLORABLE_DRIVE_POLICY =
            "original-resource-fallback-no-upstream-world-visual";

    private static final List<String> ACCELERATOR_TIERS = List.of(
            "2", "4", "8", "16", "32", "64", "128", "256", "512",
            "1k", "2k", "4k", "8k", "16k", "32k", "64k", "128k",
            "256k", "512k", "1m"
    );
    private static final Set<String> PUSH_DIRECTIONS = Set.of(
            "all", "east", "west", "south", "north", "up", "down"
    );
    private static final Set<String> FACINGS = Set.of(
            "down", "up", "north", "south", "west", "east"
    );
    private static final Map<String, CraftingDefinition> CRAFTING_DEFINITIONS =
            buildCraftingDefinitions();
    private static final Map<String, CraftingBlockKind> CRAFTING_BLOCK_KINDS =
            buildCraftingBlockKinds();
    private static final Set<String> REGISTERED_BLOCKS = buildRegisteredBlocks();
    private static final Set<String> SUPPORTED_VISUAL_BLOCKS = buildSupportedVisualBlocks();
    private static final Map<String, String> BLOCK_ENTITY_IDS = buildBlockEntityIds();
    private static final Map<String, PartDefinition> PARTS = buildParts();
    private static final List<String> DYNAMIC_CRAFTING_TEXTURES =
            buildDynamicCraftingTextures();

    private ExpandedAe211Catalog() {
    }

    public static List<String> acceleratorTiers() {
        return ACCELERATOR_TIERS;
    }

    public static Map<String, CraftingDefinition> craftingDefinitions() {
        return CRAFTING_DEFINITIONS;
    }

    public static Map<String, CraftingBlockKind> craftingBlockKinds() {
        return CRAFTING_BLOCK_KINDS;
    }

    public static CraftingBlockKind kindForCraftingBlock(String blockId) {
        return CRAFTING_BLOCK_KINDS.get(blockId);
    }

    /** All 24 exact registered blocks, including the resource-less colorable Drive. */
    public static Set<String> registeredBlocks() {
        return REGISTERED_BLOCKS;
    }

    /** The 23 blocks with an exact upstream world-visual contract. */
    public static Set<String> supportedVisualBlocks() {
        return SUPPORTED_VISUAL_BLOCKS;
    }

    public static Set<String> upstreamVisualUnavailableBlocks() {
        return Set.of(COLORABLE_DRIVE_BLOCK);
    }

    public static Set<String> ordinaryJsonBlocks() {
        return Set.of(PATTERN_PROVIDER_BLOCK);
    }

    public static Set<String> zNormalizedJsonBlocks() {
        return Set.of(IO_PORT_BLOCK);
    }

    public static Map<String, String> blockEntityIds() {
        return BLOCK_ENTITY_IDS;
    }

    public static Map<String, PartDefinition> parts() {
        return PARTS;
    }

    public static Set<String> pushDirections() {
        return PUSH_DIRECTIONS;
    }

    public static Set<String> facings() {
        return FACINGS;
    }

    /** Five shared provider textures plus twenty tier-specific light textures. */
    public static List<String> dynamicCraftingTextures() {
        return DYNAMIC_CRAFTING_TEXTURES;
    }

    private static Map<String, CraftingDefinition> buildCraftingDefinitions() {
        Map<String, CraftingDefinition> result = new LinkedHashMap<>();
        String unitName = "exp_crafting_unit";
        CraftingDefinition unit = new CraftingDefinition(
                CRAFTING_UNIT_BLOCK,
                CraftingBlockKind.UNIT,
                model(unitName),
                model(unitName + "_formed"),
                texture(unitName),
                texture("unit_base"),
                Optional.empty()
        );
        result.put(unit.blockId(), unit);
        for (String tier : ACCELERATOR_TIERS) {
            String name = "exp_crafting_accelerator_" + tier;
            CraftingDefinition accelerator = new CraftingDefinition(
                    "expandedae:" + name,
                    CraftingBlockKind.ACCELERATOR,
                    model(name),
                    model(name + "_formed"),
                    texture(name),
                    texture("light_base"),
                    Optional.of(texture(name + "_light"))
            );
            if (result.put(accelerator.blockId(), accelerator) != null) {
                throw new IllegalStateException("duplicate Expanded AE crafting block");
            }
        }
        if (result.size() != 21) {
            throw new IllegalStateException("invalid exact Expanded AE crafting catalog");
        }
        return Collections.unmodifiableMap(result);
    }

    private static Map<String, CraftingBlockKind> buildCraftingBlockKinds() {
        Map<String, CraftingBlockKind> result = new LinkedHashMap<>();
        CRAFTING_DEFINITIONS.forEach((blockId, definition) ->
                result.put(blockId, definition.kind()));
        if (result.size() != 21
                || result.values().stream().filter(CraftingBlockKind.UNIT::equals).count() != 1
                || result.values().stream().filter(CraftingBlockKind.ACCELERATOR::equals).count()
                != 20) {
            throw new IllegalStateException("invalid Expanded AE core crafting-kind mapping");
        }
        return Collections.unmodifiableMap(result);
    }

    private static Set<String> buildRegisteredBlocks() {
        Set<String> result = new LinkedHashSet<>(CRAFTING_DEFINITIONS.keySet());
        Collections.addAll(
                result,
                PATTERN_PROVIDER_BLOCK,
                IO_PORT_BLOCK,
                COLORABLE_DRIVE_BLOCK
        );
        if (result.size() != 24) {
            throw new IllegalStateException("invalid exact Expanded AE block registry");
        }
        return Collections.unmodifiableSet(result);
    }

    private static Set<String> buildSupportedVisualBlocks() {
        Set<String> result = new LinkedHashSet<>(REGISTERED_BLOCKS);
        if (!result.remove(COLORABLE_DRIVE_BLOCK) || result.size() != 23) {
            throw new IllegalStateException("invalid Expanded AE visual support set");
        }
        return Collections.unmodifiableSet(result);
    }

    private static Map<String, String> buildBlockEntityIds() {
        Map<String, String> result = new LinkedHashMap<>();
        result.put(PATTERN_PROVIDER_BLOCK, PATTERN_PROVIDER_BLOCK_ENTITY);
        result.put(IO_PORT_BLOCK, IO_PORT_BLOCK_ENTITY);
        result.put(COLORABLE_DRIVE_BLOCK, COLORABLE_DRIVE_BLOCK_ENTITY);
        CRAFTING_DEFINITIONS.keySet().forEach(block ->
                result.put(block, CRAFTING_BLOCK_ENTITY));
        if (result.size() != 24 || !result.keySet().equals(REGISTERED_BLOCKS)) {
            throw new IllegalStateException("invalid exact Expanded AE block-entity registry");
        }
        return Collections.unmodifiableMap(result);
    }

    private static Map<String, PartDefinition> buildParts() {
        Map<String, PartDefinition> result = new LinkedHashMap<>();
        result.put(PATTERN_PROVIDER_PART, new PartDefinition(
                PATTERN_PROVIDER_PART,
                List.of(
                        "expandedae:part/exp_pattern_provider_base",
                        "ae2:part/interface_off"
                ),
                false,
                "static-offline-unknown"
        ));
        result.put(ENCODING_TERMINAL_PART, new PartDefinition(
                ENCODING_TERMINAL_PART,
                List.of(
                        "ae2:part/display_base",
                        "expandedae:part/exp_encoding_terminal_off",
                        "ae2:part/display_status_off"
                ),
                true,
                "static-offline-unknown"
        ));
        return Collections.unmodifiableMap(result);
    }

    private static List<String> buildDynamicCraftingTextures() {
        Set<String> result = new LinkedHashSet<>(List.of(
                texture("ring_corner"),
                texture("ring_side_hor"),
                texture("ring_side_ver"),
                texture("unit_base"),
                texture("light_base")
        ));
        CRAFTING_DEFINITIONS.values().stream()
                .map(CraftingDefinition::dynamicLightTexture)
                .flatMap(Optional::stream)
                .forEach(result::add);
        if (result.size() != 25) {
            throw new IllegalStateException("invalid Expanded AE dynamic texture catalog");
        }
        return List.copyOf(result);
    }

    private static String model(String name) {
        return "expandedae:block/crafting/" + name;
    }

    private static String texture(String name) {
        return "expandedae:block/crafting/" + name;
    }

    /** Exact Expanded crafting registration projected onto the existing core mesh kind. */
    public record CraftingDefinition(
            String blockId,
            CraftingBlockKind kind,
            String unformedModel,
            String formedModel,
            String unformedTexture,
            String dynamicBaseTexture,
            Optional<String> dynamicLightTexture
    ) {

        public CraftingDefinition {
            if (!blockId.startsWith("expandedae:exp_crafting_")
                    || !(CraftingBlockKind.UNIT.equals(kind)
                    || CraftingBlockKind.ACCELERATOR.equals(kind))
                    || !unformedModel.startsWith("expandedae:block/crafting/")
                    || !formedModel.endsWith("_formed")
                    || !unformedTexture.startsWith("expandedae:block/crafting/")
                    || !dynamicBaseTexture.startsWith("expandedae:block/crafting/")
                    || dynamicLightTexture == null
                    || dynamicLightTexture.stream().anyMatch(
                            texture -> !texture.startsWith("expandedae:block/crafting/")
                    )) {
                throw new IllegalArgumentException("invalid Expanded AE crafting definition");
            }
            if ((CraftingBlockKind.UNIT.equals(kind) && dynamicLightTexture.isPresent())
                    || (CraftingBlockKind.ACCELERATOR.equals(kind)
                    && dynamicLightTexture.isEmpty())) {
                throw new IllegalArgumentException("invalid Expanded AE light contract");
            }
        }
    }

    /** Exact neutral model stack for one generic AE2 face part. */
    public record PartDefinition(
            String partId,
            List<String> neutralModelStack,
            boolean supportsSpin,
            String statusPolicy
    ) {

        public PartDefinition {
            if (!partId.startsWith("expandedae:")
                    || neutralModelStack.isEmpty()
                    || statusPolicy.isBlank()) {
                throw new IllegalArgumentException("invalid Expanded AE part definition");
            }
            neutralModelStack = List.copyOf(neutralModelStack);
        }
    }
}
