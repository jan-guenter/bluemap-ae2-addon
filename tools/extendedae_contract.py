#!/usr/bin/env python3
# SPDX-License-Identifier: LGPL-3.0-only
"""Deterministic exact-ExtendedAE-2.2.33 M3b profile/resource contract."""

from __future__ import annotations

import hashlib
import json
from pathlib import Path
import zipfile


VERSION = "1.21-2.2.33-neoforge"
EXPECTED_SIZE = 5_573_972
EXPECTED_SHA1 = "e87867bffee36a28f9f4493f7bb7e7a5109a480f"
EXPECTED_SHA256 = (
    "6652ed1ea4b71f585d48c05a195a77594a7a2bd1ecea0fc805db2122aafad734"
)
EXPECTED_SHA512 = (
    "a61c6f606b5d0a27857b55b8fc6a670352d91f19d2a2dadd2d650f08ae6682f4"
    "37e7b18a80c5e26122bbf7b70b007851f1aaa90442fce16d8729c71c1ec10225"
)
EXPECTED_RESOURCE_MANIFEST_SHA256 = (
    "5e72f79f45a3b120a89cf8b7a1fa15ce41bebaae62a63c6f3305ef40bd5d24ee"
)
EXPECTED_TEXTURE_MANIFEST_SHA256 = (
    "b3de9aede1d2fb8854925a11397009d7458084071d5d5acc293913e27d29b75e"
)
EXPECTED_DEPENDENT_AE2_RESOURCE_MANIFEST_SHA256 = (
    "a8d10416d0fce66d8a91ce9e0dc93a83d2f552da8762a0a90e183dc58f6745cf"
)

BLOCK = "extendedae:ex_drive"
SYNTHETIC_BLOCK_STATE = "bluemap_ae2:extendedae_ex_drive"
BASE_MODEL = "extendedae:block/extended_drive/extended_me_drive_base"
EMPTY_CELL_MODEL = "ae2:block/drive/drive_cell_empty"
GENERIC_CELL_MODEL = "ae2:block/drive/drive_cell"
SLOT_COUNT = 20

BUILT_IN_CELL_MODELS = (
    (
        "extendedae:infinity_water_cell",
        "extendedae:block/drive/infinity_water_cell",
    ),
    (
        "extendedae:infinity_cobblestone_cell",
        "extendedae:block/drive/infinity_cobblestone_cell",
    ),
    ("extendedae:void_cell", "extendedae:block/drive/void_cell"),
)

MODEL_RESOURCES = (
    "assets/extendedae/models/block/drive/infinity_cobblestone_cell.json",
    "assets/extendedae/models/block/drive/infinity_water_cell.json",
    "assets/extendedae/models/block/drive/void_cell.json",
    "assets/extendedae/models/block/ex_drive.json",
    "assets/extendedae/models/block/extended_drive/extended_me_drive_base.json",
    "assets/extendedae/models/item/ex_drive.json",
)
TEXTURES = (
    "extendedae:block/drive/infinity_cell",
    "extendedae:block/drive/void_cell",
    "extendedae:block/extended_drive/drive_inside",
    "extendedae:block/extended_drive/ex_drive_bottom",
    "extendedae:block/extended_drive/ex_drive_side",
    "extendedae:block/extended_drive/ex_drive_top",
    "extendedae:block/generics/front",
    "extendedae:block/generics/side",
)


def texture_resource_path(texture: str) -> str:
    namespace, path = texture.split(":", 1)
    return f"assets/{namespace}/textures/{path}.png"


def expected_resource_paths() -> list[str]:
    resources = ["assets/extendedae/blockstates/ex_drive.json"]
    resources.extend(MODEL_RESOURCES)
    resources.extend(texture_resource_path(texture) for texture in TEXTURES)
    resources = sorted(resources)
    if len(resources) != 15 or len(set(resources)) != 15:
        raise ValueError(
            "M3b ExtendedAE resource contract is not exactly 15 unique paths"
        )
    return resources


def texture_keys() -> list[str]:
    textures = list(TEXTURES)
    if len(textures) != 8 or len(set(textures)) != 8:
        raise ValueError(
            "M3b ExtendedAE texture contract is not exactly 8 unique keys"
        )
    return textures


def resource_manifest_for_paths(
    archive: zipfile.ZipFile, paths: list[str]
) -> bytes:
    names = set(archive.namelist())
    missing = [path for path in paths if path not in names]
    if missing:
        raise ValueError(f"artifact is missing required resources: {missing}")
    lines = [
        f"{path}\t{archive.getinfo(path).file_size}\t"
        f"{hashlib.sha256(archive.read(path)).hexdigest()}"
        for path in paths
    ]
    return ("\n".join(lines) + "\n").encode("utf-8")


def resource_manifest(archive: zipfile.ZipFile) -> bytes:
    return resource_manifest_for_paths(archive, expected_resource_paths())


def texture_manifest(archive: zipfile.ZipFile) -> bytes:
    return resource_manifest_for_paths(
        archive,
        sorted(texture_resource_path(texture) for texture in texture_keys()),
    )


def profile(required_manifest: bytes) -> dict[str, object]:
    return {
        "schemaVersion": 1,
        "profileId": "extendedae",
        "modId": "extendedae",
        "version": VERSION,
        "artifact": "ExtendedAE-1.21-2.2.33-neoforge.jar",
        "sizeBytes": EXPECTED_SIZE,
        "sha1": EXPECTED_SHA1,
        "sha256": EXPECTED_SHA256,
        "sha512": EXPECTED_SHA512,
        "minecraft": "1.21.1",
        "neoforge": "21.1.234",
        "coverageMilestone": "M3b",
        "runtimeAcceptance": "technical-pending",
        "humanAcceptance": "pending",
        "supportedDrive": {
            "blockId": BLOCK,
            "syntheticBlockState": SYNTHETIC_BLOCK_STATE,
            "slotCount": SLOT_COUNT,
            "frontSlotCount": 10,
            "backSlotCount": 10,
            "rowsPerSide": 5,
            "columnsPerSide": 2,
            "baseModel": BASE_MODEL,
            "emptyCellModel": EMPTY_CELL_MODEL,
            "builtInCellModels": dict(BUILT_IN_CELL_MODELS),
            "dependentAe2CellCatalog": {
                "profileId": "ae2",
                "version": "19.2.17",
                "itemCount": 23,
                "occupiedModelCount": 12,
            },
            "supportedItemCount": 26,
            "occupiedModelCount": 15,
            "ledSource": "position-color-no-texture",
            "ledMaterialProxy": "ae2:block/drive/drive_front",
            "ledPolicy": "static-offline-unknown",
            "unknownCellPolicy": (
                "atomic-whole-block-original-resource-fallback"
            ),
        },
        "textures": texture_keys(),
        "textureManifestSha256": EXPECTED_TEXTURE_MANIFEST_SHA256,
        "resourcePartitions": {
            "extendedaeExact": {
                "pathCount": 15,
                "blockstateCount": 1,
                "modelCount": 6,
                "textureCount": 8,
                "totalBytes": 13_242,
                "manifestFormat": "path-tab-bytes-tab-sha256-lf",
                "manifestSha256": EXPECTED_RESOURCE_MANIFEST_SHA256,
            },
            "dependentAe2M3aDrive": {
                "profileId": "ae2",
                "version": "19.2.17",
                "pathCount": 26,
                "textureCount": 10,
                "manifestSha256": (
                    EXPECTED_DEPENDENT_AE2_RESOURCE_MANIFEST_SHA256
                ),
            },
        },
        "requiredResourcesManifestSha256": hashlib.sha256(
            required_manifest
        ).hexdigest(),
    }


def profile_bytes(required_manifest: bytes) -> bytes:
    return (json.dumps(profile(required_manifest), indent=2) + "\n").encode(
        "utf-8"
    )


def parse_resource_manifest(raw: bytes) -> dict[str, tuple[int, str]]:
    resources: dict[str, tuple[int, str]] = {}
    for line in raw.decode("utf-8").splitlines():
        fields = line.split("\t")
        if len(fields) != 3:
            raise ValueError("resource manifest contains a malformed row")
        path, size_text, digest = fields
        if (
            not path
            or not size_text.isascii()
            or not size_text.isdecimal()
            or size_text.startswith("0")
            or len(digest) != 64
            or any(character not in "0123456789abcdef" for character in digest)
        ):
            raise ValueError("resource manifest contains a malformed row")
        if path in resources:
            raise ValueError(f"duplicate resource manifest path: {path}")
        resources[path] = (int(size_text), digest)
    if list(resources) != sorted(resources):
        raise ValueError("resource manifest is not sorted by path")
    return resources


def write_contract(project: Path, archive: zipfile.ZipFile) -> None:
    manifest = resource_manifest(archive)
    profile_content = profile_bytes(manifest)
    profile_root = project / (
        "src/main/resources/bluemap-ae2/profiles/extendedae/"
        "1.21-2.2.33-neoforge"
    )
    profile_root.mkdir(parents=True, exist_ok=True)
    (profile_root / "required-resources.tsv").write_bytes(manifest)
    (profile_root / "profile.json").write_bytes(profile_content)
