#!/usr/bin/env python3
# SPDX-License-Identifier: LGPL-3.0-only
"""Generate and validate the bounded AE2/ExtendedAE S1 staging fixtures."""

from __future__ import annotations

import argparse
from collections import Counter
import hashlib
import json
from pathlib import Path
import sys
from typing import Any, Iterable


ROOT = Path(__file__).resolve().parent
PROFILE_PATH = (
    ROOT.parent
    / "src/main/resources/bluemap-ae2/profiles/ae2/19.2.17/profile.json"
)
M3_COMPLETION_PROFILE_PATH = (
    ROOT.parent
    / "src/main/resources/bluemap-ae2/profiles/ae2/19.2.17/"
    "routes/m3-completion/profile.json"
)
NATIVE_STRUCTURAL_PROFILE_PATH = (
    ROOT.parent
    / "src/main/resources/bluemap-ae2/profiles/ae2/19.2.17/"
    "routes/cable-bus-structural/profile.json"
)
SUPPORT_MATRIX_PATH = (
    ROOT.parent / "src/main/resources/bluemap-ae2/support-matrix.json"
)
PROVENANCE_PATH = ROOT.parent / "provenance/upstreams.json"
NATIVE_STRUCTURAL_ORACLE_PATH = ROOT / "native-structural-oracle.json"
NATIVE_STRUCTURAL_LEGACY_INPUT_PATH = (
    ROOT / "native-structural-legacy-input.json"
)
NATIVE_STRUCTURAL_LEGACY_ORACLE_PATH = (
    ROOT / "native-structural-legacy-oracle.json"
)
M45_RUNTIME_ORACLE_PATH = ROOT / "m45-runtime-oracle.json"
M45_SCHEMA10_LEGACY_ORACLE_PATH = (
    ROOT / "m45-schema10-legacy-oracle.json"
)
M45_PROFILE_ROOT = ROOT.parent / "src/main/resources/bluemap-ae2/profiles"
FUNCTION_ROOT = Path("datapack/data/ae2_m3/function")
ALIAS_ROOT = Path("datapack/data/framedblocks_gallery/function")
LOAD_TAG = Path("datapack/data/minecraft/tags/function/load.json")

FIXTURE_BOUNDS = ((208, 99, 192), (263, 104, 239))
M2_FIXTURE_BOUNDS = ((208, 99, 242), (239, 104, 249))
M3_FIXTURE_BOUNDS = ((240, 98, 242), (263, 104, 249))
M3B_FIXTURE_BOUNDS = ((240, 98, 260), (279, 104, 267))
M3C_FIXTURE_BOUNDS = ((208, 97, 288), (279, 104, 307))
M3D_FIXTURE_BOUNDS = ((296, 97, 260), (319, 105, 299))
M3E_FIXTURE_BOUNDS = ((281, 97, 269), (294, 105, 278))
M3F_FIXTURE_BOUNDS = ((280, 96, 208), (319, 106, 230))
S1_FIXTURE_BOUNDS = ((208, 96, 312), (319, 110, 367))
M45_FIXTURE_BOUNDS = ((336, 96, 312), (511, 110, 431))
M45_CLEAR_BOUNDS = tuple(
    (
        (min_x, 96, min_z),
        (min(min_x + 43, 511), 110, min(min_z + 29, 431)),
    )
    for min_z in range(312, 432, 30)
    for min_x in range(336, 512, 44)
)
S1_CLEAR_BOUNDS = (
    ((208, 96, 312), (263, 110, 339)),
    ((264, 96, 312), (319, 110, 339)),
    ((208, 96, 340), (263, 110, 367)),
    ((264, 96, 340), (319, 110, 367)),
)
DECK_BOUNDS = ((214, 106, 251), (228, 110, 257))
SENTINEL_BOUNDS = ((255, 99, 255), (258, 102, 257))
FIXTURE_SUPPORT_BOUNDS = ((208, 99, 192), (263, 99, 239))
M2_FIXTURE_SUPPORT_BOUNDS = ((208, 99, 242), (239, 99, 249))
M3_FIXTURE_SUPPORT_BOUNDS = ((240, 98, 242), (263, 98, 249))
M3_FIXTURE_AIR_GAP_BOUNDS = ((240, 99, 242), (263, 99, 249))
M3B_FIXTURE_SUPPORT_BOUNDS = ((240, 98, 260), (279, 98, 267))
M3B_FIXTURE_AIR_GAP_BOUNDS = ((240, 99, 260), (279, 99, 267))
M3C_FIXTURE_SUPPORT_BOUNDS = ((208, 97, 288), (279, 97, 307))
M3C_FIXTURE_AIR_BOUNDS = ((208, 98, 288), (279, 104, 307))
M3D_FIXTURE_SUPPORT_BOUNDS = ((296, 97, 260), (319, 97, 299))
M3D_FIXTURE_AIR_BOUNDS = ((296, 98, 260), (319, 105, 299))
M3D_PREPROBE_POWERED_BOUNDS = ((296, 97, 268), (299, 102, 271))
M3D_PREPROBE_MIXED_BOUNDS = ((316, 97, 260), (319, 102, 262))
DECK_FLOOR_BOUNDS = ((215, 107, 252), (227, 107, 256))
DECK_AIR_BOUNDS = ((215, 108, 252), (227, 109, 256))
SENTINEL_SUPPORT = (256, 99, 256)
SENTINEL_FRAME = (256, 100, 256)
SENTINEL_GAP = (257, 100, 256)
SENTINEL_CONTROL = (258, 100, 256)
POSE_FLOOR = (221, 107, 254)
POSE_FEET = (221, 108, 254)
POSE_HEAD = (221, 109, 254)

DENSE_CABLE_BOUNDS = (
    ((272, 96, 192), (279, 99, 199)),
    ((304, 96, 192), (311, 99, 199)),
    ((272, 96, 232), (279, 99, 239)),
    ((304, 96, 232), (311, 99, 239)),
)
DENSE_OWNED_BOUNDS = tuple(
    (
        (bounds[0][0] - 1, 95, bounds[0][2] - 1),
        (bounds[1][0] + 1, 100, bounds[1][2] + 1),
    )
    for bounds in DENSE_CABLE_BOUNDS
)
DENSE_CABLE_ID = "ae2:fluix_covered_dense_cable"
DENSE_CELL_COUNT = 1024
DENSE_EXPECTED_TRIANGLES = 63_488

COLORS = (
    ("white", "white"),
    ("light_gray", "light_gray"),
    ("gray", "gray"),
    ("black", "black"),
    ("lime", "lime"),
    ("yellow", "yellow"),
    ("orange", "orange"),
    ("brown", "brown"),
    ("red", "red"),
    ("pink", "pink"),
    ("magenta", "magenta"),
    ("purple", "purple"),
    ("blue", "blue"),
    ("light_blue", "light_blue"),
    ("cyan", "cyan"),
    ("green", "green"),
    ("fluix", "transparent"),
)
COLOR_TINTS = {
    "white": ((0xB4, 0xB4, 0xB4), (0xF9, 0xF9, 0xF9)),
    "light_gray": ((0x7E, 0x7E, 0x7E), (0xC4, 0xC4, 0xC4)),
    "gray": ((0x4F, 0x4F, 0x4F), (0x94, 0x92, 0x94)),
    "black": ((0x13, 0x13, 0x13), (0x3B, 0x3B, 0x3B)),
    "lime": ((0x4E, 0xC0, 0x4E), (0xB3, 0xF8, 0x6D)),
    "yellow": ((0xFF, 0xCF, 0x40), (0xF4, 0xFF, 0x80)),
    "orange": ((0xD9, 0x78, 0x2F), (0xF2, 0xBA, 0x49)),
    "brown": ((0x6E, 0x4A, 0x12), (0x8E, 0x6E, 0x1A)),
    "red": ((0xAA, 0x21, 0x2B), (0xF0, 0x76, 0x65)),
    "pink": ((0xD8, 0x6E, 0xAA), (0xFB, 0xCA, 0xD5)),
    "magenta": ((0xC1, 0x51, 0x89), (0xE6, 0x9E, 0xBF)),
    "purple": ((0x6E, 0x5C, 0xB8), (0xB0, 0x6F, 0xDD)),
    "blue": ((0x33, 0x7F, 0xF0), (0x40, 0xC1, 0xFF)),
    "light_blue": ((0x69, 0xB9, 0xFF), (0x80, 0xF7, 0xFF)),
    "cyan": ((0x22, 0xB0, 0xAE), (0x65, 0xE8, 0xC9)),
    "green": ((0x07, 0x9B, 0x6B), (0x32, 0xD8, 0x50)),
    "fluix": ((0x5A, 0x47, 0x9E), (0xE2, 0xA3, 0xE3)),
}
COLOR_MEDIUM_TINTS = {
    "white": (0xE0, 0xE0, 0xE0),
    "light_gray": (0xA0, 0x9F, 0xA0),
    "gray": (0x6C, 0x6B, 0x6C),
    "black": (0x27, 0x27, 0x27),
    "lime": (0x70, 0xE2, 0x59),
    "yellow": (0xFF, 0xE3, 0x59),
    "orange": (0xEC, 0xA2, 0x3C),
    "brown": (0x7E, 0x5C, 0x16),
    "red": (0xD7, 0x3E, 0x42),
    "pink": (0xFF, 0x99, 0xBB),
    "magenta": (0xD5, 0x71, 0x9C),
    "purple": (0x91, 0x5D, 0xCD),
    "blue": (0x38, 0x94, 0xFF),
    "light_blue": (0x70, 0xD2, 0xFF),
    "cyan": (0x2F, 0xCC, 0xB7),
    "green": (0x17, 0xB8, 0x6D),
    "fluix": (0x91, 0x5D, 0xCD),
}
FAMILIES = (
    {
        "key": "glass",
        "id_suffix": "glass_cable",
        "core": "glass",
        "connection": "glass",
        "variant": "glass",
        "size": "normal",
    },
    {
        "key": "covered",
        "id_suffix": "covered_cable",
        "core": "covered",
        "connection": "covered",
        "variant": "covered",
        "size": "normal",
    },
    {
        "key": "smart",
        "id_suffix": "smart_cable",
        "core": "covered",
        "connection": "smart",
        "variant": "smart",
        "size": "normal",
    },
    {
        "key": "dense_covered",
        "id_suffix": "covered_dense_cable",
        "core": "dense_smart",
        "connection": "dense_covered",
        "variant": "covered",
        "size": "dense",
    },
    {
        "key": "dense_smart",
        "id_suffix": "smart_dense_cable",
        "core": "dense_smart",
        "connection": "dense_smart",
        "variant": "smart",
        "size": "dense",
    },
)
FAMILY_BY_KEY = {family["key"]: family for family in FAMILIES}
VARIANT_RANK = {"glass": 0, "covered": 1, "smart": 2}
SIZE_RANK = {"normal": 0, "dense": 1}

DIRECTION_DELTAS = {
    "down": (0, -1, 0),
    "up": (0, 1, 0),
    "north": (0, 0, -1),
    "south": (0, 0, 1),
    "west": (-1, 0, 0),
    "east": (1, 0, 0),
}
OPPOSITES = {
    "down": "up",
    "up": "down",
    "north": "south",
    "south": "north",
    "west": "east",
    "east": "west",
}

EXPECTED_M1_CASE_COUNT = 48
EXPECTED_M1_ANCHOR_COUNT = 269
EXPECTED_M1_CUSTOM_ANCHOR_COUNT = 266
EXPECTED_M1_CUSTOM_TRIANGLE_COUNT = 7_576
EXPECTED_M1_SELECTED_RESOURCE_COUNT = 140

EXPECTED_M2_CASE_COUNT = 14
EXPECTED_M2_ANCHOR_COUNT = 21
EXPECTED_M2_CUSTOM_ANCHOR_COUNT = 12
EXPECTED_M2_CUSTOM_TRIANGLE_COUNT = 1_000
EXPECTED_M2_FALLBACK_ANCHOR_COUNT = 9

EXPECTED_M3_CASE_COUNT = 14
EXPECTED_M3_ANCHOR_COUNT = 33
EXPECTED_M3_CUSTOM_ANCHOR_COUNT = 32
EXPECTED_M3_CUSTOM_TRIANGLE_COUNT = 3_856
EXPECTED_M3_FALLBACK_ANCHOR_COUNT = 1

EXPECTED_M3B_CASE_COUNT = 16
EXPECTED_M3B_ANCHOR_COUNT = 36
EXPECTED_M3B_CUSTOM_ANCHOR_COUNT = 32
EXPECTED_M3B_CUSTOM_TRIANGLE_COUNT = 5_056
EXPECTED_M3B_FALLBACK_ANCHOR_COUNT = 4

EXPECTED_M3C_CASE_COUNT = 11
EXPECTED_M3C_ANCHOR_COUNT = 47
EXPECTED_M3C_CUSTOM_ANCHOR_COUNT = 47
EXPECTED_M3C_CUSTOM_TRIANGLE_COUNT = 776
EXPECTED_M3C_SELECTED_RESOURCE_COUNT = 19

EXPECTED_M3D_CASE_COUNT = 9
EXPECTED_M3D_ANCHOR_COUNT = 86
EXPECTED_M3D_CUSTOM_ANCHOR_COUNT = 85
EXPECTED_M3D_CUSTOM_TRIANGLE_COUNT = 4_306
EXPECTED_M3D_FALLBACK_ANCHOR_COUNT = 1
EXPECTED_M3D_SELECTED_RESOURCE_COUNT = 15

EXPECTED_M3E_CASE_COUNT = 3
EXPECTED_M3E_ANCHOR_COUNT = 27
EXPECTED_M3E_CUSTOM_ANCHOR_COUNT = 27
EXPECTED_M3E_CUSTOM_TRIANGLE_COUNT = 1_188
EXPECTED_M3E_FALLBACK_ANCHOR_COUNT = 0
EXPECTED_M3E_EMITTED_RESOURCE_COUNT = 4
EXPECTED_M3E_NEW_SELECTED_RESOURCE_COUNT = 2

EXPECTED_M3F_CASE_COUNT = 7
EXPECTED_M3F_ANCHOR_COUNT = 78
EXPECTED_M3F_CUSTOM_ANCHOR_COUNT = 78
EXPECTED_M3F_CUSTOM_TRIANGLE_COUNT = 2_822
EXPECTED_M3F_FALLBACK_ANCHOR_COUNT = 0
EXPECTED_M3F_EMITTED_RESOURCE_COUNT = 15

EXPECTED_S1_CASE_COUNT = 28
EXPECTED_S1_ANCHOR_COUNT = 360
EXPECTED_S1_CUSTOM_ANCHOR_COUNT = 351
EXPECTED_S1_CUSTOM_TRIANGLE_COUNT = 37_518
EXPECTED_S1_FALLBACK_ANCHOR_COUNT = 9

EXPECTED_LEGACY_UPGRADE_CASE_COUNT = 10
EXPECTED_LEGACY_UPGRADE_ANCHOR_COUNT = 10
EXPECTED_LEGACY_UPGRADE_CUSTOM_TRIANGLE_COUNT = 840
EXPECTED_LEGACY_UPGRADE_IDENTITY_COUNT = 21
EXPECTED_LEGACY_UPGRADE_MATERIAL_ROW_COUNT = 70
EXPECTED_LEGACY_UPGRADE_FIXTURE_BLOCK_COUNT = 92

EXPECTED_CASE_COUNT = (
    EXPECTED_M1_CASE_COUNT
    + EXPECTED_M2_CASE_COUNT
    + EXPECTED_M3_CASE_COUNT
    + EXPECTED_M3B_CASE_COUNT
    + EXPECTED_M3C_CASE_COUNT
    + EXPECTED_M3D_CASE_COUNT
    + EXPECTED_M3E_CASE_COUNT
    + EXPECTED_M3F_CASE_COUNT
    + EXPECTED_S1_CASE_COUNT
)
EXPECTED_ANCHOR_COUNT = (
    EXPECTED_M1_ANCHOR_COUNT
    + EXPECTED_M2_ANCHOR_COUNT
    + EXPECTED_M3_ANCHOR_COUNT
    + EXPECTED_M3B_ANCHOR_COUNT
    + EXPECTED_M3C_ANCHOR_COUNT
    + EXPECTED_M3D_ANCHOR_COUNT
    + EXPECTED_M3E_ANCHOR_COUNT
    + EXPECTED_M3F_ANCHOR_COUNT
    + EXPECTED_S1_ANCHOR_COUNT
)
EXPECTED_CUSTOM_ANCHOR_COUNT = (
    EXPECTED_M1_CUSTOM_ANCHOR_COUNT
    + EXPECTED_M2_CUSTOM_ANCHOR_COUNT
    + EXPECTED_M3_CUSTOM_ANCHOR_COUNT
    + EXPECTED_M3B_CUSTOM_ANCHOR_COUNT
    + EXPECTED_M3C_CUSTOM_ANCHOR_COUNT
    + EXPECTED_M3D_CUSTOM_ANCHOR_COUNT
    + EXPECTED_M3E_CUSTOM_ANCHOR_COUNT
    + EXPECTED_M3F_CUSTOM_ANCHOR_COUNT
    + EXPECTED_S1_CUSTOM_ANCHOR_COUNT
)
EXPECTED_CUSTOM_TRIANGLE_COUNT = (
    EXPECTED_M1_CUSTOM_TRIANGLE_COUNT
    + EXPECTED_M2_CUSTOM_TRIANGLE_COUNT
    + EXPECTED_M3_CUSTOM_TRIANGLE_COUNT
    + EXPECTED_M3B_CUSTOM_TRIANGLE_COUNT
    + EXPECTED_M3C_CUSTOM_TRIANGLE_COUNT
    + EXPECTED_M3D_CUSTOM_TRIANGLE_COUNT
    + EXPECTED_M3E_CUSTOM_TRIANGLE_COUNT
    + EXPECTED_M3F_CUSTOM_TRIANGLE_COUNT
    + EXPECTED_S1_CUSTOM_TRIANGLE_COUNT
)
EXPECTED_STOCK_FALLBACK_ANCHOR_COUNT = (
    2
    + EXPECTED_M2_FALLBACK_ANCHOR_COUNT
    + EXPECTED_M3_FALLBACK_ANCHOR_COUNT
    + EXPECTED_M3B_FALLBACK_ANCHOR_COUNT
    + EXPECTED_M3D_FALLBACK_ANCHOR_COUNT
    + EXPECTED_M3E_FALLBACK_ANCHOR_COUNT
    + EXPECTED_M3F_FALLBACK_ANCHOR_COUNT
    + EXPECTED_S1_FALLBACK_ANCHOR_COUNT
)
EXPECTED_EFFECTIVE_CUSTOM_ANCHOR_COUNT = (
    EXPECTED_CUSTOM_ANCHOR_COUNT + EXPECTED_LEGACY_UPGRADE_ANCHOR_COUNT
)
EXPECTED_EFFECTIVE_CUSTOM_TRIANGLE_COUNT = (
    EXPECTED_CUSTOM_TRIANGLE_COUNT + EXPECTED_LEGACY_UPGRADE_CUSTOM_TRIANGLE_COUNT
)
EXPECTED_EFFECTIVE_STOCK_FALLBACK_ANCHOR_COUNT = (
    EXPECTED_STOCK_FALLBACK_ANCHOR_COUNT - EXPECTED_LEGACY_UPGRADE_ANCHOR_COUNT
)
EXPECTED_CENTER_PART_COUNT = 85
EXPECTED_CORE_PROFILE_RESOURCE_COUNT = 148
EXPECTED_DRIVE_PROFILE_RESOURCE_COUNT = 10
EXPECTED_PROFILE_RESOURCE_COUNT = (
    EXPECTED_CORE_PROFILE_RESOURCE_COUNT + EXPECTED_DRIVE_PROFILE_RESOURCE_COUNT
)
EXPECTED_EXTENDED_DRIVE_PROFILE_RESOURCE_COUNT = 8
EXPECTED_SELECTED_RESOURCE_COUNT = 218

TERMINAL_PART_ID = "ae2:terminal"
STONE_BLOCK_ID = "minecraft:stone"
STONE_TEXTURE = "minecraft:block/stone"
TERMINAL_MATERIAL_TRIANGLES = {
    "ae2:part/monitor_sides": 8,
    "ae2:part/monitor_sides_status": 8,
    "ae2:part/monitor_back": 4,
    "ae2:part/monitor_front": 2,
    "ae2:part/monitor_sides_status_off": 16,
    "ae2:part/terminal_bright": 2,
    "ae2:part/terminal_medium": 2,
    "ae2:part/terminal_dark": 2,
}

DRIVE_BLOCK_ID = "ae2:drive"
DRIVE_BASE_MODEL = "ae2:block/drive/drive_base"
DRIVE_EMPTY_CELL_MODEL = "ae2:block/drive/drive_cell_empty"
DRIVE_GENERIC_CELL_MODEL = "ae2:block/drive/drive_cell"
DRIVE_LED_POLICY = "static-offline-unknown"
DRIVE_UNKNOWN_CELL_POLICY = "atomic-whole-block-original-resource-fallback"
DRIVE_FALLBACK_CELL_ID = "megacells:item_storage_cell_1m"
DRIVE_CELL_TEXTURE = "ae2:block/drive/drive_cells"
DRIVE_LED_TEXTURE = "ae2:block/drive/drive_front"
DRIVE_BASE_MATERIAL_TRIANGLES = {
    "ae2:block/drive/drive_front": 28,
    "ae2:block/drive/drive_inside": 16,
    "ae2:block/drive/drive_inside_bottom": 2,
    "ae2:block/drive/drive_inside_top": 10,
    "ae2:block/generics/back": 6,
    "ae2:block/generics/bottom": 2,
    "ae2:block/generics/front": 8,
    "ae2:block/generics/side": 16,
    "ae2:block/generics/top": 2,
}
DRIVE_EXPLICIT_CELL_MODELS = {
    "ae2:item_storage_cell_1k": "ae2:block/drive/cells/1k_item_cell",
    "ae2:item_storage_cell_4k": "ae2:block/drive/cells/4k_item_cell",
    "ae2:item_storage_cell_16k": "ae2:block/drive/cells/16k_item_cell",
    "ae2:item_storage_cell_64k": "ae2:block/drive/cells/64k_item_cell",
    "ae2:item_storage_cell_256k": "ae2:block/drive/cells/256k_item_cell",
    "ae2:fluid_storage_cell_1k": "ae2:block/drive/cells/1k_fluid_cell",
    "ae2:fluid_storage_cell_4k": "ae2:block/drive/cells/4k_fluid_cell",
    "ae2:fluid_storage_cell_16k": "ae2:block/drive/cells/16k_fluid_cell",
    "ae2:fluid_storage_cell_64k": "ae2:block/drive/cells/64k_fluid_cell",
    "ae2:fluid_storage_cell_256k": "ae2:block/drive/cells/256k_fluid_cell",
    "ae2:creative_storage_cell": "ae2:block/drive/cells/creative_cell",
    "ae2:portable_item_cell_1k": "ae2:block/drive/cells/1k_item_cell",
    "ae2:portable_item_cell_4k": "ae2:block/drive/cells/4k_item_cell",
    "ae2:portable_item_cell_16k": "ae2:block/drive/cells/16k_item_cell",
    "ae2:portable_item_cell_64k": "ae2:block/drive/cells/64k_item_cell",
    "ae2:portable_item_cell_256k": "ae2:block/drive/cells/256k_item_cell",
    "ae2:portable_fluid_cell_1k": "ae2:block/drive/cells/1k_fluid_cell",
    "ae2:portable_fluid_cell_4k": "ae2:block/drive/cells/4k_fluid_cell",
    "ae2:portable_fluid_cell_16k": "ae2:block/drive/cells/16k_fluid_cell",
    "ae2:portable_fluid_cell_64k": "ae2:block/drive/cells/64k_fluid_cell",
    "ae2:portable_fluid_cell_256k": "ae2:block/drive/cells/256k_fluid_cell",
}
DRIVE_GENERIC_CELL_IDS = (
    "ae2:matter_cannon",
    "ae2:color_applicator",
)
DRIVE_CELL_MODELS = {
    **DRIVE_EXPLICIT_CELL_MODELS,
    **{item_id: DRIVE_GENERIC_CELL_MODEL for item_id in DRIVE_GENERIC_CELL_IDS},
}
DRIVE_OCCUPIED_MODEL_COUNT = 12
DRIVE_SLOT_COUNT = 10
DRIVE_BASE_TRIANGLE_COUNT = 90
DRIVE_CELL_CHASSIS_TRIANGLE_COUNT = 6
DRIVE_LED_TRIANGLE_COUNT = 10
DRIVE_COMPONENT_INSENSITIVITY = {
    "ae2:storage_cell_inv": [
        {"#t": "ae2:i", "#": 64, "id": "minecraft:stone"}
    ]
}

EXTENDED_DRIVE_BLOCK_ID = "extendedae:ex_drive"
EXTENDED_DRIVE_BASE_MODEL = (
    "extendedae:block/extended_drive/extended_me_drive_base"
)
EXTENDED_DRIVE_EMPTY_CELL_MODEL = DRIVE_EMPTY_CELL_MODEL
EXTENDED_DRIVE_SLOT_COUNT = 20
EXTENDED_DRIVE_FACE_SLOT_COUNT = 10
EXTENDED_DRIVE_BASE_TRIANGLE_COUNT = 116
EXTENDED_DRIVE_BASE_MATERIAL_TRIANGLES = {
    "extendedae:block/extended_drive/ex_drive_side": 8,
    "extendedae:block/extended_drive/ex_drive_top": 2,
    "extendedae:block/generics/front": 8,
    "extendedae:block/extended_drive/ex_drive_bottom": 2,
    "ae2:block/generics/bottom": 2,
    "ae2:block/drive/drive_front": 56,
    "extendedae:block/extended_drive/drive_inside": 16,
    "ae2:block/drive/drive_inside_top": 8,
    "extendedae:block/generics/side": 12,
    "ae2:block/generics/top": 2,
}
EXTENDED_DRIVE_NATIVE_CELL_MODELS = {
    "extendedae:infinity_water_cell": (
        "extendedae:block/drive/infinity_water_cell"
    ),
    "extendedae:infinity_cobblestone_cell": (
        "extendedae:block/drive/infinity_cobblestone_cell"
    ),
    "extendedae:void_cell": "extendedae:block/drive/void_cell",
}
EXTENDED_DRIVE_CELL_MODELS = {
    **DRIVE_CELL_MODELS,
    **EXTENDED_DRIVE_NATIVE_CELL_MODELS,
}
EXTENDED_DRIVE_OCCUPIED_MODEL_COUNT = 15
EXTENDED_DRIVE_MODEL_MATERIALS = {
    **{model_id: DRIVE_CELL_TEXTURE for model_id in DRIVE_CELL_MODELS.values()},
    "extendedae:block/drive/infinity_water_cell": (
        "extendedae:block/drive/infinity_cell"
    ),
    "extendedae:block/drive/infinity_cobblestone_cell": (
        "extendedae:block/drive/infinity_cell"
    ),
    "extendedae:block/drive/void_cell": "extendedae:block/drive/void_cell",
}
EXTENDED_DRIVE_SELECTED_RESOURCES = (
    "extendedae:block/extended_drive/ex_drive_side",
    "extendedae:block/extended_drive/ex_drive_top",
    "extendedae:block/generics/front",
    "extendedae:block/extended_drive/ex_drive_bottom",
    "extendedae:block/extended_drive/drive_inside",
    "extendedae:block/generics/side",
    "extendedae:block/drive/infinity_cell",
    "extendedae:block/drive/void_cell",
)
EXTENDEDAE_ARTIFACT = {
    "artifact": "ExtendedAE-1.21-2.2.33-neoforge.jar",
    "version": "1.21-2.2.33-neoforge",
    "size_bytes": 5_573_972,
    "sha1": "e87867bffee36a28f9f4493f7bb7e7a5109a480f",
    "sha256": "6652ed1ea4b71f585d48c05a195a77594a7a2bd1ecea0fc805db2122aafad734",
}
EXTENDEDAE_REQUIRED_RESOURCE_DIGESTS = {
    "assets/extendedae/blockstates/ex_drive.json": "ea4728cc083abd744bdd68f10bf93e16183319ea23d86a158a67be64ba75ede5",
    "assets/extendedae/models/block/ex_drive.json": "4df39b10fc224c4b056a90dc72a53fd41ccbba07f610774c88b4ca1138694b66",
    "assets/extendedae/models/block/extended_drive/extended_me_drive_base.json": "f3905633597dadddf7cc2ee543f36d495aa60db3893209b2831941ff3002a151",
    "assets/extendedae/models/block/drive/infinity_water_cell.json": "f7d62c4871ab13468e78ea83819a3b763bafac6e7bcc1bd55e3428e4e4f0fb08",
    "assets/extendedae/models/block/drive/infinity_cobblestone_cell.json": "a7364a25f405f1f61ac89be8d2bf0afe8fb44751cf5f71050704bf4f8d4e1a3d",
    "assets/extendedae/models/block/drive/void_cell.json": "4cd8add5900aaaa3512e8b70a8c8229ddf4984a4332609bd1c3777d547971bcc",
    "assets/extendedae/textures/block/extended_drive/drive_inside.png": "8e52d0f1dc44b489dce28c99b94b87ca5b20492553884c3b96ddedf8439782da",
    "assets/extendedae/textures/block/extended_drive/ex_drive_bottom.png": "b8ffe8fab000e1c22bb5c6f56238049faae2125ffb87fee2ebbbeca59373170e",
    "assets/extendedae/textures/block/extended_drive/ex_drive_side.png": "105c26d8c6532f24e8e5229cc99e541c931af6cfd060aa3c9a60f9d981ed2d37",
    "assets/extendedae/textures/block/extended_drive/ex_drive_top.png": "343b55b999576894107ceb763b379766c56d7d84e85cab16af3b171c7e17b986",
    "assets/extendedae/textures/block/generics/front.png": "35a63e2caa7c9911144bc59d7de0b91dd80472472849b82f0c506bf4154fdbc0",
    "assets/extendedae/textures/block/generics/side.png": "2d15ed6e345f6a9023de7042da840c0f6f3a7a31e0ee6819fd7074ec091759f4",
    "assets/extendedae/textures/block/drive/infinity_cell.png": "05da67836a6b11316b6c26e5cef900d308332049df4ddf298dfc21e0a1bed719",
    "assets/extendedae/textures/block/drive/void_cell.png": "8872f0b7cf2a7f12565ebf2b2740f6eb332c6d540ddfd206d85b5816c7e4c098",
}
EXTENDED_DRIVE_FALLBACKS = {
    "megacells:item_storage_cell_1m": "unsupported-drive-cell-id",
    "kubejs:lava_cell": "unsupported-drive-cell-id",
    "ae2:item_storage_cell_1k": "invalid-drive-cell-count",
    "minecraft:stone": "non-cell-drive-item",
}

CONNECTED_GLASS_BLOCK_IDS = (
    "ae2:quartz_glass",
    "ae2:quartz_vibrant_glass",
)
CONNECTED_GLASS_ROUTE = "ae2:connected_quartz_glass"
CONNECTED_GLASS_BASE_RESOURCES = tuple(
    f"ae2:block/glass/quartz_glass_{suffix}" for suffix in "abcd"
)
CONNECTED_GLASS_FRAME_RESOURCES = tuple(
    f"ae2:block/glass/quartz_glass_frame{mask:04b}" for mask in range(1, 16)
)
CONNECTED_GLASS_SELECTED_RESOURCES = (
    *CONNECTED_GLASS_BASE_RESOURCES,
    *CONNECTED_GLASS_FRAME_RESOURCES,
)
CONNECTED_GLASS_FACE_BIT_DIRECTIONS = {
    "down": ("south", "east", "north", "west"),
    "up": ("south", "west", "north", "east"),
    "north": ("up", "west", "down", "east"),
    "south": ("up", "east", "down", "west"),
    "west": ("up", "south", "down", "north"),
    "east": ("up", "north", "down", "south"),
}
CONNECTED_GLASS_FACE_CORNERS = {
    "east": ((1, 1, 1), (1, 0, 1), (1, 0, 0), (1, 1, 0)),
    "west": ((0, 1, 0), (0, 0, 0), (0, 0, 1), (0, 1, 1)),
    "up": ((1, 1, 1), (1, 1, 0), (0, 1, 0), (0, 1, 1)),
    "down": ((0, 0, 1), (0, 0, 0), (1, 0, 0), (1, 0, 1)),
    "south": ((0, 1, 1), (0, 0, 1), (1, 0, 1), (1, 1, 1)),
    "north": ((1, 1, 0), (1, 0, 0), (0, 0, 0), (0, 1, 0)),
}
CONNECTED_GLASS_FRAME_OCCURRENCES = {
    "0001": 3,
    "0010": 3,
    "0011": 9,
    "0100": 3,
    "0101": 5,
    "0110": 9,
    "0111": 24,
    "1000": 3,
    "1001": 9,
    "1010": 4,
    "1011": 21,
    "1100": 9,
    "1101": 24,
    "1110": 19,
    "1111": 48,
}

CRAFTING_ROUTE = "ae2:formed_crafting_cube"
CRAFTING_BLOCK_KINDS = {
    "ae2:crafting_unit": "unit",
    "ae2:crafting_accelerator": "accelerator",
    "ae2:1k_crafting_storage": "1k_storage",
    "ae2:4k_crafting_storage": "4k_storage",
    "ae2:16k_crafting_storage": "16k_storage",
    "ae2:64k_crafting_storage": "64k_storage",
    "ae2:256k_crafting_storage": "256k_storage",
    "ae2:crafting_monitor": "monitor",
}
CRAFTING_STORAGE_BLOCK_IDS = tuple(
    block_id
    for block_id, kind in CRAFTING_BLOCK_KINDS.items()
    if kind.endswith("_storage")
)
CRAFTING_MONITOR_BLOCK_ID = "ae2:crafting_monitor"
CRAFTING_RESOURCES = (
    "ae2:block/crafting/ring_corner",
    "ae2:block/crafting/ring_side_hor",
    "ae2:block/crafting/ring_side_ver",
    "ae2:block/crafting/unit_base",
    "ae2:block/crafting/light_base",
    "ae2:block/crafting/accelerator_light",
    "ae2:block/crafting/1k_storage_light",
    "ae2:block/crafting/4k_storage_light",
    "ae2:block/crafting/16k_storage_light",
    "ae2:block/crafting/64k_storage_light",
    "ae2:block/crafting/256k_storage_light",
    "ae2:block/crafting/monitor_base",
    "ae2:block/crafting/monitor_light_dark",
    "ae2:block/crafting/monitor_light_medium",
    "ae2:block/crafting/monitor_light_bright",
)
CRAFTING_LIGHT_RESOURCE_BY_KIND = {
    "accelerator": "ae2:block/crafting/accelerator_light",
    "1k_storage": "ae2:block/crafting/1k_storage_light",
    "4k_storage": "ae2:block/crafting/4k_storage_light",
    "16k_storage": "ae2:block/crafting/16k_storage_light",
    "64k_storage": "ae2:block/crafting/64k_storage_light",
    "256k_storage": "ae2:block/crafting/256k_storage_light",
}
CRAFTING_PAINT_COLORS = tuple(
    {
        "ordinal": ordinal,
        "name": registry_prefix,
        "dark_rgb_u8": list(COLOR_TINTS[registry_prefix][0]),
        "medium_rgb_u8": list(COLOR_MEDIUM_TINTS[registry_prefix]),
        "bright_rgb_u8": list(COLOR_TINTS[registry_prefix][1]),
    }
    for ordinal, (registry_prefix, _texture_name) in enumerate(COLORS)
)
CRAFTING_PAINT_BY_ORDINAL = {
    color["ordinal"]: color for color in CRAFTING_PAINT_COLORS
}
CRAFTING_MONITOR_DISPLAY_POLICY = "client-stream-only-display-omitted"
CRAFTING_RESOURCE_MANIFEST_SHA256 = (
    "dc474ba6ce7c4c2d53778827b1c1f9b4994594ea984ed7a2cbd62c40e1bc1183"
)
CRAFTING_TEXTURE_MANIFEST_SHA256 = (
    "a9a2a1ed912f562362d581cbd219b40afd4c884452a0c64cee3d015dfdc81620"
)
CRAFTING_COMPATIBLE_EXTENSION_CONTEXT = {
    "megacells:mega_crafting_unit": "megacells:mega_crafting_unit",
    "expandedae:exp_crafting_unit": "expandedae:exp_cpus",
}
CRAFTING_DIRECTION_AXES = {
    "down": "y",
    "up": "y",
    "north": "z",
    "south": "z",
    "west": "x",
    "east": "x",
}
CRAFTING_CORNER_DIRECTIONS = (
    ("up", "east", "north"),
    ("up", "east", "south"),
    ("up", "west", "north"),
    ("up", "west", "south"),
    ("down", "east", "north"),
    ("down", "east", "south"),
    ("down", "west", "north"),
    ("down", "west", "south"),
)
SCHEMA6_CASE_PREFIX_SHA256 = (
    "104fe864f0ade7f45a9091f1a5646ef843cec111ecb855578e177ca0ddd2c58c"
)
SCHEMA7_CANONICAL_SHA256 = (
    "c60d2afff5a1f92da4972963fcb926c38093f43bb6d7f550799f104349728a38"
)
SCHEMA8_CANONICAL_SHA256 = (
    "93963dd0bb60a276e1a17c6dd1f4eb916cd92bef4ef30a2e8bdc7a2bfa818b3e"
)
SCHEMA9_CANONICAL_SHA256 = (
    "75e6ba2f40631a95f20cfa00d7ca952e521bc2c7a4eb155926334a223a945f3a"
)

QUANTUM_ROUTE = "ae2:quantum_bridge"
QUANTUM_LINK_BLOCK_ID = "ae2:quantum_link"
QUANTUM_RING_BLOCK_ID = "ae2:quantum_ring"
QUANTUM_BLOCK_ENTITY_ID = "ae2:quantum_ring"
QUANTUM_LINK_RESOURCE = "ae2:block/quantum_link"
QUANTUM_RING_RESOURCE = "ae2:block/quantum_ring"
QUANTUM_GLASS_RESOURCE = "ae2:part/cable/glass/transparent"
QUANTUM_COVERED_RESOURCE = "ae2:part/cable/covered/transparent"
QUANTUM_RESOURCES = (
    QUANTUM_LINK_RESOURCE,
    QUANTUM_RING_RESOURCE,
    QUANTUM_GLASS_RESOURCE,
    QUANTUM_COVERED_RESOURCE,
)
QUANTUM_NEW_RESOURCES = (QUANTUM_LINK_RESOURCE, QUANTUM_RING_RESOURCE)
QUANTUM_PLANES = ("xz", "xy", "yz")
QUANTUM_STATIC_POLICY = "static-off-unknown"
QUANTUM_PARTICLE_POLICY = "omitted-transient-client-effects"

M3_COMPLETION_ROUTE = "ae2-m3-completion"
PAINT_BLOCK_ID = "ae2:paint"
SKY_STONE_CHEST_BLOCK_ID = "ae2:sky_stone_chest"
SMOOTH_SKY_STONE_CHEST_BLOCK_ID = "ae2:smooth_sky_stone_chest"
CRANK_BLOCK_ID = "ae2:crank"
INSCRIBER_BLOCK_ID = "ae2:inscriber"
SPATIAL_PYLON_BLOCK_ID = "ae2:spatial_pylon"
M3_COMPLETION_BLOCK_ENTITY_IDS = {
    PAINT_BLOCK_ID: "ae2:paint",
    SKY_STONE_CHEST_BLOCK_ID: "ae2:sky_chest",
    SMOOTH_SKY_STONE_CHEST_BLOCK_ID: "ae2:sky_chest",
    CRANK_BLOCK_ID: "ae2:crank",
    INSCRIBER_BLOCK_ID: "ae2:inscriber",
    SPATIAL_PYLON_BLOCK_ID: "ae2:spatial_pylon",
}
M3_COMPLETION_RESOURCES = (
    "ae2:block/paint1",
    "ae2:block/paint2",
    "ae2:block/paint3",
    "ae2:block/skychest",
    "ae2:block/skyblockchest",
    "ae2:block/crank",
    "ae2:block/inscriber",
    "ae2:block/inscriber_inside",
    "ae2:block/spatial_pylon/base",
    "ae2:block/spatial_pylon/base_end",
    "ae2:block/spatial_pylon/base_spanned",
    "ae2:block/spatial_pylon/dim",
    "ae2:block/spatial_pylon/red",
    "ae2:block/spatial_pylon/red_end",
    "ae2:block/spatial_pylon/red_spanned",
)
M3_COMPLETION_STOCK_RESOURCES = (
    "ae2:block/sky_stone_block",
    "ae2:block/smooth_sky_stone_block",
    "ae2:block/crank",
    "ae2:block/inscriber",
)
PAINT_TEXTURES = M3_COMPLETION_RESOURCES[:3]
PAINT_COLOR_NAMES = tuple(color[0] for color in COLORS[:16])
PAINT_DIRECTION_ORDINALS = tuple(DIRECTION_DELTAS)
PAINT_STATIC_POLICY = "persisted-non-lumen-splotches"
CHEST_STATIC_POLICY = "static-closed-no-contents"
CRANK_STATIC_POLICY = "static-neutral-zero-degrees"
INSCRIBER_STATIC_POLICY = "static-neutral-no-items-no-animation"
SPATIAL_PYLON_STATIC_POLICY = "local-topology-static-offline-unknown"
M3_COMPLETION_TRIANGLES = {
    SKY_STONE_CHEST_BLOCK_ID: 36,
    SMOOTH_SKY_STONE_CHEST_BLOCK_ID: 36,
    CRANK_BLOCK_ID: 34,
    INSCRIBER_BLOCK_ID: 78,
    SPATIAL_PYLON_BLOCK_ID: 24,
}
M3_COMPLETION_STOCK_MATERIALS = {
    PAINT_BLOCK_ID: {},
    SKY_STONE_CHEST_BLOCK_ID: {"ae2:block/sky_stone_block": 12},
    SMOOTH_SKY_STONE_CHEST_BLOCK_ID: {
        "ae2:block/smooth_sky_stone_block": 12
    },
    CRANK_BLOCK_ID: {"ae2:block/crank": 32},
    INSCRIBER_BLOCK_ID: {"ae2:block/inscriber": 66},
    SPATIAL_PYLON_BLOCK_ID: {},
}

NATIVE_STRUCTURAL_ROUTE = "ae2-cable-bus-structural"
NATIVE_STRUCTURAL_COVERAGE = "s1-native-structural"
NATIVE_STRUCTURAL_RESOURCE_MANIFEST_SHA256 = (
    "ae89e4fc3356503cc76ea92ac9cb11ade296551c9cca85cd583ffddbbe35bd76"
)
NATIVE_STRUCTURAL_DIRECT_RESOURCE_COUNT = 41
NATIVE_STRUCTURAL_TRANSITIVE_JSON_COUNT = 43
NATIVE_STRUCTURAL_PNG_COUNT = 56
NATIVE_STRUCTURAL_RESOURCE_COUNT = 99
NATIVE_STRUCTURAL_ORIENTATION_STATE_COUNT = 336
NATIVE_STRUCTURAL_PARTS = (
    ("quartz_fiber", "network"),
    ("toggle_bus", "redstone"),
    ("inverted_toggle_bus", "redstone"),
    ("cable_anchor", "structural"),
    ("monitor", "panel"),
    ("semi_dark_monitor", "panel"),
    ("dark_monitor", "panel"),
    ("storage_bus", "bus"),
    ("import_bus", "bus"),
    ("export_bus", "bus"),
    ("level_emitter", "emitter"),
    ("energy_level_emitter", "emitter"),
    ("annihilation_plane", "plane"),
    ("formation_plane", "plane"),
    ("pattern_encoding_terminal", "terminal"),
    ("crafting_terminal", "terminal"),
    ("terminal", "terminal"),
    ("storage_monitor", "monitor"),
    ("conversion_monitor", "monitor"),
    ("cable_pattern_provider", "service"),
    ("cable_interface", "service"),
    ("pattern_access_terminal", "terminal"),
    ("cable_energy_acceptor", "network"),
    ("me_p2p_tunnel", "p2p"),
    ("redstone_p2p_tunnel", "p2p"),
    ("item_p2p_tunnel", "p2p"),
    ("fluid_p2p_tunnel", "p2p"),
    ("fe_p2p_tunnel", "p2p"),
    ("light_p2p_tunnel", "p2p"),
)
NATIVE_STRUCTURAL_SPIN_PARTS = frozenset(
    {
        "monitor",
        "semi_dark_monitor",
        "dark_monitor",
        "pattern_encoding_terminal",
        "crafting_terminal",
        "terminal",
        "storage_monitor",
        "conversion_monitor",
        "pattern_access_terminal",
    }
)
NATIVE_STRUCTURAL_PLANE_PARTS = (
    "annihilation_plane",
    "formation_plane",
)
NATIVE_STRUCTURAL_P2P_PARTS = (
    "me_p2p_tunnel",
    "redstone_p2p_tunnel",
    "item_p2p_tunnel",
    "fluid_p2p_tunnel",
    "fe_p2p_tunnel",
    "light_p2p_tunnel",
)
NATIVE_STRUCTURAL_ENDPOINTS = {
    "covered": (
        "inscriber",
        "charger",
        "energy_acceptor",
        "vibration_chamber",
        "growth_accelerator",
        "energy_cell",
        "dense_energy_cell",
        "creative_energy_cell",
        "molecular_assembler",
    ),
    "dense_smart": ("quantum_ring", "quantum_link", "controller"),
    "smart": (
        "wireless_access_point",
        "spatial_pylon",
        "spatial_io_port",
        "spatial_anchor",
        "drive",
        "chest",
        "interface",
        "io_port",
        "crystal_resonance_generator",
        "crafting_unit",
        "crafting_accelerator",
        "1k_crafting_storage",
        "4k_crafting_storage",
        "16k_crafting_storage",
        "64k_crafting_storage",
        "256k_crafting_storage",
        "crafting_monitor",
        "pattern_provider",
    ),
}
NATIVE_STRUCTURAL_ENDPOINTS_ORDERED = (
    ("inscriber", "covered"),
    ("wireless_access_point", "smart"),
    ("charger", "covered"),
    ("quantum_ring", "dense_smart"),
    ("quantum_link", "dense_smart"),
    ("spatial_pylon", "smart"),
    ("spatial_io_port", "smart"),
    ("spatial_anchor", "smart"),
    ("controller", "dense_smart"),
    ("drive", "smart"),
    ("chest", "smart"),
    ("interface", "smart"),
    ("io_port", "smart"),
    ("energy_acceptor", "covered"),
    ("crystal_resonance_generator", "smart"),
    ("vibration_chamber", "covered"),
    ("growth_accelerator", "covered"),
    ("energy_cell", "covered"),
    ("dense_energy_cell", "covered"),
    ("creative_energy_cell", "covered"),
    ("crafting_unit", "smart"),
    ("crafting_accelerator", "smart"),
    ("1k_crafting_storage", "smart"),
    ("4k_crafting_storage", "smart"),
    ("16k_crafting_storage", "smart"),
    ("64k_crafting_storage", "smart"),
    ("256k_crafting_storage", "smart"),
    ("crafting_monitor", "smart"),
    ("pattern_provider", "smart"),
    ("molecular_assembler", "covered"),
)
NATIVE_STRUCTURAL_ENDPOINT_POLICIES = {
    "inscriber": {
        "block_entity_id": "ae2:inscriber",
        "required_state": {"facing": "east", "spin": 0, "waterlogged": False},
        "side_rule": "NO_FRONT",
    },
    "wireless_access_point": {
        "block_entity_id": "ae2:wireless_access_point",
        "required_state": {
            "facing": "east",
            "state": "off",
            "waterlogged": False,
        },
        "side_rule": "BACK",
    },
    "charger": {
        "block_entity_id": "ae2:charger",
        "required_state": {"facing": "east", "spin": 0},
        "side_rule": "NO_FRONT",
    },
    "quantum_ring": {
        "block_entity_id": "ae2:quantum_ring",
        "required_state": {"formed": True, "waterlogged": False},
        "side_rule": "FORMED_QUANTUM",
        "formation": "qnb-yz-edge-ring",
    },
    "quantum_link": {
        "block_entity_id": "ae2:quantum_ring",
        "required_state": {"formed": True, "waterlogged": False},
        "side_rule": "FORMED_QUANTUM",
        "formation": "qnb-yz-center-link",
        "connected": False,
    },
    "spatial_pylon": {
        "block_entity_id": "ae2:spatial_pylon",
        "required_state": {"powered_on": False},
        "side_rule": "VALID_STRAIGHT_PYLON",
        "formation": "vertical-three-pylon-middle",
    },
    "spatial_io_port": {
        "block_entity_id": "ae2:spatial_io_port",
        "required_state": {"facing": "north", "powered": False, "spin": 0},
        "side_rule": "ALL",
    },
    "spatial_anchor": {
        "block_entity_id": "ae2:spatial_anchor",
        "required_state": {"facing": "north", "powered": False},
        "side_rule": "ALL",
    },
    "controller": {
        "block_entity_id": "ae2:controller",
        "required_state": {"state": "offline", "type": "block"},
        "side_rule": "ALL",
    },
    "drive": {
        "block_entity_id": "ae2:drive",
        "required_state": {"facing": "east", "spin": 0},
        "side_rule": "NO_FRONT",
    },
    "chest": {
        "block_entity_id": "ae2:chest",
        "required_state": {"facing": "north", "lights_on": False, "spin": 0},
        "side_rule": "ALL",
    },
    "interface": {
        "block_entity_id": "ae2:interface",
        "required_state": {},
        "side_rule": "ALL",
    },
    "io_port": {
        "block_entity_id": "ae2:io_port",
        "required_state": {"facing": "north", "powered": False, "spin": 0},
        "side_rule": "ALL",
    },
    "energy_acceptor": {
        "block_entity_id": "ae2:energy_acceptor",
        "required_state": {},
        "side_rule": "ALL",
    },
    "crystal_resonance_generator": {
        "block_entity_id": "ae2:crystal_resonance_generator",
        "required_state": {"facing": "east", "waterlogged": False},
        "side_rule": "BACK",
    },
    "vibration_chamber": {
        "block_entity_id": "ae2:vibration_chamber",
        "required_state": {"active": False, "facing": "north", "spin": 0},
        "side_rule": "ALL",
    },
    "growth_accelerator": {
        "block_entity_id": "ae2:growth_accelerator",
        "required_state": {"facing": "east", "powered": False},
        "side_rule": "FRONT_BACK",
    },
    "energy_cell": {
        "block_entity_id": "ae2:energy_cell",
        "required_state": {"fullness": 0},
        "side_rule": "ALL",
    },
    "dense_energy_cell": {
        "block_entity_id": "ae2:dense_energy_cell",
        "required_state": {"fullness": 0},
        "side_rule": "ALL",
    },
    "creative_energy_cell": {
        "block_entity_id": "ae2:creative_energy_cell",
        "required_state": {},
        "side_rule": "ALL",
    },
    "crafting_unit": {
        "block_entity_id": "ae2:crafting_unit",
        "required_state": {"formed": True, "powered": False},
        "side_rule": "FORMED_CRAFTING",
        "formation": "vertical-crafting-pair",
    },
    "crafting_accelerator": {
        "block_entity_id": "ae2:crafting_unit",
        "required_state": {"formed": True, "powered": False},
        "side_rule": "FORMED_CRAFTING",
        "formation": "vertical-crafting-pair",
    },
    **{
        name: {
            "block_entity_id": "ae2:crafting_storage",
            "required_state": {"formed": True, "powered": False},
            "side_rule": "FORMED_CRAFTING",
            "formation": (
                "single-storage-crafting-cpu"
                if name == "1k_crafting_storage"
                else "vertical-crafting-pair"
            ),
        }
        for name in (
            "1k_crafting_storage",
            "4k_crafting_storage",
            "16k_crafting_storage",
            "64k_crafting_storage",
            "256k_crafting_storage",
        )
    },
    "crafting_monitor": {
        "block_entity_id": "ae2:crafting_monitor",
        "required_state": {
            "facing": "east",
            "formed": True,
            "powered": False,
            "spin": 0,
        },
        "side_rule": "FORMED_CRAFTING",
        "formation": "vertical-crafting-pair",
    },
    "pattern_provider": {
        "block_entity_id": "ae2:pattern_provider",
        "required_state": {"push_direction": "east"},
        "side_rule": "PUSH_DIRECTION",
    },
    "molecular_assembler": {
        "block_entity_id": "ae2:molecular_assembler",
        "required_state": {"powered": False},
        "side_rule": "ALL",
    },
}
_NATIVE_ENDPOINT_FACING_DOMAIN = [
    "down", "up", "north", "south", "west", "east"
]
_NATIVE_ENDPOINT_SPIN_DOMAIN = ["0", "1", "2", "3"]
_NATIVE_ENDPOINT_BOOLEAN_DOMAIN = ["false", "true"]
_NATIVE_ENDPOINT_FORMED_POWERED_SCHEMA = {
    "formed": _NATIVE_ENDPOINT_BOOLEAN_DOMAIN,
    "powered": _NATIVE_ENDPOINT_BOOLEAN_DOMAIN,
}
NATIVE_STRUCTURAL_ENDPOINT_STATE_SCHEMAS = {
    "inscriber": {
        "facing": _NATIVE_ENDPOINT_FACING_DOMAIN,
        "spin": _NATIVE_ENDPOINT_SPIN_DOMAIN,
        "waterlogged": _NATIVE_ENDPOINT_BOOLEAN_DOMAIN,
    },
    "wireless_access_point": {
        "facing": _NATIVE_ENDPOINT_FACING_DOMAIN,
        "state": ["off", "on", "has_channel"],
        "waterlogged": _NATIVE_ENDPOINT_BOOLEAN_DOMAIN,
    },
    "charger": {
        "facing": _NATIVE_ENDPOINT_FACING_DOMAIN,
        "spin": _NATIVE_ENDPOINT_SPIN_DOMAIN,
    },
    "quantum_ring": {
        "formed": _NATIVE_ENDPOINT_BOOLEAN_DOMAIN,
        "waterlogged": _NATIVE_ENDPOINT_BOOLEAN_DOMAIN,
    },
    "quantum_link": {
        "formed": _NATIVE_ENDPOINT_BOOLEAN_DOMAIN,
        "waterlogged": _NATIVE_ENDPOINT_BOOLEAN_DOMAIN,
    },
    "spatial_pylon": {"powered_on": _NATIVE_ENDPOINT_BOOLEAN_DOMAIN},
    "spatial_io_port": {
        "facing": _NATIVE_ENDPOINT_FACING_DOMAIN,
        "powered": _NATIVE_ENDPOINT_BOOLEAN_DOMAIN,
        "spin": _NATIVE_ENDPOINT_SPIN_DOMAIN,
    },
    "spatial_anchor": {
        "facing": _NATIVE_ENDPOINT_FACING_DOMAIN,
        "powered": _NATIVE_ENDPOINT_BOOLEAN_DOMAIN,
    },
    "controller": {
        "state": ["offline", "online", "conflicted"],
        "type": [
            "block", "column_x", "column_y", "column_z", "inside_a", "inside_b"
        ],
    },
    "drive": {
        "facing": _NATIVE_ENDPOINT_FACING_DOMAIN,
        "spin": _NATIVE_ENDPOINT_SPIN_DOMAIN,
    },
    "chest": {
        "facing": _NATIVE_ENDPOINT_FACING_DOMAIN,
        "lights_on": _NATIVE_ENDPOINT_BOOLEAN_DOMAIN,
        "spin": _NATIVE_ENDPOINT_SPIN_DOMAIN,
    },
    "interface": {},
    "io_port": {
        "facing": _NATIVE_ENDPOINT_FACING_DOMAIN,
        "powered": _NATIVE_ENDPOINT_BOOLEAN_DOMAIN,
        "spin": _NATIVE_ENDPOINT_SPIN_DOMAIN,
    },
    "energy_acceptor": {},
    "crystal_resonance_generator": {
        "facing": _NATIVE_ENDPOINT_FACING_DOMAIN,
        "waterlogged": _NATIVE_ENDPOINT_BOOLEAN_DOMAIN,
    },
    "vibration_chamber": {
        "active": _NATIVE_ENDPOINT_BOOLEAN_DOMAIN,
        "facing": _NATIVE_ENDPOINT_FACING_DOMAIN,
        "spin": _NATIVE_ENDPOINT_SPIN_DOMAIN,
    },
    "growth_accelerator": {
        "facing": _NATIVE_ENDPOINT_FACING_DOMAIN,
        "powered": _NATIVE_ENDPOINT_BOOLEAN_DOMAIN,
    },
    "energy_cell": {"fullness": ["0", "1", "2", "3", "4"]},
    "dense_energy_cell": {"fullness": ["0", "1", "2", "3", "4"]},
    "creative_energy_cell": {},
    "crafting_unit": _NATIVE_ENDPOINT_FORMED_POWERED_SCHEMA,
    "crafting_accelerator": _NATIVE_ENDPOINT_FORMED_POWERED_SCHEMA,
    "1k_crafting_storage": _NATIVE_ENDPOINT_FORMED_POWERED_SCHEMA,
    "4k_crafting_storage": _NATIVE_ENDPOINT_FORMED_POWERED_SCHEMA,
    "16k_crafting_storage": _NATIVE_ENDPOINT_FORMED_POWERED_SCHEMA,
    "64k_crafting_storage": _NATIVE_ENDPOINT_FORMED_POWERED_SCHEMA,
    "256k_crafting_storage": _NATIVE_ENDPOINT_FORMED_POWERED_SCHEMA,
    "crafting_monitor": {
        "facing": _NATIVE_ENDPOINT_FACING_DOMAIN,
        "formed": _NATIVE_ENDPOINT_BOOLEAN_DOMAIN,
        "powered": _NATIVE_ENDPOINT_BOOLEAN_DOMAIN,
        "spin": _NATIVE_ENDPOINT_SPIN_DOMAIN,
    },
    "pattern_provider": {
        "push_direction": [
            "down", "up", "north", "south", "west", "east", "all"
        ]
    },
    "molecular_assembler": {"powered": _NATIVE_ENDPOINT_BOOLEAN_DOMAIN},
}
NATIVE_STRUCTURAL_ENDPOINT_STATE_COUNTS = {
    "inscriber": 48,
    "wireless_access_point": 36,
    "charger": 24,
    "quantum_ring": 4,
    "quantum_link": 4,
    "spatial_pylon": 2,
    "spatial_io_port": 48,
    "spatial_anchor": 12,
    "controller": 18,
    "drive": 24,
    "chest": 48,
    "interface": 1,
    "io_port": 48,
    "energy_acceptor": 1,
    "crystal_resonance_generator": 12,
    "vibration_chamber": 48,
    "growth_accelerator": 12,
    "energy_cell": 5,
    "dense_energy_cell": 5,
    "creative_energy_cell": 1,
    "crafting_unit": 4,
    "crafting_accelerator": 4,
    "1k_crafting_storage": 4,
    "4k_crafting_storage": 4,
    "16k_crafting_storage": 4,
    "64k_crafting_storage": 4,
    "256k_crafting_storage": 4,
    "crafting_monitor": 96,
    "pattern_provider": 7,
    "molecular_assembler": 2,
}
NATIVE_STRUCTURAL_ENDPOINT_STATE_CARTESIAN_COUNT = 534
NATIVE_STRUCTURAL_ENDPOINT_STATE_SIDE_CARTESIAN_COUNT = 3_204
NATIVE_STRUCTURAL_ENDPOINT_BLOCKSTATE_SHA256 = {
    "inscriber": "4ec6c21834e68f179c252bf22aeb8f8f67d57ef057eb8bde57f65f576e0885f2",
    "wireless_access_point": "05c09f9e0bcb7a09ee8f2566a2eb3f885549df2765f624c7c44fed87eee6cf6e",
    "charger": "83ebcbf59495865f7302e58292f81b83231016b1fce15515ccc10cb158f73d76",
    "quantum_ring": "3db38f2e82cd1a9e1e2e45cb078d09f0d01507750cd895615bb9a7f722f27c50",
    "quantum_link": "156f0aeafca2763f1e3fccadd342c08da7870bcb3aa8f176127a2a3502b3aa7d",
    "spatial_pylon": "a3c18208840e313823afc7198e8d74da9b1e65e78dffdc6327f53d2b70e678c9",
    "spatial_io_port": "fd2ff71aef6d77ea08dcd5aa80d7972f802a7d2ecf788cf45dbc26ade51fd542",
    "spatial_anchor": "38019a1eda66fef56bf493d818fe3452cbd8367f57fecf29acbe80f7d430837f",
    "controller": "693d04c733b47e4159052d0843256fa7520bbc1984b6d9e454bec976a73d2ca8",
    "drive": "b69d86cf730369715ad51f23793efb9b6910ec9760d4ab40029e128046d204ce",
    "chest": "c628dab804fe28fa813fef46ddcf2e4f5f13192e63cea2a7f8b8dcc3d0810ed0",
    "interface": "1bc532291c1343d076662eb69d6913953b27f91ce5d722a5b78c6095f56167ee",
    "io_port": "601dccfb290cfd7f70c2f1e0662082e4f17c10ecfec3857b55f96db13113dbcf",
    "energy_acceptor": "ee3ffe5a1fc5269a13b4474bd23ef8f98869a528bda2287ba849ca4fd4f14a7c",
    "crystal_resonance_generator": "11d0a847d7abfb1db1acb8a748a3203aa7af9b76ff4c194c288ddc29d131229d",
    "vibration_chamber": "6555d07d339d0fd2af34f5b7f4fbf574313df8701544bdb2e4189a17dcc3038c",
    "growth_accelerator": "57cd5e8741a98c81b4db43bd796beaae8e9f1f227c9eeac03164b6552e8f1212",
    "energy_cell": "2e285ec4568671ea1185c70c6f38ab3a943cf24dcdd7847fe0886871409ea0fa",
    "dense_energy_cell": "357108af0d785e58fea6240d4cba13e81b686caadaab974fcf30c0ea99ca616d",
    "creative_energy_cell": "e924240fd1c63be2a7033f764213c9d0f3d8cab2269d7e73dc1f7abadb18de80",
    "crafting_unit": "b33f03d38953281265d6196e2a9f2494974275901b570f390ebf40fa3a338ece",
    "crafting_accelerator": "f2b8fd7efa88b37968f55d8169eee48d84c1c673b5b2201719037771d5e18918",
    "1k_crafting_storage": "9a1f6383cd3b54a8361cefc46740ddbee587ce79baefccb6ad6de6355833a603",
    "4k_crafting_storage": "dd4210a4c0fc5b0eb7f524571f20b7e1a92c438bc68df7324cb26c939c726abc",
    "16k_crafting_storage": "8e04febb39f74e1bb1061f9fee979be9cc4923bf14cc5a5d619cf6e681d506a4",
    "64k_crafting_storage": "d8a1b0f2f21c2f05cd959f03213d0434c6bb41e27d5591d0c3c532aea142eb7f",
    "256k_crafting_storage": "3458c6e521a76f7a0761c7efe956cc587826cfdd40d1f7c6284100990fb68905",
    "crafting_monitor": "157e2a326b835180b369874b5f6978fab7c6796293945f85a971ac3f5b1cf2b7",
    "pattern_provider": "1b8e3a67480db0dec346477a67e026798b7287db7b48b4242f58d405035b0b83",
    "molecular_assembler": "136857cc899a24bcca0b730790da3128a74c9e8196028a264e32e5e1582183a0",
}
NATIVE_STRUCTURAL_FACADE_DIRECTIONS = tuple(DIRECTION_DELTAS)
NATIVE_STRUCTURAL_PLANE_MASK_DIRECTIONS = {
    "up": ("north", "east", "south", "west"),
    "north": ("up", "west", "down", "east"),
}
NATIVE_STRUCTURAL_PLANE_MASK_BITS = (8, 4, 2, 1)
NATIVE_STRUCTURAL_PLANE_VISUAL_LOCAL_BOUND_BITS = (4, 1, 2, 8)
NATIVE_STRUCTURAL_PLANE_VISUAL_LOCAL_AXES = {
    "down": ("east", "north", "up"),
    "up": ("west", "north", "down"),
    "north": ("east", "up", "south"),
    "south": ("west", "up", "north"),
    "west": ("north", "up", "east"),
    "east": ("south", "up", "west"),
}
NATIVE_STRUCTURAL_PLANE_COLLISION_LOCAL_BOUND_BITS = {
    "down": (4, 1, 2, 8),
    "up": (1, 4, 8, 2),
    "north": (1, 4, 2, 8),
    "south": (1, 4, 2, 8),
    "west": (4, 1, 2, 8),
    "east": (4, 1, 2, 8),
}
NATIVE_STRUCTURAL_PLANE_COLLISION_LOCAL_AXES = {
    "down": ("east", "north", "down"),
    "up": ("east", "south", "up"),
    "north": ("west", "up", "north"),
    "south": ("east", "up", "south"),
    "west": ("north", "up", "west"),
    "east": ("south", "up", "east"),
}
NATIVE_STRUCTURAL_UNKNOWN_EXTENSION_ENDPOINT = {
    "block_id": "expandedae:exp_io_port",
    "block_entity_id": "expandedae:exp_io_port",
    "required_state": {"facing": "north", "powered": False, "spin": 0},
    "observed_endpoint_side": "west",
    "side_rule": "UNSUPPORTED_COMPATIBLE_GRID_HOST",
    "artifact": "expandedae-2.1.1",
    "artifact_sha256": (
        "f39c0eb9c6271f54a44ffee092a29520f53000d1005849e6afada3ad9dffba14"
    ),
}
NATIVE_STRUCTURAL_UNKNOWN_ENDPOINT_ENTRIES_SHA256 = (
    "9d30ad8f985b66db4a7e9858479234df7b1fbfe886a5e786d59a95dc4e3149c2"
)
NATIVE_STRUCTURAL_UNKNOWN_ENDPOINT_ARTIFACTS_SHA256 = (
    "405c1bfe797755841bdea8d322f5a016d15412de232b50ff31800c5bd0b5ad5a"
)
NATIVE_STRUCTURAL_FACADE_WHITELIST_IDS = (
    "ae2:quartz_glass",
    "ae2:quartz_vibrant_glass",
    "minecraft:chiseled_bookshelf",
    "minecraft:jukebox",
    "minecraft:furnace",
    "minecraft:blast_furnace",
    "minecraft:dropper",
    "minecraft:dispenser",
    "minecraft:crafter",
    "minecraft:barrel",
    "minecraft:bee_nest",
    "minecraft:beehive",
    "minecraft:sculk_catalyst",
    "minecraft:soul_sand",
    "minecraft:honey_block",
    "ae2:controller",
    "ae2:1k_crafting_storage",
    "ae2:4k_crafting_storage",
    "ae2:16k_crafting_storage",
    "ae2:64k_crafting_storage",
    "ae2:256k_crafting_storage",
    "ae2:crafting_monitor",
    "ae2:crafting_unit",
    "ae2:crafting_accelerator",
)
NATIVE_STRUCTURAL_NEUTRAL_FACADE_MATERIALS = (
    {
        "block_id": "ae2:quartz_glass",
        "properties": {},
        "material_family": "facade-aware-connected-quartz-glass-static",
        "source_model": "ae2:block/quartz_glass",
        "blockstate_sha256": "9c331aa0f423a364e136b731195caf168df6496a90a065f9699e5e8e37e70d50",
    },
    {
        "block_id": "ae2:quartz_vibrant_glass",
        "properties": {},
        "material_family": "facade-aware-connected-quartz-glass-static",
        "source_model": "ae2:block/quartz_glass",
        "blockstate_sha256": "e3b2b20544e578ff4b9d908ca1e7d281ecc46ddd8f0ee496ad53e2e344e17a99",
    },
    {
        "block_id": "ae2:controller",
        "properties": {"state": "offline", "type": "block"},
        "material_family": "controller-offline-block",
        "source_model": "ae2:block/controller/controller_block_offline",
        "blockstate_sha256": "693d04c733b47e4159052d0843256fa7520bbc1984b6d9e454bec976a73d2ca8",
    },
    {
        "block_id": "ae2:1k_crafting_storage",
        "properties": {"formed": "false", "powered": "false"},
        "material_family": "crafting-storage-1k-unformed",
        "source_model": "ae2:block/crafting/1k_storage",
        "blockstate_sha256": "9a1f6383cd3b54a8361cefc46740ddbee587ce79baefccb6ad6de6355833a603",
    },
    {
        "block_id": "ae2:4k_crafting_storage",
        "properties": {"formed": "false", "powered": "false"},
        "material_family": "crafting-storage-4k-unformed",
        "source_model": "ae2:block/crafting/4k_storage",
        "blockstate_sha256": "dd4210a4c0fc5b0eb7f524571f20b7e1a92c438bc68df7324cb26c939c726abc",
    },
    {
        "block_id": "ae2:16k_crafting_storage",
        "properties": {"formed": "false", "powered": "false"},
        "material_family": "crafting-storage-16k-unformed",
        "source_model": "ae2:block/crafting/16k_storage",
        "blockstate_sha256": "8e04febb39f74e1bb1061f9fee979be9cc4923bf14cc5a5d619cf6e681d506a4",
    },
    {
        "block_id": "ae2:64k_crafting_storage",
        "properties": {"formed": "false", "powered": "false"},
        "material_family": "crafting-storage-64k-unformed",
        "source_model": "ae2:block/crafting/64k_storage",
        "blockstate_sha256": "d8a1b0f2f21c2f05cd959f03213d0434c6bb41e27d5591d0c3c532aea142eb7f",
    },
    {
        "block_id": "ae2:256k_crafting_storage",
        "properties": {"formed": "false", "powered": "false"},
        "material_family": "crafting-storage-256k-unformed",
        "source_model": "ae2:block/crafting/256k_storage",
        "blockstate_sha256": "3458c6e521a76f7a0761c7efe956cc587826cfdd40d1f7c6284100990fb68905",
    },
    {
        "block_id": "ae2:crafting_monitor",
        "properties": {
            "facing": "north",
            "formed": "false",
            "powered": "false",
            "spin": "0",
        },
        "material_family": "crafting-monitor-unformed-north",
        "source_model": "ae2:block/crafting/monitor",
        "blockstate_sha256": "157e2a326b835180b369874b5f6978fab7c6796293945f85a971ac3f5b1cf2b7",
    },
    {
        "block_id": "ae2:crafting_unit",
        "properties": {"formed": "false", "powered": "false"},
        "material_family": "crafting-unit-unformed",
        "source_model": "ae2:block/crafting/unit",
        "blockstate_sha256": "b33f03d38953281265d6196e2a9f2494974275901b570f390ebf40fa3a338ece",
    },
    {
        "block_id": "ae2:crafting_accelerator",
        "properties": {"formed": "false", "powered": "false"},
        "material_family": "crafting-accelerator-unformed",
        "source_model": "ae2:block/crafting/accelerator",
        "blockstate_sha256": "f2b8fd7efa88b37968f55d8169eee48d84c1c673b5b2201719037771d5e18918",
    },
)
NATIVE_STRUCTURAL_VANILLA_WHITELIST_CONTROLS = (
    {
        "block_id": "minecraft:chiseled_bookshelf",
        "properties": {
            "facing": "north",
            **{f"slot_{slot}_occupied": "false" for slot in range(6)},
        },
        "control": "empty-chiseled-bookshelf",
        "is_solid_render": True,
    },
    {
        "block_id": "minecraft:furnace",
        "properties": {"facing": "north", "lit": "false"},
        "control": "unlit-north-furnace",
        "is_solid_render": True,
    },
    {
        "block_id": "minecraft:soul_sand",
        "properties": {},
        "control": "property-free-opaque-soul-sand",
        "is_solid_render": True,
    },
    {
        "block_id": "minecraft:honey_block",
        "properties": {},
        "control": "property-free-transparent-honey-block",
        "is_solid_render": False,
    },
)
NATIVE_STRUCTURAL_FACADE_WHITELIST_NEUTRAL_STATES = {
    **{
        entry["block_id"]: entry["properties"]
        for entry in NATIVE_STRUCTURAL_NEUTRAL_FACADE_MATERIALS
    },
    "minecraft:chiseled_bookshelf": {
        "facing": "north",
        **{f"slot_{slot}_occupied": "false" for slot in range(6)},
    },
    "minecraft:jukebox": {"has_record": "false"},
    "minecraft:furnace": {"facing": "north", "lit": "false"},
    "minecraft:blast_furnace": {"facing": "north", "lit": "false"},
    "minecraft:dropper": {"facing": "north", "triggered": "false"},
    "minecraft:dispenser": {"facing": "north", "triggered": "false"},
    "minecraft:crafter": {
        "crafting": "false",
        "orientation": "north_up",
        "triggered": "false",
    },
    "minecraft:barrel": {"facing": "north", "open": "false"},
    "minecraft:bee_nest": {"facing": "north", "honey_level": "0"},
    "minecraft:beehive": {"facing": "north", "honey_level": "0"},
    "minecraft:sculk_catalyst": {"bloom": "false"},
    "minecraft:soul_sand": {},
    "minecraft:honey_block": {},
}
_NATIVE_STRUCTURAL_VANILLA_FACADE_STATE_SCHEMAS = {
    "minecraft:chiseled_bookshelf": {
        "facing": ["north", "south", "west", "east"],
        **{
            f"slot_{slot}_occupied": _NATIVE_ENDPOINT_BOOLEAN_DOMAIN
            for slot in range(6)
        },
    },
    "minecraft:jukebox": {"has_record": _NATIVE_ENDPOINT_BOOLEAN_DOMAIN},
    "minecraft:furnace": {
        "facing": ["north", "south", "west", "east"],
        "lit": _NATIVE_ENDPOINT_BOOLEAN_DOMAIN,
    },
    "minecraft:blast_furnace": {
        "facing": ["north", "south", "west", "east"],
        "lit": _NATIVE_ENDPOINT_BOOLEAN_DOMAIN,
    },
    "minecraft:dropper": {
        "facing": _NATIVE_ENDPOINT_FACING_DOMAIN,
        "triggered": _NATIVE_ENDPOINT_BOOLEAN_DOMAIN,
    },
    "minecraft:dispenser": {
        "facing": _NATIVE_ENDPOINT_FACING_DOMAIN,
        "triggered": _NATIVE_ENDPOINT_BOOLEAN_DOMAIN,
    },
    "minecraft:crafter": {
        "crafting": _NATIVE_ENDPOINT_BOOLEAN_DOMAIN,
        "orientation": [
            "down_east", "down_north", "down_south", "down_west",
            "up_east", "up_north", "up_south", "up_west",
            "west_up", "east_up", "north_up", "south_up",
        ],
        "triggered": _NATIVE_ENDPOINT_BOOLEAN_DOMAIN,
    },
    "minecraft:barrel": {
        "facing": _NATIVE_ENDPOINT_FACING_DOMAIN,
        "open": _NATIVE_ENDPOINT_BOOLEAN_DOMAIN,
    },
    "minecraft:bee_nest": {
        "facing": ["north", "south", "west", "east"],
        "honey_level": ["0", "1", "2", "3", "4", "5"],
    },
    "minecraft:beehive": {
        "facing": ["north", "south", "west", "east"],
        "honey_level": ["0", "1", "2", "3", "4", "5"],
    },
    "minecraft:sculk_catalyst": {"bloom": _NATIVE_ENDPOINT_BOOLEAN_DOMAIN},
    "minecraft:soul_sand": {},
    "minecraft:honey_block": {},
}
_NATIVE_STRUCTURAL_NATIVE_FACADE_STATE_SCHEMAS = {
    "ae2:quartz_glass": {},
    "ae2:quartz_vibrant_glass": {},
    "ae2:controller": {
        "state": ["offline", "online", "conflicted"],
        "type": [
            "block", "column_x", "column_y", "column_z", "inside_a", "inside_b"
        ],
    },
    **{
        f"ae2:{name}": _NATIVE_ENDPOINT_FORMED_POWERED_SCHEMA
        for name in (
            "1k_crafting_storage",
            "4k_crafting_storage",
            "16k_crafting_storage",
            "64k_crafting_storage",
            "256k_crafting_storage",
            "crafting_unit",
            "crafting_accelerator",
        )
    },
    "ae2:crafting_monitor": {
        "facing": _NATIVE_ENDPOINT_FACING_DOMAIN,
        "formed": _NATIVE_ENDPOINT_BOOLEAN_DOMAIN,
        "powered": _NATIVE_ENDPOINT_BOOLEAN_DOMAIN,
        "spin": _NATIVE_ENDPOINT_SPIN_DOMAIN,
    },
}
NATIVE_STRUCTURAL_FACADE_WHITELIST_STATE_SCHEMAS = {
    block_id: (
        _NATIVE_STRUCTURAL_NATIVE_FACADE_STATE_SCHEMAS[block_id]
        if block_id in _NATIVE_STRUCTURAL_NATIVE_FACADE_STATE_SCHEMAS
        else _NATIVE_STRUCTURAL_VANILLA_FACADE_STATE_SCHEMAS[block_id]
    )
    for block_id in NATIVE_STRUCTURAL_FACADE_WHITELIST_IDS
}
NATIVE_STRUCTURAL_FACADE_WHITELIST_BLOCKSTATE_SHA256 = {
    **{
        entry["block_id"]: entry["blockstate_sha256"]
        for entry in NATIVE_STRUCTURAL_NEUTRAL_FACADE_MATERIALS
    },
    "minecraft:chiseled_bookshelf": (
        "7f3f363d1e155d92d08916d8f08de670e269ae4a05fce0844c8bcd6930e8d098"
    ),
    "minecraft:jukebox": (
        "8002563a048d4a5afb22d44692ca1a38e114ef95a3ccd24f02b9e0fd02b693d5"
    ),
    "minecraft:furnace": (
        "aedb43571027a5dea15ba9cbfc05f0327af3048de70b72c3cd67c851839bb284"
    ),
    "minecraft:blast_furnace": (
        "265ec5f30fa65bdaff6867bbad8de73e0a1b21ea12a33da5b771f889e4ac7dcc"
    ),
    "minecraft:dropper": (
        "c763060c1946a3031cdf6e68ab98db7d81d83c364b0ad4da54fdf055225753c3"
    ),
    "minecraft:dispenser": (
        "fc1ba39eb47f31285b5d1c9f729fabf5ad9832d8b8f6b1f510d3c870f6e6bfd8"
    ),
    "minecraft:crafter": (
        "dfa8af74cd96d1d6f2086a63fab3402864497d7f436ba469b34be58924f1edfa"
    ),
    "minecraft:barrel": (
        "d8e00576b5f85f83a42b7b31dc177e0add02cf2204fa441c0fe31cbd6d70dcca"
    ),
    "minecraft:bee_nest": (
        "09ee024cc05e40767c3e88776e336396a71b146a7fb93f64ba1860aa6a107853"
    ),
    "minecraft:beehive": (
        "c4c438bb21bf78f5bdc8835daf852c2f2040e3f96872fe4488d2710a6abfc8ae"
    ),
    "minecraft:sculk_catalyst": (
        "0e6c7b956647211dea0d7ce46e9e111296dd985c51d58a3c094630850b764504"
    ),
    "minecraft:soul_sand": (
        "6a0ea83a331843c30e21f8d7ea9252c429c4093b6222b60e62e8ab47ca802ef8"
    ),
    "minecraft:honey_block": (
        "780ffcffff91d90efe172f2f1f200a06dcbe885fe5316d16bebf72bae2ef7c44"
    ),
}
NATIVE_STRUCTURAL_FACADE_WHITELIST_STATE_POLICY = (
    "all-24-explicit-whitelist-families-require-exact-complete-persisted-"
    "property-key-set-and-value-domains;13-vanilla-families-preserve-valid-"
    "state;11-ae2-native-families-apply-declared-static-normalization;extra-"
    "missing-or-invalid-properties-atomic-original-resource-fallback"
)
NATIVE_STRUCTURAL_FACADE_WHITELIST_STATE_CONTRACT_SHA256 = (
    "ec3423ebbd53eacf06cef8a53c1d085cad3ae654c45b6aecea177caa40bc60a8"
)
NATIVE_STRUCTURAL_FACADE_WHITELIST_STATE_COUNTS = {
    "ae2:quartz_glass": 1,
    "ae2:quartz_vibrant_glass": 1,
    "minecraft:chiseled_bookshelf": 256,
    "minecraft:jukebox": 2,
    "minecraft:furnace": 8,
    "minecraft:blast_furnace": 8,
    "minecraft:dropper": 12,
    "minecraft:dispenser": 12,
    "minecraft:crafter": 48,
    "minecraft:barrel": 12,
    "minecraft:bee_nest": 24,
    "minecraft:beehive": 24,
    "minecraft:sculk_catalyst": 2,
    "minecraft:soul_sand": 1,
    "minecraft:honey_block": 1,
    "ae2:controller": 18,
    "ae2:1k_crafting_storage": 4,
    "ae2:4k_crafting_storage": 4,
    "ae2:16k_crafting_storage": 4,
    "ae2:64k_crafting_storage": 4,
    "ae2:256k_crafting_storage": 4,
    "ae2:crafting_monitor": 96,
    "ae2:crafting_unit": 4,
    "ae2:crafting_accelerator": 4,
}
NATIVE_STRUCTURAL_FACADE_WHITELIST_STATE_CARTESIAN_COUNT = 554
NATIVE_STRUCTURAL_FACADE_STATE_CLASSIFICATION_POLICY = (
    "solidRender-and-same-state-skipRendering-family-invariant-across-all-554-"
    "valid-explicit-whitelist-states;neutral-default-row-booleans-apply-to-whole-"
    "family;classification-drift-atomic-original-resource-fallback"
)
NATIVE_STRUCTURAL_FACADE_SOLID_RENDER_TRUE_CARTESIAN_COUNT = 551
NATIVE_STRUCTURAL_FACADE_SAME_STATE_SKIP_TRUE_CARTESIAN_COUNT = 3


def native_structural_state_schema_count(
    schema: dict[str, list[str]],
) -> int:
    count = 1
    for domain in schema.values():
        count *= len(domain)
    return count


def validate_native_structural_whitelist_facade_state(
    state: Any,
) -> None:
    """Reject whitelist BlockStates that can be hidden by a default variant."""

    if not isinstance(state, dict):
        raise ValueError("S1 whitelist facade state is not a compound")
    block_id = state.get("Name")
    if block_id not in NATIVE_STRUCTURAL_FACADE_WHITELIST_IDS:
        return
    if set(state) - {"Name", "Properties"}:
        raise ValueError(f"S1 whitelist facade {block_id} has extra state fields")
    properties = state.get("Properties", {})
    schema = NATIVE_STRUCTURAL_FACADE_WHITELIST_STATE_SCHEMAS[block_id]
    if not isinstance(properties, dict) or set(properties) != set(schema):
        raise ValueError(
            f"S1 whitelist facade {block_id} lacks the exact property-key set"
        )
    for key, value in properties.items():
        if not isinstance(value, str) or value not in schema[key]:
            raise ValueError(
                f"S1 whitelist facade {block_id} property {key} is out of domain"
            )


NATIVE_STRUCTURAL_FACADE_WHITELIST_SOLID_RENDER = {
    block_id: block_id
    not in {
        "ae2:quartz_glass",
        "ae2:quartz_vibrant_glass",
        "minecraft:honey_block",
    }
    for block_id in NATIVE_STRUCTURAL_FACADE_WHITELIST_IDS
}
NATIVE_STRUCTURAL_FACADE_WHITELIST_SAME_STATE_SKIP_RENDERING = {
    block_id: block_id
    in {
        "ae2:quartz_glass",
        "ae2:quartz_vibrant_glass",
        "minecraft:honey_block",
    }
    for block_id in NATIVE_STRUCTURAL_FACADE_WHITELIST_IDS
}
NATIVE_STRUCTURAL_ORDINARY_FACADE_SAME_STATE_SKIP_RENDERING = {
    "minecraft:glass": True,
    "minecraft:oak_log": False,
    "minecraft:oak_leaves": False,
}
NATIVE_STRUCTURAL_GLASSENTIAL_FULL_PACK_OVERRIDE = {
    "mod_id": "glassential",
    "version": "3.4.5",
    "artifact": "Glassential-renewed-1.21.1-3.4.5.jar",
    "artifact_size_bytes": 702_249,
    "artifact_sha1": "3a08f59f0930c8123fa1aacdfa0ba9fbdbb6e342",
    "artifact_sha256": (
        "1f0c8f7533bf3b2002575219ba795fd32a44cc5085c2710624ebbf69e6121471"
    ),
    "artifact_sha512": (
        "62ccb9057aab96ba656ec8ce357977360c1cc7761fedd7ac995a40b1f16e389c7"
        "5d753746840b11d30077b6b896938246fb281ec481e560a05084e22098c31d8"
    ),
    "curseforge_project_id": 945_149,
    "curseforge_file_id": 8_440_850,
    "modrinth_project_id": "kc9FSsYx",
    "modrinth_version_id": "ZU9ErRM9",
    "model_path": "assets/minecraft/models/block/glass.json",
    "model_sha256": (
        "dc3cf6fdf740fceb4d2224dcb4132ab103617d0b904fcbbf6b48dbee0ecc9e4e"
    ),
    "texture_path": "assets/glassential/textures/block/glass.png",
    "texture_sha256": (
        "0a5534e6eb350dbce3670d9a4bc98f98ef20fb0747068d374f3529842b902370"
    ),
    "texture_mcmeta_path": (
        "assets/glassential/textures/block/glass.png.mcmeta"
    ),
    "texture_mcmeta_size_bytes": 97,
    "texture_mcmeta_sha256": (
        "23117542de8eb132a734e588a7cac393e7d8375632e4df56cf31010a8360c719"
    ),
    "texture_mcmeta_fusion": {
        "type": "connecting",
        "layout": "full",
        "render_type": "cutout",
    },
    "texture_mcmeta_change_from_3_4_4": (
        "fusion.render_type:translucent-to-cutout-only"
    ),
    "bluemap_5_22_mcmeta_policy": (
        "non-animation-top-level-metadata-ignored;stored-material-uses-png"
    ),
    "client_visual_acceptance": (
        "must-rerun-after-fusion-render-type-translucent-to-cutout"
    ),
    "resolved_material": "glassential:block/glass",
    "priority": "first-resource-wins-glassential-before-minecraft",
}
NATIVE_STRUCTURAL_FACADE_TINT_POLICY = (
    "untinted-or-one-distinct-nonnegative-source-tint-index;untinted-layers-may-coexist;"
    "shared-tinted-layers-use-host-block-color-calculator;mixed-nonnegative-tint-indices-"
    "atomic-original-resource-fallback"
)
NATIVE_STRUCTURAL_FACADE_UV_REINTERPOLATION_POLICY = (
    "source-QuadReInterpolator-nominal-face-2d-dx-dy-bilinear;admitted-quad-projection-"
    "requires-exact-complete-InterpHelper-grid;post-clamp-and-corner-kick-target-uses-"
    "projected-dx-dy;noncompatible-projected-quads-atomic-original-resource-fallback"
)
NATIVE_STRUCTURAL_FACADE_CARDINAL_VARIANT_TRANSFORM_POLICY = (
    "exact-signed-permutation-quarter-turn-blockstate-variant-and-uvlock-coordinate-"
    "transforms;avoids-host-float-matrix-drift-before-source-exact-InterpHelper-grid"
)
NATIVE_STRUCTURAL_FACADE_ORDINARY_MATERIAL_POLICY = (
    "optional-c-glass-blocks-and-ordinary-FacadeItem-eligible-states-require-one-live-"
    "unrotated-0-to-16-six-face-full-cube-witness;bounded-additional-static-elements-"
    "and-multipart-source-quads-subject-to-uv-tint-weighted-and-semantic-resource-gates;"
    "otherwise-valid-complex-static-models-atomic-original-resource-fallback"
)
NATIVE_STRUCTURAL_FACADE_WEIGHTED_VARIANT_POLICY = (
    "exact-minecraft-stone-four-alternative-geometry-and-material-host-position-"
    "projection-retains-frozen-M2-non-pixel-identical-randomized-uv-boundary;all-other-"
    "weighted-sets-require-every-alternative-collapse-to-one-bounded-static-geometry-"
    "material-uv-descriptor;otherwise-atomic-original-resource-fallback"
)
NATIVE_STRUCTURAL_FACADE_SKIP_RENDERING_POLICY = (
    "exact-24-explicit-whitelist-same-state-table;ae2-quartz-glass-cross-family-render-"
    "shape-rule;exact-gallery-controls-glass-true-oak-log-false-oak-leaves-false;other-"
    "ordinary-tag-materials-use-bounded-BlueMap-cullingIdentical-same-state-host-projection"
)
NATIVE_STRUCTURAL_FACADE_QUARTZ_SKIP_RENDERING_POLICY = (
    "true-for-any-two-ae2-QuartzGlassBlock-families-with-equal-render-shape"
)
NATIVE_STRUCTURAL_FACADE_CUTOUT_AABB_POLICY = (
    "minecraft-AABB-normalizes-each-generated-strip-endpoint-pair-with-min-max;"
    "transparent-inset-plus-boundary-reaching-cutout-may-reverse-endpoints-and-must-"
    "produce-the-normalized-strip-not-a-degenerate-strip"
)
NATIVE_STRUCTURAL_FACADE_AO_DIRECTION_POLICY = (
    "BlueMap-ResourceModelRenderer-source-faceDir-rotated-by-blockstate-variant-only;"
    "element-rotation-affects-vertices-not-AO-direction;runtime-uses-layer-lightFace-"
    "not-quad-nominal-face"
)
NATIVE_STRUCTURAL_MAP_COLOR_ILLUMINATION_POLICY = (
    "BlueMap-map-color-illumination-uses-original-center-and-outward-world-light-only;"
    "element-lightEmission-affects-triangle-blocklight-not-map-color-brightness"
)
# The raw/source-projected matrix is frozen independently.  The subordinate
# rendered regression identity remains fail-closed until the runtime export.
S1_RAW_LOGICAL_MATRIX_SHA256 = (
    "b797930fc3f8eca822d0cbc674a4cc264671382db94d7627d9ba991c5d71fae8"
)
S1_RAW_MATRIX_SIZE_BYTES = 291_087
S1_RAW_STRIPPED_LOGICAL_MATRIX_SIZE_BYTES = 290_835
S1_RAW_STRIPPED_LOGICAL_MATRIX_SHA256 = (
    "332a33356ab887c31b8973e29ce0146b8cf900a0bd4b797b2ace2c82775c9540"
)
S1_ORACLE_SIZE_BYTES = 198_162
S1_ORACLE_SHA256 = (
    "ac9a54cee9a20be18e71d6c9fe4f16b894827d43bb49cb4d0e56c673280cec39"
)
S1_ORACLE_ANCHOR_COUNT = 351
S1_ORACLE_TRIANGLE_COUNT = 37_518
S1_ORACLE_IDENTITY_COUNT = 96
S1_ORACLE_MATERIAL_ROW_COUNT = 2_093
S1_RENDERED_LOGICAL_MATRIX_SHA256 = (
    "b5e59406b457ac7facf5fe08bbcb5cb456efbbadbbfd2fc12a435b29ae69cdc0"
)
LEGACY_UPGRADE_COVERAGE = "s1-native-structural-legacy-upgrades"
LEGACY_UPGRADE_INPUT_SIZE_BYTES = 22_189
LEGACY_UPGRADE_INPUT_SHA256 = (
    "6a578463bbacb8267e7bff82bf76708d2b2950a7e9b052b53131626a331245de"
)
LEGACY_UPGRADE_ORACLE_SIZE_BYTES = 6_155
LEGACY_UPGRADE_ORACLE_SHA256 = (
    "cf0d86c440d1f89fc13f2b131f4f1534fb42363ebdc92580af826058297eb3d0"
)
LEGACY_UPGRADE_SCHEMA9_CASES_SIZE_BYTES = 3_314_082
LEGACY_UPGRADE_SCHEMA9_GALLERY_SIZE_BYTES = 49_679
LEGACY_UPGRADE_SCHEMA9_GALLERY_SHA256 = (
    "21ceec072cc3263a41bdb81874e897d48d5a1ce5e1c7d3ac3c0de3063818ee6c"
)
LEGACY_UPGRADE_SELECTORS = (
    ("ae2-m1-02", (216, 100, 226)),
    ("ae2-m1-03", (222, 100, 226)),
    ("ae2-m2-06", (210, 100, 248)),
    ("ae2-m2-07", (213, 100, 248)),
    ("ae2-m2-09", (219, 100, 248)),
    ("ae2-m2-10", (222, 100, 248)),
    ("ae2-m2-11", (225, 100, 248)),
    ("ae2-m2-12", (228, 100, 248)),
    ("ae2-m2-13", (231, 100, 248)),
    ("ae2-m2-14", (234, 100, 248)),
)
S1_PROFILE_SIZE_BYTES = 117_013
S1_PROFILE_SHA256 = (
    "f6fa515b4e17205a019d57f253d5e71017ea20e75b8f0c333aa587afd0d0f353"
)
S1_SUPPORT_MATRIX_SIZE_BYTES = 24_084
S1_SUPPORT_MATRIX_SHA256 = (
    "d77a589dd162e4f7d37113dc40dff3eca69e6042291b53133eabe9549dae643a"
)
S1_PROVENANCE_SIZE_BYTES = 112_852
S1_PROVENANCE_SHA256 = (
    "5cea0a302297a00e4fe0bf246695c6e79f646356c87a44d396ceb365d9a249c5"
)
CURRENT_SUPPORT_MATRIX_SIZE_BYTES = 35_282
CURRENT_SUPPORT_MATRIX_SHA256 = (
    "b28808e2e55a94d214301f2a80d0eaaa5c201d4b1045c35435d95dc312eaf38f"
)
CURRENT_PROVENANCE_SIZE_BYTES = 129_805
CURRENT_PROVENANCE_SHA256 = (
    "f8b959965733cd4803c2ea9bf649177aa0f71410ba60383d210389ee37d87516"
)
CURRENT_ACCEPTED_S1_SUPPORT_PROJECTION_SHA256 = (
    "8d594a89eb71dfb4905b2e2799554b65e4787b175d0323e984e5f6053b8efc94"
)
CURRENT_ACCEPTED_S1_PROVENANCE_PROJECTION_SHA256 = (
    "481d651ff3615d9e005bf4c7f02dd733cbf0753a954327fcffef7468d23064b4"
)
CURRENT_ACCEPTED_S1_PROFILE_IDS = (
    "ae2-quartz-glass",
    "ae2-crafting",
    "ae2-quantum-bridge",
    "ae2-m3-completion",
    "ae2-cable-bus-structural",
)
S1_SCHEMA9_DISABLED_EXPECTATIONS = {
    (257, 100, 313): ("custom-m2", 116),
    (233, 100, 318): ("custom-m2", 68),
    (209, 100, 323): ("custom-m2", 68),
    (296, 100, 323): ("custom-m2", 68),
    (272, 100, 328): ("custom-m2", 68),
    (248, 100, 333): ("custom-m2", 68),
    (236, 100, 343): ("custom-m1", 12),
    (263, 100, 343): ("custom-m1", 12),
    (272, 100, 353): ("custom-m2", 116),
    (281, 100, 358): ("custom-m1", 12),
}
S1_SCHEMA9_DISABLED_RENDERED_ANCHOR_COUNT = 10
S1_SCHEMA9_DISABLED_EMPTY_ANCHOR_COUNT = 350
S1_SCHEMA9_DISABLED_TRIANGLE_COUNT = 608
S1_SCHEMA9_DISABLED_RESOURCE_COUNT = 14
CRANK_ORIENTATION_ANGLES = {
    "down": [90, 0, 0],
    "up": [270, 0, 180],
    "north": [0, 0, 0],
    "south": [0, 180, 0],
    "west": [0, 270, 0],
    "east": [0, 90, 0],
}
CHEST_Y_ROTATION = {"south": 0, "west": 90, "north": 180, "east": 270}

OBSOLETE_OUTPUTS = tuple(
    Path(f"datapack/data/{namespace}/function") / name
    for namespace, names in (
        (
            "ae2_m0",
            (
                "build.mcfunction",
                "clear.mcfunction",
                "load.mcfunction",
                "pose_south.mcfunction",
                "release.mcfunction",
                "verify.mcfunction",
            ),
        ),
        (
            "ae2_m1",
            (
                "build.mcfunction",
                "clear.mcfunction",
                "load.mcfunction",
                "pose_south.mcfunction",
                "release.mcfunction",
                "verify.mcfunction",
                "dense/build.mcfunction",
                "dense/clear.mcfunction",
                "dense/verify.mcfunction",
                "dense/release.mcfunction",
                "dense/batch_1.mcfunction",
                "dense/batch_2.mcfunction",
                "dense/batch_3.mcfunction",
                "dense/batch_4.mcfunction",
            ),
        ),
        (
            "ae2_m2",
            (
                "build.mcfunction",
                "clear.mcfunction",
                "load.mcfunction",
                "pose_south.mcfunction",
                "release.mcfunction",
                "verify.mcfunction",
                "dense/build.mcfunction",
                "dense/clear.mcfunction",
                "dense/verify.mcfunction",
                "dense/release.mcfunction",
                "dense/batch_1.mcfunction",
                "dense/batch_2.mcfunction",
                "dense/batch_3.mcfunction",
                "dense/batch_4.mcfunction",
            ),
        ),
    )
    for name in names
)
OBSOLETE_DIRECTORIES = (
    Path("datapack/data/ae2_m2/function/dense"),
    Path("datapack/data/ae2_m2/function"),
    Path("datapack/data/ae2_m2"),
    Path("datapack/data/ae2_m1/function/dense"),
    Path("datapack/data/ae2_m1/function"),
    Path("datapack/data/ae2_m1"),
    Path("datapack/data/ae2_m0/function"),
    Path("datapack/data/ae2_m0"),
)


def cable_id(family_key: str, color_prefix: str) -> str:
    return f"ae2:{color_prefix}_{FAMILY_BY_KEY[family_key]['id_suffix']}"


def decode_cable_id(value: str) -> tuple[dict[str, str], str, str]:
    for family in FAMILIES:
        suffix = "_" + family["id_suffix"]
        if value.startswith("ae2:") and value.endswith(suffix):
            color_prefix = value[len("ae2:") : -len(suffix)]
            for registry_prefix, texture_name in COLORS:
                if registry_prefix == color_prefix:
                    return family, registry_prefix, texture_name
    raise ValueError(f"unknown exact AE2 19.2.17 cable ID: {value}")


def colors_connect(first: str, second: str) -> bool:
    return first == "fluix" or second == "fluix" or first == second


def minimum_family(first_key: str, second_key: str) -> str:
    first = FAMILY_BY_KEY[first_key]
    second = FAMILY_BY_KEY[second_key]
    variant = min((first["variant"], second["variant"]), key=VARIANT_RANK.get)
    size = min((first["size"], second["size"]), key=SIZE_RANK.get)
    for family in FAMILIES:
        if family["variant"] == variant and family["size"] == size:
            return family["key"]
    raise ValueError(f"AE2 cable minimum has no family: {variant}/{size}")


def visible_family(local_key: str, effective_key: str) -> str:
    if local_key == "glass":
        return "glass"
    if local_key == "covered":
        return "covered"
    if local_key == "smart":
        return "smart" if effective_key == "smart" else "covered"
    if local_key == "dense_covered":
        return "dense_covered" if effective_key == "dense_covered" else "covered"
    if effective_key in ("glass", "covered"):
        return "covered"
    return effective_key


def within(
    position: tuple[int, int, int], bounds: tuple[tuple[int, ...], tuple[int, ...]]
) -> bool:
    return all(
        bounds[0][index] <= value <= bounds[1][index]
        for index, value in enumerate(position)
    )


def overlaps(
    first: tuple[tuple[int, ...], tuple[int, ...]],
    second: tuple[tuple[int, ...], tuple[int, ...]],
) -> bool:
    return all(
        first[0][index] <= second[1][index]
        and second[0][index] <= first[1][index]
        for index in range(3)
    )


def chunks_for_bounds(
    bounds: tuple[tuple[int, ...], tuple[int, ...]]
) -> set[tuple[int, int]]:
    return {
        (chunk_x, chunk_z)
        for chunk_x in range(bounds[0][0] // 16, bounds[1][0] // 16 + 1)
        for chunk_z in range(bounds[0][2] // 16, bounds[1][2] // 16 + 1)
    }


def dense_exclusive_chunks() -> tuple[tuple[int, int], ...]:
    main_chunks = set().union(
        *(
            chunks_for_bounds(bounds)
            for bounds in (
                FIXTURE_BOUNDS,
                M2_FIXTURE_BOUNDS,
                M3_FIXTURE_BOUNDS,
                M3B_FIXTURE_BOUNDS,
                M3C_FIXTURE_BOUNDS,
                M3D_FIXTURE_BOUNDS,
                M3E_FIXTURE_BOUNDS,
                M3F_FIXTURE_BOUNDS,
                S1_FIXTURE_BOUNDS,
                DECK_BOUNDS,
                SENTINEL_BOUNDS,
            )
        )
    )
    dense_chunks = set().union(
        *(chunks_for_bounds(bounds) for bounds in DENSE_OWNED_BOUNDS)
    )
    return tuple(sorted(dense_chunks - main_chunks))


def cable_anchor(
    position: tuple[int, int, int],
    value: str | None,
    *,
    expected_path: str = "custom-m1",
    ambiguous_neighbor: dict[str, str] | None = None,
    face_parts: dict[str, dict[str, Any]] | None = None,
    facades: dict[str, dict[str, Any]] | None = None,
    fallback_reason: str | None = None,
) -> dict[str, Any]:
    result: dict[str, Any] = {
        "position": position,
        "block_id": "ae2:cable_bus",
        "cable_id": value,
        "expected_path": expected_path,
        "face_parts": dict(face_parts or {}),
        "facades": dict(facades or {}),
    }
    if ambiguous_neighbor is not None:
        result["ambiguous_neighbor"] = ambiguous_neighbor
        result["expected_triangle_count"] = 0
    if fallback_reason is not None:
        result["fallback_reason"] = fallback_reason
        result["expected_triangle_count"] = 0
    return result


def terminal(spin: int | None, *, part_id: str = TERMINAL_PART_ID) -> dict[str, Any]:
    result: dict[str, Any] = {"id": part_id}
    if spin is not None:
        result["spin"] = spin
    return result


def native_structural_part(
    part_name: str,
    *,
    spin: int | None = None,
    frequency: int | None = None,
) -> dict[str, Any]:
    known_names = {name for name, _group in NATIVE_STRUCTURAL_PARTS}
    if part_name not in known_names:
        raise ValueError(f"unknown native structural part: {part_name}")
    if spin is not None:
        if part_name not in NATIVE_STRUCTURAL_SPIN_PARTS or spin not in range(4):
            raise ValueError(f"invalid persisted spin for {part_name}: {spin}")
    if frequency is not None:
        if part_name not in NATIVE_STRUCTURAL_P2P_PARTS or not 0 <= frequency <= 0xFFFF:
            raise ValueError(f"invalid unsigned P2P frequency for {part_name}: {frequency}")
    result: dict[str, Any] = {"id": f"ae2:{part_name}"}
    if spin is not None:
        result["spin"] = spin
    if frequency is not None:
        result["freq"] = frequency
    return result


def facade(
    block_id: str,
    properties: dict[str, str] | None = None,
) -> dict[str, Any]:
    result: dict[str, Any] = {"Name": block_id}
    if properties is not None:
        result["Properties"] = dict(properties)
    return result


def drive_item(
    item_id: str,
    *,
    count: int = 1,
    components: dict[str, Any] | None = None,
) -> dict[str, Any]:
    result: dict[str, Any] = {"id": item_id, "count": count}
    if components is not None:
        result["components"] = components
    return result


def drive_anchor(
    position: tuple[int, int, int],
    facing: str,
    spin: int,
    slot_items: dict[int, dict[str, Any]],
    *,
    expected_path: str = "custom-m3",
    fallback_reason: str | None = None,
) -> dict[str, Any]:
    inventory: list[dict[str, Any] | None] = [None] * DRIVE_SLOT_COUNT
    for slot, item in slot_items.items():
        if slot < 0 or slot >= DRIVE_SLOT_COUNT:
            raise ValueError(f"drive slot outside 0..9: {slot}")
        if inventory[slot] is not None:
            raise ValueError(f"duplicate drive slot: {slot}")
        inventory[slot] = item
    result: dict[str, Any] = {
        "position": position,
        "block_id": DRIVE_BLOCK_ID,
        "cable_id": None,
        "expected_path": expected_path,
        "block_state": {"facing": facing, "spin": spin},
        "drive_inventory": tuple(inventory),
    }
    if fallback_reason is not None:
        result["fallback_reason"] = fallback_reason
        result["expected_triangle_count"] = 0
    return result


def drive_slot_origin_sixteenths(slot: int) -> tuple[int, int, int]:
    row, column = divmod(slot, 2)
    return (9 - column * 8, 13 - row * 3, 1)


def extended_drive_anchor(
    position: tuple[int, int, int],
    facing: str,
    spin: int,
    slot_items: dict[int, dict[str, Any]],
    *,
    expected_path: str = "custom-m3b",
    fallback_reason: str | None = None,
) -> dict[str, Any]:
    inventory: list[dict[str, Any] | None] = [None] * EXTENDED_DRIVE_SLOT_COUNT
    for slot, item in slot_items.items():
        if slot < 0 or slot >= EXTENDED_DRIVE_SLOT_COUNT:
            raise ValueError(f"extended drive slot outside 0..19: {slot}")
        if inventory[slot] is not None:
            raise ValueError(f"duplicate extended drive slot: {slot}")
        inventory[slot] = item
    result: dict[str, Any] = {
        "position": position,
        "block_id": EXTENDED_DRIVE_BLOCK_ID,
        "cable_id": None,
        "expected_path": expected_path,
        "block_state": {"facing": facing, "spin": spin},
        "drive_inventory": tuple(inventory),
    }
    if fallback_reason is not None:
        result["fallback_reason"] = fallback_reason
        result["expected_triangle_count"] = 0
    return result


def connected_glass_anchor(
    position: tuple[int, int, int],
    block_id: str,
) -> dict[str, Any]:
    if block_id not in CONNECTED_GLASS_BLOCK_IDS:
        raise ValueError(f"unsupported connected-glass block: {block_id}")
    return {
        "position": position,
        "block_id": block_id,
        "cable_id": None,
        "expected_path": "custom-m3c",
    }


def crafting_anchor(
    position: tuple[int, int, int],
    block_id: str,
    *,
    powered: bool = False,
    facing: str | None = None,
    spin: int | None = None,
    painted_color_ordinal: int | None = None,
    expected_path: str = "custom-m3d",
    fallback_reason: str | None = None,
) -> dict[str, Any]:
    kind = CRAFTING_BLOCK_KINDS.get(block_id)
    if kind is None:
        raise ValueError(f"unsupported native crafting block: {block_id}")
    if kind == "monitor":
        if facing not in DIRECTION_DELTAS or spin not in range(4):
            raise ValueError("crafting monitor requires exact facing and spin 0..3")
        if painted_color_ordinal not in CRAFTING_PAINT_BY_ORDINAL:
            raise ValueError("crafting monitor requires painted color ordinal 0..16")
    elif any(value is not None for value in (facing, spin, painted_color_ordinal)):
        raise ValueError("only the crafting monitor has orientation/paint metadata")
    state: dict[str, Any] = {"formed": True, "powered": powered}
    if kind == "monitor":
        state.update({"facing": facing, "spin": spin})
    result: dict[str, Any] = {
        "position": position,
        "block_id": block_id,
        "cable_id": None,
        "expected_path": expected_path,
        "crafting_kind": kind,
        "block_state": state,
    }
    if kind == "monitor":
        result["painted_color_ordinal"] = painted_color_ordinal
        result["monitor_display_policy"] = CRAFTING_MONITOR_DISPLAY_POLICY
    if fallback_reason is not None:
        result["fallback_reason"] = fallback_reason
        result["expected_triangle_count"] = 0
    return result


def quantum_anchor(
    position: tuple[int, int, int],
    plane: str,
    role: str,
) -> dict[str, Any]:
    if plane not in QUANTUM_PLANES or role not in {"link", "corner", "edge"}:
        raise ValueError(f"invalid quantum bridge anchor: plane={plane}, role={role}")
    return {
        "position": position,
        "block_id": QUANTUM_LINK_BLOCK_ID if role == "link" else QUANTUM_RING_BLOCK_ID,
        "cable_id": None,
        "expected_path": "custom-m3e",
        "block_state": {"formed": True, "waterlogged": False},
        "expected_block_entity_id": QUANTUM_BLOCK_ENTITY_ID,
        "quantum_plane": plane,
        "quantum_role": role,
        "formation_policy": "real-complete-bridge-no-forced-state",
        "network_condition": "settled-unpowered",
        "power_overlay_policy": QUANTUM_STATIC_POLICY,
        "particle_policy": QUANTUM_PARTICLE_POLICY,
    }


def paint_splotch(
    signed_position: int,
    backing_side: str,
    color_ordinal: int,
) -> dict[str, Any]:
    if signed_position < -128 or signed_position > 127:
        raise ValueError("paint position must fit one signed byte")
    if backing_side not in PAINT_DIRECTION_ORDINALS:
        raise ValueError("paint backing side is not cardinal")
    if color_ordinal < 0 or color_ordinal >= len(PAINT_COLOR_NAMES):
        raise ValueError("paint color ordinal is outside the exact palette")
    encoded = PAINT_DIRECTION_ORDINALS.index(backing_side) | color_ordinal << 3
    seed = abs(signed_position + encoded)
    return {
        "signed_position": signed_position,
        "backing_side": backing_side,
        "visible_face": OPPOSITES[backing_side],
        "color_ordinal": color_ordinal,
        "color_name": PAINT_COLOR_NAMES[color_ordinal],
        "lumen": False,
        "encoded_unsigned": encoded,
        "texture_index": seed % 3,
        "resource": PAINT_TEXTURES[seed % 3],
        "rgb_u8": list(COLOR_MEDIUM_TINTS[PAINT_COLOR_NAMES[color_ordinal]]),
    }


def paint_dots_bytes(splotches: Iterable[dict[str, Any]]) -> tuple[int, ...]:
    records = tuple(splotches)
    if not 1 <= len(records) <= 21:
        raise ValueError("paint must contain between one and 21 splotches")
    output = [len(records)]
    for record in records:
        output.extend((record["signed_position"], record["encoded_unsigned"]))
    output.extend(0 for _ in range(256 - len(output)))
    if len(output) != 256:
        raise ValueError("paint durable dots array must contain exactly 256 bytes")
    return tuple(output)


def paint_anchor(
    position: tuple[int, int, int],
    splotches: Iterable[dict[str, Any]],
) -> dict[str, Any]:
    exact = tuple(splotches)
    return {
        "position": position,
        "block_id": PAINT_BLOCK_ID,
        "cable_id": None,
        "expected_path": "custom-m3f",
        "block_state": {"facing": exact[0]["visible_face"], "light_level": 0},
        "expected_block_entity_id": M3_COMPLETION_BLOCK_ENTITY_IDS[PAINT_BLOCK_ID],
        "paint_splotches": exact,
        "paint_dots": paint_dots_bytes(exact),
        "static_policy": PAINT_STATIC_POLICY,
    }


def m3_completion_machine_anchor(
    position: tuple[int, int, int],
    block_id: str,
    block_state: dict[str, Any],
    *,
    facing: str | None = None,
    spin: int | None = None,
) -> dict[str, Any]:
    policy = {
        SKY_STONE_CHEST_BLOCK_ID: CHEST_STATIC_POLICY,
        SMOOTH_SKY_STONE_CHEST_BLOCK_ID: CHEST_STATIC_POLICY,
        CRANK_BLOCK_ID: CRANK_STATIC_POLICY,
        INSCRIBER_BLOCK_ID: INSCRIBER_STATIC_POLICY,
    }.get(block_id)
    if policy is None:
        raise ValueError(f"unsupported M3f machine anchor: {block_id}")
    anchor: dict[str, Any] = {
        "position": position,
        "block_id": block_id,
        "cable_id": None,
        "expected_path": "custom-m3f",
        "block_state": block_state,
        "expected_block_entity_id": M3_COMPLETION_BLOCK_ENTITY_IDS[block_id],
        "static_policy": policy,
    }
    if facing is not None:
        anchor["facing"] = facing
    if spin is not None:
        anchor["spin"] = spin
    return anchor


def spatial_pylon_anchor(
    position: tuple[int, int, int],
    *,
    axis: str | None,
    axis_position: str | None,
    expected_path: str = "custom-m3f",
    fallback_reason: str | None = None,
) -> dict[str, Any]:
    anchor: dict[str, Any] = {
        "position": position,
        "block_id": SPATIAL_PYLON_BLOCK_ID,
        "cable_id": None,
        "expected_path": expected_path,
        "block_state": {"powered_on": False},
        "expected_block_entity_id": M3_COMPLETION_BLOCK_ENTITY_IDS[
            SPATIAL_PYLON_BLOCK_ID
        ],
        "pylon_axis": axis,
        "pylon_axis_position": axis_position,
        "static_policy": SPATIAL_PYLON_STATIC_POLICY,
    }
    if fallback_reason is not None:
        anchor["fallback_reason"] = fallback_reason
        anchor["expected_triangle_count"] = 0
    return anchor


def _quantum_branch_bounds(
    direction: str,
    cross_min: float,
    cross_max: float,
    negative_end: float,
    positive_start: float,
) -> list[float]:
    axis = {"west": 0, "east": 0, "down": 1, "up": 1, "north": 2, "south": 2}[direction]
    bounds = [cross_min, cross_min, cross_min, cross_max, cross_max, cross_max]
    if direction in {"west", "down", "north"}:
        bounds[axis] = 0
        bounds[axis + 3] = negative_end
    else:
        bounds[axis] = positive_start
        bounds[axis + 3] = 16
    return bounds


def _quantum_primitive(
    role: str,
    resource: str,
    bounds: list[float],
) -> dict[str, Any]:
    return {
        "role": role,
        "resource": resource,
        "bounds_sixteenths": bounds,
        "rgb_u8": [255, 255, 255],
        "ambient_occlusion_raw_u8": 255,
        "light_policy": "world-derived-face-light",
        "triangle_count": 12,
    }


def expected_quantum_geometry(
    anchor: dict[str, Any],
    quantum_positions: set[tuple[int, int, int]],
) -> tuple[list[dict[str, Any]], dict[str, int], list[dict[str, str]]]:
    position = anchor["position"]
    connected = [
        direction
        for direction, delta in DIRECTION_DELTAS.items()
        if tuple(position[index] + delta[index] for index in range(3))
        in quantum_positions
    ]
    connections = [
        {"direction": direction, "kind": "quantum-bridge"}
        for direction in connected
    ]
    role = anchor["quantum_role"]
    primitives: list[dict[str, Any]] = []
    if role == "link":
        if len(connected) != 4:
            raise ValueError(f"quantum link {position} must have four direct neighbors")
        primitives.append(
            _quantum_primitive(
                "link-center", QUANTUM_LINK_RESOURCE, [2, 2, 2, 14, 14, 14]
            )
        )
        for direction in connected:
            primitives.append(
                _quantum_primitive(
                    f"link-glass:{direction}",
                    QUANTUM_GLASS_RESOURCE,
                    _quantum_branch_bounds(direction, 6.24, 9.76, 3.984, 12.016),
                )
            )
            primitives.append(
                _quantum_primitive(
                    f"link-covered:{direction}",
                    QUANTUM_COVERED_RESOURCE,
                    _quantum_branch_bounds(direction, 4.992, 11.008, 1.992, 14.008),
                )
            )
    elif role == "corner":
        if len(connected) != 2:
            raise ValueError(f"quantum corner {position} must have two direct neighbors")
        primitives.append(
            _quantum_primitive(
                "ring-corner-center", QUANTUM_RING_RESOURCE, [2, 2, 2, 14, 14, 14]
            )
        )
        for direction in connected:
            primitives.append(
                _quantum_primitive(
                    f"ring-corner-covered:{direction}",
                    QUANTUM_COVERED_RESOURCE,
                    _quantum_branch_bounds(direction, 4.992, 11.008, 4.192, 11.808),
                )
            )
    else:
        if len(connected) != 3:
            raise ValueError(f"quantum edge {position} must have three direct neighbors")
        for axis, bounds in (
            ("x", [0, 2, 2, 16, 14, 14]),
            ("y", [2, 0, 2, 14, 16, 14]),
            ("z", [2, 2, 0, 14, 14, 16]),
        ):
            primitives.append(
                _quantum_primitive(
                    f"ring-edge-axis:{axis}", QUANTUM_RING_RESOURCE, bounds
                )
            )
    materials: Counter[str] = Counter()
    for primitive in primitives:
        materials[primitive["resource"]] += primitive["triangle_count"]
    return primitives, dict(sorted(materials.items())), connections


def _crafting_bounds_for_direction(
    direction: str,
    low: float,
    high: float,
    bounds: list[float],
) -> None:
    axis = {"x": 0, "y": 1, "z": 2}[CRAFTING_DIRECTION_AXES[direction]]
    if direction in {"down", "north", "west"}:
        bounds[axis] = low
    else:
        bounds[axis + 3] = high


def _crafting_primitive(
    role: str,
    resource: str,
    bounds: list[float],
    *,
    rgb: tuple[int, int, int] = (255, 255, 255),
    emissive: bool = False,
) -> dict[str, Any]:
    return {
        "role": role,
        "resource": resource,
        "bounds_sixteenths": bounds,
        "rgb_u8": list(rgb),
        "ambient_occlusion_raw_u8": 255,
        "light_policy": "fullbright-15" if emissive else "world-derived-face-light",
        "triangle_count": 2,
    }


def expected_crafting_geometry(
    anchor: dict[str, Any],
    crafting_positions: set[tuple[int, int, int]],
) -> tuple[list[dict[str, Any]], dict[str, int], list[dict[str, str]]]:
    position = anchor["position"]
    connected = {
        direction
        for direction, delta in DIRECTION_DELTAS.items()
        if tuple(position[index] + delta[index] for index in range(3))
        in crafting_positions
    }
    connections = [
        {"direction": direction, "kind": "crafting-unit"}
        for direction in DIRECTION_DELTAS
        if direction in connected
    ]
    kind = anchor["crafting_kind"]
    powered = anchor["block_state"]["powered"]
    face_contracts: list[dict[str, Any]] = []
    materials: Counter[str] = Counter()
    for side in DIRECTION_DELTAS:
        if side in connected:
            continue
        primitives: list[dict[str, Any]] = []
        for corner in CRAFTING_CORNER_DIRECTIONS:
            if side not in corner or any(direction in connected for direction in corner):
                continue
            bounds = [
                0 if "west" in corner else 13,
                0 if "down" in corner else 13,
                0 if "north" in corner else 13,
                3 if "west" in corner else 16,
                3 if "down" in corner else 16,
                3 if "north" in corner else 16,
            ]
            primitives.append(
                _crafting_primitive(
                    "ring-corner:" + ",".join(corner),
                    "ae2:block/crafting/ring_corner",
                    bounds,
                )
            )
        for stripe in DIRECTION_DELTAS:
            if (
                stripe == side
                or stripe == OPPOSITES[side]
                or stripe in connected
            ):
                continue
            bounds: list[float] = [0, 0, 0, 16, 16, 16]
            axis_index = {"x": 0, "y": 1, "z": 2}[
                CRAFTING_DIRECTION_AXES[stripe]
            ]
            if stripe in {"down", "north", "west"}:
                bounds[axis_index + 3] = 3
            else:
                bounds[axis_index] = 13
            third_axis = (
                {"x", "y", "z"}
                - {
                    CRAFTING_DIRECTION_AXES[side],
                    CRAFTING_DIRECTION_AXES[stripe],
                }
            ).pop()
            negative, positive = {
                "x": ("west", "east"),
                "y": ("down", "up"),
                "z": ("north", "south"),
            }[third_axis]
            third_index = {"x": 0, "y": 1, "z": 2}[third_axis]
            if negative not in connected:
                bounds[third_index] = 3
            if positive not in connected:
                bounds[third_index + 3] = 13
            vertical = (
                CRAFTING_DIRECTION_AXES[side] != "y"
                and CRAFTING_DIRECTION_AXES[stripe] in {"x", "z"}
            ) or (
                CRAFTING_DIRECTION_AXES[side] == "y"
                and CRAFTING_DIRECTION_AXES[stripe] == "x"
            )
            primitives.append(
                _crafting_primitive(
                    f"ring-stripe:{stripe}",
                    "ae2:block/crafting/ring_side_ver"
                    if vertical
                    else "ae2:block/crafting/ring_side_hor",
                    bounds,
                )
            )

        inner: list[float] = [
            0 if "west" in connected else 2.99,
            0 if "down" in connected else 2.99,
            0 if "north" in connected else 2.99,
            16 if "east" in connected else 13.01,
            16 if "up" in connected else 13.01,
            16 if "south" in connected else 13.01,
        ]
        side_axis = {"x": 0, "y": 1, "z": 2}[CRAFTING_DIRECTION_AXES[side]]
        inner[side_axis] = 0
        inner[side_axis + 3] = 16
        if kind == "unit":
            layers = (("unit-base", "ae2:block/crafting/unit_base", (255, 255, 255), False),)
        elif kind == "monitor" and side != anchor["block_state"]["facing"]:
            layers = (("monitor-chassis", "ae2:block/crafting/unit_base", (255, 255, 255), False),)
        elif kind == "monitor":
            color = CRAFTING_PAINT_BY_ORDINAL[anchor["painted_color_ordinal"]]
            layers = (
                ("monitor-base", "ae2:block/crafting/monitor_base", (255, 255, 255), False),
                ("monitor-bright", "ae2:block/crafting/monitor_light_bright", tuple(color["bright_rgb_u8"]), powered),
                ("monitor-medium", "ae2:block/crafting/monitor_light_medium", tuple(color["medium_rgb_u8"]), powered),
                ("monitor-dark", "ae2:block/crafting/monitor_light_dark", tuple(color["dark_rgb_u8"]), powered),
            )
        else:
            layers = (
                ("light-base", "ae2:block/crafting/light_base", (255, 255, 255), False),
                ("light-overlay", CRAFTING_LIGHT_RESOURCE_BY_KIND[kind], (255, 255, 255), powered),
            )
        primitives.extend(
            _crafting_primitive(role, resource, list(inner), rgb=rgb, emissive=emissive)
            for role, resource, rgb, emissive in layers
        )
        for primitive in primitives:
            materials[primitive["resource"]] += primitive["triangle_count"]
        face_contracts.append(
            {
                "direction": side,
                "primitives": primitives,
                "triangle_count": sum(
                    primitive["triangle_count"] for primitive in primitives
                ),
            }
        )
    return face_contracts, dict(sorted(materials.items())), connections


def _signed(value: int, bits: int) -> int:
    mask = (1 << bits) - 1
    value &= mask
    sign = 1 << (bits - 1)
    return value - (1 << bits) if value & sign else value


def connected_glass_base_selection(
    position: tuple[int, int, int],
) -> dict[str, Any]:
    """Reproduce the exact 1.21.1 position seed and LegacyRandomSource draws."""
    x, y, z = position
    value = _signed(
        _signed(x * 3_129_871, 32) ^ _signed(z * 116_129_781, 64) ^ y,
        64,
    )
    value = _signed(
        _signed(_signed(value * value, 64) * 42_317_861, 64)
        + _signed(value * 11, 64),
        64,
    )
    seed = value >> 16
    random_mask = (1 << 48) - 1
    state = (seed ^ 0x5DEECE66D) & random_mask

    def next_int_4() -> int:
        nonlocal state
        state = (state * 0x5DEECE66D + 0xB) & random_mask
        next_31 = state >> 17
        return (next_31 * 4) >> 31

    random_offset = next_int_4()
    v_raw = next_int_4()
    texture_index = (random_offset + next_int_4()) % 4
    divisor = 32 if texture_index < 2 else 16
    u = random_offset / divisor
    v = v_raw / divisor
    return {
        "position_seed_i64": seed,
        "draws": {
            "random_offset": random_offset,
            "v_raw": v_raw,
            "texture_index": texture_index,
        },
        "resource_path": CONNECTED_GLASS_BASE_RESOURCES[texture_index],
        "u_offset": u,
        "v_offset": v,
        "uv_corners": [
            [0.0, 0.0],
            [0.0, 1 - v],
            [1 - u, 1 - v],
            [1 - u, 0.0],
        ],
    }


def expected_connected_glass_geometry(
    anchor: dict[str, Any],
    glass_positions: set[tuple[int, int, int]],
    opaque_positions: set[tuple[int, int, int]],
) -> tuple[list[dict[str, Any]], dict[str, int], list[str], list[str]]:
    position = anchor["position"]
    selection = connected_glass_base_selection(position)
    materials: Counter[str] = Counter()
    faces: list[dict[str, Any]] = []
    connected_faces: list[str] = []
    opaque_culled_faces: list[str] = []
    for direction, delta in DIRECTION_DELTAS.items():
        adjacent = tuple(position[index] + delta[index] for index in range(3))
        if adjacent in glass_positions:
            connected_faces.append(direction)
            continue
        if adjacent in opaque_positions:
            opaque_culled_faces.append(direction)
            continue
        bit_directions = CONNECTED_GLASS_FACE_BIT_DIRECTIONS[direction]
        frame_mask_value = sum(
            (
                0
                if tuple(
                    position[axis] + DIRECTION_DELTAS[bit_direction][axis]
                    for axis in range(3)
                )
                in glass_positions
                else 1
            )
            << bit_index
            for bit_index, bit_direction in enumerate(bit_directions)
        )
        frame_mask = f"{frame_mask_value:04b}"
        base_resource = selection["resource_path"]
        materials[base_resource] += 2
        frame_resource = None
        if frame_mask != "0000":
            frame_resource = f"ae2:block/glass/quartz_glass_frame{frame_mask}"
            materials[frame_resource] += 2
        faces.append(
            {
                "direction": direction,
                "local_bit_directions": list(bit_directions),
                "frame_mask": frame_mask,
                "base_resource": base_resource,
                "base_uv_corners": selection["uv_corners"],
                "frame_resource": frame_resource,
                "frame_uv_corners": (
                    None
                    if frame_resource is None
                    else [[0.0, 0.0], [0.0, 1.0], [1.0, 1.0], [1.0, 0.0]]
                ),
                "attributes": {
                    "rgb_u8": [255, 255, 255],
                    "ambient_occlusion_raw_u8": 255,
                    "light_policy": "world-derived-with-vibrant-center-emission-floor-15",
                },
                "triangle_count": 2 + (2 if frame_resource is not None else 0),
            }
        )
    anchor["expected_glass_base_selection"] = selection
    return (
        faces,
        dict(sorted(materials.items())),
        connected_faces,
        opaque_culled_faces,
    )


def drive_contract(anchor: dict[str, Any]) -> dict[str, Any]:
    if anchor["block_id"] == DRIVE_BLOCK_ID:
        return {
            "slot_count": DRIVE_SLOT_COUNT,
            "face_slot_count": DRIVE_SLOT_COUNT,
            "base_model": DRIVE_BASE_MODEL,
            "empty_cell_model": DRIVE_EMPTY_CELL_MODEL,
            "base_triangles": DRIVE_BASE_TRIANGLE_COUNT,
            "base_materials": DRIVE_BASE_MATERIAL_TRIANGLES,
            "cell_models": DRIVE_CELL_MODELS,
            "model_materials": {
                model_id: DRIVE_CELL_TEXTURE
                for model_id in DRIVE_CELL_MODELS.values()
            },
            "triangle_formula": "90+16N",
        }
    if anchor["block_id"] == EXTENDED_DRIVE_BLOCK_ID:
        return {
            "slot_count": EXTENDED_DRIVE_SLOT_COUNT,
            "face_slot_count": EXTENDED_DRIVE_FACE_SLOT_COUNT,
            "base_model": EXTENDED_DRIVE_BASE_MODEL,
            "empty_cell_model": EXTENDED_DRIVE_EMPTY_CELL_MODEL,
            "base_triangles": EXTENDED_DRIVE_BASE_TRIANGLE_COUNT,
            "base_materials": EXTENDED_DRIVE_BASE_MATERIAL_TRIANGLES,
            "cell_models": EXTENDED_DRIVE_CELL_MODELS,
            "model_materials": EXTENDED_DRIVE_MODEL_MATERIALS,
            "triangle_formula": "116+16N",
        }
    raise ValueError(f"not a supported drive block: {anchor['block_id']}")


def native_structural_plane_local_bounds_sixteenths(
    mask: int,
    bound_bits: tuple[int, int, int, int],
    z_bounds: tuple[int, int],
) -> list[list[int]]:
    min_x_bit, max_x_bit, min_y_bit, max_y_bit = bound_bits
    return [
        [
            0 if mask & min_x_bit else 1,
            0 if mask & min_y_bit else 1,
            z_bounds[0],
        ],
        [
            16 if mask & max_x_bit else 15,
            16 if mask & max_y_bit else 15,
            z_bounds[1],
        ],
    ]


def native_structural_plane_world_bounds_sixteenths(
    local_bounds: list[list[int]],
    local_axes: tuple[str, str, str],
) -> list[list[int]]:
    world_min = [0, 0, 0]
    world_max = [0, 0, 0]
    for local_axis, direction in enumerate(local_axes):
        vector = DIRECTION_DELTAS[direction]
        world_axis = next(
            axis for axis, component in enumerate(vector) if component
        )
        if vector[world_axis] > 0:
            world_min[world_axis] = local_bounds[0][local_axis]
            world_max[world_axis] = local_bounds[1][local_axis]
        else:
            world_min[world_axis] = 16 - local_bounds[1][local_axis]
            world_max[world_axis] = 16 - local_bounds[0][local_axis]
    return [world_min, world_max]


def create_s1_cases() -> list[dict[str, Any]]:
    """Build the bounded representative S1 matrix; Cartesian closure stays Java."""
    cases: list[dict[str, Any]] = []
    next_anchor = 0

    def position() -> tuple[int, int, int]:
        nonlocal next_anchor
        if next_anchor >= EXPECTED_S1_ANCHOR_COUNT:
            raise ValueError("S1 anchor allocator exceeded its fixed matrix")
        column = next_anchor % 37
        row = next_anchor // 37
        next_anchor += 1
        return (209 + column * 3, 100, 313 + row * 5)

    def add(
        label: str,
        category: str,
        anchors: Iterable[dict[str, Any]],
        fixture_blocks: Iterable[dict[str, Any]] = (),
    ) -> None:
        cases.append(
            {
                "case_id": f"ae2-s1-{len(cases) + 1:02d}",
                "milestone": "S1",
                "coverage_id": NATIVE_STRUCTURAL_COVERAGE,
                "route": NATIVE_STRUCTURAL_ROUTE,
                "label": label,
                "category": category,
                "anchors": tuple(anchors),
                "fixture_blocks": tuple(fixture_blocks),
            }
        )

    # Every identity is installed on every face. The nine persisted-spin
    # families cycle 0,1,2,3,0,1 across the six face orbits; their exhaustive
    # face x spin Cartesian closure is asserted in Java (336 total states).
    for direction_index, direction in enumerate(DIRECTION_DELTAS):
        anchors = []
        for part_name, group in NATIVE_STRUCTURAL_PARTS:
            part = native_structural_part(
                part_name,
                spin=(direction_index % 4 if part_name in NATIVE_STRUCTURAL_SPIN_PARTS else None),
                frequency=(0 if part_name in NATIVE_STRUCTURAL_P2P_PARTS else None),
            )
            anchor = cable_anchor(
                position(),
                cable_id("glass", "fluix"),
                expected_path="custom-s1",
                face_parts={direction: part},
                facades=(
                    {direction: facade("minecraft:stone")}
                    if direction_index == 0
                    and part_name
                    in {
                        "quartz_fiber",
                        "toggle_bus",
                        "import_bus",
                        "export_bus",
                        "level_emitter",
                        "terminal",
                    }
                    else {}
                ),
            )
            anchor.update(
                {
                    "native_part_group": group,
                    "installed_face": direction,
                    "orientation_orbit": direction_index,
                    "part_cable_type_requirement": (
                        "smart"
                        if part_name in {"level_emitter", "energy_level_emitter"}
                        else "glass"
                    ),
                    "expected_visible_cable_core": (
                        "covered"
                        if part_name in {"level_emitter", "energy_level_emitter"}
                        else "glass"
                    ),
                }
            )
            facade_cutouts = {
                "quartz_fiber": [6, 6, 10, 10],
                "toggle_bus": [6, 6, 10, 10],
                "import_bus": [4, 4, 12, 12],
                "export_bus": [6, 6, 10, 10],
                "level_emitter": [7, 7, 9, 9],
                "terminal": [2, 2, 14, 14],
            }
            if direction_index == 0 and part_name in facade_cutouts:
                anchor["expected_facade_cutout_sixteenths"] = facade_cutouts[
                    part_name
                ]
            anchors.append(anchor)
        add(
            f"all-native-parts-installed-{direction}",
            "native-part-identity-face-orbit",
            anchors,
        )

    # Each mask bit means the exact same plane part exists on the matching
    # coplanar adjacent cable-bus block. Helpers are deliberately not analyzed
    # anchors, preserving one center observation per mask.
    for plane_index, part_name in enumerate(NATIVE_STRUCTURAL_PLANE_PARTS):
        installed_face = "up" if plane_index == 0 else "north"
        mask_directions = NATIVE_STRUCTURAL_PLANE_MASK_DIRECTIONS[installed_face]
        anchors = []
        helpers = []
        for mask in range(16):
            center = position()
            plane_facades = {
                installed_face: facade(
                    "minecraft:glass"
                    if plane_index == 0 and mask == 8
                    else "minecraft:stone"
                )
            }
            if plane_index == 0 and mask == 8:
                plane_facades[mask_directions[0]] = facade("minecraft:stone")
            anchor = cable_anchor(
                center,
                cable_id("covered", "fluix"),
                expected_path="custom-s1",
                face_parts={installed_face: native_structural_part(part_name)},
                facades=plane_facades,
            )
            if plane_index == 0 and mask == 8:
                anchor["plane_perpendicular_facade_intersection"] = {
                    "plane_direction": installed_face,
                    "facade_direction": mask_directions[0],
                    "mask_bit": 8,
                    "expected_cutout_reaches_block_edge": True,
                    "transparent_installed_facade": "minecraft:glass",
                    "opaque_perpendicular_facade": "minecraft:stone",
                    "aabb_reversed_endpoint_normalization": {
                        "axis": "z",
                        "normalized_min_blocks": 0.0,
                        "normalized_max_policy": "thin-facade-thickness",
                    },
                }
            visual_local_bounds = native_structural_plane_local_bounds_sixteenths(
                mask,
                NATIVE_STRUCTURAL_PLANE_VISUAL_LOCAL_BOUND_BITS,
                (0, 1),
            )
            visual_local_axes = NATIVE_STRUCTURAL_PLANE_VISUAL_LOCAL_AXES[
                installed_face
            ]
            collision_local_bounds = (
                native_structural_plane_local_bounds_sixteenths(
                    mask,
                    NATIVE_STRUCTURAL_PLANE_COLLISION_LOCAL_BOUND_BITS[
                        installed_face
                    ],
                    (15, 16),
                )
            )
            collision_local_axes = NATIVE_STRUCTURAL_PLANE_COLLISION_LOCAL_AXES[
                installed_face
            ]
            anchor.update(
                {
                    "native_part_group": f"plane-{part_name.removesuffix('_plane')}",
                    "installed_face": installed_face,
                    "plane_mask": mask,
                    "plane_mask_bit_order": list(mask_directions),
                    "facade_structural_expectation": (
                        "same-face-plane-mask-asymmetric-cutout"
                    ),
                    # The logical mask, PlaneBakedModel local sheet and
                    # BusCollisionHelper local collision box are three
                    # distinct coordinate spaces. Preserve both local inputs
                    # and their independently transformed installed-world
                    # bounds so the two paths cannot be conflated.
                    "plane_visual_local_axes": list(visual_local_axes),
                    "plane_visual_local_bounds_sixteenths": visual_local_bounds,
                    "plane_visual_world_bounds_sixteenths": (
                        native_structural_plane_world_bounds_sixteenths(
                            visual_local_bounds, visual_local_axes
                        )
                    ),
                    "plane_facade_cutout_local_axes": list(
                        collision_local_axes
                    ),
                    "plane_facade_cutout_local_bounds_sixteenths": (
                        collision_local_bounds
                    ),
                    "plane_facade_cutout_world_bounds_sixteenths": (
                        native_structural_plane_world_bounds_sixteenths(
                            collision_local_bounds, collision_local_axes
                        )
                    ),
                }
            )
            anchors.append(anchor)
            for bit, neighbor_direction in zip(
                NATIVE_STRUCTURAL_PLANE_MASK_BITS,
                mask_directions,
                strict=True,
            ):
                if not mask & bit:
                    continue
                delta = DIRECTION_DELTAS[neighbor_direction]
                helper_position = tuple(
                    center[axis] + delta[axis] for axis in range(3)
                )
                helpers.append(
                    {
                        "position": helper_position,
                        "block_id": "ae2:cable_bus",
                        "cable_id": cable_id("covered", "fluix"),
                        "face_parts": {
                            installed_face: native_structural_part(part_name)
                        },
                        "purpose": f"plane-mask-{mask:04b}-{neighbor_direction}",
                    }
                )
        add(
            f"{part_name.replace('_', '-')}-all-sixteen-masks",
            "native-plane-mask-catalog",
            anchors,
            helpers,
        )

    for frequency in (0, 0x1234, 0xFFFF):
        anchors = []
        for direction_index, part_name in enumerate(NATIVE_STRUCTURAL_P2P_PARTS):
            direction = tuple(DIRECTION_DELTAS)[direction_index]
            anchor = cable_anchor(
                position(),
                cable_id("smart", "fluix"),
                expected_path="custom-s1",
                face_parts={
                    direction: native_structural_part(
                        part_name, frequency=frequency
                    )
                },
            )
            anchor.update(
                {
                    "native_part_group": f"p2p-{part_name.removesuffix('_p2p_tunnel')}",
                    "installed_face": direction,
                    "p2p_frequency_unsigned": frequency,
                    "p2p_frequency_pixels": 16,
                }
            )
            anchors.append(anchor)
        add(
            f"all-p2p-types-frequency-{frequency}",
            "native-p2p-type-frequency",
            anchors,
        )

    dense_anchors = []
    for direction in DIRECTION_DELTAS:
        dense_anchors.append(
            cable_anchor(
                position(),
                cable_id("dense_smart", "fluix"),
                expected_path="custom-s1",
                face_parts={direction: native_structural_part("cable_anchor")},
            )
        )
    dense_anchors.append(
        cable_anchor(
            position(),
            cable_id("covered", "fluix"),
            expected_path="stock-fallback-s1",
            face_parts={"north": {"id": "ae2:monitor", "spin": 4}},
            fallback_reason="invalid-reporting-spin-monitor",
        )
    )
    dense_anchors.append(
        cable_anchor(
            position(),
            cable_id("dense_smart", "fluix"),
            expected_path="custom-s1",
        )
    )
    add(
        "dense-anchor-legality-and-persistent-spin-control",
        "native-dense-part-legality",
        dense_anchors,
    )

    part_only = [
        cable_anchor(
            position(),
            None,
            expected_path="custom-s1",
            face_parts={direction: native_structural_part("cable_anchor")},
        )
        for direction in DIRECTION_DELTAS
    ]
    part_only.append(
        cable_anchor(
            position(),
            None,
            expected_path="custom-s1",
            face_parts={
                direction: native_structural_part("cable_anchor")
                for direction in DIRECTION_DELTAS
            },
        )
    )
    part_only.append(
        cable_anchor(
            position(),
            cable_id("covered", "fluix"),
            expected_path="custom-s1",
            facades={"north": facade("minecraft:stone")},
        )
    )
    add("part-and-facade-only-buses", "native-part-only-bus", part_only)

    neutral_facades = {
        entry["block_id"]: entry
        for entry in NATIVE_STRUCTURAL_NEUTRAL_FACADE_MATERIALS
    }
    vanilla_whitelist = {
        entry["block_id"]: entry
        for entry in NATIVE_STRUCTURAL_VANILLA_WHITELIST_CONTROLS
    }
    facade_mask_overrides: dict[int, dict[str, dict[str, Any]]] = {
        1: {"down": facade("ae2:quartz_glass")},
        2: {"up": facade("ae2:quartz_vibrant_glass")},
        3: {
            direction: facade(
                "ae2:controller", {"state": "offline", "type": "block"}
            )
            for direction in ("down", "up")
        },
        4: {
            "north": facade(
                "ae2:1k_crafting_storage",
                {"formed": "false", "powered": "false"},
            )
        },
        5: {
            "down": facade("ae2:quartz_glass"),
            "north": facade("ae2:quartz_vibrant_glass"),
        },
        6: {
            direction: facade(
                "ae2:4k_crafting_storage",
                {"formed": "false", "powered": "false"},
            )
            for direction in ("up", "north")
        },
        7: {
            direction: facade(
                "ae2:16k_crafting_storage",
                {"formed": "false", "powered": "false"},
            )
            for direction in ("down", "up", "north")
        },
        8: {
            "south": facade(
                "ae2:64k_crafting_storage",
                {"formed": "false", "powered": "false"},
            )
        },
        9: {
            direction: facade(
                "ae2:256k_crafting_storage",
                {"formed": "false", "powered": "false"},
            )
            for direction in ("down", "south")
        },
        10: {
            direction: facade(
                "ae2:crafting_monitor",
                {
                    "facing": "north",
                    "formed": "false",
                    "powered": "false",
                    "spin": "0",
                },
            )
            for direction in ("up", "south")
        },
        11: {
            direction: facade(
                "ae2:crafting_unit", {"formed": "false", "powered": "false"}
            )
            for direction in ("down", "up", "south")
        },
        12: {
            direction: facade(
                "ae2:crafting_accelerator",
                {"formed": "false", "powered": "false"},
            )
            for direction in ("north", "south")
        },
        13: {
            direction: facade(
                "minecraft:chiseled_bookshelf",
                NATIVE_STRUCTURAL_VANILLA_WHITELIST_CONTROLS[0]["properties"],
            )
            for direction in ("down", "north", "south")
        },
        14: {
            direction: facade(
                "minecraft:furnace",
                NATIVE_STRUCTURAL_VANILLA_WHITELIST_CONTROLS[1]["properties"],
            )
            for direction in ("up", "north", "south")
        },
        15: {
            "down": facade("minecraft:stone"),
            "up": facade("minecraft:stone"),
            "north": facade(
                "ae2:crafting_monitor",
                {
                    "facing": "east",
                    "formed": "true",
                    "powered": "true",
                    "spin": "3",
                },
            ),
            "south": facade("minecraft:stone"),
        },
        16: {"west": facade("minecraft:soul_sand")},
        17: {
            "down": facade("minecraft:honey_block"),
            "west": facade("minecraft:stone"),
        },
        18: {
            "up": facade("minecraft:honey_block"),
            "west": facade("minecraft:soul_sand"),
        },
        63: {
            direction: facade("ae2:quartz_glass")
            for direction in NATIVE_STRUCTURAL_FACADE_DIRECTIONS
        },
    }
    facade_masks: list[dict[str, Any]] = []
    facade_mask_helpers: dict[int, list[dict[str, Any]]] = {}
    for mask in range(64):
        center = position()
        mask_facades = facade_mask_overrides.get(
            mask,
            {
                direction: facade("minecraft:stone")
                for bit, direction in enumerate(NATIVE_STRUCTURAL_FACADE_DIRECTIONS)
                if mask & (1 << bit)
            },
        )
        anchor = cable_anchor(
            center,
            cable_id("covered", "fluix"),
            expected_path="custom-s1",
            facades=mask_facades,
        )
        native_entries = []
        vanilla_entries = []
        for direction, block_state in mask_facades.items():
            block_id = block_state["Name"]
            if block_id in neutral_facades:
                entry = neutral_facades[block_id]
                native_entries.append(
                    {
                        "direction": direction,
                        **entry,
                        "persisted_properties": block_state.get("Properties", {}),
                    }
                )
            elif block_id in vanilla_whitelist:
                vanilla_entries.append(
                    {
                        "direction": direction,
                        **vanilla_whitelist[block_id],
                    }
                )
        anchor.update(
            {
                "facade_mask": mask,
                "facade_mask_bit_order": list(NATIVE_STRUCTURAL_FACADE_DIRECTIONS),
                "facade_material_class": (
                    "native-connected-transparent-whitelist"
                    if mask in {1, 2, 5, 63}
                    else "native-neutral-whitelist"
                    if native_entries
                    else "vanilla-explicit-whitelist"
                    if vanilla_entries
                    else "opaque-full-cube"
                ),
            }
        )
        if native_entries:
            anchor["native_neutral_facade_materials"] = native_entries
        if vanilla_entries:
            anchor["facade_whitelist_controls"] = vanilla_entries
        if mask == 1:
            anchor["quartz_facade_appearance_control"] = "isolated"
            anchor["quartz_facade_light_policy"] = (
                "non-emissive-facade-blocklight-zero"
            )
        elif mask == 2:
            anchor["quartz_facade_appearance_control"] = "adjacent-real-quartz"
            anchor["quartz_facade_light_policy"] = (
                "non-emissive-facade-blocklight-zero"
            )
            facade_mask_helpers[mask] = [
                {
                    "position": (center[0], center[1] + 1, center[2]),
                    "block_id": "ae2:quartz_vibrant_glass",
                    "expected_state": {},
                    "placement_state": {},
                    "purpose": "quartz-facade-adjacent-real-quartz-appearance",
                    "facade_direction": "up",
                }
            ]
        elif mask == 5:
            anchor["quartz_facade_appearance_control"] = (
                "same-block-perpendicular-quartz-vibrant-facades"
            )
            anchor["quartz_facade_light_policy"] = (
                "non-emissive-facade-blocklight-zero"
            )
        elif mask == 15:
            anchor["native_facade_normalization"] = {
                "block_id": "ae2:crafting_monitor",
                "persisted_properties": {
                    "facing": "east",
                    "formed": "true",
                    "powered": "true",
                    "spin": "3",
                },
                "normalized_properties": {
                    "facing": "east",
                    "formed": "false",
                    "powered": "false",
                    "spin": "0",
                },
                "policy": "preserve-valid-facing-force-unformed-unpowered-spin-zero",
            }
        elif mask == 17:
            anchor["facade_structural_expectation"] = (
                "honey-transparent-inset-against-opaque-stone"
            )
        elif mask == 18:
            anchor["facade_structural_expectation"] = (
                "honey-transparent-inset-against-opaque-soul-sand"
            )
        elif mask == 63:
            anchor["quartz_facade_appearance_control"] = (
                "fully-surrounded-all-facade-quads-suppressed"
            )
            anchor["quartz_facade_light_policy"] = (
                "non-emissive-facade-blocklight-zero"
            )
            anchor["facade_structural_expectation"] = (
                "zero-facade-layers-remains-custom-cable-plus-six-short-stilts"
            )
            facade_mask_helpers[mask] = [
                {
                    "position": tuple(
                        center[axis] + DIRECTION_DELTAS[direction][axis]
                        for axis in range(3)
                    ),
                    "block_id": "ae2:quartz_glass",
                    "expected_state": {},
                    "placement_state": {},
                    "purpose": "quartz-facade-fully-surrounded-appearance",
                    "facade_direction": direction,
                }
                for direction in NATIVE_STRUCTURAL_FACADE_DIRECTIONS
            ]
        facade_masks.append(anchor)
    mask_start = 0
    for case_size in (11, 11, 11, 11, 10, 10):
        mask_end = mask_start + case_size
        add(
            f"facade-mask-{mask_start:02d}-through-{mask_end - 1:02d}",
            "native-facade-mask-catalog",
            facade_masks[mask_start:mask_end],
            (
                fixture
                for mask in range(mask_start, mask_end)
                for fixture in facade_mask_helpers.get(mask, ())
            ),
        )
        mask_start = mask_end

    transparent_facades = []
    transparent_helpers = []
    for direction in DIRECTION_DELTAS:
        center = position()
        anchor = cable_anchor(
            center,
            cable_id("glass", "fluix"),
            expected_path="custom-s1",
            facades={direction: facade("minecraft:glass")},
        )
        anchor.update(
            {
                "facade_material_class": "transparent-full-cube",
                "facade_expected_stilt": True,
            }
        )
        if direction == "up":
            delta = DIRECTION_DELTAS[direction]
            transparent_helpers.append(
                {
                    "position": tuple(
                        center[axis] + delta[axis] for axis in range(3)
                    ),
                    "block_id": "minecraft:glass",
                    "expected_state": {},
                    "placement_state": {},
                    "purpose": "facade-adjacent-skip-rendering-positive",
                    "facade_direction": direction,
                }
            )
            anchor["facade_adjacent_cull_expected"] = {
                "direction": direction,
                "adjacent_block_state": facade("minecraft:glass"),
                "culled_original_face": direction,
            }
        transparent_facades.append(anchor)
    add(
        "transparent-facade-six-faces",
        "native-facade-transparency",
        transparent_facades,
        transparent_helpers,
    )

    stateful_specs = (
        ("minecraft:oak_log", {"axis": "x"}),
        ("minecraft:oak_log", {"axis": "y"}),
        ("minecraft:oak_log", {"axis": "z"}),
        ("minecraft:magma_block", None),
        (
            "minecraft:oak_leaves",
            {"distance": "1", "persistent": "true", "waterlogged": "false"},
        ),
    )
    stateful = []
    stateful_helpers = []
    for index, (block_id, properties) in enumerate(stateful_specs):
        direction = tuple(DIRECTION_DELTAS)[index]
        center = position()
        anchor = cable_anchor(
            center,
            cable_id("covered", "fluix"),
            expected_path="custom-s1",
            facades={direction: facade(block_id, properties)},
        )
        anchor["facade_material_class"] = "static-resolved-block-state"
        if block_id == "minecraft:oak_log" and properties == {"axis": "y"}:
            delta = DIRECTION_DELTAS[direction]
            stateful_helpers.append(
                {
                    "position": tuple(
                        center[axis] + delta[axis] for axis in range(3)
                    ),
                    "block_id": block_id,
                    "expected_state": properties,
                    "placement_state": properties,
                    "purpose": "facade-adjacent-skip-rendering",
                    "facade_direction": direction,
                }
            )
            anchor["facade_adjacent_render_expected"] = {
                "direction": direction,
                "adjacent_block_state": facade(block_id, properties),
                "retained_original_face": direction,
            }
            stateful_helpers[-1]["purpose"] = (
                "facade-adjacent-non-culling-control"
            )
        stateful.append(anchor)
    add(
        "stateful-facade-materials",
        "native-facade-state-resolution",
        stateful,
        stateful_helpers,
    )

    coexistence_specs = (
        (None, {}, {"south": facade("minecraft:stone")}, "facade-only-short-stilt"),
        (
            cable_id("covered", "fluix"),
            {"north": native_structural_part("cable_anchor")},
            {"north": facade("minecraft:stone")},
            "same-face-anchor-short-no-cutout",
        ),
        (
            cable_id("covered", "fluix"),
            {"east": native_structural_part("terminal", spin=2)},
            {"east": facade("minecraft:stone")},
            "same-face-part-cutout",
        ),
        (
            cable_id("smart", "fluix"),
            {},
            {
                "north": facade("minecraft:stone"),
                "east": facade("minecraft:stone"),
                "up": facade("minecraft:stone"),
            },
            "opaque-adjacent-edge-corner-mask",
        ),
        (
            cable_id("covered", "fluix"),
            {"west": native_structural_part("cable_anchor")},
            {"up": facade("minecraft:glass"), "west": facade("minecraft:stone")},
            "center-anchor-transparent-adjacency",
        ),
    )
    coexistence = []
    for cable, parts, facades, expectation in coexistence_specs:
        if cable is None and not parts:
            cable = cable_id("covered", "fluix")
        anchor = cable_anchor(
            position(),
            cable,
            expected_path="custom-s1",
            face_parts=parts,
            facades=facades,
        )
        anchor["facade_structural_expectation"] = expectation
        coexistence.append(anchor)
    add(
        "facade-stilts-clipping-and-part-coexistence",
        "native-facade-structural-coexistence",
        coexistence,
    )

    if tuple(NATIVE_STRUCTURAL_ENDPOINT_POLICIES) != tuple(
        name for name, _family in NATIVE_STRUCTURAL_ENDPOINTS_ORDERED
    ):
        raise ValueError("native endpoint policy order changed")

    def endpoint_fixture_blocks(
        center: tuple[int, int, int],
        endpoint_name: str,
    ) -> tuple[dict[str, Any], ...]:
        """Build exact state/BE/formation evidence east of one cable bus."""
        policy = NATIVE_STRUCTURAL_ENDPOINT_POLICIES[endpoint_name]
        endpoint_position = (center[0] + 1, center[1], center[2])
        block_id = f"ae2:{endpoint_name}"

        def helper(
            helper_position: tuple[int, int, int],
            helper_block_id: str,
            block_entity_id: str,
            expected_state: dict[str, Any],
            *,
            placement_state: dict[str, Any] | None = None,
            structure: str | None = None,
            role: str | None = None,
            primary: bool = False,
        ) -> dict[str, Any]:
            value: dict[str, Any] = {
                "position": helper_position,
                "block_id": helper_block_id,
                "expected_block_entity_id": block_entity_id,
                "expected_state": expected_state,
                "placement_state": (
                    expected_state if placement_state is None else placement_state
                ),
                "purpose": "native-structural-endpoint",
            }
            if structure is not None:
                value["endpoint_structure"] = structure
            if role is not None:
                value["endpoint_structure_role"] = role
            if primary:
                value.update(
                    {
                        "endpoint_catalog_id": block_id,
                        "endpoint_observed_side": "west",
                        "endpoint_side_rule": policy["side_rule"],
                        "endpoint_connected": policy.get("connected", True),
                    }
                )
            return value

        formation = policy.get("formation")
        if formation in {"qnb-yz-edge-ring", "qnb-yz-center-link"}:
            # A valid bridge is a 3x3 YZ plane. The edge-ring fixture places
            # its selected endpoint at top-middle, one block above the link;
            # the link fixture places its selected endpoint at the center.
            link_position = (
                endpoint_position
                if formation == "qnb-yz-center-link"
                else (
                    endpoint_position[0],
                    endpoint_position[1] - 1,
                    endpoint_position[2],
                )
            )
            helpers = []
            for y_offset in (-1, 0, 1):
                for z_offset in (-1, 0, 1):
                    candidate = (
                        link_position[0],
                        link_position[1] + y_offset,
                        link_position[2] + z_offset,
                    )
                    is_link = y_offset == 0 and z_offset == 0
                    is_primary = candidate == endpoint_position
                    helpers.append(
                        helper(
                            candidate,
                            "ae2:quantum_link" if is_link else "ae2:quantum_ring",
                            "ae2:quantum_ring",
                            {"formed": True, "waterlogged": False},
                            placement_state={
                                "formed": False,
                                "waterlogged": False,
                            },
                            structure=formation,
                            role=(
                                "endpoint-link"
                                if is_primary and is_link
                                else "endpoint-edge-ring"
                                if is_primary
                                else "link"
                                if is_link
                                else "ring"
                            ),
                            primary=is_primary,
                        )
                    )
            return tuple(helpers)

        if formation == "vertical-three-pylon-middle":
            return tuple(
                helper(
                    (
                        endpoint_position[0],
                        endpoint_position[1] + offset,
                        endpoint_position[2],
                    ),
                    "ae2:spatial_pylon",
                    "ae2:spatial_pylon",
                    {"powered_on": False},
                    structure=formation,
                    role="endpoint-middle" if offset == 0 else "end",
                    primary=offset == 0,
                )
                for offset in (-1, 0, 1)
            )

        if formation == "vertical-crafting-pair":
            target_state = dict(policy["required_state"])
            target_placement_state = dict(target_state)
            target_placement_state["formed"] = False
            helper_state = {"formed": True, "powered": False}
            return (
                helper(
                    endpoint_position,
                    block_id,
                    policy["block_entity_id"],
                    target_state,
                    placement_state=target_placement_state,
                    structure=formation,
                    role="endpoint",
                    primary=True,
                ),
                helper(
                    (
                        endpoint_position[0],
                        endpoint_position[1] + 1,
                        endpoint_position[2],
                    ),
                    "ae2:1k_crafting_storage",
                    "ae2:crafting_storage",
                    helper_state,
                    placement_state={"formed": False, "powered": False},
                    structure=formation,
                    role="storage-helper",
                ),
            )

        if formation == "single-storage-crafting-cpu":
            target_state = dict(policy["required_state"])
            return (
                helper(
                    endpoint_position,
                    block_id,
                    policy["block_entity_id"],
                    target_state,
                    placement_state={"formed": False, "powered": False},
                    structure=formation,
                    role="endpoint-storage",
                    primary=True,
                ),
            )

        return (
            helper(
                endpoint_position,
                block_id,
                policy["block_entity_id"],
                dict(policy["required_state"]),
                primary=True,
            ),
        )

    endpoint_case_specs = (
        NATIVE_STRUCTURAL_ENDPOINTS_ORDERED[:9],
        NATIVE_STRUCTURAL_ENDPOINTS_ORDERED[9:12],
        NATIVE_STRUCTURAL_ENDPOINTS_ORDERED[12:],
    )
    dual_endpoint_specs = {
        "controller": {"local_family": "dense_smart", "straight": True},
        "wireless_access_point": {"local_family": "smart", "straight": True},
        "energy_acceptor": {"local_family": "covered", "straight": True},
        "molecular_assembler": {
            "local_family": "covered",
            "straight": False,
            "blocking_part": "ae2:terminal",
        },
    }
    local_families = ("glass", "covered", "smart", "dense_smart")
    family_rank = {family["key"]: index for index, family in enumerate(FAMILIES)}
    endpoint_catalog_offset = 0
    for endpoint_entries in endpoint_case_specs:
        anchors = []
        helpers = []
        for endpoint_index, (endpoint_name, declared_family) in enumerate(
            endpoint_entries
        ):
            center = position()
            direction = "east"
            dual_spec = dual_endpoint_specs.get(endpoint_name)
            local_family = (
                dual_spec["local_family"]
                if dual_spec is not None
                else local_families[endpoint_index % len(local_families)]
            )
            # Reuse the M1 rank contract for minimum width. The endpoint catalog
            # never declares glass; dense endpoint families still narrow to a
            # normal cable when the local cable is normal.
            effective_family = min(
                (local_family, declared_family),
                key=lambda value: family_rank[value],
            )
            anchor = cable_anchor(
                center,
                cable_id(local_family, "fluix"),
                expected_path="custom-s1",
            )
            if endpoint_name == "wireless_access_point":
                anchor["face_parts"]["north"] = native_structural_part(
                    "cable_anchor"
                )
                anchor["facades"]["up"] = facade("minecraft:stone")
            elif endpoint_name == "energy_acceptor":
                anchor["facades"]["up"] = facade("minecraft:stone")
            elif endpoint_name == "molecular_assembler":
                anchor["face_parts"]["north"] = native_structural_part(
                    "terminal", spin=0
                )
            endpoint = {
                "direction": direction,
                "block_id": f"ae2:{endpoint_name}",
                "block_entity_id": NATIVE_STRUCTURAL_ENDPOINT_POLICIES[
                    endpoint_name
                ]["block_entity_id"],
                "required_block_state": NATIVE_STRUCTURAL_ENDPOINT_POLICIES[
                    endpoint_name
                ]["required_state"],
                "observed_endpoint_side": "west",
                "side_rule": NATIVE_STRUCTURAL_ENDPOINT_POLICIES[endpoint_name][
                    "side_rule"
                ],
                "formation": NATIVE_STRUCTURAL_ENDPOINT_POLICIES[
                    endpoint_name
                ].get("formation"),
                "exposed_on_observed_side": NATIVE_STRUCTURAL_ENDPOINT_POLICIES[
                    endpoint_name
                ].get("connected", True),
                "declared_family": declared_family,
                "local_family": local_family,
                "effective_family": effective_family,
                "collar": (
                    effective_family
                    not in {"glass", "dense_covered", "dense_smart"}
                    and NATIVE_STRUCTURAL_ENDPOINT_POLICIES[endpoint_name].get(
                        "connected", True
                    )
                    and not (dual_spec or {}).get("straight", False)
                ),
                "topology": (
                    "native-grid-node-host"
                    if NATIVE_STRUCTURAL_ENDPOINT_POLICIES[endpoint_name].get(
                        "connected", True
                    )
                    else "known-native-grid-node-host-disconnected"
                ),
            }
            endpoints = [endpoint]
            if dual_spec is not None:
                opposite_state = dict(
                    NATIVE_STRUCTURAL_ENDPOINT_POLICIES[endpoint_name][
                        "required_state"
                    ]
                )
                if endpoint_name == "wireless_access_point":
                    opposite_state["facing"] = "west"
                opposite_endpoint = {
                    **endpoint,
                    "direction": "west",
                    "required_block_state": opposite_state,
                    "observed_endpoint_side": "east",
                    "collar": endpoint["collar"],
                }
                endpoints.append(opposite_endpoint)
                opposite_position = (center[0] - 1, center[1], center[2])
                helpers.append(
                    {
                        "position": opposite_position,
                        "block_id": f"ae2:{endpoint_name}",
                        "expected_block_entity_id": NATIVE_STRUCTURAL_ENDPOINT_POLICIES[
                            endpoint_name
                        ]["block_entity_id"],
                        "expected_state": opposite_state,
                        "placement_state": opposite_state,
                        "purpose": "native-structural-opposite-endpoint",
                        "endpoint_catalog_id": f"ae2:{endpoint_name}",
                        "endpoint_observed_side": "east",
                        "endpoint_side_rule": NATIVE_STRUCTURAL_ENDPOINT_POLICIES[
                            endpoint_name
                        ]["side_rule"],
                        "endpoint_connected": True,
                    }
                )
                anchor["endpoint_straight_optimization"] = {
                    "directions": ["west", "east"],
                    "effective_family": local_family,
                    "enabled": dual_spec["straight"],
                    "facades_are_attachments": False,
                    "cable_anchor_requires_connection": False,
                    "blocking_part": dual_spec.get("blocking_part"),
                    "machine_collars": (
                        False if dual_spec["straight"] else endpoint["collar"]
                    ),
                }
            anchor["native_endpoints"] = tuple(endpoints)
            anchors.append(anchor)
            helpers.extend(endpoint_fixture_blocks(center, endpoint_name))
        add(
            (
                f"native-endpoints-profile-order-{endpoint_catalog_offset + 1:02d}"
                f"-through-{endpoint_catalog_offset + len(endpoint_entries):02d}"
            ),
            "native-endpoint-side-type-topology",
            anchors,
            helpers,
        )
        endpoint_catalog_offset += len(endpoint_entries)

    retained_fallbacks = (
        cable_anchor(
            position(),
            cable_id("covered", "fluix"),
            expected_path="stock-fallback-s1",
            face_parts={
                "north": {"id": "ae2:semi_dark_monitor", "spin": 4}
            },
            fallback_reason="invalid-reporting-spin-semi-dark-monitor",
        ),
        cable_anchor(
            position(),
            cable_id("covered", "fluix"),
            expected_path="stock-fallback-s1",
            face_parts={
                "north": native_structural_part("terminal", spin=0),
                "south": {"id": "ae2:terminal", "spin": 4},
            },
            fallback_reason="invalid-reporting-spin-terminal-multipart",
        ),
        cable_anchor(
            position(),
            cable_id("smart", "fluix"),
            expected_path="stock-fallback-s1",
            face_parts={
                "south": {"id": "ae2:dark_monitor", "spin": 4},
            },
            fallback_reason="invalid-reporting-spin-dark-monitor",
        ),
        cable_anchor(
            position(),
            cable_id("covered", "fluix"),
            expected_path="stock-fallback-s1",
            face_parts={
                "up": {"id": "ae2:pattern_encoding_terminal", "spin": 4}
            },
            fallback_reason="invalid-reporting-spin-pattern-encoding-terminal",
        ),
    )
    add(
        "persistent-invalid-reporting-spin-controls",
        "native-structural-atomic-fallback",
        retained_fallbacks,
    )

    facade_fallbacks = (
        cable_anchor(
            position(),
            cable_id("covered", "fluix"),
            expected_path="stock-fallback-s1",
            facades={
                "north": facade(
                    "minecraft:oak_stairs",
                    {
                        "facing": "east",
                        "half": "bottom",
                        "shape": "straight",
                        "waterlogged": "false",
                    },
                ),
            },
            fallback_reason="non-full-cube-facade",
        ),
        cable_anchor(
            position(),
            cable_id("covered", "fluix"),
            expected_path="stock-fallback-s1",
            face_parts={
                "down": {"id": "ae2:crafting_terminal", "spin": 4}
            },
            fallback_reason="invalid-reporting-spin-crafting-terminal",
        ),
        cable_anchor(
            position(),
            cable_id("covered", "fluix"),
            expected_path="stock-fallback-s1",
            face_parts={
                "up": {"id": "ae2:storage_monitor", "spin": 4}
            },
            fallback_reason="invalid-reporting-spin-storage-monitor",
        ),
    )
    add(
        "persistent-facade-and-spin-fallback-controls",
        "native-structural-atomic-fallback",
        facade_fallbacks,
    )

    controls = []
    control_helpers = []
    disconnected_center = position()
    disconnected = cable_anchor(
        disconnected_center,
        cable_id("smart", "fluix"),
        expected_path="custom-s1",
    )
    disconnected["native_endpoints"] = (
        {
            "direction": "east",
            "block_id": "minecraft:stone",
            "block_entity_id": None,
            "required_block_state": {},
            "observed_endpoint_side": "west",
            "side_rule": "NOT_GRID_HOST",
            "formation": None,
            "exposed_on_observed_side": False,
            "declared_family": None,
            "local_family": "smart",
            "effective_family": None,
            "topology": "known-noncatalog-disconnected",
            "collar": False,
        },
    )
    controls.append(disconnected)
    control_helpers.append(
        {
            "position": (disconnected_center[0] + 1, disconnected_center[1], disconnected_center[2]),
            "block_id": "minecraft:stone",
            "purpose": "known-noncatalog-disconnected-neighbor",
        }
    )
    restricted_center = position()
    restricted = cable_anchor(
        restricted_center,
        cable_id("covered", "fluix"),
        expected_path="custom-s1",
        face_parts={"north": native_structural_part("terminal", spin=3)},
        facades={"north": facade("minecraft:stone")},
    )
    restricted["native_endpoints"] = (
        {
            "direction": "south",
            "block_id": "ae2:wireless_access_point",
            "block_entity_id": "ae2:wireless_access_point",
            "required_block_state": {
                "facing": "north",
                "state": "off",
                "waterlogged": False,
            },
            "observed_endpoint_side": "north",
            "side_rule": "BACK",
            "formation": None,
            "exposed_on_observed_side": False,
            "declared_family": "smart",
            "local_family": "covered",
            "effective_family": "covered",
            "collar": False,
            "topology": "known-native-grid-node-host-disconnected",
        },
    )
    controls.append(restricted)
    control_helpers.append(
        {
            "position": (
                restricted_center[0],
                restricted_center[1],
                restricted_center[2] + 1,
            ),
            "block_id": "ae2:wireless_access_point",
            "expected_block_entity_id": "ae2:wireless_access_point",
            "expected_state": {
                "facing": "north",
                "state": "off",
                "waterlogged": False,
            },
            "placement_state": {
                "facing": "north",
                "state": "off",
                "waterlogged": False,
            },
            "purpose": "coexistent-part-facade-known-native-disconnected",
            "endpoint_catalog_id": "ae2:wireless_access_point",
            "endpoint_observed_side": "north",
            "endpoint_side_rule": "BACK",
            "endpoint_connected": False,
        }
    )
    extension_center = position()
    extension = cable_anchor(
        extension_center,
        cable_id("smart", "fluix"),
        expected_path="stock-fallback-s1",
        fallback_reason="known-compatible-extension-endpoint-unknown",
    )
    extension["native_endpoints"] = (
        {
            "direction": "east",
            "block_id": NATIVE_STRUCTURAL_UNKNOWN_EXTENSION_ENDPOINT["block_id"],
            "block_entity_id": NATIVE_STRUCTURAL_UNKNOWN_EXTENSION_ENDPOINT[
                "block_entity_id"
            ],
            "required_block_state": NATIVE_STRUCTURAL_UNKNOWN_EXTENSION_ENDPOINT[
                "required_state"
            ],
            "observed_endpoint_side": "west",
            "side_rule": "UNSUPPORTED_COMPATIBLE_GRID_HOST",
            "formation": None,
            "exposed_on_observed_side": True,
            "declared_family": "smart",
            "local_family": "smart",
            "effective_family": None,
            "collar": False,
            "topology": "known-compatible-extension-unknown",
        },
    )
    controls.append(extension)
    control_helpers.append(
        {
            "position": (
                extension_center[0] + 1,
                extension_center[1],
                extension_center[2],
            ),
            "block_id": NATIVE_STRUCTURAL_UNKNOWN_EXTENSION_ENDPOINT["block_id"],
            "expected_block_entity_id": NATIVE_STRUCTURAL_UNKNOWN_EXTENSION_ENDPOINT[
                "block_entity_id"
            ],
            "expected_state": NATIVE_STRUCTURAL_UNKNOWN_EXTENSION_ENDPOINT[
                "required_state"
            ],
            "placement_state": NATIVE_STRUCTURAL_UNKNOWN_EXTENSION_ENDPOINT[
                "required_state"
            ],
            "purpose": "known-compatible-extension-unknown-fallback",
            "endpoint_observed_side": "west",
            "endpoint_side_rule": "UNSUPPORTED_COMPATIBLE_GRID_HOST",
            "endpoint_connected": None,
            "artifact": NATIVE_STRUCTURAL_UNKNOWN_EXTENSION_ENDPOINT["artifact"],
            "artifact_sha256": NATIVE_STRUCTURAL_UNKNOWN_EXTENSION_ENDPOINT[
                "artifact_sha256"
            ],
        }
    )
    add(
        "disconnected-endpoint-and-whole-bus-controls",
        "native-structural-topology-fallback-control",
        controls,
        control_helpers,
    )

    validate_s1_endpoint_fixtures(cases)
    attach_and_validate_s1_schema9_disabled_projection(cases)
    if len(cases) != EXPECTED_S1_CASE_COUNT or next_anchor != EXPECTED_S1_ANCHOR_COUNT:
        raise ValueError(
            f"S1 case matrix changed: {len(cases)} cases/{next_anchor} anchors"
        )
    return cases


def validate_s1_endpoint_fixtures(cases: list[dict[str, Any]]) -> None:
    """Source-derived endpoint state, side, BE and formation acceptance gate."""
    direction_offsets = {
        "down": (0, -1, 0),
        "up": (0, 1, 0),
        "north": (0, 0, -1),
        "south": (0, 0, 1),
        "west": (-1, 0, 0),
        "east": (1, 0, 0),
    }
    all_fixture_by_position: dict[tuple[int, int, int], dict[str, Any]] = {}
    for case in cases:
        for fixture in case["fixture_blocks"]:
            fixture_position = fixture["position"]
            if fixture_position in all_fixture_by_position:
                raise ValueError(
                    f"duplicate S1 helper position {fixture_position}"
                )
            all_fixture_by_position[fixture_position] = fixture

    def validate_complete_state(
        endpoint_name: str,
        state: Any,
        label: str,
    ) -> None:
        schema = NATIVE_STRUCTURAL_ENDPOINT_STATE_SCHEMAS[endpoint_name]
        if not isinstance(state, dict) or set(state) != set(schema):
            raise ValueError(
                f"{label} state keys changed for ae2:{endpoint_name}"
            )
        for key, value in state.items():
            serialized = (
                str(value).lower() if isinstance(value, bool) else str(value)
            )
            if serialized not in schema[key]:
                raise ValueError(
                    f"{label} state value changed for ae2:{endpoint_name}.{key}"
                )

    # Check every native observation, including opposite straight endpoints
    # and the deliberately disconnected WAP control.  Exact state schemas are
    # a fail-closed input contract; an omitted property must never be hidden
    # by a default blockstate selected by the fixture/export harness.
    native_observation_count = 0
    for case in cases:
        for anchor in case["anchors"]:
            center = anchor["position"]
            for observation in anchor.get("native_endpoints", ()):
                block_id = observation.get("block_id")
                if not isinstance(block_id, str) or not block_id.startswith("ae2:"):
                    continue
                endpoint_name = block_id.removeprefix("ae2:")
                if endpoint_name not in NATIVE_STRUCTURAL_ENDPOINT_POLICIES:
                    continue
                native_observation_count += 1
                required_state = observation.get("required_block_state")
                validate_complete_state(
                    endpoint_name,
                    required_state,
                    "endpoint observation",
                )
                direction = observation.get("direction")
                offset = direction_offsets.get(direction)
                if offset is None:
                    raise ValueError(
                        f"endpoint direction changed for ae2:{endpoint_name}"
                    )
                helper_position = tuple(
                    coordinate + delta
                    for coordinate, delta in zip(center, offset, strict=True)
                )
                helper = all_fixture_by_position.get(helper_position)
                if (
                    helper is None
                    or helper.get("block_id") != block_id
                    or helper.get("expected_state") != required_state
                ):
                    raise ValueError(
                        f"endpoint helper state changed for ae2:{endpoint_name}"
                    )
                validate_complete_state(
                    endpoint_name,
                    helper.get("expected_state"),
                    "endpoint helper expected",
                )
                validate_complete_state(
                    endpoint_name,
                    helper.get("placement_state"),
                    "endpoint helper placement",
                )
    if native_observation_count != 35:
        raise ValueError(
            "native endpoint observation closure changed: "
            f"{native_observation_count} != 35"
        )

    endpoint_cases = cases[22:25]
    endpoint_anchors = [
        anchor for case in endpoint_cases for anchor in case["anchors"]
    ]
    fixture_by_position = {
        fixture["position"]: fixture
        for case in endpoint_cases
        for fixture in case["fixture_blocks"]
    }
    if len(endpoint_anchors) != len(NATIVE_STRUCTURAL_ENDPOINTS_ORDERED):
        raise ValueError("native endpoint anchor catalog changed")

    dual_specs = {
        "controller": {"local_family": "dense_smart", "straight": True},
        "wireless_access_point": {"local_family": "smart", "straight": True},
        "energy_acceptor": {"local_family": "covered", "straight": True},
        "molecular_assembler": {
            "local_family": "covered",
            "straight": False,
            "blocking_part": "ae2:terminal",
        },
    }
    for anchor, (endpoint_name, declared_family) in zip(
        endpoint_anchors,
        NATIVE_STRUCTURAL_ENDPOINTS_ORDERED,
        strict=True,
    ):
        policy = NATIVE_STRUCTURAL_ENDPOINT_POLICIES[endpoint_name]
        endpoint_id = f"ae2:{endpoint_name}"
        observations = anchor.get("native_endpoints")
        expected_observation_count = 2 if endpoint_name in dual_specs else 1
        if (
            not isinstance(observations, tuple)
            or len(observations) != expected_observation_count
        ):
            raise ValueError(f"endpoint observation count changed for {endpoint_id}")
        primary = observations[0]
        connected = policy.get("connected", True)
        local_family = decode_cable_id(anchor["cable_id"])[0]["key"]
        family_rank = {
            family["key"]: index for index, family in enumerate(FAMILIES)
        }
        expected_effective = min(
            (local_family, declared_family),
            key=lambda value: family_rank[value],
        )
        expected_collar = (
            connected
            and not dual_specs.get(endpoint_name, {}).get("straight", False)
            and expected_effective not in {"glass", "dense_covered", "dense_smart"}
        )
        expected_primary = {
            "direction": "east",
            "block_id": endpoint_id,
            "block_entity_id": policy["block_entity_id"],
            "required_block_state": policy["required_state"],
            "observed_endpoint_side": "west",
            "side_rule": policy["side_rule"],
            "formation": policy.get("formation"),
            "exposed_on_observed_side": connected,
            "declared_family": declared_family,
            "local_family": local_family,
            "effective_family": expected_effective,
            "collar": expected_collar,
            "topology": (
                "native-grid-node-host"
                if connected
                else "known-native-grid-node-host-disconnected"
            ),
        }
        if primary != expected_primary:
            raise ValueError(f"endpoint source policy changed for {endpoint_id}")

        center = anchor["position"]
        primary_position = (center[0] + 1, center[1], center[2])
        primary_fixture = fixture_by_position.get(primary_position)
        if (
            primary_fixture is None
            or primary_fixture.get("block_id") != endpoint_id
            or primary_fixture.get("expected_block_entity_id")
            != policy["block_entity_id"]
            or primary_fixture.get("expected_state") != policy["required_state"]
            or primary_fixture.get("endpoint_catalog_id") != endpoint_id
            or primary_fixture.get("endpoint_observed_side") != "west"
            or primary_fixture.get("endpoint_side_rule") != policy["side_rule"]
            or primary_fixture.get("endpoint_connected") is not connected
        ):
            raise ValueError(f"endpoint helper evidence changed for {endpoint_id}")

        formation = policy.get("formation")
        if formation in {"qnb-yz-edge-ring", "qnb-yz-center-link"}:
            link_position = (
                primary_position
                if endpoint_name == "quantum_link"
                else (primary_position[0], primary_position[1] - 1, primary_position[2])
            )
            expected_positions = {
                (
                    link_position[0],
                    link_position[1] + y_offset,
                    link_position[2] + z_offset,
                )
                for y_offset in (-1, 0, 1)
                for z_offset in (-1, 0, 1)
            }
            if any(
                fixture_by_position.get(position, {}).get("endpoint_structure")
                != formation
                for position in expected_positions
            ):
                raise ValueError(f"QNB formation evidence changed for {endpoint_id}")
        elif formation == "vertical-three-pylon-middle":
            expected_positions = {
                (primary_position[0], primary_position[1] + offset, primary_position[2])
                for offset in (-1, 0, 1)
            }
            if any(
                fixture_by_position.get(position, {}).get("endpoint_structure")
                != formation
                for position in expected_positions
            ):
                raise ValueError("spatial-pylon endpoint formation evidence changed")
        elif formation == "vertical-crafting-pair":
            helper_position = (
                primary_position[0],
                primary_position[1] + 1,
                primary_position[2],
            )
            helper = fixture_by_position.get(helper_position)
            if (
                helper is None
                or helper.get("block_id") != "ae2:1k_crafting_storage"
                or helper.get("expected_block_entity_id") != "ae2:crafting_storage"
                or helper.get("expected_state")
                != {"formed": True, "powered": False}
                or helper.get("endpoint_structure_role") != "storage-helper"
            ):
                raise ValueError(
                    f"crafting endpoint formation evidence changed for {endpoint_id}"
                )
        elif formation == "single-storage-crafting-cpu":
            if (
                primary_fixture.get("endpoint_structure") != formation
                or primary_fixture.get("endpoint_structure_role")
                != "endpoint-storage"
                or primary_fixture.get("placement_state")
                != {"formed": False, "powered": False}
            ):
                raise ValueError(
                    "single-storage crafting endpoint evidence changed"
                )

        if endpoint_name in dual_specs:
            dual_spec = dual_specs[endpoint_name]
            opposite = observations[1]
            expected_opposite_state = dict(policy["required_state"])
            if endpoint_name == "wireless_access_point":
                expected_opposite_state["facing"] = "west"
            if (
                opposite.get("direction") != "west"
                or opposite.get("observed_endpoint_side") != "east"
                or opposite.get("required_block_state") != expected_opposite_state
                or opposite.get("exposed_on_observed_side") is not True
                or opposite.get("collar") is not expected_collar
                or anchor.get("endpoint_straight_optimization")
                != {
                    "directions": ["west", "east"],
                    "effective_family": dual_spec["local_family"],
                    "enabled": dual_spec["straight"],
                    "facades_are_attachments": False,
                    "cable_anchor_requires_connection": False,
                    "blocking_part": dual_spec.get("blocking_part"),
                    "machine_collars": (
                        False if dual_spec["straight"] else expected_collar
                    ),
                }
            ):
                raise ValueError(
                    f"opposite straight endpoint policy changed for {endpoint_id}"
                )
            opposite_position = (center[0] - 1, center[1], center[2])
            opposite_fixture = fixture_by_position.get(opposite_position)
            if (
                opposite_fixture is None
                or opposite_fixture.get("expected_block_entity_id")
                != policy["block_entity_id"]
                or opposite_fixture.get("expected_state")
                != expected_opposite_state
                or opposite_fixture.get("endpoint_observed_side") != "east"
                or opposite_fixture.get("endpoint_connected") is not True
            ):
                raise ValueError(
                    f"opposite endpoint helper changed for {endpoint_id}"
                )
            expected_parts = (
                {"north": {"id": "ae2:cable_anchor"}}
                if endpoint_name == "wireless_access_point"
                else {"north": {"id": "ae2:terminal", "spin": 0}}
                if endpoint_name == "molecular_assembler"
                else {}
            )
            expected_facades = (
                {"up": {"Name": "minecraft:stone"}}
                if endpoint_name in {"wireless_access_point", "energy_acceptor"}
                else {}
            )
            if (
                anchor["face_parts"] != expected_parts
                or anchor["facades"] != expected_facades
            ):
                raise ValueError(
                    f"straight endpoint attachment semantics changed for {endpoint_id}"
                )

    # The exact pack-pinned ExpandedAE grid host is deliberately outside the
    # S1 native catalog.  It must produce UNKNOWN/whole-bus fallback rather
    # than being silently treated as a disconnected ordinary neighbor.
    control_case = cases[27]
    extension_anchor = control_case["anchors"][2]
    extension_position = extension_anchor["position"]
    expected_extension_observation = {
        "direction": "east",
        "block_id": NATIVE_STRUCTURAL_UNKNOWN_EXTENSION_ENDPOINT["block_id"],
        "block_entity_id": NATIVE_STRUCTURAL_UNKNOWN_EXTENSION_ENDPOINT[
            "block_entity_id"
        ],
        "required_block_state": NATIVE_STRUCTURAL_UNKNOWN_EXTENSION_ENDPOINT[
            "required_state"
        ],
        "observed_endpoint_side": "west",
        "side_rule": "UNSUPPORTED_COMPATIBLE_GRID_HOST",
        "formation": None,
        "exposed_on_observed_side": True,
        "declared_family": "smart",
        "local_family": "smart",
        "effective_family": None,
        "collar": False,
        "topology": "known-compatible-extension-unknown",
    }
    extension_helpers = [
        fixture
        for fixture in control_case["fixture_blocks"]
        if fixture.get("purpose")
        == "known-compatible-extension-unknown-fallback"
    ]
    if (
        extension_anchor.get("expected_path") != "stock-fallback-s1"
        or extension_anchor.get("fallback_reason")
        != "known-compatible-extension-endpoint-unknown"
        or extension_anchor.get("native_endpoints")
        != (expected_extension_observation,)
        or len(extension_helpers) != 1
        or extension_helpers[0].get("position")
        != (extension_position[0] + 1, extension_position[1], extension_position[2])
        or extension_helpers[0].get("block_id")
        != NATIVE_STRUCTURAL_UNKNOWN_EXTENSION_ENDPOINT["block_id"]
        or extension_helpers[0].get("expected_block_entity_id")
        != NATIVE_STRUCTURAL_UNKNOWN_EXTENSION_ENDPOINT["block_entity_id"]
        or extension_helpers[0].get("expected_state")
        != NATIVE_STRUCTURAL_UNKNOWN_EXTENSION_ENDPOINT["required_state"]
        or extension_helpers[0].get("artifact_sha256")
        != NATIVE_STRUCTURAL_UNKNOWN_EXTENSION_ENDPOINT["artifact_sha256"]
    ):
        raise ValueError("known-compatible extension UNKNOWN fixture changed")

    persistent_fallbacks = {
        anchor["position"]: anchor
        for case in cases
        for anchor in case["anchors"]
        if anchor.get("expected_path") == "stock-fallback-s1"
    }
    expected_invalid_spin_parts = {
        (233, 100, 343): {"north": ("ae2:monitor", 4)},
        (260, 100, 358): {"north": ("ae2:semi_dark_monitor", 4)},
        (263, 100, 358): {
            "north": ("ae2:terminal", 0),
            "south": ("ae2:terminal", 4),
        },
        (266, 100, 358): {"south": ("ae2:dark_monitor", 4)},
        (269, 100, 358): {
            "up": ("ae2:pattern_encoding_terminal", 4)
        },
        (275, 100, 358): {"down": ("ae2:crafting_terminal", 4)},
        (278, 100, 358): {"up": ("ae2:storage_monitor", 4)},
    }
    for position, expected_parts in expected_invalid_spin_parts.items():
        anchor = persistent_fallbacks.get(position)
        actual_parts = {
            direction: (part.get("id"), part.get("spin"))
            for direction, part in (anchor or {}).get("face_parts", {}).items()
        }
        if actual_parts != expected_parts:
            raise ValueError(
                f"persistent invalid-spin fallback changed at {position}"
            )
    stair_fallback = persistent_fallbacks.get((272, 100, 358))
    if (
        stair_fallback is None
        or stair_fallback.get("fallback_reason") != "non-full-cube-facade"
        or stair_fallback.get("facades")
        != {
            "north": {
                "Name": "minecraft:oak_stairs",
                "Properties": {
                    "facing": "east",
                    "half": "bottom",
                    "shape": "straight",
                    "waterlogged": "false",
                },
            }
        }
    ):
        raise ValueError("persistent stair-facade fallback changed")


def create_cases() -> list[dict[str, Any]]:
    cases: list[dict[str, Any]] = []

    def add_m1(
        label: str,
        category: str,
        anchors: Iterable[dict[str, Any]],
        fixture_blocks: Iterable[dict[str, Any]] = (),
    ) -> None:
        cases.append(
            {
                "case_id": f"ae2-m1-{len(cases) + 1:02d}",
                "milestone": "M1",
                "route": "ae2:cable_bus",
                "label": label,
                "category": category,
                "anchors": tuple(anchors),
                "fixture_blocks": tuple(fixture_blocks),
            }
        )

    add_m1(
        "stone-control",
        "control",
        (
            {
                "position": (210, 100, 226),
                "block_id": "minecraft:stone",
                "cable_id": None,
                "expected_path": "stock-control",
            },
        ),
    )
    add_m1(
        "glass-to-energy-acceptor-fallback",
        "device-endpoint-fallback",
        (
            cable_anchor(
                (216, 100, 226),
                cable_id("glass", "fluix"),
                expected_path="stock-fallback-device-endpoint",
                ambiguous_neighbor={
                    "direction": "east",
                    "block_id": "ae2:energy_acceptor",
                },
            ),
        ),
        ({"position": (217, 100, 226), "block_id": "ae2:energy_acceptor"},),
    )
    add_m1(
        "dense-smart-to-controller-fallback",
        "device-endpoint-fallback",
        (
            cable_anchor(
                (222, 100, 226),
                cable_id("dense_smart", "fluix"),
                expected_path="stock-fallback-device-endpoint",
                ambiguous_neighbor={
                    "direction": "east",
                    "block_id": "ae2:controller",
                },
            ),
        ),
        ({"position": (223, 100, 226), "block_id": "ae2:controller"},),
    )

    # Five rows of 17 same-ID dominoes cover all 85 center-part IDs and their
    # family/color-selected runtime texture closure.
    for family_index, family in enumerate(FAMILIES):
        anchors = []
        z = 194 + family_index * 4
        for color_index, (registry_prefix, _texture_name) in enumerate(COLORS):
            x = 210 + color_index * 3
            value = cable_id(family["key"], registry_prefix)
            anchors.extend(
                (cable_anchor((x, 100, z), value), cable_anchor((x + 1, 100, z), value))
            )
        add_m1(
            f"all-colors-{family['key']}-dominoes",
            "color-family-catalog",
            anchors,
        )

    # One three-block opposite-axis line per family exercises straight-model
    # simplification, including the smart off-channel overlay layers.
    for family_index, family in enumerate(FAMILIES):
        x = 210 + family_index * 7
        value = cable_id(family["key"], "fluix")
        add_m1(
            f"straight-{family['key']}",
            "straight",
            tuple(cable_anchor((x + offset, 100, 222), value) for offset in range(3)),
        )

    add_m1(
        "glass-corner",
        "junction",
        tuple(
            cable_anchor(position, cable_id("glass", "fluix"))
            for position in ((211, 100, 234), (212, 100, 234), (211, 100, 235))
        ),
    )
    add_m1(
        "smart-t-junction",
        "junction",
        tuple(
            cable_anchor(position, cable_id("smart", "fluix"))
            for position in (
                (223, 100, 234),
                (222, 100, 234),
                (224, 100, 234),
                (223, 100, 235),
            )
        ),
    )
    add_m1(
        "dense-covered-cross",
        "junction",
        tuple(
            cable_anchor(position, cable_id("dense_covered", "fluix"))
            for position in (
                (237, 100, 234),
                (236, 100, 234),
                (238, 100, 234),
                (237, 100, 233),
                (237, 100, 235),
            )
        ),
    )
    add_m1(
        "dense-smart-six-way",
        "junction",
        tuple(
            cable_anchor(position, cable_id("dense_smart", "fluix"))
            for position in (
                (251, 102, 234),
                (250, 102, 234),
                (252, 102, 234),
                (251, 102, 233),
                (251, 102, 235),
                (251, 101, 234),
                (251, 103, 234),
            )
        ),
    )

    family_pairs = tuple(
        (first, second)
        for first_index, first in enumerate(FAMILIES)
        for second in FAMILIES[first_index:]
    )
    for pair_index, (first, second) in enumerate(family_pairs):
        x = 210 + pair_index * 3
        add_m1(
            f"compatible-fluix-{first['key']}-to-{second['key']}",
            "compatible-family-pair",
            (
                cable_anchor((x, 100, 214), cable_id(first["key"], "fluix")),
                cable_anchor((x + 1, 100, 214), cable_id(second["key"], "fluix")),
            ),
        )
    add_m1(
        "compatible-red-covered-to-smart",
        "compatible-same-color-family-pair",
        (
            cable_anchor((256, 100, 214), cable_id("covered", "red")),
            cable_anchor((257, 100, 214), cable_id("smart", "red")),
        ),
    )
    for pair_index, (first, second) in enumerate(family_pairs):
        x = 210 + pair_index * 3
        add_m1(
            f"incompatible-red-{first['key']}-to-blue-{second['key']}",
            "incompatible-color-pair",
            (
                cable_anchor((x, 100, 218), cable_id(first["key"], "red")),
                cable_anchor((x + 1, 100, 218), cable_id(second["key"], "blue")),
            ),
        )

    if len(cases) != EXPECTED_M1_CASE_COUNT:
        raise ValueError("M1 regression case construction changed unexpectedly")

    def add_m2(
        label: str,
        category: str,
        anchors: Iterable[dict[str, Any]],
    ) -> None:
        case_number = len(cases) - EXPECTED_M1_CASE_COUNT + 1
        cases.append(
            {
                "case_id": f"ae2-m2-{case_number:02d}",
                "milestone": "M2",
                "route": "ae2:cable_bus",
                "label": label,
                "category": category,
                "anchors": tuple(anchors),
                "fixture_blocks": (),
            }
        )

    add_m2(
        "terminal-six-faces-spins-zero-one",
        "terminal-orientation",
        (
            cable_anchor(
                (210, 100, 243),
                cable_id("glass", "fluix"),
                expected_path="custom-m2",
                face_parts={"down": terminal(0)},
            ),
            cable_anchor(
                (214, 100, 243),
                cable_id("covered", "fluix"),
                expected_path="custom-m2",
                face_parts={"up": terminal(1)},
            ),
            cable_anchor(
                (218, 100, 243),
                cable_id("smart", "fluix"),
                expected_path="custom-m2",
                face_parts={"north": terminal(0)},
            ),
            cable_anchor(
                (222, 100, 243),
                cable_id("glass", "fluix"),
                expected_path="custom-m2",
                face_parts={"south": terminal(1)},
            ),
            cable_anchor(
                (226, 100, 243),
                cable_id("covered", "fluix"),
                expected_path="custom-m2",
                face_parts={"west": terminal(0)},
            ),
            cable_anchor(
                (230, 100, 243),
                cable_id("smart", "fluix"),
                expected_path="custom-m2",
                face_parts={"east": terminal(1)},
            ),
        ),
    )
    add_m2(
        "multiple-mixed-spin-terminals",
        "terminal-multipart",
        (
            cable_anchor(
                (234, 100, 243),
                cable_id("covered", "fluix"),
                expected_path="custom-m2",
                face_parts={
                    "down": terminal(0),
                    "south": terminal(1),
                    "east": terminal(0),
                },
            ),
        ),
    )
    add_m2(
        "terminal-disables-straight-simplification",
        "terminal-topology",
        (
            cable_anchor(
                (210, 100, 246),
                cable_id("covered", "fluix"),
                expected_path="custom-m2",
            ),
            cable_anchor(
                (211, 100, 246),
                cable_id("covered", "fluix"),
                expected_path="custom-m2",
                face_parts={"up": terminal(1)},
            ),
            cable_anchor(
                (212, 100, 246),
                cable_id("covered", "fluix"),
                expected_path="custom-m2",
            ),
        ),
    )
    add_m2(
        "plain-stone-facade-south",
        "facade-supported",
        (
            cable_anchor(
                (216, 100, 246),
                cable_id("glass", "fluix"),
                expected_path="custom-m2",
                face_parts={"south": terminal(0)},
                facades={"south": facade(STONE_BLOCK_ID)},
            ),
        ),
    )
    add_m2(
        "plain-stone-facade-up",
        "facade-supported",
        (
            cable_anchor(
                (220, 100, 246),
                cable_id("covered", "fluix"),
                expected_path="custom-m2",
                face_parts={"up": terminal(1)},
                facades={"up": facade(STONE_BLOCK_ID)},
            ),
        ),
    )

    fallback_positions = iter(range(210, 237, 3))

    def fallback_anchor(
        reason: str,
        *,
        value: str | None = None,
        centerless: bool = False,
        face_parts: dict[str, dict[str, Any]] | None = None,
        facades: dict[str, dict[str, Any]] | None = None,
    ) -> dict[str, Any]:
        return cable_anchor(
            (next(fallback_positions), 100, 248),
            None if centerless else value or cable_id("glass", "fluix"),
            expected_path="stock-fallback-m2",
            face_parts=face_parts,
            facades=facades,
            fallback_reason=reason,
        )

    add_m2(
        "cable-anchor-face-part-fallback",
        "atomic-fallback",
        (fallback_anchor(
            "unsupported-face-part",
            face_parts={"north": terminal(None, part_id="ae2:cable_anchor")},
        ),),
    )
    add_m2(
        "unsupported-monitor-part-fallback",
        "atomic-fallback",
        (fallback_anchor(
            "unsupported-face-part",
            face_parts={"north": terminal(0, part_id="ae2:monitor")},
        ),),
    )
    add_m2(
        "terminal-out-of-range-spin-fallback",
        "atomic-fallback",
        (fallback_anchor(
            "invalid-face-part-spin",
            face_parts={"north": terminal(4)},
        ),),
    )
    add_m2(
        "standalone-terminal-missing-center-fallback",
        "atomic-fallback",
        (fallback_anchor(
            "missing-center-part",
            centerless=True,
            face_parts={"north": terminal(0)},
        ),),
    )
    add_m2(
        "facade-only-fallback",
        "atomic-fallback",
        (fallback_anchor(
            "unsupported-facade-layout",
            facades={"south": facade(STONE_BLOCK_ID)},
        ),),
    )
    add_m2(
        "glass-facade-fallback",
        "atomic-fallback",
        (fallback_anchor(
            "unsupported-facade-state",
            face_parts={"south": terminal(0)},
            facades={"south": facade("minecraft:glass")},
        ),),
    )
    add_m2(
        "property-bearing-facade-fallback",
        "atomic-fallback",
        (fallback_anchor(
            "unsupported-facade-state",
            face_parts={"south": terminal(0)},
            facades={"south": facade("minecraft:oak_log", {"axis": "y"})},
        ),),
    )
    add_m2(
        "multiple-facades-fallback",
        "atomic-fallback",
        (fallback_anchor(
            "unsupported-facade-layout",
            face_parts={"north": terminal(0), "south": terminal(1)},
            facades={
                "north": facade(STONE_BLOCK_ID),
                "south": facade(STONE_BLOCK_ID),
            },
        ),),
    )
    add_m2(
        "facade-with-extra-part-fallback",
        "atomic-fallback",
        (fallback_anchor(
            "unsupported-facade-layout",
            face_parts={"north": terminal(1), "south": terminal(0)},
            facades={"south": facade(STONE_BLOCK_ID)},
        ),),
    )

    if len(cases) != EXPECTED_M1_CASE_COUNT + EXPECTED_M2_CASE_COUNT:
        raise ValueError("M2 regression case construction changed unexpectedly")

    def add_m3(
        label: str,
        category: str,
        anchors: Iterable[dict[str, Any]],
    ) -> None:
        case_number = (
            len(cases) - EXPECTED_M1_CASE_COUNT - EXPECTED_M2_CASE_COUNT + 1
        )
        cases.append(
            {
                "case_id": f"ae2-m3-{case_number:02d}",
                "milestone": "M3a",
                "route": DRIVE_BLOCK_ID,
                "label": label,
                "category": category,
                "anchors": tuple(anchors),
                "fixture_blocks": (),
            }
        )

    # Six rows provide the exact full-orientation state closure: every facing
    # value with all four persisted spins. Each drive has one ordinary cell in
    # slot 0 and remains disconnected from any AE2 network.
    for facing_index, facing in enumerate(DIRECTION_DELTAS):
        add_m3(
            f"drive-facing-{facing}-spins-zero-through-three",
            "drive-orientation",
            tuple(
                drive_anchor(
                    (242 + spin * 6, 100, 242 + facing_index),
                    facing,
                    spin,
                    {0: drive_item("ae2:item_storage_cell_1k")},
                )
                for spin in range(4)
            ),
        )

    primary_ids = tuple(DRIVE_EXPLICIT_CELL_MODELS)[:10]
    portable_ids = tuple(
        item_id
        for item_id in DRIVE_EXPLICIT_CELL_MODELS
        if item_id.startswith("ae2:portable_")
    )
    special_ids = (
        "ae2:creative_storage_cell",
        *DRIVE_GENERIC_CELL_IDS,
    )

    add_m3(
        "drive-empty",
        "drive-empty",
        (drive_anchor((241, 100, 248), "south", 0, {}),),
    )
    add_m3(
        "drive-primary-cell-catalog",
        "drive-cell-catalog",
        (
            drive_anchor(
                (244, 100, 248),
                "south",
                0,
                {slot: drive_item(item_id) for slot, item_id in enumerate(primary_ids)},
            ),
        ),
    )
    add_m3(
        "drive-portable-cell-catalog",
        "drive-cell-catalog",
        (
            drive_anchor(
                (247, 100, 248),
                "south",
                0,
                {slot: drive_item(item_id) for slot, item_id in enumerate(portable_ids)},
            ),
        ),
    )
    add_m3(
        "drive-special-cell-catalog",
        "drive-cell-catalog",
        (
            drive_anchor(
                (250, 100, 248),
                "south",
                0,
                {slot: drive_item(item_id) for slot, item_id in enumerate(special_ids)},
            ),
        ),
    )
    add_m3(
        "drive-sparse-first-last-slots",
        "drive-slot-translation",
        (
            drive_anchor(
                (253, 100, 248),
                "south",
                0,
                {
                    0: drive_item("ae2:item_storage_cell_1k"),
                    9: drive_item("ae2:fluid_storage_cell_256k"),
                },
            ),
        ),
    )
    add_m3(
        "drive-full-mixed-slots",
        "drive-slot-translation",
        (
            drive_anchor(
                (256, 100, 248),
                "south",
                0,
                {
                    0: drive_item("ae2:item_storage_cell_1k"),
                    1: drive_item("ae2:fluid_storage_cell_1k"),
                    2: drive_item("ae2:item_storage_cell_4k"),
                    3: drive_item("ae2:fluid_storage_cell_4k"),
                    4: drive_item("ae2:item_storage_cell_16k"),
                    5: drive_item("ae2:fluid_storage_cell_16k"),
                    6: drive_item("ae2:creative_storage_cell"),
                    7: drive_item("ae2:matter_cannon"),
                    8: drive_item("ae2:color_applicator"),
                    9: drive_item("ae2:portable_item_cell_256k"),
                },
            ),
        ),
    )
    add_m3(
        "drive-components-insensitive",
        "drive-component-insensitivity",
        (
            drive_anchor(
                (259, 100, 248),
                "south",
                0,
                {0: drive_item("ae2:item_storage_cell_1k")},
            ),
            drive_anchor(
                (262, 100, 248),
                "south",
                0,
                {
                    0: drive_item(
                        "ae2:item_storage_cell_1k",
                        components=DRIVE_COMPONENT_INSENSITIVITY,
                    )
                },
            ),
        ),
    )
    add_m3(
        "drive-unknown-extension-cell-fallback",
        "atomic-fallback",
        (
            drive_anchor(
                (241, 100, 249),
                "south",
                0,
                {0: drive_item(DRIVE_FALLBACK_CELL_ID)},
                expected_path="stock-fallback-m3",
                fallback_reason="unsupported-drive-cell-id",
            ),
        ),
    )

    if len(cases) != (
        EXPECTED_M1_CASE_COUNT + EXPECTED_M2_CASE_COUNT + EXPECTED_M3_CASE_COUNT
    ):
        raise ValueError("M3a case construction changed unexpectedly")

    def add_m3b(
        label: str,
        category: str,
        anchors: Iterable[dict[str, Any]],
    ) -> None:
        case_number = len(cases) - (
            EXPECTED_M1_CASE_COUNT + EXPECTED_M2_CASE_COUNT + EXPECTED_M3_CASE_COUNT
        ) + 1
        cases.append(
            {
                "case_id": f"ae2-m3b-{case_number:02d}",
                "milestone": "M3b",
                "route": EXTENDED_DRIVE_BLOCK_ID,
                "label": label,
                "category": category,
                "anchors": tuple(anchors),
                "fixture_blocks": (),
            }
        )

    # Every full block orientation is exercised with a distinct front/back
    # cell pair. Slots 0..9 use the block orientation; slots 10..19 use the
    # opposite facing with the same spin, exactly matching ExtendedAE.
    for facing_index, facing in enumerate(DIRECTION_DELTAS):
        add_m3b(
            f"extended-drive-facing-{facing}-spins-zero-through-three",
            "extended-drive-orientation",
            tuple(
                extended_drive_anchor(
                    (242 + spin * 6, 100, 260 + facing_index),
                    facing,
                    spin,
                    {
                        0: drive_item("ae2:portable_fluid_cell_16k"),
                        10: drive_item("extendedae:infinity_water_cell"),
                    },
                )
                for spin in range(4)
            ),
        )

    add_m3b(
        "extended-drive-empty",
        "extended-drive-empty",
        (extended_drive_anchor((242, 100, 266), "south", 0, {}),),
    )
    add_m3b(
        "extended-drive-native-cells-front-and-back",
        "extended-drive-native-cell-catalog",
        (
            extended_drive_anchor(
                (245, 100, 266),
                "south",
                0,
                {
                    0: drive_item("extendedae:infinity_water_cell"),
                    1: drive_item("extendedae:infinity_cobblestone_cell"),
                    2: drive_item("extendedae:void_cell"),
                    10: drive_item("extendedae:infinity_water_cell"),
                    11: drive_item("extendedae:infinity_cobblestone_cell"),
                    12: drive_item("extendedae:void_cell"),
                },
            ),
        ),
    )
    add_m3b(
        "extended-drive-sparse-front-back-edge-slots",
        "extended-drive-slot-translation",
        (
            extended_drive_anchor(
                (248, 100, 266),
                "south",
                0,
                {
                    0: drive_item("ae2:portable_fluid_cell_64k"),
                    9: drive_item("ae2:portable_fluid_cell_256k"),
                    10: drive_item("extendedae:infinity_cobblestone_cell"),
                    19: drive_item("extendedae:void_cell"),
                },
            ),
        ),
    )
    full_twenty_ids = (
        "ae2:item_storage_cell_1k",
        "ae2:item_storage_cell_4k",
        "ae2:item_storage_cell_16k",
        "ae2:item_storage_cell_64k",
        "ae2:item_storage_cell_256k",
        "ae2:fluid_storage_cell_1k",
        "ae2:fluid_storage_cell_4k",
        "ae2:fluid_storage_cell_16k",
        "ae2:fluid_storage_cell_64k",
        "ae2:fluid_storage_cell_256k",
        "ae2:portable_item_cell_1k",
        "ae2:portable_item_cell_4k",
        "ae2:portable_item_cell_16k",
        "ae2:portable_item_cell_64k",
        "ae2:portable_item_cell_256k",
        "ae2:portable_fluid_cell_1k",
        "ae2:portable_fluid_cell_4k",
        "ae2:creative_storage_cell",
        "ae2:matter_cannon",
        "ae2:color_applicator",
    )
    add_m3b(
        "extended-drive-full-twenty-slots",
        "extended-drive-full-capacity",
        (
            extended_drive_anchor(
                (251, 100, 266),
                "south",
                0,
                {
                    slot: drive_item(item_id)
                    for slot, item_id in enumerate(full_twenty_ids)
                },
            ),
        ),
    )
    add_m3b(
        "extended-drive-front-back-mirror",
        "extended-drive-front-back-mirror",
        (
            extended_drive_anchor(
                (254, 100, 266),
                "south",
                0,
                {0: drive_item("extendedae:void_cell")},
            ),
            extended_drive_anchor(
                (257, 100, 266),
                "south",
                0,
                {10: drive_item("extendedae:void_cell")},
            ),
        ),
    )
    component_slots = {
        0: drive_item("extendedae:infinity_water_cell"),
        10: drive_item("ae2:item_storage_cell_1k"),
    }
    component_slots_with_data = {
        0: drive_item(
            "extendedae:infinity_water_cell",
            components=DRIVE_COMPONENT_INSENSITIVITY,
        ),
        10: drive_item("ae2:item_storage_cell_1k"),
    }
    add_m3b(
        "extended-drive-components-insensitive",
        "extended-drive-component-insensitivity",
        (
            extended_drive_anchor(
                (260, 100, 266), "south", 0, component_slots
            ),
            extended_drive_anchor(
                (263, 100, 266), "south", 0, component_slots_with_data
            ),
        ),
    )
    for position_x, label, item_id, count, reason in (
        (
            266,
            "extended-drive-megacells-fallback",
            "megacells:item_storage_cell_1m",
            1,
            "unsupported-drive-cell-id",
        ),
        (
            269,
            "extended-drive-kubejs-cell-fallback",
            "kubejs:lava_cell",
            1,
            "unsupported-drive-cell-id",
        ),
        (
            272,
            "extended-drive-count-two-fallback",
            "ae2:item_storage_cell_1k",
            2,
            "invalid-drive-cell-count",
        ),
        (
            275,
            "extended-drive-non-cell-item-fallback",
            "minecraft:stone",
            1,
            "non-cell-drive-item",
        ),
    ):
        add_m3b(
            label,
            "atomic-fallback",
            (
                extended_drive_anchor(
                    (position_x, 100, 266),
                    "south",
                    0,
                    {0: drive_item(item_id, count=count)},
                    expected_path="stock-fallback-m3b",
                    fallback_reason=reason,
                ),
            ),
        )

    if len(cases) != (
        EXPECTED_M1_CASE_COUNT
        + EXPECTED_M2_CASE_COUNT
        + EXPECTED_M3_CASE_COUNT
        + EXPECTED_M3B_CASE_COUNT
    ):
        raise ValueError("M3b case construction changed unexpectedly")

    def add_m3c(
        label: str,
        category: str,
        anchors: Iterable[dict[str, Any]],
        fixture_blocks: Iterable[dict[str, Any]] = (),
    ) -> None:
        case_number = len(cases) - (
            EXPECTED_M1_CASE_COUNT
            + EXPECTED_M2_CASE_COUNT
            + EXPECTED_M3_CASE_COUNT
            + EXPECTED_M3B_CASE_COUNT
        ) + 1
        cases.append(
            {
                "case_id": f"ae2-m3c-{case_number:02d}",
                "milestone": "M3c",
                "route": CONNECTED_GLASS_ROUTE,
                "label": label,
                "category": category,
                "anchors": tuple(anchors),
                "fixture_blocks": tuple(fixture_blocks),
            }
        )

    ordinary, vibrant = CONNECTED_GLASS_BLOCK_IDS
    add_m3c(
        "isolated-ordinary-vibrant-matched-selection",
        "glass-variant-equivalence",
        (
            connected_glass_anchor((208, 100, 288), ordinary),
            connected_glass_anchor((244, 100, 288), vibrant),
        ),
    )
    add_m3c(
        "center-down-up",
        "glass-axis-topology",
        (
            connected_glass_anchor((214, 100, 290), ordinary),
            connected_glass_anchor((214, 99, 290), vibrant),
            connected_glass_anchor((214, 101, 290), ordinary),
        ),
    )
    add_m3c(
        "center-north-up",
        "glass-corner-topology",
        (
            connected_glass_anchor((222, 100, 290), ordinary),
            connected_glass_anchor((222, 100, 289), vibrant),
            connected_glass_anchor((222, 101, 290), ordinary),
        ),
    )
    add_m3c(
        "center-north-west",
        "glass-corner-topology",
        (
            connected_glass_anchor((230, 100, 290), ordinary),
            connected_glass_anchor((230, 100, 289), vibrant),
            connected_glass_anchor((229, 100, 290), ordinary),
        ),
    )
    add_m3c(
        "center-north-south-west",
        "glass-t-topology",
        (
            connected_glass_anchor((238, 100, 290), ordinary),
            connected_glass_anchor((238, 100, 289), vibrant),
            connected_glass_anchor((238, 100, 291), ordinary),
            connected_glass_anchor((237, 100, 290), vibrant),
        ),
    )
    add_m3c(
        "center-east-north-up-west",
        "glass-four-arm-topology",
        (
            connected_glass_anchor((250, 100, 290), ordinary),
            connected_glass_anchor((251, 100, 290), vibrant),
            connected_glass_anchor((250, 100, 289), ordinary),
            connected_glass_anchor((250, 101, 290), vibrant),
            connected_glass_anchor((249, 100, 290), ordinary),
        ),
    )
    add_m3c(
        "diagonal-only-ordinary-vibrant",
        "glass-diagonal-nonconnection",
        (
            connected_glass_anchor((258, 100, 290), ordinary),
            connected_glass_anchor((259, 101, 290), vibrant),
        ),
    )
    add_m3c(
        "three-by-three-checkerboard-plane",
        "glass-checkerboard-plane",
        tuple(
            connected_glass_anchor(
                (263 + offset_x, 100, 289 + offset_z),
                CONNECTED_GLASS_BLOCK_IDS[(offset_x + offset_z) % 2],
            )
            for offset_z in range(3)
            for offset_x in range(3)
        ),
    )
    add_m3c(
        "two-by-two-by-two-checkerboard-cube",
        "glass-checkerboard-cube",
        tuple(
            connected_glass_anchor(
                (272 + offset_x, 100 + offset_y, 289 + offset_z),
                CONNECTED_GLASS_BLOCK_IDS[
                    (offset_x + offset_y + offset_z) % 2
                ],
            )
            for offset_y in range(2)
            for offset_z in range(2)
            for offset_x in range(2)
        ),
    )
    add_m3c(
        "mixed-six-neighbor-enclosed-plus",
        "glass-enclosed-plus",
        (
            connected_glass_anchor((215, 101, 301), ordinary),
            connected_glass_anchor((215, 100, 301), vibrant),
            connected_glass_anchor((215, 102, 301), ordinary),
            connected_glass_anchor((215, 101, 300), vibrant),
            connected_glass_anchor((215, 101, 302), ordinary),
            connected_glass_anchor((214, 101, 301), vibrant),
            connected_glass_anchor((216, 101, 301), ordinary),
        ),
    )
    add_m3c(
        "opaque-neighbor-culling",
        "glass-opaque-neighbor-culling",
        (connected_glass_anchor((226, 100, 301), ordinary),),
        ({"position": (227, 100, 301), "block_id": STONE_BLOCK_ID},),
    )

    if len(cases) != (
        EXPECTED_CASE_COUNT
        - EXPECTED_M3D_CASE_COUNT
        - EXPECTED_M3E_CASE_COUNT
        - EXPECTED_M3F_CASE_COUNT
        - EXPECTED_S1_CASE_COUNT
    ):
        raise ValueError("M3c frozen case construction changed unexpectedly")

    def add_m3d(
        label: str,
        category: str,
        anchors: Iterable[dict[str, Any]],
        fixture_blocks: Iterable[dict[str, Any]] = (),
    ) -> None:
        case_number = len(cases) - (
            EXPECTED_M1_CASE_COUNT
            + EXPECTED_M2_CASE_COUNT
            + EXPECTED_M3_CASE_COUNT
            + EXPECTED_M3B_CASE_COUNT
            + EXPECTED_M3C_CASE_COUNT
        ) + 1
        cases.append(
            {
                "case_id": f"ae2-m3d-{case_number:02d}",
                "milestone": "M3d",
                "route": CRAFTING_ROUTE,
                "label": label,
                "category": category,
                "anchors": tuple(anchors),
                "fixture_blocks": tuple(fixture_blocks),
            }
        )

    add_m3d(
        "isolated-storage-catalog",
        "crafting-storage-catalog",
        tuple(
            crafting_anchor((x, 100, 261), block_id)
            for x, block_id in zip(
                (297, 301, 305, 309, 313),
                CRAFTING_STORAGE_BLOCK_IDS,
                strict=True,
            )
        ),
    )
    add_m3d(
        "unit-plus-1k-storage",
        "crafting-axis-pair",
        (
            crafting_anchor((297, 100, 265), "ae2:crafting_unit"),
            crafting_anchor((298, 100, 265), "ae2:1k_crafting_storage"),
        ),
    )
    add_m3d(
        "accelerator-plus-1k-storage",
        "crafting-axis-pair",
        (
            crafting_anchor((302, 100, 265), "ae2:crafting_accelerator"),
            crafting_anchor((303, 100, 265), "ae2:1k_crafting_storage"),
        ),
    )
    add_m3d(
        "unit-storage-accelerator-line",
        "crafting-axis-line",
        (
            crafting_anchor((307, 100, 265), "ae2:crafting_unit"),
            crafting_anchor((308, 100, 265), "ae2:1k_crafting_storage"),
            crafting_anchor((309, 100, 265), "ae2:crafting_accelerator"),
        ),
    )
    add_m3d(
        "two-by-two-plane",
        "crafting-plane",
        (
            crafting_anchor((312, 100, 264), "ae2:crafting_unit"),
            crafting_anchor((313, 100, 264), "ae2:crafting_accelerator"),
            crafting_anchor((312, 100, 265), "ae2:1k_crafting_storage"),
            crafting_anchor((313, 100, 265), "ae2:4k_crafting_storage"),
        ),
    )
    powered_cube_blocks = (
        ((297, 100, 269), "ae2:crafting_unit"),
        ((298, 100, 269), "ae2:crafting_accelerator"),
        ((297, 100, 270), "ae2:1k_crafting_storage"),
        ((298, 100, 270), "ae2:4k_crafting_storage"),
        ((297, 101, 269), "ae2:16k_crafting_storage"),
        ((298, 101, 269), "ae2:64k_crafting_storage"),
        ((297, 101, 270), "ae2:256k_crafting_storage"),
    )
    add_m3d(
        "powered-two-by-two-by-two-all-eight",
        "crafting-powered-cube",
        (
            *(
                crafting_anchor(position, block_id, powered=True)
                for position, block_id in powered_cube_blocks
            ),
            crafting_anchor(
                (298, 101, 270),
                CRAFTING_MONITOR_BLOCK_ID,
                powered=True,
                facing="up",
                spin=0,
                painted_color_ordinal=16,
            ),
        ),
        (
            {
                "position": (297, 98, 269),
                "block_id": "ae2:creative_energy_cell",
                "purpose": "real-grid-power-source",
            },
            {
                "position": (297, 99, 269),
                "block_id": "ae2:cable_bus",
                "block_entity_snbt": {
                    "hasRedstone": 2,
                    "cable": {"id": "ae2:fluix_glass_cable"},
                },
                "purpose": "real-grid-power-link",
            },
        ),
    )
    hard_cube_anchors: list[dict[str, Any]] = []
    for y in range(100, 103):
        for z in range(269, 272):
            for x in range(304, 307):
                block_id = (
                    "ae2:1k_crafting_storage"
                    if (x, y, z) == (305, 102, 269)
                    else "ae2:crafting_unit"
                )
                hard_cube_anchors.append(crafting_anchor((x, y, z), block_id))
    add_m3d(
        "unpowered-three-by-three-by-three-hard-culling",
        "crafting-hard-culling-cube",
        hard_cube_anchors,
    )

    monitor_positions = tuple(
        (x, 100, z)
        for z in (276, 281, 286, 291, 296)
        for x in (298, 303, 308, 313)
    )[:17]
    monitor_facings = (
        "south", "north", "east", "west", "up", "down",
        "south", "north", "east", "west", "up", "down",
        "south", "north", "east", "west", "up",
    )
    monitor_anchors: list[dict[str, Any]] = []
    for ordinal, (position, facing) in enumerate(
        zip(monitor_positions, monitor_facings, strict=True)
    ):
        monitor_anchors.append(
            crafting_anchor(
                position,
                CRAFTING_MONITOR_BLOCK_ID,
                facing=facing,
                spin=ordinal % 4,
                painted_color_ordinal=ordinal,
            )
        )
        back = DIRECTION_DELTAS[OPPOSITES[facing]]
        storage_position = tuple(
            position[index] + back[index] for index in range(3)
        )
        monitor_anchors.append(
            crafting_anchor(storage_position, "ae2:1k_crafting_storage")
        )
    add_m3d(
        "monitor-paint-orientation-catalog",
        "crafting-monitor-catalog",
        monitor_anchors,
    )
    mixed_anchor = crafting_anchor(
        (318, 100, 261),
        "ae2:1k_crafting_storage",
        expected_path="stock-fallback-m3d",
        fallback_reason="compatible-extension-crafting-neighbor",
    )
    mixed_anchor["compatible_neighbor_block_ids"] = [
        "megacells:mega_crafting_unit",
        "expandedae:exp_crafting_unit",
    ]
    add_m3d(
        "compatible-extension-atomic-fallback",
        "crafting-compatible-extension-fallback",
        (mixed_anchor,),
        tuple(
            {
                "position": position,
                "block_id": block_id,
                "expected_block_entity_id": block_entity_id,
                "expected_state": {"formed": True, "powered": False},
                "purpose": "compatible-crafting-neighbor-context",
            }
            for position, (block_id, block_entity_id) in zip(
                ((317, 100, 261), (319, 100, 261)),
                CRAFTING_COMPATIBLE_EXTENSION_CONTEXT.items(),
                strict=True,
            )
        ),
    )

    if len(cases) != (
        EXPECTED_CASE_COUNT
        - EXPECTED_M3E_CASE_COUNT
        - EXPECTED_M3F_CASE_COUNT
        - EXPECTED_S1_CASE_COUNT
    ):
        raise ValueError("M3d case construction changed unexpectedly")

    def add_m3e(
        label: str,
        plane: str,
        anchors: Iterable[dict[str, Any]],
    ) -> None:
        case_number = len(cases) - (
            EXPECTED_M1_CASE_COUNT
            + EXPECTED_M2_CASE_COUNT
            + EXPECTED_M3_CASE_COUNT
            + EXPECTED_M3B_CASE_COUNT
            + EXPECTED_M3C_CASE_COUNT
            + EXPECTED_M3D_CASE_COUNT
        ) + 1
        cases.append(
            {
                "case_id": f"ae2-m3e-{case_number:02d}",
                "milestone": "M3e",
                "route": QUANTUM_ROUTE,
                "label": label,
                "category": "quantum-bridge-plane-orientation",
                "anchors": tuple(anchors),
                "fixture_blocks": (),
            }
        )

    def bridge_plane(
        center: tuple[int, int, int], plane: str
    ) -> tuple[dict[str, Any], ...]:
        axes = {"xz": (0, 2), "xy": (0, 1), "yz": (1, 2)}[plane]
        anchors: list[dict[str, Any]] = []
        for second_offset in (-1, 0, 1):
            for first_offset in (-1, 0, 1):
                position = list(center)
                position[axes[0]] += first_offset
                position[axes[1]] += second_offset
                role = (
                    "link"
                    if first_offset == second_offset == 0
                    else "corner"
                    if first_offset != 0 and second_offset != 0
                    else "edge"
                )
                anchors.append(quantum_anchor(tuple(position), plane, role))
        return tuple(anchors)

    # The XZ bridge intentionally crosses the x=287/288 chunk boundary. The
    # other two planes remain disjoint within the same bounded air volume.
    add_m3e(
        "formed-unpowered-xz-chunk-boundary",
        "xz",
        bridge_plane((287, 100, 271), "xz"),
    )
    add_m3e(
        "formed-unpowered-xy",
        "xy",
        bridge_plane((283, 101, 276), "xy"),
    )
    add_m3e(
        "formed-unpowered-yz",
        "yz",
        bridge_plane((290, 101, 271), "yz"),
    )

    if len(cases) != (
        EXPECTED_CASE_COUNT - EXPECTED_M3F_CASE_COUNT - EXPECTED_S1_CASE_COUNT
    ):
        raise ValueError("M3e case construction changed unexpectedly")

    def add_m3f(
        label: str,
        category: str,
        anchors: Iterable[dict[str, Any]],
        fixture_blocks: Iterable[dict[str, Any]] = (),
    ) -> None:
        case_number = len(cases) - (
            EXPECTED_M1_CASE_COUNT
            + EXPECTED_M2_CASE_COUNT
            + EXPECTED_M3_CASE_COUNT
            + EXPECTED_M3B_CASE_COUNT
            + EXPECTED_M3C_CASE_COUNT
            + EXPECTED_M3D_CASE_COUNT
            + EXPECTED_M3E_CASE_COUNT
        ) + 1
        cases.append(
            {
                "case_id": f"ae2-m3f-{case_number:02d}",
                "milestone": "M3f",
                "route": M3_COMPLETION_ROUTE,
                "label": label,
                "category": category,
                "anchors": tuple(anchors),
                "fixture_blocks": tuple(fixture_blocks),
            }
        )

    paint_anchors: list[dict[str, Any]] = []
    paint_supports: list[dict[str, Any]] = []
    palette_positions = tuple(
        (282 + column * 4, 100, 209 + row * 4)
        for row in range(2)
        for column in range(8)
    )
    for color_ordinal, position in enumerate(palette_positions):
        paint_anchors.append(
            paint_anchor(position, (paint_splotch(-120, "down", color_ordinal),))
        )
        paint_supports.append(
            {
                "position": (position[0], position[1] - 1, position[2]),
                "block_id": "minecraft:smooth_stone",
                "purpose": "paint-backing-support",
            }
        )
    direction_positions = tuple((282 + index * 6, 100, 217) for index in range(6))
    for color_ordinal, (position, backing_side) in enumerate(
        zip(direction_positions, PAINT_DIRECTION_ORDINALS, strict=True)
    ):
        paint_anchors.append(
            paint_anchor(
                position,
                (paint_splotch(-87 + color_ordinal, backing_side, color_ordinal),),
            )
        )
        delta = DIRECTION_DELTAS[backing_side]
        paint_supports.append(
            {
                "position": tuple(position[index] + delta[index] for index in range(3)),
                "block_id": "minecraft:smooth_stone",
                "purpose": "paint-backing-support",
            }
        )
    layered_position = (318, 100, 217)
    paint_anchors.append(
        paint_anchor(
            layered_position,
            (
                paint_splotch(-120, "down", 4),
                paint_splotch(-103, "down", 10),
                paint_splotch(-86, "down", 15),
            ),
        )
    )
    paint_supports.append(
        {
            "position": (318, 99, 217),
            "block_id": "minecraft:smooth_stone",
            "purpose": "paint-backing-support",
        }
    )
    add_m3f(
        "non-lumen-palette-faces-and-layering",
        "paint-persisted-splotch-matrix",
        paint_anchors,
        paint_supports,
    )

    horizontal_facings = ("south", "west", "north", "east")
    add_m3f(
        "closed-sky-stone-chest-facings",
        "sky-stone-chest-static-closed",
        tuple(
            m3_completion_machine_anchor(
                (282 + facing_index * 6, 100, 222 + variant_index * 4),
                block_id,
                {"facing": facing, "waterlogged": False},
                facing=facing,
            )
            for variant_index, block_id in enumerate(
                (SKY_STONE_CHEST_BLOCK_ID, SMOOTH_SKY_STONE_CHEST_BLOCK_ID)
            )
            for facing_index, facing in enumerate(horizontal_facings)
        ),
    )

    crank_specs = (
        ((306, 100, 222), "down"),
        ((312, 100, 222), "up"),
        ((318, 100, 222), "north"),
        ((306, 100, 226), "south"),
        ((312, 100, 226), "west"),
        ((318, 100, 226), "east"),
    )
    crank_supports: list[dict[str, Any]] = []
    for position, facing in crank_specs:
        attachment = DIRECTION_DELTAS[OPPOSITES[facing]]
        charger_facing = "north" if facing in {"down", "up", "west", "east"} else "east"
        crank_supports.append(
            {
                "position": tuple(position[index] + attachment[index] for index in range(3)),
                "block_id": "ae2:charger",
                "expected_state": {"facing": charger_facing, "spin": 0},
                "expected_block_entity_id": "ae2:charger",
                "purpose": "persistent-crankable-context",
            }
        )
    add_m3f(
        "neutral-crank-six-facings",
        "crank-static-neutral-orientation",
        tuple(
            m3_completion_machine_anchor(
                position,
                CRANK_BLOCK_ID,
                {"facing": facing},
                facing=facing,
            )
            for position, facing in crank_specs
        ),
        crank_supports,
    )

    inscriber_positions = tuple(
        (282 + column * 5, 98 + layer * 4, 229)
        for layer in range(3)
        for column in range(8)
    )
    inscriber_states = tuple(
        (facing, spin)
        for facing in PAINT_DIRECTION_ORDINALS
        for spin in range(4)
    )
    add_m3f(
        "neutral-inscriber-all-facing-spin-states",
        "inscriber-static-neutral-orientation",
        tuple(
            m3_completion_machine_anchor(
                position,
                INSCRIBER_BLOCK_ID,
                {"facing": facing, "spin": spin, "waterlogged": False},
                facing=facing,
                spin=spin,
            )
            for position, (facing, spin) in zip(
                inscriber_positions, inscriber_states, strict=True
            )
        ),
    )

    pylon_supported = (
        spatial_pylon_anchor((282, 104, 208), axis="x", axis_position="none"),
        spatial_pylon_anchor((286, 104, 208), axis="x", axis_position="start"),
        spatial_pylon_anchor((287, 104, 208), axis="x", axis_position="middle"),
        spatial_pylon_anchor((288, 104, 208), axis="x", axis_position="end"),
        spatial_pylon_anchor((294, 102, 208), axis="y", axis_position="start"),
        spatial_pylon_anchor((294, 103, 208), axis="y", axis_position="middle"),
        spatial_pylon_anchor((294, 104, 208), axis="y", axis_position="end"),
        spatial_pylon_anchor((300, 104, 208), axis="z", axis_position="start"),
        spatial_pylon_anchor((300, 104, 209), axis="z", axis_position="middle"),
        spatial_pylon_anchor((300, 104, 210), axis="z", axis_position="end"),
    )
    add_m3f(
        "spatial-pylon-isolated-and-three-axis-lines",
        "spatial-pylon-local-static-offline",
        pylon_supported,
    )

    add_m3f(
        "spatial-pylon-perpendicular-component-unformed",
        "spatial-pylon-invalid-component-unformed",
        tuple(
            spatial_pylon_anchor(position, axis="x", axis_position="none")
            for position in (
                (310, 104, 214),
                (311, 104, 214),
                (310, 104, 215),
            )
        ),
    )
    add_m3f(
        "spatial-pylon-branched-component-unformed",
        "spatial-pylon-invalid-component-unformed",
        tuple(
            spatial_pylon_anchor(position, axis="x", axis_position="none")
            for position in (
                (316, 103, 214),
                (315, 103, 214),
                (317, 103, 214),
                (316, 104, 214),
            )
        ),
    )

    if len(cases) != EXPECTED_CASE_COUNT - EXPECTED_S1_CASE_COUNT:
        raise ValueError("M3f case construction changed unexpectedly")
    cases.extend(create_s1_cases())
    if len(cases) != EXPECTED_CASE_COUNT:
        raise ValueError("S1 case construction changed unexpectedly")
    return cases


def is_custom(anchor: dict[str, Any]) -> bool:
    return anchor["expected_path"] in (
        "custom-m1",
        "custom-m2",
        "custom-m3",
        "custom-m3b",
        "custom-m3c",
        "custom-m3d",
        "custom-m3e",
        "custom-m3f",
        "custom-s1",
    )


def expected_drive_geometry(
    anchor: dict[str, Any],
) -> tuple[dict[str, int], dict[str, Any], dict[str, Any]]:
    contract = drive_contract(anchor)
    materials: Counter[str] = Counter(contract["base_materials"])
    # Preserve the accepted schema-4 M3a manifest shape: the empty Drive still
    # declares its selected cell-atlas material with a zero triangle count.
    if anchor["block_id"] == DRIVE_BLOCK_ID:
        materials[DRIVE_CELL_TEXTURE] += 0
    slot_models: list[dict[str, Any]] = []
    occupied_count = 0
    for slot, item in enumerate(anchor["drive_inventory"]):
        face_slot = slot % contract["face_slot_count"]
        origin = drive_slot_origin_sixteenths(face_slot)
        if item is None:
            model_id = contract["empty_cell_model"]
        else:
            item_id = item["id"]
            model_id = contract["cell_models"].get(item_id)
            if model_id is None:
                raise ValueError(
                    f"custom drive contains unsupported item: {item_id}"
                )
            if item.get("count") != 1:
                raise ValueError("custom drive cell count must be exactly one")
            occupied_count += 1
            materials[contract["model_materials"][model_id]] += (
                DRIVE_CELL_CHASSIS_TRIANGLE_COUNT
            )
        slot_model = {
            "slot": slot,
            "model_id": model_id,
            "slot_origin_sixteenths": list(origin),
            "slot_origin": {
                "x": origin[0] / 16,
                "y": origin[1] / 16,
                "z": origin[2] / 16,
            },
        }
        if anchor["block_id"] == EXTENDED_DRIVE_BLOCK_ID:
            side = "front" if slot < EXTENDED_DRIVE_FACE_SLOT_COUNT else "back"
            facing = anchor["block_state"]["facing"]
            slot_model.update(
                {
                    "face": side,
                    "face_slot": face_slot,
                    "orientation": {
                        "facing": facing if side == "front" else OPPOSITES[facing],
                        "spin": anchor["block_state"]["spin"],
                    },
                    "material": (
                        contract["model_materials"].get(model_id)
                        if item is not None
                        else None
                    ),
                }
            )
        slot_models.append(slot_model)
    materials[DRIVE_LED_TEXTURE] += occupied_count * DRIVE_LED_TRIANGLE_COUNT
    expected_models = {
        "base_model_id": contract["base_model"],
        "empty_cell_model_id": contract["empty_cell_model"],
        "slots": slot_models,
    }
    if anchor["block_id"] == EXTENDED_DRIVE_BLOCK_ID:
        expected_models.update(
            {
                "front_slot_count": EXTENDED_DRIVE_FACE_SLOT_COUNT,
                "back_slot_count": EXTENDED_DRIVE_FACE_SLOT_COUNT,
                "back_orientation_policy": "opposite-facing-same-spin",
            }
        )
    expected_led = {
        "policy": DRIVE_LED_POLICY,
        "material": DRIVE_LED_TEXTURE,
        "rgb_u8": [0, 0, 0],
        "blocklight_raw_i8": 15,
        "sunlight_raw_i8": 15,
        "ambient_occlusion_f32": 1.0,
        "triangle_count_per_occupied_slot": DRIVE_LED_TRIANGLE_COUNT,
        "triangle_count": occupied_count * DRIVE_LED_TRIANGLE_COUNT,
    }
    return dict(sorted(materials.items())), expected_models, expected_led


def expected_geometry(
    anchor: dict[str, Any], cable_positions: dict[tuple[int, int, int], dict[str, Any]]
) -> tuple[
    list[dict[str, str]],
    dict[str, int],
    dict[str, dict[str, Any]],
    dict[str, dict[str, Any]],
]:
    family, color_prefix, texture_name = decode_cable_id(anchor["cable_id"])
    connections: list[dict[str, str]] = []
    for direction, delta in DIRECTION_DELTAS.items():
        adjacent_position = tuple(
            anchor["position"][index] + delta[index] for index in range(3)
        )
        adjacent = cable_positions.get(adjacent_position)
        if adjacent is None or not is_custom(adjacent):
            continue
        adjacent_family, adjacent_color, _ = decode_cable_id(adjacent["cable_id"])
        if not colors_connect(color_prefix, adjacent_color):
            continue
        effective = minimum_family(family["key"], adjacent_family["key"])
        connections.append(
            {
                "direction": direction,
                "effective_family": effective,
                "visible_family": visible_family(family["key"], effective),
            }
        )

    materials: Counter[str] = Counter()
    is_straight = (
        not anchor["face_parts"]
        and len(connections) == 2
        and connections[0]["direction"] == OPPOSITES[connections[1]["direction"]]
        and all(
            connection["effective_family"] == family["key"]
            for connection in connections
        )
    )
    if is_straight:
        base_triangles = 8 if family["key"] == "glass" else 12
        materials[f"ae2:part/cable/{family['connection']}/{texture_name}"] += base_triangles
        if family["variant"] == "smart":
            materials[f"ae2:part/cable/{family['connection']}/channels_00"] += base_triangles
            materials[f"ae2:part/cable/{family['connection']}/channels_10"] += base_triangles
    else:
        materials[f"ae2:part/cable/core/{family['core']}/{texture_name}"] += 12
        for connection in connections:
            visible = FAMILY_BY_KEY[connection["visible_family"]]
            materials[f"ae2:part/cable/{visible['connection']}/{texture_name}"] += 10
            if visible["variant"] == "smart":
                materials[f"ae2:part/cable/{visible['connection']}/channels_00"] += 10
                materials[f"ae2:part/cable/{visible['connection']}/channels_10"] += 10
        for _direction, part in anchor["face_parts"].items():
            if (
                part.get("id") != TERMINAL_PART_ID
                or set(part) != {"id", "spin"}
                or part["spin"] not in (0, 1, 2, 3)
            ):
                raise ValueError("custom M2 geometry contains an unsupported face part")
            materials[
                f"ae2:part/cable/{family['connection']}/{texture_name}"
            ] += 12
            if family["variant"] == "smart":
                materials[f"ae2:part/cable/{family['connection']}/channels_00"] += 12
                materials[f"ae2:part/cable/{family['connection']}/channels_10"] += 12
            materials.update(TERMINAL_MATERIAL_TRIANGLES)
        for _direction, block_state in anchor["facades"].items():
            if block_state != {"Name": STONE_BLOCK_ID}:
                raise ValueError("custom M2 geometry contains an unsupported facade")
            materials[STONE_TEXTURE] += 48

    overlays = {}
    dark_rgb, bright_rgb = COLOR_TINTS[color_prefix]
    for resource_path in materials:
        if resource_path.endswith("/channels_00"):
            overlays[resource_path] = {
                "rgb_u8": dark_rgb,
                "blocklight_raw_i8": 15,
                "sunlight_raw_i8": 15,
            }
        elif resource_path.endswith("/channels_10"):
            overlays[resource_path] = {
                "rgb_u8": bright_rgb,
                "blocklight_raw_i8": 15,
                "sunlight_raw_i8": 15,
            }
    terminal_layers = {}
    if anchor["face_parts"]:
        dark_rgb, bright_rgb = COLOR_TINTS[color_prefix]
        for resource_path, rgb in (
            ("ae2:part/terminal_bright", bright_rgb),
            ("ae2:part/terminal_medium", COLOR_MEDIUM_TINTS[color_prefix]),
            ("ae2:part/terminal_dark", dark_rgb),
        ):
            terminal_layers[resource_path] = {
                "rgb_u8": rgb,
                "emissive": False,
                "triangle_count_per_part": 2,
            }
    return (
        connections,
        dict(sorted(materials.items())),
        dict(sorted(overlays.items())),
        dict(sorted(terminal_layers.items())),
    )


def attach_and_validate_s1_schema9_disabled_projection(
    cases: list[dict[str, Any]],
) -> None:
    """Attach the exact unchanged schema-9 decoder/renderer projection.

    Disabling the S1 route delegates each cable bus to the accepted M1/M2
    route.  That predecessor is deliberately narrower than S1, but it still
    owns simple cable cores and the exact terminal/stone-facade lane.  The
    physical add-on-absent projection is a separate all-empty contract.
    """

    fixture_by_position = {
        fixture["position"]: fixture
        for case in cases
        for fixture in case["fixture_blocks"]
    }

    def empty_projection() -> dict[str, Any]:
        return {
            "expected_path": "stock-empty",
            "expected_triangle_count": 0,
            "expected_material_triangles": {},
            "expected_smart_overlays": {},
            "expected_terminal_layers": {},
        }

    def projection(anchor: dict[str, Any]) -> dict[str, Any]:
        cable = anchor.get("cable_id")
        if cable is None:
            return empty_projection()
        try:
            family, _color, _texture = decode_cable_id(cable)
        except ValueError:
            return empty_projection()

        normalized_parts: dict[str, dict[str, Any]] = {}
        for direction, part in anchor.get("face_parts", {}).items():
            if (
                not isinstance(part, dict)
                or part.get("id") != TERMINAL_PART_ID
                or not isinstance(part.get("spin"), int)
                or isinstance(part.get("spin"), bool)
                or part["spin"] not in range(4)
            ):
                return empty_projection()
            # BlueNBT's accepted schema-9 DTO retains only id/spin. Unknown
            # fields therefore do not alter the predecessor projection.
            normalized_parts[direction] = {
                "id": TERMINAL_PART_ID,
                "spin": part["spin"],
            }
        if family["size"] == "dense" and normalized_parts:
            return empty_projection()

        normalized_facades = dict(anchor.get("facades", {}))
        if normalized_facades:
            if (
                len(normalized_facades) != 1
                or len(normalized_parts) != 1
                or next(iter(normalized_facades)) != next(iter(normalized_parts))
                or next(iter(normalized_facades.values()))
                != {"Name": STONE_BLOCK_ID}
            ):
                return empty_projection()

        x, y, z = anchor["position"]
        for delta in DIRECTION_DELTAS.values():
            neighbor = fixture_by_position.get(
                (x + delta[0], y + delta[1], z + delta[2])
            )
            if neighbor is None:
                continue
            neighbor_id = neighbor.get("block_id")
            if (
                neighbor_id in {"minecraft:air", "minecraft:stone"}
                and neighbor.get("expected_block_entity_id") is None
            ):
                continue
            # The accepted predecessor treats any observed block-entity host,
            # non-cable AE2 block, extension grid host, or malformed cable-bus
            # neighbor as UNKNOWN and atomically delegates to stock.
            return empty_projection()

        normalized = {
            **anchor,
            "expected_path": "custom-m2" if normalized_parts else "custom-m1",
            "face_parts": normalized_parts,
            "facades": normalized_facades,
        }
        connections, materials, overlays, terminal_layers = expected_geometry(
            normalized, {}
        )
        if connections:
            raise ValueError("S1 schema-9 projection unexpectedly connected a helper")
        return {
            "expected_path": normalized["expected_path"],
            "expected_triangle_count": sum(materials.values()),
            "expected_material_triangles": materials,
            "expected_smart_overlays": overlays,
            "expected_terminal_layers": terminal_layers,
        }

    rendered: dict[tuple[int, int, int], tuple[str, int]] = {}
    resources: set[str] = set()
    triangle_count = 0
    anchor_count = 0
    for case in cases:
        for anchor in case["anchors"]:
            disabled = projection(anchor)
            anchor["schema9_route_disabled_projection"] = disabled
            anchor_count += 1
            if disabled["expected_path"] == "stock-empty":
                continue
            position = anchor["position"]
            rendered[position] = (
                disabled["expected_path"],
                disabled["expected_triangle_count"],
            )
            triangle_count += disabled["expected_triangle_count"]
            resources.update(disabled["expected_material_triangles"])

    if (
        anchor_count != EXPECTED_S1_ANCHOR_COUNT
        or rendered != S1_SCHEMA9_DISABLED_EXPECTATIONS
        or len(rendered) != S1_SCHEMA9_DISABLED_RENDERED_ANCHOR_COUNT
        or anchor_count - len(rendered) != S1_SCHEMA9_DISABLED_EMPTY_ANCHOR_COUNT
        or triangle_count != S1_SCHEMA9_DISABLED_TRIANGLE_COUNT
        or len(resources) != S1_SCHEMA9_DISABLED_RESOURCE_COUNT
    ):
        raise ValueError("S1 schema-9 route-disabled projection changed")


def expected_m3_completion_geometry(
    anchor: dict[str, Any],
    pylon_positions: set[tuple[int, int, int]],
) -> tuple[dict[str, int], list[dict[str, str]]]:
    block_id = anchor["block_id"]
    connections: list[dict[str, str]] = []
    if block_id == PAINT_BLOCK_ID:
        materials = Counter(
            splotch["resource"]
            for splotch in anchor["paint_splotches"]
            for _ in range(2)
        )
    elif block_id == SKY_STONE_CHEST_BLOCK_ID:
        materials = Counter({"ae2:block/skychest": 36})
    elif block_id == SMOOTH_SKY_STONE_CHEST_BLOCK_ID:
        materials = Counter({"ae2:block/skyblockchest": 36})
    elif block_id == CRANK_BLOCK_ID:
        materials = Counter({"ae2:block/crank": 34})
    elif block_id == INSCRIBER_BLOCK_ID:
        materials = Counter(
            {"ae2:block/inscriber": 66, "ae2:block/inscriber_inside": 12}
        )
    elif block_id == SPATIAL_PYLON_BLOCK_ID:
        for direction, delta in DIRECTION_DELTAS.items():
            adjacent = tuple(
                anchor["position"][index] + delta[index] for index in range(3)
            )
            if adjacent in pylon_positions:
                connections.append(
                    {"direction": direction, "kind": "native-spatial-pylon"}
                )
        axis_position = anchor["pylon_axis_position"]
        if axis_position == "none":
            materials = Counter(
                {
                    "ae2:block/spatial_pylon/base": 12,
                    "ae2:block/spatial_pylon/dim": 12,
                }
            )
        elif axis_position in {"start", "end"}:
            materials = Counter(
                {
                    "ae2:block/spatial_pylon/base": 4,
                    "ae2:block/spatial_pylon/base_end": 8,
                    "ae2:block/spatial_pylon/red": 4,
                    "ae2:block/spatial_pylon/red_end": 8,
                }
            )
        elif axis_position == "middle":
            materials = Counter(
                {
                    "ae2:block/spatial_pylon/base": 4,
                    "ae2:block/spatial_pylon/base_spanned": 8,
                    "ae2:block/spatial_pylon/red": 4,
                    "ae2:block/spatial_pylon/red_spanned": 8,
                }
            )
        else:
            raise ValueError("custom spatial pylon has invalid local axis position")
    else:
        raise ValueError(f"unsupported M3f block ID: {block_id}")
    return dict(sorted(materials.items())), connections


def enrich_and_validate_cases(cases: list[dict[str, Any]]) -> None:
    s1_cases = [case for case in cases if case["milestone"] == "S1"]
    native_oracle = (
        load_native_structural_oracle(s1_cases) if s1_cases else {}
    )
    positions: dict[tuple[int, int, int], str] = {}
    cable_positions: dict[tuple[int, int, int], dict[str, Any]] = {}
    fixture_positions: set[tuple[int, int, int]] = set()
    for case in cases:
        expected_case_metadata = (
            ("S1", NATIVE_STRUCTURAL_ROUTE)
            if case["case_id"].startswith("ae2-s1-")
            else ("M3f", M3_COMPLETION_ROUTE)
            if case["case_id"].startswith("ae2-m3f-")
            else ("M3e", QUANTUM_ROUTE)
            if case["case_id"].startswith("ae2-m3e-")
            else ("M3d", CRAFTING_ROUTE)
            if case["case_id"].startswith("ae2-m3d-")
            else ("M3c", CONNECTED_GLASS_ROUTE)
            if case["case_id"].startswith("ae2-m3c-")
            else ("M3b", EXTENDED_DRIVE_BLOCK_ID)
            if case["case_id"].startswith("ae2-m3b-")
            else ("M3a", DRIVE_BLOCK_ID)
            if case["case_id"].startswith("ae2-m3-")
            else ("M2", "ae2:cable_bus")
            if case["case_id"].startswith("ae2-m2-")
            else ("M1", "ae2:cable_bus")
        )
        if (case.get("milestone"), case.get("route")) != expected_case_metadata:
            raise ValueError(
                f"case {case['case_id']} has invalid explicit milestone/route"
            )
        if case["milestone"] == "S1":
            case_bounds = S1_FIXTURE_BOUNDS
        elif case["milestone"] == "M3f":
            case_bounds = M3F_FIXTURE_BOUNDS
        elif case["milestone"] == "M3e":
            case_bounds = M3E_FIXTURE_BOUNDS
        elif case["milestone"] == "M3d":
            case_bounds = M3D_FIXTURE_BOUNDS
        elif case["milestone"] == "M3c":
            case_bounds = M3C_FIXTURE_BOUNDS
        elif case["milestone"] == "M3b":
            case_bounds = M3B_FIXTURE_BOUNDS
        elif case["milestone"] == "M3a":
            case_bounds = M3_FIXTURE_BOUNDS
        elif case["milestone"] == "M2":
            case_bounds = M2_FIXTURE_BOUNDS
        else:
            case_bounds = FIXTURE_BOUNDS
        for anchor in case["anchors"]:
            position = anchor["position"]
            if not within(position, case_bounds):
                raise ValueError(f"fixture anchor outside bounds: {position}")
            if position in positions:
                raise ValueError(f"duplicate fixture anchor: {position}")
            positions[position] = case["case_id"]
            if anchor["block_id"] == "ae2:cable_bus":
                for field in ("face_parts", "facades"):
                    if not set(anchor[field]).issubset(DIRECTION_DELTAS):
                        raise ValueError(
                            f"unknown {field} direction at {position}: {anchor[field]}"
                        )
                if case["milestone"] == "S1":
                    anchor["expected_stock_triangle_count"] = 0
                    if anchor["expected_path"] == "stock-fallback-s1":
                        anchor["expected_material_triangles"] = {}
                    for facade_state in anchor["facades"].values():
                        if (
                            isinstance(facade_state, dict)
                            and facade_state.get("Name")
                            in NATIVE_STRUCTURAL_FACADE_WHITELIST_IDS
                        ):
                            try:
                                validate_native_structural_whitelist_facade_state(
                                    facade_state
                                )
                            except ValueError:
                                if anchor["expected_path"] != "stock-fallback-s1":
                                    raise
            elif anchor["block_id"] in (DRIVE_BLOCK_ID, EXTENDED_DRIVE_BLOCK_ID):
                block_state = anchor.get("block_state")
                if (
                    not isinstance(block_state, dict)
                    or set(block_state) != {"facing", "spin"}
                    or block_state["facing"] not in DIRECTION_DELTAS
                    or block_state["spin"] not in (0, 1, 2, 3)
                ):
                    raise ValueError(f"invalid exact drive block state at {position}")
                contract = drive_contract(anchor)
                inventory = anchor.get("drive_inventory")
                if (
                    not isinstance(inventory, tuple)
                    or len(inventory) != contract["slot_count"]
                ):
                    raise ValueError(f"invalid exact drive inventory at {position}")
                for slot, item in enumerate(inventory):
                    if item is None:
                        continue
                    if set(item) not in ({"id", "count"}, {"id", "count", "components"}):
                        raise ValueError(f"unexpected drive item keys in slot {slot}")
                    if not isinstance(item.get("id"), str) or not isinstance(
                        item.get("count"), int
                    ):
                        raise ValueError(f"invalid drive item in slot {slot}")
                    if is_custom(anchor) and item["count"] != 1:
                        raise ValueError(f"invalid custom drive item count in slot {slot}")
                    if not is_custom(anchor) and item["count"] not in (1, 2):
                        raise ValueError(f"invalid fallback drive item count in slot {slot}")
                    if "components" in item and item["components"] != DRIVE_COMPONENT_INSENSITIVITY:
                        raise ValueError(f"unexpected drive components in slot {slot}")
            elif (
                anchor["expected_path"] == "custom-m3c"
                and anchor["block_id"] not in CONNECTED_GLASS_BLOCK_IDS
            ):
                raise ValueError(f"unsupported fixture block anchor at {position}")
            elif anchor["block_id"] in CRAFTING_BLOCK_KINDS:
                state = anchor.get("block_state")
                expected_state_keys = (
                    {"formed", "powered", "facing", "spin"}
                    if anchor["block_id"] == CRAFTING_MONITOR_BLOCK_ID
                    else {"formed", "powered"}
                )
                if (
                    not isinstance(state, dict)
                    or set(state) != expected_state_keys
                    or state.get("formed") is not True
                    or not isinstance(state.get("powered"), bool)
                ):
                    raise ValueError(f"invalid exact crafting state at {position}")
            elif anchor["block_id"] in {QUANTUM_LINK_BLOCK_ID, QUANTUM_RING_BLOCK_ID}:
                if (
                    anchor.get("expected_path") != "custom-m3e"
                    or anchor.get("block_state")
                    != {"formed": True, "waterlogged": False}
                    or anchor.get("expected_block_entity_id") != QUANTUM_BLOCK_ENTITY_ID
                    or anchor.get("quantum_plane") not in QUANTUM_PLANES
                    or anchor.get("quantum_role") not in {"link", "corner", "edge"}
                    or anchor.get("power_overlay_policy") != QUANTUM_STATIC_POLICY
                    or anchor.get("particle_policy") != QUANTUM_PARTICLE_POLICY
                ):
                    raise ValueError(f"invalid exact quantum bridge state at {position}")
            elif case["milestone"] == "M3f":
                block_id = anchor["block_id"]
                state = anchor.get("block_state")
                if (
                    block_id not in M3_COMPLETION_BLOCK_ENTITY_IDS
                    or anchor.get("expected_block_entity_id")
                    != M3_COMPLETION_BLOCK_ENTITY_IDS[block_id]
                    or anchor.get("static_policy")
                    not in {
                        PAINT_STATIC_POLICY,
                        CHEST_STATIC_POLICY,
                        CRANK_STATIC_POLICY,
                        INSCRIBER_STATIC_POLICY,
                        SPATIAL_PYLON_STATIC_POLICY,
                    }
                ):
                    raise ValueError(f"invalid M3f route metadata at {position}")
                if block_id == PAINT_BLOCK_ID:
                    if (
                        not isinstance(state, dict)
                        or set(state) != {"facing", "light_level"}
                        or state["facing"] not in DIRECTION_DELTAS
                        or state["light_level"] != 0
                        or len(anchor.get("paint_dots", ())) != 256
                        or not 1 <= len(anchor.get("paint_splotches", ())) <= 21
                    ):
                        raise ValueError(f"invalid exact paint state at {position}")
                elif block_id in {
                    SKY_STONE_CHEST_BLOCK_ID,
                    SMOOTH_SKY_STONE_CHEST_BLOCK_ID,
                }:
                    if (
                        set(state or {}) != {"facing", "waterlogged"}
                        or state["facing"] not in CHEST_Y_ROTATION
                        or state["waterlogged"] is not False
                    ):
                        raise ValueError(f"invalid exact chest state at {position}")
                elif block_id == CRANK_BLOCK_ID:
                    if state != {"facing": anchor.get("facing")} or anchor.get(
                        "facing"
                    ) not in DIRECTION_DELTAS:
                        raise ValueError(f"invalid exact crank state at {position}")
                elif block_id == INSCRIBER_BLOCK_ID:
                    if (
                        set(state or {}) != {"facing", "spin", "waterlogged"}
                        or state["facing"] not in DIRECTION_DELTAS
                        or state["spin"] not in range(4)
                        or state["waterlogged"] is not False
                    ):
                        raise ValueError(f"invalid exact inscriber state at {position}")
                elif block_id == SPATIAL_PYLON_BLOCK_ID and state != {
                    "powered_on": False
                }:
                    raise ValueError(f"invalid exact spatial-pylon state at {position}")
                stock_materials = M3_COMPLETION_STOCK_MATERIALS[block_id]
                anchor["expected_stock_material_triangles"] = stock_materials
                anchor["expected_stock_triangle_count"] = sum(stock_materials.values())
            if anchor["cable_id"] is not None:
                decode_cable_id(anchor["cable_id"])
                cable_positions[position] = anchor
        for fixture_block in case["fixture_blocks"]:
            position = fixture_block["position"]
            if not within(position, case_bounds):
                raise ValueError(f"fixture helper block outside bounds: {position}")
            if position in positions or position in fixture_positions:
                raise ValueError(f"duplicate fixture/helper position: {position}")
            fixture_positions.add(position)

    for case in cases:
        for anchor in case["anchors"]:
            if not is_custom(anchor):
                continue
            if case["milestone"] == "S1":
                oracle = native_oracle[anchor["position"]]
                anchor["expected_connections"] = list(
                    anchor.get("native_endpoints", ())
                )
                anchor["expected_material_triangles"] = oracle[
                    "material_triangles"
                ]
                anchor["expected_smart_overlays"] = {}
                anchor["expected_terminal_layers"] = {}
                anchor["expected_triangle_count"] = oracle["triangle_count"]
                anchor["expected_geometry_signature"] = oracle[
                    "geometry_signature"
                ]
                anchor["expected_nonlighting_attribute_signature"] = oracle[
                    "nonlighting_attribute_signature"
                ]
            elif case["milestone"] == "M3f":
                materials, connections = expected_m3_completion_geometry(
                    anchor,
                    {
                        candidate["position"]
                        for candidate_case in cases
                        if candidate_case["milestone"] == "M3f"
                        for candidate in candidate_case["anchors"]
                        if candidate["block_id"] == SPATIAL_PYLON_BLOCK_ID
                        and candidate["expected_path"] == "custom-m3f"
                    },
                )
                anchor["expected_connections"] = connections
                anchor["expected_material_triangles"] = materials
                anchor["expected_smart_overlays"] = {}
                anchor["expected_terminal_layers"] = {}
                anchor["expected_triangle_count"] = sum(materials.values())
            elif anchor["block_id"] in (DRIVE_BLOCK_ID, EXTENDED_DRIVE_BLOCK_ID):
                materials, models, led = expected_drive_geometry(anchor)
                anchor["expected_connections"] = []
                anchor["expected_material_triangles"] = materials
                anchor["expected_smart_overlays"] = {}
                anchor["expected_terminal_layers"] = {}
                anchor["expected_drive_models"] = models
                anchor["expected_drive_led"] = led
                anchor["expected_triangle_count"] = sum(materials.values())
            elif anchor["block_id"] in CONNECTED_GLASS_BLOCK_IDS:
                faces, materials, connected_faces, opaque_culled_faces = (
                    expected_connected_glass_geometry(
                        anchor,
                        {
                            candidate["position"]
                            for candidate_case in cases
                            for candidate in candidate_case["anchors"]
                            if candidate["block_id"] in CONNECTED_GLASS_BLOCK_IDS
                        },
                        fixture_positions,
                    )
                )
                anchor["expected_connections"] = [
                    {"direction": direction, "kind": "connected-glass"}
                    for direction in connected_faces
                ]
                anchor["expected_opaque_culled_faces"] = opaque_culled_faces
                anchor["expected_glass_faces"] = faces
                anchor["expected_material_triangles"] = materials
                anchor["expected_smart_overlays"] = {}
                anchor["expected_terminal_layers"] = {}
                anchor["expected_triangle_count"] = sum(materials.values())
            elif anchor["block_id"] in CRAFTING_BLOCK_KINDS:
                faces, materials, connections = expected_crafting_geometry(
                    anchor,
                    {
                        candidate["position"]
                        for candidate_case in cases
                        for candidate in candidate_case["anchors"]
                        if candidate["block_id"] in CRAFTING_BLOCK_KINDS
                    },
                )
                anchor["expected_connections"] = connections
                anchor["expected_crafting_faces"] = faces
                anchor["expected_material_triangles"] = materials
                anchor["expected_smart_overlays"] = {}
                anchor["expected_terminal_layers"] = {}
                anchor["expected_triangle_count"] = sum(materials.values())
            elif anchor["block_id"] in {QUANTUM_LINK_BLOCK_ID, QUANTUM_RING_BLOCK_ID}:
                primitives, materials, connections = expected_quantum_geometry(
                    anchor,
                    {
                        candidate["position"]
                        for candidate_case in cases
                        if candidate_case["milestone"] == "M3e"
                        for candidate in candidate_case["anchors"]
                    },
                )
                anchor["expected_connections"] = connections
                anchor["expected_quantum_primitives"] = primitives
                anchor["expected_material_triangles"] = materials
                anchor["expected_smart_overlays"] = {}
                anchor["expected_terminal_layers"] = {}
                anchor["expected_triangle_count"] = sum(materials.values())
            else:
                connections, materials, overlays, terminal_layers = expected_geometry(
                    anchor,
                    cable_positions,
                )
                anchor["expected_connections"] = connections
                anchor["expected_material_triangles"] = materials
                anchor["expected_smart_overlays"] = overlays
                anchor["expected_terminal_layers"] = terminal_layers
                anchor["expected_triangle_count"] = sum(materials.values())

    if len(cases) != EXPECTED_CASE_COUNT:
        raise ValueError(f"expected {EXPECTED_CASE_COUNT} cases, got {len(cases)}")
    if len(positions) != EXPECTED_ANCHOR_COUNT:
        raise ValueError(f"expected {EXPECTED_ANCHOR_COUNT} anchors, got {len(positions)}")
    custom = [
        anchor
        for case in cases
        for anchor in case["anchors"]
        if is_custom(anchor)
    ]
    if len(custom) != EXPECTED_CUSTOM_ANCHOR_COUNT:
        raise ValueError(
            f"expected {EXPECTED_CUSTOM_ANCHOR_COUNT} custom anchors, got {len(custom)}"
        )
    triangles = sum(anchor["expected_triangle_count"] for anchor in custom)
    if triangles != EXPECTED_CUSTOM_TRIANGLE_COUNT:
        raise ValueError(
            f"expected {EXPECTED_CUSTOM_TRIANGLE_COUNT} custom triangles, got {triangles}"
        )

    for milestone, expected_anchors, expected_triangles in (
        ("m1", EXPECTED_M1_CUSTOM_ANCHOR_COUNT, EXPECTED_M1_CUSTOM_TRIANGLE_COUNT),
        ("m2", EXPECTED_M2_CUSTOM_ANCHOR_COUNT, EXPECTED_M2_CUSTOM_TRIANGLE_COUNT),
        ("m3", EXPECTED_M3_CUSTOM_ANCHOR_COUNT, EXPECTED_M3_CUSTOM_TRIANGLE_COUNT),
        (
            "m3b",
            EXPECTED_M3B_CUSTOM_ANCHOR_COUNT,
            EXPECTED_M3B_CUSTOM_TRIANGLE_COUNT,
        ),
        (
            "m3c",
            EXPECTED_M3C_CUSTOM_ANCHOR_COUNT,
            EXPECTED_M3C_CUSTOM_TRIANGLE_COUNT,
        ),
        (
            "m3d",
            EXPECTED_M3D_CUSTOM_ANCHOR_COUNT,
            EXPECTED_M3D_CUSTOM_TRIANGLE_COUNT,
        ),
        (
            "m3e",
            EXPECTED_M3E_CUSTOM_ANCHOR_COUNT,
            EXPECTED_M3E_CUSTOM_TRIANGLE_COUNT,
        ),
        (
            "m3f",
            EXPECTED_M3F_CUSTOM_ANCHOR_COUNT,
            EXPECTED_M3F_CUSTOM_TRIANGLE_COUNT,
        ),
        (
            "s1",
            EXPECTED_S1_CUSTOM_ANCHOR_COUNT,
            EXPECTED_S1_CUSTOM_TRIANGLE_COUNT,
        ),
    ):
        selected = [
            anchor
            for case in cases
            for anchor in case["anchors"]
            if anchor["expected_path"] == f"custom-{milestone}"
        ]
        if len(selected) != expected_anchors:
            raise ValueError(
                f"expected {expected_anchors} custom {milestone.upper()} anchors, "
                f"got {len(selected)}"
            )
        selected_triangles = sum(
            anchor["expected_triangle_count"] for anchor in selected
        )
        if selected_triangles != expected_triangles:
            raise ValueError(
                f"expected {expected_triangles} custom {milestone.upper()} triangles, "
                f"got {selected_triangles}"
            )

    m2_fallbacks = [
        anchor
        for case in cases
        for anchor in case["anchors"]
        if anchor["expected_path"] == "stock-fallback-m2"
    ]
    if len(m2_fallbacks) != EXPECTED_M2_FALLBACK_ANCHOR_COUNT:
        raise ValueError(
            f"expected {EXPECTED_M2_FALLBACK_ANCHOR_COUNT} M2 fallback anchors, "
            f"got {len(m2_fallbacks)}"
        )
    if any(anchor["expected_triangle_count"] != 0 for anchor in m2_fallbacks):
        raise ValueError("every M2 fallback anchor must own zero triangles")

    m3_fallbacks = [
        anchor
        for case in cases
        for anchor in case["anchors"]
        if anchor["expected_path"] == "stock-fallback-m3"
    ]
    if len(m3_fallbacks) != EXPECTED_M3_FALLBACK_ANCHOR_COUNT:
        raise ValueError(
            f"expected {EXPECTED_M3_FALLBACK_ANCHOR_COUNT} M3 fallback anchors, "
            f"got {len(m3_fallbacks)}"
        )
    if any(anchor["expected_triangle_count"] != 0 for anchor in m3_fallbacks):
        raise ValueError("every M3 fallback anchor must own zero triangles")

    m3b_fallbacks = [
        anchor
        for case in cases
        for anchor in case["anchors"]
        if anchor["expected_path"] == "stock-fallback-m3b"
    ]
    if len(m3b_fallbacks) != EXPECTED_M3B_FALLBACK_ANCHOR_COUNT:
        raise ValueError(
            f"expected {EXPECTED_M3B_FALLBACK_ANCHOR_COUNT} M3b fallback anchors, "
            f"got {len(m3b_fallbacks)}"
        )
    if any(anchor["expected_triangle_count"] != 0 for anchor in m3b_fallbacks):
        raise ValueError("every M3b fallback anchor must own zero triangles")

    m3d_fallbacks = [
        anchor
        for case in cases
        for anchor in case["anchors"]
        if anchor["expected_path"] == "stock-fallback-m3d"
    ]
    if len(m3d_fallbacks) != EXPECTED_M3D_FALLBACK_ANCHOR_COUNT:
        raise ValueError(
            f"expected {EXPECTED_M3D_FALLBACK_ANCHOR_COUNT} M3d fallback anchors, "
            f"got {len(m3d_fallbacks)}"
        )
    if any(anchor["expected_triangle_count"] != 0 for anchor in m3d_fallbacks):
        raise ValueError("the M3d mixed-extension fallback must own zero triangles")

    m3f_fallbacks = [
        anchor
        for case in cases
        for anchor in case["anchors"]
        if anchor["expected_path"] == "stock-fallback-m3f"
    ]
    if len(m3f_fallbacks) != EXPECTED_M3F_FALLBACK_ANCHOR_COUNT:
        raise ValueError(
            f"expected {EXPECTED_M3F_FALLBACK_ANCHOR_COUNT} M3f fallback anchors, "
            f"got {len(m3f_fallbacks)}"
        )
    s1_fallbacks = [
        anchor
        for case in cases
        for anchor in case["anchors"]
        if anchor["expected_path"] == "stock-fallback-s1"
    ]
    if len(s1_fallbacks) != EXPECTED_S1_FALLBACK_ANCHOR_COUNT:
        raise ValueError(
            f"expected {EXPECTED_S1_FALLBACK_ANCHOR_COUNT} S1 fallback anchors, "
            f"got {len(s1_fallbacks)}"
        )
    if any(anchor["expected_triangle_count"] != 0 for anchor in s1_fallbacks):
        raise ValueError("every S1 fallback anchor must own zero triangles")
    all_fallbacks = [
        anchor
        for case in cases
        for anchor in case["anchors"]
        if anchor["expected_path"].startswith("stock-fallback-")
    ]
    if len(all_fallbacks) != EXPECTED_STOCK_FALLBACK_ANCHOR_COUNT:
        raise ValueError("the cumulative stock-fallback anchor count changed")
    cases_by_id = {case["case_id"]: case for case in cases}
    cable_anchor_fallback = cases_by_id["ae2-m2-06"]["anchors"][0]
    if (
        cable_anchor_fallback["cable_id"] != cable_id("glass", "fluix")
        or cable_anchor_fallback["face_parts"]
        != {"north": {"id": "ae2:cable_anchor"}}
        or cable_anchor_fallback["fallback_reason"] != "unsupported-face-part"
    ):
        raise ValueError("M2 cable-anchor fallback no longer has its durable exact NBT")
    standalone_terminal = cases_by_id["ae2-m2-09"]["anchors"][0]
    if (
        standalone_terminal["cable_id"] is not None
        or standalone_terminal["face_parts"]
        != {"north": {"id": TERMINAL_PART_ID, "spin": 0}}
        or standalone_terminal["fallback_reason"] != "missing-center-part"
    ):
        raise ValueError(
            "M2 standalone-terminal fallback no longer has its durable exact NBT"
        )

    m3_cases = [case for case in cases if case["milestone"] == "M3a"]
    m3_anchors = [anchor for case in m3_cases for anchor in case["anchors"]]
    if any(anchor["position"][1] != 100 for anchor in m3_anchors):
        raise ValueError("every M3 drive anchor must remain at y=100")
    if (
        M3_FIXTURE_SUPPORT_BOUNDS[0][1] != 98
        or M3_FIXTURE_SUPPORT_BOUNDS[1][1] != 98
        or M3_FIXTURE_AIR_GAP_BOUNDS[0][1] != 99
        or M3_FIXTURE_AIR_GAP_BOUNDS[1][1] != 99
    ):
        raise ValueError("M3 floor policy must retain a y=99 air gap above y=98")
    orientation_states = {
        (anchor["block_state"]["facing"], anchor["block_state"]["spin"])
        for case in m3_cases[:6]
        for anchor in case["anchors"]
    }
    if orientation_states != {
        (facing, spin) for facing in DIRECTION_DELTAS for spin in range(4)
    }:
        raise ValueError("M3 drive orientation cases do not cover all 24 states")
    catalog_ids = {
        item["id"]
        for case in m3_cases[7:10]
        for anchor in case["anchors"]
        for item in anchor["drive_inventory"]
        if item is not None
    }
    if catalog_ids != set(DRIVE_CELL_MODELS):
        raise ValueError("M3 drive catalogs do not cover the exact 23 supported IDs")
    occupied_model_ids = {
        slot["model_id"]
        for anchor in m3_anchors
        if anchor["expected_path"] == "custom-m3"
        for slot in anchor["expected_drive_models"]["slots"]
        if slot["model_id"] != DRIVE_EMPTY_CELL_MODEL
    }
    if len(occupied_model_ids) != DRIVE_OCCUPIED_MODEL_COUNT or occupied_model_ids != set(
        DRIVE_CELL_MODELS.values()
    ):
        raise ValueError("M3 drive gallery does not cover all 12 occupied models")
    for anchor in m3_anchors:
        if anchor["expected_path"] != "custom-m3":
            continue
        occupied_count = sum(item is not None for item in anchor["drive_inventory"])
        expected_triangles = DRIVE_BASE_TRIANGLE_COUNT + 16 * occupied_count
        if anchor["expected_triangle_count"] != expected_triangles:
            raise ValueError("M3 drive triangle formula is not 90 + 16N")
        if anchor["expected_drive_led"]["triangle_count"] != 10 * occupied_count:
            raise ValueError("M3 drive LED triangle formula is not 10N")
    components_case = cases_by_id["ae2-m3-13"]["anchors"]
    if (
        components_case[0]["drive_inventory"][0]
        != drive_item("ae2:item_storage_cell_1k")
        or components_case[1]["drive_inventory"][0]
        != drive_item(
            "ae2:item_storage_cell_1k",
            components=DRIVE_COMPONENT_INSENSITIVITY,
        )
        or components_case[0]["expected_material_triangles"]
        != components_case[1]["expected_material_triangles"]
    ):
        raise ValueError("M3 component-insensitivity pair changed unexpectedly")
    durable_fallback = cases_by_id["ae2-m3-14"]["anchors"][0]
    if (
        durable_fallback["block_state"] != {"facing": "south", "spin": 0}
        or durable_fallback["drive_inventory"][0]
        != drive_item(DRIVE_FALLBACK_CELL_ID)
        or durable_fallback["fallback_reason"] != "unsupported-drive-cell-id"
    ):
        raise ValueError("M3 durable drive fallback changed unexpectedly")

    m3b_cases = [case for case in cases if case["milestone"] == "M3b"]
    m3b_anchors = [anchor for case in m3b_cases for anchor in case["anchors"]]
    if len(m3b_cases) != EXPECTED_M3B_CASE_COUNT:
        raise ValueError("M3b gallery must retain exactly 16 cases")
    if any(anchor["position"][1] != 100 for anchor in m3b_anchors):
        raise ValueError("every M3b extended-drive anchor must remain at y=100")
    if (
        M3B_FIXTURE_SUPPORT_BOUNDS[0][1] != 98
        or M3B_FIXTURE_SUPPORT_BOUNDS[1][1] != 98
        or M3B_FIXTURE_AIR_GAP_BOUNDS[0][1] != 99
        or M3B_FIXTURE_AIR_GAP_BOUNDS[1][1] != 99
    ):
        raise ValueError("M3b floor policy must retain a y=99 air gap above y=98")
    m3b_orientation_states = {
        (anchor["block_state"]["facing"], anchor["block_state"]["spin"])
        for case in m3b_cases[:6]
        for anchor in case["anchors"]
    }
    if m3b_orientation_states != {
        (facing, spin) for facing in DIRECTION_DELTAS for spin in range(4)
    }:
        raise ValueError("M3b extended-drive cases do not cover all 24 states")
    m3b_custom = [
        anchor
        for anchor in m3b_anchors
        if anchor["expected_path"] == "custom-m3b"
    ]
    catalog_ids = {
        item["id"]
        for anchor in m3b_custom
        for item in anchor["drive_inventory"]
        if item is not None
    }
    if catalog_ids != set(EXTENDED_DRIVE_CELL_MODELS):
        raise ValueError("M3b gallery does not cover the exact 26 supported cell IDs")
    occupied_model_ids = {
        slot["model_id"]
        for anchor in m3b_custom
        for slot in anchor["expected_drive_models"]["slots"]
        if slot["model_id"] != EXTENDED_DRIVE_EMPTY_CELL_MODEL
    }
    if (
        len(occupied_model_ids) != EXTENDED_DRIVE_OCCUPIED_MODEL_COUNT
        or occupied_model_ids != set(EXTENDED_DRIVE_CELL_MODELS.values())
    ):
        raise ValueError("M3b gallery does not cover all 15 occupied models")
    occupied_slot_count = 0
    for anchor in m3b_custom:
        occupied_count = sum(item is not None for item in anchor["drive_inventory"])
        occupied_slot_count += occupied_count
        if anchor["expected_triangle_count"] != (
            EXTENDED_DRIVE_BASE_TRIANGLE_COUNT + 16 * occupied_count
        ):
            raise ValueError("M3b extended-drive triangle formula is not 116 + 16N")
        if anchor["expected_drive_led"]["triangle_count"] != 10 * occupied_count:
            raise ValueError("M3b extended-drive LED triangle formula is not 10N")
        slots = anchor["expected_drive_models"]["slots"]
        if [slot["slot"] for slot in slots] != list(range(20)):
            raise ValueError("M3b drive metadata does not enumerate all 20 slots")
        for slot in slots:
            expected_face = "front" if slot["slot"] < 10 else "back"
            expected_facing = (
                anchor["block_state"]["facing"]
                if expected_face == "front"
                else OPPOSITES[anchor["block_state"]["facing"]]
            )
            if (
                slot["face"] != expected_face
                or slot["face_slot"] != slot["slot"] % 10
                or slot["orientation"]
                != {"facing": expected_facing, "spin": anchor["block_state"]["spin"]}
            ):
                raise ValueError("M3b front/back slot metadata changed unexpectedly")
    if occupied_slot_count != 84:
        raise ValueError(f"expected 84 occupied M3b slots, got {occupied_slot_count}")
    mirror_pair = cases_by_id["ae2-m3b-11"]["anchors"]
    if (
        mirror_pair[0]["drive_inventory"][0] != drive_item("extendedae:void_cell")
        or mirror_pair[1]["drive_inventory"][10] != drive_item("extendedae:void_cell")
    ):
        raise ValueError("M3b front/back mirror pair changed unexpectedly")
    component_pair = cases_by_id["ae2-m3b-12"]["anchors"]
    if (
        component_pair[0]["drive_inventory"][0]
        != drive_item("extendedae:infinity_water_cell")
        or component_pair[1]["drive_inventory"][0]
        != drive_item(
            "extendedae:infinity_water_cell",
            components=DRIVE_COMPONENT_INSENSITIVITY,
        )
        or component_pair[0]["expected_material_triangles"]
        != component_pair[1]["expected_material_triangles"]
    ):
        raise ValueError("M3b component-insensitivity pair changed unexpectedly")
    actual_fallbacks = {
        anchor["position"]: (
            anchor["drive_inventory"][0]["id"],
            anchor["drive_inventory"][0]["count"],
            anchor["fallback_reason"],
        )
        for anchor in m3b_fallbacks
    }
    expected_fallbacks = {
        (266, 100, 266): (
            "megacells:item_storage_cell_1m",
            1,
            EXTENDED_DRIVE_FALLBACKS["megacells:item_storage_cell_1m"],
        ),
        (269, 100, 266): (
            "kubejs:lava_cell",
            1,
            EXTENDED_DRIVE_FALLBACKS["kubejs:lava_cell"],
        ),
        (272, 100, 266): (
            "ae2:item_storage_cell_1k",
            2,
            EXTENDED_DRIVE_FALLBACKS["ae2:item_storage_cell_1k"],
        ),
        (275, 100, 266): (
            "minecraft:stone",
            1,
            EXTENDED_DRIVE_FALLBACKS["minecraft:stone"],
        ),
    }
    if actual_fallbacks != expected_fallbacks:
        raise ValueError("M3b atomic fallback catalog changed unexpectedly")

    m3c_cases = [case for case in cases if case["milestone"] == "M3c"]
    m3c_anchors = [anchor for case in m3c_cases for anchor in case["anchors"]]
    exact_m3c_case_shapes = [2, 3, 3, 3, 4, 5, 2, 9, 8, 7, 1]
    exact_m3c_case_triangles = [48, 56, 56, 56, 72, 88, 48, 116, 96, 120, 20]
    if (
        len(m3c_cases) != EXPECTED_M3C_CASE_COUNT
        or [len(case["anchors"]) for case in m3c_cases] != exact_m3c_case_shapes
        or [
            sum(anchor["expected_triangle_count"] for anchor in case["anchors"])
            for case in m3c_cases
        ]
        != exact_m3c_case_triangles
        or len(m3c_anchors) != EXPECTED_M3C_ANCHOR_COUNT
    ):
        raise ValueError("M3c case/anchor/triangle partition changed unexpectedly")
    m3c_resources = {
        resource
        for anchor in m3c_anchors
        for resource in anchor["expected_material_triangles"]
    }
    if m3c_resources != set(CONNECTED_GLASS_SELECTED_RESOURCES):
        raise ValueError("M3c connected-glass resource closure changed unexpectedly")
    frame_occurrences = Counter(
        face["frame_mask"]
        for anchor in m3c_anchors
        for face in anchor["expected_glass_faces"]
        if face["frame_resource"] is not None
    )
    no_frame_occurrences = sum(
        face["frame_mask"] == "0000"
        for anchor in m3c_anchors
        for face in anchor["expected_glass_faces"]
    )
    if dict(sorted(frame_occurrences.items())) != CONNECTED_GLASS_FRAME_OCCURRENCES:
        raise ValueError("M3c frame-mask occurrence contract changed unexpectedly")
    if no_frame_occurrences != 2:
        raise ValueError("M3c checkerboard center must provide two mask-0000 faces")
    selection_pair = m3c_cases[0]["anchors"]
    if {anchor["block_id"] for anchor in selection_pair} != set(
        CONNECTED_GLASS_BLOCK_IDS
    ):
        raise ValueError("M3c matched selection pair must cover both glass variants")
    first_selection = dict(selection_pair[0]["expected_glass_base_selection"])
    second_selection = dict(selection_pair[1]["expected_glass_base_selection"])
    first_selection.pop("position_seed_i64")
    second_selection.pop("position_seed_i64")
    if first_selection != second_selection:
        raise ValueError("M3c ordinary/vibrant matched selection pair changed")
    texture_indexes = {
        anchor["expected_glass_base_selection"]["draws"]["texture_index"]
        for anchor in m3c_anchors
    }
    if texture_indexes != {0, 1, 2, 3}:
        raise ValueError("M3c fixture no longer covers base selections A through D")
    opaque_case = m3c_cases[-1]
    if opaque_case["fixture_blocks"] != (
        {"position": (227, 100, 301), "block_id": STONE_BLOCK_ID},
    ) or opaque_case["anchors"][0]["expected_opaque_culled_faces"] != ["east"]:
        raise ValueError("M3c opaque-neighbor culling contract changed")

    m3d_cases = [case for case in cases if case["milestone"] == "M3d"]
    m3d_anchors = [anchor for case in m3d_cases for anchor in case["anchors"]]
    exact_m3d_shapes = [5, 2, 2, 3, 4, 8, 27, 34, 1]
    exact_m3d_triangles = [600, 142, 152, 174, 184, 234, 304, 2_516, 0]
    if (
        len(m3d_cases) != EXPECTED_M3D_CASE_COUNT
        or [len(case["anchors"]) for case in m3d_cases] != exact_m3d_shapes
        or [
            sum(anchor["expected_triangle_count"] for anchor in case["anchors"])
            for case in m3d_cases
        ]
        != exact_m3d_triangles
        or len(m3d_anchors) != EXPECTED_M3D_ANCHOR_COUNT
    ):
        raise ValueError("M3d case/anchor/triangle partition changed unexpectedly")
    m3d_custom = [
        anchor for anchor in m3d_anchors if anchor["expected_path"] == "custom-m3d"
    ]
    m3d_resources = {
        resource
        for anchor in m3d_custom
        for resource in anchor["expected_material_triangles"]
    }
    if m3d_resources != set(CRAFTING_RESOURCES):
        raise ValueError("M3d crafting resource closure changed unexpectedly")
    monitors = [
        anchor
        for anchor in m3d_custom
        if anchor["block_id"] == CRAFTING_MONITOR_BLOCK_ID
    ]
    if (
        {anchor["painted_color_ordinal"] for anchor in monitors} != set(range(17))
        or {anchor["block_state"]["facing"] for anchor in monitors}
        != set(DIRECTION_DELTAS)
        or {anchor["block_state"]["spin"] for anchor in monitors} != set(range(4))
        or any(
            OPPOSITES[anchor["block_state"]["facing"]]
            not in {connection["direction"] for connection in anchor["expected_connections"]}
            for anchor in monitors
        )
        or any(
            anchor["block_state"]["facing"]
            in {connection["direction"] for connection in anchor["expected_connections"]}
            for anchor in monitors
        )
    ):
        raise ValueError("M3d monitor paint/orientation/front-exposure contract changed")
    powered = [anchor for anchor in m3d_custom if anchor["block_state"]["powered"]]
    if len(powered) != 8 or any(
        not (297 <= anchor["position"][0] <= 298)
        or not (100 <= anchor["position"][1] <= 101)
        or not (269 <= anchor["position"][2] <= 270)
        for anchor in powered
    ):
        raise ValueError("M3d powered all-eight cube changed unexpectedly")
    enclosed = next(
        anchor for anchor in m3d_custom if anchor["position"] == (305, 101, 270)
    )
    if enclosed["expected_triangle_count"] != 0 or len(enclosed["expected_connections"]) != 6:
        raise ValueError("M3d fully enclosed zero-geometry anchor changed")
    mixed_case = m3d_cases[-1]
    if (
        mixed_case["anchors"][0]["position"] != (318, 100, 261)
        or mixed_case["anchors"][0]["fallback_reason"]
        != "compatible-extension-crafting-neighbor"
        or [block["block_id"] for block in mixed_case["fixture_blocks"]]
        != ["megacells:mega_crafting_unit", "expandedae:exp_crafting_unit"]
    ):
        raise ValueError("M3d compatible-extension fallback contract changed")
    if any(
        position[0] < 297
        or min(abs(position[0] - legacy_x) + abs(position[1] - legacy_y) + abs(position[2] - legacy_z)
               for legacy_x, legacy_y, legacy_z in (
                   anchor["position"]
                   for case in m3c_cases
                   for anchor in case["anchors"]
               )) <= 15
        for position in [
            *(anchor["position"] for anchor in m3d_anchors),
            *(block["position"] for case in m3d_cases for block in case["fixture_blocks"]),
        ]
    ):
        raise ValueError("M3d selected/context blocks must stay isolated from M3c")

    m3e_cases = [case for case in cases if case["milestone"] == "M3e"]
    m3e_anchors = [anchor for case in m3e_cases for anchor in case["anchors"]]
    expected_centers = {
        "xz": (287, 100, 271),
        "xy": (283, 101, 276),
        "yz": (290, 101, 271),
    }
    if (
        len(m3e_cases) != EXPECTED_M3E_CASE_COUNT
        or [case["case_id"] for case in m3e_cases]
        != ["ae2-m3e-01", "ae2-m3e-02", "ae2-m3e-03"]
        or [len(case["anchors"]) for case in m3e_cases] != [9, 9, 9]
        or len(m3e_anchors) != EXPECTED_M3E_ANCHOR_COUNT
        or [sum(anchor["expected_triangle_count"] for anchor in case["anchors"]) for case in m3e_cases]
        != [396, 396, 396]
        or {case["anchors"][4]["quantum_plane"] for case in m3e_cases}
        != set(QUANTUM_PLANES)
    ):
        raise ValueError("M3e quantum case/anchor/triangle partition changed")
    for case in m3e_cases:
        plane = case["anchors"][4]["quantum_plane"]
        centers = [
            anchor for anchor in case["anchors"] if anchor["quantum_role"] == "link"
        ]
        corners = [
            anchor for anchor in case["anchors"] if anchor["quantum_role"] == "corner"
        ]
        edges = [
            anchor for anchor in case["anchors"] if anchor["quantum_role"] == "edge"
        ]
        material_totals = Counter(
            {
                resource: sum(
                    anchor["expected_material_triangles"].get(resource, 0)
                    for anchor in case["anchors"]
                )
                for resource in QUANTUM_RESOURCES
            }
        )
        if (
            len(centers) != 1
            or centers[0]["position"] != expected_centers[plane]
            or len(corners) != 4
            or len(edges) != 4
            or centers[0]["expected_triangle_count"] != 108
            or any(anchor["expected_triangle_count"] != 36 for anchor in corners + edges)
            or material_totals
            != Counter(
                {
                    QUANTUM_LINK_RESOURCE: 12,
                    QUANTUM_GLASS_RESOURCE: 48,
                    QUANTUM_COVERED_RESOURCE: 144,
                    QUANTUM_RING_RESOURCE: 192,
                }
            )
        ):
            raise ValueError(f"M3e {plane} quantum topology/material contract changed")
    if not any(
        anchor["position"][0] == 287 for anchor in m3e_cases[0]["anchors"]
    ) or not any(
        anchor["position"][0] == 288 for anchor in m3e_cases[0]["anchors"]
    ):
        raise ValueError("M3e XZ bridge must cross the x=287/288 chunk boundary")
    m3e_positions = {anchor["position"] for anchor in m3e_anchors}
    earlier_positions = {
        anchor["position"]
        for case in cases
        if case["milestone"] != "M3e"
        for anchor in case["anchors"]
    }
    if m3e_positions & earlier_positions:
        raise ValueError("M3e anchors overlap the frozen M0--M3d gallery")

    m3f_cases = [case for case in cases if case["milestone"] == "M3f"]
    m3f_anchors = [anchor for case in m3f_cases for anchor in case["anchors"]]
    m3f_custom = [
        anchor for anchor in m3f_anchors if anchor["expected_path"] == "custom-m3f"
    ]
    if (
        len(m3f_cases) != EXPECTED_M3F_CASE_COUNT
        or [case["case_id"] for case in m3f_cases]
        != [f"ae2-m3f-{index:02d}" for index in range(1, 8)]
        or [len(case["anchors"]) for case in m3f_cases]
        != [23, 8, 6, 24, 10, 3, 4]
        or [
            sum(anchor["expected_triangle_count"] for anchor in case["anchors"])
            for case in m3f_cases
        ]
        != [50, 288, 204, 1_872, 240, 72, 96]
        or len(m3f_anchors) != EXPECTED_M3F_ANCHOR_COUNT
        or len(m3f_custom) != EXPECTED_M3F_CUSTOM_ANCHOR_COUNT
    ):
        raise ValueError("M3f case/anchor/triangle partition changed unexpectedly")
    m3f_resources = {
        resource
        for anchor in m3f_custom
        for resource in anchor["expected_material_triangles"]
    }
    if m3f_resources != set(M3_COMPLETION_RESOURCES):
        raise ValueError("M3f emitted resource closure changed unexpectedly")
    paint_anchors = [
        anchor for anchor in m3f_custom if anchor["block_id"] == PAINT_BLOCK_ID
    ]
    if (
        len(paint_anchors) != 23
        or sum(len(anchor["paint_splotches"]) for anchor in paint_anchors) != 25
        or {splotch["color_ordinal"] for anchor in paint_anchors for splotch in anchor["paint_splotches"]}
        != set(range(16))
        or {splotch["backing_side"] for anchor in paint_anchors for splotch in anchor["paint_splotches"]}
        != set(PAINT_DIRECTION_ORDINALS)
    ):
        raise ValueError("M3f paint palette/face/layering matrix changed unexpectedly")
    for anchor in paint_anchors:
        dots = anchor["paint_dots"]
        record_end = 1 + 2 * len(anchor["paint_splotches"])
        if (
            len(dots) != 256
            or dots[0] != len(anchor["paint_splotches"])
            or any(dots[index] != 0 for index in range(record_end, 256))
            or any(splotch["lumen"] for splotch in anchor["paint_splotches"])
        ):
            raise ValueError("M3f durable paint dots array changed unexpectedly")
    chest_anchors = [
        anchor
        for anchor in m3f_custom
        if anchor["block_id"]
        in {SKY_STONE_CHEST_BLOCK_ID, SMOOTH_SKY_STONE_CHEST_BLOCK_ID}
    ]
    if (
        len(chest_anchors) != 8
        or {
            (anchor["block_id"], anchor["block_state"]["facing"])
            for anchor in chest_anchors
        }
        != {
            (block_id, facing)
            for block_id in (SKY_STONE_CHEST_BLOCK_ID, SMOOTH_SKY_STONE_CHEST_BLOCK_ID)
            for facing in CHEST_Y_ROTATION
        }
    ):
        raise ValueError("M3f chest variant/facing matrix changed unexpectedly")
    crank_anchors = [
        anchor for anchor in m3f_custom if anchor["block_id"] == CRANK_BLOCK_ID
    ]
    if (
        len(crank_anchors) != 6
        or {anchor["facing"] for anchor in crank_anchors} != set(DIRECTION_DELTAS)
        or len(m3f_cases[2]["fixture_blocks"]) != 6
        or any(
            fixture["block_id"] != "ae2:charger"
            or fixture.get("expected_block_entity_id") != "ae2:charger"
            for fixture in m3f_cases[2]["fixture_blocks"]
        )
    ):
        raise ValueError("M3f persistent crank orientation matrix changed unexpectedly")
    inscriber_anchors = [
        anchor for anchor in m3f_custom if anchor["block_id"] == INSCRIBER_BLOCK_ID
    ]
    if (
        len(inscriber_anchors) != 24
        or {
            (anchor["block_state"]["facing"], anchor["block_state"]["spin"])
            for anchor in inscriber_anchors
        }
        != {(facing, spin) for facing in DIRECTION_DELTAS for spin in range(4)}
    ):
        raise ValueError("M3f inscriber facing/spin matrix changed unexpectedly")
    supported_pylons = [
        anchor
        for anchor in m3f_custom
        if anchor["block_id"] == SPATIAL_PYLON_BLOCK_ID
    ]
    invalid_component_positions = (
        {(310, 104, 214), (311, 104, 214), (310, 104, 215)},
        {(316, 103, 214), (315, 103, 214), (317, 103, 214), (316, 104, 214)},
    )
    if (
        len(supported_pylons) != 17
        or Counter(anchor["pylon_axis_position"] for anchor in supported_pylons)
        != Counter({"none": 8, "start": 3, "middle": 3, "end": 3})
        or {anchor["pylon_axis"] for anchor in supported_pylons} != {"x", "y", "z"}
        or any(case["fixture_blocks"] for case in m3f_cases[-2:])
        or tuple(
            {anchor["position"] for anchor in case["anchors"]}
            for case in m3f_cases[-2:]
        )
        != invalid_component_positions
        or any(
            anchor["pylon_axis"] != "x"
            or anchor["pylon_axis_position"] != "none"
            or anchor["expected_material_triangles"]
            != {
                "ae2:block/spatial_pylon/base": 12,
                "ae2:block/spatial_pylon/dim": 12,
            }
            for case in m3f_cases[-2:]
            for anchor in case["anchors"]
        )
    ):
        raise ValueError(
            "M3f spatial-pylon valid-line/invalid-component matrix changed unexpectedly"
        )
    stock_rendered = [
        anchor for anchor in m3f_anchors if anchor["expected_stock_triangle_count"] > 0
    ]
    stock_empty = [
        anchor for anchor in m3f_anchors if anchor["expected_stock_triangle_count"] == 0
    ]
    if (
        len(stock_rendered) != 38
        or len(stock_empty) != 40
        or sum(anchor["expected_stock_triangle_count"] for anchor in m3f_anchors)
        != 1_872
        or {
            resource
            for anchor in m3f_anchors
            for resource in anchor["expected_stock_material_triangles"]
        }
        != set(M3_COMPLETION_STOCK_RESOURCES)
    ):
        raise ValueError("M3f original-resource stock projection changed unexpectedly")
    m3f_positions = {anchor["position"] for anchor in m3f_anchors}
    frozen_positions = {
        anchor["position"]
        for case in cases
        if case["milestone"] != "M3f"
        for anchor in case["anchors"]
    }
    if m3f_positions & frozen_positions:
        raise ValueError("M3f anchors overlap the frozen M0--M3e gallery")

    main_bounds = (
        FIXTURE_BOUNDS,
        M2_FIXTURE_BOUNDS,
        M3_FIXTURE_BOUNDS,
        M3B_FIXTURE_BOUNDS,
        M3C_FIXTURE_BOUNDS,
        M3D_FIXTURE_BOUNDS,
        M3E_FIXTURE_BOUNDS,
        M3F_FIXTURE_BOUNDS,
        S1_FIXTURE_BOUNDS,
        DECK_BOUNDS,
        SENTINEL_BOUNDS,
    )
    for index, first in enumerate(main_bounds):
        for second in main_bounds[index + 1 :]:
            if overlaps(first, second):
                raise ValueError(f"owned gallery bounds overlap: {first} and {second}")
    for dense_bounds in DENSE_OWNED_BOUNDS:
        for gallery_bounds in main_bounds:
            if overlaps(dense_bounds, gallery_bounds):
                raise ValueError(
                    f"optional dense and gallery bounds overlap: {dense_bounds}/{gallery_bounds}"
                )
    for index, first in enumerate(DENSE_OWNED_BOUNDS):
        for second in DENSE_OWNED_BOUNDS[index + 1 :]:
            if overlaps(first, second):
                raise ValueError(f"optional dense bounds overlap: {first}/{second}")
    dense_chunk_overlap = chunks_for_bounds(M3F_FIXTURE_BOUNDS) & set().union(
        *(chunks_for_bounds(bounds) for bounds in DENSE_OWNED_BOUNDS)
    )
    if (
        (19, 14) not in dense_chunk_overlap
        or dense_chunk_overlap & set(dense_exclusive_chunks())
    ):
        raise ValueError(
            "M3f/shared optional-dense chunks must never be classified dense-exclusive"
        )

    for label, inner, outer in (
        ("fixture support", FIXTURE_SUPPORT_BOUNDS, FIXTURE_BOUNDS),
        ("M2 fixture support", M2_FIXTURE_SUPPORT_BOUNDS, M2_FIXTURE_BOUNDS),
        ("M3 fixture support", M3_FIXTURE_SUPPORT_BOUNDS, M3_FIXTURE_BOUNDS),
        ("M3 fixture air gap", M3_FIXTURE_AIR_GAP_BOUNDS, M3_FIXTURE_BOUNDS),
        ("M3b fixture support", M3B_FIXTURE_SUPPORT_BOUNDS, M3B_FIXTURE_BOUNDS),
        ("M3b fixture air gap", M3B_FIXTURE_AIR_GAP_BOUNDS, M3B_FIXTURE_BOUNDS),
        ("M3c fixture support", M3C_FIXTURE_SUPPORT_BOUNDS, M3C_FIXTURE_BOUNDS),
        ("M3c fixture air", M3C_FIXTURE_AIR_BOUNDS, M3C_FIXTURE_BOUNDS),
        ("M3d fixture support", M3D_FIXTURE_SUPPORT_BOUNDS, M3D_FIXTURE_BOUNDS),
        ("M3d fixture air", M3D_FIXTURE_AIR_BOUNDS, M3D_FIXTURE_BOUNDS),
        ("deck floor", DECK_FLOOR_BOUNDS, DECK_BOUNDS),
        ("deck air", DECK_AIR_BOUNDS, DECK_BOUNDS),
    ):
        if not within(inner[0], outer) or not within(inner[1], outer):
            raise ValueError(f"{label} mutation outside declared bounds")
    for label, position, bounds in (
        ("sentinel support", SENTINEL_SUPPORT, SENTINEL_BOUNDS),
        ("sentinel frame", SENTINEL_FRAME, SENTINEL_BOUNDS),
        ("sentinel gap", SENTINEL_GAP, SENTINEL_BOUNDS),
        ("sentinel control", SENTINEL_CONTROL, SENTINEL_BOUNDS),
        ("pose floor", POSE_FLOOR, DECK_BOUNDS),
        ("pose feet", POSE_FEET, DECK_BOUNDS),
        ("pose head", POSE_HEAD, DECK_BOUNDS),
    ):
        if not within(position, bounds):
            raise ValueError(f"{label} mutation outside declared bounds: {position}")


def load_profile_contract() -> dict[str, Any]:
    try:
        profile = json.loads(PROFILE_PATH.read_text(encoding="utf-8"))
    except (OSError, json.JSONDecodeError) as exception:
        raise ValueError(f"cannot read exact M3a profile: {PROFILE_PATH}") from exception
    if (
        profile.get("schemaVersion") != 3
        or profile.get("modId") != "ae2"
        or profile.get("version") != "19.2.17"
        or profile.get("coverageMilestone") != "M3a"
    ):
        raise ValueError("gallery requires the exact AE2 19.2.17 M3a profile")
    center_parts = profile.get("supportedCenterParts")
    resources = profile.get("textures")
    core_resources = profile.get("coreTextures")
    drive_resources = profile.get("driveTextures")
    if not isinstance(center_parts, list) or len(center_parts) != EXPECTED_CENTER_PART_COUNT:
        raise ValueError("exact M3a profile must contain 85 center-part IDs")
    if not isinstance(resources, list) or len(resources) != EXPECTED_PROFILE_RESOURCE_COUNT:
        raise ValueError("exact M3a profile must contain 158 runtime texture keys")
    if (
        not isinstance(core_resources, list)
        or len(core_resources) != EXPECTED_CORE_PROFILE_RESOURCE_COUNT
        or not isinstance(drive_resources, list)
        or len(drive_resources) != EXPECTED_DRIVE_PROFILE_RESOURCE_COUNT
        or resources != core_resources + drive_resources
    ):
        raise ValueError("exact M3a profile texture partitions differ from gallery")
    if len(set(center_parts)) != len(center_parts) or len(set(resources)) != len(resources):
        raise ValueError("exact M3a profile contains duplicate IDs or textures")
    expected_ids = [
        cable_id(family["key"], color_prefix)
        for family in FAMILIES
        for color_prefix, _texture_name in COLORS
    ]
    if center_parts != expected_ids:
        raise ValueError("exact M3a profile center-part order/content differs from gallery")
    if profile.get("supportedFaceParts") != [
        {"id": TERMINAL_PART_ID, "spins": [0, 1, 2, 3]}
    ]:
        raise ValueError("exact M3a profile terminal contract differs from gallery")
    if profile.get("facadePolicy") != {
        "blockState": {"Name": STONE_BLOCK_ID},
        "properties": "forbidden",
        "maximumFacades": 1,
        "requiredSameFacePart": TERMINAL_PART_ID,
    }:
        raise ValueError("exact M3a profile facade contract differs from gallery")
    expected_drive = {
        "blockId": DRIVE_BLOCK_ID,
        "slotCount": DRIVE_SLOT_COUNT,
        "baseModel": DRIVE_BASE_MODEL,
        "emptyCellModel": DRIVE_EMPTY_CELL_MODEL,
        "explicitCellModels": DRIVE_EXPLICIT_CELL_MODELS,
        "genericCellModel": {
            "model": DRIVE_GENERIC_CELL_MODEL,
            "itemIds": list(DRIVE_GENERIC_CELL_IDS),
        },
        "occupiedModelCount": DRIVE_OCCUPIED_MODEL_COUNT,
        "ledPolicy": DRIVE_LED_POLICY,
        "unknownCellPolicy": DRIVE_UNKNOWN_CELL_POLICY,
    }
    if profile.get("supportedDrive") != expected_drive:
        raise ValueError("exact M3a profile drive contract differs from gallery")
    if set(drive_resources) != set(DRIVE_BASE_MATERIAL_TRIANGLES) | {
        DRIVE_CELL_TEXTURE
    }:
        raise ValueError("exact M3a profile drive texture closure differs from gallery")
    return profile


def load_m3_completion_profile_contract() -> dict[str, Any]:
    try:
        profile_bytes = M3_COMPLETION_PROFILE_PATH.read_bytes()
        profile = json.loads(profile_bytes.decode("utf-8"))
    except (OSError, json.JSONDecodeError) as exception:
        raise ValueError(
            f"cannot read exact M3f profile: {M3_COMPLETION_PROFILE_PATH}"
        ) from exception
    if (
        len(profile_bytes) != 9_405
        or hashlib.sha256(profile_bytes).hexdigest()
        != "281a335d3024ebbb97c6268e768826c467d6f7ea660989fd3dae204c6c03abf3"
        or
        profile.get("schemaVersion") != 1
        or profile.get("profileId") != M3_COMPLETION_ROUTE
        or profile.get("modId") != "ae2"
        or profile.get("version") != "19.2.17"
        or profile.get("coverageMilestone") != "M3f"
        or profile.get("artifact") != "appliedenergistics2-19.2.17.jar"
        or profile.get("sizeBytes") != 8_230_896
        or profile.get("sha1") != "49c18d6a4af487957d7e5a6ad5dcbf71090b8e14"
        or profile.get("sha256")
        != "460d779a0609b81409907d9956de8f6f70a1b0912257e3e5c3c7e75ac9630e95"
        or profile.get("source", {}).get("tag") != "neoforge/v19.2.17"
        or profile.get("source", {}).get("commit")
        != "79ee2c704ad62941a426c26b1cb1f76ef5b2ee5a"
        or profile.get("source", {}).get("sha256")
        != "d2f451203cb61c2d21fae52c683083d2f72441ca7d26725f4df5934290492e6a"
        or profile.get("sha512")
        != "55edfd948366aff620881e0625e48c333a2cb847e73249bc0b588efbc4b86709992a8ffbca97ea387e270df4186fe7f74ee2f27b739f1c952e932becfb9dea33"
        or profile.get("minecraft") != "1.21.1"
        or profile.get("neoforge") != "21.1.234"
    ):
        raise ValueError("gallery requires the exact pinned AE2 19.2.17 M3f route")
    source = profile.get("source")
    if (
        not isinstance(source, dict)
        or source.get("tag") != "neoforge/v19.2.17"
        or source.get("commit") != "79ee2c704ad62941a426c26b1cb1f76ef5b2ee5a"
        or source.get("sha256")
        != "d2f451203cb61c2d21fae52c683083d2f72441ca7d26725f4df5934290492e6a"
    ):
        raise ValueError("M3f exact source correlation changed")
    if (
        set(profile.get("emittedStaticTextures", ()))
        != set(M3_COMPLETION_RESOURCES)
        or len(profile.get("emittedStaticTextures", ()))
        != EXPECTED_M3F_EMITTED_RESOURCE_COUNT
        or set(profile.get("fallbackOnlyTextures", ()))
        != {
            "ae2:block/sky_stone_block",
            "ae2:block/smooth_sky_stone_block",
        }
        or profile.get("resourcePartition", {}).get("pathCount") != 33
        or profile.get("resourcePartition", {}).get("emittedStaticTextureCount")
        != EXPECTED_M3F_EMITTED_RESOURCE_COUNT
        or profile.get("requiredResourcesManifestSha256")
        != "3faf7f29e2878f5525541bad855cbc66b6d45786dc8fc6ee29a6fbbf4878cca1"
        or profile.get("emittedStaticTextureManifestSha256")
        != "4652a3110adac720845b559b990dabd32e55887d43bc113f85856052bd0a8a05"
        or profile.get("fallbackTextureManifestSha256")
        != "aaff6681328dfc441a01f5a014182e914a82598395b7a594809b4652281a1146"
    ):
        raise ValueError("M3f route resource partition differs from gallery")
    supported_ids = [entry.get("id") for entry in profile.get("supportedBlocks", ())]
    if supported_ids != [
        PAINT_BLOCK_ID,
        SKY_STONE_CHEST_BLOCK_ID,
        SMOOTH_SKY_STONE_CHEST_BLOCK_ID,
        CRANK_BLOCK_ID,
        INSCRIBER_BLOCK_ID,
        SPATIAL_PYLON_BLOCK_ID,
    ]:
        raise ValueError("M3f supported block order/content differs from gallery")
    return profile


def validate_native_structural_endpoint_profile_contract(
    endpoints: Any,
    endpoint_side_policy: Any = None,
) -> None:
    """Validate all 30 endpoint rows against an independent source table."""

    if not isinstance(endpoints, list) or len(endpoints) != 30:
        raise ValueError("S1 native endpoint catalog differs from gallery")
    expected_endpoint_types = {
        f"ae2:{name}": family.upper()
        for family, names in NATIVE_STRUCTURAL_ENDPOINTS.items()
        for name in names
    }
    computed_state_counts = {
        name: native_structural_state_schema_count(
            NATIVE_STRUCTURAL_ENDPOINT_STATE_SCHEMAS[name]
        )
        for name, _family in NATIVE_STRUCTURAL_ENDPOINTS_ORDERED
    }
    if (
        computed_state_counts != NATIVE_STRUCTURAL_ENDPOINT_STATE_COUNTS
        or sum(computed_state_counts.values())
        != NATIVE_STRUCTURAL_ENDPOINT_STATE_CARTESIAN_COUNT
        or sum(computed_state_counts.values()) * len(DIRECTION_DELTAS)
        != NATIVE_STRUCTURAL_ENDPOINT_STATE_SIDE_CARTESIAN_COUNT
    ):
        raise ValueError("S1 native endpoint Cartesian state closure changed")
    for entry, (name, family) in zip(
        endpoints, NATIVE_STRUCTURAL_ENDPOINTS_ORDERED, strict=True
    ):
        policy = NATIVE_STRUCTURAL_ENDPOINT_POLICIES[name]
        if (
            not isinstance(entry, dict)
            or entry.get("id") != f"ae2:{name}"
            or entry.get("blockEntityId") != policy["block_entity_id"]
            or entry.get("cableType") != family.upper()
            or entry.get("sideRule") != policy["side_rule"]
            or entry.get("stateProperties")
            != NATIVE_STRUCTURAL_ENDPOINT_STATE_SCHEMAS[name]
            or entry.get("blockstateSha256")
            != NATIVE_STRUCTURAL_ENDPOINT_BLOCKSTATE_SHA256[name]
            or entry.get("topologyClass") != "native-grid-node-host"
        ):
            raise ValueError(
                f"S1 native endpoint profile row differs for ae2:{name}"
            )
    if (
        {entry.get("id"): entry.get("cableType") for entry in endpoints}
        != expected_endpoint_types
        or hashlib.sha256(
            json.dumps(
                [entry.get("blockEntityClass") for entry in endpoints],
                sort_keys=True,
                separators=(",", ":"),
            ).encode("utf-8")
        ).hexdigest()
        != "67a0abc524d7202ba2d9c5d1039fcf082f76c2ff819b7aee6274da600dbb9346"
    ):
        raise ValueError("S1 native endpoint catalog differs from gallery")
    if endpoint_side_policy is not None and (
        not isinstance(endpoint_side_policy, dict)
        or endpoint_side_policy.get("stateCartesianCount")
        != NATIVE_STRUCTURAL_ENDPOINT_STATE_CARTESIAN_COUNT
        or endpoint_side_policy.get("stateSideCartesianCount")
        != NATIVE_STRUCTURAL_ENDPOINT_STATE_SIDE_CARTESIAN_COUNT
    ):
        raise ValueError("S1 native endpoint Cartesian profile closure differs")


def validate_native_structural_facade_profile_contract(
    render_policy: Any,
) -> None:
    """Bind the expanded source-faithful facade/profile policy."""

    if not isinstance(render_policy, dict):
        raise ValueError("S1 facade render policy is missing")
    facades = render_policy.get("facades")
    eligibility = facades.get("eligibility") if isinstance(facades, dict) else None
    if not isinstance(eligibility, dict):
        raise ValueError("S1 facade eligibility policy is missing")
    expected_neutral_states = [
        {
            "blockId": block_id,
            "properties": NATIVE_STRUCTURAL_FACADE_WHITELIST_NEUTRAL_STATES[
                block_id
            ],
            "solidRender": NATIVE_STRUCTURAL_FACADE_WHITELIST_SOLID_RENDER[
                block_id
            ],
            "sameStateSkipRendering": (
                NATIVE_STRUCTURAL_FACADE_WHITELIST_SAME_STATE_SKIP_RENDERING[
                    block_id
                ]
            ),
        }
        for block_id in NATIVE_STRUCTURAL_FACADE_WHITELIST_IDS
    ]
    expected_same_state_skip = [
        {"blockId": block_id, "skipRendering": skip_rendering}
        for block_id, skip_rendering in (
            NATIVE_STRUCTURAL_FACADE_WHITELIST_SAME_STATE_SKIP_RENDERING.items()
        )
    ]
    expected_ordinary_skip = [
        {"blockId": block_id, "skipRendering": skip_rendering}
        for block_id, skip_rendering in (
            NATIVE_STRUCTURAL_ORDINARY_FACADE_SAME_STATE_SKIP_RENDERING.items()
        )
    ]
    expected_state_schemas = [
        {
            "blockId": block_id,
            "properties": NATIVE_STRUCTURAL_FACADE_WHITELIST_STATE_SCHEMAS[
                block_id
            ],
            "blockstateSha256": (
                NATIVE_STRUCTURAL_FACADE_WHITELIST_BLOCKSTATE_SHA256[block_id]
            ),
        }
        for block_id in NATIVE_STRUCTURAL_FACADE_WHITELIST_IDS
    ]
    if (
        hashlib.sha256(
            json.dumps(
                expected_state_schemas,
                sort_keys=True,
                separators=(",", ":"),
            ).encode("utf-8")
        ).hexdigest()
        != NATIVE_STRUCTURAL_FACADE_WHITELIST_STATE_CONTRACT_SHA256
    ):
        raise ValueError("S1 gallery facade state-schema contract changed")
    computed_state_counts = {
        block_id: native_structural_state_schema_count(
            NATIVE_STRUCTURAL_FACADE_WHITELIST_STATE_SCHEMAS[block_id]
        )
        for block_id in NATIVE_STRUCTURAL_FACADE_WHITELIST_IDS
    }
    if (
        computed_state_counts
        != NATIVE_STRUCTURAL_FACADE_WHITELIST_STATE_COUNTS
        or sum(computed_state_counts.values())
        != NATIVE_STRUCTURAL_FACADE_WHITELIST_STATE_CARTESIAN_COUNT
        or sum(
            computed_state_counts[block_id]
            for block_id, solid_render in (
                NATIVE_STRUCTURAL_FACADE_WHITELIST_SOLID_RENDER.items()
            )
            if solid_render
        )
        != NATIVE_STRUCTURAL_FACADE_SOLID_RENDER_TRUE_CARTESIAN_COUNT
        or sum(
            computed_state_counts[block_id]
            for block_id, skip_rendering in (
                NATIVE_STRUCTURAL_FACADE_WHITELIST_SAME_STATE_SKIP_RENDERING.items()
            )
            if skip_rendering
        )
        != NATIVE_STRUCTURAL_FACADE_SAME_STATE_SKIP_TRUE_CARTESIAN_COUNT
    ):
        raise ValueError("S1 gallery facade Cartesian state closure changed")

    def expected_native_state_policy(block_id: str) -> dict[str, Any]:
        if block_id in {"ae2:quartz_glass", "ae2:quartz_vibrant_glass"}:
            domains: dict[str, list[str]] = {}
            normalization: dict[str, str] = {}
        elif block_id == "ae2:controller":
            domains = {
                "state": ["offline", "online", "conflicted"],
                "type": [
                    "block",
                    "column_x",
                    "column_y",
                    "column_z",
                    "inside_a",
                    "inside_b",
                ],
            }
            normalization = {"state": "offline", "type": "block"}
        elif block_id == "ae2:crafting_monitor":
            domains = {
                "facing": _NATIVE_ENDPOINT_FACING_DOMAIN,
                "formed": _NATIVE_ENDPOINT_BOOLEAN_DOMAIN,
                "powered": _NATIVE_ENDPOINT_BOOLEAN_DOMAIN,
                "spin": _NATIVE_ENDPOINT_SPIN_DOMAIN,
            }
            normalization = {
                "facing": "preserve",
                "formed": "false",
                "powered": "false",
                "spin": "0",
            }
        else:
            domains = _NATIVE_ENDPOINT_FORMED_POWERED_SCHEMA
            normalization = {"formed": "false", "powered": "false"}
        return {
            "validPropertyValues": domains,
            "normalization": normalization,
            "unknownOrMalformed": "atomic-original-resource-fallback",
            "galleryPropertiesAreNeutralSampleOnly": True,
        }

    expected_native_materials = [
        {
            "blockId": entry["block_id"],
            "properties": entry["properties"],
            "materialFamily": entry["material_family"],
            "sourceModel": entry["source_model"],
            "blockstateSha256": entry["blockstate_sha256"],
            "solidRender": entry["block_id"]
            not in {"ae2:quartz_glass", "ae2:quartz_vibrant_glass"},
            "transparentFacade": entry["block_id"]
            in {"ae2:quartz_glass", "ae2:quartz_vibrant_glass"},
            "blockStateLightEmission": (
                15 if entry["block_id"] == "ae2:quartz_vibrant_glass" else 0
            ),
            "facadeQuadLightEmission": 0,
            "statePolicy": expected_native_state_policy(entry["block_id"]),
        }
        for entry in NATIVE_STRUCTURAL_NEUTRAL_FACADE_MATERIALS
    ]
    if (
        facades.get("policy")
        != "all-six-face-masks-per-instance-valid-static-block-state-material"
        or facades.get("maximumFacades") != 6
        or facades.get("maskCount") != 64
        or facades.get("material") != "per-instance-valid-static-BlockState"
        or eligibility.get("item") != "component-free-non-air-BlockItem"
        or eligibility.get("state")
        != "default-state-at-creation-then-valid-property-cycles"
        or eligibility.get("renderShape") != "MODEL"
        or eligibility.get("blockEntity") != "disallowed-unless-whitelisted"
        or eligibility.get("collision") != "full-block-unless-whitelisted"
        or eligibility.get("whitelistResourceSha256")
        != "4ff52f9d8670417406c29430f754305198ba8ab855ca34336962d6d24cf49f82"
        or eligibility.get("whitelistBlockCount") != 24
        or eligibility.get("whitelistBlocks")
        != list(NATIVE_STRUCTURAL_FACADE_WHITELIST_IDS)
        or eligibility.get("neutralDefaultStateCount") != 24
        or eligibility.get("neutralDefaultStates") != expected_neutral_states
        or eligibility.get("stateSchemaCount") != 24
        or eligibility.get("stateSchemaPolicy")
        != NATIVE_STRUCTURAL_FACADE_WHITELIST_STATE_POLICY
        or eligibility.get("stateSchemas") != expected_state_schemas
        or eligibility.get("stateCartesianCount")
        != NATIVE_STRUCTURAL_FACADE_WHITELIST_STATE_CARTESIAN_COUNT
        or eligibility.get("stateClassificationPolicy")
        != NATIVE_STRUCTURAL_FACADE_STATE_CLASSIFICATION_POLICY
        or eligibility.get("solidRenderTrueCartesianCount")
        != NATIVE_STRUCTURAL_FACADE_SOLID_RENDER_TRUE_CARTESIAN_COUNT
        or eligibility.get("sameStateSkipRenderingTrueCartesianCount")
        != NATIVE_STRUCTURAL_FACADE_SAME_STATE_SKIP_TRUE_CARTESIAN_COUNT
        or eligibility.get("sameStateSkipRenderingCount") != 24
        or eligibility.get("sameStateSkipRendering") != expected_same_state_skip
        or eligibility.get("ordinarySkipRenderingControlCount") != 3
        or eligibility.get("ordinarySkipRenderingControls")
        != expected_ordinary_skip
        or eligibility.get("skipRenderingPolicy")
        != NATIVE_STRUCTURAL_FACADE_SKIP_RENDERING_POLICY
        or eligibility.get("quartzSkipRenderingPolicy")
        != NATIVE_STRUCTURAL_FACADE_QUARTZ_SKIP_RENDERING_POLICY
        or eligibility.get("optionalTagCount") != 1
        or eligibility.get("optionalTags") != ["c:glass_blocks"]
        or eligibility.get("nativeNeutralMaterialCount") != 11
        or eligibility.get("ordinaryMaterialPolicy")
        != NATIVE_STRUCTURAL_FACADE_ORDINARY_MATERIAL_POLICY
        or eligibility.get("tintPolicy") != NATIVE_STRUCTURAL_FACADE_TINT_POLICY
        or eligibility.get("uvReinterpolationPolicy")
        != NATIVE_STRUCTURAL_FACADE_UV_REINTERPOLATION_POLICY
        or eligibility.get("cardinalVariantTransformPolicy")
        != NATIVE_STRUCTURAL_FACADE_CARDINAL_VARIANT_TRANSFORM_POLICY
        or eligibility.get("weightedVariantPolicy")
        != NATIVE_STRUCTURAL_FACADE_WEIGHTED_VARIANT_POLICY
        or eligibility.get("nativeNeutralMaterialScope")
        != "11-ae2-native-neutral-resource-pins-not-the-complete-facade-support-set"
        or eligibility.get("nativeNeutralMaterials") != expected_native_materials
        or eligibility.get("unprovenEligibility")
        != "atomic-original-resource-fallback"
        or facades.get("unsupportedTransientModels")
        != "atomic-original-resource-fallback"
    ):
        raise ValueError("S1 expanded facade eligibility differs from gallery")

    expected_quartz_dependency = {
        "profileId": "ae2-quartz-glass",
        "profileSha256": "548e5bc00ef07c6d6b93b346422b596882ec11ca03de006065fa45fecb991200",
        "resourceManifestSha256": "b51c708e7c4d26093c1b6f85b88d0be50572d3cfa76dbf802720f6ad79c7a7fa",
        "textureManifestSha256": "65005c9b76800cdeba5c4598472a44dea131c9974672f89bf421452755fefb6a",
        "textureCount": 19,
        "textureSemanticAlgorithm": (
            "decoded-width-height-argb-scanline-animation-meta-sha256-v1"
        ),
        "textureSemanticSha256": "c51ced2667879b8b298400c81805cf7d4459b5ac88c36350bca7bb6ca2bfef50",
        "glassState": {
            "neighborPositionAxes": 6,
            "neighborAppearance": (
                "adjacent-real-quartz-or-neighbor-cable-bus-facade-appearance-through-"
                "RENDERING_FACADE_DIRECTION"
            ),
            "sameCableBusOtherFacade": "not-a-GlassState-input",
            "localPerpendicularFacades": "FacadeBuilder-mask-and-transparent-inset-only",
            "visibleFace": "suppressed-for-adjacent-connected-glass",
            "frameMask": "per-visible-face-four-tangent-neighbor-tests",
        },
        "facadeQuadEmission": (
            "zero-for-quartz-and-vibrant-vibrant-source-block-light-is-15"
        ),
        "higherPriorityOverridePolicy": (
            "exact-semantic-match-or-atomic-original-resource-fallback"
        ),
    }
    expected_source_parity = {
        "thinThicknessBlocks": "1/16-0.002",
        "thinThicknessSixteenths": 0.968,
        "transparentClassification": "not-BlockState.isSolidRender-level-position",
        "transparentPerpendicularOpaqueInset": (
            "one-thin-thickness-on-each-masked-perpendicular-bound"
        ),
        "opaquePerpendicularFaceStripping": "facade-direction-bit-mask",
        "opaquePerpendicularInnerCornerKick": "one-thin-thickness",
        "cornerKickSourceEpsilon": {"unit": "block", "value": 0.00001},
        "cornerKickRuntimeEpsilon": {"unit": "sixteenth", "value": 0.00016},
        "cornerKickAnalyzerEpsilon": {"unit": "block", "value": 0.00001},
        "cardinalVariantTransform": (
            NATIVE_STRUCTURAL_FACADE_CARDINAL_VARIANT_TRANSFORM_POLICY
        ),
        "uvAfterClamp": NATIVE_STRUCTURAL_FACADE_UV_REINTERPOLATION_POLICY,
        "quadShade": "source-BakedQuad.isShade",
        "quadShadeHostProjection": (
            "source-shade-bit-semantic-locked-host-prbm-has-no-per-quad-shade-channel"
        ),
        "shadeFalseEligibility": "accepted-not-a-fallback-reason",
        "lightEmissionHostProjection": "represented",
        "quadAmbientOcclusion": "source-BakedQuad.hasAmbientOcclusion",
        "ambientOcclusionDirection": NATIVE_STRUCTURAL_FACADE_AO_DIRECTION_POLICY,
        "mapColorIllumination": NATIVE_STRUCTURAL_MAP_COLOR_ILLUMINATION_POLICY,
        "quadTint": NATIVE_STRUCTURAL_FACADE_TINT_POLICY,
        "weightedVariants": NATIVE_STRUCTURAL_FACADE_WEIGHTED_VARIANT_POLICY,
        "adjacentCull": NATIVE_STRUCTURAL_FACADE_SKIP_RENDERING_POLICY,
        "quartzCrossFamilySkipRendering": (
            NATIVE_STRUCTURAL_FACADE_QUARTZ_SKIP_RENDERING_POLICY
        ),
        "cutoutStripAabbNormalization": NATIVE_STRUCTURAL_FACADE_CUTOUT_AABB_POLICY,
        "outputCullFace": "retain-only-when-original-cull-face-equals-facade-side",
    }
    if (
        facades.get("quartzGlassDependency") != expected_quartz_dependency
        or facades.get("sourceParityGolden") != expected_source_parity
    ):
        raise ValueError("S1 facade semantic source parity differs from gallery")

    collision = render_policy.get("facadeCutoutCollision")
    part_policies = (
        collision.get("partPolicies") if isinstance(collision, dict) else None
    )
    if (
        not isinstance(part_policies, dict)
        or set(part_policies)
        != {f"ae2:{name}" for name, _group in NATIVE_STRUCTURAL_PARTS}
        or collision.get("unit") != "sixteenths"
        or collision.get("orientation")
        != "BusCollisionHelper-installed-face-local-basis"
        or collision.get("selection")
        != "union-of-every-installed-part-box-intersecting-current-facade-slab"
        or collision.get("stripAabbConstruction")
        != NATIVE_STRUCTURAL_FACADE_CUTOUT_AABB_POLICY
        or part_policies.get("ae2:quartz_fiber")
        != {"mode": "static", "boxes": [[6, 6, 10, 10, 10, 16]]}
        or part_policies.get("ae2:toggle_bus")
        != {"mode": "static", "boxes": [[6, 6, 11, 10, 10, 16]]}
        or part_policies.get("ae2:import_bus", {}).get("boxes")
        != [[6, 6, 11, 10, 10, 13], [5, 5, 13, 11, 11, 14], [4, 4, 14, 12, 12, 16]]
        or part_policies.get("ae2:export_bus", {}).get("boxes")
        != [[4, 4, 12, 12, 12, 14], [5, 5, 14, 11, 11, 15], [6, 6, 15, 10, 10, 16], [6, 6, 11, 10, 10, 12]]
        or part_policies.get("ae2:level_emitter")
        != {"mode": "static", "boxes": [[7, 7, 11, 9, 9, 16]]}
        or part_policies.get("ae2:energy_level_emitter")
        != {"mode": "static", "boxes": [[7, 7, 11, 9, 9, 16]]}
        or part_policies.get("ae2:cable_anchor")
        != {
            "mode": "same-side-facade-conditioned",
            "withoutSameSideFacade": [[7, 7, 10, 9, 9, 16]],
            "withSameSideFacade": [[7, 7, 10, 9, 9, 14]],
        }
    ):
        raise ValueError("S1 facade collision policy differs from gallery")

    expected_collision_bound_bits_by_face = {
        "down": {"minX": "right", "maxX": "left", "minY": "down", "maxY": "up"},
        "up": {"minX": "left", "maxX": "right", "minY": "up", "maxY": "down"},
        "north": {"minX": "left", "maxX": "right", "minY": "down", "maxY": "up"},
        "south": {"minX": "left", "maxX": "right", "minY": "down", "maxY": "up"},
        "west": {"minX": "right", "maxX": "left", "minY": "down", "maxY": "up"},
        "east": {"minX": "right", "maxX": "left", "minY": "down", "maxY": "up"},
    }
    expected_plane_masks = {
        "count": 16,
        "bits": {"left": 1, "down": 2, "right": 4, "up": 8},
        "compatibleNeighbor": "same-concrete-part-class-same-installed-face",
        "coordinateSpaces": (
            "mask-bits-are-PlaneConnections-front-view-logical;"
            "renderedGeometryBoundBits-are-PlaneBakedModel-visual-local-before-"
            "QuadRotator-installed-world-transform;collisionBoundBitsByInstalledFace-"
            "are-BusCollisionHelper-installed-face-local;never-reuse-bounds-across-"
            "coordinate-spaces"
        ),
        "collisionBoundBitsByInstalledFace": expected_collision_bound_bits_by_face,
        "renderedGeometryBoundBits": {
            "minX": "right",
            "maxX": "left",
            "minY": "down",
            "maxY": "up",
        },
        "facadeCutout": {
            "baseLocalBounds": [1, 1, 15, 15],
            "coordinateSpace": "BusCollisionHelper-installed-face-local",
            "boundBitsByInstalledFace": expected_collision_bound_bits_by_face,
            "minXExtendsTo": 0,
            "minYExtendsTo": 0,
            "maxXExtendsTo": 16,
            "maxYExtendsTo": 16,
        },
    }
    expected_dynamic_sheet = {
        "base": [1, 1, 15, 15, 15, 16],
        "coordinateSpace": "BusCollisionHelper-installed-face-local",
        "boundBitsByInstalledFace": expected_collision_bound_bits_by_face,
        "minXExtendsTo": 0,
        "minYExtendsTo": 0,
        "maxXExtendsTo": 16,
        "maxYExtendsTo": 16,
    }
    if (
        render_policy.get("planeConnectionMasks") != expected_plane_masks
        or part_policies.get("ae2:annihilation_plane", {}).get("dynamicSheet")
        != expected_dynamic_sheet
        or part_policies.get("ae2:formation_plane", {}).get("dynamicSheet")
        != expected_dynamic_sheet
        or render_policy.get("glassCoreOverrides")
        != {
            "defaultDesiredCableType": "GLASS",
            "overrideDesiredCableType": "SMART",
            "partIds": ["ae2:level_emitter", "ae2:energy_level_emitter"],
            "effect": "glass-center-promoted-to-covered-core",
        }
    ):
        raise ValueError("S1 plane/core source policy differs from gallery")


def validate_native_structural_unknown_endpoint_profile_contract(
    value: Any,
) -> None:
    if not isinstance(value, dict):
        raise ValueError("S1 unsupported compatible endpoint catalog is missing")
    entries = value.get("entries")
    artifacts = value.get("artifacts")
    representative = value.get("representativeControl")
    if (
        value.get("policy") != "unknown-atomic-original-resource-fallback"
        or value.get("count") != 67
        or not isinstance(entries, list)
        or len(entries) != 67
        or len({entry.get("id") for entry in entries}) != 67
        or any(
            entry.get("policy") != "unknown-atomic-original-resource-fallback"
            for entry in entries
        )
        or not isinstance(artifacts, list)
        or sum(artifact.get("endpointCount", 0) for artifact in artifacts) != 67
        or hashlib.sha256(
            json.dumps(entries, sort_keys=True, separators=(",", ":")).encode(
                "utf-8"
            )
        ).hexdigest()
        != NATIVE_STRUCTURAL_UNKNOWN_ENDPOINT_ENTRIES_SHA256
        or hashlib.sha256(
            json.dumps(artifacts, sort_keys=True, separators=(",", ":")).encode(
                "utf-8"
            )
        ).hexdigest()
        != NATIVE_STRUCTURAL_UNKNOWN_ENDPOINT_ARTIFACTS_SHA256
        or representative
        != {
            "blockId": "expandedae:exp_io_port",
            "blockEntityId": "expandedae:exp_io_port",
            "artifact": "expandedae-2.1.1",
            "properties": {"facing": "north", "powered": "false", "spin": "0"},
            "blockstateSha256": "9880448f15a4372dbfdda591d3728518df3433e7e5886cbe0b4366d74b55a76d",
            "endpointOffsetFromCable": "east",
            "contactSide": "west",
            "connectionEvidence": (
                "ExpIOPortBlockEntity-subclasses-AE2-IOPortBlockEntity-all-sides-grid-node"
            ),
            "expectedBranch": "unknown-atomic-original-resource-fallback",
        }
        or not any(
            entry.get("id") == "expandedae:exp_io_port"
            and entry.get("blockEntityId") == "expandedae:exp_io_port"
            and entry.get("artifact") == "expandedae-2.1.1"
            for entry in entries
        )
    ):
        raise ValueError("S1 unsupported compatible endpoint catalog differs")


def load_native_structural_profile_contract() -> dict[str, Any]:
    try:
        profile_bytes = NATIVE_STRUCTURAL_PROFILE_PATH.read_bytes()
        profile = json.loads(profile_bytes.decode("utf-8"))
    except (OSError, json.JSONDecodeError) as exception:
        raise ValueError(
            f"cannot read exact S1 profile: {NATIVE_STRUCTURAL_PROFILE_PATH}"
        ) from exception
    if (
        len(profile_bytes) != S1_PROFILE_SIZE_BYTES
        or hashlib.sha256(profile_bytes).hexdigest()
        != S1_PROFILE_SHA256
        or profile.get("schemaVersion") != 10
        or profile.get("profileId") != NATIVE_STRUCTURAL_ROUTE
        or profile.get("coverageMilestone") != "S1"
        or profile.get("artifact") != "appliedenergistics2-19.2.17.jar"
        or profile.get("sizeBytes") != 8_230_896
        or profile.get("sha256")
        != "460d779a0609b81409907d9956de8f6f70a1b0912257e3e5c3c7e75ac9630e95"
    ):
        raise ValueError("gallery requires the exact pinned S1 structural profile")
    face_parts = profile.get("nativeFaceParts")
    if (
        not isinstance(face_parts, list)
        or [entry.get("id") for entry in face_parts]
        != [f"ae2:{name}" for name, _group in NATIVE_STRUCTURAL_PARTS]
        or [entry.get("group") for entry in face_parts]
        != [group for _name, group in NATIVE_STRUCTURAL_PARTS]
        or [entry.get("id").removeprefix("ae2:") for entry in face_parts if entry.get("spin") != "ignored"]
        != [name for name, _group in NATIVE_STRUCTURAL_PARTS if name in NATIVE_STRUCTURAL_SPIN_PARTS]
        or [entry.get("id") for entry in face_parts if entry.get("denseCenter")]
        != ["ae2:cable_anchor"]
    ):
        raise ValueError("S1 native face-part catalog differs from gallery")
    validate_native_structural_endpoint_profile_contract(
        profile.get("nativeEndpoints"), profile.get("endpointSidePolicy")
    )
    orientation = profile.get("orientationPolicy")
    closure = profile.get("resourceClosure")
    render_policy = profile.get("renderPolicy")
    endpoint_side_policy = profile.get("endpointSidePolicy")
    if (
        orientation
        != {
            "faces": 6,
            "persistedSpinValues": [0, 1, 2, 3],
            "spinCapablePartCount": 9,
            "spinIgnoredPartCount": 20,
            "exhaustiveStateCount": NATIVE_STRUCTURAL_ORIENTATION_STATE_COUNT,
            "formula": "20*6+9*6*4",
        }
        or not isinstance(closure, dict)
        or closure.get("directNeutralModelRootCount")
        != NATIVE_STRUCTURAL_DIRECT_RESOURCE_COUNT
        or closure.get("transitiveJsonCount")
        != NATIVE_STRUCTURAL_TRANSITIVE_JSON_COUNT
        or closure.get("pngCount") != NATIVE_STRUCTURAL_PNG_COUNT
        or closure.get("pathCount") != NATIVE_STRUCTURAL_RESOURCE_COUNT
        or closure.get("totalBytes") != 51_306
        or closure.get("requiredResourcesManifestSha256")
        != NATIVE_STRUCTURAL_RESOURCE_MANIFEST_SHA256
        or closure.get("requiredResourceSizesManifestSha256")
        != "a79e93baef3f5d923730686fcc4de05ec30c8b7765aef8b32aaf871f9c4f3869"
        or closure.get("liveSemanticGate")
        != {
            "blueMapVersion": "5.22",
            "modelCount": 43,
            "modelAlgorithm": (
                "resolved-parent-applied-elements-faces-ao-shade-light-uv-texture-"
                "cull-rotation-tint-float-bits-sha256-v1"
            ),
            "modelSha256": "aefa42ad8427e8f2ac5b9f1c88807c978617d6ff70768a32223616b970b54251",
            "textureCount": 56,
            "textureAlgorithm": (
                "decoded-width-height-argb-scanline-animation-meta-sha256-v1"
            ),
            "textureSha256": "1bee2b2917edf3d1eb9ee24505f47a7377665da753f107ec1af9170d783bc833",
            "higherPriorityOverridePolicy": (
                "exact-semantic-match-or-atomic-original-resource-fallback"
            ),
        }
        or render_policy.get("transientState") != "static-off-inactive-unlocked"
        or render_policy.get("denseCenterParts") != ["ae2:cable_anchor"]
        or render_policy.get("p2pFrequency")
        != "persisted-frequency-static-glyph"
        or render_policy.get("deviceConnections")
        != "AECableType.min-local-and-endpoint"
        or render_policy.get("mapColorIllumination")
        != NATIVE_STRUCTURAL_MAP_COLOR_ILLUMINATION_POLICY
        or render_policy.get("fallback")
        != "missing-malformed-or-capped-atomic-original-resource-fallback"
        or render_policy.get("contentsItemsFluidsActivityAndDriveLeds")
        != "excluded"
        or not isinstance(endpoint_side_policy, dict)
        or endpoint_side_policy.get("serializedBlockEntityId")
        != "exact-required"
        or endpoint_side_policy.get("persistedBlockState")
        != "exact-complete-key-set-and-serialized-value-domain-required"
        or endpoint_side_policy.get("stateSchemaCount") != 30
        or endpoint_side_policy.get("stateCartesianCount")
        != NATIVE_STRUCTURAL_ENDPOINT_STATE_CARTESIAN_COUNT
        or endpoint_side_policy.get("stateSideCartesianCount")
        != NATIVE_STRUCTURAL_ENDPOINT_STATE_SIDE_CARTESIAN_COUNT
        or endpoint_side_policy.get("blockstateResourceDigestCount") != 30
        or endpoint_side_policy.get("unknownBlockEntityStateOrTopology")
        != "missing-malformed-or-capped-atomic-original-resource-fallback"
        or endpoint_side_policy.get("ruleKindCount") != 8
        or endpoint_side_policy.get("ruleCounts")
        != {
            "ALL": 12,
            "BACK": 2,
            "NO_FRONT": 3,
            "FRONT_BACK": 1,
            "PUSH_DIRECTION": 1,
            "FORMED_CRAFTING": 8,
            "FORMED_QUANTUM": 2,
            "VALID_STRAIGHT_PYLON": 1,
        }
        or endpoint_side_policy.get("branchPolicies")
        != {
            "malformedNativeEndpoint": (
                "malformed-native-endpoint-atomic-original-resource-fallback"
            ),
            "knownExtensionExactBlockAndBlockEntity": (
                "unknown-atomic-original-resource-fallback"
            ),
            "knownExtensionBlockWithMissingOrWrongBlockEntity": (
                "malformed-known-extension-observation-atomic-original-resource-fallback"
            ),
            "unrelatedBlockWithCatalogBlockEntity": "disconnected",
            "unrelatedNonNativeBlockEntity": "disconnected",
        }
    ):
        raise ValueError("S1 structural policy/resource closure differs from gallery")
    validate_native_structural_facade_profile_contract(render_policy)
    validate_native_structural_unknown_endpoint_profile_contract(
        profile.get("knownUnsupportedCompatibleEndpoints")
    )
    validate_native_structural_companion_identities()
    return profile


def validate_native_structural_companion_identities() -> None:
    """Bind current companions and the accepted-S1 semantic projection."""

    parsed: dict[str, dict[str, Any]] = {}
    for label, path, expected_size, expected_sha256 in (
        (
            "support matrix",
            SUPPORT_MATRIX_PATH,
            CURRENT_SUPPORT_MATRIX_SIZE_BYTES,
            CURRENT_SUPPORT_MATRIX_SHA256,
        ),
        (
            "provenance",
            PROVENANCE_PATH,
            CURRENT_PROVENANCE_SIZE_BYTES,
            CURRENT_PROVENANCE_SHA256,
        ),
    ):
        try:
            payload = path.read_bytes()
            value = json.loads(payload.decode("utf-8"))
        except (OSError, UnicodeDecodeError, json.JSONDecodeError) as exception:
            raise ValueError(f"cannot read exact S1 {label}: {path}") from exception
        if (
            len(payload) != expected_size
            or hashlib.sha256(payload).hexdigest() != expected_sha256
        ):
            raise ValueError(f"S1 {label} identity changed")
        if not isinstance(value, dict):
            raise ValueError(f"S1 {label} semantic projection changed")
        parsed[label] = value

    support = parsed["support matrix"]
    support_profiles = support.get("profiles")
    if not isinstance(support_profiles, list):
        raise ValueError("S1 support matrix semantic projection changed")
    accepted_profiles: list[dict[str, Any]] = []
    for profile_id in CURRENT_ACCEPTED_S1_PROFILE_IDS:
        matches = [
            profile
            for profile in support_profiles
            if isinstance(profile, dict) and profile.get("profileId") == profile_id
        ]
        if len(matches) != 1:
            raise ValueError("S1 support matrix semantic projection changed")
        accepted_profiles.append(matches[0])
    support_projection = {
        "schemaVersion": support.get("schemaVersion"),
        "baseline": support.get("baseline"),
        "acceptedProfiles": accepted_profiles,
    }
    support_projection_sha256 = hashlib.sha256(
        (
            json.dumps(
                support_projection,
                sort_keys=True,
                separators=(",", ":"),
                ensure_ascii=True,
            )
            + "\n"
        ).encode("utf-8")
    ).hexdigest()
    if support_projection_sha256 != CURRENT_ACCEPTED_S1_SUPPORT_PROJECTION_SHA256:
        raise ValueError("S1 support matrix semantic projection changed")

    provenance = parsed["provenance"]
    provenance_projection_keys = (
        "schema_version",
        "project_license",
        "source_use_lane",
        "baseline",
        "shared_component_decision",
        "ae2",
        "extendedae",
        "minecraft_resource_evidence",
        "bluemap",
        "local_adaptations",
        "non_bundled_dependencies",
    )
    if any(key not in provenance for key in provenance_projection_keys):
        raise ValueError("S1 provenance semantic projection changed")
    provenance_projection = {
        key: provenance[key] for key in provenance_projection_keys
    }
    provenance_projection_sha256 = hashlib.sha256(
        (
            json.dumps(
                provenance_projection,
                sort_keys=True,
                separators=(",", ":"),
                ensure_ascii=True,
            )
            + "\n"
        ).encode("utf-8")
    ).hexdigest()
    if (
        provenance_projection_sha256
        != CURRENT_ACCEPTED_S1_PROVENANCE_PROJECTION_SHA256
    ):
        raise ValueError("S1 provenance semantic projection changed")


def native_structural_texture_resources() -> list[str]:
    manifest_path = NATIVE_STRUCTURAL_PROFILE_PATH.with_name(
        "required-resources.tsv"
    )
    if (
        hashlib.sha256(manifest_path.read_bytes()).hexdigest()
        != "a79e93baef3f5d923730686fcc4de05ec30c8b7765aef8b32aaf871f9c4f3869"
    ):
        raise ValueError("S1 required-resource size manifest changed")
    resources: list[str] = []
    for line in manifest_path.read_text(encoding="utf-8").splitlines():
        path, _size, _digest = line.split("\t")
        if not path.endswith(".png"):
            continue
        relative = path.removeprefix("assets/").removesuffix(".png")
        namespace, texture = relative.split("/textures/", 1)
        resources.append(f"{namespace}:{texture}")
    if len(resources) != NATIVE_STRUCTURAL_PNG_COUNT or len(set(resources)) != len(resources):
        raise ValueError("S1 required texture resource closure changed")
    return resources


def load_native_structural_oracle(
    cases: Iterable[dict[str, Any]],
) -> dict[tuple[int, int, int], dict[str, Any]]:
    """Load the compiled-runtime oracle for the frozen 351 custom S1 anchors."""
    oracle_bytes = NATIVE_STRUCTURAL_ORACLE_PATH.read_bytes()
    if (
        len(oracle_bytes) != S1_ORACLE_SIZE_BYTES
        or hashlib.sha256(oracle_bytes).hexdigest() != S1_ORACLE_SHA256
    ):
        raise ValueError("S1 compiled-runtime oracle identity changed")
    payload = json.loads(oracle_bytes.decode("utf-8"))
    if (
        not isinstance(payload, dict)
        or set(payload)
        != {
            "anchors",
            "coverage_id",
            "profile_id",
            "schema_version",
            "signature_schema_version",
        }
        or payload.get("schema_version") != 2
        or payload.get("profile_id") != NATIVE_STRUCTURAL_ROUTE
        or payload.get("coverage_id") != NATIVE_STRUCTURAL_COVERAGE
        or payload.get("signature_schema_version") != 10
        or not isinstance(payload.get("anchors"), dict)
    ):
        raise ValueError("S1 compiled-runtime oracle header changed")
    expected_positions = {
        anchor["position"]
        for case in cases
        for anchor in case["anchors"]
        if anchor["expected_path"] == "custom-s1"
    }
    parsed: dict[tuple[int, int, int], dict[str, Any]] = {}
    for key, entry in payload["anchors"].items():
        try:
            position = tuple(int(value) for value in key.split())
        except (AttributeError, ValueError) as exception:
            raise ValueError("S1 oracle position key is malformed") from exception
        if (
            len(position) != 3
            or key != " ".join(str(value) for value in position)
            or position in parsed
            or not isinstance(entry, dict)
        ):
            raise ValueError("S1 oracle has a duplicate or malformed entry")
        materials = entry.get("material_triangles")
        triangle_count = entry.get("triangle_count")
        if (
            set(entry)
            != {
                "geometry_signature",
                "material_triangles",
                "nonlighting_attribute_signature",
                "triangle_count",
            }
            or not isinstance(triangle_count, int)
            or isinstance(triangle_count, bool)
            or triangle_count <= 0
            or not isinstance(materials, dict)
            or not materials
            or any(
                not isinstance(resource, str)
                or not isinstance(count, int)
                or isinstance(count, bool)
                or count <= 0
                for resource, count in materials.items()
            )
            or triangle_count != sum(materials.values())
            or not isinstance(entry.get("geometry_signature"), str)
            or len(entry["geometry_signature"]) != 64
            or any(character not in "0123456789abcdef" for character in entry["geometry_signature"])
            or not isinstance(entry.get("nonlighting_attribute_signature"), str)
            or len(entry["nonlighting_attribute_signature"]) != 64
            or any(
                character not in "0123456789abcdef"
                for character in entry["nonlighting_attribute_signature"]
            )
        ):
            raise ValueError(f"S1 oracle entry is malformed at {position}")
        parsed[position] = {
            "triangle_count": entry["triangle_count"],
            "material_triangles": dict(sorted(materials.items())),
            "geometry_signature": entry["geometry_signature"],
            "nonlighting_attribute_signature": entry[
                "nonlighting_attribute_signature"
            ],
        }
    identity_union = {
        resource
        for entry in parsed.values()
        for resource in entry["material_triangles"]
    }
    if (
        set(parsed) != expected_positions
        or len(parsed) != S1_ORACLE_ANCHOR_COUNT
        or sum(entry["triangle_count"] for entry in parsed.values())
        != S1_ORACLE_TRIANGLE_COUNT
        or len(identity_union) != S1_ORACLE_IDENTITY_COUNT
        or sum(len(entry["material_triangles"]) for entry in parsed.values())
        != S1_ORACLE_MATERIAL_ROW_COUNT
    ):
        raise ValueError("S1 oracle exact anchor/material/triangle closure changed")
    return parsed


CASES = create_cases()
enrich_and_validate_cases(CASES)

S1_LIVE_PERSISTENT_FALLBACK_REASONS = {
    (233, 100, 343): "invalid-reporting-spin-monitor",
    (260, 100, 358): "invalid-reporting-spin-semi-dark-monitor",
    (263, 100, 358): "invalid-reporting-spin-terminal-multipart",
    (266, 100, 358): "invalid-reporting-spin-dark-monitor",
    (269, 100, 358): "invalid-reporting-spin-pattern-encoding-terminal",
    (272, 100, 358): "non-full-cube-facade",
    (275, 100, 358): "invalid-reporting-spin-crafting-terminal",
    (278, 100, 358): "invalid-reporting-spin-storage-monitor",
    (287, 100, 358): "known-compatible-extension-endpoint-unknown",
}
S1_UNIT_ONLY_MALFORMED_CASES = (
    "native-non-anchor-part-on-dense-cable",
    "native-unknown-face-part-id",
    "native-p2p-missing-frequency",
    "native-malformed-facade-state",
    "native-facade-without-center",
    "native-retained-field-budget-exceeded",
)
S1_UNIT_ONLY_REASON = (
    "AE2 sanitizes these malformed fields during live block-entity loading; "
    "their S1 atomic-fallback boundary is frozen by exact Java decoder and "
    "bounded DTO tests. Physical S1 fallback evidence instead uses persistent "
    "invalid reporting-part spins, one non-full-cube facade, and one exact "
    "known-compatible extension host."
)


def validate_s1_live_fixture_policy(cases: list[dict[str, Any]]) -> None:
    anchors = {
        anchor["position"]: anchor
        for case in cases
        if case["milestone"] == "S1"
        for anchor in case["anchors"]
    }
    fallback_positions = {
        position
        for position, anchor in anchors.items()
        if anchor.get("expected_path") == "stock-fallback-s1"
    }
    if fallback_positions != set(S1_LIVE_PERSISTENT_FALLBACK_REASONS):
        raise ValueError("S1 live persistent fallback positions changed")
    for position, reason in S1_LIVE_PERSISTENT_FALLBACK_REASONS.items():
        anchor = anchors[position]
        if (
            anchor.get("expected_path") != "stock-fallback-s1"
            or anchor.get("fallback_reason") != reason
            or anchor.get("expected_triangle_count") != 0
        ):
            raise ValueError(
                f"S1 persistent live fallback contract changed at {position}"
            )


validate_s1_live_fixture_policy(CASES)
CASE_COUNT = len(CASES)
ANCHOR_COUNT = sum(len(case["anchors"]) for case in CASES)

M45_ROUTES = (
    "appflux",
    "merequester",
    "expandedae",
    "megacells",
    "advanced-ae-quantum",
    "advanced-ae-athena",
    "extendedae-matrix",
    "extendedae-planes",
)
M45_RUNTIME_COVERAGE = "m45-cumulative-runtime"
M45_RUNTIME_ORACLE_SIZE_BYTES = 221_769
M45_RUNTIME_ORACLE_SHA256 = (
    "c2ce69bed949306551ca4ff6cdebf7fac88f0f2f2fa7ab294d3312f363e1b448"
)
M45_RUNTIME_ORACLE_ANCHOR_COUNT = 391
M45_RUNTIME_ORACLE_TRIANGLE_COUNT = 23_334
M45_RUNTIME_ORACLE_IDENTITY_COUNT = 122
M45_RUNTIME_ORACLE_MATERIAL_ROW_COUNT = 2_089
M45_RUNTIME_ROUTE_ANCHOR_COUNTS = {
    "appflux": 11,
    "merequester": 36,
    "expandedae": 101,
    "megacells": 106,
    "advanced-ae-quantum": 44,
    "advanced-ae-athena": 9,
    "extendedae-matrix": 42,
    "extendedae-planes": 42,
}
M45_SCHEMA10_LEGACY_COVERAGE = "m45-schema10-legacy-upgrades"
M45_SCHEMA10_LEGACY_ORACLE_SIZE_BYTES = 2_336
M45_SCHEMA10_LEGACY_ORACLE_SHA256 = (
    "2319ecf576ba07b123078c720d941990fac939033d375e5853f51bf98348c3c7"
)
M45_SCHEMA10_LEGACY_ORACLE_ANCHOR_COUNT = 3
M45_SCHEMA10_LEGACY_ORACLE_TRIANGLE_COUNT = 282
M45_SCHEMA10_LEGACY_ORACLE_IDENTITY_COUNT = 20
M45_SCHEMA10_LEGACY_ORACLE_MATERIAL_ROW_COUNT = 26
M45_COLORABLE_DRIVE_COLORS = (
    "WHITE",
    "LIGHT_GRAY",
    "GRAY",
    "BLACK",
    "LIME",
    "YELLOW",
    "ORANGE",
    "BROWN",
    "RED",
    "PINK",
    "MAGENTA",
    "PURPLE",
    "BLUE",
    "LIGHT_BLUE",
    "CYAN",
    "GREEN",
    "TRANSPARENT",
)
M45_COLORABLE_DRIVE_POSITIONS = tuple(
    (424 + 2 * index, 100, 334) for index in range(17)
)
M45_REQUESTER_BLOCK_POSITIONS = tuple(
    (368 + 2 * index, 100, 312) for index in range(12)
)
M45_REQUESTER_TERMINAL_POSITIONS = tuple(
    (368 + 2 * (index % 12), 100, 320 + 2 * (index // 12))
    for index in range(24)
)
M45_ADVANCED_ISOLATED_POSITIONS = tuple(
    (416 + 2 * index, 100, 370) for index in range(8)
)
M45_ADVANCED_PHYSICAL_POSITIONS = tuple(
    (x, y, z)
    for y in range(100, 103)
    for z in range(376, 379)
    for x in range(416, 420)
)
M45_ADVANCED_ROLE_IDS = (
    "advanced_ae:quantum_unit",
    "advanced_ae:quantum_core",
    "advanced_ae:quantum_storage_128",
    "advanced_ae:quantum_storage_256",
    "advanced_ae:data_entangler",
    "advanced_ae:quantum_accelerator",
    "advanced_ae:quantum_multi_threader",
    "advanced_ae:quantum_structure",
)
M45_EXTENDED_ISOLATED_POSITIONS = (
    (448, 100, 370),
    *((448 + 2 * index, 100, 374) for index in range(5)),
)
M45_EXTENDED_PHYSICAL_POSITIONS = tuple(
    (x, y, z)
    for y in range(100, 103)
    for z in range(378, 381)
    for x in range(456, 460)
)
M45_EXTENDED_MATRIX_ROLE_IDS = (
    "extendedae:assembler_matrix_frame",
    "extendedae:assembler_matrix_wall",
    "extendedae:assembler_matrix_glass",
    "extendedae:assembler_matrix_pattern",
    "extendedae:assembler_matrix_crafter",
    "extendedae:assembler_matrix_speed",
)
M45_ADVANCED_ARTIFACT = {
    "artifact": "AdvancedAE-1.6.12-1.21.1.jar",
    "version": "1.6.12-1.21.1",
    "size_bytes": 4_791_255,
    "sha256": "a01d9718667ac13899013e91c5b0b7708b9b9db1da9b8e380772dde54bbe8f41",
    "source_correlation": (
        "exact-jar-authoritative-no-artifact-correlated-immutable-source"
    ),
}
M45_ADVANCED_SINGLETON_POSITION = (418, 100, 370)
M45_ADVANCED_SINGLETON_MODEL_EXCEPTION = {
    "policy": "exact-upstream-hidden-face-baggage",
    "scope": "this-anchor-only-in-every-renderer-mode",
    "model": {
        "path": "assets/advanced_ae/models/block/quantum_core.json",
        "size_bytes": 6_603,
        "sha256": (
            "078ba9642093937173784a328e56aeba95deb7cca84ec89aa193ce44cf0edb2e"
        ),
        "element_count": 11,
        "declared_face_count": 66,
        "missing_texture_face_count": 16,
        "fully_occluded_missing_texture_face_count": 16,
    },
    "occlusion_policy": (
        "each-missing-face-is-fully-covered-by-one-touching-sibling-element"
    ),
    "expected_material_triangles": {
        "advanced_ae:block/crafting/quantum_core": 96,
        "advanced_ae:block/crafting/quantum_core_out": 4,
        "bluemap:block/missing": 32,
    },
}
M45_ATHENA_ARTIFACT = {
    "artifact": "athena-neoforge-1.21.1-4.0.6.jar",
    "version": "4.0.6",
    "size_bytes": 99_944,
    "sha256": "43699885bbce3343916d4c5c4940cf0e3f9f6f02fdeb46e8655e121b42282ec5",
}
M45_EXTENDED_ARTIFACT = {
    "artifact": "ExtendedAE-1.21-2.2.35-neoforge.jar",
    "version": "1.21-2.2.35-neoforge",
    "size_bytes": 5_578_031,
    "sha256": "14a2860fa2c747e9dda2279b8933fac6311fecfee166c765171022b902591c65",
    "source_tag": "1.21-2.2.35-neoforge",
    "source_commit": "3776bc854458301bbcc9a44a8238d70a0e3dc00d",
}
M45_NATIVE_CENTER_PROJECTION_RESOURCES = (
    "ae2:part/cable/core/covered/transparent",
    "ae2:part/cable/covered/transparent",
)
M45_EXTENDED_PLANE_DEPENDENCY_RESOURCES = (
    "ae2:part/plane_sides",
    "ae2:part/transition_plane_back",
)
M45_EXTENDED_PLANE_HOST_RESOURCES = (
    "ae2:part/cable/core/covered/transparent",
    "ae2:part/cable/covered/transparent",
    "ae2:part/monitor_sides_status",
    "ae2:part/monitor_sides_status_off",
)
M45_EXTENDED_PLANE_LIVE_OBSERVATION = {
    "capture_id": "atm120-enabled-cold-live-map-2026-08-11",
    "anchor_count": 42,
    "triangle_count": 3_244,
    "material_triangles": {
        "ae2:part/cable/core/covered/transparent": 504,
        "ae2:part/cable/covered/transparent": 1_144,
        "ae2:part/monitor_sides_status": 336,
        "ae2:part/monitor_sides_status_off": 672,
        "ae2:part/plane_sides": 336,
        "ae2:part/transition_plane_back": 168,
        "extendedae:part/active_formation_plane": 42,
        "extendedae:part/smart_annihilation_plane": 42,
    },
}
M45_REQUESTER_ORIGINAL_MATERIAL_TRIANGLES = {
    "ae2:block/generics/back": 2,
    "ae2:block/generics/bottom": 2,
    "ae2:block/generics/side": 4,
    "ae2:block/generics/top": 2,
    "merequester:block/requester": 18,
}
M45_EXP_IO_ORIGINAL_MATERIAL_TRIANGLES = {
    "false": {
        "expandedae:block/exp_io_port_front_off": 2,
        "expandedae:block/exp_io_port_top_off": 2,
        "expandedae:block/generics/back": 2,
        "expandedae:block/generics/bottom": 2,
        "expandedae:block/generics/side": 4,
    },
    "true": {
        "ae2:block/generics/back": 2,
        "ae2:block/generics/bottom": 2,
        "expandedae:block/exp_io_port_front": 2,
        "expandedae:block/exp_io_port_top": 2,
        "expandedae:block/generics/side": 4,
    },
}
M45_DISABLED_PROJECTION_EVIDENCE = {
    "schema_version": 1,
    "capture": {
        "baseline": "All the Mons 1.2.0",
        "capture_id": "atm120-m45-disabled-cold-warm-live-map-2026-08-11",
        "mode": "m45-disabled",
        "cold_warm_comparable_bytes_equal": True,
        "settings_sha256": (
            "17399bd20bb0d0b133711565302b74090a6b3c6907c270af3ba4d9d3b0556410"
        ),
        "textures": {
            "compressed_sha256": (
                "6cbb1db509369c386892424b9d9201a2010b0bc07fc49ae0c2311d055c543b36"
            ),
            "payload_sha256": (
                "09deae0a21f9c14afee2b131cc42d9e91abadd1841dca8a0cfb28d03efb217ae"
            ),
            "texture_count": 43_153,
        },
        "m45_anchor_count": 409,
        "m45_nonempty_anchor_count": 299,
        "m45_empty_anchor_count": 110,
        "m45_triangle_count": 4_296,
        "m45_resource_count": 33,
        "full_map_triangle_count": 69_244,
        "full_map_resource_count": 316,
    },
    "combined_projection": {
        "anchor_count": 409,
        "nonempty_anchor_count": 299,
        "empty_anchor_count": 110,
    },
    "physical_stock_projection": {
        "anchor_count": 409,
        "nonempty_anchor_count": 110,
        "empty_anchor_count": 299,
    },
    "crafting_disabled_projection": {
        "affected_anchor_count": 20,
        "nonempty_anchor_count": 0,
        "empty_anchor_count": 20,
    },
    "single_route_projections": {
        "appflux": {"affected_anchor_count": 11, "nonempty_anchor_count": 8, "empty_anchor_count": 3},
        "merequester": {"affected_anchor_count": 36, "nonempty_anchor_count": 36, "empty_anchor_count": 0},
        "expandedae": {"affected_anchor_count": 119, "nonempty_anchor_count": 79, "empty_anchor_count": 40},
        "megacells": {"affected_anchor_count": 108, "nonempty_anchor_count": 87, "empty_anchor_count": 21},
        "advanced-ae-quantum": {"affected_anchor_count": 44, "nonempty_anchor_count": 7, "empty_anchor_count": 37},
        "advanced-ae-athena": {"affected_anchor_count": 9, "nonempty_anchor_count": 9, "empty_anchor_count": 0},
        "extendedae-matrix": {"affected_anchor_count": 42, "nonempty_anchor_count": 31, "empty_anchor_count": 11},
        "extendedae-planes": {"affected_anchor_count": 42, "nonempty_anchor_count": 42, "empty_anchor_count": 0},
    },
    "exact_empty_original_resource_classes": [
        {
            "route": "expandedae",
            "selector": "formed=true extension crafting blocks",
            "anchor_count": 22,
            "distinct_model_count": 21,
            "model_path_pattern": "assets/expandedae/models/block/crafting/*_formed.json",
            "model_size_bytes": 2,
            "model_sha256": "44136fa355b3678a1146ad16f7e8649e94fb4fc21fe77e8310c060f61caaff8a",
            "model_payload": "{}",
        },
        {
            "route": "megacells",
            "selector": "formed=true extension crafting blocks",
            "anchor_count": 9,
            "distinct_model_count": 8,
            "model_path_pattern": "assets/megacells/models/block/crafting/*_formed.json",
            "model_size_bytes": 2,
            "model_sha256": "44136fa355b3678a1146ad16f7e8649e94fb4fc21fe77e8310c060f61caaff8a",
            "model_payload": "{}",
        },
        {
            "route": "advanced-ae-quantum",
            "selector": "34 formed structures, formed multiblocked core/storage-128, and isolated structure",
            "anchor_count": 37,
            "model_path_pattern": "assets/advanced_ae/models/block/{crafting/,}quantum_*_formed.json",
            "model_size_bytes": 3,
            "model_sha256": "ca3d163bab055381827226140568f3bef7eaac187cebd76878e0b63e9e442356",
            "model_payload": "{}\n",
            "quantum_core_blockstate_path": "assets/advanced_ae/blockstates/quantum_core.json",
            "quantum_core_blockstate_size_bytes": 933,
            "quantum_core_blockstate_sha256": "9c32d450356e1c88c4dba5b7bc8bf0edea243e70e86d1111a59b378657d89bb2",
        },
        {
            "route": "extendedae-matrix",
            "selector": "all formed and unformed assembler matrix glass blocks",
            "anchor_count": 11,
            "model_path": "assets/extendedae/models/block/assembler_matrix_glass.json",
            "model_size_bytes": 53,
            "model_sha256": "27e03c3bcb9ca0b37fbe9b084c895f5e9062bf113fce892031d92b6dec9f4170",
            "loader": "extendedae:assembler_matrix_glass",
        },
    ],
    "exact_inherited_models": {
        "merequester:requester": {
            "anchor_count": 12,
            "model_path": "assets/merequester/models/block/requester.json",
            "model_size_bytes": 2_104,
            "model_sha256": "065177ccd37084d41d9da4e06759c883067164ce5dbbb966ccb2f9ac0ea452bb",
            "expected_material_triangles": M45_REQUESTER_ORIGINAL_MATERIAL_TRIANGLES,
        },
        "expandedae:exp_io_port[powered=false]": {
            "anchor_count": 24,
            "model_path": "assets/expandedae/models/block/exp_io_port.json",
            "model_size_bytes": 1_628,
            "model_sha256": "ded81529f3e48c4774f54067286d10722c6f57afef287086187020f2a74329d6",
            "expected_material_triangles": M45_EXP_IO_ORIGINAL_MATERIAL_TRIANGLES["false"],
        },
        "expandedae:exp_io_port[powered=true]": {
            "anchor_count": 24,
            "model_path": "assets/expandedae/models/block/exp_io_port_on.json",
            "model_size_bytes": 269,
            "model_sha256": "44275c3d508a48c861ceffb36279e670da1811fd76592b3b59ece9a9ae060baf",
            "parent_model": "ae2:block/spatial_io_port",
            "expected_material_triangles": M45_EXP_IO_ORIGINAL_MATERIAL_TRIANGLES["true"],
        },
    },
}
M45_LEGACY_UPGRADE_COVERAGE = "m45-legacy-route-upgrades"
M45_LEGACY_UPGRADE_CAPTURE = {
    "baseline": "All the Mons 1.2.0",
    "capture_id": "atm120-enabled-cold-warm-live-map-2026-08-11",
    "mode": "enabled-cold-warm",
    "cold_warm_oracle_bytes_equal": True,
    "settings_sha256": (
        "36061d2da93f02a0051debeced44e780d28c196baa4e5995a0e9c572f084fe5b"
    ),
    "textures": {
        "compressed_sha256": (
            "0e101af510bcd421e3dae4fb930d418cdd7c092efa7c67e6c1c99b8c907d86dd"
        ),
        "payload_sha256": (
            "c1cd190623b35058e793111c74dc26a2736821d3b975234cf23f0e9756ca2b30"
        ),
        "texture_count": 43_220,
    },
    "validation_policy": (
        "exact-runtime-map-geometry-material-nonlighting-v11"
    ),
}
M45_LEGACY_UPGRADE_SPECS = (
    {
        "case_id": "ae2-m3-14",
        "position": (241, 100, 249),
        "required_m45_routes": ("megacells",),
        "required_legacy_routes": (),
        "expected_path": "stock-fallback-m3",
        "fallback_reason": "unsupported-drive-cell-id",
        "source_kind": "ae2-drive-megacells-cell",
        "observation": {
            "tile": {"x": 7, "z": 7},
            "compressed_sha256": (
                "aef7ad0a9d65253bb8f861739c125be46953f4d0891ea9fc81b08cbc8742be20"
            ),
            "payload_sha256": (
                "63fd1bcc5cf95972eea21c1f25fb996bc6c380a2db76ac510f5c6c578d23c2ed"
            ),
            "tile_triangle_count": 7_672,
            "triangle_count": 112,
            "material_triangles": {
                "ae2:block/drive/drive_front": 38,
                "ae2:block/drive/drive_inside": 16,
                "ae2:block/drive/drive_inside_bottom": 2,
                "ae2:block/drive/drive_inside_top": 10,
                "ae2:block/generics/back": 6,
                "ae2:block/generics/bottom": 2,
                "ae2:block/generics/front": 8,
                "ae2:block/generics/side": 16,
                "ae2:block/generics/top": 2,
                "megacells:block/drive/cells/standard_cell": 6,
                "megacells:block/drive/cells/standard_cell_tiers": 6,
            },
        },
    },
    {
        "case_id": "ae2-m3b-13",
        "position": (266, 100, 266),
        "required_m45_routes": ("megacells",),
        "required_legacy_routes": ("extension",),
        "expected_path": "stock-fallback-m3b",
        "fallback_reason": "unsupported-drive-cell-id",
        "source_kind": "extended-drive-megacells-cell",
        "observation": {
            "tile": {"x": 8, "z": 8},
            "compressed_sha256": (
                "cc9ef452c58d95e42c03b76915ae4332ff0eb5f8a43ad82541427a8236298ec8"
            ),
            "payload_sha256": (
                "caddd4540d1d5233461bb70bc3f7e508930a4be7ad30eafd77d00bd4a5ddf39f"
            ),
            "tile_triangle_count": 3_238,
            "triangle_count": 138,
            "material_triangles": {
                "ae2:block/drive/drive_front": 66,
                "ae2:block/drive/drive_inside_top": 8,
                "ae2:block/generics/bottom": 2,
                "ae2:block/generics/top": 2,
                "extendedae:block/extended_drive/drive_inside": 16,
                "extendedae:block/extended_drive/ex_drive_bottom": 2,
                "extendedae:block/extended_drive/ex_drive_side": 8,
                "extendedae:block/extended_drive/ex_drive_top": 2,
                "extendedae:block/generics/front": 8,
                "extendedae:block/generics/side": 12,
                "megacells:block/drive/cells/standard_cell": 6,
                "megacells:block/drive/cells/standard_cell_tiers": 6,
            },
        },
    },
    {
        "case_id": "ae2-m3d-09",
        "position": (318, 100, 261),
        "required_m45_routes": ("expandedae", "megacells"),
        "required_legacy_routes": ("crafting",),
        "expected_path": "stock-fallback-m3d",
        "fallback_reason": "compatible-extension-crafting-neighbor",
        "source_kind": "native-crafting-expanded-mega-peer-connection",
        "observation": {
            "tile": {"x": 9, "z": 8},
            "compressed_sha256": (
                "75faa6ee158d4d3a08e6a12de3d946a4fe5eec78f179395a08e4f7927089639f"
            ),
            "payload_sha256": (
                "d4904ae3568b7cc416a4daa183f2e66a0ee3992cd399704c62da337b36b748d6"
            ),
            "tile_triangle_count": 7_190,
            "triangle_count": 32,
            "material_triangles": {
                "ae2:block/crafting/1k_storage_light": 8,
                "ae2:block/crafting/light_base": 8,
                "ae2:block/crafting/ring_side_hor": 16,
            },
        },
    },
)


def _m45_profile_json(relative: str) -> dict[str, Any]:
    value = json.loads((M45_PROFILE_ROOT / relative).read_text(encoding="utf-8"))
    if not isinstance(value, dict):
        raise ValueError(f"M4/M5 profile is not an object: {relative}")
    return value


def _m45_texture_resources(*relatives: str) -> tuple[str, ...]:
    resources: set[str] = set()
    for relative in relatives:
        path = M45_PROFILE_ROOT / relative
        for line in path.read_text(encoding="utf-8").splitlines():
            asset_path = line.split("\t", 1)[0]
            if (
                not asset_path.startswith("assets/")
                or "/textures/" not in asset_path
                or not asset_path.endswith(".png")
            ):
                continue
            namespace, texture = asset_path.removeprefix("assets/").split(
                "/textures/", 1
            )
            resources.add(f"{namespace}:{texture.removesuffix('.png')}")
    if not resources:
        raise ValueError("M4/M5 resource partition contains no textures")
    return tuple(sorted(resources))


def _m45_extended_texture_partitions() -> tuple[tuple[str, ...], tuple[str, ...]]:
    resources = _m45_texture_resources(
        "extendedae/1.21-2.2.35-neoforge/m5-required-resources.tsv"
    )
    matrix = tuple(
        resource for resource in resources if ":block/assembler_matrix/" in resource
    )
    planes = tuple(resource for resource in resources if ":part/" in resource)
    if len(matrix) != 13 or len(planes) != 4 or set(matrix) & set(planes):
        raise ValueError("ExtendedAE matrix/plane texture partition changed")
    return matrix, planes


def _m45_grid(
    min_x: int,
    max_x: int,
    min_z: int,
    count: int,
    *,
    y: int = 100,
    spacing: int = 2,
) -> tuple[tuple[int, int, int], ...]:
    columns = ((max_x - min_x) // spacing) + 1
    return tuple(
        (
            min_x + (index % columns) * spacing,
            y,
            min_z + (index // columns) * spacing,
        )
        for index in range(count)
    )


def _m45_exact_empty_original_resource(
    route: str,
    block_id: str,
    block_state: dict[str, str] | None,
) -> bool:
    state = block_state or {}
    if route in {"expandedae", "megacells"}:
        return state.get("formed") == "true"
    if route == "advanced-ae-quantum":
        return block_id == "advanced_ae:quantum_structure" or (
            state.get("formed") == "true"
            and state.get("multiblocked") == "true"
            and block_id
            in {
                "advanced_ae:quantum_core",
                "advanced_ae:quantum_storage_128",
            }
        )
    if route == "extendedae-matrix":
        return block_id == "extendedae:assembler_matrix_glass"
    return False


def _m45_exact_inherited_projection_materials(
    anchor: dict[str, Any], projection: dict[str, Any]
) -> dict[str, int] | None:
    if (
        projection.get("review_projection") != "nonempty"
        or projection.get("expected_path")
        not in {"stock-original-m45", "physical-stock-original-m45"}
    ):
        return None
    if anchor["block_id"] == "merequester:requester":
        return dict(M45_REQUESTER_ORIGINAL_MATERIAL_TRIANGLES)
    if anchor["block_id"] == "expandedae:exp_io_port":
        powered = anchor.get("block_state", {}).get("powered")
        if powered not in M45_EXP_IO_ORIGINAL_MATERIAL_TRIANGLES:
            raise ValueError("M4/M5 I/O Port projection lacks exact powered state")
        return dict(M45_EXP_IO_ORIGINAL_MATERIAL_TRIANGLES[powered])
    return None


def _m45_anchor(
    position: tuple[int, int, int],
    block_id: str,
    route: str,
    *,
    block_state: dict[str, str] | None = None,
    expected_path: str = "custom-m45",
    projection: str = "nonempty",
    fallback_reason: str | None = None,
    expected_block_entity_id: str | None = None,
    synthetic: bool = False,
    **fields: Any,
) -> dict[str, Any]:
    if route not in M45_ROUTES:
        raise ValueError(f"unknown M4/M5 route: {route}")
    exact_empty_original = _m45_exact_empty_original_resource(
        route, block_id, block_state
    )
    physical_nonempty = not exact_empty_original and block_id not in {
        "ae2:cable_bus",
        DRIVE_BLOCK_ID,
        EXTENDED_DRIVE_BLOCK_ID,
        "expandedae:colorable_drive",
    }
    route_disabled_nonempty = not exact_empty_original and block_id not in {
        DRIVE_BLOCK_ID,
        EXTENDED_DRIVE_BLOCK_ID,
        "expandedae:colorable_drive",
    }
    route_disabled_path = (
        "native-center-only-m45"
        if block_id == "ae2:cable_bus"
        else "stock-original-m45"
    )
    anchor: dict[str, Any] = {
        "position": position,
        "block_id": block_id,
        "cable_id": fields.pop("cable_id", None),
        "expected_path": expected_path,
        "m45_route": route,
        "review_projection": projection,
        "route_disabled_projections": {
            route: {
                "expected_path": route_disabled_path,
                "review_projection": (
                    "nonempty" if route_disabled_nonempty else "empty"
                ),
                "reason": (
                    "inactive-face-part-is-omitted-before-native-center-render"
                    if block_id == "ae2:cable_bus"
                    else "exact-original-resource-model-is-empty"
                    if exact_empty_original
                    else "owning-route-inactive-original-resource-fallback"
                ),
            }
        },
        "physical_stock_projection": {
            "expected_path": "physical-stock-original-m45",
            "review_projection": "nonempty" if physical_nonempty else "empty",
            "reason": (
                "ordinary-exact-artifact-json-model-remains-visible"
                if physical_nonempty
                else "exact-original-resource-model-is-empty"
                if exact_empty_original
                else "dynamic-original-resource-has-no-static-stock-projection"
            ),
        },
        "native_structural_disabled_projection": {
            "expected_path": (
                "unreachable-cable-bus-stock-m45"
                if block_id == "ae2:cable_bus"
                else expected_path
            ),
            "review_projection": (
                "empty" if block_id == "ae2:cable_bus" else projection
            ),
            "reason": (
                "native-structural-core-inactive-face-lane-unreachable"
                if block_id == "ae2:cable_bus"
                else "independent-whole-block-or-drive-lane-remains-active"
            ),
        },
        "source_derived_synthetic_fixture": synthetic,
        **fields,
    }
    if block_state is not None:
        anchor["block_state"] = dict(block_state)
    if fallback_reason is not None:
        anchor["fallback_reason"] = fallback_reason
    if expected_block_entity_id is not None:
        anchor["expected_block_entity_id"] = expected_block_entity_id
    return anchor


def _m45_cable_anchor(
    position: tuple[int, int, int],
    route: str,
    direction: str,
    part: dict[str, Any],
    *,
    cable_id: str = "ae2:fluix_covered_cable",
    expected_path: str = "custom-m45",
    projection: str = "nonempty",
    fallback_reason: str | None = None,
) -> dict[str, Any]:
    return _m45_anchor(
        position,
        "ae2:cable_bus",
        route,
        expected_path=expected_path,
        projection=projection,
        fallback_reason=fallback_reason,
        cable_id=cable_id,
        face_parts={direction: dict(part)},
        facades={},
        installed_face=direction,
    )


def _m45_item(item_id: str) -> dict[str, Any]:
    return {"id": item_id, "count": 1}


def _m45_drive_anchor(
    position: tuple[int, int, int],
    route: str,
    block_id: str,
    item_ids: Iterable[str],
) -> dict[str, Any]:
    slot_count = 10 if block_id == DRIVE_BLOCK_ID else 20
    items = [_m45_item(item_id) for item_id in item_ids]
    if len(items) > slot_count:
        raise ValueError("M4/M5 Drive fixture exceeds its slot count")
    return _m45_anchor(
        position,
        block_id,
        route,
        block_state={"facing": "north", "spin": "0"},
        expected_block_entity_id=block_id,
        drive_inventory=tuple(items + [None] * (slot_count - len(items))),
        fixture_role=(
            "native-drive-extension-cells"
            if block_id == DRIVE_BLOCK_ID
            else "extended-drive-extension-cells"
        ),
    )


def validate_m45_colorable_drive_fixtures(
    cases: Iterable[dict[str, Any]],
) -> None:
    expanded_cases = [case for case in cases if case["route"] == "expandedae"]
    if len(expanded_cases) != 1:
        raise ValueError("M4/M5 ExpandedAE case closure changed")
    anchors = [
        anchor
        for anchor in expanded_cases[0]["anchors"]
        if anchor["block_id"] == "expandedae:colorable_drive"
    ]
    if [anchor["position"] for anchor in anchors] != list(
        M45_COLORABLE_DRIVE_POSITIONS
    ):
        raise ValueError("M4/M5 colorable-drive positions/order changed")
    colors: list[str] = []
    for anchor in anchors:
        expected_nbt = anchor.get("expected_nbt")
        if (
            not isinstance(expected_nbt, dict)
            or set(expected_nbt) != {"color"}
            or not isinstance(expected_nbt.get("color"), str)
            or not expected_nbt["color"]
        ):
            raise ValueError(
                "M4/M5 colorable-drive persisted color must be a nonempty string"
            )
        colors.append(expected_nbt["color"])
        if (
            anchor["expected_path"] != "stock-fallback-m45"
            or anchor["review_projection"] != "empty"
            or anchor.get("fallback_reason")
            != "upstream-colorable-drive-world-visual-unavailable"
            or anchor.get("expected_block_entity_id")
            != "expandedae:colorable_drive"
            or anchor.get("fixture_role")
            != "all-seventeen-persisted-color-values"
        ):
            raise ValueError("M4/M5 colorable-drive fallback contract changed")
    if tuple(colors) != M45_COLORABLE_DRIVE_COLORS:
        raise ValueError("M4/M5 colorable-drive exact AEColor order changed")


def _m45_exact_route_case(
    cases: Iterable[dict[str, Any]], route: str
) -> dict[str, Any]:
    matches = [case for case in cases if case.get("route") == route]
    if len(matches) != 1:
        raise ValueError(f"M4/M5 {route} case closure changed")
    return matches[0]


def validate_m45_requester_physical_fixtures(
    cases: Iterable[dict[str, Any]],
) -> None:
    case = _m45_exact_route_case(cases, "merequester")
    if (
        case.get("label")
        != "ME Requester 12 stable idle-derived block orientations and 24 terminal orientations"
        or case.get("category")
        != "idle-derived-inactive-z-normalization-and-terminal-orbits"
        or case.get("fixture_blocks") != ()
    ):
        raise ValueError("M4/M5 ME Requester physical case metadata changed")
    requester_anchors = [
        anchor
        for anchor in case["anchors"]
        if anchor["block_id"] == "merequester:requester"
    ]
    expected_requesters: list[tuple[object, ...]] = []
    positions = iter(M45_REQUESTER_BLOCK_POSITIONS)
    for facing, y_rotation in zip(
        ("north", "east", "south", "west"),
        (0, 90, 180, 270),
        strict=True,
    ):
        expected_requesters.append(
            (
                next(positions),
                {"active": "false", "facing": facing},
                "implicit-zero",
                {"x": 0, "y": y_rotation, "z": 0},
            )
        )
    for facing in ("up", "down"):
        for spin in range(4):
            expected_requesters.append(
                (
                    next(positions),
                    {"active": "false", "facing": facing, "spin": str(spin)},
                    "ae2:z-normalized-from-spin",
                    {
                        "x": 270 if facing == "up" else 90,
                        "y": 0,
                        "z": (
                            (180, 90, 0, 270)[spin]
                            if facing == "up"
                            else (0, 270, 180, 90)[spin]
                        ),
                    },
                )
            )
    requester_signatures = [
        (
            anchor["position"],
            anchor.get("block_state"),
            anchor.get("z_rotation_source"),
            anchor.get("expected_rotation_degrees"),
        )
        for anchor in requester_anchors
    ]
    if requester_signatures != expected_requesters or any(
        anchor.get("expected_block_entity_id") != "merequester:requester"
        or anchor.get("fixture_role")
        != "idle-derived-inactive-requester-orientation"
        or anchor.get("source_derived_synthetic_fixture")
        or "placement_state" in anchor
        for anchor in requester_anchors
    ):
        raise ValueError(
            "M4/M5 ME Requester must contain exactly 12 stable idle-derived orientations"
        )

    terminal_anchors = [
        anchor
        for anchor in case["anchors"]
        if anchor["block_id"] == "ae2:cable_bus"
    ]
    expected_terminals = [
        (position, direction, spin)
        for position, (direction, spin) in zip(
            M45_REQUESTER_TERMINAL_POSITIONS,
            (
                (direction, spin)
                for direction in DIRECTION_DELTAS
                for spin in range(4)
            ),
            strict=True,
        )
    ]
    terminal_signatures = []
    for anchor in terminal_anchors:
        face_parts = anchor.get("face_parts")
        if not isinstance(face_parts, dict) or len(face_parts) != 1:
            raise ValueError("M4/M5 ME Requester terminal structure changed")
        direction, part = next(iter(face_parts.items()))
        terminal_signatures.append((anchor["position"], direction, part.get("spin")))
        if (
            part != {"id": "merequester:requester_terminal", "spin": part.get("spin")}
            or anchor.get("installed_face") != direction
        ):
            raise ValueError("M4/M5 ME Requester terminal structure changed")
    if terminal_signatures != expected_terminals or len(case["anchors"]) != 36:
        raise ValueError("M4/M5 ME Requester exact 36-anchor closure changed")


def validate_m45_expanded_physical_fixtures(
    cases: Iterable[dict[str, Any]],
) -> None:
    case = _m45_exact_route_case(cases, "expandedae")
    io_anchors = [
        anchor
        for anchor in case["anchors"]
        if anchor["block_id"] == "expandedae:exp_io_port"
    ]
    expected_io: list[tuple[object, ...]] = []
    io_positions = iter(_m45_grid(424, 446, 312, 48))
    for facing in DIRECTION_DELTAS:
        for powered in (False, True):
            for spin in range(4):
                if facing == "north":
                    rotation = (0, 0, (-90 * spin) % 360)
                elif facing == "east":
                    rotation = (0, 90, (-90 * spin) % 360)
                elif facing == "south":
                    rotation = (0, 180, (90 * spin) % 360)
                elif facing == "west":
                    rotation = (0, 270, (-90 * spin) % 360)
                elif facing == "up":
                    rotation = (270, 0, (180 - 90 * spin) % 360)
                else:
                    rotation = (90, 0, (-90 * spin) % 360)
                expected_io.append(
                    (
                        next(io_positions),
                        {
                            "facing": facing,
                            "powered": str(powered).lower(),
                            "spin": str(spin),
                        },
                        "expandedae:exp_io_port",
                        "ae2:z-normalized-from-spin",
                        dict(zip(("x", "y", "z"), rotation, strict=True)),
                    )
                )
    if [
        (
            anchor["position"],
            anchor.get("block_state"),
            anchor.get("expected_block_entity_id"),
            anchor.get("z_rotation_source"),
            anchor.get("expected_rotation_degrees"),
        )
        for anchor in io_anchors
    ] != expected_io or any(
        "placement_state" in anchor
        or "fixture_role" in anchor
        or anchor.get("source_derived_synthetic_fixture")
        for anchor in io_anchors
    ) or sum(
        anchor.get("block_state", {}).get("powered") == "true"
        for anchor in io_anchors
    ) != 24:
        raise ValueError("M4/M5 Expanded AE exact 48-state I/O Port closure changed")
    powered_positions = [
        anchor["position"]
        for anchor in io_anchors
        if anchor["block_state"]["powered"] == "true"
    ]
    expected_helpers = [
        {
            "position": (x, y - 1, z),
            "block_id": "ae2:creative_energy_cell",
            "purpose": "powered-exp-io-port-network-helper",
        }
        for x, y, z in powered_positions
    ]
    actual_helpers = [
        fixture
        for fixture in case["fixture_blocks"]
        if fixture.get("purpose") == "powered-exp-io-port-network-helper"
        or fixture.get("block_id") == "ae2:creative_energy_cell"
    ]
    if actual_helpers != expected_helpers or len(case["fixture_blocks"]) != 55:
        raise ValueError(
            "M4/M5 Expanded AE powered I/O Ports require 24 exact energy helpers"
        )
    helper_positions = {helper["position"] for helper in actual_helpers}
    if any(
        ((anchor["position"][0], 99, anchor["position"][2]) in helper_positions)
        != (anchor["block_state"]["powered"] == "true")
        for anchor in io_anchors
    ):
        raise ValueError("M4/M5 Expanded AE I/O Port helper ownership changed")


def validate_m45_advanced_quantum_physical_fixtures(
    cases: Iterable[dict[str, Any]],
) -> None:
    case = _m45_exact_route_case(cases, "advanced-ae-quantum")
    if (
        case.get("label")
        != "Advanced AE static roles plus a live-proven physical 4x3x3 quantum computer"
        or case.get("category")
        != "static-role-catalog-and-physical-valid-connected-review"
        or case.get("fixture_blocks") != ()
        or len(case["anchors"]) != 44
    ):
        raise ValueError("M4/M5 Advanced AE physical case metadata changed")
    isolated = case["anchors"][:8]
    expected_isolated = []
    for position, block_id in zip(
        M45_ADVANCED_ISOLATED_POSITIONS, M45_ADVANCED_ROLE_IDS, strict=True
    ):
        is_core = position == M45_ADVANCED_SINGLETON_POSITION
        expected_isolated.append(
            (
                position,
                block_id,
                {
                    "formed": "true" if is_core else "false",
                    "powered": "false",
                    "multiblocked": "false",
                    "light_level": "0",
                },
                (
                    "physical-valid-isolated-single-core"
                    if is_core
                    else "unformed-static-role"
                ),
            )
        )
    if [
        (
            anchor["position"],
            anchor["block_id"],
            anchor.get("block_state"),
            anchor.get("fixture_role"),
        )
        for anchor in isolated
    ] != expected_isolated:
        raise ValueError("M4/M5 Advanced AE isolated role closure changed")
    singleton = next(
        anchor
        for anchor in isolated
        if anchor["position"] == M45_ADVANCED_SINGLETON_POSITION
    )
    if (
        singleton.get("selector_scoped_model_exception")
        != M45_ADVANCED_SINGLETON_MODEL_EXCEPTION
        or any(
            "selector_scoped_model_exception" in anchor
            for anchor in case["anchors"]
            if anchor is not singleton
        )
        or (
            "assets/advanced_ae/models/block/quantum_core.json\t6603\t"
            "078ba9642093937173784a328e56aeba95deb7cca84ec89aa193ce44cf0edb2e"
        )
        not in (
            M45_PROFILE_ROOT
            / "advancedae/1.6.12/quantum-required-resources.tsv"
        ).read_text(encoding="utf-8").splitlines()
    ):
        raise ValueError(
            "M4/M5 Advanced AE singleton hidden-face model exception changed"
        )

    physical = case["anchors"][8:]
    expected_physical = []
    exact_state = {
        "formed": "true",
        "powered": "false",
        "multiblocked": "true",
        "light_level": "0",
    }
    for position in M45_ADVANCED_PHYSICAL_POSITIONS:
        block_id = (
            "advanced_ae:quantum_core"
            if position == (417, 101, 377)
            else "advanced_ae:quantum_storage_128"
            if position == (418, 101, 377)
            else "advanced_ae:quantum_structure"
        )
        expected_physical.append(
            (
                position,
                block_id,
                exact_state,
                (
                    "physical-valid-4x3x3-quantum-computer-interior"
                    if block_id != "advanced_ae:quantum_structure"
                    else "physical-valid-4x3x3-quantum-computer-boundary"
                ),
            )
        )
    if [
        (
            anchor["position"],
            anchor["block_id"],
            anchor.get("block_state"),
            anchor.get("fixture_role"),
        )
        for anchor in physical
    ] != expected_physical or any(
        anchor.get("expected_block_entity_id") != "advanced_ae:quantum_core"
        or anchor.get("source_derived_synthetic_fixture")
        or "placement_state" in anchor
        for anchor in case["anchors"]
    ):
        raise ValueError("M4/M5 Advanced AE exact physical 4x3x3 layout changed")


def _m45_extended_physical_block(
    position: tuple[int, int, int],
) -> tuple[str, dict[str, str]]:
    x, y, z = position
    boundary_axes = (
        x in {456, 459},
        y in {100, 102},
        z in {378, 380},
    )
    boundary_count = sum(boundary_axes)
    state = {"formed": "true", "powered": "false"}
    if boundary_count >= 2:
        state["shape"] = (
            "block"
            if boundary_count == 3
            else "column_x"
            if not boundary_axes[0]
            else "column_y"
            if not boundary_axes[1]
            else "column_z"
        )
        return M45_EXTENDED_MATRIX_ROLE_IDS[0], state
    if boundary_count == 1:
        return M45_EXTENDED_MATRIX_ROLE_IDS[2], state
    return (
        M45_EXTENDED_MATRIX_ROLE_IDS[3]
        if position == (457, 101, 379)
        else M45_EXTENDED_MATRIX_ROLE_IDS[4],
        state,
    )


def validate_m45_extended_matrix_physical_fixtures(
    cases: Iterable[dict[str, Any]],
) -> None:
    case = _m45_exact_route_case(cases, "extendedae-matrix")
    if (
        case.get("label")
        != "ExtendedAE static roles plus a live-proven physical 4x3x3 Assembler Matrix"
        or case.get("category")
        != "static-role-catalog-and-physical-valid-formed-review"
        or case.get("fixture_blocks") != ()
        or len(case["anchors"]) != 42
    ):
        raise ValueError("M4/M5 ExtendedAE physical case metadata changed")
    expected_isolated = []
    for index, (position, block_id) in enumerate(
        zip(
            M45_EXTENDED_ISOLATED_POSITIONS,
            M45_EXTENDED_MATRIX_ROLE_IDS,
            strict=True,
        )
    ):
        state = {"formed": "false", "powered": "false"}
        if index == 0:
            state["shape"] = "block"
        expected_isolated.append(
            (
                position,
                block_id,
                state,
                (
                    "unformed-isolated-frame-block"
                    if index == 0
                    else "unformed-static-matrix-role"
                ),
            )
        )
    if [
        (
            anchor["position"],
            anchor["block_id"],
            anchor.get("block_state"),
            anchor.get("fixture_role"),
        )
        for anchor in case["anchors"][:6]
    ] != expected_isolated:
        raise ValueError("M4/M5 ExtendedAE isolated role closure changed")
    expected_physical = [
        (
            position,
            *_m45_extended_physical_block(position),
            (
                "physical-valid-4x3x3-matrix-frame"
                if _m45_extended_physical_block(position)[0]
                == M45_EXTENDED_MATRIX_ROLE_IDS[0]
                else "physical-valid-4x3x3-matrix-glass"
                if _m45_extended_physical_block(position)[0]
                == M45_EXTENDED_MATRIX_ROLE_IDS[2]
                else "physical-valid-4x3x3-matrix-interior"
            ),
        )
        for position in M45_EXTENDED_PHYSICAL_POSITIONS
    ]
    if [
        (
            anchor["position"],
            anchor["block_id"],
            anchor.get("block_state"),
            anchor.get("fixture_role"),
        )
        for anchor in case["anchors"][6:]
    ] != expected_physical or any(
        anchor.get("expected_block_entity_id") != anchor["block_id"]
        or anchor.get("source_derived_synthetic_fixture")
        or "placement_state" in anchor
        for anchor in case["anchors"]
    ):
        raise ValueError("M4/M5 ExtendedAE exact physical 4x3x3 layout changed")


def validate_m45_physical_fixtures(cases: Iterable[dict[str, Any]]) -> None:
    exact_cases = tuple(cases)
    validate_m45_requester_physical_fixtures(exact_cases)
    validate_m45_expanded_physical_fixtures(exact_cases)
    validate_m45_advanced_quantum_physical_fixtures(exact_cases)
    validate_m45_extended_matrix_physical_fixtures(exact_cases)
    if [len(case["anchors"]) for case in exact_cases] != [
        11,
        36,
        118,
        107,
        44,
        9,
        42,
        42,
    ] or any(
        anchor.get("source_derived_synthetic_fixture")
        for case in exact_cases
        for anchor in case["anchors"]
    ):
        raise ValueError("M4/M5 exact physical route closure changed")


def validate_m45_disabled_projection_fixtures(
    cases: Iterable[dict[str, Any]],
) -> None:
    exact_cases = tuple(cases)
    anchors = [anchor for case in exact_cases for anchor in case["anchors"]]
    by_route = {case["route"]: case for case in exact_cases}
    if tuple(by_route) != M45_ROUTES or len(anchors) != 409:
        raise ValueError("M4/M5 disabled projection route closure changed")

    exact_empty = [
        anchor
        for anchor in anchors
        if _m45_exact_empty_original_resource(
            anchor["m45_route"], anchor["block_id"], anchor.get("block_state")
        )
    ]
    if {
        route: sum(anchor["m45_route"] == route for anchor in exact_empty)
        for route in M45_ROUTES
    } != {
        "appflux": 0,
        "merequester": 0,
        "expandedae": 22,
        "megacells": 9,
        "advanced-ae-quantum": 37,
        "advanced-ae-athena": 0,
        "extendedae-matrix": 11,
        "extendedae-planes": 0,
    }:
        raise ValueError("M4/M5 exact 79 empty original-resource classes changed")
    if any(
        anchor["physical_stock_projection"]["review_projection"] != "empty"
        or anchor["route_disabled_projections"][anchor["m45_route"]][
            "review_projection"
        ]
        != "empty"
        for anchor in exact_empty
    ):
        raise ValueError("M4/M5 exact empty original-resource projection changed")

    physical_nonempty = sum(
        anchor["physical_stock_projection"]["review_projection"] == "nonempty"
        for anchor in anchors
    )
    combined_nonempty = sum(
        anchor["route_disabled_projections"][anchor["m45_route"]][
            "review_projection"
        ]
        == "nonempty"
        for anchor in anchors
    )
    if (physical_nonempty, len(anchors) - physical_nonempty) != (110, 299) or (
        combined_nonempty,
        len(anchors) - combined_nonempty,
    ) != (299, 110):
        raise ValueError("M4/M5 physical/combined disabled projection changed")

    route_counts: dict[str, tuple[int, int, int]] = {}
    for disabled_route in M45_ROUTES:
        projections = [
            anchor["route_disabled_projections"][disabled_route]
            for anchor in anchors
            if disabled_route in anchor["route_disabled_projections"]
        ]
        route_counts[disabled_route] = (
            len(projections),
            sum(row["review_projection"] == "nonempty" for row in projections),
            sum(row["review_projection"] == "empty" for row in projections),
        )
    if route_counts != {
        "appflux": (11, 8, 3),
        "merequester": (36, 36, 0),
        "expandedae": (119, 79, 40),
        "megacells": (108, 87, 21),
        "advanced-ae-quantum": (44, 7, 37),
        "advanced-ae-athena": (9, 9, 0),
        "extendedae-matrix": (42, 31, 11),
        "extendedae-planes": (42, 42, 0),
    }:
        raise ValueError("M4/M5 single-route disabled projection matrix changed")

    crafting_projections = [
        anchor["crafting_disabled_projection"]
        for anchor in anchors
        if "crafting_disabled_projection" in anchor
    ]
    if len(crafting_projections) != 20 or any(
        projection["review_projection"] != "empty"
        for projection in crafting_projections
    ):
        raise ValueError("M4/M5 exact crafting-disabled projection changed")

    requester_anchors = [
        anchor
        for anchor in by_route["merequester"]["anchors"]
        if anchor["block_id"] == "merequester:requester"
    ]
    io_anchors = [
        anchor
        for anchor in by_route["expandedae"]["anchors"]
        if anchor["block_id"] == "expandedae:exp_io_port"
    ]
    if len(requester_anchors) != 12 or {
        powered: sum(
            anchor.get("block_state", {}).get("powered") == powered
            for anchor in io_anchors
        )
        for powered in ("false", "true")
    } != {"false": 24, "true": 24}:
        raise ValueError("M4/M5 exact inherited-model selector closure changed")
    for anchor in (*requester_anchors, *io_anchors):
        for projection in (
            anchor["physical_stock_projection"],
            anchor["route_disabled_projections"][anchor["m45_route"]],
        ):
            if _m45_exact_inherited_projection_materials(anchor, projection) is None:
                raise ValueError("M4/M5 inherited-model material projection changed")

    def tsv_rows(relative: str) -> list[tuple[str, int, str]]:
        return [
            (path, int(size), digest)
            for path, size, digest in (
                line.split("\t")
                for line in (M45_PROFILE_ROOT / relative)
                .read_text(encoding="utf-8")
                .splitlines()
                if line
            )
        ]

    requester_rows = tsv_rows(
        "merequester/1.21.1-1.4.3/required-resources.tsv"
    )
    expanded_rows = tsv_rows("expandedae/2.1.1/required-resources.tsv")
    mega_rows = tsv_rows("megacells/4.11.0/required-crafting-resources.tsv")
    advanced_rows = tsv_rows(
        "advancedae/1.6.12/quantum-required-resources.tsv"
    )
    extended_rows = tsv_rows(
        "extendedae/1.21-2.2.35-neoforge/m5-required-resources.tsv"
    )
    empty_digest = "44136fa355b3678a1146ad16f7e8649e94fb4fc21fe77e8310c060f61caaff8a"
    newline_empty_digest = (
        "ca3d163bab055381827226140568f3bef7eaac187cebd76878e0b63e9e442356"
    )
    expanded_formed = [
        row
        for row in expanded_rows
        if row[0].startswith("assets/expandedae/models/block/crafting/")
        and row[0].endswith("_formed.json")
    ]
    mega_formed = [
        row
        for row in mega_rows
        if row[0].startswith("assets/megacells/models/block/crafting/")
        and row[0].endswith("_formed.json")
    ]
    if (
        len(expanded_formed) != 21
        or any(row[1:] != (2, empty_digest) for row in expanded_formed)
        or len(mega_formed) != 8
        or any(row[1:] != (2, empty_digest) for row in mega_formed)
        or (
            "assets/merequester/models/block/requester.json",
            2_104,
            "065177ccd37084d41d9da4e06759c883067164ce5dbbb966ccb2f9ac0ea452bb",
        )
        not in requester_rows
        or (
            "assets/expandedae/models/block/exp_io_port.json",
            1_628,
            "ded81529f3e48c4774f54067286d10722c6f57afef287086187020f2a74329d6",
        )
        not in expanded_rows
        or (
            "assets/expandedae/models/block/exp_io_port_on.json",
            269,
            "44275c3d508a48c861ceffb36279e670da1811fd76592b3b59ece9a9ae060baf",
        )
        not in expanded_rows
        or any(
            row not in advanced_rows
            for row in (
                (
                    "assets/advanced_ae/models/block/crafting/quantum_structure_formed.json",
                    3,
                    newline_empty_digest,
                ),
                (
                    "assets/advanced_ae/models/block/crafting/quantum_storage_128_formed.json",
                    3,
                    newline_empty_digest,
                ),
                (
                    "assets/advanced_ae/models/block/quantum_core_formed.json",
                    3,
                    newline_empty_digest,
                ),
            )
        )
        or (
            "assets/extendedae/models/block/assembler_matrix_glass.json",
            53,
            "27e03c3bcb9ca0b37fbe9b084c895f5e9062bf113fce892031d92b6dec9f4170",
        )
        not in extended_rows
    ):
        raise ValueError("M4/M5 disabled projection source model evidence changed")


def create_m45_cases() -> list[dict[str, Any]]:
    cases: list[dict[str, Any]] = []

    def add(
        milestone: str,
        route: str,
        label: str,
        category: str,
        anchors: Iterable[dict[str, Any]],
        fixture_blocks: Iterable[dict[str, Any]] = (),
    ) -> None:
        index = len(cases) + 1
        cases.append(
            {
                "case_id": f"ae2-m45-{index:02d}",
                "milestone": milestone,
                "route": route,
                "label": label,
                "category": category,
                "anchors": tuple(anchors),
                "fixture_blocks": tuple(fixture_blocks),
            }
        )

    appflux_tiers = (
        "1k", "4k", "16k", "64k", "256k",
        "1m", "4m", "16m", "64m", "256m",
    )
    appflux_cells = tuple(
        item_id
        for tier in appflux_tiers
        for item_id in (
            f"appflux:fe_{tier}_cell",
            f"appflux:fe_{tier}_portable_cell",
        )
    )
    appflux_anchors = [
        _m45_anchor((336, 100, 312), "appflux:charged_redstone_block", "appflux"),
        _m45_anchor(
            (338, 100, 312),
            "appflux:flux_accessor",
            "appflux",
            expected_block_entity_id="appflux:flux_accessor",
        ),
    ]
    for position, direction in zip(
        _m45_grid(336, 346, 316, 6), DIRECTION_DELTAS, strict=True
    ):
        appflux_anchors.append(
            _m45_cable_anchor(
                position,
                "appflux",
                direction,
                {"id": "appflux:part_flux_accessor", "fast": False},
            )
        )
    appflux_anchors.extend(
        (
            _m45_drive_anchor(
                (336, 100, 320), "appflux", DRIVE_BLOCK_ID, appflux_cells[:10]
            ),
            _m45_drive_anchor(
                (338, 100, 320), "appflux", DRIVE_BLOCK_ID, appflux_cells[10:]
            ),
            _m45_drive_anchor(
                (340, 100, 320),
                "appflux",
                EXTENDED_DRIVE_BLOCK_ID,
                appflux_cells,
            ),
        )
    )
    add(
        "M4",
        "appflux",
        "AppliedFlux generic blocks, face part, and all twenty Drive-cell identities",
        "generic-route-and-drive-cell-catalog",
        appflux_anchors,
    )

    requester_anchors: list[dict[str, Any]] = []
    requester_positions = iter(M45_REQUESTER_BLOCK_POSITIONS)
    for facing, y_rotation in zip(
        ("north", "east", "south", "west"),
        (0, 90, 180, 270),
        strict=True,
    ):
        requester_anchors.append(
            _m45_anchor(
                next(requester_positions),
                "merequester:requester",
                "merequester",
                block_state={"active": "false", "facing": facing},
                expected_block_entity_id="merequester:requester",
                z_rotation_source="implicit-zero",
                expected_rotation_degrees={"x": 0, "y": y_rotation, "z": 0},
                fixture_role="idle-derived-inactive-requester-orientation",
            )
        )
    for facing in ("up", "down"):
        for spin in range(4):
            z_rotation = (
                (180, 90, 0, 270)[spin]
                if facing == "up"
                else (0, 270, 180, 90)[spin]
            )
            requester_anchors.append(
                _m45_anchor(
                    next(requester_positions),
                    "merequester:requester",
                    "merequester",
                    block_state={
                        "active": "false",
                        "facing": facing,
                        "spin": str(spin),
                    },
                    expected_block_entity_id="merequester:requester",
                    z_rotation_source="ae2:z-normalized-from-spin",
                    expected_rotation_degrees={
                        "x": 270 if facing == "up" else 90,
                        "y": 0,
                        "z": z_rotation,
                    },
                    fixture_role="idle-derived-inactive-requester-orientation",
                )
            )
    terminal_positions = iter(M45_REQUESTER_TERMINAL_POSITIONS)
    for direction in DIRECTION_DELTAS:
        for spin in range(4):
            requester_anchors.append(
                _m45_cable_anchor(
                    next(terminal_positions),
                    "merequester",
                    direction,
                    {"id": "merequester:requester_terminal", "spin": spin},
                )
            )
    add(
        "M4",
        "merequester",
        "ME Requester 12 stable idle-derived block orientations and 24 terminal orientations",
        "idle-derived-inactive-z-normalization-and-terminal-orbits",
        requester_anchors,
    )

    expanded_tiers = (
        "2", "4", "8", "16", "32", "64", "128", "256", "512",
        "1k", "2k", "4k", "8k", "16k", "32k", "64k", "128k",
        "256k", "512k", "1m",
    )
    expanded_blocks = (
        "expandedae:exp_crafting_unit",
        *(f"expandedae:exp_crafting_accelerator_{tier}" for tier in expanded_tiers),
    )
    expanded_cube = tuple(
        (x, y, z)
        for y in range(100, 103)
        for z in range(312, 315)
        for x in range(416, 419)
    )
    expanded_surface = tuple(
        position
        for position in expanded_cube
        if position != (417, 101, 313)
    )
    expanded_anchors = [
        _m45_anchor(
            position,
            block_id,
            "expandedae",
            block_state={"formed": "true", "powered": "false"},
            expected_block_entity_id="expandedae:exp_cpus",
            placement_state={"formed": "false", "powered": "false"},
            fixture_role="complete-3x3x3-crafting-cpu-visible-member",
        )
        for position, block_id in zip(
            expanded_surface[: len(expanded_blocks)], expanded_blocks, strict=True
        )
    ]
    expanded_fixtures = [
        {
            "position": position,
            "block_id": (
                "ae2:1k_crafting_storage"
                if position == (417, 101, 313)
                else "ae2:crafting_unit"
            ),
            "block_state": {"formed": "true", "powered": "false"},
            "placement_state": {"formed": "false", "powered": "false"},
            "expected_block_entity_id": (
                "ae2:crafting_storage"
                if position == (417, 101, 313)
                else "ae2:crafting_unit"
            ),
            "purpose": "complete-3x3x3-crafting-cpu-helper",
        }
        for position in expanded_cube
        if position not in expanded_surface[: len(expanded_blocks)]
    ]
    mixed_cube = tuple(
        (x, y, z)
        for y in range(100, 103)
        for z in range(312, 315)
        for x in range(464, 467)
    )
    mixed_expanded_position = (464, 100, 312)
    mixed_mega_position = (465, 100, 312)
    mixed_expanded_anchor = _m45_anchor(
        mixed_expanded_position,
        "expandedae:exp_crafting_unit",
        "expandedae",
        block_state={"formed": "true", "powered": "false"},
        expected_block_entity_id="expandedae:exp_cpus",
        placement_state={"formed": "false", "powered": "false"},
        fixture_role="mixed-owner-crafting-cpu-expanded-peer-control",
    )
    mixed_expanded_anchor["route_disabled_projections"]["megacells"] = {
        "expected_path": "stock-original-m45",
        "review_projection": "empty",
        "reason": (
            "inactive-megacells-peer-owner-selects-exact-empty-formed-model"
        ),
    }
    expanded_anchors.append(mixed_expanded_anchor)
    expanded_fixtures.extend(
        {
            "position": position,
            "block_id": (
                "ae2:1k_crafting_storage"
                if position == (465, 101, 313)
                else "ae2:crafting_unit"
            ),
            "block_state": {"formed": "true", "powered": "false"},
            "placement_state": {"formed": "false", "powered": "false"},
            "expected_block_entity_id": (
                "ae2:crafting_storage"
                if position == (465, 101, 313)
                else "ae2:crafting_unit"
            ),
            "purpose": "mixed-expanded-mega-crafting-owner-helper",
        }
        for position in mixed_cube
        if position not in {mixed_expanded_position, mixed_mega_position}
    )
    io_positions = iter(_m45_grid(424, 446, 312, 48))
    for facing in DIRECTION_DELTAS:
        for powered in (False, True):
            for spin in range(4):
                if facing == "north":
                    rotation = (0, 0, (-90 * spin) % 360)
                elif facing == "east":
                    rotation = (0, 90, (-90 * spin) % 360)
                elif facing == "south":
                    rotation = (0, 180, (90 * spin) % 360)
                elif facing == "west":
                    rotation = (0, 270, (-90 * spin) % 360)
                elif facing == "up":
                    rotation = (270, 0, (180 - 90 * spin) % 360)
                else:
                    rotation = (90, 0, (-90 * spin) % 360)
                position = next(io_positions)
                expanded_anchors.append(
                    _m45_anchor(
                        position,
                        "expandedae:exp_io_port",
                        "expandedae",
                        block_state={
                            "facing": facing,
                            "powered": str(powered).lower(),
                            "spin": str(spin),
                        },
                        expected_block_entity_id="expandedae:exp_io_port",
                        z_rotation_source="ae2:z-normalized-from-spin",
                        expected_rotation_degrees=dict(
                            zip(("x", "y", "z"), rotation, strict=True)
                        ),
                    )
                )
                if powered:
                    expanded_fixtures.append(
                        {
                            "position": (position[0], 99, position[2]),
                            "block_id": "ae2:creative_energy_cell",
                            "purpose": "powered-exp-io-port-network-helper",
                        }
                    )
    expanded_part_positions = iter(_m45_grid(424, 446, 322, 30))
    for direction in DIRECTION_DELTAS:
        expanded_anchors.append(
            _m45_cable_anchor(
                next(expanded_part_positions),
                "expandedae",
                direction,
                {"id": "expandedae:exp_pattern_provider_part"},
            )
        )
    for direction in DIRECTION_DELTAS:
        for spin in range(4):
            expanded_anchors.append(
                _m45_cable_anchor(
                    next(expanded_part_positions),
                    "expandedae",
                    direction,
                    {"id": "expandedae:exp_encoding_terminal", "spin": spin},
                )
            )
    expanded_anchors.append(
        _m45_anchor(
            (424, 100, 330),
            "expandedae:exp_pattern_provider",
            "expandedae",
            expected_block_entity_id="expandedae:exp_pattern_provider",
        )
    )
    for color, position in zip(
        M45_COLORABLE_DRIVE_COLORS,
        M45_COLORABLE_DRIVE_POSITIONS,
        strict=True,
    ):
        expanded_anchors.append(
            _m45_anchor(
                position,
                "expandedae:colorable_drive",
                "expandedae",
                expected_path="stock-fallback-m45",
                projection="empty",
                fallback_reason="upstream-colorable-drive-world-visual-unavailable",
                expected_block_entity_id="expandedae:colorable_drive",
                expected_nbt={"color": color},
                fixture_role="all-seventeen-persisted-color-values",
            )
        )
    add(
        "M4",
        "expandedae",
        "Expanded AE complete crafting, IO-port Z rotations, parts, and fallback controls",
        "complete-profile-and-failure-isolation",
        expanded_anchors,
        expanded_fixtures,
    )

    mega_profile = _m45_profile_json("megacells/4.11.0/profile.json")
    mega_blocks = tuple(
        row["id"] for row in mega_profile["crafting"]["supportedBlocks"]
    )
    mega_block_entities = {
        row["id"]: row["blockEntity"]
        for row in mega_profile["crafting"]["supportedBlocks"]
    }
    mega_cube = tuple(
        (x, y, z)
        for y in range(100, 103)
        for z in range(344, 347)
        for x in range(336, 339)
    )
    mega_surface = tuple(
        position for position in mega_cube if position != (337, 101, 345)
    )
    mega_anchors: list[dict[str, Any]] = []
    for index, (position, block_id) in enumerate(
        zip(mega_surface[: len(mega_blocks)], mega_blocks, strict=True)
    ):
        state = {"formed": "true", "powered": "false"}
        if block_id == "megacells:mega_crafting_monitor":
            state.update({"facing": "north", "spin": "0"})
        placement = dict(state)
        placement["formed"] = "false"
        mega_anchors.append(
            _m45_anchor(
                position,
                block_id,
                "megacells",
                block_state=state,
                expected_block_entity_id=mega_block_entities[block_id],
                placement_state=placement,
                fixture_role=f"complete-3x3x3-crafting-cpu-visible-member-{index}",
            )
        )
    mixed_mega_anchor = _m45_anchor(
        mixed_mega_position,
        "megacells:mega_crafting_unit",
        "megacells",
        block_state={"formed": "true", "powered": "false"},
        expected_block_entity_id="megacells:mega_crafting_unit",
        placement_state={"formed": "false", "powered": "false"},
        fixture_role="mixed-owner-crafting-cpu-megacells-peer-control",
    )
    mixed_mega_anchor["route_disabled_projections"]["expandedae"] = {
        "expected_path": "stock-original-m45",
        "review_projection": "empty",
        "reason": (
            "inactive-expandedae-peer-owner-selects-exact-empty-formed-model"
        ),
    }
    mega_anchors.append(mixed_mega_anchor)
    mega_fixtures = [
        {
            "position": position,
            "block_id": (
                "ae2:1k_crafting_storage"
                if position == (337, 101, 345)
                else "ae2:crafting_unit"
            ),
            "block_state": {"formed": "true", "powered": "false"},
            "placement_state": {"formed": "false", "powered": "false"},
            "expected_block_entity_id": (
                "ae2:crafting_storage"
                if position == (337, 101, 345)
                else "ae2:crafting_unit"
            ),
            "purpose": "complete-3x3x3-mega-crafting-helper",
        }
        for position in mega_cube
        if position not in mega_surface[: len(mega_blocks)]
    ]
    mega_cell_rows = tuple(
        line.split("\t")
        for line in (
            M45_PROFILE_ROOT / "megacells/4.11.0/cell-models.tsv"
        ).read_text(encoding="utf-8").splitlines()
        if line
    )
    if len(mega_cell_rows) != 67 or len({row[1] for row in mega_cell_rows}) != 37:
        raise ValueError("MEGA Cells exact 67-item/37-model catalog changed")
    mega_cell_ids = tuple(row[0] for row in mega_cell_rows)
    dock_positions = iter(_m45_grid(344, 374, 344, 69))
    mega_anchors.append(
        _m45_cable_anchor(
            next(dock_positions),
            "megacells",
            "north",
            {"id": "megacells:cell_dock", "spin": 0, "cell": None},
        )
    )
    for index, item_id in enumerate(mega_cell_ids):
        direction = tuple(DIRECTION_DELTAS)[index % len(DIRECTION_DELTAS)]
        mega_anchors.append(
            _m45_cable_anchor(
                next(dock_positions),
                "megacells",
                direction,
                {
                    "id": "megacells:cell_dock",
                    "spin": index % 4,
                    "cell": _m45_item(item_id),
                },
            )
        )
    mega_anchors.append(
        _m45_cable_anchor(
            next(dock_positions),
            "megacells",
            "south",
            {
                "id": "megacells:cell_dock",
                "spin": 0,
                "cell": _m45_item("minecraft:stone"),
            },
            expected_path="stock-fallback-m45",
            projection="empty",
            fallback_reason="unknown-cell-dock-cell-atomic-whole-bus-fallback",
        )
    )
    part_positions = iter(_m45_grid(344, 374, 356, 18))
    for part_id in (
        "megacells:decompression_module",
        "megacells:cable_mega_interface",
        "megacells:cable_mega_pattern_provider",
    ):
        for direction in DIRECTION_DELTAS:
            mega_anchors.append(
                _m45_cable_anchor(
                    next(part_positions), "megacells", direction, {"id": part_id}
                )
            )
    for index, start in enumerate(range(0, len(mega_cell_ids), 10)):
        mega_anchors.append(
            _m45_drive_anchor(
                (344 + index * 2, 100, 362),
                "megacells",
                DRIVE_BLOCK_ID,
                mega_cell_ids[start : start + 10],
            )
        )
    for index, start in enumerate(range(0, len(mega_cell_ids), 20)):
        mega_anchors.append(
            _m45_drive_anchor(
                (344 + index * 2, 100, 366),
                "megacells",
                EXTENDED_DRIVE_BLOCK_ID,
                mega_cell_ids[start : start + 20],
            )
        )
    add(
        "M5",
        "megacells",
        "MEGA Cells crafting, parts, Cell Dock, and all Drive-cell identities",
        "complete-megacells-static-world-profile",
        mega_anchors,
        mega_fixtures,
    )

    quantum_anchors: list[dict[str, Any]] = []
    for index, (position, block_id) in enumerate(
        zip(M45_ADVANCED_ISOLATED_POSITIONS, M45_ADVANCED_ROLE_IDS, strict=True)
    ):
        is_isolated_core = position == M45_ADVANCED_SINGLETON_POSITION
        quantum_anchors.append(
            _m45_anchor(
                position,
                block_id,
                "advanced-ae-quantum",
                block_state={
                    "formed": "true" if is_isolated_core else "false",
                    "powered": "false",
                    "multiblocked": "false",
                    "light_level": "0",
                },
                expected_block_entity_id="advanced_ae:quantum_core",
                fixture_role=(
                    "physical-valid-isolated-single-core"
                    if is_isolated_core
                    else "unformed-static-role"
                ),
                **(
                    {
                        "selector_scoped_model_exception": (
                            M45_ADVANCED_SINGLETON_MODEL_EXCEPTION
                        )
                    }
                    if is_isolated_core
                    else {}
                ),
            )
        )
    for position in M45_ADVANCED_PHYSICAL_POSITIONS:
        block_id = (
            "advanced_ae:quantum_core"
            if position == (417, 101, 377)
            else "advanced_ae:quantum_storage_128"
            if position == (418, 101, 377)
            else "advanced_ae:quantum_structure"
        )
        quantum_anchors.append(
            _m45_anchor(
                position,
                block_id,
                "advanced-ae-quantum",
                block_state={
                    "formed": "true",
                    "powered": "false",
                    "multiblocked": "true",
                    "light_level": "0",
                },
                expected_block_entity_id="advanced_ae:quantum_core",
                fixture_role=(
                    "physical-valid-4x3x3-quantum-computer-interior"
                    if block_id != "advanced_ae:quantum_structure"
                    else "physical-valid-4x3x3-quantum-computer-boundary"
                ),
            )
        )
    add(
        "M5",
        "advanced-ae-quantum",
        "Advanced AE static roles plus a live-proven physical 4x3x3 quantum computer",
        "static-role-catalog-and-physical-valid-connected-review",
        quantum_anchors,
    )

    athena_positions = (
        (424, 100, 376),
        *((426 + dx, 100 + dy, 376 + dz) for dy in range(2) for dz in range(2) for dx in range(2)),
    )
    add(
        "M5",
        "advanced-ae-athena",
        "Advanced AE quantum-alloy isolated and connected Athena topology",
        "athena-whole-state-neighborhood",
        (
            _m45_anchor(
                position,
                "advanced_ae:quantum_alloy_block",
                "advanced-ae-athena",
                fixture_role=(
                    "isolated-athena-control" if index == 0 else "connected-athena-2x2x2"
                ),
            )
            for index, position in enumerate(athena_positions)
        ),
    )

    matrix_roles = M45_EXTENDED_MATRIX_ROLE_IDS
    matrix_anchors: list[dict[str, Any]] = []
    matrix_anchors.append(
        _m45_anchor(
            M45_EXTENDED_ISOLATED_POSITIONS[0],
            matrix_roles[0],
            "extendedae-matrix",
            block_state={"formed": "false", "powered": "false", "shape": "block"},
            expected_block_entity_id=matrix_roles[0],
            fixture_role="unformed-isolated-frame-block",
        )
    )
    for position, block_id in zip(
        M45_EXTENDED_ISOLATED_POSITIONS[1:], matrix_roles[1:], strict=True
    ):
        matrix_anchors.append(
            _m45_anchor(
                position,
                block_id,
                "extendedae-matrix",
                block_state={"formed": "false", "powered": "false"},
                expected_block_entity_id=block_id,
                fixture_role="unformed-static-matrix-role",
            )
        )
    for position in M45_EXTENDED_PHYSICAL_POSITIONS:
        x, y, z = position
        boundary_axes = (
            x in {456, 459},
            y in {100, 102},
            z in {378, 380},
        )
        boundary_count = sum(boundary_axes)
        if boundary_count >= 2:
            block_id = matrix_roles[0]
            shape = (
                "block"
                if boundary_count == 3
                else "column_x"
                if not boundary_axes[0]
                else "column_y"
                if not boundary_axes[1]
                else "column_z"
            )
        elif boundary_count == 1:
            block_id = matrix_roles[2]
            shape = None
        else:
            block_id = (
                matrix_roles[3]
                if position == (457, 101, 379)
                else matrix_roles[4]
            )
            shape = None
        state = {"formed": "true", "powered": "false"}
        if shape is not None:
            state["shape"] = shape
        matrix_anchors.append(
            _m45_anchor(
                position,
                block_id,
                "extendedae-matrix",
                block_state=state,
                expected_block_entity_id=block_id,
                fixture_role=(
                    "physical-valid-4x3x3-matrix-frame"
                    if block_id == matrix_roles[0]
                    else "physical-valid-4x3x3-matrix-glass"
                    if block_id == matrix_roles[2]
                    else "physical-valid-4x3x3-matrix-interior"
                ),
            )
        )
    add(
        "M5",
        "extendedae-matrix",
        "ExtendedAE static roles plus a live-proven physical 4x3x3 Assembler Matrix",
        "static-role-catalog-and-physical-valid-formed-review",
        matrix_anchors,
    )

    plane_anchors: list[dict[str, Any]] = []
    plane_helpers: list[dict[str, Any]] = []
    plane_centers = iter(_m45_grid(480, 504, 370, 42, spacing=4))
    for part_id in (
        "extendedae:active_formation_plane",
        "extendedae:smart_annihilation_plane",
    ):
        for mask in range(16):
            center = next(plane_centers)
            anchor = _m45_cable_anchor(
                center, "extendedae-planes", "north", {"id": part_id}
            )
            anchor.update(
                {
                    "plane_mask": mask,
                    "plane_mask_bit_order": ["up", "west", "down", "east"],
                    "fixture_role": "all-sixteen-coplanar-plane-masks",
                }
            )
            plane_anchors.append(anchor)
            for bit, direction in zip(
                NATIVE_STRUCTURAL_PLANE_MASK_BITS,
                NATIVE_STRUCTURAL_PLANE_MASK_DIRECTIONS["north"],
                strict=True,
            ):
                if not mask & bit:
                    continue
                delta = DIRECTION_DELTAS[direction]
                helper_position = tuple(
                    center[axis] + delta[axis] for axis in range(3)
                )
                plane_helpers.append(
                    {
                        "position": helper_position,
                        "block_id": "ae2:cable_bus",
                        "cable_id": "ae2:fluix_covered_cable",
                        "face_parts": {"north": {"id": part_id}},
                        "purpose": f"{part_id}-mask-{mask:04b}-{direction}",
                    }
                )
        for direction in ("down", "up", "south", "west", "east"):
            plane_anchors.append(
                _m45_cable_anchor(
                    next(plane_centers),
                    "extendedae-planes",
                    direction,
                    {"id": part_id},
                )
            )
    add(
        "M5",
        "extendedae-planes",
        "ExtendedAE plane identities, installed-face orbit, and all sixteen masks",
        "native-structural-extension-plane-topology",
        plane_anchors,
        plane_helpers,
    )

    native_crafting_helpers = {
        fixture["position"]
        for case in cases
        for fixture in case["fixture_blocks"]
        if fixture["block_id"].startswith("ae2:")
        and "crafting" in fixture["block_id"]
    }
    crafting_block_ids = set(expanded_blocks) | set(mega_blocks)
    for case in cases:
        for anchor in case["anchors"]:
            if anchor["block_id"] not in crafting_block_ids:
                continue
            x, y, z = anchor["position"]
            neighbors = {
                (x + dx, y + dy, z + dz)
                for dx, dy, dz in DIRECTION_DELTAS.values()
            }
            if neighbors & native_crafting_helpers:
                anchor["crafting_disabled_projection"] = {
                    "expected_path": "stock-original-m45",
                    "review_projection": "empty",
                    "reason": (
                        "inactive-native-crafting-peer-owner-selects-exact-empty-formed-model"
                    ),
                }

    seen_positions: set[tuple[int, int, int]] = set()
    for case in cases:
        for entry in (*case["anchors"], *case["fixture_blocks"]):
            position = entry["position"]
            if position in seen_positions:
                raise ValueError(f"M4/M5 fixture position is duplicated: {position}")
            seen_positions.add(position)
            if not all(
                M45_FIXTURE_BOUNDS[0][axis]
                <= position[axis]
                <= M45_FIXTURE_BOUNDS[1][axis]
                for axis in range(3)
            ):
                raise ValueError(f"M4/M5 fixture position is out of bounds: {position}")
    if tuple(case["route"] for case in cases) != M45_ROUTES:
        raise ValueError("M4/M5 route/case ordering changed")
    validate_m45_colorable_drive_fixtures(cases)
    validate_m45_physical_fixtures(cases)
    validate_m45_disabled_projection_fixtures(cases)
    return cases


def _m45_no_duplicate_object(
    pairs: list[tuple[str, Any]],
) -> dict[str, Any]:
    value: dict[str, Any] = {}
    for key, item in pairs:
        if key in value:
            raise ValueError(f"M4/M5 oracle contains duplicate key: {key}")
        value[key] = item
    return value


def _load_exact_m45_oracle(
    path: Path,
    *,
    size_bytes: int,
    sha256: str,
    coverage_id: str,
    route_ids: tuple[str, ...],
    expected_positions: set[tuple[int, int, int]],
    expected_anchor_count: int,
    expected_triangle_count: int,
    expected_identity_count: int,
    expected_material_row_count: int,
    description: str,
) -> dict[tuple[int, int, int], dict[str, Any]]:
    oracle_bytes = path.read_bytes()
    if (
        len(oracle_bytes) != size_bytes
        or hashlib.sha256(oracle_bytes).hexdigest() != sha256
    ):
        raise ValueError(f"{description} identity changed")
    try:
        payload = json.loads(
            oracle_bytes.decode("utf-8"),
            object_pairs_hook=_m45_no_duplicate_object,
        )
    except (UnicodeDecodeError, json.JSONDecodeError) as exception:
        raise ValueError(f"{description} is not canonical JSON") from exception
    if (
        not isinstance(payload, dict)
        or set(payload)
        != {
            "anchors",
            "coverage_id",
            "route_ids",
            "schema_version",
            "signature_schema_version",
        }
        or payload.get("schema_version") != 2
        or payload.get("signature_schema_version") != 11
        or payload.get("coverage_id") != coverage_id
        or payload.get("route_ids") != list(route_ids)
        or not isinstance(payload.get("anchors"), dict)
        or (
            json.dumps(payload, indent=2, sort_keys=True, ensure_ascii=True)
            + "\n"
        ).encode("utf-8")
        != oracle_bytes
    ):
        raise ValueError(f"{description} header or canonical encoding changed")
    parsed: dict[tuple[int, int, int], dict[str, Any]] = {}
    for key, entry in payload["anchors"].items():
        try:
            position = tuple(int(value) for value in key.split())
        except (AttributeError, ValueError) as exception:
            raise ValueError(f"{description} position key is malformed") from exception
        if (
            len(position) != 3
            or key != " ".join(str(value) for value in position)
            or position in parsed
            or not isinstance(entry, dict)
        ):
            raise ValueError(f"{description} selector is noncanonical")
        materials = entry.get("material_triangles")
        triangle_count = entry.get("triangle_count")
        if (
            set(entry)
            != {
                "geometry_signature",
                "material_triangles",
                "nonlighting_attribute_signature",
                "triangle_count",
            }
            or not isinstance(triangle_count, int)
            or isinstance(triangle_count, bool)
            or triangle_count <= 0
            or not isinstance(materials, dict)
            or not materials
            or list(materials) != sorted(materials)
            or any(
                not isinstance(resource, str)
                or not resource
                or not isinstance(count, int)
                or isinstance(count, bool)
                or count <= 0
                for resource, count in materials.items()
            )
            or triangle_count != sum(materials.values())
            or any(
                not isinstance(entry.get(field), str)
                or len(entry[field]) != 64
                or any(
                    character not in "0123456789abcdef"
                    for character in entry[field]
                )
                for field in (
                    "geometry_signature",
                    "nonlighting_attribute_signature",
                )
            )
        ):
            raise ValueError(f"{description} entry is malformed at {position}")
        parsed[position] = {
            "geometry_signature": entry["geometry_signature"],
            "material_triangles": dict(materials),
            "nonlighting_attribute_signature": entry[
                "nonlighting_attribute_signature"
            ],
            "triangle_count": triangle_count,
        }
    resources = {
        resource
        for entry in parsed.values()
        for resource in entry["material_triangles"]
    }
    if (
        set(parsed) != expected_positions
        or len(parsed) != expected_anchor_count
        or sum(entry["triangle_count"] for entry in parsed.values())
        != expected_triangle_count
        or len(resources) != expected_identity_count
        or sum(len(entry["material_triangles"]) for entry in parsed.values())
        != expected_material_row_count
    ):
        raise ValueError(f"{description} exact closure changed")
    return parsed


def load_m45_runtime_oracle(
    cases: Iterable[dict[str, Any]],
) -> dict[tuple[int, int, int], dict[str, Any]]:
    cases = tuple(cases)
    route_counts = {
        route: sum(
            anchor["expected_path"] == "custom-m45"
            for case in cases
            if case["route"] == route
            for anchor in case["anchors"]
        )
        for route in M45_ROUTES
    }
    if route_counts != M45_RUNTIME_ROUTE_ANCHOR_COUNTS:
        raise ValueError("M4/M5 runtime-oracle route selector counts changed")
    expected_positions = {
        anchor["position"]
        for case in cases
        for anchor in case["anchors"]
        if anchor["expected_path"] == "custom-m45"
    }
    return _load_exact_m45_oracle(
        M45_RUNTIME_ORACLE_PATH,
        size_bytes=M45_RUNTIME_ORACLE_SIZE_BYTES,
        sha256=M45_RUNTIME_ORACLE_SHA256,
        coverage_id=M45_RUNTIME_COVERAGE,
        route_ids=M45_ROUTES,
        expected_positions=expected_positions,
        expected_anchor_count=M45_RUNTIME_ORACLE_ANCHOR_COUNT,
        expected_triangle_count=M45_RUNTIME_ORACLE_TRIANGLE_COUNT,
        expected_identity_count=M45_RUNTIME_ORACLE_IDENTITY_COUNT,
        expected_material_row_count=M45_RUNTIME_ORACLE_MATERIAL_ROW_COUNT,
        description="M4/M5 compiled-runtime oracle",
    )


def load_m45_schema10_legacy_oracle(
) -> dict[tuple[int, int, int], dict[str, Any]]:
    return _load_exact_m45_oracle(
        M45_SCHEMA10_LEGACY_ORACLE_PATH,
        size_bytes=M45_SCHEMA10_LEGACY_ORACLE_SIZE_BYTES,
        sha256=M45_SCHEMA10_LEGACY_ORACLE_SHA256,
        coverage_id=M45_SCHEMA10_LEGACY_COVERAGE,
        route_ids=("expandedae", "megacells"),
        expected_positions={
            spec["position"] for spec in M45_LEGACY_UPGRADE_SPECS
        },
        expected_anchor_count=M45_SCHEMA10_LEGACY_ORACLE_ANCHOR_COUNT,
        expected_triangle_count=M45_SCHEMA10_LEGACY_ORACLE_TRIANGLE_COUNT,
        expected_identity_count=M45_SCHEMA10_LEGACY_ORACLE_IDENTITY_COUNT,
        expected_material_row_count=M45_SCHEMA10_LEGACY_ORACLE_MATERIAL_ROW_COUNT,
        description="M4/M5 schema-10 legacy-upgrade oracle",
    )


def enrich_and_validate_m45_runtime_oracle(
    cases: Iterable[dict[str, Any]],
) -> None:
    cases = tuple(cases)
    oracle = load_m45_runtime_oracle(cases)
    seen: set[tuple[int, int, int]] = set()
    oracle_fields = {
        "expected_geometry_signature",
        "expected_material_triangles",
        "expected_nonlighting_attribute_signature",
        "expected_triangle_count",
    }
    for case in cases:
        for anchor in case["anchors"]:
            if anchor["expected_path"] == "custom-m45":
                entry = oracle[anchor["position"]]
                anchor.update(
                    {
                        "expected_geometry_signature": entry[
                            "geometry_signature"
                        ],
                        "expected_material_triangles": entry[
                            "material_triangles"
                        ],
                        "expected_nonlighting_attribute_signature": entry[
                            "nonlighting_attribute_signature"
                        ],
                        "expected_triangle_count": entry["triangle_count"],
                    }
                )
                seen.add(anchor["position"])
            elif oracle_fields & set(anchor):
                raise ValueError(
                    "M4/M5 fallback anchor unexpectedly contains runtime-oracle fields"
                )
    if seen != set(oracle):
        raise ValueError("M4/M5 runtime-oracle enrichment closure changed")


M45_CASES = create_m45_cases()
enrich_and_validate_m45_runtime_oracle(M45_CASES)
M45_CASE_COUNT = len(M45_CASES)
M45_ANCHOR_COUNT = sum(len(case["anchors"]) for case in M45_CASES)
TOTAL_CASE_COUNT = CASE_COUNT + M45_CASE_COUNT
TOTAL_ANCHOR_COUNT = ANCHOR_COUNT + M45_ANCHOR_COUNT


def json_bytes(value: object) -> bytes:
    return (json.dumps(value, indent=2, sort_keys=True) + "\n").encode("utf-8")


def _render_anchor(anchor: dict[str, Any]) -> dict[str, Any]:
    rendered: dict[str, Any] = {
        "position": dict(zip(("x", "y", "z"), anchor["position"])),
        "block_id": anchor["block_id"],
        "cable_id": anchor["cable_id"],
        "expected_path": anchor["expected_path"],
    }
    if anchor["block_id"] in (DRIVE_BLOCK_ID, EXTENDED_DRIVE_BLOCK_ID):
        rendered["block_state"] = anchor["block_state"]
        x, _y, z = anchor["position"]
        rendered["support_floor"] = {
            "position": {"x": x, "y": 98, "z": z},
            "block_id": "minecraft:smooth_stone",
        }
        rendered["air_gap"] = {
            "position": {"x": x, "y": 99, "z": z},
            "block_id": "minecraft:air",
            "purpose": "prevent-neighbor-face-culling",
        }
        rendered["inventory"] = {
            "compound": "inv",
            "slots": [
                {
                    "slot": slot,
                    "field": f"item{slot}",
                    **({"empty": True} if item is None else {"item_stack": item}),
                }
                for slot, item in enumerate(anchor["drive_inventory"])
            ],
        }
        rendered["network_condition"] = "disconnected-unpowered"
    elif anchor["block_id"] in CONNECTED_GLASS_BLOCK_IDS:
        rendered["expected_glass_base_selection"] = anchor[
            "expected_glass_base_selection"
        ]
        rendered["expected_glass_faces"] = anchor["expected_glass_faces"]
        rendered["expected_opaque_culled_faces"] = anchor[
            "expected_opaque_culled_faces"
        ]
    elif anchor["block_id"] in CRAFTING_BLOCK_KINDS:
        rendered["crafting_kind"] = anchor["crafting_kind"]
        rendered["block_state"] = anchor["block_state"]
        rendered["formation_policy"] = "real-complete-cpu-no-forced-state"
        if anchor["block_id"] == CRAFTING_MONITOR_BLOCK_ID:
            rendered["painted_color_ordinal"] = anchor["painted_color_ordinal"]
            rendered["monitor_display_policy"] = anchor["monitor_display_policy"]
        if "compatible_neighbor_block_ids" in anchor:
            rendered["compatible_neighbor_block_ids"] = anchor[
                "compatible_neighbor_block_ids"
            ]
    elif anchor["block_id"] in {QUANTUM_LINK_BLOCK_ID, QUANTUM_RING_BLOCK_ID}:
        for key in (
            "block_state",
            "expected_block_entity_id",
            "quantum_plane",
            "quantum_role",
            "formation_policy",
            "network_condition",
            "power_overlay_policy",
            "particle_policy",
        ):
            rendered[key] = anchor[key]
    elif anchor["block_id"] in M3_COMPLETION_BLOCK_ENTITY_IDS:
        for key in ("block_state", "expected_block_entity_id", "static_policy"):
            rendered[key] = anchor[key]
        rendered["expected_stock_triangle_count"] = anchor[
            "expected_stock_triangle_count"
        ]
        rendered["expected_stock_material_triangles"] = anchor[
            "expected_stock_material_triangles"
        ]
        if anchor["block_id"] == PAINT_BLOCK_ID:
            rendered["paint_splotches"] = list(anchor["paint_splotches"])
            rendered["paint_dots_signed_i8"] = list(anchor["paint_dots"])
            rendered["paint_dots_sha256"] = hashlib.sha256(
                bytes(value & 0xFF for value in anchor["paint_dots"])
            ).hexdigest()
        elif anchor["block_id"] in {
            SKY_STONE_CHEST_BLOCK_ID,
            SMOOTH_SKY_STONE_CHEST_BLOCK_ID,
        }:
            rendered["orientation"] = {
                "facing": anchor["facing"],
                "y_degrees": CHEST_Y_ROTATION[anchor["facing"]],
            }
        elif anchor["block_id"] == CRANK_BLOCK_ID:
            rendered["orientation"] = {
                "facing": anchor["facing"],
                "xyz_degrees": CRANK_ORIENTATION_ANGLES[anchor["facing"]],
            }
        elif anchor["block_id"] == INSCRIBER_BLOCK_ID:
            rendered["orientation"] = {
                "facing": anchor["facing"],
                "spin": anchor["spin"],
            }
        else:
            rendered["pylon_axis"] = anchor["pylon_axis"]
            rendered["pylon_axis_position"] = anchor["pylon_axis_position"]
    if anchor["expected_path"] in {"custom-s1", "stock-fallback-s1"}:
        rendered["expected_stock_triangle_count"] = 0
        for key in (
            "native_part_group",
            "installed_face",
            "orientation_orbit",
            "part_cable_type_requirement",
            "expected_visible_cable_core",
            "expected_facade_cutout_sixteenths",
            "plane_visual_local_axes",
            "plane_visual_local_bounds_sixteenths",
            "plane_visual_world_bounds_sixteenths",
            "plane_facade_cutout_local_axes",
            "plane_facade_cutout_local_bounds_sixteenths",
            "plane_facade_cutout_world_bounds_sixteenths",
            "plane_mask",
            "plane_mask_bit_order",
            "p2p_frequency_unsigned",
            "p2p_frequency_pixels",
            "facade_mask",
            "facade_mask_bit_order",
            "facade_material_class",
            "native_neutral_facade_materials",
            "facade_whitelist_controls",
            "native_facade_normalization",
            "quartz_facade_appearance_control",
            "quartz_facade_light_policy",
            "facade_expected_stilt",
            "facade_structural_expectation",
            "facade_adjacent_cull_expected",
            "facade_adjacent_render_expected",
            "plane_perpendicular_facade_intersection",
            "native_endpoints",
            "endpoint_straight_optimization",
            "schema9_route_disabled_projection",
        ):
            if key in anchor:
                rendered[key] = anchor[key]
        if anchor["expected_path"] == "custom-s1":
            rendered["expected_geometry_signature"] = anchor[
                "expected_geometry_signature"
            ]
            rendered["expected_nonlighting_attribute_signature"] = anchor[
                "expected_nonlighting_attribute_signature"
            ]
    if "ambiguous_neighbor" in anchor:
        rendered["ambiguous_neighbor"] = anchor["ambiguous_neighbor"]
        rendered["expected_triangle_count"] = anchor["expected_triangle_count"]
    if anchor.get("face_parts"):
        rendered["face_parts"] = [
            {"direction": direction, **anchor["face_parts"][direction]}
            for direction in DIRECTION_DELTAS
            if direction in anchor["face_parts"]
        ]
    if anchor.get("facades"):
        rendered["facades"] = [
            {
                "direction": direction,
                "block_state": anchor["facades"][direction],
            }
            for direction in DIRECTION_DELTAS
            if direction in anchor["facades"]
        ]
    if "fallback_reason" in anchor:
        rendered["fallback_reason"] = anchor["fallback_reason"]
        rendered["expected_triangle_count"] = anchor["expected_triangle_count"]
    if is_custom(anchor):
        rendered.update(
            {
                "expected_connections": anchor["expected_connections"],
                "expected_triangle_count": anchor["expected_triangle_count"],
                "expected_material_triangles": anchor[
                    "expected_material_triangles"
                ],
                "expected_smart_overlays": anchor["expected_smart_overlays"],
            }
        )
        if anchor["expected_terminal_layers"]:
            rendered["expected_terminal_layers"] = anchor[
                "expected_terminal_layers"
            ]
        if anchor["block_id"] in (DRIVE_BLOCK_ID, EXTENDED_DRIVE_BLOCK_ID):
            rendered["expected_drive_models"] = anchor["expected_drive_models"]
            rendered["expected_drive_led"] = anchor["expected_drive_led"]
        if anchor["block_id"] in CRAFTING_BLOCK_KINDS:
            rendered["expected_crafting_faces"] = anchor[
                "expected_crafting_faces"
            ]
        if anchor["block_id"] in {QUANTUM_LINK_BLOCK_ID, QUANTUM_RING_BLOCK_ID}:
            rendered["expected_quantum_primitives"] = anchor[
                "expected_quantum_primitives"
            ]
    return rendered


def _render_fixture_block(fixture_block: dict[str, Any]) -> dict[str, Any]:
    return {
        "position": dict(zip(("x", "y", "z"), fixture_block["position"])),
        **{
            key: value
            for key, value in fixture_block.items()
            if key != "position"
        },
    }


def _legacy_upgrade_raw_anchor(anchor: dict[str, Any]) -> dict[str, Any]:
    """Project one pre-oracle schema-9 anchor into the bounded raw input."""
    rendered: dict[str, Any] = {
        "position": dict(zip(("x", "y", "z"), anchor["position"])),
        "block_id": anchor["block_id"],
        "cable_id": anchor["cable_id"],
    }
    if anchor.get("face_parts"):
        rendered["face_parts"] = [
            {"direction": direction, **anchor["face_parts"][direction]}
            for direction in DIRECTION_DELTAS
            if direction in anchor["face_parts"]
        ]
    if anchor.get("facades"):
        rendered["facades"] = [
            {
                "direction": direction,
                "block_state": anchor["facades"][direction],
            }
            for direction in DIRECTION_DELTAS
            if direction in anchor["facades"]
        ]
    return rendered


def reconstruct_native_structural_legacy_input(
    source_cases: Iterable[dict[str, Any]],
) -> dict[str, Any]:
    """Rebuild the exact ten-row input without consulting either oracle."""
    by_id: dict[str, dict[str, Any]] = {}
    for case in source_cases:
        case_id = case.get("case_id")
        if isinstance(case_id, str) and case_id in {
            selector[0] for selector in LEGACY_UPGRADE_SELECTORS
        }:
            if case_id in by_id:
                raise ValueError(f"legacy upgrade source case is duplicated: {case_id}")
            by_id[case_id] = case
    if tuple(by_id) != tuple(selector[0] for selector in LEGACY_UPGRADE_SELECTORS):
        raise ValueError("legacy upgrade source case ordering or closure changed")

    endpoint_helpers = {
        "ae2-m1-02": {
            "block_id": "ae2:energy_acceptor",
            "expected_block_entity_id": "ae2:energy_acceptor",
            "expected_state": {},
        },
        "ae2-m1-03": {
            "block_id": "ae2:controller",
            "expected_block_entity_id": "ae2:controller",
            "expected_state": {"state": "offline", "type": "block"},
        },
    }
    rendered_cases: list[dict[str, Any]] = []
    seen_positions: set[tuple[int, int, int]] = set()
    for case_id, position in LEGACY_UPGRADE_SELECTORS:
        source_case = by_id[case_id]
        anchors = source_case.get("anchors")
        if (
            not isinstance(anchors, (list, tuple))
            or len(anchors) != 1
            or anchors[0].get("position") != position
            or position in seen_positions
        ):
            raise ValueError(f"legacy upgrade selector changed for {case_id}")
        seen_positions.add(position)
        anchor = anchors[0]
        if (
            anchor.get("block_id") != "ae2:cable_bus"
            or anchor.get("expected_triangle_count") != 0
            or not str(anchor.get("expected_path", "")).startswith("stock-fallback-")
        ):
            raise ValueError(f"legacy upgrade predecessor is not fallback-empty: {case_id}")

        x, y, z = position
        fixtures = [
            {
                "position": {"x": floor_x, "y": y - 1, "z": floor_z},
                "block_id": "minecraft:smooth_stone",
            }
            for floor_x in range(x - 1, x + 2)
            for floor_z in range(z - 1, z + 2)
        ]
        endpoint = endpoint_helpers.get(case_id)
        source_fixtures = source_case.get("fixture_blocks")
        if endpoint is None:
            if source_fixtures != ():
                raise ValueError(f"legacy upgrade source helper changed for {case_id}")
        else:
            expected_source_fixture = {
                "position": (x + 1, y, z),
                "block_id": endpoint["block_id"],
            }
            if source_fixtures != (expected_source_fixture,):
                raise ValueError(f"legacy endpoint source helper changed for {case_id}")
            fixtures.append(
                {
                    "position": {"x": x + 1, "y": y, "z": z},
                    **endpoint,
                }
            )
        rendered_cases.append(
            {
                "case_id": case_id,
                "anchors": [_legacy_upgrade_raw_anchor(anchor)],
                "fixture_blocks": fixtures,
            }
        )

    payload = {
        "schema_version": 1,
        "profile_id": NATIVE_STRUCTURAL_ROUTE,
        "coverage_id": LEGACY_UPGRADE_COVERAGE,
        "source_schema9": {
            "cases_size_bytes": LEGACY_UPGRADE_SCHEMA9_CASES_SIZE_BYTES,
            "cases_sha256": SCHEMA9_CANONICAL_SHA256,
            "gallery_size_bytes": LEGACY_UPGRADE_SCHEMA9_GALLERY_SIZE_BYTES,
            "gallery_sha256": LEGACY_UPGRADE_SCHEMA9_GALLERY_SHA256,
            "signature_schema_version": 9,
        },
        "synthetic_world": {
            "anchor_block_state": (
                "ae2:cable_bus[light_level=0,waterlogged=false]"
            ),
            "biome": "minecraft:plains",
            "blocklight": 0,
            "sunlight": 15,
            "support_block_state": "minecraft:smooth_stone",
            "support_patch": (
                "complete-3x3-plane-one-block-below-each-anchor"
            ),
        },
        "cases": rendered_cases,
    }
    if (
        len(rendered_cases) != EXPECTED_LEGACY_UPGRADE_CASE_COUNT
        or sum(len(case["anchors"]) for case in rendered_cases)
        != EXPECTED_LEGACY_UPGRADE_ANCHOR_COUNT
        or sum(len(case["fixture_blocks"]) for case in rendered_cases)
        != EXPECTED_LEGACY_UPGRADE_FIXTURE_BLOCK_COUNT
    ):
        raise ValueError("legacy upgrade raw input closure changed")
    return payload


def load_native_structural_legacy_input() -> dict[str, Any]:
    """Exact-gate and independently reconstruct the tracked raw input."""
    tracked = NATIVE_STRUCTURAL_LEGACY_INPUT_PATH.read_bytes()
    if (
        len(tracked) != LEGACY_UPGRADE_INPUT_SIZE_BYTES
        or hashlib.sha256(tracked).hexdigest() != LEGACY_UPGRADE_INPUT_SHA256
    ):
        raise ValueError("legacy upgrade raw input identity changed")
    reconstructed = reconstruct_native_structural_legacy_input(create_cases())
    if json_bytes(reconstructed) != tracked:
        raise ValueError("legacy upgrade raw input differs from source reconstruction")
    try:
        parsed = json.loads(tracked.decode("utf-8"))
    except (UnicodeDecodeError, json.JSONDecodeError) as exception:
        raise ValueError("legacy upgrade raw input is not canonical JSON") from exception
    if parsed != reconstructed:
        raise ValueError("legacy upgrade raw input semantic reconstruction changed")
    return parsed


def load_native_structural_legacy_oracle(
    expected_positions: set[tuple[int, int, int]],
) -> dict[tuple[int, int, int], dict[str, Any]]:
    """Strict-load the independent compiled-runtime oracle for the ten upgrades."""
    oracle_bytes = NATIVE_STRUCTURAL_LEGACY_ORACLE_PATH.read_bytes()
    if (
        len(oracle_bytes) != LEGACY_UPGRADE_ORACLE_SIZE_BYTES
        or hashlib.sha256(oracle_bytes).hexdigest() != LEGACY_UPGRADE_ORACLE_SHA256
    ):
        raise ValueError("legacy upgrade compiled-runtime oracle identity changed")
    try:
        payload = json.loads(oracle_bytes.decode("utf-8"))
    except (UnicodeDecodeError, json.JSONDecodeError) as exception:
        raise ValueError("legacy upgrade compiled-runtime oracle is not JSON") from exception
    if (
        not isinstance(payload, dict)
        or set(payload)
        != {
            "anchors",
            "coverage_id",
            "profile_id",
            "schema_version",
            "signature_schema_version",
        }
        or payload.get("schema_version") != 2
        or payload.get("signature_schema_version") != 10
        or payload.get("profile_id") != NATIVE_STRUCTURAL_ROUTE
        or payload.get("coverage_id") != LEGACY_UPGRADE_COVERAGE
        or not isinstance(payload.get("anchors"), dict)
    ):
        raise ValueError("legacy upgrade compiled-runtime oracle header changed")

    parsed: dict[tuple[int, int, int], dict[str, Any]] = {}
    for key, entry in payload["anchors"].items():
        try:
            position = tuple(int(value) for value in key.split())
        except (AttributeError, ValueError) as exception:
            raise ValueError("legacy upgrade oracle position key is malformed") from exception
        if (
            len(position) != 3
            or key != " ".join(str(value) for value in position)
            or position in parsed
            or not isinstance(entry, dict)
        ):
            raise ValueError("legacy upgrade oracle has a duplicate or malformed entry")
        materials = entry.get("material_triangles")
        triangle_count = entry.get("triangle_count")
        if (
            set(entry)
            != {
                "geometry_signature",
                "material_triangles",
                "nonlighting_attribute_signature",
                "triangle_count",
            }
            or not isinstance(triangle_count, int)
            or isinstance(triangle_count, bool)
            or triangle_count <= 0
            or not isinstance(materials, dict)
            or not materials
            or any(
                not isinstance(resource, str)
                or not isinstance(count, int)
                or isinstance(count, bool)
                or count <= 0
                for resource, count in materials.items()
            )
            or triangle_count != sum(materials.values())
            or any(
                not isinstance(entry.get(field), str)
                or len(entry[field]) != 64
                or any(character not in "0123456789abcdef" for character in entry[field])
                for field in (
                    "geometry_signature",
                    "nonlighting_attribute_signature",
                )
            )
        ):
            raise ValueError(f"legacy upgrade oracle entry is malformed at {position}")
        parsed[position] = {
            "triangle_count": entry["triangle_count"],
            "material_triangles": dict(sorted(materials.items())),
            "geometry_signature": entry["geometry_signature"],
            "nonlighting_attribute_signature": entry[
                "nonlighting_attribute_signature"
            ],
        }
    resources = {
        resource
        for entry in parsed.values()
        for resource in entry["material_triangles"]
    }
    if (
        set(parsed) != expected_positions
        or len(parsed) != EXPECTED_LEGACY_UPGRADE_ANCHOR_COUNT
        or sum(entry["triangle_count"] for entry in parsed.values())
        != EXPECTED_LEGACY_UPGRADE_CUSTOM_TRIANGLE_COUNT
        or len(resources) != EXPECTED_LEGACY_UPGRADE_IDENTITY_COUNT
        or sum(len(entry["material_triangles"]) for entry in parsed.values())
        != EXPECTED_LEGACY_UPGRADE_MATERIAL_ROW_COUNT
    ):
        raise ValueError("legacy upgrade oracle exact closure changed")
    return parsed


def native_structural_legacy_upgrades(
    rendered_cases: Iterable[dict[str, Any]],
) -> dict[str, Any]:
    """Build a non-mutating overlay over ten byte-frozen schema-9 anchors."""
    raw_input = load_native_structural_legacy_input()
    raw_by_selector: dict[tuple[str, tuple[int, int, int]], dict[str, Any]] = {}
    for raw_case in raw_input["cases"]:
        raw_anchor = raw_case["anchors"][0]
        raw_position = tuple(raw_anchor["position"][axis] for axis in ("x", "y", "z"))
        selector = (raw_case["case_id"], raw_position)
        if selector in raw_by_selector:
            raise ValueError("legacy upgrade raw selector is duplicated")
        raw_by_selector[selector] = raw_case
    if tuple(raw_by_selector) != LEGACY_UPGRADE_SELECTORS:
        raise ValueError("legacy upgrade raw selector order changed")

    rendered_by_selector: dict[
        tuple[str, tuple[int, int, int]], dict[str, Any]
    ] = {}
    for case in rendered_cases:
        for anchor in case["anchors"]:
            position = tuple(anchor["position"][axis] for axis in ("x", "y", "z"))
            selector = (case["case_id"], position)
            if selector not in raw_by_selector:
                continue
            if selector in rendered_by_selector:
                raise ValueError("legacy upgrade rendered selector is duplicated")
            rendered_by_selector[selector] = anchor
    if tuple(rendered_by_selector) != LEGACY_UPGRADE_SELECTORS:
        raise ValueError("legacy upgrade rendered selector closure changed")

    oracle = load_native_structural_legacy_oracle(
        {position for _case_id, position in LEGACY_UPGRADE_SELECTORS}
    )
    endpoint_contracts = {
        "ae2-m1-02": {
            "direction": "east",
            "block_id": "ae2:energy_acceptor",
            "block_entity_id": "ae2:energy_acceptor",
            "required_block_state": {},
            "observed_endpoint_side": "west",
            "side_rule": "ALL",
            "formation": None,
            "exposed_on_observed_side": True,
            "declared_family": "covered",
            "local_family": "glass",
            "effective_family": "glass",
            "collar": False,
            "topology": "native-grid-node-host",
        },
        "ae2-m1-03": {
            "direction": "east",
            "block_id": "ae2:controller",
            "block_entity_id": "ae2:controller",
            "required_block_state": {"state": "offline", "type": "block"},
            "observed_endpoint_side": "west",
            "side_rule": "ALL",
            "formation": None,
            "exposed_on_observed_side": True,
            "declared_family": "dense_smart",
            "local_family": "dense_smart",
            "effective_family": "dense_smart",
            "collar": False,
            "topology": "native-grid-node-host",
        },
    }
    rows: list[dict[str, Any]] = []
    for case_id, position in LEGACY_UPGRADE_SELECTORS:
        legacy_projection = rendered_by_selector[(case_id, position)]
        if (
            not legacy_projection["expected_path"].startswith("stock-fallback-")
            or legacy_projection.get("expected_triangle_count") != 0
            or "expected_material_triangles" in legacy_projection
        ):
            raise ValueError(f"legacy projection is no longer fallback-empty: {case_id}")
        raw_case = raw_by_selector[(case_id, position)]
        endpoint = endpoint_contracts.get(case_id)
        endpoint_helpers = [
            fixture
            for fixture in raw_case["fixture_blocks"]
            if fixture["position"]["y"] == position[1]
        ]
        if endpoint is None:
            if endpoint_helpers:
                raise ValueError(f"legacy upgrade has an unexpected endpoint: {case_id}")
            endpoints: list[dict[str, Any]] = []
        else:
            if (
                len(endpoint_helpers) != 1
                or endpoint_helpers[0].get("block_id") != endpoint["block_id"]
                or endpoint_helpers[0].get("expected_block_entity_id")
                != endpoint["block_entity_id"]
                or endpoint_helpers[0].get("expected_state")
                != endpoint["required_block_state"]
            ):
                raise ValueError(f"legacy upgrade endpoint input changed: {case_id}")
            endpoints = [endpoint]
        entry = oracle[position]
        rows.append(
            {
                "case_id": case_id,
                "position": dict(zip(("x", "y", "z"), position)),
                "legacy_projection": json.loads(json.dumps(legacy_projection)),
                "physical_stock": {
                    "expected_path": "stock-empty",
                    "expected_triangle_count": 0,
                    "expected_material_triangles": {},
                },
                "enabled": {
                    "expected_path": "custom-s1",
                    "expected_connections": endpoints,
                    "expected_triangle_count": entry["triangle_count"],
                    "expected_material_triangles": entry["material_triangles"],
                    "expected_smart_overlays": {},
                    "expected_geometry_signature": entry["geometry_signature"],
                    "expected_nonlighting_attribute_signature": entry[
                        "nonlighting_attribute_signature"
                    ],
                    "native_endpoints": endpoints,
                },
            }
        )

    return {
        "schema_version": 1,
        "profile_id": NATIVE_STRUCTURAL_ROUTE,
        "coverage_id": LEGACY_UPGRADE_COVERAGE,
        "input": {
            "path": NATIVE_STRUCTURAL_LEGACY_INPUT_PATH.name,
            "size_bytes": LEGACY_UPGRADE_INPUT_SIZE_BYTES,
            "sha256": LEGACY_UPGRADE_INPUT_SHA256,
            "schema_version": 1,
            "case_count": EXPECTED_LEGACY_UPGRADE_CASE_COUNT,
            "anchor_count": EXPECTED_LEGACY_UPGRADE_ANCHOR_COUNT,
            "fixture_block_count": EXPECTED_LEGACY_UPGRADE_FIXTURE_BLOCK_COUNT,
            "source_schema9": raw_input["source_schema9"],
        },
        "oracle": {
            "path": NATIVE_STRUCTURAL_LEGACY_ORACLE_PATH.name,
            "size_bytes": LEGACY_UPGRADE_ORACLE_SIZE_BYTES,
            "sha256": LEGACY_UPGRADE_ORACLE_SHA256,
            "schema_version": 2,
            "signature_schema_version": 10,
            "anchor_count": EXPECTED_LEGACY_UPGRADE_ANCHOR_COUNT,
            "triangle_count": EXPECTED_LEGACY_UPGRADE_CUSTOM_TRIANGLE_COUNT,
            "identity_count": EXPECTED_LEGACY_UPGRADE_IDENTITY_COUNT,
            "material_row_count": EXPECTED_LEGACY_UPGRADE_MATERIAL_ROW_COUNT,
        },
        "summary": {
            "case_count": EXPECTED_LEGACY_UPGRADE_CASE_COUNT,
            "anchor_count": EXPECTED_LEGACY_UPGRADE_ANCHOR_COUNT,
            "custom_anchor_count": EXPECTED_LEGACY_UPGRADE_ANCHOR_COUNT,
            "custom_triangle_count": EXPECTED_LEGACY_UPGRADE_CUSTOM_TRIANGLE_COUNT,
            "selected_resource_count": EXPECTED_LEGACY_UPGRADE_IDENTITY_COUNT,
            "new_selected_resource_count": 0,
            "material_row_count": EXPECTED_LEGACY_UPGRADE_MATERIAL_ROW_COUNT,
            "combined_native_structural_custom_anchor_count": (
                S1_ORACLE_ANCHOR_COUNT + EXPECTED_LEGACY_UPGRADE_ANCHOR_COUNT
            ),
            "combined_native_structural_custom_triangle_count": (
                S1_ORACLE_TRIANGLE_COUNT
                + EXPECTED_LEGACY_UPGRADE_CUSTOM_TRIANGLE_COUNT
            ),
            "combined_native_structural_selected_resource_count": (
                S1_ORACLE_IDENTITY_COUNT
            ),
            "combined_native_structural_material_row_count": (
                S1_ORACLE_MATERIAL_ROW_COUNT
                + EXPECTED_LEGACY_UPGRADE_MATERIAL_ROW_COUNT
            ),
            "physical_stock_projection": {
                "rendered_anchor_count": 0,
                "empty_anchor_count": EXPECTED_LEGACY_UPGRADE_ANCHOR_COUNT,
                "triangle_count": 0,
                "resource_count": 0,
                "resources": [],
            },
        },
        "rows": rows,
    }


def _render_bounds(
    bounds: tuple[tuple[int, int, int], tuple[int, int, int]]
) -> dict[str, tuple[int, int, int]]:
    return {"min": bounds[0], "max": bounds[1]}


def s1_rendered_logical_matrix_sha256(
    rendered_cases: Iterable[dict[str, Any]],
) -> str:
    """Hash every oracle-independent S1 case, anchor and helper field."""
    logical_cases = json.loads(
        json.dumps(
            [case for case in rendered_cases if case.get("milestone") == "S1"],
            sort_keys=True,
            separators=(",", ":"),
        )
    )
    oracle_fields = (
        "expected_connections",
        "expected_triangle_count",
        "expected_material_triangles",
        "expected_smart_overlays",
        "expected_geometry_signature",
        "expected_nonlighting_attribute_signature",
    )
    for case in logical_cases:
        for anchor in case["anchors"]:
            for field in oracle_fields:
                anchor.pop(field, None)
    payload = json.dumps(
        logical_cases,
        sort_keys=True,
        separators=(",", ":"),
    ).encode("utf-8")
    return hashlib.sha256(payload).hexdigest()


def _schema9_view(schema10: dict[str, object]) -> dict[str, object]:
    """Project schema 10 to the byte-frozen accepted schema-9 manifest."""
    view = json.loads(json.dumps(schema10))
    view["schema_version"] = 9
    view["signature_schema_version"] = 9
    view["cases"] = [
        case for case in view["cases"] if case.get("milestone") != "S1"
    ]
    view["case_count"] = 122
    view["anchor_count"] = 597
    view["expected_custom_summary"] = {
        "anchor_count": 579,
        "selected_resource_count": 218,
        "triangle_count": 26_580,
    }
    view["expected_stock_fallback_summary"] = {
        "anchor_count": 17,
        "triangle_count": 0,
    }
    view.pop("s1_summary", None)
    view.pop("s1_floor_policy", None)
    view.pop("native_structural_legacy_upgrades", None)
    view.get("bounds", {}).pop("s1_fixture", None)
    profile = view["profile"]
    profile["coverage_milestone"] = "M3f"
    profile["selected_resources"] = profile["selected_resources"][:217]
    for key in (
        "supported_native_structural",
        "native_structural_resources",
        "native_structural_profile",
    ):
        profile.pop(key, None)
    return view


def _schema8_view(schema9: dict[str, object]) -> dict[str, object]:
    """Project schema 9 back to the byte-frozen accepted schema-8 manifest."""
    view = json.loads(json.dumps(schema9))
    view["schema_version"] = 8
    view["signature_schema_version"] = 8
    view["cases"] = [
        case for case in view["cases"] if case.get("milestone") != "M3f"
    ]
    view["case_count"] = 115
    view["anchor_count"] = 519
    view["expected_custom_summary"] = {
        "anchor_count": 501,
        "selected_resource_count": 203,
        "triangle_count": 23_758,
    }
    view["expected_stock_fallback_summary"] = {
        "anchor_count": 17,
        "triangle_count": 0,
    }
    view.pop("m3f_summary", None)
    view.pop("m3f_floor_policy", None)
    view.get("bounds", {}).pop("m3f_fixture", None)
    profile = view["profile"]
    profile["coverage_milestone"] = "M3e"
    profile["selected_resources"] = [
        resource
        for resource in profile["selected_resources"]
        if resource not in M3_COMPLETION_RESOURCES
    ]
    for key in (
        "supported_m3_completion",
        "m3_completion_resources",
        "m3_completion_profile",
    ):
        profile.pop(key, None)
    view["optional_dense_fixture"]["exclusive_forceload_chunks"] = [
        {"x": x, "z": z}
        for x, z in (
            (16, 11), (17, 11), (17, 12), (17, 14), (17, 15),
            (18, 11), (18, 12), (18, 14), (18, 15),
            (19, 11), (19, 12), (19, 14), (19, 15),
        )
    ]
    return view


def _schema7_view(schema8: dict[str, object]) -> dict[str, object]:
    """Project schema 8 back to the byte-frozen accepted schema-7 manifest."""
    view = json.loads(json.dumps(schema8))
    view["schema_version"] = 7
    view["signature_schema_version"] = 7
    view["cases"] = [
        case for case in view["cases"] if case.get("milestone") != "M3e"
    ]
    view["case_count"] = 112
    view["anchor_count"] = 492
    view["expected_custom_summary"] = {
        "anchor_count": 474,
        "selected_resource_count": 201,
        "triangle_count": 22_570,
    }
    view["expected_stock_fallback_summary"] = {
        "anchor_count": 17,
        "triangle_count": 0,
    }
    view.pop("m3e_summary", None)
    view.pop("m3e_floor_policy", None)
    view.get("bounds", {}).pop("m3e_fixture", None)
    profile = view["profile"]
    profile["coverage_milestone"] = "M3d"
    profile["selected_resources"] = [
        resource
        for resource in profile["selected_resources"]
        if resource not in QUANTUM_NEW_RESOURCES
    ]
    for key in ("supported_quantum_bridge", "quantum_resources"):
        profile.pop(key, None)
    return view


def _render_cases(cases: Iterable[dict[str, Any]]) -> list[dict[str, Any]]:
    return [
        {
            "case_id": case["case_id"],
            "milestone": case["milestone"],
            "route": case["route"],
            **(
                {"coverage_id": case["coverage_id"]}
                if case["milestone"] == "S1"
                else {}
            ),
            "label": case["label"],
            "category": case["category"],
            "anchors": [_render_anchor(anchor) for anchor in case["anchors"]],
            "fixture_blocks": [
                _render_fixture_block(fixture_block)
                for fixture_block in case["fixture_blocks"]
            ],
        }
        for case in cases
    ]


def _m45_source_projection_resources(
    anchor: dict[str, Any], route_profile: dict[str, Any]
) -> tuple[str, ...]:
    route = anchor["m45_route"]
    block_id = anchor["block_id"]
    resources = tuple(route_profile["route_resources"])
    source_resources = tuple(route_profile["source_resources"])
    block_state = anchor.get("block_state", {})

    if route == "advanced-ae-athena":
        selected = source_resources
    elif route == "appflux":
        suffix = block_id.split(":", 1)[1]
        selected = tuple(resource for resource in resources if suffix in resource)
    elif route == "merequester":
        expected = (
            set(M45_REQUESTER_ORIGINAL_MATERIAL_TRIANGLES)
            if block_id == "merequester:requester"
            and block_state.get("active") == "false"
            else {
                "merequester:block/requester_active"
                if block_state.get("active") == "true"
                else "merequester:block/requester"
            }
        )
        selected = tuple(
            resource
            for resource in route_profile["stock_material_allowlist"]
            if resource in expected
        )
    elif route == "expandedae":
        suffix = block_id.split(":", 1)[1]
        if suffix.startswith("exp_crafting_accelerator_"):
            tier = suffix.removeprefix("exp_crafting_accelerator_")
            selected = tuple(
                resource
                for resource in resources
                if resource.endswith("/" + suffix)
                or resource.endswith("/" + suffix + "_light")
                or resource.rsplit("/", 1)[-1]
                in {
                    "light_base",
                    "ring_corner",
                    "ring_side_hor",
                    "ring_side_ver",
                    "unit_base",
                }
                or resource.endswith("_" + tier)
                or resource.endswith("_" + tier + "_light")
            )
        elif suffix == "exp_crafting_unit":
            selected = tuple(
                resource
                for resource in resources
                if resource.rsplit("/", 1)[-1]
                in {
                    "exp_crafting_unit",
                    "ring_corner",
                    "ring_side_hor",
                    "ring_side_ver",
                    "unit_base",
                }
            )
        elif suffix == "exp_io_port":
            powered = block_state.get("powered")
            if powered not in M45_EXP_IO_ORIGINAL_MATERIAL_TRIANGLES:
                raise ValueError("M4/M5 I/O Port source projection state changed")
            expected = set(M45_EXP_IO_ORIGINAL_MATERIAL_TRIANGLES[powered])
            selected = tuple(
                resource
                for resource in route_profile["stock_material_allowlist"]
                if resource in expected
            )
        elif suffix == "exp_pattern_provider":
            selected = tuple(
                resource
                for resource in resources
                if ":block/exp_pattern_provider" in resource
                or ":block/generics/" in resource
            )
        else:
            selected = ()
    elif route == "megacells":
        suffix = block_id.split(":", 1)[1]
        if "crafting" in suffix or suffix.startswith("mega_"):
            selected = tuple(
                resource for resource in resources if ":block/crafting/" in resource
            )
        else:
            selected = tuple(resource for resource in resources if suffix in resource)
    elif route == "advanced-ae-quantum":
        suffix = block_id.split(":", 1)[1]
        selected = tuple(
            resource
            for resource in resources
            if resource.rsplit("/", 1)[-1].startswith(suffix)
            or (
                block_state.get("formed") == "true"
                and ":block/crafting/quantum_internal_" in resource
            )
        )
    elif route == "extendedae-matrix":
        suffix = block_id.removeprefix("extendedae:assembler_matrix_")
        if suffix == "frame":
            selected = tuple(resource for resource in resources if "/frame_" in resource)
        elif suffix == "glass":
            selected = tuple(resource for resource in resources if "/glass/" in resource)
        else:
            selected = tuple(resource for resource in resources if suffix in resource)
    else:
        selected = ()

    selected = tuple(sorted(set(selected)))
    if not selected:
        raise ValueError(
            f"M4/M5 nonempty stock projection lacks source resources: "
            f"{route} {block_id}"
        )
    return selected


def _render_m45_projection(
    anchor: dict[str, Any],
    projection: dict[str, Any],
    route_profile: dict[str, Any],
) -> dict[str, Any]:
    rendered = dict(projection)
    if projection["review_projection"] == "empty":
        allowed_resources: tuple[str, ...] = ()
    elif projection["expected_path"] == "native-center-only-m45":
        allowed_resources = M45_NATIVE_CENTER_PROJECTION_RESOURCES
    elif (
        projection["expected_path"] == anchor["expected_path"]
        and projection["review_projection"] == anchor["review_projection"]
    ):
        # This metadata is a no-op marker: _m45_mode_projection deliberately
        # returns None for it, so enabled validation continues to use the
        # route profile. Keep only the route-local closure here instead of
        # repeating the cumulative schema-10 material set on every anchor.
        allowed_resources = tuple(
            [
                *route_profile["route_resources"],
                *route_profile["dependency_resources"],
            ]
        )
    else:
        allowed_resources = _m45_source_projection_resources(anchor, route_profile)
    rendered["allowed_resources"] = sorted(set(allowed_resources))
    exact_materials = _m45_exact_inherited_projection_materials(anchor, projection)
    if exact_materials is not None:
        if set(exact_materials) != set(allowed_resources):
            raise ValueError(
                "M4/M5 inherited-model exact material/allowlist closure changed"
            )
        rendered["expected_material_triangles"] = dict(
            sorted(exact_materials.items())
        )
    return rendered


def _render_m45_anchor(
    anchor: dict[str, Any], route_profile: dict[str, Any]
) -> dict[str, Any]:
    rendered = {
        "position": dict(zip(("x", "y", "z"), anchor["position"])),
        "block_id": anchor["block_id"],
        "expected_path": anchor["expected_path"],
        "m45_route": anchor["m45_route"],
        "review_projection": anchor["review_projection"],
        "source_derived_synthetic_fixture": anchor[
            "source_derived_synthetic_fixture"
        ],
    }
    if anchor["expected_path"] == "custom-m45":
        rendered.update(
            {
                "expected_geometry_signature": anchor[
                    "expected_geometry_signature"
                ],
                "expected_material_triangles": anchor[
                    "expected_material_triangles"
                ],
                "expected_nonlighting_attribute_signature": anchor[
                    "expected_nonlighting_attribute_signature"
                ],
                "expected_triangle_count": anchor[
                    "expected_triangle_count"
                ],
            }
        )
    for key in (
        "block_state",
        "placement_state",
        "expected_block_entity_id",
        "expected_nbt",
        "fallback_reason",
        "fixture_role",
        "installed_face",
        "z_rotation_source",
        "expected_rotation_degrees",
        "plane_mask",
        "plane_mask_bit_order",
        "selector_scoped_model_exception",
    ):
        if key in anchor:
            rendered[key] = anchor[key]
    rendered["route_disabled_projections"] = {
        route: _render_m45_projection(anchor, projection, route_profile)
        for route, projection in anchor["route_disabled_projections"].items()
    }
    for key in (
        "physical_stock_projection",
        "native_structural_disabled_projection",
        "crafting_disabled_projection",
    ):
        if key in anchor:
            rendered[key] = _render_m45_projection(
                anchor, anchor[key], route_profile
            )
    if anchor.get("cable_id") is not None:
        rendered["cable_id"] = anchor["cable_id"]
    if anchor.get("face_parts"):
        rendered["face_parts"] = [
            {"direction": direction, **part}
            for direction, part in anchor["face_parts"].items()
        ]
    if "drive_inventory" in anchor:
        rendered["inventory"] = {
            "compound": "inv",
            "slots": [
                {
                    "slot": slot,
                    "field": f"item{slot}",
                    **({"empty": True} if item is None else {"item_stack": item}),
                }
                for slot, item in enumerate(anchor["drive_inventory"])
            ],
        }
    return rendered


def _render_m45_cases(
    cases: Iterable[dict[str, Any]], route_profiles: Iterable[dict[str, Any]]
) -> list[dict[str, Any]]:
    profiles = {profile["route"]: profile for profile in route_profiles}
    return [
        {
            "case_id": case["case_id"],
            "milestone": case["milestone"],
            "route": case["route"],
            "label": case["label"],
            "category": case["category"],
            "anchors": [
                _render_m45_anchor(anchor, profiles[case["route"]])
                for anchor in case["anchors"]
            ],
            "fixture_blocks": [
                _render_fixture_block(fixture) for fixture in case["fixture_blocks"]
            ],
        }
        for case in cases
    ]


def _schema10_manifest() -> dict[str, object]:
    exact_profile = load_profile_contract()
    m3_completion_profile = load_m3_completion_profile_contract()
    native_structural_profile = load_native_structural_profile_contract()
    center_parts = exact_profile["supportedCenterParts"]
    resources = exact_profile["textures"]
    core_resources = exact_profile["coreTextures"]
    drive_resources = exact_profile["driveTextures"]
    rendered_cases = _render_cases(CASES)
    legacy_upgrades = native_structural_legacy_upgrades(rendered_cases)
    legacy_upgrade_resource_union = {
        resource
        for row in legacy_upgrades["rows"]
        for resource in row["enabled"]["expected_material_triangles"]
    }
    expected_resource_union = sorted(
        {
            resource
            for case in CASES
            for anchor in case["anchors"]
            if is_custom(anchor)
            for resource in anchor["expected_material_triangles"]
        }
        | legacy_upgrade_resource_union
    )
    legacy_selected_resources = (
        resources
        + list(EXTENDED_DRIVE_SELECTED_RESOURCES)
        + list(CONNECTED_GLASS_SELECTED_RESOURCES)
        + list(CRAFTING_RESOURCES)
        + list(QUANTUM_NEW_RESOURCES)
        + list(M3_COMPLETION_RESOURCES)
    )
    if len(legacy_selected_resources) != 217:
        raise ValueError("M3f profile must select exactly 217 unique texture keys")
    if len(set(legacy_selected_resources)) != len(legacy_selected_resources):
        raise ValueError("M3f profile contains duplicate selected texture keys")
    s1_resource_union = {
        resource
        for case in CASES
        for anchor in case["anchors"]
        if anchor["expected_path"] == "custom-s1"
        for resource in anchor["expected_material_triangles"]
    }
    if (
        len(s1_resource_union) != S1_ORACLE_IDENTITY_COUNT
        or len(legacy_upgrade_resource_union)
        != EXPECTED_LEGACY_UPGRADE_IDENTITY_COUNT
        or not legacy_upgrade_resource_union <= s1_resource_union
        or len(s1_resource_union | legacy_upgrade_resource_union)
        != S1_ORACLE_IDENTITY_COUNT
    ):
        raise ValueError("legacy upgrade/S1 selected resource closure changed")
    schema9_disabled_projection_resources = {
        resource
        for case in CASES
        if case["milestone"] == "S1"
        for anchor in case["anchors"]
        for resource in anchor["schema9_route_disabled_projection"][
            "expected_material_triangles"
        ]
    }
    if len(schema9_disabled_projection_resources) != S1_SCHEMA9_DISABLED_RESOURCE_COUNT:
        raise ValueError("S1 schema-9 route-disabled resource closure changed")
    selected_resources = legacy_selected_resources + sorted(
        s1_resource_union - set(legacy_selected_resources)
    )
    expected_resource_contract = sorted(set(selected_resources) | {STONE_TEXTURE})
    if expected_resource_union != expected_resource_contract:
        missing = sorted(set(expected_resource_contract) - set(expected_resource_union))
        unexpected = sorted(set(expected_resource_union) - set(expected_resource_contract))
        raise ValueError(
            f"gallery does not select its exact S1 resource closure; missing={missing}, "
            f"unexpected={unexpected}"
        )
    m1_resource_union = {
        resource
        for case in CASES
        for anchor in case["anchors"]
        if anchor["expected_path"] == "custom-m1"
        for resource in anchor["expected_material_triangles"]
    }
    expected_m1_resources = set(core_resources) - set(TERMINAL_MATERIAL_TRIANGLES)
    if (
        len(expected_m1_resources) != EXPECTED_M1_SELECTED_RESOURCE_COUNT
        or m1_resource_union != expected_m1_resources
    ):
        raise ValueError("M1 regression no longer selects its exact 140-resource closure")
    m2_resource_union = {
        resource
        for case in CASES
        for anchor in case["anchors"]
        if anchor["expected_path"] == "custom-m2"
        for resource in anchor["expected_material_triangles"]
    }
    if len(m2_resource_union) != 16:
        raise ValueError(
            f"expected 16 M2 selected resources, got {len(m2_resource_union)}"
        )
    m3_resource_union = {
        resource
        for case in CASES
        for anchor in case["anchors"]
        if anchor["expected_path"] == "custom-m3"
        for resource in anchor["expected_material_triangles"]
    }
    if m3_resource_union != set(drive_resources):
        raise ValueError("M3a gallery does not select its exact 10-resource closure")
    m3b_resource_union = {
        resource
        for case in CASES
        for anchor in case["anchors"]
        if anchor["expected_path"] == "custom-m3b"
        for resource in anchor["expected_material_triangles"]
    }
    if (
        len(m3b_resource_union) != 13
        or m3b_resource_union - set(resources)
        != set(EXTENDED_DRIVE_SELECTED_RESOURCES)
    ):
        raise ValueError("M3b gallery does not select its exact 13-resource closure")
    m3c_resource_union = {
        resource
        for case in CASES
        for anchor in case["anchors"]
        if anchor["expected_path"] == "custom-m3c"
        for resource in anchor["expected_material_triangles"]
    }
    if m3c_resource_union != set(CONNECTED_GLASS_SELECTED_RESOURCES):
        raise ValueError("M3c gallery does not select its exact 19-resource closure")
    m3d_resource_union = {
        resource
        for case in CASES
        for anchor in case["anchors"]
        if anchor["expected_path"] == "custom-m3d"
        for resource in anchor["expected_material_triangles"]
    }
    if m3d_resource_union != set(CRAFTING_RESOURCES):
        raise ValueError("M3d gallery does not select its exact 15-resource closure")
    m3e_resource_union = {
        resource
        for case in CASES
        for anchor in case["anchors"]
        if anchor["expected_path"] == "custom-m3e"
        for resource in anchor["expected_material_triangles"]
    }
    if m3e_resource_union != set(QUANTUM_RESOURCES):
        raise ValueError("M3e gallery does not emit its exact 4-resource closure")
    m3f_resource_union = {
        resource
        for case in CASES
        for anchor in case["anchors"]
        if anchor["expected_path"] == "custom-m3f"
        for resource in anchor["expected_material_triangles"]
    }
    if m3f_resource_union != set(M3_COMPLETION_RESOURCES):
        raise ValueError("M3f gallery does not emit its exact 15-resource closure")
    m3_occupied_slots = sum(
        item is not None
        for case in CASES
        for anchor in case["anchors"]
        if anchor["expected_path"] == "custom-m3"
        for item in anchor["drive_inventory"]
    )
    if m3_occupied_slots != 61:
        raise ValueError(f"expected 61 occupied M3a slots, got {m3_occupied_slots}")
    m3b_occupied_slots = sum(
        item is not None
        for case in CASES
        for anchor in case["anchors"]
        if anchor["expected_path"] == "custom-m3b"
        for item in anchor["drive_inventory"]
    )
    if m3b_occupied_slots != 84:
        raise ValueError(f"expected 84 occupied M3b slots, got {m3b_occupied_slots}")
    s1_logical_matrix_sha256 = s1_rendered_logical_matrix_sha256(rendered_cases)
    if (
        any(case["milestone"] == "S1" for case in CASES)
        and s1_logical_matrix_sha256 != S1_RENDERED_LOGICAL_MATRIX_SHA256
    ):
        raise ValueError("the frozen oracle-independent S1 logical matrix changed")
    frozen_prefix = json.dumps(
        rendered_cases[:103],
        sort_keys=True,
        separators=(",", ":"),
    ).encode("utf-8")
    if hashlib.sha256(frozen_prefix).hexdigest() != SCHEMA6_CASE_PREFIX_SHA256:
        raise ValueError("the frozen first 103 schema-6 cases changed")
    manifest: dict[str, object] = {
        "schema_version": 10,
        "signature_schema_version": 10,
        "license": "LGPL-3.0-only",
        "profile": {
            "mod_id": "ae2",
            "version": "19.2.17",
            "coverage_milestone": NATIVE_STRUCTURAL_COVERAGE,
            "transient_policy": "idle-off-unknown",
            "supported_center_parts": center_parts,
            "supported_face_parts": [
                {"id": TERMINAL_PART_ID, "spins": [0, 1, 2, 3]}
            ],
            "facade_policy": {
                "block_state": {"Name": STONE_BLOCK_ID},
                "properties": "forbidden",
                "maximum_facades": 1,
                "required_same_face_part": TERMINAL_PART_ID,
            },
            "supported_drive": {
                "block_id": DRIVE_BLOCK_ID,
                "slot_count": DRIVE_SLOT_COUNT,
                "base_model": DRIVE_BASE_MODEL,
                "empty_cell_model": DRIVE_EMPTY_CELL_MODEL,
                "explicit_cell_models": DRIVE_EXPLICIT_CELL_MODELS,
                "generic_cell_model": {
                    "model": DRIVE_GENERIC_CELL_MODEL,
                    "item_ids": list(DRIVE_GENERIC_CELL_IDS),
                },
                "supported_cell_id_count": len(DRIVE_CELL_MODELS),
                "occupied_model_count": DRIVE_OCCUPIED_MODEL_COUNT,
                "led_policy": DRIVE_LED_POLICY,
                "unknown_cell_policy": DRIVE_UNKNOWN_CELL_POLICY,
            },
            "supported_extended_drive": {
                "block_id": EXTENDED_DRIVE_BLOCK_ID,
                "slot_count": EXTENDED_DRIVE_SLOT_COUNT,
                "front_slot_count": EXTENDED_DRIVE_FACE_SLOT_COUNT,
                "back_slot_count": EXTENDED_DRIVE_FACE_SLOT_COUNT,
                "back_orientation_policy": "opposite-facing-same-spin",
                "base_model": EXTENDED_DRIVE_BASE_MODEL,
                "empty_cell_model": EXTENDED_DRIVE_EMPTY_CELL_MODEL,
                "accepted_cell_models": EXTENDED_DRIVE_CELL_MODELS,
                "accepted_ae2_cell_id_count": len(DRIVE_CELL_MODELS),
                "accepted_extension_cell_id_count": len(
                    EXTENDED_DRIVE_NATIVE_CELL_MODELS
                ),
                "supported_cell_id_count": len(EXTENDED_DRIVE_CELL_MODELS),
                "occupied_model_count": EXTENDED_DRIVE_OCCUPIED_MODEL_COUNT,
                "base_triangle_count": EXTENDED_DRIVE_BASE_TRIANGLE_COUNT,
                "occupied_slot_triangle_count": (
                    DRIVE_CELL_CHASSIS_TRIANGLE_COUNT + DRIVE_LED_TRIANGLE_COUNT
                ),
                "triangle_formula": "116+16N",
                "led_policy": DRIVE_LED_POLICY,
                "unknown_cell_policy": DRIVE_UNKNOWN_CELL_POLICY,
            },
            "extension_profiles": [
                {
                    "mod_id": "extendedae",
                    "version": EXTENDEDAE_ARTIFACT["version"],
                    "artifact": EXTENDEDAE_ARTIFACT,
                    "selected_resources": list(EXTENDED_DRIVE_SELECTED_RESOURCES),
                    "required_resources_sha256": (
                        EXTENDEDAE_REQUIRED_RESOURCE_DIGESTS
                    ),
                }
            ],
            "supported_connected_glass": {
                "block_ids": list(CONNECTED_GLASS_BLOCK_IDS),
                "route": CONNECTED_GLASS_ROUTE,
                "base_resources": list(CONNECTED_GLASS_BASE_RESOURCES),
                "frame_resources": list(CONNECTED_GLASS_FRAME_RESOURCES),
                "variants_share_geometry_and_material_family": True,
                "base_selection": "position-seeded-legacy-random-three-draw-v1",
                "face_triangles": "c1-c2-c3+c1-c3-c4",
                "frame_mask_policy": "four-local-connected-neighbor-absence-bits",
                "render_type": "cutout-binary-alpha",
            },
            "glass_resources": list(CONNECTED_GLASS_SELECTED_RESOURCES),
            "supported_formed_crafting": {
                "block_kinds": CRAFTING_BLOCK_KINDS,
                "route": CRAFTING_ROUTE,
                "state_gate": {"formed": True},
                "powered_overlay_policy": "persisted-powered-fullbright-15",
                "connection_policy": "six-direct-abstract-crafting-unit-neighbors",
                "monitor_paint_ordinals": list(range(17)),
                "monitor_display_policy": CRAFTING_MONITOR_DISPLAY_POLICY,
                "compatible_extension_policy": "atomic-original-resource-fallback",
            },
            "crafting_resources": list(CRAFTING_RESOURCES),
            "crafting_resource_manifest_sha256": CRAFTING_RESOURCE_MANIFEST_SHA256,
            "crafting_texture_manifest_sha256": CRAFTING_TEXTURE_MANIFEST_SHA256,
            "supported_quantum_bridge": {
                "block_ids": [QUANTUM_LINK_BLOCK_ID, QUANTUM_RING_BLOCK_ID],
                "block_entity_id": QUANTUM_BLOCK_ENTITY_ID,
                "route": QUANTUM_ROUTE,
                "state_gate": {"formed": True, "waterlogged": False},
                "topology": "complete-isolated-three-by-three-plane",
                "plane_orientations": list(QUANTUM_PLANES),
                "render_type": "cutout-binary-alpha",
                "ambient_occlusion": "client-cable-bus-neighbor-sampling",
                "gallery_ambient_occlusion_raw_u8": 255,
                "light_policy": "world-derived-own-and-outward-face-maximum",
                "power_overlay_policy": QUANTUM_STATIC_POLICY,
                "particle_policy": QUANTUM_PARTICLE_POLICY,
            },
            "quantum_resources": list(QUANTUM_RESOURCES),
            "supported_m3_completion": {
                "route": M3_COMPLETION_ROUTE,
                "block_ids": list(M3_COMPLETION_BLOCK_ENTITY_IDS),
                "block_entity_ids": M3_COMPLETION_BLOCK_ENTITY_IDS,
                "render_policy": {
                    "paint": PAINT_STATIC_POLICY,
                    "sky_stone_chests": CHEST_STATIC_POLICY,
                    "crank": CRANK_STATIC_POLICY,
                    "inscriber": INSCRIBER_STATIC_POLICY,
                    "spatial_pylon": SPATIAL_PYLON_STATIC_POLICY,
                },
                "paint_dot_count": [1, 21],
                "paint_dots_byte_length": 256,
                "paint_lumen_gallery_coverage": False,
                "sky_chest_pose": "closed",
                "crank_pose": "neutral-zero-degrees",
                "inscriber_pose": "neutral-no-items-no-animation",
                "spatial_pylon_topology": (
                    "bounded-locally-invalid-component-unformed-base-plus-dim"
                ),
                "spatial_pylon_axis_scan_bounds": {
                    "maximum_axis_scan_blocks": 256,
                    "incomplete_missing_malformed_or_capped": (
                        "atomic-original-resource-fallback"
                    ),
                },
                "atomic_fallback": "per-block-original-resource-model",
            },
            "m3_completion_resources": list(M3_COMPLETION_RESOURCES),
            "m3_completion_profile": {
                "profile_id": m3_completion_profile["profileId"],
                "profile_size_bytes": 9_405,
                "profile_sha256": (
                    "281a335d3024ebbb97c6268e768826c467d6f7ea660989fd3dae204c6c03abf3"
                ),
                "artifact": m3_completion_profile["artifact"],
                "artifact_size_bytes": m3_completion_profile["sizeBytes"],
                "artifact_sha256": m3_completion_profile["sha256"],
                "source_tag": m3_completion_profile["source"]["tag"],
                "source_commit": m3_completion_profile["source"]["commit"],
                "source_sha256": m3_completion_profile["source"]["sha256"],
                "path_count": m3_completion_profile["resourcePartition"][
                    "pathCount"
                ],
                "required_resources_manifest_sha256": m3_completion_profile[
                    "requiredResourcesManifestSha256"
                ],
                "emitted_static_texture_manifest_sha256": m3_completion_profile[
                    "emittedStaticTextureManifestSha256"
                ],
                "fallback_texture_manifest_sha256": m3_completion_profile[
                    "fallbackTextureManifestSha256"
                ],
                "fallback_only_resources": m3_completion_profile[
                    "fallbackOnlyTextures"
                ],
            },
            "supported_native_structural": {
                "route": NATIVE_STRUCTURAL_ROUTE,
                "part_ids": [
                    f"ae2:{name}" for name, _group in NATIVE_STRUCTURAL_PARTS
                ],
                "part_groups": {
                    f"ae2:{name}": group
                    for name, group in NATIVE_STRUCTURAL_PARTS
                },
                "spin_part_ids": [
                    f"ae2:{name}"
                    for name, _group in NATIVE_STRUCTURAL_PARTS
                    if name in NATIVE_STRUCTURAL_SPIN_PARTS
                ],
                "orientation_state_count": NATIVE_STRUCTURAL_ORIENTATION_STATE_COUNT,
                "plane_part_ids": [
                    f"ae2:{name}" for name in NATIVE_STRUCTURAL_PLANE_PARTS
                ],
                "plane_mask_count_per_type": 16,
                "plane_mask_bits": {
                    "up": 8,
                    "right": 4,
                    "down": 2,
                    "left": 1,
                },
                "p2p_part_ids": [
                    f"ae2:{name}" for name in NATIVE_STRUCTURAL_P2P_PARTS
                ],
                "p2p_frequency_domain": [0, 65_535],
                "dense_capable_part_ids": ["ae2:cable_anchor"],
                "endpoint_catalog": {
                    family: [f"ae2:{name}" for name in names]
                    for family, names in NATIVE_STRUCTURAL_ENDPOINTS.items()
                },
                "ordered_endpoint_policies": [
                    {
                        "id": f"ae2:{name}",
                        "cable_type": family,
                        "block_entity_id": NATIVE_STRUCTURAL_ENDPOINT_POLICIES[
                            name
                        ]["block_entity_id"],
                        "side_rule": NATIVE_STRUCTURAL_ENDPOINT_POLICIES[name][
                            "side_rule"
                        ],
                        "state_properties": NATIVE_STRUCTURAL_ENDPOINT_STATE_SCHEMAS[
                            name
                        ],
                        "blockstate_sha256": (
                            NATIVE_STRUCTURAL_ENDPOINT_BLOCKSTATE_SHA256[name]
                        ),
                    }
                    for name, family in NATIVE_STRUCTURAL_ENDPOINTS_ORDERED
                ],
                "endpoint_state_counts": NATIVE_STRUCTURAL_ENDPOINT_STATE_COUNTS,
                "endpoint_state_cartesian_count": (
                    NATIVE_STRUCTURAL_ENDPOINT_STATE_CARTESIAN_COUNT
                ),
                "endpoint_state_side_cartesian_count": (
                    NATIVE_STRUCTURAL_ENDPOINT_STATE_SIDE_CARTESIAN_COUNT
                ),
                "endpoint_topology": (
                    "direct-six-neighbor-AECableType-min-with-native-collar"
                ),
                "map_color_illumination_policy": (
                    NATIVE_STRUCTURAL_MAP_COLOR_ILLUMINATION_POLICY
                ),
                "unsupported_compatible_endpoint_control": {
                    "block_id": NATIVE_STRUCTURAL_UNKNOWN_EXTENSION_ENDPOINT[
                        "block_id"
                    ],
                    "block_entity_id": NATIVE_STRUCTURAL_UNKNOWN_EXTENSION_ENDPOINT[
                        "block_entity_id"
                    ],
                    "required_state": NATIVE_STRUCTURAL_UNKNOWN_EXTENSION_ENDPOINT[
                        "required_state"
                    ],
                    "policy": "known-compatible-UNKNOWN-whole-bus-fallback",
                },
                "unsupported_compatible_endpoint_count": 67,
                "unsupported_compatible_endpoint_entries_sha256": (
                    NATIVE_STRUCTURAL_UNKNOWN_ENDPOINT_ENTRIES_SHA256
                ),
                "unsupported_compatible_endpoint_artifacts_sha256": (
                    NATIVE_STRUCTURAL_UNKNOWN_ENDPOINT_ARTIFACTS_SHA256
                ),
                "facades": {
                    "mask_count": 64,
                    "maximum_facades": 6,
                    "material": "per-instance-valid-static-BlockState",
                    "slab_thickness_sixteenths": 0.968,
                    "same_face_part_clipping": True,
                    "facade_only_face_short_stilt": True,
                    "opaque_adjacent_edge_corner_masking": True,
                    "source_whitelist_ids": list(
                        NATIVE_STRUCTURAL_FACADE_WHITELIST_IDS
                    ),
                    "source_whitelist_neutral_states": [
                        {
                            "block_id": block_id,
                            "properties": NATIVE_STRUCTURAL_FACADE_WHITELIST_NEUTRAL_STATES[
                                block_id
                            ],
                            "solid_render": NATIVE_STRUCTURAL_FACADE_WHITELIST_SOLID_RENDER[
                                block_id
                            ],
                            "transparent_facade": not NATIVE_STRUCTURAL_FACADE_WHITELIST_SOLID_RENDER[
                                block_id
                            ],
                            "same_state_skip_rendering": (
                                NATIVE_STRUCTURAL_FACADE_WHITELIST_SAME_STATE_SKIP_RENDERING[
                                    block_id
                                ]
                            ),
                        }
                        for block_id in NATIVE_STRUCTURAL_FACADE_WHITELIST_IDS
                    ],
                    "source_whitelist_state_schema_count": 24,
                    "source_whitelist_state_schema_policy": (
                        NATIVE_STRUCTURAL_FACADE_WHITELIST_STATE_POLICY
                    ),
                    "source_whitelist_state_schemas": [
                        {
                            "block_id": block_id,
                            "properties": (
                                NATIVE_STRUCTURAL_FACADE_WHITELIST_STATE_SCHEMAS[
                                    block_id
                                ]
                            ),
                            "blockstate_sha256": (
                                NATIVE_STRUCTURAL_FACADE_WHITELIST_BLOCKSTATE_SHA256[
                                    block_id
                                ]
                            ),
                        }
                        for block_id in NATIVE_STRUCTURAL_FACADE_WHITELIST_IDS
                    ],
                    "source_whitelist_state_contract_sha256": (
                        NATIVE_STRUCTURAL_FACADE_WHITELIST_STATE_CONTRACT_SHA256
                    ),
                    "source_whitelist_state_counts": (
                        NATIVE_STRUCTURAL_FACADE_WHITELIST_STATE_COUNTS
                    ),
                    "source_whitelist_state_cartesian_count": (
                        NATIVE_STRUCTURAL_FACADE_WHITELIST_STATE_CARTESIAN_COUNT
                    ),
                    "source_whitelist_state_classification_policy": (
                        NATIVE_STRUCTURAL_FACADE_STATE_CLASSIFICATION_POLICY
                    ),
                    "source_whitelist_solid_render_true_cartesian_count": (
                        NATIVE_STRUCTURAL_FACADE_SOLID_RENDER_TRUE_CARTESIAN_COUNT
                    ),
                    "source_whitelist_same_state_skip_true_cartesian_count": (
                        NATIVE_STRUCTURAL_FACADE_SAME_STATE_SKIP_TRUE_CARTESIAN_COUNT
                    ),
                    "source_whitelist_same_state_skip_rendering": (
                        NATIVE_STRUCTURAL_FACADE_WHITELIST_SAME_STATE_SKIP_RENDERING
                    ),
                    "ordinary_same_state_skip_rendering_controls": (
                        NATIVE_STRUCTURAL_ORDINARY_FACADE_SAME_STATE_SKIP_RENDERING
                    ),
                    "full_pack_glass_override": (
                        NATIVE_STRUCTURAL_GLASSENTIAL_FULL_PACK_OVERRIDE
                    ),
                    "tint_policy": NATIVE_STRUCTURAL_FACADE_TINT_POLICY,
                    "uv_reinterpolation_policy": (
                        NATIVE_STRUCTURAL_FACADE_UV_REINTERPOLATION_POLICY
                    ),
                    "cardinal_variant_transform_policy": (
                        NATIVE_STRUCTURAL_FACADE_CARDINAL_VARIANT_TRANSFORM_POLICY
                    ),
                    "ambient_occlusion_direction_policy": (
                        NATIVE_STRUCTURAL_FACADE_AO_DIRECTION_POLICY
                    ),
                    "physical_whitelist_ids": [
                        *[
                            entry["block_id"]
                            for entry in NATIVE_STRUCTURAL_NEUTRAL_FACADE_MATERIALS
                        ],
                        *[
                            entry["block_id"]
                            for entry in NATIVE_STRUCTURAL_VANILLA_WHITELIST_CONTROLS
                        ],
                    ],
                    "neutral_native_material_count": 11,
                    "crafting_monitor_normalization": (
                        "preserve-valid-facing-force-unformed-unpowered-spin-zero"
                    ),
                },
                "transient_policy": "static-off-inactive-unlocked",
                "atomic_fallback": "whole-cable-bus-original-resource",
            },
            "native_structural_resources": {
                "operator_required_texture_resources": (
                    native_structural_texture_resources()
                ),
                "operator_required_json_count": NATIVE_STRUCTURAL_TRANSITIVE_JSON_COUNT,
                "operator_required_png_count": NATIVE_STRUCTURAL_PNG_COUNT,
                "operator_required_path_count": NATIVE_STRUCTURAL_RESOURCE_COUNT,
                "gallery_selected_resources": sorted(s1_resource_union),
            },
            "native_structural_profile": {
                "profile_id": native_structural_profile["profileId"],
                "profile_size_bytes": S1_PROFILE_SIZE_BYTES,
                "profile_sha256": S1_PROFILE_SHA256,
                "support_matrix_size_bytes": S1_SUPPORT_MATRIX_SIZE_BYTES,
                "support_matrix_sha256": S1_SUPPORT_MATRIX_SHA256,
                "provenance_size_bytes": S1_PROVENANCE_SIZE_BYTES,
                "provenance_sha256": S1_PROVENANCE_SHA256,
                "artifact": native_structural_profile["artifact"],
                "artifact_size_bytes": native_structural_profile["sizeBytes"],
                "artifact_sha256": native_structural_profile["sha256"],
                "source_tag": native_structural_profile["source"]["tag"],
                "source_commit": native_structural_profile["source"]["commit"],
                "source_sha256": native_structural_profile["source"]["sha256"],
                "direct_neutral_model_root_count": (
                    NATIVE_STRUCTURAL_DIRECT_RESOURCE_COUNT
                ),
                "transitive_json_count": NATIVE_STRUCTURAL_TRANSITIVE_JSON_COUNT,
                "png_count": NATIVE_STRUCTURAL_PNG_COUNT,
                "path_count": NATIVE_STRUCTURAL_RESOURCE_COUNT,
                "total_bytes": 51_306,
                "required_resources_manifest_sha256": (
                    NATIVE_STRUCTURAL_RESOURCE_MANIFEST_SHA256
                ),
                "required_resource_sizes_manifest_sha256": (
                    "a79e93baef3f5d923730686fcc4de05ec30c8b7765aef8b32aaf871f9c4f3869"
                ),
                "endpoint_state_schema_count": 30,
                "endpoint_state_contract_sha256": (
                    "93ad41cf224e0ab07b64ffe91381e9e70b76f14fb4f4f83e17acb101e4dfc3ae"
                ),
                "endpoint_state_cartesian_count": (
                    NATIVE_STRUCTURAL_ENDPOINT_STATE_CARTESIAN_COUNT
                ),
                "endpoint_state_side_cartesian_count": (
                    NATIVE_STRUCTURAL_ENDPOINT_STATE_SIDE_CARTESIAN_COUNT
                ),
                "facade_state_schema_count": 24,
                "facade_state_contract_sha256": (
                    NATIVE_STRUCTURAL_FACADE_WHITELIST_STATE_CONTRACT_SHA256
                ),
                "facade_state_cartesian_count": (
                    NATIVE_STRUCTURAL_FACADE_WHITELIST_STATE_CARTESIAN_COUNT
                ),
                "facade_solid_render_true_cartesian_count": (
                    NATIVE_STRUCTURAL_FACADE_SOLID_RENDER_TRUE_CARTESIAN_COUNT
                ),
                "facade_same_state_skip_true_cartesian_count": (
                    NATIVE_STRUCTURAL_FACADE_SAME_STATE_SKIP_TRUE_CARTESIAN_COUNT
                ),
                "unsupported_compatible_endpoint_count": 67,
                "unsupported_compatible_endpoint_entries_sha256": (
                    NATIVE_STRUCTURAL_UNKNOWN_ENDPOINT_ENTRIES_SHA256
                ),
                "unsupported_compatible_endpoint_artifacts_sha256": (
                    NATIVE_STRUCTURAL_UNKNOWN_ENDPOINT_ARTIFACTS_SHA256
                ),
                "live_model_semantic_sha256": (
                    "aefa42ad8427e8f2ac5b9f1c88807c978617d6ff70768a32223616b970b54251"
                ),
                "live_texture_semantic_sha256": (
                    "1bee2b2917edf3d1eb9ee24505f47a7377665da753f107ec1af9170d783bc833"
                ),
            },
            "selected_resources": selected_resources,
            "core_resources": core_resources,
            "drive_resources": drive_resources,
            "resolved_facade_resources": [STONE_TEXTURE],
            "texture_manifest_sha256": exact_profile["textureManifestSha256"],
            "core_texture_manifest_sha256": exact_profile[
                "coreTextureManifestSha256"
            ],
            "required_resources_sha256": exact_profile[
                "requiredResourcesSha256"
            ],
        },
        "bounds": {
            "fixture": _render_bounds(FIXTURE_BOUNDS),
            "m2_fixture": _render_bounds(M2_FIXTURE_BOUNDS),
            "m3_fixture": _render_bounds(M3_FIXTURE_BOUNDS),
            "m3_support_floor": _render_bounds(M3_FIXTURE_SUPPORT_BOUNDS),
            "m3_air_gap": _render_bounds(M3_FIXTURE_AIR_GAP_BOUNDS),
            "m3b_fixture": _render_bounds(M3B_FIXTURE_BOUNDS),
            "m3b_support_floor": _render_bounds(M3B_FIXTURE_SUPPORT_BOUNDS),
            "m3b_air_gap": _render_bounds(M3B_FIXTURE_AIR_GAP_BOUNDS),
            "m3c_fixture": _render_bounds(M3C_FIXTURE_BOUNDS),
            "m3c_support_floor": _render_bounds(M3C_FIXTURE_SUPPORT_BOUNDS),
            "m3c_air": _render_bounds(M3C_FIXTURE_AIR_BOUNDS),
            "m3d_fixture": _render_bounds(M3D_FIXTURE_BOUNDS),
            "m3d_support_floor": _render_bounds(M3D_FIXTURE_SUPPORT_BOUNDS),
            "m3d_air": _render_bounds(M3D_FIXTURE_AIR_BOUNDS),
            "m3e_fixture": _render_bounds(M3E_FIXTURE_BOUNDS),
            "m3f_fixture": _render_bounds(M3F_FIXTURE_BOUNDS),
            "s1_fixture": _render_bounds(S1_FIXTURE_BOUNDS),
            "observation_deck": _render_bounds(DECK_BOUNDS),
            "controller_sentinel": _render_bounds(SENTINEL_BOUNDS),
        },
        "case_count": CASE_COUNT,
        "anchor_count": ANCHOR_COUNT,
        "m3_floor_policy": {
            "anchor_y": 100,
            "support_y": 98,
            "support_block_id": "minecraft:smooth_stone",
            "air_gap_y": 99,
            "air_gap_block_id": "minecraft:air",
            "reason": "prevent-neighbor-face-culling",
        },
        "m3b_floor_policy": {
            "anchor_y": 100,
            "support_y": 98,
            "support_block_id": "minecraft:smooth_stone",
            "air_gap_y": 99,
            "air_gap_block_id": "minecraft:air",
            "reason": "prevent-neighbor-face-culling",
        },
        "m3c_floor_policy": {
            "primary_center_y": 100,
            "support_y": 97,
            "support_block_id": "minecraft:smooth_stone",
            "air_y": [98, 99],
            "air_block_id": "minecraft:air",
            "reason": "preserve-disjoint-visible-face-and-light-context",
        },
        "m3d_floor_policy": {
            "primary_anchor_y": 100,
            "owned_y": [97, 105],
            "support_y": 97,
            "support_block_id": "minecraft:smooth_stone",
            "ordinary_clear_y": [98, 105],
            "air_block_id": "minecraft:air",
            "exceptions": [
                "real-powered-grid-context",
                "vertical-monitor-storage-pairs",
                "formed-multiblock-members",
            ],
        },
        "m3e_floor_policy": {
            "support": "none-air-isolated",
            "owned_y": [97, 105],
            "air_block_id": "minecraft:air",
            "reason": "preserve-ao-255-and-world-light-contract",
            "formed_check": "two-consecutive-stable-checks",
            "power_overlay_policy": QUANTUM_STATIC_POLICY,
            "particle_policy": QUANTUM_PARTICLE_POLICY,
        },
        "m3f_floor_policy": {
            "owned_y": [96, 106],
            "ordinary_context": "air",
            "paint_support": "solid-backing-side-first",
            "paint_support_block_id": "minecraft:smooth_stone",
            "crank_support": "valid-crankable-opposite-facing-first",
            "crank_support_block_id": "ae2:charger",
            "spatial_pylon_context": "native-connected-components-first",
            "machine_contents_items_fluids_activity": "excluded",
            "reason": "structural-static-projection-only",
        },
        "s1_floor_policy": {
            "anchor_y": 100,
            "owned_y": [96, 110],
            "support": "none-air-isolated",
            "air_block_id": "minecraft:air",
            "topology_helpers": "placed-before-selected-cable-bus-anchors",
            "world_light_policy": "world-derived-own-and-outward-face-maximum",
            "reason": "stable-native-part-facade-and-endpoint-structural-context",
        },
        "expected_custom_summary": {
            "anchor_count": sum(
                is_custom(anchor)
                for case in CASES
                for anchor in case["anchors"]
            )
            + EXPECTED_LEGACY_UPGRADE_ANCHOR_COUNT,
            "triangle_count": sum(
                anchor["expected_triangle_count"]
                for case in CASES
                for anchor in case["anchors"]
                if is_custom(anchor)
            )
            + EXPECTED_LEGACY_UPGRADE_CUSTOM_TRIANGLE_COUNT,
            "selected_resource_count": len(expected_resource_contract),
        },
        "m1_regression_summary": {
            "case_count": EXPECTED_M1_CASE_COUNT,
            "anchor_count": EXPECTED_M1_ANCHOR_COUNT,
            "custom_anchor_count": EXPECTED_M1_CUSTOM_ANCHOR_COUNT,
            "custom_triangle_count": EXPECTED_M1_CUSTOM_TRIANGLE_COUNT,
            "selected_resource_count": EXPECTED_M1_SELECTED_RESOURCE_COUNT,
        },
        "m2_summary": {
            "case_count": EXPECTED_M2_CASE_COUNT,
            "anchor_count": EXPECTED_M2_ANCHOR_COUNT,
            "custom_anchor_count": EXPECTED_M2_CUSTOM_ANCHOR_COUNT,
            "custom_triangle_count": EXPECTED_M2_CUSTOM_TRIANGLE_COUNT,
            "stock_fallback_anchor_count": EXPECTED_M2_FALLBACK_ANCHOR_COUNT,
            "selected_resource_count": len(m2_resource_union),
            "new_selected_resource_count": 9,
        },
        "m3_summary": {
            "case_count": EXPECTED_M3_CASE_COUNT,
            "anchor_count": EXPECTED_M3_ANCHOR_COUNT,
            "custom_anchor_count": EXPECTED_M3_CUSTOM_ANCHOR_COUNT,
            "custom_triangle_count": EXPECTED_M3_CUSTOM_TRIANGLE_COUNT,
            "stock_fallback_anchor_count": EXPECTED_M3_FALLBACK_ANCHOR_COUNT,
            "selected_resource_count": len(m3_resource_union),
            "new_selected_resource_count": EXPECTED_DRIVE_PROFILE_RESOURCE_COUNT,
            "supported_cell_id_count": len(DRIVE_CELL_MODELS),
            "occupied_model_count": DRIVE_OCCUPIED_MODEL_COUNT,
            "occupied_slot_count": m3_occupied_slots,
            "cell_chassis_triangle_count": (
                m3_occupied_slots * DRIVE_CELL_CHASSIS_TRIANGLE_COUNT
            ),
            "offline_led_triangle_count": (
                m3_occupied_slots * DRIVE_LED_TRIANGLE_COUNT
            ),
        },
        "m3b_summary": {
            "case_count": EXPECTED_M3B_CASE_COUNT,
            "anchor_count": EXPECTED_M3B_ANCHOR_COUNT,
            "custom_anchor_count": EXPECTED_M3B_CUSTOM_ANCHOR_COUNT,
            "custom_triangle_count": EXPECTED_M3B_CUSTOM_TRIANGLE_COUNT,
            "stock_fallback_anchor_count": EXPECTED_M3B_FALLBACK_ANCHOR_COUNT,
            "selected_resource_count": len(m3b_resource_union),
            "new_selected_resource_count": (
                EXPECTED_EXTENDED_DRIVE_PROFILE_RESOURCE_COUNT
            ),
            "accepted_cell_id_count": len(EXTENDED_DRIVE_CELL_MODELS),
            "accepted_ae2_cell_id_count": len(DRIVE_CELL_MODELS),
            "accepted_extension_cell_id_count": len(
                EXTENDED_DRIVE_NATIVE_CELL_MODELS
            ),
            "occupied_model_count": EXTENDED_DRIVE_OCCUPIED_MODEL_COUNT,
            "occupied_slot_count": m3b_occupied_slots,
            "base_triangle_formula": "116+16N",
            "cell_chassis_triangle_count": (
                m3b_occupied_slots * DRIVE_CELL_CHASSIS_TRIANGLE_COUNT
            ),
            "offline_led_triangle_count": (
                m3b_occupied_slots * DRIVE_LED_TRIANGLE_COUNT
            ),
        },
        "m3c_summary": {
            "case_count": EXPECTED_M3C_CASE_COUNT,
            "anchor_count": EXPECTED_M3C_ANCHOR_COUNT,
            "custom_anchor_count": EXPECTED_M3C_CUSTOM_ANCHOR_COUNT,
            "custom_triangle_count": EXPECTED_M3C_CUSTOM_TRIANGLE_COUNT,
            "stock_fallback_anchor_count": 0,
            "selected_resource_count": EXPECTED_M3C_SELECTED_RESOURCE_COUNT,
            "new_selected_resource_count": EXPECTED_M3C_SELECTED_RESOURCE_COUNT,
            "block_ids": list(CONNECTED_GLASS_BLOCK_IDS),
            "base_selection_count": 4,
            "frame_resource_count": 15,
            "frame_mask_occurrences": CONNECTED_GLASS_FRAME_OCCURRENCES,
            "no_frame_mask": "0000",
            "no_frame_face_count": 2,
            "triangle_formula": "2*visibleFaces+2*visibleFrameFaces",
        },
        "m3d_summary": {
            "case_count": EXPECTED_M3D_CASE_COUNT,
            "anchor_count": EXPECTED_M3D_ANCHOR_COUNT,
            "custom_anchor_count": EXPECTED_M3D_CUSTOM_ANCHOR_COUNT,
            "custom_triangle_count": EXPECTED_M3D_CUSTOM_TRIANGLE_COUNT,
            "stock_fallback_anchor_count": EXPECTED_M3D_FALLBACK_ANCHOR_COUNT,
            "selected_resource_count": EXPECTED_M3D_SELECTED_RESOURCE_COUNT,
            "new_selected_resource_count": EXPECTED_M3D_SELECTED_RESOURCE_COUNT,
            "block_id_count": len(CRAFTING_BLOCK_KINDS),
            "paint_ordinal_count": len(CRAFTING_PAINT_COLORS),
            "fully_enclosed_zero_geometry_anchor_count": 1,
            "fully_enclosed_zero_geometry_evidence_status": (
                "not-renderer-provenance-distinguishable-in-prbm"
            ),
            "monitor_display_policy": CRAFTING_MONITOR_DISPLAY_POLICY,
        },
        "m3e_summary": {
            "case_count": EXPECTED_M3E_CASE_COUNT,
            "anchor_count": EXPECTED_M3E_ANCHOR_COUNT,
            "custom_anchor_count": EXPECTED_M3E_CUSTOM_ANCHOR_COUNT,
            "custom_triangle_count": EXPECTED_M3E_CUSTOM_TRIANGLE_COUNT,
            "stock_fallback_anchor_count": EXPECTED_M3E_FALLBACK_ANCHOR_COUNT,
            "emitted_resource_count": EXPECTED_M3E_EMITTED_RESOURCE_COUNT,
            "selected_resource_count": EXPECTED_M3E_EMITTED_RESOURCE_COUNT,
            "new_selected_resource_count": EXPECTED_M3E_NEW_SELECTED_RESOURCE_COUNT,
            "block_ids": [QUANTUM_LINK_BLOCK_ID, QUANTUM_RING_BLOCK_ID],
            "plane_orientations": list(QUANTUM_PLANES),
            "bridge_triangle_count": 396,
            "link_triangle_count": 108,
            "corner_triangle_count": 36,
            "edge_triangle_count": 36,
            "material_triangles_per_bridge": {
                QUANTUM_LINK_RESOURCE: 12,
                QUANTUM_GLASS_RESOURCE: 48,
                QUANTUM_COVERED_RESOURCE: 144,
                QUANTUM_RING_RESOURCE: 192,
            },
            "power_overlay_policy": QUANTUM_STATIC_POLICY,
            "particle_policy": QUANTUM_PARTICLE_POLICY,
        },
        "m3f_summary": {
            "case_count": EXPECTED_M3F_CASE_COUNT,
            "anchor_count": EXPECTED_M3F_ANCHOR_COUNT,
            "custom_anchor_count": EXPECTED_M3F_CUSTOM_ANCHOR_COUNT,
            "custom_triangle_count": EXPECTED_M3F_CUSTOM_TRIANGLE_COUNT,
            "stock_fallback_anchor_count": EXPECTED_M3F_FALLBACK_ANCHOR_COUNT,
            "emitted_resource_count": EXPECTED_M3F_EMITTED_RESOURCE_COUNT,
            "selected_resource_count": EXPECTED_M3F_EMITTED_RESOURCE_COUNT,
            "new_selected_resource_count": EXPECTED_M3F_EMITTED_RESOURCE_COUNT,
            "block_ids": list(M3_COMPLETION_BLOCK_ENTITY_IDS),
            "paint_anchor_count": 23,
            "paint_splotch_count": 25,
            "paint_triangle_count": 50,
            "sky_chest_anchor_count": 8,
            "sky_chest_triangle_count": 288,
            "crank_anchor_count": 6,
            "crank_triangle_count": 204,
            "inscriber_anchor_count": 24,
            "inscriber_triangle_count": 1_872,
            "spatial_pylon_custom_anchor_count": 17,
            "spatial_pylon_triangle_count": 408,
            "stock_projection": {
                "rendered_anchor_count": 38,
                "empty_anchor_count": 40,
                "triangle_count": 1_872,
                "resource_count": len(M3_COMPLETION_STOCK_RESOURCES),
                "resources": list(M3_COMPLETION_STOCK_RESOURCES),
            },
            "static_policy": "structural-no-contents-items-fluids-or-activity",
        },
        "s1_summary": {
            "case_count": EXPECTED_S1_CASE_COUNT,
            "anchor_count": EXPECTED_S1_ANCHOR_COUNT,
            "custom_anchor_count": 351,
            "custom_triangle_count": sum(
                anchor["expected_triangle_count"]
                for case in CASES
                for anchor in case["anchors"]
                if anchor["expected_path"] == "custom-s1"
            ),
            "stock_fallback_anchor_count": 9,
            "unit_only_malformed_cases": list(S1_UNIT_ONLY_MALFORMED_CASES),
            "unit_only_reason": S1_UNIT_ONLY_REASON,
            "runtime_oracle_size_bytes": S1_ORACLE_SIZE_BYTES,
            "runtime_oracle_sha256": S1_ORACLE_SHA256,
            "runtime_oracle_anchor_count": S1_ORACLE_ANCHOR_COUNT,
            "runtime_oracle_triangle_count": S1_ORACLE_TRIANGLE_COUNT,
            "runtime_oracle_identity_count": S1_ORACLE_IDENTITY_COUNT,
            "runtime_oracle_material_row_count": S1_ORACLE_MATERIAL_ROW_COUNT,
            "raw_logical_matrix_sha256": S1_RAW_LOGICAL_MATRIX_SHA256,
            "raw_matrix_size_bytes": S1_RAW_MATRIX_SIZE_BYTES,
            "raw_stripped_logical_matrix_size_bytes": (
                S1_RAW_STRIPPED_LOGICAL_MATRIX_SIZE_BYTES
            ),
            "raw_stripped_logical_matrix_sha256": (
                S1_RAW_STRIPPED_LOGICAL_MATRIX_SHA256
            ),
            "rendered_logical_matrix_sha256": s1_logical_matrix_sha256,
            "selected_resource_count": len(s1_resource_union),
            "new_selected_resource_count": len(
                s1_resource_union
                - (set(legacy_selected_resources) | {STONE_TEXTURE})
            ),
            "not_in_legacy_profile_resource_count": len(
                s1_resource_union - set(legacy_selected_resources)
            ),
            "part_identity_count": len(NATIVE_STRUCTURAL_PARTS),
            "spin_part_identity_count": len(NATIVE_STRUCTURAL_SPIN_PARTS),
            "orientation_state_count": NATIVE_STRUCTURAL_ORIENTATION_STATE_COUNT,
            "representative_installed_face_anchor_count": 174,
            "plane_mask_count_per_type": 16,
            "p2p_frequency_values": [0, 0x1234, 0xFFFF],
            "facade_mask_count": 64,
            "endpoint_identity_count": 30,
            "known_compatible_extension_fallback_count": 1,
            "known_compatible_extension_fallback_id": (
                NATIVE_STRUCTURAL_UNKNOWN_EXTENSION_ENDPOINT["block_id"]
            ),
            "facade_source_whitelist_id_count": 24,
            "facade_physical_whitelist_ids": [
                *[
                    entry["block_id"]
                    for entry in NATIVE_STRUCTURAL_NEUTRAL_FACADE_MATERIALS
                ],
                *[
                    entry["block_id"]
                    for entry in NATIVE_STRUCTURAL_VANILLA_WHITELIST_CONTROLS
                ],
            ],
            "facade_java_exhaustive_only_ids": [
                "minecraft:jukebox",
                "minecraft:blast_furnace",
                "minecraft:dropper",
                "minecraft:dispenser",
                "minecraft:crafter",
                "minecraft:barrel",
                "minecraft:bee_nest",
                "minecraft:beehive",
                "minecraft:sculk_catalyst",
            ],
            "predecessor_projection": {
                "rendered_anchor_count": S1_SCHEMA9_DISABLED_RENDERED_ANCHOR_COUNT,
                "empty_anchor_count": S1_SCHEMA9_DISABLED_EMPTY_ANCHOR_COUNT,
                "triangle_count": S1_SCHEMA9_DISABLED_TRIANGLE_COUNT,
                "resource_count": S1_SCHEMA9_DISABLED_RESOURCE_COUNT,
                "resources": sorted(schema9_disabled_projection_resources),
                "rendered_anchors": [
                    {
                        "position": dict(zip(("x", "y", "z"), position)),
                        "expected_path": expected_path,
                        "triangle_count": triangle_count,
                    }
                    for position, (expected_path, triangle_count) in sorted(
                        S1_SCHEMA9_DISABLED_EXPECTATIONS.items()
                    )
                ],
            },
            "physical_stock_projection": {
                "rendered_anchor_count": 0,
                "empty_anchor_count": EXPECTED_S1_ANCHOR_COUNT,
                "triangle_count": 0,
                "resource_count": 0,
                "resources": [],
            },
            "fallback_policy": "whole-cable-bus-original-resource",
        },
        "native_structural_legacy_upgrades": legacy_upgrades,
        "expected_stock_fallback_summary": {
            "anchor_count": sum(
                anchor["expected_path"].startswith("stock-fallback-")
                for case in CASES
                for anchor in case["anchors"]
            )
            - EXPECTED_LEGACY_UPGRADE_ANCHOR_COUNT,
            "triangle_count": 0,
        },
        "cases": rendered_cases,
        "optional_dense_fixture": {
            "auto_build": False,
            "cable_id": DENSE_CABLE_ID,
            "lattice_dimensions": [8, 4, 8],
            "lattice_count": len(DENSE_CABLE_BOUNDS),
            "cell_count": DENSE_CELL_COUNT,
            "expected_triangle_count": DENSE_EXPECTED_TRIANGLES,
            "expected_material_triangles": {
                "ae2:part/cable/core/dense_smart/transparent": 12_288,
                "ae2:part/cable/dense_covered/transparent": 51_200,
            },
            "cable_bounds": [_render_bounds(bounds) for bounds in DENSE_CABLE_BOUNDS],
            "owned_bounds": [_render_bounds(bounds) for bounds in DENSE_OWNED_BOUNDS],
            "exclusive_forceload_chunks": [
                {"x": chunk_x, "z": chunk_z}
                for chunk_x, chunk_z in dense_exclusive_chunks()
            ],
            "requires_main_gallery_loaded": True,
            "build_function": "ae2_m3:dense/build",
            "verify_function": "ae2_m3:dense/verify",
            "clear_function": "ae2_m3:dense/clear",
            "release_function": "ae2_m3:dense/release",
        },
        "unit_only_malformed_cases": [
            "center-not-compound",
            "missing-id",
            "id-not-string",
            "invalid-resource-location",
            "terminal-missing-spin",
            "face-part-not-compound",
            "facade-not-compound",
            "duplicate-retained-field",
            "oversized-retained-field",
            "drive-inventory-not-compound",
            "drive-slot-not-compound",
            "drive-slot-missing-id",
            "drive-slot-id-not-string",
            "drive-slot-zero-count",
            "drive-slot-nonnumeric-count",
            "drive-slot-component-budget-exceeded",
        ],
        "unit_only_reason": (
            "Container/type corruption and bounded-reader rejection remain unit-only; "
            "the M3c gallery retains the M3b durable well-formed supported and fallback "
            "NBT states, including count-two rejection and valid current-schema "
            "storage-cell components."
        ),
    }
    schema9 = _schema9_view(manifest)
    schema9_payload = json_bytes(schema9)
    if hashlib.sha256(schema9_payload).hexdigest() != SCHEMA9_CANONICAL_SHA256:
        raise ValueError("schema 10 does not embed the byte-frozen accepted schema-9 view")
    schema8_payload = json_bytes(_schema8_view(schema9))
    if hashlib.sha256(schema8_payload).hexdigest() != SCHEMA8_CANONICAL_SHA256:
        raise ValueError("schema 9 does not embed the byte-frozen accepted schema-8 view")
    schema7_payload = json_bytes(_schema7_view(_schema8_view(schema9)))
    if hashlib.sha256(schema7_payload).hexdigest() != SCHEMA7_CANONICAL_SHA256:
        raise ValueError("schema 9 does not embed the byte-frozen accepted schema-7 view")
    return manifest


def _m45_artifact_from_profile(profile: dict[str, Any]) -> dict[str, Any]:
    return {
        "artifact": profile["artifact"],
        "version": profile["version"],
        "size_bytes": profile["sizeBytes"],
        "sha256": profile["sha256"],
        **(
            {"source": profile["source"]}
            if isinstance(profile.get("source"), dict)
            else {}
        ),
    }


def _m45_legacy_upgrades(
    schema10_cases: Iterable[dict[str, Any]],
) -> dict[str, Any]:
    cases_by_id = {case["case_id"]: case for case in schema10_cases}
    oracle = load_m45_schema10_legacy_oracle()
    rows: list[dict[str, Any]] = []
    seen_positions: set[tuple[int, int, int]] = set()
    for spec in M45_LEGACY_UPGRADE_SPECS:
        case_id = spec["case_id"]
        position = spec["position"]
        source_case = cases_by_id.get(case_id)
        source_anchors = source_case.get("anchors") if source_case else None
        matches = [
            anchor
            for anchor in source_anchors or ()
            if tuple(anchor["position"][axis] for axis in ("x", "y", "z"))
            == position
        ]
        if (
            not isinstance(source_case, dict)
            or not isinstance(source_anchors, list)
            or len(matches) != 1
            or position in seen_positions
        ):
            raise ValueError("M4/M5 legacy-upgrade source selector changed")
        seen_positions.add(position)
        anchor = matches[0]
        if (
            anchor.get("expected_path") != spec["expected_path"]
            or anchor.get("expected_triangle_count") != 0
            or anchor.get("fallback_reason") != spec["fallback_reason"]
        ):
            raise ValueError(
                f"M4/M5 legacy-upgrade predecessor changed for {case_id}"
            )
        if spec["source_kind"] in {
            "ae2-drive-megacells-cell",
            "extended-drive-megacells-cell",
        }:
            expected_block_id = (
                DRIVE_BLOCK_ID
                if spec["source_kind"] == "ae2-drive-megacells-cell"
                else EXTENDED_DRIVE_BLOCK_ID
            )
            slots = anchor.get("inventory", {}).get("slots", [])
            if (
                anchor.get("block_id") != expected_block_id
                or not isinstance(slots, list)
                or not slots
                or slots[0]
                != {
                    "field": "item0",
                    "item_stack": {
                        "count": 1,
                        "id": "megacells:item_storage_cell_1m",
                    },
                    "slot": 0,
                }
                or any(not slot.get("empty") for slot in slots[1:])
            ):
                raise ValueError(
                    f"M4/M5 legacy Drive source NBT changed for {case_id}"
                )
        elif (
            spec["source_kind"]
            == "native-crafting-expanded-mega-peer-connection"
        ):
            if (
                anchor.get("block_id") != "ae2:1k_crafting_storage"
                or anchor.get("compatible_neighbor_block_ids")
                != [
                    "megacells:mega_crafting_unit",
                    "expandedae:exp_crafting_unit",
                ]
                or anchor.get("block_state")
                != {"formed": True, "powered": False}
            ):
                raise ValueError(
                    "M4/M5 legacy crafting peer source contract changed"
                )
        else:
            raise ValueError("unknown M4/M5 legacy-upgrade source kind")

        observation = spec["observation"]
        material_triangles = observation["material_triangles"]
        oracle_entry = oracle[position]
        if (
            observation["triangle_count"] != sum(material_triangles.values())
            or not material_triangles
            or any(count <= 0 for count in material_triangles.values())
            or observation["triangle_count"]
            != oracle_entry["triangle_count"]
            or material_triangles != oracle_entry["material_triangles"]
        ):
            raise ValueError("M4/M5 legacy live observation is malformed")
        predecessor_projection = {
            "expected_path": spec["expected_path"],
            "review_projection": "empty",
            "reason": spec["fallback_reason"],
            "allowed_resources": [],
        }
        rows.append(
            {
                "case_id": case_id,
                "position": dict(zip(("x", "y", "z"), position)),
                "source_kind": spec["source_kind"],
                "required_m45_routes": list(spec["required_m45_routes"]),
                "required_legacy_routes": list(spec["required_legacy_routes"]),
                "enabled": {
                    "expected_path": "custom-m45-legacy-upgrade",
                    "review_projection": "nonempty",
                    "reason": "exact-enabled-live-map-route-upgrade",
                    "allowed_resources": sorted(material_triangles),
                    "expected_triangle_count": oracle_entry[
                        "triangle_count"
                    ],
                    "expected_material_triangles": oracle_entry[
                        "material_triangles"
                    ],
                    "expected_geometry_signature": oracle_entry[
                        "geometry_signature"
                    ],
                    "expected_nonlighting_attribute_signature": oracle_entry[
                        "nonlighting_attribute_signature"
                    ],
                },
                "predecessor_projection": predecessor_projection,
                "physical_stock_projection": predecessor_projection,
                "live_observation": observation,
            }
        )

    if seen_positions != {
        spec["position"] for spec in M45_LEGACY_UPGRADE_SPECS
    }:
        raise ValueError("M4/M5 legacy-upgrade selector closure changed")
    return {
        "schema_version": 1,
        "coverage_id": M45_LEGACY_UPGRADE_COVERAGE,
        "profile_id": "m45-cumulative-review",
        "source_schema10": {
            "cases_size_bytes": 4_207_895,
            "cases_sha256": (
                "389a9b2b82dd16e3f4af82f9836e593770e404995a153218937908528c17dcee"
            ),
            "signature_schema_version": 10,
        },
        "capture": M45_LEGACY_UPGRADE_CAPTURE,
        "oracle": {
            "path": M45_SCHEMA10_LEGACY_ORACLE_PATH.name,
            "size_bytes": M45_SCHEMA10_LEGACY_ORACLE_SIZE_BYTES,
            "sha256": M45_SCHEMA10_LEGACY_ORACLE_SHA256,
            "schema_version": 2,
            "signature_schema_version": 11,
            "anchor_count": M45_SCHEMA10_LEGACY_ORACLE_ANCHOR_COUNT,
            "triangle_count": M45_SCHEMA10_LEGACY_ORACLE_TRIANGLE_COUNT,
            "identity_count": M45_SCHEMA10_LEGACY_ORACLE_IDENTITY_COUNT,
            "material_row_count": (
                M45_SCHEMA10_LEGACY_ORACLE_MATERIAL_ROW_COUNT
            ),
        },
        "rows": rows,
        "summary": {
            "anchor_count": len(rows),
            "custom_anchor_count": M45_SCHEMA10_LEGACY_ORACLE_ANCHOR_COUNT,
            "custom_triangle_count": M45_SCHEMA10_LEGACY_ORACLE_TRIANGLE_COUNT,
            "selected_resource_count": M45_SCHEMA10_LEGACY_ORACLE_IDENTITY_COUNT,
            "material_row_count": M45_SCHEMA10_LEGACY_ORACLE_MATERIAL_ROW_COUNT,
            "m45_route_dependency_anchor_counts": {
                route: sum(
                    route in spec["required_m45_routes"]
                    for spec in M45_LEGACY_UPGRADE_SPECS
                )
                for route in M45_ROUTES
                if any(
                    route in spec["required_m45_routes"]
                    for spec in M45_LEGACY_UPGRADE_SPECS
                )
            },
            "legacy_route_dependency_anchor_counts": {
                route: sum(
                    route in spec["required_legacy_routes"]
                    for spec in M45_LEGACY_UPGRADE_SPECS
                )
                for route in ("extension", "crafting")
            },
            "predecessor_projection": {
                "empty_anchor_count": len(rows),
                "triangle_count": 0,
                "resource_count": 0,
                "resources": [],
            },
            "physical_stock_projection": {
                "empty_anchor_count": len(rows),
                "triangle_count": 0,
                "resource_count": 0,
                "resources": [],
            },
        },
    }


def _m45_route_profiles(
    base_selected_resources: Iterable[str],
) -> tuple[dict[str, Any], ...]:
    base_resources = set(base_selected_resources)
    extended_matrix_resources, extended_plane_resources = (
        _m45_extended_texture_partitions()
    )
    plane_observation_materials = M45_EXTENDED_PLANE_LIVE_OBSERVATION[
        "material_triangles"
    ]
    if (
        sum(plane_observation_materials.values()) != 3_244
        or set(plane_observation_materials)
        != {
            "extendedae:part/active_formation_plane",
            "extendedae:part/smart_annihilation_plane",
        }
        | set(M45_EXTENDED_PLANE_DEPENDENCY_RESOURCES)
        | set(M45_EXTENDED_PLANE_HOST_RESOURCES)
    ):
        raise ValueError("M4/M5 Extended plane live observation changed")
    athena_source_resources = _m45_texture_resources(
        "advancedae/1.6.12/athena-required-resources.tsv"
    )
    athena_frame_zero_resources = tuple(
        sorted(
            "bluemap_ae2:m45/athena-frame-zero/" + resource.split(":", 1)[1]
            for resource in athena_source_resources
        )
    )
    route_data = (
        (
            "appflux",
            "M4",
            _m45_artifact_from_profile(
                _m45_profile_json("appflux/1.21-2.1.5-neoforge/profile.json")
            ),
            _m45_texture_resources(
                "appflux/1.21-2.1.5-neoforge/required-resources.tsv"
            ),
        ),
        (
            "merequester",
            "M4",
            _m45_artifact_from_profile(
                _m45_profile_json("merequester/1.21.1-1.4.3/profile.json")
            ),
            _m45_texture_resources(
                "merequester/1.21.1-1.4.3/required-resources.tsv"
            ),
        ),
        (
            "expandedae",
            "M4",
            _m45_artifact_from_profile(
                _m45_profile_json("expandedae/2.1.1/profile.json")
            ),
            _m45_texture_resources("expandedae/2.1.1/required-resources.tsv"),
        ),
        (
            "megacells",
            "M5",
            _m45_artifact_from_profile(
                _m45_profile_json("megacells/4.11.0/profile.json")
            ),
            _m45_texture_resources(
                "megacells/4.11.0/required-crafting-resources.tsv",
                "megacells/4.11.0/required-cell-dock-resources.tsv",
                "megacells/4.11.0/required-generic-part-resources.tsv",
                "megacells/4.11.0/required-dependent-ae2-resources.tsv",
            ),
        ),
        (
            "advanced-ae-quantum",
            "M5",
            M45_ADVANCED_ARTIFACT,
            _m45_texture_resources(
                "advancedae/1.6.12/quantum-required-resources.tsv"
            ),
        ),
        (
            "advanced-ae-athena",
            "M5",
            {
                "primary": M45_ADVANCED_ARTIFACT,
                "ctm_runtime": M45_ATHENA_ARTIFACT,
            },
            athena_frame_zero_resources,
            athena_source_resources,
            (),
        ),
        (
            "extendedae-matrix",
            "M5",
            M45_EXTENDED_ARTIFACT,
            extended_matrix_resources,
            extended_matrix_resources,
            (),
        ),
        (
            "extendedae-planes",
            "M5",
            M45_EXTENDED_ARTIFACT,
            extended_plane_resources,
            extended_plane_resources,
            M45_EXTENDED_PLANE_DEPENDENCY_RESOURCES,
        ),
    )
    profiles: list[dict[str, Any]] = []
    normalized_route_data = tuple(
        (*row, row[3], ()) if len(row) == 4 else row
        for row in route_data
    )
    for (
        route,
        milestone,
        artifact,
        route_resources,
        source_resources,
        dependency_resources,
    ) in normalized_route_data:
        case = next(case for case in M45_CASES if case["route"] == route)
        route_resource_set = set(route_resources)
        source_resource_set = set(source_resources)
        dependency_resource_set = set(dependency_resources)
        host_resource_set = set(
            M45_EXTENDED_PLANE_HOST_RESOURCES
            if route == "extendedae-planes"
            else ()
        )
        if (
            not host_resource_set <= base_resources
            or host_resource_set
            & (route_resource_set | dependency_resource_set)
        ):
            raise ValueError("M4/M5 route host-resource partition changed")
        disabled_projections = [
            anchor["route_disabled_projections"][route]
            for candidate in M45_CASES
            for anchor in candidate["anchors"]
            if route in anchor["route_disabled_projections"]
        ]
        profiles.append(
            {
                "route": route,
                "milestone": milestone,
                "artifact": artifact,
                "case_count": 1,
                "anchor_count": len(case["anchors"]),
                "route_resources": sorted(route_resource_set),
                "source_resources": sorted(source_resource_set),
                "dependency_resources": sorted(dependency_resource_set),
                "host_resources": sorted(host_resource_set),
                "enabled_live_observation": (
                    M45_EXTENDED_PLANE_LIVE_OBSERVATION
                    if route == "extendedae-planes"
                    else None
                ),
                "material_allowlist": sorted(
                    (
                        route_resource_set
                        | dependency_resource_set
                        | host_resource_set
                        if route in {"extendedae-matrix", "extendedae-planes"}
                        else base_resources
                        | route_resource_set
                        | host_resource_set
                    )
                ),
                "stock_material_allowlist": sorted(
                    base_resources
                    | route_resource_set
                    | source_resource_set
                    | dependency_resource_set
                    | host_resource_set
                ),
                "enabled_projection": (
                    "per-anchor-nonempty-or-explicit-atomic-empty-review"
                ),
                "route_disabled_projection": "per-anchor-declared-review-projection",
                "route_disabled_affected_anchor_count": len(disabled_projections),
                "route_disabled_nonempty_anchor_count": sum(
                    projection["review_projection"] == "nonempty"
                    for projection in disabled_projections
                ),
                "route_disabled_empty_anchor_count": sum(
                    projection["review_projection"] == "empty"
                    for projection in disabled_projections
                ),
                "legacy_upgrade_dependency_anchor_count": sum(
                    route in spec["required_m45_routes"]
                    for spec in M45_LEGACY_UPGRADE_SPECS
                ),
                "native_structural_dependency": (
                    "route-blocked-and-face-lane-unreachable"
                    if route == "extendedae-planes"
                    else "face-lanes-unreachable-independent-whole-block-lanes-remain-active"
                    if any(
                        anchor["block_id"] == "ae2:cable_bus"
                        for anchor in case["anchors"]
                    )
                    else "independent-route-remains-active"
                ),
                "failure_policy": "disable-only-this-route-and-preserve-other-routes",
            }
        )
    if tuple(profile["route"] for profile in profiles) != M45_ROUTES:
        raise ValueError("M4/M5 profile route closure changed")
    return tuple(profiles)


def cases_manifest() -> dict[str, object]:
    manifest = _schema10_manifest()
    schema10_payload = json_bytes(manifest)
    if hashlib.sha256(schema10_payload).hexdigest() != (
        "389a9b2b82dd16e3f4af82f9836e593770e404995a153218937908528c17dcee"
    ):
        raise ValueError("accepted schema-10 projection changed before M4/M5 append")
    profile = manifest["profile"]
    if not isinstance(profile, dict):
        raise ValueError("schema-10 profile is unavailable")
    base_selected = list(profile["selected_resources"])
    base_materials = [
        *base_selected,
        *profile.get("resolved_facade_resources", ()),
    ]
    route_profiles = _m45_route_profiles(base_materials)
    new_resources = sorted(
        {
            resource
            for route_profile in route_profiles
            for resource in route_profile["route_resources"]
        }
        - set(base_selected)
    )
    profile["coverage_milestone"] = "M5-cumulative-review"
    profile["selected_resources"] = base_selected + new_resources
    profile["m45_routes"] = list(route_profiles)
    manifest["schema_version"] = 11
    manifest["signature_schema_version"] = 11
    manifest["case_count"] = TOTAL_CASE_COUNT
    manifest["anchor_count"] = TOTAL_ANCHOR_COUNT
    manifest["cases"] = [
        *manifest["cases"],
        *_render_m45_cases(M45_CASES, route_profiles),
    ]
    manifest["m45_legacy_upgrades"] = _m45_legacy_upgrades(
        manifest["cases"][:CASE_COUNT]
    )
    manifest["bounds"]["m45_fixture"] = _render_bounds(M45_FIXTURE_BOUNDS)
    manifest["m45_floor_policy"] = {
        "anchor_y": 100,
        "owned_y": [96, 110],
        "support": "air-isolated-except-exact-powered-io-network-helpers",
        "air_block_id": "minecraft:air",
        "formed_fixture_policy": (
            "live-proven-physical-layouts-fail-closed-on-rewrite"
        ),
        "reason": (
            "bounded-physical-static-route-review-with-explicit-powered-io-helpers"
        ),
    }
    manifest["m45_review_summary"] = {
        "case_count": M45_CASE_COUNT,
        "anchor_count": M45_ANCHOR_COUNT,
        "custom_review_anchor_count": sum(
            anchor["expected_path"] == "custom-m45"
            for case in M45_CASES
            for anchor in case["anchors"]
        ),
        "atomic_fallback_anchor_count": sum(
            anchor["expected_path"] == "stock-fallback-m45"
            for case in M45_CASES
            for anchor in case["anchors"]
        ),
        "source_derived_synthetic_anchor_count": sum(
            anchor["source_derived_synthetic_fixture"]
            for case in M45_CASES
            for anchor in case["anchors"]
        ),
        "route_count": len(M45_ROUTES),
        "route_ids": list(M45_ROUTES),
        "base_schema10_case_count": CASE_COUNT,
        "base_schema10_anchor_count": ANCHOR_COUNT,
        "base_schema10_selected_resource_count": len(base_selected),
        "new_selected_resource_count": len(new_resources),
        "runtime_oracle_size_bytes": M45_RUNTIME_ORACLE_SIZE_BYTES,
        "runtime_oracle_sha256": M45_RUNTIME_ORACLE_SHA256,
        "runtime_oracle_anchor_count": M45_RUNTIME_ORACLE_ANCHOR_COUNT,
        "runtime_oracle_triangle_count": M45_RUNTIME_ORACLE_TRIANGLE_COUNT,
        "runtime_oracle_identity_count": M45_RUNTIME_ORACLE_IDENTITY_COUNT,
        "runtime_oracle_material_row_count": (
            M45_RUNTIME_ORACLE_MATERIAL_ROW_COUNT
        ),
        "legacy_upgrade_anchor_count": len(M45_LEGACY_UPGRADE_SPECS),
        "legacy_upgrade_runtime_triangle_count": (
            M45_SCHEMA10_LEGACY_ORACLE_TRIANGLE_COUNT
        ),
        "review_oracle_policy": (
            "exact-runtime-map-geometry-material-nonlighting-v11"
        ),
        "route_disabled_projection": "per-anchor-declared-review-projection",
        "disabled_projection_evidence": M45_DISABLED_PROJECTION_EVIDENCE,
        "physical_stock_projection": {
            "nonempty_anchor_count": sum(
                anchor["physical_stock_projection"]["review_projection"]
                == "nonempty"
                for case in M45_CASES
                for anchor in case["anchors"]
            ),
            "empty_anchor_count": sum(
                anchor["physical_stock_projection"]["review_projection"]
                == "empty"
                for case in M45_CASES
                for anchor in case["anchors"]
            ),
        },
        "native_structural_disabled_projection": {
            "affected_anchor_count": sum(
                anchor["native_structural_disabled_projection"]["expected_path"]
                != anchor["expected_path"]
                for case in M45_CASES
                for anchor in case["anchors"]
            ),
            "blocked_route": "extendedae-planes",
            "other_face_lanes": "unreachable",
            "independent_whole_block_lanes": "enabled",
        },
        "crafting_disabled_projection": {
            "affected_anchor_count": sum(
                "crafting_disabled_projection" in anchor
                for case in M45_CASES
                for anchor in case["anchors"]
            ),
            "policy": "direct-native-owner-observers-atomically-stock-fallback",
        },
    }
    manifest["m45_unit_only_mutations"] = {
        "advanced_quantum": [
            "missing-or-wrong-block-entity-id",
            "missing-extra-or-out-of-domain-persisted-state",
            "missing-unknown-or-malformed-3x3x3-appearance-observation",
        ],
        "extended_matrix": [
            "missing-or-wrong-block-entity-id",
            "missing-extra-or-out-of-domain-persisted-state",
            "missing-or-malformed-connected-glass-neighborhood",
        ],
        "crafting_owner_isolation": [
            "active-exact-expanded-and-megacells-peer-owners-connect",
            "inactive-native-core-owner-atomically-falls-back-still-active-center",
            "inactive-extension-peer-owner-atomically-falls-back-still-active-center",
            "malformed-exact-family-neighbor-atomically-falls-back-still-active-center",
        ],
        "topology_contract": (
            "Java unit coverage remains authoritative beyond the bounded physical review layouts"
        ),
    }
    return manifest


def cases_tsv() -> bytes:
    legacy_upgrades = native_structural_legacy_upgrades(_render_cases(CASES))
    native_effective_by_selector = {
        (
            row["case_id"],
            tuple(row["position"][axis] for axis in ("x", "y", "z")),
        ): row["enabled"]
        for row in legacy_upgrades["rows"]
    }
    if tuple(native_effective_by_selector) != LEGACY_UPGRADE_SELECTORS:
        raise ValueError("legacy upgrade TSV selector closure changed")
    m45_legacy_upgrades = _m45_legacy_upgrades(_render_cases(CASES))
    m45_effective_by_selector = {
        (
            row["case_id"],
            tuple(row["position"][axis] for axis in ("x", "y", "z")),
        ): {
            **row["enabled"],
            "expected_triangle_count": row["live_observation"][
                "triangle_count"
            ],
            "expected_material_triangles": row["live_observation"][
                "material_triangles"
            ],
        }
        for row in m45_legacy_upgrades["rows"]
    }
    expected_m45_selectors = tuple(
        (spec["case_id"], spec["position"])
        for spec in M45_LEGACY_UPGRADE_SPECS
    )
    if (
        tuple(m45_effective_by_selector) != expected_m45_selectors
        or set(native_effective_by_selector) & set(m45_effective_by_selector)
    ):
        raise ValueError("M4/M5 legacy upgrade TSV selector closure changed")
    effective_by_selector = {
        **native_effective_by_selector,
        **m45_effective_by_selector,
    }
    lines = [
        "case_id\tmilestone\tcoverage_id\troute\tlabel\tcategory\texpected_path\tblock_id\tcable_id\tanchor\t"
        "face_parts\tfacades\tfallback_reason\texpected_connections\t"
        "expected_triangles\tblock_state\tdrive_inventory\tdrive_models\t"
        "drive_led\tsupport_floor\tair_gap\tglass_base_selection\tglass_faces\t"
        "painted_color_ordinal\tcrafting_faces\tstatic_policy\tpaint_dots_sha256\t"
        "pylon_axis_position\texpected_stock_triangles\tnative_part_group\t"
        "installed_face\tplane_mask\tp2p_frequency_unsigned\tfacade_mask\t"
        "native_endpoints\texpected_geometry_signature\t"
        "expected_nonlighting_attribute_signature\t"
        "effective_overlay_path\teffective_overlay_connections\t"
        "effective_overlay_triangles\teffective_overlay_material_triangles\t"
        "effective_overlay_geometry_signature\t"
        "effective_overlay_nonlighting_attribute_signature"
    ]
    for case in CASES:
        for anchor in case["anchors"]:
            effective_overlay = effective_by_selector.get(
                (case["case_id"], anchor["position"])
            )
            face_parts = ",".join(
                f"{direction}:{part['id']}"
                + (f"@{part['spin']}" if "spin" in part else "")
                + (f"#freq={part['freq']}" if "freq" in part else "")
                for direction, part in anchor.get("face_parts", {}).items()
            ) or "none"
            facade_rows = []
            for direction, state in anchor.get("facades", {}).items():
                properties = state.get("Properties")
                if isinstance(properties, dict):
                    suffix = "[" + ",".join(
                        f"{key}={value}"
                        for key, value in properties.items()
                    ) + "]"
                elif properties is not None:
                    suffix = "[malformed=" + json.dumps(properties) + "]"
                else:
                    suffix = ""
                facade_rows.append(f"{direction}:{state['Name']}{suffix}")
            facades = ",".join(facade_rows) or "none"
            connections = ",".join(
                f"{connection['direction']}:"
                f"{connection.get('effective_family', connection.get('kind'))}"
                for connection in anchor.get("expected_connections", ())
            ) or "none"
            block_state = (
                json.dumps(anchor["block_state"], sort_keys=True, separators=(",", ":"))
                if "block_state" in anchor
                else "-"
            )
            drive_inventory = (
                json.dumps(
                    {
                        f"item{slot}": item or {}
                        for slot, item in enumerate(anchor["drive_inventory"])
                    },
                    sort_keys=True,
                    separators=(",", ":"),
                )
                if "drive_inventory" in anchor
                else "-"
            )
            drive_models = (
                json.dumps(
                    anchor["expected_drive_models"],
                    sort_keys=True,
                    separators=(",", ":"),
                )
                if "expected_drive_models" in anchor
                else "-"
            )
            drive_led = (
                json.dumps(
                    anchor["expected_drive_led"],
                    sort_keys=True,
                    separators=(",", ":"),
                )
                if "expected_drive_led" in anchor
                else "-"
            )
            if anchor["block_id"] in (DRIVE_BLOCK_ID, EXTENDED_DRIVE_BLOCK_ID):
                x, _y, z = anchor["position"]
                support_floor = f"{x} 98 {z}:minecraft:smooth_stone"
                air_gap = f"{x} 99 {z}:minecraft:air"
            else:
                support_floor = "-"
                air_gap = "-"
            if anchor["block_id"] in CONNECTED_GLASS_BLOCK_IDS:
                support_floor = f"{anchor['position'][0]} 97 {anchor['position'][2]}:minecraft:smooth_stone"
                air_gap = f"{anchor['position'][0]} 98 {anchor['position'][2]}:minecraft:air"
                glass_base_selection = json.dumps(
                    anchor["expected_glass_base_selection"],
                    sort_keys=True,
                    separators=(",", ":"),
                )
                glass_faces = json.dumps(
                    anchor["expected_glass_faces"],
                    sort_keys=True,
                    separators=(",", ":"),
                )
            else:
                glass_base_selection = "-"
                glass_faces = "-"
            if anchor["block_id"] in CRAFTING_BLOCK_KINDS:
                support_floor = f"{anchor['position'][0]} 97 {anchor['position'][2]}:minecraft:smooth_stone"
                air_gap = "owned-volume-context-dependent"
                painted_color = str(anchor.get("painted_color_ordinal", "-"))
                crafting_faces = (
                    json.dumps(
                        anchor["expected_crafting_faces"],
                        sort_keys=True,
                        separators=(",", ":"),
                    )
                    if "expected_crafting_faces" in anchor
                    else "-"
                )
            else:
                painted_color = "-"
                crafting_faces = "-"
            paint_dots_sha256 = (
                hashlib.sha256(
                    bytes(value & 0xFF for value in anchor["paint_dots"])
                ).hexdigest()
                if "paint_dots" in anchor
                else "-"
            )
            lines.append(
                "\t".join(
                    (
                        case["case_id"],
                        case["milestone"],
                        case.get("coverage_id", "-"),
                        case["route"],
                        case["label"],
                        case["category"],
                        anchor["expected_path"],
                        anchor["block_id"],
                        anchor["cable_id"] or "-",
                        " ".join(map(str, anchor["position"])),
                        face_parts,
                        facades,
                        anchor.get("fallback_reason", "-"),
                        connections,
                        str(anchor.get("expected_triangle_count", "-")),
                        block_state,
                        drive_inventory,
                        drive_models,
                        drive_led,
                        support_floor,
                        air_gap,
                        glass_base_selection,
                        glass_faces,
                        painted_color,
                        crafting_faces,
                        anchor.get("static_policy", "-"),
                        paint_dots_sha256,
                        anchor.get("pylon_axis_position") or "-",
                        str(anchor.get("expected_stock_triangle_count", "-")),
                        anchor.get("native_part_group", "-"),
                        anchor.get("installed_face", "-"),
                        str(anchor.get("plane_mask", "-")),
                        str(anchor.get("p2p_frequency_unsigned", "-")),
                        str(anchor.get("facade_mask", "-")),
                        (
                            json.dumps(
                                anchor["native_endpoints"],
                                sort_keys=True,
                                separators=(",", ":"),
                            )
                            if "native_endpoints" in anchor
                            else "-"
                        ),
                        anchor.get("expected_geometry_signature", "-"),
                        anchor.get(
                            "expected_nonlighting_attribute_signature", "-"
                        ),
                        (
                            effective_overlay["expected_path"]
                            if effective_overlay is not None
                            else "-"
                        ),
                        (
                            json.dumps(
                                effective_overlay.get("expected_connections", []),
                                sort_keys=True,
                                separators=(",", ":"),
                            )
                            if effective_overlay is not None
                            else "-"
                        ),
                        (
                            str(effective_overlay["expected_triangle_count"])
                            if effective_overlay is not None
                            else "-"
                        ),
                        (
                            json.dumps(
                                effective_overlay["expected_material_triangles"],
                                sort_keys=True,
                                separators=(",", ":"),
                            )
                            if effective_overlay is not None
                            else "-"
                        ),
                        (
                            effective_overlay.get("expected_geometry_signature", "-")
                            if effective_overlay is not None
                            else "-"
                        ),
                        (
                            effective_overlay.get(
                                "expected_nonlighting_attribute_signature", "-"
                            )
                            if effective_overlay is not None
                            else "-"
                        ),
                    )
                )
            )
    headers = lines[0].split("\t")
    for case in M45_CASES:
        for anchor in case["anchors"]:
            face_parts = ",".join(
                f"{direction}:{part['id']}"
                + (f"@{part['spin']}" if "spin" in part else "")
                + (
                    f"#cell={part['cell']['id']}"
                    if isinstance(part.get("cell"), dict)
                    else "#empty-cell"
                    if "cell" in part
                    else ""
                )
                for direction, part in anchor.get("face_parts", {}).items()
            ) or "none"
            row = {header: "-" for header in headers}
            row.update(
                {
                    "case_id": case["case_id"],
                    "milestone": case["milestone"],
                    "coverage_id": "m45-cumulative-review",
                    "route": case["route"],
                    "label": case["label"],
                    "category": case["category"],
                    "expected_path": anchor["expected_path"],
                    "block_id": anchor["block_id"],
                    "cable_id": anchor.get("cable_id") or "-",
                    "anchor": " ".join(map(str, anchor["position"])),
                    "face_parts": face_parts,
                    "facades": "none",
                    "fallback_reason": anchor.get("fallback_reason", "-"),
                    "expected_triangles": str(
                        anchor.get("expected_triangle_count", "-")
                    ),
                    "expected_geometry_signature": anchor.get(
                        "expected_geometry_signature", "-"
                    ),
                    "expected_nonlighting_attribute_signature": anchor.get(
                        "expected_nonlighting_attribute_signature", "-"
                    ),
                    "block_state": (
                        json.dumps(
                            anchor["block_state"],
                            sort_keys=True,
                            separators=(",", ":"),
                        )
                        if "block_state" in anchor
                        else "-"
                    ),
                    "drive_inventory": (
                        json.dumps(
                            {
                                f"item{slot}": item or {}
                                for slot, item in enumerate(
                                    anchor["drive_inventory"]
                                )
                            },
                            sort_keys=True,
                            separators=(",", ":"),
                        )
                        if "drive_inventory" in anchor
                        else "-"
                    ),
                    "installed_face": anchor.get("installed_face", "-"),
                    "plane_mask": str(anchor.get("plane_mask", "-")),
                }
            )
            lines.append("\t".join(row[header] for header in headers))
    return ("\n".join(lines) + "\n").encode("utf-8")


def block_position(position: tuple[int, int, int]) -> str:
    return " ".join(map(str, position))


def fill_bounds(bounds: tuple[tuple[int, ...], tuple[int, ...]]) -> str:
    return f"{block_position(bounds[0])} {block_position(bounds[1])}"


def horizontal_bounds(bounds: tuple[tuple[int, ...], tuple[int, ...]]) -> str:
    return f"{bounds[0][0]} {bounds[0][2]} {bounds[1][0]} {bounds[1][2]}"


def snbt_string(value: str) -> str:
    return json.dumps(value, ensure_ascii=True, separators=(",", ":"))


def part_snbt(part: dict[str, Any]) -> str:
    fields = [f"id:{snbt_string(part['id'])}"]
    if "spin" in part:
        fields.append(f"spin:{part['spin']}b")
    if "freq" in part:
        frequency = part["freq"]
        if isinstance(frequency, int) and not isinstance(frequency, bool):
            if not 0 <= frequency <= 0xFFFF:
                raise ValueError("P2P frequency must be an unsigned 16-bit integer")
            signed_frequency = frequency if frequency < 0x8000 else frequency - 0x10000
            fields.append(f"freq:{signed_frequency}s")
        elif isinstance(frequency, str):
            fields.append(f"freq:{snbt_string(frequency)}")
        else:
            raise ValueError("unsupported generated malformed P2P frequency")
    if "overflow" in part:
        fields.append(f"overflow:{snbt_string(part['overflow'])}")
    return "{" + ",".join(fields) + "}"


def facade_snbt(block_state: dict[str, Any]) -> str:
    fields = [f"Name:{snbt_string(block_state['Name'])}"]
    if "Properties" in block_state:
        raw_properties = block_state["Properties"]
        if isinstance(raw_properties, dict):
            properties = ",".join(
                f"{key}:{snbt_string(value)}"
                for key, value in sorted(raw_properties.items())
            )
            fields.append("Properties:{" + properties + "}")
        elif isinstance(raw_properties, str):
            fields.append(f"Properties:{snbt_string(raw_properties)}")
        else:
            raise ValueError("unsupported generated malformed facade properties")
    return "{" + ",".join(fields) + "}"


def cable_bus_snbt(
    anchor: dict[str, Any],
    *,
    include_has_redstone: bool = True,
) -> str:
    # hasRedstone is a live YesNo cache, not structural renderer input. Build
    # fixtures initialize AE2's neutral UNDECIDED ordinal, while settle/verify
    # deliberately compare only the retained fields consumed by the renderer.
    fields = ["hasRedstone:2"] if include_has_redstone else []
    if anchor["cable_id"] is not None:
        fields.append(f"cable:{{id:{snbt_string(anchor['cable_id'])}}}")
    for direction in DIRECTION_DELTAS:
        part = anchor.get("face_parts", {}).get(direction)
        if part is not None:
            fields.append(f"{direction}:{part_snbt(part)}")
    for direction in DIRECTION_DELTAS:
        block_state = anchor.get("facades", {}).get(direction)
        if block_state is not None:
            field_name = "facade" + direction[0].upper() + direction[1:]
            fields.append(f"{field_name}:{facade_snbt(block_state)}")
    return "{" + ",".join(fields) + "}"


def drive_item_snbt(item: dict[str, Any]) -> str:
    fields = [
        f"id:{snbt_string(item['id'])}",
        f"count:{item['count']}",
    ]
    if "components" in item:
        if item["components"] != DRIVE_COMPONENT_INSENSITIVITY:
            raise ValueError("cannot generate unexpected drive item components")
        fields.append(
            'components:{"ae2:storage_cell_inv":['
            '{"#t":"ae2:i","#":64L,id:"minecraft:stone"}'
            "]}"
        )
    return "{" + ",".join(fields) + "}"


def drive_inventory_snbt(anchor: dict[str, Any]) -> str:
    slots = ",".join(
        f"item{slot}:" + ("{}" if item is None else drive_item_snbt(item))
        for slot, item in enumerate(anchor["drive_inventory"])
    )
    return "{inv:{" + slots + "}}"


def drive_block_state(anchor: dict[str, Any]) -> str:
    state = anchor["block_state"]
    return f"{anchor['block_id']}[facing={state['facing']},spin={state['spin']}]"


def m45_block_state(block_id: str, state: dict[str, Any] | None) -> str:
    if not state:
        return block_id
    properties = ",".join(
        f"{key}={value}" for key, value in sorted(state.items())
    )
    return f"{block_id}[{properties}]"


def m45_part_snbt(part: dict[str, Any]) -> str:
    fields = [f"id:{snbt_string(part['id'])}"]
    if "spin" in part:
        fields.append(f"spin:{part['spin']}b")
    if "fast" in part:
        fields.append(f"fast:{1 if part['fast'] else 0}b")
    if "cell" in part:
        cell = part["cell"]
        fields.append(
            "cell:{}"
            if cell is None
            else "cell:" + drive_item_snbt(cell)
        )
    return "{" + ",".join(fields) + "}"


def m45_cable_bus_snbt(
    value: dict[str, Any], *, include_has_redstone: bool = True
) -> str:
    fields = ["hasRedstone:2"] if include_has_redstone else []
    cable_id = value.get("cable_id")
    if cable_id is not None:
        fields.append(f"cable:{{id:{snbt_string(cable_id)}}}")
    for direction in DIRECTION_DELTAS:
        part = value.get("face_parts", {}).get(direction)
        if part is not None:
            fields.append(f"{direction}:{m45_part_snbt(part)}")
    return "{" + ",".join(fields) + "}"


def m45_simple_snbt(value: dict[str, Any]) -> str:
    fields = []
    for key, item in sorted(value.items()):
        if isinstance(item, bool):
            fields.append(f"{key}:{1 if item else 0}b")
        elif isinstance(item, int) and not isinstance(item, bool):
            fields.append(f"{key}:{item}")
        elif isinstance(item, str):
            fields.append(f"{key}:{snbt_string(item)}")
        else:
            raise ValueError("unsupported M4/M5 exact NBT fixture value")
    return "{" + ",".join(fields) + "}"


def m45_build_entry_lines(value: dict[str, Any]) -> list[str]:
    position = block_position(value["position"])
    block_id = value["block_id"]
    placement_state = value.get("placement_state", value.get("block_state"))
    lines = [
        f"setblock {position} {m45_block_state(block_id, placement_state)} replace"
    ]
    if block_id == "ae2:cable_bus":
        lines.append(
            f"data merge block {position} {m45_cable_bus_snbt(value)}"
        )
    elif "drive_inventory" in value:
        lines.append(
            f"data merge block {position} {drive_inventory_snbt(value)}"
        )
    if "expected_nbt" in value:
        lines.append(
            f"data merge block {position} {m45_simple_snbt(value['expected_nbt'])}"
        )
    return lines


def m45_build_lines() -> list[str]:
    lines = [
        "# ATM 1.2.0 M4/M5 cumulative review fixtures.",
        "scoreboard objectives add ae2m45run dummy",
        "scoreboard players add #m45_builds ae2m45run 1",
    ]
    for case in M45_CASES:
        lines.append(f"# {case['case_id']} {case['label']}")
        for fixture in case["fixture_blocks"]:
            lines.extend(m45_build_entry_lines(fixture))
        for anchor in case["anchors"]:
            lines.extend(m45_build_entry_lines(anchor))
        lines.append("")
    return lines


def m45_verification_lines(objective: str) -> list[str]:
    failure = f"run scoreboard players add #failures {objective} 1"
    lines: list[str] = []
    for case in M45_CASES:
        lines.append(f"# {case['case_id']} {case['label']}")
        for value in (*case["anchors"], *case["fixture_blocks"]):
            position = block_position(value["position"])
            lines.append(
                f"execute unless block {position} "
                f"{m45_block_state(value['block_id'], value.get('block_state'))} "
                f"{failure}"
            )
            expected_block_entity_id = value.get("expected_block_entity_id")
            if expected_block_entity_id is not None:
                lines.append(
                    f"execute unless data block {position} "
                    f'{{id:"{expected_block_entity_id}"}} {failure}'
                )
            if value["block_id"] == "ae2:cable_bus":
                lines.append(
                    f"execute unless data block {position} "
                    f"{m45_cable_bus_snbt(value, include_has_redstone=False)} "
                    f"{failure}"
                )
            elif "drive_inventory" in value:
                lines.extend(drive_verification_lines(value, objective))
            if "expected_nbt" in value:
                lines.append(
                    f"execute unless data block {position} "
                    f"{m45_simple_snbt(value['expected_nbt'])} {failure}"
                )
        lines.append("")
    return lines


def crafting_placement_state(anchor: dict[str, Any]) -> str:
    if anchor["block_id"] != CRAFTING_MONITOR_BLOCK_ID:
        return anchor["block_id"]
    state = anchor["block_state"]
    return f"{anchor['block_id']}[facing={state['facing']},spin={state['spin']}]"


def crafting_expected_state(anchor: dict[str, Any]) -> str:
    state = anchor["block_state"]
    properties = [
        "formed=true",
        f"powered={str(state['powered']).lower()}",
    ]
    if anchor["block_id"] == CRAFTING_MONITOR_BLOCK_ID:
        properties.extend((f"facing={state['facing']}", f"spin={state['spin']}"))
    return f"{anchor['block_id']}[" + ",".join(properties) + "]"


def quantum_placement_state(anchor: dict[str, Any]) -> str:
    return f"{anchor['block_id']}[formed=false,waterlogged=false]"


def quantum_expected_state(anchor: dict[str, Any]) -> str:
    return f"{anchor['block_id']}[formed=true,waterlogged=false]"


def m3_completion_block_state(anchor: dict[str, Any]) -> str:
    properties = ",".join(
        f"{key}={str(value).lower() if isinstance(value, bool) else value}"
        for key, value in anchor["block_state"].items()
    )
    return f"{anchor['block_id']}[{properties}]"


def paint_dots_snbt(anchor: dict[str, Any]) -> str:
    if anchor["block_id"] != PAINT_BLOCK_ID or len(anchor["paint_dots"]) != 256:
        raise ValueError("exact paint dots SNBT requires one 256-byte paint array")
    return "[B;" + ",".join(f"{value}b" for value in anchor["paint_dots"]) + "]"


def fixture_block_state(fixture_block: dict[str, Any]) -> str:
    expected = fixture_block.get("expected_state")
    if expected is None:
        return fixture_block["block_id"]
    properties = ",".join(
        f"{key}={str(value).lower() if isinstance(value, bool) else value}"
        for key, value in expected.items()
    )
    return f"{fixture_block['block_id']}[{properties}]"


def fixture_block_placement_state(fixture_block: dict[str, Any]) -> str:
    placement = fixture_block.get("placement_state")
    if placement is None:
        return fixture_block_state(fixture_block)
    properties = ",".join(
        f"{key}={str(value).lower() if isinstance(value, bool) else value}"
        for key, value in placement.items()
    )
    if not properties:
        return fixture_block["block_id"]
    return f"{fixture_block['block_id']}[{properties}]"


def fixture_block_snbt(
    fixture_block: dict[str, Any],
    *,
    include_has_redstone: bool = True,
) -> str | None:
    value = fixture_block.get("block_entity_snbt")
    if value == {"hasRedstone": 2, "cable": {"id": "ae2:fluix_glass_cable"}}:
        return '{hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"}}'
    if value is not None:
        raise ValueError("unsupported generated fixture block NBT")
    if fixture_block.get("block_id") == "ae2:cable_bus":
        return cable_bus_snbt(
            {
                "cable_id": fixture_block.get("cable_id"),
                "face_parts": fixture_block.get("face_parts", {}),
                "facades": fixture_block.get("facades", {}),
            },
            include_has_redstone=include_has_redstone,
        )
    return None


def m3_completion_verification_lines(objective: str) -> list[str]:
    failure = f"run scoreboard players add #failures {objective} 1"
    lines: list[str] = []
    for case in CASES:
        if case["milestone"] != "M3f":
            continue
        lines.append(f"# {case['case_id']} {case['label']}")
        for anchor in case["anchors"]:
            position = block_position(anchor["position"])
            lines.append(
                f"execute unless block {position} {m3_completion_block_state(anchor)} {failure}"
            )
            lines.append(
                f"execute unless data block {position} "
                f'{{id:"{anchor["expected_block_entity_id"]}"}} {failure}'
            )
            if anchor["block_id"] == PAINT_BLOCK_ID:
                lines.append(
                    f"execute unless data block {position} "
                    f"{{dots:{paint_dots_snbt(anchor)}}} {failure}"
                )
        for fixture_block in case["fixture_blocks"]:
            position = block_position(fixture_block["position"])
            lines.append(
                f"execute unless block {position} {fixture_block_state(fixture_block)} {failure}"
            )
            expected_block_entity_id = fixture_block.get("expected_block_entity_id")
            if expected_block_entity_id is not None:
                lines.append(
                    f"execute unless data block {position} "
                    f'{{id:"{expected_block_entity_id}"}} {failure}'
                )
        lines.append("")
    return lines


def native_structural_verification_lines(objective: str) -> list[str]:
    """Verify exact retained S1 data and every direct topology helper."""
    failure = f"run scoreboard players add #failures {objective} 1"
    lines: list[str] = []
    for case in CASES:
        if case["milestone"] != "S1":
            continue
        lines.append(f"# {case['case_id']} {case['label']}")
        for anchor in case["anchors"]:
            position = block_position(anchor["position"])
            lines.append(
                f"execute unless block {position} ae2:cable_bus {failure}"
            )
            lines.append(
                f"execute unless data block {position} "
                f"{cable_bus_snbt(anchor, include_has_redstone=False)} "
                f"{failure}"
            )
        for fixture_block in case["fixture_blocks"]:
            position = block_position(fixture_block["position"])
            lines.append(
                f"execute unless block {position} "
                f"{fixture_block_state(fixture_block)} {failure}"
            )
            fixture_nbt = fixture_block_snbt(
                fixture_block,
                include_has_redstone=False,
            )
            if fixture_nbt is not None:
                lines.append(
                    f"execute unless data block {position} {fixture_nbt} {failure}"
                )
            expected_block_entity_id = fixture_block.get(
                "expected_block_entity_id"
            )
            if expected_block_entity_id is not None:
                lines.append(
                    f"execute unless data block {position} "
                    f'{{id:"{expected_block_entity_id}"}} {failure}'
                )
        lines.append("")
    return lines


def build_function() -> bytes:
    lines = [
        "# SPDX-License-Identifier: LGPL-3.0-only",
        "# Exact AE2 S1 plus ATM 1.2.0 M4/M5 cumulative review fixture.",
        "# Persist one count per complete build invocation so rebuilds are detectable.",
        "scoreboard objectives add ae2m3run dummy",
        "scoreboard players add #m3f_builds ae2m3run 1",
        "scoreboard objectives add ae2s1run dummy",
        "scoreboard players add #s1_builds ae2s1run 1",
        "function ae2_m3:clear",
        "time set noon",
        "weather clear",
        "gamerule doDaylightCycle false",
        "gamerule doWeatherCycle false",
        f"fill {fill_bounds(FIXTURE_SUPPORT_BOUNDS)} minecraft:smooth_stone replace",
        f"fill {fill_bounds(M2_FIXTURE_SUPPORT_BOUNDS)} minecraft:smooth_stone replace",
        f"fill {fill_bounds(M3_FIXTURE_SUPPORT_BOUNDS)} minecraft:smooth_stone replace",
        f"fill {fill_bounds(M3_FIXTURE_AIR_GAP_BOUNDS)} minecraft:air replace",
        f"fill {fill_bounds(M3B_FIXTURE_SUPPORT_BOUNDS)} minecraft:smooth_stone replace",
        f"fill {fill_bounds(M3B_FIXTURE_AIR_GAP_BOUNDS)} minecraft:air replace",
        f"fill {fill_bounds(M3C_FIXTURE_SUPPORT_BOUNDS)} minecraft:smooth_stone replace",
        f"fill {fill_bounds(M3C_FIXTURE_AIR_BOUNDS)} minecraft:air replace",
        f"fill {fill_bounds(M3D_FIXTURE_SUPPORT_BOUNDS)} minecraft:smooth_stone replace",
        f"fill {fill_bounds(M3D_FIXTURE_AIR_BOUNDS)} minecraft:air replace",
        f"fill {fill_bounds(M3E_FIXTURE_BOUNDS)} minecraft:air replace",
        f"fill {fill_bounds(M3F_FIXTURE_BOUNDS)} minecraft:air replace",
        *(
            f"fill {fill_bounds(bounds)} minecraft:air replace"
            for bounds in S1_CLEAR_BOUNDS
        ),
        *(
            f"fill {fill_bounds(bounds)} minecraft:air replace"
            for bounds in M45_CLEAR_BOUNDS
        ),
        "",
    ]
    for case in CASES:
        lines.append(f"# {case['case_id']} {case['label']}")
        if case["milestone"] == "S1":
            # Coplanar planes and endpoint topology observe direct neighbors,
            # so establish every helper before placing the selected anchor.
            for fixture_block in case["fixture_blocks"]:
                fixture_position = block_position(fixture_block["position"])
                lines.append(
                    f"setblock {fixture_position} "
                    f"{fixture_block_placement_state(fixture_block)} replace"
                )
                fixture_nbt = fixture_block_snbt(fixture_block)
                if fixture_nbt is not None:
                    lines.append(
                        f"data merge block {fixture_position} {fixture_nbt}"
                    )
            for anchor in case["anchors"]:
                position = block_position(anchor["position"])
                lines.append(f"setblock {position} ae2:cable_bus replace")
                lines.append(
                    f"data merge block {position} "
                    f"{cable_bus_snbt(anchor)}"
                )
            lines.append("")
            continue
        if case["milestone"] == "M3f":
            # Backing/crankable/native-neighbor context must exist before the
            # selected structural blocks are placed and persisted.
            for fixture_block in case["fixture_blocks"]:
                lines.append(
                    f"setblock {block_position(fixture_block['position'])} "
                    f"{fixture_block_state(fixture_block)} replace"
                )
            for anchor in case["anchors"]:
                position = block_position(anchor["position"])
                lines.append(
                    f"setblock {position} {m3_completion_block_state(anchor)} replace"
                )
                if anchor["block_id"] == PAINT_BLOCK_ID:
                    lines.append(
                        f"data merge block {position} {{dots:{paint_dots_snbt(anchor)}}}"
                    )
            lines.append("")
            continue
        for anchor in case["anchors"]:
            position = block_position(anchor["position"])
            if anchor["block_id"] == "ae2:cable_bus":
                lines.append(f"setblock {position} ae2:cable_bus replace")
                lines.append(
                    f"data merge block {position} {cable_bus_snbt(anchor)}"
                )
            elif anchor["block_id"] in (DRIVE_BLOCK_ID, EXTENDED_DRIVE_BLOCK_ID):
                lines.append(
                    f"setblock {position} {drive_block_state(anchor)} replace"
                )
                lines.append(
                    f"data merge block {position} {drive_inventory_snbt(anchor)}"
                )
            elif anchor["block_id"] in CRAFTING_BLOCK_KINDS:
                lines.append(
                    f"setblock {position} {crafting_placement_state(anchor)} replace"
                )
                if anchor["block_id"] == CRAFTING_MONITOR_BLOCK_ID:
                    lines.append(
                        f"data merge block {position} "
                        f"{{paintedColor:{anchor['painted_color_ordinal']}b}}"
                    )
            elif anchor["block_id"] in {QUANTUM_LINK_BLOCK_ID, QUANTUM_RING_BLOCK_ID}:
                lines.append(
                    f"setblock {position} {quantum_placement_state(anchor)} replace"
                )
            else:
                lines.append(f"setblock {position} {anchor['block_id']} replace")
        for fixture_block in case["fixture_blocks"]:
            lines.append(
                f"setblock {block_position(fixture_block['position'])} "
                f"{fixture_block['block_id']} replace"
            )
            fixture_nbt = fixture_block_snbt(fixture_block)
            if fixture_nbt is not None:
                lines.append(
                    f"data merge block {block_position(fixture_block['position'])} "
                    f"{fixture_nbt}"
                )
        lines.append("")

    lines.extend(m45_build_lines())

    lines.extend(
        (
            "# Read/capture controller compatibility sentinel.",
            f"setblock {block_position(SENTINEL_SUPPORT)} minecraft:stone replace",
            f"setblock {block_position(SENTINEL_FRAME)} framedblocks:framed_cube[alt=false,glowing=false,propagates_skylight=false,reinforced=false,solid=true,solid_bg=false] replace",
            f'data merge block {block_position(SENTINEL_FRAME)} {{camo:{{type:"framedblocks:block",state:{{Name:"minecraft:stone"}}}},glowing:0b,intangible:0b,reinforced:0b,updated:3b}}',
            f"setblock {block_position(SENTINEL_GAP)} minecraft:air replace",
            f"setblock {block_position(SENTINEL_CONTROL)} minecraft:stone replace",
            "",
            "# South observation deck and guarded pose volume.",
            f"fill {fill_bounds(DECK_FLOOR_BOUNDS)} minecraft:stone_bricks replace",
            f"fill {fill_bounds(DECK_AIR_BOUNDS)} minecraft:air replace",
            "",
            "scoreboard objectives add ae2m3s dummy",
            "scoreboard players set #attempts ae2m3s 0",
            "scoreboard players set #stable ae2m3s 0",
            "schedule function ae2_m3:settle_check 20t replace",
            f'tellraw @a [{{"text":"Built cumulative AE2 ATM 1.2.0 review gallery: {TOTAL_CASE_COUNT} cases, {TOTAL_ANCHOR_COUNT} anchors; waiting for two consecutive exact checks.","color":"aqua"}}]',
            "",
        )
    )
    return "\n".join(lines).encode("utf-8")


def load_function() -> bytes:
    lines = (
        "# SPDX-License-Identifier: LGPL-3.0-only",
        "# Force only the main gallery areas; building is an explicit operator action.",
        f"forceload add {horizontal_bounds(FIXTURE_BOUNDS)}",
        f"forceload add {horizontal_bounds(M2_FIXTURE_BOUNDS)}",
        f"forceload add {horizontal_bounds(M3_FIXTURE_BOUNDS)}",
        f"forceload add {horizontal_bounds(M3B_FIXTURE_BOUNDS)}",
        f"forceload add {horizontal_bounds(M3C_FIXTURE_BOUNDS)}",
        f"forceload add {horizontal_bounds(M3D_FIXTURE_BOUNDS)}",
        f"forceload add {horizontal_bounds(M3E_FIXTURE_BOUNDS)}",
        f"forceload add {horizontal_bounds(M3F_FIXTURE_BOUNDS)}",
        f"forceload add {horizontal_bounds(S1_FIXTURE_BOUNDS)}",
        f"forceload add {horizontal_bounds(M45_FIXTURE_BOUNDS)}",
        f"forceload add {horizontal_bounds(DECK_BOUNDS)}",
        f"forceload add {horizontal_bounds(SENTINEL_BOUNDS)}",
        "",
    )
    return "\n".join(lines).encode("utf-8")


def release_function() -> bytes:
    lines = (
        "# SPDX-License-Identifier: LGPL-3.0-only",
        "# Release only the main chunk sets forced by ae2_m3:load.",
        f"forceload remove {horizontal_bounds(FIXTURE_BOUNDS)}",
        f"forceload remove {horizontal_bounds(M2_FIXTURE_BOUNDS)}",
        f"forceload remove {horizontal_bounds(M3_FIXTURE_BOUNDS)}",
        f"forceload remove {horizontal_bounds(M3B_FIXTURE_BOUNDS)}",
        f"forceload remove {horizontal_bounds(M3C_FIXTURE_BOUNDS)}",
        f"forceload remove {horizontal_bounds(M3D_FIXTURE_BOUNDS)}",
        f"forceload remove {horizontal_bounds(M3E_FIXTURE_BOUNDS)}",
        f"forceload remove {horizontal_bounds(M3F_FIXTURE_BOUNDS)}",
        f"forceload remove {horizontal_bounds(S1_FIXTURE_BOUNDS)}",
        f"forceload remove {horizontal_bounds(M45_FIXTURE_BOUNDS)}",
        f"forceload remove {horizontal_bounds(DECK_BOUNDS)}",
        f"forceload remove {horizontal_bounds(SENTINEL_BOUNDS)}",
        'tellraw @a [{"text":"Released the cumulative AE2 review-gallery chunks.","color":"yellow"}]',
        "",
    )
    return "\n".join(lines).encode("utf-8")


def clear_function() -> bytes:
    lines = (
        "# SPDX-License-Identifier: LGPL-3.0-only",
        "# Clear only the eleven disjoint main gallery-owned volumes.",
        "schedule clear ae2_m3:settle_check",
        f"fill {fill_bounds(FIXTURE_BOUNDS)} minecraft:air replace",
        f"fill {fill_bounds(M2_FIXTURE_BOUNDS)} minecraft:air replace",
        f"fill {fill_bounds(M3_FIXTURE_BOUNDS)} minecraft:air replace",
        f"fill {fill_bounds(M3B_FIXTURE_BOUNDS)} minecraft:air replace",
        f"fill {fill_bounds(M3C_FIXTURE_BOUNDS)} minecraft:air replace",
        f"fill {fill_bounds(M3D_FIXTURE_BOUNDS)} minecraft:air replace",
        f"fill {fill_bounds(M3E_FIXTURE_BOUNDS)} minecraft:air replace",
        f"fill {fill_bounds(M3F_FIXTURE_BOUNDS)} minecraft:air replace",
        *(
            f"fill {fill_bounds(bounds)} minecraft:air replace"
            for bounds in S1_CLEAR_BOUNDS
        ),
        *(
            f"fill {fill_bounds(bounds)} minecraft:air replace"
            for bounds in M45_CLEAR_BOUNDS
        ),
        f"fill {fill_bounds(DECK_BOUNDS)} minecraft:air replace",
        f"fill {fill_bounds(SENTINEL_BOUNDS)} minecraft:air replace",
        'tellraw @a [{"text":"Cleared the bounded cumulative AE2 review-gallery volumes.","color":"yellow"}]',
        "",
    )
    return "\n".join(lines).encode("utf-8")


def drive_verification_lines(
    anchor: dict[str, Any],
    objective: str,
) -> list[str]:
    position = block_position(anchor["position"])
    failure = f"run scoreboard players add #failures {objective} 1"
    lines = [
        f"execute unless block {position} {drive_block_state(anchor)} {failure}",
        f"execute unless data block {position} inv {failure}",
    ]
    for slot, item in enumerate(anchor["drive_inventory"]):
        path = f"inv.item{slot}"
        lines.append(f"execute unless data block {position} {path} {failure}")
        if item is None:
            for retained_field in ("id", "count", "components"):
                lines.append(
                    f"execute if data block {position} {path}.{retained_field} {failure}"
                )
        else:
            expected = "{inv:{item" + str(slot) + ":" + drive_item_snbt(item) + "}}"
            lines.append(
                f"execute unless data block {position} {expected} {failure}"
            )
            if "components" not in item:
                lines.append(
                    f"execute if data block {position} {path}.components {failure}"
                )
    lines.append(
        f"execute if data block {position} inv.item{len(anchor['drive_inventory'])} "
        f"{failure}"
    )
    return lines


def crafting_verification_lines(objective: str) -> list[str]:
    failure = f"run scoreboard players add #failures {objective} 1"
    lines: list[str] = []
    for case in CASES:
        if case["milestone"] != "M3d":
            continue
        lines.append(f"# {case['case_id']} {case['label']}")
        for anchor in case["anchors"]:
            position = block_position(anchor["position"])
            lines.append(
                f"execute unless block {position} {crafting_expected_state(anchor)} {failure}"
            )
            if anchor["block_id"] == CRAFTING_MONITOR_BLOCK_ID:
                lines.append(
                    f"execute unless data block {position} "
                    f"{{paintedColor:{anchor['painted_color_ordinal']}b}} {failure}"
                )
        for fixture_block in case["fixture_blocks"]:
            position = block_position(fixture_block["position"])
            lines.append(
                f"execute unless block {position} {fixture_block_state(fixture_block)} {failure}"
            )
            fixture_nbt = fixture_block_snbt(fixture_block)
            if fixture_nbt is not None:
                lines.append(
                    f"execute unless data block {position} {fixture_nbt} {failure}"
                )
        lines.append("")
    return lines


def quantum_verification_lines(objective: str) -> list[str]:
    failure = f"run scoreboard players add #failures {objective} 1"
    lines: list[str] = []
    for case in CASES:
        if case["milestone"] != "M3e":
            continue
        lines.append(f"# {case['case_id']} {case['label']}")
        for anchor in case["anchors"]:
            position = block_position(anchor["position"])
            lines.append(
                f"execute unless block {position} {quantum_expected_state(anchor)} {failure}"
            )
            lines.append(
                f'execute unless data block {position} {{id:"{QUANTUM_BLOCK_ENTITY_ID}"}} {failure}'
            )
        lines.append("")
    return lines


def m3d_preprobe_cases() -> tuple[dict[str, Any], ...]:
    """Return only the two runtime-sensitive M3d cuboids used by preprobe."""
    selected = tuple(
        case
        for case in CASES
        if case["case_id"] in {"ae2-m3d-06", "ae2-m3d-09"}
    )
    if tuple(case["case_id"] for case in selected) != (
        "ae2-m3d-06",
        "ae2-m3d-09",
    ):
        raise ValueError("M3d preprobe case selection changed unexpectedly")
    return selected


def preprobe_verification_lines(objective: str) -> list[str]:
    """Check only stable render-relevant state in the two preprobe cuboids."""
    failure = f"run scoreboard players add #failures {objective} 1"
    lines: list[str] = []
    for case in m3d_preprobe_cases():
        lines.append(f"# {case['case_id']} {case['label']}")
        for anchor in case["anchors"]:
            position = block_position(anchor["position"])
            lines.append(
                f"execute unless block {position} {crafting_expected_state(anchor)} {failure}"
            )
            if anchor["block_id"] == CRAFTING_MONITOR_BLOCK_ID:
                lines.append(
                    f"execute unless data block {position} "
                    f"{{paintedColor:{anchor['painted_color_ordinal']}b}} {failure}"
                )
        for fixture_block in case["fixture_blocks"]:
            position = block_position(fixture_block["position"])
            lines.append(
                f"execute unless block {position} {fixture_block_state(fixture_block)} {failure}"
            )
            fixture_nbt = fixture_block_snbt(fixture_block)
            if fixture_nbt is not None:
                lines.append(
                    f"execute unless data block {position} {fixture_nbt} {failure}"
                )
        lines.append("")
    return lines


def preprobe_function() -> bytes:
    """Build only the two runtime-sensitive M3d structures, never the gallery."""
    lines = [
        "# SPDX-License-Identifier: LGPL-3.0-only",
        "# Bounded M3d formation/power preprobe; does not invoke the gallery build.",
        "schedule clear ae2_m3:preprobe_check",
        f"forceload add {horizontal_bounds(M3D_PREPROBE_POWERED_BOUNDS)}",
        f"forceload add {horizontal_bounds(M3D_PREPROBE_MIXED_BOUNDS)}",
        f"fill {fill_bounds(M3D_PREPROBE_POWERED_BOUNDS)} minecraft:air replace",
        f"fill {fill_bounds(M3D_PREPROBE_MIXED_BOUNDS)} minecraft:air replace",
        "fill 296 97 268 299 97 271 minecraft:smooth_stone replace",
        "fill 316 97 260 319 97 262 minecraft:smooth_stone replace",
        "",
    ]
    for case in m3d_preprobe_cases():
        lines.append(f"# {case['case_id']} {case['label']}")
        for anchor in case["anchors"]:
            position = block_position(anchor["position"])
            lines.append(
                f"setblock {position} {crafting_placement_state(anchor)} replace"
            )
            if anchor["block_id"] == CRAFTING_MONITOR_BLOCK_ID:
                lines.append(
                    f"data merge block {position} "
                    f"{{paintedColor:{anchor['painted_color_ordinal']}b}}"
                )
        for fixture_block in case["fixture_blocks"]:
            position = block_position(fixture_block["position"])
            lines.append(
                f"setblock {position} {fixture_block['block_id']} replace"
            )
            fixture_nbt = fixture_block_snbt(fixture_block)
            if fixture_nbt is not None:
                lines.append(f"data merge block {position} {fixture_nbt}")
        lines.append("")
    lines.extend(
        (
            "scoreboard objectives add ae2m3p dummy",
            "scoreboard players set #attempts ae2m3p 0",
            "scoreboard players set #failures ae2m3p 0",
            "scoreboard players set #stable ae2m3p 0",
            "scoreboard players set #result ae2m3p 0",
            "schedule function ae2_m3:preprobe_check 20t replace",
            'tellraw @a [{"text":"AE2 M3d bounded preprobe scheduled; inspect #result ae2m3p (1=pass, -1=timeout).","color":"aqua"}]',
            "",
        )
    )
    return "\n".join(lines).encode("utf-8")


def preprobe_check_function() -> bytes:
    lines = [
        "# SPDX-License-Identifier: LGPL-3.0-only",
        "# Require two consecutive exact checks without persisting fake formed/powered state.",
        "scoreboard objectives add ae2m3p dummy",
        "scoreboard players add #attempts ae2m3p 1",
        "scoreboard players set #failures ae2m3p 0",
        "",
        *preprobe_verification_lines("ae2m3p"),
        "execute if score #failures ae2m3p matches 0 run scoreboard players add #stable ae2m3p 1",
        "execute unless score #failures ae2m3p matches 0 run scoreboard players set #stable ae2m3p 0",
        "execute if score #stable ae2m3p matches 2.. run scoreboard players set #result ae2m3p 1",
        'execute if score #stable ae2m3p matches 2.. run tellraw @a [{"text":"AE2 M3d bounded preprobe passed two consecutive formed/powered checks.","color":"green"}]',
        "execute unless score #stable ae2m3p matches 2.. if score #attempts ae2m3p matches ..59 run schedule function ae2_m3:preprobe_check 20t replace",
        "execute unless score #stable ae2m3p matches 2.. if score #attempts ae2m3p matches 60.. run scoreboard players set #result ae2m3p -1",
        'execute unless score #stable ae2m3p matches 2.. if score #attempts ae2m3p matches 60.. run tellraw @a [{"text":"AE2 M3d bounded preprobe timed out before two consecutive exact checks.","color":"red"}]',
        "",
    ]
    return "\n".join(lines).encode("utf-8")


def settle_check_function() -> bytes:
    lines = [
        "# SPDX-License-Identifier: LGPL-3.0-only",
        "# Wait for stable S1 and exact M4/M5 persisted review states; require two consecutive exact checks.",
        "scoreboard objectives add ae2m3s dummy",
        "scoreboard players add #attempts ae2m3s 1",
        "scoreboard players set #failures ae2m3s 0",
        "",
        *crafting_verification_lines("ae2m3s"),
        *quantum_verification_lines("ae2m3s"),
        *m3_completion_verification_lines("ae2m3s"),
        *native_structural_verification_lines("ae2m3s"),
        *m45_verification_lines("ae2m3s"),
        "execute if score #failures ae2m3s matches 0 run scoreboard players add #stable ae2m3s 1",
        "execute unless score #failures ae2m3s matches 0 run scoreboard players set #stable ae2m3s 0",
        "execute if score #stable ae2m3s matches 2.. run save-all flush",
        "execute if score #stable ae2m3s matches 2.. run function ae2_m3:verify",
        "execute unless score #stable ae2m3s matches 2.. if score #attempts ae2m3s matches ..59 run schedule function ae2_m3:settle_check 20t replace",
        'execute unless score #stable ae2m3s matches 2.. if score #attempts ae2m3s matches 60.. run tellraw @a [{"text":"AE2 cumulative review fixture did not reach two consecutive exact structural checks within 60 seconds; no save/verify was accepted. A rewritten physical M5 state is a deliberate fail-closed result.","color":"red"}]',
        "",
    ]
    return "\n".join(lines).encode("utf-8")


def verify_function() -> bytes:
    lines = [
        "# SPDX-License-Identifier: LGPL-3.0-only",
        "# Structural verification only; this does not validate rendered pixels.",
        "scoreboard objectives add ae2m3v dummy",
        "scoreboard players set #failures ae2m3v 0",
        "scoreboard objectives add ae2m3run dummy",
        "execute unless score #m3f_builds ae2m3run matches 1 run scoreboard players add #failures ae2m3v 1",
        "scoreboard objectives add ae2s1run dummy",
        "execute unless score #s1_builds ae2s1run matches 1 run scoreboard players add #failures ae2m3v 1",
        "scoreboard objectives add ae2m45run dummy",
        "execute unless score #m45_builds ae2m45run matches 1 run scoreboard players add #failures ae2m3v 1",
        "execute unless score #stable ae2m3s matches 2.. run scoreboard players add #failures ae2m3v 1",
        "",
    ]
    for case in CASES:
        lines.append(f"# {case['case_id']} {case['label']}")
        for anchor in case["anchors"]:
            position = block_position(anchor["position"])
            if anchor["block_id"] in (DRIVE_BLOCK_ID, EXTENDED_DRIVE_BLOCK_ID):
                lines.extend(drive_verification_lines(anchor, "ae2m3v"))
            elif anchor["block_id"] in CRAFTING_BLOCK_KINDS:
                # M3d state/NBT is verified below as one bounded route batch.
                pass
            elif anchor["block_id"] in {QUANTUM_LINK_BLOCK_ID, QUANTUM_RING_BLOCK_ID}:
                # M3e state/BE identity is verified below as one bounded route batch.
                pass
            elif case["milestone"] == "M3f":
                # M3f state/BE/durable paint bytes are verified as one route batch.
                pass
            else:
                lines.append(
                    f"execute unless block {position} {anchor['block_id']} run scoreboard players add #failures ae2m3v 1"
                )
            if (
                anchor["block_id"] == "ae2:cable_bus"
                and case["milestone"] != "S1"
            ):
                lines.append(
                    f'execute unless data block {position} '
                    f'{cable_bus_snbt(anchor)} '
                    "run scoreboard players add #failures ae2m3v 1"
                )
                for direction, block_state in anchor.get("facades", {}).items():
                    if "Properties" not in block_state:
                        field_name = "facade" + direction[0].upper() + direction[1:]
                        lines.append(
                            f"execute if data block {position} "
                            f"{field_name}.Properties run scoreboard players add "
                            "#failures ae2m3v 1"
                        )
            x, y, z = anchor["position"]
            if anchor["block_id"] in CRAFTING_BLOCK_KINDS:
                pass
            elif anchor["block_id"] in {QUANTUM_LINK_BLOCK_ID, QUANTUM_RING_BLOCK_ID}:
                pass
            elif case["milestone"] == "M3f":
                pass
            elif case["milestone"] == "S1":
                # S1 is deliberately air-isolated; exact anchor/helper NBT is
                # checked in the route batch below, without an inherited floor.
                pass
            elif anchor["block_id"] in CONNECTED_GLASS_BLOCK_IDS:
                lines.extend(
                    (
                        f"execute unless block {x} 97 {z} minecraft:smooth_stone run scoreboard players add #failures ae2m3v 1",
                        f"execute unless block {x} 98 {z} minecraft:air run scoreboard players add #failures ae2m3v 1",
                    )
                )
            elif y == 100:
                if anchor["block_id"] in (DRIVE_BLOCK_ID, EXTENDED_DRIVE_BLOCK_ID):
                    lines.extend(
                        (
                            f"execute unless block {x} 98 {z} minecraft:smooth_stone run scoreboard players add #failures ae2m3v 1",
                            f"execute unless block {x} 99 {z} minecraft:air run scoreboard players add #failures ae2m3v 1",
                        )
                    )
                else:
                    lines.append(
                        f"execute unless block {x} 99 {z} minecraft:smooth_stone run scoreboard players add #failures ae2m3v 1"
                    )
        for fixture_block in case["fixture_blocks"]:
            if case["milestone"] in {"M3d", "M3f", "S1"}:
                continue
            lines.append(
                f"execute unless block {block_position(fixture_block['position'])} "
                f"{fixture_block['block_id']} run scoreboard players add #failures ae2m3v 1"
            )
        lines.append("")

    lines.extend(crafting_verification_lines("ae2m3v"))
    lines.extend(quantum_verification_lines("ae2m3v"))
    lines.extend(m3_completion_verification_lines("ae2m3v"))
    lines.extend(native_structural_verification_lines("ae2m3v"))
    lines.extend(m45_verification_lines("ae2m3v"))

    lines.extend(
        (
            "# Controller sentinel and guarded south pose.",
            f"execute unless block {block_position(SENTINEL_SUPPORT)} minecraft:stone run scoreboard players add #failures ae2m3v 1",
            f"execute unless block {block_position(SENTINEL_FRAME)} framedblocks:framed_cube[alt=false,glowing=false,propagates_skylight=false,reinforced=false,solid=true,solid_bg=false] run scoreboard players add #failures ae2m3v 1",
            f'execute unless data block {block_position(SENTINEL_FRAME)} {{camo:{{type:"framedblocks:block",state:{{Name:"minecraft:stone"}}}},glowing:0b,intangible:0b,reinforced:0b,updated:3b}} run scoreboard players add #failures ae2m3v 1',
            f"execute unless block {block_position(SENTINEL_GAP)} minecraft:air run scoreboard players add #failures ae2m3v 1",
            f"execute unless block {block_position(SENTINEL_CONTROL)} minecraft:stone run scoreboard players add #failures ae2m3v 1",
            f"execute unless block {block_position(POSE_FLOOR)} minecraft:stone_bricks run scoreboard players add #failures ae2m3v 1",
            f"execute unless block {block_position(POSE_FEET)} minecraft:air run scoreboard players add #failures ae2m3v 1",
            f"execute unless block {block_position(POSE_HEAD)} minecraft:air run scoreboard players add #failures ae2m3v 1",
            "",
            f'execute if score #failures ae2m3v matches 0 run tellraw @a [{{"text":"AE2 ATM 1.2.0 cumulative review verification passed: {TOTAL_CASE_COUNT}/{TOTAL_CASE_COUNT} cases and {TOTAL_ANCHOR_COUNT}/{TOTAL_ANCHOR_COUNT} anchors after two consecutive exact checks.","color":"green"}}]',
            'execute unless score #failures ae2m3v matches 0 run tellraw @a [{"text":"AE2/ExtendedAE S1 fixture verification failed with ","color":"red"},{"score":{"name":"#failures","objective":"ae2m3v"}},{"text":" mismatches.","color":"red"}]',
            "",
        )
    )
    return "\n".join(lines).encode("utf-8")


def pose_function() -> bytes:
    lines = (
        "# SPDX-License-Identifier: LGPL-3.0-only",
        "# Fixed south observation pose; no teleport if the deck is unsafe.",
        f"execute if block {block_position(POSE_FLOOR)} minecraft:stone_bricks if block {block_position(POSE_FEET)} minecraft:air if block {block_position(POSE_HEAD)} minecraft:air run teleport @s 221.5 108 254.5 180 14",
        f'execute unless block {block_position(POSE_FLOOR)} minecraft:stone_bricks run tellraw @s [{{"text":"Observation pose refused: deck floor is missing.","color":"red"}}]',
        f'execute unless block {block_position(POSE_FEET)} minecraft:air run tellraw @s [{{"text":"Observation pose refused: foot space is blocked.","color":"red"}}]',
        f'execute unless block {block_position(POSE_HEAD)} minecraft:air run tellraw @s [{{"text":"Observation pose refused: head space is blocked.","color":"red"}}]',
        "",
    )
    return "\n".join(lines).encode("utf-8")


def dense_clear_function() -> bytes:
    lines = [
        "# SPDX-License-Identifier: LGPL-3.0-only",
        "# Clear only the four disjoint optional dense-fixture owned volumes.",
    ]
    lines.extend(f"fill {fill_bounds(bounds)} minecraft:air replace" for bounds in DENSE_OWNED_BOUNDS)
    lines.extend(
        (
            'tellraw @a [{"text":"Cleared the optional AE2 M1 regression dense fixture.","color":"yellow"}]',
            "",
        )
    )
    return "\n".join(lines).encode("utf-8")


def dense_build_function() -> bytes:
    lines = [
        "# SPDX-License-Identifier: LGPL-3.0-only",
        "# Opt-in only: four scheduled 8x4x8 covered-dense lattices.",
    ]
    lines.extend(
        f"forceload add {chunk_x * 16} {chunk_z * 16}"
        for chunk_x, chunk_z in dense_exclusive_chunks()
    )
    lines.append("function ae2_m3:dense/clear")
    for bounds in DENSE_CABLE_BOUNDS:
        floor = ((bounds[0][0], 95, bounds[0][2]), (bounds[1][0], 95, bounds[1][2]))
        lines.append(f"fill {fill_bounds(floor)} minecraft:smooth_stone replace")
    lines.extend(
        (
            "schedule function ae2_m3:dense/batch_1 1t replace",
            'tellraw @a [{"text":"Scheduled the optional AE2 M1 regression dense fixture in four batches.","color":"aqua"}]',
            "",
        )
    )
    return "\n".join(lines).encode("utf-8")


def dense_batch_function(index: int) -> bytes:
    bounds = DENSE_CABLE_BOUNDS[index]
    lines = [
        "# SPDX-License-Identifier: LGPL-3.0-only",
        f"# Optional dense fixture batch {index + 1}/{len(DENSE_CABLE_BOUNDS)}.",
        f"fill {fill_bounds(bounds)} ae2:cable_bus replace",
    ]
    for x in range(bounds[0][0], bounds[1][0] + 1):
        for y in range(bounds[0][1], bounds[1][1] + 1):
            for z in range(bounds[0][2], bounds[1][2] + 1):
                lines.append(
                    f'data merge block {x} {y} {z} '
                    f'{{hasRedstone:2,cable:{{id:"{DENSE_CABLE_ID}"}}}}'
                )
    if index + 1 < len(DENSE_CABLE_BOUNDS):
        lines.append(f"schedule function ae2_m3:dense/batch_{index + 2} 20t replace")
    else:
        lines.extend(("save-all flush", "function ae2_m3:dense/verify"))
    lines.append("")
    return "\n".join(lines).encode("utf-8")


def dense_verify_function() -> bytes:
    lines = [
        "# SPDX-License-Identifier: LGPL-3.0-only",
        "# Structural verification only; expected rendered total is 63,488 triangles.",
        "scoreboard objectives add ae2m3d dummy",
        "scoreboard players set #failures ae2m3d 0",
    ]
    for bounds in DENSE_CABLE_BOUNDS:
        for x in range(bounds[0][0], bounds[1][0] + 1):
            for y in range(bounds[0][1], bounds[1][1] + 1):
                for z in range(bounds[0][2], bounds[1][2] + 1):
                    lines.append(
                        f'execute unless data block {x} {y} {z} '
                        f'{{hasRedstone:2,cable:{{id:"{DENSE_CABLE_ID}"}}}} '
                        "run scoreboard players add #failures ae2m3d 1"
                    )
    lines.extend(
        (
            f'execute if score #failures ae2m3d matches 0 run tellraw @a [{{"text":"AE2 M1 regression dense fixture verification passed: {DENSE_CELL_COUNT}/{DENSE_CELL_COUNT} cells.","color":"green"}}]',
            'execute unless score #failures ae2m3d matches 0 run tellraw @a [{"text":"AE2 M1 regression dense fixture verification failed with ","color":"red"},{"score":{"name":"#failures","objective":"ae2m3d"}},{"text":" mismatches.","color":"red"}]',
            "",
        )
    )
    return "\n".join(lines).encode("utf-8")


def dense_release_function() -> bytes:
    lines = [
        "# SPDX-License-Identifier: LGPL-3.0-only",
        "# Release only chunks forced by the opt-in dense build.",
    ]
    lines.extend(
        f"forceload remove {chunk_x * 16} {chunk_z * 16}"
        for chunk_x, chunk_z in dense_exclusive_chunks()
    )
    lines.extend(
        (
            'tellraw @a [{"text":"Released the optional AE2 M1 regression dense-fixture chunks.","color":"yellow"}]',
            "",
        )
    )
    return "\n".join(lines).encode("utf-8")


def expected_outputs() -> dict[Path, bytes]:
    pack = {
        "pack": {
            "pack_format": 48,
            "description": "AE2 ATM 1.2.0 cumulative S1/M4/M5 BlueMap review fixtures",
        }
    }
    outputs = {
        Path("cases.json"): json_bytes(cases_manifest()),
        Path("cases.tsv"): cases_tsv(),
        Path("datapack/pack.mcmeta"): json_bytes(pack),
        FUNCTION_ROOT / "build.mcfunction": build_function(),
        FUNCTION_ROOT / "load.mcfunction": load_function(),
        FUNCTION_ROOT / "release.mcfunction": release_function(),
        FUNCTION_ROOT / "clear.mcfunction": clear_function(),
        FUNCTION_ROOT / "verify.mcfunction": verify_function(),
        FUNCTION_ROOT / "settle_check.mcfunction": settle_check_function(),
        FUNCTION_ROOT / "preprobe.mcfunction": preprobe_function(),
        FUNCTION_ROOT / "preprobe_check.mcfunction": preprobe_check_function(),
        FUNCTION_ROOT / "pose_south.mcfunction": pose_function(),
        FUNCTION_ROOT / "dense/build.mcfunction": dense_build_function(),
        FUNCTION_ROOT / "dense/clear.mcfunction": dense_clear_function(),
        FUNCTION_ROOT / "dense/verify.mcfunction": dense_verify_function(),
        FUNCTION_ROOT / "dense/release.mcfunction": dense_release_function(),
        ALIAS_ROOT / "pose_south.mcfunction": (
            b"# SPDX-License-Identifier: LGPL-3.0-only\n"
            b"# Compatibility alias for the existing read/capture controller.\n"
            b"function ae2_m3:pose_south\n"
        ),
        LOAD_TAG: json_bytes({"values": ["ae2_m3:load"]}),
    }
    for index in range(len(DENSE_CABLE_BOUNDS)):
        outputs[
            FUNCTION_ROOT / f"dense/batch_{index + 1}.mcfunction"
        ] = dense_batch_function(index)
    checksums = [
        f"{hashlib.sha256(content).hexdigest()}  {path.as_posix()}"
        for path, content in sorted(outputs.items(), key=lambda row: row[0].as_posix())
    ]
    outputs[Path("SHA256SUMS")] = ("\n".join(checksums) + "\n").encode("utf-8")
    return outputs


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument(
        "--check", action="store_true", help="Verify tracked outputs without changing them."
    )
    args = parser.parse_args()

    outputs = expected_outputs()
    mismatches = []
    for relative, expected in outputs.items():
        target = ROOT / relative
        if args.check:
            if not target.is_file() or target.read_bytes() != expected:
                mismatches.append(relative.as_posix())
        else:
            target.parent.mkdir(parents=True, exist_ok=True)
            target.write_bytes(expected)
    for relative in OBSOLETE_OUTPUTS:
        target = ROOT / relative
        if args.check:
            if target.exists():
                mismatches.append(relative.as_posix())
        elif target.is_file():
            target.unlink()
    for relative in OBSOLETE_DIRECTORIES:
        target = ROOT / relative
        if args.check:
            if target.exists():
                mismatches.append(relative.as_posix())
        elif target.exists():
            if not target.is_dir():
                raise ValueError(f"obsolete generated namespace is not a directory: {target}")
            try:
                target.rmdir()
            except OSError as exception:
                raise ValueError(
                    f"obsolete generated namespace contains unexpected files: {target}"
                ) from exception

    if mismatches:
        raise ValueError(f"generated gallery outputs are stale: {mismatches}")
    action = "Verified" if args.check else "Generated"
    print(
        f"{action} AE2 19.2.17 ATM 1.2.0 cumulative review gallery: "
        f"{TOTAL_CASE_COUNT} cases, "
        f"{TOTAL_ANCHOR_COUNT} source anchors, "
        f"{EXPECTED_CUSTOM_ANCHOR_COUNT}/{EXPECTED_CUSTOM_TRIANGLE_COUNT} "
        f"source-row custom anchors/triangles, "
        f"{EXPECTED_EFFECTIVE_CUSTOM_ANCHOR_COUNT}/"
        f"{EXPECTED_EFFECTIVE_CUSTOM_TRIANGLE_COUNT} effective custom anchors/triangles, "
        f"{EXPECTED_EFFECTIVE_STOCK_FALLBACK_ANCHOR_COUNT} effective fallbacks; "
        f"M1 regression {EXPECTED_M1_CASE_COUNT} cases/{EXPECTED_M1_ANCHOR_COUNT} "
        f"anchors, M2 {EXPECTED_M2_CASE_COUNT} cases/{EXPECTED_M2_ANCHOR_COUNT} anchors, "
        f"M3a {EXPECTED_M3_CASE_COUNT} cases/{EXPECTED_M3_ANCHOR_COUNT} anchors, "
        f"M3b {EXPECTED_M3B_CASE_COUNT} cases/{EXPECTED_M3B_ANCHOR_COUNT} anchors, "
        f"M3c {EXPECTED_M3C_CASE_COUNT} cases/{EXPECTED_M3C_ANCHOR_COUNT} anchors, "
        f"M3d {EXPECTED_M3D_CASE_COUNT} cases/{EXPECTED_M3D_ANCHOR_COUNT} anchors; "
        f"M3e {EXPECTED_M3E_CASE_COUNT} cases/{EXPECTED_M3E_ANCHOR_COUNT} anchors; "
        f"M3f {EXPECTED_M3F_CASE_COUNT} cases/{EXPECTED_M3F_ANCHOR_COUNT} anchors; "
        f"S1 {EXPECTED_S1_CASE_COUNT} cases/{EXPECTED_S1_ANCHOR_COUNT} anchors/"
        f"{EXPECTED_S1_CUSTOM_TRIANGLE_COUNT} appended custom triangles, "
        f"legacy upgrades {EXPECTED_LEGACY_UPGRADE_ANCHOR_COUNT}/"
        f"{EXPECTED_LEGACY_UPGRADE_CUSTOM_TRIANGLE_COUNT}, combined native structural "
        f"{S1_ORACLE_ANCHOR_COUNT + EXPECTED_LEGACY_UPGRADE_ANCHOR_COUNT}/"
        f"{S1_ORACLE_TRIANGLE_COUNT + EXPECTED_LEGACY_UPGRADE_CUSTOM_TRIANGLE_COUNT}; "
        f"M4/M5 review {M45_CASE_COUNT} cases/{M45_ANCHOR_COUNT} anchors across "
        f"{len(M45_ROUTES)} independently disabled routes; "
        f"optional dense fixture {DENSE_CELL_COUNT} cells/"
        f"{DENSE_EXPECTED_TRIANGLES} triangles."
    )
    return 0


if __name__ == "__main__":
    try:
        sys.exit(main())
    except (OSError, ValueError) as error:
        print(f"gallery generation failed: {error}", file=sys.stderr)
        sys.exit(1)
