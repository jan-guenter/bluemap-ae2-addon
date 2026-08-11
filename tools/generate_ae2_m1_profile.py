#!/usr/bin/env python3
# SPDX-License-Identifier: LGPL-3.0-only
"""Generate or check the exact AE2 19.2.17 M3a profile from the pinned JAR."""

from __future__ import annotations

import argparse
import hashlib
from pathlib import Path
import sys
import zipfile

from ae2_m1_contract import (
    EXPECTED_CORE_RESOURCE_MANIFEST_SHA256,
    EXPECTED_CORE_TEXTURE_MANIFEST_SHA256,
    EXPECTED_DRIVE_RESOURCE_MANIFEST_SHA256,
    EXPECTED_SHA256,
    EXPECTED_SIZE,
    EXPECTED_TEXTURE_MANIFEST_SHA256,
    core_resource_manifest,
    core_texture_manifest_sha256,
    drive_resource_manifest,
    profile_bytes,
    resource_manifest,
    texture_manifest_sha256,
)


def digest(path: Path) -> str:
    value = hashlib.sha256()
    with path.open("rb") as source:
        for chunk in iter(lambda: source.read(64 * 1024), b""):
            value.update(chunk)
    return value.hexdigest()


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--jar", required=True, type=Path)
    parser.add_argument("--check", action="store_true")
    args = parser.parse_args()

    if args.jar.stat().st_size != EXPECTED_SIZE or digest(args.jar) != EXPECTED_SHA256:
        raise ValueError("input is not the exact AE2 19.2.17 artifact")

    project = Path(__file__).resolve().parents[1]
    root = project / "src/main/resources/bluemap-ae2/profiles/ae2/19.2.17"
    with zipfile.ZipFile(args.jar) as archive:
        manifest = resource_manifest(archive)
        core_manifest = core_resource_manifest(archive)
        drive_manifest = drive_resource_manifest(archive)
        if (
            hashlib.sha256(core_manifest).hexdigest()
            != EXPECTED_CORE_RESOURCE_MANIFEST_SHA256
        ):
            raise ValueError("M0-M2 resource closure changed")
        if (
            hashlib.sha256(drive_manifest).hexdigest()
            != EXPECTED_DRIVE_RESOURCE_MANIFEST_SHA256
        ):
            raise ValueError("M3a drive resource closure changed")
        if (
            core_texture_manifest_sha256(archive)
            != EXPECTED_CORE_TEXTURE_MANIFEST_SHA256
        ):
            raise ValueError("M0-M2 texture-manifest aggregate changed")
        if texture_manifest_sha256(archive) != EXPECTED_TEXTURE_MANIFEST_SHA256:
            raise ValueError("M3a texture-manifest aggregate changed")
    outputs = {
        root / "required-resources.sha256": manifest,
        root / "profile.json": profile_bytes(manifest),
    }

    stale = []
    for path, expected in outputs.items():
        if args.check:
            if not path.is_file() or path.read_bytes() != expected:
                stale.append(path.name)
        else:
            path.write_bytes(expected)
    if stale:
        raise ValueError(f"generated M3a profile outputs are stale: {stale}")
    print(
        ("Verified" if args.check else "Generated")
        + " exact AE2 19.2.17 M3a profile: 85 center IDs, 1 face-part ID, "
        "23 drive item IDs, 158 textures, 196 resources."
    )
    return 0


if __name__ == "__main__":
    try:
        sys.exit(main())
    except (OSError, ValueError, zipfile.BadZipFile) as error:
        print(f"profile generation failed: {error}", file=sys.stderr)
        sys.exit(1)
