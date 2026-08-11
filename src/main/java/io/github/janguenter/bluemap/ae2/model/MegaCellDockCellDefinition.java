/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.model;

import java.util.Objects;

/** One exact MEGA Cells item-to-drive-chassis registration. */
public record MegaCellDockCellDefinition(
        String itemId,
        String modelId,
        ChassisKind chassisKind
) {

    public MegaCellDockCellDefinition {
        Objects.requireNonNull(itemId, "itemId");
        Objects.requireNonNull(modelId, "modelId");
        Objects.requireNonNull(chassisKind, "chassisKind");
        if (!itemId.matches("megacells:[a-z0-9_]+")) {
            throw new IllegalArgumentException("invalid MEGA Cells item ID");
        }
        if (!modelId.matches("megacells:block/drive/cells/[a-z0-9_]+")) {
            throw new IllegalArgumentException("invalid MEGA Cells chassis model ID");
        }
    }

    public enum ChassisKind {
        STANDARD("standard", 12),
        MISC("misc", 6);

        private final String serializedName;
        private final int nominalTriangles;

        ChassisKind(String serializedName, int nominalTriangles) {
            this.serializedName = serializedName;
            this.nominalTriangles = nominalTriangles;
        }

        public String serializedName() {
            return serializedName;
        }

        public int nominalTriangles() {
            return nominalTriangles;
        }

        public static ChassisKind fromSerializedName(String name) {
            for (ChassisKind value : values()) {
                if (value.serializedName.equals(name)) {
                    return value;
                }
            }
            throw new IllegalArgumentException("unknown MEGA Cells chassis kind: " + name);
        }
    }
}
