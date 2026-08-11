/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.profile.extendedae;

import io.github.janguenter.bluemap.ae2.profile.extendedae.ExtendedAe2235Catalog.MatrixKind;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExtendedAe2235ProfileTest {

    @Test
    void pinsExactArtifactAndSourceTag() {
        assertEquals("1.21-2.2.35-neoforge", ExtendedAe2235Profile.VERSION);
        assertEquals(5_578_031L, ExtendedAe2235Profile.JAR_BYTES);
        assertEquals("1.21-2.2.35-neoforge", ExtendedAe2235Profile.SOURCE_TAG);
        assertEquals("3776bc854458301bbcc9a44a8238d70a0e3dc00d",
                ExtendedAe2235Profile.SOURCE_COMMIT);
        assertTrue(ExtendedAe2235Profile.acceptsArtifact(
                ExtendedAe2235Profile.JAR_BYTES,
                ExtendedAe2235Profile.JAR_SHA256
        ));
        assertFalse(ExtendedAe2235Profile.acceptsArtifact(
                ExtendedAe2235Profile.JAR_BYTES,
                "0".repeat(64)
        ));
        assertThrows(IllegalArgumentException.class, () ->
                ExtendedAe2235Profile.requireExactArtifact(0, null));
    }

    @Test
    void closesSixMatrixBlocksAndTwoNoSpinPlanes() {
        assertEquals(6, ExtendedAe2235Catalog.matrixDefinitions().size());
        assertEquals(Set.of(
                "extendedae:assembler_matrix_frame",
                "extendedae:assembler_matrix_wall",
                "extendedae:assembler_matrix_glass",
                "extendedae:assembler_matrix_pattern",
                "extendedae:assembler_matrix_crafter",
                "extendedae:assembler_matrix_speed"
        ), ExtendedAe2235Catalog.matrixBlockIds());
        assertTrue(ExtendedAe2235Catalog.matrixDefinitions().values().stream()
                .allMatch(definition -> definition.blockId().equals(definition.blockEntityId())));
        assertEquals(9, ExtendedAe2235Catalog.matrixDefinitions().values().stream()
                .mapToInt(definition -> definition.modelResources().size())
                .sum());
        assertEquals(Set.of(
                ExtendedAe2235Catalog.FORMED_PROPERTY,
                ExtendedAe2235Catalog.POWERED_PROPERTY,
                ExtendedAe2235Catalog.SHAPE_PROPERTY
        ), ExtendedAe2235Catalog.requireMatrixDefinition(
                "extendedae:assembler_matrix_frame"
        ).stateProperties());
        assertEquals(Map.of(
                ExtendedAe2235Catalog.FORMED_PROPERTY, java.util.List.of("false", "true"),
                ExtendedAe2235Catalog.POWERED_PROPERTY, java.util.List.of("false", "true"),
                ExtendedAe2235Catalog.SHAPE_PROPERTY,
                java.util.List.of("block", "column_x", "column_y", "column_z")
        ), ExtendedAe2235Catalog.requireMatrixDefinition(
                "extendedae:assembler_matrix_frame"
        ).persistedStateDomains());
        assertEquals(Map.of(
                ExtendedAe2235Catalog.FORMED_PROPERTY, java.util.List.of("false", "true"),
                ExtendedAe2235Catalog.POWERED_PROPERTY, java.util.List.of("false", "true")
        ), ExtendedAe2235Catalog.requireMatrixDefinition(
                "extendedae:assembler_matrix_glass"
        ).persistedStateDomains());
        assertTrue(ExtendedAe2235Catalog.requireMatrixDefinition(
                "extendedae:assembler_matrix_glass"
        ).connectedGeometry());
        assertEquals(2, ExtendedAe2235Catalog.planeDefinitions().size());
        assertTrue(ExtendedAe2235Catalog.planeDefinitions().values().stream()
                .noneMatch(ExtendedAe2235Catalog.PlaneDefinition::supportsSpin));
        assertTrue(ExtendedAe2235Catalog.planeDefinitions().values().stream()
                .allMatch(definition -> definition.cableConnectionLength() == 1));
        assertTrue(ExtendedAe2235Catalog.planeDefinitions().values().stream()
                .allMatch(definition -> "ae2:part/plane_sides".equals(
                        definition.sideTexture()
                )));
    }

    @Test
    void foldsDisjointDriveAndM5ResourcePartitions() {
        assertEquals(38, ExtendedAe2235Profile.m5RequiredResources().size());
        assertEquals(7_172L, ExtendedAe2235Profile.m5RequiredResourceSizes().values().stream()
                .mapToLong(Long::longValue)
                .sum());
        assertEquals(15, ExtendedAe2235Profile.driveRequiredResources().size());
        assertEquals(13_242L,
                ExtendedAe2235Profile.driveRequiredResourceSizes().values().stream()
                        .mapToLong(Long::longValue)
                        .sum());
        assertTrue(ExtendedAe2235Profile.m5RequiredResources().keySet().stream()
                .noneMatch(ExtendedAe2235Profile.driveRequiredResources()::containsKey));
        assertEquals(53, ExtendedAe2235Profile.allRequiredResources().size());
        assertEquals(20_414L,
                ExtendedAe2235Profile.allRequiredResourceSizes().values().stream()
                        .mapToLong(Long::longValue)
                        .sum());
        assertEquals(ExtendedAe2235Profile.allRequiredResources().keySet(),
                ExtendedAe2235Profile.allRequiredResourceSizes().keySet());
        assertEquals(
                "9b7a212beddd3ca7e9921d7d4563dfbd452255cd6b0dad8ac0464fd8cffc4c65",
                ExtendedAe2235Profile.ALL_RESOURCE_CLOSURE_SHA256
        );
    }

    @Test
    void catalogFailsClosedForUnknownContent() {
        assertThrows(IllegalArgumentException.class, () ->
                ExtendedAe2235Catalog.requireMatrixDefinition("extendedae:not_matrix"));
        assertThrows(IllegalArgumentException.class, () ->
                ExtendedAe2235Catalog.requirePlaneDefinition("extendedae:not_plane"));
        assertEquals(MatrixKind.FRAME, ExtendedAe2235Catalog.requireMatrixDefinition(
                "extendedae:assembler_matrix_frame"
        ).kind());
    }

    @Test
    void persistedMatrixStateGateUsesExactRoleSpecificKeysAndDomains() {
        var frame = ExtendedAe2235Catalog.requireMatrixDefinition(
                "extendedae:assembler_matrix_frame"
        );
        var wall = ExtendedAe2235Catalog.requireMatrixDefinition(
                "extendedae:assembler_matrix_wall"
        );
        assertTrue(frame.acceptsPersistedState(Map.of(
                ExtendedAe2235Catalog.FORMED_PROPERTY, "true",
                ExtendedAe2235Catalog.POWERED_PROPERTY, "false",
                ExtendedAe2235Catalog.SHAPE_PROPERTY, "column_z"
        )));
        assertFalse(frame.acceptsPersistedState(Map.of(
                ExtendedAe2235Catalog.FORMED_PROPERTY, "true",
                ExtendedAe2235Catalog.POWERED_PROPERTY, "false"
        )));
        assertFalse(frame.acceptsPersistedState(Map.of(
                ExtendedAe2235Catalog.FORMED_PROPERTY, "true",
                ExtendedAe2235Catalog.POWERED_PROPERTY, "false",
                ExtendedAe2235Catalog.SHAPE_PROPERTY, "diagonal"
        )));
        assertTrue(wall.acceptsPersistedState(Map.of(
                ExtendedAe2235Catalog.FORMED_PROPERTY, "false",
                ExtendedAe2235Catalog.POWERED_PROPERTY, "true"
        )));
        assertFalse(wall.acceptsPersistedState(Map.of(
                ExtendedAe2235Catalog.FORMED_PROPERTY, "false",
                ExtendedAe2235Catalog.POWERED_PROPERTY, "true",
                ExtendedAe2235Catalog.SHAPE_PROPERTY, "block"
        )));
        assertTrue(ExtendedAe2235Catalog.matrixDefinitions().values().stream()
                .filter(definition -> definition.kind() != MatrixKind.FRAME)
                .allMatch(definition -> definition.persistedStateDomains().equals(
                        wall.persistedStateDomains()
                )));
    }
}
