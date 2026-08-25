/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.api;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class Ae2ExtensionRegistryHostAccessTest {

    @Test
    void dependentCallerCannotAcquireHostMutationAuthority() {
        assertThrows(
                SecurityException.class,
                Ae2ExtensionRegistry.Host::acquireAccess
        );
    }
}
