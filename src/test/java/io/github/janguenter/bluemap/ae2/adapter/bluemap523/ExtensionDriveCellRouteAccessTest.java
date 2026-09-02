/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.adapter.bluemap523;

import io.github.janguenter.bluemap.ae2.model.DriveCellOwner;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExtensionDriveCellRouteAccessTest {

    @Test
    void activationAndFailureStayLocalToTheOwningExtension() {
        M45Runtime runtime = new M45Runtime();
        ExtensionDriveCellRouteAccess access = new ExtensionDriveCellRouteAccess(runtime);
        runtime.route(M45Runtime.APPFLUX).activate("exact-appflux");

        assertTrue(access.isActive(DriveCellOwner.APPLIED_FLUX));
        assertFalse(access.isActive(DriveCellOwner.MEGA_CELLS));

        runtime.route(M45Runtime.MEGA_CELLS).activate("exact-megacells");
        access.disable(DriveCellOwner.APPLIED_FLUX);

        assertFalse(access.isActive(DriveCellOwner.APPLIED_FLUX));
        assertTrue(access.isActive(DriveCellOwner.MEGA_CELLS));
        assertTrue(runtime.route(M45Runtime.APPFLUX).isDisabled());
        assertFalse(runtime.route(M45Runtime.MEGA_CELLS).isDisabled());
    }

    @Test
    void coreOwnersCannotBeRoutedThroughOptionalState() {
        ExtensionDriveCellRouteAccess access = new ExtensionDriveCellRouteAccess(
                new M45Runtime()
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> access.isActive(DriveCellOwner.AE2)
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> access.disable(DriveCellOwner.EXTENDED_AE)
        );
    }
}
