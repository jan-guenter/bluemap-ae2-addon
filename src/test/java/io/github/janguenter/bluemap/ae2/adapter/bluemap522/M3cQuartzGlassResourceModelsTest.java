/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.adapter.bluemap522;

import de.bluecolored.bluemap.core.resources.pack.PackVersion;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.texture.Texture;
import de.bluecolored.bluemap.core.util.Key;
import io.github.janguenter.bluemap.ae2.model.QuartzGlassGeometry;
import io.github.janguenter.bluemap.ae2.model.QuartzGlassSnapshot;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class M3cQuartzGlassResourceModelsTest {

    @Test
    void exactNineteenTextureClosureIsRequired() throws Exception {
        ResourcePack empty = new ResourcePack(new PackVersion(34, 0));
        assertEquals(19, M3cQuartzGlassResourceModels.requiredTextures().size());
        assertFalse(M3cQuartzGlassResourceModels.resourcesSupported(empty));

        ResourcePack exact = exactResources();
        assertTrue(M3cQuartzGlassResourceModels.resourcesSupported(exact));
    }

    @Test
    void baseIndicesAndAllNonzeroMasksResolveToExactTextureNames() {
        List<QuartzGlassGeometry.Quad> isolated = QuartzGlassGeometry.forSnapshot(
                QuartzGlassSnapshot.isolated("ae2:quartz_glass"),
                0,
                0,
                0
        );
        QuartzGlassGeometry.Quad base = isolated.stream()
                .filter(quad -> quad.layer() == QuartzGlassGeometry.Layer.BASE)
                .findFirst()
                .orElseThrow();
        assertEquals(
                "ae2:block/glass/quartz_glass_c",
                M3cQuartzGlassResourceModels.texture(base).getFormatted()
        );

        for (int mask = 1; mask <= 15; mask++) {
            QuartzGlassGeometry.Quad frame = new QuartzGlassGeometry.Quad(
                    io.github.janguenter.bluemap.ae2.model.Direction6.UP,
                    QuartzGlassGeometry.Layer.FRAME,
                    mask,
                    isolated.get(1).vertices()
            );
            assertEquals(
                    "ae2:block/glass/quartz_glass_frame"
                            + String.format("%4s", Integer.toBinaryString(mask)).replace(' ', '0'),
                    M3cQuartzGlassResourceModels.texture(frame).getFormatted()
            );
        }
    }

    static ResourcePack exactResources() throws IOException {
        ResourcePack resourcePack = new ResourcePack(new PackVersion(34, 0));
        for (Key texture : M3cQuartzGlassResourceModels.requiredTextures()) {
            putTexture(resourcePack, texture, 0xFFFFFFFF);
        }
        return resourcePack;
    }

    static void putTexture(ResourcePack resourcePack, Key key, int argb) throws IOException {
        BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        image.setRGB(0, 0, argb);
        resourcePack.getTextures().put(key, Texture.from(key, image));
    }
}
