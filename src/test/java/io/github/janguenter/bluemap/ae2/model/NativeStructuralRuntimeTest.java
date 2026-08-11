/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.model;

import io.github.janguenter.bluemap.ae2.profile.Ae219217NativeStructuralProfile;
import io.github.janguenter.bluemap.ae2.profile.extendedae.ExtendedAe2235Catalog;
import org.junit.jupiter.api.Test;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NativeStructuralRuntimeTest {

    private final NativeStructuralCableBusDecoder decoder =
            new NativeStructuralCableBusDecoder();

    @Test
    void closedCatalogsMatchTheExactProfile() {
        assertEquals(29, NativeStructuralPartCatalog.definitions().size());
        assertEquals(
                Ae219217NativeStructuralProfile.nativeFacePartIds(),
                NativeStructuralPartCatalog.definitions().stream()
                        .map(NativeStructuralPartCatalog.Definition::id)
                        .toList()
        );
        assertEquals(30, NativeEndpointCatalog.definitions().size());
        assertEquals(
                Ae219217NativeStructuralProfile.nativeEndpointIds(),
                NativeEndpointCatalog.definitions().stream()
                        .map(NativeEndpointCatalog.Definition::blockId)
                        .toList()
        );
        assertEquals(
                Map.of(
                        CableFamily.SMART, 18L,
                        CableFamily.COVERED, 9L,
                        CableFamily.DENSE_SMART, 3L
                ),
                NativeEndpointCatalog.definitions().stream().collect(
                        java.util.stream.Collectors.groupingBy(
                                NativeEndpointCatalog.Definition::cableType,
                                java.util.stream.Collectors.counting()
                        )
                )
        );
    }

    @Test
    void extendedPlaneCatalogAndDecoderRequireTheExactActiveRouteAndState() {
        assertEquals(10, NativeStructuralPartCatalog.extensionIds().size());
        NativeStructuralCableBusDecoder active = new NativeStructuralCableBusDecoder(
                "extendedae-planes"::equals
        );

        for (ExtendedAe2235Catalog.PlaneDefinition plane
                : ExtendedAe2235Catalog.planeDefinitions().values()) {
            NativeStructuralPartCatalog.Definition definition =
                    NativeStructuralPartCatalog.require(plane.partId());
            assertTrue(definition.isExtension());
            assertEquals("extendedae-planes", definition.extensionRouteId());
            assertEquals(NativeStructuralPartCatalog.Kind.PLANE, definition.kind());
            assertFalse(definition.persistedSpin());
            assertEquals(1, definition.cableConnectionLength());
            assertEquals(1D, definition.facadeCutoutMin16());
            assertEquals(15D, definition.facadeCutoutMax16());
            assertEquals(
                    List.of(
                            "ae2:part/transition_plane_off",
                            plane.staticFrontTexture()
                    ),
                    definition.modelPaths()
            );

            Map<Direction6, Object> exact = Map.of(
                    Direction6.NORTH,
                    Map.of("id", plane.partId())
            );
            assertEquals(
                    NativeStructuralDecodeResult.Status.UNSUPPORTED_FACE_PART,
                    decoder.decode(null, exact, Map.of()).status()
            );
            NativeStructuralDecodeResult accepted = active.decode(
                    null,
                    exact,
                    Map.of()
            );
            assertTrue(accepted.isSupported());
            assertEquals(
                    plane.partId(),
                    accepted.snapshot().faceParts().get(Direction6.NORTH).id()
            );

            for (String forbidden : List.of("spin", "freq", "cell")) {
                Object value = switch (forbidden) {
                    case "spin" -> (byte) 0;
                    case "freq" -> (short) 0;
                    case "cell" -> Map.of();
                    default -> throw new IllegalStateException("unexpected test field");
                };
                assertEquals(
                        NativeStructuralDecodeResult.Status.MALFORMED_FACE_PART,
                        active.decode(
                                null,
                                Map.of(Direction6.NORTH, Map.of(
                                        "id", plane.partId(),
                                        forbidden, value
                                )),
                                Map.of()
                        ).status(),
                        plane.partId() + "/" + forbidden
                );
            }
        }
    }

    @Test
    void exhaustsAll336NativeInstalledFaceAndSpinStates() {
        int states = 0;
        for (NativeStructuralPartCatalog.Definition definition
                : NativeStructuralPartCatalog.definitions()) {
            int spins = definition.persistedSpin() ? 4 : 1;
            for (Direction6 direction : Direction6.values()) {
                for (int spin = 0; spin < spins; spin++) {
                    NativeStructuralDecodeResult result = decoder.decode(
                            null,
                            Map.of(direction, rawPart(definition, spin, (short) 0)),
                            Map.of()
                    );
                    assertTrue(result.isSupported(), definition.id());
                    FacePartSnapshot decoded = result.supportedSnapshot()
                            .orElseThrow().faceParts().get(direction);
                    assertEquals(spin, decoded.spin());
                    assertEquals(definition.id(), decoded.id());
                    states++;
                }
            }
        }
        assertEquals(336, states);
    }

    @Test
    void exhausts986DenseCableAndNativePartPairs() {
        List<CableDefinition> dense = Ae2CableCatalog.definitions().stream()
                .filter(definition -> definition.family().isDense())
                .toList();
        assertEquals(34, dense.size());
        int pairs = 0;
        int supported = 0;
        for (CableDefinition cable : dense) {
            for (NativeStructuralPartCatalog.Definition part
                    : NativeStructuralPartCatalog.definitions()) {
                NativeStructuralDecodeResult result = decoder.decode(
                        Map.of("id", cable.id()),
                        Map.of(Direction6.NORTH, rawPart(part, 0, (short) 0)),
                        Map.of()
                );
                boolean expected = NativeStructuralPartCatalog.CABLE_ANCHOR.equals(
                        part.id()
                );
                assertEquals(expected, result.isSupported(), cable.id() + "/" + part.id());
                if (expected) {
                    supported++;
                } else {
                    assertEquals(
                            NativeStructuralDecodeResult.Status
                                    .UNSUPPORTED_FACE_PART_TOPOLOGY,
                            result.status()
                    );
                }
                pairs++;
            }
        }
        assertEquals(986, pairs);
        assertEquals(34, supported);
    }

    @Test
    void exhaustsAll64FacadeMasksAndRejectsFacadeOnlyWithoutCenter() {
        for (int mask = 0; mask < 64; mask++) {
            EnumMap<Direction6, Object> facades = new EnumMap<>(Direction6.class);
            for (Direction6 direction : Direction6.values()) {
                if ((mask & direction.maskBit()) != 0) {
                    facades.put(direction, Map.of("Name", "minecraft:stone"));
                }
            }
            NativeStructuralDecodeResult result = decoder.decode(
                    Map.of("id", Ae2CableCatalog.FLUIX_GLASS_CABLE),
                    Map.of(),
                    facades
            );
            assertTrue(result.isSupported(), "mask=" + mask);
            assertEquals(Integer.bitCount(mask), result.snapshot().facades().size());
        }

        NativeStructuralDecodeResult partOnly = decoder.decode(
                null,
                Map.of(Direction6.UP, Map.of("id", "ae2:cable_anchor")),
                Map.of()
        );
        assertTrue(partOnly.isSupported());
        assertFalse(partOnly.snapshot().hasCenter());
        assertEquals(
                NativeStructuralDecodeResult.Status.UNSUPPORTED_FACADE_LAYOUT,
                decoder.decode(
                        null,
                        Map.of(),
                        Map.of(Direction6.UP, Map.of("Name", "minecraft:stone"))
                ).status()
        );
    }

    @Test
    void exhausts384NativeAndExtendedPlaneStatesAnd108P2pFrequencyStates() {
        int planes = 0;
        List<String> planeIds = new java.util.ArrayList<>(List.of(
                NativeStructuralPartCatalog.ANNIHILATION_PLANE,
                NativeStructuralPartCatalog.FORMATION_PLANE
        ));
        planeIds.addAll(ExtendedAe2235Catalog.planePartIds());
        for (String id : planeIds) {
            String expectedFront = NativePartGeometry.planeFrontTexture(id);
            for (Direction6 face : Direction6.values()) {
                for (int mask = 0; mask < 16; mask++) {
                    List<NativePartGeometry.Quad> quads =
                            NativePartGeometry.plane(id, face, mask);
                    assertEquals(6, quads.size());
                    assertEquals(
                            1,
                            quads.stream()
                                    .filter(quad -> expectedFront.equals(quad.texture()))
                                    .count(),
                            id + "/" + face + "/" + mask
                    );
                    assertTrue(quads.stream().noneMatch(
                            NativePartGeometry.Quad::ambientOcclusion
                    ));
                    assertTrue(quads.stream().noneMatch(NativePartGeometry.Quad::emissive));
                    assertTrue(quads.stream().allMatch(quad -> quad.vertices().size() == 4));
                    planes++;
                }
            }
        }
        assertEquals(384, planes);

        int p2pStates = 0;
        List<String> p2pIds = NativeStructuralPartCatalog.definitions().stream()
                .filter(definition -> definition.kind()
                        == NativeStructuralPartCatalog.Kind.P2P)
                .map(NativeStructuralPartCatalog.Definition::id)
                .toList();
        assertEquals(6, p2pIds.size());
        for (String id : p2pIds) {
            for (Direction6 face : Direction6.values()) {
                for (short persisted : new short[]{0, (short) 0x1234, (short) 0xffff}) {
                    NativeStructuralDecodeResult decoded = decoder.decode(
                            null,
                            Map.of(face, Map.of("id", id, "freq", persisted)),
                            Map.of()
                    );
                    assertTrue(decoded.isSupported());
                    int unsigned = Short.toUnsignedInt(persisted);
                    assertEquals(
                            unsigned,
                            decoded.snapshot().faceParts().get(face).p2pFrequency()
                    );
                    List<NativePartGeometry.Quad> quads =
                            NativePartGeometry.p2p(face, unsigned);
                    assertEquals(96, quads.size());
                    assertTrue(quads.stream().noneMatch(
                            NativePartGeometry.Quad::ambientOcclusion
                    ));
                    assertTrue(quads.stream().noneMatch(NativePartGeometry.Quad::emissive));
                    p2pStates++;
                }
            }
        }
        assertEquals(108, p2pStates);
    }

    @Test
    void endpointConnectionsUseMinimumFamilyAndExactCollarsOnEverySide() {
        int states = 0;
        CableDefinition local = Ae2CableCatalog.definitions().stream()
                .filter(definition -> definition.family() == CableFamily.DENSE_SMART)
                .findFirst()
                .orElseThrow();
        for (NativeEndpointCatalog.Definition endpoint
                : NativeEndpointCatalog.definitions()) {
            CableFamily effective = CableFamily.minimum(
                    local.family(),
                    endpoint.cableType()
            );
            for (Direction6 direction : Direction6.values()) {
                NativeStructuralSnapshot snapshot = NativeStructuralSnapshot.decoded(
                        local,
                        Map.of(),
                        Map.of()
                ).withConnection(
                        direction,
                        NativeStructuralSnapshot.Connection.endpoint(
                                endpoint.blockId(),
                                endpoint.cableType(),
                                effective
                        )
                );
                List<CableGeometry.Quad> geometry =
                        CableGeometry.forNativeSnapshot(snapshot);
                assertFalse(geometry.isEmpty());
                assertEquals(
                        !effective.isDense() && effective != CableFamily.GLASS,
                        snapshot.connections().get(direction).collar()
                );
                states++;
            }
        }
        assertEquals(180, states);
    }

    @Test
    void glassLevelEmittersPromoteOnlyTheCenterCoreToCoveredGeometry() {
        CableDefinition glass = cable(CableFamily.GLASS);
        int states = 0;
        for (String emitter : List.of(
                "ae2:level_emitter",
                "ae2:energy_level_emitter"
        )) {
            for (Direction6 direction : Direction6.values()) {
                NativeStructuralSnapshot snapshot = NativeStructuralSnapshot.decoded(
                        glass,
                        Map.of(direction, FacePartSnapshot.withoutSpin(emitter)),
                        Map.of()
                );
                List<CableGeometry.Quad> geometry =
                        CableGeometry.forNativeSnapshot(snapshot);
                String message = emitter + "/" + direction;
                assertEquals(6, geometry.size(), message);
                assertCoreBounds(geometry, 5D, 11D, message);
                states++;
            }
        }
        assertEquals(12, states);

        NativeStructuralSnapshot ordinary = NativeStructuralSnapshot.decoded(
                glass,
                Map.of(
                        Direction6.UP,
                        FacePartSnapshot.withoutSpin("ae2:quartz_fiber")
                ),
                Map.of()
        );
        assertCoreBounds(
                CableGeometry.forNativeSnapshot(ordinary),
                6D,
                10D,
                "ordinary glass part"
        );
    }

    @Test
    void oppositeCableBusesAndEndpointsUseStraightGeometryWithoutCollars() {
        for (CableFamily family : List.of(
                CableFamily.COVERED,
                CableFamily.SMART,
                CableFamily.DENSE_SMART
        )) {
            CableDefinition local = cable(family);
            for (boolean endpoints : List.of(false, true)) {
                NativeStructuralSnapshot snapshot = NativeStructuralSnapshot.decoded(
                        local,
                        Map.of(),
                        Map.of()
                );
                NativeStructuralSnapshot.Connection west = endpoints
                        ? NativeStructuralSnapshot.Connection.endpoint(
                                endpointFor(family), family, family
                        )
                        : NativeStructuralSnapshot.Connection.cableBus(family, family);
                NativeStructuralSnapshot.Connection east = endpoints
                        ? NativeStructuralSnapshot.Connection.endpoint(
                                endpointFor(family), family, family
                        )
                        : NativeStructuralSnapshot.Connection.cableBus(family, family);
                snapshot = snapshot
                        .withConnection(Direction6.WEST, west)
                        .withConnection(Direction6.EAST, east);

                List<CableGeometry.Quad> geometry =
                        CableGeometry.forNativeSnapshot(snapshot);
                assertEquals(family.isSmart() ? 18 : 6, geometry.size());
                assertTrue(geometry.stream().noneMatch(quad ->
                        quad.textureRole() == CableGeometry.TextureRole.CORE));
                double expectedMinimum = family.isDense() ? -0.01D : 0D;
                double expectedMaximum = family.isDense() ? 16.01D : 16D;
                List<CableGeometry.Vertex> vertices = geometry.stream()
                        .flatMap(quad -> quad.vertices().stream())
                        .toList();
                assertEquals(
                        expectedMinimum,
                        vertices.stream().mapToDouble(CableGeometry.Vertex::x16)
                                .min().orElseThrow(),
                        0D
                );
                assertEquals(
                        expectedMaximum,
                        vertices.stream().mapToDouble(CableGeometry.Vertex::x16)
                                .max().orElseThrow(),
                        0D
                );
            }
        }
    }

    @Test
    void facadesAndCableAnchorsDoNotBlockStraightButSolidPartsDo() {
        CableFamily family = CableFamily.SMART;
        NativeStructuralSnapshot.Connection west =
                NativeStructuralSnapshot.Connection.endpoint(
                        "ae2:wireless_access_point", family, family
                );
        NativeStructuralSnapshot.Connection east =
                NativeStructuralSnapshot.Connection.endpoint(
                        "ae2:wireless_access_point", family, family
                );
        NativeStructuralSnapshot nonBlocking = NativeStructuralSnapshot.decoded(
                cable(family),
                Map.of(
                        Direction6.UP,
                        FacePartSnapshot.withoutSpin(NativeStructuralPartCatalog.CABLE_ANCHOR),
                        Direction6.DOWN,
                        FacePartSnapshot.withoutSpin(NativeStructuralPartCatalog.CABLE_ANCHOR)
                ),
                Map.of(
                        Direction6.NORTH,
                        new FacadeSnapshot("minecraft:stone", Map.of()),
                        Direction6.SOUTH,
                        new FacadeSnapshot("minecraft:glass", Map.of())
                )
        ).withConnection(Direction6.WEST, west).withConnection(Direction6.EAST, east);
        assertEquals(18, CableGeometry.forNativeSnapshot(nonBlocking).size());

        NativeStructuralSnapshot blocked = NativeStructuralSnapshot.decoded(
                cable(family),
                Map.of(Direction6.UP, new FacePartSnapshot("ae2:terminal", 0)),
                Map.of(Direction6.NORTH, new FacadeSnapshot("minecraft:stone", Map.of()))
        ).withConnection(Direction6.WEST, west).withConnection(Direction6.EAST, east);
        List<CableGeometry.Quad> ordinary = CableGeometry.forNativeSnapshot(blocked);
        assertEquals(84, ordinary.size());
        assertTrue(ordinary.stream().anyMatch(quad ->
                quad.textureRole() == CableGeometry.TextureRole.CORE));
    }

    @Test
    void rejectsMalformedBoundedFieldsAtomically() {
        assertEquals(
                NativeStructuralDecodeResult.Status.MALFORMED_FACE_PART,
                decoder.decode(null, null, Map.of()).status()
        );
        assertEquals(
                NativeStructuralDecodeResult.Status.MALFORMED_FACADE,
                decoder.decode(null, Map.of(), null).status()
        );
        Map<Direction6, Object> nullPartDirection = new HashMap<>();
        nullPartDirection.put(null, Map.of("id", "ae2:cable_anchor"));
        assertEquals(
                NativeStructuralDecodeResult.Status.MALFORMED_FACE_PART,
                decoder.decode(null, nullPartDirection, Map.of()).status()
        );
        Map<Direction6, Object> nullFacadeDirection = new HashMap<>();
        nullFacadeDirection.put(null, Map.of("Name", "minecraft:stone"));
        assertEquals(
                NativeStructuralDecodeResult.Status.MALFORMED_FACADE,
                decoder.decode(
                        Map.of("id", Ae2CableCatalog.FLUIX_GLASS_CABLE),
                        Map.of(),
                        nullFacadeDirection
                ).status()
        );
        assertEquals(
                NativeStructuralDecodeResult.Status.INVALID_FACE_PART_SPIN,
                decoder.decode(
                        null,
                        Map.of(Direction6.UP, Map.of("id", "ae2:terminal", "spin", 0)),
                        Map.of()
                ).status()
        );
        assertEquals(
                NativeStructuralDecodeResult.Status.INVALID_P2P_FREQUENCY,
                decoder.decode(
                        null,
                        Map.of(Direction6.UP, Map.of(
                                "id", "ae2:me_p2p_tunnel",
                                "freq", 65535
                        )),
                        Map.of()
                ).status()
        );
        assertEquals(
                NativeStructuralDecodeResult.Status.MALFORMED_FACADE,
                decoder.decode(
                        Map.of("id", Ae2CableCatalog.FLUIX_GLASS_CABLE),
                        Map.of(),
                        Map.of(Direction6.UP, Map.of(
                                "Name", "minecraft:oak_log",
                                "Properties", Map.of("axis", 1)
                        ))
                ).status()
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> NativePartGeometry.p2p(Direction6.NORTH, 0x1_0000)
        );
        assertNotNull(NativeStructuralPartCatalog.require("ae2:cable_anchor"));
    }

    @Test
    void rejectsUnitOnlyMalformedInputsMovedOutOfThePhysicalGallery() {
        assertEquals(
                NativeStructuralDecodeResult.Status.UNSUPPORTED_FACE_PART,
                decoder.decode(
                        Map.of("id", "ae2:fluix_covered_cable"),
                        Map.of(Direction6.NORTH, Map.of("id", "ae2:not_a_part")),
                        Map.of()
                ).status()
        );
        assertEquals(
                NativeStructuralDecodeResult.Status.INVALID_P2P_FREQUENCY,
                decoder.decode(
                        Map.of("id", "ae2:fluix_smart_cable"),
                        Map.of(
                                Direction6.NORTH,
                                Map.of("id", "ae2:me_p2p_tunnel"),
                                Direction6.SOUTH,
                                Map.of("id", "ae2:cable_anchor")
                        ),
                        Map.of()
                ).status()
        );
        assertEquals(
                NativeStructuralDecodeResult.Status.MALFORMED_FACADE,
                decoder.decode(
                        Map.of("id", "ae2:fluix_covered_cable"),
                        Map.of(),
                        Map.of(Direction6.UP, Map.of(
                                "Name", "not a resource location",
                                "Properties", "not-a-compound"
                        ))
                ).status()
        );
    }

    private static Map<String, Object> rawPart(
            NativeStructuralPartCatalog.Definition definition,
            int spin,
            short frequency
    ) {
        java.util.LinkedHashMap<String, Object> result = new java.util.LinkedHashMap<>();
        result.put("id", definition.id());
        if (definition.persistedSpin()) {
            result.put("spin", (byte) spin);
        }
        if (definition.kind() == NativeStructuralPartCatalog.Kind.P2P) {
            result.put("freq", frequency);
        }
        return Map.copyOf(result);
    }

    private static CableDefinition cable(CableFamily family) {
        return Ae2CableCatalog.definitions().stream()
                .filter(definition -> definition.family() == family)
                .findFirst()
                .orElseThrow();
    }

    private static String endpointFor(CableFamily family) {
        return switch (family) {
            case COVERED -> "ae2:energy_acceptor";
            case SMART -> "ae2:wireless_access_point";
            case DENSE_SMART -> "ae2:controller";
            default -> throw new IllegalArgumentException("unsupported test family " + family);
        };
    }

    private static void assertCoreBounds(
            List<CableGeometry.Quad> geometry,
            double expectedMin,
            double expectedMax,
            String message
    ) {
        List<CableGeometry.Vertex> vertices = geometry.stream()
                .filter(quad -> quad.textureRole() == CableGeometry.TextureRole.CORE)
                .flatMap(quad -> quad.vertices().stream())
                .toList();
        assertFalse(vertices.isEmpty(), message);
        assertEquals(expectedMin, vertices.stream().mapToDouble(vertex -> Math.min(
                vertex.x16(), Math.min(vertex.y16(), vertex.z16())
        )).min().orElseThrow(), message);
        assertEquals(expectedMax, vertices.stream().mapToDouble(vertex -> Math.max(
                vertex.x16(), Math.max(vertex.y16(), vertex.z16())
        )).max().orElseThrow(), message);
    }
}
