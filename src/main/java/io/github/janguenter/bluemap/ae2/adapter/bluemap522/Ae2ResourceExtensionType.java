/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.adapter.bluemap522;

import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.util.Key;
import io.github.janguenter.bluemap.ae2.api.Ae2ExtensionRegistry;
import io.github.janguenter.bluemap.ae2.activation.CraftingRouteActivation;
import io.github.janguenter.bluemap.ae2.activation.DriveRouteActivation;
import io.github.janguenter.bluemap.ae2.activation.ExtendedAeDriveRouteActivation;
import io.github.janguenter.bluemap.ae2.activation.M3CompletionRouteActivation;
import io.github.janguenter.bluemap.ae2.activation.NativeStructuralRouteActivation;
import io.github.janguenter.bluemap.ae2.activation.ProfileActivation;
import io.github.janguenter.bluemap.ae2.activation.QuantumBridgeRouteActivation;
import io.github.janguenter.bluemap.ae2.activation.QuartzGlassRouteActivation;

/** Factory registered before BlueMap constructs any resource pack. */
final class Ae2ResourceExtensionType implements ResourcePack.Extension<Ae2ResourceExtension> {

    private final Key key;
    private final ProfileActivation activation;
    private final DriveRouteActivation driveActivation;
    private final ExtendedAeDriveRouteActivation extendedDriveActivation;
    private final QuartzGlassRouteActivation quartzGlassActivation;
    private final CraftingRouteActivation craftingActivation;
    private final QuantumBridgeRouteActivation quantumBridgeActivation;
    private final M3CompletionRouteActivation m3CompletionActivation;
    private final NativeStructuralRouteActivation nativeStructuralActivation;

    Ae2ResourceExtensionType(Key key, ProfileActivation activation) {
        this(
                key,
                activation,
                new DriveRouteActivation(),
                new ExtendedAeDriveRouteActivation(),
                new QuartzGlassRouteActivation(),
                new CraftingRouteActivation(),
                new QuantumBridgeRouteActivation()
        );
    }

    Ae2ResourceExtensionType(
            Key key,
            ProfileActivation activation,
            DriveRouteActivation driveActivation
    ) {
        this(
                key,
                activation,
                driveActivation,
                new ExtendedAeDriveRouteActivation(),
                new QuartzGlassRouteActivation(),
                new CraftingRouteActivation(),
                new QuantumBridgeRouteActivation()
        );
    }

    Ae2ResourceExtensionType(
            Key key,
            ProfileActivation activation,
            DriveRouteActivation driveActivation,
            ExtendedAeDriveRouteActivation extendedDriveActivation
    ) {
        this(
                key,
                activation,
                driveActivation,
                extendedDriveActivation,
                new QuartzGlassRouteActivation(),
                new CraftingRouteActivation(),
                new QuantumBridgeRouteActivation()
        );
    }

    Ae2ResourceExtensionType(
            Key key,
            ProfileActivation activation,
            DriveRouteActivation driveActivation,
            ExtendedAeDriveRouteActivation extendedDriveActivation,
            QuartzGlassRouteActivation quartzGlassActivation
    ) {
        this(
                key,
                activation,
                driveActivation,
                extendedDriveActivation,
                quartzGlassActivation,
                new CraftingRouteActivation(),
                new QuantumBridgeRouteActivation()
        );
    }

    Ae2ResourceExtensionType(
            Key key,
            ProfileActivation activation,
            DriveRouteActivation driveActivation,
            ExtendedAeDriveRouteActivation extendedDriveActivation,
            QuartzGlassRouteActivation quartzGlassActivation,
            CraftingRouteActivation craftingActivation
    ) {
        this(
                key,
                activation,
                driveActivation,
                extendedDriveActivation,
                quartzGlassActivation,
                craftingActivation,
                new QuantumBridgeRouteActivation()
        );
    }

    Ae2ResourceExtensionType(
            Key key,
            ProfileActivation activation,
            DriveRouteActivation driveActivation,
            ExtendedAeDriveRouteActivation extendedDriveActivation,
            QuartzGlassRouteActivation quartzGlassActivation,
            CraftingRouteActivation craftingActivation,
            QuantumBridgeRouteActivation quantumBridgeActivation
    ) {
        this(
                key,
                activation,
                driveActivation,
                extendedDriveActivation,
                quartzGlassActivation,
                craftingActivation,
                quantumBridgeActivation,
                new M3CompletionRouteActivation()
        );
    }

    Ae2ResourceExtensionType(
            Key key,
            ProfileActivation activation,
            DriveRouteActivation driveActivation,
            ExtendedAeDriveRouteActivation extendedDriveActivation,
            QuartzGlassRouteActivation quartzGlassActivation,
            CraftingRouteActivation craftingActivation,
            QuantumBridgeRouteActivation quantumBridgeActivation,
            M3CompletionRouteActivation m3CompletionActivation
    ) {
        this(
                key,
                activation,
                driveActivation,
                extendedDriveActivation,
                quartzGlassActivation,
                craftingActivation,
                quantumBridgeActivation,
                m3CompletionActivation,
                new NativeStructuralRouteActivation()
        );
    }

    Ae2ResourceExtensionType(
            Key key,
            ProfileActivation activation,
            DriveRouteActivation driveActivation,
            ExtendedAeDriveRouteActivation extendedDriveActivation,
            QuartzGlassRouteActivation quartzGlassActivation,
            CraftingRouteActivation craftingActivation,
            QuantumBridgeRouteActivation quantumBridgeActivation,
            M3CompletionRouteActivation m3CompletionActivation,
            NativeStructuralRouteActivation nativeStructuralActivation
    ) {
        this.key = key;
        this.activation = activation;
        this.driveActivation = driveActivation;
        this.extendedDriveActivation = extendedDriveActivation;
        this.quartzGlassActivation = quartzGlassActivation;
        this.craftingActivation = craftingActivation;
        this.quantumBridgeActivation = quantumBridgeActivation;
        this.m3CompletionActivation = m3CompletionActivation;
        this.nativeStructuralActivation = nativeStructuralActivation;
    }

    @Override
    public Key getKey() {
        return key;
    }

    @Override
    public Ae2ResourceExtension create(ResourcePack pack) {
        Ae2ExtensionRegistry.Host.freezeForResourceRendering(
                Ae2ExtensionRegistry.Host.acquireAccess()
        );
        return new Ae2ResourceExtension(
                pack,
                activation,
                driveActivation,
                extendedDriveActivation,
                quartzGlassActivation,
                craftingActivation,
                quantumBridgeActivation,
                m3CompletionActivation,
                nativeStructuralActivation
        );
    }
}
