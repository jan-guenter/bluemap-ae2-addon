/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.model;

import java.util.Map;
import java.util.EnumMap;
import java.util.regex.Pattern;

/** Strict decoder for the bounded AE2 19.2.17 cable-center payload. */
public final class CableBusDecoder {

    private static final Pattern RESOURCE_LOCATION = Pattern.compile(
            "[a-z0-9_.-]+:[a-z0-9/._-]+"
    );

    public CableBusDecodeResult decode(Object rawCable, boolean hasAttachmentsOrFacades) {
        if (hasAttachmentsOrFacades) {
            return CableBusDecodeResult.fallback(
                    CableBusDecodeResult.Status.UNSUPPORTED_ATTACHMENTS_OR_FACADES
            );
        }
        return decode(rawCable, Map.of(), Map.of());
    }

    public CableBusDecodeResult decode(
            Object rawCable,
            Map<Direction6, Object> rawFaceParts,
            Map<Direction6, Object> rawFacades
    ) {
        if (rawCable == null) {
            return CableBusDecodeResult.fallback(
                    CableBusDecodeResult.Status.MISSING_CENTER_PART
            );
        }
        if (!(rawCable instanceof Map<?, ?> cable)) {
            return CableBusDecodeResult.fallback(
                    CableBusDecodeResult.Status.CENTER_PART_NOT_COMPOUND
            );
        }

        Object rawId = cable.get("id");
        if (rawId == null) {
            return CableBusDecodeResult.fallback(
                    CableBusDecodeResult.Status.MISSING_CENTER_PART_ID
            );
        }
        if (!(rawId instanceof String id)) {
            return CableBusDecodeResult.fallback(
                    CableBusDecodeResult.Status.CENTER_PART_ID_NOT_STRING
            );
        }
        if (!RESOURCE_LOCATION.matcher(id).matches()) {
            return CableBusDecodeResult.fallback(
                    CableBusDecodeResult.Status.INVALID_CENTER_PART_ID
            );
        }
        CableDefinition definition = Ae2CableCatalog.find(id).orElse(null);
        if (definition == null) {
            return CableBusDecodeResult.fallback(
                    CableBusDecodeResult.Status.UNSUPPORTED_CENTER_PART
            );
        }
        EnumMap<Direction6, FacePartSnapshot> faceParts = new EnumMap<>(Direction6.class);
        for (Map.Entry<Direction6, Object> entry : rawFaceParts.entrySet()) {
            FacePartDecode part = decodeFacePart(entry.getValue());
            if (part.status() != CableBusDecodeResult.Status.SUPPORTED) {
                return CableBusDecodeResult.fallback(part.status());
            }
            faceParts.put(entry.getKey(), part.snapshot());
        }
        if (!faceParts.isEmpty() && definition.family().isDense()) {
            return CableBusDecodeResult.fallback(
                    CableBusDecodeResult.Status.UNSUPPORTED_FACE_PART_TOPOLOGY
            );
        }

        EnumMap<Direction6, FacadeSnapshot> facades = new EnumMap<>(Direction6.class);
        for (Map.Entry<Direction6, Object> entry : rawFacades.entrySet()) {
            FacadeDecode facade = decodeFacade(entry.getValue());
            if (facade.status() != CableBusDecodeResult.Status.SUPPORTED) {
                return CableBusDecodeResult.fallback(facade.status());
            }
            facades.put(entry.getKey(), facade.snapshot());
        }
        if (facades.size() > 1) {
            return CableBusDecodeResult.fallback(
                    CableBusDecodeResult.Status.UNSUPPORTED_FACADE_LAYOUT
            );
        }
        if (!facades.isEmpty()) {
            Direction6 direction = facades.keySet().iterator().next();
            if (faceParts.size() != 1 || !faceParts.containsKey(direction)) {
                return CableBusDecodeResult.fallback(
                        CableBusDecodeResult.Status.UNSUPPORTED_FACADE_LAYOUT
                );
            }
        }

        return CableBusDecodeResult.supported(new CableBusSnapshot(
                definition,
                Map.of(),
                faceParts,
                facades
        ));
    }

    private static FacePartDecode decodeFacePart(Object rawPart) {
        if (!(rawPart instanceof Map<?, ?> part)) {
            return FacePartDecode.fallback(CableBusDecodeResult.Status.MALFORMED_FACE_PART);
        }
        Object rawId = part.get("id");
        if (!(rawId instanceof String id) || !RESOURCE_LOCATION.matcher(id).matches()) {
            return FacePartDecode.fallback(CableBusDecodeResult.Status.MALFORMED_FACE_PART);
        }
        if (!FacePartSnapshot.TERMINAL.equals(id)) {
            return FacePartDecode.fallback(CableBusDecodeResult.Status.UNSUPPORTED_FACE_PART);
        }
        Object rawSpin = part.get("spin");
        if (!(rawSpin instanceof Byte spin)) {
            return FacePartDecode.fallback(CableBusDecodeResult.Status.INVALID_FACE_PART_SPIN);
        }
        if (spin < 0 || spin > 3) {
            return FacePartDecode.fallback(CableBusDecodeResult.Status.INVALID_FACE_PART_SPIN);
        }
        return FacePartDecode.supported(new FacePartSnapshot(id, spin));
    }

    private static FacadeDecode decodeFacade(Object rawFacade) {
        if (!(rawFacade instanceof Map<?, ?> facade)) {
            return FacadeDecode.fallback(CableBusDecodeResult.Status.MALFORMED_FACADE);
        }
        Object rawName = facade.get("Name");
        if (!(rawName instanceof String name) || !RESOURCE_LOCATION.matcher(name).matches()) {
            return FacadeDecode.fallback(CableBusDecodeResult.Status.MALFORMED_FACADE);
        }
        if (facade.containsKey("Properties")) {
            Object rawProperties = facade.get("Properties");
            if (!(rawProperties instanceof Map<?, ?> properties)) {
                return FacadeDecode.fallback(CableBusDecodeResult.Status.MALFORMED_FACADE);
            }
            for (Map.Entry<?, ?> property : properties.entrySet()) {
                if (!(property.getKey() instanceof String)
                        || !(property.getValue() instanceof String)) {
                    return FacadeDecode.fallback(CableBusDecodeResult.Status.MALFORMED_FACADE);
                }
            }
            return FacadeDecode.fallback(
                    CableBusDecodeResult.Status.UNSUPPORTED_FACADE_STATE
            );
        }
        Map<String, String> normalized = new java.util.TreeMap<>();
        FacadeSnapshot snapshot = new FacadeSnapshot(name, normalized);
        if (!snapshot.isPlainStone()) {
            return FacadeDecode.fallback(CableBusDecodeResult.Status.UNSUPPORTED_FACADE_STATE);
        }
        return FacadeDecode.supported(snapshot);
    }

    private record FacePartDecode(
            CableBusDecodeResult.Status status,
            FacePartSnapshot snapshot
    ) {
        private static FacePartDecode supported(FacePartSnapshot snapshot) {
            return new FacePartDecode(CableBusDecodeResult.Status.SUPPORTED, snapshot);
        }

        private static FacePartDecode fallback(CableBusDecodeResult.Status status) {
            return new FacePartDecode(status, null);
        }
    }

    private record FacadeDecode(
            CableBusDecodeResult.Status status,
            FacadeSnapshot snapshot
    ) {
        private static FacadeDecode supported(FacadeSnapshot snapshot) {
            return new FacadeDecode(CableBusDecodeResult.Status.SUPPORTED, snapshot);
        }

        private static FacadeDecode fallback(CableBusDecodeResult.Status status) {
            return new FacadeDecode(status, null);
        }
    }
}
