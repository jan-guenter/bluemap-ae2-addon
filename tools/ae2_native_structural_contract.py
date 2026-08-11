#!/usr/bin/env python3
# SPDX-License-Identifier: LGPL-3.0-only
"""Exact AE2 19.2.17 post-M3 native cable-bus structural contract."""

from __future__ import annotations

import hashlib
import json
import math
import zipfile


SCHEMA_VERSION = 10
PROFILE_ID = "ae2-cable-bus-structural"
PACK_NAME = "All the Mons"
PACK_VERSION = "1.2.0"
PACK_COMMIT = "c7bb230f21d14d26859d0b92548f089b3a493ad9"
MINECRAFT_VERSION = "1.21.1"
NEOFORGE_VERSION = "21.1.248"
FULL_PACK_OVERRIDE_EVIDENCE = {
    "scope": "current-s1-full-pack-resource-override",
    "pack": {
        "name": PACK_NAME,
        "version": PACK_VERSION,
        "commit": PACK_COMMIT,
    },
    "mod": {
        "id": "glassential",
        "version": "3.4.5",
    },
    "distribution": {
        "provider": "CurseForge",
        "projectId": 945_149,
        "fileId": 8_440_850,
    },
    "artifact": {
        "fileName": "Glassential-renewed-1.21.1-3.4.5.jar",
        "sizeBytes": 702_249,
        "sha1": "3a08f59f0930c8123fa1aacdfa0ba9fbdbb6e342",
        "sha256": (
            "1f0c8f7533bf3b2002575219ba795fd32a44cc5085c2710624ebbf69e6121471"
        ),
        "sha512": (
            "62ccb9057aab96ba656ec8ce357977360c1cc7761fedd7ac995a40b1f16e389c7"
            "5d753746840b11d30077b6b896938246fb281ec481e560a05084e22098c31d8"
        ),
        "bundled": False,
    },
    "resources": [
        {
            "role": "minecraft-glass-model-override",
            "path": "assets/minecraft/models/block/glass.json",
            "sizeBytes": 192,
            "sha256": (
                "dc3cf6fdf740fceb4d2224dcb4132ab103617d0b904fcbbf6b48dbee0ecc9e4e"
            ),
        },
        {
            "role": "glassential-glass-texture",
            "path": "assets/glassential/textures/block/glass.png",
            "sizeBytes": 1_041,
            "sha256": (
                "0a5534e6eb350dbce3670d9a4bc98f98ef20fb0747068d374f3529842b902370"
            ),
        },
        {
            "role": "fusion-non-animation-metadata",
            "path": "assets/glassential/textures/block/glass.png.mcmeta",
            "sizeBytes": 97,
            "sha256": (
                "23117542de8eb132a734e588a7cac393e7d8375632e4df56cf31010a8360c719"
            ),
        },
    ],
    "semanticMetadata": {
        "fusion": {
            "type": "connecting",
            "layout": "full",
            "render_type": "cutout",
        },
        "renderType": "cutout",
        "blueMap522NonAnimationMcmetaPolicy": (
            "ignored-by-resource-loader;not-an-animation-contract"
        ),
        "operatorInstalledResources": True,
    },
    "acceptance": {
        "exactArtifactAndResourceVerification": "passed",
        "freshUnmodifiedClientVisual": "pending",
    },
}
CURRENT_HOST_EVIDENCE = {
    "scope": "current-s1-canonical-bluemap-host",
    "release": {
        "provider": "GitHub",
        "tag": "v5.22-agent.backport-5.22-mc1.21.1-2",
        "gitCommit": "9be321df995a1103808621d529eb72773e719d4d",
    },
    "artifact": {
        "fileName": (
            "bluemap-5.22-agent.backport-5.22-mc1.21.1-2-neoforge.jar"
        ),
        "sizeBytes": 6_467_235,
        "sha1": "eb1d3311da2caa5eeff03dfb3e4d1d26c73f71c7",
        "sha256": (
            "749f7647fa29764cea113114a7ab3259271bab3da22720989f2bd9fd1f3ba150"
        ),
        "sha512": (
            "8d7ae9caef2069866e8dba9c1d6d3710948f9efe60f1fd89679784349f89196d"
            "bb6c015de195dbc4208a6f822c60c06d1f051b6e30e3c14162f075c09385dc26"
        ),
        "bundled": False,
    },
    "implementation": {
        "modId": "bluemap",
        "version": "5.22-agent.backport-5.22-mc1.21.1-2",
        "displayTest": "IGNORE_SERVER_VERSION",
        "minecraftVersionRange": "[1.21.1,1.21.2)",
        "neoForgeVersionRange": "[21.1.248,21.2)",
    },
    "embeddedEvidence": [
        {
            "role": "neoforge-mod-metadata",
            "path": "META-INF/neoforge.mods.toml",
            "sizeBytes": 560,
            "sha256": (
                "5e83eef29bbad4ec1f65f39f5b1c40f3012101e0afef64785424254213876d31"
            ),
        },
        {
            "role": "bluemap-implementation-version",
            "path": "de/bluecolored/bluemap/version.json",
            "sizeBytes": 110,
            "sha256": (
                "a047fa2144d7f08ee50c30600a13f80041fe9b63042a2519b358e9f3ccb68c29"
            ),
        },
    ],
    "acceptance": {
        "exactArtifactAndEmbeddedMetadataVerification": "passed",
        "deployment": "operator-installed-not-bundled",
    },
}
ROUTE_RESOURCE_ROOT = (
    "bluemap-ae2/profiles/ae2/19.2.17/routes/cable-bus-structural"
)
VERSION = "19.2.17"
EXPECTED_SIZE = 8_230_896
EXPECTED_SHA1 = "49c18d6a4af487957d7e5a6ad5dcbf71090b8e14"
EXPECTED_SHA256 = (
    "460d779a0609b81409907d9956de8f6f70a1b0912257e3e5c3c7e75ac9630e95"
)
EXPECTED_SHA512 = (
    "55edfd948366aff620881e0625e48c333a2cb847e73249bc0b588efbc4b867099"
    "92a8ffbca97ea387e270df4186fe7f74ee2f27b739f1c952e932becfb9dea33"
)

SOURCE_COMMIT = "79ee2c704ad62941a426c26b1cb1f76ef5b2ee5a"
EXPECTED_SOURCES_SIZE = 3_814_167
EXPECTED_SOURCES_SHA1 = "260a230e9fa1b4885489f7f58b30c19226093a52"
EXPECTED_SOURCES_SHA256 = (
    "d2f451203cb61c2d21fae52c683083d2f72441ca7d26725f4df5934290492e6a"
)
EXPECTED_SOURCES_SHA512 = (
    "c7feaf57a7f56b76dc019519b84cf4e0718cc334827c0497f17dd65921e723a2b"
    "98f9a072807a46f2c7e3431be71eeec3bdea9a990b661076af849e3d7ef2c34"
)

FACE_COUNT = 6
SPIN_COUNT = 4
FACE_PART_COUNT = 29
SPIN_CAPABLE_PART_COUNT = 9
SMART_CORE_PART_COUNT = 2
NATIVE_ENDPOINT_COUNT = 30
ENDPOINT_STATE_SCHEMA_COUNT = 30
ENDPOINT_STATE_COMBINATION_COUNT = 534
ENDPOINT_STATE_SIDE_COMBINATION_COUNT = 3_204
UNSUPPORTED_COMPATIBLE_ENDPOINT_COUNT = 67
ENDPOINT_SIDE_RULE_KIND_COUNT = 8
ORIENTATION_STATE_COUNT = 336
PLANE_CONNECTION_MASK_COUNT = 16
DIRECT_NEUTRAL_RESOURCE_COUNT = 41
TRANSITIVE_JSON_RESOURCE_COUNT = 43
PNG_RESOURCE_COUNT = 56
REQUIRED_RESOURCE_COUNT = 99
REQUIRED_RESOURCE_BYTES = 51_306
FACADE_MASK_COUNT = 64
FACADE_WHITELIST_BLOCK_COUNT = 24
FACADE_WHITELIST_OPTIONAL_TAG_COUNT = 1
FACADE_WHITELIST_NEUTRAL_STATE_COUNT = 24
FACADE_WHITELIST_STATE_SCHEMA_COUNT = 24
FACADE_WHITELIST_STATE_COMBINATION_COUNT = 554
FACADE_WHITELIST_SOLID_RENDER_TRUE_STATE_COUNT = 551
FACADE_WHITELIST_SAME_STATE_SKIP_RENDERING_TRUE_STATE_COUNT = 3
FACADE_WHITELIST_SAME_STATE_SKIP_RENDERING_COUNT = 24
FACADE_ORDINARY_SKIP_RENDERING_CONTROL_COUNT = 3
NATIVE_FACADE_NEUTRAL_MATERIAL_COUNT = 11
MAX_FACADES = 6
MAX_PART_SLOTS = 6
MAX_FACADE_SCAN = 6

TRANSIENT_POLICY = "static-off-inactive-unlocked"
FACADE_POLICY = "all-six-face-masks-per-instance-valid-static-block-state-material"
FALLBACK_POLICY = "missing-malformed-or-capped-atomic-original-resource-fallback"
FACADE_DIRECTIONAL_SHADE_POLICY = (
    "source-shade-bit-semantic-locked-host-prbm-has-no-per-quad-shade-channel"
)
FACADE_AMBIENT_OCCLUSION_DIRECTION_POLICY = (
    "BlueMap-ResourceModelRenderer-source-faceDir-rotated-by-blockstate-variant-"
    "only;element-rotation-affects-vertices-not-AO-direction;runtime-uses-layer-"
    "lightFace-not-quad-nominal-face"
)
STRUCTURAL_MAP_COLOR_ILLUMINATION_POLICY = (
    "BlueMap-map-color-illumination-uses-original-center-and-outward-world-light-"
    "only;element-lightEmission-affects-triangle-blocklight-not-map-color-"
    "brightness"
)
FACADE_SUPPORT_SET_POLICY = (
    "all-24-explicit-whitelist-families-plus-bounded-static-full-cube-witness-"
    "material-lane"
)
FACADE_ORDINARY_MATERIAL_POLICY = (
    "optional-c-glass-blocks-and-ordinary-FacadeItem-eligible-states-require-"
    "one-live-unrotated-0-to-16-six-face-full-cube-witness;bounded-additional-"
    "static-elements-and-multipart-source-quads-subject-to-uv-tint-weighted-and-"
    "semantic-resource-gates;otherwise-valid-complex-static-models-atomic-"
    "original-resource-fallback"
)
FACADE_TINT_POLICY = (
    "untinted-or-one-distinct-nonnegative-source-tint-index;"
    "untinted-layers-may-coexist;shared-tinted-layers-use-host-block-color-"
    "calculator;mixed-nonnegative-tint-indices-atomic-original-resource-fallback"
)
FACADE_UV_REINTERPOLATION_POLICY = (
    "source-QuadReInterpolator-nominal-face-2d-dx-dy-bilinear;admitted-quad-"
    "projection-requires-exact-complete-InterpHelper-grid;post-clamp-and-corner-"
    "kick-target-uses-projected-dx-dy;noncompatible-projected-quads-atomic-"
    "original-resource-fallback"
)
FACADE_CARDINAL_VARIANT_TRANSFORM_POLICY = (
    "exact-signed-permutation-quarter-turn-blockstate-variant-and-uvlock-"
    "coordinate-transforms;avoids-"
    "host-float-matrix-drift-before-source-exact-InterpHelper-grid"
)
FACADE_WEIGHTED_VARIANT_POLICY = (
    "exact-minecraft-stone-four-alternative-geometry-and-material-host-position-"
    "projection-retains-frozen-M2-non-pixel-identical-randomized-uv-boundary;"
    "all-other-weighted-sets-require-every-alternative-collapse-to-one-bounded-"
    "static-geometry-material-uv-descriptor;otherwise-atomic-original-resource-"
    "fallback"
)
FACADE_SKIP_RENDERING_POLICY = (
    "exact-24-explicit-whitelist-same-state-table;ae2-quartz-glass-cross-family-"
    "render-shape-rule;exact-gallery-controls-glass-true-oak-log-false-oak-"
    "leaves-false;other-ordinary-tag-materials-use-bounded-BlueMap-"
    "cullingIdentical-same-state-host-projection"
)
FACADE_WHITELIST_STATE_POLICY = (
    "all-24-explicit-whitelist-families-require-exact-complete-persisted-"
    "property-key-set-and-value-domains;13-vanilla-families-preserve-valid-"
    "state;11-ae2-native-families-apply-declared-static-normalization;extra-"
    "missing-or-invalid-properties-atomic-original-resource-fallback"
)
FACADE_WHITELIST_STATE_CLASSIFICATION_POLICY = (
    "solidRender-and-same-state-skipRendering-family-invariant-across-all-554-"
    "valid-explicit-whitelist-states;neutral-default-row-booleans-apply-to-whole-"
    "family;classification-drift-atomic-original-resource-fallback"
)
FACADE_QUARTZ_SKIP_RENDERING_POLICY = (
    "true-for-any-two-ae2-QuartzGlassBlock-families-with-equal-render-shape"
)
FACADE_CUTOUT_STRIP_AABB_POLICY = (
    "minecraft-AABB-normalizes-each-generated-strip-endpoint-pair-with-min-max;"
    "transparent-inset-plus-boundary-reaching-cutout-may-reverse-endpoints-and-"
    "must-produce-the-normalized-strip-not-a-degenerate-strip"
)
FACADE_CORNER_KICK_SOURCE_EPSILON_BLOCKS = 0.00001
FACADE_CORNER_KICK_RUNTIME_EPSILON_SIXTEENTHS = 0.00016
FACADE_CORNER_KICK_ANALYZER_EPSILON_BLOCKS = 0.00001
NATIVE_FACADE_NEUTRAL_SCOPE = (
    "11-ae2-native-neutral-resource-pins-not-the-complete-facade-support-set"
)
KNOWN_UNSUPPORTED_COMPATIBLE_ENDPOINT_POLICY = (
    "unknown-atomic-original-resource-fallback"
)
MALFORMED_NATIVE_ENDPOINT_POLICY = (
    "malformed-native-endpoint-atomic-original-resource-fallback"
)
MALFORMED_KNOWN_EXTENSION_ENDPOINT_POLICY = (
    "malformed-known-extension-observation-atomic-original-resource-fallback"
)

SMART_CORE_PART_IDS = (
    "ae2:level_emitter",
    "ae2:energy_level_emitter",
)
PLANE_CONNECTION_MASK_BITS = {
    "left": 1,
    "down": 2,
    "right": 4,
    "up": 8,
}
PLANE_COLLISION_BOUND_BITS_BY_FACE = {
    "down": {
        "minX": "right",
        "maxX": "left",
        "minY": "down",
        "maxY": "up",
    },
    "up": {
        "minX": "left",
        "maxX": "right",
        "minY": "up",
        "maxY": "down",
    },
    "north": {
        "minX": "left",
        "maxX": "right",
        "minY": "down",
        "maxY": "up",
    },
    "south": {
        "minX": "left",
        "maxX": "right",
        "minY": "down",
        "maxY": "up",
    },
    "west": {
        "minX": "right",
        "maxX": "left",
        "minY": "down",
        "maxY": "up",
    },
    "east": {
        "minX": "right",
        "maxX": "left",
        "minY": "down",
        "maxY": "up",
    },
}
PLANE_RENDER_BOUND_BITS = {
    "minX": "right",
    "maxX": "left",
    "minY": "down",
    "maxY": "up",
}
PLANE_COORDINATE_SPACE_POLICY = (
    "mask-bits-are-PlaneConnections-front-view-logical;renderedGeometryBoundBits-"
    "are-PlaneBakedModel-visual-local-before-QuadRotator-installed-world-transform;"
    "collisionBoundBitsByInstalledFace-are-BusCollisionHelper-installed-face-local;"
    "never-reuse-bounds-across-coordinate-spaces"
)

EXPECTED_RESOURCE_MANIFEST_SHA256 = (
    "ae89e4fc3356503cc76ea92ac9cb11ade296551c9cca85cd583ffddbbe35bd76"
)
EXPECTED_RESOURCE_SIZES_MANIFEST_SHA256 = (
    "a79e93baef3f5d923730686fcc4de05ec30c8b7765aef8b32aaf871f9c4f3869"
)
LIVE_MODEL_SEMANTIC_SIGNATURE_SHA256 = (
    "aefa42ad8427e8f2ac5b9f1c88807c978617d6ff70768a32223616b970b54251"
)
LIVE_TEXTURE_SEMANTIC_SIGNATURE_SHA256 = (
    "1bee2b2917edf3d1eb9ee24505f47a7377665da753f107ec1af9170d783bc833"
)
QUARTZ_FACADE_DEPENDENCY_PROFILE_ID = "ae2-quartz-glass"
QUARTZ_FACADE_DEPENDENCY_PROFILE_SHA256 = (
    "548e5bc00ef07c6d6b93b346422b596882ec11ca03de006065fa45fecb991200"
)
QUARTZ_FACADE_DEPENDENCY_RESOURCE_MANIFEST_SHA256 = (
    "b51c708e7c4d26093c1b6f85b88d0be50572d3cfa76dbf802720f6ad79c7a7fa"
)
QUARTZ_FACADE_DEPENDENCY_TEXTURE_MANIFEST_SHA256 = (
    "65005c9b76800cdeba5c4598472a44dea131c9974672f89bf421452755fefb6a"
)
QUARTZ_FACADE_DEPENDENCY_TEXTURE_COUNT = 19
QUARTZ_FACADE_DEPENDENCY_TEXTURE_SEMANTIC_SIGNATURE_SHA256 = (
    "c51ced2667879b8b298400c81805cf7d4459b5ac88c36350bca7bb6ca2bfef50"
)
EXPECTED_PROFILE_SHA256 = (
    "f6fa515b4e17205a019d57f253d5e71017ea20e75b8f0c333aa587afd0d0f353"
)


def _part(
    part_id: str,
    group: str,
    source_class: str,
    models: tuple[str, ...],
    *,
    spin: bool = False,
    dense: bool = False,
) -> dict[str, object]:
    return {
        "id": part_id,
        "group": group,
        "sourceClass": source_class,
        "neutralModels": models,
        "spin": "persisted-byte-0-through-3" if spin else "ignored",
        "denseCenter": dense,
    }


# AEParts declaration order. Model layers are the exact static off/inactive/unlocked
# selection; cable_anchor_short is the facade-conditioned anchor layer.
FACE_PARTS = (
    _part("ae2:quartz_fiber", "network", "appeng.parts.networking.QuartzFiberPart",
          ("ae2:part/quartz_fiber",)),
    _part("ae2:toggle_bus", "redstone", "appeng.parts.misc.ToggleBusPart",
          ("ae2:part/toggle_bus_base", "ae2:part/toggle_bus_status_off")),
    _part("ae2:inverted_toggle_bus", "redstone",
          "appeng.parts.misc.InvertedToggleBusPart",
          ("ae2:part/inverted_toggle_bus_base", "ae2:part/toggle_bus_status_off")),
    _part("ae2:cable_anchor", "structural", "appeng.parts.misc.CableAnchorPart",
          ("ae2:part/cable_anchor", "ae2:part/cable_anchor_short"), dense=True),
    _part("ae2:monitor", "panel", "appeng.parts.reporting.PanelPart",
          ("ae2:part/monitor_base", "ae2:part/monitor_bright_off"), spin=True),
    _part("ae2:semi_dark_monitor", "panel", "appeng.parts.reporting.SemiDarkPanelPart",
          ("ae2:part/monitor_base", "ae2:part/monitor_medium_off"), spin=True),
    _part("ae2:dark_monitor", "panel", "appeng.parts.reporting.DarkPanelPart",
          ("ae2:part/monitor_base", "ae2:part/monitor_dark_off"), spin=True),
    _part("ae2:storage_bus", "bus", "appeng.parts.storagebus.StorageBusPart",
          ("ae2:part/storage_bus_base", "ae2:part/storage_bus_off")),
    _part("ae2:import_bus", "bus", "appeng.parts.automation.ImportBusPart",
          ("ae2:part/import_bus_base", "ae2:part/import_bus_off")),
    _part("ae2:export_bus", "bus", "appeng.parts.automation.ExportBusPart",
          ("ae2:part/export_bus_base", "ae2:part/export_bus_off")),
    _part("ae2:level_emitter", "emitter",
          "appeng.parts.automation.StorageLevelEmitterPart",
          ("ae2:part/level_emitter_base_off", "ae2:part/level_emitter_status_off")),
    _part("ae2:energy_level_emitter", "emitter",
          "appeng.parts.automation.EnergyLevelEmitterPart",
          ("ae2:part/level_emitter_base_off", "ae2:part/level_emitter_status_off")),
    _part("ae2:annihilation_plane", "plane",
          "appeng.parts.automation.AnnihilationPlanePart",
          ("ae2:part/transition_plane_off", "ae2:part/annihilation_plane")),
    _part("ae2:formation_plane", "plane", "appeng.parts.automation.FormationPlanePart",
          ("ae2:part/transition_plane_off", "ae2:part/formation_plane")),
    _part("ae2:pattern_encoding_terminal", "terminal",
          "appeng.parts.encoding.PatternEncodingTerminalPart",
          ("ae2:part/display_base", "ae2:part/display_status_off",
           "ae2:part/pattern_encoding_terminal_off"), spin=True),
    _part("ae2:crafting_terminal", "terminal",
          "appeng.parts.reporting.CraftingTerminalPart",
          ("ae2:part/display_base", "ae2:part/display_status_off",
           "ae2:part/crafting_terminal_off"), spin=True),
    _part("ae2:terminal", "terminal", "appeng.parts.reporting.ItemTerminalPart",
          ("ae2:part/display_base", "ae2:part/display_status_off",
           "ae2:part/terminal_off"), spin=True),
    _part("ae2:storage_monitor", "monitor",
          "appeng.parts.reporting.StorageMonitorPart",
          ("ae2:part/display_base", "ae2:part/display_status_off",
           "ae2:part/storage_monitor_off"), spin=True),
    _part("ae2:conversion_monitor", "monitor",
          "appeng.parts.reporting.ConversionMonitorPart",
          ("ae2:part/display_base", "ae2:part/display_status_off",
           "ae2:part/conversion_monitor_off"), spin=True),
    _part("ae2:cable_pattern_provider", "service",
          "appeng.parts.crafting.PatternProviderPart",
          ("ae2:part/pattern_provider_base", "ae2:part/interface_off")),
    _part("ae2:cable_interface", "service", "appeng.parts.misc.InterfacePart",
          ("ae2:part/interface_base", "ae2:part/interface_off")),
    _part("ae2:pattern_access_terminal", "terminal",
          "appeng.parts.reporting.PatternAccessTerminalPart",
          ("ae2:part/display_base", "ae2:part/display_status_off",
           "ae2:part/pattern_access_terminal_off"), spin=True),
    _part("ae2:cable_energy_acceptor", "network",
          "appeng.parts.networking.EnergyAcceptorPart",
          ("ae2:part/energy_acceptor",)),
    _part("ae2:me_p2p_tunnel", "p2p", "appeng.parts.p2p.MEP2PTunnelPart",
          ("ae2:part/p2p/p2p_tunnel_status_off",
           "ae2:part/p2p/p2p_tunnel_frequency", "ae2:part/p2p/p2p_tunnel_me")),
    _part("ae2:redstone_p2p_tunnel", "p2p",
          "appeng.parts.p2p.RedstoneP2PTunnelPart",
          ("ae2:part/p2p/p2p_tunnel_status_off",
           "ae2:part/p2p/p2p_tunnel_frequency", "ae2:part/p2p/p2p_tunnel_redstone")),
    _part("ae2:item_p2p_tunnel", "p2p", "appeng.parts.p2p.ItemP2PTunnelPart",
          ("ae2:part/p2p/p2p_tunnel_status_off",
           "ae2:part/p2p/p2p_tunnel_frequency", "ae2:part/p2p/p2p_tunnel_items")),
    _part("ae2:fluid_p2p_tunnel", "p2p", "appeng.parts.p2p.FluidP2PTunnelPart",
          ("ae2:part/p2p/p2p_tunnel_status_off",
           "ae2:part/p2p/p2p_tunnel_frequency", "ae2:part/p2p/p2p_tunnel_fluids")),
    _part("ae2:fe_p2p_tunnel", "p2p", "appeng.parts.p2p.FEP2PTunnelPart",
          ("ae2:part/p2p/p2p_tunnel_status_off",
           "ae2:part/p2p/p2p_tunnel_frequency", "ae2:part/p2p/p2p_tunnel_fe")),
    _part("ae2:light_p2p_tunnel", "p2p", "appeng.parts.p2p.LightP2PTunnelPart",
          ("ae2:part/p2p/p2p_tunnel_status_off",
           "ae2:part/p2p/p2p_tunnel_frequency", "ae2:part/p2p/p2p_tunnel_light")),
)


def _static_collision(*boxes: tuple[int, int, int, int, int, int]) -> dict[str, object]:
    return {"mode": "static", "boxes": [list(box) for box in boxes]}


_REPORTING_COLLISION = _static_collision(
    (2, 2, 14, 14, 14, 16),
    (4, 4, 13, 12, 12, 14),
)
_P2P_COLLISION = _static_collision(
    (5, 5, 12, 11, 11, 13),
    (3, 3, 13, 13, 13, 14),
    (2, 2, 14, 14, 14, 16),
)
_PLANE_COLLISION = {
    "mode": "plane-connection-mask",
    "fixedBoxes": [[5, 5, 14, 11, 11, 15]],
    "dynamicSheet": {
        "base": [1, 1, 15, 15, 15, 16],
        "coordinateSpace": "BusCollisionHelper-installed-face-local",
        "boundBitsByInstalledFace": PLANE_COLLISION_BOUND_BITS_BY_FACE,
        "minXExtendsTo": 0,
        "minYExtendsTo": 0,
        "maxXExtendsTo": 16,
        "maxYExtendsTo": 16,
    },
}

_PART_COLLISION_POLICIES_BY_ID = {
    "ae2:quartz_fiber": _static_collision((6, 6, 10, 10, 10, 16)),
    "ae2:toggle_bus": _static_collision((6, 6, 11, 10, 10, 16)),
    "ae2:inverted_toggle_bus": _static_collision((6, 6, 11, 10, 10, 16)),
    "ae2:cable_anchor": {
        "mode": "same-side-facade-conditioned",
        "withoutSameSideFacade": [[7, 7, 10, 9, 9, 16]],
        "withSameSideFacade": [[7, 7, 10, 9, 9, 14]],
    },
    **{
        part_id: _REPORTING_COLLISION
        for part_id in (
            "ae2:monitor",
            "ae2:semi_dark_monitor",
            "ae2:dark_monitor",
            "ae2:pattern_encoding_terminal",
            "ae2:crafting_terminal",
            "ae2:terminal",
            "ae2:storage_monitor",
            "ae2:conversion_monitor",
            "ae2:pattern_access_terminal",
        )
    },
    "ae2:storage_bus": _static_collision(
        (3, 3, 15, 13, 13, 16),
        (2, 2, 14, 14, 14, 15),
        (5, 5, 12, 11, 11, 14),
    ),
    "ae2:import_bus": _static_collision(
        (6, 6, 11, 10, 10, 13),
        (5, 5, 13, 11, 11, 14),
        (4, 4, 14, 12, 12, 16),
    ),
    "ae2:export_bus": _static_collision(
        (4, 4, 12, 12, 12, 14),
        (5, 5, 14, 11, 11, 15),
        (6, 6, 15, 10, 10, 16),
        (6, 6, 11, 10, 10, 12),
    ),
    "ae2:level_emitter": _static_collision((7, 7, 11, 9, 9, 16)),
    "ae2:energy_level_emitter": _static_collision((7, 7, 11, 9, 9, 16)),
    "ae2:annihilation_plane": _PLANE_COLLISION,
    "ae2:formation_plane": _PLANE_COLLISION,
    "ae2:cable_pattern_provider": _static_collision(
        (2, 2, 14, 14, 14, 16),
        (5, 5, 12, 11, 11, 14),
    ),
    "ae2:cable_interface": _static_collision(
        (2, 2, 14, 14, 14, 16),
        (5, 5, 12, 11, 11, 14),
    ),
    "ae2:cable_energy_acceptor": _static_collision(
        (2, 2, 14, 14, 14, 16),
        (4, 4, 12, 12, 12, 14),
    ),
    **{
        part_id: _P2P_COLLISION
        for part_id in (
            "ae2:me_p2p_tunnel",
            "ae2:redstone_p2p_tunnel",
            "ae2:item_p2p_tunnel",
            "ae2:fluid_p2p_tunnel",
            "ae2:fe_p2p_tunnel",
            "ae2:light_p2p_tunnel",
        )
    },
}

# Keep the serialized contract in the exact AEParts declaration order even
# where several part IDs share one immutable source-derived policy object.
PART_COLLISION_POLICIES = {
    part["id"]: _PART_COLLISION_POLICIES_BY_ID[part["id"]]
    for part in FACE_PARTS
}

_DIRECTION_VALUES = ("down", "up", "north", "south", "west", "east")
_BOOLEAN_VALUES = ("false", "true")
_SPIN_VALUES = ("0", "1", "2", "3")


def _state_schema(**properties: tuple[str, ...]) -> dict[str, list[str]]:
    return {name: list(values) for name, values in properties.items()}


# Complete persisted BlockState key/domain schemas in native endpoint order.
# Empty maps are intentional: those blocks have no persisted state properties,
# and any extra property is malformed rather than ignorable.
ENDPOINT_STATE_SCHEMAS = {
    "ae2:inscriber": _state_schema(
        facing=_DIRECTION_VALUES, spin=_SPIN_VALUES, waterlogged=_BOOLEAN_VALUES
    ),
    "ae2:wireless_access_point": _state_schema(
        facing=_DIRECTION_VALUES,
        state=("off", "on", "has_channel"),
        waterlogged=_BOOLEAN_VALUES,
    ),
    "ae2:charger": _state_schema(facing=_DIRECTION_VALUES, spin=_SPIN_VALUES),
    "ae2:quantum_ring": _state_schema(
        formed=_BOOLEAN_VALUES, waterlogged=_BOOLEAN_VALUES
    ),
    "ae2:quantum_link": _state_schema(
        formed=_BOOLEAN_VALUES, waterlogged=_BOOLEAN_VALUES
    ),
    "ae2:spatial_pylon": _state_schema(powered_on=_BOOLEAN_VALUES),
    "ae2:spatial_io_port": _state_schema(
        facing=_DIRECTION_VALUES, powered=_BOOLEAN_VALUES, spin=_SPIN_VALUES
    ),
    "ae2:spatial_anchor": _state_schema(
        facing=_DIRECTION_VALUES, powered=_BOOLEAN_VALUES
    ),
    "ae2:controller": _state_schema(
        state=("offline", "online", "conflicted"),
        type=("block", "column_x", "column_y", "column_z", "inside_a", "inside_b"),
    ),
    "ae2:drive": _state_schema(facing=_DIRECTION_VALUES, spin=_SPIN_VALUES),
    "ae2:chest": _state_schema(
        facing=_DIRECTION_VALUES, lights_on=_BOOLEAN_VALUES, spin=_SPIN_VALUES
    ),
    "ae2:interface": _state_schema(),
    "ae2:io_port": _state_schema(
        facing=_DIRECTION_VALUES, powered=_BOOLEAN_VALUES, spin=_SPIN_VALUES
    ),
    "ae2:energy_acceptor": _state_schema(),
    "ae2:crystal_resonance_generator": _state_schema(
        facing=_DIRECTION_VALUES, waterlogged=_BOOLEAN_VALUES
    ),
    "ae2:vibration_chamber": _state_schema(
        active=_BOOLEAN_VALUES, facing=_DIRECTION_VALUES, spin=_SPIN_VALUES
    ),
    "ae2:growth_accelerator": _state_schema(
        facing=_DIRECTION_VALUES, powered=_BOOLEAN_VALUES
    ),
    "ae2:energy_cell": _state_schema(fullness=("0", "1", "2", "3", "4")),
    "ae2:dense_energy_cell": _state_schema(fullness=("0", "1", "2", "3", "4")),
    "ae2:creative_energy_cell": _state_schema(),
    "ae2:crafting_unit": _state_schema(
        formed=_BOOLEAN_VALUES, powered=_BOOLEAN_VALUES
    ),
    "ae2:crafting_accelerator": _state_schema(
        formed=_BOOLEAN_VALUES, powered=_BOOLEAN_VALUES
    ),
    "ae2:1k_crafting_storage": _state_schema(
        formed=_BOOLEAN_VALUES, powered=_BOOLEAN_VALUES
    ),
    "ae2:4k_crafting_storage": _state_schema(
        formed=_BOOLEAN_VALUES, powered=_BOOLEAN_VALUES
    ),
    "ae2:16k_crafting_storage": _state_schema(
        formed=_BOOLEAN_VALUES, powered=_BOOLEAN_VALUES
    ),
    "ae2:64k_crafting_storage": _state_schema(
        formed=_BOOLEAN_VALUES, powered=_BOOLEAN_VALUES
    ),
    "ae2:256k_crafting_storage": _state_schema(
        formed=_BOOLEAN_VALUES, powered=_BOOLEAN_VALUES
    ),
    "ae2:crafting_monitor": _state_schema(
        facing=_DIRECTION_VALUES,
        formed=_BOOLEAN_VALUES,
        powered=_BOOLEAN_VALUES,
        spin=_SPIN_VALUES,
    ),
    "ae2:pattern_provider": _state_schema(
        push_direction=(*_DIRECTION_VALUES, "all")
    ),
    "ae2:molecular_assembler": _state_schema(powered=_BOOLEAN_VALUES),
}

ENDPOINT_BLOCKSTATE_SHA256 = {
    "ae2:inscriber": "4ec6c21834e68f179c252bf22aeb8f8f67d57ef057eb8bde57f65f576e0885f2",
    "ae2:wireless_access_point": "05c09f9e0bcb7a09ee8f2566a2eb3f885549df2765f624c7c44fed87eee6cf6e",
    "ae2:charger": "83ebcbf59495865f7302e58292f81b83231016b1fce15515ccc10cb158f73d76",
    "ae2:quantum_ring": "3db38f2e82cd1a9e1e2e45cb078d09f0d01507750cd895615bb9a7f722f27c50",
    "ae2:quantum_link": "156f0aeafca2763f1e3fccadd342c08da7870bcb3aa8f176127a2a3502b3aa7d",
    "ae2:spatial_pylon": "a3c18208840e313823afc7198e8d74da9b1e65e78dffdc6327f53d2b70e678c9",
    "ae2:spatial_io_port": "fd2ff71aef6d77ea08dcd5aa80d7972f802a7d2ecf788cf45dbc26ade51fd542",
    "ae2:spatial_anchor": "38019a1eda66fef56bf493d818fe3452cbd8367f57fecf29acbe80f7d430837f",
    "ae2:controller": "693d04c733b47e4159052d0843256fa7520bbc1984b6d9e454bec976a73d2ca8",
    "ae2:drive": "b69d86cf730369715ad51f23793efb9b6910ec9760d4ab40029e128046d204ce",
    "ae2:chest": "c628dab804fe28fa813fef46ddcf2e4f5f13192e63cea2a7f8b8dcc3d0810ed0",
    "ae2:interface": "1bc532291c1343d076662eb69d6913953b27f91ce5d722a5b78c6095f56167ee",
    "ae2:io_port": "601dccfb290cfd7f70c2f1e0662082e4f17c10ecfec3857b55f96db13113dbcf",
    "ae2:energy_acceptor": "ee3ffe5a1fc5269a13b4474bd23ef8f98869a528bda2287ba849ca4fd4f14a7c",
    "ae2:crystal_resonance_generator": (
        "11d0a847d7abfb1db1acb8a748a3203aa7af9b76ff4c194c288ddc29d131229d"
    ),
    "ae2:vibration_chamber": "6555d07d339d0fd2af34f5b7f4fbf574313df8701544bdb2e4189a17dcc3038c",
    "ae2:growth_accelerator": "57cd5e8741a98c81b4db43bd796beaae8e9f1f227c9eeac03164b6552e8f1212",
    "ae2:energy_cell": "2e285ec4568671ea1185c70c6f38ab3a943cf24dcdd7847fe0886871409ea0fa",
    "ae2:dense_energy_cell": "357108af0d785e58fea6240d4cba13e81b686caadaab974fcf30c0ea99ca616d",
    "ae2:creative_energy_cell": "e924240fd1c63be2a7033f764213c9d0f3d8cab2269d7e73dc1f7abadb18de80",
    "ae2:crafting_unit": "b33f03d38953281265d6196e2a9f2494974275901b570f390ebf40fa3a338ece",
    "ae2:crafting_accelerator": "f2b8fd7efa88b37968f55d8169eee48d84c1c673b5b2201719037771d5e18918",
    "ae2:1k_crafting_storage": "9a1f6383cd3b54a8361cefc46740ddbee587ce79baefccb6ad6de6355833a603",
    "ae2:4k_crafting_storage": "dd4210a4c0fc5b0eb7f524571f20b7e1a92c438bc68df7324cb26c939c726abc",
    "ae2:16k_crafting_storage": "8e04febb39f74e1bb1061f9fee979be9cc4923bf14cc5a5d619cf6e681d506a4",
    "ae2:64k_crafting_storage": "d8a1b0f2f21c2f05cd959f03213d0434c6bb41e27d5591d0c3c532aea142eb7f",
    "ae2:256k_crafting_storage": "3458c6e521a76f7a0761c7efe956cc587826cfdd40d1f7c6284100990fb68905",
    "ae2:crafting_monitor": "157e2a326b835180b369874b5f6978fab7c6796293945f85a971ac3f5b1cf2b7",
    "ae2:pattern_provider": "1b8e3a67480db0dec346477a67e026798b7287db7b48b4242f58d405035b0b83",
    "ae2:molecular_assembler": "136857cc899a24bcca0b730790da3128a74c9e8196028a264e32e5e1582183a0",
}


def _endpoint(
    endpoint_id: str,
    source_class: str,
    block_entity_id: str,
    cable_type: str,
    side_rule: str,
) -> dict[str, object]:
    return {
        "id": endpoint_id,
        "blockEntityClass": source_class,
        "blockEntityId": block_entity_id,
        "cableType": cable_type,
        "sideRule": side_rule,
        "stateProperties": ENDPOINT_STATE_SCHEMAS[endpoint_id],
        "blockstateSha256": ENDPOINT_BLOCKSTATE_SHA256[endpoint_id],
        "topologyClass": "native-grid-node-host",
    }


# AEBlockEntities registration order, expanded to the concrete block IDs hosted
# by shared block-entity types. Cable types are exact getCableConnectionType
# overrides or the AENetworkedBlockEntity SMART default.
ENDPOINTS = (
    _endpoint("ae2:inscriber", "appeng.blockentity.misc.InscriberBlockEntity",
              "ae2:inscriber", "COVERED", "NO_FRONT"),
    _endpoint("ae2:wireless_access_point",
              "appeng.blockentity.networking.WirelessAccessPointBlockEntity",
              "ae2:wireless_access_point", "SMART", "BACK"),
    _endpoint("ae2:charger", "appeng.blockentity.misc.ChargerBlockEntity",
              "ae2:charger", "COVERED", "NO_FRONT"),
    _endpoint("ae2:quantum_ring", "appeng.blockentity.qnb.QuantumBridgeBlockEntity",
              "ae2:quantum_ring", "DENSE_SMART", "FORMED_QUANTUM"),
    _endpoint("ae2:quantum_link", "appeng.blockentity.qnb.QuantumBridgeBlockEntity",
              "ae2:quantum_ring", "DENSE_SMART", "FORMED_QUANTUM"),
    _endpoint("ae2:spatial_pylon", "appeng.blockentity.spatial.SpatialPylonBlockEntity",
              "ae2:spatial_pylon", "SMART", "VALID_STRAIGHT_PYLON"),
    _endpoint("ae2:spatial_io_port", "appeng.blockentity.spatial.SpatialIOPortBlockEntity",
              "ae2:spatial_io_port", "SMART", "ALL"),
    _endpoint("ae2:spatial_anchor", "appeng.blockentity.spatial.SpatialAnchorBlockEntity",
              "ae2:spatial_anchor", "SMART", "ALL"),
    _endpoint("ae2:controller", "appeng.blockentity.networking.ControllerBlockEntity",
              "ae2:controller", "DENSE_SMART", "ALL"),
    _endpoint("ae2:drive", "appeng.blockentity.storage.DriveBlockEntity",
              "ae2:drive", "SMART", "NO_FRONT"),
    _endpoint("ae2:chest", "appeng.blockentity.storage.MEChestBlockEntity",
              "ae2:chest", "SMART", "ALL"),
    _endpoint("ae2:interface", "appeng.blockentity.misc.InterfaceBlockEntity",
              "ae2:interface", "SMART", "ALL"),
    _endpoint("ae2:io_port", "appeng.blockentity.storage.IOPortBlockEntity",
              "ae2:io_port", "SMART", "ALL"),
    _endpoint("ae2:energy_acceptor",
              "appeng.blockentity.networking.EnergyAcceptorBlockEntity",
              "ae2:energy_acceptor", "COVERED", "ALL"),
    _endpoint("ae2:crystal_resonance_generator",
              "appeng.blockentity.networking.CrystalResonanceGeneratorBlockEntity",
              "ae2:crystal_resonance_generator", "SMART", "BACK"),
    _endpoint("ae2:vibration_chamber",
              "appeng.blockentity.misc.VibrationChamberBlockEntity",
              "ae2:vibration_chamber", "COVERED", "ALL"),
    _endpoint("ae2:growth_accelerator",
              "appeng.blockentity.misc.GrowthAcceleratorBlockEntity",
              "ae2:growth_accelerator", "COVERED", "FRONT_BACK"),
    _endpoint("ae2:energy_cell", "appeng.blockentity.networking.EnergyCellBlockEntity",
              "ae2:energy_cell", "COVERED", "ALL"),
    _endpoint("ae2:dense_energy_cell",
              "appeng.blockentity.networking.EnergyCellBlockEntity",
              "ae2:dense_energy_cell", "COVERED", "ALL"),
    _endpoint("ae2:creative_energy_cell",
              "appeng.blockentity.networking.CreativeEnergyCellBlockEntity",
              "ae2:creative_energy_cell", "COVERED", "ALL"),
    _endpoint("ae2:crafting_unit", "appeng.blockentity.crafting.CraftingBlockEntity",
              "ae2:crafting_unit", "SMART", "FORMED_CRAFTING"),
    _endpoint("ae2:crafting_accelerator",
              "appeng.blockentity.crafting.CraftingBlockEntity",
              "ae2:crafting_unit", "SMART", "FORMED_CRAFTING"),
    _endpoint("ae2:1k_crafting_storage",
              "appeng.blockentity.crafting.CraftingBlockEntity",
              "ae2:crafting_storage", "SMART", "FORMED_CRAFTING"),
    _endpoint("ae2:4k_crafting_storage",
              "appeng.blockentity.crafting.CraftingBlockEntity",
              "ae2:crafting_storage", "SMART", "FORMED_CRAFTING"),
    _endpoint("ae2:16k_crafting_storage",
              "appeng.blockentity.crafting.CraftingBlockEntity",
              "ae2:crafting_storage", "SMART", "FORMED_CRAFTING"),
    _endpoint("ae2:64k_crafting_storage",
              "appeng.blockentity.crafting.CraftingBlockEntity",
              "ae2:crafting_storage", "SMART", "FORMED_CRAFTING"),
    _endpoint("ae2:256k_crafting_storage",
              "appeng.blockentity.crafting.CraftingBlockEntity",
              "ae2:crafting_storage", "SMART", "FORMED_CRAFTING"),
    _endpoint("ae2:crafting_monitor",
              "appeng.blockentity.crafting.CraftingMonitorBlockEntity",
              "ae2:crafting_monitor", "SMART", "FORMED_CRAFTING"),
    _endpoint("ae2:pattern_provider",
              "appeng.blockentity.crafting.PatternProviderBlockEntity",
              "ae2:pattern_provider", "SMART", "PUSH_DIRECTION"),
    _endpoint("ae2:molecular_assembler",
              "appeng.blockentity.crafting.MolecularAssemblerBlockEntity",
              "ae2:molecular_assembler", "COVERED", "ALL"),
)


def _unsupported_endpoint(
    block_id: str,
    block_entity_id: str,
    artifact: str,
) -> dict[str, str]:
    return {
        "id": block_id,
        "blockEntityId": block_entity_id,
        "artifact": artifact,
        "policy": KNOWN_UNSUPPORTED_COMPATIBLE_ENDPOINT_POLICY,
    }


_EXPANDED_AE_CRAFTING_IDS = (
    "expandedae:exp_crafting_unit",
    *(f"expandedae:exp_crafting_accelerator_{tier}" for tier in (
        "2", "4", "8", "16", "32", "64", "128", "256", "512", "1k",
        "2k", "4k", "8k", "16k", "32k", "64k", "128k", "256k",
        "512k", "1m",
    )),
)
_MEGA_CELLS_STORAGE_IDS = tuple(
    f"megacells:{tier}m_crafting_storage"
    for tier in ("1", "4", "16", "64", "256")
)
_ADVANCED_AE_QUANTUM_IDS = (
    "advanced_ae:quantum_unit",
    "advanced_ae:quantum_core",
    "advanced_ae:data_entangler",
    "advanced_ae:quantum_storage_128",
    "advanced_ae:quantum_storage_256",
    "advanced_ae:quantum_accelerator",
    "advanced_ae:quantum_multi_threader",
    "advanced_ae:quantum_structure",
)
_EXTENDED_AE_GRID_IDS = tuple(
    f"extendedae:{path}" for path in (
        "crystal_assembler",
        "ex_pattern_provider",
        "ex_interface",
        "wireless_connect",
        "ex_drive",
        "ex_molecular_assembler",
        "ex_inscriber",
        "ex_charger",
        "crystal_fixer",
        "caner",
        "ex_io_port",
        "circuit_cutter",
        "oversize_interface",
        "assembler_matrix_frame",
        "assembler_matrix_wall",
        "assembler_matrix_glass",
        "assembler_matrix_pattern",
        "assembler_matrix_crafter",
        "assembler_matrix_speed",
        "wireless_hub",
    )
)

UNSUPPORTED_COMPATIBLE_ENDPOINTS = (
    _unsupported_endpoint(
        "expandedae:exp_pattern_provider",
        "expandedae:exp_pattern_provider",
        "expandedae-2.1.1",
    ),
    _unsupported_endpoint(
        "expandedae:exp_io_port",
        "expandedae:exp_io_port",
        "expandedae-2.1.1",
    ),
    _unsupported_endpoint(
        "expandedae:colorable_drive",
        "expandedae:colorable_drive",
        "expandedae-2.1.1",
    ),
    *(
        _unsupported_endpoint(
            block_id,
            "expandedae:exp_cpus",
            "expandedae-2.1.1",
        )
        for block_id in _EXPANDED_AE_CRAFTING_IDS
    ),
    _unsupported_endpoint(
        "megacells:mega_energy_cell",
        "megacells:mega_energy_cell",
        "megacells-4.11.0",
    ),
    *(
        _unsupported_endpoint(
            block_id,
            "megacells:mega_crafting_unit",
            "megacells-4.11.0",
        )
        for block_id in (
            "megacells:mega_crafting_unit",
            "megacells:mega_crafting_accelerator",
        )
    ),
    *(
        _unsupported_endpoint(
            block_id,
            "megacells:mega_crafting_storage",
            "megacells-4.11.0",
        )
        for block_id in _MEGA_CELLS_STORAGE_IDS
    ),
    _unsupported_endpoint(
        "megacells:mega_crafting_monitor",
        "megacells:mega_crafting_monitor",
        "megacells-4.11.0",
    ),
    _unsupported_endpoint(
        "megacells:mega_interface",
        "megacells:mega_interface",
        "megacells-4.11.0",
    ),
    _unsupported_endpoint(
        "megacells:mega_pattern_provider",
        "megacells:mega_pattern_provider",
        "megacells-4.11.0",
    ),
    *(
        _unsupported_endpoint(
            block_id,
            "advanced_ae:quantum_core",
            "advanced_ae-1.6.12-1.21.1",
        )
        for block_id in _ADVANCED_AE_QUANTUM_IDS
    ),
    _unsupported_endpoint(
        "advanced_ae:adv_pattern_provider",
        "advanced_ae:adv_pattern_provider",
        "advanced_ae-1.6.12-1.21.1",
    ),
    _unsupported_endpoint(
        "advanced_ae:small_adv_pattern_provider",
        "advanced_ae:small_adv_pattern_provider",
        "advanced_ae-1.6.12-1.21.1",
    ),
    _unsupported_endpoint(
        "advanced_ae:reaction_chamber",
        "advanced_ae:reaction_chamber",
        "advanced_ae-1.6.12-1.21.1",
    ),
    _unsupported_endpoint(
        "advanced_ae:quantum_crafter",
        "advanced_ae:quantum_craft",
        "advanced_ae-1.6.12-1.21.1",
    ),
    *(
        _unsupported_endpoint(
            block_id,
            block_id,
            "extendedae-1.21-2.2.35-neoforge",
        )
        for block_id in _EXTENDED_AE_GRID_IDS
    ),
)

UNSUPPORTED_COMPATIBLE_ENDPOINT_ARTIFACTS = (
    {
        "id": "expandedae-2.1.1",
        "fileName": "expandedae-2.1.1.jar",
        "sizeBytes": 496_713,
        "sha1": "c4db013f83e569b016da329b3ddc9c14acc75d7d",
        "sha256": "f39c0eb9c6271f54a44ffee092a29520f53000d1005849e6afada3ad9dffba14",
        "sha512": (
            "5d6b0c7430d6f1f2bdb2cb38832ee27d0b28402d16171a9fe746d0275ba54c28"
            "8405b64b9ad269c010aadd729e82ddeb61b9550c0361c6e1ece2c0bdc77a4b23"
        ),
        "endpointCount": 24,
    },
    {
        "id": "megacells-4.11.0",
        "fileName": "megacells-4.11.0.jar",
        "sizeBytes": 1_137_276,
        "sha1": "f0b1a44bf30c8a9e14e2fa7fce37360191aa55e8",
        "sha256": "a386bbf12afb11729b0dcf77f64221893d250f22e6185a4d728b9799b230bc55",
        "sha512": (
            "1f5c30f5c6516eae20eb3d8502eebc8f3fa43d42815ecd182beea2c244c7dacf"
            "450fa0fafa6f6f7ab836d7f68e6de2b2366fbb0eb2938823f3d370217a4e8671"
        ),
        "endpointCount": 11,
    },
    {
        "id": "advanced_ae-1.6.12-1.21.1",
        "fileName": "AdvancedAE-1.6.12-1.21.1.jar",
        "sizeBytes": 4_791_255,
        "sha1": "9358ccfa5477c7ab1c5ffab6c831e105fe46ecc3",
        "sha256": "a01d9718667ac13899013e91c5b0b7708b9b9db1da9b8e380772dde54bbe8f41",
        "sha512": (
            "ab61c57355649a967a0bcf6b9413cd6b62728d26e914543b3231eea33bde5571"
            "536bd589ae1ac026d46799711508c942284c3419e19ff5d5bf80f1045442f33a"
        ),
        "endpointCount": 12,
    },
    {
        "id": "extendedae-1.21-2.2.35-neoforge",
        "fileName": "ExtendedAE-1.21-2.2.35-neoforge.jar",
        "sizeBytes": 5_578_031,
        "sha1": "e3521ca2567fabe0f0131cc923ec94dd99d6fa7b",
        "sha256": "14a2860fa2c747e9dda2279b8933fac6311fecfee166c765171022b902591c65",
        "sha512": (
            "e5b76a50802087d999bf6c113bc635e8ade9f20e06f4d3276a144f4eaa3090fc"
            "3b6c67b9b6a1f7d0d036e48e69f601114a0cc92c5a8d45953f895718f806348c"
        ),
        "source": {
            "tag": "1.21-2.2.35-neoforge",
            "commit": "3776bc854458301bbcc9a44a8238d70a0e3dc00d",
        },
        "endpointCount": 20,
    },
)

KNOWN_EXTENSION_FALLBACK_CONTROL = {
    "blockId": "expandedae:exp_io_port",
    "blockEntityId": "expandedae:exp_io_port",
    "artifact": "expandedae-2.1.1",
    "properties": {"facing": "north", "powered": "false", "spin": "0"},
    "blockstateSha256": (
        "9880448f15a4372dbfdda591d3728518df3433e7e5886cbe0b4366d74b55a76d"
    ),
    "endpointOffsetFromCable": "east",
    "contactSide": "west",
    "connectionEvidence": (
        "ExpIOPortBlockEntity-subclasses-AE2-IOPortBlockEntity-all-sides-grid-node"
    ),
    "expectedBranch": KNOWN_UNSUPPORTED_COMPATIBLE_ENDPOINT_POLICY,
}

ENDPOINT_SIDE_RULES = (
    {
        "id": "ALL",
        "stateInputs": [],
        "allowedSides": "all-six",
        "disallowedSides": "none",
        "topology": "none",
    },
    {
        "id": "BACK",
        "stateInputs": ["facing"],
        "allowedSides": "opposite-facing",
        "disallowedSides": "all-except-opposite-facing",
        "topology": "none",
    },
    {
        "id": "NO_FRONT",
        "stateInputs": ["facing"],
        "allowedSides": "all-except-facing",
        "disallowedSides": "facing",
        "topology": "none",
    },
    {
        "id": "FRONT_BACK",
        "stateInputs": ["facing"],
        "allowedSides": "facing-and-opposite-facing",
        "disallowedSides": "four-perpendicular-sides",
        "topology": "none",
    },
    {
        "id": "PUSH_DIRECTION",
        "stateInputs": ["push_direction"],
        "allowedSides": "all-if-all-else-all-except-push-direction",
        "disallowedSides": "none-if-all-else-push-direction",
        "topology": "none",
    },
    {
        "id": "FORMED_CRAFTING",
        "stateInputs": ["formed"],
        "allowedSides": "all-six-if-formed",
        "disallowedSides": "all-six-if-unformed",
        "topology": "trust-exact-persisted-formed-state-and-serialized-block-entity-id",
    },
    {
        "id": "FORMED_QUANTUM",
        "stateInputs": ["formed"],
        "allowedSides": "edge-ring-all-six-else-adjacent-quantum-bridges",
        "disallowedSides": "all-if-unformed-else-non-adjacent-for-link-or-corner",
        "topology": "three-by-three-plane-eight-rings-and-center-link",
    },
    {
        "id": "VALID_STRAIGHT_PYLON",
        "stateInputs": [],
        "allowedSides": "all-six-if-valid-straight-component",
        "disallowedSides": "all-six-if-unformed-or-branched",
        "topology": "straight-axis-line-of-at-least-two-pylons-bounded-256",
    },
)

ENDPOINT_SIDE_RULE_COUNTS = {
    "ALL": 12,
    "BACK": 2,
    "NO_FRONT": 3,
    "FRONT_BACK": 1,
    "PUSH_DIRECTION": 1,
    "FORMED_CRAFTING": 8,
    "FORMED_QUANTUM": 2,
    "VALID_STRAIGHT_PYLON": 1,
}

FACADE_SOURCE_PARITY_GOLDEN = {
    "thinThicknessBlocks": "1/16-0.002",
    "thinThicknessSixteenths": 0.968,
    "transparentClassification": "not-BlockState.isSolidRender-level-position",
    "transparentPerpendicularOpaqueInset": (
        "one-thin-thickness-on-each-masked-perpendicular-bound"
    ),
    "opaquePerpendicularFaceStripping": "facade-direction-bit-mask",
    "opaquePerpendicularInnerCornerKick": "one-thin-thickness",
    "cornerKickSourceEpsilon": {
        "unit": "block",
        "value": FACADE_CORNER_KICK_SOURCE_EPSILON_BLOCKS,
    },
    "cornerKickRuntimeEpsilon": {
        "unit": "sixteenth",
        "value": FACADE_CORNER_KICK_RUNTIME_EPSILON_SIXTEENTHS,
    },
    "cornerKickAnalyzerEpsilon": {
        "unit": "block",
        "value": FACADE_CORNER_KICK_ANALYZER_EPSILON_BLOCKS,
    },
    "uvAfterClamp": FACADE_UV_REINTERPOLATION_POLICY,
    "cardinalVariantTransform": FACADE_CARDINAL_VARIANT_TRANSFORM_POLICY,
    "quadShade": "source-BakedQuad.isShade",
    "quadShadeHostProjection": FACADE_DIRECTIONAL_SHADE_POLICY,
    "shadeFalseEligibility": "accepted-not-a-fallback-reason",
    "lightEmissionHostProjection": "represented",
    "quadAmbientOcclusion": "source-BakedQuad.hasAmbientOcclusion",
    "ambientOcclusionDirection": FACADE_AMBIENT_OCCLUSION_DIRECTION_POLICY,
    "mapColorIllumination": STRUCTURAL_MAP_COLOR_ILLUMINATION_POLICY,
    "quadTint": FACADE_TINT_POLICY,
    "weightedVariants": FACADE_WEIGHTED_VARIANT_POLICY,
    "adjacentCull": FACADE_SKIP_RENDERING_POLICY,
    "quartzCrossFamilySkipRendering": FACADE_QUARTZ_SKIP_RENDERING_POLICY,
    "cutoutStripAabbNormalization": FACADE_CUTOUT_STRIP_AABB_POLICY,
    "outputCullFace": "retain-only-when-original-cull-face-equals-facade-side",
}

FACADE_WHITELIST_BLOCK_IDS = (
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
FACADE_WHITELIST_OPTIONAL_TAGS = ("c:glass_blocks",)
_FACADE_WHITELIST_SAME_STATE_SKIP_RENDERING_TRUE_IDS = frozenset(
    {
        "ae2:quartz_glass",
        "ae2:quartz_vibrant_glass",
        "minecraft:honey_block",
    }
)
FACADE_WHITELIST_SAME_STATE_SKIP_RENDERING = {
    block_id: block_id in _FACADE_WHITELIST_SAME_STATE_SKIP_RENDERING_TRUE_IDS
    for block_id in FACADE_WHITELIST_BLOCK_IDS
}
FACADE_ORDINARY_SKIP_RENDERING_CONTROLS = {
    "minecraft:glass": True,
    "minecraft:oak_log": False,
    "minecraft:oak_leaves": False,
}
FACADE_WHITELIST_RESOURCE_SHA256 = (
    "4ff52f9d8670417406c29430f754305198ba8ab855ca34336962d6d24cf49f82"
)


def _native_facade_material(
    block_id: str,
    properties: dict[str, str],
    material_family: str,
    source_model: str,
    blockstate_sha256: str,
    solid_render: bool,
    block_state_light_emission: int,
    facade_quad_light_emission: int,
) -> dict[str, object]:
    if block_id in ("ae2:quartz_glass", "ae2:quartz_vibrant_glass"):
        valid_property_values: dict[str, list[str]] = {}
        normalization: dict[str, str] = {}
    elif block_id == "ae2:controller":
        valid_property_values = {
            "state": ["offline", "online", "conflicted"],
            "type": [
                "block", "column_x", "column_y", "column_z", "inside_a", "inside_b"
            ],
        }
        normalization = {"state": "offline", "type": "block"}
    elif block_id == "ae2:crafting_monitor":
        valid_property_values = {
            "facing": ["down", "up", "north", "south", "west", "east"],
            "formed": ["false", "true"],
            "powered": ["false", "true"],
            "spin": ["0", "1", "2", "3"],
        }
        normalization = {
            "facing": "preserve",
            "formed": "false",
            "powered": "false",
            "spin": "0",
        }
    else:
        valid_property_values = {
            "formed": ["false", "true"],
            "powered": ["false", "true"],
        }
        normalization = {"formed": "false", "powered": "false"}
    return {
        "blockId": block_id,
        "properties": properties,
        "materialFamily": material_family,
        "sourceModel": source_model,
        "blockstateSha256": blockstate_sha256,
        "solidRender": solid_render,
        "transparentFacade": not solid_render,
        "blockStateLightEmission": block_state_light_emission,
        "facadeQuadLightEmission": facade_quad_light_emission,
        "statePolicy": {
            "validPropertyValues": valid_property_values,
            "normalization": normalization,
            "unknownOrMalformed": "atomic-original-resource-fallback",
            "galleryPropertiesAreNeutralSampleOnly": True,
        },
    }


NATIVE_FACADE_NEUTRAL_MATERIALS = (
    _native_facade_material(
        "ae2:quartz_glass", {}, "facade-aware-connected-quartz-glass-static",
        "ae2:block/quartz_glass",
        "9c331aa0f423a364e136b731195caf168df6496a90a065f9699e5e8e37e70d50",
        False, 0, 0,
    ),
    _native_facade_material(
        "ae2:quartz_vibrant_glass", {},
        "facade-aware-connected-quartz-glass-static",
        "ae2:block/quartz_glass",
        "e3b2b20544e578ff4b9d908ca1e7d281ecc46ddd8f0ee496ad53e2e344e17a99",
        False, 15, 0,
    ),
    _native_facade_material(
        "ae2:controller", {"state": "offline", "type": "block"},
        "controller-offline-block", "ae2:block/controller/controller_block_offline",
        "693d04c733b47e4159052d0843256fa7520bbc1984b6d9e454bec976a73d2ca8",
        True, 0, 0,
    ),
    _native_facade_material(
        "ae2:1k_crafting_storage", {"formed": "false", "powered": "false"},
        "crafting-storage-1k-unformed", "ae2:block/crafting/1k_storage",
        "9a1f6383cd3b54a8361cefc46740ddbee587ce79baefccb6ad6de6355833a603",
        True, 0, 0,
    ),
    _native_facade_material(
        "ae2:4k_crafting_storage", {"formed": "false", "powered": "false"},
        "crafting-storage-4k-unformed", "ae2:block/crafting/4k_storage",
        "dd4210a4c0fc5b0eb7f524571f20b7e1a92c438bc68df7324cb26c939c726abc",
        True, 0, 0,
    ),
    _native_facade_material(
        "ae2:16k_crafting_storage", {"formed": "false", "powered": "false"},
        "crafting-storage-16k-unformed", "ae2:block/crafting/16k_storage",
        "8e04febb39f74e1bb1061f9fee979be9cc4923bf14cc5a5d619cf6e681d506a4",
        True, 0, 0,
    ),
    _native_facade_material(
        "ae2:64k_crafting_storage", {"formed": "false", "powered": "false"},
        "crafting-storage-64k-unformed", "ae2:block/crafting/64k_storage",
        "d8a1b0f2f21c2f05cd959f03213d0434c6bb41e27d5591d0c3c532aea142eb7f",
        True, 0, 0,
    ),
    _native_facade_material(
        "ae2:256k_crafting_storage", {"formed": "false", "powered": "false"},
        "crafting-storage-256k-unformed", "ae2:block/crafting/256k_storage",
        "3458c6e521a76f7a0761c7efe956cc587826cfdd40d1f7c6284100990fb68905",
        True, 0, 0,
    ),
    _native_facade_material(
        "ae2:crafting_monitor", {
            "facing": "north", "formed": "false", "powered": "false", "spin": "0"
        },
        "crafting-monitor-unformed-north", "ae2:block/crafting/monitor",
        "157e2a326b835180b369874b5f6978fab7c6796293945f85a971ac3f5b1cf2b7",
        True, 0, 0,
    ),
    _native_facade_material(
        "ae2:crafting_unit", {"formed": "false", "powered": "false"},
        "crafting-unit-unformed", "ae2:block/crafting/unit",
        "b33f03d38953281265d6196e2a9f2494974275901b570f390ebf40fa3a338ece",
        True, 0, 0,
    ),
    _native_facade_material(
        "ae2:crafting_accelerator", {"formed": "false", "powered": "false"},
        "crafting-accelerator-unformed", "ae2:block/crafting/accelerator",
        "f2b8fd7efa88b37968f55d8169eee48d84c1c673b5b2201719037771d5e18918",
        True, 0, 0,
    ),
)

_FACADE_BOOLEAN_DOMAIN = ["false", "true"]
_FACADE_DIRECTION_DOMAIN = ["down", "up", "north", "south", "west", "east"]
_FACADE_HORIZONTAL_DIRECTION_DOMAIN = ["north", "south", "west", "east"]
_VANILLA_FACADE_WHITELIST_STATE_SCHEMAS = {
    "minecraft:chiseled_bookshelf": {
        "facing": _FACADE_HORIZONTAL_DIRECTION_DOMAIN,
        "slot_0_occupied": _FACADE_BOOLEAN_DOMAIN,
        "slot_1_occupied": _FACADE_BOOLEAN_DOMAIN,
        "slot_2_occupied": _FACADE_BOOLEAN_DOMAIN,
        "slot_3_occupied": _FACADE_BOOLEAN_DOMAIN,
        "slot_4_occupied": _FACADE_BOOLEAN_DOMAIN,
        "slot_5_occupied": _FACADE_BOOLEAN_DOMAIN,
    },
    "minecraft:jukebox": {"has_record": _FACADE_BOOLEAN_DOMAIN},
    "minecraft:furnace": {
        "facing": _FACADE_HORIZONTAL_DIRECTION_DOMAIN,
        "lit": _FACADE_BOOLEAN_DOMAIN,
    },
    "minecraft:blast_furnace": {
        "facing": _FACADE_HORIZONTAL_DIRECTION_DOMAIN,
        "lit": _FACADE_BOOLEAN_DOMAIN,
    },
    "minecraft:dropper": {
        "facing": _FACADE_DIRECTION_DOMAIN,
        "triggered": _FACADE_BOOLEAN_DOMAIN,
    },
    "minecraft:dispenser": {
        "facing": _FACADE_DIRECTION_DOMAIN,
        "triggered": _FACADE_BOOLEAN_DOMAIN,
    },
    "minecraft:crafter": {
        "crafting": _FACADE_BOOLEAN_DOMAIN,
        "orientation": [
            "down_east", "down_north", "down_south", "down_west",
            "up_east", "up_north", "up_south", "up_west",
            "west_up", "east_up", "north_up", "south_up",
        ],
        "triggered": _FACADE_BOOLEAN_DOMAIN,
    },
    "minecraft:barrel": {
        "facing": _FACADE_DIRECTION_DOMAIN,
        "open": _FACADE_BOOLEAN_DOMAIN,
    },
    "minecraft:bee_nest": {
        "facing": _FACADE_HORIZONTAL_DIRECTION_DOMAIN,
        "honey_level": ["0", "1", "2", "3", "4", "5"],
    },
    "minecraft:beehive": {
        "facing": _FACADE_HORIZONTAL_DIRECTION_DOMAIN,
        "honey_level": ["0", "1", "2", "3", "4", "5"],
    },
    "minecraft:sculk_catalyst": {"bloom": _FACADE_BOOLEAN_DOMAIN},
    "minecraft:soul_sand": {},
    "minecraft:honey_block": {},
}

_VANILLA_FACADE_WHITELIST_NEUTRAL_STATES = {
    "minecraft:chiseled_bookshelf": {
        "properties": {
            "facing": "north",
            "slot_0_occupied": "false",
            "slot_1_occupied": "false",
            "slot_2_occupied": "false",
            "slot_3_occupied": "false",
            "slot_4_occupied": "false",
            "slot_5_occupied": "false",
        },
        "solidRender": True,
    },
    "minecraft:jukebox": {
        "properties": {"has_record": "false"},
        "solidRender": True,
    },
    "minecraft:furnace": {
        "properties": {"facing": "north", "lit": "false"},
        "solidRender": True,
    },
    "minecraft:blast_furnace": {
        "properties": {"facing": "north", "lit": "false"},
        "solidRender": True,
    },
    "minecraft:dropper": {
        "properties": {"facing": "north", "triggered": "false"},
        "solidRender": True,
    },
    "minecraft:dispenser": {
        "properties": {"facing": "north", "triggered": "false"},
        "solidRender": True,
    },
    "minecraft:crafter": {
        "properties": {
            "crafting": "false",
            "orientation": "north_up",
            "triggered": "false",
        },
        "solidRender": True,
    },
    "minecraft:barrel": {
        "properties": {"facing": "north", "open": "false"},
        "solidRender": True,
    },
    "minecraft:bee_nest": {
        "properties": {"facing": "north", "honey_level": "0"},
        "solidRender": True,
    },
    "minecraft:beehive": {
        "properties": {"facing": "north", "honey_level": "0"},
        "solidRender": True,
    },
    "minecraft:sculk_catalyst": {
        "properties": {"bloom": "false"},
        "solidRender": True,
    },
    "minecraft:soul_sand": {"properties": {}, "solidRender": True},
    "minecraft:honey_block": {"properties": {}, "solidRender": False},
}

_NATIVE_FACADE_NEUTRAL_BY_ID = {
    entry["blockId"]: entry for entry in NATIVE_FACADE_NEUTRAL_MATERIALS
}
FACADE_WHITELIST_STATE_SCHEMAS = {
    block_id: (
        _NATIVE_FACADE_NEUTRAL_BY_ID[block_id]["statePolicy"]
        ["validPropertyValues"]
        if block_id in _NATIVE_FACADE_NEUTRAL_BY_ID
        else _VANILLA_FACADE_WHITELIST_STATE_SCHEMAS[block_id]
    )
    for block_id in FACADE_WHITELIST_BLOCK_IDS
}
_VANILLA_FACADE_WHITELIST_BLOCKSTATE_SHA256 = {
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
FACADE_WHITELIST_BLOCKSTATE_SHA256 = {
    block_id: (
        _NATIVE_FACADE_NEUTRAL_BY_ID[block_id]["blockstateSha256"]
        if block_id in _NATIVE_FACADE_NEUTRAL_BY_ID
        else _VANILLA_FACADE_WHITELIST_BLOCKSTATE_SHA256[block_id]
    )
    for block_id in FACADE_WHITELIST_BLOCK_IDS
}
FACADE_WHITELIST_NEUTRAL_STATES = tuple(
    {
        "blockId": block_id,
        "properties": (
            _NATIVE_FACADE_NEUTRAL_BY_ID[block_id]["properties"]
            if block_id in _NATIVE_FACADE_NEUTRAL_BY_ID
            else _VANILLA_FACADE_WHITELIST_NEUTRAL_STATES[block_id]["properties"]
        ),
        "solidRender": (
            _NATIVE_FACADE_NEUTRAL_BY_ID[block_id]["solidRender"]
            if block_id in _NATIVE_FACADE_NEUTRAL_BY_ID
            else _VANILLA_FACADE_WHITELIST_NEUTRAL_STATES[block_id]["solidRender"]
        ),
        "sameStateSkipRendering": (
            FACADE_WHITELIST_SAME_STATE_SKIP_RENDERING[block_id]
        ),
    }
    for block_id in FACADE_WHITELIST_BLOCK_IDS
)

SOURCE_ONLY_TEXTURES = (
    "ae2:part/annihilation_plane",
    "ae2:part/formation_plane",
    "ae2:part/plane_sides",
    "ae2:part/p2p_tunnel_frequency",
)

EXPECTED_FROZEN_OUTPUT_SHA256 = {
    "src/main/resources/bluemap-ae2/profiles/ae2/19.2.17/profile.json": (
        "2c27976a718834dbc97b3eb7cac6543c4ad2a966737c7bccbadb2b1c39c837e8"
    ),
    "src/main/resources/bluemap-ae2/profiles/ae2/19.2.17/required-resources.sha256": (
        "408297def444f1392b7b87fdc4b8520099513b4c57c63a4176b808ce61b4e1be"
    ),
    "src/main/resources/bluemap-ae2/profiles/ae2/19.2.17/routes/quartz-glass/profile.json": (
        "548e5bc00ef07c6d6b93b346422b596882ec11ca03de006065fa45fecb991200"
    ),
    "src/main/resources/bluemap-ae2/profiles/ae2/19.2.17/routes/quartz-glass/required-resources.tsv": (
        "b51c708e7c4d26093c1b6f85b88d0be50572d3cfa76dbf802720f6ad79c7a7fa"
    ),
    "src/main/resources/bluemap-ae2/profiles/ae2/19.2.17/routes/crafting/profile.json": (
        "676f63473b4827bb952a7c1f3fb457a6a03c3bfd0fb4b29a122b9f57468ba0f7"
    ),
    "src/main/resources/bluemap-ae2/profiles/ae2/19.2.17/routes/crafting/required-resources.tsv": (
        "dc474ba6ce7c4c2d53778827b1c1f9b4994594ea984ed7a2cbd62c40e1bc1183"
    ),
    "src/main/resources/bluemap-ae2/profiles/ae2/19.2.17/routes/quantum-bridge/profile.json": (
        "21afa152e3f56d8bdde9f602748c0efbca52a2c55d5dd7a836adca267c65480e"
    ),
    "src/main/resources/bluemap-ae2/profiles/ae2/19.2.17/routes/quantum-bridge/required-resources.tsv": (
        "717eed1ada75fb43c1324792c147cd8c2308d8c73ee82bf52d8de6bad4f74ed9"
    ),
    "src/main/resources/bluemap-ae2/profiles/ae2/19.2.17/routes/m3-completion/profile.json": (
        "281a335d3024ebbb97c6268e768826c467d6f7ea660989fd3dae204c6c03abf3"
    ),
    "src/main/resources/bluemap-ae2/profiles/ae2/19.2.17/routes/m3-completion/required-resources.tsv": (
        "3faf7f29e2878f5525541bad855cbc66b6d45786dc8fc6ee29a6fbbf4878cca1"
    ),
    "src/main/resources/bluemap-ae2/profiles/extendedae/1.21-2.2.33-neoforge/profile.json": (
        "eab467f46c27974e1f7d54fe749366b92eacd8d63d57bad1e8f3e452d82ad1df"
    ),
    "src/main/resources/bluemap-ae2/profiles/extendedae/1.21-2.2.33-neoforge/required-resources.tsv": (
        "5e72f79f45a3b120a89cf8b7a1fa15ce41bebaae62a63c6f3305ef40bd5d24ee"
    ),
}

ACCEPTED_ARTIFACTS = (
    {"milestone": "M0", "sizeBytes": 123_527,
     "sha256": "84a3b972d86a49a723e56a820ff3b59039654e2108d8ba965493d2302a5b1e41"},
    {"milestone": "M1", "sizeBytes": 161_930,
     "sha256": "e02beee7fdafeba9c3ef0ea42deda0a7709cc70df23d4778cfb7a72b1fdaf2e1"},
    {"milestone": "M2", "sizeBytes": 203_599,
     "sha256": "fc11af62359746990a2b35470c1da66e606b13a36be33a5b854d343eebb108d2"},
    {"milestone": "M3a", "sizeBytes": 259_005,
     "sha256": "55a11805373aebfde821e5009723ec7d672fb290127dbc60131ffa344c99518a"},
    {"milestone": "M3b", "sizeBytes": 323_416,
     "sha256": "f02123cb602bb7b6466d1529c5518e45862f53f413ce9a75ecc067d1a30607d1"},
    {"milestone": "M3c", "sizeBytes": 375_558,
     "sha256": "4c1b557ae4c79c738005b74e2f0c89ca4fbe503dd6ef0ba614fae34d8e449d47"},
    {"milestone": "M3d", "sizeBytes": 448_915,
     "sha256": "ca057f025338150255ea916402c08bc8b614f9398a063e7433bbe468808c93ee"},
    {"milestone": "M3e", "sizeBytes": 513_674,
     "sha256": "98ff55eaba609fc894b01e0c4d922b47f1871c324945f88f7a34864cf48b124f"},
    {"milestone": "M3f", "sizeBytes": 623_591,
     "sha256": "ca67c0fc433e43f8e0801ed8d2cccfe47aae317fbc329c099bc8cd741ec3b42b"},
)

ACCEPTED_GALLERY_SCHEMAS = (
    {"milestone": "M2", "schemaVersion": 3,
     "sha256": "46f1be884675b27d8b6a599ffb9cfafd28610a03fd6f13a9b215cd887a9edadb"},
    {"milestone": "M3a", "schemaVersion": 4, "sizeBytes": 550_496,
     "sha256": "95e9398ed6c9bf3edf7ceb910b84329fba0227037c003837c628ee0fb657f339"},
    {"milestone": "M3b", "schemaVersion": 5, "sizeBytes": 1_143_610,
     "sha256": "5a1297668b6922b03ae3f2ab089b643aa9521060e61607bbbae353a80d8494fc"},
    {"milestone": "M3c", "schemaVersion": 6, "sizeBytes": 1_517_248,
     "sha256": "2d4fbba58ea2c4d3ed741e93a8dd9857523cac9cda021ffd3111e6ac51aec602"},
    {"milestone": "M3d", "schemaVersion": 7, "sizeBytes": 3_030_512,
     "sha256": "c60d2afff5a1f92da4972963fcb926c38093f43bb6d7f550799f104349728a38"},
    {"milestone": "M3e", "schemaVersion": 8, "sizeBytes": 3_123_572,
     "sha256": "93963dd0bb60a276e1a17c6dd1f4eb916cd92bef4ef30a2e8bdc7a2bfa818b3e"},
    {"milestone": "M3f", "schemaVersion": 9, "sizeBytes": 3_314_082,
     "sha256": "75e6ba2f40631a95f20cfa00d7ca952e521bc2c7a4eb155926334a223a945f3a"},
)


def _split_identifier(identifier: str) -> tuple[str, str]:
    if ":" not in identifier:
        return "minecraft", identifier
    namespace, path = identifier.split(":", 1)
    if not namespace or not path:
        raise ValueError(f"malformed resource identifier: {identifier}")
    return namespace, path


def model_resource_path(identifier: str) -> str:
    namespace, path = _split_identifier(identifier)
    return f"assets/{namespace}/models/{path}.json"


def texture_resource_path(identifier: str) -> str:
    namespace, path = _split_identifier(identifier)
    return f"assets/{namespace}/textures/{path}.png"


def direct_model_roots() -> tuple[str, ...]:
    roots: list[str] = []
    for part in FACE_PARTS:
        for model in part["neutralModels"]:
            if model not in roots:
                roots.append(model)
    if len(roots) != DIRECT_NEUTRAL_RESOURCE_COUNT:
        raise ValueError("native structural direct-model root count changed")
    return tuple(roots)


def resource_rows(archive: zipfile.ZipFile) -> tuple[tuple[str, int, str], ...]:
    names = [entry.filename for entry in archive.infolist()]
    if len(names) != len(set(names)):
        raise ValueError("artifact contains duplicate ZIP entry names")
    available = set(names)
    for endpoint_id, expected_digest in ENDPOINT_BLOCKSTATE_SHA256.items():
        namespace, path_name = _split_identifier(endpoint_id)
        blockstate_path = f"assets/{namespace}/blockstates/{path_name}.json"
        if blockstate_path not in available:
            raise ValueError(
                f"artifact is missing native endpoint blockstate: {blockstate_path}"
            )
        if hashlib.sha256(archive.read(blockstate_path)).hexdigest() != expected_digest:
            raise ValueError(
                f"native endpoint blockstate changed: {blockstate_path}"
            )
    models: dict[str, dict[str, object]] = {}
    pending = list(direct_model_roots())
    while pending:
        identifier = pending.pop()
        path = model_resource_path(identifier)
        if path in models:
            continue
        if path not in available:
            raise ValueError(f"artifact is missing required model: {path}")
        value = json.loads(archive.read(path).decode("utf-8"))
        if not isinstance(value, dict):
            raise ValueError(f"required model is not a JSON object: {path}")
        models[path] = value
        parent = value.get("parent")
        if parent is not None:
            if not isinstance(parent, str):
                raise ValueError(f"required model has a non-string parent: {path}")
            if not parent.startswith("builtin/"):
                pending.append(parent)

    textures = set(SOURCE_ONLY_TEXTURES)
    for path, model in models.items():
        table = model.get("textures", {})
        if not isinstance(table, dict):
            raise ValueError(f"required model has a non-object texture table: {path}")
        for value in table.values():
            if not isinstance(value, str):
                raise ValueError(f"required model has a non-string texture: {path}")
            if not value.startswith("#") and value != "_type_":
                textures.add(value)

    paths = sorted((*models, *(texture_resource_path(value) for value in textures)))
    if len(models) != TRANSITIVE_JSON_RESOURCE_COUNT:
        raise ValueError("native structural transitive JSON count changed")
    if len(textures) != PNG_RESOURCE_COUNT or len(paths) != REQUIRED_RESOURCE_COUNT:
        raise ValueError("native structural resource closure count changed")
    rows = []
    for path in paths:
        if path not in available:
            raise ValueError(f"artifact is missing required resource: {path}")
        raw = archive.read(path)
        rows.append((path, len(raw), hashlib.sha256(raw).hexdigest()))
    if sum(size for _, size, _ in rows) != REQUIRED_RESOURCE_BYTES:
        raise ValueError("native structural resource closure byte size changed")
    return tuple(rows)


def checksum_manifest(rows: tuple[tuple[str, int, str], ...]) -> bytes:
    return "".join(f"{digest}  {path}\n" for path, _, digest in rows).encode()


def size_manifest(rows: tuple[tuple[str, int, str], ...]) -> bytes:
    return "".join(
        f"{path}\t{size}\t{digest}\n" for path, size, digest in rows
    ).encode()


def parse_checksum_manifest(raw: bytes) -> dict[str, str]:
    result: dict[str, str] = {}
    for line in raw.decode("utf-8").splitlines():
        fields = line.split("  ", 1)
        if (len(fields) != 2 or len(fields[0]) != 64
                or any(character not in "0123456789abcdef" for character in fields[0])
                or not fields[1]):
            raise ValueError("checksum manifest contains a malformed row")
        if fields[1] in result:
            raise ValueError(f"duplicate checksum manifest path: {fields[1]}")
        result[fields[1]] = fields[0]
    if list(result) != sorted(result):
        raise ValueError("checksum manifest is not sorted by path")
    return result


def parse_size_manifest(raw: bytes) -> dict[str, tuple[int, str]]:
    result: dict[str, tuple[int, str]] = {}
    for line in raw.decode("utf-8").splitlines():
        fields = line.split("\t")
        if (len(fields) != 3 or not fields[0] or not fields[1].isascii()
                or not fields[1].isdecimal() or fields[1].startswith("0")
                or len(fields[2]) != 64
                or any(character not in "0123456789abcdef" for character in fields[2])):
            raise ValueError("size manifest contains a malformed row")
        if fields[0] in result:
            raise ValueError(f"duplicate size manifest path: {fields[0]}")
        result[fields[0]] = (int(fields[1]), fields[2])
    if list(result) != sorted(result):
        raise ValueError("size manifest is not sorted by path")
    return result


def _profile_part(part: dict[str, object]) -> dict[str, object]:
    return {
        "id": part["id"],
        "group": part["group"],
        "sourceClass": part["sourceClass"],
        "neutralModels": list(part["neutralModels"]),
        "spin": part["spin"],
        "denseCenter": part["denseCenter"],
    }


def _validate_endpoint_contract() -> None:
    endpoint_ids = [endpoint["id"] for endpoint in ENDPOINTS]
    if len(endpoint_ids) != NATIVE_ENDPOINT_COUNT or len(set(endpoint_ids)) != len(
        endpoint_ids
    ):
        raise ValueError("native structural endpoint catalog changed")
    rule_ids = [rule["id"] for rule in ENDPOINT_SIDE_RULES]
    if len(rule_ids) != ENDPOINT_SIDE_RULE_KIND_COUNT or len(set(rule_ids)) != len(
        rule_ids
    ):
        raise ValueError("native structural endpoint side-rule catalog changed")
    if rule_ids != list(ENDPOINT_SIDE_RULE_COUNTS):
        raise ValueError("native structural endpoint side-rule order changed")
    actual_counts = {
        rule: sum(endpoint["sideRule"] == rule for endpoint in ENDPOINTS)
        for rule in rule_ids
    }
    if actual_counts != ENDPOINT_SIDE_RULE_COUNTS:
        raise ValueError("native structural endpoint side-rule totals changed")
    required_keys = {
        "id",
        "blockEntityClass",
        "blockEntityId",
        "cableType",
        "sideRule",
        "stateProperties",
        "blockstateSha256",
        "topologyClass",
    }
    if (
        list(ENDPOINT_STATE_SCHEMAS) != endpoint_ids
        or list(ENDPOINT_BLOCKSTATE_SHA256) != endpoint_ids
        or sum(
            math.prod(len(values) for values in schema.values())
            for schema in ENDPOINT_STATE_SCHEMAS.values()
        )
        != ENDPOINT_STATE_COMBINATION_COUNT
        or ENDPOINT_STATE_COMBINATION_COUNT * FACE_COUNT
        != ENDPOINT_STATE_SIDE_COMBINATION_COUNT
    ):
        raise ValueError("native structural endpoint state-schema order changed")
    rule_inputs = {
        rule["id"]: set(rule["stateInputs"])
        for rule in ENDPOINT_SIDE_RULES
    }
    for endpoint in ENDPOINTS:
        if set(endpoint) != required_keys:
            raise ValueError("native structural endpoint fields changed")
        state_properties = endpoint["stateProperties"]
        if (
            not endpoint["id"].startswith("ae2:")
            or not endpoint["blockEntityId"].startswith("ae2:")
            or endpoint["sideRule"] not in ENDPOINT_SIDE_RULE_COUNTS
            or endpoint["cableType"] not in {"SMART", "COVERED", "DENSE_SMART"}
            or state_properties != ENDPOINT_STATE_SCHEMAS[endpoint["id"]]
            or not isinstance(state_properties, dict)
            or any(
                not isinstance(name, str)
                or not name
                or not isinstance(values, list)
                or not values
                or len(values) != len(set(values))
                or any(not isinstance(value, str) or not value for value in values)
                for name, values in state_properties.items()
            )
            or not rule_inputs[endpoint["sideRule"]].issubset(state_properties)
            or endpoint["blockstateSha256"]
            != ENDPOINT_BLOCKSTATE_SHA256[endpoint["id"]]
            or len(endpoint["blockstateSha256"]) != 64
            or endpoint["topologyClass"] != "native-grid-node-host"
        ):
            raise ValueError("native structural endpoint evidence changed")
    unsupported_ids = [entry["id"] for entry in UNSUPPORTED_COMPATIBLE_ENDPOINTS]
    if (
        len(unsupported_ids) != UNSUPPORTED_COMPATIBLE_ENDPOINT_COUNT
        or len(set(unsupported_ids)) != len(unsupported_ids)
        or set(unsupported_ids).intersection(endpoint_ids)
    ):
        raise ValueError("native structural unsupported-compatible catalog changed")
    artifact_counts = {
        artifact["id"]: artifact["endpointCount"]
        for artifact in UNSUPPORTED_COMPATIBLE_ENDPOINT_ARTIFACTS
    }
    actual_artifact_counts = {
        artifact: sum(
            endpoint["artifact"] == artifact
            for endpoint in UNSUPPORTED_COMPATIBLE_ENDPOINTS
        )
        for artifact in artifact_counts
    }
    if actual_artifact_counts != artifact_counts:
        raise ValueError("native structural extension endpoint totals changed")
    for endpoint in UNSUPPORTED_COMPATIBLE_ENDPOINTS:
        if (
            set(endpoint) != {"id", "blockEntityId", "artifact", "policy"}
            or endpoint["artifact"] not in artifact_counts
            or endpoint["policy"] != KNOWN_UNSUPPORTED_COMPATIBLE_ENDPOINT_POLICY
            or ":" not in endpoint["id"]
            or ":" not in endpoint["blockEntityId"]
        ):
            raise ValueError("native structural extension endpoint evidence changed")


def _validate_part_collision_contract() -> None:
    part_ids = [part["id"] for part in FACE_PARTS]
    if list(PART_COLLISION_POLICIES) != part_ids:
        raise ValueError("native structural part collision order changed")
    expected_plane_bound_bits_by_face = {
        "down": {
            "minX": "right", "maxX": "left", "minY": "down", "maxY": "up",
        },
        "up": {
            "minX": "left", "maxX": "right", "minY": "up", "maxY": "down",
        },
        "north": {
            "minX": "left", "maxX": "right", "minY": "down", "maxY": "up",
        },
        "south": {
            "minX": "left", "maxX": "right", "minY": "down", "maxY": "up",
        },
        "west": {
            "minX": "right", "maxX": "left", "minY": "down", "maxY": "up",
        },
        "east": {
            "minX": "right", "maxX": "left", "minY": "down", "maxY": "up",
        },
    }
    if PLANE_COLLISION_BOUND_BITS_BY_FACE != expected_plane_bound_bits_by_face:
        raise ValueError("native structural face-aware plane collision bits changed")
    expected_dynamic_sheet = {
        "base": [1, 1, 15, 15, 15, 16],
        "coordinateSpace": "BusCollisionHelper-installed-face-local",
        "boundBitsByInstalledFace": expected_plane_bound_bits_by_face,
        "minXExtendsTo": 0,
        "minYExtendsTo": 0,
        "maxXExtendsTo": 16,
        "maxYExtendsTo": 16,
    }
    if _PLANE_COLLISION["dynamicSheet"] != expected_dynamic_sheet:
        raise ValueError("native structural plane collision sheet changed")
    for part_id, policy in PART_COLLISION_POLICIES.items():
        mode = policy.get("mode")
        if mode == "static":
            boxes = policy.get("boxes")
        elif mode == "same-side-facade-conditioned":
            boxes = (
                policy.get("withoutSameSideFacade", [])
                + policy.get("withSameSideFacade", [])
            )
        elif mode == "plane-connection-mask":
            boxes = policy.get("fixedBoxes", []) + [
                policy.get("dynamicSheet", {}).get("base", [])
            ]
        else:
            raise ValueError(f"unsupported part collision mode for {part_id}")
        if not boxes:
            raise ValueError(f"empty part collision policy for {part_id}")
        for box in boxes:
            if (
                len(box) != 6
                or any(not isinstance(value, int) for value in box)
                or not all(0 <= value <= 16 for value in box)
                or not all(box[index] < box[index + 3] for index in range(3))
            ):
                raise ValueError(f"invalid part collision box for {part_id}")


def _validate_facade_contract() -> None:
    if (
        len(FACADE_WHITELIST_BLOCK_IDS) != FACADE_WHITELIST_BLOCK_COUNT
        or len(set(FACADE_WHITELIST_BLOCK_IDS)) != FACADE_WHITELIST_BLOCK_COUNT
        or len(FACADE_WHITELIST_OPTIONAL_TAGS)
        != FACADE_WHITELIST_OPTIONAL_TAG_COUNT
    ):
        raise ValueError("native structural facade whitelist changed")
    materials = list(NATIVE_FACADE_NEUTRAL_MATERIALS)
    if (
        len(materials) != NATIVE_FACADE_NEUTRAL_MATERIAL_COUNT
        or len({entry["blockId"] for entry in materials}) != len(materials)
        or [entry["blockId"] for entry in materials]
        != list(FACADE_WHITELIST_BLOCK_IDS[:2] + FACADE_WHITELIST_BLOCK_IDS[15:24])
    ):
        raise ValueError("native neutral facade material order changed")
    for entry in materials:
        state_policy = entry.get("statePolicy")
        valid_values = (
            state_policy.get("validPropertyValues")
            if isinstance(state_policy, dict)
            else None
        )
        normalization = (
            state_policy.get("normalization")
            if isinstance(state_policy, dict)
            else None
        )
        if (
            set(entry) != {
                "blockId", "properties", "materialFamily", "sourceModel",
                "blockstateSha256", "solidRender", "transparentFacade",
                "blockStateLightEmission", "facadeQuadLightEmission", "statePolicy",
            }
            or not isinstance(state_policy, dict)
            or set(state_policy) != {
                "validPropertyValues", "normalization", "unknownOrMalformed",
                "galleryPropertiesAreNeutralSampleOnly",
            }
            or state_policy["unknownOrMalformed"]
            != "atomic-original-resource-fallback"
            or state_policy["galleryPropertiesAreNeutralSampleOnly"] is not True
            or not isinstance(entry["properties"], dict)
            or not isinstance(valid_values, dict)
            or not isinstance(normalization, dict)
            or set(entry["properties"]) != set(valid_values)
            or set(valid_values) != set(normalization)
            or any(
                not isinstance(values, list)
                or not values
                or len(values) != len(set(values))
                or entry["properties"][name] not in values
                or (
                    normalization[name] != "preserve"
                    and normalization[name] not in values
                )
                for name, values in valid_values.items()
            )
            or not isinstance(entry["solidRender"], bool)
            or entry["transparentFacade"] == entry["solidRender"]
            or not 0 <= entry["blockStateLightEmission"] <= 15
            or not 0 <= entry["facadeQuadLightEmission"] <= 15
            or len(entry["blockstateSha256"]) != 64
        ):
            raise ValueError("native neutral facade material evidence changed")
    neutral_states = list(FACADE_WHITELIST_NEUTRAL_STATES)
    if (
        len(neutral_states) != FACADE_WHITELIST_NEUTRAL_STATE_COUNT
        or [entry["blockId"] for entry in neutral_states]
        != list(FACADE_WHITELIST_BLOCK_IDS)
        or any(
            set(entry) != {
                "blockId",
                "properties",
                "solidRender",
                "sameStateSkipRendering",
            }
            or not isinstance(entry["properties"], dict)
            or not isinstance(entry["solidRender"], bool)
            or not isinstance(entry["sameStateSkipRendering"], bool)
            or entry["sameStateSkipRendering"]
            is not FACADE_WHITELIST_SAME_STATE_SKIP_RENDERING[entry["blockId"]]
            for entry in neutral_states
        )
        or len(FACADE_WHITELIST_STATE_SCHEMAS)
        != FACADE_WHITELIST_STATE_SCHEMA_COUNT
        or list(FACADE_WHITELIST_STATE_SCHEMAS)
        != list(FACADE_WHITELIST_BLOCK_IDS)
        or sum(
            math.prod(len(values) for values in schema.values())
            for schema in FACADE_WHITELIST_STATE_SCHEMAS.values()
        )
        != FACADE_WHITELIST_STATE_COMBINATION_COUNT
        or sum(
            math.prod(len(values) for values in schema.values())
            for schema, neutral in zip(
                FACADE_WHITELIST_STATE_SCHEMAS.values(), neutral_states
            )
            if neutral["solidRender"]
        )
        != FACADE_WHITELIST_SOLID_RENDER_TRUE_STATE_COUNT
        or sum(
            math.prod(len(values) for values in schema.values())
            for schema, neutral in zip(
                FACADE_WHITELIST_STATE_SCHEMAS.values(), neutral_states
            )
            if neutral["sameStateSkipRendering"]
        )
        != FACADE_WHITELIST_SAME_STATE_SKIP_RENDERING_TRUE_STATE_COUNT
        or list(FACADE_WHITELIST_BLOCKSTATE_SHA256)
        != list(FACADE_WHITELIST_BLOCK_IDS)
        or any(
            len(digest) != 64
            or any(character not in "0123456789abcdef" for character in digest)
            for digest in FACADE_WHITELIST_BLOCKSTATE_SHA256.values()
        )
        or any(
            not isinstance(schema, dict)
            or set(schema) != set(neutral_states[index]["properties"])
            or any(
                not isinstance(values, list)
                or not values
                or len(values) != len(set(values))
                or neutral_states[index]["properties"][property_name] not in values
                for property_name, values in schema.items()
            )
            for index, schema in enumerate(
                FACADE_WHITELIST_STATE_SCHEMAS.values()
            )
        )
        or [
            entry["blockId"]
            for entry in neutral_states
            if entry["solidRender"] is False
        ]
        != [
            "ae2:quartz_glass",
            "ae2:quartz_vibrant_glass",
            "minecraft:honey_block",
        ]
        or len(FACADE_WHITELIST_SAME_STATE_SKIP_RENDERING)
        != FACADE_WHITELIST_SAME_STATE_SKIP_RENDERING_COUNT
        or list(FACADE_WHITELIST_SAME_STATE_SKIP_RENDERING)
        != list(FACADE_WHITELIST_BLOCK_IDS)
        or [
            block_id
            for block_id, skip_rendering in (
                FACADE_WHITELIST_SAME_STATE_SKIP_RENDERING.items()
            )
            if skip_rendering
        ]
        != [
            "ae2:quartz_glass",
            "ae2:quartz_vibrant_glass",
            "minecraft:honey_block",
        ]
        or len(FACADE_ORDINARY_SKIP_RENDERING_CONTROLS)
        != FACADE_ORDINARY_SKIP_RENDERING_CONTROL_COUNT
        or FACADE_ORDINARY_SKIP_RENDERING_CONTROLS
        != {
            "minecraft:glass": True,
            "minecraft:oak_log": False,
            "minecraft:oak_leaves": False,
        }
        or FACADE_CORNER_KICK_RUNTIME_EPSILON_SIXTEENTHS
        != FACADE_CORNER_KICK_SOURCE_EPSILON_BLOCKS * 16
        or FACADE_CORNER_KICK_ANALYZER_EPSILON_BLOCKS
        != FACADE_CORNER_KICK_SOURCE_EPSILON_BLOCKS
    ):
        raise ValueError("facade whitelist neutral state evidence changed")


def profile(checksums: bytes, sizes: bytes) -> dict[str, object]:
    _validate_endpoint_contract()
    _validate_part_collision_contract()
    _validate_facade_contract()
    checksum_values = parse_checksum_manifest(checksums)
    size_values = parse_size_manifest(sizes)
    if checksum_values != {path: digest for path, (_, digest) in size_values.items()}:
        raise ValueError("native structural resource manifests disagree")
    if len(checksum_values) != REQUIRED_RESOURCE_COUNT:
        raise ValueError("native structural profile resource count changed")
    return {
        "schemaVersion": SCHEMA_VERSION,
        "profileId": PROFILE_ID,
        "routeResourceRoot": ROUTE_RESOURCE_ROOT,
        "modId": "ae2",
        "version": VERSION,
        "artifact": "appliedenergistics2-19.2.17.jar",
        "sizeBytes": EXPECTED_SIZE,
        "sha1": EXPECTED_SHA1,
        "sha256": EXPECTED_SHA256,
        "sha512": EXPECTED_SHA512,
        "source": {
            "tag": "neoforge/v19.2.17",
            "commit": SOURCE_COMMIT,
            "artifact": "appliedenergistics2-19.2.17-sources.jar",
            "sizeBytes": EXPECTED_SOURCES_SIZE,
            "sha1": EXPECTED_SOURCES_SHA1,
            "sha256": EXPECTED_SOURCES_SHA256,
            "sha512": EXPECTED_SOURCES_SHA512,
        },
        "pack": {
            "name": PACK_NAME,
            "version": PACK_VERSION,
            "commit": PACK_COMMIT,
        },
        "minecraft": MINECRAFT_VERSION,
        "neoforge": NEOFORGE_VERSION,
        "coverageMilestone": "S1",
        "buildAcceptance": "local-build-candidate",
        "runtimeAcceptance": "technical-lifecycle-pending",
        "humanAcceptance": "pending",
        "nativeFaceParts": [_profile_part(part) for part in FACE_PARTS],
        "nativeEndpoints": list(ENDPOINTS),
        "knownUnsupportedCompatibleEndpoints": {
            "policy": KNOWN_UNSUPPORTED_COMPATIBLE_ENDPOINT_POLICY,
            "count": UNSUPPORTED_COMPATIBLE_ENDPOINT_COUNT,
            "artifacts": list(UNSUPPORTED_COMPATIBLE_ENDPOINT_ARTIFACTS),
            "entries": list(UNSUPPORTED_COMPATIBLE_ENDPOINTS),
            "representativeControl": KNOWN_EXTENSION_FALLBACK_CONTROL,
        },
        "endpointSidePolicy": {
            "serializedBlockEntityId": "exact-required",
            "persistedBlockState": (
                "exact-complete-key-set-and-serialized-value-domain-required"
            ),
            "stateSchemaCount": ENDPOINT_STATE_SCHEMA_COUNT,
            "stateCartesianCount": ENDPOINT_STATE_COMBINATION_COUNT,
            "stateSideCartesianCount": ENDPOINT_STATE_SIDE_COMBINATION_COUNT,
            "blockstateResourceDigestCount": ENDPOINT_STATE_SCHEMA_COUNT,
            "unknownBlockEntityStateOrTopology": FALLBACK_POLICY,
            "branchPolicies": {
                "malformedNativeEndpoint": MALFORMED_NATIVE_ENDPOINT_POLICY,
                "knownExtensionExactBlockAndBlockEntity": (
                    KNOWN_UNSUPPORTED_COMPATIBLE_ENDPOINT_POLICY
                ),
                "knownExtensionBlockWithMissingOrWrongBlockEntity": (
                    MALFORMED_KNOWN_EXTENSION_ENDPOINT_POLICY
                ),
                "unrelatedBlockWithCatalogBlockEntity": "disconnected",
                "unrelatedNonNativeBlockEntity": "disconnected",
            },
            "ruleKindCount": ENDPOINT_SIDE_RULE_KIND_COUNT,
            "ruleCounts": ENDPOINT_SIDE_RULE_COUNTS,
            "rules": list(ENDPOINT_SIDE_RULES),
        },
        "orientationPolicy": {
            "faces": FACE_COUNT,
            "persistedSpinValues": [0, 1, 2, 3],
            "spinCapablePartCount": SPIN_CAPABLE_PART_COUNT,
            "spinIgnoredPartCount": FACE_PART_COUNT - SPIN_CAPABLE_PART_COUNT,
            "exhaustiveStateCount": ORIENTATION_STATE_COUNT,
            "formula": "20*6+9*6*4",
        },
        "renderPolicy": {
            "transientState": TRANSIENT_POLICY,
            "denseCenterParts": ["ae2:cable_anchor"],
            "glassCoreOverrides": {
                "defaultDesiredCableType": "GLASS",
                "overrideDesiredCableType": "SMART",
                "partIds": list(SMART_CORE_PART_IDS),
                "effect": "glass-center-promoted-to-covered-core",
            },
            "planeConnectionMasks": {
                "count": PLANE_CONNECTION_MASK_COUNT,
                "bits": PLANE_CONNECTION_MASK_BITS,
                "compatibleNeighbor": "same-concrete-part-class-same-installed-face",
                "coordinateSpaces": PLANE_COORDINATE_SPACE_POLICY,
                "collisionBoundBitsByInstalledFace": (
                    PLANE_COLLISION_BOUND_BITS_BY_FACE
                ),
                "renderedGeometryBoundBits": PLANE_RENDER_BOUND_BITS,
                "facadeCutout": {
                    "baseLocalBounds": [1, 1, 15, 15],
                    "coordinateSpace": "BusCollisionHelper-installed-face-local",
                    "boundBitsByInstalledFace": (
                        PLANE_COLLISION_BOUND_BITS_BY_FACE
                    ),
                    "minXExtendsTo": 0,
                    "minYExtendsTo": 0,
                    "maxXExtendsTo": 16,
                    "maxYExtendsTo": 16,
                },
            },
            "facadeCutoutCollision": {
                "unit": "sixteenths",
                "orientation": "BusCollisionHelper-installed-face-local-basis",
                "selection": (
                    "union-of-every-installed-part-box-intersecting-current-facade-slab"
                ),
                "stripAabbConstruction": FACADE_CUTOUT_STRIP_AABB_POLICY,
                "partPolicies": PART_COLLISION_POLICIES,
            },
            "p2pFrequency": "persisted-frequency-static-glyph",
            "deviceConnections": "AECableType.min-local-and-endpoint",
            "mapColorIllumination": STRUCTURAL_MAP_COLOR_ILLUMINATION_POLICY,
            "facades": {
                "policy": FACADE_POLICY,
                "maximumFacades": MAX_FACADES,
                "maskCount": FACADE_MASK_COUNT,
                "material": "per-instance-valid-static-BlockState",
                "eligibility": {
                    "item": "component-free-non-air-BlockItem",
                    "state": "default-state-at-creation-then-valid-property-cycles",
                    "renderShape": "MODEL",
                    "blockEntity": "disallowed-unless-whitelisted",
                    "collision": "full-block-unless-whitelisted",
                    "whitelistResourceSha256": FACADE_WHITELIST_RESOURCE_SHA256,
                    "whitelistBlockCount": FACADE_WHITELIST_BLOCK_COUNT,
                    "whitelistBlocks": list(FACADE_WHITELIST_BLOCK_IDS),
                    "neutralDefaultStateCount": (
                        FACADE_WHITELIST_NEUTRAL_STATE_COUNT
                    ),
                    "neutralDefaultStates": list(
                        FACADE_WHITELIST_NEUTRAL_STATES
                    ),
                    "stateSchemaCount": FACADE_WHITELIST_STATE_SCHEMA_COUNT,
                    "stateCartesianCount": (
                        FACADE_WHITELIST_STATE_COMBINATION_COUNT
                    ),
                    "solidRenderTrueCartesianCount": (
                        FACADE_WHITELIST_SOLID_RENDER_TRUE_STATE_COUNT
                    ),
                    "sameStateSkipRenderingTrueCartesianCount": (
                        FACADE_WHITELIST_SAME_STATE_SKIP_RENDERING_TRUE_STATE_COUNT
                    ),
                    "stateSchemaPolicy": FACADE_WHITELIST_STATE_POLICY,
                    "stateClassificationPolicy": (
                        FACADE_WHITELIST_STATE_CLASSIFICATION_POLICY
                    ),
                    "stateSchemas": [
                        {
                            "blockId": block_id,
                            "properties": properties,
                            "blockstateSha256": (
                                FACADE_WHITELIST_BLOCKSTATE_SHA256[block_id]
                            ),
                        }
                        for block_id, properties in (
                            FACADE_WHITELIST_STATE_SCHEMAS.items()
                        )
                    ],
                    "solidRenderSource": (
                        "pinned-Minecraft-BlockState.isSolidRender-level-position"
                    ),
                    "sameStateSkipRenderingCount": (
                        FACADE_WHITELIST_SAME_STATE_SKIP_RENDERING_COUNT
                    ),
                    "sameStateSkipRendering": [
                        {
                            "blockId": block_id,
                            "skipRendering": skip_rendering,
                        }
                        for block_id, skip_rendering in (
                            FACADE_WHITELIST_SAME_STATE_SKIP_RENDERING.items()
                        )
                    ],
                    "skipRenderingPolicy": FACADE_SKIP_RENDERING_POLICY,
                    "quartzSkipRenderingPolicy": (
                        FACADE_QUARTZ_SKIP_RENDERING_POLICY
                    ),
                    "ordinarySkipRenderingControlCount": (
                        FACADE_ORDINARY_SKIP_RENDERING_CONTROL_COUNT
                    ),
                    "ordinarySkipRenderingControls": [
                        {
                            "blockId": block_id,
                            "skipRendering": skip_rendering,
                        }
                        for block_id, skip_rendering in (
                            FACADE_ORDINARY_SKIP_RENDERING_CONTROLS.items()
                        )
                    ],
                    "optionalTagCount": FACADE_WHITELIST_OPTIONAL_TAG_COUNT,
                    "optionalTags": list(FACADE_WHITELIST_OPTIONAL_TAGS),
                    "nativeNeutralMaterialCount": (
                        NATIVE_FACADE_NEUTRAL_MATERIAL_COUNT
                    ),
                    "supportSetPolicy": FACADE_SUPPORT_SET_POLICY,
                    "ordinaryMaterialPolicy": FACADE_ORDINARY_MATERIAL_POLICY,
                    "tintPolicy": FACADE_TINT_POLICY,
                    "uvReinterpolationPolicy": FACADE_UV_REINTERPOLATION_POLICY,
                    "cardinalVariantTransformPolicy": (
                        FACADE_CARDINAL_VARIANT_TRANSFORM_POLICY
                    ),
                    "weightedVariantPolicy": FACADE_WEIGHTED_VARIANT_POLICY,
                    "nativeNeutralMaterialScope": NATIVE_FACADE_NEUTRAL_SCOPE,
                    "nativeNeutralMaterials": list(
                        NATIVE_FACADE_NEUTRAL_MATERIALS
                    ),
                    "unprovenEligibility": "atomic-original-resource-fallback",
                },
                "unsupportedTransientModels": "atomic-original-resource-fallback",
                "quartzGlassDependency": {
                    "profileId": QUARTZ_FACADE_DEPENDENCY_PROFILE_ID,
                    "profileSha256": QUARTZ_FACADE_DEPENDENCY_PROFILE_SHA256,
                    "resourceManifestSha256": (
                        QUARTZ_FACADE_DEPENDENCY_RESOURCE_MANIFEST_SHA256
                    ),
                    "textureManifestSha256": (
                        QUARTZ_FACADE_DEPENDENCY_TEXTURE_MANIFEST_SHA256
                    ),
                    "textureCount": QUARTZ_FACADE_DEPENDENCY_TEXTURE_COUNT,
                    "textureSemanticAlgorithm": (
                        "decoded-width-height-argb-scanline-animation-meta-sha256-v1"
                    ),
                    "textureSemanticSha256": (
                        QUARTZ_FACADE_DEPENDENCY_TEXTURE_SEMANTIC_SIGNATURE_SHA256
                    ),
                    "glassState": {
                        "neighborPositionAxes": 6,
                        "neighborAppearance": (
                            "adjacent-real-quartz-or-neighbor-cable-bus-facade-"
                            "appearance-through-RENDERING_FACADE_DIRECTION"
                        ),
                        "sameCableBusOtherFacade": "not-a-GlassState-input",
                        "localPerpendicularFacades": (
                            "FacadeBuilder-mask-and-transparent-inset-only"
                        ),
                        "visibleFace": "suppressed-for-adjacent-connected-glass",
                        "frameMask": "per-visible-face-four-tangent-neighbor-tests",
                    },
                    "facadeQuadEmission": (
                        "zero-for-quartz-and-vibrant-vibrant-source-block-light-is-15"
                    ),
                    "higherPriorityOverridePolicy": (
                        "exact-semantic-match-or-atomic-original-resource-fallback"
                    ),
                },
                "sourceParityGolden": FACADE_SOURCE_PARITY_GOLDEN,
            },
            "fallback": FALLBACK_POLICY,
            "contentsItemsFluidsActivityAndDriveLeds": "excluded",
        },
        "resourceClosure": {
            "directNeutralModelRootCount": DIRECT_NEUTRAL_RESOURCE_COUNT,
            "transitiveJsonCount": TRANSITIVE_JSON_RESOURCE_COUNT,
            "pngCount": PNG_RESOURCE_COUNT,
            "pathCount": REQUIRED_RESOURCE_COUNT,
            "totalBytes": REQUIRED_RESOURCE_BYTES,
            "directNeutralModelRoots": list(direct_model_roots()),
            "sourceOnlyTextures": list(SOURCE_ONLY_TEXTURES),
            "requiredResourcesManifestSha256": EXPECTED_RESOURCE_MANIFEST_SHA256,
            "requiredResourceSizesManifestSha256": (
                EXPECTED_RESOURCE_SIZES_MANIFEST_SHA256
            ),
            "liveSemanticGate": {
                "blueMapVersion": "5.22",
                "modelCount": TRANSITIVE_JSON_RESOURCE_COUNT,
                "modelAlgorithm": (
                    "resolved-parent-applied-elements-faces-ao-shade-light-uv-"
                    "texture-cull-rotation-tint-float-bits-sha256-v1"
                ),
                "modelSha256": LIVE_MODEL_SEMANTIC_SIGNATURE_SHA256,
                "textureCount": PNG_RESOURCE_COUNT,
                "textureAlgorithm": (
                    "decoded-width-height-argb-scanline-animation-meta-sha256-v1"
                ),
                "textureSha256": LIVE_TEXTURE_SEMANTIC_SIGNATURE_SHA256,
                "higherPriorityOverridePolicy": (
                    "exact-semantic-match-or-atomic-original-resource-fallback"
                ),
            },
        },
        "frozenAcceptedBaseline": {
            "artifacts": list(ACCEPTED_ARTIFACTS),
            "gallerySchemas": list(ACCEPTED_GALLERY_SCHEMAS),
            "generatedOutputs": EXPECTED_FROZEN_OUTPUT_SHA256,
        },
        "limitations": [
            "exact-only-ae2-19.2.17",
            "static-off-inactive-unlocked",
            "machine-contents-held-items-fluids-and-activity-excluded",
            "accurate-drive-led-state-excluded",
            "missing-malformed-or-capped-observation-falls-back-atomically",
            "no-upstream-model-texture-source-class-or-precomputed-mesh-is-bundled",
        ],
    }


def profile_bytes(checksums: bytes, sizes: bytes) -> bytes:
    return (json.dumps(profile(checksums, sizes), indent=2) + "\n").encode()
