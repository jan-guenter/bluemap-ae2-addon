/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.adapter.bluemap523;

import de.bluecolored.bluemap.core.resources.ResourcePath;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.model.Model;
import de.bluecolored.bluemap.core.util.Key;
import io.github.janguenter.bluemap.ae2.model.PaintGeometry;
import io.github.janguenter.bluemap.ae2.model.SpatialPylonGeometry;
import io.github.janguenter.bluemap.ae2.profile.Ae219217M3CompletionProfile;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/** Exact operator-installed resources consumed by the combined M3 route. */
final class M3CompletionResourceModels {

    static final ResourcePath<Model> CRANK_BASE = model("ae2:block/crank_base");
    static final ResourcePath<Model> CRANK_HANDLE = model("ae2:block/crank_handle");
    static final ResourcePath<Model> INSCRIBER = model("ae2:block/inscriber");

    static final Key CRANK_TEXTURE = Key.parse(Ae219217M3CompletionProfile.CRANK_TEXTURE);
    static final Key INSCRIBER_TEXTURE = Key.parse(
            Ae219217M3CompletionProfile.INSCRIBER_TEXTURE
    );
    static final Key INSCRIBER_INSIDE_TEXTURE = Key.parse(
            Ae219217M3CompletionProfile.INSCRIBER_INSIDE_TEXTURE
    );
    static final Key SKY_CHEST_TEXTURE = Key.parse(
            Ae219217M3CompletionProfile.SKY_CHEST_TEXTURE
    );
    static final Key SKY_BLOCK_CHEST_TEXTURE = Key.parse(
            Ae219217M3CompletionProfile.SKY_BLOCK_CHEST_TEXTURE
    );

    private static final List<Key> PAINT_TEXTURES = List.of(
            Key.parse(Ae219217M3CompletionProfile.PAINT1_TEXTURE),
            Key.parse(Ae219217M3CompletionProfile.PAINT2_TEXTURE),
            Key.parse(Ae219217M3CompletionProfile.PAINT3_TEXTURE)
    );
    private static final Map<SpatialPylonGeometry.Texture, Key> PYLON_TEXTURES = Map.ofEntries(
            Map.entry(SpatialPylonGeometry.Texture.BASE,
                    Key.parse(Ae219217M3CompletionProfile.PYLON_BASE_TEXTURE)),
            Map.entry(SpatialPylonGeometry.Texture.BASE_END,
                    Key.parse(Ae219217M3CompletionProfile.PYLON_BASE_END_TEXTURE)),
            Map.entry(SpatialPylonGeometry.Texture.BASE_SPANNED,
                    Key.parse(Ae219217M3CompletionProfile.PYLON_BASE_SPANNED_TEXTURE)),
            Map.entry(SpatialPylonGeometry.Texture.DIM,
                    Key.parse(Ae219217M3CompletionProfile.PYLON_DIM_TEXTURE)),
            Map.entry(SpatialPylonGeometry.Texture.DIM_END,
                    Key.parse(Ae219217M3CompletionProfile.PYLON_DIM_END_TEXTURE)),
            Map.entry(SpatialPylonGeometry.Texture.DIM_SPANNED,
                    Key.parse(Ae219217M3CompletionProfile.PYLON_DIM_SPANNED_TEXTURE)),
            Map.entry(SpatialPylonGeometry.Texture.RED,
                    Key.parse(Ae219217M3CompletionProfile.PYLON_RED_TEXTURE)),
            Map.entry(SpatialPylonGeometry.Texture.RED_END,
                    Key.parse(Ae219217M3CompletionProfile.PYLON_RED_END_TEXTURE)),
            Map.entry(SpatialPylonGeometry.Texture.RED_SPANNED,
                    Key.parse(Ae219217M3CompletionProfile.PYLON_RED_SPANNED_TEXTURE))
    );
    private static final Set<Key> REQUIRED_TEXTURES = requiredTextureClosure();
    private static final Map<String, String> EXPECTED_MODEL_SIGNATURES = Map.of(
            CRANK_BASE.getFormatted(),
            "2e310bd6c9f4c3fd3a454e2fd47213435508362d48b1f8b42f65719d681da652",
            CRANK_HANDLE.getFormatted(),
            "e68bcd98509270ad63c3a5a032499d75f41c519afdab91952fef7bb0b3af350a",
            INSCRIBER.getFormatted(),
            "388edb63f7a601b170953cb20822ce96a39412e90b016d7aec461bf5bacdde7d"
    );

    private M3CompletionResourceModels() {
    }

    static ResourcePath<Model> model(String path) {
        return new ResourcePath<>(path);
    }

    static Set<Key> requiredTextures() {
        return REQUIRED_TEXTURES;
    }

    static Map<String, String> expectedModelSignatures() {
        return EXPECTED_MODEL_SIGNATURES;
    }

    static Key paintTexture(PaintGeometry.Quad quad) {
        return PAINT_TEXTURES.get(quad.textureIndex());
    }

    static Key pylonTexture(SpatialPylonGeometry.Quad quad) {
        Key texture = PYLON_TEXTURES.get(quad.texture());
        if (texture == null) {
            throw new IllegalArgumentException("unsupported spatial-pylon texture");
        }
        return texture;
    }

    static Key chestTexture(String blockId) {
        if (Ae219217M3CompletionProfile.SKY_STONE_CHEST_BLOCK.equals(blockId)) {
            return SKY_CHEST_TEXTURE;
        }
        if (Ae219217M3CompletionProfile.SMOOTH_SKY_STONE_CHEST_BLOCK.equals(blockId)) {
            return SKY_BLOCK_CHEST_TEXTURE;
        }
        throw new IllegalArgumentException("unsupported Sky Stone chest block");
    }

    static boolean exactRouteContractAvailable() {
        return Ae219217M3CompletionProfile.BLOCKS.size() == 6
                && Ae219217M3CompletionProfile.SYNTHETIC_BLOCK_STATES.size() == 6
                && Ae219217M3CompletionProfile.requiredResources().size() == 33
                && Ae219217M3CompletionProfile.requiredResourceSizes().size() == 33
                && Ae219217M3CompletionProfile.sourceTextures().size() == 17
                && Ae219217M3CompletionProfile.fallbackOnlyTextures().size() == 2
                && REQUIRED_TEXTURES.size() == 19
                && PAINT_TEXTURES.size() == 3
                && PYLON_TEXTURES.size() == SpatialPylonGeometry.Texture.values().length;
    }

    static boolean resourcesSupported(ResourcePack resourcePack) {
        if (resourcePack == null || !exactRouteContractAvailable()) {
            return false;
        }
        for (Key texture : REQUIRED_TEXTURES) {
            if (ResourcePack.MISSING_TEXTURE.equals(texture)
                    || resourcePack.getTextures().get(texture) == null) {
                return false;
            }
        }
        return exactModel(resourcePack, CRANK_BASE)
                && exactModel(resourcePack, CRANK_HANDLE)
                && exactModel(resourcePack, INSCRIBER);
    }

    private static boolean exactModel(
            ResourcePack resourcePack,
            ResourcePath<Model> path
    ) {
        Model model = resourcePack.getModels().get(path);
        String expected = EXPECTED_MODEL_SIGNATURES.get(path.getFormatted());
        return model != null
                && expected != null
                && model.getParent() == null
                && model.getElements() != null
                && expected.equals(M3DriveResourceModels.semanticSignature(model));
    }

    private static Set<Key> requiredTextureClosure() {
        return java.util.stream.Stream.concat(
                        Ae219217M3CompletionProfile.sourceTextures().stream(),
                        Ae219217M3CompletionProfile.fallbackOnlyTextures().stream()
                )
                .map(Key::parse)
                .collect(Collectors.toUnmodifiableSet());
    }
}
