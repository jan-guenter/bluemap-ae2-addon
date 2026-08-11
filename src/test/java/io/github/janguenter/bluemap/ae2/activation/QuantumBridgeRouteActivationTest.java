/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.activation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QuantumBridgeRouteActivationTest {

    @Test
    void routeActivatesAndAForcedDisablementIsTerminal() {
        QuantumBridgeRouteActivation activation = new QuantumBridgeRouteActivation();

        assertFalse(activation.isActive());
        assertEquals("quantum-bridge-not-installed", activation.reason());

        activation.inactive(
                QuantumBridgeRouteActivation.Reason.AWAITING_EXACT_PROFILE
        );
        activation.activate();
        assertTrue(activation.isActive());
        assertEquals("exact-19.2.17-quantum-bridge", activation.reason());

        activation.disable(
                QuantumBridgeRouteActivation.Reason.RENDER_CALLBACK_FAILED
        );
        activation.activate();
        activation.inactive(
                QuantumBridgeRouteActivation.Reason.REQUIRED_RESOURCES_MISMATCH
        );
        assertTrue(activation.isDisabled());
        assertEquals("quantum-bridge-render-callback-failed", activation.reason());
    }

    @Test
    void inactiveAndDisabledReasonsCannotCrossStateBoundaries() {
        QuantumBridgeRouteActivation activation = new QuantumBridgeRouteActivation();

        assertThrows(
                IllegalArgumentException.class,
                () -> activation.inactive(
                        QuantumBridgeRouteActivation.Reason.RENDER_CALLBACK_FAILED
                )
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> activation.disable(
                        QuantumBridgeRouteActivation.Reason.AWAITING_EXACT_PROFILE
                )
        );
    }

    @Test
    void quantumFailureLeavesEveryPreviouslyAcceptedRouteActive() {
        ProfileActivation core = new ProfileActivation();
        DriveRouteActivation drive = new DriveRouteActivation();
        ExtendedAeDriveRouteActivation extended = new ExtendedAeDriveRouteActivation();
        QuartzGlassRouteActivation glass = new QuartzGlassRouteActivation();
        CraftingRouteActivation crafting = new CraftingRouteActivation();
        QuantumBridgeRouteActivation quantum = new QuantumBridgeRouteActivation();
        core.activate();
        drive.activate();
        extended.activate();
        glass.activate();
        crafting.activate();

        quantum.disable(QuantumBridgeRouteActivation.Reason.REGISTRY_COLLISION);

        assertTrue(quantum.isDisabled());
        assertTrue(core.isActive());
        assertTrue(drive.isActive());
        assertTrue(extended.isActive());
        assertTrue(glass.isActive());
        assertTrue(crafting.isActive());
    }
}
