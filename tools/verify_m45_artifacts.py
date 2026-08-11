#!/usr/bin/env python3
# SPDX-License-Identifier: LGPL-3.0-only
"""Verify the exact All the Mons 1.2.0 M4/M5 artifact/resource closure."""

from __future__ import annotations

import argparse
from dataclasses import dataclass
import hashlib
from pathlib import Path, PurePosixPath
import re
import sys
import zipfile


PROJECT_ROOT = Path(__file__).resolve().parents[1]
DEFAULT_RESOURCE_ROOT = PROJECT_ROOT / "src/main/resources/bluemap-ae2/profiles"
SHA256_PATTERN = re.compile(r"[0-9a-f]{64}")


@dataclass(frozen=True)
class ArtifactIdentity:
    label: str
    size: int
    sha1: str
    sha256: str
    sha512: str


@dataclass(frozen=True)
class ResourceManifest:
    relative_path: str
    artifact: str
    expected_rows: int


ARTIFACTS = {
    "ae2": ArtifactIdentity(
        "Applied Energistics 2 19.2.17",
        8_230_896,
        "49c18d6a4af487957d7e5a6ad5dcbf71090b8e14",
        "460d779a0609b81409907d9956de8f6f70a1b0912257e3e5c3c7e75ac9630e95",
        "55edfd948366aff620881e0625e48c333a2cb847e73249bc0b588efbc4b867099"
        "92a8ffbca97ea387e270df4186fe7f74ee2f27b739f1c952e932becfb9dea33",
    ),
    "appflux": ArtifactIdentity(
        "AppliedFlux 1.21-2.1.5-neoforge",
        345_117,
        "a98eeadf414e6b3f6878324a3fbdee3fa5fcdadf",
        "57e6a2c0f38e660c9e8416f9081d8c515f5ad096d6793d7b7f039e8e210d245b",
        "27bb367ad2f6695a485e11bf6e9567a86eb2d817194f5bdb381d1e493c345e005"
        "5eefb727308ca28fd27cf170d4303788c22681c349065515841cfe7a8c4b01b",
    ),
    "merequester": ArtifactIdentity(
        "ME Requester 1.21.1-1.4.3",
        184_517,
        "b0a801e9b7af930da5f58176156d58c26f6232b1",
        "68f3c861a802d48afeb6e3a48e8ee4f8633904340ac3f89f17493dc84490e385",
        "4411670eaea8403414c773646cb9498291e843ba995100c4fca4ab53d5ab3aa0c"
        "bf913921403c8af1d32b67ab940bac8c8a16a910069f6f7e2902e42e11f66b7",
    ),
    "expandedae": ArtifactIdentity(
        "Expanded AE 2.1.1",
        496_713,
        "c4db013f83e569b016da329b3ddc9c14acc75d7d",
        "f39c0eb9c6271f54a44ffee092a29520f53000d1005849e6afada3ad9dffba14",
        "5d6b0c7430d6f1f2bdb2cb38832ee27d0b28402d16171a9fe746d0275ba54c28"
        "8405b64b9ad269c010aadd729e82ddeb61b9550c0361c6e1ece2c0bdc77a4b23",
    ),
    "megacells": ArtifactIdentity(
        "MEGA Cells 4.11.0",
        1_137_276,
        "f0b1a44bf30c8a9e14e2fa7fce37360191aa55e8",
        "a386bbf12afb11729b0dcf77f64221893d250f22e6185a4d728b9799b230bc55",
        "1f5c30f5c6516eae20eb3d8502eebc8f3fa43d42815ecd182beea2c244c7dacf"
        "450fa0fafa6f6f7ab836d7f68e6de2b2366fbb0eb2938823f3d370217a4e8671",
    ),
    "advancedae": ArtifactIdentity(
        "Advanced AE 1.6.12-1.21.1",
        4_791_255,
        "9358ccfa5477c7ab1c5ffab6c831e105fe46ecc3",
        "a01d9718667ac13899013e91c5b0b7708b9b9db1da9b8e380772dde54bbe8f41",
        "ab61c57355649a967a0bcf6b9413cd6b62728d26e914543b3231eea33bde5571"
        "536bd589ae1ac026d46799711508c942284c3419e19ff5d5bf80f1045442f33a",
    ),
    "athena": ArtifactIdentity(
        "Athena 4.0.6",
        99_944,
        "4bcbdf388bd5e387beca7c627224aac33584b55b",
        "43699885bbce3343916d4c5c4940cf0e3f9f6f02fdeb46e8655e121b42282ec5",
        "ab40a306a26ce834daae921a1e87768cd2538a4bfe27a4480f97af854084cc334"
        "e7416b1bd0b7583834a32a86951283f29fd4b1df7b98a967a6b26a3ec05e2cf",
    ),
    "extendedae": ArtifactIdentity(
        "ExtendedAE 1.21-2.2.35-neoforge",
        5_578_031,
        "e3521ca2567fabe0f0131cc923ec94dd99d6fa7b",
        "14a2860fa2c747e9dda2279b8933fac6311fecfee166c765171022b902591c65",
        "e5b76a50802087d999bf6c113bc635e8ade9f20e06f4d3276a144f4eaa3090fc"
        "3b6c67b9b6a1f7d0d036e48e69f601114a0cc92c5a8d45953f895718f806348c",
    ),
}


RESOURCE_MANIFESTS = (
    ResourceManifest(
        "appflux/1.21-2.1.5-neoforge/required-resources.tsv",
        "appflux",
        19,
    ),
    ResourceManifest(
        "merequester/1.21.1-1.4.3/required-resources.tsv",
        "merequester",
        12,
    ),
    ResourceManifest(
        "expandedae/2.1.1/required-resources.tsv", "expandedae", 142
    ),
    ResourceManifest(
        "megacells/4.11.0/required-cell-dock-resources.tsv", "megacells", 43
    ),
    ResourceManifest(
        "megacells/4.11.0/required-crafting-resources.tsv", "megacells", 28
    ),
    ResourceManifest(
        "megacells/4.11.0/required-generic-part-resources.tsv", "megacells", 11
    ),
    # MEGA composes these seven exact resources from the AE2 artifact.
    ResourceManifest(
        "megacells/4.11.0/required-dependent-ae2-resources.tsv", "ae2", 7
    ),
    ResourceManifest(
        "advancedae/1.6.12/quantum-required-resources.tsv", "advancedae", 48
    ),
    ResourceManifest(
        "advancedae/1.6.12/athena-required-resources.tsv", "advancedae", 12
    ),
    # ExtendedAE 2.2.35 retains the accepted Drive partition byte-exactly.
    ResourceManifest(
        "extendedae/1.21-2.2.33-neoforge/required-resources.tsv",
        "extendedae",
        15,
    ),
    ResourceManifest(
        "extendedae/1.21-2.2.35-neoforge/m5-required-resources.tsv",
        "extendedae",
        38,
    ),
)


CELL_MODEL_CATALOG = "megacells/4.11.0/cell-models.tsv"
CELL_MODEL_ROWS = 67
CELL_MODEL_RESOURCE_MANIFEST = (
    "megacells/4.11.0/required-cell-dock-resources.tsv"
)
MANIFEST_SCOPE_ROOTS = (
    "appflux/1.21-2.1.5-neoforge",
    "merequester/1.21.1-1.4.3",
    "expandedae/2.1.1",
    "megacells/4.11.0",
    "advancedae/1.6.12",
    "extendedae/1.21-2.2.33-neoforge",
    "extendedae/1.21-2.2.35-neoforge",
)


def digest(path: Path, algorithm: str) -> str:
    value = hashlib.new(algorithm)
    with path.open("rb") as source:
        for chunk in iter(lambda: source.read(64 * 1024), b""):
            value.update(chunk)
    return value.hexdigest()


def verify_exact_identity(path: Path, identity: ArtifactIdentity) -> None:
    if not path.is_file():
        raise ValueError(f"{identity.label} is not a regular file: {path}")
    if path.stat().st_size != identity.size:
        raise ValueError(
            f"{identity.label} size changed: got {path.stat().st_size}, "
            f"expected {identity.size}"
        )
    for algorithm in ("sha1", "sha256", "sha512"):
        actual = digest(path, algorithm)
        expected = getattr(identity, algorithm)
        if actual != expected:
            raise ValueError(
                f"{identity.label} {algorithm.upper()} changed: "
                f"got {actual}, expected {expected}"
            )


def _validate_resource_path(raw: str, label: str) -> str:
    path = PurePosixPath(raw)
    if (
        not raw.startswith("assets/")
        or raw.startswith("/")
        or "\\" in raw
        or path.as_posix() != raw
        or ".." in path.parts
    ):
        raise ValueError(f"{label}: unsafe or non-canonical resource path: {raw}")
    return raw


def parse_resource_manifest(path: Path) -> tuple[tuple[str, int, str], ...]:
    try:
        text = path.read_text(encoding="utf-8")
    except (OSError, UnicodeError) as error:
        raise ValueError(f"cannot read resource manifest {path}") from error
    if not text.endswith("\n"):
        raise ValueError(f"resource manifest must end with LF: {path}")
    rows: list[tuple[str, int, str]] = []
    for line_number, line in enumerate(text.splitlines(), start=1):
        columns = line.split("\t")
        label = f"{path}:{line_number}"
        if len(columns) != 3:
            raise ValueError(f"{label}: expected three tab-separated columns")
        resource = _validate_resource_path(columns[0], label)
        if not columns[1].isdigit() or str(int(columns[1])) != columns[1]:
            raise ValueError(f"{label}: non-canonical resource size")
        size = int(columns[1])
        if size <= 0:
            raise ValueError(f"{label}: resource size must be positive")
        if SHA256_PATTERN.fullmatch(columns[2]) is None:
            raise ValueError(f"{label}: invalid SHA-256")
        rows.append((resource, size, columns[2]))
    resources = [row[0] for row in rows]
    if resources != sorted(resources):
        raise ValueError(f"resource manifest is not sorted: {path}")
    if len(resources) != len(set(resources)):
        raise ValueError(f"resource manifest contains duplicate paths: {path}")
    return tuple(rows)


def verify_resource_manifest(
    archive: zipfile.ZipFile,
    manifest_path: Path,
    expected_rows: int,
) -> tuple[tuple[str, int, str], ...]:
    rows = parse_resource_manifest(manifest_path)
    if len(rows) != expected_rows:
        raise ValueError(
            f"{manifest_path}: got {len(rows)} rows, expected {expected_rows}"
        )
    for resource, expected_size, expected_sha256 in rows:
        try:
            raw = archive.read(resource)
        except KeyError as error:
            raise ValueError(
                f"{manifest_path}: artifact is missing {resource}"
            ) from error
        if len(raw) != expected_size:
            raise ValueError(
                f"{manifest_path}: {resource} size changed: "
                f"got {len(raw)}, expected {expected_size}"
            )
        actual_sha256 = hashlib.sha256(raw).hexdigest()
        if actual_sha256 != expected_sha256:
            raise ValueError(
                f"{manifest_path}: {resource} SHA-256 changed: "
                f"got {actual_sha256}, expected {expected_sha256}"
            )
    return rows


def verify_resource_manifest_set(resource_root: Path) -> None:
    expected = {manifest.relative_path for manifest in RESOURCE_MANIFESTS}
    actual: set[str] = set()
    for scope in MANIFEST_SCOPE_ROOTS:
        scope_root = resource_root / scope
        actual.update(
            path.relative_to(resource_root).as_posix()
            for path in scope_root.glob("*required*.tsv")
        )
    if actual != expected:
        missing = sorted(expected - actual)
        unexpected = sorted(actual - expected)
        raise ValueError(
            "M4/M5 resource manifest set changed: "
            f"missing={missing}, unexpected={unexpected}"
        )


def verify_cell_model_catalog(
    path: Path,
    cell_resource_paths: set[str],
) -> tuple[int, int]:
    try:
        text = path.read_text(encoding="utf-8")
    except (OSError, UnicodeError) as error:
        raise ValueError(f"cannot read MEGA Cells model catalog {path}") from error
    if not text.endswith("\n"):
        raise ValueError(f"MEGA Cells model catalog must end with LF: {path}")
    item_ids: list[str] = []
    model_resources: set[str] = set()
    for line_number, line in enumerate(text.splitlines(), start=1):
        columns = line.split("\t")
        label = f"{path}:{line_number}"
        if len(columns) != 3:
            raise ValueError(f"{label}: expected three tab-separated columns")
        item_id, model_id, kind = columns
        if item_id.count(":") != 1 or model_id.count(":") != 1:
            raise ValueError(f"{label}: invalid item or model identifier")
        if kind not in {"misc", "standard"}:
            raise ValueError(f"{label}: unsupported cell-model kind: {kind}")
        namespace, model_path = model_id.split(":", maxsplit=1)
        resource = f"assets/{namespace}/models/{model_path}.json"
        _validate_resource_path(resource, label)
        item_ids.append(item_id)
        model_resources.add(resource)
    if len(item_ids) != CELL_MODEL_ROWS:
        raise ValueError(
            f"{path}: got {len(item_ids)} rows, expected {CELL_MODEL_ROWS}"
        )
    if item_ids != sorted(item_ids) or len(item_ids) != len(set(item_ids)):
        raise ValueError(f"{path}: item IDs must be unique and sorted")
    missing = sorted(model_resources - cell_resource_paths)
    if missing:
        raise ValueError(
            "MEGA Cells model catalog references unaudited resources: "
            + ", ".join(missing)
        )
    return len(item_ids), len(model_resources)


def verify_artifacts(
    artifact_paths: dict[str, Path],
    resource_root: Path = DEFAULT_RESOURCE_ROOT,
) -> tuple[int, int, int, int]:
    if set(artifact_paths) != set(ARTIFACTS):
        raise ValueError("M4/M5 artifact set changed")
    verify_resource_manifest_set(resource_root)
    archives: dict[str, zipfile.ZipFile] = {}
    try:
        for key, identity in ARTIFACTS.items():
            artifact_path = artifact_paths[key]
            verify_exact_identity(artifact_path, identity)
            archive = zipfile.ZipFile(artifact_path)
            names = [entry.filename for entry in archive.infolist()]
            if len(names) != len(set(names)):
                raise ValueError(f"{identity.label} contains duplicate ZIP entries")
            archives[key] = archive

        verified_rows = 0
        cell_resource_paths: set[str] | None = None
        for manifest in RESOURCE_MANIFESTS:
            rows = verify_resource_manifest(
                archives[manifest.artifact],
                resource_root / manifest.relative_path,
                manifest.expected_rows,
            )
            verified_rows += len(rows)
            if manifest.relative_path == CELL_MODEL_RESOURCE_MANIFEST:
                cell_resource_paths = {row[0] for row in rows}
        if cell_resource_paths is None:
            raise ValueError("MEGA Cells resource manifest mapping is missing")
        catalog_rows, _catalog_models = verify_cell_model_catalog(
            resource_root / CELL_MODEL_CATALOG,
            cell_resource_paths,
        )
        return len(archives), len(RESOURCE_MANIFESTS), verified_rows, catalog_rows
    except zipfile.BadZipFile as error:
        raise ValueError(f"invalid M4/M5 JAR: {error}") from error
    finally:
        for archive in archives.values():
            archive.close()


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--ae2-jar", required=True, type=Path)
    parser.add_argument("--appflux-jar", required=True, type=Path)
    parser.add_argument("--me-requester-jar", required=True, type=Path)
    parser.add_argument("--expanded-ae-jar", required=True, type=Path)
    parser.add_argument("--mega-cells-jar", required=True, type=Path)
    parser.add_argument("--advanced-ae-jar", required=True, type=Path)
    parser.add_argument("--athena-jar", required=True, type=Path)
    parser.add_argument("--extended-ae-jar", required=True, type=Path)
    parser.add_argument(
        "--resource-root",
        type=Path,
        default=DEFAULT_RESOURCE_ROOT,
        help="profile-resource root (defaults to the checked-out project)",
    )
    return parser.parse_args()


def main() -> int:
    args = parse_args()
    artifact_paths = {
        "ae2": args.ae2_jar,
        "appflux": args.appflux_jar,
        "merequester": args.me_requester_jar,
        "expandedae": args.expanded_ae_jar,
        "megacells": args.mega_cells_jar,
        "advancedae": args.advanced_ae_jar,
        "athena": args.athena_jar,
        "extendedae": args.extended_ae_jar,
    }
    try:
        artifacts, manifests, resource_rows, catalog_rows = verify_artifacts(
            artifact_paths,
            args.resource_root,
        )
    except (OSError, ValueError, zipfile.BadZipFile) as error:
        print(f"M4/M5 artifact verification failed: {error}", file=sys.stderr)
        return 1
    print(
        "Verified exact All the Mons 1.2.0 M4/M5 closure: "
        f"{artifacts} artifacts, {manifests} resource manifests, "
        f"{resource_rows} resource rows, and {catalog_rows} MEGA cell rows."
    )
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
