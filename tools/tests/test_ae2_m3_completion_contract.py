# SPDX-License-Identifier: LGPL-3.0-only

import hashlib
import json
import sys
import unittest
from pathlib import Path


ROOT = Path(__file__).resolve().parents[2]
sys.path.insert(0, str(ROOT / "tools"))

from ae2_m3_completion_contract import (  # noqa: E402
    BLOCK_ENTITY_IDS,
    EMITTED_STATIC_TEXTURES,
    EXPECTED_EMITTED_STATIC_TEXTURE_MANIFEST_SHA256,
    EXPECTED_FALLBACK_TEXTURE_MANIFEST_SHA256,
    EXPECTED_FROZEN_OUTPUT_SHA256,
    EXPECTED_PROFILE_SHA256,
    EXPECTED_RESOURCE_MANIFEST_SHA256,
    EXPECTED_RESOURCES,
    EXPECTED_SOURCE_TEXTURE_MANIFEST_SHA256,
    EXPECTED_SOURCES_SHA256,
    FALLBACK_ONLY_TEXTURES,
    PROFILE_ID,
    SOURCE_COMMIT,
    SOURCE_TEXTURES,
    SPATIAL_PYLON_AMBIGUOUS_POLICY,
    SPATIAL_PYLON_COMPONENT_MAX_BLOCKS,
    SPATIAL_PYLON_INCOMPLETE_COMPONENT_POLICY,
    SUPPORTED_BLOCKS,
    SYNTHETIC_BLOCK_STATES,
    expected_manifest,
    expected_resource_paths,
    parse_resource_manifest,
    profile,
    profile_bytes,
    texture_manifest,
)


class Ae2M3CompletionContractTest(unittest.TestCase):
    def test_public_identity_and_closed_resource_partition(self):
        self.assertEqual("ae2-m3-completion", PROFILE_ID)
        self.assertEqual(5, len(set(SYNTHETIC_BLOCK_STATES.values())))
        self.assertEqual(5, len(set(BLOCK_ENTITY_IDS.values())))
        self.assertEqual("79ee2c704ad62941a426c26b1cb1f76ef5b2ee5a", SOURCE_COMMIT)
        self.assertEqual(
            "d2f451203cb61c2d21fae52c683083d2f72441ca7d26725f4df5934290492e6a",
            EXPECTED_SOURCES_SHA256,
        )
        self.assertEqual(6, len(SUPPORTED_BLOCKS))
        self.assertEqual(33, len(EXPECTED_RESOURCES))
        self.assertEqual(expected_resource_paths(), sorted(expected_resource_paths()))
        self.assertEqual(22_491, sum(size for _, size, _ in EXPECTED_RESOURCES))

    def test_exact_partition_and_profile_hashes_are_locked(self):
        manifest = expected_manifest()
        self.assertEqual(
            EXPECTED_RESOURCE_MANIFEST_SHA256,
            hashlib.sha256(manifest).hexdigest(),
        )
        for textures, expected in (
            (SOURCE_TEXTURES, EXPECTED_SOURCE_TEXTURE_MANIFEST_SHA256),
            (
                EMITTED_STATIC_TEXTURES,
                EXPECTED_EMITTED_STATIC_TEXTURE_MANIFEST_SHA256,
            ),
            (FALLBACK_ONLY_TEXTURES, EXPECTED_FALLBACK_TEXTURE_MANIFEST_SHA256),
        ):
            self.assertEqual(
                expected,
                hashlib.sha256(texture_manifest(manifest, textures)).hexdigest(),
            )
        self.assertEqual(
            EXPECTED_PROFILE_SHA256,
            hashlib.sha256(profile_bytes(manifest)).hexdigest(),
        )

    def test_static_texture_and_fallback_boundaries_are_exact(self):
        self.assertEqual(17, len(SOURCE_TEXTURES))
        self.assertEqual(17, len(set(SOURCE_TEXTURES)))
        self.assertEqual(15, len(EMITTED_STATIC_TEXTURES))
        self.assertEqual(15, len(set(EMITTED_STATIC_TEXTURES)))
        self.assertLess(set(EMITTED_STATIC_TEXTURES), set(SOURCE_TEXTURES))
        self.assertEqual(2, len(FALLBACK_ONLY_TEXTURES))
        self.assertTrue(set(FALLBACK_ONLY_TEXTURES).isdisjoint(SOURCE_TEXTURES))
        self.assertNotIn(
            "ae2:block/spatial_pylon/dim_end", EMITTED_STATIC_TEXTURES
        )
        self.assertNotIn(
            "ae2:block/spatial_pylon/dim_spanned", EMITTED_STATIC_TEXTURES
        )

    def test_profile_binds_persistence_static_projection_and_limitations(self):
        value = profile(expected_manifest())
        self.assertEqual("M3f", value["coverageMilestone"])
        self.assertEqual("local-build-candidate", value["buildAcceptance"])
        self.assertEqual(
            "technical-lifecycle-pending", value["runtimeAcceptance"]
        )
        self.assertEqual("pending", value["humanAcceptance"])
        self.assertEqual(
            "1-through-21",
            value["persistedState"]["paint"]["blockEntity"]["acceptedCount"],
        )
        self.assertEqual(
            {
                "facing": "required-six-way-direction",
                "light_level": "required-integer-0-through-2",
            },
            value["persistedState"]["paint"]["blockProperties"],
        )
        self.assertEqual(
            [],
            value["persistedState"]["spatialPylon"]["blockEntity"]
            ["ordinarySavedRenderFields"],
        )
        pylon = value["topologyPolicy"]["spatialPylon"]
        self.assertEqual("uncached-native-axis-line-scan", pylon["evidence"])
        self.assertEqual(
            SPATIAL_PYLON_COMPONENT_MAX_BLOCKS,
            pylon["maximumAxisLinePylons"],
        )
        self.assertEqual(
            SPATIAL_PYLON_AMBIGUOUS_POLICY,
            pylon["boundedLocallyInvalidComponent"],
        )
        self.assertEqual(
            SPATIAL_PYLON_INCOMPLETE_COMPONENT_POLICY,
            pylon["missingMalformedOrCapped"],
        )
        self.assertEqual(
            "not-claimed-beyond-bounded-local-component",
            pylon["globalClusterValidity"],
        )
        self.assertEqual(
            "not-claimed-static-offline-unknown", pylon["onlineAndPower"]
        )
        self.assertEqual(
            "native-block-id-only-no-connector-claim",
            pylon["extensionInteraction"],
        )
        self.assertEqual(42, value["renderPolicy"]["paint"]["maximumTriangles"])
        self.assertEqual(24, value["renderPolicy"]["spatialPylon"]["triangles"])
        self.assertEqual(
            36, value["renderPolicy"]["skyStoneChest"]["trianglesPerChest"]
        )
        self.assertEqual(34, value["renderPolicy"]["crank"]["triangles"])
        self.assertEqual(78, value["renderPolicy"]["inscriber"]["maximumTriangles"])
        self.assertEqual(0, value["resourcePartition"]["acceptedProfileOverlapCount"])

    def test_committed_generated_outputs_and_frozen_routes_match(self):
        route_root = ROOT / (
            "src/main/resources/bluemap-ae2/profiles/ae2/19.2.17/"
            "routes/m3-completion"
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

    def test_full_build_run_guard_is_persistent_and_exact(self):
        function_root = ROOT / "gallery/datapack/data/ae2_m3/function"
        build = (function_root / "build.mcfunction").read_text("utf-8")
        verify = (function_root / "verify.mcfunction").read_text("utf-8")

        increment = "scoreboard players add #m3f_builds ae2m3run 1"
        self.assertEqual(1, build.count(increment))
        self.assertIn("scoreboard objectives add ae2m3run dummy", build)
        self.assertNotIn("scoreboard players set #m3f_builds ae2m3run", build)
        self.assertNotIn("scoreboard players add #builds ae2m3run", build)

        # Applying the generated full-build command stream once creates score
        # one; applying it a second time produces two, which verify rejects.
        build_increments = build.count(increment)
        first_build_score = build_increments
        second_build_score = first_build_score + build_increments
        self.assertEqual(1, first_build_score)
        self.assertEqual(2, second_build_score)
        self.assertIn(
            "execute unless score #m3f_builds ae2m3run matches 1 run scoreboard "
            "players add #failures ae2m3v 1",
            verify,
        )
        self.assertNotIn("score #builds ae2m3run", verify)

        for name in ("clear", "load", "release", "settle_check"):
            lifecycle = (function_root / f"{name}.mcfunction").read_text("utf-8")
            self.assertNotIn("#m3f_builds", lifecycle, name)
            self.assertNotIn("ae2m3run", lifecycle, name)

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
