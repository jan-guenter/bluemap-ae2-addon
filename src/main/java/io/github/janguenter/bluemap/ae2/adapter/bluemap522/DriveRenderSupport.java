/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.adapter.bluemap522;

import de.bluecolored.bluemap.core.map.TextureGallery;
import de.bluecolored.bluemap.core.map.hires.MaxCapacityReachedException;
import de.bluecolored.bluemap.core.map.hires.RenderSettings;
import de.bluecolored.bluemap.core.map.hires.TileModel;
import de.bluecolored.bluemap.core.map.hires.TileModelView;
import de.bluecolored.bluemap.core.map.hires.block.ResourceModelRenderer;
import de.bluecolored.bluemap.core.resources.ResourcePath;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.Variant;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.model.Model;
import de.bluecolored.bluemap.core.util.Key;
import de.bluecolored.bluemap.core.util.math.Color;
import de.bluecolored.bluemap.core.util.math.MatrixM4f;
import de.bluecolored.bluemap.core.world.BlockState;
import de.bluecolored.bluemap.core.world.LightData;
import de.bluecolored.bluemap.core.world.block.BlockNeighborhood;
import io.github.janguenter.bluemap.ae2.diagnostics.BoundedDiagnostics;
import io.github.janguenter.bluemap.ae2.model.Direction6;
import io.github.janguenter.bluemap.ae2.model.DriveGeometry;
import io.github.janguenter.bluemap.ae2.model.PartOrientation;

import java.util.ArrayList;
import java.util.List;

/** Shared exact mechanics for native and two-sided drive resource rendering. */
final class DriveRenderSupport {

    private static final float SIXTEENTH = 1F / 16F;

    private final ResourcePack resourcePack;
    private final TextureGallery textureGallery;
    private final RenderSettings renderSettings;
    private final ResourceModelRenderer resourceRenderer;
    private final Key ledTexture;

    DriveRenderSupport(
            ResourcePack resourcePack,
            TextureGallery textureGallery,
            RenderSettings renderSettings,
            Key ledTexture
    ) {
        this.resourcePack = resourcePack;
        this.textureGallery = textureGallery;
        this.renderSettings = renderSettings;
        this.ledTexture = ledTexture;
        this.resourceRenderer = new ResourceModelRenderer(
                resourcePack,
                textureGallery,
                renderSettings
        );
    }

    Color renderModel(
            BlockNeighborhood block,
            ResourcePath<Model> model,
            PartOrientation orientation,
            TileModel tileModel
    ) {
        return renderModel(
                block,
                orientedVariant(model, orientation),
                tileModel,
                true
        );
    }

    Color renderModelPreservingAmbientOcclusion(
            BlockNeighborhood block,
            ResourcePath<Model> model,
            PartOrientation orientation,
            TileModel tileModel
    ) {
        return renderModel(
                block,
                orientedVariant(model, orientation),
                tileModel,
                false
        );
    }

    Color renderCellAndOfflineLed(
            BlockNeighborhood block,
            TileModel tileModel,
            ResourcePath<Model> model,
            PartOrientation orientation,
            int localSlot
    ) {
        Variant variant = orientedVariant(model, orientation);
        TileModelView cellView = new TileModelView(tileModel);
        Color cellColor = new Color().set(0F, 0F, 0F, 0F, true);
        resourceRenderer.render(block, variant, cellView, cellColor);
        forceAmbientOcclusion(cellView);
        translateOrientedSlot(cellView, variant.getTransformMatrix(), localSlot);
        emitOfflineLed(block, tileModel, variant.getTransformMatrix(), localSlot);
        return cellColor;
    }

    void renderOriginalSafely(
            BlockNeighborhood block,
            TileModelView tileModel,
            Color blockColor,
            int renderStart,
            BoundedDiagnostics.Event failureEvent
    ) {
        try {
            de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.BlockState original =
                    resourcePack.getBlockStates().get(block.getBlockState().getId());
            if (renderResource(original, block.getBlockState(), block, tileModel, blockColor)) {
                return;
            }
            de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.BlockState missing =
                    resourcePack.getBlockStates().get(ResourcePack.MISSING_BLOCK_STATE);
            renderResource(missing, BlockState.MISSING, block, tileModel, blockColor);
        } catch (MaxCapacityReachedException exception) {
            throw exception;
        } catch (RuntimeException | LinkageError exception) {
            tileModel.initialize(renderStart).reset();
            blockColor.set(0F, 0F, 0F, 0F, true);
            BoundedDiagnostics.report(failureEvent);
        }
    }

    static Variant orientedVariant(
            ResourcePath<Model> model,
            PartOrientation orientation
    ) {
        return new Variant(
                new ResourcePath<>(model.getFormatted()),
                orientation.x(),
                orientation.y(),
                orientation.z()
        );
    }

    static float addMapColor(Color target, Color addition, float opacity) {
        if (addition.a <= 0F) {
            return opacity;
        }
        float updatedOpacity = Math.max(opacity, addition.a);
        target.add(addition.premultiplied());
        return updatedOpacity;
    }

    static void finishMapColor(Color blockColor, float colorOpacity) {
        if (blockColor.a > 0F) {
            blockColor.flatten().straight();
            blockColor.a = colorOpacity;
        }
    }

    static boolean ledVisible(
            boolean renderTopOnly,
            MatrixM4f orientation,
            Direction6 face
    ) {
        if (!renderTopOnly) {
            return true;
        }
        float rotatedNormalY = orientation.m10 * face.stepX()
                + orientation.m11 * face.stepY()
                + orientation.m12 * face.stepZ();
        return rotatedNormalY >= 0.01F;
    }

    private Color renderModel(
            BlockNeighborhood block,
            Variant variant,
            TileModel model,
            boolean forceAmbientOcclusion
    ) {
        TileModelView view = new TileModelView(model);
        Color color = new Color().set(0F, 0F, 0F, 0F, true);
        resourceRenderer.render(block, variant, view, color);
        if (forceAmbientOcclusion) {
            forceAmbientOcclusion(view);
        }
        return color;
    }

    private void emitOfflineLed(
            BlockNeighborhood block,
            TileModel model,
            MatrixM4f orientation,
            int localSlot
    ) {
        TileModelView ledView = new TileModelView(model);
        for (DriveGeometry.LedQuad quad : DriveGeometry.offlineUnknownLed(localSlot)) {
            if (!ledVisible(renderSettings.isRenderTopOnly(), orientation, quad.face())
                    || isLedFaceRemovedAsCave(block, orientation, quad.face())) {
                continue;
            }
            List<DriveGeometry.Position> vertices = quad.vertices();
            int first = ledView.add(2);
            int second = first + 1;
            setTriangle(model, first, vertices.get(0), vertices.get(1), vertices.get(2));
            setTriangle(model, second, vertices.get(0), vertices.get(2), vertices.get(3));
            int material = textureGallery.get(ledTexture);
            for (int face = first; face <= second; face++) {
                model.setMaterialIndex(face, material);
                model.setColor(face, 0F, 0F, 0F);
                model.setSunlight(face, 15);
                model.setBlocklight(face, 15);
                model.setAOs(face, 1F, 1F, 1F);
            }
        }
        ledView.transform(orientation);
    }

    private boolean isLedFaceRemovedAsCave(
            BlockNeighborhood block,
            MatrixM4f orientation,
            Direction6 face
    ) {
        if (!block.isRemoveIfCave()) {
            return false;
        }
        LightData centerLight = block.getLightData();
        int dx = Math.round(orientation.m00 * face.stepX()
                + orientation.m01 * face.stepY()
                + orientation.m02 * face.stepZ());
        int dy = Math.round(orientation.m10 * face.stepX()
                + orientation.m11 * face.stepY()
                + orientation.m12 * face.stepZ());
        int dz = Math.round(orientation.m20 * face.stepX()
                + orientation.m21 * face.stepY()
                + orientation.m22 * face.stepZ());
        LightData neighborLight = block.getNeighborBlock(dx, dy, dz).getLightData();
        int skyLight = Math.max(centerLight.getSkyLight(), neighborLight.getSkyLight());
        int blockLight = Math.max(centerLight.getBlockLight(), neighborLight.getBlockLight());
        int detectionLight = renderSettings.isCaveDetectionUsesBlockLight()
                ? Math.max(blockLight, skyLight)
                : skyLight;
        return detectionLight == 0;
    }

    private static void setTriangle(
            TileModel model,
            int face,
            DriveGeometry.Position first,
            DriveGeometry.Position second,
            DriveGeometry.Position third
    ) {
        model.setPositions(
                face,
                units(first.x16()), units(first.y16()), units(first.z16()),
                units(second.x16()), units(second.y16()), units(second.z16()),
                units(third.x16()), units(third.y16()), units(third.z16())
        );
        model.setUvs(face, 0F, 0F, 0F, 0F, 0F, 0F);
    }

    private static void forceAmbientOcclusion(TileModelView view) {
        TileModel model = view.getTileModel();
        int end = view.getStart() + view.getSize();
        for (int face = view.getStart(); face < end; face++) {
            model.setAOs(face, 1F, 1F, 1F);
        }
    }

    private static void translateOrientedSlot(
            TileModelView view,
            MatrixM4f orientation,
            int slot
    ) {
        DriveGeometry.SlotOrigin origin = DriveGeometry.slotOrigin(slot);
        float x = units(origin.x16());
        float y = units(origin.y16());
        float z = units(origin.z16());
        view.translate(
                orientation.m00 * x + orientation.m01 * y + orientation.m02 * z,
                orientation.m10 * x + orientation.m11 * y + orientation.m12 * z,
                orientation.m20 * x + orientation.m21 * y + orientation.m22 * z
        );
    }

    private boolean renderResource(
            de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.BlockState resource,
            BlockState state,
            BlockNeighborhood block,
            TileModelView tileModel,
            Color blockColor
    ) {
        if (resource == null) {
            return false;
        }
        int modelStart = tileModel.getStart();
        List<Variant> variants = new ArrayList<>();
        resource.forEach(state, block.getX(), block.getY(), block.getZ(), variants::add);
        if (variants.isEmpty()) {
            return false;
        }

        float colorOpacity = 0F;
        blockColor.set(0F, 0F, 0F, 0F, true);
        boolean rendered = false;
        for (Variant variant : variants) {
            if (variant.getModel().getResource(resourcePack.getModels()::get) == null) {
                continue;
            }
            Color variantColor = new Color().set(0F, 0F, 0F, 0F, true);
            resourceRenderer.render(block, variant, tileModel.initialize(), variantColor);
            rendered = true;
            colorOpacity = Math.max(colorOpacity, variantColor.a);
            blockColor.add(variantColor.premultiplied());
        }
        if (!rendered) {
            tileModel.initialize(modelStart);
            return false;
        }
        finishMapColor(blockColor, colorOpacity);
        tileModel.initialize(modelStart);
        return true;
    }

    private static float units(double coordinate16) {
        return (float) (coordinate16 * SIXTEENTH);
    }
}
