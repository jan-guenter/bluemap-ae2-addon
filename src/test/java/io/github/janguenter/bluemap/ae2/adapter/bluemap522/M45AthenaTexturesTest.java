/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.adapter.bluemap522;

import de.bluecolored.bluemap.core.resources.pack.PackVersion;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.texture.AnimationMeta;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.texture.Texture;
import de.bluecolored.bluemap.core.util.Key;
import io.github.janguenter.bluemap.ae2.model.advancedae.AdvancedAeAthenaGeometry;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class M45AthenaTexturesTest {

    @Test
    void bakesFiveExactFrameZeroTexturesWithoutAnimation() throws Exception {
        ResourcePack pack = exactPack();

        assertTrue(M45AthenaTextures.bake(pack));

        for (AdvancedAeAthenaGeometry.Texture value
                : AdvancedAeAthenaGeometry.Texture.values()) {
            Key sourceKey = Key.parse(value.textureId());
            Key staticKey = M45AthenaTextures.staticFrame(value.textureId());
            Texture source = pack.getTextures().get(sourceKey);
            Texture frame = pack.getTextures().get(staticKey);
            assertEquals(16, frame.getTextureImage().getWidth());
            assertEquals(16, frame.getTextureImage().getHeight());
            assertNull(frame.getAnimation());
            assertEquals(
                    source.getTextureImage().getRGB(7, 5),
                    frame.getTextureImage().getRGB(7, 5)
            );
            assertEquals(
                    0xff000000 | sourceKey.getFormatted().hashCode() & 0x00ffffff,
                    frame.getTextureImage().getRGB(0, 0)
            );
        }
    }

    @Test
    void rejectsMissingDimensionsAndAnimationDriftAtomically() throws Exception {
        ResourcePack missing = exactPack();
        String particle = AdvancedAeAthenaGeometry.Texture.PARTICLE.textureId();
        missing.getTextures().remove(Key.parse(particle));
        assertFalse(M45AthenaTextures.bake(missing));
        assertNoSyntheticTextures(missing);

        ResourcePack dimensions = exactPack();
        dimensions.getTextures().put(
                Key.parse(particle),
                Texture.from(Key.parse(particle), new BufferedImage(16, 16,
                        BufferedImage.TYPE_INT_ARGB), exactAnimation())
        );
        assertFalse(M45AthenaTextures.bake(dimensions));
        assertNoSyntheticTextures(dimensions);

        ResourcePack animation = exactPack();
        animation.getTextures().put(
                Key.parse(particle),
                Texture.from(Key.parse(particle), exactImage(0xff112233),
                        new AnimationMeta(false, 1, 1, 1, List.of(
                                new AnimationMeta.FrameMeta(0, 32),
                                new AnimationMeta.FrameMeta(1, 4)
                        )))
        );
        assertFalse(M45AthenaTextures.bake(animation));
        assertNoSyntheticTextures(animation);
    }

    @Test
    void rejectsSyntheticKeyCollisionWithoutOverwritingOrPartiallyInstalling()
            throws Exception {
        ResourcePack pack = exactPack();
        String particle = AdvancedAeAthenaGeometry.Texture.PARTICLE.textureId();
        Key collision = M45AthenaTextures.staticFrame(particle);
        Texture sentinel = Texture.from(
                collision,
                new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB)
        );
        pack.getTextures().put(collision, sentinel);

        assertFalse(M45AthenaTextures.bake(pack));
        assertSame(sentinel, pack.getTextures().get(collision));
        for (AdvancedAeAthenaGeometry.Texture value
                : AdvancedAeAthenaGeometry.Texture.values()) {
            Key target = M45AthenaTextures.staticFrame(value.textureId());
            if (!target.equals(collision)) {
                assertFalse(pack.getTextures().containsKey(target));
            }
        }
    }

    private static ResourcePack exactPack() throws Exception {
        ResourcePack pack = new ResourcePack(new PackVersion(34, 0));
        for (AdvancedAeAthenaGeometry.Texture value
                : AdvancedAeAthenaGeometry.Texture.values()) {
            Key key = Key.parse(value.textureId());
            int color = 0xff000000 | key.getFormatted().hashCode() & 0x00ffffff;
            pack.getTextures().put(
                    key,
                    Texture.from(key, exactImage(color), exactAnimation())
            );
        }
        return pack;
    }

    private static BufferedImage exactImage(int firstFrameColor) {
        BufferedImage image = new BufferedImage(16, 32, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                image.setRGB(x, y, firstFrameColor);
                image.setRGB(x, y + 16, 0xffabcdef);
            }
        }
        return image;
    }

    private static AnimationMeta exactAnimation() {
        return new AnimationMeta(true, 1, 1, 1, List.of(
                new AnimationMeta.FrameMeta(0, 32),
                new AnimationMeta.FrameMeta(1, 4)
        ));
    }

    private static void assertNoSyntheticTextures(ResourcePack pack) {
        for (AdvancedAeAthenaGeometry.Texture value
                : AdvancedAeAthenaGeometry.Texture.values()) {
            assertNull(pack.getTextures().get(
                    M45AthenaTextures.staticFrame(value.textureId())
            ));
        }
    }
}
