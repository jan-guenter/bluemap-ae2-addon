/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.adapter.bluemap522;

import io.github.janguenter.bluemap.ae2.activation.ExtensionRouteActivation;
import org.junit.jupiter.api.Test;

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

        assertTrue(runtime.routes().size() == 8);
        assertThrows(IllegalArgumentException.class, () -> runtime.route("unknown"));
    }
}
