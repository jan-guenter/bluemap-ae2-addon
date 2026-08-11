/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.adapter.bluemap522;

import de.bluecolored.bluemap.core.map.hires.block.BlockRendererType;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.Variant;
import de.bluecolored.bluemap.core.util.Key;
import de.bluecolored.bluemap.core.world.BlockEntity;
import de.bluecolored.bluemap.core.world.mca.MCAUtil;
import de.bluecolored.bluemap.core.world.mca.blockentity.BlockEntityType;
import de.bluecolored.bluenbt.NBTWriter;
import io.github.janguenter.bluemap.ae2.activation.ExtensionRouteActivation;
import io.github.janguenter.bluemap.ae2.profile.megacells.MegaCells4110Profile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/** Registration boundary for the optional ATM 1.2.0 M4/M5 routes. */
final class M45Adapter {

    private static final Key RENDERER_KEY = Key.parse("bluemap_ae2:m45");
    private static final Key EXTENSION_KEY = Key.parse("bluemap_ae2:m45-atm-1.2.0");
    private static final Key MEGA_MONITOR_BE = Key.parse(
            MegaCells4110Profile.CRAFTING_MONITOR_BLOCK_ENTITY
    );
    private static final M45Runtime RUNTIME = new M45Runtime();
    private static final BlockRendererType RENDERER = new BlockRendererType.Impl(
            RENDERER_KEY,
            (resourcePack, textureGallery, renderSettings) -> new M45BlockRenderer(
                    resourcePack,
                    textureGallery,
                    renderSettings,
                    RUNTIME
            )
    );
    private static final ResourcePack.Extension<M45ResourceExtension> EXTENSION =
            new M45ResourceExtensionType(EXTENSION_KEY, RUNTIME);
    private static final BlockEntityType MEGA_MONITOR_TYPE = new BlockEntityType.Impl(
            MEGA_MONITOR_BE,
            Ae2CraftingMonitorBlockEntityData.class
    );

    private static boolean installed;

    private M45Adapter() {
    }

    static synchronized boolean install() {
        if (installed) {
            return ownsSharedKeys();
        }
        if (!BlueMap522Adapter.canRegisterExact(BlockRendererType.REGISTRY, RENDERER)
                || !BlueMap522Adapter.canRegisterExact(
                        ResourcePack.Extension.REGISTRY,
                        EXTENSION
                )) {
            disableAllForCollision();
            return false;
        }

        // DTO aliases precede the first BlueNBT probe in either resource extension.
        if (!BlueMap522Adapter.canRegisterExact(
                BlockEntityType.REGISTRY,
                MEGA_MONITOR_TYPE
        ) || !BlueMap522Adapter.registerExact(
                BlockEntityType.REGISTRY,
                MEGA_MONITOR_TYPE
        )) {
            RUNTIME.route(M45Runtime.MEGA_CELLS).disable(
                    ExtensionRouteActivation.Reason.REGISTRY_COLLISION,
                    "mega-monitor-registry-collision"
            );
        }
        if (!BlueMap522Adapter.registerExact(BlockRendererType.REGISTRY, RENDERER)
                || !BlueMap522Adapter.registerExact(ResourcePack.Extension.REGISTRY, EXTENSION)) {
            disableAllForCollision();
            return false;
        }
        installed = true;
        RUNTIME.routes().forEach(route -> route.inactive(
                ExtensionRouteActivation.Reason.AWAITING_EXACT_PROFILE,
                "awaiting-exact-profile"
        ));
        return true;
    }

    static M45Runtime runtime() {
        return RUNTIME;
    }

    static void reconcileCoreAfterBake(
            boolean coreActive,
            boolean nativeStructuralActive
    ) {
        blockRoutesIfCoreInactive(RUNTIME, coreActive);
        blockExtendedPlanesIfNativeStructuralInactive(
                RUNTIME,
                nativeStructuralActive
        );
    }

    static void blockRoutesIfCoreInactive(M45Runtime runtime, boolean coreActive) {
        runtime.blockActiveRoutesIfCoreInactive(coreActive);
    }

    static void blockExtendedPlanesIfNativeStructuralInactive(
            M45Runtime runtime,
            boolean nativeStructuralActive
    ) {
        runtime.blockExtendedPlanesIfNativeStructuralInactive(nativeStructuralActive);
    }

    static boolean isExpectedSyntheticVariant(Variant variant) {
        return variant != null
                && variant.getRenderer() == RENDERER
                && ResourcePack.MISSING_BLOCK_MODEL.equals(variant.getModel())
                && !variant.isTransformed()
                && !variant.isUvlock()
                && Double.compare(variant.getWeight(), 1D) == 0;
    }

    static boolean probeMegaMonitorRetention() {
        try {
            BlockEntity parsed = MCAUtil.BLUENBT.read(
                    new ByteArrayInputStream(megaMonitorProbe()),
                    BlockEntity.class
            );
            return parsed instanceof Ae2CraftingMonitorBlockEntityData monitor
                    && MEGA_MONITOR_BE.equals(monitor.getId())
                    && monitor.getX() == -71
                    && monitor.getY() == 89
                    && monitor.getZ() == 37
                    && monitor.retainsProbeFields();
        } catch (IOException | RuntimeException | LinkageError exception) {
            return false;
        }
    }

    private static byte[] megaMonitorProbe() throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (NBTWriter writer = new NBTWriter(bytes)) {
            writer.beginCompound();
            writer.name("id").value(MegaCells4110Profile.CRAFTING_MONITOR_BLOCK_ENTITY);
            writer.name("x").value(-71);
            writer.name("y").value(89);
            writer.name("z").value(37);
            writer.name("paintedColor").value((byte) 11);
            writer.endCompound();
        }
        return bytes.toByteArray();
    }

    private static boolean ownsSharedKeys() {
        return BlockRendererType.REGISTRY.get(RENDERER.getKey()) == RENDERER
                && ResourcePack.Extension.REGISTRY.get(EXTENSION.getKey()) == EXTENSION;
    }

    private static void disableAllForCollision() {
        RUNTIME.routes().forEach(route -> route.disable(
                ExtensionRouteActivation.Reason.REGISTRY_COLLISION,
                "registry-collision"
        ));
    }
}
