/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.model;

import org.junit.jupiter.api.Test;

import java.util.EnumMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CableBusDecoderM2Test {

    private static final String GLASS_CABLE = "ae2:fluix_glass_cable";
    private static final String COVERED_CABLE = "ae2:fluix_covered_cable";
    private static final String DENSE_CABLE = "ae2:fluix_covered_dense_cable";

    private final CableBusDecoder decoder = new CableBusDecoder();

    @Test
    void acceptsTheExactTerminalAndAllFourPersistedByteSpins() {
        for (int spin = 0; spin < 4; spin++) {
            CableBusDecodeResult result = decoder.decode(
                    center(GLASS_CABLE),
                    Map.of(Direction6.NORTH, terminal(spin)),
                    Map.of()
            );

            assertTrue(result.isSupported(), "spin " + spin);
            assertEquals(
                    new FacePartSnapshot(FacePartSnapshot.TERMINAL, spin),
                    result.supportedSnapshot().orElseThrow()
                            .faceParts().get(Direction6.NORTH),
                    "spin " + spin
            );
        }
    }

    @Test
    void rejectsOtherPartIdsAndNonByteOrOutOfRangeSpins() {
        assertStatus(
                GLASS_CABLE,
                Map.of(Direction6.NORTH, part("ae2:monitor", (byte) 0)),
                Map.of(),
                CableBusDecodeResult.Status.UNSUPPORTED_FACE_PART
        );
        assertStatus(
                GLASS_CABLE,
                Map.of(Direction6.NORTH, part(FacePartSnapshot.TERMINAL, 0)),
                Map.of(),
                CableBusDecodeResult.Status.INVALID_FACE_PART_SPIN
        );
        assertStatus(
                GLASS_CABLE,
                Map.of(Direction6.NORTH, part(FacePartSnapshot.TERMINAL, (byte) -1)),
                Map.of(),
                CableBusDecodeResult.Status.INVALID_FACE_PART_SPIN
        );
        assertStatus(
                GLASS_CABLE,
                Map.of(Direction6.NORTH, part(FacePartSnapshot.TERMINAL, (byte) 4)),
                Map.of(),
                CableBusDecodeResult.Status.INVALID_FACE_PART_SPIN
        );
    }

    @Test
    void retainsMultipleFacePartsWithIndependentDirectionsAndSpins() {
        EnumMap<Direction6, Object> rawParts = new EnumMap<>(Direction6.class);
        rawParts.put(Direction6.DOWN, terminal(0));
        rawParts.put(Direction6.SOUTH, terminal(1));
        rawParts.put(Direction6.EAST, terminal(3));

        CableBusDecodeResult result = decoder.decode(
                center(COVERED_CABLE),
                rawParts,
                Map.of()
        );

        CableBusSnapshot snapshot = result.supportedSnapshot().orElseThrow();
        assertEquals(3, snapshot.faceParts().size());
        assertEquals(0, snapshot.faceParts().get(Direction6.DOWN).spin());
        assertEquals(1, snapshot.faceParts().get(Direction6.SOUTH).spin());
        assertEquals(3, snapshot.faceParts().get(Direction6.EAST).spin());

        rawParts.clear();
        assertEquals(3, snapshot.faceParts().size());
        assertThrows(UnsupportedOperationException.class, snapshot.faceParts()::clear);
    }

    @Test
    void rejectsEveryDenseCableFamilyWhenAnyFacePartIsPresent() {
        for (CableDefinition definition : Ae2CableCatalog.definitions()) {
            if (!definition.family().isDense()) {
                continue;
            }
            assertStatus(
                    definition.id(),
                    Map.of(Direction6.UP, terminal(2)),
                    Map.of(),
                    CableBusDecodeResult.Status.UNSUPPORTED_FACE_PART_TOPOLOGY
            );
        }
    }

    @Test
    void acceptsOnlyOnePlainStoneFacadeOnTheSoleTerminalFace() {
        CableBusDecodeResult result = decoder.decode(
                center(GLASS_CABLE),
                Map.of(Direction6.WEST, terminal(2)),
                Map.of(Direction6.WEST, stoneFacade())
        );

        CableBusSnapshot snapshot = result.supportedSnapshot().orElseThrow();
        assertEquals(
                new FacadeSnapshot(FacadeSnapshot.STONE, Map.of()),
                snapshot.facades().get(Direction6.WEST)
        );
        assertTrue(snapshot.hasFacePart(Direction6.WEST));
        assertTrue(snapshot.hasFacade(Direction6.WEST));
    }

    @Test
    void rejectsNonStoneAndEveryFacadePropertiesPayload() {
        assertStatus(
                GLASS_CABLE,
                Map.of(Direction6.NORTH, terminal(0)),
                Map.of(Direction6.NORTH, Map.of("Name", "minecraft:glass")),
                CableBusDecodeResult.Status.UNSUPPORTED_FACADE_STATE
        );
        assertStatus(
                GLASS_CABLE,
                Map.of(Direction6.NORTH, terminal(0)),
                Map.of(Direction6.NORTH, Map.of(
                        "Name", FacadeSnapshot.STONE,
                        "Properties", Map.of()
                )),
                CableBusDecodeResult.Status.UNSUPPORTED_FACADE_STATE
        );
        assertStatus(
                GLASS_CABLE,
                Map.of(Direction6.NORTH, terminal(0)),
                Map.of(Direction6.NORTH, Map.of(
                        "Name", FacadeSnapshot.STONE,
                        "Properties", Map.of("variant", "smooth")
                )),
                CableBusDecodeResult.Status.UNSUPPORTED_FACADE_STATE
        );
    }

    @Test
    void rejectsFacadeWithoutPartOnAnotherFaceOrWithMultipleParts() {
        assertStatus(
                GLASS_CABLE,
                Map.of(),
                Map.of(Direction6.NORTH, stoneFacade()),
                CableBusDecodeResult.Status.UNSUPPORTED_FACADE_LAYOUT
        );
        assertStatus(
                GLASS_CABLE,
                Map.of(Direction6.SOUTH, terminal(0)),
                Map.of(Direction6.NORTH, stoneFacade()),
                CableBusDecodeResult.Status.UNSUPPORTED_FACADE_LAYOUT
        );
        assertStatus(
                GLASS_CABLE,
                Map.of(
                        Direction6.NORTH, terminal(0),
                        Direction6.SOUTH, terminal(1)
                ),
                Map.of(Direction6.NORTH, stoneFacade()),
                CableBusDecodeResult.Status.UNSUPPORTED_FACADE_LAYOUT
        );
    }

    @Test
    void rejectsMultipleFacadesEvenWhenEachHasAMatchingPart() {
        assertStatus(
                GLASS_CABLE,
                Map.of(
                        Direction6.NORTH, terminal(0),
                        Direction6.SOUTH, terminal(1)
                ),
                Map.of(
                        Direction6.NORTH, stoneFacade(),
                        Direction6.SOUTH, stoneFacade()
                ),
                CableBusDecodeResult.Status.UNSUPPORTED_FACADE_LAYOUT
        );
    }

    private void assertStatus(
            String cableId,
            Map<Direction6, Object> faceParts,
            Map<Direction6, Object> facades,
            CableBusDecodeResult.Status expected
    ) {
        CableBusDecodeResult result = decoder.decode(center(cableId), faceParts, facades);
        assertFalse(result.isSupported());
        assertTrue(result.supportedSnapshot().isEmpty());
        assertEquals(expected, result.status());
    }

    private static Map<String, Object> center(String id) {
        return Map.of("id", id);
    }

    private static Map<String, Object> terminal(int spin) {
        return part(FacePartSnapshot.TERMINAL, (byte) spin);
    }

    private static Map<String, Object> part(String id, Object spin) {
        return Map.of("id", id, "spin", spin);
    }

    private static Map<String, Object> stoneFacade() {
        return Map.of("Name", FacadeSnapshot.STONE);
    }
}
