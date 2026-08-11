# SPDX-License-Identifier: LGPL-3.0-only

import hashlib
import json
import re
import sys
import unittest
from pathlib import Path


ROOT = Path(__file__).resolve().parents[2]
sys.path.insert(0, str(ROOT / "tools"))

from ae2_crafting_contract import (  # noqa: E402
    EXPANDED_AE_COMPATIBLE_CONNECTORS,
    EXPECTED_FROZEN_OUTPUT_SHA256,
    EXPECTED_PROFILE_SHA256,
    EXPECTED_RESOURCE_MANIFEST_SHA256,
    EXPECTED_RESOURCES,
    EXPECTED_TEXTURE_MANIFEST_SHA256,
    MEGA_CELLS_COMPATIBLE_CONNECTORS,
    PROFILE_ID,
    SUPPORTED_BLOCKS,
    SYNTHETIC_BLOCK_STATE,
    UNSUPPORTED_COMPATIBLE_CONNECTORS,
    expected_manifest,
    expected_resource_paths,
    parse_resource_manifest,
    profile,
    profile_bytes,
    texture_keys,
    texture_manifest,
)


class Ae2CraftingContractTest(unittest.TestCase):
    def test_public_identity_and_closed_resource_partition(self):
        self.assertEqual("ae2-crafting", PROFILE_ID)
        self.assertEqual("bluemap_ae2:crafting", SYNTHETIC_BLOCK_STATE)
        self.assertEqual(8, len(SUPPORTED_BLOCKS))
        self.assertEqual(8, len({block_id for block_id, _ in SUPPORTED_BLOCKS}))
        self.assertEqual(30, len(EXPECTED_RESOURCES))
        self.assertEqual(expected_resource_paths(), sorted(expected_resource_paths()))
        self.assertEqual(15, len(texture_keys()))
        self.assertEqual(15, len(set(texture_keys())))
        self.assertEqual(6_177, sum(size for _, size, _ in EXPECTED_RESOURCES))

    def test_exact_partition_and_profile_hashes_are_locked(self):
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
            EXPECTED_PROFILE_SHA256,
            hashlib.sha256(profile_bytes(manifest)).hexdigest(),
        )

    def test_extension_connector_boundary_is_exact_and_not_rendered(self):
        self.assertEqual(8, len(MEGA_CELLS_COMPATIBLE_CONNECTORS))
        self.assertEqual(21, len(EXPANDED_AE_COMPATIBLE_CONNECTORS))
        self.assertEqual(29, len(UNSUPPORTED_COMPATIBLE_CONNECTORS))
        self.assertEqual(29, len(set(UNSUPPORTED_COMPATIBLE_CONNECTORS)))
        self.assertFalse(
            {block_id for block_id, _ in SUPPORTED_BLOCKS}
            & set(UNSUPPORTED_COMPATIBLE_CONNECTORS)
        )
        self.assertFalse(
            any(
                connector.startswith(("advanced_ae:", "extendedae:"))
                for connector in UNSUPPORTED_COMPATIBLE_CONNECTORS
            )
        )

    def test_profile_binds_formed_monitor_and_fallback_boundaries(self):
        value = profile(expected_manifest())
        self.assertEqual("M3d", value["coverageMilestone"])
        self.assertEqual("local-build-candidate", value["buildAcceptance"])
        self.assertEqual(
            "technical-lifecycle-pending", value["runtimeAcceptance"]
        )
        self.assertEqual("pending", value["humanAcceptance"])
        self.assertEqual("required-true", value["persistedState"]["formed"])
        self.assertEqual(
            {"fullSolid": True, "occluding": True},
            value["renderPolicy"]["blockProperties"]["formed"],
        )
        self.assertEqual(
            {"fullSolid": True, "occluding": True},
            value["renderPolicy"]["blockProperties"]["unformed"],
        )
        self.assertEqual(
            "client-stream-only-omitted",
            value["persistedState"]["monitorBlockEntity"]["displayGenericStack"],
        )
        self.assertEqual(
            29,
            len(
                value["connectionPolicy"][
                    "compatibleButUnsupportedConnectorBlocks"
                ]
            ),
        )
        self.assertEqual(
            "atomic-original-resource-fallback",
            value["connectionPolicy"]["compatibleButUnsupportedPolicy"],
        )
        self.assertEqual(7, value["resourcePartition"]["formedModelCount"])
        self.assertEqual(
            "client-built-in-absent",
            value["resourcePartition"]["monitorFormedModel"],
        )

    def test_committed_generated_outputs_and_frozen_routes_match(self):
        route_root = ROOT / (
            "src/main/resources/bluemap-ae2/profiles/ae2/19.2.17/"
            "routes/crafting"
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

    def test_preprobe_is_bounded_stable_and_never_invokes_full_build(self):
        function_root = ROOT / "gallery/datapack/data/ae2_m3/function"
        preprobe = (function_root / "preprobe.mcfunction").read_text("utf-8")
        build = (function_root / "build.mcfunction").read_text("utf-8")
        check = (function_root / "preprobe_check.mcfunction").read_text("utf-8")
        settle = (function_root / "settle_check.mcfunction").read_text("utf-8")
        verify = (function_root / "verify.mcfunction").read_text("utf-8")
        combined = preprobe + "\n" + check

        powered_crafting_snbt = (
            '{hasRedstone:2,cable:{id:"ae2:fluix_glass_cable"}}'
        )
        powered_crafting_link = (
            "data merge block 297 99 269 " + powered_crafting_snbt
        )
        self.assertIn(powered_crafting_link, preprobe)
        self.assertIn(powered_crafting_link, build)
        for contents, objective in (
            (check, "ae2m3p"),
            (settle, "ae2m3s"),
            (verify, "ae2m3v"),
        ):
            self.assertIn(
                "execute unless data block 297 99 269 " + powered_crafting_snbt
                + " run scoreboard players add #failures " + objective + " 1",
                contents,
            )
        self.assertNotIn(
            "data merge block 297 99 269 {hasRedstone:2}\n",
            "\n".join((preprobe, build, check, settle, verify)),
        )

        self.assertNotIn("function ae2_m3:build", combined)
        self.assertNotIn("ae2m3run", combined)
        self.assertNotIn("#builds", combined)
        self.assertNotRegex(
            combined,
            r"data (?:merge|modify) block .*\b(?:formed|powered)\b",
        )
        self.assertNotRegex(check, r'\{id:"(?:megacells|expandedae):')
        self.assertIn("scoreboard players set #result ae2m3p 1", check)
        self.assertIn("scoreboard players set #result ae2m3p -1", check)
        self.assertIn("scoreboard players add #stable ae2m3p 1", check)
        self.assertIn("scoreboard players set #stable ae2m3p 0", check)
        self.assertIn("score #stable ae2m3p matches 2..", check)

        command_coordinate = re.compile(
            r"(?:fill|setblock|data merge block|"
            r"execute (?:if|unless) (?:data )?block) "
            r"(-?\d+) (-?\d+) (-?\d+)"
        )
        positions = [
            tuple(map(int, match.groups()))
            for match in command_coordinate.finditer(combined)
        ]
        self.assertTrue(positions)
        for x, y, z in positions:
            self.assertIn(x, range(296, 320))
            self.assertIn(y, range(97, 106))
            self.assertIn(z, range(260, 300))

        for x1, z1, x2, z2 in re.findall(
            r"forceload add (-?\d+) (-?\d+) (-?\d+) (-?\d+)",
            preprobe,
        ):
            self.assertIn(int(x1), range(296, 320))
            self.assertIn(int(x2), range(296, 320))
            self.assertIn(int(z1), range(260, 300))
            self.assertIn(int(z2), range(260, 300))

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
