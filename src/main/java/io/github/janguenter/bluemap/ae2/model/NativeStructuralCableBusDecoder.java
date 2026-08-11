/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.model;

import java.util.EnumMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/** Strict bounded decoder for the independent native structural cable-bus route. */
public final class NativeStructuralCableBusDecoder {

    private static final Pattern RESOURCE_LOCATION = Pattern.compile(
            "[a-z0-9_.-]+:[a-z0-9/._-]+"
    );

    private final Predicate<String> activeExtensionRoute;

    /** Core-only decoder retained for exact AE2 and unit-only hostile inputs. */
    public NativeStructuralCableBusDecoder() {
        this(routeId -> false);
    }

    /** Decoder whose caller explicitly supplies independently active extension routes. */
    public NativeStructuralCableBusDecoder(Predicate<String> activeExtensionRoute) {
        this.activeExtensionRoute = Objects.requireNonNull(
                activeExtensionRoute,
                "activeExtensionRoute"
        );
    }

    public NativeStructuralDecodeResult decode(
            Object rawCable,
            Map<Direction6, Object> rawFaceParts,
            Map<Direction6, Object> rawFacades
    ) {
        if (rawFaceParts == null || rawFaceParts.size() > Direction6.values().length) {
            return fallback(NativeStructuralDecodeResult.Status.MALFORMED_FACE_PART);
        }
        if (rawFacades == null || rawFacades.size() > Direction6.values().length) {
            return fallback(NativeStructuralDecodeResult.Status.MALFORMED_FACADE);
        }

        CableDefinition cable = null;
        if (rawCable != null) {
            if (!(rawCable instanceof Map<?, ?> center)) {
                return fallback(NativeStructuralDecodeResult.Status.MALFORMED_CENTER_PART);
            }
            Object rawId = center.get("id");
            if (!(rawId instanceof String id) || !RESOURCE_LOCATION.matcher(id).matches()) {
                return fallback(NativeStructuralDecodeResult.Status.MALFORMED_CENTER_PART);
            }
            cable = Ae2CableCatalog.find(id).orElse(null);
            if (cable == null) {
                return fallback(NativeStructuralDecodeResult.Status.UNSUPPORTED_CENTER_PART);
            }
        }

        EnumMap<Direction6, FacePartSnapshot> faceParts = new EnumMap<>(Direction6.class);
        for (Map.Entry<Direction6, Object> entry : rawFaceParts.entrySet()) {
            if (entry.getKey() == null) {
                return fallback(NativeStructuralDecodeResult.Status.MALFORMED_FACE_PART);
            }
            PartDecode decoded = decodePart(entry.getValue());
            if (decoded.status() != NativeStructuralDecodeResult.Status.SUPPORTED) {
                return fallback(decoded.status());
            }
            if (cable != null && cable.family().isDense()
                    && !NativeStructuralPartCatalog.CABLE_ANCHOR.equals(decoded.part().id())) {
                return fallback(
                        NativeStructuralDecodeResult.Status.UNSUPPORTED_FACE_PART_TOPOLOGY
                );
            }
            faceParts.put(entry.getKey(), decoded.part());
        }

        EnumMap<Direction6, FacadeSnapshot> facades = new EnumMap<>(Direction6.class);
        for (Map.Entry<Direction6, Object> entry : rawFacades.entrySet()) {
            if (entry.getKey() == null) {
                return fallback(NativeStructuralDecodeResult.Status.MALFORMED_FACADE);
            }
            FacadeDecode decoded = decodeFacade(entry.getValue());
            if (decoded.status() != NativeStructuralDecodeResult.Status.SUPPORTED) {
                return fallback(decoded.status());
            }
            facades.put(entry.getKey(), decoded.facade());
        }

        if (cable == null && faceParts.isEmpty()) {
            return fallback(facades.isEmpty()
                    ? NativeStructuralDecodeResult.Status.EMPTY_BUS
                    : NativeStructuralDecodeResult.Status.UNSUPPORTED_FACADE_LAYOUT);
        }
        if (cable == null && !facades.isEmpty()) {
            return fallback(NativeStructuralDecodeResult.Status.UNSUPPORTED_FACADE_LAYOUT);
        }

        return NativeStructuralDecodeResult.supported(
                NativeStructuralSnapshot.decoded(cable, faceParts, facades)
        );
    }

    private PartDecode decodePart(Object rawPart) {
        if (!(rawPart instanceof Map<?, ?> part)) {
            return PartDecode.fallback(NativeStructuralDecodeResult.Status.MALFORMED_FACE_PART);
        }
        Object rawId = part.get("id");
        if (!(rawId instanceof String id) || !RESOURCE_LOCATION.matcher(id).matches()) {
            return PartDecode.fallback(NativeStructuralDecodeResult.Status.MALFORMED_FACE_PART);
        }
        NativeStructuralPartCatalog.Definition definition =
                NativeStructuralPartCatalog.findAny(id).orElse(null);
        if (definition == null) {
            return PartDecode.fallback(NativeStructuralDecodeResult.Status.UNSUPPORTED_FACE_PART);
        }
        if (definition.isExtension()
                && !activeExtensionRoute.test(definition.extensionRouteId())) {
            return PartDecode.fallback(NativeStructuralDecodeResult.Status.UNSUPPORTED_FACE_PART);
        }

        int spin = 0;
        if (definition.persistedSpin()) {
            Object rawSpin = part.get("spin");
            if (!(rawSpin instanceof Byte persistedSpin)
                    || persistedSpin < 0 || persistedSpin > 3) {
                return PartDecode.fallback(
                        NativeStructuralDecodeResult.Status.INVALID_FACE_PART_SPIN
                );
            }
            spin = persistedSpin;
        } else if (part.containsKey("spin")) {
            return PartDecode.fallback(NativeStructuralDecodeResult.Status.MALFORMED_FACE_PART);
        }

        Integer frequency = null;
        if (definition.kind() == NativeStructuralPartCatalog.Kind.P2P) {
            Object rawFrequency = part.get("freq");
            if (!(rawFrequency instanceof Short persistedFrequency)) {
                return PartDecode.fallback(
                        NativeStructuralDecodeResult.Status.INVALID_P2P_FREQUENCY
                );
            }
            frequency = persistedFrequency & 0xffff;
        } else if (part.containsKey("freq")) {
            return PartDecode.fallback(NativeStructuralDecodeResult.Status.MALFORMED_FACE_PART);
        }

        String cellItemId = null;
        if (definition.kind() == NativeStructuralPartCatalog.Kind.CELL_DOCK) {
            if (part.containsKey("cell")) {
                Object rawCell = part.get("cell");
                if (!(rawCell instanceof Map<?, ?> cell)) {
                    return PartDecode.fallback(
                            NativeStructuralDecodeResult.Status.MALFORMED_FACE_PART
                    );
                }
                if (!cell.isEmpty()) {
                    if (cell.size() != 1
                            || !(cell.get("id") instanceof String itemId)
                            || !RESOURCE_LOCATION.matcher(itemId).matches()) {
                        return PartDecode.fallback(
                                NativeStructuralDecodeResult.Status.MALFORMED_FACE_PART
                        );
                    }
                    cellItemId = itemId;
                }
            }
        } else if (part.containsKey("cell")) {
            return PartDecode.fallback(NativeStructuralDecodeResult.Status.MALFORMED_FACE_PART);
        }

        return PartDecode.supported(new FacePartSnapshot(
                id,
                spin,
                frequency,
                cellItemId
        ));
    }

    private static FacadeDecode decodeFacade(Object rawFacade) {
        if (!(rawFacade instanceof Map<?, ?> facade)) {
            return FacadeDecode.fallback(NativeStructuralDecodeResult.Status.MALFORMED_FACADE);
        }
        Object rawName = facade.get("Name");
        if (!(rawName instanceof String name) || !RESOURCE_LOCATION.matcher(name).matches()) {
            return FacadeDecode.fallback(NativeStructuralDecodeResult.Status.MALFORMED_FACADE);
        }
        TreeMap<String, String> properties = new TreeMap<>();
        if (facade.containsKey("Properties")) {
            if (!(facade.get("Properties") instanceof Map<?, ?> rawProperties)) {
                return FacadeDecode.fallback(
                        NativeStructuralDecodeResult.Status.MALFORMED_FACADE
                );
            }
            for (Map.Entry<?, ?> entry : rawProperties.entrySet()) {
                if (!(entry.getKey() instanceof String key)
                        || !(entry.getValue() instanceof String value)
                        || key.isEmpty() || value.isEmpty()) {
                    return FacadeDecode.fallback(
                            NativeStructuralDecodeResult.Status.MALFORMED_FACADE
                    );
                }
                properties.put(key, value);
            }
        }
        return FacadeDecode.supported(new FacadeSnapshot(name, properties));
    }

    private static NativeStructuralDecodeResult fallback(
            NativeStructuralDecodeResult.Status status
    ) {
        return NativeStructuralDecodeResult.fallback(status);
    }

    private record PartDecode(
            NativeStructuralDecodeResult.Status status,
            FacePartSnapshot part
    ) {
        private static PartDecode supported(FacePartSnapshot part) {
            return new PartDecode(NativeStructuralDecodeResult.Status.SUPPORTED, part);
        }

        private static PartDecode fallback(NativeStructuralDecodeResult.Status status) {
            return new PartDecode(status, null);
        }
    }

    private record FacadeDecode(
            NativeStructuralDecodeResult.Status status,
            FacadeSnapshot facade
    ) {
        private static FacadeDecode supported(FacadeSnapshot facade) {
            return new FacadeDecode(NativeStructuralDecodeResult.Status.SUPPORTED, facade);
        }

        private static FacadeDecode fallback(NativeStructuralDecodeResult.Status status) {
            return new FacadeDecode(status, null);
        }
    }
}
