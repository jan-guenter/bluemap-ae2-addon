#!/usr/bin/env python3
# SPDX-License-Identifier: LGPL-3.0-only
"""Generate or check the exact AE2 19.2.17 S1 structural route resources."""

from __future__ import annotations

import argparse
import hashlib
import json
from pathlib import Path
import re
import sys
import zipfile

from ae2_native_structural_contract import (
    CURRENT_HOST_EVIDENCE,
    DIRECT_NEUTRAL_RESOURCE_COUNT,
    EXPECTED_RESOURCE_MANIFEST_SHA256,
    EXPECTED_RESOURCE_SIZES_MANIFEST_SHA256,
    EXPECTED_SHA256,
    EXPECTED_SIZE,
    FULL_PACK_OVERRIDE_EVIDENCE,
    PNG_RESOURCE_COUNT,
    REQUIRED_RESOURCE_BYTES,
    REQUIRED_RESOURCE_COUNT,
    ROUTE_RESOURCE_ROOT,
    TRANSITIVE_JSON_RESOURCE_COUNT,
    checksum_manifest,
    profile_bytes,
    resource_rows,
    size_manifest,
)


SUPPORT_MATRIX_PATH = Path("src/main/resources/bluemap-ae2/support-matrix.json")
PROVENANCE_PATH = Path("provenance/upstreams.json")


def digest(path: Path) -> str:
    value = hashlib.sha256()
    with path.open("rb") as source:
        for chunk in iter(lambda: source.read(64 * 1024), b""):
            value.update(chunk)
    return value.hexdigest()


def generated_outputs(jar: Path) -> dict[str, bytes]:
    if not jar.is_file():
        raise ValueError(f"artifact is not a regular file: {jar}")
    if jar.stat().st_size != EXPECTED_SIZE or digest(jar) != EXPECTED_SHA256:
        raise ValueError("input is not the exact AE2 19.2.17 artifact")
    with zipfile.ZipFile(jar) as archive:
        rows = resource_rows(archive)
    checksums = checksum_manifest(rows)
    sizes = size_manifest(rows)
    if hashlib.sha256(checksums).hexdigest() != EXPECTED_RESOURCE_MANIFEST_SHA256:
        raise ValueError("native structural checksum manifest changed")
    if hashlib.sha256(sizes).hexdigest() != EXPECTED_RESOURCE_SIZES_MANIFEST_SHA256:
        raise ValueError("native structural size manifest changed")
    return {
        "required-resources.sha256": checksums,
        "required-resources.tsv": sizes,
        "profile.json": profile_bytes(checksums, sizes),
    }


def _snake_case(value: str) -> str:
    return re.sub(r"(?<!^)(?=[A-Z])", "_", value).lower()


def _snake_case_view(value: object) -> object:
    if isinstance(value, dict):
        return {
            _snake_case(str(key)): _snake_case_view(item)
            for key, item in value.items()
        }
    if isinstance(value, list):
        return [_snake_case_view(item) for item in value]
    return value


def _object_fragment(key: str, value: object, indent: int) -> bytes:
    prefix = " " * indent
    encoded = json.dumps(value, indent=2).splitlines()
    lines = [f'{prefix}"{key}": {encoded[0]}']
    lines.extend(f"{prefix}{line}" for line in encoded[1:])
    return ("\n".join(lines) + ",\n").encode("utf-8")


def _object_end(raw: bytes, start: int) -> int:
    depth = 0
    in_string = False
    escaped = False
    for index in range(start, len(raw)):
        value = raw[index]
        if in_string:
            if escaped:
                escaped = False
            elif value == ord("\\"):
                escaped = True
            elif value == ord('"'):
                in_string = False
            continue
        if value == ord('"'):
            in_string = True
        elif value == ord("{"):
            depth += 1
        elif value == ord("}"):
            depth -= 1
            if depth == 0:
                return index + 1
    raise ValueError("generated metadata object is unterminated")


def _project_metadata_object(
    raw: bytes,
    *,
    section_marker: bytes,
    key: str,
    before_marker: bytes,
    value: object,
    indent: int = 6,
) -> bytes:
    section = raw.find(section_marker)
    if section < 0:
        raise ValueError(f"generated metadata section is missing: {section_marker!r}")
    before = raw.find(before_marker, section)
    if before < 0:
        raise ValueError(f"generated metadata insertion point is missing: {before_marker!r}")
    fragment = _object_fragment(key, value, indent)
    key_marker = f'{" " * indent}"{key}": '.encode("utf-8")
    existing = raw.find(key_marker, section, before)
    if existing < 0:
        return raw[:before] + fragment + raw[before:]
    object_start = raw.find(b"{", existing, before)
    if object_start < 0:
        raise ValueError(f"generated metadata object is malformed: {key}")
    object_end = _object_end(raw, object_start)
    line_end = raw.find(b"\n", object_end)
    if line_end < 0 or raw[object_end:line_end].strip() != b",":
        raise ValueError(f"generated metadata object terminator changed: {key}")
    return raw[:existing] + fragment + raw[line_end + 1:]


def metadata_outputs(project: Path) -> dict[Path, bytes]:
    support_path = project / SUPPORT_MATRIX_PATH
    provenance_path = project / PROVENANCE_PATH
    support = _project_metadata_object(
        support_path.read_bytes(),
        section_marker=b'      "profileId": "ae2-cable-bus-structural",',
        key="fullPackOverrideEvidence",
        before_marker=b'      "status": "s1-complete",',
        value=FULL_PACK_OVERRIDE_EVIDENCE,
    )
    support = _project_metadata_object(
        support,
        section_marker=b'      "profileId": "ae2-cable-bus-structural",',
        key="currentHostEvidence",
        before_marker=b'      "status": "s1-complete",',
        value=CURRENT_HOST_EVIDENCE,
    )
    provenance = _project_metadata_object(
        provenance_path.read_bytes(),
        section_marker=b'    "s1_native_structural_profile_audit": {',
        key="full_pack_override_evidence",
        before_marker=b'      "runtime_artifact": {',
        value=_snake_case_view(FULL_PACK_OVERRIDE_EVIDENCE),
    )
    provenance = _project_metadata_object(
        provenance,
        section_marker=b'    "s1_native_structural_profile_audit": {',
        key="current_host_evidence",
        before_marker=b'      "runtime_artifact": {',
        value=_snake_case_view(CURRENT_HOST_EVIDENCE),
    )
    json.loads(support)
    json.loads(provenance)
    return {
        support_path: support,
        provenance_path: provenance,
    }


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--jar", required=True, type=Path)
    parser.add_argument("--check", action="store_true")
    args = parser.parse_args()

    project = Path(__file__).resolve().parents[1]
    root = project / "src/main/resources" / ROUTE_RESOURCE_ROOT
    outputs = generated_outputs(args.jar)
    stale = []
    for name, expected in outputs.items():
        path = root / name
        if args.check:
            if not path.is_file() or path.read_bytes() != expected:
                stale.append(name)
        else:
            path.parent.mkdir(parents=True, exist_ok=True)
            path.write_bytes(expected)
    for path, expected in metadata_outputs(project).items():
        if args.check:
            if path.read_bytes() != expected:
                stale.append(str(path.relative_to(project)))
        else:
            path.write_bytes(expected)
    if stale:
        raise ValueError(f"generated native structural outputs are stale: {stale}")
    print(
        ("Verified" if args.check else "Generated")
        + f" exact AE2 19.2.17 S1 structural profile: "
        + f"{DIRECT_NEUTRAL_RESOURCE_COUNT} roots, "
        + f"{TRANSITIVE_JSON_RESOURCE_COUNT} JSON, {PNG_RESOURCE_COUNT} PNG, "
        + f"{REQUIRED_RESOURCE_COUNT} resources and {REQUIRED_RESOURCE_BYTES} bytes; "
        + "current full-pack override and host evidence in support and provenance "
        + "metadata."
    )
    return 0


if __name__ == "__main__":
    try:
        sys.exit(main())
    except (OSError, UnicodeError, ValueError, zipfile.BadZipFile) as error:
        print(f"AE2 native structural generation failed: {error}", file=sys.stderr)
        sys.exit(1)
