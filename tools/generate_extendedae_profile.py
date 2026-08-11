#!/usr/bin/env python3
# SPDX-License-Identifier: LGPL-3.0-only
"""Generate or check the exact ExtendedAE 2.2.33 M3b profile."""

from __future__ import annotations

import argparse
import hashlib
from pathlib import Path
import sys
import zipfile

from extendedae_contract import (
    EXPECTED_RESOURCE_MANIFEST_SHA256,
    EXPECTED_SHA256,
    EXPECTED_SIZE,
    EXPECTED_TEXTURE_MANIFEST_SHA256,
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


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--jar", required=True, type=Path)
    parser.add_argument("--check", action="store_true")
    args = parser.parse_args()

    if not args.jar.is_file():
        raise ValueError(f"artifact is not a regular file: {args.jar}")
    if args.jar.stat().st_size != EXPECTED_SIZE or digest(args.jar) != EXPECTED_SHA256:
        raise ValueError("input is not the exact ExtendedAE 2.2.33 artifact")

    project = Path(__file__).resolve().parents[1]
    root = project / (
        "src/main/resources/bluemap-ae2/profiles/extendedae/"
        "1.21-2.2.33-neoforge"
    )
    with zipfile.ZipFile(args.jar) as archive:
        manifest = resource_manifest(archive)
        if hashlib.sha256(manifest).hexdigest() != EXPECTED_RESOURCE_MANIFEST_SHA256:
            raise ValueError("M3b ExtendedAE resource closure changed")
        if (
            hashlib.sha256(texture_manifest(archive)).hexdigest()
            != EXPECTED_TEXTURE_MANIFEST_SHA256
        ):
            raise ValueError("M3b ExtendedAE texture closure changed")
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
        raise ValueError(f"generated M3b ExtendedAE outputs are stale: {stale}")
    print(
        ("Verified" if args.check else "Generated")
        + " exact ExtendedAE 2.2.33 M3b profile: 20 slots, 3 built-in "
        "extension IDs, 8 textures and 15 disjoint resources."
    )
    return 0


if __name__ == "__main__":
    try:
        sys.exit(main())
    except (OSError, ValueError, zipfile.BadZipFile) as error:
        print(f"ExtendedAE profile generation failed: {error}", file=sys.stderr)
        sys.exit(1)
