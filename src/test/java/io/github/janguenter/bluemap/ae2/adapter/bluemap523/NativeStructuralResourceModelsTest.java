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
import de.bluecolored.bluemap.core.resources.pack.resourcepack.texture.AnimationMeta;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.texture.Texture;
import de.bluecolored.bluemap.core.util.Direction;
import de.bluecolored.bluemap.core.util.Key;
import io.github.janguenter.bluemap.ae2.model.NativeStructuralPartCatalog;
import io.github.janguenter.bluemap.ae2.profile.Ae219217NativeStructuralProfile;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NativeStructuralResourceModelsTest {

    @Test
    void writesExactParsedSemanticSignaturesWhenOptedIn() throws Exception {
        String output = System.getenv("AE2_S1_WRITE_SEMANTIC_SIGNATURES");
        Assumptions.assumeTrue(output != null && !output.isBlank());

        Path ae2Main = Path.of(
                "/root/work/allthemons/research/ae2-19.2.17/src/main/resources"
        );
        Path ae2Generated = Path.of(
                "/root/work/allthemons/research/ae2-19.2.17/src/generated/resources"
        );
        ResourcePack pack = new ResourcePack(new PackVersion(34, 0));
        pack.loadResources(List.of(
                Path.of("/root/work/allthemons/bluemap-backport/core/src/main/resourceExtensions"),
                ae2Main,
                ae2Generated,
                Path.of("/root/.gradle/caches/neoformruntime/artifacts/"
                        + "minecraft_1.21.1_client.jar")
        ));
        java.util.LinkedHashSet<Key> probedTextures = new java.util.LinkedHashSet<>(
                NativeStructuralResourceModels.requiredTextures()
        );
        probedTextures.addAll(M3cQuartzGlassResourceModels.requiredTextures());
        for (Key key : probedTextures) {
            if (pack.getTextures().get(key) != null) {
                continue;
            }
            Path relative = Path.of("assets", key.getNamespace(), "textures",
                    key.getValue() + ".png");
            Path png = Files.exists(ae2Generated.resolve(relative))
                    ? ae2Generated.resolve(relative) : ae2Main.resolve(relative);
            BufferedImage image = ImageIO.read(png.toFile());
            assertNotNull(image, png.toString());
            pack.getTextures().put(key, Texture.from(key, image));
        }

        NativeStructuralSemanticResources.Signatures signatures =
                NativeStructuralSemanticResources.signatures(pack);
        String quartzFacadeTextures = NativeStructuralSemanticResources.textureSignature(
                pack,
                M3cQuartzGlassResourceModels.requiredTextures()
        );
        assertNotNull(signatures);
        assertNotNull(quartzFacadeTextures);
        assertTrue(NativeStructuralResourceModels.resourcesSupported(pack));
        Files.writeString(
                Path.of(output),
                "models=" + signatures.models() + "\n"
                        + "textures=" + signatures.textures() + "\n"
                        + "facadeQuartzTextures=" + quartzFacadeTextures + "\n",
                StandardCharsets.UTF_8
        );
    }

    @Test
    void exact99ResourceSemanticClosureIsBoundDirectly() throws Exception {
        assertTrue(NativeStructuralResourceModels.exactRouteContractAvailable());
        assertEquals(43, Ae219217NativeStructuralProfile.TRANSITIVE_JSON_RESOURCE_COUNT);
        assertEquals(56, NativeStructuralResourceModels.requiredTextures().size());
        assertEquals(99, Ae219217NativeStructuralProfile.requiredResources().size());

        ResourcePack resources = exactResources();
        NativeStructuralSemanticResources.Signatures expected =
                NativeStructuralSemanticResources.signatures(resources);
        assertNotNull(expected);
        assertTrue(NativeStructuralSemanticResources.supports(resources, expected));
        Key missing = NativeStructuralResourceModels.requiredTextures().iterator().next();
        resources.getTextures().remove(missing);
        assertFalse(NativeStructuralSemanticResources.supports(resources, expected));
    }

    @Test
    void sameCountModelPixelAndAnimationDriftAreRejected() throws Exception {
        ResourcePack baseline = exactResources();
        NativeStructuralSemanticResources.Signatures expected =
                NativeStructuralSemanticResources.signatures(baseline);
        assertNotNull(expected);

        ResourcePack modelDrift = exactResources();
        String modelPath = NativeStructuralSemanticResources.requiredModels().getFirst();
        Key texture = NativeStructuralResourceModels.requiredTextures().iterator().next();
        modelDrift.getModels().put(Key.parse(modelPath), cube(texture, 15F));
        assertFalse(NativeStructuralSemanticResources.supports(modelDrift, expected));

        ResourcePack pixelDrift = exactResources();
        pixelDrift.getTextures().put(texture, texture(texture, 0xff000000, null));
        assertFalse(NativeStructuralSemanticResources.supports(pixelDrift, expected));

        ResourcePack animationDrift = exactResources();
        animationDrift.getTextures().put(texture, texture(
                texture,
                0xffffffff,
                new AnimationMeta(false, 1, 1, 2, null)
        ));
        assertFalse(NativeStructuralSemanticResources.supports(animationDrift, expected));
    }

    @Test
    void quartzFacadeDependencyRejectsSameDimensionPixelAndAnimationDrift()
            throws Exception {
        ResourcePack baseline = exactResources();
        for (Key key : M3cQuartzGlassResourceModels.requiredTextures()) {
            baseline.getTextures().put(key, texture(key, 0xffffffff, null));
        }
        String expected = NativeStructuralSemanticResources.textureSignature(
                baseline,
                M3cQuartzGlassResourceModels.requiredTextures()
        );
        assertNotNull(expected);

        Key changed = M3cQuartzGlassResourceModels.requiredTextures().iterator().next();
        baseline.getTextures().put(changed, texture(changed, 0xff010101, null));
        assertFalse(expected.equals(NativeStructuralSemanticResources.textureSignature(
                baseline,
                M3cQuartzGlassResourceModels.requiredTextures()
        )));
        baseline.getTextures().put(changed, texture(
                changed,
                0xffffffff,
                new AnimationMeta(true, 1, 1, 3, null)
        ));
        assertFalse(expected.equals(NativeStructuralSemanticResources.textureSignature(
                baseline,
                M3cQuartzGlassResourceModels.requiredTextures()
        )));
    }

    @Test
    void selectedLayersAreExactStaticOffVariants() {
        assertEquals(
                java.util.List.of("ae2:part/cable_anchor"),
                NativeStructuralResourceModels.renderedModelPaths(
                        NativeStructuralPartCatalog.require("ae2:cable_anchor"),
                        false
                )
        );
        assertEquals(
                java.util.List.of("ae2:part/cable_anchor_short"),
                NativeStructuralResourceModels.renderedModelPaths(
                        NativeStructuralPartCatalog.require("ae2:cable_anchor"),
                        true
                )
        );
        assertEquals(
                java.util.List.of("ae2:part/transition_plane_off"),
                NativeStructuralResourceModels.renderedModelPaths(
                        NativeStructuralPartCatalog.require("ae2:formation_plane"),
                        false
                )
        );
        assertEquals(
                java.util.List.of(
                        "ae2:part/p2p/p2p_tunnel_status_off",
                        "ae2:part/p2p/p2p_tunnel_me"
                ),
                NativeStructuralResourceModels.renderedModelPaths(
                        NativeStructuralPartCatalog.require("ae2:me_p2p_tunnel"),
                        false
                )
        );
    }

    private static ResourcePack exactResources() throws IOException {
        ResourcePack pack = new ResourcePack(new PackVersion(34, 0));
        for (Key texture : NativeStructuralResourceModels.requiredTextures()) {
            pack.getTextures().put(texture, texture(texture, 0xffffffff, null));
        }
        Key texture = NativeStructuralResourceModels.requiredTextures().iterator().next();
        for (String path : NativeStructuralSemanticResources.requiredModels()) {
            pack.getModels().put(Key.parse(path), cube(texture));
        }
        return pack;
    }

    private static Model cube(Key texture) {
        return cube(texture, 16F);
    }

    private static Model cube(Key texture, float extent) {
        EnumMap<Direction, Face> faces = new EnumMap<>(Direction.class);
        for (Direction direction : Direction.values()) {
            faces.put(direction, new Face(
                    new Vector4f(0, 0, 16, 16),
                    new TextureVariable(new ResourcePath<Texture>(texture)),
                    direction,
                    0,
                    -1
            ));
        }
        return new Model(
                Map.of(),
                new Element[]{new Element(
                        Vector3f.ZERO,
                        new Vector3f(extent, 16, 16),
                        faces
                )},
                true
        );
    }

    private static Texture texture(
            Key key,
            int argb,
            AnimationMeta animation
    ) throws IOException {
        BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        image.setRGB(0, 0, argb);
        return Texture.from(key, image, animation);
    }
}
