/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.api;

import java.util.List;
import java.util.Objects;

/** Atomic data bundle owned by one independently fail-closed extension route. */
public record ExtensionDefinition(
        String routeId,
        String ownerNamespace,
        List<CableBusPartDefinition> cableBusParts,
        List<NativeDriveCellDefinition> nativeDriveCells
) {

    public ExtensionDefinition {
        Identifiers.requireRouteId(routeId);
        Identifiers.requireNamespace(ownerNamespace);
        Objects.requireNonNull(cableBusParts, "cableBusParts");
        Objects.requireNonNull(nativeDriveCells, "nativeDriveCells");
        cableBusParts = List.copyOf(cableBusParts);
        nativeDriveCells = List.copyOf(nativeDriveCells);
        if (cableBusParts.isEmpty() && nativeDriveCells.isEmpty()) {
            throw new IllegalArgumentException("an extension must register at least one definition");
        }
        if (cableBusParts.size() > 64 || nativeDriveCells.size() > 256) {
            throw new IllegalArgumentException("extension definition count exceeds the API bound");
        }
    }
}
