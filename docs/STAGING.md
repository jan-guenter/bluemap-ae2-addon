# Isolated cumulative AE2 staging evidence

## M4/M5 accepted local checkpoint

The All the Mons 1.2.0 M4/M5 candidate completed its exact reproducible-build,
schema-11, and isolated technical-lifecycle gates. It adds eight independent
routes: `appflux`, `merequester`, `expandedae`, `megacells`,
`advanced-ae-quantum`, `advanced-ae-athena`, `extendedae-matrix`, and
`extendedae-planes`. The canonical `extendedae` disable switch covers both
ExtendedAE M4/M5 routes; the internal IDs remain available for route-isolation
diagnosis.

Two byte-identical builds each ran 562 Java tests (560 passed and two opt-in
exporter tests were intentionally skipped) and 180 pre-oracle Python tests.
After the runtime oracles were frozen, the exact CPython 3.13.14 suite passed
192/192 tests in 945.343 seconds, and the generator/checksum closure passed.

| Candidate artifact | Size | SHA-256 |
| --- | ---: | --- |
| Production JAR | 1,207,683 bytes | `6fed7a625b02229213a047788995944f14e7e7fcabe0e0ddc6d9b5e994146e9f` |
| Sources JAR | 532,979 bytes | `4a45c60f8512630c6bd9735e26018d019ebe99d58f2c87fa2f3c46e101b624d8` |
| POM | 1,637 bytes | `967132ef80201099cfb1a798f03ff1ac37e0ac84a551694d7276ac20c7ccc136` |
| Gradle module metadata | 2,861 bytes | `3f0ba24c34ef535c99cbd6dabcd7d6bb0f784ca6ffd032f06faaf9a9b5d7b0b8` |
| Schema-11 gallery ZIP | 94,537 bytes | `c67b4f794092f6e994349a8ee9320c052e2efc87f04e8813faf158c3455fe33b` |
| Schema-11 `cases.json` | 6,017,554 bytes | `914dab6931077521959cf59260a1ffb0cdbe105385f43880763b289f8117ec55` |
| Main runtime oracle | 221,769 bytes | `c2ce69bed949306551ca4ff6cdebf7fac88f0f2f2fa7ab294d3312f363e1b448` |
| Schema-10 legacy-upgrade oracle | 2,336 bytes | `2319ecf576ba07b123078c720d941990fac939033d375e5853f51bf98348c3c7` |
| Enabled map archive | 20,821,895 bytes | `44422aa71c2f450951d8433e25e01de7a0b00dbd0d9c4fa4ff74ca98e649a2df` |

Schema 11 contains 158 cases/1,366 anchors. Its main oracle locks 391 custom
M4/M5 anchors, 23,334 triangles, 122 resources, and 2,089 material rows; its
legacy-upgrade oracle locks three retained positions and 282 triangles. The
isolated lifecycle produced exact byte-identical cold/warm pairs:

| Mode/phases | Report | Report SHA-256 | 79-row manifest SHA-256 |
| --- | ---: | --- | --- |
| Initial and restored enabled cold/warm | 7,695,130 bytes | `6aaa925961ace51dfd2a7e074f9745d78e4aae28a334425f55498fa2bb5038fc` | `97a4f0915d6698e0962d6355f0835a2286a3ee7cae983ad1f3514c9ecb76b46f` |
| Combined-disabled cold/warm | 6,760,492 bytes | `acc180648556ba2a0fc4115172ba04aad4e0c266358379bf75f80f5353c62aa2` | `d457c23adc189c0e03718aad5c09649a7db63c1efb0174b439432df9f480ac56` |
| Crafting-disabled cold/warm | 5,956,875 bytes | `8b2b7b3b857c88f1c201399cffeeead588ac78e3ae3023d64ca4c3952a8b1593` | `dd86e95e7001ddb22e355f6910d7e7d3ded032d4eb2fcedc1a5ffd2deb1258cc` |
| Native-structural-disabled cold/warm | 5,697,202 bytes | `0742cc20f7fd7af776f1a342c52206e6ee6107c758146b9376358019b8a34a10` | `680ae50473f9829f396fc09f260e28221f283eda30a2b438688a81ca9788a668` |

Initial and restored enabled output is identical. Full-JVM transitions,
cold/warm determinism, and the restart, initializer, worker, one-build, and
settle gates passed. The exact aliases on `data-atm120` are
`ae2-addon-m5-candidate.jar`, `ae2-m5-gallery-candidate.zip`, and
`ae2-m5-enabled-candidate-map-2026-08-11.tar.gz`. Hash-exact accepted aliases
`ae2-addon-m5-accepted.jar`, `ae2-m5-gallery-accepted.zip`, and
`ae2-m5-enabled-accepted-map-2026-08-11.tar.gz` coexist with those candidate
aliases. The owner accepted the exact artifacts in BlueMap and separately
authorized publication on 2026-08-12. S1 is the accepted predecessor.

Live power, channels, activity, requests, displayed stacks, inventories,
fluids, animations, and accurate LEDs remain intentionally outside the review.

## Environment boundary

M4/M5 uses the All the Mons 1.2.0 versioned volume in the isolated
`bluemap-ae2-staging` synthetic world. It is neither the FramedBlocks lab nor
production. The current retained technical modes are enabled initial/restored,
combined M4/M5-disabled, crafting-disabled, and native-structural-disabled.
The exact gallery bounds are recorded in `gallery/cases.json`; the M4/M5 append
occupies the disjoint `x=336..511`, `y=96..110`, `z=312..431` volume while the
schema-10 predecessor coordinates remain unchanged.

The M3b results below remain accepted historical evidence. The exact M3c Java,
profile, schema-6 gallery and analyzer implementation passed reproducible build
gates and the complete isolated technical lifecycle on 2026-08-08, and the
owner visually accepted its exact JAR, gallery and map archive that day. M3c is
complete. The exact M3d artifact subsequently passed its reproducible build,
complete isolated technical lifecycle and owner visual acceptance on
2026-08-08. The exact M3e candidate subsequently passed its reproducible
build and complete isolated technical lifecycle that day, and the owner
visually accepted the exact JAR, gallery and archive. M3f subsequently
completed its implementation and exact isolated technical lifecycle on
2026-08-09. The owner visually accepted the exact corrected M3f JAR, gallery
and map archive that day, completing M3. At that checkpoint M3f was the latest
accepted slice and M3e was its previous accepted rollback checkpoint.

The All the Mons `1.2.0` S1 revalidation completed its exact isolated
technical lifecycle on 2026-08-11. It is pinned
to pack commit `c7bb230f21d14d26859d0b92548f089b3a493ad9`, NeoForge `21.1.248`,
Advanced AE `1.6.12-1.21.1`, ExtendedAE `1.21-2.2.35-neoforge`,
Glassential `3.4.5` and the canonical 6,467,235-byte BlueMap
`5.22-agent.backport-5.22-mc1.21.1-2` NeoForge JAR with SHA-256
`749f7647fa29764cea113114a7ab3259271bab3da22720989f2bd9fd1f3ba150`.
Its exact local add-on artifact is 855,833 bytes with SHA-256
`5dad1cf654c13b5b0aa5411264104ff2f17b942b7d4c5def698d24c476951c39`;
the schema-10 gallery remains 70,925 bytes at SHA-256
`66253309fd2cbe6b48c4ff621b71efa573b90a8d14b199205397df4d85d305e5`.
The owner completed the bounded BlueMap visual review for that exact JAR,
gallery and deterministic map archive on 2026-08-11. The lifecycle used a
fresh versioned staging volume and separately named maps; evidence from another
pack or host tuple is not interchangeable with this All the Mons 1.2.0 record.

Only one full ATMons lab runs at a time on the available node. Scale the
disposable FramedBlocks deployment to zero before starting this lab; do not
delete its PVC or evidence.

The copied `ATMons - DevTest` Prism client may be reused, but its installed
protocol-v2 controller **must not be armed in the AE2 lab**. That controller's
target policy is FramedBlocks-only; the compatibility sentinel and pose alias
can satisfy its target lock while unintentionally exposing its complete
FramedBlocks action surface. Authentication, connection and screenshots remain
manual until a first-class, separately reviewed AE2 target policy is approved.

## Current fixture

`gallery/datapack` owns ten disjoint bounded areas in the overworld:

| Area | Bounds/use |
| --- | --- |
| Retained M1 fixture | `x=208..263`, `y=99..104`, `z=192..239` |
| M2 fixture | `x=208..239`, `y=99..104`, `z=242..249` |
| M3a Drive fixture | `x=240..263`, `y=98..104`, `z=242..249` |
| M3b Extended Drive fixture | `x=240..279`, `y=98..104`, `z=260..267` |
| M3c connected-glass fixture | `x=208..279`, `y=97..104`, `z=288..307` |
| M3d formed-crafting fixture | `x=296..319`, `y=97..105`, `z=260..299` |
| M3e quantum-bridge fixture | `x=281..294`, `y=97..105`, `z=269..278` |
| M3f completion fixture | `x=280..319`, `y=96..106`, `z=208..230` |
| South observation deck | `x=214..228`, `y=106..110`, `z=251..257` |
| Controller sentinel | `x=255..258`, `y=99..102`, `z=255..257` |

The 122 logical cases contain 597 anchors. The retained 48 M1 cases and 269
anchors cover every one of the 85 exact center-part IDs, same- and cross-family
compatibility, straight and junction topologies, one stone control and two
conservative device-fallback controls. Fourteen M2 cases add terminals on all
six faces and spins `0..3`, multiple terminals, a terminal-bearing line, two
same-face property-free stone facades and nine further atomic-fallback cases.
Fourteen M3a cases add 33 Drive anchors covering all 24 `facing`/`spin` states,
all 23 exact native item IDs and 12 occupied models, empty/sparse/full slot
layouts, component insensitivity and one unsupported MEGA Cells fallback.
Sixteen M3b cases add 36 Extended Drive anchors covering all 24 states, twenty
front/rear slots, all 26 exact supported items and 15 occupied models,
empty/catalog/sparse/full layouts, mirror/component pairs and four atomic
fallbacks. Eleven M3c cases add 47 custom connected-glass anchors and 776
triangles across isolated ordinary/vibrant equivalence, axial and corner
connections, T/four-arm/plane/cube/enclosed structures, diagonal
non-connection and opaque-neighbor culling. They select all 19 M3c resources
and exercise all 15 frame textures plus mask `0000`; this is a representative
gallery, while Java tests cover all 64 direct-neighbor masks.
Nine M3d cases add 86 anchors covering all eight native blocks, isolated and
connected topology, a powered 2-by-2-by-2 CPU, an unpowered hard-culling
3-by-3-by-3 CPU, all 17 monitor paint ordinals, all facings/spins and one
known-compatible mixed-extension atomic fallback. Its 85 custom anchors own
4,306 triangles and select all 15 crafting resources; 84 are nonzero and the
fully enclosed center is explicitly zero geometry.
Three M3e cases add 27 anchors for complete dry formed XZ, XY and YZ quantum
bridges. Every bridge has one link, four edge rings and four corner rings and
owns exactly 396 static-off triangles, for 1,188 M3e triangles. Across the
cumulative M0-M3e fixture, 501 custom anchors own 23,758 custom triangles and
select 203 resources; 17 atomic fallbacks remain empty and the stone control
remains ten triangles. Seven M3f cases add 78 anchors for persisted paint,
both closed Sky Stone chests, every neutral crank attachment, all 24 neutral
Inscriber transforms and 17 spatial-pylon anchors, including every member of
the invalid L/T components. Cumulatively, 579 custom anchors own 26,580 custom
triangles and select 218 resources; 17 atomic fallbacks remain empty. The M3f
slice owns 2,822 triangles with no fallback; its pylons own 408 triangles.
The FramedBlocks sentinel and `framedblocks_gallery:pose_south` alias exist
only for fixture/client compatibility. They are not an AE2 permission boundary
and must not be used to arm protocol-v2 in this lab.

Install the datapack only in the disposable world, then run:

```text
function ae2_m3:build
function ae2_m3:verify
function ae2_m3:pose_south
function ae2_m3:release
```

`build` starts with the bounded clear function and ends with `save-all flush`
and structural verification, but the persistent `#m3f_builds ae2m3run`
counter makes a second full build detectable and invalid for the M3f evidence
lifecycle. `verify` proves exact
blocks, retained cable-bus and both Drive NBT subsets, center/part/item IDs,
native ten-slot and Extended twenty-slot front/rear layouts, property absence
on plain stone facades, both property-free glass IDs and their support/air
volume, all stable M3d formed/powered/facing/spin and monitor paint fields,
all 27 formed/dry quantum members and their exact block entity IDs,
the exact M3f paint payloads, closed chest states, crank/Inscriber transforms,
local straight pylon roles and all members of both invalid L/T components,
helper devices, support/air-gap planes, sentinel and safe pose volume.
It rejects an unexpected Extended Drive `item20`. It does not prove which
renderer ran or validate client/BlueMap pixels. The load function only restores
the bounded force-load areas; it never clears or rebuilds the fixture.
`release` removes those force-loads for the persistence probe or lab retirement.

The opt-in dense fixture contains four disjoint 8-by-4-by-8 fluix
covered-dense cable lattices. It is never called by the normal load path:

```text
function ae2_m3:dense/build
function ae2_m3:dense/verify
function ae2_m3:dense/clear
function ae2_m3:dense/release
```

The build runs in four scheduled batches. `dense/clear` removes the blocks;
`dense/release` removes only the dense fixture's disjoint force-loads.

## Lifecycle

1. Explicitly build, verify and flush the normal fixture once. After AE2's
   scheduled initialization, run `function ae2_m3:verify` again and require
   scoreboard `#failures ae2m3v` to be zero and `#stable ae2m3s` to be two.
2. Flush, release the main force-loads and keep players away until those chunks
   unload. Re-add the bounded force-load ranges without calling `build`,
   then require the verifier to remain at zero.
3. Restart the complete JVM with no deployment hook calling `build`; verify
   again and inspect the six critical supported and unsupported NBT states.
4. With the exact add-on present, purge/rerender `ae2_staging`, complete the
   task queue, and preserve cold and warm schema-9 reports and non-live web
   artifacts. Require 597 anchors, 579 custom anchors, 26,580 custom triangles,
   218 resources and 17 zero-triangle fallbacks.
5. Stop, disable only profile ID `extendedae`, restart, verify without
   rebuilding, purge/rerender `ae2_extension_disabled`, and require all 36
   M3b anchors to stay empty while the M3a, M3c, M3d, M3e and M3f subreports
   remain exact.
6. Stop, disable only profile ID `ae2-quartz-glass`, restart, verify without
   rebuilding, purge/rerender `ae2_glass_disabled`, and require all 47 M3c
   anchors to stay empty while the M0-M3b, M3d, M3e and M3f slices remain exact.
7. Stop, disable only profile ID `ae2-crafting`, restart, verify without
   rebuilding, purge/rerender `ae2_crafting_disabled`, and require all 86 M3d
   anchors empty while reproducing the exact M0-M3c, M3e and M3f totals.
8. Stop, disable only profile ID `ae2-quantum-bridge`, restart, verify without
   rebuilding, purge/rerender `ae2_quantum_disabled`, and require all 27 M3e
   anchors empty while retaining the exact M3f slice and reproducing the
   accepted schema-7 M0-M3d totals.
9. Stop, disable only profile ID `ae2-m3-completion`, restart, verify without
   rebuilding, purge/rerender `ae2_m3_completion_disabled`, and require the
   exact original-resource projection at all 78 M3f anchors: 38 rendered, 40
   empty and 1,872 triangles, while the accepted schema-8 custom slice stays
   exact.
10. Stop, physically remove only the add-on, restart, purge/rerender
   `ae2_stock`, and preserve cold and warm results separately. The stone
   control plus 38 stock-rendered M3f anchors own 1,882 triangles; 40 M3f
   anchors and all 518 legacy non-control anchors must be empty.
11. Stop, restore the exact add-on and enabled configuration, restart, verify
   without rebuilding and intentionally rerender `ae2_staging`.
12. Run the two-render-thread smoke, repeat the enabled capture for handoff, and
   compare all four semantic signatures and every relevant web artifact
   outside live/`rstate` bookkeeping.

The optional M1 dense regression is independent of this required M3f lifecycle.
If run, build/verify it only through the `ae2_m3:dense/*` commands, capture its
bounded report, then clear/release it and reproduce the compact gallery before
continuing.

The completed M3f checkpoint below exercised the M3f-specific persistence,
enabled, M3-completion-disabled, physically absent stock and restored-enabled
lanes. It did not rerun the four older extension/glass/crafting/quantum
disablement deployments; their accepted M3e runtime evidence remains intact,
while schema-9 synthetic/analyzer tests retain every route-isolation contract.

No visual or performance-budget row is passed merely because this lifecycle
completed. Human comparison and renderer-specific measurement are separate
gates.

Human review must compare geometry, frame connectivity, asymmetric UV
selection, cutout, ordinary/vibrant appearance and opaque-neighbor culling,
but must not demand pixel-identical directional lighting: BlueMap 5.22 PRBM
cannot encode AE2's client `shade=false` quad flag. Cross-mod blocks that only
appear as quartz glass through Minecraft `getAppearance` are also outside this
route and should remain visibly disconnected. Client particles/items are not
part of the gallery acceptance.

M3d human review must additionally compare rings, corners, stripes, connected
face suppression, powered overlays, all storage kinds and painted monitor
fronts. The transient Crafting Monitor displayed job stack is intentionally
absent and is not an acceptance requirement.

M3e human review must compare all three bridge planes, the distinct link,
corner and edge roles, arm bounds, UV orientation, materials, ambient
occlusion and ordinary world light. Every bridge is deliberately shown in the
static off state. Animated quantum overlays, network power, particles and any
extension connection are not acceptance requirements. The owner completed
this review on 2026-08-08 for the exact accepted artifact set below.

M3f human review must compare paint color/face placement, both closed chest
variants, all six neutral crank attachments, all 24 neutral Inscriber
transforms, isolated/start/middle/end pylon roles on all three axes, and every
member of the invalid L- and T-shaped pylon components in the unformed
BASE-plus-DIM appearance. Contents, held items, fluids, animations,
live/activity state and accurate Drive LEDs are not acceptance requirements.
The owner completed this review on 2026-08-09 for the exact corrected artifact
set documented below.

S1 human review must compare all 29 native face-part families, plane masks,
P2P frequency pixels, legal dense and multipart layouts, bounded facade
materials, native endpoint connections and every atomic-fallback control in
the exact schema-10 gallery. Static off/inactive/unlocked state is intentional.
Live network state, activity, contents, animations and accurate Drive LEDs are
not acceptance requirements. The owner completed this review in BlueMap on
2026-08-11 for the exact artifact set recorded below.

## S1 All the Mons 1.2.0 accepted technical/lifecycle and visual evidence

On 2026-08-11 the exact 855,833-byte S1 add-on JAR with SHA-256
`5dad1cf654c13b5b0aa5411264104ff2f17b942b7d4c5def698d24c476951c39`
passed the isolated All the Mons 1.2.0 lifecycle against NeoForge `21.1.248`,
the exact 6,467,235-byte BlueMap `-2` host
`749f7647fa29764cea113114a7ab3259271bab3da22720989f2bd9fd1f3ba150`
and the unchanged exact 70,925-byte schema-10 gallery
`66253309fd2cbe6b48c4ff621b71efa573b90a8d14b199205397df4d85d305e5`.
Every artifact initializer verified its expected identity and exited zero.

The fixture was built exactly once for M3f and once for S1. It stayed at
`#m3f_builds=1`, `#s1_builds=1`, `#stable=2` and `#failures=0` through actual
chunk unload/reload, one complete JVM restart, native-structural disablement,
physical add-on removal and final restoration. No transition rebuilt the
fixture. Every capture pod had zero restarts, and every init container exited
zero. The restored BlueMap host activated every exact M0-S1 route, including
the retargeted ExtendedAE and native-structural routes.

Every mode's cold/warm pair was byte-identical. Restored enabled cold/warm was
also byte-identical to initial enabled cold/warm:

| Mode/phases | Report | Report SHA-256 | 46-file comparable-manifest SHA-256 |
| --- | ---: | --- | --- |
| Initial enabled cold/warm and restored cold/warm | 5,657,463 bytes | `14aa3b46386bead1f656f9796305c0000e835e5948ae06367d947a3afe837723` | `e1e592faabd263e1b9bacce14d56577f330d1b5cbd80336f2bd1563d3f1b2a78` |
| Native-structural-disabled cold/warm | 4,289,919 bytes | `63e528d6aa3c033cd6b2251f7a569cc0e4e7dc4bfba81d75862f8fa7a416e274` | `7cb0b48aa938109d8c001d32f01375aba7f29f7f9b8c5f96a927b52335a7df03` |
| Physically add-on-absent stock cold/warm | 993,266 bytes | `a1e148fde5af118def7e379c44a2294ca5d9485a60824ad2764911f1805788a4` | `9539d160733a7616ec9092e82086539b4db725e0afab2b04f5111a77e0da66f0` |

Enabled observed 150 cases/957 anchors, 940 custom anchors, 64,938 custom
triangles, 289 resources, 16 zero-triangle fallbacks and 64,948 selected
triangles including stone. Native-structural-disabled observed 589 custom
anchors, 27,188 custom triangles, 218 resources, 17 fallbacks and 27,198
selected triangles. Its appended predecessor projection was exactly ten
rendered/350 empty anchors with 608 triangles across 14 resources, and all ten
legacy upgrades were empty. Physical stock observed 1,882 selected triangles
across five resources; all 360 appended S1 anchors and all ten legacy-upgrade
positions were empty.

The two configured BlueMap render workers advanced by 650 and 747 CPU ticks
during the bounded restored rerender. This is a concurrency observation, not a
renderer-only performance claim. Packaging the restored 46-file comparable
set twice in sorted deterministic order produced byte-identical 20,660,117-
byte archives with SHA-256
`0f57f33a205124c67069263cce0af8d74fa04343397317c4e491275df41558cb`;
extracting the archive reproduced the retained manifest exactly. After final
verification, `save-all flush` completed and the staging deployment was scaled
to zero. The versioned staging data and evidence remain retained.

The final visual-review pod was Ready with zero restarts, every initializer
exited zero and every exact route activated. The final verifier counters were
`#m3f_builds=1`, `#s1_builds=1`, `#stable=2` and `#failures=0`; `save-all flush`
completed before the deployment was scaled to zero. On 2026-08-11 the
owner visually accepted the exact JAR, gallery and deterministic map archive
above in BlueMap. The hash-exact candidate aliases are
`ae2-addon-s1-candidate.jar`, `ae2-s1-gallery-candidate.zip` and
`ae2-s1-enabled-candidate-map-2026-08-11.tar.gz`; the hash-exact accepted
aliases are `ae2-addon-s1-accepted.jar`, `ae2-s1-gallery-accepted.zip` and
`ae2-s1-enabled-accepted-map-2026-08-11.tar.gz`. Both alias sets were verified
on `data-atm120`, and the historical PVC was untouched. S1 remains an exact
human-accepted local checkpoint and is unreleased. M4/M5 was accepted and
publication was separately authorized on 2026-08-12. The distinct
non-SNAPSHOT gate subsequently published immutable prerelease
`v0.1.0-alpha.1`; the hash-exact staging alias for that published prerelease
build, distinct from the M4/M5 local-checkpoint aliases, is
`ae2-addon-0.1.0-alpha.1-accepted.jar` at 1,207,650 bytes and SHA-256
`8372b4f043f3091f6aa1fed6a76006e5e04cbf462647472beb392c9789da9e03`.

## M3f accepted technical/lifecycle and visual evidence

On 2026-08-09 the exact local M3f candidate passed the coordinated source,
artifact, generated-profile, schema-9 analyzer and isolated runtime gates. All
365 Java tests and 116 Python tests passed. The prior M3f candidate was
withdrawn after owner visual review exposed invisible L/T pylon members; it
was never accepted. The corrected artifact below supersedes it and alone
received the bounded 2026-08-09 acceptance.

| Artifact | Size | SHA-256 |
| --- | ---: | --- |
| Production JAR | 623,591 bytes | `ca67c0fc433e43f8e0801ed8d2cccfe47aae317fbc329c099bc8cd741ec3b42b` |
| Sources JAR | 276,986 bytes | `2a3bb3713ff56731992d405a58fc6a137dcfc8fff43467de7196ad33c444795c` |
| POM | 1,637 bytes | `967132ef80201099cfb1a798f03ff1ac37e0ac84a551694d7276ac20c7ccc136` |
| Gradle module metadata | 2,859 bytes | `106df734036c9f72f0463a03e2a282d430653edab5e761b165b51fe99f937d7a` |
| M3-completion profile | 9,405 bytes | `281a335d3024ebbb97c6268e768826c467d6f7ea660989fd3dae204c6c03abf3` |
| Schema-9 gallery ZIP | 49,679 bytes | `21ceec072cc3263a41bdb81874e897d48d5a1ce5e1c7d3ac3c0de3063818ee6c` |
| Schema-9 `cases.json` | 3,314,082 bytes | `75e6ba2f40631a95f20cfa00d7ca952e521bc2c7a4eb155926334a223a945f3a` |
| Packaged support matrix | 9,684 bytes | `71ec7977c7990678b9f34b27b976a0a1381b85a292afd409556f8190cee88863` |
| Packaged provenance manifest | 84,990 bytes | `dd3bda10236288c5a8e745978e7f55467961bf3aab1a4cb8ab527e8b1eec6e50` |

Exact-source review established an uncached native-axis scan capped at 256
pylons. Every fully observed invalid L/T component member renders AE2's
unformed `NONE` role with BASE plus DIM; only missing, malformed or capped
observation falls back atomically.

The schema-9 gallery contains 122 cases and 597 anchors: 579 custom anchors,
26,580 custom triangles, 218 resources, 17 empty fallbacks and one ten-
triangle stone control. M3f contributes seven cases, 78 custom anchors, 2,822
triangles, no fallback and 15 emitted resources. Its 17 pylon anchors own 408
triangles. Enabled therefore selects 26,590 triangles in total. The accepted schema-8
projection remains frozen at SHA-256
`93963dd0bb60a276e1a17c6dd1f4eb916cd92bef4ef30a2e8bdc7a2bfa818b3e`.

Every completed cold/warm pair was byte-identical, and restored enabled
exactly matched initial enabled. Every manifest has 44 unique rehashed entries
and totals 3,893 bytes:

| Mode/phases | Report | Report SHA-256 | 44-file manifest SHA-256 |
| --- | ---: | --- | --- |
| Enabled cold/warm/restored | 3,783,797 bytes | `7022a33448dab364cb825a8d67359795560b6a8793b64544ccb0b4c1fda7484e` | `259af4eea91a32acc07d1572e8f3f42e6276b46999496d0380ec009c10970fd8` |
| M3-completion-disabled cold/warm | 3,646,167 bytes | `c9391aacb0fa7e268b67bc4723ffa8958593acbf06e529542c63fc7fa59ba707` | `34bad6b250e18217485d68dfb989cfbc6d9b53f480be71c534f91675dc3c6a3e` |
| Physically add-on-absent stock cold/warm | 614,214 bytes | `30439e78593dacc43a0d3822f039ebbbd058d83be64023650471dab07179c4bc` | `b66f1e00f658d8fbd6ed842aae614b5db7bc94580215b2ad22a2a769e4d73efa` |

Both stock-style modes validated the exact original-resource projection at all
78 M3f anchors: 38 rendered, 40 empty, 1,872 triangles and four M3f resources.
Physical stock additionally retained the ten-triangle stone control, totaling
1,882 triangles and five resources; all 518 legacy non-control anchors stayed
empty. The stable manifests exclude exactly two `live/` and six `rstate/`
files from each captured map tree.

The fixture retained `#m3f_builds ae2m3run = 1`, `#failures ae2m3v = 0` and
`#stable ae2m3s = 2` through full JVM persistence and final restoration without
rebuilding. The restored pod was Ready with
restart count zero; all four init containers completed at exit zero. Four
render workers advanced by 22, 16, 22 and 14 scheduler jiffies. Packaging the
restored 44 paths twice produced byte-identical 20,450,880-byte archives with
exactly 44 safe `ae2_staging/`-prefixed regular entries and SHA-256
`e66abf203481c5df0fa0fc0062c414876f9ef6428cd637de0795f821496c51a9`.

For transparency, the first M3f evidence guard reused the historical M3d
`#builds=1` score. The sole M3f build therefore produced legacy `#builds=2`
and one guard-only verifier failure. The corrected gallery introduced
`#m3f_builds`; the retained, already-built fixture was explicitly adopted at
`#m3f_builds=1`, and no second build occurred. Every subsequent delayed,
unload/reload, full-JVM, route-mode and restored check passed. This was an
evidence-guard/operator-sequencing correction, not an add-on or runtime
failure, and is not characterized as a clean first attempt.

The exact staged candidate aliases are `ae2-addon-m3f-candidate.jar`,
`ae2-m3f-gallery-candidate.zip` and
`ae2-m3f-enabled-candidate-map-2026-08-09.tar.gz`. The hash-exact accepted
aliases are `ae2-addon-m3f-accepted.jar`, `ae2-m3f-gallery-accepted.zip` and
`ae2-m3f-enabled-accepted-map-2026-08-09.tar.gz`. All M3e candidate and
accepted JAR, gallery and map aliases retained their exact hashes. On
2026-08-09 the owner visually accepted the exact corrected JAR, gallery and
archive above, completing M3. At that checkpoint M3f was the latest accepted
slice and M3e was its previous accepted rollback checkpoint. S1 subsequently
completed the post-M3 cable-bus structural-completeness step and received the
bounded acceptance recorded above.

## M3e quantum bridge accepted technical/lifecycle and visual evidence

On 2026-08-08 the exact local M3e artifact passed two byte-identical clean
builds, all 316 Java tests in 53 suites, all 98 Python tests and the complete
technical lifecycle and owner visual review.

| Artifact | Size | SHA-256 |
| --- | ---: | --- |
| Production JAR | 513,674 bytes | `98ff55eaba609fc894b01e0c4d922b47f1871c324945f88f7a34864cf48b124f` |
| Sources JAR | 234,963 bytes | `2bc749373eeb29bd30b9edb58006c7248da1cc09a6abdc7abb404b86a4045a1e` |
| POM | 1,637 bytes | `967132ef80201099cfb1a798f03ff1ac37e0ac84a551694d7276ac20c7ccc136` |
| Gradle module metadata | 2,859 bytes | `d1da10c42393c8a9cb79b77ad67a0b3d15140ef58fef593b20f25710fc8b0e02` |
| Gallery ZIP | 45,009 bytes | `498bac2f82b78451eb24da416ded1d625e5785cc5d3e5910b4c34bfecc05c390` |
| Schema-8 `cases.json` | 3,123,572 bytes | `93963dd0bb60a276e1a17c6dd1f4eb916cd92bef4ef30a2e8bdc7a2bfa818b3e` |

The schema-8 gallery contains 115 cases and 519 anchors: 501 custom anchors,
23,758 custom triangles, 203 resources, 17 empty fallbacks and one ten-
triangle stone control. M3e contributes three cases, 27 anchors, 1,188
triangles and four emitted resources. The schema-7 projection remains frozen
at SHA-256
`c60d2afff5a1f92da4972963fcb926c38093f43bb6d7f550799f104349728a38`.

Each cold/warm pair was byte-identical, and restored enabled exactly matched
the initial enabled evidence:

| Mode/phases | Report | Report SHA-256 | 44-file manifest SHA-256 |
| --- | ---: | --- | --- |
| Enabled cold/warm/restored | 3,519,405 bytes | `d306aff2a0f2eca2882a3d52426140b8caf98aeda76d0722dd14b59ea8a5e9e9` | `b2d61c1dea7bb10dbfbb07a62fd27d1a1a380ca2fbf1b49e06a9f4f232400ca5` |
| Extension-disabled cold/warm | 3,190,022 bytes | `d871f95fd67e9e805829c37260e3dc43558cc4e388332d9771d388857a088f28` | `aef3b77514db808a5013c7692f481121f58cc6ca2122627f103216504880da59` |
| Glass-disabled cold/warm | 3,337,094 bytes | `9a5c1103b55d6637d2b954c681dc15f333d944d2f59d983da2d1bf2645ad405c` | `9e1b684530df1b7c8256fce6cefa8832e4e9327e353cf45dbda95d5aaddda7ab` |
| Crafting-disabled cold/warm | 1,831,965 bytes | `3b103035582b4db56dd81e08a3e20b7c80f839246f7d6a380a542b26994e5205` | `4de9c9bdb5dadca8ca26d07d78c7743f3a3d67ef6d67a29979d3be8143caf76a` |
| Quantum-disabled cold/warm | 3,420,708 bytes | `13c4ca3676868418f9f6797e5b4b4816a38d7ed2c518e1cbb329063ff005e20b` | `f26045142b4aa0df1f262abe95302e7341cb2ceca88fed60d2340d39269178b6` |
| Physically add-on-absent stock cold/warm | 522,872 bytes | `0c3e4333782c244779a55201ab7a6362942e763c858bfdca2348b48204ce5e40` | `1cf7a62eb02a3cc6af5fb3b550c9aee40cfaebf521ededf41b2db95018f39c53` |

Every manifest has 44 entries and totals 3,893 bytes. The fixture was built
once and retained `#stable ae2m3s = 2` and `#failures ae2m3v = 0` through
delayed verification, actual unload/reload, datapack reload, complete JVM
restarts, every mode and restoration. Every capture pod had zero restarts;
the two render threads advanced by 587 and 245 scheduler jiffies. Stock kept
only the stone control and left the other 518 anchors empty.

Packaging the restored 44 files twice produced byte-identical 20,424,799-byte,
44-entry archives with SHA-256
`9e145fffbe87205651ed7cc6b4cb706b7dcbe394ac26e7ce2eb1d6d55ea411a7`.
The exact candidate aliases `ae2-addon-m3e-candidate.jar`,
`ae2-m3e-gallery-candidate.zip` and
`ae2-m3e-enabled-candidate-map-2026-08-08.tar.gz` remain byte-identical to
the accepted aliases `ae2-addon-m3e-accepted.jar`,
`ae2-m3e-gallery-accepted.zip` and
`ae2-m3e-enabled-accepted-map-2026-08-08.tar.gz`. On 2026-08-08 the owner
visually accepted the exact JAR, gallery and archive. M3e was the latest
accepted slice at that checkpoint; M3d remained its previous rollback. The
later exact corrected M3f JAR, gallery and archive completed technical and
human acceptance on 2026-08-09. Publication remained blocked at that
historical checkpoint.

## M3d formed crafting accepted technical/lifecycle and visual evidence

On 2026-08-08 the exact local M3d artifact passed two reproducible clean
builds, all 285 Java and 85 Python tests, the complete technical lifecycle and
owner visual review.

| Artifact | Size | SHA-256 |
| --- | ---: | --- |
| Production JAR | 448,915 bytes | `ca057f025338150255ea916402c08bc8b614f9398a063e7433bbe468808c93ee` |
| Sources JAR | 213,004 bytes | `3e95cabf3e9dcf4ab5c8c2b6d6661ba6464a6cb3e6abd6d33fcfb904b5197c4f` |
| Gallery ZIP | 44,201 bytes | `4a18b45f2c03c8d1d3c49a731df2c2503745952faccf9ba06ec8f301909b81f3` |
| Schema-7 `cases.json` | 3,030,512 bytes | `c60d2afff5a1f92da4972963fcb926c38093f43bb6d7f550799f104349728a38` |
| Enabled map archive | 20,417,822 bytes | `672cdffaf5135f34c4b10c24638056540dcaadbb5fd2d78b3096897436d8a2c6` |

| Mode/phases | Report | Report SHA-256 | 44-file manifest SHA-256 |
| --- | ---: | --- | --- |
| Enabled cold/warm/restored cold/restored warm | 3,369,655 bytes | `f23f43997d680425225706982d504e8df07fb4145543e5cb900b2e6f2dfcdff0` | `06b533030f416bdd1f772cbdcc45ffa3acece219758ddd725e1a637617fcbcc5` |
| Extension-disabled cold/warm | 3,040,920 bytes | `fdf01d9225af70361fd0f9862a689f67e466698f65d2882c20d95b6182d484eb` | `42c332a6ce704db2a5fbc1d4f36ac08fea685fba82deb1408653abfb6410adce` |
| Glass-disabled cold/warm | 3,188,639 bytes | `ef6a7c232fb16fa065fb4c7b4ed90f9d3f1b1a6f73c8598094774ee03b63251a` | `f6191ce6392fca2bdabfe2fc2afede756ada6ffb13ca152cdbc1b136924d6338` |
| Crafting-disabled cold/warm | 1,684,698 bytes | `a8e44c392c585df611f0c818767cba3a4ed20bd83e717ae7c1f3c9de09e9df0b` | `ec89cca1a43fdc2764ee667609ced69cec182d9d298072d219fdb567c9545e25` |
| Physically add-on-absent stock cold/warm | 494,826 bytes | `7253151c5b0188ca238d14fc684a94c4a52392e9ba13f312d76643683840e9b6` | `f3044cc295312b37bfa9b7626c9ff7ec66e1f12601f97484cca17efca00d688b` |

Every cold/warm pair was byte-identical. Restored enabled cold and warm were
also byte-identical to the initial enabled pair. The fixture remained at one
build, zero failures and two stable checks through initial/delayed
verification, true unload/reload, full JVM restarts and all route phases.
Both render threads advanced by 313 and 520 scheduler jiffies; capture pods
had zero restarts. One operator sequencing attempt hit an init failure before
Minecraft started and was not an add-on or capture restart.

The hash-exact accepted PVC aliases are `ae2-addon-m3d-accepted.jar`,
`ae2-m3d-gallery-accepted.zip` and
`ae2-m3d-enabled-accepted-map-2026-08-08.tar.gz`. The candidate aliases
`ae2-addon-m3d-candidate.jar`, `ae2-m3d-gallery-candidate.zip` and
`ae2-m3d-enabled-candidate-map-2026-08-08.tar.gz` remain retained. Neither
alias set is publication or durable evidence storage. The owner's acceptance
is bounded to the exact production JAR, gallery and map archive listed above;
M3d is complete and remains the previous accepted rollback slice. M3e was
later technically validated and visually accepted as recorded above.

## M3c connected quartz glass accepted technical/lifecycle and visual evidence

On 2026-08-08 the exact local `0.1.0-alpha.1-SNAPSHOT` M3c candidate passed two
byte-reproducible clean builds and its source/static gates:

| Artifact | Size | SHA-256 |
| --- | ---: | --- |
| Production JAR | 375,558 bytes | `4c1b557ae4c79c738005b74e2f0c89ca4fbe503dd6ef0ba614fae34d8e449d47` |
| Sources JAR | 186,080 bytes | `6b011719264229629fb1011f4c6de566bf16b13603a6c2412b4ed4ced70a4036` |
| POM | 1,637 bytes | `967132ef80201099cfb1a798f03ff1ac37e0ac84a551694d7276ac20c7ccc136` |
| Gradle module metadata | 2,859 bytes | `5922575b3002d2cc48e1e5e2fa6a795b8b7f11349b1bb0d04f17db7c0b182876` |
| Gallery ZIP | 38,929 bytes | `0839009fe6a4f4785f864f33bc97fef28b8418f077d5d66a20efc3e8eeb4edab` |

The 1,517,248-byte schema-6 `cases.json` has SHA-256
`2d4fbba58ea2c4d3ed741e93a8dd9857523cac9cda021ffd3111e6ac51aec602`.
All 247 Java tests and 71 Python tests passed, together with generated-profile,
gallery, exact AE2/ExtendedAE artifact, production/source-JAR, provenance,
publication-metadata and workflow gates.

The cumulative fixture was built exactly once. Delayed verification, actual
chunk unload/reload, full JVM restarts, extension-disabled, glass-disabled,
physically add-on-absent stock and final restoration each retained
`#failures ae2m3v = 0`, `#builds ae2m3run = 1` and all six critical NBT probes
without calling `build` again. The exact report and web-manifest pairs were:

| Mode/phases | Report | Report SHA-256 | 44-file manifest SHA-256 |
| --- | ---: | --- | --- |
| Enabled cold/warm/restored | 1,582,336 bytes | `e81b8e6eed2047629a933a21e0e345c4880db2b12e264455fa84ef59b63d824f` | `3257a86e895956d7701f056ef46f3188fb4fca2f704b8c7e67641164666221f3` |
| Extension-disabled cold/warm | 1,253,567 bytes | `bd540b437d67830a4899c4437eb396b7229104cc2afe89e5c0a4a7e8d5cbfe32` | `6ac6e2578374d34908cdddf2a451706ac7bc7d1d2831a66757fe6c652015614e` |
| Glass-disabled cold/warm | 1,402,207 bytes | `34a488beb4cc1d4ce6dfee2183e61839304b626ad9b5360f6e302687c4d2b442` | `9d7c9f196eb3e89acadf7eb7bfb1cf4ad85da6d8a4403f7ea4d4e9810ad7250b` |
| Physically add-on-absent stock cold/warm | 415,265 bytes | `08436ad0d03e37a3578552935efc1a6dd5743f5ed8de75d923d2fdf518ff2b0d` | `738d29ddceaa000c867d0616fa9a389010a19672404e76b9f9644368cd4d15fa` |

Enabled cold, warm and restored reports and manifests were byte-identical.
They validated all 406 anchors: 389 custom anchors owned 18,264 custom
triangles and selected all 186 resources, all 16 atomic fallbacks stayed empty,
and the stone control owned ten triangles. Extension-disabled kept all 47 M3c
anchors exact while all 36 M3b anchors stayed empty. Glass-disabled kept the
accepted schema-5 M3b slice exact while all 47 M3c anchors stayed empty. Stock
selected only the stone control and left the other 405 anchors empty. Every
non-enabled cold/warm report and manifest pair was independently byte-
identical. Final restoration reproduced the initial enabled report and all 44
files without rebuilding.

Every accepted capture pod had zero restarts and both configured render threads
accumulated positive CPU time. One operator Ctrl-C while detaching the console
caused a restart attempt during pre-lifecycle setup; it preceded the accepted
captures and was not an add-on restart. Packaging the restored 44 files twice
produced byte-identical 20,376,253-byte archives
with SHA-256
`3fb5fb174f23c0f2d8ce9f98e8c12feb8b12c444060e35b9d8e0036d8ec165e5`.

The staging-PVC candidate aliases `ae2-addon-m3c-candidate.jar` and
`ae2-m3c-gallery-candidate.zip` remain. Owner acceptance added the hash-exact
aliases `ae2-addon-m3c-accepted.jar`, `ae2-m3c-gallery-accepted.zip` and
`ae2-m3c-enabled-accepted-map-2026-08-08.tar.gz`. On 2026-08-08 the owner
visually accepted the exact 375,558-byte production JAR, exact 38,929-byte
gallery ZIP and exact 20,376,253-byte map archive with SHA-256 identities
`4c1b557ae4c79c738005b74e2f0c89ca4fbe503dd6ef0ba614fae34d8e449d47`,
`0839009fe6a4f4785f864f33bc97fef28b8418f077d5d66a20efc3e8eeb4edab`
and
`3fb5fb174f23c0f2d8ce9f98e8c12feb8b12c444060e35b9d8e0036d8ec165e5`.
These aliases are staging conveniences, not publication or durable evidence
storage. The packaged notices, provenance and support matrix in the tested JAR
intentionally retain their build-time pre-runtime/pending checkpoint. Editing
them after this lifecycle would create a different JAR, so these unpackaged
notes carry the post-build technical and human result. This completes M3c.
M3d's later technical lifecycle and human acceptance are recorded above; M3e
quantum acceptance is also recorded above. M3f now combines paint, both Sky
Stone chests, neutral crank, neutral structural Inscriber and locally inferred
static/offline spatial pylons. Its implementation and isolated technical
lifecycle are complete, and the exact corrected artifacts were visually
accepted on 2026-08-09, completing M3. Machine contents, held items, fluids, live/activity-specific
state and accurate Drive LEDs are accepted non-goals. S1 later completed the
post-M3 structural step and remains the accepted predecessor. The M4/M5
implementation is now the owner-accepted checkpoint described above.
Publication was separately authorized on 2026-08-12. Remote creation, tags,
Maven, and root-submodule integration must still follow the exact verified
release sequence.

## M3b Extended Drive accepted technical/lifecycle and visual evidence

On 2026-08-07 the exact local `0.1.0-alpha.1-SNAPSHOT` M3b candidate passed
two reproducible clean builds and its source/static gates:

| Artifact | Size | SHA-256 |
| --- | ---: | --- |
| Production JAR | 323,416 bytes | `f02123cb602bb7b6466d1529c5518e45862f53f413ce9a75ecc067d1a30607d1` |
| Sources JAR | 166,516 bytes | `8574cf425d5e76c527e02eeeda1f3805845cfe9e6cad6f6fa2f6457aa2157909` |
| POM | 1,637 bytes | `967132ef80201099cfb1a798f03ff1ac37e0ac84a551694d7276ac20c7ccc136` |
| Gradle module metadata | 2,859 bytes | `5a739c3cd624ad3c2313bc49c77bfe66d54df6851a321f46ce6f1d94084e5386` |
| Gallery ZIP | 37,614 bytes | `69bdb99d9c8f6838c3b8d5847c32702761cfa77b263ee95384ca24357c84cf92` |

The 1,143,610-byte `cases.json` has SHA-256
`5a1297668b6922b03ae3f2ab089b643aa9521060e61607bbbae353a80d8494fc`.
All 217 Java tests and 56 Python tests passed, along with generated-profile,
gallery, exact AE2/ExtendedAE artifact, production/source-JAR, provenance,
publication-metadata and workflow gates. The exact ExtendedAE input is
5,573,972 bytes with SHA-256
`6652ed1ea4b71f585d48c05a195a77594a7a2bd1ecea0fc805db2122aafad734`.

The cumulative fixture was built exactly once. Delayed verification, actual
chunk unload/reload, datapack reload and complete JVM restarts each retained
`#failures ae2m3v = 0` and `#builds ae2m3run = 1` without calling `build`.
The full twenty-slot Drive, component-bearing native cell, unsupported MEGA
Cells item, KubeJS lava item, invalid count-two AE2 cell and stone item retained
their exact NBT. No players were present during accepted captures.

The enabled cold and warm schema-5 captures validated all 92 cases and 359
anchors. Exactly 342 custom anchors contributed 17,488 triangles and selected
all 167 resources. The 16 atomic fallback anchors owned zero triangles; the
stone control contributed ten, for 17,498 selected triangles. Both
1,335,214-byte reports had SHA-256
`e5e466d9f207beba2032775b66acf091c5c3bf647b40bc811b62a714dae6dd8e`.
All 44 comparable files outside live/`rstate` bookkeeping were byte-identical,
with manifest SHA-256
`1b1db461fc8e76d4e988bd60c403af8601c51cf4d6fac0bfb4e748df5c4c60e9`.
Their signatures were:

| Signature | SHA-256 |
| --- | --- |
| Geometry | `e465a99bb2f0eb07431836f257c571e6744fba6246a5a162c736df252296ff9a` |
| Attributes | `84996f7b32c2698526abe4df09143f0035b995750b81740944db1ae7cb7ebfcc` |
| Material | `f0a3b683fc7949e5f406c05f1aa85f5a1d4fb04da01383d04e3e1541ac9b4e6c` |
| Shape | `e91c1e07670ad643c47fc18454c6311089524614f462e5b2e49d48a095e5a7ce` |

The embedded M3a regression reproduced all four accepted schema-4 signatures.
The Extended Drive front/rear mirror and component-insensitivity gates passed.
For the component pair only, ordinary model blocklight and sunlight are
excluded because BlueMap derives them from neighboring world context;
geometry, materials, UVs, colors and AO still match. The independent
per-anchor contract still requires every LED to be geometrically exact, black,
fullbright and AO one.

The independent extension-disabled cold and warm captures preserved all 310
accepted M3a custom anchors and 12,432 custom triangles while all 36 M3b
anchors owned zero triangles. The stone control contributed ten, for 12,442
selected triangles. Both 1,006,824-byte reports had SHA-256
`2947419d53cf7e2e2f8a4a3cacecc2515eb6aec2382f328e48fb596a98c9d754`;
all 44 comparable files were byte-identical with manifest SHA-256
`f82b43aa87d2f5b8e8e856f769bf138584b242cc53c99a73c5a6752be2de7fa1`.
Their signatures were:

| Signature | SHA-256 |
| --- | --- |
| Geometry | `5934cb141759aaecfdfffffd54fa10361d6037a096003abf6edf052ec9be7344` |
| Attributes | `c29c7ec4f5e8f9228a3b4810dd9204d9071632a861daa67c23d7a620c4a86e8a` |
| Material | `014833bfb50bb84c85752a88a0df33ff2fc3c9c927ad697e45abd400105b1065` |
| Shape | `48f27694e7e6d9e6ccbe70a123d3d59d5408dfd6574b021caccf883ed9e06168` |

The stock deployment physically omitted the add-on while retaining the same
world and gallery. Its cold/warm captures selected only the ten-triangle stone
control; the other 358 anchors remained empty. Both 365,850-byte reports had
SHA-256
`630c96be1406504267e27871e6113b0d98d23da522c9358c31f2f61efaff6f1a`,
and all 44 comparable files were byte-identical with manifest SHA-256
`3ad852e739f4cacceee2fbc275a0ee3c6a231623a3a429fe2d0821915bb4b592`.
Their signatures were:

| Signature | SHA-256 |
| --- | --- |
| Geometry | `f087256e697e8e4f799a0d67840bf911790bcb9f378c34bd37de9a081b84712a` |
| Attributes | `de1ae31e7c84178a9d8c98ac8ff3d76b8738417f08808d363a628d6afa7c27e5` |
| Material | `254ea5afb6632a92605f4d022fc5506c6d72cbcbf2fd1efc8e6ef5f5df749b4e` |
| Shape | `c2b1ab9d9a6c34e3c85044e07dd5bd45107f97d8d1725c1e94567ae4894d00ee` |

The final restored process activated the accepted cable-bus/native-Drive
routes and exact M3b Extended Drive route. It verified the fixture and six NBT
probes with zero failures and one build, then produced cold and warm reports
and manifests byte-identical to the initial enabled captures. Both configured
render threads accumulated 420 and 418 scheduler jiffies. These are a bounded
concurrency smoke, not renderer-only timing or a performance budget.

Every accepted capture pod had zero restarts. One earlier pre-runtime
`verify-and-unpack` init attempt restarted before its verification marker
existed; Minecraft never started, so it produced no add-on restart or runtime
result. It remains recorded as operator sequencing evidence rather than being
folded into the accepted captures.

The deterministic restored map archive was generated twice. Both files are
20,351,418 bytes with SHA-256
`c73844990847148d9cd3d315832085e49776e9253c5c8eca6f0b7659d73c4285`
and contain the same 44 comparable files. This closes the complete M3b
technical lifecycle. On 2026-08-07 the owner visually accepted the exact
323,416-byte JAR, the exact 37,614-byte gallery ZIP and this exact
20,351,418-byte map archive. That decision closes M3b only; the artifacts
remain local and unpublished.

The accepted M3b artifact contains no M3c behavior, and its exact packaged
metadata remains frozen. M3c's separately completed technical lifecycle and
human acceptance are recorded above. At this M3b checkpoint, M3 remained
incomplete; the current M3f technical/visual status is recorded above.

## M3a Drive accepted technical/lifecycle and visual evidence

On 2026-08-07 the exact local `0.1.0-alpha.1-SNAPSHOT` M3a candidate passed two
reproducible clean builds and its source/static gates. The build artifacts
recorded for this checkpoint are:

| Artifact | Size | SHA-256 |
| --- | ---: | --- |
| Production JAR | 259,005 bytes | `55a11805373aebfde821e5009723ec7d672fb290127dbc60131ffa344c99518a` |
| Sources JAR | 136,798 bytes | `40a06a77768f085d4680cb5eae435b35b6115482f231874cd4f13fac73b6edea` |
| POM | 1,637 bytes | `967132ef80201099cfb1a798f03ff1ac37e0ac84a551694d7276ac20c7ccc136` |
| Gallery ZIP | 26,633 bytes | `91057d94890bc3bd063fb7c4bf951f9a70b62a00ee01a9b00deb0fd8f674bb2b` |

No Gradle module-metadata file was present in the retained build output, so no
identity is invented for it. All 173 Java tests and 45 Python tests passed,
along with the generated-profile, gallery, pinned-artifact, JAR-content,
provenance, publication-metadata and workflow gates. The 550,496-byte
`cases.json` has SHA-256
`95e9398ed6c9bf3edf7ceb910b84329fba0227037c003837c628ee0fb657f339`.

The fixture was built exactly once. Delayed verification, actual chunk
unload/reload and a complete JVM restart each reported
`#failures ae2m3v = 0` without calling `build`. The component-bearing native
cell, unsupported `megacells:item_storage_cell_1m`, full ten-slot Drive and
orientation control retained their exact critical NBT. The enabled process
emitted both the accepted cable-bus activation and
`BlueMap AE2 exact 19.2.17 M3a drive route activated.`

The enabled cold and warm schema-4 captures each validated all 76 cases and 323
anchors. Exactly 310 custom anchors contributed 12,432 triangles and selected
all 159 runtime materials. The 12 atomic fallback anchors owned zero
triangles; the stone control contributed ten, for 12,442 selected triangles.
Both 939,040-byte reports had SHA-256
`283f158df62486d92eb36f4a12b25a37381e6315304d1a1fdd76f66da9691e55`
and all 44 comparable files outside live/`rstate` bookkeeping were
byte-identical. Their signatures were:

| Signature | SHA-256 |
| --- | --- |
| Geometry | `ab86a8abc75a695c6ce25e514ff0946a801ddf68012055aa7073fe23208631df` |
| Attributes | `aa6d72d0853b36568dce2422cf46052f54b546b1f122be7214da6b5de85dd7b2` |
| Material | `e304aa44b1ea7c6cd6e39fe170fbfc5f53792621e0e0bbb7e47bf14cee4af2e0` |
| Shape | `c9d145097ababe6dee12afba3f53606a8a19b7a8755da8e209c4d8a8c016d8fb` |

The retained M2 subreport reproduced its accepted signatures exactly. The two
component-insensitivity anchors each owned 106 triangles and matched after
positional normalization.

The stock deployment physically omitted the AE2 add-on JAR while retaining the
same world and gallery. Its cold and warm schema-4 captures selected ten
triangles at the stone control and zero at all other 322 anchors. Both
322,637-byte reports had SHA-256
`ffca251826a5297d4c9b1dfba175e709cb5641b0bf924906d312cd50f57e873d`
and all 44 comparable files were byte-identical. Their signatures were:

| Signature | SHA-256 |
| --- | --- |
| Geometry | `1659242f61a4b8619b4986d8485996b3d7fc7efe90dba4e87ba7b0def4800ccb` |
| Attributes | `281ea25f15f2596d26dcef059e20b40190792cb2cec74c81761abf83d9a96528` |
| Material | `c7bc149e15830027975074f78a21a0fdfbae986bc563a48a01ccdea048afd8a6` |
| Shape | `770b4b9320137e2a8630a86b08da3242e3a0d4e7d60d3bb39c83e019c03fd95d` |

Every accepted capture phase observed zero pod restarts, no unexpected/add-on
restart occurred and both configured render threads accumulated CPU time. One
operator-timing restart attempt occurred during transition away from an
already captured enabled phase; it is explicitly excluded from the accepted
capture phases rather than misreported as an add-on restart.

The final restored pod `minecraft-6c76dd98f-qz74h` was Ready with restart
count zero. The M3 namespace was the only installed AE2 gallery; both exact
core and Drive routes activated. Without calling `build`, `ae2_m3:verify`
reported zero failures and all four critical NBT probes remained unchanged.
The restored 939,040-byte report had the same
`283f158df62486d92eb36f4a12b25a37381e6315304d1a1fdd76f66da9691e55`
SHA-256 as enabled cold/warm. Its 44-file manifest had SHA-256
`414cbdcc4a90d654b02e0a7e1b6d58fcb47f3b76ffa34bb4ac3f26d5ce258651`.
Both were byte-identical to the cold and warm files.

The deterministic map archive was generated twice; both 20,320,750-byte files
had SHA-256
`18cd08c9f0de6bc132bbf9f5cbf1d692d475864ca9aa818eed6166d892a94dce`
and contained the same 44 comparable files. The retained stock lifecycle log
is 63,743 bytes with SHA-256
`44625b1fa2318ce277d5cbbf0b1c7242f40a4ab652d389d99efe7afcd52cb8a2`;
the final restored snapshot log is 533,520 bytes with SHA-256
`7c2f56e939a8214a42c69d1f2346dff2da73a1aedc23340f689d3399b8e97b35`.
Logs remain local evidence and are not repository or release assets.

This closes the complete M3a technical lifecycle. Staging convenience copies
were promoted after owner visual acceptance on 2026-08-07 as
`ae2-addon-m3a-accepted.jar`, `ae2-m3a-gallery-accepted.zip` and
`ae2-m3a-enabled-accepted-map-2026-08-07.tar.gz`, retaining the exact JAR,
gallery and archive identities above. They are local convenience copies, not a
publication or durable evidence store. The accepted M2 files remain exact and
unchanged. M3a was independently accepted; the separately tested M3b artifact
above was also independently accepted and did not inherit M3a acceptance.

## M2 accepted technical/lifecycle and visual evidence

On 2026-08-07 the exact local `0.1.0-alpha.1-SNAPSHOT` M2 candidate completed
its reproducible build gates and the full enabled/stock-absent/re-enabled
isolated lifecycle. Its artifacts were:

| Artifact | Size | SHA-256 |
| --- | ---: | --- |
| Production JAR | 203,599 bytes | `fc11af62359746990a2b35470c1da66e606b13a36be33a5b854d343eebb108d2` |
| Sources JAR | 113,078 bytes | `e6e49ac44da1720ec58cc59f8a33ad006d477e134649eb85bfcd9232a3933290` |
| POM | 1,637 bytes | `967132ef80201099cfb1a798f03ff1ac37e0ac84a551694d7276ac20c7ccc136` |
| Gradle module metadata | 2,859 bytes | `da9cf95a6a4fe99bb77bcd6acdd98c8f9211ef43b0a0fcf18245cccc4c7cafc6` |
| Gallery ZIP | 20,510 bytes | `31ded92dfd927a6dcffe4be81c971233ce2a14b929a978e2f58c7ae3fb40fc82` |

Two clean builds reproduced every listed artifact. All 120 Java tests and 38
Python tests passed, along with the generated-profile, gallery,
pinned-artifact, JAR-content, provenance, publication-metadata and workflow
gates. The generated `cases.json` has SHA-256
`46f1be884675b27d8b6a599ffb9cfafd28610a03fd6f13a9b215cd887a9edadb`.

The deployed profile emitted the exact activation line
`BlueMap AE2 exact 19.2.17 M2 cable-bus profile activated.` The delayed
post-build verifier, the verifier after force-load release and chunk
unload/reload, and the verifier after a complete JVM restart all reported
`#failures ae2m2v = 0`. The cable-anchor fallback without a spin, the
unsupported terminal spin `4b`, the standalone terminal spin `0b` without a
center, and the supported same-face property-free stone facade plus terminal
and center all retained their expected NBT. The load function did not rebuild
the fixture during these probes.

The enabled cold and warm schema-3 captures each selected all 62 cases and 290
anchors. Exactly 278 custom anchors contributed 8,576 triangles and selected
149 runtime materials. The 11 atomic fallback anchors owned zero triangles;
the vanilla stone control contributed the remaining 10, for 8,586 selected
triangles. Both 650,411-byte reports had SHA-256
`cf6020a1c2a5e9e77f7f227aa49de0cf3e4db50a6fdf48b4cd90e66c53375aad`
and all 44 relevant files outside live/`rstate` bookkeeping were
byte-identical. Their signatures were:

| Signature | SHA-256 |
| --- | --- |
| Geometry | `3f0e699ca3aef6867a40526e5b616f369bd269fa967ad0576e616935cd87116f` |
| Attributes | `89306526d33d72eddb19ce2eb2a48d0f8b7616945509ea57a85f5ab39a09e02c` |
| Material | `b6dde2b8f06d2636d27d047425321a57371deaee2f944c19653ad632d37677c3` |
| Shape | `99615cdfba024841027967d65ff02025e4a6d9af11eb1dc025efbe8178553ec5` |

The stock deployment physically omitted the add-on. Its cold and warm
schema-3 captures selected 10 triangles at the vanilla stone control and zero
at the other 289 anchors. Both 284,464-byte reports had SHA-256
`e19d6d4b5486d6d21f80529f097cf5ee03a624558f692848041436c45ecbf656`
and all 44 relevant files were byte-identical. Their signatures were:

| Signature | SHA-256 |
| --- | --- |
| Geometry | `e9d613dd5c75cc7cd93bf5943369cb0d58d02d2e4ebacddee94bc92d0b23a5c3` |
| Attributes | `d26560361c6bfdc614e60d199f6e4091f25aa347831d09830d7de70a68de6483` |
| Material | `5c170eed2eeee35a5f6a861d7b5a1af171d96188df1ac12fdce02f84893a2801` |
| Shape | `f61154507f628e22b2a017f586bc96d9b43d710c3d03d74f15b643d82e38ec6f` |

The final phase restored the exact 203,599-byte M2 JAR and `ae2_staging`
configuration. The same exact activation line was emitted, the verifier again
reported `#failures ae2m2v = 0`, and the four critical NBT states remained
unchanged without calling `build`. The restored 650,411-byte report had the
same
`cf6020a1c2a5e9e77f7f227aa49de0cf3e4db50a6fdf48b4cd90e66c53375aad`
SHA-256 and four signatures as the initial enabled report. All 44 relevant
files were also byte-identical to the initial enabled capture.

Every observed pod stayed at zero restarts and both configured render threads
accumulated nonzero CPU time. This passes the exact M2 technical/lifecycle
candidate. The owner visually accepted the exact JAR and bounded M2 gallery on
2026-08-07, completing M2. That checkpoint unblocked M3 planning and
implementation; the later independently accepted M3a, M3b and M3c evidence is
documented above.
Publication,
remote creation, tagging and root-submodule integration remain blocked through
implemented and human-accepted M5 plus a separate publication decision.

At acceptance time, the staging PVC retained convenience evidence copies:
`ae2-addon-m2-accepted.jar` is 203,599 bytes with SHA-256
`fc11af62359746990a2b35470c1da66e606b13a36be33a5b854d343eebb108d2`,
and `ae2-m2-accepted-map-2026-08-07.tar.gz` is 20,300,438 bytes with SHA-256
`0ef8373dbe85a4e039f6a25340bfca9ba2a742c57f00f3166642d210b07febc2`.
These staging-local copies are neither a durable publication archive nor a
release asset; the exact identities above, not the mutable PVC path, define
what was retained.

## Final M1 technical/lifecycle evidence

On 2026-08-04 the exact local `0.1.0-alpha.1-SNAPSHOT` M1 candidate completed
the isolated lifecycle. Its build artifacts were:

| Artifact | Size | SHA-256 |
| --- | ---: | --- |
| Production JAR | 161,930 bytes | `e02beee7fdafeba9c3ef0ea42deda0a7709cc70df23d4778cfb7a72b1fdaf2e1` |
| Sources JAR | 95,000 bytes | `70a62f64eb524f92667eeed24d480642390daeee125fb03fb1efa7caa473075a` |
| POM | 1,637 bytes | `967132ef80201099cfb1a798f03ff1ac37e0ac84a551694d7276ac20c7ccc136` |
| Gradle module metadata | 2,858 bytes | `256cbba7eac12e90aef509e49c72d762e14644d1820589331c60f767f6e9c59e` |
| Gallery ZIP | 19,198 bytes | `03da6fe12776f49b27f5aa46ae925c5e386da00cc7a28d0da54b6ef8a0402dba` |

Two clean builds reproduced the artifacts. All 89 Java tests and 36 Python
tests passed, as did the production-JAR, provenance, publication-metadata and
`actionlint` gates. The deployed profile emitted the exact activation line
`BlueMap AE2 exact 19.2.17 M1 cable profile activated.`

The enabled initial, warm, post-dense compact, re-enabled and final handoff
captures selected all 48 cases and 269 anchors. Exactly 266 anchors used the
custom contract, contributing 7,576 of the 7,586 selected triangles and all
140 runtime texture resources. Each of the two device-fallback anchors owned
zero triangles. Every comparable capture had these signatures:

| Signature | SHA-256 |
| --- | --- |
| Geometry | `c16c96e0b48024a31c32b2586cd5e9f8fff6b6505383f159ee36bdf3f9411add` |
| Attributes | `1d877c7f37343b05562fb7ca289fcdd4ed3c36da742af6b2bd0254bfee46bf62` |
| Material | `23d1d1294a05f0c39425ff6d0c17ce6ea2eb3ed28a49c321360365dac98c00a8` |
| Shape | `a2b16b722e3895a2ab1a3943d7125f7d9725ccb9d5a78b3c86f9e7da3d6337f2` |

All 44 relevant web files outside live/`rstate` bookkeeping were byte-identical
across those enabled results. The stock restart physically omitted the add-on.
The single stone control owned 10 triangles and all other 268 anchors owned
zero; stock cold and warm had 44 byte-identical relevant files and these
signatures:

| Signature | SHA-256 |
| --- | --- |
| Geometry | `541e8d1554a88abf8c0409a51ff10e944a9f611d62410c2d842036eda6442be4` |
| Attributes | `897278c0d68c6a703dc02245cf034c17265dd670ffe89f8195c4348e82d2e60f` |
| Material | `7c244911f469947b18fe4833b802447a6e89bd6ca7ad3eb28e482066b1c124b4` |
| Shape | `5fa7281d581e6130e8dd81700eef7a1aa133ed563c9e10f44489b64d13c0dd20` |

The opt-in dense report selected exactly 1,024 cells and 63,488 triangles:
12,288 used `ae2:part/cable/core/dense_smart/transparent` and 51,200 used
`ae2:part/cable/dense_covered/transparent`.

| Dense signature | SHA-256 |
| --- | --- |
| Geometry | `722f1043b49a1e1e72b76cec35eddbce91a982d660d915093825a88ed84d3b71` |
| Attributes | `81e8984d29a03b1a01219ea771bbb7b74dd68522dd26e9f6fe301464e7a015ee` |
| Material | `8b09fe4ead18fc7fe1b79bff779c391947ef2e67f547ddcd7a2d88d0df3f69cd` |
| Shape | `5aff1ef214aa80a4682e4da79f6f46c19803f638f53cdc069d37e7b5ca11da6f` |

Across the coarse dense interval, whole-container CPU increased by 39,411,786
microseconds and sampled memory increased by 33,382,400 bytes. These numbers
include the complete container and are neither renderer-only measurements nor
accepted performance budgets. Dense was then cleared and released; the normal
gallery reproduced the earlier compact result byte-for-byte and semantically.

Every accepted pod stayed at zero restarts. No targeted add-on failure was
observed. Each enabled purge emitted one bounded unavailable-neighbor warning,
and no stale M0 diagnostic appeared. At final observation both configured
render threads had nonzero accumulated CPU time, rounded to two and three
seconds.

This evidence passed the M1 technical/lifecycle candidate, and the owner
visually accepted the exact M1 artifact on 2026-08-07. That historical
acceptance did not pre-establish M2, which was independently accepted above.
External repository or remote creation, tags, GitHub Releases, Maven
publication and root-submodule integration remain blocked through implemented
and human-accepted M5.

## Historical accepted M0 evidence

On 2026-08-03 the exact local `0.1.0-alpha.1-SNAPSHOT` M0 production JAR
(123,527 bytes, SHA-256
`84a3b972d86a49a723e56a820ff3b59039654e2108d8ba965493d2302a5b1e41`)
completed the fresh enabled/stock-absent/re-enabled lifecycle. All 17 gallery
anchors were selected. Enabled, warm, restored, instrumented two-render-thread
and immediate-repeat captures each selected 310 triangles with geometry
signature
`bd3113bce247a6964fa25a492e2b2ec89450667f5084d5c8d1fd585589474211`
and attribute signature
`fcd1ecfbb32005db9c30f5039c18fd88eaef8537879b76d2d6f137e712d9e03a`.
All 35 relevant web artifacts outside live/`rstate` bookkeeping were
byte-identical.

The stock restart physically omitted the AE2 add-on and selected 10 triangles
at the same 17 anchors. Its geometry and attribute signatures were
`98137335d9d758646f89fe6539e7e0ea005b87b6daa8c58218d4849417ecb291`
and
`b53288c77c8741efb362a06490e564b61677767c170dac53725073c940d8bf0d`.
Restoring the exact JAR reproduced the initial enabled result. Every accepted
pod stayed at zero restarts, both configured render threads accumulated CPU
time, and the one bounded unsupported-cable fallback warning was expected.

The owner compared this exact M0 gallery in the modded client and BlueMap and
accepted it on 2026-08-04. That acceptance applies only to the
`84a3b972d86a49a723e56a820ff3b59039654e2108d8ba965493d2302a5b1e41`
artifact and bounded M0 fixture.

A later metadata-only correction replaced two narrative `spdx` strings in the
packaged provenance manifest with valid SPDX IDs and separate notes. It
produced a 123,607-byte JAR with SHA-256
`8dfe3952c95342f5a2783a5b86c33fd1c1f3c384cb247a752a994716b1bcb3e4`.
Expanded comparison found exactly one changed entry,
`META-INF/bluemap-ae2/upstreams.json`. That corrected artifact passed its
build/provenance/JAR gates but did not replace the accepted M0 runtime
evidence.

## PRBM evidence analyzer

For the accepted M3f artifact set and each future revalidation, run the repository's
dependency-free schema-9 analyzer against the directory containing
`settings.json`, `textures.json.gz` and `tiles/0`:

```text
python3 tools/analyze_prbm.py \
  --map-root /absolute/path/to/ae2_staging \
  --output /tmp/ae2-m3f-prbm-report.json
```

Add `--include-dense` only after the opt-in dense fixture completes. For a
physically add-on-absent capture, use `--stock-baseline`; to prove independent
route disablement, use `--extension-disabled`, `--glass-disabled`,
`--crafting-disabled`, `--quantum-disabled` or `--m3-completion-disabled` against
the matching map. These modes are mutually exclusive. Compare all four summary
signatures and preserve the reports with the runtime evidence, but do not
commit map output or generated reports.

The parser validates the complete gzip member, exact BlueMap 5.22 PRBM v1
structure, material bounds, matching texture gallery and all 597 manifest
anchors. Enabled mode validates 579 custom anchors, 26,580 custom triangles,
17 zero-triangle fallbacks and the exact 218-resource closure. In addition to native and Extended Drive
checks, M3c validates glass face planes, winding-derived normals, asymmetric
UVs, frame masks, material counts, shared-face absence, white/AO-one
attributes and vibrant blocklight 15. Ordinary blocklight and all sunlight are
world-derived. Extension-disabled requires all 36 M3b anchors empty while M3c
remains exact; glass-disabled requires all 47 M3c anchors empty while the
accepted M3b slice remains exact. Crafting-disabled requires all 86 M3d anchors
empty while the M0-M3c, M3e and M3f totals remain exact. M3e validates all 27
quantum anchors, exact complete-plane role geometry, winding, bounds-mapped
UVs, four emitted static-off resources, neighbor-derived AO and host light.
Quantum-disabled requires those 27 anchors empty while the frozen accepted
schema-7 projection remains exact and M3f remains active. M3f independently
validates all 78 completion anchors, exact paint/chest/crank/Inscriber/pylon
geometry, including all seven invalid-component pylon members, materials, UVs,
winding, AO and host light. M3-completion-disabled requires the exact
original-resource projection at all 78 M3f anchors: 38 rendered, 40 empty and
1,872 triangles, while the accepted
schema-8 custom slice remains exact. Physical stock retains that same M3f
projection plus the ten-triangle stone control; all 518 legacy non-control
anchors are empty. Its inward-biased triangle-
centroid selection is a spatial inference. PRBM retains no block, NBT, mod or
renderer provenance or `BakedQuad shade` flag, so matching signatures
demonstrate stored geometry at the bounded coordinates, not which callback
emitted it or pixel-identical client directional lighting. Historical schema-2
M1, schema-3 M2, schema-4 M3a, schema-5 M3b, schema-6 M3c and schema-7
M3d and schema-8 M3e reports remain evidence only for their exact artifacts
and bounded galleries. The exact M3f enabled cold, warm and restored report is
3,783,797 bytes with SHA-256
`7022a33448dab364cb825a8d67359795560b6a8793b64544ccb0b4c1fda7484e`.
