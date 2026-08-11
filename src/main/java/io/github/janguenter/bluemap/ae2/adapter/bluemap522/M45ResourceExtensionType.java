/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.adapter.bluemap522;

import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.util.Key;

import java.util.Objects;

/** Factory registered before BlueMap creates a resource pack. */
final class M45ResourceExtensionType
        implements ResourcePack.Extension<M45ResourceExtension> {

    private final Key key;
    private final M45Runtime runtime;

    M45ResourceExtensionType(Key key, M45Runtime runtime) {
        this.key = Objects.requireNonNull(key, "key");
        this.runtime = Objects.requireNonNull(runtime, "runtime");
    }

    @Override
    public Key getKey() {
        return key;
    }

    @Override
    public M45ResourceExtension create(ResourcePack pack) {
        return new M45ResourceExtension(pack, runtime);
    }
}
