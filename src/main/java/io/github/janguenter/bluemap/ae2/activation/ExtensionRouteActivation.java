/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.activation;

import java.util.Objects;

/**
 * Thread-safe fail-closed state for one exact, independently isolated extension route.
 *
 * <p>The route ID and exact artifact reason remain data, so adding another exact
 * AE2-family profile does not require another nearly identical activation class.</p>
 */
public final class ExtensionRouteActivation {

    private final String routeId;
    private volatile Snapshot snapshot;

    public ExtensionRouteActivation(String routeId) {
        this.routeId = requireWireValue(routeId, "routeId");
        this.snapshot = new Snapshot(State.INACTIVE, Reason.NOT_INSTALLED, "not-installed");
    }

    public String routeId() {
        return routeId;
    }

    public Snapshot snapshot() {
        return snapshot;
    }

    public boolean isActive() {
        return snapshot.state() == State.ACTIVE;
    }

    public boolean isDisabled() {
        return snapshot.state() == State.DISABLED;
    }

    public synchronized void activate(String exactReason) {
        if (snapshot.state() != State.DISABLED) {
            snapshot = new Snapshot(
                    State.ACTIVE,
                    Reason.EXACT_PROFILE,
                    requireWireValue(exactReason, "exactReason")
            );
        }
    }

    public synchronized void inactive(Reason reason, String detail) {
        Objects.requireNonNull(reason, "reason");
        if (reason.state() != State.INACTIVE) {
            throw new IllegalArgumentException("reason does not describe an inactive state");
        }
        if (snapshot.state() != State.DISABLED) {
            snapshot = new Snapshot(State.INACTIVE, reason, requireWireValue(detail, "detail"));
        }
    }

    public synchronized void disable(Reason reason, String detail) {
        Objects.requireNonNull(reason, "reason");
        if (reason.state() != State.DISABLED) {
            throw new IllegalArgumentException("reason does not describe a disabled state");
        }
        snapshot = new Snapshot(State.DISABLED, reason, requireWireValue(detail, "detail"));
    }

    private static String requireWireValue(String value, String label) {
        Objects.requireNonNull(value, label);
        if (!value.matches("[a-z0-9][a-z0-9._-]*")) {
            throw new IllegalArgumentException(label + " must be a lowercase wire value");
        }
        return value;
    }

    public enum State {
        INACTIVE,
        ACTIVE,
        DISABLED
    }

    public enum Reason {
        NOT_INSTALLED(State.INACTIVE),
        AWAITING_EXACT_PROFILE(State.INACTIVE),
        ARTIFACT_NOT_FOUND(State.INACTIVE),
        ARTIFACT_MISMATCH(State.INACTIVE),
        MULTIPLE_ARTIFACTS(State.INACTIVE),
        ARTIFACT_READ_FAILED(State.INACTIVE),
        BLOCKED_BY_CORE(State.INACTIVE),
        REQUIRED_RESOURCES_MISMATCH(State.INACTIVE),
        SYNTHETIC_BLOCK_STATE_MISSING(State.INACTIVE),
        SYNTHETIC_BLOCK_STATE_INVALID(State.INACTIVE),
        EXACT_PROFILE(State.ACTIVE),
        OPERATOR_DISABLED(State.DISABLED),
        REGISTRY_COLLISION(State.DISABLED),
        RETENTION_PROBE_FAILED(State.DISABLED),
        RESOURCE_LOAD_CALLBACK_FAILED(State.DISABLED),
        RESOURCE_BAKE_CALLBACK_FAILED(State.DISABLED),
        RENDER_CALLBACK_FAILED(State.DISABLED);

        private final State state;

        Reason(State state) {
            this.state = state;
        }

        State state() {
            return state;
        }
    }

    public record Snapshot(State state, Reason reason, String detail) {

        public Snapshot {
            Objects.requireNonNull(state, "state");
            Objects.requireNonNull(reason, "reason");
            requireWireValue(detail, "detail");
            if (reason.state() != state) {
                throw new IllegalArgumentException("reason and state differ");
            }
        }
    }
}
