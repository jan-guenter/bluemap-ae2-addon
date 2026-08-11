/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.diagnostics;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.logging.Logger;

/** Fixed-message, location-free diagnostics with a strict per-event emission cap. */
public final class BoundedDiagnostics {

    private static final Logger LOGGER = Logger.getLogger("BlueMapAE2");
    private static final BoundedDiagnostics GLOBAL = new BoundedDiagnostics(
            1,
            (level, key, message) -> {
                if (level == Level.WARNING) {
                    LOGGER.warning(message);
                } else {
                    LOGGER.info(message);
                }
            }
    );

    private final int maximumEmissionsPerEvent;
    private final Sink sink;
    private final AtomicIntegerArray counts = new AtomicIntegerArray(Event.values().length);

    public BoundedDiagnostics(int maximumEmissionsPerEvent, Sink sink) {
        if (maximumEmissionsPerEvent < 1) {
            throw new IllegalArgumentException("maximumEmissionsPerEvent must be positive");
        }
        this.maximumEmissionsPerEvent = maximumEmissionsPerEvent;
        this.sink = Objects.requireNonNull(sink, "sink");
    }

    public static boolean report(Event event) {
        return GLOBAL.tryReport(event);
    }

    public boolean tryReport(Event event) {
        Objects.requireNonNull(event, "event");
        int index = event.ordinal();
        while (true) {
            int previous = counts.get(index);
            if (previous >= maximumEmissionsPerEvent) {
                return false;
            }
            if (counts.compareAndSet(index, previous, previous + 1)) {
                try {
                    sink.accept(event.level(), event.key(), event.message());
                    return true;
                } catch (RuntimeException | LinkageError ignored) {
                    // Diagnostics must never turn a contained renderer or
                    // resource callback failure into a BlueMap failure.
                    return false;
                }
            }
        }
    }

    @FunctionalInterface
    public interface Sink {
        void accept(Level level, String key, String message);
    }

    public enum Level {
        INFO,
        WARNING
    }

    public enum Event {
        PROFILE_ACTIVATED(
                Level.INFO,
                "profile-activated",
                "BlueMap AE2 exact 19.2.17 M2 cable-bus profile activated."
        ),
        DRIVE_ROUTE_ACTIVATED(
                Level.INFO,
                "drive-route-activated",
                "BlueMap AE2 exact 19.2.17 M3a drive route activated."
        ),
        EXTENDED_DRIVE_ROUTE_ACTIVATED(
                Level.INFO,
                "extended-drive-route-activated",
                "BlueMap AE2 exact compatible ExtendedAE M3b drive route activated."
        ),
        QUARTZ_GLASS_ROUTE_ACTIVATED(
                Level.INFO,
                "quartz-glass-route-activated",
                "BlueMap AE2 exact 19.2.17 M3c quartz-glass route activated."
        ),
        CRAFTING_ROUTE_ACTIVATED(
                Level.INFO,
                "crafting-route-activated",
                "BlueMap AE2 exact 19.2.17 M3d formed-crafting route activated."
        ),
        QUANTUM_BRIDGE_ROUTE_ACTIVATED(
                Level.INFO,
                "quantum-bridge-route-activated",
                "BlueMap AE2 exact 19.2.17 M3e quantum-bridge route activated."
        ),
        M3_COMPLETION_ROUTE_ACTIVATED(
                Level.INFO,
                "m3-completion-route-activated",
                "BlueMap AE2 exact 19.2.17 M3 completion route activated."
        ),
        NATIVE_STRUCTURAL_ROUTE_ACTIVATED(
                Level.INFO,
                "native-structural-route-activated",
                "BlueMap AE2 exact 19.2.17 native cable-bus structural route activated."
        ),
        PROFILE_DISABLED(
                Level.INFO,
                "profile-disabled",
                "BlueMap AE2 profile is disabled by operator configuration."
        ),
        EXTENDED_PROFILE_DISABLED(
                Level.INFO,
                "extended-profile-disabled",
                "BlueMap AE2 ExtendedAE profile is disabled by operator configuration."
        ),
        QUARTZ_GLASS_PROFILE_DISABLED(
                Level.INFO,
                "quartz-glass-profile-disabled",
                "BlueMap AE2 quartz-glass profile is disabled by operator configuration."
        ),
        CRAFTING_PROFILE_DISABLED(
                Level.INFO,
                "crafting-profile-disabled",
                "BlueMap AE2 formed-crafting profile is disabled by operator configuration."
        ),
        QUANTUM_BRIDGE_PROFILE_DISABLED(
                Level.INFO,
                "quantum-bridge-profile-disabled",
                "BlueMap AE2 quantum-bridge profile is disabled by operator configuration."
        ),
        M3_COMPLETION_PROFILE_DISABLED(
                Level.INFO,
                "m3-completion-profile-disabled",
                "BlueMap AE2 M3 completion profile is disabled by operator configuration."
        ),
        NATIVE_STRUCTURAL_PROFILE_DISABLED(
                Level.INFO,
                "native-structural-profile-disabled",
                "BlueMap AE2 native cable-bus structural profile is disabled by operator configuration."
        ),
        ARTIFACT_NOT_FOUND(
                Level.WARNING,
                "ae2-artifact-not-found",
                "BlueMap AE2 add-on is inactive: the pinned AE2 artifact was not found."
        ),
        MULTIPLE_ARTIFACTS(
                Level.WARNING,
                "multiple-ae2-artifacts",
                "BlueMap AE2 add-on is inactive: multiple AE2 artifacts were found."
        ),
        UNSUPPORTED_ARTIFACT(
                Level.WARNING,
                "unsupported-ae2-artifact",
                "BlueMap AE2 add-on is inactive: the AE2 artifact identity is unsupported."
        ),
        ARTIFACT_READ_FAILED(
                Level.WARNING,
                "ae2-artifact-read-failed",
                "BlueMap AE2 add-on is inactive: the AE2 artifact identity could not be read."
        ),
        EXTENDED_ARTIFACT_NOT_FOUND(
                Level.WARNING,
                "extendedae-artifact-not-found",
                "BlueMap AE2 Extended Drive route is inactive: the pinned ExtendedAE artifact "
                        + "was not found."
        ),
        MULTIPLE_EXTENDED_ARTIFACTS(
                Level.WARNING,
                "multiple-extendedae-artifacts",
                "BlueMap AE2 Extended Drive route is inactive: multiple ExtendedAE artifacts "
                        + "were found."
        ),
        UNSUPPORTED_EXTENDED_ARTIFACT(
                Level.WARNING,
                "unsupported-extendedae-artifact",
                "BlueMap AE2 Extended Drive route is inactive: the ExtendedAE artifact identity "
                        + "is unsupported."
        ),
        EXTENDED_ARTIFACT_READ_FAILED(
                Level.WARNING,
                "extendedae-artifact-read-failed",
                "BlueMap AE2 Extended Drive route is inactive: the ExtendedAE artifact identity "
                        + "could not be read."
        ),
        REQUIRED_RESOURCES_MISMATCH(
                Level.WARNING,
                "required-resources-mismatch",
                "BlueMap AE2 add-on is inactive: required AE2 resources did not match."
        ),
        REGISTRY_COLLISION(
                Level.WARNING,
                "registry-collision",
                "BlueMap AE2 add-on is inactive: a required BlueMap registry key was occupied."
        ),
        DRIVE_REGISTRY_COLLISION(
                Level.WARNING,
                "drive-registry-collision",
                "BlueMap AE2 drive route is inactive: a registry key was occupied."
        ),
        EXTENDED_DRIVE_REGISTRY_COLLISION(
                Level.WARNING,
                "extended-drive-registry-collision",
                "BlueMap AE2 Extended Drive route is inactive: a registry key was occupied."
        ),
        QUARTZ_GLASS_REGISTRY_COLLISION(
                Level.WARNING,
                "quartz-glass-registry-collision",
                "BlueMap AE2 quartz-glass route is inactive: a registry key was occupied."
        ),
        CRAFTING_REGISTRY_COLLISION(
                Level.WARNING,
                "crafting-registry-collision",
                "BlueMap AE2 formed-crafting route is inactive: a registry key was occupied."
        ),
        QUANTUM_BRIDGE_REGISTRY_COLLISION(
                Level.WARNING,
                "quantum-bridge-registry-collision",
                "BlueMap AE2 quantum-bridge route is inactive: a registry key was occupied."
        ),
        M3_COMPLETION_REGISTRY_COLLISION(
                Level.WARNING,
                "m3-completion-registry-collision",
                "BlueMap AE2 M3 completion route is inactive: a registry key was occupied."
        ),
        RETENTION_PROBE_FAILED(
                Level.WARNING,
                "bluenbt-retention-probe-failed",
                "BlueMap AE2 add-on is inactive: required block data was not retained."
        ),
        DRIVE_RETENTION_PROBE_FAILED(
                Level.WARNING,
                "drive-retention-probe-failed",
                "BlueMap AE2 drive route is inactive: drive inventory was not retained."
        ),
        EXTENDED_DRIVE_RETENTION_PROBE_FAILED(
                Level.WARNING,
                "extended-drive-retention-probe-failed",
                "BlueMap AE2 Extended Drive route is inactive: its 20-slot inventory was not "
                        + "retained."
        ),
        CRAFTING_RETENTION_PROBE_FAILED(
                Level.WARNING,
                "crafting-retention-probe-failed",
                "BlueMap AE2 formed-crafting route is inactive: monitor color was not retained."
        ),
        M3_COMPLETION_RETENTION_PROBE_FAILED(
                Level.WARNING,
                "m3-completion-retention-probe-failed",
                "BlueMap AE2 M3 completion route is inactive: paint dots were not retained."
        ),
        RESOURCE_CALLBACK_FAILED(
                Level.WARNING,
                "resource-callback-failed",
                "BlueMap AE2 add-on is inactive: a resource callback failed safely."
        ),
        DRIVE_RESOURCE_CALLBACK_FAILED(
                Level.WARNING,
                "drive-resource-callback-failed",
                "BlueMap AE2 drive route is inactive: a resource callback failed safely."
        ),
        EXTENDED_DRIVE_RESOURCE_CALLBACK_FAILED(
                Level.WARNING,
                "extended-drive-resource-callback-failed",
                "BlueMap AE2 Extended Drive route is inactive: a resource callback failed "
                        + "safely."
        ),
        QUARTZ_GLASS_RESOURCE_CALLBACK_FAILED(
                Level.WARNING,
                "quartz-glass-resource-callback-failed",
                "BlueMap AE2 quartz-glass route is inactive: a resource callback failed safely."
        ),
        CRAFTING_RESOURCE_CALLBACK_FAILED(
                Level.WARNING,
                "crafting-resource-callback-failed",
                "BlueMap AE2 formed-crafting route is inactive: a resource callback failed "
                        + "safely."
        ),
        QUANTUM_BRIDGE_RESOURCE_CALLBACK_FAILED(
                Level.WARNING,
                "quantum-bridge-resource-callback-failed",
                "BlueMap AE2 quantum-bridge route is inactive: a resource callback failed "
                        + "safely."
        ),
        M3_COMPLETION_RESOURCE_CALLBACK_FAILED(
                Level.WARNING,
                "m3-completion-resource-callback-failed",
                "BlueMap AE2 M3 completion route is inactive: a resource callback failed "
                        + "safely."
        ),
        NATIVE_STRUCTURAL_RESOURCE_CALLBACK_FAILED(
                Level.WARNING,
                "native-structural-resource-callback-failed",
                "BlueMap AE2 native cable-bus structural route resource callback failed safely."
        ),
        DRIVE_REQUIRED_RESOURCES_MISMATCH(
                Level.WARNING,
                "drive-required-resources-mismatch",
                "BlueMap AE2 drive route is inactive: required drive resources did not match."
        ),
        EXTENDED_DRIVE_REQUIRED_RESOURCES_MISMATCH(
                Level.WARNING,
                "extended-drive-required-resources-mismatch",
                "BlueMap AE2 Extended Drive route is inactive: required resources did not "
                        + "match."
        ),
        QUARTZ_GLASS_REQUIRED_RESOURCES_MISMATCH(
                Level.WARNING,
                "quartz-glass-required-resources-mismatch",
                "BlueMap AE2 quartz-glass route is inactive: required resources did not match."
        ),
        CRAFTING_REQUIRED_RESOURCES_MISMATCH(
                Level.WARNING,
                "crafting-required-resources-mismatch",
                "BlueMap AE2 formed-crafting route is inactive: required resources did not "
                        + "match."
        ),
        QUANTUM_BRIDGE_REQUIRED_RESOURCES_MISMATCH(
                Level.WARNING,
                "quantum-bridge-required-resources-mismatch",
                "BlueMap AE2 quantum-bridge route is inactive: required resources did not "
                        + "match."
        ),
        M3_COMPLETION_REQUIRED_RESOURCES_MISMATCH(
                Level.WARNING,
                "m3-completion-required-resources-mismatch",
                "BlueMap AE2 M3 completion route is inactive: required resources did not match."
        ),
        NATIVE_STRUCTURAL_REQUIRED_RESOURCES_MISMATCH(
                Level.WARNING,
                "native-structural-required-resources-mismatch",
                "BlueMap AE2 native cable-bus structural route is inactive: required resources did not match."
        ),
        NATIVE_STRUCTURAL_RETENTION_PROBE_FAILED(
                Level.WARNING,
                "native-structural-retention-probe-failed",
                "BlueMap AE2 native cable-bus structural route is inactive: bounded data retention failed."
        ),
        MALFORMED_BLOCK_DATA(
                Level.WARNING,
                "malformed-block-data",
                "BlueMap AE2 used stock fallback for malformed cable-bus data."
        ),
        DRIVE_MALFORMED_BLOCK_DATA(
                Level.WARNING,
                "drive-malformed-block-data",
                "BlueMap AE2 used stock fallback for malformed drive data."
        ),
        EXTENDED_DRIVE_MALFORMED_BLOCK_DATA(
                Level.WARNING,
                "extended-drive-malformed-block-data",
                "BlueMap AE2 used stock fallback for malformed Extended Drive data."
        ),
        CRAFTING_MALFORMED_BLOCK_DATA(
                Level.WARNING,
                "crafting-malformed-block-data",
                "BlueMap AE2 used stock fallback for malformed crafting-monitor data."
        ),
        M3_COMPLETION_MALFORMED_BLOCK_DATA(
                Level.WARNING,
                "m3-completion-malformed-block-data",
                "BlueMap AE2 used stock fallback for malformed M3 completion block data."
        ),
        NATIVE_STRUCTURAL_MALFORMED_BLOCK_DATA(
                Level.WARNING,
                "native-structural-malformed-block-data",
                "BlueMap AE2 used stock fallback for malformed native cable-bus structural data."
        ),
        UNSUPPORTED_BLOCK_STATE(
                Level.WARNING,
                "unsupported-block-state",
                "BlueMap AE2 used stock fallback for a cable-bus state outside M2."
        ),
        DRIVE_UNSUPPORTED_BLOCK_STATE(
                Level.WARNING,
                "drive-unsupported-block-state",
                "BlueMap AE2 used stock fallback for a drive state outside M3a."
        ),
        EXTENDED_DRIVE_UNSUPPORTED_BLOCK_STATE(
                Level.WARNING,
                "extended-drive-unsupported-block-state",
                "BlueMap AE2 used stock fallback for an Extended Drive state outside M3b."
        ),
        QUARTZ_GLASS_UNSUPPORTED_BLOCK_STATE(
                Level.WARNING,
                "quartz-glass-unsupported-block-state",
                "BlueMap AE2 used stock fallback for a quartz-glass state outside M3c."
        ),
        CRAFTING_UNSUPPORTED_BLOCK_STATE(
                Level.WARNING,
                "crafting-unsupported-block-state",
                "BlueMap AE2 used stock fallback for a crafting state outside M3d."
        ),
        QUANTUM_BRIDGE_UNSUPPORTED_BLOCK_STATE(
                Level.WARNING,
                "quantum-bridge-unsupported-block-state",
                "BlueMap AE2 used stock fallback for a quantum-bridge state outside M3e."
        ),
        M3_COMPLETION_UNSUPPORTED_BLOCK_STATE(
                Level.WARNING,
                "m3-completion-unsupported-block-state",
                "BlueMap AE2 used stock fallback for a state outside the M3 completion route."
        ),
        NATIVE_STRUCTURAL_UNSUPPORTED_BLOCK_STATE(
                Level.WARNING,
                "native-structural-unsupported-block-state",
                "BlueMap AE2 used stock fallback for a state outside the native structural route."
        ),
        DRIVE_UNSUPPORTED_CELL(
                Level.WARNING,
                "drive-unsupported-cell",
                "BlueMap AE2 used stock fallback for an unsupported drive cell."
        ),
        EXTENDED_DRIVE_UNSUPPORTED_CELL(
                Level.WARNING,
                "extended-drive-unsupported-cell",
                "BlueMap AE2 used stock fallback for an unsupported Extended Drive cell."
        ),
        UNSUPPORTED_CENTER_PART(
                Level.WARNING,
                "unsupported-center-part",
                "BlueMap AE2 used stock fallback for an unsupported cable center."
        ),
        NATIVE_STRUCTURAL_UNSUPPORTED_CENTER_PART(
                Level.WARNING,
                "native-structural-unsupported-center-part",
                "BlueMap AE2 used stock fallback for an unsupported structural cable center."
        ),
        UNSUPPORTED_ATTACHMENTS_OR_FACADES(
                Level.WARNING,
                "unsupported-attachments-or-facades",
                "BlueMap AE2 used stock fallback for unsupported cable-bus contents."
        ),
        UNSUPPORTED_FACE_PART(
                Level.WARNING,
                "unsupported-face-part",
                "BlueMap AE2 used stock fallback for an unsupported face part or layout."
        ),
        NATIVE_STRUCTURAL_UNSUPPORTED_FACE_PART(
                Level.WARNING,
                "native-structural-unsupported-face-part",
                "BlueMap AE2 used stock fallback for an unsupported native face part or layout."
        ),
        UNSUPPORTED_FACADE_STATE(
                Level.WARNING,
                "unsupported-facade-state",
                "BlueMap AE2 used stock fallback for an unsupported facade state."
        ),
        NATIVE_STRUCTURAL_UNSUPPORTED_FACADE_STATE(
                Level.WARNING,
                "native-structural-unsupported-facade-state",
                "BlueMap AE2 used stock fallback for a non-static or unsupported facade state."
        ),
        UNSUPPORTED_FACADE_LAYOUT(
                Level.WARNING,
                "unsupported-facade-layout",
                "BlueMap AE2 used stock fallback for an unsupported facade layout."
        ),
        NATIVE_STRUCTURAL_UNSUPPORTED_FACADE_LAYOUT(
                Level.WARNING,
                "native-structural-unsupported-facade-layout",
                "BlueMap AE2 used stock fallback for an invalid structural facade layout."
        ),
        UNSUPPORTED_NEIGHBOR_DATA(
                Level.WARNING,
                "unsupported-neighbor-data",
                "BlueMap AE2 used stock fallback because neighbor cable data was unavailable."
        ),
        NATIVE_STRUCTURAL_UNSUPPORTED_NEIGHBOR_DATA(
                Level.WARNING,
                "native-structural-unsupported-neighbor-data",
                "BlueMap AE2 used stock fallback because structural topology data was unavailable."
        ),
        QUARTZ_GLASS_UNSUPPORTED_NEIGHBOR_DATA(
                Level.WARNING,
                "quartz-glass-unsupported-neighbor-data",
                "BlueMap AE2 used stock fallback because quartz-glass neighbor data was "
                        + "unavailable."
        ),
        CRAFTING_UNSUPPORTED_NEIGHBOR_DATA(
                Level.WARNING,
                "crafting-unsupported-neighbor-data",
                "BlueMap AE2 used stock fallback because crafting neighbor data was unavailable."
        ),
        QUANTUM_BRIDGE_UNSUPPORTED_NEIGHBOR_DATA(
                Level.WARNING,
                "quantum-bridge-unsupported-neighbor-data",
                "BlueMap AE2 used stock fallback because quantum-bridge neighbor data was "
                        + "unavailable."
        ),
        M3_COMPLETION_UNSUPPORTED_NEIGHBOR_DATA(
                Level.WARNING,
                "m3-completion-unsupported-neighbor-data",
                "BlueMap AE2 used stock fallback because required M3 completion context was "
                        + "missing, malformed, or outside a bounded pylon scan."
        ),
        QUANTUM_BRIDGE_INVALID_TOPOLOGY(
                Level.WARNING,
                "quantum-bridge-invalid-topology",
                "BlueMap AE2 used stock fallback for an invalid or ambiguous quantum bridge."
        ),
        M3_COMPLETION_INVALID_TOPOLOGY(
                Level.WARNING,
                "m3-completion-invalid-topology",
                "BlueMap AE2 used a conservative unformed appearance for a bounded, locally "
                        + "invalid spatial-pylon component."
        ),
        CRAFTING_UNSUPPORTED_COMPATIBLE_NEIGHBOR(
                Level.WARNING,
                "crafting-unsupported-compatible-neighbor",
                "BlueMap AE2 used stock fallback beside a compatible extension crafting block."
        ),
        TEXTURE_MISSING(
                Level.WARNING,
                "texture-missing",
                "BlueMap AE2 used stock fallback because a required installed texture was missing."
        ),
        RENDER_FAILED(
                Level.WARNING,
                "render-failed",
                "BlueMap AE2 geometry rendering failed; the stock resource was used."
        ),
        DRIVE_RENDER_FAILED(
                Level.WARNING,
                "drive-render-failed",
                "BlueMap AE2 drive rendering failed; the stock resource was used."
        ),
        EXTENDED_DRIVE_RENDER_FAILED(
                Level.WARNING,
                "extended-drive-render-failed",
                "BlueMap AE2 Extended Drive rendering failed; the stock resource was used."
        ),
        QUARTZ_GLASS_RENDER_FAILED(
                Level.WARNING,
                "quartz-glass-render-failed",
                "BlueMap AE2 quartz-glass rendering failed; the stock resource was used."
        ),
        CRAFTING_RENDER_FAILED(
                Level.WARNING,
                "crafting-render-failed",
                "BlueMap AE2 formed-crafting rendering failed; the stock resource was used."
        ),
        QUANTUM_BRIDGE_RENDER_FAILED(
                Level.WARNING,
                "quantum-bridge-render-failed",
                "BlueMap AE2 quantum-bridge rendering failed; the stock resource was used."
        ),
        M3_COMPLETION_RENDER_FAILED(
                Level.WARNING,
                "m3-completion-render-failed",
                "BlueMap AE2 M3 completion rendering failed; the stock resource was used."
        ),
        NATIVE_STRUCTURAL_RENDER_FAILED(
                Level.WARNING,
                "native-structural-render-failed",
                "BlueMap AE2 native structural rendering failed; the whole stock resource was used."
        );

        private final Level level;
        private final String key;
        private final String message;

        Event(Level level, String key, String message) {
            this.level = level;
            this.key = key;
            this.message = message;
        }

        public Level level() {
            return level;
        }

        public String key() {
            return key;
        }

        public String message() {
            return message;
        }
    }
}
