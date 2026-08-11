# SPDX-License-Identifier: LGPL-3.0-only

import hashlib
import json
import sys
import unittest
from pathlib import Path


ROOT = Path(__file__).resolve().parents[2]
sys.path.insert(0, str(ROOT / "tools"))

from extendedae_contract import (  # noqa: E402
    BUILT_IN_CELL_MODELS,
    EXPECTED_DEPENDENT_AE2_RESOURCE_MANIFEST_SHA256,
    EXPECTED_RESOURCE_MANIFEST_SHA256,
    EXPECTED_TEXTURE_MANIFEST_SHA256,
    MODEL_RESOURCES,
    expected_resource_paths,
    parse_resource_manifest,
    profile,
    texture_keys,
)


class ExtendedAeContractTest(unittest.TestCase):
    def test_exact_resource_partition_is_closed_and_disjoint(self):
        resources = expected_resource_paths()

        self.assertEqual(15, len(resources))
        self.assertEqual(15, len(set(resources)))
        self.assertEqual(sorted(resources), resources)
        self.assertTrue(
            all(path.startswith("assets/extendedae/") for path in resources)
        )
        self.assertEqual(6, len(MODEL_RESOURCES))
        self.assertIn("assets/extendedae/models/item/ex_drive.json", resources)
        self.assertEqual(8, len(texture_keys()))
        self.assertEqual(8, len(set(texture_keys())))

    def test_closed_cell_boundary_is_three_builtins_plus_ae2_reference(self):
        builtins = dict(BUILT_IN_CELL_MODELS)

        self.assertEqual(3, len(builtins))
        self.assertEqual(
            "extendedae:block/drive/infinity_water_cell",
            builtins["extendedae:infinity_water_cell"],
        )
        self.assertNotIn("kubejs:water_cell", builtins)
        self.assertTrue(all(item.startswith("extendedae:") for item in builtins))

    def test_profile_binds_status_layout_and_resource_partitions(self):
        manifest = b"assets/example\t1\t" + b"0" * 64 + b"\n"
        value = profile(manifest)

        self.assertEqual(1, value["schemaVersion"])
        self.assertEqual("extendedae", value["profileId"])
        self.assertEqual("M3b", value["coverageMilestone"])
        self.assertEqual("technical-pending", value["runtimeAcceptance"])
        self.assertEqual("pending", value["humanAcceptance"])
        drive = value["supportedDrive"]
        self.assertEqual("extendedae:ex_drive", drive["blockId"])
        self.assertEqual(20, drive["slotCount"])
        self.assertEqual(10, drive["frontSlotCount"])
        self.assertEqual(10, drive["backSlotCount"])
        self.assertEqual(dict(BUILT_IN_CELL_MODELS), drive["builtInCellModels"])
        self.assertEqual(26, drive["supportedItemCount"])
        self.assertEqual("position-color-no-texture", drive["ledSource"])
        self.assertEqual("ae2:block/drive/drive_front", drive["ledMaterialProxy"])
        self.assertEqual("static-offline-unknown", drive["ledPolicy"])
        self.assertEqual(
            EXPECTED_TEXTURE_MANIFEST_SHA256,
            value["textureManifestSha256"],
        )
        self.assertEqual(
            EXPECTED_RESOURCE_MANIFEST_SHA256,
            value["resourcePartitions"]["extendedaeExact"]["manifestSha256"],
        )
        self.assertEqual(
            EXPECTED_DEPENDENT_AE2_RESOURCE_MANIFEST_SHA256,
            value["resourcePartitions"]["dependentAe2M3aDrive"][
                "manifestSha256"
            ],
        )
        self.assertEqual(
            hashlib.sha256(manifest).hexdigest(),
            value["requiredResourcesManifestSha256"],
        )

    def test_committed_generated_outputs_match_contract(self):
        root = ROOT / (
            "src/main/resources/bluemap-ae2/profiles/extendedae/"
            "1.21-2.2.33-neoforge"
        )
        manifest = (root / "required-resources.tsv").read_bytes()
        actual_profile = json.loads(
            (root / "profile.json").read_text(encoding="utf-8")
        )
        resources = parse_resource_manifest(manifest)

        self.assertEqual(expected_resource_paths(), list(resources))
        self.assertEqual(
            EXPECTED_RESOURCE_MANIFEST_SHA256,
            hashlib.sha256(manifest).hexdigest(),
        )
        self.assertEqual(profile(manifest), actual_profile)

    def test_manifest_parser_rejects_bad_rows_duplicates_and_order(self):
        digest_a = "a" * 64
        digest_b = "b" * 64
        valid = f"a\t1\t{digest_a}\nb\t2\t{digest_b}\n".encode()
        self.assertEqual(
            {"a": (1, digest_a), "b": (2, digest_b)},
            parse_resource_manifest(valid),
        )
        with self.assertRaisesRegex(ValueError, "not sorted"):
            parse_resource_manifest(
                f"b\t1\t{digest_a}\na\t2\t{digest_b}\n".encode()
            )
        with self.assertRaisesRegex(ValueError, "duplicate"):
            parse_resource_manifest(
                f"a\t1\t{digest_a}\na\t2\t{digest_b}\n".encode()
            )
        for malformed in (
            b"not-a-row\n",
            f"a\t0\t{digest_a}\n".encode(),
            f"a\t01\t{digest_a}\n".encode(),
            f"a\t1\t{'g' * 64}\n".encode(),
        ):
            with self.assertRaisesRegex(ValueError, "malformed"):
                parse_resource_manifest(malformed)


if __name__ == "__main__":
    unittest.main()
