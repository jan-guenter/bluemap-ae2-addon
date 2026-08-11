# SPDX-License-Identifier: LGPL-3.0-only

import hashlib
import struct
import sys
import unittest
from pathlib import Path


ROOT = Path(__file__).resolve().parents[2]
sys.path.insert(0, str(ROOT / "tools"))

from verify_m3_completion_artifact import (  # noqa: E402
    EXPECTED_RUNTIME_CLASS_SHA256,
    EXPECTED_SOURCE_SHA256,
    require_markers,
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


class VerifyM3CompletionArtifactTest(unittest.TestCase):
    def test_runtime_and_source_allowlists_are_closed(self):
        self.assertEqual(30, len(EXPECTED_RUNTIME_CLASS_SHA256))
        self.assertEqual(30, len(set(EXPECTED_RUNTIME_CLASS_SHA256)))
        self.assertEqual(30, len(EXPECTED_SOURCE_SHA256))
        self.assertEqual(30, len(set(EXPECTED_SOURCE_SHA256)))
        for digest in (
            *EXPECTED_RUNTIME_CLASS_SHA256.values(),
            *EXPECTED_SOURCE_SHA256.values(),
        ):
            self.assertEqual(64, len(digest))
            int(digest, 16)

    def test_png_header_accepts_exact_rgba8_family_dimensions(self):
        verify_png_header("assets/ae2/textures/block/paint1.png", png_header(16, 16))
        verify_png_header("assets/ae2/textures/block/inscriber.png", png_header(64, 64))
        verify_png_header("assets/ae2/textures/block/skychest.png", png_header(64, 64))
        verify_png_header(
            "assets/ae2/textures/block/skyblockchest.png", png_header(64, 64)
        )
        for label, path, raw in (
            (
                "wrong ordinary width",
                "assets/ae2/textures/block/paint1.png",
                png_header(32, 16),
            ),
            (
                "wrong chest height",
                "assets/ae2/textures/block/skychest.png",
                png_header(64, 16),
            ),
            (
                "wrong bit depth",
                "assets/ae2/textures/block/crank.png",
                png_header(16, 16, 16),
            ),
            (
                "wrong color type",
                "assets/ae2/textures/block/crank.png",
                png_header(16, 16, 8, 2),
            ),
        ):
            with self.subTest(label=label):
                with self.assertRaisesRegex(ValueError, "encoding changed"):
                    verify_png_header(path, raw)

    def test_hash_guard_fails_closed(self):
        actual = hashlib.sha256(b"actual").hexdigest()
        expected = hashlib.sha256(b"expected").hexdigest()
        verify_hash("same", actual, actual)
        with self.assertRaisesRegex(ValueError, "got"):
            verify_hash("different", actual, expected)

    def test_source_marker_guard_fails_closed(self):
        source = b"ClientState.DEFAULT AxisPosition.NONE"
        require_markers(
            source,
            "pylon default",
            (b"ClientState.DEFAULT", b"AxisPosition.NONE"),
        )
        with self.assertRaisesRegex(ValueError, "source marker changed"):
            require_markers(
                source,
                "pylon default",
                (b"ClientState.DEFAULT", b"SpatialPylonTextureType.DIM"),
            )


if __name__ == "__main__":
    unittest.main()
