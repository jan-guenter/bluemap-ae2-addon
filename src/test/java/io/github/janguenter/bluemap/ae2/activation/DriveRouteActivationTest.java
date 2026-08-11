/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.activation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DriveRouteActivationTest {

    @Test
    void startsInactiveAndActivatesWithAnIndependentExactReason() {
        DriveRouteActivation activation = new DriveRouteActivation();

        assertFalse(activation.isActive());
        assertFalse(activation.isDisabled());
        assertEquals(DriveRouteActivation.Reason.NOT_INSTALLED, activation.snapshot().reason());

        activation.activate();

        assertTrue(activation.isActive());
        assertEquals(
                DriveRouteActivation.Reason.EXACT_19_2_17_DRIVE,
                activation.snapshot().reason()
        );
    }

    @Test
    void inactiveResourceMismatchCanRecoverButDisablementIsTerminal() {
        DriveRouteActivation activation = new DriveRouteActivation();

        activation.inactive(DriveRouteActivation.Reason.REQUIRED_RESOURCES_MISMATCH);
        assertFalse(activation.isActive());
        activation.activate();
        assertTrue(activation.isActive());

        activation.disable(DriveRouteActivation.Reason.RENDER_CALLBACK_FAILED);
        activation.activate();
        activation.inactive(DriveRouteActivation.Reason.AWAITING_EXACT_PROFILE);

        assertTrue(activation.isDisabled());
        assertEquals(
                DriveRouteActivation.Reason.RENDER_CALLBACK_FAILED,
                activation.snapshot().reason()
        );
    }

    @Test
    void transitionsRejectReasonsForTheWrongState() {
        DriveRouteActivation activation = new DriveRouteActivation();

        assertThrows(
                IllegalArgumentException.class,
                () -> activation.inactive(
                        DriveRouteActivation.Reason.EXACT_19_2_17_DRIVE
                )
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> activation.disable(
                        DriveRouteActivation.Reason.REQUIRED_RESOURCES_MISMATCH
                )
        );
    }
}
