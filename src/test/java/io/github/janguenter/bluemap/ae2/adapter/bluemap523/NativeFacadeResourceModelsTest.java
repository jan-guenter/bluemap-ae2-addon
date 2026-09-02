/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.adapter.bluemap523;

import com.flowpowered.math.vector.Vector3f;
import com.flowpowered.math.vector.Vector4f;
import de.bluecolored.bluemap.core.map.hires.block.BlockRendererType;
import de.bluecolored.bluemap.core.resources.ResourcePath;
import de.bluecolored.bluemap.core.resources.pack.PackVersion;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.Multipart;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.Variant;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.VariantSet;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.Variants;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.model.Element;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.model.Face;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.model.Model;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.model.Rotation;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.model.TextureVariable;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.texture.AnimationMeta;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.texture.Texture;
import de.bluecolored.bluemap.core.util.Direction;
import de.bluecolored.bluemap.core.util.Key;
import de.bluecolored.bluemap.core.util.math.Axis;
import de.bluecolored.bluemap.core.world.BlockState;
import io.github.janguenter.bluemap.ae2.model.Direction6;
import io.github.janguenter.bluemap.ae2.model.FacadeGeometry;
import io.github.janguenter.bluemap.ae2.model.FacadeSnapshot;
import io.github.janguenter.bluemap.ae2.profile.Ae219217NativeStructuralProfile;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NativeFacadeResourceModelsTest {

    private static final Key TEXTURE = Key.parse("minecraft:block/oak_leaves");
    private static final Key MODEL = Key.parse("minecraft:block/oak_leaves");
    private static final Key BLOCK = Key.parse("minecraft:oak_leaves");

    @Test
    void acceptsPersistedTintedTransparentStaticFullCubeState() throws Exception {
        ResourcePack pack = pack(0x80ffffff, 0, 16F);
        FacadeSnapshot state = new FacadeSnapshot(
                BLOCK.getFormatted(),
                Map.of("distance", "1", "persistent", "true", "waterlogged", "false")
        );

        NativeFacadeResourceModels.FacadeMaterial material =
                NativeFacadeResourceModels.resolve(pack, state);
        assertEquals(BLOCK, material.blockState().getId());
        assertEquals(state.properties(), material.blockState().getProperties());
        assertFalse(material.opaque());
        assertTrue(material.ambientOcclusion());
        for (Direction6 direction : Direction6.values()) {
            assertEquals(TEXTURE, material.texture(direction));
            assertEquals(0, material.tintIndex(direction));
        }
    }

    @Test
    void acceptsAnimatedOpaqueStaticFullCubeMaterial() throws Exception {
        AnimationMeta animation = new AnimationMeta(
                false,
                16,
                16,
                2,
                List.of(new AnimationMeta.FrameMeta(0, 2))
        );
        ResourcePack pack = pack(0xffffffff, -1, 16F, animation);
        Key magmaBlock = Key.parse("minecraft:magma_block");
        pack.getBlockStates().put(
                magmaBlock,
                pack.getBlockStates().get(BLOCK)
        );

        NativeFacadeResourceModels.FacadeMaterial material =
                NativeFacadeResourceModels.resolve(
                        pack,
                        new FacadeSnapshot(magmaBlock.getFormatted(), Map.of())
                );

        assertEquals(magmaBlock, material.blockState().getId());
        assertTrue(material.opaque());
        assertEquals(animation, pack.getTextures().get(TEXTURE).getAnimation());
    }

    @Test
    void acceptsUntintedOpaqueStaticFullCubeAndRejectsUnsupportedTopology()
            throws Exception {
        ResourcePack opaque = pack(0xffffffff, -1, 16F);
        NativeFacadeResourceModels.FacadeMaterial material =
                NativeFacadeResourceModels.resolve(
                        opaque,
                        new FacadeSnapshot(BLOCK.getFormatted(), Map.of())
                );
        assertTrue(material.opaque());

        ResourcePack nonFullCube = pack(0xffffffff, -1, 8F);
        assertNull(NativeFacadeResourceModels.resolve(
                nonFullCube,
                new FacadeSnapshot(BLOCK.getFormatted(), Map.of())
        ));

        ResourcePack weighted = pack(0xffffffff, -1, 16F);
        Variant variant = new Variant(new ResourcePath<Model>(MODEL));
        Variant sameDescriptorDifferentWeight = new Variant(
                new ResourcePath<Model>(MODEL),
                0,
                0,
                0,
                false,
                2
        );
        weighted.getBlockStates().put(
                BLOCK,
                new de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.BlockState(
                        new Variants(
                                new VariantSet[0],
                                new VariantSet(variant, sameDescriptorDifferentWeight)
                        )
                )
        );
        assertTrue(NativeFacadeResourceModels.resolve(
                weighted,
                new FacadeSnapshot(BLOCK.getFormatted(), Map.of()),
                17,
                64,
                -5
        ).opaque());

        ResourcePack distinctWeighted = pack(0xffffffff, -1, 16F);
        Variant rotated = new Variant(
                new ResourcePath<Model>(MODEL),
                0,
                90,
                0,
                false,
                1
        );
        distinctWeighted.getBlockStates().put(
                BLOCK,
                new de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.BlockState(
                        new Variants(
                                new VariantSet[0],
                                new VariantSet(variant, rotated)
                        )
                )
        );
        assertNull(NativeFacadeResourceModels.resolve(
                distinctWeighted,
                new FacadeSnapshot(BLOCK.getFormatted(), Map.of()),
                17,
                64,
                -5
        ));
        assertNull(NativeFacadeResourceModels.resolve(
                opaque,
                new FacadeSnapshot("ae2:not_a_facade_block", Map.of())
        ));
    }

    @Test
    void acceptsBoundedDeterministicStaticMultipartLayers() throws Exception {
        ResourcePack multipart = pack(0xffffffff, -1, 16F);
        Variant first = new Variant(new ResourcePath<Model>(MODEL));
        Variant second = new Variant(new ResourcePath<Model>(MODEL));
        multipart.getBlockStates().put(
                BLOCK,
                new de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.BlockState(
                        new Multipart(new VariantSet[]{
                                new VariantSet(first),
                                new VariantSet(second)
                        })
                )
        );

        NativeFacadeResourceModels.FacadeMaterial material =
                NativeFacadeResourceModels.resolve(
                        multipart,
                        new FacadeSnapshot(BLOCK.getFormatted(), Map.of()),
                        11,
                        64,
                        -9
                );

        assertTrue(material.opaque());
        assertTrue(material.ambientOcclusion());
        for (Direction6 direction : Direction6.values()) {
            assertEquals(2, material.layers(direction).size());
            assertEquals(TEXTURE, material.layers(direction).get(0).texture());
            assertEquals(TEXTURE, material.layers(direction).get(1).texture());
        }
    }

    @Test
    void rejectsStairLikeAndUnsupportedMultipartModelsAtomically() throws Exception {
        ResourcePack stairLike = pack(0xffffffff, -1, 8F);
        assertNull(NativeFacadeResourceModels.resolve(
                stairLike,
                new FacadeSnapshot(BLOCK.getFormatted(), Map.of(
                        "facing", "east",
                        "half", "bottom",
                        "shape", "straight",
                        "waterlogged", "false"
                ))
        ));

        ResourcePack partialMultipart = pack(0xffffffff, -1, 8F);
        partialMultipart.getBlockStates().put(
                BLOCK,
                new de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.BlockState(
                        new Multipart(new VariantSet[]{new VariantSet(
                                new Variant(new ResourcePath<Model>(MODEL))
                        )})
                )
        );
        assertNull(NativeFacadeResourceModels.resolve(
                partialMultipart,
                new FacadeSnapshot(BLOCK.getFormatted(), Map.of("dynamic", "true"))
        ));

        ResourcePack customRenderer = pack(0xffffffff, -1, 16F);
        Variant dynamic = new Variant(new ResourcePath<Model>(MODEL));
        dynamic.setRenderer(BlockRendererType.LIQUID);
        customRenderer.getBlockStates().put(
                BLOCK,
                new de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.BlockState(
                        new Multipart(new VariantSet[]{new VariantSet(dynamic)})
                )
        );
        assertNull(NativeFacadeResourceModels.resolve(
                customRenderer,
                new FacadeSnapshot(BLOCK.getFormatted(), Map.of())
        ));

        ResourcePack unbounded = pack(0xffffffff, -1, 16F);
        VariantSet[] tooManyParts = new VariantSet[65];
        for (int index = 0; index < tooManyParts.length; index++) {
            tooManyParts[index] = new VariantSet(
                    new Variant(new ResourcePath<Model>(MODEL))
            );
        }
        unbounded.getBlockStates().put(
                BLOCK,
                new de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.BlockState(
                        new Multipart(tooManyParts)
                )
        );
        assertNull(NativeFacadeResourceModels.resolve(
                unbounded,
                new FacadeSnapshot(BLOCK.getFormatted(), Map.of())
        ));
    }

    @Test
    void rejectsMixedNonnegativeTintIndexesAtomically() throws Exception {
        ResourcePack pack = pack(0xffffffff, 0, 16F);
        EnumMap<Direction, Face> mixedFaces = new EnumMap<>(Direction.class);
        for (Direction direction : Direction.values()) {
            mixedFaces.put(direction, new Face(
                    new Vector4f(0, 0, 16, 16),
                    new TextureVariable(new ResourcePath<Texture>(TEXTURE)),
                    direction,
                    0,
                    direction == Direction.UP ? 1 : 0
            ));
        }
        pack.getModels().put(
                MODEL,
                new Model(new Element(
                        Vector3f.ZERO,
                        new Vector3f(16, 16, 16),
                        mixedFaces
                ))
        );

        assertNull(NativeFacadeResourceModels.resolve(
                pack,
                new FacadeSnapshot(BLOCK.getFormatted(), Map.of())
        ));
    }

    @Test
    void acceptsAndRetainsBoundedRotationUvlockShadeAndLightProjection()
            throws Exception {
        ResourcePack pack = pack(0xffffffff, -1, 16F);
        EnumMap<Direction, Face> angledFaces = new EnumMap<>(Direction.class);
        angledFaces.put(Direction.NORTH, new Face(
                new Vector4f(2, 3, 14, 15),
                new TextureVariable(new ResourcePath<Texture>(TEXTURE)),
                Direction.NORTH,
                90,
                -1
        ));
        Element angled = new Element(
                new Vector3f(2, 2, 4),
                new Vector3f(14, 14, 6),
                new Rotation(new Vector3f(8, 8, 8), Axis.Y, 22.5F, true),
                false,
                7,
                angledFaces
        );
        Model base = pack.getModels().get(MODEL);
        pack.getModels().put(
                MODEL,
                new Model(
                        Map.of(),
                        new Element[]{base.getElements()[0], angled},
                        true
                )
        );
        Variant transformed = new Variant(
                new ResourcePath<Model>(MODEL),
                0,
                90,
                0,
                true,
                1
        );
        pack.getBlockStates().put(
                BLOCK,
                new de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.BlockState(
                        new Variants(new VariantSet[0], new VariantSet(transformed))
                )
        );

        NativeFacadeResourceModels.FacadeMaterial material =
                NativeFacadeResourceModels.resolve(
                        pack,
                        new FacadeSnapshot(BLOCK.getFormatted(), Map.of())
                );
        assertNotNull(material);
        NativeFacadeResourceModels.FacadeLayer projected = material.layers().stream()
                .filter(layer -> !layer.shade())
                .findFirst()
                .orElseThrow();
        assertEquals(7, projected.lightEmission());
        assertEquals(4, projected.sourceVertices().size());
        assertEquals(14D, projected.sourceVertices().get(0).u16(), 0D);
        assertEquals(15D, projected.sourceVertices().get(0).v16(), 0D);
        assertEquals(14D, projected.sourceVertices().get(1).u16(), 0D);
        assertEquals(3D, projected.sourceVertices().get(1).v16(), 0D);
        assertEquals(2D, projected.sourceVertices().get(2).u16(), 0D);
        assertEquals(3D, projected.sourceVertices().get(2).v16(), 0D);
        assertEquals(2D, projected.sourceVertices().get(3).u16(), 0D);
        assertEquals(15D, projected.sourceVertices().get(3).v16(), 0D);
    }

    @Test
    void retainsSlantedSourceNormalForTopOnlyAndMapProjection() throws Exception {
        ResourcePack pack = pack(0xffffffff, -1, 16F);
        EnumMap<Direction, Face> angledFaces = new EnumMap<>(Direction.class);
        angledFaces.put(Direction.NORTH, new Face(
                new Vector4f(0, 0, 16, 16),
                new TextureVariable(new ResourcePath<Texture>(TEXTURE)),
                null,
                0,
                -1
        ));
        Element angled = new Element(
                new Vector3f(2, 2, 4),
                new Vector3f(14, 14, 6),
                new Rotation(new Vector3f(8, 8, 8), Axis.X, 60F, false),
                true,
                0,
                angledFaces
        );
        Model base = pack.getModels().get(MODEL);
        pack.getModels().put(
                MODEL,
                new Model(
                        Map.of(),
                        new Element[]{base.getElements()[0], angled},
                        true
                )
        );

        NativeFacadeResourceModels.FacadeMaterial material =
                NativeFacadeResourceModels.resolve(
                        pack,
                        new FacadeSnapshot(BLOCK.getFormatted(), Map.of())
                );

        assertNotNull(material);
        NativeFacadeResourceModels.FacadeLayer slanted = material.layers().stream()
                .filter(layer -> layer.lightFace() == Direction6.NORTH
                        && layer.sourceNormalY() > 0.01F)
                .findFirst()
                .orElseThrow();
        assertEquals(Direction6.UP, slanted.nominalFace());
        assertEquals(Direction6.NORTH, slanted.lightFace());
    }

    @Test
    void rejectsFaceNormalRotationWithoutExactProjectedInterpolationGrid()
            throws Exception {
        ResourcePack pack = pack(0xffffffff, -1, 16F);
        EnumMap<Direction, Face> rotatedFaces = new EnumMap<>(Direction.class);
        rotatedFaces.put(Direction.NORTH, new Face(
                new Vector4f(1, 2, 13, 15),
                new TextureVariable(new ResourcePath<Texture>(TEXTURE)),
                Direction.NORTH,
                90,
                -1
        ));
        Element rotated = new Element(
                new Vector3f(2, 2, 4),
                new Vector3f(14, 14, 6),
                new Rotation(new Vector3f(8, 8, 8), Axis.Z, 22.5F, true),
                true,
                0,
                rotatedFaces
        );
        Model base = pack.getModels().get(MODEL);
        pack.getModels().put(
                MODEL,
                new Model(
                        Map.of(),
                        new Element[]{base.getElements()[0], rotated},
                        true
                )
        );

        NativeFacadeResourceModels.FacadeMaterial material =
                NativeFacadeResourceModels.resolve(
                        pack,
                        new FacadeSnapshot(BLOCK.getFormatted(), Map.of())
                );

        assertNull(material);
    }

    @Test
    void normalizesNativeMachineStateAndPreservesMonitorFacing() throws Exception {
        ResourcePack pack = pack(0xffffffff, -1, 16F);
        Key controller = Key.parse("ae2:controller");
        Key monitor = Key.parse("ae2:crafting_monitor");
        pack.getBlockStates().put(controller, pack.getBlockStates().get(BLOCK));
        pack.getBlockStates().put(monitor, pack.getBlockStates().get(BLOCK));

        NativeFacadeResourceModels.FacadeMaterial controllerMaterial =
                NativeFacadeResourceModels.resolve(
                        pack,
                        new FacadeSnapshot(controller.getFormatted(), Map.of(
                                "state", "online",
                                "type", "inside_b"
                        ))
                );
        assertEquals(
                Map.of("state", "offline", "type", "block"),
                controllerMaterial.blockState().getProperties()
        );

        NativeFacadeResourceModels.FacadeMaterial monitorMaterial =
                NativeFacadeResourceModels.resolve(
                        pack,
                        new FacadeSnapshot(monitor.getFormatted(), Map.of(
                                "facing", "east",
                                "formed", "true",
                                "powered", "true",
                                "spin", "3"
                        ))
                );
        assertEquals(
                Map.of(
                        "facing", "east",
                        "formed", "false",
                        "powered", "false",
                        "spin", "0"
                ),
                monitorMaterial.blockState().getProperties()
        );
        assertNull(NativeFacadeResourceModels.resolve(
                pack,
                new FacadeSnapshot(monitor.getFormatted(), Map.of(
                        "facing", "sideways",
                        "formed", "true",
                        "powered", "true",
                        "spin", "3"
                ))
        ));
    }

    @Test
    void quartzFacadesUseFacadeAwareMasksAndRemainNonEmissive() throws Exception {
        ResourcePack pack = pack(0xffffffff, -1, 16F);
        for (Key texture : M3cQuartzGlassResourceModels.requiredTextures()) {
            pack.getTextures().put(texture, texture(texture, 0xffffffff, null));
        }
        EnumMap<Direction6, Integer> frameMasks = new EnumMap<>(Direction6.class);
        for (Direction6 direction : Direction6.values()) {
            frameMasks.put(direction, direction == Direction6.UP ? 5 : 15);
        }
        NativeFacadeResourceModels.QuartzFacadeAppearance appearance =
                new NativeFacadeResourceModels.QuartzFacadeAppearance(
                        Direction6.NORTH.maskBit(),
                        frameMasks
                );
        NativeFacadeResourceModels.FacadeMaterial material =
                NativeFacadeResourceModels.resolve(
                        pack,
                        new FacadeSnapshot("ae2:quartz_vibrant_glass", Map.of()),
                        13,
                        64,
                        -7,
                        appearance
                );
        assertNotNull(material);
        assertFalse(material.opaque());
        assertTrue(material.layers(Direction6.NORTH).isEmpty());
        assertEquals(2, material.layers(Direction6.UP).size());
        assertEquals(
                "ae2:block/glass/quartz_glass_frame0101",
                material.layers(Direction6.UP).get(1).texture().getFormatted()
        );
        assertTrue(material.layers().stream().allMatch(
                layer -> layer.lightEmission() == 0 && !layer.ambientOcclusion()
        ));
        assertNull(NativeFacadeResourceModels.resolve(
                pack,
                new FacadeSnapshot("ae2:quartz_glass", Map.of())
        ));
    }

    @Test
    void fullySurroundedQuartzFacadeIsAValidZeroQuadMaterial() throws Exception {
        ResourcePack pack = pack(0xffffffff, -1, 16F);
        for (Key texture : M3cQuartzGlassResourceModels.requiredTextures()) {
            pack.getTextures().put(texture, texture(texture, 0xffffffff, null));
        }
        EnumMap<Direction6, Integer> frames = new EnumMap<>(Direction6.class);
        for (Direction6 direction : Direction6.values()) {
            frames.put(direction, 0);
        }
        for (String blockId : List.of(
                "ae2:quartz_glass",
                "ae2:quartz_vibrant_glass"
        )) {
            NativeFacadeResourceModels.FacadeMaterial material =
                    NativeFacadeResourceModels.resolve(
                            pack,
                            new FacadeSnapshot(blockId, Map.of()),
                            0,
                            80,
                            0,
                            new NativeFacadeResourceModels.QuartzFacadeAppearance(
                                    63,
                                    frames
                            )
                    );

            assertNotNull(material, blockId);
            assertFalse(material.opaque(), blockId);
            assertTrue(material.layers().isEmpty(), blockId);
        }
    }

    @Test
    void exactResourcesResolveAllTwentyFourExplicitWhitelistDefaults()
            throws Exception {
        ResourcePack pack = exactResourcePack();
        Map<String, Map<String, String>> defaults = explicitWhitelistDefaults();
        assertEquals(
                Ae219217NativeStructuralProfile.facadeWhitelistBlockIds(),
                defaults.keySet().stream().toList()
        );
        EnumMap<Direction6, Integer> frames = new EnumMap<>(Direction6.class);
        for (Direction6 direction : Direction6.values()) {
            frames.put(direction, 15);
        }
        NativeFacadeResourceModels.QuartzFacadeAppearance isolated =
                new NativeFacadeResourceModels.QuartzFacadeAppearance(0, frames);
        for (Map.Entry<String, Map<String, String>> entry : defaults.entrySet()) {
            Key id = Key.parse(entry.getKey());
            NativeFacadeResourceModels.FacadeMaterial material =
                    NativeFacadeResourceModels.resolve(
                            pack,
                            new FacadeSnapshot(entry.getKey(), entry.getValue()),
                            7,
                            80,
                            -11,
                            NativeFacadeResourceModels.isNativeQuartzFacade(id)
                                    ? isolated : null
            );
            assertNotNull(material, entry.getKey());
            assertEquals(
                    Ae219217NativeStructuralProfile.facadeWhitelistNeutralStates()
                            .get(entry.getKey()).properties(),
                    entry.getValue(),
                    entry.getKey()
            );
            assertFalse(material.layers().isEmpty(), entry.getKey());
            assertEquals(
                    !Set.of(
                            "ae2:quartz_glass",
                            "ae2:quartz_vibrant_glass",
                            "minecraft:honey_block"
                    ).contains(entry.getKey()),
                    material.opaque(),
                    entry.getKey()
            );
            long clipped = material.layers().stream().flatMap(layer ->
                    FacadeGeometry.clip(
                            layer.nominalFace(),
                            layer.sourceVertices(),
                            Direction6.UP,
                            null,
                            Direction6.UP.maskBit(),
                            !material.opaque()
                    ).stream()
            ).count();
            assertTrue(clipped > 0, entry.getKey());
        }
    }

    @Test
    void explicitWhitelistAcceptsEveryStateDomainAndRejectsMalformedStates()
            throws Exception {
        ResourcePack pack = exactResourcePack();
        Map<String, Map<String, String>> defaults = explicitWhitelistDefaults();
        Map<String, Integer> expectedCounts = explicitWhitelistStateCounts();
        assertEquals(
                Ae219217NativeStructuralProfile.facadeWhitelistBlockIds(),
                Ae219217NativeStructuralProfile.facadeWhitelistStateSchemas()
                        .keySet().stream().toList()
        );
        assertEquals(
                Ae219217NativeStructuralProfile.facadeWhitelistBlockIds(),
                expectedCounts.keySet().stream().toList()
        );
        int validStates = 0;
        for (String blockId : Ae219217NativeStructuralProfile.facadeWhitelistBlockIds()) {
            Map<String, List<String>> schema =
                    Ae219217NativeStructuralProfile.facadeWhitelistStateSchema(blockId);
            Map<String, String> defaultState = defaults.get(blockId);
            assertEquals(schema.keySet(), defaultState.keySet(), blockId);
            List<Map<String, String>> states = cartesianStates(schema);
            assertEquals(expectedCounts.get(blockId), states.size(), blockId);
            for (Map<String, String> state : states) {
                NativeFacadeResourceModels.FacadeMaterial material =
                        resolveExplicit(pack, blockId, state);
                assertNotNull(material, blockId + " " + state);
                assertEquals(
                        expectedProjectedState(blockId, state),
                        material.blockState().getProperties(),
                        blockId + " " + state
                );
                assertEquals(
                        Ae219217NativeStructuralProfile.facadeWhitelistNeutralStates()
                                .get(blockId).solidRender(),
                        material.opaque(),
                        blockId + " " + state
                );
            }
            validStates += states.size();

            LinkedHashMap<String, String> extra = new LinkedHashMap<>(defaultState);
            extra.put("not_a_persisted_property", "false");
            assertNull(resolveExplicit(pack, blockId, extra), blockId + " extra");

            for (Map.Entry<String, List<String>> property : schema.entrySet()) {
                LinkedHashMap<String, String> missing = new LinkedHashMap<>(defaultState);
                missing.remove(property.getKey());
                assertNull(
                        resolveExplicit(pack, blockId, missing),
                        blockId + " missing " + property.getKey()
                );

                LinkedHashMap<String, String> invalid = new LinkedHashMap<>(defaultState);
                invalid.put(property.getKey(), "not_a_domain_value");
                assertNull(
                        resolveExplicit(pack, blockId, invalid),
                        blockId + " invalid " + property.getKey()
                );

            }
        }
        assertEquals(554, validStates);
        assertEquals(
                Ae219217NativeStructuralProfile.FACADE_WHITELIST_STATE_COMBINATION_COUNT,
                validStates
        );
    }

    @Test
    void sameStateLogKeepsTwelveTrianglesWhileGlassSkipsOnlyItsOutwardPair()
            throws Exception {
        ResourcePack pack = exactResourcePack();
        for (FacadeSnapshot snapshot : List.of(
                new FacadeSnapshot("minecraft:oak_log", Map.of("axis", "x")),
                new FacadeSnapshot("minecraft:glass", Map.of())
        )) {
            NativeFacadeResourceModels.FacadeMaterial material =
                    NativeFacadeResourceModels.resolve(pack, snapshot);
            assertNotNull(material, snapshot.blockId());
            int triangles = 0;
            for (NativeFacadeResourceModels.FacadeLayer layer : material.layers()) {
                BlockState appearance = layer.cullFace() == Direction6.UP
                        ? material.blockState() : BlockState.AIR;
                if (layer.cullFace() != null && CableBusRenderer.skipsFacadeRendering(
                        pack,
                        material.blockState(),
                        appearance
                )) {
                    continue;
                }
                triangles += 2 * FacadeGeometry.clip(
                        layer.nominalFace(),
                        layer.sourceVertices(),
                        Direction6.UP,
                        null,
                        0,
                        !material.opaque()
                ).size();
            }
            assertEquals(
                    snapshot.blockId().equals("minecraft:oak_log") ? 12 : 10,
                    triangles,
                    snapshot.blockId()
            );
        }
    }

    @Test
    void exactSkipRenderingTablesCoverHoneyQuartzGlassLogAndLeaves()
            throws Exception {
        ResourcePack pack = exactResourcePack();
        BlockState quartz = new BlockState(Key.parse("ae2:quartz_glass"), Map.of());
        BlockState vibrant = new BlockState(
                Key.parse("ae2:quartz_vibrant_glass"),
                Map.of()
        );
        BlockState honey = new BlockState(Key.parse("minecraft:honey_block"), Map.of());
        BlockState glass = new BlockState(Key.parse("minecraft:glass"), Map.of());
        BlockState log = new BlockState(
                Key.parse("minecraft:oak_log"),
                Map.of("axis", "x")
        );
        BlockState leaves = new BlockState(
                Key.parse("minecraft:oak_leaves"),
                Map.of("distance", "1", "persistent", "true", "waterlogged", "false")
        );

        assertTrue(CableBusRenderer.skipsFacadeRendering(pack, quartz, vibrant));
        assertTrue(CableBusRenderer.skipsFacadeRendering(pack, vibrant, quartz));
        assertTrue(CableBusRenderer.skipsFacadeRendering(pack, honey, honey));
        assertTrue(CableBusRenderer.skipsFacadeRendering(pack, glass, glass));
        assertFalse(CableBusRenderer.skipsFacadeRendering(pack, log, log));
        assertFalse(CableBusRenderer.skipsFacadeRendering(pack, leaves, leaves));
        assertFalse(CableBusRenderer.skipsFacadeRendering(pack, honey, glass));
    }

    @Test
    void exactWeightedStoneRetainsTheAcceptedOpaqueMaterialProjection()
            throws Exception {
        ResourcePack pack = exactResourcePack();

        NativeFacadeResourceModels.FacadeMaterial material =
                NativeFacadeResourceModels.resolve(
                        pack,
                        new FacadeSnapshot("minecraft:stone", Map.of()),
                        17,
                        64,
                        -5
                );

        assertNotNull(material);
        assertTrue(material.opaque());
        assertEquals(
                Set.of(Key.parse("minecraft:block/stone")),
                material.layers().stream()
                        .map(NativeFacadeResourceModels.FacadeLayer::texture)
                        .collect(java.util.stream.Collectors.toSet())
        );
    }

    private static ResourcePack pack(int argb, int tintIndex, float extent)
            throws IOException {
        return pack(argb, tintIndex, extent, null);
    }

    private static ResourcePack pack(
            int argb,
            int tintIndex,
            float extent,
            AnimationMeta animation
    ) throws IOException {
        ResourcePack pack = new ResourcePack(new PackVersion(34, 0));
        BufferedImage image = new BufferedImage(2, 1, BufferedImage.TYPE_INT_ARGB);
        image.setRGB(0, 0, argb);
        image.setRGB(1, 0, argb == 0x80ffffff ? 0x00ffffff : argb);
        pack.getTextures().put(TEXTURE, Texture.from(TEXTURE, image, animation));

        EnumMap<Direction, Face> faces = new EnumMap<>(Direction.class);
        for (Direction direction : Direction.values()) {
            faces.put(direction, new Face(
                    new Vector4f(0, 0, 16, 16),
                    new TextureVariable(new ResourcePath<Texture>(TEXTURE)),
                    direction,
                    0,
                    tintIndex
            ));
        }
        pack.getModels().put(
                MODEL,
                new Model(
                        Map.of(),
                        new Element[]{new Element(
                                Vector3f.ZERO,
                                new Vector3f(extent, extent, extent),
                                faces
                        )},
                        true
                )
        );
        Variant variant = new Variant(new ResourcePath<Model>(MODEL));
        pack.getBlockStates().put(
                BLOCK,
                new de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.BlockState(
                        new Variants(new VariantSet[0], new VariantSet(variant))
                )
        );
        return pack;
    }

    private static ResourcePack exactResourcePack() throws Exception {
        Path ae2Jar = requiredPinnedPath("bluemapAe2.testAe2Jar");
        Path minecraftClientJar = requiredPinnedPath(
                "bluemapAe2.testMinecraftClientJar"
        );
        Path blueMapSource = requiredPinnedPath(
                "bluemapAe2.testBlueMapSourcePath"
        );
        ResourcePack pack = new ResourcePack(new PackVersion(34, 0));
        pack.loadResources(List.of(
                blueMapSource.resolve("core/src/main/resourceExtensions"),
                ae2Jar,
                minecraftClientJar
        ));
        for (Key key : M3cQuartzGlassResourceModels.requiredTextures()) {
            if (pack.getTextures().get(key) != null) {
                continue;
            }
            String entryName = "assets/" + key.getNamespace() + "/textures/"
                    + key.getValue() + ".png";
            BufferedImage image;
            try (ZipFile archive = new ZipFile(ae2Jar.toFile())) {
                ZipEntry entry = archive.getEntry(entryName);
                assertNotNull(entry, entryName);
                image = ImageIO.read(archive.getInputStream(entry));
            }
            assertNotNull(image, entryName);
            pack.getTextures().put(key, Texture.from(key, image));
        }
        return pack;
    }

    private static Path requiredPinnedPath(String property) {
        String value = System.getProperty(property, "");
        assertFalse(value.isBlank(), property);
        Path path = Path.of(value);
        assertTrue(Files.exists(path), path.toString());
        return path;
    }

    private static Map<String, Map<String, String>> explicitWhitelistDefaults() {
        LinkedHashMap<String, Map<String, String>> states = new LinkedHashMap<>();
        for (String blockId : Ae219217NativeStructuralProfile.facadeWhitelistBlockIds()) {
            Ae219217NativeStructuralProfile.NeutralFacadeMaterial neutral =
                    Ae219217NativeStructuralProfile.nativeFacadeNeutralMaterials()
                            .get(blockId);
            if (neutral != null) {
                states.put(blockId, neutral.properties());
                continue;
            }
            states.put(blockId, switch (blockId) {
                case "minecraft:chiseled_bookshelf" -> Map.of(
                        "facing", "north",
                        "slot_0_occupied", "false",
                        "slot_1_occupied", "false",
                        "slot_2_occupied", "false",
                        "slot_3_occupied", "false",
                        "slot_4_occupied", "false",
                        "slot_5_occupied", "false"
                );
                case "minecraft:jukebox" -> Map.of("has_record", "false");
                case "minecraft:furnace", "minecraft:blast_furnace" -> Map.of(
                        "facing", "north", "lit", "false"
                );
                case "minecraft:dropper", "minecraft:dispenser" -> Map.of(
                        "facing", "north", "triggered", "false"
                );
                case "minecraft:crafter" -> Map.of(
                        "crafting", "false",
                        "orientation", "north_up",
                        "triggered", "false"
                );
                case "minecraft:barrel" -> Map.of(
                        "facing", "north", "open", "false"
                );
                case "minecraft:bee_nest", "minecraft:beehive" -> Map.of(
                        "facing", "north", "honey_level", "0"
                );
                case "minecraft:sculk_catalyst" -> Map.of("bloom", "false");
                case "minecraft:soul_sand", "minecraft:honey_block" -> Map.of();
                default -> throw new IllegalStateException(
                        "missing explicit whitelist default for " + blockId
                );
            });
        }
        return java.util.Collections.unmodifiableMap(states);
    }

    private static Map<String, Integer> explicitWhitelistStateCounts() {
        LinkedHashMap<String, Integer> counts = new LinkedHashMap<>();
        counts.put("ae2:quartz_glass", 1);
        counts.put("ae2:quartz_vibrant_glass", 1);
        counts.put("minecraft:chiseled_bookshelf", 256);
        counts.put("minecraft:jukebox", 2);
        counts.put("minecraft:furnace", 8);
        counts.put("minecraft:blast_furnace", 8);
        counts.put("minecraft:dropper", 12);
        counts.put("minecraft:dispenser", 12);
        counts.put("minecraft:crafter", 48);
        counts.put("minecraft:barrel", 12);
        counts.put("minecraft:bee_nest", 24);
        counts.put("minecraft:beehive", 24);
        counts.put("minecraft:sculk_catalyst", 2);
        counts.put("minecraft:soul_sand", 1);
        counts.put("minecraft:honey_block", 1);
        counts.put("ae2:controller", 18);
        counts.put("ae2:1k_crafting_storage", 4);
        counts.put("ae2:4k_crafting_storage", 4);
        counts.put("ae2:16k_crafting_storage", 4);
        counts.put("ae2:64k_crafting_storage", 4);
        counts.put("ae2:256k_crafting_storage", 4);
        counts.put("ae2:crafting_monitor", 96);
        counts.put("ae2:crafting_unit", 4);
        counts.put("ae2:crafting_accelerator", 4);
        return java.util.Collections.unmodifiableMap(counts);
    }

    private static List<Map<String, String>> cartesianStates(
            Map<String, List<String>> schema
    ) {
        List<Map<String, String>> states = new ArrayList<>();
        states.add(new LinkedHashMap<>());
        for (Map.Entry<String, List<String>> property : schema.entrySet()) {
            List<Map<String, String>> expanded = new ArrayList<>();
            for (Map<String, String> state : states) {
                for (String value : property.getValue()) {
                    LinkedHashMap<String, String> candidate = new LinkedHashMap<>(state);
                    candidate.put(property.getKey(), value);
                    expanded.add(candidate);
                }
            }
            states = expanded;
        }
        return states.stream().map(state ->
                java.util.Collections.unmodifiableMap(new LinkedHashMap<>(state))
        ).toList();
    }

    private static NativeFacadeResourceModels.FacadeMaterial resolveExplicit(
            ResourcePack pack,
            String blockId,
            Map<String, String> properties
    ) {
        if (!NativeFacadeResourceModels.isNativeQuartzFacade(Key.parse(blockId))) {
            return NativeFacadeResourceModels.resolve(
                    pack,
                    new FacadeSnapshot(blockId, properties),
                    7,
                    80,
                    -11
            );
        }
        EnumMap<Direction6, Integer> frames = new EnumMap<>(Direction6.class);
        for (Direction6 direction : Direction6.values()) {
            frames.put(direction, 15);
        }
        return NativeFacadeResourceModels.resolve(
                pack,
                new FacadeSnapshot(blockId, properties),
                7,
                80,
                -11,
                new NativeFacadeResourceModels.QuartzFacadeAppearance(0, frames)
        );
    }

    private static Map<String, String> expectedProjectedState(
            String blockId,
            Map<String, String> properties
    ) {
        Ae219217NativeStructuralProfile.NeutralFacadeMaterial neutral =
                Ae219217NativeStructuralProfile.nativeFacadeNeutralMaterials()
                        .get(blockId);
        if (neutral == null) {
            return properties;
        }
        LinkedHashMap<String, String> projected = new LinkedHashMap<>();
        neutral.normalization().forEach((key, value) -> projected.put(
                key,
                "preserve".equals(value) ? properties.get(key) : value
        ));
        return projected;
    }

    private static Texture texture(
            Key key,
            int argb,
            AnimationMeta animation
    ) throws IOException {
        BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        image.setRGB(0, 0, argb);
        return Texture.from(key, image, animation);
    }
}
