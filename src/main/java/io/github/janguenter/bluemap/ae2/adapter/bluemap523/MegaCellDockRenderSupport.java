/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.adapter.bluemap523;

import de.bluecolored.bluemap.core.map.TextureGallery;
import de.bluecolored.bluemap.core.map.hires.MaxCapacityReachedException;
import de.bluecolored.bluemap.core.map.hires.RenderSettings;
import de.bluecolored.bluemap.core.map.hires.TileModel;
import de.bluecolored.bluemap.core.map.hires.TileModelView;
import de.bluecolored.bluemap.core.map.hires.block.ResourceModelRenderer;
import de.bluecolored.bluemap.core.resources.ResourcePath;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.Variant;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.model.Element;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.model.Face;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.model.Model;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.texture.Texture;
import de.bluecolored.bluemap.core.util.Direction;
import de.bluecolored.bluemap.core.util.Key;
import de.bluecolored.bluemap.core.util.math.Color;
import de.bluecolored.bluemap.core.util.math.MatrixM4f;
import de.bluecolored.bluemap.core.world.LightData;
import de.bluecolored.bluemap.core.world.block.BlockNeighborhood;
import io.github.janguenter.bluemap.ae2.model.Direction6;
import io.github.janguenter.bluemap.ae2.model.FacePartSnapshot;
import io.github.janguenter.bluemap.ae2.model.MegaCellDockCellCatalog;
import io.github.janguenter.bluemap.ae2.model.MegaCellDockCellDefinition;
import io.github.janguenter.bluemap.ae2.model.MegaCellDockGeometry;
import io.github.janguenter.bluemap.ae2.model.MegaCellDockModel;
import io.github.janguenter.bluemap.ae2.model.MegaCellDockSnapshot;
import io.github.janguenter.bluemap.ae2.model.PartOrientation;
import io.github.janguenter.bluemap.ae2.profile.megacells.MegaCells4110Profile;

import java.util.Objects;

/** BlueMap 5.22 projection of the dynamic layer of one exact MEGA Cell Dock. */
final class MegaCellDockRenderSupport {

    private static final float HALF = 0.5F;
    private static final Key LED_CARRIER_TEXTURE =
            Key.parse("megacells:block/drive/cells/standard_cell");

    private final ResourcePack resourcePack;
    private final TextureGallery textureGallery;
    private final RenderSettings renderSettings;
    private final ResourceModelRenderer resourceRenderer;

    MegaCellDockRenderSupport(
            ResourcePack resourcePack,
            TextureGallery textureGallery,
            RenderSettings renderSettings
    ) {
        this.resourcePack = Objects.requireNonNull(resourcePack, "resourcePack");
        this.textureGallery = Objects.requireNonNull(textureGallery, "textureGallery");
        this.renderSettings = Objects.requireNonNull(renderSettings, "renderSettings");
        this.resourceRenderer = new ResourceModelRenderer(
                resourcePack,
                textureGallery,
                renderSettings
        );
    }

    /** Empty docks need no dynamic resources; unknown cells require atomic stock fallback. */
    boolean resourcesSupported(FacePartSnapshot part) {
        try {
            Resolution resolution = resolve(part);
            return switch (resolution.status()) {
                case EMPTY -> true;
                case UNSUPPORTED -> false;
                case OCCUPIED -> exactModelAvailable(resolution.cell());
            };
        } catch (MaxCapacityReachedException exception) {
            throw exception;
        } catch (RuntimeException | LinkageError exception) {
            throw RouteFailure.causedBy(exception);
        }
    }

    /** Emits only the chassis and two static black LEDs; the caller already emitted the body. */
    Color renderDynamic(
            Direction6 side,
            FacePartSnapshot part,
            BlockNeighborhood block,
            TileModel tileModel
    ) {
        try {
            Resolution resolution = resolve(part);
            if (resolution.status() == Status.EMPTY) {
                return transparent();
            }
            if (resolution.status() != Status.OCCUPIED
                    || !exactModelAvailable(resolution.cell())) {
                throw new IllegalStateException(
                        "preflighted MEGA Cell Dock resources disappeared"
                );
            }

            MegaCellDockSnapshot snapshot = MegaCellDockSnapshot.occupied(
                    Objects.requireNonNull(side, "side"),
                    part.spin(),
                    resolution.cell()
            );
            MegaCellDockModel dynamic = MegaCellDockGeometry.model(snapshot);
            MegaCellDockModel.ModelPlacement chassis =
                    dynamic.cellChassis().orElseThrow();
            Color chassisColor = renderChassis(block, tileModel, chassis);
            for (MegaCellDockModel.LedPlacement led : dynamic.offlineLeds()) {
                renderLed(block, tileModel, led);
            }
            return chassisColor;
        } catch (MaxCapacityReachedException exception) {
            throw exception;
        } catch (RouteFailure failure) {
            throw failure;
        } catch (RuntimeException | LinkageError exception) {
            throw RouteFailure.causedBy(exception);
        }
    }

    static Resolution resolve(FacePartSnapshot part) {
        Objects.requireNonNull(part, "part");
        if (!MegaCells4110Profile.CELL_DOCK_PART.equals(part.id())) {
            throw new IllegalArgumentException("part is not the exact MEGA Cell Dock");
        }
        if (part.cellItemId() == null) {
            return new Resolution(Status.EMPTY, null);
        }
        MegaCellDockCellDefinition cell = MegaCellDockCellCatalog.find(part.cellItemId())
                .orElse(null);
        return cell == null
                ? new Resolution(Status.UNSUPPORTED, null)
                : new Resolution(Status.OCCUPIED, cell);
    }

    /** Equivalent BlueMap orientation for AE2 {@code BlockOrientation.get(front, top)}. */
    static PartOrientation orientationFor(MegaCellDockGeometry.Orientation orientation) {
        Objects.requireNonNull(orientation, "orientation");
        for (int spin = 0; spin < 4; spin++) {
            if (MegaCellDockGeometry.upFromSpin(orientation.front(), spin)
                    == orientation.top()) {
                return PartOrientation.forPart(orientation.front(), spin);
            }
        }
        throw new IllegalArgumentException("orientation has no exact AE2 spin");
    }

    /** Exact source matrix: {@code T(center) * BlockOrientation * T(local)}. */
    static MatrixM4f sourceTransform(MegaCellDockGeometry.Transform transform) {
        Objects.requireNonNull(transform, "transform");
        PartOrientation orientation = orientationFor(transform.orientation());
        MegaCellDockGeometry.Position local = transform.localTranslation();
        MegaCellDockGeometry.Position center = transform.centerTranslation();
        return new MatrixM4f()
                .translate((float) local.x(), (float) local.y(), (float) local.z())
                .rotateYXZ(-orientation.x(), -orientation.y(), -orientation.z())
                .translate((float) center.x(), (float) center.y(), (float) center.z());
    }

    /**
     * Translation which turns BlueMap's centered variant rotation into the exact source matrix.
     */
    static MegaCellDockGeometry.Position postVariantTranslation(
            MegaCellDockGeometry.Transform transform
    ) {
        Objects.requireNonNull(transform, "transform");
        MatrixM4f variant = variantFor(transform).getTransformMatrix();
        MegaCellDockGeometry.Position center = transform.centerTranslation();
        MegaCellDockGeometry.Position local = transform.localTranslation();
        double x = local.x() + HALF;
        double y = local.y() + HALF;
        double z = local.z() + HALF;
        return new MegaCellDockGeometry.Position(
                center.x() - HALF + variant.m00 * x + variant.m01 * y + variant.m02 * z,
                center.y() - HALF + variant.m10 * x + variant.m11 * y + variant.m12 * z,
                center.z() - HALF + variant.m20 * x + variant.m21 * y + variant.m22 * z
        );
    }

    private Color renderChassis(
            BlockNeighborhood block,
            TileModel tileModel,
            MegaCellDockModel.ModelPlacement placement
    ) {
        Variant variant = variantFor(placement.transform(), placement.modelId());
        TileModelView chassisView = new TileModelView(tileModel);
        Color color = transparent();
        resourceRenderer.render(block, variant, chassisView, color);
        MegaCellDockGeometry.Position translation =
                postVariantTranslation(placement.transform());
        chassisView.translate(
                (float) translation.x(),
                (float) translation.y(),
                (float) translation.z()
        );
        return color;
    }

    private void renderLed(
            BlockNeighborhood block,
            TileModel tileModel,
            MegaCellDockModel.LedPlacement placement
    ) {
        MatrixM4f transform = sourceTransform(placement.transform());
        TileModelView ledView = new TileModelView(tileModel);
        for (MegaCellDockGeometry.LedQuad quad : placement.quads()) {
            if (!ledVisible(transform, quad.face())
                    || isRemovedAsCave(block, transform, quad.face())) {
                continue;
            }
            int first = ledView.add(2);
            setTriangle(
                    tileModel,
                    first,
                    quad.vertices().get(0),
                    quad.vertices().get(1),
                    quad.vertices().get(2)
            );
            setTriangle(
                    tileModel,
                    first + 1,
                    quad.vertices().get(0),
                    quad.vertices().get(2),
                    quad.vertices().get(3)
            );
            int material = textureGallery.get(LED_CARRIER_TEXTURE);
            for (int face = first; face <= first + 1; face++) {
                tileModel.setMaterialIndex(face, material);
                tileModel.setColor(face, 0F, 0F, 0F);
                tileModel.setSunlight(face, 15);
                tileModel.setBlocklight(face, 15);
                tileModel.setAOs(face, 1F, 1F, 1F);
            }
        }
        ledView.transform(transform);
    }

    private boolean exactModelAvailable(MegaCellDockCellDefinition cell) {
        if (cell == null || resourcePack.getTextures().get(LED_CARRIER_TEXTURE) == null) {
            return false;
        }
        Model model = resolvedModel(cell.modelId());
        if (model == null) {
            return false;
        }
        int faceCount = 0;
        for (Element element : model.getElements()) {
            if (element == null) {
                return false;
            }
            for (Direction direction : Direction.values()) {
                Face face = element.getFaces().get(direction);
                if (face == null) {
                    continue;
                }
                ResourcePath<Texture> texture = face.getTexture().getTexturePath(
                        model.getTextures()::get
                );
                if (texture == null
                        || texture.getResource(resourcePack.getTextures()::get) == null) {
                    return false;
                }
                faceCount++;
            }
        }
        return faceCount * 2 == cell.chassisKind().nominalTriangles();
    }

    private Model resolvedModel(String modelId) {
        Model model = resourcePack.getModels().get(new ResourcePath<Model>(modelId));
        if (model == null) {
            return null;
        }
        model.applyParent(resourcePack.getModels());
        if (model.getElements() == null) {
            return null;
        }
        model.calculateProperties(resourcePack.getTextures());
        return model;
    }

    private boolean ledVisible(MatrixM4f transform, Direction6 face) {
        if (!renderSettings.isRenderTopOnly()) {
            return true;
        }
        return transform.m10 * face.stepX()
                + transform.m11 * face.stepY()
                + transform.m12 * face.stepZ() >= 0.01F;
    }

    private boolean isRemovedAsCave(
            BlockNeighborhood block,
            MatrixM4f transform,
            Direction6 face
    ) {
        if (!block.isRemoveIfCave()) {
            return false;
        }
        int dx = Math.round(transform.m00 * face.stepX()
                + transform.m01 * face.stepY()
                + transform.m02 * face.stepZ());
        int dy = Math.round(transform.m10 * face.stepX()
                + transform.m11 * face.stepY()
                + transform.m12 * face.stepZ());
        int dz = Math.round(transform.m20 * face.stepX()
                + transform.m21 * face.stepY()
                + transform.m22 * face.stepZ());
        LightData center = block.getLightData();
        LightData neighbor = block.getNeighborBlock(dx, dy, dz).getLightData();
        int skyLight = Math.max(center.getSkyLight(), neighbor.getSkyLight());
        int blockLight = Math.max(center.getBlockLight(), neighbor.getBlockLight());
        int detectionLight = renderSettings.isCaveDetectionUsesBlockLight()
                ? Math.max(skyLight, blockLight) : skyLight;
        return detectionLight == 0;
    }

    private static Variant variantFor(MegaCellDockGeometry.Transform transform) {
        return variantFor(transform, ResourcePack.MISSING_BLOCK_MODEL.getFormatted());
    }

    private static Variant variantFor(
            MegaCellDockGeometry.Transform transform,
            String modelId
    ) {
        PartOrientation orientation = orientationFor(transform.orientation());
        return new Variant(
                new ResourcePath<Model>(modelId),
                orientation.x(),
                orientation.y(),
                orientation.z()
        );
    }

    private static void setTriangle(
            TileModel model,
            int face,
            MegaCellDockGeometry.Position first,
            MegaCellDockGeometry.Position second,
            MegaCellDockGeometry.Position third
    ) {
        model.setPositions(
                face,
                (float) first.x(), (float) first.y(), (float) first.z(),
                (float) second.x(), (float) second.y(), (float) second.z(),
                (float) third.x(), (float) third.y(), (float) third.z()
        );
        model.setUvs(face, 0F, 0F, 0F, 0F, 0F, 0F);
    }

    private static Color transparent() {
        return new Color().set(0F, 0F, 0F, 0F, true);
    }

    enum Status {
        EMPTY,
        OCCUPIED,
        UNSUPPORTED
    }

    record Resolution(Status status, MegaCellDockCellDefinition cell) {

        Resolution {
            Objects.requireNonNull(status, "status");
            if ((status == Status.OCCUPIED) != (cell != null)) {
                throw new IllegalArgumentException(
                        "only an occupied Cell Dock resolution may contain a cell"
                );
            }
        }
    }

    static final class RouteFailure extends RuntimeException {

        private static final long serialVersionUID = 1L;

        private RouteFailure(Throwable cause) {
            super("MEGA Cell Dock render callback failed", cause);
        }

        static RouteFailure causedBy(Throwable cause) {
            return new RouteFailure(Objects.requireNonNull(cause, "cause"));
        }
    }
}
