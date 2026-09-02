/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.adapter.bluemap523;

import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.util.Key;
import io.github.janguenter.bluemap.ae2.model.QuantumBridgeGeometry;
import io.github.janguenter.bluemap.ae2.profile.Ae219217QuantumBridgeProfile;

import java.util.List;
import java.util.Map;
import java.util.Set;

/** Exact operator-installed texture mapping for the AE2 19.2.17 M3e route. */
final class M3eQuantumBridgeResourceModels {

    static final String PROFILE_ID = Ae219217QuantumBridgeProfile.PROFILE_ID;
    static final String LINK_BLOCK = Ae219217QuantumBridgeProfile.QUANTUM_LINK_BLOCK;
    static final String RING_BLOCK = Ae219217QuantumBridgeProfile.QUANTUM_RING_BLOCK;
    static final String SYNTHETIC_BLOCK_STATE =
            Ae219217QuantumBridgeProfile.SYNTHETIC_BLOCK_STATE;
    static final String BLOCK_ENTITY_ID = Ae219217QuantumBridgeProfile.BLOCK_ENTITY_ID;

    static final String LINK_TEXTURE = Ae219217QuantumBridgeProfile.QUANTUM_LINK_TEXTURE;
    static final String RING_TEXTURE = Ae219217QuantumBridgeProfile.QUANTUM_RING_TEXTURE;
    static final String RING_LIGHT_TEXTURE =
            Ae219217QuantumBridgeProfile.QUANTUM_RING_LIGHT_TEXTURE;
    static final String RING_LIGHT_CORNER_TEXTURE =
            Ae219217QuantumBridgeProfile.QUANTUM_RING_LIGHT_CORNER_TEXTURE;
    static final String GLASS_TEXTURE =
            Ae219217QuantumBridgeProfile.GLASS_TRANSPARENT_TEXTURE;
    static final String COVERED_TEXTURE =
            Ae219217QuantumBridgeProfile.COVERED_TRANSPARENT_TEXTURE;

    private static final Map<QuantumBridgeGeometry.Layer, Key> EMITTED_TEXTURES = Map.of(
            QuantumBridgeGeometry.Layer.LINK, Key.parse(LINK_TEXTURE),
            QuantumBridgeGeometry.Layer.RING, Key.parse(RING_TEXTURE),
            QuantumBridgeGeometry.Layer.GLASS, Key.parse(GLASS_TEXTURE),
            QuantumBridgeGeometry.Layer.COVERED, Key.parse(COVERED_TEXTURE)
    );
    private static final Set<Key> REQUIRED_TEXTURES = Set.of(
            Key.parse(LINK_TEXTURE),
            Key.parse(RING_TEXTURE),
            Key.parse(RING_LIGHT_TEXTURE),
            Key.parse(RING_LIGHT_CORNER_TEXTURE),
            Key.parse(GLASS_TEXTURE),
            Key.parse(COVERED_TEXTURE)
    );

    private M3eQuantumBridgeResourceModels() {
    }

    static Set<Key> requiredTextures() {
        return REQUIRED_TEXTURES;
    }

    static Set<Key> emittedTextures() {
        return Set.copyOf(EMITTED_TEXTURES.values());
    }

    static boolean exactRouteContractAvailable() {
        Set<String> exactBlocks = Set.of(LINK_BLOCK, RING_BLOCK);
        List<String> sourceTextures = Ae219217QuantumBridgeProfile.sourceTextures();
        List<String> emittedOffTextures =
                Ae219217QuantumBridgeProfile.emittedOffTextures();
        return Ae219217QuantumBridgeProfile.requiredResources().size() == 13
                && Ae219217QuantumBridgeProfile.requiredResourceSizes().size() == 13
                && Ae219217QuantumBridgeProfile.BLOCKS.equals(exactBlocks)
                && sourceTextures.size() == 6
                && sourceTextures.stream().map(Key::parse).collect(
                        java.util.stream.Collectors.toUnmodifiableSet()
                ).equals(REQUIRED_TEXTURES)
                && emittedOffTextures.size() == 4
                && emittedOffTextures.stream().map(Key::parse).collect(
                        java.util.stream.Collectors.toUnmodifiableSet()
                ).equals(emittedTextures())
                && EMITTED_TEXTURES.size() == QuantumBridgeGeometry.Layer.values().length
                && !emittedTextures().contains(Key.parse(RING_LIGHT_TEXTURE))
                && !emittedTextures().contains(Key.parse(RING_LIGHT_CORNER_TEXTURE));
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
        return true;
    }

    static Key texture(QuantumBridgeGeometry.Quad quad) {
        Key texture = EMITTED_TEXTURES.get(quad.layer());
        if (texture == null) {
            throw new IllegalArgumentException(
                    "unsupported quantum-bridge layer " + quad.layer()
            );
        }
        return texture;
    }
}
