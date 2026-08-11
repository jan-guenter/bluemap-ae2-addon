/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.adapter.bluemap522;

import com.flowpowered.math.vector.Vector3f;
import com.flowpowered.math.vector.Vector4f;
import de.bluecolored.bluemap.core.resources.ResourcePath;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.model.Element;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.model.Face;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.model.Model;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.model.Rotation;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.model.TextureVariable;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.texture.Texture;
import de.bluecolored.bluemap.core.util.Direction;
import de.bluecolored.bluemap.core.util.math.Axis;

import java.lang.reflect.Field;
import java.util.EnumMap;
import java.util.Map;

/** Independent normalized semantic fixtures for the three exact M3f models. */
final class ExactM3CompletionModelFixtures {

    private static final String CRANK_TEXTURE = "ae2:block/crank";
    private static final String INSCRIBER_TEXTURE = "ae2:block/inscriber";
    private static final Field AMBIENT_OCCLUSION = ambientOcclusionField();

    private ExactM3CompletionModelFixtures() {
    }

    static void install(ResourcePack resourcePack) {
        resourcePack.getModels().put(
                M3CompletionResourceModels.CRANK_BASE,
                crankBase()
        );
        resourcePack.getModels().put(
                M3CompletionResourceModels.CRANK_HANDLE,
                crankHandle()
        );
        resourcePack.getModels().put(
                M3CompletionResourceModels.INSCRIBER,
                inscriber()
        );
    }

    static Model crankBaseGeometryDrift() {
        return crankBase(5F, uv(3F, 0F, 7F, 4F), CRANK_TEXTURE, true);
    }

    static Model crankBaseUvDrift() {
        return crankBase(6F, uv(2F, 0F, 7F, 4F), CRANK_TEXTURE, true);
    }

    static Model crankBaseTextureDrift() {
        return crankBase(6F, uv(3F, 0F, 7F, 4F), "ae2:block/paint1", true);
    }

    static Model crankBaseAmbientOcclusionDrift() {
        return crankBase(6F, uv(3F, 0F, 7F, 4F), CRANK_TEXTURE, false);
    }

    private static Model crankBase() {
        return crankBase(6F, uv(3F, 0F, 7F, 4F), CRANK_TEXTURE, true);
    }

    private static Model crankBase(
            float fromX,
            Vector4f northUv,
            String texture,
            boolean ambientOcclusion
    ) {
        return model(
                ambientOcclusion,
                element(
                        point(fromX, 6F, 13F),
                        point(10F, 10F, 16F),
                        rotation(8F, 8F, 8F, Axis.Y, 0F, false),
                        face(Direction.NORTH, northUv, texture, null, 180),
                        face(Direction.EAST, uv(0F, 4F, 3F, 8F), texture, null, 0),
                        face(Direction.SOUTH, uv(7F, 0F, 11F, 4F), texture, null, 0),
                        face(Direction.WEST, uv(0F, 4F, 3F, 0F), texture, null, 180),
                        face(Direction.UP, uv(0F, 8F, 3F, 4F), texture, null, 270),
                        face(Direction.DOWN, uv(0F, 0F, 3F, 4F), texture, null, 90)
                )
        );
    }

    private static Model crankHandle() {
        return model(
                true,
                element(
                        point(7F, 7F, 7F),
                        point(9F, 9F, 13F),
                        null,
                        face(Direction.EAST, uv(2F, 8F, 0F, 14F), CRANK_TEXTURE, null, 90),
                        face(Direction.SOUTH, uv(12F, 4F, 14F, 6F), CRANK_TEXTURE, null, 0),
                        face(Direction.WEST, uv(6F, 8F, 4F, 14F), CRANK_TEXTURE, null, 270),
                        face(Direction.UP, uv(4F, 8F, 2F, 14F), CRANK_TEXTURE, null, 0),
                        face(Direction.DOWN, uv(8F, 8F, 6F, 14F), CRANK_TEXTURE, null, 180)
                ),
                element(
                        point(7F, 7F, 5F),
                        point(14F, 9F, 7F),
                        null,
                        face(Direction.NORTH, uv(3F, 6F, 10F, 8F), CRANK_TEXTURE, null, 180),
                        face(Direction.EAST, uv(10F, 4F, 12F, 6F), CRANK_TEXTURE, null, 90),
                        face(Direction.SOUTH, uv(8F, 10F, 15F, 12F), CRANK_TEXTURE, null, 0),
                        face(Direction.WEST, uv(10F, 6F, 12F, 8F), CRANK_TEXTURE, null, 270),
                        face(Direction.UP, uv(3F, 4F, 10F, 6F), CRANK_TEXTURE, null, 0),
                        face(Direction.DOWN, uv(8F, 8F, 15F, 10F), CRANK_TEXTURE, null, 180)
                )
        );
    }

    private static Model inscriber() {
        return model(
                true,
                element(
                        point(0F, 12F, 0F),
                        point(16F, 16F, 16F),
                        rotation(8F, 8F, 8F, Axis.Y, 0F, false),
                        face(Direction.NORTH, uv(0F, 4F, 4F, 5F), INSCRIBER_TEXTURE, Direction.NORTH, 0),
                        face(Direction.EAST, uv(0F, 4F, 4F, 5F), INSCRIBER_TEXTURE, Direction.EAST, 0),
                        face(Direction.SOUTH, uv(0F, 4F, 4F, 5F), INSCRIBER_TEXTURE, Direction.SOUTH, 0),
                        face(Direction.WEST, uv(4F, 4F, 0F, 5F), INSCRIBER_TEXTURE, Direction.WEST, 0),
                        face(Direction.UP, uv(0F, 0F, 4F, 4F), INSCRIBER_TEXTURE, Direction.UP, 270),
                        face(Direction.DOWN, uv(8F, 4F, 12F, 8F), INSCRIBER_TEXTURE, null, 270)
                ),
                element(
                        point(0F, 0F, 0F),
                        point(16F, 4F, 16F),
                        rotation(8F, 8F, 8F, Axis.Y, 0F, false),
                        face(Direction.NORTH, uv(0F, 5F, 4F, 6F), INSCRIBER_TEXTURE, Direction.NORTH, 0),
                        face(Direction.EAST, uv(0F, 5F, 4F, 6F), INSCRIBER_TEXTURE, Direction.EAST, 0),
                        face(Direction.SOUTH, uv(0F, 5F, 4F, 6F), INSCRIBER_TEXTURE, Direction.SOUTH, 0),
                        face(Direction.WEST, uv(0F, 5F, 4F, 6F), INSCRIBER_TEXTURE, Direction.WEST, 0),
                        face(Direction.UP, uv(8F, 0F, 4F, 4F), INSCRIBER_TEXTURE, null, 270),
                        face(Direction.DOWN, uv(8F, 0F, 12F, 4F), INSCRIBER_TEXTURE, Direction.DOWN, 270)
                ),
                element(
                        point(0F, 4F, 2F),
                        point(2F, 12F, 14F),
                        null,
                        face(Direction.NORTH, uv(4.5F, 4F, 4F, 6F), INSCRIBER_TEXTURE, null, 0),
                        face(Direction.EAST, uv(4F, 6F, 7F, 8F), INSCRIBER_TEXTURE, null, 0),
                        face(Direction.SOUTH, uv(7F, 4F, 6.5F, 6F), INSCRIBER_TEXTURE, null, 0),
                        face(Direction.WEST, uv(4F, 4F, 7F, 6F), INSCRIBER_TEXTURE, Direction.WEST, 0)
                ),
                element(
                        point(14F, 4F, 2F),
                        point(16F, 12F, 14F),
                        null,
                        face(Direction.NORTH, uv(7F, 4F, 6.5F, 6F), INSCRIBER_TEXTURE, null, 0),
                        face(Direction.EAST, uv(4F, 4F, 7F, 6F), INSCRIBER_TEXTURE, Direction.EAST, 0),
                        face(Direction.SOUTH, uv(4.5F, 4F, 4F, 6F), INSCRIBER_TEXTURE, null, 0),
                        face(Direction.WEST, uv(7F, 8F, 4F, 6F), INSCRIBER_TEXTURE, null, 180)
                ),
                element(
                        point(13F, 4F, 7F),
                        point(14F, 12F, 9F),
                        null,
                        face(Direction.NORTH, uv(2.75F, 6F, 3F, 8F), INSCRIBER_TEXTURE, null, 180),
                        face(Direction.SOUTH, uv(2.5F, 6F, 2.75F, 8F), INSCRIBER_TEXTURE, null, 180),
                        face(Direction.WEST, uv(2.5F, 6F, 3F, 8F), INSCRIBER_TEXTURE, null, 180)
                ),
                element(
                        point(2F, 4F, 7F),
                        point(3F, 12F, 9F),
                        rotation(16F, 0F, 0F, Axis.Y, 0F, false),
                        face(Direction.NORTH, uv(3F, 6F, 2.75F, 8F), INSCRIBER_TEXTURE, null, 180),
                        face(Direction.EAST, uv(3F, 6F, 2.5F, 8F), INSCRIBER_TEXTURE, null, 180),
                        face(Direction.SOUTH, uv(2.75F, 6F, 2.5F, 8F), INSCRIBER_TEXTURE, null, 180)
                ),
                element(
                        point(3F, 4F, 14F),
                        point(13F, 12F, 16F),
                        null,
                        face(Direction.NORTH, uv(4.25F, 6F, 6.75F, 8F), INSCRIBER_TEXTURE, null, 0),
                        face(Direction.EAST, uv(2.5F, 6F, 2F, 8F), INSCRIBER_TEXTURE, null, 0),
                        face(Direction.SOUTH, uv(0F, 6F, 2.5F, 8F), INSCRIBER_TEXTURE, Direction.SOUTH, 0),
                        face(Direction.WEST, uv(0.5F, 6F, 0F, 8F), INSCRIBER_TEXTURE, null, 0)
                ),
                element(
                        point(7F, 4F, 13F),
                        point(9F, 12F, 14F),
                        null,
                        face(Direction.NORTH, uv(2.5F, 6F, 3F, 8F), INSCRIBER_TEXTURE, null, 0),
                        face(Direction.EAST, uv(2.5F, 6F, 2.75F, 8F), INSCRIBER_TEXTURE, null, 0),
                        face(Direction.WEST, uv(2.75F, 6F, 3F, 8F), INSCRIBER_TEXTURE, null, 0)
                )
        );
    }

    private static Model model(boolean ambientOcclusion, Element... elements) {
        Model model = new Model(Map.of(), elements);
        try {
            AMBIENT_OCCLUSION.set(model, ambientOcclusion);
        } catch (IllegalAccessException exception) {
            throw new AssertionError(exception);
        }
        return model;
    }

    private static Element element(
            Vector3f from,
            Vector3f to,
            Rotation rotation,
            FaceEntry... entries
    ) {
        EnumMap<Direction, Face> faces = new EnumMap<>(Direction.class);
        for (FaceEntry entry : entries) {
            faces.put(entry.direction(), entry.face());
        }
        return rotation == null
                ? new Element(from, to, faces)
                : new Element(from, to, rotation, faces);
    }

    private static FaceEntry face(
            Direction direction,
            Vector4f uv,
            String texture,
            Direction cullface,
            int rotation
    ) {
        return new FaceEntry(
                direction,
                new Face(
                        uv,
                        new TextureVariable(new ResourcePath<Texture>(texture)),
                        cullface,
                        rotation,
                        -1
                )
        );
    }

    private static Vector3f point(float x, float y, float z) {
        return new Vector3f(x, y, z);
    }

    private static Vector4f uv(float minU, float minV, float maxU, float maxV) {
        return new Vector4f(minU, minV, maxU, maxV);
    }

    private static Rotation rotation(
            float originX,
            float originY,
            float originZ,
            Axis axis,
            float angle,
            boolean rescale
    ) {
        return new Rotation(
                new Vector3f(originX, originY, originZ),
                axis,
                angle,
                rescale
        );
    }

    private static Field ambientOcclusionField() {
        try {
            Field field = Model.class.getDeclaredField("ambientocclusion");
            field.setAccessible(true);
            return field;
        } catch (ReflectiveOperationException exception) {
            throw new ExceptionInInitializerError(exception);
        }
    }

    private record FaceEntry(Direction direction, Face face) {
    }
}
