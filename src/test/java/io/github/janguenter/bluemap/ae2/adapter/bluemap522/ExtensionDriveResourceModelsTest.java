/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.adapter.bluemap522;

import com.flowpowered.math.vector.Vector3f;
import de.bluecolored.bluemap.core.resources.pack.PackVersion;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.model.Element;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.model.Model;
import de.bluecolored.bluemap.core.util.Direction;
import io.github.janguenter.bluemap.ae2.model.DriveCellCatalog;
import io.github.janguenter.bluemap.ae2.model.DriveCellDefinition;
import io.github.janguenter.bluemap.ae2.model.DriveCellOwner;
import io.github.janguenter.bluemap.ae2.model.ExtendedAeDriveCellDefinition;
import io.github.janguenter.bluemap.ae2.model.MegaCellDockCellCatalog;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.EnumMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExtensionDriveResourceModelsTest {

    private static final Field AMBIENT_OCCLUSION = ambientOcclusionField();

    @Test
    void acceptsEveryExactExtensionChassisShapeAfterItsProfileGate() throws Exception {
        ResourcePack resources = new ResourcePack(new PackVersion(34, 0));
        for (DriveCellDefinition definition : DriveCellCatalog.extensionDefinitions(
                DriveCellOwner.APPLIED_FLUX
        )) {
            resources.getModels().put(
                    M3DriveResourceModels.model(definition.modelId()),
                    model(1)
            );
        }
        for (DriveCellDefinition definition : DriveCellCatalog.extensionDefinitions(
                DriveCellOwner.MEGA_CELLS
        )) {
            int elements = MegaCellDockCellCatalog.require(definition.itemId())
                    .chassisKind().nominalTriangles() / 6;
            resources.getModels().put(
                    M3DriveResourceModels.model(definition.modelId()),
                    model(elements)
            );
        }

        for (DriveCellOwner owner : new DriveCellOwner[]{
                DriveCellOwner.APPLIED_FLUX,
                DriveCellOwner.MEGA_CELLS
        }) {
            for (DriveCellDefinition definition
                    : DriveCellCatalog.extensionDefinitions(owner)) {
                assertTrue(
                        ExtensionDriveResourceModels.supported(resources, definition),
                        definition.itemId()
                );
                assertTrue(
                        ExtensionDriveResourceModels.supported(
                                resources,
                                new ExtendedAeDriveCellDefinition(
                                        definition.itemId(),
                                        definition.modelId()
                                )
                        ),
                        definition.itemId()
                );
            }
        }
    }

    @Test
    void rejectsMissingWrongShapeAmbientAndCoreModels() throws Exception {
        DriveCellDefinition appFlux = DriveCellCatalog.extensionDefinitions(
                DriveCellOwner.APPLIED_FLUX
        ).getFirst();
        DriveCellDefinition mega = DriveCellCatalog.extensionDefinitions(
                DriveCellOwner.MEGA_CELLS
        ).stream().filter(definition -> MegaCellDockCellCatalog.require(
                definition.itemId()
        ).chassisKind().nominalTriangles() == 12).findFirst().orElseThrow();

        ResourcePack resources = new ResourcePack(new PackVersion(34, 0));
        assertFalse(ExtensionDriveResourceModels.supported(resources, appFlux));

        resources.getModels().put(
                M3DriveResourceModels.model(appFlux.modelId()),
                model(2)
        );
        assertFalse(ExtensionDriveResourceModels.supported(resources, appFlux));

        resources.getModels().put(
                M3DriveResourceModels.model(mega.modelId()),
                model(1)
        );
        assertFalse(ExtensionDriveResourceModels.supported(resources, mega));

        Model ambient = model(1);
        AMBIENT_OCCLUSION.set(ambient, true);
        resources.getModels().put(
                M3DriveResourceModels.model(appFlux.modelId()),
                ambient
        );
        assertFalse(ExtensionDriveResourceModels.supported(resources, appFlux));
        assertFalse(ExtensionDriveResourceModels.supported(
                resources,
                DriveCellCatalog.require("ae2:item_storage_cell_1k")
        ));
    }

    private static Model model(int elementCount) throws IllegalAccessException {
        Element[] elements = new Element[elementCount];
        for (int index = 0; index < elementCount; index++) {
            elements[index] = new Element(
                    Vector3f.ZERO,
                    new Vector3f(6F, 2F, 2F),
                    new EnumMap<>(Direction.class)
            );
        }
        Model model = new Model(Map.of(), elements);
        AMBIENT_OCCLUSION.set(model, false);
        return model;
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
