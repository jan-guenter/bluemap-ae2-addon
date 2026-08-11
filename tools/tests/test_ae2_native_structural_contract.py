# SPDX-License-Identifier: LGPL-3.0-only

import hashlib
import json
import math
import sys
import unittest
from pathlib import Path


ROOT = Path(__file__).resolve().parents[2]
sys.path.insert(0, str(ROOT / "tools"))

from ae2_native_structural_contract import (  # noqa: E402
    ACCEPTED_ARTIFACTS,
    ACCEPTED_GALLERY_SCHEMAS,
    CURRENT_HOST_EVIDENCE,
    DIRECT_NEUTRAL_RESOURCE_COUNT,
    ENDPOINT_SIDE_RULE_COUNTS,
    ENDPOINT_SIDE_RULE_KIND_COUNT,
    ENDPOINT_SIDE_RULES,
    ENDPOINT_BLOCKSTATE_SHA256,
    ENDPOINT_STATE_SCHEMAS,
    ENDPOINT_STATE_COMBINATION_COUNT,
    ENDPOINT_STATE_SIDE_COMBINATION_COUNT,
    ENDPOINTS,
    EXPECTED_FROZEN_OUTPUT_SHA256,
    EXPECTED_PROFILE_SHA256,
    EXPECTED_RESOURCE_MANIFEST_SHA256,
    EXPECTED_RESOURCE_SIZES_MANIFEST_SHA256,
    FACE_PARTS,
    FACADE_WHITELIST_BLOCK_IDS,
    FACADE_WHITELIST_BLOCKSTATE_SHA256,
    FACADE_WHITELIST_NEUTRAL_STATES,
    FACADE_WHITELIST_OPTIONAL_TAGS,
    FACADE_WHITELIST_SAME_STATE_SKIP_RENDERING,
    FACADE_WHITELIST_STATE_POLICY,
    FACADE_WHITELIST_STATE_CLASSIFICATION_POLICY,
    FACADE_WHITELIST_STATE_SCHEMAS,
    FACADE_WHITELIST_STATE_COMBINATION_COUNT,
    FACADE_WHITELIST_SOLID_RENDER_TRUE_STATE_COUNT,
    FACADE_WHITELIST_SAME_STATE_SKIP_RENDERING_TRUE_STATE_COUNT,
    FACADE_AMBIENT_OCCLUSION_DIRECTION_POLICY,
    FACADE_CARDINAL_VARIANT_TRANSFORM_POLICY,
    FACADE_CORNER_KICK_ANALYZER_EPSILON_BLOCKS,
    FACADE_CORNER_KICK_RUNTIME_EPSILON_SIXTEENTHS,
    FACADE_CORNER_KICK_SOURCE_EPSILON_BLOCKS,
    FACADE_CUTOUT_STRIP_AABB_POLICY,
    FACADE_DIRECTIONAL_SHADE_POLICY,
    FACADE_ORDINARY_MATERIAL_POLICY,
    FACADE_ORDINARY_SKIP_RENDERING_CONTROLS,
    FACADE_QUARTZ_SKIP_RENDERING_POLICY,
    FACADE_SKIP_RENDERING_POLICY,
    FACADE_SUPPORT_SET_POLICY,
    FACADE_TINT_POLICY,
    FACADE_UV_REINTERPOLATION_POLICY,
    FACADE_WEIGHTED_VARIANT_POLICY,
    FULL_PACK_OVERRIDE_EVIDENCE,
    KNOWN_EXTENSION_FALLBACK_CONTROL,
    KNOWN_UNSUPPORTED_COMPATIBLE_ENDPOINT_POLICY,
    LIVE_MODEL_SEMANTIC_SIGNATURE_SHA256,
    LIVE_TEXTURE_SEMANTIC_SIGNATURE_SHA256,
    MALFORMED_KNOWN_EXTENSION_ENDPOINT_POLICY,
    MALFORMED_NATIVE_ENDPOINT_POLICY,
    NATIVE_FACADE_NEUTRAL_MATERIALS,
    NATIVE_FACADE_NEUTRAL_SCOPE,
    ORIENTATION_STATE_COUNT,
    PACK_COMMIT,
    PACK_NAME,
    PACK_VERSION,
    PART_COLLISION_POLICIES,
    PROFILE_ID,
    PLANE_CONNECTION_MASK_BITS,
    PLANE_CONNECTION_MASK_COUNT,
    PLANE_COLLISION_BOUND_BITS_BY_FACE,
    PLANE_COORDINATE_SPACE_POLICY,
    PLANE_RENDER_BOUND_BITS,
    QUARTZ_FACADE_DEPENDENCY_TEXTURE_COUNT,
    QUARTZ_FACADE_DEPENDENCY_TEXTURE_SEMANTIC_SIGNATURE_SHA256,
    REQUIRED_RESOURCE_BYTES,
    REQUIRED_RESOURCE_COUNT,
    SCHEMA_VERSION,
    SMART_CORE_PART_COUNT,
    SMART_CORE_PART_IDS,
    SPIN_CAPABLE_PART_COUNT,
    STRUCTURAL_MAP_COLOR_ILLUMINATION_POLICY,
    UNSUPPORTED_COMPATIBLE_ENDPOINT_ARTIFACTS,
    UNSUPPORTED_COMPATIBLE_ENDPOINT_COUNT,
    UNSUPPORTED_COMPATIBLE_ENDPOINTS,
    direct_model_roots,
    parse_checksum_manifest,
    parse_size_manifest,
    profile,
    profile_bytes,
)


class Ae2NativeStructuralContractTest(unittest.TestCase):
    def route_root(self) -> Path:
        return ROOT / (
            "src/main/resources/bluemap-ae2/profiles/ae2/19.2.17/routes/"
            "cable-bus-structural"
        )

    def test_current_full_pack_override_evidence_is_exact_and_unbundled(self):
        evidence = FULL_PACK_OVERRIDE_EVIDENCE
        self.assertEqual(
            {
                "provider": "CurseForge",
                "projectId": 945_149,
                "fileId": 8_440_850,
            },
            evidence["distribution"],
        )
        self.assertEqual(702_249, evidence["artifact"]["sizeBytes"])
        self.assertFalse(evidence["artifact"]["bundled"])
        self.assertEqual(3, len(evidence["resources"]))
        self.assertEqual(
            [192, 1_041, 97],
            [resource["sizeBytes"] for resource in evidence["resources"]],
        )
        self.assertEqual(
            {
                "type": "connecting",
                "layout": "full",
                "render_type": "cutout",
            },
            evidence["semanticMetadata"]["fusion"],
        )
        self.assertEqual(
            "ignored-by-resource-loader;not-an-animation-contract",
            evidence["semanticMetadata"]["blueMap522NonAnimationMcmetaPolicy"],
        )
        self.assertEqual(
            "pending",
            evidence["acceptance"]["freshUnmodifiedClientVisual"],
        )

    def test_current_canonical_bluemap_host_evidence_is_exact_and_unbundled(self):
        evidence = CURRENT_HOST_EVIDENCE
        self.assertEqual(
            "v5.22-agent.backport-5.22-mc1.21.1-2",
            evidence["release"]["tag"],
        )
        self.assertEqual(
            "9be321df995a1103808621d529eb72773e719d4d",
            evidence["release"]["gitCommit"],
        )
        self.assertEqual(6_467_235, evidence["artifact"]["sizeBytes"])
        self.assertEqual(
            "749f7647fa29764cea113114a7ab3259271bab3da22720989f2bd9fd1f3ba150",
            evidence["artifact"]["sha256"],
        )
        self.assertFalse(evidence["artifact"]["bundled"])
        self.assertEqual(
            "[21.1.248,21.2)",
            evidence["implementation"]["neoForgeVersionRange"],
        )
        self.assertEqual(
            "[1.21.1,1.21.2)",
            evidence["implementation"]["minecraftVersionRange"],
        )
        self.assertEqual(
            [560, 110],
            [resource["sizeBytes"] for resource in evidence["embeddedEvidence"]],
        )

    def test_exact_part_orientation_and_endpoint_catalogs(self):
        self.assertEqual(10, SCHEMA_VERSION)
        self.assertEqual("ae2-cable-bus-structural", PROFILE_ID)
        self.assertEqual("All the Mons", PACK_NAME)
        self.assertEqual("1.2.0", PACK_VERSION)
        self.assertEqual(
            "c7bb230f21d14d26859d0b92548f089b3a493ad9", PACK_COMMIT
        )
        self.assertEqual(29, len(FACE_PARTS))
        self.assertEqual(29, len({part["id"] for part in FACE_PARTS}))
        spin_parts = [part for part in FACE_PARTS if part["spin"] != "ignored"]
        self.assertEqual(SPIN_CAPABLE_PART_COUNT, len(spin_parts))
        self.assertEqual(
            ORIENTATION_STATE_COUNT,
            (len(FACE_PARTS) - len(spin_parts)) * 6 + len(spin_parts) * 6 * 4,
        )
        self.assertEqual(["ae2:cable_anchor"], [
            part["id"] for part in FACE_PARTS if part["denseCenter"]
        ])
        self.assertEqual(2, SMART_CORE_PART_COUNT)
        self.assertEqual(
            ("ae2:level_emitter", "ae2:energy_level_emitter"),
            SMART_CORE_PART_IDS,
        )
        self.assertEqual(
            {"left": 1, "down": 2, "right": 4, "up": 8},
            PLANE_CONNECTION_MASK_BITS,
        )
        self.assertEqual(16, PLANE_CONNECTION_MASK_COUNT)
        expected_collision_bits_by_face = {
            "down": {
                "minX": "right", "maxX": "left", "minY": "down", "maxY": "up",
            },
            "up": {
                "minX": "left", "maxX": "right", "minY": "up", "maxY": "down",
            },
            "north": {
                "minX": "left", "maxX": "right", "minY": "down", "maxY": "up",
            },
            "south": {
                "minX": "left", "maxX": "right", "minY": "down", "maxY": "up",
            },
            "west": {
                "minX": "right", "maxX": "left", "minY": "down", "maxY": "up",
            },
            "east": {
                "minX": "right", "maxX": "left", "minY": "down", "maxY": "up",
            },
        }
        self.assertEqual(
            expected_collision_bits_by_face,
            PLANE_COLLISION_BOUND_BITS_BY_FACE,
        )
        self.assertEqual(
            [part["id"] for part in FACE_PARTS],
            list(PART_COLLISION_POLICIES),
        )
        self.assertEqual(
            [[6, 6, 10, 10, 10, 16]],
            PART_COLLISION_POLICIES["ae2:quartz_fiber"]["boxes"],
        )
        self.assertEqual(
            [[7, 7, 10, 9, 9, 14]],
            PART_COLLISION_POLICIES["ae2:cable_anchor"]["withSameSideFacade"],
        )
        self.assertEqual(30, len(ENDPOINTS))
        self.assertEqual(30, len({endpoint["id"] for endpoint in ENDPOINTS}))
        self.assertEqual(
            [
                "ae2:inscriber",
                "ae2:wireless_access_point",
                "ae2:charger",
                "ae2:quantum_ring",
                "ae2:quantum_link",
                "ae2:spatial_pylon",
                "ae2:spatial_io_port",
                "ae2:spatial_anchor",
                "ae2:controller",
                "ae2:drive",
                "ae2:chest",
                "ae2:interface",
                "ae2:io_port",
                "ae2:energy_acceptor",
                "ae2:crystal_resonance_generator",
                "ae2:vibration_chamber",
                "ae2:growth_accelerator",
                "ae2:energy_cell",
                "ae2:dense_energy_cell",
                "ae2:creative_energy_cell",
                "ae2:crafting_unit",
                "ae2:crafting_accelerator",
                "ae2:1k_crafting_storage",
                "ae2:4k_crafting_storage",
                "ae2:16k_crafting_storage",
                "ae2:64k_crafting_storage",
                "ae2:256k_crafting_storage",
                "ae2:crafting_monitor",
                "ae2:pattern_provider",
                "ae2:molecular_assembler",
            ],
            [endpoint["id"] for endpoint in ENDPOINTS],
        )
        cable_types = [endpoint["cableType"] for endpoint in ENDPOINTS]
        self.assertEqual(18, cable_types.count("SMART"))
        self.assertEqual(9, cable_types.count("COVERED"))
        self.assertEqual(3, cable_types.count("DENSE_SMART"))
        self.assertEqual(ENDPOINT_SIDE_RULE_KIND_COUNT, len(ENDPOINT_SIDE_RULES))
        self.assertEqual(
            [
                "ALL",
                "BACK",
                "NO_FRONT",
                "FRONT_BACK",
                "PUSH_DIRECTION",
                "FORMED_CRAFTING",
                "FORMED_QUANTUM",
                "VALID_STRAIGHT_PYLON",
            ],
            [rule["id"] for rule in ENDPOINT_SIDE_RULES],
        )
        self.assertEqual(
            {
                "ALL": 12,
                "BACK": 2,
                "NO_FRONT": 3,
                "FRONT_BACK": 1,
                "PUSH_DIRECTION": 1,
                "FORMED_CRAFTING": 8,
                "FORMED_QUANTUM": 2,
                "VALID_STRAIGHT_PYLON": 1,
            },
            ENDPOINT_SIDE_RULE_COUNTS,
        )
        self.assertEqual(
            "ae2:quantum_ring",
            ENDPOINTS[4]["blockEntityId"],
        )
        self.assertEqual(
            ["ae2:crafting_unit", "ae2:crafting_unit"],
            [endpoint["blockEntityId"] for endpoint in ENDPOINTS[20:22]],
        )
        self.assertEqual(
            ["ae2:crafting_storage"] * 5,
            [endpoint["blockEntityId"] for endpoint in ENDPOINTS[22:27]],
        )
        self.assertEqual(
            [endpoint["id"] for endpoint in ENDPOINTS],
            list(ENDPOINT_STATE_SCHEMAS),
        )
        self.assertEqual(
            [endpoint["id"] for endpoint in ENDPOINTS],
            list(ENDPOINT_BLOCKSTATE_SHA256),
        )
        self.assertEqual(
            ["facing", "state", "waterlogged"],
            list(ENDPOINT_STATE_SCHEMAS["ae2:wireless_access_point"]),
        )
        self.assertEqual(
            ["facing", "waterlogged"],
            list(ENDPOINT_STATE_SCHEMAS["ae2:crystal_resonance_generator"]),
        )
        self.assertEqual(
            {"powered": ["false", "true"]},
            ENDPOINT_STATE_SCHEMAS["ae2:molecular_assembler"],
        )
        self.assertEqual({}, ENDPOINT_STATE_SCHEMAS["ae2:interface"])
        self.assertEqual({}, ENDPOINT_STATE_SCHEMAS["ae2:energy_acceptor"])
        self.assertEqual({}, ENDPOINT_STATE_SCHEMAS["ae2:creative_energy_cell"])
        endpoint_cardinalities = [
            math.prod(len(values) for values in schema.values())
            for schema in ENDPOINT_STATE_SCHEMAS.values()
        ]
        self.assertEqual(
            [
                48, 36, 24, 4, 4, 2, 48, 12, 18, 24, 48, 1, 48, 1, 12,
                48, 12, 5, 5, 1, 4, 4, 4, 4, 4, 4, 4, 96, 7, 2,
            ],
            endpoint_cardinalities,
        )
        self.assertEqual(ENDPOINT_STATE_COMBINATION_COUNT, sum(endpoint_cardinalities))
        self.assertEqual(
            ENDPOINT_STATE_SIDE_COMBINATION_COUNT,
            ENDPOINT_STATE_COMBINATION_COUNT * 6,
        )
        generated = profile(
            (self.route_root() / "required-resources.sha256").read_bytes(),
            (self.route_root() / "required-resources.tsv").read_bytes(),
        )
        self.assertEqual(list(ENDPOINTS), generated["nativeEndpoints"])
        self.assertEqual(
            ENDPOINT_SIDE_RULE_COUNTS,
            generated["endpointSidePolicy"]["ruleCounts"],
        )
        self.assertEqual(
            "exact-complete-key-set-and-serialized-value-domain-required",
            generated["endpointSidePolicy"]["persistedBlockState"],
        )
        self.assertEqual(
            ENDPOINT_STATE_COMBINATION_COUNT,
            generated["endpointSidePolicy"]["stateCartesianCount"],
        )
        self.assertEqual(
            ENDPOINT_STATE_SIDE_COMBINATION_COUNT,
            generated["endpointSidePolicy"]["stateSideCartesianCount"],
        )
        self.assertEqual(
            list(SMART_CORE_PART_IDS),
            generated["renderPolicy"]["glassCoreOverrides"]["partIds"],
        )
        self.assertEqual(
            [1, 1, 15, 15],
            generated["renderPolicy"]["planeConnectionMasks"]
            ["facadeCutout"]["baseLocalBounds"],
        )
        expected_collision_bits_by_face = {
            "down": {
                "minX": "right", "maxX": "left", "minY": "down", "maxY": "up",
            },
            "up": {
                "minX": "left", "maxX": "right", "minY": "up", "maxY": "down",
            },
            "north": {
                "minX": "left", "maxX": "right", "minY": "down", "maxY": "up",
            },
            "south": {
                "minX": "left", "maxX": "right", "minY": "down", "maxY": "up",
            },
            "west": {
                "minX": "right", "maxX": "left", "minY": "down", "maxY": "up",
            },
            "east": {
                "minX": "right", "maxX": "left", "minY": "down", "maxY": "up",
            },
        }
        plane_masks = generated["renderPolicy"]["planeConnectionMasks"]
        self.assertEqual(
            expected_collision_bits_by_face,
            plane_masks["collisionBoundBitsByInstalledFace"],
        )
        self.assertEqual(
            expected_collision_bits_by_face,
            plane_masks["facadeCutout"]["boundBitsByInstalledFace"],
        )
        self.assertEqual(
            PLANE_COORDINATE_SPACE_POLICY,
            plane_masks["coordinateSpaces"],
        )
        self.assertEqual(
            {"minX": "right", "maxX": "left", "minY": "down", "maxY": "up"},
            PLANE_RENDER_BOUND_BITS,
        )
        self.assertEqual(
            PART_COLLISION_POLICIES,
            generated["renderPolicy"]["facadeCutoutCollision"]["partPolicies"],
        )
        facade_eligibility = generated["renderPolicy"]["facades"]["eligibility"]
        self.assertEqual(list(FACADE_WHITELIST_BLOCK_IDS),
                         facade_eligibility["whitelistBlocks"])
        self.assertEqual(list(FACADE_WHITELIST_OPTIONAL_TAGS),
                         facade_eligibility["optionalTags"])
        self.assertEqual(
            list(FACADE_WHITELIST_NEUTRAL_STATES),
            facade_eligibility["neutralDefaultStates"],
        )
        self.assertEqual(24, facade_eligibility["stateSchemaCount"])
        facade_cardinalities = [
            math.prod(len(values) for values in schema.values())
            for schema in FACADE_WHITELIST_STATE_SCHEMAS.values()
        ]
        self.assertEqual(
            [
                1, 1, 256, 2, 8, 8, 12, 12, 48, 12, 24, 24, 2, 1, 1,
                18, 4, 4, 4, 4, 4, 96, 4, 4,
            ],
            facade_cardinalities,
        )
        self.assertEqual(
            FACADE_WHITELIST_STATE_COMBINATION_COUNT,
            sum(facade_cardinalities),
        )
        self.assertEqual(
            FACADE_WHITELIST_STATE_COMBINATION_COUNT,
            facade_eligibility["stateCartesianCount"],
        )
        self.assertEqual(
            FACADE_WHITELIST_SOLID_RENDER_TRUE_STATE_COUNT,
            sum(
                cardinality
                for cardinality, neutral in zip(
                    facade_cardinalities, FACADE_WHITELIST_NEUTRAL_STATES
                )
                if neutral["solidRender"]
            ),
        )
        self.assertEqual(
            FACADE_WHITELIST_SOLID_RENDER_TRUE_STATE_COUNT,
            facade_eligibility["solidRenderTrueCartesianCount"],
        )
        self.assertEqual(
            FACADE_WHITELIST_SAME_STATE_SKIP_RENDERING_TRUE_STATE_COUNT,
            sum(
                cardinality
                for cardinality, neutral in zip(
                    facade_cardinalities, FACADE_WHITELIST_NEUTRAL_STATES
                )
                if neutral["sameStateSkipRendering"]
            ),
        )
        self.assertEqual(
            FACADE_WHITELIST_SAME_STATE_SKIP_RENDERING_TRUE_STATE_COUNT,
            facade_eligibility["sameStateSkipRenderingTrueCartesianCount"],
        )
        self.assertEqual(
            FACADE_WHITELIST_STATE_POLICY,
            facade_eligibility["stateSchemaPolicy"],
        )
        self.assertEqual(
            FACADE_WHITELIST_STATE_CLASSIFICATION_POLICY,
            facade_eligibility["stateClassificationPolicy"],
        )
        self.assertEqual(
            [
                {
                    "blockId": block_id,
                    "properties": properties,
                    "blockstateSha256": FACADE_WHITELIST_BLOCKSTATE_SHA256[
                        block_id
                    ],
                }
                for block_id, properties in FACADE_WHITELIST_STATE_SCHEMAS.items()
            ],
            facade_eligibility["stateSchemas"],
        )
        self.assertEqual(
            ["down", "up", "north", "south", "west", "east"],
            FACADE_WHITELIST_STATE_SCHEMAS["minecraft:dispenser"]["facing"],
        )
        self.assertEqual(
            [
                "down_east", "down_north", "down_south", "down_west",
                "up_east", "up_north", "up_south", "up_west",
                "west_up", "east_up", "north_up", "south_up",
            ],
            FACADE_WHITELIST_STATE_SCHEMAS["minecraft:crafter"]["orientation"],
        )
        self.assertEqual(
            {}, FACADE_WHITELIST_STATE_SCHEMAS["minecraft:honey_block"]
        )
        self.assertEqual(
            "780ffcffff91d90efe172f2f1f200a06dcbe885fe5316d16bebf72bae2ef7c44",
            FACADE_WHITELIST_BLOCKSTATE_SHA256["minecraft:honey_block"],
        )
        self.assertEqual(
            [
                "ae2:quartz_glass",
                "ae2:quartz_vibrant_glass",
                "minecraft:honey_block",
            ],
            [
                entry["blockId"]
                for entry in FACADE_WHITELIST_NEUTRAL_STATES
                if not entry["solidRender"]
            ],
        )
        self.assertTrue(
            next(
                entry for entry in FACADE_WHITELIST_NEUTRAL_STATES
                if entry["blockId"] == "minecraft:soul_sand"
            )["solidRender"]
        )
        self.assertEqual(
            [
                "ae2:quartz_glass",
                "ae2:quartz_vibrant_glass",
                "minecraft:honey_block",
            ],
            [
                block_id
                for block_id, skip_rendering in (
                    FACADE_WHITELIST_SAME_STATE_SKIP_RENDERING.items()
                )
                if skip_rendering
            ],
        )
        self.assertEqual(
            [
                {"blockId": block_id, "skipRendering": skip_rendering}
                for block_id, skip_rendering in (
                    FACADE_WHITELIST_SAME_STATE_SKIP_RENDERING.items()
                )
            ],
            facade_eligibility["sameStateSkipRendering"],
        )
        self.assertEqual(
            FACADE_ORDINARY_SKIP_RENDERING_CONTROLS,
            {
                entry["blockId"]: entry["skipRendering"]
                for entry in facade_eligibility["ordinarySkipRenderingControls"]
            },
        )
        self.assertEqual(
            FACADE_SKIP_RENDERING_POLICY,
            facade_eligibility["skipRenderingPolicy"],
        )
        self.assertEqual(
            FACADE_QUARTZ_SKIP_RENDERING_POLICY,
            facade_eligibility["quartzSkipRenderingPolicy"],
        )
        self.assertEqual(list(NATIVE_FACADE_NEUTRAL_MATERIALS),
                         facade_eligibility["nativeNeutralMaterials"])
        self.assertEqual(
            FACADE_ORDINARY_MATERIAL_POLICY,
            facade_eligibility["ordinaryMaterialPolicy"],
        )
        self.assertEqual(
            FACADE_TINT_POLICY,
            facade_eligibility["tintPolicy"],
        )
        self.assertEqual(
            FACADE_UV_REINTERPOLATION_POLICY,
            facade_eligibility["uvReinterpolationPolicy"],
        )
        self.assertEqual(
            FACADE_CARDINAL_VARIANT_TRANSFORM_POLICY,
            facade_eligibility["cardinalVariantTransformPolicy"],
        )
        self.assertEqual(
            FACADE_WEIGHTED_VARIANT_POLICY,
            facade_eligibility["weightedVariantPolicy"],
        )
        neutral_by_id = {
            value["blockId"]: value for value in NATIVE_FACADE_NEUTRAL_MATERIALS
        }
        self.assertEqual(
            {"formed": "false", "powered": "false"},
            neutral_by_id["ae2:1k_crafting_storage"]["properties"],
        )
        self.assertEqual(
            {
                "facing": "preserve",
                "formed": "false",
                "powered": "false",
                "spin": "0",
            },
            neutral_by_id["ae2:crafting_monitor"]["statePolicy"]
            ["normalization"],
        )
        self.assertEqual(
            0,
            neutral_by_id["ae2:quartz_vibrant_glass"]
            ["facadeQuadLightEmission"],
        )
        self.assertEqual(
            "source-shade-bit-semantic-locked-host-prbm-has-no-per-quad-shade-channel",
            FACADE_DIRECTIONAL_SHADE_POLICY,
        )
        self.assertEqual(
            FACADE_SUPPORT_SET_POLICY,
            facade_eligibility["supportSetPolicy"],
        )
        self.assertEqual(
            NATIVE_FACADE_NEUTRAL_SCOPE,
            facade_eligibility["nativeNeutralMaterialScope"],
        )
        self.assertEqual(
            "accepted-not-a-fallback-reason",
            generated["renderPolicy"]["facades"]["sourceParityGolden"]
            ["shadeFalseEligibility"],
        )
        self.assertEqual(
            FACADE_TINT_POLICY,
            generated["renderPolicy"]["facades"]["sourceParityGolden"]
            ["quadTint"],
        )
        self.assertEqual(
            LIVE_MODEL_SEMANTIC_SIGNATURE_SHA256,
            generated["resourceClosure"]["liveSemanticGate"]["modelSha256"],
        )
        self.assertEqual(
            LIVE_TEXTURE_SEMANTIC_SIGNATURE_SHA256,
            generated["resourceClosure"]["liveSemanticGate"]["textureSha256"],
        )
        quartz_dependency = generated["renderPolicy"]["facades"][
            "quartzGlassDependency"
        ]
        self.assertEqual(
            QUARTZ_FACADE_DEPENDENCY_TEXTURE_COUNT,
            quartz_dependency["textureCount"],
        )
        self.assertEqual(
            QUARTZ_FACADE_DEPENDENCY_TEXTURE_SEMANTIC_SIGNATURE_SHA256,
            quartz_dependency["textureSemanticSha256"],
        )
        self.assertEqual(
            "not-a-GlassState-input",
            quartz_dependency["glassState"]["sameCableBusOtherFacade"],
        )

    def test_exact_pinned_extension_grid_node_fallback_catalog(self):
        self.assertEqual(67, UNSUPPORTED_COMPATIBLE_ENDPOINT_COUNT)
        self.assertEqual(67, len(UNSUPPORTED_COMPATIBLE_ENDPOINTS))
        self.assertEqual(
            [24, 11, 12, 20],
            [
                value["endpointCount"]
                for value in UNSUPPORTED_COMPATIBLE_ENDPOINT_ARTIFACTS
            ],
        )
        artifacts = {
            value["id"]: value for value in UNSUPPORTED_COMPATIBLE_ENDPOINT_ARTIFACTS
        }
        self.assertEqual(
            4_791_255,
            artifacts["advanced_ae-1.6.12-1.21.1"]["sizeBytes"],
        )
        self.assertEqual(
            "a01d9718667ac13899013e91c5b0b7708b9b9db1da9b8e380772dde54bbe8f41",
            artifacts["advanced_ae-1.6.12-1.21.1"]["sha256"],
        )
        self.assertEqual(
            5_578_031,
            artifacts["extendedae-1.21-2.2.35-neoforge"]["sizeBytes"],
        )
        self.assertEqual(
            {
                "tag": "1.21-2.2.35-neoforge",
                "commit": "3776bc854458301bbcc9a44a8238d70a0e3dc00d",
            },
            artifacts["extendedae-1.21-2.2.35-neoforge"]["source"],
        )
        self.assertEqual(
            "expandedae:exp_pattern_provider",
            UNSUPPORTED_COMPATIBLE_ENDPOINTS[0]["id"],
        )
        self.assertEqual(
            "extendedae:wireless_hub",
            UNSUPPORTED_COMPATIBLE_ENDPOINTS[-1]["id"],
        )
        by_id = {value["id"]: value for value in UNSUPPORTED_COMPATIBLE_ENDPOINTS}
        self.assertEqual(
            "megacells:mega_crafting_storage",
            by_id["megacells:256m_crafting_storage"]["blockEntityId"],
        )
        self.assertEqual(
            "advanced_ae:quantum_craft",
            by_id["advanced_ae:quantum_crafter"]["blockEntityId"],
        )
        generated = profile(
            (self.route_root() / "required-resources.sha256").read_bytes(),
            (self.route_root() / "required-resources.tsv").read_bytes(),
        )
        self.assertEqual(
            {
                "name": "All the Mons",
                "version": "1.2.0",
                "commit": "c7bb230f21d14d26859d0b92548f089b3a493ad9",
            },
            generated["pack"],
        )
        self.assertEqual("21.1.248", generated["neoforge"])
        self.assertEqual(
            list(UNSUPPORTED_COMPATIBLE_ENDPOINTS),
            generated["knownUnsupportedCompatibleEndpoints"]["entries"],
        )
        self.assertEqual(
            KNOWN_EXTENSION_FALLBACK_CONTROL,
            generated["knownUnsupportedCompatibleEndpoints"]
            ["representativeControl"],
        )
        self.assertEqual(
            {
                "malformedNativeEndpoint": MALFORMED_NATIVE_ENDPOINT_POLICY,
                "knownExtensionExactBlockAndBlockEntity": (
                    KNOWN_UNSUPPORTED_COMPATIBLE_ENDPOINT_POLICY
                ),
                "knownExtensionBlockWithMissingOrWrongBlockEntity": (
                    MALFORMED_KNOWN_EXTENSION_ENDPOINT_POLICY
                ),
                "unrelatedBlockWithCatalogBlockEntity": "disconnected",
                "unrelatedNonNativeBlockEntity": "disconnected",
            },
            generated["endpointSidePolicy"]["branchPolicies"],
        )
        facade_golden = generated["renderPolicy"]["facades"][
            "sourceParityGolden"
        ]
        self.assertEqual(0.968, facade_golden["thinThicknessSixteenths"])
        self.assertEqual(
            FACADE_UV_REINTERPOLATION_POLICY,
            facade_golden["uvAfterClamp"],
        )
        self.assertEqual(
            FACADE_WEIGHTED_VARIANT_POLICY,
            facade_golden["weightedVariants"],
        )
        self.assertEqual(
            {"unit": "block", "value": FACADE_CORNER_KICK_SOURCE_EPSILON_BLOCKS},
            facade_golden["cornerKickSourceEpsilon"],
        )
        self.assertEqual(
            {
                "unit": "sixteenth",
                "value": FACADE_CORNER_KICK_RUNTIME_EPSILON_SIXTEENTHS,
            },
            facade_golden["cornerKickRuntimeEpsilon"],
        )
        self.assertEqual(
            {
                "unit": "block",
                "value": FACADE_CORNER_KICK_ANALYZER_EPSILON_BLOCKS,
            },
            facade_golden["cornerKickAnalyzerEpsilon"],
        )
        self.assertEqual(
            FACADE_CUTOUT_STRIP_AABB_POLICY,
            facade_golden["cutoutStripAabbNormalization"],
        )
        self.assertEqual(
            FACADE_CARDINAL_VARIANT_TRANSFORM_POLICY,
            facade_golden["cardinalVariantTransform"],
        )
        self.assertEqual(
            FACADE_AMBIENT_OCCLUSION_DIRECTION_POLICY,
            facade_golden["ambientOcclusionDirection"],
        )
        self.assertEqual(
            STRUCTURAL_MAP_COLOR_ILLUMINATION_POLICY,
            facade_golden["mapColorIllumination"],
        )
        self.assertEqual(
            STRUCTURAL_MAP_COLOR_ILLUMINATION_POLICY,
            generated["renderPolicy"]["mapColorIllumination"],
        )
        self.assertEqual(
            FACADE_CUTOUT_STRIP_AABB_POLICY,
            generated["renderPolicy"]["facadeCutoutCollision"]
            ["stripAabbConstruction"],
        )

    def test_direct_model_catalog_and_generated_manifests_are_exact(self):
        self.assertEqual(DIRECT_NEUTRAL_RESOURCE_COUNT, len(direct_model_roots()))
        root = self.route_root()
        checksums = (root / "required-resources.sha256").read_bytes()
        sizes = (root / "required-resources.tsv").read_bytes()
        checksum_values = parse_checksum_manifest(checksums)
        size_values = parse_size_manifest(sizes)
        self.assertEqual(REQUIRED_RESOURCE_COUNT, len(checksum_values))
        self.assertEqual(set(checksum_values), set(size_values))
        self.assertEqual(
            REQUIRED_RESOURCE_BYTES,
            sum(size for size, _ in size_values.values()),
        )
        self.assertEqual(
            EXPECTED_RESOURCE_MANIFEST_SHA256,
            hashlib.sha256(checksums).hexdigest(),
        )
        self.assertEqual(
            EXPECTED_RESOURCE_SIZES_MANIFEST_SHA256,
            hashlib.sha256(sizes).hexdigest(),
        )

    def test_generated_profile_and_every_frozen_output_are_exact(self):
        root = self.route_root()
        checksums = (root / "required-resources.sha256").read_bytes()
        sizes = (root / "required-resources.tsv").read_bytes()
        actual = (root / "profile.json").read_bytes()
        self.assertEqual(profile_bytes(checksums, sizes), actual)
        self.assertEqual(profile(checksums, sizes), json.loads(actual))
        self.assertEqual(EXPECTED_PROFILE_SHA256, hashlib.sha256(actual).hexdigest())
        self.assertEqual(
            ["M0", "M1", "M2", "M3a", "M3b", "M3c", "M3d", "M3e", "M3f"],
            [value["milestone"] for value in ACCEPTED_ARTIFACTS],
        )
        self.assertEqual(
            [3, 4, 5, 6, 7, 8, 9],
            [value["schemaVersion"] for value in ACCEPTED_GALLERY_SCHEMAS],
        )
        for relative, expected in EXPECTED_FROZEN_OUTPUT_SHA256.items():
            self.assertEqual(
                expected,
                hashlib.sha256((ROOT / relative).read_bytes()).hexdigest(),
                relative,
            )

    def test_manifest_parsers_fail_closed(self):
        digest_a = "a" * 64
        digest_b = "b" * 64
        self.assertEqual(
            {"a": digest_a, "b": digest_b},
            parse_checksum_manifest(
                f"{digest_a}  a\n{digest_b}  b\n".encode()
            ),
        )
        self.assertEqual(
            {"a": (1, digest_a), "b": (2, digest_b)},
            parse_size_manifest(
                f"a\t1\t{digest_a}\nb\t2\t{digest_b}\n".encode()
            ),
        )
        for parser, malformed in (
            (parse_checksum_manifest, b"not-a-row\n"),
            (parse_checksum_manifest, f"{digest_a}  b\n{digest_b}  a\n".encode()),
            (parse_size_manifest, f"a\t0\t{digest_a}\n".encode()),
            (parse_size_manifest, f"a\t1\t{'g' * 64}\n".encode()),
        ):
            with self.subTest(parser=parser.__name__, malformed=malformed):
                with self.assertRaises(ValueError):
                    parser(malformed)


if __name__ == "__main__":
    unittest.main()
