/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.activation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QuartzGlassRouteActivationTest {

    @Test
    void startsInactiveAndActivatesWithItsOwnExactReason() {
        QuartzGlassRouteActivation activation = new QuartzGlassRouteActivation();

        assertFalse(activation.isActive());
        assertFalse(activation.isDisabled());
        assertEquals(
                QuartzGlassRouteActivation.Reason.NOT_INSTALLED,
                activation.snapshot().reason()
        );

        activation.activate();

        assertTrue(activation.isActive());
        assertEquals(
                QuartzGlassRouteActivation.Reason.EXACT_19_2_17_QUARTZ_GLASS,
                activation.snapshot().reason()
        );
    }

    @Test
    void inactiveMismatchCanRecoverButDisablementIsTerminal() {
        QuartzGlassRouteActivation activation = new QuartzGlassRouteActivation();

        activation.inactive(
                QuartzGlassRouteActivation.Reason.REQUIRED_RESOURCES_MISMATCH
        );
        activation.activate();
        assertTrue(activation.isActive());

        activation.disable(QuartzGlassRouteActivation.Reason.RENDER_CALLBACK_FAILED);
        activation.activate();
        activation.inactive(
                QuartzGlassRouteActivation.Reason.AWAITING_EXACT_PROFILE
        );

        assertTrue(activation.isDisabled());
        assertEquals(
                QuartzGlassRouteActivation.Reason.RENDER_CALLBACK_FAILED,
                activation.snapshot().reason()
        );
    }

    @Test
    void transitionsRejectReasonsForTheWrongState() {
        QuartzGlassRouteActivation activation = new QuartzGlassRouteActivation();

        assertThrows(
                IllegalArgumentException.class,
                () -> activation.inactive(
                        QuartzGlassRouteActivation.Reason.EXACT_19_2_17_QUARTZ_GLASS
                )
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> activation.disable(
                        QuartzGlassRouteActivation.Reason.REQUIRED_RESOURCES_MISMATCH
                )
        );
    }
}
