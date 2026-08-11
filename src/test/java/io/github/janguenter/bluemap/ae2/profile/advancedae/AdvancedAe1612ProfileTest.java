/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.profile.advancedae;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AdvancedAe1612ProfileTest {

    @Test
    void pinsExactArtifactAndRejectsNearMisses() {
        assertEquals("1.6.12-1.21.1", AdvancedAe1612Profile.VERSION);
        assertEquals(4_791_255L, AdvancedAe1612Profile.JAR_BYTES);
        assertTrue(AdvancedAe1612Profile.acceptsArtifact(
                AdvancedAe1612Profile.JAR_BYTES,
                AdvancedAe1612Profile.JAR_SHA256
        ));
        assertFalse(AdvancedAe1612Profile.acceptsArtifact(
                AdvancedAe1612Profile.JAR_BYTES - 1,
                AdvancedAe1612Profile.JAR_SHA256
        ));
        assertFalse(AdvancedAe1612Profile.acceptsArtifact(
                AdvancedAe1612Profile.JAR_BYTES,
                "0".repeat(64)
        ));
        assertThrows(IllegalArgumentException.class, () ->
                AdvancedAe1612Profile.requireExactArtifact(0, null));
    }

    @Test
    void closesEightQuantumBlocksAndExactResources() {
        assertEquals(8, AdvancedAe1612Catalog.quantumDefinitions().size());
        assertEquals(7, AdvancedAe1612Catalog.internalBlockIds().size());
        assertEquals(Set.of("advanced_ae:quantum_structure"),
                AdvancedAe1612Catalog.structureBlockIds());
        assertTrue(AdvancedAe1612Catalog.quantumDefinitions().values().stream()
                .allMatch(definition -> AdvancedAe1612Catalog.QUANTUM_BLOCK_ENTITY.equals(
                        definition.blockEntityId()
                )));
        assertEquals(16, AdvancedAe1612Catalog.quantumDefinitions().values().stream()
                .mapToInt(definition -> definition.modelResources().size())
                .sum());
        assertEquals(24, AdvancedAe1612Catalog.quantumTextureResources().size());

        Map<String, java.util.List<String>> domains = Map.of(
                AdvancedAe1612Catalog.FORMED_PROPERTY, java.util.List.of("false", "true"),
                AdvancedAe1612Catalog.POWERED_PROPERTY, java.util.List.of("false", "true"),
                AdvancedAe1612Catalog.MULTIBLOCKED_PROPERTY,
                java.util.List.of("false", "true"),
                AdvancedAe1612Catalog.LIGHT_LEVEL_PROPERTY,
                java.util.List.of(
                        "0", "1", "2", "3", "4", "5", "6", "7",
                        "8", "9", "10", "11", "12", "13", "14", "15"
                )
        );
        assertEquals(domains, AdvancedAe1612Catalog.quantumPersistedStateDomains());
        assertTrue(AdvancedAe1612Catalog.quantumDefinitions().values().stream()
                .allMatch(definition -> domains.equals(definition.persistedStateDomains())));

        assertEquals(48, AdvancedAe1612Profile.requiredResources().size());
        assertEquals(48, AdvancedAe1612Profile.requiredResourceSizes().size());
        assertEquals(24_053L, AdvancedAe1612Profile.requiredResourceSizes().values().stream()
                .mapToLong(Long::longValue)
                .sum());
        assertEquals(AdvancedAe1612Profile.requiredResources().keySet(),
                AdvancedAe1612Profile.requiredResourceSizes().keySet());
        assertTrue(AdvancedAe1612Catalog.STATIC_POLICY.contains("non-emissive"));
        assertEquals(AdvancedAe1612Catalog.QuantumKind.CORE,
                AdvancedAe1612Catalog.quantumKindOrNull("advanced_ae:quantum_core"));
        assertEquals(null, AdvancedAe1612Catalog.quantumKindOrNull("minecraft:stone"));
    }

    @Test
    void catalogFailsClosedForUnknownBlocks() {
        assertThrows(IllegalArgumentException.class, () ->
                AdvancedAe1612Catalog.requireQuantumDefinition("advanced_ae:not_quantum"));
    }

    @Test
    void persistedQuantumStateGateRequiresExactKeysAndDomainsForEveryRole() {
        Map<String, String> exact = Map.of(
                AdvancedAe1612Catalog.FORMED_PROPERTY, "true",
                AdvancedAe1612Catalog.POWERED_PROPERTY, "false",
                AdvancedAe1612Catalog.MULTIBLOCKED_PROPERTY, "true",
                AdvancedAe1612Catalog.LIGHT_LEVEL_PROPERTY, "15"
        );
        assertTrue(AdvancedAe1612Catalog.quantumDefinitions().values().stream()
                .allMatch(definition -> definition.acceptsPersistedState(exact)));

        Map<String, String> missing = Map.of(
                AdvancedAe1612Catalog.FORMED_PROPERTY, "true",
                AdvancedAe1612Catalog.POWERED_PROPERTY, "false",
                AdvancedAe1612Catalog.MULTIBLOCKED_PROPERTY, "true"
        );
        assertTrue(AdvancedAe1612Catalog.quantumDefinitions().values().stream()
                .noneMatch(definition -> definition.acceptsPersistedState(missing)));

        Map<String, String> badLight = new java.util.LinkedHashMap<>(exact);
        badLight.put(AdvancedAe1612Catalog.LIGHT_LEVEL_PROPERTY, "16");
        assertTrue(AdvancedAe1612Catalog.quantumDefinitions().values().stream()
                .noneMatch(definition -> definition.acceptsPersistedState(badLight)));

        Map<String, String> extra = new java.util.LinkedHashMap<>(exact);
        extra.put("unexpected", "false");
        assertTrue(AdvancedAe1612Catalog.quantumDefinitions().values().stream()
                .noneMatch(definition -> definition.acceptsPersistedState(extra)));
    }
}
