/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.adapter.bluemap522;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.bluecolored.bluemap.core.map.TextureGallery;
import de.bluecolored.bluemap.core.map.hires.ArrayTileModel;
import de.bluecolored.bluemap.core.map.hires.PRBMWriter;
import de.bluecolored.bluemap.core.map.hires.RenderSettings;
import de.bluecolored.bluemap.core.map.hires.TileModelView;
import de.bluecolored.bluemap.core.map.mask.Mask;
import de.bluecolored.bluemap.core.resources.adapter.ResourcesGson;
import de.bluecolored.bluemap.core.resources.ResourcePath;
import de.bluecolored.bluemap.core.resources.pack.PackVersion;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.texture.Texture;
import de.bluecolored.bluemap.core.util.Key;
import de.bluecolored.bluemap.core.util.math.Color;
import de.bluecolored.bluemap.core.world.BlockEntity;
import de.bluecolored.bluemap.core.world.BlockState;
import de.bluecolored.bluemap.core.world.DimensionType;
import de.bluecolored.bluemap.core.world.LightData;
import de.bluecolored.bluemap.core.world.biome.Biome;
import de.bluecolored.bluemap.core.world.block.BlockAccess;
import de.bluecolored.bluemap.core.world.block.BlockNeighborhood;
import de.bluecolored.bluemap.core.world.mca.blockentity.MCABlockEntity;
import io.github.janguenter.bluemap.ae2.activation.NativeStructuralRouteActivation;
import io.github.janguenter.bluemap.ae2.activation.ProfileActivation;
import io.github.janguenter.bluemap.ae2.model.FacadeSnapshot;
import io.github.janguenter.bluemap.ae2.profile.Ae219217NativeStructuralProfile;
import io.github.janguenter.bluemap.ae2.profile.Ae219217Profile;
import io.github.janguenter.bluemap.ae2.model.NativeStructuralPartCatalog;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Opt-in compiled-runtime exporter for the frozen schema-10 S1 oracle.
 *
 * <p>Normal test runs skip this evidence helper. Set {@code AE2_S1_ORACLE_EXPORT}
 * to a directory, set {@code AE2_S1_GLASSENTIAL_JAR} to the exact full-pack
 * input, and optionally override the other exact input/resource paths below.
 */
class NativeStructuralOracleExporterTest {

    private static final long EXPECTED_CASES_SIZE_BYTES = 291_087L;
    private static final String EXPECTED_CASES_SHA256 =
            "b797930fc3f8eca822d0cbc674a4cc264671382db94d7627d9ba991c5d71fae8";
    private static final long EXPECTED_LEGACY_INPUT_SIZE_BYTES = 22_189L;
    private static final String EXPECTED_LEGACY_INPUT_SHA256 =
            "6a578463bbacb8267e7bff82bf76708d2b2950a7e9b052b53131626a331245de";
    private static final long EXPECTED_SCHEMA9_CASES_SIZE_BYTES = 3_314_082L;
    private static final String EXPECTED_SCHEMA9_CASES_SHA256 =
            "75e6ba2f40631a95f20cfa00d7ca952e521bc2c7a4eb155926334a223a945f3a";
    private static final long EXPECTED_SCHEMA9_GALLERY_SIZE_BYTES = 49_679L;
    private static final String EXPECTED_SCHEMA9_GALLERY_SHA256 =
            "21ceec072cc3263a41bdb81874e897d48d5a1ce5e1c7d3ac3c0de3063818ee6c";
    private static final int EXPECTED_LEGACY_TRIANGLE_COUNT = 840;
    private static final long EXPECTED_GLASSENTIAL_SIZE_BYTES = 702_249L;
    private static final String EXPECTED_GLASSENTIAL_SHA1 =
            "3a08f59f0930c8123fa1aacdfa0ba9fbdbb6e342";
    private static final String EXPECTED_GLASSENTIAL_SHA256 =
            "1f0c8f7533bf3b2002575219ba795fd32a44cc5085c2710624ebbf69e6121471";
    private static final String EXPECTED_GLASSENTIAL_SHA512 =
            "62ccb9057aab96ba656ec8ce357977360c1cc7761fedd7ac995a40b1f16e389c7"
                    + "5d753746840b11d30077b6b896938246fb281ec481e560a05084e22098c31d8";
    private static final String GLASSENTIAL_GLASS_MODEL =
            "assets/minecraft/models/block/glass.json";
    private static final String EXPECTED_GLASSENTIAL_GLASS_MODEL_SHA256 =
            "dc3cf6fdf740fceb4d2224dcb4132ab103617d0b904fcbbf6b48dbee0ecc9e4e";
    private static final String GLASSENTIAL_GLASS_TEXTURE =
            "assets/glassential/textures/block/glass.png";
    private static final String EXPECTED_GLASSENTIAL_GLASS_TEXTURE_SHA256 =
            "0a5534e6eb350dbce3670d9a4bc98f98ef20fb0747068d374f3529842b902370";
    private static final String GLASSENTIAL_GLASS_TEXTURE_MCMETA =
            "assets/glassential/textures/block/glass.png.mcmeta";
    private static final int EXPECTED_GLASSENTIAL_GLASS_TEXTURE_MCMETA_SIZE_BYTES = 97;
    private static final String EXPECTED_GLASSENTIAL_GLASS_TEXTURE_MCMETA_SHA256 =
            "23117542de8eb132a734e588a7cac393e7d8375632e4df56cf31010a8360c719";
    private static final String EXPECTED_PROFILE_SHA256 =
            "f6fa515b4e17205a019d57f253d5e71017ea20e75b8f0c333aa587afd0d0f353";
    private static final String PROFILE_RESOURCE =
            "/bluemap-ae2/profiles/ae2/19.2.17/routes/cable-bus-structural/profile.json";
    private static final Key CABLE_BUS = Key.parse(Ae219217Profile.CABLE_BUS_BLOCK);
    private static final Set<String> LEGACY_ORACLE_OUTPUT_FIELDS = Set.of(
            "expected_path",
            "expected_connections",
            "expected_triangle_count",
            "expected_material_triangles",
            "expected_smart_overlays",
            "expected_terminal_layers",
            "expected_geometry_signature",
            "expected_attribute_signature",
            "expected_nonlighting_attribute_signature",
            "triangle_count",
            "material_triangles",
            "geometry_signature",
            "attribute_signature",
            "nonlighting_attribute_signature"
    );
    private static final List<LegacyAnchorIdentity> EXPECTED_LEGACY_ANCHORS = List.of(
            legacyAnchor("ae2-m1-02", 216, 100, 226),
            legacyAnchor("ae2-m1-03", 222, 100, 226),
            legacyAnchor("ae2-m2-06", 210, 100, 248),
            legacyAnchor("ae2-m2-07", 213, 100, 248),
            legacyAnchor("ae2-m2-09", 219, 100, 248),
            legacyAnchor("ae2-m2-10", 222, 100, 248),
            legacyAnchor("ae2-m2-11", 225, 100, 248),
            legacyAnchor("ae2-m2-12", 228, 100, 248),
            legacyAnchor("ae2-m2-13", 231, 100, 248),
            legacyAnchor("ae2-m2-14", 234, 100, 248)
    );

    private static final Path DEFAULT_CASES = Path.of(
            "/tmp/ae2-s1-cases-final.json"
    );
    private static final Path DEFAULT_LEGACY_INPUT = Path.of(
            "gallery/native-structural-legacy-input.json"
    );
    private static final Path DEFAULT_BLUE_MAP_RESOURCES = Path.of(
            "/root/work/allthemons/bluemap-backport/core/src/main/resourceExtensions"
    );
    private static final Path DEFAULT_AE2_RESOURCES = Path.of(
            "/root/work/allthemons/research/ae2-19.2.17/src/main/resources"
    );
    private static final Path DEFAULT_AE2_GENERATED_RESOURCES = Path.of(
            "/root/work/allthemons/research/ae2-19.2.17/src/generated/resources"
    );
    private static final Path DEFAULT_MINECRAFT_CLIENT = Path.of(
            "/root/.gradle/caches/neoformruntime/artifacts/minecraft_1.21.1_client.jar"
    );
    private static final Gson GSON = ResourcesGson.INSTANCE;

    @Test
    void exportsFrozenCustomAnchorsThroughPinnedWriter() throws Exception {
        String requestedOutput = System.getenv("AE2_S1_ORACLE_EXPORT");
        Assumptions.assumeTrue(requestedOutput != null && !requestedOutput.isBlank());

        Path output = Path.of(requestedOutput);
        Path casesPath = configured("AE2_S1_CASES", DEFAULT_CASES);
        Path legacyInputPath = configured(
                "AE2_S1_LEGACY_INPUT",
                DEFAULT_LEGACY_INPUT
        );
        Path blueMapResources = configured(
                "AE2_S1_BLUEMAP_RESOURCES",
                DEFAULT_BLUE_MAP_RESOURCES
        );
        Path ae2Resources = configured("AE2_S1_AE2_RESOURCES", DEFAULT_AE2_RESOURCES);
        Path ae2GeneratedResources = configured(
                "AE2_S1_AE2_GENERATED_RESOURCES",
                DEFAULT_AE2_GENERATED_RESOURCES
        );
        Path minecraftClient = configured(
                "AE2_S1_MINECRAFT_CLIENT",
                DEFAULT_MINECRAFT_CLIENT
        );
        Path glassentialJar = requiredConfigured("AE2_S1_GLASSENTIAL_JAR");
        for (Path required : List.of(
                casesPath,
                legacyInputPath,
                blueMapResources,
                ae2Resources,
                ae2GeneratedResources,
                minecraftClient,
                glassentialJar
        )) {
            assertTrue(Files.exists(required), required.toString());
        }
        assertEquals(EXPECTED_CASES_SIZE_BYTES, Files.size(casesPath));
        assertEquals(EXPECTED_CASES_SHA256, sha256(casesPath));
        assertEquals(EXPECTED_GLASSENTIAL_SIZE_BYTES, Files.size(glassentialJar));
        assertEquals(EXPECTED_GLASSENTIAL_SHA1, sha1(glassentialJar));
        assertEquals(EXPECTED_GLASSENTIAL_SHA256, sha256(glassentialJar));
        assertEquals(EXPECTED_GLASSENTIAL_SHA512, sha512(glassentialJar));
        assertEquals(
                EXPECTED_GLASSENTIAL_GLASS_MODEL_SHA256,
                sha256(readArchiveEntry(glassentialJar, GLASSENTIAL_GLASS_MODEL))
        );
        assertEquals(
                EXPECTED_GLASSENTIAL_GLASS_TEXTURE_SHA256,
                sha256(readArchiveEntry(glassentialJar, GLASSENTIAL_GLASS_TEXTURE))
        );
        byte[] glassTextureMcmeta = readArchiveEntry(
                glassentialJar,
                GLASSENTIAL_GLASS_TEXTURE_MCMETA
        );
        assertEquals(
                EXPECTED_GLASSENTIAL_GLASS_TEXTURE_MCMETA_SIZE_BYTES,
                glassTextureMcmeta.length
        );
        assertEquals(
                EXPECTED_GLASSENTIAL_GLASS_TEXTURE_MCMETA_SHA256,
                sha256(glassTextureMcmeta)
        );
        assertEquals(
                EXPECTED_PROFILE_SHA256,
                Ae219217NativeStructuralProfile.PROFILE_SHA256
        );
        try (InputStream profile = NativeStructuralOracleExporterTest.class
                .getResourceAsStream(PROFILE_RESOURCE)) {
            assertTrue(profile != null, PROFILE_RESOURCE);
            assertEquals(EXPECTED_PROFILE_SHA256, sha256(profile.readAllBytes()));
        }

        JsonArray cases = GSON.fromJson(
                Files.readString(casesPath, StandardCharsets.UTF_8),
                JsonArray.class
        );
        LegacyFixture legacyFixture = loadLegacyFixture(legacyInputPath);
        ResourcePack resourcePack = new ResourcePack(new PackVersion(34, 0));
        resourcePack.loadResources(List.of(
                blueMapResources,
                ae2Resources,
                ae2GeneratedResources,
                glassentialJar,
                minecraftClient
        ));
        loadAnyFilteredStructuralTextures(resourcePack, ae2Resources);
        assertTrue(
                NativeStructuralResourceModels.resourcesSupported(resourcePack),
                () -> structuralResourceProblems(resourcePack)
        );
        NativeFacadeResourceModels.FacadeMaterial fullPackGlass =
                NativeFacadeResourceModels.resolve(
                        resourcePack,
                        new FacadeSnapshot("minecraft:glass", Map.of())
                );
        assertNotNull(fullPackGlass);
        assertTrue(
                fullPackGlass.layers().stream().allMatch(layer ->
                        layer.texture().equals(Key.parse("glassential:block/glass"))
                ),
                "Glassential must win the first-resource BlueMap pack precedence"
        );
        Texture storedFullPackGlass = resourcePack.getTextures().get(
                Key.parse("glassential:block/glass")
        );
        assertNotNull(storedFullPackGlass);
        assertNotNull(storedFullPackGlass.getAnimation());
        assertEquals(false, storedFullPackGlass.getAnimation().isInterpolate());
        assertEquals(1, storedFullPackGlass.getAnimation().getWidth());
        assertEquals(1, storedFullPackGlass.getAnimation().getHeight());
        assertEquals(1, storedFullPackGlass.getAnimation().getFrametime());
        assertTrue(
                storedFullPackGlass.getAnimation().getFrames() == null,
                "BlueMap 5.22 must ignore the non-animation Fusion mcmeta section; "
                        + "the client cutout visual still requires fresh human acceptance"
        );

        TextureGallery gallery = new TextureGallery();
        gallery.put(resourcePack.getTextures());

        Map<Position, BlockState> states = new HashMap<>();
        Map<Position, BlockEntity> blockEntities = new HashMap<>();
        List<Anchor> customAnchors = new ArrayList<>(351);
        for (JsonElement caseElement : cases) {
            JsonObject fixtureCase = caseElement.getAsJsonObject();
            for (JsonElement anchorElement : fixtureCase.getAsJsonArray("anchors")) {
                JsonObject anchor = anchorElement.getAsJsonObject();
                Position position = position(anchor.get("position"));
                states.put(position, exactCableBusState());
                boolean custom = "custom-s1".equals(
                        anchor.get("expected_path").getAsString()
                );
                Ae2CableBusBlockEntityData data = custom
                        ? cableBus(anchor) : new Ae2CableBusBlockEntityData();
                setBlockEntityId(data, CABLE_BUS);
                blockEntities.put(
                        position,
                        data
                );
                if (custom) {
                    customAnchors.add(new Anchor(position, anchor));
                }
            }
            for (JsonElement helperElement
                    : fixtureCase.getAsJsonArray("fixture_blocks")) {
                JsonObject helper = helperElement.getAsJsonObject();
                Position position = position(helper.get("position"));
                String blockId = helper.get("block_id").getAsString();
                if ("ae2:cable_bus".equals(blockId)) {
                    states.put(position, exactCableBusState());
                    blockEntities.put(position, cableBus(helper));
                } else {
                    states.put(position, blockState(helper));
                    if (helper.has("expected_block_entity_id")) {
                        MCABlockEntity entity = new MCABlockEntity();
                        setBlockEntityId(
                                entity,
                                Key.parse(helper.get("expected_block_entity_id")
                                        .getAsString())
                        );
                        blockEntities.put(position, entity);
                    }
                }
            }
        }
        assertEquals(351, customAnchors.size());

        ProfileActivation profileActivation = new ProfileActivation();
        profileActivation.activate();
        NativeStructuralRouteActivation routeActivation =
                new NativeStructuralRouteActivation();
        routeActivation.activate();
        CableBusRenderer renderer = new CableBusRenderer(
                resourcePack,
                gallery,
                TEST_RENDER_SETTINGS,
                profileActivation,
                routeActivation
        );

        RenderOutput s1 = renderAnchors(
                renderer,
                routeActivation,
                resourcePack,
                states,
                blockEntities,
                customAnchors
        );
        RenderOutput legacy = renderAnchors(
                renderer,
                routeActivation,
                resourcePack,
                legacyFixture.states(),
                legacyFixture.blockEntities(),
                legacyFixture.anchors()
        );
        assertEquals(EXPECTED_LEGACY_TRIANGLE_COUNT, legacy.triangleCount());

        Files.createDirectories(output);
        try (OutputStream stream = Files.newOutputStream(output.resolve("s1.prbm"));
             PRBMWriter writer = new PRBMWriter(stream)) {
            writer.write(s1.model());
        }
        try (OutputStream stream = Files.newOutputStream(
                output.resolve("legacy-upgrades.prbm")
        ); PRBMWriter writer = new PRBMWriter(stream)) {
            writer.write(legacy.model());
        }
        try (OutputStream stream = Files.newOutputStream(output.resolve("textures.json"))) {
            gallery.writeTexturesFile(stream);
        }
        Files.writeString(
                output.resolve("ranges.json"),
                GSON.toJson(s1.ranges()) + "\n",
                StandardCharsets.UTF_8
        );
        Files.writeString(
                output.resolve("legacy-upgrades-ranges.json"),
                GSON.toJson(legacy.ranges()) + "\n",
                StandardCharsets.UTF_8
        );
        Files.writeString(
                output.resolve("source-cases.sha256"),
                sha256(casesPath) + "  " + casesPath + "\n",
                StandardCharsets.UTF_8
        );
        Files.writeString(
                output.resolve("source-legacy-input.sha256"),
                sha256(legacyInputPath) + "  " + legacyInputPath + "\n",
                StandardCharsets.UTF_8
        );
        Files.writeString(
                output.resolve("source-glassential.sha256"),
                sha256(glassentialJar) + "  " + glassentialJar + "\n",
                StandardCharsets.UTF_8
        );
    }

    @Test
    void validatesFrozenLegacyUpgradeInputWhenAvailable() throws Exception {
        Assumptions.assumeTrue(Files.isRegularFile(DEFAULT_LEGACY_INPUT));

        LegacyFixture fixture = loadLegacyFixture(DEFAULT_LEGACY_INPUT);

        assertEquals(10, fixture.anchors().size());
        assertEquals(102, fixture.states().size());
        assertEquals(12, fixture.blockEntities().size());
        assertEquals(
                BlockState.fromString("ae2:energy_acceptor"),
                fixture.states().get(new Position(217, 100, 226))
        );
        assertEquals(
                new BlockState(
                        Key.parse("ae2:controller"),
                        Map.of("state", "offline", "type", "block")
                ),
                fixture.states().get(new Position(223, 100, 226))
        );
        for (LegacyAnchorIdentity identity : EXPECTED_LEGACY_ANCHORS) {
            Position anchor = identity.position();
            for (int x = anchor.x() - 1; x <= anchor.x() + 1; x++) {
                for (int z = anchor.z() - 1; z <= anchor.z() + 1; z++) {
                    assertEquals(
                            BlockState.fromString("minecraft:smooth_stone"),
                            fixture.states().get(new Position(x, 99, z))
                    );
                }
            }
        }
    }

    @Test
    void freshNeighborhoodsKeepModuloEightAliasesIndependent() {
        Position first = new Position(1, 17, 1);
        Position moduloAlias = new Position(9, 17, 1);
        BlockState firstState = BlockState.fromString("minecraft:stone");
        BlockState aliasState = BlockState.fromString("minecraft:glass");
        Map<Position, BlockState> states = Map.of(
                first, firstState,
                moduloAlias, aliasState
        );
        ResourcePack resourcePack = new ResourcePack(new PackVersion(34, 0));

        BlockNeighborhood firstNeighborhood = freshNeighborhood(
                states,
                Map.of(),
                resourcePack,
                first
        );
        BlockNeighborhood aliasNeighborhood = freshNeighborhood(
                states,
                Map.of(),
                resourcePack,
                moduloAlias
        );

        assertNotSame(firstNeighborhood, aliasNeighborhood);
        assertEquals(first.x(), firstNeighborhood.getX());
        assertEquals(moduloAlias.x(), aliasNeighborhood.getX());
        assertEquals(firstState, firstNeighborhood.getBlockState());
        assertEquals(aliasState, aliasNeighborhood.getBlockState());
    }

    private static RenderOutput renderAnchors(
            CableBusRenderer renderer,
            NativeStructuralRouteActivation routeActivation,
            ResourcePack resourcePack,
            Map<Position, BlockState> states,
            Map<Position, BlockEntity> blockEntities,
            List<Anchor> anchors
    ) {
        ArrayTileModel model = new ArrayTileModel(65_536);
        JsonObject ranges = new JsonObject();
        int triangleCount = 0;
        for (Anchor anchor : anchors) {
            Position position = anchor.position();
            BlockNeighborhood neighborhood = freshNeighborhood(
                    states,
                    blockEntities,
                    resourcePack,
                    position
            );
            TileModelView view = new TileModelView(model);
            renderer.render(neighborhood, null, view, new Color());
            assertTrue(routeActivation.isActive(), routeActivation.reason());
            assertTrue(view.getSize() > 0, position.toString());

            int tileLocalX = Math.floorMod(position.x() - 2, 32);
            int tileLocalZ = Math.floorMod(position.z() - 2, 32);
            view.translate(tileLocalX, position.y(), tileLocalZ);

            JsonObject range = new JsonObject();
            range.addProperty("start", view.getStart());
            range.addProperty("count", view.getSize());
            range.addProperty("tile_local_x", tileLocalX);
            range.addProperty("tile_local_z", tileLocalZ);
            ranges.add(position.key(), range);
            triangleCount += view.getSize();
        }
        return new RenderOutput(model, ranges, triangleCount);
    }

    private static LegacyFixture loadLegacyFixture(Path inputPath) throws Exception {
        assertTrue(Files.isRegularFile(inputPath), inputPath.toString());
        assertEquals(EXPECTED_LEGACY_INPUT_SIZE_BYTES, Files.size(inputPath));
        assertEquals(EXPECTED_LEGACY_INPUT_SHA256, sha256(inputPath));

        JsonObject input = GSON.fromJson(
                Files.readString(inputPath, StandardCharsets.UTF_8),
                JsonObject.class
        );
        assertNotNull(input);
        assertEquals(
                Set.of(
                        "cases",
                        "coverage_id",
                        "profile_id",
                        "schema_version",
                        "source_schema9",
                        "synthetic_world"
                ),
                keys(input)
        );
        assertNoLegacyOracleOutputFields(input);
        assertEquals(1, input.get("schema_version").getAsInt());
        assertEquals(
                "s1-native-structural-legacy-upgrades",
                input.get("coverage_id").getAsString()
        );
        assertEquals(
                "ae2-cable-bus-structural",
                input.get("profile_id").getAsString()
        );
        validateLegacySourceHeader(input.getAsJsonObject("source_schema9"));
        validateLegacySyntheticWorld(input.getAsJsonObject("synthetic_world"));

        JsonArray cases = input.getAsJsonArray("cases");
        assertNotNull(cases);
        assertEquals(EXPECTED_LEGACY_ANCHORS.size(), cases.size());

        Map<Position, BlockState> states = new HashMap<>();
        Map<Position, BlockEntity> blockEntities = new HashMap<>();
        Set<Position> helperPositions = new HashSet<>();
        List<Anchor> anchors = new ArrayList<>(EXPECTED_LEGACY_ANCHORS.size());
        int floorCount = 0;
        for (int index = 0; index < cases.size(); index++) {
            JsonObject fixtureCase = cases.get(index).getAsJsonObject();
            LegacyAnchorIdentity identity = EXPECTED_LEGACY_ANCHORS.get(index);
            assertEquals(
                    Set.of("anchors", "case_id", "fixture_blocks"),
                    keys(fixtureCase)
            );
            assertEquals(identity.caseId(), fixtureCase.get("case_id").getAsString());

            JsonArray rawAnchors = fixtureCase.getAsJsonArray("anchors");
            assertNotNull(rawAnchors);
            assertEquals(1, rawAnchors.size());
            JsonObject anchor = rawAnchors.get(0).getAsJsonObject();
            Set<String> anchorKeys = keys(anchor);
            assertTrue(
                    Set.of("block_id", "cable_id", "facades", "face_parts", "position")
                            .containsAll(anchorKeys),
                    anchorKeys.toString()
            );
            assertTrue(
                    anchorKeys.containsAll(Set.of("block_id", "cable_id", "position")),
                    anchorKeys.toString()
            );
            assertEquals("ae2:cable_bus", anchor.get("block_id").getAsString());
            Position anchorPosition = position(anchor.get("position"));
            assertEquals(identity.position(), anchorPosition);
            assertTrue(
                    states.put(anchorPosition, exactCableBusState()) == null,
                    anchorPosition.toString()
            );
            Ae2CableBusBlockEntityData cableBus = cableBus(anchor);
            assertTrue(
                    blockEntities.put(anchorPosition, cableBus) == null,
                    anchorPosition.toString()
            );
            anchors.add(new Anchor(anchorPosition, anchor));

            JsonArray helpers = fixtureCase.getAsJsonArray("fixture_blocks");
            assertNotNull(helpers);
            Set<Position> actualFloor = new HashSet<>();
            List<JsonObject> endpoints = new ArrayList<>(1);
            for (JsonElement helperElement : helpers) {
                JsonObject helper = helperElement.getAsJsonObject();
                Position helperPosition = position(helper.get("position"));
                assertTrue(helperPositions.add(helperPosition), helperPosition.toString());
                assertTrue(
                        states.put(helperPosition, blockState(helper)) == null,
                        helperPosition.toString()
                );
                String blockId = helper.get("block_id").getAsString();
                if ("minecraft:smooth_stone".equals(blockId)) {
                    assertEquals(Set.of("block_id", "position"), keys(helper));
                    actualFloor.add(helperPosition);
                    floorCount++;
                } else {
                    endpoints.add(helper);
                }
                if (helper.has("expected_block_entity_id")) {
                    MCABlockEntity entity = new MCABlockEntity();
                    setBlockEntityId(
                            entity,
                            Key.parse(helper.get("expected_block_entity_id").getAsString())
                    );
                    assertTrue(
                            blockEntities.put(helperPosition, entity) == null,
                            helperPosition.toString()
                    );
                }
            }
            assertEquals(expectedLegacyFloor(anchorPosition), actualFloor);
            validateLegacyEndpoint(identity, endpoints, states, blockEntities);
        }

        assertEquals(10, anchors.size());
        assertEquals(92, helperPositions.size());
        assertEquals(90, floorCount);
        assertEquals(102, states.size());
        assertEquals(12, blockEntities.size());
        return new LegacyFixture(states, blockEntities, anchors);
    }

    private static void validateLegacySourceHeader(JsonObject source) {
        assertNotNull(source);
        assertEquals(
                Set.of(
                        "cases_sha256",
                        "cases_size_bytes",
                        "gallery_sha256",
                        "gallery_size_bytes",
                        "signature_schema_version"
                ),
                keys(source)
        );
        assertEquals(
                EXPECTED_SCHEMA9_CASES_SIZE_BYTES,
                source.get("cases_size_bytes").getAsLong()
        );
        assertEquals(
                EXPECTED_SCHEMA9_CASES_SHA256,
                source.get("cases_sha256").getAsString()
        );
        assertEquals(
                EXPECTED_SCHEMA9_GALLERY_SIZE_BYTES,
                source.get("gallery_size_bytes").getAsLong()
        );
        assertEquals(
                EXPECTED_SCHEMA9_GALLERY_SHA256,
                source.get("gallery_sha256").getAsString()
        );
        assertEquals(9, source.get("signature_schema_version").getAsInt());
    }

    private static void validateLegacySyntheticWorld(JsonObject world) {
        assertNotNull(world);
        assertEquals(
                Set.of(
                        "anchor_block_state",
                        "biome",
                        "blocklight",
                        "sunlight",
                        "support_block_state",
                        "support_patch"
                ),
                keys(world)
        );
        assertEquals(
                "ae2:cable_bus[light_level=0,waterlogged=false]",
                world.get("anchor_block_state").getAsString()
        );
        assertEquals("minecraft:plains", world.get("biome").getAsString());
        assertEquals(0, world.get("blocklight").getAsInt());
        assertEquals(15, world.get("sunlight").getAsInt());
        assertEquals(
                "minecraft:smooth_stone",
                world.get("support_block_state").getAsString()
        );
        assertEquals(
                "complete-3x3-plane-one-block-below-each-anchor",
                world.get("support_patch").getAsString()
        );
    }

    private static Set<Position> expectedLegacyFloor(Position anchor) {
        Set<Position> expected = new HashSet<>(9);
        for (int x = anchor.x() - 1; x <= anchor.x() + 1; x++) {
            for (int z = anchor.z() - 1; z <= anchor.z() + 1; z++) {
                expected.add(new Position(x, anchor.y() - 1, z));
            }
        }
        return expected;
    }

    private static void validateLegacyEndpoint(
            LegacyAnchorIdentity identity,
            List<JsonObject> endpoints,
            Map<Position, BlockState> states,
            Map<Position, BlockEntity> blockEntities
    ) {
        String blockId;
        String blockEntityId;
        Map<String, String> properties;
        if ("ae2-m1-02".equals(identity.caseId())) {
            blockId = "ae2:energy_acceptor";
            blockEntityId = "ae2:energy_acceptor";
            properties = Map.of();
        } else if ("ae2-m1-03".equals(identity.caseId())) {
            blockId = "ae2:controller";
            blockEntityId = "ae2:controller";
            properties = Map.of("state", "offline", "type", "block");
        } else {
            assertTrue(endpoints.isEmpty(), identity.caseId());
            return;
        }

        assertEquals(1, endpoints.size());
        JsonObject endpoint = endpoints.get(0);
        assertEquals(
                Set.of("block_id", "expected_block_entity_id", "expected_state", "position"),
                keys(endpoint)
        );
        Position endpointPosition = new Position(
                identity.position().x() + 1,
                identity.position().y(),
                identity.position().z()
        );
        assertEquals(endpointPosition, position(endpoint.get("position")));
        assertEquals(blockId, endpoint.get("block_id").getAsString());
        assertEquals(
                blockEntityId,
                endpoint.get("expected_block_entity_id").getAsString()
        );
        JsonObject rawState = endpoint.getAsJsonObject("expected_state");
        assertNotNull(rawState);
        assertEquals(properties.keySet(), keys(rawState));
        properties.forEach((key, value) ->
                assertEquals(value, rawState.get(key).getAsString())
        );
        assertEquals(
                new BlockState(Key.parse(blockId), properties),
                states.get(endpointPosition)
        );
        assertEquals(
                Key.parse(blockEntityId),
                blockEntities.get(endpointPosition).getId()
        );
    }

    private static Set<String> keys(JsonObject value) {
        Set<String> keys = new HashSet<>();
        value.entrySet().forEach(entry -> keys.add(entry.getKey()));
        return Set.copyOf(keys);
    }

    private static void assertNoLegacyOracleOutputFields(JsonElement value) {
        if (value.isJsonArray()) {
            value.getAsJsonArray().forEach(
                    NativeStructuralOracleExporterTest::assertNoLegacyOracleOutputFields
            );
            return;
        }
        if (!value.isJsonObject()) {
            return;
        }
        for (Map.Entry<String, JsonElement> entry
                : value.getAsJsonObject().entrySet()) {
            assertTrue(
                    !LEGACY_ORACLE_OUTPUT_FIELDS.contains(entry.getKey()),
                    entry.getKey()
            );
            assertNoLegacyOracleOutputFields(entry.getValue());
        }
    }

    private static LegacyAnchorIdentity legacyAnchor(
            String caseId,
            int x,
            int y,
            int z
    ) {
        return new LegacyAnchorIdentity(caseId, new Position(x, y, z));
    }

    private static BlockNeighborhood freshNeighborhood(
            Map<Position, BlockState> states,
            Map<Position, BlockEntity> blockEntities,
            ResourcePack resourcePack,
            Position position
    ) {
        TestBlockAccess currentAccess = new TestBlockAccess(states, blockEntities);
        BlockNeighborhood neighborhood = new BlockNeighborhood(
                currentAccess,
                resourcePack,
                TEST_RENDER_SETTINGS,
                DimensionType.OVERWORLD
        );
        neighborhood.set(position.x(), position.y(), position.z());
        return neighborhood;
    }

    private static Path configured(String name, Path fallback) {
        String configured = System.getenv(name);
        return configured == null || configured.isBlank()
                ? fallback : Path.of(configured);
    }

    private static Path requiredConfigured(String name) {
        String configured = System.getenv(name);
        assertTrue(
                configured != null && !configured.isBlank(),
                name + " must name the exact full-pack input"
        );
        return Path.of(configured);
    }

    private static byte[] readArchiveEntry(Path archive, String name)
            throws IOException {
        try (ZipFile zip = new ZipFile(archive.toFile())) {
            ZipEntry entry = zip.getEntry(name);
            assertTrue(entry != null && !entry.isDirectory(), name);
            try (InputStream stream = zip.getInputStream(entry)) {
                return stream.readAllBytes();
            }
        }
    }

    private static void loadAnyFilteredStructuralTextures(
            ResourcePack pack,
            Path ae2Resources
    ) throws IOException {
        LinkedHashSet<Key> required = new LinkedHashSet<>();
        Ae219217Profile.textures().stream().map(Key::parse).forEach(required::add);
        required.addAll(NativeStructuralResourceModels.requiredTextures());
        required.addAll(M3cQuartzGlassResourceModels.requiredTextures());
        for (Key key : required) {
            if (pack.getTextures().get(key) != null) {
                continue;
            }
            Path png = ae2Resources.resolve("assets")
                    .resolve(key.getNamespace())
                    .resolve("textures")
                    .resolve(key.getValue() + ".png");
            BufferedImage image = ImageIO.read(png.toFile());
            if (image == null) {
                throw new IOException("not a PNG: " + png);
            }
            pack.getTextures().put(key, Texture.from(key, image));
        }
    }

    private static String structuralResourceProblems(ResourcePack pack) {
        List<String> problems = new ArrayList<>();
        if (!NativeStructuralResourceModels.exactRouteContractAvailable()) {
            problems.add("exact route contract unavailable");
        }
        NativeStructuralSemanticResources.Signatures signatures =
                NativeStructuralSemanticResources.signatures(pack);
        if (signatures == null) {
            problems.add("semantic signatures unavailable");
        } else {
            if (!Ae219217NativeStructuralProfile
                    .LIVE_MODEL_SEMANTIC_SIGNATURE_SHA256.equals(signatures.models())) {
                problems.add("model semantic signature " + signatures.models());
            }
            if (!Ae219217NativeStructuralProfile
                    .LIVE_TEXTURE_SEMANTIC_SIGNATURE_SHA256.equals(
                            signatures.textures()
                    )) {
                problems.add("texture semantic signature " + signatures.textures());
            }
        }
        String quartzTextures = NativeStructuralSemanticResources.textureSignature(
                pack,
                M3cQuartzGlassResourceModels.requiredTextures()
        );
        if (!Ae219217NativeStructuralProfile
                .QUARTZ_FACADE_DEPENDENCY_TEXTURE_SEMANTIC_SIGNATURE_SHA256.equals(
                        quartzTextures
                )) {
            problems.add("quartz texture semantic signature " + quartzTextures);
        }
        NativeStructuralResourceModels.requiredTextures().stream()
                .filter(key -> pack.getTextures().get(key) == null)
                .forEach(key -> problems.add("missing texture " + key));
        NativeStructuralPartCatalog.definitions().stream()
                .flatMap(definition -> definition.modelPaths().stream())
                .distinct()
                .filter(path -> pack.getModels().get(new ResourcePath<>(path)) == null)
                .forEach(path -> problems.add("missing model " + path));
        return String.join(", ", problems);
    }

    private static Ae2CableBusBlockEntityData cableBus(JsonObject value)
            throws ReflectiveOperationException {
        Ae2CableBusBlockEntityData data = new Ae2CableBusBlockEntityData();
        setBlockEntityId(data, CABLE_BUS);
        JsonElement cable = value.get("cable_id");
        if (cable != null && !cable.isJsonNull()) {
            setRetained(data, "cable", Map.of("id", cable.getAsString()));
        }
        copyFaceParts(data, value.get("face_parts"));
        copyFacades(data, value.get("facades"));
        return data;
    }

    private static void copyFaceParts(
            Ae2CableBusBlockEntityData data,
            JsonElement parts
    ) throws ReflectiveOperationException {
        if (parts == null || parts.isJsonNull()) {
            return;
        }
        if (parts.isJsonArray()) {
            for (JsonElement element : parts.getAsJsonArray()) {
                JsonObject part = element.getAsJsonObject();
                setRetained(
                        data,
                        part.get("direction").getAsString(),
                        retainedPart(part)
                );
            }
            return;
        }
        for (Map.Entry<String, JsonElement> entry
                : parts.getAsJsonObject().entrySet()) {
            setRetained(
                    data,
                    entry.getKey(),
                    retainedPart(entry.getValue().getAsJsonObject())
            );
        }
    }

    private static Map<String, Object> retainedPart(JsonObject part) {
        Map<String, Object> retained = new LinkedHashMap<>();
        retained.put("id", part.get("id").getAsString());
        if (part.has("spin")) {
            retained.put("spin", part.get("spin").getAsByte());
        }
        if (part.has("freq")) {
            retained.put("freq", (short) part.get("freq").getAsInt());
        }
        return Map.copyOf(retained);
    }

    private static void copyFacades(
            Ae2CableBusBlockEntityData data,
            JsonElement facades
    ) throws ReflectiveOperationException {
        if (facades == null || facades.isJsonNull()) {
            return;
        }
        if (facades.isJsonArray()) {
            for (JsonElement element : facades.getAsJsonArray()) {
                JsonObject facade = element.getAsJsonObject();
                setFacade(
                        data,
                        facade.get("direction").getAsString(),
                        facade.getAsJsonObject("block_state")
                );
            }
            return;
        }
        for (Map.Entry<String, JsonElement> entry
                : facades.getAsJsonObject().entrySet()) {
            setFacade(data, entry.getKey(), entry.getValue().getAsJsonObject());
        }
    }

    private static void setFacade(
            Ae2CableBusBlockEntityData data,
            String direction,
            JsonObject facade
    ) throws ReflectiveOperationException {
        Map<String, Object> retained = new LinkedHashMap<>();
        retained.put("Name", facade.get("Name").getAsString());
        if (facade.has("Properties")) {
            Map<String, String> properties = new LinkedHashMap<>();
            facade.getAsJsonObject("Properties").entrySet().forEach(property ->
                    properties.put(
                            property.getKey(),
                            property.getValue().getAsString()
                    )
            );
            retained.put("Properties", Map.copyOf(properties));
        }
        setRetained(
                data,
                "facade" + direction.substring(0, 1).toUpperCase(Locale.ROOT)
                        + direction.substring(1),
                Map.copyOf(retained)
        );
    }

    private static void setRetained(
            Ae2CableBusBlockEntityData data,
            String name,
            Object value
    ) throws ReflectiveOperationException {
        Field field = Ae2CableBusBlockEntityData.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(data, value);
    }

    private static void setBlockEntityId(
            MCABlockEntity data,
            Key id
    ) throws ReflectiveOperationException {
        Field field = MCABlockEntity.class.getDeclaredField("id");
        field.setAccessible(true);
        field.set(data, id);
    }

    private static Position position(JsonElement value) {
        JsonObject position = value.getAsJsonObject();
        return new Position(
                position.get("x").getAsInt(),
                position.get("y").getAsInt(),
                position.get("z").getAsInt()
        );
    }

    private static BlockState blockState(JsonObject value) {
        String blockId = value.get("block_id").getAsString();
        JsonObject state = value.getAsJsonObject("expected_state");
        if (state == null || state.size() == 0) {
            return BlockState.fromString(blockId);
        }
        Map<String, String> properties = new LinkedHashMap<>();
        state.entrySet().forEach(entry ->
                properties.put(entry.getKey(), entry.getValue().getAsString())
        );
        return new BlockState(Key.parse(blockId), Map.copyOf(properties));
    }

    private static BlockState exactCableBusState() {
        return BlockState.fromString(
                "ae2:cable_bus[light_level=0,waterlogged=false]"
        );
    }

    private static String sha256(Path path) throws Exception {
        return sha256(Files.readAllBytes(path));
    }

    private static String sha256(byte[] value) throws Exception {
        return digest("SHA-256", value);
    }

    private static String sha1(Path path) throws Exception {
        return digest("SHA-1", Files.readAllBytes(path));
    }

    private static String sha512(Path path) throws Exception {
        return digest("SHA-512", Files.readAllBytes(path));
    }

    private static String digest(String algorithm, byte[] value) throws Exception {
        byte[] digest = java.security.MessageDigest.getInstance(algorithm)
                .digest(value);
        return java.util.HexFormat.of().formatHex(digest);
    }

    private static final RenderSettings TEST_RENDER_SETTINGS = new RenderSettings() {
        @Override
        public int getRemoveCavesBelowY() {
            return Integer.MIN_VALUE;
        }

        @Override
        public int getCaveDetectionOceanFloor() {
            return 0;
        }

        @Override
        public boolean isCaveDetectionUsesBlockLight() {
            return false;
        }

        @Override
        public float getAmbientLight() {
            return 0F;
        }

        @Override
        public boolean isRenderEdges() {
            return false;
        }

        @Override
        public Mask getRenderMask() {
            return Mask.ALL;
        }

        @Override
        public boolean isSaveHiresLayer() {
            return false;
        }

        @Override
        public boolean isRenderTopOnly() {
            return false;
        }
    };

    private static final class TestBlockAccess implements BlockAccess {
        private final Map<Position, BlockState> states;
        private final Map<Position, BlockEntity> blockEntities;
        private int x;
        private int y;
        private int z;

        private TestBlockAccess(
                Map<Position, BlockState> states,
                Map<Position, BlockEntity> blockEntities
        ) {
            this.states = states;
            this.blockEntities = blockEntities;
        }

        @Override
        public void set(int newX, int newY, int newZ) {
            x = newX;
            y = newY;
            z = newZ;
        }

        @Override
        public BlockAccess copy() {
            return new TestBlockAccess(states, blockEntities);
        }

        @Override
        public int getX() {
            return x;
        }

        @Override
        public int getY() {
            return y;
        }

        @Override
        public int getZ() {
            return z;
        }

        @Override
        public BlockState getBlockState() {
            return states.getOrDefault(new Position(x, y, z), BlockState.AIR);
        }

        @Override
        public LightData getLightData() {
            return new LightData(15, 0);
        }

        @Override
        public Biome getBiome() {
            return PLAINS;
        }

        @Override
        public BlockEntity getBlockEntity() {
            return blockEntities.get(new Position(x, y, z));
        }

        @Override
        public boolean hasOceanFloorY() {
            return false;
        }

        @Override
        public int getOceanFloorY() {
            return 0;
        }
    }

    private static final Biome PLAINS = new Biome() {
        private final Color water = new Color().set(4159204 | 0xFF000000).premultiplied();
        private final Color overlay = new Color().premultiplied();

        @Override
        public Key getKey() {
            return Key.parse("minecraft:plains");
        }

        @Override
        public float getDownfall() {
            return 0.4F;
        }

        @Override
        public float getTemperature() {
            return 0.8F;
        }

        @Override
        public Color getWaterColor() {
            return water;
        }

        @Override
        public Color getOverlayFoliageColor() {
            return overlay;
        }

        @Override
        public Color getOverlayDryFoliageColor() {
            return overlay;
        }

        @Override
        public Color getOverlayGrassColor() {
            return overlay;
        }

        @Override
        public de.bluecolored.bluemap.core.world.biome.GrassColorModifier
                getGrassColorModifier() {
            return de.bluecolored.bluemap.core.world.biome.GrassColorModifier.NONE;
        }
    };

    private record Position(int x, int y, int z) {
        String key() {
            return x + " " + y + " " + z;
        }
    }

    private record Anchor(Position position, JsonObject value) {
    }

    private record LegacyAnchorIdentity(String caseId, Position position) {
    }

    private record LegacyFixture(
            Map<Position, BlockState> states,
            Map<Position, BlockEntity> blockEntities,
            List<Anchor> anchors
    ) {
    }

    private record RenderOutput(
            ArrayTileModel model,
            JsonObject ranges,
            int triangleCount
    ) {
    }
}
