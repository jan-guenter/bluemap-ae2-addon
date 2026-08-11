/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.profile;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProfileDisablementTest {

    @Test
    void propertyAndEnvironmentCsvAreCombinedAndNormalized() {
        ProfileDisablement disablement = ProfileDisablement.from(
                " AE2, expandedae,ae2,ae2-quartz-glass,ae2-crafting,,bad/profile ",
                "MEGACELLS, advanced_ae, invalid profile"
        );

        assertEquals(
                Set.of(
                        "ae2",
                        "ae2-crafting",
                        "ae2-quartz-glass",
                        "expandedae",
                        "megacells",
                        "advanced_ae"
                ),
                disablement.disabledProfiles()
        );
        assertTrue(disablement.isDisabled("AE2"));
        assertTrue(disablement.isDisabled("megacells"));
        assertTrue(disablement.isDisabled("AE2-QUARTZ-GLASS"));
        assertTrue(disablement.isDisabled("AE2-CRAFTING"));
        assertFalse(disablement.isDisabled("extendedae"));
    }

    @Test
    void nullAndBlankInputsProduceNoDisablement() {
        assertTrue(ProfileDisablement.from(null, null).disabledProfiles().isEmpty());
        assertTrue(ProfileDisablement.from("  ", ",,").disabledProfiles().isEmpty());
    }

    @Test
    void exposedProfileSetIsImmutable() {
        ProfileDisablement disablement = ProfileDisablement.from("ae2", null);

        org.junit.jupiter.api.Assertions.assertThrows(
                UnsupportedOperationException.class,
                () -> disablement.disabledProfiles().clear()
        );
    }
}
