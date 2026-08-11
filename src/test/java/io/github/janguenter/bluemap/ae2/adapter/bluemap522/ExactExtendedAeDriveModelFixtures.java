/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.adapter.bluemap522;

import com.flowpowered.math.vector.Vector3f;
import com.flowpowered.math.vector.Vector4f;
import de.bluecolored.bluemap.core.resources.ResourcePath;
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
import java.util.LinkedHashMap;
import java.util.Map;

/** Normalized semantic fixtures derived from the pinned ExtendedAE 2.2.33 models. */
final class ExactExtendedAeDriveModelFixtures {

    private static final String FRONT = "ae2:block/drive/drive_front";
    private static final String INSIDE_TOP = "ae2:block/drive/drive_inside_top";
    private static final String EXTENDED_FRONT = "extendedae:block/generics/front";
    private static final String EXTENDED_SIDE = "extendedae:block/generics/side";
    private static final String EXTENDED_INSIDE =
            "extendedae:block/extended_drive/drive_inside";
    private static final Field AMBIENT_OCCLUSION = ambientOcclusionField();

    private ExactExtendedAeDriveModelFixtures() {
    }

    static Map<String, Model> models() {
        Map<String, Model> models = new LinkedHashMap<>();
        models.put(
                "extendedae:block/extended_drive/extended_me_drive_base",
                model(
                        true,
                        element(point(0, 1, 0), point(1, 15, 16), null,
                                face(Direction.NORTH, uv(0, 1, 1, 15),
                                        EXTENDED_FRONT, Direction.NORTH, 0),
                                face(Direction.EAST, uv(0, 1, 16, 15),
                                        EXTENDED_INSIDE, null, 180),
                                face(Direction.SOUTH, uv(0, 1, 1, 15),
                                        extended("ex_drive_side"), Direction.SOUTH, 0),
                                face(Direction.WEST, uv(0, 1, 16, 15),
                                        extended("ex_drive_side"), Direction.WEST, 0)),
                        element(point(15, 1, 0), point(16, 15, 16), null,
                                face(Direction.NORTH, uv(0, 1, 1, 15),
                                        EXTENDED_FRONT, Direction.NORTH, 0),
                                face(Direction.EAST, uv(16, 1, 0, 15),
                                        extended("ex_drive_side"), Direction.EAST, 0),
                                face(Direction.SOUTH, uv(15, 1, 16, 15),
                                        extended("ex_drive_side"), Direction.SOUTH, 0),
                                face(Direction.WEST, uv(0, 15, 16, 1),
                                        EXTENDED_INSIDE, null, 0)),
                        element(point(1, 1, 7), point(15, 15, 9),
                                rotation(8, 8, 2),
                                face(Direction.NORTH, uv(1, 1, 15, 15),
                                        FRONT, null, 0),
                                face(Direction.SOUTH, uv(1, 1, 15, 15),
                                        FRONT, Direction.SOUTH, 0)),
                        element(point(0, 0, 0), point(16, 1, 16), null,
                                face(Direction.NORTH, uv(0, 15, 16, 16),
                                        EXTENDED_FRONT, Direction.NORTH, 0),
                                face(Direction.EAST, uv(0, 15, 16, 16),
                                        EXTENDED_SIDE, Direction.EAST, 0),
                                face(Direction.SOUTH, uv(0, 15, 16, 16),
                                        EXTENDED_SIDE, Direction.SOUTH, 0),
                                face(Direction.WEST, uv(0, 15, 16, 16),
                                        EXTENDED_SIDE, Direction.WEST, 0),
                                face(Direction.UP, uv(0, 0, 16, 16),
                                        extended("ex_drive_bottom"), null, 90),
                                face(Direction.DOWN, uv(0, 0, 16, 16),
                                        "ae2:block/generics/bottom", Direction.DOWN, 0)),
                        element(point(0, 15, 0), point(16, 16, 16), null,
                                face(Direction.NORTH, uv(16, 0, 0, 1),
                                        EXTENDED_FRONT, Direction.NORTH, 0),
                                face(Direction.EAST, uv(0, 0, 16, 1),
                                        EXTENDED_SIDE, Direction.EAST, 0),
                                face(Direction.SOUTH, uv(0, 0, 16, 1),
                                        EXTENDED_SIDE, Direction.SOUTH, 0),
                                face(Direction.WEST, uv(0, 0, 16, 1),
                                        EXTENDED_SIDE, Direction.WEST, 0),
                                face(Direction.UP, uv(0, 0, 16, 16),
                                        "ae2:block/generics/top", Direction.UP, 0),
                                face(Direction.DOWN, uv(0, 0, 16, 16),
                                        extended("ex_drive_top"), null, 270)),
                        element(point(7, 1, 1), point(9, 15, 15), null,
                                face(Direction.NORTH, uv(7, 1, 9, 15),
                                        FRONT, null, 0),
                                face(Direction.EAST, uv(1, 1, 15, 15),
                                        EXTENDED_INSIDE, null, 180),
                                face(Direction.SOUTH, uv(7, 1, 9, 12),
                                        FRONT, null, 0),
                                face(Direction.WEST, uv(16, 15, 1, 1),
                                        EXTENDED_INSIDE, null, 0)),
                        internalSeparator(3, 3, 13, uv(3, 1, 7, 15)),
                        frontSeparator(3, uv(1, 12, 15, 13), uv(15, 12, 1, 13)),
                        frontSeparator(6, uv(1, 9, 15, 10), uv(15, 9, 1, 10)),
                        frontSeparator(9, uv(1, 6, 15, 7), uv(15, 6, 1, 7)),
                        frontSeparator(12, uv(1, 3, 15, 4), uv(15, 3, 1, 4)),
                        rearSeparator(6, -6),
                        rearSeparator(3, -9),
                        rearSeparator(12, 0),
                        rearSeparator(9, -3),
                        internalSeparator(6, 3, 13, uv(7, 1, 11, 15)),
                        internalSeparator(9, 3, 13, uv(11, 15, 15, 1)),
                        internalSeparator(12, 3, 13, uv(7, 15, 11, 1))
                )
        );
        models.put(
                "extendedae:block/drive/infinity_water_cell",
                cell("extendedae:block/drive/infinity_cell", uv(0, 0, 6, 2))
        );
        models.put(
                "extendedae:block/drive/infinity_cobblestone_cell",
                cell("extendedae:block/drive/infinity_cell", uv(6, 0, 12, 2))
        );
        models.put(
                "extendedae:block/drive/void_cell",
                cell("extendedae:block/drive/void_cell", uv(0, 0, 6, 2))
        );
        return Map.copyOf(models);
    }

    private static Element frontSeparator(
            int y,
            Vector4f frontUv,
            Vector4f horizontalUv
    ) {
        return element(
                point(1, y, 2),
                point(15, y + 1, 3),
                null,
                face(Direction.NORTH, frontUv, FRONT, null, 0),
                face(Direction.UP, horizontalUv, FRONT, null, 0),
                face(Direction.DOWN, horizontalUv, FRONT, null, 0)
        );
    }

    private static Element rearSeparator(int y, int rotationY) {
        return element(
                point(1, y, 13),
                point(15, y + 1, 14),
                rotation(0, rotationY, 11),
                face(Direction.SOUTH, uv(1, 3, 15, 4), FRONT, null, 0),
                face(Direction.UP, uv(15, 3, 1, 4), FRONT, null, 0),
                face(Direction.DOWN, uv(15, 3, 1, 4), FRONT, null, 0)
        );
    }

    private static Element internalSeparator(
            int y,
            int fromZ,
            int toZ,
            Vector4f upUv
    ) {
        return element(
                point(1, y, fromZ),
                point(15, y + 1, toZ),
                null,
                face(Direction.UP, upUv, INSIDE_TOP, null, 90),
                face(Direction.DOWN, uv(11, 1, 15, 15),
                        EXTENDED_INSIDE, null, 270)
        );
    }

    private static Model cell(String texture, Vector4f northUv) {
        float start = northUv.getX();
        float end = northUv.getZ();
        return model(
                false,
                element(
                        point(0, 0, 0),
                        point(6, 2, 2),
                        rotation(9, 8, 8),
                        face(Direction.NORTH, northUv, texture, Direction.NORTH, 0),
                        face(Direction.UP, uv(end, 0, start, 2),
                                texture, Direction.NORTH, 0),
                        face(Direction.DOWN, uv(end, 0, start, 2),
                                texture, Direction.NORTH, 0)
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

    private static Rotation rotation(float originX, float originY, float originZ) {
        return new Rotation(
                new Vector3f(originX, originY, originZ),
                Axis.Y,
                0,
                false
        );
    }

    private static String extended(String texture) {
        return "extendedae:block/extended_drive/" + texture;
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
