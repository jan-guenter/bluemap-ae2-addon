#!/usr/bin/env python3
# SPDX-License-Identifier: LGPL-3.0-only
"""Produce deterministic AE2/ExtendedAE S1 evidence from BlueMap 5.22 output.

The parser is intentionally limited to the non-indexed, little-endian PRBM v1
layout emitted by BlueMap 5.22's ``PRBMWriter``.  It also validates the single
gzip member used by file map storage and resolves material ordinals through the
matching ``textures.json.gz`` file.

PRBM does not retain a block or renderer identifier. Gallery ownership first
moves each triangle centroid ``2^-10`` blocks opposite its geometric normal
and floors that sample to a Minecraft block cell. The bias safely exceeds AE2
dense straight cable's ``0.01/16`` axial overhang while remaining far below its
0.25-block minimum width. A concave facade-cutout wall can point inward on a
block boundary; only when the primary owner is unselected may exact closed-
cube containment recover one unique selected anchor, with ambiguity rejected.
This remains a documented spatial inference rather than renderer provenance.

The default mode validates the enabled S1 native cable-bus structural route,
including its byte-frozen accepted schema-9 M0--M3f view. The five historical
route-disabled modes preserve every other slice; ``--m3-completion-disabled``
selects the exact original-resource models at all 78 M3f anchors.
``--native-structural-disabled`` delegates every S1 cable bus to the accepted
schema-9 predecessor: 10 anchors remain custom (608 triangles across 14
resources), 350 are empty and all 10 legacy upgrades are empty. The physically
add-on-absent
``--stock-baseline`` is distinct: it keeps the stone control plus 38 stock M3f
machines (1,882 triangles across five resources) while all 360 appended S1
anchors and all 10 legacy upgrades are empty. All eight modes share only the
dependency-free PRBM parser and spatial-selection contract.
"""

from __future__ import annotations

import argparse
from collections import Counter
from dataclasses import dataclass, replace
import hashlib
from itertools import product
import json
import math
from pathlib import Path
import struct
import sys
from typing import Any, Iterable, Sequence
import zlib


SCHEMA_VERSION = 12
PRBM_VERSION = 1
PRBM_HEADER_FLAGS = 0x07
# AE2 19.2.17's dense straight axial caps extend 0.01/16 blocks beyond the
# owning cell. 2^-10 is larger than that 0.000625 extension while remaining
# far below the narrowest cable cross-section (0.25 blocks).
OWNERSHIP_EPSILON = 2.0**-10
SHAPE_QUANTUM = 2.0**-16
IO_CHUNK_BYTES = 1024 * 1024
MAX_JSON_BYTES = 16 * 1024 * 1024
MAX_COMPRESSED_BYTES = 128 * 1024 * 1024
MAX_DECOMPRESSED_BYTES = 256 * 1024 * 1024
GEOMETRY_TOLERANCE = 2.0**-12

DIRECTION_VECTORS = {
    "down": (0, -1, 0),
    "up": (0, 1, 0),
    "north": (0, 0, -1),
    "south": (0, 0, 1),
    "west": (-1, 0, 0),
    "east": (1, 0, 0),
}
TERMINAL_UP_DIRECTIONS = {
    "down": ("north", "west", "south", "east"),
    "up": ("north", "east", "south", "west"),
    "north": ("up", "west", "down", "east"),
    "south": ("up", "west", "down", "east"),
    "west": ("up", "south", "down", "north"),
    "east": ("up", "north", "down", "south"),
}
TERMINAL_LAYER_RESOURCES = {
    "ae2:part/terminal_bright",
    "ae2:part/terminal_medium",
    "ae2:part/terminal_dark",
}
FACADE_THICKNESS = 0.968 / 16.0
FACADE_HOLE_MIN = 2.0 / 16.0
FACADE_HOLE_MAX = 14.0 / 16.0
DRIVE_CELL_MATERIAL = "ae2:block/drive/drive_cells"
DRIVE_LED_MATERIAL = "ae2:block/drive/drive_front"
EXTENDED_DRIVE_BLOCK_ID = "extendedae:ex_drive"
EXTENDED_DRIVE_BASE_TRIANGLE_COUNT = 116
EXTENDED_DRIVE_CELL_MATERIALS = {
    "extendedae:block/drive/infinity_water_cell": "extendedae:block/drive/infinity_cell",
    "extendedae:block/drive/infinity_cobblestone_cell": "extendedae:block/drive/infinity_cell",
    "extendedae:block/drive/void_cell": "extendedae:block/drive/void_cell",
}
DRIVE_ORIENTATION_ANGLES = {
    "down": (90, 0, (0, 270, 180, 90)),
    "up": (270, 0, (180, 90, 0, 270)),
    "north": (0, 0, (0, 270, 180, 90)),
    "south": (0, 180, (0, 90, 180, 270)),
    "west": (0, 270, (0, 270, 180, 90)),
    "east": (0, 90, (0, 270, 180, 90)),
}
DRIVE_MODEL_UV_RECTS = {
    "ae2:block/drive/cells/1k_item_cell": ((0, 0, 6, 2), (6, 0, 0, 2), (6, 0, 0, 2)),
    "ae2:block/drive/cells/4k_item_cell": ((0, 2, 6, 4), (6, 2, 0, 4), (6, 2, 0, 4)),
    "ae2:block/drive/cells/16k_item_cell": ((0, 4, 6, 6), (6, 4, 0, 6), (6, 4, 0, 6)),
    "ae2:block/drive/cells/64k_item_cell": ((0, 6, 6, 8), (6, 6, 0, 8), (6, 6, 0, 8)),
    "ae2:block/drive/cells/256k_item_cell": ((0, 8, 6, 10), (6, 8, 0, 10), (6, 8, 0, 10)),
    "ae2:block/drive/cells/1k_fluid_cell": ((6, 0, 12, 2), (12, 0, 6, 2), (12, 0, 6, 2)),
    "ae2:block/drive/cells/4k_fluid_cell": ((6, 2, 12, 4), (12, 2, 6, 4), (12, 2, 6, 4)),
    "ae2:block/drive/cells/16k_fluid_cell": ((6, 4, 12, 6), (12, 4, 6, 6), (12, 4, 6, 6)),
    "ae2:block/drive/cells/64k_fluid_cell": ((6, 6, 12, 8), (12, 6, 6, 8), (12, 6, 6, 8)),
    "ae2:block/drive/cells/256k_fluid_cell": ((6, 8, 12, 10), (12, 8, 6, 10), (12, 8, 6, 10)),
    "ae2:block/drive/cells/creative_cell": ((0, 12, 6, 14), (6, 12, 0, 14), (6, 12, 0, 14)),
    "ae2:block/drive/drive_cell": ((0, 2, 6, 4), (0, 0, 6, 2), (0, 4, 6, 6)),
    "extendedae:block/drive/infinity_water_cell": (
        (0, 0, 6, 2),
        (6, 0, 0, 2),
        (6, 0, 0, 2),
    ),
    "extendedae:block/drive/infinity_cobblestone_cell": (
        (6, 0, 12, 2),
        (12, 0, 6, 2),
        (12, 0, 6, 2),
    ),
    "extendedae:block/drive/void_cell": (
        (0, 0, 6, 2),
        (6, 0, 0, 2),
        (6, 0, 0, 2),
    ),
}
DRIVE_EXPLICIT_CELL_MODELS = {
    **{
        f"ae2:{kind}_storage_cell_{capacity}": f"ae2:block/drive/cells/{capacity}_{kind}_cell"
        for kind in ("item", "fluid")
        for capacity in ("1k", "4k", "16k", "64k", "256k")
    },
    **{
        f"ae2:portable_{kind}_cell_{capacity}": f"ae2:block/drive/cells/{capacity}_{kind}_cell"
        for kind in ("item", "fluid")
        for capacity in ("1k", "4k", "16k", "64k", "256k")
    },
    "ae2:creative_storage_cell": "ae2:block/drive/cells/creative_cell",
}
DRIVE_GENERIC_CELL_MODELS = {
    "ae2:matter_cannon": "ae2:block/drive/drive_cell",
    "ae2:color_applicator": "ae2:block/drive/drive_cell",
}
EXTENDED_DRIVE_NATIVE_CELL_MODELS = {
    "extendedae:infinity_water_cell": "extendedae:block/drive/infinity_water_cell",
    "extendedae:infinity_cobblestone_cell": (
        "extendedae:block/drive/infinity_cobblestone_cell"
    ),
    "extendedae:void_cell": "extendedae:block/drive/void_cell",
}
EXTENDED_DRIVE_CELL_MODELS = {
    **DRIVE_EXPLICIT_CELL_MODELS,
    **DRIVE_GENERIC_CELL_MODELS,
    **EXTENDED_DRIVE_NATIVE_CELL_MODELS,
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
CRAFTING_RESOURCE_MANIFEST_SHA256 = "dc474ba6ce7c4c2d53778827b1c1f9b4994594ea984ed7a2cbd62c40e1bc1183"
CRAFTING_TEXTURE_MANIFEST_SHA256 = "a9a2a1ed912f562362d581cbd219b40afd4c884452a0c64cee3d015dfdc81620"
CRAFTING_MONITOR_DISPLAY_POLICY = "client-stream-only-display-omitted"
SCHEMA7_CANONICAL_SHA256 = "c60d2afff5a1f92da4972963fcb926c38093f43bb6d7f550799f104349728a38"
SCHEMA8_CANONICAL_SHA256 = "93963dd0bb60a276e1a17c6dd1f4eb916cd92bef4ef30a2e8bdc7a2bfa818b3e"
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
    "ae2:block/paint1", "ae2:block/paint2", "ae2:block/paint3",
    "ae2:block/skychest", "ae2:block/skyblockchest", "ae2:block/crank",
    "ae2:block/inscriber", "ae2:block/inscriber_inside",
    "ae2:block/spatial_pylon/base", "ae2:block/spatial_pylon/base_end",
    "ae2:block/spatial_pylon/base_spanned", "ae2:block/spatial_pylon/dim",
    "ae2:block/spatial_pylon/red", "ae2:block/spatial_pylon/red_end",
    "ae2:block/spatial_pylon/red_spanned",
)
M3_COMPLETION_STOCK_RESOURCES = (
    "ae2:block/sky_stone_block", "ae2:block/smooth_sky_stone_block",
    "ae2:block/crank", "ae2:block/inscriber",
)
M3_COMPLETION_STOCK_MATERIALS = {
    PAINT_BLOCK_ID: {},
    SKY_STONE_CHEST_BLOCK_ID: {"ae2:block/sky_stone_block": 12},
    SMOOTH_SKY_STONE_CHEST_BLOCK_ID: {"ae2:block/smooth_sky_stone_block": 12},
    CRANK_BLOCK_ID: {"ae2:block/crank": 32},
    INSCRIBER_BLOCK_ID: {"ae2:block/inscriber": 66},
    SPATIAL_PYLON_BLOCK_ID: {},
}
SCHEMA9_CANONICAL_SHA256 = "75e6ba2f40631a95f20cfa00d7ca952e521bc2c7a4eb155926334a223a945f3a"
SCHEMA10_CANONICAL_SHA256 = "389a9b2b82dd16e3f4af82f9836e593770e404995a153218937908528c17dcee"
SCHEMA11_CANONICAL_SHA256 = "914dab6931077521959cf59260a1ffb0cdbe105385f43880763b289f8117ec55"
SCHEMA12_CANONICAL_SHA256 = "f73959670b6490c27b9b89e61e486f06750fb15e2a74c5cc36047c5e81b77483"
APPMEK_DRIVE_ROUTE = "appmek-drive-cells"
APPMEK_DRIVE_TEXTURE = "appmek:block/drive/drive_cells"
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
M45_RUNTIME_ORACLE_PATH = (
    Path(__file__).resolve().parents[1] / "gallery/m45-runtime-oracle.json"
)
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
M45_SCHEMA10_LEGACY_ORACLE_PATH = (
    Path(__file__).resolve().parents[1]
    / "gallery/m45-schema10-legacy-oracle.json"
)
M45_SCHEMA10_LEGACY_ORACLE_SIZE_BYTES = 2_336
M45_SCHEMA10_LEGACY_ORACLE_SHA256 = (
    "2319ecf576ba07b123078c720d941990fac939033d375e5853f51bf98348c3c7"
)
M45_SCHEMA10_LEGACY_ORACLE_ANCHOR_COUNT = 3
M45_SCHEMA10_LEGACY_ORACLE_TRIANGLE_COUNT = 282
M45_SCHEMA10_LEGACY_ORACLE_IDENTITY_COUNT = 20
M45_SCHEMA10_LEGACY_ORACLE_MATERIAL_ROW_COUNT = 26
M45_EXTENDED_MATRIX_RESOURCES = (
    "extendedae:block/assembler_matrix/crafter_core",
    "extendedae:block/assembler_matrix/frame_block_off",
    "extendedae:block/assembler_matrix/frame_block_on",
    "extendedae:block/assembler_matrix/frame_column_off",
    "extendedae:block/assembler_matrix/frame_column_on",
    "extendedae:block/assembler_matrix/glass/face_a",
    "extendedae:block/assembler_matrix/glass/face_b",
    "extendedae:block/assembler_matrix/glass/face_c",
    "extendedae:block/assembler_matrix/glass/full",
    "extendedae:block/assembler_matrix/glass/sides",
    "extendedae:block/assembler_matrix/pattern_core",
    "extendedae:block/assembler_matrix/speed_core",
    "extendedae:block/assembler_matrix/wall_block",
)
M45_EXTENDED_PLANE_RESOURCES = (
    "extendedae:part/active_formation_plane",
    "extendedae:part/active_formation_plane_on",
    "extendedae:part/smart_annihilation_plane",
    "extendedae:part/smart_annihilation_plane_on",
)
M45_EXTENDED_PLANE_DEPENDENCY_RESOURCES = (
    "ae2:part/plane_sides",
    "ae2:part/transition_plane_back",
)
M45_NATIVE_CENTER_PROJECTION_RESOURCES = (
    "ae2:part/cable/core/covered/transparent",
    "ae2:part/cable/covered/transparent",
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
    (
        "ae2-m3-14",
        (241, 100, 249),
        ("megacells",),
        (),
        "stock-fallback-m3",
        "unsupported-drive-cell-id",
        "ae2-drive-megacells-cell",
        {
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
    ),
    (
        "ae2-m3b-13",
        (266, 100, 266),
        ("megacells",),
        ("extension",),
        "stock-fallback-m3b",
        "unsupported-drive-cell-id",
        "extended-drive-megacells-cell",
        {
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
    ),
    (
        "ae2-m3d-09",
        (318, 100, 261),
        ("expandedae", "megacells"),
        ("crafting",),
        "stock-fallback-m3d",
        "compatible-extension-crafting-neighbor",
        "native-crafting-expanded-mega-peer-connection",
        {
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
    ),
)
M45_LEGACY_UPGRADE_REGRESSION_POSITIONS = {
    "m2": (),
    "m3a": ((241, 100, 249),),
    "m3b": ((241, 100, 249), (266, 100, 266)),
    "schema6": ((241, 100, 249), (266, 100, 266)),
    "schema7": (
        (241, 100, 249),
        (266, 100, 266),
        (318, 100, 261),
    ),
    "schema8": (
        (241, 100, 249),
        (266, 100, 266),
        (318, 100, 261),
    ),
}
NATIVE_STRUCTURAL_ROUTE = "ae2-cable-bus-structural"
NATIVE_STRUCTURAL_COVERAGE = "s1-native-structural"
NATIVE_STRUCTURAL_RESOURCE_MANIFEST_SHA256 = (
    "ae89e4fc3356503cc76ea92ac9cb11ade296551c9cca85cd583ffddbbe35bd76"
)
NATIVE_STRUCTURAL_TEXTURE_RESOURCE_LIST_SHA256 = (
    "e57547e12bfe4e8ecb61d83e26ca5ef63811324f973794c511196b10113517e5"
)
NATIVE_STRUCTURAL_PART_IDS = (
    "quartz_fiber", "toggle_bus", "inverted_toggle_bus", "cable_anchor",
    "monitor", "semi_dark_monitor", "dark_monitor", "storage_bus",
    "import_bus", "export_bus", "level_emitter", "energy_level_emitter",
    "annihilation_plane", "formation_plane", "pattern_encoding_terminal",
    "crafting_terminal", "terminal", "storage_monitor", "conversion_monitor",
    "cable_pattern_provider", "cable_interface", "pattern_access_terminal",
    "cable_energy_acceptor", "me_p2p_tunnel", "redstone_p2p_tunnel",
    "item_p2p_tunnel", "fluid_p2p_tunnel", "fe_p2p_tunnel",
    "light_p2p_tunnel",
)
NATIVE_STRUCTURAL_PART_GROUPS = dict(
    zip(
        NATIVE_STRUCTURAL_PART_IDS,
        (
            "network", "redstone", "redstone", "structural",
            "panel", "panel", "panel", "bus", "bus", "bus",
            "emitter", "emitter", "plane", "plane", "terminal",
            "terminal", "terminal", "monitor", "monitor", "service",
            "service", "terminal", "network", "p2p", "p2p", "p2p",
            "p2p", "p2p", "p2p",
        ),
        strict=True,
    )
)
NATIVE_STRUCTURAL_SPIN_PART_IDS = frozenset(
    {
        "monitor", "semi_dark_monitor", "dark_monitor",
        "pattern_encoding_terminal", "crafting_terminal", "terminal",
        "storage_monitor", "conversion_monitor", "pattern_access_terminal",
    }
)
NATIVE_STRUCTURAL_PLANE_PART_IDS = ("annihilation_plane", "formation_plane")
NATIVE_STRUCTURAL_P2P_PART_IDS = (
    "me_p2p_tunnel", "redstone_p2p_tunnel", "item_p2p_tunnel",
    "fluid_p2p_tunnel", "fe_p2p_tunnel", "light_p2p_tunnel",
)
NATIVE_STRUCTURAL_ENDPOINTS = {
    "covered": (
        "inscriber", "charger", "energy_acceptor", "vibration_chamber",
        "growth_accelerator", "energy_cell", "dense_energy_cell",
        "creative_energy_cell", "molecular_assembler",
    ),
    "dense_smart": ("quantum_ring", "quantum_link", "controller"),
    "smart": (
        "wireless_access_point", "spatial_pylon", "spatial_io_port",
        "spatial_anchor", "drive", "chest", "interface", "io_port",
        "crystal_resonance_generator", "crafting_unit", "crafting_accelerator",
        "1k_crafting_storage", "4k_crafting_storage", "16k_crafting_storage",
        "64k_crafting_storage", "256k_crafting_storage", "crafting_monitor",
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
# Source-audited AE2 19.2.17 endpoint projection.  This table is intentionally
# independent of the generated profile and runtime-exported regression oracle:
# schema-10 must prove that every representative fixture carries the exact
# persisted BlockState, serialized block-entity identity and observed-side
# rule needed by the real grid-node host.
NATIVE_STRUCTURAL_ENDPOINT_POLICIES = {
    "inscriber": ("ae2:inscriber", {"facing": "east", "spin": 0, "waterlogged": False}, "NO_FRONT", None, True),
    "wireless_access_point": ("ae2:wireless_access_point", {"facing": "east", "state": "off", "waterlogged": False}, "BACK", None, True),
    "charger": ("ae2:charger", {"facing": "east", "spin": 0}, "NO_FRONT", None, True),
    "quantum_ring": ("ae2:quantum_ring", {"formed": True, "waterlogged": False}, "FORMED_QUANTUM", "qnb-yz-edge-ring", True),
    "quantum_link": ("ae2:quantum_ring", {"formed": True, "waterlogged": False}, "FORMED_QUANTUM", "qnb-yz-center-link", False),
    "spatial_pylon": ("ae2:spatial_pylon", {"powered_on": False}, "VALID_STRAIGHT_PYLON", "vertical-three-pylon-middle", True),
    "spatial_io_port": ("ae2:spatial_io_port", {"facing": "north", "powered": False, "spin": 0}, "ALL", None, True),
    "spatial_anchor": ("ae2:spatial_anchor", {"facing": "north", "powered": False}, "ALL", None, True),
    "controller": ("ae2:controller", {"state": "offline", "type": "block"}, "ALL", None, True),
    "drive": ("ae2:drive", {"facing": "east", "spin": 0}, "NO_FRONT", None, True),
    "chest": ("ae2:chest", {"facing": "north", "lights_on": False, "spin": 0}, "ALL", None, True),
    "interface": ("ae2:interface", {}, "ALL", None, True),
    "io_port": ("ae2:io_port", {"facing": "north", "powered": False, "spin": 0}, "ALL", None, True),
    "energy_acceptor": ("ae2:energy_acceptor", {}, "ALL", None, True),
    "crystal_resonance_generator": ("ae2:crystal_resonance_generator", {"facing": "east", "waterlogged": False}, "BACK", None, True),
    "vibration_chamber": ("ae2:vibration_chamber", {"active": False, "facing": "north", "spin": 0}, "ALL", None, True),
    "growth_accelerator": ("ae2:growth_accelerator", {"facing": "east", "powered": False}, "FRONT_BACK", None, True),
    "energy_cell": ("ae2:energy_cell", {"fullness": 0}, "ALL", None, True),
    "dense_energy_cell": ("ae2:dense_energy_cell", {"fullness": 0}, "ALL", None, True),
    "creative_energy_cell": ("ae2:creative_energy_cell", {}, "ALL", None, True),
    "crafting_unit": ("ae2:crafting_unit", {"formed": True, "powered": False}, "FORMED_CRAFTING", "vertical-crafting-pair", True),
    "crafting_accelerator": ("ae2:crafting_unit", {"formed": True, "powered": False}, "FORMED_CRAFTING", "vertical-crafting-pair", True),
    "1k_crafting_storage": ("ae2:crafting_storage", {"formed": True, "powered": False}, "FORMED_CRAFTING", "single-storage-crafting-cpu", True),
    "4k_crafting_storage": ("ae2:crafting_storage", {"formed": True, "powered": False}, "FORMED_CRAFTING", "vertical-crafting-pair", True),
    "16k_crafting_storage": ("ae2:crafting_storage", {"formed": True, "powered": False}, "FORMED_CRAFTING", "vertical-crafting-pair", True),
    "64k_crafting_storage": ("ae2:crafting_storage", {"formed": True, "powered": False}, "FORMED_CRAFTING", "vertical-crafting-pair", True),
    "256k_crafting_storage": ("ae2:crafting_storage", {"formed": True, "powered": False}, "FORMED_CRAFTING", "vertical-crafting-pair", True),
    "crafting_monitor": ("ae2:crafting_monitor", {"facing": "east", "formed": True, "powered": False, "spin": 0}, "FORMED_CRAFTING", "vertical-crafting-pair", True),
    "pattern_provider": ("ae2:pattern_provider", {"push_direction": "east"}, "PUSH_DIRECTION", None, True),
    "molecular_assembler": ("ae2:molecular_assembler", {"powered": False}, "ALL", None, True),
}
NATIVE_STRUCTURAL_DUAL_ENDPOINTS = {
    "controller": ("dense_smart", True, None),
    "wireless_access_point": ("smart", True, None),
    "energy_acceptor": ("covered", True, None),
    "molecular_assembler": ("covered", False, "ae2:terminal"),
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
    ("ae2:quartz_glass", {}, "facade-aware-connected-quartz-glass-static", "ae2:block/quartz_glass", "9c331aa0f423a364e136b731195caf168df6496a90a065f9699e5e8e37e70d50"),
    ("ae2:quartz_vibrant_glass", {}, "facade-aware-connected-quartz-glass-static", "ae2:block/quartz_glass", "e3b2b20544e578ff4b9d908ca1e7d281ecc46ddd8f0ee496ad53e2e344e17a99"),
    ("ae2:controller", {"state": "offline", "type": "block"}, "controller-offline-block", "ae2:block/controller/controller_block_offline", "693d04c733b47e4159052d0843256fa7520bbc1984b6d9e454bec976a73d2ca8"),
    ("ae2:1k_crafting_storage", {"formed": "false", "powered": "false"}, "crafting-storage-1k-unformed", "ae2:block/crafting/1k_storage", "9a1f6383cd3b54a8361cefc46740ddbee587ce79baefccb6ad6de6355833a603"),
    ("ae2:4k_crafting_storage", {"formed": "false", "powered": "false"}, "crafting-storage-4k-unformed", "ae2:block/crafting/4k_storage", "dd4210a4c0fc5b0eb7f524571f20b7e1a92c438bc68df7324cb26c939c726abc"),
    ("ae2:16k_crafting_storage", {"formed": "false", "powered": "false"}, "crafting-storage-16k-unformed", "ae2:block/crafting/16k_storage", "8e04febb39f74e1bb1061f9fee979be9cc4923bf14cc5a5d619cf6e681d506a4"),
    ("ae2:64k_crafting_storage", {"formed": "false", "powered": "false"}, "crafting-storage-64k-unformed", "ae2:block/crafting/64k_storage", "d8a1b0f2f21c2f05cd959f03213d0434c6bb41e27d5591d0c3c532aea142eb7f"),
    ("ae2:256k_crafting_storage", {"formed": "false", "powered": "false"}, "crafting-storage-256k-unformed", "ae2:block/crafting/256k_storage", "3458c6e521a76f7a0761c7efe956cc587826cfdd40d1f7c6284100990fb68905"),
    ("ae2:crafting_monitor", {"facing": "north", "formed": "false", "powered": "false", "spin": "0"}, "crafting-monitor-unformed-north", "ae2:block/crafting/monitor", "157e2a326b835180b369874b5f6978fab7c6796293945f85a971ac3f5b1cf2b7"),
    ("ae2:crafting_unit", {"formed": "false", "powered": "false"}, "crafting-unit-unformed", "ae2:block/crafting/unit", "b33f03d38953281265d6196e2a9f2494974275901b570f390ebf40fa3a338ece"),
    ("ae2:crafting_accelerator", {"formed": "false", "powered": "false"}, "crafting-accelerator-unformed", "ae2:block/crafting/accelerator", "f2b8fd7efa88b37968f55d8169eee48d84c1c673b5b2201719037771d5e18918"),
)
NATIVE_STRUCTURAL_PHYSICAL_FACADE_WHITELIST_IDS = (
    *(entry[0] for entry in NATIVE_STRUCTURAL_NEUTRAL_FACADE_MATERIALS),
    "minecraft:chiseled_bookshelf",
    "minecraft:furnace",
    "minecraft:soul_sand",
    "minecraft:honey_block",
)
NATIVE_STRUCTURAL_FACADE_WHITELIST_NEUTRAL_STATES = {
    **{
        entry[0]: entry[1]
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
_NATIVE_STRUCTURAL_FACADE_BOOLEAN_DOMAIN = ["false", "true"]
_NATIVE_STRUCTURAL_FACADE_FACING_DOMAIN = [
    "down",
    "up",
    "north",
    "south",
    "west",
    "east",
]
_NATIVE_STRUCTURAL_FACADE_HORIZONTAL_FACING_DOMAIN = [
    "north",
    "south",
    "west",
    "east",
]
_NATIVE_STRUCTURAL_FACADE_FORMED_POWERED_SCHEMA = {
    "formed": _NATIVE_STRUCTURAL_FACADE_BOOLEAN_DOMAIN,
    "powered": _NATIVE_STRUCTURAL_FACADE_BOOLEAN_DOMAIN,
}
NATIVE_STRUCTURAL_FACADE_WHITELIST_STATE_SCHEMAS = {
    "ae2:quartz_glass": {},
    "ae2:quartz_vibrant_glass": {},
    "minecraft:chiseled_bookshelf": {
        "facing": _NATIVE_STRUCTURAL_FACADE_HORIZONTAL_FACING_DOMAIN,
        **{
            f"slot_{slot}_occupied": _NATIVE_STRUCTURAL_FACADE_BOOLEAN_DOMAIN
            for slot in range(6)
        },
    },
    "minecraft:jukebox": {
        "has_record": _NATIVE_STRUCTURAL_FACADE_BOOLEAN_DOMAIN
    },
    "minecraft:furnace": {
        "facing": _NATIVE_STRUCTURAL_FACADE_HORIZONTAL_FACING_DOMAIN,
        "lit": _NATIVE_STRUCTURAL_FACADE_BOOLEAN_DOMAIN,
    },
    "minecraft:blast_furnace": {
        "facing": _NATIVE_STRUCTURAL_FACADE_HORIZONTAL_FACING_DOMAIN,
        "lit": _NATIVE_STRUCTURAL_FACADE_BOOLEAN_DOMAIN,
    },
    "minecraft:dropper": {
        "facing": _NATIVE_STRUCTURAL_FACADE_FACING_DOMAIN,
        "triggered": _NATIVE_STRUCTURAL_FACADE_BOOLEAN_DOMAIN,
    },
    "minecraft:dispenser": {
        "facing": _NATIVE_STRUCTURAL_FACADE_FACING_DOMAIN,
        "triggered": _NATIVE_STRUCTURAL_FACADE_BOOLEAN_DOMAIN,
    },
    "minecraft:crafter": {
        "crafting": _NATIVE_STRUCTURAL_FACADE_BOOLEAN_DOMAIN,
        "orientation": [
            "down_east",
            "down_north",
            "down_south",
            "down_west",
            "up_east",
            "up_north",
            "up_south",
            "up_west",
            "west_up",
            "east_up",
            "north_up",
            "south_up",
        ],
        "triggered": _NATIVE_STRUCTURAL_FACADE_BOOLEAN_DOMAIN,
    },
    "minecraft:barrel": {
        "facing": _NATIVE_STRUCTURAL_FACADE_FACING_DOMAIN,
        "open": _NATIVE_STRUCTURAL_FACADE_BOOLEAN_DOMAIN,
    },
    "minecraft:bee_nest": {
        "facing": _NATIVE_STRUCTURAL_FACADE_HORIZONTAL_FACING_DOMAIN,
        "honey_level": ["0", "1", "2", "3", "4", "5"],
    },
    "minecraft:beehive": {
        "facing": _NATIVE_STRUCTURAL_FACADE_HORIZONTAL_FACING_DOMAIN,
        "honey_level": ["0", "1", "2", "3", "4", "5"],
    },
    "minecraft:sculk_catalyst": {
        "bloom": _NATIVE_STRUCTURAL_FACADE_BOOLEAN_DOMAIN
    },
    "minecraft:soul_sand": {},
    "minecraft:honey_block": {},
    "ae2:controller": {
        "state": ["offline", "online", "conflicted"],
        "type": [
            "block",
            "column_x",
            "column_y",
            "column_z",
            "inside_a",
            "inside_b",
        ],
    },
    **{
        f"ae2:{name}": _NATIVE_STRUCTURAL_FACADE_FORMED_POWERED_SCHEMA
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
        "facing": _NATIVE_STRUCTURAL_FACADE_FACING_DOMAIN,
        "formed": _NATIVE_STRUCTURAL_FACADE_BOOLEAN_DOMAIN,
        "powered": _NATIVE_STRUCTURAL_FACADE_BOOLEAN_DOMAIN,
        "spin": ["0", "1", "2", "3"],
    },
}
NATIVE_STRUCTURAL_FACADE_WHITELIST_BLOCKSTATE_SHA256 = {
    **{
        entry[0]: entry[4]
        for entry in NATIVE_STRUCTURAL_NEUTRAL_FACADE_MATERIALS
    },
    "minecraft:chiseled_bookshelf": "7f3f363d1e155d92d08916d8f08de670e269ae4a05fce0844c8bcd6930e8d098",
    "minecraft:jukebox": "8002563a048d4a5afb22d44692ca1a38e114ef95a3ccd24f02b9e0fd02b693d5",
    "minecraft:furnace": "aedb43571027a5dea15ba9cbfc05f0327af3048de70b72c3cd67c851839bb284",
    "minecraft:blast_furnace": "265ec5f30fa65bdaff6867bbad8de73e0a1b21ea12a33da5b771f889e4ac7dcc",
    "minecraft:dropper": "c763060c1946a3031cdf6e68ab98db7d81d83c364b0ad4da54fdf055225753c3",
    "minecraft:dispenser": "fc1ba39eb47f31285b5d1c9f729fabf5ad9832d8b8f6b1f510d3c870f6e6bfd8",
    "minecraft:crafter": "dfa8af74cd96d1d6f2086a63fab3402864497d7f436ba469b34be58924f1edfa",
    "minecraft:barrel": "d8e00576b5f85f83a42b7b31dc177e0add02cf2204fa441c0fe31cbd6d70dcca",
    "minecraft:bee_nest": "09ee024cc05e40767c3e88776e336396a71b146a7fb93f64ba1860aa6a107853",
    "minecraft:beehive": "c4c438bb21bf78f5bdc8835daf852c2f2040e3f96872fe4488d2710a6abfc8ae",
    "minecraft:sculk_catalyst": "0e6c7b956647211dea0d7ce46e9e111296dd985c51d58a3c094630850b764504",
    "minecraft:soul_sand": "6a0ea83a331843c30e21f8d7ea9252c429c4093b6222b60e62e8ab47ca802ef8",
    "minecraft:honey_block": "780ffcffff91d90efe172f2f1f200a06dcbe885fe5316d16bebf72bae2ef7c44",
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
NATIVE_STRUCTURAL_GLASSENTIAL_MATERIAL = (
    NATIVE_STRUCTURAL_GLASSENTIAL_FULL_PACK_OVERRIDE["resolved_material"]
)
NATIVE_STRUCTURAL_GLASSENTIAL_FACADE_TRIANGLES = {
    (233, 100, 353): 12,
    (236, 100, 353): 10,
    (239, 100, 353): 12,
    (242, 100, 353): 12,
    (245, 100, 353): 12,
    (248, 100, 353): 12,
    (278, 100, 353): 12,
    (311, 100, 333): 46,
}
NATIVE_STRUCTURAL_GLASSENTIAL_FACADE_TRIANGLE_COUNT = 128
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
NATIVE_STRUCTURAL_FACADE_AO_DIRECTION_POLICY = (
    "BlueMap-ResourceModelRenderer-source-faceDir-rotated-by-blockstate-variant-only;"
    "element-rotation-affects-vertices-not-AO-direction;runtime-uses-layer-lightFace-"
    "not-quad-nominal-face"
)
NATIVE_STRUCTURAL_MAP_COLOR_ILLUMINATION_POLICY = (
    "BlueMap-map-color-illumination-uses-original-center-and-outward-world-light-only;"
    "element-lightEmission-affects-triangle-blocklight-not-map-color-brightness"
)
NATIVE_STRUCTURAL_EMITTER_PART_IDS = frozenset(
    {"level_emitter", "energy_level_emitter"}
)
NATIVE_STRUCTURAL_FORCED_FULLBRIGHT_RESOURCES = frozenset(
    {
        "ae2:part/cable/smart/channels_00",
        "ae2:part/cable/smart/channels_10",
        "ae2:part/cable/dense_smart/channels_00",
        "ae2:part/cable/dense_smart/channels_10",
    }
)
NATIVE_STRUCTURAL_WORLD_LIGHT_POLICY = (
    "environment-derived-own-and-outward-face-maximum-excluded-from-native-invariant"
)
NATIVE_STRUCTURAL_LIGHT_VALIDATION = "per-triangle-flat-range-0..15"
NATIVE_STRUCTURAL_FACADE_THICKNESS = 1.0 / 16.0 - 0.002
NATIVE_STRUCTURAL_CORNER_KICKER_EPSILON = 0.00001
NATIVE_STRUCTURAL_PLANE_MASK_BITS = {"up": 8, "right": 4, "down": 2, "left": 1}
NATIVE_STRUCTURAL_ORIENTATION_STATE_COUNT = 336
NATIVE_STRUCTURAL_CASE_COUNT = 28
NATIVE_STRUCTURAL_ANCHOR_COUNT = 360
# The raw/source-projected matrix is frozen independently.  The subordinate
# rendered regression identity remains fail-closed until the runtime export.
NATIVE_STRUCTURAL_RAW_LOGICAL_MATRIX_SHA256 = (
    "b797930fc3f8eca822d0cbc674a4cc264671382db94d7627d9ba991c5d71fae8"
)
NATIVE_STRUCTURAL_RAW_MATRIX_SIZE_BYTES = 291_087
NATIVE_STRUCTURAL_RAW_STRIPPED_LOGICAL_MATRIX_SIZE_BYTES = 290_835
NATIVE_STRUCTURAL_RAW_STRIPPED_LOGICAL_MATRIX_SHA256 = (
    "332a33356ab887c31b8973e29ce0146b8cf900a0bd4b797b2ace2c82775c9540"
)
NATIVE_STRUCTURAL_ORACLE_SIZE_BYTES = 198_162
NATIVE_STRUCTURAL_ORACLE_SHA256 = (
    "ac9a54cee9a20be18e71d6c9fe4f16b894827d43bb49cb4d0e56c673280cec39"
)
NATIVE_STRUCTURAL_ORACLE_ANCHOR_COUNT = 351
NATIVE_STRUCTURAL_ORACLE_TRIANGLE_COUNT = 37_518
NATIVE_STRUCTURAL_ORACLE_IDENTITY_COUNT = 96
NATIVE_STRUCTURAL_ORACLE_MATERIAL_ROW_COUNT = 2_093
NATIVE_STRUCTURAL_LEGACY_COVERAGE = "s1-native-structural-legacy-upgrades"
NATIVE_STRUCTURAL_LEGACY_INPUT_PATH = (
    Path(__file__).resolve().parents[1]
    / "gallery/native-structural-legacy-input.json"
)
NATIVE_STRUCTURAL_LEGACY_INPUT_SIZE_BYTES = 22_189
NATIVE_STRUCTURAL_LEGACY_INPUT_SHA256 = (
    "6a578463bbacb8267e7bff82bf76708d2b2950a7e9b052b53131626a331245de"
)
NATIVE_STRUCTURAL_LEGACY_ORACLE_PATH = (
    Path(__file__).resolve().parents[1]
    / "gallery/native-structural-legacy-oracle.json"
)
NATIVE_STRUCTURAL_LEGACY_ORACLE_SIZE_BYTES = 6_155
NATIVE_STRUCTURAL_LEGACY_ORACLE_SHA256 = (
    "cf0d86c440d1f89fc13f2b131f4f1534fb42363ebdc92580af826058297eb3d0"
)
NATIVE_STRUCTURAL_LEGACY_SCHEMA9_CASES_SIZE_BYTES = 3_314_082
NATIVE_STRUCTURAL_LEGACY_SCHEMA9_GALLERY_SIZE_BYTES = 49_679
NATIVE_STRUCTURAL_LEGACY_SCHEMA9_GALLERY_SHA256 = (
    "21ceec072cc3263a41bdb81874e897d48d5a1ce5e1c7d3ac3c0de3063818ee6c"
)
NATIVE_STRUCTURAL_LEGACY_CASE_COUNT = 10
NATIVE_STRUCTURAL_LEGACY_ANCHOR_COUNT = 10
NATIVE_STRUCTURAL_LEGACY_TRIANGLE_COUNT = 840
NATIVE_STRUCTURAL_LEGACY_IDENTITY_COUNT = 21
NATIVE_STRUCTURAL_LEGACY_MATERIAL_ROW_COUNT = 70
NATIVE_STRUCTURAL_LEGACY_FIXTURE_BLOCK_COUNT = 92
NATIVE_STRUCTURAL_LEGACY_SELECTORS = (
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
NATIVE_STRUCTURAL_RENDERED_LOGICAL_MATRIX_SHA256 = (
    "b5e59406b457ac7facf5fe08bbcb5cb456efbbadbbfd2fc12a435b29ae69cdc0"
)
NATIVE_STRUCTURAL_PROFILE_SIZE_BYTES = 117_013
NATIVE_STRUCTURAL_PROFILE_SHA256 = (
    "f6fa515b4e17205a019d57f253d5e71017ea20e75b8f0c333aa587afd0d0f353"
)
NATIVE_STRUCTURAL_SUPPORT_MATRIX_SIZE_BYTES = 24_084
NATIVE_STRUCTURAL_SUPPORT_MATRIX_SHA256 = (
    "d77a589dd162e4f7d37113dc40dff3eca69e6042291b53133eabe9549dae643a"
)
NATIVE_STRUCTURAL_PROVENANCE_SIZE_BYTES = 112_852
NATIVE_STRUCTURAL_PROVENANCE_SHA256 = (
    "5cea0a302297a00e4fe0bf246695c6e79f646356c87a44d396ceb365d9a249c5"
)
NATIVE_STRUCTURAL_ENDPOINT_STATE_CONTRACT_SHA256 = (
    "93ad41cf224e0ab07b64ffe91381e9e70b76f14fb4f4f83e17acb101e4dfc3ae"
)
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
NATIVE_STRUCTURAL_SCHEMA9_DISABLED_EXPECTATIONS = {
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
NATIVE_STRUCTURAL_SCHEMA9_DISABLED_RENDERED_ANCHOR_COUNT = 10
NATIVE_STRUCTURAL_SCHEMA9_DISABLED_EMPTY_ANCHOR_COUNT = 350
NATIVE_STRUCTURAL_SCHEMA9_DISABLED_TRIANGLE_COUNT = 608
NATIVE_STRUCTURAL_SCHEMA9_DISABLED_RESOURCE_COUNT = 14
NATIVE_STRUCTURAL_SCHEMA9_DISABLED_RESOURCES = (
    "ae2:part/cable/core/covered/transparent",
    "ae2:part/cable/core/dense_smart/transparent",
    "ae2:part/cable/core/glass/transparent",
    "ae2:part/cable/covered/transparent",
    "ae2:part/cable/glass/transparent",
    "ae2:part/monitor_back",
    "ae2:part/monitor_front",
    "ae2:part/monitor_sides",
    "ae2:part/monitor_sides_status",
    "ae2:part/monitor_sides_status_off",
    "ae2:part/terminal_bright",
    "ae2:part/terminal_dark",
    "ae2:part/terminal_medium",
    "minecraft:block/stone",
)
NATIVE_STRUCTURAL_UNIT_ONLY_MALFORMED_CASES = (
    "native-non-anchor-part-on-dense-cable",
    "native-unknown-face-part-id",
    "native-p2p-missing-frequency",
    "native-malformed-facade-state",
    "native-facade-without-center",
    "native-retained-field-budget-exceeded",
)
NATIVE_STRUCTURAL_UNIT_ONLY_REASON = (
    "AE2 sanitizes these malformed fields during live block-entity loading; "
    "their S1 atomic-fallback boundary is frozen by exact Java decoder and "
    "bounded DTO tests. Physical S1 fallback evidence instead uses persistent "
    "invalid reporting-part spins, one non-full-cube facade, and one exact "
    "known-compatible extension host."
)
NATIVE_STRUCTURAL_SCHEMA9_TERMINAL_MATERIALS = {
    "ae2:part/monitor_sides": 8,
    "ae2:part/monitor_sides_status": 8,
    "ae2:part/monitor_back": 4,
    "ae2:part/monitor_front": 2,
    "ae2:part/monitor_sides_status_off": 16,
    "ae2:part/terminal_bright": 2,
    "ae2:part/terminal_medium": 2,
    "ae2:part/terminal_dark": 2,
}
M3F_CASES_CANONICAL_SHA256 = "096432c6034697b40ce725a11c2bd9b2d462d5f8cc0763efae3cfc184e27c1bb"
M3_COMPLETION_PROFILE_SHA256 = "281a335d3024ebbb97c6268e768826c467d6f7ea660989fd3dae204c6c03abf3"
M3F_MODEL_FACE_ORDER = ("down", "up", "north", "south", "west", "east")
# Byte-for-byte AE2 19.2.17 JSON-model projections. Each face row is
# (direction, (u1, v1, u2, v2), clockwise quarter-turn degrees).
M3F_CRANK_MODEL = (
    (
        (6, 6, 13), (10, 10, 16), "base",
        (
            ("north", (3, 0, 7, 4), 180),
            ("east", (0, 4, 3, 8), 0),
            ("south", (7, 0, 11, 4), 0),
            ("west", (0, 4, 3, 0), 180),
            ("up", (0, 8, 3, 4), 270),
            ("down", (0, 0, 3, 4), 90),
        ),
    ),
    (
        (7, 7, 7), (9, 9, 13), "shaft",
        (
            ("east", (2, 8, 0, 14), 90),
            ("south", (12, 4, 14, 6), 0),
            ("west", (6, 8, 4, 14), 270),
            ("up", (4, 8, 2, 14), 0),
            ("down", (8, 8, 6, 14), 180),
        ),
    ),
    (
        (7, 7, 5), (14, 9, 7), "handle",
        (
            ("north", (3, 6, 10, 8), 180),
            ("east", (10, 4, 12, 6), 90),
            ("south", (8, 10, 15, 12), 0),
            ("west", (10, 6, 12, 8), 270),
            ("up", (3, 4, 10, 6), 0),
            ("down", (8, 8, 15, 10), 180),
        ),
    ),
)
M3F_INSCRIBER_MODEL = (
    (
        (0, 12, 0), (16, 16, 16), "top",
        (
            ("north", (0, 4, 4, 5), 0), ("east", (0, 4, 4, 5), 0),
            ("south", (0, 4, 4, 5), 0), ("west", (4, 4, 0, 5), 0),
            ("up", (0, 0, 4, 4), 270), ("down", (8, 4, 12, 8), 270),
        ),
    ),
    (
        (0, 0, 0), (16, 4, 16), "bottom",
        (
            ("north", (0, 5, 4, 6), 0), ("east", (0, 5, 4, 6), 0),
            ("south", (0, 5, 4, 6), 0), ("west", (0, 5, 4, 6), 0),
            ("up", (8, 0, 4, 4), 270), ("down", (8, 0, 12, 4), 270),
        ),
    ),
    (
        (0, 4, 2), (2, 12, 14), "west-frame",
        (
            ("north", (4.5, 4, 4, 6), 0), ("east", (4, 6, 7, 8), 0),
            ("south", (7, 4, 6.5, 6), 0), ("west", (4, 4, 7, 6), 0),
        ),
    ),
    (
        (14, 4, 2), (16, 12, 14), "east-frame",
        (
            ("north", (7, 4, 6.5, 6), 0), ("east", (4, 4, 7, 6), 0),
            ("south", (4.5, 4, 4, 6), 0), ("west", (7, 8, 4, 6), 180),
        ),
    ),
    (
        (13, 4, 7), (14, 12, 9), "east-guide",
        (
            ("north", (2.75, 6, 3, 8), 180),
            ("south", (2.5, 6, 2.75, 8), 180),
            ("west", (2.5, 6, 3, 8), 180),
        ),
    ),
    (
        (2, 4, 7), (3, 12, 9), "west-guide",
        (
            ("north", (3, 6, 2.75, 8), 180),
            ("east", (3, 6, 2.5, 8), 180),
            ("south", (2.75, 6, 2.5, 8), 180),
        ),
    ),
    (
        (3, 4, 14), (13, 12, 16), "back",
        (
            ("north", (4.25, 6, 6.75, 8), 0),
            ("east", (2.5, 6, 2, 8), 0),
            ("south", (0, 6, 2.5, 8), 0),
            ("west", (0.5, 6, 0, 8), 0),
        ),
    ),
    (
        (7, 4, 13), (9, 12, 14), "back-guide",
        (
            ("north", (2.5, 6, 3, 8), 0),
            ("east", (2.5, 6, 2.75, 8), 0),
            ("west", (2.75, 6, 3, 8), 0),
        ),
    ),
)
CRAFTING_OPPOSITES = {
    "down": "up", "up": "down", "north": "south", "south": "north",
    "west": "east", "east": "west",
}
CRAFTING_DIRECTION_AXES = {
    "down": "y", "up": "y", "north": "z", "south": "z",
    "west": "x", "east": "x",
}
CRAFTING_CORNER_DIRECTIONS = (
    ("up", "east", "north"), ("up", "east", "south"),
    ("up", "west", "north"), ("up", "west", "south"),
    ("down", "east", "north"), ("down", "east", "south"),
    ("down", "west", "north"), ("down", "west", "south"),
)
CRAFTING_LIGHT_RESOURCE_BY_KIND = {
    "accelerator": "ae2:block/crafting/accelerator_light",
    "1k_storage": "ae2:block/crafting/1k_storage_light",
    "4k_storage": "ae2:block/crafting/4k_storage_light",
    "16k_storage": "ae2:block/crafting/16k_storage_light",
    "64k_storage": "ae2:block/crafting/64k_storage_light",
    "256k_storage": "ae2:block/crafting/256k_storage_light",
}
CRAFTING_PAINT_NAMES = (
    "white", "light_gray", "gray", "black", "lime", "yellow", "orange",
    "brown", "red", "pink", "magenta", "purple", "blue", "light_blue",
    "cyan", "green", "fluix",
)
CRAFTING_PAINT_DARK = (
    0xB4B4B4, 0x7E7E7E, 0x4F4F4F, 0x131313, 0x4EC04E, 0xFFCF40,
    0xD9782F, 0x6E4A12, 0xAA212B, 0xD86EAA, 0xC15189, 0x6E5CB8,
    0x337FF0, 0x69B9FF, 0x22B0AE, 0x079B6B, 0x5A479E,
)
CRAFTING_PAINT_MEDIUM = (
    0xE0E0E0, 0xA09FA0, 0x6C6B6C, 0x272727, 0x70E259, 0xFFE359,
    0xECA23C, 0x7E5C16, 0xD73E42, 0xFF99BB, 0xD5719C, 0x915DCD,
    0x3894FF, 0x70D2FF, 0x2FCCB7, 0x17B86D, 0x915DCD,
)
CRAFTING_PAINT_BRIGHT = (
    0xF9F9F9, 0xC4C4C4, 0x949294, 0x3B3B3B, 0xB3F86D, 0xF4FF80,
    0xF2BA49, 0x8E6E1A, 0xF07665, 0xFBCAD5, 0xE69EBF, 0xB06FDD,
    0x40C1FF, 0x80F7FF, 0x65E8C9, 0x32D850, 0xE2A3E3,
)
_GLASS_ORDINARY, _GLASS_VIBRANT = CONNECTED_GLASS_BLOCK_IDS
M3C_CASE_LAYOUTS = (
    (
        "isolated-ordinary-vibrant-matched-selection",
        "glass-variant-equivalence",
        (((208, 100, 288), _GLASS_ORDINARY), ((244, 100, 288), _GLASS_VIBRANT)),
        (),
    ),
    (
        "center-down-up",
        "glass-axis-topology",
        (
            ((214, 100, 290), _GLASS_ORDINARY),
            ((214, 99, 290), _GLASS_VIBRANT),
            ((214, 101, 290), _GLASS_ORDINARY),
        ),
        (),
    ),
    (
        "center-north-up",
        "glass-corner-topology",
        (
            ((222, 100, 290), _GLASS_ORDINARY),
            ((222, 100, 289), _GLASS_VIBRANT),
            ((222, 101, 290), _GLASS_ORDINARY),
        ),
        (),
    ),
    (
        "center-north-west",
        "glass-corner-topology",
        (
            ((230, 100, 290), _GLASS_ORDINARY),
            ((230, 100, 289), _GLASS_VIBRANT),
            ((229, 100, 290), _GLASS_ORDINARY),
        ),
        (),
    ),
    (
        "center-north-south-west",
        "glass-t-topology",
        (
            ((238, 100, 290), _GLASS_ORDINARY),
            ((238, 100, 289), _GLASS_VIBRANT),
            ((238, 100, 291), _GLASS_ORDINARY),
            ((237, 100, 290), _GLASS_VIBRANT),
        ),
        (),
    ),
    (
        "center-east-north-up-west",
        "glass-four-arm-topology",
        (
            ((250, 100, 290), _GLASS_ORDINARY),
            ((251, 100, 290), _GLASS_VIBRANT),
            ((250, 100, 289), _GLASS_ORDINARY),
            ((250, 101, 290), _GLASS_VIBRANT),
            ((249, 100, 290), _GLASS_ORDINARY),
        ),
        (),
    ),
    (
        "diagonal-only-ordinary-vibrant",
        "glass-diagonal-nonconnection",
        (((258, 100, 290), _GLASS_ORDINARY), ((259, 101, 290), _GLASS_VIBRANT)),
        (),
    ),
    (
        "three-by-three-checkerboard-plane",
        "glass-checkerboard-plane",
        tuple(
            (
                (263 + offset_x, 100, 289 + offset_z),
                CONNECTED_GLASS_BLOCK_IDS[(offset_x + offset_z) % 2],
            )
            for offset_z in range(3)
            for offset_x in range(3)
        ),
        (),
    ),
    (
        "two-by-two-by-two-checkerboard-cube",
        "glass-checkerboard-cube",
        tuple(
            (
                (272 + offset_x, 100 + offset_y, 289 + offset_z),
                CONNECTED_GLASS_BLOCK_IDS[(offset_x + offset_y + offset_z) % 2],
            )
            for offset_y in range(2)
            for offset_z in range(2)
            for offset_x in range(2)
        ),
        (),
    ),
    (
        "mixed-six-neighbor-enclosed-plus",
        "glass-enclosed-plus",
        (
            ((215, 101, 301), _GLASS_ORDINARY),
            ((215, 100, 301), _GLASS_VIBRANT),
            ((215, 102, 301), _GLASS_ORDINARY),
            ((215, 101, 300), _GLASS_VIBRANT),
            ((215, 101, 302), _GLASS_ORDINARY),
            ((214, 101, 301), _GLASS_VIBRANT),
            ((216, 101, 301), _GLASS_ORDINARY),
        ),
        (),
    ),
    (
        "opaque-neighbor-culling",
        "glass-opaque-neighbor-culling",
        (((226, 100, 301), _GLASS_ORDINARY),),
        (((227, 100, 301), "minecraft:stone"),),
    ),
)
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

EXPECTED_ATTRIBUTES = (
    ("position", 0x21),
    ("normal", 0x63),
    ("color", 0x67),
    ("uv", 0x11),
    ("ao", 0x47),
    ("blocklight", 0x03),
    ("sunlight", 0x03),
)

ENCODINGS = {
    1: ("f", 4),
    3: ("b", 1),
    4: ("h", 2),
    6: ("i", 4),
    7: ("B", 1),
    8: ("H", 2),
    10: ("I", 4),
}


class EvidenceError(ValueError):
    """Raised when map output cannot satisfy the exact evidence contract."""


@dataclass(frozen=True)
class GzipPayload:
    compressed_sha256: str
    payload_sha256: str
    payload: bytes


@dataclass(frozen=True)
class Attribute:
    name: str
    flags: int
    cardinality: int
    encoding: int
    format_char: str
    bytes_per_element: int
    offset: int


@dataclass(frozen=True)
class MaterialGroup:
    material_index: int
    start: int
    count: int


@dataclass(frozen=True)
class PrbmDocument:
    data: bytes
    value_count: int
    attributes: dict[str, Attribute]
    groups: tuple[MaterialGroup, ...]

    @property
    def triangle_count(self) -> int:
        return self.value_count // 3

    def values(self, attribute_name: str, vertex_index: int) -> tuple[int | float, ...]:
        attribute = self.attributes[attribute_name]
        if vertex_index < 0 or vertex_index >= self.value_count:
            raise EvidenceError(f"vertex index outside PRBM values: {vertex_index}")
        offset = attribute.offset + (
            vertex_index * attribute.cardinality * attribute.bytes_per_element
        )
        return struct.unpack_from(
            "<" + attribute.format_char * attribute.cardinality,
            self.data,
            offset,
        )


@dataclass(frozen=True)
class TextureRef:
    index: int
    resource_path: str

    @property
    def semantic_identity(self) -> str:
        return self.resource_path


@dataclass(frozen=True)
class MapSettings:
    tile_size_x: int
    tile_size_z: int
    translate_x: int
    translate_z: int

    def tile_for(self, x: int, z: int) -> tuple[int, int]:
        return (
            math.floor((x - self.translate_x) / self.tile_size_x),
            math.floor((z - self.translate_z) / self.tile_size_z),
        )

    def tile_origin(self, tile_x: int, tile_z: int) -> tuple[int, int]:
        return (
            tile_x * self.tile_size_x + self.translate_x,
            tile_z * self.tile_size_z + self.translate_z,
        )


@dataclass(frozen=True)
class FacePartContract:
    direction: str
    part_id: str
    spin: int | None


@dataclass(frozen=True)
class FacadeContract:
    direction: str
    block_state_json: str


@dataclass(frozen=True)
class TerminalLayerContract:
    resource_path: str
    rgb: tuple[int, int, int]
    emissive: bool
    triangle_count_per_part: int


@dataclass(frozen=True)
class DriveSlotContract:
    slot: int
    item_id: str | None
    model_id: str
    origin: tuple[float, float, float]
    face: str
    face_slot: int
    facing: str
    spin: int
    material: str | None


@dataclass(frozen=True)
class DriveContract:
    facing: str
    spin: int
    slots: tuple[DriveSlotContract, ...]
    occupied_slots: tuple[int, ...]
    led_triangle_count: int
    block_id: str
    base_triangle_count: int
    triangle_formula: str


@dataclass(frozen=True)
class GlassFaceContract:
    direction: str
    frame_mask: str
    base_resource: str
    base_uvs: tuple[tuple[float, float], ...]
    frame_resource: str | None
    frame_uvs: tuple[tuple[float, float], ...]


@dataclass(frozen=True)
class GlassContract:
    block_id: str
    texture_index: int
    faces: tuple[GlassFaceContract, ...]
    connected_faces: tuple[str, ...]
    opaque_culled_faces: tuple[str, ...]


@dataclass(frozen=True)
class CraftingPrimitiveContract:
    role: str
    resource_path: str
    bounds_sixteenths: tuple[float, float, float, float, float, float]
    rgb: tuple[int, int, int]
    emissive: bool


@dataclass(frozen=True)
class CraftingFaceContract:
    direction: str
    primitives: tuple[CraftingPrimitiveContract, ...]


@dataclass(frozen=True)
class CraftingContract:
    block_id: str
    kind: str
    formed: bool
    powered: bool
    facing: str | None
    spin: int | None
    painted_color_ordinal: int | None
    monitor_display_policy: str | None
    connections: tuple[str, ...]
    faces: tuple[CraftingFaceContract, ...]
    fully_enclosed_zero_geometry: bool


@dataclass(frozen=True)
class QuantumPrimitiveContract:
    role: str
    resource_path: str
    bounds_sixteenths: tuple[float, float, float, float, float, float]


@dataclass(frozen=True)
class QuantumContract:
    block_id: str
    plane: str
    role: str
    formed: bool
    waterlogged: bool
    connections: tuple[str, ...]
    primitives: tuple[QuantumPrimitiveContract, ...]
    power_overlay_policy: str
    particle_policy: str


@dataclass(frozen=True)
class PaintSplotchContract:
    signed_position: int
    backing_side: str
    visible_face: str
    resource: str
    rgb: tuple[int, int, int]


@dataclass(frozen=True)
class M3CompletionContract:
    block_id: str
    block_state_json: str
    static_policy: str
    paint_splotches: tuple[PaintSplotchContract, ...]
    pylon_axis: str | None
    pylon_axis_position: str | None
    expected_stock_material_triangles: tuple[tuple[str, int], ...]
    expected_stock_triangle_count: int


@dataclass(frozen=True)
class NativeStructuralPartContract:
    direction: str
    part_id: str
    group: str
    spin: int | None
    frequency: int | None


@dataclass(frozen=True)
class NativeStructuralEndpointContract:
    direction: str
    block_id: str
    block_entity_id: str | None
    required_block_state_json: str
    observed_endpoint_side: str
    side_rule: str
    formation: str | None
    exposed_on_observed_side: bool
    declared_family: str | None
    local_family: str | None
    effective_family: str | None
    collar: bool
    topology: str | None


@dataclass(frozen=True)
class Schema9DisabledProjectionContract:
    expected_path: str
    expected_triangle_count: int
    expected_material_triangles: tuple[tuple[str, int], ...]
    expected_smart_overlays: tuple[
        tuple[str, tuple[int, int, int], int, int], ...
    ]
    expected_terminal_layers: tuple[TerminalLayerContract, ...]


@dataclass(frozen=True)
class NativeStructuralContract:
    cable_id: str | None
    parts: tuple[NativeStructuralPartContract, ...]
    facade_mask: int | None
    plane_mask: int | None
    p2p_frequency: int | None
    endpoints: tuple[NativeStructuralEndpointContract, ...]
    endpoint_straight_optimization_json: str | None
    expected_geometry_signature: str | None
    expected_nonlighting_attribute_signature: str | None
    stock_triangle_count: int
    schema9_route_disabled_projection: Schema9DisabledProjectionContract | None = None


@dataclass(frozen=True)
class M45ProjectionContract:
    expected_path: str
    review_projection: str
    reason: str
    allowed_resources: tuple[str, ...]
    expected_material_triangles: tuple[tuple[str, int], ...]
    expected_triangle_count: int | None = None
    expected_geometry_signature: str | None = None
    expected_nonlighting_attribute_signature: str | None = None


@dataclass(frozen=True)
class M45ReviewContract:
    route: str
    review_projection: str
    allowed_resources: tuple[str, ...]
    stock_allowed_resources: tuple[str, ...]
    route_resources: tuple[str, ...]
    source_resources: tuple[str, ...]
    host_resources: tuple[str, ...]
    route_disabled_projections: tuple[
        tuple[str, M45ProjectionContract], ...
    ]
    physical_stock_projection: M45ProjectionContract
    native_structural_disabled_projection: M45ProjectionContract
    crafting_disabled_projection: M45ProjectionContract | None
    source_derived_synthetic_fixture: bool
    selector_scoped_exact_material_triangles: tuple[tuple[str, int], ...]
    expected_geometry_signature: str | None = None
    expected_nonlighting_attribute_signature: str | None = None


@dataclass(frozen=True)
class M45LegacyUpgradeContract:
    required_m45_routes: tuple[str, ...]
    required_legacy_routes: tuple[str, ...]
    enabled_projection: M45ProjectionContract
    predecessor_projection: M45ProjectionContract
    observed_triangle_count: int
    observed_material_triangles: tuple[tuple[str, int], ...]


@dataclass(frozen=True)
class AppMekProjectionContract:
    expected_path: str
    review_projection: str
    reason: str


@dataclass(frozen=True)
class AppMekAnchorContract:
    case_id: str
    position: tuple[int, int, int]
    route: str | None
    block_id: str
    expected_path: str
    expected_triangle_count: int
    expected_material_triangles: tuple[tuple[str, int], ...]
    physical_stock_projection: AppMekProjectionContract
    route_disabled_projection: AppMekProjectionContract
    native_structural_disabled_projection: AppMekProjectionContract
    native_drive_disabled_projection: AppMekProjectionContract


@dataclass(frozen=True)
class AnchorContract:
    case_id: str
    case_label: str
    expected_path: str
    position: tuple[int, int, int]
    expected_triangle_count: int | None
    expected_material_triangles: tuple[tuple[str, int], ...]
    expected_smart_overlays: tuple[
        tuple[str, tuple[int, int, int], int, int], ...
    ]
    face_parts: tuple[FacePartContract, ...]
    facades: tuple[FacadeContract, ...]
    expected_terminal_layers: tuple[TerminalLayerContract, ...]
    drive: DriveContract | None
    fallback_reason: str | None
    glass: GlassContract | None = None
    crafting: CraftingContract | None = None
    quantum: QuantumContract | None = None
    m3_completion: M3CompletionContract | None = None
    native_structural: NativeStructuralContract | None = None
    m45: M45ReviewContract | None = None
    m45_legacy_upgrade: M45LegacyUpgradeContract | None = None


@dataclass(frozen=True)
class CaseContract:
    case_id: str
    milestone: str
    route: str
    label: str
    category: str
    anchors: tuple[AnchorContract, ...]


@dataclass(frozen=True)
class GalleryContract:
    cases: tuple[CaseContract, ...]
    expected_selected_resources: tuple[str, ...]
    expected_custom_anchor_count: int
    expected_custom_triangle_count: int
    stock_fallback_positions: tuple[tuple[int, int, int], ...]
    dense_positions: tuple[tuple[int, int, int], ...]
    expected_dense_triangle_count: int
    expected_dense_material_triangles: tuple[tuple[str, int], ...]
    schema_version: int
    signature_schema_version: int
    m2_regression_positions: tuple[tuple[int, int, int], ...]
    drive_component_pair: tuple[tuple[int, int, int], ...]
    m3a_regression_positions: tuple[tuple[int, int, int], ...]
    extended_drive_component_pair: tuple[tuple[int, int, int], ...]
    extended_drive_mirror_pair: tuple[tuple[int, int, int], ...]
    extension_positions: tuple[tuple[int, int, int], ...]
    m3b_regression_positions: tuple[tuple[int, int, int], ...] = ()
    glass_positions: tuple[tuple[int, int, int], ...] = ()
    crafting_positions: tuple[tuple[int, int, int], ...] = ()
    quantum_positions: tuple[tuple[int, int, int], ...] = ()
    m3_completion_positions: tuple[tuple[int, int, int], ...] = ()
    native_structural_positions: tuple[tuple[int, int, int], ...] = ()
    native_structural_legacy_upgrade_positions: tuple[
        tuple[int, int, int], ...
    ] = ()
    m45_positions: tuple[tuple[int, int, int], ...] = ()
    m45_route_positions: tuple[
        tuple[str, tuple[tuple[int, int, int], ...]], ...
    ] = ()
    m45_legacy_upgrade_positions: tuple[tuple[int, int, int], ...] = ()
    appmek_anchors: tuple[AppMekAnchorContract, ...] = ()
    appmek_positions: tuple[tuple[int, int, int], ...] = ()
    appmek_route_positions: tuple[tuple[int, int, int], ...] = ()


@dataclass(frozen=True)
class TriangleRecord:
    material_index: int
    material_identity: str
    shape: str
    geometry: str
    attributes: str
    colors: tuple[tuple[int, int, int], ...]
    aos: tuple[int, ...]
    blocklights: tuple[int, ...]
    sunlights: tuple[int, ...]
    positions: tuple[tuple[float, float, float], ...]
    uvs: tuple[tuple[float, float], ...]
    normals: tuple[tuple[int, int, int], ...]


def sha256_bytes(value: bytes) -> str:
    return hashlib.sha256(value).hexdigest()


def sha256_text(value: str) -> str:
    return sha256_bytes(value.encode("utf-8"))


def canonical_json(value: Any, *, pretty: bool = False) -> str:
    if pretty:
        return json.dumps(value, indent=2, sort_keys=True, ensure_ascii=True) + "\n"
    return json.dumps(value, separators=(",", ":"), sort_keys=True, ensure_ascii=True)


def canonical_float(value: float) -> str:
    """Return one exact, locale-independent token for a finite binary float."""
    if not math.isfinite(value):
        raise EvidenceError("PRBM contains a non-finite float")
    if value == 0.0:
        return "0x0p+0"
    mantissa, exponent = value.hex().split("p", 1)
    mantissa = mantissa.rstrip("0").rstrip(".")
    return f"{mantissa}p{int(exponent):+d}"


def canonical_shape_float(value: float) -> str:
    """Remove tile-local float noise without erasing AE2's 0.01/16 overhang."""
    return canonical_float(round(value / SHAPE_QUANTUM) * SHAPE_QUANTUM)


def _no_duplicate_object(pairs: list[tuple[str, Any]]) -> dict[str, Any]:
    value: dict[str, Any] = {}
    for key, item in pairs:
        if key in value:
            raise EvidenceError(f"JSON contains duplicate object key: {key}")
        value[key] = item
    return value


def _invalid_json_constant(value: str) -> None:
    raise EvidenceError(f"JSON contains non-standard numeric constant: {value}")


def parse_json_bytes(payload: bytes, description: str) -> Any:
    try:
        text = payload.decode("utf-8")
    except UnicodeDecodeError as exception:
        raise EvidenceError(f"{description} is not UTF-8") from exception
    try:
        return json.loads(
            text,
            object_pairs_hook=_no_duplicate_object,
            parse_constant=_invalid_json_constant,
        )
    except (json.JSONDecodeError, EvidenceError) as exception:
        raise EvidenceError(f"invalid {description}: {exception}") from exception


def read_json(path: Path, description: str) -> tuple[Any, str]:
    payload = read_bounded(path, MAX_JSON_BYTES, description)
    return parse_json_bytes(payload, description), sha256_bytes(payload)


def read_bounded(path: Path, limit: int, description: str) -> bytes:
    payload = bytearray()
    try:
        with path.open("rb") as source:
            while True:
                chunk = source.read(min(IO_CHUNK_BYTES, limit + 1 - len(payload)))
                if not chunk:
                    break
                payload.extend(chunk)
                if len(payload) > limit:
                    raise EvidenceError(f"{description} exceeds {limit} bytes: {path}")
    except OSError as exception:
        raise EvidenceError(f"cannot read {description}: {path}") from exception
    return bytes(payload)


def read_single_gzip(path: Path) -> GzipPayload:
    """Validate and decompress exactly one gzip member, including CRC/ISIZE."""
    compressed_digest = hashlib.sha256()
    compressed_size = 0
    payload = bytearray()
    decompressor = zlib.decompressobj(16 + zlib.MAX_WBITS)
    try:
        with path.open("rb") as source:
            while True:
                compressed = source.read(IO_CHUNK_BYTES)
                if not compressed:
                    break
                compressed_size += len(compressed)
                if compressed_size > MAX_COMPRESSED_BYTES:
                    raise EvidenceError(
                        f"gzip input exceeds {MAX_COMPRESSED_BYTES} bytes: {path}"
                    )
                compressed_digest.update(compressed)
                if compressed_size == len(compressed) and (
                    len(compressed) < 2 or compressed[:2] != b"\x1f\x8b"
                ):
                    raise EvidenceError(f"input is not a gzip member: {path}")
                if decompressor.eof:
                    raise EvidenceError(
                        f"gzip input must contain exactly one member: {path}"
                    )
                remaining = MAX_DECOMPRESSED_BYTES + 1 - len(payload)
                payload.extend(decompressor.decompress(compressed, remaining))
                if (
                    len(payload) > MAX_DECOMPRESSED_BYTES
                    or decompressor.unconsumed_tail
                ):
                    raise EvidenceError(
                        f"gzip payload exceeds {MAX_DECOMPRESSED_BYTES} bytes: {path}"
                    )
                if decompressor.unused_data:
                    raise EvidenceError(
                        f"gzip input must contain exactly one member: {path}"
                    )
    except OSError as exception:
        raise EvidenceError(f"cannot read gzip input: {path}") from exception
    except zlib.error as exception:
        raise EvidenceError(f"gzip integrity validation failed: {path}: {exception}") from exception

    if compressed_size < 18:
        raise EvidenceError(f"input is not a gzip member: {path}")
    try:
        payload.extend(decompressor.flush(MAX_DECOMPRESSED_BYTES + 1 - len(payload)))
    except zlib.error as exception:
        raise EvidenceError(f"gzip integrity validation failed: {path}: {exception}") from exception
    if len(payload) > MAX_DECOMPRESSED_BYTES:
        raise EvidenceError(f"gzip payload exceeds {MAX_DECOMPRESSED_BYTES} bytes: {path}")
    if not decompressor.eof:
        raise EvidenceError(f"truncated gzip member: {path}")
    if decompressor.unused_data or decompressor.unconsumed_tail:
        raise EvidenceError(f"gzip input must contain exactly one member: {path}")
    return GzipPayload(
        compressed_sha256=compressed_digest.hexdigest(),
        payload_sha256=sha256_bytes(payload),
        payload=bytes(payload),
    )


def _align4(data: bytes, position: int, label: str) -> int:
    aligned = (position + 3) & ~3
    if aligned > len(data):
        raise EvidenceError(f"truncated PRBM while aligning {label}")
    if any(data[position:aligned]):
        raise EvidenceError(f"non-zero PRBM padding before {label}")
    return aligned


def _uint24_little(data: bytes, offset: int) -> int:
    return data[offset] | (data[offset + 1] << 8) | (data[offset + 2] << 16)


def parse_prbm(data: bytes) -> PrbmDocument:
    """Parse the exact PRBM v1 dialect emitted by BlueMap 5.22."""
    if len(data) < 12:
        raise EvidenceError("PRBM is shorter than its header and terminator")
    if data[0] != PRBM_VERSION:
        raise EvidenceError(f"unsupported PRBM version: {data[0]}")
    if data[1] != PRBM_HEADER_FLAGS:
        raise EvidenceError(
            f"unsupported PRBM header flags 0x{data[1]:02x}; expected 0x07"
        )

    value_count = _uint24_little(data, 2)
    index_count = _uint24_little(data, 5)
    if index_count != 0:
        raise EvidenceError("BlueMap 5.22 writer output must be non-indexed")
    if value_count % 3 != 0:
        raise EvidenceError("PRBM vertex count is not divisible by three")

    position = 8
    attributes: dict[str, Attribute] = {}
    observed_attributes: list[tuple[str, int]] = []
    for attribute_number in range(len(EXPECTED_ATTRIBUTES)):
        terminator = data.find(b"\0", position, min(len(data), position + 65))
        if terminator < 0:
            raise EvidenceError(f"attribute {attribute_number} has no bounded name terminator")
        raw_name = data[position:terminator]
        if not raw_name:
            raise EvidenceError(f"attribute {attribute_number} has an empty name")
        try:
            name = raw_name.decode("ascii")
        except UnicodeDecodeError as exception:
            raise EvidenceError(f"attribute {attribute_number} name is not ASCII") from exception
        position = terminator + 1
        if position >= len(data):
            raise EvidenceError(f"attribute {name} is missing flags")
        flags = data[position]
        position += 1
        observed_attributes.append((name, flags))

        cardinality = ((flags >> 4) & 0x03) + 1
        encoding = flags & 0x0F
        encoding_info = ENCODINGS.get(encoding)
        if encoding_info is None:
            raise EvidenceError(f"attribute {name} uses unsupported encoding {encoding}")
        format_char, bytes_per_element = encoding_info
        position = _align4(data, position, f"attribute {name}")
        byte_count = value_count * cardinality * bytes_per_element
        end = position + byte_count
        if end > len(data):
            raise EvidenceError(f"attribute {name} values are truncated")
        if name in attributes:
            raise EvidenceError(f"duplicate PRBM attribute: {name}")
        attributes[name] = Attribute(
            name=name,
            flags=flags,
            cardinality=cardinality,
            encoding=encoding,
            format_char=format_char,
            bytes_per_element=bytes_per_element,
            offset=position,
        )
        position = end

    if tuple(observed_attributes) != EXPECTED_ATTRIBUTES:
        raise EvidenceError(
            "PRBM attributes differ from BlueMap 5.22 writer output: "
            f"{observed_attributes!r}"
        )

    position = _align4(data, position, "material groups")
    groups: list[MaterialGroup] = []
    while True:
        if position + 4 > len(data):
            raise EvidenceError("PRBM material groups have no terminator")
        material_index = struct.unpack_from("<i", data, position)[0]
        position += 4
        if material_index == -1:
            break
        if material_index < 0:
            raise EvidenceError(f"negative PRBM material index: {material_index}")
        if position + 8 > len(data):
            raise EvidenceError("truncated PRBM material group")
        start, count = struct.unpack_from("<ii", data, position)
        position += 8
        groups.append(MaterialGroup(material_index, start, count))
    if position != len(data):
        raise EvidenceError("PRBM contains trailing bytes after the material terminator")

    cursor = 0
    for group in groups:
        if group.start != cursor:
            raise EvidenceError("PRBM material groups do not form a contiguous partition")
        if group.count <= 0 or group.count % 3 != 0:
            raise EvidenceError("PRBM material group count is not positive whole triangles")
        cursor += group.count
        if cursor > value_count:
            raise EvidenceError("PRBM material groups exceed the vertex count")
    if cursor != value_count:
        raise EvidenceError("PRBM material groups do not cover every vertex")
    if value_count == 0 and groups:
        raise EvidenceError("empty PRBM unexpectedly contains material groups")

    document = PrbmDocument(
        data=data,
        value_count=value_count,
        attributes=attributes,
        groups=tuple(groups),
    )
    _validate_float_attributes(document)
    return document


def _validate_float_attributes(document: PrbmDocument) -> None:
    for name in ("position", "uv"):
        attribute = document.attributes[name]
        scalar_count = document.value_count * attribute.cardinality
        end = attribute.offset + scalar_count * attribute.bytes_per_element
        for (value,) in struct.iter_unpack(
            "<f", document.data[attribute.offset:end]
        ):
            if not math.isfinite(value):
                raise EvidenceError(f"PRBM {name} contains a non-finite float")


def parse_settings(map_root: Path) -> tuple[MapSettings, str]:
    value, digest = read_json(map_root / "settings.json", "BlueMap settings.json")
    if not isinstance(value, dict) or not isinstance(value.get("hires"), dict):
        raise EvidenceError("settings.json has no hires object")
    hires = value["hires"]

    def exact_vector(name: str) -> tuple[int, int]:
        vector = hires.get(name)
        if (
            not isinstance(vector, list)
            or len(vector) != 2
            or any(not isinstance(item, int) or isinstance(item, bool) for item in vector)
        ):
            raise EvidenceError(f"settings hires.{name} must be a two-integer array")
        return vector[0], vector[1]

    tile_size = exact_vector("tileSize")
    scale = exact_vector("scale")
    translate = exact_vector("translate")
    if tile_size[0] <= 0 or tile_size[1] <= 0:
        raise EvidenceError("settings hires tile size must be positive")
    if scale != (1, 1):
        raise EvidenceError("exact BlueMap 5.22 map output must use hires scale [1,1]")
    return MapSettings(tile_size[0], tile_size[1], translate[0], translate[1]), digest


def parse_textures(map_root: Path) -> tuple[tuple[TextureRef, ...], dict[str, Any]]:
    gzip_payload = read_single_gzip(map_root / "textures.json.gz")
    value = parse_json_bytes(gzip_payload.payload, "textures.json.gz payload")
    if not isinstance(value, list):
        raise EvidenceError("textures.json.gz payload must be an array")
    textures: list[TextureRef] = []
    resource_paths: set[str] = set()
    for index, entry in enumerate(value):
        if not isinstance(entry, dict):
            raise EvidenceError(f"texture {index} is not an object")
        resource_path = entry.get("resourcePath")
        if not isinstance(resource_path, str):
            raise EvidenceError(f"texture {index} resourcePath is missing or not a string")
        if resource_path == "":
            raise EvidenceError(f"texture {index} resourcePath is empty")
        if resource_path in resource_paths:
            raise EvidenceError(f"duplicate texture resourcePath: {resource_path}")
        resource_paths.add(resource_path)
        textures.append(TextureRef(index=index, resource_path=resource_path))
    return tuple(textures), {
        "compressed_sha256": gzip_payload.compressed_sha256,
        "payload_sha256": gzip_payload.payload_sha256,
        "texture_count": len(textures),
    }


def _parse_cases_value(
    value: dict[str, Any], digest: str
) -> tuple[GalleryContract, dict[str, Any]]:
    if not isinstance(value, dict) or not isinstance(value.get("cases"), list):
        raise EvidenceError("gallery cases manifest has no cases array")
    schema_version = value.get("schema_version")
    signature_schema_version = value.get("signature_schema_version")
    if schema_version not in {3, 4} or signature_schema_version != schema_version:
        raise EvidenceError("gallery cases manifest must use matching schema/signature version 3 or 4")
    profile = value.get("profile")
    if not isinstance(profile, dict):
        raise EvidenceError("gallery cases manifest has no exact profile")
    if (
        profile.get("mod_id") != "ae2"
        or profile.get("version") != "19.2.17"
        or profile.get("coverage_milestone")
        != ("M2" if schema_version == 3 else "M3a")
        or profile.get("transient_policy") != "idle-off-unknown"
        or profile.get("texture_manifest_sha256") != (
            "e40a9bc4942d8999d825f42bce94947079948d74024f4f1a078cc55252d81d33"
            if schema_version == 3
            else "c0e66d75cad06649b021f8a9073629d6619050c4f69e78c522b6fa32fb232242"
        )
        or profile.get("supported_face_parts")
        != [{"id": "ae2:terminal", "spins": [0, 1, 2, 3]}]
        or profile.get("facade_policy")
        != {
            "block_state": {"Name": "minecraft:stone"},
            "maximum_facades": 1,
            "properties": "forbidden",
            "required_same_face_part": "ae2:terminal",
        }
    ):
        raise EvidenceError(
            f"gallery cases manifest is not the exact AE2 19.2.17 schema-{schema_version} profile"
        )
    supported_center_parts = profile.get("supported_center_parts")
    selected_resources = profile.get("selected_resources")
    resolved_facade_resources = profile.get("resolved_facade_resources")
    if (
        not isinstance(supported_center_parts, list)
        or len(supported_center_parts) != 85
        or any(not isinstance(item, str) or not item for item in supported_center_parts)
        or len(set(supported_center_parts)) != len(supported_center_parts)
    ):
        raise EvidenceError("gallery exact profile must contain 85 unique center-part IDs")
    if (
        not isinstance(selected_resources, list)
        or len(selected_resources) != (148 if schema_version == 3 else 158)
        or any(not isinstance(item, str) or not item for item in selected_resources)
        or len(set(selected_resources)) != len(selected_resources)
    ):
        raise EvidenceError(
            f"gallery exact profile must contain {148 if schema_version == 3 else 158} unique AE2 resources"
        )
    if resolved_facade_resources != ["minecraft:block/stone"]:
        raise EvidenceError("gallery exact profile must resolve only the stone facade resource")
    expected_selected_resources = set(selected_resources) | set(resolved_facade_resources)
    expected_material_count = 149 if schema_version == 3 else 159
    if len(expected_selected_resources) != expected_material_count:
        raise EvidenceError(
            f"gallery exact material closure must contain {expected_material_count} resources"
        )

    if schema_version == 4:
        core_resources = profile.get("core_resources")
        drive_resources = profile.get("drive_resources")
        supported_drive = profile.get("supported_drive")
        exact_drive_resources = {
            "ae2:block/drive/drive_cells",
            "ae2:block/drive/drive_front",
            "ae2:block/drive/drive_inside",
            "ae2:block/drive/drive_inside_bottom",
            "ae2:block/drive/drive_inside_top",
            "ae2:block/generics/back",
            "ae2:block/generics/bottom",
            "ae2:block/generics/front",
            "ae2:block/generics/side",
            "ae2:block/generics/top",
        }
        if (
            not isinstance(core_resources, list)
            or len(core_resources) != 148
            or len(set(core_resources)) != 148
            or not isinstance(drive_resources, list)
            or set(drive_resources) != exact_drive_resources
            or len(drive_resources) != 10
            or set(core_resources).intersection(drive_resources)
            or set(selected_resources) != set(core_resources) | set(drive_resources)
            or profile.get("core_texture_manifest_sha256")
            != "e40a9bc4942d8999d825f42bce94947079948d74024f4f1a078cc55252d81d33"
            or profile.get("required_resources_sha256")
            != "408297def444f1392b7b87fdc4b8520099513b4c57c63a4176b808ce61b4e1be"
            or not isinstance(supported_drive, dict)
            or supported_drive.get("block_id") != "ae2:drive"
            or supported_drive.get("base_model") != "ae2:block/drive/drive_base"
            or supported_drive.get("empty_cell_model")
            != "ae2:block/drive/drive_cell_empty"
            or supported_drive.get("slot_count") != 10
            or supported_drive.get("supported_cell_id_count") != 23
            or supported_drive.get("occupied_model_count") != 12
            or supported_drive.get("led_policy") != "static-offline-unknown"
            or supported_drive.get("unknown_cell_policy")
            != "atomic-whole-block-original-resource-fallback"
        ):
            raise EvidenceError("gallery exact M3a Drive profile is invalid")
        explicit_models = supported_drive.get("explicit_cell_models")
        generic_model = supported_drive.get("generic_cell_model")
        if (
            explicit_models != DRIVE_EXPLICIT_CELL_MODELS
            or generic_model
            != {
                "item_ids": ["ae2:matter_cannon", "ae2:color_applicator"],
                "model": "ae2:block/drive/drive_cell",
            }
        ):
            raise EvidenceError("gallery exact M3a Drive cell catalog is invalid")

    expected_summary = value.get("expected_custom_summary")
    exact_custom_summary = (
        {
            "anchor_count": 278,
            "selected_resource_count": 149,
            "triangle_count": 8_576,
        }
        if schema_version == 3
        else {
            "anchor_count": 310,
            "selected_resource_count": 159,
            "triangle_count": 12_432,
        }
    )
    if expected_summary != exact_custom_summary:
        raise EvidenceError("gallery expected custom summary is not the exact milestone contract")
    expected_custom_anchor_count = expected_summary.get("anchor_count")
    expected_custom_triangle_count = expected_summary.get("triangle_count")
    expected_resource_count = expected_summary.get("selected_resource_count")
    if (
        not isinstance(expected_custom_anchor_count, int)
        or isinstance(expected_custom_anchor_count, bool)
        or expected_custom_anchor_count < 1
        or not isinstance(expected_custom_triangle_count, int)
        or isinstance(expected_custom_triangle_count, bool)
        or expected_custom_triangle_count < 1
        or expected_resource_count != len(expected_selected_resources)
    ):
        raise EvidenceError("gallery expected custom summary is invalid")
    fallback_summary = value.get("expected_stock_fallback_summary")
    expected_fallback_count = 11 if schema_version == 3 else 12
    if fallback_summary != {
        "anchor_count": expected_fallback_count,
        "triangle_count": 0,
    }:
        raise EvidenceError(
            f"gallery stock-fallback summary must require {expected_fallback_count} empty anchors"
        )
    m1_summary = value.get("m1_regression_summary")
    if m1_summary != {
        "anchor_count": 269,
        "case_count": 48,
        "custom_anchor_count": 266,
        "custom_triangle_count": 7_576,
        "selected_resource_count": 140,
    }:
        raise EvidenceError("gallery M1 regression summary changed")
    m2_summary = value.get("m2_summary")
    if m2_summary != {
        "anchor_count": 21,
        "case_count": 14,
        "custom_anchor_count": 12,
        "custom_triangle_count": 1_000,
        "new_selected_resource_count": 9,
        "selected_resource_count": 16,
        "stock_fallback_anchor_count": 9,
    }:
        raise EvidenceError("gallery M2 summary changed")
    m3_summary = value.get("m3_summary")
    if schema_version == 4 and m3_summary != {
        "anchor_count": 33,
        "case_count": 14,
        "cell_chassis_triangle_count": 366,
        "custom_anchor_count": 32,
        "custom_triangle_count": 3_856,
        "new_selected_resource_count": 10,
        "occupied_model_count": 12,
        "occupied_slot_count": 61,
        "offline_led_triangle_count": 610,
        "selected_resource_count": 10,
        "stock_fallback_anchor_count": 1,
        "supported_cell_id_count": 23,
    }:
        raise EvidenceError("gallery M3a summary changed")
    if schema_version == 3 and m3_summary is not None:
        raise EvidenceError("schema-3 gallery must not contain an M3 summary")
    unit_only_cases = value.get("unit_only_malformed_cases")
    if (
        not isinstance(unit_only_cases, list)
        or not unit_only_cases
        or any(not isinstance(item, str) or not item for item in unit_only_cases)
        or len(unit_only_cases) != len(set(unit_only_cases))
        or not isinstance(value.get("unit_only_reason"), str)
        or not value["unit_only_reason"]
    ):
        raise EvidenceError("gallery unit-only malformed-state boundary is invalid")

    cases: list[CaseContract] = []
    case_ids: set[str] = set()
    positions: set[tuple[int, int, int]] = set()
    custom_anchor_count = 0
    custom_triangle_count = 0
    expected_resource_union: set[str] = set()
    observed_center_parts: set[str] = set()
    stock_fallback_positions: list[tuple[int, int, int]] = []
    m1_custom_anchor_count = 0
    m1_custom_triangle_count = 0
    m1_resource_union: set[str] = set()
    m2_custom_anchor_count = 0
    m2_custom_triangle_count = 0
    m2_resource_union: set[str] = set()
    m2_fallback_count = 0
    m3_custom_anchor_count = 0
    m3_custom_triangle_count = 0
    m3_resource_union: set[str] = set()
    m3_fallback_count = 0
    m3_occupied_slot_count = 0
    m3_led_triangle_count = 0
    m3_chassis_triangle_count = 0
    m3_supported_cell_ids: set[str] = set()
    m3_occupied_model_ids: set[str] = set()
    m3_orientation_contracts: set[tuple[str, int]] = set()
    drive_component_pair: list[tuple[int, int, int]] = []
    drive_component_variants: set[bool] = set()
    supported_fallback_reasons = {
        "missing-center-part",
        "unsupported-face-part",
        "invalid-face-part-spin",
        "unsupported-face-part-topology",
        "unsupported-facade-layout",
        "unsupported-facade-state",
        "unsupported-drive-cell-id",
    }
    for case_value in value["cases"]:
        if not isinstance(case_value, dict):
            raise EvidenceError("gallery case is not an object")
        case_id = case_value.get("case_id")
        label = case_value.get("label")
        category = case_value.get("category")
        anchor_values = case_value.get("anchors")
        if not all(isinstance(item, str) and item for item in (case_id, label, category)):
            raise EvidenceError("gallery case identity is incomplete")
        if case_id in case_ids:
            raise EvidenceError(f"duplicate gallery case ID: {case_id}")
        case_ids.add(case_id)
        if not isinstance(anchor_values, list) or not anchor_values:
            raise EvidenceError(f"gallery case {case_id} has no anchors")
        anchors: list[AnchorContract] = []
        for anchor_value in anchor_values:
            position_value = (
                anchor_value.get("position") if isinstance(anchor_value, dict) else None
            )
            if not isinstance(position_value, dict):
                raise EvidenceError(f"gallery case {case_id} has an invalid anchor")
            coordinates = tuple(position_value.get(axis) for axis in ("x", "y", "z"))
            if any(
                not isinstance(coordinate, int) or isinstance(coordinate, bool)
                for coordinate in coordinates
            ):
                raise EvidenceError(f"gallery case {case_id} anchor is not integral")
            position_tuple = (coordinates[0], coordinates[1], coordinates[2])
            if position_tuple in positions:
                raise EvidenceError(f"duplicate gallery anchor: {position_tuple}")
            positions.add(position_tuple)
            expected_path = anchor_value.get("expected_path")
            if not isinstance(expected_path, str) or not expected_path:
                raise EvidenceError(f"gallery case {case_id} anchor has no expected path")
            cable_id = anchor_value.get("cable_id")
            if cable_id is not None and (not isinstance(cable_id, str) or not cable_id):
                raise EvidenceError(f"gallery case {case_id} anchor cable ID is invalid")
            expected_triangle_count: int | None = None
            expected_material_triangles: tuple[tuple[str, int], ...] = ()
            expected_smart_overlays: tuple[
                tuple[str, tuple[int, int, int], int, int], ...
            ] = ()
            face_parts: list[FacePartContract] = []
            face_part_value = anchor_value.get("face_parts", [])
            if not isinstance(face_part_value, list):
                raise EvidenceError(f"gallery anchor {position_tuple} face parts are invalid")
            face_directions: set[str] = set()
            for part in face_part_value:
                if not isinstance(part, dict):
                    raise EvidenceError(f"gallery anchor {position_tuple} face part is invalid")
                direction = part.get("direction")
                part_id = part.get("id")
                spin = part.get("spin")
                if (
                    direction not in DIRECTION_VECTORS
                    or direction in face_directions
                    or not isinstance(part_id, str)
                    or not part_id
                    or (spin is not None and (
                        not isinstance(spin, int) or isinstance(spin, bool)
                    ))
                ):
                    raise EvidenceError(f"gallery anchor {position_tuple} face part is invalid")
                face_directions.add(direction)
                face_parts.append(FacePartContract(direction, part_id, spin))

            facades: list[FacadeContract] = []
            facade_value = anchor_value.get("facades", [])
            if not isinstance(facade_value, list):
                raise EvidenceError(f"gallery anchor {position_tuple} facades are invalid")
            facade_directions: set[str] = set()
            for facade in facade_value:
                if not isinstance(facade, dict):
                    raise EvidenceError(f"gallery anchor {position_tuple} facade is invalid")
                direction = facade.get("direction")
                block_state = facade.get("block_state")
                if (
                    direction not in DIRECTION_VECTORS
                    or direction in facade_directions
                    or not isinstance(block_state, dict)
                    or not isinstance(block_state.get("Name"), str)
                    or not block_state["Name"]
                ):
                    raise EvidenceError(f"gallery anchor {position_tuple} facade is invalid")
                facade_directions.add(direction)
                facades.append(FacadeContract(direction, canonical_json(block_state)))

            terminal_layers: list[TerminalLayerContract] = []
            drive_contract: DriveContract | None = None
            terminal_layer_value = anchor_value.get("expected_terminal_layers", {})
            if not isinstance(terminal_layer_value, dict):
                raise EvidenceError(
                    f"gallery anchor {position_tuple} terminal layer contract is invalid"
                )

            if expected_path in {"custom-m1", "custom-m2", "custom-m3"}:
                custom_anchor_count += 1
                if expected_path != "custom-m3" and not isinstance(cable_id, str):
                    raise EvidenceError(f"custom gallery anchor {position_tuple} has no cable ID")
                if isinstance(cable_id, str):
                    observed_center_parts.add(cable_id)
                expected_triangle_count = anchor_value.get("expected_triangle_count")
                material_value = anchor_value.get("expected_material_triangles")
                if (
                    not isinstance(expected_triangle_count, int)
                    or isinstance(expected_triangle_count, bool)
                    or expected_triangle_count < 1
                    or not isinstance(material_value, dict)
                    or not material_value
                ):
                    raise EvidenceError(
                        f"custom gallery anchor {position_tuple} has no exact geometry contract"
                    )
                material_rows: list[tuple[str, int]] = []
                for resource_path, triangle_count in material_value.items():
                    if (
                        not isinstance(resource_path, str)
                        or not resource_path
                        or not isinstance(triangle_count, int)
                        or isinstance(triangle_count, bool)
                        or triangle_count < 0
                        or (
                            triangle_count == 0
                            and not (
                                expected_path == "custom-m3"
                                and resource_path == DRIVE_CELL_MATERIAL
                            )
                        )
                    ):
                        raise EvidenceError(
                            f"custom gallery anchor {position_tuple} has an invalid material contract"
                        )
                    if triangle_count > 0:
                        material_rows.append((resource_path, triangle_count))
                    expected_resource_union.add(resource_path)
                expected_material_triangles = tuple(sorted(material_rows))
                if sum(count for _path, count in expected_material_triangles) != expected_triangle_count:
                    raise EvidenceError(
                        f"custom gallery anchor {position_tuple} material/triangle totals differ"
                    )
                custom_triangle_count += expected_triangle_count
                if expected_path == "custom-m1":
                    m1_custom_anchor_count += 1
                    m1_custom_triangle_count += expected_triangle_count
                    m1_resource_union.update(material_value)
                    if face_parts or facades or terminal_layer_value:
                        raise EvidenceError(
                            f"M1 regression anchor {position_tuple} declares M2 state"
                        )
                elif expected_path == "custom-m2":
                    m2_custom_anchor_count += 1
                    m2_custom_triangle_count += expected_triangle_count
                    m2_resource_union.update(material_value)
                    if any(
                        part.part_id != "ae2:terminal"
                        or part.spin is None
                        or part.spin < 0
                        or part.spin > 3
                        for part in face_parts
                    ):
                        raise EvidenceError(
                            f"custom M2 anchor {position_tuple} has an unsupported face part"
                        )
                    if len(facades) > 1 or any(
                        facade.block_state_json
                        != canonical_json({"Name": "minecraft:stone"})
                        for facade in facades
                    ):
                        raise EvidenceError(
                            f"custom M2 anchor {position_tuple} has an unsupported facade"
                        )
                    if facades and (
                        len(face_parts) != 1
                        or facades[0].direction != face_parts[0].direction
                    ):
                        raise EvidenceError(
                            f"custom M2 anchor {position_tuple} facade is not on its terminal face"
                        )
                    if bool(face_parts) != bool(terminal_layer_value):
                        raise EvidenceError(
                            f"custom M2 anchor {position_tuple} terminal layers are incomplete"
                        )
                    if terminal_layer_value and set(terminal_layer_value) != TERMINAL_LAYER_RESOURCES:
                        raise EvidenceError(
                            f"custom M2 anchor {position_tuple} terminal layer resources changed"
                        )
                    for resource_path, layer in terminal_layer_value.items():
                        rgb = layer.get("rgb_u8") if isinstance(layer, dict) else None
                        emissive = layer.get("emissive") if isinstance(layer, dict) else None
                        per_part = (
                            layer.get("triangle_count_per_part")
                            if isinstance(layer, dict)
                            else None
                        )
                        if (
                            not isinstance(rgb, list)
                            or len(rgb) != 3
                            or any(
                                not isinstance(component, int)
                                or isinstance(component, bool)
                                or component < 0
                                or component > 255
                                for component in rgb
                            )
                            or not isinstance(emissive, bool)
                            or not isinstance(per_part, int)
                            or isinstance(per_part, bool)
                            or per_part != 2
                            or material_value.get(resource_path)
                            != per_part * len(face_parts)
                        ):
                            raise EvidenceError(
                                f"custom M2 anchor {position_tuple} terminal layer contract is invalid"
                            )
                        terminal_layers.append(
                            TerminalLayerContract(
                                resource_path,
                                (rgb[0], rgb[1], rgb[2]),
                                emissive,
                                per_part,
                            )
                        )
                    if facades and material_value.get("minecraft:block/stone") != 48:
                        raise EvidenceError(
                            f"custom M2 anchor {position_tuple} facade triangle contract changed"
                        )
                else:
                    if schema_version != 4:
                        raise EvidenceError("schema-3 gallery cannot declare custom M3 anchors")
                    if face_parts or facades or terminal_layer_value:
                        raise EvidenceError(
                            f"custom M3 Drive anchor {position_tuple} declares cable-bus state"
                        )
                    block_state = anchor_value.get("block_state")
                    inventory = anchor_value.get("inventory")
                    drive_models = anchor_value.get("expected_drive_models")
                    led = anchor_value.get("expected_drive_led")
                    if (
                        anchor_value.get("block_id") != "ae2:drive"
                        or not isinstance(block_state, dict)
                        or set(block_state) != {"facing", "spin"}
                        or block_state.get("facing") not in DRIVE_ORIENTATION_ANGLES
                        or not isinstance(block_state.get("spin"), int)
                        or isinstance(block_state.get("spin"), bool)
                        or block_state["spin"] not in range(4)
                        or not isinstance(inventory, dict)
                        or inventory.get("compound") != "inv"
                        or not isinstance(inventory.get("slots"), list)
                        or len(inventory["slots"]) != 10
                        or not isinstance(drive_models, dict)
                        or drive_models.get("base_model_id")
                        != "ae2:block/drive/drive_base"
                        or drive_models.get("empty_cell_model_id")
                        != "ae2:block/drive/drive_cell_empty"
                        or not isinstance(drive_models.get("slots"), list)
                        or len(drive_models["slots"]) != 10
                    ):
                        raise EvidenceError(
                            f"custom M3 Drive anchor {position_tuple} metadata is invalid"
                        )
                    slot_contracts: list[DriveSlotContract] = []
                    occupied_slots: list[int] = []
                    for slot in range(10):
                        inventory_slot = inventory["slots"][slot]
                        model_slot = drive_models["slots"][slot]
                        expected_origin_16 = (
                            9 - (slot % 2) * 8,
                            13 - (slot // 2) * 3,
                            1,
                        )
                        expected_origin = tuple(value / 16 for value in expected_origin_16)
                        if (
                            not isinstance(inventory_slot, dict)
                            or inventory_slot.get("slot") != slot
                            or inventory_slot.get("field") != f"item{slot}"
                            or not isinstance(model_slot, dict)
                            or model_slot.get("slot") != slot
                            or model_slot.get("slot_origin_sixteenths")
                            != list(expected_origin_16)
                            or model_slot.get("slot_origin")
                            != dict(zip(("x", "y", "z"), expected_origin, strict=True))
                        ):
                            raise EvidenceError(
                                f"custom M3 Drive anchor {position_tuple} slot {slot} metadata is invalid"
                            )
                        empty = inventory_slot.get("empty") is True
                        stack = inventory_slot.get("item_stack")
                        item_id: str | None = None
                        if empty:
                            if set(inventory_slot) != {"slot", "field", "empty"}:
                                raise EvidenceError(
                                    f"custom M3 Drive anchor {position_tuple} empty slot {slot} is invalid"
                                )
                            expected_model = "ae2:block/drive/drive_cell_empty"
                        else:
                            if (
                                not isinstance(stack, dict)
                                or not isinstance(stack.get("id"), str)
                                or not stack["id"]
                                or stack.get("count") != 1
                            ):
                                raise EvidenceError(
                                    f"custom M3 Drive anchor {position_tuple} occupied slot {slot} is invalid"
                                )
                            item_id = stack["id"]
                            occupied_slots.append(slot)
                            m3_supported_cell_ids.add(item_id)
                            expected_model = profile["supported_drive"]["explicit_cell_models"].get(item_id)
                            if item_id in profile["supported_drive"]["generic_cell_model"]["item_ids"]:
                                expected_model = profile["supported_drive"]["generic_cell_model"]["model"]
                            if expected_model is None:
                                raise EvidenceError(
                                    f"custom M3 Drive anchor {position_tuple} uses an unsupported cell ID"
                                )
                            m3_occupied_model_ids.add(expected_model)
                        model_id = model_slot.get("model_id")
                        if model_id != expected_model:
                            raise EvidenceError(
                                f"custom M3 Drive anchor {position_tuple} slot {slot} model mapping changed"
                            )
                        slot_contracts.append(
                            DriveSlotContract(
                                slot,
                                item_id,
                                model_id,
                                expected_origin,
                                "front",
                                slot,
                                block_state["facing"],
                                block_state["spin"],
                                DRIVE_CELL_MATERIAL if item_id is not None else None,
                            )
                        )
                    occupied_count = len(occupied_slots)
                    exact_led = {
                        "ambient_occlusion_f32": 1.0,
                        "blocklight_raw_i8": 15,
                        "material": DRIVE_LED_MATERIAL,
                        "policy": "static-offline-unknown",
                        "rgb_u8": [0, 0, 0],
                        "sunlight_raw_i8": 15,
                        "triangle_count": occupied_count * 10,
                        "triangle_count_per_occupied_slot": 10,
                    }
                    if (
                        led != exact_led
                        or expected_triangle_count != 90 + 16 * occupied_count
                        or material_value.get(DRIVE_CELL_MATERIAL) != 6 * occupied_count
                        or material_value.get(DRIVE_LED_MATERIAL)
                        != 28 + 10 * occupied_count
                    ):
                        raise EvidenceError(
                            f"custom M3 Drive anchor {position_tuple} does not satisfy 90+16N"
                        )
                    drive_contract = DriveContract(
                        block_state["facing"],
                        block_state["spin"],
                        tuple(slot_contracts),
                        tuple(occupied_slots),
                        occupied_count * 10,
                        "ae2:drive",
                        90,
                        "90+16N",
                    )
                    m3_custom_anchor_count += 1
                    m3_custom_triangle_count += expected_triangle_count
                    m3_resource_union.update(material_value)
                    m3_occupied_slot_count += occupied_count
                    m3_led_triangle_count += occupied_count * 10
                    m3_chassis_triangle_count += occupied_count * 6
                    if category == "drive-orientation":
                        m3_orientation_contracts.add(
                            (block_state["facing"], block_state["spin"])
                        )
                        if (
                            occupied_slots != [0]
                            or slot_contracts[0].item_id != "ae2:item_storage_cell_1k"
                            or slot_contracts[0].model_id
                            != "ae2:block/drive/cells/1k_item_cell"
                        ):
                            raise EvidenceError("M3 Drive orientation case must occupy only slot zero")
                    if category == "drive-component-insensitivity":
                        component_payload = inventory["slots"][0]["item_stack"].get(
                            "components"
                        )
                        if component_payload is not None and component_payload != {
                            "ae2:storage_cell_inv": [
                                {"#": 64, "#t": "ae2:i", "id": "minecraft:stone"}
                            ]
                        }:
                            raise EvidenceError(
                                "M3 Drive component-insensitivity payload changed"
                            )
                        drive_component_variants.add(component_payload is not None)
                        drive_component_pair.append(position_tuple)
                overlay_value = anchor_value.get("expected_smart_overlays", {})
                if not isinstance(overlay_value, dict):
                    raise EvidenceError(
                        f"custom gallery anchor {position_tuple} smart overlay contract is invalid"
                    )
                overlay_rows: list[
                    tuple[str, tuple[int, int, int], int, int]
                ] = []
                for resource_path, overlay in overlay_value.items():
                    rgb = overlay.get("rgb_u8") if isinstance(overlay, dict) else None
                    blocklight = (
                        overlay.get("blocklight_raw_i8")
                        if isinstance(overlay, dict)
                        else None
                    )
                    sunlight = (
                        overlay.get("sunlight_raw_i8")
                        if isinstance(overlay, dict)
                        else None
                    )
                    if (
                        resource_path not in material_value
                        or not isinstance(rgb, list)
                        or len(rgb) != 3
                        or any(
                            not isinstance(component, int)
                            or isinstance(component, bool)
                            or component < 0
                            or component > 255
                            for component in rgb
                        )
                        or blocklight != 15
                        or sunlight != 15
                    ):
                        raise EvidenceError(
                            f"custom gallery anchor {position_tuple} smart overlay contract is invalid"
                        )
                    overlay_rows.append(
                        (
                            resource_path,
                            (rgb[0], rgb[1], rgb[2]),
                            blocklight,
                            sunlight,
                        )
                    )
                expected_overlay_paths = {
                    resource_path
                    for resource_path in material_value
                    if resource_path.endswith(("/channels_00", "/channels_10"))
                }
                if set(overlay_value) != expected_overlay_paths:
                    raise EvidenceError(
                        f"custom gallery anchor {position_tuple} does not fully declare smart overlays"
                    )
                expected_smart_overlays = tuple(sorted(overlay_rows))
            elif expected_path == "stock-fallback-device-endpoint":
                if anchor_value.get("expected_triangle_count") != 0:
                    raise EvidenceError(
                        f"device fallback anchor {position_tuple} must require zero triangles"
                    )
                expected_triangle_count = 0
                stock_fallback_positions.append(position_tuple)
            elif expected_path == "stock-fallback-m2":
                fallback_reason = anchor_value.get("fallback_reason")
                if (
                    anchor_value.get("expected_triangle_count") != 0
                    or fallback_reason not in supported_fallback_reasons
                ):
                    raise EvidenceError(
                        f"M2 fallback anchor {position_tuple} has an invalid reason or triangle count"
                    )
                expected_triangle_count = 0
                stock_fallback_positions.append(position_tuple)
                m2_fallback_count += 1
            elif expected_path == "stock-fallback-m3":
                fallback_reason = anchor_value.get("fallback_reason")
                if (
                    schema_version != 4
                    or anchor_value.get("block_id") != "ae2:drive"
                    or anchor_value.get("expected_triangle_count") != 0
                    or fallback_reason != "unsupported-drive-cell-id"
                ):
                    raise EvidenceError(
                        f"M3 fallback anchor {position_tuple} has an invalid reason or triangle count"
                    )
                expected_triangle_count = 0
                stock_fallback_positions.append(position_tuple)
                m3_fallback_count += 1
            elif expected_path == "stock-control":
                if anchor_value.get("block_id") != "minecraft:stone":
                    raise EvidenceError("gallery stock control is not plain stone")
            elif any(
                key in anchor_value
                for key in (
                    "expected_triangle_count",
                    "expected_material_triangles",
                    "expected_smart_overlays",
                )
            ):
                raise EvidenceError(
                    f"non-custom gallery anchor {position_tuple} declares custom geometry"
                )
            else:
                raise EvidenceError(
                    f"gallery anchor {position_tuple} has an unsupported expected path"
                )
            fallback_reason = anchor_value.get("fallback_reason")
            if fallback_reason is not None and not isinstance(fallback_reason, str):
                raise EvidenceError(f"gallery anchor {position_tuple} fallback reason is invalid")
            anchors.append(
                AnchorContract(
                    case_id,
                    label,
                    expected_path,
                    position_tuple,
                    expected_triangle_count,
                    expected_material_triangles,
                    expected_smart_overlays,
                    tuple(face_parts),
                    tuple(facades),
                    tuple(sorted(terminal_layers, key=lambda item: item.resource_path)),
                    drive_contract,
                    fallback_reason,
                )
            )
        legacy_milestone = (
            "M1"
            if case_id.startswith("ae2-m1-")
            else "M2"
            if case_id.startswith("ae2-m2-")
            else "M3a"
        )
        legacy_route = "ae2:drive" if legacy_milestone == "M3a" else "ae2:cable_bus"
        cases.append(
            CaseContract(
                case_id,
                legacy_milestone,
                legacy_route,
                label,
                category,
                tuple(anchors),
            )
        )

    declared_cases = value.get("case_count")
    declared_anchors = value.get("anchor_count")
    if declared_cases != len(cases) or declared_anchors != len(positions):
        raise EvidenceError("gallery declared case/anchor counts do not match its contents")
    expected_case_count = 62 if schema_version == 3 else 76
    expected_anchor_count = 290 if schema_version == 3 else 323
    if declared_cases != expected_case_count or declared_anchors != expected_anchor_count:
        raise EvidenceError(
            f"gallery is not the exact {expected_case_count}-case/{expected_anchor_count}-anchor contract"
        )
    if custom_anchor_count != expected_custom_anchor_count:
        raise EvidenceError("gallery custom anchor total differs from its declared contract")
    if custom_triangle_count != expected_custom_triangle_count:
        raise EvidenceError("gallery custom triangle total differs from its declared contract")
    if observed_center_parts != set(supported_center_parts):
        raise EvidenceError("gallery custom anchors do not cover all 85 exact center-part IDs")
    if expected_resource_union != expected_selected_resources:
        raise EvidenceError(
            f"gallery custom anchors do not select all {expected_material_count} exact materials"
        )
    if len(stock_fallback_positions) != expected_fallback_count:
        raise EvidenceError(
            f"gallery must contain exactly {expected_fallback_count} stock fallbacks"
        )
    m1_cases = [case for case in cases if case.case_id.startswith("ae2-m1-")]
    m2_cases = [case for case in cases if case.case_id.startswith("ae2-m2-")]
    m3_cases = [case for case in cases if case.case_id.startswith("ae2-m3-")]
    if (
        len(m1_cases) != m1_summary["case_count"]
        or sum(len(case.anchors) for case in m1_cases) != m1_summary["anchor_count"]
        or m1_custom_anchor_count != m1_summary["custom_anchor_count"]
        or m1_custom_triangle_count != m1_summary["custom_triangle_count"]
        or len(m1_resource_union) != m1_summary["selected_resource_count"]
    ):
        raise EvidenceError("gallery M1 regression contents differ from their summary")
    if (
        len(m2_cases) != m2_summary["case_count"]
        or sum(len(case.anchors) for case in m2_cases) != m2_summary["anchor_count"]
        or m2_custom_anchor_count != m2_summary["custom_anchor_count"]
        or m2_custom_triangle_count != m2_summary["custom_triangle_count"]
        or len(m2_resource_union) != m2_summary["selected_resource_count"]
        or len(m2_resource_union - m1_resource_union)
        != m2_summary["new_selected_resource_count"]
        or m2_fallback_count != m2_summary["stock_fallback_anchor_count"]
    ):
        raise EvidenceError("gallery M2 contents differ from their summary")
    if schema_version == 3 and m3_cases:
        raise EvidenceError("schema-3 gallery contains M3 cases")
    if schema_version == 4 and (
        len(m3_cases) != m3_summary["case_count"]
        or sum(len(case.anchors) for case in m3_cases) != m3_summary["anchor_count"]
        or m3_custom_anchor_count != m3_summary["custom_anchor_count"]
        or m3_custom_triangle_count != m3_summary["custom_triangle_count"]
        or len(m3_resource_union) != m3_summary["selected_resource_count"]
        or len(m3_resource_union - (m1_resource_union | m2_resource_union))
        != m3_summary["new_selected_resource_count"]
        or m3_fallback_count != m3_summary["stock_fallback_anchor_count"]
        or m3_occupied_slot_count != m3_summary["occupied_slot_count"]
        or m3_led_triangle_count != m3_summary["offline_led_triangle_count"]
        or m3_chassis_triangle_count != m3_summary["cell_chassis_triangle_count"]
        or len(m3_supported_cell_ids) != m3_summary["supported_cell_id_count"]
        or len(m3_occupied_model_ids) != m3_summary["occupied_model_count"]
        or m3_orientation_contracts
        != {(facing, spin) for facing in DRIVE_ORIENTATION_ANGLES for spin in range(4)}
        or len(drive_component_pair) != 2
        or drive_component_variants != {False, True}
    ):
        raise EvidenceError("gallery M3a contents differ from their summary")
    if schema_version == 4:
        pair_contracts = [
            anchor.drive
            for case in cases
            for anchor in case.anchors
            if anchor.position in drive_component_pair
        ]
        if len(pair_contracts) != 2 or pair_contracts[0] != pair_contracts[1]:
            raise EvidenceError("M3 Drive component-insensitivity contracts differ")

    dense_value = value.get("optional_dense_fixture")
    if not isinstance(dense_value, dict) or dense_value.get("auto_build") is not False:
        raise EvidenceError("gallery has no opt-in-only dense fixture contract")
    dense_bounds_value = dense_value.get("cable_bounds")
    dense_material_value = dense_value.get("expected_material_triangles")
    dense_forceload_value = dense_value.get("exclusive_forceload_chunks")
    if not isinstance(dense_bounds_value, list) or len(dense_bounds_value) != 4:
        raise EvidenceError("dense fixture must declare four cable bounds")
    if not isinstance(dense_material_value, dict):
        raise EvidenceError("dense fixture has no exact material contract")
    dense_positions: set[tuple[int, int, int]] = set()
    for bounds_value in dense_bounds_value:
        if not isinstance(bounds_value, dict):
            raise EvidenceError("dense fixture bound is not an object")
        minimum = bounds_value.get("min")
        maximum = bounds_value.get("max")
        if (
            not isinstance(minimum, list)
            or not isinstance(maximum, list)
            or len(minimum) != 3
            or len(maximum) != 3
            or any(
                not isinstance(item, int) or isinstance(item, bool)
                for item in minimum + maximum
            )
            or tuple(maximum[index] - minimum[index] + 1 for index in range(3))
            != (8, 4, 8)
        ):
            raise EvidenceError("dense fixture bound is not an exact 8x4x8 lattice")
        for x in range(minimum[0], maximum[0] + 1):
            for y in range(minimum[1], maximum[1] + 1):
                for z in range(minimum[2], maximum[2] + 1):
                    position = (x, y, z)
                    if position in dense_positions or position in positions:
                        raise EvidenceError("dense fixture cells overlap")
                    dense_positions.add(position)
    expected_dense_materials = tuple(sorted(dense_material_value.items()))
    expected_dense_forceload_chunks = {
        (16, 11),
        (17, 11),
        (17, 12),
        (17, 14),
        (17, 15),
        (18, 11),
        (18, 12),
        (18, 14),
        (18, 15),
        (19, 11),
        (19, 12),
        (19, 14),
        (19, 15),
    }
    if (
        dense_value.get("requires_main_gallery_loaded") is not True
        or not isinstance(dense_forceload_value, list)
        or {
            (entry.get("x"), entry.get("z"))
            for entry in dense_forceload_value
            if isinstance(entry, dict)
        }
        != expected_dense_forceload_chunks
        or len(dense_forceload_value) != len(expected_dense_forceload_chunks)
    ):
        raise EvidenceError("dense fixture force-load ownership contract is invalid")
    if (
        dense_value.get("cell_count") != len(dense_positions)
        or len(dense_positions) != 1024
        or dense_value.get("expected_triangle_count") != 63_488
        or expected_dense_materials
        != (
            ("ae2:part/cable/core/dense_smart/transparent", 12_288),
            ("ae2:part/cable/dense_covered/transparent", 51_200),
        )
    ):
        raise EvidenceError("dense fixture totals differ from the exact retained M1 contract")

    contract = GalleryContract(
        cases=tuple(cases),
        expected_selected_resources=tuple(sorted(expected_selected_resources)),
        expected_custom_anchor_count=expected_custom_anchor_count,
        expected_custom_triangle_count=expected_custom_triangle_count,
        stock_fallback_positions=tuple(sorted(stock_fallback_positions)),
        dense_positions=tuple(sorted(dense_positions)),
        expected_dense_triangle_count=63_488,
        expected_dense_material_triangles=expected_dense_materials,
        schema_version=schema_version,
        signature_schema_version=signature_schema_version,
        m2_regression_positions=tuple(
            sorted(
                anchor.position
                for case in cases
                if case.milestone in {"M1", "M2"}
                for anchor in case.anchors
            )
        ),
        drive_component_pair=tuple(sorted(drive_component_pair)),
        m3a_regression_positions=(
            tuple(sorted(positions)) if schema_version == 4 else ()
        ),
        extended_drive_component_pair=(),
        extended_drive_mirror_pair=(),
        extension_positions=(),
    )
    return contract, {
        "sha256": digest,
        "schema_version": schema_version,
        "signature_schema_version": signature_schema_version,
        "case_count": len(cases),
        "anchor_count": len(positions),
        "profile": {
            "mod_id": "ae2",
            "version": "19.2.17",
            "coverage_milestone": "M2" if schema_version == 3 else "M3a",
            "transient_policy": "idle-off-unknown",
            "supported_center_part_count": len(supported_center_parts),
            "supported_face_part_count": 1,
            "selected_resource_count": len(selected_resources),
            "resolved_facade_resource_count": len(resolved_facade_resources),
            "custom_material_count": len(expected_selected_resources),
            "texture_manifest_sha256": profile.get("texture_manifest_sha256"),
        },
        "expected_custom_summary": {
            "anchor_count": expected_custom_anchor_count,
            "triangle_count": expected_custom_triangle_count,
            "selected_resource_count": len(expected_selected_resources),
        },
        "expected_stock_fallback_summary": {
            "anchor_count": expected_fallback_count,
            "triangle_count": 0,
        },
        "m1_regression_summary": m1_summary,
        "m2_summary": m2_summary,
        **({"m3_summary": m3_summary} if schema_version == 4 else {}),
        "optional_dense_fixture": {
            "auto_build": False,
            "cell_count": len(dense_positions),
            "expected_triangle_count": 63_488,
            "expected_material_triangles": dict(expected_dense_materials),
        },
    }


def _schema4_view(schema5: dict[str, Any]) -> dict[str, Any]:
    """Return the exact accepted M3a contract embedded by schema 5."""
    view = json.loads(json.dumps(schema5))
    view["schema_version"] = 4
    view["signature_schema_version"] = 4
    view["cases"] = [
        case
        for case in view["cases"]
        if case.get("milestone") != "M3b"
    ]
    for case in view["cases"]:
        case.pop("milestone", None)
        case.pop("route", None)
    view["case_count"] = 76
    view["anchor_count"] = 323
    view["expected_custom_summary"] = {
        "anchor_count": 310,
        "selected_resource_count": 159,
        "triangle_count": 12_432,
    }
    view["expected_stock_fallback_summary"] = {
        "anchor_count": 12,
        "triangle_count": 0,
    }
    view.pop("m3b_summary", None)
    view.pop("m3b_floor_policy", None)
    profile = view["profile"]
    profile["coverage_milestone"] = "M3a"
    profile["selected_resources"] = profile["core_resources"] + profile["drive_resources"]
    profile.pop("supported_extended_drive", None)
    profile.pop("extension_profiles", None)
    return view


def _schema5_view(schema6: dict[str, Any]) -> dict[str, Any]:
    """Return the frozen accepted M3b contract embedded by schema 6."""
    view = json.loads(json.dumps(schema6))
    view["schema_version"] = 5
    view["signature_schema_version"] = 5
    view["cases"] = [
        case for case in view["cases"] if case.get("milestone") != "M3c"
    ]
    view["case_count"] = 92
    view["anchor_count"] = 359
    view["expected_custom_summary"] = {
        "anchor_count": 342,
        "selected_resource_count": 167,
        "triangle_count": 17_488,
    }
    view["expected_stock_fallback_summary"] = {
        "anchor_count": 16,
        "triangle_count": 0,
    }
    view.pop("m3c_summary", None)
    view.pop("m3c_floor_policy", None)
    bounds = view.get("bounds", {})
    for key in ("m3c_fixture", "m3c_support_floor", "m3c_air"):
        bounds.pop(key, None)
    profile = view["profile"]
    profile["coverage_milestone"] = "M3b"
    profile["selected_resources"] = (
        profile["core_resources"]
        + profile["drive_resources"]
        + list(EXTENDED_DRIVE_SELECTED_RESOURCES)
    )
    profile.pop("supported_connected_glass", None)
    profile.pop("glass_resources", None)
    return view


def _schema6_view(schema7: dict[str, Any]) -> dict[str, Any]:
    """Return the byte-frozen accepted M3c contract embedded by schema 7."""
    view = json.loads(json.dumps(schema7))
    view["schema_version"] = 6
    view["signature_schema_version"] = 6
    view["cases"] = [
        case for case in view["cases"] if case.get("milestone") != "M3d"
    ]
    view["case_count"] = 103
    view["anchor_count"] = 406
    view["expected_custom_summary"] = {
        "anchor_count": 389,
        "selected_resource_count": 186,
        "triangle_count": 18_264,
    }
    view["expected_stock_fallback_summary"] = {
        "anchor_count": 16,
        "triangle_count": 0,
    }
    view.pop("m3d_summary", None)
    view.pop("m3d_floor_policy", None)
    bounds = view.get("bounds", {})
    for key in ("m3d_fixture", "m3d_support_floor", "m3d_air"):
        bounds.pop(key, None)
    profile = view["profile"]
    profile["coverage_milestone"] = "M3c"
    profile["selected_resources"] = profile["selected_resources"][:-15]
    for key in (
        "supported_formed_crafting",
        "crafting_resources",
        "crafting_resource_manifest_sha256",
        "crafting_texture_manifest_sha256",
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


def _schema11_view(schema12: dict[str, Any]) -> dict[str, Any]:
    """Project schema 12 to the byte-frozen accepted schema-11 manifest."""
    view = json.loads(json.dumps(schema12))
    profile = view.get("profile")
    cases = view.get("cases")
    if not isinstance(profile, dict) or not isinstance(cases, list):
        raise EvidenceError("schema-12 Applied Mekanistics projection metadata is missing")
    if (
        view.get("case_count") != 162
        or view.get("anchor_count") != 1_373
        or len(cases) != 162
        or len(profile.get("selected_resources", ())) != 434
        or profile.get("selected_resources", ())[-1:] != [APPMEK_DRIVE_TEXTURE]
    ):
        raise EvidenceError("schema-12 accepted schema-11 projection identity changed")
    view["schema_version"] = 11
    view["signature_schema_version"] = 11
    view["case_count"] = 158
    view["anchor_count"] = 1_366
    view["cases"] = cases[:-4]
    profile["coverage_milestone"] = "M5-cumulative-review"
    profile["selected_resources"] = profile["selected_resources"][:-1]
    profile.pop("appmek_routes", None)
    profile.pop("supported_applied_mekanistics", None)
    view.get("bounds", {}).pop("appmek_fixture", None)
    for key in (
        "appmek_floor_policy",
        "appmek_review_summary",
        "appmek_verification_policy",
    ):
        view.pop(key, None)
    return view


def _schema10_view(schema11: dict[str, Any]) -> dict[str, Any]:
    """Project schema 11 to the byte-frozen accepted schema-10 manifest."""
    view = json.loads(json.dumps(schema11))
    summary = view.get("m45_review_summary")
    profile = view.get("profile")
    if not isinstance(summary, dict) or not isinstance(profile, dict):
        raise EvidenceError("schema-11 M4/M5 projection metadata is missing")
    base_case_count = summary.get("base_schema10_case_count")
    base_anchor_count = summary.get("base_schema10_anchor_count")
    base_resource_count = summary.get("base_schema10_selected_resource_count")
    selected_resources = profile.get("selected_resources")
    if (
        base_case_count != 150
        or base_anchor_count != 957
        or base_resource_count != 289
        or not isinstance(selected_resources, list)
        or len(selected_resources) < base_resource_count
    ):
        raise EvidenceError("schema-11 accepted schema-10 projection identity changed")
    view["schema_version"] = 10
    view["signature_schema_version"] = 10
    view["case_count"] = base_case_count
    view["anchor_count"] = base_anchor_count
    view["cases"] = [
        case
        for case in view.get("cases", [])
        if not str(case.get("case_id", "")).startswith("ae2-m45-")
    ]
    profile["coverage_milestone"] = NATIVE_STRUCTURAL_COVERAGE
    profile["selected_resources"] = selected_resources[:base_resource_count]
    profile.pop("m45_routes", None)
    bounds = view.get("bounds")
    if isinstance(bounds, dict):
        bounds.pop("m45_fixture", None)
    for key in (
        "m45_floor_policy",
        "m45_legacy_upgrades",
        "m45_review_summary",
        "m45_unit_only_mutations",
    ):
        view.pop(key, None)
    return view


def _schema9_view(schema10: dict[str, Any]) -> dict[str, Any]:
    """Return the byte-frozen accepted M3f contract embedded by schema 10."""
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


def _schema8_view(schema9: dict[str, Any]) -> dict[str, Any]:
    """Return the byte-frozen accepted M3e contract embedded by schema 9."""
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
    # Schema 8 predated M3f's shared chunk (19,14), so retain the accepted
    # dense-exclusive projection byte-for-byte even though schema 9 correctly
    # removes every chunk touched by M3F_FIXTURE_BOUNDS.
    view["optional_dense_fixture"]["exclusive_forceload_chunks"] = [
        {"x": x, "z": z}
        for x, z in (
            (16, 11), (17, 11), (17, 12), (17, 14), (17, 15),
            (18, 11), (18, 12), (18, 14), (18, 15),
            (19, 11), (19, 12), (19, 14), (19, 15),
        )
    ]
    return view


def _schema7_view(schema8: dict[str, Any]) -> dict[str, Any]:
    """Return the byte-frozen accepted M3d contract embedded by schema 8."""
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
    profile.pop("supported_quantum_bridge", None)
    profile.pop("quantum_resources", None)
    return view


def _signed_integer(value: int, bits: int) -> int:
    mask = (1 << bits) - 1
    value &= mask
    sign = 1 << (bits - 1)
    return value - (1 << bits) if value & sign else value


def _connected_glass_base_selection(
    position: tuple[int, int, int],
) -> dict[str, Any]:
    x, y, z = position
    value = _signed_integer(
        _signed_integer(x * 3_129_871, 32)
        ^ _signed_integer(z * 116_129_781, 64)
        ^ y,
        64,
    )
    value = _signed_integer(
        _signed_integer(
            _signed_integer(value * value, 64) * 42_317_861,
            64,
        )
        + _signed_integer(value * 11, 64),
        64,
    )
    seed = value >> 16
    random_mask = (1 << 48) - 1
    state = (seed ^ 0x5DEECE66D) & random_mask

    def next_int_4() -> int:
        nonlocal state
        state = (state * 0x5DEECE66D + 0xB) & random_mask
        return (((state >> 17) * 4) >> 31)

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


def _expected_connected_glass_metadata(
    position: tuple[int, int, int],
    glass_positions: set[tuple[int, int, int]],
    opaque_positions: set[tuple[int, int, int]],
) -> tuple[dict[str, Any], list[dict[str, Any]], dict[str, int], list[dict[str, str]], list[str]]:
    selection = _connected_glass_base_selection(position)
    faces: list[dict[str, Any]] = []
    materials: Counter[str] = Counter()
    connections: list[dict[str, str]] = []
    opaque_culled: list[str] = []
    for direction, delta in DIRECTION_VECTORS.items():
        adjacent = tuple(position[index] + delta[index] for index in range(3))
        if adjacent in glass_positions:
            connections.append({"direction": direction, "kind": "connected-glass"})
            continue
        if adjacent in opaque_positions:
            opaque_culled.append(direction)
            continue
        bit_directions = CONNECTED_GLASS_FACE_BIT_DIRECTIONS[direction]
        frame_mask_value = sum(
            (
                0
                if tuple(
                    position[axis] + DIRECTION_VECTORS[bit_direction][axis]
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
    return selection, faces, dict(sorted(materials.items())), connections, opaque_culled


def _crafting_rgb(value: int) -> tuple[int, int, int]:
    return ((value >> 16) & 0xFF, (value >> 8) & 0xFF, value & 0xFF)


def _crafting_primitive_metadata(
    role: str,
    resource: str,
    bounds: Sequence[float],
    *,
    rgb: tuple[int, int, int] = (255, 255, 255),
    emissive: bool = False,
) -> dict[str, Any]:
    return {
        "role": role,
        "resource": resource,
        "bounds_sixteenths": list(bounds),
        "rgb_u8": list(rgb),
        "ambient_occlusion_raw_u8": 255,
        "light_policy": "fullbright-15" if emissive else "world-derived-face-light",
        "triangle_count": 2,
    }


def _expected_crafting_metadata(
    position: tuple[int, int, int],
    kind: str,
    powered: bool,
    facing: str | None,
    painted_color_ordinal: int | None,
    crafting_positions: set[tuple[int, int, int]],
) -> tuple[list[dict[str, Any]], dict[str, int], list[dict[str, str]]]:
    """Independently reproduce AE2 19.2.17's formed-cube face topology."""
    connected = {
        direction
        for direction, delta in DIRECTION_VECTORS.items()
        if tuple(position[index] + delta[index] for index in range(3))
        in crafting_positions
    }
    connections = [
        {"direction": direction, "kind": "crafting-unit"}
        for direction in DIRECTION_VECTORS
        if direction in connected
    ]
    faces: list[dict[str, Any]] = []
    materials: Counter[str] = Counter()
    for side in DIRECTION_VECTORS:
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
                _crafting_primitive_metadata(
                    "ring-corner:" + ",".join(corner),
                    "ae2:block/crafting/ring_corner",
                    bounds,
                )
            )
        for stripe in DIRECTION_VECTORS:
            if stripe in {side, CRAFTING_OPPOSITES[side]} or stripe in connected:
                continue
            bounds: list[float] = [0, 0, 0, 16, 16, 16]
            stripe_axis = CRAFTING_DIRECTION_AXES[stripe]
            stripe_index = {"x": 0, "y": 1, "z": 2}[stripe_axis]
            if stripe in {"down", "north", "west"}:
                bounds[stripe_index + 3] = 3
            else:
                bounds[stripe_index] = 13
            third_axis = (
                {"x", "y", "z"}
                - {CRAFTING_DIRECTION_AXES[side], stripe_axis}
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
                CRAFTING_DIRECTION_AXES[side] != "y" and stripe_axis in {"x", "z"}
            ) or (
                CRAFTING_DIRECTION_AXES[side] == "y" and stripe_axis == "x"
            )
            primitives.append(
                _crafting_primitive_metadata(
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
        side_index = {"x": 0, "y": 1, "z": 2}[
            CRAFTING_DIRECTION_AXES[side]
        ]
        inner[side_index] = 0
        inner[side_index + 3] = 16
        if kind == "unit":
            layers = (
                ("unit-base", "ae2:block/crafting/unit_base", (255, 255, 255), False),
            )
        elif kind == "monitor" and side != facing:
            layers = (
                ("monitor-chassis", "ae2:block/crafting/unit_base", (255, 255, 255), False),
            )
        elif kind == "monitor":
            if painted_color_ordinal not in range(17):
                raise EvidenceError("crafting monitor paint ordinal is outside 0..16")
            layers = (
                ("monitor-base", "ae2:block/crafting/monitor_base", (255, 255, 255), False),
                (
                    "monitor-bright",
                    "ae2:block/crafting/monitor_light_bright",
                    _crafting_rgb(CRAFTING_PAINT_BRIGHT[painted_color_ordinal]),
                    powered,
                ),
                (
                    "monitor-medium",
                    "ae2:block/crafting/monitor_light_medium",
                    _crafting_rgb(CRAFTING_PAINT_MEDIUM[painted_color_ordinal]),
                    powered,
                ),
                (
                    "monitor-dark",
                    "ae2:block/crafting/monitor_light_dark",
                    _crafting_rgb(CRAFTING_PAINT_DARK[painted_color_ordinal]),
                    powered,
                ),
            )
        else:
            layers = (
                ("light-base", "ae2:block/crafting/light_base", (255, 255, 255), False),
                (
                    "light-overlay",
                    CRAFTING_LIGHT_RESOURCE_BY_KIND[kind],
                    (255, 255, 255),
                    powered,
                ),
            )
        primitives.extend(
            _crafting_primitive_metadata(
                role, resource, inner, rgb=rgb, emissive=emissive
            )
            for role, resource, rgb, emissive in layers
        )
        for primitive in primitives:
            materials[primitive["resource"]] += 2
        faces.append(
            {
                "direction": side,
                "primitives": primitives,
                "triangle_count": 2 * len(primitives),
            }
        )
    return faces, dict(sorted(materials.items())), connections


def _m3d_layouts() -> tuple[dict[str, Any], ...]:
    """Return the exact nine M3d cases without consulting generated metadata."""
    isolated = tuple(
        {"position": (x, 100, 261), "block_id": block_id}
        for x, block_id in zip(
            (297, 301, 305, 309, 313),
            (
                "ae2:1k_crafting_storage",
                "ae2:4k_crafting_storage",
                "ae2:16k_crafting_storage",
                "ae2:64k_crafting_storage",
                "ae2:256k_crafting_storage",
            ),
            strict=True,
        )
    )
    powered = (
        {"position": (297, 100, 269), "block_id": "ae2:crafting_unit", "powered": True},
        {"position": (298, 100, 269), "block_id": "ae2:crafting_accelerator", "powered": True},
        {"position": (297, 100, 270), "block_id": "ae2:1k_crafting_storage", "powered": True},
        {"position": (298, 100, 270), "block_id": "ae2:4k_crafting_storage", "powered": True},
        {"position": (297, 101, 269), "block_id": "ae2:16k_crafting_storage", "powered": True},
        {"position": (298, 101, 269), "block_id": "ae2:64k_crafting_storage", "powered": True},
        {"position": (297, 101, 270), "block_id": "ae2:256k_crafting_storage", "powered": True},
        {
            "position": (298, 101, 270),
            "block_id": "ae2:crafting_monitor",
            "powered": True,
            "facing": "up",
            "spin": 0,
            "paint": 16,
        },
    )
    hard_cube = tuple(
        {
            "position": (x, y, z),
            "block_id": (
                "ae2:1k_crafting_storage"
                if (x, y, z) == (305, 102, 269)
                else "ae2:crafting_unit"
            ),
        }
        for y in range(100, 103)
        for z in range(269, 272)
        for x in range(304, 307)
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
    monitor_pairs: list[dict[str, Any]] = []
    for ordinal, (position, facing) in enumerate(
        zip(monitor_positions, monitor_facings, strict=True)
    ):
        monitor_pairs.append(
            {
                "position": position,
                "block_id": "ae2:crafting_monitor",
                "facing": facing,
                "spin": ordinal % 4,
                "paint": ordinal,
            }
        )
        delta = DIRECTION_VECTORS[CRAFTING_OPPOSITES[facing]]
        monitor_pairs.append(
            {
                "position": tuple(position[index] + delta[index] for index in range(3)),
                "block_id": "ae2:1k_crafting_storage",
            }
        )
    return (
        {
            "label": "isolated-storage-catalog",
            "category": "crafting-storage-catalog",
            "anchors": isolated,
            "fixtures": (),
        },
        {
            "label": "unit-plus-1k-storage",
            "category": "crafting-axis-pair",
            "anchors": (
                {"position": (297, 100, 265), "block_id": "ae2:crafting_unit"},
                {"position": (298, 100, 265), "block_id": "ae2:1k_crafting_storage"},
            ),
            "fixtures": (),
        },
        {
            "label": "accelerator-plus-1k-storage",
            "category": "crafting-axis-pair",
            "anchors": (
                {"position": (302, 100, 265), "block_id": "ae2:crafting_accelerator"},
                {"position": (303, 100, 265), "block_id": "ae2:1k_crafting_storage"},
            ),
            "fixtures": (),
        },
        {
            "label": "unit-storage-accelerator-line",
            "category": "crafting-axis-line",
            "anchors": (
                {"position": (307, 100, 265), "block_id": "ae2:crafting_unit"},
                {"position": (308, 100, 265), "block_id": "ae2:1k_crafting_storage"},
                {"position": (309, 100, 265), "block_id": "ae2:crafting_accelerator"},
            ),
            "fixtures": (),
        },
        {
            "label": "two-by-two-plane",
            "category": "crafting-plane",
            "anchors": (
                {"position": (312, 100, 264), "block_id": "ae2:crafting_unit"},
                {"position": (313, 100, 264), "block_id": "ae2:crafting_accelerator"},
                {"position": (312, 100, 265), "block_id": "ae2:1k_crafting_storage"},
                {"position": (313, 100, 265), "block_id": "ae2:4k_crafting_storage"},
            ),
            "fixtures": (),
        },
        {
            "label": "powered-two-by-two-by-two-all-eight",
            "category": "crafting-powered-cube",
            "anchors": powered,
            "fixtures": (
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
        },
        {
            "label": "unpowered-three-by-three-by-three-hard-culling",
            "category": "crafting-hard-culling-cube",
            "anchors": hard_cube,
            "fixtures": (),
        },
        {
            "label": "monitor-paint-orientation-catalog",
            "category": "crafting-monitor-catalog",
            "anchors": tuple(monitor_pairs),
            "fixtures": (),
        },
        {
            "label": "compatible-extension-atomic-fallback",
            "category": "crafting-compatible-extension-fallback",
            "anchors": (
                {
                    "position": (318, 100, 261),
                    "block_id": "ae2:1k_crafting_storage",
                    "expected_path": "stock-fallback-m3d",
                },
            ),
            "fixtures": (
                {
                    "position": (317, 100, 261),
                    "block_id": "megacells:mega_crafting_unit",
                    "expected_block_entity_id": "megacells:mega_crafting_unit",
                    "expected_state": {"formed": True, "powered": False},
                    "purpose": "compatible-crafting-neighbor-context",
                },
                {
                    "position": (319, 100, 261),
                    "block_id": "expandedae:exp_crafting_unit",
                    "expected_block_entity_id": "expandedae:exp_cpus",
                    "expected_state": {"formed": True, "powered": False},
                    "purpose": "compatible-crafting-neighbor-context",
                },
            ),
        },
    )


def _manifest_position(position: tuple[int, int, int]) -> dict[str, int]:
    return dict(zip(("x", "y", "z"), position, strict=True))


def _manifest_fixture(fixture: dict[str, Any]) -> dict[str, Any]:
    return {
        key: (_manifest_position(value) if key == "position" else value)
        for key, value in fixture.items()
    }


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


def _quantum_primitive_metadata(
    role: str, resource: str, bounds: Sequence[float]
) -> dict[str, Any]:
    return {
        "role": role,
        "resource": resource,
        "bounds_sixteenths": list(bounds),
        "rgb_u8": [255, 255, 255],
        "ambient_occlusion_raw_u8": 255,
        "light_policy": "world-derived-face-light",
        "triangle_count": 12,
    }


def _expected_quantum_metadata(
    position: tuple[int, int, int],
    role: str,
    quantum_positions: set[tuple[int, int, int]],
) -> tuple[list[dict[str, Any]], dict[str, int], list[dict[str, str]]]:
    connected = [
        direction
        for direction, delta in DIRECTION_VECTORS.items()
        if tuple(position[index] + delta[index] for index in range(3))
        in quantum_positions
    ]
    connections = [
        {"direction": direction, "kind": "quantum-bridge"}
        for direction in connected
    ]
    primitives: list[dict[str, Any]] = []
    if role == "link":
        if len(connected) != 4:
            raise EvidenceError(f"quantum link {position} must have four neighbors")
        primitives.append(
            _quantum_primitive_metadata(
                "link-center", QUANTUM_LINK_RESOURCE, (2, 2, 2, 14, 14, 14)
            )
        )
        for direction in connected:
            primitives.append(
                _quantum_primitive_metadata(
                    f"link-glass:{direction}",
                    QUANTUM_GLASS_RESOURCE,
                    _quantum_branch_bounds(direction, 6.24, 9.76, 3.984, 12.016),
                )
            )
            primitives.append(
                _quantum_primitive_metadata(
                    f"link-covered:{direction}",
                    QUANTUM_COVERED_RESOURCE,
                    _quantum_branch_bounds(direction, 4.992, 11.008, 1.992, 14.008),
                )
            )
    elif role == "corner":
        if len(connected) != 2:
            raise EvidenceError(f"quantum corner {position} must have two neighbors")
        primitives.append(
            _quantum_primitive_metadata(
                "ring-corner-center", QUANTUM_RING_RESOURCE, (2, 2, 2, 14, 14, 14)
            )
        )
        for direction in connected:
            primitives.append(
                _quantum_primitive_metadata(
                    f"ring-corner-covered:{direction}",
                    QUANTUM_COVERED_RESOURCE,
                    _quantum_branch_bounds(direction, 4.992, 11.008, 4.192, 11.808),
                )
            )
    elif role == "edge":
        if len(connected) != 3:
            raise EvidenceError(f"quantum edge {position} must have three neighbors")
        for axis, bounds in (
            ("x", (0, 2, 2, 16, 14, 14)),
            ("y", (2, 0, 2, 14, 16, 14)),
            ("z", (2, 2, 0, 14, 14, 16)),
        ):
            primitives.append(
                _quantum_primitive_metadata(
                    f"ring-edge-axis:{axis}", QUANTUM_RING_RESOURCE, bounds
                )
            )
    else:
        raise EvidenceError(f"unknown quantum role at {position}: {role}")
    materials: Counter[str] = Counter()
    for primitive in primitives:
        materials[primitive["resource"]] += primitive["triangle_count"]
    return primitives, dict(sorted(materials.items())), connections


def _m3e_layouts() -> tuple[dict[str, Any], ...]:
    definitions = (
        ("formed-unpowered-xz-chunk-boundary", "xz", (287, 100, 271)),
        ("formed-unpowered-xy", "xy", (283, 101, 276)),
        ("formed-unpowered-yz", "yz", (290, 101, 271)),
    )
    layouts: list[dict[str, Any]] = []
    for label, plane, center in definitions:
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
                anchors.append({"position": tuple(position), "role": role})
        layouts.append(
            {
                "label": label,
                "plane": plane,
                "center": center,
                "anchors": tuple(anchors),
            }
        )
    return tuple(layouts)


def _s1_xyz(raw_position: Any, description: str) -> tuple[int, int, int]:
    if (
        not isinstance(raw_position, dict)
        or set(raw_position) != {"x", "y", "z"}
        or any(
            not isinstance(raw_position[axis], int)
            or isinstance(raw_position[axis], bool)
            for axis in ("x", "y", "z")
        )
    ):
        raise EvidenceError(f"{description} is not exact xyz metadata")
    return tuple(raw_position[axis] for axis in ("x", "y", "z"))


def _native_structural_cable_family(cable_id: Any) -> str:
    if not isinstance(cable_id, str) or not cable_id.startswith("ae2:"):
        raise EvidenceError("native endpoint anchor lacks an exact cable ID")
    suffixes = (
        ("_covered_dense_cable", "dense_covered"),
        ("_smart_dense_cable", "dense_smart"),
        ("_glass_cable", "glass"),
        ("_covered_cable", "covered"),
        ("_smart_cable", "smart"),
    )
    for suffix, family in suffixes:
        if cable_id.endswith(suffix):
            return family
    raise EvidenceError(f"unknown native endpoint cable family: {cable_id}")


def _validate_native_structural_whitelist_facade_state(
    state: dict[str, Any],
) -> None:
    """Enforce the pinned complete BlockState schema for explicit whitelist IDs."""

    block_id = state.get("Name")
    if block_id not in NATIVE_STRUCTURAL_FACADE_WHITELIST_IDS:
        return
    if set(state) - {"Name", "Properties"}:
        raise EvidenceError(
            f"S1 whitelist facade {block_id} has unexpected state fields"
        )
    properties = state.get("Properties", {})
    schema = NATIVE_STRUCTURAL_FACADE_WHITELIST_STATE_SCHEMAS[block_id]
    if not isinstance(properties, dict) or set(properties) != set(schema):
        raise EvidenceError(
            f"S1 whitelist facade {block_id} lacks the exact property-key set"
        )
    for key, value in properties.items():
        if not isinstance(value, str) or value not in schema[key]:
            raise EvidenceError(
                f"S1 whitelist facade {block_id} property {key} is out of domain"
            )


def _schema9_disabled_source_projection(
    raw_anchor: dict[str, Any],
    fixture_by_position: dict[tuple[int, int, int], dict[str, Any]],
) -> dict[str, Any]:
    """Independently derive the accepted predecessor route for one S1 anchor."""

    empty = {
        "expected_path": "stock-empty",
        "expected_triangle_count": 0,
        "expected_material_triangles": {},
        "expected_smart_overlays": {},
        "expected_terminal_layers": {},
    }
    cable_id = raw_anchor.get("cable_id")
    if cable_id is None:
        return empty
    try:
        family = _native_structural_cable_family(cable_id)
    except EvidenceError:
        return empty
    if not cable_id.startswith("ae2:fluix_"):
        raise EvidenceError("S1 predecessor source gate expects the fixed fluix matrix")

    raw_parts = raw_anchor.get("face_parts", [])
    raw_facades = raw_anchor.get("facades", [])
    if not isinstance(raw_parts, list) or not isinstance(raw_facades, list):
        return empty
    parts: list[dict[str, Any]] = []
    for part in raw_parts:
        if (
            not isinstance(part, dict)
            or part.get("direction") not in DIRECTION_VECTORS
            or part.get("id") != "ae2:terminal"
            or not isinstance(part.get("spin"), int)
            or isinstance(part.get("spin"), bool)
            or part["spin"] not in range(4)
        ):
            return empty
        # The accepted schema-9 BlueNBT DTO retains id/spin only; additional
        # source fields are intentionally ignored by this predecessor route.
        parts.append(
            {
                "direction": part["direction"],
                "id": "ae2:terminal",
                "spin": part["spin"],
            }
        )
    if family.startswith("dense_") and parts:
        return empty

    if raw_facades:
        if (
            len(raw_facades) != 1
            or len(parts) != 1
            or not isinstance(raw_facades[0], dict)
            or raw_facades[0].get("direction") != parts[0]["direction"]
            or raw_facades[0].get("block_state")
            != {"Name": "minecraft:stone"}
        ):
            return empty

    position = _s1_xyz(raw_anchor.get("position"), "S1 predecessor anchor position")
    for delta in DIRECTION_VECTORS.values():
        neighbor = fixture_by_position.get(
            tuple(position[axis] + delta[axis] for axis in range(3))
        )
        if neighbor is None:
            continue
        if (
            neighbor.get("block_id") in {"minecraft:air", "minecraft:stone"}
            and neighbor.get("expected_block_entity_id") is None
        ):
            continue
        return empty

    # Smart cable cores intentionally use the covered core material; only the
    # connection arms carry the smart channel overlays.  Dense covered and
    # dense smart likewise share the dense-smart core model in schema 9.
    core_family = {
        "glass": "glass",
        "covered": "covered",
        "smart": "covered",
        "dense_covered": "dense_smart",
        "dense_smart": "dense_smart",
    }[family]
    materials: Counter[str] = Counter(
        {f"ae2:part/cable/core/{core_family}/transparent": 12}
    )
    overlays: dict[str, Any] = {}
    terminal_layers: dict[str, Any] = {}
    if parts:
        part_count = len(parts)
        materials[f"ae2:part/cable/{family}/transparent"] += 12 * part_count
        if family in {"smart", "dense_smart"}:
            for suffix, rgb in (
                ("channels_00", [90, 71, 158]),
                ("channels_10", [226, 163, 227]),
            ):
                resource = f"ae2:part/cable/{family}/{suffix}"
                materials[resource] += 12 * part_count
                overlays[resource] = {
                    "rgb_u8": rgb,
                    "blocklight_raw_i8": 15,
                    "sunlight_raw_i8": 15,
                }
        for resource, count in NATIVE_STRUCTURAL_SCHEMA9_TERMINAL_MATERIALS.items():
            materials[resource] += count * part_count
        for resource, rgb in (
            ("ae2:part/terminal_bright", [226, 163, 227]),
            ("ae2:part/terminal_dark", [90, 71, 158]),
            ("ae2:part/terminal_medium", [145, 93, 205]),
        ):
            terminal_layers[resource] = {
                "rgb_u8": rgb,
                "emissive": False,
                "triangle_count_per_part": 2,
            }
    if raw_facades:
        materials["minecraft:block/stone"] += 48
    return {
        "expected_path": "custom-m2" if parts else "custom-m1",
        "expected_triangle_count": sum(materials.values()),
        "expected_material_triangles": dict(sorted(materials.items())),
        "expected_smart_overlays": dict(sorted(overlays.items())),
        "expected_terminal_layers": dict(sorted(terminal_layers.items())),
    }


def _validate_s1_facade_gallery_source_fixtures(
    raw_cases: Sequence[dict[str, Any]],
) -> None:
    """Validate the fixed physical facade representatives without an oracle."""
    mask_cases = raw_cases[13:19]
    mask_anchors = [
        anchor for case in mask_cases for anchor in case.get("anchors", ())
    ]
    if len(mask_anchors) != 64:
        raise EvidenceError("S1 facade-mask source catalog changed")
    neutral_by_id = {
        block_id: (properties, family, source_model, blockstate_sha256)
        for block_id, properties, family, source_model, blockstate_sha256
        in NATIVE_STRUCTURAL_NEUTRAL_FACADE_MATERIALS
    }
    vanilla_controls = {
        "minecraft:chiseled_bookshelf": (
            {
                "facing": "north",
                **{f"slot_{slot}_occupied": "false" for slot in range(6)},
            },
            "empty-chiseled-bookshelf",
            True,
        ),
        "minecraft:furnace": (
            {"facing": "north", "lit": "false"},
            "unlit-north-furnace",
            True,
        ),
        "minecraft:soul_sand": (
            {}, "property-free-opaque-soul-sand", True
        ),
        "minecraft:honey_block": (
            {}, "property-free-transparent-honey-block", False
        ),
    }
    physical_ids: set[str] = set()
    for mask, anchor in enumerate(mask_anchors):
        # The generated manifest omits optional empty arrays.  Mask zero has
        # no installed facade, so normalize only that omitted representation
        # to the source matrix's canonical empty facade set.
        facades = anchor.get("facades", [])
        if not isinstance(facades, list):
            raise EvidenceError("S1 facade-mask facade array changed")
        expected_directions = {
            direction
            for bit, direction in enumerate(DIRECTION_VECTORS)
            if mask & (1 << bit)
        }
        if {facade.get("direction") for facade in facades} != expected_directions:
            raise EvidenceError(f"S1 facade mask {mask} direction projection changed")
        state_by_direction = {
            facade["direction"]: facade.get("block_state") for facade in facades
        }
        metadata = anchor.get("native_neutral_facade_materials", [])
        if not isinstance(metadata, list):
            raise EvidenceError("S1 native facade metadata array changed")
        metadata_by_direction = {entry.get("direction"): entry for entry in metadata}
        expected_metadata_directions: set[str] = set()
        for direction, state in state_by_direction.items():
            block_id = state.get("Name") if isinstance(state, dict) else None
            if block_id in NATIVE_STRUCTURAL_FACADE_WHITELIST_IDS:
                physical_ids.add(block_id)
            contract = neutral_by_id.get(block_id)
            if contract is None:
                continue
            expected_metadata_directions.add(direction)
            properties, family, source_model, blockstate_sha256 = contract
            entry = metadata_by_direction.get(direction)
            if (
                not isinstance(entry, dict)
                or entry.get("block_id") != block_id
                or entry.get("properties") != properties
                or entry.get("material_family") != family
                or entry.get("source_model") != source_model
                or entry.get("blockstate_sha256") != blockstate_sha256
                or entry.get("persisted_properties")
                != state.get("Properties", {})
            ):
                raise EvidenceError(f"S1 native facade source pin changed for {block_id}")
        if set(metadata_by_direction) != expected_metadata_directions:
            raise EvidenceError("S1 native facade metadata face projection changed")
        vanilla_metadata = anchor.get("facade_whitelist_controls", [])
        if not isinstance(vanilla_metadata, list):
            raise EvidenceError("S1 vanilla whitelist metadata array changed")
        vanilla_by_direction = {
            entry.get("direction"): entry for entry in vanilla_metadata
        }
        expected_vanilla_directions: set[str] = set()
        for direction, state in state_by_direction.items():
            block_id = state.get("Name") if isinstance(state, dict) else None
            control = vanilla_controls.get(block_id)
            if control is None:
                continue
            expected_vanilla_directions.add(direction)
            properties, control_name, is_solid_render = control
            entry = vanilla_by_direction.get(direction)
            if (
                state.get("Properties", {}) != properties
                or not isinstance(entry, dict)
                or entry.get("block_id") != block_id
                or entry.get("properties") != properties
                or entry.get("control") != control_name
                or entry.get("is_solid_render") is not is_solid_render
            ):
                raise EvidenceError(
                    f"S1 vanilla whitelist source pin changed for {block_id}"
                )
        if set(vanilla_by_direction) != expected_vanilla_directions:
            raise EvidenceError("S1 vanilla whitelist metadata face projection changed")

    if physical_ids != set(NATIVE_STRUCTURAL_PHYSICAL_FACADE_WHITELIST_IDS):
        raise EvidenceError("S1 physical facade whitelist coverage changed")
    expected_quartz = {
        1: "isolated",
        2: "adjacent-real-quartz",
        5: "same-block-perpendicular-quartz-vibrant-facades",
        63: "fully-surrounded-all-facade-quads-suppressed",
    }
    if any(
        mask_anchors[mask].get("quartz_facade_appearance_control") != control
        or mask_anchors[mask].get("quartz_facade_light_policy")
        != "non-emissive-facade-blocklight-zero"
        for mask, control in expected_quartz.items()
    ):
        raise EvidenceError("S1 quartz facade source controls changed")
    quartz_helpers = [
        fixture
        for case in mask_cases
        for fixture in case.get("fixture_blocks", ())
        if fixture.get("purpose")
        == "quartz-facade-adjacent-real-quartz-appearance"
    ]
    quartz_center = _s1_xyz(
        mask_anchors[2].get("position"), "S1 quartz appearance anchor"
    )
    if (
        len(quartz_helpers) != 1
        or _s1_xyz(quartz_helpers[0].get("position"), "S1 quartz appearance helper")
        != (quartz_center[0], quartz_center[1] + 1, quartz_center[2])
        or quartz_helpers[0].get("block_id") != "ae2:quartz_vibrant_glass"
        or quartz_helpers[0].get("expected_state") != {}
    ):
        raise EvidenceError("S1 adjacent real quartz source control changed")
    enclosed_helpers = [
        fixture
        for case in mask_cases
        for fixture in case.get("fixture_blocks", ())
        if fixture.get("purpose")
        == "quartz-facade-fully-surrounded-appearance"
    ]
    enclosed_center = _s1_xyz(
        mask_anchors[63].get("position"), "S1 enclosed quartz appearance anchor"
    )
    expected_enclosed = {
        tuple(
            enclosed_center[axis] + DIRECTION_VECTORS[direction][axis]
            for axis in range(3)
        ): direction
        for direction in DIRECTION_VECTORS
    }
    if (
        len(enclosed_helpers) != 6
        or {
            _s1_xyz(
                fixture.get("position"),
                "S1 enclosed quartz appearance helper",
            ): fixture.get("facade_direction")
            for fixture in enclosed_helpers
        }
        != expected_enclosed
        or any(
            fixture.get("block_id") != "ae2:quartz_glass"
            or fixture.get("expected_state") != {}
            for fixture in enclosed_helpers
        )
        or mask_anchors[63].get("facade_structural_expectation")
        != "zero-facade-layers-remains-custom-cable-plus-six-short-stilts"
    ):
        raise EvidenceError("S1 fully surrounded quartz source control changed")

    bookshelf_properties = {
        "facing": "north",
        **{f"slot_{slot}_occupied": "false" for slot in range(6)},
    }
    furnace_properties = {"facing": "north", "lit": "false"}
    if any(
        facade.get("block_state")
        != {"Name": "minecraft:chiseled_bookshelf", "Properties": bookshelf_properties}
        for facade in mask_anchors[13].get("facades", ())
    ) or any(
        facade.get("block_state")
        != {"Name": "minecraft:furnace", "Properties": furnace_properties}
        for facade in mask_anchors[14].get("facades", ())
    ):
        raise EvidenceError("S1 vanilla whitelist source controls changed")
    expected_normalization = {
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
    if mask_anchors[15].get("native_facade_normalization") != expected_normalization:
        raise EvidenceError("S1 crafting-monitor facade normalization changed")
    if (
        mask_anchors[17].get("facade_structural_expectation")
        != "honey-transparent-inset-against-opaque-stone"
        or mask_anchors[17].get("facades")
        != [
            {"direction": "down", "block_state": {"Name": "minecraft:honey_block"}},
            {"direction": "west", "block_state": {"Name": "minecraft:stone"}},
        ]
        or mask_anchors[18].get("facade_structural_expectation")
        != "honey-transparent-inset-against-opaque-soul-sand"
        or mask_anchors[18].get("facades")
        != [
            {"direction": "up", "block_state": {"Name": "minecraft:honey_block"}},
            {"direction": "west", "block_state": {"Name": "minecraft:soul_sand"}},
        ]
    ):
        raise EvidenceError("S1 soul-sand/honey transparency interaction changed")


def _validate_s1_endpoint_source_fixtures(raw_cases: Sequence[dict[str, Any]]) -> None:
    """Validate endpoint fixtures from hard-coded AE2 source facts.

    The runtime oracle is deliberately not consulted here.  This is the
    acceptance layer that prevents a renderer/exporter pair from blessing the
    same wrong side, state, block-entity identity or formed topology.
    """
    all_fixture_by_position: dict[tuple[int, int, int], dict[str, Any]] = {}
    for case in raw_cases:
        fixtures = case.get("fixture_blocks")
        if not isinstance(fixtures, list):
            raise EvidenceError("S1 fixture array changed")
        for fixture in fixtures:
            if not isinstance(fixture, dict):
                raise EvidenceError("S1 fixture is not an object")
            position = _s1_xyz(fixture.get("position"), "S1 fixture position")
            if position in all_fixture_by_position:
                raise EvidenceError("S1 fixture position is duplicated")
            all_fixture_by_position[position] = fixture

    # Independently require the complete persisted key set on every native
    # endpoint observation and its direct helper, including opposite straight
    # endpoints and known-disconnected controls.  The canonical policy states
    # below contain the exact key sets bound to the profile schemas; the later
    # profile gate independently validates every domain and Cartesian product.
    native_observation_count = 0
    for case in raw_cases:
        anchors = case.get("anchors")
        if not isinstance(anchors, list):
            raise EvidenceError("S1 anchor array changed")
        for anchor in anchors:
            if not isinstance(anchor, dict):
                raise EvidenceError("S1 anchor is not an object")
            center = _s1_xyz(anchor.get("position"), "S1 anchor position")
            observations = anchor.get("native_endpoints", [])
            if not isinstance(observations, list):
                raise EvidenceError("S1 endpoint observation array changed")
            for observation in observations:
                if not isinstance(observation, dict):
                    raise EvidenceError("S1 endpoint observation is not an object")
                block_id = observation.get("block_id")
                if not isinstance(block_id, str) or not block_id.startswith("ae2:"):
                    continue
                endpoint_name = block_id.removeprefix("ae2:")
                policy = NATIVE_STRUCTURAL_ENDPOINT_POLICIES.get(endpoint_name)
                if policy is None:
                    continue
                native_observation_count += 1
                required_state = observation.get("required_block_state")
                expected_keys = set(policy[1])
                if not isinstance(required_state, dict) or set(required_state) != expected_keys:
                    raise EvidenceError(
                        f"native endpoint state keys changed for {block_id}"
                    )
                direction = observation.get("direction")
                if direction not in DIRECTION_VECTORS:
                    raise EvidenceError(
                        f"native endpoint direction changed for {block_id}"
                    )
                helper_position = tuple(
                    center[axis] + DIRECTION_VECTORS[direction][axis]
                    for axis in range(3)
                )
                helper = all_fixture_by_position.get(helper_position)
                if (
                    helper is None
                    or helper.get("block_id") != block_id
                    or helper.get("expected_state") != required_state
                    or not isinstance(helper.get("placement_state"), dict)
                    or set(helper["placement_state"]) != expected_keys
                ):
                    raise EvidenceError(
                        f"native endpoint helper state changed for {block_id}"
                    )
    if native_observation_count != 35:
        raise EvidenceError(
            "native endpoint observation closure changed: "
            f"{native_observation_count} != 35"
        )

    endpoint_cases = raw_cases[22:25]
    endpoint_anchors = [
        anchor for case in endpoint_cases for anchor in case.get("anchors", ())
    ]
    if len(endpoint_anchors) != len(NATIVE_STRUCTURAL_ENDPOINTS_ORDERED):
        raise EvidenceError("native endpoint source fixture catalog changed")
    fixture_by_position: dict[tuple[int, int, int], dict[str, Any]] = {}
    for case in endpoint_cases:
        fixtures = case.get("fixture_blocks")
        if not isinstance(fixtures, list):
            raise EvidenceError("native endpoint fixture array changed")
        for fixture in fixtures:
            if not isinstance(fixture, dict):
                raise EvidenceError("native endpoint fixture is not an object")
            position = _s1_xyz(
                fixture.get("position"), "native endpoint fixture position"
            )
            if position in fixture_by_position:
                raise EvidenceError("native endpoint fixture position is duplicated")
            fixture_by_position[position] = fixture

    family_rank = {
        "glass": 0,
        "covered": 1,
        "smart": 2,
        "dense_covered": 3,
        "dense_smart": 4,
    }
    for anchor, (endpoint_name, declared_family) in zip(
        endpoint_anchors, NATIVE_STRUCTURAL_ENDPOINTS_ORDERED, strict=True
    ):
        if not isinstance(anchor, dict):
            raise EvidenceError("native endpoint anchor is not an object")
        center = _s1_xyz(anchor.get("position"), "native endpoint anchor position")
        observations = anchor.get("native_endpoints")
        dual = NATIVE_STRUCTURAL_DUAL_ENDPOINTS.get(endpoint_name)
        if (
            not isinstance(observations, list)
            or len(observations) != (2 if dual is not None else 1)
            or any(not isinstance(item, dict) for item in observations)
        ):
            raise EvidenceError(
                f"native endpoint observation list changed for ae2:{endpoint_name}"
            )
        block_entity_id, required_state, side_rule, formation, connected = (
            NATIVE_STRUCTURAL_ENDPOINT_POLICIES[endpoint_name]
        )
        local_family = _native_structural_cable_family(anchor.get("cable_id"))
        effective_family = min(
            (local_family, declared_family), key=family_rank.__getitem__
        )
        straight = dual is not None and dual[1]
        collar = (
            connected
            and not straight
            and effective_family not in {"glass", "dense_covered", "dense_smart"}
        )
        endpoint_id = f"ae2:{endpoint_name}"
        expected_primary = {
            "direction": "east",
            "block_id": endpoint_id,
            "block_entity_id": block_entity_id,
            "required_block_state": required_state,
            "observed_endpoint_side": "west",
            "side_rule": side_rule,
            "formation": formation,
            "exposed_on_observed_side": connected,
            "declared_family": declared_family,
            "local_family": local_family,
            "effective_family": effective_family,
            "collar": collar,
            "topology": (
                "native-grid-node-host"
                if connected
                else "known-native-grid-node-host-disconnected"
            ),
        }
        if observations[0] != expected_primary:
            raise EvidenceError(
                f"native endpoint source policy changed for {endpoint_id}"
            )

        primary_position = (center[0] + 1, center[1], center[2])
        primary_fixture = fixture_by_position.get(primary_position)
        if (
            primary_fixture is None
            or primary_fixture.get("block_id") != endpoint_id
            or primary_fixture.get("expected_block_entity_id") != block_entity_id
            or primary_fixture.get("expected_state") != required_state
            or primary_fixture.get("endpoint_catalog_id") != endpoint_id
            or primary_fixture.get("endpoint_observed_side") != "west"
            or primary_fixture.get("endpoint_side_rule") != side_rule
            or primary_fixture.get("endpoint_connected") is not connected
        ):
            raise EvidenceError(
                f"native endpoint helper evidence changed for {endpoint_id}"
            )

        if formation in {"qnb-yz-edge-ring", "qnb-yz-center-link"}:
            link_position = (
                primary_position
                if endpoint_name == "quantum_link"
                else (primary_position[0], primary_position[1] - 1, primary_position[2])
            )
            for y_offset in (-1, 0, 1):
                for z_offset in (-1, 0, 1):
                    position = (
                        link_position[0],
                        link_position[1] + y_offset,
                        link_position[2] + z_offset,
                    )
                    fixture = fixture_by_position.get(position)
                    is_link = y_offset == 0 and z_offset == 0
                    if (
                        fixture is None
                        or fixture.get("block_id")
                        != ("ae2:quantum_link" if is_link else "ae2:quantum_ring")
                        or fixture.get("expected_block_entity_id") != "ae2:quantum_ring"
                        or fixture.get("expected_state")
                        != {"formed": True, "waterlogged": False}
                        or fixture.get("placement_state")
                        != {"formed": False, "waterlogged": False}
                        or fixture.get("endpoint_structure") != formation
                    ):
                        raise EvidenceError(
                            f"formed QNB source fixture changed for {endpoint_id}"
                        )
        elif formation == "vertical-three-pylon-middle":
            for offset in (-1, 0, 1):
                fixture = fixture_by_position.get(
                    (primary_position[0], primary_position[1] + offset, primary_position[2])
                )
                if (
                    fixture is None
                    or fixture.get("block_id") != "ae2:spatial_pylon"
                    or fixture.get("expected_block_entity_id") != "ae2:spatial_pylon"
                    or fixture.get("expected_state") != {"powered_on": False}
                    or fixture.get("endpoint_structure") != formation
                ):
                    raise EvidenceError("formed spatial-pylon source fixture changed")
        elif formation == "vertical-crafting-pair":
            helper = fixture_by_position.get(
                (primary_position[0], primary_position[1] + 1, primary_position[2])
            )
            if (
                primary_fixture.get("placement_state")
                != {**required_state, "formed": False}
                or helper is None
                or helper.get("block_id") != "ae2:1k_crafting_storage"
                or helper.get("expected_block_entity_id") != "ae2:crafting_storage"
                or helper.get("expected_state")
                != {"formed": True, "powered": False}
                or helper.get("placement_state")
                != {"formed": False, "powered": False}
                or helper.get("endpoint_structure_role") != "storage-helper"
            ):
                raise EvidenceError(
                    f"formed crafting source fixture changed for {endpoint_id}"
                )
        elif formation == "single-storage-crafting-cpu":
            if (
                primary_fixture.get("placement_state")
                != {"formed": False, "powered": False}
                or primary_fixture.get("endpoint_structure") != formation
                or primary_fixture.get("endpoint_structure_role")
                != "endpoint-storage"
            ):
                raise EvidenceError(
                    "single-storage crafting source fixture changed"
                )

        if dual is None:
            continue
        dual_family, dual_straight, blocking_part = dual
        if local_family != dual_family:
            raise EvidenceError(f"dual endpoint cable family changed for {endpoint_id}")
        opposite_state = dict(required_state)
        if endpoint_name == "wireless_access_point":
            opposite_state["facing"] = "west"
        expected_opposite = dict(expected_primary)
        expected_opposite.update(
            {
                "direction": "west",
                "required_block_state": opposite_state,
                "observed_endpoint_side": "east",
            }
        )
        if observations[1] != expected_opposite:
            raise EvidenceError(
                f"opposite endpoint source policy changed for {endpoint_id}"
            )
        opposite_fixture = fixture_by_position.get(
            (center[0] - 1, center[1], center[2])
        )
        if (
            opposite_fixture is None
            or opposite_fixture.get("block_id") != endpoint_id
            or opposite_fixture.get("expected_block_entity_id") != block_entity_id
            or opposite_fixture.get("expected_state") != opposite_state
            or opposite_fixture.get("endpoint_observed_side") != "east"
            or opposite_fixture.get("endpoint_connected") is not True
        ):
            raise EvidenceError(
                f"opposite endpoint helper evidence changed for {endpoint_id}"
            )
        expected_straight = {
            "directions": ["west", "east"],
            "effective_family": dual_family,
            "enabled": dual_straight,
            "facades_are_attachments": False,
            "cable_anchor_requires_connection": False,
            "blocking_part": blocking_part,
            "machine_collars": False if dual_straight else collar,
        }
        if anchor.get("endpoint_straight_optimization") != expected_straight:
            raise EvidenceError(
                f"straight endpoint source policy changed for {endpoint_id}"
            )
        expected_parts = (
            [{"direction": "north", "id": "ae2:cable_anchor"}]
            if endpoint_name == "wireless_access_point"
            else [{"direction": "north", "id": "ae2:terminal", "spin": 0}]
            if endpoint_name == "molecular_assembler"
            else []
        )
        expected_facades = (
            [{"direction": "up", "block_state": {"Name": "minecraft:stone"}}]
            if endpoint_name in {"wireless_access_point", "energy_acceptor"}
            else []
        )
        if anchor.get("face_parts", []) != expected_parts or anchor.get(
            "facades", []
        ) != expected_facades:
            raise EvidenceError(
                f"straight endpoint attachment semantics changed for {endpoint_id}"
            )

    control_case = raw_cases[27]
    controls = control_case.get("anchors")
    control_fixtures = control_case.get("fixture_blocks")
    if (
        not isinstance(controls, list)
        or len(controls) != 3
        or not isinstance(control_fixtures, list)
    ):
        raise EvidenceError("S1 endpoint fallback controls changed")
    extension = controls[2]
    extension_position = _s1_xyz(
        extension.get("position"), "known-compatible extension anchor position"
    )
    expected_extension = {
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
    matching_helpers = [
        fixture
        for fixture in control_fixtures
        if fixture.get("purpose")
        == "known-compatible-extension-unknown-fallback"
    ]
    if (
        extension.get("expected_path") != "stock-fallback-s1"
        or extension.get("fallback_reason")
        != "known-compatible-extension-endpoint-unknown"
        or extension.get("native_endpoints") != [expected_extension]
        or len(matching_helpers) != 1
    ):
        raise EvidenceError("known-compatible extension UNKNOWN policy changed")
    helper = matching_helpers[0]
    if (
        _s1_xyz(helper.get("position"), "known-compatible extension helper position")
        != (extension_position[0] + 1, extension_position[1], extension_position[2])
        or helper.get("block_id")
        != NATIVE_STRUCTURAL_UNKNOWN_EXTENSION_ENDPOINT["block_id"]
        or helper.get("expected_block_entity_id")
        != NATIVE_STRUCTURAL_UNKNOWN_EXTENSION_ENDPOINT["block_entity_id"]
        or helper.get("expected_state")
        != NATIVE_STRUCTURAL_UNKNOWN_EXTENSION_ENDPOINT["required_state"]
        or helper.get("placement_state")
        != NATIVE_STRUCTURAL_UNKNOWN_EXTENSION_ENDPOINT["required_state"]
        or helper.get("endpoint_observed_side") != "west"
        or helper.get("endpoint_side_rule") != "UNSUPPORTED_COMPATIBLE_GRID_HOST"
        or helper.get("artifact")
        != NATIVE_STRUCTURAL_UNKNOWN_EXTENSION_ENDPOINT["artifact"]
        or helper.get("artifact_sha256")
        != NATIVE_STRUCTURAL_UNKNOWN_EXTENSION_ENDPOINT["artifact_sha256"]
    ):
        raise EvidenceError("known-compatible extension helper evidence changed")

    persistent_fallbacks = {
        _s1_xyz(anchor.get("position"), "persistent fallback position"): anchor
        for case in raw_cases
        for anchor in case.get("anchors", [])
        if anchor.get("expected_path") == "stock-fallback-s1"
    }
    expected_invalid_spin_parts = {
        (233, 100, 343): [
            {"direction": "north", "id": "ae2:monitor", "spin": 4}
        ],
        (260, 100, 358): [
            {"direction": "north", "id": "ae2:semi_dark_monitor", "spin": 4}
        ],
        (263, 100, 358): [
            {"direction": "north", "id": "ae2:terminal", "spin": 0},
            {"direction": "south", "id": "ae2:terminal", "spin": 4},
        ],
        (266, 100, 358): [
            {"direction": "south", "id": "ae2:dark_monitor", "spin": 4}
        ],
        (269, 100, 358): [
            {
                "direction": "up",
                "id": "ae2:pattern_encoding_terminal",
                "spin": 4,
            }
        ],
        (275, 100, 358): [
            {"direction": "down", "id": "ae2:crafting_terminal", "spin": 4}
        ],
        (278, 100, 358): [
            {"direction": "up", "id": "ae2:storage_monitor", "spin": 4}
        ],
    }
    for position, expected_parts in expected_invalid_spin_parts.items():
        anchor = persistent_fallbacks.get(position)
        if (
            not isinstance(anchor, dict)
            or anchor.get("face_parts") != expected_parts
            or anchor.get("facades", []) != []
            or not str(anchor.get("fallback_reason", "")).startswith(
                "invalid-reporting-spin-"
            )
        ):
            raise EvidenceError(
                f"persistent invalid-spin fallback changed at {position}"
            )
    stair_fallback = persistent_fallbacks.get((272, 100, 358))
    if (
        not isinstance(stair_fallback, dict)
        or stair_fallback.get("fallback_reason") != "non-full-cube-facade"
        or stair_fallback.get("face_parts", []) != []
        or stair_fallback.get("facades")
        != [
            {
                "direction": "north",
                "block_state": {
                    "Name": "minecraft:oak_stairs",
                    "Properties": {
                        "facing": "east",
                        "half": "bottom",
                        "shape": "straight",
                        "waterlogged": "false",
                    },
                },
            }
        ]
    ):
        raise EvidenceError("persistent non-full-cube facade fallback changed")
    _validate_s1_facade_gallery_source_fixtures(raw_cases)


def _legacy_upgrade_raw_anchor(raw_anchor: dict[str, Any]) -> dict[str, Any]:
    projected: dict[str, Any] = {
        "position": raw_anchor.get("position"),
        "block_id": raw_anchor.get("block_id"),
        "cable_id": raw_anchor.get("cable_id"),
    }
    for field in ("face_parts", "facades"):
        if field in raw_anchor:
            projected[field] = raw_anchor[field]
    return projected


def _reconstruct_native_structural_legacy_input(
    schema9: dict[str, Any],
) -> dict[str, Any]:
    raw_cases = schema9.get("cases")
    if not isinstance(raw_cases, list):
        raise EvidenceError("schema-9 cases are unavailable for legacy reconstruction")
    selected_ids = {case_id for case_id, _position in NATIVE_STRUCTURAL_LEGACY_SELECTORS}
    selected_cases = [case for case in raw_cases if case.get("case_id") in selected_ids]
    if [case.get("case_id") for case in selected_cases] != [
        case_id for case_id, _position in NATIVE_STRUCTURAL_LEGACY_SELECTORS
    ]:
        raise EvidenceError("legacy upgrade source case order/closure changed")

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
    for raw_case, (case_id, position) in zip(
        selected_cases, NATIVE_STRUCTURAL_LEGACY_SELECTORS, strict=True
    ):
        anchors = raw_case.get("anchors")
        if (
            not isinstance(anchors, list)
            or len(anchors) != 1
            or anchors[0].get("position")
            != dict(zip(("x", "y", "z"), position))
        ):
            raise EvidenceError(f"legacy upgrade source selector changed for {case_id}")
        anchor = anchors[0]
        if (
            anchor.get("block_id") != "ae2:cable_bus"
            or anchor.get("expected_triangle_count") != 0
            or not str(anchor.get("expected_path", "")).startswith("stock-fallback-")
        ):
            raise EvidenceError(f"legacy upgrade source is no longer fallback-empty: {case_id}")
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
        source_fixtures = raw_case.get("fixture_blocks")
        if endpoint is None:
            if source_fixtures != []:
                raise EvidenceError(f"legacy upgrade source helper changed for {case_id}")
        else:
            if source_fixtures != [
                {
                    "position": {"x": x + 1, "y": y, "z": z},
                    "block_id": endpoint["block_id"],
                }
            ]:
                raise EvidenceError(f"legacy endpoint source helper changed for {case_id}")
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
    return {
        "schema_version": 1,
        "profile_id": NATIVE_STRUCTURAL_ROUTE,
        "coverage_id": NATIVE_STRUCTURAL_LEGACY_COVERAGE,
        "source_schema9": {
            "cases_size_bytes": NATIVE_STRUCTURAL_LEGACY_SCHEMA9_CASES_SIZE_BYTES,
            "cases_sha256": SCHEMA9_CANONICAL_SHA256,
            "gallery_size_bytes": NATIVE_STRUCTURAL_LEGACY_SCHEMA9_GALLERY_SIZE_BYTES,
            "gallery_sha256": NATIVE_STRUCTURAL_LEGACY_SCHEMA9_GALLERY_SHA256,
            "signature_schema_version": 9,
        },
        "synthetic_world": {
            "anchor_block_state": "ae2:cable_bus[light_level=0,waterlogged=false]",
            "biome": "minecraft:plains",
            "blocklight": 0,
            "sunlight": 15,
            "support_block_state": "minecraft:smooth_stone",
            "support_patch": "complete-3x3-plane-one-block-below-each-anchor",
        },
        "cases": rendered_cases,
    }


def _read_exact_native_structural_legacy_input(
    schema9: dict[str, Any],
) -> dict[str, Any]:
    payload = read_bounded(
        NATIVE_STRUCTURAL_LEGACY_INPUT_PATH,
        NATIVE_STRUCTURAL_LEGACY_INPUT_SIZE_BYTES,
        "native structural legacy raw input",
    )
    if (
        len(payload) != NATIVE_STRUCTURAL_LEGACY_INPUT_SIZE_BYTES
        or sha256_bytes(payload) != NATIVE_STRUCTURAL_LEGACY_INPUT_SHA256
    ):
        raise EvidenceError("native structural legacy raw input identity changed")
    value = parse_json_bytes(payload, "native structural legacy raw input")
    reconstructed = _reconstruct_native_structural_legacy_input(schema9)
    if (
        value != reconstructed
        or canonical_json(reconstructed, pretty=True).encode("utf-8") != payload
    ):
        raise EvidenceError(
            "native structural legacy raw input differs from schema-9 source reconstruction"
        )
    if (
        len(value["cases"]) != NATIVE_STRUCTURAL_LEGACY_CASE_COUNT
        or sum(len(case["anchors"]) for case in value["cases"])
        != NATIVE_STRUCTURAL_LEGACY_ANCHOR_COUNT
        or sum(len(case["fixture_blocks"]) for case in value["cases"])
        != NATIVE_STRUCTURAL_LEGACY_FIXTURE_BLOCK_COUNT
    ):
        raise EvidenceError("native structural legacy raw input closure changed")
    return value


def _read_exact_native_structural_legacy_oracle() -> dict[
    tuple[int, int, int], dict[str, Any]
]:
    payload = read_bounded(
        NATIVE_STRUCTURAL_LEGACY_ORACLE_PATH,
        NATIVE_STRUCTURAL_LEGACY_ORACLE_SIZE_BYTES,
        "native structural legacy oracle",
    )
    if (
        len(payload) != NATIVE_STRUCTURAL_LEGACY_ORACLE_SIZE_BYTES
        or sha256_bytes(payload) != NATIVE_STRUCTURAL_LEGACY_ORACLE_SHA256
    ):
        raise EvidenceError("native structural legacy oracle identity changed")
    value = parse_json_bytes(payload, "native structural legacy oracle")
    if (
        not isinstance(value, dict)
        or set(value)
        != {
            "anchors",
            "coverage_id",
            "profile_id",
            "schema_version",
            "signature_schema_version",
        }
        or value.get("schema_version") != 2
        or value.get("signature_schema_version") != 10
        or value.get("profile_id") != NATIVE_STRUCTURAL_ROUTE
        or value.get("coverage_id") != NATIVE_STRUCTURAL_LEGACY_COVERAGE
        or not isinstance(value.get("anchors"), dict)
    ):
        raise EvidenceError("native structural legacy oracle header changed")
    parsed: dict[tuple[int, int, int], dict[str, Any]] = {}
    for key, entry in value["anchors"].items():
        try:
            position = tuple(int(item) for item in key.split())
        except (AttributeError, ValueError) as exception:
            raise EvidenceError("native structural legacy oracle position is malformed") from exception
        if (
            len(position) != 3
            or key != " ".join(str(item) for item in position)
            or position in parsed
            or not isinstance(entry, dict)
        ):
            raise EvidenceError("native structural legacy oracle selector is noncanonical")
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
            raise EvidenceError(
                f"native structural legacy oracle entry changed at {position}"
            )
        parsed[position] = entry
    resources = {
        resource
        for entry in parsed.values()
        for resource in entry["material_triangles"]
    }
    if (
        set(parsed) != {position for _case_id, position in NATIVE_STRUCTURAL_LEGACY_SELECTORS}
        or len(parsed) != NATIVE_STRUCTURAL_LEGACY_ANCHOR_COUNT
        or sum(entry["triangle_count"] for entry in parsed.values())
        != NATIVE_STRUCTURAL_LEGACY_TRIANGLE_COUNT
        or len(resources) != NATIVE_STRUCTURAL_LEGACY_IDENTITY_COUNT
        or sum(len(entry["material_triangles"]) for entry in parsed.values())
        != NATIVE_STRUCTURAL_LEGACY_MATERIAL_ROW_COUNT
    ):
        raise EvidenceError("native structural legacy oracle closure changed")
    return parsed


def _native_structural_legacy_endpoint_contracts(
    value: Any, case_id: str
) -> tuple[NativeStructuralEndpointContract, ...]:
    expected = {
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
    expected_rows = [expected[case_id]] if case_id in expected else []
    if value != expected_rows:
        raise EvidenceError(f"legacy upgrade endpoint contract changed for {case_id}")
    return tuple(
        NativeStructuralEndpointContract(
            row["direction"],
            row["block_id"],
            row["block_entity_id"],
            canonical_json(row["required_block_state"]),
            row["observed_endpoint_side"],
            row["side_rule"],
            row["formation"],
            row["exposed_on_observed_side"],
            row["declared_family"],
            row["local_family"],
            row["effective_family"],
            row["collar"],
            row["topology"],
        )
        for row in expected_rows
    )


def _parse_native_structural_legacy_upgrades(
    schema10: dict[str, Any],
    schema9: dict[str, Any],
    parsed_cases: list[CaseContract],
) -> tuple[list[CaseContract], dict[str, Any]]:
    raw_input = _read_exact_native_structural_legacy_input(schema9)
    oracle = _read_exact_native_structural_legacy_oracle()
    overlay = schema10.get("native_structural_legacy_upgrades")
    exact_source_schema9 = raw_input["source_schema9"]
    exact_summary = {
        "case_count": NATIVE_STRUCTURAL_LEGACY_CASE_COUNT,
        "anchor_count": NATIVE_STRUCTURAL_LEGACY_ANCHOR_COUNT,
        "custom_anchor_count": NATIVE_STRUCTURAL_LEGACY_ANCHOR_COUNT,
        "custom_triangle_count": NATIVE_STRUCTURAL_LEGACY_TRIANGLE_COUNT,
        "selected_resource_count": NATIVE_STRUCTURAL_LEGACY_IDENTITY_COUNT,
        "new_selected_resource_count": 0,
        "material_row_count": NATIVE_STRUCTURAL_LEGACY_MATERIAL_ROW_COUNT,
        "combined_native_structural_custom_anchor_count": 361,
        "combined_native_structural_custom_triangle_count": 38_358,
        "combined_native_structural_selected_resource_count": 96,
        "combined_native_structural_material_row_count": 2_163,
        "physical_stock_projection": {
            "rendered_anchor_count": 0,
            "empty_anchor_count": NATIVE_STRUCTURAL_LEGACY_ANCHOR_COUNT,
            "triangle_count": 0,
            "resource_count": 0,
            "resources": [],
        },
    }
    if (
        not isinstance(overlay, dict)
        or set(overlay)
        != {
            "coverage_id",
            "input",
            "oracle",
            "profile_id",
            "rows",
            "schema_version",
            "summary",
        }
        or overlay.get("schema_version") != 1
        or overlay.get("profile_id") != NATIVE_STRUCTURAL_ROUTE
        or overlay.get("coverage_id") != NATIVE_STRUCTURAL_LEGACY_COVERAGE
        or overlay.get("input")
        != {
            "path": NATIVE_STRUCTURAL_LEGACY_INPUT_PATH.name,
            "size_bytes": NATIVE_STRUCTURAL_LEGACY_INPUT_SIZE_BYTES,
            "sha256": NATIVE_STRUCTURAL_LEGACY_INPUT_SHA256,
            "schema_version": 1,
            "case_count": NATIVE_STRUCTURAL_LEGACY_CASE_COUNT,
            "anchor_count": NATIVE_STRUCTURAL_LEGACY_ANCHOR_COUNT,
            "fixture_block_count": NATIVE_STRUCTURAL_LEGACY_FIXTURE_BLOCK_COUNT,
            "source_schema9": exact_source_schema9,
        }
        or overlay.get("oracle")
        != {
            "path": NATIVE_STRUCTURAL_LEGACY_ORACLE_PATH.name,
            "size_bytes": NATIVE_STRUCTURAL_LEGACY_ORACLE_SIZE_BYTES,
            "sha256": NATIVE_STRUCTURAL_LEGACY_ORACLE_SHA256,
            "schema_version": 2,
            "signature_schema_version": 10,
            "anchor_count": NATIVE_STRUCTURAL_LEGACY_ANCHOR_COUNT,
            "triangle_count": NATIVE_STRUCTURAL_LEGACY_TRIANGLE_COUNT,
            "identity_count": NATIVE_STRUCTURAL_LEGACY_IDENTITY_COUNT,
            "material_row_count": NATIVE_STRUCTURAL_LEGACY_MATERIAL_ROW_COUNT,
        }
        or overlay.get("summary") != exact_summary
        or not isinstance(overlay.get("rows"), list)
        or len(overlay["rows"]) != NATIVE_STRUCTURAL_LEGACY_ANCHOR_COUNT
    ):
        raise EvidenceError("schema-10 native structural legacy overlay header changed")

    raw_schema9_cases = {
        case["case_id"]: case
        for case in schema9["cases"]
        if case.get("case_id")
        in {case_id for case_id, _position in NATIVE_STRUCTURAL_LEGACY_SELECTORS}
    }
    replacements: dict[tuple[str, tuple[int, int, int]], AnchorContract] = {}
    for row, (case_id, position) in zip(
        overlay["rows"], NATIVE_STRUCTURAL_LEGACY_SELECTORS, strict=True
    ):
        position_value = dict(zip(("x", "y", "z"), position))
        if (
            not isinstance(row, dict)
            or set(row)
            != {
                "case_id",
                "enabled",
                "legacy_projection",
                "physical_stock",
                "position",
            }
            or row.get("case_id") != case_id
            or row.get("position") != position_value
        ):
            raise EvidenceError("native structural legacy overlay selector changed")
        source_case = raw_schema9_cases.get(case_id)
        if not isinstance(source_case, dict) or len(source_case.get("anchors", ())) != 1:
            raise EvidenceError(f"native structural legacy source case missing: {case_id}")
        legacy_projection = source_case["anchors"][0]
        if (
            row.get("legacy_projection") != legacy_projection
            or legacy_projection.get("position") != position_value
            or not str(legacy_projection.get("expected_path", "")).startswith(
                "stock-fallback-"
            )
            or legacy_projection.get("expected_triangle_count") != 0
            or "expected_material_triangles" in legacy_projection
        ):
            raise EvidenceError(f"native structural legacy projection changed: {case_id}")
        if row.get("physical_stock") != {
            "expected_path": "stock-empty",
            "expected_triangle_count": 0,
            "expected_material_triangles": {},
        }:
            raise EvidenceError(f"native structural legacy stock projection changed: {case_id}")
        enabled = row.get("enabled")
        expected_oracle = oracle[position]
        if (
            not isinstance(enabled, dict)
            or set(enabled)
            != {
                "expected_connections",
                "expected_geometry_signature",
                "expected_material_triangles",
                "expected_nonlighting_attribute_signature",
                "expected_path",
                "expected_smart_overlays",
                "expected_triangle_count",
                "native_endpoints",
            }
            or enabled.get("expected_path") != "custom-s1"
            or enabled.get("expected_connections") != enabled.get("native_endpoints")
            or enabled.get("expected_smart_overlays") != {}
            or enabled.get("expected_triangle_count")
            != expected_oracle["triangle_count"]
            or enabled.get("expected_material_triangles")
            != expected_oracle["material_triangles"]
            or enabled.get("expected_geometry_signature")
            != expected_oracle["geometry_signature"]
            or enabled.get("expected_nonlighting_attribute_signature")
            != expected_oracle["nonlighting_attribute_signature"]
        ):
            raise EvidenceError(f"native structural legacy enabled contract changed: {case_id}")
        endpoints = _native_structural_legacy_endpoint_contracts(
            enabled["native_endpoints"], case_id
        )

        face_parts: list[FacePartContract] = []
        native_parts: list[NativeStructuralPartContract] = []
        seen_part_directions: set[str] = set()
        raw_parts = legacy_projection.get("face_parts", [])
        if not isinstance(raw_parts, list):
            raise EvidenceError(f"native structural legacy face parts changed: {case_id}")
        for part in raw_parts:
            if not isinstance(part, dict):
                raise EvidenceError(f"native structural legacy face part is malformed: {case_id}")
            direction = part.get("direction")
            full_id = part.get("id")
            name = full_id.removeprefix("ae2:") if isinstance(full_id, str) else None
            spin = part.get("spin")
            if (
                direction not in DIRECTION_VECTORS
                or direction in seen_part_directions
                or name not in NATIVE_STRUCTURAL_PART_IDS
                or (name in NATIVE_STRUCTURAL_SPIN_PART_IDS and spin not in range(4))
                or (name not in NATIVE_STRUCTURAL_SPIN_PART_IDS and spin is not None)
                or set(part)
                != ({"direction", "id", "spin"} if spin is not None else {"direction", "id"})
            ):
                raise EvidenceError(f"native structural legacy face part changed: {case_id}")
            seen_part_directions.add(direction)
            face_parts.append(FacePartContract(direction, full_id, spin))
            native_parts.append(
                NativeStructuralPartContract(
                    direction,
                    full_id,
                    NATIVE_STRUCTURAL_PART_GROUPS[name],
                    spin,
                    None,
                )
            )

        facades: list[FacadeContract] = []
        seen_facade_directions: set[str] = set()
        raw_facades = legacy_projection.get("facades", [])
        if not isinstance(raw_facades, list):
            raise EvidenceError(f"native structural legacy facades changed: {case_id}")
        for facade in raw_facades:
            if not isinstance(facade, dict) or set(facade) != {"direction", "block_state"}:
                raise EvidenceError(f"native structural legacy facade is malformed: {case_id}")
            direction = facade.get("direction")
            state = facade.get("block_state")
            if direction not in DIRECTION_VECTORS or direction in seen_facade_directions:
                raise EvidenceError(f"native structural legacy facade direction changed: {case_id}")
            _validate_native_structural_whitelist_facade_state(state)
            seen_facade_directions.add(direction)
            facades.append(FacadeContract(direction, canonical_json(state)))

        facade_mask = sum(
            1 << index
            for index, direction in enumerate(DIRECTION_VECTORS)
            if direction in seen_facade_directions
        ) if seen_facade_directions else None
        projection = Schema9DisabledProjectionContract(
            legacy_projection["expected_path"],
            0,
            (),
            (),
            (),
        )
        native = NativeStructuralContract(
            legacy_projection.get("cable_id"),
            tuple(native_parts),
            facade_mask,
            None,
            None,
            endpoints,
            None,
            enabled["expected_geometry_signature"],
            enabled["expected_nonlighting_attribute_signature"],
            0,
            projection,
        )
        selector = (case_id, position)
        if selector in replacements:
            raise EvidenceError("native structural legacy overlay selector is duplicated")
        replacements[selector] = AnchorContract(
            case_id=case_id,
            case_label=source_case["label"],
            expected_path="custom-s1",
            position=position,
            expected_triangle_count=enabled["expected_triangle_count"],
            expected_material_triangles=tuple(
                sorted(enabled["expected_material_triangles"].items())
            ),
            expected_smart_overlays=(),
            face_parts=tuple(face_parts),
            facades=tuple(facades),
            expected_terminal_layers=(),
            drive=None,
            fallback_reason=None,
            native_structural=native,
        )

    upgraded_cases: list[CaseContract] = []
    replaced: set[tuple[str, tuple[int, int, int]]] = set()
    for case in parsed_cases:
        anchors = []
        for anchor in case.anchors:
            selector = (case.case_id, anchor.position)
            replacement = replacements.get(selector)
            anchors.append(replacement if replacement is not None else anchor)
            if replacement is not None:
                replaced.add(selector)
        upgraded_cases.append(
            CaseContract(
                case.case_id,
                case.milestone,
                case.route,
                case.label,
                case.category,
                tuple(anchors),
            )
        )
    if replaced != set(NATIVE_STRUCTURAL_LEGACY_SELECTORS):
        raise EvidenceError("native structural legacy overlay has a dangling selector")
    return upgraded_cases, {
        "coverage_id": NATIVE_STRUCTURAL_LEGACY_COVERAGE,
        "input": overlay["input"],
        "oracle": overlay["oracle"],
        "summary": exact_summary,
    }


def _parse_schema10_cases(
    value: dict[str, Any], digest: str
) -> tuple[GalleryContract, dict[str, Any]]:
    if value.get("signature_schema_version") != 10:
        raise EvidenceError("schema-10 gallery must use signature schema 10")
    schema9 = _schema9_view(value)
    schema9_payload = canonical_json(schema9, pretty=True).encode("utf-8")
    schema9_sha256 = sha256_bytes(schema9_payload)
    if schema9_sha256 != SCHEMA9_CANONICAL_SHA256:
        raise EvidenceError("schema-10 does not embed the byte-frozen accepted schema-9 view")
    legacy, legacy_evidence = _parse_schema9_cases(schema9, schema9_sha256)

    all_cases = value.get("cases")
    if (
        not isinstance(all_cases, list)
        or value.get("case_count") != 150
        or value.get("anchor_count") != 957
    ):
        raise EvidenceError("schema-10 cases must be an array")
    raw_cases = [case for case in all_cases if case.get("milestone") == "S1"]
    s1_fixture_by_position: dict[tuple[int, int, int], dict[str, Any]] = {}
    for raw_case in raw_cases:
        raw_fixtures = raw_case.get("fixture_blocks", [])
        if not isinstance(raw_fixtures, list):
            raise EvidenceError("S1 fixture block array changed")
        for raw_fixture in raw_fixtures:
            if not isinstance(raw_fixture, dict):
                raise EvidenceError("S1 fixture block is not an object")
            fixture_position = _s1_xyz(
                raw_fixture.get("position"), "S1 fixture block position"
            )
            if fixture_position in s1_fixture_by_position:
                raise EvidenceError("S1 fixture block position is duplicated")
            s1_fixture_by_position[fixture_position] = raw_fixture
    logical_cases = json.loads(canonical_json(raw_cases))
    for logical_case in logical_cases:
        for logical_anchor in logical_case.get("anchors", ()):
            for field in (
                "expected_connections",
                "expected_triangle_count",
                "expected_material_triangles",
                "expected_smart_overlays",
                "expected_geometry_signature",
                "expected_nonlighting_attribute_signature",
            ):
                logical_anchor.pop(field, None)
    logical_matrix_sha256 = sha256_text(canonical_json(logical_cases))
    expected_case_sizes = [
        *([29] * 6),
        16,
        16,
        6,
        6,
        6,
        8,
        8,
        11,
        11,
        11,
        11,
        10,
        10,
        6,
        5,
        5,
        9,
        3,
        18,
        4,
        3,
        3,
    ]
    if (
        len(raw_cases) != NATIVE_STRUCTURAL_CASE_COUNT
        or [case.get("case_id") for case in raw_cases]
        != [f"ae2-s1-{index:02d}" for index in range(1, 29)]
        or any(case.get("route") != NATIVE_STRUCTURAL_ROUTE for case in raw_cases)
        or any(case.get("coverage_id") != NATIVE_STRUCTURAL_COVERAGE for case in raw_cases)
        or [len(case.get("anchors", ())) for case in raw_cases] != expected_case_sizes
    ):
        raise EvidenceError("schema-10 exact 28-case S1 matrix changed")
    _validate_s1_endpoint_source_fixtures(raw_cases)

    profile = value.get("profile")
    supported = (
        profile.get("supported_native_structural")
        if isinstance(profile, dict)
        else None
    )
    native_resources = (
        profile.get("native_structural_resources")
        if isinstance(profile, dict)
        else None
    )
    native_profile = (
        profile.get("native_structural_profile")
        if isinstance(profile, dict)
        else None
    )
    facade_profile = (
        supported.get("facades") if isinstance(supported, dict) else None
    )
    _validate_native_structural_glassential_override(
        facade_profile.get("full_pack_glass_override")
        if isinstance(facade_profile, dict)
        else None
    )
    computed_endpoint_state_counts: dict[str, int] = {}
    ordered_endpoint_policies = (
        supported.get("ordered_endpoint_policies")
        if isinstance(supported, dict)
        else None
    )
    if isinstance(ordered_endpoint_policies, list):
        for row in ordered_endpoint_policies:
            if not isinstance(row, dict):
                continue
            endpoint_id = row.get("id")
            state_properties = row.get("state_properties")
            if (
                not isinstance(endpoint_id, str)
                or not endpoint_id.startswith("ae2:")
                or not isinstance(state_properties, dict)
            ):
                continue
            state_count = 1
            valid_domains = True
            for domain in state_properties.values():
                if not isinstance(domain, list) or not domain:
                    valid_domains = False
                    break
                state_count *= len(domain)
            if valid_domains:
                computed_endpoint_state_counts[
                    endpoint_id.removeprefix("ae2:")
                ] = state_count
    computed_facade_state_counts: dict[str, int] = {}
    for block_id, schema in (
        NATIVE_STRUCTURAL_FACADE_WHITELIST_STATE_SCHEMAS.items()
    ):
        state_count = 1
        for domain in schema.values():
            state_count *= len(domain)
        computed_facade_state_counts[block_id] = state_count
    if (
        not isinstance(profile, dict)
        or profile.get("coverage_milestone") != NATIVE_STRUCTURAL_COVERAGE
        or not isinstance(supported, dict)
        or supported.get("route") != NATIVE_STRUCTURAL_ROUTE
        or supported.get("part_ids") != [f"ae2:{name}" for name in NATIVE_STRUCTURAL_PART_IDS]
        or supported.get("part_groups")
        != {
            f"ae2:{name}": NATIVE_STRUCTURAL_PART_GROUPS[name]
            for name in NATIVE_STRUCTURAL_PART_IDS
        }
        or supported.get("spin_part_ids")
        != [f"ae2:{name}" for name in NATIVE_STRUCTURAL_PART_IDS if name in NATIVE_STRUCTURAL_SPIN_PART_IDS]
        or supported.get("orientation_state_count") != NATIVE_STRUCTURAL_ORIENTATION_STATE_COUNT
        or supported.get("plane_mask_count_per_type") != 16
        or supported.get("plane_part_ids")
        != [f"ae2:{name}" for name in NATIVE_STRUCTURAL_PLANE_PART_IDS]
        or supported.get("plane_mask_bits")
        != {"up": 8, "right": 4, "down": 2, "left": 1}
        or supported.get("p2p_frequency_domain") != [0, 65535]
        or supported.get("p2p_part_ids")
        != [f"ae2:{name}" for name in NATIVE_STRUCTURAL_P2P_PART_IDS]
        or supported.get("dense_capable_part_ids") != ["ae2:cable_anchor"]
        or supported.get("endpoint_catalog")
        != {
            family: [f"ae2:{name}" for name in names]
            for family, names in NATIVE_STRUCTURAL_ENDPOINTS.items()
        }
        or not isinstance(supported.get("ordered_endpoint_policies"), list)
        or len(supported["ordered_endpoint_policies"]) != 30
        or [
            (entry.get("id"), entry.get("cable_type"))
            for entry in supported["ordered_endpoint_policies"]
        ]
        != [
            (f"ae2:{name}", family)
            for name, family in NATIVE_STRUCTURAL_ENDPOINTS_ORDERED
        ]
        or sha256_text(
            canonical_json(supported["ordered_endpoint_policies"])
        )
        != NATIVE_STRUCTURAL_ENDPOINT_STATE_CONTRACT_SHA256
        or computed_endpoint_state_counts
        != NATIVE_STRUCTURAL_ENDPOINT_STATE_COUNTS
        or sum(computed_endpoint_state_counts.values())
        != NATIVE_STRUCTURAL_ENDPOINT_STATE_CARTESIAN_COUNT
        or sum(computed_endpoint_state_counts.values()) * len(DIRECTION_VECTORS)
        != NATIVE_STRUCTURAL_ENDPOINT_STATE_SIDE_CARTESIAN_COUNT
        or supported.get("endpoint_state_counts")
        != NATIVE_STRUCTURAL_ENDPOINT_STATE_COUNTS
        or supported.get("endpoint_state_cartesian_count")
        != NATIVE_STRUCTURAL_ENDPOINT_STATE_CARTESIAN_COUNT
        or supported.get("endpoint_state_side_cartesian_count")
        != NATIVE_STRUCTURAL_ENDPOINT_STATE_SIDE_CARTESIAN_COUNT
        or computed_facade_state_counts
        != NATIVE_STRUCTURAL_FACADE_WHITELIST_STATE_COUNTS
        or sum(computed_facade_state_counts.values())
        != NATIVE_STRUCTURAL_FACADE_WHITELIST_STATE_CARTESIAN_COUNT
        or sum(
            computed_facade_state_counts[block_id]
            for block_id, solid_render in (
                NATIVE_STRUCTURAL_FACADE_WHITELIST_SOLID_RENDER.items()
            )
            if solid_render
        )
        != NATIVE_STRUCTURAL_FACADE_SOLID_RENDER_TRUE_CARTESIAN_COUNT
        or sum(
            computed_facade_state_counts[block_id]
            for block_id, skip_rendering in (
                NATIVE_STRUCTURAL_FACADE_WHITELIST_SAME_STATE_SKIP_RENDERING.items()
            )
            if skip_rendering
        )
        != NATIVE_STRUCTURAL_FACADE_SAME_STATE_SKIP_TRUE_CARTESIAN_COUNT
        or supported.get("endpoint_topology")
        != "direct-six-neighbor-AECableType-min-with-native-collar"
        or supported.get("map_color_illumination_policy")
        != NATIVE_STRUCTURAL_MAP_COLOR_ILLUMINATION_POLICY
        or supported.get("unsupported_compatible_endpoint_control")
        != {
            "block_id": NATIVE_STRUCTURAL_UNKNOWN_EXTENSION_ENDPOINT["block_id"],
            "block_entity_id": NATIVE_STRUCTURAL_UNKNOWN_EXTENSION_ENDPOINT[
                "block_entity_id"
            ],
            "required_state": NATIVE_STRUCTURAL_UNKNOWN_EXTENSION_ENDPOINT[
                "required_state"
            ],
            "policy": "known-compatible-UNKNOWN-whole-bus-fallback",
        }
        or supported.get("unsupported_compatible_endpoint_count") != 67
        or supported.get("unsupported_compatible_endpoint_entries_sha256")
        != NATIVE_STRUCTURAL_UNKNOWN_ENDPOINT_ENTRIES_SHA256
        or supported.get("unsupported_compatible_endpoint_artifacts_sha256")
        != NATIVE_STRUCTURAL_UNKNOWN_ENDPOINT_ARTIFACTS_SHA256
        or supported.get("transient_policy") != "static-off-inactive-unlocked"
        or supported.get("facades")
        != {
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
            "physical_whitelist_ids": list(
                NATIVE_STRUCTURAL_PHYSICAL_FACADE_WHITELIST_IDS
            ),
            "neutral_native_material_count": 11,
            "crafting_monitor_normalization": (
                "preserve-valid-facing-force-unformed-unpowered-spin-zero"
            ),
        }
        or supported.get("atomic_fallback") != "whole-cable-bus-original-resource"
        or not isinstance(native_resources, dict)
        or native_resources.get("operator_required_json_count") != 43
        or native_resources.get("operator_required_png_count") != 56
        or native_resources.get("operator_required_path_count") != 99
        or not isinstance(
            native_resources.get("operator_required_texture_resources"), list
        )
        or len(native_resources["operator_required_texture_resources"]) != 56
        or len(set(native_resources["operator_required_texture_resources"])) != 56
        or sha256_text(
            canonical_json(native_resources["operator_required_texture_resources"])
        )
        != NATIVE_STRUCTURAL_TEXTURE_RESOURCE_LIST_SHA256
        or not isinstance(native_profile, dict)
        or native_profile.get("profile_id") != NATIVE_STRUCTURAL_ROUTE
        or native_profile.get("profile_size_bytes")
        != NATIVE_STRUCTURAL_PROFILE_SIZE_BYTES
        or native_profile.get("profile_sha256")
        != NATIVE_STRUCTURAL_PROFILE_SHA256
        or native_profile.get("support_matrix_size_bytes")
        != NATIVE_STRUCTURAL_SUPPORT_MATRIX_SIZE_BYTES
        or native_profile.get("support_matrix_sha256")
        != NATIVE_STRUCTURAL_SUPPORT_MATRIX_SHA256
        or native_profile.get("provenance_size_bytes")
        != NATIVE_STRUCTURAL_PROVENANCE_SIZE_BYTES
        or native_profile.get("provenance_sha256")
        != NATIVE_STRUCTURAL_PROVENANCE_SHA256
        or native_profile.get("artifact") != "appliedenergistics2-19.2.17.jar"
        or native_profile.get("artifact_size_bytes") != 8_230_896
        or native_profile.get("artifact_sha256")
        != "460d779a0609b81409907d9956de8f6f70a1b0912257e3e5c3c7e75ac9630e95"
        or native_profile.get("source_tag") != "neoforge/v19.2.17"
        or native_profile.get("source_commit")
        != "79ee2c704ad62941a426c26b1cb1f76ef5b2ee5a"
        or native_profile.get("source_sha256")
        != "d2f451203cb61c2d21fae52c683083d2f72441ca7d26725f4df5934290492e6a"
        or native_profile.get("direct_neutral_model_root_count") != 41
        or native_profile.get("transitive_json_count") != 43
        or native_profile.get("png_count") != 56
        or native_profile.get("path_count") != 99
        or native_profile.get("total_bytes") != 51_306
        or native_profile.get("required_resources_manifest_sha256")
        != NATIVE_STRUCTURAL_RESOURCE_MANIFEST_SHA256
        or native_profile.get("required_resource_sizes_manifest_sha256")
        != "a79e93baef3f5d923730686fcc4de05ec30c8b7765aef8b32aaf871f9c4f3869"
        or native_profile.get("endpoint_state_schema_count") != 30
        or native_profile.get("endpoint_state_contract_sha256")
        != NATIVE_STRUCTURAL_ENDPOINT_STATE_CONTRACT_SHA256
        or native_profile.get("endpoint_state_cartesian_count")
        != NATIVE_STRUCTURAL_ENDPOINT_STATE_CARTESIAN_COUNT
        or native_profile.get("endpoint_state_side_cartesian_count")
        != NATIVE_STRUCTURAL_ENDPOINT_STATE_SIDE_CARTESIAN_COUNT
        or native_profile.get("facade_state_schema_count") != 24
        or native_profile.get("facade_state_contract_sha256")
        != NATIVE_STRUCTURAL_FACADE_WHITELIST_STATE_CONTRACT_SHA256
        or native_profile.get("facade_state_cartesian_count")
        != NATIVE_STRUCTURAL_FACADE_WHITELIST_STATE_CARTESIAN_COUNT
        or native_profile.get("facade_solid_render_true_cartesian_count")
        != NATIVE_STRUCTURAL_FACADE_SOLID_RENDER_TRUE_CARTESIAN_COUNT
        or native_profile.get("facade_same_state_skip_true_cartesian_count")
        != NATIVE_STRUCTURAL_FACADE_SAME_STATE_SKIP_TRUE_CARTESIAN_COUNT
        or native_profile.get("unsupported_compatible_endpoint_count") != 67
        or native_profile.get("unsupported_compatible_endpoint_entries_sha256")
        != NATIVE_STRUCTURAL_UNKNOWN_ENDPOINT_ENTRIES_SHA256
        or native_profile.get("unsupported_compatible_endpoint_artifacts_sha256")
        != NATIVE_STRUCTURAL_UNKNOWN_ENDPOINT_ARTIFACTS_SHA256
        or native_profile.get("live_model_semantic_sha256")
        != "aefa42ad8427e8f2ac5b9f1c88807c978617d6ff70768a32223616b970b54251"
        or native_profile.get("live_texture_semantic_sha256")
        != "1bee2b2917edf3d1eb9ee24505f47a7377665da753f107ec1af9170d783bc833"
    ):
        raise EvidenceError("schema-10 native structural profile contract changed")

    parsed_cases = list(legacy.cases)
    positions: set[tuple[int, int, int]] = set()
    fallback_positions: list[tuple[int, int, int]] = []
    custom_count = 0
    custom_triangles = 0
    custom_resources: set[str] = set()
    installed_face_catalog: list[tuple[str, str, int | None]] = []
    plane_masks: dict[str, set[int]] = {
        part_id: set() for part_id in NATIVE_STRUCTURAL_PLANE_PART_IDS
    }
    p2p_matrix: set[tuple[str, int]] = set()
    facade_masks: set[int] = set()
    endpoints: set[str] = set()
    ordered_endpoint_catalog: list[tuple[str, str]] = []
    schema9_disabled_rendered: dict[
        tuple[int, int, int], tuple[str, int]
    ] = {}
    schema9_disabled_resources: set[str] = set()
    schema9_disabled_triangles = 0

    for raw_case in raw_cases:
        parsed_anchors: list[AnchorContract] = []
        for raw_anchor in raw_case["anchors"]:
            raw_position = raw_anchor.get("position")
            if not isinstance(raw_position, dict) or set(raw_position) != {"x", "y", "z"}:
                raise EvidenceError("S1 anchor position is not exact xyz metadata")
            position = tuple(raw_position[axis] for axis in ("x", "y", "z"))
            if (
                not all(isinstance(coordinate, int) and not isinstance(coordinate, bool) for coordinate in position)
                or position in positions
                or not (208 <= position[0] <= 319 and 96 <= position[1] <= 110 and 312 <= position[2] <= 367)
            ):
                raise EvidenceError(f"S1 anchor position is invalid: {position}")
            positions.add(position)
            if raw_anchor.get("block_id") != "ae2:cable_bus":
                raise EvidenceError(f"S1 anchor {position} is not a cable bus")

            raw_parts = raw_anchor.get("face_parts", [])
            raw_facades = raw_anchor.get("facades", [])
            if not isinstance(raw_parts, list) or not isinstance(raw_facades, list):
                raise EvidenceError(f"S1 anchor {position} has malformed part/facade arrays")
            native_parts: list[NativeStructuralPartContract] = []
            face_parts: list[FacePartContract] = []
            seen_part_directions: set[str] = set()
            for raw_part in raw_parts:
                if not isinstance(raw_part, dict):
                    raise EvidenceError(f"S1 anchor {position} has a non-object face part")
                direction = raw_part.get("direction")
                full_id = raw_part.get("id")
                part_name = full_id.removeprefix("ae2:") if isinstance(full_id, str) else None
                spin = raw_part.get("spin")
                frequency = raw_part.get("freq")
                if (
                    direction not in DIRECTION_VECTORS
                    or direction in seen_part_directions
                    or part_name not in NATIVE_STRUCTURAL_PART_IDS
                ):
                    # Explicit fallback controls intentionally retain malformed
                    # identities/fields. Their geometry must remain stock-empty.
                    if raw_anchor.get("expected_path") != "stock-fallback-s1":
                        raise EvidenceError(f"S1 custom anchor {position} has an invalid part")
                    continue
                seen_part_directions.add(direction)
                if part_name in NATIVE_STRUCTURAL_SPIN_PART_IDS:
                    if spin not in range(4):
                        if raw_anchor.get("expected_path") != "stock-fallback-s1":
                            raise EvidenceError(f"S1 custom reporting part {position} has invalid spin")
                        continue
                elif spin is not None:
                    raise EvidenceError(f"S1 non-reporting part {position} persists spin")
                if part_name in NATIVE_STRUCTURAL_P2P_PART_IDS:
                    if not isinstance(frequency, int) or isinstance(frequency, bool) or not 0 <= frequency <= 65535:
                        if raw_anchor.get("expected_path") != "stock-fallback-s1":
                            raise EvidenceError(f"S1 custom P2P part {position} has invalid frequency")
                        continue
                    p2p_matrix.add((part_name, frequency))
                elif frequency is not None:
                    raise EvidenceError(f"S1 non-P2P part {position} persists frequency")
                group = NATIVE_STRUCTURAL_PART_GROUPS[part_name]
                native_parts.append(
                    NativeStructuralPartContract(direction, full_id, group, spin, frequency)
                )
                face_parts.append(FacePartContract(direction, full_id, spin))

            facades: list[FacadeContract] = []
            seen_facade_directions: set[str] = set()
            for raw_facade in raw_facades:
                if not isinstance(raw_facade, dict):
                    raise EvidenceError(f"S1 anchor {position} has a non-object facade")
                direction = raw_facade.get("direction")
                state = raw_facade.get("block_state")
                if direction not in DIRECTION_VECTORS or direction in seen_facade_directions:
                    raise EvidenceError(f"S1 anchor {position} has an invalid facade direction")
                seen_facade_directions.add(direction)
                if not isinstance(state, dict) or not isinstance(state.get("Name"), str):
                    if raw_anchor.get("expected_path") != "stock-fallback-s1":
                        raise EvidenceError(f"S1 custom anchor {position} has malformed facade state")
                    continue
                try:
                    _validate_native_structural_whitelist_facade_state(state)
                except EvidenceError:
                    if raw_anchor.get("expected_path") != "stock-fallback-s1":
                        raise
                    continue
                facades.append(FacadeContract(direction, canonical_json(state)))

            expected_path = raw_anchor.get("expected_path")
            expected_triangle_count = raw_anchor.get("expected_triangle_count")
            raw_materials = raw_anchor.get("expected_material_triangles", {})
            fallback_reason = raw_anchor.get("fallback_reason")
            if expected_path == "custom-s1":
                if (
                    not isinstance(expected_triangle_count, int)
                    or isinstance(expected_triangle_count, bool)
                    or expected_triangle_count <= 0
                    or not isinstance(raw_materials, dict)
                    or not raw_materials
                    or any(
                        not isinstance(resource, str)
                        or not isinstance(count, int)
                        or isinstance(count, bool)
                        or count <= 0
                        for resource, count in raw_materials.items()
                    )
                    or sum(raw_materials.values()) != expected_triangle_count
                    or fallback_reason is not None
                ):
                    raise EvidenceError(f"S1 custom anchor {position} geometry metadata changed")
                custom_count += 1
                custom_triangles += expected_triangle_count
                custom_resources.update(raw_materials)
            elif expected_path == "stock-fallback-s1":
                if expected_triangle_count != 0 or raw_materials or not isinstance(fallback_reason, str):
                    raise EvidenceError(f"S1 fallback anchor {position} projection changed")
                fallback_positions.append(position)
            else:
                raise EvidenceError(f"S1 anchor {position} has an unsupported path")
            if raw_anchor.get("expected_stock_triangle_count") != 0:
                raise EvidenceError(f"S1 anchor {position} stock projection must be empty")

            source_projection = _schema9_disabled_source_projection(
                raw_anchor, s1_fixture_by_position
            )
            if raw_anchor.get("schema9_route_disabled_projection") != source_projection:
                raise EvidenceError(
                    f"S1 anchor {position} schema-9 route-disabled projection changed"
                )
            projection_path = source_projection["expected_path"]
            projection_materials = tuple(
                sorted(source_projection["expected_material_triangles"].items())
            )
            projection_overlays = tuple(
                sorted(
                    (
                        resource,
                        tuple(value["rgb_u8"]),
                        value["blocklight_raw_i8"],
                        value["sunlight_raw_i8"],
                    )
                    for resource, value in source_projection[
                        "expected_smart_overlays"
                    ].items()
                )
            )
            projection_terminal_layers = tuple(
                sorted(
                    (
                        TerminalLayerContract(
                            resource,
                            tuple(value["rgb_u8"]),
                            value["emissive"],
                            value["triangle_count_per_part"],
                        )
                        for resource, value in source_projection[
                            "expected_terminal_layers"
                        ].items()
                    ),
                    key=lambda item: item.resource_path,
                )
            )
            if projection_path != "stock-empty":
                schema9_disabled_rendered[position] = (
                    projection_path,
                    source_projection["expected_triangle_count"],
                )
                schema9_disabled_triangles += source_projection[
                    "expected_triangle_count"
                ]
                schema9_disabled_resources.update(
                    source_projection["expected_material_triangles"]
                )

            facade_mask = raw_anchor.get("facade_mask")
            if facade_mask is not None:
                if not isinstance(facade_mask, int) or isinstance(facade_mask, bool) or not 0 <= facade_mask <= 63:
                    raise EvidenceError(f"S1 anchor {position} has invalid facade mask")
                facade_masks.add(facade_mask)
            plane_mask = raw_anchor.get("plane_mask")
            if plane_mask is not None:
                plane_parts = [part.part_id.removeprefix("ae2:") for part in native_parts if part.part_id.removeprefix("ae2:") in NATIVE_STRUCTURAL_PLANE_PART_IDS]
                if len(plane_parts) != 1 or not isinstance(plane_mask, int) or not 0 <= plane_mask <= 15:
                    raise EvidenceError(f"S1 anchor {position} has invalid plane mask metadata")
                plane_masks[plane_parts[0]].add(plane_mask)
            p2p_frequency = raw_anchor.get("p2p_frequency_unsigned")
            if p2p_frequency is not None and p2p_frequency not in {0, 0x1234, 0xFFFF}:
                raise EvidenceError(f"S1 anchor {position} has an invalid P2P gallery frequency")

            endpoint_contracts: list[NativeStructuralEndpointContract] = []
            raw_endpoints = raw_anchor.get("native_endpoints", [])
            if not isinstance(raw_endpoints, list):
                raise EvidenceError(f"S1 anchor {position} has malformed endpoint array")
            if len(raw_endpoints) > 2:
                raise EvidenceError(f"S1 anchor {position} has too many endpoint observations")
            known_endpoint_names = {
                name for names in NATIVE_STRUCTURAL_ENDPOINTS.values() for name in names
            }
            seen_endpoint_directions: set[str] = set()
            for endpoint_index, endpoint in enumerate(raw_endpoints):
                if (
                    not isinstance(endpoint, dict)
                    or endpoint.get("direction") not in DIRECTION_VECTORS
                    or endpoint.get("direction") in seen_endpoint_directions
                ):
                    raise EvidenceError(f"S1 anchor {position} has malformed endpoint metadata")
                seen_endpoint_directions.add(endpoint["direction"])
                endpoint_id = endpoint.get("block_id")
                required_state = endpoint.get("required_block_state")
                observed_side = endpoint.get("observed_endpoint_side")
                side_rule = endpoint.get("side_rule")
                formation = endpoint.get("formation")
                exposed = endpoint.get("exposed_on_observed_side")
                if (
                    not isinstance(endpoint_id, str)
                    or not isinstance(required_state, dict)
                    or any(not isinstance(key, str) for key in required_state)
                    or observed_side not in DIRECTION_VECTORS
                    or not isinstance(side_rule, str)
                    or (formation is not None and not isinstance(formation, str))
                    or not isinstance(exposed, bool)
                ):
                    raise EvidenceError(f"S1 anchor {position} has malformed endpoint identity/state")
                endpoint_name = endpoint_id.removeprefix("ae2:")
                if endpoint_name in known_endpoint_names:
                    endpoints.add(endpoint_name)
                    if (
                        raw_case["case_id"]
                        in {"ae2-s1-23", "ae2-s1-24", "ae2-s1-25"}
                        and endpoint_index == 0
                    ):
                        ordered_endpoint_catalog.append(
                            (endpoint_name, endpoint.get("declared_family"))
                        )
                    expected_topology = (
                        "native-grid-node-host"
                        if exposed
                        else "known-native-grid-node-host-disconnected"
                    )
                    straight = raw_anchor.get("endpoint_straight_optimization")
                    straight_enabled = (
                        isinstance(straight, dict) and straight.get("enabled") is True
                    )
                    expected_collar = (
                        exposed
                        and not straight_enabled
                        and endpoint.get("effective_family")
                        not in {"glass", "dense_covered", "dense_smart"}
                    )
                    if (
                        endpoint.get("topology") != expected_topology
                        or endpoint.get("collar") is not expected_collar
                    ):
                        raise EvidenceError(
                            f"S1 native endpoint {position} topology/collar changed"
                        )
                elif endpoint_id == NATIVE_STRUCTURAL_UNKNOWN_EXTENSION_ENDPOINT[
                    "block_id"
                ]:
                    if (
                        expected_path != "stock-fallback-s1"
                        or fallback_reason
                        != "known-compatible-extension-endpoint-unknown"
                        or endpoint.get("block_entity_id")
                        != NATIVE_STRUCTURAL_UNKNOWN_EXTENSION_ENDPOINT[
                            "block_entity_id"
                        ]
                        or required_state
                        != NATIVE_STRUCTURAL_UNKNOWN_EXTENSION_ENDPOINT[
                            "required_state"
                        ]
                        or observed_side != "west"
                        or side_rule != "UNSUPPORTED_COMPATIBLE_GRID_HOST"
                        or exposed is not True
                        or endpoint.get("declared_family") != "smart"
                        or endpoint.get("local_family") != "smart"
                        or endpoint.get("effective_family") is not None
                        or endpoint.get("collar") is not False
                        or endpoint.get("topology")
                        != "known-compatible-extension-unknown"
                    ):
                        raise EvidenceError(
                            f"S1 compatible extension {position} UNKNOWN fallback changed"
                        )
                elif endpoint.get("topology") != "known-noncatalog-disconnected":
                    raise EvidenceError(f"S1 anchor {position} has unknown endpoint semantics")
                endpoint_contracts.append(
                    NativeStructuralEndpointContract(
                        endpoint["direction"],
                        endpoint_id,
                        endpoint.get("block_entity_id"),
                        canonical_json(required_state),
                        observed_side,
                        side_rule,
                        formation,
                        exposed,
                        endpoint.get("declared_family"),
                        endpoint.get("local_family"),
                        endpoint.get("effective_family"),
                        endpoint.get("collar") is True,
                        endpoint.get("topology"),
                    )
                )

            geometry_signature = raw_anchor.get("expected_geometry_signature")
            nonlighting_attribute_signature = raw_anchor.get(
                "expected_nonlighting_attribute_signature"
            )
            if expected_path == "custom-s1":
                if (
                    not isinstance(geometry_signature, str)
                    or len(geometry_signature) != 64
                    or any(character not in "0123456789abcdef" for character in geometry_signature)
                    or not isinstance(nonlighting_attribute_signature, str)
                    or len(nonlighting_attribute_signature) != 64
                    or any(
                        character not in "0123456789abcdef"
                        for character in nonlighting_attribute_signature
                    )
                ):
                    raise EvidenceError(
                        f"S1 custom anchor {position} lacks exact non-lighting signatures"
                    )
            native = NativeStructuralContract(
                raw_anchor.get("cable_id"),
                tuple(native_parts),
                facade_mask,
                plane_mask,
                p2p_frequency,
                tuple(endpoint_contracts),
                (
                    canonical_json(raw_anchor["endpoint_straight_optimization"])
                    if "endpoint_straight_optimization" in raw_anchor
                    else None
                ),
                geometry_signature,
                nonlighting_attribute_signature,
                0,
                Schema9DisabledProjectionContract(
                    projection_path,
                    source_projection["expected_triangle_count"],
                    projection_materials,
                    projection_overlays,
                    projection_terminal_layers,
                ),
            )
            parsed_anchors.append(
                AnchorContract(
                    case_id=raw_case["case_id"],
                    case_label=raw_case["label"],
                    expected_path=expected_path,
                    position=position,
                    expected_triangle_count=expected_triangle_count,
                    expected_material_triangles=tuple(sorted(raw_materials.items())),
                    expected_smart_overlays=(),
                    face_parts=tuple(face_parts),
                    facades=tuple(facades),
                    expected_terminal_layers=(),
                    drive=None,
                    fallback_reason=fallback_reason,
                    native_structural=native,
                )
            )
        parsed_cases.append(
            CaseContract(
                raw_case["case_id"],
                "S1",
                NATIVE_STRUCTURAL_ROUTE,
                raw_case["label"],
                raw_case["category"],
                tuple(parsed_anchors),
            )
        )

    parsed_cases, legacy_upgrade_evidence = (
        _parse_native_structural_legacy_upgrades(value, schema9, parsed_cases)
    )
    legacy_upgrade_positions = {
        position for _case_id, position in NATIVE_STRUCTURAL_LEGACY_SELECTORS
    }
    legacy_upgrade_resources = {
        resource
        for case in parsed_cases
        for anchor in case.anchors
        if anchor.position in legacy_upgrade_positions
        for resource, _count in anchor.expected_material_triangles
    }
    if (
        len(legacy_upgrade_resources) != NATIVE_STRUCTURAL_LEGACY_IDENTITY_COUNT
        or not legacy_upgrade_resources <= custom_resources
        or len(custom_resources | legacy_upgrade_resources)
        != NATIVE_STRUCTURAL_ORACLE_IDENTITY_COUNT
    ):
        raise EvidenceError("legacy upgrade/appended S1 resource closure changed")

    # The first six cases are the representative installed-face orbit.
    for direction_index, raw_case in enumerate(raw_cases[:6]):
        direction = tuple(DIRECTION_VECTORS)[direction_index]
        for raw_anchor, part_name in zip(raw_case["anchors"], NATIVE_STRUCTURAL_PART_IDS, strict=True):
            parts = raw_anchor.get("face_parts")
            expected_spin = direction_index % 4 if part_name in NATIVE_STRUCTURAL_SPIN_PART_IDS else None
            if not isinstance(parts, list) or len(parts) != 1:
                raise EvidenceError("S1 installed-face orbit no longer has one part per anchor")
            part = parts[0]
            installed_face_catalog.append((part.get("id"), part.get("direction"), part.get("spin")))
            if (
                part.get("id") != f"ae2:{part_name}"
                or part.get("direction") != direction
                or part.get("spin") != expected_spin
                or raw_anchor.get("part_cable_type_requirement")
                != ("smart" if part_name in NATIVE_STRUCTURAL_EMITTER_PART_IDS else "glass")
                or raw_anchor.get("expected_visible_cable_core")
                != ("covered" if part_name in NATIVE_STRUCTURAL_EMITTER_PART_IDS else "glass")
            ):
                raise EvidenceError("S1 installed-face representative orbit changed")
            cutout_goldens = {
                "quartz_fiber": [6, 6, 10, 10],
                "toggle_bus": [6, 6, 10, 10],
                "import_bus": [4, 4, 12, 12],
                "export_bus": [6, 6, 10, 10],
                "level_emitter": [7, 7, 9, 9],
                "terminal": [2, 2, 14, 14],
            }
            expected_facade = (
                [{"direction": direction, "block_state": {"Name": "minecraft:stone"}}]
                if direction_index == 0 and part_name in cutout_goldens
                else []
            )
            if (
                raw_anchor.get("facades", []) != expected_facade
                or raw_anchor.get("expected_facade_cutout_sixteenths")
                != (
                    cutout_goldens[part_name]
                    if direction_index == 0 and part_name in cutout_goldens
                    else None
                )
            ):
                raise EvidenceError(
                    f"S1 source-derived facade cutout golden changed for {part_name}"
                )

    for plane_case_index, raw_case in enumerate(raw_cases[6:8]):
        installed_face = "up" if plane_case_index == 0 else "north"
        for mask, raw_anchor in enumerate(raw_case["anchors"]):
            visual_local = _native_plane_local_bounds(
                mask,
                _NATIVE_PLANE_VISUAL_LOCAL_BOUND_BITS,
                (0.0, 1.0 / 16.0),
            )
            collision_local = _native_plane_local_bounds(
                mask,
                _NATIVE_PLANE_COLLISION_LOCAL_BOUND_BITS[installed_face],
                (15.0 / 16.0, 1.0),
            )

            def sixteenths(
                bounds: tuple[
                    tuple[float, float, float], tuple[float, float, float]
                ]
            ) -> list[list[int]]:
                return [
                    [round(coordinate * 16.0) for coordinate in bound]
                    for bound in bounds
                ]

            expected_facade_directions = [installed_face]
            if plane_case_index == 0 and mask == 8:
                expected_facade_directions.append("north")
            expected_facade_states = (
                [
                    {"Name": "minecraft:glass"},
                    {"Name": "minecraft:stone"},
                ]
                if plane_case_index == 0 and mask == 8
                else [{"Name": "minecraft:stone"}]
            )
            if (
                [facade.get("direction") for facade in raw_anchor.get("facades", ())]
                != expected_facade_directions
                or [
                    facade.get("block_state")
                    for facade in raw_anchor.get("facades", ())
                ]
                != expected_facade_states
                or raw_anchor.get("facade_structural_expectation")
                != "same-face-plane-mask-asymmetric-cutout"
                or raw_anchor.get("plane_visual_local_axes")
                != list(_NATIVE_PLANE_VISUAL_LOCAL_AXES[installed_face])
                or raw_anchor.get("plane_visual_local_bounds_sixteenths")
                != sixteenths(visual_local)
                or raw_anchor.get("plane_visual_world_bounds_sixteenths")
                != sixteenths(
                    _native_plane_visual_expected_bounds(installed_face, mask)
                )
                or raw_anchor.get("plane_facade_cutout_local_axes")
                != list(_NATIVE_PLANE_COLLISION_LOCAL_AXES[installed_face])
                or raw_anchor.get("plane_facade_cutout_local_bounds_sixteenths")
                != sixteenths(collision_local)
                or raw_anchor.get("plane_facade_cutout_world_bounds_sixteenths")
                != sixteenths(
                    _native_plane_facade_cutout_expected_bounds(
                        installed_face, mask
                    )
                )
            ):
                raise EvidenceError("S1 plane facade/cutout source matrix changed")
            expected_cross = (
                {
                    "plane_direction": "up",
                    "facade_direction": "north",
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
                if plane_case_index == 0 and mask == 8
                else None
            )
            if raw_anchor.get("plane_perpendicular_facade_intersection") != expected_cross:
                raise EvidenceError("S1 cross-face plane/facade source golden changed")

    mask_cases = raw_cases[13:19]
    mask_anchors = [
        anchor for case in mask_cases for anchor in case.get("anchors", ())
    ]
    if len(mask_anchors) != 64:
        raise EvidenceError("S1 facade-mask source catalog changed")
    neutral_by_id = {
        block_id: {
            "properties": properties,
            "material_family": family,
            "source_model": source_model,
            "blockstate_sha256": blockstate_sha256,
        }
        for block_id, properties, family, source_model, blockstate_sha256
        in NATIVE_STRUCTURAL_NEUTRAL_FACADE_MATERIALS
    }
    physical_whitelist_ids: set[str] = set()
    for mask, raw_anchor in enumerate(mask_anchors):
        # As above, the rendered manifest omits the optional empty list for
        # facade mask zero while preserving every non-empty facade array.
        raw_facades = raw_anchor.get("facades", [])
        if not isinstance(raw_facades, list):
            raise EvidenceError("S1 facade-mask facade array changed")
        expected_directions = {
            direction
            for bit, direction in enumerate(DIRECTION_VECTORS)
            if mask & (1 << bit)
        }
        if {facade.get("direction") for facade in raw_facades} != expected_directions:
            raise EvidenceError(
                f"S1 facade mask {mask} no longer matches installed directions"
            )
        facade_by_direction = {
            facade["direction"]: facade.get("block_state") for facade in raw_facades
        }
        for state in facade_by_direction.values():
            if isinstance(state, dict):
                _validate_native_structural_whitelist_facade_state(state)
            if (
                isinstance(state, dict)
                and state.get("Name")
                in NATIVE_STRUCTURAL_FACADE_WHITELIST_IDS
            ):
                physical_whitelist_ids.add(state["Name"])

        raw_native = raw_anchor.get("native_neutral_facade_materials", [])
        if not isinstance(raw_native, list):
            raise EvidenceError("S1 native facade material metadata changed")
        native_by_direction = {entry.get("direction"): entry for entry in raw_native}
        for direction, state in facade_by_direction.items():
            block_id = state.get("Name") if isinstance(state, dict) else None
            if block_id not in neutral_by_id:
                continue
            contract = neutral_by_id[block_id]
            metadata = native_by_direction.get(direction)
            if (
                not isinstance(metadata, dict)
                or metadata.get("block_id") != block_id
                or metadata.get("properties") != contract["properties"]
                or metadata.get("material_family") != contract["material_family"]
                or metadata.get("source_model") != contract["source_model"]
                or metadata.get("blockstate_sha256")
                != contract["blockstate_sha256"]
                or metadata.get("persisted_properties")
                != state.get("Properties", {})
            ):
                raise EvidenceError(
                    f"S1 native facade contract changed for {block_id} on mask {mask}"
                )
        if set(native_by_direction) != {
            direction
            for direction, state in facade_by_direction.items()
            if isinstance(state, dict) and state.get("Name") in neutral_by_id
        }:
            raise EvidenceError("S1 native facade metadata has an extra/missing face")

    expected_physical = set(NATIVE_STRUCTURAL_PHYSICAL_FACADE_WHITELIST_IDS)
    if physical_whitelist_ids != expected_physical:
        raise EvidenceError("S1 physically represented facade whitelist changed")

    quartz_controls = {
        1: "isolated",
        2: "adjacent-real-quartz",
        5: "same-block-perpendicular-quartz-vibrant-facades",
        63: "fully-surrounded-all-facade-quads-suppressed",
    }
    for mask, expectation in quartz_controls.items():
        if (
            mask_anchors[mask].get("quartz_facade_appearance_control")
            != expectation
            or mask_anchors[mask].get("quartz_facade_light_policy")
            != "non-emissive-facade-blocklight-zero"
        ):
            raise EvidenceError("S1 quartz facade source controls changed")
    if mask_anchors[1].get("facades") != [
        {"direction": "down", "block_state": {"Name": "ae2:quartz_glass"}}
    ] or mask_anchors[2].get("facades") != [
        {"direction": "up", "block_state": {"Name": "ae2:quartz_vibrant_glass"}}
    ] or mask_anchors[5].get("facades") != [
        {"direction": "down", "block_state": {"Name": "ae2:quartz_glass"}},
        {"direction": "north", "block_state": {"Name": "ae2:quartz_vibrant_glass"}},
    ] or mask_anchors[63].get("facades") != [
        {"direction": direction, "block_state": {"Name": "ae2:quartz_glass"}}
        for direction in DIRECTION_VECTORS
    ]:
        raise EvidenceError("S1 quartz facade appearance layout changed")
    quartz_helpers = [
        fixture
        for case in mask_cases
        for fixture in case.get("fixture_blocks", ())
        if fixture.get("purpose")
        == "quartz-facade-adjacent-real-quartz-appearance"
    ]
    quartz_center = _s1_xyz(
        mask_anchors[2].get("position"), "S1 adjacent quartz facade position"
    )
    if (
        len(quartz_helpers) != 1
        or _s1_xyz(quartz_helpers[0].get("position"), "S1 real quartz helper position")
        != (quartz_center[0], quartz_center[1] + 1, quartz_center[2])
        or quartz_helpers[0].get("block_id") != "ae2:quartz_vibrant_glass"
        or quartz_helpers[0].get("expected_state") != {}
    ):
        raise EvidenceError("S1 adjacent real quartz appearance control changed")
    enclosed_helpers = [
        fixture
        for case in mask_cases
        for fixture in case.get("fixture_blocks", ())
        if fixture.get("purpose")
        == "quartz-facade-fully-surrounded-appearance"
    ]
    enclosed_center = _s1_xyz(
        mask_anchors[63].get("position"), "S1 enclosed quartz facade position"
    )
    if (
        len(enclosed_helpers) != 6
        or {
            _s1_xyz(
                fixture.get("position"), "S1 enclosed quartz helper position"
            ): fixture.get("facade_direction")
            for fixture in enclosed_helpers
        }
        != {
            tuple(
                enclosed_center[axis] + DIRECTION_VECTORS[direction][axis]
                for axis in range(3)
            ): direction
            for direction in DIRECTION_VECTORS
        }
        or any(
            fixture.get("block_id") != "ae2:quartz_glass"
            or fixture.get("expected_state") != {}
            for fixture in enclosed_helpers
        )
        or mask_anchors[63].get("facade_structural_expectation")
        != "zero-facade-layers-remains-custom-cable-plus-six-short-stilts"
    ):
        raise EvidenceError("S1 fully surrounded quartz appearance control changed")

    bookshelf_state = {
        "Name": "minecraft:chiseled_bookshelf",
        "Properties": {
            "facing": "north",
            **{f"slot_{slot}_occupied": "false" for slot in range(6)},
        },
    }
    furnace_state = {
        "Name": "minecraft:furnace",
        "Properties": {"facing": "north", "lit": "false"},
    }
    if (
        {canonical_json(facade["block_state"]) for facade in mask_anchors[13]["facades"]}
        != {canonical_json(bookshelf_state)}
        or {canonical_json(facade["block_state"]) for facade in mask_anchors[14]["facades"]}
        != {canonical_json(furnace_state)}
    ):
        raise EvidenceError("S1 vanilla facade whitelist controls changed")
    expected_normalization = {
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
    if mask_anchors[15].get("native_facade_normalization") != expected_normalization:
        raise EvidenceError("S1 native crafting-monitor normalization changed")

    transparent_case = raw_cases[19]
    transparent_anchors = transparent_case.get("anchors", ())
    transparent_fixtures = transparent_case.get("fixture_blocks")
    if len(transparent_anchors) != 6:
        raise EvidenceError("S1 transparent facade source matrix changed")
    glass_cull_anchor = transparent_anchors[1]
    glass_cull_position = _s1_xyz(
        glass_cull_anchor.get("position"), "S1 glass adjacent-cull anchor position"
    )
    expected_glass_cull = {
        "direction": "up",
        "adjacent_block_state": {"Name": "minecraft:glass"},
        "culled_original_face": "up",
    }
    if (
        glass_cull_anchor.get("facade_adjacent_cull_expected")
        != expected_glass_cull
        or not isinstance(transparent_fixtures, list)
        or len(transparent_fixtures) != 1
        or _s1_xyz(
            transparent_fixtures[0].get("position"),
            "S1 glass adjacent-cull helper position",
        )
        != (glass_cull_position[0], glass_cull_position[1] + 1, glass_cull_position[2])
        or transparent_fixtures[0].get("block_id") != "minecraft:glass"
        or transparent_fixtures[0].get("expected_state") != {}
        or transparent_fixtures[0].get("purpose")
        != "facade-adjacent-skip-rendering-positive"
    ):
        raise EvidenceError("S1 glass skipRendering source golden changed")

    stateful_case = raw_cases[20]
    stateful_anchors = stateful_case.get("anchors", ())
    expected_logs = (("down", "x"), ("up", "y"), ("north", "z"))
    if len(stateful_anchors) != 5:
        raise EvidenceError("S1 stateful facade source matrix changed")
    for raw_anchor, (direction, axis) in zip(
        stateful_anchors[:3], expected_logs, strict=True
    ):
        if raw_anchor.get("facades") != [
            {
                "direction": direction,
                "block_state": {
                    "Name": "minecraft:oak_log",
                    "Properties": {"axis": axis},
                },
            }
        ]:
            raise EvidenceError("S1 axis-rotated oak-log facade golden changed")
    render_anchor = stateful_anchors[1]
    render_position = _s1_xyz(
        render_anchor.get("position"), "S1 adjacent-render anchor position"
    )
    render_fixtures = stateful_case.get("fixture_blocks")
    expected_render = {
        "direction": "up",
        "adjacent_block_state": {
            "Name": "minecraft:oak_log",
            "Properties": {"axis": "y"},
        },
        "retained_original_face": "up",
    }
    if (
        render_anchor.get("facade_adjacent_render_expected") != expected_render
        or render_anchor.get("facade_adjacent_cull_expected") is not None
        or not isinstance(render_fixtures, list)
        or len(render_fixtures) != 1
        or _s1_xyz(
            render_fixtures[0].get("position"),
            "S1 adjacent-render helper position",
        )
        != (render_position[0], render_position[1] + 1, render_position[2])
        or render_fixtures[0].get("block_id") != "minecraft:oak_log"
        or render_fixtures[0].get("expected_state") != {"axis": "y"}
        or render_fixtures[0].get("purpose")
        != "facade-adjacent-non-culling-control"
    ):
        raise EvidenceError("S1 oak-log no-cull source golden changed")

    coexistence = raw_cases[21].get("anchors", ())
    if (
        len(coexistence) != 5
        or [
            anchor.get("facade_structural_expectation") for anchor in coexistence
        ]
        != [
            "facade-only-short-stilt",
            "same-face-anchor-short-no-cutout",
            "same-face-part-cutout",
            "opaque-adjacent-edge-corner-mask",
            "center-anchor-transparent-adjacency",
        ]
    ):
        raise EvidenceError("S1 facade source-parity coexistence matrix changed")

    expected_endpoints = {name for names in NATIVE_STRUCTURAL_ENDPOINTS.values() for name in names}
    if (
        len(positions) != NATIVE_STRUCTURAL_ANCHOR_COUNT
        or custom_count != 351
        or len(fallback_positions) != 9
        or len(installed_face_catalog) != 174
        or any(masks != set(range(16)) for masks in plane_masks.values())
        or p2p_matrix != {
            (part_name, frequency)
            for part_name in NATIVE_STRUCTURAL_P2P_PART_IDS
            for frequency in (0, 0x1234, 0xFFFF)
        }
        or facade_masks != set(range(64))
        or endpoints != expected_endpoints
        or tuple(ordered_endpoint_catalog) != NATIVE_STRUCTURAL_ENDPOINTS_ORDERED
        or schema9_disabled_rendered
        != NATIVE_STRUCTURAL_SCHEMA9_DISABLED_EXPECTATIONS
        or len(schema9_disabled_rendered)
        != NATIVE_STRUCTURAL_SCHEMA9_DISABLED_RENDERED_ANCHOR_COUNT
        or len(positions) - len(schema9_disabled_rendered)
        != NATIVE_STRUCTURAL_SCHEMA9_DISABLED_EMPTY_ANCHOR_COUNT
        or schema9_disabled_triangles
        != NATIVE_STRUCTURAL_SCHEMA9_DISABLED_TRIANGLE_COUNT
        or tuple(sorted(schema9_disabled_resources))
        != NATIVE_STRUCTURAL_SCHEMA9_DISABLED_RESOURCES
    ):
        raise EvidenceError("schema-10 S1 aggregate coverage matrix changed")

    summary = value.get("s1_summary")
    if (
        not isinstance(summary, dict)
        or summary.get("case_count") != 28
        or summary.get("anchor_count") != 360
        or summary.get("custom_anchor_count") != 351
        or summary.get("stock_fallback_anchor_count") != 9
        or summary.get("unit_only_malformed_cases")
        != list(NATIVE_STRUCTURAL_UNIT_ONLY_MALFORMED_CASES)
        or summary.get("unit_only_reason")
        != NATIVE_STRUCTURAL_UNIT_ONLY_REASON
        or summary.get("runtime_oracle_size_bytes")
        != NATIVE_STRUCTURAL_ORACLE_SIZE_BYTES
        or summary.get("runtime_oracle_sha256")
        != NATIVE_STRUCTURAL_ORACLE_SHA256
        or summary.get("runtime_oracle_anchor_count")
        != NATIVE_STRUCTURAL_ORACLE_ANCHOR_COUNT
        or summary.get("runtime_oracle_triangle_count")
        != NATIVE_STRUCTURAL_ORACLE_TRIANGLE_COUNT
        or summary.get("runtime_oracle_identity_count")
        != NATIVE_STRUCTURAL_ORACLE_IDENTITY_COUNT
        or summary.get("runtime_oracle_material_row_count")
        != NATIVE_STRUCTURAL_ORACLE_MATERIAL_ROW_COUNT
        or summary.get("custom_triangle_count") != custom_triangles
        or summary.get("selected_resource_count") != 96
        or summary.get("new_selected_resource_count") != 71
        or summary.get("not_in_legacy_profile_resource_count") != 72
        or summary.get("part_identity_count") != 29
        or summary.get("spin_part_identity_count") != 9
        or summary.get("orientation_state_count") != 336
        or summary.get("plane_mask_count_per_type") != 16
        or summary.get("endpoint_identity_count") != 30
        or summary.get("known_compatible_extension_fallback_count") != 1
        or summary.get("known_compatible_extension_fallback_id")
        != NATIVE_STRUCTURAL_UNKNOWN_EXTENSION_ENDPOINT["block_id"]
        or summary.get("facade_mask_count") != 64
        or summary.get("facade_source_whitelist_id_count") != 24
        or summary.get("facade_physical_whitelist_ids")
        != list(NATIVE_STRUCTURAL_PHYSICAL_FACADE_WHITELIST_IDS)
        or summary.get("facade_java_exhaustive_only_ids")
        != [
            block_id
            for block_id in NATIVE_STRUCTURAL_FACADE_WHITELIST_IDS
            if block_id not in NATIVE_STRUCTURAL_PHYSICAL_FACADE_WHITELIST_IDS
        ]
        or summary.get("raw_logical_matrix_sha256")
        != NATIVE_STRUCTURAL_RAW_LOGICAL_MATRIX_SHA256
        or summary.get("raw_matrix_size_bytes")
        != NATIVE_STRUCTURAL_RAW_MATRIX_SIZE_BYTES
        or summary.get("raw_stripped_logical_matrix_size_bytes")
        != NATIVE_STRUCTURAL_RAW_STRIPPED_LOGICAL_MATRIX_SIZE_BYTES
        or summary.get("raw_stripped_logical_matrix_sha256")
        != NATIVE_STRUCTURAL_RAW_STRIPPED_LOGICAL_MATRIX_SHA256
        or summary.get("rendered_logical_matrix_sha256")
        != NATIVE_STRUCTURAL_RENDERED_LOGICAL_MATRIX_SHA256
        or logical_matrix_sha256
        != NATIVE_STRUCTURAL_RENDERED_LOGICAL_MATRIX_SHA256
        or summary.get("predecessor_projection")
        != {
            "rendered_anchor_count": 10,
            "empty_anchor_count": 350,
            "triangle_count": 608,
            "resource_count": 14,
            "resources": list(NATIVE_STRUCTURAL_SCHEMA9_DISABLED_RESOURCES),
            "rendered_anchors": [
                {
                    "position": dict(zip(("x", "y", "z"), position)),
                    "expected_path": expected_path,
                    "triangle_count": triangle_count,
                }
                for position, (expected_path, triangle_count) in sorted(
                    NATIVE_STRUCTURAL_SCHEMA9_DISABLED_EXPECTATIONS.items()
                )
            ],
        }
        or summary.get("physical_stock_projection")
        != {
            "rendered_anchor_count": 0,
            "empty_anchor_count": 360,
            "triangle_count": 0,
            "resource_count": 0,
            "resources": [],
        }
    ):
        raise EvidenceError("schema-10 S1 summary changed")

    bounds = value.get("bounds")
    floor_policy = value.get("s1_floor_policy")
    expected_custom_summary = value.get("expected_custom_summary")
    expected_fallback_summary = value.get("expected_stock_fallback_summary")
    if (
        not isinstance(bounds, dict)
        or bounds.get("s1_fixture")
        != {"min": [208, 96, 312], "max": [319, 110, 367]}
        or floor_policy
        != {
            "anchor_y": 100,
            "owned_y": [96, 110],
            "support": "none-air-isolated",
            "air_block_id": "minecraft:air",
            "topology_helpers": "placed-before-selected-cable-bus-anchors",
            "world_light_policy": "world-derived-own-and-outward-face-maximum",
            "reason": "stable-native-part-facade-and-endpoint-structural-context",
        }
        or expected_custom_summary
        != {
            "anchor_count": (
                579 + custom_count + NATIVE_STRUCTURAL_LEGACY_ANCHOR_COUNT
            ),
            "triangle_count": (
                26_580
                + custom_triangles
                + NATIVE_STRUCTURAL_LEGACY_TRIANGLE_COUNT
            ),
            "selected_resource_count": len(
                set(legacy.expected_selected_resources)
                | custom_resources
                | legacy_upgrade_resources
            ),
        }
        or expected_fallback_summary
        != {"anchor_count": 16, "triangle_count": 0}
    ):
        raise EvidenceError("schema-10 S1 bounds/global summaries changed")

    expected_selected = tuple(
        sorted(
            set(legacy.expected_selected_resources)
            | custom_resources
            | legacy_upgrade_resources
        )
    )
    legacy_selected_order = schema9["profile"]["selected_resources"]
    expected_selected_order = legacy_selected_order + sorted(
        custom_resources - set(legacy_selected_order)
    )
    if (
        profile.get("selected_resources") != expected_selected_order
        or native_resources.get("gallery_selected_resources")
        != sorted(custom_resources)
    ):
        raise EvidenceError("schema-10 selected structural resource closure changed")
    contract = GalleryContract(
        cases=tuple(parsed_cases),
        expected_selected_resources=expected_selected,
        expected_custom_anchor_count=(
            legacy.expected_custom_anchor_count
            + custom_count
            + NATIVE_STRUCTURAL_LEGACY_ANCHOR_COUNT
        ),
        expected_custom_triangle_count=(
            legacy.expected_custom_triangle_count
            + custom_triangles
            + NATIVE_STRUCTURAL_LEGACY_TRIANGLE_COUNT
        ),
        stock_fallback_positions=tuple(
            sorted(
                (
                    set(legacy.stock_fallback_positions)
                    - legacy_upgrade_positions
                )
                | set(fallback_positions)
            )
        ),
        dense_positions=legacy.dense_positions,
        expected_dense_triangle_count=legacy.expected_dense_triangle_count,
        expected_dense_material_triangles=legacy.expected_dense_material_triangles,
        schema_version=10,
        signature_schema_version=10,
        m2_regression_positions=legacy.m2_regression_positions,
        drive_component_pair=legacy.drive_component_pair,
        m3a_regression_positions=legacy.m3a_regression_positions,
        extended_drive_component_pair=legacy.extended_drive_component_pair,
        extended_drive_mirror_pair=legacy.extended_drive_mirror_pair,
        extension_positions=legacy.extension_positions,
        m3b_regression_positions=legacy.m3b_regression_positions,
        glass_positions=legacy.glass_positions,
        crafting_positions=legacy.crafting_positions,
        quantum_positions=legacy.quantum_positions,
        m3_completion_positions=legacy.m3_completion_positions,
        native_structural_positions=tuple(sorted(positions)),
        native_structural_legacy_upgrade_positions=tuple(
            sorted(legacy_upgrade_positions)
        ),
    )
    evidence = dict(legacy_evidence)
    evidence.update(
        {
            "sha256": digest,
            "schema_version": 10,
            "signature_schema_version": 10,
            "case_count": len(parsed_cases),
            "anchor_count": sum(len(case.anchors) for case in parsed_cases),
            "frozen_schema9_view_sha256": schema9_sha256,
            "profile": {
                **legacy_evidence["profile"],
                "coverage_milestone": NATIVE_STRUCTURAL_COVERAGE,
                "selected_resource_count": len(profile["selected_resources"]),
                "custom_material_count": len(expected_selected),
                "native_structural_part_count": 29,
                "native_structural_endpoint_count": 30,
                "native_structural_resource_manifest_sha256": (
                    NATIVE_STRUCTURAL_RESOURCE_MANIFEST_SHA256
                ),
            },
            "expected_custom_summary": value.get("expected_custom_summary"),
            "expected_stock_fallback_summary": value.get("expected_stock_fallback_summary"),
            "s1_summary": summary,
            "native_structural_legacy_upgrades": legacy_upgrade_evidence,
        }
    )
    return contract, evidence


def _validate_native_structural_glassential_override(value: Any) -> None:
    """Fail closed unless schema 10 pins the exact full-pack glass override."""
    if value != NATIVE_STRUCTURAL_GLASSENTIAL_FULL_PACK_OVERRIDE:
        raise EvidenceError(
            "schema-10 Glassential full-pack facade override contract changed"
        )


def _parse_schema9_cases(
    value: dict[str, Any], digest: str
) -> tuple[GalleryContract, dict[str, Any]]:
    if value.get("signature_schema_version") != 9:
        raise EvidenceError("schema-9 gallery must use signature schema 9")
    profile = value.get("profile")
    selected_resources = (
        profile.get("selected_resources") if isinstance(profile, dict) else None
    )
    exact_supported = {
        "route": M3_COMPLETION_ROUTE,
        "block_ids": list(M3_COMPLETION_BLOCK_ENTITY_IDS),
        "block_entity_ids": M3_COMPLETION_BLOCK_ENTITY_IDS,
        "render_policy": {
            "paint": "persisted-non-lumen-splotches",
            "sky_stone_chests": "static-closed-no-contents",
            "crank": "static-neutral-zero-degrees",
            "inscriber": "static-neutral-no-items-no-animation",
            "spatial_pylon": "local-topology-static-offline-unknown",
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
    }
    exact_profile = {
        "profile_id": M3_COMPLETION_ROUTE,
        "profile_size_bytes": 9_405,
        "profile_sha256": M3_COMPLETION_PROFILE_SHA256,
        "artifact": "appliedenergistics2-19.2.17.jar",
        "artifact_size_bytes": 8_230_896,
        "artifact_sha256": (
            "460d779a0609b81409907d9956de8f6f70a1b0912257e3e5c3c7e75ac9630e95"
        ),
        "source_tag": "neoforge/v19.2.17",
        "source_commit": "79ee2c704ad62941a426c26b1cb1f76ef5b2ee5a",
        "source_sha256": (
            "d2f451203cb61c2d21fae52c683083d2f72441ca7d26725f4df5934290492e6a"
        ),
        "path_count": 33,
        "required_resources_manifest_sha256": (
            "3faf7f29e2878f5525541bad855cbc66b6d45786dc8fc6ee29a6fbbf4878cca1"
        ),
        "emitted_static_texture_manifest_sha256": (
            "4652a3110adac720845b559b990dabd32e55887d43bc113f85856052bd0a8a05"
        ),
        "fallback_texture_manifest_sha256": (
            "aaff6681328dfc441a01f5a014182e914a82598395b7a594809b4652281a1146"
        ),
        "fallback_only_resources": [
            "ae2:block/sky_stone_block",
            "ae2:block/smooth_sky_stone_block",
        ],
    }
    if (
        not isinstance(profile, dict)
        or profile.get("coverage_milestone") != "M3f"
        or not isinstance(selected_resources, list)
        or len(selected_resources) != 217
        or len(set(selected_resources)) != 217
        or selected_resources[-15:] != list(M3_COMPLETION_RESOURCES)
        or profile.get("m3_completion_resources") != list(M3_COMPLETION_RESOURCES)
        or profile.get("supported_m3_completion") != exact_supported
        or profile.get("m3_completion_profile") != exact_profile
    ):
        raise EvidenceError("schema-9 gallery does not select the exact M3f route profile")
    exact_summary = {
        "case_count": 7,
        "anchor_count": 78,
        "custom_anchor_count": 78,
        "custom_triangle_count": 2_822,
        "stock_fallback_anchor_count": 0,
        "emitted_resource_count": 15,
        "selected_resource_count": 15,
        "new_selected_resource_count": 15,
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
            "resource_count": 4,
            "resources": list(M3_COMPLETION_STOCK_RESOURCES),
        },
        "static_policy": "structural-no-contents-items-fluids-or-activity",
    }
    exact_floor = {
        "owned_y": [96, 106],
        "ordinary_context": "air",
        "paint_support": "solid-backing-side-first",
        "paint_support_block_id": "minecraft:smooth_stone",
        "crank_support": "valid-crankable-opposite-facing-first",
        "crank_support_block_id": "ae2:charger",
        "spatial_pylon_context": "native-connected-components-first",
        "machine_contents_items_fluids_activity": "excluded",
        "reason": "structural-static-projection-only",
    }
    if (
        value.get("case_count") != 122
        or value.get("anchor_count") != 597
        or value.get("expected_custom_summary")
        != {
            "anchor_count": 579,
            "selected_resource_count": 218,
            "triangle_count": 26_580,
        }
        or value.get("expected_stock_fallback_summary")
        != {"anchor_count": 17, "triangle_count": 0}
        or value.get("m3f_summary") != exact_summary
        or value.get("m3f_floor_policy") != exact_floor
        or value.get("bounds", {}).get("m3f_fixture")
        != {"min": [280, 96, 208], "max": [319, 106, 230]}
    ):
        raise EvidenceError("schema-9 M3f summary/bounds/structural contract changed")

    schema8 = _schema8_view(value)
    schema8_payload = canonical_json(schema8, pretty=True).encode("utf-8")
    schema8_sha256 = sha256_bytes(schema8_payload)
    if schema8_sha256 != SCHEMA8_CANONICAL_SHA256:
        raise EvidenceError("schema-9 does not embed the byte-frozen accepted schema-8 view")
    legacy, legacy_evidence = _parse_schema8_cases(schema8, schema8_sha256)

    all_cases = value.get("cases")
    if not isinstance(all_cases, list) or len(all_cases) != 122:
        raise EvidenceError("schema-9 gallery must contain exactly 122 cases")
    raw_cases = [case for case in all_cases if case.get("milestone") == "M3f"]
    if (
        [case.get("case_id") for case in raw_cases]
        != [f"ae2-m3f-{index:02d}" for index in range(1, 8)]
        or any(case.get("route") != M3_COMPLETION_ROUTE for case in raw_cases)
        or [len(case.get("anchors", ())) for case in raw_cases]
        != [23, 8, 6, 24, 10, 3, 4]
        or sha256_text(canonical_json(raw_cases, pretty=True))
        != M3F_CASES_CANONICAL_SHA256
    ):
        raise EvidenceError("schema-9 exact M3f case matrix changed")
    invalid_component_specs = (
        (
            "spatial-pylon-perpendicular-component-unformed",
            {
                (310, 104, 214),
                (311, 104, 214),
                (310, 104, 215),
            },
        ),
        (
            "spatial-pylon-branched-component-unformed",
            {
                (316, 103, 214),
                (315, 103, 214),
                (317, 103, 214),
                (316, 104, 214),
            },
        ),
    )
    for raw_case, (label, positions) in zip(
        raw_cases[-2:], invalid_component_specs, strict=True
    ):
        anchors = raw_case.get("anchors")
        if (
            raw_case.get("label") != label
            or raw_case.get("category")
            != "spatial-pylon-invalid-component-unformed"
            or raw_case.get("fixture_blocks") != []
            or not isinstance(anchors, list)
            or {
                tuple(anchor.get("position", {}).get(axis) for axis in ("x", "y", "z"))
                for anchor in anchors
            }
            != positions
            or any(
                anchor.get("expected_path") != "custom-m3f"
                or anchor.get("pylon_axis") != "x"
                or anchor.get("pylon_axis_position") != "none"
                or anchor.get("expected_material_triangles")
                != {
                    "ae2:block/spatial_pylon/base": 12,
                    "ae2:block/spatial_pylon/dim": 12,
                }
                or anchor.get("expected_triangle_count") != 24
                or anchor.get("fallback_reason") is not None
                for anchor in anchors
            )
        ):
            raise EvidenceError(
                "schema-9 M3f invalid pylon component must be custom unformed base-plus-dim"
            )

    parsed_cases = list(legacy.cases)
    completion_positions: set[tuple[int, int, int]] = set()
    fallback_positions: list[tuple[int, int, int]] = []
    custom_count = 0
    custom_triangles = 0
    custom_resources: set[str] = set()
    stock_rendered = 0
    stock_empty = 0
    stock_triangles = 0
    for raw_case in raw_cases:
        anchors: list[AnchorContract] = []
        for raw_anchor in raw_case["anchors"]:
            raw_position = raw_anchor.get("position")
            if not isinstance(raw_position, dict) or set(raw_position) != {"x", "y", "z"}:
                raise EvidenceError("M3f anchor position is not exact xyz metadata")
            position = tuple(raw_position[axis] for axis in ("x", "y", "z"))
            if (
                not all(isinstance(coordinate, int) for coordinate in position)
                or position in completion_positions
                or not (280 <= position[0] <= 319 and 96 <= position[1] <= 106 and 208 <= position[2] <= 230)
            ):
                raise EvidenceError(f"M3f anchor position is invalid: {position}")
            completion_positions.add(position)
            block_id = raw_anchor.get("block_id")
            if (
                block_id not in M3_COMPLETION_BLOCK_ENTITY_IDS
                or raw_anchor.get("expected_block_entity_id")
                != M3_COMPLETION_BLOCK_ENTITY_IDS[block_id]
            ):
                raise EvidenceError(f"M3f anchor {position} block/BE identity changed")
            stock_materials = raw_anchor.get("expected_stock_material_triangles")
            stock_triangle_count = raw_anchor.get("expected_stock_triangle_count")
            if (
                stock_materials != M3_COMPLETION_STOCK_MATERIALS[block_id]
                or stock_triangle_count != sum(stock_materials.values())
            ):
                raise EvidenceError(f"M3f anchor {position} stock projection changed")
            stock_triangles += stock_triangle_count
            if stock_triangle_count:
                stock_rendered += 1
            else:
                stock_empty += 1
            expected_path = raw_anchor.get("expected_path")
            expected_triangle_count = raw_anchor.get("expected_triangle_count")
            material_value = raw_anchor.get("expected_material_triangles", {})
            if (
                not isinstance(material_value, dict)
                or not all(
                    isinstance(resource, str)
                    and isinstance(count, int)
                    and count > 0
                    for resource, count in material_value.items()
                )
            ):
                raise EvidenceError(f"M3f anchor {position} material contract is invalid")
            fallback_reason = raw_anchor.get("fallback_reason")
            if expected_path == "custom-m3f":
                if (
                    not isinstance(expected_triangle_count, int)
                    or expected_triangle_count <= 0
                    or sum(material_value.values()) != expected_triangle_count
                    or raw_anchor.get("expected_smart_overlays") != {}
                    or fallback_reason is not None
                ):
                    raise EvidenceError(f"M3f custom anchor {position} metadata changed")
                custom_count += 1
                custom_triangles += expected_triangle_count
                custom_resources.update(material_value)
            elif expected_path == "stock-fallback-m3f":
                if (
                    block_id != SPATIAL_PYLON_BLOCK_ID
                    or expected_triangle_count != 0
                    or material_value
                    or fallback_reason
                    not in {
                        "perpendicular-pylon-neighbors",
                        "three-or-more-pylon-neighbors",
                    }
                ):
                    raise EvidenceError(f"M3f fallback anchor {position} metadata changed")
                fallback_positions.append(position)
            else:
                raise EvidenceError(f"M3f anchor {position} has an unsupported path")
            paint_rows: list[PaintSplotchContract] = []
            if block_id == PAINT_BLOCK_ID:
                splotches = raw_anchor.get("paint_splotches")
                dots = raw_anchor.get("paint_dots_signed_i8")
                if (
                    not isinstance(splotches, list)
                    or not 1 <= len(splotches) <= 21
                    or not isinstance(dots, list)
                    or len(dots) != 256
                    or dots[0] != len(splotches)
                    or any(not isinstance(item, int) or not -128 <= item <= 127 for item in dots)
                    or sha256_bytes(bytes(item & 0xFF for item in dots))
                    != raw_anchor.get("paint_dots_sha256")
                ):
                    raise EvidenceError(f"M3f paint anchor {position} durable dots changed")
                used_dots = 1 + len(splotches) * 2
                if any(dots[index] != 0 for index in range(used_dots, 256)):
                    raise EvidenceError(f"M3f paint anchor {position} durable dots padding changed")
                for index, splotch in enumerate(splotches):
                    resource = splotch.get("resource")
                    rgb = splotch.get("rgb_u8")
                    signed_position = splotch.get("signed_position")
                    backing_side = splotch.get("backing_side")
                    visible_face = splotch.get("visible_face")
                    color_ordinal = splotch.get("color_ordinal")
                    if (
                        not isinstance(signed_position, int)
                        or isinstance(signed_position, bool)
                        or not -128 <= signed_position <= 127
                        or backing_side not in DIRECTION_VECTORS
                        or visible_face != CRAFTING_OPPOSITES.get(backing_side)
                        or not isinstance(color_ordinal, int)
                        or isinstance(color_ordinal, bool)
                        or not 0 <= color_ordinal <= 15
                    ):
                        raise EvidenceError(f"M3f paint anchor {position} splotch encoding changed")
                    encoded = tuple(DIRECTION_VECTORS).index(backing_side) | color_ordinal << 3
                    texture_index = abs(signed_position + encoded) % 3
                    if (
                        resource not in M3_COMPLETION_RESOURCES[:3]
                        or resource != M3_COMPLETION_RESOURCES[texture_index]
                        or not isinstance(rgb, list)
                        or len(rgb) != 3
                        or any(not isinstance(channel, int) or not 0 <= channel <= 255 for channel in rgb)
                        or splotch.get("lumen") is not False
                        or splotch.get("encoded_unsigned") != encoded
                        or splotch.get("texture_index") != texture_index
                        or dots[1 + index * 2] != signed_position
                        or dots[2 + index * 2] != encoded
                    ):
                        raise EvidenceError(f"M3f paint anchor {position} splotch changed")
                    paint_rows.append(
                        PaintSplotchContract(
                            signed_position,
                            backing_side,
                            visible_face,
                            resource,
                            tuple(rgb),
                        )
                    )
            completion = M3CompletionContract(
                block_id=block_id,
                block_state_json=canonical_json(raw_anchor.get("block_state")),
                static_policy=raw_anchor.get("static_policy"),
                paint_splotches=tuple(paint_rows),
                pylon_axis=raw_anchor.get("pylon_axis"),
                pylon_axis_position=raw_anchor.get("pylon_axis_position"),
                expected_stock_material_triangles=tuple(sorted(stock_materials.items())),
                expected_stock_triangle_count=stock_triangle_count,
            )
            anchors.append(
                AnchorContract(
                    case_id=raw_case["case_id"],
                    case_label=raw_case["label"],
                    expected_path=expected_path,
                    position=position,
                    expected_triangle_count=expected_triangle_count,
                    expected_material_triangles=tuple(sorted(material_value.items())),
                    expected_smart_overlays=(),
                    face_parts=(),
                    facades=(),
                    expected_terminal_layers=(),
                    drive=None,
                    fallback_reason=fallback_reason,
                    m3_completion=completion,
                )
            )
        parsed_cases.append(
            CaseContract(
                raw_case["case_id"],
                "M3f",
                M3_COMPLETION_ROUTE,
                raw_case["label"],
                raw_case["category"],
                tuple(anchors),
            )
        )
    legacy_positions = {anchor.position for case in legacy.cases for anchor in case.anchors}
    if (
        len(completion_positions) != 78
        or completion_positions & legacy_positions
        or custom_count != 78
        or custom_triangles != 2_822
        or custom_resources != set(M3_COMPLETION_RESOURCES)
        or fallback_positions
        or stock_rendered != 38
        or stock_empty != 40
        or stock_triangles != 1_872
    ):
        raise EvidenceError("schema-9 M3f aggregate projection changed")

    expected_selected = tuple(
        sorted(set(legacy.expected_selected_resources) | set(M3_COMPLETION_RESOURCES))
    )
    contract = GalleryContract(
        cases=tuple(parsed_cases),
        expected_selected_resources=expected_selected,
        expected_custom_anchor_count=579,
        expected_custom_triangle_count=26_580,
        stock_fallback_positions=tuple(
            sorted((*legacy.stock_fallback_positions, *fallback_positions))
        ),
        dense_positions=legacy.dense_positions,
        expected_dense_triangle_count=legacy.expected_dense_triangle_count,
        expected_dense_material_triangles=legacy.expected_dense_material_triangles,
        schema_version=9,
        signature_schema_version=9,
        m2_regression_positions=legacy.m2_regression_positions,
        drive_component_pair=legacy.drive_component_pair,
        m3a_regression_positions=legacy.m3a_regression_positions,
        extended_drive_component_pair=legacy.extended_drive_component_pair,
        extended_drive_mirror_pair=legacy.extended_drive_mirror_pair,
        extension_positions=legacy.extension_positions,
        m3b_regression_positions=legacy.m3b_regression_positions,
        glass_positions=legacy.glass_positions,
        crafting_positions=legacy.crafting_positions,
        quantum_positions=legacy.quantum_positions,
        m3_completion_positions=tuple(sorted(completion_positions)),
    )
    evidence = dict(legacy_evidence)
    evidence.update(
        {
            "sha256": digest,
            "schema_version": 9,
            "signature_schema_version": 9,
            "case_count": 122,
            "anchor_count": 597,
            "frozen_schema8_view_sha256": schema8_sha256,
            "profile": {
                **legacy_evidence["profile"],
                "coverage_milestone": "M3f",
                "selected_resource_count": 217,
                "custom_material_count": 218,
                "m3_completion_block_id_count": 6,
                "m3_completion_resource_count": 15,
                "m3_completion_profile_sha256": M3_COMPLETION_PROFILE_SHA256,
            },
            "expected_custom_summary": value["expected_custom_summary"],
            "expected_stock_fallback_summary": value[
                "expected_stock_fallback_summary"
            ],
            "m3f_summary": exact_summary,
        }
    )
    return contract, evidence


def _parse_schema8_cases(
    value: dict[str, Any], digest: str
) -> tuple[GalleryContract, dict[str, Any]]:
    if value.get("signature_schema_version") != 8:
        raise EvidenceError("schema-8 gallery must use signature schema 8")
    profile = value.get("profile")
    selected_resources = profile.get("selected_resources") if isinstance(profile, dict) else None
    exact_supported = {
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
    }
    if (
        not isinstance(profile, dict)
        or profile.get("coverage_milestone") != "M3e"
        or not isinstance(selected_resources, list)
        or len(selected_resources) != 202
        or len(set(selected_resources)) != 202
        or selected_resources[-2:] != list(QUANTUM_NEW_RESOURCES)
        or profile.get("quantum_resources") != list(QUANTUM_RESOURCES)
        or profile.get("supported_quantum_bridge") != exact_supported
    ):
        raise EvidenceError("schema-8 gallery does not select the exact M3e quantum profile")
    exact_m3e_summary = {
        "case_count": 3,
        "anchor_count": 27,
        "custom_anchor_count": 27,
        "custom_triangle_count": 1_188,
        "stock_fallback_anchor_count": 0,
        "emitted_resource_count": 4,
        "selected_resource_count": 4,
        "new_selected_resource_count": 2,
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
    }
    if (
        value.get("case_count") != 115
        or value.get("anchor_count") != 519
        or value.get("expected_custom_summary")
        != {
            "anchor_count": 501,
            "selected_resource_count": 203,
            "triangle_count": 23_758,
        }
        or value.get("expected_stock_fallback_summary")
        != {"anchor_count": 17, "triangle_count": 0}
        or value.get("m3e_summary") != exact_m3e_summary
        or value.get("bounds", {}).get("m3e_fixture")
        != {"min": [281, 97, 269], "max": [294, 105, 278]}
        or value.get("m3e_floor_policy")
        != {
            "support": "none-air-isolated",
            "owned_y": [97, 105],
            "air_block_id": "minecraft:air",
            "reason": "preserve-ao-255-and-world-light-contract",
            "formed_check": "two-consecutive-stable-checks",
            "power_overlay_policy": QUANTUM_STATIC_POLICY,
            "particle_policy": QUANTUM_PARTICLE_POLICY,
        }
    ):
        raise EvidenceError("schema-8 M3e summary/bounds/air contract changed")

    schema7 = _schema7_view(value)
    schema7_payload = canonical_json(schema7, pretty=True).encode("utf-8")
    schema7_sha256 = sha256_bytes(schema7_payload)
    if schema7_sha256 != SCHEMA7_CANONICAL_SHA256:
        raise EvidenceError("schema-8 does not embed the byte-frozen accepted schema-7 view")
    legacy, legacy_evidence = _parse_schema7_cases(schema7, schema7_sha256)

    all_cases = value.get("cases")
    if not isinstance(all_cases, list) or len(all_cases) != 115:
        raise EvidenceError("schema-8 gallery must contain exactly 115 cases")
    raw_cases = [case for case in all_cases if case.get("milestone") == "M3e"]
    if [case.get("case_id") for case in raw_cases] != [
        "ae2-m3e-01", "ae2-m3e-02", "ae2-m3e-03"
    ]:
        raise EvidenceError("schema-8 gallery must contain ordered M3e cases 01..03")
    if any(case.get("route") != QUANTUM_ROUTE for case in raw_cases):
        raise EvidenceError("schema-8 M3e route metadata changed")

    layouts = _m3e_layouts()
    quantum_positions = {
        spec["position"]
        for layout in layouts
        for spec in layout["anchors"]
    }
    legacy_positions = {
        anchor.position for case in legacy.cases for anchor in case.anchors
    }
    if (
        len(quantum_positions) != 27
        or quantum_positions & legacy_positions
        or any(
            not (281 <= x <= 294 and 97 <= y <= 105 and 269 <= z <= 278)
            for x, y, z in quantum_positions
        )
        or not ({287, 288} <= {position[0] for position in quantum_positions})
    ):
        raise EvidenceError("schema-8 M3e route isolation/chunk-boundary contract changed")

    parsed_cases = list(legacy.cases)
    resource_union: set[str] = set()
    material_totals_by_case: list[Counter[str]] = []
    for case_index, (raw_case, layout) in enumerate(
        zip(raw_cases, layouts, strict=True), start=1
    ):
        if (
            raw_case.get("label") != layout["label"]
            or raw_case.get("category") != "quantum-bridge-plane-orientation"
            or raw_case.get("route") != QUANTUM_ROUTE
            or raw_case.get("fixture_blocks") != []
        ):
            raise EvidenceError(f"M3e case {case_index:02d} identity/context changed")
        raw_anchors = raw_case.get("anchors")
        if not isinstance(raw_anchors, list) or len(raw_anchors) != 9:
            raise EvidenceError(f"M3e case {case_index:02d} anchor count changed")
        anchors: list[AnchorContract] = []
        case_materials: Counter[str] = Counter()
        for raw_anchor, spec in zip(raw_anchors, layout["anchors"], strict=True):
            position = spec["position"]
            role = spec["role"]
            plane = layout["plane"]
            block_id = QUANTUM_LINK_BLOCK_ID if role == "link" else QUANTUM_RING_BLOCK_ID
            primitives, materials, connections = _expected_quantum_metadata(
                position, role, quantum_positions
            )
            expected_anchor = {
                "position": _manifest_position(position),
                "block_id": block_id,
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
                "expected_connections": connections,
                "expected_triangle_count": sum(materials.values()),
                "expected_material_triangles": materials,
                "expected_smart_overlays": {},
                "expected_quantum_primitives": primitives,
            }
            if raw_anchor != expected_anchor:
                raise EvidenceError(
                    f"M3e anchor {position} metadata differs from exact static-off topology"
                )
            quantum = QuantumContract(
                block_id=block_id,
                plane=plane,
                role=role,
                formed=True,
                waterlogged=False,
                connections=tuple(connection["direction"] for connection in connections),
                primitives=tuple(
                    QuantumPrimitiveContract(
                        primitive["role"],
                        primitive["resource"],
                        tuple(primitive["bounds_sixteenths"]),
                    )
                    for primitive in primitives
                ),
                power_overlay_policy=QUANTUM_STATIC_POLICY,
                particle_policy=QUANTUM_PARTICLE_POLICY,
            )
            anchors.append(
                AnchorContract(
                    case_id=raw_case["case_id"],
                    case_label=layout["label"],
                    expected_path="custom-m3e",
                    position=position,
                    expected_triangle_count=sum(materials.values()),
                    expected_material_triangles=tuple(sorted(materials.items())),
                    expected_smart_overlays=(),
                    face_parts=(),
                    facades=(),
                    expected_terminal_layers=(),
                    drive=None,
                    fallback_reason=None,
                    quantum=quantum,
                )
            )
            resource_union.update(materials)
            case_materials.update(materials)
        if (
            sum(anchor.expected_triangle_count or 0 for anchor in anchors) != 396
            or case_materials
            != Counter(
                {
                    QUANTUM_LINK_RESOURCE: 12,
                    QUANTUM_GLASS_RESOURCE: 48,
                    QUANTUM_COVERED_RESOURCE: 144,
                    QUANTUM_RING_RESOURCE: 192,
                }
            )
            or [anchor.quantum.role for anchor in anchors].count("link") != 1
            or [anchor.quantum.role for anchor in anchors].count("corner") != 4
            or [anchor.quantum.role for anchor in anchors].count("edge") != 4
        ):
            raise EvidenceError(f"M3e case {case_index:02d} topology/material totals changed")
        material_totals_by_case.append(case_materials)
        parsed_cases.append(
            CaseContract(
                raw_case["case_id"],
                "M3e",
                QUANTUM_ROUTE,
                layout["label"],
                "quantum-bridge-plane-orientation",
                tuple(anchors),
            )
        )
    if resource_union != set(QUANTUM_RESOURCES):
        raise EvidenceError("schema-8 M3e emitted resource closure changed")

    expected_selected = tuple(
        sorted(set(legacy.expected_selected_resources) | set(QUANTUM_RESOURCES))
    )
    contract = GalleryContract(
        cases=tuple(parsed_cases),
        expected_selected_resources=expected_selected,
        expected_custom_anchor_count=501,
        expected_custom_triangle_count=23_758,
        stock_fallback_positions=legacy.stock_fallback_positions,
        dense_positions=legacy.dense_positions,
        expected_dense_triangle_count=legacy.expected_dense_triangle_count,
        expected_dense_material_triangles=legacy.expected_dense_material_triangles,
        schema_version=8,
        signature_schema_version=8,
        m2_regression_positions=legacy.m2_regression_positions,
        drive_component_pair=legacy.drive_component_pair,
        m3a_regression_positions=legacy.m3a_regression_positions,
        extended_drive_component_pair=legacy.extended_drive_component_pair,
        extended_drive_mirror_pair=legacy.extended_drive_mirror_pair,
        extension_positions=legacy.extension_positions,
        m3b_regression_positions=legacy.m3b_regression_positions,
        glass_positions=legacy.glass_positions,
        crafting_positions=legacy.crafting_positions,
        quantum_positions=tuple(sorted(quantum_positions)),
    )
    evidence = dict(legacy_evidence)
    evidence.update(
        {
            "sha256": digest,
            "schema_version": 8,
            "signature_schema_version": 8,
            "case_count": 115,
            "anchor_count": 519,
            "frozen_schema7_view_sha256": schema7_sha256,
            "profile": {
                **legacy_evidence["profile"],
                "coverage_milestone": "M3e",
                "selected_resource_count": 202,
                "custom_material_count": 203,
                "quantum_bridge_block_id_count": 2,
                "quantum_bridge_resource_count": 4,
            },
            "expected_custom_summary": value["expected_custom_summary"],
            "expected_stock_fallback_summary": value["expected_stock_fallback_summary"],
            "m3e_summary": exact_m3e_summary,
        }
    )
    return contract, evidence


def _parse_schema7_cases(
    value: dict[str, Any], digest: str
) -> tuple[GalleryContract, dict[str, Any]]:
    if value.get("signature_schema_version") != 7:
        raise EvidenceError("schema-7 gallery must use signature schema 7")
    profile = value.get("profile")
    selected_resources = profile.get("selected_resources") if isinstance(profile, dict) else None
    exact_supported = {
        "block_kinds": CRAFTING_BLOCK_KINDS,
        "route": CRAFTING_ROUTE,
        "state_gate": {"formed": True},
        "connection_policy": "six-direct-abstract-crafting-unit-neighbors",
        "compatible_extension_policy": "atomic-original-resource-fallback",
        "powered_overlay_policy": "persisted-powered-fullbright-15",
        "monitor_display_policy": CRAFTING_MONITOR_DISPLAY_POLICY,
        "monitor_paint_ordinals": list(range(17)),
    }
    if (
        not isinstance(profile, dict)
        or profile.get("coverage_milestone") != "M3d"
        or not isinstance(selected_resources, list)
        or len(selected_resources) != 200
        or len(set(selected_resources)) != 200
        or selected_resources[-15:] != list(CRAFTING_RESOURCES)
        or profile.get("crafting_resources") != list(CRAFTING_RESOURCES)
        or profile.get("crafting_resource_manifest_sha256")
        != CRAFTING_RESOURCE_MANIFEST_SHA256
        or profile.get("crafting_texture_manifest_sha256")
        != CRAFTING_TEXTURE_MANIFEST_SHA256
        or profile.get("supported_formed_crafting") != exact_supported
    ):
        raise EvidenceError("schema-7 gallery does not select the exact M3d crafting profile")
    exact_m3d_summary = {
        "case_count": 9,
        "anchor_count": 86,
        "custom_anchor_count": 85,
        "custom_triangle_count": 4_306,
        "stock_fallback_anchor_count": 1,
        "selected_resource_count": 15,
        "new_selected_resource_count": 15,
        "block_id_count": 8,
        "paint_ordinal_count": 17,
        "monitor_display_policy": CRAFTING_MONITOR_DISPLAY_POLICY,
        "fully_enclosed_zero_geometry_anchor_count": 1,
        "fully_enclosed_zero_geometry_evidence_status": (
            "not-renderer-provenance-distinguishable-in-prbm"
        ),
    }
    if (
        value.get("case_count") != 112
        or value.get("anchor_count") != 492
        or value.get("expected_custom_summary")
        != {
            "anchor_count": 474,
            "selected_resource_count": 201,
            "triangle_count": 22_570,
        }
        or value.get("expected_stock_fallback_summary")
        != {"anchor_count": 17, "triangle_count": 0}
        or value.get("m3d_summary") != exact_m3d_summary
        or value.get("bounds", {}).get("m3d_fixture")
        != {"min": [296, 97, 260], "max": [319, 105, 299]}
        or value.get("bounds", {}).get("m3d_support_floor")
        != {"min": [296, 97, 260], "max": [319, 97, 299]}
        or value.get("bounds", {}).get("m3d_air")
        != {"min": [296, 98, 260], "max": [319, 105, 299]}
        or value.get("m3d_floor_policy")
        != {
            "primary_anchor_y": 100,
            "support_y": 97,
            "support_block_id": "minecraft:smooth_stone",
            "ordinary_clear_y": [98, 105],
            "air_block_id": "minecraft:air",
            "owned_y": [97, 105],
            "exceptions": [
                "real-powered-grid-context",
                "vertical-monitor-storage-pairs",
                "formed-multiblock-members",
            ],
        }
    ):
        raise EvidenceError("schema-7 M3d summary/bounds/floor contract changed")

    schema6 = _schema6_view(value)
    schema6_payload = canonical_json(schema6, pretty=True).encode("utf-8")
    schema6_sha256 = sha256_bytes(schema6_payload)
    if schema6_sha256 != "2d4fbba58ea2c4d3ed741e93a8dd9857523cac9cda021ffd3111e6ac51aec602":
        raise EvidenceError("schema-7 does not embed the byte-frozen accepted schema-6 view")
    legacy, legacy_evidence = _parse_schema6_cases(schema6, schema6_sha256)

    all_cases = value.get("cases")
    if not isinstance(all_cases, list) or len(all_cases) != 112:
        raise EvidenceError("schema-7 gallery must contain exactly 112 cases")
    raw_cases = [case for case in all_cases if case.get("milestone") == "M3d"]
    if [case.get("case_id") for case in raw_cases] != [
        f"ae2-m3d-{number:02d}" for number in range(1, 10)
    ]:
        raise EvidenceError("schema-7 gallery must contain ordered M3d cases 01..09")
    if any(case.get("route") != CRAFTING_ROUTE for case in raw_cases):
        raise EvidenceError("schema-7 M3d route metadata changed")

    layouts = _m3d_layouts()
    crafting_positions = {
        anchor["position"] for layout in layouts for anchor in layout["anchors"]
    }
    context_positions = {
        fixture["position"] for layout in layouts for fixture in layout["fixtures"]
    }
    legacy_positions = {
        anchor.position for case in legacy.cases for anchor in case.anchors
    }
    vibrant_positions = {
        position
        for _label, _category, layout, _fixtures in M3C_CASE_LAYOUTS
        for position, block_id in layout
        if block_id == "ae2:quartz_vibrant_glass"
    }
    owned_positions = crafting_positions | context_positions
    if (
        len(crafting_positions) != 86
        or crafting_positions & legacy_positions
        or any(position[0] < 297 for position in owned_positions)
        or min(
            sum(abs(position[axis] - vibrant[axis]) for axis in range(3))
            for position in owned_positions
            for vibrant in vibrant_positions
        )
        <= 15
    ):
        raise EvidenceError("schema-7 M3d route-isolation contract changed")

    parsed_cases = list(legacy.cases)
    resource_union: set[str] = set()
    triangle_total = 0
    custom_count = 0
    fallback_positions: set[tuple[int, int, int]] = set()
    paint_ordinals: set[int] = set()
    monitor_facings: set[str] = set()
    monitor_spins: set[int] = set()
    powered_count = 0
    enclosed_zero_count = 0
    expected_case_triangles = [600, 142, 152, 174, 184, 234, 304, 2_516, 0]
    for case_index, (raw_case, layout) in enumerate(
        zip(raw_cases, layouts, strict=True), start=1
    ):
        expected_fixtures = [
            _manifest_fixture(fixture) for fixture in layout["fixtures"]
        ]
        if (
            raw_case.get("label") != layout["label"]
            or raw_case.get("category") != layout["category"]
            or raw_case.get("route") != CRAFTING_ROUTE
            or raw_case.get("fixture_blocks") != expected_fixtures
        ):
            raise EvidenceError(f"M3d case {case_index:02d} identity/context changed")
        raw_anchors = raw_case.get("anchors")
        if not isinstance(raw_anchors, list) or len(raw_anchors) != len(layout["anchors"]):
            raise EvidenceError(f"M3d case {case_index:02d} anchor count changed")
        anchors: list[AnchorContract] = []
        for raw_anchor, spec in zip(raw_anchors, layout["anchors"], strict=True):
            position = spec["position"]
            block_id = spec["block_id"]
            kind = CRAFTING_BLOCK_KINDS[block_id]
            powered = bool(spec.get("powered", False))
            facing = spec.get("facing")
            spin = spec.get("spin")
            paint = spec.get("paint")
            expected_path = spec.get("expected_path", "custom-m3d")
            state: dict[str, Any] = {"formed": True, "powered": powered}
            if kind == "monitor":
                state.update({"facing": facing, "spin": spin})
            expected_anchor: dict[str, Any] = {
                "position": _manifest_position(position),
                "block_id": block_id,
                "cable_id": None,
                "expected_path": expected_path,
                "crafting_kind": kind,
                "block_state": state,
            }
            faces: list[dict[str, Any]] = []
            materials: dict[str, int] = {}
            connections: list[dict[str, str]] = []
            fallback_reason: str | None = None
            if expected_path == "stock-fallback-m3d":
                fallback_reason = "compatible-extension-crafting-neighbor"
                expected_anchor.update(
                    {
                        "fallback_reason": fallback_reason,
                        "expected_triangle_count": 0,
                        "compatible_neighbor_block_ids": [
                            "megacells:mega_crafting_unit",
                            "expandedae:exp_crafting_unit",
                        ],
                    }
                )
                fallback_positions.add(position)
            else:
                faces, materials, connections = _expected_crafting_metadata(
                    position, kind, powered, facing, paint, crafting_positions
                )
                expected_anchor.update(
                    {
                        "expected_connections": connections,
                        "expected_crafting_faces": faces,
                        "expected_triangle_count": sum(materials.values()),
                        "expected_material_triangles": materials,
                        "expected_smart_overlays": {},
                    }
                )
                resource_union.update(materials)
                triangle_total += sum(materials.values())
                custom_count += 1
            expected_anchor["formation_policy"] = "real-complete-cpu-no-forced-state"
            if kind == "monitor":
                expected_anchor["painted_color_ordinal"] = paint
                expected_anchor["monitor_display_policy"] = CRAFTING_MONITOR_DISPLAY_POLICY
                paint_ordinals.add(paint)
                monitor_facings.add(facing)
                monitor_spins.add(spin)
            if raw_anchor != expected_anchor:
                raise EvidenceError(
                    f"M3d anchor {position} metadata differs from exact formed topology"
                )
            primitive_contracts = tuple(
                CraftingFaceContract(
                    face["direction"],
                    tuple(
                        CraftingPrimitiveContract(
                            primitive["role"],
                            primitive["resource"],
                            tuple(primitive["bounds_sixteenths"]),
                            tuple(primitive["rgb_u8"]),
                            primitive["light_policy"] == "fullbright-15",
                        )
                        for primitive in face["primitives"]
                    ),
                )
                for face in faces
            )
            fully_enclosed = position == (305, 101, 270)
            if fully_enclosed:
                enclosed_zero_count += 1
            if powered:
                powered_count += 1
            crafting = CraftingContract(
                block_id,
                kind,
                True,
                powered,
                facing,
                spin,
                paint,
                CRAFTING_MONITOR_DISPLAY_POLICY if kind == "monitor" else None,
                tuple(connection["direction"] for connection in connections),
                primitive_contracts,
                fully_enclosed,
            )
            anchors.append(
                AnchorContract(
                    raw_case["case_id"],
                    layout["label"],
                    expected_path,
                    position,
                    0 if expected_path == "stock-fallback-m3d" else sum(materials.values()),
                    tuple(sorted(materials.items())),
                    (),
                    (),
                    (),
                    (),
                    None,
                    fallback_reason,
                    None,
                    crafting,
                )
            )
        parsed_case = CaseContract(
            raw_case["case_id"],
            "M3d",
            CRAFTING_ROUTE,
            layout["label"],
            layout["category"],
            tuple(anchors),
        )
        if sum(anchor.expected_triangle_count or 0 for anchor in anchors) != expected_case_triangles[case_index - 1]:
            raise EvidenceError(f"M3d case {case_index:02d} triangle total changed")
        parsed_cases.append(parsed_case)

    if (
        custom_count != 85
        or triangle_total != 4_306
        or fallback_positions != {(318, 100, 261)}
        or resource_union != set(CRAFTING_RESOURCES)
        or paint_ordinals != set(range(17))
        or monitor_facings != set(DIRECTION_VECTORS)
        or monitor_spins != set(range(4))
        or powered_count != 8
        or enclosed_zero_count != 1
    ):
        raise EvidenceError("schema-7 M3d matrix differs from its exact contract")

    expected_selected = tuple(
        sorted(set(legacy.expected_selected_resources) | set(CRAFTING_RESOURCES))
    )
    contract = GalleryContract(
        cases=tuple(parsed_cases),
        expected_selected_resources=expected_selected,
        expected_custom_anchor_count=474,
        expected_custom_triangle_count=22_570,
        stock_fallback_positions=tuple(
            sorted(set(legacy.stock_fallback_positions) | fallback_positions)
        ),
        dense_positions=legacy.dense_positions,
        expected_dense_triangle_count=legacy.expected_dense_triangle_count,
        expected_dense_material_triangles=legacy.expected_dense_material_triangles,
        schema_version=7,
        signature_schema_version=7,
        m2_regression_positions=legacy.m2_regression_positions,
        drive_component_pair=legacy.drive_component_pair,
        m3a_regression_positions=legacy.m3a_regression_positions,
        extended_drive_component_pair=legacy.extended_drive_component_pair,
        extended_drive_mirror_pair=legacy.extended_drive_mirror_pair,
        extension_positions=legacy.extension_positions,
        m3b_regression_positions=legacy.m3b_regression_positions,
        glass_positions=legacy.glass_positions,
        crafting_positions=tuple(sorted(crafting_positions)),
    )
    evidence = dict(legacy_evidence)
    evidence.update(
        {
            "sha256": digest,
            "schema_version": 7,
            "signature_schema_version": 7,
            "case_count": 112,
            "anchor_count": 492,
            "frozen_schema6_view_sha256": schema6_sha256,
            "profile": {
                **legacy_evidence["profile"],
                "coverage_milestone": "M3d",
                "selected_resource_count": 200,
                "custom_material_count": 201,
                "formed_crafting_block_id_count": 8,
                "formed_crafting_resource_count": 15,
            },
            "expected_custom_summary": value["expected_custom_summary"],
            "expected_stock_fallback_summary": value["expected_stock_fallback_summary"],
            "m3d_summary": exact_m3d_summary,
        }
    )
    return contract, evidence


def _parse_schema6_cases(
    value: dict[str, Any], digest: str
) -> tuple[GalleryContract, dict[str, Any]]:
    if value.get("signature_schema_version") != 6:
        raise EvidenceError("schema-6 gallery must use signature schema 6")
    profile = value.get("profile")
    selected_resources = profile.get("selected_resources") if isinstance(profile, dict) else None
    exact_supported_glass = {
        "block_ids": list(CONNECTED_GLASS_BLOCK_IDS),
        "route": CONNECTED_GLASS_ROUTE,
        "base_resources": list(CONNECTED_GLASS_BASE_RESOURCES),
        "frame_resources": list(CONNECTED_GLASS_FRAME_RESOURCES),
        "variants_share_geometry_and_material_family": True,
        "base_selection": "position-seeded-legacy-random-three-draw-v1",
        "face_triangles": "c1-c2-c3+c1-c3-c4",
        "frame_mask_policy": "four-local-connected-neighbor-absence-bits",
        "render_type": "cutout-binary-alpha",
    }
    if (
        not isinstance(profile, dict)
        or profile.get("coverage_milestone") != "M3c"
        or not isinstance(selected_resources, list)
        or len(selected_resources) != 185
        or len(set(selected_resources)) != 185
        or selected_resources[-19:] != list(CONNECTED_GLASS_SELECTED_RESOURCES)
        or profile.get("glass_resources") != list(CONNECTED_GLASS_SELECTED_RESOURCES)
        or profile.get("supported_connected_glass") != exact_supported_glass
    ):
        raise EvidenceError("schema-6 gallery does not select the exact M3c glass profile")
    exact_m3c_summary = {
        "case_count": 11,
        "anchor_count": 47,
        "custom_anchor_count": 47,
        "custom_triangle_count": 776,
        "stock_fallback_anchor_count": 0,
        "selected_resource_count": 19,
        "new_selected_resource_count": 19,
        "block_ids": list(CONNECTED_GLASS_BLOCK_IDS),
        "base_selection_count": 4,
        "frame_resource_count": 15,
        "frame_mask_occurrences": CONNECTED_GLASS_FRAME_OCCURRENCES,
        "no_frame_mask": "0000",
        "no_frame_face_count": 2,
        "triangle_formula": "2*visibleFaces+2*visibleFrameFaces",
    }
    if (
        value.get("case_count") != 103
        or value.get("anchor_count") != 406
        or value.get("expected_custom_summary")
        != {
            "anchor_count": 389,
            "selected_resource_count": 186,
            "triangle_count": 18_264,
        }
        or value.get("expected_stock_fallback_summary")
        != {"anchor_count": 16, "triangle_count": 0}
        or value.get("m3c_summary") != exact_m3c_summary
        or value.get("bounds", {}).get("m3c_fixture")
        != {"min": [208, 97, 288], "max": [279, 104, 307]}
        or value.get("m3c_floor_policy")
        != {
            "primary_center_y": 100,
            "support_y": 97,
            "support_block_id": "minecraft:smooth_stone",
            "air_y": [98, 99],
            "air_block_id": "minecraft:air",
            "reason": "preserve-disjoint-visible-face-and-light-context",
        }
    ):
        raise EvidenceError("schema-6 M3c summary/bounds/floor contract changed")

    legacy, legacy_evidence = _parse_schema5_cases(_schema5_view(value), digest)
    all_cases = value.get("cases")
    if not isinstance(all_cases, list):
        raise EvidenceError("schema-6 gallery has no cases")
    raw_cases = [case for case in all_cases if case.get("milestone") == "M3c"]
    if [case.get("case_id") for case in raw_cases] != [
        f"ae2-m3c-{number:02d}" for number in range(1, 12)
    ]:
        raise EvidenceError("schema-6 gallery must contain ordered M3c cases 01..11")
    if len(all_cases) != 103 or any(
        case.get("milestone") == "M3c" and case.get("route") != CONNECTED_GLASS_ROUTE
        for case in all_cases
    ):
        raise EvidenceError("schema-6 M3c milestone/route metadata changed")

    glass_positions = {
        position for _label, _category, layout, _fixtures in M3C_CASE_LAYOUTS for position, _block in layout
    }
    opaque_positions = {
        position for _label, _category, _layout, fixtures in M3C_CASE_LAYOUTS for position, _block in fixtures
    }
    legacy_positions = {
        anchor.position for case in legacy.cases for anchor in case.anchors
    }
    if glass_positions & legacy_positions or len(glass_positions) != 47:
        raise EvidenceError("schema-6 M3c positions overlap the frozen M3b contract")

    parsed_cases = list(legacy.cases)
    resource_union: set[str] = set()
    triangle_total = 0
    frame_occurrences: Counter[str] = Counter()
    no_frame_faces = 0
    texture_indexes: set[int] = set()
    for case_index, (raw_case, layout_contract) in enumerate(
        zip(raw_cases, M3C_CASE_LAYOUTS, strict=True), start=1
    ):
        label, category, layout, fixtures = layout_contract
        if (
            raw_case.get("label") != label
            or raw_case.get("category") != category
            or raw_case.get("route") != CONNECTED_GLASS_ROUTE
            or raw_case.get("fixture_blocks")
            != [
                {
                    "position": dict(zip(("x", "y", "z"), position, strict=True)),
                    "block_id": block_id,
                }
                for position, block_id in fixtures
            ]
        ):
            raise EvidenceError(f"M3c case {case_index:02d} identity/context changed")
        raw_anchors = raw_case.get("anchors")
        if not isinstance(raw_anchors, list) or len(raw_anchors) != len(layout):
            raise EvidenceError(f"M3c case {case_index:02d} anchor count changed")
        anchors: list[AnchorContract] = []
        for raw_anchor, (position, block_id) in zip(raw_anchors, layout, strict=True):
            selection, faces, materials, connections, opaque_culled = (
                _expected_connected_glass_metadata(
                    position, glass_positions, opaque_positions
                )
            )
            expected_anchor = {
                "position": dict(zip(("x", "y", "z"), position, strict=True)),
                "block_id": block_id,
                "cable_id": None,
                "expected_path": "custom-m3c",
                "expected_glass_base_selection": selection,
                "expected_glass_faces": faces,
                "expected_opaque_culled_faces": opaque_culled,
                "expected_connections": connections,
                "expected_triangle_count": sum(materials.values()),
                "expected_material_triangles": materials,
                "expected_smart_overlays": {},
            }
            if raw_anchor != expected_anchor:
                raise EvidenceError(f"M3c anchor {position} metadata differs from exact topology")
            face_contracts = tuple(
                GlassFaceContract(
                    face["direction"],
                    face["frame_mask"],
                    face["base_resource"],
                    tuple(tuple(uv) for uv in face["base_uv_corners"]),
                    face["frame_resource"],
                    tuple(tuple(uv) for uv in (face["frame_uv_corners"] or ())),
                )
                for face in faces
            )
            glass = GlassContract(
                block_id,
                selection["draws"]["texture_index"],
                face_contracts,
                tuple(connection["direction"] for connection in connections),
                tuple(opaque_culled),
            )
            anchors.append(
                AnchorContract(
                    raw_case["case_id"],
                    label,
                    "custom-m3c",
                    position,
                    sum(materials.values()),
                    tuple(sorted(materials.items())),
                    (),
                    (),
                    (),
                    (),
                    None,
                    None,
                    glass,
                )
            )
            resource_union.update(materials)
            triangle_total += sum(materials.values())
            texture_indexes.add(selection["draws"]["texture_index"])
            for face in faces:
                if face["frame_mask"] == "0000":
                    no_frame_faces += 1
                else:
                    frame_occurrences[face["frame_mask"]] += 1
        parsed_cases.append(
            CaseContract(
                raw_case["case_id"],
                "M3c",
                CONNECTED_GLASS_ROUTE,
                label,
                category,
                tuple(anchors),
            )
        )

    exact_case_triangles = [48, 56, 56, 56, 72, 88, 48, 116, 96, 120, 20]
    if (
        [sum(anchor.expected_triangle_count or 0 for anchor in case.anchors) for case in parsed_cases[-11:]]
        != exact_case_triangles
        or triangle_total != 776
        or resource_union != set(CONNECTED_GLASS_SELECTED_RESOURCES)
        or dict(sorted(frame_occurrences.items())) != CONNECTED_GLASS_FRAME_OCCURRENCES
        or no_frame_faces != 2
        or texture_indexes != {0, 1, 2, 3}
    ):
        raise EvidenceError("schema-6 M3c matrix differs from its exact contract")
    pair = parsed_cases[-11].anchors
    if len(pair) != 2 or pair[0].glass is None or pair[1].glass is None:
        raise EvidenceError("schema-6 M3c variant pair is incomplete")
    if (
        pair[0].glass.texture_index != pair[1].glass.texture_index
        or pair[0].expected_material_triangles != pair[1].expected_material_triangles
    ):
        raise EvidenceError("schema-6 M3c variant pair selection differs")

    expected_selected = tuple(
        sorted(set(legacy.expected_selected_resources) | set(CONNECTED_GLASS_SELECTED_RESOURCES))
    )
    contract = GalleryContract(
        cases=tuple(parsed_cases),
        expected_selected_resources=expected_selected,
        expected_custom_anchor_count=389,
        expected_custom_triangle_count=18_264,
        stock_fallback_positions=legacy.stock_fallback_positions,
        dense_positions=legacy.dense_positions,
        expected_dense_triangle_count=legacy.expected_dense_triangle_count,
        expected_dense_material_triangles=legacy.expected_dense_material_triangles,
        schema_version=6,
        signature_schema_version=6,
        m2_regression_positions=legacy.m2_regression_positions,
        drive_component_pair=legacy.drive_component_pair,
        m3a_regression_positions=legacy.m3a_regression_positions,
        extended_drive_component_pair=legacy.extended_drive_component_pair,
        extended_drive_mirror_pair=legacy.extended_drive_mirror_pair,
        extension_positions=legacy.extension_positions,
        m3b_regression_positions=tuple(sorted(legacy_positions)),
        glass_positions=tuple(sorted(glass_positions)),
    )
    evidence = dict(legacy_evidence)
    evidence.update(
        {
            "sha256": digest,
            "schema_version": 6,
            "signature_schema_version": 6,
            "case_count": 103,
            "anchor_count": 406,
            "profile": {
                **legacy_evidence["profile"],
                "coverage_milestone": "M3c",
                "selected_resource_count": 185,
                "custom_material_count": 186,
                "connected_glass_block_id_count": 2,
                "connected_glass_resource_count": 19,
            },
            "expected_custom_summary": value["expected_custom_summary"],
            "expected_stock_fallback_summary": value["expected_stock_fallback_summary"],
            "m3c_summary": exact_m3c_summary,
        }
    )
    return contract, evidence


def _extended_slot_material(model_id: str) -> str:
    return EXTENDED_DRIVE_CELL_MATERIALS.get(model_id, DRIVE_CELL_MATERIAL)


def _parse_schema5_cases(
    value: dict[str, Any], digest: str
) -> tuple[GalleryContract, dict[str, Any]]:
    if value.get("signature_schema_version") != 5:
        raise EvidenceError("schema-5 gallery must use signature schema 5")
    profile = value.get("profile")
    if not isinstance(profile, dict):
        raise EvidenceError("schema-5 gallery has no profile")
    extension_resources = list(EXTENDED_DRIVE_SELECTED_RESOURCES)
    selected_resources = profile.get("selected_resources")
    if (
        profile.get("coverage_milestone") != "M3b"
        or not isinstance(selected_resources, list)
        or len(selected_resources) != 166
        or len(set(selected_resources)) != 166
        or selected_resources[-8:] != extension_resources
    ):
        raise EvidenceError("schema-5 gallery does not select the exact M3b resources")
    supported = profile.get("supported_extended_drive")
    exact_supported = {
        "block_id": EXTENDED_DRIVE_BLOCK_ID,
        "slot_count": 20,
        "front_slot_count": 10,
        "back_slot_count": 10,
        "back_orientation_policy": "opposite-facing-same-spin",
        "base_model": "extendedae:block/extended_drive/extended_me_drive_base",
        "empty_cell_model": "ae2:block/drive/drive_cell_empty",
        "accepted_cell_models": EXTENDED_DRIVE_CELL_MODELS,
        "accepted_ae2_cell_id_count": 23,
        "accepted_extension_cell_id_count": 3,
        "supported_cell_id_count": 26,
        "occupied_model_count": 15,
        "base_triangle_count": 116,
        "occupied_slot_triangle_count": 16,
        "triangle_formula": "116+16N",
        "led_policy": "static-offline-unknown",
        "unknown_cell_policy": "atomic-whole-block-original-resource-fallback",
    }
    extension_profiles = profile.get("extension_profiles")
    expected_artifact = {
        "artifact": "ExtendedAE-1.21-2.2.33-neoforge.jar",
        "version": "1.21-2.2.33-neoforge",
        "size_bytes": 5_573_972,
        "sha1": "e87867bffee36a28f9f4493f7bb7e7a5109a480f",
        "sha256": "6652ed1ea4b71f585d48c05a195a77594a7a2bd1ecea0fc805db2122aafad734",
    }
    if (
        supported != exact_supported
        or extension_profiles
        != [
            {
                "mod_id": "extendedae",
                "version": "1.21-2.2.33-neoforge",
                "artifact": expected_artifact,
                "selected_resources": extension_resources,
                "required_resources_sha256": EXTENDEDAE_REQUIRED_RESOURCE_DIGESTS,
            }
        ]
    ):
        raise EvidenceError("schema-5 ExtendedAE profile/artifact contract changed")
    if (
        value.get("case_count") != 92
        or value.get("anchor_count") != 359
        or value.get("expected_custom_summary") != {
        "anchor_count": 342,
        "selected_resource_count": 167,
        "triangle_count": 17_488,
        }
        or value.get("expected_stock_fallback_summary")
        != {"anchor_count": 16, "triangle_count": 0}
    ):
        raise EvidenceError("schema-5 gallery summary changed")
    exact_m3b_summary = {
        "accepted_ae2_cell_id_count": 23,
        "accepted_cell_id_count": 26,
        "accepted_extension_cell_id_count": 3,
        "anchor_count": 36,
        "base_triangle_formula": "116+16N",
        "case_count": 16,
        "cell_chassis_triangle_count": 504,
        "custom_anchor_count": 32,
        "custom_triangle_count": 5_056,
        "new_selected_resource_count": 8,
        "occupied_model_count": 15,
        "occupied_slot_count": 84,
        "offline_led_triangle_count": 840,
        "selected_resource_count": 13,
        "stock_fallback_anchor_count": 4,
    }
    if value.get("m3b_summary") != exact_m3b_summary:
        raise EvidenceError("schema-5 M3b summary changed")
    if value.get("bounds", {}).get("m3b_fixture") != {
        "min": [240, 98, 260],
        "max": [279, 104, 267],
    } or value.get("m3b_floor_policy") != {
        "anchor_y": 100,
        "support_y": 98,
        "support_block_id": "minecraft:smooth_stone",
        "air_gap_y": 99,
        "air_gap_block_id": "minecraft:air",
        "reason": "prevent-neighbor-face-culling",
    }:
        raise EvidenceError("schema-5 M3b bounds/floor policy changed")

    exact_case_metadata = {
        **{f"ae2-m1-{number:02d}": ("M1", "ae2:cable_bus") for number in range(1, 49)},
        **{f"ae2-m2-{number:02d}": ("M2", "ae2:cable_bus") for number in range(1, 15)},
        **{f"ae2-m3-{number:02d}": ("M3a", "ae2:drive") for number in range(1, 15)},
        **{
            f"ae2-m3b-{number:02d}": ("M3b", EXTENDED_DRIVE_BLOCK_ID)
            for number in range(1, 17)
        },
    }
    all_case_values = value.get("cases")
    if not isinstance(all_case_values, list) or any(
        not isinstance(case, dict)
        or not isinstance(case.get("case_id"), str)
        or (case.get("milestone"), case.get("route"))
        != exact_case_metadata.get(case.get("case_id"))
        for case in all_case_values
    ):
        raise EvidenceError("schema-5 case milestone/route metadata changed")

    legacy, legacy_evidence = _parse_cases_value(_schema4_view(value), digest)
    legacy_positions = {
        anchor.position for case in legacy.cases for anchor in case.anchors
    }
    raw_m3b_cases = [
        case
        for case in all_case_values
        if case["milestone"] == "M3b"
        and case["route"] == EXTENDED_DRIVE_BLOCK_ID
    ]
    if [case["case_id"] for case in raw_m3b_cases] != [
        f"ae2-m3b-{number:02d}" for number in range(1, 17)
    ]:
        raise EvidenceError("schema-5 gallery must contain ordered M3b cases 01..16")

    cases: list[CaseContract] = list(legacy.cases)
    extension_positions: set[tuple[int, int, int]] = set()
    custom_count = 0
    custom_triangles = 0
    occupied_count = 0
    supported_ids: set[str] = set()
    occupied_models: set[str] = set()
    resource_union: set[str] = set()
    orientation_states: set[tuple[str, int]] = set()
    fallback_positions: list[tuple[int, int, int]] = []
    component_pair: list[tuple[int, int, int]] = []
    component_variants: set[bool] = set()
    mirror_pair: list[tuple[int, int, int]] = []
    fallback_catalog: dict[tuple[int, int, int], tuple[str, int, str]] = {}
    valid_component = {
        "ae2:storage_cell_inv": [
            {"#": 64, "#t": "ae2:i", "id": "minecraft:stone"}
        ]
    }
    for case_value in raw_m3b_cases:
        case_id = case_value["case_id"]
        label = case_value.get("label")
        category = case_value.get("category")
        anchors_value = case_value.get("anchors")
        if (
            not isinstance(label, str)
            or not label
            or not isinstance(category, str)
            or not category
            or not isinstance(anchors_value, list)
            or not anchors_value
        ):
            raise EvidenceError(f"M3b case {case_id} identity/anchors are invalid")
        anchors: list[AnchorContract] = []
        for anchor_value in anchors_value:
            if not isinstance(anchor_value, dict):
                raise EvidenceError(f"M3b case {case_id} has a non-object anchor")
            position_value = anchor_value.get("position")
            if not isinstance(position_value, dict):
                raise EvidenceError(f"M3b case {case_id} has no position")
            coordinates = tuple(position_value.get(axis) for axis in ("x", "y", "z"))
            if any(
                not isinstance(coordinate, int) or isinstance(coordinate, bool)
                for coordinate in coordinates
            ):
                raise EvidenceError(f"M3b case {case_id} position is not integral")
            position = (coordinates[0], coordinates[1], coordinates[2])
            if (
                position in legacy_positions
                or position in extension_positions
                or not (240 <= position[0] <= 279)
                or position[1] != 100
                or not (260 <= position[2] <= 267)
            ):
                raise EvidenceError(f"M3b anchor position is outside/disjoint: {position}")
            extension_positions.add(position)
            expected_path = anchor_value.get("expected_path")
            if anchor_value.get("block_id") != EXTENDED_DRIVE_BLOCK_ID:
                raise EvidenceError(f"M3b anchor {position} is not an Extended Drive")
            block_state = anchor_value.get("block_state")
            inventory = anchor_value.get("inventory")
            if (
                not isinstance(block_state, dict)
                or set(block_state) != {"facing", "spin"}
                or block_state.get("facing") not in DRIVE_ORIENTATION_ANGLES
                or block_state.get("spin") not in range(4)
                or isinstance(block_state.get("spin"), bool)
                or not isinstance(inventory, dict)
                or inventory.get("compound") != "inv"
                or not isinstance(inventory.get("slots"), list)
                or len(inventory["slots"]) != 20
            ):
                raise EvidenceError(f"M3b anchor {position} state/inventory is invalid")
            if (
                anchor_value.get("support_floor")
                != {
                    "position": {"x": position[0], "y": 98, "z": position[2]},
                    "block_id": "minecraft:smooth_stone",
                }
                or anchor_value.get("air_gap")
                != {
                    "position": {"x": position[0], "y": 99, "z": position[2]},
                    "block_id": "minecraft:air",
                    "purpose": "prevent-neighbor-face-culling",
                }
                or anchor_value.get("network_condition") != "disconnected-unpowered"
            ):
                raise EvidenceError(f"M3b anchor {position} support/air/network contract changed")
            if expected_path == "custom-m3b":
                material_value = anchor_value.get("expected_material_triangles")
                triangle_count = anchor_value.get("expected_triangle_count")
                models = anchor_value.get("expected_drive_models")
                led = anchor_value.get("expected_drive_led")
                if (
                    not isinstance(material_value, dict)
                    or not material_value
                    or any(
                        not isinstance(path, str)
                        or not path
                        or not isinstance(count, int)
                        or isinstance(count, bool)
                        or count <= 0
                        for path, count in material_value.items()
                    )
                    or not isinstance(triangle_count, int)
                    or isinstance(triangle_count, bool)
                    or not isinstance(models, dict)
                    or models.get("base_model_id")
                    != "extendedae:block/extended_drive/extended_me_drive_base"
                    or models.get("empty_cell_model_id")
                    != "ae2:block/drive/drive_cell_empty"
                    or models.get("front_slot_count") != 10
                    or models.get("back_slot_count") != 10
                    or models.get("back_orientation_policy")
                    != "opposite-facing-same-spin"
                    or not isinstance(models.get("slots"), list)
                    or len(models["slots"]) != 20
                ):
                    raise EvidenceError(f"custom M3b anchor {position} metadata is invalid")
                slots: list[DriveSlotContract] = []
                occupied_slots: list[int] = []
                exact_materials = Counter(EXTENDED_DRIVE_BASE_MATERIAL_TRIANGLES)
                for slot in range(20):
                    inventory_slot = inventory["slots"][slot]
                    model_slot = models["slots"][slot]
                    face_slot = slot % 10
                    expected_origin_16 = (
                        9 - (face_slot % 2) * 8,
                        13 - (face_slot // 2) * 3,
                        1,
                    )
                    expected_origin = tuple(value / 16 for value in expected_origin_16)
                    face = "front" if slot < 10 else "back"
                    facing = (
                        block_state["facing"]
                        if face == "front"
                        else {
                            "down": "up",
                            "up": "down",
                            "north": "south",
                            "south": "north",
                            "west": "east",
                            "east": "west",
                        }[block_state["facing"]]
                    )
                    if (
                        not isinstance(inventory_slot, dict)
                        or inventory_slot.get("slot") != slot
                        or inventory_slot.get("field") != f"item{slot}"
                        or not isinstance(model_slot, dict)
                        or model_slot.get("slot") != slot
                        or model_slot.get("face") != face
                        or model_slot.get("face_slot") != face_slot
                        or model_slot.get("orientation")
                        != {"facing": facing, "spin": block_state["spin"]}
                        or model_slot.get("slot_origin_sixteenths")
                        != list(expected_origin_16)
                        or model_slot.get("slot_origin")
                        != dict(zip(("x", "y", "z"), expected_origin, strict=True))
                    ):
                        raise EvidenceError(f"M3b anchor {position} slot {slot} metadata is invalid")
                    item_id: str | None = None
                    if inventory_slot.get("empty") is True:
                        if set(inventory_slot) != {"slot", "field", "empty"}:
                            raise EvidenceError(f"M3b anchor {position} empty slot {slot} is invalid")
                        expected_model = "ae2:block/drive/drive_cell_empty"
                        material = None
                    else:
                        stack = inventory_slot.get("item_stack")
                        if (
                            not isinstance(stack, dict)
                            or not isinstance(stack.get("id"), str)
                            or stack.get("count") != 1
                            or set(stack) - {"id", "count", "components"}
                            or (
                                "components" in stack
                                and stack["components"] != valid_component
                            )
                        ):
                            raise EvidenceError(f"M3b anchor {position} occupied slot {slot} is invalid")
                        item_id = stack["id"]
                        expected_model = EXTENDED_DRIVE_CELL_MODELS.get(item_id)
                        if expected_model is None:
                            raise EvidenceError(f"M3b anchor {position} uses unsupported cell {item_id}")
                        material = _extended_slot_material(expected_model)
                        occupied_slots.append(slot)
                        supported_ids.add(item_id)
                        occupied_models.add(expected_model)
                        exact_materials[material] += 6
                    if (
                        model_slot.get("model_id") != expected_model
                        or model_slot.get("material") != material
                    ):
                        raise EvidenceError(f"M3b anchor {position} slot {slot} model/material changed")
                    slots.append(
                        DriveSlotContract(
                            slot,
                            item_id,
                            expected_model,
                            expected_origin,
                            face,
                            face_slot,
                            facing,
                            block_state["spin"],
                            material,
                        )
                    )
                occupied = len(occupied_slots)
                exact_materials[DRIVE_LED_MATERIAL] += occupied * 10
                exact_led = {
                    "ambient_occlusion_f32": 1.0,
                    "blocklight_raw_i8": 15,
                    "material": DRIVE_LED_MATERIAL,
                    "policy": "static-offline-unknown",
                    "rgb_u8": [0, 0, 0],
                    "sunlight_raw_i8": 15,
                    "triangle_count": occupied * 10,
                    "triangle_count_per_occupied_slot": 10,
                }
                if (
                    led != exact_led
                    or triangle_count != EXTENDED_DRIVE_BASE_TRIANGLE_COUNT + 16 * occupied
                    or Counter(material_value) != exact_materials
                    or sum(material_value.values()) != triangle_count
                    or anchor_value.get("cable_id") is not None
                    or anchor_value.get("face_parts")
                    or anchor_value.get("facades")
                    or anchor_value.get("expected_terminal_layers")
                    or anchor_value.get("expected_smart_overlays") != {}
                ):
                    raise EvidenceError(f"custom M3b anchor {position} does not satisfy 116+16N")
                drive = DriveContract(
                    block_state["facing"],
                    block_state["spin"],
                    tuple(slots),
                    tuple(occupied_slots),
                    occupied * 10,
                    EXTENDED_DRIVE_BLOCK_ID,
                    EXTENDED_DRIVE_BASE_TRIANGLE_COUNT,
                    "116+16N",
                )
                expected_materials = tuple(sorted(material_value.items()))
                custom_count += 1
                custom_triangles += triangle_count
                occupied_count += occupied
                resource_union.update(material_value)
                if category == "extended-drive-orientation":
                    orientation_states.add((block_state["facing"], block_state["spin"]))
                    if occupied_slots != [0, 10]:
                        raise EvidenceError("M3b orientation case must occupy front/back slot zero")
                if category == "extended-drive-component-insensitivity":
                    component = inventory["slots"][0]["item_stack"].get("components")
                    component_variants.add(component is not None)
                    component_pair.append(position)
                if category == "extended-drive-front-back-mirror":
                    mirror_pair.append(position)
                anchor_contract = AnchorContract(
                    case_id,
                    label,
                    expected_path,
                    position,
                    triangle_count,
                    expected_materials,
                    (),
                    (),
                    (),
                    (),
                    drive,
                    None,
                )
            elif expected_path == "stock-fallback-m3b":
                reason = anchor_value.get("fallback_reason")
                slots = inventory["slots"]
                if any(
                    not isinstance(slot_value, dict)
                    or slot_value.get("slot") != slot
                    or slot_value.get("field") != f"item{slot}"
                    or (
                        slot > 0
                        and (
                            slot_value.get("empty") is not True
                            or set(slot_value) != {"slot", "field", "empty"}
                        )
                    )
                    for slot, slot_value in enumerate(slots)
                ):
                    raise EvidenceError(f"M3b fallback anchor {position} slot metadata is invalid")
                occupied_stacks = [
                    slot.get("item_stack")
                    for slot in slots
                    if isinstance(slot, dict) and slot.get("empty") is not True
                ]
                if (
                    anchor_value.get("expected_triangle_count") != 0
                    or reason not in {
                        "unsupported-drive-cell-id",
                        "invalid-drive-cell-count",
                        "non-cell-drive-item",
                    }
                    or len(occupied_stacks) != 1
                    or block_state != {"facing": "south", "spin": 0}
                    or slots[0].get("empty") is True
                    or slots[0].get("item_stack") != occupied_stacks[0]
                    or set(slots[0]) != {"slot", "field", "item_stack"}
                    or not isinstance(occupied_stacks[0], dict)
                    or set(occupied_stacks[0]) != {"id", "count"}
                    or not isinstance(occupied_stacks[0].get("id"), str)
                    or not isinstance(occupied_stacks[0].get("count"), int)
                ):
                    raise EvidenceError(f"M3b fallback anchor {position} is invalid")
                fallback_catalog[position] = (
                    occupied_stacks[0]["id"],
                    occupied_stacks[0]["count"],
                    reason,
                )
                fallback_positions.append(position)
                anchor_contract = AnchorContract(
                    case_id,
                    label,
                    expected_path,
                    position,
                    0,
                    (),
                    (),
                    (),
                    (),
                    (),
                    None,
                    reason,
                )
            else:
                raise EvidenceError(f"M3b anchor {position} expected path is invalid")
            anchors.append(anchor_contract)
        cases.append(
            CaseContract(
                case_id,
                case_value["milestone"],
                case_value["route"],
                label,
                category,
                tuple(anchors),
            )
        )

    exact_fallback_catalog = {
        (266, 100, 266): ("megacells:item_storage_cell_1m", 1, "unsupported-drive-cell-id"),
        (269, 100, 266): ("kubejs:lava_cell", 1, "unsupported-drive-cell-id"),
        (272, 100, 266): ("ae2:item_storage_cell_1k", 2, "invalid-drive-cell-count"),
        (275, 100, 266): ("minecraft:stone", 1, "non-cell-drive-item"),
    }
    if (
        len(raw_m3b_cases) != 16
        or len(extension_positions) != 36
        or custom_count != 32
        or custom_triangles != 5_056
        or occupied_count != 84
        or supported_ids != set(EXTENDED_DRIVE_CELL_MODELS)
        or occupied_models != set(EXTENDED_DRIVE_CELL_MODELS.values())
        or len(resource_union) != 13
        or resource_union - set(legacy.expected_selected_resources)
        != set(EXTENDED_DRIVE_SELECTED_RESOURCES)
        or orientation_states
        != {(facing, spin) for facing in DRIVE_ORIENTATION_ANGLES for spin in range(4)}
        or len(component_pair) != 2
        or component_variants != {False, True}
        or len(mirror_pair) != 2
        or fallback_catalog != exact_fallback_catalog
    ):
        raise EvidenceError("schema-5 M3b matrix differs from its exact contract")
    component_contracts = [
        anchor.drive
        for case in cases
        for anchor in case.anchors
        if anchor.position in component_pair
    ]
    if len(component_contracts) != 2 or component_contracts[0] != component_contracts[1]:
        raise EvidenceError("M3b component-insensitivity contracts differ")

    expected_selected = tuple(
        sorted(set(legacy.expected_selected_resources) | set(EXTENDED_DRIVE_SELECTED_RESOURCES))
    )
    contract = GalleryContract(
        cases=tuple(cases),
        expected_selected_resources=expected_selected,
        expected_custom_anchor_count=342,
        expected_custom_triangle_count=17_488,
        stock_fallback_positions=tuple(
            sorted(set(legacy.stock_fallback_positions) | set(fallback_positions))
        ),
        dense_positions=legacy.dense_positions,
        expected_dense_triangle_count=legacy.expected_dense_triangle_count,
        expected_dense_material_triangles=legacy.expected_dense_material_triangles,
        schema_version=5,
        signature_schema_version=5,
        m2_regression_positions=legacy.m2_regression_positions,
        drive_component_pair=legacy.drive_component_pair,
        m3a_regression_positions=legacy.m3a_regression_positions,
        extended_drive_component_pair=tuple(sorted(component_pair)),
        extended_drive_mirror_pair=tuple(sorted(mirror_pair)),
        extension_positions=tuple(sorted(extension_positions)),
    )
    evidence = dict(legacy_evidence)
    evidence.update(
        {
            "sha256": digest,
            "schema_version": 5,
            "signature_schema_version": 5,
            "case_count": 92,
            "anchor_count": 359,
            "profile": {
                **legacy_evidence["profile"],
                "coverage_milestone": "M3b",
                "selected_resource_count": 166,
                "custom_material_count": 167,
                "extension_profile_count": 1,
                "accepted_extended_drive_cell_id_count": 26,
            },
            "expected_custom_summary": value["expected_custom_summary"],
            "expected_stock_fallback_summary": value[
                "expected_stock_fallback_summary"
            ],
            "m3b_summary": exact_m3b_summary,
        }
    )
    return contract, evidence


def _read_exact_m45_oracle(
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
    payload = read_bounded(path, size_bytes, description)
    if len(payload) != size_bytes or sha256_bytes(payload) != sha256:
        raise EvidenceError(f"{description} identity changed")
    value = parse_json_bytes(payload, description)
    if (
        not isinstance(value, dict)
        or set(value)
        != {
            "anchors",
            "coverage_id",
            "route_ids",
            "schema_version",
            "signature_schema_version",
        }
        or value.get("schema_version") != 2
        or value.get("signature_schema_version") != 11
        or value.get("coverage_id") != coverage_id
        or value.get("route_ids") != list(route_ids)
        or not isinstance(value.get("anchors"), dict)
        or canonical_json(value, pretty=True).encode("utf-8") != payload
    ):
        raise EvidenceError(f"{description} header or canonical encoding changed")
    parsed: dict[tuple[int, int, int], dict[str, Any]] = {}
    for key, entry in value["anchors"].items():
        try:
            position = tuple(int(item) for item in key.split())
        except (AttributeError, ValueError) as exception:
            raise EvidenceError(f"{description} position is malformed") from exception
        if (
            len(position) != 3
            or key != " ".join(str(item) for item in position)
            or position in parsed
            or not isinstance(entry, dict)
        ):
            raise EvidenceError(f"{description} selector is noncanonical")
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
            raise EvidenceError(f"{description} entry changed at {position}")
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
        raise EvidenceError(f"{description} exact closure changed")
    return parsed


def _read_exact_m45_runtime_oracle(
    expected_positions_by_route: dict[
        str, set[tuple[int, int, int]]
    ],
) -> dict[tuple[int, int, int], dict[str, Any]]:
    if (
        tuple(expected_positions_by_route) != M45_ROUTES
        or {
            route: len(expected_positions_by_route[route])
            for route in M45_ROUTES
        }
        != M45_RUNTIME_ROUTE_ANCHOR_COUNTS
        or sum(map(len, expected_positions_by_route.values()))
        != len(
            {
                position
                for positions in expected_positions_by_route.values()
                for position in positions
            }
        )
    ):
        raise EvidenceError("M4/M5 runtime-oracle route selector closure changed")
    return _read_exact_m45_oracle(
        M45_RUNTIME_ORACLE_PATH,
        size_bytes=M45_RUNTIME_ORACLE_SIZE_BYTES,
        sha256=M45_RUNTIME_ORACLE_SHA256,
        coverage_id=M45_RUNTIME_COVERAGE,
        route_ids=M45_ROUTES,
        expected_positions={
            position
            for positions in expected_positions_by_route.values()
            for position in positions
        },
        expected_anchor_count=M45_RUNTIME_ORACLE_ANCHOR_COUNT,
        expected_triangle_count=M45_RUNTIME_ORACLE_TRIANGLE_COUNT,
        expected_identity_count=M45_RUNTIME_ORACLE_IDENTITY_COUNT,
        expected_material_row_count=M45_RUNTIME_ORACLE_MATERIAL_ROW_COUNT,
        description="M4/M5 compiled-runtime oracle",
    )


def _read_exact_m45_schema10_legacy_oracle() -> dict[
    tuple[int, int, int], dict[str, Any]
]:
    return _read_exact_m45_oracle(
        M45_SCHEMA10_LEGACY_ORACLE_PATH,
        size_bytes=M45_SCHEMA10_LEGACY_ORACLE_SIZE_BYTES,
        sha256=M45_SCHEMA10_LEGACY_ORACLE_SHA256,
        coverage_id=M45_SCHEMA10_LEGACY_COVERAGE,
        route_ids=("expandedae", "megacells"),
        expected_positions={
            position for _case_id, position, *_suffix in M45_LEGACY_UPGRADE_SPECS
        },
        expected_anchor_count=M45_SCHEMA10_LEGACY_ORACLE_ANCHOR_COUNT,
        expected_triangle_count=M45_SCHEMA10_LEGACY_ORACLE_TRIANGLE_COUNT,
        expected_identity_count=M45_SCHEMA10_LEGACY_ORACLE_IDENTITY_COUNT,
        expected_material_row_count=M45_SCHEMA10_LEGACY_ORACLE_MATERIAL_ROW_COUNT,
        description="M4/M5 schema-10 legacy-upgrade oracle",
    )


def _parse_m45_projection(
    value: Any,
    description: str,
) -> M45ProjectionContract:
    base_keys = {
        "expected_path",
        "review_projection",
        "reason",
        "allowed_resources",
    }
    material_key = {"expected_material_triangles"}
    runtime_keys = {
        "expected_geometry_signature",
        "expected_nonlighting_attribute_signature",
        "expected_triangle_count",
    }
    if (
        not isinstance(value, dict)
        or set(value)
        not in (
            base_keys,
            base_keys | material_key,
            base_keys | material_key | runtime_keys,
        )
        or value.get("review_projection") not in {"empty", "nonempty"}
        or not isinstance(value.get("expected_path"), str)
        or not value["expected_path"]
        or not isinstance(value.get("reason"), str)
        or not value["reason"]
        or not isinstance(value.get("allowed_resources"), list)
        or value["allowed_resources"] != sorted(set(value["allowed_resources"]))
        or any(
            not isinstance(resource, str) or not resource
            for resource in value["allowed_resources"]
        )
        or bool(value["allowed_resources"])
        != (value["review_projection"] == "nonempty")
    ):
        raise EvidenceError(f"{description} is malformed")
    expected_materials = value.get("expected_material_triangles", {})
    if (
        not isinstance(expected_materials, dict)
        or list(expected_materials) != sorted(expected_materials)
        or not expected_materials
        and "expected_material_triangles" in value
        or any(
            not isinstance(resource, str)
            or not resource
            or not isinstance(count, int)
            or isinstance(count, bool)
            or count <= 0
            for resource, count in expected_materials.items()
        )
        or set(expected_materials) - set(value["allowed_resources"])
        or bool(expected_materials)
        and value["review_projection"] != "nonempty"
    ):
        raise EvidenceError(f"{description} exact material signature is malformed")
    has_runtime_oracle = runtime_keys <= set(value)
    expected_triangle_count = value.get("expected_triangle_count")
    expected_geometry_signature = value.get("expected_geometry_signature")
    expected_nonlighting_attribute_signature = value.get(
        "expected_nonlighting_attribute_signature"
    )
    if has_runtime_oracle:
        if (
            not isinstance(expected_triangle_count, int)
            or isinstance(expected_triangle_count, bool)
            or expected_triangle_count <= 0
            or expected_triangle_count != sum(expected_materials.values())
            or value["review_projection"] != "nonempty"
            or any(
                not isinstance(signature, str)
                or len(signature) != 64
                or any(
                    character not in "0123456789abcdef"
                    for character in signature
                )
                for signature in (
                    expected_geometry_signature,
                    expected_nonlighting_attribute_signature,
                )
            )
        ):
            raise EvidenceError(
                f"{description} exact runtime oracle is malformed"
            )
    elif any(key in value for key in runtime_keys):
        raise EvidenceError(f"{description} runtime oracle is incomplete")
    return M45ProjectionContract(
        expected_path=value["expected_path"],
        review_projection=value["review_projection"],
        reason=value["reason"],
        allowed_resources=tuple(value["allowed_resources"]),
        expected_material_triangles=tuple(expected_materials.items()),
        expected_triangle_count=expected_triangle_count,
        expected_geometry_signature=expected_geometry_signature,
        expected_nonlighting_attribute_signature=(
            expected_nonlighting_attribute_signature
        ),
    )


def _m45_exact_empty_original_resource(
    route: str, raw_anchor: dict[str, Any]
) -> bool:
    block_id = raw_anchor.get("block_id")
    state = raw_anchor.get("block_state")
    if not isinstance(state, dict):
        state = {}
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
    return (
        route == "extendedae-matrix"
        and block_id == "extendedae:assembler_matrix_glass"
    )


def _m45_expected_exact_projection_materials(
    raw_anchor: dict[str, Any], projection: M45ProjectionContract
) -> tuple[tuple[str, int], ...]:
    if (
        projection.review_projection != "nonempty"
        or projection.expected_path
        not in {"stock-original-m45", "physical-stock-original-m45"}
    ):
        return ()
    block_id = raw_anchor.get("block_id")
    if block_id == "merequester:requester":
        return tuple(sorted(M45_REQUESTER_ORIGINAL_MATERIAL_TRIANGLES.items()))
    if block_id == "expandedae:exp_io_port":
        state = raw_anchor.get("block_state")
        powered = state.get("powered") if isinstance(state, dict) else None
        if powered not in M45_EXP_IO_ORIGINAL_MATERIAL_TRIANGLES:
            raise EvidenceError(
                "schema-11 M4/M5 I/O Port projection powered state changed"
            )
        return tuple(
            sorted(M45_EXP_IO_ORIGINAL_MATERIAL_TRIANGLES[powered].items())
        )
    return ()


def _parse_m45_legacy_upgrades(
    value: dict[str, Any],
    schema10: dict[str, Any],
    parsed_cases: list[CaseContract],
) -> tuple[list[CaseContract], dict[str, Any]]:
    overlay = value.get("m45_legacy_upgrades")
    oracle = _read_exact_m45_schema10_legacy_oracle()
    exact_oracle_metadata = {
        "path": M45_SCHEMA10_LEGACY_ORACLE_PATH.name,
        "size_bytes": M45_SCHEMA10_LEGACY_ORACLE_SIZE_BYTES,
        "sha256": M45_SCHEMA10_LEGACY_ORACLE_SHA256,
        "schema_version": 2,
        "signature_schema_version": 11,
        "anchor_count": M45_SCHEMA10_LEGACY_ORACLE_ANCHOR_COUNT,
        "triangle_count": M45_SCHEMA10_LEGACY_ORACLE_TRIANGLE_COUNT,
        "identity_count": M45_SCHEMA10_LEGACY_ORACLE_IDENTITY_COUNT,
        "material_row_count": M45_SCHEMA10_LEGACY_ORACLE_MATERIAL_ROW_COUNT,
    }
    exact_summary = {
        "anchor_count": len(M45_LEGACY_UPGRADE_SPECS),
        "custom_anchor_count": M45_SCHEMA10_LEGACY_ORACLE_ANCHOR_COUNT,
        "custom_triangle_count": M45_SCHEMA10_LEGACY_ORACLE_TRIANGLE_COUNT,
        "selected_resource_count": M45_SCHEMA10_LEGACY_ORACLE_IDENTITY_COUNT,
        "material_row_count": M45_SCHEMA10_LEGACY_ORACLE_MATERIAL_ROW_COUNT,
        "m45_route_dependency_anchor_counts": {
            route: sum(
                route in required_m45_routes
                for (
                    _case_id,
                    _position,
                    required_m45_routes,
                    _required_legacy_routes,
                    _expected_path,
                    _fallback_reason,
                    _source_kind,
                    _observation,
                ) in M45_LEGACY_UPGRADE_SPECS
            )
            for route in M45_ROUTES
            if any(
                route in required_m45_routes
                for (
                    _case_id,
                    _position,
                    required_m45_routes,
                    _required_legacy_routes,
                    _expected_path,
                    _fallback_reason,
                    _source_kind,
                    _observation,
                ) in M45_LEGACY_UPGRADE_SPECS
            )
        },
        "legacy_route_dependency_anchor_counts": {
            route: sum(
                route in required_legacy_routes
                for (
                    _case_id,
                    _position,
                    _required_m45_routes,
                    required_legacy_routes,
                    _expected_path,
                    _fallback_reason,
                    _source_kind,
                    _observation,
                ) in M45_LEGACY_UPGRADE_SPECS
            )
            for route in ("extension", "crafting")
        },
        "predecessor_projection": {
            "empty_anchor_count": len(M45_LEGACY_UPGRADE_SPECS),
            "triangle_count": 0,
            "resource_count": 0,
            "resources": [],
        },
        "physical_stock_projection": {
            "empty_anchor_count": len(M45_LEGACY_UPGRADE_SPECS),
            "triangle_count": 0,
            "resource_count": 0,
            "resources": [],
        },
    }
    if (
        not isinstance(overlay, dict)
        or set(overlay)
        != {
            "capture",
            "coverage_id",
            "oracle",
            "profile_id",
            "rows",
            "schema_version",
            "source_schema10",
            "summary",
        }
        or overlay.get("schema_version") != 1
        or overlay.get("coverage_id") != M45_LEGACY_UPGRADE_COVERAGE
        or overlay.get("profile_id") != "m45-cumulative-review"
        or overlay.get("source_schema10")
        != {
            "cases_size_bytes": 4_207_895,
            "cases_sha256": SCHEMA10_CANONICAL_SHA256,
            "signature_schema_version": 10,
        }
        or overlay.get("capture") != M45_LEGACY_UPGRADE_CAPTURE
        or overlay.get("oracle") != exact_oracle_metadata
        or overlay.get("summary") != exact_summary
        or not isinstance(overlay.get("rows"), list)
        or len(overlay["rows"]) != len(M45_LEGACY_UPGRADE_SPECS)
    ):
        raise EvidenceError("schema-11 M4/M5 legacy-upgrade header changed")

    raw_cases = {
        case.get("case_id"): case
        for case in schema10.get("cases", [])
        if isinstance(case, dict)
    }
    parsed_by_selector = {
        (anchor.case_id, anchor.position): anchor
        for case in parsed_cases
        for anchor in case.anchors
    }
    replacements: dict[tuple[str, tuple[int, int, int]], AnchorContract] = {}
    for row, spec in zip(
        overlay["rows"], M45_LEGACY_UPGRADE_SPECS, strict=True
    ):
        (
            case_id,
            position,
            required_m45_routes,
            required_legacy_routes,
            expected_path,
            fallback_reason,
            source_kind,
            observation,
        ) = spec
        position_value = dict(zip(("x", "y", "z"), position))
        if (
            not isinstance(row, dict)
            or set(row)
            != {
                "case_id",
                "enabled",
                "live_observation",
                "physical_stock_projection",
                "position",
                "predecessor_projection",
                "required_legacy_routes",
                "required_m45_routes",
                "source_kind",
            }
            or row.get("case_id") != case_id
            or row.get("position") != position_value
            or row.get("source_kind") != source_kind
            or row.get("required_m45_routes") != list(required_m45_routes)
            or row.get("required_legacy_routes") != list(required_legacy_routes)
            or row.get("live_observation") != observation
        ):
            raise EvidenceError("schema-11 M4/M5 legacy-upgrade selector changed")
        raw_case = raw_cases.get(case_id)
        raw_anchors = raw_case.get("anchors") if isinstance(raw_case, dict) else None
        raw_matches = [
            anchor
            for anchor in raw_anchors or ()
            if anchor.get("position") == position_value
        ]
        parsed_anchor = parsed_by_selector.get((case_id, position))
        if (
            not isinstance(raw_anchors, list)
            or len(raw_matches) != 1
            or parsed_anchor is None
            or parsed_anchor.expected_path != expected_path
            or parsed_anchor.expected_triangle_count != 0
            or parsed_anchor.fallback_reason != fallback_reason
            or parsed_anchor.m45 is not None
            or parsed_anchor.m45_legacy_upgrade is not None
        ):
            raise EvidenceError(
                f"schema-11 M4/M5 legacy predecessor changed: {case_id}"
            )
        raw_anchor = raw_matches[0]
        if source_kind in {
            "ae2-drive-megacells-cell",
            "extended-drive-megacells-cell",
        }:
            expected_block_id = (
                "ae2:drive"
                if source_kind == "ae2-drive-megacells-cell"
                else EXTENDED_DRIVE_BLOCK_ID
            )
            slots = raw_anchor.get("inventory", {}).get("slots", [])
            if (
                raw_anchor.get("block_id") != expected_block_id
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
                raise EvidenceError(
                    f"schema-11 M4/M5 legacy Drive NBT changed: {case_id}"
                )
        elif source_kind == "native-crafting-expanded-mega-peer-connection":
            if (
                raw_anchor.get("block_id") != "ae2:1k_crafting_storage"
                or raw_anchor.get("compatible_neighbor_block_ids")
                != [
                    "megacells:mega_crafting_unit",
                    "expandedae:exp_crafting_unit",
                ]
                or raw_anchor.get("block_state")
                != {"formed": True, "powered": False}
            ):
                raise EvidenceError(
                    "schema-11 legacy crafting peer source changed"
                )
        else:
            raise EvidenceError("unknown schema-11 M4/M5 legacy source kind")

        enabled = _parse_m45_projection(
            row.get("enabled"), f"M4/M5 legacy enabled projection at {position}"
        )
        predecessor = _parse_m45_projection(
            row.get("predecessor_projection"),
            f"M4/M5 legacy predecessor projection at {position}",
        )
        physical_stock = _parse_m45_projection(
            row.get("physical_stock_projection"),
            f"M4/M5 legacy physical-stock projection at {position}",
        )
        if (
            enabled.expected_path != "custom-m45-legacy-upgrade"
            or enabled.review_projection != "nonempty"
            or enabled.reason != "exact-enabled-live-map-route-upgrade"
            or enabled.allowed_resources
            != tuple(sorted(observation["material_triangles"]))
            or enabled.expected_triangle_count
            != oracle[position]["triangle_count"]
            or dict(enabled.expected_material_triangles)
            != oracle[position]["material_triangles"]
            or enabled.expected_geometry_signature
            != oracle[position]["geometry_signature"]
            or enabled.expected_nonlighting_attribute_signature
            != oracle[position]["nonlighting_attribute_signature"]
            or predecessor
            != M45ProjectionContract(
                expected_path,
                "empty",
                fallback_reason,
                (),
                (),
            )
            or physical_stock != predecessor
            or observation["triangle_count"]
            != sum(observation["material_triangles"].values())
            or observation["triangle_count"]
            != oracle[position]["triangle_count"]
            or observation["material_triangles"]
            != oracle[position]["material_triangles"]
        ):
            raise EvidenceError(
                f"schema-11 M4/M5 legacy projection changed: {case_id}"
            )
        selector = (case_id, position)
        if selector in replacements:
            raise EvidenceError("schema-11 M4/M5 legacy selector duplicated")
        replacements[selector] = replace(
            parsed_anchor,
            m45_legacy_upgrade=M45LegacyUpgradeContract(
                required_m45_routes=required_m45_routes,
                required_legacy_routes=required_legacy_routes,
                enabled_projection=enabled,
                predecessor_projection=predecessor,
                observed_triangle_count=observation["triangle_count"],
                observed_material_triangles=tuple(
                    sorted(observation["material_triangles"].items())
                ),
            ),
        )

    expected_selectors = {
        (case_id, position)
        for case_id, position, *_suffix in M45_LEGACY_UPGRADE_SPECS
    }
    if set(replacements) != expected_selectors:
        raise EvidenceError("schema-11 M4/M5 legacy selector closure changed")
    replaced_cases = [
        replace(
            case,
            anchors=tuple(
                replacements.get((anchor.case_id, anchor.position), anchor)
                for anchor in case.anchors
            ),
        )
        for case in parsed_cases
    ]
    return replaced_cases, {
        "coverage_id": M45_LEGACY_UPGRADE_COVERAGE,
        "capture": overlay["capture"],
        "oracle": exact_oracle_metadata,
        "source_schema10": overlay["source_schema10"],
        "summary": exact_summary,
        "rows": overlay["rows"],
    }


def _parse_schema11_cases(
    value: dict[str, Any], digest: str
) -> tuple[GalleryContract, dict[str, Any]]:
    if value.get("signature_schema_version") != 11:
        raise EvidenceError("schema-11 gallery must use signature schema 11")
    schema10 = _schema10_view(value)
    schema10_payload = canonical_json(schema10, pretty=True).encode("utf-8")
    schema10_sha256 = sha256_bytes(schema10_payload)
    if schema10_sha256 != SCHEMA10_CANONICAL_SHA256:
        raise EvidenceError(
            "schema-11 does not embed the byte-frozen accepted schema-10 view"
        )
    legacy, legacy_evidence = _parse_schema10_cases(schema10, schema10_sha256)

    all_cases = value.get("cases")
    profile = value.get("profile")
    route_profiles = profile.get("m45_routes") if isinstance(profile, dict) else None
    summary = value.get("m45_review_summary")
    if (
        not isinstance(all_cases, list)
        or not isinstance(profile, dict)
        or not isinstance(route_profiles, list)
        or not isinstance(summary, dict)
        or value.get("case_count") != 158
        or value.get("anchor_count") != 1_366
    ):
        raise EvidenceError("schema-11 cumulative M4/M5 header changed")
    raw_cases = [
        case
        for case in all_cases
        if str(case.get("case_id", "")).startswith("ae2-m45-")
    ]
    if (
        len(raw_cases) != 8
        or [case.get("case_id") for case in raw_cases]
        != [f"ae2-m45-{index:02d}" for index in range(1, 9)]
        or tuple(case.get("route") for case in raw_cases) != M45_ROUTES
        or [len(case.get("anchors", ())) for case in raw_cases]
        != [11, 36, 118, 107, 44, 9, 42, 42]
    ):
        raise EvidenceError("schema-11 exact eight-route review matrix changed")
    expected_oracle_positions_by_route = {
        raw_case["route"]: {
            _s1_xyz(
                raw_anchor.get("position"),
                "M4/M5 runtime-oracle selector",
            )
            for raw_anchor in raw_case["anchors"]
            if raw_anchor.get("expected_path") == "custom-m45"
        }
        for raw_case in raw_cases
    }
    m45_runtime_oracle = _read_exact_m45_runtime_oracle(
        expected_oracle_positions_by_route
    )
    expected_summary = {
        "case_count": 8,
        "anchor_count": 409,
        "custom_review_anchor_count": 391,
        "atomic_fallback_anchor_count": 18,
        "source_derived_synthetic_anchor_count": 0,
        "route_count": 8,
        "route_ids": list(M45_ROUTES),
        "base_schema10_case_count": 150,
        "base_schema10_anchor_count": 957,
        "base_schema10_selected_resource_count": 289,
        "new_selected_resource_count": 144,
        "runtime_oracle_size_bytes": M45_RUNTIME_ORACLE_SIZE_BYTES,
        "runtime_oracle_sha256": M45_RUNTIME_ORACLE_SHA256,
        "runtime_oracle_anchor_count": M45_RUNTIME_ORACLE_ANCHOR_COUNT,
        "runtime_oracle_triangle_count": M45_RUNTIME_ORACLE_TRIANGLE_COUNT,
        "runtime_oracle_identity_count": M45_RUNTIME_ORACLE_IDENTITY_COUNT,
        "runtime_oracle_material_row_count": (
            M45_RUNTIME_ORACLE_MATERIAL_ROW_COUNT
        ),
        "legacy_upgrade_anchor_count": 3,
        "legacy_upgrade_runtime_triangle_count": (
            M45_SCHEMA10_LEGACY_ORACLE_TRIANGLE_COUNT
        ),
        "review_oracle_policy": (
            "exact-runtime-map-geometry-material-nonlighting-v11"
        ),
        "route_disabled_projection": "per-anchor-declared-review-projection",
        "disabled_projection_evidence": M45_DISABLED_PROJECTION_EVIDENCE,
        "physical_stock_projection": {
            "nonempty_anchor_count": 110,
            "empty_anchor_count": 299,
        },
        "native_structural_disabled_projection": {
            "affected_anchor_count": 189,
            "blocked_route": "extendedae-planes",
            "other_face_lanes": "unreachable",
            "independent_whole_block_lanes": "enabled",
        },
        "crafting_disabled_projection": {
            "affected_anchor_count": 20,
            "policy": "direct-native-owner-observers-atomically-stock-fallback",
        },
    }
    if summary != expected_summary:
        raise EvidenceError("schema-11 M4/M5 review summary changed")
    unit_only = value.get("m45_unit_only_mutations")
    if unit_only != {
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
    }:
        raise EvidenceError("schema-11 M4/M5 unit-only mutation boundary changed")
    if value.get("m45_floor_policy") != {
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
    }:
        raise EvidenceError("schema-11 M4/M5 floor/persistence policy changed")
    bounds = value.get("bounds")
    if not isinstance(bounds, dict) or bounds.get("m45_fixture") != {
        "min": [336, 96, 312],
        "max": [511, 110, 431],
    }:
        raise EvidenceError("schema-11 M4/M5 bounded fixture changed")

    route_profile_by_id: dict[str, dict[str, Any]] = {}
    schema10_selected_resources = set(
        schema10["profile"]["selected_resources"]
    )
    for route_profile in route_profiles:
        if not isinstance(route_profile, dict):
            raise EvidenceError("schema-11 route profile is not an object")
        route = route_profile.get("route")
        route_resources = route_profile.get("route_resources")
        source_resources = route_profile.get("source_resources")
        dependency_resources = route_profile.get("dependency_resources")
        host_resources = route_profile.get("host_resources")
        allowlist = route_profile.get("material_allowlist")
        stock_allowlist = route_profile.get("stock_material_allowlist")
        if (
            route not in M45_ROUTES
            or route in route_profile_by_id
            or not isinstance(route_resources, list)
            or not route_resources
            or route_resources != sorted(set(route_resources))
            or not isinstance(source_resources, list)
            or not source_resources
            or source_resources != sorted(set(source_resources))
            or not isinstance(dependency_resources, list)
            or dependency_resources != sorted(set(dependency_resources))
            or set(dependency_resources) & set(route_resources)
            or (route != "extendedae-planes" and bool(dependency_resources))
            or not isinstance(host_resources, list)
            or host_resources != sorted(set(host_resources))
            or (route != "extendedae-planes" and bool(host_resources))
            or set(host_resources)
            & (set(route_resources) | set(dependency_resources))
            or not set(host_resources) <= schema10_selected_resources
            or not isinstance(allowlist, list)
            or allowlist != sorted(set(allowlist))
            or not set(route_resources) <= set(allowlist)
            or not set(dependency_resources) <= set(allowlist)
            or not set(host_resources) <= set(allowlist)
            or not isinstance(stock_allowlist, list)
            or stock_allowlist != sorted(set(stock_allowlist))
            or not set(allowlist) <= set(stock_allowlist)
            or not set(source_resources) <= set(stock_allowlist)
            or "bluemap:block/missing"
            in {
                *route_resources,
                *source_resources,
                *dependency_resources,
                *host_resources,
                *allowlist,
                *stock_allowlist,
            }
            or route_profile.get("enabled_live_observation")
            != (
                M45_EXTENDED_PLANE_LIVE_OBSERVATION
                if route == "extendedae-planes"
                else None
            )
            or route_profile.get("case_count") != 1
            or route_profile.get("route_disabled_projection")
            != "per-anchor-declared-review-projection"
            or not isinstance(
                route_profile.get("route_disabled_affected_anchor_count"), int
            )
            or not isinstance(
                route_profile.get("route_disabled_nonempty_anchor_count"), int
            )
            or not isinstance(
                route_profile.get("route_disabled_empty_anchor_count"), int
            )
            or route_profile["route_disabled_affected_anchor_count"]
            != route_profile["route_disabled_nonempty_anchor_count"]
            + route_profile["route_disabled_empty_anchor_count"]
            or route_profile.get("legacy_upgrade_dependency_anchor_count")
            != sum(
                route in required_m45_routes
                for (
                    _case_id,
                    _position,
                    required_m45_routes,
                    _required_legacy_routes,
                    _expected_path,
                    _fallback_reason,
                    _source_kind,
                    _observation,
                ) in M45_LEGACY_UPGRADE_SPECS
            )
            or route_profile.get("native_structural_dependency") not in {
                "route-blocked-and-face-lane-unreachable",
                "face-lanes-unreachable-independent-whole-block-lanes-remain-active",
                "independent-route-remains-active",
            }
            or route_profile.get("failure_policy")
            != "disable-only-this-route-and-preserve-other-routes"
            or not isinstance(route_profile.get("artifact"), dict)
        ):
            raise EvidenceError("schema-11 route profile contract changed")
        route_profile_by_id[route] = route_profile
    if tuple(route_profile_by_id) != M45_ROUTES:
        raise EvidenceError("schema-11 route profile ordering changed")
    matrix_profile = route_profile_by_id["extendedae-matrix"]
    plane_profile = route_profile_by_id["extendedae-planes"]
    if (
        tuple(matrix_profile["route_resources"])
        != M45_EXTENDED_MATRIX_RESOURCES
        or matrix_profile["dependency_resources"] != []
        or matrix_profile["host_resources"] != []
        or set(matrix_profile["material_allowlist"])
        != set(M45_EXTENDED_MATRIX_RESOURCES)
        or tuple(plane_profile["route_resources"])
        != M45_EXTENDED_PLANE_RESOURCES
        or tuple(plane_profile["dependency_resources"])
        != M45_EXTENDED_PLANE_DEPENDENCY_RESOURCES
        or tuple(plane_profile["host_resources"])
        != M45_EXTENDED_PLANE_HOST_RESOURCES
        or set(plane_profile["material_allowlist"])
        != set(M45_EXTENDED_PLANE_RESOURCES)
        | set(M45_EXTENDED_PLANE_DEPENDENCY_RESOURCES)
        | set(M45_EXTENDED_PLANE_HOST_RESOURCES)
        or set(matrix_profile["route_resources"])
        & set(plane_profile["route_resources"])
    ):
        raise EvidenceError("schema-11 ExtendedAE route resource partition changed")
    athena_profile = route_profile_by_id["advanced-ae-athena"]
    expected_athena_sources = {
        "advanced_ae:block/quantum_alloy_block",
        "advanced_ae:block/quantum_alloy_block_center",
        "advanced_ae:block/quantum_alloy_block_empty",
        "advanced_ae:block/quantum_alloy_block_h",
        "advanced_ae:block/quantum_alloy_block_v",
    }
    expected_athena_frames = {
        "bluemap_ae2:m45/athena-frame-zero/" + resource.split(":", 1)[1]
        for resource in expected_athena_sources
    }
    if (
        set(athena_profile["source_resources"]) != expected_athena_sources
        or set(athena_profile["route_resources"]) != expected_athena_frames
        or expected_athena_sources & set(athena_profile["material_allowlist"])
    ):
        raise EvidenceError("schema-11 Athena frame-zero resource boundary changed")

    selected_resources = profile.get("selected_resources")
    base_selected_order = schema10["profile"]["selected_resources"]
    new_resources = sorted(
        {
            resource
            for route_profile in route_profiles
            for resource in route_profile["route_resources"]
        }
        - set(base_selected_order)
    )
    if (
        profile.get("coverage_milestone") != "M5-cumulative-review"
        or selected_resources != [*base_selected_order, *new_resources]
        or len(new_resources) != 144
        or "bluemap:block/missing" in selected_resources
    ):
        raise EvidenceError("schema-11 selected M4/M5 resource closure changed")

    parsed_cases, m45_legacy_upgrade_evidence = _parse_m45_legacy_upgrades(
        value, schema10, list(legacy.cases)
    )
    m45_legacy_upgrade_positions = {
        position for _case_id, position, *_suffix in M45_LEGACY_UPGRADE_SPECS
    }
    for case in parsed_cases:
        for anchor in case.anchors:
            upgrade = anchor.m45_legacy_upgrade
            if upgrade is None:
                continue
            for required_route in upgrade.required_m45_routes:
                if not set(upgrade.enabled_projection.allowed_resources) <= set(
                    route_profile_by_id[required_route]["material_allowlist"]
                ):
                    raise EvidenceError(
                        "schema-11 legacy upgrade exceeds its route material allowlist"
                    )
    m45_positions: set[tuple[int, int, int]] = set()
    route_positions: dict[str, list[tuple[int, int, int]]] = {
        route: [] for route in M45_ROUTES
    }
    fallback_count = 0
    synthetic_count = 0
    fixture_positions: set[tuple[int, int, int]] = set()
    selector_exception_positions: set[tuple[int, int, int]] = set()
    exact_empty_original_by_route: Counter[str] = Counter()
    physical_stock_nonempty_count = 0
    combined_disabled_nonempty_count = 0
    crafting_disabled_projection_count = 0
    crafting_disabled_nonempty_count = 0
    exact_inherited_selector_counts: Counter[str] = Counter()
    for raw_case in raw_cases:
        route = raw_case["route"]
        route_profile = route_profile_by_id[route]
        milestone = raw_case.get("milestone")
        if (
            milestone not in {"M4", "M5"}
            or milestone != route_profile.get("milestone")
            or not isinstance(raw_case.get("label"), str)
            or not isinstance(raw_case.get("category"), str)
            or not isinstance(raw_case.get("anchors"), list)
            or not isinstance(raw_case.get("fixture_blocks"), list)
        ):
            raise EvidenceError("schema-11 M4/M5 case metadata changed")
        anchors: list[AnchorContract] = []
        for raw_anchor in raw_case["anchors"]:
            if not isinstance(raw_anchor, dict):
                raise EvidenceError("schema-11 M4/M5 anchor is not an object")
            position = _s1_xyz(raw_anchor.get("position"), "M4/M5 anchor position")
            expected_path = raw_anchor.get("expected_path")
            projection = raw_anchor.get("review_projection")
            synthetic = raw_anchor.get("source_derived_synthetic_fixture")
            if (
                position in m45_positions
                or position in fixture_positions
                or position in {anchor.position for case in legacy.cases for anchor in case.anchors}
                or raw_anchor.get("m45_route") != route
                or expected_path not in {"custom-m45", "stock-fallback-m45"}
                or projection
                != ("nonempty" if expected_path == "custom-m45" else "empty")
                or not isinstance(synthetic, bool)
                or not isinstance(raw_anchor.get("block_id"), str)
            ):
                raise EvidenceError("schema-11 M4/M5 anchor contract changed")
            oracle_entry = m45_runtime_oracle.get(position)
            oracle_fields = {
                "expected_geometry_signature",
                "expected_material_triangles",
                "expected_nonlighting_attribute_signature",
                "expected_triangle_count",
            }
            if expected_path == "custom-m45":
                if (
                    oracle_entry is None
                    or raw_anchor.get("expected_triangle_count")
                    != oracle_entry["triangle_count"]
                    or raw_anchor.get("expected_material_triangles")
                    != oracle_entry["material_triangles"]
                    or raw_anchor.get("expected_geometry_signature")
                    != oracle_entry["geometry_signature"]
                    or raw_anchor.get(
                        "expected_nonlighting_attribute_signature"
                    )
                    != oracle_entry["nonlighting_attribute_signature"]
                ):
                    raise EvidenceError(
                        f"schema-11 M4/M5 exact runtime oracle changed at {position}"
                    )
                expected_triangle_count = oracle_entry["triangle_count"]
                expected_material_triangles = tuple(
                    oracle_entry["material_triangles"].items()
                )
                expected_geometry_signature = oracle_entry[
                    "geometry_signature"
                ]
                expected_nonlighting_attribute_signature = oracle_entry[
                    "nonlighting_attribute_signature"
                ]
            else:
                if oracle_entry is not None or oracle_fields & set(raw_anchor):
                    raise EvidenceError(
                        "schema-11 M4/M5 fallback escaped exact-empty oracle exclusion"
                    )
                expected_triangle_count = None
                expected_material_triangles = ()
                expected_geometry_signature = None
                expected_nonlighting_attribute_signature = None
            raw_route_projections = raw_anchor.get("route_disabled_projections")
            if (
                not isinstance(raw_route_projections, dict)
                or route not in raw_route_projections
                or any(key not in M45_ROUTES for key in raw_route_projections)
            ):
                raise EvidenceError(
                    "schema-11 M4/M5 route-disabled projection map changed"
                )
            route_disabled_projections = tuple(
                (
                    key,
                    _parse_m45_projection(
                        raw_route_projections[key],
                        f"M4/M5 {key} disabled projection at {position}",
                    ),
                )
                for key in M45_ROUTES
                if key in raw_route_projections
            )
            physical_stock_projection = _parse_m45_projection(
                raw_anchor.get("physical_stock_projection"),
                f"M4/M5 physical stock projection at {position}",
            )
            native_structural_projection = _parse_m45_projection(
                raw_anchor.get("native_structural_disabled_projection"),
                f"M4/M5 native-structural-disabled projection at {position}",
            )
            crafting_projection = (
                _parse_m45_projection(
                    raw_anchor["crafting_disabled_projection"],
                    f"M4/M5 crafting-disabled projection at {position}",
                )
                if "crafting_disabled_projection" in raw_anchor
                else None
            )
            all_mode_projections = [
                *(projection for _route, projection in route_disabled_projections),
                physical_stock_projection,
                native_structural_projection,
                *((crafting_projection,) if crafting_projection is not None else ()),
            ]
            for mode_projection in all_mode_projections:
                expected_exact_materials = (
                    _m45_expected_exact_projection_materials(
                        raw_anchor, mode_projection
                    )
                )
                if (
                    mode_projection.expected_material_triangles
                    != expected_exact_materials
                    or expected_exact_materials
                    and set(mode_projection.allowed_resources)
                    != {
                        resource
                        for resource, _count in expected_exact_materials
                    }
                ):
                    raise EvidenceError(
                        "schema-11 M4/M5 inherited-model exact material "
                        "projection changed"
                    )
            own_projection = dict(route_disabled_projections)[route]
            physical_stock_nonempty_count += (
                physical_stock_projection.review_projection == "nonempty"
            )
            combined_disabled_nonempty_count += (
                own_projection.review_projection == "nonempty"
            )
            if crafting_projection is not None:
                crafting_disabled_projection_count += 1
                crafting_disabled_nonempty_count += (
                    crafting_projection.review_projection == "nonempty"
                )
            if _m45_exact_empty_original_resource(route, raw_anchor):
                exact_empty_original_by_route[route] += 1
                if (
                    physical_stock_projection.review_projection != "empty"
                    or own_projection.review_projection != "empty"
                ):
                    raise EvidenceError(
                        "schema-11 exact empty original-resource projection changed"
                    )
            if raw_anchor.get("block_id") == "merequester:requester":
                exact_inherited_selector_counts["merequester:requester"] += 1
            elif raw_anchor.get("block_id") == "expandedae:exp_io_port":
                state = raw_anchor.get("block_state")
                powered = state.get("powered") if isinstance(state, dict) else None
                exact_inherited_selector_counts[
                    f"expandedae:exp_io_port[powered={powered}]"
                ] += 1
            if route == "extendedae-planes":
                if (
                    own_projection.expected_path
                    != "native-center-only-m45"
                    or own_projection.review_projection != "nonempty"
                    or own_projection.allowed_resources
                    != M45_NATIVE_CENTER_PROJECTION_RESOURCES
                    or physical_stock_projection.review_projection != "empty"
                    or physical_stock_projection.allowed_resources
                    or native_structural_projection.review_projection
                    != "empty"
                    or native_structural_projection.allowed_resources
                ):
                    raise EvidenceError(
                        "schema-11 Extended plane mode projections changed"
                    )
            selector_exception = raw_anchor.get(
                "selector_scoped_model_exception"
            )
            if position == M45_ADVANCED_SINGLETON_POSITION:
                if (
                    route != "advanced-ae-quantum"
                    or raw_anchor.get("block_id") != "advanced_ae:quantum_core"
                    or expected_path != "custom-m45"
                    or selector_exception
                    != M45_ADVANCED_SINGLETON_MODEL_EXCEPTION
                ):
                    raise EvidenceError(
                        "schema-11 Advanced singleton model exception changed"
                    )
                selector_exception_positions.add(position)
                selector_material_triangles = tuple(
                    sorted(
                        selector_exception[
                            "expected_material_triangles"
                        ].items()
                    )
                )
            elif selector_exception is not None:
                raise EvidenceError(
                    "schema-11 M4/M5 model exception escaped its exact selector"
                )
            else:
                selector_material_triangles = ()
            if expected_path == "custom-m45" and (
                set(dict(expected_material_triangles))
                - set(route_profile["material_allowlist"])
                - set(dict(selector_material_triangles))
            ):
                raise EvidenceError(
                    "schema-11 M4/M5 runtime oracle exceeds its route material closure"
                )
            if expected_path == "stock-fallback-m45":
                fallback_count += 1
                if not isinstance(raw_anchor.get("fallback_reason"), str):
                    raise EvidenceError("M4/M5 atomic fallback reason is missing")
            synthetic_count += synthetic
            m45_positions.add(position)
            route_positions[route].append(position)
            m45 = M45ReviewContract(
                route=route,
                review_projection=projection,
                allowed_resources=tuple(route_profile["material_allowlist"]),
                stock_allowed_resources=tuple(
                    route_profile["stock_material_allowlist"]
                ),
                route_resources=tuple(route_profile["route_resources"]),
                source_resources=tuple(route_profile["source_resources"]),
                host_resources=tuple(route_profile["host_resources"]),
                route_disabled_projections=route_disabled_projections,
                physical_stock_projection=physical_stock_projection,
                native_structural_disabled_projection=native_structural_projection,
                crafting_disabled_projection=crafting_projection,
                source_derived_synthetic_fixture=synthetic,
                selector_scoped_exact_material_triangles=(
                    selector_material_triangles
                ),
                expected_geometry_signature=expected_geometry_signature,
                expected_nonlighting_attribute_signature=(
                    expected_nonlighting_attribute_signature
                ),
            )
            anchors.append(
                AnchorContract(
                    case_id=raw_case["case_id"],
                    case_label=raw_case["label"],
                    expected_path=expected_path,
                    position=position,
                    expected_triangle_count=expected_triangle_count,
                    expected_material_triangles=expected_material_triangles,
                    expected_smart_overlays=(),
                    face_parts=(),
                    facades=(),
                    expected_terminal_layers=(),
                    drive=None,
                    fallback_reason=raw_anchor.get("fallback_reason"),
                    m45=m45,
                )
            )
        for fixture in raw_case["fixture_blocks"]:
            if not isinstance(fixture, dict):
                raise EvidenceError("schema-11 M4/M5 fixture helper is not an object")
            fixture_position = _s1_xyz(
                fixture.get("position"), "M4/M5 fixture helper position"
            )
            if fixture_position in m45_positions or fixture_position in fixture_positions:
                raise EvidenceError("schema-11 M4/M5 helper position is duplicated")
            fixture_positions.add(fixture_position)
        parsed_cases.append(
            CaseContract(
                raw_case["case_id"],
                milestone,
                route,
                raw_case["label"],
                raw_case["category"],
                tuple(anchors),
            )
        )
    if (
        len(m45_positions) != 409
        or fallback_count != 18
        or {
            anchor.position
            for case in parsed_cases
            for anchor in case.anchors
            if anchor.m45 is not None
            and anchor.expected_path == "custom-m45"
        }
        != set(m45_runtime_oracle)
        or synthetic_count != 0
        or selector_exception_positions
        != {M45_ADVANCED_SINGLETON_POSITION}
        or any(
            len(route_positions[route]) != route_profile_by_id[route]["anchor_count"]
            for route in M45_ROUTES
        )
    ):
        raise EvidenceError("schema-11 M4/M5 anchor aggregate changed")
    if (
        exact_empty_original_by_route
        != Counter(
            {
                "expandedae": 22,
                "megacells": 9,
                "advanced-ae-quantum": 37,
                "extendedae-matrix": 11,
            }
        )
        or (physical_stock_nonempty_count, 409 - physical_stock_nonempty_count)
        != (110, 299)
        or (
            combined_disabled_nonempty_count,
            409 - combined_disabled_nonempty_count,
        )
        != (299, 110)
        or (
            crafting_disabled_projection_count,
            crafting_disabled_nonempty_count,
            crafting_disabled_projection_count
            - crafting_disabled_nonempty_count,
        )
        != (20, 0, 20)
        or exact_inherited_selector_counts
        != Counter(
            {
                "merequester:requester": 12,
                "expandedae:exp_io_port[powered=false]": 24,
                "expandedae:exp_io_port[powered=true]": 24,
            }
        )
    ):
        raise EvidenceError(
            "schema-11 M4/M5 exact disabled projection closure changed"
        )

    for disabled_route in M45_ROUTES:
        projections = [
            projection
            for case in parsed_cases
            for anchor in case.anchors
            if anchor.m45 is not None
            for route_id, projection in anchor.m45.route_disabled_projections
            if route_id == disabled_route
        ]
        route_profile = route_profile_by_id[disabled_route]
        exact_route_projection = M45_DISABLED_PROJECTION_EVIDENCE[
            "single_route_projections"
        ][disabled_route]
        if (
            len(projections)
            != route_profile["route_disabled_affected_anchor_count"]
            or sum(p.review_projection == "nonempty" for p in projections)
            != route_profile["route_disabled_nonempty_anchor_count"]
            or sum(p.review_projection == "empty" for p in projections)
            != route_profile["route_disabled_empty_anchor_count"]
            or len(projections)
            != exact_route_projection["affected_anchor_count"]
            or sum(p.review_projection == "nonempty" for p in projections)
            != exact_route_projection["nonempty_anchor_count"]
            or sum(p.review_projection == "empty" for p in projections)
            != exact_route_projection["empty_anchor_count"]
        ):
            raise EvidenceError(
                f"schema-11 {disabled_route} disabled projection aggregate changed"
            )

    contract = replace(
        legacy,
        cases=tuple(parsed_cases),
        expected_selected_resources=tuple(
            sorted(set(legacy.expected_selected_resources) | set(new_resources))
        ),
        schema_version=11,
        signature_schema_version=11,
        m45_positions=tuple(sorted(m45_positions)),
        m45_route_positions=tuple(
            (route, tuple(sorted(route_positions[route]))) for route in M45_ROUTES
        ),
        m45_legacy_upgrade_positions=tuple(
            sorted(m45_legacy_upgrade_positions)
        ),
    )
    evidence = dict(legacy_evidence)
    evidence.update(
        {
            "sha256": digest,
            "schema_version": 11,
            "signature_schema_version": 11,
            "case_count": len(parsed_cases),
            "anchor_count": sum(len(case.anchors) for case in parsed_cases),
            "frozen_schema10_view_sha256": schema10_sha256,
            "m45_review_summary": summary,
            "m45_routes": route_profiles,
            "m45_legacy_upgrades": m45_legacy_upgrade_evidence,
            "m45_unit_only_mutations": unit_only,
        }
    )
    evidence["profile"] = {
        **legacy_evidence["profile"],
        "coverage_milestone": "M5-cumulative-review",
        "selected_resource_count": len(selected_resources),
        "m45_route_count": len(M45_ROUTES),
    }
    return contract, evidence


def _parse_appmek_projection(
    value: Any,
    label: str,
) -> AppMekProjectionContract:
    if (
        not isinstance(value, dict)
        or set(value) != {"expected_path", "review_projection", "reason"}
        or not isinstance(value.get("expected_path"), str)
        or value.get("review_projection") not in {"empty", "nonempty"}
        or not isinstance(value.get("reason"), str)
    ):
        raise EvidenceError(f"schema-12 {label} projection changed")
    return AppMekProjectionContract(
        expected_path=value["expected_path"],
        review_projection=value["review_projection"],
        reason=value["reason"],
    )


def _parse_schema12_cases(
    value: dict[str, Any], digest: str
) -> tuple[GalleryContract, dict[str, Any]]:
    if digest != SCHEMA12_CANONICAL_SHA256:
        raise EvidenceError("schema-12 narrow Applied Mekanistics manifest changed")
    if value.get("signature_schema_version") != 12:
        raise EvidenceError("schema-12 gallery must use signature schema 12")

    schema11 = _schema11_view(value)
    schema11_payload = canonical_json(schema11, pretty=True).encode("utf-8")
    schema11_sha256 = sha256_bytes(schema11_payload)
    if schema11_sha256 != SCHEMA11_CANONICAL_SHA256:
        raise EvidenceError(
            "schema-12 does not embed the byte-frozen accepted schema-11 view"
        )
    legacy, legacy_evidence = _parse_schema11_cases(schema11, schema11_sha256)

    all_cases = value.get("cases")
    profile = value.get("profile")
    summary = value.get("appmek_review_summary")
    route_profiles = profile.get("appmek_routes") if isinstance(profile, dict) else None
    if (
        not isinstance(all_cases, list)
        or not isinstance(profile, dict)
        or not isinstance(summary, dict)
        or not isinstance(route_profiles, list)
        or value.get("case_count") != 162
        or value.get("anchor_count") != 1_373
        or len(all_cases) != 162
    ):
        raise EvidenceError("schema-12 narrow Applied Mekanistics header changed")
    raw_cases = all_cases[-4:]
    if (
        [case.get("case_id") for case in raw_cases]
        != [f"ae2-appmek-{index:02d}" for index in range(1, 5)]
        or [len(case.get("anchors", ())) for case in raw_cases] != [1, 3, 2, 1]
        or [case.get("route") for case in raw_cases]
        != [
            APPMEK_DRIVE_ROUTE,
            APPMEK_DRIVE_ROUTE,
            "parent-renderer-controls",
            "parent-renderer-controls",
        ]
    ):
        raise EvidenceError("schema-12 exact four-case review matrix changed")
    expected_summary = {
        "case_count": 4,
        "anchor_count": 7,
        "case_anchor_allocation": [1, 3, 2, 1],
        "custom_anchor_count": 6,
        "fallback_anchor_count": 0,
        "control_anchor_count": 1,
        "review_control_anchor_count": 3,
        "fixture_block_count": 3,
        "route_ids": [APPMEK_DRIVE_ROUTE],
        "route_affected_anchor_counts": {APPMEK_DRIVE_ROUTE: 4},
        "enabled_preoracle_projection": {
            "nonempty_anchor_count": 7,
            "empty_anchor_count": 0,
        },
        "physical_stock_projection": {
            "nonempty_anchor_count": 1,
            "empty_anchor_count": 6,
        },
        "appmek_drive_route_disabled_projection": {
            "nonempty_anchor_count": 3,
            "empty_anchor_count": 4,
        },
        "native_structural_disabled_projection": {
            "nonempty_anchor_count": 5,
            "empty_anchor_count": 2,
        },
        "native_drive_disabled_projection": {
            "nonempty_anchor_count": 3,
            "empty_anchor_count": 4,
        },
        "new_selected_resource_target_count": 1,
        "new_selected_resource_targets": [APPMEK_DRIVE_TEXTURE],
        "new_selected_resource_count": 1,
        "runtime_oracle": {
            "status": "pending-exact-enabled-live-map-capture",
            "policy": "exact-prbm-geometry-material-and-nonlighting-signatures",
            "capture_anchor_count": 7,
            "expected_nonempty_anchor_count": 7,
            "expected_empty_anchor_count": 0,
            "synthetic_geometry_forbidden": True,
        },
        "unit_only_geometry_policy": (
            "existing-native-drive-and-parent-renderer-tests-remain-authoritative-"
            "beyond-the-small-live-matrix"
        ),
    }
    if summary != expected_summary:
        raise EvidenceError("schema-12 narrow Applied Mekanistics summary changed")
    if len(route_profiles) != 1:
        raise EvidenceError("schema-12 Applied Mekanistics route profile changed")
    route_profile = route_profiles[0]
    if (
        route_profile.get("route") != APPMEK_DRIVE_ROUTE
        or route_profile.get("route_resources") != [APPMEK_DRIVE_TEXTURE]
        or route_profile.get("affected_anchor_count") != 4
        or route_profile.get("case_count") != 2
        or route_profile.get("artifact", {}).get("sha256")
        != "8946fea39451dbce8e709dedbef40a52ba337bdf7a25ac0c4b503800b1bf0773"
        or route_profile.get("mekanism_runtime", {}).get("version") != "10.7.19"
        or route_profile.get("mekanism_runtime", {}).get("sha256")
        != "004dbc9f3106f4d192aeaa1ee1190dd16ec9ca8059ed3d093b80034f4c574f43"
    ):
        raise EvidenceError("schema-12 exact AppMek/Mekanism route identity changed")

    parsed: list[AppMekAnchorContract] = []
    seen_positions = {anchor.position for case in legacy.cases for anchor in case.anchors}
    expected_triangle_counts = [250, 106, 106, 106, 74, 74, 18]
    for raw_case in raw_cases:
        for raw_anchor in raw_case["anchors"]:
            position = _s1_xyz(raw_anchor.get("position"), "AppMek anchor position")
            if position in seen_positions:
                raise EvidenceError("schema-12 Applied Mekanistics position is duplicated")
            seen_positions.add(position)
            route = raw_anchor.get("appmek_route")
            expected_path = raw_anchor.get("expected_path")
            expected_materials = raw_anchor.get("expected_material_triangles", {})
            if (
                route not in {None, APPMEK_DRIVE_ROUTE}
                or not isinstance(raw_anchor.get("block_id"), str)
                or not isinstance(expected_path, str)
                or raw_anchor.get("review_projection") != "nonempty"
                or raw_anchor.get("runtime_oracle_policy")
                != "exact-live-prbm-pending-capture-no-synthetic-mesh-expectation"
                or not isinstance(raw_anchor.get("expected_triangle_count"), int)
                or not isinstance(expected_materials, dict)
                or any(
                    not isinstance(resource, str)
                    or not isinstance(count, int)
                    or count <= 0
                    for resource, count in expected_materials.items()
                )
                or {
                    "expected_geometry_signature",
                    "expected_nonlighting_attribute_signature",
                    "source_derived_synthetic_fixture",
                }
                & set(raw_anchor)
            ):
                raise EvidenceError("schema-12 Applied Mekanistics anchor changed")
            parsed.append(
                AppMekAnchorContract(
                    case_id=raw_case["case_id"],
                    position=position,
                    route=route,
                    block_id=raw_anchor["block_id"],
                    expected_path=expected_path,
                    expected_triangle_count=raw_anchor["expected_triangle_count"],
                    expected_material_triangles=tuple(sorted(expected_materials.items())),
                    physical_stock_projection=_parse_appmek_projection(
                        raw_anchor.get("physical_stock_projection"),
                        f"physical stock at {position}",
                    ),
                    route_disabled_projection=_parse_appmek_projection(
                        raw_anchor.get("route_disabled_projection"),
                        f"route disabled at {position}",
                    ),
                    native_structural_disabled_projection=_parse_appmek_projection(
                        raw_anchor.get("native_structural_disabled_projection"),
                        f"native structural disabled at {position}",
                    ),
                    native_drive_disabled_projection=_parse_appmek_projection(
                        raw_anchor.get("native_drive_disabled_projection"),
                        f"native Drive disabled at {position}",
                    ),
                )
            )
    if (
        len(parsed) != 7
        or [anchor.expected_triangle_count for anchor in parsed]
        != expected_triangle_counts
        or [anchor.route for anchor in parsed]
        != [APPMEK_DRIVE_ROUTE] * 4 + [None] * 3
        or [anchor.block_id for anchor in parsed]
        != ["ae2:drive"] * 4
        + ["ae2:cable_bus", "ae2:cable_bus", "mekanism:basic_pressurized_tube"]
        or [dict(anchor.expected_material_triangles).get(APPMEK_DRIVE_TEXTURE, 0)
            for anchor in parsed]
        != [60, 6, 6, 6, 0, 0, 0]
    ):
        raise EvidenceError("schema-12 Applied Mekanistics selector closure changed")
    tube = raw_cases[-1]["anchors"][0]
    if tube.get("transmitter_topology") != {
        "acceptors_unsigned_byte": 32,
        "connection_mode_ordinals": [0, 0, 0, 0, 0, 0],
        "connections_unsigned_byte": 0,
        "direction_ordinal_order": ["down", "up", "north", "south", "west", "east"],
        "east_mode": "normal",
    }:
        raise EvidenceError("schema-12 pressurized-tube seam topology changed")

    selected_resources = profile.get("selected_resources")
    if (
        not isinstance(selected_resources, list)
        or selected_resources[-1:] != [APPMEK_DRIVE_TEXTURE]
        or len(selected_resources) != 434
    ):
        raise EvidenceError("schema-12 selected-resource closure changed")
    route_positions = tuple(
        sorted(anchor.position for anchor in parsed if anchor.route == APPMEK_DRIVE_ROUTE)
    )
    contract = replace(
        legacy,
        expected_selected_resources=tuple(sorted(selected_resources)),
        schema_version=12,
        signature_schema_version=12,
        appmek_anchors=tuple(parsed),
        appmek_positions=tuple(sorted(anchor.position for anchor in parsed)),
        appmek_route_positions=route_positions,
    )
    evidence = dict(legacy_evidence)
    evidence.update(
        {
            "sha256": digest,
            "schema_version": 12,
            "signature_schema_version": 12,
            "case_count": 162,
            "anchor_count": 1_373,
            "frozen_schema11_view_sha256": schema11_sha256,
            "appmek_review_summary": summary,
            "appmek_routes": route_profiles,
            "appmek_preoracle_excluded_anchor_count": 7,
        }
    )
    evidence["profile"] = {
        **legacy_evidence["profile"],
        "coverage_milestone": "Applied-Mekanistics-extension-review",
        "selected_resource_count": len(selected_resources),
        "appmek_route_count": 1,
    }
    return contract, evidence


def parse_cases(path: Path) -> tuple[GalleryContract, dict[str, Any]]:
    value, digest = read_json(path, "gallery cases manifest")
    if not isinstance(value, dict):
        raise EvidenceError("gallery cases manifest is not an object")
    if value.get("schema_version") == 12:
        return _parse_schema12_cases(value, digest)
    if value.get("schema_version") == 11:
        return _parse_schema11_cases(value, digest)
    if value.get("schema_version") == 10:
        return _parse_schema10_cases(value, digest)
    if value.get("schema_version") == 9:
        return _parse_schema9_cases(value, digest)
    if value.get("schema_version") == 8:
        return _parse_schema8_cases(value, digest)
    if value.get("schema_version") == 7:
        return _parse_schema7_cases(value, digest)
    if value.get("schema_version") == 6:
        return _parse_schema6_cases(value, digest)
    if value.get("schema_version") == 5:
        return _parse_schema5_cases(value, digest)
    return _parse_cases_value(value, digest)


def _axis_path(prefix: str, value: int) -> list[str]:
    sign = "-" if value < 0 else ""
    digits = str(abs(value))
    return [prefix + sign + digits[0], *digits[1:]]


def tile_path(map_root: Path, tile_x: int, tile_z: int) -> Path:
    parts = _axis_path("x", tile_x) + _axis_path("z", tile_z)
    parts[-1] += ".prbm.gz"
    return map_root.joinpath("tiles", "0", *parts)


def _material_for_vertex(groups: Sequence[MaterialGroup], vertex: int) -> int:
    # Material groups are few and contiguous in exact writer output.
    low = 0
    high = len(groups)
    while low < high:
        middle = (low + high) // 2
        group = groups[middle]
        if vertex < group.start:
            high = middle
        elif vertex >= group.start + group.count:
            low = middle + 1
        else:
            return group.material_index
    raise EvidenceError(f"no material group owns vertex {vertex}")


def _cross_normal(
    positions: Sequence[tuple[float, float, float]],
) -> tuple[float, float, float]:
    first, second, third = positions
    ab = tuple(second[index] - first[index] for index in range(3))
    ac = tuple(third[index] - first[index] for index in range(3))
    cross = (
        ab[1] * ac[2] - ab[2] * ac[1],
        ab[2] * ac[0] - ab[0] * ac[2],
        ab[0] * ac[1] - ab[1] * ac[0],
    )
    length = math.sqrt(sum(value * value for value in cross))
    if not math.isfinite(length) or length <= 0.0:
        raise EvidenceError("PRBM contains a degenerate triangle")
    return tuple(value / length for value in cross)  # type: ignore[return-value]


def _triangle_owner(
    positions: Sequence[tuple[float, float, float]],
) -> tuple[int, int, int]:
    normal = _cross_normal(positions)
    centroid = tuple(
        sum(position[index] for position in positions) / 3.0 for index in range(3)
    )
    inward = tuple(
        centroid[index] - normal[index] * OWNERSHIP_EPSILON for index in range(3)
    )
    return tuple(math.floor(value) for value in inward)  # type: ignore[return-value]


def _triangle_closed_cube_owners(
    positions: Sequence[tuple[float, float, float]],
) -> set[tuple[int, int, int]]:
    """Return unit cells whose closed cube contains the whole triangle.

    Facade cutout walls can terminate exactly on a block boundary while their
    geometric normal points into the cutout.  Those concave faces deliberately
    violate the ordinary outward-winding ownership inference.  The same pinned
    epsilon used for dense cable overhangs bounds this secondary containment
    test; it is never used while the primary winding owner is selected.
    """
    axis_ranges: list[range] = []
    for axis in range(3):
        minimum = min(position[axis] for position in positions)
        maximum = max(position[axis] for position in positions)
        first = math.ceil(maximum - 1.0 - OWNERSHIP_EPSILON)
        last = math.floor(minimum + OWNERSHIP_EPSILON)
        if first > last:
            return set()
        axis_ranges.append(range(first, last + 1))
    return {
        (x, y, z)
        for x, y, z in product(*axis_ranges)
    }


def _native_plane_facade_wall_matches(
    anchor: AnchorContract,
    positions: Sequence[tuple[float, float, float]],
    material_identity: str,
) -> bool:
    """Recognize only source-derived S1 plane-facade slab walls.

    FacadeBuilder's ring boxes emit inward-wound side walls on exact block
    boundaries.  Their winding owner is therefore the neighboring cell.  The
    recovery is deliberately narrower than closed-cube containment: it is
    available only to a declared schema-10 native plane with an exact same-face
    stone/glass strip, plus the one declared UP-glass/NORTH-stone normalized
    cross-face strip.  Both branches require exactly three corners of one
    enabled FacadeBuilder rectangle with its exact inward cardinal winding.
    """
    native = anchor.native_structural
    if (
        anchor.expected_path != "custom-s1"
        or native is None
        or native.plane_mask is None
    ):
        return False
    plane_parts = [part for part in native.parts if part.group == "plane"]
    if len(plane_parts) != 1:
        return False
    plane = plane_parts[0]
    facade = next(
        (
            candidate
            for candidate in anchor.facades
            if candidate.direction == plane.direction
        ),
        None,
    )
    if facade is None:
        return False
    state = json.loads(facade.block_state_json)
    same_face_material = {
        "minecraft:stone": "minecraft:block/stone",
        "minecraft:glass": NATIVE_STRUCTURAL_GLASSENTIAL_MATERIAL,
    }.get(state.get("Name"))
    perpendicular_normalized_strip = False
    if material_identity != same_face_material:
        facade_states = {
            candidate.direction: json.loads(candidate.block_state_json)
            for candidate in anchor.facades
        }
        perpendicular_normalized_strip = (
            anchor.case_id == "ae2-s1-07"
            and native.cable_id == "ae2:fluix_covered_cable"
            and plane.direction == "up"
            and plane.part_id == "ae2:annihilation_plane"
            and native.plane_mask == 8
            and facade_states
            == {
                "up": {"Name": "minecraft:glass"},
                "north": {"Name": "minecraft:stone"},
            }
            and material_identity == "minecraft:block/stone"
        )
        if not perpendicular_normalized_strip:
            return False

    local = tuple(
        tuple(position[axis] - anchor.position[axis] for axis in range(3))
        for position in positions
    )
    if any(
        coordinate < -OWNERSHIP_EPSILON
        or coordinate > 1.0 + OWNERSHIP_EPSILON
        for position in local
        for coordinate in position
    ):
        return False

    unit = 1.0 / 16.0
    thickness = NATIVE_STRUCTURAL_FACADE_THICKNESS
    rectangles: list[
        tuple[int, tuple[int, int, int], tuple[tuple[float, float, float], ...]]
    ] = []

    def add_rectangle(
        bit: int,
        normal: tuple[int, int, int],
        axis_a: int,
        range_a: tuple[float, float],
        axis_b: int,
        range_b: tuple[float, float],
        fixed_axis: int,
        fixed_value: float,
    ) -> None:
        if not native.plane_mask & bit:
            return
        corners = []
        for value_a, value_b in (
            (range_a[0], range_b[0]),
            (range_a[1], range_b[0]),
            (range_a[1], range_b[1]),
            (range_a[0], range_b[1]),
        ):
            corner = [0.0, 0.0, 0.0]
            corner[axis_a] = value_a
            corner[axis_b] = value_b
            corner[fixed_axis] = fixed_value
            corners.append(tuple(corner))
        rectangles.append((bit, normal, tuple(corners)))

    if perpendicular_normalized_strip:
        # In the one declared cross-face source control, the UP plane's bit-8
        # collision sheet reaches the NORTH edge.  FacadeBuilder's normalized
        # NORTH stone strip consequently retains this exact inward top wall.
        # It is distinct from the same-face UP glass cutout walls above.
        add_rectangle(
            8,
            (0, -1, 0),
            0,
            (0.0, 1.0),
            2,
            (0.0, thickness),
            1,
            1.0,
        )
    elif plane.direction == "up":
        slab = (1.0 - thickness, 1.0)
        x_span = (
            0.0 if native.plane_mask & 1 else unit,
            1.0 if native.plane_mask & 4 else 15.0 * unit,
        )
        add_rectangle(1, (1, 0, 0), 1, slab, 2, (0.0, 1.0), 0, 0.0)
        add_rectangle(4, (-1, 0, 0), 1, slab, 2, (0.0, 1.0), 0, 1.0)
        add_rectangle(8, (0, 0, 1), 0, x_span, 1, slab, 2, 0.0)
        add_rectangle(2, (0, 0, -1), 0, x_span, 1, slab, 2, 1.0)
    elif plane.direction == "north":
        slab = (0.0, thickness)
        y_span = (
            0.0 if native.plane_mask & 2 else unit,
            1.0 if native.plane_mask & 8 else 15.0 * unit,
        )
        add_rectangle(1, (-1, 0, 0), 1, y_span, 2, slab, 0, 1.0)
        add_rectangle(4, (1, 0, 0), 1, y_span, 2, slab, 0, 0.0)
        add_rectangle(2, (0, 1, 0), 0, (0.0, 1.0), 2, slab, 1, 0.0)
        add_rectangle(8, (0, -1, 0), 0, (0.0, 1.0), 2, slab, 1, 1.0)
    else:
        return False

    normal = _cross_normal(local)
    matches = 0
    for _bit, expected_normal, corners in rectangles:
        if any(
            not _near(normal[axis], float(expected_normal[axis]))
            for axis in range(3)
        ):
            continue
        matched_corners: set[int] = set()
        for position in local:
            corner_index = next(
                (
                    index
                    for index, corner in enumerate(corners)
                    if all(
                        _near(position[axis], corner[axis])
                        for axis in range(3)
                    )
                ),
                None,
            )
            if corner_index is None:
                break
            matched_corners.add(corner_index)
        else:
            if len(matched_corners) == 3:
                matches += 1
    return matches == 1


def _selected_triangle_owner(
    positions: Sequence[tuple[float, float, float]],
    selected_positions: set[tuple[int, int, int]],
    recovery_anchors: dict[tuple[int, int, int], AnchorContract] | None = None,
    material_identity: str | None = None,
) -> tuple[int, int, int] | None:
    """Resolve one owner with an S1 plane-facade-only wall recovery."""
    primary = _triangle_owner(positions)
    if primary in selected_positions:
        return primary
    if recovery_anchors is None or material_identity is None:
        return None
    candidates = {
        candidate
        for candidate in _triangle_closed_cube_owners(positions)
        & selected_positions
        if candidate in recovery_anchors
        and _native_plane_facade_wall_matches(
            recovery_anchors[candidate], positions, material_identity
        )
    }
    if len(candidates) == 1:
        return next(iter(candidates))
    if len(candidates) > 1:
        raise EvidenceError(
            "PRBM triangle has ambiguous native plane-facade wall ownership: "
            f"{sorted(candidates)}"
        )
    return None


def _rotate_to_smallest(
    geometry_vertices: list[dict[str, Any]],
    attribute_vertices: list[dict[str, Any]],
) -> tuple[list[dict[str, Any]], list[dict[str, Any]], int]:
    rotations = [
        geometry_vertices[offset:] + geometry_vertices[:offset] for offset in range(3)
    ]
    keys = [canonical_json(rotation) for rotation in rotations]
    offset = min(range(3), key=lambda item: keys[item])
    return (
        rotations[offset],
        attribute_vertices[offset:] + attribute_vertices[:offset],
        offset,
    )


def _triangle_record(
    document: PrbmDocument,
    triangle_index: int,
    positions: Sequence[tuple[float, float, float]],
    anchor: tuple[int, int, int],
    texture: TextureRef,
) -> TriangleRecord:
    shape_vertices: list[dict[str, Any]] = []
    geometry_vertices: list[dict[str, Any]] = []
    attribute_vertices: list[dict[str, Any]] = []
    relative_positions: list[tuple[float, float, float]] = []
    raw_uvs: list[tuple[float, float]] = []
    for corner in range(3):
        vertex_index = triangle_index * 3 + corner
        relative_position = tuple(
            positions[corner][axis] - anchor[axis] for axis in range(3)
        )
        uv = document.values("uv", vertex_index)
        relative_positions.append(relative_position)
        raw_uvs.append((float(uv[0]), float(uv[1])))
        geometry_vertex = {
            "position": [canonical_float(value) for value in relative_position],
            "uv": [canonical_float(float(value)) for value in uv],
        }
        shape_vertices.append(
            {
                "position": [
                    canonical_shape_float(value) for value in relative_position
                ]
            }
        )
        geometry_vertices.append(geometry_vertex)
        attribute_vertices.append(
            {
                **geometry_vertex,
                "normal_raw_i8": list(document.values("normal", vertex_index)),
                "color_raw_u8": list(document.values("color", vertex_index)),
                "ao_raw_u8": document.values("ao", vertex_index)[0],
                "blocklight_raw_i8": document.values("blocklight", vertex_index)[0],
                "sunlight_raw_i8": document.values("sunlight", vertex_index)[0],
            }
        )
    geometry_vertices, attribute_vertices, rotation_offset = _rotate_to_smallest(
        geometry_vertices, attribute_vertices
    )
    relative_positions = (
        relative_positions[rotation_offset:] + relative_positions[:rotation_offset]
    )
    raw_uvs = raw_uvs[rotation_offset:] + raw_uvs[:rotation_offset]
    shape_rotations = [
        shape_vertices[offset:] + shape_vertices[:offset] for offset in range(3)
    ]
    shape_vertices = min(shape_rotations, key=canonical_json)
    material_identity = texture.semantic_identity
    geometry = canonical_json(
        {"material": material_identity, "vertices": geometry_vertices}
    )
    attributes = canonical_json(
        {"material": material_identity, "vertices": attribute_vertices}
    )
    return TriangleRecord(
        texture.index,
        material_identity,
        canonical_json({"vertices": shape_vertices}),
        geometry,
        attributes,
        tuple(tuple(vertex["color_raw_u8"]) for vertex in attribute_vertices),
        tuple(vertex["ao_raw_u8"] for vertex in attribute_vertices),
        tuple(vertex["blocklight_raw_i8"] for vertex in attribute_vertices),
        tuple(vertex["sunlight_raw_i8"] for vertex in attribute_vertices),
        tuple(relative_positions),
        tuple(raw_uvs),
        tuple(tuple(vertex["normal_raw_i8"]) for vertex in attribute_vertices),
    )


def _direction_for_axis(axis: int, positive: bool) -> str:
    return (
        ("west", "east"),
        ("down", "up"),
        ("north", "south"),
    )[axis][1 if positive else 0]


def _boundary_direction(
    positions: Iterable[tuple[float, float, float]],
) -> str:
    values = list(positions)
    candidates: list[str] = []
    for axis in range(3):
        coordinates = [position[axis] for position in values]
        if max(coordinates) - min(coordinates) > GEOMETRY_TOLERANCE:
            continue
        coordinate = sum(coordinates) / len(coordinates)
        if abs(coordinate) <= GEOMETRY_TOLERANCE:
            candidates.append(_direction_for_axis(axis, False))
        elif abs(coordinate - 1.0) <= GEOMETRY_TOLERANCE:
            candidates.append(_direction_for_axis(axis, True))
    if len(candidates) != 1:
        raise EvidenceError("terminal layer is not on exactly one block boundary")
    return candidates[0]


def _vector_direction(vector: tuple[float, float, float]) -> str:
    axis = max(range(3), key=lambda index: abs(vector[index]))
    if abs(vector[axis]) < 0.5 or any(
        abs(vector[index]) > GEOMETRY_TOLERANCE
        for index in range(3)
        if index != axis
    ):
        raise EvidenceError("terminal UV-up vector is not axis aligned")
    return _direction_for_axis(axis, vector[axis] > 0)


def _terminal_layout(records: Sequence[TriangleRecord]) -> tuple[str, str]:
    if len(records) != 2:
        raise EvidenceError("terminal layer must contain exactly two triangles per part")
    positions = [position for record in records for position in record.positions]
    direction = _boundary_direction(positions)
    fixed_axis = next(
        axis
        for axis, component in enumerate(DIRECTION_VECTORS[direction])
        if component
    )
    for axis in range(3):
        if axis == fixed_axis:
            continue
        coordinates = [position[axis] for position in positions]
        if (
            abs(min(coordinates) - 0.125) > GEOMETRY_TOLERANCE
            or abs(max(coordinates) - 0.875) > GEOMETRY_TOLERANCE
        ):
            raise EvidenceError("terminal layer does not use the exact 12x12 face square")

    corners: dict[tuple[int, int], tuple[float, float, float]] = {}
    for record in records:
        for position, uv in zip(record.positions, record.uvs, strict=True):
            if (
                min(abs(uv[0] - 0.125), abs(uv[0] - 0.875))
                > GEOMETRY_TOLERANCE
                or min(abs(uv[1] - 0.125), abs(uv[1] - 0.875))
                > GEOMETRY_TOLERANCE
            ):
                raise EvidenceError("terminal layer UVs differ from the exact model")
            key = (0 if uv[0] < 0.5 else 1, 0 if uv[1] < 0.5 else 1)
            previous = corners.setdefault(key, position)
            if any(
                abs(previous[axis] - position[axis]) > GEOMETRY_TOLERANCE
                for axis in range(3)
            ):
                raise EvidenceError("terminal layer maps one UV corner to multiple positions")
    if set(corners) != {(0, 0), (0, 1), (1, 0), (1, 1)}:
        raise EvidenceError("terminal layer does not contain all four UV corners")
    top = tuple(
        (corners[(0, 0)][axis] + corners[(1, 0)][axis]) / 2.0
        for axis in range(3)
    )
    bottom = tuple(
        (corners[(0, 1)][axis] + corners[(1, 1)][axis]) / 2.0
        for axis in range(3)
    )
    up_direction = _vector_direction(
        tuple(top[axis] - bottom[axis] for axis in range(3))
    )
    return direction, up_direction


def _validate_terminal_contract(
    anchor: AnchorContract,
    records: Sequence[TriangleRecord],
) -> dict[str, Any]:
    if not anchor.face_parts:
        if anchor.expected_terminal_layers:
            raise EvidenceError(f"terminal-free anchor {anchor.position} declares layers")
        return {}
    expected_parts = {part.direction: part for part in anchor.face_parts}
    if len(expected_parts) != len(anchor.face_parts):
        raise EvidenceError(f"terminal anchor {anchor.position} repeats a face")
    result: dict[str, Any] = {}
    for layer in anchor.expected_terminal_layers:
        layer_records = [
            record for record in records if record.material_identity == layer.resource_path
        ]
        if len(layer_records) != layer.triangle_count_per_part * len(expected_parts):
            raise EvidenceError(
                f"terminal anchor {anchor.position} has the wrong layer triangle count"
            )
        if any(
            color != layer.rgb
            for record in layer_records
            for color in record.colors
        ):
            raise EvidenceError(
                f"terminal anchor {anchor.position} layer tint differs from the M2 contract"
            )
        fullbright_vertices = [
            blocklight == 15 and sunlight == 15
            for record in layer_records
            for blocklight, sunlight in zip(
                record.blocklights, record.sunlights, strict=True
            )
        ]
        if layer.emissive != all(fullbright_vertices):
            raise EvidenceError(
                f"terminal anchor {anchor.position} layer emissivity differs from the M2 contract"
            )

        by_direction: dict[str, list[TriangleRecord]] = {}
        for record in layer_records:
            direction = _boundary_direction(record.positions)
            by_direction.setdefault(direction, []).append(record)
        if set(by_direction) != set(expected_parts):
            raise EvidenceError(
                f"terminal anchor {anchor.position} layers are on the wrong faces"
            )
        layouts: dict[str, dict[str, Any]] = {}
        for direction, part in sorted(expected_parts.items()):
            part_records = by_direction[direction]
            if len(part_records) != layer.triangle_count_per_part:
                raise EvidenceError(
                    f"terminal anchor {anchor.position} face has the wrong layer count"
                )
            observed_direction, observed_up = _terminal_layout(part_records)
            expected_up = TERMINAL_UP_DIRECTIONS[direction][part.spin]  # type: ignore[index]
            if observed_direction != direction or observed_up != expected_up:
                raise EvidenceError(
                    f"terminal anchor {anchor.position} spin/layout differs from the M2 contract"
                )
            layouts[direction] = {"spin": part.spin, "up": observed_up}
        result[layer.resource_path] = {
            "rgb_u8": list(layer.rgb),
            "emissive": layer.emissive,
            "triangle_count_per_part": layer.triangle_count_per_part,
            "layouts": layouts,
        }
    return result


def _validate_facade_contract(
    anchor: AnchorContract,
    records: Sequence[TriangleRecord],
) -> dict[str, Any]:
    if not anchor.facades:
        return {}
    if len(anchor.facades) != 1:
        raise EvidenceError(f"custom facade anchor {anchor.position} has multiple facades")
    facade = anchor.facades[0]
    facade_records = [
        record for record in records if record.material_identity == "minecraft:block/stone"
    ]
    if len(facade_records) != 48:
        raise EvidenceError(f"facade anchor {anchor.position} must contain 48 stone triangles")
    positions = [position for record in facade_records for position in record.positions]
    candidates: list[str] = []
    for axis in range(3):
        coordinates = [position[axis] for position in positions]
        if min(coordinates) >= -GEOMETRY_TOLERANCE and max(coordinates) <= (
            FACADE_THICKNESS + GEOMETRY_TOLERANCE
        ):
            candidates.append(_direction_for_axis(axis, False))
        if min(coordinates) >= (1.0 - FACADE_THICKNESS - GEOMETRY_TOLERANCE) and max(
            coordinates
        ) <= (1.0 + GEOMETRY_TOLERANCE):
            candidates.append(_direction_for_axis(axis, True))
    if candidates != [facade.direction]:
        raise EvidenceError(f"facade anchor {anchor.position} is on the wrong face")
    fixed_axis = next(
        axis
        for axis, component in enumerate(DIRECTION_VECTORS[facade.direction])
        if component
    )
    projected_axes = [axis for axis in range(3) if axis != fixed_axis]
    for axis in projected_axes:
        coordinates = [position[axis] for position in positions]
        if min(coordinates) > GEOMETRY_TOLERANCE or max(coordinates) < 1.0 - GEOMETRY_TOLERANCE:
            raise EvidenceError(f"facade anchor {anchor.position} does not span its block face")
    for record in facade_records:
        centroid = tuple(
            sum(position[axis] for position in record.positions) / 3.0
            for axis in projected_axes
        )
        if all(
            FACADE_HOLE_MIN + GEOMETRY_TOLERANCE
            < coordinate
            < FACADE_HOLE_MAX - GEOMETRY_TOLERANCE
            for coordinate in centroid
        ):
            raise EvidenceError(f"facade anchor {anchor.position} fills its terminal hole")
    return {
        "direction": facade.direction,
        "block_state": json.loads(facade.block_state_json),
        "material": "minecraft:block/stone",
        "triangle_count": 48,
    }


def _validate_active_facade_contract(
    anchor: AnchorContract,
    records: Sequence[TriangleRecord],
) -> dict[str, Any]:
    """Dispatch old M2 facade goldens only for their original renderer path."""
    if anchor.expected_path == "custom-s1" and anchor.native_structural is not None:
        return {}
    return _validate_facade_contract(anchor, records)


def _glass_triangle_key(
    material: str,
    positions: Sequence[tuple[float, float, float]],
    uvs: Sequence[tuple[float, float]],
) -> str:
    vertices = [
        {
            "position": [canonical_float(float(value)) for value in position],
            "uv": [canonical_float(float(value)) for value in uv],
        }
        for position, uv in zip(positions, uvs, strict=True)
    ]
    rotations = [vertices[offset:] + vertices[:offset] for offset in range(3)]
    return canonical_json({"material": material, "vertices": min(rotations, key=canonical_json)})


def _glass_normal_raw(direction: str) -> tuple[int, int, int]:
    return tuple(
        max(-128, min(127, math.trunc(component * 128.0 - 0.5)))
        for component in DIRECTION_VECTORS[direction]
    )


def _validate_connected_glass_contract(
    anchor: AnchorContract,
    records: Sequence[TriangleRecord],
) -> dict[str, Any]:
    if anchor.glass is None:
        return {}
    expected: Counter[str] = Counter()
    expected_directions = {face.direction for face in anchor.glass.faces}
    result_faces: dict[str, Any] = {}
    for face in anchor.glass.faces:
        corners = CONNECTED_GLASS_FACE_CORNERS[face.direction]
        for indexes in ((0, 1, 2), (0, 2, 3)):
            expected[
                _glass_triangle_key(
                    face.base_resource,
                    tuple(corners[index] for index in indexes),
                    tuple(face.base_uvs[index] for index in indexes),
                )
            ] += 1
            if face.frame_resource is not None:
                expected[
                    _glass_triangle_key(
                        face.frame_resource,
                        tuple(corners[index] for index in indexes),
                        tuple(face.frame_uvs[index] for index in indexes),
                    )
                ] += 1
        result_faces[face.direction] = {
            "base_resource": face.base_resource,
            "frame_mask": face.frame_mask,
            "frame_resource": face.frame_resource,
            "triangle_count": 2 + (2 if face.frame_resource is not None else 0),
        }

    actual: Counter[str] = Counter()
    observed_directions: set[str] = set()
    face_lights: dict[str, set[tuple[int, int]]] = {}
    for record in records:
        direction = _boundary_direction(record.positions)
        observed_directions.add(direction)
        expected_normal = _glass_normal_raw(direction)
        if any(normal != expected_normal for normal in record.normals):
            raise EvidenceError(
                f"connected-glass anchor {anchor.position} has wrong {direction} normals"
            )
        if (
            any(color != (255, 255, 255) for color in record.colors)
            or any(ao != 255 for ao in record.aos)
        ):
            raise EvidenceError(
                f"connected-glass anchor {anchor.position} has wrong color/AO"
            )
        light_pairs = set(zip(record.blocklights, record.sunlights, strict=True))
        if (
            len(light_pairs) != 1
            or any(
                blocklight not in range(16) or sunlight not in range(16)
                for blocklight, sunlight in light_pairs
            )
        ):
            raise EvidenceError(
                f"connected-glass anchor {anchor.position} has invalid world-derived light"
            )
        face_lights.setdefault(direction, set()).update(light_pairs)
        actual[_glass_triangle_key(record.material_identity, record.positions, record.uvs)] += 1
    if observed_directions != expected_directions:
        hidden = set(DIRECTION_VECTORS) - expected_directions
        raise EvidenceError(
            f"connected-glass anchor {anchor.position} visible/shared faces changed; "
            f"observed={sorted(observed_directions)}, hidden={sorted(hidden)}"
        )
    if actual != expected:
        raise EvidenceError(
            f"connected-glass anchor {anchor.position} geometry/corners/material/UV changed"
        )
    if any(len(values) != 1 for values in face_lights.values()):
        raise EvidenceError(
            f"connected-glass anchor {anchor.position} base/frame face light is inconsistent"
        )
    if anchor.glass.block_id == "ae2:quartz_vibrant_glass" and any(
        next(iter(values))[0] != 15 for values in face_lights.values()
    ):
        raise EvidenceError(
            f"connected-glass anchor {anchor.position} vibrant blocklight is not 15"
        )
    for direction, light in face_lights.items():
        blocklight, sunlight = next(iter(light))
        result_faces[direction]["world_light"] = {
            "blocklight_raw_i8": blocklight,
            "sunlight_raw_i8": sunlight,
        }
    return {
        "block_id": anchor.glass.block_id,
        "texture_index": anchor.glass.texture_index,
        "connected_faces": list(anchor.glass.connected_faces),
        "opaque_culled_faces": list(anchor.glass.opaque_culled_faces),
        "faces": result_faces,
        "visible_face_count": len(anchor.glass.faces),
        "visible_frame_face_count": sum(
            face.frame_resource is not None for face in anchor.glass.faces
        ),
        "triangle_formula": "2*visibleFaces+2*visibleFrameFaces",
        "attributes": {
            "rgb_u8": [255, 255, 255],
            "ambient_occlusion_raw_u8": 255,
            "light_policy": "world-derived-with-vibrant-center-emission-floor-15",
        },
        "validated": True,
    }


def _crafting_quad(
    direction: str,
    bounds_sixteenths: Sequence[float],
) -> tuple[
    tuple[tuple[float, float, float], ...],
    tuple[tuple[float, float], ...],
]:
    x1, y1, z1, x2, y2, z2 = (
        float(value) / 16.0 for value in bounds_sixteenths
    )
    positions = {
        "down": ((x1, y1, z2), (x1, y1, z1), (x2, y1, z1), (x2, y1, z2)),
        "up": ((x1, y2, z1), (x1, y2, z2), (x2, y2, z2), (x2, y2, z1)),
        "north": ((x2, y2, z1), (x2, y1, z1), (x1, y1, z1), (x1, y2, z1)),
        "south": ((x1, y2, z2), (x1, y1, z2), (x2, y1, z2), (x2, y2, z2)),
        "west": ((x1, y2, z1), (x1, y1, z1), (x1, y1, z2), (x1, y2, z2)),
        "east": ((x2, y2, z2), (x2, y1, z2), (x2, y1, z1), (x2, y2, z1)),
    }[direction]
    if direction in {"down", "up"}:
        u1, u2, v1, v2 = x1, x2, z1, z2
        uvs = ((u1, v1), (u1, v2), (u2, v2), (u2, v1))
    else:
        v1, v2 = 1.0 - y1, 1.0 - y2
        if direction == "north":
            u1, u2 = 1.0 - x2, 1.0 - x1
        elif direction == "south":
            u1, u2 = x1, x2
        elif direction == "west":
            u1, u2 = z1, z2
        else:
            u1, u2 = 1.0 - z2, 1.0 - z1
        uvs = ((u1, v2), (u1, v1), (u2, v1), (u2, v2))
    return positions, uvs


def _crafting_triangle_key(
    material: str,
    positions: Sequence[tuple[float, float, float]],
    uvs: Sequence[tuple[float, float]],
) -> str:
    vertices = [
        {
            "position_q16": [
                round(component / SHAPE_QUANTUM) for component in position
            ],
            "uv_q16": [round(component / SHAPE_QUANTUM) for component in uv],
        }
        for position, uv in zip(positions, uvs, strict=True)
    ]
    rotations = [vertices[offset:] + vertices[:offset] for offset in range(3)]
    return canonical_json(
        {"material": material, "vertices": min(rotations, key=canonical_json)}
    )


def _validate_crafting_contract(
    anchor: AnchorContract,
    records: Sequence[TriangleRecord],
) -> dict[str, Any]:
    crafting = anchor.crafting
    if crafting is None:
        return {}
    if crafting.fully_enclosed_zero_geometry:
        if records or crafting.faces:
            raise EvidenceError(
                f"fully enclosed crafting anchor {anchor.position} must emit zero geometry"
            )
        return {
            "block_id": crafting.block_id,
            "kind": crafting.kind,
            "formed": crafting.formed,
            "powered": crafting.powered,
            "connections": list(crafting.connections),
            "triangle_count": 0,
            "evidence_status": "fully-enclosed-zero-geometry",
            "renderer_provenance_status": (
                "not-renderer-provenance-distinguishable-in-prbm"
            ),
            "validated_topology": True,
        }

    expected: Counter[str] = Counter()
    expected_attributes: dict[
        str, tuple[str, CraftingPrimitiveContract]
    ] = {}
    result_faces: dict[str, Any] = {}
    for face in crafting.faces:
        primitive_results: list[dict[str, Any]] = []
        for primitive in face.primitives:
            corners, uvs = _crafting_quad(
                face.direction, primitive.bounds_sixteenths
            )
            for indexes in ((0, 1, 2), (0, 2, 3)):
                key = _crafting_triangle_key(
                    primitive.resource_path,
                    tuple(corners[index] for index in indexes),
                    tuple(uvs[index] for index in indexes),
                )
                expected[key] += 1
                previous = expected_attributes.setdefault(
                    key, (face.direction, primitive)
                )
                if previous != (face.direction, primitive):
                    raise EvidenceError(
                        f"crafting anchor {anchor.position} has ambiguous primitive geometry"
                    )
            primitive_results.append(
                {
                    "role": primitive.role,
                    "resource": primitive.resource_path,
                    "bounds_sixteenths": list(primitive.bounds_sixteenths),
                    "rgb_u8": list(primitive.rgb),
                    "light_policy": (
                        "fullbright-15"
                        if primitive.emissive
                        else "world-derived-face-light"
                    ),
                    "triangle_count": 2,
                }
            )
        result_faces[face.direction] = {
            "primitives": primitive_results,
            "triangle_count": 2 * len(face.primitives),
        }

    actual: Counter[str] = Counter()
    observed_directions: set[str] = set()
    face_world_lights: dict[str, set[tuple[int, int]]] = {}
    fullbright_triangle_count = 0
    for record in records:
        key = _crafting_triangle_key(
            record.material_identity, record.positions, record.uvs
        )
        actual[key] += 1
        expected_attribute = expected_attributes.get(key)
        if expected_attribute is None:
            continue
        direction, primitive = expected_attribute
        observed_directions.add(direction)
        if any(
            normal != _glass_normal_raw(direction) for normal in record.normals
        ):
            raise EvidenceError(
                f"crafting anchor {anchor.position} has wrong {direction} normals"
            )
        if (
            any(color != primitive.rgb for color in record.colors)
            or any(ao != 255 for ao in record.aos)
        ):
            raise EvidenceError(
                f"crafting anchor {anchor.position} has wrong primitive color/AO"
            )
        light_pairs = set(zip(record.blocklights, record.sunlights, strict=True))
        if primitive.emissive:
            if light_pairs != {(15, 15)}:
                raise EvidenceError(
                    f"crafting anchor {anchor.position} powered overlay is not fullbright"
                )
            fullbright_triangle_count += 1
        else:
            if (
                len(light_pairs) != 1
                or any(
                    blocklight not in range(16) or sunlight not in range(16)
                    for blocklight, sunlight in light_pairs
                )
            ):
                raise EvidenceError(
                    f"crafting anchor {anchor.position} has invalid world-derived face light"
                )
            face_world_lights.setdefault(direction, set()).update(light_pairs)
    if actual != expected:
        raise EvidenceError(
            f"crafting anchor {anchor.position} topology/material/UV/winding changed"
        )
    expected_directions = {face.direction for face in crafting.faces}
    if observed_directions != expected_directions:
        raise EvidenceError(
            f"crafting anchor {anchor.position} visible/connected faces changed"
        )
    if any(len(values) != 1 for values in face_world_lights.values()):
        raise EvidenceError(
            f"crafting anchor {anchor.position} world-derived face light is inconsistent"
        )
    for direction, values in face_world_lights.items():
        blocklight, sunlight = next(iter(values))
        result_faces[direction]["world_light"] = {
            "blocklight_raw_i8": blocklight,
            "sunlight_raw_i8": sunlight,
        }
    return {
        "block_id": crafting.block_id,
        "kind": crafting.kind,
        "formed": crafting.formed,
        "powered": crafting.powered,
        "facing": crafting.facing,
        "spin": crafting.spin,
        "painted_color_ordinal": crafting.painted_color_ordinal,
        "monitor_display_policy": crafting.monitor_display_policy,
        "connections": list(crafting.connections),
        "faces": result_faces,
        "visible_face_count": len(crafting.faces),
        "fullbright_triangle_count": fullbright_triangle_count,
        "triangle_count": len(records),
        "evidence_status": "custom-nonzero-renderer-route-output",
        "validated": True,
    }


def _quantum_quad(
    direction: str,
    bounds_sixteenths: Sequence[float],
) -> tuple[
    tuple[tuple[float, float, float], ...],
    tuple[tuple[float, float], ...],
]:
    """Reproduce AE2 CubeBuilder bounds-mapped quads for quantum cuboids."""
    x1, y1, z1, x2, y2, z2 = (
        float(value) / 16.0 for value in bounds_sixteenths
    )
    positions = {
        "down": ((x1, y1, z2), (x1, y1, z1), (x2, y1, z1), (x2, y1, z2)),
        "up": ((x1, y2, z1), (x1, y2, z2), (x2, y2, z2), (x2, y2, z1)),
        "west": ((x1, y2, z1), (x1, y1, z1), (x1, y1, z2), (x1, y2, z2)),
        "east": ((x2, y2, z2), (x2, y1, z2), (x2, y1, z1), (x2, y2, z1)),
        "north": ((x2, y2, z1), (x2, y1, z1), (x1, y1, z1), (x1, y2, z1)),
        "south": ((x1, y2, z2), (x1, y1, z2), (x2, y1, z2), (x2, y2, z2)),
    }[direction]
    if direction in {"down", "up"}:
        u1, u2, v1, v2 = x1, x2, z1, z2
        uvs = ((u1, v1), (u1, v2), (u2, v2), (u2, v1))
    else:
        v1, v2 = 1.0 - y1, 1.0 - y2
        if direction == "north":
            u1, u2 = 1.0 - x2, 1.0 - x1
        elif direction == "south":
            u1, u2 = x1, x2
        elif direction == "west":
            u1, u2 = z1, z2
        else:
            u1, u2 = 1.0 - z2, 1.0 - z1
        uvs = ((u1, v2), (u1, v1), (u2, v1), (u2, v2))
    return positions, uvs


def _quantum_triangle_key(
    material: str,
    positions: Sequence[tuple[float, float, float]],
    uvs: Sequence[tuple[float, float]],
) -> str:
    vertices = [
        {
            "position_q16": [
                round(component / SHAPE_QUANTUM) for component in position
            ],
            "uv_q16": [round(component / SHAPE_QUANTUM) for component in uv],
        }
        for position, uv in zip(positions, uvs, strict=True)
    ]
    rotations = [vertices[offset:] + vertices[:offset] for offset in range(3)]
    return canonical_json(
        {"material": material, "vertices": min(rotations, key=canonical_json)}
    )


def _validate_quantum_contract(
    anchor: AnchorContract,
    records: Sequence[TriangleRecord],
) -> dict[str, Any]:
    quantum = anchor.quantum
    if quantum is None:
        return {}
    expected: Counter[str] = Counter()
    expected_attributes: dict[
        str, tuple[str, QuantumPrimitiveContract]
    ] = {}
    primitive_results: list[dict[str, Any]] = []
    for primitive in quantum.primitives:
        for direction in DIRECTION_VECTORS:
            corners, uvs = _quantum_quad(direction, primitive.bounds_sixteenths)
            for indexes in ((0, 1, 2), (0, 2, 3)):
                key = _quantum_triangle_key(
                    primitive.resource_path,
                    tuple(corners[index] for index in indexes),
                    tuple(uvs[index] for index in indexes),
                )
                expected[key] += 1
                previous = expected_attributes.setdefault(
                    key, (direction, primitive)
                )
                if previous != (direction, primitive):
                    raise EvidenceError(
                        f"quantum anchor {anchor.position} has ambiguous cuboid geometry"
                    )
        primitive_results.append(
            {
                "role": primitive.role,
                "resource": primitive.resource_path,
                "bounds_sixteenths": list(primitive.bounds_sixteenths),
                "triangle_count": 12,
            }
        )

    actual: Counter[str] = Counter()
    face_lights: dict[str, set[tuple[int, int]]] = {}
    for record in records:
        key = _quantum_triangle_key(
            record.material_identity, record.positions, record.uvs
        )
        actual[key] += 1
        attribute = expected_attributes.get(key)
        if attribute is None:
            continue
        direction, _primitive = attribute
        if any(
            normal != _glass_normal_raw(direction) for normal in record.normals
        ):
            raise EvidenceError(
                f"quantum anchor {anchor.position} has wrong {direction} normals"
            )
        if (
            any(color != (255, 255, 255) for color in record.colors)
            or any(ao != 255 for ao in record.aos)
        ):
            raise EvidenceError(
                f"quantum anchor {anchor.position} has wrong CUTOUT color/AO"
            )
        light_pairs = set(zip(record.blocklights, record.sunlights, strict=True))
        if (
            len(light_pairs) != 1
            or any(
                blocklight not in range(16) or sunlight not in range(16)
                for blocklight, sunlight in light_pairs
            )
        ):
            raise EvidenceError(
                f"quantum anchor {anchor.position} has invalid world-derived face light"
            )
        face_lights.setdefault(direction, set()).update(light_pairs)
    if actual != expected:
        raise EvidenceError(
            f"quantum anchor {anchor.position} cuboid/material/UV/winding changed"
        )
    if set(face_lights) != set(DIRECTION_VECTORS) or any(
        len(values) != 1 for values in face_lights.values()
    ):
        raise EvidenceError(
            f"quantum anchor {anchor.position} world-derived directional light changed"
        )
    return {
        "block_id": quantum.block_id,
        "plane": quantum.plane,
        "role": quantum.role,
        "formed": quantum.formed,
        "waterlogged": quantum.waterlogged,
        "connections": list(quantum.connections),
        "primitives": primitive_results,
        "triangle_count": len(records),
        "render_type": "cutout-binary-alpha",
        "rgb_u8": [255, 255, 255],
        "ambient_occlusion_raw_u8": 255,
        "ambient_occlusion_policy": "client-cable-bus-neighbor-sampling-air-isolated",
        "world_light": {
            direction: {
                "blocklight_raw_i8": next(iter(face_lights[direction]))[0],
                "sunlight_raw_i8": next(iter(face_lights[direction]))[1],
            }
            for direction in DIRECTION_VECTORS
        },
        "world_light_policy": "world-derived-own-and-outward-face-maximum",
        "power_overlay_policy": quantum.power_overlay_policy,
        "particle_policy": quantum.particle_policy,
        "validated": True,
    }


@dataclass(frozen=True)
class _M3fExpectedTriangle:
    material: str
    positions: tuple[tuple[float, float, float], ...]
    uvs: tuple[tuple[float, float], ...]
    rgb: tuple[int, int, int]
    aos: tuple[int, int, int]
    world_face: str
    role: str


def _m3f_f32(value: float) -> float:
    return struct.unpack("<f", struct.pack("<f", value))[0]


def _m3f_add(first: float, second: float) -> float:
    return _m3f_f32(_m3f_f32(first) + _m3f_f32(second))


def _m3f_sub(first: float, second: float) -> float:
    return _m3f_f32(_m3f_f32(first) - _m3f_f32(second))


def _m3f_matrix_point(
    point: tuple[float, float, float],
    matrix: tuple[tuple[float, ...], ...],
) -> tuple[float, float, float]:
    centered = tuple(value - 0.5 for value in point)
    return tuple(
        0.5 + sum(matrix[row][column] * centered[column] for column in range(3))
        for row in range(3)
    )  # type: ignore[return-value]


def _m3f_cardinal_face(
    positions: Sequence[tuple[float, float, float]],
) -> str:
    normal = _cross_normal(positions)
    for direction, vector in DIRECTION_VECTORS.items():
        if all(abs(normal[axis] - vector[axis]) <= GEOMETRY_TOLERANCE for axis in range(3)):
            return direction
    raise EvidenceError("M3f expected geometry is not cardinal")


def _m3f_quad(
    material: str,
    vertices: Sequence[tuple[float, float, float, float, float]],
    *,
    rgb: tuple[int, int, int] = (255, 255, 255),
    aos: tuple[int, int, int, int] = (255, 255, 255, 255),
    matrix: tuple[tuple[float, ...], ...] | None = None,
    role: str,
) -> list[_M3fExpectedTriangle]:
    if len(vertices) != 4:
        raise EvidenceError("M3f expected quad does not have four vertices")
    positions = tuple(
        (vertex[0], vertex[1], vertex[2])
        if matrix is None
        else _m3f_matrix_point((vertex[0], vertex[1], vertex[2]), matrix)
        for vertex in vertices
    )
    uvs = tuple((vertex[3], vertex[4]) for vertex in vertices)
    output: list[_M3fExpectedTriangle] = []
    for indexes in ((0, 1, 2), (0, 2, 3)):
        triangle_positions = tuple(positions[index] for index in indexes)
        output.append(
            _M3fExpectedTriangle(
                material,
                triangle_positions,
                tuple(uvs[index] for index in indexes),
                rgb,
                tuple(aos[index] for index in indexes),
                _m3f_cardinal_face(triangle_positions),
                role,
            )
        )
    return output


def _m3f_model_vertices(
    minimum: tuple[float, float, float],
    maximum: tuple[float, float, float],
    face: str,
    uv_rect: tuple[float, float, float, float],
    rotation: int,
) -> tuple[tuple[float, float, float, float, float], ...]:
    min_x, min_y, min_z = minimum
    max_x, max_y, max_z = maximum
    corners = (
        (min_x, min_y, min_z), (min_x, min_y, max_z),
        (max_x, min_y, min_z), (max_x, min_y, max_z),
        (min_x, max_y, min_z), (min_x, max_y, max_z),
        (max_x, max_y, min_z), (max_x, max_y, max_z),
    )
    face_corners = {
        "down": (0, 2, 3, 1), "up": (5, 7, 6, 4),
        "north": (2, 0, 4, 6), "south": (1, 3, 7, 5),
        "west": (0, 1, 5, 4), "east": (3, 2, 6, 7),
    }[face]
    u1, v1, u2, v2 = (value / 16 for value in uv_rect)
    raw_uvs = ((u1, v2), (u2, v2), (u2, v1), (u1, v1))
    steps = (rotation // 90) % 4
    uvs = tuple(raw_uvs[(steps + index) % 4] for index in range(4))
    return tuple(
        (
            corners[corner][0] / 16,
            corners[corner][1] / 16,
            corners[corner][2] / 16,
            uvs[index][0],
            uvs[index][1],
        )
        for index, corner in enumerate(face_corners)
    )


def _m3f_json_model(
    model: Sequence[tuple[Any, ...]],
    material: str,
    matrix: tuple[tuple[float, ...], ...],
    role_prefix: str,
) -> list[_M3fExpectedTriangle]:
    output: list[_M3fExpectedTriangle] = []
    for minimum, maximum, element_role, face_rows in model:
        faces = {row[0]: row[1:] for row in face_rows}
        for face in M3F_MODEL_FACE_ORDER:
            if face not in faces:
                continue
            uv_rect, rotation = faces[face]
            output.extend(
                _m3f_quad(
                    material,
                    _m3f_model_vertices(minimum, maximum, face, uv_rect, rotation),
                    matrix=matrix,
                    role=f"{role_prefix}:{element_role}:{face}",
                )
            )
    return output


def _m3f_paint_vertices(
    splotch: PaintSplotchContract,
    layer: int,
) -> tuple[tuple[float, float, float, float, float], ...]:
    buffer = _m3f_f32(0.1)
    x = _m3f_f32((splotch.signed_position & 0x0F) / 15.0)
    y = _m3f_f32(((splotch.signed_position >> 4) & 0x0F) / 15.0)
    x = max(buffer, min(_m3f_sub(1.0, buffer), x))
    y = max(buffer, min(_m3f_sub(1.0, buffer), y))
    offset = _m3f_f32(0.001)
    for _ in range(layer):
        offset = _m3f_add(offset, 0.001)
    plane = (
        _m3f_sub(1.0, offset)
        if splotch.backing_side in {"up", "east", "south"}
        else offset
    )
    if splotch.backing_side in {"up", "down"}:
        bounds = (
            _m3f_sub(x, buffer), plane, _m3f_sub(y, buffer),
            _m3f_add(x, buffer), plane, _m3f_add(y, buffer),
        )
    elif splotch.backing_side in {"east", "west"}:
        bounds = (
            plane, _m3f_sub(x, buffer), _m3f_sub(y, buffer),
            plane, _m3f_add(x, buffer), _m3f_add(y, buffer),
        )
    else:
        bounds = (
            _m3f_sub(x, buffer), _m3f_sub(y, buffer), plane,
            _m3f_add(x, buffer), _m3f_add(y, buffer), plane,
        )
    x1, y1, z1, x2, y2, z2 = bounds
    face = splotch.visible_face
    if face == "down":
        left, bottom, right, top, depth = x1, z1, x2, z2, y1
    elif face == "up":
        left, bottom, right, top, depth = (
            x1, _m3f_sub(1.0, z2), x2, _m3f_sub(1.0, z1),
            _m3f_sub(1.0, y2),
        )
    elif face == "north":
        left, bottom, right, top, depth = (
            _m3f_sub(1.0, x2), y1, _m3f_sub(1.0, x1), y2, z1,
        )
    elif face == "south":
        left, bottom, right, top, depth = (
            x1, y1, x2, y2, _m3f_sub(1.0, z2),
        )
    elif face == "west":
        left, bottom, right, top, depth = z1, y1, z2, y2, x1
    else:
        left, bottom, right, top, depth = (
            _m3f_sub(1.0, z2), y1, _m3f_sub(1.0, z1), y2,
            _m3f_sub(1.0, x2),
        )
    if face == "down":
        positions = (
            (left, depth, top), (left, depth, bottom),
            (right, depth, bottom), (right, depth, top),
        )
    elif face == "up":
        depth = _m3f_sub(1.0, depth)
        top = _m3f_sub(1.0, top)
        bottom = _m3f_sub(1.0, bottom)
        positions = (
            (left, depth, top), (left, depth, bottom),
            (right, depth, bottom), (right, depth, top),
        )
    elif face == "west":
        positions = (
            (depth, top, left), (depth, bottom, left),
            (depth, bottom, right), (depth, top, right),
        )
    elif face == "east":
        depth = _m3f_sub(1.0, depth)
        left = _m3f_sub(1.0, left)
        right = _m3f_sub(1.0, right)
        positions = (
            (depth, top, left), (depth, bottom, left),
            (depth, bottom, right), (depth, top, right),
        )
    elif face == "north":
        positions = (
            (_m3f_sub(1.0, left), top, depth),
            (_m3f_sub(1.0, left), bottom, depth),
            (_m3f_sub(1.0, right), bottom, depth),
            (_m3f_sub(1.0, right), top, depth),
        )
    else:
        depth = _m3f_sub(1.0, depth)
        left = _m3f_sub(1.0, left)
        right = _m3f_sub(1.0, right)
        positions = (
            (_m3f_sub(1.0, left), top, depth),
            (_m3f_sub(1.0, left), bottom, depth),
            (_m3f_sub(1.0, right), bottom, depth),
            (_m3f_sub(1.0, right), top, depth),
        )
    uv = (
        ((0.0, 0.0), (0.0, 1.0), (1.0, 1.0), (1.0, 0.0))
        if face in {"down", "up"}
        else ((0.0, 1.0), (0.0, 0.0), (1.0, 0.0), (1.0, 1.0))
    )
    return tuple(
        (
            _m3f_f32(_m3f_f32(position[0] * 16.0) / 16.0),
            _m3f_f32(_m3f_f32(position[1] * 16.0) / 16.0),
            _m3f_f32(_m3f_f32(position[2] * 16.0) / 16.0),
            uv[index][0], uv[index][1],
        )
        for index, position in enumerate(positions)
    )


def _m3f_chest_matrix(facing: str) -> tuple[tuple[float, ...], ...]:
    return {
        "south": ((1.0, 0.0, 0.0), (0.0, 1.0, 0.0), (0.0, 0.0, 1.0)),
        "west": ((0.0, 0.0, -1.0), (0.0, 1.0, 0.0), (1.0, 0.0, 0.0)),
        "north": ((-1.0, 0.0, 0.0), (0.0, 1.0, 0.0), (0.0, 0.0, -1.0)),
        "east": ((0.0, 0.0, 1.0), (0.0, 1.0, 0.0), (-1.0, 0.0, 0.0)),
    }[facing]


def _m3f_chest(
    material: str,
    facing: str,
) -> list[_M3fExpectedTriangle]:
    output: list[_M3fExpectedTriangle] = []
    matrix = _m3f_chest_matrix(facing)
    cubes = (
        ("lid", 0, 0, 1, 10, 1, 14, 5, 14),
        ("lock", 0, 0, 7, 8, 15, 2, 4, 1),
        ("bottom", 0, 19, 1, 0, 1, 14, 10, 14),
    )
    for role, texture_u, texture_v, x, y, z, dx, dy, dz in cubes:
        v0, v1, v2, v3 = (
            (x, y, z), (x + dx, y, z),
            (x + dx, y + dy, z), (x, y + dy, z),
        )
        v4, v5, v6, v7 = (
            (x, y, z + dz), (x + dx, y, z + dz),
            (x + dx, y + dy, z + dz), (x, y + dy, z + dz),
        )
        u0, u1, u2 = texture_u, texture_u + dz, texture_u + dz + dx
        u3 = texture_u + dz + dx + dx
        u4 = texture_u + dz + dx + dz
        u5 = u4 + dx
        vv0, vv1, vv2 = texture_v, texture_v + dz, texture_v + dz + dy
        faces = (
            ("down", (v5, v4, v0, v1), u1, vv0, u2, vv1),
            ("up", (v2, v3, v7, v6), u2, vv1, u3, vv0),
            ("west", (v0, v4, v7, v3), u0, vv1, u1, vv2),
            ("north", (v1, v0, v3, v2), u1, vv1, u2, vv2),
            ("east", (v5, v1, v2, v6), u2, vv1, u4, vv2),
            ("south", (v4, v5, v6, v7), u4, vv1, u5, vv2),
        )
        for face, positions, first_u, first_v, second_u, second_v in faces:
            face_uvs = (
                (second_u, first_v), (first_u, first_v),
                (first_u, second_v), (second_u, second_v),
            )
            vertices = tuple(
                (
                    positions[index][0] / 16,
                    positions[index][1] / 16,
                    positions[index][2] / 16,
                    face_uvs[index][0] / 64,
                    face_uvs[index][1] / 64,
                )
                for index in range(4)
            )
            output.extend(
                _m3f_quad(
                    material, vertices, matrix=matrix, role=f"chest:{role}:{face}"
                )
            )
    return output


def _m3f_inscriber_stamps(
    matrix: tuple[tuple[float, ...], ...],
) -> list[_M3fExpectedTriangle]:
    two = _m3f_f32(2.0 / 16.0)
    high = _m3f_sub(1.0, two)
    top_middle, bottom_middle = _m3f_f32(0.52), _m3f_f32(0.48)
    press, base = _m3f_f32(0.2), _m3f_f32(0.4)
    inner_v = _m3f_f32(0.125)
    outer_v = _m3f_sub(inner_v, _m3f_sub(press, base))

    def vertex(x: float, y: float, z: float, u: float, v: float) -> tuple[float, ...]:
        return (_m3f_f32(x), _m3f_f32(y), _m3f_f32(z), _m3f_f32(u), _m3f_f32(v))

    quads = (
        ("top-down", (
            vertex(two, _m3f_add(top_middle, press), two, .875, .125),
            vertex(high, _m3f_add(top_middle, press), two, .125, .125),
            vertex(high, _m3f_add(top_middle, press), high, .125, .875),
            vertex(two, _m3f_add(top_middle, press), high, .875, .875),
        )),
        ("top-north", (
            vertex(two, _m3f_add(top_middle, base), two, .125, outer_v),
            vertex(high, _m3f_add(top_middle, base), two, .875, outer_v),
            vertex(high, _m3f_add(top_middle, press), two, .875, inner_v),
            vertex(two, _m3f_add(top_middle, press), two, .125, inner_v),
        )),
        ("top-south", (
            vertex(two, _m3f_add(top_middle, base), high, .125, outer_v),
            vertex(two, _m3f_add(top_middle, press), high, .125, inner_v),
            vertex(high, _m3f_add(top_middle, press), high, .875, inner_v),
            vertex(high, _m3f_add(top_middle, base), high, .875, outer_v),
        )),
        ("bottom-up", (
            vertex(high, _m3f_sub(bottom_middle, press), two, .875, .125),
            vertex(two, _m3f_sub(bottom_middle, press), two, .125, .125),
            vertex(two, _m3f_sub(bottom_middle, press), high, .125, .875),
            vertex(high, _m3f_sub(bottom_middle, press), high, .875, .875),
        )),
        ("bottom-north", (
            vertex(high, _m3f_sub(bottom_middle, base), two, .125, outer_v),
            vertex(two, _m3f_sub(bottom_middle, base), two, .875, outer_v),
            vertex(two, _m3f_sub(bottom_middle, press), two, .875, inner_v),
            vertex(high, _m3f_sub(bottom_middle, press), two, .125, inner_v),
        )),
        ("bottom-south", (
            vertex(two, _m3f_sub(bottom_middle, press), high, .875, inner_v),
            vertex(two, _m3f_sub(bottom_middle, base), high, .875, outer_v),
            vertex(high, _m3f_sub(bottom_middle, base), high, .125, outer_v),
            vertex(high, _m3f_sub(bottom_middle, press), high, .125, inner_v),
        )),
    )
    return [
        triangle
        for role, vertices in quads
        for triangle in _m3f_quad(
            "ae2:block/inscriber_inside",
            vertices,
            matrix=matrix,
            role=f"inscriber-stamp:{role}",
        )
    ]


def _m3f_pylon(
    axis: str,
    axis_position: str,
) -> list[_M3fExpectedTriangle]:
    positions = {
        "down": ((0, 0, 1), (0, 0, 0), (1, 0, 0), (1, 0, 1)),
        "up": ((0, 1, 0), (0, 1, 1), (1, 1, 1), (1, 1, 0)),
        "north": ((1, 1, 0), (1, 0, 0), (0, 0, 0), (0, 1, 0)),
        "south": ((0, 1, 1), (0, 0, 1), (1, 0, 1), (1, 1, 1)),
        "west": ((0, 1, 0), (0, 0, 0), (0, 0, 1), (0, 1, 1)),
        "east": ((1, 1, 1), (1, 0, 1), (1, 0, 0), (1, 1, 0)),
    }
    axis_faces = {
        "x": {"west", "east"}, "y": {"down", "up"}, "z": {"north", "south"}
    }[axis]
    output: list[_M3fExpectedTriangle] = []
    for layer in ("outer", "inner"):
        for face in DIRECTION_VECTORS:
            cap = face in axis_faces
            if axis_position == "none":
                suffix = "base" if layer == "outer" else "dim"
            elif layer == "outer":
                suffix = "base" if cap else (
                    "base_spanned" if axis_position == "middle" else "base_end"
                )
            else:
                suffix = "red" if cap else (
                    "red_spanned" if axis_position == "middle" else "red_end"
                )
            rotation = 0
            flip_v = False
            if axis_position != "none":
                if axis == "y":
                    flip_v = axis_position == "end" and face not in {"up", "down"}
                elif axis == "x":
                    rotation = 1 if face in {"north", "south"} else 3 if face in {"up", "down"} else 0
                    if axis_position == "start":
                        flip_v = face in {"up", "down", "north"}
                    elif axis_position == "end":
                        flip_v = face == "south"
                else:
                    rotation = 1 if face in {"west", "east"} else 0
                    if axis_position == "start":
                        flip_v = face in {"up", "east"}
                    elif axis_position == "end":
                        flip_v = face in {"down", "west"}
            v1, v2 = ((0.0, 1.0) if face in {"down", "up"} else (1.0, 0.0))
            if flip_v:
                v1, v2 = v2, v1
            base_uv = (
                ((0.0, v1), (0.0, v2), (1.0, v2), (1.0, v1))
                if face in {"down", "up"}
                else ((0.0, v2), (0.0, v1), (1.0, v1), (1.0, v2))
            )
            rotated_uv: list[tuple[float, float] | None] = [None] * 4
            for index in range(4):
                rotated_uv[(index + 4 - rotation) % 4] = base_uv[index]
            vertices = tuple(
                (*positions[face][index], *rotated_uv[index])  # type: ignore[misc]
                for index in range(4)
            )
            output.extend(
                _m3f_quad(
                    f"ae2:block/spatial_pylon/{suffix}",
                    vertices,
                    role=f"pylon:{axis}:{axis_position}:{layer}:{face}",
                )
            )
    return output


def _m3f_expected_geometry(
    completion: M3CompletionContract,
) -> list[_M3fExpectedTriangle]:
    state = json.loads(completion.block_state_json)
    if completion.block_id == PAINT_BLOCK_ID:
        return [
            triangle
            for layer, splotch in enumerate(completion.paint_splotches)
            for triangle in _m3f_quad(
                splotch.resource,
                _m3f_paint_vertices(splotch, layer),
                rgb=splotch.rgb,
                role=f"paint:{layer}:{splotch.backing_side}",
            )
        ]
    if completion.block_id in {SKY_STONE_CHEST_BLOCK_ID, SMOOTH_SKY_STONE_CHEST_BLOCK_ID}:
        material = (
            "ae2:block/skychest"
            if completion.block_id == SKY_STONE_CHEST_BLOCK_ID
            else "ae2:block/skyblockchest"
        )
        return _m3f_chest(material, state["facing"])
    if completion.block_id == CRANK_BLOCK_ID:
        return _m3f_json_model(
            M3F_CRANK_MODEL,
            "ae2:block/crank",
            _drive_rotation_matrix(state["facing"], 0),
            "crank",
        )
    if completion.block_id == INSCRIBER_BLOCK_ID:
        matrix = _drive_rotation_matrix(state["facing"], state["spin"])
        return [
            *_m3f_json_model(
                M3F_INSCRIBER_MODEL,
                "ae2:block/inscriber",
                matrix,
                "inscriber-shell",
            ),
            *_m3f_inscriber_stamps(matrix),
        ]
    if completion.block_id == SPATIAL_PYLON_BLOCK_ID:
        if completion.pylon_axis is None or completion.pylon_axis_position is None:
            raise EvidenceError("custom M3f pylon has no exact inferred axis role")
        return _m3f_pylon(completion.pylon_axis, completion.pylon_axis_position)
    raise EvidenceError(f"unsupported M3f exact geometry block: {completion.block_id}")


def _m3f_geometry_key(
    material: str,
    positions: Sequence[tuple[float, float, float]],
    uvs: Sequence[tuple[float, float]],
) -> tuple[tuple[Any, ...], int]:
    vertices = tuple(
        (
            *(round(component / SHAPE_QUANTUM) for component in position),
            *(round(component / SHAPE_QUANTUM) for component in uv),
        )
        for position, uv in zip(positions, uvs, strict=True)
    )
    rotations = tuple(vertices[offset:] + vertices[:offset] for offset in range(3))
    offset = min(range(3), key=lambda index: rotations[index])
    return (material, rotations[offset]), offset


def _m3f_rotate_values(values: Sequence[Any], offset: int) -> tuple[Any, ...]:
    return tuple(values[offset:]) + tuple(values[:offset])


def _m3f_prbm_relative_position(
    point: tuple[float, float, float],
    anchor: tuple[int, int, int],
    settings: MapSettings,
) -> tuple[float, float, float]:
    tile = settings.tile_for(anchor[0], anchor[2])
    origin_x, origin_z = settings.tile_origin(*tile)
    stored_coordinates = (
        _m3f_f32(anchor[0] - origin_x + point[0]),
        _m3f_f32(anchor[1] + point[1]),
        _m3f_f32(anchor[2] - origin_z + point[2]),
    )
    return (
        stored_coordinates[0] - (anchor[0] - origin_x),
        stored_coordinates[1] - anchor[1],
        stored_coordinates[2] - (anchor[2] - origin_z),
    )


def _validate_m3_completion_contract(
    anchor: AnchorContract,
    records: Sequence[TriangleRecord],
    settings: MapSettings,
) -> dict[str, Any]:
    completion = anchor.m3_completion
    if completion is None:
        return {}
    if anchor.expected_path != "custom-m3f":
        return {
            "block_id": completion.block_id,
            "static_policy": completion.static_policy,
            "fallback": True,
            "validated": True,
        }

    expected = _m3f_expected_geometry(completion)
    expected_geometry: Counter[tuple[Any, ...]] = Counter()
    expected_attributes: Counter[tuple[Any, ...]] = Counter()
    expected_faces: set[str] = set()
    for triangle in expected:
        prbm_positions = tuple(
            _m3f_prbm_relative_position(point, anchor.position, settings)
            for point in triangle.positions
        )
        key, offset = _m3f_geometry_key(
            triangle.material, prbm_positions, triangle.uvs
        )
        expected_geometry[key] += 1
        normal = _glass_normal_raw(triangle.world_face)
        expected_attributes[
            (
                key,
                _m3f_rotate_values((triangle.rgb,) * 3, offset),
                _m3f_rotate_values(triangle.aos, offset),
                _m3f_rotate_values((normal,) * 3, offset),
            )
        ] += 1
        expected_faces.add(triangle.world_face)

    actual_geometry: Counter[tuple[Any, ...]] = Counter()
    actual_attributes: Counter[tuple[Any, ...]] = Counter()
    face_lights: dict[str, set[tuple[int, int]]] = {}
    for record in records:
        key, offset = _m3f_geometry_key(
            record.material_identity, record.positions, record.uvs
        )
        actual_geometry[key] += 1
        colors = _m3f_rotate_values(record.colors, offset)
        aos = _m3f_rotate_values(record.aos, offset)
        normals = _m3f_rotate_values(record.normals, offset)
        actual_attributes[(key, colors, aos, normals)] += 1
        if len(set(record.normals)) != 1:
            raise EvidenceError(
                f"M3f anchor {anchor.position} has non-flat encoded normals"
            )
        world_face = next(
            (
                direction
                for direction in DIRECTION_VECTORS
                if record.normals[0] == _glass_normal_raw(direction)
            ),
            None,
        )
        if world_face is None:
            raise EvidenceError(
                f"M3f anchor {anchor.position} has non-cardinal encoded normals"
            )
        if (
            len(set(record.blocklights)) != 1
            or len(set(record.sunlights)) != 1
            or not 0 <= record.blocklights[0] <= 15
            or not 0 <= record.sunlights[0] <= 15
        ):
            raise EvidenceError(
                f"M3f anchor {anchor.position} has invalid per-triangle world light"
            )
        face_lights.setdefault(world_face, set()).add(
            (record.blocklights[0], record.sunlights[0])
        )

    if actual_geometry != expected_geometry:
        raise EvidenceError(
            f"M3f anchor {anchor.position} exact q16 geometry/winding/material/UV changed"
        )
    if actual_attributes != expected_attributes:
        raise EvidenceError(
            f"M3f anchor {anchor.position} exact normals/tint/AO changed"
        )
    if set(face_lights) != expected_faces or any(
        len(values) != 1 for values in face_lights.values()
    ):
        raise EvidenceError(
            f"M3f anchor {anchor.position} face-consistent world light changed"
        )

    geometry_rows = [canonical_json(key) for key in expected_geometry.elements()]
    return {
        "block_id": completion.block_id,
        "block_state": json.loads(completion.block_state_json),
        "static_policy": completion.static_policy,
        "paint_splotch_count": len(completion.paint_splotches),
        "pylon_axis": completion.pylon_axis,
        "pylon_axis_position": completion.pylon_axis_position,
        "triangle_count": len(records),
        "geometry_quantum": "2^-16-block-and-uv",
        "geometry_q16_signature": sha256_text(
            "m3f-exact-q16-geometry-v9\n" + "\n".join(sorted(geometry_rows)) + "\n"
        ),
        "ambient_occlusion_policy": (
            "host-neighbor-sampled-shell-plus-forced-neutral-stamps"
            if completion.block_id == INSCRIBER_BLOCK_ID
            else "host-neighbor-sampled-resource-model"
            if completion.block_id == CRANK_BLOCK_ID
            else "forced-raw-255-primitives"
        ),
        "world_light": {
            face: {
                "blocklight_raw_i8": next(iter(face_lights[face]))[0],
                "sunlight_raw_i8": next(iter(face_lights[face]))[1],
            }
            for face in sorted(face_lights)
        },
        "world_light_policy": "world-derived-own-and-outward-face-maximum",
        "validated": True,
    }


def _near(value: float, expected: float, tolerance: float = GEOMETRY_TOLERANCE) -> bool:
    return abs(value - expected) <= tolerance


def _record_direction(record: TriangleRecord) -> str:
    components = tuple(
        sum(normal[axis] for normal in record.normals) for axis in range(3)
    )
    axis = max(range(3), key=lambda index: abs(components[index]))
    if components[axis] == 0:
        raise EvidenceError("native structural triangle has no dominant normal")
    return (
        ("west", "east"),
        ("down", "up"),
        ("north", "south"),
    )[axis][components[axis] > 0]


def _native_structural_nonlighting_signature(
    records: Sequence[TriangleRecord],
    signature_scope: str,
) -> str:
    rows = [
        canonical_json(
            {
                # The canonical geometry row already retains resolved material,
                # positions, winding and UVs.  Keep it byte-exact rather than
                # reconstructing those fields from decoded floating-point data.
                "geometry": record.geometry,
                "normal_raw_i8": [list(normal) for normal in record.normals],
                "color_raw_u8": [list(color) for color in record.colors],
                "ao_raw_u8": list(record.aos),
            }
        )
        for record in records
    ]
    return sha256_text(
        "native-structural-nonlighting-attributes-v10\n"
        + signature_scope
        + "\n"
        + "\n".join(sorted(rows))
        + "\n"
    )


def _validate_native_structural_light_contract(
    records: Sequence[TriangleRecord],
    description: str,
) -> dict[str, int]:
    if not records:
        raise EvidenceError(f"{description} has no source-parity triangles")
    forced_fullbright_triangle_count = 0
    for record in records:
        for channel, values in (
            ("blocklight", record.blocklights),
            ("sunlight", record.sunlights),
        ):
            if len(values) != 3:
                raise EvidenceError(
                    f"{description} {channel} must contain exactly three triangle values"
                )
            if any(
                not isinstance(value, int)
                or isinstance(value, bool)
                or value < 0
                or value > 15
                for value in values
            ):
                raise EvidenceError(
                    f"{description} {channel} is outside the source-derived [0, 15] bound"
                )
            if len(set(values)) != 1:
                raise EvidenceError(
                    f"{description} {channel} is not flat within one triangle"
                )
        if record.material_identity in NATIVE_STRUCTURAL_FORCED_FULLBRIGHT_RESOURCES:
            forced_fullbright_triangle_count += 1
            if record.blocklights != (15, 15, 15) or record.sunlights != (15, 15, 15):
                raise EvidenceError(
                    f"{description} forced-fullbright channel overlay is not exact 15/15"
                )
        elif record.material_identity.endswith(("/channels_00", "/channels_10")):
            raise EvidenceError(
                f"{description} has an unsupported channel-overlay light contract"
            )
    return {
        "triangle_count": len(records),
        "forced_fullbright_triangle_count": forced_fullbright_triangle_count,
    }


def _assert_native_world_attributes(
    records: Sequence[TriangleRecord],
    description: str,
    *,
    require_air_isolated_ao: bool = True,
) -> dict[str, int]:
    light_contract = _validate_native_structural_light_contract(
        records, description
    )
    if require_air_isolated_ao:
        for record in records:
            if any(ao != 255 for ao in record.aos):
                raise EvidenceError(f"{description} air-isolated AO changed")
    return light_contract


def _assert_native_anchor_world_attributes(
    anchor: AnchorContract,
    records: Sequence[TriangleRecord],
    description: str,
) -> dict[str, int]:
    legacy_by_position = {
        position: case_id
        for case_id, position in NATIVE_STRUCTURAL_LEGACY_SELECTORS
    }
    legacy_case_id = legacy_by_position.get(anchor.position)
    if legacy_case_id is not None and anchor.case_id != legacy_case_id:
        raise EvidenceError(
            f"native legacy AO selector mismatch at {anchor.position}"
        )
    return _assert_native_world_attributes(
        records,
        description,
        require_air_isolated_ao=legacy_case_id is None,
    )


_NATIVE_PLANE_VISUAL_LOCAL_BOUND_BITS = (4, 1, 2, 8)
_NATIVE_PLANE_VISUAL_LOCAL_AXES = {
    "down": ("east", "north", "up"),
    "up": ("west", "north", "down"),
    "north": ("east", "up", "south"),
    "south": ("west", "up", "north"),
    "west": ("north", "up", "east"),
    "east": ("south", "up", "west"),
}
_NATIVE_PLANE_COLLISION_LOCAL_BOUND_BITS = {
    "down": (4, 1, 2, 8),
    "up": (1, 4, 8, 2),
    "north": (1, 4, 2, 8),
    "south": (1, 4, 2, 8),
    "west": (4, 1, 2, 8),
    "east": (4, 1, 2, 8),
}
_NATIVE_PLANE_COLLISION_LOCAL_AXES = {
    "down": ("east", "north", "down"),
    "up": ("east", "south", "up"),
    "north": ("west", "up", "north"),
    "south": ("east", "up", "south"),
    "west": ("north", "up", "west"),
    "east": ("south", "up", "east"),
}


def _native_plane_local_bounds(
    mask: int,
    bound_bits: tuple[int, int, int, int],
    z_bounds: tuple[float, float],
) -> tuple[tuple[float, float, float], tuple[float, float, float]]:
    if mask < 0 or mask > 15:
        raise EvidenceError(f"native plane mask is outside [0, 15]: {mask}")
    min_x_bit, max_x_bit, min_y_bit, max_y_bit = bound_bits
    unit = 1.0 / 16.0
    return (
        (
            0.0 if mask & min_x_bit else unit,
            0.0 if mask & min_y_bit else unit,
            z_bounds[0],
        ),
        (
            1.0 if mask & max_x_bit else 15.0 * unit,
            1.0 if mask & max_y_bit else 15.0 * unit,
            z_bounds[1],
        ),
    )


def _native_plane_orient_bounds(
    local_bounds: tuple[
        tuple[float, float, float], tuple[float, float, float]
    ],
    local_axes: tuple[str, str, str],
) -> tuple[tuple[float, float, float], tuple[float, float, float]]:
    """Apply one exact signed-permutation part transform to an AABB."""
    world_min = [0.0, 0.0, 0.0]
    world_max = [0.0, 0.0, 0.0]
    for local_axis, direction in enumerate(local_axes):
        vector = DIRECTION_VECTORS[direction]
        world_axis = next(
            axis for axis, component in enumerate(vector) if component
        )
        if vector[world_axis] > 0:
            world_min[world_axis] = local_bounds[0][local_axis]
            world_max[world_axis] = local_bounds[1][local_axis]
        else:
            world_min[world_axis] = 1.0 - local_bounds[1][local_axis]
            world_max[world_axis] = 1.0 - local_bounds[0][local_axis]
    return tuple(world_min), tuple(world_max)  # type: ignore[return-value]


def _native_plane_visual_expected_bounds(
    direction: str, mask: int
) -> tuple[tuple[float, float, float], tuple[float, float, float]]:
    """PlaneBakedModel local bits after the exact installed-face transform."""
    axes = _NATIVE_PLANE_VISUAL_LOCAL_AXES.get(direction)
    if axes is None:
        raise EvidenceError(f"unsupported S1 plane golden direction: {direction}")
    local = _native_plane_local_bounds(
        mask,
        _NATIVE_PLANE_VISUAL_LOCAL_BOUND_BITS,
        (0.0, 1.0 / 16.0),
    )
    return _native_plane_orient_bounds(local, axes)


def _native_plane_facade_cutout_expected_bounds(
    direction: str, mask: int
) -> tuple[tuple[float, float, float], tuple[float, float, float]]:
    """Source-derived PlaneConnectionHelper collision box in world axes.

    This remains independent from PlaneBakedModel. BusCollisionHelper's local
    axes vary by installed face, so the logical bit-to-local-bound table is
    face-aware even though the final world bounds coincide with the visual
    sheet for every exact orientation.
    """
    bound_bits = _NATIVE_PLANE_COLLISION_LOCAL_BOUND_BITS.get(direction)
    axes = _NATIVE_PLANE_COLLISION_LOCAL_AXES.get(direction)
    if bound_bits is None or axes is None:
        raise EvidenceError(f"unsupported S1 plane facade golden direction: {direction}")
    local = _native_plane_local_bounds(
        mask,
        bound_bits,
        (15.0 / 16.0, 1.0),
    )
    return _native_plane_orient_bounds(local, axes)


def _validate_native_plane_source(
    anchor: AnchorContract,
    records: Sequence[TriangleRecord],
    part: NativeStructuralPartContract,
) -> dict[str, Any]:
    contract = anchor.native_structural
    assert contract is not None and contract.plane_mask is not None
    part_name = part.part_id.removeprefix("ae2:")
    front = f"ae2:part/{part_name}"
    expected_materials = {
        front: 2,
        "ae2:part/plane_sides": 8,
        # Two built-in backs plus two transition-chassis backs.
        "ae2:part/transition_plane_back": 4,
    }
    plane_records = [
        record for record in records if record.material_identity in expected_materials
    ]
    if Counter(record.material_identity for record in plane_records) != Counter(
        expected_materials
    ):
        raise EvidenceError(
            f"native plane {anchor.position} source material/triangle closure changed"
        )
    normal = DIRECTION_VECTORS[part.direction]
    fixed_axis = next(
        axis for axis, component in enumerate(normal) if component
    )
    outward_coordinate = 1.0 if normal[fixed_axis] > 0 else 0.0
    built_in_back_coordinate = (
        15.0 / 16.0 if normal[fixed_axis] > 0 else 1.0 / 16.0
    )
    chassis_back_coordinate = (
        14.0 / 16.0 if normal[fixed_axis] > 0 else 2.0 / 16.0
    )
    front_records = [
        record for record in plane_records if record.material_identity == front
    ]
    side_records = [
        record
        for record in plane_records
        if record.material_identity == "ae2:part/plane_sides"
    ]
    back_records = [
        record
        for record in plane_records
        if record.material_identity == "ae2:part/transition_plane_back"
    ]

    def fixed_at(
        record: TriangleRecord, coordinate: float
    ) -> bool:
        return all(
            _near(position[fixed_axis], coordinate)
            for position in record.positions
        )

    built_in_back_records = [
        record
        for record in back_records
        if fixed_at(record, built_in_back_coordinate)
    ]
    chassis_back_records = [
        record
        for record in back_records
        if fixed_at(record, chassis_back_coordinate)
    ]
    if (
        len(front_records) != 2
        or any(not fixed_at(record, outward_coordinate) for record in front_records)
        or len(side_records) != 8
        or len(built_in_back_records) != 2
        or len(chassis_back_records) != 2
        or set(map(id, built_in_back_records)).intersection(
            map(id, chassis_back_records)
        )
    ):
        raise EvidenceError(
            f"native plane {anchor.position} built-in/chassis layer geometry changed"
        )
    visual_records = front_records + side_records + built_in_back_records
    expected_min, expected_max = _native_plane_visual_expected_bounds(
        part.direction, contract.plane_mask
    )
    for axis in range(3):
        values = [
            position[axis]
            for record in visual_records
            for position in record.positions
        ]
        if not _near(min(values), expected_min[axis]) or not _near(
            max(values), expected_max[axis]
        ):
            raise EvidenceError(
                f"native plane {anchor.position} mask-derived visual geometry changed"
            )
    if any(
        any(color != (255, 255, 255) for color in record.colors)
        for record in plane_records
    ):
        raise EvidenceError(f"native plane {anchor.position} neutral color changed")
    _assert_native_anchor_world_attributes(
        anchor, plane_records, f"native plane {anchor.position}"
    )

    # Every plane-mask representative carries a same-face opaque facade.  For
    # the ordinary one-face rows, the projected slab ring may contain only the
    # outer block edge and the source collision-box edge expanded by the four
    # PlaneConnectionHelper bits.  The mask-8 UP row additionally exercises a
    # perpendicular NORTH facade and is validated by its explicit edge reach.
    stone_records = [
        record
        for record in records
        if record.material_identity == "minecraft:block/stone"
    ]
    glass_records = [
        record
        for record in records
        if record.material_identity == NATIVE_STRUCTURAL_GLASSENTIAL_MATERIAL
    ]
    facade_records = stone_records + glass_records
    if not facade_records:
        raise EvidenceError(f"native plane {anchor.position} facade is missing")
    facade_directions = {facade.direction for facade in anchor.facades}
    cutout_min, cutout_max = _native_plane_facade_cutout_expected_bounds(
        part.direction, contract.plane_mask
    )
    if len(facade_directions) == 1:
        if part.direction == "up":
            projected_axes = (0, 2)
            expected_values = (
                {0.0, 1.0, cutout_min[0], cutout_max[0]},
                {0.0, 1.0, cutout_min[2], cutout_max[2]},
            )
        else:
            projected_axes = (0, 1)
            expected_values = (
                {0.0, 1.0, cutout_min[0], cutout_max[0]},
                {0.0, 1.0, cutout_min[1], cutout_max[1]},
            )
        for projected_axis, allowed in zip(
            projected_axes, expected_values, strict=True
        ):
            actual = {
                value
                for record in stone_records
                for position in record.positions
                for value in (position[projected_axis],)
            }
            if any(not any(_near(value, expected) for expected in allowed) for value in actual):
                raise EvidenceError(
                    f"native plane {anchor.position} facade hole is not mask-derived"
                )
            for expected in {cutout_min[projected_axis], cutout_max[projected_axis]}:
                if not any(_near(value, expected) for value in actual):
                    raise EvidenceError(
                        f"native plane {anchor.position} facade hole edge is missing"
                    )
    else:
        # FacadeBuilder first insets this transparent UP facade away from the
        # opaque NORTH facade (z=t..1). The plane collision hole reaches z=0,
        # so getBoxes constructs the boundary strip with reversed z endpoints
        # (t..0). Minecraft AABB normalizes them to 0..t; dropping that strip
        # or clamping it to zero is a source-parity failure.
        normalized_boundary_strip = any(
            _near(min(position[2] for position in record.positions), 0.0)
            and _near(
                max(position[2] for position in record.positions),
                NATIVE_STRUCTURAL_FACADE_THICKNESS,
            )
            and min(position[0] for position in record.positions)
            >= 1.0 / 16.0 - GEOMETRY_TOLERANCE
            and max(position[0] for position in record.positions)
            <= 15.0 / 16.0 + GEOMETRY_TOLERANCE
            for record in glass_records
        )
        if (
            part.direction != "up"
            or contract.plane_mask != 8
            or facade_directions != {"up", "north"}
            or not glass_records
            or not stone_records
            or not normalized_boundary_strip
            or not any(
                _near(position[2], 0.0)
                and _near(position[1], 15.0 / 16.0)
                for record in stone_records
                for position in record.positions
            )
        ):
            raise EvidenceError(
                f"native plane {anchor.position} perpendicular transparent facade AABB normalization changed"
            )
        _assert_native_anchor_world_attributes(
            anchor,
            facade_records,
            f"native plane {anchor.position} facade boundary normalization",
        )
    result = {
        "kind": "plane",
        "mask": contract.plane_mask,
        "visual_bounds": [list(expected_min), list(expected_max)],
        "facade_cutout_bounds": [list(cutout_min), list(cutout_max)],
        "triangle_count": len(plane_records),
        "ao_raw_u8": 255,
        "emissive": False,
        "light_policy": "world-derived-own-and-outward-face-maximum",
        "light_validation": NATIVE_STRUCTURAL_LIGHT_VALIDATION,
    }
    if len(facade_directions) > 1:
        result["transparent_boundary_aabb_normalization"] = {
            "axis": "z",
            "normalized_strip_bounds": [
                0.0,
                NATIVE_STRUCTURAL_FACADE_THICKNESS,
            ],
            "source_constructor": "minecraft-aabb-min-max-normalizes-reversed-endpoints",
        }
    return result


_P2P_BLACK_VARIANTS = (
    0xB4B4B4, 0x7E7E7E, 0x4F4F4F, 0x131313,
    0x4EC04E, 0xFFCF40, 0xD9782F, 0x6E4A12,
    0xAA212B, 0xD86EAA, 0xC15189, 0x6E5CB8,
    0x337FF0, 0x69B9FF, 0x22B0AE, 0x079B6B,
)


def _inactive_p2p_rgb(index: int) -> tuple[int, int, int]:
    rgb = _P2P_BLACK_VARIANTS[index]
    # CubeBuilder casts the positive 0.3-scaled channel to int before
    # PRBMWriter stores it as an unsigned byte.  Java's cast truncates; Python
    # round() would produce source-inexact bytes for half of the 0x1234 lane.
    return tuple(
        int(((rgb >> shift) & 0xFF) * 0.3)
        for shift in (16, 8, 0)
    )


def _validate_native_p2p_source(
    anchor: AnchorContract,
    records: Sequence[TriangleRecord],
) -> dict[str, Any]:
    contract = anchor.native_structural
    assert contract is not None and contract.p2p_frequency is not None
    frequency_records = [
        record
        for record in records
        if record.material_identity == "ae2:part/p2p_tunnel_frequency"
    ]
    if len(frequency_records) != 192:
        raise EvidenceError(
            f"native P2P {anchor.position} must contain sixteen one-pixel cubes"
        )
    coordinate_sets = []
    for axis in range(3):
        coordinate_sets.append(
            {
                round(position[axis] * 16)
                for record in frequency_records
                for position in record.positions
            }
        )
    pixel_axes = sum(
        values == {3, 4, 5, 11, 12, 13} for values in coordinate_sets
    )
    depth_axes = sum(
        values in ({2, 3}, {13, 14}) for values in coordinate_sets
    )
    if pixel_axes != 2 or depth_axes != 1:
        raise EvidenceError(
            f"native P2P {anchor.position} source pixel geometry changed"
        )
    nibbles = tuple(
        (contract.p2p_frequency >> (4 * (3 - index))) & 0xF
        for index in range(4)
    )
    expected_colors = Counter(
        {_inactive_p2p_rgb(nibble): 48 * nibbles.count(nibble) for nibble in set(nibbles)}
    )
    actual_colors = Counter()
    for record in frequency_records:
        if len(set(record.colors)) != 1:
            raise EvidenceError(f"native P2P {anchor.position} pixel tint is not flat")
        actual_colors[record.colors[0]] += 1
    if actual_colors != expected_colors:
        raise EvidenceError(
            f"native P2P {anchor.position} unsigned-frequency nibble colors changed"
        )
    _assert_native_anchor_world_attributes(
        anchor, frequency_records, f"native P2P {anchor.position}"
    )
    return {
        "kind": "p2p-frequency",
        "frequency_unsigned": contract.p2p_frequency,
        "nibbles": list(nibbles),
        "triangle_count": 192,
        "ao_raw_u8": 255,
        "emissive": False,
        "light_policy": "world-derived-own-and-outward-face-maximum",
        "light_validation": NATIVE_STRUCTURAL_LIGHT_VALIDATION,
    }


def _native_corner_kicker_matches(
    vertex: tuple[float, float, float],
    corner: tuple[float, float, float],
) -> bool:
    """Exact QuadCornerKicker three-plane tolerance predicate."""
    return all(
        actual == expected
        or abs(actual - expected) < NATIVE_STRUCTURAL_CORNER_KICKER_EPSILON
        for actual, expected in zip(vertex, corner, strict=True)
    )


def _native_face_stripper_matches_exact_bound(
    coordinates: Sequence[float], bound: float
) -> bool:
    """Exact QuadFaceStripper post-clamp equality (deliberately no epsilon)."""
    return len(coordinates) == 4 and all(value == bound for value in coordinates)


def _native_cable_anchor_component_bounds(
    direction: str,
    *,
    short: bool,
) -> tuple[tuple[float, float, float], tuple[float, float, float]]:
    """Source-derived cable-anchor/stilt box after installed-face rotation."""
    normal = DIRECTION_VECTORS[direction]
    minimum = [7.0 / 16.0] * 3
    maximum = [9.0 / 16.0] * 3
    axis = next(index for index, component in enumerate(normal) if component)
    if normal[axis] < 0:
        minimum[axis] = 1.0 / 16.0 if short else 0.0
        maximum[axis] = 6.0 / 16.0
    else:
        minimum[axis] = 10.0 / 16.0
        maximum[axis] = 15.0 / 16.0 if short else 1.0
    return tuple(minimum), tuple(maximum)  # type: ignore[return-value]


def _validate_native_cable_anchor_components(
    anchor: AnchorContract,
    records: Sequence[TriangleRecord],
    part_by_direction: dict[str, FacePartContract],
) -> tuple[dict[str, Any], ...]:
    """Separate installed anchors from same-material facade-only stilts."""
    expectations: list[tuple[str, str, bool]] = []
    facade_directions = {facade.direction for facade in anchor.facades}
    for direction, part in part_by_direction.items():
        if part.part_id == "ae2:cable_anchor":
            expectations.append(
                ("installed-cable-anchor", direction, direction in facade_directions)
            )
    for direction in facade_directions - set(part_by_direction):
        expectations.append(("facade-only-short-stilt", direction, True))

    anchor_records = [
        record
        for record in records
        if record.material_identity == "ae2:part/cable_anchor"
    ]
    has_declared_anchor_part = any(
        part.part_id == "ae2:cable_anchor"
        for part in part_by_direction.values()
    )
    if (
        not anchor_records
        and not has_declared_anchor_part
        and len(facade_directions) < len(DIRECTION_VECTORS)
        and not isinstance(anchor, AnchorContract)
    ):
        # Narrow manual material/UV/corner goldens intentionally omit
        # unrelated stilt output. Full runtime records never take this path.
        return ()
    if not expectations:
        if anchor_records:
            raise EvidenceError(
                f"native facade {anchor.position} has an undeclared cable-anchor component"
            )
        return ()

    matched_record_ids: set[int] = set()
    components: list[dict[str, Any]] = []
    for role, direction, short in expectations:
        expected_min, expected_max = _native_cable_anchor_component_bounds(
            direction, short=short
        )
        component_records = [
            record
            for record in anchor_records
            if all(
                expected_min[axis] - GEOMETRY_TOLERANCE
                <= position[axis]
                <= expected_max[axis] + GEOMETRY_TOLERANCE
                for position in record.positions
                for axis in range(3)
            )
        ]
        if len(component_records) != 12 or any(
            id(record) in matched_record_ids for record in component_records
        ):
            raise EvidenceError(
                f"native facade {anchor.position} {role} triangle closure changed"
            )
        observed_min = tuple(
            min(
                position[axis]
                for record in component_records
                for position in record.positions
            )
            for axis in range(3)
        )
        observed_max = tuple(
            max(
                position[axis]
                for record in component_records
                for position in record.positions
            )
            for axis in range(3)
        )
        if any(
            not _near(observed_min[axis], expected_min[axis])
            or not _near(observed_max[axis], expected_max[axis])
            for axis in range(3)
        ):
            raise EvidenceError(
                f"native facade {anchor.position} {role} bounds changed"
            )
        matched_record_ids.update(id(record) for record in component_records)
        components.append(
            {
                "role": role,
                "direction": direction,
                "short": short,
                "triangle_count": 12,
                "bounds": [list(expected_min), list(expected_max)],
            }
        )
    if len(matched_record_ids) != len(anchor_records):
        raise EvidenceError(
            f"native facade {anchor.position} cable-anchor component leakage changed"
        )
    return tuple(components)


def _validate_native_facade_source(
    anchor: AnchorContract,
    records: Sequence[TriangleRecord],
) -> list[dict[str, Any]]:
    facts: list[dict[str, Any]] = []
    if not anchor.facades:
        return facts
    facade_states = {
        facade.direction: json.loads(facade.block_state_json)
        for facade in anchor.facades
    }
    fully_surrounded_quartz = set(facade_states) == set(DIRECTION_VECTORS) and all(
        state == {"Name": "ae2:quartz_glass"}
        for state in facade_states.values()
    )
    part_by_direction = {part.direction: part for part in anchor.face_parts}
    anchor_components = _validate_native_cable_anchor_components(
        anchor, records, part_by_direction
    )
    cutout_goldens = {
        "ae2:quartz_fiber": (6, 6, 10, 10),
        "ae2:toggle_bus": (6, 6, 10, 10),
        "ae2:import_bus": (4, 4, 12, 12),
        "ae2:export_bus": (6, 6, 10, 10),
        "ae2:level_emitter": (7, 7, 9, 9),
        "ae2:terminal": (2, 2, 14, 14),
    }
    for direction, part in part_by_direction.items():
        if (
            part.part_id == "ae2:cable_anchor"
            and any(facade.direction == direction for facade in anchor.facades)
        ):
            stone_records = [
                record
                for record in records
                if record.material_identity == "minecraft:block/stone"
            ]
            component = next(
                (
                    item
                    for item in anchor_components
                    if item["role"] == "installed-cable-anchor"
                    and item["direction"] == direction
                ),
                None,
            )
            if (
                len(stone_records) != 12
                or component is None
                or component["short"] is not True
                or component["triangle_count"] != 12
            ):
                raise EvidenceError(
                    f"native cable-anchor facade {anchor.position} must use a full slab and short anchor"
                )
            facts.append(
                {
                    "kind": "same-face-short-anchor-no-cutout",
                    "facade_triangle_count": 12,
                    "anchor_triangle_count": 12,
                }
            )
    for facade in anchor.facades:
        part = part_by_direction.get(facade.direction)
        expected_cutout = cutout_goldens.get(part.part_id) if part is not None else None
        state = json.loads(facade.block_state_json)
        if expected_cutout is not None:
            normal = DIRECTION_VECTORS[facade.direction]
            fixed_axis = next(
                axis for axis, component in enumerate(normal) if component
            )
            fixed_min, fixed_max = (
                (0.0, NATIVE_STRUCTURAL_FACADE_THICKNESS)
                if normal[fixed_axis] < 0
                else (1.0 - NATIVE_STRUCTURAL_FACADE_THICKNESS, 1.0)
            )
            facade_materials = {
                "minecraft:stone": {"minecraft:block/stone"},
                "minecraft:glass": {NATIVE_STRUCTURAL_GLASSENTIAL_MATERIAL},
                "minecraft:oak_log": {
                    "minecraft:block/oak_log",
                    "minecraft:block/oak_log_top",
                },
            }.get(state.get("Name"))
            if facade_materials is None:
                raise EvidenceError(
                    f"native part facade {anchor.position} lacks a source material golden"
                )
            facade_records = [
                record
                for record in records
                if record.material_identity in facade_materials
                and all(
                    fixed_min - GEOMETRY_TOLERANCE
                    <= position[fixed_axis]
                    <= fixed_max + GEOMETRY_TOLERANCE
                    for position in record.positions
                )
            ]
            if len(facade_records) != 48:
                raise EvidenceError(
                    f"native part facade {anchor.position} source ring closure changed"
                )
            fixed_values = [
                position[fixed_axis]
                for record in facade_records
                for position in record.positions
            ]
            if (
                min(fixed_values) < fixed_min - GEOMETRY_TOLERANCE
                or max(fixed_values) > fixed_max + GEOMETRY_TOLERANCE
            ):
                raise EvidenceError(
                    f"native part facade {anchor.position} is on the wrong installed face"
                )
            projected_axes = [axis for axis in range(3) if axis != fixed_axis]
            allowed_by_axis = (
                {
                    0.0,
                    1.0,
                    expected_cutout[0] / 16,
                    expected_cutout[2] / 16,
                },
                {
                    0.0,
                    1.0,
                    expected_cutout[1] / 16,
                    expected_cutout[3] / 16,
                },
            )
            for axis, allowed in zip(
                projected_axes, allowed_by_axis, strict=True
            ):
                actual = {
                    position[axis]
                    for record in facade_records
                    for position in record.positions
                }
                if any(
                    not any(_near(value, expected) for expected in allowed)
                    for value in actual
                ) or any(
                    not any(_near(value, expected) for value in actual)
                    for expected in allowed
                ):
                    raise EvidenceError(
                        f"native part facade {anchor.position} collision-box union changed"
                    )
            _assert_native_anchor_world_attributes(
                anchor, facade_records, f"native part facade {anchor.position}"
            )
            facts.append(
                {
                    "kind": "part-facade-cutout",
                    "part_id": part.part_id,
                    "block_id": state["Name"],
                    "cutout_sixteenths": list(expected_cutout),
                    "triangle_count": 48,
                }
            )

        if state.get("Name") == "minecraft:oak_log" and expected_cutout is None:
            axis = state.get("Properties", {}).get("axis")
            log_records = [
                record
                for record in records
                if record.material_identity
                in {"minecraft:block/oak_log", "minecraft:block/oak_log_top"}
            ]
            top_records = [
                record
                for record in log_records
                if record.material_identity == "minecraft:block/oak_log_top"
            ]
            if len(log_records) != 12 or len(top_records) != 4:
                raise EvidenceError(
                    f"native oak-log facade {anchor.position} no-cull/material projection changed"
                )
            expected_normals = {
                "x": {"west", "east"},
                "y": {"down", "up"},
                "z": {"north", "south"},
            }.get(axis)
            if expected_normals is None or {
                _record_direction(record) for record in top_records
            } != expected_normals:
                raise EvidenceError(
                    f"native oak-log facade {anchor.position} axis transform changed"
                )
            uv_values = [uv for record in top_records for uv in record.uvs]
            spans = sorted(
                (
                    max(uv[index] for uv in uv_values)
                    - min(uv[index] for uv in uv_values)
                    for index in range(2)
                )
            )
            expected_spans = (
                [NATIVE_STRUCTURAL_FACADE_THICKNESS, 1.0]
                if axis == "x"
                else [1.0, 1.0]
            )
            if any(
                not _near(actual, expected)
                for actual, expected in zip(spans, expected_spans, strict=True)
            ):
                raise EvidenceError(
                    f"native oak-log facade {anchor.position} reinterpolated UV changed"
                )
            _assert_native_anchor_world_attributes(
                anchor, log_records, f"native oak-log facade {anchor.position}"
            )
            facts.append(
                {
                    "kind": "stateful-oak-log-facade",
                    "axis": axis,
                    "top_triangle_count": len(top_records),
                    "uv_spans": spans,
                }
            )

        if (
            state.get("Name") == "minecraft:glass"
            and anchor.case_id == "ae2-s1-20"
        ):
            glass_records = [
                record
                for record in records
                if record.material_identity == NATIVE_STRUCTURAL_GLASSENTIAL_MATERIAL
            ]
            adjacent_cull_control = facade.direction == "up"
            expected_count = 10 if adjacent_cull_control else 12
            if len(glass_records) != expected_count:
                raise EvidenceError(
                    f"native glass facade {anchor.position} skipRendering projection changed"
                )
            if adjacent_cull_control and any(
                _record_direction(record) == "up" for record in glass_records
            ):
                raise EvidenceError(
                    f"native glass facade {anchor.position} retained a culled outward face"
                )
            _assert_native_anchor_world_attributes(
                anchor, glass_records, f"native glass facade {anchor.position}"
            )
            facts.append(
                {
                    "kind": (
                        "same-state-glass-skip-rendering"
                        if adjacent_cull_control
                        else "transparent-glass-facade"
                    ),
                    "triangle_count": expected_count,
                }
            )

        if state.get("Name") in CONNECTED_GLASS_BLOCK_IDS:
            quartz_records = [
                record
                for record in records
                if record.material_identity in CONNECTED_GLASS_SELECTED_RESOURCES
            ]
            if not fully_surrounded_quartz:
                _assert_native_anchor_world_attributes(
                    anchor,
                    quartz_records,
                    f"native quartz facade {anchor.position}",
                )
                facts.append(
                    {
                        "kind": "native-connected-quartz-facade",
                        "block_id": state["Name"],
                        "non_emissive": True,
                        "source_layer_light_emission": 0,
                        "light_policy": (
                            "world-derived-own-and-outward-face-maximum-with-"
                            "source-emission-floor-0"
                        ),
                        "light_validation": NATIVE_STRUCTURAL_LIGHT_VALIDATION,
                    }
                )

    if fully_surrounded_quartz:
        quartz_records = [
            record
            for record in records
            if record.material_identity in CONNECTED_GLASS_SELECTED_RESOURCES
        ]
        stilt_records = [
            record
            for record in records
            if record.material_identity == "ae2:part/cable_anchor"
        ]
        cable_records = [
            record
            for record in records
            if record.material_identity.startswith("ae2:part/cable/")
        ]
        stilt_components = [
            component
            for component in anchor_components
            if component["role"] == "facade-only-short-stilt"
        ]
        if (
            quartz_records
            or len(stilt_components) != 6
            or sum(component["triangle_count"] for component in stilt_components)
            != 72
            or len(stilt_records) != 72
            or len(cable_records) != 12
        ):
            raise EvidenceError(
                f"native enclosed quartz facade {anchor.position} zero-layer custom projection changed"
            )
        _assert_native_anchor_world_attributes(
            anchor,
            stilt_records + cable_records,
            f"native enclosed quartz facade {anchor.position}",
        )
        facts.append(
            {
                "kind": "fully-surrounded-quartz-zero-facade-layers",
                "facade_triangle_count": 0,
                "short_stilt_triangle_count": 72,
                "cable_triangle_count": 12,
                "remains_custom": True,
            }
        )
    if (
        facade_states.get("up", {}).get("Name") == "minecraft:glass"
        and facade_states.get("west", {}).get("Name") == "minecraft:stone"
    ):
        glass_records = [
            record
            for record in records
            if record.material_identity == NATIVE_STRUCTURAL_GLASSENTIAL_MATERIAL
        ]
        if (
            not glass_records
            or not _near(
                min(
                    position[0]
                    for record in glass_records
                    for position in record.positions
                ),
                NATIVE_STRUCTURAL_FACADE_THICKNESS,
            )
        ):
            raise EvidenceError(
                f"native transparent facade {anchor.position} opaque-edge inset changed"
            )
        _assert_native_anchor_world_attributes(
            anchor, glass_records, f"native transparent facade {anchor.position}"
        )
        facts.append(
            {
                "kind": "transparent-opaque-inset",
                "inset_blocks": NATIVE_STRUCTURAL_FACADE_THICKNESS,
            }
        )
    if set(facade_states) == {"north", "east", "up"} and all(
        state == {"Name": "minecraft:stone"}
        for state in facade_states.values()
    ):
        stone_records = [
            record
            for record in records
            if record.material_identity == "minecraft:block/stone"
        ]
        kicked = any(
            _record_direction(record) in {"west", "east"}
            and any(
                _near(position[1], 1.0 - NATIVE_STRUCTURAL_FACADE_THICKNESS)
                and _near(position[2], NATIVE_STRUCTURAL_FACADE_THICKNESS)
                for position in record.positions
            )
            for record in stone_records
        )
        if not kicked:
            raise EvidenceError(
                f"native opaque facade {anchor.position} inner corner kick changed"
            )
        facts.append(
            {
                "kind": "opaque-inner-corner-kick",
                "kick_blocks": NATIVE_STRUCTURAL_FACADE_THICKNESS,
            }
        )
    return facts


def _validate_native_straight_source(
    anchor: AnchorContract,
    records: Sequence[TriangleRecord],
) -> dict[str, Any] | None:
    contract = anchor.native_structural
    assert contract is not None
    if contract.endpoint_straight_optimization_json is None:
        return None
    straight = json.loads(contract.endpoint_straight_optimization_json)
    if not straight.get("enabled"):
        return {
            "kind": "straight-blocked-by-part",
            "blocking_part": straight.get("blocking_part"),
            "machine_collars": straight.get("machine_collars"),
        }
    family = straight["effective_family"]
    cable_records = [
        record
        for record in records
        if record.material_identity.startswith("ae2:part/cable/")
    ]
    expected_count = 36 if family in {"smart", "dense_smart"} else 12
    if len(cable_records) != expected_count:
        raise EvidenceError(
            f"native endpoint {anchor.position} straight optimization leaked collars"
        )
    light_contract = _validate_native_structural_light_contract(
        cable_records, f"native endpoint {anchor.position} straight cable"
    )
    extension = 0.01 / 16.0 if family == "dense_smart" else 0.0
    cross_min = (3.0 if family == "dense_smart" else 5.0) / 16.0
    cross_max = (13.0 if family == "dense_smart" else 11.0) / 16.0
    allowed = (
        {-extension, 1.0 + extension},
        {cross_min, cross_max},
        {cross_min, cross_max},
    )
    for record in cable_records:
        for position in record.positions:
            if any(
                not any(_near(position[axis], candidate) for candidate in allowed[axis])
                for axis in range(3)
            ):
                raise EvidenceError(
                    f"native endpoint {anchor.position} straight geometry leaked arm/collar vertices"
                )
    # BlueMap's world AO samples the adjacent endpoint block on each axial
    # cap. Controller and Energy Acceptor are solid-render hosts and darken
    # those cap vertices to raw 63; Wireless AP is non-occluding and remains
    # raw 255. Side faces remain air-isolated. Validate every base/overlay
    # layer so channel textures cannot hide an AO mismatch; ordinary light is
    # world-derived, while the exact channel overlay resources are separately
    # forced to 15/15 by the generic S1 light contract above.
    endpoint_solid_render = {
        "ae2:controller": True,
        "ae2:energy_acceptor": True,
        "ae2:wireless_access_point": False,
    }
    endpoint_ao: dict[str, int] = {}
    for endpoint in contract.endpoints:
        solid_render = endpoint_solid_render.get(endpoint.block_id)
        if solid_render is None:
            raise EvidenceError(
                f"native endpoint {anchor.position} straight AO host is unsupported"
            )
        endpoint_ao[endpoint.direction] = 63 if solid_render else 255
    if set(endpoint_ao) != set(straight["directions"]):
        raise EvidenceError(
            f"native endpoint {anchor.position} straight AO directions changed"
        )
    for record in cable_records:
        direction = _record_direction(record)
        expected_ao = endpoint_ao.get(direction, 255)
        if any(ao != expected_ao for ao in record.aos):
            raise EvidenceError(
                f"native endpoint {anchor.position} endpoint-adjacency AO changed"
            )
    if any(endpoint.collar for endpoint in contract.endpoints):
        raise EvidenceError(
            f"native endpoint {anchor.position} straight metadata leaked a collar"
        )
    return {
        "kind": "opposite-native-endpoint-straight",
        "family": family,
        "triangle_count": len(cable_records),
        "machine_collars": False,
        "facades_ignored_as_attachments": True,
        "cable_anchor_ignored_as_attachment": True,
        "endpoint_ao_raw_u8": dict(sorted(endpoint_ao.items())),
        "side_ao_raw_u8": 255,
        "world_light_policy": NATIVE_STRUCTURAL_WORLD_LIGHT_POLICY,
        "light_validation": NATIVE_STRUCTURAL_LIGHT_VALIDATION,
        "forced_fullbright_triangle_count": light_contract[
            "forced_fullbright_triangle_count"
        ],
    }


def _validate_native_structural_glassential_closure(
    records_by_position: dict[
        tuple[int, int, int], Sequence[TriangleRecord]
    ],
) -> dict[str, Any]:
    """Prove the exact eight-row full-pack glass material substitution."""
    observed = {
        position: count
        for position, records in records_by_position.items()
        if (
            count := sum(
                record.material_identity == NATIVE_STRUCTURAL_GLASSENTIAL_MATERIAL
                for record in records
            )
        )
    }
    vanilla_glass_triangle_count = sum(
        record.material_identity == "minecraft:block/glass"
        for records in records_by_position.values()
        for record in records
    )
    if (
        observed != NATIVE_STRUCTURAL_GLASSENTIAL_FACADE_TRIANGLES
        or sum(observed.values())
        != NATIVE_STRUCTURAL_GLASSENTIAL_FACADE_TRIANGLE_COUNT
        or vanilla_glass_triangle_count != 0
    ):
        raise EvidenceError(
            "S1 Glassential full-pack facade material closure changed"
        )
    return {
        **NATIVE_STRUCTURAL_GLASSENTIAL_FULL_PACK_OVERRIDE,
        "active": True,
        "anchor_count": len(observed),
        "triangle_count": sum(observed.values()),
        "vanilla_glass_triangle_count": vanilla_glass_triangle_count,
        "anchor_triangle_counts": [
            {
                "position": {"x": position[0], "y": position[1], "z": position[2]},
                "triangle_count": count,
            }
            for position, count in sorted(observed.items())
        ],
        "validated": True,
    }


def _validate_native_structural_source_semantics(
    anchor: AnchorContract,
    records: Sequence[TriangleRecord],
) -> dict[str, Any]:
    contract = anchor.native_structural
    assert contract is not None
    facts: list[dict[str, Any]] = []
    emitter_parts = [
        part
        for part in contract.parts
        if part.part_id.removeprefix("ae2:") in NATIVE_STRUCTURAL_EMITTER_PART_IDS
    ]
    if emitter_parts and contract.cable_id == "ae2:fluix_glass_cable":
        covered_core = [
            record
            for record in records
            if record.material_identity
            == "ae2:part/cable/core/covered/transparent"
        ]
        if len(covered_core) != 12 or any(
            record.material_identity == "ae2:part/cable/core/glass/transparent"
            for record in records
        ):
            raise EvidenceError(
                f"native level emitter {anchor.position} did not force a covered core"
            )
        _assert_native_anchor_world_attributes(
            anchor, covered_core, f"native level emitter core {anchor.position}"
        )
        facts.append(
            {
                "kind": "smart-requesting-part-core",
                "part_id": emitter_parts[0].part_id,
                "source_center_family": "glass",
                "rendered_core_family": "covered",
                "triangle_count": 12,
                "emissive": False,
                "light_policy": "world-derived-own-and-outward-face-maximum",
                "light_validation": NATIVE_STRUCTURAL_LIGHT_VALIDATION,
            }
        )
    plane_parts = [
        part
        for part in contract.parts
        if part.part_id.removeprefix("ae2:") in NATIVE_STRUCTURAL_PLANE_PART_IDS
    ]
    if plane_parts and contract.plane_mask is not None:
        facts.append(_validate_native_plane_source(anchor, records, plane_parts[0]))
    if contract.p2p_frequency is not None:
        facts.append(_validate_native_p2p_source(anchor, records))
    facts.extend(_validate_native_facade_source(anchor, records))
    straight = _validate_native_straight_source(anchor, records)
    if straight is not None:
        facts.append(straight)
    return {
        "authority": "independent-ae2-19.2.17-source-goldens",
        "runtime_oracle_role": "subordinate-regression-snapshot",
        "facts": facts,
        "validated": True,
    }


def _validate_native_structural_contract(
    anchor: AnchorContract,
    records: Sequence[TriangleRecord],
    result: dict[str, Any],
) -> dict[str, Any] | None:
    contract = anchor.native_structural
    if contract is None:
        return None
    light_contract = _validate_native_structural_light_contract(
        records, f"native structural anchor {anchor.position}"
    )
    source_parity = _validate_native_structural_source_semantics(anchor, records)
    signature_scope = (
        f"anchor-v10:{anchor.position[0]},{anchor.position[1]},{anchor.position[2]}"
    )
    nonlighting_attribute_signature = _native_structural_nonlighting_signature(
        records, signature_scope
    )
    if (
        contract.expected_geometry_signature is None
        or contract.expected_nonlighting_attribute_signature is None
        or result.get("geometry_signature") != contract.expected_geometry_signature
        or nonlighting_attribute_signature
        != contract.expected_nonlighting_attribute_signature
    ):
        raise EvidenceError(
            f"native structural anchor {anchor.position} geometry/UV/material/normal/color/AO signature changed"
        )
    return {
        "profile": NATIVE_STRUCTURAL_ROUTE,
        "cable_id": contract.cable_id,
        "parts": [
            {
                "direction": part.direction,
                "part_id": part.part_id,
                "group": part.group,
                "spin": part.spin,
                "frequency_unsigned": part.frequency,
            }
            for part in contract.parts
        ],
        "facade_mask": contract.facade_mask,
        "plane_mask": contract.plane_mask,
        "p2p_frequency_unsigned": contract.p2p_frequency,
        "endpoints": [
            {
                "direction": endpoint.direction,
                "block_id": endpoint.block_id,
                "block_entity_id": endpoint.block_entity_id,
                "required_block_state": json.loads(
                    endpoint.required_block_state_json
                ),
                "observed_endpoint_side": endpoint.observed_endpoint_side,
                "side_rule": endpoint.side_rule,
                "formation": endpoint.formation,
                "exposed_on_observed_side": endpoint.exposed_on_observed_side,
                "declared_family": endpoint.declared_family,
                "local_family": endpoint.local_family,
                "effective_family": endpoint.effective_family,
                "collar": endpoint.collar,
                "topology": endpoint.topology,
            }
            for endpoint in contract.endpoints
        ],
        "endpoint_straight_optimization": (
            json.loads(contract.endpoint_straight_optimization_json)
            if contract.endpoint_straight_optimization_json is not None
            else None
        ),
        "geometry_uv_material_signature": contract.expected_geometry_signature,
        "nonlighting_attribute_signature": (
            contract.expected_nonlighting_attribute_signature
        ),
        "observed_full_attribute_signature": result.get("attribute_signature"),
        "world_light_policy": NATIVE_STRUCTURAL_WORLD_LIGHT_POLICY,
        "light_validation": NATIVE_STRUCTURAL_LIGHT_VALIDATION,
        "forced_fullbright_resources": sorted(
            NATIVE_STRUCTURAL_FORCED_FULLBRIGHT_RESOURCES
        ),
        "forced_fullbright_triangle_count": light_contract[
            "forced_fullbright_triangle_count"
        ],
        "source_parity": source_parity,
        "validated": True,
    }


def _connected_glass_nonlighting_signature(
    records: Sequence[TriangleRecord],
) -> str:
    rows = []
    for record in records:
        rows.append(
            canonical_json(
                {
                    "material": record.material_identity,
                    "positions": [
                        [canonical_float(value) for value in position]
                        for position in record.positions
                    ],
                    "uvs": [
                        [canonical_float(value) for value in uv]
                        for uv in record.uvs
                    ],
                    "normals": [list(normal) for normal in record.normals],
                    "colors": [list(color) for color in record.colors],
                    "ao": list(record.aos),
                }
            )
        )
    return sha256_text("connected-glass-nonlighting-v6\n" + "\n".join(sorted(rows)) + "\n")


def _drive_rotation_matrix(facing: str, spin: int) -> tuple[tuple[float, ...], ...]:
    """Match Variant's exact ``rotateYXZ(-x, -y, -z)`` quarter turns."""
    x, y, spins = DRIVE_ORIENTATION_ANGLES[facing]
    pitch = math.radians(-x) * 0.5
    yaw = math.radians(-y) * 0.5
    roll = math.radians(-spins[spin]) * 0.5
    sx, cx = math.sin(pitch), math.cos(pitch)
    sy, cy = math.sin(yaw), math.cos(yaw)
    sz, cz = math.sin(roll), math.cos(roll)
    cysx, sycx = cy * sx, sy * cx
    sysx, cycx = sy * sx, cy * cx
    qx = cysx * cz + sycx * sz
    qy = sycx * cz - cysx * sz
    qz = cycx * sz - sysx * cz
    qw = cycx * cz + sysx * sz
    matrix = (
        (1 - 2 * qy * qy - 2 * qz * qz, 2 * qx * qy - 2 * qw * qz, 2 * qx * qz + 2 * qw * qy),
        (2 * qx * qy + 2 * qw * qz, 1 - 2 * qx * qx - 2 * qz * qz, 2 * qy * qz - 2 * qw * qx),
        (2 * qx * qz - 2 * qw * qy, 2 * qy * qz + 2 * qx * qw, 1 - 2 * qx * qx - 2 * qy * qy),
    )
    return tuple(
        tuple(0.0 if abs(value) < 1.0e-12 else float(round(value, 12)) for value in row)
        for row in matrix
    )


def _drive_transform_point(
    point: tuple[float, float, float], facing: str, spin: int
) -> tuple[float, float, float]:
    matrix = _drive_rotation_matrix(facing, spin)
    centered = tuple(value - 0.5 for value in point)
    return tuple(
        0.5 + sum(matrix[row][column] * centered[column] for column in range(3))
        for row in range(3)
    )  # type: ignore[return-value]


def _drive_inverse_point(
    point: tuple[float, float, float], facing: str, spin: int
) -> tuple[float, float, float]:
    matrix = _drive_rotation_matrix(facing, spin)
    centered = tuple(value - 0.5 for value in point)
    return tuple(
        0.5 + sum(matrix[column][row] * centered[column] for column in range(3))
        for row in range(3)
    )  # type: ignore[return-value]


def _drive_close(first: float, second: float) -> bool:
    return abs(first - second) <= GEOMETRY_TOLERANCE


def _drive_triangle_key(
    positions: Iterable[tuple[float, float, float]],
) -> tuple[tuple[int, int, int], ...]:
    quantum = 2.0**-16
    return tuple(
        sorted(
            tuple(round(coordinate / quantum) for coordinate in position)
            for position in positions
        )
    )


def _drive_led_triangles(slot: DriveSlotContract) -> list[tuple[tuple[float, float, float], ...]]:
    ox, oy, oz = slot.origin

    def point(x16: float, y16: float, z16: float) -> tuple[float, float, float]:
        return (ox + x16 / 16, oy + y16 / 16, oz + z16 / 16)

    quads = (
        (point(4, 1, -0.001), point(5, 1, -0.001), point(5, -0.001, -0.001), point(4, -0.001, -0.001)),
        (point(5, 1, -0.001), point(5, 1, 0.999), point(5, -0.001, 0.999), point(5, -0.001, -0.001)),
        (point(4, 1, 0.999), point(4, 1, -0.001), point(4, -0.001, -0.001), point(4, -0.001, 0.999)),
        (point(4, 1, 0.999), point(5, 1, 0.999), point(5, 1, -0.001), point(4, 1, -0.001)),
        (point(4, -0.001, -0.001), point(5, -0.001, -0.001), point(5, -0.001, 0.999), point(4, -0.001, 0.999)),
    )
    return [
        triangle
        for quad in quads
        for triangle in ((quad[0], quad[1], quad[2]), (quad[0], quad[2], quad[3]))
    ]


def _drive_chassis_face(
    canonical_positions: Sequence[tuple[float, float, float]],
    slot: DriveSlotContract,
) -> str | None:
    ox, oy, oz = slot.origin
    minimum = (ox, oy, oz)
    maximum = (ox + 6 / 16, oy + 2 / 16, oz + 2 / 16)
    if any(
        coordinate < minimum[axis] - GEOMETRY_TOLERANCE
        or coordinate > maximum[axis] + GEOMETRY_TOLERANCE
        for position in canonical_positions
        for axis, coordinate in enumerate(position)
    ):
        return None
    if all(_drive_close(position[2], oz) for position in canonical_positions):
        face = "north"
    elif all(_drive_close(position[1], maximum[1]) for position in canonical_positions):
        face = "up"
    elif all(_drive_close(position[1], oy) for position in canonical_positions):
        face = "down"
    else:
        return None
    for position in canonical_positions:
        if any(
            not (_drive_close(position[axis], minimum[axis]) or _drive_close(position[axis], maximum[axis]))
            for axis in range(3)
        ):
            return None
    return face


def _drive_uv_corners(rect: tuple[int, int, int, int]) -> set[tuple[int, int]]:
    u0, v0, u1, v1 = rect
    return {
        (u0, v0),
        (u0, v1),
        (u1, v0),
        (u1, v1),
    }


def _validate_drive_contract(
    anchor: AnchorContract,
    records: Sequence[TriangleRecord],
) -> dict[str, Any]:
    drive = anchor.drive
    if drive is None:
        return {}
    if any(value != 255 for record in records for value in record.aos):
        raise EvidenceError(
            f"Drive anchor {anchor.position} does not force ambient occlusion to raw 255"
        )
    occupied = {slot.slot: slot for slot in drive.slots if slot.item_id is not None}
    occupied_materials = {slot.material for slot in occupied.values()}
    chassis_records = [
        record
        for record in records
        if record.material_identity in occupied_materials
    ]
    led_records = [
        record
        for record in records
        if record.material_identity == DRIVE_LED_MATERIAL
        and all(color == (0, 0, 0) for color in record.colors)
        and all(value == 255 for value in record.aos)
        and all(value == 15 for value in record.blocklights)
        and all(value == 15 for value in record.sunlights)
    ]
    if len(chassis_records) != 6 * len(occupied):
        raise EvidenceError(
            f"Drive anchor {anchor.position} has the wrong occupied chassis triangle count"
        )
    if len(led_records) != drive.led_triangle_count:
        raise EvidenceError(
            f"Drive anchor {anchor.position} has the wrong offline LED attribute count"
        )
    chassis_by_slot_face: dict[tuple[int, str], list[TriangleRecord]] = {}
    for record in chassis_records:
        matches = [
            (slot.slot, face)
            for slot in occupied.values()
            if record.material_identity == slot.material
            and (
                face := _drive_chassis_face(
                    tuple(
                        _drive_inverse_point(position, slot.facing, slot.spin)
                        for position in record.positions
                    ),
                    slot,
                )
            )
            is not None
        ]
        if len(matches) != 1:
            raise EvidenceError(
                f"Drive anchor {anchor.position} chassis triangle does not map to one declared slot"
            )
        chassis_by_slot_face.setdefault(matches[0], []).append(record)

    slot_results: dict[str, Any] = {}
    face_order = ("north", "up", "down")
    for slot_number, slot in sorted(occupied.items()):
        model_rects = DRIVE_MODEL_UV_RECTS[slot.model_id]
        face_uvs: dict[str, list[list[int]]] = {}
        for face, rect in zip(face_order, model_rects, strict=True):
            face_records = chassis_by_slot_face.get((slot_number, face), [])
            if len(face_records) != 2:
                raise EvidenceError(
                    f"Drive anchor {anchor.position} slot {slot_number} has the wrong {face} chassis face"
                )
            observed_uvs = {
                (round(uv[0] * 16), round(uv[1] * 16))
                for record in face_records
                for uv in record.uvs
            }
            if observed_uvs != _drive_uv_corners(rect):
                raise EvidenceError(
                    f"Drive anchor {anchor.position} slot {slot_number} UVs do not match {slot.model_id}"
                )
            face_uvs[face] = [list(point) for point in sorted(observed_uvs)]
        slot_results[str(slot_number)] = {
            "item_id": slot.item_id,
            "model_id": slot.model_id,
            "origin": {"x": slot.origin[0], "y": slot.origin[1], "z": slot.origin[2]},
            "chassis_triangle_count": 6,
            "uv_corners_sixteenths": face_uvs,
            "face": slot.face,
            "face_slot": slot.face_slot,
            "facing": slot.facing,
            "spin": slot.spin,
            "material": slot.material,
        }

    actual_led_geometry = Counter(
        _drive_triangle_key(record.positions)
        for record in led_records
    )
    expected_led_geometry = Counter(
        _drive_triangle_key(
            _drive_transform_point(position, slot.facing, slot.spin)
            for position in triangle
        )
        for slot in occupied.values()
        for triangle in _drive_led_triangles(slot)
    )
    if actual_led_geometry != expected_led_geometry or any(
        any(not _drive_close(component, 0.0) for component in uv)
        for record in led_records
        for uv in record.uvs
    ):
        raise EvidenceError(
            f"Drive anchor {anchor.position} offline LED slot geometry or UVs changed"
        )
    return {
        "facing": drive.facing,
        "spin": drive.spin,
        "block_id": drive.block_id,
        "triangle_formula": drive.triangle_formula,
        "base_triangle_count": drive.base_triangle_count,
        "occupied_slot_count": len(occupied),
        "occupied_slots": slot_results,
        "cell_chassis": {
            "material": (
                next(iter(occupied_materials))
                if len(occupied_materials) == 1
                else None
            ),
            "material_triangle_counts": dict(
                sorted(Counter(record.material_identity for record in chassis_records).items())
            ),
            "triangle_count": len(chassis_records),
            "triangle_count_per_occupied_slot": 6,
        },
        "offline_led": {
            "material": DRIVE_LED_MATERIAL,
            "policy": "static-offline-unknown",
            "rgb_u8": [0, 0, 0],
            "ambient_occlusion_raw_u8": 255,
            "blocklight_raw_i8": 15,
            "sunlight_raw_i8": 15,
            "triangle_count": len(led_records),
            "triangle_count_per_occupied_slot": 10,
        },
    }


def _drive_normalized_signature(records: Sequence[TriangleRecord]) -> str:
    rows = []
    for record in records:
        rows.append(
            canonical_json(
                {
                    "material": record.material_identity,
                    "positions": _drive_triangle_key(record.positions),
                    "uvs": sorted(
                        (round(uv[0] / SHAPE_QUANTUM), round(uv[1] / SHAPE_QUANTUM))
                        for uv in record.uvs
                    ),
                    "colors": sorted(record.colors),
                    "ao": sorted(record.aos),
                    "blocklight": sorted(record.blocklights),
                    "sunlight": sorted(record.sunlights),
                }
            )
        )
    return sha256_text("drive-normalized-geometry-attributes-v4\n" + "\n".join(sorted(rows)) + "\n")


def _drive_component_invariant_signature(
    records: Sequence[TriangleRecord],
) -> str:
    """Hash renderer-controlled Drive output while excluding world light."""
    rows = []
    for record in records:
        rows.append(
            canonical_json(
                {
                    "material": record.material_identity,
                    "positions": _drive_triangle_key(record.positions),
                    "uvs": sorted(
                        (round(uv[0] / SHAPE_QUANTUM), round(uv[1] / SHAPE_QUANTUM))
                        for uv in record.uvs
                    ),
                    "colors": sorted(record.colors),
                    "ao": sorted(record.aos),
                }
            )
        )
    return sha256_text(
        "drive-component-invariant-nonlighting-attributes-v5\n"
        + "\n".join(sorted(rows))
        + "\n"
    )


def _drive_face_local_signature(
    records: Sequence[TriangleRecord], drive: DriveContract
) -> str:
    occupied = [slot for slot in drive.slots if slot.item_id is not None]
    if len(occupied) != 1:
        raise EvidenceError("front/back mirror signature requires one occupied slot")
    slot = occupied[0]
    selected_records = [
        record
        for record in records
        if record.material_identity == slot.material
        or (
            record.material_identity == DRIVE_LED_MATERIAL
            and all(color == (0, 0, 0) for color in record.colors)
            and all(value == 15 for value in record.blocklights)
            and all(value == 15 for value in record.sunlights)
        )
    ]
    if len(selected_records) != 16:
        raise EvidenceError("front/back mirror cell must own exactly 16 emitted triangles")
    rows = [
        canonical_json(
            {
                "material": record.material_identity,
                "positions": _drive_triangle_key(
                    _drive_inverse_point(position, slot.facing, slot.spin)
                    for position in record.positions
                ),
                "uvs": sorted(
                    (round(uv[0] / SHAPE_QUANTUM), round(uv[1] / SHAPE_QUANTUM))
                    for uv in record.uvs
                ),
                "colors": sorted(record.colors),
                "ao": sorted(record.aos),
                "blocklight": sorted(record.blocklights),
                "sunlight": sorted(record.sunlights),
            }
        )
        for record in selected_records
    ]
    return sha256_text(
        "extended-drive-face-local-geometry-attributes-v5\n"
        + "\n".join(sorted(rows))
        + "\n"
    )


def _records_result(records: Iterable[TriangleRecord], signature_scope: str) -> dict[str, Any]:
    records_list = list(records)
    shape_rows = sorted(record.shape for record in records_list)
    geometry_rows = sorted(record.geometry for record in records_list)
    attribute_rows = sorted(record.attributes for record in records_list)
    ordinal_material_counter = Counter(
        (record.material_index, record.material_identity) for record in records_list
    )
    semantic_material_counter = Counter(
        record.material_identity for record in records_list
    )
    materials = [
        {
            "material_index": material_index,
            "resource_path": identity,
            "resolved": True,
            "triangle_count": count,
        }
        for (material_index, identity), count in sorted(
            ordinal_material_counter.items(), key=lambda row: (row[0][1], row[0][0])
        )
    ]
    material_rows = [
        f"{identity}\0{count}"
        for identity, count in sorted(semantic_material_counter.items())
    ]
    return {
        "triangle_count": len(records_list),
        "materials": materials,
        "shape_signature": sha256_text(
            "material-independent-shape-v3\n" + "\n".join(shape_rows) + "\n"
        ),
        "material_signature": sha256_text(
            signature_scope + "\n" + "\n".join(material_rows) + "\n"
        ),
        "geometry_signature": sha256_text(
            signature_scope + "\n" + "\n".join(geometry_rows) + "\n"
        ),
        "attribute_signature": sha256_text(
            signature_scope + "\n" + "\n".join(attribute_rows) + "\n"
        ),
    }


def _validate_schema9_disabled_projection(
    anchor: AnchorContract,
    records: Sequence[TriangleRecord],
) -> dict[str, Any]:
    native = anchor.native_structural
    if native is None:
        raise EvidenceError("S1 predecessor projection anchor lacks its contract")
    projection = native.schema9_route_disabled_projection
    if projection is None:
        raise EvidenceError("S1 predecessor projection metadata is missing")
    actual_materials = Counter(record.material_identity for record in records)
    expected_materials = dict(projection.expected_material_triangles)
    if (
        len(records) != projection.expected_triangle_count
        or actual_materials != expected_materials
    ):
        raise EvidenceError(
            f"native-structural-disabled anchor {anchor.position} differs from "
            "its exact schema-9 predecessor projection"
        )
    if projection.expected_triangle_count == 0:
        if records or expected_materials:
            raise EvidenceError(
                f"native-structural-disabled anchor {anchor.position} expected "
                "an empty schema-9 predecessor projection"
            )
        return {
            "expected_path": projection.expected_path,
            "triangle_count": 0,
            "material_triangle_counts": {},
            "validated": True,
        }

    _validate_native_structural_light_contract(
        records, f"schema-9 predecessor anchor {anchor.position}"
    )
    projected_anchor = AnchorContract(
        case_id=anchor.case_id,
        case_label=anchor.case_label,
        expected_path=projection.expected_path,
        position=anchor.position,
        expected_triangle_count=projection.expected_triangle_count,
        expected_material_triangles=projection.expected_material_triangles,
        expected_smart_overlays=projection.expected_smart_overlays,
        face_parts=anchor.face_parts,
        facades=anchor.facades,
        expected_terminal_layers=projection.expected_terminal_layers,
        drive=None,
        fallback_reason=None,
    )
    overlay_by_resource = {
        resource: (rgb, blocklight, sunlight)
        for resource, rgb, blocklight, sunlight
        in projection.expected_smart_overlays
    }
    terminal_by_resource = {
        layer.resource_path: layer for layer in projection.expected_terminal_layers
    }
    for record in records:
        if any(ao != 255 for ao in record.aos):
            raise EvidenceError(
                f"schema-9 predecessor anchor {anchor.position} ambient occlusion changed"
            )
        overlay = overlay_by_resource.get(record.material_identity)
        terminal = terminal_by_resource.get(record.material_identity)
        expected_rgb = (
            overlay[0]
            if overlay is not None
            else terminal.rgb
            if terminal is not None
            else (255, 255, 255)
        )
        if (
            any(color != expected_rgb for color in record.colors)
            or (
                overlay is not None
                and (
                    any(light != overlay[1] for light in record.blocklights)
                    or any(light != overlay[2] for light in record.sunlights)
                )
            )
        ):
            raise EvidenceError(
                f"schema-9 predecessor anchor {anchor.position} color/light changed"
            )
    terminal_contract = _validate_terminal_contract(projected_anchor, records)
    facade_contract = _validate_facade_contract(projected_anchor, records)
    return {
        "expected_path": projection.expected_path,
        "triangle_count": projection.expected_triangle_count,
        "material_triangle_counts": expected_materials,
        "smart_overlays": {
            resource: {
                "rgb_u8": list(rgb),
                "blocklight_raw_i8": blocklight,
                "sunlight_raw_i8": sunlight,
            }
            for resource, rgb, blocklight, sunlight
            in projection.expected_smart_overlays
        },
        "terminal_layers": terminal_contract,
        "facade": facade_contract,
        "validated": True,
    }


def _legacy_upgrade_regression_marker(excluded_anchor_count: int) -> dict[str, int]:
    if excluded_anchor_count not in {0, NATIVE_STRUCTURAL_LEGACY_ANCHOR_COUNT}:
        raise EvidenceError("legacy upgrade regression exclusion count changed")
    return (
        {"legacy_upgrade_excluded_anchor_count": excluded_anchor_count}
        if excluded_anchor_count
        else {}
    )


def _m45_legacy_upgrade_regression_marker(
    excluded_anchor_count: int,
) -> dict[str, int]:
    if excluded_anchor_count not in range(
        len(M45_LEGACY_UPGRADE_SPECS) + 1
    ):
        raise EvidenceError(
            "M4/M5 legacy-upgrade regression exclusion count changed"
        )
    return (
        {"m45_legacy_upgrade_excluded_anchor_count": excluded_anchor_count}
        if excluded_anchor_count
        else {}
    )


def _m45_mode_projection(
    anchor: AnchorContract,
    *,
    stock_baseline: bool,
    crafting_disabled: bool,
    native_structural_disabled: bool,
    m45_route_disabled: str | None,
    m45_disabled: bool = False,
) -> M45ProjectionContract | None:
    m45 = anchor.m45
    if m45 is None:
        return None
    if stock_baseline:
        return m45.physical_stock_projection
    if native_structural_disabled:
        projection = m45.native_structural_disabled_projection
        return (
            None
            if projection.expected_path == anchor.expected_path
            and projection.review_projection == m45.review_projection
            else projection
        )
    if crafting_disabled and m45.crafting_disabled_projection is not None:
        return m45.crafting_disabled_projection
    if m45_disabled:
        return dict(m45.route_disabled_projections).get(m45.route)
    if m45_route_disabled is not None:
        return dict(m45.route_disabled_projections).get(m45_route_disabled)
    return None


def _appmek_mode_projection(
    anchor: AppMekAnchorContract,
    *,
    stock_baseline: bool,
    native_structural_disabled: bool,
    appmek_drive_disabled: bool,
) -> AppMekProjectionContract | None:
    if stock_baseline:
        return anchor.physical_stock_projection
    if native_structural_disabled:
        return anchor.native_structural_disabled_projection
    if appmek_drive_disabled:
        return anchor.route_disabled_projection
    return None


def _m45_legacy_upgrade_mode_projection(
    anchor: AnchorContract,
    *,
    stock_baseline: bool,
    extension_disabled: bool,
    crafting_disabled: bool,
    m45_route_disabled: str | None,
    m45_disabled: bool,
) -> M45ProjectionContract | None:
    upgrade = anchor.m45_legacy_upgrade
    if upgrade is None:
        return None
    if stock_baseline:
        return upgrade.predecessor_projection
    if m45_disabled or (
        m45_route_disabled is not None
        and m45_route_disabled in upgrade.required_m45_routes
    ):
        return upgrade.predecessor_projection
    if extension_disabled and "extension" in upgrade.required_legacy_routes:
        return upgrade.predecessor_projection
    if crafting_disabled and "crafting" in upgrade.required_legacy_routes:
        return upgrade.predecessor_projection
    return None


def _m45_nonlighting_signature(
    records: Sequence[TriangleRecord],
    signature_scope: str,
) -> str:
    rows = [
        canonical_json(
            {
                "geometry": record.geometry,
                "normal_raw_i8": [list(normal) for normal in record.normals],
                "color_raw_u8": [list(color) for color in record.colors],
                "ao_raw_u8": list(record.aos),
            }
        )
        for record in records
    ]
    return sha256_text(
        "m45-nonlighting-attributes-v11\n"
        + signature_scope
        + "\n"
        + "\n".join(sorted(rows))
        + "\n"
    )


def _validate_m45_light_contract(
    records: Sequence[TriangleRecord],
    description: str,
) -> dict[str, int]:
    if not records:
        raise EvidenceError(f"{description} has no oracle triangles")
    for record in records:
        for channel, values in (
            ("blocklight", record.blocklights),
            ("sunlight", record.sunlights),
        ):
            if len(values) != 3:
                raise EvidenceError(
                    f"{description} {channel} must contain exactly three triangle values"
                )
            if any(
                not isinstance(value, int)
                or isinstance(value, bool)
                or value < 0
                or value > 15
                for value in values
            ):
                raise EvidenceError(
                    f"{description} {channel} is outside the [0, 15] bound"
                )
            if len(set(values)) != 1:
                raise EvidenceError(
                    f"{description} {channel} is not flat within one triangle"
                )
    return {"triangle_count": len(records), "flat_triangle_count": len(records)}


def _validate_m45_runtime_oracle(
    position: tuple[int, int, int],
    records: Sequence[TriangleRecord],
    result: dict[str, Any],
    *,
    expected_triangle_count: int | None,
    expected_material_triangles: tuple[tuple[str, int], ...],
    expected_geometry_signature: str | None,
    expected_nonlighting_attribute_signature: str | None,
    description: str,
) -> dict[str, Any]:
    if (
        expected_triangle_count is None
        or expected_geometry_signature is None
        or expected_nonlighting_attribute_signature is None
    ):
        raise EvidenceError(f"{description} lacks its exact runtime oracle")
    light_validation = _validate_m45_light_contract(records, description)
    actual_materials = Counter(record.material_identity for record in records)
    signature_scope = f"anchor-v11:{position[0]},{position[1]},{position[2]}"
    nonlighting_attribute_signature = _m45_nonlighting_signature(
        records, signature_scope
    )
    if (
        len(records) != expected_triangle_count
        or actual_materials != dict(expected_material_triangles)
        or result.get("geometry_signature") != expected_geometry_signature
        or nonlighting_attribute_signature
        != expected_nonlighting_attribute_signature
    ):
        raise EvidenceError(
            f"{description} geometry/UV/material/normal/color/AO signature changed"
        )
    return {
        "triangle_count": expected_triangle_count,
        "material_triangle_counts": dict(expected_material_triangles),
        "geometry_uv_material_signature": expected_geometry_signature,
        "nonlighting_attribute_signature": (
            expected_nonlighting_attribute_signature
        ),
        "observed_full_attribute_signature": result.get("attribute_signature"),
        "light_validation": light_validation,
        "runtime_oracle_validated": True,
    }


def _validate_m45_legacy_upgrade(
    anchor: AnchorContract,
    records: list[TriangleRecord],
    result: dict[str, Any],
    projection: M45ProjectionContract | None,
) -> dict[str, Any]:
    upgrade = anchor.m45_legacy_upgrade
    if upgrade is None:
        raise EvidenceError("M4/M5 legacy-upgrade anchor lacks its contract")
    effective = upgrade.enabled_projection if projection is None else projection
    actual_materials = Counter(record.material_identity for record in records)
    unexpected_resources = sorted(
        set(actual_materials) - set(effective.allowed_resources)
    )
    if unexpected_resources:
        raise EvidenceError(
            f"M4/M5 legacy-upgrade anchor {anchor.position} emitted resources "
            f"outside its exact observed allowlist: {unexpected_resources}"
        )
    if effective.review_projection == "empty" and records:
        raise EvidenceError(
            f"M4/M5 legacy predecessor anchor {anchor.position} owns "
            f"{len(records)} triangles; expected empty"
        )
    if effective.review_projection == "nonempty" and not records:
        raise EvidenceError(
            f"M4/M5 legacy upgraded anchor {anchor.position} is empty; "
            "expected exact runtime geometry"
        )
    expected_materials = dict(effective.expected_material_triangles)
    if expected_materials and actual_materials != expected_materials:
        raise EvidenceError(
            f"M4/M5 legacy-upgrade anchor {anchor.position} differs from its exact material signature"
        )
    runtime_oracle = (
        _validate_m45_runtime_oracle(
            anchor.position,
            records,
            result,
            expected_triangle_count=effective.expected_triangle_count,
            expected_material_triangles=effective.expected_material_triangles,
            expected_geometry_signature=effective.expected_geometry_signature,
            expected_nonlighting_attribute_signature=(
                effective.expected_nonlighting_attribute_signature
            ),
            description=f"M4/M5 legacy-upgrade anchor {anchor.position}",
        )
        if projection is None
        else None
    )
    contract = {
        "source_expected_path": anchor.expected_path,
        "expected_path": effective.expected_path,
        "review_projection": effective.review_projection,
        "projection_reason": effective.reason,
        "triangle_count": len(records),
        "material_triangle_counts": dict(sorted(actual_materials.items())),
        "allowed_resources": list(effective.allowed_resources),
        "required_m45_routes": list(upgrade.required_m45_routes),
        "required_legacy_routes": list(upgrade.required_legacy_routes),
        "raw_observed_baseline": {
            "triangle_count": upgrade.observed_triangle_count,
            "material_triangle_counts": dict(
                upgrade.observed_material_triangles
            ),
            "enforcement": (
                "exact-runtime-map-geometry-material-nonlighting-v11"
            ),
        },
        "runtime_oracle": runtime_oracle,
        "validated": True,
    }
    if runtime_oracle is not None:
        contract["triangle_count"] = runtime_oracle["triangle_count"]
        contract["material_triangle_counts"] = runtime_oracle[
            "material_triangle_counts"
        ]
    return contract


def _validate_m45_selector_material_exception(
    anchor: AnchorContract,
    actual_materials: Counter[str],
) -> set[str]:
    m45 = anchor.m45
    if m45 is None:
        raise EvidenceError("M4/M5 selector exception lacks its review contract")
    expected_materials = dict(
        m45.selector_scoped_exact_material_triangles
    )
    if expected_materials:
        if (
            anchor.position != M45_ADVANCED_SINGLETON_POSITION
            or actual_materials != expected_materials
        ):
            raise EvidenceError(
                "M4/M5 Advanced singleton must own exactly 132 upstream-model "
                "triangles (96 core, 4 core-out, 32 fully occluded missing)"
            )
        return set(expected_materials)
    if actual_materials.get("bluemap:block/missing", 0):
        raise EvidenceError(
            f"M4/M5 anchor {anchor.position} emitted selector-forbidden "
            "bluemap:block/missing triangles"
        )
    return set()


def _validate_m45_projection(
    anchor: AnchorContract,
    records: list[TriangleRecord],
    projection: M45ProjectionContract,
) -> dict[str, Any]:
    m45 = anchor.m45
    if m45 is None:
        raise EvidenceError("M4/M5 projected anchor is missing its review contract")
    actual_materials = Counter(record.material_identity for record in records)
    selector_exception_resources = _validate_m45_selector_material_exception(
        anchor, actual_materials
    )
    unexpected_resources = sorted(
        set(actual_materials)
        - set(projection.allowed_resources)
        - selector_exception_resources
    )
    if unexpected_resources:
        raise EvidenceError(
            f"M4/M5 projected anchor {anchor.position} emitted resources outside "
            f"its exact {projection.expected_path} projection: {unexpected_resources}"
        )
    expected_materials = dict(projection.expected_material_triangles)
    if expected_materials and actual_materials != expected_materials:
        raise EvidenceError(
            f"M4/M5 projected anchor {anchor.position} differs from its exact "
            "inherited original-resource material signature"
        )
    if projection.review_projection == "empty" and records:
        raise EvidenceError(
            f"M4/M5 projected anchor {anchor.position} owns {len(records)} "
            "triangles; expected empty"
        )
    if projection.review_projection == "nonempty" and not records:
        raise EvidenceError(
            f"M4/M5 projected anchor {anchor.position} is empty; expected nonempty"
        )
    return {
        "triangle_count": len(records),
        "material_triangle_counts": dict(sorted(actual_materials.items())),
        "m45_route": m45.route,
        "expected_path": projection.expected_path,
        "review_projection": projection.review_projection,
        "projection_reason": projection.reason,
        "allowed_resources": list(projection.allowed_resources),
        "exact_material_signature": bool(expected_materials),
        "host_resources": list(m45.host_resources),
        "selector_scoped_model_exception": bool(
            m45.selector_scoped_exact_material_triangles
        ),
        "validated": True,
    }


def analyze(
    map_root: Path,
    cases_path: Path,
    *,
    include_dense: bool = False,
    stock_baseline: bool = False,
    extension_disabled: bool = False,
    glass_disabled: bool = False,
    crafting_disabled: bool = False,
    quantum_disabled: bool = False,
    m3_completion_disabled: bool = False,
    native_structural_disabled: bool = False,
    m45_route_disabled: str | None = None,
    m45_disabled: bool = False,
    appmek_drive_disabled: bool = False,
) -> dict[str, Any]:
    if m45_route_disabled is not None and m45_route_disabled not in M45_ROUTES:
        raise EvidenceError(f"unknown M4/M5 route: {m45_route_disabled}")
    if sum(
        (
            stock_baseline,
            extension_disabled,
            glass_disabled,
            crafting_disabled,
            quantum_disabled,
            m3_completion_disabled,
            native_structural_disabled,
            m45_route_disabled is not None,
            m45_disabled,
            appmek_drive_disabled,
        )
    ) > 1:
        raise EvidenceError(
            "stock-baseline and all route-disabled modes are mutually exclusive"
        )
    if stock_baseline and include_dense:
        raise EvidenceError("stock-baseline mode cannot include the add-on dense fixture")
    map_root = map_root.resolve()
    cases_path = cases_path.resolve()
    settings, settings_digest = parse_settings(map_root)
    textures, textures_evidence = parse_textures(map_root)
    gallery, cases_evidence = parse_cases(cases_path)
    if extension_disabled and gallery.schema_version not in {5, 6, 7, 8, 9, 10, 11, 12}:
        raise EvidenceError("extension-disabled mode requires a schema-5+ M3b gallery")
    if glass_disabled and gallery.schema_version not in {6, 7, 8, 9, 10, 11, 12}:
        raise EvidenceError("glass-disabled mode requires a schema-6+ M3c gallery")
    if crafting_disabled and gallery.schema_version not in {7, 8, 9, 10, 11, 12}:
        raise EvidenceError("crafting-disabled mode requires a schema-7+ M3d gallery")
    if quantum_disabled and gallery.schema_version not in {8, 9, 10, 11, 12}:
        raise EvidenceError("quantum-disabled mode requires a schema-8+ M3e gallery")
    if m3_completion_disabled and gallery.schema_version not in {9, 10, 11, 12}:
        raise EvidenceError("m3-completion-disabled mode requires a schema-9+ M3f gallery")
    if native_structural_disabled and gallery.schema_version != 10:
        if gallery.schema_version not in {11, 12}:
            raise EvidenceError(
                "native-structural-disabled mode requires the schema-10+ S1 gallery"
            )
    if (m45_route_disabled is not None or m45_disabled) and gallery.schema_version not in {11, 12}:
        raise EvidenceError("M4/M5 route-disabled mode requires the schema-11 gallery")
    if appmek_drive_disabled and gallery.schema_version != 12:
        raise EvidenceError(
            "AppMek Drive route-disabled mode requires the schema-12 gallery"
        )
    validation_mode = (
        "stock-baseline"
        if stock_baseline
        else "extension-disabled"
        if extension_disabled
        else "glass-disabled"
        if glass_disabled
        else "crafting-disabled"
        if crafting_disabled
        else "quantum-disabled"
        if quantum_disabled
        else "m3-completion-disabled"
        if m3_completion_disabled
        else "native-structural-disabled"
        if native_structural_disabled
        else f"m45-route-disabled:{m45_route_disabled}"
        if m45_route_disabled is not None
        else "m45-disabled"
        if m45_disabled
        else "appmek-drive-disabled"
        if appmek_drive_disabled
        else "enabled"
    )
    legacy_validation_mode = (
        "enabled"
        if native_structural_disabled
        or m45_route_disabled is not None
        or m45_disabled
        or appmek_drive_disabled
        else validation_mode
    )
    cases = gallery.cases

    disabled_positions: set[tuple[int, int, int]] = set()
    if extension_disabled:
        disabled_positions.update(gallery.extension_positions)
    elif glass_disabled:
        disabled_positions.update(gallery.glass_positions)
    elif crafting_disabled:
        disabled_positions.update(gallery.crafting_positions)
    elif quantum_disabled:
        disabled_positions.update(gallery.quantum_positions)
    elif m3_completion_disabled:
        disabled_positions.update(gallery.m3_completion_positions)
    elif native_structural_disabled:
        disabled_positions.update(gallery.native_structural_positions)
        disabled_positions.update(
            gallery.native_structural_legacy_upgrade_positions
        )
    elif m45_route_disabled is not None:
        disabled_positions.update(
            dict(gallery.m45_route_positions)[m45_route_disabled]
        )
    elif m45_disabled:
        disabled_positions.update(gallery.m45_positions)
    m45_legacy_effective_projections = {
        anchor.position: _m45_legacy_upgrade_mode_projection(
            anchor,
            stock_baseline=stock_baseline,
            extension_disabled=extension_disabled,
            crafting_disabled=crafting_disabled,
            m45_route_disabled=m45_route_disabled,
            m45_disabled=m45_disabled,
        )
        for case in cases
        for anchor in case.anchors
        if anchor.m45_legacy_upgrade is not None
    }
    expected_active_custom_anchors = [
        anchor
        for case in cases
        for anchor in case.anchors
        if anchor.expected_path.startswith("custom-")
        and anchor.m45 is None
        and anchor.position not in disabled_positions
    ]
    expected_native_predecessor_anchors = [
        anchor
        for case in cases
        for anchor in case.anchors
        if native_structural_disabled
        and anchor.position in gallery.native_structural_positions
        and anchor.native_structural is not None
        and anchor.native_structural.schema9_route_disabled_projection is not None
        and (
            anchor.native_structural.schema9_route_disabled_projection.expected_path
        ).startswith("custom-")
    ]
    expected_active_custom_anchor_count = (
        len(expected_active_custom_anchors)
        + len(expected_native_predecessor_anchors)
    )
    expected_active_custom_triangles = sum(
        anchor.expected_triangle_count or 0 for anchor in expected_active_custom_anchors
    ) + sum(
        anchor.native_structural.schema9_route_disabled_projection.expected_triangle_count
        for anchor in expected_native_predecessor_anchors
        if anchor.native_structural is not None
        and anchor.native_structural.schema9_route_disabled_projection is not None
    )
    expected_active_custom_resources = sorted(
        {
            resource
            for anchor in expected_active_custom_anchors
            for resource, _count in anchor.expected_material_triangles
        }
        | {
            resource
            for anchor in expected_native_predecessor_anchors
            if anchor.native_structural is not None
            and anchor.native_structural.schema9_route_disabled_projection is not None
            for resource, _count in anchor.native_structural.schema9_route_disabled_projection.expected_material_triangles
        }
    )
    expected_active_fallback_count = sum(
        anchor.expected_path.startswith("stock-fallback-")
        and anchor.m45 is None
        and anchor.m45_legacy_upgrade is None
        and anchor.position not in disabled_positions
        for case in cases
        for anchor in case.anchors
    ) + (
        len(gallery.native_structural_legacy_upgrade_positions)
        if native_structural_disabled
        else 0
    )
    m45_effective_projections = {
        anchor.position: _m45_mode_projection(
            anchor,
            stock_baseline=stock_baseline,
            crafting_disabled=crafting_disabled,
            native_structural_disabled=native_structural_disabled,
            m45_route_disabled=m45_route_disabled,
            m45_disabled=m45_disabled,
        )
        for case in cases
        for anchor in case.anchors
        if anchor.m45 is not None
    }
    expected_m45_custom_count = sum(
        anchor.m45 is not None
        and anchor.expected_path == "custom-m45"
        and m45_effective_projections[anchor.position] is None
        for case in cases
        for anchor in case.anchors
    )
    expected_m45_fallback_count = sum(
        anchor.m45 is not None
        and anchor.expected_path == "stock-fallback-m45"
        and m45_effective_projections[anchor.position] is None
        for case in cases
        for anchor in case.anchors
    )
    expected_m45_disabled_count = sum(
        projection is not None
        for projection in m45_effective_projections.values()
    )
    expected_m45_projected_nonempty_count = sum(
        projection is not None and projection.review_projection == "nonempty"
        for projection in m45_effective_projections.values()
    )
    expected_m45_projected_empty_count = sum(
        projection is not None and projection.review_projection == "empty"
        for projection in m45_effective_projections.values()
    )
    if gallery.schema_version == 10 and validation_mode == "enabled" and (
        expected_active_custom_anchor_count != 940
        or expected_active_custom_triangles != 64_938
        or len(expected_active_custom_resources) != 289
        or expected_active_fallback_count != 16
    ):
        raise EvidenceError("schema-10 enabled aggregate contract changed")

    anchor_lookup = {
        anchor.position: anchor for case in cases for anchor in case.anchors
    }
    selection_positions = set(anchor_lookup)
    if include_dense:
        selection_positions.update(gallery.dense_positions)
    tile_anchors: dict[tuple[int, int], set[tuple[int, int, int]]] = {}
    for position in selection_positions:
        tile = settings.tile_for(position[0], position[2])
        tile_anchors.setdefault(tile, set()).add(position)

    selected: dict[tuple[int, int, int], list[TriangleRecord]] = {
        position: [] for position in selection_positions
    }
    tile_results: list[dict[str, Any]] = []
    for tile_x, tile_z in sorted(tile_anchors):
        path = tile_path(map_root, tile_x, tile_z)
        gzip_payload = read_single_gzip(path)
        document = parse_prbm(gzip_payload.payload)
        for group in document.groups:
            if group.material_index >= len(textures):
                raise EvidenceError(
                    f"tile ({tile_x},{tile_z}) uses material {group.material_index}, "
                    f"but textures.json has {len(textures)} entries"
                )
        origin_x, origin_z = settings.tile_origin(tile_x, tile_z)
        selected_count = 0
        for triangle_index in range(document.triangle_count):
            positions = []
            for corner in range(3):
                local = document.values("position", triangle_index * 3 + corner)
                positions.append(
                    (
                        float(local[0]) + origin_x,
                        float(local[1]),
                        float(local[2]) + origin_z,
                    )
                )
            material_index = _material_for_vertex(
                document.groups, triangle_index * 3
            )
            texture = textures[material_index]
            owner = _selected_triangle_owner(
                positions,
                tile_anchors[(tile_x, tile_z)],
                anchor_lookup,
                texture.semantic_identity,
            )
            if owner is None:
                continue
            selected[owner].append(
                _triangle_record(
                    document,
                    triangle_index,
                    positions,
                    owner,
                    texture,
                )
            )
            selected_count += 1
        tile_results.append(
            {
                "tile": {"x": tile_x, "z": tile_z},
                "compressed_sha256": gzip_payload.compressed_sha256,
                "payload_sha256": gzip_payload.payload_sha256,
                "triangle_count": document.triangle_count,
                "selected_triangle_count": selected_count,
                "material_group_count": len(document.groups),
            }
        )

    case_results: list[dict[str, Any]] = []
    all_geometry_rows: list[str] = []
    all_attribute_rows: list[str] = []
    all_material_rows: list[str] = []
    all_shape_rows: list[str] = []
    custom_records: list[TriangleRecord] = []
    m2_regression_records: dict[tuple[int, int, int], list[TriangleRecord]] = {}
    m2_regression_rows = {
        "geometry": [],
        "attributes": [],
        "materials": [],
        "shapes": [],
    }
    m3a_regression_records: dict[tuple[int, int, int], list[TriangleRecord]] = {}
    m3a_regression_rows = {
        "geometry": [],
        "attributes": [],
        "materials": [],
        "shapes": [],
    }
    m3b_regression_records: dict[tuple[int, int, int], list[TriangleRecord]] = {}
    m3b_regression_rows = {
        "geometry": [],
        "attributes": [],
        "materials": [],
        "shapes": [],
    }
    schema6_regression_records: dict[
        tuple[int, int, int], list[TriangleRecord]
    ] = {}
    schema6_regression_rows = {
        "geometry": [],
        "attributes": [],
        "materials": [],
        "shapes": [],
    }
    schema7_regression_records: dict[
        tuple[int, int, int], list[TriangleRecord]
    ] = {}
    schema7_regression_rows = {
        "geometry": [],
        "attributes": [],
        "materials": [],
        "shapes": [],
    }
    schema8_regression_records: dict[
        tuple[int, int, int], list[TriangleRecord]
    ] = {}
    schema8_regression_rows = {
        "geometry": [],
        "attributes": [],
        "materials": [],
        "shapes": [],
    }
    glass_records: dict[tuple[int, int, int], list[TriangleRecord]] = {}
    crafting_records: dict[tuple[int, int, int], list[TriangleRecord]] = {}
    quantum_records: dict[tuple[int, int, int], list[TriangleRecord]] = {}
    m3_completion_records: dict[tuple[int, int, int], list[TriangleRecord]] = {}
    native_structural_records: dict[tuple[int, int, int], list[TriangleRecord]] = {}
    native_structural_legacy_upgrade_records: dict[
        tuple[int, int, int], list[TriangleRecord]
    ] = {}
    m45_legacy_upgrade_records: dict[
        tuple[int, int, int], list[TriangleRecord]
    ] = {}
    m45_records: dict[tuple[int, int, int], list[TriangleRecord]] = {}
    component_pair_records: dict[tuple[int, int, int], list[TriangleRecord]] = {}
    extended_component_pair_records: dict[
        tuple[int, int, int], list[TriangleRecord]
    ] = {}
    extended_mirror_pair_records: dict[
        tuple[int, int, int], list[TriangleRecord]
    ] = {}
    validated_stock_fallbacks = 0
    validated_stock_control = 0
    validated_stock_empty_anchors = 0
    validated_extension_disabled_anchors = 0
    validated_glass_disabled_anchors = 0
    validated_crafting_disabled_anchors = 0
    validated_quantum_disabled_anchors = 0
    validated_m3_completion_disabled_anchors = 0
    validated_native_structural_disabled_anchors = 0
    validated_native_structural_legacy_disabled_anchors = 0
    validated_native_structural_predecessor_rendered_anchors = 0
    validated_native_structural_predecessor_empty_anchors = 0
    validated_m3_completion_stock_rendered_anchors = 0
    validated_m3_completion_stock_empty_anchors = 0
    validated_m45_custom_anchors = 0
    validated_m45_fallback_anchors = 0
    validated_m45_disabled_anchors = 0
    validated_m45_stock_empty_anchors = 0
    validated_m45_stock_nonempty_anchors = 0
    validated_m45_projected_empty_anchors = 0
    validated_m45_legacy_active_anchors = 0
    validated_m45_legacy_projected_anchors = 0
    validated_m45_projected_nonempty_anchors = 0
    for case in cases:
        anchor_results: list[dict[str, Any]] = []
        case_records: list[TriangleRecord] = []
        for anchor in case.anchors:
            records = selected[anchor.position]
            case_records.extend(records)
            is_native_structural_legacy_upgrade = (
                anchor.position
                in gallery.native_structural_legacy_upgrade_positions
            )
            is_m45_legacy_upgrade = anchor.m45_legacy_upgrade is not None
            m45_legacy_projection = m45_legacy_effective_projections.get(
                anchor.position
            )
            retain_legacy_predecessor_regression = (
                (
                    not is_native_structural_legacy_upgrade
                    or native_structural_disabled
                    or stock_baseline
                )
                and (
                    not is_m45_legacy_upgrade
                    or m45_legacy_projection is not None
                )
            )
            is_m2_regression = (
                anchor.position in gallery.m2_regression_positions
                and retain_legacy_predecessor_regression
            )
            is_m3a_regression = (
                anchor.position in gallery.m3a_regression_positions
                and retain_legacy_predecessor_regression
            )
            is_extension = anchor.position in gallery.extension_positions
            is_glass = anchor.position in gallery.glass_positions
            is_crafting = anchor.position in gallery.crafting_positions
            is_quantum = anchor.position in gallery.quantum_positions
            is_m3_completion = anchor.position in gallery.m3_completion_positions
            is_appended_native_structural = (
                anchor.position in gallery.native_structural_positions
            )
            is_native_structural = (
                is_appended_native_structural
                or is_native_structural_legacy_upgrade
            )
            is_m45 = anchor.m45 is not None
            m45_projection = m45_effective_projections.get(anchor.position)
            is_m3b_regression = (
                anchor.position in gallery.m3b_regression_positions
                and retain_legacy_predecessor_regression
            )
            anchor_version = (
                11
                if is_m45_legacy_upgrade and m45_legacy_projection is None
                else 3
                if is_m2_regression
                else 4
                if is_m3a_regression
                else 5
                if is_m3b_regression
                else 6
                if is_glass
                else 7
                if is_crafting
                else 8
                if is_quantum
                else 9
                if is_m3_completion
                else 10
                if is_native_structural
                else 11
                if is_m45
                else gallery.signature_schema_version
            )
            anchor_prefix = (
                f"stock-anchor-v{anchor_version}"
                if stock_baseline
                else f"anchor-v{anchor_version}"
            )
            anchor_scope = (
                f"{anchor_prefix}:{anchor.position[0]},{anchor.position[1]},"
                f"{anchor.position[2]}"
            )
            result = _records_result(records, anchor_scope)
            anchor_contract: dict[str, Any] | None = None
            if is_m45_legacy_upgrade:
                anchor_contract = _validate_m45_legacy_upgrade(
                    anchor, records, result, m45_legacy_projection
                )
                m45_legacy_upgrade_records[anchor.position] = records
                if m45_legacy_projection is None:
                    validated_m45_legacy_active_anchors += 1
                else:
                    validated_m45_legacy_projected_anchors += 1
                    if stock_baseline:
                        validated_stock_empty_anchors += 1
                    if extension_disabled and anchor.position in gallery.extension_positions:
                        validated_extension_disabled_anchors += 1
                    if crafting_disabled and anchor.position in gallery.crafting_positions:
                        validated_crafting_disabled_anchors += 1
            elif is_m45 and m45_projection is not None:
                anchor_contract = _validate_m45_projection(
                    anchor, records, m45_projection
                )
                m45_records[anchor.position] = records
                validated_m45_disabled_anchors += 1
                if m45_projection.review_projection == "nonempty":
                    validated_m45_projected_nonempty_anchors += 1
                else:
                    validated_m45_projected_empty_anchors += 1
                if stock_baseline:
                    anchor_contract["physical_stock_projection"] = True
                    if records:
                        validated_m45_stock_nonempty_anchors += 1
                    else:
                        validated_m45_stock_empty_anchors += 1
                        validated_stock_empty_anchors += 1
            elif stock_baseline:
                if anchor.expected_path == "stock-control":
                    actual_materials = Counter(
                        record.material_identity for record in records
                    )
                    if len(records) != 10 or actual_materials != {
                        "minecraft:block/stone": 10
                    }:
                        raise EvidenceError(
                            f"stock baseline stone control {anchor.position} must own "
                            "exactly 10 minecraft:block/stone triangles"
                        )
                    validated_stock_control += 1
                    anchor_contract = {
                        "triangle_count": 10,
                        "material_triangle_counts": {
                            "minecraft:block/stone": 10
                        },
                        "validated": True,
                    }
                elif is_m3_completion:
                    completion = anchor.m3_completion
                    if completion is None:
                        raise EvidenceError("M3f stock anchor is missing its contract")
                    actual_materials = Counter(
                        record.material_identity for record in records
                    )
                    expected_materials = dict(
                        completion.expected_stock_material_triangles
                    )
                    if (
                        len(records) != completion.expected_stock_triangle_count
                        or actual_materials != expected_materials
                    ):
                        raise EvidenceError(
                            f"stock baseline M3f anchor {anchor.position} differs from its exact original-resource model"
                        )
                    if records:
                        validated_m3_completion_stock_rendered_anchors += 1
                    else:
                        validated_m3_completion_stock_empty_anchors += 1
                    anchor_contract = {
                        "triangle_count": completion.expected_stock_triangle_count,
                        "material_triangle_counts": expected_materials,
                        "profile": M3_COMPLETION_ROUTE,
                        "original_resource_model": True,
                        "validated": True,
                    }
                else:
                    if records:
                        raise EvidenceError(
                            f"stock baseline non-control anchor {anchor.position} owns "
                            f"{len(records)} triangles; expected zero"
                        )
                    validated_stock_empty_anchors += 1
                    anchor_contract = {
                        "triangle_count": 0,
                        "material_triangle_counts": {},
                        "validated": True,
                    }
            elif is_m45:
                m45 = anchor.m45
                if m45 is None:
                    raise EvidenceError("M4/M5 anchor is missing its review contract")
                actual_materials = Counter(
                    record.material_identity for record in records
                )
                selector_exception_resources = (
                    _validate_m45_selector_material_exception(
                        anchor, actual_materials
                    )
                )
                unexpected_resources = sorted(
                    set(actual_materials)
                    - set(m45.allowed_resources)
                    - selector_exception_resources
                )
                if unexpected_resources:
                    raise EvidenceError(
                        f"M4/M5 anchor {anchor.position} emitted resources outside "
                        f"route {m45.route}: {unexpected_resources}"
                    )
                runtime_oracle: dict[str, Any] | None = None
                if anchor.expected_path == "custom-m45":
                    runtime_oracle = _validate_m45_runtime_oracle(
                        anchor.position,
                        records,
                        result,
                        expected_triangle_count=anchor.expected_triangle_count,
                        expected_material_triangles=(
                            anchor.expected_material_triangles
                        ),
                        expected_geometry_signature=(
                            m45.expected_geometry_signature
                        ),
                        expected_nonlighting_attribute_signature=(
                            m45.expected_nonlighting_attribute_signature
                        ),
                        description=f"M4/M5 custom anchor {anchor.position}",
                    )
                    validated_m45_custom_anchors += 1
                elif anchor.expected_path == "stock-fallback-m45":
                    if records:
                        raise EvidenceError(
                            f"M4/M5 atomic fallback anchor {anchor.position} owns "
                            f"{len(records)} triangles; expected zero"
                        )
                    validated_m45_fallback_anchors += 1
                else:
                    raise EvidenceError("unknown M4/M5 anchor projection")
                m45_records[anchor.position] = records
                anchor_contract = {
                    "triangle_count": len(records),
                    "material_triangle_counts": dict(sorted(actual_materials.items())),
                    "m45_route": m45.route,
                    "host_resources": list(m45.host_resources),
                    "review_projection": m45.review_projection,
                    "route_disabled_projections": {
                        route: {
                            "expected_path": projection.expected_path,
                            "review_projection": projection.review_projection,
                            "reason": projection.reason,
                            "allowed_resources": list(
                                projection.allowed_resources
                            ),
                        }
                        for route, projection in m45.route_disabled_projections
                    },
                    "source_derived_synthetic_fixture": (
                        m45.source_derived_synthetic_fixture
                    ),
                    "selector_scoped_model_exception": bool(
                        m45.selector_scoped_exact_material_triangles
                    ),
                    "runtime_oracle": runtime_oracle,
                    "validated": True,
                }
            elif extension_disabled and is_extension:
                if records:
                    raise EvidenceError(
                        f"extension-disabled anchor {anchor.position} owns "
                        f"{len(records)} triangles; expected zero"
                    )
                validated_extension_disabled_anchors += 1
                anchor_contract = {
                    "triangle_count": 0,
                    "material_triangle_counts": {},
                    "extension_profile": "extendedae",
                    "validated": True,
                }
            elif glass_disabled and is_glass:
                if records:
                    raise EvidenceError(
                        f"glass-disabled anchor {anchor.position} owns "
                        f"{len(records)} triangles; expected zero"
                    )
                validated_glass_disabled_anchors += 1
                anchor_contract = {
                    "triangle_count": 0,
                    "material_triangle_counts": {},
                    "profile": "connected-quartz-glass",
                    "validated": True,
                }
            elif crafting_disabled and is_crafting:
                if records:
                    raise EvidenceError(
                        f"crafting-disabled anchor {anchor.position} owns "
                        f"{len(records)} triangles; expected zero"
                    )
                validated_crafting_disabled_anchors += 1
                anchor_contract = {
                    "triangle_count": 0,
                    "material_triangle_counts": {},
                    "profile": "ae2-crafting",
                    "validated": True,
                }
            elif quantum_disabled and is_quantum:
                if records:
                    raise EvidenceError(
                        f"quantum-disabled anchor {anchor.position} owns "
                        f"{len(records)} triangles; expected zero"
                    )
                validated_quantum_disabled_anchors += 1
                anchor_contract = {
                    "triangle_count": 0,
                    "material_triangle_counts": {},
                    "profile": "ae2-quantum-bridge",
                    "validated": True,
                }
            elif m3_completion_disabled and is_m3_completion:
                completion = anchor.m3_completion
                if completion is None:
                    raise EvidenceError("M3f route-disabled anchor is missing its contract")
                actual_materials = Counter(
                    record.material_identity for record in records
                )
                expected_materials = dict(
                    completion.expected_stock_material_triangles
                )
                if (
                    len(records) != completion.expected_stock_triangle_count
                    or actual_materials != expected_materials
                ):
                    raise EvidenceError(
                        f"m3-completion-disabled anchor {anchor.position} differs from its exact original-resource model"
                    )
                validated_m3_completion_disabled_anchors += 1
                if records:
                    validated_m3_completion_stock_rendered_anchors += 1
                else:
                    validated_m3_completion_stock_empty_anchors += 1
                anchor_contract = {
                    "triangle_count": completion.expected_stock_triangle_count,
                    "material_triangle_counts": expected_materials,
                    "profile": M3_COMPLETION_ROUTE,
                    "original_resource_model": True,
                    "validated": True,
                }
            elif native_structural_disabled and is_native_structural:
                predecessor_contract = _validate_schema9_disabled_projection(
                    anchor, records
                )
                if is_native_structural_legacy_upgrade:
                    validated_native_structural_legacy_disabled_anchors += 1
                else:
                    validated_native_structural_disabled_anchors += 1
                    if records:
                        validated_native_structural_predecessor_rendered_anchors += 1
                        custom_records.extend(records)
                    else:
                        validated_native_structural_predecessor_empty_anchors += 1
                anchor_contract = {
                    **predecessor_contract,
                    "profile": NATIVE_STRUCTURAL_ROUTE,
                    "predecessor_route": "accepted-schema9-m1-m2",
                    "validated": True,
                }
            elif anchor.expected_path == "stock-control":
                actual_materials = Counter(
                    record.material_identity for record in records
                )
                if len(records) != 10 or actual_materials != {
                    "minecraft:block/stone": 10
                }:
                    raise EvidenceError(
                        f"enabled stone control {anchor.position} must own exactly "
                        "10 minecraft:block/stone triangles"
                    )
                validated_stock_control += 1
                anchor_contract = {
                    "triangle_count": 10,
                    "material_triangle_counts": {"minecraft:block/stone": 10},
                    "validated": True,
                }
            elif anchor.expected_path in {
                "custom-m1",
                "custom-m2",
                "custom-m3",
                "custom-m3b",
                "custom-m3c",
                "custom-m3d",
                "custom-m3e",
                "custom-m3f",
                "custom-s1",
            }:
                actual_materials = Counter(
                    record.material_identity for record in records
                )
                expected_materials = dict(anchor.expected_material_triangles)
                if len(records) != anchor.expected_triangle_count:
                    raise EvidenceError(
                        f"custom anchor {anchor.position} has {len(records)} triangles; "
                        f"expected {anchor.expected_triangle_count}"
                    )
                if actual_materials != expected_materials:
                    raise EvidenceError(
                        f"custom anchor {anchor.position} material triangle counts "
                        "differ from the exact milestone contract"
                    )
                for (
                    resource_path,
                    expected_rgb,
                    expected_blocklight,
                    expected_sunlight,
                ) in anchor.expected_smart_overlays:
                    overlay_records = [
                        record
                        for record in records
                        if record.material_identity == resource_path
                    ]
                    if not overlay_records or any(
                        any(color != expected_rgb for color in record.colors)
                        or any(
                            light != expected_blocklight
                            for light in record.blocklights
                        )
                        or any(
                            light != expected_sunlight
                            for light in record.sunlights
                        )
                        for record in overlay_records
                    ):
                        raise EvidenceError(
                            f"custom anchor {anchor.position} smart overlay attributes "
                            "differ from the exact M2 contract"
                        )
                terminal_contract = _validate_terminal_contract(anchor, records)
                facade_contract = _validate_active_facade_contract(anchor, records)
                drive_contract = _validate_drive_contract(anchor, records)
                glass_contract = _validate_connected_glass_contract(anchor, records)
                crafting_contract = _validate_crafting_contract(anchor, records)
                quantum_contract = _validate_quantum_contract(anchor, records)
                m3_completion_contract = _validate_m3_completion_contract(
                    anchor, records, settings
                )
                native_structural_contract = _validate_native_structural_contract(
                    anchor, records, result
                )
                custom_records.extend(records)
                anchor_contract = {
                    "triangle_count": anchor.expected_triangle_count,
                    "material_triangle_counts": dict(
                        anchor.expected_material_triangles
                    ),
                    "smart_overlays": {
                        resource_path: {
                            "rgb_u8": list(rgb),
                            "blocklight_raw_i8": blocklight,
                            "sunlight_raw_i8": sunlight,
                        }
                        for resource_path, rgb, blocklight, sunlight in anchor.expected_smart_overlays
                    },
                    "terminal_layers": terminal_contract,
                    "facade": facade_contract,
                    "drive": drive_contract,
                    "connected_glass": glass_contract,
                    "formed_crafting": crafting_contract,
                    "quantum_bridge": quantum_contract,
                    "m3_completion": m3_completion_contract,
                    "native_structural": native_structural_contract,
                    "validated": True,
                }
            elif anchor.expected_path in {
                "stock-fallback-device-endpoint",
                "stock-fallback-m2",
                "stock-fallback-m3",
                "stock-fallback-m3b",
                "stock-fallback-m3d",
                "stock-fallback-m3f",
                "stock-fallback-s1",
            }:
                if records:
                    raise EvidenceError(
                        f"stock fallback anchor {anchor.position} owns "
                        f"{len(records)} triangles; expected zero"
                    )
                validated_stock_fallbacks += 1
                anchor_contract = {
                    "triangle_count": 0,
                    "material_triangle_counts": {},
                    "smart_overlays": {},
                    "fallback_reason": anchor.fallback_reason,
                    "validated": True,
                }
            result["position"] = {
                "x": anchor.position[0],
                "y": anchor.position[1],
                "z": anchor.position[2],
            }
            result["expected_path"] = (
                anchor_contract["expected_path"]
                if is_m45_legacy_upgrade and anchor_contract is not None
                else anchor.expected_path
            )
            result["validation_mode"] = (
                validation_mode
                if is_native_structural or is_m45 or is_m45_legacy_upgrade
                else legacy_validation_mode
            )
            result["native_structural_legacy_upgrade"] = (
                is_native_structural_legacy_upgrade
            )
            result["m45_legacy_upgrade"] = is_m45_legacy_upgrade
            if anchor_contract is not None:
                result["contract"] = anchor_contract
            anchor_results.append(result)
            all_geometry_rows.append(
                f"{anchor_scope}\0{result['geometry_signature']}"
            )
            all_attribute_rows.append(
                f"{anchor_scope}\0{result['attribute_signature']}"
            )
            all_material_rows.append(
                f"{anchor_scope}\0{result['material_signature']}"
            )
            all_shape_rows.append(result["shape_signature"])
            if is_m2_regression:
                m2_regression_records[anchor.position] = records
                m2_regression_rows["geometry"].append(
                    f"{anchor_scope}\0{result['geometry_signature']}"
                )
                m2_regression_rows["attributes"].append(
                    f"{anchor_scope}\0{result['attribute_signature']}"
                )
                m2_regression_rows["materials"].append(
                    f"{anchor_scope}\0{result['material_signature']}"
                )
                m2_regression_rows["shapes"].append(result["shape_signature"])
            if is_m3a_regression:
                m3a_regression_records[anchor.position] = records
                m3a_regression_rows["geometry"].append(
                    f"{anchor_scope}\0{result['geometry_signature']}"
                )
                m3a_regression_rows["attributes"].append(
                    f"{anchor_scope}\0{result['attribute_signature']}"
                )
                m3a_regression_rows["materials"].append(
                    f"{anchor_scope}\0{result['material_signature']}"
                )
                m3a_regression_rows["shapes"].append(result["shape_signature"])
            if is_m3b_regression:
                m3b_regression_records[anchor.position] = records
                m3b_regression_rows["geometry"].append(
                    f"{anchor_scope}\0{result['geometry_signature']}"
                )
                m3b_regression_rows["attributes"].append(
                    f"{anchor_scope}\0{result['attribute_signature']}"
                )
                m3b_regression_rows["materials"].append(
                    f"{anchor_scope}\0{result['material_signature']}"
                )
                m3b_regression_rows["shapes"].append(result["shape_signature"])
            if is_glass:
                glass_records[anchor.position] = records
            if is_crafting:
                crafting_records[anchor.position] = records
            if is_quantum:
                quantum_records[anchor.position] = records
            if is_m3_completion:
                m3_completion_records[anchor.position] = records
            if is_appended_native_structural:
                native_structural_records[anchor.position] = records
            elif is_native_structural_legacy_upgrade:
                native_structural_legacy_upgrade_records[anchor.position] = records
            if (
                gallery.schema_version >= 7
                and not is_crafting
                and not is_quantum
                and not is_m3_completion
                and not is_appended_native_structural
                and not is_m45
                and retain_legacy_predecessor_regression
            ):
                schema6_regression_records[anchor.position] = records
                schema6_regression_rows["geometry"].append(
                    f"{anchor_scope}\0{result['geometry_signature']}"
                )
                schema6_regression_rows["attributes"].append(
                    f"{anchor_scope}\0{result['attribute_signature']}"
                )
                schema6_regression_rows["materials"].append(
                    f"{anchor_scope}\0{result['material_signature']}"
                )
                schema6_regression_rows["shapes"].append(
                    result["shape_signature"]
                )
            if (
                gallery.schema_version >= 8
                and not is_quantum
                and not is_m3_completion
                and not is_appended_native_structural
                and not is_m45
                and retain_legacy_predecessor_regression
            ):
                schema7_regression_records[anchor.position] = records
                schema7_regression_rows["geometry"].append(
                    f"{anchor_scope}\0{result['geometry_signature']}"
                )
                schema7_regression_rows["attributes"].append(
                    f"{anchor_scope}\0{result['attribute_signature']}"
                )
                schema7_regression_rows["materials"].append(
                    f"{anchor_scope}\0{result['material_signature']}"
                )
                schema7_regression_rows["shapes"].append(result["shape_signature"])
            if (
                gallery.schema_version >= 9
                and not is_m3_completion
                and not is_appended_native_structural
                and not is_m45
                and retain_legacy_predecessor_regression
            ):
                schema8_regression_records[anchor.position] = records
                schema8_regression_rows["geometry"].append(
                    f"{anchor_scope}\0{result['geometry_signature']}"
                )
                schema8_regression_rows["attributes"].append(
                    f"{anchor_scope}\0{result['attribute_signature']}"
                )
                schema8_regression_rows["materials"].append(
                    f"{anchor_scope}\0{result['material_signature']}"
                )
                schema8_regression_rows["shapes"].append(result["shape_signature"])
            if anchor.position in gallery.drive_component_pair:
                component_pair_records[anchor.position] = records
            if anchor.position in gallery.extended_drive_component_pair:
                extended_component_pair_records[anchor.position] = records
            if anchor.position in gallery.extended_drive_mirror_pair:
                extended_mirror_pair_records[anchor.position] = records
        case_version = (
            3
            if case.milestone in {"M1", "M2"}
            else 4
            if case.milestone == "M3a"
            else 5
            if case.milestone == "M3b"
            else 6
            if case.milestone == "M3c"
            else 7
            if case.milestone == "M3d"
            else 8
            if case.milestone == "M3e"
            else 9
            if case.milestone == "M3f"
            else 10
            if case.milestone == "S1"
            else 11
            if case.milestone in {"M4", "M5"}
            else gallery.signature_schema_version
        )
        case_prefix = (
            f"stock-case-v{case_version}"
            if stock_baseline
            else f"case-v{case_version}"
        )
        case_scope = f"{case_prefix}:{case.case_id}"
        case_result = _records_result(case_records, case_scope)
        case_result.update(
            {
                "case_id": case.case_id,
                "label": case.label,
                "category": case.category,
                "validation_mode": (
                    validation_mode
                    if case.milestone in {"S1", "M4", "M5"}
                    or any(
                        anchor.position
                        in gallery.native_structural_legacy_upgrade_positions
                        for anchor in case.anchors
                    )
                    or any(
                        anchor.m45_legacy_upgrade is not None
                        for anchor in case.anchors
                    )
                    else legacy_validation_mode
                ),
                "expected_paths": sorted(
                    {anchor.expected_path for anchor in case.anchors}
                ),
                "anchors": anchor_results,
            }
        )
        case_results.append(case_result)

    if gallery.schema_version >= 11:
        m45_missing_materials = {
            position: sum(
                record.material_identity == "bluemap:block/missing"
                for record in records
            )
            for position, records in m45_records.items()
            if any(
                record.material_identity == "bluemap:block/missing"
                for record in records
            )
        }
        if m45_missing_materials != {
            M45_ADVANCED_SINGLETON_POSITION: 32
        }:
            raise EvidenceError(
                "schema-11 M4/M5 missing-material ownership must be exactly "
                "32 fully occluded triangles at the Advanced singleton"
            )

    custom_resource_paths = sorted(
        {record.material_identity for record in custom_records}
    )
    selected_resource_paths = sorted(
        {
            record.material_identity
            for records in selected.values()
            for record in records
        }
    )
    selected_triangle_count = sum(
        len(records) for records in selected.values()
    )
    if gallery.schema_version == 10:
        exact_mode_totals = {
            "enabled": (64_948, 289),
            "native-structural-disabled": (27_198, 218),
            "stock-baseline": (1_882, 5),
        }
        expected_mode_total = exact_mode_totals.get(validation_mode)
        if expected_mode_total is not None and (
            selected_triangle_count,
            len(selected_resource_paths),
        ) != expected_mode_total:
            raise EvidenceError(
                f"schema-10 {validation_mode} selected aggregate changed"
            )
    validated_active_stock_fallbacks = validated_stock_fallbacks + (
        validated_native_structural_legacy_disabled_anchors
        if native_structural_disabled
        else 0
    )
    if gallery.schema_version >= 11 and (
        len(m45_legacy_upgrade_records) != len(M45_LEGACY_UPGRADE_SPECS)
        or validated_m45_legacy_active_anchors
        + validated_m45_legacy_projected_anchors
        != len(M45_LEGACY_UPGRADE_SPECS)
        or validated_m45_legacy_projected_anchors
        != sum(
            projection is not None
            for projection in m45_legacy_effective_projections.values()
        )
    ):
        raise EvidenceError(
            "M4/M5 legacy-upgrade validation closure changed"
        )
    if native_structural_disabled and (
        expected_active_custom_anchor_count != 589
        or expected_active_custom_triangles != 27_188
        or expected_active_fallback_count
        != (14 if gallery.schema_version >= 11 else 17)
    ):
        raise EvidenceError(
            "native-structural-disabled effective aggregate contract changed"
        )
    if stock_baseline:
        expected_empty = (
            len(anchor_lookup)
            - 1
            - len(gallery.m3_completion_positions)
            - expected_m45_projected_nonempty_count
        )
        expected_m3_completion_stock_rendered = 38 if gallery.schema_version >= 9 else 0
        expected_m3_completion_stock_empty = 40 if gallery.schema_version >= 9 else 0
        if (
            validated_stock_control != 1
            or validated_stock_empty_anchors != expected_empty
            or validated_m3_completion_stock_rendered_anchors
            != expected_m3_completion_stock_rendered
            or validated_m3_completion_stock_empty_anchors
            != expected_m3_completion_stock_empty
            or validated_m45_stock_empty_anchors
            != expected_m45_projected_empty_count
            or validated_m45_stock_nonempty_anchors
            != expected_m45_projected_nonempty_count
        ):
            raise EvidenceError(
                f"stock baseline did not validate all {len(anchor_lookup)} exact anchors"
            )
    else:
        if len(custom_records) != expected_active_custom_triangles:
            raise EvidenceError(
                "selected custom triangle total differs from the active-route contract"
            )
        if custom_resource_paths != expected_active_custom_resources:
            raise EvidenceError(
                "selected custom resources differ from the active-route material closure"
            )
        if validated_active_stock_fallbacks != expected_active_fallback_count:
            raise EvidenceError("not all active-route stock fallback anchors were validated")
        if (
            validated_m45_custom_anchors != expected_m45_custom_count
            or validated_m45_fallback_anchors != expected_m45_fallback_count
            or validated_m45_disabled_anchors != expected_m45_disabled_count
            or validated_m45_projected_nonempty_anchors
            != expected_m45_projected_nonempty_count
            or validated_m45_projected_empty_anchors
            != expected_m45_projected_empty_count
        ):
            raise EvidenceError(
                "not all M4/M5 enabled, fallback, and route-disabled anchors were validated"
            )
        expected_disabled_counts = (
            (extension_disabled, validated_extension_disabled_anchors, len(gallery.extension_positions), "extension"),
            (glass_disabled, validated_glass_disabled_anchors, len(gallery.glass_positions), "glass"),
            (crafting_disabled, validated_crafting_disabled_anchors, len(gallery.crafting_positions), "crafting"),
            (quantum_disabled, validated_quantum_disabled_anchors, len(gallery.quantum_positions), "quantum"),
            (native_structural_disabled, validated_native_structural_disabled_anchors, len(gallery.native_structural_positions), "native-structural"),
        )
        for active, actual, expected, label in expected_disabled_counts:
            if active and actual != expected:
                raise EvidenceError(
                    f"{label}-disabled mode validated {actual} anchors; expected {expected}"
                )
        if native_structural_disabled and (
            validated_native_structural_predecessor_rendered_anchors
            != NATIVE_STRUCTURAL_SCHEMA9_DISABLED_RENDERED_ANCHOR_COUNT
            or validated_native_structural_predecessor_empty_anchors
            != NATIVE_STRUCTURAL_SCHEMA9_DISABLED_EMPTY_ANCHOR_COUNT
            or validated_native_structural_legacy_disabled_anchors
            != NATIVE_STRUCTURAL_LEGACY_ANCHOR_COUNT
        ):
            raise EvidenceError(
                "native-structural-disabled predecessor rendered/empty counters changed"
            )
        if m3_completion_disabled and (
            validated_m3_completion_disabled_anchors != 78
            or validated_m3_completion_stock_rendered_anchors != 38
            or validated_m3_completion_stock_empty_anchors != 40
        ):
            raise EvidenceError(
                "m3-completion-disabled mode did not validate all 78 original-resource anchors"
            )

    if not stock_baseline and validated_stock_control != 1:
        raise EvidenceError("enabled profile did not validate the exact stone control")

    dense_result: dict[str, Any] = {"analyzed": False}
    if include_dense:
        dense_records = [
            record
            for position in gallery.dense_positions
            for record in selected[position]
        ]
        actual_dense_materials = Counter(
            record.material_identity for record in dense_records
        )
        if len(dense_records) != gallery.expected_dense_triangle_count:
            raise EvidenceError(
                f"dense fixture has {len(dense_records)} triangles; expected "
                f"{gallery.expected_dense_triangle_count}"
            )
        if actual_dense_materials != dict(gallery.expected_dense_material_triangles):
            raise EvidenceError(
                "dense fixture material triangle counts differ from the exact M1 contract"
            )
        dense_result = _records_result(dense_records, "dense-fixture-v3")
        dense_result.update(
            {
                "analyzed": True,
                "cell_count": len(gallery.dense_positions),
                "contract": {
                    "triangle_count": gallery.expected_dense_triangle_count,
                    "material_triangle_counts": dict(
                        gallery.expected_dense_material_triangles
                    ),
                    "validated": True,
                },
            }
        )

    signature_prefix = "stock-map" if stock_baseline else "map"
    signature_version = gallery.signature_schema_version
    summary: dict[str, Any] = {
        "mode": validation_mode,
        "case_count": len(case_results),
        "anchor_count": len(anchor_lookup),
        "selected_triangle_count": selected_triangle_count,
        "material_signature": sha256_text(
            f"{signature_prefix}-materials-v{signature_version}\n"
            + "\n".join(sorted(all_material_rows))
            + "\n"
        ),
        "shape_signature": sha256_text(
            f"{signature_prefix}-shapes-v{signature_version}\n"
            + "\n".join(sorted(all_shape_rows))
            + "\n"
        ),
        "geometry_signature": sha256_text(
            f"{signature_prefix}-geometry-v{signature_version}\n"
            + "\n".join(sorted(all_geometry_rows))
            + "\n"
        ),
        "attribute_signature": sha256_text(
            f"{signature_prefix}-attributes-v{signature_version}\n"
            + "\n".join(sorted(all_attribute_rows))
            + "\n"
        ),
        "contract_validated": True,
    }
    if gallery.schema_version >= 9:
        summary.update(
            {
                "selected_resource_count": len(selected_resource_paths),
                "selected_resources": selected_resource_paths,
            }
        )
    if stock_baseline:
        summary.update(
            {
                "stock_control_anchor_count": validated_stock_control,
                "stock_control_triangle_count": 10,
                "stock_empty_anchor_count": validated_stock_empty_anchors,
                "stock_empty_triangle_count": 0,
            }
        )
        if gallery.schema_version >= 9:
            summary.update(
                {
                    "m3_completion_stock_rendered_anchor_count": (
                        validated_m3_completion_stock_rendered_anchors
                    ),
                    "m3_completion_stock_empty_anchor_count": (
                        validated_m3_completion_stock_empty_anchors
                    ),
                    "m3_completion_stock_triangle_count": 1_872,
                }
            )
    else:
        summary.update(
            {
                "custom_anchor_count": (
                    expected_active_custom_anchor_count
                ),
                "custom_triangle_count": len(custom_records),
                "custom_selected_resource_count": len(custom_resource_paths),
                "custom_selected_resources": custom_resource_paths,
                "stock_fallback_anchor_count": validated_active_stock_fallbacks,
                "stock_fallback_triangle_count": 0,
                "stock_control_anchor_count": validated_stock_control,
                "stock_control_triangle_count": 10,
            }
        )
        if extension_disabled:
            summary.update(
                {
                    "extension_disabled_anchor_count": (
                        validated_extension_disabled_anchors
                    ),
                    "extension_disabled_triangle_count": 0,
                }
            )
        if glass_disabled:
            summary.update(
                {
                    "glass_disabled_anchor_count": validated_glass_disabled_anchors,
                    "glass_disabled_triangle_count": 0,
                }
            )
        if crafting_disabled:
            summary.update(
                {
                    "crafting_disabled_anchor_count": (
                        validated_crafting_disabled_anchors
                    ),
                    "crafting_disabled_triangle_count": 0,
                }
            )
        if quantum_disabled:
            summary.update(
                {
                    "quantum_disabled_anchor_count": (
                        validated_quantum_disabled_anchors
                    ),
                    "quantum_disabled_triangle_count": 0,
                }
            )
        if m3_completion_disabled:
            summary.update(
                {
                    "m3_completion_disabled_anchor_count": (
                        validated_m3_completion_disabled_anchors
                    ),
                    "m3_completion_stock_rendered_anchor_count": (
                        validated_m3_completion_stock_rendered_anchors
                    ),
                    "m3_completion_stock_empty_anchor_count": (
                        validated_m3_completion_stock_empty_anchors
                    ),
                    "m3_completion_stock_triangle_count": 1_872,
                }
            )
        if native_structural_disabled:
            summary.update(
                {
                    "native_structural_disabled_anchor_count": (
                        validated_native_structural_disabled_anchors
                    ),
                    "native_structural_disabled_triangle_count": (
                        NATIVE_STRUCTURAL_SCHEMA9_DISABLED_TRIANGLE_COUNT
                    ),
                    "native_structural_legacy_disabled_anchor_count": (
                        validated_native_structural_legacy_disabled_anchors
                    ),
                    "native_structural_predecessor_rendered_anchor_count": (
                        validated_native_structural_predecessor_rendered_anchors
                    ),
                    "native_structural_predecessor_empty_anchor_count": (
                        validated_native_structural_predecessor_empty_anchors
                    ),
                }
            )

    m45_legacy_upgrades: dict[str, Any] | None = None
    m45_review: dict[str, Any] | None = None
    if gallery.schema_version >= 11:
        legacy_upgrade_anchors = [
            anchor
            for case in cases
            for anchor in case.anchors
            if anchor.m45_legacy_upgrade is not None
        ]
        legacy_upgrade_records = [
            record
            for anchor in legacy_upgrade_anchors
            for record in m45_legacy_upgrade_records[anchor.position]
        ]
        legacy_upgrade_resources = sorted(
            {record.material_identity for record in legacy_upgrade_records}
        )
        m45_legacy_upgrades = {
            "mode": validation_mode,
            "coverage_id": M45_LEGACY_UPGRADE_COVERAGE,
            "anchor_count": len(legacy_upgrade_anchors),
            "active_anchor_count": validated_m45_legacy_active_anchors,
            "projected_anchor_count": validated_m45_legacy_projected_anchors,
            "triangle_count": len(legacy_upgrade_records),
            "selected_resource_count": len(legacy_upgrade_resources),
            "selected_resources": legacy_upgrade_resources,
            "capture": cases_evidence["m45_legacy_upgrades"]["capture"],
            "oracle": cases_evidence["m45_legacy_upgrades"]["oracle"],
            "observed_baseline": cases_evidence["m45_legacy_upgrades"][
                "summary"
            ],
            "rows": [
                {
                    "case_id": anchor.case_id,
                    "position": dict(
                        zip(("x", "y", "z"), anchor.position)
                    ),
                    "active": m45_legacy_effective_projections[anchor.position]
                    is None,
                    "effective_path": (
                        anchor.m45_legacy_upgrade.enabled_projection.expected_path
                        if m45_legacy_effective_projections[anchor.position] is None
                        else m45_legacy_effective_projections[
                            anchor.position
                        ].expected_path
                    ),
                    "triangle_count": len(
                        m45_legacy_upgrade_records[anchor.position]
                    ),
                    "material_triangle_counts": dict(
                        sorted(
                            Counter(
                                record.material_identity
                                for record in m45_legacy_upgrade_records[
                                    anchor.position
                                ]
                            ).items()
                        )
                    ),
                    "required_m45_routes": list(
                        anchor.m45_legacy_upgrade.required_m45_routes
                    ),
                    "required_legacy_routes": list(
                        anchor.m45_legacy_upgrade.required_legacy_routes
                    ),
                }
                for anchor in legacy_upgrade_anchors
            ],
            "validation_policy": (
                "exact-runtime-map-geometry-material-nonlighting-v11"
            ),
            "contract_validated": True,
        }
        m45_route_results: list[dict[str, Any]] = []
        all_m45_records = [
            record
            for position in gallery.m45_positions
            for record in selected[position]
        ]
        disabled_capture = M45_DISABLED_PROJECTION_EVIDENCE["capture"]
        disabled_capture_match = (
            settings_digest == disabled_capture["settings_sha256"]
            and textures_evidence["compressed_sha256"]
            == disabled_capture["textures"]["compressed_sha256"]
            and textures_evidence["payload_sha256"]
            == disabled_capture["textures"]["payload_sha256"]
            and textures_evidence["texture_count"]
            == disabled_capture["textures"]["texture_count"]
        )
        if m45_disabled and disabled_capture_match:
            m45_capture_resources = {
                record.material_identity for record in all_m45_records
            }
            if (
                len(all_m45_records) != disabled_capture["m45_triangle_count"]
                or len(m45_capture_resources)
                != disabled_capture["m45_resource_count"]
                or selected_triangle_count
                != disabled_capture["full_map_triangle_count"]
                or len(selected_resource_paths)
                != disabled_capture["full_map_resource_count"]
            ):
                raise EvidenceError(
                    "M4/M5 retained combined-disabled capture aggregate changed"
                )
        for route, positions in gallery.m45_route_positions:
            route_records = [
                record for position in positions for record in selected[position]
            ]
            route_anchors = [anchor_lookup[position] for position in positions]
            route_result = _records_result(
                route_records, f"m45-route-v11:{route}"
            )
            plane_observation_active = (
                route == "extendedae-planes"
                and all(
                    m45_effective_projections[position] is None
                    for position in positions
                )
            )
            if plane_observation_active:
                actual_plane_materials = Counter(
                    record.material_identity for record in route_records
                )
                if (
                    len(route_records)
                    != M45_EXTENDED_PLANE_LIVE_OBSERVATION["triangle_count"]
                    or actual_plane_materials
                    != M45_EXTENDED_PLANE_LIVE_OBSERVATION[
                        "material_triangles"
                    ]
                ):
                    raise EvidenceError(
                        "M4/M5 Extended plane enabled live aggregate changed"
                    )
            route_result.update(
                {
                    "route": route,
                    "host_resources": list(
                        route_anchors[0].m45.host_resources
                    ),
                    "enabled_live_observation": (
                        {
                            **M45_EXTENDED_PLANE_LIVE_OBSERVATION,
                            "active": plane_observation_active,
                            "validated": True,
                        }
                        if route == "extendedae-planes"
                        else None
                    ),
                    "anchor_count": len(positions),
                    "custom_review_anchor_count": sum(
                        anchor.expected_path == "custom-m45"
                        for anchor in route_anchors
                    ),
                    "atomic_fallback_anchor_count": sum(
                        anchor.expected_path == "stock-fallback-m45"
                        for anchor in route_anchors
                    ),
                    "nonempty_anchor_count": sum(
                        bool(selected[position]) for position in positions
                    ),
                    "selected_resource_count": len(
                        {record.material_identity for record in route_records}
                    ),
                    "selected_resources": sorted(
                        {record.material_identity for record in route_records}
                    ),
                    "route_disabled": (
                        m45_disabled or route == m45_route_disabled
                    ),
                    "mode_projected_anchor_count": sum(
                        m45_effective_projections[position] is not None
                        for position in positions
                    ),
                    "mode_projected_nonempty_anchor_count": sum(
                        m45_effective_projections[position] is not None
                        and m45_effective_projections[position].review_projection
                        == "nonempty"
                        for position in positions
                    ),
                    "mode_projected_empty_anchor_count": sum(
                        m45_effective_projections[position] is not None
                        and m45_effective_projections[position].review_projection
                        == "empty"
                        for position in positions
                    ),
                    "physical_stock_projection": stock_baseline,
                    "legacy_upgrade_dependency_anchor_count": sum(
                        route in anchor.m45_legacy_upgrade.required_m45_routes
                        for anchor in legacy_upgrade_anchors
                    ),
                    "legacy_upgrade_active_anchor_count": sum(
                        route in anchor.m45_legacy_upgrade.required_m45_routes
                        and m45_legacy_effective_projections[anchor.position]
                        is None
                        for anchor in legacy_upgrade_anchors
                    ),
                    "legacy_upgrade_projected_anchor_count": sum(
                        route in anchor.m45_legacy_upgrade.required_m45_routes
                        and m45_legacy_effective_projections[anchor.position]
                        is not None
                        for anchor in legacy_upgrade_anchors
                    ),
                    "legacy_upgrade_triangle_count": sum(
                        len(m45_legacy_upgrade_records[anchor.position])
                        for anchor in legacy_upgrade_anchors
                        if route
                        in anchor.m45_legacy_upgrade.required_m45_routes
                    ),
                    "contract_validated": True,
                }
            )
            m45_route_results.append(route_result)
        m45_review = {
            "mode": validation_mode,
            "case_count": 8,
            "anchor_count": len(gallery.m45_positions),
            "custom_review_anchor_count": expected_m45_custom_count,
            "atomic_fallback_anchor_count": expected_m45_fallback_count,
            "route_disabled_anchor_count": expected_m45_disabled_count,
            "projected_nonempty_anchor_count": (
                expected_m45_projected_nonempty_count
            ),
            "projected_empty_anchor_count": expected_m45_projected_empty_count,
            "source_derived_synthetic_anchor_count": sum(
                anchor.m45 is not None
                and anchor.m45.source_derived_synthetic_fixture
                for case in cases
                for anchor in case.anchors
            ),
            "triangle_count": len(all_m45_records),
            "legacy_upgrade_anchor_count": len(legacy_upgrade_anchors),
            "legacy_upgrade_active_anchor_count": (
                validated_m45_legacy_active_anchors
            ),
            "legacy_upgrade_projected_anchor_count": (
                validated_m45_legacy_projected_anchors
            ),
            "legacy_upgrade_triangle_count": len(legacy_upgrade_records),
            "selected_resource_count": len(
                {record.material_identity for record in all_m45_records}
            ),
            "selected_resources": sorted(
                {record.material_identity for record in all_m45_records}
            ),
            "routes": m45_route_results,
            "runtime_oracle": {
                "path": M45_RUNTIME_ORACLE_PATH.name,
                "size_bytes": M45_RUNTIME_ORACLE_SIZE_BYTES,
                "sha256": M45_RUNTIME_ORACLE_SHA256,
                "schema_version": 2,
                "signature_schema_version": 11,
                "anchor_count": M45_RUNTIME_ORACLE_ANCHOR_COUNT,
                "triangle_count": M45_RUNTIME_ORACLE_TRIANGLE_COUNT,
                "identity_count": M45_RUNTIME_ORACLE_IDENTITY_COUNT,
                "material_row_count": M45_RUNTIME_ORACLE_MATERIAL_ROW_COUNT,
            },
            "oracle_policy": (
                "exact-runtime-map-geometry-material-nonlighting-v11"
            ),
            "disabled_projection_evidence": M45_DISABLED_PROJECTION_EVIDENCE,
            "disabled_projection_capture_match": disabled_capture_match,
            "physical_formed_fixture_policy": (
                "live-proven-bounded-layouts-fail-closed-on-game-rewrite"
            ),
            "contract_validated": True,
        }
        summary["m45_legacy_upgrade_active_anchor_count"] = (
            validated_m45_legacy_active_anchors
        )
        summary["m45_legacy_upgrade_projected_anchor_count"] = (
            validated_m45_legacy_projected_anchors
        )
        summary["m45_legacy_upgrade_triangle_count"] = len(
            legacy_upgrade_records
        )
        if stock_baseline:
            summary["m45_stock_empty_anchor_count"] = (
                validated_m45_stock_empty_anchors
            )
            summary["m45_stock_nonempty_anchor_count"] = (
                validated_m45_stock_nonempty_anchors
            )
            summary["m45_stock_triangle_count"] = len(all_m45_records)
        else:
            enabled_m45_custom_records = [
                record
                for case in cases
                for anchor in case.anchors
                if anchor.m45 is not None
                and anchor.expected_path == "custom-m45"
                and m45_effective_projections[anchor.position] is None
                for record in selected[anchor.position]
            ]
            projected_m45_records = [
                record
                for position, projection in m45_effective_projections.items()
                if projection is not None
                for record in selected[position]
            ]
            summary["m45_custom_review_anchor_count"] = (
                validated_m45_custom_anchors
            )
            summary["m45_custom_review_triangle_count"] = len(
                enabled_m45_custom_records
            )
            summary["m45_atomic_fallback_anchor_count"] = (
                validated_m45_fallback_anchors
            )
            summary["m45_route_disabled_anchor_count"] = (
                validated_m45_disabled_anchors
            )
            summary["m45_projected_nonempty_anchor_count"] = (
                validated_m45_projected_nonempty_anchors
            )
            summary["m45_projected_empty_anchor_count"] = (
                validated_m45_projected_empty_anchors
            )
            summary["m45_projected_triangle_count"] = len(
                projected_m45_records
            )

    m2_regression: dict[str, Any] | None = None
    m3a_regression: dict[str, Any] | None = None
    drive_component_insensitivity: dict[str, Any] | None = None
    extended_drive_component_insensitivity: dict[str, Any] | None = None
    extended_drive_front_back_mirror: dict[str, Any] | None = None
    m3b_regression: dict[str, Any] | None = None
    connected_glass: dict[str, Any] | None = None
    schema6_regression: dict[str, Any] | None = None
    schema7_regression: dict[str, Any] | None = None
    schema8_regression: dict[str, Any] | None = None
    formed_crafting: dict[str, Any] | None = None
    quantum_bridge: dict[str, Any] | None = None
    m3_completion: dict[str, Any] | None = None
    native_structural: dict[str, Any] | None = None
    native_structural_legacy_upgrades: dict[str, Any] | None = None
    predecessor_upgrade_exclusion_count = (
        len(gallery.native_structural_legacy_upgrade_positions)
        if gallery.schema_version >= 10
        and not native_structural_disabled
        and not stock_baseline
        else 0
    )
    active_m45_legacy_upgrade_positions = {
        position
        for position, projection in m45_legacy_effective_projections.items()
        if projection is None
    }

    def m45_legacy_regression_exclusion(view: str) -> int:
        if gallery.schema_version < 11:
            return 0
        expected_positions = set(
            M45_LEGACY_UPGRADE_REGRESSION_POSITIONS[view]
        )
        if not expected_positions <= set(gallery.m45_legacy_upgrade_positions):
            raise EvidenceError(
                f"M4/M5 legacy-upgrade {view} regression selectors changed"
            )
        return len(expected_positions & active_m45_legacy_upgrade_positions)

    if gallery.schema_version >= 4:
        m2_m45_exclusion_count = m45_legacy_regression_exclusion("m2")
        m2_total_exclusion_count = (
            predecessor_upgrade_exclusion_count + m2_m45_exclusion_count
        )
        expected_m2_regression_anchors = 290 - m2_total_exclusion_count
        if len(m2_regression_records) != expected_m2_regression_anchors:
            raise EvidenceError(
                "normalized M2 regression slice has the wrong anchor closure"
            )
        regression_selected_count = sum(
            len(records) for records in m2_regression_records.values()
        )
        regression_prefix = "stock-map" if stock_baseline else "map"
        m2_regression = {
            "mode": legacy_validation_mode,
            "case_count": 62,
            "anchor_count": expected_m2_regression_anchors,
            **_legacy_upgrade_regression_marker(
                predecessor_upgrade_exclusion_count
            ),
            **_m45_legacy_upgrade_regression_marker(
                m2_m45_exclusion_count
            ),
            "selected_triangle_count": regression_selected_count,
            "material_signature": sha256_text(
                f"{regression_prefix}-materials-v3\n"
                + "\n".join(sorted(m2_regression_rows["materials"]))
                + "\n"
            ),
            "shape_signature": sha256_text(
                f"{regression_prefix}-shapes-v3\n"
                + "\n".join(sorted(m2_regression_rows["shapes"]))
                + "\n"
            ),
            "geometry_signature": sha256_text(
                f"{regression_prefix}-geometry-v3\n"
                + "\n".join(sorted(m2_regression_rows["geometry"]))
                + "\n"
            ),
            "attribute_signature": sha256_text(
                f"{regression_prefix}-attributes-v3\n"
                + "\n".join(sorted(m2_regression_rows["attributes"]))
                + "\n"
            ),
            "contract_validated": True,
        }
        if stock_baseline:
            m2_regression.update(
                {
                    "stock_control_anchor_count": 1,
                    "stock_control_triangle_count": 10,
                    "stock_empty_anchor_count": 289,
                    "stock_empty_triangle_count": 0,
                }
            )
        else:
            regression_custom = [
                record
                for case in cases
                if case.milestone in {"M1", "M2"}
                for anchor in case.anchors
                if anchor.expected_path in {"custom-m1", "custom-m2"}
                for record in m2_regression_records[anchor.position]
            ]
            m2_regression.update(
                {
                    "custom_anchor_count": 278,
                    "custom_triangle_count": len(regression_custom),
                    "custom_selected_resource_count": len(
                        {record.material_identity for record in regression_custom}
                    ),
                    "stock_fallback_anchor_count": (
                        11 - m2_total_exclusion_count
                    ),
                    "stock_fallback_triangle_count": 0,
                }
            )

        if not stock_baseline:
            if len(component_pair_records) != 2:
                raise EvidenceError("Drive component-insensitivity pair is incomplete")
            pair_rows = [
                _records_result(
                    component_pair_records[position],
                    "drive-component-insensitivity-v4",
                )
                for position in gallery.drive_component_pair
            ]
            signature_keys = (
                "shape_signature",
                "material_signature",
            )
            normalized_signatures = [
                _drive_normalized_signature(component_pair_records[position])
                for position in gallery.drive_component_pair
            ]
            if any(
                pair_rows[0][key] != pair_rows[1][key] for key in signature_keys
            ) or normalized_signatures[0] != normalized_signatures[1]:
                raise EvidenceError(
                    "Drive component-bearing and component-free anchors differ"
                )
            drive_component_insensitivity = {
                "validated": True,
                "anchor_count": 2,
                "triangle_count_per_anchor": pair_rows[0]["triangle_count"],
                "normalized_geometry_attribute_signature": normalized_signatures[0],
                **{key: pair_rows[0][key] for key in signature_keys},
            }

    if gallery.schema_version >= 5:
        m3a_m45_exclusion_count = m45_legacy_regression_exclusion("m3a")
        m3a_total_exclusion_count = (
            predecessor_upgrade_exclusion_count + m3a_m45_exclusion_count
        )
        expected_m3a_regression_anchors = 323 - m3a_total_exclusion_count
        if len(m3a_regression_records) != expected_m3a_regression_anchors:
            raise EvidenceError("frozen M3a regression slice has the wrong anchor closure")
        regression_selected_count = sum(
            len(records) for records in m3a_regression_records.values()
        )
        regression_prefix = "stock-map" if stock_baseline else "map"
        m3a_regression = {
            "mode": legacy_validation_mode,
            "case_count": 76,
            "anchor_count": expected_m3a_regression_anchors,
            **_legacy_upgrade_regression_marker(
                predecessor_upgrade_exclusion_count
            ),
            **_m45_legacy_upgrade_regression_marker(
                m3a_m45_exclusion_count
            ),
            "selected_triangle_count": regression_selected_count,
            "material_signature": sha256_text(
                f"{regression_prefix}-materials-v4\n"
                + "\n".join(sorted(m3a_regression_rows["materials"]))
                + "\n"
            ),
            "shape_signature": sha256_text(
                f"{regression_prefix}-shapes-v4\n"
                + "\n".join(sorted(m3a_regression_rows["shapes"]))
                + "\n"
            ),
            "geometry_signature": sha256_text(
                f"{regression_prefix}-geometry-v4\n"
                + "\n".join(sorted(m3a_regression_rows["geometry"]))
                + "\n"
            ),
            "attribute_signature": sha256_text(
                f"{regression_prefix}-attributes-v4\n"
                + "\n".join(sorted(m3a_regression_rows["attributes"]))
                + "\n"
            ),
            "contract_validated": True,
        }
        if stock_baseline:
            m3a_regression.update(
                {
                    "stock_control_anchor_count": 1,
                    "stock_control_triangle_count": 10,
                    "stock_empty_anchor_count": 322,
                    "stock_empty_triangle_count": 0,
                }
            )
        else:
            m3a_custom = [
                record
                for case in cases
                for anchor in case.anchors
                if anchor.position not in gallery.extension_positions
                if anchor.expected_path in {"custom-m1", "custom-m2", "custom-m3"}
                for record in m3a_regression_records[anchor.position]
            ]
            m3a_regression.update(
                {
                    "custom_anchor_count": 310,
                    "custom_triangle_count": len(m3a_custom),
                    "custom_selected_resource_count": len(
                        {record.material_identity for record in m3a_custom}
                    ),
                    "stock_fallback_anchor_count": (
                        12 - m3a_total_exclusion_count
                    ),
                    "stock_fallback_triangle_count": 0,
                }
            )

        if not stock_baseline and not extension_disabled:
            if len(extended_component_pair_records) != 2:
                raise EvidenceError("Extended Drive component-insensitivity pair is incomplete")
            extended_pair_rows = [
                _records_result(
                    extended_component_pair_records[position],
                    "extended-drive-component-insensitivity-v5",
                )
                for position in gallery.extended_drive_component_pair
            ]
            signature_keys = ("shape_signature", "material_signature")
            normalized_signatures = [
                _drive_component_invariant_signature(
                    extended_component_pair_records[position]
                )
                for position in gallery.extended_drive_component_pair
            ]
            if any(
                extended_pair_rows[0][key] != extended_pair_rows[1][key]
                for key in signature_keys
            ) or normalized_signatures[0] != normalized_signatures[1]:
                raise EvidenceError(
                    "Extended Drive component-bearing and component-free anchors differ"
                )
            extended_drive_component_insensitivity = {
                "validated": True,
                "anchor_count": 2,
                "triangle_count_per_anchor": extended_pair_rows[0]["triangle_count"],
                "normalized_geometry_nonlighting_attribute_signature": (
                    normalized_signatures[0]
                ),
                "world_light_policy": "excluded-environment-dependent",
                **{
                    key: extended_pair_rows[0][key]
                    for key in signature_keys
                },
            }
            if len(extended_mirror_pair_records) != 2:
                raise EvidenceError("Extended Drive front/back mirror pair is incomplete")
            mirror_signatures = [
                _drive_face_local_signature(
                    extended_mirror_pair_records[position],
                    next(
                        anchor.drive
                        for case in cases
                        for anchor in case.anchors
                        if anchor.position == position
                    ),
                )
                for position in gallery.extended_drive_mirror_pair
            ]
            if mirror_signatures[0] != mirror_signatures[1]:
                raise EvidenceError("Extended Drive front/back face-local geometry differs")
            extended_drive_front_back_mirror = {
                "validated": True,
                "anchor_count": 2,
                "triangle_count_per_anchor": len(
                    extended_mirror_pair_records[
                        gallery.extended_drive_mirror_pair[0]
                    ]
                ),
                "face_local_geometry_attribute_signature": mirror_signatures[0],
            }

    if gallery.schema_version >= 6:
        m3b_m45_exclusion_count = m45_legacy_regression_exclusion("m3b")
        m3b_total_exclusion_count = (
            predecessor_upgrade_exclusion_count + m3b_m45_exclusion_count
        )
        expected_m3b_regression_anchors = 359 - m3b_total_exclusion_count
        if len(m3b_regression_records) != expected_m3b_regression_anchors:
            raise EvidenceError("frozen M3b regression slice has the wrong anchor closure")
        regression_prefix = "stock-map" if stock_baseline else "map"
        m3b_regression = {
            "mode": legacy_validation_mode,
            "case_count": 92,
            "anchor_count": expected_m3b_regression_anchors,
            **_legacy_upgrade_regression_marker(
                predecessor_upgrade_exclusion_count
            ),
            **_m45_legacy_upgrade_regression_marker(
                m3b_m45_exclusion_count
            ),
            "selected_triangle_count": sum(
                len(records) for records in m3b_regression_records.values()
            ),
            "material_signature": sha256_text(
                f"{regression_prefix}-materials-v5\n"
                + "\n".join(sorted(m3b_regression_rows["materials"]))
                + "\n"
            ),
            "shape_signature": sha256_text(
                f"{regression_prefix}-shapes-v5\n"
                + "\n".join(sorted(m3b_regression_rows["shapes"]))
                + "\n"
            ),
            "geometry_signature": sha256_text(
                f"{regression_prefix}-geometry-v5\n"
                + "\n".join(sorted(m3b_regression_rows["geometry"]))
                + "\n"
            ),
            "attribute_signature": sha256_text(
                f"{regression_prefix}-attributes-v5\n"
                + "\n".join(sorted(m3b_regression_rows["attributes"]))
                + "\n"
            ),
            "contract_validated": True,
        }
        if stock_baseline:
            m3b_regression.update(
                {
                    "stock_control_anchor_count": 1,
                    "stock_control_triangle_count": 10,
                    "stock_empty_anchor_count": 358,
                    "stock_empty_triangle_count": 0,
                }
            )
        elif extension_disabled:
            m3b_regression.update(
                {
                    "custom_anchor_count": 310,
                    "custom_triangle_count": 12_432,
                    "custom_selected_resource_count": 159,
                    "stock_fallback_anchor_count": (
                        12 - m3b_total_exclusion_count
                    ),
                    "stock_fallback_triangle_count": 0,
                    "extension_disabled_anchor_count": 36,
                    "extension_disabled_triangle_count": 0,
                }
            )
        else:
            m3b_regression.update(
                {
                    "custom_anchor_count": 342,
                    "custom_triangle_count": 17_488,
                    "custom_selected_resource_count": 167,
                    "stock_fallback_anchor_count": (
                        16 - m3b_total_exclusion_count
                    ),
                    "stock_fallback_triangle_count": 0,
                }
            )

        if not stock_baseline and not glass_disabled:
            if len(glass_records) != 47 or sum(map(len, glass_records.values())) != 776:
                raise EvidenceError("connected-glass validation slice is incomplete")
            pair_positions = ((208, 100, 288), (244, 100, 288))
            pair_rows = [
                _records_result(glass_records[position], "connected-glass-variant-v6")
                for position in pair_positions
            ]
            for key in ("triangle_count", "shape_signature", "material_signature", "geometry_signature"):
                if pair_rows[0][key] != pair_rows[1][key]:
                    raise EvidenceError("ordinary/vibrant matched glass topology differs")
            nonlighting_pair = [
                _connected_glass_nonlighting_signature(glass_records[position])
                for position in pair_positions
            ]
            if nonlighting_pair[0] != nonlighting_pair[1]:
                raise EvidenceError("ordinary/vibrant nonlighting glass outputs differ")
            all_glass_records = [
                record
                for position in gallery.glass_positions
                for record in glass_records[position]
            ]
            connected_glass = {
                "validated": True,
                "case_count": 11,
                "anchor_count": 47,
                "triangle_count": 776,
                "selected_resource_count": len(
                    {record.material_identity for record in all_glass_records}
                ),
                "frame_mask_occurrences": CONNECTED_GLASS_FRAME_OCCURRENCES,
                "no_frame_face_count": 2,
                "variant_equivalence": {
                    "validated": True,
                    "anchor_count": 2,
                    "triangle_count_per_anchor": 24,
                    "nonlighting_signature": nonlighting_pair[0],
                    "shape_signature": pair_rows[0]["shape_signature"],
                    "material_signature": pair_rows[0]["material_signature"],
                    "geometry_signature": pair_rows[0]["geometry_signature"],
                    "ordinary_attribute_signature": pair_rows[0]["attribute_signature"],
                    "vibrant_attribute_signature": pair_rows[1]["attribute_signature"],
                    "world_light_excluded": True,
                },
                "nonlighting_topology_signature": (
                    _connected_glass_nonlighting_signature(all_glass_records)
                ),
                "world_light_policy": "world-derived-with-vibrant-center-emission-floor-15",
            }

    if gallery.schema_version >= 7:
        schema6_m45_exclusion_count = m45_legacy_regression_exclusion(
            "schema6"
        )
        schema6_total_exclusion_count = (
            predecessor_upgrade_exclusion_count
            + schema6_m45_exclusion_count
        )
        expected_schema6_regression_anchors = (
            406 - schema6_total_exclusion_count
        )
        if len(schema6_regression_records) != expected_schema6_regression_anchors:
            raise EvidenceError(
                "frozen schema-6 regression view does not contain 406 anchors"
            )
        regression_prefix = "stock-map" if stock_baseline else "map"
        schema6_custom_records = [
            record
            for case in cases
            if case.milestone != "M3d"
            for anchor in case.anchors
            if anchor.expected_path
            in {"custom-m1", "custom-m2", "custom-m3", "custom-m3b", "custom-m3c"}
            for record in schema6_regression_records[anchor.position]
        ]
        selected_triangle_count = sum(
            len(records) for records in schema6_regression_records.values()
        )
        expected_selected = (
            10
            if stock_baseline
            else 13_218
            if extension_disabled
            else 17_498
            if glass_disabled
            else 18_274
        )
        if selected_triangle_count != expected_selected:
            raise EvidenceError(
                "schema-7 frozen schema-6 regression triangle total changed"
            )
        schema6_regression = {
            "mode": legacy_validation_mode,
            "schema_version": 6,
            "signature_schema_version": 6,
            "cases_manifest_sha256": (
                "2d4fbba58ea2c4d3ed741e93a8dd9857523cac9cda021ffd3111e6ac51aec602"
            ),
            "case_count": 103,
            "anchor_count": expected_schema6_regression_anchors,
            **_legacy_upgrade_regression_marker(
                predecessor_upgrade_exclusion_count
            ),
            **_m45_legacy_upgrade_regression_marker(
                schema6_m45_exclusion_count
            ),
            "selected_triangle_count": selected_triangle_count,
            "material_signature": sha256_text(
                f"{regression_prefix}-materials-v6\n"
                + "\n".join(sorted(schema6_regression_rows["materials"]))
                + "\n"
            ),
            "shape_signature": sha256_text(
                f"{regression_prefix}-shapes-v6\n"
                + "\n".join(sorted(schema6_regression_rows["shapes"]))
                + "\n"
            ),
            "geometry_signature": sha256_text(
                f"{regression_prefix}-geometry-v6\n"
                + "\n".join(sorted(schema6_regression_rows["geometry"]))
                + "\n"
            ),
            "attribute_signature": sha256_text(
                f"{regression_prefix}-attributes-v6\n"
                + "\n".join(sorted(schema6_regression_rows["attributes"]))
                + "\n"
            ),
            "contract_validated": True,
        }
        if stock_baseline:
            schema6_regression.update(
                {
                    "stock_control_anchor_count": 1,
                    "stock_control_triangle_count": 10,
                    "stock_empty_anchor_count": 405,
                    "stock_empty_triangle_count": 0,
                }
            )
        else:
            schema6_regression.update(
                {
                    "custom_anchor_count": (
                        357
                        if extension_disabled
                        else 342
                        if glass_disabled
                        else 389
                    ),
                    "custom_triangle_count": len(schema6_custom_records),
                    "custom_selected_resource_count": len(
                        {
                            record.material_identity
                            for record in schema6_custom_records
                        }
                    ),
                    "stock_fallback_anchor_count": (
                        (12 if extension_disabled else 16)
                        - schema6_total_exclusion_count
                    ),
                    "stock_fallback_triangle_count": 0,
                }
            )
            if extension_disabled:
                schema6_regression.update(
                    {
                        "extension_disabled_anchor_count": 36,
                        "extension_disabled_triangle_count": 0,
                    }
                )
            if glass_disabled:
                schema6_regression.update(
                    {
                        "glass_disabled_anchor_count": 47,
                        "glass_disabled_triangle_count": 0,
                    }
                )

        if len(crafting_records) != 86:
            raise EvidenceError("M3d crafting evidence slice does not contain 86 anchors")
        crafting_custom_anchors = [
            anchor
            for case in cases
            if case.milestone == "M3d"
            for anchor in case.anchors
            if anchor.expected_path == "custom-m3d"
        ]
        nonzero_custom = sum(
            bool(crafting_records[anchor.position])
            for anchor in crafting_custom_anchors
        )
        crafting_triangle_count = sum(
            len(crafting_records[anchor.position])
            for anchor in crafting_custom_anchors
        )
        if stock_baseline or crafting_disabled:
            if nonzero_custom != 0 or crafting_triangle_count != 0:
                raise EvidenceError("disabled/stock M3d crafting route emitted geometry")
        elif nonzero_custom != 84 or crafting_triangle_count != 4_306:
            raise EvidenceError(
                "enabled M3d route activity differs from 84 nonzero custom anchors"
            )
        formed_crafting = {
            "mode": legacy_validation_mode,
            "route": CRAFTING_ROUTE,
            "case_count": 9,
            "anchor_count": 86,
            "custom_anchor_count": 85,
            "nonzero_custom_anchor_count": nonzero_custom,
            "custom_triangle_count": crafting_triangle_count,
            "stock_fallback_anchor_count": (
                0 if crafting_disabled or stock_baseline else 1
            ),
            "route_disabled_anchor_count": (
                86 if crafting_disabled else 0
            ),
            "route_activity_evidence": (
                "route-disabled-all-empty"
                if crafting_disabled
                else "stock-addon-absent-all-empty"
                if stock_baseline
                else "84-nonzero-custom-anchors-plus-route-disabled-comparison"
            ),
            "fully_enclosed_zero_geometry": {
                "position": {"x": 305, "y": 101, "z": 270},
                "triangle_count": 0,
                "evidence_status": "fully-enclosed-zero-geometry",
                "renderer_provenance_status": (
                    "not-renderer-provenance-distinguishable-in-prbm"
                ),
            },
            "monitor_display_policy": CRAFTING_MONITOR_DISPLAY_POLICY,
            "contract_validated": True,
        }

    if gallery.schema_version >= 8:
        schema7_m45_exclusion_count = m45_legacy_regression_exclusion(
            "schema7"
        )
        schema7_total_exclusion_count = (
            predecessor_upgrade_exclusion_count
            + schema7_m45_exclusion_count
        )
        expected_schema7_regression_anchors = (
            492 - schema7_total_exclusion_count
        )
        if len(schema7_regression_records) != expected_schema7_regression_anchors:
            raise EvidenceError(
                "frozen schema-7 regression view does not contain 492 anchors"
            )
        regression_prefix = "stock-map" if stock_baseline else "map"
        schema7_custom_records = [
            record
            for case in cases
            if case.milestone not in {"M3e", "M3f"}
            for anchor in case.anchors
            if anchor.expected_path
            in {
                "custom-m1",
                "custom-m2",
                "custom-m3",
                "custom-m3b",
                "custom-m3c",
                "custom-m3d",
            }
            for record in schema7_regression_records[anchor.position]
        ]
        schema7_selected_triangle_count = sum(
            len(records) for records in schema7_regression_records.values()
        )
        expected_schema7_selected = (
            10
            if stock_baseline
            else 17_524
            if extension_disabled
            else 21_804
            if glass_disabled
            else 18_274
            if crafting_disabled
            else 22_580
        )
        if schema7_selected_triangle_count != expected_schema7_selected:
            raise EvidenceError(
                "schema-8 frozen schema-7 regression triangle total changed"
            )
        schema7_effective_mode = (
            "enabled"
            if quantum_disabled or m3_completion_disabled or native_structural_disabled
            else validation_mode
        )
        schema7_regression = {
            "mode": schema7_effective_mode,
            "schema_version": 7,
            "signature_schema_version": 7,
            "cases_manifest_sha256": SCHEMA7_CANONICAL_SHA256,
            "case_count": 112,
            "anchor_count": expected_schema7_regression_anchors,
            **_legacy_upgrade_regression_marker(
                predecessor_upgrade_exclusion_count
            ),
            **_m45_legacy_upgrade_regression_marker(
                schema7_m45_exclusion_count
            ),
            "selected_triangle_count": schema7_selected_triangle_count,
            "material_signature": sha256_text(
                f"{regression_prefix}-materials-v7\n"
                + "\n".join(sorted(schema7_regression_rows["materials"]))
                + "\n"
            ),
            "shape_signature": sha256_text(
                f"{regression_prefix}-shapes-v7\n"
                + "\n".join(sorted(schema7_regression_rows["shapes"]))
                + "\n"
            ),
            "geometry_signature": sha256_text(
                f"{regression_prefix}-geometry-v7\n"
                + "\n".join(sorted(schema7_regression_rows["geometry"]))
                + "\n"
            ),
            "attribute_signature": sha256_text(
                f"{regression_prefix}-attributes-v7\n"
                + "\n".join(sorted(schema7_regression_rows["attributes"]))
                + "\n"
            ),
            "contract_validated": True,
        }
        if stock_baseline:
            schema7_regression.update(
                {
                    "stock_control_anchor_count": 1,
                    "stock_control_triangle_count": 10,
                    "stock_empty_anchor_count": 491,
                    "stock_empty_triangle_count": 0,
                }
            )
        else:
            expected_schema7_custom_anchors = (
                442
                if extension_disabled
                else 427
                if glass_disabled
                else 389
                if crafting_disabled
                else 474
            )
            expected_schema7_resources = (
                193
                if extension_disabled
                else 182
                if glass_disabled
                else 186
                if crafting_disabled
                else 201
            )
            actual_schema7_resources = len(
                {
                    record.material_identity
                    for record in schema7_custom_records
                }
            )
            if (
                len(schema7_custom_records) != schema7_selected_triangle_count - 10
                or actual_schema7_resources != expected_schema7_resources
            ):
                raise EvidenceError(
                    "schema-8 frozen schema-7 custom resource/triangle closure changed"
                )
            schema7_regression.update(
                {
                    "custom_anchor_count": expected_schema7_custom_anchors,
                    "custom_triangle_count": len(schema7_custom_records),
                    "custom_selected_resource_count": actual_schema7_resources,
                    "stock_fallback_anchor_count": (
                        (13
                        if extension_disabled
                        else 16
                        if crafting_disabled
                        else 17)
                        - schema7_total_exclusion_count
                    ),
                    "stock_fallback_triangle_count": 0,
                }
            )
            if extension_disabled:
                schema7_regression.update(
                    {
                        "extension_disabled_anchor_count": 36,
                        "extension_disabled_triangle_count": 0,
                    }
                )
            if glass_disabled:
                schema7_regression.update(
                    {
                        "glass_disabled_anchor_count": 47,
                        "glass_disabled_triangle_count": 0,
                    }
                )
            if crafting_disabled:
                schema7_regression.update(
                    {
                        "crafting_disabled_anchor_count": 86,
                        "crafting_disabled_triangle_count": 0,
                    }
                )

        if len(quantum_records) != 27:
            raise EvidenceError("M3e quantum evidence slice does not contain 27 anchors")
        quantum_custom_anchors = [
            anchor
            for case in cases
            if case.milestone == "M3e"
            for anchor in case.anchors
            if anchor.expected_path == "custom-m3e"
        ]
        quantum_nonzero_custom = sum(
            bool(quantum_records[anchor.position])
            for anchor in quantum_custom_anchors
        )
        quantum_triangle_count = sum(
            len(quantum_records[anchor.position])
            for anchor in quantum_custom_anchors
        )
        if stock_baseline or quantum_disabled:
            if quantum_nonzero_custom != 0 or quantum_triangle_count != 0:
                raise EvidenceError("disabled/stock M3e quantum route emitted geometry")
        elif quantum_nonzero_custom != 27 or quantum_triangle_count != 1_188:
            raise EvidenceError(
                "enabled M3e route activity differs from 27 nonzero custom anchors"
            )
        quantum_bridge = {
            "mode": legacy_validation_mode,
            "route": QUANTUM_ROUTE,
            "case_count": 3,
            "anchor_count": 27,
            "custom_anchor_count": 27,
            "nonzero_custom_anchor_count": quantum_nonzero_custom,
            "custom_triangle_count": quantum_triangle_count,
            "stock_fallback_anchor_count": 0,
            "route_disabled_anchor_count": 27 if quantum_disabled else 0,
            "route_activity_evidence": (
                "route-disabled-all-empty"
                if quantum_disabled
                else "stock-addon-absent-all-empty"
                if stock_baseline
                else "three-complete-formed-static-off-bridges"
            ),
            "plane_orientations": list(QUANTUM_PLANES),
            "topology": "complete-isolated-three-by-three-plane",
            "per_bridge": {
                "anchor_count": 9,
                "triangle_count": 396,
                "material_triangle_counts": {
                    QUANTUM_LINK_RESOURCE: 12,
                    QUANTUM_GLASS_RESOURCE: 48,
                    QUANTUM_COVERED_RESOURCE: 144,
                    QUANTUM_RING_RESOURCE: 192,
                },
                "roles": {
                    "link": {"count": 1, "triangle_count_each": 108},
                    "corner": {"count": 4, "triangle_count_each": 36},
                    "edge": {"count": 4, "triangle_count_each": 36},
                },
            },
            "chunk_boundary_fixture": {
                "plane": "xz",
                "crossed_x_boundary": [287, 288],
                "link_position": {"x": 287, "y": 100, "z": 271},
                "validated": True,
            },
            "power_overlay_policy": QUANTUM_STATIC_POLICY,
            "particle_policy": QUANTUM_PARTICLE_POLICY,
            "waterlogged_fixture_policy": "dry-only-host-water-pass-outside-slice",
            "contract_validated": True,
        }

    if gallery.schema_version >= 9:
        schema8_m45_exclusion_count = m45_legacy_regression_exclusion(
            "schema8"
        )
        schema8_total_exclusion_count = (
            predecessor_upgrade_exclusion_count
            + schema8_m45_exclusion_count
        )
        expected_schema8_regression_anchors = (
            519 - schema8_total_exclusion_count
        )
        if len(schema8_regression_records) != expected_schema8_regression_anchors:
            raise EvidenceError(
                "frozen schema-8 regression view does not contain 519 anchors"
            )
        schema8_selected_triangle_count = sum(
            len(records) for records in schema8_regression_records.values()
        )
        expected_schema8_selected = (
            10
            if stock_baseline
            else 18_712
            if extension_disabled
            else 22_992
            if glass_disabled
            else 19_462
            if crafting_disabled
            else 22_580
            if quantum_disabled
            else 23_768
        )
        if schema8_selected_triangle_count != expected_schema8_selected:
            raise EvidenceError("schema-9 frozen schema-8 regression triangle total changed")
        regression_prefix = "stock-map" if stock_baseline else "map"
        schema8_effective_mode = (
            "enabled"
            if m3_completion_disabled or native_structural_disabled
            else validation_mode
        )
        schema8_regression = {
            "mode": schema8_effective_mode,
            "schema_version": 8,
            "signature_schema_version": 8,
            "cases_manifest_sha256": SCHEMA8_CANONICAL_SHA256,
            "case_count": 115,
            "anchor_count": expected_schema8_regression_anchors,
            **_legacy_upgrade_regression_marker(
                predecessor_upgrade_exclusion_count
            ),
            **_m45_legacy_upgrade_regression_marker(
                schema8_m45_exclusion_count
            ),
            "selected_triangle_count": schema8_selected_triangle_count,
            "material_signature": sha256_text(
                f"{regression_prefix}-materials-v8\n"
                + "\n".join(sorted(schema8_regression_rows["materials"]))
                + "\n"
            ),
            "shape_signature": sha256_text(
                f"{regression_prefix}-shapes-v8\n"
                + "\n".join(sorted(schema8_regression_rows["shapes"]))
                + "\n"
            ),
            "geometry_signature": sha256_text(
                f"{regression_prefix}-geometry-v8\n"
                + "\n".join(sorted(schema8_regression_rows["geometry"]))
                + "\n"
            ),
            "attribute_signature": sha256_text(
                f"{regression_prefix}-attributes-v8\n"
                + "\n".join(sorted(schema8_regression_rows["attributes"]))
                + "\n"
            ),
            "contract_validated": True,
        }
        if stock_baseline:
            schema8_regression.update(
                {
                    "stock_control_anchor_count": 1,
                    "stock_control_triangle_count": 10,
                    "stock_empty_anchor_count": 518,
                    "stock_empty_triangle_count": 0,
                }
            )
        else:
            schema8_custom_records = [
                record
                for case in cases
                if case.milestone not in {"M3f", "S1", "M4", "M5"}
                for anchor in case.anchors
                if anchor.position
                not in gallery.native_structural_legacy_upgrade_positions
                if anchor.expected_path.startswith("custom-")
                for record in schema8_regression_records[anchor.position]
            ]
            expected_custom_anchors = (
                469
                if extension_disabled
                else 454
                if glass_disabled
                else 416
                if crafting_disabled
                else 474
                if quantum_disabled
                else 501
            )
            expected_resources = (
                195
                if extension_disabled
                else 184
                if glass_disabled
                else 188
                if crafting_disabled
                else 201
                if quantum_disabled
                else 203
            )
            actual_resources = len(
                {record.material_identity for record in schema8_custom_records}
            )
            if (
                len(schema8_custom_records) != schema8_selected_triangle_count - 10
                or actual_resources != expected_resources
            ):
                raise EvidenceError(
                    "schema-9 frozen schema-8 custom resource/triangle closure changed"
                )
            schema8_regression.update(
                {
                    "custom_anchor_count": expected_custom_anchors,
                    "custom_triangle_count": len(schema8_custom_records),
                    "custom_selected_resource_count": actual_resources,
                    "stock_fallback_anchor_count": (
                        (13
                        if extension_disabled
                        else 16
                        if crafting_disabled
                        else 17)
                        - schema8_total_exclusion_count
                    ),
                    "stock_fallback_triangle_count": 0,
                }
            )
            if extension_disabled:
                schema8_regression["extension_disabled_anchor_count"] = 36
            if glass_disabled:
                schema8_regression["glass_disabled_anchor_count"] = 47
            if crafting_disabled:
                schema8_regression["crafting_disabled_anchor_count"] = 86
            if quantum_disabled:
                schema8_regression["quantum_disabled_anchor_count"] = 27

        if len(m3_completion_records) != 78:
            raise EvidenceError("M3f completion evidence slice does not contain 78 anchors")
        completion_anchors = [
            anchor
            for case in cases
            if case.milestone == "M3f"
            for anchor in case.anchors
        ]
        completion_custom = [
            anchor for anchor in completion_anchors if anchor.expected_path == "custom-m3f"
        ]
        route_records = [
            record
            for anchor in completion_anchors
            for record in m3_completion_records[anchor.position]
        ]
        route_resources = sorted({record.material_identity for record in route_records})
        if stock_baseline or m3_completion_disabled:
            if (
                len(route_records) != 1_872
                or len(route_resources) != 4
                or set(route_resources) != set(M3_COMPLETION_STOCK_RESOURCES)
                or validated_m3_completion_stock_rendered_anchors != 38
                or validated_m3_completion_stock_empty_anchors != 40
            ):
                raise EvidenceError("M3f original-resource route projection changed")
            completion_custom_triangle_count = 0
            completion_nonzero_custom = 0
            completion_fallback_count = 0
        else:
            completion_custom_triangle_count = sum(
                len(m3_completion_records[anchor.position])
                for anchor in completion_custom
            )
            completion_nonzero_custom = sum(
                bool(m3_completion_records[anchor.position])
                for anchor in completion_custom
            )
            if (
                completion_custom_triangle_count != 2_822
                or completion_nonzero_custom != 78
                or len(route_records) != 2_822
                or route_resources != sorted(M3_COMPLETION_RESOURCES)
            ):
                raise EvidenceError("enabled M3f completion route activity changed")
            completion_fallback_count = 0
        m3_completion = {
            "mode": legacy_validation_mode,
            "route": M3_COMPLETION_ROUTE,
            "profile_sha256": M3_COMPLETION_PROFILE_SHA256,
            "case_count": 7,
            "anchor_count": 78,
            "custom_anchor_count": 78,
            "nonzero_custom_anchor_count": completion_nonzero_custom,
            "custom_triangle_count": completion_custom_triangle_count,
            "stock_fallback_anchor_count": completion_fallback_count,
            "route_disabled_anchor_count": (
                78 if m3_completion_disabled else 0
            ),
            "original_resource_projection": {
                "active": stock_baseline or m3_completion_disabled,
                "rendered_anchor_count": (
                    validated_m3_completion_stock_rendered_anchors
                ),
                "empty_anchor_count": validated_m3_completion_stock_empty_anchors,
                "triangle_count": 1_872 if stock_baseline or m3_completion_disabled else 0,
                "selected_resource_count": (
                    4 if stock_baseline or m3_completion_disabled else 0
                ),
                "resources": (
                    list(M3_COMPLETION_STOCK_RESOURCES)
                    if stock_baseline or m3_completion_disabled
                    else []
                ),
            },
            "route_selected_resource_count": len(route_resources),
            "route_selected_resources": route_resources,
            "paint": {
                "anchor_count": 23,
                "splotch_count": 25,
                "triangle_count": 50 if not (stock_baseline or m3_completion_disabled) else 0,
                "durable_dots_byte_length": 256,
                "lumen_covered": False,
            },
            "sky_chests": {"anchor_count": 8, "pose": "closed"},
            "crank": {"anchor_count": 6, "pose": "neutral-zero-degrees"},
            "inscriber": {
                "anchor_count": 24,
                "pose": "neutral-no-items-no-animation",
            },
            "spatial_pylon": {
                "custom_anchor_count": 17,
                "fallback_anchor_count": 0,
                "topology": (
                    "bounded-locally-invalid-component-unformed-base-plus-dim"
                ),
                "maximum_axis_scan_blocks": 256,
                "incomplete_missing_malformed_or_capped": (
                    "atomic-original-resource-fallback"
                ),
            },
            "excluded": "contents-items-fluids-machine-activity",
            "contract_validated": True,
        }

    if gallery.schema_version >= 10:
        if len(native_structural_records) != NATIVE_STRUCTURAL_ANCHOR_COUNT:
            raise EvidenceError("S1 native structural evidence slice is incomplete")
        structural_anchors = [
            anchor
            for case in cases
            if case.milestone == "S1"
            for anchor in case.anchors
        ]
        structural_custom = [
            anchor for anchor in structural_anchors if anchor.expected_path == "custom-s1"
        ]
        structural_route_records = [
            record
            for anchor in structural_anchors
            for record in native_structural_records[anchor.position]
        ]
        structural_resources = sorted(
            {record.material_identity for record in structural_route_records}
        )
        glassential_override_evidence: dict[str, Any] = {
            **NATIVE_STRUCTURAL_GLASSENTIAL_FULL_PACK_OVERRIDE,
            "active": False,
            "anchor_count": 0,
            "triangle_count": 0,
            "vanilla_glass_triangle_count": 0,
            "anchor_triangle_counts": [],
            "validated": True,
        }
        if stock_baseline:
            if structural_route_records:
                raise EvidenceError("S1 physical stock projection must be empty at all 360 anchors")
            structural_custom_triangle_count = 0
            structural_nonzero_custom = 0
            structural_fallback_count = 0
        elif native_structural_disabled:
            if (
                len(structural_route_records)
                != NATIVE_STRUCTURAL_SCHEMA9_DISABLED_TRIANGLE_COUNT
                or structural_resources
                != list(NATIVE_STRUCTURAL_SCHEMA9_DISABLED_RESOURCES)
                or validated_native_structural_predecessor_rendered_anchors
                != NATIVE_STRUCTURAL_SCHEMA9_DISABLED_RENDERED_ANCHOR_COUNT
                or validated_native_structural_predecessor_empty_anchors
                != NATIVE_STRUCTURAL_SCHEMA9_DISABLED_EMPTY_ANCHOR_COUNT
            ):
                raise EvidenceError("S1 schema-9 predecessor projection changed")
            structural_custom_triangle_count = 0
            structural_nonzero_custom = 0
            structural_fallback_count = 0
        else:
            structural_custom_triangle_count = sum(
                len(native_structural_records[anchor.position])
                for anchor in structural_custom
            )
            structural_nonzero_custom = sum(
                bool(native_structural_records[anchor.position])
                for anchor in structural_custom
            )
            expected_structural_triangles = sum(
                anchor.expected_triangle_count or 0 for anchor in structural_custom
            )
            if (
                structural_custom_triangle_count != expected_structural_triangles
                or structural_nonzero_custom != 351
            ):
                raise EvidenceError("enabled S1 native structural route activity changed")
            structural_fallback_count = 9
            glassential_override_evidence = (
                _validate_native_structural_glassential_closure(
                    native_structural_records
                )
            )
        native_structural = {
            "mode": validation_mode,
            "coverage_id": NATIVE_STRUCTURAL_COVERAGE,
            "route": NATIVE_STRUCTURAL_ROUTE,
            "resource_manifest_sha256": NATIVE_STRUCTURAL_RESOURCE_MANIFEST_SHA256,
            "case_count": 28,
            "anchor_count": 360,
            "custom_anchor_count": 351,
            "nonzero_custom_anchor_count": structural_nonzero_custom,
            "custom_triangle_count": structural_custom_triangle_count,
            "stock_fallback_anchor_count": structural_fallback_count,
            "route_disabled_anchor_count": (
                360 if native_structural_disabled else 0
            ),
            "predecessor_projection": {
                "active": native_structural_disabled,
                "profile": "accepted-schema9-m1-m2",
                "rendered_anchor_count": (
                    validated_native_structural_predecessor_rendered_anchors
                    if native_structural_disabled
                    else 0
                ),
                "empty_anchor_count": (
                    validated_native_structural_predecessor_empty_anchors
                    if native_structural_disabled
                    else 0
                ),
                "triangle_count": (
                    NATIVE_STRUCTURAL_SCHEMA9_DISABLED_TRIANGLE_COUNT
                    if native_structural_disabled
                    else 0
                ),
                "selected_resource_count": (
                    NATIVE_STRUCTURAL_SCHEMA9_DISABLED_RESOURCE_COUNT
                    if native_structural_disabled
                    else 0
                ),
                "resources": (
                    list(NATIVE_STRUCTURAL_SCHEMA9_DISABLED_RESOURCES)
                    if native_structural_disabled
                    else []
                ),
            },
            "physical_stock_projection": {
                "active": stock_baseline,
                "rendered_anchor_count": 0,
                "empty_anchor_count": 360 if stock_baseline else 0,
                "triangle_count": 0,
                "selected_resource_count": 0,
                "resources": [],
            },
            "route_selected_resource_count": len(structural_resources),
            "route_selected_resources": structural_resources,
            "part_identity_count": 29,
            "spin_part_identity_count": 9,
            "orientation_state_count": 336,
            "representative_installed_face_anchor_count": 174,
            "plane_mask_count_per_type": 16,
            "p2p_frequency_values": [0, 0x1234, 0xFFFF],
            "facade_mask_count": 64,
            "full_pack_glass_override": glassential_override_evidence,
            "endpoint_identity_count": 30,
            "dense_capable_part_ids": ["ae2:cable_anchor"],
            "fallback_policy": "whole-cable-bus-original-resource",
            "invariant_signature": (
                "material-position-winding-uv-normal-rgb-ao-excluding-blocklight-sunlight"
            ),
            "world_light_policy": NATIVE_STRUCTURAL_WORLD_LIGHT_POLICY,
            "light_validation": NATIVE_STRUCTURAL_LIGHT_VALIDATION,
            "forced_fullbright_resources": sorted(
                NATIVE_STRUCTURAL_FORCED_FULLBRIGHT_RESOURCES
            ),
            "contract_validated": True,
        }

        if (
            len(native_structural_legacy_upgrade_records)
            != NATIVE_STRUCTURAL_LEGACY_ANCHOR_COUNT
        ):
            raise EvidenceError(
                "S1 native structural legacy-upgrade evidence slice is incomplete"
            )
        legacy_upgrade_anchors = [
            anchor
            for case in cases
            for anchor in case.anchors
            if anchor.position
            in gallery.native_structural_legacy_upgrade_positions
        ]
        legacy_upgrade_route_records = [
            record
            for anchor in legacy_upgrade_anchors
            for record in native_structural_legacy_upgrade_records[anchor.position]
        ]
        legacy_upgrade_resources = sorted(
            {
                record.material_identity
                for record in legacy_upgrade_route_records
            }
        )
        legacy_upgrade_material_rows = sum(
            len(
                {
                    record.material_identity
                    for record in native_structural_legacy_upgrade_records[
                        anchor.position
                    ]
                }
            )
            for anchor in legacy_upgrade_anchors
        )
        legacy_glassential_triangles = sum(
            record.material_identity == NATIVE_STRUCTURAL_GLASSENTIAL_MATERIAL
            for record in legacy_upgrade_route_records
        )
        if stock_baseline or native_structural_disabled:
            if legacy_upgrade_route_records or legacy_upgrade_resources:
                raise EvidenceError(
                    "disabled/physical-stock legacy upgrades must remain empty"
                )
            legacy_upgrade_nonzero_custom = 0
            legacy_upgrade_custom_triangles = 0
            legacy_upgrade_material_rows = 0
            if legacy_glassential_triangles != 0:
                raise EvidenceError(
                    "disabled legacy Glassential facade emitted geometry"
                )
        else:
            legacy_upgrade_nonzero_custom = sum(
                bool(
                    native_structural_legacy_upgrade_records[anchor.position]
                )
                for anchor in legacy_upgrade_anchors
            )
            legacy_upgrade_custom_triangles = len(
                legacy_upgrade_route_records
            )
            if (
                legacy_upgrade_nonzero_custom
                != NATIVE_STRUCTURAL_LEGACY_ANCHOR_COUNT
                or legacy_upgrade_custom_triangles
                != NATIVE_STRUCTURAL_LEGACY_TRIANGLE_COUNT
                or len(legacy_upgrade_resources)
                != NATIVE_STRUCTURAL_LEGACY_IDENTITY_COUNT
                or legacy_upgrade_material_rows
                != NATIVE_STRUCTURAL_LEGACY_MATERIAL_ROW_COUNT
                or legacy_glassential_triangles != 48
            ):
                raise EvidenceError(
                    "enabled native structural legacy-upgrade closure changed"
                )
        native_structural_legacy_upgrades = {
            "mode": validation_mode,
            "coverage_id": NATIVE_STRUCTURAL_LEGACY_COVERAGE,
            "route": NATIVE_STRUCTURAL_ROUTE,
            "case_count": NATIVE_STRUCTURAL_LEGACY_CASE_COUNT,
            "anchor_count": NATIVE_STRUCTURAL_LEGACY_ANCHOR_COUNT,
            "custom_anchor_count": NATIVE_STRUCTURAL_LEGACY_ANCHOR_COUNT,
            "nonzero_custom_anchor_count": legacy_upgrade_nonzero_custom,
            "custom_triangle_count": legacy_upgrade_custom_triangles,
            "selected_resource_count": len(legacy_upgrade_resources),
            "selected_resources": legacy_upgrade_resources,
            "material_row_count": legacy_upgrade_material_rows,
            "oracle": {
                "size_bytes": NATIVE_STRUCTURAL_LEGACY_ORACLE_SIZE_BYTES,
                "sha256": NATIVE_STRUCTURAL_LEGACY_ORACLE_SHA256,
                "triangle_count": NATIVE_STRUCTURAL_LEGACY_TRIANGLE_COUNT,
                "identity_count": NATIVE_STRUCTURAL_LEGACY_IDENTITY_COUNT,
                "material_row_count": NATIVE_STRUCTURAL_LEGACY_MATERIAL_ROW_COUNT,
            },
            "route_disabled_anchor_count": (
                NATIVE_STRUCTURAL_LEGACY_ANCHOR_COUNT
                if native_structural_disabled
                else 0
            ),
            "predecessor_projection": {
                "active": native_structural_disabled,
                "profile": "accepted-schema9-m1-m2",
                "fallback_anchor_count": (
                    NATIVE_STRUCTURAL_LEGACY_ANCHOR_COUNT
                    if native_structural_disabled
                    else 0
                ),
                "rendered_anchor_count": 0,
                "empty_anchor_count": (
                    NATIVE_STRUCTURAL_LEGACY_ANCHOR_COUNT
                    if native_structural_disabled
                    else 0
                ),
                "triangle_count": 0,
                "selected_resource_count": 0,
                "resources": [],
            },
            "physical_stock_projection": {
                "active": stock_baseline,
                "rendered_anchor_count": 0,
                "empty_anchor_count": (
                    NATIVE_STRUCTURAL_LEGACY_ANCHOR_COUNT
                    if stock_baseline
                    else 0
                ),
                "triangle_count": 0,
                "selected_resource_count": 0,
                "resources": [],
            },
            "full_pack_glass_override": {
                "material": NATIVE_STRUCTURAL_GLASSENTIAL_MATERIAL,
                "anchor_count": (
                    1 if legacy_glassential_triangles else 0
                ),
                "triangle_count": legacy_glassential_triangles,
                "separate_from_appended_s1_closure": True,
                "validated": True,
            },
            "contract_validated": True,
        }

    appmek_review: dict[str, Any] | None = None
    if gallery.schema_version == 12:
        appmek_rows = []
        mode_nonempty = 0
        for anchor in gallery.appmek_anchors:
            projection = _appmek_mode_projection(
                anchor,
                stock_baseline=stock_baseline,
                native_structural_disabled=native_structural_disabled,
                appmek_drive_disabled=appmek_drive_disabled,
            )
            effective_path = (
                anchor.expected_path if projection is None else projection.expected_path
            )
            effective_review = (
                "nonempty" if projection is None else projection.review_projection
            )
            mode_nonempty += effective_review == "nonempty"
            appmek_rows.append(
                {
                    "case_id": anchor.case_id,
                    "position": dict(zip(("x", "y", "z"), anchor.position)),
                    "route": anchor.route,
                    "block_id": anchor.block_id,
                    "effective_path": effective_path,
                    "review_projection": effective_review,
                    "runtime_oracle_status": "pending",
                }
            )
        expected_mode_counts = {
            "enabled": (7, 0),
            "stock-baseline": (1, 6),
            "native-structural-disabled": (5, 2),
            "appmek-drive-disabled": (3, 4),
        }.get(validation_mode, (7, 0))
        if (mode_nonempty, 7 - mode_nonempty) != expected_mode_counts:
            raise EvidenceError("schema-12 AppMek projection aggregate changed")
        appmek_review = {
            "mode": validation_mode,
            "route": APPMEK_DRIVE_ROUTE,
            "anchor_count": 7,
            "route_affected_anchor_count": 4,
            "parent_renderer_control_anchor_count": 3,
            "preoracle_excluded_from_prbm_selection": True,
            "synthetic_geometry_forbidden": True,
            "mode_projection": {
                "nonempty_anchor_count": mode_nonempty,
                "empty_anchor_count": 7 - mode_nonempty,
            },
            "declared_native_drive_disabled_projection": {
                "nonempty_anchor_count": sum(
                    anchor.native_drive_disabled_projection.review_projection
                    == "nonempty"
                    for anchor in gallery.appmek_anchors
                ),
                "empty_anchor_count": sum(
                    anchor.native_drive_disabled_projection.review_projection == "empty"
                    for anchor in gallery.appmek_anchors
                ),
            },
            "anchors": appmek_rows,
            "contract_validated": True,
        }

    report = {
        "schema_version": gallery.schema_version,
        "mode": validation_mode,
        "format_contract": {
            "bluemap": "5.22",
            "prbm_version": PRBM_VERSION,
            "prbm_dialect": "non-indexed-little-endian-seven-attribute",
            "compression": "single-gzip-member-with-crc-and-isize",
            "float_canonicalization": "finite-binary-hex-v1",
            "triangle_canonicalization": "winding-preserving-smallest-cyclic-rotation-v1",
            "spatial_selection": "inward-biased-geometric-centroid-v2-dense-cap-safe",
            "semantic_signature_schema": (
                "resolved-resource-path-v3-m2-layout"
                if gallery.signature_schema_version == 3
                else "resolved-resource-path-v4-m3a-drive-layout"
                if gallery.signature_schema_version == 4
                else "resolved-resource-path-v5-m3b-extended-drive-layout"
                if gallery.signature_schema_version == 5
                else "resolved-resource-path-v6-m3c-connected-glass-layout"
                if gallery.signature_schema_version == 6
                else "resolved-resource-path-v7-m3d-formed-crafting-layout"
                if gallery.signature_schema_version == 7
                else "resolved-resource-path-v8-m3e-quantum-bridge-layout"
                if gallery.signature_schema_version == 8
                else "resolved-resource-path-v9-m3f-static-structural-layout"
                if gallery.signature_schema_version == 9
                else "resolved-resource-path-v10-s1-native-structural-layout"
                if gallery.signature_schema_version == 10
                else "resolved-resource-path-v11-m45-cumulative-review-layout"
                if gallery.signature_schema_version == 11
                else "resolved-resource-path-v12-appmek-preoracle-layout"
            ),
            "shape_quantum": canonical_float(SHAPE_QUANTUM),
            "ownership_epsilon": canonical_float(OWNERSHIP_EPSILON),
            "renderer_identity_retained": False,
        },
        "inputs": {
            "settings": {"sha256": settings_digest},
            "textures": textures_evidence,
            "cases": cases_evidence,
        },
        "tiles": tile_results,
        "cases": case_results,
        "dense_fixture": dense_result,
        "summary": summary,
        "limitations": [
            "PRBM and textures.json retain no block ID, mod ID, block-entity data, or renderer provenance.",
            "Per-anchor ownership is a geometric inference from triangle winding and position.",
            "Material ordinals resolve only to the texture-gallery resourcePath present in the matching textures file.",
            "Semantic signatures use resolved resource paths and do not depend on texture-gallery ordinals; input evidence still records the matching textures payload.",
            "The material-independent shape signature covers relative positions only, allowing color variants with identical topology to share shape goldens.",
            "The semantic geometry signature covers positions, UVs, and resolved material identity; the attribute signature additionally covers encoded normals, color, AO, and light.",
            "M2 terminal validation infers installed face and spin from each colored layer's boundary plane and UV-up vector; facade validation checks the declared thin stone ring face and central opening.",
        ],
    }
    if gallery.schema_version >= 4:
        report["limitations"].append(
            "M3a Drive validation inversely applies the declared facing/spin transform and checks occupied chassis slot bounds, exact model UV regions, and static black fullbright LED geometry; PRBM still does not retain the source item stack or components."
        )
    if gallery.schema_version >= 5:
        report["limitations"].append(
            "M3b Extended Drive validation treats slots 0..9 as front and 10..19 as back, applying opposite-facing/same-spin transforms per back slot; the static black fullbright LED proxy uses ae2:block/drive/drive_front because PRBM has no untextured POSITION_COLOR material identity."
        )
        report["limitations"].append(
            "The M3b component-insensitivity cross-anchor signature excludes blocklight and sunlight because BlueMap derives ordinary resource-model lighting from block and face-neighbor light context; every anchor's static LED fullbright attributes remain independently enforced."
        )
    if gallery.schema_version >= 6:
        report["limitations"].append(
            "M3c connected-glass validation enforces exact face planes, winding-derived normals, position-seeded asymmetric base UVs, frame masks, shared-face absence, white CUTOUT color and AO 255. Blocklight/sunlight must be valid and consistent across each face's base/frame triangles; vibrant blocklight must be 15, while ordinary blocklight and both variants' sunlight are world-derived. Attribute signatures retain actual light, topology and cross-variant signatures exclude it, and Java source tests remain authoritative for max(center,outward), all 64 neighbor masks and cave behavior."
        )
    if gallery.schema_version >= 7:
        report["limitations"].append(
            "M3d formed-crafting validation enforces exact visible-face topology, CubeBuilder winding and standard UVs, material, tint, AO 255, world-derived per-face light and powered fullbright overlays. PRBM retains neither formed/powered/facing/spin/paint block state nor renderer provenance; those values are manifest/runtime contracts. The fully enclosed center emits zero triangles and is therefore explicitly not distinguishable from stock fallback by PRBM alone. Crafting Monitor client job-display pixels are omitted."
        )
    if gallery.schema_version >= 8:
        report["limitations"].append(
            "M3e quantum-bridge validation covers only complete, settled, dry and unpowered three-by-three structures in the XY, XZ and YZ planes. It enforces static-off cuboids, CubeBuilder winding, bounds-mapped UVs, materials, white CUTOUT color, air-isolated AO 255 and ordinary directional world light. Transient powered/QES overlays and particles are intentionally omitted; malformed, unformed and waterlogged behavior remains synthetic/Java evidence rather than physical gallery anchors."
        )
    if gallery.schema_version >= 9:
        report["limitations"].append(
            "M3f validates static structural projections only: persisted paint splotches, closed Sky Stone chests, neutral cranks, neutral empty Inscribers and locally inferred static/offline spatial pylons. Machine contents, held items, fluids, activity, animation, global pylon validity, live power and particles are intentionally excluded."
        )
    if gallery.schema_version >= 10:
        report["limitations"].append(
            "S1 validates static off/inactive/unlocked cable-bus structure only. Exact per-anchor invariant signatures cover geometry, winding, UV, resolved material, normals, color and AO while excluding only environment-derived blocklight and sunlight; full observed attribute signatures still retain both light channels. Every S1 triangle must have flat in-range light, the four smart-channel resources remain exact 15/15, and source/profile gates retain emission bounds. PRBM still does not retain source NBT, facade BlockState, endpoint identity or renderer provenance, so those remain independently locked manifest/runtime contracts."
        )
    if gallery.schema_version >= 11:
        report["limitations"].append(
            "M4/M5 freezes all 391 appended custom anchors to exact per-anchor triangle counts, material counts, geometry, winding, UVs, encoded normals, color and AO while excluding only environment-derived blocklight and sunlight; full observed attribute signatures retain both light channels and every oracle triangle must have flat in-range light. The 18 atomic fallbacks remain exact empty. Independently disabled, physical-stock, crafting-disabled and native-structural-disabled anchors use their mode-specific projections; the retained ATM 1.2.0 combined-disabled cold/warm map additionally freezes its aggregate closure, the 79 source-empty model selectors, and exact inherited ME Requester and powered-state-specific Expanded I/O Port material signatures. ME Requester physically covers only the stable IDLE-derived inactive block orientations; no fake request or crafting link is created to force transient activity. Expanded AE powered I/O Ports use exact creative-energy-cell helpers. Advanced AE quantum and ExtendedAE matrix use live-proven bounded 4x3x3 physical layouts, while their remaining role blocks stay isolated static controls; settle/verify fails closed if the game rewrites any expected state, and exhaustive topology/malformed-input behavior remains independently unit-tested."
        )
        report["limitations"].append(
            "Three byte-frozen schema-10 fallback anchors are upgraded only by the active M4/M5 route set. Their separate schema-11 oracle is byte-identical across the preserved ATM 1.2.0 enabled cold/warm captures and freezes exact triangle/material counts, geometry, winding, UVs, encoded normals, color and AO while excluding only bounded flat world light. Disabling any declared owner route, the required legacy Extended/crafting route, all M4/M5 routes, or the physical add-on restores the exact empty predecessor fallback."
        )
    if m2_regression is not None:
        report["m2_regression"] = m2_regression
    if drive_component_insensitivity is not None:
        report["drive_component_insensitivity"] = drive_component_insensitivity
    if m3a_regression is not None:
        report["m3a_regression"] = m3a_regression
    if extended_drive_component_insensitivity is not None:
        report["extended_drive_component_insensitivity"] = (
            extended_drive_component_insensitivity
        )
    if extended_drive_front_back_mirror is not None:
        report["extended_drive_front_back_mirror"] = (
            extended_drive_front_back_mirror
        )
    if m3b_regression is not None:
        report["m3b_regression"] = m3b_regression
    if connected_glass is not None:
        report["connected_glass"] = connected_glass
    if schema6_regression is not None:
        report["schema6_regression"] = schema6_regression
    if formed_crafting is not None:
        report["formed_crafting"] = formed_crafting
    if schema7_regression is not None:
        report["schema7_regression"] = schema7_regression
    if quantum_bridge is not None:
        report["quantum_bridge"] = quantum_bridge
    if schema8_regression is not None:
        report["schema8_regression"] = schema8_regression
    if m3_completion is not None:
        report["m3_completion"] = m3_completion
    if native_structural is not None:
        report["native_structural"] = native_structural
    if native_structural_legacy_upgrades is not None:
        report["native_structural_legacy_upgrades"] = (
            native_structural_legacy_upgrades
        )
    if m45_legacy_upgrades is not None:
        report["m45_legacy_upgrades"] = m45_legacy_upgrades
    if m45_review is not None:
        report["m45_review"] = m45_review
    if appmek_review is not None:
        report["appmek_review"] = appmek_review
    return report


def parse_args(arguments: Sequence[str]) -> argparse.Namespace:
    project_root = Path(__file__).resolve().parents[1]
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument(
        "--map-root",
        type=Path,
        required=True,
        help="BlueMap map data root containing settings.json, textures.json.gz and tiles/0",
    )
    parser.add_argument(
        "--cases",
        type=Path,
        default=project_root / "gallery" / "cases.json",
        help="gallery cases manifest (default: this repository's gallery/cases.json)",
    )
    parser.add_argument(
        "--output",
        type=Path,
        help="write the report here instead of stdout",
    )
    parser.add_argument(
        "--include-dense",
        action="store_true",
        help=(
            "also require and analyze the opt-in 1,024-cell dense fixture "
            "(normal visual analysis does not require it)"
        ),
    )
    parser.add_argument(
        "--stock-baseline",
        action="store_true",
        help=(
            "validate the add-on-absent schema-10 baseline: the stone control "
            "plus 38 stock-rendered M3f machines own 1,882 triangles across "
            "five resources, while the other 918 anchors are empty, including "
            "all 360 appended S1 anchors and all 10 legacy upgrades"
        ),
    )
    parser.add_argument(
        "--extension-disabled",
        action="store_true",
        help=(
            "validate the independent ExtendedAE-route failure mode: all 36 "
            "M3b anchors must be empty while later M3c--S1 routes remain enabled"
        ),
    )
    parser.add_argument(
        "--glass-disabled",
        action="store_true",
        help=(
            "validate the independent connected-glass-route failure mode: all "
            "47 M3c anchors must be empty while M3b and later M3d--S1 remain enabled"
        ),
    )
    parser.add_argument(
        "--crafting-disabled",
        action="store_true",
        help=(
            "validate the independent formed-crafting route failure mode: all "
            "86 M3d anchors must be empty while schema 6 and M3e--S1 remain enabled"
        ),
    )
    parser.add_argument(
        "--quantum-disabled",
        action="store_true",
        help=(
            "validate the independent quantum-bridge route failure mode: all "
            "27 M3e anchors must be empty while the exact schema-7 routes and "
            "M3f--S1 remain enabled"
        ),
    )
    parser.add_argument(
        "--m3-completion-disabled",
        action="store_true",
        help=(
            "validate the independent M3f completion-route failure mode: all "
            "78 M3f anchors use their exact original-resource models while the "
            "byte-frozen schema-8 routes and S1 remain enabled"
        ),
    )
    parser.add_argument(
        "--native-structural-disabled",
        action="store_true",
        help=(
            "validate the independent S1 native-structural route failure mode: "
            "10 appended S1 anchors use the 608-triangle/14-resource predecessor "
            "projection, 350 appended anchors and all 10 legacy upgrades are "
            "empty, and the byte-frozen schema-9 routes remain enabled"
        ),
    )
    parser.add_argument(
        "--m45-route-disabled",
        choices=M45_ROUTES,
        help=(
            "validate one independently disabled ATM 1.2.0 M4/M5 route: every "
            "affected anchor uses its declared stock, native-center, or empty "
            "projection while the exact accepted S1 projection and the other "
            "seven M4/M5 routes remain enabled"
        ),
    )
    parser.add_argument(
        "--m45-disabled",
        action="store_true",
        help=(
            "validate all eight ATM 1.2.0 M4/M5 routes disabled together: "
            "each M4/M5 anchor uses its declared owner-route projection while "
            "the exact accepted S1 projection remains enabled"
        ),
    )
    parser.add_argument(
        "--appmek-drive-disabled",
        action="store_true",
        help=(
            "validate the declared schema-12 AppMek Drive route-disabled "
            "projection while the byte-frozen schema-11 map remains enabled; "
            "the seven new selectors remain excluded until the live oracle freezes"
        ),
    )
    return parser.parse_args(arguments)


def main(arguments: Sequence[str] | None = None) -> int:
    options = parse_args(sys.argv[1:] if arguments is None else arguments)
    try:
        report = analyze(
            options.map_root,
            options.cases,
            include_dense=options.include_dense,
            stock_baseline=options.stock_baseline,
            extension_disabled=options.extension_disabled,
            glass_disabled=options.glass_disabled,
            crafting_disabled=options.crafting_disabled,
            quantum_disabled=options.quantum_disabled,
            m3_completion_disabled=options.m3_completion_disabled,
            native_structural_disabled=options.native_structural_disabled,
            m45_route_disabled=options.m45_route_disabled,
            m45_disabled=options.m45_disabled,
            appmek_drive_disabled=options.appmek_drive_disabled,
        )
        encoded = canonical_json(report, pretty=True)
        if options.output is None:
            sys.stdout.write(encoded)
        else:
            options.output.parent.mkdir(parents=True, exist_ok=True)
            options.output.write_text(encoded, encoding="utf-8", newline="\n")
    except (EvidenceError, OSError) as exception:
        print(f"error: {exception}", file=sys.stderr)
        return 2
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
