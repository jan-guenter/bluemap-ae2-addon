/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.adapter.bluemap522;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AdapterCompatibilityTest {

    @Test
    void acceptsOnlyTheTwoAuditedBlueMapIdentities() {
        assertTrue(AdapterCompatibility.supported(
                AdapterCompatibility.UPSTREAM_VERSION,
                AdapterCompatibility.UPSTREAM_COMMIT
        ));
        assertTrue(AdapterCompatibility.supported(
                AdapterCompatibility.BACKPORT_VERSION,
                AdapterCompatibility.BACKPORT_COMMIT
        ));

        assertFalse(AdapterCompatibility.supported(
                AdapterCompatibility.UPSTREAM_VERSION,
                AdapterCompatibility.BACKPORT_COMMIT
        ));
        assertFalse(AdapterCompatibility.supported(
                "5.22.1",
                AdapterCompatibility.UPSTREAM_COMMIT
        ));
        assertFalse(AdapterCompatibility.supported("5.22", "unknown"));
    }
}
