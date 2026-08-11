# SPDX-License-Identifier: LGPL-3.0-only

import hashlib
import struct
import sys
import unittest
from pathlib import Path


ROOT = Path(__file__).resolve().parents[2]
sys.path.insert(0, str(ROOT / "tools"))

from verify_quantum_bridge_artifact import (  # noqa: E402
    EXPECTED_RUNTIME_CLASS_SHA256,
    EXPECTED_SOURCE_SHA256,
    verify_hash,
    verify_png_header,
)


def png_header(width: int, height: int, bit_depth: int = 8, color_type: int = 6):
    return (
        b"\x89PNG\r\n\x1a\n"
        + struct.pack(">I", 13)
        + b"IHDR"
        + struct.pack(">IIBB", width, height, bit_depth, color_type)
        + b"\x00" * 7
    )


class VerifyQuantumBridgeArtifactTest(unittest.TestCase):
    def test_runtime_and_source_allowlists_are_closed(self):
        self.assertEqual(12, len(EXPECTED_RUNTIME_CLASS_SHA256))
        self.assertEqual(12, len(set(EXPECTED_RUNTIME_CLASS_SHA256)))
        self.assertEqual(10, len(EXPECTED_SOURCE_SHA256))
        self.assertEqual(10, len(set(EXPECTED_SOURCE_SHA256)))
        for digest in (*EXPECTED_RUNTIME_CLASS_SHA256.values(), *EXPECTED_SOURCE_SHA256.values()):
            self.assertEqual(64, len(digest))
            int(digest, 16)

    def test_png_header_accepts_only_exact_static_or_animated_rgba8_shape(self):
        verify_png_header("static", png_header(16, 16), False)
        verify_png_header("animated", png_header(16, 512), True)
        for label, raw, animated in (
            ("wrong width", png_header(32, 16), False),
            ("wrong static height", png_header(16, 512), False),
            ("wrong animated height", png_header(16, 16), True),
            ("wrong bit depth", png_header(16, 16, 16), False),
            ("wrong color type", png_header(16, 16, 8, 2), False),
        ):
            with self.subTest(label=label):
                with self.assertRaisesRegex(ValueError, "encoding changed"):
                    verify_png_header(label, raw, animated)

    def test_hash_guard_fails_closed(self):
        actual = hashlib.sha256(b"actual").hexdigest()
        expected = hashlib.sha256(b"expected").hexdigest()
        verify_hash("same", actual, actual)
        with self.assertRaisesRegex(ValueError, "got"):
            verify_hash("different", actual, expected)


if __name__ == "__main__":
    unittest.main()
