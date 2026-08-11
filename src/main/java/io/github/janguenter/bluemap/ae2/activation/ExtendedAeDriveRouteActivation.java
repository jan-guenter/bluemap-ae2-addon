/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.activation;

import io.github.janguenter.bluemap.ae2.profile.extendedae.ExtendedAe2233Profile;
import io.github.janguenter.bluemap.ae2.profile.extendedae.ExtendedAe2235ArtifactIdentity;

import java.util.Objects;

/** Thread-safe fail-closed state for the optional exact ExtendedAE Drive route. */
public final class ExtendedAeDriveRouteActivation {

    private volatile Snapshot snapshot = new Snapshot(State.INACTIVE, Reason.NOT_INSTALLED);

    public Snapshot snapshot() {
        return snapshot;
    }

    public boolean isActive() {
        return snapshot.state() == State.ACTIVE;
    }

    public boolean isDisabled() {
        return snapshot.state() == State.DISABLED;
    }

    public String reason() {
        return snapshot.reason().wireName();
    }

    public synchronized void activate() {
        activate(ExtendedAe2233Profile.EXACT_REASON);
    }

    public synchronized void activate(String exactArtifactReason) {
        Objects.requireNonNull(exactArtifactReason, "exactArtifactReason");
        if (snapshot.state() != State.DISABLED) {
            Reason reason;
            if (ExtendedAe2233Profile.EXACT_REASON.equals(exactArtifactReason)) {
                reason = Reason.EXACT_AE2_19_2_17_EXTENDEDAE_2_2_33_DRIVE;
            } else if (ExtendedAe2235ArtifactIdentity.EXACT_REASON.equals(
                    exactArtifactReason
            )) {
                reason = Reason.EXACT_AE2_19_2_17_EXTENDEDAE_2_2_35_DRIVE;
            } else {
                throw new IllegalArgumentException("unsupported exact ExtendedAE reason");
            }
            snapshot = new Snapshot(
                    State.ACTIVE,
                    reason
            );
        }
    }

    public synchronized void inactive(Reason reason) {
        Objects.requireNonNull(reason, "reason");
        if (reason.state() != State.INACTIVE) {
            throw new IllegalArgumentException("reason does not describe an inactive state");
        }
        if (snapshot.state() != State.DISABLED) {
            snapshot = new Snapshot(State.INACTIVE, reason);
        }
    }

    public synchronized void disable(Reason reason) {
        Objects.requireNonNull(reason, "reason");
        if (reason.state() != State.DISABLED) {
            throw new IllegalArgumentException("reason does not describe a disabled state");
        }
        snapshot = new Snapshot(State.DISABLED, reason);
    }

    public enum State {
        INACTIVE,
        ACTIVE,
        DISABLED
    }

    public enum Reason {
        NOT_INSTALLED("extended-drive-not-installed", State.INACTIVE),
        AWAITING_EXACT_PROFILE("awaiting-exact-extended-drive-profile", State.INACTIVE),
        EXACT_AE2_19_2_17_EXTENDEDAE_2_2_33_DRIVE(
                "exact-ae2-19.2.17-extendedae-2.2.33-drive",
                State.ACTIVE
        ),
        EXACT_AE2_19_2_17_EXTENDEDAE_2_2_35_DRIVE(
                "exact-ae2-19.2.17-extendedae-2.2.35-drive",
                State.ACTIVE
        ),
        AE2_ARTIFACT_NOT_FOUND("extended-drive-ae2-artifact-not-found", State.INACTIVE),
        AE2_ARTIFACT_MISMATCH("extended-drive-ae2-artifact-mismatch", State.INACTIVE),
        EXTENDEDAE_ARTIFACT_NOT_FOUND(
                "extended-drive-extendedae-artifact-not-found",
                State.INACTIVE
        ),
        EXTENDEDAE_ARTIFACT_MISMATCH(
                "extended-drive-extendedae-artifact-mismatch",
                State.INACTIVE
        ),
        ARTIFACT_READ_FAILED("extended-drive-artifact-read-failed", State.INACTIVE),
        SYNTHETIC_BLOCK_STATE_MISSING(
                "extended-drive-synthetic-blockstate-missing",
                State.INACTIVE
        ),
        SYNTHETIC_BLOCK_STATE_INVALID(
                "extended-drive-synthetic-blockstate-invalid",
                State.INACTIVE
        ),
        REQUIRED_RESOURCES_MISMATCH(
                "extended-drive-required-resources-mismatch",
                State.INACTIVE
        ),
        OPERATOR_DISABLED("extended-drive-disabled-by-operator", State.DISABLED),
        REGISTRY_COLLISION("extended-drive-registry-collision", State.DISABLED),
        BLUENBT_RETENTION_PROBE_FAILED(
                "extended-drive-bluenbt-retention-probe-failed",
                State.DISABLED
        ),
        RESOURCE_LOAD_CALLBACK_FAILED(
                "extended-drive-resource-load-callback-failed",
                State.DISABLED
        ),
        RESOURCE_BAKE_CALLBACK_FAILED(
                "extended-drive-resource-bake-callback-failed",
                State.DISABLED
        ),
        RENDER_CALLBACK_FAILED("extended-drive-render-callback-failed", State.DISABLED);

        private final String wireName;
        private final State state;

        Reason(String wireName, State state) {
            this.wireName = wireName;
            this.state = state;
        }

        public String wireName() {
            return wireName;
        }

        State state() {
            return state;
        }
    }

    public record Snapshot(State state, Reason reason) {

        public Snapshot {
            Objects.requireNonNull(state, "state");
            Objects.requireNonNull(reason, "reason");
        }
    }
}
