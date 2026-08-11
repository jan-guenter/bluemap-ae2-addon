/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.model;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

/** Normalized persisted block state used by one AE2 facade. */
public record FacadeSnapshot(String blockId, Map<String, String> properties) {

    public static final String STONE = "minecraft:stone";

    public FacadeSnapshot {
        Objects.requireNonNull(blockId, "blockId");
        Objects.requireNonNull(properties, "properties");
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            if (entry.getKey() == null || entry.getValue() == null) {
                throw new IllegalArgumentException("facade properties must be non-null");
            }
        }
        TreeMap<String, String> copy = new TreeMap<>(properties);
        properties = Collections.unmodifiableMap(copy);
    }

    public boolean isPlainStone() {
        return STONE.equals(blockId) && properties.isEmpty();
    }
}
