/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.profile.expandedae;

import io.github.janguenter.bluemap.ae2.model.CraftingBlockKind;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExpandedAe211ProfileTest {

    @Test
    void exactArtifactAndCompleteVisualPartitionAreLocked() {
        assertEquals(142, ExpandedAe211Profile.requiredResources().size());
        assertEquals(
                46_365L,
                ExpandedAe211Profile.requiredResourceSizes().values().stream()
                        .mapToLong(Long::longValue)
                        .sum()
        );
        assertEquals(
                23,
                ExpandedAe211Profile.requiredResources().keySet().stream()
                        .filter(path -> path.contains("/blockstates/"))
                        .count()
        );
        assertEquals(
                49,
                ExpandedAe211Profile.requiredResources().keySet().stream()
                        .filter(path -> path.contains("/models/"))
                        .count()
        );
        assertEquals(
                70,
                ExpandedAe211Profile.requiredResources().keySet().stream()
                        .filter(path -> path.contains("/textures/"))
                        .count()
        );
        assertTrue(ExpandedAe211Profile.acceptsArtifact(
                ExpandedAe211Profile.JAR_BYTES,
                ExpandedAe211Profile.JAR_SHA256
        ));
        assertFalse(ExpandedAe211Profile.acceptsArtifact(
                ExpandedAe211Profile.JAR_BYTES - 1,
                ExpandedAe211Profile.JAR_SHA256
        ));
    }

    @Test
    void allTwentyOneCraftingBlocksMapToExistingCoreKinds() {
        assertEquals(21, ExpandedAe211Catalog.craftingDefinitions().size());
        assertEquals(21, ExpandedAe211Catalog.craftingBlockKinds().size());
        assertEquals(
                1,
                ExpandedAe211Catalog.craftingBlockKinds().values().stream()
                        .filter(CraftingBlockKind.UNIT::equals)
                        .count()
        );
        assertEquals(
                20,
                ExpandedAe211Catalog.craftingBlockKinds().values().stream()
                        .filter(CraftingBlockKind.ACCELERATOR::equals)
                        .count()
        );
        assertEquals(
                CraftingBlockKind.UNIT,
                ExpandedAe211Catalog.kindForCraftingBlock(
                        "expandedae:exp_crafting_unit"
                )
        );
        ExpandedAe211Catalog.CraftingDefinition oneMillion =
                ExpandedAe211Catalog.craftingDefinitions().get(
                        "expandedae:exp_crafting_accelerator_1m"
                );
        assertEquals(CraftingBlockKind.ACCELERATOR, oneMillion.kind());
        assertEquals(
                Optional.of(
                        "expandedae:block/crafting/exp_crafting_accelerator_1m_light"
                ),
                oneMillion.dynamicLightTexture()
        );
        assertEquals(25, ExpandedAe211Catalog.dynamicCraftingTextures().size());
    }

    @Test
    void staticRoutesPartsAndUpstreamMissingDriveAreExplicit() {
        assertEquals(24, ExpandedAe211Catalog.registeredBlocks().size());
        assertEquals(23, ExpandedAe211Catalog.supportedVisualBlocks().size());
        assertEquals(
                Set.of("expandedae:colorable_drive"),
                ExpandedAe211Catalog.upstreamVisualUnavailableBlocks()
        );
        assertFalse(ExpandedAe211Catalog.supportedVisualBlocks().contains(
                ExpandedAe211Catalog.COLORABLE_DRIVE_BLOCK
        ));
        assertEquals(24, ExpandedAe211Catalog.blockEntityIds().size());
        assertEquals(
                "expandedae:exp_cpus",
                ExpandedAe211Catalog.blockEntityIds().get(
                        "expandedae:exp_crafting_accelerator_512k"
                )
        );
        assertEquals(2, ExpandedAe211Catalog.parts().size());
        assertFalse(ExpandedAe211Catalog.parts().get(
                ExpandedAe211Catalog.PATTERN_PROVIDER_PART
        ).supportsSpin());
        assertTrue(ExpandedAe211Catalog.parts().get(
                ExpandedAe211Catalog.ENCODING_TERMINAL_PART
        ).supportsSpin());
        assertEquals("color", ExpandedAe211Catalog.COLOR_NBT_KEY);
        assertEquals(16, ExpandedAe211Catalog.MAX_COLOR);
    }

    @Test
    void exposedExtensionContractsAreImmutable() {
        assertThrows(
                UnsupportedOperationException.class,
                () -> ExpandedAe211Catalog.craftingDefinitions().clear()
        );
        assertThrows(
                UnsupportedOperationException.class,
                () -> ExpandedAe211Catalog.registeredBlocks().clear()
        );
        assertThrows(
                UnsupportedOperationException.class,
                () -> ExpandedAe211Profile.requiredResources().clear()
        );
    }
}
