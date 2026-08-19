/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.adapter.bluemap522;

import io.github.janguenter.bluemap.ae2.activation.ExtensionRouteActivation;
import io.github.janguenter.bluemap.ae2.model.DriveCellOwner;
import io.github.janguenter.bluemap.ae2.model.DriveCellRouteAccess;

import java.util.Objects;

/** Bridges optional Drive-cell ownership to independently fail-closed routes. */
final class ExtensionDriveCellRouteAccess implements DriveCellRouteAccess {

    private final M45Runtime runtime;

    ExtensionDriveCellRouteAccess(M45Runtime runtime) {
        this.runtime = Objects.requireNonNull(runtime, "runtime");
    }

    @Override
    public boolean isActive(DriveCellOwner owner) {
        return runtime.active(routeId(owner));
    }

    @Override
    public void disable(DriveCellOwner owner) {
        runtime.route(routeId(owner)).disable(
                ExtensionRouteActivation.Reason.RENDER_CALLBACK_FAILED,
                "drive-cell-render-failed"
        );
    }

    @Override
    public void blockIfNativeDriveInactive() {
        runtime.blockAppMekDriveCellsIfNativeDriveInactive(false);
    }

    private static String routeId(DriveCellOwner owner) {
        return switch (Objects.requireNonNull(owner, "owner")) {
            case APPLIED_FLUX -> M45Runtime.APPFLUX;
            case MEGA_CELLS -> M45Runtime.MEGA_CELLS;
            case APPLIED_MEKANISTICS -> M45Runtime.APPMEK_DRIVE_CELLS;
            case AE2, EXTENDED_AE -> throw new IllegalArgumentException(
                    "core cell owner has no optional route"
            );
        };
    }
}
