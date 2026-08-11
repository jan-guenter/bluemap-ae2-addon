/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.model.advancedae;

import io.github.janguenter.bluemap.ae2.model.Direction6;
import io.github.janguenter.bluemap.ae2.model.advancedae.AdvancedAeAthenaGeometry.Corner;
import io.github.janguenter.bluemap.ae2.model.advancedae.AdvancedAeAthenaGeometry.Texture;
import io.github.janguenter.bluemap.ae2.model.advancedae.AdvancedAeAthenaSnapshot.Offset;
import io.github.janguenter.bluemap.ae2.profile.advancedae.AdvancedAe1612Catalog;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AdvancedAeAthenaGeometryTest {

    @Test
    void portsFiveTextureTruthTable() {
        assertEquals(Texture.PARTICLE, AdvancedAeAthenaGeometry.texture(false, false, false));
        assertEquals(Texture.PARTICLE, AdvancedAeAthenaGeometry.texture(false, false, true));
        assertEquals(Texture.VERTICAL, AdvancedAeAthenaGeometry.texture(true, false, false));
        assertEquals(Texture.VERTICAL, AdvancedAeAthenaGeometry.texture(true, false, true));
        assertEquals(Texture.HORIZONTAL, AdvancedAeAthenaGeometry.texture(false, true, false));
        assertEquals(Texture.HORIZONTAL, AdvancedAeAthenaGeometry.texture(false, true, true));
        assertEquals(Texture.CENTER, AdvancedAeAthenaGeometry.texture(true, true, false));
        assertEquals(Texture.EMPTY, AdvancedAeAthenaGeometry.texture(true, true, true));
        assertEquals("advanced_ae:block/quantum_alloy_block",
                Texture.PARTICLE.textureId());
        assertEquals("advanced_ae:block/quantum_alloy_block_empty",
                Texture.EMPTY.textureId());
        assertEquals("advanced_ae:block/quantum_alloy_block_center",
                Texture.CENTER.textureId());
        assertEquals("advanced_ae:block/quantum_alloy_block_v",
                Texture.VERTICAL.textureId());
        assertEquals("advanced_ae:block/quantum_alloy_block_h",
                Texture.HORIZONTAL.textureId());
    }

    @Test
    void preservesExactFaceBases() {
        assertEquals(new Offset(0, 0, -1),
                AdvancedAeAthenaGeometry.faceBasis(Direction6.UP).up());
        assertEquals(new Offset(-1, 0, 0),
                AdvancedAeAthenaGeometry.faceBasis(Direction6.UP).left());
        assertEquals(new Offset(0, 0, 1),
                AdvancedAeAthenaGeometry.faceBasis(Direction6.DOWN).up());
        assertEquals(new Offset(1, 0, 0),
                AdvancedAeAthenaGeometry.faceBasis(Direction6.NORTH).left());
        assertEquals(new Offset(-1, 0, 0),
                AdvancedAeAthenaGeometry.faceBasis(Direction6.SOUTH).left());
        assertEquals(new Offset(0, 0, -1),
                AdvancedAeAthenaGeometry.faceBasis(Direction6.WEST).left());
        assertEquals(new Offset(0, 0, 1),
                AdvancedAeAthenaGeometry.faceBasis(Direction6.EAST).left());
    }

    @Test
    void cullsMatchingDirectFacesAndCollapsesFullyConnectedFace() {
        AdvancedAeAthenaSnapshot isolated = snapshot(Set.of());
        assertEquals(24, AdvancedAeAthenaGeometry.forSnapshot(isolated).size());

        AdvancedAeAthenaSnapshot east = snapshot(Set.of(new Offset(1, 0, 0)));
        assertTrue(AdvancedAeAthenaGeometry.forSnapshot(east).stream()
                .noneMatch(quad -> quad.face() == Direction6.EAST));

        AdvancedAeAthenaGeometry.FaceBasis basis =
                AdvancedAeAthenaGeometry.faceBasis(Direction6.UP);
        Set<Offset> connected = new LinkedHashSet<>(Set.of(
                basis.up(), basis.down(), basis.left(), basis.right(),
                basis.up().plus(basis.left()), basis.up().plus(basis.right()),
                basis.down().plus(basis.left()), basis.down().plus(basis.right())
        ));
        var up = AdvancedAeAthenaGeometry.forSnapshot(snapshot(connected)).stream()
                .filter(quad -> quad.face() == Direction6.UP)
                .toList();
        assertEquals(1, up.size());
        assertEquals(Corner.FULL, up.getFirst().corner());
        assertEquals(Texture.EMPTY, up.getFirst().texture());
    }

    @Test
    void acceptsOnlyTheExactWholeBlockstateOwner() {
        assertThrows(IllegalArgumentException.class, () ->
                new AdvancedAeAthenaSnapshot("advanced_ae:not_alloy", Set.of()));
        assertThrows(IllegalArgumentException.class, () -> new Offset(0, 0, 0));

        Map<Offset, Boolean> observations = new LinkedHashMap<>();
        observations.put(new Offset(1, 0, 0), true);
        observations.put(new Offset(-1, 0, 0), false);
        observations.put(new Offset(0, 1, 0), null);
        AdvancedAeAthenaSnapshot snapshot =
                AdvancedAeAthenaSnapshot.observedWholeStateMatches(
                        AdvancedAe1612Catalog.QUANTUM_ALLOY_BLOCK,
                        observations
                );
        assertEquals(Set.of(new Offset(1, 0, 0)), snapshot.matchingNeighbors());
        assertEquals("neighbor-BlockState-equals-center-BlockState",
                AdvancedAeAthenaSnapshot.WHOLE_BLOCK_STATE_IDENTITY_POLICY);
    }

    private static AdvancedAeAthenaSnapshot snapshot(Set<Offset> neighbors) {
        return new AdvancedAeAthenaSnapshot(
                AdvancedAe1612Catalog.QUANTUM_ALLOY_BLOCK,
                neighbors
        );
    }
}
