#!/usr/bin/env python3
# SPDX-License-Identifier: LGPL-3.0-only
"""Deterministic exact-AE2-19.2.17 M3e quantum-bridge route contract."""

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

PROFILE_ID = "ae2-quantum-bridge"
SYNTHETIC_BLOCK_STATE = "bluemap_ae2:quantum_bridge"
BLOCK_ENTITY_ID = "ae2:quantum_ring"
SUPPORTED_BLOCKS = (
    ("ae2:quantum_link", ("center",)),
    ("ae2:quantum_ring", ("corner", "edge")),
)
SOURCE_TEXTURES = (
    "ae2:block/quantum_link",
    "ae2:block/quantum_ring",
    "ae2:block/quantum_ring_light",
    "ae2:block/quantum_ring_light_corner",
    "ae2:part/cable/glass/transparent",
    "ae2:part/cable/covered/transparent",
)
EMITTED_OFF_TEXTURES = (
    "ae2:block/quantum_link",
    "ae2:block/quantum_ring",
    "ae2:part/cable/glass/transparent",
    "ae2:part/cable/covered/transparent",
)

EXPECTED_RESOURCE_MANIFEST_SHA256 = (
    "717eed1ada75fb43c1324792c147cd8c2308d8c73ee82bf52d8de6bad4f74ed9"
)
EXPECTED_SOURCE_TEXTURE_MANIFEST_SHA256 = (
    "47afa14a8397a0adba9f92663cd2ae08776fc2f0abec6361e5e728cfdba110ae"
)
EXPECTED_EMITTED_OFF_TEXTURE_MANIFEST_SHA256 = (
    "2905881b9f5ad2f0ac8fc84c825c2d659779f710a27dae9270ce6d741b5e4cdc"
)
EXPECTED_PROFILE_SHA256 = (
    "21afa152e3f56d8bdde9f602748c0efbca52a2c55d5dd7a836adca267c65480e"
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
    (
        "src/main/resources/bluemap-ae2/profiles/ae2/19.2.17/routes/"
        "crafting/profile.json"
    ): "676f63473b4827bb952a7c1f3fb457a6a03c3bfd0fb4b29a122b9f57468ba0f7",
    (
        "src/main/resources/bluemap-ae2/profiles/ae2/19.2.17/routes/"
        "crafting/required-resources.tsv"
    ): "dc474ba6ce7c4c2d53778827b1c1f9b4994594ea984ed7a2cbd62c40e1bc1183",
}

# Sorted path, uncompressed byte size and SHA-256 from the exact runtime JAR.
EXPECTED_RESOURCES = (
    ("assets/ae2/blockstates/quantum_link.json", 158,
     "156f0aeafca2763f1e3fccadd342c08da7870bcb3aa8f176127a2a3502b3aa7d"),
    ("assets/ae2/blockstates/quantum_ring.json", 158,
     "3db38f2e82cd1a9e1e2e45cb078d09f0d01507750cd895615bb9a7f722f27c50"),
    ("assets/ae2/models/block/qnb/link.json", 620,
     "db210ce0b28a49180a27de93b23396df60707fc267ffd27a11acd2472dc484a1"),
    ("assets/ae2/models/block/qnb/qnb_formed.json", 2,
     "44136fa355b3678a1146ad16f7e8649e94fb4fc21fe77e8310c060f61caaff8a"),
    ("assets/ae2/models/block/qnb/ring.json", 620,
     "9bb86854c10e906fc744f90d01b0ac805d6ad18e730c9231427a7444f2fed826"),
    ("assets/ae2/textures/block/quantum_link.png", 426,
     "39fb6167112daab4cfe5eb07517683132695729fc24dfa8a6d76e7460f187bd1"),
    ("assets/ae2/textures/block/quantum_ring.png", 439,
     "37170ad250a932eca4e38f896368ed211d1c26d5ca7b4ac770156853640aac55"),
    ("assets/ae2/textures/block/quantum_ring_light.png", 364,
     "459709c8be7e4118e0cf2a1772a42b6132224e08f1f42ac4fd43631a8805e42a"),
    ("assets/ae2/textures/block/quantum_ring_light.png.mcmeta", 39,
     "37ca1f757a42143edbc722b8933dc7ccb1b797ee3eaa4278783583d79858937d"),
    ("assets/ae2/textures/block/quantum_ring_light_corner.png", 374,
     "9da5f3921928e7adf2824c0562eac829e61fd03d3a0187007d2f987b8edc812d"),
    ("assets/ae2/textures/block/quantum_ring_light_corner.png.mcmeta", 39,
     "37ca1f757a42143edbc722b8933dc7ccb1b797ee3eaa4278783583d79858937d"),
    ("assets/ae2/textures/part/cable/covered/transparent.png", 354,
     "acfce374bdcaa4c2401cf09ad3f963ccc06ff49871e6766e228782085b7fd383"),
    ("assets/ae2/textures/part/cable/glass/transparent.png", 205,
     "42f01d60366d4a9424a9cabffa558f6fbe70db4c3b9217c3c7475b5db703ad0c"),
)


def texture_resource_path(texture: str) -> str:
    namespace, path = texture.split(":", 1)
    return f"assets/{namespace}/textures/{path}.png"


def expected_resource_paths() -> list[str]:
    paths = [path for path, _, _ in EXPECTED_RESOURCES]
    if len(paths) != 13 or len(set(paths)) != 13 or paths != sorted(paths):
        raise ValueError("M3e resource contract is not 13 unique sorted paths")
    return paths


def source_texture_keys() -> list[str]:
    textures = list(SOURCE_TEXTURES)
    if len(textures) != 6 or len(set(textures)) != 6:
        raise ValueError("M3e source texture contract is not six unique keys")
    return textures


def emitted_off_texture_keys() -> list[str]:
    textures = list(EMITTED_OFF_TEXTURES)
    if len(textures) != 4 or len(set(textures)) != 4:
        raise ValueError("M3e emitted-off texture contract is not four unique keys")
    if not set(textures).issubset(SOURCE_TEXTURES):
        raise ValueError("M3e emitted-off textures are not a source-texture subset")
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


def texture_manifest(required_manifest: bytes, textures: tuple[str, ...]) -> bytes:
    resources = parse_resource_manifest(required_manifest)
    paths = {texture_resource_path(texture) for texture in textures}
    return b"".join(
        f"{path}\t{size}\t{digest}\n".encode("utf-8")
        for path, (size, digest) in resources.items()
        if path in paths
    )


def profile(required_manifest: bytes) -> dict[str, object]:
    resources = parse_resource_manifest(required_manifest)
    expected = {path: (size, digest) for path, size, digest in EXPECTED_RESOURCES}
    if resources != expected:
        raise ValueError("M3e manifest does not match the exact resource rows")
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
        "source": {
            "tag": "neoforge/v19.2.17",
            "commit": SOURCE_COMMIT,
            "artifact": "appliedenergistics2-19.2.17-sources.jar",
            "sizeBytes": EXPECTED_SOURCES_SIZE,
            "sha1": EXPECTED_SOURCES_SHA1,
            "sha256": EXPECTED_SOURCES_SHA256,
            "sha512": EXPECTED_SOURCES_SHA512,
        },
        "minecraft": "1.21.1",
        "neoforge": "21.1.234",
        "coverageMilestone": "M3e",
        "buildAcceptance": "local-build-candidate",
        "runtimeAcceptance": "technical-lifecycle-pending",
        "humanAcceptance": "pending",
        "supportedBlocks": [
            {"id": block_id, "roles": list(roles)}
            for block_id, roles in SUPPORTED_BLOCKS
        ],
        "syntheticBlockState": SYNTHETIC_BLOCK_STATE,
        "persistedState": {
            "blockProperties": {
                "formed": "required-true",
                "waterlogged": "required-boolean",
            },
            "blockEntity": {
                "requiredId": BLOCK_ENTITY_ID,
                "renderFieldsRetained": [],
                "inventory": "ignored",
                "quantumStorage": "ignored",
                "proxy": "ignored",
            },
            "transientClientStream": {
                "constructedRole": "unavailable-not-decoded",
                "corner": "unavailable-not-decoded",
                "powered": "unavailable-not-decoded",
            },
        },
        "topologyPolicy": {
            "shape": "isolated-3x3x1-plane",
            "planes": ["xy", "xz", "yz"],
            "center": {"block": "ae2:quantum_link", "count": 1},
            "ring": {
                "block": "ae2:quantum_ring",
                "count": 8,
                "cornerCount": 4,
                "edgeCount": 4,
            },
            "validation": "all-nine-native-formed-same-plane",
            "invalidOrAmbiguous": "atomic-original-resource-fallback",
            "extensionInteraction": "native-block-ids-only-no-connector-claim",
        },
        "renderPolicy": {
            "formedOnly": True,
            "unformed": "stock-bluemap-model",
            "power": "static-off-unknown",
            "poweredOverlays": "excluded",
            "particles": "excluded",
            "layer": "cutout",
            "ambientOcclusion": True,
            "directionalShade": True,
            "tintIndex": -1,
            "cullFace": "none-general-quads",
            "light": "world-derived",
            "fullBridgeTriangles": 396,
        },
        "sourceTextures": source_texture_keys(),
        "emittedOffTextures": emitted_off_texture_keys(),
        "resourcePartition": {
            "pathCount": 13,
            "routeOnlyPathCount": 11,
            "sharedMainResourceCount": 2,
            "blockstateCount": 2,
            "modelCount": 3,
            "sourceTextureCount": 6,
            "textureMetadataCount": 2,
            "emittedOffTextureCount": 4,
            "totalBytes": 3_798,
            "formedModel": "client-built-in-empty-json-stub",
        },
        "requiredResourcesManifestSha256": EXPECTED_RESOURCE_MANIFEST_SHA256,
        "sourceTextureManifestSha256": EXPECTED_SOURCE_TEXTURE_MANIFEST_SHA256,
        "emittedOffTextureManifestSha256": (
            EXPECTED_EMITTED_OFF_TEXTURE_MANIFEST_SHA256
        ),
    }


def profile_bytes(required_manifest: bytes) -> bytes:
    return (json.dumps(profile(required_manifest), indent=2) + "\n").encode("utf-8")
