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
import de.bluecolored.bluemap.core.util.Key;
import de.bluecolored.bluemap.core.util.math.Color;
import de.bluecolored.bluemap.core.world.BlockState;
import de.bluecolored.bluemap.core.world.block.BlockNeighborhood;
import io.github.janguenter.bluemap.ae2.activation.ExtendedAeDriveRouteActivation;
import io.github.janguenter.bluemap.ae2.activation.ProfileActivation;
import io.github.janguenter.bluemap.ae2.diagnostics.BoundedDiagnostics;
import io.github.janguenter.bluemap.ae2.model.Direction6;
import io.github.janguenter.bluemap.ae2.model.DriveCellOwner;
import io.github.janguenter.bluemap.ae2.model.DriveCellRouteAccess;
import io.github.janguenter.bluemap.ae2.model.ExtendedAeDriveBayLayout;
import io.github.janguenter.bluemap.ae2.model.ExtendedAeDriveCellDefinition;
import io.github.janguenter.bluemap.ae2.model.ExtendedAeDriveDecodeResult;
import io.github.janguenter.bluemap.ae2.model.ExtendedAeDriveDecoder;
import io.github.janguenter.bluemap.ae2.model.ExtendedAeDriveInventoryProjection;
import io.github.janguenter.bluemap.ae2.model.ExtendedAeDriveSnapshot;
import io.github.janguenter.bluemap.ae2.profile.extendedae.ExtendedAe2233Profile;

import java.util.Locale;
import java.util.Set;

/** Exact-profile renderer for the bounded ExtendedAE 2.2.33 M3b Drive slice. */
final class ExtendedAeDriveRenderer implements BlockRenderer {

    private static final Key BLOCK = Key.parse(ExtendedAe2233Profile.BLOCK);
    private static final Set<String> PROPERTY_KEYS = Set.of("facing", "spin");
    private static final Set<String> FACINGS = Set.of(
            "down", "up", "north", "south", "west", "east"
    );
    private static final Set<String> SPINS = Set.of("0", "1", "2", "3");

    private final ResourcePack resourcePack;
    private final ProfileActivation profileActivation;
    private final ExtendedAeDriveRouteActivation driveActivation;
    private final DriveRenderSupport renderSupport;
    private final ResourceValidator resourceValidator;
    private final DriveCellRouteAccess cellRoutes;
    private final ExtendedAeDriveDecoder decoder;
    private Boolean resourcesSupported;

    ExtendedAeDriveRenderer(
            ResourcePack resourcePack,
            TextureGallery textureGallery,
            RenderSettings renderSettings,
            ProfileActivation profileActivation,
            ExtendedAeDriveRouteActivation driveActivation
    ) {
        this(
                resourcePack,
                textureGallery,
                renderSettings,
                profileActivation,
                driveActivation,
                M3bExtendedAeDriveResourceModels::resourcesSupported,
                new ExtensionDriveCellRouteAccess(M45Adapter.runtime())
        );
    }

    ExtendedAeDriveRenderer(
            ResourcePack resourcePack,
            TextureGallery textureGallery,
            RenderSettings renderSettings,
            ProfileActivation profileActivation,
            ExtendedAeDriveRouteActivation driveActivation,
            ResourceValidator resourceValidator
    ) {
        this(
                resourcePack,
                textureGallery,
                renderSettings,
                profileActivation,
                driveActivation,
                resourceValidator,
                DriveCellRouteAccess.NONE
        );
    }

    ExtendedAeDriveRenderer(
            ResourcePack resourcePack,
            TextureGallery textureGallery,
            RenderSettings renderSettings,
            ProfileActivation profileActivation,
            ExtendedAeDriveRouteActivation driveActivation,
            ResourceValidator resourceValidator,
            DriveCellRouteAccess cellRoutes
    ) {
        this.resourcePack = resourcePack;
        this.profileActivation = profileActivation;
        this.driveActivation = driveActivation;
        this.resourceValidator = resourceValidator;
        this.cellRoutes = java.util.Objects.requireNonNull(cellRoutes, "cellRoutes");
        this.decoder = new ExtendedAeDriveDecoder(cellRoutes);
        this.renderSupport = new DriveRenderSupport(
                resourcePack,
                textureGallery,
                renderSettings,
                M3bExtendedAeDriveResourceModels.LED_TEXTURE
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
            if (!isExactState(block.getBlockState())) {
                fallback(
                        BoundedDiagnostics.Event.EXTENDED_DRIVE_UNSUPPORTED_BLOCK_STATE,
                        block,
                        tileModel,
                        blockColor,
                        renderStart
                );
                return;
            }
            ExtendedAeDriveBlockEntityData blockEntity = block.getBlockEntity()
                    instanceof ExtendedAeDriveBlockEntityData data ? data : null;
            if (blockEntity == null) {
                fallback(
                        BoundedDiagnostics.Event.EXTENDED_DRIVE_MALFORMED_BLOCK_DATA,
                        block,
                        tileModel,
                        blockColor,
                        renderStart
                );
                return;
            }

            ExtendedAeDriveDecodeResult decoded = decoder.decode(
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
                        BoundedDiagnostics.Event.EXTENDED_DRIVE_REQUIRED_RESOURCES_MISMATCH,
                        block,
                        tileModel,
                        blockColor,
                        renderStart
                );
                return;
            }

            ExtendedAeDriveSnapshot snapshot = decoded.supportedSnapshot().orElseThrow();
            if (!hasExactExtensionResources(snapshot)) {
                fallback(
                        BoundedDiagnostics.Event.EXTENDED_DRIVE_REQUIRED_RESOURCES_MISMATCH,
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
                        BoundedDiagnostics.Event.EXTENDED_DRIVE_RENDER_FAILED,
                        block,
                        tileModel,
                        blockColor,
                        renderStart
                );
            }
        } catch (MaxCapacityReachedException exception) {
            throw exception;
        } catch (RuntimeException | LinkageError exception) {
            driveActivation.disable(
                    ExtendedAeDriveRouteActivation.Reason.RENDER_CALLBACK_FAILED
            );
            fallback(
                    BoundedDiagnostics.Event.EXTENDED_DRIVE_RENDER_FAILED,
                    block,
                    tileModel,
                    blockColor,
                    renderStart
            );
        }
    }

    static boolean isExactState(BlockState state) {
        return state != null
                && BLOCK.equals(state.getId())
                && PROPERTY_KEYS.equals(state.getProperties().keySet())
                && FACINGS.contains(state.getProperties().get("facing"))
                && SPINS.contains(state.getProperties().get("spin"));
    }

    private boolean renderSnapshot(
            ExtendedAeDriveSnapshot snapshot,
            BlockNeighborhood block,
            TileModelView tileModel,
            Color blockColor,
            int renderStart
    ) {
        float colorOpacity = 0F;
        blockColor.set(0F, 0F, 0F, 0F, true);

        Color baseColor = renderSupport.renderModel(
                block,
                M3bExtendedAeDriveResourceModels.DRIVE_BASE,
                snapshot.frontOrientation(),
                tileModel.getTileModel()
        );
        colorOpacity = DriveRenderSupport.addMapColor(
                blockColor,
                baseColor,
                colorOpacity
        );
        for (int slot = 0;
             slot < ExtendedAeDriveInventoryProjection.SLOT_COUNT;
             slot++) {
            ExtendedAeDriveCellDefinition cell = snapshot.cell(slot).orElse(null);
            if (cell == null) {
                continue;
            }
            ExtendedAeDriveBayLayout.Bay bay = snapshot.bay(slot);
            Color cellColor;
            try {
                cellColor = renderSupport.renderCellAndOfflineLed(
                        block,
                        tileModel.getTileModel(),
                        M3bExtendedAeDriveResourceModels.model(cell.modelId()),
                        bay.orientation(),
                        bay.localSlot()
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

    private boolean hasExactResources() {
        if (resourcesSupported == null) {
            resourcesSupported = resourceValidator.resourcesSupported(resourcePack);
        }
        return resourcesSupported;
    }

    private boolean hasExactExtensionResources(ExtendedAeDriveSnapshot snapshot) {
        for (java.util.Optional<ExtendedAeDriveCellDefinition> candidate
                : snapshot.cells()) {
            ExtendedAeDriveCellDefinition cell = candidate.orElse(null);
            if (cell == null || !cell.owner().requiresExtensionRoute()) {
                continue;
            }
            try {
                if (!cellRoutes.isActive(cell.owner())
                        || !ExtensionDriveResourceModels.supported(resourcePack, cell)) {
                    disableExtensionSafely(cell.owner());
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
            // Optional route-state failure must not disable the accepted Extended Drive.
        }
    }

    private static Direction6 facing(BlockState state) {
        return Direction6.valueOf(
                state.getProperties().get("facing").toUpperCase(Locale.ROOT)
        );
    }

    private static int spin(BlockState state) {
        return Integer.parseInt(state.getProperties().get("spin"));
    }

    private static BoundedDiagnostics.Event eventFor(
            ExtendedAeDriveDecodeResult.Status status
    ) {
        return switch (status) {
            case UNSUPPORTED_CELL_ID ->
                    BoundedDiagnostics.Event.EXTENDED_DRIVE_UNSUPPORTED_CELL;
            case INVALID_FACING, INVALID_SPIN ->
                    BoundedDiagnostics.Event.EXTENDED_DRIVE_UNSUPPORTED_BLOCK_STATE;
            default -> BoundedDiagnostics.Event.EXTENDED_DRIVE_MALFORMED_BLOCK_DATA;
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
                BoundedDiagnostics.Event.EXTENDED_DRIVE_RENDER_FAILED
        );
    }

    @FunctionalInterface
    interface ResourceValidator {
        boolean resourcesSupported(ResourcePack resourcePack);
    }
}
