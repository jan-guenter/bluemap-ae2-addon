/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.adapter.bluemap523;

import com.flowpowered.math.vector.Vector3f;
import com.flowpowered.math.vector.Vector4f;
import de.bluecolored.bluemap.core.resources.ResourcePath;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.model.Element;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.model.Face;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.model.Model;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.model.Rotation;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.model.TextureVariable;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.texture.AnimationMeta;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.texture.Texture;
import de.bluecolored.bluemap.core.util.Direction;
import de.bluecolored.bluemap.core.util.Key;
import de.bluecolored.bluemap.core.util.math.Axis;
import io.github.janguenter.bluemap.ae2.activation.ExtensionRouteActivation;
import io.github.janguenter.bluemap.ae2.model.DriveCellCatalog;
import io.github.janguenter.bluemap.ae2.model.DriveCellDefinition;
import io.github.janguenter.bluemap.ae2.model.DriveCellOwner;
import io.github.janguenter.bluemap.ae2.profile.appmek.AppMek163Profile;
import org.junit.jupiter.api.Test;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.UnaryOperator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AppMekResourceModelsTest {

    private static final String FIRST_MODEL =
            "appmek:block/drive/cells/chemical_storage_cell_1k";

    @Test
    void externallySuppliedSixResourceClosureMatchesEverySemanticSignature()
            throws Exception {
        ResourcePack resources = exactResources();

        assertEquals(
                AppMek163Profile.DRIVE_TEXTURE_SEMANTIC_SHA256,
                AppMekResourceModels.driveTextureSignature(resources)
        );
        assertTrue(AppMekResourceModels.driveSupported(resources));
        for (DriveCellDefinition definition : DriveCellCatalog.extensionDefinitions(
                DriveCellOwner.APPLIED_MEKANISTICS
        )) {
            assertTrue(
                    AppMekDriveModelContract.supported(
                            resources.getModels().get(
                                    M3DriveResourceModels.model(definition.modelId())
                            ),
                            definition.modelId()
                    ),
                    definition.modelId()
            );
        }
    }

    @Test
    void outputRelevantModelMutationsFailClosed() throws Exception {
        assertModelMutationRejected(model -> new Model(
                new ResourcePath<>("minecraft:block/cube"),
                Map.of()
        ));
        assertElementMutationRejected(element -> new Element(
                new Vector3f(0F, 0F, 0.125F),
                element.getTo(),
                element.getRotation(),
                element.isShade(),
                element.getLightEmission(),
                element.getFaces()
        ));
        assertElementMutationRejected(element -> new Element(
                element.getFrom(),
                element.getTo(),
                new Rotation(new Vector3f(9F, 8F, 8F), Axis.X, 0F, false),
                element.isShade(),
                element.getLightEmission(),
                element.getFaces()
        ));
        assertElementMutationRejected(element -> new Element(
                element.getFrom(), element.getTo(), element.getRotation(), false,
                element.getLightEmission(), element.getFaces()
        ));
        assertElementMutationRejected(element -> new Element(
                element.getFrom(), element.getTo(), element.getRotation(),
                element.isShade(), 1, element.getFaces()
        ));
        assertFaceMutationRejected(null);
        assertFaceMutationRejected(face -> new Face(
                new Vector4f(0F, 0F, 5F, 2F),
                face.getTexture(),
                face.getCullface(),
                face.getRotation(),
                face.getTintindex()
        ));
        assertFaceMutationRejected(face -> new Face(
                face.getUv(),
                new TextureVariable(new ResourcePath<Texture>("minecraft:block/stone")),
                face.getCullface(),
                face.getRotation(),
                face.getTintindex()
        ));
        assertFaceMutationRejected(face -> new Face(
                face.getUv(), face.getTexture(), null, face.getRotation(),
                face.getTintindex()
        ));
        assertFaceMutationRejected(face -> new Face(
                face.getUv(), face.getTexture(), face.getCullface(),
                face.getRotation(), 0
        ));
    }

    @Test
    void decodedTexturePixelAnimationAndDimensionsAreSemantic() throws Exception {
        ResourcePack pixel = exactResources();
        Texture original = pixel.getTextures().get(AppMekResourceModels.DRIVE_TEXTURE);
        pixel.getTextures().put(
                AppMekResourceModels.DRIVE_TEXTURE,
                changedPixel(original)
        );
        assertFalse(AppMekResourceModels.driveSupported(pixel));

        ResourcePack animated = exactResources();
        original = animated.getTextures().get(AppMekResourceModels.DRIVE_TEXTURE);
        animated.getTextures().put(
                AppMekResourceModels.DRIVE_TEXTURE,
                Texture.from(
                        AppMekResourceModels.DRIVE_TEXTURE,
                        original.getTextureImage(),
                        new AnimationMeta(false, 16, 16, 2, null)
                )
        );
        assertFalse(AppMekResourceModels.driveSupported(animated));

        ResourcePack dimension = exactResources();
        BufferedImage tiny = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        tiny.setRGB(0, 0, original.getTextureImage().getRGB(0, 0));
        dimension.getTextures().put(
                AppMekResourceModels.DRIVE_TEXTURE,
                Texture.from(AppMekResourceModels.DRIVE_TEXTURE, tiny)
        );
        assertFalse(AppMekResourceModels.driveSupported(dimension));
    }

    @Test
    void bakeSemanticMismatchMakesOnlyAppMekDriveRouteInactive() throws Exception {
        ResourcePack drift = exactResources();
        drift.getTextures().put(
                AppMekResourceModels.DRIVE_TEXTURE,
                changedPixel(drift.getTextures().get(AppMekResourceModels.DRIVE_TEXTURE))
        );
        M45Runtime runtime = new M45Runtime();
        runtime.route(M45Runtime.APPMEK_DRIVE_CELLS).activate("exact-appmek-mekanism");

        new M45ResourceExtension(
                drift,
                runtime,
                () -> true,
                () -> true,
                () -> true,
                () -> false
        ).bake();

        assertFalse(runtime.active(M45Runtime.APPMEK_DRIVE_CELLS));
        assertFalse(runtime.route(M45Runtime.APPMEK_DRIVE_CELLS).isDisabled());
        assertEquals(
                ExtensionRouteActivation.Reason.REQUIRED_RESOURCES_MISMATCH,
                runtime.route(M45Runtime.APPMEK_DRIVE_CELLS).snapshot().reason()
        );
        assertFalse(runtime.route(M45Runtime.APPFLUX).isDisabled());
        assertFalse(runtime.route(M45Runtime.MEGA_CELLS).isDisabled());
    }

    private static void assertModelMutationRejected(UnaryOperator<Model> mutation)
            throws Exception {
        ResourcePack resources = exactResources();
        ResourcePath<Model> path = M3DriveResourceModels.model(FIRST_MODEL);
        resources.getModels().put(path, mutation.apply(resources.getModels().get(path)));
        assertFalse(AppMekResourceModels.driveSupported(resources));
    }

    private static void assertElementMutationRejected(UnaryOperator<Element> mutation)
            throws Exception {
        assertModelMutationRejected(model -> new Model(
                model.getTextures(),
                new Element[]{mutation.apply(model.getElements()[0])},
                model.isAmbientocclusion()
        ));
    }

    private static void assertFaceMutationRejected(UnaryOperator<Face> mutation)
            throws Exception {
        assertElementMutationRejected(element -> {
            EnumMap<Direction, Face> faces = new EnumMap<>(Direction.class);
            element.getFaces().forEach((direction, face) ->
                    faces.put(direction, face.copy()));
            if (mutation == null) {
                faces.remove(Direction.NORTH);
            } else {
                faces.put(Direction.NORTH, mutation.apply(faces.get(Direction.NORTH)));
            }
            return new Element(
                    element.getFrom(),
                    element.getTo(),
                    element.getRotation(),
                    element.isShade(),
                    element.getLightEmission(),
                    faces
            );
        });
    }

    private static Texture changedPixel(Texture original) throws Exception {
        BufferedImage source = original.getTextureImage();
        BufferedImage changed = new BufferedImage(
                source.getWidth(),
                source.getHeight(),
                BufferedImage.TYPE_INT_ARGB
        );
        Graphics2D graphics = changed.createGraphics();
        try {
            graphics.drawImage(source, 0, 0, null);
        } finally {
            graphics.dispose();
        }
        changed.setRGB(0, 0, source.getRGB(0, 0) ^ 0x00010101);
        return Texture.from(AppMekResourceModels.DRIVE_TEXTURE, changed);
    }

    private static ResourcePack exactResources() throws Exception {
        return AppMekExternalResourceTestSupport.exactResources();
    }
}
