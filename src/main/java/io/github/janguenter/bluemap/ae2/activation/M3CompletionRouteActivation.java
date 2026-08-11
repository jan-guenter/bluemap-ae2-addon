/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.activation;

import java.util.Objects;

/** Thread-safe fail-closed state for the exact AE2 M3 completion route. */
public final class M3CompletionRouteActivation {

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
            snapshot = new Snapshot(State.ACTIVE, Reason.EXACT_19_2_17_M3_COMPLETION);
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
        NOT_INSTALLED("m3-completion-not-installed", State.INACTIVE),
        AWAITING_EXACT_PROFILE("awaiting-exact-ae2-m3-completion-profile", State.INACTIVE),
        EXACT_19_2_17_M3_COMPLETION("exact-19.2.17-m3-completion", State.ACTIVE),
        SYNTHETIC_BLOCK_STATE_MISSING(
                "m3-completion-synthetic-blockstate-missing",
                State.INACTIVE
        ),
        SYNTHETIC_BLOCK_STATE_INVALID(
                "m3-completion-synthetic-blockstate-invalid",
                State.INACTIVE
        ),
        REQUIRED_RESOURCES_MISMATCH(
                "m3-completion-required-resources-mismatch",
                State.INACTIVE
        ),
        OPERATOR_DISABLED("m3-completion-disabled-by-operator", State.DISABLED),
        REGISTRY_COLLISION("m3-completion-registry-collision", State.DISABLED),
        RETENTION_PROBE_FAILED("m3-completion-retention-probe-failed", State.DISABLED),
        RESOURCE_LOAD_CALLBACK_FAILED(
                "m3-completion-resource-load-callback-failed",
                State.DISABLED
        ),
        RESOURCE_BAKE_CALLBACK_FAILED(
                "m3-completion-resource-bake-callback-failed",
                State.DISABLED
        ),
        RENDER_CALLBACK_FAILED("m3-completion-render-callback-failed", State.DISABLED);

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
