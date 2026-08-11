#!/usr/bin/env python3
# SPDX-License-Identifier: LGPL-3.0-only
"""Deterministic exact-AE2-19.2.17 M3d formed-crafting route contract."""

from __future__ import annotations

import hashlib
import json
import zipfile


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

PROFILE_ID = "ae2-crafting"
SYNTHETIC_BLOCK_STATE = "bluemap_ae2:crafting"
SUPPORTED_BLOCKS = (
    ("ae2:crafting_unit", "unit"),
    ("ae2:crafting_accelerator", "accelerator"),
    ("ae2:1k_crafting_storage", "storage_1k"),
    ("ae2:4k_crafting_storage", "storage_4k"),
    ("ae2:16k_crafting_storage", "storage_16k"),
    ("ae2:64k_crafting_storage", "storage_64k"),
    ("ae2:256k_crafting_storage", "storage_256k"),
    ("ae2:crafting_monitor", "monitor"),
)

MEGA_CELLS_ARTIFACT = {
    "modId": "megacells",
    "version": "4.11.0",
    "fileName": "megacells-4.11.0.jar",
    "sizeBytes": 1_137_276,
    "sha1": "f0b1a44bf30c8a9e14e2fa7fce37360191aa55e8",
    "sha256": "a386bbf12afb11729b0dcf77f64221893d250f22e6185a4d728b9799b230bc55",
}
EXPANDED_AE_ARTIFACT = {
    "modId": "expandedae",
    "version": "2.1.1",
    "fileName": "expandedae-2.1.1.jar",
    "sizeBytes": 496_713,
    "sha1": "c4db013f83e569b016da329b3ddc9c14acc75d7d",
    "sha256": "f39c0eb9c6271f54a44ffee092a29520f53000d1005849e6afada3ad9dffba14",
}
ADVANCED_AE_ARTIFACT = {
    "modId": "advanced_ae",
    "version": "1.6.11-1.21.1",
    "fileName": "AdvancedAE-1.6.11-1.21.1.jar",
    "sizeBytes": 4_797_100,
    "sha1": "0af8033f7291b9f5062b229053e16b439a906db9",
    "sha256": "891e1f8ee0f3ac1bbce03fc2848b761f9c52bea4533eb3419ae849582e15ced7",
}
EXTENDED_AE_ARTIFACT = {
    "modId": "extendedae",
    "version": "1.21-2.2.33-neoforge",
    "fileName": "ExtendedAE-1.21-2.2.33-neoforge.jar",
    "sizeBytes": 5_573_972,
    "sha1": "e87867bffee36a28f9f4493f7bb7e7a5109a480f",
    "sha256": "6652ed1ea4b71f585d48c05a195a77594a7a2bd1ecea0fc805db2122aafad734",
}

MEGA_CELLS_COMPATIBLE_CONNECTORS = (
    "megacells:mega_crafting_unit",
    "megacells:mega_crafting_accelerator",
    "megacells:mega_crafting_monitor",
    "megacells:1m_crafting_storage",
    "megacells:4m_crafting_storage",
    "megacells:16m_crafting_storage",
    "megacells:64m_crafting_storage",
    "megacells:256m_crafting_storage",
)
EXPANDED_AE_COMPATIBLE_CONNECTORS = (
    "expandedae:exp_crafting_unit",
    *(
        f"expandedae:exp_crafting_accelerator_{tier}"
        for tier in (
            "2",
            "4",
            "8",
            "16",
            "32",
            "64",
            "128",
            "256",
            "512",
            "1k",
            "2k",
            "4k",
            "8k",
            "16k",
            "32k",
            "64k",
            "128k",
            "256k",
            "512k",
            "1m",
        )
    ),
)
UNSUPPORTED_COMPATIBLE_CONNECTORS = (
    *MEGA_CELLS_COMPATIBLE_CONNECTORS,
    *EXPANDED_AE_COMPATIBLE_CONNECTORS,
)

TEXTURES = (
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

EXPECTED_RESOURCE_MANIFEST_SHA256 = (
    "dc474ba6ce7c4c2d53778827b1c1f9b4994594ea984ed7a2cbd62c40e1bc1183"
)
EXPECTED_TEXTURE_MANIFEST_SHA256 = (
    "a9a2a1ed912f562362d581cbd219b40afd4c884452a0c64cee3d015dfdc81620"
)
EXPECTED_PROFILE_SHA256 = (
    "676f63473b4827bb952a7c1f3fb457a6a03c3bfd0fb4b29a122b9f57468ba0f7"
)
EXPECTED_FROZEN_OUTPUT_SHA256 = {
    "src/main/resources/bluemap-ae2/profiles/ae2/19.2.17/profile.json": (
        "2c27976a718834dbc97b3eb7cac6543c4ad2a966737c7bccbadb2b1c39c837e8"
    ),
    "src/main/resources/bluemap-ae2/profiles/ae2/19.2.17/required-resources.sha256": (
        "408297def444f1392b7b87fdc4b8520099513b4c57c63a4176b808ce61b4e1be"
    ),
    (
        "src/main/resources/bluemap-ae2/profiles/ae2/19.2.17/routes/"
        "quartz-glass/profile.json"
    ): "548e5bc00ef07c6d6b93b346422b596882ec11ca03de006065fa45fecb991200",
    (
        "src/main/resources/bluemap-ae2/profiles/ae2/19.2.17/routes/"
        "quartz-glass/required-resources.tsv"
    ): "b51c708e7c4d26093c1b6f85b88d0be50572d3cfa76dbf802720f6ad79c7a7fa",
    (
        "src/main/resources/bluemap-ae2/profiles/extendedae/"
        "1.21-2.2.33-neoforge/profile.json"
    ): "eab467f46c27974e1f7d54fe749366b92eacd8d63d57bad1e8f3e452d82ad1df",
    (
        "src/main/resources/bluemap-ae2/profiles/extendedae/"
        "1.21-2.2.33-neoforge/required-resources.tsv"
    ): "5e72f79f45a3b120a89cf8b7a1fa15ce41bebaae62a63c6f3305ef40bd5d24ee",
}

# Sorted path, uncompressed byte size and SHA-256 from the exact runtime JAR.
EXPECTED_RESOURCES = (
    ("assets/ae2/blockstates/16k_crafting_storage.json", 182,
     "8e04febb39f74e1bb1061f9fee979be9cc4923bf14cc5a5d619cf6e681d506a4"),
    ("assets/ae2/blockstates/1k_crafting_storage.json", 180,
     "9a1f6383cd3b54a8361cefc46740ddbee587ce79baefccb6ad6de6355833a603"),
    ("assets/ae2/blockstates/256k_crafting_storage.json", 184,
     "3458c6e521a76f7a0761c7efe956cc587826cfdd40d1f7c6284100990fb68905"),
    ("assets/ae2/blockstates/4k_crafting_storage.json", 180,
     "dd4210a4c0fc5b0eb7f524571f20b7e1a92c438bc68df7324cb26c939c726abc"),
    ("assets/ae2/blockstates/64k_crafting_storage.json", 182,
     "d8a1b0f2f21c2f05cd959f03213d0434c6bb41e27d5591d0c3c532aea142eb7f"),
    ("assets/ae2/blockstates/crafting_accelerator.json", 182,
     "f2b8fd7efa88b37968f55d8169eee48d84c1c673b5b2201719037771d5e18918"),
    ("assets/ae2/blockstates/crafting_monitor.json", 1_176,
     "157e2a326b835180b369874b5f6978fab7c6796293945f85a971ac3f5b1cf2b7"),
    ("assets/ae2/blockstates/crafting_unit.json", 168,
     "b33f03d38953281265d6196e2a9f2494974275901b570f390ebf40fa3a338ece"),
    ("assets/ae2/models/block/crafting/16k_storage_formed.json", 2,
     "44136fa355b3678a1146ad16f7e8649e94fb4fc21fe77e8310c060f61caaff8a"),
    ("assets/ae2/models/block/crafting/1k_storage_formed.json", 2,
     "44136fa355b3678a1146ad16f7e8649e94fb4fc21fe77e8310c060f61caaff8a"),
    ("assets/ae2/models/block/crafting/256k_storage_formed.json", 2,
     "44136fa355b3678a1146ad16f7e8649e94fb4fc21fe77e8310c060f61caaff8a"),
    ("assets/ae2/models/block/crafting/4k_storage_formed.json", 2,
     "44136fa355b3678a1146ad16f7e8649e94fb4fc21fe77e8310c060f61caaff8a"),
    ("assets/ae2/models/block/crafting/64k_storage_formed.json", 2,
     "44136fa355b3678a1146ad16f7e8649e94fb4fc21fe77e8310c060f61caaff8a"),
    ("assets/ae2/models/block/crafting/accelerator_formed.json", 2,
     "44136fa355b3678a1146ad16f7e8649e94fb4fc21fe77e8310c060f61caaff8a"),
    ("assets/ae2/models/block/crafting/unit_formed.json", 2,
     "44136fa355b3678a1146ad16f7e8649e94fb4fc21fe77e8310c060f61caaff8a"),
    ("assets/ae2/textures/block/crafting/16k_storage_light.png", 210,
     "eaa2acd1f40f6bede0e1660e13b4db491f3f5daac7790e2e0413c4955e610345"),
    ("assets/ae2/textures/block/crafting/1k_storage_light.png", 203,
     "8749916a5ee88e126ec6e419d1b0742082334134a6d5a074b8648894afb2d7a3"),
    ("assets/ae2/textures/block/crafting/256k_storage_light.png", 211,
     "f5b4bb5399486e19300e05de3c634ee285535dda21e5e9471a3c5dde9b3f2623"),
    ("assets/ae2/textures/block/crafting/4k_storage_light.png", 204,
     "7d1cafbdd8a96932374d3facf85d01ff8a5a35b833a5fd8d247ae92f646d24d4"),
    ("assets/ae2/textures/block/crafting/64k_storage_light.png", 211,
     "5cbf819eac916426a27906f6f54cdca2b80e2713e0f3095e855273c86e3a59a9"),
    ("assets/ae2/textures/block/crafting/accelerator_light.png", 233,
     "21b1bf9f3657a3e74cc087dbf049f05108836d2385899de4b4d14852be9794ce"),
    ("assets/ae2/textures/block/crafting/light_base.png", 310,
     "95a6a354108815b445ab45fd5e225a0083a329d610c527673ad4802cdace0231"),
    ("assets/ae2/textures/block/crafting/monitor_base.png", 326,
     "75ddcd9f9a81449b22b923cd4040ac11a25b2bbe80715b061975d0b843dcca65"),
    ("assets/ae2/textures/block/crafting/monitor_light_bright.png", 125,
     "4f90a8096f2968f966af8731393c46298026cc822ed9dbc2217daa47d4b8d599"),
    ("assets/ae2/textures/block/crafting/monitor_light_dark.png", 169,
     "1d2c333bd8234919c372a533b0692703120a90c566abbdab7ff4bb948abdcf58"),
    ("assets/ae2/textures/block/crafting/monitor_light_medium.png", 170,
     "72c7fc154b3cee7e3c6d0cbb4c7abfee3aada6a3858149b6fb9a91ce087624e5"),
    ("assets/ae2/textures/block/crafting/ring_corner.png", 310,
     "e0498d29bc83d2ff3b8008f1883041bdf1a7e9f11d379bbda7d221ec44985303"),
    ("assets/ae2/textures/block/crafting/ring_side_hor.png", 300,
     "b2cb76d380327c1c6be15535c54b49a1179e7161925afc8d0b76a5a248bd7d33"),
    ("assets/ae2/textures/block/crafting/ring_side_ver.png", 383,
     "2a2f25fea8ef7f87d45e2a4f5fff4c2f4c2d30823bd94a7d390e562c465a2b69"),
    ("assets/ae2/textures/block/crafting/unit_base.png", 364,
     "e427a887f4a3d95227a4154a8e85add316587671ccaa347991348cd71f661234"),
)


def texture_resource_path(texture: str) -> str:
    namespace, path = texture.split(":", 1)
    return f"assets/{namespace}/textures/{path}.png"


def expected_resource_paths() -> list[str]:
    paths = [path for path, _, _ in EXPECTED_RESOURCES]
    if len(paths) != 30 or len(set(paths)) != 30 or paths != sorted(paths):
        raise ValueError("M3d resource contract is not 30 unique sorted paths")
    return paths


def texture_keys() -> list[str]:
    textures = list(TEXTURES)
    if len(textures) != 15 or len(set(textures)) != 15:
        raise ValueError("M3d texture contract is not exactly 15 unique keys")
    return textures


def expected_manifest() -> bytes:
    return "".join(
        f"{path}\t{size}\t{digest}\n"
        for path, size, digest in EXPECTED_RESOURCES
    ).encode("utf-8")


def resource_manifest(archive: zipfile.ZipFile) -> bytes:
    names = [entry.filename for entry in archive.infolist()]
    if len(names) != len(set(names)):
        raise ValueError("artifact contains duplicate ZIP entry names")
    for path, expected_size, expected_digest in EXPECTED_RESOURCES:
        if path not in names:
            raise ValueError(f"artifact is missing required resource: {path}")
        if archive.getinfo(path).file_size != expected_size:
            raise ValueError(f"required resource size changed: {path}")
        actual_digest = hashlib.sha256(archive.read(path)).hexdigest()
        if actual_digest != expected_digest:
            raise ValueError(f"required resource SHA-256 changed: {path}")
    return expected_manifest()


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


def texture_manifest(required_manifest: bytes) -> bytes:
    resources = parse_resource_manifest(required_manifest)
    texture_paths = {texture_resource_path(texture) for texture in texture_keys()}
    return b"".join(
        f"{path}\t{size}\t{digest}\n".encode("utf-8")
        for path, (size, digest) in resources.items()
        if path in texture_paths
    )


def profile(required_manifest: bytes) -> dict[str, object]:
    resources = parse_resource_manifest(required_manifest)
    expected = {path: (size, digest) for path, size, digest in EXPECTED_RESOURCES}
    if resources != expected:
        raise ValueError("M3d manifest does not match the exact resource rows")
    if len(UNSUPPORTED_COMPATIBLE_CONNECTORS) != 29:
        raise ValueError("M3d extension connector boundary is not exactly 29 IDs")
    return {
        "schemaVersion": 1,
        "profileId": PROFILE_ID,
        "modId": "ae2",
        "version": VERSION,
        "artifact": "appliedenergistics2-19.2.17.jar",
        "sizeBytes": EXPECTED_SIZE,
        "sha1": EXPECTED_SHA1,
        "sha256": EXPECTED_SHA256,
        "sha512": EXPECTED_SHA512,
        "minecraft": "1.21.1",
        "neoforge": "21.1.234",
        "coverageMilestone": "M3d",
        "buildAcceptance": "local-build-candidate",
        "runtimeAcceptance": "technical-lifecycle-pending",
        "humanAcceptance": "pending",
        "supportedBlocks": [
            {"id": block_id, "kind": kind} for block_id, kind in SUPPORTED_BLOCKS
        ],
        "syntheticBlockState": SYNTHETIC_BLOCK_STATE,
        "persistedState": {
            "formed": "required-true",
            "powered": "required-boolean",
            "monitorOrientation": ["facing", "spin"],
            "monitorBlockEntity": {
                "retained": ["paintedColor"],
                "paintedColorOrdinals": "0-through-16",
                "missingPaintedColor": "transparent-ordinal-16",
                "displayGenericStack": "client-stream-only-omitted",
            },
        },
        "connectionPolicy": {
            "directNeighborAxes": 6,
            "nativeConnectorBlocks": [block_id for block_id, _ in SUPPORTED_BLOCKS],
            "compatibleButUnsupportedConnectorBlocks": list(
                UNSUPPORTED_COMPATIBLE_CONNECTORS
            ),
            "compatibleButUnsupportedPolicy": "atomic-original-resource-fallback",
            "unknownNonNativePolicy": "disconnected",
            "missingOrMalformedNativePolicy": "atomic-original-resource-fallback",
        },
        "renderPolicy": {
            "formedOnly": True,
            "unformed": "stock-bluemap-model",
            "blockProperties": {
                "formed": {"fullSolid": True, "occluding": True},
                "unformed": {"fullSolid": True, "occluding": True},
            },
            "layer": "cutout",
            "ambientOcclusion": False,
            "directionalShade": True,
            "poweredOverlayLight": 15,
            "unpoweredOverlayLight": "world-derived",
        },
        "textures": texture_keys(),
        "resourcePartition": {
            "pathCount": 30,
            "blockstateCount": 8,
            "formedModelCount": 7,
            "textureCount": 15,
            "totalBytes": 6_177,
            "monitorFormedModel": "client-built-in-absent",
        },
        "requiredResourcesManifestSha256": EXPECTED_RESOURCE_MANIFEST_SHA256,
        "textureManifestSha256": EXPECTED_TEXTURE_MANIFEST_SHA256,
    }


def profile_bytes(required_manifest: bytes) -> bytes:
    return (json.dumps(profile(required_manifest), indent=2) + "\n").encode("utf-8")
