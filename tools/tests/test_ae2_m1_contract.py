# SPDX-License-Identifier: LGPL-3.0-only

import hashlib
import json
import sys
import unittest
from pathlib import Path


ROOT = Path(__file__).resolve().parents[2]
sys.path.insert(0, str(ROOT / "tools"))

from ae2_m1_contract import (  # noqa: E402
    DRIVE_EXPLICIT_CELL_MODELS,
    DRIVE_GENERIC_CELL_IDS,
    DRIVE_GENERIC_CELL_MODEL,
    DRIVE_MODEL_RESOURCES,
    EXPECTED_CORE_RESOURCE_MANIFEST_SHA256,
    EXPECTED_CORE_TEXTURE_MANIFEST_SHA256,
    EXPECTED_DRIVE_RESOURCE_MANIFEST_SHA256,
    EXPECTED_TEXTURE_MANIFEST_SHA256,
    cable_ids,
    core_expected_resource_paths,
    core_texture_keys,
    drive_cell_models,
    drive_resource_paths,
    drive_texture_keys,
    expected_resource_paths,
    parse_resource_manifest,
    profile,
    TERMINAL_MODEL_RESOURCES,
    TERMINAL_TEXTURES,
    texture_keys,
)


class Ae2M1ContractTest(unittest.TestCase):
    def test_catalog_is_the_exact_closed_cartesian_product(self):
        ids = cable_ids()

        self.assertEqual(85, len(ids))
        self.assertEqual(85, len(set(ids)))
        self.assertEqual("ae2:white_glass_cable", ids[0])
        self.assertEqual("ae2:fluix_smart_dense_cable", ids[-1])
        self.assertIn("ae2:light_blue_covered_dense_cable", ids)

    def test_texture_and_resource_closures_are_exact_and_unique(self):
        textures = texture_keys()
        resources = expected_resource_paths()

        self.assertEqual(158, len(textures))
        self.assertEqual(158, len(set(textures)))
        self.assertEqual(196, len(resources))
        self.assertEqual(196, len(set(resources)))
        self.assertEqual(sorted(resources), resources)
        self.assertIn(
            "assets/ae2/textures/part/cable/core/glass/transparent.png",
            resources,
        )
        self.assertIn(
            "assets/ae2/textures/part/cable/dense_smart/channels_10.png",
            resources,
        )
        self.assertEqual(
            {
                "assets/ae2/models/part/display_base.json",
                "assets/ae2/models/part/display_off.json",
                "assets/ae2/models/part/display_status_off.json",
                "assets/ae2/models/part/terminal_off.json",
            },
            set(TERMINAL_MODEL_RESOURCES),
        )
        self.assertEqual(8, len(TERMINAL_TEXTURES))
        self.assertTrue(set(TERMINAL_TEXTURES).issubset(core_texture_keys()))
        self.assertIn(
            "assets/ae2/textures/part/terminal_medium.png",
            resources,
        )
        self.assertEqual(148, len(core_texture_keys()))
        self.assertEqual(170, len(core_expected_resource_paths()))
        self.assertEqual(10, len(drive_texture_keys()))
        self.assertEqual(26, len(drive_resource_paths()))
        self.assertTrue(
            set(core_expected_resource_paths()).isdisjoint(drive_resource_paths())
        )
        self.assertEqual(15, len(DRIVE_MODEL_RESOURCES))
        self.assertIn("assets/ae2/blockstates/drive.json", drive_resource_paths())
        self.assertIn(
            "assets/ae2/models/block/drive/cells/creative_cell.json",
            drive_resource_paths(),
        )
        self.assertIn(
            "assets/ae2/textures/block/generics/front.png",
            drive_resource_paths(),
        )

    def test_drive_catalog_is_exact_and_separates_generic_items(self):
        explicit = dict(DRIVE_EXPLICIT_CELL_MODELS)
        models = drive_cell_models()

        self.assertEqual(21, len(explicit))
        self.assertEqual(2, len(DRIVE_GENERIC_CELL_IDS))
        self.assertEqual(23, len(models))
        self.assertEqual(12, len(set(models.values())))
        self.assertEqual(
            "ae2:block/drive/cells/1k_item_cell",
            explicit["ae2:item_storage_cell_1k"],
        )
        self.assertEqual(
            explicit["ae2:item_storage_cell_1k"],
            explicit["ae2:portable_item_cell_1k"],
        )
        self.assertEqual(
            "ae2:block/drive/cells/creative_cell",
            explicit["ae2:creative_storage_cell"],
        )
        self.assertTrue(
            all(models[item_id] == DRIVE_GENERIC_CELL_MODEL
                for item_id in DRIVE_GENERIC_CELL_IDS)
        )

    def test_profile_binds_the_manifest_and_texture_aggregate(self):
        manifest = b"0" * 64 + b"  assets/example\n"
        value = profile(manifest)

        self.assertEqual(3, value["schemaVersion"])
        self.assertEqual("M3a", value["coverageMilestone"])
        self.assertEqual(cable_ids(), value["supportedCenterParts"])
        self.assertEqual(
            [{"id": "ae2:terminal", "spins": [0, 1, 2, 3]}],
            value["supportedFaceParts"],
        )
        self.assertEqual(
            {
                "blockState": {"Name": "minecraft:stone"},
                "properties": "forbidden",
                "maximumFacades": 1,
                "requiredSameFacePart": "ae2:terminal",
            },
            value["facadePolicy"],
        )
        drive = value["supportedDrive"]
        self.assertEqual("ae2:drive", drive["blockId"])
        self.assertEqual(10, drive["slotCount"])
        self.assertEqual(dict(DRIVE_EXPLICIT_CELL_MODELS), drive["explicitCellModels"])
        self.assertEqual(
            {
                "model": DRIVE_GENERIC_CELL_MODEL,
                "itemIds": list(DRIVE_GENERIC_CELL_IDS),
            },
            drive["genericCellModel"],
        )
        self.assertEqual(12, drive["occupiedModelCount"])
        self.assertEqual("static-offline-unknown", drive["ledPolicy"])
        self.assertEqual(
            "atomic-whole-block-original-resource-fallback",
            drive["unknownCellPolicy"],
        )
        self.assertEqual(core_texture_keys(), value["coreTextures"])
        self.assertEqual(drive_texture_keys(), value["driveTextures"])
        self.assertEqual(texture_keys(), value["textures"])
        self.assertEqual(
            EXPECTED_CORE_TEXTURE_MANIFEST_SHA256,
            value["coreTextureManifestSha256"],
        )
        self.assertEqual(
            EXPECTED_TEXTURE_MANIFEST_SHA256,
            value["textureManifestSha256"],
        )
        self.assertEqual(
            {
                "coreM0ThroughM2": {
                    "pathCount": 170,
                    "textureCount": 148,
                    "manifestSha256": EXPECTED_CORE_RESOURCE_MANIFEST_SHA256,
                },
                "m3aDrive": {
                    "pathCount": 26,
                    "textureCount": 10,
                    "manifestSha256": EXPECTED_DRIVE_RESOURCE_MANIFEST_SHA256,
                },
            },
            value["resourcePartitions"],
        )
        self.assertEqual(
            hashlib.sha256(manifest).hexdigest(),
            value["requiredResourcesSha256"],
        )

    def test_committed_generated_outputs_match_the_contract(self):
        profile_root = (
            ROOT / "src/main/resources/bluemap-ae2/profiles/ae2/19.2.17"
        )
        manifest = (profile_root / "required-resources.sha256").read_bytes()
        resources = parse_resource_manifest(manifest)
        actual_profile = json.loads(
            (profile_root / "profile.json").read_text(encoding="utf-8")
        )

        self.assertEqual(expected_resource_paths(), list(resources))
        self.assertEqual(profile(manifest), actual_profile)
        core_manifest = b"".join(
            f"{resources[path]}  {path}\n".encode("utf-8")
            for path in core_expected_resource_paths()
        )
        drive_manifest = b"".join(
            f"{resources[path]}  {path}\n".encode("utf-8")
            for path in drive_resource_paths()
        )
        self.assertEqual(
            EXPECTED_CORE_RESOURCE_MANIFEST_SHA256,
            hashlib.sha256(core_manifest).hexdigest(),
        )
        self.assertEqual(
            EXPECTED_DRIVE_RESOURCE_MANIFEST_SHA256,
            hashlib.sha256(drive_manifest).hexdigest(),
        )

    def test_resource_manifest_parser_rejects_unsorted_duplicate_and_bad_rows(self):
        digest_a = b"a" * 64
        digest_b = b"b" * 64
        valid = digest_a + b"  a\n" + digest_b + b"  b\n"
        self.assertEqual({"a": "a" * 64, "b": "b" * 64}, parse_resource_manifest(valid))

        with self.assertRaisesRegex(ValueError, "not sorted"):
            parse_resource_manifest(digest_a + b"  b\n" + digest_b + b"  a\n")
        with self.assertRaisesRegex(ValueError, "duplicate"):
            parse_resource_manifest(digest_a + b"  a\n" + digest_b + b"  a\n")
        with self.assertRaisesRegex(ValueError, "malformed"):
            parse_resource_manifest(b"not-a-digest  a\n")


if __name__ == "__main__":
    unittest.main()
