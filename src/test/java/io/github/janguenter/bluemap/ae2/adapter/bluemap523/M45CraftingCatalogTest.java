/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.adapter.bluemap523;

import de.bluecolored.bluemap.core.util.Key;
import de.bluecolored.bluemap.core.world.BlockState;
import io.github.janguenter.bluemap.ae2.model.CraftingBlockKind;
import io.github.janguenter.bluemap.ae2.model.CraftingGeometry;
import io.github.janguenter.bluemap.ae2.profile.expandedae.ExpandedAe211Catalog;
import io.github.janguenter.bluemap.ae2.profile.megacells.MegaCells4110Profile;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class M45CraftingCatalogTest {

    @Test
    void exposesTwentyNineDisjointExtensionCraftingBlocks() {
        assertEquals(29, M45CraftingCatalog.extensionBlocks().size());
        assertEquals(
                21,
                M45CraftingCatalog.extensionBlocks().stream()
                        .filter(id -> M45Runtime.EXPANDED_AE.equals(
                                M45CraftingCatalog.route(id)
                        ))
                        .count()
        );
        assertEquals(
                8,
                M45CraftingCatalog.extensionBlocks().stream()
                        .filter(id -> M45Runtime.MEGA_CELLS.equals(
                                M45CraftingCatalog.route(id)
                        ))
                        .count()
        );
    }

    @Test
    void requiresExactStateDomains() {
        assertTrue(M45CraftingCatalog.isExactState(state(
                ExpandedAe211Catalog.CRAFTING_UNIT_BLOCK,
                Map.of("formed", "true", "powered", "false")
        )));
        assertTrue(M45CraftingCatalog.isExactState(state(
                MegaCells4110Profile.CRAFTING_MONITOR,
                Map.of(
                        "formed", "true", "powered", "true",
                        "facing", "south", "spin", "3"
                )
        )));
        assertFalse(M45CraftingCatalog.isExactState(state(
                MegaCells4110Profile.CRAFTING_MONITOR,
                Map.of("formed", "true", "powered", "true")
        )));
        assertFalse(M45CraftingCatalog.isExactState(state(
                ExpandedAe211Catalog.CRAFTING_UNIT_BLOCK,
                Map.of("formed", "yes", "powered", "false")
        )));
    }

    @Test
    void bindsEveryReachableLayerToTheOwningTextureFamily() {
        String expandedUnit = ExpandedAe211Catalog.CRAFTING_UNIT_BLOCK;
        String expandedAccelerator = ExpandedAe211Catalog.craftingDefinitions().values()
                .stream()
                .filter(definition -> definition.kind() == CraftingBlockKind.ACCELERATOR)
                .findFirst()
                .orElseThrow()
                .blockId();
        assertEquals(
                "expandedae:block/crafting/unit_base",
                M45CraftingCatalog.texture(expandedUnit, quad(CraftingGeometry.Layer.UNIT_BASE))
                        .getFormatted()
        );
        assertTrue(
                M45CraftingCatalog.texture(
                        expandedAccelerator,
                        quad(CraftingGeometry.Layer.ACCELERATOR_LIGHT)
                ).getFormatted().startsWith("expandedae:block/crafting/")
        );
        for (CraftingGeometry.Layer layer : CraftingGeometry.Layer.values()) {
            assertNotNull(M45CraftingCatalog.texture(
                    MegaCells4110Profile.CRAFTING_MONITOR,
                    quad(layer)
            ));
        }
        assertNull(M45CraftingCatalog.texture("minecraft:stone", quad(
                CraftingGeometry.Layer.UNIT_BASE
        )));
    }

    private static BlockState state(String id, Map<String, String> properties) {
        return new BlockState(Key.parse(id), properties);
    }

    private static CraftingGeometry.Quad quad(CraftingGeometry.Layer layer) {
        return new CraftingGeometry.Quad(
                io.github.janguenter.bluemap.ae2.model.Direction6.UP,
                layer,
                java.util.List.of(
                        new CraftingGeometry.Vertex(0, 16, 0, 0, 0),
                        new CraftingGeometry.Vertex(0, 16, 16, 0, 16),
                        new CraftingGeometry.Vertex(16, 16, 16, 16, 16),
                        new CraftingGeometry.Vertex(16, 16, 0, 16, 0)
                )
        );
    }
}
