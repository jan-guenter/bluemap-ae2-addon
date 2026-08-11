/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.adapter.bluemap522;

import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.texture.AnimationMeta;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.texture.Texture;
import de.bluecolored.bluemap.core.util.Key;
import io.github.janguenter.bluemap.ae2.model.advancedae.AdvancedAeAthenaGeometry;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/** Route-local, non-animated frame-zero textures for the exact Athena projection. */
final class M45AthenaTextures {

    private static final int FRAME_SIZE = 16;
    private static final Map<Key, Key> STATIC_KEYS = staticKeys();

    private M45AthenaTextures() {
    }

    static Key staticFrame(String sourceTexture) {
        Key result = STATIC_KEYS.get(Key.parse(sourceTexture));
        if (result == null) {
            throw new IllegalArgumentException("unknown exact Athena texture");
        }
        return result;
    }

    /** Validates and installs all five derived textures atomically. */
    static boolean bake(ResourcePack resourcePack) {
        Objects.requireNonNull(resourcePack, "resourcePack");
        for (Key target : STATIC_KEYS.values()) {
            if (resourcePack.getTextures().containsKey(target)) {
                return false;
            }
        }
        LinkedHashMap<Key, Texture> planned = new LinkedHashMap<>();
        try {
            for (Map.Entry<Key, Key> entry : STATIC_KEYS.entrySet()) {
                Texture source = resourcePack.getTextures().get(entry.getKey());
                if (!exactAnimatedSource(source)) {
                    return false;
                }
                BufferedImage image = source.getTextureImage();
                BufferedImage frame = new BufferedImage(
                        FRAME_SIZE,
                        FRAME_SIZE,
                        BufferedImage.TYPE_INT_ARGB
                );
                Graphics2D graphics = frame.createGraphics();
                try {
                    graphics.drawImage(
                            image,
                            0,
                            0,
                            FRAME_SIZE,
                            FRAME_SIZE,
                            0,
                            0,
                            FRAME_SIZE,
                            FRAME_SIZE,
                            null
                    );
                } finally {
                    graphics.dispose();
                }
                planned.put(entry.getValue(), Texture.from(entry.getValue(), frame));
            }
        } catch (IOException | RuntimeException exception) {
            return false;
        }
        planned.forEach(resourcePack.getTextures()::put);
        return true;
    }

    static boolean exactAnimatedSource(Texture texture) throws IOException {
        if (texture == null) {
            return false;
        }
        BufferedImage image = texture.getTextureImage();
        if (image == null
                || image.getWidth() != FRAME_SIZE
                || image.getHeight() != FRAME_SIZE * 2) {
            return false;
        }
        AnimationMeta animation = texture.getAnimation();
        if (animation == null
                || !animation.isInterpolate()
                || animation.getWidth() != 1
                || animation.getHeight() != 1
                || animation.getFrametime() != 1) {
            return false;
        }
        java.util.List<AnimationMeta.FrameMeta> frames = animation.getFrames();
        return frames != null
                && frames.size() == 2
                && frames.get(0).getIndex() == 0
                && frames.get(0).getTime() == 32
                && frames.get(1).getIndex() == 1
                && frames.get(1).getTime() == 4;
    }

    private static Map<Key, Key> staticKeys() {
        LinkedHashMap<Key, Key> result = new LinkedHashMap<>();
        for (AdvancedAeAthenaGeometry.Texture texture
                : AdvancedAeAthenaGeometry.Texture.values()) {
            Key source = Key.parse(texture.textureId());
            result.put(
                    source,
                    Key.parse("bluemap_ae2:m45/athena-frame-zero/" + source.getValue())
            );
        }
        return Map.copyOf(result);
    }
}
