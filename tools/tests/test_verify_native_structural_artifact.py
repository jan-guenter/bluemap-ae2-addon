# SPDX-License-Identifier: LGPL-3.0-only

import hashlib
import sys
import unittest
from pathlib import Path


ROOT = Path(__file__).resolve().parents[2]
sys.path.insert(0, str(ROOT / "tools"))

from verify_native_structural_artifact import (  # noqa: E402
    BLUEMAP_HOST_RESOURCES,
    BLUEMAP_HOST_SHA256,
    BLUEMAP_HOST_SIZE,
    EXTENSION_REGISTRY_CLASSES,
    EXPECTED_SOURCE_MARKERS,
    GLASSENTIAL_RESOURCES,
    GLASSENTIAL_SHA256,
    GLASSENTIAL_SIZE,
    MINECRAFT_CLIENT_SHA256,
    MINECRAFT_CLIENT_SIZE,
    expected_audit_classes,
    require_markers,
    verify_extension_endpoint_artifacts,
    verify_hash,
)


class VerifyNativeStructuralArtifactTest(unittest.TestCase):
    def test_runtime_and_source_audit_class_set_is_closed(self):
        classes = expected_audit_classes()
        self.assertEqual(82, len(classes))
        self.assertEqual(82, len(set(classes)))
        self.assertIn("appeng.core.definitions.AEParts", classes)
        self.assertIn("appeng.parts.automation.AnnihilationPlanePart", classes)
        self.assertIn(
            "appeng.blockentity.crafting.MolecularAssemblerBlockEntity", classes
        )
        self.assertIn("appeng.me.helpers.IGridConnectedBlockEntity", classes)
        self.assertIn(
            "appeng.me.cluster.implementations.QuantumCalculator", classes
        )
        self.assertIn(
            "appeng.thirdparty.codechicken.lib.model.pipeline.transformers."
            "QuadReInterpolator",
            classes,
        )
        self.assertIn(
            "appeng.thirdparty.codechicken.lib.math.InterpHelper",
            classes,
        )
        self.assertIn("appeng.parts.automation.PlaneConnectionHelper", classes)
        self.assertIn("appeng.parts.automation.PlaneBakedModel", classes)
        self.assertIn("appeng.items.parts.FacadeItem", classes)
        self.assertIn("appeng.facade.FacadeContainer", classes)
        self.assertIn("appeng.decorative.solid.QuartzGlassBlock", classes)
        self.assertEqual(26_836_906, MINECRAFT_CLIENT_SIZE)
        self.assertEqual(
            "499f6897d1837516680f3114072d8106e11c9adcd933fe5cf051b551089b0c99",
            MINECRAFT_CLIENT_SHA256,
        )
        self.assertEqual(702_249, GLASSENTIAL_SIZE)
        self.assertEqual(
            "1f0c8f7533bf3b2002575219ba795fd32a44cc5085c2710624ebbf69e6121471",
            GLASSENTIAL_SHA256,
        )
        self.assertEqual(
            {
                "assets/minecraft/models/block/glass.json": (192, "dc3cf6"),
                "assets/glassential/textures/block/glass.png": (1_041, "0a5534"),
                "assets/glassential/textures/block/glass.png.mcmeta": (97, "231175"),
            },
            {
                path: (size, sha256[:6])
                for path, (size, sha256) in GLASSENTIAL_RESOURCES.items()
            },
        )
        self.assertEqual(6_467_235, BLUEMAP_HOST_SIZE)
        self.assertEqual(
            "749f7647fa29764cea113114a7ab3259271bab3da22720989f2bd9fd1f3ba150",
            BLUEMAP_HOST_SHA256,
        )
        self.assertEqual(
            {
                "META-INF/neoforge.mods.toml": (560, "5e83ee"),
                "de/bluecolored/bluemap/version.json": (110, "a047fa"),
            },
            {
                path: (size, sha256[:6])
                for path, (size, sha256) in BLUEMAP_HOST_RESOURCES.items()
            },
        )

    def test_source_marker_guard_fails_closed(self):
        source = b"alpha beta gamma"
        require_markers(source, "example", (b"alpha", b"gamma"))
        with self.assertRaisesRegex(ValueError, "source markers changed"):
            require_markers(source, "example", (b"alpha", b"delta"))
        self.assertGreaterEqual(len(EXPECTED_SOURCE_MARKERS), 41)

    def test_hash_guard_fails_closed(self):
        actual = hashlib.sha256(b"actual").hexdigest()
        expected = hashlib.sha256(b"expected").hexdigest()
        verify_hash("same", actual, actual)
        with self.assertRaisesRegex(ValueError, "got"):
            verify_hash("different", actual, expected)

    def test_extension_endpoint_artifact_gate_is_closed(self):
        self.assertEqual(
            {
                "expandedae-2.1.1",
                "megacells-4.11.0",
                "advanced_ae-1.6.12-1.21.1",
                "extendedae-1.21-2.2.35-neoforge",
            },
            set(EXTENSION_REGISTRY_CLASSES),
        )
        with self.assertRaisesRegex(ValueError, "artifact set changed"):
            verify_extension_endpoint_artifacts({})


if __name__ == "__main__":
    unittest.main()
