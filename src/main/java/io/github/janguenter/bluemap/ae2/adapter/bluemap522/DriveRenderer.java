/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.adapter.bluemap522;

import de.bluecolored.bluemap.core.map.TextureGallery;
import de.bluecolored.bluemap.core.map.hires.MaxCapacityReachedException;
import de.bluecolored.bluemap.core.map.hires.RenderSettings;
import de.bluecolored.bluemap.core.map.hires.TileModelView;
import de.bluecolored.bluemap.core.map.hires.block.BlockRenderer;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.Variant;
import de.bluecolored.bluemap.core.util.math.Color;
import de.bluecolored.bluemap.core.util.math.MatrixM4f;
import de.bluecolored.bluemap.core.world.BlockState;
import de.bluecolored.bluemap.core.world.block.BlockNeighborhood;
import io.github.janguenter.bluemap.ae2.activation.DriveRouteActivation;
import io.github.janguenter.bluemap.ae2.activation.ProfileActivation;
import io.github.janguenter.bluemap.ae2.diagnostics.BoundedDiagnostics;
import io.github.janguenter.bluemap.ae2.model.Direction6;
import io.github.janguenter.bluemap.ae2.model.DriveCellDefinition;
import io.github.janguenter.bluemap.ae2.model.DriveCellOwner;
import io.github.janguenter.bluemap.ae2.model.DriveCellRouteAccess;
import io.github.janguenter.bluemap.ae2.model.DriveDecodeResult;
import io.github.janguenter.bluemap.ae2.model.DriveDecoder;
import io.github.janguenter.bluemap.ae2.model.DriveInventoryProjection;
import io.github.janguenter.bluemap.ae2.model.DriveSnapshot;

import java.util.Locale;

/** Exact-profile renderer for the bounded AE2 19.2.17 M3a drive slice. */
final class DriveRenderer implements BlockRenderer {

    private final ResourcePack resourcePack;
    private final ProfileActivation profileActivation;
    private final DriveRouteActivation driveActivation;
    private final DriveRenderSupport renderSupport;
    private final ResourceValidator resourceValidator;
    private final DriveCellRouteAccess cellRoutes;
    private final ExtensionDriveResourceValidator extensionResourceValidator;
    private final DriveDecoder decoder;
    private Boolean resourcesSupported;

    DriveRenderer(
            ResourcePack resourcePack,
            TextureGallery textureGallery,
            RenderSettings renderSettings,
            ProfileActivation profileActivation,
            DriveRouteActivation driveActivation
    ) {
        this(
                resourcePack,
                textureGallery,
                renderSettings,
                profileActivation,
                driveActivation,
                M3DriveResourceModels::resourcesSupported,
                new ExtensionDriveCellRouteAccess(M45Adapter.runtime()),
                ExtensionDriveResourceModels::supported
        );
    }

    DriveRenderer(
            ResourcePack resourcePack,
            TextureGallery textureGallery,
            RenderSettings renderSettings,
            ProfileActivation profileActivation,
            DriveRouteActivation driveActivation,
            ResourceValidator resourceValidator
    ) {
        this(
                resourcePack,
                textureGallery,
                renderSettings,
                profileActivation,
                driveActivation,
                resourceValidator,
                DriveCellRouteAccess.NONE,
                ExtensionDriveResourceModels::supported
        );
    }

    DriveRenderer(
            ResourcePack resourcePack,
            TextureGallery textureGallery,
            RenderSettings renderSettings,
            ProfileActivation profileActivation,
            DriveRouteActivation driveActivation,
            ResourceValidator resourceValidator,
            DriveCellRouteAccess cellRoutes
    ) {
        this(
                resourcePack,
                textureGallery,
                renderSettings,
                profileActivation,
                driveActivation,
                resourceValidator,
                cellRoutes,
                ExtensionDriveResourceModels::supported
        );
    }

    DriveRenderer(
            ResourcePack resourcePack,
            TextureGallery textureGallery,
            RenderSettings renderSettings,
            ProfileActivation profileActivation,
            DriveRouteActivation driveActivation,
            ResourceValidator resourceValidator,
            DriveCellRouteAccess cellRoutes,
            ExtensionDriveResourceValidator extensionResourceValidator
    ) {
        this.resourcePack = resourcePack;
        this.profileActivation = profileActivation;
        this.driveActivation = driveActivation;
        this.resourceValidator = resourceValidator;
        this.cellRoutes = java.util.Objects.requireNonNull(cellRoutes, "cellRoutes");
        this.extensionResourceValidator = java.util.Objects.requireNonNull(
                extensionResourceValidator,
                "extensionResourceValidator"
        );
        this.decoder = new DriveDecoder(cellRoutes);
        this.renderSupport = new DriveRenderSupport(
                resourcePack,
                textureGallery,
                renderSettings,
                M3DriveResourceModels.LED_TEXTURE
        );
    }

    @Override
    public void render(
            BlockNeighborhood block,
            Variant ignoredVariant,
            TileModelView tileModel,
            Color blockColor
    ) {
        int renderStart = tileModel.getStart();
        if (!profileActivation.isActive() || !driveActivation.isActive()) {
            renderOriginalSafely(block, tileModel, blockColor, renderStart);
            return;
        }

        try {
            if (!Ae2ResourceExtension.isExactDriveState(block.getBlockState())) {
                fallback(
                        BoundedDiagnostics.Event.DRIVE_UNSUPPORTED_BLOCK_STATE,
                        block,
                        tileModel,
                        blockColor,
                        renderStart
                );
                return;
            }
            Ae2DriveBlockEntityData blockEntity = block.getBlockEntity()
                    instanceof Ae2DriveBlockEntityData data ? data : null;
            if (blockEntity == null) {
                fallback(
                        BoundedDiagnostics.Event.DRIVE_MALFORMED_BLOCK_DATA,
                        block,
                        tileModel,
                        blockColor,
                        renderStart
                );
                return;
            }

            DriveDecodeResult decoded = decoder.decode(
                    blockEntity.getInventory(),
                    facing(block.getBlockState()),
                    spin(block.getBlockState())
            );
            if (!decoded.isSupported()) {
                fallback(
                        eventFor(decoded.status()),
                        block,
                        tileModel,
                        blockColor,
                        renderStart
                );
                return;
            }
            if (!hasExactResources()) {
                fallback(
                        BoundedDiagnostics.Event.DRIVE_REQUIRED_RESOURCES_MISMATCH,
                        block,
                        tileModel,
                        blockColor,
                        renderStart
                );
                return;
            }

            DriveSnapshot snapshot = decoded.supportedSnapshot().orElseThrow();
            if (!hasExactExtensionResources(snapshot)) {
                fallback(
                        BoundedDiagnostics.Event.DRIVE_REQUIRED_RESOURCES_MISMATCH,
                        block,
                        tileModel,
                        blockColor,
                        renderStart
                );
                return;
            }

            if (!renderSnapshot(
                    snapshot,
                    block,
                    tileModel,
                    blockColor,
                    renderStart
            )) {
                fallback(
                        BoundedDiagnostics.Event.DRIVE_RENDER_FAILED,
                        block,
                        tileModel,
                        blockColor,
                        renderStart
                );
            }
        } catch (MaxCapacityReachedException exception) {
            throw exception;
        } catch (RuntimeException | LinkageError exception) {
            driveActivation.disable(DriveRouteActivation.Reason.RENDER_CALLBACK_FAILED);
            try {
                cellRoutes.blockIfNativeDriveInactive();
            } catch (RuntimeException | LinkageError ignored) {
                // Dependency reconciliation cannot replace the atomic stock fallback.
            }
            fallback(
                    BoundedDiagnostics.Event.DRIVE_RENDER_FAILED,
                    block,
                    tileModel,
                    blockColor,
                    renderStart
            );
        }
    }

    private boolean renderSnapshot(
            DriveSnapshot snapshot,
            BlockNeighborhood block,
            TileModelView tileModel,
            Color blockColor,
            int renderStart
    ) {
        float colorOpacity = 0F;
        blockColor.set(0F, 0F, 0F, 0F, true);

        Color baseColor = renderSupport.renderModel(
                block,
                M3DriveResourceModels.DRIVE_BASE,
                snapshot.orientation(),
                tileModel.getTileModel()
        );
        colorOpacity = DriveRenderSupport.addMapColor(
                blockColor,
                baseColor,
                colorOpacity
        );
        for (int slot = 0; slot < DriveInventoryProjection.SLOT_COUNT; slot++) {
            DriveCellDefinition cell = snapshot.cell(slot).orElse(null);
            if (cell == null) {
                continue;
            }
            Color cellColor;
            try {
                cellColor = renderSupport.renderCellAndOfflineLed(
                        block,
                        tileModel.getTileModel(),
                        M3DriveResourceModels.model(cell.modelId()),
                        snapshot.orientation(),
                        slot
                );
            } catch (MaxCapacityReachedException exception) {
                throw exception;
            } catch (RuntimeException | LinkageError exception) {
                if (!cell.owner().requiresExtensionRoute()) {
                    throw exception;
                }
                disableExtensionSafely(cell.owner());
                return false;
            }
            colorOpacity = DriveRenderSupport.addMapColor(
                    blockColor,
                    cellColor,
                    colorOpacity
            );
        }

        DriveRenderSupport.finishMapColor(blockColor, colorOpacity);
        tileModel.initialize(renderStart);
        return true;
    }

    static boolean ledVisible(
            boolean renderTopOnly,
            MatrixM4f orientation,
            Direction6 face
    ) {
        return DriveRenderSupport.ledVisible(renderTopOnly, orientation, face);
    }

    private boolean hasExactResources() {
        if (resourcesSupported == null) {
            resourcesSupported = resourceValidator.resourcesSupported(resourcePack);
        }
        return resourcesSupported;
    }

    private boolean hasExactExtensionResources(DriveSnapshot snapshot) {
        for (java.util.Optional<DriveCellDefinition> candidate : snapshot.cells()) {
            DriveCellDefinition cell = candidate.orElse(null);
            if (cell == null || !cell.owner().requiresExtensionRoute()) {
                continue;
            }
            try {
                if (!cellRoutes.isActive(cell.owner())) {
                    return false;
                }
                if (!extensionResourceValidator.supported(
                        resourcePack,
                        cell.itemId(),
                        cell.modelId(),
                        cell.owner()
                )) {
                    if (cell.owner() != DriveCellOwner.APPLIED_MEKANISTICS) {
                        disableExtensionSafely(cell.owner());
                    }
                    return false;
                }
            } catch (RuntimeException | LinkageError exception) {
                disableExtensionSafely(cell.owner());
                return false;
            }
        }
        return true;
    }

    private void disableExtensionSafely(DriveCellOwner owner) {
        try {
            cellRoutes.disable(owner);
        } catch (RuntimeException | LinkageError ignored) {
            // Optional route-state failure must not disable the accepted native Drive.
        }
    }

    private static Direction6 facing(BlockState state) {
        String value = state.getProperties().get("facing");
        return value == null ? null : Direction6.valueOf(value.toUpperCase(Locale.ROOT));
    }

    private static int spin(BlockState state) {
        String value = state.getProperties().get("spin");
        return value == null ? -1 : Integer.parseInt(value);
    }

    private static BoundedDiagnostics.Event eventFor(DriveDecodeResult.Status status) {
        return switch (status) {
            case UNSUPPORTED_CELL_ID -> BoundedDiagnostics.Event.DRIVE_UNSUPPORTED_CELL;
            case INVALID_FACING, INVALID_SPIN ->
                    BoundedDiagnostics.Event.DRIVE_UNSUPPORTED_BLOCK_STATE;
            default -> BoundedDiagnostics.Event.DRIVE_MALFORMED_BLOCK_DATA;
        };
    }

    private void fallback(
            BoundedDiagnostics.Event event,
            BlockNeighborhood block,
            TileModelView tileModel,
            Color blockColor,
            int renderStart
    ) {
        BoundedDiagnostics.report(event);
        tileModel.initialize(renderStart).reset();
        blockColor.set(0F, 0F, 0F, 0F, true);
        renderOriginalSafely(block, tileModel, blockColor, renderStart);
    }

    private void renderOriginalSafely(
            BlockNeighborhood block,
            TileModelView tileModel,
            Color blockColor,
            int renderStart
    ) {
        renderSupport.renderOriginalSafely(
                block,
                tileModel,
                blockColor,
                renderStart,
                BoundedDiagnostics.Event.DRIVE_RENDER_FAILED
        );
    }

    @FunctionalInterface
    interface ResourceValidator {
        boolean resourcesSupported(ResourcePack resourcePack);
    }
}
