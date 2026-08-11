/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.activation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NativeStructuralRouteActivationTest {

    @Test
    void isFailClosedAndDisablementIsSticky() {
        NativeStructuralRouteActivation activation =
                new NativeStructuralRouteActivation();
        assertFalse(activation.isActive());
        assertEquals("native-structural-not-installed", activation.reason());

        activation.inactive(
                NativeStructuralRouteActivation.Reason.AWAITING_EXACT_PROFILE
        );
        activation.activate();
        assertTrue(activation.isActive());

        activation.disable(
                NativeStructuralRouteActivation.Reason.RENDER_CALLBACK_FAILED
        );
        activation.activate();
        activation.inactive(
                NativeStructuralRouteActivation.Reason.REQUIRED_RESOURCES_MISMATCH
        );
        assertTrue(activation.isDisabled());
        assertEquals(
                "native-structural-render-callback-failed",
                activation.reason()
        );
    }
}
