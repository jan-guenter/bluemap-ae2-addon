/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.adapter.bluemap523;

import de.bluecolored.bluemap.core.resources.pack.PackVersion;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.world.BlockProperties;
import de.bluecolored.bluemap.core.world.BlockState;
import io.github.janguenter.bluemap.ae2.activation.ExtensionRouteActivation;
import io.github.janguenter.bluemap.ae2.profile.ProfileDisablement;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class M45ResourceExtensionTest {

    @Test
    void operatorDisablementIsRouteLocalAndPrecedesArtifactReads() throws Exception {
        String property = ProfileDisablement.SYSTEM_PROPERTY;
        String previous = System.getProperty(property);
        try {
            System.setProperty(property, M45Runtime.APPFLUX);
            M45Runtime runtime = new M45Runtime();
            M45ResourceExtension extension = extension(runtime, true);

            extension.loadResources(List.of());

            assertEquals(
                    ExtensionRouteActivation.Reason.OPERATOR_DISABLED,
                    runtime.route(M45Runtime.APPFLUX).snapshot().reason()
            );
            assertEquals(
                    ExtensionRouteActivation.Reason.ARTIFACT_NOT_FOUND,
                    runtime.route(M45Runtime.ME_REQUESTER).snapshot().reason()
            );
            assertFalse(runtime.route(M45Runtime.ME_REQUESTER).isDisabled());
        } finally {
            if (previous == null) {
                System.clearProperty(property);
            } else {
                System.setProperty(property, previous);
            }
        }
    }

    @Test
    void canonicalExtendedProfileSwitchDisablesBothIndependentSlices() throws Exception {
        String property = ProfileDisablement.SYSTEM_PROPERTY;
        String previous = System.getProperty(property);
        try {
            System.setProperty(property, "extendedae");
            M45Runtime runtime = new M45Runtime();

            extension(runtime, true).loadResources(List.of());

            assertEquals(
                    ExtensionRouteActivation.Reason.OPERATOR_DISABLED,
                    runtime.route(M45Runtime.EXTENDED_MATRIX).snapshot().reason()
            );
            assertEquals(
                    ExtensionRouteActivation.Reason.OPERATOR_DISABLED,
                    runtime.route(M45Runtime.EXTENDED_PLANES).snapshot().reason()
            );
            assertEquals(
                    ExtensionRouteActivation.Reason.ARTIFACT_NOT_FOUND,
                    runtime.route(M45Runtime.MEGA_CELLS).snapshot().reason()
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
    void canonicalAppMekSwitchDisablesTheSingleDriveCellRoute() throws Exception {
        String property = ProfileDisablement.SYSTEM_PROPERTY;
        String previous = System.getProperty(property);
        try {
            System.setProperty(property, "appmek");
            M45Runtime runtime = new M45Runtime();

            extension(runtime, true).loadResources(List.of());

            assertEquals(
                    ExtensionRouteActivation.Reason.OPERATOR_DISABLED,
                    runtime.route(M45Runtime.APPMEK_DRIVE_CELLS).snapshot().reason()
            );
            assertEquals(
                    ExtensionRouteActivation.Reason.ARTIFACT_NOT_FOUND,
                    runtime.route(M45Runtime.APPFLUX).snapshot().reason()
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
    void appMekDriveRouteRequiresBothExactArtifactsAndExactResolvedResources()
            throws Exception {
        Path appMek = requiredPath("bluemapAe2.testAppMekJar");
        Path mekanism = requiredPath("bluemapAe2.testMekanismJar");

        M45Runtime exactRuntime = new M45Runtime();
        M45ResourceExtension exact = appMekExtension(
                AppMekExternalResourceTestSupport.exactResources(),
                exactRuntime,
                true,
                false
        );
        exact.loadResources(List.of(appMek, mekanism));
        exact.bake();
        assertTrue(exactRuntime.active(M45Runtime.APPMEK_DRIVE_CELLS));

        for (List<Path> missingOne : List.of(List.of(appMek), List.of(mekanism))) {
            M45Runtime runtime = new M45Runtime();
            M45ResourceExtension extension = appMekExtension(
                    AppMekExternalResourceTestSupport.exactResources(),
                    runtime,
                    true,
                    false
            );
            extension.loadResources(missingOne);
            assertFalse(runtime.active(M45Runtime.APPMEK_DRIVE_CELLS));
            assertFalse(runtime.route(M45Runtime.APPMEK_DRIVE_CELLS).isDisabled());
            assertEquals(
                    ExtensionRouteActivation.Reason.ARTIFACT_NOT_FOUND,
                    runtime.route(M45Runtime.APPMEK_DRIVE_CELLS).snapshot().reason()
            );
            assertFalse(runtime.route(M45Runtime.APPFLUX).isDisabled());
        }
    }

    @Test
    void finalCoreGateRunsAtBakeWithoutSuppressingTextureCollection() {
        M45Runtime blocked = new M45Runtime();
        blocked.route(M45Runtime.APPFLUX).activate("exact-profile");
        M45ResourceExtension extension = extension(blocked, false);

        assertFalse(extension.collectUsedTextureKeys().isEmpty());
        extension.bake();

        assertEquals(
                ExtensionRouteActivation.Reason.BLOCKED_BY_CORE,
                blocked.route(M45Runtime.APPFLUX).snapshot().reason()
        );
        assertFalse(blocked.route(M45Runtime.APPFLUX).isDisabled());

        M45Runtime activeCore = new M45Runtime();
        activeCore.route(M45Runtime.APPFLUX).activate("exact-profile");
        extension(activeCore, true).bake();
        assertEquals(
                ExtensionRouteActivation.Reason.REQUIRED_RESOURCES_MISMATCH,
                activeCore.route(M45Runtime.APPFLUX).snapshot().reason()
        );
    }

    @Test
    void disabledRouteCannotBeReactivatedByLaterResourceCallbacks() {
        M45Runtime runtime = new M45Runtime();
        runtime.route(M45Runtime.MEGA_CELLS).disable(
                ExtensionRouteActivation.Reason.REGISTRY_COLLISION,
                "mega-monitor-registry-collision"
        );
        runtime.route(M45Runtime.MEGA_CELLS).activate("exact-profile");

        assertTrue(runtime.route(M45Runtime.MEGA_CELLS).isDisabled());
        assertEquals(
                ExtensionRouteActivation.Reason.REGISTRY_COLLISION,
                runtime.route(M45Runtime.MEGA_CELLS).snapshot().reason()
        );
    }

    @Test
    void customBlockPropertiesPreserveTransparentAndFullSolidBoundaries() {
        M45Runtime runtime = new M45Runtime();
        runtime.route(M45Runtime.ADVANCED_QUANTUM).activate("exact-profile");
        runtime.route(M45Runtime.ADVANCED_ATHENA).activate("exact-profile");
        runtime.route(M45Runtime.EXTENDED_MATRIX).activate("exact-profile");
        runtime.route(M45Runtime.MEGA_CELLS).activate("exact-profile");
        M45ResourceExtension extension = extension(runtime, true);

        assertProperties(extension, "advanced_ae:quantum_core", false, false);
        assertProperties(extension, "advanced_ae:quantum_structure", false, false);
        assertProperties(extension, "extendedae:assembler_matrix_glass", false, false);
        assertProperties(extension, "advanced_ae:quantum_alloy_block", true, true);
        assertProperties(extension, "megacells:mega_crafting_unit", true, true);
    }

    @Test
    void bothBakeOrdersConvergeOnBlockedByCore() {
        M45Runtime m45First = new M45Runtime();
        m45First.route(M45Runtime.EXPANDED_AE).activate("exact-profile");
        M45Adapter.blockRoutesIfCoreInactive(m45First, true);
        assertTrue(m45First.active(M45Runtime.EXPANDED_AE));
        M45Adapter.blockRoutesIfCoreInactive(m45First, false);
        assertBlockedByCore(m45First, M45Runtime.EXPANDED_AE);

        M45Runtime coreFirst = new M45Runtime();
        coreFirst.route(M45Runtime.MEGA_CELLS).activate("exact-profile");
        M45Adapter.blockRoutesIfCoreInactive(coreFirst, false);
        assertBlockedByCore(coreFirst, M45Runtime.MEGA_CELLS);
        M45Adapter.blockRoutesIfCoreInactive(coreFirst, true);
        assertBlockedByCore(coreFirst, M45Runtime.MEGA_CELLS);
    }

    @Test
    void faceOnlyExtendedPlanesAlsoRequireNativeStructuralCapability() {
        M45Runtime lateNativeFailure = new M45Runtime();
        lateNativeFailure.route(M45Runtime.EXTENDED_PLANES).activate("exact-profile");
        M45Adapter.blockExtendedPlanesIfNativeStructuralInactive(
                lateNativeFailure,
                true
        );
        assertTrue(lateNativeFailure.active(M45Runtime.EXTENDED_PLANES));
        M45Adapter.blockExtendedPlanesIfNativeStructuralInactive(
                lateNativeFailure,
                false
        );
        assertBlockedByCore(lateNativeFailure, M45Runtime.EXTENDED_PLANES);
        assertEquals(
                "native-structural-core-inactive",
                lateNativeFailure.route(M45Runtime.EXTENDED_PLANES)
                        .snapshot().detail()
        );

        M45Runtime mixed = new M45Runtime();
        mixed.route(M45Runtime.APPFLUX).activate("exact-profile");
        mixed.route(M45Runtime.EXTENDED_PLANES).activate("exact-profile");
        M45Adapter.blockExtendedPlanesIfNativeStructuralInactive(mixed, false);
        assertTrue(mixed.active(M45Runtime.APPFLUX));
        assertBlockedByCore(mixed, M45Runtime.EXTENDED_PLANES);
    }

    @Test
    void appMekDriveDependencyConvergesAcrossResourceExtensionBakeOrders()
            throws Exception {
        M45Runtime pendingFirst = new M45Runtime();
        pendingFirst.route(M45Runtime.APPMEK_DRIVE_CELLS).activate("exact-profile");
        M45ResourceExtension pending = appMekExtension(
                AppMekExternalResourceTestSupport.exactResources(),
                pendingFirst,
                false,
                true
        );
        pending.bake();
        assertTrue(pendingFirst.active(M45Runtime.APPMEK_DRIVE_CELLS));
        M45Adapter.blockAppMekDriveCellsIfNativeDriveInactive(pendingFirst, false);
        assertBlockedByCore(pendingFirst, M45Runtime.APPMEK_DRIVE_CELLS);

        M45Runtime driveFirst = new M45Runtime();
        driveFirst.route(M45Runtime.APPMEK_DRIVE_CELLS).activate("exact-profile");
        appMekExtension(
                AppMekExternalResourceTestSupport.exactResources(),
                driveFirst,
                false,
                false
        ).bake();
        assertBlockedByCore(driveFirst, M45Runtime.APPMEK_DRIVE_CELLS);
        assertEquals(
                "native-drive-core-inactive",
                driveFirst.route(M45Runtime.APPMEK_DRIVE_CELLS).snapshot().detail()
        );
    }

    private static M45ResourceExtension extension(M45Runtime runtime, boolean coreActive) {
        return new M45ResourceExtension(
                new ResourcePack(new PackVersion(34, 0)),
                runtime,
                () -> coreActive
        );
    }

    private static M45ResourceExtension appMekExtension(
            ResourcePack resources,
            M45Runtime runtime,
            boolean nativeDriveActive,
            boolean nativeDrivePending
    ) {
        return new M45ResourceExtension(
                resources,
                runtime,
                () -> true,
                () -> true,
                () -> nativeDriveActive,
                () -> nativeDrivePending
        );
    }

    private static Path requiredPath(String property) {
        String value = System.getProperty(property, "");
        if (value.isBlank()) {
            throw new IllegalStateException("missing test property " + property);
        }
        return Path.of(value);
    }

    private static void assertProperties(
            M45ResourceExtension extension,
            String blockId,
            boolean culling,
            boolean occluding
    ) {
        BlockProperties.Builder builder = BlockProperties.builder()
                .culling(!culling)
                .occluding(!occluding)
                .cullingIdentical(true);
        extension.getBlockProperties(BlockState.fromString(blockId), builder);
        BlockProperties properties = builder.build();
        assertEquals(culling, properties.isCulling());
        assertEquals(occluding, properties.isOccluding());
        assertFalse(properties.getCullingIdentical());
    }

    private static void assertBlockedByCore(M45Runtime runtime, String routeId) {
        assertFalse(runtime.active(routeId));
        assertEquals(
                ExtensionRouteActivation.Reason.BLOCKED_BY_CORE,
                runtime.route(routeId).snapshot().reason()
        );
    }
}
