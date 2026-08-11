#!/usr/bin/env python3
# SPDX-License-Identifier: LGPL-3.0-only
"""Verify exact AE2 M3d resources and exact pack extension connector evidence."""

from __future__ import annotations

import argparse
import hashlib
import json
from pathlib import Path
import struct
import sys
import tomllib
import zipfile

from ae2_crafting_contract import (
    ADVANCED_AE_ARTIFACT,
    EXPANDED_AE_ARTIFACT,
    EXPANDED_AE_COMPATIBLE_CONNECTORS,
    EXPECTED_FROZEN_OUTPUT_SHA256,
    EXPECTED_PROFILE_SHA256,
    EXPECTED_RESOURCE_MANIFEST_SHA256,
    EXPECTED_SHA1,
    EXPECTED_SHA256,
    EXPECTED_SHA512,
    EXPECTED_SIZE,
    EXPECTED_TEXTURE_MANIFEST_SHA256,
    EXTENDED_AE_ARTIFACT,
    MEGA_CELLS_ARTIFACT,
    MEGA_CELLS_COMPATIBLE_CONNECTORS,
    UNSUPPORTED_COMPATIBLE_CONNECTORS,
    expected_resource_paths,
    parse_resource_manifest,
    profile,
    profile_bytes,
    texture_manifest,
)
from verify_pinned_artifact import verify_metadata


EXPECTED_AE2_CLASS_SHA256 = {
    "appeng/client/render/crafting/CraftingCubeBakedModel.class": (
        "c2d28cf704d1765bf063495d011ce5ae8925aa0003b0c71c39e2ce4ed5fcd124"
    ),
    "appeng/client/render/crafting/UnitBakedModel.class": (
        "810683548a6662ef95123f9711df81b8cbb7c0ecf74bb24a12bac942aae251fe"
    ),
    "appeng/client/render/crafting/LightBakedModel.class": (
        "dab88c09606426823435819725b47c8e91998d4a6f691f87e7f32a606f34d669"
    ),
    "appeng/client/render/crafting/MonitorBakedModel.class": (
        "2837cd33bb090c13608bdc5044e3a38e561d9a51b6a289a88ebc950bdbf5511d"
    ),
    "appeng/client/render/crafting/CraftingUnitModelProvider.class": (
        "63910490b139af9c9008c98ca12af658c474efb92aeb137d20b7fc05f7685904"
    ),
    "appeng/client/render/cablebus/CubeBuilder.class": (
        "5b7cd4efccaa90f3a539a6a7a30c3d5fbb931858e5363cb539b0d72e446f90db"
    ),
    "appeng/blockentity/crafting/CraftingBlockEntity.class": (
        "2d6d6ccd633e5d61d9c064c04bb7c301758632304bb62bf529c8ceb916057d0c"
    ),
    "appeng/blockentity/crafting/CraftingMonitorBlockEntity.class": (
        "a491e419fa5221fcc203e789a7e8cd13f1bb1f303c6fbb5cd5ac7e7c9fa557f5"
    ),
    "appeng/me/cluster/implementations/CraftingCPUCalculator.class": (
        "12ab79b576575aea98d481c23c796257ff8057d36f3a1e6f5e95770a921c8d24"
    ),
    "appeng/block/crafting/AbstractCraftingUnitBlock.class": (
        "f921dcc4dcc734b13c6712a3bd47674a4ff4ae75233806b2a6ae4ed6fab08408"
    ),
    "appeng/block/crafting/CraftingUnitBlock.class": (
        "3847db3fc763d6a523296790d3c7c70f047d8b5eb28955e4aa0b2c38f636ba84"
    ),
    "appeng/block/crafting/CraftingMonitorBlock.class": (
        "4574fb72d321455e99adf3916d1ed1e95e44624ed1d6634fdaa554b3d982c943"
    ),
    "appeng/core/definitions/AEBlocks.class": (
        "1b882d6be449388cdaa838b235c0dc646ca205fae5c92d811278bca3c5b13afb"
    ),
    "appeng/block/AEBaseBlock.class": (
        "dcfd04cbaee52d7c7e6854a36e291ccbbc77eaaa53577960bce68fc29d16dfd2"
    ),
}

MEGA_DEFINITION_CLASS = "gripe/_90/megacells/definition/MEGABlocks.class"
MEGA_DEFINITION_SHA256 = (
    "2cd93e8c40c27c1123d724214f8b51e8c58aa95faf44bf9cead50d0ba20e40f9"
)
EXPANDED_DEFINITION_CLASS = "lu/kolja/expandedae/definition/ExpBlocks.class"
EXPANDED_DEFINITION_SHA256 = (
    "a0974082fac7050bcc26174c64ea40755d9e9c15d14fd170879b3e28cab6d371"
)
ADVANCED_BASE_CLASS = (
    "net/pedroksl/advanced_ae/common/blocks/AAEAbstractCraftingUnitBlock.class"
)
ADVANCED_BASE_SHA256 = (
    "de6cb2f4b716c0114791797203f6db325eaf07833ebb4ad5166844b9a852e06c"
)

AE2_CRAFTING_UNIT_CLASS = b"appeng/block/crafting/CraftingUnitBlock"
AE2_CRAFTING_MONITOR_CLASS = b"appeng/block/crafting/CraftingMonitorBlock"
AE2_ABSTRACT_CRAFTING_CLASS = b"appeng/block/crafting/AbstractCraftingUnitBlock"


def digest(path: Path, algorithm: str) -> str:
    value = hashlib.new(algorithm)
    with path.open("rb") as source:
        for chunk in iter(lambda: source.read(64 * 1024), b""):
            value.update(chunk)
    return value.hexdigest()


def verify_hash(label: str, actual: str, expected: str) -> None:
    if actual != expected:
        raise ValueError(f"{label}: got {actual}, expected {expected}")


def verify_png_header(path: str, raw: bytes) -> None:
    if len(raw) < 33 or raw[:8] != b"\x89PNG\r\n\x1a\n":
        raise ValueError(f"{path} is not a complete PNG")
    length = struct.unpack(">I", raw[8:12])[0]
    if length != 13 or raw[12:16] != b"IHDR":
        raise ValueError(f"{path} has no canonical PNG IHDR")
    width, height, bit_depth, color_type = struct.unpack(">IIBB", raw[16:26])
    if (width, height, bit_depth, color_type) != (16, 16, 8, 6):
        raise ValueError(f"{path} is not exact 16x16 RGBA8 texture data")


def verify_profile(project: Path) -> dict[str, tuple[int, str]]:
    route_root = project / (
        "src/main/resources/bluemap-ae2/profiles/ae2/19.2.17/routes/crafting"
    )
    manifest = (route_root / "required-resources.tsv").read_bytes()
    verify_hash(
        "M3d resource manifest SHA-256",
        hashlib.sha256(manifest).hexdigest(),
        EXPECTED_RESOURCE_MANIFEST_SHA256,
    )
    verify_hash(
        "M3d texture manifest SHA-256",
        hashlib.sha256(texture_manifest(manifest)).hexdigest(),
        EXPECTED_TEXTURE_MANIFEST_SHA256,
    )
    resources = parse_resource_manifest(manifest)
    if list(resources) != expected_resource_paths():
        raise ValueError("M3d manifest does not match the exact path set")
    actual_profile_bytes = (route_root / "profile.json").read_bytes()
    verify_hash(
        "M3d generated profile SHA-256",
        hashlib.sha256(actual_profile_bytes).hexdigest(),
        EXPECTED_PROFILE_SHA256,
    )
    actual_profile = json.loads(actual_profile_bytes.decode("utf-8"))
    if actual_profile != profile(manifest):
        raise ValueError("M3d profile does not match the exact contract")
    if actual_profile_bytes != profile_bytes(manifest):
        raise ValueError("M3d profile serialization is not canonical")

    for relative_path, expected_digest in EXPECTED_FROZEN_OUTPUT_SHA256.items():
        verify_hash(
            f"frozen accepted output {relative_path}",
            hashlib.sha256((project / relative_path).read_bytes()).hexdigest(),
            expected_digest,
        )
    return resources


def verify_blockstate_semantics(archive: zipfile.ZipFile) -> None:
    simple_models = {
        "ae2:crafting_unit": ("unit", "unit_formed"),
        "ae2:crafting_accelerator": ("accelerator", "accelerator_formed"),
        "ae2:1k_crafting_storage": ("1k_storage", "1k_storage_formed"),
        "ae2:4k_crafting_storage": ("4k_storage", "4k_storage_formed"),
        "ae2:16k_crafting_storage": ("16k_storage", "16k_storage_formed"),
        "ae2:64k_crafting_storage": ("64k_storage", "64k_storage_formed"),
        "ae2:256k_crafting_storage": ("256k_storage", "256k_storage_formed"),
    }
    for block_id, (unformed, formed) in simple_models.items():
        path = f"assets/ae2/blockstates/{block_id.split(':', 1)[1]}.json"
        actual = json.loads(archive.read(path))
        expected = {
            "variants": {
                "formed=false": {"model": f"ae2:block/crafting/{unformed}"},
                "formed=true": {"model": f"ae2:block/crafting/{formed}"},
            }
        }
        if actual != expected:
            raise ValueError(f"formed/unformed blockstate routing changed: {path}")

    monitor = json.loads(archive.read("assets/ae2/blockstates/crafting_monitor.json"))
    variants = monitor.get("variants")
    if not isinstance(variants, dict) or set(variants) != {
        f"facing={facing},formed={formed}"
        for facing in ("down", "east", "north", "south", "up", "west")
        for formed in ("false", "true")
    }:
        raise ValueError("crafting-monitor facing/formed state closure changed")
    for key, value in variants.items():
        expected_model = (
            "ae2:block/crafting/monitor_formed"
            if key.endswith("formed=true")
            else "ae2:block/crafting/monitor"
        )
        if value.get("model") != expected_model:
            raise ValueError("crafting-monitor model routing changed")


def verify_ae2_archive(
    jar: Path, expected_resources: dict[str, tuple[int, str]]
) -> None:
    with zipfile.ZipFile(jar) as archive:
        names = [entry.filename for entry in archive.infolist()]
        if len(names) != len(set(names)):
            raise ValueError("AE2 artifact contains duplicate ZIP entry names")
        for path, (expected_size, expected_digest) in expected_resources.items():
            if path not in names:
                raise ValueError(f"artifact is missing required resource: {path}")
            if archive.getinfo(path).file_size != expected_size:
                raise ValueError(f"required resource size changed: {path}")
            verify_hash(
                f"{path} SHA-256",
                hashlib.sha256(archive.read(path)).hexdigest(),
                expected_digest,
            )

        verify_metadata(archive.read("META-INF/neoforge.mods.toml"))
        verify_blockstate_semantics(archive)
        if "assets/ae2/models/block/crafting/monitor_formed.json" in names:
            raise ValueError("client-built-in monitor formed model unexpectedly became JSON")
        for path in expected_resources:
            raw = archive.read(path)
            if path.endswith("_formed.json") and json.loads(raw) != {}:
                raise ValueError(f"formed dynamic-model stub changed: {path}")
            if path.endswith(".png"):
                verify_png_header(path, raw)
                if f"{path}.mcmeta" in names:
                    raise ValueError(f"M3d texture unexpectedly has metadata: {path}")

        for path, expected_digest in EXPECTED_AE2_CLASS_SHA256.items():
            if path not in names:
                raise ValueError(f"artifact is missing audited class: {path}")
            verify_hash(
                f"{path} SHA-256",
                hashlib.sha256(archive.read(path)).hexdigest(),
                expected_digest,
            )

        connection_class = archive.read(
            "appeng/blockentity/crafting/CraftingBlockEntity.class"
        )
        if AE2_ABSTRACT_CRAFTING_CLASS not in connection_class:
            raise ValueError("AE2 direct-neighbor crafting superclass test changed")
        base_block = archive.read("appeng/block/AEBaseBlock.class")
        unit_block = archive.read("appeng/block/crafting/CraftingUnitBlock.class")
        monitor_block = archive.read("appeng/block/crafting/CraftingMonitorBlock.class")
        if b"forceSolidOn" not in base_block or b"metalProps" not in base_block:
            raise ValueError("AE2 metalProps force-solid policy changed")
        if b"metalProps" not in unit_block or b"metalProps" not in monitor_block:
            raise ValueError("AE2 crafting blocks no longer use metalProps")


def verify_artifact_identity(path: Path, contract: dict[str, object]) -> None:
    if not path.is_file():
        raise ValueError(f"artifact is not a regular file: {path}")
    if path.stat().st_size != contract["sizeBytes"]:
        raise ValueError(f"{contract['modId']} artifact size changed")
    verify_hash(
        f"{contract['modId']} artifact SHA-1",
        digest(path, "sha1"),
        str(contract["sha1"]),
    )
    verify_hash(
        f"{contract['modId']} artifact SHA-256",
        digest(path, "sha256"),
        str(contract["sha256"]),
    )


def verify_connector_artifacts(
    mega_cells_jar: Path,
    expanded_ae_jar: Path,
    advanced_ae_jar: Path,
    extended_ae_jar: Path,
) -> None:
    artifacts = (
        (mega_cells_jar, MEGA_CELLS_ARTIFACT),
        (expanded_ae_jar, EXPANDED_AE_ARTIFACT),
        (advanced_ae_jar, ADVANCED_AE_ARTIFACT),
        (extended_ae_jar, EXTENDED_AE_ARTIFACT),
    )
    for path, contract in artifacts:
        verify_artifact_identity(path, contract)

    with zipfile.ZipFile(mega_cells_jar) as archive:
        definition = archive.read(MEGA_DEFINITION_CLASS)
        verify_hash(
            "MEGA Cells block definitions SHA-256",
            hashlib.sha256(definition).hexdigest(),
            MEGA_DEFINITION_SHA256,
        )
        if (
            AE2_CRAFTING_UNIT_CLASS not in definition
            or AE2_CRAFTING_MONITOR_CLASS not in definition
        ):
            raise ValueError("MEGA Cells no longer constructs AE2 crafting block classes")
        names = set(archive.namelist())
        for block_id in MEGA_CELLS_COMPATIBLE_CONNECTORS:
            block_name = block_id.split(":", 1)[1]
            if block_name.encode("ascii") not in definition:
                raise ValueError(
                    f"MEGA Cells definition no longer registers connector: {block_id}"
                )
            expected = f"assets/megacells/blockstates/{block_name}.json"
            if expected not in names:
                raise ValueError(f"MEGA Cells connector blockstate missing: {block_id}")

    with zipfile.ZipFile(expanded_ae_jar) as archive:
        definition = archive.read(EXPANDED_DEFINITION_CLASS)
        verify_hash(
            "Expanded AE block definitions SHA-256",
            hashlib.sha256(definition).hexdigest(),
            EXPANDED_DEFINITION_SHA256,
        )
        if AE2_CRAFTING_UNIT_CLASS not in definition:
            raise ValueError("Expanded AE no longer constructs AE2 CraftingUnitBlock")
        names = set(archive.namelist())
        for block_id in EXPANDED_AE_COMPATIBLE_CONNECTORS:
            block_name = block_id.split(":", 1)[1]
            if block_name.encode("ascii") not in definition:
                raise ValueError(
                    f"Expanded AE definition no longer registers connector: {block_id}"
                )
            expected = f"assets/expandedae/blockstates/{block_name}.json"
            if expected not in names:
                raise ValueError(f"Expanded AE connector blockstate missing: {block_id}")

    marker_classes = (
        AE2_ABSTRACT_CRAFTING_CLASS,
        AE2_CRAFTING_UNIT_CLASS,
        AE2_CRAFTING_MONITOR_CLASS,
    )
    for path, label in (
        (advanced_ae_jar, "Advanced AE"),
        (extended_ae_jar, "ExtendedAE"),
    ):
        with zipfile.ZipFile(path) as archive:
            matches = [
                entry.filename
                for entry in archive.infolist()
                if entry.filename.endswith(".class")
                and any(marker in archive.read(entry) for marker in marker_classes)
            ]
            if matches:
                raise ValueError(
                    f"{label} unexpectedly references AE2 native crafting classes: {matches}"
                )

    with zipfile.ZipFile(advanced_ae_jar) as archive:
        advanced_base = archive.read(ADVANCED_BASE_CLASS)
        verify_hash(
            "Advanced AE unrelated crafting base SHA-256",
            hashlib.sha256(advanced_base).hexdigest(),
            ADVANCED_BASE_SHA256,
        )
        if b"appeng/block/AEBaseEntityBlock" not in advanced_base:
            raise ValueError("Advanced AE crafting base superclass changed")

    if len(UNSUPPORTED_COMPATIBLE_CONNECTORS) != 29 or len(
        set(UNSUPPORTED_COMPATIBLE_CONNECTORS)
    ) != 29:
        raise ValueError("known compatible extension connector set is not exact")


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--jar", required=True, type=Path)
    parser.add_argument("--mega-cells-jar", required=True, type=Path)
    parser.add_argument("--expanded-ae-jar", required=True, type=Path)
    parser.add_argument("--advanced-ae-jar", required=True, type=Path)
    parser.add_argument("--extended-ae-jar", required=True, type=Path)
    args = parser.parse_args()

    project = Path(__file__).resolve().parents[1]
    if not args.jar.is_file():
        raise ValueError(f"artifact is not a regular file: {args.jar}")
    if args.jar.stat().st_size != EXPECTED_SIZE:
        raise ValueError(
            f"AE2 artifact size: got {args.jar.stat().st_size}, expected {EXPECTED_SIZE}"
        )
    verify_hash("AE2 artifact SHA-1", digest(args.jar, "sha1"), EXPECTED_SHA1)
    verify_hash("AE2 artifact SHA-256", digest(args.jar, "sha256"), EXPECTED_SHA256)
    verify_hash("AE2 artifact SHA-512", digest(args.jar, "sha512"), EXPECTED_SHA512)
    resources = verify_profile(project)
    verify_ae2_archive(args.jar, resources)
    verify_connector_artifacts(
        args.mega_cells_jar,
        args.expanded_ae_jar,
        args.advanced_ae_jar,
        args.extended_ae_jar,
    )

    print(
        "Verified AE2 19.2.17 M3d crafting: exact AE2 artifact, metadata, "
        "14 audited classes, 8 formed blocks, 15 textures and 30 disjoint "
        "resources; exact MEGA Cells/Expanded AE native-subclass connector "
        "set is 29 IDs; Advanced AE and ExtendedAE remain outside it; "
        "accepted main, ExtendedAE and M3c generated outputs are unchanged."
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
        print(f"AE2 crafting verification failed: {error}", file=sys.stderr)
        sys.exit(1)
