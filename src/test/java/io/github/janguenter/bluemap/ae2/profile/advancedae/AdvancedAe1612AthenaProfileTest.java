/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.profile.advancedae;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AdvancedAe1612AthenaProfileTest {

    @Test
    void pinsAthenaIndependentlyFromAdvancedAe() {
        assertEquals("4.0.6", Athena406ArtifactIdentity.VERSION);
        assertEquals(99_944L, Athena406ArtifactIdentity.JAR_BYTES);
        assertEquals(
                "43699885bbce3343916d4c5c4940cf0e3f9f6f02fdeb46e8655e121b42282ec5",
                Athena406ArtifactIdentity.JAR_SHA256
        );
        assertTrue(AdvancedAe1612AthenaProfile.acceptsArtifacts(
                AdvancedAe1612Profile.JAR_BYTES,
                AdvancedAe1612Profile.JAR_SHA256,
                Athena406ArtifactIdentity.JAR_BYTES,
                Athena406ArtifactIdentity.JAR_SHA256
        ));
        assertFalse(AdvancedAe1612AthenaProfile.acceptsArtifacts(
                AdvancedAe1612Profile.JAR_BYTES,
                AdvancedAe1612Profile.JAR_SHA256,
                Athena406ArtifactIdentity.JAR_BYTES,
                "0".repeat(64)
        ));
    }

    @Test
    void closesWholeBlockstateAndFiveAnimatedTextureFamilies() {
        assertEquals("whole-blockstate-identity", AdvancedAe1612AthenaProfile.CONNECT_POLICY);
        assertEquals(12, AdvancedAe1612AthenaProfile.requiredResources().size());
        assertEquals(4_389L,
                AdvancedAe1612AthenaProfile.requiredResourceSizes().values().stream()
                        .mapToLong(Long::longValue)
                        .sum());
        assertEquals(5, AdvancedAe1612AthenaProfile.requiredResources().keySet().stream()
                .filter(path -> path.endsWith(".png.mcmeta"))
                .count());
        assertEquals(5, AdvancedAe1612AthenaProfile.requiredResources().keySet().stream()
                .filter(path -> path.endsWith(".png"))
                .count());
    }
}
