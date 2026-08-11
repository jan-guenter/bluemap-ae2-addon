/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.model;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Immutable neutral templates matching AE2 19.2.17's five cable families. */
public final class CableGeometry {

    private static final List<Direction6> CANONICAL_AXES = List.of(
            Direction6.DOWN,
            Direction6.NORTH,
            Direction6.WEST
    );
    private static final Map<CoreType, List<Quad>> CORES = buildCores();
    private static final Map<ArmKey, List<Quad>> ARMS = buildArms();
    private static final Map<AttachmentArmKey, List<Quad>> ATTACHMENT_ARMS =
            buildAttachmentArms();
    private static final Map<ConstrainedArmKey, List<Quad>> CONSTRAINED_ARMS =
            buildConstrainedArms();
    private static final Map<CollarKey, List<Quad>> COLLARS = buildCollars();
    private static final Map<StraightKey, List<Quad>> STRAIGHTS = buildStraights();
    private static final List<String> M0_SIGNATURES = buildM0Signatures();
    private static final String M0_CATALOG_SIGNATURE = buildM0CatalogSignature();

    private CableGeometry() {
    }

    public static List<Quad> forSnapshot(CableBusSnapshot snapshot) {
        Objects.requireNonNull(snapshot, "snapshot");
        Direction6 straight = straightDirection(snapshot);
        if (straight != null) {
            return STRAIGHTS.get(new StraightKey(snapshot.cable().family(), straight));
        }

        List<Quad> quads = new ArrayList<>();
        quads.addAll(CORES.get(CoreType.forFamily(snapshot.cable().family())));
        for (Direction6 direction : snapshot.faceParts().keySet()) {
            quads.addAll(ATTACHMENT_ARMS.get(new AttachmentArmKey(
                    snapshot.cable().family(),
                    direction
            )));
        }
        for (Direction6 direction : Direction6.values()) {
            CableFamily effectiveType = snapshot.connectionType(direction);
            if (effectiveType == null) {
                continue;
            }
            CableFamily visibleFamily = visibleArmFamily(
                    snapshot.cable().family(),
                    effectiveType
            );
            quads.addAll(ARMS.get(new ArmKey(visibleFamily, direction)));
        }
        return List.copyOf(quads);
    }

    /** Geometry for the independent native structural route. */
    public static List<Quad> forNativeSnapshot(NativeStructuralSnapshot snapshot) {
        Objects.requireNonNull(snapshot, "snapshot");
        if (!snapshot.hasCenter()) {
            return List.of();
        }

        Direction6 straight = nativeStraightDirection(snapshot);
        if (straight != null) {
            return STRAIGHTS.get(new StraightKey(snapshot.cable().family(), straight));
        }

        CableFamily localFamily = snapshot.cable().family();
        List<Quad> quads = new ArrayList<>();
        CoreType coreType = localFamily == CableFamily.GLASS
                && snapshot.faceParts().values().stream()
                .anyMatch(part -> NativeStructuralPartCatalog.require(part.id())
                        .requestsSmartCore())
                ? CoreType.COVERED : CoreType.forFamily(localFamily);
        quads.addAll(CORES.get(coreType));

        if (!localFamily.isDense()) {
            for (Map.Entry<Direction6, FacePartSnapshot> entry
                    : snapshot.faceParts().entrySet()) {
                NativeStructuralPartCatalog.Definition definition =
                        NativeStructuralPartCatalog.require(entry.getValue().id());
                if (definition.kind() == NativeStructuralPartCatalog.Kind.ANCHOR) {
                    continue;
                }
                int distance = definition.cableConnectionLength();
                int coreReach = localFamily == CableFamily.GLASS ? 6 : 5;
                if (distance >= coreReach) {
                    continue;
                }
                List<Quad> constrained = CONSTRAINED_ARMS.get(new ConstrainedArmKey(
                        localFamily,
                        entry.getKey(),
                        distance
                ));
                if (constrained == null) {
                    throw new IllegalStateException("missing constrained cable template");
                }
                quads.addAll(constrained);
            }
        }

        for (Map.Entry<Direction6, NativeStructuralSnapshot.Connection> entry
                : snapshot.connections().entrySet()) {
            NativeStructuralSnapshot.Connection connection = entry.getValue();
            CableFamily visibleFamily = visibleArmFamily(
                    localFamily,
                    connection.effectiveFamily()
            );
            if (connection.collar()) {
                CableFamily collarFamily = visibleFamily == CableFamily.GLASS
                        ? CableFamily.COVERED : visibleFamily;
                List<Quad> collar = COLLARS.get(new CollarKey(
                        collarFamily,
                        entry.getKey()
                ));
                if (collar == null) {
                    throw new IllegalStateException("missing native endpoint collar template");
                }
                quads.addAll(collar);
            }
            quads.addAll(ARMS.get(new ArmKey(visibleFamily, entry.getKey())));
        }
        return List.copyOf(quads);
    }

    /** M0 regression helper: a fluix glass cable with same-family connections. */
    public static List<Quad> forMask(int connectionMask) {
        CableBusSnapshot.validateMask(connectionMask);
        return forSnapshot(
                CableBusSnapshot.fluixGlassCable().withConnectionMask(connectionMask)
        );
    }

    public static String signatureForMask(int connectionMask) {
        CableBusSnapshot.validateMask(connectionMask);
        return M0_SIGNATURES.get(connectionMask);
    }

    public static String catalogSignature() {
        return M0_CATALOG_SIGNATURE;
    }

    public static TemplateStats templateStats() {
        int lists = CORES.size() + ARMS.size() + ATTACHMENT_ARMS.size()
                + STRAIGHTS.size();
        int quads = CORES.values().stream().mapToInt(List::size).sum()
                + ARMS.values().stream().mapToInt(List::size).sum()
                + ATTACHMENT_ARMS.values().stream().mapToInt(List::size).sum()
                + STRAIGHTS.values().stream().mapToInt(List::size).sum();
        int vertices = CORES.values().stream().flatMap(List::stream)
                .mapToInt(quad -> quad.vertices().size()).sum()
                + ARMS.values().stream().flatMap(List::stream)
                .mapToInt(quad -> quad.vertices().size()).sum()
                + ATTACHMENT_ARMS.values().stream().flatMap(List::stream)
                .mapToInt(quad -> quad.vertices().size()).sum()
                + STRAIGHTS.values().stream().flatMap(List::stream)
                .mapToInt(quad -> quad.vertices().size()).sum();
        return new TemplateStats(lists, quads, vertices);
    }

    static CableFamily visibleArmFamily(
            CableFamily localFamily,
            CableFamily effectiveType
    ) {
        Objects.requireNonNull(localFamily, "localFamily");
        Objects.requireNonNull(effectiveType, "effectiveType");
        return switch (localFamily) {
            case GLASS -> CableFamily.GLASS;
            case COVERED -> CableFamily.COVERED;
            case SMART -> effectiveType == CableFamily.SMART
                    ? CableFamily.SMART : CableFamily.COVERED;
            case DENSE_COVERED -> effectiveType == CableFamily.DENSE_COVERED
                    ? CableFamily.DENSE_COVERED : CableFamily.COVERED;
            case DENSE_SMART -> switch (effectiveType) {
                case GLASS, COVERED -> CableFamily.COVERED;
                case SMART -> CableFamily.SMART;
                case DENSE_COVERED -> CableFamily.DENSE_COVERED;
                case DENSE_SMART -> CableFamily.DENSE_SMART;
            };
        };
    }

    private static Direction6 straightDirection(CableBusSnapshot snapshot) {
        if (!snapshot.faceParts().isEmpty()) {
            return null;
        }
        if (snapshot.connectionTypes().size() != 2) {
            return null;
        }
        CableFamily localFamily = snapshot.cable().family();
        for (Direction6 direction : Direction6.values()) {
            if (snapshot.connectionType(direction) != localFamily
                    || snapshot.connectionType(direction.opposite()) != localFamily) {
                continue;
            }
            if (snapshot.connectionMask()
                    == (direction.maskBit() | direction.opposite().maskBit())) {
                return direction;
            }
        }
        return null;
    }

    private static Direction6 nativeStraightDirection(
            NativeStructuralSnapshot snapshot
    ) {
        if (snapshot.connections().size() != 2
                || snapshot.faceParts().values().stream().anyMatch(part ->
                        NativeStructuralPartCatalog.require(part.id()).kind()
                                != NativeStructuralPartCatalog.Kind.ANCHOR)) {
            return null;
        }
        CableFamily localFamily = snapshot.cable().family();
        for (Direction6 direction : CANONICAL_AXES) {
            NativeStructuralSnapshot.Connection first = snapshot.connections().get(direction);
            NativeStructuralSnapshot.Connection second = snapshot.connections().get(
                    direction.opposite()
            );
            if (first != null && second != null
                    && first.effectiveFamily() == localFamily
                    && second.effectiveFamily() == localFamily) {
                return direction;
            }
        }
        return null;
    }

    private static Map<CoreType, List<Quad>> buildCores() {
        EnumMap<CoreType, List<Quad>> cores = new EnumMap<>(CoreType.class);
        for (CoreType coreType : CoreType.values()) {
            Builder builder = new Builder(TextureRole.CORE, null);
            switch (coreType) {
                case GLASS -> builder.addCube(6, 6, 6, 10, 10, 10);
                case COVERED -> builder.addCube(5, 5, 5, 11, 11, 11);
                case DENSE -> builder.addCube(3, 3, 3, 13, 13, 13);
            }
            cores.put(coreType, builder.output());
        }
        return Map.copyOf(cores);
    }

    private static Map<ArmKey, List<Quad>> buildArms() {
        Map<ArmKey, List<Quad>> arms = new LinkedHashMap<>();
        for (CableFamily visibleFamily : CableFamily.values()) {
            for (Direction6 direction : Direction6.values()) {
                arms.put(
                        new ArmKey(visibleFamily, direction),
                        buildArm(visibleFamily, direction)
                );
            }
        }
        return Map.copyOf(arms);
    }

    private static List<Quad> buildArm(
            CableFamily visibleFamily,
            Direction6 direction
    ) {
        Builder builder = new Builder(TextureRole.CONNECTION, visibleFamily);
        builder.setDrawFaces(
                EnumSet.complementOf(EnumSet.of(direction))
        );
        configureArmOrientation(builder, visibleFamily, direction);
        addArmCube(builder, visibleFamily, direction);

        if (visibleFamily.isSmart()) {
            builder.setTextureRole(TextureRole.SMART_CHANNELS_ODD);
            builder.setTintRole(TintRole.DARK);
            builder.setEmissive(true);
            addArmCube(builder, visibleFamily, direction);

            builder.setTextureRole(TextureRole.SMART_CHANNELS_EVEN);
            builder.setTintRole(TintRole.BRIGHT);
            addArmCube(builder, visibleFamily, direction);
        }
        return builder.output();
    }

    private static void configureArmOrientation(
            Builder builder,
            CableFamily family,
            Direction6 direction
    ) {
        if (family == CableFamily.SMART) {
            switch (direction) {
                case DOWN -> {
                    builder.setFlipU(Direction6.EAST, true);
                    builder.setFlipU(Direction6.NORTH, true);
                }
                case UP -> {
                    builder.setFlipU(Direction6.EAST, true);
                    builder.setFlipU(Direction6.NORTH, true);
                    builder.setFlipV(Direction6.DOWN, true);
                }
                case SOUTH -> builder.setFlipU(Direction6.NORTH, true);
                case WEST -> {
                    builder.setFlipV(Direction6.DOWN, true);
                    builder.setFlipU(Direction6.EAST, true);
                }
                case EAST -> builder.setFlipV(Direction6.DOWN, true);
                case NORTH -> {
                    // Default orientation.
                }
            }
        } else if (family == CableFamily.DENSE_SMART) {
            switch (direction) {
                case WEST, EAST -> builder.setFlipV(Direction6.DOWN, true);
                case DOWN, UP -> {
                    builder.setFlipU(Direction6.NORTH, true);
                    builder.setFlipU(Direction6.EAST, true);
                }
                case NORTH, SOUTH -> {
                    // Default orientation.
                }
            }
        }
    }

    private static void addArmCube(
            Builder builder,
            CableFamily visibleFamily,
            Direction6 direction
    ) {
        if (visibleFamily == CableFamily.GLASS) {
            addDirectionalCube(builder, direction, 6, 10, 6, 10);
        } else if (visibleFamily.isDense()) {
            addDirectionalCube(builder, direction, 4, 12, 5, 11);
        } else {
            addDirectionalCube(builder, direction, 6, 10, 5, 11);
        }
    }

    private static void addDirectionalCube(
            Builder builder,
            Direction6 direction,
            double crossMin,
            double crossMax,
            double negativeJoin,
            double positiveJoin
    ) {
        switch (direction) {
            case DOWN -> builder.addCube(
                    crossMin, 0, crossMin,
                    crossMax, negativeJoin, crossMax
            );
            case UP -> builder.addCube(
                    crossMin, positiveJoin, crossMin,
                    crossMax, 16, crossMax
            );
            case NORTH -> builder.addCube(
                    crossMin, crossMin, 0,
                    crossMax, crossMax, negativeJoin
            );
            case SOUTH -> builder.addCube(
                    crossMin, crossMin, positiveJoin,
                    crossMax, crossMax, 16
            );
            case WEST -> builder.addCube(
                    0, crossMin, crossMin,
                    negativeJoin, crossMax, crossMax
            );
            case EAST -> builder.addCube(
                    positiveJoin, crossMin, crossMin,
                    16, crossMax, crossMax
            );
        }
    }

    private static Map<AttachmentArmKey, List<Quad>> buildAttachmentArms() {
        Map<AttachmentArmKey, List<Quad>> arms = new LinkedHashMap<>();
        for (CableFamily family : List.of(
                CableFamily.GLASS,
                CableFamily.COVERED,
                CableFamily.SMART
        )) {
            for (Direction6 direction : Direction6.values()) {
                arms.put(
                        new AttachmentArmKey(family, direction),
                        buildAttachmentArm(family, direction)
                );
            }
        }
        return Map.copyOf(arms);
    }

    private static Map<ConstrainedArmKey, List<Quad>> buildConstrainedArms() {
        Map<ConstrainedArmKey, List<Quad>> arms = new LinkedHashMap<>();
        for (CableFamily family : List.of(
                CableFamily.GLASS,
                CableFamily.COVERED,
                CableFamily.SMART
        )) {
            int coreReach = family == CableFamily.GLASS ? 6 : 5;
            for (Direction6 direction : Direction6.values()) {
                for (int distance = 0; distance < coreReach; distance++) {
                    arms.put(
                            new ConstrainedArmKey(family, direction, distance),
                            buildConstrainedArm(family, direction, distance)
                    );
                }
            }
        }
        return Map.copyOf(arms);
    }

    private static List<Quad> buildConstrainedArm(
            CableFamily family,
            Direction6 direction,
            int distance
    ) {
        Builder builder = new Builder(TextureRole.CONNECTION, family);
        configureAttachmentOrientation(builder, family, direction);
        addConstrainedCube(builder, family, direction, distance);
        if (family == CableFamily.SMART) {
            builder.setTextureRole(TextureRole.SMART_CHANNELS_ODD);
            builder.setTintRole(TintRole.DARK);
            builder.setEmissive(true);
            addConstrainedCube(builder, family, direction, distance);

            builder.setTextureRole(TextureRole.SMART_CHANNELS_EVEN);
            builder.setTintRole(TintRole.BRIGHT);
            addConstrainedCube(builder, family, direction, distance);
        }
        return builder.output();
    }

    private static void addConstrainedCube(
            Builder builder,
            CableFamily family,
            Direction6 direction,
            int distance
    ) {
        double coreMin = family == CableFamily.GLASS ? 6 : 5;
        double coreMax = family == CableFamily.GLASS ? 10 : 11;
        addDirectionalSegment(
                builder,
                direction,
                6,
                10,
                distance,
                coreMin,
                coreMax,
                16 - distance
        );
    }

    private static Map<CollarKey, List<Quad>> buildCollars() {
        Map<CollarKey, List<Quad>> collars = new LinkedHashMap<>();
        for (CableFamily family : List.of(CableFamily.COVERED, CableFamily.SMART)) {
            for (Direction6 direction : Direction6.values()) {
                collars.put(
                        new CollarKey(family, direction),
                        buildCollar(family, direction)
                );
            }
        }
        return Map.copyOf(collars);
    }

    private static List<Quad> buildCollar(
            CableFamily family,
            Direction6 direction
    ) {
        Builder builder = new Builder(TextureRole.CONNECTION, family);
        builder.setDrawFaces(EnumSet.complementOf(EnumSet.of(direction)));
        configureAttachmentOrientation(builder, family, direction);
        addDirectionalSegment(builder, direction, 5, 11, 0, 4, 12, 16);
        if (family == CableFamily.SMART) {
            builder.setTextureRole(TextureRole.SMART_CHANNELS_ODD);
            builder.setTintRole(TintRole.DARK);
            builder.setEmissive(true);
            addDirectionalSegment(builder, direction, 5, 11, 0, 4, 12, 16);

            builder.setTextureRole(TextureRole.SMART_CHANNELS_EVEN);
            builder.setTintRole(TintRole.BRIGHT);
            addDirectionalSegment(builder, direction, 5, 11, 0, 4, 12, 16);
        }
        return builder.output();
    }

    private static List<Quad> buildAttachmentArm(
            CableFamily family,
            Direction6 direction
    ) {
        Builder builder = new Builder(TextureRole.CONNECTION, family);
        configureAttachmentOrientation(builder, family, direction);
        addAttachmentCube(builder, family, direction);
        if (family == CableFamily.SMART) {
            builder.setTextureRole(TextureRole.SMART_CHANNELS_ODD);
            builder.setTintRole(TintRole.DARK);
            builder.setEmissive(true);
            addAttachmentCube(builder, family, direction);

            builder.setTextureRole(TextureRole.SMART_CHANNELS_EVEN);
            builder.setTintRole(TintRole.BRIGHT);
            addAttachmentCube(builder, family, direction);
        }
        return builder.output();
    }

    private static void configureAttachmentOrientation(
            Builder builder,
            CableFamily family,
            Direction6 direction
    ) {
        if (family != CableFamily.SMART) {
            return;
        }
        switch (direction) {
            case DOWN, UP -> {
                builder.setFlipU(Direction6.EAST, true);
                builder.setFlipU(Direction6.NORTH, true);
            }
            case WEST, EAST -> builder.setFlipV(Direction6.DOWN, true);
            case NORTH, SOUTH -> {
                // Default orientation.
            }
        }
    }

    private static void addAttachmentCube(
            Builder builder,
            CableFamily family,
            Direction6 direction
    ) {
        double coreMin = family == CableFamily.GLASS ? 6 : 5;
        double coreMax = family == CableFamily.GLASS ? 10 : 11;
        addDirectionalSegment(
                builder,
                direction,
                6,
                10,
                3,
                coreMin,
                coreMax,
                13
        );
    }

    private static void addDirectionalSegment(
            Builder builder,
            Direction6 direction,
            double crossMin,
            double crossMax,
            double negativeMin,
            double negativeMax,
            double positiveMin,
            double positiveMax
    ) {
        switch (direction) {
            case DOWN -> builder.addCube(
                    crossMin, negativeMin, crossMin,
                    crossMax, negativeMax, crossMax
            );
            case UP -> builder.addCube(
                    crossMin, positiveMin, crossMin,
                    crossMax, positiveMax, crossMax
            );
            case NORTH -> builder.addCube(
                    crossMin, crossMin, negativeMin,
                    crossMax, crossMax, negativeMax
            );
            case SOUTH -> builder.addCube(
                    crossMin, crossMin, positiveMin,
                    crossMax, crossMax, positiveMax
            );
            case WEST -> builder.addCube(
                    negativeMin, crossMin, crossMin,
                    negativeMax, crossMax, crossMax
            );
            case EAST -> builder.addCube(
                    positiveMin, crossMin, crossMin,
                    positiveMax, crossMax, crossMax
            );
        }
    }

    private static Map<StraightKey, List<Quad>> buildStraights() {
        Map<StraightKey, List<Quad>> straights = new LinkedHashMap<>();
        for (CableFamily family : CableFamily.values()) {
            for (Direction6 direction : CANONICAL_AXES) {
                straights.put(
                        new StraightKey(family, direction),
                        buildStraight(family, direction)
                );
            }
        }
        return Map.copyOf(straights);
    }

    private static List<Quad> buildStraight(
            CableFamily family,
            Direction6 direction
    ) {
        Builder builder = new Builder(TextureRole.CONNECTION, family);
        configureStraightOrientation(builder, family, direction);
        addStraightCube(builder, family, direction);

        if (family.isSmart()) {
            builder.setTextureRole(TextureRole.SMART_CHANNELS_ODD);
            builder.setTintRole(TintRole.DARK);
            builder.setEmissive(true);
            addStraightCube(builder, family, direction);

            builder.setTextureRole(TextureRole.SMART_CHANNELS_EVEN);
            builder.setTintRole(TintRole.BRIGHT);
            addStraightCube(builder, family, direction);
        }
        return builder.output();
    }

    private static void configureStraightOrientation(
            Builder builder,
            CableFamily family,
            Direction6 direction
    ) {
        if (family == CableFamily.SMART) {
            switch (direction) {
                case EAST, WEST -> builder.setFlipV(Direction6.DOWN, true);
                case UP, DOWN -> builder.setFlipU(Direction6.NORTH, true);
                case NORTH, SOUTH -> {
                    // Default orientation.
                }
            }
        } else if (family == CableFamily.DENSE_SMART) {
            switch (direction) {
                case NORTH -> builder.setFlipU(Direction6.NORTH, true);
                case WEST -> {
                    builder.setFlipV(Direction6.DOWN, true);
                    builder.setFlipU(Direction6.EAST, true);
                }
                case DOWN -> {
                    builder.setFlipU(Direction6.NORTH, true);
                    builder.setFlipV(Direction6.DOWN, true);
                }
                case UP, SOUTH, EAST -> {
                    // Default orientation.
                }
            }
        }

        if (family != CableFamily.GLASS) {
            double min = family.isDense() ? 3 : 5;
            double max = family.isDense() ? 13 : 11;
            setStraightUvs(builder, direction, min, max);
        }
    }

    private static void setStraightUvs(
            Builder builder,
            Direction6 direction,
            double min,
            double max
    ) {
        switch (direction) {
            case DOWN, UP -> {
                builder.setCustomUv(Direction6.NORTH, min, 0, max, min);
                builder.setCustomUv(Direction6.EAST, min, 0, max, min);
                builder.setCustomUv(Direction6.SOUTH, min, 0, max, min);
                builder.setCustomUv(Direction6.WEST, min, 0, max, min);
            }
            case EAST, WEST -> {
                builder.setCustomUv(Direction6.UP, 0, min, min, max);
                builder.setCustomUv(Direction6.DOWN, 0, min, min, max);
                builder.setCustomUv(Direction6.NORTH, 0, min, min, max);
                builder.setCustomUv(Direction6.SOUTH, 0, min, min, max);
            }
            case NORTH, SOUTH -> {
                builder.setCustomUv(Direction6.UP, min, 0, max, min);
                builder.setCustomUv(Direction6.DOWN, min, 0, max, min);
                builder.setCustomUv(Direction6.EAST, 0, min, min, max);
                builder.setCustomUv(Direction6.WEST, 0, min, min, max);
            }
        }
    }

    private static void addStraightCube(
            Builder builder,
            CableFamily family,
            Direction6 direction
    ) {
        if (family == CableFamily.GLASS) {
            builder.setDrawFaces(EnumSet.complementOf(
                    EnumSet.of(direction, direction.opposite())
            ));
            addAxisCube(builder, direction, 6, 10, 0, 16);
            return;
        }

        builder.setDrawFaces(EnumSet.allOf(Direction6.class));
        if (family.isDense()) {
            withStraightRotations(builder, direction, () ->
                    addAxisCube(builder, direction, 3, 13, -0.01, 16.01)
            );
        } else {
            withStraightRotations(builder, direction, () ->
                    addAxisCube(builder, direction, 5, 11, 0, 16)
            );
        }
    }

    private static void withStraightRotations(
            Builder builder,
            Direction6 direction,
            Runnable addCube
    ) {
        switch (direction) {
            case DOWN, UP -> builder.setUvRotation(Direction6.EAST, 2);
            case EAST, WEST -> {
                builder.setUvRotation(Direction6.SOUTH, 2);
                builder.setUvRotation(Direction6.NORTH, 2);
            }
            case NORTH, SOUTH -> {
                builder.setUvRotation(Direction6.EAST, 2);
                builder.setUvRotation(Direction6.WEST, 2);
            }
        }
        addCube.run();
        for (Direction6 face : Direction6.values()) {
            builder.setUvRotation(face, 0);
        }
    }

    private static void addAxisCube(
            Builder builder,
            Direction6 direction,
            double crossMin,
            double crossMax,
            double axisMin,
            double axisMax
    ) {
        switch (direction) {
            case DOWN, UP -> builder.addCube(
                    crossMin, axisMin, crossMin,
                    crossMax, axisMax, crossMax
            );
            case NORTH, SOUTH -> builder.addCube(
                    crossMin, crossMin, axisMin,
                    crossMax, crossMax, axisMax
            );
            case WEST, EAST -> builder.addCube(
                    axisMin, crossMin, crossMin,
                    axisMax, crossMax, crossMax
            );
        }
    }

    private static List<String> buildM0Signatures() {
        List<String> signatures = new ArrayList<>(64);
        for (int mask = 0; mask < 64; mask++) {
            signatures.add(signature(mask, forMask(mask)));
        }
        return List.copyOf(signatures);
    }

    private static String signature(int mask, List<Quad> quads) {
        StringBuilder canonical = new StringBuilder();
        canonical.append(mask).append('\n');
        for (Quad quad : quads) {
            canonical.append(quad.face().name())
                    .append('|')
                    .append(quad.textureRole().name())
                    .append('\n');
            for (Vertex vertex : quad.vertices()) {
                canonical.append(number(vertex.x16())).append('|')
                        .append(number(vertex.y16())).append('|')
                        .append(number(vertex.z16())).append('|')
                        .append(number(vertex.u16())).append('|')
                        .append(number(vertex.v16())).append('\n');
            }
        }
        return sha256(canonical.toString());
    }

    private static String buildM0CatalogSignature() {
        StringBuilder canonical = new StringBuilder();
        for (int mask = 0; mask < M0_SIGNATURES.size(); mask++) {
            canonical.append(mask)
                    .append(':')
                    .append(M0_SIGNATURES.get(mask))
                    .append('\n');
        }
        return sha256(canonical.toString());
    }

    private static String number(double value) {
        return BigDecimal.valueOf(value).stripTrailingZeros().toPlainString();
    }

    private static String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(
                    digest.digest(value.getBytes(StandardCharsets.UTF_8))
            );
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }

    public enum TextureRole {
        CORE,
        CONNECTION,
        SMART_CHANNELS_ODD,
        SMART_CHANNELS_EVEN
    }

    public enum TintRole {
        WHITE,
        DARK,
        BRIGHT
    }

    /** Exact vertex and UV coordinates in sixteenths of a block or texture. */
    public record Vertex(double x16, double y16, double z16, double u16, double v16) {

        public Vertex {
            requireCoordinate("x16", x16);
            requireCoordinate("y16", y16);
            requireCoordinate("z16", z16);
            requireUv("u16", u16);
            requireUv("v16", v16);
        }

        private static void requireCoordinate(String name, double value) {
            if (!Double.isFinite(value) || value < -0.01 || value > 16.01) {
                throw new IllegalArgumentException(name + " must be in [-0.01, 16.01]");
            }
        }

        private static void requireUv(String name, double value) {
            if (!Double.isFinite(value) || value < 0 || value > 16) {
                throw new IllegalArgumentException(name + " must be in [0, 16]");
            }
        }
    }

    public record Quad(
            Direction6 face,
            TextureRole textureRole,
            CableFamily materialFamily,
            TintRole tintRole,
            boolean emissive,
            List<Vertex> vertices
    ) {

        public Quad {
            Objects.requireNonNull(face, "face");
            Objects.requireNonNull(textureRole, "textureRole");
            Objects.requireNonNull(tintRole, "tintRole");
            vertices = List.copyOf(Objects.requireNonNull(vertices, "vertices"));
            if (textureRole == TextureRole.CORE && materialFamily != null) {
                throw new IllegalArgumentException("core quads have no material family");
            }
            if (textureRole != TextureRole.CORE && materialFamily == null) {
                throw new IllegalArgumentException("non-core quads need a material family");
            }
            if (vertices.size() != 4) {
                throw new IllegalArgumentException("a quad must contain exactly four vertices");
            }
        }
    }

    public record TemplateStats(int lists, int quads, int vertices) {
    }

    private record ArmKey(CableFamily visibleFamily, Direction6 direction) {
    }

    private record AttachmentArmKey(CableFamily family, Direction6 direction) {
    }

    private record ConstrainedArmKey(
            CableFamily family,
            Direction6 direction,
            int distance
    ) {
    }

    private record CollarKey(CableFamily family, Direction6 direction) {
    }

    private record StraightKey(CableFamily family, Direction6 direction) {
    }

    private enum CoreType {
        GLASS,
        COVERED,
        DENSE;

        private static CoreType forFamily(CableFamily family) {
            return switch (family) {
                case GLASS -> GLASS;
                case COVERED, SMART -> COVERED;
                case DENSE_COVERED, DENSE_SMART -> DENSE;
            };
        }
    }

    private static final class Builder {

        private final List<Quad> output = new ArrayList<>();
        private final EnumMap<Direction6, UvRect> customUvs =
                new EnumMap<>(Direction6.class);
        private final EnumMap<Direction6, Integer> rotations =
                new EnumMap<>(Direction6.class);
        private final EnumSet<Direction6> flipU = EnumSet.noneOf(Direction6.class);
        private final EnumSet<Direction6> flipV = EnumSet.noneOf(Direction6.class);
        private EnumSet<Direction6> drawFaces = EnumSet.allOf(Direction6.class);
        private TextureRole textureRole;
        private final CableFamily materialFamily;
        private TintRole tintRole = TintRole.WHITE;
        private boolean emissive;

        private Builder(TextureRole textureRole, CableFamily materialFamily) {
            this.textureRole = Objects.requireNonNull(textureRole, "textureRole");
            this.materialFamily = materialFamily;
        }

        private void setDrawFaces(EnumSet<Direction6> faces) {
            drawFaces = EnumSet.copyOf(faces);
        }

        private void setTextureRole(TextureRole role) {
            textureRole = Objects.requireNonNull(role, "role");
        }

        private void setTintRole(TintRole role) {
            tintRole = Objects.requireNonNull(role, "role");
        }

        private void setEmissive(boolean value) {
            emissive = value;
        }

        private void setFlipU(Direction6 face, boolean value) {
            setFlag(flipU, face, value);
        }

        private void setFlipV(Direction6 face, boolean value) {
            setFlag(flipV, face, value);
        }

        private static void setFlag(
                EnumSet<Direction6> flags,
                Direction6 face,
                boolean value
        ) {
            if (value) {
                flags.add(face);
            } else {
                flags.remove(face);
            }
        }

        private void setCustomUv(
                Direction6 face,
                double u1,
                double v1,
                double u2,
                double v2
        ) {
            customUvs.put(face, new UvRect(u1, v1, u2, v2));
        }

        private void setUvRotation(Direction6 face, int rotation) {
            if (rotation < 0 || rotation > 3) {
                throw new IllegalArgumentException("UV rotation must be in [0, 3]");
            }
            rotations.put(face, rotation);
        }

        private void addCube(
                double x1,
                double y1,
                double z1,
                double x2,
                double y2,
                double z2
        ) {
            for (Direction6 face : Direction6.values()) {
                if (drawFaces.contains(face)) {
                    output.add(quad(face, x1, y1, z1, x2, y2, z2));
                }
            }
        }

        private Quad quad(
                Direction6 face,
                double x1,
                double y1,
                double z1,
                double x2,
                double y2,
                double z2
        ) {
            UvRect uv = customUvs.get(face);
            if (uv == null) {
                uv = standardUv(face, x1, y1, z1, x2, y2, z2);
            }
            if (flipU.contains(face)) {
                uv = new UvRect(uv.u2(), uv.v1(), uv.u1(), uv.v2());
            }
            if (flipV.contains(face)) {
                uv = new UvRect(uv.u1(), uv.v2(), uv.u2(), uv.v1());
            }

            List<Position> positions = positions(face, x1, y1, z1, x2, y2, z2);
            UvPoint[] points = uvPoints(face, uv, rotations.getOrDefault(face, 0));
            List<Vertex> vertices = new ArrayList<>(4);
            for (int index = 0; index < 4; index++) {
                Position position = positions.get(index);
                vertices.add(new Vertex(
                        position.x(),
                        position.y(),
                        position.z(),
                        points[index].u(),
                        points[index].v()
                ));
            }
            return new Quad(
                    face,
                    textureRole,
                    materialFamily,
                    tintRole,
                    emissive,
                    vertices
            );
        }

        private List<Quad> output() {
            return List.copyOf(output);
        }
    }

    private static UvRect standardUv(
            Direction6 face,
            double x1,
            double y1,
            double z1,
            double x2,
            double y2,
            double z2
    ) {
        double v1;
        double v2;
        if (face == Direction6.DOWN || face == Direction6.UP) {
            v1 = z1;
            v2 = z2;
        } else {
            v1 = 16 - y1;
            v2 = 16 - y2;
        }
        return switch (face) {
            case DOWN, UP, SOUTH -> new UvRect(x1, v1, x2, v2);
            case NORTH -> new UvRect(16 - x2, v1, 16 - x1, v2);
            case WEST -> new UvRect(z1, v1, z2, v2);
            case EAST -> new UvRect(16 - z2, v1, 16 - z1, v2);
        };
    }

    private static List<Position> positions(
            Direction6 face,
            double x1,
            double y1,
            double z1,
            double x2,
            double y2,
            double z2
    ) {
        return switch (face) {
            case DOWN -> List.of(
                    new Position(x1, y1, z2),
                    new Position(x1, y1, z1),
                    new Position(x2, y1, z1),
                    new Position(x2, y1, z2)
            );
            case UP -> List.of(
                    new Position(x1, y2, z1),
                    new Position(x1, y2, z2),
                    new Position(x2, y2, z2),
                    new Position(x2, y2, z1)
            );
            case NORTH -> List.of(
                    new Position(x2, y2, z1),
                    new Position(x2, y1, z1),
                    new Position(x1, y1, z1),
                    new Position(x1, y2, z1)
            );
            case SOUTH -> List.of(
                    new Position(x1, y2, z2),
                    new Position(x1, y1, z2),
                    new Position(x2, y1, z2),
                    new Position(x2, y2, z2)
            );
            case WEST -> List.of(
                    new Position(x1, y2, z1),
                    new Position(x1, y1, z1),
                    new Position(x1, y1, z2),
                    new Position(x1, y2, z2)
            );
            case EAST -> List.of(
                    new Position(x2, y2, z2),
                    new Position(x2, y1, z2),
                    new Position(x2, y1, z1),
                    new Position(x2, y2, z1)
            );
        };
    }

    private static UvPoint[] uvPoints(Direction6 face, UvRect uv, int rotation) {
        UvPoint[] source;
        if (face == Direction6.DOWN || face == Direction6.UP) {
            source = new UvPoint[]{
                    new UvPoint(uv.u1(), uv.v1()),
                    new UvPoint(uv.u1(), uv.v2()),
                    new UvPoint(uv.u2(), uv.v2()),
                    new UvPoint(uv.u2(), uv.v1())
            };
        } else {
            source = new UvPoint[]{
                    new UvPoint(uv.u1(), uv.v2()),
                    new UvPoint(uv.u1(), uv.v1()),
                    new UvPoint(uv.u2(), uv.v1()),
                    new UvPoint(uv.u2(), uv.v2())
            };
        }
        UvPoint[] rotated = new UvPoint[4];
        for (int index = 0; index < source.length; index++) {
            rotated[Math.floorMod(index - rotation, 4)] = source[index];
        }
        return rotated;
    }

    private record UvRect(double u1, double v1, double u2, double v2) {
    }

    private record UvPoint(double u, double v) {
    }

    private record Position(double x, double y, double z) {
    }
}
