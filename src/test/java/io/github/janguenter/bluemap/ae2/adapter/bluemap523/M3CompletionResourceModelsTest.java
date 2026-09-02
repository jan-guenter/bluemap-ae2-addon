/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.adapter.bluemap523;

import com.flowpowered.math.vector.Vector3f;
import com.flowpowered.math.vector.Vector4f;
import de.bluecolored.bluemap.core.resources.ResourcePath;
import de.bluecolored.bluemap.core.resources.pack.PackVersion;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.model.Element;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.model.Face;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.model.Model;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.model.TextureVariable;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.texture.Texture;
import de.bluecolored.bluemap.core.util.Direction;
import de.bluecolored.bluemap.core.util.Key;
import io.github.janguenter.bluemap.ae2.model.Direction6;
import io.github.janguenter.bluemap.ae2.model.PaintGeometry;
import io.github.janguenter.bluemap.ae2.model.SpatialPylonGeometry;
import io.github.janguenter.bluemap.ae2.model.SpatialPylonSnapshot;
import io.github.janguenter.bluemap.ae2.profile.Ae219217M3CompletionProfile;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class M3CompletionResourceModelsTest {

    @Test
    void exactClosureAndEveryPrimitiveTextureMappingAreClosed() throws Exception {
        ResourcePack resourcePack = exactResources();

        assertTrue(M3CompletionResourceModels.exactRouteContractAvailable());
        assertEquals(19, M3CompletionResourceModels.requiredTextures().size());
        assertEquals(
                Map.of(
                        "ae2:block/crank_base",
                        "2e310bd6c9f4c3fd3a454e2fd47213435508362d48b1f8b42f65719d681da652",
                        "ae2:block/crank_handle",
                        "e68bcd98509270ad63c3a5a032499d75f41c519afdab91952fef7bb0b3af350a",
                        "ae2:block/inscriber",
                        "388edb63f7a601b170953cb20822ce96a39412e90b016d7aec461bf5bacdde7d"
                ),
                M3CompletionResourceModels.expectedModelSignatures()
        );
        assertTrue(M3CompletionResourceModels.requiredTextures().containsAll(
                Ae219217M3CompletionProfile.fallbackOnlyTextures().stream()
                        .map(Key::parse)
                        .toList()
        ));
        assertTrue(M3CompletionResourceModels.resourcesSupported(resourcePack));
        assertEquals(
                Ae219217M3CompletionProfile.SKY_CHEST_TEXTURE,
                M3CompletionResourceModels.chestTexture(
                        Ae219217M3CompletionProfile.SKY_STONE_CHEST_BLOCK
                ).getFormatted()
        );
        assertEquals(
                Ae219217M3CompletionProfile.SKY_BLOCK_CHEST_TEXTURE,
                M3CompletionResourceModels.chestTexture(
                        Ae219217M3CompletionProfile.SMOOTH_SKY_STONE_CHEST_BLOCK
                ).getFormatted()
        );

        List<String> paintTextures = new ArrayList<>();
        for (int seed = 0; seed < 3; seed++) {
            PaintGeometry.Vertex vertex = new PaintGeometry.Vertex(0, 0, 0, 0, 0);
            PaintGeometry.Quad quad = new PaintGeometry.Quad(
                    Direction6.UP,
                    seed,
                    0xffffff,
                    false,
                    List.of(vertex, vertex, vertex, vertex)
            );
            paintTextures.add(
                    M3CompletionResourceModels.paintTexture(quad).getFormatted()
            );
        }
        assertEquals(List.of(
                Ae219217M3CompletionProfile.PAINT1_TEXTURE,
                Ae219217M3CompletionProfile.PAINT2_TEXTURE,
                Ae219217M3CompletionProfile.PAINT3_TEXTURE
        ), paintTextures);

        for (SpatialPylonSnapshot snapshot : List.of(
                new SpatialPylonSnapshot(
                        SpatialPylonSnapshot.Axis.X,
                        SpatialPylonSnapshot.AxisPosition.NONE
                ),
                new SpatialPylonSnapshot(
                        SpatialPylonSnapshot.Axis.X,
                        SpatialPylonSnapshot.AxisPosition.START
                ),
                new SpatialPylonSnapshot(
                        SpatialPylonSnapshot.Axis.Y,
                        SpatialPylonSnapshot.AxisPosition.MIDDLE
                ),
                new SpatialPylonSnapshot(
                        SpatialPylonSnapshot.Axis.Z,
                        SpatialPylonSnapshot.AxisPosition.END
                )
        )) {
            for (SpatialPylonGeometry.Quad quad
                    : SpatialPylonGeometry.forSnapshot(snapshot)) {
                assertTrue(M3CompletionResourceModels.requiredTextures().contains(
                        M3CompletionResourceModels.pylonTexture(quad)
                ));
            }
        }
    }

    @Test
    void rejectsMissingTexturesAndSameCountSemanticModelDrift()
            throws Exception {
        ResourcePack missingTexture = exactResources();
        missingTexture.getTextures().remove(M3CompletionResourceModels.CRANK_TEXTURE);
        assertFalse(M3CompletionResourceModels.resourcesSupported(missingTexture));

        ResourcePack countOnlyModel = exactResources();
        countOnlyModel.getModels().put(
                M3CompletionResourceModels.CRANK_HANDLE,
                model(M3CompletionResourceModels.CRANK_TEXTURE, 6, 6)
        );
        assertFalse(M3CompletionResourceModels.resourcesSupported(countOnlyModel));

        assertCrankBaseMutationRejected(
                ExactM3CompletionModelFixtures.crankBaseGeometryDrift()
        );
        assertCrankBaseMutationRejected(
                ExactM3CompletionModelFixtures.crankBaseUvDrift()
        );
        assertCrankBaseMutationRejected(
                ExactM3CompletionModelFixtures.crankBaseTextureDrift()
        );
        assertCrankBaseMutationRejected(
                ExactM3CompletionModelFixtures.crankBaseAmbientOcclusionDrift()
        );
    }

    static ResourcePack exactResources() throws IOException {
        ResourcePack resourcePack = new ResourcePack(new PackVersion(34, 0));
        int color = 0xff203040;
        for (Key texture : M3CompletionResourceModels.requiredTextures()) {
            putTexture(resourcePack, texture, color++);
        }
        ExactM3CompletionModelFixtures.install(resourcePack);
        return resourcePack;
    }

    private static void assertCrankBaseMutationRejected(Model mutation)
            throws Exception {
        ResourcePack resourcePack = exactResources();
        Model canonical = resourcePack.getModels().get(
                M3CompletionResourceModels.CRANK_BASE
        );
        assertEquals(canonical.getElements().length, mutation.getElements().length);
        assertEquals(
                faceCount(canonical),
                faceCount(mutation),
                "semantic drift fixture must preserve the face count"
        );
        resourcePack.getModels().put(
                M3CompletionResourceModels.CRANK_BASE,
                mutation
        );
        assertFalse(M3CompletionResourceModels.resourcesSupported(resourcePack));
    }

    private static int faceCount(Model model) {
        return java.util.Arrays.stream(model.getElements())
                .mapToInt(element -> element.getFaces().size())
                .sum();
    }

    static void putTexture(ResourcePack resourcePack, Key key, int argb)
            throws IOException {
        BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        image.setRGB(0, 0, argb);
        resourcePack.getTextures().put(key, Texture.from(key, image));
    }

    private static Model model(Key texture, int... faceCounts) {
        Element[] elements = new Element[faceCounts.length];
        for (int index = 0; index < elements.length; index++) {
            EnumMap<Direction, Face> faces = new EnumMap<>(Direction.class);
            for (int face = 0; face < faceCounts[index]; face++) {
                Direction direction = Direction.values()[face];
                faces.put(direction, new Face(
                        new Vector4f(0F, 0F, 16F, 16F),
                        new TextureVariable(new ResourcePath<Texture>(texture)),
                        null,
                        0,
                        -1
                ));
            }
            elements[index] = new Element(
                    Vector3f.ZERO,
                    new Vector3f(16F, 16F, 16F),
                    faces
            );
        }
        return new Model(elements);
    }
}
