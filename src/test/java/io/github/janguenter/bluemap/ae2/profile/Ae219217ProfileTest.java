/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.profile;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import de.bluecolored.bluemap.core.resources.adapter.ResourcesGson;
import io.github.janguenter.bluemap.ae2.model.Ae2CableCatalog;
import io.github.janguenter.bluemap.ae2.model.DriveCellCatalog;
import io.github.janguenter.bluemap.ae2.model.DriveCellDefinition;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Ae219217ProfileTest {

    @Test
    void exactIdentityAndM3aPolicyAreLocked() {
        assertEquals("ae2", Ae219217Profile.PROFILE_ID);
        assertEquals("19.2.17", Ae219217Profile.VERSION);
        assertEquals("1.21.1", Ae219217Profile.MINECRAFT_VERSION);
        assertEquals("21.1.234", Ae219217Profile.NEOFORGE_VERSION);
        assertEquals("idle-off-unknown", Ae219217Profile.TRANSIENT_POLICY);
        assertEquals(8_230_896L, Ae219217Profile.JAR_BYTES);
        assertEquals(
                "460d779a0609b81409907d9956de8f6f70a1b0912257e3e5c3c7e75ac9630e95",
                Ae219217Profile.JAR_SHA256
        );
        assertEquals(85, Ae219217Profile.supportedCenterParts().size());
        assertEquals(148, Ae219217Profile.coreTextures().size());
        assertEquals(10, Ae219217Profile.driveTextures().size());
        assertEquals(158, Ae219217Profile.textures().size());
        assertEquals(170, Ae219217Profile.coreRequiredResources().size());
        assertEquals(26, Ae219217Profile.driveRequiredResources().size());
        assertEquals(196, Ae219217Profile.requiredResources().size());
        assertTrue(Set.copyOf(Ae219217Profile.coreTextures()).stream()
                .noneMatch(Set.copyOf(Ae219217Profile.driveTextures())::contains));
        assertTrue(Set.copyOf(Ae219217Profile.coreRequiredResources().keySet()).stream()
                .noneMatch(Ae219217Profile.driveRequiredResources()::containsKey));
        assertEquals(Ae219217Profile.requiredResources().keySet(), union(
                Ae219217Profile.coreRequiredResources().keySet(),
                Ae219217Profile.driveRequiredResources().keySet()
        ));
        assertEquals(21, Ae219217Profile.explicitDriveCellModels().size());
        assertEquals(2, Ae219217Profile.genericDriveCellIds().size());
        assertEquals(23, Ae219217Profile.driveCellModels().size());
        assertEquals(12, Set.copyOf(Ae219217Profile.driveCellModels().values()).size());
        assertEquals(
                DriveCellCatalog.definitions().stream().collect(
                        java.util.stream.Collectors.toMap(
                                DriveCellDefinition::itemId,
                                DriveCellDefinition::modelId
                        )
                ),
                Ae219217Profile.driveCellModels()
        );
    }

    @Test
    void profileCollectionsAreImmutable() {
        assertThrows(
                UnsupportedOperationException.class,
                () -> Ae219217Profile.supportedCenterParts().clear()
        );
        assertThrows(
                UnsupportedOperationException.class,
                () -> Ae219217Profile.textures().clear()
        );
        assertThrows(
                UnsupportedOperationException.class,
                () -> Ae219217Profile.coreTextures().clear()
        );
        assertThrows(
                UnsupportedOperationException.class,
                () -> Ae219217Profile.driveTextures().clear()
        );
        assertThrows(
                UnsupportedOperationException.class,
                () -> Ae219217Profile.requiredResources().clear()
        );
        assertThrows(
                UnsupportedOperationException.class,
                () -> Ae219217Profile.coreRequiredResources().clear()
        );
        assertThrows(
                UnsupportedOperationException.class,
                () -> Ae219217Profile.driveRequiredResources().clear()
        );
        assertThrows(
                UnsupportedOperationException.class,
                () -> Ae219217Profile.explicitDriveCellModels().clear()
        );
        assertThrows(
                UnsupportedOperationException.class,
                () -> Ae219217Profile.genericDriveCellIds().clear()
        );
        assertThrows(
                UnsupportedOperationException.class,
                () -> Ae219217Profile.driveCellModels().clear()
        );
        assertTrue(Ae219217Profile.requiredResources().values().stream()
                .allMatch(value -> value.matches("[0-9a-f]{64}")));
    }

    @Test
    void packagedProfileAndRuntimeCatalogCannotDriftApart() throws Exception {
        try (InputStream input = Ae219217Profile.class.getResourceAsStream(
                "/bluemap-ae2/profiles/ae2/19.2.17/profile.json"
        )) {
            assertNotNull(input);
            JsonObject document = ResourcesGson.INSTANCE.fromJson(
                    new InputStreamReader(input, StandardCharsets.UTF_8),
                    JsonObject.class
            );

            assertEquals(3, document.get("schemaVersion").getAsInt());
            assertEquals("M3a", document.get("coverageMilestone").getAsString());
            assertEquals(
                    List.copyOf(Ae2CableCatalog.ids()),
                    strings(document.getAsJsonArray("supportedCenterParts"))
            );
            assertEquals(
                    Ae219217Profile.coreTextures(),
                    strings(document.getAsJsonArray("coreTextures"))
            );
            assertEquals(
                    Ae219217Profile.driveTextures(),
                    strings(document.getAsJsonArray("driveTextures"))
            );
            assertEquals(
                    Ae219217Profile.textures(),
                    strings(document.getAsJsonArray("textures"))
            );
            assertEquals(
                    "e40a9bc4942d8999d825f42bce94947079948d74024f4f1a078cc55252d81d33",
                    document.get("coreTextureManifestSha256").getAsString()
            );
            assertEquals(
                    "c0e66d75cad06649b021f8a9073629d6619050c4f69e78c522b6fa32fb232242",
                    document.get("textureManifestSha256").getAsString()
            );
            assertEquals(
                    "408297def444f1392b7b87fdc4b8520099513b4c57c63a4176b808ce61b4e1be",
                    document.get("requiredResourcesSha256").getAsString()
            );
            JsonObject part = document.getAsJsonArray("supportedFaceParts")
                    .get(0).getAsJsonObject();
            assertEquals(Ae219217Profile.TERMINAL_PART, part.get("id").getAsString());
            assertEquals(List.of(0, 1, 2, 3), integers(part.getAsJsonArray("spins")));
            JsonObject facade = document.getAsJsonObject("facadePolicy");
            assertEquals(
                    Ae219217Profile.FACADE_BLOCK,
                    facade.getAsJsonObject("blockState").get("Name").getAsString()
            );
            assertEquals("forbidden", facade.get("properties").getAsString());
            assertEquals(1, facade.get("maximumFacades").getAsInt());
            assertEquals(
                    Ae219217Profile.TERMINAL_PART,
                    facade.get("requiredSameFacePart").getAsString()
            );
            JsonObject drive = document.getAsJsonObject("supportedDrive");
            assertEquals(Ae219217Profile.DRIVE_BLOCK, drive.get("blockId").getAsString());
            assertEquals(Ae219217Profile.DRIVE_SLOT_COUNT, drive.get("slotCount").getAsInt());
            assertEquals(
                    Ae219217Profile.DRIVE_BASE_MODEL,
                    drive.get("baseModel").getAsString()
            );
            assertEquals(
                    Ae219217Profile.DRIVE_EMPTY_CELL_MODEL,
                    drive.get("emptyCellModel").getAsString()
            );
            assertEquals(
                    Ae219217Profile.explicitDriveCellModels(),
                    stringMap(drive.getAsJsonObject("explicitCellModels"))
            );
            JsonObject generic = drive.getAsJsonObject("genericCellModel");
            assertEquals(
                    Ae219217Profile.DRIVE_GENERIC_CELL_MODEL,
                    generic.get("model").getAsString()
            );
            assertEquals(
                    Ae219217Profile.genericDriveCellIds(),
                    strings(generic.getAsJsonArray("itemIds"))
            );
            assertEquals(12, drive.get("occupiedModelCount").getAsInt());
            assertEquals(
                    Ae219217Profile.DRIVE_LED_POLICY,
                    drive.get("ledPolicy").getAsString()
            );
            assertEquals(
                    Ae219217Profile.DRIVE_UNKNOWN_CELL_POLICY,
                    drive.get("unknownCellPolicy").getAsString()
            );
            JsonObject partitions = document.getAsJsonObject("resourcePartitions");
            assertPartition(
                    partitions.getAsJsonObject("coreM0ThroughM2"),
                    170,
                    148,
                    "4f783945d92be446c8e5939f9455b24f9d463cb39f6b4e35e76c9b6fb713b3c2"
            );
            assertPartition(
                    partitions.getAsJsonObject("m3aDrive"),
                    26,
                    10,
                    "a8d10416d0fce66d8a91ce9e0dc93a83d2f552da8762a0a90e183dc58f6745cf"
            );
        }
    }

    private static void assertPartition(
            JsonObject partition,
            int pathCount,
            int textureCount,
            String manifestSha256
    ) {
        assertEquals(pathCount, partition.get("pathCount").getAsInt());
        assertEquals(textureCount, partition.get("textureCount").getAsInt());
        assertEquals(manifestSha256, partition.get("manifestSha256").getAsString());
    }

    private static Map<String, String> stringMap(JsonObject object) {
        Map<String, String> values = new java.util.LinkedHashMap<>();
        object.entrySet().forEach(entry -> values.put(
                entry.getKey(),
                entry.getValue().getAsString()
        ));
        return Map.copyOf(values);
    }

    private static Set<String> union(Set<String> first, Set<String> second) {
        Set<String> values = new HashSet<>(first);
        values.addAll(second);
        return Set.copyOf(values);
    }

    private static List<String> strings(JsonArray array) {
        List<String> values = new ArrayList<>(array.size());
        array.forEach(element -> values.add(element.getAsString()));
        return List.copyOf(values);
    }

    private static List<Integer> integers(JsonArray array) {
        List<Integer> values = new ArrayList<>(array.size());
        array.forEach(element -> values.add(element.getAsInt()));
        return List.copyOf(values);
    }
}
