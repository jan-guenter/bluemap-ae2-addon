#!/usr/bin/env python3
# SPDX-License-Identifier: LGPL-3.0-only
"""Deterministic exact-AE2-19.2.17 remaining-M3 route contract."""

from __future__ import annotations

import hashlib
import json
import zipfile


VERSION = "19.2.17"
EXPECTED_SIZE = 8_230_896
EXPECTED_SHA1 = "49c18d6a4af487957d7e5a6ad5dcbf71090b8e14"
EXPECTED_SHA256 = (
    "460d779a0609b81409907d9956de8f6f70a1b0912257e3e5c3c7e75ac9630e95"
)
EXPECTED_SHA512 = (
    "55edfd948366aff620881e0625e48c333a2cb847e73249bc0b588efbc4b867099"
    "92a8ffbca97ea387e270df4186fe7f74ee2f27b739f1c952e932becfb9dea33"
)

SOURCE_COMMIT = "79ee2c704ad62941a426c26b1cb1f76ef5b2ee5a"
EXPECTED_SOURCES_SIZE = 3_814_167
EXPECTED_SOURCES_SHA1 = "260a230e9fa1b4885489f7f58b30c19226093a52"
EXPECTED_SOURCES_SHA256 = (
    "d2f451203cb61c2d21fae52c683083d2f72441ca7d26725f4df5934290492e6a"
)
EXPECTED_SOURCES_SHA512 = (
    "c7feaf57a7f56b76dc019519b84cf4e0718cc334827c0497f17dd65921e723a2b"
    "98f9a072807a46f2c7e3431be71eeec3bdea9a990b661076af849e3d7ef2c34"
)

PROFILE_ID = "ae2-m3-completion"
SPATIAL_PYLON_COMPONENT_MAX_BLOCKS = 256
SPATIAL_PYLON_AMBIGUOUS_POLICY = (
    "bounded-locally-invalid-component-unformed-base-plus-dim"
)
SPATIAL_PYLON_INCOMPLETE_COMPONENT_POLICY = "atomic-original-resource-fallback"
SUPPORTED_BLOCKS = (
    ("ae2:paint", "persisted-splotches"),
    ("ae2:sky_stone_chest", "static-closed-stone"),
    ("ae2:smooth_sky_stone_chest", "static-closed-smooth"),
    ("ae2:crank", "static-neutral"),
    ("ae2:inscriber", "static-neutral-structural"),
    ("ae2:spatial_pylon", "local-topology-static-offline-unknown"),
)
SYNTHETIC_BLOCK_STATES = {
    "paint": "bluemap_ae2:paint",
    "skyStoneChest": "bluemap_ae2:sky_stone_chest",
    "crank": "bluemap_ae2:crank",
    "inscriber": "bluemap_ae2:inscriber",
    "spatialPylon": "bluemap_ae2:spatial_pylon",
}
BLOCK_ENTITY_IDS = {
    "paint": "ae2:paint",
    "skyStoneChest": "ae2:sky_chest",
    "crank": "ae2:crank",
    "inscriber": "ae2:inscriber",
    "spatialPylon": "ae2:spatial_pylon",
}

SOURCE_TEXTURES = (
    "ae2:block/crank",
    "ae2:block/inscriber",
    "ae2:block/inscriber_inside",
    "ae2:block/paint1",
    "ae2:block/paint2",
    "ae2:block/paint3",
    "ae2:block/skyblockchest",
    "ae2:block/skychest",
    "ae2:block/spatial_pylon/base",
    "ae2:block/spatial_pylon/base_end",
    "ae2:block/spatial_pylon/base_spanned",
    "ae2:block/spatial_pylon/dim",
    "ae2:block/spatial_pylon/dim_end",
    "ae2:block/spatial_pylon/dim_spanned",
    "ae2:block/spatial_pylon/red",
    "ae2:block/spatial_pylon/red_end",
    "ae2:block/spatial_pylon/red_spanned",
)
EMITTED_STATIC_TEXTURES = (
    "ae2:block/crank",
    "ae2:block/inscriber",
    "ae2:block/inscriber_inside",
    "ae2:block/paint1",
    "ae2:block/paint2",
    "ae2:block/paint3",
    "ae2:block/skyblockchest",
    "ae2:block/skychest",
    "ae2:block/spatial_pylon/base",
    "ae2:block/spatial_pylon/base_end",
    "ae2:block/spatial_pylon/base_spanned",
    "ae2:block/spatial_pylon/dim",
    "ae2:block/spatial_pylon/red",
    "ae2:block/spatial_pylon/red_end",
    "ae2:block/spatial_pylon/red_spanned",
)
FALLBACK_ONLY_TEXTURES = (
    "ae2:block/sky_stone_block",
    "ae2:block/smooth_sky_stone_block",
)

EXPECTED_RESOURCE_MANIFEST_SHA256 = (
    "3faf7f29e2878f5525541bad855cbc66b6d45786dc8fc6ee29a6fbbf4878cca1"
)
EXPECTED_SOURCE_TEXTURE_MANIFEST_SHA256 = (
    "030ebfafeeef07005946fcf5abf7b28365ec02001273b4bedcaa26b41f8de395"
)
EXPECTED_EMITTED_STATIC_TEXTURE_MANIFEST_SHA256 = (
    "4652a3110adac720845b559b990dabd32e55887d43bc113f85856052bd0a8a05"
)
EXPECTED_FALLBACK_TEXTURE_MANIFEST_SHA256 = (
    "aaff6681328dfc441a01f5a014182e914a82598395b7a594809b4652281a1146"
)
EXPECTED_PROFILE_SHA256 = (
    "281a335d3024ebbb97c6268e768826c467d6f7ea660989fd3dae204c6c03abf3"
)

EXPECTED_FROZEN_OUTPUT_SHA256 = {
    "src/main/resources/bluemap-ae2/profiles/ae2/19.2.17/profile.json": (
        "2c27976a718834dbc97b3eb7cac6543c4ad2a966737c7bccbadb2b1c39c837e8"
    ),
    "src/main/resources/bluemap-ae2/profiles/ae2/19.2.17/required-resources.sha256": (
        "408297def444f1392b7b87fdc4b8520099513b4c57c63a4176b808ce61b4e1be"
    ),
    (
        "src/main/resources/bluemap-ae2/profiles/ae2/19.2.17/routes/"
        "quartz-glass/profile.json"
    ): "548e5bc00ef07c6d6b93b346422b596882ec11ca03de006065fa45fecb991200",
    (
        "src/main/resources/bluemap-ae2/profiles/ae2/19.2.17/routes/"
        "quartz-glass/required-resources.tsv"
    ): "b51c708e7c4d26093c1b6f85b88d0be50572d3cfa76dbf802720f6ad79c7a7fa",
    (
        "src/main/resources/bluemap-ae2/profiles/ae2/19.2.17/routes/"
        "crafting/profile.json"
    ): "676f63473b4827bb952a7c1f3fb457a6a03c3bfd0fb4b29a122b9f57468ba0f7",
    (
        "src/main/resources/bluemap-ae2/profiles/ae2/19.2.17/routes/"
        "crafting/required-resources.tsv"
    ): "dc474ba6ce7c4c2d53778827b1c1f9b4994594ea984ed7a2cbd62c40e1bc1183",
    (
        "src/main/resources/bluemap-ae2/profiles/ae2/19.2.17/routes/"
        "quantum-bridge/profile.json"
    ): "21afa152e3f56d8bdde9f602748c0efbca52a2c55d5dd7a836adca267c65480e",
    (
        "src/main/resources/bluemap-ae2/profiles/ae2/19.2.17/routes/"
        "quantum-bridge/required-resources.tsv"
    ): "717eed1ada75fb43c1324792c147cd8c2308d8c73ee82bf52d8de6bad4f74ed9",
    (
        "src/main/resources/bluemap-ae2/profiles/extendedae/"
        "1.21-2.2.33-neoforge/profile.json"
    ): "eab467f46c27974e1f7d54fe749366b92eacd8d63d57bad1e8f3e452d82ad1df",
    (
        "src/main/resources/bluemap-ae2/profiles/extendedae/"
        "1.21-2.2.33-neoforge/required-resources.tsv"
    ): "5e72f79f45a3b120a89cf8b7a1fa15ce41bebaae62a63c6f3305ef40bd5d24ee",
}

# Sorted path, uncompressed byte size and SHA-256 from the exact runtime JAR.
EXPECTED_RESOURCES = (
    ("assets/ae2/blockstates/crank.json", 73,
     "b84f70dfcc8157059a5bc2ae9723dab4ad38a8b1057fea6feb564e0617145b61"),
    ("assets/ae2/blockstates/inscriber.json", 2416,
     "4ec6c21834e68f179c252bf22aeb8f8f67d57ef057eb8bde57f65f576e0885f2"),
    ("assets/ae2/blockstates/paint.json", 72,
     "ca68a31445ba5e647e216cc4a90434171b4686f53c866e1600eed27228263afc"),
    ("assets/ae2/blockstates/sky_stone_chest.json", 181,
     "e7fa4d5b7a79b46ae4ede289028c91901f124d3690c75012856dcd28d7ffe948"),
    ("assets/ae2/blockstates/smooth_sky_stone_chest.json", 188,
     "69ddcea6ca145c657b3a97fd13d603736acd76f9b08a77670bed3d25f3050e86"),
    ("assets/ae2/blockstates/spatial_pylon.json", 80,
     "a3c18208840e313823afc7198e8d74da9b1e65e78dffdc6327f53d2b70e678c9"),
    ("assets/ae2/models/block/crank.json", 1525,
     "c541206d4522bec58cb7ff8a02a569ccd40357aa02fd9931bd5ce109ff8d440a"),
    ("assets/ae2/models/block/crank_base.json", 750,
     "07fabbc0f4ef73be66c504a91903feef7e828063a654e226fa0f4973257cbbd8"),
    ("assets/ae2/models/block/crank_handle.json", 1161,
     "f5a736d959b9fd3a86fd0aea9ab26aa73f58da71f543875f9c1b473f2aa6e5df"),
    ("assets/ae2/models/block/inscriber.json", 4718,
     "9a4d6d44c4c72ee961349958980b49c767c659b6ec7e130cb018687575bd3025"),
    ("assets/ae2/models/block/paint.json", 2,
     "44136fa355b3678a1146ad16f7e8649e94fb4fc21fe77e8310c060f61caaff8a"),
    ("assets/ae2/models/block/sky_stone_block.json", 102,
     "11937150d3305ec2d9925bf1c70e90ba971ef0c8af1b51360259f13eee0b3fb6"),
    ("assets/ae2/models/block/smooth_sky_stone_block.json", 110,
     "da07f0dc6b19316cd4c82bd867c51c802415553813c31472e93050a3c649b5b0"),
    ("assets/ae2/models/block/spatial_pylon.json", 2,
     "44136fa355b3678a1146ad16f7e8649e94fb4fc21fe77e8310c060f61caaff8a"),
    ("assets/ae2/textures/block/crank.png", 484,
     "74d6ee5de730fe3f5bef55c6cd34be1ae259b9b57e12721366f624568be5d387"),
    ("assets/ae2/textures/block/inscriber.png", 1838,
     "3325132b305c801b9c7ffe13bc9053a17693b863da1838f1e475a48f5cbfb9ff"),
    ("assets/ae2/textures/block/inscriber_inside.png", 295,
     "24bb72ed36dda2354852168e5cc1dcec7064bc9cb30387065c43e1bd68c41c50"),
    ("assets/ae2/textures/block/paint1.png", 330,
     "98925a6daa0864457d036039bdd1019a0a0128e2b986903b61353756d2b803cc"),
    ("assets/ae2/textures/block/paint2.png", 352,
     "0fce6817933305c86c45a744a44567ec678f7112d4bcd7663611bbb8c62ccb96"),
    ("assets/ae2/textures/block/paint3.png", 285,
     "745fb40ed1aac35986844eb79b50f869b46a8b682df9bde36f7dcc5f99b136d6"),
    ("assets/ae2/textures/block/sky_stone_block.png", 392,
     "512fd9b4234502f1e867d7b4793ee3f60415be23d48e0cd985c4468d55433736"),
    ("assets/ae2/textures/block/skyblockchest.png", 2351,
     "92f4e462ebe6eb28da240eb57821a131386c764ab8a82c95da207ce42da64296"),
    ("assets/ae2/textures/block/skychest.png", 2391,
     "7bc73f73667e9a6fdb764862344dbf6523ac45f4f4778b7395859d381e387a1f"),
    ("assets/ae2/textures/block/smooth_sky_stone_block.png", 336,
     "e3c5a3c3e4245d6fb97930915c3a7b3f89b5da1e8be614be7ee07daaa2617653"),
    ("assets/ae2/textures/block/spatial_pylon/base.png", 277,
     "6599f956b8369668d7b246ffd161b6db2be9c857bf9e1d69a97075288c5ec189"),
    ("assets/ae2/textures/block/spatial_pylon/base_end.png", 255,
     "57ebeb67d886dd53670d6c1db32d90000c3c340147e16ac3ec093762b224f090"),
    ("assets/ae2/textures/block/spatial_pylon/base_spanned.png", 221,
     "e14d59f51e2334a24b167d88478d2a725377ce25bc042c5e39629e58f3d98d7a"),
    ("assets/ae2/textures/block/spatial_pylon/dim.png", 264,
     "443ac34e23888c7005727c114e3023f74ead431d46d7413d07ace9282b8bbb3e"),
    ("assets/ae2/textures/block/spatial_pylon/dim_end.png", 227,
     "fa6aa2ce0c2e5ec127d256841688c332a96eee40f5a98b282f2dd82dea4cadab"),
    ("assets/ae2/textures/block/spatial_pylon/dim_spanned.png", 154,
     "4464a057d0c2d09506ce31b4ca149c9379128f21dd5dc587eb31f64bfdac6a0a"),
    ("assets/ae2/textures/block/spatial_pylon/red.png", 273,
     "ebfa5a0ace58d417597128656c5b163fb9ccab6f5985380822ed3efc5071cb48"),
    ("assets/ae2/textures/block/spatial_pylon/red_end.png", 232,
     "da445eff70d22f8b01b08ecd403e59f9e9789f33452bf9beb680bb7d869d9e8b"),
    ("assets/ae2/textures/block/spatial_pylon/red_spanned.png", 154,
     "088c55ad7d3abfbe50828c9878d04ad4d06af02b3babeb5d28c6d198cdb62f89"),
)


def texture_resource_path(texture: str) -> str:
    namespace, path = texture.split(":", 1)
    return f"assets/{namespace}/textures/{path}.png"


def expected_resource_paths() -> list[str]:
    paths = [path for path, _, _ in EXPECTED_RESOURCES]
    if len(paths) != 33 or len(set(paths)) != 33 or paths != sorted(paths):
        raise ValueError("M3-completion resource contract is not 33 sorted paths")
    return paths


def expected_manifest() -> bytes:
    return "".join(
        f"{path}\t{size}\t{digest}\n"
        for path, size, digest in EXPECTED_RESOURCES
    ).encode("utf-8")


def resource_manifest(archive: zipfile.ZipFile) -> bytes:
    names = [entry.filename for entry in archive.infolist()]
    if len(names) != len(set(names)):
        raise ValueError("artifact contains duplicate ZIP entry names")
    for path, expected_size, expected_digest in EXPECTED_RESOURCES:
        if path not in names:
            raise ValueError(f"artifact is missing required resource: {path}")
        if archive.getinfo(path).file_size != expected_size:
            raise ValueError(f"required resource size changed: {path}")
        actual_digest = hashlib.sha256(archive.read(path)).hexdigest()
        if actual_digest != expected_digest:
            raise ValueError(f"required resource SHA-256 changed: {path}")
    return expected_manifest()


def parse_resource_manifest(raw: bytes) -> dict[str, tuple[int, str]]:
    resources: dict[str, tuple[int, str]] = {}
    for line in raw.decode("utf-8").splitlines():
        fields = line.split("\t")
        if len(fields) != 3:
            raise ValueError("resource manifest contains a malformed row")
        path, size_text, digest = fields
        if (
            not path
            or not size_text.isascii()
            or not size_text.isdecimal()
            or size_text.startswith("0")
            or len(digest) != 64
            or any(character not in "0123456789abcdef" for character in digest)
        ):
            raise ValueError("resource manifest contains a malformed row")
        if path in resources:
            raise ValueError(f"duplicate resource manifest path: {path}")
        resources[path] = (int(size_text), digest)
    if list(resources) != sorted(resources):
        raise ValueError("resource manifest is not sorted by path")
    return resources


def texture_manifest(required_manifest: bytes, textures: tuple[str, ...]) -> bytes:
    resources = parse_resource_manifest(required_manifest)
    paths = {texture_resource_path(texture) for texture in textures}
    if not paths.issubset(resources):
        raise ValueError("texture partition is outside the exact resource closure")
    return b"".join(
        f"{path}\t{size}\t{digest}\n".encode("utf-8")
        for path, (size, digest) in resources.items()
        if path in paths
    )


def profile(required_manifest: bytes) -> dict[str, object]:
    resources = parse_resource_manifest(required_manifest)
    expected = {path: (size, digest) for path, size, digest in EXPECTED_RESOURCES}
    if resources != expected:
        raise ValueError("M3-completion manifest does not match exact resource rows")
    return {
        "schemaVersion": 1,
        "profileId": PROFILE_ID,
        "modId": "ae2",
        "version": VERSION,
        "artifact": "appliedenergistics2-19.2.17.jar",
        "sizeBytes": EXPECTED_SIZE,
        "sha1": EXPECTED_SHA1,
        "sha256": EXPECTED_SHA256,
        "sha512": EXPECTED_SHA512,
        "source": {
            "tag": "neoforge/v19.2.17",
            "commit": SOURCE_COMMIT,
            "artifact": "appliedenergistics2-19.2.17-sources.jar",
            "sizeBytes": EXPECTED_SOURCES_SIZE,
            "sha1": EXPECTED_SOURCES_SHA1,
            "sha256": EXPECTED_SOURCES_SHA256,
            "sha512": EXPECTED_SOURCES_SHA512,
        },
        "minecraft": "1.21.1",
        "neoforge": "21.1.234",
        "coverageMilestone": "M3f",
        "buildAcceptance": "local-build-candidate",
        "runtimeAcceptance": "technical-lifecycle-pending",
        "humanAcceptance": "pending",
        "supportedBlocks": [
            {"id": block_id, "projection": projection}
            for block_id, projection in SUPPORTED_BLOCKS
        ],
        "syntheticBlockStates": SYNTHETIC_BLOCK_STATES,
        "blockEntityIds": BLOCK_ENTITY_IDS,
        "persistedState": {
            "paint": {
                "blockProperties": {
                    "facing": "required-six-way-direction",
                    "light_level": "required-integer-0-through-2",
                },
                "blockEntity": {
                    "requiredId": "ae2:paint",
                    "dots": "required-bounded-byte-array",
                    "encoding": "count-byte-then-two-bytes-per-splotch",
                    "acceptedCount": "1-through-21",
                    "positionByte": "x-low-nibble-y-high-nibble-divided-by-15",
                    "valueByte": (
                        "direction-low-3-bits-color-next-4-bits-lumen-high-bit"
                    ),
                    "acceptedDirectionOrdinals": "0-through-5",
                    "acceptedColorOrdinals": "0-through-15",
                    "malformed": "atomic-original-resource-fallback",
                },
            },
            "skyStoneChest": {
                "blockProperties": {
                    "facing": "required-horizontal-direction",
                    "waterlogged": "required-boolean",
                },
                "blockEntity": {
                    "requiredId": "ae2:sky_chest",
                    "inventory": "ignored",
                    "openers": "transient-excluded",
                },
            },
            "crank": {
                "blockProperties": {"facing": "required-six-way-direction"},
                "blockEntity": {
                    "requiredId": "ae2:crank",
                    "rotation": "transient-stream-excluded-neutral-zero",
                },
            },
            "inscriber": {
                "blockProperties": {
                    "facing": "required-six-way-direction",
                    "spin": "required-integer-0-through-3",
                    "waterlogged": "required-boolean",
                },
                "blockEntity": {
                    "requiredId": "ae2:inscriber",
                    "inventory": "ignored",
                    "processingAndSmash": "excluded-neutral-zero",
                },
            },
            "spatialPylon": {
                "blockProperties": {"powered_on": "accepted-but-ignored"},
                "blockEntity": {
                    "requiredId": "ae2:spatial_pylon",
                    "ordinarySavedRenderFields": [],
                    "visualStateOnly": ["powered", "online", "axisPosition", "axis"],
                },
            },
        },
        "topologyPolicy": {
            "spatialPylon": {
                "evidence": "uncached-native-axis-line-scan",
                "maximumAxisLinePylons": SPATIAL_PYLON_COMPONENT_MAX_BLOCKS,
                "none": "isolated-base-plus-dim",
                "start": "one-positive-axis-neighbor",
                "end": "one-negative-axis-neighbor",
                "middle": "two-opposite-same-axis-neighbors",
                "formedAxisCaps": "outer-base-inner-red",
                "formedEndpointLateralFaces": "outer-base-end-inner-red-end",
                "formedMiddleLateralFaces": (
                    "outer-base-spanned-inner-red-spanned"
                ),
                "uvPolicy": "exact-source-axis-role-rotations-and-v-flips",
                "boundedLocallyInvalidComponent": SPATIAL_PYLON_AMBIGUOUS_POLICY,
                "missingMalformedOrCapped": (
                    SPATIAL_PYLON_INCOMPLETE_COMPONENT_POLICY
                ),
                "globalClusterValidity": "not-claimed-beyond-bounded-local-component",
                "onlineAndPower": "not-claimed-static-offline-unknown",
                "extensionInteraction": "native-block-id-only-no-connector-claim",
            }
        },
        "renderPolicy": {
            "paint": {
                "layer": "cutout",
                "ambientOcclusion": False,
                "light": "host-block-state-with-emissive-lumen-quads",
                "spriteSelection": "abs-position-plus-value-modulo-3",
                "orderedFaceOffset": "0.001-per-splotch-starting-at-0.001",
                "trianglesPerSplotch": 2,
                "maximumSplotches": 21,
                "maximumTriangles": 42,
            },
            "skyStoneChest": {
                "layer": "entity-cutout-equivalent",
                "pose": "closed",
                "orientation": "horizontal-front-negative-yaw-about-center",
                "inventory": "excluded",
                "light": "world-derived",
                "trianglesPerChest": 36,
            },
            "crank": {
                "layer": "cutout",
                "pose": "neutral-zero-degrees",
                "orientation": "full-source-block-orientation-about-center",
                "ambientOcclusion": True,
                "light": "world-derived",
                "triangles": 34,
            },
            "inscriber": {
                "casingLayer": "stock-model",
                "stampLayer": "solid",
                "pose": "neutral-progress-zero",
                "orientation": "full-source-block-orientation-about-center",
                "itemsAndAnimation": "excluded",
                "light": "world-derived",
                "casingTrianglesBeforeNeighborCulling": 66,
                "stampTriangles": 12,
                "maximumTriangles": 78,
            },
            "spatialPylon": {
                "layer": "cutout",
                "ambientOcclusion": False,
                "power": "static-offline-unknown-non-emissive",
                "light": "world-derived",
                "triangles": 24,
            },
            "atomicFallback": "per-block-original-resource-model",
            "particles": "excluded",
            "contentsItemsFluidsAndMachineActivity": "excluded",
        },
        "sourceTextures": list(SOURCE_TEXTURES),
        "emittedStaticTextures": list(EMITTED_STATIC_TEXTURES),
        "fallbackOnlyTextures": list(FALLBACK_ONLY_TEXTURES),
        "resourcePartition": {
            "pathCount": 33,
            "blockstateCount": 6,
            "modelCount": 8,
            "textureCount": 19,
            "sourceTextureCount": 17,
            "emittedStaticTextureCount": 15,
            "fallbackOnlyTextureCount": 2,
            "totalBytes": 22_491,
            "acceptedProfileOverlapCount": 0,
            "familyPaths": {
                "paint": {"count": 5, "bytes": 1_041},
                "spatialPylon": {"count": 11, "bytes": 2_139},
                "skyStoneChest": {"count": 8, "bytes": 6_051},
                "crank": {"count": 5, "bytes": 3_993},
                "inscriber": {"count": 4, "bytes": 9_267},
            },
        },
        "requiredResourcesManifestSha256": EXPECTED_RESOURCE_MANIFEST_SHA256,
        "sourceTextureManifestSha256": EXPECTED_SOURCE_TEXTURE_MANIFEST_SHA256,
        "emittedStaticTextureManifestSha256": (
            EXPECTED_EMITTED_STATIC_TEXTURE_MANIFEST_SHA256
        ),
        "fallbackTextureManifestSha256": (
            EXPECTED_FALLBACK_TEXTURE_MANIFEST_SHA256
        ),
        "limitations": [
            "exact-only-ae2-19.2.17",
            "spatial-pylon-role-is-bounded-local-and-non-authoritative",
            "spatial-pylon-online-power-and-global-validity-beyond-the-bounded-local-component-are-not-claimed",
            "machine-contents-items-fluids-and-activity-are-excluded",
            "sky-chests-are-always-closed",
            "crank-and-inscriber-use-neutral-static-poses",
            "particles-are-excluded",
            "non-native-extension-connectors-are-not-claimed",
        ],
    }


def profile_bytes(required_manifest: bytes) -> bytes:
    return (json.dumps(profile(required_manifest), indent=2) + "\n").encode("utf-8")
