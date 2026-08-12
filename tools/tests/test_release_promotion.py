# SPDX-License-Identifier: LGPL-3.0-only

import re
import unittest
from pathlib import Path


ROOT = Path(__file__).resolve().parents[2]
BUILD = (ROOT / "build.gradle").read_text(encoding="utf-8")
CI = (ROOT / ".github/workflows/ci.yml").read_text(encoding="utf-8")
RELEASE = (ROOT / ".github/workflows/release.yml").read_text(encoding="utf-8")


class ReleasePromotionContractTest(unittest.TestCase):
    def test_exact_promotion_java_is_consistent(self):
        build_version = re.search(
            r"promotionJavaVersion = '([^']+)'", BUILD
        ).group(1)
        workflow_versions = [
            re.search(r"PROMOTION_JAVA_VERSION: '([^']+)'", workflow).group(1)
            for workflow in (CI, RELEASE)
        ]

        self.assertEqual("21.0.12+8", build_version)
        self.assertEqual(
            ["21.0.12+8.0.LTS", "21.0.12+8.0.LTS"], workflow_versions
        )
        self.assertNotIn("java-version: '21'", CI)
        self.assertNotIn("java-version: '21'", RELEASE)

    def test_release_candidate_schema_and_artifacts_are_closed(self):
        self.assertIn(
            '"release-candidates/${project.version}.json"', BUILD
        )
        self.assertIn("tasks.register('verifyReleaseCandidate')", BUILD)
        self.assertIn("dependsOn tasks.named('verifyReleaseMetadata')", BUILD)
        self.assertIn(
            "['productionJar', 'sourcesJar', 'pom', 'moduleMetadata'] as Set",
            BUILD,
        )
        self.assertIn("['sizeBytes', 'sha256'] as Set", BUILD)
        self.assertIn("object_pairs_hook=reject_duplicate_keys", BUILD)

    def test_ci_bootstraps_then_requires_non_snapshot_candidate(self):
        self.assertIn("tasks+=(verifyReleaseMetadata)", CI)
        upload = CI.index("- name: Upload build outputs and reports")
        candidate = CI.index("- name: Verify committed release candidate")
        self.assertLess(upload, candidate)
        candidate_step = CI[candidate:]
        self.assertIn("if: steps.version.outputs.release == 'true'", candidate_step)
        self.assertIn("verifyReleaseCandidate", candidate_step)

    def test_tag_verifies_candidate_before_external_writes(self):
        candidate = RELEASE.index("verifyReleaseCandidate")
        write_steps = (
            "- name: Create draft GitHub release",
            "- name: Attest release JARs",
            "- name: Publish Maven package",
            "- name: Reverify and publish GitHub release",
        )
        self.assertTrue(all(candidate < RELEASE.index(step) for step in write_steps))
        self.assertIn("cache-read-only: true", RELEASE)

    def test_existing_exact_maven_version_skips_republication(self):
        inspect = RELEASE.index("- name: Inspect immutable Maven version")
        draft = RELEASE.index("- name: Create draft GitHub release")
        publish = RELEASE.index("- name: Publish Maven package")
        self.assertLess(inspect, draft)
        self.assertLess(inspect, publish)
        self.assertIn("cmp \"${local_files[index]}\"", RELEASE)
        self.assertIn("verify_checksums", RELEASE)
        self.assertIn("maven-metadata.xml", RELEASE)
        self.assertIn("versions.count(sys.argv[2])", RELEASE)
        self.assertIn("echo 'publish=false'", RELEASE)
        self.assertIn("if: steps.maven.outputs.publish == 'true'", RELEASE[publish:])

    def test_release_versions_are_serialized_and_draft_assets_are_exact(self):
        self.assertIn(
            "group: release-maven-${{ github.repository }}", RELEASE
        )
        verify_assets = RELEASE.index("- name: Verify exact draft release assets")
        attest = RELEASE.index("- name: Attest release JARs")
        publish = RELEASE.index("- name: Publish Maven package")
        self.assertLess(verify_assets, attest)
        self.assertLess(verify_assets, publish)
        asset_gate = RELEASE[verify_assets:attest]
        self.assertIn("Draft Release asset allowlist mismatch", asset_gate)
        self.assertIn("gh release download", asset_gate)
        self.assertIn("cmp \"${asset}\"", asset_gate)
        final_gate = RELEASE.index("- name: Reverify and publish GitHub release")
        self.assertIn("final-draft-assets", RELEASE[final_gate:])
        self.assertIn("cmp \"${asset}\"", RELEASE[final_gate:])


if __name__ == "__main__":
    unittest.main()
