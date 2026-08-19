/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.adapter.bluemap522;

import io.github.janguenter.bluemap.ae2.activation.ExtensionRouteActivation;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class M45RuntimeTest {

    @Test
    void routesAreIndependent() {
        M45Runtime runtime = new M45Runtime();
        runtime.route(M45Runtime.EXPANDED_AE).activate("exact-2.1.1");
        runtime.route(M45Runtime.MEGA_CELLS).inactive(
                ExtensionRouteActivation.Reason.REQUIRED_RESOURCES_MISMATCH,
                "resource-mismatch"
        );

        assertTrue(runtime.active(M45Runtime.EXPANDED_AE));
        assertFalse(runtime.active(M45Runtime.MEGA_CELLS));
        assertFalse(runtime.active(M45Runtime.APPFLUX));
    }

    @Test
    void exactRouteSetIsClosed() {
        M45Runtime runtime = new M45Runtime();

        assertEquals(
                Set.of(
                        M45Runtime.APPFLUX,
                        M45Runtime.ME_REQUESTER,
                        M45Runtime.EXPANDED_AE,
                        M45Runtime.MEGA_CELLS,
                        M45Runtime.ADVANCED_QUANTUM,
                        M45Runtime.ADVANCED_ATHENA,
                        M45Runtime.EXTENDED_MATRIX,
                        M45Runtime.EXTENDED_PLANES,
                        M45Runtime.APPMEK_DRIVE_CELLS
                ),
                runtime.routes().stream()
                        .map(ExtensionRouteActivation::routeId)
                        .collect(java.util.stream.Collectors.toUnmodifiableSet())
        );
        assertThrows(IllegalArgumentException.class, () -> runtime.route("unknown"));
    }
}
