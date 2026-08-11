/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.adapter.bluemap522;

import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.util.Key;
import io.github.janguenter.bluemap.ae2.model.CraftingGeometry;
import io.github.janguenter.bluemap.ae2.profile.Ae219217CraftingProfile;

import java.util.Map;
import java.util.Set;

/** Exact operator-installed texture mapping for the AE2 19.2.17 M3d route. */
final class M3dCraftingResourceModels {

    private static final Map<CraftingGeometry.Layer, Key> TEXTURES = Map.ofEntries(
            texture(
                    CraftingGeometry.Layer.RING_CORNER,
                    Ae219217CraftingProfile.RING_CORNER_TEXTURE
            ),
            texture(
                    CraftingGeometry.Layer.RING_SIDE_HORIZONTAL,
                    Ae219217CraftingProfile.RING_SIDE_HORIZONTAL_TEXTURE
            ),
            texture(
                    CraftingGeometry.Layer.RING_SIDE_VERTICAL,
                    Ae219217CraftingProfile.RING_SIDE_VERTICAL_TEXTURE
            ),
            texture(
                    CraftingGeometry.Layer.UNIT_BASE,
                    Ae219217CraftingProfile.UNIT_BASE_TEXTURE
            ),
            texture(
                    CraftingGeometry.Layer.LIGHT_BASE,
                    Ae219217CraftingProfile.LIGHT_BASE_TEXTURE
            ),
            texture(
                    CraftingGeometry.Layer.ACCELERATOR_LIGHT,
                    Ae219217CraftingProfile.ACCELERATOR_LIGHT_TEXTURE
            ),
            texture(
                    CraftingGeometry.Layer.STORAGE_1K_LIGHT,
                    Ae219217CraftingProfile.STORAGE_1K_LIGHT_TEXTURE
            ),
            texture(
                    CraftingGeometry.Layer.STORAGE_4K_LIGHT,
                    Ae219217CraftingProfile.STORAGE_4K_LIGHT_TEXTURE
            ),
            texture(
                    CraftingGeometry.Layer.STORAGE_16K_LIGHT,
                    Ae219217CraftingProfile.STORAGE_16K_LIGHT_TEXTURE
            ),
            texture(
                    CraftingGeometry.Layer.STORAGE_64K_LIGHT,
                    Ae219217CraftingProfile.STORAGE_64K_LIGHT_TEXTURE
            ),
            texture(
                    CraftingGeometry.Layer.STORAGE_256K_LIGHT,
                    Ae219217CraftingProfile.STORAGE_256K_LIGHT_TEXTURE
            ),
            texture(
                    CraftingGeometry.Layer.MONITOR_BASE,
                    Ae219217CraftingProfile.MONITOR_BASE_TEXTURE
            ),
            texture(
                    CraftingGeometry.Layer.MONITOR_LIGHT_DARK,
                    Ae219217CraftingProfile.MONITOR_LIGHT_DARK_TEXTURE
            ),
            texture(
                    CraftingGeometry.Layer.MONITOR_LIGHT_MEDIUM,
                    Ae219217CraftingProfile.MONITOR_LIGHT_MEDIUM_TEXTURE
            ),
            texture(
                    CraftingGeometry.Layer.MONITOR_LIGHT_BRIGHT,
                    Ae219217CraftingProfile.MONITOR_LIGHT_BRIGHT_TEXTURE
            )
    );
    private static final Set<Key> REQUIRED_TEXTURES = Set.copyOf(TEXTURES.values());

    private M3dCraftingResourceModels() {
    }

    static Set<Key> requiredTextures() {
        return REQUIRED_TEXTURES;
    }

    static boolean resourcesSupported(ResourcePack resourcePack) {
        if (resourcePack == null
                || REQUIRED_TEXTURES.size() != CraftingGeometry.Layer.values().length
                || TEXTURES.size() != CraftingGeometry.Layer.values().length) {
            return false;
        }
        for (Key texture : REQUIRED_TEXTURES) {
            if (ResourcePack.MISSING_TEXTURE.equals(texture)
                    || resourcePack.getTextures().get(texture) == null) {
                return false;
            }
        }
        return true;
    }

    static Key texture(CraftingGeometry.Quad quad) {
        Key texture = TEXTURES.get(quad.layer());
        if (texture == null) {
            throw new IllegalArgumentException("unsupported crafting layer " + quad.layer());
        }
        return texture;
    }

    private static Map.Entry<CraftingGeometry.Layer, Key> texture(
            CraftingGeometry.Layer layer,
            String key
    ) {
        return Map.entry(layer, Key.parse(key));
    }
}
