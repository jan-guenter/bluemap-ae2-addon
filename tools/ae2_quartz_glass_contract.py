#!/usr/bin/env python3
# SPDX-License-Identifier: LGPL-3.0-only
"""Deterministic exact-AE2-19.2.17 M3c quartz-glass route contract."""

from __future__ import annotations

import hashlib
import json
from pathlib import Path
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

PROFILE_ID = "ae2-quartz-glass"
BLOCKS = ("ae2:quartz_glass", "ae2:quartz_vibrant_glass")
SYNTHETIC_BLOCK_STATE = "bluemap_ae2:quartz_glass"

EXPECTED_RESOURCE_MANIFEST_SHA256 = (
    "b51c708e7c4d26093c1b6f85b88d0be50572d3cfa76dbf802720f6ad79c7a7fa"
)
EXPECTED_TEXTURE_MANIFEST_SHA256 = (
    "65005c9b76800cdeba5c4598472a44dea131c9974672f89bf421452755fefb6a"
)
EXPECTED_CANONICAL_RESOURCE_PARTITION_SHA256 = (
    "3704e90b1c8ec9ee5a7d7215995869500b50c9b61a797584f6732713dab7103d"
)
EXPECTED_CANONICAL_TEXTURE_PARTITION_SHA256 = (
    "f9373f23e0924f6f2e7315ffb42f4d12b01d6d81b4996651917b865166d04e15"
)
EXPECTED_MAIN_PROFILE_SHA256 = (
    "2c27976a718834dbc97b3eb7cac6543c4ad2a966737c7bccbadb2b1c39c837e8"
)
EXPECTED_MAIN_RESOURCE_MANIFEST_SHA256 = (
    "408297def444f1392b7b87fdc4b8520099513b4c57c63a4176b808ce61b4e1be"
)

TEXTURES = (
    "ae2:block/glass/quartz_glass_a",
    "ae2:block/glass/quartz_glass_b",
    "ae2:block/glass/quartz_glass_c",
    "ae2:block/glass/quartz_glass_d",
    "ae2:block/glass/quartz_glass_frame0001",
    "ae2:block/glass/quartz_glass_frame0010",
    "ae2:block/glass/quartz_glass_frame0011",
    "ae2:block/glass/quartz_glass_frame0100",
    "ae2:block/glass/quartz_glass_frame0101",
    "ae2:block/glass/quartz_glass_frame0110",
    "ae2:block/glass/quartz_glass_frame0111",
    "ae2:block/glass/quartz_glass_frame1000",
    "ae2:block/glass/quartz_glass_frame1001",
    "ae2:block/glass/quartz_glass_frame1010",
    "ae2:block/glass/quartz_glass_frame1011",
    "ae2:block/glass/quartz_glass_frame1100",
    "ae2:block/glass/quartz_glass_frame1101",
    "ae2:block/glass/quartz_glass_frame1110",
    "ae2:block/glass/quartz_glass_frame1111",
)

# Sorted path, uncompressed byte size and SHA-256 from the exact runtime JAR.
EXPECTED_RESOURCES = (
    (
        "assets/ae2/blockstates/quartz_glass.json",
        79,
        "9c331aa0f423a364e136b731195caf168df6496a90a065f9699e5e8e37e70d50",
    ),
    (
        "assets/ae2/blockstates/quartz_vibrant_glass.json",
        80,
        "e3b2b20544e578ff4b9d908ca1e7d281ecc46ddd8f0ee496ad53e2e344e17a99",
    ),
    (
        "assets/ae2/models/block/quartz_glass.json",
        2,
        "44136fa355b3678a1146ad16f7e8649e94fb4fc21fe77e8310c060f61caaff8a",
    ),
    (
        "assets/ae2/textures/block/glass/quartz_glass_a.png",
        237,
        "03eefc0034161f2c3015f56e65e0e0f8fbbb32ddfbbd2fb33900c41b37396f65",
    ),
    (
        "assets/ae2/textures/block/glass/quartz_glass_b.png",
        224,
        "9069ec15d85e6662ca17c6013b89690f46790ca83ec6044a61322c25a45d478e",
    ),
    (
        "assets/ae2/textures/block/glass/quartz_glass_c.png",
        149,
        "b223a687e4de6b450f67923dd9a88def3f9ff955e52200487803f88b984f68a4",
    ),
    (
        "assets/ae2/textures/block/glass/quartz_glass_d.png",
        141,
        "645076bc4d7ff53c212ab6f8c4d567ba3522587a960eea97c5212836b30f4b51",
    ),
    (
        "assets/ae2/textures/block/glass/quartz_glass_frame0001.png",
        136,
        "b27c9090a71bcc815efd166f377a4c2553e7ff34aee6a94cfb210b6d8b02fc0c",
    ),
    (
        "assets/ae2/textures/block/glass/quartz_glass_frame0010.png",
        134,
        "daf355d1a3ec37c371d6e9d5c21710f6c9e2884e349905d59fc3e9d8b0781717",
    ),
    (
        "assets/ae2/textures/block/glass/quartz_glass_frame0011.png",
        244,
        "bd3653de025d6b706c4751804ea10feafb48d5552a782b0fa80fa6b951317f1c",
    ),
    (
        "assets/ae2/textures/block/glass/quartz_glass_frame0100.png",
        132,
        "c4319692608f1002bf828fa006e17974ccf1e50e427b27d4cb040bb3d60a5366",
    ),
    (
        "assets/ae2/textures/block/glass/quartz_glass_frame0101.png",
        229,
        "00a77db35d0655a515c52a7c11cb18747605a597ecf765813a3efae7a48ccc59",
    ),
    (
        "assets/ae2/textures/block/glass/quartz_glass_frame0110.png",
        248,
        "18be5354753232b92b60e0600888f1b66f6391882332c459f9ac9e0e09d4de7a",
    ),
    (
        "assets/ae2/textures/block/glass/quartz_glass_frame0111.png",
        249,
        "f19cce4dbbc34e6d7c2533608d94238aa0c3c9d54de4d9f6db898e8c5ba562e5",
    ),
    (
        "assets/ae2/textures/block/glass/quartz_glass_frame1000.png",
        131,
        "fd93b5b0011c73fc17d344cfb24dce9227186e1e720a5e98eb526a35df90c05c",
    ),
    (
        "assets/ae2/textures/block/glass/quartz_glass_frame1001.png",
        240,
        "119c60f019b071143bf04237983a087ef1a9c90c5c52a2734c1b2fbca8b692ea",
    ),
    (
        "assets/ae2/textures/block/glass/quartz_glass_frame1010.png",
        250,
        "6edd8816778c6c4b6ae696e00d515aee2bf423177ab98f7c0201d93b86878ae3",
    ),
    (
        "assets/ae2/textures/block/glass/quartz_glass_frame1011.png",
        245,
        "4132e2e7b26aadfb4c74428e1b17d9bc3c644f8a6c7e0a8535d6a2e512530bbf",
    ),
    (
        "assets/ae2/textures/block/glass/quartz_glass_frame1100.png",
        247,
        "19ee505da75859f13173fc7bd7c34732a1c93a81e02e5388d579de66ab29805e",
    ),
    (
        "assets/ae2/textures/block/glass/quartz_glass_frame1101.png",
        227,
        "467bfb389e3466844cd27a022cac8cb6548ce84a717b26959bcda12df3bc7fd1",
    ),
    (
        "assets/ae2/textures/block/glass/quartz_glass_frame1110.png",
        263,
        "5012fd1baffbb676c6dcbd65291663e12312ba8edb019e3d264b088cb77ca9c5",
    ),
    (
        "assets/ae2/textures/block/glass/quartz_glass_frame1111.png",
        300,
        "710a0df40e537f2fbfc4687164c2844e73378b27b6b2e14e84f66edd6f9babe8",
    ),
)


def texture_resource_path(texture: str) -> str:
    namespace, path = texture.split(":", 1)
    return f"assets/{namespace}/textures/{path}.png"


def expected_resource_paths() -> list[str]:
    paths = [path for path, _, _ in EXPECTED_RESOURCES]
    if len(paths) != 22 or len(set(paths)) != 22 or paths != sorted(paths):
        raise ValueError("M3c resource contract is not 22 unique sorted paths")
    return paths


def texture_keys() -> list[str]:
    textures = list(TEXTURES)
    if len(textures) != 19 or len(set(textures)) != 19:
        raise ValueError("M3c texture contract is not exactly 19 unique keys")
    return textures


def expected_manifest() -> bytes:
    return (
        "".join(
            f"{path}\t{size}\t{digest}\n"
            for path, size, digest in EXPECTED_RESOURCES
        )
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


def canonical_partition_manifest(
    required_manifest: bytes, *, textures_only: bool = False
) -> bytes:
    resources = parse_resource_manifest(required_manifest)
    texture_paths = {texture_resource_path(texture) for texture in texture_keys()}
    return b"".join(
        f"{digest}\t{size}\t{path}\n".encode("utf-8")
        for path, (size, digest) in resources.items()
        if not textures_only or path in texture_paths
    )


def profile(required_manifest: bytes) -> dict[str, object]:
    resources = parse_resource_manifest(required_manifest)
    expected = {path: (size, digest) for path, size, digest in EXPECTED_RESOURCES}
    if resources != expected:
        raise ValueError("M3c manifest does not match the exact resource rows")
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
        "coverageMilestone": "M3c",
        "runtimeAcceptance": "not-yet-runtime-validated",
        "humanAcceptance": "pending",
        "supportedBlocks": [
            {"id": BLOCKS[0], "lightEmission": 0},
            {"id": BLOCKS[1], "lightEmission": 15},
        ],
        "syntheticBlockState": SYNTHETIC_BLOCK_STATE,
        "persistedState": {
            "properties": "forbidden",
            "blockEntity": "none",
        },
        "connectionPolicy": {
            "directNeighborAxes": 6,
            "sameFamilyBlocks": list(BLOCKS),
            "mutualAppearanceRequiredByClient": True,
            "crossModAppearance": "unsupported-treated-disconnected",
            "malformedNativeOrMissingNeighbor": "atomic-original-resource-fallback",
        },
        "randomPolicy": {
            "positionSeed": "minecraft-blockstate-seed-java-overflow",
            "generator": "LegacyRandomSource",
            "faceCallsReseeded": True,
            "drawsPerVisibleFace": 3,
            "textureIndex": "(draw0+draw2)%4",
            "uvCrop": "u=draw0/16,v=draw1/16;halve-both-when-index<2",
        },
        "renderPolicy": {
            "layer": "cutout",
            "ambientOcclusion": False,
            "directionalShade": False,
            "tintIndex": -1,
            "baseTextureCount": 4,
            "frameTextureCount": 15,
        },
        "textures": texture_keys(),
        "resourcePartition": {
            "pathCount": 22,
            "blockstateCount": 2,
            "modelCount": 1,
            "textureCount": 19,
            "totalBytes": 4187,
            "manifestFormat": "path-tab-bytes-tab-sha256-lf",
            "manifestSha256": EXPECTED_RESOURCE_MANIFEST_SHA256,
            "textureManifestSha256": EXPECTED_TEXTURE_MANIFEST_SHA256,
            "canonicalFormat": "sha256-tab-bytes-tab-path-lf",
            "canonicalManifestSha256": (
                EXPECTED_CANONICAL_RESOURCE_PARTITION_SHA256
            ),
            "canonicalTextureManifestSha256": (
                EXPECTED_CANONICAL_TEXTURE_PARTITION_SHA256
            ),
        },
        "requiredResourcesManifestSha256": hashlib.sha256(
            required_manifest
        ).hexdigest(),
    }


def profile_bytes(required_manifest: bytes) -> bytes:
    return (json.dumps(profile(required_manifest), indent=2) + "\n").encode(
        "utf-8"
    )


def write_contract(project: Path, archive: zipfile.ZipFile) -> None:
    manifest = resource_manifest(archive)
    root = project / (
        "src/main/resources/bluemap-ae2/profiles/ae2/19.2.17/"
        "routes/quartz-glass"
    )
    root.mkdir(parents=True, exist_ok=True)
    (root / "required-resources.tsv").write_bytes(manifest)
    (root / "profile.json").write_bytes(profile_bytes(manifest))
