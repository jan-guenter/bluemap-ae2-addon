#!/usr/bin/env python3
# SPDX-License-Identifier: LGPL-3.0-only
"""Generate or check the exact AE2 19.2.17 M3c quartz-glass route."""

from __future__ import annotations

import argparse
import hashlib
from pathlib import Path
import sys
import zipfile

from ae2_quartz_glass_contract import (
    EXPECTED_CANONICAL_RESOURCE_PARTITION_SHA256,
    EXPECTED_CANONICAL_TEXTURE_PARTITION_SHA256,
    EXPECTED_RESOURCE_MANIFEST_SHA256,
    EXPECTED_SHA256,
    EXPECTED_SIZE,
    EXPECTED_TEXTURE_MANIFEST_SHA256,
    canonical_partition_manifest,
    profile_bytes,
    resource_manifest,
    texture_manifest,
)


def digest(path: Path) -> str:
    value = hashlib.sha256()
    with path.open("rb") as source:
        for chunk in iter(lambda: source.read(64 * 1024), b""):
            value.update(chunk)
    return value.hexdigest()


def verify_partition_hashes(manifest: bytes) -> None:
    checks = (
        ("resource manifest", manifest, EXPECTED_RESOURCE_MANIFEST_SHA256),
        (
            "texture manifest",
            texture_manifest(manifest),
            EXPECTED_TEXTURE_MANIFEST_SHA256,
        ),
        (
            "canonical resource partition",
            canonical_partition_manifest(manifest),
            EXPECTED_CANONICAL_RESOURCE_PARTITION_SHA256,
        ),
        (
            "canonical texture partition",
            canonical_partition_manifest(manifest, textures_only=True),
            EXPECTED_CANONICAL_TEXTURE_PARTITION_SHA256,
        ),
    )
    for label, content, expected in checks:
        if hashlib.sha256(content).hexdigest() != expected:
            raise ValueError(f"M3c {label} changed")


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--jar", required=True, type=Path)
    parser.add_argument("--check", action="store_true")
    args = parser.parse_args()

    if not args.jar.is_file():
        raise ValueError(f"artifact is not a regular file: {args.jar}")
    if args.jar.stat().st_size != EXPECTED_SIZE or digest(args.jar) != EXPECTED_SHA256:
        raise ValueError("input is not the exact AE2 19.2.17 artifact")

    project = Path(__file__).resolve().parents[1]
    root = project / (
        "src/main/resources/bluemap-ae2/profiles/ae2/19.2.17/"
        "routes/quartz-glass"
    )
    with zipfile.ZipFile(args.jar) as archive:
        manifest = resource_manifest(archive)
    verify_partition_hashes(manifest)
    outputs = {
        root / "required-resources.tsv": manifest,
        root / "profile.json": profile_bytes(manifest),
    }

    stale = []
    for path, expected in outputs.items():
        if args.check:
            if not path.is_file() or path.read_bytes() != expected:
                stale.append(path.name)
        else:
            path.parent.mkdir(parents=True, exist_ok=True)
            path.write_bytes(expected)
    if stale:
        raise ValueError(f"generated M3c outputs are stale: {stale}")
    print(
        ("Verified" if args.check else "Generated")
        + " exact AE2 19.2.17 M3c quartz-glass route: 2 blocks, "
        "19 textures and 22 disjoint resources."
    )
    return 0


if __name__ == "__main__":
    try:
        sys.exit(main())
    except (OSError, UnicodeError, ValueError, zipfile.BadZipFile) as error:
        print(f"AE2 quartz-glass profile generation failed: {error}", file=sys.stderr)
        sys.exit(1)
