# SPDX-License-Identifier: LGPL-3.0-only

import json
import struct
import sys
import unittest
from pathlib import Path


ROOT = Path(__file__).resolve().parents[2]
sys.path.insert(0, str(ROOT / "tools"))

from verify_quartz_glass_artifact import (  # noqa: E402
    EXPECTED_CLASS_SHA256,
    verify_hash,
    verify_png_header,
    verify_route_json,
)


class VerifyQuartzGlassArtifactTest(unittest.TestCase):
    def test_exact_audited_class_contract_is_closed(self):
        self.assertEqual(11, len(EXPECTED_CLASS_SHA256))
        self.assertIn(
            "appeng/client/render/model/GlassBakedModel.class",
            EXPECTED_CLASS_SHA256,
        )
        self.assertIn(
            "appeng/decorative/solid/QuartzGlassBlock.class",
            EXPECTED_CLASS_SHA256,
        )
        self.assertTrue(
            all(len(value) == 64 for value in EXPECTED_CLASS_SHA256.values())
        )

    def test_route_json_accepts_only_exact_dynamic_model_dispatch(self):
        blockstate = json.dumps(
            {"variants": {"": {"model": "ae2:block/quartz_glass"}}}
        ).encode()
        verify_route_json(blockstate, b"{}")
        with self.assertRaisesRegex(ValueError, "routing changed"):
            verify_route_json(b'{"variants": {}}', b"{}")
        with self.assertRaisesRegex(ValueError, "stub changed"):
            verify_route_json(blockstate, b'{"parent": "minecraft:block/cube"}')

    def test_png_header_requires_exact_rgba8_dimensions(self):
        signature = b"\x89PNG\r\n\x1a\n"
        header = struct.pack(">I", 13) + b"IHDR"
        exact = signature + header + struct.pack(">IIBBBBB", 16, 16, 8, 6, 0, 0, 0)
        exact += b"\x00\x00\x00\x00"
        verify_png_header("texture.png", exact)
        wrong = signature + header + struct.pack(">IIBBBBB", 32, 16, 8, 6, 0, 0, 0)
        wrong += b"\x00\x00\x00\x00"
        with self.assertRaisesRegex(ValueError, "16x16 RGBA8"):
            verify_png_header("texture.png", wrong)

    def test_hash_failure_reports_label_and_both_values(self):
        with self.assertRaisesRegex(ValueError, "route SHA-256: got a, expected b"):
            verify_hash("route SHA-256", "a", "b")


if __name__ == "__main__":
    unittest.main()
