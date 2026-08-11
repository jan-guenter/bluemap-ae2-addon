#!/usr/bin/env python3
# SPDX-License-Identifier: LGPL-3.0-only
"""Verify the exact AE2 19.2.17 M3a input and its required resources."""

from __future__ import annotations

import argparse
import hashlib
import json
from pathlib import Path
import struct
import sys
import tomllib
import zipfile

from ae2_m1_contract import (
    EXPECTED_SHA1,
    EXPECTED_SHA256,
    EXPECTED_SIZE,
    EXPECTED_TEXTURE_MANIFEST_SHA256,
    expected_resource_paths,
    parse_resource_manifest,
    profile,
    texture_manifest_sha256,
)


EXPECTED_SHA512 = (
    "55edfd948366aff620881e0625e48c333a2cb847e73249bc0b588efbc4b867099"
    "92a8ffbca97ea387e270df4186fe7f74ee2f27b739f1c952e932becfb9dea33"
)


def digest(path: Path, algorithm: str) -> str:
    value = hashlib.new(algorithm)
    with path.open("rb") as source:
        for chunk in iter(lambda: source.read(64 * 1024), b""):
            value.update(chunk)
    return value.hexdigest()


def verify_hash(label: str, actual: str, expected: str) -> None:
    if actual != expected:
        raise ValueError(f"{label}: got {actual}, expected {expected}")


def dependency_by_id(metadata: dict[str, object], mod_id: str) -> dict[str, object]:
    dependencies = metadata.get("dependencies", {})
    if not isinstance(dependencies, dict):
        raise ValueError("NeoForge metadata has no dependency table")
    ae2_dependencies = dependencies.get("ae2", [])
    if not isinstance(ae2_dependencies, list):
        raise ValueError("NeoForge metadata has no AE2 dependency list")
    matches = [
        dependency
        for dependency in ae2_dependencies
        if isinstance(dependency, dict) and dependency.get("modId") == mod_id
    ]
    if len(matches) != 1:
        raise ValueError(f"expected one {mod_id} dependency, got {len(matches)}")
    return matches[0]


def verify_metadata(raw: bytes) -> None:
    metadata = tomllib.loads(raw.decode("utf-8"))
    mods = metadata.get("mods")
    if not isinstance(mods, list) or len(mods) != 1:
        raise ValueError("NeoForge metadata must declare exactly one mod")
    mod = mods[0]
    if not isinstance(mod, dict):
        raise ValueError("NeoForge mod declaration is malformed")
    if mod.get("modId") != "ae2" or mod.get("version") != "19.2.17":
        raise ValueError("NeoForge metadata does not identify AE2 19.2.17")

    minecraft = dependency_by_id(metadata, "minecraft")
    neoforge = dependency_by_id(metadata, "neoforge")
    if minecraft.get("versionRange") != "[1.21.1]":
        raise ValueError("AE2 Minecraft dependency is not exactly 1.21.1")
    if neoforge.get("versionRange") != "[21.1.169,)":
        raise ValueError("AE2 NeoForge dependency range changed")


def verify_profile(project: Path) -> dict[str, str]:
    profile_path = project / (
        "src/main/resources/bluemap-ae2/profiles/ae2/19.2.17/profile.json"
    )
    actual_profile = json.loads(profile_path.read_text(encoding="utf-8"))
    manifest_path = profile_path.with_name("required-resources.sha256")
    manifest = manifest_path.read_bytes()
    resources = parse_resource_manifest(manifest)
    if list(resources) != expected_resource_paths():
        raise ValueError(f"{manifest_path} does not match the exact M3a path set")
    expected = profile(manifest)
    if actual_profile != expected:
        raise ValueError(f"{profile_path} does not match the exact M3a profile")
    return resources


def verify_archive(jar: Path, expected_resources: dict[str, str]) -> None:
    with zipfile.ZipFile(jar) as archive:
        names = [entry.filename for entry in archive.infolist()]
        if len(names) != len(set(names)):
            raise ValueError("artifact contains duplicate ZIP entry names")

        missing = [name for name in expected_resources if name not in names]
        if missing:
            raise ValueError(f"artifact is missing required resources: {missing}")

        for name, expected in expected_resources.items():
            verify_hash(
                f"{name} SHA-256",
                hashlib.sha256(archive.read(name)).hexdigest(),
                expected,
            )

        verify_metadata(archive.read("META-INF/neoforge.mods.toml"))

        blockstate = json.loads(
            archive.read("assets/ae2/blockstates/cable_bus.json")
        )
        if blockstate != {"variants": {"": {"model": "ae2:block/cable_bus"}}}:
            raise ValueError("cable-bus blockstate routing changed")

        model = json.loads(archive.read("assets/ae2/models/block/cable_bus.json"))
        if model != {}:
            raise ValueError("stock cable-bus model is no longer the expected empty model")

        if texture_manifest_sha256(archive) != EXPECTED_TEXTURE_MANIFEST_SHA256:
            raise ValueError("M3a texture-manifest aggregate changed")

        class_header = archive.read("appeng/core/AppEng.class")[:8]
        if len(class_header) != 8 or class_header[:4] != b"\xca\xfe\xba\xbe":
            raise ValueError("AppEng.class has an invalid class-file header")
        class_major = struct.unpack(">H", class_header[6:8])[0]
        if class_major != 65:
            raise ValueError(f"AppEng.class major is {class_major}, expected Java 21 (65)")


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
        "Verified AE2 19.2.17: exact artifact, metadata, Java 21 class, "
        "85 cable IDs, ae2:terminal spins 0-3, 23 Drive item IDs, "
        "158 textures and 196 required M3a resources."
    )
    return 0


if __name__ == "__main__":
    try:
        sys.exit(main())
    except (
        json.JSONDecodeError,
        OSError,
        tomllib.TOMLDecodeError,
        ValueError,
        zipfile.BadZipFile,
    ) as error:
        print(f"verification failed: {error}", file=sys.stderr)
        sys.exit(1)
