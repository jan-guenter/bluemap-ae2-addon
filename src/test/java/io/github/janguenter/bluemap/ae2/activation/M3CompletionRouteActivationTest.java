/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.activation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class M3CompletionRouteActivationTest {

    @Test
    void routeActivatesAndDisablementIsTerminal() {
        M3CompletionRouteActivation activation = new M3CompletionRouteActivation();
        assertFalse(activation.isActive());
        assertEquals("m3-completion-not-installed", activation.reason());

        activation.inactive(M3CompletionRouteActivation.Reason.AWAITING_EXACT_PROFILE);
        activation.activate();
        assertTrue(activation.isActive());
        assertEquals("exact-19.2.17-m3-completion", activation.reason());

        activation.disable(M3CompletionRouteActivation.Reason.RENDER_CALLBACK_FAILED);
        activation.activate();
        activation.inactive(M3CompletionRouteActivation.Reason.REQUIRED_RESOURCES_MISMATCH);
        assertTrue(activation.isDisabled());
        assertEquals("m3-completion-render-callback-failed", activation.reason());
    }

    @Test
    void stateReasonsCannotCrossBoundaries() {
        M3CompletionRouteActivation activation = new M3CompletionRouteActivation();
        assertThrows(IllegalArgumentException.class, () -> activation.inactive(
                M3CompletionRouteActivation.Reason.REGISTRY_COLLISION
        ));
        assertThrows(IllegalArgumentException.class, () -> activation.disable(
                M3CompletionRouteActivation.Reason.AWAITING_EXACT_PROFILE
        ));
    }

    @Test
    void routeFailureLeavesEveryEarlierRouteActive() {
        ProfileActivation core = new ProfileActivation();
        DriveRouteActivation drive = new DriveRouteActivation();
        ExtendedAeDriveRouteActivation extended = new ExtendedAeDriveRouteActivation();
        QuartzGlassRouteActivation glass = new QuartzGlassRouteActivation();
        CraftingRouteActivation crafting = new CraftingRouteActivation();
        QuantumBridgeRouteActivation quantum = new QuantumBridgeRouteActivation();
        M3CompletionRouteActivation completion = new M3CompletionRouteActivation();
        core.activate();
        drive.activate();
        extended.activate();
        glass.activate();
        crafting.activate();
        quantum.activate();

        completion.disable(M3CompletionRouteActivation.Reason.REGISTRY_COLLISION);

        assertTrue(completion.isDisabled());
        assertTrue(core.isActive());
        assertTrue(drive.isActive());
        assertTrue(extended.isActive());
        assertTrue(glass.isActive());
        assertTrue(crafting.isActive());
        assertTrue(quantum.isActive());
    }
}
