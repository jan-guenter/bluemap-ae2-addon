/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.activation;

import io.github.janguenter.bluemap.ae2.profile.Ae219217Profile;

import java.util.Objects;

/** Thread-safe, typed, fail-closed state for one installed exact profile. */
public final class ProfileActivation {

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
        if (snapshot.state() != State.DISABLED) {
            snapshot = new Snapshot(State.ACTIVE, Reason.EXACT_19_2_17);
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
        NOT_INSTALLED("not-installed", State.INACTIVE),
        AWAITING_EXACT_PROFILE("awaiting-exact-ae2-profile", State.INACTIVE),
        EXACT_19_2_17(Ae219217Profile.EXACT_REASON, State.ACTIVE),
        ARTIFACT_NOT_FOUND("ae2-artifact-not-found", State.INACTIVE),
        MULTIPLE_ARTIFACTS("multiple-ae2-artifacts", State.INACTIVE),
        UNSUPPORTED_ARTIFACT("unsupported-ae2-artifact", State.INACTIVE),
        ARTIFACT_IDENTITY_REJECTED("ae2-artifact-identity-rejected", State.INACTIVE),
        ARTIFACT_READ_FAILED("ae2-artifact-read-failed", State.INACTIVE),
        REQUIRED_RESOURCES_MISMATCH("required-resources-mismatch", State.INACTIVE),
        SYNTHETIC_BLOCK_STATE_MISSING("synthetic-blockstate-missing", State.INACTIVE),
        SYNTHETIC_BLOCK_STATE_INVALID("synthetic-blockstate-invalid", State.INACTIVE),
        REQUIRED_TEXTURE_MISSING("required-texture-missing", State.INACTIVE),
        UNSUPPORTED_BLUEMAP_RUNTIME("unsupported-bluemap-runtime", State.DISABLED),
        OPERATOR_DISABLED("profile-disabled-by-operator", State.DISABLED),
        BLUENBT_RETENTION_PROBE_FAILED("bluenbt-retention-probe-failed", State.DISABLED),
        REGISTRY_COLLISION("registry-collision", State.DISABLED),
        RESOURCE_LOAD_CALLBACK_FAILED("resource-load-callback-failed", State.DISABLED),
        RESOURCE_BAKE_CALLBACK_FAILED("resource-bake-callback-failed", State.DISABLED),
        RENDER_CALLBACK_FAILED("render-callback-failed", State.DISABLED);

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
