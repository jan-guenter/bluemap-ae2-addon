#!/usr/bin/env python3
# SPDX-License-Identifier: LGPL-3.0-only
"""Tests for the dependency-free BlueMap 5.22 runtime evidence analyzer."""

from __future__ import annotations

import base64
from collections import Counter
import copy
from dataclasses import dataclass, replace
import gzip
import hashlib
import json
import math
from pathlib import Path
import shutil
import struct
import subprocess
import tempfile
import unittest
from unittest import mock

from tools import analyze_prbm


PROJECT_ROOT = Path(__file__).resolve().parents[2]
PROJECT_CASES_PATH = PROJECT_ROOT / "gallery" / "cases.json"
# Most historical analyzer tests deliberately exercise the byte-frozen
# accepted schema-9 projection. AnalyzerTest.setUp replaces this with a
# per-test projected path once the generated gallery advances to schema 10.
CASES_PATH = PROJECT_CASES_PATH
EXPECTED_PATH = Path(__file__).resolve().parent / "fixtures" / "expected-report.json"
GOLDEN_PATH = (
    Path(__file__).resolve().parent
    / "fixtures"
    / "exact-writer-one-triangle.prbm.b64"
)
NATIVE_STRUCTURAL_GOLDENS_PATH = (
    Path(__file__).resolve().parent
    / "fixtures"
    / "native-structural-goldens.json"
)
ANALYZER_PROVENANCE_PATH = PROJECT_ROOT / "tools" / "analyzer-upstreams.json"
NATIVE_STRUCTURAL_RUNTIME_S1_PATH = (
    Path(__file__).resolve().parent
    / "fixtures"
    / "native-structural-runtime-s1.prbm.gz"
)
NATIVE_STRUCTURAL_RUNTIME_LEGACY_PATH = (
    Path(__file__).resolve().parent
    / "fixtures"
    / "native-structural-runtime-legacy.prbm.gz"
)
NATIVE_STRUCTURAL_RUNTIME_MATERIALS_PATH = (
    Path(__file__).resolve().parent
    / "fixtures"
    / "native-structural-runtime-prbm-materials.json"
)
NATIVE_STRUCTURAL_RUNTIME_FIXTURE_IDENTITIES = {
    NATIVE_STRUCTURAL_RUNTIME_S1_PATH: (
        208_783,
        "ad56cf5a2b19adb37d7b856d4659dc8a202ae27f6d58c5776c32242b58f461ce",
    ),
    NATIVE_STRUCTURAL_RUNTIME_LEGACY_PATH: (
        5_539,
        "c7a3f611f483604f9ad7a05c647479e0ca3e71f6459b77c968b2bb532f99bbbd",
    ),
    NATIVE_STRUCTURAL_RUNTIME_MATERIALS_PATH: (
        4_046,
        "0809aea24bb48c930513149cdddb60da2383c6583caab2900f3ebf51d48afa10",
    ),
}
EXACT_M45_RUNTIME_VALIDATOR = analyze_prbm._validate_m45_runtime_oracle


def _schema3_m2_manifest() -> dict[str, object]:
    manifest = _schema7_m3d_manifest()
    manifest["schema_version"] = 3
    manifest["signature_schema_version"] = 3
    manifest["cases"] = [
        case
        for case in manifest["cases"]
        if case.get("milestone") in {"M1", "M2"}
    ]
    for case in manifest["cases"]:
        case.pop("milestone", None)
        case.pop("route", None)
    manifest["case_count"] = 62
    manifest["anchor_count"] = 290
    manifest["expected_custom_summary"] = {
        "anchor_count": 278,
        "selected_resource_count": 149,
        "triangle_count": 8_576,
    }
    manifest["expected_stock_fallback_summary"] = {
        "anchor_count": 11,
        "triangle_count": 0,
    }
    manifest.pop("m3_summary")
    manifest.pop("m3b_summary")
    manifest.pop("m3b_floor_policy")
    manifest.pop("m3c_summary")
    manifest.pop("m3c_floor_policy")
    manifest.pop("m3d_summary")
    manifest.pop("m3d_floor_policy")
    profile = manifest["profile"]
    profile["coverage_milestone"] = "M2"
    profile["selected_resources"] = profile["core_resources"]
    profile["texture_manifest_sha256"] = profile["core_texture_manifest_sha256"]
    for key in (
        "core_resources",
        "core_texture_manifest_sha256",
        "drive_resources",
        "required_resources_sha256",
        "supported_drive",
        "supported_extended_drive",
        "extension_profiles",
        "supported_connected_glass",
        "glass_resources",
        "supported_formed_crafting",
        "crafting_resources",
        "crafting_resource_manifest_sha256",
        "crafting_texture_manifest_sha256",
    ):
        profile.pop(key, None)
    return manifest


def _schema4_m3a_manifest() -> dict[str, object]:
    manifest = _schema7_m3d_manifest()
    manifest["schema_version"] = 4
    manifest["signature_schema_version"] = 4
    manifest["cases"] = [
        case
        for case in manifest["cases"]
        if case.get("milestone") not in {"M3b", "M3c", "M3d"}
    ]
    for case in manifest["cases"]:
        case.pop("milestone", None)
        case.pop("route", None)
    manifest["case_count"] = 76
    manifest["anchor_count"] = 323
    manifest["expected_custom_summary"] = {
        "anchor_count": 310,
        "selected_resource_count": 159,
        "triangle_count": 12_432,
    }
    manifest["expected_stock_fallback_summary"] = {
        "anchor_count": 12,
        "triangle_count": 0,
    }
    manifest.pop("m3b_summary")
    manifest.pop("m3b_floor_policy")
    manifest.pop("m3c_summary")
    manifest.pop("m3c_floor_policy")
    manifest.pop("m3d_summary")
    manifest.pop("m3d_floor_policy")
    profile = manifest["profile"]
    profile["coverage_milestone"] = "M3a"
    profile["selected_resources"] = profile["core_resources"] + profile["drive_resources"]
    profile.pop("supported_extended_drive")
    profile.pop("extension_profiles")
    profile.pop("supported_connected_glass")
    profile.pop("glass_resources")
    profile.pop("supported_formed_crafting")
    profile.pop("crafting_resources")
    profile.pop("crafting_resource_manifest_sha256")
    profile.pop("crafting_texture_manifest_sha256")
    return manifest


def _schema5_m3b_manifest() -> dict[str, object]:
    return analyze_prbm._schema5_view(
        analyze_prbm._schema6_view(
            _schema7_m3d_manifest()
        )
    )


def _schema6_m3c_manifest() -> dict[str, object]:
    return analyze_prbm._schema6_view(
        _schema7_m3d_manifest()
    )


def _schema7_m3d_manifest() -> dict[str, object]:
    manifest = json.loads(CASES_PATH.read_text(encoding="utf-8"))
    if manifest.get("schema_version") == 11:
        manifest = analyze_prbm._schema10_view(manifest)
    if manifest.get("schema_version") == 10:
        manifest = analyze_prbm._schema9_view(manifest)
    return analyze_prbm._schema7_view(
        analyze_prbm._schema8_view(
            manifest
        )
    )


def _pad4(output: bytearray) -> None:
    output.extend(b"\0" * (-len(output) & 3))


def _uint24(value: int) -> bytes:
    return bytes((value & 0xFF, (value >> 8) & 0xFF, (value >> 16) & 0xFF))


def _normal(positions: tuple[tuple[float, float, float], ...]) -> tuple[int, int, int]:
    first, second, third = positions
    ab = tuple(second[index] - first[index] for index in range(3))
    ac = tuple(third[index] - first[index] for index in range(3))
    cross = (
        ab[1] * ac[2] - ab[2] * ac[1],
        ab[2] * ac[0] - ab[0] * ac[2],
        ab[0] * ac[1] - ab[1] * ac[0],
    )
    length = math.sqrt(sum(value * value for value in cross))
    normalized = tuple(value / length for value in cross)
    return tuple(
        max(-128, min(127, math.trunc(value * 128.0 - 0.5)))
        for value in normalized
    )


DEFAULT_UVS = ((0.0, 0.0), (1.0, 0.0), (0.0, 1.0))
FixtureTriangle = tuple[
    int,
    tuple[tuple[float, float, float], ...],
    tuple[int, int, int],
    int,
    int,
    int,
    tuple[tuple[float, float], ...],
]


@dataclass(frozen=True)
class ExactFixtureTriangle:
    material: int
    positions: tuple[tuple[float, float, float], ...]
    normals: tuple[tuple[int, int, int], ...]
    colors: tuple[tuple[int, int, int], ...]
    uvs: tuple[tuple[float, float], ...]
    aos: tuple[int, ...]
    blocklights: tuple[int, ...]
    sunlights: tuple[int, ...]


def _exact_fixture_triangle(
    value: FixtureTriangle | ExactFixtureTriangle,
) -> ExactFixtureTriangle:
    if isinstance(value, ExactFixtureTriangle):
        return value
    material, positions, rgb, blocklight, sunlight, ao, uvs = value
    normal = _normal(positions)
    return ExactFixtureTriangle(
        material,
        positions,
        (normal, normal, normal),
        (rgb, rgb, rgb),
        uvs,
        (ao, ao, ao),
        (blocklight, blocklight, blocklight),
        (sunlight, sunlight, sunlight),
    )


def encode_prbm(
    triangles: list[FixtureTriangle | ExactFixtureTriangle]
) -> bytes:
    """Independent minimal encoder for the exact writer layout used as fixture."""
    exact_triangles = sorted(
        (_exact_fixture_triangle(item) for item in triangles),
        key=lambda item: item.material,
    )
    value_count = len(exact_triangles) * 3
    output = bytearray((1, 0x07))
    output.extend(_uint24(value_count))
    output.extend(_uint24(0))

    positions = [
        value
        for triangle in exact_triangles
        for vertex in triangle.positions
        for value in vertex
    ]
    normals = [
        value for triangle in exact_triangles for normal in triangle.normals for value in normal
    ]
    colors = [
        component for triangle in exact_triangles for rgb in triangle.colors for component in rgb
    ]
    uvs = [
        value
        for triangle in exact_triangles
        for uv in triangle.uvs
        for value in uv
    ]
    ao = [
        value for triangle in exact_triangles for value in triangle.aos
    ]
    blocklight = [
        light for triangle in exact_triangles for light in triangle.blocklights
    ]
    sunlight = [
        light for triangle in exact_triangles for light in triangle.sunlights
    ]
    attributes = (
        ("position", 0x21, struct.pack("<" + "f" * len(positions), *positions)),
        ("normal", 0x63, struct.pack("<" + "b" * len(normals), *normals)),
        ("color", 0x67, bytes(colors)),
        ("uv", 0x11, struct.pack("<" + "f" * len(uvs), *uvs)),
        ("ao", 0x47, bytes(ao)),
        ("blocklight", 0x03, struct.pack("<" + "b" * len(blocklight), *blocklight)),
        ("sunlight", 0x03, struct.pack("<" + "b" * len(sunlight), *sunlight)),
    )
    for name, flags, payload in attributes:
        output.extend(name.encode("ascii"))
        output.append(0)
        output.append(flags)
        _pad4(output)
        output.extend(payload)

    _pad4(output)
    group_start = 0
    while group_start < len(exact_triangles):
        material = exact_triangles[group_start].material
        group_end = group_start + 1
        while (
            group_end < len(exact_triangles)
            and exact_triangles[group_end].material == material
        ):
            group_end += 1
        output.extend(struct.pack("<iii", material, group_start * 3, (group_end - group_start) * 3))
        group_start = group_end
    output.extend(struct.pack("<i", -1))
    return bytes(output)


def _native_structural_runtime_fixture_materials() -> dict[int, str]:
    for path, (expected_size, expected_sha256) in (
        NATIVE_STRUCTURAL_RUNTIME_FIXTURE_IDENTITIES.items()
    ):
        payload = path.read_bytes()
        if len(payload) != expected_size or hashlib.sha256(payload).hexdigest() != (
            expected_sha256
        ):
            raise AssertionError(
                f"accepted S1 runtime fixture identity changed: {path.name}"
            )
    raw = json.loads(
        NATIVE_STRUCTURAL_RUNTIME_MATERIALS_PATH.read_text(encoding="utf-8")
    )
    return {int(index): resource for index, resource in raw.items()}


def _native_structural_runtime_triangles(
    cases: dict[str, object], material_indexes: dict[str, int]
) -> dict[tuple[int, int, int], list[ExactFixtureTriangle]]:
    source_materials = _native_structural_runtime_fixture_materials()

    def decode(
        path: Path,
        selectors: list[tuple[tuple[int, int, int], int]],
    ) -> dict[tuple[int, int, int], list[ExactFixtureTriangle]]:
        document = analyze_prbm.parse_prbm(gzip.decompress(path.read_bytes()))
        if sum(count for _position, count in selectors) != document.triangle_count:
            raise AssertionError(
                f"accepted S1 runtime fixture selector closure changed: {path.name}"
            )
        result: dict[tuple[int, int, int], list[ExactFixtureTriangle]] = {}
        triangle_index = 0
        group_index = 0
        for position, count in selectors:
            rows: list[ExactFixtureTriangle] = []
            for _offset in range(count):
                vertex_index = triangle_index * 3
                while (
                    group_index + 1 < len(document.groups)
                    and vertex_index
                    >= document.groups[group_index].start
                    + document.groups[group_index].count
                ):
                    group_index += 1
                source_index = document.groups[group_index].material_index
                resource = source_materials.get(source_index)
                if resource is None or resource not in material_indexes:
                    raise AssertionError(
                        f"accepted S1 runtime material is unavailable: {source_index}"
                    )
                corners = range(vertex_index, vertex_index + 3)
                rows.append(
                    ExactFixtureTriangle(
                        material_indexes[resource],
                        tuple(
                            tuple(float(value) for value in document.values("position", corner))
                            for corner in corners
                        ),
                        tuple(
                            tuple(int(value) for value in document.values("normal", corner))
                            for corner in corners
                        ),
                        tuple(
                            tuple(int(value) for value in document.values("color", corner))
                            for corner in corners
                        ),
                        tuple(
                            tuple(float(value) for value in document.values("uv", corner))
                            for corner in corners
                        ),
                        tuple(
                            int(document.values("ao", corner)[0]) for corner in corners
                        ),
                        tuple(
                            int(document.values("blocklight", corner)[0])
                            for corner in corners
                        ),
                        tuple(
                            int(document.values("sunlight", corner)[0])
                            for corner in corners
                        ),
                    )
                )
                triangle_index += 1
            result[position] = rows
        return result

    main_selectors = [
        (
            tuple(anchor["position"][axis] for axis in ("x", "y", "z")),
            anchor["expected_triangle_count"],
        )
        for case in cases["cases"]
        if case.get("milestone") == "S1"
        for anchor in case["anchors"]
        if anchor["expected_path"] == "custom-s1"
    ]
    legacy_selectors = [
        (
            tuple(row["position"][axis] for axis in ("x", "y", "z")),
            row["enabled"]["expected_triangle_count"],
        )
        for row in cases["native_structural_legacy_upgrades"]["rows"]
    ]
    if len(main_selectors) != 351 or len(legacy_selectors) != 10:
        raise AssertionError("accepted S1 runtime fixture selector count changed")
    return {
        **decode(NATIVE_STRUCTURAL_RUNTIME_S1_PATH, main_selectors),
        **decode(NATIVE_STRUCTURAL_RUNTIME_LEGACY_PATH, legacy_selectors),
    }


def _native_signature_result(
    *,
    positions: tuple[tuple[float, float, float], ...] = (
        (0.2, 0.5, 0.2),
        (0.8, 0.5, 0.2),
        (0.2, 0.5, 0.8),
    ),
    resource: str = "ae2:part/cable/core/covered/transparent",
    rgb: tuple[int, int, int] = (255, 255, 255),
    ao: int = 255,
    blocklight: int = 0,
    sunlight: int = 15,
    uvs: tuple[tuple[float, float], ...] = DEFAULT_UVS,
    mutate_normal: bool = False,
    blocklights: tuple[int, ...] | None = None,
    sunlights: tuple[int, ...] | None = None,
) -> dict[str, object]:
    """Build an independent one-triangle PRBM signature fixture."""
    payload = bytearray(
        encode_prbm(
            [(0, positions, rgb, blocklight, sunlight, ao, uvs)]
        )
    )
    document = analyze_prbm.parse_prbm(payload)
    if mutate_normal:
        payload[document.attributes["normal"].offset] ^= 1
        document = analyze_prbm.parse_prbm(payload)
    decoded_positions = tuple(
        tuple(float(value) for value in document.values("position", corner))
        for corner in range(3)
    )
    record = analyze_prbm._triangle_record(
        document,
        0,
        decoded_positions,
        (0, 0, 0),
        analyze_prbm.TextureRef(0, resource),
    )
    if blocklights is not None or sunlights is not None:
        record = replace(
            record,
            blocklights=(record.blocklights if blocklights is None else blocklights),
            sunlights=(record.sunlights if sunlights is None else sunlights),
        )
    result = analyze_prbm._records_result(
        [record], "anchor-v10:0,0,0"
    )
    result["nonlighting_attribute_signature"] = (
        analyze_prbm._native_structural_nonlighting_signature(
            [record], "anchor-v10:0,0,0"
        )
    )
    result["records"] = (record,)
    return result


def _m45_signature_result(
    *,
    positions: tuple[tuple[float, float, float], ...] = (
        (0.2, 0.5, 0.2),
        (0.8, 0.5, 0.2),
        (0.2, 0.5, 0.8),
    ),
    resource: str = "ae2:block/generics/side",
    rgb: tuple[int, int, int] = (255, 255, 255),
    ao: int = 255,
    blocklight: int = 0,
    sunlight: int = 15,
    uvs: tuple[tuple[float, float], ...] = DEFAULT_UVS,
    mutate_normal: bool = False,
    blocklights: tuple[int, ...] | None = None,
    sunlights: tuple[int, ...] | None = None,
) -> dict[str, object]:
    payload = bytearray(
        encode_prbm(
            [(0, positions, rgb, blocklight, sunlight, ao, uvs)]
        )
    )
    document = analyze_prbm.parse_prbm(payload)
    if mutate_normal:
        payload[document.attributes["normal"].offset] ^= 1
        document = analyze_prbm.parse_prbm(payload)
    decoded_positions = tuple(
        tuple(float(value) for value in document.values("position", corner))
        for corner in range(3)
    )
    record = analyze_prbm._triangle_record(
        document,
        0,
        decoded_positions,
        (0, 0, 0),
        analyze_prbm.TextureRef(0, resource),
    )
    if blocklights is not None or sunlights is not None:
        record = replace(
            record,
            blocklights=(
                record.blocklights if blocklights is None else blocklights
            ),
            sunlights=(
                record.sunlights if sunlights is None else sunlights
            ),
        )
    scope = "anchor-v11:0,0,0"
    result = analyze_prbm._records_result([record], scope)
    result["nonlighting_attribute_signature"] = (
        analyze_prbm._m45_nonlighting_signature([record], scope)
    )
    result["records"] = (record,)
    return result


def _synthetic_m45_runtime_validator(
    position: tuple[int, int, int],
    records: tuple[analyze_prbm.TriangleRecord, ...]
    | list[analyze_prbm.TriangleRecord],
    result: dict[str, object],
    **_expected: object,
) -> dict[str, object]:
    """Keep small synthetic mode fixtures focused on routing/projections.

    Exact M45 geometry and non-lighting parity is exercised directly below and
    by the retained cold/warm runtime-map replays; synthesizing all 23,616
    accepted triangles would duplicate the runtime evidence in the unit suite.
    """
    light = analyze_prbm._validate_m45_light_contract(
        records, f"synthetic M4/M5 anchor {position}"
    )
    scope = f"anchor-v11:{position[0]},{position[1]},{position[2]}"
    return {
        "triangle_count": len(records),
        "material_triangle_counts": dict(
            sorted(Counter(record.material_identity for record in records).items())
        ),
        "geometry_uv_material_signature": result.get("geometry_signature"),
        "nonlighting_attribute_signature": (
            analyze_prbm._m45_nonlighting_signature(records, scope)
        ),
        "observed_full_attribute_signature": result.get("attribute_signature"),
        "light_validation": light,
        "runtime_oracle_validated": True,
        "synthetic_test_substitution": True,
    }


def _manual_native_record(
    material: str,
    positions: tuple[tuple[float, float, float], ...],
    *,
    uvs: tuple[tuple[float, float], ...] = DEFAULT_UVS,
    normal: tuple[int, int, int] = (0, 127, 0),
    color: tuple[int, int, int] = (255, 255, 255),
    ao: int = 255,
    blocklight: int = 0,
    sunlight: int = 15,
    blocklights: tuple[int, ...] | None = None,
    sunlights: tuple[int, ...] | None = None,
) -> analyze_prbm.TriangleRecord:
    return analyze_prbm.TriangleRecord(
        material_index=0,
        material_identity=material,
        shape="manual-source-shape",
        geometry="manual-source-geometry",
        attributes="manual-source-attributes",
        colors=(color, color, color),
        aos=(ao, ao, ao),
        blocklights=(
            (blocklight, blocklight, blocklight)
            if blocklights is None
            else blocklights
        ),
        sunlights=(
            (sunlight, sunlight, sunlight)
            if sunlights is None
            else sunlights
        ),
        positions=positions,
        uvs=uvs,
        normals=(normal, normal, normal),
    )


def _source_generator_namespace() -> dict[str, object]:
    """Load generator definitions without invoking its runtime oracle."""
    generator_path = PROJECT_ROOT / "gallery" / "generate.py"
    source = generator_path.read_text(encoding="utf-8")
    prefix = source.split("\nCASES = create_cases()\n", 1)[0]
    namespace: dict[str, object] = {
        "__file__": str(generator_path),
        "__name__": "gallery_source_matrix_fixture",
    }
    exec(compile(prefix, str(generator_path), "exec"), namespace)
    return namespace


def _full_generator_namespace() -> dict[str, object]:
    """Load the generator without invoking its command-line writer."""
    generator_path = PROJECT_ROOT / "gallery" / "generate.py"
    namespace: dict[str, object] = {
        "__file__": str(generator_path),
        "__name__": "gallery_full_fixture",
    }
    exec(
        compile(generator_path.read_text(encoding="utf-8"), str(generator_path), "exec"),
        namespace,
    )
    return namespace


def _generated_schema10_manifest() -> tuple[dict[str, object], dict[str, object]]:
    """Build and JSON-roundtrip schema 10 without writing generated artifacts."""
    namespace = _full_generator_namespace()
    manifest = json.loads(
        analyze_prbm.canonical_json(
            namespace.get("_schema10_manifest", namespace["cases_manifest"])(),
            pretty=True,
        )
    )
    return namespace, manifest


def _source_s1_cases_from_generator() -> list[dict[str, object]]:
    """Load the raw logical S1 matrix without invoking its runtime oracle."""
    namespace = _source_generator_namespace()
    raw_cases = namespace["create_s1_cases"]()
    rendered_cases: list[dict[str, object]] = []
    for raw_case in raw_cases:
        anchors = []
        for raw_anchor in raw_case["anchors"]:
            anchor = json.loads(json.dumps(raw_anchor))
            x, y, z = raw_anchor["position"]
            anchor["position"] = {"x": x, "y": y, "z": z}
            anchor["face_parts"] = [
                {"direction": direction, **part}
                for direction, part in raw_anchor.get("face_parts", {}).items()
            ]
            anchor["facades"] = [
                {"direction": direction, "block_state": state}
                for direction, state in raw_anchor.get("facades", {}).items()
            ]
            anchors.append(anchor)
        fixtures = []
        for raw_fixture in raw_case["fixture_blocks"]:
            fixture = json.loads(json.dumps(raw_fixture))
            x, y, z = raw_fixture["position"]
            fixture["position"] = {"x": x, "y": y, "z": z}
            fixtures.append(fixture)
        rendered_cases.append(
            {
                **{
                    key: value
                    for key, value in raw_case.items()
                    if key not in {"anchors", "fixture_blocks"}
                },
                "anchors": anchors,
                "fixture_blocks": fixtures,
            }
        )
    return rendered_cases


def _cross(
    first: tuple[int, int, int], second: tuple[int, int, int]
) -> tuple[int, int, int]:
    return (
        first[1] * second[2] - first[2] * second[1],
        first[2] * second[0] - first[0] * second[2],
        first[0] * second[1] - first[1] * second[0],
    )


def _terminal_triangles(
    position: tuple[int, int, int],
    direction: str,
    spin: int,
    material: int,
    rgb: tuple[int, int, int],
    blocklight: int,
    sunlight: int,
) -> list[FixtureTriangle]:
    normal = analyze_prbm.DIRECTION_VECTORS[direction]
    up = analyze_prbm.DIRECTION_VECTORS[
        analyze_prbm.TERMINAL_UP_DIRECTIONS[direction][spin]
    ]
    right = _cross(normal, up)
    center = tuple(
        position[axis] + 0.5 + normal[axis] * 0.5 for axis in range(3)
    )

    def corner(right_scale: float, up_scale: float) -> tuple[float, float, float]:
        return tuple(
            center[axis]
            + right[axis] * right_scale
            + up[axis] * up_scale
            for axis in range(3)
        )

    bottom_right = corner(0.375, -0.375)
    bottom_left = corner(-0.375, -0.375)
    top_left = corner(-0.375, 0.375)
    top_right = corner(0.375, 0.375)
    return [
        (
            material,
            (bottom_right, bottom_left, top_left),
            rgb,
            blocklight,
            sunlight,
            255,
            ((0.125, 0.875), (0.875, 0.875), (0.875, 0.125)),
        ),
        (
            material,
            (bottom_right, top_left, top_right),
            rgb,
            blocklight,
            sunlight,
            255,
            ((0.125, 0.875), (0.875, 0.125), (0.125, 0.125)),
        ),
    ]


def _facade_triangles(
    position: tuple[int, int, int],
    direction: str,
    material: int,
) -> list[FixtureTriangle]:
    normal = analyze_prbm.DIRECTION_VECTORS[direction]
    fixed_axis = next(axis for axis, component in enumerate(normal) if component)
    projected_axes = [axis for axis in range(3) if axis != fixed_axis]
    fixed = 0.03 if normal[fixed_axis] < 0 else 0.97
    strips = (
        ((0.0, 0.05), (0.10, 0.05), (0.05, 0.95)),
        ((1.0, 0.05), (0.95, 0.95), (0.90, 0.05)),
        ((0.05, 0.0), (0.95, 0.0), (0.50, 0.10)),
        ((0.05, 1.0), (0.50, 0.90), (0.95, 1.0)),
    )
    output = []
    for strip in strips:
        triangle = []
        for first, second in strip:
            coordinates = [0.0, 0.0, 0.0]
            coordinates[fixed_axis] = fixed
            coordinates[projected_axes[0]] = first
            coordinates[projected_axes[1]] = second
            triangle.append(
                tuple(position[axis] + coordinates[axis] for axis in range(3))
            )
        output.extend(
            (
                material,
                tuple(triangle),
                (255, 255, 255),
                0,
                15,
                255,
                DEFAULT_UVS,
            )
            for _repeat in range(12)
        )
    return output


def _fixture_quad(
    material: int,
    vertices: tuple[tuple[float, float, float], ...],
    rgb: tuple[int, int, int],
    blocklight: int,
    sunlight: int,
    ao: int,
    uv_rect: tuple[int, int, int, int] | None = None,
) -> list[FixtureTriangle]:
    if uv_rect is None:
        uvs = ((0.0, 0.0),) * 4
    else:
        u0, v0, u1, v1 = (value / 16 for value in uv_rect)
        uvs = ((u0, v1), (u1, v1), (u1, v0), (u0, v0))
    return [
        (material, (vertices[0], vertices[1], vertices[2]), rgb, blocklight, sunlight, ao, (uvs[0], uvs[1], uvs[2])),
        (material, (vertices[0], vertices[2], vertices[3]), rgb, blocklight, sunlight, ao, (uvs[0], uvs[2], uvs[3])),
    ]


def _glass_face_triangles(
    face: dict[str, object],
    material_indexes: dict[str, int],
) -> list[FixtureTriangle]:
    direction = face["direction"]
    assert isinstance(direction, str)
    corners = analyze_prbm.CONNECTED_GLASS_FACE_CORNERS[direction]
    base_resource = face["base_resource"]
    base_uvs = face["base_uv_corners"]
    assert isinstance(base_resource, str)
    assert isinstance(base_uvs, list)
    output: list[FixtureTriangle] = []
    for indexes in ((0, 1, 2), (0, 2, 3)):
        output.append(
            (
                material_indexes[base_resource],
                tuple(tuple(float(value) for value in corners[index]) for index in indexes),
                (255, 255, 255),
                0,
                15,
                255,
                tuple(tuple(float(value) for value in base_uvs[index]) for index in indexes),
            )
        )
    frame_resource = face["frame_resource"]
    frame_uvs = face["frame_uv_corners"]
    if frame_resource is not None:
        assert isinstance(frame_resource, str)
        assert isinstance(frame_uvs, list)
        for indexes in ((0, 1, 2), (0, 2, 3)):
            output.append(
                (
                    material_indexes[frame_resource],
                    tuple(tuple(float(value) for value in corners[index]) for index in indexes),
                    (255, 255, 255),
                    0,
                    15,
                    255,
                    tuple(tuple(float(value) for value in frame_uvs[index]) for index in indexes),
                )
            )
    return output


def _crafting_face_triangles(
    face: dict[str, object],
    material_indexes: dict[str, int],
) -> list[FixtureTriangle]:
    direction = face["direction"]
    primitives = face["primitives"]
    assert isinstance(direction, str)
    assert isinstance(primitives, list)
    output: list[FixtureTriangle] = []
    for primitive in primitives:
        assert isinstance(primitive, dict)
        resource = primitive["resource"]
        bounds = primitive["bounds_sixteenths"]
        rgb = primitive["rgb_u8"]
        assert isinstance(resource, str)
        assert isinstance(bounds, list)
        assert isinstance(rgb, list)
        corners, uvs = analyze_prbm._crafting_quad(direction, bounds)
        fullbright = primitive["light_policy"] == "fullbright-15"
        for indexes in ((0, 1, 2), (0, 2, 3)):
            output.append(
                (
                    material_indexes[resource],
                    tuple(corners[index] for index in indexes),
                    tuple(rgb),
                    15 if fullbright else 0,
                    15,
                    255,
                    tuple(uvs[index] for index in indexes),
                )
            )
    return output


def _drive_world_point(
    position: tuple[int, int, int],
    facing: str,
    spin: int,
    point: tuple[float, float, float],
) -> tuple[float, float, float]:
    oriented = analyze_prbm._drive_transform_point(point, facing, spin)
    return tuple(position[axis] + oriented[axis] for axis in range(3))


def _drive_chassis_triangles(
    position: tuple[int, int, int],
    facing: str,
    spin: int,
    slot: dict[str, object],
    material: int,
) -> list[FixtureTriangle]:
    origin = slot["slot_origin"]
    assert isinstance(origin, dict)
    model_id = slot["model_id"]
    assert isinstance(model_id, str)
    ox, oy, oz = (float(origin[axis]) for axis in ("x", "y", "z"))
    x0, x1 = ox, ox + 6 / 16
    y0, y1 = oy, oy + 2 / 16
    z0, z1 = oz, oz + 2 / 16
    quads = (
        ((x1, y0, z0), (x0, y0, z0), (x0, y1, z0), (x1, y1, z0)),
        ((x1, y1, z0), (x0, y1, z0), (x0, y1, z1), (x1, y1, z1)),
        ((x0, y0, z0), (x1, y0, z0), (x1, y0, z1), (x0, y0, z1)),
    )
    output: list[FixtureTriangle] = []
    for quad, uv_rect in zip(
        quads, analyze_prbm.DRIVE_MODEL_UV_RECTS[model_id], strict=True
    ):
        output.extend(
            _fixture_quad(
                material,
                tuple(
                    _drive_world_point(position, facing, spin, point)
                    for point in quad
                ),
                (255, 255, 255),
                0,
                15,
                255,
                uv_rect,
            )
        )
    return output


def _drive_led_triangles(
    position: tuple[int, int, int],
    facing: str,
    spin: int,
    slot: dict[str, object],
    material: int,
    *,
    rgb: tuple[int, int, int] = (0, 0, 0),
    blocklight: int = 15,
    sunlight: int = 15,
    ao: int = 255,
) -> list[FixtureTriangle]:
    origin = slot["slot_origin"]
    assert isinstance(origin, dict)
    ox, oy, oz = (float(origin[axis]) for axis in ("x", "y", "z"))

    def point(x16: float, y16: float, z16: float) -> tuple[float, float, float]:
        return _drive_world_point(
            position,
            facing,
            spin,
            (ox + x16 / 16, oy + y16 / 16, oz + z16 / 16),
        )

    quads = (
        (point(4, 1, -0.001), point(5, 1, -0.001), point(5, -0.001, -0.001), point(4, -0.001, -0.001)),
        (point(5, 1, -0.001), point(5, 1, 0.999), point(5, -0.001, 0.999), point(5, -0.001, -0.001)),
        (point(4, 1, 0.999), point(4, 1, -0.001), point(4, -0.001, -0.001), point(4, -0.001, 0.999)),
        (point(4, 1, 0.999), point(5, 1, 0.999), point(5, 1, -0.001), point(4, 1, -0.001)),
        (point(4, -0.001, -0.001), point(5, -0.001, -0.001), point(5, -0.001, 0.999), point(4, -0.001, 0.999)),
    )
    return [
        triangle
        for quad in quads
        for triangle in _fixture_quad(
            material, quad, rgb, blocklight, sunlight, ao
        )
    ]


def _quantum_primitive_triangles(
    primitive: dict[str, object],
    material_indexes: dict[str, int],
) -> list[FixtureTriangle]:
    """Build the pinned CubeBuilder cuboid contract without analyzer helpers."""
    bounds = primitive["bounds_sixteenths"]
    assert isinstance(bounds, list) and len(bounds) == 6
    x1, y1, z1, x2, y2, z2 = (float(value) / 16 for value in bounds)
    resource = primitive["resource"]
    assert isinstance(resource, str)
    quads = {
        "down": (
            ((x1, y1, z2), (x1, y1, z1), (x2, y1, z1), (x2, y1, z2)),
            ((x1, z1), (x1, z2), (x2, z2), (x2, z1)),
        ),
        "up": (
            ((x1, y2, z1), (x1, y2, z2), (x2, y2, z2), (x2, y2, z1)),
            ((x1, z1), (x1, z2), (x2, z2), (x2, z1)),
        ),
        "west": (
            ((x1, y2, z1), (x1, y1, z1), (x1, y1, z2), (x1, y2, z2)),
            ((z1, 1 - y2), (z1, 1 - y1), (z2, 1 - y1), (z2, 1 - y2)),
        ),
        "east": (
            ((x2, y2, z2), (x2, y1, z2), (x2, y1, z1), (x2, y2, z1)),
            ((1 - z2, 1 - y2), (1 - z2, 1 - y1), (1 - z1, 1 - y1), (1 - z1, 1 - y2)),
        ),
        "north": (
            ((x2, y2, z1), (x2, y1, z1), (x1, y1, z1), (x1, y2, z1)),
            ((1 - x2, 1 - y2), (1 - x2, 1 - y1), (1 - x1, 1 - y1), (1 - x1, 1 - y2)),
        ),
        "south": (
            ((x1, y2, z2), (x1, y1, z2), (x2, y1, z2), (x2, y2, z2)),
            ((x1, 1 - y2), (x1, 1 - y1), (x2, 1 - y1), (x2, 1 - y2)),
        ),
    }
    return [
        (
            material_indexes[resource],
            tuple(corners[index] for index in indexes),
            (255, 255, 255),
            0,
            15,
            255,
            tuple(uvs[index] for index in indexes),
        )
        for corners, uvs in quads.values()
        for indexes in ((0, 1, 2), (0, 2, 3))
    ]


def _m3f_fixture_triangles(
    anchor: dict[str, object],
    material_indexes: dict[str, int],
) -> list[FixtureTriangle]:
    """Encode the source-derived schema-9 primitive/model contract as PRBM input."""
    raw_splotches = anchor.get("paint_splotches", [])
    assert isinstance(raw_splotches, list)
    splotches = tuple(
        analyze_prbm.PaintSplotchContract(
            int(splotch["signed_position"]),
            str(splotch["backing_side"]),
            str(splotch["visible_face"]),
            str(splotch["resource"]),
            tuple(int(value) for value in splotch["rgb_u8"]),
        )
        for splotch in raw_splotches
    )
    block_id = str(anchor["block_id"])
    stock_materials = anchor["expected_stock_material_triangles"]
    assert isinstance(stock_materials, dict)
    completion = analyze_prbm.M3CompletionContract(
        block_id=block_id,
        block_state_json=analyze_prbm.canonical_json(anchor["block_state"]),
        static_policy=str(anchor["static_policy"]),
        paint_splotches=splotches,
        pylon_axis=(str(anchor["pylon_axis"]) if anchor.get("pylon_axis") is not None else None),
        pylon_axis_position=(
            str(anchor["pylon_axis_position"])
            if anchor.get("pylon_axis_position") is not None
            else None
        ),
        expected_stock_material_triangles=tuple(
            sorted((str(resource), int(count)) for resource, count in stock_materials.items())
        ),
        expected_stock_triangle_count=int(anchor["expected_stock_triangle_count"]),
    )
    position_value = anchor["position"]
    assert isinstance(position_value, dict)
    world_origin = tuple(int(position_value[axis]) for axis in ("x", "y", "z"))
    output: list[FixtureTriangle] = []
    for expected in analyze_prbm._m3f_expected_geometry(completion):
        if len(set(expected.aos)) != 1:
            raise AssertionError("fixture encoder requires one AO value per exact M3f triangle")
        output.append(
            (
                material_indexes[expected.material],
                tuple(
                    tuple(world_origin[axis] + point[axis] for axis in range(3))
                    for point in expected.positions
                ),
                expected.rgb,
                0,
                15,
                expected.aos[0],
                expected.uvs,
            )
        )
    return output


def _write_gzip(path: Path, payload: bytes) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_bytes(gzip.compress(payload, compresslevel=9, mtime=0))


def build_fixture(
    map_root: Path,
    *,
    cases_path: Path | None = None,
    invalid_material: bool = False,
    invalid_unselected_material: bool = False,
    reverse_texture_ordinals: bool = False,
    corrupt_smart_overlay: bool = False,
    corrupt_terminal_tint: bool = False,
    corrupt_terminal_spin: bool = False,
    corrupt_facade_layout: bool = False,
    corrupt_drive_orientation: bool = False,
    corrupt_drive_slot_translation: bool = False,
    corrupt_drive_chassis_uv: bool = False,
    corrupt_drive_led_attributes: bool = False,
    corrupt_drive_base_ao: bool = False,
    corrupt_drive_component_pair: bool = False,
    vary_extended_component_world_light: bool = False,
    corrupt_extended_component_led_light: bool = False,
    corrupt_glass_geometry: bool = False,
    corrupt_glass_winding: bool = False,
    corrupt_glass_uv: bool = False,
    corrupt_glass_material: bool = False,
    corrupt_glass_frame_material: bool = False,
    corrupt_glass_attributes: bool = False,
    corrupt_glass_rgb: bool = False,
    corrupt_glass_face_light: bool = False,
    corrupt_vibrant_glass_light: bool = False,
    vary_glass_world_light: bool = False,
    leak_glass_shared_face: bool = False,
    leak_glass_disabled: bool = False,
    corrupt_crafting_geometry: bool = False,
    corrupt_crafting_winding: bool = False,
    corrupt_crafting_uv: bool = False,
    corrupt_crafting_material: bool = False,
    corrupt_crafting_rgb: bool = False,
    corrupt_crafting_ao: bool = False,
    corrupt_crafting_light: bool = False,
    leak_crafting_disabled: bool = False,
    corrupt_quantum_geometry: bool = False,
    corrupt_quantum_winding: bool = False,
    corrupt_quantum_uv: bool = False,
    corrupt_quantum_material: bool = False,
    corrupt_quantum_rgb: bool = False,
    corrupt_quantum_ao: bool = False,
    corrupt_quantum_light: bool = False,
    corrupt_m3f_geometry: bool = False,
    corrupt_m3f_winding_normal: bool = False,
    corrupt_m3f_uv: bool = False,
    corrupt_m3f_material: bool = False,
    corrupt_m3f_rgb: bool = False,
    corrupt_m3f_ao: bool = False,
    corrupt_m3f_face_light: bool = False,
    corrupt_m3f_paint_layer: bool = False,
    corrupt_m3f_paint_clamp: bool = False,
    corrupt_m3f_chest_orientation: bool = False,
    corrupt_m3f_crank_orientation: bool = False,
    corrupt_m3f_inscriber_orientation: bool = False,
    corrupt_m3f_pylon_topology: bool = False,
    corrupt_m3f_chest_lock_uv: bool = False,
    corrupt_m3f_chest_lock_bounds: bool = False,
    corrupt_m3f_chest_texture: bool = False,
    corrupt_m3f_crank_shaft_north: bool = False,
    corrupt_m3f_crank_handle_placement: bool = False,
    corrupt_m3f_crank_uv_rotation: bool = False,
    corrupt_m3f_inscriber_material_split: bool = False,
    corrupt_m3f_inscriber_stamp_position: bool = False,
    corrupt_m3f_inscriber_stamp_uv: bool = False,
    corrupt_m3f_pylon_layer_material: bool = False,
    corrupt_m3f_pylon_x_uv: bool = False,
    corrupt_m3f_pylon_y_uv: bool = False,
    corrupt_m3f_pylon_z_uv: bool = False,
    corrupt_m3f_family_ao: str | None = None,
    corrupt_m3f_family_light: str | None = None,
    leak_quantum_disabled: bool = False,
    include_dense: bool = False,
    leak_device_fallback: bool = False,
    leak_m2_fallback: bool = False,
    leak_m3_fallback: bool = False,
    leak_m3b_fallback: bool = False,
    leak_m3d_fallback: bool = False,
    stock_baseline: bool = False,
    extension_disabled: bool = False,
    glass_disabled: bool = False,
    crafting_disabled: bool = False,
    quantum_disabled: bool = False,
    m3_completion_disabled: bool = False,
    leak_m3_completion_disabled: bool = False,
    native_structural_disabled: bool = False,
    leak_native_structural_disabled: bool = False,
    m45_route_disabled: str | None = None,
    m45_disabled: bool = False,
    leak_m45_route_disabled: bool = False,
    leak_m45_projection_kind: str | None = None,
    corrupt_m45_inherited_projection: bool = False,
    wrong_m45_route_material: bool = False,
    corrupt_m45_singleton_materials: bool = False,
    leak_m45_missing_elsewhere: bool = False,
    corrupt_m45_extended_plane_observation: bool = False,
    empty_m45_legacy_upgrade: bool = False,
    leak_m45_legacy_upgrade: bool = False,
    leak_stock_anchor: bool = False,
    stock_stone_triangle_count: int = 10,
    stock_wrong_material: bool = False,
) -> None:
    if cases_path is None:
        cases_path = CASES_PATH
    map_root.mkdir(parents=True)
    settings = {
        "hires": {"tileSize": [32, 32], "scale": [1, 1], "translate": [2, 2]}
    }
    (map_root / "settings.json").write_text(
        json.dumps(settings, sort_keys=True, separators=(",", ":")),
        encoding="utf-8",
    )
    cases = json.loads(cases_path.read_text(encoding="utf-8"))
    if leak_m45_projection_kind not in {None, "stock", "native-center"}:
        raise AssertionError("unknown M4/M5 projected-resource leak kind")
    s1_active = (
        cases.get("schema_version") == 11
        and not stock_baseline
        and not native_structural_disabled
    )
    runtime_s1_materials = (
        _native_structural_runtime_fixture_materials() if s1_active else {}
    )
    m45_profiles = {
        profile["route"]: profile
        for profile in cases.get("profile", {}).get("m45_routes", [])
    }
    m45_legacy_upgrades = {
        (
            row["case_id"],
            tuple(row["position"][axis] for axis in ("x", "y", "z")),
        ): row
        for row in cases.get("m45_legacy_upgrades", {}).get("rows", [])
    }
    extended_plane_materials_by_position: dict[
        tuple[int, int, int], list[str]
    ] = {}
    if cases.get("schema_version") == 11:
        plane_case = next(
            case
            for case in cases["cases"]
            if case.get("route") == "extendedae-planes"
        )
        plane_profile = m45_profiles["extendedae-planes"]
        plane_tokens = [
            resource
            for resource, count in plane_profile[
                "enabled_live_observation"
            ]["material_triangles"].items()
            for _repeat in range(count)
        ]
        plane_positions = [
            tuple(anchor["position"][axis] for axis in ("x", "y", "z"))
            for anchor in plane_case["anchors"]
        ]
        if len(plane_positions) != 42 or len(plane_tokens) != 3_244:
            raise AssertionError("Extended plane synthetic observation changed")
        for position in plane_positions:
            extended_plane_materials_by_position[position] = [
                plane_tokens.pop(0)
            ]
        extended_plane_materials_by_position[plane_positions[0]].extend(
            plane_tokens
        )
    texture_paths = ["bluemap:block/missing", "minecraft:block/stone"]
    if cases.get("schema_version") == 11:
        for profile in m45_profiles.values():
            texture_paths.extend(profile["stock_material_allowlist"])
        texture_paths.extend(
            resource
            for case in cases["cases"]
            for anchor in case["anchors"]
            for resource in anchor.get(
                "selector_scoped_model_exception", {}
            ).get("expected_material_triangles", {})
        )
    if s1_active:
        texture_paths.extend(runtime_s1_materials.values())
    if not stock_baseline:
        texture_paths.extend(("ae2:block/energy_acceptor", "ae2:block/controller"))
        texture_paths.extend(cases["profile"]["selected_resources"])
    if cases.get("schema_version", 0) >= 9:
        texture_paths.extend(analyze_prbm.M3_COMPLETION_STOCK_RESOURCES)
    textures = [
        {"resourcePath": resource_path}
        for index, resource_path in enumerate(texture_paths)
        if resource_path not in texture_paths[:index]
    ]
    if reverse_texture_ordinals:
        textures.reverse()
    material_indexes = {
        entry["resourcePath"]: index for index, entry in enumerate(textures)
    }
    runtime_s1_triangles = (
        _native_structural_runtime_triangles(cases, material_indexes)
        if s1_active
        else {}
    )
    _write_gzip(
        map_root / "textures.json.gz",
        json.dumps(textures, sort_keys=True, separators=(",", ":")).encode("utf-8"),
    )

    triangles_by_tile: dict[
        tuple[int, int], list[FixtureTriangle | ExactFixtureTriangle]
    ] = {}
    wrote_invalid_unselected = False
    wrote_invalid_selected = False
    wrote_corrupt_overlay = False
    wrote_corrupt_terminal_tint = False
    wrote_corrupt_terminal_spin = False
    wrote_corrupt_facade_layout = False
    wrote_corrupt_drive_orientation = False
    wrote_corrupt_drive_slot_translation = False
    wrote_corrupt_drive_chassis_uv = False
    wrote_corrupt_drive_led_attributes = False
    wrote_corrupt_drive_base_ao = False
    wrote_corrupt_drive_component_pair = False
    wrote_extended_component_world_light = False
    wrote_corrupt_extended_component_led_light = False
    wrote_corrupt_glass_geometry = False
    wrote_corrupt_glass_winding = False
    wrote_corrupt_glass_uv = False
    wrote_corrupt_glass_material = False
    wrote_corrupt_glass_frame_material = False
    wrote_corrupt_glass_attributes = False
    wrote_corrupt_glass_rgb = False
    wrote_corrupt_glass_face_light = False
    wrote_corrupt_vibrant_glass_light = False
    wrote_varied_glass_world_light = False
    wrote_glass_shared_face = False
    wrote_corrupt_crafting_geometry = False
    wrote_corrupt_crafting_winding = False
    wrote_corrupt_crafting_uv = False
    wrote_corrupt_crafting_material = False
    wrote_corrupt_crafting_rgb = False
    wrote_corrupt_crafting_ao = False
    wrote_corrupt_crafting_light = False
    wrote_corrupt_quantum_geometry = False
    wrote_corrupt_quantum_winding = False
    wrote_corrupt_quantum_uv = False
    wrote_corrupt_quantum_material = False
    wrote_corrupt_quantum_rgb = False
    wrote_corrupt_quantum_ao = False
    wrote_corrupt_quantum_light = False
    wrote_corrupt_m3f_geometry = False
    wrote_corrupt_m3f_winding_normal = False
    wrote_corrupt_m3f_uv = False
    wrote_corrupt_m3f_material = False
    wrote_corrupt_m3f_rgb = False
    wrote_corrupt_m3f_ao = False
    wrote_corrupt_m3f_face_light = False
    wrote_corrupt_m3f_paint_layer = False
    wrote_corrupt_m3f_paint_clamp = False
    wrote_corrupt_m3f_chest_orientation = False
    wrote_corrupt_m3f_crank_orientation = False
    wrote_corrupt_m3f_inscriber_orientation = False
    wrote_corrupt_m3f_pylon_topology = False
    wrote_corrupt_m3f_chest_lock_uv = False
    wrote_corrupt_m3f_chest_lock_bounds = False
    wrote_corrupt_m3f_chest_texture = False
    wrote_corrupt_m3f_crank_shaft_north = False
    wrote_corrupt_m3f_crank_handle_placement = False
    wrote_corrupt_m3f_crank_uv_rotation = False
    wrote_corrupt_m3f_inscriber_material_split = False
    wrote_corrupt_m3f_inscriber_stamp_position = False
    wrote_corrupt_m3f_inscriber_stamp_uv = False
    wrote_corrupt_m3f_pylon_layer_material = False
    wrote_corrupt_m3f_pylon_x_uv = False
    wrote_corrupt_m3f_pylon_y_uv = False
    wrote_corrupt_m3f_pylon_z_uv = False
    wrote_corrupt_m3f_family_ao = False
    wrote_corrupt_m3f_family_light = False
    wrote_fallback_leak = False
    wrote_m2_fallback_leak = False
    wrote_m3_fallback_leak = False
    wrote_m3b_fallback_leak = False
    wrote_m3d_fallback_leak = False
    wrote_m3_completion_leak = False
    wrote_native_structural_leak = False
    wrote_m45_route_leak = False
    wrote_m45_projection_material_leak = False
    wrote_corrupt_m45_inherited_projection = False
    wrote_wrong_m45_route_material = False
    wrote_corrupt_m45_singleton_materials = False
    wrote_m45_missing_elsewhere = False
    wrote_corrupt_m45_extended_plane_observation = False
    wrote_empty_m45_legacy_upgrade = False
    wrote_m45_legacy_upgrade_leak = False
    wrote_stock_leak = False
    for case in cases["cases"]:
        for anchor in case["anchors"]:
            x, y, z = (anchor["position"][axis] for axis in ("x", "y", "z"))
            tile = (math.floor((x - 2) / 32), math.floor((z - 2) / 32))
            origin = (tile[0] * 32 + 2, tile[1] * 32 + 2)
            triangles_by_tile.setdefault(tile, [])

            accepted_s1_triangles = runtime_s1_triangles.get((x, y, z))
            if accepted_s1_triangles is not None:
                triangles_by_tile[tile].extend(accepted_s1_triangles)
                continue

            expected_path = anchor["expected_path"]
            m45_legacy_upgrade = m45_legacy_upgrades.get(
                (case["case_id"], (x, y, z))
            )
            predecessor_projection = (
                anchor.get("schema9_route_disabled_projection")
                if native_structural_disabled and case.get("milestone") == "S1"
                else None
            )
            m45_projection = None
            if expected_path in {"custom-m45", "stock-fallback-m45"}:
                if stock_baseline:
                    m45_projection = anchor["physical_stock_projection"]
                elif native_structural_disabled:
                    candidate = anchor["native_structural_disabled_projection"]
                    if (
                        candidate["expected_path"] != expected_path
                        or candidate["review_projection"]
                        != anchor["review_projection"]
                    ):
                        m45_projection = candidate
                elif crafting_disabled:
                    m45_projection = anchor.get("crafting_disabled_projection")
                elif m45_route_disabled is not None:
                    m45_projection = anchor["route_disabled_projections"].get(
                        m45_route_disabled
                    )
                elif m45_disabled:
                    m45_projection = anchor["route_disabled_projections"].get(
                        anchor["m45_route"]
                    )
            if m45_legacy_upgrade is not None:
                required_m45_routes = set(
                    m45_legacy_upgrade["required_m45_routes"]
                )
                required_legacy_routes = set(
                    m45_legacy_upgrade["required_legacy_routes"]
                )
                active = not (
                    stock_baseline
                    or m45_disabled
                    or m45_route_disabled in required_m45_routes
                    or (
                        extension_disabled
                        and "extension" in required_legacy_routes
                    )
                    or (
                        crafting_disabled
                        and "crafting" in required_legacy_routes
                    )
                )
                if active:
                    observation = m45_legacy_upgrade["live_observation"]
                    materials = [
                        (
                            material_indexes[resource],
                            (255, 255, 255),
                            0,
                            15,
                        )
                        for resource, count in observation[
                            "material_triangles"
                        ].items()
                        for _repeat in range(count)
                    ]
                    if (
                        empty_m45_legacy_upgrade
                        and not wrote_empty_m45_legacy_upgrade
                    ):
                        materials = []
                        wrote_empty_m45_legacy_upgrade = True
                    if (
                        leak_m45_legacy_upgrade
                        and not wrote_m45_legacy_upgrade_leak
                    ):
                        materials.append(
                            (
                                material_indexes["bluemap:block/missing"],
                                (255, 255, 255),
                                0,
                                15,
                            )
                        )
                        wrote_m45_legacy_upgrade_leak = True
                else:
                    materials = []
            elif "selector_scoped_model_exception" in anchor:
                materials = [
                    (
                        material_indexes[resource],
                        (255, 255, 255),
                        0,
                        15,
                    )
                    for resource, count in anchor[
                        "selector_scoped_model_exception"
                    ]["expected_material_triangles"].items()
                    for _repeat in range(count)
                ]
                if corrupt_m45_singleton_materials:
                    materials.pop()
                    wrote_corrupt_m45_singleton_materials = True
            elif m45_projection is not None:
                materials = []
                if m45_projection["review_projection"] == "nonempty":
                    route_profile = m45_profiles[case["route"]]
                    exact_materials = m45_projection.get(
                        "expected_material_triangles"
                    )
                    if exact_materials:
                        materials.extend(
                            (
                                material_indexes[resource],
                                (255, 255, 255),
                                0,
                                15,
                            )
                            for resource, count in exact_materials.items()
                            for _repeat in range(count)
                        )
                        if (
                            corrupt_m45_inherited_projection
                            and not wrote_corrupt_m45_inherited_projection
                        ):
                            materials.pop()
                            wrote_corrupt_m45_inherited_projection = True
                    else:
                        resource = m45_projection["allowed_resources"][0]
                        materials.append(
                            (material_indexes[resource], (255, 255, 255), 0, 15)
                        )
                    expected_projection_path = (
                        "native-center-only-m45"
                        if leak_m45_projection_kind == "native-center"
                        else None
                    )
                    leak_matches = (
                        leak_m45_projection_kind == "stock"
                        and "stock" in m45_projection["expected_path"]
                    ) or (
                        expected_projection_path is not None
                        and m45_projection["expected_path"]
                        == expected_projection_path
                    )
                    if leak_matches and not wrote_m45_projection_material_leak:
                        allowed = set(m45_projection["allowed_resources"])
                        leak_resource = next(
                            resource
                            for resource in route_profile["route_resources"]
                            if resource not in allowed
                        )
                        materials.append(
                            (
                                material_indexes[leak_resource],
                                (255, 255, 255),
                                0,
                                15,
                            )
                        )
                        wrote_m45_projection_material_leak = True
                if (
                    leak_m45_route_disabled
                    and m45_route_disabled is not None
                    and m45_projection["review_projection"] == "empty"
                    and not wrote_m45_route_leak
                ):
                    leak_resource = m45_profiles[case["route"]][
                        "stock_material_allowlist"
                    ][0]
                    materials.append(
                        (
                            material_indexes[leak_resource],
                            (255, 255, 255),
                            0,
                            15,
                        )
                    )
                    wrote_m45_route_leak = True
            elif (
                expected_path == "custom-m45"
                and case.get("route") == "extendedae-planes"
            ):
                plane_materials = list(
                    extended_plane_materials_by_position[(x, y, z)]
                )
                if (
                    corrupt_m45_extended_plane_observation
                    and not wrote_corrupt_m45_extended_plane_observation
                ):
                    plane_materials.pop()
                    wrote_corrupt_m45_extended_plane_observation = True
                materials = [
                    (
                        material_indexes[resource],
                        (255, 255, 255),
                        0,
                        15,
                    )
                    for resource in plane_materials
                ]
            elif stock_baseline:
                materials = []
                if expected_path == "stock-control":
                    stock_resource = (
                        "bluemap:block/missing"
                        if stock_wrong_material
                        else "minecraft:block/stone"
                    )
                    materials = [
                        (material_indexes[stock_resource], (255, 255, 255), 0, 15)
                        for _repeat in range(stock_stone_triangle_count)
                    ]
                elif case.get("milestone") == "M3f":
                    materials = [
                        (
                            material_indexes[resource_path],
                            (255, 255, 255),
                            0,
                            15,
                        )
                        for resource_path, count in anchor[
                            "expected_stock_material_triangles"
                        ].items()
                        for _repeat in range(count)
                    ]
                elif leak_stock_anchor and not wrote_stock_leak:
                    materials = [
                        (
                            material_indexes["bluemap:block/missing"],
                            (255, 255, 255),
                            0,
                            15,
                        )
                    ]
                    wrote_stock_leak = True
            elif expected_path == "custom-m45":
                route = case["route"]
                resource = m45_profiles[route]["route_resources"][0]
                if wrong_m45_route_material and not wrote_wrong_m45_route_material:
                    allowed = set(m45_profiles[route]["material_allowlist"])
                    resource = next(
                        candidate
                        for other_route, other_profile in m45_profiles.items()
                        if other_route != route
                        for candidate in other_profile["route_resources"]
                        if candidate not in allowed
                    )
                    wrote_wrong_m45_route_material = True
                materials = [
                    (material_indexes[resource], (255, 255, 255), 0, 15)
                ]
            elif expected_path == "stock-fallback-m45":
                materials = []
            elif extension_disabled and expected_path in {
                "custom-m3b",
                "stock-fallback-m3b",
            }:
                materials = []
            elif glass_disabled and expected_path == "custom-m3c":
                materials = []
            elif crafting_disabled and expected_path in {
                "custom-m3d",
                "stock-fallback-m3d",
            }:
                materials = []
            elif quantum_disabled and expected_path == "custom-m3e":
                materials = []
            elif m3_completion_disabled and case.get("milestone") == "M3f":
                materials = [
                    (
                        material_indexes[resource_path],
                        (255, 255, 255),
                        0,
                        15,
                    )
                    for resource_path, count in anchor[
                        "expected_stock_material_triangles"
                    ].items()
                    for _repeat in range(count)
                ]
                if leak_m3_completion_disabled and not wrote_m3_completion_leak:
                    materials.append(
                        (
                            material_indexes["bluemap:block/missing"],
                            (255, 255, 255),
                            0,
                            15,
                        )
                    )
                    wrote_m3_completion_leak = True
            elif native_structural_disabled and case.get("milestone") == "S1":
                if not isinstance(predecessor_projection, dict):
                    raise AssertionError(
                        "native-structural-disabled fixture lacks predecessor metadata"
                    )
                predecessor_path = predecessor_projection["expected_path"]
                if predecessor_path == "stock-empty":
                    materials = []
                else:
                    predecessor_materials = dict(
                        predecessor_projection["expected_material_triangles"]
                    )
                    predecessor_special = set(
                        predecessor_projection.get("expected_terminal_layers", {})
                    )
                    if anchor.get("facades"):
                        predecessor_special.add("minecraft:block/stone")
                    materials = [
                        (
                            material_indexes[resource_path],
                            tuple(
                                predecessor_projection.get(
                                    "expected_smart_overlays", {}
                                )
                                .get(resource_path, {})
                                .get("rgb_u8", (255, 255, 255))
                            ),
                            predecessor_projection.get(
                                "expected_smart_overlays", {}
                            )
                            .get(resource_path, {})
                            .get("blocklight_raw_i8", 0),
                            predecessor_projection.get(
                                "expected_smart_overlays", {}
                            )
                            .get(resource_path, {})
                            .get("sunlight_raw_i8", 15),
                        )
                        for resource_path, count in predecessor_materials.items()
                        if resource_path not in predecessor_special
                        for _repeat in range(count)
                    ]
                if leak_native_structural_disabled and not wrote_native_structural_leak:
                    materials.append(
                        (
                            material_indexes["bluemap:block/missing"],
                            (255, 255, 255),
                            0,
                            15,
                        )
                    )
                    wrote_native_structural_leak = True
            elif expected_path in {"custom-m1", "custom-m2", "custom-m3", "custom-m3b", "custom-m3c", "custom-m3d", "custom-m3e", "custom-m3f", "custom-s1"}:
                if expected_path == "custom-s1":
                    raise AssertionError(
                        "synthetic enabled S1 geometry must use the independent runtime oracle"
                    )
                if expected_path in {"custom-m3c", "custom-m3d", "custom-m3e"}:
                    materials = []
                    material_counts = {}
                    special_resources = set()
                else:
                    special_resources = set(anchor.get("expected_terminal_layers", {}))
                    if anchor.get("facades"):
                        special_resources.add("minecraft:block/stone")
                    material_counts = dict(anchor["expected_material_triangles"])
                if expected_path in {"custom-m3", "custom-m3b"}:
                    occupied_models = [
                        slot
                        for slot, inventory_slot in zip(
                            anchor["expected_drive_models"]["slots"],
                            anchor["inventory"]["slots"],
                            strict=True,
                        )
                        if not inventory_slot.get("empty")
                    ]
                    special_resources.update(
                        slot.get("material", analyze_prbm.DRIVE_CELL_MATERIAL)
                        for slot in occupied_models
                    )
                    material_counts[analyze_prbm.DRIVE_LED_MATERIAL] -= anchor[
                        "expected_drive_led"
                    ]["triangle_count"]
                if expected_path == "custom-m3f" and anchor["block_id"] == "ae2:paint":
                    materials = [
                        (
                            material_indexes[splotch["resource"]],
                            tuple(splotch["rgb_u8"]),
                            0,
                            15,
                        )
                        for splotch in anchor["paint_splotches"]
                        for _repeat in range(2)
                    ]
                elif expected_path not in {"custom-m3c", "custom-m3d", "custom-m3e"}:
                    materials = [
                        (
                            material_indexes[resource_path],
                            tuple(
                                anchor.get("expected_smart_overlays", {})
                                .get(resource_path, {})
                                .get("rgb_u8", (255, 255, 255))
                            ),
                            anchor.get("expected_smart_overlays", {})
                            .get(resource_path, {})
                            .get("blocklight_raw_i8", 0),
                            anchor.get("expected_smart_overlays", {})
                            .get(resource_path, {})
                            .get("sunlight_raw_i8", 15),
                        )
                        for resource_path, count in material_counts.items()
                        if resource_path not in special_resources
                        for _repeat in range(count)
                    ]
            elif expected_path in {
                "stock-fallback-device-endpoint",
                "stock-fallback-m2",
                "stock-fallback-m3",
                "stock-fallback-m3b",
                "stock-fallback-m3d",
                "stock-fallback-m3f",
                "stock-fallback-s1",
            }:
                materials = []
                leak_this = (
                    leak_device_fallback and not wrote_fallback_leak
                ) or (
                    expected_path == "stock-fallback-m2"
                    and leak_m2_fallback
                    and not wrote_m2_fallback_leak
                ) or (
                    expected_path == "stock-fallback-m3"
                    and leak_m3_fallback
                    and not wrote_m3_fallback_leak
                ) or (
                    expected_path == "stock-fallback-m3b"
                    and leak_m3b_fallback
                    and not wrote_m3b_fallback_leak
                ) or (
                    expected_path == "stock-fallback-m3d"
                    and leak_m3d_fallback
                    and not wrote_m3d_fallback_leak
                )
                if leak_this:
                    materials = [
                        (
                            material_indexes["ae2:block/energy_acceptor"],
                            (255, 255, 255),
                            0,
                            15,
                        )
                    ]
                    if leak_device_fallback and not wrote_fallback_leak:
                        wrote_fallback_leak = True
                    if expected_path == "stock-fallback-m2":
                        wrote_m2_fallback_leak = True
                    if expected_path == "stock-fallback-m3":
                        wrote_m3_fallback_leak = True
                    if expected_path == "stock-fallback-m3b":
                        wrote_m3b_fallback_leak = True
                    if expected_path == "stock-fallback-m3d":
                        wrote_m3d_fallback_leak = True
            elif anchor["block_id"] == "minecraft:stone":
                materials = [
                    (material_indexes["minecraft:block/stone"], (255, 255, 255), 0, 15)
                    for _repeat in range(10)
                ]
            else:
                raise AssertionError(f"unexpected fixture path: {expected_path}")
            if (
                leak_m45_missing_elsewhere
                and expected_path == "custom-m45"
                and "selector_scoped_model_exception" not in anchor
                and not wrote_m45_missing_elsewhere
            ):
                materials.append(
                    (
                        material_indexes["bluemap:block/missing"],
                        (255, 255, 255),
                        0,
                        15,
                    )
                )
                wrote_m45_missing_elsewhere = True
            if (
                invalid_material
                and expected_path in {"custom-m1", "custom-m2"}
                and not wrote_invalid_selected
            ):
                _material, rgb, blocklight, sunlight = materials[0]
                materials[0] = (len(textures) + 10, rgb, blocklight, sunlight)
                wrote_invalid_selected = True
            if corrupt_smart_overlay and not wrote_corrupt_overlay:
                for index, (material, rgb, blocklight, sunlight) in enumerate(materials):
                    if blocklight == 15:
                        materials[index] = (
                            material,
                            ((rgb[0] + 1) & 0xFF, rgb[1], rgb[2]),
                            blocklight,
                            sunlight,
                        )
                        wrote_corrupt_overlay = True
                        break

            world_triangles = [
                (
                    material,
                    (
                        (x + 0.2, y + 0.5, z + 0.2),
                        (x + 0.8, y + 0.5, z + 0.2),
                        (x + 0.2, y + 0.5, z + 0.8),
                    ),
                    rgb,
                    blocklight,
                    sunlight,
                    255,
                    DEFAULT_UVS,
                )
                for material, rgb, blocklight, sunlight in materials
            ]
            if (
                not stock_baseline
                and not m3_completion_disabled
                and expected_path == "custom-m3f"
            ):
                world_triangles = _m3f_fixture_triangles(anchor, material_indexes)
                if corrupt_m3f_geometry and not wrote_corrupt_m3f_geometry:
                    record = world_triangles[0]
                    world_triangles[0] = (
                        record[0],
                        (
                            (record[1][0][0] + 1 / 64, *record[1][0][1:]),
                            *record[1][1:],
                        ),
                        *record[2:],
                    )
                    wrote_corrupt_m3f_geometry = True
                if (
                    corrupt_m3f_winding_normal
                    and not wrote_corrupt_m3f_winding_normal
                ):
                    record = world_triangles[0]
                    world_triangles[0] = (
                        record[0],
                        (record[1][0], record[1][2], record[1][1]),
                        *record[2:6],
                        (record[6][0], record[6][2], record[6][1]),
                    )
                    wrote_corrupt_m3f_winding_normal = True
                if corrupt_m3f_uv and not wrote_corrupt_m3f_uv:
                    record = world_triangles[0]
                    world_triangles[0] = (
                        *record[:6],
                        ((record[6][0][0] + 1 / 32, record[6][0][1]), *record[6][1:]),
                    )
                    wrote_corrupt_m3f_uv = True
                if corrupt_m3f_material and not wrote_corrupt_m3f_material:
                    record = world_triangles[0]
                    world_triangles[0] = (
                        material_indexes["bluemap:block/missing"], *record[1:]
                    )
                    wrote_corrupt_m3f_material = True
                if corrupt_m3f_rgb and not wrote_corrupt_m3f_rgb:
                    record = world_triangles[0]
                    world_triangles[0] = (
                        record[0], record[1],
                        ((record[2][0] + 1) & 0xFF, record[2][1], record[2][2]),
                        *record[3:],
                    )
                    wrote_corrupt_m3f_rgb = True
                if corrupt_m3f_ao and not wrote_corrupt_m3f_ao:
                    record = world_triangles[0]
                    world_triangles[0] = (*record[:5], 254, record[6])
                    wrote_corrupt_m3f_ao = True
                if corrupt_m3f_face_light and not wrote_corrupt_m3f_face_light:
                    record = world_triangles[0]
                    world_triangles[0] = (
                        record[0], record[1], record[2], 1, record[4], record[5], record[6]
                    )
                    wrote_corrupt_m3f_face_light = True
                if (
                    corrupt_m3f_paint_clamp
                    and not wrote_corrupt_m3f_paint_clamp
                    and anchor["block_id"] == "ae2:paint"
                ):
                    mutated = dict(anchor)
                    mutated_splotches = [dict(item) for item in anchor["paint_splotches"]]
                    mutated_splotches[0]["signed_position"] = 0
                    mutated["paint_splotches"] = mutated_splotches
                    world_triangles = _m3f_fixture_triangles(mutated, material_indexes)
                    wrote_corrupt_m3f_paint_clamp = True
                if (
                    corrupt_m3f_paint_layer
                    and not wrote_corrupt_m3f_paint_layer
                    and anchor["block_id"] == "ae2:paint"
                    and len(anchor.get("paint_splotches", ())) > 1
                ):
                    record = world_triangles[2]
                    world_triangles[2] = (
                        record[0],
                        tuple(
                            (point[0], point[1] + 1 / 256, point[2])
                            for point in record[1]
                        ),
                        *record[2:],
                    )
                    wrote_corrupt_m3f_paint_layer = True
                if (
                    corrupt_m3f_chest_orientation
                    and not wrote_corrupt_m3f_chest_orientation
                    and anchor["block_id"] == "ae2:sky_stone_chest"
                    and anchor["block_state"]["facing"] == "west"
                ):
                    mutated = dict(anchor)
                    mutated["block_state"] = {
                        **anchor["block_state"], "facing": "south"
                    }
                    world_triangles = _m3f_fixture_triangles(mutated, material_indexes)
                    wrote_corrupt_m3f_chest_orientation = True
                if (
                    corrupt_m3f_crank_orientation
                    and not wrote_corrupt_m3f_crank_orientation
                    and anchor["block_id"] == "ae2:crank"
                    and anchor["block_state"]["facing"] == "up"
                ):
                    mutated = dict(anchor)
                    mutated["block_state"] = {"facing": "north"}
                    world_triangles = _m3f_fixture_triangles(mutated, material_indexes)
                    wrote_corrupt_m3f_crank_orientation = True
                if (
                    corrupt_m3f_inscriber_orientation
                    and not wrote_corrupt_m3f_inscriber_orientation
                    and anchor["block_id"] == "ae2:inscriber"
                    and anchor["block_state"]["facing"] == "north"
                    and anchor["block_state"]["spin"] == 1
                ):
                    mutated = dict(anchor)
                    mutated["block_state"] = {
                        **anchor["block_state"], "spin": 0
                    }
                    world_triangles = _m3f_fixture_triangles(mutated, material_indexes)
                    wrote_corrupt_m3f_inscriber_orientation = True
                if (
                    corrupt_m3f_pylon_topology
                    and not wrote_corrupt_m3f_pylon_topology
                    and anchor["block_id"] == "ae2:spatial_pylon"
                    and anchor.get("pylon_axis") == "x"
                    and anchor.get("pylon_axis_position") == "start"
                ):
                    mutated = dict(anchor)
                    mutated["pylon_axis_position"] = "end"
                    world_triangles = _m3f_fixture_triangles(mutated, material_indexes)
                    wrote_corrupt_m3f_pylon_topology = True
                if (
                    corrupt_m3f_chest_lock_uv
                    and not wrote_corrupt_m3f_chest_lock_uv
                    and anchor["block_id"] == "ae2:sky_stone_chest"
                ):
                    record = world_triangles[20]
                    world_triangles[20] = (
                        *record[:6],
                        ((record[6][0][0] + 1 / 64, record[6][0][1]), *record[6][1:]),
                    )
                    wrote_corrupt_m3f_chest_lock_uv = True
                if (
                    corrupt_m3f_chest_lock_bounds
                    and not wrote_corrupt_m3f_chest_lock_bounds
                    and anchor["block_id"] == "ae2:sky_stone_chest"
                ):
                    record = world_triangles[12]
                    world_triangles[12] = (
                        record[0],
                        (
                            (record[1][0][0] + 1 / 64, *record[1][0][1:]),
                            *record[1][1:],
                        ),
                        *record[2:],
                    )
                    wrote_corrupt_m3f_chest_lock_bounds = True
                if (
                    corrupt_m3f_chest_texture
                    and not wrote_corrupt_m3f_chest_texture
                    and anchor["block_id"] == "ae2:sky_stone_chest"
                ):
                    record = world_triangles[12]
                    world_triangles[12] = (
                        material_indexes["ae2:block/skyblockchest"], *record[1:]
                    )
                    wrote_corrupt_m3f_chest_texture = True
                if (
                    corrupt_m3f_crank_shaft_north
                    and not wrote_corrupt_m3f_crank_shaft_north
                    and anchor["block_id"] == "ae2:crank"
                    and anchor["block_state"]["facing"] == "north"
                ):
                    material = material_indexes["ae2:block/crank"]
                    vertices = tuple(
                        (x + px / 16, y + py / 16, z + pz / 16)
                        for px, py, pz in (
                            (9, 7, 7), (7, 7, 7), (7, 9, 7), (9, 9, 7)
                        )
                    )
                    world_triangles.extend(
                        _fixture_quad(
                            material, vertices, (255, 255, 255), 0, 15, 255,
                            (0, 0, 2, 2),
                        )
                    )
                    wrote_corrupt_m3f_crank_shaft_north = True
                if (
                    corrupt_m3f_crank_handle_placement
                    and not wrote_corrupt_m3f_crank_handle_placement
                    and anchor["block_id"] == "ae2:crank"
                ):
                    record = world_triangles[22]
                    world_triangles[22] = (
                        record[0],
                        tuple(
                            (point[0] + 1 / 32, point[1], point[2])
                            for point in record[1]
                        ),
                        *record[2:],
                    )
                    wrote_corrupt_m3f_crank_handle_placement = True
                if (
                    corrupt_m3f_crank_uv_rotation
                    and not wrote_corrupt_m3f_crank_uv_rotation
                    and anchor["block_id"] == "ae2:crank"
                ):
                    record = world_triangles[12]
                    world_triangles[12] = (
                        *record[:6],
                        (record[6][1], record[6][2], record[6][0]),
                    )
                    wrote_corrupt_m3f_crank_uv_rotation = True
                if (
                    corrupt_m3f_inscriber_material_split
                    and not wrote_corrupt_m3f_inscriber_material_split
                    and anchor["block_id"] == "ae2:inscriber"
                ):
                    shell_record = world_triangles[0]
                    stamp_record = world_triangles[66]
                    world_triangles[0] = (stamp_record[0], *shell_record[1:])
                    world_triangles[66] = (shell_record[0], *stamp_record[1:])
                    wrote_corrupt_m3f_inscriber_material_split = True
                if (
                    corrupt_m3f_inscriber_stamp_position
                    and not wrote_corrupt_m3f_inscriber_stamp_position
                    and anchor["block_id"] == "ae2:inscriber"
                ):
                    record = world_triangles[66]
                    world_triangles[66] = (
                        record[0],
                        tuple(
                            (point[0], point[1] + 1 / 256, point[2])
                            for point in record[1]
                        ),
                        *record[2:],
                    )
                    wrote_corrupt_m3f_inscriber_stamp_position = True
                if (
                    corrupt_m3f_inscriber_stamp_uv
                    and not wrote_corrupt_m3f_inscriber_stamp_uv
                    and anchor["block_id"] == "ae2:inscriber"
                ):
                    record = world_triangles[68]
                    world_triangles[68] = (
                        *record[:6],
                        ((record[6][0][0], record[6][0][1] + 1 / 32), *record[6][1:]),
                    )
                    wrote_corrupt_m3f_inscriber_stamp_uv = True
                if (
                    corrupt_m3f_pylon_layer_material
                    and not wrote_corrupt_m3f_pylon_layer_material
                    and anchor["block_id"] == "ae2:spatial_pylon"
                    and anchor.get("pylon_axis_position") == "none"
                ):
                    outer = world_triangles[0]
                    # Swap across different coincident-cube faces. Swapping the
                    # same face between identical outer/inner cubes is
                    # intentionally PRBM-indistinguishable as an unordered mesh.
                    inner = world_triangles[14]
                    world_triangles[0] = (inner[0], *outer[1:])
                    world_triangles[14] = (outer[0], *inner[1:])
                    wrote_corrupt_m3f_pylon_layer_material = True
                for axis, role, enabled, wrote_name in (
                    ("x", "start", corrupt_m3f_pylon_x_uv, "x"),
                    ("y", "end", corrupt_m3f_pylon_y_uv, "y"),
                    ("z", "start", corrupt_m3f_pylon_z_uv, "z"),
                ):
                    already = {
                        "x": wrote_corrupt_m3f_pylon_x_uv,
                        "y": wrote_corrupt_m3f_pylon_y_uv,
                        "z": wrote_corrupt_m3f_pylon_z_uv,
                    }[wrote_name]
                    if (
                        enabled and not already
                        and anchor["block_id"] == "ae2:spatial_pylon"
                        and anchor.get("pylon_axis") == axis
                        and anchor.get("pylon_axis_position") == role
                    ):
                        record = world_triangles[0]
                        world_triangles[0] = (
                            *record[:6],
                            ((1.0 - record[6][0][0], record[6][0][1]), *record[6][1:]),
                        )
                        if wrote_name == "x":
                            wrote_corrupt_m3f_pylon_x_uv = True
                        elif wrote_name == "y":
                            wrote_corrupt_m3f_pylon_y_uv = True
                        else:
                            wrote_corrupt_m3f_pylon_z_uv = True
                family = (
                    "chest"
                    if anchor["block_id"] in {
                        "ae2:sky_stone_chest", "ae2:smooth_sky_stone_chest"
                    }
                    else "pylon"
                    if anchor["block_id"] == "ae2:spatial_pylon"
                    else str(anchor["block_id"]).split(":", 1)[1]
                )
                if (
                    corrupt_m3f_family_ao == family
                    and not wrote_corrupt_m3f_family_ao
                ):
                    record = world_triangles[0]
                    world_triangles[0] = (*record[:5], 254, record[6])
                    wrote_corrupt_m3f_family_ao = True
                if (
                    corrupt_m3f_family_light == family
                    and not wrote_corrupt_m3f_family_light
                ):
                    record = world_triangles[0]
                    world_triangles[0] = (
                        record[0], record[1], record[2], 1, record[4], record[5], record[6]
                    )
                    wrote_corrupt_m3f_family_light = True
            if (
                not stock_baseline
                and (not glass_disabled or leak_glass_disabled)
                and expected_path == "custom-m3c"
            ):
                glass_triangles = [
                    triangle
                    for face in anchor["expected_glass_faces"]
                    for triangle in _glass_face_triangles(face, material_indexes)
                ]
                glass_triangles = [
                    (
                        material,
                        tuple(
                            (x + point[0], y + point[1], z + point[2])
                            for point in triangle
                        ),
                        rgb,
                        blocklight,
                        sunlight,
                        ao,
                        uvs,
                    )
                    for material, triangle, rgb, blocklight, sunlight, ao, uvs in glass_triangles
                ]
                glass_blocklight = (
                    15 if anchor["block_id"] == "ae2:quartz_vibrant_glass" else 0
                )
                glass_triangles = [
                    (material, triangle, rgb, glass_blocklight, sunlight, ao, uvs)
                    for material, triangle, rgb, _blocklight, sunlight, ao, uvs in glass_triangles
                ]
                if (
                    corrupt_vibrant_glass_light
                    and not wrote_corrupt_vibrant_glass_light
                    and anchor["block_id"] == "ae2:quartz_vibrant_glass"
                ):
                    glass_triangles = [
                        (material, triangle, rgb, 14, sunlight, ao, uvs)
                        for material, triangle, rgb, _blocklight, sunlight, ao, uvs
                        in glass_triangles
                    ]
                    wrote_corrupt_vibrant_glass_light = True
                if corrupt_glass_geometry and not wrote_corrupt_glass_geometry:
                    record = glass_triangles[0]
                    material, triangle, rgb, blocklight, sunlight, ao, uvs = record
                    glass_triangles[0] = (
                        material,
                        (
                            (triangle[0][0] + 1 / 64, triangle[0][1], triangle[0][2]),
                            triangle[1],
                            triangle[2],
                        ),
                        rgb,
                        blocklight,
                        sunlight,
                        ao,
                        uvs,
                    )
                    wrote_corrupt_glass_geometry = True
                if corrupt_glass_winding and not wrote_corrupt_glass_winding:
                    record = glass_triangles[0]
                    material, triangle, rgb, blocklight, sunlight, ao, uvs = record
                    glass_triangles[0] = (
                        material,
                        (triangle[0], triangle[2], triangle[1]),
                        rgb,
                        blocklight,
                        sunlight,
                        ao,
                        (uvs[0], uvs[2], uvs[1]),
                    )
                    wrote_corrupt_glass_winding = True
                if corrupt_glass_uv and not wrote_corrupt_glass_uv:
                    record = glass_triangles[0]
                    glass_triangles[0] = (
                        *record[:6],
                        ((record[6][0][0] + 1 / 32, record[6][0][1]), *record[6][1:]),
                    )
                    wrote_corrupt_glass_uv = True
                if corrupt_glass_material and not wrote_corrupt_glass_material:
                    record = glass_triangles[0]
                    glass_triangles[0] = (
                        material_indexes["bluemap:block/missing"],
                        *record[1:],
                    )
                    wrote_corrupt_glass_material = True
                if (
                    corrupt_glass_frame_material
                    and not wrote_corrupt_glass_frame_material
                ):
                    frame_index = next(
                        index
                        for index, record in enumerate(glass_triangles)
                        if "quartz_glass_frame" in textures[record[0]]["resourcePath"]
                    )
                    record = glass_triangles[frame_index]
                    glass_triangles[frame_index] = (
                        material_indexes["ae2:block/glass/quartz_glass_frame0001"],
                        *record[1:],
                    )
                    wrote_corrupt_glass_frame_material = True
                if corrupt_glass_attributes and not wrote_corrupt_glass_attributes:
                    record = glass_triangles[0]
                    glass_triangles[0] = (*record[:5], 254, record[6])
                    wrote_corrupt_glass_attributes = True
                if corrupt_glass_rgb and not wrote_corrupt_glass_rgb:
                    record = glass_triangles[0]
                    glass_triangles[0] = (
                        record[0],
                        record[1],
                        (254, 255, 255),
                        *record[3:],
                    )
                    wrote_corrupt_glass_rgb = True
                if corrupt_glass_face_light and not wrote_corrupt_glass_face_light:
                    record = glass_triangles[0]
                    glass_triangles[0] = (
                        record[0],
                        record[1],
                        record[2],
                        (record[3] + 1) % 16,
                        record[4],
                        record[5],
                        record[6],
                    )
                    wrote_corrupt_glass_face_light = True
                if vary_glass_world_light and not wrote_varied_glass_world_light:
                    first_face_count = anchor["expected_glass_faces"][0][
                        "triangle_count"
                    ]
                    glass_triangles = [
                        (
                            material,
                            triangle,
                            rgb,
                            7 if index < first_face_count else blocklight,
                            sunlight,
                            ao,
                            uvs,
                        )
                        for index, (
                            material,
                            triangle,
                            rgb,
                            blocklight,
                            sunlight,
                            ao,
                            uvs,
                        ) in enumerate(glass_triangles)
                    ]
                    wrote_varied_glass_world_light = True
                if leak_glass_shared_face and not wrote_glass_shared_face:
                    connected = anchor["expected_connections"]
                    if connected:
                        direction = connected[0]["direction"]
                        leak_face = {
                            "direction": direction,
                            "base_resource": anchor["expected_glass_base_selection"]["resource_path"],
                            "base_uv_corners": anchor["expected_glass_base_selection"]["uv_corners"],
                            "frame_resource": None,
                            "frame_uv_corners": None,
                        }
                        leaked = _glass_face_triangles(leak_face, material_indexes)
                        glass_triangles.extend(
                            (
                                material,
                                tuple(
                                    (x + point[0], y + point[1], z + point[2])
                                    for point in triangle
                                ),
                                rgb,
                                blocklight,
                                sunlight,
                                ao,
                                uvs,
                            )
                            for material, triangle, rgb, blocklight, sunlight, ao, uvs in leaked
                        )
                        wrote_glass_shared_face = True
                world_triangles.extend(glass_triangles)
            if (
                not stock_baseline
                and (not crafting_disabled or leak_crafting_disabled)
                and expected_path == "custom-m3d"
            ):
                crafting_triangles = [
                    triangle
                    for face in anchor["expected_crafting_faces"]
                    for triangle in _crafting_face_triangles(
                        face, material_indexes
                    )
                ]
                crafting_triangles = [
                    (
                        material,
                        tuple(
                            (x + point[0], y + point[1], z + point[2])
                            for point in triangle
                        ),
                        rgb,
                        blocklight,
                        sunlight,
                        ao,
                        uvs,
                    )
                    for material, triangle, rgb, blocklight, sunlight, ao, uvs
                    in crafting_triangles
                ]
                if (
                    corrupt_crafting_geometry
                    and crafting_triangles
                    and not wrote_corrupt_crafting_geometry
                ):
                    record = crafting_triangles[0]
                    triangle = record[1]
                    crafting_triangles[0] = (
                        record[0],
                        (
                            (
                                triangle[0][0] + 1 / 64,
                                triangle[0][1],
                                triangle[0][2],
                            ),
                            triangle[1],
                            triangle[2],
                        ),
                        *record[2:],
                    )
                    wrote_corrupt_crafting_geometry = True
                if (
                    corrupt_crafting_winding
                    and crafting_triangles
                    and not wrote_corrupt_crafting_winding
                ):
                    record = crafting_triangles[0]
                    crafting_triangles[0] = (
                        record[0],
                        (record[1][0], record[1][2], record[1][1]),
                        *record[2:6],
                        (record[6][0], record[6][2], record[6][1]),
                    )
                    wrote_corrupt_crafting_winding = True
                if (
                    corrupt_crafting_uv
                    and crafting_triangles
                    and not wrote_corrupt_crafting_uv
                ):
                    record = crafting_triangles[0]
                    crafting_triangles[0] = (
                        *record[:6],
                        (
                            (record[6][0][0] + 1 / 32, record[6][0][1]),
                            *record[6][1:],
                        ),
                    )
                    wrote_corrupt_crafting_uv = True
                if (
                    corrupt_crafting_material
                    and crafting_triangles
                    and not wrote_corrupt_crafting_material
                ):
                    crafting_triangles[0] = (
                        material_indexes["bluemap:block/missing"],
                        *crafting_triangles[0][1:],
                    )
                    wrote_corrupt_crafting_material = True
                if (
                    corrupt_crafting_rgb
                    and crafting_triangles
                    and not wrote_corrupt_crafting_rgb
                ):
                    record = crafting_triangles[0]
                    crafting_triangles[0] = (
                        record[0],
                        record[1],
                        ((record[2][0] + 1) & 0xFF, record[2][1], record[2][2]),
                        *record[3:],
                    )
                    wrote_corrupt_crafting_rgb = True
                if (
                    corrupt_crafting_ao
                    and crafting_triangles
                    and not wrote_corrupt_crafting_ao
                ):
                    record = crafting_triangles[0]
                    crafting_triangles[0] = (*record[:5], 254, record[6])
                    wrote_corrupt_crafting_ao = True
                if corrupt_crafting_light and not wrote_corrupt_crafting_light:
                    for record_index, record in enumerate(crafting_triangles):
                        if record[3] == 15 and record[4] == 15:
                            crafting_triangles[record_index] = (
                                record[0],
                                record[1],
                                record[2],
                                14,
                                record[4],
                                record[5],
                                record[6],
                            )
                            wrote_corrupt_crafting_light = True
                            break
                world_triangles.extend(crafting_triangles)
            if (
                not stock_baseline
                and (not quantum_disabled or leak_quantum_disabled)
                and expected_path == "custom-m3e"
            ):
                quantum_triangles = [
                    triangle
                    for primitive in anchor["expected_quantum_primitives"]
                    for triangle in _quantum_primitive_triangles(
                        primitive, material_indexes
                    )
                ]
                quantum_triangles = [
                    (
                        material,
                        tuple(
                            (x + point[0], y + point[1], z + point[2])
                            for point in triangle
                        ),
                        rgb,
                        blocklight,
                        sunlight,
                        ao,
                        uvs,
                    )
                    for material, triangle, rgb, blocklight, sunlight, ao, uvs
                    in quantum_triangles
                ]
                if corrupt_quantum_geometry and not wrote_corrupt_quantum_geometry:
                    record = quantum_triangles[0]
                    triangle = record[1]
                    quantum_triangles[0] = (
                        record[0],
                        (
                            (
                                triangle[0][0] + 1 / 64,
                                triangle[0][1],
                                triangle[0][2],
                            ),
                            triangle[1],
                            triangle[2],
                        ),
                        *record[2:],
                    )
                    wrote_corrupt_quantum_geometry = True
                if corrupt_quantum_winding and not wrote_corrupt_quantum_winding:
                    record = quantum_triangles[0]
                    quantum_triangles[0] = (
                        record[0],
                        (record[1][0], record[1][2], record[1][1]),
                        *record[2:6],
                        (record[6][0], record[6][2], record[6][1]),
                    )
                    wrote_corrupt_quantum_winding = True
                if corrupt_quantum_uv and not wrote_corrupt_quantum_uv:
                    record = quantum_triangles[0]
                    quantum_triangles[0] = (
                        *record[:6],
                        (
                            (record[6][0][0] + 1 / 32, record[6][0][1]),
                            *record[6][1:],
                        ),
                    )
                    wrote_corrupt_quantum_uv = True
                if corrupt_quantum_material and not wrote_corrupt_quantum_material:
                    quantum_triangles[0] = (
                        material_indexes["bluemap:block/missing"],
                        *quantum_triangles[0][1:],
                    )
                    wrote_corrupt_quantum_material = True
                if corrupt_quantum_rgb and not wrote_corrupt_quantum_rgb:
                    record = quantum_triangles[0]
                    quantum_triangles[0] = (
                        record[0], record[1], (254, 255, 255), *record[3:]
                    )
                    wrote_corrupt_quantum_rgb = True
                if corrupt_quantum_ao and not wrote_corrupt_quantum_ao:
                    record = quantum_triangles[0]
                    quantum_triangles[0] = (*record[:5], 254, record[6])
                    wrote_corrupt_quantum_ao = True
                if corrupt_quantum_light and not wrote_corrupt_quantum_light:
                    record = quantum_triangles[0]
                    quantum_triangles[0] = (
                        record[0],
                        record[1],
                        record[2],
                        1,
                        record[4],
                        record[5],
                        record[6],
                    )
                    wrote_corrupt_quantum_light = True
                world_triangles.extend(quantum_triangles)
            if (
                expected_path == "custom-m3"
                and corrupt_drive_base_ao
                and not wrote_corrupt_drive_base_ao
            ):
                first = world_triangles[0]
                world_triangles[0] = (*first[:5], 254, first[6])
                wrote_corrupt_drive_base_ao = True
            if not stock_baseline and (
                expected_path == "custom-m2"
                or (
                    native_structural_disabled
                    and isinstance(predecessor_projection, dict)
                    and predecessor_projection.get("expected_path") == "custom-m2"
                )
            ):
                terminal_source = (
                    predecessor_projection
                    if native_structural_disabled
                    and isinstance(predecessor_projection, dict)
                    else anchor
                )
                for part in anchor.get("face_parts", []):
                    if part.get("id") != "ae2:terminal" or not isinstance(
                        part.get("spin"), int
                    ):
                        continue
                    spin = part["spin"]
                    if corrupt_terminal_spin and not wrote_corrupt_terminal_spin:
                        spin = (spin + 1) % 4
                        wrote_corrupt_terminal_spin = True
                    for resource_path, layer in terminal_source.get(
                        "expected_terminal_layers", {}
                    ).items():
                        rgb = tuple(layer["rgb_u8"])
                        if (
                            corrupt_terminal_tint
                            and not wrote_corrupt_terminal_tint
                        ):
                            rgb = ((rgb[0] + 1) & 0xFF, rgb[1], rgb[2])
                            wrote_corrupt_terminal_tint = True
                        world_triangles.extend(
                            _terminal_triangles(
                                (x, y, z),
                                part["direction"],
                                spin,
                                material_indexes[resource_path],
                                rgb,
                                15 if layer["emissive"] else 0,
                                15,
                            )
                        )
                for facade in anchor.get("facades", []):
                    direction = facade["direction"]
                    if corrupt_facade_layout and not wrote_corrupt_facade_layout:
                        direction = {
                            "down": "up",
                            "up": "down",
                            "north": "south",
                            "south": "north",
                            "west": "east",
                            "east": "west",
                        }[direction]
                        wrote_corrupt_facade_layout = True
                    world_triangles.extend(
                        _facade_triangles(
                            (x, y, z),
                            direction,
                            material_indexes["minecraft:block/stone"],
                        )
                    )
            if (
                not stock_baseline
                and (not extension_disabled or expected_path != "custom-m3b")
                and expected_path in {"custom-m3", "custom-m3b"}
            ):
                state = anchor["block_state"]
                facing = state["facing"]
                spin = state["spin"]
                if corrupt_drive_orientation and not wrote_corrupt_drive_orientation:
                    spin = (spin + 1) % 4
                    wrote_corrupt_drive_orientation = True
                occupied_slots = [
                    slot
                    for slot, inventory_slot in zip(
                        anchor["expected_drive_models"]["slots"],
                        anchor["inventory"]["slots"],
                        strict=True,
                    )
                    if not inventory_slot.get("empty")
                ]
                for slot in occupied_slots:
                    slot_facing = slot.get("orientation", {}).get("facing", facing)
                    slot_spin = slot.get("orientation", {}).get("spin", spin)
                    slot_material = slot.get(
                        "material", analyze_prbm.DRIVE_CELL_MATERIAL
                    )
                    effective_slot = slot
                    if (
                        corrupt_drive_slot_translation
                        and not wrote_corrupt_drive_slot_translation
                    ):
                        effective_slot = dict(slot)
                        effective_slot["slot_origin"] = dict(slot["slot_origin"])
                        effective_slot["slot_origin"]["x"] += 1 / 16
                        wrote_corrupt_drive_slot_translation = True
                    chassis = _drive_chassis_triangles(
                        (x, y, z),
                        slot_facing,
                        slot_spin,
                        effective_slot,
                        material_indexes[slot_material],
                    )
                    if (
                        corrupt_drive_chassis_uv
                        and not wrote_corrupt_drive_chassis_uv
                    ):
                        first = chassis[0]
                        chassis[0] = (
                            *first[:6],
                            tuple((uv[0] + 1 / 16, uv[1]) for uv in first[6]),
                        )
                        wrote_corrupt_drive_chassis_uv = True
                    world_triangles.extend(chassis)
                    led_ao = 255
                    if (
                        corrupt_drive_led_attributes
                        and not wrote_corrupt_drive_led_attributes
                    ):
                        led_ao = 254
                        wrote_corrupt_drive_led_attributes = True
                    world_triangles.extend(
                        _drive_led_triangles(
                            (x, y, z),
                            slot_facing,
                            slot_spin,
                            effective_slot,
                            material_indexes[analyze_prbm.DRIVE_LED_MATERIAL],
                            ao=led_ao,
                        )
                    )
                if (
                    corrupt_drive_component_pair
                    and case["category"] == "drive-component-insensitivity"
                    and anchor["inventory"]["slots"][0].get("item_stack", {}).get(
                        "components"
                    )
                    and not wrote_corrupt_drive_component_pair
                ):
                    material, triangle, rgb, blocklight, sunlight, ao, uvs = world_triangles[0]
                    world_triangles[0] = (
                        material,
                        tuple(
                            (
                                point[0] + (1 / 64 if corner == 0 else 0),
                                point[1],
                                point[2],
                            )
                            for corner, point in enumerate(triangle)
                        ),
                        rgb,
                        blocklight,
                        sunlight,
                        ao,
                        uvs,
                    )
                    wrote_corrupt_drive_component_pair = True
                if (
                    case.get("milestone") == "M3b"
                    and case["category"]
                    == "extended-drive-component-insensitivity"
                    and anchor["inventory"]["slots"][0].get("item_stack", {}).get(
                        "components"
                    )
                ):
                    if (
                        vary_extended_component_world_light
                        and not wrote_extended_component_world_light
                    ):
                        world_triangles = [
                            (
                                material,
                                triangle,
                                rgb,
                                blocklight,
                                (
                                    0
                                    if not (
                                        material
                                        == material_indexes[
                                            analyze_prbm.DRIVE_LED_MATERIAL
                                        ]
                                        and rgb == (0, 0, 0)
                                        and blocklight == 15
                                        and sunlight == 15
                                    )
                                    else sunlight
                                ),
                                ao,
                                uvs,
                            )
                            for (
                                material,
                                triangle,
                                rgb,
                                blocklight,
                                sunlight,
                                ao,
                                uvs,
                            ) in world_triangles
                        ]
                        wrote_extended_component_world_light = True
                    if (
                        corrupt_extended_component_led_light
                        and not wrote_corrupt_extended_component_led_light
                    ):
                        for index, record in enumerate(world_triangles):
                            (
                                material,
                                triangle,
                                rgb,
                                blocklight,
                                sunlight,
                                ao,
                                uvs,
                            ) = record
                            if (
                                material
                                == material_indexes[analyze_prbm.DRIVE_LED_MATERIAL]
                                and rgb == (0, 0, 0)
                                and blocklight == 15
                                and sunlight == 15
                            ):
                                world_triangles[index] = (
                                    material,
                                    triangle,
                                    rgb,
                                    blocklight,
                                    14,
                                    ao,
                                    uvs,
                                )
                                wrote_corrupt_extended_component_led_light = True
                                break
            if y == 100 and expected_path not in {"custom-m3c", "custom-m3d", "custom-m3e", "custom-m3f", "stock-fallback-m3f", "custom-s1", "stock-fallback-s1"}:
                # Top face of the fixture support. Inward bias owns y=99.
                support_material = material_indexes["minecraft:block/stone"]
                if invalid_unselected_material and not wrote_invalid_unselected:
                    support_material = len(textures) + 11
                    wrote_invalid_unselected = True
                world_triangles.append(
                    (
                        support_material,
                        (
                            (x + 0.1, y, z + 0.1),
                            (x + 0.1, y, z + 0.9),
                            (x + 0.9, y, z + 0.1),
                        ),
                        (255, 255, 255),
                        0,
                        15,
                        255,
                        DEFAULT_UVS,
                    )
                )
            for material, triangle, rgb, blocklight, sunlight, ao, uvs in world_triangles:
                local = tuple(
                    (point[0] - origin[0], point[1], point[2] - origin[1])
                    for point in triangle
                )
                triangles_by_tile.setdefault(tile, []).append(
                    (material, local, rgb, blocklight, sunlight, ao, uvs)
                )

    # The M3c opaque context stone is deliberately present in the PRBM tile,
    # but its owner is not a gallery anchor and therefore must stay unselected.
    for case in cases["cases"]:
        for fixture_block in case.get("fixture_blocks", []):
            if fixture_block.get("block_id") != "minecraft:stone":
                continue
            x, y, z = (
                fixture_block["position"][axis] for axis in ("x", "y", "z")
            )
            tile = (math.floor((x - 2) / 32), math.floor((z - 2) / 32))
            origin = (tile[0] * 32 + 2, tile[1] * 32 + 2)
            local_triangle = (
                (x + 0.2 - origin[0], y + 0.5, z + 0.2 - origin[1]),
                (x + 0.8 - origin[0], y + 0.5, z + 0.2 - origin[1]),
                (x + 0.2 - origin[0], y + 0.5, z + 0.8 - origin[1]),
            )
            triangles_by_tile.setdefault(tile, []).extend(
                (
                    material_indexes["minecraft:block/stone"],
                    local_triangle,
                    (255, 255, 255),
                    0,
                    15,
                    255,
                    DEFAULT_UVS,
                )
                for _repeat in range(10)
            )

    if include_dense:
        dense = cases["optional_dense_fixture"]
        dense_positions = {
            (x, y, z)
            for bounds in dense["cable_bounds"]
            for x in range(bounds["min"][0], bounds["max"][0] + 1)
            for y in range(bounds["min"][1], bounds["max"][1] + 1)
            for z in range(bounds["min"][2], bounds["max"][2] + 1)
        }
        core_material = material_indexes[
            "ae2:part/cable/core/dense_smart/transparent"
        ]
        connection_material = material_indexes[
            "ae2:part/cable/dense_covered/transparent"
        ]
        for x, y, z in sorted(dense_positions):
            degree = sum(
                (x + dx, y + dy, z + dz) in dense_positions
                for dx, dy, dz in (
                    (-1, 0, 0),
                    (1, 0, 0),
                    (0, -1, 0),
                    (0, 1, 0),
                    (0, 0, -1),
                    (0, 0, 1),
                )
            )
            tile = (math.floor((x - 2) / 32), math.floor((z - 2) / 32))
            origin = (tile[0] * 32 + 2, tile[1] * 32 + 2)
            triangle = (
                (x + 0.2 - origin[0], y + 0.5, z + 0.2 - origin[1]),
                (x + 0.8 - origin[0], y + 0.5, z + 0.2 - origin[1]),
                (x + 0.2 - origin[0], y + 0.5, z + 0.8 - origin[1]),
            )
            triangles_by_tile.setdefault(tile, []).extend(
                (core_material, triangle, (255, 255, 255), 0, 15, 255, DEFAULT_UVS)
                for _repeat in range(12)
            )
            triangles_by_tile[tile].extend(
                (
                    connection_material,
                    triangle,
                    (255, 255, 255),
                    0,
                    15,
                    255,
                    DEFAULT_UVS,
                )
                for _repeat in range(10 * degree)
            )

    if (
        leak_m45_projection_kind is not None
        and not wrote_m45_projection_material_leak
    ):
        raise AssertionError("requested M4/M5 projection leak was not emitted")
    if (
        corrupt_m45_singleton_materials
        and not wrote_corrupt_m45_singleton_materials
    ):
        raise AssertionError("requested Advanced singleton mutation was not emitted")
    if leak_m45_missing_elsewhere and not wrote_m45_missing_elsewhere:
        raise AssertionError("requested M4/M5 missing-material leak was not emitted")
    if (
        corrupt_m45_extended_plane_observation
        and not wrote_corrupt_m45_extended_plane_observation
    ):
        raise AssertionError("requested Extended plane mutation was not emitted")

    for tile, triangles in triangles_by_tile.items():
        path = analyze_prbm.tile_path(map_root, tile[0], tile[1])
        _write_gzip(path, encode_prbm(triangles))


class AnalyzerTest(unittest.TestCase):
    def setUp(self) -> None:
        global CASES_PATH
        self.temporary = tempfile.TemporaryDirectory()
        self.addCleanup(self.temporary.cleanup)
        self.schema12_cases_path = PROJECT_CASES_PATH
        self.schema11_cases_path = PROJECT_CASES_PATH
        manifest = json.loads(PROJECT_CASES_PATH.read_text(encoding="utf-8"))
        if manifest.get("schema_version") == 12:
            schema11 = analyze_prbm._schema11_view(manifest)
            self.schema11_cases_path = (
                Path(self.temporary.name) / "accepted-schema11-cases.json"
            )
            self.schema11_cases_path.write_text(
                analyze_prbm.canonical_json(schema11, pretty=True),
                encoding="utf-8",
            )
            manifest = schema11
        if manifest.get("schema_version") == 11:
            schema10 = analyze_prbm._schema10_view(manifest)
            self.schema10_cases_path = (
                Path(self.temporary.name) / "accepted-schema10-cases.json"
            )
            self.schema10_cases_path.write_text(
                analyze_prbm.canonical_json(schema10, pretty=True),
                encoding="utf-8",
            )
            manifest = schema10
        else:
            self.schema10_cases_path = PROJECT_CASES_PATH
        if manifest.get("schema_version") == 10:
            manifest = analyze_prbm._schema9_view(manifest)
        self.cases_path = Path(self.temporary.name) / "accepted-schema9-cases.json"
        self.cases_path.write_text(
            json.dumps(manifest, sort_keys=True, separators=(",", ":")) + "\n",
            encoding="utf-8",
        )
        previous_cases_path = CASES_PATH
        CASES_PATH = self.cases_path
        self.addCleanup(self._restore_cases_path, previous_cases_path)
        self.map_root = Path(self.temporary.name) / "map"
        build_fixture(self.map_root)
        m45_runtime_patcher = mock.patch.object(
            analyze_prbm,
            "_validate_m45_runtime_oracle",
            side_effect=_synthetic_m45_runtime_validator,
        )
        m45_runtime_patcher.start()
        self.addCleanup(m45_runtime_patcher.stop)

    def test_schema12_narrow_appmek_manifest_and_projection_modes_are_exact(
        self,
    ) -> None:
        gallery, evidence = analyze_prbm.parse_cases(self.schema12_cases_path)
        self.assertEqual(12, gallery.schema_version)
        self.assertEqual(158, len(gallery.cases))
        self.assertEqual(7, len(gallery.appmek_anchors))
        self.assertEqual(4, len(gallery.appmek_route_positions))
        self.assertEqual(162, evidence["case_count"])
        self.assertEqual(1_373, evidence["anchor_count"])
        self.assertEqual(
            analyze_prbm.SCHEMA11_CANONICAL_SHA256,
            evidence["frozen_schema11_view_sha256"],
        )
        self.assertEqual(
            [250, 106, 106, 106, 74, 74, 18],
            [anchor.expected_triangle_count for anchor in gallery.appmek_anchors],
        )
        self.assertEqual(
            [60, 6, 6, 6, 0, 0, 0],
            [
                dict(anchor.expected_material_triangles).get(
                    analyze_prbm.APPMEK_DRIVE_TEXTURE,
                    0,
                )
                for anchor in gallery.appmek_anchors
            ],
        )
        mode_counts = {}
        for mode, kwargs in {
            "enabled": {},
            "stock": {"stock_baseline": True},
            "native-structural": {"native_structural_disabled": True},
            "appmek-drive": {"appmek_drive_disabled": True},
        }.items():
            projections = [
                analyze_prbm._appmek_mode_projection(
                    anchor,
                    stock_baseline=kwargs.get("stock_baseline", False),
                    native_structural_disabled=kwargs.get(
                        "native_structural_disabled",
                        False,
                    ),
                    appmek_drive_disabled=kwargs.get(
                        "appmek_drive_disabled",
                        False,
                    ),
                )
                for anchor in gallery.appmek_anchors
            ]
            nonempty = sum(
                projection is None or projection.review_projection == "nonempty"
                for projection in projections
            )
            mode_counts[mode] = (nonempty, 7 - nonempty)
        self.assertEqual(
            {
                "enabled": (7, 0),
                "stock": (1, 6),
                "native-structural": (5, 2),
                "appmek-drive": (3, 4),
            },
            mode_counts,
        )

        parsed = analyze_prbm.parse_args(
            ["--map-root", str(self.map_root), "--appmek-drive-disabled"]
        )
        self.assertTrue(parsed.appmek_drive_disabled)

    def test_schema12_manifest_mutations_fail_closed_before_live_oracle(self) -> None:
        manifest = json.loads(
            self.schema12_cases_path.read_text(encoding="utf-8")
        )
        mutations = []
        for mutator in (
            lambda value: value["cases"][-1]["anchors"][0].__setitem__(
                "block_id", "minecraft:stone"
            ),
            lambda value: value["cases"][-2]["anchors"][0]["position"].__setitem__(
                "x", 999
            ),
            lambda value: value["cases"][-4]["anchors"][0].__setitem__(
                "expected_triangle_count", 249
            ),
            lambda value: value["profile"]["appmek_routes"][0].__setitem__(
                "route", "future-route"
            ),
        ):
            value = copy.deepcopy(manifest)
            mutator(value)
            mutations.append(value)
        for value in mutations:
            payload = analyze_prbm.canonical_json(value, pretty=True).encode("utf-8")
            with self.assertRaises(analyze_prbm.EvidenceError):
                analyze_prbm._parse_schema12_cases(
                    value,
                    hashlib.sha256(payload).hexdigest(),
                )

    @staticmethod
    def _restore_cases_path(path: Path) -> None:
        global CASES_PATH
        CASES_PATH = path

    def test_schema11_m45_contract_freezes_routes_dependencies_and_athena_keys(
        self,
    ) -> None:
        gallery, evidence = analyze_prbm.parse_cases(self.schema11_cases_path)
        self.assertEqual(11, gallery.schema_version)
        self.assertEqual(158, len(gallery.cases))
        self.assertEqual(409, len(gallery.m45_positions))
        self.assertEqual(
            [11, 36, 118, 107, 44, 9, 42, 42],
            [len(positions) for _route, positions in gallery.m45_route_positions],
        )
        summary = evidence["m45_review_summary"]
        self.assertEqual(
            (
                221_769,
                analyze_prbm.M45_RUNTIME_ORACLE_SHA256,
                391,
                23_334,
                122,
                2_089,
                "exact-runtime-map-geometry-material-nonlighting-v11",
            ),
            (
                summary["runtime_oracle_size_bytes"],
                summary["runtime_oracle_sha256"],
                summary["runtime_oracle_anchor_count"],
                summary["runtime_oracle_triangle_count"],
                summary["runtime_oracle_identity_count"],
                summary["runtime_oracle_material_row_count"],
                summary["review_oracle_policy"],
            ),
        )
        self.assertEqual(
            {"nonempty_anchor_count": 110, "empty_anchor_count": 299},
            summary["physical_stock_projection"],
        )
        self.assertEqual(0, summary["source_derived_synthetic_anchor_count"])
        self.assertEqual(
            189,
            summary["native_structural_disabled_projection"][
                "affected_anchor_count"
            ],
        )
        self.assertEqual(
            20,
            summary["crafting_disabled_projection"]["affected_anchor_count"],
        )

        anchors = {
            anchor.position: anchor
            for case in gallery.cases
            for anchor in case.anchors
            if anchor.m45 is not None
        }
        expanded = anchors[(464, 100, 312)]
        megacells = anchors[(465, 100, 312)]
        self.assertEqual(
            "inactive-megacells-peer-owner-selects-exact-empty-formed-model",
            dict(expanded.m45.route_disabled_projections)["megacells"].reason,
        )
        self.assertEqual(
            "inactive-expandedae-peer-owner-selects-exact-empty-formed-model",
            dict(megacells.m45.route_disabled_projections)["expandedae"].reason,
        )
        self.assertEqual(
            "empty",
            dict(expanded.m45.route_disabled_projections)[
                "megacells"
            ].review_projection,
        )
        self.assertEqual(
            "empty",
            dict(megacells.m45.route_disabled_projections)[
                "expandedae"
            ].review_projection,
        )
        self.assertIsNotNone(expanded.m45.crafting_disabled_projection)
        self.assertIsNotNone(megacells.m45.crafting_disabled_projection)

        athena = next(
            route
            for route in evidence["m45_routes"]
            if route["route"] == "advanced-ae-athena"
        )
        self.assertEqual(5, len(athena["route_resources"]))
        self.assertTrue(
            all(
                resource.startswith(
                    "bluemap_ae2:m45/athena-frame-zero/block/"
                )
                for resource in athena["route_resources"]
            )
        )
        self.assertTrue(
            all(
                resource.startswith("advanced_ae:block/quantum_alloy_block")
                for resource in athena["source_resources"]
            )
        )
        self.assertTrue(
            set(athena["source_resources"]).isdisjoint(
                athena["material_allowlist"]
            )
        )
        routes = {route["route"]: route for route in evidence["m45_routes"]}
        matrix = routes["extendedae-matrix"]
        planes = routes["extendedae-planes"]
        self.assertEqual(
            list(analyze_prbm.M45_EXTENDED_MATRIX_RESOURCES),
            matrix["route_resources"],
        )
        self.assertEqual([], matrix["dependency_resources"])
        self.assertEqual([], matrix["host_resources"])
        self.assertEqual(
            set(analyze_prbm.M45_EXTENDED_MATRIX_RESOURCES),
            set(matrix["material_allowlist"]),
        )
        self.assertEqual(
            list(analyze_prbm.M45_EXTENDED_PLANE_RESOURCES),
            planes["route_resources"],
        )
        self.assertEqual(
            list(analyze_prbm.M45_EXTENDED_PLANE_DEPENDENCY_RESOURCES),
            planes["dependency_resources"],
        )
        self.assertEqual(
            list(analyze_prbm.M45_EXTENDED_PLANE_HOST_RESOURCES),
            planes["host_resources"],
        )
        self.assertEqual(
            set(analyze_prbm.M45_EXTENDED_PLANE_RESOURCES)
            | set(analyze_prbm.M45_EXTENDED_PLANE_DEPENDENCY_RESOURCES)
            | set(analyze_prbm.M45_EXTENDED_PLANE_HOST_RESOURCES),
            set(planes["material_allowlist"]),
        )
        self.assertTrue(
            set(planes["host_resources"])
            <= set(
                json.loads(
                    self.schema10_cases_path.read_text(encoding="utf-8")
                )["profile"]["selected_resources"]
            )
        )
        self.assertTrue(
            all(
                route["host_resources"] == []
                for route in evidence["m45_routes"]
                if route["route"] != "extendedae-planes"
            )
        )
        self.assertTrue(
            set(matrix["route_resources"]).isdisjoint(planes["route_resources"])
        )

        manifest = json.loads(
            self.schema11_cases_path.read_text(encoding="utf-8")
        )
        host_mutations = []
        for mutation in ("missing", "reordered", "overlap", "other-route"):
            value = json.loads(json.dumps(manifest))
            profiles = {
                row["route"]: row for row in value["profile"]["m45_routes"]
            }
            plane = profiles["extendedae-planes"]
            if mutation == "missing":
                host = plane["host_resources"].pop()
                plane["material_allowlist"].remove(host)
            elif mutation == "reordered":
                plane["host_resources"][0], plane["host_resources"][1] = (
                    plane["host_resources"][1],
                    plane["host_resources"][0],
                )
            elif mutation == "overlap":
                plane["host_resources"][0] = plane["route_resources"][0]
                plane["host_resources"].sort()
            else:
                profiles["extendedae-matrix"]["host_resources"] = [
                    analyze_prbm.M45_EXTENDED_PLANE_HOST_RESOURCES[0]
                ]
            host_mutations.append(value)
        for value in host_mutations:
            payload = analyze_prbm.canonical_json(value, pretty=True).encode(
                "utf-8"
            )
            with self.assertRaises(analyze_prbm.EvidenceError):
                analyze_prbm._parse_schema11_cases(
                    value, hashlib.sha256(payload).hexdigest()
                )

    def test_schema11_m45_runtime_oracle_identity_and_closure_are_exact(
        self,
    ) -> None:
        manifest = json.loads(
            self.schema11_cases_path.read_text(encoding="utf-8")
        )
        raw_cases = [
            case
            for case in manifest["cases"]
            if str(case.get("case_id", "")).startswith("ae2-m45-")
        ]
        expected_by_route = {
            case["route"]: {
                tuple(anchor["position"][axis] for axis in ("x", "y", "z"))
                for anchor in case["anchors"]
                if anchor["expected_path"] == "custom-m45"
            }
            for case in raw_cases
        }
        main = analyze_prbm._read_exact_m45_runtime_oracle(
            expected_by_route
        )
        legacy = analyze_prbm._read_exact_m45_schema10_legacy_oracle()
        self.assertEqual(391, len(main))
        self.assertEqual(23_334, sum(row["triangle_count"] for row in main.values()))
        self.assertEqual(
            122,
            len(
                {
                    resource
                    for row in main.values()
                    for resource in row["material_triangles"]
                }
            ),
        )
        self.assertEqual(2_089, sum(len(row["material_triangles"]) for row in main.values()))
        self.assertEqual(3, len(legacy))
        self.assertEqual(282, sum(row["triangle_count"] for row in legacy.values()))
        self.assertEqual(
            {
                (241, 100, 249),
                (266, 100, 266),
                (318, 100, 261),
            },
            set(legacy),
        )
        for path, size, digest in (
            (
                PROJECT_ROOT / "gallery/m45-runtime-oracle.json",
                221_769,
                analyze_prbm.M45_RUNTIME_ORACLE_SHA256,
            ),
            (
                PROJECT_ROOT / "gallery/m45-schema10-legacy-oracle.json",
                2_336,
                analyze_prbm.M45_SCHEMA10_LEGACY_ORACLE_SHA256,
            ),
        ):
            payload = path.read_bytes()
            self.assertEqual(size, len(payload))
            self.assertEqual(digest, hashlib.sha256(payload).hexdigest())

        namespace = _full_generator_namespace()
        self.assertEqual(
            main,
            namespace["load_m45_runtime_oracle"](
                namespace["M45_CASES"]
            ),
        )
        self.assertEqual(
            legacy,
            namespace["load_m45_schema10_legacy_oracle"](),
        )

    def test_schema11_m45_runtime_oracle_parser_is_fail_closed(self) -> None:
        manifest = json.loads(
            self.schema11_cases_path.read_text(encoding="utf-8")
        )
        expected_by_route = {
            case["route"]: {
                tuple(anchor["position"][axis] for axis in ("x", "y", "z"))
                for anchor in case["anchors"]
                if anchor["expected_path"] == "custom-m45"
            }
            for case in manifest["cases"]
            if str(case.get("case_id", "")).startswith("ae2-m45-")
        }
        original = json.loads(
            (PROJECT_ROOT / "gallery/m45-runtime-oracle.json").read_text(
                encoding="utf-8"
            )
        )
        first_key = next(iter(original["anchors"]))
        mutations: list[tuple[str, dict[str, object]]] = []
        value = copy.deepcopy(original)
        value["route_ids"].reverse()
        mutations.append(("route-order", value))
        value = copy.deepcopy(original)
        value["anchors"].pop(first_key)
        mutations.append(("missing-selector", value))
        value = copy.deepcopy(original)
        entry = value["anchors"].pop(first_key)
        value["anchors"]["0" + first_key] = entry
        mutations.append(("noncanonical-selector", value))
        value = copy.deepcopy(original)
        value["anchors"][first_key]["triangle_count"] = True
        mutations.append(("boolean-count", value))
        value = copy.deepcopy(original)
        entry = value["anchors"][first_key]
        entry["attribute_signature"] = entry.pop(
            "nonlighting_attribute_signature"
        )
        mutations.append(("wrong-signature-field", value))
        value = copy.deepcopy(original)
        material = next(iter(value["anchors"][first_key]["material_triangles"]))
        value["anchors"][first_key]["material_triangles"][material] += 1
        mutations.append(("material-total", value))

        for label, value in mutations:
            with self.subTest(label=label):
                payload = analyze_prbm.canonical_json(
                    value, pretty=True
                ).encode("utf-8")
                path = Path(self.temporary.name) / f"{label}.json"
                path.write_bytes(payload)
                with mock.patch.multiple(
                    analyze_prbm,
                    M45_RUNTIME_ORACLE_PATH=path,
                    M45_RUNTIME_ORACLE_SIZE_BYTES=len(payload),
                    M45_RUNTIME_ORACLE_SHA256=hashlib.sha256(
                        payload
                    ).hexdigest(),
                ):
                    with self.assertRaises(analyze_prbm.EvidenceError):
                        analyze_prbm._read_exact_m45_runtime_oracle(
                            expected_by_route
                        )

    def test_schema11_m45_manifest_oracle_fields_are_exact(self) -> None:
        manifest = json.loads(
            self.schema11_cases_path.read_text(encoding="utf-8")
        )

        def changed() -> dict[str, object]:
            return copy.deepcopy(manifest)

        mutations: list[tuple[str, dict[str, object]]] = []
        value = changed()
        custom = next(
            anchor
            for case in value["cases"]
            for anchor in case["anchors"]
            if anchor.get("expected_path") == "custom-m45"
        )
        custom.pop("expected_nonlighting_attribute_signature")
        mutations.append(("missing-nonlighting", value))
        value = changed()
        custom = next(
            anchor
            for case in value["cases"]
            for anchor in case["anchors"]
            if anchor.get("expected_path") == "custom-m45"
        )
        custom["expected_triangle_count"] += 1
        mutations.append(("count", value))
        value = changed()
        fallback = next(
            anchor
            for case in value["cases"]
            for anchor in case["anchors"]
            if anchor.get("expected_path") == "stock-fallback-m45"
        )
        fallback["expected_geometry_signature"] = "0" * 64
        mutations.append(("fallback-field", value))
        value = changed()
        value["m45_review_summary"]["runtime_oracle_sha256"] = "0" * 64
        mutations.append(("summary-identity", value))

        for label, value in mutations:
            with self.subTest(label=label):
                payload = analyze_prbm.canonical_json(
                    value, pretty=True
                ).encode("utf-8")
                with self.assertRaises(analyze_prbm.EvidenceError):
                    analyze_prbm._parse_schema11_cases(
                        value, hashlib.sha256(payload).hexdigest()
                    )

    def test_schema11_m45_runtime_oracle_excludes_only_lighting(
        self,
    ) -> None:
        baseline = _m45_signature_result()
        expected_materials = (("ae2:block/generics/side", 1),)

        def validate(value: dict[str, object]) -> dict[str, object]:
            return EXACT_M45_RUNTIME_VALIDATOR(
                (0, 0, 0),
                value["records"],
                value,
                expected_triangle_count=1,
                expected_material_triangles=expected_materials,
                expected_geometry_signature=baseline["geometry_signature"],
                expected_nonlighting_attribute_signature=baseline[
                    "nonlighting_attribute_signature"
                ],
                description="M4/M5 mutation fixture",
            )

        validated = validate(baseline)
        self.assertEqual(
            baseline["attribute_signature"],
            validated["observed_full_attribute_signature"],
        )
        mutations = {
            "geometry": _m45_signature_result(
                positions=((0.25, 0.5, 0.2), (0.8, 0.5, 0.2), (0.2, 0.5, 0.8))
            ),
            "uv": _m45_signature_result(
                uvs=((0.25, 0.0), (1.0, 0.0), (0.0, 1.0))
            ),
            "material": _m45_signature_result(resource="ae2:block/generics/top"),
            "normal": _m45_signature_result(mutate_normal=True),
            "color": _m45_signature_result(rgb=(254, 255, 255)),
            "ao": _m45_signature_result(ao=254),
        }
        for label, value in mutations.items():
            with self.subTest(label=label):
                with self.assertRaises(analyze_prbm.EvidenceError):
                    validate(value)

        varied_light = _m45_signature_result(
            blocklight=7,
            sunlight=11,
        )
        self.assertEqual(
            baseline["nonlighting_attribute_signature"],
            varied_light["nonlighting_attribute_signature"],
        )
        self.assertNotEqual(
            baseline["attribute_signature"],
            varied_light["attribute_signature"],
        )
        self.assertEqual(
            varied_light["attribute_signature"],
            validate(varied_light)["observed_full_attribute_signature"],
        )
        for label, value in (
            ("nonflat", _m45_signature_result(blocklights=(0, 1, 0))),
            ("out-of-range", _m45_signature_result(sunlights=(16, 16, 16))),
        ):
            with self.subTest(label=label):
                with self.assertRaises(analyze_prbm.EvidenceError):
                    validate(value)

    def test_schema11_active_legacy_upgrade_uses_v11_signature_scope(
        self,
    ) -> None:
        target = (318, 100, 261)
        root = Path(self.temporary.name) / "schema11-legacy-v11-scope"
        build_fixture(root, cases_path=self.schema11_cases_path)
        observed: list[tuple[int, int, int]] = []

        def validate(
            position: tuple[int, int, int],
            records: tuple[analyze_prbm.TriangleRecord, ...]
            | list[analyze_prbm.TriangleRecord],
            result: dict[str, object],
            **expected: object,
        ) -> dict[str, object]:
            if position == target:
                v11 = analyze_prbm._records_result(
                    records, "anchor-v11:318,100,261"
                )
                v7 = analyze_prbm._records_result(
                    records, "anchor-v7:318,100,261"
                )
                self.assertEqual(
                    v11["geometry_signature"], result["geometry_signature"]
                )
                self.assertNotEqual(
                    v7["geometry_signature"], result["geometry_signature"]
                )
                observed.append(position)
            return _synthetic_m45_runtime_validator(
                position, records, result, **expected
            )

        with mock.patch.object(
            analyze_prbm,
            "_validate_m45_runtime_oracle",
            side_effect=validate,
        ):
            analyze_prbm.analyze(root, self.schema11_cases_path)
        self.assertEqual([target], observed)

    def test_schema11_m45_legacy_upgrades_are_exact_and_nonmutating(
        self,
    ) -> None:
        gallery, evidence = analyze_prbm.parse_cases(self.schema11_cases_path)
        self.assertEqual(
            (
                (241, 100, 249),
                (266, 100, 266),
                (318, 100, 261),
            ),
            gallery.m45_legacy_upgrade_positions,
        )
        upgrades = {
            anchor.position: anchor
            for case in gallery.cases
            for anchor in case.anchors
            if anchor.m45_legacy_upgrade is not None
        }
        self.assertEqual(
            {
                (241, 100, 249): "stock-fallback-m3",
                (266, 100, 266): "stock-fallback-m3b",
                (318, 100, 261): "stock-fallback-m3d",
            },
            {
                position: anchor.expected_path
                for position, anchor in upgrades.items()
            },
        )
        self.assertEqual(
            {
                "anchor_count": 3,
                "custom_anchor_count": 3,
                "custom_triangle_count": 282,
                "selected_resource_count": 20,
                "material_row_count": 26,
                "m45_route_dependency_anchor_counts": {
                    "expandedae": 1,
                    "megacells": 3,
                },
                "legacy_route_dependency_anchor_counts": {
                    "extension": 1,
                    "crafting": 1,
                },
                "predecessor_projection": {
                    "empty_anchor_count": 3,
                    "triangle_count": 0,
                    "resource_count": 0,
                    "resources": [],
                },
                "physical_stock_projection": {
                    "empty_anchor_count": 3,
                    "triangle_count": 0,
                    "resource_count": 0,
                    "resources": [],
                },
            },
            evidence["m45_legacy_upgrades"]["summary"],
        )
        self.assertEqual(
            {
                "path": "m45-schema10-legacy-oracle.json",
                "size_bytes": 2_336,
                "sha256": analyze_prbm.M45_SCHEMA10_LEGACY_ORACLE_SHA256,
                "schema_version": 2,
                "signature_schema_version": 11,
                "anchor_count": 3,
                "triangle_count": 282,
                "identity_count": 20,
                "material_row_count": 26,
            },
            evidence["m45_legacy_upgrades"]["oracle"],
        )
        routes = {row["route"]: row for row in evidence["m45_routes"]}
        self.assertEqual(3, routes["megacells"]["legacy_upgrade_dependency_anchor_count"])
        self.assertEqual(1, routes["expandedae"]["legacy_upgrade_dependency_anchor_count"])
        self.assertTrue(
            all(
                row["legacy_upgrade_dependency_anchor_count"] == 0
                for route, row in routes.items()
                if route not in {"megacells", "expandedae"}
            )
        )

        manifest = json.loads(
            self.schema11_cases_path.read_text(encoding="utf-8")
        )
        schema10_payload = analyze_prbm.canonical_json(
            analyze_prbm._schema10_view(manifest), pretty=True
        ).encode("utf-8")
        self.assertEqual(4_207_895, len(schema10_payload))
        self.assertEqual(
            analyze_prbm.SCHEMA10_CANONICAL_SHA256,
            hashlib.sha256(schema10_payload).hexdigest(),
        )

        namespace = _full_generator_namespace()
        tsv = namespace["cases_tsv"]().decode("utf-8").splitlines()
        header = tsv[0].split("\t")
        source_path_index = header.index("expected_path")
        effective_path_index = header.index("effective_overlay_path")
        overlay_rows = {
            (row[0], row[header.index("anchor")]): (
                row[source_path_index],
                row[effective_path_index],
            )
            for line in tsv[1:]
            if (row := line.split("\t"))[effective_path_index]
            == "custom-m45-legacy-upgrade"
        }
        self.assertEqual(
            {
                ("ae2-m3-14", "241 100 249"): (
                    "stock-fallback-m3",
                    "custom-m45-legacy-upgrade",
                ),
                ("ae2-m3b-13", "266 100 266"): (
                    "stock-fallback-m3b",
                    "custom-m45-legacy-upgrade",
                ),
                ("ae2-m3d-09", "318 100 261"): (
                    "stock-fallback-m3d",
                    "custom-m45-legacy-upgrade",
                ),
            },
            overlay_rows,
        )
        outputs = namespace["expected_outputs"]()
        build = outputs[
            Path("datapack/data/ae2_m3/function/build.mcfunction")
        ]
        verify = outputs[
            Path("datapack/data/ae2_m3/function/verify.mcfunction")
        ]
        self.assertEqual(272_646, len(build))
        self.assertEqual(
            "a0f8061bef43704866a38ecf8f508dbe6318b26f8c9ea17f2539ba59fb16d557",
            hashlib.sha256(build).hexdigest(),
        )
        self.assertEqual(1_008_964, len(verify))
        self.assertEqual(
            "799bd217357b7ebf58a16792b540e6cc1d3dd72efad9808fc3980d063d4b3133",
            hashlib.sha256(verify).hexdigest(),
        )
        datapack_digest = hashlib.sha256()
        for path, payload in sorted(
            (
                (path, payload)
                for path, payload in outputs.items()
                if path.parts and path.parts[0] == "datapack"
            ),
            key=lambda row: row[0].as_posix(),
        ):
            datapack_digest.update(
                Path(*path.parts[1:]).as_posix().encode("utf-8")
            )
            datapack_digest.update(b"\0")
            datapack_digest.update(payload)
            datapack_digest.update(b"\0")
        self.assertEqual(
            "3391d9fc02c18f54a58d2e0f6758a8f03ad75d506e67aea5afd14fb927b0e72f",
            datapack_digest.hexdigest(),
        )

        def changed() -> dict[str, object]:
            return json.loads(json.dumps(manifest))

        mutations: list[tuple[str, dict[str, object]]] = []
        value = changed()
        value["m45_legacy_upgrades"]["rows"].pop()
        mutations.append(("missing", value))
        value = changed()
        rows = value["m45_legacy_upgrades"]["rows"]
        rows[0], rows[1] = rows[1], rows[0]
        mutations.append(("reordered", value))
        value = changed()
        rows = value["m45_legacy_upgrades"]["rows"]
        rows[1] = json.loads(json.dumps(rows[0]))
        mutations.append(("duplicate", value))
        for label, field, bad_value in (
            ("case", "case_id", "wrong"),
            ("source-kind", "source_kind", "wrong"),
            ("required-route", "required_m45_routes", ["megacells", "appflux"]),
            ("required-legacy", "required_legacy_routes", ["crafting"]),
        ):
            value = changed()
            value["m45_legacy_upgrades"]["rows"][0][field] = bad_value
            mutations.append((label, value))
        value = changed()
        value["m45_legacy_upgrades"]["rows"][0]["position"]["x"] += 1
        mutations.append(("position", value))
        value = changed()
        value["m45_legacy_upgrades"]["rows"][0]["enabled"][
            "allowed_resources"
        ].pop()
        mutations.append(("allowlist", value))
        value = changed()
        value["m45_legacy_upgrades"]["rows"][0]["enabled"][
            "expected_triangle_count"
        ] += 1
        mutations.append(("enabled-oracle-count", value))
        value = changed()
        value["m45_legacy_upgrades"]["rows"][0]["enabled"][
            "expected_geometry_signature"
        ] = "0" * 64
        mutations.append(("enabled-oracle-geometry", value))
        value = changed()
        value["m45_legacy_upgrades"]["oracle"]["sha256"] = "0" * 64
        mutations.append(("oracle-metadata", value))
        value = changed()
        value["m45_legacy_upgrades"]["rows"][0]["live_observation"][
            "triangle_count"
        ] += 1
        mutations.append(("observed-count", value))
        value = changed()
        value["m45_legacy_upgrades"]["rows"][0]["live_observation"][
            "material_triangles"
        ]["megacells:block/drive/cells/standard_cell"] = 5
        mutations.append(("observed-material", value))
        value = changed()
        value["m45_legacy_upgrades"]["rows"][0]["predecessor_projection"][
            "reason"
        ] = "wrong"
        mutations.append(("predecessor", value))
        value = changed()
        value["m45_legacy_upgrades"]["rows"][0]["physical_stock_projection"][
            "allowed_resources"
        ] = ["minecraft:block/stone"]
        value["m45_legacy_upgrades"]["rows"][0]["physical_stock_projection"][
            "review_projection"
        ] = "nonempty"
        mutations.append(("stock", value))
        value = changed()
        source = next(
            case for case in value["cases"] if case["case_id"] == "ae2-m3-14"
        )
        source["anchors"][0]["inventory"]["slots"][0]["item_stack"]["count"] = 2
        mutations.append(("source-nbt", value))
        value = changed()
        source = next(
            case for case in value["cases"] if case["case_id"] == "ae2-m3d-09"
        )
        source["anchors"][0]["compatible_neighbor_block_ids"].reverse()
        mutations.append(("source-peer-order", value))
        value = changed()
        megacells = next(
            route
            for route in value["profile"]["m45_routes"]
            if route["route"] == "megacells"
        )
        megacells["material_allowlist"].remove("ae2:block/drive/drive_front")
        mutations.append(("route-allowlist", value))

        for label, mutation in mutations:
            with self.subTest(label=label):
                payload = analyze_prbm.canonical_json(
                    mutation, pretty=True
                ).encode("utf-8")
                with self.assertRaises(analyze_prbm.EvidenceError):
                    analyze_prbm._parse_schema11_cases(
                        mutation, hashlib.sha256(payload).hexdigest()
                    )

    def test_schema11_colorable_drive_colors_are_exact_strings_and_commands(
        self,
    ) -> None:
        namespace = _full_generator_namespace()
        cases = namespace["create_m45_cases"]()
        expanded = next(case for case in cases if case["route"] == "expandedae")
        anchors = [
            anchor
            for anchor in expanded["anchors"]
            if anchor["block_id"] == "expandedae:colorable_drive"
        ]
        colors = tuple(anchor["expected_nbt"]["color"] for anchor in anchors)
        self.assertEqual(namespace["M45_COLORABLE_DRIVE_COLORS"], colors)
        self.assertEqual(
            namespace["M45_COLORABLE_DRIVE_POSITIONS"],
            tuple(anchor["position"] for anchor in anchors),
        )
        self.assertTrue(all(isinstance(color, str) and color for color in colors))

        build_lines = set(namespace["m45_build_lines"]())
        settle_lines = set(namespace["m45_verification_lines"]("ae2m3s"))
        verify_lines = set(namespace["m45_verification_lines"]("ae2m3v"))
        settle_payload = namespace["settle_check_function"]().decode("utf-8")
        verify_payload = namespace["verify_function"]().decode("utf-8")
        for anchor, color in zip(anchors, colors, strict=True):
            position = " ".join(map(str, anchor["position"]))
            build = f'data merge block {position} {{color:"{color}"}}'
            settle = (
                f'execute unless data block {position} {{color:"{color}"}} '
                "run scoreboard players add #failures ae2m3s 1"
            )
            verify = (
                f'execute unless data block {position} {{color:"{color}"}} '
                "run scoreboard players add #failures ae2m3v 1"
            )
            self.assertIn(build, build_lines)
            self.assertIn(settle, settle_lines)
            self.assertIn(verify, verify_lines)
            self.assertIn(settle + "\n", settle_payload)
            self.assertIn(verify + "\n", verify_payload)

        colorable_indexes = [
            index
            for index, anchor in enumerate(expanded["anchors"])
            if anchor["block_id"] == "expandedae:colorable_drive"
        ]
        for label, bad_value in (
            ("numeric", 0),
            ("unknown", "AQUA"),
            ("empty", ""),
        ):
            with self.subTest(mutation=label):
                mutated = copy.deepcopy(cases)
                mutated_expanded = next(
                    case for case in mutated if case["route"] == "expandedae"
                )
                mutated_expanded["anchors"][colorable_indexes[0]]["expected_nbt"] = {
                    "color": bad_value
                }
                with self.assertRaisesRegex(
                    ValueError,
                    "persisted color must be a nonempty string|exact AEColor order",
                ):
                    namespace["validate_m45_colorable_drive_fixtures"](mutated)

        reordered = copy.deepcopy(cases)
        reordered_expanded = next(
            case for case in reordered if case["route"] == "expandedae"
        )
        first, second = colorable_indexes[:2]
        reordered_expanded["anchors"][first]["expected_nbt"], reordered_expanded[
            "anchors"
        ][second]["expected_nbt"] = (
            reordered_expanded["anchors"][second]["expected_nbt"],
            reordered_expanded["anchors"][first]["expected_nbt"],
        )
        with self.assertRaisesRegex(ValueError, "exact AEColor order"):
            namespace["validate_m45_colorable_drive_fixtures"](reordered)

    def test_schema11_m45_physical_fixtures_are_exact_and_fail_closed(
        self,
    ) -> None:
        namespace = _full_generator_namespace()
        cases = namespace["create_m45_cases"]()
        namespace["validate_m45_physical_fixtures"](cases)
        by_route = {case["route"]: case for case in cases}
        self.assertEqual(
            [11, 36, 118, 107, 44, 9, 42, 42],
            [len(case["anchors"]) for case in cases],
        )
        self.assertFalse(
            any(
                anchor["source_derived_synthetic_fixture"]
                for case in cases
                for anchor in case["anchors"]
            )
        )

        requester = by_route["merequester"]
        requester_blocks = [
            anchor
            for anchor in requester["anchors"]
            if anchor["block_id"] == "merequester:requester"
        ]
        self.assertEqual(12, len(requester_blocks))
        self.assertTrue(
            all(anchor["block_state"]["active"] == "false" for anchor in requester_blocks)
        )

        expanded = by_route["expandedae"]
        io_anchors = [
            anchor
            for anchor in expanded["anchors"]
            if anchor["block_id"] == "expandedae:exp_io_port"
        ]
        powered_io = [
            anchor
            for anchor in io_anchors
            if anchor["block_state"]["powered"] == "true"
        ]
        energy_helpers = [
            fixture
            for fixture in expanded["fixture_blocks"]
            if fixture.get("purpose") == "powered-exp-io-port-network-helper"
        ]
        self.assertEqual(48, len(io_anchors))
        self.assertEqual(24, len(powered_io))
        self.assertEqual(24, len(energy_helpers))
        self.assertEqual(
            [
                (anchor["position"][0], 99, anchor["position"][2])
                for anchor in powered_io
            ],
            [helper["position"] for helper in energy_helpers],
        )

        advanced = by_route["advanced-ae-quantum"]
        advanced_physical = advanced["anchors"][8:]
        self.assertEqual(36, len(advanced_physical))
        advanced_singleton = next(
            anchor
            for anchor in advanced["anchors"]
            if anchor["position"] == (418, 100, 370)
        )
        self.assertEqual(
            {
                "formed": "true",
                "light_level": "0",
                "multiblocked": "false",
                "powered": "false",
            },
            advanced_singleton["block_state"],
        )
        self.assertEqual(
            namespace["M45_ADVANCED_SINGLETON_MODEL_EXCEPTION"],
            advanced_singleton["selector_scoped_model_exception"],
        )
        self.assertEqual(
            Counter(
                {
                    "advanced_ae:quantum_structure": 34,
                    "advanced_ae:quantum_core": 1,
                    "advanced_ae:quantum_storage_128": 1,
                }
            ),
            Counter(anchor["block_id"] for anchor in advanced_physical),
        )

        extended = by_route["extendedae-matrix"]
        extended_physical = extended["anchors"][6:]
        self.assertEqual(36, len(extended_physical))
        self.assertEqual(
            Counter(
                {
                    "extendedae:assembler_matrix_frame": 24,
                    "extendedae:assembler_matrix_glass": 10,
                    "extendedae:assembler_matrix_pattern": 1,
                    "extendedae:assembler_matrix_crafter": 1,
                }
            ),
            Counter(anchor["block_id"] for anchor in extended_physical),
        )
        self.assertEqual(
            Counter({"block": 8, "column_x": 8, "column_y": 4, "column_z": 4}),
            Counter(
                anchor["block_state"]["shape"]
                for anchor in extended_physical
                if anchor["block_id"] == "extendedae:assembler_matrix_frame"
            ),
        )

        build_lines = set(namespace["m45_build_lines"]())
        settle_lines = set(namespace["m45_verification_lines"]("ae2m3s"))
        verify_lines = set(namespace["m45_verification_lines"]("ae2m3v"))
        build_payload = "\n".join(namespace["m45_build_lines"]())
        clear_payload = namespace["clear_function"]().decode("utf-8")
        self.assertNotIn("merequester:requester[active=true", build_payload)
        for anchor in requester_blocks:
            position = " ".join(map(str, anchor["position"]))
            self.assertFalse(
                any(line.startswith(f"data merge block {position} ") for line in build_lines)
            )
        for helper in energy_helpers:
            position = " ".join(map(str, helper["position"]))
            self.assertIn(
                f"setblock {position} ae2:creative_energy_cell replace",
                build_lines,
            )
            self.assertIn(
                f"execute unless block {position} ae2:creative_energy_cell "
                "run scoreboard players add #failures ae2m3s 1",
                settle_lines,
            )
            self.assertIn(
                f"execute unless block {position} ae2:creative_energy_cell "
                "run scoreboard players add #failures ae2m3v 1",
                verify_lines,
            )
        self.assertIn(
            "fill 424 96 312 467 110 341 minecraft:air replace\n",
            clear_payload,
        )
        for anchor in [*powered_io, *advanced_physical, *extended_physical]:
            position = " ".join(map(str, anchor["position"]))
            block_state = namespace["m45_block_state"](
                anchor["block_id"], anchor["block_state"]
            )
            self.assertIn(f"setblock {position} {block_state} replace", build_lines)
            self.assertIn(
                f"execute unless block {position} {block_state} "
                "run scoreboard players add #failures ae2m3s 1",
                settle_lines,
            )
            self.assertIn(
                f"execute unless block {position} {block_state} "
                "run scoreboard players add #failures ae2m3v 1",
                verify_lines,
            )

        def mutation(route: str) -> tuple[list[dict[str, object]], dict[str, object]]:
            mutated = copy.deepcopy(cases)
            return mutated, next(case for case in mutated if case["route"] == route)

        mutated, case = mutation("merequester")
        case["anchors"][0]["block_state"]["active"] = "true"
        with self.assertRaisesRegex(ValueError, "Requester"):
            namespace["validate_m45_requester_physical_fixtures"](mutated)

        mutated, case = mutation("merequester")
        anchors = case["anchors"]
        case["anchors"] = (anchors[1], anchors[0], *anchors[2:])
        with self.assertRaisesRegex(ValueError, "Requester"):
            namespace["validate_m45_requester_physical_fixtures"](mutated)

        for label, mutate_helper in (
            ("missing", lambda helpers: helpers[1:]),
            (
                "extra",
                lambda helpers: (
                    *helpers,
                    {
                        "position": (424, 99, 312),
                        "block_id": "ae2:creative_energy_cell",
                        "purpose": "powered-exp-io-port-network-helper",
                    },
                ),
            ),
        ):
            with self.subTest(expanded_helper_count=label):
                mutated, case = mutation("expandedae")
                fixtures = tuple(case["fixture_blocks"])
                energy_index = next(
                    index
                    for index, fixture in enumerate(fixtures)
                    if fixture.get("purpose") == "powered-exp-io-port-network-helper"
                )
                prefix = fixtures[:energy_index]
                helpers = fixtures[energy_index:]
                case["fixture_blocks"] = (*prefix, *mutate_helper(helpers))
                with self.assertRaisesRegex(ValueError, "Expanded AE"):
                    namespace["validate_m45_expanded_physical_fixtures"](mutated)

        for label, field, value in (
            ("id", "block_id", "ae2:energy_cell"),
            ("offset", "position", (432, 98, 312)),
        ):
            with self.subTest(expanded_helper=label):
                mutated, case = mutation("expandedae")
                helper = next(
                    fixture
                    for fixture in case["fixture_blocks"]
                    if fixture.get("purpose") == "powered-exp-io-port-network-helper"
                )
                helper[field] = value
                with self.assertRaisesRegex(ValueError, "Expanded AE"):
                    namespace["validate_m45_expanded_physical_fixtures"](mutated)

        mutated, case = mutation("expandedae")
        io = next(
            anchor
            for anchor in case["anchors"]
            if anchor["block_id"] == "expandedae:exp_io_port"
        )
        io["expected_block_entity_id"] = "expandedae:wrong"
        with self.assertRaisesRegex(ValueError, "Expanded AE"):
            namespace["validate_m45_expanded_physical_fixtures"](mutated)

        for key, bad_value in (
            ("formed", "false"),
            ("powered", "true"),
            ("multiblocked", "false"),
            ("light_level", "1"),
        ):
            with self.subTest(advanced_state=key):
                mutated, case = mutation("advanced-ae-quantum")
                case["anchors"][8]["block_state"][key] = bad_value
                with self.assertRaisesRegex(ValueError, "Advanced AE"):
                    namespace["validate_m45_advanced_quantum_physical_fixtures"](
                        mutated
                    )
        for label, field, value in (
            ("be", "expected_block_entity_id", "advanced_ae:wrong"),
            ("synthetic", "source_derived_synthetic_fixture", True),
            ("placement", "placement_state", {"formed": "false"}),
        ):
            with self.subTest(advanced_contract=label):
                mutated, case = mutation("advanced-ae-quantum")
                case["anchors"][8][field] = value
                with self.assertRaisesRegex(ValueError, "Advanced AE"):
                    namespace["validate_m45_advanced_quantum_physical_fixtures"](
                        mutated
                    )
        mutated, case = mutation("advanced-ae-quantum")
        anchors = case["anchors"]
        case["anchors"] = (*anchors[:8], anchors[9], anchors[8], *anchors[10:])
        with self.assertRaisesRegex(ValueError, "Advanced AE"):
            namespace["validate_m45_advanced_quantum_physical_fixtures"](mutated)

        for label, mutate_exception in (
            (
                "missing",
                lambda singleton: singleton.pop(
                    "selector_scoped_model_exception"
                ),
            ),
            (
                "count",
                lambda singleton: singleton[
                    "selector_scoped_model_exception"
                ]["expected_material_triangles"].__setitem__(
                    "bluemap:block/missing", 31
                ),
            ),
            (
                "model-hash",
                lambda singleton: singleton[
                    "selector_scoped_model_exception"
                ]["model"].__setitem__("sha256", "0" * 64),
            ),
        ):
            with self.subTest(advanced_singleton_exception=label):
                mutated, case = mutation("advanced-ae-quantum")
                singleton = next(
                    anchor
                    for anchor in case["anchors"]
                    if anchor["position"] == (418, 100, 370)
                )
                mutate_exception(singleton)
                with self.assertRaisesRegex(ValueError, "Advanced AE"):
                    namespace[
                        "validate_m45_advanced_quantum_physical_fixtures"
                    ](mutated)

        mutated, case = mutation("advanced-ae-quantum")
        case["anchors"][0]["selector_scoped_model_exception"] = copy.deepcopy(
            namespace["M45_ADVANCED_SINGLETON_MODEL_EXCEPTION"]
        )
        with self.assertRaisesRegex(ValueError, "Advanced AE"):
            namespace["validate_m45_advanced_quantum_physical_fixtures"](mutated)

        for key, bad_value in (
            ("formed", "false"),
            ("powered", "true"),
            ("shape", "column_y"),
        ):
            with self.subTest(extended_state=key):
                mutated, case = mutation("extendedae-matrix")
                case["anchors"][6]["block_state"][key] = bad_value
                with self.assertRaisesRegex(ValueError, "ExtendedAE"):
                    namespace["validate_m45_extended_matrix_physical_fixtures"](
                        mutated
                    )
        for label, field, value in (
            ("be", "expected_block_entity_id", "extendedae:wrong"),
            ("synthetic", "source_derived_synthetic_fixture", True),
            ("placement", "placement_state", {"formed": "false"}),
        ):
            with self.subTest(extended_contract=label):
                mutated, case = mutation("extendedae-matrix")
                case["anchors"][6][field] = value
                with self.assertRaisesRegex(ValueError, "ExtendedAE"):
                    namespace["validate_m45_extended_matrix_physical_fixtures"](
                        mutated
                    )
        mutated, case = mutation("extendedae-matrix")
        anchors = case["anchors"]
        case["anchors"] = (*anchors[:6], anchors[7], anchors[6], *anchors[8:])
        with self.assertRaisesRegex(ValueError, "ExtendedAE"):
            namespace["validate_m45_extended_matrix_physical_fixtures"](mutated)

    def test_schema11_advanced_singleton_hidden_faces_are_selector_exact(
        self,
    ) -> None:
        gallery, _evidence = analyze_prbm.parse_cases(self.schema11_cases_path)
        singleton = next(
            anchor
            for case in gallery.cases
            for anchor in case.anchors
            if anchor.position == (418, 100, 370)
        )
        self.assertEqual(
            tuple(
                sorted(
                    analyze_prbm.M45_ADVANCED_SINGLETON_MODEL_EXCEPTION[
                        "expected_material_triangles"
                    ].items()
                )
            ),
            singleton.m45.selector_scoped_exact_material_triangles,
        )

        manifest = json.loads(
            self.schema11_cases_path.read_text(encoding="utf-8")
        )
        raw_singletons = [
            anchor
            for case in manifest["cases"]
            for anchor in case["anchors"]
            if "selector_scoped_model_exception" in anchor
        ]
        self.assertEqual(1, len(raw_singletons))
        self.assertEqual(
            {"x": 418, "y": 100, "z": 370},
            raw_singletons[0]["position"],
        )
        self.assertEqual(
            analyze_prbm.M45_ADVANCED_SINGLETON_MODEL_EXCEPTION,
            raw_singletons[0]["selector_scoped_model_exception"],
        )
        self.assertNotIn(
            "bluemap:block/missing", manifest["profile"]["selected_resources"]
        )
        for route in manifest["profile"]["m45_routes"]:
            for field in (
                "route_resources",
                "source_resources",
                "dependency_resources",
                "host_resources",
                "material_allowlist",
                "stock_material_allowlist",
            ):
                self.assertNotIn("bluemap:block/missing", route[field])

        def mutated_manifest() -> dict[str, object]:
            return json.loads(json.dumps(manifest))

        mutations = []
        value = mutated_manifest()
        raw = next(
            anchor
            for case in value["cases"]
            for anchor in case["anchors"]
            if anchor["position"] == {"x": 418, "y": 100, "z": 370}
        )
        raw.pop("selector_scoped_model_exception")
        mutations.append(value)
        value = mutated_manifest()
        raw = next(
            anchor
            for case in value["cases"]
            for anchor in case["anchors"]
            if anchor["position"] == {"x": 418, "y": 100, "z": 370}
        )
        raw["selector_scoped_model_exception"][
            "expected_material_triangles"
        ]["bluemap:block/missing"] = 30
        mutations.append(value)
        value = mutated_manifest()
        target_case = next(
            case for case in value["cases"] if case["case_id"] == "ae2-m45-05"
        )
        target_case["anchors"][0]["selector_scoped_model_exception"] = (
            copy.deepcopy(analyze_prbm.M45_ADVANCED_SINGLETON_MODEL_EXCEPTION)
        )
        mutations.append(value)
        value = mutated_manifest()
        route = next(
            route
            for route in value["profile"]["m45_routes"]
            if route["route"] == "advanced-ae-quantum"
        )
        route["material_allowlist"].append("bluemap:block/missing")
        route["material_allowlist"].sort()
        route["stock_material_allowlist"].append("bluemap:block/missing")
        route["stock_material_allowlist"].sort()
        mutations.append(value)
        for value in mutations:
            payload = analyze_prbm.canonical_json(value, pretty=True).encode(
                "utf-8"
            )
            with self.assertRaises(analyze_prbm.EvidenceError):
                analyze_prbm._parse_schema11_cases(
                    value, hashlib.sha256(payload).hexdigest()
                )

        enabled_root = Path(self.temporary.name) / "schema11-singleton-enabled"
        build_fixture(enabled_root, cases_path=self.schema11_cases_path)
        report = analyze_prbm.analyze(enabled_root, self.schema11_cases_path)
        result = next(
            anchor
            for case in report["cases"]
            for anchor in case["anchors"]
            if anchor["position"] == {"x": 418, "y": 100, "z": 370}
        )
        self.assertEqual(132, result["triangle_count"])
        self.assertEqual(
            analyze_prbm.M45_ADVANCED_SINGLETON_MODEL_EXCEPTION[
                "expected_material_triangles"
            ],
            result["contract"]["material_triangle_counts"],
        )
        self.assertTrue(
            result["contract"]["selector_scoped_model_exception"]
        )

        corrupt_root = Path(self.temporary.name) / "schema11-singleton-corrupt"
        build_fixture(
            corrupt_root,
            cases_path=self.schema11_cases_path,
            corrupt_m45_singleton_materials=True,
        )
        with self.assertRaisesRegex(
            analyze_prbm.EvidenceError, "must own exactly 132"
        ):
            analyze_prbm.analyze(corrupt_root, self.schema11_cases_path)

        leak_root = Path(self.temporary.name) / "schema11-missing-elsewhere"
        build_fixture(
            leak_root,
            cases_path=self.schema11_cases_path,
            leak_m45_missing_elsewhere=True,
        )
        with self.assertRaisesRegex(
            analyze_prbm.EvidenceError, "selector-forbidden"
        ):
            analyze_prbm.analyze(leak_root, self.schema11_cases_path)

    def test_schema11_m45_projection_matrix_has_exact_affected_counts(self) -> None:
        gallery, evidence = analyze_prbm.parse_cases(self.schema11_cases_path)
        anchors = [
            anchor
            for case in gallery.cases
            for anchor in case.anchors
            if anchor.m45 is not None
        ]

        def counts(**mode: object) -> tuple[int, int, int]:
            projections = [
                analyze_prbm._m45_mode_projection(anchor, **mode)
                for anchor in anchors
            ]
            return (
                sum(projection is not None for projection in projections),
                sum(
                    projection is not None
                    and projection.review_projection == "nonempty"
                    for projection in projections
                ),
                sum(
                    projection is not None
                    and projection.review_projection == "empty"
                    for projection in projections
                ),
            )

        base = {
            "stock_baseline": False,
            "crafting_disabled": False,
            "native_structural_disabled": False,
            "m45_route_disabled": None,
            "m45_disabled": False,
        }
        exact_single_routes = {
            "appflux": (11, 8, 3),
            "merequester": (36, 36, 0),
            "expandedae": (119, 79, 40),
            "megacells": (108, 87, 21),
            "advanced-ae-quantum": (44, 7, 37),
            "advanced-ae-athena": (9, 9, 0),
            "extendedae-matrix": (42, 31, 11),
            "extendedae-planes": (42, 42, 0),
        }
        for route, expected in exact_single_routes.items():
            with self.subTest(disabled_route=route):
                self.assertEqual(
                    expected,
                    counts(**{**base, "m45_route_disabled": route}),
                )
        self.assertEqual(
            (20, 0, 20), counts(**{**base, "crafting_disabled": True})
        )
        self.assertEqual(
            (189, 0, 189),
            counts(**{**base, "native_structural_disabled": True}),
        )
        self.assertEqual(
            (119, 79, 40),
            counts(**{**base, "m45_route_disabled": "expandedae"}),
        )
        self.assertEqual(
            (108, 87, 21),
            counts(**{**base, "m45_route_disabled": "megacells"}),
        )
        self.assertEqual(
            (409, 110, 299), counts(**{**base, "stock_baseline": True})
        )
        self.assertEqual(
            (409, 299, 110), counts(**{**base, "m45_disabled": True})
        )
        self.assertEqual(
            analyze_prbm.M45_DISABLED_PROJECTION_EVIDENCE,
            evidence["m45_review_summary"]["disabled_projection_evidence"],
        )

    def test_schema11_disabled_projection_models_and_materials_fail_closed(
        self,
    ) -> None:
        manifest = json.loads(
            self.schema11_cases_path.read_text(encoding="utf-8")
        )
        m45_cases = [
            case
            for case in manifest["cases"]
            if case.get("milestone") in {"M4", "M5"}
        ]
        anchors = [anchor for case in m45_cases for anchor in case["anchors"]]
        exact_empty = [
            anchor
            for case in m45_cases
            for anchor in case["anchors"]
            if analyze_prbm._m45_exact_empty_original_resource(
                case["route"], anchor
            )
        ]
        self.assertEqual(
            Counter(
                {
                    "expandedae": 22,
                    "megacells": 9,
                    "advanced-ae-quantum": 37,
                    "extendedae-matrix": 11,
                }
            ),
            Counter(anchor["m45_route"] for anchor in exact_empty),
        )
        self.assertTrue(
            all(
                anchor["physical_stock_projection"]["review_projection"]
                == "empty"
                and anchor["route_disabled_projections"][
                    anchor["m45_route"]
                ]["review_projection"]
                == "empty"
                for anchor in exact_empty
            )
        )

        requester = [
            anchor
            for anchor in anchors
            if anchor["block_id"] == "merequester:requester"
        ]
        io_ports = [
            anchor
            for anchor in anchors
            if anchor["block_id"] == "expandedae:exp_io_port"
        ]
        self.assertEqual(12, len(requester))
        self.assertEqual(48, len(io_ports))
        for anchor in requester:
            for projection in (
                anchor["physical_stock_projection"],
                anchor["route_disabled_projections"]["merequester"],
            ):
                self.assertEqual(
                    analyze_prbm.M45_REQUESTER_ORIGINAL_MATERIAL_TRIANGLES,
                    projection["expected_material_triangles"],
                )
                self.assertEqual(
                    set(projection["expected_material_triangles"]),
                    set(projection["allowed_resources"]),
                )
        for anchor in io_ports:
            expected = analyze_prbm.M45_EXP_IO_ORIGINAL_MATERIAL_TRIANGLES[
                anchor["block_state"]["powered"]
            ]
            for projection in (
                anchor["physical_stock_projection"],
                anchor["route_disabled_projections"]["expandedae"],
            ):
                self.assertEqual(expected, projection["expected_material_triangles"])
                self.assertEqual(
                    set(expected), set(projection["allowed_resources"])
                )

        def changed() -> dict[str, object]:
            return copy.deepcopy(manifest)

        mutations: list[dict[str, object]] = []
        value = changed()
        value["m45_review_summary"]["disabled_projection_evidence"][
            "exact_empty_original_resource_classes"
        ][0]["model_sha256"] = "0" * 64
        mutations.append(value)
        value = changed()
        raw_requester = next(
            anchor
            for case in value["cases"]
            for anchor in case["anchors"]
            if anchor["block_id"] == "merequester:requester"
        )
        raw_requester["route_disabled_projections"]["merequester"].pop(
            "expected_material_triangles"
        )
        mutations.append(value)
        value = changed()
        raw_requester = next(
            anchor
            for case in value["cases"]
            for anchor in case["anchors"]
            if anchor["block_id"] == "merequester:requester"
        )
        raw_requester["physical_stock_projection"]["allowed_resources"].append(
            "merequester:block/requester_active"
        )
        raw_requester["physical_stock_projection"]["allowed_resources"].sort()
        mutations.append(value)
        value = changed()
        raw_io = next(
            anchor
            for case in value["cases"]
            for anchor in case["anchors"]
            if anchor["block_id"] == "expandedae:exp_io_port"
        )
        first_resource = next(
            iter(
                raw_io["physical_stock_projection"][
                    "expected_material_triangles"
                ]
            )
        )
        raw_io["physical_stock_projection"]["expected_material_triangles"][
            first_resource
        ] += 1
        mutations.append(value)
        value = changed()
        formed = next(
            anchor
            for case in value["cases"]
            for anchor in case["anchors"]
            if anchor.get("m45_route") == "advanced-ae-quantum"
            and anchor.get("block_id") == "advanced_ae:quantum_structure"
        )
        formed["physical_stock_projection"].update(
            {
                "review_projection": "nonempty",
                "allowed_resources": [
                    "advanced_ae:block/crafting/quantum_structure"
                ],
            }
        )
        mutations.append(value)
        for index, value in enumerate(mutations):
            with self.subTest(manifest_mutation=index):
                payload = analyze_prbm.canonical_json(
                    value, pretty=True
                ).encode("utf-8")
                with self.assertRaises(analyze_prbm.EvidenceError):
                    analyze_prbm._parse_schema11_cases(
                        value, hashlib.sha256(payload).hexdigest()
                    )

        corrupt_root = Path(self.temporary.name) / "m45-inherited-corrupt"
        build_fixture(
            corrupt_root,
            cases_path=self.schema11_cases_path,
            m45_disabled=True,
            corrupt_m45_inherited_projection=True,
        )
        with self.assertRaisesRegex(
            analyze_prbm.EvidenceError,
            "exact inherited original-resource material signature",
        ):
            analyze_prbm.analyze(
                corrupt_root,
                self.schema11_cases_path,
                m45_disabled=True,
            )

        namespace = _full_generator_namespace()
        source_cases = namespace["create_m45_cases"]()
        namespace["validate_m45_disabled_projection_fixtures"](source_cases)
        mutated_cases = copy.deepcopy(source_cases)
        mixed = next(
            anchor
            for case in mutated_cases
            for anchor in case["anchors"]
            if anchor["position"] == (464, 100, 312)
        )
        mixed["route_disabled_projections"]["megacells"][
            "review_projection"
        ] = "nonempty"
        with self.assertRaisesRegex(ValueError, "single-route disabled"):
            namespace["validate_m45_disabled_projection_fixtures"](
                mutated_cases
            )

    def test_schema11_extended_plane_live_aggregate_rejects_count_drift(
        self,
    ) -> None:
        root = Path(self.temporary.name) / "schema11-plane-observation-drift"
        build_fixture(
            root,
            cases_path=self.schema11_cases_path,
            corrupt_m45_extended_plane_observation=True,
        )
        with self.assertRaisesRegex(
            analyze_prbm.EvidenceError,
            "Extended plane enabled live aggregate changed",
        ):
            analyze_prbm.analyze(root, self.schema11_cases_path)

    def test_schema11_stock_and_native_dependency_modes_are_analyzable(self) -> None:
        stock_root = Path(self.temporary.name) / "schema11-stock"
        build_fixture(
            stock_root,
            cases_path=self.schema11_cases_path,
            stock_baseline=True,
        )
        stock = analyze_prbm.analyze(
            stock_root,
            self.schema11_cases_path,
            stock_baseline=True,
        )
        self.assertEqual(
            "live-proven-bounded-layouts-fail-closed-on-game-rewrite",
            stock["m45_review"]["physical_formed_fixture_policy"],
        )
        self.assertFalse(
            any(
                "source-derived synthetic" in limitation
                for limitation in stock["limitations"]
            )
        )
        self.assertEqual(110, stock["summary"]["m45_stock_nonempty_anchor_count"])
        self.assertEqual(299, stock["summary"]["m45_stock_empty_anchor_count"])
        self.assertEqual(1_093, stock["summary"]["m45_stock_triangle_count"])
        self.assertEqual(
            (0, 3, 0, 0),
            (
                stock["m45_legacy_upgrades"]["active_anchor_count"],
                stock["m45_legacy_upgrades"]["projected_anchor_count"],
                stock["m45_legacy_upgrades"]["triangle_count"],
                stock["m45_legacy_upgrades"]["selected_resource_count"],
            ),
        )

        native_root = Path(self.temporary.name) / "schema11-native-disabled"
        build_fixture(
            native_root,
            cases_path=self.schema11_cases_path,
            native_structural_disabled=True,
        )
        native = analyze_prbm.analyze(
            native_root,
            self.schema11_cases_path,
            native_structural_disabled=True,
        )
        self.assertEqual(
            203, native["summary"]["m45_custom_review_anchor_count"]
        )
        self.assertEqual(
            17, native["summary"]["m45_atomic_fallback_anchor_count"]
        )
        self.assertEqual(
            189, native["summary"]["m45_route_disabled_anchor_count"]
        )
        self.assertEqual(
            189, native["summary"]["m45_projected_empty_anchor_count"]
        )
        by_route = {route["route"]: route for route in native["m45_review"]["routes"]}
        self.assertEqual(6, by_route["appflux"]["mode_projected_empty_anchor_count"])
        self.assertEqual(
            42,
            by_route["extendedae-planes"]["mode_projected_empty_anchor_count"],
        )
        self.assertEqual(
            (3, 0, 282, 20),
            (
                native["m45_legacy_upgrades"]["active_anchor_count"],
                native["m45_legacy_upgrades"]["projected_anchor_count"],
                native["m45_legacy_upgrades"]["triangle_count"],
                native["m45_legacy_upgrades"]["selected_resource_count"],
            ),
        )
        self.assertFalse(
            by_route["extendedae-planes"]["enabled_live_observation"][
                "active"
            ]
        )

    def test_schema11_all_single_and_combined_m45_disabled_modes_keep_s1_exact(
        self,
    ) -> None:
        _gallery, evidence = analyze_prbm.parse_cases(self.schema11_cases_path)
        expected = {
            route["route"]: (
                route["route_disabled_affected_anchor_count"],
                route["route_disabled_nonempty_anchor_count"],
                route["route_disabled_empty_anchor_count"],
            )
            for route in evidence["m45_routes"]
        }
        for route in analyze_prbm.M45_ROUTES:
            with self.subTest(route=route):
                root = Path(self.temporary.name) / f"schema11-disabled-{route}"
                build_fixture(
                    root,
                    cases_path=self.schema11_cases_path,
                    m45_route_disabled=route,
                )
                report = analyze_prbm.analyze(
                    root,
                    self.schema11_cases_path,
                    m45_route_disabled=route,
                )
                affected, nonempty, empty = expected[route]
                self.assertEqual(f"m45-route-disabled:{route}", report["mode"])
                self.assertEqual(
                    affected,
                    report["summary"]["m45_route_disabled_anchor_count"],
                )
                self.assertEqual(
                    nonempty,
                    report["summary"]["m45_projected_nonempty_anchor_count"],
                )
                self.assertEqual(
                    empty,
                    report["summary"]["m45_projected_empty_anchor_count"],
                )
                self.assertEqual(351, report["native_structural"]["custom_anchor_count"])
                self.assertEqual(
                    37_518,
                    report["native_structural"]["custom_triangle_count"],
                )
                self.assertEqual(
                    10,
                    report["native_structural_legacy_upgrades"][
                        "custom_anchor_count"
                    ],
                )
                self.assertEqual(
                    840,
                    report["native_structural_legacy_upgrades"][
                        "custom_triangle_count"
                    ],
                )
                expected_active, expected_projected, expected_triangles = (
                    (0, 3, 0)
                    if route == "megacells"
                    else (2, 1, 250)
                    if route == "expandedae"
                    else (3, 0, 282)
                )
                self.assertEqual(
                    (
                        expected_active,
                        expected_projected,
                        expected_triangles,
                    ),
                    (
                        report["m45_legacy_upgrades"]["active_anchor_count"],
                        report["m45_legacy_upgrades"]["projected_anchor_count"],
                        report["m45_legacy_upgrades"]["triangle_count"],
                    ),
                )
                plane = next(
                    result
                    for result in report["m45_review"]["routes"]
                    if result["route"] == "extendedae-planes"
                )
                self.assertEqual(
                    route != "extendedae-planes",
                    plane["enabled_live_observation"]["active"],
                )
                if route == "extendedae-planes":
                    self.assertTrue(
                        set(plane["selected_resources"])
                        <= set(
                            analyze_prbm.M45_NATIVE_CENTER_PROJECTION_RESOURCES
                        )
                    )
                else:
                    self.assertEqual(3_244, plane["triangle_count"])
                    self.assertEqual(8, plane["selected_resource_count"])

        combined_root = Path(self.temporary.name) / "schema11-m45-disabled"
        build_fixture(
            combined_root,
            cases_path=self.schema11_cases_path,
            m45_disabled=True,
        )
        combined = analyze_prbm.analyze(
            combined_root,
            self.schema11_cases_path,
            m45_disabled=True,
        )
        self.assertEqual("m45-disabled", combined["mode"])
        self.assertEqual(
            409, combined["summary"]["m45_route_disabled_anchor_count"]
        )
        self.assertEqual(
            299, combined["summary"]["m45_projected_nonempty_anchor_count"]
        )
        self.assertEqual(
            110, combined["summary"]["m45_projected_empty_anchor_count"]
        )
        self.assertTrue(
            all(route["route_disabled"] for route in combined["m45_review"]["routes"])
        )
        self.assertEqual(351, combined["native_structural"]["custom_anchor_count"])
        self.assertEqual(
            10,
            combined["native_structural_legacy_upgrades"]["custom_anchor_count"],
        )
        self.assertEqual(
            (0, 3, 0),
            (
                combined["m45_legacy_upgrades"]["active_anchor_count"],
                combined["m45_legacy_upgrades"]["projected_anchor_count"],
                combined["m45_legacy_upgrades"]["triangle_count"],
            ),
        )
        combined_plane = next(
            route
            for route in combined["m45_review"]["routes"]
            if route["route"] == "extendedae-planes"
        )
        self.assertFalse(
            combined_plane["enabled_live_observation"]["active"]
        )
        self.assertTrue(
            set(combined_plane["selected_resources"])
            <= set(analyze_prbm.M45_NATIVE_CENTER_PROJECTION_RESOURCES)
        )

    def test_schema11_m45_legacy_upgrade_enabled_and_legacy_modes_fail_closed(
        self,
    ) -> None:
        expected_modes = (
            ("enabled", {}, (3, 0, 282, 20)),
            (
                "extension-disabled",
                {"extension_disabled": True},
                (2, 1, 144, 14),
            ),
            (
                "crafting-disabled",
                {"crafting_disabled": True},
                (2, 1, 250, 17),
            ),
        )
        for label, mode, expected in expected_modes:
            with self.subTest(mode=label):
                root = Path(self.temporary.name) / f"schema11-{label}-overlay"
                build_fixture(
                    root,
                    cases_path=self.schema11_cases_path,
                    **mode,
                )
                report = analyze_prbm.analyze(
                    root,
                    self.schema11_cases_path,
                    **mode,
                )
                self.assertEqual(label, report["mode"])
                self.assertEqual(
                    expected,
                    (
                        report["m45_legacy_upgrades"]["active_anchor_count"],
                        report["m45_legacy_upgrades"]["projected_anchor_count"],
                        report["m45_legacy_upgrades"]["triangle_count"],
                        report["m45_legacy_upgrades"]["selected_resource_count"],
                    ),
                )

        empty_root = Path(self.temporary.name) / "schema11-overlay-empty"
        build_fixture(
            empty_root,
            cases_path=self.schema11_cases_path,
            empty_m45_legacy_upgrade=True,
        )
        with self.assertRaisesRegex(
            analyze_prbm.EvidenceError, "expected exact runtime geometry"
        ):
            analyze_prbm.analyze(empty_root, self.schema11_cases_path)

        leak_root = Path(self.temporary.name) / "schema11-overlay-leak"
        build_fixture(
            leak_root,
            cases_path=self.schema11_cases_path,
            leak_m45_legacy_upgrade=True,
        )
        with self.assertRaisesRegex(
            analyze_prbm.EvidenceError, "exact observed allowlist"
        ):
            analyze_prbm.analyze(leak_root, self.schema11_cases_path)

    def test_schema11_projected_nonempty_material_allowlists_reject_route_leaks(
        self,
    ) -> None:
        stock_leak = Path(self.temporary.name) / "schema11-stock-material-leak"
        build_fixture(
            stock_leak,
            cases_path=self.schema11_cases_path,
            m45_disabled=True,
            leak_m45_projection_kind="stock",
        )
        with self.assertRaisesRegex(
            analyze_prbm.EvidenceError, "exact stock-original-m45 projection"
        ):
            analyze_prbm.analyze(
                stock_leak,
                self.schema11_cases_path,
                m45_disabled=True,
            )

        native_center_leak = (
            Path(self.temporary.name) / "schema11-native-center-material-leak"
        )
        build_fixture(
            native_center_leak,
            cases_path=self.schema11_cases_path,
            m45_route_disabled="extendedae-planes",
            leak_m45_projection_kind="native-center",
        )
        with self.assertRaisesRegex(
            analyze_prbm.EvidenceError, "exact native-center-only-m45 projection"
        ):
            analyze_prbm.analyze(
                native_center_leak,
                self.schema11_cases_path,
                m45_route_disabled="extendedae-planes",
            )

    def test_m45_disabled_cli_flag_is_explicit(self) -> None:
        options = analyze_prbm.parse_args(
            ["--map-root", str(self.map_root), "--m45-disabled"]
        )
        self.assertTrue(options.m45_disabled)
        self.assertIsNone(options.m45_route_disabled)

    def test_schema11_parser_rejects_animated_athena_emission_boundary(self) -> None:
        manifest = json.loads(self.schema11_cases_path.read_text(encoding="utf-8"))
        athena = next(
            route
            for route in manifest["profile"]["m45_routes"]
            if route["route"] == "advanced-ae-athena"
        )
        athena["route_resources"] = list(athena["source_resources"])
        athena["material_allowlist"] = sorted(
            set(athena["material_allowlist"]) | set(athena["source_resources"])
        )
        invalid = Path(self.temporary.name) / "invalid-athena-schema11.json"
        invalid.write_text(
            analyze_prbm.canonical_json(manifest, pretty=True),
            encoding="utf-8",
        )
        with self.assertRaisesRegex(
            analyze_prbm.EvidenceError, "Athena frame-zero"
        ):
            analyze_prbm.parse_cases(invalid)

    def test_fixture_selects_all_597_anchors_deterministically(self) -> None:
        expected = json.loads(EXPECTED_PATH.read_text(encoding="utf-8"))
        first = analyze_prbm.analyze(self.map_root, CASES_PATH)
        second = analyze_prbm.analyze(self.map_root, CASES_PATH)

        self.assertEqual(first, second)
        self.assertEqual("enabled", first["mode"])
        self.assertEqual("enabled", first["summary"]["mode"])
        self.assertEqual(
            analyze_prbm.canonical_json(first, pretty=True),
            analyze_prbm.canonical_json(second, pretty=True),
        )
        self.assertEqual(122, first["summary"]["case_count"])
        self.assertEqual(597, first["summary"]["anchor_count"])
        self.assertEqual(579, first["summary"]["custom_anchor_count"])
        self.assertEqual(26_580, first["summary"]["custom_triangle_count"])
        self.assertEqual(17, first["summary"]["stock_fallback_anchor_count"])
        self.assertEqual(218, first["summary"]["selected_resource_count"])
        self.assertEqual(
            expected["case_triangle_counts"],
            {
                case["case_id"]: case["triangle_count"]
                for case in first["cases"]
                if case["case_id"] in expected["case_triangle_counts"]
            },
        )
        self.assertEqual(9, first["schema_version"])
        self.assertEqual(597, sum(len(case["anchors"]) for case in first["cases"]))
        self.assertEqual(26_590, first["summary"]["selected_triangle_count"])
        manifest_profile = json.loads(CASES_PATH.read_text(encoding="utf-8"))["profile"]
        self.assertEqual(
            sorted(
                manifest_profile["selected_resources"]
                + manifest_profile["resolved_facade_resources"]
            ),
            [
                resource
                for resource in first["summary"]["custom_selected_resources"]
                if resource
            ],
        )
        self.assertGreaterEqual(
            sum(tile["triangle_count"] for tile in first["tiles"]), 26_590
        )
        self.assertEqual(expected["m2_regression_summary"], first["m2_regression"])
        self.assertEqual(expected["m3a_regression_summary"], first["m3a_regression"])
        self.assertEqual(expected["m3b_regression_summary"], first["m3b_regression"])
        self.assertEqual(expected["connected_glass"], first["connected_glass"])
        self.assertEqual(expected["formed_crafting"], first["formed_crafting"])
        self.assertEqual(
            expected["schema6_regression"], first["schema6_regression"]
        )
        self.assertEqual(22_580, first["schema7_regression"]["selected_triangle_count"])
        self.assertEqual(1_188, first["quantum_bridge"]["custom_triangle_count"])
        self.assertEqual(2_822, first["m3_completion"]["custom_triangle_count"])
        self.assertEqual(
            analyze_prbm.SCHEMA8_CANONICAL_SHA256,
            first["schema8_regression"]["cases_manifest_sha256"],
        )
        for section in (
            "m2_regression",
            "m3a_regression",
            "m3b_regression",
            "schema6_regression",
            "schema7_regression",
            "schema8_regression",
        ):
            self.assertNotIn(
                "legacy_upgrade_excluded_anchor_count",
                first[section],
                section,
            )
        self.assertEqual(
            expected["drive_component_insensitivity"],
            first["drive_component_insensitivity"],
        )

    def test_schema10_freezes_the_complete_s1_logical_matrix(self) -> None:
        manifest = json.loads(
            self.schema10_cases_path.read_text(encoding="utf-8")
        )
        if manifest.get("schema_version") != 10:
            self.skipTest("generated S1 schema-10 gallery is not active yet")
        s1_cases = [
            case for case in manifest["cases"] if case.get("milestone") == "S1"
        ]
        if (
            not s1_cases
            or "native_endpoints" not in s1_cases[22]["anchors"][0]
            or "schema9_route_disabled_projection"
            not in s1_cases[0]["anchors"][0]
        ):
            self.skipTest(
                "tracked schema-10 artifact is a withdrawn pre-source-parity matrix"
            )

        contract, evidence = analyze_prbm.parse_cases(self.schema10_cases_path)
        self.assertEqual(10, contract.schema_version)
        self.assertEqual(150, len(contract.cases))
        self.assertEqual(957, sum(len(case.anchors) for case in contract.cases))
        self.assertEqual(
            analyze_prbm.NATIVE_STRUCTURAL_RENDERED_LOGICAL_MATRIX_SHA256,
            evidence["s1_summary"]["rendered_logical_matrix_sha256"],
        )

        ordered_endpoints = tuple(
            (
                anchor["native_endpoints"][0]["block_id"].removeprefix("ae2:"),
                anchor["native_endpoints"][0]["declared_family"],
            )
            for case in s1_cases[22:25]
            for anchor in case["anchors"]
        )
        self.assertEqual(
            analyze_prbm.NATIVE_STRUCTURAL_ENDPOINTS_ORDERED,
            ordered_endpoints,
        )

        mutations: list[tuple[str, dict[str, object]]] = []
        changed = json.loads(json.dumps(manifest))
        changed_s1 = [case for case in changed["cases"] if case.get("milestone") == "S1"]
        changed_s1[22]["anchors"][0], changed_s1[22]["anchors"][1] = (
            changed_s1[22]["anchors"][1],
            changed_s1[22]["anchors"][0],
        )
        mutations.append(("endpoint-order", changed))

        changed = json.loads(json.dumps(manifest))
        changed_s1 = [case for case in changed["cases"] if case.get("milestone") == "S1"]
        changed_s1[22]["anchors"][0]["native_endpoints"][0]["topology"] = "disconnected"
        mutations.append(("endpoint-topology", changed))

        changed = json.loads(json.dumps(manifest))
        changed_s1 = [case for case in changed["cases"] if case.get("milestone") == "S1"]
        changed_s1[0]["anchors"][0]["native_part_group"] = "wrong"
        mutations.append(("part-group", changed))

        changed = json.loads(json.dumps(manifest))
        changed_s1 = [case for case in changed["cases"] if case.get("milestone") == "S1"]
        changed_s1[6]["anchors"][0]["plane_mask"] = 15
        mutations.append(("plane-mask", changed))

        changed = json.loads(json.dumps(manifest))
        changed_s1 = [case for case in changed["cases"] if case.get("milestone") == "S1"]
        changed_s1[20]["anchors"][4]["facades"][0]["block_state"]["Properties"]["distance"] = "2"
        mutations.append(("facade-state", changed))

        changed = json.loads(json.dumps(manifest))
        changed["profile"]["supported_native_structural"][
            "endpoint_state_counts"
        ]["inscriber"] -= 1
        mutations.append(("endpoint-state-cartesian", changed))

        changed = json.loads(json.dumps(manifest))
        changed["profile"]["supported_native_structural"]["facades"][
            "source_whitelist_state_counts"
        ]["minecraft:furnace"] -= 1
        mutations.append(("facade-state-cartesian", changed))

        changed = json.loads(json.dumps(manifest))
        changed["profile"]["supported_native_structural"]["facades"][
            "full_pack_glass_override"
        ]["artifact_sha256"] = "0" * 64
        mutations.append(("glassential-full-pack-override", changed))

        changed = json.loads(json.dumps(manifest))
        changed["profile"]["native_structural_profile"][
            "endpoint_state_side_cartesian_count"
        ] -= 1
        mutations.append(("endpoint-state-side-cartesian", changed))

        changed = json.loads(json.dumps(manifest))
        changed["s1_summary"]["runtime_oracle_triangle_count"] -= 1
        mutations.append(("runtime-oracle-closure", changed))

        changed = json.loads(json.dumps(manifest))
        changed_s1 = [case for case in changed["cases"] if case.get("milestone") == "S1"]
        signature = changed_s1[0]["anchors"][0].pop(
            "expected_nonlighting_attribute_signature"
        )
        changed_s1[0]["anchors"][0]["expected_attribute_signature"] = signature
        mutations.append(("legacy-light-bearing-signature-field", changed))

        for label, changed_manifest in mutations:
            with self.subTest(label=label):
                changed_path = Path(self.temporary.name) / f"changed-{label}.json"
                changed_path.write_text(
                    analyze_prbm.canonical_json(changed_manifest, pretty=True),
                    encoding="utf-8",
                )
                with self.assertRaises(analyze_prbm.EvidenceError):
                    analyze_prbm.parse_cases(changed_path)

    def test_schema10_glassential_full_pack_override_is_exact_and_fail_closed(
        self,
    ) -> None:
        expected = {
            "mod_id": "glassential",
            "version": "3.4.5",
            "artifact": "Glassential-renewed-1.21.1-3.4.5.jar",
            "artifact_size_bytes": 702_249,
            "artifact_sha1": "3a08f59f0930c8123fa1aacdfa0ba9fbdbb6e342",
            "artifact_sha256": (
                "1f0c8f7533bf3b2002575219ba795fd32a44cc5085c2710624ebbf69e6121471"
            ),
            "artifact_sha512": (
                "62ccb9057aab96ba656ec8ce357977360c1cc7761fedd7ac995a40b1f16e389c7"
                "5d753746840b11d30077b6b896938246fb281ec481e560a05084e22098c31d8"
            ),
            "curseforge_project_id": 945_149,
            "curseforge_file_id": 8_440_850,
            "modrinth_project_id": "kc9FSsYx",
            "modrinth_version_id": "ZU9ErRM9",
            "model_path": "assets/minecraft/models/block/glass.json",
            "model_sha256": (
                "dc3cf6fdf740fceb4d2224dcb4132ab103617d0b904fcbbf6b48dbee0ecc9e4e"
            ),
            "texture_path": "assets/glassential/textures/block/glass.png",
            "texture_sha256": (
                "0a5534e6eb350dbce3670d9a4bc98f98ef20fb0747068d374f3529842b902370"
            ),
            "texture_mcmeta_path": (
                "assets/glassential/textures/block/glass.png.mcmeta"
            ),
            "texture_mcmeta_size_bytes": 97,
            "texture_mcmeta_sha256": (
                "23117542de8eb132a734e588a7cac393e7d8375632e4df56cf31010a8360c719"
            ),
            "texture_mcmeta_fusion": {
                "type": "connecting",
                "layout": "full",
                "render_type": "cutout",
            },
            "texture_mcmeta_change_from_3_4_4": (
                "fusion.render_type:translucent-to-cutout-only"
            ),
            "bluemap_5_22_mcmeta_policy": (
                "non-animation-top-level-metadata-ignored;stored-material-uses-png"
            ),
            "client_visual_acceptance": (
                "must-rerun-after-fusion-render-type-translucent-to-cutout"
            ),
            "resolved_material": "glassential:block/glass",
            "priority": "first-resource-wins-glassential-before-minecraft",
        }
        generator = _source_generator_namespace()
        self.assertEqual(
            expected,
            generator["NATIVE_STRUCTURAL_GLASSENTIAL_FULL_PACK_OVERRIDE"],
        )
        self.assertEqual(
            expected,
            analyze_prbm.NATIVE_STRUCTURAL_GLASSENTIAL_FULL_PACK_OVERRIDE,
        )
        analyze_prbm._validate_native_structural_glassential_override(expected)
        schema10 = json.loads(
            self.schema10_cases_path.read_text(encoding="utf-8")
        )
        schema10["profile"]["supported_native_structural"]["facades"][
            "full_pack_glass_override"
        ] = expected
        schema9_payload = analyze_prbm.canonical_json(
            analyze_prbm._schema9_view(schema10), pretty=True
        ).encode("utf-8")
        self.assertEqual(3_314_082, len(schema9_payload))
        self.assertEqual(
            analyze_prbm.SCHEMA9_CANONICAL_SHA256,
            hashlib.sha256(schema9_payload).hexdigest(),
        )

        for field in expected:
            with self.subTest(field=field):
                changed = json.loads(json.dumps(expected))
                changed.pop(field)
                with self.assertRaisesRegex(
                    analyze_prbm.EvidenceError, "Glassential full-pack"
                ):
                    analyze_prbm._validate_native_structural_glassential_override(
                        changed
                    )
        changed = json.loads(json.dumps(expected))
        changed["unexpected"] = True
        with self.assertRaisesRegex(
            analyze_prbm.EvidenceError, "Glassential full-pack"
        ):
            analyze_prbm._validate_native_structural_glassential_override(changed)
        changed = json.loads(json.dumps(expected))
        changed["texture_mcmeta_fusion"]["render_type"] = "translucent"
        with self.assertRaisesRegex(
            analyze_prbm.EvidenceError, "Glassential full-pack"
        ):
            analyze_prbm._validate_native_structural_glassential_override(changed)
        changed = json.loads(json.dumps(expected))
        changed["bluemap_5_22_mcmeta_policy"] = "fusion-metadata-applied"
        with self.assertRaisesRegex(
            analyze_prbm.EvidenceError, "Glassential full-pack"
        ):
            analyze_prbm._validate_native_structural_glassential_override(changed)

    def test_schema10_glassential_eight_row_128_triangle_closure_is_exact(
        self,
    ) -> None:
        record = _manual_native_record(
            analyze_prbm.NATIVE_STRUCTURAL_GLASSENTIAL_MATERIAL,
            ((0.0, 0.0, 0.0), (1.0, 0.0, 0.0), (0.0, 1.0, 1.0)),
        )
        records = {
            position: (record,) * count
            for position, count in (
                analyze_prbm.NATIVE_STRUCTURAL_GLASSENTIAL_FACADE_TRIANGLES.items()
            )
        }
        evidence = analyze_prbm._validate_native_structural_glassential_closure(
            records
        )
        self.assertEqual(8, evidence["anchor_count"])
        self.assertEqual(128, evidence["triangle_count"])
        self.assertEqual(0, evidence["vanilla_glass_triangle_count"])
        self.assertEqual(
            "glassential:block/glass", evidence["resolved_material"]
        )

        missing = dict(records)
        first_position = next(iter(missing))
        missing[first_position] = missing[first_position][:-1]
        vanilla = dict(records)
        vanilla[first_position] = (
            _manual_native_record(
                "minecraft:block/glass", record.positions
            ),
            *vanilla[first_position][1:],
        )
        extra = dict(records)
        extra[(0, 0, 0)] = (record,)
        for label, changed in (
            ("missing-triangle", missing),
            ("vanilla-material", vanilla),
            ("extra-row", extra),
        ):
            with self.subTest(label=label):
                with self.assertRaisesRegex(
                    analyze_prbm.EvidenceError, "material closure"
                ):
                    analyze_prbm._validate_native_structural_glassential_closure(
                        changed
                    )

    def test_schema10_raw_matrix_input_is_reproducible_and_source_projected(
        self,
    ) -> None:
        namespace = _source_generator_namespace()
        cases = _source_s1_cases_from_generator()
        payload = analyze_prbm.canonical_json(cases).encode("utf-8")
        self.assertEqual(
            namespace["S1_RAW_MATRIX_SIZE_BYTES"], len(payload)
        )
        self.assertEqual(
            namespace["S1_RAW_LOGICAL_MATRIX_SHA256"],
            hashlib.sha256(payload).hexdigest(),
        )
        self.assertEqual(
            analyze_prbm.NATIVE_STRUCTURAL_RAW_MATRIX_SIZE_BYTES,
            len(payload),
        )
        self.assertEqual(
            analyze_prbm.NATIVE_STRUCTURAL_RAW_LOGICAL_MATRIX_SHA256,
            hashlib.sha256(payload).hexdigest(),
        )

        logical_cases = json.loads(payload)
        for case in logical_cases:
            for anchor in case["anchors"]:
                for field in (
                    "expected_connections",
                    "expected_triangle_count",
                    "expected_material_triangles",
                    "expected_smart_overlays",
                    "expected_geometry_signature",
                    "expected_nonlighting_attribute_signature",
                ):
                    anchor.pop(field, None)
        logical_payload = analyze_prbm.canonical_json(logical_cases).encode(
            "utf-8"
        )
        self.assertEqual(
            namespace["S1_RAW_STRIPPED_LOGICAL_MATRIX_SIZE_BYTES"],
            len(logical_payload),
        )
        self.assertEqual(
            namespace["S1_RAW_STRIPPED_LOGICAL_MATRIX_SHA256"],
            hashlib.sha256(logical_payload).hexdigest(),
        )
        self.assertEqual(
            analyze_prbm.NATIVE_STRUCTURAL_RAW_STRIPPED_LOGICAL_MATRIX_SIZE_BYTES,
            len(logical_payload),
        )
        self.assertEqual(
            analyze_prbm.NATIVE_STRUCTURAL_RAW_STRIPPED_LOGICAL_MATRIX_SHA256,
            hashlib.sha256(logical_payload).hexdigest(),
        )

        anchors = [anchor for case in cases for anchor in case["anchors"]]
        helpers = [
            helper for case in cases for helper in case["fixture_blocks"]
        ]
        self.assertEqual((28, 360, 135), (len(cases), len(anchors), len(helpers)))
        self.assertEqual(
            Counter({"custom-s1": 351, "stock-fallback-s1": 9}),
            Counter(anchor["expected_path"] for anchor in anchors),
        )

        def xyz(value: dict[str, int]) -> tuple[int, int, int]:
            return tuple(value[axis] for axis in ("x", "y", "z"))

        anchor_positions = [xyz(anchor["position"]) for anchor in anchors]
        helper_positions = [xyz(helper["position"]) for helper in helpers]
        self.assertEqual(360, len(set(anchor_positions)))
        self.assertEqual(135, len(set(helper_positions)))
        self.assertFalse(set(anchor_positions) & set(helper_positions))
        fixture_by_position = {
            xyz(helper["position"]): helper for helper in helpers
        }
        predecessor = {}
        predecessor_triangles = 0
        predecessor_resources = set()
        for anchor in anchors:
            projection = analyze_prbm._schema9_disabled_source_projection(
                anchor, fixture_by_position
            )
            self.assertEqual(
                anchor["schema9_route_disabled_projection"], projection
            )
            if projection["expected_path"] != "stock-empty":
                position = xyz(anchor["position"])
                predecessor[position] = (
                    projection["expected_path"],
                    projection["expected_triangle_count"],
                )
                predecessor_triangles += projection["expected_triangle_count"]
                predecessor_resources.update(
                    projection["expected_material_triangles"]
                )
        self.assertEqual(
            analyze_prbm.NATIVE_STRUCTURAL_SCHEMA9_DISABLED_EXPECTATIONS,
            predecessor,
        )
        self.assertEqual((10, 350, 608, 14), (
            len(predecessor),
            360 - len(predecessor),
            predecessor_triangles,
            len(predecessor_resources),
        ))
        self.assertEqual(
            set(analyze_prbm.NATIVE_STRUCTURAL_SCHEMA9_DISABLED_RESOURCES),
            predecessor_resources,
        )

    def test_schema10_legacy_upgrade_overlay_is_exact_and_nonmutating(self) -> None:
        namespace, manifest = _generated_schema10_manifest()
        schema9_payload = analyze_prbm.canonical_json(
            analyze_prbm._schema9_view(manifest), pretty=True
        ).encode("utf-8")
        self.assertEqual(3_314_082, len(schema9_payload))
        self.assertEqual(
            analyze_prbm.SCHEMA9_CANONICAL_SHA256,
            hashlib.sha256(schema9_payload).hexdigest(),
        )

        contract, evidence = analyze_prbm._parse_schema10_cases(
            manifest, "in-memory-schema10"
        )
        self.assertEqual(
            (940, 64_938, 289, 16),
            (
                contract.expected_custom_anchor_count,
                contract.expected_custom_triangle_count,
                len(contract.expected_selected_resources),
                len(contract.stock_fallback_positions),
            ),
        )
        self.assertEqual(360, len(contract.native_structural_positions))
        self.assertEqual(
            10, len(contract.native_structural_legacy_upgrade_positions)
        )
        self.assertEqual(
            {"legacy_upgrade_excluded_anchor_count": 10},
            analyze_prbm._legacy_upgrade_regression_marker(10),
        )
        self.assertEqual(
            {}, analyze_prbm._legacy_upgrade_regression_marker(0)
        )
        legacy_positions = set(
            contract.native_structural_legacy_upgrade_positions
        )
        for positions, expected_active_count in (
            (contract.m2_regression_positions, 280),
            (contract.m3a_regression_positions, 313),
            (contract.m3b_regression_positions, 349),
        ):
            self.assertTrue(legacy_positions <= set(positions))
            self.assertEqual(
                expected_active_count, len(set(positions) - legacy_positions)
            )
        self.assertEqual(
            {
                "case_count": 10,
                "anchor_count": 10,
                "custom_anchor_count": 10,
                "custom_triangle_count": 840,
                "selected_resource_count": 21,
                "new_selected_resource_count": 0,
                "material_row_count": 70,
                "combined_native_structural_custom_anchor_count": 361,
                "combined_native_structural_custom_triangle_count": 38_358,
                "combined_native_structural_selected_resource_count": 96,
                "combined_native_structural_material_row_count": 2_163,
                "physical_stock_projection": {
                    "rendered_anchor_count": 0,
                    "empty_anchor_count": 10,
                    "triangle_count": 0,
                    "resource_count": 0,
                    "resources": [],
                },
            },
            evidence["native_structural_legacy_upgrades"]["summary"],
        )

        anchors_by_position = {
            anchor.position: anchor
            for case in contract.cases
            for anchor in case.anchors
        }
        appended = [
            anchors_by_position[position]
            for position in contract.native_structural_positions
        ]
        legacy = [
            anchors_by_position[position]
            for position in contract.native_structural_legacy_upgrade_positions
        ]

        def closure(
            anchors: list[analyze_prbm.AnchorContract],
        ) -> tuple[int, int, int, int, int]:
            resources = {
                resource
                for anchor in anchors
                for resource, _count in anchor.expected_material_triangles
            }
            return (
                sum(anchor.expected_path == "custom-s1" for anchor in anchors),
                sum(anchor.expected_triangle_count or 0 for anchor in anchors),
                len(resources),
                sum(len(anchor.expected_material_triangles) for anchor in anchors),
                sum(
                    count
                    for anchor in anchors
                    for resource, count in anchor.expected_material_triangles
                    if resource == analyze_prbm.NATIVE_STRUCTURAL_GLASSENTIAL_MATERIAL
                ),
            )

        self.assertEqual((351, 37_518, 96, 2_093, 128), closure(appended))
        self.assertEqual((10, 840, 21, 70, 48), closure(legacy))
        self.assertEqual(
            0,
            sum(
                count
                for anchor in appended + legacy
                for resource, count in anchor.expected_material_triangles
                if resource == "minecraft:block/glass"
            ),
        )

        raw_input = namespace["reconstruct_native_structural_legacy_input"](
            namespace["create_cases"]()
        )
        raw_bytes = namespace["json_bytes"](raw_input)
        self.assertEqual(22_189, len(raw_bytes))
        self.assertEqual(
            analyze_prbm.NATIVE_STRUCTURAL_LEGACY_INPUT_SHA256,
            hashlib.sha256(raw_bytes).hexdigest(),
        )
        self.assertEqual(
            raw_input,
            analyze_prbm._reconstruct_native_structural_legacy_input(
                analyze_prbm._schema9_view(manifest)
            ),
        )
        self.assertEqual(
            92,
            sum(len(case["fixture_blocks"]) for case in raw_input["cases"]),
        )

        tsv_lines = namespace["cases_tsv"]().decode("utf-8").splitlines()
        header = tsv_lines[0].split("\t")
        self.assertEqual(43, len(header))
        self.assertEqual(
            [
                "effective_overlay_path",
                "effective_overlay_connections",
                "effective_overlay_triangles",
                "effective_overlay_material_triangles",
                "effective_overlay_geometry_signature",
                "effective_overlay_nonlighting_attribute_signature",
            ],
            header[-6:],
        )
        effective_rows = [
            row.split("\t")
            for row in tsv_lines[1:]
            if row.split("\t")[-6] == "custom-s1"
        ]
        self.assertEqual(10, len(effective_rows))
        self.assertEqual(
            2,
            sum(json.loads(row[-5]) != [] for row in effective_rows),
        )

    def test_schema10_legacy_upgrade_overlay_mutations_fail_closed(self) -> None:
        _namespace, manifest = _generated_schema10_manifest()

        def changed() -> dict[str, object]:
            return json.loads(json.dumps(manifest))

        mutations: list[tuple[str, dict[str, object]]] = []

        value = changed()
        value["native_structural_legacy_upgrades"]["rows"][0]["case_id"] = "wrong"
        mutations.append(("selector", value))

        value = changed()
        rows = value["native_structural_legacy_upgrades"]["rows"]
        rows[1] = json.loads(json.dumps(rows[0]))
        mutations.append(("duplicate", value))

        value = changed()
        value["native_structural_legacy_upgrades"]["rows"][0]["position"]["x"] += 1
        mutations.append(("dangling", value))

        value = changed()
        value["native_structural_legacy_upgrades"]["rows"][0][
            "legacy_projection"
        ]["expected_path"] = "custom-m1"
        mutations.append(("nonfallback", value))

        value = changed()
        value["native_structural_legacy_upgrades"]["rows"][0]["enabled"][
            "expected_path"
        ] = "custom-m2"
        mutations.append(("path", value))

        value = changed()
        value["native_structural_legacy_upgrades"]["rows"][0]["enabled"][
            "expected_triangle_count"
        ] += 1
        mutations.append(("count", value))

        value = changed()
        materials = value["native_structural_legacy_upgrades"]["rows"][6][
            "enabled"
        ]["expected_material_triangles"]
        glass = materials.pop("glassential:block/glass")
        materials["minecraft:block/glass"] = glass
        mutations.append(("material", value))

        value = changed()
        value["native_structural_legacy_upgrades"]["rows"][0]["enabled"][
            "expected_nonlighting_attribute_signature"
        ] = "0" * 64
        mutations.append(("signature", value))

        value = changed()
        value["native_structural_legacy_upgrades"]["rows"][0]["enabled"][
            "expected_connections"
        ][0]["required_block_state"] = {"wrong": True}
        mutations.append(("endpoint", value))

        value = changed()
        value["native_structural_legacy_upgrades"]["input"]["sha256"] = "0" * 64
        mutations.append(("input", value))

        value = changed()
        value["native_structural_legacy_upgrades"]["summary"][
            "combined_native_structural_custom_triangle_count"
        ] += 1
        mutations.append(("combined-summary", value))

        value = changed()
        value["native_structural_legacy_upgrades"]["rows"][0][
            "physical_stock"
        ]["expected_path"] = "stock-fallback-m2"
        mutations.append(("physical-stock", value))

        value = changed()
        value["cases"][1]["anchors"][0]["expected_path"] = "custom-m1"
        mutations.append(("source", value))

        value = changed()
        value["profile"]["coverage_milestone"] = "changed"
        mutations.append(("schema9", value))

        for label, mutation in mutations:
            with self.subTest(label=label):
                with self.assertRaises(analyze_prbm.EvidenceError):
                    analyze_prbm._parse_schema10_cases(mutation, "mutated")

    def test_schema10_legacy_facades_use_only_native_active_goldens(self) -> None:
        _namespace, manifest = _generated_schema10_manifest()
        contract, _evidence = analyze_prbm._parse_schema10_cases(
            manifest, "in-memory-schema10"
        )
        anchors_by_position = {
            anchor.position: anchor
            for case in contract.cases
            for anchor in case.anchors
        }
        for position in ((222, 100, 248), (225, 100, 248)):
            with self.subTest(position=position):
                anchor = anchors_by_position[position]
                self.assertEqual("custom-s1", anchor.expected_path)
                self.assertIsNotNone(anchor.native_structural)
                with mock.patch.object(
                    analyze_prbm,
                    "_validate_facade_contract",
                    side_effect=AssertionError("legacy facade validator invoked"),
                ) as legacy_validator:
                    self.assertEqual(
                        {},
                        analyze_prbm._validate_active_facade_contract(anchor, []),
                    )
                legacy_validator.assert_not_called()

                projection = analyze_prbm._validate_schema9_disabled_projection(
                    anchor, []
                )
                self.assertEqual("stock-fallback-m2", projection["expected_path"])
                self.assertEqual(0, projection["triangle_count"])

        schema9 = analyze_prbm._schema9_view(manifest)
        schema9_payload = analyze_prbm.canonical_json(schema9, pretty=True).encode(
            "utf-8"
        )
        legacy, _evidence = analyze_prbm._parse_schema9_cases(
            schema9, hashlib.sha256(schema9_payload).hexdigest()
        )
        legacy_facade = next(
            anchor
            for case in legacy.cases
            for anchor in case.anchors
            if anchor.expected_path == "custom-m2" and anchor.facades
        )
        with mock.patch.object(
            analyze_prbm,
            "_validate_facade_contract",
            return_value={"legacy": True},
        ) as legacy_validator:
            self.assertEqual(
                {"legacy": True},
                analyze_prbm._validate_active_facade_contract(legacy_facade, []),
            )
        legacy_validator.assert_called_once_with(legacy_facade, [])

    def test_native_structural_legacy_floor_ao_is_signature_bound(self) -> None:
        material = "ae2:part/cable/core/glass/transparent"
        triangle = ((0.2, 0.5, 0.2), (0.8, 0.5, 0.2), (0.2, 0.5, 0.8))
        legacy_record = _manual_native_record(material, triangle, ao=63)
        scope = "anchor-v10:216,100,226"
        signature = analyze_prbm._native_structural_nonlighting_signature(
            [legacy_record], scope
        )
        native = analyze_prbm.NativeStructuralContract(
            cable_id="ae2:fluix_glass_cable",
            parts=(),
            facade_mask=None,
            plane_mask=None,
            p2p_frequency=None,
            endpoints=(),
            endpoint_straight_optimization_json=None,
            expected_geometry_signature="geometry",
            expected_nonlighting_attribute_signature=signature,
            stock_triangle_count=0,
            schema9_route_disabled_projection=None,
        )
        legacy_anchor = analyze_prbm.AnchorContract(
            case_id="ae2-m1-02",
            case_label="legacy floor AO",
            expected_path="custom-s1",
            position=(216, 100, 226),
            expected_triangle_count=1,
            expected_material_triangles=((material, 1),),
            expected_smart_overlays=(),
            face_parts=(),
            facades=(),
            expected_terminal_layers=(),
            drive=None,
            fallback_reason=None,
            native_structural=native,
        )
        validated = analyze_prbm._validate_native_structural_contract(
            legacy_anchor,
            [legacy_record],
            {"geometry_signature": "geometry", "attribute_signature": "full"},
        )
        self.assertIsNotNone(validated)

        changed_record = _manual_native_record(material, triangle, ao=127)
        with self.assertRaisesRegex(
            analyze_prbm.EvidenceError, "geometry/UV/material/normal/color/AO"
        ):
            analyze_prbm._validate_native_structural_contract(
                legacy_anchor,
                [changed_record],
                {"geometry_signature": "geometry", "attribute_signature": "full"},
            )

        appended_records = [
            _manual_native_record(material, triangle, ao=255),
            legacy_record,
        ]
        appended_native = replace(
            native,
            expected_nonlighting_attribute_signature=(
                analyze_prbm._native_structural_nonlighting_signature(
                    appended_records, "anchor-v10:305,100,353"
                )
            ),
        )
        appended_anchor = replace(
            legacy_anchor,
            case_id="ae2-s1-23",
            position=(305, 100, 353),
            expected_triangle_count=2,
            expected_material_triangles=((material, 2),),
            native_structural=appended_native,
        )
        self.assertIsNotNone(
            analyze_prbm._validate_native_structural_contract(
                appended_anchor,
                appended_records,
                {"geometry_signature": "geometry", "attribute_signature": "full"},
            )
        )
        changed_appended_records = [
            appended_records[0],
            _manual_native_record(material, triangle, ao=127),
        ]
        with self.assertRaisesRegex(
            analyze_prbm.EvidenceError, "geometry/UV/material/normal/color/AO"
        ):
            analyze_prbm._validate_native_structural_contract(
                appended_anchor,
                changed_appended_records,
                {"geometry_signature": "geometry", "attribute_signature": "full"},
            )

        air_isolated_anchor = replace(
            appended_anchor,
            case_id="ae2-s1-01",
            position=(209, 100, 313),
        )
        with self.assertRaisesRegex(
            analyze_prbm.EvidenceError, "air-isolated AO changed"
        ):
            analyze_prbm._assert_native_anchor_world_attributes(
                air_isolated_anchor,
                [legacy_record],
                "appended air-isolated source golden",
            )

    def test_s1_live_fallbacks_are_persistent_and_verification_ignores_redstone_cache(
        self,
    ) -> None:
        namespace = _full_generator_namespace()
        anchors = {
            tuple(anchor["position"]): anchor
            for case in namespace["CASES"]
            if case["milestone"] == "S1"
            for anchor in case["anchors"]
        }
        expected = {
            (233, 100, 343): (
                "invalid-reporting-spin-monitor",
                "ae2:fluix_covered_cable",
                {"north": {"id": "ae2:monitor", "spin": 4}},
                {},
            ),
            (260, 100, 358): (
                "invalid-reporting-spin-semi-dark-monitor",
                "ae2:fluix_covered_cable",
                {"north": {"id": "ae2:semi_dark_monitor", "spin": 4}},
                {},
            ),
            (263, 100, 358): (
                "invalid-reporting-spin-terminal-multipart",
                "ae2:fluix_covered_cable",
                {
                    "north": {"id": "ae2:terminal", "spin": 0},
                    "south": {"id": "ae2:terminal", "spin": 4},
                },
                {},
            ),
            (266, 100, 358): (
                "invalid-reporting-spin-dark-monitor",
                "ae2:fluix_smart_cable",
                {"south": {"id": "ae2:dark_monitor", "spin": 4}},
                {},
            ),
            (269, 100, 358): (
                "invalid-reporting-spin-pattern-encoding-terminal",
                "ae2:fluix_covered_cable",
                {"up": {"id": "ae2:pattern_encoding_terminal", "spin": 4}},
                {},
            ),
            (272, 100, 358): (
                "non-full-cube-facade",
                "ae2:fluix_covered_cable",
                {},
                {
                    "north": {
                        "Name": "minecraft:oak_stairs",
                        "Properties": {
                            "facing": "east",
                            "half": "bottom",
                            "shape": "straight",
                            "waterlogged": "false",
                        },
                    }
                },
            ),
            (275, 100, 358): (
                "invalid-reporting-spin-crafting-terminal",
                "ae2:fluix_covered_cable",
                {"down": {"id": "ae2:crafting_terminal", "spin": 4}},
                {},
            ),
            (278, 100, 358): (
                "invalid-reporting-spin-storage-monitor",
                "ae2:fluix_covered_cable",
                {"up": {"id": "ae2:storage_monitor", "spin": 4}},
                {},
            ),
            (287, 100, 358): (
                "known-compatible-extension-endpoint-unknown",
                "ae2:fluix_smart_cable",
                {},
                {},
            ),
        }
        fallback_positions = {
            position
            for position, anchor in anchors.items()
            if anchor["expected_path"] == "stock-fallback-s1"
        }
        self.assertEqual(set(expected), fallback_positions)

        build = namespace["build_function"]().decode("utf-8")
        verification = namespace["native_structural_verification_lines"]("audit")
        verification_text = "\n".join(verification)
        self.assertNotIn("hasRedstone", verification_text)
        summary = namespace["cases_manifest"]()["s1_summary"]
        self.assertEqual(
            list(analyze_prbm.NATIVE_STRUCTURAL_UNIT_ONLY_MALFORMED_CASES),
            summary["unit_only_malformed_cases"],
        )
        self.assertEqual(
            analyze_prbm.NATIVE_STRUCTURAL_UNIT_ONLY_REASON,
            summary["unit_only_reason"],
        )
        for position, (reason, cable_id, face_parts, facades) in expected.items():
            with self.subTest(position=position):
                anchor = anchors[position]
                self.assertEqual("ae2:cable_bus", anchor["block_id"])
                self.assertEqual(reason, anchor["fallback_reason"])
                self.assertEqual(cable_id, anchor["cable_id"])
                self.assertEqual(face_parts, anchor["face_parts"])
                self.assertEqual(facades, anchor["facades"])
                block_position = " ".join(map(str, position))
                self.assertIn(
                    f"setblock {block_position} ae2:cable_bus replace",
                    build,
                )
                self.assertIn(
                    f"data merge block {block_position} "
                    f"{namespace['cable_bus_snbt'](anchor)}",
                    build,
                )
                self.assertIn(
                    f"execute unless block {block_position} ae2:cable_bus ",
                    verification_text,
                )
                self.assertIn(
                    f"execute unless data block {block_position} "
                    f"{namespace['cable_bus_snbt'](anchor, include_has_redstone=False)} ",
                    verification_text,
                )

    def test_native_structural_runtime_oracle_identity_and_closure_are_exact(
        self,
    ) -> None:
        namespace = _source_generator_namespace()
        s1_cases = namespace["create_s1_cases"]()
        parsed = namespace["load_native_structural_oracle"](s1_cases)
        oracle_path = PROJECT_ROOT / "gallery/native-structural-oracle.json"
        oracle_bytes = oracle_path.read_bytes()
        oracle_payload = json.loads(oracle_bytes)
        self.assertEqual(2, oracle_payload["schema_version"])
        self.assertTrue(
            all(
                set(entry)
                == {
                    "geometry_signature",
                    "material_triangles",
                    "nonlighting_attribute_signature",
                    "triangle_count",
                }
                for entry in oracle_payload["anchors"].values()
            )
        )
        identity_union = {
            resource
            for entry in parsed.values()
            for resource in entry["material_triangles"]
        }
        self.assertEqual(
            namespace["S1_ORACLE_SIZE_BYTES"], len(oracle_bytes)
        )
        self.assertEqual(
            namespace["S1_ORACLE_SHA256"], hashlib.sha256(oracle_bytes).hexdigest()
        )
        self.assertEqual(namespace["S1_ORACLE_ANCHOR_COUNT"], len(parsed))
        self.assertEqual(
            namespace["S1_ORACLE_TRIANGLE_COUNT"],
            sum(entry["triangle_count"] for entry in parsed.values()),
        )
        self.assertEqual(
            namespace["S1_ORACLE_IDENTITY_COUNT"], len(identity_union)
        )
        self.assertEqual(
            namespace["S1_ORACLE_MATERIAL_ROW_COUNT"],
            sum(len(entry["material_triangles"]) for entry in parsed.values()),
        )
        self.assertEqual(
            (
                namespace["S1_ORACLE_SIZE_BYTES"],
                namespace["S1_ORACLE_SHA256"],
                namespace["S1_ORACLE_ANCHOR_COUNT"],
                namespace["S1_ORACLE_TRIANGLE_COUNT"],
                namespace["S1_ORACLE_IDENTITY_COUNT"],
                namespace["S1_ORACLE_MATERIAL_ROW_COUNT"],
            ),
            (
                analyze_prbm.NATIVE_STRUCTURAL_ORACLE_SIZE_BYTES,
                analyze_prbm.NATIVE_STRUCTURAL_ORACLE_SHA256,
                analyze_prbm.NATIVE_STRUCTURAL_ORACLE_ANCHOR_COUNT,
                analyze_prbm.NATIVE_STRUCTURAL_ORACLE_TRIANGLE_COUNT,
                analyze_prbm.NATIVE_STRUCTURAL_ORACLE_IDENTITY_COUNT,
                analyze_prbm.NATIVE_STRUCTURAL_ORACLE_MATERIAL_ROW_COUNT,
            ),
        )

    def test_native_structural_oracle_selectors_and_counts_are_strictly_typed(
        self,
    ) -> None:
        namespace, manifest = _generated_schema10_manifest()
        main_cases = namespace["create_s1_cases"]()
        legacy_positions = {
            position
            for _case_id, position in namespace["LEGACY_UPGRADE_SELECTORS"]
        }
        oracle_kinds = (
            (
                "main",
                PROJECT_ROOT / "gallery/native-structural-oracle.json",
                "NATIVE_STRUCTURAL_ORACLE_PATH",
                "S1_ORACLE_SIZE_BYTES",
                "S1_ORACLE_SHA256",
                lambda: namespace["load_native_structural_oracle"](main_cases),
            ),
            (
                "legacy",
                PROJECT_ROOT / "gallery/native-structural-legacy-oracle.json",
                "NATIVE_STRUCTURAL_LEGACY_ORACLE_PATH",
                "LEGACY_UPGRADE_ORACLE_SIZE_BYTES",
                "LEGACY_UPGRADE_ORACLE_SHA256",
                lambda: namespace["load_native_structural_legacy_oracle"](
                    legacy_positions
                ),
            ),
        )
        for (
            kind,
            source_path,
            path_constant,
            size_constant,
            digest_constant,
            load,
        ) in oracle_kinds:
            source = json.loads(source_path.read_text(encoding="utf-8"))
            first_key = next(iter(source["anchors"]))
            for mutation in ("noncanonical-position", "boolean-triangle-count"):
                with self.subTest(kind=kind, mutation=mutation):
                    changed = json.loads(json.dumps(source))
                    if mutation == "noncanonical-position":
                        first = changed["anchors"].pop(first_key)
                        changed["anchors"] = {
                            f" {first_key}": first,
                            **changed["anchors"],
                        }
                    else:
                        changed["anchors"][first_key]["triangle_count"] = True
                    payload = namespace["json_bytes"](changed)
                    path = (
                        Path(self.temporary.name)
                        / f"{kind}-{mutation}-oracle.json"
                    )
                    path.write_bytes(payload)
                    namespace[path_constant] = path
                    namespace[size_constant] = len(payload)
                    namespace[digest_constant] = hashlib.sha256(payload).hexdigest()
                    with self.assertRaises(ValueError):
                        load()

        schema9 = analyze_prbm._schema9_view(manifest)
        legacy_source = json.loads(
            (
                PROJECT_ROOT / "gallery/native-structural-legacy-oracle.json"
            ).read_text(encoding="utf-8")
        )
        first_key = next(iter(legacy_source["anchors"]))
        for mutation in ("noncanonical-position", "boolean-triangle-count"):
            with self.subTest(analyzer=True, mutation=mutation):
                changed = json.loads(json.dumps(legacy_source))
                if mutation == "noncanonical-position":
                    first = changed["anchors"].pop(first_key)
                    changed["anchors"] = {
                        f" {first_key}": first,
                        **changed["anchors"],
                    }
                else:
                    changed["anchors"][first_key]["triangle_count"] = True
                payload = analyze_prbm.canonical_json(
                    changed, pretty=True
                ).encode("utf-8")
                path = (
                    Path(self.temporary.name)
                    / f"analyzer-legacy-{mutation}-oracle.json"
                )
                path.write_bytes(payload)
                with mock.patch.multiple(
                    analyze_prbm,
                    NATIVE_STRUCTURAL_LEGACY_ORACLE_PATH=path,
                    NATIVE_STRUCTURAL_LEGACY_ORACLE_SIZE_BYTES=len(payload),
                    NATIVE_STRUCTURAL_LEGACY_ORACLE_SHA256=(
                        hashlib.sha256(payload).hexdigest()
                    ),
                ):
                    with self.assertRaises(analyze_prbm.EvidenceError):
                        analyze_prbm._read_exact_native_structural_legacy_oracle()

        reconstructed = analyze_prbm._reconstruct_native_structural_legacy_input(
            schema9
        )
        changed_input = json.loads(json.dumps(reconstructed))
        changed_input["cases"][0]["fixture_blocks"][-1]["expected_state"] = {
            "wrong": True
        }
        changed_payload = analyze_prbm.canonical_json(
            changed_input, pretty=True
        ).encode("utf-8")
        changed_path = Path(self.temporary.name) / "changed-legacy-input.json"
        changed_path.write_bytes(changed_payload)
        with mock.patch.multiple(
            analyze_prbm,
            NATIVE_STRUCTURAL_LEGACY_INPUT_PATH=changed_path,
            NATIVE_STRUCTURAL_LEGACY_INPUT_SIZE_BYTES=len(changed_payload),
            NATIVE_STRUCTURAL_LEGACY_INPUT_SHA256=(
                hashlib.sha256(changed_payload).hexdigest()
            ),
        ):
            with self.assertRaisesRegex(
                analyze_prbm.EvidenceError, "source reconstruction"
            ):
                analyze_prbm._read_exact_native_structural_legacy_input(schema9)

        namespace["NATIVE_STRUCTURAL_LEGACY_INPUT_PATH"] = changed_path
        namespace["LEGACY_UPGRADE_INPUT_SIZE_BYTES"] = len(changed_payload)
        namespace["LEGACY_UPGRADE_INPUT_SHA256"] = hashlib.sha256(
            changed_payload
        ).hexdigest()
        with self.assertRaisesRegex(ValueError, "source reconstruction"):
            namespace["load_native_structural_legacy_input"]()

    def test_native_structural_oracle_requires_schema2_nonlighting_field(
        self,
    ) -> None:
        namespace = _source_generator_namespace()
        s1_cases = namespace["create_s1_cases"]()
        schema2_payload = json.loads(
            (PROJECT_ROOT / "gallery/native-structural-oracle.json").read_text(
                encoding="utf-8"
            )
        )
        self.assertEqual(2, schema2_payload["schema_version"])
        parsed = namespace["load_native_structural_oracle"](s1_cases)
        self.assertTrue(
            all(
                "nonlighting_attribute_signature" in entry
                and "attribute_signature" not in entry
                for entry in parsed.values()
            )
        )

        for mutation in ("schema1", "legacy-field"):
            with self.subTest(mutation=mutation):
                changed = json.loads(json.dumps(schema2_payload))
                if mutation == "schema1":
                    changed["schema_version"] = 1
                    error = "header changed"
                else:
                    first = next(iter(changed["anchors"].values()))
                    first["attribute_signature"] = first.pop(
                        "nonlighting_attribute_signature"
                    )
                    error = "entry is malformed"
                payload = analyze_prbm.canonical_json(
                    changed, pretty=True
                ).encode("utf-8")
                path = Path(self.temporary.name) / f"{mutation}-oracle.json"
                path.write_bytes(payload)
                namespace["NATIVE_STRUCTURAL_ORACLE_PATH"] = path
                namespace["S1_ORACLE_SIZE_BYTES"] = len(payload)
                namespace["S1_ORACLE_SHA256"] = hashlib.sha256(
                    payload
                ).hexdigest()
                with self.assertRaisesRegex(ValueError, error):
                    namespace["load_native_structural_oracle"](s1_cases)

    def test_schema10_facade_mask_zero_may_omit_empty_optional_array(
        self,
    ) -> None:
        manifest = json.loads(
            self.schema10_cases_path.read_text(encoding="utf-8")
        )
        cases = [
            case for case in manifest["cases"] if case.get("milestone") == "S1"
        ]
        mask_zero = cases[13]["anchors"][0]
        self.assertEqual(0, mask_zero["facade_mask"])
        self.assertNotIn("facades", mask_zero)
        analyze_prbm._validate_s1_facade_gallery_source_fixtures(cases)

        malformed = json.loads(json.dumps(cases))
        malformed[13]["anchors"][0]["facades"] = {}
        with self.assertRaisesRegex(
            analyze_prbm.EvidenceError,
            "S1 facade-mask facade array changed",
        ):
            analyze_prbm._validate_s1_facade_gallery_source_fixtures(malformed)

        missing_nonzero = json.loads(json.dumps(cases))
        self.assertEqual(1, missing_nonzero[13]["anchors"][1]["facade_mask"])
        missing_nonzero[13]["anchors"][1].pop("facades")
        with self.assertRaisesRegex(
            analyze_prbm.EvidenceError,
            "S1 facade mask 1 direction projection changed",
        ):
            analyze_prbm._validate_s1_facade_gallery_source_fixtures(
                missing_nonzero
            )

    def test_schema10_matches_independent_native_structural_goldens(self) -> None:
        goldens = json.loads(
            NATIVE_STRUCTURAL_GOLDENS_PATH.read_text(encoding="utf-8")
        )
        self.assertEqual(1, goldens["schema_version"])
        self.assertEqual(
            "79ee2c704ad62941a426c26b1cb1f76ef5b2ee5a",
            goldens["authority"]["source_commit"],
        )
        self.assertAlmostEqual(
            analyze_prbm.NATIVE_STRUCTURAL_FACADE_THICKNESS,
            goldens["facades"]["thin_thickness_blocks"],
        )
        self.assertEqual(
            {
                "ae2:quartz_fiber": [6, 6, 10, 10],
                "ae2:toggle_bus": [6, 6, 10, 10],
                "ae2:import_bus": [4, 4, 12, 12],
                "ae2:export_bus": [6, 6, 10, 10],
                "ae2:level_emitter": [7, 7, 9, 9],
                "ae2:terminal": [2, 2, 14, 14],
            },
            goldens["facades"]["part_cutout_sixteenths"],
        )
        self.assertEqual(
            analyze_prbm.NATIVE_STRUCTURAL_PLANE_MASK_BITS,
            goldens["planes"]["mask_bits"],
        )
        self.assertEqual(
            {"min": "right-bit-4", "max": "left-bit-1"},
            goldens["planes"]["visual_x_extension"],
        )
        self.assertEqual(
            {"min": "left-bit-1", "max": "right-bit-4"},
            goldens["planes"]["facade_collision_x_extension"],
        )
        self.assertEqual(
            {
                "installed_face": "up",
                "plane_mask": 8,
                "transparent_facade": "up",
                "opaque_perpendicular_facade": "north",
                "normalized_axis": "z",
                "normalized_strip_bounds_blocks": [
                    0.0,
                    analyze_prbm.NATIVE_STRUCTURAL_FACADE_THICKNESS,
                ],
                "source_constructor": "minecraft-aabb-min-max-normalizes-reversed-endpoints",
            },
            goldens["planes"]["transparent_boundary_aabb_normalization"],
        )
        self.assertEqual(
            analyze_prbm.NATIVE_STRUCTURAL_CORNER_KICKER_EPSILON,
            goldens["facades"]["opaque_inner_corner_kick"][
                "three_plane_match_epsilon"
            ],
        )
        self.assertEqual(
            "prbm-blocks",
            goldens["facades"]["opaque_inner_corner_kick"][
                "epsilon_coordinate_units"
            ],
        )
        self.assertEqual(
            {
                "positive": "same-state-glass-omits-original-outward-face-before-clamp",
                "negative": "same-state-oak-log-retains-original-outward-face",
            },
            goldens["facades"]["adjacent_cull"],
        )
        self.assertEqual(
            analyze_prbm.NATIVE_STRUCTURAL_ENDPOINT_STATE_COUNTS,
            goldens["endpoint_state_schemas"]["state_counts"],
        )
        self.assertEqual(
            analyze_prbm.NATIVE_STRUCTURAL_ENDPOINT_STATE_CARTESIAN_COUNT,
            goldens["endpoint_state_schemas"]["state_cartesian_count"],
        )
        self.assertEqual(
            analyze_prbm.NATIVE_STRUCTURAL_ENDPOINT_STATE_SIDE_CARTESIAN_COUNT,
            goldens["endpoint_state_schemas"]["state_side_cartesian_count"],
        )
        self.assertEqual(192, goldens["p2p_frequency"]["triangle_count"])
        self.assertEqual(
            {
                "1": [37, 37, 37],
                "2": [23, 23, 23],
                "3": [5, 5, 5],
                "4": [23, 57, 23],
            },
            goldens["p2p_frequency"]["frequency_0x1234_inactive_rgb_u8"],
        )
        self.assertEqual(
            {"ae2:level_emitter", "ae2:energy_level_emitter"},
            set(goldens["smart_core_parts"]),
        )
        self.assertEqual(
            set(analyze_prbm.NATIVE_STRUCTURAL_PHYSICAL_FACADE_WHITELIST_IDS),
            set(goldens["facade_whitelist"]["physically_represented"]),
        )
        self.assertEqual(
            set(analyze_prbm.NATIVE_STRUCTURAL_FACADE_WHITELIST_IDS)
            - set(analyze_prbm.NATIVE_STRUCTURAL_PHYSICAL_FACADE_WHITELIST_IDS),
            set(goldens["facade_whitelist"]["java_exhaustive_only"]),
        )
        self.assertEqual(
            analyze_prbm.NATIVE_STRUCTURAL_FACADE_WHITELIST_SOLID_RENDER,
            goldens["facade_whitelist"]["solid_render"],
        )
        self.assertEqual(
            analyze_prbm.NATIVE_STRUCTURAL_FACADE_WHITELIST_SAME_STATE_SKIP_RENDERING,
            goldens["facade_whitelist"]["same_state_skip_rendering"],
        )
        self.assertEqual(
            analyze_prbm.NATIVE_STRUCTURAL_ORDINARY_FACADE_SAME_STATE_SKIP_RENDERING,
            goldens["facade_whitelist"][
                "ordinary_same_state_skip_rendering_controls"
            ],
        )
        self.assertEqual(24, goldens["facade_whitelist"]["state_schema_count"])
        self.assertEqual(
            analyze_prbm.NATIVE_STRUCTURAL_FACADE_WHITELIST_STATE_POLICY,
            goldens["facade_whitelist"]["state_schema_policy"],
        )
        self.assertEqual(
            analyze_prbm.NATIVE_STRUCTURAL_FACADE_WHITELIST_STATE_CONTRACT_SHA256,
            goldens["facade_whitelist"]["state_contract_sha256"],
        )
        self.assertEqual(
            analyze_prbm.NATIVE_STRUCTURAL_FACADE_WHITELIST_STATE_COUNTS,
            goldens["facade_whitelist"]["state_counts"],
        )
        self.assertEqual(
            analyze_prbm.NATIVE_STRUCTURAL_FACADE_WHITELIST_STATE_CARTESIAN_COUNT,
            goldens["facade_whitelist"]["state_cartesian_count"],
        )
        self.assertEqual(
            analyze_prbm.NATIVE_STRUCTURAL_FACADE_STATE_CLASSIFICATION_POLICY,
            goldens["facade_whitelist"]["state_classification_policy"],
        )
        self.assertEqual(
            analyze_prbm.NATIVE_STRUCTURAL_FACADE_SOLID_RENDER_TRUE_CARTESIAN_COUNT,
            goldens["facade_whitelist"][
                "solid_render_true_cartesian_count"
            ],
        )
        self.assertEqual(
            analyze_prbm.NATIVE_STRUCTURAL_FACADE_SAME_STATE_SKIP_TRUE_CARTESIAN_COUNT,
            goldens["facade_whitelist"][
                "same_state_skip_rendering_true_cartesian_count"
            ],
        )
        computed_state_counts = {}
        for block_id, schema in (
            analyze_prbm.NATIVE_STRUCTURAL_FACADE_WHITELIST_STATE_SCHEMAS.items()
        ):
            state_count = 1
            for domain in schema.values():
                state_count *= len(domain)
            computed_state_counts[block_id] = state_count
        self.assertEqual(
            analyze_prbm.NATIVE_STRUCTURAL_FACADE_WHITELIST_STATE_COUNTS,
            computed_state_counts,
        )
        self.assertEqual(
            analyze_prbm.NATIVE_STRUCTURAL_FACADE_WHITELIST_STATE_CARTESIAN_COUNT,
            sum(computed_state_counts.values()),
        )
        self.assertEqual(
            analyze_prbm.NATIVE_STRUCTURAL_FACADE_SOLID_RENDER_TRUE_CARTESIAN_COUNT,
            sum(
                computed_state_counts[block_id]
                for block_id, solid_render in (
                    analyze_prbm.NATIVE_STRUCTURAL_FACADE_WHITELIST_SOLID_RENDER.items()
                )
                if solid_render
            ),
        )
        self.assertEqual(
            analyze_prbm.NATIVE_STRUCTURAL_FACADE_SAME_STATE_SKIP_TRUE_CARTESIAN_COUNT,
            sum(
                computed_state_counts[block_id]
                for block_id, skip_rendering in (
                    analyze_prbm.NATIVE_STRUCTURAL_FACADE_WHITELIST_SAME_STATE_SKIP_RENDERING.items()
                )
                if skip_rendering
            ),
        )
        self.assertEqual(
            analyze_prbm.NATIVE_STRUCTURAL_FACADE_WHITELIST_STATE_CONTRACT_SHA256,
            analyze_prbm.sha256_text(
                analyze_prbm.canonical_json(
                    [
                        {
                            "blockId": block_id,
                            "properties": (
                                analyze_prbm.NATIVE_STRUCTURAL_FACADE_WHITELIST_STATE_SCHEMAS[
                                    block_id
                                ]
                            ),
                            "blockstateSha256": (
                                analyze_prbm.NATIVE_STRUCTURAL_FACADE_WHITELIST_BLOCKSTATE_SHA256[
                                    block_id
                                ]
                            ),
                        }
                        for block_id in (
                            analyze_prbm.NATIVE_STRUCTURAL_FACADE_WHITELIST_IDS
                        )
                    ]
                )
            ),
        )
        self.assertEqual(
            0, goldens["facades"]["vibrant_quartz_facade_blocklight_raw_i8"]
        )
        self.assertEqual(
            analyze_prbm.NATIVE_STRUCTURAL_FACADE_AO_DIRECTION_POLICY,
            goldens["facades"]["ambient_occlusion_direction"],
        )
        self.assertEqual(
            analyze_prbm.NATIVE_STRUCTURAL_FACADE_CARDINAL_VARIANT_TRANSFORM_POLICY,
            goldens["facades"]["cardinal_variant_transform"],
        )
        self.assertEqual(
            analyze_prbm.NATIVE_STRUCTURAL_MAP_COLOR_ILLUMINATION_POLICY,
            goldens["facades"]["map_color_illumination"],
        )
        self.assertEqual(
            {
                "facade_triangle_count": 0,
                "short_stilt_triangle_count": 72,
                "cable_triangle_count": 12,
                "remains_custom": True,
            },
            goldens["facades"]["fully_surrounded_quartz"],
        )

        source_cases = _source_s1_cases_from_generator()
        analyze_prbm._validate_s1_endpoint_source_fixtures(source_cases)
        self.assertEqual(
            ["west", "north", "down"],
            source_cases[6]["anchors"][4]["plane_visual_local_axes"],
        )
        self.assertEqual(
            [[0, 1, 0], [15, 15, 1]],
            source_cases[6]["anchors"][4][
                "plane_visual_local_bounds_sixteenths"
            ],
        )
        self.assertEqual(
            [[1, 15, 1], [16, 16, 15]],
            source_cases[6]["anchors"][4][
                "plane_visual_world_bounds_sixteenths"
            ],
        )
        self.assertEqual(
            ["east", "south", "up"],
            source_cases[6]["anchors"][4][
                "plane_facade_cutout_local_axes"
            ],
        )
        self.assertEqual(
            [[1, 1, 15], [16, 15, 16]],
            source_cases[6]["anchors"][4][
                "plane_facade_cutout_local_bounds_sixteenths"
            ],
        )
        self.assertEqual(
            [[1, 15, 1], [16, 16, 15]],
            source_cases[6]["anchors"][4][
                "plane_facade_cutout_world_bounds_sixteenths"
            ],
        )
        self.assertEqual(
            "known-compatible-extension-endpoint-unknown",
            source_cases[27]["anchors"][2]["fallback_reason"],
        )

        manifest = json.loads(
            self.schema10_cases_path.read_text(encoding="utf-8")
        )
        if manifest.get("schema_version") != 10:
            self.skipTest("generated S1 schema-10 gallery is not active yet")
        s1_cases = [
            case for case in manifest["cases"] if case.get("milestone") == "S1"
        ]
        if not s1_cases or "native_endpoints" not in s1_cases[22]["anchors"][0]:
            self.skipTest("tracked schema-10 artifact is a withdrawn pre-source-parity matrix")
        analyze_prbm._validate_s1_endpoint_source_fixtures(s1_cases)
        sized_cutouts = {
            part["id"]: anchor["expected_facade_cutout_sixteenths"]
            for anchor in s1_cases[0]["anchors"]
            if "expected_facade_cutout_sixteenths" in anchor
            for part in anchor["face_parts"]
        }
        self.assertEqual(
            goldens["facades"]["part_cutout_sixteenths"], sized_cutouts
        )
        self.assertEqual(
            "same-face-anchor-short-no-cutout",
            s1_cases[21]["anchors"][1]["facade_structural_expectation"],
        )
        self.assertEqual(
            8,
            s1_cases[6]["anchors"][8][
                "plane_perpendicular_facade_intersection"
            ]["mask_bit"],
        )

    def test_schema9_disabled_projection_is_independently_source_derived(self) -> None:
        cases = _source_s1_cases_from_generator()
        fixtures = {
            tuple(fixture["position"][axis] for axis in ("x", "y", "z")): fixture
            for case in cases
            for fixture in case["fixture_blocks"]
        }
        rendered: dict[tuple[int, int, int], tuple[str, int]] = {}
        resources: set[str] = set()
        triangles = 0
        for case in cases:
            for anchor in case["anchors"]:
                position = tuple(anchor["position"][axis] for axis in ("x", "y", "z"))
                source = analyze_prbm._schema9_disabled_source_projection(
                    anchor, fixtures
                )
                self.assertEqual(
                    source,
                    anchor["schema9_route_disabled_projection"],
                    position,
                )
                if source["expected_path"] == "stock-empty":
                    continue
                rendered[position] = (
                    source["expected_path"],
                    source["expected_triangle_count"],
                )
                triangles += source["expected_triangle_count"]
                resources.update(source["expected_material_triangles"])

        self.assertEqual(
            analyze_prbm.NATIVE_STRUCTURAL_SCHEMA9_DISABLED_EXPECTATIONS,
            rendered,
        )
        self.assertEqual(10, len(rendered))
        self.assertEqual(350, 360 - len(rendered))
        self.assertEqual(608, triangles)
        self.assertEqual(
            analyze_prbm.NATIVE_STRUCTURAL_SCHEMA9_DISABLED_RESOURCES,
            tuple(sorted(resources)),
        )

        persistent_invalid_spin = next(
            anchor
            for case in cases
            for anchor in case["anchors"]
            if anchor["position"] == {"x": 278, "y": 100, "z": 358}
        )
        self.assertEqual(
            "stock-empty",
            analyze_prbm._schema9_disabled_source_projection(
                persistent_invalid_spin, fixtures
            )["expected_path"],
        )
        predecessor_terminal = next(
            anchor
            for case in cases
            for anchor in case["anchors"]
            if anchor["position"] == {"x": 272, "y": 100, "z": 353}
        )
        self.assertEqual(
            "custom-m2",
            analyze_prbm._schema9_disabled_source_projection(
                predecessor_terminal, fixtures
            )["expected_path"],
        )
        rejected = json.loads(json.dumps(predecessor_terminal))
        rejected["face_parts"][0]["id"] = "ae2:not_a_terminal"
        self.assertEqual(
            "stock-empty",
            analyze_prbm._schema9_disabled_source_projection(
                rejected, fixtures
            )["expected_path"],
        )
        metadata_mutation = json.loads(json.dumps(predecessor_terminal))
        metadata_mutation["schema9_route_disabled_projection"][
            "expected_triangle_count"
        ] += 1
        self.assertNotEqual(
            metadata_mutation["schema9_route_disabled_projection"],
            analyze_prbm._schema9_disabled_source_projection(
                metadata_mutation, fixtures
            ),
        )

    def test_schema9_disabled_projection_enforces_material_color_ao_and_world_light(
        self,
    ) -> None:
        material = "ae2:part/cable/core/covered/transparent"
        projection = analyze_prbm.Schema9DisabledProjectionContract(
            expected_path="custom-m1",
            expected_triangle_count=12,
            expected_material_triangles=((material, 12),),
            expected_smart_overlays=(),
            expected_terminal_layers=(),
        )
        native = analyze_prbm.NativeStructuralContract(
            cable_id="ae2:fluix_covered_cable",
            parts=(),
            facade_mask=None,
            plane_mask=None,
            p2p_frequency=None,
            endpoints=(),
            endpoint_straight_optimization_json=None,
            expected_geometry_signature=None,
            expected_nonlighting_attribute_signature=None,
            stock_triangle_count=0,
            schema9_route_disabled_projection=projection,
        )
        anchor = analyze_prbm.AnchorContract(
            case_id="ae2-s1-source",
            case_label="schema9 predecessor source mutation",
            expected_path="custom-s1",
            position=(1, 2, 3),
            expected_triangle_count=12,
            expected_material_triangles=((material, 12),),
            expected_smart_overlays=(),
            face_parts=(),
            facades=(),
            expected_terminal_layers=(),
            drive=None,
            fallback_reason=None,
            native_structural=native,
        )
        triangle = ((0.2, 0.5, 0.2), (0.8, 0.5, 0.2), (0.2, 0.5, 0.8))
        records = [_manual_native_record(material, triangle) for _ in range(12)]
        result = analyze_prbm._validate_schema9_disabled_projection(anchor, records)
        self.assertEqual(12, result["triangle_count"])
        environmental_records = [
            replace(
                record,
                blocklights=(8, 8, 8),
                sunlights=(9, 9, 9),
            )
            for record in records
        ]
        environmental_result = analyze_prbm._validate_schema9_disabled_projection(
            anchor, environmental_records
        )
        self.assertEqual(12, environmental_result["triangle_count"])

        with self.assertRaisesRegex(
            analyze_prbm.EvidenceError, "exact schema-9 predecessor projection"
        ):
            analyze_prbm._validate_schema9_disabled_projection(anchor, records[:-1])
        wrong_material = [
            _manual_native_record("bluemap:block/missing", triangle), *records[1:]
        ]
        with self.assertRaisesRegex(
            analyze_prbm.EvidenceError, "exact schema-9 predecessor projection"
        ):
            analyze_prbm._validate_schema9_disabled_projection(
                anchor, wrong_material
            )
        bad_rgb = [
            replace(records[0], colors=((254, 255, 255),) * 3),
            *records[1:],
        ]
        with self.assertRaisesRegex(
            analyze_prbm.EvidenceError, "color/light changed"
        ):
            analyze_prbm._validate_schema9_disabled_projection(anchor, bad_rgb)
        bad_ao = [_manual_native_record(material, triangle, ao=254), *records[1:]]
        with self.assertRaisesRegex(
            analyze_prbm.EvidenceError, "ambient occlusion changed"
        ):
            analyze_prbm._validate_schema9_disabled_projection(anchor, bad_ao)
        nonflat_light = [
            replace(records[0], blocklights=(8, 9, 8)),
            *records[1:],
        ]
        with self.assertRaisesRegex(
            analyze_prbm.EvidenceError, "not flat within one triangle"
        ):
            analyze_prbm._validate_schema9_disabled_projection(
                anchor, nonflat_light
            )
        out_of_range_light = [
            replace(records[0], sunlights=(16, 16, 16)),
            *records[1:],
        ]
        with self.assertRaisesRegex(
            analyze_prbm.EvidenceError, r"outside the source-derived \[0, 15\] bound"
        ):
            analyze_prbm._validate_schema9_disabled_projection(
                anchor, out_of_range_light
            )

    def test_schema9_disabled_projection_keeps_channel_overlays_fullbright(
        self,
    ) -> None:
        material = "ae2:part/cable/smart/channels_00"
        rgb = (180, 180, 180)
        projection = analyze_prbm.Schema9DisabledProjectionContract(
            expected_path="custom-m1",
            expected_triangle_count=1,
            expected_material_triangles=((material, 1),),
            expected_smart_overlays=((material, rgb, 15, 15),),
            expected_terminal_layers=(),
        )
        native = analyze_prbm.NativeStructuralContract(
            cable_id="ae2:fluix_smart_cable",
            parts=(),
            facade_mask=None,
            plane_mask=None,
            p2p_frequency=None,
            endpoints=(),
            endpoint_straight_optimization_json=None,
            expected_geometry_signature=None,
            expected_nonlighting_attribute_signature=None,
            stock_triangle_count=0,
            schema9_route_disabled_projection=projection,
        )
        anchor = analyze_prbm.AnchorContract(
            case_id="ae2-s1-source",
            case_label="schema9 predecessor forced channel overlay",
            expected_path="custom-s1",
            position=(1, 2, 3),
            expected_triangle_count=1,
            expected_material_triangles=((material, 1),),
            expected_smart_overlays=((material, rgb, 15, 15),),
            face_parts=(),
            facades=(),
            expected_terminal_layers=(),
            drive=None,
            fallback_reason=None,
            native_structural=native,
        )
        triangle = ((0.2, 0.5, 0.2), (0.8, 0.5, 0.2), (0.2, 0.5, 0.8))
        record = _manual_native_record(
            material,
            triangle,
            color=rgb,
            blocklight=15,
            sunlight=15,
        )
        result = analyze_prbm._validate_schema9_disabled_projection(
            anchor, [record]
        )
        self.assertEqual(1, result["triangle_count"])
        for channel, changed in (
            ("blocklight", replace(record, blocklights=(14, 14, 14))),
            ("sunlight", replace(record, sunlights=(14, 14, 14))),
        ):
            with self.subTest(channel=channel):
                with self.assertRaisesRegex(
                    analyze_prbm.EvidenceError,
                    "forced-fullbright channel overlay is not exact 15/15",
                ):
                    analyze_prbm._validate_schema9_disabled_projection(
                        anchor, [changed]
                    )

    def test_native_structural_disabled_is_exact_schema9_predecessor_projection(self) -> None:
        _namespace, manifest = _generated_schema10_manifest()
        schema9_payload = analyze_prbm.canonical_json(
            analyze_prbm._schema9_view(manifest), pretty=True
        ).encode("utf-8")
        self.assertEqual(3_314_082, len(schema9_payload))
        self.assertEqual(
            analyze_prbm.SCHEMA9_CANONICAL_SHA256,
            hashlib.sha256(schema9_payload).hexdigest(),
        )
        schema10_path = Path(self.temporary.name) / "generated-schema10-disabled.json"
        schema10_path.write_text(
            analyze_prbm.canonical_json(manifest, pretty=True),
            encoding="utf-8",
        )
        root = Path(self.temporary.name) / "native-structural-disabled"
        build_fixture(
            root,
            cases_path=schema10_path,
            native_structural_disabled=True,
        )
        first = analyze_prbm.analyze(
            root,
            schema10_path,
            native_structural_disabled=True,
        )
        second = analyze_prbm.analyze(
            root,
            schema10_path,
            native_structural_disabled=True,
        )
        self.assertEqual(first, second)
        self.assertEqual("native-structural-disabled", first["mode"])
        self.assertEqual(150, first["summary"]["case_count"])
        self.assertEqual(957, first["summary"]["anchor_count"])
        self.assertEqual(589, first["summary"]["custom_anchor_count"])
        self.assertEqual(27_188, first["summary"]["custom_triangle_count"])
        self.assertEqual(17, first["summary"]["stock_fallback_anchor_count"])
        self.assertEqual(
            360, first["summary"]["native_structural_disabled_anchor_count"]
        )
        self.assertEqual(
            10,
            first["summary"]["native_structural_legacy_disabled_anchor_count"],
        )
        self.assertEqual(608, first["summary"]["native_structural_disabled_triangle_count"])
        self.assertEqual(
            10,
            first["summary"][
                "native_structural_predecessor_rendered_anchor_count"
            ],
        )
        self.assertEqual(
            350,
            first["summary"]["native_structural_predecessor_empty_anchor_count"],
        )
        self.assertEqual(0, first["native_structural"]["custom_triangle_count"])
        self.assertEqual(0, first["native_structural"]["stock_fallback_anchor_count"])
        self.assertEqual(360, first["native_structural"]["route_disabled_anchor_count"])
        predecessor = first["native_structural"]["predecessor_projection"]
        self.assertTrue(predecessor["active"])
        self.assertEqual(10, predecessor["rendered_anchor_count"])
        self.assertEqual(350, predecessor["empty_anchor_count"])
        self.assertEqual(608, predecessor["triangle_count"])
        self.assertEqual(14, predecessor["selected_resource_count"])
        self.assertFalse(
            first["native_structural"]["physical_stock_projection"]["active"]
        )
        legacy_upgrades = first["native_structural_legacy_upgrades"]
        self.assertEqual("native-structural-disabled", legacy_upgrades["mode"])
        self.assertEqual(
            (10, 0, 0, 0),
            (
                legacy_upgrades["route_disabled_anchor_count"],
                legacy_upgrades["nonzero_custom_anchor_count"],
                legacy_upgrades["custom_triangle_count"],
                legacy_upgrades["selected_resource_count"],
            ),
        )
        self.assertEqual(
            {
                "active": True,
                "profile": "accepted-schema9-m1-m2",
                "fallback_anchor_count": 10,
                "rendered_anchor_count": 0,
                "empty_anchor_count": 10,
                "triangle_count": 0,
                "selected_resource_count": 0,
                "resources": [],
            },
            legacy_upgrades["predecessor_projection"],
        )
        self.assertFalse(legacy_upgrades["physical_stock_projection"]["active"])
        self.assertEqual(
            0, legacy_upgrades["full_pack_glass_override"]["triangle_count"]
        )
        upgrade_results = [
            anchor
            for case in first["cases"]
            for anchor in case["anchors"]
            if anchor["native_structural_legacy_upgrade"]
        ]
        self.assertEqual(10, len(upgrade_results))
        self.assertEqual(
            Counter(
                {
                    "stock-fallback-device-endpoint": 2,
                    "stock-fallback-m2": 8,
                }
            ),
            Counter(
                anchor["contract"]["expected_path"]
                for anchor in upgrade_results
            ),
        )
        self.assertTrue(
            all(
                anchor["expected_path"] == "custom-s1"
                and anchor["triangle_count"] == 0
                and anchor["contract"]["triangle_count"] == 0
                for anchor in upgrade_results
            )
        )
        accepted_schema9 = analyze_prbm.analyze(self.map_root, self.cases_path)
        for section in (
            "m2_regression",
            "m3a_regression",
            "m3b_regression",
            "connected_glass",
            "formed_crafting",
            "quantum_bridge",
            "m3_completion",
        ):
            self.assertEqual(
                accepted_schema9[section], first[section], section
            )
        for section in (
            "m2_regression",
            "m3a_regression",
            "m3b_regression",
            "schema6_regression",
            "schema7_regression",
            "schema8_regression",
        ):
            self.assertNotIn(
                "legacy_upgrade_excluded_anchor_count",
                first[section],
                section,
            )

    def test_native_structural_disabled_rejects_one_triangle_leak(self) -> None:
        _namespace, manifest = _generated_schema10_manifest()
        schema10_path = Path(self.temporary.name) / "generated-schema10-leak.json"
        schema10_path.write_text(
            analyze_prbm.canonical_json(manifest, pretty=True),
            encoding="utf-8",
        )
        root = Path(self.temporary.name) / "native-structural-leak"
        build_fixture(
            root,
            cases_path=schema10_path,
            native_structural_disabled=True,
            leak_native_structural_disabled=True,
        )
        with self.assertRaisesRegex(
            analyze_prbm.EvidenceError,
            "native-structural-disabled anchor .* exact schema-9 predecessor projection",
        ):
            analyze_prbm.analyze(
                root,
                schema10_path,
                native_structural_disabled=True,
            )

    def test_native_structural_legacy_disabled_rejects_one_triangle_leak(
        self,
    ) -> None:
        _namespace, manifest = _generated_schema10_manifest()
        schema10_path = (
            Path(self.temporary.name) / "generated-schema10-legacy-leak.json"
        )
        schema10_path.write_text(
            analyze_prbm.canonical_json(manifest, pretty=True),
            encoding="utf-8",
        )
        root = Path(self.temporary.name) / "native-structural-legacy-leak"
        build_fixture(
            root,
            cases_path=schema10_path,
            native_structural_disabled=True,
            leak_device_fallback=True,
        )
        with self.assertRaisesRegex(
            analyze_prbm.EvidenceError,
            r"native-structural-disabled anchor \(216, 100, 226\) differs from its exact schema-9 predecessor projection",
        ):
            analyze_prbm.analyze(
                root,
                schema10_path,
                native_structural_disabled=True,
            )

    def test_schema10_stock_baseline_projects_every_s1_anchor_empty(self) -> None:
        _namespace, manifest = _generated_schema10_manifest()
        schema10_path = Path(self.temporary.name) / "generated-schema10-stock.json"
        schema10_path.write_text(
            analyze_prbm.canonical_json(manifest, pretty=True),
            encoding="utf-8",
        )
        root = Path(self.temporary.name) / "schema10-stock"
        build_fixture(
            root,
            cases_path=schema10_path,
            stock_baseline=True,
        )
        report = analyze_prbm.analyze(
            root,
            schema10_path,
            stock_baseline=True,
        )
        self.assertEqual("stock-baseline", report["mode"])
        self.assertEqual(150, report["summary"]["case_count"])
        self.assertEqual(957, report["summary"]["anchor_count"])
        self.assertEqual(878, report["summary"]["stock_empty_anchor_count"])
        self.assertEqual(
            40, report["summary"]["m3_completion_stock_empty_anchor_count"]
        )
        self.assertEqual(1_882, report["summary"]["selected_triangle_count"])
        self.assertEqual(0, report["native_structural"]["custom_triangle_count"])
        self.assertEqual(0, report["native_structural"]["route_selected_resource_count"])
        self.assertTrue(
            report["native_structural"]["physical_stock_projection"]["active"]
        )
        self.assertFalse(
            report["native_structural"]["predecessor_projection"]["active"]
        )
        legacy_upgrades = report["native_structural_legacy_upgrades"]
        self.assertEqual("stock-baseline", legacy_upgrades["mode"])
        self.assertEqual(
            (0, 0, 0, 0),
            (
                legacy_upgrades["route_disabled_anchor_count"],
                legacy_upgrades["nonzero_custom_anchor_count"],
                legacy_upgrades["custom_triangle_count"],
                legacy_upgrades["selected_resource_count"],
            ),
        )
        self.assertFalse(legacy_upgrades["predecessor_projection"]["active"])
        self.assertEqual(
            {
                "active": True,
                "rendered_anchor_count": 0,
                "empty_anchor_count": 10,
                "triangle_count": 0,
                "selected_resource_count": 0,
                "resources": [],
            },
            legacy_upgrades["physical_stock_projection"],
        )
        self.assertEqual(
            0, legacy_upgrades["full_pack_glass_override"]["triangle_count"]
        )
        for section in (
            "m2_regression",
            "m3a_regression",
            "m3b_regression",
            "schema6_regression",
            "schema7_regression",
            "schema8_regression",
        ):
            self.assertNotIn(
                "legacy_upgrade_excluded_anchor_count",
                report[section],
                section,
            )

    def test_report_does_not_depend_on_map_root_path(self) -> None:
        copied = Path(self.temporary.name) / "copied" / "map"
        shutil.copytree(self.map_root, copied)
        self.assertEqual(
            analyze_prbm.analyze(self.map_root, CASES_PATH),
            analyze_prbm.analyze(copied, CASES_PATH),
        )

    def test_semantic_signatures_do_not_depend_on_texture_ordinals(self) -> None:
        reordered = Path(self.temporary.name) / "reordered" / "map"
        build_fixture(reordered, reverse_texture_ordinals=True)
        first = analyze_prbm.analyze(self.map_root, CASES_PATH)["summary"]
        second = analyze_prbm.analyze(reordered, CASES_PATH)["summary"]

        for key in (
            "shape_signature",
            "material_signature",
            "geometry_signature",
            "attribute_signature",
        ):
            self.assertEqual(first[key], second[key], key)
        self.assertEqual(
            first["custom_selected_resources"], second["custom_selected_resources"]
        )

    def test_material_independent_shapes_are_shared_across_color_variants(self) -> None:
        report = analyze_prbm.analyze(self.map_root, CASES_PATH)
        catalog_cases = [
            case for case in report["cases"] if case["category"] == "color-family-catalog"
        ]
        self.assertEqual(5, len(catalog_cases))
        for case in catalog_cases:
            with self.subTest(case=case["case_id"]):
                self.assertEqual(
                    1,
                    len(
                        {
                            anchor["shape_signature"]
                            for anchor in case["anchors"][0::2]
                        }
                    ),
                )
                self.assertEqual(
                    1,
                    len(
                        {
                            anchor["shape_signature"]
                            for anchor in case["anchors"][1::2]
                        }
                    ),
                )

    def test_exact_m3f_manifest_extends_a_byte_frozen_schema8_contract(self) -> None:
        contract, evidence = analyze_prbm.parse_cases(CASES_PATH)
        manifest = json.loads(CASES_PATH.read_text(encoding="utf-8"))

        self.assertEqual(9, evidence["schema_version"])
        self.assertEqual(9, evidence["signature_schema_version"])
        self.assertEqual(122, len(contract.cases))
        self.assertEqual(579, contract.expected_custom_anchor_count)
        self.assertEqual(26_580, contract.expected_custom_triangle_count)
        self.assertEqual(218, len(contract.expected_selected_resources))
        self.assertEqual(218, evidence["expected_custom_summary"]["selected_resource_count"])
        self.assertEqual(597, sum(len(case.anchors) for case in contract.cases))
        self.assertEqual(
            {
                "anchor_count": 47,
                "base_selection_count": 4,
                "block_ids": ["ae2:quartz_glass", "ae2:quartz_vibrant_glass"],
                "case_count": 11,
                "custom_anchor_count": 47,
                "custom_triangle_count": 776,
                "frame_mask_occurrences": analyze_prbm.CONNECTED_GLASS_FRAME_OCCURRENCES,
                "frame_resource_count": 15,
                "new_selected_resource_count": 19,
                "no_frame_face_count": 2,
                "no_frame_mask": "0000",
                "selected_resource_count": 19,
                "stock_fallback_anchor_count": 0,
                "triangle_formula": "2*visibleFaces+2*visibleFrameFaces",
            },
            evidence["m3c_summary"],
        )
        self.assertEqual(
            {
                "anchor_count": 86,
                "block_id_count": 8,
                "case_count": 9,
                "custom_anchor_count": 85,
                "custom_triangle_count": 4_306,
                "fully_enclosed_zero_geometry_anchor_count": 1,
                "fully_enclosed_zero_geometry_evidence_status": (
                    "not-renderer-provenance-distinguishable-in-prbm"
                ),
                "monitor_display_policy": "client-stream-only-display-omitted",
                "new_selected_resource_count": 15,
                "paint_ordinal_count": 17,
                "selected_resource_count": 15,
                "stock_fallback_anchor_count": 1,
            },
            evidence["m3d_summary"],
        )
        frozen_cases = json.dumps(
            manifest["cases"][:92],
            sort_keys=True,
            separators=(",", ":"),
            ensure_ascii=True,
        ).encode("utf-8")
        self.assertEqual(
            "a022d0e75aab44d75692cc0a8848eb3eaecb26d4afe939da7d4797edf7dcb08e",
            hashlib.sha256(frozen_cases).hexdigest(),
        )
        frozen_schema6 = analyze_prbm.canonical_json(
            analyze_prbm._schema6_view(
                analyze_prbm._schema7_view(analyze_prbm._schema8_view(manifest))
            ),
            pretty=True,
        ).encode("utf-8")
        self.assertEqual(
            "2d4fbba58ea2c4d3ed741e93a8dd9857523cac9cda021ffd3111e6ac51aec602",
            hashlib.sha256(frozen_schema6).hexdigest(),
        )
        frozen_schema8 = analyze_prbm._schema8_view(manifest)
        self.assertEqual(manifest["cases"][:115], frozen_schema8["cases"])
        self.assertEqual(
            analyze_prbm.SCHEMA8_CANONICAL_SHA256,
            hashlib.sha256(
                analyze_prbm.canonical_json(frozen_schema8, pretty=True).encode("utf-8")
            ).hexdigest(),
        )
        frozen_schema7 = analyze_prbm._schema7_view(frozen_schema8)
        self.assertEqual(manifest["cases"][:112], frozen_schema7["cases"])
        frozen_schema7_bytes = analyze_prbm.canonical_json(
            frozen_schema7, pretty=True
        ).encode("utf-8")
        self.assertEqual(
            analyze_prbm.SCHEMA7_CANONICAL_SHA256,
            hashlib.sha256(frozen_schema7_bytes).hexdigest(),
        )
        self.assertEqual(
            analyze_prbm.SCHEMA7_CANONICAL_SHA256,
            evidence["frozen_schema7_view_sha256"],
        )
        self.assertEqual(
            analyze_prbm.SCHEMA8_CANONICAL_SHA256,
            evidence["frozen_schema8_view_sha256"],
        )

        schema5_path = Path(self.temporary.name) / "m3b-cases.json"
        schema5_path.write_text(
            json.dumps(_schema5_m3b_manifest(), indent=2, sort_keys=True) + "\n",
            encoding="utf-8",
        )
        schema5_contract, schema5_evidence = analyze_prbm.parse_cases(schema5_path)
        self.assertEqual(5, schema5_contract.schema_version)
        self.assertEqual(5, schema5_evidence["schema_version"])
        self.assertEqual(92, len(schema5_contract.cases))
        self.assertEqual(342, schema5_contract.expected_custom_anchor_count)
        self.assertEqual(17_488, schema5_contract.expected_custom_triangle_count)
        self.assertEqual(167, len(schema5_contract.expected_selected_resources))
        self.assertEqual(359, sum(len(case.anchors) for case in schema5_contract.cases))
        schema5_report = analyze_prbm.analyze(self.map_root, schema5_path)
        schema8_report = analyze_prbm.analyze(self.map_root, CASES_PATH)
        frozen_regression = schema8_report["m3b_regression"]
        self.assertEqual(
            frozen_regression,
            {
                key: schema5_report["summary"][key]
                for key in frozen_regression
            },
        )
        schema6_path = Path(self.temporary.name) / "m3c-cases.json"
        schema6_path.write_text(
            analyze_prbm.canonical_json(_schema6_m3c_manifest(), pretty=True),
            encoding="utf-8",
        )
        schema6_report = analyze_prbm.analyze(self.map_root, schema6_path)
        for key in (
            "case_count",
            "anchor_count",
            "selected_triangle_count",
            "material_signature",
            "shape_signature",
            "geometry_signature",
            "attribute_signature",
            "custom_anchor_count",
            "custom_triangle_count",
            "custom_selected_resource_count",
            "stock_fallback_anchor_count",
            "stock_fallback_triangle_count",
        ):
            self.assertEqual(
                schema6_report["summary"][key],
                schema8_report["schema6_regression"][key],
                key,
            )
        schema7_path = Path(self.temporary.name) / "m3d-cases.json"
        schema7_path.write_text(
            analyze_prbm.canonical_json(frozen_schema7, pretty=True),
            encoding="utf-8",
        )
        schema7_report = analyze_prbm.analyze(self.map_root, schema7_path)
        for key, value in schema8_report["schema7_regression"].items():
            if key in {"schema_version", "signature_schema_version", "cases_manifest_sha256"}:
                continue
            self.assertEqual(value, schema7_report["summary"][key], key)
        self.assertEqual(
            {
                "anchor_count": 269,
                "case_count": 48,
                "custom_anchor_count": 266,
                "custom_triangle_count": 7_576,
                "selected_resource_count": 140,
            },
            evidence["m1_regression_summary"],
        )
        self.assertEqual(14, evidence["m2_summary"]["case_count"])
        self.assertEqual(1_000, evidence["m2_summary"]["custom_triangle_count"])
        self.assertEqual(14, evidence["m3_summary"]["case_count"])
        self.assertEqual(3_856, evidence["m3_summary"]["custom_triangle_count"])
        self.assertEqual(61, evidence["m3_summary"]["occupied_slot_count"])
        self.assertEqual(16, evidence["m3b_summary"]["case_count"])
        self.assertEqual(5_056, evidence["m3b_summary"]["custom_triangle_count"])
        self.assertEqual(84, evidence["m3b_summary"]["occupied_slot_count"])
        self.assertEqual(26, evidence["m3b_summary"]["accepted_cell_id_count"])
        durable_face_fallback = next(
            case for case in manifest["cases"] if case["case_id"] == "ae2-m2-06"
        )["anchors"][0]
        self.assertEqual("ae2:cable_anchor", durable_face_fallback["face_parts"][0]["id"])
        standalone_terminal = next(
            case for case in manifest["cases"] if case["case_id"] == "ae2-m2-09"
        )["anchors"][0]
        self.assertIsNone(standalone_terminal.get("cable_id"))
        self.assertEqual("missing-center-part", standalone_terminal["fallback_reason"])
        same_color = next(
            case
            for case in manifest["cases"]
            if case["label"] == "compatible-red-covered-to-smart"
        )
        self.assertEqual(2, len(same_color["anchors"]))
        self.assertEqual(
            [22, 22],
            [anchor["expected_triangle_count"] for anchor in same_color["anchors"]],
        )
        self.assertEqual(
            ["covered", "covered"],
            [
                anchor["expected_connections"][0]["effective_family"]
                for anchor in same_color["anchors"]
            ],
        )

    def test_m3f_invalid_pylon_components_are_custom_unformed_and_fail_closed(self) -> None:
        manifest = json.loads(CASES_PATH.read_text(encoding="utf-8"))
        components = [
            case
            for case in manifest["cases"]
            if case["case_id"] in {"ae2-m3f-06", "ae2-m3f-07"}
        ]
        self.assertEqual([3, 4], [len(case["anchors"]) for case in components])
        self.assertTrue(all(not case["fixture_blocks"] for case in components))
        for anchor in (
            anchor for case in components for anchor in case["anchors"]
        ):
            self.assertEqual("custom-m3f", anchor["expected_path"])
            self.assertEqual("none", anchor["pylon_axis_position"])
            self.assertEqual(24, anchor["expected_triangle_count"])
            self.assertEqual(
                {
                    "ae2:block/spatial_pylon/base": 12,
                    "ae2:block/spatial_pylon/dim": 12,
                },
                anchor["expected_material_triangles"],
            )
            self.assertNotIn("fallback_reason", anchor)

        def assert_rejected(name: str, mutation) -> None:
            changed = json.loads(CASES_PATH.read_text(encoding="utf-8"))
            mutation(changed)
            path = Path(self.temporary.name) / f"bad-m3f-component-{name}.json"
            path.write_text(
                json.dumps(changed, sort_keys=True, separators=(",", ":")),
                encoding="utf-8",
            )
            with self.assertRaises(analyze_prbm.EvidenceError):
                analyze_prbm.parse_cases(path)

        def component_anchor(changed: dict[str, object]) -> dict[str, object]:
            return next(
                case
                for case in changed["cases"]
                if case["case_id"] == "ae2-m3f-06"
            )["anchors"][0]

        mutations = (
            (
                "formed-role",
                lambda changed: component_anchor(changed).update(
                    pylon_axis_position="start"
                ),
            ),
            (
                "fallback",
                lambda changed: component_anchor(changed).update(
                    expected_path="stock-fallback-m3f",
                    expected_triangle_count=0,
                    expected_material_triangles={},
                    fallback_reason="perpendicular-pylon-neighbors",
                ),
            ),
            (
                "missing-component-anchor",
                lambda changed: next(
                    case
                    for case in changed["cases"]
                    if case["case_id"] == "ae2-m3f-07"
                )["anchors"].pop(),
            ),
            (
                "scan-cap",
                lambda changed: changed["profile"]["supported_m3_completion"][
                    "spatial_pylon_axis_scan_bounds"
                ].update(maximum_axis_scan_blocks=255),
            ),
        )
        for name, mutation in mutations:
            with self.subTest(name=name):
                assert_rejected(name, mutation)

    def test_schema3_manifest_and_normalized_m2_report_remain_compatible(self) -> None:
        schema3_path = Path(self.temporary.name) / "m2-cases.json"
        schema3_path.write_text(
            json.dumps(
                _schema3_m2_manifest(),
                indent=2,
                sort_keys=True,
                ensure_ascii=True,
            )
            + "\n",
            encoding="utf-8",
        )
        contract, evidence = analyze_prbm.parse_cases(schema3_path)
        report = analyze_prbm.analyze(self.map_root, schema3_path)
        expected = json.loads(EXPECTED_PATH.read_text(encoding="utf-8"))

        self.assertEqual(3, contract.schema_version)
        self.assertEqual(3, evidence["schema_version"])
        self.assertEqual(62, len(contract.cases))
        self.assertEqual(290, sum(len(case.anchors) for case in contract.cases))
        self.assertEqual(3, report["schema_version"])
        self.assertEqual(
            "resolved-resource-path-v3-m2-layout",
            report["format_contract"]["semantic_signature_schema"],
        )
        self.assertNotIn("m2_regression", report)
        self.assertNotIn("drive_component_insensitivity", report)
        self.assertEqual(
            expected["m2_regression_summary"],
            {
                key: report["summary"][key]
                for key in expected["m2_regression_summary"]
            },
        )
        self.assertEqual(
            {
                case_id: count
                for case_id, count in expected["case_triangle_counts"].items()
                if case_id.startswith(("ae2-m1-", "ae2-m2-"))
            },
            {case["case_id"]: case["triangle_count"] for case in report["cases"]},
        )

    def test_frozen_schema4_m3a_enabled_and_stock_signatures_are_retained(self) -> None:
        schema4_path = Path(self.temporary.name) / "m3a-cases.json"
        schema4_path.write_text(
            json.dumps(_schema4_m3a_manifest(), indent=2, sort_keys=True) + "\n",
            encoding="utf-8",
        )
        contract, evidence = analyze_prbm.parse_cases(schema4_path)
        enabled = analyze_prbm.analyze(self.map_root, schema4_path)
        expected = json.loads(EXPECTED_PATH.read_text(encoding="utf-8"))

        self.assertEqual(4, contract.schema_version)
        self.assertEqual(4, evidence["schema_version"])
        self.assertEqual(323, sum(len(case.anchors) for case in contract.cases))
        self.assertEqual(
            expected["m3a_regression_summary"],
            {
                key: enabled["summary"][key]
                for key in expected["m3a_regression_summary"]
            },
        )

        stock_root = Path(self.temporary.name) / "schema4-stock" / "map"
        build_fixture(stock_root, stock_baseline=True)
        stock = analyze_prbm.analyze(
            stock_root, schema4_path, stock_baseline=True
        )
        self.assertEqual(
            expected["m3a_regression_stock_summary"], stock["summary"]
        )

    def test_extension_disabled_mode_keeps_m3a_and_m3c_but_zeros_m3b(self) -> None:
        root = Path(self.temporary.name) / "extension-disabled" / "map"
        build_fixture(root, extension_disabled=True)
        first = analyze_prbm.analyze(
            root, CASES_PATH, extension_disabled=True
        )
        second = analyze_prbm.analyze(
            root, CASES_PATH, extension_disabled=True
        )
        expected = json.loads(EXPECTED_PATH.read_text(encoding="utf-8"))

        self.assertEqual(first, second)
        self.assertEqual("extension-disabled", first["mode"])
        self.assertEqual(21_534, first["summary"]["selected_triangle_count"])
        self.assertEqual(21_524, first["summary"]["custom_triangle_count"])
        self.assertEqual(547, first["summary"]["custom_anchor_count"])
        self.assertEqual(210, first["summary"]["selected_resource_count"])
        self.assertEqual(13, first["summary"]["stock_fallback_anchor_count"])
        self.assertEqual(36, first["summary"]["extension_disabled_anchor_count"])
        self.assertEqual(
            expected["m3a_regression_extension_disabled_summary"],
            first["m3a_regression"],
        )
        self.assertNotIn("extended_drive_component_insensitivity", first)
        self.assertNotIn("extended_drive_front_back_mirror", first)
        self.assertEqual(47, first["connected_glass"]["anchor_count"])
        self.assertEqual(776, first["connected_glass"]["triangle_count"])
        options = analyze_prbm.parse_args(
            ("--map-root", str(root), "--extension-disabled")
        )
        self.assertTrue(options.extension_disabled)
        with self.assertRaisesRegex(
            analyze_prbm.EvidenceError, "mutually exclusive"
        ):
            analyze_prbm.analyze(
                root,
                CASES_PATH,
                stock_baseline=True,
                extension_disabled=True,
            )

    def test_glass_disabled_mode_freezes_m3b_and_zeros_all_m3c_anchors(self) -> None:
        root = Path(self.temporary.name) / "glass-disabled" / "map"
        build_fixture(root, glass_disabled=True)
        first = analyze_prbm.analyze(root, CASES_PATH, glass_disabled=True)
        second = analyze_prbm.analyze(root, CASES_PATH, glass_disabled=True)
        self.assertEqual(first, second)
        self.assertEqual("glass-disabled", first["mode"])
        self.assertEqual(25_814, first["summary"]["selected_triangle_count"])
        self.assertEqual(25_804, first["summary"]["custom_triangle_count"])
        self.assertEqual(532, first["summary"]["custom_anchor_count"])
        self.assertEqual(199, first["summary"]["selected_resource_count"])
        self.assertEqual(17, first["summary"]["stock_fallback_anchor_count"])
        self.assertEqual(47, first["summary"]["glass_disabled_anchor_count"])
        self.assertEqual(0, first["summary"]["glass_disabled_triangle_count"])
        self.assertNotIn("connected_glass", first)
        self.assertIn("extended_drive_component_insensitivity", first)
        self.assertIn("extended_drive_front_back_mirror", first)
        enabled = analyze_prbm.analyze(self.map_root, CASES_PATH)
        for key in (
            "selected_triangle_count",
            "material_signature",
            "shape_signature",
            "geometry_signature",
            "attribute_signature",
        ):
            self.assertEqual(
                enabled["m3b_regression"][key],
                first["m3b_regression"][key],
                key,
            )
        options = analyze_prbm.parse_args(
            ("--map-root", str(root), "--glass-disabled")
        )
        self.assertTrue(options.glass_disabled)
        with self.assertRaisesRegex(analyze_prbm.EvidenceError, "mutually exclusive"):
            analyze_prbm.analyze(
                root,
                CASES_PATH,
                extension_disabled=True,
                glass_disabled=True,
            )

        leaked = Path(self.temporary.name) / "glass-disabled-leak" / "map"
        build_fixture(leaked, glass_disabled=True, leak_glass_disabled=True)
        with self.assertRaisesRegex(
            analyze_prbm.EvidenceError, "glass-disabled anchor .* expected zero"
        ):
            analyze_prbm.analyze(leaked, CASES_PATH, glass_disabled=True)

    def test_crafting_disabled_mode_retains_schema6_and_quantum_but_zeros_m3d(self) -> None:
        root = Path(self.temporary.name) / "crafting-disabled" / "map"
        build_fixture(root, crafting_disabled=True)
        first = analyze_prbm.analyze(
            root, CASES_PATH, crafting_disabled=True
        )
        second = analyze_prbm.analyze(
            root, CASES_PATH, crafting_disabled=True
        )
        self.assertEqual(first, second)
        self.assertEqual("crafting-disabled", first["mode"])
        self.assertEqual(22_284, first["summary"]["selected_triangle_count"])
        self.assertEqual(22_274, first["summary"]["custom_triangle_count"])
        self.assertEqual(494, first["summary"]["custom_anchor_count"])
        self.assertEqual(203, first["summary"]["selected_resource_count"])
        self.assertEqual(16, first["summary"]["stock_fallback_anchor_count"])
        self.assertEqual(86, first["summary"]["crafting_disabled_anchor_count"])
        self.assertEqual(0, first["summary"]["crafting_disabled_triangle_count"])
        self.assertEqual(19_462, first["schema8_regression"]["selected_triangle_count"])
        self.assertEqual(18_274, first["schema6_regression"]["selected_triangle_count"])
        self.assertEqual(1_188, first["quantum_bridge"]["custom_triangle_count"])
        self.assertEqual(0, first["formed_crafting"]["nonzero_custom_anchor_count"])
        self.assertEqual(86, first["formed_crafting"]["route_disabled_anchor_count"])
        options = analyze_prbm.parse_args(
            ("--map-root", str(root), "--crafting-disabled")
        )
        self.assertTrue(options.crafting_disabled)

        leaked = Path(self.temporary.name) / "crafting-disabled-leak" / "map"
        build_fixture(
            leaked,
            crafting_disabled=True,
            leak_crafting_disabled=True,
        )
        with self.assertRaisesRegex(
            analyze_prbm.EvidenceError, "crafting-disabled anchor .* expected zero"
        ):
            analyze_prbm.analyze(
                leaked, CASES_PATH, crafting_disabled=True
            )

    def test_m3d_formed_crafting_topology_paint_and_evidence_status_are_exact(self) -> None:
        report = analyze_prbm.analyze(self.map_root, CASES_PATH)
        cases = [
            case
            for case in report["cases"]
            if case["case_id"].startswith("ae2-m3d-")
        ]
        anchors = [anchor for case in cases for anchor in case["anchors"]]
        self.assertEqual(9, len(cases))
        self.assertEqual(86, len(anchors))
        self.assertEqual(
            [600, 142, 152, 174, 184, 234, 304, 2_516, 0],
            [case["triangle_count"] for case in cases],
        )
        custom = [
            anchor for anchor in anchors if anchor["expected_path"] == "custom-m3d"
        ]
        self.assertEqual(85, len(custom))
        self.assertEqual(4_306, sum(anchor["triangle_count"] for anchor in custom))
        self.assertEqual(
            84, report["formed_crafting"]["nonzero_custom_anchor_count"]
        )
        center = next(
            anchor
            for anchor in custom
            if anchor["position"] == {"x": 305, "y": 101, "z": 270}
        )
        self.assertEqual(0, center["triangle_count"])
        self.assertEqual(
            "fully-enclosed-zero-geometry",
            center["contract"]["formed_crafting"]["evidence_status"],
        )
        self.assertEqual(
            "not-renderer-provenance-distinguishable-in-prbm",
            center["contract"]["formed_crafting"]["renderer_provenance_status"],
        )
        monitors = [
            anchor["contract"]["formed_crafting"]
            for anchor in custom
            if anchor["contract"]["formed_crafting"]["kind"] == "monitor"
        ]
        self.assertEqual(set(range(17)), {monitor["painted_color_ordinal"] for monitor in monitors})
        self.assertEqual(set(analyze_prbm.DIRECTION_VECTORS), {monitor["facing"] for monitor in monitors})
        self.assertEqual(set(range(4)), {monitor["spin"] for monitor in monitors})
        powered = [
            anchor["contract"]["formed_crafting"]
            for anchor in custom
            if anchor["contract"]["formed_crafting"]["powered"]
        ]
        self.assertEqual(8, len(powered))
        self.assertGreater(sum(item["fullbright_triangle_count"] for item in powered), 0)

    def test_m3d_geometry_material_uv_color_ao_and_light_drift_fail_closed(self) -> None:
        corruptions = (
            ("geometry", {"corrupt_crafting_geometry": True}),
            ("winding", {"corrupt_crafting_winding": True}),
            ("uv", {"corrupt_crafting_uv": True}),
            ("material", {"corrupt_crafting_material": True}),
            ("rgb", {"corrupt_crafting_rgb": True}),
            ("ao", {"corrupt_crafting_ao": True}),
            ("light", {"corrupt_crafting_light": True}),
        )
        for name, options in corruptions:
            with self.subTest(name=name):
                root = Path(self.temporary.name) / f"corrupt-crafting-{name}" / "map"
                build_fixture(root, **options)
                with self.assertRaises(analyze_prbm.EvidenceError):
                    analyze_prbm.analyze(root, CASES_PATH)

    def test_m3d_manifest_state_kind_power_paint_and_frozen_prefix_fail_closed(self) -> None:
        def assert_rejected(mutator) -> None:
            manifest = json.loads(CASES_PATH.read_text(encoding="utf-8"))
            mutator(manifest)
            path = Path(self.temporary.name) / f"bad-m3d-{id(mutator)}.json"
            path.write_text(
                json.dumps(manifest, sort_keys=True, separators=(",", ":")),
                encoding="utf-8",
            )
            with self.assertRaises(analyze_prbm.EvidenceError):
                analyze_prbm.parse_cases(path)

        m3d = lambda manifest: [
            case for case in manifest["cases"] if case.get("milestone") == "M3d"
        ]
        mutations = (
            lambda manifest: m3d(manifest)[0]["anchors"][0]["block_state"].update(formed=False),
            lambda manifest: m3d(manifest)[0]["anchors"][0].update(crafting_kind="unit"),
            lambda manifest: m3d(manifest)[5]["anchors"][0]["block_state"].update(powered=False),
            lambda manifest: m3d(manifest)[7]["anchors"][0].update(painted_color_ordinal=15),
            lambda manifest: m3d(manifest)[0]["anchors"][0]["expected_crafting_faces"][0]["primitives"][0].update(bounds_sixteenths=[0, 0, 0, 1, 1, 1]),
            lambda manifest: manifest["m3d_summary"].update(custom_triangle_count=4_305),
            lambda manifest: manifest["cases"][92]["anchors"][0]["expected_glass_faces"][0].update(frame_mask="0001"),
        )
        for mutation in mutations:
            assert_rejected(mutation)

    def test_m3e_three_plane_quantum_topology_materials_and_static_off_contract_are_exact(self) -> None:
        report = analyze_prbm.analyze(self.map_root, CASES_PATH)
        cases = [
            case for case in report["cases"] if case["case_id"].startswith("ae2-m3e-")
        ]
        anchors = [anchor for case in cases for anchor in case["anchors"]]
        self.assertEqual(3, len(cases))
        self.assertEqual(27, len(anchors))
        self.assertEqual([396, 396, 396], [case["triangle_count"] for case in cases])
        self.assertEqual(1_188, sum(anchor["triangle_count"] for anchor in anchors))
        self.assertEqual(
            {"xz", "xy", "yz"},
            {
                anchor["contract"]["quantum_bridge"]["plane"]
                for anchor in anchors
            },
        )
        self.assertEqual(
            {"link": 3, "corner": 12, "edge": 12},
            {
                role: sum(
                    anchor["contract"]["quantum_bridge"]["role"] == role
                    for anchor in anchors
                )
                for role in ("link", "corner", "edge")
            },
        )
        expected_per_bridge = {
            analyze_prbm.QUANTUM_LINK_RESOURCE: 12,
            analyze_prbm.QUANTUM_GLASS_RESOURCE: 48,
            analyze_prbm.QUANTUM_COVERED_RESOURCE: 144,
            analyze_prbm.QUANTUM_RING_RESOURCE: 192,
        }
        for case in cases:
            actual_materials: dict[str, int] = {}
            for anchor in case["anchors"]:
                for resource, count in anchor["contract"][
                    "material_triangle_counts"
                ].items():
                    actual_materials[resource] = actual_materials.get(resource, 0) + count
            self.assertEqual(expected_per_bridge, actual_materials)
        for anchor in anchors:
            contract = anchor["contract"]["quantum_bridge"]
            self.assertTrue(contract["formed"])
            self.assertFalse(contract["waterlogged"])
            self.assertEqual([255, 255, 255], contract["rgb_u8"])
            self.assertEqual(255, contract["ambient_occlusion_raw_u8"])
            self.assertEqual(
                "world-derived-own-and-outward-face-maximum",
                contract["world_light_policy"],
            )
            self.assertEqual(
                analyze_prbm.QUANTUM_STATIC_POLICY,
                contract["power_overlay_policy"],
            )
            self.assertEqual(
                analyze_prbm.QUANTUM_PARTICLE_POLICY,
                contract["particle_policy"],
            )
            self.assertTrue(all(item["triangle_count"] == 12 for item in contract["primitives"]))
            self.assertEqual(set(analyze_prbm.DIRECTION_VECTORS), set(contract["world_light"]))
            self.assertTrue(
                all(
                    0 <= light[channel] <= 15
                    for light in contract["world_light"].values()
                    for channel in ("blocklight_raw_i8", "sunlight_raw_i8")
                )
            )
        quantum_positions = {
            (anchor["position"]["x"], anchor["position"]["y"], anchor["position"]["z"])
            for anchor in anchors
        }
        self.assertIn((287, 100, 271), quantum_positions)
        self.assertTrue({287, 288} <= {position[0] for position in quantum_positions})
        manifest = json.loads(CASES_PATH.read_text(encoding="utf-8"))
        selected = set(manifest["profile"]["selected_resources"])
        self.assertTrue(set(analyze_prbm.QUANTUM_RESOURCES) <= selected)
        self.assertFalse(any("quantum_ring_light" in resource for resource in selected))
        self.assertFalse(
            any("quantum_ring_light" in resource for resource in report["summary"]["custom_selected_resources"])
        )
        self.assertEqual(
            expected_per_bridge,
            report["quantum_bridge"]["per_bridge"]["material_triangle_counts"],
        )

    def test_m3e_geometry_winding_uv_material_rgb_ao_and_light_drift_fail_closed(self) -> None:
        corruptions = (
            ("geometry", {"corrupt_quantum_geometry": True}),
            ("winding", {"corrupt_quantum_winding": True}),
            ("uv", {"corrupt_quantum_uv": True}),
            ("material", {"corrupt_quantum_material": True}),
            ("rgb", {"corrupt_quantum_rgb": True}),
            ("ao", {"corrupt_quantum_ao": True}),
            ("light", {"corrupt_quantum_light": True}),
        )
        for name, options in corruptions:
            with self.subTest(name=name):
                root = Path(self.temporary.name) / f"corrupt-quantum-{name}" / "map"
                build_fixture(root, **options)
                with self.assertRaises(analyze_prbm.EvidenceError):
                    analyze_prbm.analyze(root, CASES_PATH)

    def test_m3e_manifest_role_state_topology_resources_and_policy_fail_closed(self) -> None:
        def assert_rejected(name: str, mutator) -> None:
            manifest = json.loads(CASES_PATH.read_text(encoding="utf-8"))
            mutator(manifest)
            path = Path(self.temporary.name) / f"bad-m3e-{name}.json"
            path.write_text(
                json.dumps(manifest, sort_keys=True, separators=(",", ":")),
                encoding="utf-8",
            )
            with self.assertRaises(analyze_prbm.EvidenceError):
                analyze_prbm.parse_cases(path)

        def m3e(manifest: dict[str, object]) -> list[dict[str, object]]:
            return [
                case
                for case in manifest["cases"]
                if case.get("milestone") == "M3e"
            ]

        mutations = (
            ("role", lambda manifest: m3e(manifest)[0]["anchors"][0].update(quantum_role="edge")),
            ("formed", lambda manifest: m3e(manifest)[0]["anchors"][0]["block_state"].update(formed=False)),
            ("plane", lambda manifest: m3e(manifest)[0]["anchors"][0].update(quantum_plane="xy")),
            ("topology", lambda manifest: m3e(manifest)[0]["anchors"][0]["expected_connections"].pop()),
            ("position", lambda manifest: m3e(manifest)[0]["anchors"][0]["position"].update(x=285)),
            ("cuboid", lambda manifest: m3e(manifest)[0]["anchors"][0]["expected_quantum_primitives"][0].update(bounds_sixteenths=[1, 2, 2, 14, 14, 14])),
            ("material", lambda manifest: m3e(manifest)[0]["anchors"][0]["expected_material_triangles"].update({analyze_prbm.QUANTUM_RING_RESOURCE: 11})),
            ("power-policy", lambda manifest: manifest["profile"]["supported_quantum_bridge"].update(power_overlay_policy="powered")),
            ("particle-policy", lambda manifest: manifest["m3e_summary"].update(particle_policy="included")),
            ("light-texture", lambda manifest: manifest["profile"]["selected_resources"].append("ae2:block/quantum_ring_light")),
        )
        for name, mutation in mutations:
            with self.subTest(name=name):
                assert_rejected(name, mutation)

    def test_quantum_disabled_mode_keeps_exact_schema7_and_rejects_leaks(self) -> None:
        root = Path(self.temporary.name) / "quantum-disabled" / "map"
        build_fixture(root, quantum_disabled=True)
        first = analyze_prbm.analyze(root, CASES_PATH, quantum_disabled=True)
        second = analyze_prbm.analyze(root, CASES_PATH, quantum_disabled=True)
        enabled = analyze_prbm.analyze(self.map_root, CASES_PATH)
        self.assertEqual(first, second)
        self.assertEqual("quantum-disabled", first["mode"])
        self.assertEqual(597, first["summary"]["anchor_count"])
        self.assertEqual(552, first["summary"]["custom_anchor_count"])
        self.assertEqual(25_392, first["summary"]["custom_triangle_count"])
        self.assertEqual(216, first["summary"]["custom_selected_resource_count"])
        self.assertEqual(25_402, first["summary"]["selected_triangle_count"])
        self.assertEqual(27, first["summary"]["quantum_disabled_anchor_count"])
        self.assertEqual(0, first["quantum_bridge"]["nonzero_custom_anchor_count"])
        self.assertEqual(27, first["quantum_bridge"]["route_disabled_anchor_count"])
        self.assertEqual(enabled["schema7_regression"], first["schema7_regression"])
        options = analyze_prbm.parse_args(
            ("--map-root", str(root), "--quantum-disabled")
        )
        self.assertTrue(options.quantum_disabled)
        with self.assertRaisesRegex(analyze_prbm.EvidenceError, "mutually exclusive"):
            analyze_prbm.analyze(
                root,
                CASES_PATH,
                crafting_disabled=True,
                quantum_disabled=True,
            )
        leaked = Path(self.temporary.name) / "quantum-disabled-leak" / "map"
        build_fixture(
            leaked,
            quantum_disabled=True,
            leak_quantum_disabled=True,
        )
        with self.assertRaisesRegex(
            analyze_prbm.EvidenceError, "quantum-disabled anchor .* expected zero"
        ):
            analyze_prbm.analyze(leaked, CASES_PATH, quantum_disabled=True)

    def test_m3c_frame_masks_use_ae2_numeric_bit_order(self) -> None:
        manifest = _schema6_m3c_manifest()
        center = next(
            anchor
            for case in manifest["cases"]
            if case["case_id"] == "ae2-m3c-02"
            for anchor in case["anchors"]
            if anchor["position"] == {"x": 214, "y": 100, "z": 290}
        )

        self.assertEqual(
            ["down", "up"],
            [connection["direction"] for connection in center["expected_connections"]],
        )
        self.assertEqual(
            {"north": "1010", "south": "1010", "west": "1010", "east": "1010"},
            {
                face["direction"]: face["frame_mask"]
                for face in center["expected_glass_faces"]
            },
        )
        self.assertEqual(
            {
                "ae2:block/glass/quartz_glass_a": 8,
                "ae2:block/glass/quartz_glass_frame1010": 8,
            },
            center["expected_material_triangles"],
        )

        for face in center["expected_glass_faces"]:
            face["frame_mask"] = "0101"
            face["frame_resource"] = "ae2:block/glass/quartz_glass_frame0101"
        center["expected_material_triangles"] = {
            "ae2:block/glass/quartz_glass_a": 8,
            "ae2:block/glass/quartz_glass_frame0101": 8,
        }
        reversed_path = Path(self.temporary.name) / "reversed-frame-bit-order.json"
        reversed_path.write_text(
            json.dumps(manifest, sort_keys=True, separators=(",", ":")),
            encoding="utf-8",
        )
        with self.assertRaisesRegex(analyze_prbm.EvidenceError, "metadata differs"):
            analyze_prbm.parse_cases(reversed_path)

    def test_m3c_connected_glass_matrix_masks_resources_and_context_are_exact(self) -> None:
        report = analyze_prbm.analyze(self.map_root, CASES_PATH)
        cases = [
            case for case in report["cases"] if case["case_id"].startswith("ae2-m3c-")
        ]
        anchors = [anchor for case in cases for anchor in case["anchors"]]
        self.assertEqual(11, len(cases))
        self.assertEqual(47, len(anchors))
        self.assertEqual(
            [48, 56, 56, 56, 72, 88, 48, 116, 96, 120, 20],
            [case["triangle_count"] for case in cases],
        )
        self.assertEqual(776, sum(anchor["triangle_count"] for anchor in anchors))
        self.assertEqual(
            {"ae2:quartz_glass", "ae2:quartz_vibrant_glass"},
            {
                anchor["contract"]["connected_glass"]["block_id"]
                for anchor in anchors
            },
        )
        self.assertEqual(
            {0, 1, 2, 3},
            {
                anchor["contract"]["connected_glass"]["texture_index"]
                for anchor in anchors
            },
        )
        connected = report["connected_glass"]
        self.assertEqual(19, connected["selected_resource_count"])
        self.assertEqual(
            set(analyze_prbm.CONNECTED_GLASS_SELECTED_RESOURCES),
            {
                material["resource_path"]
                for anchor in anchors
                for material in anchor["materials"]
            },
        )
        self.assertEqual(
            analyze_prbm.CONNECTED_GLASS_FRAME_OCCURRENCES,
            connected["frame_mask_occurrences"],
        )
        self.assertEqual(2, connected["no_frame_face_count"])
        base_only_faces = [
            face
            for anchor in anchors
            for face in anchor["contract"]["connected_glass"]["faces"].values()
            if face["frame_mask"] == "0000"
        ]
        self.assertEqual(2, len(base_only_faces))
        self.assertTrue(all(face["frame_resource"] is None for face in base_only_faces))
        variant = connected["variant_equivalence"]
        self.assertTrue(variant["validated"])
        expected_variant = json.loads(
            EXPECTED_PATH.read_text(encoding="utf-8")
        )["connected_glass"]["variant_equivalence"]
        for key in ("shape_signature", "material_signature", "geometry_signature"):
            self.assertEqual(
                expected_variant[key],
                variant[key],
                key,
            )
        self.assertNotEqual(
            variant["ordinary_attribute_signature"],
            variant["vibrant_attribute_signature"],
        )
        self.assertEqual(
            "world-derived-with-vibrant-center-emission-floor-15",
            connected["world_light_policy"],
        )

        opaque_case = cases[-1]
        self.assertEqual(20, opaque_case["triangle_count"])
        opaque_anchor = opaque_case["anchors"][0]
        self.assertEqual(
            ["east"],
            opaque_anchor["contract"]["connected_glass"]["opaque_culled_faces"],
        )
        self.assertNotIn(
            "minecraft:block/stone",
            {material["resource_path"] for material in opaque_anchor["materials"]},
        )
        context_tile = next(
            tile for tile in report["tiles"] if tile["tile"] == {"x": 7, "z": 9}
        )
        self.assertEqual(
            10,
            context_tile["triangle_count"] - context_tile["selected_triangle_count"],
        )

    def test_m3c_world_light_is_evidence_but_vibrant_has_emission_floor(self) -> None:
        baseline = analyze_prbm.analyze(self.map_root, CASES_PATH)
        varied_root = Path(self.temporary.name) / "ordinary-world-light" / "map"
        build_fixture(varied_root, vary_glass_world_light=True)
        varied = analyze_prbm.analyze(varied_root, CASES_PATH)

        self.assertEqual(
            baseline["summary"]["geometry_signature"],
            varied["summary"]["geometry_signature"],
        )
        self.assertNotEqual(
            baseline["summary"]["attribute_signature"],
            varied["summary"]["attribute_signature"],
        )
        self.assertEqual(
            baseline["connected_glass"]["nonlighting_topology_signature"],
            varied["connected_glass"]["nonlighting_topology_signature"],
        )

        inconsistent = Path(self.temporary.name) / "inconsistent-face-light" / "map"
        build_fixture(inconsistent, corrupt_glass_face_light=True)
        with self.assertRaisesRegex(
            analyze_prbm.EvidenceError, "base/frame face light is inconsistent"
        ):
            analyze_prbm.analyze(inconsistent, CASES_PATH)

        dim_vibrant = Path(self.temporary.name) / "dim-vibrant" / "map"
        build_fixture(dim_vibrant, corrupt_vibrant_glass_light=True)
        with self.assertRaisesRegex(
            analyze_prbm.EvidenceError, "vibrant blocklight is not 15"
        ):
            analyze_prbm.analyze(dim_vibrant, CASES_PATH)

    def test_m3c_geometry_material_uv_attribute_and_shared_face_drift_fail_closed(self) -> None:
        corruptions = (
            ("position-plane", {"corrupt_glass_geometry": True}, "geometry/corners/material/UV"),
            ("normal-winding", {"corrupt_glass_winding": True}, "expected 24"),
            ("base-uv", {"corrupt_glass_uv": True}, "geometry/corners/material/UV"),
            ("base-material", {"corrupt_glass_material": True}, "material triangle counts"),
            ("frame-material", {"corrupt_glass_frame_material": True}, "material triangle counts"),
            ("ao", {"corrupt_glass_attributes": True}, "wrong color/AO"),
            ("rgb", {"corrupt_glass_rgb": True}, "wrong color/AO"),
            ("shared-face", {"leak_glass_shared_face": True}, "expected 16"),
        )
        for name, options, error in corruptions:
            with self.subTest(name=name):
                root = Path(self.temporary.name) / f"corrupt-glass-{name}" / "map"
                build_fixture(root, **options)
                with self.assertRaisesRegex(analyze_prbm.EvidenceError, error):
                    analyze_prbm.analyze(root, CASES_PATH)

    def test_m3c_manifest_rejects_symmetric_uv_mask_and_summary_drift(self) -> None:
        manifest = _schema6_m3c_manifest()
        first = next(
            case for case in manifest["cases"] if case["case_id"] == "ae2-m3c-01"
        )["anchors"][0]
        selection = first["expected_glass_base_selection"]
        u_offset = selection["u_offset"]
        v_offset = selection["v_offset"]
        symmetric = [
            [u_offset, v_offset],
            [u_offset, 1 - v_offset],
            [1 - u_offset, 1 - v_offset],
            [1 - u_offset, v_offset],
        ]
        selection["uv_corners"] = symmetric
        for face in first["expected_glass_faces"]:
            face["base_uv_corners"] = symmetric
        symmetric_path = Path(self.temporary.name) / "symmetric-inset-uv.json"
        symmetric_path.write_text(
            json.dumps(manifest, sort_keys=True, separators=(",", ":")),
            encoding="utf-8",
        )
        with self.assertRaisesRegex(analyze_prbm.EvidenceError, "metadata differs"):
            analyze_prbm.parse_cases(symmetric_path)

        manifest = _schema6_m3c_manifest()
        first = next(
            case for case in manifest["cases"] if case["case_id"] == "ae2-m3c-01"
        )["anchors"][0]
        first["expected_glass_faces"][0]["frame_mask"] = "0001"
        mask_path = Path(self.temporary.name) / "wrong-frame-mask.json"
        mask_path.write_text(
            json.dumps(manifest, sort_keys=True, separators=(",", ":")),
            encoding="utf-8",
        )
        with self.assertRaisesRegex(analyze_prbm.EvidenceError, "metadata differs"):
            analyze_prbm.parse_cases(mask_path)

        manifest = _schema6_m3c_manifest()
        manifest["m3c_summary"]["custom_triangle_count"] -= 1
        summary_path = Path(self.temporary.name) / "wrong-m3c-summary.json"
        summary_path.write_text(
            json.dumps(manifest, sort_keys=True, separators=(",", ":")),
            encoding="utf-8",
        )
        with self.assertRaisesRegex(analyze_prbm.EvidenceError, "summary/bounds/floor"):
            analyze_prbm.parse_cases(summary_path)

    def test_m3b_extended_drive_matrix_and_face_semantics_are_exact(self) -> None:
        report = analyze_prbm.analyze(self.map_root, CASES_PATH)
        anchors = [
            anchor
            for case in report["cases"]
            for anchor in case["anchors"]
            if anchor["expected_path"] == "custom-m3b"
        ]
        self.assertEqual(32, len(anchors))
        orientation_states = {
            (
                anchor["contract"]["drive"]["facing"],
                anchor["contract"]["drive"]["spin"],
            )
            for case in report["cases"]
            if case["category"] == "extended-drive-orientation"
            for anchor in case["anchors"]
        }
        self.assertEqual(
            {
                (facing, spin)
                for facing in analyze_prbm.DRIVE_ORIENTATION_ANGLES
                for spin in range(4)
            },
            orientation_states,
        )
        chassis = 0
        leds = 0
        models = set()
        occupied_slots = 0
        for anchor in anchors:
            drive = anchor["contract"]["drive"]
            occupied = drive["occupied_slot_count"]
            self.assertEqual("116+16N", drive["triangle_formula"])
            self.assertEqual(116 + 16 * occupied, anchor["triangle_count"])
            chassis += drive["cell_chassis"]["triangle_count"]
            leds += drive["offline_led"]["triangle_count"]
            occupied_slots += occupied
            for slot_number, slot in drive["occupied_slots"].items():
                number = int(slot_number)
                models.add(slot["model_id"])
                self.assertEqual("front" if number < 10 else "back", slot["face"])
                self.assertEqual(number % 10, slot["face_slot"])
                self.assertEqual(6, slot["chassis_triangle_count"])
        self.assertEqual(84, occupied_slots)
        self.assertEqual(504, chassis)
        self.assertEqual(840, leds)
        self.assertEqual(15, len(models))
        expected = json.loads(EXPECTED_PATH.read_text(encoding="utf-8"))
        self.assertEqual(
            expected["extended_drive_component_insensitivity"],
            report["extended_drive_component_insensitivity"],
        )
        self.assertEqual(
            expected["extended_drive_front_back_mirror"],
            report["extended_drive_front_back_mirror"],
        )

    def test_m3b_component_pair_excludes_only_environmental_world_light(self) -> None:
        varied_root = Path(self.temporary.name) / "m3b-component-world-light" / "map"
        build_fixture(varied_root, vary_extended_component_world_light=True)
        varied = analyze_prbm.analyze(varied_root, CASES_PATH)
        baseline = analyze_prbm.analyze(self.map_root, CASES_PATH)

        component_case = next(
            case
            for case in varied["cases"]
            if case["case_id"] == "ae2-m3b-12"
        )
        self.assertNotEqual(
            component_case["anchors"][0]["attribute_signature"],
            component_case["anchors"][1]["attribute_signature"],
        )
        self.assertNotEqual(
            baseline["summary"]["attribute_signature"],
            varied["summary"]["attribute_signature"],
        )
        component_result = varied["extended_drive_component_insensitivity"]
        self.assertTrue(component_result["validated"])
        self.assertEqual(
            "excluded-environment-dependent",
            component_result["world_light_policy"],
        )
        self.assertIn(
            "normalized_geometry_nonlighting_attribute_signature",
            component_result,
        )

    def test_m3b_component_pair_still_requires_exact_fullbright_leds(self) -> None:
        corrupted = Path(self.temporary.name) / "m3b-component-led-light" / "map"
        build_fixture(
            corrupted,
            vary_extended_component_world_light=True,
            corrupt_extended_component_led_light=True,
        )
        with self.assertRaisesRegex(
            analyze_prbm.EvidenceError,
            "wrong offline LED attribute count",
        ):
            analyze_prbm.analyze(corrupted, CASES_PATH)

    def test_m3b_manifest_rejects_orientation_and_artifact_drift(self) -> None:
        manifest = _schema5_m3b_manifest()
        manifest["cases"][-1]["route"] = "ae2:drive"
        bad_route = Path(self.temporary.name) / "bad-m3b-route.json"
        bad_route.write_text(
            json.dumps(manifest, sort_keys=True, separators=(",", ":")),
            encoding="utf-8",
        )
        with self.assertRaisesRegex(analyze_prbm.EvidenceError, "milestone/route"):
            analyze_prbm.parse_cases(bad_route)

        manifest = _schema5_m3b_manifest()
        first = next(
            case for case in manifest["cases"] if case["case_id"] == "ae2-m3b-01"
        )["anchors"][0]
        first["expected_drive_models"]["slots"][10]["orientation"]["facing"] = "north"
        bad_orientation = Path(self.temporary.name) / "bad-m3b-orientation.json"
        bad_orientation.write_text(
            json.dumps(manifest, sort_keys=True, separators=(",", ":")),
            encoding="utf-8",
        )
        with self.assertRaisesRegex(analyze_prbm.EvidenceError, "slot 10 metadata"):
            analyze_prbm.parse_cases(bad_orientation)

        manifest = _schema5_m3b_manifest()
        manifest["profile"]["extension_profiles"][0]["artifact"]["sha256"] = "0" * 64
        bad_artifact = Path(self.temporary.name) / "bad-m3b-artifact.json"
        bad_artifact.write_text(
            json.dumps(manifest, sort_keys=True, separators=(",", ":")),
            encoding="utf-8",
        )
        with self.assertRaisesRegex(analyze_prbm.EvidenceError, "artifact contract"):
            analyze_prbm.parse_cases(bad_artifact)

    def test_stock_device_fallbacks_must_own_zero_triangles(self) -> None:
        report = analyze_prbm.analyze(self.map_root, CASES_PATH)
        self.assertEqual(17, report["summary"]["stock_fallback_anchor_count"])
        self.assertEqual(0, report["summary"]["stock_fallback_triangle_count"])

        leaked = Path(self.temporary.name) / "leaked-fallback" / "map"
        build_fixture(leaked, leak_device_fallback=True)
        with self.assertRaisesRegex(
            analyze_prbm.EvidenceError, "fallback anchor .* expected zero"
        ):
            analyze_prbm.analyze(leaked, CASES_PATH)

        leaked_m2 = Path(self.temporary.name) / "leaked-m2-fallback" / "map"
        build_fixture(leaked_m2, leak_m2_fallback=True)
        with self.assertRaisesRegex(
            analyze_prbm.EvidenceError, "fallback anchor .* expected zero"
        ):
            analyze_prbm.analyze(leaked_m2, CASES_PATH)

        leaked_m3 = Path(self.temporary.name) / "leaked-m3-fallback" / "map"
        build_fixture(leaked_m3, leak_m3_fallback=True)
        with self.assertRaisesRegex(
            analyze_prbm.EvidenceError, "fallback anchor .* expected zero"
        ):
            analyze_prbm.analyze(leaked_m3, CASES_PATH)

        leaked_m3b = Path(self.temporary.name) / "leaked-m3b-fallback" / "map"
        build_fixture(leaked_m3b, leak_m3b_fallback=True)
        with self.assertRaisesRegex(
            analyze_prbm.EvidenceError, "fallback anchor .* expected zero"
        ):
            analyze_prbm.analyze(leaked_m3b, CASES_PATH)

        leaked_m3d = Path(self.temporary.name) / "leaked-m3d-fallback" / "map"
        build_fixture(leaked_m3d, leak_m3d_fallback=True)
        with self.assertRaisesRegex(
            analyze_prbm.EvidenceError, "fallback anchor .* expected zero"
        ):
            analyze_prbm.analyze(leaked_m3d, CASES_PATH)

    def test_m3f_exact_geometry_orientation_topology_and_face_light_are_reported(self) -> None:
        report = analyze_prbm.analyze(self.map_root, CASES_PATH)
        cases = [
            case for case in report["cases"] if case["case_id"].startswith("ae2-m3f-")
        ]
        anchors = [anchor for case in cases for anchor in case["anchors"]]
        custom = [anchor for anchor in anchors if anchor["expected_path"] == "custom-m3f"]
        self.assertEqual(7, len(cases))
        self.assertEqual(78, len(anchors))
        self.assertEqual(78, len(custom))
        self.assertEqual(
            [50, 288, 204, 1_872, 240, 72, 96],
            [case["triangle_count"] for case in cases],
        )
        for anchor in custom:
            contract = anchor["contract"]["m3_completion"]
            self.assertEqual("2^-16-block-and-uv", contract["geometry_quantum"])
            self.assertEqual(64, len(contract["geometry_q16_signature"]))
            self.assertEqual(
                "world-derived-own-and-outward-face-maximum",
                contract["world_light_policy"],
            )
            self.assertTrue(contract["validated"])
            self.assertTrue(contract["world_light"])
            self.assertTrue(
                all(
                    0 <= light[channel] <= 15
                    for light in contract["world_light"].values()
                    for channel in ("blocklight_raw_i8", "sunlight_raw_i8")
                )
            )

        by_block: dict[str, list[str]] = {}
        for anchor in custom:
            contract = anchor["contract"]["m3_completion"]
            by_block.setdefault(contract["block_id"], []).append(
                contract["geometry_q16_signature"]
            )
        self.assertEqual(4, len(set(by_block["ae2:sky_stone_chest"])))
        self.assertEqual(4, len(set(by_block["ae2:smooth_sky_stone_chest"])))
        self.assertEqual(6, len(set(by_block["ae2:crank"])))
        self.assertEqual(24, len(set(by_block["ae2:inscriber"])))

        # The lock is deliberately non-cubic (dx=2,dz=1), so these exact
        # east/south UV domains guard the ModelPart u4/u5 strip calculation.
        chest = analyze_prbm._m3f_chest("ae2:block/skychest", "south")
        lock_east = [item for item in chest if item.role == "chest:lock:east"]
        lock_south = [item for item in chest if item.role == "chest:lock:south"]
        self.assertEqual(
            {(3, 1), (4, 1), (3, 5), (4, 5)},
            {(round(u * 64), round(v * 64)) for item in lock_east for u, v in item.uvs},
        )
        self.assertEqual(
            {(4, 1), (6, 1), (4, 5), (6, 5)},
            {(round(u * 64), round(v * 64)) for item in lock_south for u, v in item.uvs},
        )

    def test_m3f_family_builders_match_independent_source_lock_goldens(self) -> None:
        quantum = analyze_prbm.SHAPE_QUANTUM
        low = analyze_prbm.PaintSplotchContract(
            0, "down", "up", "ae2:block/paint1", (1, 2, 3)
        )
        high = analyze_prbm.PaintSplotchContract(
            -1, "down", "up", "ae2:block/paint1", (1, 2, 3)
        )
        self.assertEqual(
            [
                (0, 66, 0, 0, 0), (0, 66, 13_107, 0, 65_536),
                (13_107, 66, 13_107, 65_536, 65_536),
                (13_107, 66, 0, 65_536, 0),
            ],
            [
                tuple(round(value / quantum) for value in vertex)
                for vertex in analyze_prbm._m3f_paint_vertices(low, 0)
            ],
        )
        self.assertEqual(
            [
                (52_429, 1_376, 52_429, 0, 0),
                (52_429, 1_376, 65_536, 0, 65_536),
                (65_536, 1_376, 65_536, 65_536, 65_536),
                (65_536, 1_376, 52_429, 65_536, 0),
            ],
            [
                tuple(round(value / quantum) for value in vertex)
                for vertex in analyze_prbm._m3f_paint_vertices(high, 20)
            ],
        )

        crank = analyze_prbm._m3f_json_model(
            analyze_prbm.M3F_CRANK_MODEL,
            "ae2:block/crank",
            analyze_prbm._drive_rotation_matrix("north", 0),
            "crank",
        )
        expected_crank_roles = {
            *(f"crank:base:{face}" for face in analyze_prbm.M3F_MODEL_FACE_ORDER),
            *(f"crank:shaft:{face}" for face in ("down", "up", "south", "west", "east")),
            *(f"crank:handle:{face}" for face in analyze_prbm.M3F_MODEL_FACE_ORDER),
        }
        self.assertEqual(34, len(crank))
        self.assertEqual(expected_crank_roles, {item.role for item in crank})
        self.assertNotIn("crank:shaft:north", expected_crank_roles)
        shaft_east = [item for item in crank if item.role == "crank:shaft:east"]
        self.assertEqual(
            {(0, 8), (0, 14), (2, 8), (2, 14)},
            {(round(u * 16), round(v * 16)) for item in shaft_east for u, v in item.uvs},
        )
        self.assertEqual(
            {(9, 7, 7), (9, 7, 13), (9, 9, 7), (9, 9, 13)},
            {
                tuple(round(value * 16) for value in point)
                for item in shaft_east for point in item.positions
            },
        )

        shell = analyze_prbm._m3f_json_model(
            analyze_prbm.M3F_INSCRIBER_MODEL,
            "ae2:block/inscriber",
            analyze_prbm._drive_rotation_matrix("north", 0),
            "inscriber-shell",
        )
        stamps = analyze_prbm._m3f_inscriber_stamps(
            analyze_prbm._drive_rotation_matrix("north", 0)
        )
        self.assertEqual(66, len(shell))
        self.assertEqual(33, len({item.role for item in shell}))
        self.assertEqual({"ae2:block/inscriber"}, {item.material for item in shell})
        self.assertEqual(12, len(stamps))
        self.assertEqual({"ae2:block/inscriber_inside"}, {item.material for item in stamps})
        self.assertEqual(
            {5_243, 18_350, 47_186, 60_293},
            {
                round(point[1] / quantum)
                for item in stamps for point in item.positions
            },
        )
        side_stamps = [item for item in stamps if item.role.endswith(("north", "south"))]
        self.assertEqual(
            {125, 325},
            {round(v * 1000) for item in side_stamps for _u, v in item.uvs},
        )

        pylon_samples = {
            ("x", "start", "pylon:x:start:outer:north"): {
                ((0, 0, 0), (1, 1)), ((0, 1, 0), (0, 1)),
                ((1, 0, 0), (1, 0)), ((1, 1, 0), (0, 0)),
            },
            ("y", "end", "pylon:y:end:outer:north"): {
                ((0, 0, 0), (1, 0)), ((0, 1, 0), (1, 1)),
                ((1, 0, 0), (0, 0)), ((1, 1, 0), (0, 1)),
            },
            ("z", "start", "pylon:z:start:outer:east"): {
                ((1, 0, 0), (1, 1)), ((1, 0, 1), (1, 0)),
                ((1, 1, 0), (0, 1)), ((1, 1, 1), (0, 0)),
            },
        }
        for (axis, role, primitive_role), expected in pylon_samples.items():
            rows = [
                item
                for item in analyze_prbm._m3f_pylon(axis, role)
                if item.role == primitive_role
            ]
            actual = {
                (
                    tuple(round(component) for component in point),
                    (round(uv[0]), round(uv[1])),
                )
                for item in rows
                for point, uv in zip(item.positions, item.uvs, strict=True)
            }
            self.assertEqual(expected, actual)

        expected_pylon_signatures = {
            ("x", "none"): "a131f13a920420d4d02cefd202625c7c9706c44598017b8235fceb34963a7b93",
            ("x", "start"): "0450392f857dc18270e5891ab85aed298f28b6a932c9e38be7805db5683d7381",
            ("x", "middle"): "2d4e37ce3da769c66358a53f8b59ebc75d38b5eab769c2bcab51efce5095270a",
            ("x", "end"): "865bd71ea45c1bc1718093d2300c0e054100c0831b42b0c1b90a0b547f1f65b1",
            ("y", "start"): "ee3c922743da9a3df76453b492bcb9606be99c663aaaef97292738c0ee9c6214",
            ("y", "middle"): "b21d262ef08009580929b2e570523c5eee65567190692e3c1b01d80ce1a4a1cf",
            ("y", "end"): "629eb3f276b76937715442843d6fb2ae65babdfcc057c68bfd260707e2fee3be",
            ("z", "start"): "592ef601bf24ddf5ed8beb00d695d5c9bdb7e5c8c9a42d3688c2935cf1dfa9ef",
            ("z", "middle"): "982665c76942493ae1c70fe9a1230dbecbd9be5374ad04f04a9815eb42ae613a",
            ("z", "end"): "fa61fb417884ed754bb779d97a81e9f927f1da56d547163bb76fb0d6167b2dce",
        }
        actual_pylon_signatures = {}
        for state in expected_pylon_signatures:
            keys = sorted(
                analyze_prbm._m3f_geometry_key(item.material, item.positions, item.uvs)[0]
                for item in analyze_prbm._m3f_pylon(*state)
            )
            actual_pylon_signatures[state] = analyze_prbm.sha256_text(
                "m3f-pylon-builder-golden-v1\n"
                + analyze_prbm.canonical_json(keys)
                + "\n"
            )
        self.assertEqual(expected_pylon_signatures, actual_pylon_signatures)
        self.assertEqual(10, len(set(actual_pylon_signatures.values())))

    def test_m3f_each_geometry_attribute_orientation_and_topology_hook_fails_closed(self) -> None:
        corruptions = (
            ("geometry", {"corrupt_m3f_geometry": True}),
            ("winding-normal", {"corrupt_m3f_winding_normal": True}),
            ("uv", {"corrupt_m3f_uv": True}),
            ("material", {"corrupt_m3f_material": True}),
            ("rgb", {"corrupt_m3f_rgb": True}),
            ("ao", {"corrupt_m3f_ao": True}),
            ("face-light", {"corrupt_m3f_face_light": True}),
            ("paint-clamp", {"corrupt_m3f_paint_clamp": True}),
            ("paint-layer", {"corrupt_m3f_paint_layer": True}),
            ("chest-orientation", {"corrupt_m3f_chest_orientation": True}),
            ("crank-orientation", {"corrupt_m3f_crank_orientation": True}),
            ("inscriber-orientation", {"corrupt_m3f_inscriber_orientation": True}),
            ("pylon-topology", {"corrupt_m3f_pylon_topology": True}),
            ("chest-lock-uv", {"corrupt_m3f_chest_lock_uv": True}),
            ("chest-lock-bounds", {"corrupt_m3f_chest_lock_bounds": True}),
            ("chest-texture", {"corrupt_m3f_chest_texture": True}),
            ("crank-shaft-north", {"corrupt_m3f_crank_shaft_north": True}),
            ("crank-handle-placement", {"corrupt_m3f_crank_handle_placement": True}),
            ("crank-uv-rotation", {"corrupt_m3f_crank_uv_rotation": True}),
            ("inscriber-material-split", {"corrupt_m3f_inscriber_material_split": True}),
            ("inscriber-stamp-position", {"corrupt_m3f_inscriber_stamp_position": True}),
            ("inscriber-stamp-uv", {"corrupt_m3f_inscriber_stamp_uv": True}),
            ("pylon-layer-material", {"corrupt_m3f_pylon_layer_material": True}),
            ("pylon-x-uv", {"corrupt_m3f_pylon_x_uv": True}),
            ("pylon-y-uv", {"corrupt_m3f_pylon_y_uv": True}),
            ("pylon-z-uv", {"corrupt_m3f_pylon_z_uv": True}),
        )
        for name, options in corruptions:
            with self.subTest(name=name):
                root = Path(self.temporary.name) / f"corrupt-m3f-{name}" / "map"
                build_fixture(root, **options)
                with self.assertRaises(analyze_prbm.EvidenceError):
                    analyze_prbm.analyze(root, CASES_PATH)

        for attribute in ("ao", "light"):
            for family in ("paint", "chest", "crank", "inscriber", "pylon"):
                with self.subTest(attribute=attribute, family=family):
                    root = (
                        Path(self.temporary.name)
                        / f"corrupt-m3f-{family}-{attribute}"
                        / "map"
                    )
                    build_fixture(
                        root,
                        **{f"corrupt_m3f_family_{attribute}": family},
                    )
                    with self.assertRaises(analyze_prbm.EvidenceError):
                        analyze_prbm.analyze(root, CASES_PATH)

    def test_stock_baseline_validates_original_m3f_resource_models(self) -> None:
        stock_root = Path(self.temporary.name) / "stock" / "map"
        build_fixture(stock_root, stock_baseline=True)
        first = analyze_prbm.analyze(
            stock_root, CASES_PATH, stock_baseline=True
        )
        second = analyze_prbm.analyze(
            stock_root, CASES_PATH, stock_baseline=True
        )
        expected = json.loads(EXPECTED_PATH.read_text(encoding="utf-8"))

        self.assertEqual(first, second)
        self.assertEqual("stock-baseline", first["mode"])
        self.assertEqual(1_882, first["summary"]["selected_triangle_count"])
        self.assertEqual(5, first["summary"]["selected_resource_count"])
        self.assertEqual(518, first["summary"]["stock_empty_anchor_count"])
        self.assertEqual(
            38, first["summary"]["m3_completion_stock_rendered_anchor_count"]
        )
        self.assertEqual(
            40, first["summary"]["m3_completion_stock_empty_anchor_count"]
        )
        self.assertEqual(
            expected["m2_regression_stock_summary"], first["m2_regression"]
        )
        self.assertEqual(
            expected["m3a_regression_stock_summary"], first["m3a_regression"]
        )
        self.assertEqual(
            expected["m3b_regression_stock_summary"], first["m3b_regression"]
        )
        self.assertEqual({"analyzed": False}, first["dense_fixture"])
        self.assertEqual(10, first["cases"][0]["triangle_count"])
        self.assertEqual(
            1_872,
            sum(case["triangle_count"] for case in first["cases"][1:]),
        )
        self.assertTrue(
            all(
                anchor["contract"]["validated"]
                for case in first["cases"]
                for anchor in case["anchors"]
            )
        )

        output = Path(self.temporary.name) / "stock-report.json"
        self.assertEqual(
            0,
            analyze_prbm.main(
                (
                    "--map-root",
                    str(stock_root),
                    "--stock-baseline",
                    "--cases",
                    str(CASES_PATH),
                    "--output",
                    str(output),
                )
            ),
        )
        self.assertEqual(first, json.loads(output.read_text(encoding="utf-8")))

        reordered = Path(self.temporary.name) / "stock-reordered" / "map"
        build_fixture(
            reordered,
            stock_baseline=True,
            reverse_texture_ordinals=True,
        )
        reordered_summary = analyze_prbm.analyze(
            reordered, CASES_PATH, stock_baseline=True
        )["summary"]
        for key in (
            "shape_signature",
            "material_signature",
            "geometry_signature",
            "attribute_signature",
        ):
            self.assertEqual(first["summary"][key], reordered_summary[key], key)

    def test_m3_completion_disabled_uses_all_78_original_resource_projections(self) -> None:
        root = Path(self.temporary.name) / "m3-completion-disabled" / "map"
        build_fixture(root, m3_completion_disabled=True)
        report = analyze_prbm.analyze(
            root, CASES_PATH, m3_completion_disabled=True
        )

        self.assertEqual("m3-completion-disabled", report["mode"])
        self.assertEqual(597, report["summary"]["anchor_count"])
        self.assertEqual(501, report["summary"]["custom_anchor_count"])
        self.assertEqual(23_758, report["summary"]["custom_triangle_count"])
        self.assertEqual(25_640, report["summary"]["selected_triangle_count"])
        self.assertEqual(207, report["summary"]["selected_resource_count"])
        self.assertEqual(17, report["summary"]["stock_fallback_anchor_count"])
        self.assertEqual(78, report["m3_completion"]["route_disabled_anchor_count"])
        self.assertEqual(0, report["m3_completion"]["custom_triangle_count"])
        self.assertEqual(
            38,
            report["m3_completion"]["original_resource_projection"][
                "rendered_anchor_count"
            ],
        )
        self.assertEqual(
            40,
            report["m3_completion"]["original_resource_projection"][
                "empty_anchor_count"
            ],
        )

        leaked = Path(self.temporary.name) / "m3-completion-disabled-leak" / "map"
        build_fixture(
            leaked,
            m3_completion_disabled=True,
            leak_m3_completion_disabled=True,
        )
        with self.assertRaisesRegex(
            analyze_prbm.EvidenceError,
            "m3-completion-disabled anchor .* differs from its exact original-resource model",
        ):
            analyze_prbm.analyze(
                leaked, CASES_PATH, m3_completion_disabled=True
            )

    def test_stock_baseline_fails_on_leaks_or_wrong_stone_contract(self) -> None:
        leaked = Path(self.temporary.name) / "stock-leak" / "map"
        build_fixture(leaked, stock_baseline=True, leak_stock_anchor=True)
        with self.assertRaisesRegex(
            analyze_prbm.EvidenceError, "non-control anchor .* expected zero"
        ):
            analyze_prbm.analyze(leaked, CASES_PATH, stock_baseline=True)

        wrong_count = Path(self.temporary.name) / "stock-count" / "map"
        build_fixture(
            wrong_count,
            stock_baseline=True,
            stock_stone_triangle_count=9,
        )
        with self.assertRaisesRegex(
            analyze_prbm.EvidenceError, "exactly 10 minecraft:block/stone"
        ):
            analyze_prbm.analyze(wrong_count, CASES_PATH, stock_baseline=True)

        wrong_material = Path(self.temporary.name) / "stock-material" / "map"
        build_fixture(
            wrong_material,
            stock_baseline=True,
            stock_wrong_material=True,
        )
        with self.assertRaisesRegex(
            analyze_prbm.EvidenceError, "exactly 10 minecraft:block/stone"
        ):
            analyze_prbm.analyze(wrong_material, CASES_PATH, stock_baseline=True)

    def test_stock_baseline_rejects_dense_mode(self) -> None:
        with self.assertRaisesRegex(
            analyze_prbm.EvidenceError, "cannot include"
        ):
            analyze_prbm.analyze(
                self.map_root,
                CASES_PATH,
                stock_baseline=True,
                include_dense=True,
            )

    def test_smart_overlay_tint_and_fullbright_contract_is_enforced(self) -> None:
        corrupted = Path(self.temporary.name) / "corrupt-overlay" / "map"
        build_fixture(corrupted, corrupt_smart_overlay=True)
        with self.assertRaisesRegex(
            analyze_prbm.EvidenceError, "smart overlay attributes"
        ):
            analyze_prbm.analyze(corrupted, CASES_PATH)

    def test_terminal_tint_face_and_spin_contracts_are_enforced(self) -> None:
        report = analyze_prbm.analyze(self.map_root, CASES_PATH)
        orientation = next(
            case for case in report["cases"] if case["case_id"] == "ae2-m2-01"
        )
        self.assertEqual(6, len(orientation["anchors"]))
        for anchor in orientation["anchors"]:
            terminal_layers = anchor["contract"]["terminal_layers"]
            self.assertEqual(analyze_prbm.TERMINAL_LAYER_RESOURCES, set(terminal_layers))
            self.assertTrue(
                all(not layer["emissive"] for layer in terminal_layers.values())
            )
            self.assertEqual(
                1,
                len(next(iter(terminal_layers.values()))["layouts"]),
            )

        wrong_tint = Path(self.temporary.name) / "wrong-terminal-tint" / "map"
        build_fixture(wrong_tint, corrupt_terminal_tint=True)
        with self.assertRaisesRegex(analyze_prbm.EvidenceError, "layer tint"):
            analyze_prbm.analyze(wrong_tint, CASES_PATH)

        wrong_spin = Path(self.temporary.name) / "wrong-terminal-spin" / "map"
        build_fixture(wrong_spin, corrupt_terminal_spin=True)
        with self.assertRaisesRegex(analyze_prbm.EvidenceError, "spin/layout"):
            analyze_prbm.analyze(wrong_spin, CASES_PATH)

    def test_facade_material_face_and_hole_contract_are_enforced(self) -> None:
        report = analyze_prbm.analyze(self.map_root, CASES_PATH)
        facade_cases = [
            case for case in report["cases"] if case["category"] == "facade-supported"
        ]
        self.assertEqual(2, len(facade_cases))
        self.assertEqual(
            {"south", "up"},
            {
                case["anchors"][0]["contract"]["facade"]["direction"]
                for case in facade_cases
            },
        )
        self.assertTrue(
            all(
                case["anchors"][0]["contract"]["facade"]["triangle_count"] == 48
                for case in facade_cases
            )
        )

        wrong_layout = Path(self.temporary.name) / "wrong-facade-layout" / "map"
        build_fixture(wrong_layout, corrupt_facade_layout=True)
        with self.assertRaisesRegex(analyze_prbm.EvidenceError, "wrong face"):
            analyze_prbm.analyze(wrong_layout, CASES_PATH)

    def test_drive_contract_validates_all_orientations_slots_models_and_leds(self) -> None:
        report = analyze_prbm.analyze(self.map_root, CASES_PATH)
        drive_anchors = [
            anchor
            for case in report["cases"]
            for anchor in case["anchors"]
            if anchor["expected_path"] == "custom-m3"
        ]
        self.assertEqual(32, len(drive_anchors))
        orientation_contracts = {
            (
                anchor["contract"]["drive"]["facing"],
                anchor["contract"]["drive"]["spin"],
            )
            for case in report["cases"]
            if case["category"] == "drive-orientation"
            for anchor in case["anchors"]
        }
        self.assertEqual(
            {
                (facing, spin)
                for facing in analyze_prbm.DRIVE_ORIENTATION_ANGLES
                for spin in range(4)
            },
            orientation_contracts,
        )
        occupied_models = set()
        chassis_triangles = 0
        led_triangles = 0
        for anchor in drive_anchors:
            drive = anchor["contract"]["drive"]
            occupied = drive["occupied_slot_count"]
            self.assertEqual("90+16N", drive["triangle_formula"])
            self.assertEqual(90 + 16 * occupied, anchor["triangle_count"])
            self.assertEqual(6 * occupied, drive["cell_chassis"]["triangle_count"])
            self.assertEqual(10 * occupied, drive["offline_led"]["triangle_count"])
            self.assertEqual([0, 0, 0], drive["offline_led"]["rgb_u8"])
            self.assertEqual(255, drive["offline_led"]["ambient_occlusion_raw_u8"])
            self.assertEqual(15, drive["offline_led"]["blocklight_raw_i8"])
            self.assertEqual(15, drive["offline_led"]["sunlight_raw_i8"])
            chassis_triangles += drive["cell_chassis"]["triangle_count"]
            led_triangles += drive["offline_led"]["triangle_count"]
            for slot in drive["occupied_slots"].values():
                occupied_models.add(slot["model_id"])
                self.assertEqual(
                    {"north", "up", "down"},
                    set(slot["uv_corners_sixteenths"]),
                )
                self.assertEqual(6, slot["chassis_triangle_count"])
        self.assertEqual(12, len(occupied_models))
        self.assertEqual(366, chassis_triangles)
        self.assertEqual(610, led_triangles)
        self.assertEqual(218, report["summary"]["custom_selected_resource_count"])

    def test_drive_orientation_matrices_map_canonical_front_and_four_spins(self) -> None:
        center = (0.5, 0.5, 0.5)
        canonical_front = (0.5, 0.5, 0.0)
        canonical_up = (0.5, 1.0, 0.5)
        for facing, expected_direction in analyze_prbm.DIRECTION_VECTORS.items():
            observed_up_vectors = set()
            for spin in range(4):
                front = analyze_prbm._drive_transform_point(
                    canonical_front, facing, spin
                )
                observed_direction = tuple(
                    round((front[axis] - center[axis]) * 2) for axis in range(3)
                )
                self.assertEqual(expected_direction, observed_direction)
                up = analyze_prbm._drive_transform_point(canonical_up, facing, spin)
                observed_up_vectors.add(
                    tuple(round((up[axis] - center[axis]) * 2) for axis in range(3))
                )
            self.assertEqual(4, len(observed_up_vectors), facing)

    def test_drive_geometry_uv_orientation_and_led_corruptions_fail_closed(self) -> None:
        corruptions = (
            ("orientation", {"corrupt_drive_orientation": True}, "Drive anchor"),
            ("slot", {"corrupt_drive_slot_translation": True}, "Drive anchor"),
            ("uv", {"corrupt_drive_chassis_uv": True}, "UVs do not match"),
            ("led-ao", {"corrupt_drive_led_attributes": True}, "ambient occlusion"),
            ("base-ao", {"corrupt_drive_base_ao": True}, "ambient occlusion"),
        )
        for name, options, error in corruptions:
            with self.subTest(name=name):
                root = Path(self.temporary.name) / f"corrupt-drive-{name}" / "map"
                build_fixture(root, **options)
                with self.assertRaisesRegex(analyze_prbm.EvidenceError, error):
                    analyze_prbm.analyze(root, CASES_PATH)

    def test_drive_components_are_geometry_insensitive(self) -> None:
        report = analyze_prbm.analyze(self.map_root, CASES_PATH)
        component_result = report["drive_component_insensitivity"]
        self.assertTrue(component_result["validated"])
        self.assertEqual(2, component_result["anchor_count"])
        self.assertEqual(106, component_result["triangle_count_per_anchor"])

        corrupted = Path(self.temporary.name) / "corrupt-drive-components" / "map"
        build_fixture(corrupted, corrupt_drive_component_pair=True)
        with self.assertRaisesRegex(
            analyze_prbm.EvidenceError,
            "component-bearing and component-free",
        ):
            analyze_prbm.analyze(corrupted, CASES_PATH)

    def test_drive_manifest_formula_and_exact_material_closure_are_fail_closed(self) -> None:
        manifest = json.loads(CASES_PATH.read_text(encoding="utf-8"))
        first_drive = next(
            anchor
            for case in manifest["cases"]
            if case["case_id"] == "ae2-m3-01"
            for anchor in case["anchors"]
        )
        first_drive["expected_triangle_count"] -= 1
        invalid_formula = Path(self.temporary.name) / "invalid-drive-formula.json"
        invalid_formula.write_text(
            json.dumps(manifest, sort_keys=True, separators=(",", ":")),
            encoding="utf-8",
        )
        with self.assertRaises(analyze_prbm.EvidenceError):
            analyze_prbm.parse_cases(invalid_formula)

        manifest = _schema5_m3b_manifest()
        manifest["profile"]["selected_resources"].pop()
        invalid_closure = Path(self.temporary.name) / "invalid-drive-closure.json"
        invalid_closure.write_text(
            json.dumps(manifest, sort_keys=True, separators=(",", ":")),
            encoding="utf-8",
        )
        with self.assertRaisesRegex(
            analyze_prbm.EvidenceError, "exact M3b resources"
        ):
            analyze_prbm.parse_cases(invalid_closure)

    def test_dense_fixture_is_opt_in_and_enforces_exact_totals(self) -> None:
        normal = analyze_prbm.analyze(self.map_root, CASES_PATH)
        self.assertEqual({"analyzed": False}, normal["dense_fixture"])

        dense_root = Path(self.temporary.name) / "dense" / "map"
        build_fixture(dense_root, include_dense=True)
        report = analyze_prbm.analyze(
            dense_root, CASES_PATH, include_dense=True
        )
        dense = report["dense_fixture"]
        self.assertTrue(dense["analyzed"])
        self.assertEqual(1_024, dense["cell_count"])
        self.assertEqual(63_488, dense["triangle_count"])
        self.assertEqual(
            {
                "ae2:part/cable/core/dense_smart/transparent": 12_288,
                "ae2:part/cable/dense_covered/transparent": 51_200,
            },
            dense["contract"]["material_triangle_counts"],
        )
        self.assertTrue(dense["contract"]["validated"])

    def test_dense_straight_axial_caps_are_owned_by_the_emitting_cell(self) -> None:
        x, y, z = (10, 20, 30)
        extension = 0.01 / 16.0
        negative_x = (
            (x - extension, y + 0.25, z + 0.25),
            (x - extension, y + 0.25, z + 0.75),
            (x - extension, y + 0.75, z + 0.25),
        )
        positive_x = (
            (x + 1 + extension, y + 0.25, z + 0.25),
            (x + 1 + extension, y + 0.75, z + 0.25),
            (x + 1 + extension, y + 0.25, z + 0.75),
        )

        self.assertGreater(analyze_prbm.OWNERSHIP_EPSILON, extension)
        self.assertLess(analyze_prbm.OWNERSHIP_EPSILON, 0.25)
        self.assertEqual((x, y, z), analyze_prbm._triangle_owner(negative_x))
        self.assertEqual((x, y, z), analyze_prbm._triangle_owner(positive_x))

    def test_selected_owner_recovers_only_native_plane_facade_walls(self) -> None:
        def plane_anchor(
            position: tuple[int, int, int],
            direction: str,
            mask: int,
            *,
            facade_direction: str | None = None,
            facade_name: str = "minecraft:stone",
            case_id: str = "ae2-s1-plane-owner",
            additional_facades: tuple[tuple[str, str], ...] = (),
        ) -> analyze_prbm.AnchorContract:
            part = analyze_prbm.NativeStructuralPartContract(
                direction, "ae2:annihilation_plane", "plane", None, None
            )
            native = analyze_prbm.NativeStructuralContract(
                cable_id="ae2:fluix_covered_cable",
                parts=(part,),
                facade_mask=None,
                plane_mask=mask,
                p2p_frequency=None,
                endpoints=(),
                endpoint_straight_optimization_json=None,
                expected_geometry_signature=None,
                expected_nonlighting_attribute_signature=None,
                stock_triangle_count=0,
            )
            return analyze_prbm.AnchorContract(
                case_id,
                "source wall",
                "custom-s1",
                position,
                1,
                (("minecraft:block/stone", 1),),
                (),
                (analyze_prbm.FacePartContract(direction, part.part_id, None),),
                tuple(
                    [
                        analyze_prbm.FacadeContract(
                            facade_direction or direction,
                            analyze_prbm.canonical_json(
                                {"Name": facade_name}
                            ),
                        )
                    ]
                    + [
                        analyze_prbm.FacadeContract(
                            additional_direction,
                            analyze_prbm.canonical_json(
                                {"Name": additional_name}
                            ),
                        )
                        for additional_direction, additional_name in additional_facades
                    ]
                ),
                (),
                None,
                None,
                native_structural=native,
            )

        def source_rectangles(
            direction: str, mask: int
        ) -> tuple[
            tuple[
                tuple[int, int, int],
                tuple[tuple[float, float, float], ...],
            ],
            ...,
        ]:
            unit = 1.0 / 16.0
            thickness = analyze_prbm.NATIVE_STRUCTURAL_FACADE_THICKNESS
            rows = []

            def rectangle(
                bit: int,
                normal: tuple[int, int, int],
                axis_a: int,
                range_a: tuple[float, float],
                axis_b: int,
                range_b: tuple[float, float],
                fixed_axis: int,
                fixed: float,
            ) -> None:
                if not mask & bit:
                    return
                corners = []
                for value_a, value_b in (
                    (range_a[0], range_b[0]),
                    (range_a[1], range_b[0]),
                    (range_a[1], range_b[1]),
                    (range_a[0], range_b[1]),
                ):
                    corner = [0.0, 0.0, 0.0]
                    corner[axis_a] = value_a
                    corner[axis_b] = value_b
                    corner[fixed_axis] = fixed
                    corners.append(tuple(corner))
                rows.append((normal, tuple(corners)))

            if direction == "up":
                slab = (1.0 - thickness, 1.0)
                x_span = (
                    0.0 if mask & 1 else unit,
                    1.0 if mask & 4 else 15.0 * unit,
                )
                rectangle(1, (1, 0, 0), 1, slab, 2, (0.0, 1.0), 0, 0.0)
                rectangle(4, (-1, 0, 0), 1, slab, 2, (0.0, 1.0), 0, 1.0)
                rectangle(8, (0, 0, 1), 0, x_span, 1, slab, 2, 0.0)
                rectangle(2, (0, 0, -1), 0, x_span, 1, slab, 2, 1.0)
            else:
                slab = (0.0, thickness)
                y_span = (
                    0.0 if mask & 2 else unit,
                    1.0 if mask & 8 else 15.0 * unit,
                )
                rectangle(1, (-1, 0, 0), 1, y_span, 2, slab, 0, 1.0)
                rectangle(4, (1, 0, 0), 1, y_span, 2, slab, 0, 0.0)
                rectangle(2, (0, 1, 0), 0, (0.0, 1.0), 2, slab, 1, 0.0)
                rectangle(8, (0, -1, 0), 0, (0.0, 1.0), 2, slab, 1, 1.0)
            return tuple(rows)

        def rectangle_triangles(
            anchor: analyze_prbm.AnchorContract,
            expected_normal: tuple[int, int, int],
            corners: tuple[tuple[float, float, float], ...],
        ) -> tuple[tuple[tuple[float, float, float], ...], ...]:
            local_triangles = [
                (corners[0], corners[1], corners[2]),
                (corners[0], corners[2], corners[3]),
            ]
            if any(
                not analyze_prbm._near(float(expected_normal[axis]), value)
                for axis, value in enumerate(
                    analyze_prbm._cross_normal(local_triangles[0])
                )
            ):
                local_triangles = [
                    (corners[0], corners[2], corners[1]),
                    (corners[0], corners[3], corners[2]),
                ]
            return tuple(
                tuple(
                    tuple(
                        point[axis] + anchor.position[axis]
                        for axis in range(3)
                    )
                    for point in triangle
                )
                for triangle in local_triangles
            )

        cross_face_corners = (
            (0.0, 1.0, 0.0),
            (1.0, 1.0, 0.0),
            (1.0, 1.0, analyze_prbm.NATIVE_STRUCTURAL_FACADE_THICKNESS),
            (0.0, 1.0, analyze_prbm.NATIVE_STRUCTURAL_FACADE_THICKNESS),
        )
        recovered = 0
        recovered_by_direction_mask = {}
        for direction_index, direction in enumerate(("up", "north")):
            for mask in range(16):
                position = (100 + direction_index * 100 + mask * 3, 20, 30)
                if direction == "up" and mask == 8:
                    anchor = plane_anchor(
                        position,
                        direction,
                        mask,
                        facade_name="minecraft:glass",
                        case_id="ae2-s1-07",
                        additional_facades=(("north", "minecraft:stone"),),
                    )
                    rectangles = (((0, -1, 0), cross_face_corners),)
                else:
                    anchor = plane_anchor(position, direction, mask)
                    rectangles = source_rectangles(direction, mask)
                selected = {anchor.position}
                lookup = {anchor.position: anchor}
                recovered_for_mask = 0
                for normal, corners in rectangles:
                    for triangle in rectangle_triangles(anchor, normal, corners):
                        self.assertNotEqual(
                            anchor.position,
                            analyze_prbm._triangle_owner(triangle),
                        )
                        self.assertEqual(
                            anchor.position,
                            analyze_prbm._selected_triangle_owner(
                                triangle,
                                selected,
                                lookup,
                                "minecraft:block/stone",
                            ),
                        )
                        recovered += 1
                        recovered_for_mask += 1
                recovered_by_direction_mask[(direction, mask)] = recovered_for_mask
        self.assertEqual(128, recovered)
        self.assertEqual(
            {
                (direction, mask): 2 * mask.bit_count()
                for direction in ("up", "north")
                for mask in range(16)
            },
            recovered_by_direction_mask,
        )

        # Exact finalized UP world polarity is X min1/max4 and Z min8/max2.
        # Keep these facts hard-coded instead of deriving them from the
        # production matcher so the old withdrawn bit2-min/bit8-max table
        # cannot be blessed by changing both helpers together.
        up_bit_2 = source_rectangles("up", 2)
        up_bit_8 = source_rectangles("up", 8)
        self.assertEqual(1, len(up_bit_2))
        self.assertEqual((0, 0, -1), up_bit_2[0][0])
        self.assertEqual({1.0}, {corner[2] for corner in up_bit_2[0][1]})
        self.assertEqual(1, len(up_bit_8))
        self.assertEqual((0, 0, 1), up_bit_8[0][0])
        self.assertEqual({0.0}, {corner[2] for corner in up_bit_8[0][1]})

        cross_face = plane_anchor(
            (10, 20, 30),
            "up",
            8,
            facade_name="minecraft:glass",
            case_id="ae2-s1-07",
            additional_facades=(("north", "minecraft:stone"),),
        )
        cross_face_triangles = rectangle_triangles(
            cross_face, (0, -1, 0), cross_face_corners
        )
        for triangle in cross_face_triangles:
            self.assertEqual(
                cross_face.position,
                analyze_prbm._selected_triangle_owner(
                    triangle,
                    {cross_face.position},
                    {cross_face.position: cross_face},
                    "minecraft:block/stone",
                ),
            )

        cross_face_negatives = (
            plane_anchor(
                cross_face.position,
                "up",
                8,
                facade_name="minecraft:glass",
                additional_facades=(("north", "minecraft:stone"),),
            ),
            plane_anchor(
                cross_face.position,
                "up",
                2,
                facade_name="minecraft:glass",
                case_id="ae2-s1-07",
                additional_facades=(("north", "minecraft:stone"),),
            ),
            plane_anchor(
                cross_face.position,
                "up",
                8,
                facade_name="minecraft:glass",
                case_id="ae2-s1-07",
            ),
            plane_anchor(
                cross_face.position,
                "up",
                8,
                facade_name="minecraft:glass",
                case_id="ae2-s1-07",
                additional_facades=(("north", "minecraft:glass"),),
            ),
        )
        for wrong_cross_face in cross_face_negatives:
            for triangle in cross_face_triangles:
                self.assertFalse(
                    analyze_prbm._native_plane_facade_wall_matches(
                        wrong_cross_face,
                        triangle,
                        "minecraft:block/stone",
                    )
                )
        self.assertFalse(
            analyze_prbm._native_plane_facade_wall_matches(
                cross_face,
                cross_face_triangles[0],
                "ae2:part/plane_sides",
            )
        )
        malformed_cross_face = list(cross_face_triangles[0])
        malformed_cross_face[2] = (
            malformed_cross_face[2][0] - 0.125,
            malformed_cross_face[2][1],
            malformed_cross_face[2][2],
        )
        self.assertFalse(
            analyze_prbm._native_plane_facade_wall_matches(
                cross_face,
                tuple(malformed_cross_face),
                "minecraft:block/stone",
            )
        )

        anchor = plane_anchor((10, 20, 30), "up", 1)
        normal, corners = source_rectangles("up", 1)[0]
        boundary_wall = rectangle_triangles(anchor, normal, corners)[0]
        primary = analyze_prbm._triangle_owner(boundary_wall)
        self.assertEqual(
            primary,
            analyze_prbm._selected_triangle_owner(
                boundary_wall,
                {primary, anchor.position},
                {anchor.position: anchor},
                "minecraft:block/stone",
            ),
        )
        mask_zero = plane_anchor((10, 20, 30), "up", 0)
        self.assertIsNone(
            analyze_prbm._selected_triangle_owner(
                boundary_wall,
                {mask_zero.position},
                {mask_zero.position: mask_zero},
                "minecraft:block/stone",
            )
        )
        unset_bit = plane_anchor((10, 20, 30), "up", 2)
        self.assertIsNone(
            analyze_prbm._selected_triangle_owner(
                boundary_wall,
                {unset_bit.position},
                {unset_bit.position: unset_bit},
                "minecraft:block/stone",
            )
        )
        # A helper's plane material cannot be mistaken for the selected
        # facade, even when its geometry occupies the same closed boundary.
        self.assertIsNone(
            analyze_prbm._selected_triangle_owner(
                boundary_wall,
                {anchor.position},
                {anchor.position: anchor},
                "ae2:part/plane_sides",
            )
        )
        wrong_facade = plane_anchor(
            anchor.position, "up", 1, facade_direction="north"
        )
        self.assertIsNone(
            analyze_prbm._selected_triangle_owner(
                boundary_wall,
                {wrong_facade.position},
                {wrong_facade.position: wrong_facade},
                "minecraft:block/stone",
            )
        )
        glass_facade = plane_anchor(
            anchor.position, "up", 1, facade_name="minecraft:glass"
        )
        self.assertIsNone(
            analyze_prbm._selected_triangle_owner(
                boundary_wall,
                {glass_facade.position},
                {glass_facade.position: glass_facade},
                "minecraft:block/stone",
            )
        )
        self.assertEqual(
            glass_facade.position,
            analyze_prbm._selected_triangle_owner(
                boundary_wall,
                {glass_facade.position},
                {glass_facade.position: glass_facade},
                analyze_prbm.NATIVE_STRUCTURAL_GLASSENTIAL_MATERIAL,
            ),
        )
        self.assertIsNone(
            analyze_prbm._selected_triangle_owner(
                boundary_wall,
                {glass_facade.position},
                {glass_facade.position: glass_facade},
                "minecraft:block/glass",
            )
        )

        def rejected_mutation(
            triangle: tuple[tuple[float, float, float], ...]
        ) -> None:
            self.assertFalse(
                analyze_prbm._native_plane_facade_wall_matches(
                    anchor, triangle, "minecraft:block/stone"
                )
            )
            if analyze_prbm._triangle_owner(triangle) != anchor.position:
                self.assertIsNone(
                    analyze_prbm._selected_triangle_owner(
                        triangle,
                        {anchor.position},
                        {anchor.position: anchor},
                        "minecraft:block/stone",
                    )
                )

        partial = list(boundary_wall)
        partial[0] = (
            partial[0][0], partial[0][1], partial[0][2] + 0.2
        )
        rejected_mutation(tuple(partial))
        moved_corner = list(boundary_wall)
        moved_corner[1] = (
            moved_corner[1][0] + 0.01,
            moved_corner[1][1],
            moved_corner[1][2],
        )
        rejected_mutation(tuple(moved_corner))
        wrong_thickness = tuple(
            (
                point[0],
                point[1] - 0.01 if point[1] < 21.0 else point[1],
                point[2],
            )
            for point in boundary_wall
        )
        rejected_mutation(wrong_thickness)
        rejected_mutation(
            (boundary_wall[0], boundary_wall[2], boundary_wall[1])
        )
        noncardinal = list(boundary_wall)
        noncardinal[2] = (
            noncardinal[2][0] + 0.01,
            noncardinal[2][1],
            noncardinal[2][2],
        )
        rejected_mutation(tuple(noncardinal))
        # The schema-9 stone control's ordinary support top has no slab-depth
        # span and its normal is parallel to the facade normal. It must stay
        # with primary ownership rather than leaking into an S1 anchor.
        support_top = (
            (10.0, 21.0, 30.0),
            (11.0, 21.0, 30.0),
            (11.0, 21.0, 31.0),
        )
        self.assertIsNone(
            analyze_prbm._selected_triangle_owner(
                support_top,
                {anchor.position},
                {anchor.position: anchor},
                "minecraft:block/stone",
            )
        )
        self.assertIsNone(
            analyze_prbm._selected_triangle_owner(
                boundary_wall,
                {(99, 20, 30)},
                {anchor.position: anchor},
                "minecraft:block/stone",
            )
        )

        # Candidate aggregation remains fail-closed if future source geometry
        # ever makes one wall eligible for two selected anchors.
        epsilon = analyze_prbm.OWNERSHIP_EPSILON / 4.0
        left = plane_anchor((10, 20, 30), "up", 0)
        diagonal = plane_anchor((10, 20, 29), "up", 0)
        slab_min = 21.0 - analyze_prbm.NATIVE_STRUCTURAL_FACADE_THICKNESS
        ambiguous_wall = (
            (10.0, slab_min, 30.0 - epsilon),
            (10.0, 21.0, 30.0 + epsilon),
            (10.0, slab_min, 30.0 + epsilon),
        )
        ambiguous_selected = {left.position, diagonal.position}
        self.assertNotIn(
            analyze_prbm._triangle_owner(ambiguous_wall), ambiguous_selected
        )
        with mock.patch.object(
            analyze_prbm,
            "_native_plane_facade_wall_matches",
            return_value=True,
        ):
            with self.assertRaisesRegex(
                analyze_prbm.EvidenceError,
                "ambiguous native plane-facade wall ownership",
            ):
                analyze_prbm._selected_triangle_owner(
                    ambiguous_wall,
                    ambiguous_selected,
                    {left.position: left, diagonal.position: diagonal},
                    "minecraft:block/stone",
                )

    def test_gzip_crc_and_single_member_are_enforced(self) -> None:
        path = self.map_root / "textures.json.gz"
        valid = path.read_bytes()
        corrupted = bytearray(valid)
        corrupted[-8] ^= 0x01
        path.write_bytes(corrupted)
        with self.assertRaisesRegex(analyze_prbm.EvidenceError, "integrity"):
            analyze_prbm.read_single_gzip(path)

        path.write_bytes(valid + valid)
        with self.assertRaisesRegex(analyze_prbm.EvidenceError, "exactly one member"):
            analyze_prbm.read_single_gzip(path)

    def test_exact_prbm_header_and_terminator_are_enforced(self) -> None:
        path = next((self.map_root / "tiles" / "0").rglob("*.prbm.gz"))
        payload = analyze_prbm.read_single_gzip(path).payload
        invalid_version = bytes((2,)) + payload[1:]
        with self.assertRaisesRegex(analyze_prbm.EvidenceError, "version"):
            analyze_prbm.parse_prbm(invalid_version)
        invalid_flags = payload[:1] + bytes((0x27,)) + payload[2:]
        with self.assertRaisesRegex(analyze_prbm.EvidenceError, "header flags"):
            analyze_prbm.parse_prbm(invalid_flags)
        with self.assertRaisesRegex(analyze_prbm.EvidenceError, "trailing bytes"):
            analyze_prbm.parse_prbm(payload + b"\0")

    def test_parser_reads_exact_output_from_the_pinned_bluemap_writer(self) -> None:
        payload = base64.b64decode(GOLDEN_PATH.read_bytes().strip(), validate=True)
        document = analyze_prbm.parse_prbm(payload)

        self.assertEqual(1, document.triangle_count)
        self.assertEqual(3, document.value_count)
        self.assertEqual(
            (analyze_prbm.MaterialGroup(material_index=3, start=0, count=3),),
            document.groups,
        )
        self.assertEqual((0.25, 100.5, 0.25), document.values("position", 0))
        self.assertEqual((0.75, 100.5, 0.25), document.values("position", 1))
        self.assertEqual((0.25, 100.5, 0.75), document.values("position", 2))
        self.assertEqual((0.0, 0.0), document.values("uv", 0))
        self.assertEqual((1.0, 0.0), document.values("uv", 1))
        self.assertEqual((0.0, 1.0), document.values("uv", 2))

    def test_native_structural_signatures_independently_cover_every_prbm_field(self) -> None:
        baseline = _native_signature_result()
        baseline_records = baseline["records"]
        contract = analyze_prbm.NativeStructuralContract(
            cable_id="ae2:fluix_covered_cable",
            parts=(),
            facade_mask=None,
            plane_mask=None,
            p2p_frequency=None,
            endpoints=(),
            endpoint_straight_optimization_json=None,
            expected_geometry_signature=baseline["geometry_signature"],
            expected_nonlighting_attribute_signature=baseline[
                "nonlighting_attribute_signature"
            ],
            stock_triangle_count=0,
        )
        anchor = type("NativeStructuralAnchorFixture", (), {})()
        anchor.native_structural = contract
        anchor.position = (0, 0, 0)
        anchor.facades = ()
        anchor.face_parts = ()
        validated = analyze_prbm._validate_native_structural_contract(
            anchor, baseline_records, baseline
        )
        self.assertTrue(validated["validated"])
        self.assertEqual(
            baseline["attribute_signature"],
            validated["observed_full_attribute_signature"],
        )
        self.assertEqual(
            baseline["nonlighting_attribute_signature"],
            validated["nonlighting_attribute_signature"],
        )

        invariant_mutations = (
            _native_signature_result(
                positions=((0.2, 0.5, 0.2), (0.75, 0.5, 0.2), (0.2, 0.5, 0.8))
            ),
            _native_signature_result(
                positions=((0.2, 0.5, 0.2), (0.2, 0.5, 0.8), (0.8, 0.5, 0.2))
            ),
            _native_signature_result(
                uvs=((0.0, 0.0), (0.75, 0.0), (0.0, 1.0))
            ),
            _native_signature_result(resource="ae2:part/cable/covered/transparent"),
            _native_signature_result(rgb=(254, 255, 255)),
            _native_signature_result(ao=254),
            _native_signature_result(mutate_normal=True),
        )
        for mutated in invariant_mutations:
            with self.subTest(mutated=mutated):
                self.assertNotEqual(
                    (
                        baseline["geometry_signature"],
                        baseline["attribute_signature"],
                    ),
                    (
                        mutated["geometry_signature"],
                        mutated["attribute_signature"],
                    ),
                )
                with self.assertRaisesRegex(
                    analyze_prbm.EvidenceError,
                    "geometry/UV/material/normal/color/AO signature changed",
                ):
                    analyze_prbm._validate_native_structural_contract(
                        anchor, mutated["records"], mutated
                    )

        for varied_light in (
            _native_signature_result(blocklight=7),
            _native_signature_result(sunlight=14),
        ):
            with self.subTest(varied_light=varied_light):
                self.assertEqual(
                    baseline["nonlighting_attribute_signature"],
                    varied_light["nonlighting_attribute_signature"],
                )
                self.assertNotEqual(
                    baseline["attribute_signature"],
                    varied_light["attribute_signature"],
                )
                varied = analyze_prbm._validate_native_structural_contract(
                    anchor, varied_light["records"], varied_light
                )
                self.assertEqual(
                    varied_light["attribute_signature"],
                    varied["observed_full_attribute_signature"],
                )

        malformed_light = (
            _native_signature_result(blocklights=(0, 1, 0)),
            _native_signature_result(sunlights=(15, 14, 15)),
            _native_signature_result(blocklights=(-1, -1, -1)),
            _native_signature_result(sunlights=(16, 16, 16)),
        )
        for malformed in malformed_light:
            with self.subTest(malformed=malformed):
                with self.assertRaisesRegex(
                    analyze_prbm.EvidenceError,
                    "not flat|outside the source-derived",
                ):
                    analyze_prbm._validate_native_structural_contract(
                        anchor, malformed["records"], malformed
                    )

        for resource in sorted(
            analyze_prbm.NATIVE_STRUCTURAL_FORCED_FULLBRIGHT_RESOURCES
        ):
            exact = _manual_native_record(
                resource,
                ((0.0, 0.0, 0.0), (1.0, 0.0, 0.0), (0.0, 1.0, 0.0)),
                blocklight=15,
                sunlight=15,
            )
            analyze_prbm._validate_native_structural_light_contract(
                [exact], resource
            )
            for channel in ("blocklight", "sunlight"):
                changed = _manual_native_record(
                    resource,
                    exact.positions,
                    blocklight=14 if channel == "blocklight" else 15,
                    sunlight=14 if channel == "sunlight" else 15,
                )
                with self.subTest(resource=resource, channel=channel):
                    with self.assertRaisesRegex(
                        analyze_prbm.EvidenceError, "exact 15/15"
                    ):
                        analyze_prbm._validate_native_structural_light_contract(
                            [changed], resource
                        )
        unsupported_channel = _manual_native_record(
            "ae2:part/cable/future/channels_00",
            ((0.0, 0.0, 0.0), (1.0, 0.0, 0.0), (0.0, 1.0, 0.0)),
            blocklight=15,
            sunlight=15,
        )
        with self.assertRaisesRegex(
            analyze_prbm.EvidenceError, "unsupported channel-overlay"
        ):
            analyze_prbm._validate_native_structural_light_contract(
                [unsupported_channel], "unsupported channel"
            )

    def test_native_endpoint_source_fixtures_reject_state_be_side_and_topology_mutations(self) -> None:
        baseline = _source_s1_cases_from_generator()
        analyze_prbm._validate_s1_endpoint_source_fixtures(baseline)

        mutations = []
        changed = json.loads(json.dumps(baseline))
        changed[22]["anchors"][0]["native_endpoints"][0]["block_entity_id"] = "ae2:charger"
        mutations.append(changed)
        changed = json.loads(json.dumps(baseline))
        changed[22]["anchors"][1]["native_endpoints"][0]["observed_endpoint_side"] = "east"
        mutations.append(changed)
        changed = json.loads(json.dumps(baseline))
        changed[22]["fixture_blocks"][0]["expected_state"]["facing"] = "west"
        mutations.append(changed)
        changed = json.loads(json.dumps(baseline))
        changed[27]["anchors"][1]["native_endpoints"][0][
            "required_block_state"
        ].pop("waterlogged")
        mutations.append(changed)
        changed = json.loads(json.dumps(baseline))
        restricted_helper = next(
            fixture
            for fixture in changed[27]["fixture_blocks"]
            if fixture.get("purpose")
            == "coexistent-part-facade-known-native-disconnected"
        )
        restricted_helper["expected_state"].pop("waterlogged")
        mutations.append(changed)
        changed = json.loads(json.dumps(baseline))
        restricted_helper = next(
            fixture
            for fixture in changed[27]["fixture_blocks"]
            if fixture.get("purpose")
            == "coexistent-part-facade-known-native-disconnected"
        )
        restricted_helper["placement_state"].pop("waterlogged")
        mutations.append(changed)
        changed = json.loads(json.dumps(baseline))
        changed[22]["fixture_blocks"] = [
            fixture
            for fixture in changed[22]["fixture_blocks"]
            if fixture.get("endpoint_structure") != "qnb-yz-edge-ring"
            or fixture.get("endpoint_structure_role") == "endpoint-edge-ring"
        ]
        mutations.append(changed)
        changed = json.loads(json.dumps(baseline))
        changed[22]["anchors"][8]["endpoint_straight_optimization"]["machine_collars"] = True
        mutations.append(changed)
        changed = json.loads(json.dumps(baseline))
        changed[27]["anchors"][2]["native_endpoints"][0][
            "required_block_state"
        ]["powered"] = True
        mutations.append(changed)
        changed = json.loads(json.dumps(baseline))
        changed[27]["fixture_blocks"][-1]["artifact_sha256"] = "0" * 64
        mutations.append(changed)
        changed = json.loads(json.dumps(baseline))
        changed[13]["anchors"][1]["native_neutral_facade_materials"][0][
            "material_family"
        ] = "wrong"
        mutations.append(changed)
        changed = json.loads(json.dumps(baseline))
        changed[13]["fixture_blocks"][0]["block_id"] = "ae2:quartz_glass"
        mutations.append(changed)
        changed = json.loads(json.dumps(baseline))
        changed[14]["anchors"][2]["facades"][0]["block_state"]["Properties"][
            "slot_0_occupied"
        ] = "true"
        mutations.append(changed)
        changed = json.loads(json.dumps(baseline))
        changed[14]["anchors"][3]["facades"][0]["block_state"][
            "Properties"
        ].pop("lit")
        mutations.append(changed)
        changed = json.loads(json.dumps(baseline))
        changed[14]["anchors"][3]["facades"][0]["block_state"][
            "Properties"
        ]["invented"] = "false"
        mutations.append(changed)
        changed = json.loads(json.dumps(baseline))
        changed[14]["anchors"][3]["facades"][0]["block_state"][
            "Properties"
        ]["facing"] = "diagonal"
        mutations.append(changed)
        changed = json.loads(json.dumps(baseline))
        changed[14]["anchors"][4]["native_facade_normalization"][
            "normalized_properties"
        ]["facing"] = "north"
        mutations.append(changed)
        changed = json.loads(json.dumps(baseline))
        changed[14]["anchors"][5]["facade_whitelist_controls"][0][
            "is_solid_render"
        ] = False
        mutations.append(changed)
        changed = json.loads(json.dumps(baseline))
        changed[14]["anchors"][7]["facade_structural_expectation"] = "wrong"
        mutations.append(changed)

        for changed in mutations:
            with self.subTest(mutation=mutations.index(changed)):
                with self.assertRaises(analyze_prbm.EvidenceError):
                    analyze_prbm._validate_s1_endpoint_source_fixtures(changed)

    def test_schema10_custom_p2p_missing_frequency_fails_closed(self) -> None:
        manifest = json.loads(
            self.schema10_cases_path.read_text(encoding="utf-8")
        )
        case = next(
            candidate
            for candidate in manifest["cases"]
            if candidate.get("case_id") == "ae2-s1-09"
        )
        part = case["anchors"][0]["face_parts"][0]
        self.assertIn("freq", part)
        part.pop("freq")
        changed_path = Path(self.temporary.name) / "missing-p2p-frequency.json"
        changed_path.write_text(
            analyze_prbm.canonical_json(manifest, pretty=True),
            encoding="utf-8",
        )
        with self.assertRaisesRegex(
            analyze_prbm.EvidenceError,
            "custom P2P part .* invalid frequency",
        ):
            analyze_prbm.parse_cases(changed_path)

    def test_native_endpoint_profile_complete_state_domains_are_independent_and_fail_closed(
        self,
    ) -> None:
        from tools import ae2_native_structural_contract as profile_contract

        namespace = _source_generator_namespace()
        validate = namespace[
            "validate_native_structural_endpoint_profile_contract"
        ]
        schemas = namespace["NATIVE_STRUCTURAL_ENDPOINT_STATE_SCHEMAS"]
        policies = namespace["NATIVE_STRUCTURAL_ENDPOINT_POLICIES"]
        ordered = namespace["NATIVE_STRUCTURAL_ENDPOINTS_ORDERED"]
        for name, _family in ordered:
            required_state = policies[name]["required_state"]
            schema = schemas[name]
            self.assertEqual(set(schema), set(required_state), name)
            for key, value in required_state.items():
                serialized = (
                    str(value).lower() if isinstance(value, bool) else str(value)
                )
                self.assertIn(serialized, schema[key], (name, key))
        computed_state_counts = {}
        for name, _family in ordered:
            state_count = 1
            for domain in schemas[name].values():
                state_count *= len(domain)
            computed_state_counts[name] = state_count
        self.assertEqual(
            namespace["NATIVE_STRUCTURAL_ENDPOINT_STATE_COUNTS"],
            computed_state_counts,
        )
        self.assertEqual(
            analyze_prbm.NATIVE_STRUCTURAL_ENDPOINT_STATE_COUNTS,
            computed_state_counts,
        )
        self.assertEqual(
            namespace["NATIVE_STRUCTURAL_ENDPOINT_STATE_CARTESIAN_COUNT"],
            sum(computed_state_counts.values()),
        )
        self.assertEqual(
            analyze_prbm.NATIVE_STRUCTURAL_ENDPOINT_STATE_CARTESIAN_COUNT,
            sum(computed_state_counts.values()),
        )
        self.assertEqual(
            namespace[
                "NATIVE_STRUCTURAL_ENDPOINT_STATE_SIDE_CARTESIAN_COUNT"
            ],
            sum(computed_state_counts.values()) * 6,
        )
        self.assertEqual(
            analyze_prbm.NATIVE_STRUCTURAL_ENDPOINT_STATE_SIDE_CARTESIAN_COUNT,
            sum(computed_state_counts.values()) * 6,
        )
        ordered_rows = [
            {
                "id": f"ae2:{name}",
                "cable_type": family,
                "block_entity_id": policies[name]["block_entity_id"],
                "side_rule": policies[name]["side_rule"],
                "state_properties": schemas[name],
                "blockstate_sha256": namespace[
                    "NATIVE_STRUCTURAL_ENDPOINT_BLOCKSTATE_SHA256"
                ][name],
            }
            for name, family in ordered
        ]
        self.assertEqual(
            analyze_prbm.NATIVE_STRUCTURAL_ENDPOINT_STATE_CONTRACT_SHA256,
            analyze_prbm.sha256_text(analyze_prbm.canonical_json(ordered_rows)),
        )

        profile_path = (
            PROJECT_ROOT
            / "src/main/resources/bluemap-ae2/profiles/ae2/19.2.17/routes/cable-bus-structural/profile.json"
        )
        profile_root = profile_path.parent
        profile = profile_contract.profile(
            (profile_root / "required-resources.sha256").read_bytes(),
            (profile_root / "required-resources.tsv").read_bytes(),
        )
        endpoints = profile["nativeEndpoints"]
        endpoint_side_policy = profile["endpointSidePolicy"]
        validate(endpoints, endpoint_side_policy)
        mutations = {}
        missing = json.loads(json.dumps(endpoints))
        missing[1]["stateProperties"].pop("waterlogged")
        mutations["missing-key"] = missing
        extra = json.loads(json.dumps(endpoints))
        extra[1]["stateProperties"]["unexpected"] = ["false", "true"]
        mutations["extra-key"] = extra
        out_of_domain = json.loads(json.dumps(endpoints))
        out_of_domain[1]["stateProperties"]["waterlogged"] = [
            "false", "true", "maybe"
        ]
        mutations["out-of-domain"] = out_of_domain
        wrong_digest = json.loads(json.dumps(endpoints))
        wrong_digest[1]["blockstateSha256"] = "0" * 64
        mutations["blockstate-digest"] = wrong_digest
        for label, changed in mutations.items():
            with self.subTest(label=label):
                with self.assertRaisesRegex(ValueError, "endpoint profile row"):
                    validate(changed, endpoint_side_policy)
        changed_policy = json.loads(json.dumps(endpoint_side_policy))
        changed_policy["stateCartesianCount"] -= 1
        with self.assertRaisesRegex(ValueError, "Cartesian profile closure"):
            validate(endpoints, changed_policy)
        changed_policy = json.loads(json.dumps(endpoint_side_policy))
        changed_policy["stateSideCartesianCount"] -= 1
        with self.assertRaisesRegex(ValueError, "Cartesian profile closure"):
            validate(endpoints, changed_policy)

    def test_known_extension_profile_catalog_is_exact_and_fail_closed(self) -> None:
        namespace = _source_generator_namespace()
        validate = namespace[
            "validate_native_structural_unknown_endpoint_profile_contract"
        ]
        profile_path = (
            PROJECT_ROOT
            / "src/main/resources/bluemap-ae2/profiles/ae2/19.2.17/routes/cable-bus-structural/profile.json"
        )
        catalog = json.loads(profile_path.read_text(encoding="utf-8"))[
            "knownUnsupportedCompatibleEndpoints"
        ]
        validate(catalog)
        mutations = {}
        wrong_entry = json.loads(json.dumps(catalog))
        wrong_entry["entries"][0]["id"] = "expandedae:wrong"
        mutations["entry"] = wrong_entry
        wrong_artifact_count = json.loads(json.dumps(catalog))
        wrong_artifact_count["artifacts"][0]["endpointCount"] -= 1
        mutations["artifact-count"] = wrong_artifact_count
        wrong_representative = json.loads(json.dumps(catalog))
        wrong_representative["representativeControl"]["blockEntityId"] = (
            "expandedae:wrong"
        )
        mutations["representative"] = wrong_representative
        for label, changed in mutations.items():
            with self.subTest(label=label):
                with self.assertRaisesRegex(ValueError, "endpoint catalog"):
                    validate(changed)

    def test_native_facade_profile_source_contract_rejects_semantic_mutations(
        self,
    ) -> None:
        from tools import ae2_native_structural_contract as profile_contract

        namespace = _source_generator_namespace()
        validate = namespace[
            "validate_native_structural_facade_profile_contract"
        ]
        profile_root = (
            PROJECT_ROOT
            / "src/main/resources/bluemap-ae2/profiles/ae2/19.2.17/routes/cable-bus-structural"
        )
        profile = profile_contract.profile(
            (profile_root / "required-resources.sha256").read_bytes(),
            (profile_root / "required-resources.tsv").read_bytes(),
        )
        render_policy = profile["renderPolicy"]
        validate(render_policy)
        expected_rows = [
            {
                "blockId": block_id,
                "properties": namespace[
                    "NATIVE_STRUCTURAL_FACADE_WHITELIST_STATE_SCHEMAS"
                ][block_id],
                "blockstateSha256": namespace[
                    "NATIVE_STRUCTURAL_FACADE_WHITELIST_BLOCKSTATE_SHA256"
                ][block_id],
            }
            for block_id in namespace[
                "NATIVE_STRUCTURAL_FACADE_WHITELIST_IDS"
            ]
        ]
        self.assertEqual(
            namespace[
                "NATIVE_STRUCTURAL_FACADE_WHITELIST_STATE_CONTRACT_SHA256"
            ],
            analyze_prbm.sha256_text(analyze_prbm.canonical_json(expected_rows)),
        )
        self.assertEqual(
            namespace[
                "NATIVE_STRUCTURAL_FACADE_WHITELIST_STATE_CONTRACT_SHA256"
            ],
            analyze_prbm.NATIVE_STRUCTURAL_FACADE_WHITELIST_STATE_CONTRACT_SHA256,
        )
        self.assertEqual(
            namespace["NATIVE_STRUCTURAL_FACADE_WHITELIST_STATE_SCHEMAS"],
            analyze_prbm.NATIVE_STRUCTURAL_FACADE_WHITELIST_STATE_SCHEMAS,
        )
        self.assertEqual(
            namespace["NATIVE_STRUCTURAL_FACADE_WHITELIST_BLOCKSTATE_SHA256"],
            analyze_prbm.NATIVE_STRUCTURAL_FACADE_WHITELIST_BLOCKSTATE_SHA256,
        )
        computed_state_counts = {
            block_id: namespace["native_structural_state_schema_count"](
                namespace[
                    "NATIVE_STRUCTURAL_FACADE_WHITELIST_STATE_SCHEMAS"
                ][block_id]
            )
            for block_id in namespace[
                "NATIVE_STRUCTURAL_FACADE_WHITELIST_IDS"
            ]
        }
        self.assertEqual(
            namespace["NATIVE_STRUCTURAL_FACADE_WHITELIST_STATE_COUNTS"],
            computed_state_counts,
        )
        self.assertEqual(
            analyze_prbm.NATIVE_STRUCTURAL_FACADE_WHITELIST_STATE_COUNTS,
            computed_state_counts,
        )
        self.assertEqual(
            namespace[
                "NATIVE_STRUCTURAL_FACADE_WHITELIST_STATE_CARTESIAN_COUNT"
            ],
            sum(computed_state_counts.values()),
        )

        mutations = {}
        changed = json.loads(json.dumps(render_policy))
        changed["facades"]["eligibility"]["sameStateSkipRendering"][0][
            "skipRendering"
        ] = False
        mutations["skip-rendering"] = changed
        changed = json.loads(json.dumps(render_policy))
        del changed["facades"]["eligibility"]["stateSchemas"][4][
            "properties"
        ]["lit"]
        mutations["whitelist-state-missing-key"] = changed
        changed = json.loads(json.dumps(render_policy))
        changed["facades"]["eligibility"]["stateSchemas"][4][
            "properties"
        ]["invented"] = ["false", "true"]
        mutations["whitelist-state-extra-key"] = changed
        changed = json.loads(json.dumps(render_policy))
        changed["facades"]["eligibility"]["stateSchemas"][4][
            "properties"
        ]["facing"].append("diagonal")
        mutations["whitelist-state-out-of-domain"] = changed
        changed = json.loads(json.dumps(render_policy))
        changed["facades"]["eligibility"]["stateSchemas"][4][
            "blockstateSha256"
        ] = "0" * 64
        mutations["whitelist-state-resource-digest"] = changed
        changed = json.loads(json.dumps(render_policy))
        schemas = changed["facades"]["eligibility"]["stateSchemas"]
        schemas[4], schemas[5] = schemas[5], schemas[4]
        mutations["whitelist-state-row-order"] = changed
        changed = json.loads(json.dumps(render_policy))
        changed["facades"]["eligibility"]["stateSchemaPolicy"] = (
            "accept-default-variant"
        )
        mutations["whitelist-state-policy"] = changed
        changed = json.loads(json.dumps(render_policy))
        changed["facades"]["eligibility"]["stateCartesianCount"] -= 1
        mutations["whitelist-state-cartesian-count"] = changed
        changed = json.loads(json.dumps(render_policy))
        changed["facades"]["eligibility"]["stateClassificationPolicy"] = (
            "neutral-state-only"
        )
        mutations["whitelist-state-classification-policy"] = changed
        changed = json.loads(json.dumps(render_policy))
        changed["facades"]["eligibility"][
            "solidRenderTrueCartesianCount"
        ] -= 1
        mutations["whitelist-solid-render-cartesian-count"] = changed
        changed = json.loads(json.dumps(render_policy))
        changed["facades"]["eligibility"][
            "sameStateSkipRenderingTrueCartesianCount"
        ] += 1
        mutations["whitelist-skip-rendering-cartesian-count"] = changed
        changed = json.loads(json.dumps(render_policy))
        changed["facades"]["eligibility"]["uvReinterpolationPolicy"] = (
            "three-dimensional-gram-solve"
        )
        mutations["uv"] = changed
        changed = json.loads(json.dumps(render_policy))
        changed["facades"]["sourceParityGolden"]["cardinalVariantTransform"] = (
            "host-float-matrix"
        )
        mutations["cardinal-transform"] = changed
        changed = json.loads(json.dumps(render_policy))
        changed["facades"]["sourceParityGolden"]["cornerKickRuntimeEpsilon"][
            "value"
        ] = 0.00001
        mutations["epsilon-units"] = changed
        changed = json.loads(json.dumps(render_policy))
        changed["facades"]["sourceParityGolden"]["ambientOcclusionDirection"] = (
            "rotated-element-normal"
        )
        mutations["ao-direction"] = changed
        changed = json.loads(json.dumps(render_policy))
        changed["facades"]["sourceParityGolden"]["mapColorIllumination"] = (
            "element-emission"
        )
        mutations["map-color"] = changed
        changed = json.loads(json.dumps(render_policy))
        changed["facades"]["sourceParityGolden"][
            "cutoutStripAabbNormalization"
        ] = "degenerate-reversed-endpoints"
        mutations["aabb-normalization"] = changed
        changed = json.loads(json.dumps(render_policy))
        changed["facades"]["eligibility"]["nativeNeutralMaterials"][8][
            "statePolicy"
        ]["normalization"]["facing"] = "north"
        mutations["monitor-normalization"] = changed
        changed = json.loads(json.dumps(render_policy))
        changed["facadeCutoutCollision"]["partPolicies"]["ae2:import_bus"][
            "boxes"
        ][-1][0] = 6
        mutations["collision-union"] = changed
        changed = json.loads(json.dumps(render_policy))
        changed["planeConnectionMasks"]["coordinateSpaces"] = (
            "one-shared-plane-coordinate-space"
        )
        mutations["plane-coordinate-spaces"] = changed
        changed = json.loads(json.dumps(render_policy))
        changed["planeConnectionMasks"]["collisionBoundBitsByInstalledFace"][
            "up"
        ]["minY"] = "down"
        mutations["plane-face-aware-collision-bits"] = changed
        changed = json.loads(json.dumps(render_policy))
        changed["facadeCutoutCollision"]["partPolicies"][
            "ae2:annihilation_plane"
        ]["dynamicSheet"]["boundBitsByInstalledFace"]["west"]["minX"] = "left"
        mutations["plane-dynamic-sheet-face-bits"] = changed

        for label, changed in mutations.items():
            with self.subTest(label=label):
                with self.assertRaises(ValueError):
                    validate(changed)

    def test_native_facade_whitelist_state_helper_is_exact_and_fail_closed(
        self,
    ) -> None:
        namespace = _source_generator_namespace()
        validate = namespace[
            "validate_native_structural_whitelist_facade_state"
        ]
        baseline = {
            "Name": "minecraft:furnace",
            "Properties": {"facing": "north", "lit": "false"},
        }
        validate(baseline)

        mutations = []
        changed = json.loads(json.dumps(baseline))
        changed["Properties"].pop("lit")
        mutations.append(changed)
        changed = json.loads(json.dumps(baseline))
        changed["Properties"]["invented"] = "false"
        mutations.append(changed)
        changed = json.loads(json.dumps(baseline))
        changed["Properties"]["facing"] = "diagonal"
        mutations.append(changed)
        changed = json.loads(json.dumps(baseline))
        changed["Unexpected"] = "value"
        mutations.append(changed)
        for changed in mutations:
            with self.subTest(state=changed):
                with self.assertRaisesRegex(ValueError, "whitelist facade"):
                    validate(changed)

    def test_native_structural_final_companion_identities_are_fail_closed(
        self,
    ) -> None:
        namespace = _source_generator_namespace()
        validate = namespace["validate_native_structural_companion_identities"]
        validate()
        mutations = {
            "CURRENT_SUPPORT_MATRIX_SIZE_BYTES": 0,
            "CURRENT_SUPPORT_MATRIX_SHA256": "0" * 64,
            "CURRENT_PROVENANCE_SIZE_BYTES": 0,
            "CURRENT_PROVENANCE_SHA256": "0" * 64,
            "CURRENT_ACCEPTED_S1_SUPPORT_PROJECTION_SHA256": "0" * 64,
            "CURRENT_ACCEPTED_S1_PROVENANCE_PROJECTION_SHA256": "0" * 64,
        }
        for key, wrong_value in mutations.items():
            with self.subTest(key=key):
                original = namespace[key]
                namespace[key] = wrong_value
                try:
                    with self.assertRaisesRegex(
                        ValueError,
                        "identity changed|semantic projection changed",
                    ):
                        validate()
                finally:
                    namespace[key] = original

    def test_native_plane_source_golden_rejects_mask_geometry_and_invalid_light(self) -> None:
        part = analyze_prbm.NativeStructuralPartContract(
            "up", "ae2:annihilation_plane", "plane", None, None
        )
        contract = analyze_prbm.NativeStructuralContract(
            cable_id="ae2:fluix_covered_cable",
            parts=(part,),
            facade_mask=None,
            plane_mask=0,
            p2p_frequency=None,
            endpoints=(),
            endpoint_straight_optimization_json=None,
            expected_geometry_signature=None,
            expected_nonlighting_attribute_signature=None,
            stock_triangle_count=0,
        )
        anchor = type("PlaneSourceAnchor", (), {})()
        anchor.native_structural = contract
        anchor.position = (0, 0, 0)
        anchor.facades = (
            analyze_prbm.FacadeContract(
                "up", analyze_prbm.canonical_json({"Name": "minecraft:stone"})
            ),
        )
        bounds_triangle = (
            (1 / 16, 15 / 16, 1 / 16),
            (15 / 16, 15 / 16, 15 / 16),
            (1 / 16, 1.0, 15 / 16),
        )
        def plane_layers(
            front_material: str,
            visual_triangle: tuple[tuple[float, float, float], ...],
        ) -> list[analyze_prbm.TriangleRecord]:
            minimum_x = min(position[0] for position in visual_triangle)
            maximum_x = max(position[0] for position in visual_triangle)
            minimum_z = min(position[2] for position in visual_triangle)
            maximum_z = max(position[2] for position in visual_triangle)
            front = (
                (minimum_x, 1.0, minimum_z),
                (maximum_x, 1.0, maximum_z),
                (minimum_x, 1.0, maximum_z),
            )
            built_in_back = tuple(
                (x, 15 / 16, z) for x, _y, z in front
            )
            chassis_back = tuple(
                (x, 14 / 16, z) for x, _y, z in front
            )
            return [
                *(
                    _manual_native_record(front_material, front)
                    for _ in range(2)
                ),
                *(
                    _manual_native_record(
                        "ae2:part/plane_sides", visual_triangle
                    )
                    for _ in range(8)
                ),
                *(
                    _manual_native_record(
                        "ae2:part/transition_plane_back", built_in_back
                    )
                    for _ in range(2)
                ),
                *(
                    _manual_native_record(
                        "ae2:part/transition_plane_back", chassis_back
                    )
                    for _ in range(2)
                ),
            ]

        records = plane_layers("ae2:part/annihilation_plane", bounds_triangle)
        records.extend(
            (
                _manual_native_record(
                    "minecraft:block/stone",
                    ((0.0, 15 / 16, 0.0), (1.0, 15 / 16, 1.0), (1 / 16, 1.0, 15 / 16)),
                ),
                _manual_native_record(
                    "minecraft:block/stone",
                    ((15 / 16, 15 / 16, 1 / 16), (1.0, 1.0, 0.0), (0.0, 1.0, 1.0)),
                ),
            )
        )
        fact = analyze_prbm._validate_native_plane_source(anchor, records, part)
        self.assertEqual(14, fact["triangle_count"])
        self.assertEqual(
            "world-derived-own-and-outward-face-maximum",
            fact["light_policy"],
        )

        missing_chassis_back = list(records)
        del missing_chassis_back[13]
        with self.assertRaisesRegex(
            analyze_prbm.EvidenceError, "material/triangle closure"
        ):
            analyze_prbm._validate_native_plane_source(
                anchor, missing_chassis_back, part
            )

        bad_geometry = [
            _manual_native_record(
                record.material_identity,
                tuple(
                    (0.8 if analyze_prbm._near(position[0], 15 / 16) else position[0], position[1], position[2])
                    for position in record.positions
                ),
            )
            if record.material_identity.startswith("ae2:")
            else record
            for record in records
        ]
        with self.assertRaisesRegex(analyze_prbm.EvidenceError, "mask-derived"):
            analyze_prbm._validate_native_plane_source(anchor, bad_geometry, part)
        varied_world_light = list(records)
        varied_world_light[0] = _manual_native_record(
            varied_world_light[0].material_identity,
            varied_world_light[0].positions,
            blocklight=7,
            sunlight=14,
        )
        analyze_prbm._validate_native_plane_source(
            anchor, varied_world_light, part
        )
        bad_light = list(records)
        bad_light[0] = _manual_native_record(
            bad_light[0].material_identity,
            bad_light[0].positions,
            sunlights=(15, 14, 15),
        )
        with self.assertRaisesRegex(analyze_prbm.EvidenceError, "not flat"):
            analyze_prbm._validate_native_plane_source(anchor, bad_light, part)

        # The exact source-local tables differ, and their independent
        # installed-face transforms happen to converge on the same world
        # bounds. Neither coordinate space may be silently reused as the
        # other.
        self.assertEqual(
            (4, 1, 2, 8),
            analyze_prbm._NATIVE_PLANE_VISUAL_LOCAL_BOUND_BITS,
        )
        self.assertEqual(
            {
                "down": (4, 1, 2, 8),
                "up": (1, 4, 8, 2),
                "north": (1, 4, 2, 8),
                "south": (1, 4, 2, 8),
                "west": (4, 1, 2, 8),
                "east": (4, 1, 2, 8),
            },
            analyze_prbm._NATIVE_PLANE_COLLISION_LOCAL_BOUND_BITS,
        )
        for installed_face in analyze_prbm.DIRECTION_VECTORS:
            for installed_mask in range(16):
                self.assertEqual(
                    analyze_prbm._native_plane_visual_expected_bounds(
                        installed_face, installed_mask
                    ),
                    analyze_prbm._native_plane_facade_cutout_expected_bounds(
                        installed_face, installed_mask
                    ),
                )
        right_contract = analyze_prbm.NativeStructuralContract(
            cable_id="ae2:fluix_covered_cable",
            parts=(part,),
            facade_mask=None,
            plane_mask=4,
            p2p_frequency=None,
            endpoints=(),
            endpoint_straight_optimization_json=None,
            expected_geometry_signature=None,
            expected_nonlighting_attribute_signature=None,
            stock_triangle_count=0,
        )
        anchor.native_structural = right_contract
        visual_triangle = (
            (1 / 16, 15 / 16, 1 / 16),
            (1.0, 15 / 16, 15 / 16),
            (1 / 16, 1.0, 15 / 16),
        )
        right_records = plane_layers(
            "ae2:part/annihilation_plane", visual_triangle
        )
        right_records.extend(
            (
                _manual_native_record(
                    "minecraft:block/stone",
                    ((0.0, 15 / 16, 0.0), (1.0, 15 / 16, 1.0), (1 / 16, 1.0, 15 / 16)),
                ),
                _manual_native_record(
                    "minecraft:block/stone",
                    ((1 / 16, 15 / 16, 1 / 16), (1.0, 1.0, 0.0), (0.0, 1.0, 1.0)),
                ),
            )
        )
        right_fact = analyze_prbm._validate_native_plane_source(
            anchor, right_records, part
        )
        self.assertEqual(1 / 16, right_fact["visual_bounds"][0][0])
        self.assertEqual(1.0, right_fact["visual_bounds"][1][0])
        self.assertEqual(1 / 16, right_fact["facade_cutout_bounds"][0][0])
        self.assertEqual(1.0, right_fact["facade_cutout_bounds"][1][0])

        collision_used_as_visual = [
            _manual_native_record(
                record.material_identity,
                tuple(
                    (15 / 16 if analyze_prbm._near(position[0], 1.0) else position[0], position[1], position[2])
                    for position in record.positions
                ),
            )
            if record.material_identity.startswith("ae2:")
            else record
            for record in right_records
        ]
        with self.assertRaisesRegex(analyze_prbm.EvidenceError, "visual geometry"):
            analyze_prbm._validate_native_plane_source(
                anchor, collision_used_as_visual, part
            )

        visual_used_as_collision = [
            _manual_native_record(
                record.material_identity,
                tuple(
                    (0.0 if analyze_prbm._near(position[0], 1 / 16) else position[0], position[1], position[2])
                    for position in record.positions
                ),
            )
            if record.material_identity == "minecraft:block/stone"
            else record
            for record in right_records
        ]
        with self.assertRaisesRegex(analyze_prbm.EvidenceError, "facade hole"):
            analyze_prbm._validate_native_plane_source(
                anchor, visual_used_as_collision, part
            )

        # UP bit 8 extends the collision hole to z=0. The transparent UP
        # facade is first inset to z=t by the perpendicular opaque NORTH
        # facade, so FacadeBuilder#getBoxes passes the reversed interval t..0
        # to Minecraft AABB. AABB normalizes it back to the visible 0..t strip.
        boundary_contract = analyze_prbm.NativeStructuralContract(
            cable_id="ae2:fluix_covered_cable",
            parts=(part,),
            facade_mask=None,
            plane_mask=8,
            p2p_frequency=None,
            endpoints=(),
            endpoint_straight_optimization_json=None,
            expected_geometry_signature=None,
            expected_nonlighting_attribute_signature=None,
            stock_triangle_count=0,
        )
        boundary = type("PlaneTransparentBoundarySourceAnchor", (), {})()
        boundary.native_structural = boundary_contract
        boundary.position = (0, 0, 0)
        boundary.facades = (
            analyze_prbm.FacadeContract(
                "up", analyze_prbm.canonical_json({"Name": "minecraft:glass"})
            ),
            analyze_prbm.FacadeContract(
                "north", analyze_prbm.canonical_json({"Name": "minecraft:stone"})
            ),
        )
        boundary_visual = (
            (1 / 16, 15 / 16, 0.0),
            (15 / 16, 15 / 16, 15 / 16),
            (1 / 16, 1.0, 15 / 16),
        )
        boundary_records = plane_layers(
            "ae2:part/annihilation_plane", boundary_visual
        )
        thickness = analyze_prbm.NATIVE_STRUCTURAL_FACADE_THICKNESS
        normalized_strip = _manual_native_record(
            analyze_prbm.NATIVE_STRUCTURAL_GLASSENTIAL_MATERIAL,
            (
                (1 / 16, 15 / 16, 0.0),
                (15 / 16, 15 / 16, thickness),
                (1 / 16, 1.0, thickness),
            ),
        )
        perpendicular_stone = _manual_native_record(
            "minecraft:block/stone",
            (
                (0.0, 15 / 16, 0.0),
                (1.0, 15 / 16, 0.0),
                (0.0, 1.0, thickness),
            ),
        )
        boundary_records.extend((normalized_strip, perpendicular_stone))
        boundary_fact = analyze_prbm._validate_native_plane_source(
            boundary, boundary_records, part
        )
        expected_boundary = json.loads(
            NATIVE_STRUCTURAL_GOLDENS_PATH.read_text(encoding="utf-8")
        )["planes"]["transparent_boundary_aabb_normalization"]
        self.assertEqual(
            expected_boundary["normalized_strip_bounds_blocks"],
            boundary_fact["transparent_boundary_aabb_normalization"][
                "normalized_strip_bounds"
            ],
        )
        self.assertEqual(
            expected_boundary["source_constructor"],
            boundary_fact["transparent_boundary_aabb_normalization"][
                "source_constructor"
            ],
        )
        without_aabb_normalization = list(boundary_records)
        without_aabb_normalization[-2] = _manual_native_record(
            analyze_prbm.NATIVE_STRUCTURAL_GLASSENTIAL_MATERIAL,
            tuple(
                (x, y, thickness) for x, y, _z in normalized_strip.positions
            ),
        )
        with self.assertRaisesRegex(
            analyze_prbm.EvidenceError, "AABB normalization"
        ):
            analyze_prbm._validate_native_plane_source(
                boundary, without_aabb_normalization, part
            )

    def test_native_p2p_source_golden_keeps_frequency_pixels_nonemissive(self) -> None:
        frequency = 0x1234
        expected_nibble_colors = {
            1: (37, 37, 37),
            2: (23, 23, 23),
            3: (5, 5, 5),
            4: (23, 57, 23),
        }
        self.assertEqual(
            expected_nibble_colors,
            {
                nibble: analyze_prbm._inactive_p2p_rgb(nibble)
                for nibble in expected_nibble_colors
            },
        )
        part = analyze_prbm.NativeStructuralPartContract(
            "north", "ae2:me_p2p_tunnel", "p2p", None, frequency
        )
        contract = analyze_prbm.NativeStructuralContract(
            cable_id="ae2:fluix_smart_cable",
            parts=(part,),
            facade_mask=None,
            plane_mask=None,
            p2p_frequency=frequency,
            endpoints=(),
            endpoint_straight_optimization_json=None,
            expected_geometry_signature=None,
            expected_nonlighting_attribute_signature=None,
            stock_triangle_count=0,
        )
        anchor = type("P2PSourceAnchor", (), {})()
        anchor.native_structural = contract
        anchor.position = (0, 0, 0)
        coordinates = (3, 4, 5, 11, 12, 13)
        records = []
        for nibble in (1, 2, 3, 4):
            color = expected_nibble_colors[nibble]
            for index in range(48):
                records.append(
                    _manual_native_record(
                        "ae2:part/p2p_tunnel_frequency",
                        (
                            (coordinates[index % 6] / 16, coordinates[(index // 6) % 6] / 16, 2 / 16),
                            (coordinates[(index + 1) % 6] / 16, coordinates[(index + 2) % 6] / 16, 3 / 16),
                            (coordinates[(index + 3) % 6] / 16, coordinates[(index + 4) % 6] / 16, 2 / 16),
                        ),
                        color=color,
                    )
                )
        fact = analyze_prbm._validate_native_p2p_source(anchor, records)
        self.assertEqual([1, 2, 3, 4], fact["nibbles"])
        self.assertFalse(fact["emissive"])
        self.assertEqual(
            "world-derived-own-and-outward-face-maximum",
            fact["light_policy"],
        )

        varied_world_light = list(records)
        varied_world_light[0] = _manual_native_record(
            varied_world_light[0].material_identity,
            varied_world_light[0].positions,
            color=varied_world_light[0].colors[0],
            blocklight=7,
            sunlight=14,
        )
        analyze_prbm._validate_native_p2p_source(anchor, varied_world_light)
        nonflat_world_light = list(records)
        nonflat_world_light[0] = _manual_native_record(
            nonflat_world_light[0].material_identity,
            nonflat_world_light[0].positions,
            color=nonflat_world_light[0].colors[0],
            blocklights=(0, 1, 0),
        )
        with self.assertRaisesRegex(analyze_prbm.EvidenceError, "not flat"):
            analyze_prbm._validate_native_p2p_source(
                anchor, nonflat_world_light
            )

        bad_color = list(records)
        bad_color[0] = _manual_native_record(
            bad_color[0].material_identity,
            bad_color[0].positions,
            color=(255, 0, 0),
        )
        with self.assertRaisesRegex(analyze_prbm.EvidenceError, "nibble colors"):
            analyze_prbm._validate_native_p2p_source(anchor, bad_color)
        rounded_color = list(records)
        rounded_color[0] = _manual_native_record(
            rounded_color[0].material_identity,
            rounded_color[0].positions,
            color=(38, 38, 38),
        )
        with self.assertRaisesRegex(analyze_prbm.EvidenceError, "nibble colors"):
            analyze_prbm._validate_native_p2p_source(anchor, rounded_color)
        bad_geometry = list(records)
        bad_geometry[0] = _manual_native_record(
            bad_geometry[0].material_identity,
            ((6 / 16, 3 / 16, 2 / 16),) + bad_geometry[0].positions[1:],
            color=bad_geometry[0].colors[0],
        )
        with self.assertRaisesRegex(analyze_prbm.EvidenceError, "pixel geometry"):
            analyze_prbm._validate_native_p2p_source(anchor, bad_geometry)

    def test_native_facade_cutout_and_short_anchor_source_goldens_reject_mutations(self) -> None:
        part = analyze_prbm.NativeStructuralPartContract(
            "down", "ae2:quartz_fiber", "network", None, None
        )
        anchor = type("FacadeCutoutSourceAnchor", (), {})()
        anchor.position = (0, 0, 0)
        anchor.face_parts = (
            analyze_prbm.FacePartContract("down", "ae2:quartz_fiber", None),
        )
        anchor.facades = (
            analyze_prbm.FacadeContract(
                "down", analyze_prbm.canonical_json({"Name": "minecraft:stone"})
            ),
        )
        anchor.native_structural = analyze_prbm.NativeStructuralContract(
            "ae2:fluix_glass_cable", (part,), None, None, None, (), None, None, None, 0
        )
        allowed = (0.0, 6 / 16, 10 / 16, 1.0)
        stone = [
            _manual_native_record(
                "minecraft:block/stone",
                (
                    (allowed[index % 4], 0.0, allowed[(index + 1) % 4]),
                    (allowed[(index + 2) % 4], analyze_prbm.NATIVE_STRUCTURAL_FACADE_THICKNESS, allowed[(index + 3) % 4]),
                    (allowed[(index + 1) % 4], 0.0, allowed[(index + 2) % 4]),
                ),
                normal=(0, -127, 0),
            )
            for index in range(48)
        ]
        facts = analyze_prbm._validate_native_facade_source(anchor, stone)
        self.assertEqual("part-facade-cutout", facts[0]["kind"])
        bad = list(stone)
        bad[0] = _manual_native_record(
            "minecraft:block/stone",
            ((2 / 16, 0.0, 0.0),) + bad[0].positions[1:],
            normal=(0, -127, 0),
        )
        with self.assertRaisesRegex(analyze_prbm.EvidenceError, "collision-box union"):
            analyze_prbm._validate_native_facade_source(anchor, bad)

        # The same terminal cutout is projected through an EAST facade's Y/Z
        # plane.  This independently prevents the validator from accepting
        # only the historical DOWN/XZ orientation.
        east = type("EastTerminalFacadeSourceAnchor", (), {})()
        east.position = (0, 0, 0)
        east.face_parts = (
            analyze_prbm.FacePartContract("east", "ae2:terminal", 1),
        )
        east.facades = (
            analyze_prbm.FacadeContract(
                "east", analyze_prbm.canonical_json({"Name": "minecraft:stone"})
            ),
        )
        east_allowed = (0.0, 2 / 16, 14 / 16, 1.0)
        east_stone = [
            _manual_native_record(
                "minecraft:block/stone",
                (
                    (
                        1.0,
                        east_allowed[index % 4],
                        east_allowed[(index + 1) % 4],
                    ),
                    (
                        1.0 - analyze_prbm.NATIVE_STRUCTURAL_FACADE_THICKNESS,
                        east_allowed[(index + 2) % 4],
                        east_allowed[(index + 3) % 4],
                    ),
                    (
                        1.0,
                        east_allowed[(index + 1) % 4],
                        east_allowed[(index + 2) % 4],
                    ),
                ),
                normal=(127, 0, 0),
            )
            for index in range(48)
        ]
        east_facts = analyze_prbm._validate_native_facade_source(
            east, east_stone
        )
        self.assertEqual("ae2:terminal", east_facts[0]["part_id"])
        east_bad = list(east_stone)
        east_bad[0] = _manual_native_record(
            "minecraft:block/stone",
            ((1.0, 3 / 16, 0.0),) + east_bad[0].positions[1:],
            normal=(127, 0, 0),
        )
        with self.assertRaisesRegex(
            analyze_prbm.EvidenceError, "collision-box union"
        ):
            analyze_prbm._validate_native_facade_source(east, east_bad)

        short = type("ShortAnchorSourceAnchor", (), {})()
        short.position = (0, 0, 0)
        short.face_parts = (
            analyze_prbm.FacePartContract("north", "ae2:cable_anchor", None),
        )
        short.facades = (
            analyze_prbm.FacadeContract(
                "north", analyze_prbm.canonical_json({"Name": "minecraft:stone"})
            ),
        )
        short.native_structural = analyze_prbm.NativeStructuralContract(
            "ae2:fluix_covered_cable",
            (analyze_prbm.NativeStructuralPartContract("north", "ae2:cable_anchor", "structural", None, None),),
            None,
            None,
            None,
            (),
            None,
            None,
            None,
            0,
        )
        full_slab = [
            _manual_native_record(
                "minecraft:block/stone",
                ((0.0, 0.0, 0.0), (1.0, 1.0, 0.0), (0.0, 1.0, analyze_prbm.NATIVE_STRUCTURAL_FACADE_THICKNESS)),
                normal=(0, 0, -127),
            )
            for _ in range(12)
        ]
        short_box = [
            _manual_native_record(
                "ae2:part/cable_anchor",
                ((7 / 16, 7 / 16, 1 / 16), (9 / 16, 9 / 16, 6 / 16), (7 / 16, 9 / 16, 6 / 16)),
                normal=(0, 0, -127),
            )
            for _ in range(12)
        ]
        facts = analyze_prbm._validate_native_facade_source(
            short, full_slab + short_box
        )
        self.assertEqual("same-face-short-anchor-no-cutout", facts[0]["kind"])

        # A different-face facade uses the same material for its short stilt
        # as the installed full cable anchor. Geometry, not aggregate count,
        # must distinguish the two twelve-triangle components.
        mixed = type("InstalledAnchorAndFacadeStiltSourceAnchor", (), {})()
        mixed.position = (0, 0, 0)
        mixed.face_parts = (
            analyze_prbm.FacePartContract("north", "ae2:cable_anchor", None),
        )
        mixed.facades = (
            analyze_prbm.FacadeContract(
                "up", analyze_prbm.canonical_json({"Name": "minecraft:stone"})
            ),
        )

        def anchor_box_records(
            direction: str, *, short_component: bool
        ) -> list[analyze_prbm.TriangleRecord]:
            minimum, maximum = (
                analyze_prbm._native_cable_anchor_component_bounds(
                    direction, short=short_component
                )
            )
            triangle = (
                minimum,
                maximum,
                (minimum[0], maximum[1], maximum[2]),
            )
            return [
                _manual_native_record("ae2:part/cable_anchor", triangle)
                for _ in range(12)
            ]

        installed = anchor_box_records("north", short_component=False)
        facade_stilt = anchor_box_records("up", short_component=True)
        components = analyze_prbm._validate_native_cable_anchor_components(
            mixed,
            installed + facade_stilt,
            {"north": mixed.face_parts[0]},
        )
        self.assertEqual(
            {"installed-cable-anchor", "facade-only-short-stilt"},
            {component["role"] for component in components},
        )
        swapped = list(installed + facade_stilt)
        swapped[0] = facade_stilt[0]
        with self.assertRaisesRegex(
            analyze_prbm.EvidenceError, "triangle closure|component leakage"
        ):
            analyze_prbm._validate_native_cable_anchor_components(
                mixed,
                swapped,
                {"north": mixed.face_parts[0]},
            )

    def test_native_facade_state_uv_cull_inset_and_corner_source_goldens(self) -> None:
        epsilon = analyze_prbm.NATIVE_STRUCTURAL_CORNER_KICKER_EPSILON
        self.assertTrue(
            analyze_prbm._native_corner_kicker_matches(
                (1.0 + epsilon / 2, 0.0, 1.0 - epsilon / 2),
                (1.0, 0.0, 1.0),
            )
        )
        self.assertFalse(
            analyze_prbm._native_corner_kicker_matches(
                (1.0 + epsilon * 2, 0.0, 1.0), (1.0, 0.0, 1.0)
            )
        )
        self.assertTrue(
            analyze_prbm._native_face_stripper_matches_exact_bound(
                (1.0, 1.0, 1.0, 1.0), 1.0
            )
        )
        self.assertFalse(
            analyze_prbm._native_face_stripper_matches_exact_bound(
                (1.0, 1.0, 1.0, 1.0 - epsilon / 2), 1.0
            )
        )
        normal_by_direction = {
            "down": (0, -127, 0),
            "up": (0, 127, 0),
            "north": (0, 0, -127),
            "south": (0, 0, 127),
            "west": (-127, 0, 0),
            "east": (127, 0, 0),
        }

        def log_records(axis: str) -> list[analyze_prbm.TriangleRecord]:
            top_directions = {
                "x": {"west", "east"},
                "y": {"down", "up"},
                "z": {"north", "south"},
            }[axis]
            top_uvs = (
                ((0.0, 0.0), (analyze_prbm.NATIVE_STRUCTURAL_FACADE_THICKNESS, 0.0), (0.0, 1.0))
                if axis == "x"
                else DEFAULT_UVS
            )
            records = []
            for direction, normal in normal_by_direction.items():
                material = (
                    "minecraft:block/oak_log_top"
                    if direction in top_directions
                    else "minecraft:block/oak_log"
                )
                records.extend(
                    _manual_native_record(
                        material,
                        ((0.0, 0.0, 0.0), (1.0, 0.0, 0.0), (0.0, 1.0, 1.0)),
                        normal=normal,
                        uvs=top_uvs if material.endswith("_top") else DEFAULT_UVS,
                    )
                    for _ in range(2)
                )
            return records

        axis_x = type("AxisXLogFacadeSourceAnchor", (), {})()
        axis_x.position = (0, 0, 0)
        axis_x.case_id = "ae2-s1-21"
        axis_x.face_parts = ()
        axis_x.facades = (
            analyze_prbm.FacadeContract(
                "down",
                analyze_prbm.canonical_json(
                    {"Name": "minecraft:oak_log", "Properties": {"axis": "x"}}
                ),
            ),
        )
        x_records = log_records("x")
        facts = analyze_prbm._validate_native_facade_source(axis_x, x_records)
        self.assertEqual(
            [analyze_prbm.NATIVE_STRUCTURAL_FACADE_THICKNESS, 1.0],
            facts[0]["uv_spans"],
        )
        bad_uv = list(x_records)
        first_top = next(
            index
            for index, record in enumerate(bad_uv)
            if record.material_identity == "minecraft:block/oak_log_top"
        )
        bad_uv[first_top] = _manual_native_record(
            bad_uv[first_top].material_identity,
            bad_uv[first_top].positions,
            normal=bad_uv[first_top].normals[0],
            uvs=DEFAULT_UVS,
        )
        with self.assertRaisesRegex(analyze_prbm.EvidenceError, "reinterpolated UV"):
            analyze_prbm._validate_native_facade_source(axis_x, bad_uv)

        axis_y = type("AxisYLogNoCullSourceAnchor", (), {})()
        axis_y.position = (0, 0, 0)
        axis_y.case_id = "ae2-s1-21"
        axis_y.face_parts = ()
        axis_y.facades = (
            analyze_prbm.FacadeContract(
                "up",
                analyze_prbm.canonical_json(
                    {"Name": "minecraft:oak_log", "Properties": {"axis": "y"}}
                ),
            ),
        )
        y_records = log_records("y")
        facts = analyze_prbm._validate_native_facade_source(axis_y, y_records)
        self.assertEqual(4, facts[0]["top_triangle_count"])
        with self.assertRaisesRegex(analyze_prbm.EvidenceError, "no-cull"):
            analyze_prbm._validate_native_facade_source(
                axis_y,
                [
                    record
                    for record in y_records
                    if analyze_prbm._record_direction(record) != "up"
                ],
            )

        glass = type("GlassCullSourceAnchor", (), {})()
        glass.position = (0, 0, 0)
        glass.case_id = "ae2-s1-20"
        glass.face_parts = ()
        glass.facades = (
            analyze_prbm.FacadeContract(
                "up", analyze_prbm.canonical_json({"Name": "minecraft:glass"})
            ),
        )
        glass_records = [
            _manual_native_record(
                analyze_prbm.NATIVE_STRUCTURAL_GLASSENTIAL_MATERIAL,
                ((0.0, 0.0, 0.0), (1.0, 0.0, 0.0), (0.0, 1.0, 1.0)),
                normal=normal_by_direction[direction],
            )
            for direction in ("down", "north", "south", "west", "east")
            for _ in range(2)
        ]
        facts = analyze_prbm._validate_native_facade_source(glass, glass_records)
        self.assertEqual("same-state-glass-skip-rendering", facts[0]["kind"])
        with self.assertRaisesRegex(analyze_prbm.EvidenceError, "skipRendering"):
            analyze_prbm._validate_native_facade_source(
                glass,
                [
                    replace(record, material_identity="minecraft:block/glass")
                    for record in glass_records
                ],
            )
        with self.assertRaisesRegex(analyze_prbm.EvidenceError, "skipRendering"):
            analyze_prbm._validate_native_facade_source(
                glass,
                glass_records
                + [
                    _manual_native_record(
                        analyze_prbm.NATIVE_STRUCTURAL_GLASSENTIAL_MATERIAL,
                        ((0.0, 1.0, 0.0), (1.0, 1.0, 0.0), (0.0, 1.0, 1.0)),
                        normal=normal_by_direction["up"],
                    )
                    for _ in range(2)
                ],
            )

        vibrant = type("VibrantQuartzFacadeLightSourceAnchor", (), {})()
        vibrant.position = (0, 0, 0)
        vibrant.case_id = "ae2-s1-14"
        vibrant.face_parts = ()
        vibrant.facades = (
            analyze_prbm.FacadeContract(
                "up",
                analyze_prbm.canonical_json(
                    {"Name": "ae2:quartz_vibrant_glass"}
                ),
            ),
        )
        vibrant_record = _manual_native_record(
            analyze_prbm.CONNECTED_GLASS_BASE_RESOURCES[0],
            ((0.0, 1.0, 0.0), (1.0, 1.0, 0.0), (0.0, 1.0, 1.0)),
        )
        facts = analyze_prbm._validate_native_facade_source(
            vibrant, [vibrant_record]
        )
        self.assertTrue(facts[0]["non_emissive"])
        self.assertEqual(0, facts[0]["source_layer_light_emission"])
        ambient_full_blocklight = _manual_native_record(
            vibrant_record.material_identity,
            vibrant_record.positions,
            blocklight=15,
            sunlight=14,
        )
        analyze_prbm._validate_native_facade_source(
            vibrant, [ambient_full_blocklight]
        )
        with self.assertRaisesRegex(analyze_prbm.EvidenceError, "not flat"):
            analyze_prbm._validate_native_facade_source(
                vibrant,
                [
                    _manual_native_record(
                        vibrant_record.material_identity,
                        vibrant_record.positions,
                        blocklights=(15, 14, 15),
                    )
                ],
            )

        inset = type("TransparentInsetSourceAnchor", (), {})()
        inset.position = (0, 0, 0)
        inset.case_id = "ae2-s1-22"
        inset.face_parts = ()
        inset.facades = (
            analyze_prbm.FacadeContract(
                "up", analyze_prbm.canonical_json({"Name": "minecraft:glass"})
            ),
            analyze_prbm.FacadeContract(
                "west", analyze_prbm.canonical_json({"Name": "minecraft:stone"})
            ),
        )
        inset_record = _manual_native_record(
            analyze_prbm.NATIVE_STRUCTURAL_GLASSENTIAL_MATERIAL,
            (
                (analyze_prbm.NATIVE_STRUCTURAL_FACADE_THICKNESS, 1.0, 0.0),
                (1.0, 1.0, 0.0),
                (analyze_prbm.NATIVE_STRUCTURAL_FACADE_THICKNESS, 1.0, 1.0),
            ),
        )
        facts = analyze_prbm._validate_native_facade_source(inset, [inset_record])
        self.assertEqual("transparent-opaque-inset", facts[0]["kind"])
        with self.assertRaisesRegex(analyze_prbm.EvidenceError, "opaque-edge inset"):
            analyze_prbm._validate_native_facade_source(
                inset,
                [
                    _manual_native_record(
                        analyze_prbm.NATIVE_STRUCTURAL_GLASSENTIAL_MATERIAL,
                        ((0.0, 1.0, 0.0), (1.0, 1.0, 0.0), (0.0, 1.0, 1.0)),
                    )
                ],
            )

        corner = type("OpaqueCornerSourceAnchor", (), {})()
        corner.position = (0, 0, 0)
        corner.case_id = "ae2-s1-22"
        corner.face_parts = ()
        corner.facades = tuple(
            analyze_prbm.FacadeContract(
                direction, analyze_prbm.canonical_json({"Name": "minecraft:stone"})
            )
            for direction in ("north", "east", "up")
        )
        corner_record = _manual_native_record(
            "minecraft:block/stone",
            (
                (0.0, 1.0 - analyze_prbm.NATIVE_STRUCTURAL_FACADE_THICKNESS, analyze_prbm.NATIVE_STRUCTURAL_FACADE_THICKNESS),
                (0.0, 1.0, 0.0),
                (0.0, 1.0, 1.0),
            ),
            normal=normal_by_direction["west"],
        )
        facts = analyze_prbm._validate_native_facade_source(corner, [corner_record])
        self.assertEqual("opaque-inner-corner-kick", facts[0]["kind"])
        with self.assertRaisesRegex(analyze_prbm.EvidenceError, "corner kick"):
            analyze_prbm._validate_native_facade_source(
                corner,
                [
                    _manual_native_record(
                        "minecraft:block/stone",
                        ((0.0, 1.0, 0.0), (0.0, 1.0, 1.0), (0.0, 0.0, 0.0)),
                        normal=normal_by_direction["west"],
                    )
                ],
            )

    def test_fully_surrounded_quartz_zero_layer_remains_custom_source_golden(
        self,
    ) -> None:
        golden = json.loads(
            NATIVE_STRUCTURAL_GOLDENS_PATH.read_text(encoding="utf-8")
        )["facades"]["fully_surrounded_quartz"]
        anchor = type("FullySurroundedQuartzSourceAnchor", (), {})()
        anchor.position = (0, 0, 0)
        anchor.case_id = "ae2-s1-19"
        anchor.face_parts = ()
        anchor.facades = tuple(
            analyze_prbm.FacadeContract(
                direction,
                analyze_prbm.canonical_json({"Name": "ae2:quartz_glass"}),
            )
            for direction in analyze_prbm.DIRECTION_VECTORS
        )
        stilt_records = []
        for direction in analyze_prbm.DIRECTION_VECTORS:
            minimum, maximum = (
                analyze_prbm._native_cable_anchor_component_bounds(
                    direction, short=True
                )
            )
            triangle = (
                minimum,
                maximum,
                (minimum[0], maximum[1], maximum[2]),
            )
            stilt_records.extend(
                _manual_native_record("ae2:part/cable_anchor", triangle)
                for _ in range(12)
            )
        triangle = (
            (5 / 16, 5 / 16, 5 / 16),
            (11 / 16, 5 / 16, 5 / 16),
            (5 / 16, 11 / 16, 11 / 16),
        )
        cable_records = [
            _manual_native_record(
                "ae2:part/cable/core/covered/transparent", triangle
            )
            for _ in range(golden["cable_triangle_count"])
        ]
        records = stilt_records + cable_records
        facts = analyze_prbm._validate_native_facade_source(anchor, records)
        self.assertEqual(
            {
                "kind": "fully-surrounded-quartz-zero-facade-layers",
                **golden,
            },
            facts[0],
        )

        mutations = {
            "facade-layer": records
            + [
                _manual_native_record(
                    analyze_prbm.CONNECTED_GLASS_BASE_RESOURCES[0], triangle
                )
            ],
            "missing-stilt": records[1:],
            "missing-cable": records[:-1],
            "stilt-ao": [
                _manual_native_record(
                    stilt_records[0].material_identity,
                    stilt_records[0].positions,
                    ao=254,
                ),
                *records[1:],
            ],
        }
        for label, mutation in mutations.items():
            with self.subTest(label=label):
                with self.assertRaises(analyze_prbm.EvidenceError):
                    analyze_prbm._validate_native_facade_source(
                        anchor, mutation
                    )

    def test_native_level_emitter_source_golden_forces_covered_world_lit_core(self) -> None:
        part = analyze_prbm.NativeStructuralPartContract(
            "down", "ae2:level_emitter", "emitter", None, None
        )
        anchor = type("EmitterCoreSourceAnchor", (), {})()
        anchor.position = (0, 0, 0)
        anchor.case_id = "ae2-s1-01"
        anchor.face_parts = ()
        anchor.facades = ()
        anchor.native_structural = analyze_prbm.NativeStructuralContract(
            "ae2:fluix_glass_cable",
            (part,),
            None,
            None,
            None,
            (),
            None,
            None,
            None,
            0,
        )
        positions = (
            (5 / 16, 5 / 16, 5 / 16),
            (11 / 16, 5 / 16, 5 / 16),
            (5 / 16, 11 / 16, 11 / 16),
        )
        records = [
            _manual_native_record(
                "ae2:part/cable/core/covered/transparent", positions
            )
            for _ in range(12)
        ]
        result = analyze_prbm._validate_native_structural_source_semantics(
            anchor, records
        )
        self.assertEqual("smart-requesting-part-core", result["facts"][0]["kind"])
        self.assertFalse(result["facts"][0]["emissive"])
        with self.assertRaisesRegex(analyze_prbm.EvidenceError, "covered core"):
            analyze_prbm._validate_native_structural_source_semantics(
                anchor,
                [
                    _manual_native_record(
                        "ae2:part/cable/core/glass/transparent", positions
                    )
                    for _ in range(12)
                ],
            )
        varied_world_light = list(records)
        varied_world_light[0] = _manual_native_record(
            varied_world_light[0].material_identity,
            varied_world_light[0].positions,
            blocklight=6,
            sunlight=14,
        )
        analyze_prbm._validate_native_structural_source_semantics(
            anchor, varied_world_light
        )
        bad_light = list(records)
        bad_light[0] = _manual_native_record(
            bad_light[0].material_identity,
            bad_light[0].positions,
            sunlights=(15, 14, 15),
        )
        with self.assertRaisesRegex(analyze_prbm.EvidenceError, "not flat"):
            analyze_prbm._validate_native_structural_source_semantics(
                anchor, bad_light
            )

    def test_native_endpoint_straight_source_golden_rejects_collar_leakage(self) -> None:
        endpoint = analyze_prbm.NativeStructuralEndpointContract(
            "east",
            "ae2:energy_acceptor",
            "ae2:energy_acceptor",
            "{}",
            "west",
            "ALL",
            None,
            True,
            "covered",
            "covered",
            "covered",
            False,
            "native-grid-node-host",
        )
        opposite = analyze_prbm.NativeStructuralEndpointContract(
            "west",
            "ae2:energy_acceptor",
            "ae2:energy_acceptor",
            "{}",
            "east",
            "ALL",
            None,
            True,
            "covered",
            "covered",
            "covered",
            False,
            "native-grid-node-host",
        )
        straight = {
            "directions": ["west", "east"],
            "effective_family": "covered",
            "enabled": True,
            "facades_are_attachments": False,
            "cable_anchor_requires_connection": False,
            "blocking_part": None,
            "machine_collars": False,
        }
        anchor = type("StraightSourceAnchor", (), {})()
        anchor.position = (0, 0, 0)
        anchor.native_structural = analyze_prbm.NativeStructuralContract(
            "ae2:fluix_covered_cable",
            (),
            None,
            None,
            None,
            (endpoint, opposite),
            analyze_prbm.canonical_json(straight),
            None,
            None,
            0,
        )
        def straight_box_records(
            material: str,
            endpoint_ao: int,
        ) -> list[analyze_prbm.TriangleRecord]:
            low, high = 5 / 16, 11 / 16
            quads = {
                "west": (
                    (0.0, low, low),
                    (0.0, low, high),
                    (0.0, high, high),
                    (0.0, high, low),
                ),
                "east": (
                    (1.0, low, low),
                    (1.0, high, low),
                    (1.0, high, high),
                    (1.0, low, high),
                ),
                "down": (
                    (0.0, low, low),
                    (1.0, low, low),
                    (1.0, low, high),
                    (0.0, low, high),
                ),
                "up": (
                    (0.0, high, low),
                    (0.0, high, high),
                    (1.0, high, high),
                    (1.0, high, low),
                ),
                "north": (
                    (0.0, low, low),
                    (0.0, high, low),
                    (1.0, high, low),
                    (1.0, low, low),
                ),
                "south": (
                    (0.0, low, high),
                    (1.0, low, high),
                    (1.0, high, high),
                    (0.0, high, high),
                ),
            }
            normals = {
                "west": (-127, 0, 0),
                "east": (127, 0, 0),
                "down": (0, -127, 0),
                "up": (0, 127, 0),
                "north": (0, 0, -127),
                "south": (0, 0, 127),
            }
            result = []
            blocklight = 15 if "/channels_" in material else 0
            for direction, quad in quads.items():
                ao = endpoint_ao if direction in {"west", "east"} else 255
                result.extend(
                    (
                        _manual_native_record(
                            material,
                            (quad[0], quad[1], quad[2]),
                            normal=normals[direction],
                            ao=ao,
                            blocklight=blocklight,
                        ),
                        _manual_native_record(
                            material,
                            (quad[0], quad[2], quad[3]),
                            normal=normals[direction],
                            ao=ao,
                            blocklight=blocklight,
                        ),
                    )
                )
            return result

        records = straight_box_records(
            "ae2:part/cable/covered/transparent", 63
        )
        fact = analyze_prbm._validate_native_straight_source(anchor, records)
        self.assertEqual(False, fact["machine_collars"])
        self.assertEqual(
            {"east": 63, "west": 63}, fact["endpoint_ao_raw_u8"]
        )
        varied_world_light = list(records)
        varied_world_light[0] = _manual_native_record(
            varied_world_light[0].material_identity,
            varied_world_light[0].positions,
            normal=(-127, 0, 0),
            ao=63,
            blocklight=8,
            sunlight=14,
        )
        analyze_prbm._validate_native_straight_source(
            anchor, varied_world_light
        )
        bad = list(records)
        bad[0] = _manual_native_record(
            bad[0].material_identity,
            ((0.2, 0.2, 0.2),) + bad[0].positions[1:],
            normal=(-127, 0, 0),
            ao=63,
        )
        with self.assertRaisesRegex(analyze_prbm.EvidenceError, "arm/collar"):
            analyze_prbm._validate_native_straight_source(anchor, bad)

        bad_ao = list(records)
        bad_ao[0] = _manual_native_record(
            bad_ao[0].material_identity,
            bad_ao[0].positions,
            normal=(-127, 0, 0),
            ao=255,
        )
        with self.assertRaisesRegex(
            analyze_prbm.EvidenceError, "endpoint-adjacency AO"
        ):
            analyze_prbm._validate_native_straight_source(anchor, bad_ao)

        wap_endpoint = analyze_prbm.NativeStructuralEndpointContract(
            "east",
            "ae2:wireless_access_point",
            "ae2:wireless_access_point",
            "{}",
            "west",
            "BACK",
            None,
            True,
            "smart",
            "smart",
            "smart",
            False,
            "native-grid-node-host",
        )
        wap_opposite = analyze_prbm.NativeStructuralEndpointContract(
            "west",
            "ae2:wireless_access_point",
            "ae2:wireless_access_point",
            "{}",
            "east",
            "BACK",
            None,
            True,
            "smart",
            "smart",
            "smart",
            False,
            "native-grid-node-host",
        )
        wap_straight = {**straight, "effective_family": "smart"}
        wap_anchor = type("NonOccludingStraightSourceAnchor", (), {})()
        wap_anchor.position = (0, 0, 0)
        wap_anchor.native_structural = analyze_prbm.NativeStructuralContract(
            "ae2:fluix_smart_cable",
            (),
            None,
            None,
            None,
            (wap_endpoint, wap_opposite),
            analyze_prbm.canonical_json(wap_straight),
            None,
            None,
            0,
        )
        wap_records = [
            record
            for material in (
                "ae2:part/cable/smart/transparent",
                "ae2:part/cable/smart/channels_00",
                "ae2:part/cable/smart/channels_10",
            )
            for record in straight_box_records(material, 255)
        ]
        wap_fact = analyze_prbm._validate_native_straight_source(
            wap_anchor, wap_records
        )
        self.assertEqual(
            {"east": 255, "west": 255}, wap_fact["endpoint_ao_raw_u8"]
        )
        self.assertEqual(24, wap_fact["forced_fullbright_triangle_count"])
        channel_index = next(
            index
            for index, record in enumerate(wap_records)
            if record.material_identity.endswith("/channels_00")
        )
        for channel in ("blocklight", "sunlight"):
            bad_channel_light = list(wap_records)
            channel_record = bad_channel_light[channel_index]
            bad_channel_light[channel_index] = _manual_native_record(
                channel_record.material_identity,
                channel_record.positions,
                normal=channel_record.normals[0],
                ao=channel_record.aos[0],
                blocklight=14 if channel == "blocklight" else 15,
                sunlight=14 if channel == "sunlight" else 15,
            )
            with self.subTest(channel=channel):
                with self.assertRaisesRegex(
                    analyze_prbm.EvidenceError, "exact 15/15"
                ):
                    analyze_prbm._validate_native_straight_source(
                        wap_anchor, bad_channel_light
                    )
        bad_wap_ao = list(wap_records)
        bad_wap_ao[0] = _manual_native_record(
            bad_wap_ao[0].material_identity,
            bad_wap_ao[0].positions,
            normal=(-127, 0, 0),
            ao=63,
        )
        with self.assertRaisesRegex(
            analyze_prbm.EvidenceError, "endpoint-adjacency AO"
        ):
            analyze_prbm._validate_native_straight_source(
                wap_anchor, bad_wap_ao
            )

    def test_parser_rejects_nonzero_padding_and_noncontiguous_groups(self) -> None:
        payload = base64.b64decode(GOLDEN_PATH.read_bytes().strip(), validate=True)

        invalid_padding = bytearray(payload)
        invalid_padding[18] = 1
        with self.assertRaisesRegex(analyze_prbm.EvidenceError, "padding"):
            analyze_prbm.parse_prbm(invalid_padding)

        invalid_group = bytearray(payload)
        struct.pack_into("<i", invalid_group, len(invalid_group) - 12, 1)
        with self.assertRaisesRegex(analyze_prbm.EvidenceError, "contiguous partition"):
            analyze_prbm.parse_prbm(invalid_group)

    def test_inward_ownership_handles_all_six_block_faces(self) -> None:
        x, y, z = (10, 20, 30)
        triangles = (
            (
                (x + 0.2, y + 1, z + 0.2),
                (x + 0.2, y + 1, z + 0.8),
                (x + 0.8, y + 1, z + 0.2),
            ),
            (
                (x + 0.2, y, z + 0.2),
                (x + 0.8, y, z + 0.2),
                (x + 0.2, y, z + 0.8),
            ),
            (
                (x + 1, y + 0.2, z + 0.2),
                (x + 1, y + 0.8, z + 0.2),
                (x + 1, y + 0.2, z + 0.8),
            ),
            (
                (x, y + 0.2, z + 0.2),
                (x, y + 0.2, z + 0.8),
                (x, y + 0.8, z + 0.2),
            ),
            (
                (x + 0.2, y + 0.2, z + 1),
                (x + 0.8, y + 0.2, z + 1),
                (x + 0.2, y + 0.8, z + 1),
            ),
            (
                (x + 0.2, y + 0.2, z),
                (x + 0.2, y + 0.8, z),
                (x + 0.8, y + 0.2, z),
            ),
        )
        for triangle in triangles:
            with self.subTest(triangle=triangle):
                self.assertEqual((x, y, z), analyze_prbm._triangle_owner(triangle))

    def test_file_grid_path_encoding_matches_positive_and_negative_coordinates(self) -> None:
        root = Path("map")
        self.assertEqual(
            root / "tiles/0/x1/2/z-3/4.prbm.gz",
            analyze_prbm.tile_path(root, 12, -34),
        )
        self.assertEqual(
            root / "tiles/0/x-1/z0.prbm.gz",
            analyze_prbm.tile_path(root, -1, 0),
        )

    def test_json_rejects_duplicate_keys_and_nonstandard_numbers(self) -> None:
        with self.assertRaisesRegex(analyze_prbm.EvidenceError, "duplicate"):
            analyze_prbm.parse_json_bytes(b'{"x":1,"x":2}', "fixture")
        with self.assertRaisesRegex(analyze_prbm.EvidenceError, "non-standard"):
            analyze_prbm.parse_json_bytes(b'{"x":NaN}', "fixture")

    def test_plain_input_is_bounded_before_complete_allocation(self) -> None:
        path = Path(self.temporary.name) / "oversized.json"
        path.write_bytes(b"12345")
        with self.assertRaisesRegex(analyze_prbm.EvidenceError, "exceeds 4 bytes"):
            analyze_prbm.read_bounded(path, 4, "fixture JSON")

    def test_gzip_input_and_payload_are_bounded_during_streaming(self) -> None:
        path = self.map_root / "textures.json.gz"
        compressed_limit = analyze_prbm.MAX_COMPRESSED_BYTES
        decompressed_limit = analyze_prbm.MAX_DECOMPRESSED_BYTES
        try:
            analyze_prbm.MAX_COMPRESSED_BYTES = 4
            with self.assertRaisesRegex(analyze_prbm.EvidenceError, "gzip input exceeds 4"):
                analyze_prbm.read_single_gzip(path)

            analyze_prbm.MAX_COMPRESSED_BYTES = compressed_limit
            analyze_prbm.MAX_DECOMPRESSED_BYTES = 4
            with self.assertRaisesRegex(analyze_prbm.EvidenceError, "gzip payload exceeds 4"):
                analyze_prbm.read_single_gzip(path)
        finally:
            analyze_prbm.MAX_COMPRESSED_BYTES = compressed_limit
            analyze_prbm.MAX_DECOMPRESSED_BYTES = decompressed_limit

    def test_analyzer_provenance_locks_exact_writer_and_loader_blobs(self) -> None:
        provenance = json.loads(ANALYZER_PROVENANCE_PATH.read_text(encoding="utf-8"))
        self.assertEqual(1, provenance["schema_version"])
        self.assertEqual("LGPL-3.0-only", provenance["project_license"])
        self.assertEqual(
            "9be321df995a1103808621d529eb72773e719d4d",
            provenance["bluemap"]["backport_commit"],
        )
        source_root_candidates = (
            PROJECT_ROOT.parent / "bluemap-backport",
            PROJECT_ROOT / ".ci" / "bluemap",
        )
        source_root = next(
            (candidate for candidate in source_root_candidates if candidate.is_dir()),
            None,
        )
        self.assertIsNotNone(source_root, "exact BlueMap source checkout is missing")
        source_root = source_root.resolve()
        head = subprocess.check_output(
            ("git", "-C", str(source_root), "rev-parse", "HEAD"),
            text=True,
        ).strip()
        self.assertEqual(provenance["bluemap"]["backport_commit"], head)
        source_blobs = provenance["bluemap"]["source_blobs"]
        self.assertGreaterEqual(len(source_blobs), 18)
        for relative_path, expected_blob in source_blobs.items():
            payload = (source_root / relative_path).read_bytes()
            actual_blob = hashlib.sha1(
                f"blob {len(payload)}\0".encode("ascii") + payload,
                usedforsecurity=False,
            ).hexdigest()
            self.assertEqual(expected_blob, actual_blob, relative_path)

    def test_material_ordinal_must_exist_in_matching_texture_gallery(self) -> None:
        invalid_root = Path(self.temporary.name) / "invalid-material"
        build_fixture(invalid_root, invalid_material=True)
        with self.assertRaisesRegex(analyze_prbm.EvidenceError, "uses material"):
            analyze_prbm.analyze(invalid_root, CASES_PATH)

        invalid_unselected_root = Path(self.temporary.name) / "invalid-unselected"
        build_fixture(invalid_unselected_root, invalid_unselected_material=True)
        with self.assertRaisesRegex(analyze_prbm.EvidenceError, "uses material"):
            analyze_prbm.analyze(invalid_unselected_root, CASES_PATH)

    def test_texture_gallery_requires_unique_resolved_resource_paths(self) -> None:
        invalid_entries = (
            [None],
            [{}],
            [{"resourcePath": ""}],
            [
                {"resourcePath": "minecraft:block/stone"},
                {"resourcePath": "minecraft:block/stone"},
            ],
        )
        for index, entries in enumerate(invalid_entries):
            with self.subTest(index=index):
                root = Path(self.temporary.name) / f"invalid-textures-{index}"
                root.mkdir()
                _write_gzip(
                    root / "textures.json.gz",
                    json.dumps(entries, separators=(",", ":")).encode("utf-8"),
                )
                with self.assertRaises(analyze_prbm.EvidenceError):
                    analyze_prbm.parse_textures(root)

    def test_float_canonicalization_normalizes_zero_and_rejects_nonfinite(self) -> None:
        self.assertEqual("0x0p+0", analyze_prbm.canonical_float(0.0))
        self.assertEqual("0x0p+0", analyze_prbm.canonical_float(-0.0))
        self.assertEqual("0x1.8p-1", analyze_prbm.canonical_float(0.75))
        for value in (math.inf, -math.inf, math.nan):
            with self.assertRaises(analyze_prbm.EvidenceError):
                analyze_prbm.canonical_float(value)


if __name__ == "__main__":
    unittest.main()
