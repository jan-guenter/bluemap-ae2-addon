# Provenance

The machine-readable source-use record is
[`provenance/upstreams.json`](../provenance/upstreams.json). This document
explains its evidence and redistribution boundary.

The repository-only PRBM evidence analyzer has a separate exact format-source
record in [`tools/analyzer-upstreams.json`](../tools/analyzer-upstreams.json).
Keeping this tooling record outside the production manifest preserves the
byte-identical runtime candidate: neither the analyzer nor its provenance is
packaged in either the production or sources JAR.

## Exact current runtime evidence

The All the Mons 1.2.0 M4/M5 candidate uses the exact 8,230,896-byte
`appliedenergistics2-19.2.17.jar`. Its SHA-1 matches the current pack ledger and
Modrinth identity, and its bytes match the Maven Central runtime publication.
The same AE2 bytes occurred in earlier historical evidence, but byte equality
does not transfer a result between pack or host tuples.

| Digest | Value |
| --- | --- |
| SHA-1 | `49c18d6a4af487957d7e5a6ad5dcbf71090b8e14` |
| SHA-256 | `460d779a0609b81409907d9956de8f6f70a1b0912257e3e5c3c7e75ac9630e95` |
| SHA-512 | `55edfd948366aff620881e0625e48c333a2cb847e73249bc0b588efbc4b86709992a8ffbca97ea387e270df4186fe7f74ee2f27b739f1c952e932becfb9dea33` |

`tools/verify_pinned_artifact.py` checks all three complete-artifact digests,
size, Java class version, exact mod/dependency metadata, the empty stock
cable-bus model, all 85 cable IDs, the exact `ae2:terminal` face-part ID,
`ae2:drive`, the closed 23-item native Drive catalog, 158 texture digests and
the complete 196-resource M3a closure. The full sorted texture-digest aggregate
is
`c0e66d75cad06649b021f8a9073629d6619050c4f69e78c522b6fa32fb232242`;
the full resource-manifest SHA-256 is
`408297def444f1392b7b87fdc4b8520099513b4c57c63a4176b808ce61b4e1be`
and the generated profile SHA-256 is
`2c27976a718834dbc97b3eb7cac6543c4ad2a966737c7bccbadb2b1c39c837e8`.
The unchanged 170-resource M0-M2 core partition remains
`4f783945d92be446c8e5939f9455b24f9d463cb39f6b4e35e76c9b6fb713b3c2`;
the disjoint 26-resource Drive partition is
`a8d10416d0fce66d8a91ce9e0dc93a83d2f552da8762a0a90e183dc58f6745cf`.
The third-party JAR is an ephemeral test input and is never committed, cached
as a CI artifact or published.

## Applied Mekanistics exact evidence

Published `0.1.0-alpha.2` is additionally pinned to these operator-supplied
All the Mons `1.2.0` runtime files:

| Evidence input | Size | SHA-256 | Exact source correlation |
| --- | ---: | --- | --- |
| Applied Mekanistics `1.6.3` | 149,709 bytes | `8946fea39451dbce8e709dedbef40a52ba337bdf7a25ac0c4b503800b1bf0773` | tag `1.6.3`, commit `137f24bb9a46775ddd5a620055270b5e8a540f5a` |
| Mekanism mod version `10.7.19` (`Mekanism-1.21.1-10.7.19.85.jar`) | 11,976,009 bytes | `004dbc9f3106f4d192aeaa1ee1190dd16ec9ca8059ed3d093b80034f4c574f43` | exact source tag commit `a00109e4856fd38b9c5b3dd7f22ce4a59cd65a80` |

The AppMek metadata requires AE2 `[19.2.10,20.0.0)` and literally records
Mekanism `[10.7.14,11-)`. Both complete files must match before
`appmek-drive-cells` can activate. The exact resource closure contains six
Drive paths, 3,611 bytes in total. Runtime then
validates the resolved model and decoded texture semantics so a higher-priority
resource-pack override cannot bypass the artifact gate.

AppMek declares code `LGPL-3.0-or-later` and assets
`CC BY-NC-SA 3.0`; Mekanism declares MIT. This repository contains only
independently written adapter/model-decoder code plus identifiers, sizes,
digests, and semantic signatures. It bundles no AppMek or Mekanism class,
source, model, texture, binary, or JAR. All visual resources are resolved from
the exact operator-installed artifacts. The exact alpha.2 runtime gallery and
owner visual review passed on 2026-08-19; the owner separately authorized
publication. The owner-accepted alpha.1 section below stays historical and
byte-bound.

## Ars Energistique registration-hook evidence

Version `0.1.0-alpha.3` adds an independently written data-only API. The Ars
Energistique example records only owner-supplied identifiers and model paths;
the host packages no Ars Energistique or Ars Nouveau class, source, model,
texture, binary, or JAR. The soft-dependent add-on owns the exact artifact and
resource gate and activates its immutable route only after that gate passes.

Evidence `atm120-arseng-owner-accepted-2026-08-25` records the bounded staging
facts: All the Mons `1.2.0` loaded exact Ars Energistique `2.1.1-beta` with AE2
`19.2.17` and Ars Nouveau `5.13.0`; the owner accepted 25 cells, including
three registered part types across six faces and five source cells in one
Drive. The public BlueMap view was agent-sanity-checked and owner accepted.
This record makes no production claim and adds no Ars artifact to the host's
M45 verifier.

## All the Mons 1.2.0 M4/M5 exact evidence

The completed M4/M5 implementation is bound to the following exact external
runtime inputs from the All the Mons `1.2.0` closure. The verifier checks each
complete file by size, SHA-1, SHA-256, and SHA-512; SHA-256 is shown here for
compact review.

| Evidence input | Size | SHA-256 | Declared license |
| --- | ---: | --- | --- |
| AppliedFlux `1.21-2.1.5-neoforge` | 345,117 bytes | `57e6a2c0f38e660c9e8416f9081d8c515f5ad096d6793d7b7f039e8e210d245b` | LGPL-3.0 family |
| ME Requester `1.21.1-1.4.3` | 184,517 bytes | `68f3c861a802d48afeb6e3a48e8ee4f8633904340ac3f89f17493dc84490e385` | LGPL-3.0 family |
| Expanded AE `2.1.1` | 496,713 bytes | `f39c0eb9c6271f54a44ffee092a29520f53000d1005849e6afada3ad9dffba14` | LGPL-3.0 family |
| MEGA Cells `4.11.0` | 1,137,276 bytes | `a386bbf12afb11729b0dcf77f64221893d250f22e6185a4d728b9799b230bc55` | LGPL-3.0 family |
| Advanced AE `1.6.12-1.21.1` | 4,791,255 bytes | `a01d9718667ac13899013e91c5b0b7708b9b9db1da9b8e380772dde54bbe8f41` | LGPL-3.0 family |
| Athena `4.0.6` | 99,944 bytes | `43699885bbce3343916d4c5c4940cf0e3f9f6f02fdeb46e8655e121b42282ec5` | MIT |
| ExtendedAE `1.21-2.2.35-neoforge` | 5,578,031 bytes | `14a2860fa2c747e9dda2279b8933fac6311fecfee166c765171022b902591c65` | LGPL-3.0 family |

`tools/verify_m45_artifacts.py`, invoked by
`verifyM45PinnedArtifacts`, additionally verifies eleven exact resource
manifests with 375 rows and the sorted 67-row MEGA Cells cell-model catalog.
The resource manifests cover 19 AppliedFlux, 12 ME Requester, 142 Expanded AE,
82 MEGA Cells, seven dependent AE2, 60 Advanced AE/Athena, and 53 ExtendedAE
rows. The MEGA cell catalog maps 67 exact items to 37 model identities. This is
an exact input and source-use gate, not a reproducible-build claim for any
upstream project.

The eight route partitions are `appflux`, `merequester`, `expandedae`,
`megacells`, `advanced-ae-quantum`, `advanced-ae-athena`,
`extendedae-matrix`, and `extendedae-planes`. The implementation records only
identifiers, sizes, digests, bounded semantic catalogs, and independently
written runtime geometry. No upstream JAR, class, source file, JSON model,
texture, animation metadata, client capture, or precomputed mesh is bundled in
the production or sources JAR. Every referenced visual resource is resolved
from the operator-installed exact artifacts.

The Java runtime audit and M45 verifier above passed. Two byte-identical builds
each ran 562 Java tests (560 passed and two opt-in exporter tests were
intentionally skipped) and 180 pre-oracle Python tests. After the exact runtime
oracles were frozen, the CPython 3.13.14 suite passed 192/192 tests in 945.343
seconds, and the generator/checksum closure passed.

The exact owner-accepted local identities are:

| Artifact | Size | SHA-256 |
| --- | ---: | --- |
| Production JAR | 1,207,683 bytes | `6fed7a625b02229213a047788995944f14e7e7fcabe0e0ddc6d9b5e994146e9f` |
| Sources JAR | 532,979 bytes | `4a45c60f8512630c6bd9735e26018d019ebe99d58f2c87fa2f3c46e101b624d8` |
| POM | 1,637 bytes | `967132ef80201099cfb1a798f03ff1ac37e0ac84a551694d7276ac20c7ccc136` |
| Gradle module metadata | 2,861 bytes | `3f0ba24c34ef535c99cbd6dabcd7d6bb0f784ca6ffd032f06faaf9a9b5d7b0b8` |
| Schema-11 gallery ZIP | 94,537 bytes | `c67b4f794092f6e994349a8ee9320c052e2efc87f04e8813faf158c3455fe33b` |
| Schema-11 `cases.json` | 6,017,554 bytes | `914dab6931077521959cf59260a1ffb0cdbe105385f43880763b289f8117ec55` |
| M4/M5 main runtime oracle | 221,769 bytes | `c2ce69bed949306551ca4ff6cdebf7fac88f0f2f2fa7ab294d3312f363e1b448` |
| M4/M5 schema-10 legacy-upgrade oracle | 2,336 bytes | `2319ecf576ba07b123078c720d941990fac939033d375e5853f51bf98348c3c7` |
| Enabled map archive | 20,821,895 bytes | `44422aa71c2f450951d8433e25e01de7a0b00dbd0d9c4fa4ff74ca98e649a2df` |

Schema 11 contains 158 cases and 1,366 anchors. The main oracle locks 391
custom M4/M5 anchors, 23,334 triangles, 122 resources, and 2,089 material rows;
the legacy-upgrade oracle locks three retained positions, 282 triangles, 20
resources, and 26 material rows. The isolated lifecycle produced these exact
byte-identical cold/warm report and comparable-manifest pairs:

| Mode/phases | Report | Report SHA-256 | 79-row manifest SHA-256 |
| --- | ---: | --- | --- |
| Initial and restored enabled cold/warm | 7,695,130 bytes | `6aaa925961ace51dfd2a7e074f9745d78e4aae28a334425f55498fa2bb5038fc` | `97a4f0915d6698e0962d6355f0835a2286a3ee7cae983ad1f3514c9ecb76b46f` |
| Combined-disabled cold/warm | 6,760,492 bytes | `acc180648556ba2a0fc4115172ba04aad4e0c266358379bf75f80f5353c62aa2` | `d457c23adc189c0e03718aad5c09649a7db63c1efb0174b439432df9f480ac56` |
| Crafting-disabled cold/warm | 5,956,875 bytes | `8b2b7b3b857c88f1c201399cffeeead588ac78e3ae3023d64ca4c3952a8b1593` | `dd86e95e7001ddb22e355f6910d7e7d3ded032d4eb2fcedc1a5ffd2deb1258cc` |
| Native-structural-disabled cold/warm | 5,697,202 bytes | `0742cc20f7fd7af776f1a342c52206e6ee6107c758146b9376358019b8a34a10` | `680ae50473f9829f396fc09f260e28221f283eda30a2b438688a81ca9788a668` |

Full-JVM transitions, restored-enabled equality, and the restart, initializer,
worker, one-build, and settle gates passed. The exact candidate aliases on
`data-atm120` are `ae2-addon-m5-candidate.jar`,
`ae2-m5-gallery-candidate.zip`, and
`ae2-m5-enabled-candidate-map-2026-08-11.tar.gz`. Hash-exact accepted aliases
`ae2-addon-m5-accepted.jar`, `ae2-m5-gallery-accepted.zip`, and
`ae2-m5-enabled-accepted-map-2026-08-11.tar.gz` coexist on `data-atm120`.
The owner accepted those exact artifacts in BlueMap and separately authorized
publication on 2026-08-12. M4/M5 is the latest accepted checkpoint and S1 is
its accepted predecessor.

## Source correlation

AE2 tag `neoforge/v19.2.17` resolves to commit
`79ee2c704ad62941a426c26b1cb1f76ef5b2ee5a`. The official Maven sources JAR
has SHA-256
`d2f451203cb61c2d21fae52c683083d2f72441ca7d26725f4df5934290492e6a`.
The following M0-critical source files are byte-identical between that sources
JAR and the exact-tag checkout:

- `CableBuilder.java`;
- `CubeBuilder.java`;
- `CableBusBakedModel.java`;
- `CableCoreType.java`;
- `AEColor.java`;
- `AEParts.java`;
- `CableBusContainer.java`;
- `CableBusBlockEntity.java`;
- `CablePart.java`.

This establishes `T4_SOURCE_CORRELATED` evidence for the M0 behavior. It is
not a reproducible-build claim for the complete AE2 binary.

M1's exact-version audit additionally locks the exact-tag blobs for
`AECableType`, `AECableVariant`, `AECableSize`, `CableBusRenderState`,
`SmartCableTextures`, all six relevant cable-part classes,
`InWorldGridNode`, `AEBasePart`, `VisualStateSaving`,
`StructureTemplateMixin` and `FacadeContainer`. Those sources establish the
color/family compatibility matrix, saved-data boundary, channel-overlay
policy and conservative attachment/facade fallback used by the M1 design.
Their full paths and Git blob identities are in the machine-readable
manifest. This still does not establish a reproducible build of AE2 or a
general compatibility claim beyond 19.2.17.

## M3a Drive audit evidence

The implemented M3a profile is based on the exact-tag blobs for
`StorageCellModels`, `AEItemIds` and `InitStorageCells`. Together with the
exact runtime-class digests and the resource partition, these establish the
bounded 23-item-to-12-model catalog, the generic chassis used by matter cannon
and color applicator, and the native-vs-extension boundary. The Drive
partition contains one blockstate, 15 models and ten textures; it is identified
by SHA-256
`a8d10416d0fce66d8a91ce9e0dc93a83d2f552da8762a0a90e183dc58f6745cf`.

This audit does not claim that ordinary Anvil data contains live Drive state.
The implementation deliberately renders black `static-offline-unknown` LEDs
and ignores bounded component contents. Unknown items, including extension
cells, use atomic whole-block original-resource fallback. The exact
259,005-byte M3a JAR with SHA-256
`55a11805373aebfde821e5009723ec7d672fb290127dbc60131ffa344c99518a`
passed its reproducible build, source tests, persistence probes and initial
enabled/physically-absent-stock phases. The restored-enabled phase subsequently
verified the unchanged fixture without rebuilding and reproduced the initial
report and 44 comparable files byte-for-byte, closing the technical lifecycle.
The owner visually accepted this exact M3a artifact and bounded gallery on
2026-08-07.

The packaged machine-readable provenance in that exact M3a artifact records
its build-time checkpoint before runtime validation. Changing it would create
a different JAR, so this unpackaged document records the later technical and
human evidence.

## M3b ExtendedAE audit and post-build evidence

The exact pack-pinned ExtendedAE runtime artifact is
`ExtendedAE-1.21-2.2.33-neoforge.jar`, 5,573,972 bytes. Its bytes were matched
to the fresh All the Mons 1.1.1 Prism instance:

| Digest | Value |
| --- | --- |
| SHA-1 | `e87867bffee36a28f9f4493f7bb7e7a5109a480f` |
| SHA-256 | `6652ed1ea4b71f585d48c05a195a77594a7a2bd1ecea0fc805db2122aafad734` |
| SHA-512 | `a61c6f606b5d0a27857b55b8fc6a670352d91f19d2a2dadd2d650f08ae6682f437e7b18a80c5e26122bbf7b70b007851f1aaa90442fce16d8729c71c1ec10225` |

ExtendedAE tag `1.21-2.2.33-neoforge` resolves to commit
`90005ee29839fb9fa83bbe6544919c722f8b0dc6`. The exact-tag Ex Drive JSON
resources match the runtime artifact semantically; line endings explain the
observed textual differences and the base model is byte-identical. Exact
runtime class and resource digests remain primary evidence. This correlation
is `T4_SOURCE_CORRELATED`, not a complete reproducible build of ExtendedAE.

The exact source/class audit covers `BlockExDrive`, `TileExDrive`,
`ExDriveModel`, `ExDriveBakedModel`, `ExDriveTESR`, the AE2-reflection helper,
the registry/singleton/client registration paths and built-in infinity/void
cell implementations. Their exact Git blobs and runtime class SHA-256 values
are retained in `provenance/upstreams.json`. This establishes the twenty-slot
front/rear layout, three built-in cell IDs, source orientation behavior and
textureless position/color LED boundary used by M3b.

The disjoint ExtendedAE input closure contains one blockstate, six models and
eight runtime textures across 15 resources and 13,242 bytes:

| Contract | SHA-256 |
| --- | --- |
| Resource manifest | `5e72f79f45a3b120a89cf8b7a1fa15ce41bebaae62a63c6f3305ef40bd5d24ee` |
| Texture manifest | `b3de9aede1d2fb8854925a11397009d7458084071d5d5acc293913e27d29b75e` |
| Generated profile | `eab467f46c27974e1f7d54fe749366b92eacd8d63d57bad1e8f3e452d82ad1df` |
| Dependent AE2 Drive partition | `a8d10416d0fce66d8a91ce9e0dc93a83d2f552da8762a0a90e183dc58f6745cf` |

No ExtendedAE source, class, JAR, model, texture, captured mesh or precomputed
mesh is bundled. The add-on packages only local code, synthetic dispatch,
identifiers, sizes and digests; all upstream resources remain supplied by the
operator-installed exact mod JARs.

The exact 323,416-byte M3b candidate JAR with SHA-256
`f02123cb602bb7b6466d1529c5518e45862f53f413ce9a75ecc067d1a30607d1`
passed two reproducible builds, 217 Java/56 Python tests, one-build NBT
persistence and the complete enabled/extension-disabled/physically-absent-
stock/restored lifecycle on 2026-08-07. Enabled cold/warm/restored reports and
44-file manifests were byte-identical. Extension-disabled retained the exact
accepted M3a subreport while all 36 M3b anchors stayed empty. The stock pair
selected only the stone control. On 2026-08-07 the owner visually accepted the
exact JAR, exact 37,614-byte gallery ZIP with SHA-256
`69bdb99d9c8f6838c3b8d5847c32702761cfa77b263ee95384ca24357c84cf92`
and exact 20,351,418-byte map archive with SHA-256
`c73844990847148d9cd3d315832085e49776e9253c5c8eca6f0b7659d73c4285`.
That acceptance closes M3b only. The accepted M3b artifact contains no M3c
behavior.

The packaged `NOTICE.md`, `THIRD_PARTY.md`,
`META-INF/bluemap-ae2/upstreams.json` and support matrix in this exact JAR
intentionally retain the build-time M3b-pending checkpoint. Updating them after
runtime would change the tested JAR identity. This unpackaged document and
`STAGING.md` are the post-build evidence record; the frozen packaged status is
not a claim that the lifecycle and later human acceptance were never
completed.

## M3c connected-quartz-glass audit and accepted post-build evidence

The M3c implementation is bound to the same exact AE2 19.2.17 runtime JAR and
tag commit recorded above. Its source/class audit covers `QuartzGlassBlock`,
`GlassState`, `GlassModel`, `GlassBakedModel`, `RenderHelper`, the built-in
model registration/hooks and exact runtime block registration. Corresponding
Git blob and runtime class SHA-256 identities are retained in
`provenance/upstreams.json`.

The disjoint M3c input closure contains two blockstates, one dynamic-model
identity and 19 runtime textures across 22 resources and 4,187 bytes:

| Contract | SHA-256 |
| --- | --- |
| Resource manifest | `b51c708e7c4d26093c1b6f85b88d0be50572d3cfa76dbf802720f6ad79c7a7fa` |
| Texture manifest | `65005c9b76800cdeba5c4598472a44dea131c9974672f89bf421452755fefb6a` |
| Canonical resource partition | `3704e90b1c8ec9ee5a7d7215995869500b50c9b61a797584f6732713dab7103d` |
| Generated route profile | `548e5bc00ef07c6d6b93b346422b596882ec11ca03de006065fa45fecb991200` |

The accepted M3a main profile and manifest remain byte-identical at
`2c27976a718834dbc97b3eb7cac6543c4ad2a966737c7bccbadb2b1c39c837e8`
and
`408297def444f1392b7b87fdc4b8520099513b4c57c63a4176b808ce61b4e1be`.
No AE2 class, JAR, model, texture, capture or precomputed mesh is bundled; the
add-on records identifiers, sizes and digests and resolves operator-installed
resources at runtime.

The local renderer/profile and schema-6 evidence lane implement exact native
six-neighbor topology, face-local masks, position-seeded legacy RNG,
asymmetric UV clamps, CUTOUT/no-AO geometry, vibrant emission 15 and
BlueMap-native culling/light/cave/map-color behavior. The representative
gallery adds 11 cases, 47 anchors, 776 triangles and all 19 resources; Java
tests cover all 64 connection masks. The complete gate passed 247 Java and 71
Python tests.

Two clean builds reproduced the exact local accepted build artifacts:

| Artifact | Size | SHA-256 |
| --- | ---: | --- |
| Production JAR | 375,558 bytes | `4c1b557ae4c79c738005b74e2f0c89ca4fbe503dd6ef0ba614fae34d8e449d47` |
| Sources JAR | 186,080 bytes | `6b011719264229629fb1011f4c6de566bf16b13603a6c2412b4ed4ced70a4036` |
| POM | 1,637 bytes | `967132ef80201099cfb1a798f03ff1ac37e0ac84a551694d7276ac20c7ccc136` |
| Gradle module metadata | 2,859 bytes | `5922575b3002d2cc48e1e5e2fa6a795b8b7f11349b1bb0d04f17db7c0b182876` |
| Gallery ZIP | 38,929 bytes | `0839009fe6a4f4785f864f33bc97fef28b8418f077d5d66a20efc3e8eeb4edab` |

The exact 1,517,248-byte schema-6 `cases.json` has SHA-256
`2d4fbba58ea2c4d3ed741e93a8dd9857523cac9cda021ffd3111e6ac51aec602`.
The complete isolated technical lifecycle passed on 2026-08-08. Enabled cold,
warm and restored 1,582,336-byte reports were byte-identical with SHA-256
`e81b8e6eed2047629a933a21e0e345c4880db2b12e264455fa84ef59b63d824f`;
their 44-file manifests were byte-identical with SHA-256
`3257a86e895956d7701f056ef46f3188fb4fca2f704b8c7e67641164666221f3`.
Extension-disabled, glass-disabled and physically add-on-absent stock
cold/warm pairs were independently byte-identical and proved their exact route
boundaries. Their report/44-file-manifest SHA-256 pairs were respectively
`bd540b437d67830a4899c4437eb396b7229104cc2afe89e5c0a4a7e8d5cbfe32` /
`6ac6e2578374d34908cdddf2a451706ac7bc7d1d2831a66757fe6c652015614e`,
`34a488beb4cc1d4ce6dfee2183e61839304b626ad9b5360f6e302687c4d2b442` /
`9d7c9f196eb3e89acadf7eb7bfb1cf4ad85da6d8a4403f7ea4d4e9810ad7250b`
and
`08436ad0d03e37a3578552935efc1a6dd5743f5ed8de75d923d2fdf518ff2b0d` /
`738d29ddceaa000c867d0616fa9a389010a19672404e76b9f9644368cd4d15fa`.
Restoration reproduced the enabled report and manifest without
rebuilding the one-build, zero-failure fixture. Packaging the restored 44 files
twice produced the exact 20,376,253-byte archive with SHA-256
`3fb5fb174f23c0f2d8ce9f98e8c12feb8b12c444060e35b9d8e0036d8ec165e5`.
Every accepted capture pod had zero restarts and both render threads
accumulated CPU time. One operator Ctrl-C while detaching the console caused a
restart attempt during pre-lifecycle setup; it preceded the accepted captures
and was not an add-on restart.

On 2026-08-08 the owner visually accepted the exact 375,558-byte production
JAR, exact 38,929-byte gallery ZIP and exact 20,376,253-byte map archive. Their
SHA-256 identities are respectively
`4c1b557ae4c79c738005b74e2f0c89ca4fbe503dd6ef0ba614fae34d8e449d47`,
`0839009fe6a4f4785f864f33bc97fef28b8418f077d5d66a20efc3e8eeb4edab`
and
`3fb5fb174f23c0f2d8ce9f98e8c12feb8b12c444060e35b9d8e0036d8ec165e5`.
That bounded decision completes M3c. The candidate PVC aliases remain, and
the hash-exact accepted aliases are `ae2-addon-m3c-accepted.jar`,
`ae2-m3c-gallery-accepted.zip` and
`ae2-m3c-enabled-accepted-map-2026-08-08.tar.gz`. These aliases are staging
conveniences, not publication or durable provenance storage.

The exact client uses reciprocal `getAppearance` calls and can connect a
cross-mod appearance proxy. BlueMap 5.22 does not expose that query, so known
non-native neighbors are treated as disconnected and only missing or malformed
native state falls back atomically. AE2's `GlassBakedModel` leaves the baked
quad `shade` flag false; BlueMap's seven-attribute PRBM has no corresponding
field and its web shader applies normal-based directional darkening. This is a
host-format limitation, so no pixel-identical directional-lighting claim is
made. Client particles/items are outside the world-block renderer.

The packaged `NOTICE.md`, `THIRD_PARTY.md`,
`META-INF/bluemap-ae2/upstreams.json` and support matrix in this exact M3c JAR
intentionally retain their build-time pre-runtime/pending checkpoint. Updating
them after the lifecycle would change the tested 375,558-byte JAR identity.
This unpackaged document and `STAGING.md` therefore record the post-build
technical and human results. Backfilling that acceptance into packaged files
would create a different, untested JAR.

## M3d formed-crafting audit and accepted post-build evidence

M3d is bound to the same exact AE2 19.2.17 runtime and source identities
recorded above. The route profile covers eight native crafting blockstates,
seven formed models and fifteen textures across 30 exact resources and 6,177
bytes. The generated route profile SHA-256 is
`676f63473b4827bb952a7c1f3fb457a6a03c3bfd0fb4b29a122b9f57468ba0f7`;
the required-resource manifest SHA-256 is
`dc474ba6ce7c4c2d53778827b1c1f9b4994594ea984ed7a2cbd62c40e1bc1183`
and the texture-manifest SHA-256 is
`a9a2a1ed912f562362d581cbd219b40afd4c884452a0c64cee3d015dfdc81620`.
No AE2 model, texture, class, JAR, client capture or precomputed mesh is
bundled.

Two clean builds reproduced the exact 448,915-byte JAR with SHA-256
`ca057f025338150255ea916402c08bc8b614f9398a063e7433bbe468808c93ee`
and the exact 213,004-byte sources JAR with SHA-256
`3e95cabf3e9dcf4ab5c8c2b6d6661ba6464a6cb3e6abd6d33fcfb904b5197c4f`.
All 285 Java and 85 Python tests passed. The exact 44,201-byte gallery ZIP has
SHA-256
`4a18b45f2c03c8d1d3c49a731df2c2503745952faccf9ba06ec8f301909b81f3`;
the 3,030,512-byte schema-7 `cases.json` has SHA-256
`c60d2afff5a1f92da4972963fcb926c38093f43bb6d7f550799f104349728a38`.
It embeds the exact accepted schema-6 view at SHA-256
`2d4fbba58ea2c4d3ed741e93a8dd9857523cac9cda021ffd3111e6ac51aec602`.

The complete technical lifecycle passed on 2026-08-08. Every cold/warm report
and 44-file manifest pair was byte-identical; restored enabled cold and warm
also matched the initial enabled pair exactly. Report/manifest SHA-256 pairs
were: enabled `f23f43997d680425225706982d504e8df07fb4145543e5cb900b2e6f2dfcdff0` /
`06b533030f416bdd1f772cbdcc45ffa3acece219758ddd725e1a637617fcbcc5`,
extension-disabled `fdf01d9225af70361fd0f9862a689f67e466698f65d2882c20d95b6182d484eb` /
`42c332a6ce704db2a5fbc1d4f36ac08fea685fba82deb1408653abfb6410adce`,
glass-disabled `ef6a7c232fb16fa065fb4c7b4ed90f9d3f1b1a6f73c8598094774ee03b63251a` /
`f6191ce6392fca2bdabfe2fc2afede756ada6ffb13ca152cdbc1b136924d6338`,
crafting-disabled `a8e44c392c585df611f0c818767cba3a4ed20bd83e717ae7c1f3c9de09e9df0b` /
`ec89cca1a43fdc2764ee667609ced69cec182d9d298072d219fdb567c9545e25`,
and physically absent stock
`7253151c5b0188ca238d14fc684a94c4a52392e9ba13f312d76643683840e9b6` /
`f3044cc295312b37bfa9b7626c9ff7ec66e1f12601f97484cca17efca00d688b`.

The fixture remained at one build, zero failures and two stable checks through
delayed verification, actual unload/reload, full JVM restarts and every route
mode. Both render threads advanced by 313 and 520 scheduler jiffies, and
capture pods had zero restarts. One operator sequencing attempt failed during
init before Minecraft started; it was not an add-on or capture restart. The
exact 20,417,822-byte enabled map archive has SHA-256
`672cdffaf5135f34c4b10c24638056540dcaadbb5fd2d78b3096897436d8a2c6`.

On 2026-08-08 the owner visually accepted the exact JAR, gallery and map
archive identities above. The PVC aliases `ae2-addon-m3d-accepted.jar`,
`ae2-m3d-gallery-accepted.zip` and
`ae2-m3d-enabled-accepted-map-2026-08-08.tar.gz` are hash-exact acceptance
conveniences. The corresponding candidate aliases remain retained. Neither
alias set is publication or durable provenance. The packaged notices,
provenance, support matrix and build metadata retain their frozen build-time
checkpoint; they must not be rewritten to backfill this post-build result.

## M3e quantum-bridge audit and post-build accepted evidence

M3e is bound to the same exact 8,230,896-byte AE2 19.2.17 runtime JAR,
official 3,814,167-byte sources JAR and exact tag commit recorded above. The
source-correlated route covers only `ae2:quantum_link`, `ae2:quantum_ring`,
their `formed`/`waterlogged` states, expected block entity ID
`ae2:quantum_ring` and complete isolated native `3x3x1` structures. It makes
no extension-connector claim and interprets no persisted block-entity payload
as power state.

The exact 3,873-byte generated route profile has SHA-256
`21afa152e3f56d8bdde9f602748c0efbca52a2c55d5dd7a836adca267c65480e`.
Its 1,513-byte, 13-row required-resource manifest has SHA-256
`717eed1ada75fb43c1324792c147cd8c2308d8c73ee82bf52d8de6bad4f74ed9`
and binds exactly 3,798 bytes: two blockstates, three source models, six PNG
textures and two animation metadata files. The source audit includes both
animated light textures, while the `static-off-unknown` renderer emits only
the link, ring, transparent-glass and transparent-covered textures. No AE2
source, class, JAR, model, texture, client capture or precomputed mesh is
bundled; every resource is resolved from the operator-installed exact JAR.

The family-private renderer ports exact source-float bounds and arithmetic,
complete XZ/XY/YZ topology inference, `CubeBuilder` bounds-mapped UVs and
winding. It applies neighbor-derived ambient occlusion and BlueMap host light.
Quantum particles, animated power overlays, transient network state and item
rendering are excluded.

Two clean builds reproduced all four publication-format artifacts
byte-for-byte without publishing them:

| Artifact | Size | SHA-256 |
| --- | ---: | --- |
| Production JAR | 513,674 bytes | `98ff55eaba609fc894b01e0c4d922b47f1871c324945f88f7a34864cf48b124f` |
| Sources JAR | 234,963 bytes | `2bc749373eeb29bd30b9edb58006c7248da1cc09a6abdc7abb404b86a4045a1e` |
| POM | 1,637 bytes | `967132ef80201099cfb1a798f03ff1ac37e0ac84a551694d7276ac20c7ccc136` |
| Gradle module metadata | 2,859 bytes | `d1da10c42393c8a9cb79b77ad67a0b3d15140ef58fef593b20f25710fc8b0e02` |

All 316 Java tests in 53 suites and 98 Python tests passed. The deterministic
45,009-byte schema-8 gallery ZIP has SHA-256
`498bac2f82b78451eb24da416ded1d625e5785cc5d3e5910b4c34bfecc05c390`;
its 3,123,572-byte `cases.json` has SHA-256
`93963dd0bb60a276e1a17c6dd1f4eb916cd92bef4ef30a2e8bdc7a2bfa818b3e`.
The gallery contains 115 cases and 519 anchors, including three M3e cases, 27
anchors and 1,188 triangles. Its accepted schema-7 projection remains frozen
at SHA-256
`c60d2afff5a1f92da4972963fcb926c38093f43bb6d7f550799f104349728a38`.

The isolated technical lifecycle passed on 2026-08-08. Every cold/warm pair
was byte-identical, and restored enabled exactly reproduced initial enabled.
Report/44-file-manifest SHA-256 pairs were: enabled/restored
`d306aff2a0f2eca2882a3d52426140b8caf98aeda76d0722dd14b59ea8a5e9e9` /
`b2d61c1dea7bb10dbfbb07a62fd27d1a1a380ca2fbf1b49e06a9f4f232400ca5`,
extension-disabled
`d871f95fd67e9e805829c37260e3dc43558cc4e388332d9771d388857a088f28` /
`aef3b77514db808a5013c7692f481121f58cc6ca2122627f103216504880da59`,
glass-disabled
`9a5c1103b55d6637d2b954c681dc15f333d944d2f59d983da2d1bf2645ad405c` /
`9e1b684530df1b7c8256fce6cefa8832e4e9327e353cf45dbda95d5aaddda7ab`,
crafting-disabled
`3b103035582b4db56dd81e08a3e20b7c80f839246f7d6a380a542b26994e5205` /
`4de9c9bdb5dadca8ca26d07d78c7743f3a3d67ef6d67a29979d3be8143caf76a`,
quantum-disabled
`13c4ca3676868418f9f6797e5b4b4816a38d7ed2c518e1cbb329063ff005e20b` /
`f26045142b4aa0df1f262abe95302e7341cb2ceca88fed60d2340d39269178b6`,
and physically absent stock
`0c3e4333782c244779a55201ab7a6362942e763c858bfdca2348b48204ce5e40` /
`1cf7a62eb02a3cc6af5fb3b550c9aee40cfaebf521ededf41b2db95018f39c53`.
Every manifest has 44 entries and 3,893 bytes.

The fixture remained at one build, zero failures and two stable checks through
delayed verification, unload/reload, datapack reload, complete JVM restarts,
every route mode and restoration. Capture pods had zero restarts, and the two
render threads advanced by 587 and 245 scheduler jiffies. Packaging the same
restored 44 files twice produced the byte-identical 20,424,799-byte archive
with SHA-256
`9e145fffbe87205651ed7cc6b4cb706b7dcbe394ac26e7ce2eb1d6d55ea411a7`.

The exact candidate aliases `ae2-addon-m3e-candidate.jar`,
`ae2-m3e-gallery-candidate.zip` and
`ae2-m3e-enabled-candidate-map-2026-08-08.tar.gz` remain byte-identical to
the hash-exact accepted aliases `ae2-addon-m3e-accepted.jar`,
`ae2-m3e-gallery-accepted.zip` and
`ae2-m3e-enabled-accepted-map-2026-08-08.tar.gz`. Packaged notices, provenance and support
metadata truthfully preserve their build-time local-implementation/synthetic-
gate checkpoint and therefore predate the technical lifecycle. This
unpackaged document records the later evidence without changing the tested JAR.
The owner visually accepted the exact JAR, gallery and archive on 2026-08-08.
M3e was the latest accepted slice at that checkpoint and M3d remained its
previous rollback checkpoint. Their candidate and accepted aliases remain
unchanged by the later M3f work. Publication remains blocked.

## M3f M3-completion audit and accepted post-build evidence

M3f is bound to the same exact 8,230,896-byte AE2 19.2.17 runtime JAR,
3,814,167-byte official sources JAR and exact tag commit recorded above. The
exact verifier locks 30 runtime classes and 30 byte-identical source files for
persisted paint, both Sky Stone chest variants, crank, Inscriber and spatial
pylons. It also proves six native block IDs, five bounded block-entity
identities, 17 source textures, 15 emitted-static textures, two fallback-only
textures and a disjoint 33-resource/22,491-byte closure. Contents, held items,
fluids, activity, global spatial-pylon validity and extension connectors are
not inferred or claimed.

The exact 9,405-byte generated `ae2-m3-completion` profile has SHA-256
`281a335d3024ebbb97c6268e768826c467d6f7ea660989fd3dae204c6c03abf3`.
Its 3,738-byte, 33-row required-resource manifest has SHA-256
`3faf7f29e2878f5525541bad855cbc66b6d45786dc8fc6ee29a6fbbf4878cca1`.
All previously accepted M0-M3e generated profiles and resource manifests stay
byte-identical, and no upstream source, class, JAR, model, texture, client
capture or precomputed mesh is bundled.

The prior M3f candidate was withdrawn after owner visual review exposed
invisible L/T pylon members. It was never accepted. The exact-source-corrected
artifact below supersedes it and was visually accepted on 2026-08-09.

Two clean builds reproduced the exact local accepted artifacts:

| Artifact | Size | SHA-256 |
| --- | ---: | --- |
| Production JAR | 623,591 bytes | `ca67c0fc433e43f8e0801ed8d2cccfe47aae317fbc329c099bc8cd741ec3b42b` |
| Sources JAR | 276,986 bytes | `2a3bb3713ff56731992d405a58fc6a137dcfc8fff43467de7196ad33c444795c` |
| POM | 1,637 bytes | `967132ef80201099cfb1a798f03ff1ac37e0ac84a551694d7276ac20c7ccc136` |
| Gradle module metadata | 2,859 bytes | `106df734036c9f72f0463a03e2a282d430653edab5e761b165b51fe99f937d7a` |
| Schema-9 gallery ZIP | 49,679 bytes | `21ceec072cc3263a41bdb81874e897d48d5a1ce5e1c7d3ac3c0de3063818ee6c` |
| Schema-9 `cases.json` | 3,314,082 bytes | `75e6ba2f40631a95f20cfa00d7ca952e521bc2c7a4eb155926334a223a945f3a` |
| Packaged support matrix | 9,684 bytes | `71ec7977c7990678b9f34b27b976a0a1381b85a292afd409556f8190cee88863` |
| Packaged provenance manifest | 84,990 bytes | `dd3bda10236288c5a8e745978e7f55467961bf3aab1a4cb8ab527e8b1eec6e50` |

All 365 Java tests and 116 Python tests passed. Exact-source review corrected
the pylon boundary: an uncached native-axis scan capped at 256 pylons renders
every member of a fully observed invalid L/T component as AE2's unformed
`NONE` role with BASE plus DIM; only missing, malformed or capped observation
uses atomic original-resource fallback. Schema 9 embeds the accepted schema-8
M0-M3e projection byte-exactly and adds seven cases and 78 anchors. All 78 M3f
anchors are custom, own 2,822 triangles and emit 15 static resources; the 17
pylon anchors own 408 triangles. Across all 122 cases and 597 anchors, 579
custom anchors own 26,580 triangles and 17 atomic fallbacks own zero triangles.
The separate stone control brings selected output to 26,590 triangles.

The isolated technical lifecycle completed on 2026-08-09. Every cold/warm
pair was byte-identical, restored enabled exactly reproduced initial enabled,
and each retained manifest contains the same 44 unique rehashed paths:

| Mode/phases | Report | Report SHA-256 | 44-file manifest SHA-256 |
| --- | ---: | --- | --- |
| Enabled cold/warm/restored | 3,783,797 bytes | `7022a33448dab364cb825a8d67359795560b6a8793b64544ccb0b4c1fda7484e` | `259af4eea91a32acc07d1572e8f3f42e6276b46999496d0380ec009c10970fd8` |
| M3-completion-disabled cold/warm | 3,646,167 bytes | `c9391aacb0fa7e268b67bc4723ffa8958593acbf06e529542c63fc7fa59ba707` | `34bad6b250e18217485d68dfb989cfbc6d9b53f480be71c534f91675dc3c6a3e` |
| Physically add-on-absent stock cold/warm | 614,214 bytes | `30439e78593dacc43a0d3822f039ebbbd058d83be64023650471dab07179c4bc` | `b66f1e00f658d8fbd6ed842aae614b5db7bc94580215b2ad22a2a769e4d73efa` |

M3-completion-disabled renders the exact stock projection at 38 M3f anchors
and leaves 40 M3f anchors empty while retaining the accepted M0-M3e routes.
Physical stock has the same 38-rendered/40-empty M3f split; together with the
ten-triangle stone control it owns 1,882 triangles across five resources and
leaves the other 558 anchors empty. The restored pod had zero restarts and
restored the exact candidate JAR and gallery identities above. Packaging its
44 files twice produced byte-identical 20,450,880-byte archives with SHA-256
`e66abf203481c5df0fa0fc0062c414876f9ef6428cd637de0795f821496c51a9`.

The M3f candidate aliases are `ae2-addon-m3f-candidate.jar`,
`ae2-m3f-gallery-candidate.zip` and
`ae2-m3f-enabled-candidate-map-2026-08-09.tar.gz`; all three are hash-exact to
the artifacts above. The hash-exact accepted aliases are
`ae2-addon-m3f-accepted.jar`, `ae2-m3f-gallery-accepted.zip` and
`ae2-m3f-enabled-accepted-map-2026-08-09.tar.gz`. The M3e candidate and
accepted JAR, gallery and map-archive aliases remain hash-exact and unchanged.
On 2026-08-09 the owner visually accepted the exact corrected JAR, gallery and
map archive above. That bounded decision completed M3 and made M3f the latest
accepted slice at that checkpoint; M3e was its previous accepted rollback
checkpoint. S1 subsequently implemented the former post-M3 structural step as
the human-accepted local checkpoint recorded below. The owner-accepted M4/M5
checkpoint follows independently under the exact All the Mons 1.2.0 evidence
recorded above.

## S1 native-structural provenance and accepted local checkpoint

S1 is bound to the same exact AE2 19.2.17 runtime/source identities and
Minecraft 1.21.1 client-resource identity recorded in this document. The
current accepted local artifact is retargeted to All the Mons `1.2.0` at
pack commit
`c7bb230f21d14d26859d0b92548f089b3a493ad9` and NeoForge `21.1.248`.
Its exact verifier binds the pack-pinned MEGA Cells 4.11.0, Expanded AE 2.1.1,
Advanced AE 1.6.12, ExtendedAE 2.2.35 and Glassential 3.4.5 artifacts used to
classify bounded facade and endpoint behavior. The older Advanced AE 1.6.11
and ExtendedAE 2.2.33 identities remain historical M0-M3f/S1 audit evidence,
not current-pack inputs. These inputs remain external evidence; no third-party
source, class, JAR, model, texture, capture or precomputed mesh is
redistributed.

| Current All the Mons 1.2.0 S1 input | Size | SHA-256 |
| --- | ---: | --- |
| Advanced AE `1.6.12-1.21.1` | 4,791,255 bytes | `a01d9718667ac13899013e91c5b0b7708b9b9db1da9b8e380772dde54bbe8f41` |
| ExtendedAE `1.21-2.2.35-neoforge` | 5,578,031 bytes | `14a2860fa2c747e9dda2279b8933fac6311fecfee166c765171022b902591c65` |
| Glassential Renewed `3.4.5` | 702,249 bytes | `1f0c8f7533bf3b2002575219ba795fd32a44cc5085c2710624ebbf69e6121471` |

ExtendedAE tag `1.21-2.2.35-neoforge` resolves to commit
`3776bc854458301bbcc9a44a8238d70a0e3dc00d`.

The current runtime host is the canonical 6,467,235-byte
`bluemap-5.22-agent.backport-5.22-mc1.21.1-2-neoforge.jar`, SHA-256
`749f7647fa29764cea113114a7ab3259271bab3da22720989f2bd9fd1f3ba150`,
from BlueMap backport commit `9be321df995a1103808621d529eb72773e719d4d`.
That exact identity supersedes the prior host only for the 1.2.0 retarget; it
does not rewrite the accepted historical runtime record at backport commit
`fe79cf5b9f4d8ca28f4e41c2aeb9ef792e336a8d`.

The route/profile source audit covers the 29 ordered native face-part
identities, nine persisted-spin reporting parts, both plane connection
families, six unsigned-short-frequency P2P parts, legal dense cable-anchor
layouts, exact bounded facade state/material behavior and 30 native endpoint
state/side contracts. The pack-pinned Expanded AE I/O Port is retained as a
known compatible-unknown control and forces atomic fallback rather than a
guessed connection.

Two clean builds with Eclipse Temurin `21.0.12+8` and Python `3.13.14`
reproduced the exact local artifact set. Each ran 448 Java tests (446 passed and
two opt-in exporter tests were intentionally skipped) and 167 passing Python
tests; all gates passed:

| Artifact | Size | SHA-256 |
| --- | ---: | --- |
| Production JAR | 855,833 bytes | `5dad1cf654c13b5b0aa5411264104ff2f17b942b7d4c5def698d24c476951c39` |
| Sources JAR | 388,206 bytes | `9e294bcd04614132fd15270650d1f9e369a9491e2d13dd00d82f8e8060c2dcf2` |
| POM | 1,637 bytes | `967132ef80201099cfb1a798f03ff1ac37e0ac84a551694d7276ac20c7ccc136` |
| Gradle module metadata | 2,859 bytes | `8753a96f4d79e924058a17d8fa92c26e13ff7cf89ca2c1156c6f75f95d038c02` |
| Schema-10 gallery ZIP | 70,925 bytes | `66253309fd2cbe6b48c4ff621b71efa573b90a8d14b199205397df4d85d305e5` |
| Schema-10 `cases.json` | 4,207,895 bytes | `389a9b2b82dd16e3f4af82f9836e593770e404995a153218937908528c17dcee` |
| Appended S1 oracle | 198,162 bytes | `ac9a54cee9a20be18e71d6c9fe4f16b894827d43bb49cb4d0e56c673280cec39` |
| Legacy-upgrade oracle | 6,155 bytes | `cf0d86c440d1f89fc13f2b131f4f1534fb42363ebdc92580af826058297eb3d0` |
| Enabled map archive | 20,660,117 bytes | `0f57f33a205124c67069263cce0af8d74fa04343397317c4e491275df41558cb` |

Schema 10 preserves the accepted schema-9 identities exactly: the 3,314,082-
byte cases file remains
`75e6ba2f40631a95f20cfa00d7ca952e521bc2c7a4eb155926334a223a945f3a`
and the 49,679-byte gallery ZIP remains
`21ceec072cc3263a41bdb81874e897d48d5a1ce5e1c7d3ac3c0de3063818ee6c`.
The appended oracle locks 351 custom anchors, 37,518 triangles, 96 resources
and 2,093 material rows across 28 cases/360 anchors, with nine zero-triangle
fallbacks. The separate legacy oracle locks ten retained positions, 840
triangles, 21 resources and 70 rows. Their union contains 370 positions, 361
custom anchors, 38,358 triangles, 96 resources and 2,163 rows.

Enabled schema 10 totals 940 custom anchors, 64,938 custom triangles, 289
resources and 16 zero-triangle fallbacks across 150 cases/957 anchors; the
stone control yields 64,948 selected triangles. Native-structural-disabled
totals 589 custom anchors, 27,188 custom triangles, 218 resources, 17
fallbacks and 27,198 selected triangles. Its appended predecessor is exactly
ten rendered/350 empty anchors, 608 triangles and 14 resources, while all ten
legacy upgrades are empty. Physical stock selects 1,882 triangles across five
resources and leaves 918 anchors empty, including all appended and legacy-
upgrade positions.

S1's invariant signatures exclude only world-derived blocklight and sunlight;
complete observed attribute signatures retain both, per-triangle light must be
flat and within `0..15`, and the four smart-channel resources are exact
fullbright.

The exact All the Mons 1.2.0 isolated lifecycle completed on 2026-08-11. Each
cold/warm pair was byte-identical, and restored enabled reproduced initial
enabled exactly:

| Mode/phases | Report | Report SHA-256 | 46-file manifest SHA-256 |
| --- | ---: | --- | --- |
| Initial enabled cold/warm and restored cold/warm | 5,657,463 bytes | `14aa3b46386bead1f656f9796305c0000e835e5948ae06367d947a3afe837723` | `e1e592faabd263e1b9bacce14d56577f330d1b5cbd80336f2bd1563d3f1b2a78` |
| Native-structural-disabled cold/warm | 4,289,919 bytes | `63e528d6aa3c033cd6b2251f7a569cc0e4e7dc4bfba81d75862f8fa7a416e274` | `7cb0b48aa938109d8c001d32f01375aba7f29f7f9b8c5f96a927b52335a7df03` |
| Physically add-on-absent stock cold/warm | 993,266 bytes | `a1e148fde5af118def7e379c44a2294ca5d9485a60824ad2764911f1805788a4` | `9539d160733a7616ec9092e82086539b4db725e0afab2b04f5111a77e0da66f0` |

Enabled retained all 150 cases/957 anchors and the exact totals above.
Native-structural-disabled retained the exact ten-rendered/350-empty
predecessor projection and left all ten legacy upgrades empty. Physical stock
left all 360 appended S1 anchors and all ten legacy-upgrade positions empty.
The fixture stayed at `#m3f_builds=1`, `#s1_builds=1`, `#stable=2` and
`#failures=0` through actual unload/reload, a full JVM restart, route
disablement, physical removal and restoration. Every capture pod had zero
restarts and every init container exited zero. The restored BlueMap `-2` /
NeoForge 21.1.248 host activated every exact route, and its two render workers
advanced by 650 and 747 CPU ticks.

Packaging the restored comparable set twice produced byte-identical,
sorted 46-member archives at 20,660,117 bytes with SHA-256
`0f57f33a205124c67069263cce0af8d74fa04343397317c4e491275df41558cb`;
the extracted manifest reproduced the retained manifest exactly. On
2026-08-11 the owner visually accepted the exact JAR, gallery and map archive
in BlueMap. Candidate aliases `ae2-addon-s1-candidate.jar`,
`ae2-s1-gallery-candidate.zip` and
`ae2-s1-enabled-candidate-map-2026-08-11.tar.gz` are hash-exact to accepted
aliases `ae2-addon-s1-accepted.jar`, `ae2-s1-gallery-accepted.zip` and
`ae2-s1-enabled-accepted-map-2026-08-11.tar.gz`. Both alias sets were verified
on `data-atm120`; the historical PVC was untouched. The final review pod was
Ready with zero restarts and zero initializer failures, all exact routes
active and verifier counters `1/1/2/0`; `save-all flush` completed before
scale-to-zero. At that historical checkpoint S1 was the latest exact human-
accepted local result and remained unreleased; none of the S1 evidence alone
authorized publication. M4/M5 was separately accepted and published as
immutable prerelease `v0.1.0-alpha.1` on 2026-08-12. That publication does not
transfer acceptance or release authorization to the alpha.2 Applied
Mekanistics candidate; alpha.2 received its own bounded runtime acceptance and
release authorization on 2026-08-19.

## M2 facade/material audit evidence

The M2 facade/material provenance checkpoint is an exact source, bytecode and
resource audit used by the implemented local M2 profile. The exact M2 build
and completed enabled/stock-absent/re-enabled technical lifecycle are recorded
separately in `STAGING.md`; this provenance audit does not by itself prove
runtime behavior. The owner independently visually accepted the exact
203,599-byte M2 JAR and bounded gallery on 2026-08-07. A staging-PVC convenience
copy of that accepted JAR retains the same
`fc11af62359746990a2b35470c1da66e606b13a36be33a5b854d343eebb108d2`
SHA-256; it is not a publication or durable provenance store. That M2 artifact
contains no M3 work. M3a and M3b were separately tested and accepted. M3c has
exact reproducible artifacts and completed technical, lifecycle and human
acceptance. M3d formed crafting blocks have exact reproducible artifacts,
a completed technical lifecycle and human acceptance on 2026-08-08. M3e
quantum bridges also have exact reproducible artifacts, a completed technical
lifecycle and owner visual acceptance on 2026-08-08. M3f now has the exact
reproducible artifacts, completed technical lifecycle and bounded human
acceptance recorded above for
paint, both Sky Stone chests, neutral crank, neutral structural Inscriber and
locally inferred static/offline spatial pylons. M3 is complete. Machine contents, held items,
fluids, live/activity-specific state and accurate Drive LEDs are accepted
non-goals. S1 implements the post-M3 structural step as a reproducible,
runtime-validated and owner-visually-accepted local checkpoint. M4/M5 now
follows as the independently gated, owner-accepted checkpoint recorded above.
The owner separately authorized publication on 2026-08-12; repository and
release claims still require their own completed, verified operations.

The persisted facade boundary is established by the exact-tag blobs for:

- `FacadeContainer`, which derives `facadeDown` through `facadeEast` and uses
  Minecraft `BlockState.CODEC` for direct root-level NBT values;
- `FacadePart` and `FacadeItem`, which retain the block state and permit
  property cycling after placement;
- the generated `whitelisted/facades` tag, which proves that AE2's own
  accepted facade inventory is substantially wider than the add-on's planned
  exact plain-stone lane.

The client geometry and occlusion boundary is established by the exact-tag
blobs for `FacadeBuilder`, `FacadeRenderState`, `FacadeBlockAccess`,
`CableBusBakedModel`, `CableBusContainer`, `CableBusBlock`,
`BusCollisionHelper`, `AbstractReportingPart`, and `CableAnchorPart`.
The S1 plane contract additionally binds `PlaneConnections`,
`PlaneConnectionHelper`, `PlaneBakedModel`, and `QuadRotator`. Logical
front-view connection bits remain `up=8`, `right=4`, `down=2`, and `left=1`;
the baked model's visual-local bounds are transformed separately into world
space, while collision/facade cutouts use the installed face's exact
`BusCollisionHelper` local axes. In particular, DOWN/WEST/EAST reverse the
collision-local X bit polarity and UP reverses collision-local Y; the profile
therefore records a six-face table instead of a false uniform mapping.
`FacadeBuilder` also uses four source files vendored by AE2 from
CodeChickenLib: `QuadClamper`, `QuadReInterpolator`, `QuadFaceStripper`, and
`QuadCornerKicker`. Their file headers identify them as
LGPL-2.1-or-later. Every exact Git blob and corresponding top-level runtime
class SHA-256 is recorded in `provenance/upstreams.json`. The full runtime JAR
hash remains the primary behavior gate; the class digests document the exact
bytecode inspected and do not assert a reproducible build from the tag.

The facade-related exact AE2 runtime resources inspected were:

| Resource | SHA-256 | Purpose |
| --- | --- | --- |
| `assets/ae2/blockstates/cable_bus.json` | `32fe3ae039325fa7ed69e0475ce3a8ba20655f123032b9adb5725b636e6ff277` | Stock blockstate dispatch |
| `assets/ae2/models/block/cable_bus.json` | `44136fa355b3678a1146ad16f7e8649e94fb4fc21fe77e8310c060f61caaff8a` | Empty stock model |
| `assets/ae2/models/part/cable_anchor_short.json` | `d24f0f9f02bdb31589d843a2cfade5f25519d5769f44e7157a453592c632acb9` | Facade-side stilt evidence |
| `assets/ae2/textures/part/cable_anchor.png` | `34fa4349df6e0ce4a7830cdd459e671242a3fd2eafe14b598daa8a77a352c27c` | Operator-installed stilt material |
| `assets/ae2/models/part/translucent_facade.json` | `377ffcbd5f7177b21cecd6435d44264aa0cc63098b5a03721ec5bf2d169d0a66` | Unsupported transient client-mode evidence |
| `assets/ae2/textures/part/translucent_facade.png` | `07bbbd6247af59d151f8925f4ade7502d372f5cb80ef9f3d927a2c5f95951d0e` | Unsupported transient client-mode evidence |
| `data/ae2/tags/block/whitelisted/facades.json` | `4ff52f9d8670417406c29430f754305198ba8ab855ca34336962d6d24cf49f82` | Upstream accepted-state boundary |

The exact Minecraft 1.21.1 client-resource JAR used for the plain-stone proof
is 26,836,906 bytes, official SHA-1
`30c73b1c5da787909b2f73340419fdf13b9def88`, and SHA-256
`499f6897d1837516680f3114072d8106e11c9adcd933fe5cf051b551089b0c99`.
Its complete stone model/material closure is:

| Resource | SHA-256 |
| --- | --- |
| `assets/minecraft/blockstates/stone.json` | `34476f2bb98e7abec029c07c3cbfd8ba3a0141c0df3f02ff255018dbec625bbc` |
| `assets/minecraft/models/block/stone.json` | `c0734082e7f1e2f44809737b2fbe1136fc7304bde7232e45fbe921884a4a327e` |
| `assets/minecraft/models/block/stone_mirrored.json` | `f6127c3048b1812b664209825271553c71c2b2dae9d6a19933702b6fdbe34370` |
| `assets/minecraft/models/block/cube_all.json` | `be3205d629ffb9e03834c3d1d083f8a2c62e9f9ae80820755129408634e9144a` |
| `assets/minecraft/models/block/cube_mirrored_all.json` | `eb0682e3126d537e8a15af343bc40e32339baa63f4f541143ed969e25e9231e0` |
| `assets/minecraft/models/block/cube.json` | `3e4aacd02e816aeba38f83076596e18ded4cf49c01e17c62d1fce79850ffb84e` |
| `assets/minecraft/models/block/cube_mirrored.json` | `e0a7b21709196c8641177e68a0a5e2a25d1de8f82e7ece75120dd38ed5d7a0fa` |
| `assets/minecraft/models/block/block.json` | `3ef6c442f1ab55d2a57fa58e28bb831268159052659f12b453b637b31ded1da8` |
| `assets/minecraft/textures/block/stone.png` | `33ac70ba8cb701087642a12c5fd29c1ba3006dc4730e9b014ed3a19ce7cdda7b` |

The four weighted stone alternatives collapse to the same opaque,
non-animated material, but their client-selected mirrored/rotated UV
orientation is not persisted in Anvil. The M2 fidelity boundary may therefore
claim facade geometry and material, not pixel-identical randomized texture
orientation. The Minecraft artifact and resources are evidence only and are
not bundled or redistributed.

Effective resource-stack validation is informed by the exact BlueMap backport
blobs for `ResourcePool`, `Model`, `VariantSet`, `Texture`, and
`TextureGallery`, in addition to the already recorded resource renderer and
pack classes. Those identities are also in the machine-readable manifest.

## Adaptation and license

AE2 implementation source is offered under LGPLv3-or-later. This project
selects version 3 and distributes its adapted implementation under
`LGPL-3.0-only`. The modification notice and exact source blob identities are
recorded in `NOTICE.md` and the machine-readable manifest. The referenced
`AEColor` and `AECable*` API sources are MIT-licensed; the complete MIT notice
is retained.

The adaptation is limited to:

- a reduced persisted center-part DTO/decoder;
- an exact 85-ID color/family catalog and compatibility matrix;
- audited family-specific core/arm dimensions and straight-connection
  behavior;
- static zero-channel smart-cable overlays under `idle-off-unknown`;
- face winding and UV projection rules;
- an independent neutral geometry representation.

The implemented M2 lane adds a family-local bounded facade block-state decoder,
exact opaque plain-stone material proof and AE2-specific slab/aperture
generation. Its clipping, box union, UV reinterpolation, stilt, join and
occlusion rules remain private to this LGPL-3.0-only repository. This
provenance statement defines the source-use and license boundary; the runtime
evidence and completed human acceptance are recorded separately.

The M3a adaptation adds only an AE2-family-private bounded ten-slot projection,
the exact 23-item-to-12-model catalog, model composition/orientation and the
static offline-unknown LED policy. It copies no AE2 model, texture, item
component, inventory content or precomputed mesh.

The M3b adaptation adds a family-private twenty-slot projection, exact
front/rear bay transform, closed 26-item/15-model catalog, independent
activation boundary and Extended Drive base composition. It reuses local
neutral Drive slot/LED mechanics but copies no ExtendedAE source, model,
texture, component, inventory content or mesh. ExtendedAE declares LGPL-3.0;
this project retains its `LGPL-3.0-only` lane. Upstream credits Sea_Kerman for
models and Ridanisaurus for the new textures; those assets remain
operator-installed and unredistributed.

The M3c adaptation adds a family-private property-free two-block snapshot,
six-neighbor mask projection, exact position RNG, base/frame quad geometry and
independent fail-closed BlueMap route. It references the 22-resource/19-texture
operator-installed closure but copies no AE2 source, model, texture, capture or
mesh. Minecraft `getAppearance` proxies, client particles/items and
pixel-identical `shade=false` directional lighting are explicitly outside this
bounded BlueMap 5.22 world-rendering claim.

The M3d adaptation adds family-private formed-crafting topology, monitor color
projection and `CraftingCube`/`CubeBuilder` geometry. The M3e adaptation adds
family-private native quantum topology inference and ports the exact source-
float formed-model/`CubeBuilder` geometry and UV rules. Both resolve only
operator-installed resources; neither copies models, textures, captures or
precomputed meshes. M3e deliberately omits animated light overlays and
particles under `static-off-unknown`.

The M3f adaptation adds a family-private bounded paint projection, closed
Sky Stone chest models, neutral crank/Inscriber transforms and local spatial-
pylon axis/role inference. Its uncached native-axis scan is capped at 256
pylons; complete observed invalid bend/branch components render AE2's unformed
BASE-plus-DIM role, while missing, malformed or capped observation falls back
atomically. It reconstructs geometry at render time from exact
persisted state and operator-installed resources, copies no model, texture,
capture or precomputed mesh, and deliberately omits contents, held items,
fluids, activity, global pylon validity and live machine state.

The S1 adaptation adds family-private face-part chassis composition, plane and
P2P projection, dense cable-bus topology, facade clamp/reinterpolation and
native endpoint connection inference. It resolves only exact
operator-installed resources, copies no upstream model, texture, source,
capture or mesh, and deliberately omits live part activity, displayed
contents, network state and animations.

The M4 AppliedFlux adaptation adds a closed 20-item/ten-model Drive catalog
and one neutral Flux Accessor part. The ME Requester adaptation adds exact
saved block transforms, including `ae2:z` normalization, and one offline
terminal pose. The Expanded AE adaptation adds its exact I/O Port transforms,
two part stacks and 21 formed-crafting identities. The M5 MEGA Cells adaptation
adds eight crafting roles, three generic parts, Cell Dock structure and a
67-item/37-model static cell catalog shared by both Drive types. These projects
declare LGPL-3.0-family terms; the local adaptations remain in this project's
`LGPL-3.0-only` lane.

The Advanced AE adaptation adds exact static quantum-computer topology and
geometry for eight roles, forcing transient power/light/emission/animation
off. Its quantum-alloy CTM route independently implements the five-texture
selection semantics observed with exact Athena 4.0.6 and generates synthetic
static frame-zero textures from validated operator-installed strips. Advanced
AE declares LGPL-3.0-family terms. Athena declares MIT; its existing complete
MIT license text is retained in `LICENSES/MIT.txt`. No Athena source, class,
model, texture, animation metadata, or binary is copied.

The ExtendedAE M5 adaptation adds six static Assembler Matrix roles and two
static cable-bus planes while retaining the exact current Extended Drive
partition. It preserves durable formation and frame shape, normalizes power
and activity off, and reuses only locally implemented neutral geometry.
ExtendedAE declares LGPL-3.0-family terms. All M4/M5 upstream resources remain
external and operator installed.

AE2 models and textures are separately licensed CC BY-NC-SA 3.0. None is
copied into this repository or production JAR. The add-on refers to texture
identifiers and resolves the operator-installed resources at runtime. No raw
client capture or precomputed upstream mesh is distributed. The generated AE2
facade-whitelist tag is evidence only and is not copied; this record makes no
separate redistribution claim for that generated file. AE2's upstream text
lane is separately documented as CC0. Minecraft stone models and texture
remain operator-supplied Mojang content and are likewise not copied.

The CodeChickenLib quad-transform sources vendored in AE2 are
LGPL-2.1-or-later. No CodeChicken source or class is copied. Where the M2 AE2
facade implementation adapts bounded behavior informed by that audit, the
project selects the compatible version-3 license path, retains exact
attribution here and in the machine-readable record, and keeps that
implementation out of any permissive shared toolkit.

BlueMap is MIT-licensed and remains an external runtime host. The project
compiles against the exact local backport source and adapts bounded exact-ABI,
ambient-occlusion, map-color and stock-fallback patterns, but packages no
BlueMap or BlueNBT classes. Its full MIT copyright and permission notice is
retained in `LICENSES/MIT.txt` and the production and sources JARs.

The dependency-free evidence analyzer was checked against the exact backport
blobs for `PRBMWriter`, the web `PRBMLoader`, tile loading/transforms,
file-grid storage and `TextureGallery`. It independently parses only the
functional on-disk format needed for bounded staging evidence. The exact blob
IDs are locked in `tools/analyzer-upstreams.json`; no BlueMap source or binary
is copied into the analyzer or fixture.

## Audit commands

Historical M3b/M3d verification keeps the 2.2.33/1.6.11 input properties.
`verifyNativeStructuralPinnedArtifact` uses the S1 2.2.35/1.6.12/
Glassential 3.4.5/canonical BlueMap `nativeStructural*Jar` properties. The
current extension gate is `verifyM45PinnedArtifacts` with all seven
`m45*Jar` properties.

```bash
python3 -m json.tool provenance/upstreams.json >/dev/null
python3 -m json.tool tools/analyzer-upstreams.json >/dev/null
python3 tools/verify_pinned_artifact.py \
  --jar /absolute/path/appliedenergistics2-19.2.17.jar
python3 tools/verify_extendedae_artifact.py \
  --jar /absolute/path/ExtendedAE-1.21-2.2.33-neoforge.jar
./gradlew --no-daemon verifyCraftingPinnedArtifact \
  -Pae2Jar=/absolute/path/appliedenergistics2-19.2.17.jar \
  -PmegaCellsJar=/absolute/path/megacells-4.11.0.jar \
  -PexpandedAeJar=/absolute/path/expandedae-2.1.1.jar \
  -PadvancedAeJar=/absolute/path/AdvancedAE-1.6.11-1.21.1.jar \
  -PextendedAeJar=/absolute/path/ExtendedAE-1.21-2.2.33-neoforge.jar
./gradlew --no-daemon verifyQuantumBridgePinnedArtifact \
  -Pae2Jar=/absolute/path/appliedenergistics2-19.2.17.jar \
  -Pae2SourcesJar=/absolute/path/appliedenergistics2-19.2.17-sources.jar
./gradlew --no-daemon verifyM3CompletionPinnedArtifact \
  -Pae2Jar=/absolute/path/appliedenergistics2-19.2.17.jar \
  -Pae2SourcesJar=/absolute/path/appliedenergistics2-19.2.17-sources.jar
./gradlew --no-daemon verifyNativeStructuralPinnedArtifact \
  -Pae2Jar=/absolute/path/appliedenergistics2-19.2.17.jar \
  -Pae2SourcesJar=/absolute/path/appliedenergistics2-19.2.17-sources.jar \
  -PmegaCellsJar=/absolute/path/megacells-4.11.0.jar \
  -PexpandedAeJar=/absolute/path/expandedae-2.1.1.jar \
  -PnativeStructuralAdvancedAeJar=/absolute/path/AdvancedAE-1.6.12-1.21.1.jar \
  -PnativeStructuralExtendedAeJar=/absolute/path/ExtendedAE-1.21-2.2.35-neoforge.jar \
  -PnativeStructuralGlassentialJar=/absolute/path/Glassential-renewed-1.21.1-3.4.5.jar \
  -PnativeStructuralBlueMapJar=/absolute/path/bluemap-5.22-agent.backport-5.22-mc1.21.1-2-neoforge.jar \
  -PminecraftClientJar=/absolute/path/minecraft-1.21.1-client.jar
./gradlew --no-daemon verifyM45PinnedArtifacts \
  -Pae2Jar=/absolute/path/appliedenergistics2-19.2.17.jar \
  -Pm45AppFluxJar=/absolute/path/AppliedFlux-1.21-2.1.5-neoforge.jar \
  -Pm45MeRequesterJar=/absolute/path/merequester-neoforge-1.21.1-1.4.3.jar \
  -Pm45ExpandedAeJar=/absolute/path/expandedae-2.1.1.jar \
  -Pm45MegaCellsJar=/absolute/path/megacells-4.11.0.jar \
  -Pm45AdvancedAeJar=/absolute/path/AdvancedAE-1.6.12-1.21.1.jar \
  -Pm45AthenaJar=/absolute/path/athena-neoforge-1.21.1-4.0.6.jar \
  -Pm45ExtendedAeJar=/absolute/path/ExtendedAE-1.21-2.2.35-neoforge.jar
./gradlew --no-daemon verifyProvenance verifyProductionJar
```
