# SPDX-License-Identifier: LGPL-3.0-only

import struct
import sys
import unittest
from pathlib import Path


ROOT = Path(__file__).resolve().parents[2]
sys.path.insert(0, str(ROOT / "tools"))

from verify_crafting_artifact import (  # noqa: E402
    ADVANCED_BASE_SHA256,
    EXPECTED_AE2_CLASS_SHA256,
    EXPANDED_DEFINITION_SHA256,
    MEGA_DEFINITION_SHA256,
    verify_hash,
    verify_png_header,
)


class VerifyCraftingArtifactTest(unittest.TestCase):
    def test_exact_audited_class_contracts_are_closed(self):
        self.assertEqual(14, len(EXPECTED_AE2_CLASS_SHA256))
        self.assertIn(
            "appeng/client/render/crafting/CraftingCubeBakedModel.class",
            EXPECTED_AE2_CLASS_SHA256,
        )
        self.assertIn(
            "appeng/blockentity/crafting/CraftingBlockEntity.class",
            EXPECTED_AE2_CLASS_SHA256,
        )
        self.assertTrue(
            all(len(value) == 64 for value in EXPECTED_AE2_CLASS_SHA256.values())
        )
        self.assertEqual(64, len(MEGA_DEFINITION_SHA256))
        self.assertEqual(64, len(EXPANDED_DEFINITION_SHA256))
        self.assertEqual(64, len(ADVANCED_BASE_SHA256))

    def test_png_header_requires_exact_rgba8_dimensions(self):
        signature = b"\x89PNG\r\n\x1a\n"
        header = struct.pack(">I", 13) + b"IHDR"
        exact = signature + header + struct.pack(">IIBBBBB", 16, 16, 8, 6, 0, 0, 0)
        exact += b"\x00\x00\x00\x00"
        verify_png_header("texture.png", exact)
        wrong = signature + header + struct.pack(">IIBBBBB", 16, 32, 8, 6, 0, 0, 0)
        wrong += b"\x00\x00\x00\x00"
        with self.assertRaisesRegex(ValueError, "16x16 RGBA8"):
            verify_png_header("texture.png", wrong)

    def test_hash_failure_reports_label_and_both_values(self):
        with self.assertRaisesRegex(ValueError, "route SHA-256: got a, expected b"):
            verify_hash("route SHA-256", "a", "b")


if __name__ == "__main__":
    unittest.main()
