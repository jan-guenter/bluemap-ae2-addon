/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.adapter.bluemap523;

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

/** Normalized semantic fixtures derived from the pinned AE2 19.2.17 model JSON. */
final class ExactDriveModelFixtures {
  private static final Field AMBIENT_OCCLUSION = ambientOcclusionField();

  private ExactDriveModelFixtures() {}

  static Map<String, Model> models() {
    Map<String, Model> models = new LinkedHashMap<>();
    models.put("ae2:block/drive/drive_base",
        model(true,
            element(point(0F, 1F, 0F), point(1F, 15F, 16F), null,
                face(Direction.NORTH, uv(15F, 1F, 16F, 15F), "ae2:block/generics/front", Direction.NORTH, 0, -1),
                face(
                    Direction.EAST, uv(0F, 1F, 16F, 15F), "ae2:block/drive/drive_inside", Direction.NORTH, 180, -1),
                face(Direction.SOUTH, uv(0F, 1F, 1F, 15F), "ae2:block/generics/back", Direction.SOUTH, 0, -1),
                face(Direction.WEST, uv(0F, 1F, 16F, 15F), "ae2:block/generics/side", Direction.WEST, 0, -1)),
            element(point(15F, 1F, 0F), point(16F, 15F, 16F), null,
                face(Direction.NORTH, uv(0F, 1F, 1F, 15F), "ae2:block/generics/front", Direction.NORTH, 0, -1),
                face(Direction.EAST, uv(16F, 1F, 0F, 15F), "ae2:block/generics/side", Direction.EAST, 0, -1),
                face(Direction.SOUTH, uv(15F, 1F, 16F, 15F), "ae2:block/generics/back", Direction.SOUTH, 0, -1),
                face(Direction.WEST, uv(0F, 15F, 16F, 1F), "ae2:block/drive/drive_inside", Direction.NORTH, 0, -1)),
            element(point(1F, 1F, 7F), point(15F, 15F, 16F), rotation(8F, 8F, 9F, Axis.Y, 0F, false),
                face(Direction.NORTH, uv(1F, 1F, 15F, 15F), "ae2:block/drive/drive_front", null, 0, -1),
                face(Direction.SOUTH, uv(1F, 1F, 15F, 15F), "ae2:block/generics/back", Direction.SOUTH, 0, -1)),
            element(point(0F, 0F, 0F), point(16F, 1F, 16F), null,
                face(Direction.NORTH, uv(0F, 15F, 16F, 16F), "ae2:block/generics/front", Direction.NORTH, 0, -1),
                face(Direction.EAST, uv(0F, 15F, 16F, 16F), "ae2:block/generics/side", Direction.EAST, 0, -1),
                face(Direction.SOUTH, uv(0F, 15F, 16F, 16F), "ae2:block/generics/side", Direction.SOUTH, 0, -1),
                face(Direction.WEST, uv(0F, 15F, 16F, 16F), "ae2:block/generics/side", Direction.WEST, 0, -1),
                face(Direction.UP, uv(0F, 0F, 16F, 16F), "ae2:block/drive/drive_inside_bottom", Direction.NORTH, 90,
                    -1),
                face(Direction.DOWN, uv(0F, 0F, 16F, 16F), "ae2:block/generics/bottom", Direction.DOWN, 0, -1)),
            element(point(0F, 15F, 0F), point(16F, 16F, 16F), null,
                face(Direction.NORTH, uv(16F, 0F, 0F, 1F), "ae2:block/generics/front", Direction.NORTH, 0, -1),
                face(Direction.EAST, uv(0F, 0F, 16F, 1F), "ae2:block/generics/side", Direction.EAST, 0, -1),
                face(Direction.SOUTH, uv(0F, 0F, 16F, 1F), "ae2:block/generics/side", Direction.SOUTH, 0, -1),
                face(Direction.WEST, uv(0F, 0F, 16F, 1F), "ae2:block/generics/side", Direction.WEST, 0, -1),
                face(Direction.UP, uv(0F, 0F, 16F, 16F), "ae2:block/generics/top", Direction.UP, 0, -1),
                face(Direction.DOWN, uv(0F, 0F, 16F, 16F), "ae2:block/drive/drive_inside_top", Direction.NORTH, 270,
                    -1)),
            element(point(7F, 1F, 1F), point(9F, 15F, 7F), null,
                face(Direction.NORTH, uv(7F, 1F, 9F, 15F), "ae2:block/drive/drive_front", Direction.NORTH, 0, -1),
                face(Direction.EAST, uv(1F, 1F, 7F, 15F), "ae2:block/drive/drive_inside", Direction.NORTH, 180, -1),
                face(Direction.WEST, uv(9F, 1F, 15F, 15F), "ae2:block/drive/drive_inside", Direction.NORTH, 0, -1)),
            element(point(1F, 3F, 3F), point(15F, 4F, 7F), null,
                face(
                    Direction.UP, uv(3F, 1F, 7F, 15F), "ae2:block/drive/drive_inside_top", Direction.NORTH, 90, -1),
                face(Direction.DOWN, uv(11F, 1F, 15F, 15F), "ae2:block/drive/drive_inside", Direction.NORTH, 270,
                    -1)),
            element(point(1F, 3F, 2F), point(15F, 4F, 3F), null,
                face(Direction.NORTH, uv(1F, 12F, 15F, 13F), "ae2:block/drive/drive_front", Direction.NORTH, 0, -1),
                face(Direction.UP, uv(15F, 12F, 1F, 13F), "ae2:block/drive/drive_front", Direction.NORTH, 0, -1),
                face(Direction.DOWN, uv(15F, 12F, 1F, 13F), "ae2:block/drive/drive_front", Direction.NORTH, 0, -1)),
            element(point(1F, 6F, 2F), point(15F, 7F, 3F), null,
                face(Direction.NORTH, uv(1F, 9F, 15F, 10F), "ae2:block/drive/drive_front", Direction.NORTH, 0, -1),
                face(Direction.UP, uv(15F, 9F, 1F, 10F), "ae2:block/drive/drive_front", Direction.NORTH, 0, -1),
                face(Direction.DOWN, uv(15F, 9F, 1F, 10F), "ae2:block/drive/drive_front", Direction.NORTH, 0, -1)),
            element(point(1F, 9F, 2F), point(15F, 10F, 3F), null,
                face(Direction.NORTH, uv(1F, 6F, 15F, 7F), "ae2:block/drive/drive_front", Direction.NORTH, 0, -1),
                face(Direction.UP, uv(15F, 6F, 1F, 7F), "ae2:block/drive/drive_front", Direction.NORTH, 0, -1),
                face(Direction.DOWN, uv(15F, 6F, 1F, 7F), "ae2:block/drive/drive_front", Direction.NORTH, 0, -1)),
            element(point(1F, 12F, 2F), point(15F, 13F, 3F), null,
                face(Direction.NORTH, uv(1F, 3F, 15F, 4F), "ae2:block/drive/drive_front", Direction.NORTH, 0, -1),
                face(Direction.UP, uv(15F, 3F, 1F, 4F), "ae2:block/drive/drive_front", Direction.NORTH, 0, -1),
                face(Direction.DOWN, uv(15F, 3F, 1F, 4F), "ae2:block/drive/drive_front", Direction.NORTH, 0, -1)),
            element(point(1F, 6F, 3F), point(15F, 7F, 7F), null,
                face(Direction.UP, uv(7F, 1F, 11F, 15F), "ae2:block/drive/drive_inside_top", Direction.NORTH, 90,
                    -1),
                face(Direction.DOWN, uv(11F, 1F, 15F, 15F), "ae2:block/drive/drive_inside", Direction.NORTH, 270,
                    -1)),
            element(point(1F, 9F, 3F), point(15F, 10F, 7F), null,
                face(Direction.UP, uv(11F, 15F, 15F, 1F), "ae2:block/drive/drive_inside_top", Direction.NORTH, 90,
                    -1),
                face(Direction.DOWN, uv(11F, 1F, 15F, 15F), "ae2:block/drive/drive_inside", Direction.NORTH, 270,
                    -1)),
            element(point(1F, 12F, 3F), point(15F, 13F, 7F), null,
                face(Direction.UP, uv(7F, 15F, 11F, 1F), "ae2:block/drive/drive_inside_top", Direction.NORTH, 90,
                    -1),
                face(Direction.DOWN, uv(11F, 1F, 15F, 15F), "ae2:block/drive/drive_inside", Direction.NORTH, 270,
                    -1))));
    models.put("ae2:block/drive/drive_cell_empty", model(true));
    models.put("ae2:block/drive/drive_cell",
        model(false,
            element(point(0F, 0F, 0F), point(6F, 2F, 2F), rotation(9F, 8F, 8F, Axis.Y, 0F, false),
                face(Direction.NORTH, uv(0F, 2F, 6F, 4F), "ae2:block/drive/drive_cells", Direction.NORTH, 0, -1),
                face(Direction.UP, uv(0F, 0F, 6F, 2F), "ae2:block/drive/drive_cells", Direction.NORTH, 0, -1),
                face(Direction.DOWN, uv(0F, 4F, 6F, 6F), "ae2:block/drive/drive_cells", Direction.NORTH, 0, -1))));
    models.put("ae2:block/drive/cells/1k_item_cell",
        model(false,
            element(point(0F, 0F, 0F), point(6F, 2F, 2F), rotation(9F, 8F, 8F, Axis.Y, 0F, false),
                face(Direction.NORTH, uv(0F, 0F, 6F, 2F), "ae2:block/drive/drive_cells", Direction.NORTH, 0, -1),
                face(Direction.UP, uv(6F, 0F, 0F, 2F), "ae2:block/drive/drive_cells", Direction.NORTH, 0, -1),
                face(Direction.DOWN, uv(6F, 0F, 0F, 2F), "ae2:block/drive/drive_cells", Direction.NORTH, 0, -1))));
    models.put("ae2:block/drive/cells/4k_item_cell",
        model(false,
            element(point(0F, 0F, 0F), point(6F, 2F, 2F), rotation(9F, 8F, 8F, Axis.Y, 0F, false),
                face(Direction.NORTH, uv(0F, 2F, 6F, 4F), "ae2:block/drive/drive_cells", Direction.NORTH, 0, -1),
                face(Direction.UP, uv(6F, 2F, 0F, 4F), "ae2:block/drive/drive_cells", Direction.NORTH, 0, -1),
                face(Direction.DOWN, uv(6F, 2F, 0F, 4F), "ae2:block/drive/drive_cells", Direction.NORTH, 0, -1))));
    models.put("ae2:block/drive/cells/16k_item_cell",
        model(false,
            element(point(0F, 0F, 0F), point(6F, 2F, 2F), rotation(9F, 8F, 8F, Axis.Y, 0F, false),
                face(Direction.NORTH, uv(0F, 4F, 6F, 6F), "ae2:block/drive/drive_cells", Direction.NORTH, 0, -1),
                face(Direction.UP, uv(6F, 4F, 0F, 6F), "ae2:block/drive/drive_cells", Direction.NORTH, 0, -1),
                face(Direction.DOWN, uv(6F, 4F, 0F, 6F), "ae2:block/drive/drive_cells", Direction.NORTH, 0, -1))));
    models.put("ae2:block/drive/cells/64k_item_cell",
        model(false,
            element(point(0F, 0F, 0F), point(6F, 2F, 2F), rotation(9F, 8F, 8F, Axis.Y, 0F, false),
                face(Direction.NORTH, uv(0F, 6F, 6F, 8F), "ae2:block/drive/drive_cells", Direction.NORTH, 0, -1),
                face(Direction.UP, uv(6F, 6F, 0F, 8F), "ae2:block/drive/drive_cells", Direction.NORTH, 0, -1),
                face(Direction.DOWN, uv(6F, 6F, 0F, 8F), "ae2:block/drive/drive_cells", Direction.NORTH, 0, -1))));
    models.put("ae2:block/drive/cells/256k_item_cell",
        model(false,
            element(point(0F, 0F, 0F), point(6F, 2F, 2F), rotation(9F, 8F, 8F, Axis.Y, 0F, false),
                face(Direction.NORTH, uv(0F, 8F, 6F, 10F), "ae2:block/drive/drive_cells", Direction.NORTH, 0, -1),
                face(Direction.UP, uv(6F, 8F, 0F, 10F), "ae2:block/drive/drive_cells", Direction.NORTH, 0, -1),
                face(Direction.DOWN, uv(6F, 8F, 0F, 10F), "ae2:block/drive/drive_cells", Direction.NORTH, 0, -1))));
    models.put("ae2:block/drive/cells/1k_fluid_cell",
        model(false,
            element(point(0F, 0F, 0F), point(6F, 2F, 2F), rotation(9F, 8F, 8F, Axis.Y, 0F, false),
                face(Direction.NORTH, uv(6F, 0F, 12F, 2F), "ae2:block/drive/drive_cells", Direction.NORTH, 0, -1),
                face(Direction.UP, uv(12F, 0F, 6F, 2F), "ae2:block/drive/drive_cells", Direction.NORTH, 0, -1),
                face(Direction.DOWN, uv(12F, 0F, 6F, 2F), "ae2:block/drive/drive_cells", Direction.NORTH, 0, -1))));
    models.put("ae2:block/drive/cells/4k_fluid_cell",
        model(false,
            element(point(0F, 0F, 0F), point(6F, 2F, 2F), rotation(9F, 8F, 8F, Axis.Y, 0F, false),
                face(Direction.NORTH, uv(6F, 2F, 12F, 4F), "ae2:block/drive/drive_cells", Direction.NORTH, 0, -1),
                face(Direction.UP, uv(12F, 2F, 6F, 4F), "ae2:block/drive/drive_cells", Direction.NORTH, 0, -1),
                face(Direction.DOWN, uv(12F, 2F, 6F, 4F), "ae2:block/drive/drive_cells", Direction.NORTH, 0, -1))));
    models.put("ae2:block/drive/cells/16k_fluid_cell",
        model(false,
            element(point(0F, 0F, 0F), point(6F, 2F, 2F), rotation(9F, 8F, 8F, Axis.Y, 0F, false),
                face(Direction.NORTH, uv(6F, 4F, 12F, 6F), "ae2:block/drive/drive_cells", Direction.NORTH, 0, -1),
                face(Direction.UP, uv(12F, 4F, 6F, 6F), "ae2:block/drive/drive_cells", Direction.NORTH, 0, -1),
                face(Direction.DOWN, uv(12F, 4F, 6F, 6F), "ae2:block/drive/drive_cells", Direction.NORTH, 0, -1))));
    models.put("ae2:block/drive/cells/64k_fluid_cell",
        model(false,
            element(point(0F, 0F, 0F), point(6F, 2F, 2F), rotation(9F, 8F, 8F, Axis.Y, 0F, false),
                face(Direction.NORTH, uv(6F, 6F, 12F, 8F), "ae2:block/drive/drive_cells", Direction.NORTH, 0, -1),
                face(Direction.UP, uv(12F, 6F, 6F, 8F), "ae2:block/drive/drive_cells", Direction.NORTH, 0, -1),
                face(Direction.DOWN, uv(12F, 6F, 6F, 8F), "ae2:block/drive/drive_cells", Direction.NORTH, 0, -1))));
    models.put("ae2:block/drive/cells/256k_fluid_cell",
        model(false,
            element(point(0F, 0F, 0F), point(6F, 2F, 2F), rotation(9F, 8F, 8F, Axis.Y, 0F, false),
                face(Direction.NORTH, uv(6F, 8F, 12F, 10F), "ae2:block/drive/drive_cells", Direction.NORTH, 0, -1),
                face(Direction.UP, uv(12F, 8F, 6F, 10F), "ae2:block/drive/drive_cells", Direction.NORTH, 0, -1),
                face(
                    Direction.DOWN, uv(12F, 8F, 6F, 10F), "ae2:block/drive/drive_cells", Direction.NORTH, 0, -1))));
    models.put("ae2:block/drive/cells/creative_cell",
        model(false,
            element(point(0F, 0F, 0F), point(6F, 2F, 2F), rotation(9F, 8F, 8F, Axis.Y, 0F, false),
                face(Direction.NORTH, uv(0F, 12F, 6F, 14F), "ae2:block/drive/drive_cells", Direction.NORTH, 0, -1),
                face(Direction.UP, uv(6F, 12F, 0F, 14F), "ae2:block/drive/drive_cells", Direction.NORTH, 0, -1),
                face(
                    Direction.DOWN, uv(6F, 12F, 0F, 14F), "ae2:block/drive/drive_cells", Direction.NORTH, 0, -1))));
    return Map.copyOf(models);
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

  private static Element element(Vector3f from, Vector3f to, Rotation rotation, FaceEntry... entries) {
    EnumMap<Direction, Face> faces = new EnumMap<>(Direction.class);
    for (FaceEntry entry : entries) {
      faces.put(entry.direction(), entry.face());
    }
    return rotation == null ? new Element(from, to, faces) : new Element(from, to, rotation, faces);
  }

  private static FaceEntry face(
      Direction direction, Vector4f uv, String texture, Direction cullface, int rotation, int tint) {
    return new FaceEntry(
        direction, new Face(uv, new TextureVariable(new ResourcePath<Texture>(texture)), cullface, rotation, tint));
  }

  private static Vector3f point(float x, float y, float z) {
    return new Vector3f(x, y, z);
  }

  private static Vector4f uv(float minU, float minV, float maxU, float maxV) {
    return new Vector4f(minU, minV, maxU, maxV);
  }

  private static Rotation rotation(
      float originX, float originY, float originZ, Axis axis, float angle, boolean rescale) {
    return new Rotation(new Vector3f(originX, originY, originZ), axis, angle, rescale);
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

  private record FaceEntry(Direction direction, Face face) {}
}
