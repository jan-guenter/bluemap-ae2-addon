/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.api;

/** One exact item-to-chassis mapping for the native ten-slot AE2 Drive. */
public record NativeDriveCellDefinition(String itemId, String modelId) {

    public NativeDriveCellDefinition {
        Identifiers.requireResourceLocation(itemId, "itemId");
        Identifiers.requireResourceLocation(modelId, "modelId");
    }
}
