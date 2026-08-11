#!/usr/bin/env python3
# SPDX-License-Identifier: LGPL-3.0-only
"""Verify the exact AE2 M3e runtime, sources and quantum-route evidence."""

from __future__ import annotations

import argparse
import hashlib
import json
from pathlib import Path
import struct
import sys
import tomllib
import zipfile

from ae2_quantum_bridge_contract import (
    EMITTED_OFF_TEXTURES,
    EXPECTED_EMITTED_OFF_TEXTURE_MANIFEST_SHA256,
    EXPECTED_FROZEN_OUTPUT_SHA256,
    EXPECTED_PROFILE_SHA256,
    EXPECTED_RESOURCE_MANIFEST_SHA256,
    EXPECTED_SHA1,
    EXPECTED_SHA256,
    EXPECTED_SHA512,
    EXPECTED_SIZE,
    EXPECTED_SOURCE_TEXTURE_MANIFEST_SHA256,
    EXPECTED_SOURCES_SHA1,
    EXPECTED_SOURCES_SHA256,
    EXPECTED_SOURCES_SHA512,
    EXPECTED_SOURCES_SIZE,
    SOURCE_COMMIT,
    SOURCE_TEXTURES,
    expected_resource_paths,
    parse_resource_manifest,
    profile,
    profile_bytes,
    texture_manifest,
)
from verify_pinned_artifact import verify_metadata


EXPECTED_RUNTIME_CLASS_SHA256 = {
    "appeng/block/qnb/QnbFormedBakedModel.class": (
        "8f70836cc8176971da98f8a9083a19e81402bf5ce03b876d69ebe875b084c79b"
    ),
    "appeng/block/qnb/QnbFormedModel.class": (
        "a78d5c92a199ff86e5acfb01ba61ec3e0a4d056563911ad78f76b879cc9fe9d0"
    ),
    "appeng/block/qnb/QnbFormedState.class": (
        "ef7f4feae3c195377dd3aab83d5696a121b92afb5f48760b275ec83a166edaae"
    ),
    "appeng/block/qnb/QuantumBaseBlock.class": (
        "2291eb7de33359b5720d3a7c18c3842f1324f3b12db4064b4e910926fc541027"
    ),
    "appeng/block/qnb/QuantumLinkChamberBlock.class": (
        "8192c87899e2eb5204e6ee541781c3aed7698abf97041cb4b66b48366e7bf408"
    ),
    "appeng/block/qnb/QuantumRingBlock.class": (
        "a0503866192a2ce9c673dd326d07b4266680c7c8de52852360ec9e31ab0d1490"
    ),
    "appeng/blockentity/qnb/QuantumBridgeBlockEntity.class": (
        "dbe33a976b94735580752ad8eab1e252862a59c22e9c42969da84c7bbf94c74b"
    ),
    "appeng/me/cluster/implementations/QuantumCalculator.class": (
        "dcdb05f98dc4bddb8dc9544d0225a889e7184200f61ae44ee6ce254e5c091f6b"
    ),
    "appeng/me/cluster/implementations/QuantumCluster.class": (
        "2ee1973b616c663ffd725a0fba83d1a6ca6b293ace093c8c908eee2f6188096f"
    ),
    "appeng/client/render/cablebus/CubeBuilder.class": (
        "5b7cd4efccaa90f3a539a6a7a30c3d5fbb931858e5363cb539b0d72e446f90db"
    ),
    "appeng/core/definitions/AEBlocks.class": (
        "1b882d6be449388cdaa838b235c0dc646ca205fae5c92d811278bca3c5b13afb"
    ),
    "appeng/init/client/InitBuiltInModels.class": (
        "4dc64e570af985893039fe8519c3295ed5330f995984d48bb6b67d5b67978341"
    ),
}

EXPECTED_SOURCE_SHA256 = {
    "appeng/block/qnb/QnbFormedBakedModel.java": (
        "d7d5574dfa9a40dd393978b02f19f6f463dcf939eb69ee2b9baaa08590e7b17a"
    ),
    "appeng/block/qnb/QnbFormedModel.java": (
        "71226a3ae629de21da093eeaf97b7f745450c7c2c4f08384368d681d7f32ebeb"
    ),
    "appeng/block/qnb/QnbFormedState.java": (
        "cf037cda80c129d3039ced2152751a2fc048b2d4f4b1dcb26de6f5e68093aed8"
    ),
    "appeng/block/qnb/QuantumBaseBlock.java": (
        "c903eb98bb371499c2563ac108cf4d4481bf57c909c84d9c80cb5658b753fadf"
    ),
    "appeng/block/qnb/QuantumLinkChamberBlock.java": (
        "dbbb9224e9591da21212059c3cc2c51d1f80d532a9a0ab69b917ead2f2282af6"
    ),
    "appeng/block/qnb/QuantumRingBlock.java": (
        "0ef921b385c48b500d50737e5bce3fab4d34f5a2dd140c16d4e8bbaafb6a18ae"
    ),
    "appeng/blockentity/qnb/QuantumBridgeBlockEntity.java": (
        "2931d4f1776a4aa5a1c6a434b88370ed823ace85992a6684d85084993abf1951"
    ),
    "appeng/me/cluster/implementations/QuantumCalculator.java": (
        "dc623b16e88a8178715820e50d48a2d86d08b6d31820c78e46e14ba3a7806959"
    ),
    "appeng/me/cluster/implementations/QuantumCluster.java": (
        "b01ea7ed850b4d7ffee2c55fcb93e7fcc575d8c9481c86433a4ea9b4287fd39f"
    ),
    "appeng/server/testplots/QnbTestPlots.java": (
        "b4181396c098fe18497bd18dbdd9a7c0c914dff22b537187a9b810fed5fad83a"
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


def verify_png_header(path: str, raw: bytes, animated: bool) -> None:
    if len(raw) < 33 or raw[:8] != b"\x89PNG\r\n\x1a\n":
        raise ValueError(f"{path} is not a complete PNG")
    length = struct.unpack(">I", raw[8:12])[0]
    if length != 13 or raw[12:16] != b"IHDR":
        raise ValueError(f"{path} has no canonical PNG IHDR")
    width, height, bit_depth, color_type = struct.unpack(">IIBB", raw[16:26])
    expected_height = 512 if animated else 16
    if (width, height, bit_depth, color_type) != (16, expected_height, 8, 6):
        raise ValueError(f"{path} dimensions or RGBA8 encoding changed")


def verify_profile(project: Path) -> dict[str, tuple[int, str]]:
    route_root = project / (
        "src/main/resources/bluemap-ae2/profiles/ae2/19.2.17/routes/"
        "quantum-bridge"
    )
    manifest = (route_root / "required-resources.tsv").read_bytes()
    verify_hash(
        "M3e resource manifest SHA-256",
        hashlib.sha256(manifest).hexdigest(),
        EXPECTED_RESOURCE_MANIFEST_SHA256,
    )
    verify_hash(
        "M3e source texture manifest SHA-256",
        hashlib.sha256(texture_manifest(manifest, SOURCE_TEXTURES)).hexdigest(),
        EXPECTED_SOURCE_TEXTURE_MANIFEST_SHA256,
    )
    verify_hash(
        "M3e emitted-off texture manifest SHA-256",
        hashlib.sha256(texture_manifest(manifest, EMITTED_OFF_TEXTURES)).hexdigest(),
        EXPECTED_EMITTED_OFF_TEXTURE_MANIFEST_SHA256,
    )
    resources = parse_resource_manifest(manifest)
    if list(resources) != expected_resource_paths():
        raise ValueError("M3e manifest does not match the exact path set")

    actual_profile_bytes = (route_root / "profile.json").read_bytes()
    verify_hash(
        "M3e generated profile SHA-256",
        hashlib.sha256(actual_profile_bytes).hexdigest(),
        EXPECTED_PROFILE_SHA256,
    )
    if json.loads(actual_profile_bytes.decode("utf-8")) != profile(manifest):
        raise ValueError("M3e profile does not match the exact contract")
    if actual_profile_bytes != profile_bytes(manifest):
        raise ValueError("M3e profile serialization is not canonical")

    for relative_path, expected_digest in EXPECTED_FROZEN_OUTPUT_SHA256.items():
        verify_hash(
            f"frozen accepted output {relative_path}",
            hashlib.sha256((project / relative_path).read_bytes()).hexdigest(),
            expected_digest,
        )
    return resources


def verify_blockstates_and_models(archive: zipfile.ZipFile) -> None:
    for block, model in (("quantum_link", "link"), ("quantum_ring", "ring")):
        blockstate = json.loads(
            archive.read(f"assets/ae2/blockstates/{block}.json")
        )
        expected = {
            "variants": {
                "formed=false": {"model": f"ae2:block/qnb/{model}"},
                "formed=true": {"model": "ae2:block/qnb/qnb_formed"},
            }
        }
        if blockstate != expected:
            raise ValueError(f"{block} formed routing changed")

        actual_model = json.loads(
            archive.read(f"assets/ae2/models/block/qnb/{model}.json")
        )
        expected_model = {
            "parent": "block/block",
            "textures": {
                "particle": f"ae2:block/{block}",
                "sides": f"ae2:block/{block}",
            },
            "render_type": "cutout",
            "elements": [{
                "from": [2, 2, 2],
                "to": [14, 14, 14],
                "faces": {
                    direction: {"texture": "#sides"}
                    for direction in ("down", "up", "north", "east", "south", "west")
                },
            }],
        }
        if actual_model != expected_model:
            raise ValueError(f"{block} unformed fallback model changed")

    if json.loads(archive.read("assets/ae2/models/block/qnb/qnb_formed.json")) != {}:
        raise ValueError("formed client-built-in model stub changed")


def verify_runtime_archive(
    jar: Path, expected_resources: dict[str, tuple[int, str]]
) -> None:
    with zipfile.ZipFile(jar) as archive:
        names = [entry.filename for entry in archive.infolist()]
        if len(names) != len(set(names)):
            raise ValueError("AE2 runtime artifact contains duplicate ZIP entry names")
        for path, (expected_size, expected_digest) in expected_resources.items():
            if path not in names:
                raise ValueError(f"runtime artifact is missing required resource: {path}")
            if archive.getinfo(path).file_size != expected_size:
                raise ValueError(f"required resource size changed: {path}")
            verify_hash(
                f"{path} SHA-256",
                hashlib.sha256(archive.read(path)).hexdigest(),
                expected_digest,
            )

        verify_metadata(archive.read("META-INF/neoforge.mods.toml"))
        verify_blockstates_and_models(archive)
        for path in expected_resources:
            if not path.endswith(".png"):
                continue
            animated = path in {
                "assets/ae2/textures/block/quantum_ring_light.png",
                "assets/ae2/textures/block/quantum_ring_light_corner.png",
            }
            verify_png_header(path, archive.read(path), animated)
            metadata_path = f"{path}.mcmeta"
            if animated:
                if json.loads(archive.read(metadata_path)) != {
                    "animation": {"frametime": 2}
                }:
                    raise ValueError(f"{metadata_path} animation contract changed")
            elif metadata_path in names:
                raise ValueError(f"static M3e texture unexpectedly has metadata: {path}")

        if set(EXPECTED_RUNTIME_CLASS_SHA256) - set(names):
            raise ValueError("runtime artifact is missing an audited M3e class")
        for path, expected_digest in EXPECTED_RUNTIME_CLASS_SHA256.items():
            verify_hash(
                f"{path} SHA-256",
                hashlib.sha256(archive.read(path)).hexdigest(),
                expected_digest,
            )


def verify_sources_archive(sources_jar: Path) -> None:
    with zipfile.ZipFile(sources_jar) as archive:
        names = [entry.filename for entry in archive.infolist()]
        if len(names) != len(set(names)):
            raise ValueError("AE2 sources artifact contains duplicate ZIP entry names")
        if set(EXPECTED_SOURCE_SHA256) - set(names):
            raise ValueError("sources artifact is missing an audited M3e source")
        for path, expected_digest in EXPECTED_SOURCE_SHA256.items():
            verify_hash(
                f"{path} SHA-256",
                hashlib.sha256(archive.read(path)).hexdigest(),
                expected_digest,
            )

        base_source = archive.read("appeng/block/qnb/QuantumBaseBlock.java")
        formed_state = archive.read("appeng/block/qnb/QnbFormedState.java")
        block_entity = archive.read(
            "appeng/blockentity/qnb/QuantumBridgeBlockEntity.java"
        )
        calculator = archive.read(
            "appeng/me/cluster/implementations/QuantumCalculator.java"
        )
        if b"FORMED" not in base_source or b"WATERLOGGED" not in base_source:
            raise ValueError("saved formed/waterlogged block-state contract changed")
        for marker in (b"adjacentQuantumBridges", b"corner", b"powered"):
            if marker not in formed_state:
                raise ValueError("transient formed render-state contract changed")
        for marker in (b"constructed", b"writeToStream", b"readFromStream"):
            if marker not in block_entity:
                raise ValueError("client-stream-only quantum state contract changed")
        for marker in (b"AEBlocks.QUANTUM_LINK", b"AEBlocks.QUANTUM_RING"):
            if marker not in calculator:
                raise ValueError("native quantum topology class contract changed")


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--jar", required=True, type=Path)
    parser.add_argument("--sources-jar", required=True, type=Path)
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
    project = Path(__file__).resolve().parents[1]
    resources = verify_profile(project)
    verify_runtime_archive(args.jar, resources)
    verify_sources_archive(args.sources_jar)

    print(
        "Verified AE2 19.2.17 M3e quantum bridge: exact runtime and sources "
        f"({SOURCE_COMMIT}), 12 audited runtime classes, 10 audited source "
        "files, 2 native blocks, 6 source textures, 4 emitted-off textures, "
        "13 resources (2 shared with main) and all accepted earlier profile "
        "outputs unchanged; "
        "no extension connector compatibility is claimed."
    )
    return 0


if __name__ == "__main__":
    try:
        sys.exit(main())
    except (
        json.JSONDecodeError,
        OSError,
        tomllib.TOMLDecodeError,
        UnicodeError,
        ValueError,
        zipfile.BadZipFile,
    ) as error:
        print(f"AE2 quantum artifact verification failed: {error}", file=sys.stderr)
        sys.exit(1)
