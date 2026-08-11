/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.adapter.bluemap522;

import io.github.janguenter.bluemap.ae2.model.Direction6;
import io.github.janguenter.bluemap.ae2.model.NativeStructuralPartCatalog;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

/**
 * Enforces extension failure isolation before the strict core cable-bus decoder.
 *
 * <p>Unknown non-profile parts remain untouched and therefore retain atomic
 * fallback. Only a positively catalogued extension part may be omitted, and
 * only when its independently owned route is inactive or its own direct state
 * is malformed.</p>
 */
final class M45FacePartSanitizer {

    private M45FacePartSanitizer() {
    }

    static Map<Direction6, Object> sanitize(
            Map<Direction6, Object> rawParts,
            M45Runtime runtime
    ) {
        Objects.requireNonNull(runtime, "runtime");
        if (rawParts == null) {
            return null;
        }
        EnumMap<Direction6, Object> result = new EnumMap<>(Direction6.class);
        for (Map.Entry<Direction6, Object> entry : rawParts.entrySet()) {
            if (entry.getKey() == null) {
                // Preserve the malformed boundary for the strict decoder. EnumMap
                // cannot represent it, and silently dropping it would turn an
                // invalid callback into apparently supported structural data.
                return rawParts;
            }
            Object raw = entry.getValue();
            NativeStructuralPartCatalog.Definition extension = extensionDefinition(raw);
            if (extension == null) {
                result.put(entry.getKey(), raw);
                continue;
            }
            if (!runtime.active(extension.extensionRouteId())
                    || !directStateValid(raw, extension)) {
                continue;
            }
            result.put(entry.getKey(), raw);
        }
        return Collections.unmodifiableMap(result);
    }

    private static NativeStructuralPartCatalog.Definition extensionDefinition(Object raw) {
        if (!(raw instanceof Map<?, ?> part) || !(part.get("id") instanceof String id)) {
            return null;
        }
        NativeStructuralPartCatalog.Definition definition =
                NativeStructuralPartCatalog.findAny(id).orElse(null);
        return definition != null && definition.isExtension() ? definition : null;
    }

    private static boolean directStateValid(
            Object raw,
            NativeStructuralPartCatalog.Definition definition
    ) {
        Map<?, ?> part = (Map<?, ?>) raw;
        if (definition.persistedSpin()) {
            if (!(part.get("spin") instanceof Byte spin) || spin < 0 || spin > 3) {
                return false;
            }
        } else if (part.containsKey("spin")) {
            return false;
        }
        if (part.containsKey("freq")) {
            return false;
        }
        if (definition.kind() != NativeStructuralPartCatalog.Kind.CELL_DOCK) {
            return !part.containsKey("cell");
        }
        if (!part.containsKey("cell")) {
            return true;
        }
        Object rawCell = part.get("cell");
        if (!(rawCell instanceof Map<?, ?> cell)) {
            return false;
        }
        return cell.isEmpty()
                || cell.size() == 1
                && cell.get("id") instanceof String itemId
                && itemId.matches("[a-z0-9_.-]+:[a-z0-9/._-]+");
    }
}
