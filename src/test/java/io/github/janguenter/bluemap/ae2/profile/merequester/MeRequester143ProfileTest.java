/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.profile.merequester;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MeRequester143ProfileTest {

    @Test
    void exactArtifactAndResourcesAreLocked() {
        assertEquals(12, MeRequester143Profile.requiredResources().size());
        assertEquals(
                9_295L,
                MeRequester143Profile.requiredResourceSizes().values().stream()
                        .mapToLong(Long::longValue)
                        .sum()
        );
        assertTrue(MeRequester143Profile.acceptsArtifact(
                MeRequester143Profile.JAR_BYTES,
                MeRequester143Profile.JAR_SHA256
        ));
        assertFalse(MeRequester143Profile.acceptsArtifact(
                MeRequester143Profile.JAR_BYTES,
                null
        ));
        assertThrows(
                IllegalArgumentException.class,
                () -> MeRequester143Profile.requireExactArtifact(0, "0".repeat(64))
        );
    }

    @Test
    void requesterVariantMatrixPreservesExactVerticalSpin() {
        assertEquals(24, MeRequester143Catalog.requesterVariants().size());
        assertEquals(
                12,
                MeRequester143Catalog.requesterVariants().values().stream()
                        .filter(MeRequester143Catalog.RequesterVariant::requiresAe2Z)
                        .count()
        );
        assertEquals(
                90,
                MeRequester143Catalog.variantForState(
                        "active=false,facing=east"
                ).yRotation()
        );
        MeRequester143Catalog.RequesterVariant up =
                MeRequester143Catalog.variantForState(
                        "active=true,facing=up,spin=1"
                );
        assertEquals(270, up.xRotation());
        assertEquals(90, up.zRotation());
        assertEquals(MeRequester143Catalog.REQUESTER_ACTIVE_MODEL, up.modelId());
        MeRequester143Catalog.RequesterVariant down =
                MeRequester143Catalog.variantForState(
                        "active=false,facing=down,spin=3"
                );
        assertEquals(90, down.xRotation());
        assertEquals(90, down.zRotation());
        assertNull(MeRequester143Catalog.variantForState(
                "active=false,facing=north,spin=0"
        ));
    }

    @Test
    void terminalUsesExistingNeutralDisplayPartContract() {
        assertEquals(
                List.of(
                        "ae2:part/display_base",
                        "merequester:part/requester_terminal_off",
                        "ae2:part/display_status_off"
                ),
                MeRequester143Catalog.terminalOffModelStack()
        );
        assertEquals(0, MeRequester143Catalog.MIN_SPIN);
        assertEquals(3, MeRequester143Catalog.MAX_SPIN);
        assertThrows(
                UnsupportedOperationException.class,
                () -> MeRequester143Catalog.requesterVariants().clear()
        );
    }
}
