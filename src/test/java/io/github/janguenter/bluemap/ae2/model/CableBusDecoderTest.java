/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.model;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CableBusDecoderTest {

    private final CableBusDecoder decoder = new CableBusDecoder();

    @Test
    void decodesAnExactCatalogCenter() {
        CableBusDecodeResult result = decoder.decode(
                Map.of("id", CableBusSnapshot.FLUIX_GLASS_CABLE),
                false
        );

        assertTrue(result.isSupported());
        assertEquals(CableBusDecodeResult.Status.SUPPORTED, result.status());
        assertEquals(CableBusSnapshot.fluixGlassCable(), result.supportedSnapshot().orElseThrow());
    }

    @Test
    void decodesAllEightyFiveExactCatalogCenters() {
        for (CableDefinition definition : Ae2CableCatalog.definitions()) {
            CableBusDecodeResult result = decoder.decode(
                    Map.of("id", definition.id()),
                    false
            );
            assertTrue(result.isSupported(), definition.id());
            assertEquals(
                    CableBusSnapshot.isolated(definition),
                    result.supportedSnapshot().orElseThrow()
            );
        }
    }

    @Test
    void classifiesEveryMalformedFixtureWithoutThrowing() {
        assertStatus(null, CableBusDecodeResult.Status.MISSING_CENTER_PART);
        assertStatus("malformed", CableBusDecodeResult.Status.CENTER_PART_NOT_COMPOUND);
        assertStatus(Map.of(), CableBusDecodeResult.Status.MISSING_CENTER_PART_ID);
        assertStatus(Map.of("id", 7), CableBusDecodeResult.Status.CENTER_PART_ID_NOT_STRING);
        assertStatus(
                Map.of("id", "not a resource id"),
                CableBusDecodeResult.Status.INVALID_CENTER_PART_ID
        );
        assertStatus(
                Map.of("id", "AE2:fluix_glass_cable"),
                CableBusDecodeResult.Status.INVALID_CENTER_PART_ID
        );
    }

    @Test
    void validUnknownAndNonCableAe2PartsUseUnsupportedFallback() {
        assertStatus(
                Map.of("id", "test:unknown_part"),
                CableBusDecodeResult.Status.UNSUPPORTED_CENTER_PART
        );
        assertStatus(
                Map.of("id", "ae2:terminal"),
                CableBusDecodeResult.Status.UNSUPPORTED_CENTER_PART
        );
    }

    @Test
    void anyRetainedAttachmentOrFacadeRejectsTheWholeM1Block() {
        CableBusDecodeResult result = decoder.decode(
                Map.of("id", CableBusSnapshot.FLUIX_GLASS_CABLE),
                true
        );

        assertFalse(result.isSupported());
        assertEquals(
                CableBusDecodeResult.Status.UNSUPPORTED_ATTACHMENTS_OR_FACADES,
                result.status()
        );
    }

    @Test
    void snapshotMaskIsImmutableAndValidated() {
        CableBusSnapshot base = CableBusSnapshot.fluixGlassCable();
        CableBusSnapshot east = base.withConnection(Direction6.EAST, true);

        assertFalse(base.connects(Direction6.EAST));
        assertTrue(east.connects(Direction6.EAST));
        assertEquals(0, base.connectionMask());
        assertEquals(Direction6.EAST.maskBit(), east.connectionMask());
        assertEquals(base, east.withConnection(Direction6.EAST, false));
        assertThrows(IllegalArgumentException.class, () -> base.withConnectionMask(-1));
        assertThrows(IllegalArgumentException.class, () -> base.withConnectionMask(64));
    }

    @Test
    void decodeResultEnforcesSupportedSnapshotInvariant() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new CableBusDecodeResult(CableBusDecodeResult.Status.SUPPORTED, null)
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> new CableBusDecodeResult(
                        CableBusDecodeResult.Status.UNSUPPORTED_CENTER_PART,
                        CableBusSnapshot.fluixGlassCable()
                )
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> CableBusDecodeResult.fallback(CableBusDecodeResult.Status.SUPPORTED)
        );
    }

    private void assertStatus(Object cable, CableBusDecodeResult.Status expected) {
        CableBusDecodeResult result = decoder.decode(cable, false);
        assertFalse(result.isSupported());
        assertTrue(result.supportedSnapshot().isEmpty());
        assertEquals(expected, result.status());
    }
}
