/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.profile;

import io.github.janguenter.bluemap.ae2.model.DriveCellCatalog;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** Exact AE2 19.2.17 constants for the independently gated M3a drive slice. */
public final class Ae219217DriveProfile {

    public static final String DRIVE_BLOCK = "ae2:drive";
    public static final String SYNTHETIC_BLOCK_STATE = "bluemap_ae2:drive";
    public static final String LED_TEXTURE = "ae2:block/drive/drive_front";

    private static final Set<String> ITEM_IDS = DriveCellCatalog.ids();
    private static final Set<String> OCCUPIED_MODELS = DriveCellCatalog.occupiedModels();
    private static final Set<String> MODELS = buildModels();
    private static final List<String> TEXTURES = List.of(
            "ae2:block/drive/drive_front",
            "ae2:block/drive/drive_cells",
            "ae2:block/drive/drive_inside_top",
            "ae2:block/drive/drive_inside_bottom",
            "ae2:block/drive/drive_inside",
            "ae2:block/generics/back",
            "ae2:block/generics/bottom",
            "ae2:block/generics/front",
            "ae2:block/generics/side",
            "ae2:block/generics/top"
    );

    private Ae219217DriveProfile() {
    }

    public static Set<String> itemIds() {
        return ITEM_IDS;
    }

    public static Set<String> occupiedModels() {
        return OCCUPIED_MODELS;
    }

    /** Base, empty chassis and all twelve occupied chassis models. */
    public static Set<String> models() {
        return MODELS;
    }

    public static List<String> textures() {
        return TEXTURES;
    }

    private static Set<String> buildModels() {
        Set<String> models = new LinkedHashSet<>();
        models.add(DriveCellCatalog.BASE_MODEL);
        models.add(DriveCellCatalog.EMPTY_CELL_MODEL);
        models.addAll(DriveCellCatalog.occupiedModels());
        return Set.copyOf(models);
    }
}
