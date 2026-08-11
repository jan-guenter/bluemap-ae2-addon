#!/usr/bin/env python3
# SPDX-License-Identifier: LGPL-3.0-only
"""Deterministic exact-AE2-19.2.17 M3a profile/resource contract."""

from __future__ import annotations

import hashlib
import json
from pathlib import Path
import zipfile


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

FAMILIES = (
    ("glass_cable", "glass", "glass"),
    ("covered_cable", "covered", "covered"),
    ("smart_cable", "smart", "covered"),
    ("covered_dense_cable", "dense_covered", "dense_smart"),
    ("smart_dense_cable", "dense_smart", "dense_smart"),
)

CORE_FOLDERS = ("glass", "covered", "dense_smart")
MODEL_FOLDERS = ("glass", "covered", "smart", "dense_covered", "dense_smart")
MODEL_KINDS = ("center", "connection", "straight")
OVERLAY_TEXTURES = (
    "ae2:part/cable/smart/channels_00",
    "ae2:part/cable/smart/channels_10",
    "ae2:part/cable/dense_smart/channels_00",
    "ae2:part/cable/dense_smart/channels_10",
)
TERMINAL_TEXTURES = (
    "ae2:part/monitor_sides",
    "ae2:part/monitor_sides_status",
    "ae2:part/monitor_back",
    "ae2:part/monitor_front",
    "ae2:part/monitor_sides_status_off",
    "ae2:part/terminal_bright",
    "ae2:part/terminal_medium",
    "ae2:part/terminal_dark",
)
TERMINAL_MODEL_RESOURCES = (
    "assets/ae2/models/part/display_base.json",
    "assets/ae2/models/part/display_off.json",
    "assets/ae2/models/part/display_status_off.json",
    "assets/ae2/models/part/terminal_off.json",
)
DRIVE_MODEL_RESOURCES = (
    "assets/ae2/models/block/drive.json",
    "assets/ae2/models/block/drive/drive_base.json",
    "assets/ae2/models/block/drive/drive_cell.json",
    "assets/ae2/models/block/drive/drive_cell_empty.json",
    "assets/ae2/models/block/drive/cells/1k_item_cell.json",
    "assets/ae2/models/block/drive/cells/4k_item_cell.json",
    "assets/ae2/models/block/drive/cells/16k_item_cell.json",
    "assets/ae2/models/block/drive/cells/64k_item_cell.json",
    "assets/ae2/models/block/drive/cells/256k_item_cell.json",
    "assets/ae2/models/block/drive/cells/1k_fluid_cell.json",
    "assets/ae2/models/block/drive/cells/4k_fluid_cell.json",
    "assets/ae2/models/block/drive/cells/16k_fluid_cell.json",
    "assets/ae2/models/block/drive/cells/64k_fluid_cell.json",
    "assets/ae2/models/block/drive/cells/256k_fluid_cell.json",
    "assets/ae2/models/block/drive/cells/creative_cell.json",
)
DRIVE_TEXTURES = (
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
)
DRIVE_BASE_MODEL = "ae2:block/drive/drive_base"
DRIVE_EMPTY_CELL_MODEL = "ae2:block/drive/drive_cell_empty"
DRIVE_GENERIC_CELL_MODEL = "ae2:block/drive/drive_cell"
DRIVE_EXPLICIT_CELL_MODELS = (
    ("ae2:item_storage_cell_1k", "ae2:block/drive/cells/1k_item_cell"),
    ("ae2:item_storage_cell_4k", "ae2:block/drive/cells/4k_item_cell"),
    ("ae2:item_storage_cell_16k", "ae2:block/drive/cells/16k_item_cell"),
    ("ae2:item_storage_cell_64k", "ae2:block/drive/cells/64k_item_cell"),
    ("ae2:item_storage_cell_256k", "ae2:block/drive/cells/256k_item_cell"),
    ("ae2:fluid_storage_cell_1k", "ae2:block/drive/cells/1k_fluid_cell"),
    ("ae2:fluid_storage_cell_4k", "ae2:block/drive/cells/4k_fluid_cell"),
    ("ae2:fluid_storage_cell_16k", "ae2:block/drive/cells/16k_fluid_cell"),
    ("ae2:fluid_storage_cell_64k", "ae2:block/drive/cells/64k_fluid_cell"),
    ("ae2:fluid_storage_cell_256k", "ae2:block/drive/cells/256k_fluid_cell"),
    ("ae2:creative_storage_cell", "ae2:block/drive/cells/creative_cell"),
    ("ae2:portable_item_cell_1k", "ae2:block/drive/cells/1k_item_cell"),
    ("ae2:portable_item_cell_4k", "ae2:block/drive/cells/4k_item_cell"),
    ("ae2:portable_item_cell_16k", "ae2:block/drive/cells/16k_item_cell"),
    ("ae2:portable_item_cell_64k", "ae2:block/drive/cells/64k_item_cell"),
    ("ae2:portable_item_cell_256k", "ae2:block/drive/cells/256k_item_cell"),
    ("ae2:portable_fluid_cell_1k", "ae2:block/drive/cells/1k_fluid_cell"),
    ("ae2:portable_fluid_cell_4k", "ae2:block/drive/cells/4k_fluid_cell"),
    ("ae2:portable_fluid_cell_16k", "ae2:block/drive/cells/16k_fluid_cell"),
    ("ae2:portable_fluid_cell_64k", "ae2:block/drive/cells/64k_fluid_cell"),
    ("ae2:portable_fluid_cell_256k", "ae2:block/drive/cells/256k_fluid_cell"),
)
DRIVE_GENERIC_CELL_IDS = (
    "ae2:matter_cannon",
    "ae2:color_applicator",
)
BASE_RESOURCES = (
    "META-INF/neoforge.mods.toml",
    "assets/ae2/blockstates/cable_bus.json",
    "assets/ae2/models/block/cable_bus.json",
)

EXPECTED_SIZE = 8_230_896
EXPECTED_SHA1 = "49c18d6a4af487957d7e5a6ad5dcbf71090b8e14"
EXPECTED_SHA256 = "460d779a0609b81409907d9956de8f6f70a1b0912257e3e5c3c7e75ac9630e95"
EXPECTED_CORE_TEXTURE_MANIFEST_SHA256 = (
    "e40a9bc4942d8999d825f42bce94947079948d74024f4f1a078cc55252d81d33"
)
EXPECTED_TEXTURE_MANIFEST_SHA256 = (
    "c0e66d75cad06649b021f8a9073629d6619050c4f69e78c522b6fa32fb232242"
)
EXPECTED_CORE_RESOURCE_MANIFEST_SHA256 = (
    "4f783945d92be446c8e5939f9455b24f9d463cb39f6b4e35e76c9b6fb713b3c2"
)
EXPECTED_DRIVE_RESOURCE_MANIFEST_SHA256 = (
    "a8d10416d0fce66d8a91ce9e0dc93a83d2f552da8762a0a90e183dc58f6745cf"
)


def cable_ids() -> list[str]:
    return [
        f"ae2:{registry_prefix}_{id_suffix}"
        for id_suffix, _, _ in FAMILIES
        for registry_prefix, _ in COLORS
    ]


def core_texture_keys() -> list[str]:
    textures = [
        f"ae2:part/cable/core/{folder}/{texture_name}"
        for folder in CORE_FOLDERS
        for _, texture_name in COLORS
    ]
    textures.extend(
        f"ae2:part/cable/{connection_folder}/{texture_name}"
        for _, connection_folder, _ in FAMILIES
        for _, texture_name in COLORS
    )
    textures.extend(OVERLAY_TEXTURES)
    textures.extend(TERMINAL_TEXTURES)
    if len(textures) != 148 or len(set(textures)) != 148:
        raise ValueError("M0-M2 texture contract is not exactly 148 unique keys")
    return textures


def drive_texture_keys() -> list[str]:
    textures = list(DRIVE_TEXTURES)
    if len(textures) != 10 or len(set(textures)) != 10:
        raise ValueError("M3a drive texture contract is not exactly 10 unique keys")
    return textures


def texture_keys() -> list[str]:
    textures = core_texture_keys() + drive_texture_keys()
    if len(textures) != 158 or len(set(textures)) != 158:
        raise ValueError("M3a texture contract is not exactly 158 unique keys")
    return textures


def texture_resource_path(texture: str) -> str:
    namespace, path = texture.split(":", 1)
    return f"assets/{namespace}/textures/{path}.png"


def core_expected_resource_paths() -> list[str]:
    resources = list(BASE_RESOURCES)
    resources.extend(
        f"assets/ae2/models/part/cable/{folder}/{kind}.json"
        for folder in MODEL_FOLDERS
        for kind in MODEL_KINDS
    )
    resources.extend(TERMINAL_MODEL_RESOURCES)
    resources.extend(
        texture_resource_path(texture) for texture in core_texture_keys()
    )
    resources = sorted(resources)
    if len(resources) != 170 or len(set(resources)) != 170:
        raise ValueError("M0-M2 resource contract is not exactly 170 unique paths")
    return resources


def drive_resource_paths() -> list[str]:
    resources = ["assets/ae2/blockstates/drive.json"]
    resources.extend(DRIVE_MODEL_RESOURCES)
    resources.extend(
        texture_resource_path(texture) for texture in drive_texture_keys()
    )
    resources = sorted(resources)
    if len(resources) != 26 or len(set(resources)) != 26:
        raise ValueError("M3a drive resource contract is not exactly 26 unique paths")
    return resources


def expected_resource_paths() -> list[str]:
    resources = sorted(core_expected_resource_paths() + drive_resource_paths())
    if len(resources) != 196 or len(set(resources)) != 196:
        raise ValueError("M3a resource contract is not exactly 196 unique paths")
    return resources


def drive_cell_models() -> dict[str, str]:
    models = dict(DRIVE_EXPLICIT_CELL_MODELS)
    models.update(
        (item_id, DRIVE_GENERIC_CELL_MODEL) for item_id in DRIVE_GENERIC_CELL_IDS
    )
    if len(models) != 23 or len(set(models.values())) != 12:
        raise ValueError("M3a drive catalog is not exactly 23 IDs and 12 models")
    return models


def resource_manifest_for_paths(
    archive: zipfile.ZipFile, paths: list[str]
) -> bytes:
    names = set(archive.namelist())
    missing = [path for path in paths if path not in names]
    if missing:
        raise ValueError(f"artifact is missing required resources: {missing}")
    lines = [
        f"{hashlib.sha256(archive.read(path)).hexdigest()}  {path}"
        for path in paths
    ]
    return ("\n".join(lines) + "\n").encode("utf-8")


def resource_manifest(archive: zipfile.ZipFile) -> bytes:
    return resource_manifest_for_paths(archive, expected_resource_paths())


def core_resource_manifest(archive: zipfile.ZipFile) -> bytes:
    return resource_manifest_for_paths(archive, core_expected_resource_paths())


def drive_resource_manifest(archive: zipfile.ZipFile) -> bytes:
    return resource_manifest_for_paths(archive, drive_resource_paths())


def texture_manifest_sha256(archive: zipfile.ZipFile) -> str:
    rows = [
        (
            texture_resource_path(key),
            hashlib.sha256(archive.read(texture_resource_path(key))).hexdigest(),
        )
        for key in texture_keys()
    ]
    canonical = (
        "\n".join(f"{digest}  {path}" for path, digest in sorted(rows)) + "\n"
    ).encode("utf-8")
    return hashlib.sha256(canonical).hexdigest()


def core_texture_manifest_sha256(archive: zipfile.ZipFile) -> str:
    rows = [
        (
            texture_resource_path(key),
            hashlib.sha256(archive.read(texture_resource_path(key))).hexdigest(),
        )
        for key in core_texture_keys()
    ]
    canonical = (
        "\n".join(f"{digest}  {path}" for path, digest in sorted(rows)) + "\n"
    ).encode("utf-8")
    return hashlib.sha256(canonical).hexdigest()


def profile(required_manifest: bytes) -> dict[str, object]:
    return {
        "schemaVersion": 3,
        "modId": "ae2",
        "version": "19.2.17",
        "artifact": "appliedenergistics2-19.2.17.jar",
        "sizeBytes": EXPECTED_SIZE,
        "sha1": EXPECTED_SHA1,
        "sha256": EXPECTED_SHA256,
        "minecraft": "1.21.1",
        "neoforge": "21.1.234",
        "coverageMilestone": "M3a",
        "transientPolicy": "idle-off-unknown",
        "supportedCenterParts": cable_ids(),
        "supportedFaceParts": [
            {
                "id": "ae2:terminal",
                "spins": [0, 1, 2, 3],
            }
        ],
        "facadePolicy": {
            "blockState": {"Name": "minecraft:stone"},
            "properties": "forbidden",
            "maximumFacades": 1,
            "requiredSameFacePart": "ae2:terminal",
        },
        "supportedDrive": {
            "blockId": "ae2:drive",
            "slotCount": 10,
            "baseModel": DRIVE_BASE_MODEL,
            "emptyCellModel": DRIVE_EMPTY_CELL_MODEL,
            "explicitCellModels": dict(DRIVE_EXPLICIT_CELL_MODELS),
            "genericCellModel": {
                "model": DRIVE_GENERIC_CELL_MODEL,
                "itemIds": list(DRIVE_GENERIC_CELL_IDS),
            },
            "occupiedModelCount": 12,
            "ledPolicy": "static-offline-unknown",
            "unknownCellPolicy": "atomic-whole-block-original-resource-fallback",
        },
        "coreTextures": core_texture_keys(),
        "driveTextures": drive_texture_keys(),
        "textures": texture_keys(),
        "coreTextureManifestSha256": EXPECTED_CORE_TEXTURE_MANIFEST_SHA256,
        "textureManifestSha256": EXPECTED_TEXTURE_MANIFEST_SHA256,
        "resourcePartitions": {
            "coreM0ThroughM2": {
                "pathCount": 170,
                "textureCount": 148,
                "manifestSha256": EXPECTED_CORE_RESOURCE_MANIFEST_SHA256,
            },
            "m3aDrive": {
                "pathCount": 26,
                "textureCount": 10,
                "manifestSha256": EXPECTED_DRIVE_RESOURCE_MANIFEST_SHA256,
            },
        },
        "requiredResourcesSha256": hashlib.sha256(required_manifest).hexdigest(),
    }


def profile_bytes(required_manifest: bytes) -> bytes:
    return (json.dumps(profile(required_manifest), indent=2) + "\n").encode("utf-8")


def parse_resource_manifest(raw: bytes) -> dict[str, str]:
    resources: dict[str, str] = {}
    lines = raw.decode("utf-8").splitlines()
    for line in lines:
        digest, separator, path = line.partition("  ")
        if separator != "  " or len(digest) != 64 or not path:
            raise ValueError("resource manifest contains a malformed row")
        if any(character not in "0123456789abcdef" for character in digest):
            raise ValueError("resource manifest contains a malformed digest")
        if path in resources:
            raise ValueError(f"duplicate resource manifest path: {path}")
        resources[path] = digest
    if list(resources) != sorted(resources):
        raise ValueError("resource manifest is not sorted by path")
    return resources


def write_contract(project: Path, archive: zipfile.ZipFile) -> None:
    manifest = resource_manifest(archive)
    profile_content = profile_bytes(manifest)
    profile_root = project / "src/main/resources/bluemap-ae2/profiles/ae2/19.2.17"
    (profile_root / "required-resources.sha256").write_bytes(manifest)
    (profile_root / "profile.json").write_bytes(profile_content)
