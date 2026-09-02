/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.adapter.bluemap523;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class Ae2ZRotationRendererTest {

    @Test
    void normalizesOnlyObjectPropertyAliases() {
        String input = """
                {
                  "variants": {
                    "facing=up,spin=1": {
                      "ae2:z": 90,
                      "model": "example:block/ae2:z"
                    },
                    "facing=down,spin=0": {
                      "model": "example:block/static"
                    }
                  }
                }
                """;
        Ae2ZRotationRenderer.Normalization normalization =
                Ae2ZRotationRenderer.normalize(input);
        assertEquals(1, normalization.replacements());
        assertEquals(input.replace("\"ae2:z\": 90", "\"z\": 90"), normalization.json());
    }

    @Test
    void recognizesEscapedAliasAndLeavesStandardJsonUnchanged() {
        assertEquals(
                "{\"z\":270}",
                Ae2ZRotationRenderer.normalizeBlockstateJson(
                        "{\"\\u0061e2:z\":270}"
                )
        );
        String standard = "{\"variants\":{\"\":{\"model\":\"test:block/model\"}}}";
        assertEquals(standard, Ae2ZRotationRenderer.normalizeBlockstateJson(standard));
    }

    @Test
    void duplicateOrMalformedRotationDataFailsClosed() {
        assertThrows(
                IllegalArgumentException.class,
                () -> Ae2ZRotationRenderer.normalizeBlockstateJson(
                        "{\"variants\":{\"\":{\"z\":90,\"ae2:z\":180}}}"
                )
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> Ae2ZRotationRenderer.normalizeBlockstateJson(
                        "{\"variants\":{\"\":{\"ae2:z\":90,\"ae2:z\":180}}}"
                )
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> Ae2ZRotationRenderer.normalizeBlockstateJson("[]")
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> Ae2ZRotationRenderer.normalizeBlockstateJson("{\"ae2:z\":90]")
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> Ae2ZRotationRenderer.normalizeBlockstateJson("{\"ae2:z\":90")
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> Ae2ZRotationRenderer.normalizeBlockstateJson("{}{}")
        );
    }
}
