/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.activation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CraftingRouteActivationTest {

    @Test
    void routeActivatesAndAForcedDisablementIsTerminal() {
        CraftingRouteActivation activation = new CraftingRouteActivation();

        assertFalse(activation.isActive());
        assertEquals("crafting-not-installed", activation.reason());

        activation.inactive(CraftingRouteActivation.Reason.AWAITING_EXACT_PROFILE);
        activation.activate();
        assertTrue(activation.isActive());
        assertEquals("exact-19.2.17-crafting", activation.reason());

        activation.disable(CraftingRouteActivation.Reason.RENDER_CALLBACK_FAILED);
        activation.activate();
        activation.inactive(CraftingRouteActivation.Reason.REQUIRED_RESOURCES_MISMATCH);
        assertTrue(activation.isDisabled());
        assertEquals("crafting-render-callback-failed", activation.reason());
    }

    @Test
    void inactiveAndDisabledReasonsCannotCrossStateBoundaries() {
        CraftingRouteActivation activation = new CraftingRouteActivation();

        assertThrows(
                IllegalArgumentException.class,
                () -> activation.inactive(
                        CraftingRouteActivation.Reason.RENDER_CALLBACK_FAILED
                )
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> activation.disable(
                        CraftingRouteActivation.Reason.AWAITING_EXACT_PROFILE
                )
        );
    }

    @Test
    void craftingRegistryCollisionCannotChangeAcceptedRouteState() {
        ProfileActivation core = new ProfileActivation();
        DriveRouteActivation drive = new DriveRouteActivation();
        ExtendedAeDriveRouteActivation extended =
                new ExtendedAeDriveRouteActivation();
        QuartzGlassRouteActivation glass = new QuartzGlassRouteActivation();
        CraftingRouteActivation crafting = new CraftingRouteActivation();
        core.activate();
        drive.activate();
        extended.activate();
        glass.activate();

        crafting.disable(CraftingRouteActivation.Reason.REGISTRY_COLLISION);

        assertTrue(crafting.isDisabled());
        assertEquals("crafting-registry-collision", crafting.reason());
        assertTrue(core.isActive());
        assertTrue(drive.isActive());
        assertTrue(extended.isActive());
        assertTrue(glass.isActive());
    }
}
