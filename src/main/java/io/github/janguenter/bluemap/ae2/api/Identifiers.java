/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.api;

import java.util.Objects;
import java.util.regex.Pattern;

final class Identifiers {

    private static final Pattern RESOURCE_LOCATION = Pattern.compile(
            "[a-z0-9_.-]+:[a-z0-9/._-]+"
    );
    private static final Pattern NAMESPACE = Pattern.compile("[a-z0-9_.-]+");
    private static final Pattern ROUTE = Pattern.compile("[a-z0-9][a-z0-9._-]*");

    private Identifiers() {
    }

    static String requireResourceLocation(String value, String name) {
        Objects.requireNonNull(value, name);
        if (value.length() > 256 || !RESOURCE_LOCATION.matcher(value).matches()) {
            throw new IllegalArgumentException("invalid " + name);
        }
        return value;
    }

    static String requireNamespace(String value) {
        Objects.requireNonNull(value, "ownerNamespace");
        if (value.length() > 64 || !NAMESPACE.matcher(value).matches()) {
            throw new IllegalArgumentException("invalid ownerNamespace");
        }
        return value;
    }

    static String requireRouteId(String value) {
        Objects.requireNonNull(value, "routeId");
        if (value.length() > 128 || !ROUTE.matcher(value).matches()) {
            throw new IllegalArgumentException("invalid routeId");
        }
        return value;
    }

    static String namespace(String resourceLocation) {
        return resourceLocation.substring(0, resourceLocation.indexOf(':'));
    }
}
