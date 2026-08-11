/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.adapter.bluemap522;

import de.bluecolored.bluemap.core.map.hires.block.BlockRendererType;
import de.bluecolored.bluemap.core.util.Key;
import de.bluecolored.bluemap.core.util.Registry;
import de.bluecolored.bluemap.core.world.mca.blockentity.BlockEntityType;
import io.github.janguenter.bluemap.ae2.activation.M3CompletionRouteActivation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class M3CompletionAdapterIntegrationTest {

    @Test
    void installOwnsPaintDtoBeforeTheDurableRetentionProbe() {
        assertTrue(BlueMap522Adapter.install());

        BlockEntityType paint = BlockEntityType.REGISTRY.get(Key.parse("ae2:paint"));
        assertEquals(Ae2PaintBlockEntityData.class, paint.getBlockEntityClass());
        assertFalse(BlueMap522Adapter.m3CompletionActivationForTesting().isActive());
        assertEquals(
                "awaiting-exact-ae2-m3-completion-profile",
                BlueMap522Adapter.m3CompletionActivationForTesting().reason()
        );
        assertTrue(BlueMap522Adapter.probeM3CompletionPaintRetention());
    }

    @Test
    void sharedRendererCollisionDisablesOnlyTheCombinedCandidateRoute() {
        Registry<BlockRendererType> registry = new Registry<>();
        BlockRendererType collision = new BlockRendererType.Impl(
                Key.parse("bluemap_ae2:m3_completion"),
                (resourcePack, textureGallery, renderSettings) -> null
        );
        assertTrue(BlueMap522Adapter.registerExact(registry, collision));

        M3CompletionRouteActivation activation = new M3CompletionRouteActivation();
        activation.activate();
        assertFalse(BlueMap522Adapter.registerM3CompletionRendererExact(
                registry,
                activation
        ));
        assertTrue(activation.isDisabled());
        assertEquals("m3-completion-registry-collision", activation.reason());
    }
}
