# SPDX-License-Identifier: LGPL-3.0-only

import hashlib
import json
import sys
import tempfile
import unittest
from pathlib import Path


ROOT = Path(__file__).resolve().parents[2]
sys.path.insert(0, str(ROOT / "tools"))

from ae2_native_structural_contract import (  # noqa: E402
    CURRENT_HOST_EVIDENCE,
    EXPECTED_PROFILE_SHA256,
    FULL_PACK_OVERRIDE_EVIDENCE,
)
from generate_ae2_native_structural_profile import (  # noqa: E402
    PROVENANCE_PATH,
    SUPPORT_MATRIX_PATH,
    _snake_case_view,
    generated_outputs,
    metadata_outputs,
)


class GenerateAe2NativeStructuralProfileTest(unittest.TestCase):
    def test_current_full_pack_override_evidence_is_generated_into_metadata(self):
        outputs = metadata_outputs(ROOT)
        support = json.loads(outputs[ROOT / SUPPORT_MATRIX_PATH])
        profile = next(
            item
            for item in support["profiles"]
            if item.get("profileId") == "ae2-cable-bus-structural"
        )
        self.assertEqual(
            FULL_PACK_OVERRIDE_EVIDENCE,
            profile["fullPackOverrideEvidence"],
        )
        self.assertEqual(CURRENT_HOST_EVIDENCE, profile["currentHostEvidence"])
        provenance = json.loads(outputs[ROOT / PROVENANCE_PATH])
        self.assertEqual(
            _snake_case_view(FULL_PACK_OVERRIDE_EVIDENCE),
            provenance["ae2"]["s1_native_structural_profile_audit"][
                "full_pack_override_evidence"
            ],
        )
        self.assertEqual(
            _snake_case_view(CURRENT_HOST_EVIDENCE),
            provenance["ae2"]["s1_native_structural_profile_audit"][
                "current_host_evidence"
            ],
        )
        route_profile = ROOT / (
            "src/main/resources/bluemap-ae2/profiles/ae2/19.2.17/routes/"
            "cable-bus-structural/profile.json"
        )
        self.assertEqual(
            EXPECTED_PROFILE_SHA256,
            hashlib.sha256(route_profile.read_bytes()).hexdigest(),
        )

    def test_non_exact_input_fails_before_generation(self):
        with tempfile.TemporaryDirectory() as directory:
            path = Path(directory) / "not-ae2.jar"
            path.write_bytes(b"not the exact artifact")
            with self.assertRaisesRegex(ValueError, "not the exact AE2"):
                generated_outputs(path)

    def test_missing_input_fails_closed(self):
        with tempfile.TemporaryDirectory() as directory:
            path = Path(directory) / "missing.jar"
            with self.assertRaisesRegex(ValueError, "not a regular file"):
                generated_outputs(path)


if __name__ == "__main__":
    unittest.main()
