#!/usr/bin/env python3
# SPDX-License-Identifier: LGPL-3.0-only
"""Verify the exact ExtendedAE 2.2.33 M3b input and resource profile."""

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
    EXPECTED_DRIVE_RESOURCE_MANIFEST_SHA256,
    drive_resource_paths,
    parse_resource_manifest as parse_ae2_resource_manifest,
)
from extendedae_contract import (
    EXPECTED_RESOURCE_MANIFEST_SHA256,
    EXPECTED_SHA1,
    EXPECTED_SHA256,
    EXPECTED_SHA512,
    EXPECTED_SIZE,
    EXPECTED_TEXTURE_MANIFEST_SHA256,
    expected_resource_paths,
    parse_resource_manifest,
    profile,
    texture_manifest,
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
    extendedae_dependencies = dependencies.get("extendedae", [])
    if not isinstance(extendedae_dependencies, list):
        raise ValueError("NeoForge metadata has no ExtendedAE dependency list")
    matches = [
        dependency
        for dependency in extendedae_dependencies
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
    if (
        mod.get("modId") != "extendedae"
        or mod.get("version") != "1.21-2.2.33-neoforge"
    ):
        raise ValueError("NeoForge metadata does not identify ExtendedAE 2.2.33")
    if metadata.get("license") != "LGPL-3.0":
        raise ValueError("ExtendedAE license declaration changed")

    expected_ranges = {
        "minecraft": "[1.21.1,)",
        "neoforge": "[21.1.194,)",
        "ae2": "[19.2.8,)",
    }
    for mod_id, version_range in expected_ranges.items():
        dependency = dependency_by_id(metadata, mod_id)
        if dependency.get("versionRange") != version_range:
            raise ValueError(f"ExtendedAE {mod_id} dependency range changed")


def verify_profile(project: Path) -> dict[str, tuple[int, str]]:
    profile_path = project / (
        "src/main/resources/bluemap-ae2/profiles/extendedae/"
        "1.21-2.2.33-neoforge/profile.json"
    )
    actual_profile = json.loads(profile_path.read_text(encoding="utf-8"))
    manifest_path = profile_path.with_name("required-resources.tsv")
    manifest = manifest_path.read_bytes()
    verify_hash(
        "ExtendedAE resource manifest SHA-256",
        hashlib.sha256(manifest).hexdigest(),
        EXPECTED_RESOURCE_MANIFEST_SHA256,
    )
    resources = parse_resource_manifest(manifest)
    if list(resources) != expected_resource_paths():
        raise ValueError(f"{manifest_path} does not match the exact M3b path set")
    if actual_profile != profile(manifest):
        raise ValueError(f"{profile_path} does not match the exact M3b profile")

    ae2_manifest_path = project / (
        "src/main/resources/bluemap-ae2/profiles/ae2/19.2.17/"
        "required-resources.sha256"
    )
    ae2_resources = parse_ae2_resource_manifest(ae2_manifest_path.read_bytes())
    drive_manifest = b"".join(
        f"{ae2_resources[path]}  {path}\n".encode("utf-8")
        for path in drive_resource_paths()
    )
    verify_hash(
        "dependent AE2 M3a Drive manifest SHA-256",
        hashlib.sha256(drive_manifest).hexdigest(),
        EXPECTED_DRIVE_RESOURCE_MANIFEST_SHA256,
    )
    return resources


def verify_archive(
    jar: Path, expected_resources: dict[str, tuple[int, str]]
) -> None:
    with zipfile.ZipFile(jar) as archive:
        names = [entry.filename for entry in archive.infolist()]
        if len(names) != len(set(names)):
            raise ValueError("artifact contains duplicate ZIP entry names")

        missing = [name for name in expected_resources if name not in names]
        if missing:
            raise ValueError(f"artifact is missing required resources: {missing}")
        for name, (expected_size, expected_digest) in expected_resources.items():
            if archive.getinfo(name).file_size != expected_size:
                raise ValueError(f"{name} size changed")
            verify_hash(
                f"{name} SHA-256",
                hashlib.sha256(archive.read(name)).hexdigest(),
                expected_digest,
            )

        verify_metadata(archive.read("META-INF/neoforge.mods.toml"))
        blockstate = json.loads(
            archive.read("assets/extendedae/blockstates/ex_drive.json")
        )
        variants = blockstate.get("variants")
        if not isinstance(variants, dict) or len(variants) != 24:
            raise ValueError("Ex Drive blockstate is not the exact 24-state contract")
        expected_states = {
            f"facing={facing},spin={spin}"
            for facing in ("down", "east", "north", "south", "up", "west")
            for spin in range(4)
        }
        if set(variants) != expected_states:
            raise ValueError("Ex Drive facing/spin state set changed")
        if any(
            not isinstance(value, dict)
            or value.get("model") != "extendedae:block/ex_drive"
            for value in variants.values()
        ):
            raise ValueError("Ex Drive blockstate model routing changed")

        loader = json.loads(
            archive.read("assets/extendedae/models/block/ex_drive.json")
        )
        if loader != {"loader": "extendedae:ex_drive"}:
            raise ValueError("Ex Drive geometry-loader stub changed")
        item_model = json.loads(
            archive.read("assets/extendedae/models/item/ex_drive.json")
        )
        if item_model != {
            "parent": "extendedae:block/extended_drive/extended_me_drive_base"
        }:
            raise ValueError("Ex Drive item-model parent changed")

        verify_hash(
            "ExtendedAE texture manifest SHA-256",
            hashlib.sha256(texture_manifest(archive)).hexdigest(),
            EXPECTED_TEXTURE_MANIFEST_SHA256,
        )

        class_header = archive.read(
            "com/glodblock/github/extendedae/ExtendedAE.class"
        )[:8]
        if len(class_header) != 8 or class_header[:4] != b"\xca\xfe\xba\xbe":
            raise ValueError("ExtendedAE.class has an invalid class-file header")
        class_major = struct.unpack(">H", class_header[6:8])[0]
        if class_major != 65:
            raise ValueError(
                f"ExtendedAE.class major is {class_major}, expected Java 21 (65)"
            )


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
        "Verified ExtendedAE 2.2.33: exact artifact, metadata, Java 21 class, "
        "24 Ex Drive states, 3 built-in extension cell IDs, 8 textures and "
        "15 disjoint M3b resources; dependent AE2 M3a Drive partition unchanged."
    )
    return 0


if __name__ == "__main__":
    try:
        sys.exit(main())
    except (
        json.JSONDecodeError,
        OSError,
        tomllib.TOMLDecodeError,
        UnicodeDecodeError,
        ValueError,
        zipfile.BadZipFile,
    ) as error:
        print(f"ExtendedAE verification failed: {error}", file=sys.stderr)
        sys.exit(1)
