/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.activation;

import java.util.Objects;

/** Thread-safe fail-closed state for the exact connected-quartz-glass route. */
public final class QuartzGlassRouteActivation {

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
            snapshot = new Snapshot(State.ACTIVE, Reason.EXACT_19_2_17_QUARTZ_GLASS);
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
        NOT_INSTALLED("quartz-glass-not-installed", State.INACTIVE),
        AWAITING_EXACT_PROFILE("awaiting-exact-quartz-glass-profile", State.INACTIVE),
        EXACT_19_2_17_QUARTZ_GLASS("exact-19.2.17-quartz-glass", State.ACTIVE),
        SYNTHETIC_BLOCK_STATE_MISSING(
                "quartz-glass-synthetic-blockstate-missing",
                State.INACTIVE
        ),
        SYNTHETIC_BLOCK_STATE_INVALID(
                "quartz-glass-synthetic-blockstate-invalid",
                State.INACTIVE
        ),
        REQUIRED_RESOURCES_MISMATCH(
                "quartz-glass-required-resources-mismatch",
                State.INACTIVE
        ),
        OPERATOR_DISABLED("quartz-glass-disabled-by-operator", State.DISABLED),
        REGISTRY_COLLISION("quartz-glass-registry-collision", State.DISABLED),
        RESOURCE_LOAD_CALLBACK_FAILED(
                "quartz-glass-resource-load-callback-failed",
                State.DISABLED
        ),
        RESOURCE_BAKE_CALLBACK_FAILED(
                "quartz-glass-resource-bake-callback-failed",
                State.DISABLED
        ),
        RENDER_CALLBACK_FAILED("quartz-glass-render-callback-failed", State.DISABLED);

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
