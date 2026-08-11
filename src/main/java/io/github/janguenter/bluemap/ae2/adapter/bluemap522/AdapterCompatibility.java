/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.adapter.bluemap522;

import de.bluecolored.bluemap.core.BlueMap;

/** Exact BlueMap runtime identities whose audited internal ABI is accepted. */
public final class AdapterCompatibility {

    public static final String UPSTREAM_VERSION = "5.22";
    public static final String UPSTREAM_COMMIT =
            "fe5115d5548a30d34175b8e0449aaca280af199f";
    public static final String BACKPORT_VERSION = "5.22-agent.backport-5.22-mc1.21.1-2";
    public static final String BACKPORT_COMMIT =
            "9be321df995a1103808621d529eb72773e719d4d";

    private AdapterCompatibility() {
    }

    public static boolean currentRuntimeSupported() {
        return supported(BlueMap.VERSION, BlueMap.GIT_HASH);
    }

    public static boolean supported(String version, String gitHash) {
        return (UPSTREAM_VERSION.equals(version) && UPSTREAM_COMMIT.equals(gitHash))
                || (BACKPORT_VERSION.equals(version) && BACKPORT_COMMIT.equals(gitHash));
    }
}
