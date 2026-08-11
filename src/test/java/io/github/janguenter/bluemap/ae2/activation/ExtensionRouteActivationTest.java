/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.activation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExtensionRouteActivationTest {

    @Test
    void activatesOneExactRouteWithoutSharingState() {
        ExtensionRouteActivation expanded = new ExtensionRouteActivation("expandedae");
        ExtensionRouteActivation mega = new ExtensionRouteActivation("megacells");

        expanded.activate("exact-2.1.1");

        assertTrue(expanded.isActive());
        assertEquals("exact-2.1.1", expanded.snapshot().detail());
        assertFalse(mega.isActive());
        assertEquals(
                ExtensionRouteActivation.Reason.NOT_INSTALLED,
                mega.snapshot().reason()
        );
    }

    @Test
    void disabledRouteCannotBeReactivatedOrMadeInactive() {
        ExtensionRouteActivation activation = new ExtensionRouteActivation("advanced-ae");
        activation.disable(
                ExtensionRouteActivation.Reason.REGISTRY_COLLISION,
                "renderer-key-collision"
        );

        activation.activate("exact-1.6.12");
        activation.inactive(
                ExtensionRouteActivation.Reason.ARTIFACT_NOT_FOUND,
                "artifact-not-found"
        );

        assertTrue(activation.isDisabled());
        assertEquals(
                ExtensionRouteActivation.Reason.REGISTRY_COLLISION,
                activation.snapshot().reason()
        );
    }

    @Test
    void rejectsMismatchedReasonsAndNoncanonicalWireValues() {
        ExtensionRouteActivation activation = new ExtensionRouteActivation("extendedae");

        assertThrows(
                IllegalArgumentException.class,
                () -> activation.inactive(
                        ExtensionRouteActivation.Reason.RENDER_CALLBACK_FAILED,
                        "render-failed"
                )
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> activation.disable(
                        ExtensionRouteActivation.Reason.ARTIFACT_MISMATCH,
                        "artifact-mismatch"
                )
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> new ExtensionRouteActivation("ExpandedAE")
        );
    }
}
