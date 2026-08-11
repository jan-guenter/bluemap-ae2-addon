/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.adapter.bluemap522;

import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.util.Key;
import io.github.janguenter.bluemap.ae2.model.QuartzGlassGeometry;
import io.github.janguenter.bluemap.ae2.profile.Ae219217QuartzGlassProfile;

import java.util.List;
import java.util.Set;

/** Exact operator-installed texture mapping for the AE2 19.2.17 M3c route. */
final class M3cQuartzGlassResourceModels {

    private static final List<Key> BASE_TEXTURES = List.of(
            Key.parse("ae2:block/glass/quartz_glass_a"),
            Key.parse("ae2:block/glass/quartz_glass_b"),
            Key.parse("ae2:block/glass/quartz_glass_c"),
            Key.parse("ae2:block/glass/quartz_glass_d")
    );
    private static final List<Key> FRAME_TEXTURES = java.util.stream.IntStream
            .rangeClosed(1, 15)
            .mapToObj(mask -> Key.parse(String.format(
                    java.util.Locale.ROOT,
                    "ae2:block/glass/quartz_glass_frame%4s",
                    Integer.toBinaryString(mask)
            ).replace(' ', '0')))
            .toList();
    private static final Set<Key> REQUIRED_TEXTURES = Ae219217QuartzGlassProfile.textures()
            .stream()
            .map(Key::parse)
            .collect(java.util.stream.Collectors.toUnmodifiableSet());

    private M3cQuartzGlassResourceModels() {
    }

    static Set<Key> requiredTextures() {
        return REQUIRED_TEXTURES;
    }

    static boolean resourcesSupported(ResourcePack resourcePack) {
        if (resourcePack == null || REQUIRED_TEXTURES.size() != 19) {
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

    static Key texture(QuartzGlassGeometry.Quad quad) {
        return switch (quad.layer()) {
            case BASE -> BASE_TEXTURES.get(quad.textureIndex());
            case FRAME -> FRAME_TEXTURES.get(quad.textureIndex() - 1);
        };
    }
}
