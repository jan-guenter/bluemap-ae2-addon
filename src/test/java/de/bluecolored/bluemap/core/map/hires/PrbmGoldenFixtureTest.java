/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package de.bluecolored.bluemap.core.map.hires;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

import org.junit.jupiter.api.Test;

/** Locks a tiny analyzer fixture to the exact pinned BlueMap writer. */
class PrbmGoldenFixtureTest {

    @Test
    void exactPinnedWriterMatchesGolden() throws IOException {
        ArrayTileModel model = new ArrayTileModel(1);
        int triangle = model.add(1);
        model.setPositions(
                triangle,
                0.25F, 100.5F, 0.25F,
                0.75F, 100.5F, 0.25F,
                0.25F, 100.5F, 0.75F
        );
        model.setUvs(triangle, 0F, 0F, 1F, 0F, 0F, 1F);
        model.setAOs(triangle, 1F, 0.5F, 0.25F);
        model.setColor(triangle, 1F, 0.5F, 0.25F);
        model.setBlocklight(triangle, 7);
        model.setSunlight(triangle, 15);
        model.setMaterialIndex(triangle, 3);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (PRBMWriter writer = new PRBMWriter(output)) {
            writer.write(model);
        }
        String encoded = Base64.getEncoder().encodeToString(output.toByteArray());
        Path fixture = Path.of(
                "tools/tests/fixtures/exact-writer-one-triangle.prbm.b64"
        );
        assertEquals(Files.readString(fixture, UTF_8).trim(), encoded);
    }
}
