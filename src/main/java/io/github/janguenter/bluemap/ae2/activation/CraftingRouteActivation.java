/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.activation;

import java.util.Objects;

/** Thread-safe fail-closed state for the exact formed-crafting-block route. */
public final class CraftingRouteActivation {

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
            snapshot = new Snapshot(State.ACTIVE, Reason.EXACT_19_2_17_CRAFTING);
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
        NOT_INSTALLED("crafting-not-installed", State.INACTIVE),
        AWAITING_EXACT_PROFILE("awaiting-exact-ae2-crafting-profile", State.INACTIVE),
        EXACT_19_2_17_CRAFTING("exact-19.2.17-crafting", State.ACTIVE),
        SYNTHETIC_BLOCK_STATE_MISSING(
                "crafting-synthetic-blockstate-missing",
                State.INACTIVE
        ),
        SYNTHETIC_BLOCK_STATE_INVALID(
                "crafting-synthetic-blockstate-invalid",
                State.INACTIVE
        ),
        REQUIRED_RESOURCES_MISMATCH(
                "crafting-required-resources-mismatch",
                State.INACTIVE
        ),
        OPERATOR_DISABLED("crafting-disabled-by-operator", State.DISABLED),
        REGISTRY_COLLISION("crafting-registry-collision", State.DISABLED),
        BLUENBT_RETENTION_PROBE_FAILED(
                "crafting-bluenbt-retention-probe-failed",
                State.DISABLED
        ),
        RESOURCE_LOAD_CALLBACK_FAILED(
                "crafting-resource-load-callback-failed",
                State.DISABLED
        ),
        RESOURCE_BAKE_CALLBACK_FAILED(
                "crafting-resource-bake-callback-failed",
                State.DISABLED
        ),
        RENDER_CALLBACK_FAILED("crafting-render-callback-failed", State.DISABLED);

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
