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
import de.bluecolored.bluemap.core.resources.pack.resourcepack.model.TextureVariable;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.texture.Texture;
import de.bluecolored.bluemap.core.util.Direction;
import io.github.janguenter.bluemap.ae2.model.DriveCellCatalog;
import io.github.janguenter.bluemap.ae2.model.DriveCellDefinition;
import io.github.janguenter.bluemap.ae2.model.DriveCellOwner;
import io.github.janguenter.bluemap.ae2.model.MegaCellDockCellCatalog;

import java.lang.reflect.Field;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/** Minimal exact-shape fixtures shared by both Drive renderer integration tests. */
final class ExtensionDriveRendererTestSupport {

    private static final Field AMBIENT_OCCLUSION = ambientOcclusionField();

    private ExtensionDriveRendererTestSupport() {
    }

    static DriveCellDefinition representative(DriveCellOwner owner) {
        List<DriveCellDefinition> definitions = DriveCellCatalog.extensionDefinitions(owner);
        if (owner == DriveCellOwner.APPLIED_FLUX) {
            return definitions.getFirst();
        }
        return definitions.stream()
                .filter(definition -> MegaCellDockCellCatalog.require(definition.itemId())
                        .chassisKind().nominalTriangles() == 6)
                .findFirst()
                .orElseThrow();
    }

    static void putExactShapeModel(
            ResourcePack resourcePack,
            DriveCellDefinition definition
    ) throws IllegalAccessException {
        EnumMap<Direction, Face> faces = new EnumMap<>(Direction.class);
        for (Direction direction : List.of(
                Direction.NORTH,
                Direction.UP,
                Direction.DOWN
        )) {
            faces.put(direction, new Face(
                    new Vector4f(0F, 0F, 16F, 16F),
                    new TextureVariable(new ResourcePath<Texture>(
                            "ae2:block/drive/drive_cells"
                    )),
                    Direction.NORTH,
                    0,
                    -1
            ));
        }
        Model model = new Model(
                Map.of(),
                new Element[]{new Element(
                        Vector3f.ZERO,
                        new Vector3f(6F, 2F, 2F),
                        faces
                )}
        );
        AMBIENT_OCCLUSION.set(model, false);
        resourcePack.getModels().put(
                M3DriveResourceModels.model(definition.modelId()),
                model
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
}
