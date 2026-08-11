/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.model;

import java.util.Objects;

/** Immutable exact cable registry identity plus its normalized family and color. */
public record CableDefinition(String id, CableFamily family, CableColor color) {

    public CableDefinition {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(family, "family");
        Objects.requireNonNull(color, "color");
        if (!id.startsWith("ae2:") || id.length() <= "ae2:".length()) {
            throw new IllegalArgumentException("cable ID must use the ae2 namespace");
        }
    }

    public String coreTexture() {
        return coreTexture(family);
    }

    public String coreTexture(CableFamily renderedFamily) {
        Objects.requireNonNull(renderedFamily, "renderedFamily");
        return "ae2:part/cable/core/" + renderedFamily.coreTextureFolder()
                + "/" + color.textureName();
    }

    public String connectionTexture(CableFamily renderedFamily) {
        Objects.requireNonNull(renderedFamily, "renderedFamily");
        return "ae2:part/cable/" + renderedFamily.connectionTextureFolder()
                + "/" + color.textureName();
    }
}
