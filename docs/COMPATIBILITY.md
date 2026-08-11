# Compatibility

## Exact active profile target

The local profile is intentionally pinned to one complete tuple:

| Component | Locked identity |
| --- | --- |
| All the Mons | `1.2.0`, pack commit `c7bb230f21d14d26859d0b92548f089b3a493ad9` |
| Minecraft | `1.21.1` |
| NeoForge | tested pack pin `21.1.248` |
| Java | `21` |
| AE2 | `19.2.17`, SHA-256 `460d779a0609b81409907d9956de8f6f70a1b0912257e3e5c3c7e75ac9630e95` |
| AppliedFlux | `1.21-2.1.5-neoforge`, SHA-256 `57e6a2c0f38e660c9e8416f9081d8c515f5ad096d6793d7b7f039e8e210d245b` |
| ME Requester | `1.21.1-1.4.3`, SHA-256 `68f3c861a802d48afeb6e3a48e8ee4f8633904340ac3f89f17493dc84490e385` |
| Expanded AE | `2.1.1`, SHA-256 `f39c0eb9c6271f54a44ffee092a29520f53000d1005849e6afada3ad9dffba14` |
| MEGA Cells | `4.11.0`, SHA-256 `a386bbf12afb11729b0dcf77f64221893d250f22e6185a4d728b9799b230bc55` |
| Advanced AE | `1.6.12-1.21.1`, SHA-256 `a01d9718667ac13899013e91c5b0b7708b9b9db1da9b8e380772dde54bbe8f41` |
| Athena | `4.0.6`, SHA-256 `43699885bbce3343916d4c5c4940cf0e3f9f6f02fdeb46e8655e121b42282ec5` |
| ExtendedAE | `1.21-2.2.35-neoforge`, SHA-256 `14a2860fa2c747e9dda2279b8933fac6311fecfee166c765171022b902591c65` |
| Glassential | `3.4.5`, SHA-256 `1f0c8f7533bf3b2002575219ba795fd32a44cc5085c2710624ebbf69e6121471` |
| BlueMap upstream | `5.22`, commit `fe5115d5548a30d34175b8e0449aaca280af199f` |
| BlueMap backport | `5.22-agent.backport-5.22-mc1.21.1-2`, commit `9be321df995a1103808621d529eb72773e719d4d`; 6,467,235-byte NeoForge JAR SHA-256 `749f7647fa29764cea113114a7ab3259271bab3da22720989f2bd9fd1f3ba150` |
| BlueMapAPI fork | commit `285c9a60eff3ac2b0cab308ce1058d1565be0971` |

AE2's own runtime metadata permits NeoForge `[21.1.169,)`; this project has
not generalized or tested that broader range. The narrower ATMons pin is the
only runtime target.

The M0-M3f runtime and human-acceptance records below are deliberately
historical: they bind All the Mons `1.1.1` at pack commit
`94a224acf6eace3edf7ea64e6033b458f5bda288`, NeoForge `21.1.234`, Advanced AE
`1.6.11-1.21.1` at SHA-256
`891e1f8ee0f3ac1bbce03fc2848b761f9c52bea4533eb3419ae849582e15ced7`,
ExtendedAE `1.21-2.2.33-neoforge` at SHA-256
`6652ed1ea4b71f585d48c05a195a77594a7a2bd1ecea0fc805db2122aafad734`
and BlueMap backport commit `fe79cf5b9f4d8ca28f4e41c2aeb9ef792e336a8d`.
The All the Mons 1.2.0 S1 checkpoint and M4/M5 candidate do not transfer or
replace those accepted identities.

Runtime activation locates the one JAR declaring AE2 in BlueMap's resource
roots and checks its complete 8,230,896-byte size and SHA-256. Synthetic
dispatch identity and all selected installed texture keys must also pass.
The AE2 artifact gate additionally checks SHA-1/SHA-512, exact metadata, all
85 center-cable IDs, one face-part ID, `ae2:drive`, the closed 23-item native
Drive catalog, 158 AE2 texture digests and the complete 196-resource closure.
The canonical vanilla stone material is separately checked through the
effective operator-installed resource stack.

The ExtendedAE route separately locates the one JAR declaring `extendedae`,
checks either the historical exact 5,573,972-byte 2.2.33 identity or the
current exact 5,578,031-byte 2.2.35 identity, exact
metadata, Java 21 class version, `extendedae:ex_drive`, three built-in cell
IDs and the exact 15-resource/eight-texture partition. It depends on the
accepted AE2 Drive partition but fails closed without disabling accepted AE2
routes.

The disjoint M3c route reuses the already exact-gated AE2 artifact and then
checks its own generated profile plus 22-resource/19-texture partition. Its
synthetic blockstate and renderer identity must match before either exact
native glass ID is rerouted. A missing or invalid M3c route leaves the accepted
core, native Drive and Extended Drive routes unchanged.

The disjoint M3d route verifies its exact eight native crafting blockstates,
seven formed models, fifteen textures and 30-resource partition before
registering `ae2-crafting`. The disjoint M3e route separately verifies exact
`ae2:quantum_link` and `ae2:quantum_ring` blockstates, three source models,
six source textures, two animation metadata files and the exact 13-resource/
3,798-byte closure. Its synthetic blockstate and renderer identity must match
before `ae2-quantum-bridge` reroutes either native ID. Failure of either route
does not alter earlier accepted routes.

The disjoint M3f route verifies six exact native block IDs, five bounded block-
entity identities, exact crank and Inscriber model semantics, 17 source
textures and its 33-resource/22,491-byte closure (recorded by a 3,738-byte
manifest) before `ae2-m3-completion` reroutes any supported block. Its
synthetic blockstates, DTO retention and renderer identities must all match.
Failure leaves M0-M3e active.

The disjoint S1 `ae2-cable-bus-structural` route verifies the exact 29-part
catalog, nine reporting-spin parts, both planes, six P2P frequency parts,
legal dense layouts, facade state/material contracts and 30 native endpoint
state/side identities. It also audits the exact pack-pinned extension and
Minecraft inputs required by those bounded decisions. Missing, modified or
incomplete evidence leaves S1 inactive while preserving the accepted M0-M3f
routes.

The M4/M5 gate verifies the exact AE2 artifact and all seven extension inputs
before activating any of these eight independent routes:

| Route | Required exact input | Additional dependency |
| --- | --- | --- |
| `appflux` | AppliedFlux `1.21-2.1.5-neoforge` | active AE2 core |
| `merequester` | ME Requester `1.21.1-1.4.3` | active AE2 core |
| `expandedae` | Expanded AE `2.1.1` | active AE2 core |
| `megacells` | MEGA Cells `4.11.0` | active AE2 core and exact AE2 monitor resources |
| `advanced-ae-quantum` | Advanced AE `1.6.12-1.21.1` | active AE2 core |
| `advanced-ae-athena` | Advanced AE `1.6.12-1.21.1` plus Athena `4.0.6` | active AE2 core |
| `extendedae-matrix` | ExtendedAE `1.21-2.2.35-neoforge` | active AE2 core |
| `extendedae-planes` | ExtendedAE `1.21-2.2.35-neoforge` | active AE2 core and active S1 native structural route |

`verifyM45PinnedArtifacts` checks complete SHA-1/SHA-256/SHA-512 identities,
eleven exact resource manifests with 375 rows, and the 67-row MEGA Cells cell-
model catalog. Each route independently checks its artifact, resources,
synthetic dispatch where needed, and callback boundary. A route-local mismatch
or failure leaves the other routes unchanged. Loss of the exact AE2 core
blocks all eight; loss of S1 blocks only `extendedae-planes` among them.

The exact bounded M0 runtime artifact
`84a3b972d86a49a723e56a820ff3b59039654e2108d8ba965493d2302a5b1e41`
was human accepted on 2026-08-04. The exact 161,930-byte M1 SNAPSHOT JAR with
SHA-256
`e02beee7fdafeba9c3ef0ea42deda0a7709cc70df23d4778cfb7a72b1fdaf2e1`
subsequently passed its isolated technical/lifecycle gates and was human
visually accepted on 2026-08-07. It is not the M0 artifact and is not a
release. That acceptance did not pre-establish the independently tested M2
result.

M2 is an accepted local implementation for the historical 1.1.1 dependency tuple.
It adds static idle/off `ae2:terminal` byte spins `0..3`, multiple supported
orientations/parts, and only one canonical property-free `minecraft:stone`
facade on the same face as exactly one terminal. The exact staged M2 artifact
is a 203,599-byte SNAPSHOT JAR with SHA-256
`fc11af62359746990a2b35470c1da66e606b13a36be33a5b854d343eebb108d2`.
Its source/build gates and enabled/stock-absent/re-enabled technical lifecycle
passed, and the owner visually accepted this exact artifact and bounded M2
gallery on 2026-08-07. It remains a local SNAPSHOT rather than a release.

M3 implementation, technical validation and human acceptance are complete.
The corrected M3f artifact set was visually accepted on 2026-08-09. Its bounded M3a artifact
adds only native
`ae2:drive` block states and the exact 23-item AE2 19.2.17 catalog under a
`static-offline-unknown` LED policy. The exact 259,005-byte local SNAPSHOT JAR
has SHA-256
`55a11805373aebfde821e5009723ec7d672fb290127dbc60131ffa344c99518a`.
Its reproducible builds, 173 Java/45 Python tests, persistence probes and
complete enabled/physically-absent-stock/re-enabled lifecycle passed. The
restored report and all 44 comparable files were byte-identical to the initial
enabled capture without rebuilding the fixture. The owner visually accepted
that exact artifact and bounded gallery on 2026-08-07.

M3b adds exact ExtendedAE `extendedae:ex_drive` with twenty front/rear slots,
all 24 block states and a closed 26-item catalog. The exact 323,416-byte local
SNAPSHOT JAR has SHA-256
`f02123cb602bb7b6466d1529c5518e45862f53f413ce9a75ecc067d1a30607d1`.
Two reproducible builds, 217 Java/56 Python tests, persistence probes and the
complete enabled/extension-disabled/physically-absent-stock/restored lifecycle
passed. On 2026-08-07 the owner visually accepted this exact JAR, its exact
37,614-byte gallery ZIP with SHA-256
`69bdb99d9c8f6838c3b8d5847c32702761cfa77b263ee95384ca24357c84cf92`
and exact 20,351,418-byte map archive with SHA-256
`c73844990847148d9cd3d315832085e49776e9253c5c8eca6f0b7659d73c4285`.
M3b is an accepted slice.

M3c implements exact `ae2:quartz_glass` and
`ae2:quartz_vibrant_glass` world-block rendering for that historical tuple. The Java
renderer/profile, disjoint 22-resource/19-texture contract and schema-6
11-case/47-anchor evidence lane are complete. The exact 375,558-byte local
SNAPSHOT JAR has SHA-256
`4c1b557ae4c79c738005b74e2f0c89ca4fbe503dd6ef0ba614fae34d8e449d47`.
Reproducible build gates, 247 Java/71 Python tests and the complete isolated
enabled/extension-disabled/glass-disabled/physically-absent-stock/restored
lifecycle passed on 2026-08-08. The gallery is representative; exhaustive Java
tests cover all 64 direct-neighbor masks. On the same date, the owner visually
accepted the exact 375,558-byte production JAR, exact 38,929-byte gallery ZIP
and exact 20,376,253-byte map archive with SHA-256 identities
`4c1b557ae4c79c738005b74e2f0c89ca4fbe503dd6ef0ba614fae34d8e449d47`,
`0839009fe6a4f4785f864f33bc97fef28b8418f077d5d66a20efc3e8eeb4edab`
and
`3fb5fb174f23c0f2d8ce9f98e8c12feb8b12c444060e35b9d8e0036d8ec165e5`.
M3c is therefore a completed and human-accepted historical slice.

M3d implements the independent `ae2-crafting` route for exactly eight native
AE2 19.2.17 formed-crafting blocks. The exact 448,915-byte local SNAPSHOT JAR
has SHA-256
`ca057f025338150255ea916402c08bc8b614f9398a063e7433bbe468808c93ee`.
Two reproducible builds, 285 Java/85 Python tests, persistence probes and the
complete enabled/extension-disabled/glass-disabled/crafting-disabled/
physically-absent-stock/restored technical lifecycle passed on 2026-08-08.
Every cold/warm report and 44-file manifest pair was byte-identical, and both
restored-enabled captures reproduced the initial enabled evidence. On the same
date, the owner visually accepted this exact JAR, the exact 44,201-byte gallery
ZIP with SHA-256
`4a18b45f2c03c8d1d3c49a731df2c2503745952faccf9ba06ec8f301909b81f3`
and the exact 20,417,822-byte map archive with SHA-256
`672cdffaf5135f34c4b10c24638056540dcaadbb5fd2d78b3096897436d8a2c6`.
M3d is therefore the previous completed and human-accepted bounded slice.

M3e adds exact complete formed quantum bridges in the XZ, XY and YZ planes
under the independent `ae2-quantum-bridge` route and a
`static-off-unknown` power policy. The exact 513,674-byte local SNAPSHOT JAR
has SHA-256
`98ff55eaba609fc894b01e0c4d922b47f1871c324945f88f7a34864cf48b124f`.
Two byte-identical clean builds, 316 Java tests in 53 suites, 98 Python tests,
one-build persistence and the complete enabled/extension-disabled/glass-
disabled/crafting-disabled/quantum-disabled/physically-absent-stock/restored
technical lifecycle passed on 2026-08-08. The owner visually accepted the
exact M3e JAR, gallery and map archive that day. Hash-exact accepted aliases
exist, candidate aliases remain, and M3e was the latest accepted slice at that
checkpoint.

M3f adds exact persisted paint, both closed Sky Stone chest variants, neutral
crank and structural Inscriber poses, and locally inferred static/offline
spatial-pylon topology under `ae2-m3-completion`. Exact-source review binds an
uncached native-axis scan capped at 256 pylons: complete invalid L/T components
render unformed BASE plus DIM, while missing, malformed or capped observation
falls back atomically. The exact 623,591-byte local
SNAPSHOT JAR has SHA-256
`ca67c0fc433e43f8e0801ed8d2cccfe47aae317fbc329c099bc8cd741ec3b42b`.
All 365 Java and 116 Python tests passed, and the exact schema-9 gallery and
isolated enabled/M3-completion-disabled/physical-stock/restored lifecycle
passed on 2026-08-09. Physical stock renders 38 M3f anchors and leaves 40
empty. Candidate aliases remain as `ae2-addon-m3f-candidate.jar`,
`ae2-m3f-gallery-candidate.zip` and
`ae2-m3f-enabled-candidate-map-2026-08-09.tar.gz`. The hash-exact accepted
aliases are `ae2-addon-m3f-accepted.jar`, `ae2-m3f-gallery-accepted.zip` and
`ae2-m3f-enabled-accepted-map-2026-08-09.tar.gz`. The owner visually accepted
the exact corrected JAR, gallery and map archive on 2026-08-09, completing M3.
At that checkpoint M3f was the latest accepted slice and M3e was its previous
accepted rollback checkpoint. The prior M3f candidate was withdrawn after
visual review exposed invisible L/T pylon members; it was never accepted, and this corrected
artifact supersedes it.

S1 is implemented for the current All the Mons 1.2.0 tuple as an accepted
local checkpoint. Two clean Eclipse Temurin `21.0.12+8` / Python `3.13.14`
builds reproduced its exact artifacts. Each ran 448 Java tests (446 passed and
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

The artifact set retains the accepted 49,679-byte schema-9 gallery
`21ceec072cc3263a41bdb81874e897d48d5a1ce5e1c7d3ac3c0de3063818ee6c`
and 3,314,082-byte cases file
`75e6ba2f40631a95f20cfa00d7ca952e521bc2c7a4eb155926334a223a945f3a`
immutably. Its exact isolated route-isolation/physical-stock/restoration
lifecycle passed on 2026-08-11. Initial and restored enabled cold/warm output
shares report SHA-256
`14aa3b46386bead1f656f9796305c0000e835e5948ae06367d947a3afe837723`
and 46-file manifest SHA-256
`e1e592faabd263e1b9bacce14d56577f330d1b5cbd80336f2bd1563d3f1b2a78`.
The owner visually accepted the exact S1 JAR, gallery and map archive in
BlueMap on 2026-08-11. S1 is the latest exact human-accepted local checkpoint. No
broader BlueMap, AE2, Minecraft, NeoForge or pack-version compatibility
follows from this exact result.

M4/M5 is the complete owner-accepted local checkpoint for the exact
All the Mons 1.2.0 tuple above. The Java runtime audit and exact M45 verifier
passed. Two byte-identical builds each ran 562 Java tests (560 passed and two
intentional skips) and 180 pre-oracle Python tests; the final frozen-oracle
CPython 3.13.14 suite passed 192/192 tests. The exact schema-11 evidence and
enabled initial/restored, combined-disabled, crafting-disabled, and native-
structural-disabled cold/warm lifecycle modes passed. This is an exact runtime
compatibility result for that tuple and was owner visually accepted on
2026-08-12; it is not a broader compatibility claim.

## Version behavior

- Human-accepted runtime evidence: the exact M0 through S1 artifacts and their
  bounded routes described in `COVERAGE.md` have completed human visual
  acceptance.
- Accepted M2 evidence: exact reproducible build artifacts, generated
  profile/gallery, source tests, NBT persistence through delayed verification,
  chunk unload/reload and full JVM restarts, deterministic enabled/stock
  cold/warm stored-map captures, and byte-identical restored enabled output.
  Human visual review of the exact artifact and bounded gallery was accepted
  on 2026-08-07.
- Accepted M3a evidence: exact reproducible build artifacts, generated
  schema-4 profile/gallery, source tests, NBT persistence through delayed
  verification, actual chunk unload/reload and full JVM restarts, plus
  deterministic enabled and physically add-on-absent stock cold/warm stored
  maps and byte-identical restored-enabled output, followed by owner visual
  acceptance on 2026-08-07.
- Accepted M3b evidence: exact reproducible artifacts, schema-5 gallery,
  source tests, one-build persistence across actual unload/reload and full JVM
  restarts, deterministic enabled, independently extension-disabled and
  physically add-on-absent stock cold/warm maps, plus byte-identical restored
  output, followed by exact owner visual acceptance on 2026-08-07.
- Accepted M3c evidence: exact reproducible artifacts, schema-6
  gallery, 247 Java/71 Python tests, one-build persistence across actual
  unload/reload and full JVM restarts, deterministic enabled, independently
  extension-disabled, independently glass-disabled and physically add-on-
  absent stock cold/warm maps, plus byte-identical restored output on
  2026-08-08, followed by exact owner visual acceptance that day.
- Accepted M3d evidence: exact reproducible artifacts, schema-7
  gallery with a frozen schema-6 regression, 285 Java/85 Python tests,
  one-build persistence with zero failures and two stable checks, deterministic
  enabled/extension-disabled/glass-disabled/crafting-disabled/stock cold and
  warm maps, two exact restored-enabled captures, and owner visual acceptance
  of the exact JAR, gallery and map archive on 2026-08-08.
- Accepted M3e evidence: exact reproducible artifacts, schema-8 gallery with
  a frozen accepted schema-7 projection, 316 Java tests in 53 suites, 98
  Python tests, one-build persistence with zero failures and two stable checks,
  deterministic enabled/extension-disabled/glass-disabled/crafting-disabled/
  quantum-disabled/stock cold and warm maps, and exact restored-enabled output
  on 2026-08-08, followed by owner visual acceptance of the exact JAR, gallery
  and archive that day.
- Accepted M3f evidence: exact final artifacts, schema-9 gallery with a
  byte-frozen accepted schema-8 projection, 365 Java tests, 116 Python tests,
  exactly one M3f fixture build, zero failures and two stable checks,
  deterministic enabled/M3-completion-disabled/physical-stock cold and warm
  maps, exact restored-enabled output and a reproduced 44-entry map archive on
  2026-08-09, followed by owner visual acceptance of the exact corrected JAR,
  gallery and archive that day.
- Accepted S1 local evidence: two reproducible exact-toolchain
  builds, each with 448 Java tests (446 passed and two intentional exporter
  skips), 167 passing Python tests, all gates, schema-10 source/profile/gallery gates and
  independent appended/legacy oracles. Enabled analyzer totals are 940 custom
  anchors, 64,938 custom triangles, 289 resources, 16 fallbacks and 64,948
  selected triangles across 150 cases/957 anchors. The exact enabled/native-
  structural-disabled/physical-stock/restored lifecycle passed on 2026-08-11;
  cold/warm pairs were byte-identical, restoration reproduced initial enabled,
  fixture counters stayed at one M3f build/one S1 build/two stable/zero
  failures, every pod had zero restarts, and both render workers advanced.
  The owner visually accepted the exact JAR, gallery and map archive in BlueMap
  on 2026-08-11; hash-exact accepted aliases exist alongside the candidate
  aliases on `data-atm120`.
- Owner-accepted M4/M5 evidence: a clear Java runtime
  audit, the exact eight-artifact/eleven-manifest M45 verifier, two byte-
  identical builds, 562 Java tests per build (560 passed and two intentional
  skips), 180 pre-oracle Python tests per build, and a final 192/192 CPython
  3.13.14 suite. Schema-11 live-map evidence and the isolated cold/warm/full-
  JVM lifecycle passed. The owner accepted the exact JAR, gallery, and map
  archive in BlueMap on 2026-08-12.
- AE2 absent: the add-on is silent and BlueMap remains stock.
- AE2 present but unknown, modified or incomplete: warn once and keep stock
  routing.
- Exact profile with malformed or unsupported block data: use the original
  AE2 resource for that block. For M3c, missing or malformed native-neighbor
  data falls back atomically; a known non-native neighbor is a definite
  disconnection.
- Unknown BlueMap internals: keep every add-on route inactive.

There is no compatibility override. Supporting a later AE2 or BlueMap version
requires a new reviewed profile, exact evidence and the same runtime gates.
The implemented M3a lane makes no claim for extension cells. M3b supports only
the exact ExtendedAE block and three built-in ExtendedAE items in addition to
the accepted AE2 catalog; dynamic KubeJS cells, other extension cells, later
items and later inventory schemas remain unsupported. M3c connected quartz
glass has completed exact builds, isolated technical validation and human
acceptance. BlueMap cannot perform the
client's reciprocal `getAppearance` checks, so cross-mod appearance proxies do
not connect. BlueMap 5.22 PRBM also cannot encode AE2's client
`BakedQuad shade=false` flag; no pixel-identical directional-lighting claim is
made. Client particles and item rendering remain out of scope. The accepted
M3b artifact contains no M3c behavior. M3d formed crafting blocks are
implemented, technically validated and human accepted only for the historical
All the Mons 1.1.1 tuple recorded with that evidence. Exact
unformed states stay stock; monitor transient displayed stacks are omitted;
known compatible MEGA Cells/Expanded AE crafting neighbors fall back rather
than being guessed. M3e formed quantum bridges are implemented and technically
validated only for complete isolated native `3x3x1` structures. Their
power-state animation and particles are omitted, and no persisted-NBT power
claim is made. Human visual review passed for the exact bounded M3e artifact
set on 2026-08-08. M3f is implemented and technically validated under
`ae2-m3-completion` for paint, both Sky Stone chests, neutral crank, neutral
structural Inscriber and locally inferred static/offline spatial pylons,
including the bounded complete-invalid-component unformed projection.
Machine contents, held items, fluids, live or activity-specific state and
accurate Drive LEDs are accepted non-goals, not deferred work. The bounded
2026-08-09 M3f acceptance completes M3. S1 now implements the post-M3
cable-bus structural-completeness step as a local candidate covering native
face-part chassis/planes/P2P, dense layouts, bounded exact facades and native
attached-device structural geometry. The accepted dynamic-state non-goals do
not move into S1. Its runtime gate and bounded owner BlueMap visual review
passed on 2026-08-11; extension work remains independently gated.
M4/M5 now implements exact static profiles for the six pack-pinned extension
families and Athena dependency through the eight routes listed above. This
does not widen any version range. Its exact runtime and visual acceptance is
bounded to the tuple above. Live power, channels, activity, requests,
displayed stacks, inventories, fluids, animations and accurate LEDs remain
excluded. Publication was separately authorized on 2026-08-12 and remains an
exact non-SNAPSHOT release gate.

## Operator disable switch

The `ae2` profile can be disabled before JVM startup with either a comma-
separated Java system property or environment variable:

```text
-Dbluemap.ae2.disabledProfiles=ae2
BLUEMAP_AE2_DISABLED_PROFILES=ae2
```

Both sources are merged when present, so either can disable the profile.
Values other than known exact profile names do not enable compatibility. A
restart is required after changing the switch.

Use `extendedae` to disable the Extended Drive plus both current ExtendedAE
M4/M5 routes while retaining exact AE2 and other-family routes:

```text
-Dbluemap.ae2.disabledProfiles=extendedae
BLUEMAP_AE2_DISABLED_PROFILES=extendedae
```

Disabling `ae2` also leaves every dependent ExtendedAE, connected-glass,
formed-crafting, quantum-bridge, M3-completion, S1, and M4/M5 route inactive.

Use `ae2-quartz-glass` to disable only the M3c connected-glass route while
retaining every accepted M0-M3b route:

```text
-Dbluemap.ae2.disabledProfiles=ae2-quartz-glass
BLUEMAP_AE2_DISABLED_PROFILES=ae2-quartz-glass
```

Use `ae2-crafting` to disable only the M3d formed-crafting route while
retaining every accepted M0-M3c route:

```text
-Dbluemap.ae2.disabledProfiles=ae2-crafting
BLUEMAP_AE2_DISABLED_PROFILES=ae2-crafting
```

Use `ae2-quantum-bridge` to disable only the M3e quantum route while
retaining every accepted M0-M3d route:

```text
-Dbluemap.ae2.disabledProfiles=ae2-quantum-bridge
BLUEMAP_AE2_DISABLED_PROFILES=ae2-quantum-bridge
```

Use `ae2-m3-completion` to disable only the M3f static-structure route while
retaining M0-M3e:

```text
-Dbluemap.ae2.disabledProfiles=ae2-m3-completion
BLUEMAP_AE2_DISABLED_PROFILES=ae2-m3-completion
```

Use `ae2-cable-bus-structural` to disable only the S1 structural expansion
while retaining the accepted M0-M3f routes and their predecessor decisions:

```text
-Dbluemap.ae2.disabledProfiles=ae2-cable-bus-structural
BLUEMAP_AE2_DISABLED_PROFILES=ae2-cable-bus-structural
```

The M4/M5 operator IDs are the eight internal route IDs. Each disables only
its owning route, except the canonical `extendedae` alias above, which also
covers both ExtendedAE M4/M5 routes:

```text
-Dbluemap.ae2.disabledProfiles=appflux,merequester,expandedae,megacells
-Dbluemap.ae2.disabledProfiles=advanced-ae-quantum,advanced-ae-athena
-Dbluemap.ae2.disabledProfiles=extendedae-matrix,extendedae-planes
```

Disabling `extendedae-planes` does not disable the matrix or Extended Drive;
disabling `extendedae-matrix` does not disable the planes or Drive. Disabling
either Advanced AE route leaves the other route unchanged. A restart is
required for every change.

The schema-10 disabled contract retains 589 custom anchors, 27,188 custom
triangles, 218 resources and 17 fallbacks. At the appended S1 positions it
renders the exact ten-anchor/608-triangle/14-resource predecessor projection,
leaves 350 empty and leaves all ten legacy-upgrade positions empty. The exact
4,289,919-byte cold/warm report and 46-file manifest observed this result in
the 2026-08-11 isolated lifecycle, at SHA-256
`63e528d6aa3c033cd6b2251f7a569cc0e4e7dc4bfba81d75862f8fa7a416e274`
and
`7cb0b48aa938109d8c001d32f01375aba7f29f7f9b8c5f96a927b52335a7df03`.

All disablement is restart-scoped; a disabled route cannot be reactivated by a
resource reload in the same JVM.

## Family support matrix

| Module | Pack-pinned version | Current state |
| --- | --- | --- |
| AE2 | `19.2.17` | Exact ATM 1.2.0 S1 structural lifecycle and owner BlueMap visual review accepted on 2026-08-11; exact core input for the technically validated M4/M5 candidate |
| AppliedFlux | `1.21-2.1.5-neoforge` | M4 static part and 20-cell Drive integration implemented; exact technical lifecycle passed; visual review pending |
| ME Requester | `1.21.1-1.4.3` | M4 requester block and terminal implemented; exact technical lifecycle passed; visual review pending |
| Expanded AE | `2.1.1` | M4 I/O Port, two parts and 21 formed-crafting blocks implemented; colorable Drive remains intentional stock fallback; exact technical lifecycle passed; visual review pending |
| MEGA Cells | `4.11.0` | M5 eight crafting blocks, three parts, Cell Dock and 67-cell integration implemented; exact technical lifecycle passed; visual review pending |
| Advanced AE | `1.6.12-1.21.1` | M5 eight quantum blocks and Athena-backed quantum-alloy CTM implemented; exact technical lifecycle passed; visual review pending |
| Athena | `4.0.6` | Exact dependency for Advanced AE frame-zero CTM; no independent content route; technical lifecycle passed with `advanced-ae-athena`; visual review pending |
| ExtendedAE | `1.21-2.2.35-neoforge` | Exact Extended Drive retained; M5 six matrix blocks and two planes implemented; exact M5 technical lifecycle and owner visual review passed |
| Glassential | `3.4.5` | Current S1 facade-material evidence input; no dedicated rendering profile |

Presence in the packaged support matrix is not runtime acceptance. The eight
M4/M5 route IDs have accepted exact profiles only for this tuple; their
schema-11, isolated lifecycle, and owner review do not imply broader support.
M4/M5 is the latest accepted checkpoint and S1 is its accepted predecessor.
The separate accepted native AE2
routes and exact Extended Drive retain their documented boundaries.
