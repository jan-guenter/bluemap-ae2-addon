/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.adapter.bluemap523;

import de.bluecolored.bluemap.core.util.Key;
import de.bluecolored.bluemap.core.world.BlockState;
import io.github.janguenter.bluemap.ae2.profile.expandedae.ExpandedAe211Catalog;
import io.github.janguenter.bluemap.ae2.profile.merequester.MeRequester143Catalog;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Ae2ZVariantResolverTest {

    @Test
    void resolvesEveryRequesterStateFromTheExactCatalog() {
        MeRequester143Catalog.requesterVariants().values().forEach(expected -> {
            Map<String, String> properties = new LinkedHashMap<>();
            properties.put("active", Boolean.toString(expected.active()));
            properties.put("facing", expected.facing());
            if (expected.spin() >= 0) {
                properties.put("spin", Integer.toString(expected.spin()));
            }
            var actual = Ae2ZVariantResolver.resolve(new BlockState(
                    Key.parse(MeRequester143Catalog.REQUESTER_BLOCK),
                    properties
            ));
            assertEquals(expected.modelId(), actual.getModel().getFormatted());
            assertEquals(expected.xRotation(), actual.getX());
            assertEquals(expected.yRotation(), actual.getY());
            assertEquals(expected.zRotation(), actual.getZ());
            assertEquals(expected.requiresAe2Z(), actual.getZ() != 0);
        });
    }

    @Test
    void resolvesAllFortyEightExpandedIoPortStates() {
        int resolved = 0;
        for (boolean powered : new boolean[]{false, true}) {
            for (String facing : ExpandedAe211Catalog.facings()) {
                for (int spin = 0; spin < 4; spin++) {
                    var variant = Ae2ZVariantResolver.resolve(new BlockState(
                            Key.parse(ExpandedAe211Catalog.IO_PORT_BLOCK),
                            Map.of(
                                    "facing", facing,
                                    "powered", Boolean.toString(powered),
                                    "spin", Integer.toString(spin)
                            )
                    ));
                    assertEquals(
                            powered ? "expandedae:block/exp_io_port_on"
                                    : "expandedae:block/exp_io_port",
                            variant.getModel().getFormatted()
                    );
                    assertTrue(variant.getX() % 90 == 0);
                    assertTrue(variant.getY() % 90 == 0);
                    assertTrue(variant.getZ() % 90 == 0);
                    resolved++;
                }
            }
        }
        assertEquals(48, resolved);
    }

    @Test
    void rejectsUnknownOrMalformedStates() {
        assertNull(Ae2ZVariantResolver.resolve(new BlockState(
                Key.parse("minecraft:stone"),
                Map.of()
        )));
        assertNull(Ae2ZVariantResolver.resolve(new BlockState(
                Key.parse(MeRequester143Catalog.REQUESTER_BLOCK),
                Map.of("active", "false", "facing", "up")
        )));
        assertNull(Ae2ZVariantResolver.resolve(new BlockState(
                Key.parse(ExpandedAe211Catalog.IO_PORT_BLOCK),
                Map.of("powered", "false", "facing", "north", "spin", "4")
        )));
        assertFalse(MeRequester143Catalog.requesterVariants().isEmpty());
    }
}
