/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.model;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Ae2CableCatalogTest {

    @Test
    void catalogIsTheExactFiveBySeventeenCartesianProduct() {
        assertEquals(85, Ae2CableCatalog.definitions().size());
        assertEquals(85, Ae2CableCatalog.ids().size());

        Set<String> expected = new HashSet<>();
        for (CableFamily family : CableFamily.values()) {
            for (CableColor color : CableColor.values()) {
                String id = "ae2:" + color.registryPrefix() + "_" + family.idSuffix();
                expected.add(id);
                CableDefinition definition = Ae2CableCatalog.require(id);
                assertEquals(family, definition.family());
                assertEquals(color, definition.color());
            }
        }
        assertEquals(expected, Ae2CableCatalog.ids());
        assertEquals(
                CableColor.TRANSPARENT,
                Ae2CableCatalog.require("ae2:fluix_glass_cable").color()
        );
        assertEquals(
                "ae2:part/cable/core/glass/transparent",
                Ae2CableCatalog.require("ae2:fluix_glass_cable").coreTexture()
        );
        assertEquals(
                "ae2:part/cable/core/covered/transparent",
                Ae2CableCatalog.require("ae2:fluix_glass_cable")
                        .coreTexture(CableFamily.COVERED)
        );
        assertEquals(
                "ae2:part/cable/dense_covered/purple",
                Ae2CableCatalog.require("ae2:purple_covered_dense_cable")
                        .connectionTexture(CableFamily.DENSE_COVERED)
        );
    }

    @Test
    void exactTextureClosureContainsAllOneHundredFortyKeys() {
        assertEquals(140, Ae2CableCatalog.textures().size());
        assertTrue(Ae2CableCatalog.textures().contains(
                "ae2:part/cable/core/dense_smart/transparent"
        ));
        assertTrue(Ae2CableCatalog.textures().contains(
                Ae2CableCatalog.SMART_CHANNELS_OFF_ODD
        ));
        assertTrue(Ae2CableCatalog.textures().contains(
                Ae2CableCatalog.DENSE_SMART_CHANNELS_OFF_EVEN
        ));
        assertFalse(Ae2CableCatalog.textures().stream().anyMatch(
                texture -> texture.contains("/fluix")
        ));
    }

    @Test
    void catalogCollectionsAreImmutableAndUnknownIdsAreRejected() {
        assertThrows(UnsupportedOperationException.class,
                () -> Ae2CableCatalog.definitions().clear());
        assertThrows(UnsupportedOperationException.class,
                () -> Ae2CableCatalog.ids().clear());
        assertThrows(UnsupportedOperationException.class,
                () -> Ae2CableCatalog.textures().clear());
        assertTrue(Ae2CableCatalog.find("ae2:terminal").isEmpty());
        assertThrows(IllegalArgumentException.class,
                () -> Ae2CableCatalog.require("ae2:terminal"));
    }

    @Test
    void allColorPairsFollowTheExactTransparentOrEqualRule() {
        for (CableColor first : CableColor.values()) {
            for (CableColor second : CableColor.values()) {
                boolean expected = first == CableColor.TRANSPARENT
                        || second == CableColor.TRANSPARENT
                        || first == second;
                assertEquals(expected, first.connectsTo(second),
                        first + " -> " + second);
                assertEquals(expected, second.connectsTo(first),
                        second + " -> " + first);
            }
        }
    }

    @Test
    void allSmartOverlayTintPairsMatchExactAeColorVariants() {
        int[][] expected = {
                {0xb4b4b4, 0xf9f9f9},
                {0x7e7e7e, 0xc4c4c4},
                {0x4f4f4f, 0x949294},
                {0x131313, 0x3b3b3b},
                {0x4ec04e, 0xb3f86d},
                {0xffcf40, 0xf4ff80},
                {0xd9782f, 0xf2ba49},
                {0x6e4a12, 0x8e6e1a},
                {0xaa212b, 0xf07665},
                {0xd86eaa, 0xfbcad5},
                {0xc15189, 0xe69ebf},
                {0x6e5cb8, 0xb06fdd},
                {0x337ff0, 0x40c1ff},
                {0x69b9ff, 0x80f7ff},
                {0x22b0ae, 0x65e8c9},
                {0x079b6b, 0x32d850},
                {0x5a479e, 0xe2a3e3}
        };

        CableColor[] colors = CableColor.values();
        assertEquals(expected.length, colors.length);
        for (int index = 0; index < colors.length; index++) {
            assertEquals(
                    expected[index][0],
                    colors[index].darkRgb(),
                    colors[index].name()
            );
            assertEquals(
                    expected[index][1],
                    colors[index].brightRgb(),
                    colors[index].name()
            );
        }
    }

    @Test
    void familyMinimumAndVisibleHalfArmMatricesAreExact() {
        CableFamily[][] effective = {
                {g(), g(), g(), g(), g()},
                {g(), c(), c(), c(), c()},
                {g(), c(), s(), c(), s()},
                {g(), c(), c(), dc(), dc()},
                {g(), c(), s(), dc(), ds()}
        };
        CableFamily[][] visible = {
                {g(), g(), g(), g(), g()},
                {c(), c(), c(), c(), c()},
                {c(), c(), s(), c(), s()},
                {c(), c(), c(), dc(), dc()},
                {c(), c(), s(), dc(), ds()}
        };

        CableFamily[] families = CableFamily.values();
        for (int local = 0; local < families.length; local++) {
            for (int neighbor = 0; neighbor < families.length; neighbor++) {
                CableFamily actualEffective = CableFamily.minimum(
                        families[local],
                        families[neighbor]
                );
                assertEquals(effective[local][neighbor], actualEffective);
                assertEquals(
                        visible[local][neighbor],
                        CableGeometry.visibleArmFamily(
                                families[local],
                                actualEffective
                        )
                );
            }
        }
    }

    private static CableFamily g() {
        return CableFamily.GLASS;
    }

    private static CableFamily c() {
        return CableFamily.COVERED;
    }

    private static CableFamily s() {
        return CableFamily.SMART;
    }

    private static CableFamily dc() {
        return CableFamily.DENSE_COVERED;
    }

    private static CableFamily ds() {
        return CableFamily.DENSE_SMART;
    }
}
