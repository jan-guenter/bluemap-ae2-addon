/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.api;

import java.util.List;
import java.util.Objects;

/** Immutable cable-bus part data supplied by one soft-dependent add-on. */
public record CableBusPartDefinition(
        String partId,
        CableBusPartKind kind,
        int cableConnectionLength,
        double facadeCutoutMin16,
        double facadeCutoutMax16,
        List<String> modelPaths
) {

    public CableBusPartDefinition {
        Identifiers.requireResourceLocation(partId, "partId");
        Objects.requireNonNull(kind, "kind");
        if (cableConnectionLength < 0 || cableConnectionLength > 16) {
            throw new IllegalArgumentException("cableConnectionLength must be in [0, 16]");
        }
        if (!Double.isFinite(facadeCutoutMin16)
                || !Double.isFinite(facadeCutoutMax16)
                || facadeCutoutMin16 < 0D || facadeCutoutMax16 > 16D
                || facadeCutoutMin16 >= facadeCutoutMax16) {
            throw new IllegalArgumentException("invalid facade cutout bounds");
        }
        Objects.requireNonNull(modelPaths, "modelPaths");
        modelPaths = List.copyOf(modelPaths);
        if (modelPaths.isEmpty() || modelPaths.size() > 4) {
            throw new IllegalArgumentException("modelPaths must contain one to four entries");
        }
        modelPaths.forEach(path -> Identifiers.requireResourceLocation(path, "modelPath"));
        if (kind == CableBusPartKind.P2P && (modelPaths.size() != 3
                || !"ae2:part/p2p/p2p_tunnel_status_off".equals(modelPaths.get(0))
                || !"ae2:part/p2p/p2p_tunnel_frequency".equals(modelPaths.get(1)))) {
            throw new IllegalArgumentException(
                    "P2P parts require AE2's status and frequency layers plus one front layer"
            );
        }
    }
}
