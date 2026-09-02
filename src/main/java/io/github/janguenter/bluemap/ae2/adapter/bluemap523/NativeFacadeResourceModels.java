/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.adapter.bluemap523;

import com.flowpowered.math.vector.Vector3f;
import com.flowpowered.math.vector.Vector4f;
import de.bluecolored.bluemap.core.map.hires.block.BlockRendererType;
import de.bluecolored.bluemap.core.resources.ResourcePath;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.Multipart;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.Variant;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.VariantSet;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.Variants;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.model.Element;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.model.Face;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.model.Model;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.model.Rotation;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.texture.Texture;
import de.bluecolored.bluemap.core.util.Direction;
import de.bluecolored.bluemap.core.util.Key;
import de.bluecolored.bluemap.core.util.math.VectorM3f;
import de.bluecolored.bluemap.core.world.BlockState;
import io.github.janguenter.bluemap.ae2.model.CableGeometry;
import io.github.janguenter.bluemap.ae2.model.Direction6;
import io.github.janguenter.bluemap.ae2.model.FacadeSnapshot;
import io.github.janguenter.bluemap.ae2.model.QuartzGlassGeometry;
import io.github.janguenter.bluemap.ae2.model.QuartzGlassSnapshot;
import io.github.janguenter.bluemap.ae2.profile.Ae219217NativeStructuralProfile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Semantic gate and bounded baked-quad projection for static facade states. */
final class NativeFacadeResourceModels {

    private static final int MAX_STATIC_SELECTIONS = 64;
    private static final int MAX_MODEL_ELEMENTS = 256;
    private static final int MAX_SOURCE_QUADS = 4_096;
    private static final float MAX_COORDINATE_16 = 64F;

    private NativeFacadeResourceModels() {
    }

    static FacadeMaterial resolve(ResourcePack resourcePack, FacadeSnapshot snapshot) {
        return resolve(resourcePack, snapshot, 0, 0, 0);
    }

    static FacadeMaterial resolve(
            ResourcePack resourcePack,
            FacadeSnapshot snapshot,
            int x,
            int y,
            int z
    ) {
        return resolve(resourcePack, snapshot, x, y, z, null);
    }

    static FacadeMaterial resolve(
            ResourcePack resourcePack,
            FacadeSnapshot snapshot,
            int x,
            int y,
            int z,
            QuartzFacadeAppearance quartzAppearance
    ) {
        try {
            return resolveChecked(
                    resourcePack,
                    snapshot,
                    x,
                    y,
                    z,
                    quartzAppearance
            );
        } catch (RuntimeException exception) {
            return null;
        }
    }

    private static FacadeMaterial resolveChecked(
            ResourcePack resourcePack,
            FacadeSnapshot snapshot,
            int x,
            int y,
            int z,
            QuartzFacadeAppearance quartzAppearance
    ) {
        Key blockId = Key.parse(snapshot.blockId());
        Map<String, List<String>> explicitSchema =
                Ae219217NativeStructuralProfile.facadeWhitelistStateSchema(
                        blockId.getFormatted()
                );
        if (explicitSchema != null
                && !matchesExactStateSchema(snapshot.properties(), explicitSchema)) {
            return null;
        }
        if (isNativeQuartzFacade(blockId)) {
            return resolveNativeQuartzFacade(
                    resourcePack,
                    snapshot,
                    x,
                    y,
                    z,
                    quartzAppearance
            );
        }
        FacadeSnapshot projected = neutralNativeProjection(snapshot);
        if (projected == null) {
            return null;
        }
        de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.BlockState resource =
                resourcePack.getBlockStates().get(blockId);
        if (resource == null || !isBoundedStaticResource(resource)) {
            return null;
        }

        BlockState state = new BlockState(blockId, projected.properties());
        if (!weightedSelectionsEquivalent(resourcePack, resource, state, blockId)) {
            return null;
        }
        List<Variant> selected = new ArrayList<>();
        resource.forEach(state, x, y, z, selected::add);
        boolean multipart = resource.getMultipart() != null;
        if (selected.isEmpty() || selected.size() > MAX_STATIC_SELECTIONS
                || (!multipart && selected.size() != 1)) {
            return null;
        }

        List<FacadeLayer> layers = new ArrayList<>();
        boolean fullCubeWitness = false;
        for (Variant variant : selected) {
            Model model = variant.getModel().getResource(resourcePack.getModels()::get);
            if (model == null) {
                return null;
            }
            model.applyParent(resourcePack.getModels());
            Element[] elements = model.getElements();
            if (elements == null || elements.length == 0
                    || elements.length > MAX_MODEL_ELEMENTS) {
                return null;
            }
            model.calculateProperties(resourcePack.getTextures());

            for (Element element : elements) {
                if (!validElement(element)) {
                    return null;
                }
                fullCubeWitness |= isFullCubeWitness(element);
                for (Direction sourceDirection : Direction.values()) {
                    Face sourceFace = element.getFaces().get(sourceDirection);
                    if (sourceFace == null) {
                        continue;
                    }
                    FacadeLayer layer = sourceLayer(
                            resourcePack,
                            model,
                            element,
                            sourceDirection,
                            sourceFace,
                            variant
                    );
                    if (layer == null || layers.size() == MAX_SOURCE_QUADS) {
                        return null;
                    }
                    layers.add(layer);
                }
            }
        }
        boolean explicitlyWhitelisted =
                Ae219217NativeStructuralProfile.facadeWhitelistBlockIds()
                        .contains(blockId.getFormatted());
        long distinctTintIndexes = layers.stream()
                .mapToInt(FacadeLayer::tintIndex)
                .filter(index -> index >= 0)
                .distinct()
                .count();
        if ((!fullCubeWitness && !explicitlyWhitelisted) || layers.isEmpty()
                || distinctTintIndexes > 1) {
            return null;
        }

        Ae219217NativeStructuralProfile.FacadeNeutralState explicitNeutral =
                Ae219217NativeStructuralProfile.facadeWhitelistNeutralStates()
                        .get(blockId.getFormatted());
        // AE2 derives FacadeRenderState#transparent from BlockState#isSolidRender.
        // The explicit whitelist is source-probed and profile-pinned because
        // BlueMap's inferred culling property is not equivalent for materials
        // such as honey and soul sand.  The host property remains the bounded
        // projection for ordinary/tag-provided facade states.
        boolean opaque = explicitNeutral == null
                ? resourcePack.getBlockProperties(state).isCulling()
                : explicitNeutral.solidRender();
        return new FacadeMaterial(state, layers, opaque);
    }

    private static FacadeLayer sourceLayer(
            ResourcePack resourcePack,
            Model model,
            Element element,
            Direction sourceDirection,
            Face sourceFace,
            Variant variant
    ) {
        if (sourceFace.getTintindex() < -1
                || Math.floorMod(sourceFace.getRotation(), 90) != 0) {
            return null;
        }
        ResourcePath<Texture> texturePath = sourceFace.getTexture().getTexturePath(
                model.getTextures()::get
        );
        if (texturePath == null) {
            return null;
        }
        Key textureKey = Key.parse(texturePath.getFormatted());
        if (ResourcePack.MISSING_TEXTURE.equals(textureKey)
                || resourcePack.getTextures().get(textureKey) == null) {
            return null;
        }

        List<CableGeometry.Vertex> vertices = transformedVertices(
                element,
                sourceDirection,
                sourceFace,
                variant
        );
        Direction6 nominalFace = nominalFace(vertices);
        Direction6 cullFace = sourceFace.getCullface() == null
                ? null : transformedDirection(sourceFace.getCullface(), variant);
        Direction6 lightFace = transformedDirection(sourceDirection, variant);
        VectorM3f sourceNormal = transformedSourceNormal(
                sourceDirection,
                element,
                variant
        );
        if (vertices == null || nominalFace == null
                || sourceFace.getCullface() != null && cullFace == null
                || lightFace == null || !Float.isFinite(sourceNormal.y)
                || !interpolatable(vertices, nominalFace)) {
            return null;
        }
        return new FacadeLayer(
                textureKey,
                sourceFace.getTintindex(),
                element.isShade(),
                model.isAmbientocclusion(),
                cullFace,
                nominalFace,
                lightFace,
                sourceNormal.y,
                element.getLightEmission(),
                vertices
        );
    }

    private static boolean validElement(Element element) {
        if (element == null
                || element.getLightEmission() < 0 || element.getLightEmission() > 15
                || !validVector(element.getFrom()) || !validVector(element.getTo())
                || element.getFaces() == null || element.getFaces().isEmpty()
                || element.getFaces().size() > Direction.values().length) {
            return false;
        }
        Rotation rotation = element.getRotation();
        return rotation != null && validVector(rotation.getOrigin())
                && finiteBounded(rotation.getX()) && finiteBounded(rotation.getY())
                && finiteBounded(rotation.getZ()) && finiteBounded(rotation.getAngle());
    }

    private static FacadeSnapshot neutralNativeProjection(FacadeSnapshot snapshot) {
        Ae219217NativeStructuralProfile.NeutralFacadeMaterial neutral =
                Ae219217NativeStructuralProfile.nativeFacadeNeutralMaterials()
                        .get(snapshot.blockId());
        if (neutral == null) {
            return snapshot;
        }
        Map<String, String> properties = snapshot.properties();
        if (!properties.keySet().equals(neutral.validPropertyValues().keySet())) {
            return null;
        }
        for (Map.Entry<String, List<String>> entry
                : neutral.validPropertyValues().entrySet()) {
            if (!entry.getValue().contains(properties.get(entry.getKey()))) {
                return null;
            }
        }
        java.util.LinkedHashMap<String, String> projected =
                new java.util.LinkedHashMap<>();
        neutral.normalization().forEach((key, value) -> projected.put(
                key,
                "preserve".equals(value) ? properties.get(key) : value
        ));
        return new FacadeSnapshot(snapshot.blockId(), projected);
    }

    private static boolean matchesExactStateSchema(
            Map<String, String> properties,
            Map<String, List<String>> schema
    ) {
        if (properties == null || schema == null
                || !properties.keySet().equals(schema.keySet())) {
            return false;
        }
        for (Map.Entry<String, List<String>> entry : schema.entrySet()) {
            if (entry.getValue() == null || entry.getValue().isEmpty()
                    || !entry.getValue().contains(properties.get(entry.getKey()))) {
                return false;
            }
        }
        return true;
    }

    private static boolean validVector(Vector3f vector) {
        return vector != null && finiteBounded(vector.getX())
                && finiteBounded(vector.getY()) && finiteBounded(vector.getZ());
    }

    private static boolean finiteBounded(float value) {
        return Float.isFinite(value) && Math.abs(value) <= MAX_COORDINATE_16;
    }

    private static boolean isFullCubeWitness(Element element) {
        return Vector3f.ZERO.equals(element.getFrom())
                && new Vector3f(16, 16, 16).equals(element.getTo())
                && zeroRotation(element)
                && element.getFaces().size() == Direction.values().length;
    }

    private static List<CableGeometry.Vertex> transformedVertices(
            Element element,
            Direction direction,
            Face face,
            Variant variant
    ) {
        Vector4f uv = face.getUv();
        if (uv == null || !Float.isFinite(uv.getX()) || !Float.isFinite(uv.getY())
                || !Float.isFinite(uv.getZ()) || !Float.isFinite(uv.getW())) {
            return null;
        }
        FacadeUv[] rawUvs = {
                new FacadeUv(uv.getX(), uv.getW()),
                new FacadeUv(uv.getZ(), uv.getW()),
                new FacadeUv(uv.getZ(), uv.getY()),
                new FacadeUv(uv.getX(), uv.getY())
        };
        int rotationSteps = Math.floorMod(Math.floorDiv(face.getRotation(), 90), 4);
        FacadeUv[] uvs = new FacadeUv[4];
        for (int index = 0; index < uvs.length; index++) {
            uvs[index] = rawUvs[(rotationSteps + index) % rawUvs.length];
        }
        if (variant.isUvlock() && variant.isTransformed()) {
            int quarterTurns = uvLockQuarterTurns(direction, variant);
            for (int index = 0; index < uvs.length; index++) {
                FacadeUv point = uvs[index];
                uvs[index] = switch (quarterTurns) {
                    case 0 -> point;
                    case 1 -> new FacadeUv(16D - point.v16(), point.u16());
                    case 2 -> new FacadeUv(16D - point.u16(), 16D - point.v16());
                    case 3 -> new FacadeUv(point.v16(), 16D - point.u16());
                    default -> throw new AssertionError("invalid quarter turn");
                };
            }
        }

        List<Vector3f> positions = sourcePositions(direction, element);
        List<CableGeometry.Vertex> result = new ArrayList<>(4);
        for (int index = 0; index < positions.size(); index++) {
            Vector3f source = positions.get(index);
            VectorM3f point = new VectorM3f(source.getX(), source.getY(), source.getZ());
            point.transform(element.getRotation().getMatrix());
            point.mul(1F / 16F);
            if (variant.isTransformed()) {
                cardinalVariantTransform(variant).transformPoint(point);
            }
            point.mul(16F);
            if (!finiteBounded(point.x) || !finiteBounded(point.y)
                    || !finiteBounded(point.z)) {
                return null;
            }
            result.add(new CableGeometry.Vertex(
                    point.x,
                    point.y,
                    point.z,
                    uvs[index].u16(),
                    uvs[index].v16()
            ));
        }
        return List.copyOf(result);
    }

    private static List<Vector3f> sourcePositions(Direction face, Element element) {
        float minX = element.getFrom().getX();
        float minY = element.getFrom().getY();
        float minZ = element.getFrom().getZ();
        float maxX = element.getTo().getX();
        float maxY = element.getTo().getY();
        float maxZ = element.getTo().getZ();
        return switch (face) {
            case DOWN -> List.of(
                    new Vector3f(minX, minY, minZ),
                    new Vector3f(maxX, minY, minZ),
                    new Vector3f(maxX, minY, maxZ),
                    new Vector3f(minX, minY, maxZ)
            );
            case UP -> List.of(
                    new Vector3f(minX, maxY, maxZ),
                    new Vector3f(maxX, maxY, maxZ),
                    new Vector3f(maxX, maxY, minZ),
                    new Vector3f(minX, maxY, minZ)
            );
            case NORTH -> List.of(
                    new Vector3f(maxX, minY, minZ),
                    new Vector3f(minX, minY, minZ),
                    new Vector3f(minX, maxY, minZ),
                    new Vector3f(maxX, maxY, minZ)
            );
            case SOUTH -> List.of(
                    new Vector3f(minX, minY, maxZ),
                    new Vector3f(maxX, minY, maxZ),
                    new Vector3f(maxX, maxY, maxZ),
                    new Vector3f(minX, maxY, maxZ)
            );
            case WEST -> List.of(
                    new Vector3f(minX, minY, minZ),
                    new Vector3f(minX, minY, maxZ),
                    new Vector3f(minX, maxY, maxZ),
                    new Vector3f(minX, maxY, minZ)
            );
            case EAST -> List.of(
                    new Vector3f(maxX, minY, maxZ),
                    new Vector3f(maxX, minY, minZ),
                    new Vector3f(maxX, maxY, minZ),
                    new Vector3f(maxX, maxY, maxZ)
            );
        };
    }

    private static Direction6 nominalFace(List<CableGeometry.Vertex> vertices) {
        if (vertices == null || vertices.size() != 4) {
            return null;
        }
        CableGeometry.Vertex first = vertices.get(0);
        CableGeometry.Vertex second = vertices.get(1);
        CableGeometry.Vertex third = vertices.get(2);
        double ax = second.x16() - first.x16();
        double ay = second.y16() - first.y16();
        double az = second.z16() - first.z16();
        double bx = third.x16() - second.x16();
        double by = third.y16() - second.y16();
        double bz = third.z16() - second.z16();
        double nx = ay * bz - az * by;
        double ny = az * bx - ax * bz;
        double nz = ax * by - ay * bx;
        Direction6 best = null;
        double bestDot = 0D;
        for (Direction6 candidate : Direction6.values()) {
            double dot = nx * candidate.stepX() + ny * candidate.stepY()
                    + nz * candidate.stepZ();
            if (dot > bestDot) {
                bestDot = dot;
                best = candidate;
            }
        }
        return bestDot > 1.0E-9 ? best : null;
    }

    private static boolean interpolatable(
            List<CableGeometry.Vertex> vertices,
            Direction6 face
    ) {
        if (vertices == null || vertices.size() != 4 || face == null) {
            return false;
        }
        int firstAxis = tangentAxisA(face);
        int secondAxis = tangentAxisB(face);
        CableGeometry.Vertex origin = vertices.get(0);
        double originX = coordinate(origin, firstAxis);
        double originY = coordinate(origin, secondAxis);
        CableGeometry.Vertex p10 = null;
        CableGeometry.Vertex p01 = null;
        CableGeometry.Vertex p11 = null;
        for (int index = 1; index < vertices.size(); index++) {
            CableGeometry.Vertex vertex = vertices.get(index);
            double x = coordinate(vertex, firstAxis);
            double y = coordinate(vertex, secondAxis);
            if (Double.compare(originY, y) == 0) {
                p10 = vertex;
            } else if (Double.compare(originX, x) == 0) {
                p01 = vertex;
            } else {
                p11 = vertex;
            }
        }
        return p10 != null && p01 != null && p11 != null
                && Double.compare(originX, coordinate(p10, firstAxis)) != 0
                && Double.compare(originY, coordinate(p01, secondAxis)) != 0;
    }

    private static int tangentAxisA(Direction6 face) {
        return switch (face) {
            case DOWN, UP, NORTH, SOUTH -> 0;
            case WEST, EAST -> 2;
        };
    }

    private static int tangentAxisB(Direction6 face) {
        return switch (face) {
            case DOWN, UP -> 2;
            case NORTH, SOUTH, WEST, EAST -> 1;
        };
    }

    private static double coordinate(CableGeometry.Vertex vertex, int axis) {
        return switch (axis) {
            case 0 -> vertex.x16();
            case 1 -> vertex.y16();
            case 2 -> vertex.z16();
            default -> throw new IllegalArgumentException("invalid axis");
        };
    }

    private static boolean isBoundedStaticResource(
            de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.BlockState resource
    ) {
        Variants variants = resource.getVariants();
        Multipart multipart = resource.getMultipart();
        if ((variants == null) == (multipart == null)) {
            return false;
        }
        if (variants != null) {
            VariantSet[] sets = variants.getVariants();
            if (sets == null || sets.length > MAX_STATIC_SELECTIONS) {
                return false;
            }
            for (VariantSet set : sets) {
                if (!isBoundedStaticSet(set)) {
                    return false;
                }
            }
            return variants.getDefaultVariant() == null
                    || isBoundedStaticSet(variants.getDefaultVariant());
        }

        VariantSet[] parts = multipart.getParts();
        if (parts == null || parts.length == 0
                || parts.length > MAX_STATIC_SELECTIONS) {
            return false;
        }
        for (VariantSet part : parts) {
            if (!isBoundedStaticSet(part)) {
                return false;
            }
        }
        return true;
    }

    private static boolean isBoundedStaticSet(VariantSet set) {
        if (set == null || set.getCondition() == null
                || set.getVariants() == null || set.getVariants().length == 0
                || set.getVariants().length > MAX_STATIC_SELECTIONS) {
            return false;
        }
        double totalWeight = 0D;
        for (Variant variant : set.getVariants()) {
            if (variant == null || variant.getRenderer() != BlockRendererType.DEFAULT
                    || variant.getModel() == null
                    || ResourcePack.MISSING_BLOCK_MODEL.equals(variant.getModel())
                    || !Double.isFinite(variant.getWeight()) || variant.getWeight() <= 0D
                    || !quarterTurn(variant.getX())
                    || !quarterTurn(variant.getY())
                    || !quarterTurn(variant.getZ())) {
                return false;
            }
            totalWeight += variant.getWeight();
        }
        return Double.isFinite(totalWeight) && totalWeight > 0D;
    }

    private static boolean weightedSelectionsEquivalent(
            ResourcePack resourcePack,
            de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.BlockState resource,
            BlockState state,
            Key blockId
    ) {
        // M2 already records this exact host-projection boundary: vanilla stone's
        // four base/mirrored/y180 choices collapse to one opaque, nonanimated
        // material descriptor even though their randomized per-quad UVs differ.
        if (M2ResourceModels.STONE.equals(blockId)
                && state.getProperties().isEmpty()
                && M2ResourceModels.resolveStoneTexture(resourcePack) != null) {
            return true;
        }

        Variants variants = resource.getVariants();
        if (variants != null) {
            for (VariantSet set : variants.getVariants()) {
                if (set.getCondition().matches(state)) {
                    return weightedSetEquivalent(resourcePack, set);
                }
            }
            VariantSet defaults = variants.getDefaultVariant();
            return defaults == null || weightedSetEquivalent(resourcePack, defaults);
        }

        for (VariantSet set : resource.getMultipart().getParts()) {
            if (set.getCondition().matches(state)
                    && !weightedSetEquivalent(resourcePack, set)) {
                return false;
            }
        }
        return true;
    }

    private static boolean weightedSetEquivalent(
            ResourcePack resourcePack,
            VariantSet set
    ) {
        Variant[] variants = set.getVariants();
        if (variants.length <= 1) {
            return true;
        }
        VariantDescriptor expected = variantDescriptor(resourcePack, variants[0]);
        if (expected == null) {
            return false;
        }
        for (int index = 1; index < variants.length; index++) {
            if (!expected.equals(variantDescriptor(resourcePack, variants[index]))) {
                return false;
            }
        }
        return true;
    }

    private static VariantDescriptor variantDescriptor(
            ResourcePack resourcePack,
            Variant variant
    ) {
        Model model = variant.getModel().getResource(resourcePack.getModels()::get);
        if (model == null) {
            return null;
        }
        model.applyParent(resourcePack.getModels());
        Element[] elements = model.getElements();
        if (elements == null || elements.length == 0
                || elements.length > MAX_MODEL_ELEMENTS) {
            return null;
        }
        model.calculateProperties(resourcePack.getTextures());

        boolean fullCubeWitness = false;
        List<FacadeLayer> layers = new ArrayList<>();
        for (Element element : elements) {
            if (!validElement(element)) {
                return null;
            }
            fullCubeWitness |= isFullCubeWitness(element);
            for (Direction direction : Direction.values()) {
                Face face = element.getFaces().get(direction);
                if (face == null) {
                    continue;
                }
                FacadeLayer layer = sourceLayer(
                        resourcePack,
                        model,
                        element,
                        direction,
                        face,
                        variant
                );
                if (layer == null || layers.size() == MAX_SOURCE_QUADS) {
                    return null;
                }
                layers.add(layer);
            }
        }
        return layers.isEmpty()
                ? null : new VariantDescriptor(fullCubeWitness, List.copyOf(layers));
    }

    private static boolean quarterTurn(float value) {
        return Float.isFinite(value) && value % 90F == 0F;
    }

    private static Direction6 transformedDirection(
            Direction sourceDirection,
            Variant variant
    ) {
        VectorM3f vector = rotated(sourceDirection, variant);
        int x = Math.round(vector.x);
        int y = Math.round(vector.y);
        int z = Math.round(vector.z);
        for (Direction6 direction : Direction6.values()) {
            if (direction.stepX() == x && direction.stepY() == y
                    && direction.stepZ() == z) {
                return direction;
            }
        }
        return null;
    }

    private static VectorM3f transformedSourceNormal(
            Direction direction,
            Element element,
            Variant variant
    ) {
        VectorM3f normal = new VectorM3f(
                direction.toVector().getX(),
                direction.toVector().getY(),
                direction.toVector().getZ()
        );
        normal.rotateAndScale(element.getRotation().getMatrix());
        if (variant.isTransformed()) {
            cardinalVariantTransform(variant).transformVector(normal);
        }
        return normal;
    }

    private static int uvLockQuarterTurns(Direction direction, Variant variant) {
        VectorM3f rotatedNormal = rotated(direction, variant);
        VectorM3f rotatedUp = rotated(direction.getLocalUp(), variant);
        VectorM3f projectedWorldUp = new VectorM3f(0F, 1F, 0F);
        float dot = projectedWorldUp.dot(rotatedNormal);
        projectedWorldUp.set(
                -rotatedNormal.x * dot,
                1F - rotatedNormal.y * dot,
                -rotatedNormal.z * dot
        );
        if (projectedWorldUp.lengthSquared() < 0.01D) {
            Direction upDown = rotatedNormal.y > 0F ? Direction.UP : Direction.DOWN;
            projectedWorldUp.set(upDown.getLocalUp().toVector());
        } else {
            projectedWorldUp.normalize();
        }
        dot = rotatedUp.dot(projectedWorldUp);
        VectorM3f cross = new VectorM3f(rotatedUp.x, rotatedUp.y, rotatedUp.z)
                .cross(projectedWorldUp);
        double rotation = Math.atan2(cross.dot(rotatedNormal), dot);
        double turns = rotation / (Math.PI / 2D);
        int rounded = (int) Math.round(turns);
        if (Math.abs(turns - rounded) > 1.0E-6D) {
            throw new IllegalArgumentException("uv lock is not a cardinal transform");
        }
        return Math.floorMod(rounded, 4);
    }

    private static VectorM3f rotated(Direction direction, Variant variant) {
        VectorM3f vector = new VectorM3f(
                direction.toVector().getX(),
                direction.toVector().getY(),
                direction.toVector().getZ()
        );
        if (variant.isTransformed()) {
            cardinalVariantTransform(variant).transformVector(vector);
        }
        return vector;
    }

    private static CardinalVariantTransform cardinalVariantTransform(Variant variant) {
        VectorM3f translation = transformedVariantPoint(variant, 0F, 0F, 0F);
        VectorM3f xBasis = transformedVariantPoint(variant, 1F, 0F, 0F);
        VectorM3f yBasis = transformedVariantPoint(variant, 0F, 1F, 0F);
        VectorM3f zBasis = transformedVariantPoint(variant, 0F, 0F, 1F);
        return new CardinalVariantTransform(
                cardinalComponent(xBasis.x - translation.x),
                cardinalComponent(yBasis.x - translation.x),
                cardinalComponent(zBasis.x - translation.x),
                cardinalComponent(xBasis.y - translation.y),
                cardinalComponent(yBasis.y - translation.y),
                cardinalComponent(zBasis.y - translation.y),
                cardinalComponent(xBasis.z - translation.z),
                cardinalComponent(yBasis.z - translation.z),
                cardinalComponent(zBasis.z - translation.z),
                cardinalComponent(translation.x),
                cardinalComponent(translation.y),
                cardinalComponent(translation.z)
        );
    }

    private static VectorM3f transformedVariantPoint(
            Variant variant,
            float x,
            float y,
            float z
    ) {
        VectorM3f point = new VectorM3f(x, y, z);
        point.transform(variant.getTransformMatrix());
        return point;
    }

    private static int cardinalComponent(float value) {
        int rounded = Math.round(value);
        if (!Float.isFinite(value) || Math.abs(value - rounded) > 1.0E-4F
                || Math.abs(rounded) > 1) {
            throw new IllegalArgumentException("variant is not a cardinal transform");
        }
        return rounded;
    }

    private static boolean zeroRotation(Element element) {
        return element.getRotation().getX() == 0
                && element.getRotation().getY() == 0
                && element.getRotation().getZ() == 0
                && element.getRotation().getAngle() == 0
                && !element.getRotation().isRescale();
    }

    record FacadeLayer(
            Key texture,
            int tintIndex,
            boolean shade,
            boolean ambientOcclusion,
            Direction6 cullFace,
            Direction6 nominalFace,
            Direction6 lightFace,
            float sourceNormalY,
            int lightEmission,
            List<CableGeometry.Vertex> sourceVertices
    ) {
        FacadeLayer {
            if (texture == null || tintIndex < -1 || nominalFace == null
                    || lightFace == null || !Float.isFinite(sourceNormalY)
                    || lightEmission < 0 || lightEmission > 15
                    || sourceVertices == null || sourceVertices.size() != 4) {
                throw new IllegalArgumentException("invalid facade layer");
            }
            sourceVertices = List.copyOf(sourceVertices);
        }
    }

    record FacadeMaterial(
            BlockState blockState,
            List<FacadeLayer> layers,
            boolean opaque
    ) {
        FacadeMaterial {
            boolean zeroLayerQuartz = blockState != null
                    && isNativeQuartzFacade(blockState.getId())
                    && layers != null && layers.isEmpty();
            if (blockState == null || layers == null
                    || layers.isEmpty() && !zeroLayerQuartz
                    || layers.size() > MAX_SOURCE_QUADS) {
                throw new IllegalArgumentException("facade material has invalid layers");
            }
            layers = List.copyOf(layers);
        }

        Key texture(Direction6 direction) {
            return layers(direction).getFirst().texture();
        }

        int tintIndex(Direction6 direction) {
            return layers(direction).getFirst().tintIndex();
        }

        List<FacadeLayer> layers(Direction6 direction) {
            return layers.stream()
                    .filter(layer -> layer.nominalFace() == direction)
                    .toList();
        }

        boolean ambientOcclusion() {
            return layers.stream().allMatch(FacadeLayer::ambientOcclusion);
        }
    }

    private record FacadeUv(double u16, double v16) {
    }

    private record VariantDescriptor(
            boolean fullCubeWitness,
            List<FacadeLayer> layers
    ) {
    }

    private record CardinalVariantTransform(
            int xx, int xy, int xz,
            int yx, int yy, int yz,
            int zx, int zy, int zz,
            int tx, int ty, int tz
    ) {
        private CardinalVariantTransform {
            if (Math.abs(xx) + Math.abs(xy) + Math.abs(xz) != 1
                    || Math.abs(yx) + Math.abs(yy) + Math.abs(yz) != 1
                    || Math.abs(zx) + Math.abs(zy) + Math.abs(zz) != 1
                    || Math.abs(xx) + Math.abs(yx) + Math.abs(zx) != 1
                    || Math.abs(xy) + Math.abs(yy) + Math.abs(zy) != 1
                    || Math.abs(xz) + Math.abs(yz) + Math.abs(zz) != 1) {
                throw new IllegalArgumentException("variant is not a cardinal transform");
            }
        }

        private void transformPoint(VectorM3f point) {
            float x = point.x;
            float y = point.y;
            float z = point.z;
            point.set(
                    tx + xx * x + xy * y + xz * z,
                    ty + yx * x + yy * y + yz * z,
                    tz + zx * x + zy * y + zz * z
            );
        }

        private void transformVector(VectorM3f vector) {
            float x = vector.x;
            float y = vector.y;
            float z = vector.z;
            vector.set(
                    xx * x + xy * y + xz * z,
                    yx * x + yy * y + yz * z,
                    zx * x + zy * y + zz * z
            );
        }
    }

    static boolean isNativeQuartzFacade(Key blockId) {
        return "ae2:quartz_glass".equals(blockId.getFormatted())
                || "ae2:quartz_vibrant_glass".equals(blockId.getFormatted());
    }

    private static FacadeMaterial resolveNativeQuartzFacade(
            ResourcePack resourcePack,
            FacadeSnapshot snapshot,
            int x,
            int y,
            int z,
            QuartzFacadeAppearance appearance
    ) {
        Ae219217NativeStructuralProfile.NeutralFacadeMaterial neutral =
                Ae219217NativeStructuralProfile.nativeFacadeNeutralMaterials()
                        .get(snapshot.blockId());
        if (neutral == null || !neutral.properties().equals(snapshot.properties())
                || appearance == null) {
            return null;
        }
        List<FacadeLayer> layers = new ArrayList<>();
        List<QuartzGlassGeometry.Quad> isolated = QuartzGlassGeometry.forSnapshot(
                QuartzGlassSnapshot.isolated(snapshot.blockId()),
                x,
                y,
                z
        );
        for (Direction6 face : Direction6.values()) {
            if (appearance.suppresses(face)) {
                continue;
            }
            QuartzGlassGeometry.Quad base = isolated.stream()
                    .filter(quad -> quad.face() == face
                            && quad.layer() == QuartzGlassGeometry.Layer.BASE)
                    .findFirst()
                    .orElseThrow();
            if (!addQuartzLayer(resourcePack, layers, base)) {
                return null;
            }
            int frameMask = appearance.frameMask(face);
            if (frameMask == 0) {
                continue;
            }
            QuartzGlassGeometry.Quad frameTemplate = isolated.stream()
                    .filter(quad -> quad.face() == face
                            && quad.layer() == QuartzGlassGeometry.Layer.FRAME)
                    .findFirst()
                    .orElseThrow();
            QuartzGlassGeometry.Quad frame = new QuartzGlassGeometry.Quad(
                    face,
                    QuartzGlassGeometry.Layer.FRAME,
                    frameMask,
                    frameTemplate.vertices()
            );
            if (!addQuartzLayer(resourcePack, layers, frame)) {
                return null;
            }
        }
        return new FacadeMaterial(
                new BlockState(Key.parse(snapshot.blockId()), snapshot.properties()),
                layers,
                false
        );
    }

    private static boolean addQuartzLayer(
            ResourcePack resourcePack,
            List<FacadeLayer> layers,
            QuartzGlassGeometry.Quad quad
    ) {
        Key texture = M3cQuartzGlassResourceModels.texture(quad);
        if (resourcePack.getTextures().get(texture) == null) {
            return false;
        }
        List<CableGeometry.Vertex> vertices = quad.vertices().stream()
                .map(vertex -> new CableGeometry.Vertex(
                        vertex.x16(),
                        vertex.y16(),
                        vertex.z16(),
                        vertex.u16(),
                        vertex.v16()
                ))
                .toList();
        layers.add(new FacadeLayer(
                texture,
                -1,
                true,
                false,
                quad.face(),
                quad.face(),
                quad.face(),
                quad.face().stepY(),
                0,
                vertices
        ));
        return true;
    }

    record QuartzFacadeAppearance(
            int suppressedFaceMask,
            Map<Direction6, Integer> frameMasks
    ) {
        QuartzFacadeAppearance {
            if ((suppressedFaceMask & ~0x3f) != 0 || frameMasks == null
                    || frameMasks.size() != Direction6.values().length) {
                throw new IllegalArgumentException("invalid quartz facade appearance");
            }
            java.util.EnumMap<Direction6, Integer> copy =
                    new java.util.EnumMap<>(Direction6.class);
            for (Direction6 direction : Direction6.values()) {
                Integer mask = frameMasks.get(direction);
                if (mask == null || mask < 0 || mask > 15) {
                    throw new IllegalArgumentException("invalid quartz frame mask");
                }
                copy.put(direction, mask);
            }
            frameMasks = Map.copyOf(copy);
        }

        boolean suppresses(Direction6 direction) {
            return (suppressedFaceMask & direction.maskBit()) != 0;
        }

        int frameMask(Direction6 direction) {
            return frameMasks.get(direction);
        }
    }
}
