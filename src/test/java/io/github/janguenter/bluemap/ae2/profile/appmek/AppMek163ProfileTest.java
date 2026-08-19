/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.profile.appmek;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AppMek163ProfileTest {

    @Test
    void exactArtifactPairAndDependencyRangesArePinned() {
        assertEquals("appmek", AppMek163Profile.MOD_ID);
        assertEquals("1.6.3", AppMek163Profile.VERSION);
        assertEquals(149_709L, AppMek163Profile.JAR_BYTES);
        assertEquals(
                "8946fea39451dbce8e709dedbef40a52ba337bdf7a25ac0c4b503800b1bf0773",
                AppMek163Profile.JAR_SHA256
        );
        assertEquals("[19.2.10,20.0.0)", AppMek163Profile.REQUIRED_AE2_VERSION_RANGE);
        assertEquals("[10.7.14,11-)", AppMek163Profile.REQUIRED_MEKANISM_VERSION_RANGE);
        assertEquals("10.7.19", AppMek163Profile.MEKANISM_VERSION);
        assertEquals(11_976_009L, AppMek163Profile.MEKANISM_JAR_BYTES);
        assertEquals(
                "004dbc9f3106f4d192aeaa1ee1190dd16ec9ca8059ed3d093b80034f4c574f43",
                AppMek163Profile.MEKANISM_JAR_SHA256
        );
        assertTrue(AppMek163Profile.acceptsAppMekArtifact(
                AppMek163Profile.JAR_BYTES,
                AppMek163Profile.JAR_SHA256
        ));
        assertTrue(AppMek163Profile.acceptsMekanismArtifact(
                AppMek163Profile.MEKANISM_JAR_BYTES,
                AppMek163Profile.MEKANISM_JAR_SHA256
        ));
        assertFalse(AppMek163Profile.acceptsAppMekArtifact(
                AppMek163Profile.JAR_BYTES + 1,
                AppMek163Profile.JAR_SHA256
        ));
        assertFalse(AppMek163Profile.acceptsMekanismArtifact(
                AppMek163Profile.MEKANISM_JAR_BYTES,
                "0".repeat(64)
        ));
    }

    @Test
    void exactDriveClosureIsSixPathsAnd3611Bytes() {
        Map<String, String> resources = AppMek163Profile.driveRequiredResources();
        Map<String, Long> sizes = AppMek163Profile.driveRequiredResourceSizes();

        assertEquals(AppMek163Profile.DRIVE_RESOURCE_COUNT, resources.size());
        assertEquals(
                AppMek163Profile.DRIVE_RESOURCE_BYTES,
                sizes.values().stream().mapToLong(Long::longValue).sum()
        );
        assertEquals(5, resources.keySet().stream()
                .filter(path -> path.contains("/models/block/drive/cells/"))
                .count());
        assertEquals(
                "34514c0f69228a8c008b5c8c696f7d460614ca4ad3531a4bcc9e5aa16c647fc5",
                resources.get("assets/appmek/textures/block/drive/drive_cells.png")
        );
    }

    @Test
    void packagedProfileDeclaresOnlyNativeDriveAndMatchesSemanticConstants()
            throws Exception {
        JsonObject profile;
        try (InputStreamReader reader = new InputStreamReader(
                AppMek163ProfileTest.class.getResourceAsStream(
                        "/bluemap-ae2/profiles/appmek/1.6.3/profile.json"
                ),
                StandardCharsets.UTF_8
        )) {
            profile = JsonParser.parseReader(reader).getAsJsonObject();
        }

        JsonArray routes = profile.getAsJsonArray("routes");
        assertEquals(1, routes.size());
        JsonObject route = routes.get(0).getAsJsonObject();
        assertEquals("appmek-drive-cells", route.get("routeId").getAsString());
        assertEquals("ae2:drive",
                route.getAsJsonObject("coverage").get("host").getAsString());
        assertEquals(6,
                route.getAsJsonObject("resourcePartition").get("pathCount").getAsInt());
        assertEquals(3611,
                route.getAsJsonObject("resourcePartition").get("totalBytes").getAsInt());

        JsonObject gate = profile.getAsJsonObject("semanticResourceGate");
        assertEquals(
                AppMek163Profile.MODEL_SEMANTIC_ALGORITHM,
                gate.get("resolvedModelAlgorithm").getAsString()
        );
        assertEquals(
                AppMek163Profile.TEXTURE_SEMANTIC_ALGORITHM,
                gate.get("decodedTextureAlgorithm").getAsString()
        );
        JsonObject drive = gate.getAsJsonObject("drive");
        assertEquals(AppMek163Profile.DRIVE_TEXTURE,
                drive.get("texture").getAsString());
        assertEquals(
                AppMek163Profile.DRIVE_TEXTURE_SEMANTIC_SHA256,
                drive.get("decodedTextureSemanticSha256").getAsString()
        );
        assertEquals(
                AppMek163Profile.DRIVE_MODEL_SEMANTIC_SHA256,
                drive.getAsJsonObject("modelSemanticSha256").entrySet().stream()
                        .collect(java.util.stream.Collectors.toUnmodifiableMap(
                                Map.Entry::getKey,
                                entry -> entry.getValue().getAsString()
                        ))
        );
    }
}
