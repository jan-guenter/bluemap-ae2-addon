# SPDX-License-Identifier: LGPL-3.0-only

import hashlib
import json
import sys
import unittest
from pathlib import Path


ROOT = Path(__file__).resolve().parents[2]
sys.path.insert(0, str(ROOT / "tools"))

from ae2_quartz_glass_contract import (  # noqa: E402
    BLOCKS,
    EXPECTED_CANONICAL_RESOURCE_PARTITION_SHA256,
    EXPECTED_CANONICAL_TEXTURE_PARTITION_SHA256,
    EXPECTED_RESOURCE_MANIFEST_SHA256,
    EXPECTED_RESOURCES,
    EXPECTED_TEXTURE_MANIFEST_SHA256,
    PROFILE_ID,
    SYNTHETIC_BLOCK_STATE,
    canonical_partition_manifest,
    expected_manifest,
    expected_resource_paths,
    parse_resource_manifest,
    profile,
    profile_bytes,
    texture_keys,
    texture_manifest,
)


class Ae2QuartzGlassContractTest(unittest.TestCase):
    def test_public_identity_and_closed_resource_partition(self):
        self.assertEqual("ae2-quartz-glass", PROFILE_ID)
        self.assertEqual(
            ("ae2:quartz_glass", "ae2:quartz_vibrant_glass"), BLOCKS
        )
        self.assertEqual("bluemap_ae2:quartz_glass", SYNTHETIC_BLOCK_STATE)
        self.assertEqual(22, len(EXPECTED_RESOURCES))
        self.assertEqual(expected_resource_paths(), sorted(expected_resource_paths()))
        self.assertEqual(19, len(texture_keys()))
        self.assertEqual(19, len(set(texture_keys())))
        self.assertEqual(4_187, sum(size for _, size, _ in EXPECTED_RESOURCES))

    def test_all_exact_partition_hashes_are_locked(self):
        manifest = expected_manifest()
        self.assertEqual(
            EXPECTED_RESOURCE_MANIFEST_SHA256,
            hashlib.sha256(manifest).hexdigest(),
        )
        self.assertEqual(
            EXPECTED_TEXTURE_MANIFEST_SHA256,
            hashlib.sha256(texture_manifest(manifest)).hexdigest(),
        )
        self.assertEqual(
            EXPECTED_CANONICAL_RESOURCE_PARTITION_SHA256,
            hashlib.sha256(canonical_partition_manifest(manifest)).hexdigest(),
        )
        self.assertEqual(
            EXPECTED_CANONICAL_TEXTURE_PARTITION_SHA256,
            hashlib.sha256(
                canonical_partition_manifest(manifest, textures_only=True)
            ).hexdigest(),
        )

    def test_profile_binds_exact_render_and_fallback_boundaries(self):
        value = profile(expected_manifest())
        self.assertEqual(1, value["schemaVersion"])
        self.assertEqual("M3c", value["coverageMilestone"])
        self.assertEqual("not-yet-runtime-validated", value["runtimeAcceptance"])
        self.assertEqual("pending", value["humanAcceptance"])
        self.assertEqual(6, value["connectionPolicy"]["directNeighborAxes"])
        self.assertTrue(
            value["connectionPolicy"]["mutualAppearanceRequiredByClient"]
        )
        self.assertEqual(
            "unsupported-treated-disconnected",
            value["connectionPolicy"]["crossModAppearance"],
        )
        self.assertEqual(
            "atomic-original-resource-fallback",
            value["connectionPolicy"]["malformedNativeOrMissingNeighbor"],
        )
        self.assertEqual("cutout", value["renderPolicy"]["layer"])
        self.assertFalse(value["renderPolicy"]["ambientOcclusion"])
        self.assertEqual(15, value["renderPolicy"]["frameTextureCount"])

    def test_committed_generated_outputs_match_contract(self):
        root = ROOT / (
            "src/main/resources/bluemap-ae2/profiles/ae2/19.2.17/"
            "routes/quartz-glass"
        )
        manifest = (root / "required-resources.tsv").read_bytes()
        actual_profile = json.loads((root / "profile.json").read_text("utf-8"))
        self.assertEqual(expected_manifest(), manifest)
        self.assertEqual(profile(manifest), actual_profile)
        self.assertEqual(profile_bytes(manifest), (root / "profile.json").read_bytes())

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
