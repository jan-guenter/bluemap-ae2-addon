/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.adapter.bluemap522;

import com.flowpowered.math.vector.Vector3f;
import com.flowpowered.math.vector.Vector4f;
import de.bluecolored.bluemap.core.map.TextureGallery;
import de.bluecolored.bluemap.core.map.hires.ArrayTileModel;
import de.bluecolored.bluemap.core.map.hires.MaxCapacityReachedException;
import de.bluecolored.bluemap.core.map.hires.RenderSettings;
import de.bluecolored.bluemap.core.map.hires.TileModelView;
import de.bluecolored.bluemap.core.map.mask.Mask;
import de.bluecolored.bluemap.core.resources.ResourcePath;
import de.bluecolored.bluemap.core.resources.adapter.ResourcesGson;
import de.bluecolored.bluemap.core.resources.pack.PackVersion;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.Variant;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.VariantSet;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.Variants;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.model.Element;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.model.Face;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.model.Model;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.model.Rotation;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.model.TextureVariable;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.texture.Texture;
import de.bluecolored.bluemap.core.util.Direction;
import de.bluecolored.bluemap.core.util.Key;
import de.bluecolored.bluemap.core.util.math.Axis;
import de.bluecolored.bluemap.core.util.math.Color;
import de.bluecolored.bluemap.core.world.BlockEntity;
import de.bluecolored.bluemap.core.world.BlockState;
import de.bluecolored.bluemap.core.world.DimensionType;
import de.bluecolored.bluemap.core.world.LightData;
import de.bluecolored.bluemap.core.world.biome.Biome;
import de.bluecolored.bluemap.core.world.block.BlockAccess;
import de.bluecolored.bluemap.core.world.block.BlockNeighborhood;
import de.bluecolored.bluemap.core.world.mca.blockentity.MCABlockEntity;
import io.github.janguenter.bluemap.ae2.activation.ExtensionRouteActivation;
import io.github.janguenter.bluemap.ae2.activation.NativeStructuralRouteActivation;
import io.github.janguenter.bluemap.ae2.activation.ProfileActivation;
import io.github.janguenter.bluemap.ae2.model.Ae2CableCatalog;
import io.github.janguenter.bluemap.ae2.model.CableFamily;
import io.github.janguenter.bluemap.ae2.model.Direction6;
import io.github.janguenter.bluemap.ae2.model.FacadeGeometry;
import io.github.janguenter.bluemap.ae2.model.FacadeSnapshot;
import io.github.janguenter.bluemap.ae2.model.NativePartGeometry;
import io.github.janguenter.bluemap.ae2.model.NativeStructuralPartCatalog;
import io.github.janguenter.bluemap.ae2.profile.Ae219217NativeStructuralProfile;
import io.github.janguenter.bluemap.ae2.profile.Ae219217Profile;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CableBusRendererTest {

    private static final float MESH_EPSILON = 0.000001F;
    private static final int X = 1;
    private static final int Y = 64;
    private static final int Z = 1;
    private static final Key CORE = Key.parse(Ae219217Profile.CORE_TEXTURE);
    private static final Key COVERED_CORE =
            Key.parse("ae2:part/cable/core/covered/transparent");
    private static final Key CONNECTION = Key.parse(Ae219217Profile.CONNECTION_TEXTURE);
    private static final Key ORIGINAL = Key.parse("test:block/original");
    private static final Key EXTENSION_PART_TEXTURE =
            Key.parse("test:part/extension");
    private static final Key CABLE_BUS = Key.parse(Ae219217Profile.CABLE_BUS_BLOCK);

    @Test
    void emitsTheIsolatedCoreWithExactUnitsTextureLightAoAndMapColor() throws Exception {
        Fixture fixture = fixture(false, false, true);
        RecordingTileModel model = new RecordingTileModel();
        Color mapColor = new Color();

        fixture.renderer().render(
                fixture.neighborhood(),
                null,
                new TileModelView(model),
                mapColor
        );

        assertEquals(12, model.size());
        int coreMaterial = fixture.gallery().get(CORE);
        for (int face = 0; face < model.size(); face++) {
            assertEquals(coreMaterial, model.face(face).material());
        }
        assertArrayEquals(new float[]{
                0.375F, 0.375F, 0.625F,
                0.375F, 0.375F, 0.375F,
                0.625F, 0.375F, 0.375F
        }, model.face(0).positions(), 0F);
        assertArrayEquals(new float[]{
                0.375F, 0.375F,
                0.375F, 0.625F,
                0.625F, 0.625F
        }, model.face(0).uvs(), 0F);
        assertArrayEquals(new float[]{1F, 1F, 1F}, model.face(2).aos(), 0F);
        assertEquals(5, model.face(2).sunlight());
        assertEquals(4, model.face(2).blocklight());
        assertEquals(1F / 3F, mapColor.r, 0.00001F);
        assertEquals(1F / 3F, mapColor.g, 0.00001F);
        assertEquals(1F / 3F, mapColor.b, 0.00001F);
        assertEquals(1F, mapColor.a, 0F);
    }

    @Test
    void simplifiesTwoOppositeExactNeighborsToOneCaplessStraightPrism() throws Exception {
        Fixture fixture = fixture(true, true, true);
        RecordingTileModel model = new RecordingTileModel();

        fixture.renderer().render(
                fixture.neighborhood(),
                null,
                new TileModelView(model),
                new Color()
        );

        assertEquals(8, model.size());
        int connectionMaterial = fixture.gallery().get(CONNECTION);
        for (int face = 0; face < model.size(); face++) {
            assertEquals(connectionMaterial, model.face(face).material());
        }
    }

    @Test
    void unknownCableNeighborUsesOnlyTheWholeStockBlockFallback() throws Exception {
        Fixture fixture = fixture(false, true, false);
        RecordingTileModel model = new RecordingTileModel();

        fixture.renderer().render(
                fixture.neighborhood(),
                null,
                new TileModelView(model),
                new Color()
        );

        assertEquals(10, model.size());
        int originalMaterial = fixture.gallery().get(ORIGINAL);
        for (int face = 0; face < model.size(); face++) {
            assertEquals(originalMaterial, model.face(face).material());
        }
    }

    @Test
    void incompatibleDyedNeighborsRemainCustomRenderedButDisconnected() throws Exception {
        Fixture fixture = fixture(
                "ae2:red_covered_cable",
                null,
                "ae2:blue_smart_dense_cable",
                true
        );

        RecordingTileModel model = render(fixture);

        assertEquals(12, model.size());
        int core = fixture.gallery().get(Key.parse(
                "ae2:part/cable/core/covered/red"
        ));
        for (int face = 0; face < model.size(); face++) {
            assertEquals(core, model.face(face).material());
        }
    }

    @Test
    void equalDyedCrossFamilyNeighborsConnectThroughTheVisibleFamilyRule()
            throws Exception {
        Fixture fixture = fixture(
                "ae2:red_covered_cable",
                null,
                "ae2:red_smart_cable",
                true
        );

        RecordingTileModel model = render(fixture);

        assertEquals(22, model.size());
        assertEquals(
                fixture.gallery().get(Key.parse("ae2:part/cable/covered/red")),
                model.face(12).material()
        );
    }

    @Test
    void compatibleSmartArmUsesOffOverlaysTintAndFullBright() throws Exception {
        Fixture fixture = fixture(
                "ae2:fluix_smart_dense_cable",
                null,
                "ae2:red_smart_cable",
                true
        );

        RecordingTileModel model = render(fixture);

        assertEquals(42, model.size());
        assertEquals(
                fixture.gallery().get(Key.parse("ae2:part/cable/smart/transparent")),
                model.face(12).material()
        );
        assertEquals(
                fixture.gallery().get(Key.parse(Ae2CableCatalog.SMART_CHANNELS_OFF_ODD)),
                model.face(22).material()
        );
        assertArrayEquals(
                new float[]{0x5a / 255F, 0x47 / 255F, 0x9e / 255F},
                model.face(22).color(),
                0F
        );
        assertEquals(15, model.face(22).sunlight());
        assertEquals(15, model.face(22).blocklight());
        assertEquals(
                fixture.gallery().get(Key.parse(Ae2CableCatalog.SMART_CHANNELS_OFF_EVEN)),
                model.face(32).material()
        );
        assertArrayEquals(
                new float[]{0xe2 / 255F, 0xa3 / 255F, 0xe3 / 255F},
                model.face(32).color(),
                0F
        );
    }

    @Test
    void allTwentyFiveCompatibleFamilyPairsUseTheExactVisibleHalfArm() throws Exception {
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
                String localId = "ae2:fluix_" + families[local].idSuffix();
                String neighborId = "ae2:fluix_" + families[neighbor].idSuffix();
                CableFamily arm = visible[local][neighbor];
                Fixture fixture = fixture(localId, null, neighborId, true);

                RecordingTileModel model = render(fixture);

                assertEquals(arm.isSmart() ? 42 : 22, model.size(),
                        families[local] + " -> " + families[neighbor]);
                assertEquals(
                        fixture.gallery().get(Key.parse(
                                Ae2CableCatalog.require(localId).connectionTexture(arm)
                        )),
                        model.face(12).material()
                );
            }
        }
    }

    @Test
    void missingSelectedTextureFallsBackBeforeEmittingCustomGeometry() throws Exception {
        Fixture fixture = fixture(
                "ae2:fluix_smart_cable",
                null,
                "ae2:fluix_smart_cable",
                true
        );
        fixture.resourcePack().getTextures().remove(
                Key.parse(Ae2CableCatalog.SMART_CHANNELS_OFF_EVEN)
        );

        RecordingTileModel model = render(fixture);

        assertEquals(10, model.size());
        int originalMaterial = fixture.gallery().get(ORIGINAL);
        for (int face = 0; face < model.size(); face++) {
            assertEquals(originalMaterial, model.face(face).material());
        }
        assertTrue(fixture.activation().isActive());
    }

    @Test
    void clearsOnlyAirOrNonAe2NeighborsWithoutBlockEntities() throws Exception {
        Position east = new Position(X + 1, Y, Z);

        Fixture terrain = fixture(false, false, true);
        terrain.states().put(east, BlockState.fromString("minecraft:stone"));
        RecordingTileModel terrainModel = render(terrain);
        assertEquals(terrain.gallery().get(CORE), terrainModel.face(0).material());

        Fixture ae2Device = fixture(false, false, true);
        ae2Device.states().put(east, BlockState.fromString("ae2:controller"));
        RecordingTileModel ae2Model = render(ae2Device);
        assertEquals(ae2Device.gallery().get(ORIGINAL), ae2Model.face(0).material());

        Fixture blockEntityEndpoint = fixture(false, false, true);
        blockEntityEndpoint.states().put(east, BlockState.fromString("example:endpoint"));
        blockEntityEndpoint.blockEntities().put(east, new Ae2CableBusBlockEntityData());
        RecordingTileModel endpointModel = render(blockEntityEndpoint);
        assertEquals(
                blockEntityEndpoint.gallery().get(ORIGINAL),
                endpointModel.face(0).material()
        );
    }

    @Test
    void recognizedCenterWithFacePartOrFacadeUsesWholeStockFallback() throws Exception {
        for (String retainedField : List.of("north", "facadeSouth")) {
            Fixture fixture = fixture(false, false, true);
            fixture.blockEntities().put(
                    new Position(X, Y, Z),
                    cableBusWithRetainedField(retainedField)
            );

            RecordingTileModel model = render(fixture);
            assertEquals(12, model.size());
            int originalMaterial = fixture.gallery().get(ORIGINAL);
            for (int face = 0; face < model.size(); face++) {
                assertEquals(originalMaterial, model.face(face).material());
            }
            assertTrue(fixture.activation().isActive(), fixture.activation().reason());
        }
    }

    @Test
    void terminalUsesTheInstalledModelsConstrainedArmAndExactCableTints()
            throws Exception {
        Fixture fixture = fixture(false, false, true);
        fixture.blockEntities().put(
                new Position(X, Y, Z),
                cableBusWithTerminals(
                        "ae2:fluix_glass_cable",
                        Map.of(Direction.NORTH, 0),
                        null
                )
        );

        RecordingTileModel model = render(fixture);

        assertEquals(68, model.size());
        assertArrayEquals(
                rgb(0xe2a3e3),
                model.face(46).color(),
                0F
        );
        assertArrayEquals(
                rgb(0x915dcd),
                model.face(48).color(),
                0F
        );
        assertArrayEquals(
                rgb(0x5a479e),
                model.face(50).color(),
                0F
        );
        assertTrue(fixture.activation().isActive(), fixture.activation().reason());
    }

    @Test
    void multipleTerminalFacesDisableStraightSimplificationAndRemainIndependent()
            throws Exception {
        Fixture fixture = fixture(true, true, true);
        fixture.blockEntities().put(
                new Position(X, Y, Z),
                cableBusWithTerminals(
                        "ae2:fluix_glass_cable",
                        Map.of(Direction.NORTH, 1, Direction.UP, 3),
                        null
                )
        );

        RecordingTileModel model = render(fixture);

        assertEquals(144, model.size());
        assertTrue(fixture.activation().isActive(), fixture.activation().reason());
    }

    @Test
    void sameFacePlainStoneFacadeAddsTheExactFourStripRing() throws Exception {
        Fixture fixture = fixture(false, false, true);
        fixture.blockEntities().put(
                new Position(X, Y, Z),
                cableBusWithTerminals(
                        "ae2:fluix_glass_cable",
                        Map.of(Direction.SOUTH, 1),
                        Direction.SOUTH
                )
        );

        RecordingTileModel model = render(fixture);

        assertEquals(116, model.size());
        int stone = fixture.gallery().get(M2ResourceModels.STONE_TEXTURE);
        for (int face = 68; face < 116; face++) {
            assertEquals(stone, model.face(face).material());
        }
        assertTrue(fixture.activation().isActive(), fixture.activation().reason());
    }

    @Test
    void fullySurroundedNativeQuartzFacadesKeepCenterAndPartWithoutMaterialLeak()
            throws Exception {
        for (String facadeId : List.of(
                "ae2:quartz_glass",
                "ae2:quartz_vibrant_glass"
        )) {
            Fixture fixture = fixture(false, false, true);
            for (Key texture : M3cQuartzGlassResourceModels.requiredTextures()) {
                putTexture(fixture.resourcePack(), texture, 0xFFFFFFFF);
            }
            fixture.gallery().put(fixture.resourcePack().getTextures());
            Ae2CableBusBlockEntityData data = cableBusWithTerminals(
                    "ae2:fluix_glass_cable",
                    Map.of(Direction.UP, 0),
                    null
            );
            setRetainedField(data, "facadeUp", Map.of("Name", facadeId));
            fixture.blockEntities().put(new Position(X, Y, Z), data);
            for (Direction6 direction : Direction6.values()) {
                fixture.states().put(
                        new Position(
                                X + direction.stepX(),
                                Y + direction.stepY(),
                                Z + direction.stepZ()
                        ),
                        BlockState.fromString(facadeId)
                );
            }
            NativeRuntime runtime = nativeRuntime(fixture);

            RecordingTileModel model = render(runtime.renderer(), fixture);

            assertEquals(68, model.size(), facadeId);
            java.util.Set<Integer> quartzMaterials =
                    M3cQuartzGlassResourceModels.requiredTextures().stream()
                            .map(fixture.gallery()::get)
                            .collect(java.util.stream.Collectors.toSet());
            for (int face = 0; face < model.size(); face++) {
                assertTrue(
                        !quartzMaterials.contains(model.face(face).material()),
                        facadeId + "/" + face
                );
            }
            assertTrue(runtime.activation().isActive(), runtime.activation().reason());

            RecordingTileModel capacity = new RecordingTileModel();
            capacity.failWithCapacityOnAddInvocation(1);
            assertThrows(
                    MaxCapacityReachedException.class,
                    () -> runtime.renderer().render(
                            fixture.neighborhood(),
                            null,
                            new TileModelView(capacity),
                            new Color()
                    )
            );
            assertTrue(runtime.activation().isActive(), runtime.activation().reason());
        }
    }

    @Test
    void nativeFacadeCarriesSourceAoAndLightEmissionIntoEveryTriangle()
            throws Exception {
        Fixture fixture = fixture(false, false, true);
        Key facadeId = Key.parse("test:emissive_facade");
        Key facadeTexture = Key.parse("test:block/emissive_facade");
        putFacadeResource(
                fixture.resourcePack(),
                facadeId,
                facadeTexture,
                false,
                7
        );
        fixture.gallery().put(fixture.resourcePack().getTextures());
        Ae2CableBusBlockEntityData data = cableBusWithTerminals(
                "ae2:fluix_glass_cable",
                Map.of(Direction.UP, 0),
                null
        );
        setRetainedField(data, "facadeUp", Map.of("Name", facadeId.getFormatted()));
        fixture.blockEntities().put(new Position(X, Y, Z), data);
        NativeRuntime runtime = nativeRuntime(fixture);

        RecordingTileModel model = render(runtime.renderer(), fixture);

        int material = fixture.gallery().get(facadeTexture);
        int facadeTriangles = 0;
        for (int face = 0; face < model.size(); face++) {
            if (model.face(face).material() != material) {
                continue;
            }
            assertEquals(7, model.face(face).blocklight());
            assertArrayEquals(new float[]{1F, 1F, 1F}, model.face(face).aos(), 0F);
            facadeTriangles++;
        }
        assertEquals(48, facadeTriangles);
        assertTrue(runtime.activation().isActive(), runtime.activation().reason());
    }

    @Test
    void nativePartCableTintAndMapColorSurviveTopOnlyAndCaveFaceFiltering()
            throws Exception {
        for (RenderSettings settings : List.of(
                TEST_RENDER_SETTINGS,
                settings(true, Integer.MIN_VALUE, false),
                settings(false, Y + 1, false)
        )) {
            Fixture fixture = fixture(
                    "ae2:fluix_glass_cable",
                    null,
                    null,
                    true,
                    settings
            );
            fixture.blockEntities().put(
                    new Position(X, Y, Z),
                    cableBusWithTerminals(
                            "ae2:fluix_glass_cable",
                            Map.of(Direction.UP, 0),
                            null
                    )
            );
            fixture.lights().put(new Position(X, Y, Z), new LightData(0, 0));
            fixture.lights().put(new Position(X, Y + 1, Z), new LightData(15, 0));
            for (Key texture : List.copyOf(fixture.resourcePack().getTextures().keySet())) {
                putTexture(fixture.resourcePack(), texture, 0x00000000);
            }
            putTexture(
                    fixture.resourcePack(),
                    Key.parse("ae2:part/terminal_bright"),
                    0xffffffff
            );
            putTexture(
                    fixture.resourcePack(),
                    Key.parse("ae2:part/terminal_medium"),
                    0xffffffff
            );
            putTexture(
                    fixture.resourcePack(),
                    Key.parse("ae2:part/terminal_dark"),
                    0xffffffff
            );
            NativeRuntime runtime = nativeRuntime(fixture);

            RecordingTileModel model = new RecordingTileModel();
            Color mapColor = new Color();
            runtime.renderer().render(
                    fixture.neighborhood(),
                    null,
                    new TileModelView(model),
                    mapColor
            );

            assertTintedMaterial(
                    model,
                    fixture.gallery().get(Key.parse("ae2:part/terminal_bright")),
                    rgb(0xe2a3e3)
            );
            assertTintedMaterial(
                    model,
                    fixture.gallery().get(Key.parse("ae2:part/terminal_medium")),
                    rgb(0x915dcd)
            );
            assertTintedMaterial(
                    model,
                    fixture.gallery().get(Key.parse("ae2:part/terminal_dark")),
                    rgb(0x5a479e)
            );
            assertEquals((226F + 145F + 90F) / (3F * 255F), mapColor.r, 0.00001F);
            assertEquals((163F + 93F + 71F) / (3F * 255F), mapColor.g, 0.00001F);
            assertEquals((227F + 205F + 158F) / (3F * 255F), mapColor.b, 0.00001F);
            assertEquals(1F, mapColor.a, 0F);
            assertTrue(runtime.activation().isActive(), runtime.activation().reason());
        }
    }

    @Test
    void nativeGlassEmittersUseCoveredCoreMaterialOnEveryInstalledFace()
            throws Exception {
        int states = 0;
        for (String emitter : List.of(
                "ae2:level_emitter",
                "ae2:energy_level_emitter"
        )) {
            for (Direction6 direction : Direction6.values()) {
                Fixture fixture = fixture(false, false, true);
                putLevelEmitterResources(fixture);
                fixture.blockEntities().put(
                        new Position(X, Y, Z),
                        cableBusWithFacePart(
                                "ae2:fluix_glass_cable",
                                direction,
                                emitter
                        )
                );
                NativeRuntime runtime = nativeRuntime(fixture);

                RecordingTileModel model = render(runtime.renderer(), fixture);

                String message = emitter + "/" + direction;
                assertEquals(12, model.size(), message);
                assertEquals(
                        12,
                        materialCount(model, fixture.gallery().get(COVERED_CORE)),
                        message
                );
                assertEquals(
                        0,
                        materialCount(model, fixture.gallery().get(CORE)),
                        message
                );
                assertTrue(runtime.activation().isActive(), runtime.activation().reason());
                states++;
            }
        }
        assertEquals(12, states);
    }

    @Test
    void storageBusReachesTheQioDashboardSeamWithoutChangingItsExactMesh()
            throws Exception {
        assertNativeStorageBusSeam(
                Direction6.EAST,
                "mekanism:qio_dashboard"
        );
    }

    @Test
    void storageBusReachesTheRadioactiveWasteBarrelSeamWithoutChangingItsExactMesh()
            throws Exception {
        assertNativeStorageBusSeam(
                Direction6.UP,
                "mekanism:radioactive_waste_barrel"
        );
    }

    @Test
    void missingPromotedEmitterCoreTextureFallsBackBeforeCustomEmission()
            throws Exception {
        Fixture fixture = fixture(false, false, true);
        putLevelEmitterResources(fixture);
        fixture.resourcePack().getTextures().remove(COVERED_CORE);
        fixture.blockEntities().put(
                new Position(X, Y, Z),
                cableBusWithFacePart(
                        "ae2:fluix_glass_cable",
                        Direction6.UP,
                        "ae2:level_emitter"
                )
        );
        NativeRuntime runtime = nativeRuntime(fixture);

        RecordingTileModel model = render(runtime.renderer(), fixture);

        assertOriginalOnly(model, fixture, "missing promoted emitter core texture");
        assertEquals(0, materialCount(model, fixture.gallery().get(CORE)));
        assertTrue(runtime.activation().isActive(), runtime.activation().reason());
    }

    @Test
    void nativePartMapColorIgnoresEmissionWhileTrianglesRetainIt() throws Exception {
        Fixture fixture = fixture(false, false, true);
        fixture.blockEntities().put(
                new Position(X, Y, Z),
                cableBusWithTerminals(
                        "ae2:fluix_glass_cable",
                        Map.of(Direction.UP, 0),
                        null
                )
        );
        fixture.lights().put(new Position(X, Y, Z), new LightData(0, 0));
        fixture.lights().put(new Position(X, Y + 1, Z), new LightData(0, 0));
        for (Key texture : List.copyOf(fixture.resourcePack().getTextures().keySet())) {
            putTexture(fixture.resourcePack(), texture, 0x00000000);
        }
        for (String texture : List.of(
                "ae2:part/terminal_bright",
                "ae2:part/terminal_medium",
                "ae2:part/terminal_dark"
        )) {
            putTexture(fixture.resourcePack(), Key.parse(texture), 0xffffffff);
        }
        Key terminal = Key.parse("ae2:part/terminal_off");
        Model source = fixture.resourcePack().getModels().get(terminal);
        Element[] emissive = Arrays.stream(source.getElements())
                .map(element -> new Element(
                        element.getFrom(),
                        element.getTo(),
                        element.getRotation(),
                        element.isShade(),
                        7,
                        element.getFaces()
                ))
                .toArray(Element[]::new);
        Model projected = new Model(source.getTextures(), emissive, source.isAmbientocclusion());
        projected.calculateProperties(fixture.resourcePack().getTextures());
        fixture.resourcePack().getModels().put(terminal, projected);
        NativeRuntime runtime = nativeRuntime(fixture);

        RecordingTileModel model = new RecordingTileModel();
        Color mapColor = new Color();
        runtime.renderer().render(
                fixture.neighborhood(),
                null,
                new TileModelView(model),
                mapColor
        );

        assertEquals(0F, mapColor.r, 0F);
        assertEquals(0F, mapColor.g, 0F);
        assertEquals(0F, mapColor.b, 0F);
        assertEquals(1F, mapColor.a, 0F);
        boolean triangleEmissionRetained = false;
        for (int face = 0; face < model.size(); face++) {
            triangleEmissionRetained |= model.face(face).blocklight() == 7;
        }
        assertTrue(
                triangleEmissionRetained,
                "element light emission must remain on high-resolution triangles"
        );
        assertTrue(runtime.activation().isActive(), runtime.activation().reason());
    }

    @Test
    void topOnlyFacadeUsesContinuousSlantedSourceNormalForGeometryAndMapColor()
            throws Exception {
        Fixture fixture = fixture(
                "ae2:fluix_glass_cable",
                null,
                null,
                true,
                settings(true, Integer.MIN_VALUE, false)
        );
        for (Key texture : List.copyOf(fixture.resourcePack().getTextures().keySet())) {
            putTexture(fixture.resourcePack(), texture, 0x00000000);
        }
        Key blockId = Key.parse("test:slanted_facade");
        Key transparentTexture = Key.parse("test:block/slanted_facade_transparent");
        Key slantedTexture = Key.parse("test:block/slanted_facade_visible");
        putTexture(fixture.resourcePack(), transparentTexture, 0x00000000);
        putTexture(fixture.resourcePack(), slantedTexture, 0xffffffff);

        EnumMap<Direction, Face> cubeFaces = new EnumMap<>(Direction.class);
        for (Direction direction : Direction.values()) {
            cubeFaces.put(direction, new Face(
                    new Vector4f(0, 0, 16, 16),
                    new TextureVariable(new ResourcePath<Texture>(transparentTexture)),
                    direction,
                    0,
                    -1
            ));
        }
        EnumMap<Direction, Face> slantedFaces = new EnumMap<>(Direction.class);
        slantedFaces.put(Direction.NORTH, new Face(
                new Vector4f(0, 0, 16, 16),
                new TextureVariable(new ResourcePath<Texture>(slantedTexture)),
                null,
                0,
                -1
        ));
        Element cube = new Element(
                Vector3f.ZERO,
                new Vector3f(16, 16, 16),
                cubeFaces
        );
        Element slanted = new Element(
                new Vector3f(0, 5, -2.81F),
                new Vector3f(16, 5.1F, -1.81F),
                new Rotation(new Vector3f(8, 8, 8), Axis.X, 60F, false),
                true,
                0,
                slantedFaces
        );
        Key modelId = Key.parse("test:block/slanted_facade_model");
        Model resource = new Model(Map.of(), new Element[]{cube, slanted}, true);
        resource.calculateProperties(fixture.resourcePack().getTextures());
        fixture.resourcePack().getModels().put(modelId, resource);
        fixture.resourcePack().getBlockStates().put(
                blockId,
                new de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.BlockState(
                        new Variants(
                                new VariantSet[0],
                                new VariantSet(new Variant(new ResourcePath<Model>(modelId)))
                        )
                )
        );
        fixture.gallery().put(fixture.resourcePack().getTextures());
        NativeFacadeResourceModels.FacadeMaterial projected =
                NativeFacadeResourceModels.resolve(
                        fixture.resourcePack(),
                        new FacadeSnapshot(blockId.getFormatted(), Map.of()),
                        X,
                        Y,
                        Z
                );
        assertNotNull(projected);
        NativeFacadeResourceModels.FacadeLayer slantedLayer =
                projected.layers().stream()
                        .filter(layer -> layer.texture().equals(slantedTexture))
                        .findFirst()
                        .orElseThrow();
        assertEquals(Direction6.UP, slantedLayer.nominalFace());
        assertEquals(Direction6.NORTH, slantedLayer.lightFace());
        assertEquals(1, FacadeGeometry.clip(
                slantedLayer.nominalFace(),
                slantedLayer.sourceVertices(),
                Direction6.NORTH,
                null,
                0,
                !projected.opaque()
        ).size());
        Ae2CableBusBlockEntityData data = cableBus("ae2:fluix_glass_cable");
        setRetainedField(data, "facadeNorth", Map.of("Name", blockId.getFormatted()));
        fixture.blockEntities().put(new Position(X, Y, Z), data);
        fixture.lights().put(new Position(X, Y, Z), new LightData(15, 0));
        fixture.states().put(
                new Position(X + 1, Y, Z - 1),
                BlockState.fromString("minecraft:stone")
        );
        NativeRuntime runtime = nativeRuntime(fixture);

        RecordingTileModel model = new RecordingTileModel();
        Color mapColor = new Color();
        runtime.renderer().render(
                fixture.neighborhood(),
                null,
                new TileModelView(model),
                mapColor
        );

        int material = fixture.gallery().get(slantedTexture);
        int triangles = 0;
        boolean sourceFaceAoApplied = false;
        for (int face = 0; face < model.size(); face++) {
            if (model.face(face).material() == material) {
                triangles++;
                for (float ao : model.face(face).aos()) {
                    sourceFaceAoApplied |= ao < 1F;
                }
            }
        }
        assertEquals(2, triangles);
        assertTrue(sourceFaceAoApplied, "NORTH source-face AO must shade the slant");
        assertEquals(1F, mapColor.r, 0F);
        assertEquals(1F, mapColor.g, 0F);
        assertEquals(1F, mapColor.b, 0F);
        assertEquals(1F, mapColor.a, 0F);
        assertTrue(runtime.activation().isActive(), runtime.activation().reason());
    }

    @Test
    void extensionEndpointCompatibilityRequiresTheExactBlockEntityPair()
            throws Exception {
        Position east = new Position(X + 1, Y, Z);
        String extensionId = Ae219217NativeStructuralProfile
                .knownUnsupportedCompatibleEndpointIds().getFirst();
        String extensionBlockEntityId = Ae219217NativeStructuralProfile
                .knownUnsupportedCompatibleEndpointBlockEntityId(extensionId);

        Fixture ordinary = fixture(false, false, true);
        ordinary.states().put(east, BlockState.fromString("minecraft:stone"));
        ordinary.blockEntities().put(
                east,
                new TestBlockEntity(Key.parse(extensionBlockEntityId), east)
        );
        assertEquals(
                CableBusRenderer.UnsupportedEndpointObservation.UNRELATED,
                CableBusRenderer.classifyUnsupportedEndpoint(
                        "minecraft:stone",
                        ordinary.neighborhood().getNeighborBlock(1, 0, 0)
                )
        );
        NativeRuntime ordinaryRuntime = nativeRuntime(ordinary);
        RecordingTileModel ordinaryModel = render(ordinaryRuntime.renderer(), ordinary);
        assertEquals(12, ordinaryModel.size());
        assertEquals(ordinary.gallery().get(CORE), ordinaryModel.face(0).material());

        Fixture extension = fixture(false, false, true);
        extension.states().put(east, BlockState.fromString(extensionId));
        extension.blockEntities().put(
                east,
                new TestBlockEntity(Key.parse(extensionBlockEntityId), east)
        );
        assertEquals(
                CableBusRenderer.UnsupportedEndpointObservation.EXACT_COMPATIBLE,
                CableBusRenderer.classifyUnsupportedEndpoint(
                        extensionId,
                        extension.neighborhood().getNeighborBlock(1, 0, 0)
                )
        );
        NativeRuntime extensionRuntime = nativeRuntime(extension);
        RecordingTileModel extensionModel = render(extensionRuntime.renderer(), extension);
        assertOriginalOnly(extensionModel, extension, extensionId);
        assertTrue(
                extensionRuntime.activation().isActive(),
                extensionRuntime.activation().reason()
        );

        Fixture missingEntity = fixture(false, false, true);
        missingEntity.states().put(east, BlockState.fromString(extensionId));
        assertEquals(
                CableBusRenderer.UnsupportedEndpointObservation.MALFORMED,
                CableBusRenderer.classifyUnsupportedEndpoint(
                        extensionId,
                        missingEntity.neighborhood().getNeighborBlock(1, 0, 0)
                )
        );
        assertOriginalOnly(
                render(nativeRuntime(missingEntity).renderer(), missingEntity),
                missingEntity,
                "missing extension block entity"
        );

        Fixture wrongEntity = fixture(false, false, true);
        wrongEntity.states().put(east, BlockState.fromString(extensionId));
        wrongEntity.blockEntities().put(
                east,
                new TestBlockEntity(Key.parse("minecraft:furnace"), east)
        );
        assertEquals(
                CableBusRenderer.UnsupportedEndpointObservation.MALFORMED,
                CableBusRenderer.classifyUnsupportedEndpoint(
                        extensionId,
                        wrongEntity.neighborhood().getNeighborBlock(1, 0, 0)
                )
        );
        assertOriginalOnly(
                render(nativeRuntime(wrongEntity).renderer(), wrongEntity),
                wrongEntity,
                "wrong extension block entity"
        );
    }

    @Test
    void nativeStructuralLocalCableBusRequiresExactSerializedBlockEntityId()
            throws Exception {
        for (Key invalidId : invalidCableBusBlockEntityIds()) {
            Fixture fixture = fixture(false, false, true);
            Ae2CableBusBlockEntityData local = cableBus("ae2:fluix_glass_cable");
            setBlockEntityId(local, invalidId);
            fixture.blockEntities().put(new Position(X, Y, Z), local);
            NativeRuntime runtime = nativeRuntime(fixture);

            assertOriginalOnly(
                    render(runtime.renderer(), fixture),
                    fixture,
                    "local cable-bus block-entity ID " + invalidId
            );
            assertTrue(runtime.activation().isActive(), runtime.activation().reason());
        }
    }

    @Test
    void nativeStructuralDirectNeighborRequiresExactSerializedBlockEntityId()
            throws Exception {
        Position east = new Position(X + 1, Y, Z);
        for (Key invalidId : invalidCableBusBlockEntityIds()) {
            Fixture fixture = fixture(false, true, true);
            Ae2CableBusBlockEntityData adjacent = cableBus("ae2:fluix_glass_cable");
            setBlockEntityId(adjacent, invalidId);
            fixture.blockEntities().put(east, adjacent);
            NativeRuntime runtime = nativeRuntime(fixture);

            assertOriginalOnly(
                    render(runtime.renderer(), fixture),
                    fixture,
                    "direct neighbor cable-bus block-entity ID " + invalidId
            );
            assertTrue(runtime.activation().isActive(), runtime.activation().reason());
        }
    }

    @Test
    void nativeStructuralPlaneNeighborRequiresExactSerializedBlockEntityId()
            throws Exception {
        Position center = new Position(X, Y, Z);
        Position north = new Position(X, Y, Z - 1);
        for (Key invalidId : invalidCableBusBlockEntityIds()) {
            Fixture fixture = fixture(false, false, true);
            putPlaneResources(fixture);
            fixture.blockEntities().put(
                    center,
                    cableBusWithFacePart(
                            Direction6.UP,
                            NativeStructuralPartCatalog.FORMATION_PLANE
                    )
            );
            fixture.states().put(north, exactState());
            Ae2CableBusBlockEntityData adjacent = cableBusWithFacePart(
                    Direction6.UP,
                    NativeStructuralPartCatalog.FORMATION_PLANE
            );
            setBlockEntityId(adjacent, invalidId);
            fixture.blockEntities().put(north, adjacent);
            NativeRuntime runtime = nativeRuntime(fixture);

            assertOriginalOnly(
                    render(runtime.renderer(), fixture),
                    fixture,
                    "plane neighbor cable-bus block-entity ID " + invalidId
            );
            assertTrue(runtime.activation().isActive(), runtime.activation().reason());
        }
    }

    @Test
    void nativeStructuralFacadeAppearanceRequiresExactSerializedBlockEntityId()
            throws Exception {
        Position center = new Position(X, Y, Z);
        Position up = new Position(X, Y + 1, Z);
        for (Key invalidId : invalidCableBusBlockEntityIds()) {
            Fixture fixture = fixture(false, false, true);
            Ae2CableBusBlockEntityData local = cableBus("ae2:fluix_glass_cable");
            setRetainedField(local, "facadeUp", Map.of("Name", "minecraft:stone"));
            fixture.blockEntities().put(center, local);
            fixture.states().put(up, exactState());
            Ae2CableBusBlockEntityData adjacent = cableBus("ae2:fluix_glass_cable");
            setBlockEntityId(adjacent, invalidId);
            fixture.blockEntities().put(up, adjacent);
            NativeRuntime runtime = nativeRuntime(fixture);

            assertOriginalOnly(
                    render(runtime.renderer(), fixture),
                    fixture,
                    "facade neighbor cable-bus block-entity ID " + invalidId
            );
            assertTrue(runtime.activation().isActive(), runtime.activation().reason());
        }
    }

    @Test
    void operatorDisabledStructuralRoutePreservesPredecessorRuntimeRendering()
            throws Exception {
        List<Ae2CableBusBlockEntityData> predecessorCases = List.of(
                cableBusWithTerminals(
                        "ae2:fluix_glass_cable",
                        Map.of(Direction.NORTH, 0),
                        null
                ),
                cableBus("ae2:fluix_covered_dense_cable"),
                cableBus("ae2:fluix_covered_cable"),
                cableBusWithTerminals(
                        "ae2:fluix_glass_cable",
                        Map.of(Direction.SOUTH, 1),
                        Direction.SOUTH
                ),
                cableBus("ae2:fluix_smart_cable")
        );
        for (Ae2CableBusBlockEntityData data : predecessorCases) {
            Fixture fixture = fixture(false, false, true);
            fixture.blockEntities().put(new Position(X, Y, Z), data);
            fixture.states().put(
                    new Position(X + 1, Y, Z),
                    BlockState.fromString("minecraft:stone")
            );
            RecordingTileModel predecessor = render(fixture);
            NativeStructuralRouteActivation route =
                    new NativeStructuralRouteActivation();
            route.disable(NativeStructuralRouteActivation.Reason.OPERATOR_DISABLED);
            CableBusRenderer disabled = new CableBusRenderer(
                    fixture.resourcePack(),
                    fixture.gallery(),
                    fixture.renderSettings(),
                    fixture.activation(),
                    route
            );

            RecordingTileModel disabledModel = render(disabled, fixture);

            assertModelEquals(predecessor, disabledModel);
            assertTrue(route.isDisabled(), route.reason());
        }
    }

    @Test
    void facadeOutwardFaceUsesBlueMapCullingProperties() throws Exception {
        for (Map.Entry<String, Integer> expectation : Map.of(
                "minecraft:stone", 108,
                "test:opaque", 108,
                "test:non_culling", 116
        ).entrySet()) {
            Fixture fixture = fixture(false, false, true);
            fixture.blockEntities().put(
                    new Position(X, Y, Z),
                    cableBusWithTerminals(
                            "ae2:fluix_glass_cable",
                            Map.of(Direction.SOUTH, 1),
                            Direction.SOUTH
                    )
            );
            fixture.states().put(
                    new Position(X, Y, Z + 1),
                    BlockState.fromString(expectation.getKey())
            );

            RecordingTileModel model = render(fixture);

            assertEquals(expectation.getValue(), model.size(), expectation.getKey());
            assertTrue(fixture.activation().isActive(), fixture.activation().reason());
        }
    }

    @Test
    void denseTerminalAndUnsupportedFacadeLayoutsStayAtomicStockFallback()
            throws Exception {
        List<Ae2CableBusBlockEntityData> unsupported = List.of(
                cableBusWithTerminals(
                        "ae2:fluix_covered_dense_cable",
                        Map.of(Direction.NORTH, 0),
                        null
                ),
                cableBusWithTerminals(
                        "ae2:fluix_glass_cable",
                        Map.of(Direction.NORTH, 0, Direction.UP, 1),
                        Direction.NORTH
                )
        );
        for (Ae2CableBusBlockEntityData data : unsupported) {
            Fixture fixture = fixture(false, false, true);
            fixture.blockEntities().put(new Position(X, Y, Z), data);

            RecordingTileModel model = render(fixture);

            assertEquals(12, model.size());
            int original = fixture.gallery().get(ORIGINAL);
            for (int face = 0; face < model.size(); face++) {
                assertEquals(original, model.face(face).material());
            }
            assertTrue(fixture.activation().isActive(), fixture.activation().reason());
        }
    }

    @Test
    void missingM2ResourceFallsBackBeforeAnyCustomTriangle() throws Exception {
        Fixture fixture = fixture(false, false, true);
        fixture.blockEntities().put(
                new Position(X, Y, Z),
                cableBusWithTerminals(
                        "ae2:fluix_glass_cable",
                        Map.of(Direction.NORTH, 0),
                        null
                )
        );
        fixture.resourcePack().getModels().remove(Key.parse("ae2:part/display_base"));

        RecordingTileModel model = render(fixture);

        assertEquals(12, model.size());
        int original = fixture.gallery().get(ORIGINAL);
        for (int face = 0; face < model.size(); face++) {
            assertEquals(original, model.face(face).material());
        }
        assertTrue(fixture.activation().isActive(), fixture.activation().reason());
    }

    @Test
    void terminalEmissionFailureResetsEveryCableTriangleBeforeStockFallback()
            throws Exception {
        Fixture fixture = fixture(false, false, true);
        fixture.blockEntities().put(
                new Position(X, Y, Z),
                cableBusWithTerminals(
                        "ae2:fluix_glass_cable",
                        Map.of(Direction.NORTH, 0),
                        null
                )
        );
        RecordingTileModel model = new RecordingTileModel();
        model.failOnAddInvocation(14);

        fixture.renderer().render(
                fixture.neighborhood(),
                null,
                new TileModelView(model),
                new Color()
        );

        assertEquals(12, model.size());
        int original = fixture.gallery().get(ORIGINAL);
        for (int face = 0; face < model.size(); face++) {
            assertEquals(original, model.face(face).material());
        }
        assertTrue(fixture.activation().isDisabled());
        assertEquals("render-callback-failed", fixture.activation().reason());
    }

    @Test
    void allTenExtensionFacePartsRespectRouteActivationAndAtomicFallback()
            throws Exception {
        List<NativeStructuralPartCatalog.Definition> definitions =
                NativeStructuralPartCatalog.extensionIds().stream()
                        .sorted()
                        .map(NativeStructuralPartCatalog::require)
                        .toList();
        assertEquals(10, definitions.size());

        for (NativeStructuralPartCatalog.Definition definition : definitions) {
            Fixture active = extensionPartFixture(definition);
            M45Runtime activeRuntime = activeM45Runtime();
            RecordingTileModel activeModel = render(
                    nativeRuntime(active, activeRuntime).renderer(),
                    active
            );
            assertTrue(activeModel.size() > 12, definition.id());
            assertTrue(activeRuntime.active(definition.extensionRouteId()), definition.id());

            Fixture inactive = extensionPartFixture(definition);
            M45Runtime inactiveRuntime = activeM45Runtime();
            inactiveRuntime.route(definition.extensionRouteId()).inactive(
                    ExtensionRouteActivation.Reason.ARTIFACT_NOT_FOUND,
                    "test-route-inactive"
            );
            RecordingTileModel inactiveModel = render(
                    nativeRuntime(inactive, inactiveRuntime).renderer(),
                    inactive
            );
            assertEquals(12, inactiveModel.size(), definition.id());
            int core = inactive.gallery().get(CORE);
            for (int face = 0; face < inactiveModel.size(); face++) {
                assertEquals(core, inactiveModel.face(face).material(), definition.id());
            }

            Fixture missingResource = extensionPartFixture(definition);
            removeSelectedExtensionPartResource(missingResource, definition);
            M45Runtime missingRuntime = activeM45Runtime();
            RecordingTileModel missingModel = render(
                    nativeRuntime(missingResource, missingRuntime).renderer(),
                    missingResource
            );
            assertOriginalOnly(missingModel, missingResource, definition.id());
            assertTrue(missingRuntime.active(definition.extensionRouteId()), definition.id());
        }
    }

    @Test
    void runtimeCoreFailuresImmediatelyBlockOnlyTheirDependentM45Routes()
            throws Exception {
        Fixture baseFailure = fixture(false, false, true);
        baseFailure.blockEntities().put(
                new Position(X, Y, Z),
                cableBusWithTerminals(
                        "ae2:fluix_glass_cable",
                        Map.of(Direction.NORTH, 0),
                        null
                )
        );
        M45Runtime allDependents = activeM45Runtime();
        allDependents.route(M45Runtime.APPFLUX).disable(
                ExtensionRouteActivation.Reason.RENDER_CALLBACK_FAILED,
                "preexisting-extension-failure"
        );
        CableBusRenderer baseRenderer = new CableBusRenderer(
                baseFailure.resourcePack(),
                baseFailure.gallery(),
                baseFailure.renderSettings(),
                baseFailure.activation(),
                new NativeStructuralRouteActivation(),
                allDependents
        );
        RecordingTileModel baseModel = new RecordingTileModel();
        baseModel.failOnAddInvocation(14);
        baseRenderer.render(
                baseFailure.neighborhood(),
                null,
                new TileModelView(baseModel),
                new Color()
        );
        assertTrue(baseFailure.activation().isDisabled());
        assertTrue(allDependents.routes().stream()
                .filter(route -> !route.routeId().equals(M45Runtime.APPFLUX))
                .allMatch(route -> route.snapshot().reason()
                        == ExtensionRouteActivation.Reason.BLOCKED_BY_CORE));
        assertEquals(
                "preexisting-extension-failure",
                allDependents.route(M45Runtime.APPFLUX).snapshot().detail()
        );

        Fixture nativeFailure = fixture(false, false, true);
        NativeStructuralRouteActivation nativeRoute =
                new NativeStructuralRouteActivation();
        nativeRoute.activate();
        M45Runtime faceDependents = activeM45Runtime();
        CableBusRenderer nativeRenderer = new CableBusRenderer(
                nativeFailure.resourcePack(),
                nativeFailure.gallery(),
                nativeFailure.renderSettings(),
                nativeFailure.activation(),
                nativeRoute,
                faceDependents
        );
        RecordingTileModel nativeModel = new RecordingTileModel();
        nativeModel.failOnAddInvocation(1);
        nativeRenderer.render(
                nativeFailure.neighborhood(),
                null,
                new TileModelView(nativeModel),
                new Color()
        );
        assertTrue(nativeRoute.isDisabled());
        assertEquals(
                ExtensionRouteActivation.Reason.BLOCKED_BY_CORE,
                faceDependents.route(M45Runtime.EXTENDED_PLANES).snapshot().reason()
        );
        assertTrue(faceDependents.active(M45Runtime.APPFLUX));
    }

    @Test
    void oppositeNeighborTerminalDefinitivelyBlocksTheCableConnection()
            throws Exception {
        Fixture fixture = fixture(false, true, true);
        fixture.blockEntities().put(
                new Position(X + 1, Y, Z),
                cableBusWithTerminals(
                        "ae2:fluix_glass_cable",
                        Map.of(Direction.WEST, 0),
                        null
                )
        );

        RecordingTileModel model = render(fixture);

        assertEquals(12, model.size());
        assertEquals(fixture.gallery().get(CORE), model.face(0).material());
    }

    @Test
    void unsupportedOppositeNeighborPartMakesTopologyUnknown() throws Exception {
        Fixture fixture = fixture(false, true, true);
        Ae2CableBusBlockEntityData neighbor = cableBus();
        setRetainedField(
                neighbor,
                "west",
                Map.of("id", "ae2:unknown_part", "spin", (byte) 0)
        );
        fixture.blockEntities().put(new Position(X + 1, Y, Z), neighbor);

        RecordingTileModel model = render(fixture);

        assertEquals(10, model.size());
        int original = fixture.gallery().get(ORIGINAL);
        for (int face = 0; face < model.size(); face++) {
            assertEquals(original, model.face(face).material());
        }
        assertTrue(fixture.activation().isActive(), fixture.activation().reason());
    }

    @Test
    void strictStateFallbackAndRenderFailureNeverLeakPartialGeometry() throws Exception {
        Fixture unsupported = fixture(false, false, true);
        unsupported.states().put(
                new Position(X, Y, Z),
                BlockState.fromString("ae2:cable_bus[light_level=1,waterlogged=false]")
        );
        RecordingTileModel stateModel = new RecordingTileModel();
        unsupported.renderer().render(
                unsupported.neighborhood(),
                null,
                new TileModelView(stateModel),
                new Color()
        );
        assertEquals(12, stateModel.size());
        assertEquals(unsupported.gallery().get(ORIGINAL), stateModel.face(0).material());

        Fixture failure = fixture(false, false, true);
        RecordingTileModel failedModel = new RecordingTileModel();
        failedModel.add(1);
        failedModel.setMaterialIndex(0, 777);
        failedModel.failOnAddInvocation(2);
        failure.renderer().render(
                failure.neighborhood(),
                null,
                new TileModelView(failedModel),
                new Color()
        );

        assertEquals(13, failedModel.size());
        assertEquals(777, failedModel.face(0).material());
        int originalMaterial = failure.gallery().get(ORIGINAL);
        for (int face = 1; face < failedModel.size(); face++) {
            assertEquals(originalMaterial, failedModel.face(face).material());
        }
        assertTrue(failure.activation().isDisabled());
        assertEquals("render-callback-failed", failure.activation().reason());

        RecordingTileModel secondModel = render(failure);
        assertEquals(12, secondModel.size());
        for (int face = 0; face < secondModel.size(); face++) {
            assertEquals(originalMaterial, secondModel.face(face).material());
        }
    }

    @Test
    void inactiveStockLinkageErrorIsContainedAndClearsOnlyTheCurrentBlock() throws Exception {
        Fixture fixture = fixture(false, false, true);
        fixture.activation().inactive(ProfileActivation.Reason.AWAITING_EXACT_PROFILE);

        RecordingTileModel model = new RecordingTileModel();
        model.add(1);
        model.setMaterialIndex(0, 777);
        model.failWithLinkageErrorOnAddInvocation(1);
        Color mapColor = new Color().set(1F, 1F, 1F, 1F, true);

        fixture.renderer().render(
                fixture.neighborhood(),
                null,
                new TileModelView(model),
                mapColor
        );

        assertEquals(1, model.size());
        assertEquals(777, model.face(0).material());
        assertEquals(0F, mapColor.r, 0F);
        assertEquals(0F, mapColor.g, 0F);
        assertEquals(0F, mapColor.b, 0F);
        assertEquals(0F, mapColor.a, 0F);
        assertEquals("awaiting-exact-ae2-profile", fixture.activation().reason());
    }

    @Test
    void activeOuterFailureAndStockLinkageErrorRemainTileSafe() throws Exception {
        Fixture fixture = fixture(false, false, true);
        RecordingTileModel model = new RecordingTileModel();
        model.add(1);
        model.setMaterialIndex(0, 777);
        model.failWithRuntimeThenLinkageOnAddInvocations(2, 3);
        Color mapColor = new Color().set(1F, 1F, 1F, 1F, true);

        fixture.renderer().render(
                fixture.neighborhood(),
                null,
                new TileModelView(model),
                mapColor
        );

        assertEquals(1, model.size());
        assertEquals(777, model.face(0).material());
        assertEquals(0F, mapColor.r, 0F);
        assertEquals(0F, mapColor.g, 0F);
        assertEquals(0F, mapColor.b, 0F);
        assertEquals(0F, mapColor.a, 0F);
        assertTrue(fixture.activation().isDisabled());
        assertEquals("render-callback-failed", fixture.activation().reason());
    }

    @Test
    void tileCapacityExceptionPropagatesWithoutChangingActiveProfile() throws Exception {
        Fixture fixture = fixture(false, false, true);
        RecordingTileModel model = new RecordingTileModel();
        model.failWithCapacityOnAddInvocation(1);

        assertThrows(
                MaxCapacityReachedException.class,
                () -> fixture.renderer().render(
                        fixture.neighborhood(),
                        null,
                        new TileModelView(model),
                        new Color()
                )
        );

        assertTrue(fixture.activation().isActive(), fixture.activation().reason());
    }

    @Test
    void tileCapacityExceptionPropagatesThroughInactiveStockPath() throws Exception {
        Fixture fixture = fixture(false, false, true);
        fixture.activation().inactive(ProfileActivation.Reason.AWAITING_EXACT_PROFILE);
        RecordingTileModel model = new RecordingTileModel();
        model.failWithCapacityOnAddInvocation(1);

        assertThrows(
                MaxCapacityReachedException.class,
                () -> fixture.renderer().render(
                        fixture.neighborhood(),
                        null,
                        new TileModelView(model),
                        new Color()
                )
        );

        assertEquals("awaiting-exact-ae2-profile", fixture.activation().reason());
    }

    @Test
    void sixteenthBoundaryClassificationIsExact() {
        assertEquals(-1, CableBusRenderer.boundaryOffset(0));
        assertEquals(0, CableBusRenderer.boundaryOffset(1));
        assertEquals(0, CableBusRenderer.boundaryOffset(15));
        assertEquals(1, CableBusRenderer.boundaryOffset(16));
    }

    private static Fixture fixture(
            boolean westNeighbor,
            boolean eastNeighbor,
            boolean includeEastBlockEntity
    ) throws Exception {
        return fixture(
                "ae2:fluix_glass_cable",
                westNeighbor ? "ae2:fluix_glass_cable" : null,
                eastNeighbor ? "ae2:fluix_glass_cable" : null,
                includeEastBlockEntity
        );
    }

    private static Fixture fixture(
            String centerCable,
            String westCable,
            String eastCable,
            boolean includeEastBlockEntity
    ) throws Exception {
        return fixture(
                centerCable,
                westCable,
                eastCable,
                includeEastBlockEntity,
                TEST_RENDER_SETTINGS
        );
    }

    private static Fixture fixture(
            String centerCable,
            String westCable,
            String eastCable,
            boolean includeEastBlockEntity,
            RenderSettings renderSettings
    ) throws Exception {
        ResourcePack resourcePack = new ResourcePack(new PackVersion(34, 0));
        for (String texture : Ae219217Profile.textures()) {
            putTexture(resourcePack, Key.parse(texture), 0xFFFFFFFF);
        }
        putM2Resources(resourcePack);
        putTexture(resourcePack, ORIGINAL, 0xFF0000FF);
        putOriginalCube(resourcePack);

        TextureGallery gallery = new TextureGallery();
        gallery.put(resourcePack.getTextures());

        Map<Position, BlockState> states = new HashMap<>();
        Map<Position, BlockEntity> blockEntities = new HashMap<>();
        Map<Position, LightData> lights = new HashMap<>();
        Position center = new Position(X, Y, Z);
        states.put(center, exactState());
        blockEntities.put(center, cableBus(centerCable));
        lights.put(center, new LightData(3, 2));
        lights.put(new Position(X, Y + 1, Z), new LightData(5, 4));

        if (westCable != null) {
            Position west = new Position(X - 1, Y, Z);
            states.put(west, exactState());
            blockEntities.put(west, cableBus(westCable));
        }
        if (eastCable != null) {
            Position east = new Position(X + 1, Y, Z);
            states.put(east, exactState());
            if (includeEastBlockEntity) {
                blockEntities.put(east, cableBus(eastCable));
            }
        }

        BlockNeighborhood neighborhood = new BlockNeighborhood(
                new TestBlockAccess(states, blockEntities, lights),
                resourcePack,
                renderSettings,
                DimensionType.OVERWORLD
        );
        neighborhood.set(X, Y, Z);

        ProfileActivation activation = new ProfileActivation();
        activation.activate();
        return new Fixture(
                states,
                blockEntities,
                resourcePack,
                gallery,
                neighborhood,
                lights,
                renderSettings,
                activation,
                new CableBusRenderer(
                        resourcePack,
                        gallery,
                        renderSettings,
                        activation
                )
        );
    }

    private static RecordingTileModel render(Fixture fixture) {
        return render(fixture.renderer(), fixture);
    }

    private static RecordingTileModel render(
            CableBusRenderer renderer,
            Fixture fixture
    ) {
        RecordingTileModel model = new RecordingTileModel();
        renderer.render(
                fixture.neighborhood(),
                null,
                new TileModelView(model),
                new Color()
        );
        return model;
    }

    private static NativeRuntime nativeRuntime(Fixture fixture) {
        return nativeRuntime(fixture, M45Adapter.runtime());
    }

    private static void assertNativeStorageBusSeam(
            Direction6 direction,
            String targetBlockId
    ) throws Exception {
        Fixture fixture = fixture(false, false, true);
        ResourcePack exact = AppMekExternalResourceTestSupport.exactResources();
        AppMekExternalResourceTestSupport.putExactAe2Textures(
                exact,
                NativeStructuralResourceModels.requiredTextures()
        );
        for (Key key : exact.getModels().keySet()) {
            fixture.resourcePack().getModels().put(key, exact.getModels().get(key));
        }
        for (Key key : exact.getTextures().keySet()) {
            fixture.resourcePack().getTextures().put(
                    key,
                    exact.getTextures().get(key)
            );
        }
        fixture.gallery().put(fixture.resourcePack().getTextures());
        fixture.blockEntities().put(
                new Position(X, Y, Z),
                cableBusWithFacePart(
                        "ae2:fluix_glass_cable",
                        direction,
                        "ae2:storage_bus"
                )
        );
        fixture.states().put(
                new Position(
                        X + direction.stepX(),
                        Y + direction.stepY(),
                        Z + direction.stepZ()
                ),
                BlockState.fromString(targetBlockId)
        );

        RecordingTileModel model = render(nativeRuntime(fixture).renderer(), fixture);

        assertEquals(74, model.size(), targetBlockId);
        float attachmentPlane = direction == Direction6.EAST || direction == Direction6.UP
                ? 14F : 2F;
        int axis = direction == Direction6.EAST ? 0 : direction == Direction6.UP ? 1 : 2;
        boolean reachesAttachmentPlane = false;
        float minimum = Float.POSITIVE_INFINITY;
        float maximum = Float.NEGATIVE_INFINITY;
        for (int face = 0; face < model.size(); face++) {
            float[] positions = model.face(face).positions();
            for (int index = axis; index < positions.length; index += 3) {
                minimum = Math.min(minimum, positions[index]);
                maximum = Math.max(maximum, positions[index]);
                reachesAttachmentPlane |=
                        Math.abs(positions[index] - attachmentPlane) <= MESH_EPSILON;
                assertTrue(Float.isFinite(positions[index]),
                        targetBlockId + "/" + face + "/" + index);
            }
        }
        assertTrue(
                reachesAttachmentPlane,
                targetBlockId + "/" + minimum + "/" + maximum
        );
    }

    private static NativeRuntime nativeRuntime(
            Fixture fixture,
            M45Runtime m45Runtime
    ) {
        NativeStructuralRouteActivation route = new NativeStructuralRouteActivation();
        route.activate();
        return new NativeRuntime(
                route,
                new CableBusRenderer(
                        fixture.resourcePack(),
                        fixture.gallery(),
                        fixture.renderSettings(),
                        fixture.activation(),
                        route,
                        m45Runtime
                )
        );
    }

    private static M45Runtime activeM45Runtime() {
        M45Runtime runtime = new M45Runtime();
        runtime.routes().forEach(route -> route.activate("exact-profile"));
        return runtime;
    }

    private static void assertOriginalOnly(
            RecordingTileModel model,
            Fixture fixture,
            String message
    ) {
        int original = fixture.gallery().get(ORIGINAL);
        assertTrue(model.size() > 0, message);
        for (int face = 0; face < model.size(); face++) {
            assertEquals(original, model.face(face).material(), message);
        }
    }

    private static void assertTintedMaterial(
            RecordingTileModel model,
            int material,
            float[] expectedColor
    ) {
        int matches = 0;
        for (int face = 0; face < model.size(); face++) {
            if (model.face(face).material() == material) {
                assertArrayEquals(expectedColor, model.face(face).color(), 0F);
                matches++;
            }
        }
        assertTrue(matches > 0, "expected emitted material " + material);
    }

    private static void assertModelEquals(
            RecordingTileModel expected,
            RecordingTileModel actual
    ) {
        assertEquals(expected.size(), actual.size());
        for (int face = 0; face < expected.size(); face++) {
            assertArrayEquals(
                    expected.face(face).positions(),
                    actual.face(face).positions(),
                    0F
            );
            assertArrayEquals(expected.face(face).uvs(), actual.face(face).uvs(), 0F);
            assertArrayEquals(expected.face(face).aos(), actual.face(face).aos(), 0F);
            assertArrayEquals(expected.face(face).color(), actual.face(face).color(), 0F);
            assertEquals(expected.face(face).sunlight(), actual.face(face).sunlight());
            assertEquals(expected.face(face).blocklight(), actual.face(face).blocklight());
            assertEquals(expected.face(face).material(), actual.face(face).material());
        }
    }

    private static BlockState exactState() {
        return BlockState.fromString(
                "ae2:cable_bus[light_level=0,waterlogged=false]"
        );
    }

    private static Ae2CableBusBlockEntityData cableBus() throws ReflectiveOperationException {
        return cableBus("ae2:fluix_glass_cable");
    }

    private static Ae2CableBusBlockEntityData cableBus(String cableId)
            throws ReflectiveOperationException {
        Ae2CableBusBlockEntityData data = new Ae2CableBusBlockEntityData();
        setBlockEntityId(data, CABLE_BUS);
        Field cable = Ae2CableBusBlockEntityData.class.getDeclaredField("cable");
        cable.setAccessible(true);
        cable.set(data, Map.of("id", cableId));
        return data;
    }

    private static Ae2CableBusBlockEntityData cableBusWithFacePart(
            Direction6 direction,
            String partId
    ) throws ReflectiveOperationException {
        Ae2CableBusBlockEntityData data = new Ae2CableBusBlockEntityData();
        setBlockEntityId(data, CABLE_BUS);
        setRetainedField(
                data,
                direction.name().toLowerCase(java.util.Locale.ROOT),
                Map.of("id", partId)
        );
        return data;
    }

    private static Ae2CableBusBlockEntityData cableBusWithFacePart(
            String cableId,
            Direction6 direction,
            String partId
    ) throws ReflectiveOperationException {
        Ae2CableBusBlockEntityData data = cableBus(cableId);
        setRetainedField(
                data,
                direction.name().toLowerCase(java.util.Locale.ROOT),
                Map.of("id", partId)
        );
        return data;
    }

    private static List<Key> invalidCableBusBlockEntityIds() {
        return Arrays.asList(null, Key.parse("minecraft:furnace"));
    }

    private static void setBlockEntityId(
            Ae2CableBusBlockEntityData data,
            Key id
    ) throws ReflectiveOperationException {
        Field field = MCABlockEntity.class.getDeclaredField("id");
        field.setAccessible(true);
        field.set(data, id);
    }

    private static Ae2CableBusBlockEntityData cableBusWithRetainedField(String fieldName)
            throws ReflectiveOperationException {
        Ae2CableBusBlockEntityData data = cableBus();
        Field retained = Ae2CableBusBlockEntityData.class.getDeclaredField(fieldName);
        retained.setAccessible(true);
        retained.set(
                data,
                fieldName.startsWith("facade")
                        ? Map.of("Name", "minecraft:stone")
                        : Map.of("id", "ae2:unknown_part", "spin", (byte) 0)
        );
        return data;
    }

    private static Ae2CableBusBlockEntityData cableBusWithTerminals(
            String cableId,
            Map<Direction, Integer> terminals,
            Direction facade
    ) throws ReflectiveOperationException {
        Ae2CableBusBlockEntityData data = cableBus(cableId);
        for (Map.Entry<Direction, Integer> entry : terminals.entrySet()) {
            setRetainedField(
                    data,
                    entry.getKey().name().toLowerCase(java.util.Locale.ROOT),
                    Map.of(
                            "id", "ae2:terminal",
                            "spin", entry.getValue().byteValue()
                    )
            );
        }
        if (facade != null) {
            String direction = facade.name().toLowerCase(java.util.Locale.ROOT);
            setRetainedField(
                    data,
                    "facade" + Character.toUpperCase(direction.charAt(0))
                            + direction.substring(1),
                    Map.of("Name", "minecraft:stone")
            );
        }
        return data;
    }

    private static void setRetainedField(
            Ae2CableBusBlockEntityData data,
            String fieldName,
            Object value
    ) throws ReflectiveOperationException {
        Field field = Ae2CableBusBlockEntityData.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(data, value);
    }

    private static Fixture extensionPartFixture(
            NativeStructuralPartCatalog.Definition definition
    ) throws Exception {
        Fixture fixture = fixture(false, false, true);
        putExtensionPartResources(fixture, definition);
        Ae2CableBusBlockEntityData data = cableBus("ae2:fluix_glass_cable");
        LinkedHashMap<String, Object> rawPart = new LinkedHashMap<>();
        rawPart.put("id", definition.id());
        if (definition.persistedSpin()) {
            rawPart.put("spin", (byte) 0);
        }
        setRetainedField(data, "north", rawPart);
        fixture.blockEntities().put(new Position(X, Y, Z), data);
        return fixture;
    }

    private static void putExtensionPartResources(
            Fixture fixture,
            NativeStructuralPartCatalog.Definition definition
    ) throws IOException {
        putTexture(fixture.resourcePack(), EXTENSION_PART_TEXTURE, 0xFFFFFFFF);
        for (String modelPath : NativeStructuralResourceModels.renderedModelPaths(
                definition,
                false
        )) {
            fixture.resourcePack().getModels().putIfAbsent(
                    Key.parse(modelPath),
                    extensionPartModel()
            );
        }
        if (definition.kind() == NativeStructuralPartCatalog.Kind.PLANE) {
            for (String texture : List.of(
                    NativePartGeometry.planeFrontTexture(definition.id()),
                    NativePartGeometry.PLANE_SIDE_TEXTURE,
                    NativePartGeometry.PLANE_BACK_TEXTURE
            )) {
                putTexture(fixture.resourcePack(), Key.parse(texture), 0xFFFFFFFF);
            }
        }
        fixture.gallery().put(fixture.resourcePack().getTextures());
    }

    private static void removeSelectedExtensionPartResource(
            Fixture fixture,
            NativeStructuralPartCatalog.Definition definition
    ) {
        if (definition.kind() == NativeStructuralPartCatalog.Kind.PLANE) {
            fixture.resourcePack().getTextures().remove(
                    Key.parse(NativePartGeometry.planeFrontTexture(definition.id()))
            );
            return;
        }
        String namespace = definition.id().substring(0, definition.id().indexOf(':') + 1);
        String ownedModel = NativeStructuralResourceModels.renderedModelPaths(
                definition,
                false
        ).stream().filter(model -> model.startsWith(namespace)).findFirst().orElseThrow();
        fixture.resourcePack().getModels().remove(Key.parse(ownedModel));
    }

    private static Model extensionPartModel() {
        EnumMap<Direction, Face> faces = new EnumMap<>(Direction.class);
        for (Direction direction : Direction.values()) {
            faces.put(direction, new Face(
                    new Vector4f(0F, 0F, 16F, 16F),
                    new TextureVariable(new ResourcePath<Texture>(EXTENSION_PART_TEXTURE)),
                    null,
                    0,
                    -1
            ));
        }
        return new Model(
                Map.of(),
                new Element[]{new Element(
                        new Vector3f(2F, 2F, 0F),
                        new Vector3f(14F, 14F, 3F),
                        faces
                )},
                false
        );
    }

    private static void putPlaneResources(Fixture fixture) throws IOException {
        fixture.resourcePack().getModels().put(
                Key.parse("ae2:part/transition_plane_off"),
                new Model(new Element[0])
        );
        for (String texture : List.of(
                "ae2:part/formation_plane",
                NativePartGeometry.PLANE_SIDE_TEXTURE,
                NativePartGeometry.PLANE_BACK_TEXTURE
        )) {
            putTexture(fixture.resourcePack(), Key.parse(texture), 0xFFFFFFFF);
        }
        fixture.gallery().put(fixture.resourcePack().getTextures());
    }

    private static void putLevelEmitterResources(Fixture fixture) {
        for (String model : List.of(
                "ae2:part/level_emitter_base_off",
                "ae2:part/level_emitter_status_off"
        )) {
            fixture.resourcePack().getModels().put(
                    Key.parse(model),
                    new Model(new Element[0])
            );
        }
    }

    private static int materialCount(RecordingTileModel model, int material) {
        int count = 0;
        for (int face = 0; face < model.size(); face++) {
            if (model.face(face).material() == material) {
                count++;
            }
        }
        return count;
    }

    private static void putM2Resources(ResourcePack resourcePack) throws IOException {
        Map<Key, String> models = Map.of(
                Key.parse("ae2:part/display_base"), """
                        {"textures":{"sides":"ae2:part/monitor_sides",
                          "sidesStatus":"ae2:part/monitor_sides_status",
                          "back":"ae2:part/monitor_back","front":"ae2:part/monitor_front"},
                         "elements":[
                          {"from":[2,2,0],"to":[14,14,2],"faces":{
                           "down":{"texture":"#sides"},"up":{"texture":"#sides"},
                           "south":{"texture":"#back"},"east":{"texture":"#sides"},
                           "north":{"texture":"#front"},"west":{"texture":"#sides"}}},
                          {"from":[4,4,2],"to":[12,12,3],"faces":{
                           "down":{"texture":"#sidesStatus"},
                           "up":{"texture":"#sidesStatus"},
                           "south":{"texture":"#back"},
                           "east":{"texture":"#sidesStatus"},
                           "west":{"texture":"#sidesStatus"}}}]}
                        """,
                Key.parse("ae2:part/display_off"), """
                        {"textures":{"lightsBright":"ae2:part/terminal_bright",
                          "lightsMedium":"ae2:part/terminal_medium",
                          "lightsDark":"ae2:part/terminal_dark"},
                         "elements":[
                          {"from":[2,2,0],"to":[14,14,2],"faces":{"north":{
                           "texture":"#lightsBright","tintindex":3}}},
                          {"from":[2,2,0],"to":[14,14,2],"faces":{"north":{
                           "texture":"#lightsMedium","tintindex":2}}},
                          {"from":[2,2,0],"to":[14,14,2],"faces":{"north":{
                           "texture":"#lightsDark","tintindex":1}}}]}
                        """,
                Key.parse("ae2:part/terminal_off"), """
                        {"parent":"ae2:part/display_off",
                         "textures":{"lightsBright":"ae2:part/terminal_bright",
                          "lightsMedium":"ae2:part/terminal_medium",
                          "lightsDark":"ae2:part/terminal_dark"}}
                        """,
                Key.parse("ae2:part/display_status_off"), """
                        {"textures":{"indicator":"ae2:part/monitor_sides_status_off"},
                         "elements":[
                          {"from":[7,11,2],"to":[9,12,3],"faces":{
                           "south":{"texture":"#indicator"},"up":{"texture":"#indicator"}}},
                          {"from":[7,4,2],"to":[9,5,3],"faces":{
                           "south":{"texture":"#indicator"},"down":{"texture":"#indicator"}}},
                          {"from":[4,7,2],"to":[5,9,3],"faces":{
                           "south":{"texture":"#indicator"},"west":{"texture":"#indicator"}}},
                          {"from":[11,7,2],"to":[12,9,3],"faces":{
                           "east":{"texture":"#indicator"},"south":{"texture":"#indicator"}}}]}
                        """,
                Key.parse("ae2:part/cable_anchor_short"), """
                        {"textures":{"0":"ae2:part/cable_anchor"},"elements":[
                         {"from":[7,7,1],"to":[9,9,6],"faces":{
                          "north":{"uv":[6,0,8,2],"texture":"#0"},
                          "east":{"uv":[1,4,6,6],"texture":"#0"},
                          "south":{"uv":[6,4,8,6],"texture":"#0"},
                          "west":{"uv":[0,0,5,2],"texture":"#0"},
                          "up":{"uv":[0,2,5,4],"rotation":90,"texture":"#0"},
                          "down":{"uv":[1,6,6,8],"rotation":90,"texture":"#0"}}}]}
                        """,
                Key.parse("minecraft:block/stone"), fullCubeModelJson(),
                Key.parse("minecraft:block/stone_mirrored"), fullCubeModelJson()
        );
        for (Map.Entry<Key, String> entry : models.entrySet()) {
            resourcePack.getModels().put(
                    entry.getKey(),
                    ResourcesGson.INSTANCE.fromJson(entry.getValue(), Model.class)
            );
        }
        resourcePack.getModels().get(Key.parse("ae2:part/terminal_off"))
                .applyParent(resourcePack.getModels());
        resourcePack.getBlockStates().put(
                M2ResourceModels.STONE,
                ResourcesGson.INSTANCE.fromJson("""
                        {"variants":{"": [
                          {"model":"minecraft:block/stone"},
                          {"model":"minecraft:block/stone","y":180},
                          {"model":"minecraft:block/stone_mirrored"},
                          {"model":"minecraft:block/stone_mirrored","y":180}
                        ]}}
                        """, de.bluecolored.bluemap.core.resources.pack.resourcepack
                                .blockstate.BlockState.class)
        );
        for (Key texture : M2ResourceModels.requiredTextures()) {
            if (resourcePack.getTextures().get(texture) == null) {
                putTexture(resourcePack, texture, 0xFFFFFFFF);
            }
        }
        Key cableAnchorTexture = Key.parse("ae2:part/cable_anchor");
        if (resourcePack.getTextures().get(cableAnchorTexture) == null) {
            putTexture(resourcePack, cableAnchorTexture, 0xFFFFFFFF);
        }
        resourcePack.getModels().get(Key.parse("minecraft:block/stone"))
                .calculateProperties(resourcePack.getTextures());
        resourcePack.getModels().get(Key.parse("minecraft:block/stone_mirrored"))
                .calculateProperties(resourcePack.getTextures());
    }

    private static String fullCubeModelJson() {
        return """
                {"textures":{"all":"minecraft:block/stone"},"elements":[{
                 "from":[0,0,0],"to":[16,16,16],"faces":{
                  "down":{"texture":"#all","cullface":"down"},
                  "up":{"texture":"#all","cullface":"up"},
                  "north":{"texture":"#all","cullface":"north"},
                  "south":{"texture":"#all","cullface":"south"},
                  "west":{"texture":"#all","cullface":"west"},
                  "east":{"texture":"#all","cullface":"east"}}}]}
                """;
    }

    private static float[] rgb(int value) {
        return new float[]{
                ((value >> 16) & 0xff) / 255F,
                ((value >> 8) & 0xff) / 255F,
                (value & 0xff) / 255F
        };
    }

    private static void putTexture(ResourcePack pack, Key key, int argb) throws IOException {
        BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        image.setRGB(0, 0, argb);
        pack.getTextures().put(key, Texture.from(key, image));
    }

    private static void putOriginalCube(ResourcePack resourcePack) {
        EnumMap<Direction, Face> faces = new EnumMap<>(Direction.class);
        for (Direction direction : Direction.values()) {
            faces.put(direction, new Face(
                    new Vector4f(0F, 0F, 16F, 16F),
                    new TextureVariable(new ResourcePath<Texture>(ORIGINAL)),
                    direction,
                    0,
                    -1
            ));
        }
        Model model = new Model(new Element(
                Vector3f.ZERO,
                new Vector3f(16F, 16F, 16F),
                faces
        ));
        model.calculateProperties(resourcePack.getTextures());
        Key modelKey = Key.parse("test:block/original_model");
        resourcePack.getModels().put(modelKey, model);
        Variant variant = new Variant(new ResourcePath<Model>(modelKey));
        de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.BlockState blockState =
                new de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.BlockState(
                        new Variants(new VariantSet[0], new VariantSet(variant))
                );
        resourcePack.getBlockStates().put(
                Key.parse(Ae219217Profile.CABLE_BUS_BLOCK),
                blockState
        );
        resourcePack.getBlockStates().put(Key.parse("test:opaque"), blockState);
    }

    private static void putFacadeResource(
            ResourcePack resourcePack,
            Key blockId,
            Key texture,
            boolean ambientOcclusion,
            int lightEmission
    ) throws IOException {
        putTexture(resourcePack, texture, 0xFFFFFFFF);
        EnumMap<Direction, Face> faces = new EnumMap<>(Direction.class);
        for (Direction direction : Direction.values()) {
            faces.put(direction, new Face(
                    new Vector4f(0F, 0F, 16F, 16F),
                    new TextureVariable(new ResourcePath<Texture>(texture)),
                    direction,
                    0,
                    -1
            ));
        }
        Key modelId = Key.parse(blockId.getFormatted() + "_model");
        Model model = new Model(
                Map.of(),
                new Element[]{new Element(
                        Vector3f.ZERO,
                        new Vector3f(16F, 16F, 16F),
                        Rotation.ZERO,
                        true,
                        lightEmission,
                        faces
                )},
                ambientOcclusion
        );
        model.calculateProperties(resourcePack.getTextures());
        resourcePack.getModels().put(modelId, model);
        Variant variant = new Variant(new ResourcePath<Model>(modelId));
        resourcePack.getBlockStates().put(
                blockId,
                new de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate
                        .BlockState(
                                new Variants(
                                        new VariantSet[0],
                                        new VariantSet(variant)
                                )
                        )
        );
    }

    private static final RenderSettings TEST_RENDER_SETTINGS = settings(
            false,
            Integer.MIN_VALUE,
            false
    );

    private static RenderSettings settings(
            boolean topOnly,
            int removeCavesBelowY,
            boolean caveUsesBlockLight
    ) {
        return new RenderSettings() {
            @Override
            public int getRemoveCavesBelowY() {
                return removeCavesBelowY;
            }

            @Override
            public int getCaveDetectionOceanFloor() {
                return 0;
            }

            @Override
            public boolean isCaveDetectionUsesBlockLight() {
                return caveUsesBlockLight;
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
                return topOnly;
            }
        };
    }

    private static final class TestBlockAccess implements BlockAccess {
        private final Map<Position, BlockState> states;
        private final Map<Position, BlockEntity> blockEntities;
        private final Map<Position, LightData> lights;
        private int x;
        private int y;
        private int z;

        private TestBlockAccess(
                Map<Position, BlockState> states,
                Map<Position, BlockEntity> blockEntities,
                Map<Position, LightData> lights
        ) {
            this.states = states;
            this.blockEntities = blockEntities;
            this.lights = lights;
        }

        @Override
        public void set(int newX, int newY, int newZ) {
            x = newX;
            y = newY;
            z = newZ;
        }

        @Override
        public BlockAccess copy() {
            return new TestBlockAccess(states, blockEntities, lights);
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
            LightData light = lights.get(new Position(x, y, z));
            return light == null
                    ? new LightData(0, 0)
                    : new LightData(light.getSkyLight(), light.getBlockLight());
        }

        @Override
        public Biome getBiome() {
            return Biome.DEFAULT;
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

    private static final class RecordingTileModel extends ArrayTileModel {
        private final List<FaceData> faces = new ArrayList<>();
        private int addInvocation;
        private int failedAddInvocation = -1;
        private int linkageFailureInvocation = -1;
        private int capacityFailureInvocation = -1;

        private RecordingTileModel() {
            super(32);
        }

        void failOnAddInvocation(int invocation) {
            addInvocation = 0;
            failedAddInvocation = invocation;
            linkageFailureInvocation = -1;
            capacityFailureInvocation = -1;
        }

        void failWithLinkageErrorOnAddInvocation(int invocation) {
            addInvocation = 0;
            failedAddInvocation = -1;
            linkageFailureInvocation = invocation;
            capacityFailureInvocation = -1;
        }

        void failWithRuntimeThenLinkageOnAddInvocations(
                int runtimeInvocation,
                int linkageInvocation
        ) {
            addInvocation = 0;
            failedAddInvocation = runtimeInvocation;
            linkageFailureInvocation = linkageInvocation;
            capacityFailureInvocation = -1;
        }

        void failWithCapacityOnAddInvocation(int invocation) {
            addInvocation = 0;
            failedAddInvocation = -1;
            linkageFailureInvocation = -1;
            capacityFailureInvocation = invocation;
        }

        FaceData face(int face) {
            return faces.get(face);
        }

        @Override
        public int add(int count) {
            addInvocation++;
            if (addInvocation == capacityFailureInvocation) {
                capacityFailureInvocation = -1;
                throw new MaxCapacityReachedException("injected capacity");
            }
            if (addInvocation == failedAddInvocation) {
                failedAddInvocation = -1;
                throw new IllegalStateException("injected mesh emission failure");
            }
            if (addInvocation == linkageFailureInvocation) {
                linkageFailureInvocation = -1;
                throw new NoClassDefFoundError("injected stock renderer linkage failure");
            }
            int start = super.add(count);
            while (faces.size() < size()) {
                faces.add(new FaceData());
            }
            return start;
        }

        @Override
        public RecordingTileModel reset(int size) {
            super.reset(size);
            while (faces.size() > size) {
                faces.removeLast();
            }
            return this;
        }

        @Override
        public RecordingTileModel setPositions(
                int face,
                float x1, float y1, float z1,
                float x2, float y2, float z2,
                float x3, float y3, float z3
        ) {
            super.setPositions(face, x1, y1, z1, x2, y2, z2, x3, y3, z3);
            face(face).positions = new float[]{x1, y1, z1, x2, y2, z2, x3, y3, z3};
            return this;
        }

        @Override
        public RecordingTileModel setUvs(
                int face,
                float u1, float v1,
                float u2, float v2,
                float u3, float v3
        ) {
            super.setUvs(face, u1, v1, u2, v2, u3, v3);
            face(face).uvs = new float[]{u1, v1, u2, v2, u3, v3};
            return this;
        }

        @Override
        public RecordingTileModel setAOs(int face, float ao1, float ao2, float ao3) {
            super.setAOs(face, ao1, ao2, ao3);
            face(face).aos = new float[]{ao1, ao2, ao3};
            return this;
        }

        @Override
        public RecordingTileModel setColor(int face, float red, float green, float blue) {
            super.setColor(face, red, green, blue);
            face(face).color = new float[]{red, green, blue};
            return this;
        }

        @Override
        public RecordingTileModel setSunlight(int face, int sunlight) {
            super.setSunlight(face, sunlight);
            face(face).sunlight = sunlight;
            return this;
        }

        @Override
        public RecordingTileModel setBlocklight(int face, int blocklight) {
            super.setBlocklight(face, blocklight);
            face(face).blocklight = blocklight;
            return this;
        }

        @Override
        public RecordingTileModel setMaterialIndex(int face, int material) {
            super.setMaterialIndex(face, material);
            face(face).material = material;
            return this;
        }
    }

    private static final class FaceData {
        private float[] positions;
        private float[] uvs;
        private float[] aos;
        private float[] color;
        private int sunlight;
        private int blocklight;
        private int material;

        float[] positions() {
            return positions;
        }

        float[] uvs() {
            return uvs;
        }

        float[] aos() {
            return aos;
        }

        float[] color() {
            return color;
        }

        int sunlight() {
            return sunlight;
        }

        int blocklight() {
            return blocklight;
        }

        int material() {
            return material;
        }
    }

    private record Fixture(
            Map<Position, BlockState> states,
            Map<Position, BlockEntity> blockEntities,
            ResourcePack resourcePack,
            TextureGallery gallery,
            BlockNeighborhood neighborhood,
            Map<Position, LightData> lights,
            RenderSettings renderSettings,
            ProfileActivation activation,
            CableBusRenderer renderer
    ) {
    }

    private record NativeRuntime(
            NativeStructuralRouteActivation activation,
            CableBusRenderer renderer
    ) {
    }

    private record TestBlockEntity(Key id, Position position) implements BlockEntity {
        @Override
        public Key getId() {
            return id;
        }

        @Override
        public int getX() {
            return position.x();
        }

        @Override
        public int getY() {
            return position.y();
        }

        @Override
        public int getZ() {
            return position.z();
        }

        @Override
        public boolean isKeepPacked() {
            return false;
        }
    }

    private record Position(int x, int y, int z) {
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
