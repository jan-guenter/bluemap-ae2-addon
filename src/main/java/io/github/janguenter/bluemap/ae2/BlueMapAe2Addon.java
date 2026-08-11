/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2;

import io.github.janguenter.bluemap.ae2.adapter.bluemap522.AdapterCompatibility;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * BlueMap add-on entrypoint. BlueMap invokes this before it constructs its
 * resource pack, which is the only safe point to register the cable-bus DTO.
 */
public final class BlueMapAe2Addon implements Runnable {

    public BlueMapAe2Addon() {
    }

    @Override
    public void run() {
        try {
            if (!AdapterCompatibility.currentRuntimeSupported()) {
                inactive("unsupported BlueMap internal ABI", null);
                return;
            }

            Class<?> adapterType = Class.forName(
                    "io.github.janguenter.bluemap.ae2.adapter.bluemap522.BlueMap522Adapter",
                    true,
                    BlueMapAe2Addon.class.getClassLoader()
            );
            Method install = adapterType.getMethod("install");
            install.invoke(null);
        } catch (InvocationTargetException exception) {
            inactive("exact adapter initialization failed", exception.getCause());
        } catch (ReflectiveOperationException | LinkageError | RuntimeException exception) {
            inactive("exact adapter is unavailable", exception);
        }
    }

    private static void inactive(String reason, Throwable cause) {
        String detail = cause == null ? "" : " (" + cause.getClass().getSimpleName() + ")";
        System.err.println("BlueMap AE2 add-on is inactive: " + reason + detail + ".");
    }
}
