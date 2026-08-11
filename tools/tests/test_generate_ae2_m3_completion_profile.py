# SPDX-License-Identifier: LGPL-3.0-only

import sys
import unittest
from pathlib import Path


ROOT = Path(__file__).resolve().parents[2]
sys.path.insert(0, str(ROOT / "tools"))

from ae2_m3_completion_contract import expected_manifest  # noqa: E402
from generate_ae2_m3_completion_profile import (  # noqa: E402
    verify_partition_hashes,
)


class GenerateAe2M3CompletionProfileTest(unittest.TestCase):
    def test_exact_partition_hashes_pass(self):
        verify_partition_hashes(expected_manifest())

    def test_changed_partition_fails_closed(self):
        changed = expected_manifest().replace(b"paint1.png", b"paint9.png", 1)
        with self.assertRaises((KeyError, ValueError)):
            verify_partition_hashes(changed)


if __name__ == "__main__":
    unittest.main()
