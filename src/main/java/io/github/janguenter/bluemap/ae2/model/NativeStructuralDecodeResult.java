/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.model;

import java.util.Objects;
import java.util.Optional;

/** Decode outcome; every non-supported status requires whole-block stock fallback. */
public record NativeStructuralDecodeResult(Status status, NativeStructuralSnapshot snapshot) {

    public NativeStructuralDecodeResult {
        Objects.requireNonNull(status, "status");
        if ((status == Status.SUPPORTED) != (snapshot != null)) {
            throw new IllegalArgumentException("only supported results may contain a snapshot");
        }
    }

    public static NativeStructuralDecodeResult supported(NativeStructuralSnapshot snapshot) {
        return new NativeStructuralDecodeResult(
                Status.SUPPORTED,
                Objects.requireNonNull(snapshot, "snapshot")
        );
    }

    public static NativeStructuralDecodeResult fallback(Status status) {
        if (status == Status.SUPPORTED) {
            throw new IllegalArgumentException("supported results require a snapshot");
        }
        return new NativeStructuralDecodeResult(status, null);
    }

    public boolean isSupported() {
        return status == Status.SUPPORTED;
    }

    public Optional<NativeStructuralSnapshot> supportedSnapshot() {
        return Optional.ofNullable(snapshot);
    }

    public enum Status {
        SUPPORTED,
        EMPTY_BUS,
        MALFORMED_CENTER_PART,
        UNSUPPORTED_CENTER_PART,
        MALFORMED_FACE_PART,
        UNSUPPORTED_FACE_PART,
        INVALID_FACE_PART_SPIN,
        INVALID_P2P_FREQUENCY,
        UNSUPPORTED_FACE_PART_TOPOLOGY,
        MALFORMED_FACADE,
        UNSUPPORTED_FACADE_LAYOUT
    }
}
