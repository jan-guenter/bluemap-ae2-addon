/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.profile;

import io.github.janguenter.bluemap.ae2.profile.appflux.AppFlux215Profile;
import io.github.janguenter.bluemap.ae2.profile.expandedae.ExpandedAe211Profile;
import io.github.janguenter.bluemap.ae2.profile.merequester.MeRequester143Profile;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class M4ExtensionIsolationTest {

    @Test
    void oneRejectedExtensionIdentityDoesNotMutateAnotherProfile() {
        assertThrows(
                IllegalArgumentException.class,
                () -> AppFlux215Profile.requireExactArtifact(
                        AppFlux215Profile.JAR_BYTES,
                        "0".repeat(64)
                )
        );
        assertTrue(MeRequester143Profile.acceptsArtifact(
                MeRequester143Profile.JAR_BYTES,
                MeRequester143Profile.JAR_SHA256
        ));
        assertTrue(ExpandedAe211Profile.acceptsArtifact(
                ExpandedAe211Profile.JAR_BYTES,
                ExpandedAe211Profile.JAR_SHA256
        ));
        assertEquals(12, MeRequester143Profile.requiredResources().size());
        assertEquals(142, ExpandedAe211Profile.requiredResources().size());
    }

    @Test
    void expandedAeRejectionLeavesAppliedFluxProfileUsable() {
        assertThrows(
                IllegalArgumentException.class,
                () -> ExpandedAe211Profile.requireExactArtifact(
                        ExpandedAe211Profile.JAR_BYTES + 1,
                        ExpandedAe211Profile.JAR_SHA256
                )
        );
        assertTrue(AppFlux215Profile.acceptsArtifact(
                AppFlux215Profile.JAR_BYTES,
                AppFlux215Profile.JAR_SHA256
        ));
        assertEquals(19, AppFlux215Profile.requiredResources().size());
    }
}
