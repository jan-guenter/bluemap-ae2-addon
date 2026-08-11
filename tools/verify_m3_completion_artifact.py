#!/usr/bin/env python3
# SPDX-License-Identifier: LGPL-3.0-only
"""Verify exact AE2 M3f runtime, sources and remaining-M3 route evidence."""

from __future__ import annotations

import argparse
import hashlib
import json
from pathlib import Path
import struct
import sys
import tomllib
import zipfile

from ae2_m3_completion_contract import (
    EMITTED_STATIC_TEXTURES,
    EXPECTED_EMITTED_STATIC_TEXTURE_MANIFEST_SHA256,
    EXPECTED_FALLBACK_TEXTURE_MANIFEST_SHA256,
    EXPECTED_FROZEN_OUTPUT_SHA256,
    EXPECTED_PROFILE_SHA256,
    EXPECTED_RESOURCE_MANIFEST_SHA256,
    EXPECTED_SHA1,
    EXPECTED_SHA256,
    EXPECTED_SHA512,
    EXPECTED_SIZE,
    EXPECTED_SOURCE_TEXTURE_MANIFEST_SHA256,
    EXPECTED_SOURCES_SHA1,
    EXPECTED_SOURCES_SHA256,
    EXPECTED_SOURCES_SHA512,
    EXPECTED_SOURCES_SIZE,
    FALLBACK_ONLY_TEXTURES,
    SOURCE_COMMIT,
    SOURCE_TEXTURES,
    expected_resource_paths,
    parse_resource_manifest,
    profile,
    profile_bytes,
    texture_manifest,
)
from verify_pinned_artifact import verify_metadata


EXPECTED_RUNTIME_CLASS_SHA256 = {
    "appeng/block/paint/PaintSplotches.class": (
        "35508b7a7426201d09df82529621da236db4f63058d9df758be8536e73b73e30"
    ),
    "appeng/block/paint/PaintSplotchesBakedModel.class": (
        "68ad0fe5b6601c9ad0bdb2b98ec9e7dfd39bbf7e5d10d195075a4211378d972c"
    ),
    "appeng/block/paint/PaintSplotchesBlock.class": (
        "2d349a59a7c128371ca8ae1ef85a3ea619900fcfb7d837a514203e2d4b522427"
    ),
    "appeng/block/paint/PaintSplotchesModel.class": (
        "b438964401920ac778df5f17eab86c72568065923613641cd07a5e14519b8674"
    ),
    "appeng/blockentity/misc/PaintSplotchesBlockEntity.class": (
        "7847f9cd2e0dfe6e20efcfa27c9c70d1862bf957b46bd2b2da3d75c2f7b9218d"
    ),
    "appeng/helpers/Splotch.class": (
        "b280f37c4a3b992b6f4962e7748185a175f9bc2638b66ae53bdeb30904270958"
    ),
    "appeng/block/spatial/SpatialPylonBlock.class": (
        "ad426acb5d9e34b179478229f870292ffd01f035385316f6321937775fdd1da4"
    ),
    "appeng/blockentity/spatial/SpatialPylonBlockEntity.class": (
        "b78700ef03ece7d863c655f5f22f9829cba124b73d81c1835197bdc4c477f893"
    ),
    "appeng/client/render/tesr/spatial/SpatialPylonBakedModel.class": (
        "cd212436d4e10ca8f8a36f776965d51ad3b80bdfcf889575e1f3daca52a1d552"
    ),
    "appeng/client/render/tesr/spatial/SpatialPylonModel.class": (
        "0a8abaaad5fdbb4749f30ad078887ef3967f6dba8ee552dfe57a718de6b363d6"
    ),
    "appeng/client/render/tesr/spatial/SpatialPylonTextureType.class": (
        "07d71645f638e1e2a22da325122412b14f7852f7723b8557734c08b64fa03f24"
    ),
    "appeng/me/cluster/implementations/SpatialPylonCalculator.class": (
        "db6a1c7e442ba67fdc1cb4db47fd0b9ddb3e6624fd194ea0fab691ac489dd168"
    ),
    "appeng/me/cluster/implementations/SpatialPylonCluster.class": (
        "c87fdaadf5e8ea963a0bdb798fc31a6e7a644e365fcf537bc3d6fab86aff74d9"
    ),
    "appeng/me/cluster/MBCalculator.class": (
        "a06d61bf4443de04769998a0294127209c578a8dc1a858a441709486c2b07cba"
    ),
    "appeng/me/service/SpatialPylonService.class": (
        "d8d7f78555cfe77ea7cfd2c838cb06f6202a94e9bcca5edacf3c47d39f3e71eb"
    ),
    "appeng/block/storage/SkyChestBlock.class": (
        "59117a5efe4c082dbf9c4e2b4e2c0388eef6293011f9a1b7d9218584ac095a60"
    ),
    "appeng/blockentity/storage/SkyChestBlockEntity.class": (
        "efe4462edfa0b89ca6064e830d2e5f9931de164ef0bd97bfb62af940029c7b7a"
    ),
    "appeng/client/render/tesr/SkyChestTESR.class": (
        "6ed715db1e9009ff2312e4fc863a6fd2adb525736231e322937d52e181fef3ab"
    ),
    "appeng/block/misc/CrankBlock.class": (
        "be3c8f3d7d73bfc7e801ef66fe0b46fec5e2bd5ef08a6b6d91e2933e8bd5f44a"
    ),
    "appeng/blockentity/misc/CrankBlockEntity.class": (
        "6eab6d38bd91cb3fca99a5bec44201759cc7dc18a50962cc11a32b09e368cd46"
    ),
    "appeng/client/render/tesr/CrankRenderer.class": (
        "dead2d7edde004ed0f84fb7fb89fbe0e886dce36a0d73c01b3f46db323667c4d"
    ),
    "appeng/block/misc/InscriberBlock.class": (
        "b84e655ced087399130981feb13ad5668dac5b2689138e89b14ecc312b5d706c"
    ),
    "appeng/blockentity/misc/InscriberBlockEntity.class": (
        "8fb4a5b9440811fba3468853918e8b4df5b385cbca3164cb9211307950a92466"
    ),
    "appeng/client/render/tesr/InscriberTESR.class": (
        "d697a877d973951efc5724a5224ff682343bf27e139a5c02421cfda3e61fcda5"
    ),
    "appeng/client/render/cablebus/CubeBuilder.class": (
        "5b7cd4efccaa90f3a539a6a7a30c3d5fbb931858e5363cb539b0d72e446f90db"
    ),
    "appeng/api/orientation/BlockOrientation.class": (
        "1a47c706e3d1659a2cbaea229dc367698d96bfd752a171b6ea627262fa352186"
    ),
    "appeng/core/AppEngClient.class": (
        "d8c952a6ab7845c7341b1427ec94c7319a95908c4be035f14ef81a798ec179c8"
    ),
    "appeng/core/definitions/AEBlocks.class": (
        "1b882d6be449388cdaa838b235c0dc646ca205fae5c92d811278bca3c5b13afb"
    ),
    "appeng/core/definitions/AEBlockEntities.class": (
        "54bd311db893bb10b44f8244e27b1da4afa27a3ab1219da3c0c3c255c0ff9fce"
    ),
    "appeng/init/client/InitBuiltInModels.class": (
        "4dc64e570af985893039fe8519c3295ed5330f995984d48bb6b67d5b67978341"
    ),
}

EXPECTED_SOURCE_SHA256 = {
    "appeng/block/paint/PaintSplotches.java": (
        "56b66d9c11a2aeed55c034e494533bb4f78c044d76c0321b944374b62e7c9f33"
    ),
    "appeng/block/paint/PaintSplotchesBakedModel.java": (
        "b756405efb320d7a95c88f2d121d2037eab684a8a1fd422b3df675e477e51a8a"
    ),
    "appeng/block/paint/PaintSplotchesBlock.java": (
        "9a1c010799e7af7d6959c39a40e331ceba4ba1977470852554dbd8e48aea1205"
    ),
    "appeng/block/paint/PaintSplotchesModel.java": (
        "2bb61e5d50b6d2658bfc4de5712a0edd7b1425544d87104fa4657148b877f066"
    ),
    "appeng/blockentity/misc/PaintSplotchesBlockEntity.java": (
        "54771d96a978f4811103fd64f5a6d23ea735998176448ee4246542c1664253b8"
    ),
    "appeng/helpers/Splotch.java": (
        "35221ef5399250f93d94953b4cbbcc1b115d46bc597b5ca6590b90f6a910f4f5"
    ),
    "appeng/block/spatial/SpatialPylonBlock.java": (
        "56a0d1cd4be85bd9ba8ab278e3c02a653c0e3709069ac30ac7cac5db29d4baca"
    ),
    "appeng/blockentity/spatial/SpatialPylonBlockEntity.java": (
        "dcd3f12e7b4fece000245cd919f4700afde976cdd0f1466fd3670e6f307887fb"
    ),
    "appeng/client/render/tesr/spatial/SpatialPylonBakedModel.java": (
        "2662bd666b710230aecbfde96e9b82021e8a3c1abd9d19b609b9fe49b3ea0190"
    ),
    "appeng/client/render/tesr/spatial/SpatialPylonModel.java": (
        "57507ed8e052e9cc0cb1eb1933e8b83cdd47e6ba23e39eac9c27a306943c2877"
    ),
    "appeng/client/render/tesr/spatial/SpatialPylonTextureType.java": (
        "8d8e67372ed6d3ea3fbc74082b1efc603e0107598ec251738e35040bdea47b3e"
    ),
    "appeng/me/cluster/implementations/SpatialPylonCalculator.java": (
        "9e8b5a3d730e051e8cac2b42dcacc5e65e2a33014c0477f96b2ff2f5f6df15c1"
    ),
    "appeng/me/cluster/implementations/SpatialPylonCluster.java": (
        "4a436fcd938879db35cf18c9c116adef0dd744868e7c6c5820820e44d5582c60"
    ),
    "appeng/me/cluster/MBCalculator.java": (
        "61d3439fa25c3fd88d60da739dd30a010ed4a5ad773a24ba80c77ebe4955d313"
    ),
    "appeng/me/service/SpatialPylonService.java": (
        "9f021f49de8bb9706652dae8e3aa5a1e7215e72051551a2993bacedb966f46df"
    ),
    "appeng/block/storage/SkyChestBlock.java": (
        "8150f003d125b4285024782359ec70d1226062b4a6a4ec89b0340ad39c5d8d17"
    ),
    "appeng/blockentity/storage/SkyChestBlockEntity.java": (
        "b6bb1b60f80e23827de61d2e91d2c99d690ce35415ad175607cdea12e6413a7c"
    ),
    "appeng/client/render/tesr/SkyChestTESR.java": (
        "b58079d1474af74b4ef5b6ea0b66d3cea939413a7035b7221c847c31fe9ed366"
    ),
    "appeng/block/misc/CrankBlock.java": (
        "fb89977b57ad5c4c70cb4b31ca96ca780d4c3e5f87ae504057c6e8f3258cd332"
    ),
    "appeng/blockentity/misc/CrankBlockEntity.java": (
        "b652a60396a5887c8880d614a2811df577148ece8a23184480b2dfb5d3146386"
    ),
    "appeng/client/render/tesr/CrankRenderer.java": (
        "66dbe5e6e1cdbe1fa364f9c53bb0418fd045df2fee08e5e61b52486b4fd2fedc"
    ),
    "appeng/block/misc/InscriberBlock.java": (
        "b9f9066ace85d5ffc8d6845eff88ab1078db869921dae339aaf087ce60f46565"
    ),
    "appeng/blockentity/misc/InscriberBlockEntity.java": (
        "f26bf81a5c4ca9ead16af9d59f8160837d451b067769066c340bdb16eed8785c"
    ),
    "appeng/client/render/tesr/InscriberTESR.java": (
        "9cefcd67765913615eaae590bc10e9b59483bcf63b9bfea8045a3c58fe407c5f"
    ),
    "appeng/client/render/cablebus/CubeBuilder.java": (
        "728a2b601396db6ad881f0d7d8871dde870fc7b55abbe598c95cc87e22684b5b"
    ),
    "appeng/api/orientation/BlockOrientation.java": (
        "d91cada671714ecbe797af72cbc2faa87038a972582d6a19f583f78422220d8a"
    ),
    "appeng/core/AppEngClient.java": (
        "6bc171c7c1c10a181b1009a29af5879a8610c618eb752becca7cdb2ae790f03c"
    ),
    "appeng/core/definitions/AEBlocks.java": (
        "350ecdb536e2a57f3380aae04639ab761d64467f573f134b069e0a0c898a5d58"
    ),
    "appeng/core/definitions/AEBlockEntities.java": (
        "e6bcec2dc1a5e699c2f6ec38f3df16596828f64fab4ced85608c3d80214a124c"
    ),
    "appeng/init/client/InitBuiltInModels.java": (
        "517a2da6cf528b8133c471375cb6e901673e68d8c052bdba3ae685b9c1f01b7a"
    ),
}


def digest(path: Path, algorithm: str) -> str:
    value = hashlib.new(algorithm)
    with path.open("rb") as source:
        for chunk in iter(lambda: source.read(64 * 1024), b""):
            value.update(chunk)
    return value.hexdigest()


def verify_hash(label: str, actual: str, expected: str) -> None:
    if actual != expected:
        raise ValueError(f"{label}: got {actual}, expected {expected}")


def verify_exact_identity(
    path: Path,
    label: str,
    size: int,
    sha1: str,
    sha256: str,
    sha512: str,
) -> None:
    if not path.is_file():
        raise ValueError(f"{label} is not a regular file: {path}")
    if path.stat().st_size != size:
        raise ValueError(f"{label} size changed")
    verify_hash(f"{label} SHA-1", digest(path, "sha1"), sha1)
    verify_hash(f"{label} SHA-256", digest(path, "sha256"), sha256)
    verify_hash(f"{label} SHA-512", digest(path, "sha512"), sha512)


def verify_png_header(path: str, raw: bytes) -> None:
    if len(raw) < 33 or raw[:8] != b"\x89PNG\r\n\x1a\n":
        raise ValueError(f"{path} is not a complete PNG")
    length = struct.unpack(">I", raw[8:12])[0]
    if length != 13 or raw[12:16] != b"IHDR":
        raise ValueError(f"{path} has no canonical PNG IHDR")
    width, height, bit_depth, color_type = struct.unpack(">IIBB", raw[16:26])
    expected_size = 64 if path.endswith((
        "/inscriber.png",
        "/skychest.png",
        "/skyblockchest.png",
    )) else 16
    if (width, height, bit_depth, color_type) != (
        expected_size,
        expected_size,
        8,
        6,
    ):
        raise ValueError(f"{path} dimensions or RGBA8 encoding changed")


def accepted_resource_paths(project: Path) -> set[str]:
    paths: set[str] = set()
    main = project / (
        "src/main/resources/bluemap-ae2/profiles/ae2/19.2.17/"
        "required-resources.sha256"
    )
    for line in main.read_text(encoding="utf-8").splitlines():
        fields = line.split("  ", 1)
        if len(fields) != 2:
            raise ValueError("accepted main resource manifest is malformed")
        paths.add(fields[1])
    for relative in (
        "ae2/19.2.17/routes/quartz-glass/required-resources.tsv",
        "ae2/19.2.17/routes/crafting/required-resources.tsv",
        "ae2/19.2.17/routes/quantum-bridge/required-resources.tsv",
        "extendedae/1.21-2.2.33-neoforge/required-resources.tsv",
    ):
        manifest = project / "src/main/resources/bluemap-ae2/profiles" / relative
        paths.update(parse_resource_manifest(manifest.read_bytes()))
    return paths


def verify_profile(project: Path) -> dict[str, tuple[int, str]]:
    route_root = project / (
        "src/main/resources/bluemap-ae2/profiles/ae2/19.2.17/routes/"
        "m3-completion"
    )
    actual_route_files = {
        path.name for path in route_root.iterdir() if path.is_file()
    }
    if actual_route_files != {"profile.json", "required-resources.tsv"}:
        raise ValueError("M3f route directory contains non-contract files")

    manifest = (route_root / "required-resources.tsv").read_bytes()
    verify_hash(
        "M3f resource manifest SHA-256",
        hashlib.sha256(manifest).hexdigest(),
        EXPECTED_RESOURCE_MANIFEST_SHA256,
    )
    partitions = (
        (
            "source texture manifest",
            SOURCE_TEXTURES,
            EXPECTED_SOURCE_TEXTURE_MANIFEST_SHA256,
        ),
        (
            "emitted-static texture manifest",
            EMITTED_STATIC_TEXTURES,
            EXPECTED_EMITTED_STATIC_TEXTURE_MANIFEST_SHA256,
        ),
        (
            "fallback texture manifest",
            FALLBACK_ONLY_TEXTURES,
            EXPECTED_FALLBACK_TEXTURE_MANIFEST_SHA256,
        ),
    )
    for label, textures, expected in partitions:
        verify_hash(
            f"M3f {label} SHA-256",
            hashlib.sha256(texture_manifest(manifest, textures)).hexdigest(),
            expected,
        )

    resources = parse_resource_manifest(manifest)
    if list(resources) != expected_resource_paths():
        raise ValueError("M3f manifest does not match the exact path set")
    overlap = set(resources) & accepted_resource_paths(project)
    if overlap:
        raise ValueError(f"M3f resource closure overlaps accepted profiles: {overlap}")

    actual_profile_bytes = (route_root / "profile.json").read_bytes()
    verify_hash(
        "M3f generated profile SHA-256",
        hashlib.sha256(actual_profile_bytes).hexdigest(),
        EXPECTED_PROFILE_SHA256,
    )
    if json.loads(actual_profile_bytes.decode("utf-8")) != profile(manifest):
        raise ValueError("M3f profile does not match the exact contract")
    if actual_profile_bytes != profile_bytes(manifest):
        raise ValueError("M3f profile serialization is not canonical")

    for relative_path, expected_digest in EXPECTED_FROZEN_OUTPUT_SHA256.items():
        verify_hash(
            f"frozen accepted output {relative_path}",
            hashlib.sha256((project / relative_path).read_bytes()).hexdigest(),
            expected_digest,
        )
    return resources


def verify_blockstates_and_models(archive: zipfile.ZipFile) -> None:
    for block in ("paint", "spatial_pylon"):
        if json.loads(archive.read(f"assets/ae2/blockstates/{block}.json")) != {
            "variants": {"": {"model": f"ae2:block/{block}"}}
        }:
            raise ValueError(f"{block} built-in-model route changed")
        if json.loads(archive.read(f"assets/ae2/models/block/{block}.json")) != {}:
            raise ValueError(f"{block} built-in empty model stub changed")

    chest_routes = {
        "sky_stone_chest": "sky_stone_block",
        "smooth_sky_stone_chest": "smooth_sky_stone_block",
    }
    for block, model in chest_routes.items():
        state = json.loads(archive.read(f"assets/ae2/blockstates/{block}.json"))
        if state.get("variants") != {"": {"model": f"ae2:block/{model}"}}:
            raise ValueError(f"{block} particle fallback route changed")
        fallback = json.loads(archive.read(f"assets/ae2/models/block/{model}.json"))
        if fallback != {
            "parent": "minecraft:block/cube_all",
            "textures": {"all": f"ae2:block/{model}"},
        }:
            raise ValueError(f"{block} full-cube fallback model changed")

    crank = json.loads(archive.read("assets/ae2/models/block/crank.json"))
    crank_base = json.loads(archive.read("assets/ae2/models/block/crank_base.json"))
    crank_handle = json.loads(archive.read("assets/ae2/models/block/crank_handle.json"))
    if [len(model.get("elements", [])) for model in (crank, crank_base, crank_handle)] != [3, 1, 2]:
        raise ValueError("crank stock/base/handle structural models changed")
    if json.loads(archive.read("assets/ae2/blockstates/crank.json")) != {
        "variants": {"": {"model": "ae2:block/crank"}}
    }:
        raise ValueError("crank stock model route changed")

    inscriber_state = json.loads(archive.read("assets/ae2/blockstates/inscriber.json"))
    if len(inscriber_state.get("variants", {})) != 24:
        raise ValueError("inscriber orientation-state matrix changed")
    inscriber_model = json.loads(archive.read("assets/ae2/models/block/inscriber.json"))
    if (
        inscriber_model.get("textures") != {
            "particle": "ae2:block/inscriber",
            "base": "ae2:block/inscriber",
        }
        or len(inscriber_model.get("elements", [])) != 8
    ):
        raise ValueError("inscriber structural casing model changed")


def verify_runtime_archive(
    jar: Path, expected_resources: dict[str, tuple[int, str]]
) -> None:
    with zipfile.ZipFile(jar) as archive:
        names = [entry.filename for entry in archive.infolist()]
        if len(names) != len(set(names)):
            raise ValueError("AE2 runtime artifact contains duplicate ZIP entry names")
        for path, (expected_size, expected_digest) in expected_resources.items():
            if path not in names:
                raise ValueError(f"runtime artifact is missing required resource: {path}")
            if archive.getinfo(path).file_size != expected_size:
                raise ValueError(f"required resource size changed: {path}")
            verify_hash(
                f"{path} SHA-256",
                hashlib.sha256(archive.read(path)).hexdigest(),
                expected_digest,
            )

        verify_metadata(archive.read("META-INF/neoforge.mods.toml"))
        verify_blockstates_and_models(archive)
        for path in expected_resources:
            if not path.endswith(".png"):
                continue
            verify_png_header(path, archive.read(path))
            if f"{path}.mcmeta" in names:
                raise ValueError(f"static M3f texture unexpectedly has metadata: {path}")

        if set(EXPECTED_RUNTIME_CLASS_SHA256) - set(names):
            raise ValueError("runtime artifact is missing an audited M3f class")
        for path, expected_digest in EXPECTED_RUNTIME_CLASS_SHA256.items():
            verify_hash(
                f"{path} SHA-256",
                hashlib.sha256(archive.read(path)).hexdigest(),
                expected_digest,
            )


def require_markers(source: bytes, label: str, markers: tuple[bytes, ...]) -> None:
    for marker in markers:
        if marker not in source:
            raise ValueError(f"{label} source marker changed: {marker!r}")


def verify_sources_archive(sources_jar: Path) -> None:
    with zipfile.ZipFile(sources_jar) as archive:
        names = [entry.filename for entry in archive.infolist()]
        if len(names) != len(set(names)):
            raise ValueError("AE2 sources artifact contains duplicate ZIP entry names")
        if set(EXPECTED_SOURCE_SHA256) - set(names):
            raise ValueError("sources artifact is missing an audited M3f source")
        for path, expected_digest in EXPECTED_SOURCE_SHA256.items():
            verify_hash(
                f"{path} SHA-256",
                hashlib.sha256(archive.read(path)).hexdigest(),
                expected_digest,
            )

        require_markers(
            archive.read("appeng/blockentity/misc/PaintSplotchesBlockEntity.java"),
            "persisted paint",
            (b'putByteArray("dots"', b"dots.size() > 20", b"writeBuffer", b"readBuffer"),
        )
        require_markers(
            archive.read("appeng/helpers/Splotch.java"),
            "paint byte encoding",
            (b"val & 0x07", b"val >> 3 & 0x0F", b"val >> 7 & 0x01", b"/ 15.0f"),
        )
        require_markers(
            archive.read("appeng/block/paint/PaintSplotchesBakedModel.java"),
            "paint renderer",
            (b"0.001f", b"setEmissiveMaterial(true)", b"getSeed() %", b"RenderType.CUTOUT"),
        )
        require_markers(
            archive.read("appeng/blockentity/spatial/SpatialPylonBlockEntity.java"),
            "transient pylon state",
            (
                b"ClientState.DEFAULT",
                b"saveVisualState",
                b"this.cluster = c",
                b"this.recalculateDisplay()",
                b"AxisPosition.NONE",
                b"axisPosition",
                b"online",
                b"powered",
            ),
        )
        require_markers(
            archive.read("appeng/me/cluster/implementations/SpatialPylonCalculator.java"),
            "global pylon topology",
            (b"checkMultiblockScale", b"verifyInternalStructure", b"SpatialPylonBlockEntity"),
        )
        require_markers(
            archive.read("appeng/me/cluster/MBCalculator.java"),
            "invalid pylon cluster disconnect",
            (
                b"isValidBlockEntityAt",
                b"verifyUnownedRegion",
                b"verifyInternalStructure",
                b"this.disconnect()",
            ),
        )
        require_markers(
            archive.read("appeng/client/render/tesr/spatial/SpatialPylonBakedModel.java"),
            "pylon model",
            (
                b"axisPosition() != SpatialPylonBlockEntity.AxisPosition.NONE",
                b"SpatialPylonTextureType.BASE",
                b"SpatialPylonTextureType.DIM",
                b"builder.addCube(0, 0, 0, 16, 16, 16)",
                b"BASE_SPANNED",
                b"RED_SPANNED",
                b"DIM_SPANNED",
                b"setUvRotation",
                b"setFlipV",
            ),
        )
        require_markers(
            archive.read("appeng/client/render/tesr/SkyChestTESR.java"),
            "closed chest model",
            (b"addBox(1.0F, 0.0F, 1.0F, 14.0F, 10.0F, 14.0F)", b"getOpenNess", b"entityCutout"),
        )
        require_markers(
            archive.read("appeng/client/render/tesr/CrankRenderer.java"),
            "neutral crank model",
            (b"block/crank_base", b"block/crank_handle", b"getVisibleRotation", b"tesselateWithAO"),
        )
        require_markers(
            archive.read("appeng/client/render/tesr/InscriberTESR.java"),
            "neutral inscriber stamps",
            (b"float press = 0.2f", b"final float TwoPx", b"float base = 0.4f", b"renderItem"),
        )


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--jar", required=True, type=Path)
    parser.add_argument("--sources-jar", required=True, type=Path)
    args = parser.parse_args()

    verify_exact_identity(
        args.jar,
        "AE2 runtime artifact",
        EXPECTED_SIZE,
        EXPECTED_SHA1,
        EXPECTED_SHA256,
        EXPECTED_SHA512,
    )
    verify_exact_identity(
        args.sources_jar,
        "AE2 sources artifact",
        EXPECTED_SOURCES_SIZE,
        EXPECTED_SOURCES_SHA1,
        EXPECTED_SOURCES_SHA256,
        EXPECTED_SOURCES_SHA512,
    )
    project = Path(__file__).resolve().parents[1]
    resources = verify_profile(project)
    verify_runtime_archive(args.jar, resources)
    verify_sources_archive(args.sources_jar)

    print(
        "Verified AE2 19.2.17 M3f completion route: exact runtime and sources "
        f"({SOURCE_COMMIT}), 30 audited runtime classes, 30 byte-identical "
        "audited source files, 6 native blocks, 17 source textures, 15 "
        "emitted-static textures, 2 fallback-only textures, 33 disjoint "
        "resources and all accepted M0-M3e profile outputs unchanged; bounded "
        "locally invalid pylon components use unformed BASE plus DIM, while "
        "contents, activity, global validity beyond that bounded local "
        "component and extension connector compatibility remain unclaimed."
    )
    return 0


if __name__ == "__main__":
    try:
        sys.exit(main())
    except (
        json.JSONDecodeError,
        OSError,
        tomllib.TOMLDecodeError,
        UnicodeError,
        ValueError,
        zipfile.BadZipFile,
    ) as error:
        print(f"AE2 M3f artifact verification failed: {error}", file=sys.stderr)
        sys.exit(1)
