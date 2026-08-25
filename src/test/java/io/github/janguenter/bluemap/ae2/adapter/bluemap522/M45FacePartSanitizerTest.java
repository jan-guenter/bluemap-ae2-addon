/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.adapter.bluemap522;

import io.github.janguenter.bluemap.ae2.activation.ExtensionRouteActivation;
import io.github.janguenter.bluemap.ae2.model.Direction6;
import io.github.janguenter.bluemap.ae2.model.NativeStructuralPartCatalog;
import io.github.janguenter.bluemap.ae2.profile.extendedae.ExtendedAe2235Catalog;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class M45FacePartSanitizerTest {

    @Test
    void omitsOnlyKnownInactiveOrMalformedExtensionParts() {
        M45Runtime runtime = new M45Runtime();
        runtime.route(M45Runtime.APPFLUX).activate("exact-profile");
        runtime.route(M45Runtime.ME_REQUESTER).activate("exact-profile");
        Object core = Map.of("id", "ae2:terminal", "spin", (byte) 0);
        Object unknown = Map.of("id", "example:unknown");
        Map<Direction6, Object> result = M45FacePartSanitizer.sanitize(
                Map.of(
                        Direction6.DOWN, core,
                        Direction6.UP, unknown,
                        Direction6.NORTH, Map.of("id", "appflux:part_flux_accessor"),
                        Direction6.SOUTH, Map.of(
                                "id", "merequester:requester_terminal",
                                "spin", (byte) 4
                        ),
                        Direction6.WEST, Map.of(
                                "id", "expandedae:exp_pattern_provider_part"
                        )
                ),
                runtime
        );
        assertEquals(3, result.size());
        assertSame(core, result.get(Direction6.DOWN));
        assertSame(unknown, result.get(Direction6.UP));
        assertEquals(
                "appflux:part_flux_accessor",
                ((Map<?, ?>) result.get(Direction6.NORTH)).get("id")
        );
    }

    @Test
    void disabledStateIsStickyAndThereforeOmitted() {
        M45Runtime runtime = new M45Runtime();
        runtime.route(M45Runtime.APPFLUX).disable(
                ExtensionRouteActivation.Reason.RENDER_CALLBACK_FAILED,
                "render-callback-failed"
        );
        runtime.route(M45Runtime.APPFLUX).activate("exact-profile");
        assertEquals(
                Map.of(),
                M45FacePartSanitizer.sanitize(
                        Map.of(
                                Direction6.UP,
                                Map.of("id", "appflux:part_flux_accessor")
                        ),
                        runtime
                )
        );
    }

    @Test
    void preservesNullDirectionForStrictDecoderAndOmitsMalformedDockState() {
        M45Runtime runtime = new M45Runtime();
        runtime.route(M45Runtime.MEGA_CELLS).activate("exact-profile");
        Map<Direction6, Object> nullDirection = new LinkedHashMap<>();
        nullDirection.put(null, Map.of("id", "ae2:terminal", "spin", (byte) 0));
        assertSame(nullDirection, M45FacePartSanitizer.sanitize(nullDirection, runtime));

        assertEquals(
                Map.of(),
                M45FacePartSanitizer.sanitize(
                        Map.of(
                                Direction6.NORTH,
                                Map.of(
                                        "id", "megacells:cell_dock",
                                        "spin", (byte) 0,
                                        "cell", Boolean.FALSE
                                )
                        ),
                        runtime
                )
        );
    }

    @Test
    void extendedPlanesAreRetainedOnlyForTheActiveRouteAndExactDirectState() {
        M45Runtime runtime = new M45Runtime();
        String activePlane = ExtendedAe2235Catalog.planePartIds().iterator().next();
        String peerPlane = ExtendedAe2235Catalog.planePartIds().stream()
                .filter(id -> !id.equals(activePlane))
                .findFirst()
                .orElseThrow();
        Map<Direction6, Object> exact = Map.of(
                Direction6.NORTH, Map.of("id", activePlane),
                Direction6.SOUTH, Map.of("id", peerPlane)
        );

        assertEquals(Map.of(), M45FacePartSanitizer.sanitize(exact, runtime));

        runtime.route(M45Runtime.EXTENDED_PLANES).activate("exact-profile");
        assertEquals(exact, M45FacePartSanitizer.sanitize(exact, runtime));

        for (Map.Entry<String, Object> malformed : Map.<String, Object>of(
                "spin", (byte) 0,
                "freq", (short) 0,
                "cell", Map.of()
        ).entrySet()) {
            assertEquals(
                    Map.of(),
                    M45FacePartSanitizer.sanitize(
                            Map.of(Direction6.UP, Map.of(
                                    "id", activePlane,
                                    malformed.getKey(), malformed.getValue()
                            )),
                            runtime
                    ),
                    malformed.getKey()
            );
        }
    }

    @Test
    void p2pExtensionsRequireAndRetainThePersistedShortFrequency() {
        NativeStructuralPartCatalog.Definition definition =
                new NativeStructuralPartCatalog.Definition(
                        "arseng:source_p2p_tunnel",
                        NativeStructuralPartCatalog.Kind.P2P,
                        false,
                        1,
                        2D,
                        14D,
                        java.util.List.of(
                                "ae2:part/p2p/p2p_tunnel_status_off",
                                "ae2:part/p2p/p2p_tunnel_frequency",
                                "arseng:part/source_p2p_tunnel"
                        ),
                        "arseng-2.1.1-beta"
                );

        assertTrue(M45FacePartSanitizer.directStateValid(
                Map.of("id", definition.id(), "freq", (short) -1),
                definition
        ));
        assertFalse(M45FacePartSanitizer.directStateValid(
                Map.of("id", definition.id()),
                definition
        ));
        assertFalse(M45FacePartSanitizer.directStateValid(
                Map.of("id", definition.id(), "freq", 65_535),
                definition
        ));
    }
}
