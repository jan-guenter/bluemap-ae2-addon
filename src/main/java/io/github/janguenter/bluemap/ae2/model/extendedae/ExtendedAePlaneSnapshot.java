/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.model.extendedae;

import io.github.janguenter.bluemap.ae2.model.Direction6;
import io.github.janguenter.bluemap.ae2.profile.extendedae.ExtendedAe2235Catalog;

import java.util.Objects;

/** Installed ExtendedAE plane state; deliberately has no spin coordinate. */
public record ExtendedAePlaneSnapshot(
        String partId,
        Direction6 installedFace,
        boolean active,
        boolean powered,
        int connectionMask
) {

    public ExtendedAePlaneSnapshot {
        ExtendedAe2235Catalog.requirePlaneDefinition(partId);
        Objects.requireNonNull(installedFace, "installedFace");
        if (connectionMask < 0
                || connectionMask >= ExtendedAe2235Catalog.PLANE_CONNECTION_MASK_COUNT) {
            throw new IllegalArgumentException("plane connection mask must be in [0, 15]");
        }
    }

    /** Static maps cannot prove live grid state; both activity flags normalize off. */
    public ExtendedAePlaneSnapshot staticProjection() {
        return active || powered
                ? new ExtendedAePlaneSnapshot(
                        partId,
                        installedFace,
                        false,
                        false,
                        connectionMask
                )
                : this;
    }
}
