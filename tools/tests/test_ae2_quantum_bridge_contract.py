# SPDX-License-Identifier: LGPL-3.0-only

import hashlib
import json
import sys
import unittest
from pathlib import Path


ROOT = Path(__file__).resolve().parents[2]
sys.path.insert(0, str(ROOT / "tools"))

from ae2_quantum_bridge_contract import (  # noqa: E402
    BLOCK_ENTITY_ID,
    EMITTED_OFF_TEXTURES,
    EXPECTED_EMITTED_OFF_TEXTURE_MANIFEST_SHA256,
    EXPECTED_FROZEN_OUTPUT_SHA256,
    EXPECTED_PROFILE_SHA256,
    EXPECTED_RESOURCE_MANIFEST_SHA256,
    EXPECTED_RESOURCES,
    EXPECTED_SOURCE_TEXTURE_MANIFEST_SHA256,
    EXPECTED_SOURCES_SHA256,
    PROFILE_ID,
    SOURCE_COMMIT,
    SOURCE_TEXTURES,
    SUPPORTED_BLOCKS,
    SYNTHETIC_BLOCK_STATE,
    emitted_off_texture_keys,
    expected_manifest,
    expected_resource_paths,
    parse_resource_manifest,
    profile,
    profile_bytes,
    source_texture_keys,
    texture_manifest,
)


class Ae2QuantumBridgeContractTest(unittest.TestCase):
    def test_public_identity_and_closed_resource_partition(self):
        self.assertEqual("ae2-quantum-bridge", PROFILE_ID)
        self.assertEqual("bluemap_ae2:quantum_bridge", SYNTHETIC_BLOCK_STATE)
        self.assertEqual("ae2:quantum_ring", BLOCK_ENTITY_ID)
        self.assertEqual("79ee2c704ad62941a426c26b1cb1f76ef5b2ee5a", SOURCE_COMMIT)
        self.assertEqual(
            "d2f451203cb61c2d21fae52c683083d2f72441ca7d26725f4df5934290492e6a",
            EXPECTED_SOURCES_SHA256,
        )
        self.assertEqual(2, len(SUPPORTED_BLOCKS))
        self.assertEqual(13, len(EXPECTED_RESOURCES))
        self.assertEqual(expected_resource_paths(), sorted(expected_resource_paths()))
        self.assertEqual(3_798, sum(size for _, size, _ in EXPECTED_RESOURCES))

    def test_exact_partition_and_profile_hashes_are_locked(self):
        manifest = expected_manifest()
        self.assertEqual(
            EXPECTED_RESOURCE_MANIFEST_SHA256,
            hashlib.sha256(manifest).hexdigest(),
        )
        self.assertEqual(
            EXPECTED_SOURCE_TEXTURE_MANIFEST_SHA256,
            hashlib.sha256(texture_manifest(manifest, SOURCE_TEXTURES)).hexdigest(),
        )
        self.assertEqual(
            EXPECTED_EMITTED_OFF_TEXTURE_MANIFEST_SHA256,
            hashlib.sha256(
                texture_manifest(manifest, EMITTED_OFF_TEXTURES)
            ).hexdigest(),
        )
        self.assertEqual(
            EXPECTED_PROFILE_SHA256,
            hashlib.sha256(profile_bytes(manifest)).hexdigest(),
        )

    def test_source_and_emitted_texture_boundaries_are_exact(self):
        self.assertEqual(6, len(source_texture_keys()))
        self.assertEqual(6, len(set(source_texture_keys())))
        self.assertEqual(4, len(emitted_off_texture_keys()))
        self.assertEqual(4, len(set(emitted_off_texture_keys())))
        self.assertLess(set(emitted_off_texture_keys()), set(source_texture_keys()))
        self.assertNotIn("ae2:block/quantum_ring_light", emitted_off_texture_keys())
        self.assertNotIn(
            "ae2:block/quantum_ring_light_corner", emitted_off_texture_keys()
        )

    def test_profile_binds_saved_transient_topology_and_fallback_boundaries(self):
        value = profile(expected_manifest())
        self.assertEqual("M3e", value["coverageMilestone"])
        self.assertEqual("local-build-candidate", value["buildAcceptance"])
        self.assertEqual(
            "technical-lifecycle-pending", value["runtimeAcceptance"]
        )
        self.assertEqual("pending", value["humanAcceptance"])
        self.assertEqual(
            {"formed": "required-true", "waterlogged": "required-boolean"},
            value["persistedState"]["blockProperties"],
        )
        self.assertEqual([], value["persistedState"]["blockEntity"]["renderFieldsRetained"])
        self.assertEqual(
            "unavailable-not-decoded",
            value["persistedState"]["transientClientStream"]["powered"],
        )
        self.assertEqual("isolated-3x3x1-plane", value["topologyPolicy"]["shape"])
        self.assertEqual(8, value["topologyPolicy"]["ring"]["count"])
        self.assertEqual(
            "native-block-ids-only-no-connector-claim",
            value["topologyPolicy"]["extensionInteraction"],
        )
        self.assertEqual("static-off-unknown", value["renderPolicy"]["power"])
        self.assertEqual("excluded", value["renderPolicy"]["particles"])
        self.assertEqual(396, value["renderPolicy"]["fullBridgeTriangles"])
        self.assertEqual(11, value["resourcePartition"]["routeOnlyPathCount"])
        self.assertEqual(2, value["resourcePartition"]["sharedMainResourceCount"])

    def test_committed_generated_outputs_and_frozen_routes_match(self):
        route_root = ROOT / (
            "src/main/resources/bluemap-ae2/profiles/ae2/19.2.17/"
            "routes/quantum-bridge"
        )
        manifest = (route_root / "required-resources.tsv").read_bytes()
        actual_profile = json.loads((route_root / "profile.json").read_text("utf-8"))
        self.assertEqual(expected_manifest(), manifest)
        self.assertEqual(profile(manifest), actual_profile)
        self.assertEqual(profile_bytes(manifest), (route_root / "profile.json").read_bytes())
        for relative_path, expected_digest in EXPECTED_FROZEN_OUTPUT_SHA256.items():
            self.assertEqual(
                expected_digest,
                hashlib.sha256((ROOT / relative_path).read_bytes()).hexdigest(),
                relative_path,
            )

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
