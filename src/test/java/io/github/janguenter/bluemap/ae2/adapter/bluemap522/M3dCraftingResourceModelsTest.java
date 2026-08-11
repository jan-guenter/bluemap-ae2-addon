/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.adapter.bluemap522;

import de.bluecolored.bluemap.core.resources.pack.PackVersion;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.texture.Texture;
import de.bluecolored.bluemap.core.util.Key;
import io.github.janguenter.bluemap.ae2.model.CableColor;
import io.github.janguenter.bluemap.ae2.model.CraftingBlockKind;
import io.github.janguenter.bluemap.ae2.model.CraftingGeometry;
import io.github.janguenter.bluemap.ae2.model.CraftingSnapshot;
import io.github.janguenter.bluemap.ae2.model.Direction6;
import io.github.janguenter.bluemap.ae2.profile.Ae219217CraftingProfile;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class M3dCraftingResourceModelsTest {

    @Test
    void exactFifteenTextureClosureIsRequiredWithoutEagerManifestAccess()
            throws Exception {
        ResourcePack empty = new ResourcePack(new PackVersion(34, 0));
        assertEquals(15, M3dCraftingResourceModels.requiredTextures().size());
        assertFalse(M3dCraftingResourceModels.resourcesSupported(empty));

        ResourcePack exact = exactResources();
        assertTrue(M3dCraftingResourceModels.resourcesSupported(exact));

        Key removed = Key.parse(Ae219217CraftingProfile.MONITOR_LIGHT_DARK_TEXTURE);
        exact.getTextures().remove(removed);
        assertFalse(M3dCraftingResourceModels.resourcesSupported(exact));
    }

    @Test
    void everyNeutralLayerMapsToItsExactOperatorInstalledTexture() {
        Map<CraftingGeometry.Layer, String> expected = new EnumMap<>(
                CraftingGeometry.Layer.class
        );
        expected.put(
                CraftingGeometry.Layer.RING_CORNER,
                Ae219217CraftingProfile.RING_CORNER_TEXTURE
        );
        expected.put(
                CraftingGeometry.Layer.RING_SIDE_HORIZONTAL,
                Ae219217CraftingProfile.RING_SIDE_HORIZONTAL_TEXTURE
        );
        expected.put(
                CraftingGeometry.Layer.RING_SIDE_VERTICAL,
                Ae219217CraftingProfile.RING_SIDE_VERTICAL_TEXTURE
        );
        expected.put(CraftingGeometry.Layer.UNIT_BASE, Ae219217CraftingProfile.UNIT_BASE_TEXTURE);
        expected.put(
                CraftingGeometry.Layer.LIGHT_BASE,
                Ae219217CraftingProfile.LIGHT_BASE_TEXTURE
        );
        expected.put(
                CraftingGeometry.Layer.ACCELERATOR_LIGHT,
                Ae219217CraftingProfile.ACCELERATOR_LIGHT_TEXTURE
        );
        expected.put(
                CraftingGeometry.Layer.STORAGE_1K_LIGHT,
                Ae219217CraftingProfile.STORAGE_1K_LIGHT_TEXTURE
        );
        expected.put(
                CraftingGeometry.Layer.STORAGE_4K_LIGHT,
                Ae219217CraftingProfile.STORAGE_4K_LIGHT_TEXTURE
        );
        expected.put(
                CraftingGeometry.Layer.STORAGE_16K_LIGHT,
                Ae219217CraftingProfile.STORAGE_16K_LIGHT_TEXTURE
        );
        expected.put(
                CraftingGeometry.Layer.STORAGE_64K_LIGHT,
                Ae219217CraftingProfile.STORAGE_64K_LIGHT_TEXTURE
        );
        expected.put(
                CraftingGeometry.Layer.STORAGE_256K_LIGHT,
                Ae219217CraftingProfile.STORAGE_256K_LIGHT_TEXTURE
        );
        expected.put(
                CraftingGeometry.Layer.MONITOR_BASE,
                Ae219217CraftingProfile.MONITOR_BASE_TEXTURE
        );
        expected.put(
                CraftingGeometry.Layer.MONITOR_LIGHT_DARK,
                Ae219217CraftingProfile.MONITOR_LIGHT_DARK_TEXTURE
        );
        expected.put(
                CraftingGeometry.Layer.MONITOR_LIGHT_MEDIUM,
                Ae219217CraftingProfile.MONITOR_LIGHT_MEDIUM_TEXTURE
        );
        expected.put(
                CraftingGeometry.Layer.MONITOR_LIGHT_BRIGHT,
                Ae219217CraftingProfile.MONITOR_LIGHT_BRIGHT_TEXTURE
        );

        Map<CraftingGeometry.Layer, CraftingGeometry.Quad> examples = new EnumMap<>(
                CraftingGeometry.Layer.class
        );
        for (CraftingBlockKind kind : CraftingBlockKind.values()) {
            for (CraftingGeometry.Quad quad : CraftingGeometry.forSnapshot(
                    new CraftingSnapshot(
                            kind,
                            true,
                            Direction6.NORTH,
                            0,
                            CableColor.TRANSPARENT,
                            Set.of()
                    )
            )) {
                examples.putIfAbsent(quad.layer(), quad);
            }
        }
        assertEquals(expected.keySet(), examples.keySet());
        for (Map.Entry<CraftingGeometry.Layer, String> entry : expected.entrySet()) {
            assertEquals(
                    entry.getValue(),
                    M3dCraftingResourceModels.texture(examples.get(entry.getKey()))
                            .getFormatted(),
                    entry.getKey().name()
            );
        }
    }

    static ResourcePack exactResources() throws IOException {
        ResourcePack resourcePack = new ResourcePack(new PackVersion(34, 0));
        int color = 0xFF102030;
        for (Key texture : M3dCraftingResourceModels.requiredTextures()) {
            putTexture(resourcePack, texture, color++);
        }
        return resourcePack;
    }

    static void putTexture(ResourcePack resourcePack, Key key, int argb)
            throws IOException {
        BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        image.setRGB(0, 0, argb);
        resourcePack.getTextures().put(key, Texture.from(key, image));
    }
}
