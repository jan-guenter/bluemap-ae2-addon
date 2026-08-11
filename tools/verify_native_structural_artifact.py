#!/usr/bin/env python3
# SPDX-License-Identifier: LGPL-3.0-only
"""Verify exact AE2/MC runtime/source evidence and the S1 structural profile."""

from __future__ import annotations

import argparse
import hashlib
import json
from pathlib import Path
import sys
import tomllib
import zipfile

from ae2_native_structural_contract import (
    CURRENT_HOST_EVIDENCE,
    ENDPOINTS,
    FACADE_WHITELIST_BLOCK_IDS,
    FACADE_WHITELIST_BLOCKSTATE_SHA256,
    FACADE_WHITELIST_OPTIONAL_TAGS,
    FACADE_WHITELIST_RESOURCE_SHA256,
    FULL_PACK_OVERRIDE_EVIDENCE,
    EXPECTED_FROZEN_OUTPUT_SHA256,
    EXPECTED_PROFILE_SHA256,
    EXPECTED_RESOURCE_MANIFEST_SHA256,
    EXPECTED_RESOURCE_SIZES_MANIFEST_SHA256,
    EXPECTED_SHA1,
    EXPECTED_SHA256,
    EXPECTED_SHA512,
    EXPECTED_SIZE,
    EXPECTED_SOURCES_SHA1,
    EXPECTED_SOURCES_SHA256,
    EXPECTED_SOURCES_SHA512,
    EXPECTED_SOURCES_SIZE,
    FACE_PARTS,
    KNOWN_EXTENSION_FALLBACK_CONTROL,
    NATIVE_FACADE_NEUTRAL_MATERIALS,
    REQUIRED_RESOURCE_COUNT,
    ROUTE_RESOURCE_ROOT,
    UNSUPPORTED_COMPATIBLE_ENDPOINT_ARTIFACTS,
    UNSUPPORTED_COMPATIBLE_ENDPOINTS,
    checksum_manifest,
    parse_checksum_manifest,
    parse_size_manifest,
    profile,
    profile_bytes,
    resource_rows,
    size_manifest,
)
from verify_pinned_artifact import verify_metadata


MINECRAFT_CLIENT_SIZE = 26_836_906
MINECRAFT_CLIENT_SHA1 = "30c73b1c5da787909b2f73340419fdf13b9def88"
MINECRAFT_CLIENT_SHA256 = (
    "499f6897d1837516680f3114072d8106e11c9adcd933fe5cf051b551089b0c99"
)
MINECRAFT_CLIENT_SHA512 = (
    "a63c09e9b8cfe80bd5815c88818291d54cbe7c9ffcd806be6365376e865c44d5a"
    "a85e8348467e6ded8f4d9722047d3c5b075f755047a982c2e75223d2b9f68ac"
)

_GLASSENTIAL_ARTIFACT = FULL_PACK_OVERRIDE_EVIDENCE["artifact"]
GLASSENTIAL_SIZE = _GLASSENTIAL_ARTIFACT["sizeBytes"]
GLASSENTIAL_SHA1 = _GLASSENTIAL_ARTIFACT["sha1"]
GLASSENTIAL_SHA256 = _GLASSENTIAL_ARTIFACT["sha256"]
GLASSENTIAL_SHA512 = _GLASSENTIAL_ARTIFACT["sha512"]
GLASSENTIAL_RESOURCES = {
    resource["path"]: (resource["sizeBytes"], resource["sha256"])
    for resource in FULL_PACK_OVERRIDE_EVIDENCE["resources"]
}

_BLUEMAP_HOST_ARTIFACT = CURRENT_HOST_EVIDENCE["artifact"]
BLUEMAP_HOST_SIZE = _BLUEMAP_HOST_ARTIFACT["sizeBytes"]
BLUEMAP_HOST_SHA1 = _BLUEMAP_HOST_ARTIFACT["sha1"]
BLUEMAP_HOST_SHA256 = _BLUEMAP_HOST_ARTIFACT["sha256"]
BLUEMAP_HOST_SHA512 = _BLUEMAP_HOST_ARTIFACT["sha512"]
BLUEMAP_HOST_RESOURCES = {
    resource["path"]: (resource["sizeBytes"], resource["sha256"])
    for resource in CURRENT_HOST_EVIDENCE["embeddedEvidence"]
}


SUPPORT_SOURCE_CLASSES = (
    "appeng.api.ids.AEPartIds",
    "appeng.api.parts.IPart",
    "appeng.api.util.AECableType",
    "appeng.blockentity.grid.AENetworkedBlockEntity",
    "appeng.client.render.cablebus.CableBusBakedModel",
    "appeng.client.render.cablebus.CableBusRenderState",
    "appeng.client.render.cablebus.FacadeBuilder",
    "appeng.client.render.cablebus.FacadeRenderState",
    "appeng.decorative.solid.QuartzGlassBlock",
    "appeng.facade.FacadeContainer",
    "appeng.facade.FacadePart",
    "appeng.items.parts.FacadeItem",
    "appeng.thirdparty.codechicken.lib.model.pipeline.transformers.QuadCornerKicker",
    "appeng.thirdparty.codechicken.lib.model.pipeline.transformers.QuadReInterpolator",
    "appeng.thirdparty.codechicken.lib.math.InterpHelper",
    "appeng.core.definitions.AEBlockEntities",
    "appeng.core.definitions.AEParts",
    "appeng.me.cluster.implementations.CraftingCPUCalculator",
    "appeng.me.cluster.implementations.QuantumCalculator",
    "appeng.me.cluster.implementations.SpatialPylonCalculator",
    "appeng.me.helpers.IGridConnectedBlockEntity",
    "appeng.parts.PartModel",
    "appeng.parts.automation.PlaneModels",
    "appeng.parts.automation.PlaneBakedModel",
    "appeng.parts.automation.AbstractLevelEmitterPart",
    "appeng.parts.automation.PlaneConnectionHelper",
    "appeng.parts.automation.PlaneConnections",
    "appeng.parts.BusCollisionHelper",
    "appeng.parts.CableBusContainer",
    "appeng.parts.p2p.P2PModels",
    "appeng.parts.reporting.AbstractReportingPart",
)

EXPECTED_SOURCE_MARKERS = {
    "appeng/api/util/AECableType.java": (
        b"COVERED(AECableVariant.COVERED, AECableSize.NORMAL)",
        b"SMART(AECableVariant.SMART, AECableSize.NORMAL)",
        b"DENSE_SMART(AECableVariant.SMART, AECableSize.DENSE)",
        b"public static AECableType min(AECableType a, AECableType b)",
    ),
    "appeng/blockentity/grid/AENetworkedBlockEntity.java": (
        b"public AECableType getCableConnectionType(Direction dir)",
        b"return AECableType.SMART;",
    ),
    "appeng/core/definitions/AEParts.java": (
        b"public static final ItemDefinition<PartItem<QuartzFiberPart>> QUARTZ_FIBER",
        b"public static final ItemDefinition<PartItem<LightP2PTunnelPart>> LIGHT_P2P_TUNNEL",
        b"PartModels.registerModels(PartModelsHelper.createModels(partClass));",
    ),
    "appeng/core/definitions/AEBlockEntities.java": (
        b'INSCRIBER = create("inscriber"',
        b'QUANTUM_BRIDGE = create("quantum_ring"',
        b'AEBlocks.QUANTUM_RING,\n            AEBlocks.QUANTUM_LINK',
        b'CRAFTING_UNIT = create("crafting_unit"',
        b'AEBlocks.CRAFTING_UNIT,\n            AEBlocks.CRAFTING_ACCELERATOR',
        b'CRAFTING_STORAGE = create("crafting_storage"',
        b'MOLECULAR_ASSEMBLER = create(',
    ),
    "appeng/me/helpers/IGridConnectedBlockEntity.java": (
        b"default Set<Direction> getGridConnectableSides(BlockOrientation orientation)",
        b"return EnumSet.allOf(Direction.class);",
        b"inWorldGridNode.isExposedOnSide(dir)",
    ),
    "appeng/blockentity/misc/InscriberBlockEntity.java": (
        b"EnumSet.complementOf(EnumSet.of(orientation.getSide(RelativeSide.FRONT)))",
        b"return AECableType.COVERED;",
    ),
    "appeng/blockentity/networking/WirelessAccessPointBlockEntity.java": (
        b"EnumSet.of(orientation.getSide(RelativeSide.BACK))",
        b"return AECableType.SMART;",
    ),
    "appeng/blockentity/misc/ChargerBlockEntity.java": (
        b"EnumSet.complementOf(EnumSet.of(orientation.getSide(RelativeSide.FRONT)))",
        b"return AECableType.COVERED;",
    ),
    "appeng/blockentity/qnb/QuantumBridgeBlockEntity.java": (
        b"if (!isFormed())",
        b"if (this.isCorner() || this.isCenter())",
        b"return this.getAdjacentQuantumBridges();",
        b"return AECableType.DENSE_SMART;",
    ),
    "appeng/blockentity/spatial/SpatialPylonBlockEntity.java": (
        b"this.cluster == null ? EnumSet.noneOf(Direction.class) : EnumSet.allOf(Direction.class)",
    ),
    "appeng/blockentity/storage/DriveBlockEntity.java": (
        b"EnumSet.complementOf(EnumSet.of(orientation.getSide(RelativeSide.FRONT)))",
        b"return AECableType.SMART;",
    ),
    "appeng/blockentity/networking/CrystalResonanceGeneratorBlockEntity.java": (
        b"EnumSet.of(orientation.getSide(RelativeSide.BACK))",
        b"return AECableType.SMART;",
    ),
    "appeng/blockentity/misc/GrowthAcceleratorBlockEntity.java": (
        b"orientation.getSides(EnumSet.of(RelativeSide.FRONT, RelativeSide.BACK))",
        b"return AECableType.COVERED;",
    ),
    "appeng/blockentity/crafting/CraftingBlockEntity.java": (
        b"if (isFormed())",
        b"return EnumSet.noneOf(Direction.class);",
        b"getValue(AbstractCraftingUnitBlock.FORMED)",
    ),
    "appeng/blockentity/crafting/PatternProviderBlockEntity.java": (
        b"var pushDirection = getPushDirection().getDirection();",
        b"return EnumSet.complementOf(EnumSet.of(pushDirection));",
        b"return AECableType.SMART;",
    ),
    "appeng/me/cluster/implementations/QuantumCalculator.java": (
        b"== 9)",
        b"if (num == 5)",
        b"AEBlocks.QUANTUM_LINK",
        b"AEBlocks.QUANTUM_RING",
    ),
    "appeng/me/cluster/implementations/CraftingCPUCalculator.java": (
        b"storage |= craftingBlockEntity.getStorageBytes() > 0;",
        b"return storage;",
    ),
    "appeng/me/cluster/implementations/SpatialPylonCalculator.java": (
        b"min.getX() == max.getX() && min.getY() == max.getY() && min.getZ() != max.getZ()",
        b"te instanceof SpatialPylonBlockEntity",
    ),
    "appeng/parts/reporting/AbstractReportingPart.java": (
        b"private byte spin = 0; // 0-3",
        b'data.putByte("spin", this.getSpin());',
        b".with(AEModelData.SPIN, getSpin())",
        b"bch.addBox(2, 2, 14, 14, 14, 16);",
        b"bch.addBox(4, 4, 13, 12, 12, 14);",
    ),
    "appeng/api/parts/IPart.java": (
        b"default AECableType getDesiredConnectionType()",
        b"return AECableType.GLASS;",
    ),
    "appeng/parts/automation/AbstractLevelEmitterPart.java": (
        b"public final AECableType getDesiredConnectionType()",
        b"return AECableType.SMART;",
        b"bch.addBox(7, 7, 11, 9, 9, 16);",
    ),
    "appeng/parts/automation/PlaneConnections.java": (
        b"private static final int BITMASK_UP = 8;",
        b"private static final int BITMASK_RIGHT = 4;",
        b"private static final int BITMASK_DOWN = 2;",
        b"private static final int BITMASK_LEFT = 1;",
        b"new ArrayList<>(16)",
    ),
    "appeng/parts/automation/PlaneConnectionHelper.java": (
        b"case UP:\n                facingRight = Direction.EAST;\n"
        b"                facingUp = Direction.NORTH;",
        b"case DOWN:\n                facingRight = Direction.WEST;\n"
        b"                facingUp = Direction.NORTH;",
        b"case NORTH:\n                facingRight = Direction.WEST;\n"
        b"                facingUp = Direction.UP;",
        b"case SOUTH:\n                facingRight = Direction.EAST;\n"
        b"                facingUp = Direction.UP;",
        b"case WEST:\n                facingRight = Direction.SOUTH;\n"
        b"                facingUp = Direction.UP;",
        b"case EAST:\n                facingRight = Direction.NORTH;\n"
        b"                facingUp = Direction.UP;",
        b"int minX = 1;",
        b"int minY = 1;",
        b"int maxX = 15;",
        b"int maxY = 15;",
        b"minX = 0;",
        b"maxX = 16;",
        b"minY = 0;",
        b"maxY = 16;",
        b"p != null && p.getClass() == part.getClass()",
    ),
    "appeng/parts/automation/PlaneBakedModel.java": (
        b"int minX = permutation.isRight() ? 0 : 1;",
        b"int maxX = permutation.isLeft() ? 16 : 15;",
        b"int minY = permutation.isDown() ? 0 : 1;",
        b"int maxY = permutation.isUp() ? 16 : 15;",
    ),
    "appeng/parts/misc/CableAnchorPart.java": (
        b'AppEng.makeId("part/cable_anchor")',
        b'AppEng.makeId("part/cable_anchor_short")',
        b"getFacadeContainer().getFacade(this.mySide) != null",
        b"bch.addBox(7, 7, 10, 9, 9, 14);",
        b"bch.addBox(7, 7, 10, 9, 9, 16);",
    ),
    "appeng/parts/networking/QuartzFiberPart.java": (
        b"bch.addBox(6, 6, 10, 10, 10, 16);",
    ),
    "appeng/parts/misc/ToggleBusPart.java": (
        b"bch.addBox(6, 6, 11, 10, 10, 16);",
    ),
    "appeng/parts/storagebus/StorageBusPart.java": (
        b"bch.addBox(3, 3, 15, 13, 13, 16);",
        b"bch.addBox(2, 2, 14, 14, 14, 15);",
        b"bch.addBox(5, 5, 12, 11, 11, 14);",
    ),
    "appeng/parts/automation/ImportBusPart.java": (
        b"bch.addBox(6, 6, 11, 10, 10, 13);",
        b"bch.addBox(5, 5, 13, 11, 11, 14);",
        b"bch.addBox(4, 4, 14, 12, 12, 16);",
    ),
    "appeng/parts/automation/ExportBusPart.java": (
        b"bch.addBox(4, 4, 12, 12, 12, 14);",
        b"bch.addBox(5, 5, 14, 11, 11, 15);",
        b"bch.addBox(6, 6, 15, 10, 10, 16);",
        b"bch.addBox(6, 6, 11, 10, 10, 12);",
    ),
    "appeng/parts/automation/AnnihilationPlanePart.java": (
        b"connectionHelper.getBoxes(bch);",
    ),
    "appeng/parts/automation/FormationPlanePart.java": (
        b"connectionHelper.getBoxes(bch);",
    ),
    "appeng/parts/crafting/PatternProviderPart.java": (
        b"bch.addBox(2, 2, 14, 14, 14, 16);",
        b"bch.addBox(5, 5, 12, 11, 11, 14);",
    ),
    "appeng/parts/misc/InterfacePart.java": (
        b"bch.addBox(2, 2, 14, 14, 14, 16);",
        b"bch.addBox(5, 5, 12, 11, 11, 14);",
    ),
    "appeng/parts/networking/EnergyAcceptorPart.java": (
        b"bch.addBox(2, 2, 14, 14, 14, 16);",
        b"bch.addBox(4, 4, 12, 12, 12, 14);",
    ),
    "appeng/parts/p2p/P2PTunnelPart.java": (
        b"bch.addBox(5, 5, 12, 11, 11, 13);",
        b"bch.addBox(3, 3, 13, 13, 13, 14);",
        b"bch.addBox(2, 2, 14, 14, 14, 16);",
    ),
    "appeng/parts/BusCollisionHelper.java": (
        b"public BusCollisionHelper(List<AABB> boxes, @Nullable Direction s, boolean visual)",
        b"case DOWN -> {\n                    this.x = Direction.EAST;\n"
        b"                    this.y = Direction.NORTH;",
        b"case UP -> {\n                    this.x = Direction.EAST;\n"
        b"                    this.y = Direction.SOUTH;",
        b"case EAST -> {\n                    this.x = Direction.SOUTH;\n"
        b"                    this.y = Direction.UP;",
        b"case WEST -> {\n                    this.x = Direction.NORTH;\n"
        b"                    this.y = Direction.UP;",
        b"case NORTH -> {\n                    this.x = Direction.WEST;\n"
        b"                    this.y = Direction.UP;",
        b"case SOUTH -> {\n                    this.x = Direction.EAST;\n"
        b"                    this.y = Direction.UP;",
        b"this.isVisual = visual;",
        b"return !this.isVisual;",
    ),
    "appeng/parts/automation/PlaneModels.java": (
        b'"part/transition_plane_off"',
        b"this.modelOff = new PartModel(MODEL_CHASSIS_OFF, planeOff);",
    ),
    "appeng/parts/p2p/P2PModels.java": (
        b'"part/p2p/p2p_tunnel_status_off"',
        b'"part/p2p/p2p_tunnel_frequency"',
    ),
    "appeng/parts/CableBusContainer.java": (
        b"getCableConnectionType",
        b"FacadeContainer",
        b"var desiredType = part.getDesiredConnectionType();",
        b"renderState.getCoreType() == CableCoreType.GLASS",
        b"renderState.setCoreType(CableCoreType.COVERED);",
        b"!facade.getBlockState().isSolidRender(level, getBlockEntity().getBlockPos())",
    ),
    "appeng/client/render/cablebus/CableBusBakedModel.java": (
        b"FacadeBuilder",
        b"CableBusRenderState",
    ),
    "appeng/client/render/cablebus/FacadeBuilder.java": (
        b"public static final double THIN_THICKNESS = 1D / 16D - 2e-3;",
        b"if (facadeRenderState.isTransparent())",
        b"if (otherState != null && !otherState.isTransparent())",
        b"QuadFaceStripper faceStripper = new QuadFaceStripper(fullBounds, facadeMask);",
        b"QuadCornerKicker kicker = new QuadCornerKicker();",
        b"kicker.setThickness(THIN_THICKNESS);",
        b"if (blockState.skipRendering(adjState, cullFace))",
        b"emitter.fromVanilla(quad.getVertices(), 0);",
        b"emitter.cullFace(cullFace == side ? side : null);",
        b"emitter.shade(quad.isShade());",
        b"emitter.ambientOcclusion(quad.hasAmbientOcclusion());",
        b"interpolator.setInputQuad(emitter);",
        b"interpolator.transform(emitter);",
        b"blockColors.getColor(blockState, facadeAccess, pos, quad.getTintIndex())",
        b"if (bb.intersects(facadeBox))",
        b"b.maxX = Math.max(b.maxX, bb.maxX);",
        b"b.minX = Math.min(b.minX, bb.minX);",
        b"boxes.add(new AABB(fb.minX, fb.minY, fb.minZ, hole.minX, fb.maxY, fb.maxZ));",
        b"boxes.add(new AABB(hole.maxX, fb.minY, fb.minZ, fb.maxX, fb.maxY, fb.maxZ));",
    ),
    "appeng/decorative/solid/QuartzGlassBlock.java": (
        b"adjacentBlockState.getBlock() instanceof QuartzGlassBlock",
        b"adjacentBlockState.getRenderShape() == state.getRenderShape()",
        b"return super.skipRendering(state, adjacentBlockState, side);",
    ),
    "appeng/items/parts/FacadeItem.java": (
        b"itemStack.getComponentsPatch().isEmpty()",
        b"itemStack.getItem() instanceof BlockItem blockItem",
        b"blockState.getRenderShape() == RenderShape.MODEL",
        b"defaultState.hasBlockEntity()",
        b"defaultState.isCollisionShapeFullBlock(EmptyBlockGetter.INSTANCE, BlockPos.ZERO)",
        b"!isBlockEntity || isWhiteListed",
        b"isFullCube || isWhiteListed",
    ),
    "appeng/facade/FacadePart.java": (
        b"private BlockState facade;",
        b"var newState = getBlockState().cycle(property);",
        b"setBlockState(newState);",
    ),
    "appeng/facade/FacadeContainer.java": (
        b"BlockState.CODEC.decode(NbtOps.INSTANCE, tag)",
        b"BlockState.CODEC.encodeStart(NbtOps.INSTANCE",
    ),
    "appeng/block/networking/CableBusBlock.java": (
        b"public static ThreadLocal<Direction> RENDERING_FACADE_DIRECTION",
        b"if (side.getOpposite() != renderingFacadeDir)",
        b"return facadeState.getSourceBlock();",
        b"facades.containsKey(renderingFacadeDir)",
    ),
    "appeng/client/render/model/GlassBakedModel.java": (
        b"static final Material[] TEXTURES_FRAME = generateTexturesFrame();",
        b"final GlassState glassState = getGlassState(blockView, state, pos);",
        b"if (glassState.hasAdjacentGlassBlock(side))",
        b"final int edgeBitmask = glassState.getMask(side);",
        b"private static boolean isGlassBlock(",
        b".getBlock() instanceof QuartzGlassBlock",
        b"return builder.bakeQuad();",
    ),
    "appeng/core/definitions/AEBlocks.java": (
        b"new QuartzGlassBlock(glassProps().noOcclusion()",
        b"new QuartzLampBlock(glassProps().lightLevel(b -> 15).noOcclusion()",
    ),
    "appeng/block/AEBaseBlock.java": (
        b"for (var property : getOrientationStrategy().getProperties())",
        b"return OrientationStrategies.none();",
    ),
    "appeng/api/orientation/IOrientationStrategy.java": (
        b'IntegerProperty SPIN = IntegerProperty.create("spin", 0, 3);',
    ),
    "appeng/api/orientation/FacingStrategy.java": (
        b"this.properties = Collections.singletonList(property);",
        b"return properties;",
    ),
    "appeng/block/misc/InscriberBlock.java": (
        b"builder.add(WATERLOGGED);",
        b"return OrientationStrategies.full();",
    ),
    "appeng/block/networking/WirelessAccessPointBlock.java": (
        b"OFF, ON, HAS_CHANNEL;",
        b"builder.add(STATE);",
        b"builder.add(WATERLOGGED);",
        b"return OrientationStrategies.facing();",
    ),
    "appeng/block/misc/ChargerBlock.java": (
        b"return OrientationStrategies.full();",
    ),
    "appeng/block/qnb/QuantumBaseBlock.java": (
        b'BooleanProperty FORMED = BooleanProperty.create("formed");',
        b"builder.add(FORMED);",
        b"builder.add(WATERLOGGED);",
    ),
    "appeng/block/spatial/SpatialPylonBlock.java": (
        b'BooleanProperty POWERED_ON = BooleanProperty.create("powered_on");',
        b"builder.add(POWERED_ON);",
    ),
    "appeng/block/spatial/SpatialIOPortBlock.java": (
        b'BooleanProperty POWERED = BooleanProperty.create("powered");',
        b"builder.add(POWERED);",
        b"return OrientationStrategies.full();",
    ),
    "appeng/block/spatial/SpatialAnchorBlock.java": (
        b'BooleanProperty POWERED = BooleanProperty.create("powered");',
        b"builder.add(POWERED);",
        b"return OrientationStrategies.facing();",
    ),
    "appeng/block/networking/ControllerBlock.java": (
        b"offline, online, conflicted;",
        b"block, column_x, column_y, column_z, inside_a, inside_b;",
        b"setValue(CONTROLLER_STATE, ControllerBlockState.offline)",
        b".setValue(CONTROLLER_TYPE, ControllerRenderType.block)",
        b"builder.add(CONTROLLER_STATE);",
        b"builder.add(CONTROLLER_TYPE);",
    ),
    "appeng/block/storage/DriveBlock.java": (
        b"return OrientationStrategies.full();",
    ),
    "appeng/block/storage/MEChestBlock.java": (
        b'BooleanProperty LIGHTS_ON = BooleanProperty.create("lights_on");',
        b"builder.add(LIGHTS_ON);",
        b"return OrientationStrategies.full();",
    ),
    "appeng/block/misc/InterfaceBlock.java": (
        b"public class InterfaceBlock extends AEBaseEntityBlock<InterfaceBlockEntity>",
    ),
    "appeng/block/storage/IOPortBlock.java": (
        b'BooleanProperty POWERED = BooleanProperty.create("powered");',
        b"builder.add(POWERED);",
        b"return OrientationStrategies.full();",
    ),
    "appeng/block/networking/EnergyAcceptorBlock.java": (
        b"public class EnergyAcceptorBlock extends AEBaseEntityBlock<EnergyAcceptorBlockEntity>",
    ),
    "appeng/block/networking/CrystalResonanceGeneratorBlock.java": (
        b"builder.add(WATERLOGGED);",
        b"return OrientationStrategies.facing();",
    ),
    "appeng/block/misc/VibrationChamberBlock.java": (
        b'BooleanProperty ACTIVE = BooleanProperty.create("active");',
        b"builder.add(ACTIVE);",
        b"return OrientationStrategies.full();",
    ),
    "appeng/block/misc/GrowthAcceleratorBlock.java": (
        b'BooleanProperty POWERED = BooleanProperty.create("powered");',
        b"builder.add(POWERED);",
        b"return OrientationStrategies.facing();",
    ),
    "appeng/block/networking/EnergyCellBlock.java": (
        b"public static final int MAX_FULLNESS = 4;",
        b'IntegerProperty ENERGY_STORAGE = IntegerProperty.create("fullness", 0, MAX_FULLNESS);',
        b"builder.add(ENERGY_STORAGE);",
    ),
    "appeng/block/networking/CreativeEnergyCellBlock.java": (
        b"public class CreativeEnergyCellBlock extends "
        b"AEBaseEntityBlock<CreativeEnergyCellBlockEntity>",
    ),
    "appeng/block/crafting/AbstractCraftingUnitBlock.java": (
        b"defaultBlockState().setValue(FORMED, false).setValue(POWERED, false)",
        b"builder.add(POWERED);",
        b"builder.add(FORMED);",
    ),
    "appeng/block/crafting/CraftingMonitorBlock.java": (
        b"return OrientationStrategies.full();",
    ),
    "appeng/block/crafting/CraftingUnitBlock.java": (
        b"public class CraftingUnitBlock extends AbstractCraftingUnitBlock<CraftingBlockEntity>",
    ),
    "appeng/block/crafting/PatternProviderBlock.java": (
        b"builder.add(PUSH_DIRECTION);",
    ),
    "appeng/block/crafting/PushDirection.java": (
        b"DOWN(Direction.DOWN),",
        b"UP(Direction.UP),",
        b"NORTH(Direction.NORTH),",
        b"SOUTH(Direction.SOUTH),",
        b"WEST(Direction.WEST),",
        b"EAST(Direction.EAST),",
        b"ALL;",
    ),
    "appeng/block/crafting/MolecularAssemblerBlock.java": (
        b'BooleanProperty POWERED = BooleanProperty.create("powered");',
        b"builder.add(POWERED);",
    ),
    "appeng/api/orientation/FacingWithSpinStrategy.java": (
        b"BlockStateProperties.FACING,",
        b"SPIN);",
        b"return state.getValue(SPIN);",
    ),
    (
        "appeng/thirdparty/codechicken/lib/model/pipeline/transformers/"
        "QuadCornerKicker.java"
    ): (
        b"if (side != this.mySide && side != (this.mySide ^ 1))",
        b"(this.facadeMask & 1 << hoz) != 0",
        b"x -= vec.getX() * this.thickness;",
        b"private final static double EPSILON = 0.00001;",
        b"return Math.abs(a - b) < EPSILON;",
    ),
    (
        "appeng/thirdparty/codechicken/lib/model/pipeline/transformers/"
        "QuadReInterpolator.java"
    ): (
        b"int s = quad.nominalFace().ordinal() >> 1;",
        b"int xIdx = dx(s);",
        b"int yIdx = dy(s);",
        b"originalSpriteColor[i] = quad.color(i);",
        b"originalSpriteU[i] = quad.u(i);",
        b"originalSpriteV[i] = quad.v(i);",
        b"interpColorFrom(quad, i);",
        b"interpUVFrom(quad, i);",
        b"interpLightMapFrom(quad, i);",
    ),
    "appeng/thirdparty/codechicken/lib/math/InterpHelper.java": (
        b"if (this.y0 == y)",
        b"else if (this.x0 == x)",
        b"this.rX = (x - this.x0) / (this.x1 - this.x0);",
        b"this.rY = (y - this.y0) / (this.y1 - this.y0);",
        b"return f0 * (1 - this.rY) + f1 * this.rY;",
    ),
}

EXTENSION_REGISTRY_CLASSES = {
    "expandedae-2.1.1": (
        "lu/kolja/expandedae/definition/ExpBlocks.class",
        "lu/kolja/expandedae/definition/ExpBlockEntities.class",
    ),
    "megacells-4.11.0": (
        "gripe/_90/megacells/definition/MEGABlocks.class",
        "gripe/_90/megacells/definition/MEGABlockEntities.class",
    ),
    "advanced_ae-1.6.12-1.21.1": (
        "net/pedroksl/advanced_ae/common/definitions/AAEBlocks.class",
        "net/pedroksl/advanced_ae/common/definitions/AAEBlockEntities.class",
    ),
    "extendedae-1.21-2.2.35-neoforge": (
        "com/glodblock/github/extendedae/common/EAESingletons.class",
    ),
}


def digest(path: Path, algorithm: str) -> str:
    value = hashlib.new(algorithm)
    with path.open("rb") as source:
        for chunk in iter(lambda: source.read(64 * 1024), b""):
            value.update(chunk)
    return value.hexdigest()


def verify_hash(label: str, actual: str, expected: str) -> None:
    if actual != expected:
        raise ValueError(f"{label}: got {actual}, expected {expected}")


def verify_exact_identity(
    path: Path,
    label: str,
    size: int,
    sha1: str,
    sha256: str,
    sha512: str,
) -> None:
    if not path.is_file():
        raise ValueError(f"{label} is not a regular file: {path}")
    if path.stat().st_size != size:
        raise ValueError(f"{label} size changed")
    verify_hash(f"{label} SHA-1", digest(path, "sha1"), sha1)
    verify_hash(f"{label} SHA-256", digest(path, "sha256"), sha256)
    verify_hash(f"{label} SHA-512", digest(path, "sha512"), sha512)


def verify_glassential_artifact(path: Path) -> None:
    verify_exact_identity(
        path,
        "Glassential Renewed 3.4.5 runtime artifact",
        GLASSENTIAL_SIZE,
        GLASSENTIAL_SHA1,
        GLASSENTIAL_SHA256,
        GLASSENTIAL_SHA512,
    )
    with zipfile.ZipFile(path) as archive:
        names = [entry.filename for entry in archive.infolist()]
        if len(names) != len(set(names)):
            raise ValueError("Glassential Renewed 3.4.5 contains duplicate ZIP entries")
        for resource, (expected_size, expected_sha256) in (
            GLASSENTIAL_RESOURCES.items()
        ):
            try:
                raw = archive.read(resource)
            except KeyError as error:
                raise ValueError(
                    f"Glassential Renewed 3.4.5 is missing {resource}"
                ) from error
            if len(raw) != expected_size:
                raise ValueError(
                    f"Glassential Renewed 3.4.5 resource size changed: {resource}"
                )
            verify_hash(
                f"Glassential Renewed 3.4.5 resource {resource} SHA-256",
                hashlib.sha256(raw).hexdigest(),
                expected_sha256,
            )
        mcmeta_path = "assets/glassential/textures/block/glass.png.mcmeta"
        mcmeta = json.loads(archive.read(mcmeta_path))
        if mcmeta != {
            "fusion": FULL_PACK_OVERRIDE_EVIDENCE["semanticMetadata"]["fusion"]
        }:
            raise ValueError(
                "Glassential Renewed 3.4.5 Fusion cutout metadata changed"
            )


def verify_bluemap_host_artifact(path: Path) -> None:
    verify_exact_identity(
        path,
        "canonical BlueMap 5.22 NeoForge host artifact",
        BLUEMAP_HOST_SIZE,
        BLUEMAP_HOST_SHA1,
        BLUEMAP_HOST_SHA256,
        BLUEMAP_HOST_SHA512,
    )
    with zipfile.ZipFile(path) as archive:
        names = [entry.filename for entry in archive.infolist()]
        if len(names) != len(set(names)):
            raise ValueError("canonical BlueMap host contains duplicate ZIP entries")
        for resource, (expected_size, expected_sha256) in (
            BLUEMAP_HOST_RESOURCES.items()
        ):
            try:
                raw = archive.read(resource)
            except KeyError as error:
                raise ValueError(
                    f"canonical BlueMap host is missing {resource}"
                ) from error
            if len(raw) != expected_size:
                raise ValueError(
                    f"canonical BlueMap host resource size changed: {resource}"
                )
            verify_hash(
                f"canonical BlueMap host resource {resource} SHA-256",
                hashlib.sha256(raw).hexdigest(),
                expected_sha256,
            )
        metadata = tomllib.loads(
            archive.read("META-INF/neoforge.mods.toml").decode("utf-8")
        )
        expected_implementation = CURRENT_HOST_EVIDENCE["implementation"]
        mods = metadata.get("mods", [])
        if len(mods) != 1 or {
            "modId": mods[0].get("modId"),
            "version": mods[0].get("version"),
            "displayTest": mods[0].get("displayTest"),
        } != {
            "modId": expected_implementation["modId"],
            "version": expected_implementation["version"],
            "displayTest": expected_implementation["displayTest"],
        }:
            raise ValueError("canonical BlueMap host implementation metadata changed")
        dependencies = {
            dependency.get("modId"): dependency.get("versionRange")
            for dependency in metadata.get("dependencies", {}).get("bluemap", [])
        }
        if dependencies != {
            "minecraft": expected_implementation["minecraftVersionRange"],
            "neoforge": expected_implementation["neoForgeVersionRange"],
        }:
            raise ValueError("canonical BlueMap host dependency ranges changed")
        version = json.loads(archive.read("de/bluecolored/bluemap/version.json"))
        if version != {
            "version": expected_implementation["version"],
            "git-hash": CURRENT_HOST_EVIDENCE["release"]["gitCommit"],
        }:
            raise ValueError("canonical BlueMap host embedded version changed")


def class_to_path(class_name: str, suffix: str) -> str:
    return class_name.replace(".", "/") + suffix


def expected_audit_classes() -> set[str]:
    classes = {part["sourceClass"] for part in FACE_PARTS}
    classes.update(endpoint["blockEntityClass"] for endpoint in ENDPOINTS)
    classes.update(SUPPORT_SOURCE_CLASSES)
    return classes


def require_markers(raw: bytes, label: str, markers: tuple[bytes, ...]) -> None:
    missing = [marker.decode("utf-8") for marker in markers if marker not in raw]
    if missing:
        raise ValueError(f"{label} source markers changed: {missing}")


def verify_sources_archive(sources_jar: Path) -> None:
    with zipfile.ZipFile(sources_jar) as archive:
        names = [entry.filename for entry in archive.infolist()]
        if len(names) != len(set(names)):
            raise ValueError("AE2 sources artifact contains duplicate ZIP entries")
        available = set(names)
        expected = {
            class_to_path(class_name, ".java")
            for class_name in expected_audit_classes()
        }
        missing = sorted(expected - available)
        if missing:
            raise ValueError(f"AE2 sources artifact is missing audit files: {missing}")
        for path, markers in EXPECTED_SOURCE_MARKERS.items():
            require_markers(archive.read(path), path, markers)


def verify_runtime_archive(jar: Path) -> None:
    with zipfile.ZipFile(jar) as archive:
        names = [entry.filename for entry in archive.infolist()]
        if len(names) != len(set(names)):
            raise ValueError("AE2 runtime artifact contains duplicate ZIP entries")
        available = set(names)
        expected = {
            class_to_path(class_name, ".class")
            for class_name in expected_audit_classes()
        }
        missing = sorted(expected - available)
        if missing:
            raise ValueError(f"AE2 runtime artifact is missing audit classes: {missing}")
        rows = resource_rows(archive)
        checksums = checksum_manifest(rows)
        sizes = size_manifest(rows)
        verify_hash(
            "native structural checksum manifest SHA-256",
            hashlib.sha256(checksums).hexdigest(),
            EXPECTED_RESOURCE_MANIFEST_SHA256,
        )
        verify_hash(
            "native structural size manifest SHA-256",
            hashlib.sha256(sizes).hexdigest(),
            EXPECTED_RESOURCE_SIZES_MANIFEST_SHA256,
        )
        whitelist_path = "data/ae2/tags/block/whitelisted/facades.json"
        whitelist_raw = archive.read(whitelist_path)
        verify_hash(
            "AE2 facade whitelist SHA-256",
            hashlib.sha256(whitelist_raw).hexdigest(),
            FACADE_WHITELIST_RESOURCE_SHA256,
        )
        whitelist = json.loads(whitelist_raw.decode("utf-8"))
        values = whitelist.get("values") if isinstance(whitelist, dict) else None
        if not isinstance(values, list) or values[:-1] != list(FACADE_WHITELIST_BLOCK_IDS):
            raise ValueError("AE2 facade whitelist explicit ID order changed")
        if values[-1] != {
            "id": "#" + FACADE_WHITELIST_OPTIONAL_TAGS[0],
            "required": False,
        }:
            raise ValueError("AE2 facade whitelist optional tag changed")
        for material in NATIVE_FACADE_NEUTRAL_MATERIALS:
            namespace, block_path = material["blockId"].split(":", 1)
            blockstate_path = f"assets/{namespace}/blockstates/{block_path}.json"
            blockstate_raw = archive.read(blockstate_path)
            verify_hash(
                f"neutral facade blockstate {material['blockId']} SHA-256",
                hashlib.sha256(blockstate_raw).hexdigest(),
                material["blockstateSha256"],
            )


def verify_facade_blockstate_resources(ae2_jar: Path, minecraft_client_jar: Path) -> None:
    """Bind every explicit facade state schema to its exact blockstate bytes."""
    with zipfile.ZipFile(ae2_jar) as ae2_archive, zipfile.ZipFile(
        minecraft_client_jar
    ) as minecraft_archive:
        for block_id in FACADE_WHITELIST_BLOCK_IDS:
            namespace, path = block_id.split(":", 1)
            archive = ae2_archive if namespace == "ae2" else minecraft_archive
            resource = f"assets/{namespace}/blockstates/{path}.json"
            try:
                raw = archive.read(resource)
            except KeyError as error:
                raise ValueError(
                    f"missing facade blockstate resource: {resource}"
                ) from error
            verify_hash(
                f"facade blockstate resource {block_id}",
                hashlib.sha256(raw).hexdigest(),
                FACADE_WHITELIST_BLOCKSTATE_SHA256[block_id],
            )


def verify_extension_endpoint_artifacts(artifacts: dict[str, Path]) -> None:
    expected_artifacts = {
        artifact["id"]: artifact
        for artifact in UNSUPPORTED_COMPATIBLE_ENDPOINT_ARTIFACTS
    }
    if set(artifacts) != set(expected_artifacts):
        raise ValueError("extension endpoint artifact set changed")
    if set(EXTENSION_REGISTRY_CLASSES) != set(expected_artifacts):
        raise ValueError("extension endpoint registry-class set changed")

    for artifact_id, expected in expected_artifacts.items():
        path = artifacts[artifact_id]
        verify_exact_identity(
            path,
            artifact_id,
            expected["sizeBytes"],
            expected["sha1"],
            expected["sha256"],
            expected["sha512"],
        )
        with zipfile.ZipFile(path) as archive:
            names = [entry.filename for entry in archive.infolist()]
            if len(names) != len(set(names)):
                raise ValueError(f"{artifact_id} contains duplicate ZIP entries")
            registry_paths = EXTENSION_REGISTRY_CLASSES[artifact_id]
            missing = sorted(set(registry_paths) - set(names))
            if missing:
                raise ValueError(
                    f"{artifact_id} is missing exact registry classes: {missing}"
                )
            registry_bytes = b"\n".join(archive.read(name) for name in registry_paths)
            if artifact_id == KNOWN_EXTENSION_FALLBACK_CONTROL["artifact"]:
                control_blockstate = archive.read(
                    "assets/expandedae/blockstates/exp_io_port.json"
                )
                verify_hash(
                    "ExpandedAE UNKNOWN control blockstate SHA-256",
                    hashlib.sha256(control_blockstate).hexdigest(),
                    KNOWN_EXTENSION_FALLBACK_CONTROL["blockstateSha256"],
                )
                control_be = archive.read(
                    "lu/kolja/expandedae/block/entity/ExpIOPortBlockEntity.class"
                )
                if b"appeng/blockentity/storage/IOPortBlockEntity" not in control_be:
                    raise ValueError(
                        "ExpandedAE UNKNOWN control no longer subclasses AE2 I/O Port"
                    )

        entries = [
            endpoint
            for endpoint in UNSUPPORTED_COMPATIBLE_ENDPOINTS
            if endpoint["artifact"] == artifact_id
        ]
        if len(entries) != expected["endpointCount"]:
            raise ValueError(f"{artifact_id} endpoint count changed")
        for endpoint in entries:
            for field in ("id", "blockEntityId"):
                marker = endpoint[field].split(":", 1)[1].encode("utf-8")
                if marker not in registry_bytes:
                    raise ValueError(
                        f"{artifact_id} registry evidence missing {endpoint[field]}"
                    )


def verify_profile(project: Path, jar: Path) -> None:
    route = project / "src/main/resources" / ROUTE_RESOURCE_ROOT
    actual_files = {path.name for path in route.iterdir() if path.is_file()}
    if actual_files != {
        "profile.json",
        "required-resources.sha256",
        "required-resources.tsv",
    }:
        raise ValueError("native structural route directory contains non-contract files")

    with zipfile.ZipFile(jar) as archive:
        rows = resource_rows(archive)
    expected_checksums = checksum_manifest(rows)
    expected_sizes = size_manifest(rows)
    actual_checksums = (route / "required-resources.sha256").read_bytes()
    actual_sizes = (route / "required-resources.tsv").read_bytes()
    if actual_checksums != expected_checksums or actual_sizes != expected_sizes:
        raise ValueError("native structural generated resource manifests are stale")
    if len(parse_checksum_manifest(actual_checksums)) != REQUIRED_RESOURCE_COUNT:
        raise ValueError("native structural checksum manifest count changed")
    if set(parse_checksum_manifest(actual_checksums)) != set(
        parse_size_manifest(actual_sizes)
    ):
        raise ValueError("native structural committed manifests disagree")

    actual_profile = (route / "profile.json").read_bytes()
    verify_hash(
        "native structural generated profile SHA-256",
        hashlib.sha256(actual_profile).hexdigest(),
        EXPECTED_PROFILE_SHA256,
    )
    if actual_profile != profile_bytes(actual_checksums, actual_sizes):
        raise ValueError("native structural generated profile is stale")
    if json.loads(actual_profile.decode("utf-8")) != profile(
        actual_checksums, actual_sizes
    ):
        raise ValueError("native structural generated profile contract changed")


def verify_frozen_outputs(project: Path) -> None:
    for relative, expected in EXPECTED_FROZEN_OUTPUT_SHA256.items():
        path = project / relative
        if not path.is_file():
            raise ValueError(f"frozen accepted output is missing: {relative}")
        verify_hash(
            f"frozen accepted output {relative}",
            hashlib.sha256(path.read_bytes()).hexdigest(),
            expected,
        )


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--jar", required=True, type=Path)
    parser.add_argument("--sources-jar", required=True, type=Path)
    parser.add_argument("--expanded-ae-jar", required=True, type=Path)
    parser.add_argument("--mega-cells-jar", required=True, type=Path)
    parser.add_argument("--advanced-ae-jar", required=True, type=Path)
    parser.add_argument("--extended-ae-jar", required=True, type=Path)
    parser.add_argument("--glassential-jar", required=True, type=Path)
    parser.add_argument("--bluemap-jar", required=True, type=Path)
    parser.add_argument("--minecraft-client-jar", required=True, type=Path)
    args = parser.parse_args()

    verify_exact_identity(
        args.jar,
        "AE2 runtime artifact",
        EXPECTED_SIZE,
        EXPECTED_SHA1,
        EXPECTED_SHA256,
        EXPECTED_SHA512,
    )
    verify_exact_identity(
        args.sources_jar,
        "AE2 sources artifact",
        EXPECTED_SOURCES_SIZE,
        EXPECTED_SOURCES_SHA1,
        EXPECTED_SOURCES_SHA256,
        EXPECTED_SOURCES_SHA512,
    )
    verify_exact_identity(
        args.minecraft_client_jar,
        "Minecraft 1.21.1 client artifact",
        MINECRAFT_CLIENT_SIZE,
        MINECRAFT_CLIENT_SHA1,
        MINECRAFT_CLIENT_SHA256,
        MINECRAFT_CLIENT_SHA512,
    )
    verify_glassential_artifact(args.glassential_jar)
    verify_bluemap_host_artifact(args.bluemap_jar)
    with zipfile.ZipFile(args.jar) as archive:
        verify_metadata(archive.read("META-INF/neoforge.mods.toml"))
    verify_runtime_archive(args.jar)
    verify_sources_archive(args.sources_jar)
    verify_facade_blockstate_resources(args.jar, args.minecraft_client_jar)
    verify_extension_endpoint_artifacts({
        "expandedae-2.1.1": args.expanded_ae_jar,
        "megacells-4.11.0": args.mega_cells_jar,
        "advanced_ae-1.6.12-1.21.1": args.advanced_ae_jar,
        "extendedae-1.21-2.2.35-neoforge": args.extended_ae_jar,
    })
    project = Path(__file__).resolve().parents[1]
    verify_profile(project, args.jar)
    verify_frozen_outputs(project)
    print(
        "Verified exact AE2 19.2.17 S1 structural evidence: 29 face parts, "
        "30 native and 67 known extension endpoints, 99 resources, exact "
        "AE2/MC facade blockstates, Glassential 3.4.5 override resources, "
        "canonical BlueMap 5.22 host, sources and frozen M0-M3f outputs."
    )
    return 0


if __name__ == "__main__":
    try:
        sys.exit(main())
    except (OSError, UnicodeError, ValueError, zipfile.BadZipFile) as error:
        print(f"AE2 native structural verification failed: {error}", file=sys.stderr)
        sys.exit(1)
