/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.model;

import java.util.Objects;
import java.util.Optional;

/** Exact-profile drive decode outcome; non-supported results require stock fallback. */
public record DriveDecodeResult(Status status, DriveSnapshot snapshot) {

    public DriveDecodeResult {
        Objects.requireNonNull(status, "status");
        if ((status == Status.SUPPORTED) != (snapshot != null)) {
            throw new IllegalArgumentException("only supported results may contain a snapshot");
        }
    }

    public static DriveDecodeResult supported(DriveSnapshot snapshot) {
        return new DriveDecodeResult(Status.SUPPORTED, Objects.requireNonNull(snapshot, "snapshot"));
    }

    public static DriveDecodeResult fallback(Status status) {
        if (status == Status.SUPPORTED) {
            throw new IllegalArgumentException("supported results require a snapshot");
        }
        return new DriveDecodeResult(status, null);
    }

    public boolean isSupported() {
        return status == Status.SUPPORTED;
    }

    public Optional<DriveSnapshot> supportedSnapshot() {
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
