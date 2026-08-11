# SPDX-License-Identifier: LGPL-3.0-only

import hashlib
import sys
import tempfile
import unittest
from pathlib import Path
import zipfile


ROOT = Path(__file__).resolve().parents[2]
sys.path.insert(0, str(ROOT / "tools"))

from verify_m45_artifacts import (  # noqa: E402
    ARTIFACTS,
    CELL_MODEL_CATALOG,
    CELL_MODEL_ROWS,
    DEFAULT_RESOURCE_ROOT,
    RESOURCE_MANIFESTS,
    ArtifactIdentity,
    parse_resource_manifest,
    verify_cell_model_catalog,
    verify_exact_identity,
    verify_resource_manifest,
    verify_resource_manifest_set,
)


class VerifyM45ArtifactsTest(unittest.TestCase):
    def test_exact_artifact_set_is_closed(self):
        self.assertEqual(
            {
                "ae2",
                "appflux",
                "merequester",
                "expandedae",
                "megacells",
                "advancedae",
                "athena",
                "extendedae",
            },
            set(ARTIFACTS),
        )
        self.assertEqual(8_230_896, ARTIFACTS["ae2"].size)
        self.assertEqual(345_117, ARTIFACTS["appflux"].size)
        self.assertEqual(184_517, ARTIFACTS["merequester"].size)
        self.assertEqual(496_713, ARTIFACTS["expandedae"].size)
        self.assertEqual(1_137_276, ARTIFACTS["megacells"].size)
        self.assertEqual(4_791_255, ARTIFACTS["advancedae"].size)
        self.assertEqual(99_944, ARTIFACTS["athena"].size)
        self.assertEqual(5_578_031, ARTIFACTS["extendedae"].size)

    def test_resource_manifest_mapping_is_closed(self):
        self.assertEqual(11, len(RESOURCE_MANIFESTS))
        self.assertEqual(375, sum(item.expected_rows for item in RESOURCE_MANIFESTS))
        mapping = {
            manifest.relative_path: manifest.artifact
            for manifest in RESOURCE_MANIFESTS
        }
        self.assertEqual(
            "ae2",
            mapping[
                "megacells/4.11.0/required-dependent-ae2-resources.tsv"
            ],
        )
        self.assertEqual(
            "extendedae",
            mapping[
                "extendedae/1.21-2.2.33-neoforge/required-resources.tsv"
            ],
        )
        self.assertEqual(
            "extendedae",
            mapping[
                "extendedae/1.21-2.2.35-neoforge/m5-required-resources.tsv"
            ],
        )
        for manifest in RESOURCE_MANIFESTS:
            rows = parse_resource_manifest(
                DEFAULT_RESOURCE_ROOT / manifest.relative_path
            )
            self.assertEqual(manifest.expected_rows, len(rows))
        verify_resource_manifest_set(DEFAULT_RESOURCE_ROOT)

    def test_identity_guard_checks_all_bytes(self):
        with tempfile.TemporaryDirectory() as temporary:
            path = Path(temporary) / "artifact.jar"
            path.write_bytes(b"exact artifact")
            identity = ArtifactIdentity(
                "fixture",
                path.stat().st_size,
                hashlib.sha1(path.read_bytes()).hexdigest(),
                hashlib.sha256(path.read_bytes()).hexdigest(),
                hashlib.sha512(path.read_bytes()).hexdigest(),
            )
            verify_exact_identity(path, identity)
            path.write_bytes(b"wrong artifact")
            with self.assertRaisesRegex(ValueError, "changed"):
                verify_exact_identity(path, identity)

    def test_manifest_parser_rejects_noncanonical_rows(self):
        with tempfile.TemporaryDirectory() as temporary:
            root = Path(temporary)
            path = root / "manifest.tsv"
            digest = hashlib.sha256(b"x").hexdigest()
            path.write_text(
                f"assets/test/z.txt\t1\t{digest}\n"
                f"assets/test/a.txt\t1\t{digest}\n",
                encoding="utf-8",
            )
            with self.assertRaisesRegex(ValueError, "not sorted"):
                parse_resource_manifest(path)
            path.write_text(
                f"../escape\t1\t{digest}\n",
                encoding="utf-8",
            )
            with self.assertRaisesRegex(ValueError, "unsafe"):
                parse_resource_manifest(path)

    def test_resource_rows_are_verified_against_archive_bytes(self):
        with tempfile.TemporaryDirectory() as temporary:
            root = Path(temporary)
            archive_path = root / "fixture.jar"
            resource = "assets/test/value.txt"
            raw = b"verified resource"
            with zipfile.ZipFile(archive_path, "w") as archive:
                archive.writestr(resource, raw)
            manifest_path = root / "manifest.tsv"
            manifest_path.write_text(
                f"{resource}\t{len(raw)}\t{hashlib.sha256(raw).hexdigest()}\n",
                encoding="utf-8",
            )
            with zipfile.ZipFile(archive_path) as archive:
                verify_resource_manifest(archive, manifest_path, 1)
                manifest_path.write_text(
                    f"{resource}\t{len(raw)}\t{'0' * 64}\n",
                    encoding="utf-8",
                )
                with self.assertRaisesRegex(ValueError, "SHA-256 changed"):
                    verify_resource_manifest(archive, manifest_path, 1)

    def test_mega_cell_catalog_is_fully_resource_backed(self):
        catalog_path = DEFAULT_RESOURCE_ROOT / CELL_MODEL_CATALOG
        resource_manifest = next(
            manifest
            for manifest in RESOURCE_MANIFESTS
            if manifest.relative_path
            == "megacells/4.11.0/required-cell-dock-resources.tsv"
        )
        resources = {
            row[0]
            for row in parse_resource_manifest(
                DEFAULT_RESOURCE_ROOT / resource_manifest.relative_path
            )
        }
        rows, models = verify_cell_model_catalog(catalog_path, resources)
        self.assertEqual(CELL_MODEL_ROWS, rows)
        self.assertEqual(37, models)
        with self.assertRaisesRegex(ValueError, "unaudited resources"):
            verify_cell_model_catalog(catalog_path, set())


if __name__ == "__main__":
    unittest.main()
