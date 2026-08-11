/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.model.advancedae;

import io.github.janguenter.bluemap.ae2.model.Direction6;
import io.github.janguenter.bluemap.ae2.model.advancedae.AdvancedQuantumGeometry.Corner;
import io.github.janguenter.bluemap.ae2.model.advancedae.AdvancedQuantumSnapshot.Offset;
import io.github.janguenter.bluemap.ae2.model.advancedae.AdvancedQuantumSnapshot.VisualMode;
import io.github.janguenter.bluemap.ae2.profile.advancedae.AdvancedAe1612Catalog.QuantumKind;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.LinkedHashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AdvancedQuantumGeometryTest {

    @Test
    void preservesRoleFormationAndCoreStandaloneModes() {
        assertEquals(VisualMode.JSON_UNFORMED,
                snapshot(QuantumKind.UNIT, false, false, false, 0, Map.of()).visualMode());
        assertEquals(VisualMode.JSON_CORE_STANDALONE,
                snapshot(QuantumKind.CORE, true, false, false, 0, Map.of()).visualMode());
        assertEquals(VisualMode.CONNECTED_INTERNAL,
                snapshot(QuantumKind.CORE, true, false, true, 0, Map.of()).visualMode());
        assertEquals(VisualMode.CONNECTED_STRUCTURE,
                snapshot(QuantumKind.STRUCTURE, false, false, false, 0, Map.of()).visualMode());
        assertEquals(
                "assets/advanced_ae/models/block/crafting/quantum_unit.json",
                snapshot(QuantumKind.UNIT, false, false, false, 0, Map.of())
                        .staticJsonModelResource().orElseThrow()
        );
        assertEquals(
                "assets/advanced_ae/models/block/quantum_core.json",
                snapshot(QuantumKind.CORE, true, false, false, 0, Map.of())
                        .staticJsonModelResource().orElseThrow()
        );
        assertTrue(snapshot(QuantumKind.CORE, true, false, true, 0, Map.of())
                .staticJsonModelResource().isEmpty());
    }

    @Test
    void enforcesRoleSpecificAppearanceConnections() {
        Offset east = Offset.direct(Direction6.EAST);
        Offset west = Offset.direct(Direction6.WEST);
        AdvancedQuantumSnapshot internal = snapshot(
                QuantumKind.UNIT,
                true,
                false,
                false,
                0,
                Map.of(east, QuantumKind.STRUCTURE, west, QuantumKind.CORE)
        );
        assertFalse(internal.hasCompatibleNeighbor(east));
        assertTrue(internal.hasCompatibleNeighbor(west));

        AdvancedQuantumSnapshot structure = snapshot(
                QuantumKind.STRUCTURE,
                true,
                false,
                false,
                0,
                Map.of(east, QuantumKind.STRUCTURE, west, QuantumKind.UNIT)
        );
        assertTrue(structure.hasCompatibleNeighbor(east));
        assertFalse(structure.hasCompatibleNeighbor(west));
    }

    @Test
    void portsExactConnectedQuadCountsAndDirectFaceCulling() {
        AdvancedQuantumSnapshot internal = snapshot(
                QuantumKind.UNIT, true, false, false, 0, Map.of()
        );
        AdvancedQuantumSnapshot structure = snapshot(
                QuantumKind.STRUCTURE, true, false, false, 0, Map.of()
        );
        assertEquals(30, AdvancedQuantumGeometry.forSnapshot(internal).size());
        assertEquals(54, AdvancedQuantumGeometry.forSnapshot(structure).size());
        assertTrue(AdvancedQuantumGeometry.forSnapshot(snapshot(
                QuantumKind.UNIT, false, false, false, 0, Map.of()
        )).isEmpty());

        AdvancedQuantumSnapshot eastConnected = snapshot(
                QuantumKind.UNIT,
                true,
                false,
                false,
                0,
                Map.of(Offset.direct(Direction6.EAST), QuantumKind.CORE)
        );
        assertTrue(AdvancedQuantumGeometry.forSnapshot(eastConnected).stream()
                .noneMatch(quad -> quad.face() == Direction6.EAST));
    }

    @Test
    void portsAllCornerIndicesAndVerticalFaceCoordinates() {
        assertEquals(0, AdvancedQuantumGeometry.sideTile(false, false, false));
        assertEquals(0, AdvancedQuantumGeometry.sideTile(false, false, true));
        assertEquals(1, AdvancedQuantumGeometry.sideTile(true, true, false));
        assertEquals(-1, AdvancedQuantumGeometry.sideTile(true, true, true));
        assertEquals(2, AdvancedQuantumGeometry.sideTile(false, true, false));
        assertEquals(2, AdvancedQuantumGeometry.sideTile(false, true, true));
        assertEquals(3, AdvancedQuantumGeometry.sideTile(true, false, false));
        assertEquals(3, AdvancedQuantumGeometry.sideTile(true, false, true));

        AdvancedQuantumGeometry.Check up = AdvancedQuantumGeometry.check(
                Direction6.UP,
                Corner.LEFT_UP
        );
        assertEquals(new Offset(0, 0, 1), up.first());
        assertEquals(new Offset(-1, 0, 0), up.second());
        assertEquals(new Offset(-1, 0, 1), up.diagonal());
        assertThrows(IllegalArgumentException.class, () ->
                AdvancedQuantumGeometry.check(Direction6.UP, Corner.FULL));
    }

    @Test
    void modelsCompleteScanAndForcesEveryLiveVisualOff() {
        assertEquals(27, AdvancedQuantumSnapshot.scanOffsets().size());
        assertTrue(AdvancedQuantumSnapshot.scanOffsets().contains(new Offset(0, 0, 0)));
        AdvancedQuantumSnapshot snapshot = snapshot(
                QuantumKind.UNIT, true, true, false, 12, Map.of()
        );
        assertFalse(snapshot.staticVisualState().powered());
        assertEquals(0, snapshot.staticVisualState().lightLevel());
        assertFalse(snapshot.staticVisualState().emissive());
        assertEquals(0, snapshot.staticVisualState().animationFrame());
        assertThrows(IllegalArgumentException.class, () -> new Offset(2, 0, 0));
    }

    @Test
    void exposesExactStaticMaterialsAndFailsClosedRawObservations() {
        var internalFace = AdvancedQuantumGeometry.material(
                QuantumKind.CORE,
                AdvancedQuantumGeometry.Surface.INTERNAL_FACE
        );
        assertEquals("advanced_ae:block/crafting/quantum_internal_formed_face",
                internalFace.texture());
        assertEquals(AdvancedQuantumGeometry.RenderLayer.CUTOUT,
                internalFace.renderLayer());
        assertFalse(internalFace.emissive());
        assertFalse(internalFace.ambientOcclusion());
        assertFalse(internalFace.usesBlockLight());
        assertEquals(0, internalFace.animationFrame());
        assertEquals(AdvancedQuantumGeometry.RenderLayer.TRANSLUCENT,
                AdvancedQuantumGeometry.material(
                        QuantumKind.STRUCTURE,
                        AdvancedQuantumGeometry.Surface.STRUCTURE_FACE
                ).renderLayer());
        assertEquals("advanced_ae:block/crafting/quantum_structure_formed_sides",
                AdvancedQuantumGeometry.material(
                        QuantumKind.STRUCTURE,
                        AdvancedQuantumGeometry.Surface.STRUCTURE_SIDE_INNER
                ).texture());
        assertThrows(IllegalArgumentException.class, () -> AdvancedQuantumGeometry.material(
                QuantumKind.STRUCTURE,
                AdvancedQuantumGeometry.Surface.INTERNAL_SIDE
        ));

        Map<Offset, String> observations = new LinkedHashMap<>();
        observations.put(Offset.direct(Direction6.EAST), "advanced_ae:quantum_core");
        observations.put(Offset.direct(Direction6.WEST), "minecraft:stone");
        observations.put(Offset.direct(Direction6.NORTH), null);
        AdvancedQuantumSnapshot snapshot = AdvancedQuantumSnapshot.observed(
                QuantumKind.UNIT,
                true,
                false,
                false,
                0,
                0,
                0,
                0,
                observations
        );
        assertTrue(snapshot.hasCompatibleNeighbor(Offset.direct(Direction6.EAST)));
        assertFalse(snapshot.hasCompatibleNeighbor(Offset.direct(Direction6.WEST)));
        assertFalse(snapshot.hasCompatibleNeighbor(Offset.direct(Direction6.NORTH)));
    }

    private static AdvancedQuantumSnapshot snapshot(
            QuantumKind kind,
            boolean formed,
            boolean powered,
            boolean multiblocked,
            int lightLevel,
            Map<Offset, QuantumKind> neighbors
    ) {
        return new AdvancedQuantumSnapshot(
                kind,
                formed,
                powered,
                multiblocked,
                lightLevel,
                0,
                0,
                0,
                neighbors
        );
    }
}
