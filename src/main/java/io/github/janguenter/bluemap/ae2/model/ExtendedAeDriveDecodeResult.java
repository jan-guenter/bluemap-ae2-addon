/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.model;

import java.util.Objects;
import java.util.Optional;

/** Exact M3b decode outcome; every non-supported result requires atomic fallback. */
public record ExtendedAeDriveDecodeResult(
        Status status,
        ExtendedAeDriveSnapshot snapshot
) {

    public ExtendedAeDriveDecodeResult {
        Objects.requireNonNull(status, "status");
        if ((status == Status.SUPPORTED) != (snapshot != null)) {
            throw new IllegalArgumentException("only supported results may contain a snapshot");
        }
    }

    public static ExtendedAeDriveDecodeResult supported(ExtendedAeDriveSnapshot snapshot) {
        return new ExtendedAeDriveDecodeResult(
                Status.SUPPORTED,
                Objects.requireNonNull(snapshot, "snapshot")
        );
    }

    public static ExtendedAeDriveDecodeResult fallback(Status status) {
        if (status == Status.SUPPORTED) {
            throw new IllegalArgumentException("supported results require a snapshot");
        }
        return new ExtendedAeDriveDecodeResult(status, null);
    }

    public boolean isSupported() {
        return status == Status.SUPPORTED;
    }

    public Optional<ExtendedAeDriveSnapshot> supportedSnapshot() {
        return Optional.ofNullable(snapshot);
    }

    public enum Status {
        SUPPORTED,
        MISSING_INVENTORY,
        INVALID_FACING,
        INVALID_SPIN,
        INVALID_CELL_COUNT,
        INVALID_CELL_ID,
        UNSUPPORTED_CELL_ID
    }
}
