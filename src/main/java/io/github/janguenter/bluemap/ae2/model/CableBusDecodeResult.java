/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.model;

import java.util.Objects;
import java.util.Optional;

/** Exact-profile decode outcome; every non-supported status requires stock fallback. */
public record CableBusDecodeResult(Status status, CableBusSnapshot snapshot) {

    public CableBusDecodeResult {
        Objects.requireNonNull(status, "status");
        if ((status == Status.SUPPORTED) != (snapshot != null)) {
            throw new IllegalArgumentException("only supported results may contain a snapshot");
        }
    }

    public static CableBusDecodeResult supported(CableBusSnapshot snapshot) {
        return new CableBusDecodeResult(Status.SUPPORTED, Objects.requireNonNull(snapshot, "snapshot"));
    }

    public static CableBusDecodeResult fallback(Status status) {
        if (status == Status.SUPPORTED) {
            throw new IllegalArgumentException("supported results require a snapshot");
        }
        return new CableBusDecodeResult(status, null);
    }

    public boolean isSupported() {
        return status == Status.SUPPORTED;
    }

    public Optional<CableBusSnapshot> supportedSnapshot() {
        return Optional.ofNullable(snapshot);
    }

    public enum Status {
        SUPPORTED,
        MISSING_CENTER_PART,
        CENTER_PART_NOT_COMPOUND,
        MISSING_CENTER_PART_ID,
        CENTER_PART_ID_NOT_STRING,
        INVALID_CENTER_PART_ID,
        UNSUPPORTED_CENTER_PART,
        MALFORMED_FACE_PART,
        UNSUPPORTED_FACE_PART,
        INVALID_FACE_PART_SPIN,
        UNSUPPORTED_FACE_PART_TOPOLOGY,
        MALFORMED_FACADE,
        UNSUPPORTED_FACADE_STATE,
        UNSUPPORTED_FACADE_LAYOUT,
        UNSUPPORTED_ATTACHMENTS_OR_FACADES
    }
}
