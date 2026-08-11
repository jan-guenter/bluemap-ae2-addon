/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.activation;

import io.github.janguenter.bluemap.ae2.profile.extendedae.ExtendedAe2235ArtifactIdentity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExtendedAeDriveRouteActivationTest {

    @Test
    void startsInactiveAndActivatesIndependently() {
        ExtendedAeDriveRouteActivation activation = new ExtendedAeDriveRouteActivation();

        assertFalse(activation.isActive());
        assertFalse(activation.isDisabled());
        assertEquals(
                ExtendedAeDriveRouteActivation.Reason.NOT_INSTALLED,
                activation.snapshot().reason()
        );

        activation.activate();

        assertTrue(activation.isActive());
        assertEquals(
                ExtendedAeDriveRouteActivation.Reason
                        .EXACT_AE2_19_2_17_EXTENDEDAE_2_2_33_DRIVE,
                activation.snapshot().reason()
        );
    }

    @Test
    void recoverableMismatchDoesNotAffectTerminalRouteDisablement() {
        ExtendedAeDriveRouteActivation activation = new ExtendedAeDriveRouteActivation();

        activation.inactive(
                ExtendedAeDriveRouteActivation.Reason.EXTENDEDAE_ARTIFACT_MISMATCH
        );
        activation.activate();
        assertTrue(activation.isActive());

        activation.disable(ExtendedAeDriveRouteActivation.Reason.RENDER_CALLBACK_FAILED);
        activation.activate();
        activation.inactive(ExtendedAeDriveRouteActivation.Reason.AWAITING_EXACT_PROFILE);

        assertTrue(activation.isDisabled());
        assertEquals(
                "extended-drive-render-callback-failed",
                activation.reason()
        );
    }

    @Test
    void activatesTheExact2235CompatibilityIdentityIndependently() {
        ExtendedAeDriveRouteActivation activation = new ExtendedAeDriveRouteActivation();

        activation.activate(ExtendedAe2235ArtifactIdentity.EXACT_REASON);

        assertTrue(activation.isActive());
        assertEquals(
                ExtendedAeDriveRouteActivation.Reason
                        .EXACT_AE2_19_2_17_EXTENDEDAE_2_2_35_DRIVE,
                activation.snapshot().reason()
        );
    }

    @Test
    void transitionMethodsRejectReasonsFromTheWrongState() {
        ExtendedAeDriveRouteActivation activation = new ExtendedAeDriveRouteActivation();

        assertThrows(
                IllegalArgumentException.class,
                () -> activation.inactive(
                        ExtendedAeDriveRouteActivation.Reason
                                .EXACT_AE2_19_2_17_EXTENDEDAE_2_2_33_DRIVE
                )
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> activation.disable(
                        ExtendedAeDriveRouteActivation.Reason.REQUIRED_RESOURCES_MISMATCH
                )
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> activation.activate("exact-unreviewed-version")
        );
    }
}
