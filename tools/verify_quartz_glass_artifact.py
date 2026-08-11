#!/usr/bin/env python3
# SPDX-License-Identifier: LGPL-3.0-only
"""Verify the exact AE2 19.2.17 M3c quartz-glass route input."""

from __future__ import annotations

import argparse
import hashlib
import json
from pathlib import Path
import struct
import sys
import tomllib
import zipfile

from ae2_quartz_glass_contract import (
    EXPECTED_CANONICAL_RESOURCE_PARTITION_SHA256,
    EXPECTED_CANONICAL_TEXTURE_PARTITION_SHA256,
    EXPECTED_MAIN_PROFILE_SHA256,
    EXPECTED_MAIN_RESOURCE_MANIFEST_SHA256,
    EXPECTED_RESOURCE_MANIFEST_SHA256,
    EXPECTED_SHA1,
    EXPECTED_SHA256,
    EXPECTED_SHA512,
    EXPECTED_SIZE,
    EXPECTED_TEXTURE_MANIFEST_SHA256,
    canonical_partition_manifest,
    expected_resource_paths,
    parse_resource_manifest,
    profile,
    profile_bytes,
    texture_manifest,
)
from verify_pinned_artifact import verify_metadata


EXPECTED_CLASS_SHA256 = {
    "appeng/decorative/solid/QuartzGlassBlock.class": (
        "bb465d3b32a4ca4ec8f2356bbd99937f2d5d66cfe99e581608cbec5b35012205"
    ),
    "appeng/decorative/solid/QuartzLampBlock.class": (
        "ce00695e6c01578f787c8e409375f80f15ccdcba213f5311b8d2d68f2d56b7e5"
    ),
    "appeng/decorative/solid/GlassState.class": (
        "2ad93080f06f5fb51084c3ccfe8f06f3c1d7e58e4b43df9c52071b8089de5b67"
    ),
    "appeng/client/render/model/GlassModel.class": (
        "ef8258c23aefad903ce5a3b0814022120476a5430907aae1ff7dde00227d390d"
    ),
    "appeng/client/render/model/GlassBakedModel.class": (
        "c348b7f6610f825aa5184679baf52f29a2ac590c257fcc44bcfbe1da0e564c24"
    ),
    "appeng/client/render/model/RenderHelper.class": (
        "a634cc4a75389f426a5e48f87f16ad333381afa05aab30a92b29c76c9d966eee"
    ),
    "appeng/init/client/InitBuiltInModels.class": (
        "4dc64e570af985893039fe8519c3295ed5330f995984d48bb6b67d5b67978341"
    ),
    "appeng/hooks/BuiltInModelHooks.class": (
        "90da9249c096d2fad66f3b926f502cfb01f64303a25c1e664728e7002a50e416"
    ),
    "appeng/mixins/ModelBakeryMixin.class": (
        "0fd8071529619f52b94805e614e417825994db9e1011d0e23e01396fd5bfc5cf"
    ),
    "appeng/core/definitions/AEBlocks.class": (
        "1b882d6be449388cdaa838b235c0dc646ca205fae5c92d811278bca3c5b13afb"
    ),
    "appeng/core/AppEngClient.class": (
        "d8c952a6ab7845c7341b1427ec94c7319a95908c4be035f14ef81a798ec179c8"
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


def verify_route_json(blockstate_raw: bytes, model_raw: bytes) -> None:
    blockstate = json.loads(blockstate_raw)
    if blockstate != {"variants": {"": {"model": "ae2:block/quartz_glass"}}}:
        raise ValueError("quartz-glass blockstate routing changed")
    if json.loads(model_raw) != {}:
        raise ValueError("quartz-glass dynamic-model stub changed")


def verify_png_header(path: str, raw: bytes) -> None:
    if len(raw) < 33 or raw[:8] != b"\x89PNG\r\n\x1a\n":
        raise ValueError(f"{path} is not a complete PNG")
    length = struct.unpack(">I", raw[8:12])[0]
    if length != 13 or raw[12:16] != b"IHDR":
        raise ValueError(f"{path} has no canonical PNG IHDR")
    width, height, bit_depth, color_type = struct.unpack(">IIBB", raw[16:26])
    if (width, height, bit_depth, color_type) != (16, 16, 8, 6):
        raise ValueError(f"{path} is not exact 16x16 RGBA8 texture data")


def verify_profile(project: Path) -> dict[str, tuple[int, str]]:
    root = project / (
        "src/main/resources/bluemap-ae2/profiles/ae2/19.2.17/"
        "routes/quartz-glass"
    )
    manifest_path = root / "required-resources.tsv"
    manifest = manifest_path.read_bytes()
    verify_hash(
        "M3c resource manifest SHA-256",
        hashlib.sha256(manifest).hexdigest(),
        EXPECTED_RESOURCE_MANIFEST_SHA256,
    )
    verify_hash(
        "M3c texture manifest SHA-256",
        hashlib.sha256(texture_manifest(manifest)).hexdigest(),
        EXPECTED_TEXTURE_MANIFEST_SHA256,
    )
    verify_hash(
        "M3c canonical resource partition SHA-256",
        hashlib.sha256(canonical_partition_manifest(manifest)).hexdigest(),
        EXPECTED_CANONICAL_RESOURCE_PARTITION_SHA256,
    )
    verify_hash(
        "M3c canonical texture partition SHA-256",
        hashlib.sha256(
            canonical_partition_manifest(manifest, textures_only=True)
        ).hexdigest(),
        EXPECTED_CANONICAL_TEXTURE_PARTITION_SHA256,
    )
    resources = parse_resource_manifest(manifest)
    if list(resources) != expected_resource_paths():
        raise ValueError("M3c manifest does not match the exact path set")
    profile_path = root / "profile.json"
    actual_profile_bytes = profile_path.read_bytes()
    actual_profile = json.loads(actual_profile_bytes.decode("utf-8"))
    if actual_profile != profile(manifest):
        raise ValueError("M3c profile does not match the exact contract")
    if actual_profile_bytes != profile_bytes(manifest):
        raise ValueError("M3c profile serialization is not canonical")

    main_root = root.parents[1]
    verify_hash(
        "accepted M3a main profile SHA-256",
        hashlib.sha256((main_root / "profile.json").read_bytes()).hexdigest(),
        EXPECTED_MAIN_PROFILE_SHA256,
    )
    verify_hash(
        "accepted M3a main resource manifest SHA-256",
        hashlib.sha256(
            (main_root / "required-resources.sha256").read_bytes()
        ).hexdigest(),
        EXPECTED_MAIN_RESOURCE_MANIFEST_SHA256,
    )
    return resources


def verify_archive(
    jar: Path, expected_resources: dict[str, tuple[int, str]]
) -> None:
    with zipfile.ZipFile(jar) as archive:
        names = [entry.filename for entry in archive.infolist()]
        if len(names) != len(set(names)):
            raise ValueError("artifact contains duplicate ZIP entry names")
        for path, (expected_size, expected_digest) in expected_resources.items():
            if path not in names:
                raise ValueError(f"artifact is missing required resource: {path}")
            if archive.getinfo(path).file_size != expected_size:
                raise ValueError(f"required resource size changed: {path}")
            verify_hash(
                f"{path} SHA-256",
                hashlib.sha256(archive.read(path)).hexdigest(),
                expected_digest,
            )

        verify_metadata(archive.read("META-INF/neoforge.mods.toml"))
        first_blockstate = archive.read(
            "assets/ae2/blockstates/quartz_glass.json"
        )
        second_blockstate = archive.read(
            "assets/ae2/blockstates/quartz_vibrant_glass.json"
        )
        model = archive.read("assets/ae2/models/block/quartz_glass.json")
        verify_route_json(first_blockstate, model)
        verify_route_json(second_blockstate, model)

        for path in expected_resources:
            if path.endswith(".png"):
                verify_png_header(path, archive.read(path))
                if f"{path}.mcmeta" in names:
                    raise ValueError(f"M3c texture unexpectedly has metadata: {path}")

        for path, expected_digest in EXPECTED_CLASS_SHA256.items():
            if path not in names:
                raise ValueError(f"artifact is missing audited class: {path}")
            verify_hash(
                f"{path} SHA-256",
                hashlib.sha256(archive.read(path)).hexdigest(),
                expected_digest,
            )

        class_header = archive.read("appeng/core/AppEng.class")[:8]
        if len(class_header) != 8 or class_header[:4] != b"\xca\xfe\xba\xbe":
            raise ValueError("AppEng.class has an invalid class-file header")
        if struct.unpack(">H", class_header[6:8])[0] != 65:
            raise ValueError("AppEng.class is not a Java 21 class")


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--jar", required=True, type=Path)
    args = parser.parse_args()

    project = Path(__file__).resolve().parents[1]
    if not args.jar.is_file():
        raise ValueError(f"artifact is not a regular file: {args.jar}")
    if args.jar.stat().st_size != EXPECTED_SIZE:
        raise ValueError(
            f"artifact size: got {args.jar.stat().st_size}, expected {EXPECTED_SIZE}"
        )
    verify_hash("artifact SHA-1", digest(args.jar, "sha1"), EXPECTED_SHA1)
    verify_hash("artifact SHA-256", digest(args.jar, "sha256"), EXPECTED_SHA256)
    verify_hash("artifact SHA-512", digest(args.jar, "sha512"), EXPECTED_SHA512)
    resources = verify_profile(project)
    verify_archive(args.jar, resources)

    print(
        "Verified AE2 19.2.17 M3c quartz glass: exact artifact, metadata, "
        "11 audited classes, 2 property-free blocks, 19 textures and "
        "22 disjoint resources; accepted 196-resource M3a profile unchanged."
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
        print(f"AE2 quartz-glass verification failed: {error}", file=sys.stderr)
        sys.exit(1)
