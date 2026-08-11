/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.adapter.bluemap522;

import de.bluecolored.bluemap.core.resources.pack.PackVersion;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.texture.Texture;
import de.bluecolored.bluemap.core.util.Key;
import io.github.janguenter.bluemap.ae2.model.Direction6;
import io.github.janguenter.bluemap.ae2.model.QuantumBridgeGeometry;
import io.github.janguenter.bluemap.ae2.model.QuantumBridgeSnapshot;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class M3eQuantumBridgeResourceModelsTest {

    @Test
    void sixSourceTexturesAreRequiredButStaticOffEmitsOnlyFour() throws Exception {
        ResourcePack empty = new ResourcePack(new PackVersion(34, 0));
        assertEquals(6, M3eQuantumBridgeResourceModels.requiredTextures().size());
        assertEquals(4, M3eQuantumBridgeResourceModels.emittedTextures().size());
        assertTrue(M3eQuantumBridgeResourceModels.exactRouteContractAvailable());
        assertFalse(M3eQuantumBridgeResourceModels.resourcesSupported(empty));

        ResourcePack exact = exactResources();
        assertTrue(M3eQuantumBridgeResourceModels.resourcesSupported(exact));
        exact.getTextures().remove(Key.parse(
                M3eQuantumBridgeResourceModels.RING_LIGHT_TEXTURE
        ));
        assertFalse(M3eQuantumBridgeResourceModels.resourcesSupported(exact));
    }

    @Test
    void everyNeutralLayerMapsToOneOfFourExactOffTextures() {
        Map<QuantumBridgeGeometry.Layer, String> expected = new EnumMap<>(
                QuantumBridgeGeometry.Layer.class
        );
        expected.put(
                QuantumBridgeGeometry.Layer.LINK,
                M3eQuantumBridgeResourceModels.LINK_TEXTURE
        );
        expected.put(
                QuantumBridgeGeometry.Layer.RING,
                M3eQuantumBridgeResourceModels.RING_TEXTURE
        );
        expected.put(
                QuantumBridgeGeometry.Layer.GLASS,
                M3eQuantumBridgeResourceModels.GLASS_TEXTURE
        );
        expected.put(
                QuantumBridgeGeometry.Layer.COVERED,
                M3eQuantumBridgeResourceModels.COVERED_TEXTURE
        );

        Map<QuantumBridgeGeometry.Layer, QuantumBridgeGeometry.Quad> examples =
                new EnumMap<>(QuantumBridgeGeometry.Layer.class);
        for (QuantumBridgeSnapshot snapshot : Set.of(
                new QuantumBridgeSnapshot(
                        QuantumBridgeSnapshot.Role.LINK,
                        Set.of(
                                Direction6.NORTH,
                                Direction6.SOUTH,
                                Direction6.WEST,
                                Direction6.EAST
                        ),
                        false
                ),
                new QuantumBridgeSnapshot(
                        QuantumBridgeSnapshot.Role.EDGE_RING,
                        Set.of(Direction6.EAST, Direction6.WEST, Direction6.UP),
                        false
                )
        )) {
            for (QuantumBridgeGeometry.Quad quad
                    : QuantumBridgeGeometry.forSnapshot(snapshot)) {
                examples.putIfAbsent(quad.layer(), quad);
            }
        }
        assertEquals(expected.keySet(), examples.keySet());
        for (Map.Entry<QuantumBridgeGeometry.Layer, String> entry : expected.entrySet()) {
            assertEquals(
                    entry.getValue(),
                    M3eQuantumBridgeResourceModels.texture(examples.get(entry.getKey()))
                            .getFormatted()
            );
        }
        assertFalse(M3eQuantumBridgeResourceModels.emittedTextures().contains(
                Key.parse(M3eQuantumBridgeResourceModels.RING_LIGHT_TEXTURE)
        ));
        assertFalse(M3eQuantumBridgeResourceModels.emittedTextures().contains(
                Key.parse(M3eQuantumBridgeResourceModels.RING_LIGHT_CORNER_TEXTURE)
        ));
    }

    static ResourcePack exactResources() throws IOException {
        ResourcePack resourcePack = new ResourcePack(new PackVersion(34, 0));
        int color = 0xFF102030;
        for (Key texture : M3eQuantumBridgeResourceModels.requiredTextures()) {
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
