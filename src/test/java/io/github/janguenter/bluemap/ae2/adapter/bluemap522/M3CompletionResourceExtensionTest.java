/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.adapter.bluemap522;

import de.bluecolored.bluemap.core.resources.adapter.ResourcesGson;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.util.Key;
import de.bluecolored.bluemap.core.world.BlockProperties;
import de.bluecolored.bluemap.core.world.BlockState;
import io.github.janguenter.bluemap.ae2.activation.CraftingRouteActivation;
import io.github.janguenter.bluemap.ae2.activation.DriveRouteActivation;
import io.github.janguenter.bluemap.ae2.activation.ExtendedAeDriveRouteActivation;
import io.github.janguenter.bluemap.ae2.activation.M3CompletionRouteActivation;
import io.github.janguenter.bluemap.ae2.activation.ProfileActivation;
import io.github.janguenter.bluemap.ae2.activation.QuantumBridgeRouteActivation;
import io.github.janguenter.bluemap.ae2.activation.QuartzGlassRouteActivation;
import io.github.janguenter.bluemap.ae2.profile.Ae219217Profile;
import io.github.janguenter.bluemap.ae2.profile.ExactArtifactDetector;
import io.github.janguenter.bluemap.ae2.profile.ProfileDisablement;
import io.github.janguenter.bluemap.ae2.profile.extendedae.ExtendedAe2233Profile;
import io.github.janguenter.bluemap.ae2.profile.extendedae.ExtendedAeArtifactDetector;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class M3CompletionResourceExtensionTest {

    private static final Map<Key, Key> ROUTES = Map.ofEntries(
            route("ae2:paint", "bluemap_ae2:paint"),
            route("ae2:sky_stone_chest", "bluemap_ae2:sky_stone_chest"),
            route("ae2:smooth_sky_stone_chest", "bluemap_ae2:sky_stone_chest"),
            route("ae2:crank", "bluemap_ae2:crank"),
            route("ae2:inscriber", "bluemap_ae2:inscriber"),
            route("ae2:spatial_pylon", "bluemap_ae2:spatial_pylon")
    );

    @Test
    void oneActivationRoutesAllSixIdsAndPreservesAcceptedCoreRoute() {
        ProfileActivation core = new ProfileActivation();
        M3CompletionRouteActivation completion = new M3CompletionRouteActivation();
        Ae2ResourceExtension extension = new Ae2ResourceExtension(
                null,
                core,
                new DriveRouteActivation(),
                new ExtendedAeDriveRouteActivation(),
                new QuartzGlassRouteActivation(),
                new CraftingRouteActivation(),
                new QuantumBridgeRouteActivation(),
                completion
        );
        core.activate();

        for (Key physical : ROUTES.keySet()) {
            assertEquals(physical, extension.getBlockStateKey(physical));
        }
        completion.activate();
        ROUTES.forEach((physical, synthetic) ->
                assertEquals(synthetic, extension.getBlockStateKey(physical)));

        BlockProperties.Builder builder = BlockProperties.builder()
                .culling(true)
                .occluding(true)
                .cullingIdentical(true);
        extension.getBlockProperties(
                BlockState.fromString("ae2:inscriber[facing=north,spin=0,waterlogged=false]"),
                builder
        );
        BlockProperties properties = builder.build();
        assertFalse(properties.isCulling());
        assertFalse(properties.isOccluding());
        assertFalse(properties.getCullingIdentical());

        completion.disable(M3CompletionRouteActivation.Reason.RENDER_CALLBACK_FAILED);
        ROUTES.keySet().forEach(physical ->
                assertEquals(physical, extension.getBlockStateKey(physical)));
        assertEquals(
                Key.parse(Ae219217Profile.SYNTHETIC_BLOCK_STATE),
                extension.getBlockStateKey(Key.parse(Ae219217Profile.CABLE_BUS_BLOCK))
        );
    }

    @Test
    void exactStatesAndSharedSyntheticRendererAreClosed() {
        assertTrue(Ae2ResourceExtension.isExactM3CompletionState(
                BlockState.fromString("ae2:paint[facing=up,light_level=2]")
        ));
        assertTrue(Ae2ResourceExtension.isExactM3CompletionState(
                BlockState.fromString(
                        "ae2:smooth_sky_stone_chest[facing=east,waterlogged=true]"
                )
        ));
        assertTrue(Ae2ResourceExtension.isExactM3CompletionState(
                BlockState.fromString("ae2:crank[facing=down]")
        ));
        assertTrue(Ae2ResourceExtension.isExactM3CompletionState(
                BlockState.fromString(
                        "ae2:inscriber[facing=south,spin=3,waterlogged=false]"
                )
        ));
        assertTrue(Ae2ResourceExtension.isExactM3CompletionState(
                BlockState.fromString("ae2:spatial_pylon[powered_on=false]")
        ));
        assertFalse(Ae2ResourceExtension.isExactM3CompletionState(
                BlockState.fromString("ae2:crank[facing=north,future=false]")
        ));
        assertFalse(Ae2ResourceExtension.isExactM3CompletionState(
                BlockState.fromString("ae2:paint[facing=up,light_level=3]")
        ));

        assertTrue(BlueMap522Adapter.install());
        assertTrue(Ae2ResourceExtension.isExpectedM3CompletionSyntheticBlockState(
                completionSynthetic()
        ));
        assertFalse(Ae2ResourceExtension.isExpectedM3CompletionSyntheticBlockState(
                parse("""
                        {"variants":{"":{"renderer":"bluemap:default",
                                           "model":"bluemap:block/missing"}}}
                        """)
        ));
    }

    @Test
    void exactLoadAndBakeActivateTheCombinedRouteAndCollectItsClosure()
            throws Exception {
        LoadFixture fixture = loadFixture(() -> true, () -> true, ignored -> true);

        fixture.extension().loadResources(List.of());

        assertAcceptedRoutesAndCompletionActive(fixture);
        assertTrue(fixture.extension().collectUsedTextureKeys().containsAll(
                M3CompletionResourceModels.requiredTextures()
        ));
        fixture.extension().bake();
        assertTrue(fixture.completion().isActive(), fixture.completion().reason());
    }

    @Test
    void missingSyntheticAndProfileLinkageFailureStayRouteLocal() throws Exception {
        LoadFixture missing = loadFixture(() -> true, () -> true, ignored -> true);
        missing.resourcePack().getBlockStates().remove(Key.parse("bluemap_ae2:crank"));
        missing.extension().loadResources(List.of());
        assertTrue(missing.core().isActive(), missing.core().reason());
        assertFalse(missing.completion().isActive());
        assertEquals(
                "m3-completion-synthetic-blockstate-missing",
                missing.completion().reason()
        );

        LoadFixture linkage = loadFixture(
                () -> {
                    throw new NoClassDefFoundError("injected-m3-completion-profile");
                },
                () -> true,
                ignored -> true
        );
        assertDoesNotThrow(() -> linkage.extension().loadResources(List.of()));
        assertTrue(linkage.core().isActive(), linkage.core().reason());
        assertTrue(linkage.completion().isDisabled());
        assertEquals(
                "m3-completion-resource-load-callback-failed",
                linkage.completion().reason()
        );
    }

    @Test
    void operatorDisablementPrecedesLazyProfileAccess() throws Exception {
        String property = ProfileDisablement.SYSTEM_PROPERTY;
        String previous = System.getProperty(property);
        try {
            System.setProperty(property, "ae2-m3-completion");
            LoadFixture fixture = loadFixture(
                    () -> {
                        throw new AssertionError("disabled route touched profile");
                    },
                    () -> true,
                    ignored -> true
            );
            fixture.extension().loadResources(List.of());
            assertTrue(fixture.core().isActive(), fixture.core().reason());
            assertTrue(fixture.completion().isDisabled());
            assertEquals(
                    "m3-completion-disabled-by-operator",
                    fixture.completion().reason()
            );
        } finally {
            if (previous == null) {
                System.clearProperty(property);
            } else {
                System.setProperty(property, previous);
            }
        }
    }

    @Test
    void bakeResourceFailureDeactivatesOnlyCompletionRoute() throws Exception {
        LoadFixture fixture = loadFixture(() -> true, () -> true, ignored -> false);
        fixture.extension().loadResources(List.of());
        assertTrue(fixture.completion().isActive(), fixture.completion().reason());

        fixture.extension().bake();

        assertTrue(fixture.core().isActive(), fixture.core().reason());
        assertFalse(fixture.completion().isActive());
        assertEquals(
                "m3-completion-required-resources-mismatch",
                fixture.completion().reason()
        );
    }

    private static LoadFixture loadFixture(
            Ae2ResourceExtension.M3CompletionProfileProbe profileProbe,
            Ae2ResourceExtension.M3CompletionRetentionProbe retentionProbe,
            Ae2ResourceExtension.M3CompletionResourceProbe resourceProbe
    ) throws Exception {
        assertTrue(BlueMap522Adapter.install());
        ResourcePack resourcePack = M3CompletionResourceModelsTest.exactResources();
        Ae2ResourceExtensionTest.putValidM2Resources(resourcePack);
        putSyntheticStates(resourcePack);

        ProfileActivation core = new ProfileActivation();
        DriveRouteActivation drive = new DriveRouteActivation();
        ExtendedAeDriveRouteActivation extended = new ExtendedAeDriveRouteActivation();
        QuartzGlassRouteActivation glass = new QuartzGlassRouteActivation();
        CraftingRouteActivation crafting = new CraftingRouteActivation();
        QuantumBridgeRouteActivation quantum = new QuantumBridgeRouteActivation();
        M3CompletionRouteActivation completion = new M3CompletionRouteActivation();
        Ae2ResourceExtension extension = new Ae2ResourceExtension(
                resourcePack,
                core,
                drive,
                extended,
                glass,
                crafting,
                quantum,
                completion,
                ignored -> new ExactArtifactDetector.Detection(
                        true,
                        Ae219217Profile.EXACT_REASON
                ),
                ignored -> new ExtendedAeArtifactDetector.Detection(
                        true,
                        ExtendedAe2233Profile.EXACT_REASON
                ),
                () -> true,
                () -> true,
                () -> true,
                ignored -> true,
                profileProbe,
                retentionProbe,
                resourceProbe
        );
        return new LoadFixture(
                resourcePack,
                extension,
                core,
                drive,
                extended,
                glass,
                crafting,
                quantum,
                completion
        );
    }

    private static void assertAcceptedRoutesAndCompletionActive(LoadFixture fixture) {
        assertTrue(fixture.core().isActive(), fixture.core().reason());
        assertTrue(fixture.drive().isActive(), fixture.drive().reason());
        assertTrue(fixture.extended().isActive(), fixture.extended().reason());
        assertTrue(fixture.glass().isActive(), fixture.glass().reason());
        assertTrue(fixture.crafting().isActive(), fixture.crafting().reason());
        assertTrue(fixture.quantum().isActive(), fixture.quantum().reason());
        assertTrue(fixture.completion().isActive(), fixture.completion().reason());
        ROUTES.forEach((physical, synthetic) ->
                assertEquals(synthetic, fixture.extension().getBlockStateKey(physical)));
    }

    private static void putSyntheticStates(ResourcePack resourcePack) {
        Map.of(
                "bluemap_ae2:fluix_glass_cable", "bluemap_ae2:fluix_glass_cable",
                "bluemap_ae2:drive", "bluemap_ae2:drive",
                "bluemap_ae2:extendedae_ex_drive", "bluemap_ae2:extendedae_ex_drive",
                "bluemap_ae2:quartz_glass", "bluemap_ae2:quartz_glass",
                "bluemap_ae2:crafting", "bluemap_ae2:crafting",
                "bluemap_ae2:quantum_bridge", "bluemap_ae2:quantum_bridge"
        ).forEach((state, renderer) -> resourcePack.getBlockStates().put(
                Key.parse(state),
                synthetic(renderer)
        ));
        ROUTES.values().stream().distinct().forEach(state ->
                resourcePack.getBlockStates().put(state, completionSynthetic()));
    }

    private static de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.BlockState
            completionSynthetic() {
        return synthetic("bluemap_ae2:m3_completion");
    }

    private static de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.BlockState
            synthetic(String renderer) {
        return parse("""
                {"variants":{"":{"renderer":"%s",
                                   "model":"bluemap:block/missing"}}}
                """.formatted(renderer));
    }

    private static de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.BlockState
            parse(String json) {
        return ResourcesGson.INSTANCE.fromJson(
                json,
                de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.BlockState.class
        );
    }

    private static Map.Entry<Key, Key> route(String physical, String synthetic) {
        return Map.entry(Key.parse(physical), Key.parse(synthetic));
    }

    private record LoadFixture(
            ResourcePack resourcePack,
            Ae2ResourceExtension extension,
            ProfileActivation core,
            DriveRouteActivation drive,
            ExtendedAeDriveRouteActivation extended,
            QuartzGlassRouteActivation glass,
            CraftingRouteActivation crafting,
            QuantumBridgeRouteActivation quantum,
            M3CompletionRouteActivation completion
    ) {
    }
}
