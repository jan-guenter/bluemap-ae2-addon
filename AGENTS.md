# Agent guide: BlueMap AE2 Add-on

Read this file, `README.md`, `docs/ARCHITECTURE.md`,
`docs/PROVENANCE.md`, and `docs/RELEASING.md` before changing the project.
Portfolio evidence remains in the parent workspace under
`bluemap-addons/ae2/` and is not source to copy wholesale.

## Exact current local target

| Component | Locked identity |
| --- | --- |
| All the Mons | `1.2.0`, pack commit `c7bb230f21d14d26859d0b92548f089b3a493ad9` |
| Minecraft | `1.21.1` |
| NeoForge | `21.1.248` |
| Java | `21` |
| AE2 | `19.2.17`, SHA-1 `49c18d6a4af487957d7e5a6ad5dcbf71090b8e14`, SHA-256 `460d779a0609b81409907d9956de8f6f70a1b0912257e3e5c3c7e75ac9630e95` |
| AE2 source | tag `neoforge/v19.2.17`, commit `79ee2c704ad62941a426c26b1cb1f76ef5b2ee5a` |
| AppliedFlux | `1.21-2.1.5-neoforge`, SHA-256 `57e6a2c0f38e660c9e8416f9081d8c515f5ad096d6793d7b7f039e8e210d245b` |
| ME Requester | `1.21.1-1.4.3`, SHA-256 `68f3c861a802d48afeb6e3a48e8ee4f8633904340ac3f89f17493dc84490e385` |
| Expanded AE | `2.1.1`, SHA-256 `f39c0eb9c6271f54a44ffee092a29520f53000d1005849e6afada3ad9dffba14` |
| MEGA Cells | `4.11.0`, SHA-256 `a386bbf12afb11729b0dcf77f64221893d250f22e6185a4d728b9799b230bc55` |
| Advanced AE | `1.6.12-1.21.1`, SHA-256 `a01d9718667ac13899013e91c5b0b7708b9b9db1da9b8e380772dde54bbe8f41` |
| Athena | `4.0.6`, SHA-256 `43699885bbce3343916d4c5c4940cf0e3f9f6f02fdeb46e8655e121b42282ec5` |
| ExtendedAE | `1.21-2.2.35-neoforge`, SHA-256 `14a2860fa2c747e9dda2279b8933fac6311fecfee166c765171022b902591c65` |
| ExtendedAE source | tag `1.21-2.2.35-neoforge`, commit `3776bc854458301bbcc9a44a8238d70a0e3dc00d` |
| Glassential | `3.4.5`, SHA-256 `1f0c8f7533bf3b2002575219ba795fd32a44cc5085c2710624ebbf69e6121471` |
| BlueMap upstream | `5.22`, commit `fe5115d5548a30d34175b8e0449aaca280af199f` |
| BlueMap backport | `5.22-agent.backport-5.22-mc1.21.1-2`, commit `9be321df995a1103808621d529eb72773e719d4d`; 6,467,235-byte NeoForge JAR SHA-256 `749f7647fa29764cea113114a7ab3259271bab3da22720989f2bd9fd1f3ba150` |
| BlueMapAPI fork | commit `285c9a60eff3ac2b0cab308ce1058d1565be0971` |

The human-accepted M0-M3f artifacts remain historical All the Mons `1.1.1`
(pack commit `94a224acf6eace3edf7ea64e6033b458f5bda288`) / NeoForge
`21.1.234` evidence. That historical closure used Advanced AE
`1.6.11-1.21.1` (SHA-256
`891e1f8ee0f3ac1bbce03fc2848b761f9c52bea4533eb3419ae849582e15ced7`),
ExtendedAE `1.21-2.2.33-neoforge` (SHA-256
`6652ed1ea4b71f585d48c05a195a77594a7a2bd1ecea0fc805db2122aafad734`)
and BlueMap backport commit `fe79cf5b9f4d8ca28f4e41c2aeb9ef792e336a8d`.
These identities below must not be relabeled as All the Mons 1.2.0 results.

The exact M0 runtime artifact
`84a3b972d86a49a723e56a820ff3b59039654e2108d8ba965493d2302a5b1e41`
was accepted by human comparison in both the modded client and BlueMap on
2026-08-04. That acceptance applies only to the bounded M0
`ae2:fluix_glass_cable` gallery and that exact JAR.

The exact 161,930-byte M1 SNAPSHOT JAR with SHA-256
`e02beee7fdafeba9c3ef0ea42deda0a7709cc70df23d4778cfb7a72b1fdaf2e1`
passed its reproducible build and isolated technical/lifecycle gates on
2026-08-04 and was human visually accepted on 2026-08-07. That acceptance
applies only to this exact JAR and its bounded M1 behavior.

M2 is the completed and accepted local milestone. It retains all accepted M1
behavior and adds the exact `ae2:terminal` face part in the static idle/off
pose for spins `0..3`, including multiple supported orientations and parts.
Its only facade lane is one canonical, property-free `minecraft:stone` facade
on the same face as exactly one terminal; every other part, facade or topology
uses whole-block stock fallback. The exact input closure is 85 center IDs, one
face-part ID, 148 AE2 textures and 170 resources. The exact 203,599-byte local
M2 SNAPSHOT JAR has SHA-256
`fc11af62359746990a2b35470c1da66e606b13a36be33a5b854d343eebb108d2`.
Its schema-3 gallery has 62 cases and 290 anchors. Build gates and the complete
enabled/stock-absent/re-enabled technical lifecycle have passed, including
persistence across delayed verification, chunk unload/reload and full JVM
restarts. The restored exact JAR reproduced the initial enabled report and all
44 relevant web files without rebuilding the fixture. Human visual acceptance
of that exact JAR and bounded M2 gallery was recorded on 2026-08-07. Do not
transfer that acceptance to another artifact, version or broader topology.

M3 implementation, technical validation and human acceptance are complete.
The owner visually accepted the exact corrected M3f JAR, gallery and map archive on
2026-08-09; that bounded decision completes M3. Its first bounded slice, M3a,
adds only the
native `ae2:drive` for all 24 persisted `facing`/`spin` states and the exact
23 AE2 19.2.17 item IDs accepted by the Drive catalog. Ordinary Anvil data
does not preserve online state, so occupied slots use the explicit
`static-offline-unknown` LED policy. Unknown or extension cell IDs, malformed
inventory/state data and every condition outside that exact lane use
whole-block stock fallback.

The exact 259,005-byte local M3a SNAPSHOT JAR with SHA-256
`55a11805373aebfde821e5009723ec7d672fb290127dbc60131ffa344c99518a`
passed two reproducible clean builds, 173 Java tests, 45 Python tests, the
generated profile/gallery gates, delayed/chunk/full-JVM persistence probes,
and the complete isolated enabled/physically-absent-stock/re-enabled lifecycle
on 2026-08-07. Its schema-4 gallery has 76 cases and 323 anchors. The restored
pod verified the fixture and four critical NBT probes without invoking
`build`, then reproduced the initial 939,040-byte report and all 44 comparable
web files byte-for-byte. The restored report SHA-256 is
`283f158df62486d92eb36f4a12b25a37381e6315304d1a1fdd76f66da9691e55`;
the 44-file manifest SHA-256 is
`414cbdcc4a90d654b02e0a7e1b6d58fcb47f3b76ffa34bb4ac3f26d5ce258651`.
The complete technical lifecycle passed, and the owner visually accepted this
exact M3a JAR and bounded gallery on 2026-08-07. That decision closes M3a only;
never transfer it to a different artifact or to M3b.

M3b is the completed and accepted Extended Drive slice. It adds the
exact pack-pinned `extendedae:ex_drive` through an independently fail-closed
route. The strict lane supports all 24 `facing`/`spin` states, 20 persisted
inventory slots split as front `0..9` and rear `10..19`, the accepted 23-item
AE2 catalog plus three built-in ExtendedAE cells, and 15 occupied models. The
rear face uses the opposite facing and the same spin. Empty Extended Drives
emit 116 triangles; each occupied slot adds six chassis and ten black
fullbright offline-unknown LED triangles, for exactly `116 + 16N`.

The exact 323,416-byte M3b SNAPSHOT JAR with SHA-256
`f02123cb602bb7b6466d1529c5518e45862f53f413ce9a75ecc067d1a30607d1`
passed two reproducible builds, 217 Java tests, 56 Python tests and the
complete isolated enabled/extension-disabled/physically-absent-stock/restored
lifecycle on 2026-08-07. Its schema-5 gallery has 92 cases and 359 anchors.
Enabled output contains 342 custom anchors, 17,488 custom triangles, 167
selected resources and 16 zero-leak fallbacks. Enabled cold, warm and restored
reports are byte-identical at 1,335,214 bytes with SHA-256
`e5e466d9f207beba2032775b66acf091c5c3bf647b40bc811b62a714dae6dd8e`;
their 44-file manifests have SHA-256
`1b1db461fc8e76d4e988bd60c403af8601c51cf4d6fac0bfb4e748df5c4c60e9`.
The extension-disabled and stock cold/warm pairs are independently
byte-identical. The fixture was built once, verified with zero failures across
the lifecycle and retained all six critical NBT probes. On 2026-08-07 the
owner visually accepted this exact JAR, the exact 37,614-byte gallery ZIP with
SHA-256
`69bdb99d9c8f6838c3b8d5847c32702761cfa77b263ee95384ca24357c84cf92`
and the exact 20,351,418-byte accepted map archive with SHA-256
`c73844990847148d9cd3d315832085e49776e9253c5c8eca6f0b7659d73c4285`.
That acceptance closes M3b only.

M3c connected quartz glass is complete and human accepted. It supports only
exact property-free `ae2:quartz_glass` and
`ae2:quartz_vibrant_glass`, using a disjoint 22-resource/19-texture route. Its
representative schema-6 gallery adds 11 cases, 47 anchors and 776 triangles to
the frozen M3b slice; Java tests remain authoritative for all 64 direct-
neighbor masks.

The exact 375,558-byte M3c SNAPSHOT JAR with SHA-256
`4c1b557ae4c79c738005b74e2f0c89ca4fbe503dd6ef0ba614fae34d8e449d47`
passed reproducible build gates, 247 Java tests, 71 Python tests and the
complete isolated enabled/extension-disabled/glass-disabled/physically-absent-
stock/restored lifecycle on 2026-08-08. The 38,929-byte gallery ZIP has
SHA-256
`0839009fe6a4f4785f864f33bc97fef28b8418f077d5d66a20efc3e8eeb4edab`;
the 1,517,248-byte schema-6 `cases.json` has SHA-256
`2d4fbba58ea2c4d3ed741e93a8dd9857523cac9cda021ffd3111e6ac51aec602`.
Enabled cold, warm and restored reports are byte-identical at 1,582,336 bytes
with SHA-256
`e81b8e6eed2047629a933a21e0e345c4880db2b12e264455fa84ef59b63d824f`;
their 44-file manifests have SHA-256
`3257a86e895956d7701f056ef46f3188fb4fca2f704b8c7e67641164666221f3`.
All route-isolation pairs were independently byte-identical, and the exact
20,376,253-byte enabled map archive was reproduced twice with SHA-256
`3fb5fb174f23c0f2d8ce9f98e8c12feb8b12c444060e35b9d8e0036d8ec165e5`.
The fixture remained at one build and zero failures through delayed, unload,
full-JVM, route-mode and restored verification without rebuilding. Every
accepted capture pod had zero restarts and both render threads accumulated
CPU time. One operator Ctrl-C while detaching the console caused a restart
attempt during pre-lifecycle setup; it preceded the accepted captures and was
not an add-on restart.

On 2026-08-08 the owner visually accepted this exact 375,558-byte JAR, the
exact 38,929-byte gallery ZIP and the exact 20,376,253-byte map archive with
SHA-256
`3fb5fb174f23c0f2d8ce9f98e8c12feb8b12c444060e35b9d8e0036d8ec165e5`.
That decision completes M3c and is bounded to those three identities. The
hash-exact accepted PVC aliases are `ae2-addon-m3c-accepted.jar`,
`ae2-m3c-gallery-accepted.zip` and
`ae2-m3c-enabled-accepted-map-2026-08-08.tar.gz`; the candidate aliases remain.
The packaged notices, provenance and support metadata in the tested M3c JAR
intentionally retain their build-time pending checkpoint; changing them would
change the accepted JAR. Post-build technical and human evidence belongs in
unpackaged documentation.

M3d formed crafting blocks are implemented, have passed the complete local
technical lifecycle and were human visually accepted on 2026-08-08. The exact
448,915-byte accepted JAR has SHA-256
`ca057f025338150255ea916402c08bc8b614f9398a063e7433bbe468808c93ee`;
the 213,004-byte sources JAR has SHA-256
`3e95cabf3e9dcf4ab5c8c2b6d6661ba6464a6cb3e6abd6d33fcfb904b5197c4f`.
Two clean builds reproduced the artifact, and all 285 Java and 85 Python
tests passed. The exact 44,201-byte schema-7 gallery ZIP has SHA-256
`4a18b45f2c03c8d1d3c49a731df2c2503745952faccf9ba06ec8f301909b81f3`;
its 3,030,512-byte `cases.json` has SHA-256
`c60d2afff5a1f92da4972963fcb926c38093f43bb6d7f550799f104349728a38`.

The cumulative fixture was built exactly once and retained zero verifier
failures and two consecutive stable checks through initial, delayed, actual
chunk unload/reload and full-JVM probes. Enabled, extension-disabled,
glass-disabled, crafting-disabled and physically add-on-absent stock
cold/warm pairs were independently byte-identical; restored enabled cold and
warm reproduced the initial enabled report and 44-file manifest exactly. The
exact 20,417,822-byte enabled map archive has SHA-256
`672cdffaf5135f34c4b10c24638056540dcaadbb5fd2d78b3096897436d8a2c6`.
Both render threads advanced by 313 and 520 scheduler jiffies, and every
accepted capture pod had zero restarts. One operator sequencing attempt hit an
init failure before Minecraft started; it was not an add-on or capture restart.

The hash-exact accepted PVC aliases are `ae2-addon-m3d-accepted.jar`,
`ae2-m3d-gallery-accepted.zip` and
`ae2-m3d-enabled-accepted-map-2026-08-08.tar.gz`. The corresponding candidate
aliases remain retained; neither alias set is a publication store. M3d is
accepted and complete as a bounded slice.

M3e quantum bridges are implemented and passed the complete local technical
lifecycle and owner visual review on 2026-08-08. The
exact 513,674-byte candidate JAR has SHA-256
`98ff55eaba609fc894b01e0c4d922b47f1871c324945f88f7a34864cf48b124f`;
the 234,963-byte sources JAR has SHA-256
`2bc749373eeb29bd30b9edb58006c7248da1cc09a6abdc7abb404b86a4045a1e`.
The exact 1,637-byte POM remains
`967132ef80201099cfb1a798f03ff1ac37e0ac84a551694d7276ac20c7ccc136`,
and the 2,859-byte Gradle module metadata is
`d1da10c42393c8a9cb79b77ad67a0b3d15140ef58fef593b20f25710fc8b0e02`.
Two clean builds reproduced all four identities. All 316 Java tests across 53
suites and all 98 Python tests passed.

The exact 45,009-byte schema-8 gallery ZIP has SHA-256
`498bac2f82b78451eb24da416ded1d625e5785cc5d3e5910b4c34bfecc05c390`;
its 3,123,572-byte `cases.json` has SHA-256
`93963dd0bb60a276e1a17c6dd1f4eb916cd92bef4ef30a2e8bdc7a2bfa818b3e`
and embeds the accepted schema-7 M0-M3d view exactly at SHA-256
`c60d2afff5a1f92da4972963fcb926c38093f43bb6d7f550799f104349728a38`.
Across 115 cases and 519 anchors, 501 custom anchors own 23,758 custom
triangles and select 203 resources; 17 atomic fallbacks own zero triangles.
The three M3e cases contain 27 custom anchors and 1,188 triangles across one
complete formed bridge in each XZ, XY and YZ plane.

The one-build fixture retained `#failures ae2m3v = 0` and
`#stable ae2m3s = 2` through delayed verification, actual unload/reload, full
JVM restarts, enabled, extension-disabled, glass-disabled, crafting-disabled,
quantum-disabled, physically add-on-absent stock and restored-enabled phases.
Every cold/warm report and 44-file manifest pair was byte-identical; restored
enabled exactly reproduced initial enabled. Every capture pod had zero
restarts, and both render threads advanced by 587 and 245 scheduler jiffies.
The exact 20,424,799-byte enabled candidate map archive was reproduced twice
with SHA-256
`9e145fffbe87205651ed7cc6b4cb706b7dcbe394ac26e7ce2eb1d6d55ea411a7`.

Candidate PVC aliases remain for M3e:
`ae2-addon-m3e-candidate.jar`, `ae2-m3e-gallery-candidate.zip` and
`ae2-m3e-enabled-candidate-map-2026-08-08.tar.gz`. The hash-exact accepted
aliases are `ae2-addon-m3e-accepted.jar`, `ae2-m3e-gallery-accepted.zip` and
`ae2-m3e-enabled-accepted-map-2026-08-08.tar.gz`. Both sets are staging
conveniences, not publication inputs or durable evidence storage. M3d remains
the previous accepted rollback slice; M3e was the latest human-accepted slice
at that checkpoint.

M3f is complete and human accepted under profile ID `ae2-m3-completion`. It
covers
persisted paint splotches, both closed Sky Stone chest variants, a neutral
crank, the neutral structural Inscriber and locally inferred static/offline
spatial pylons. The exact 623,591-byte accepted JAR has SHA-256
`ca67c0fc433e43f8e0801ed8d2cccfe47aae317fbc329c099bc8cd741ec3b42b`;
the 276,986-byte sources JAR is
`2a3bb3713ff56731992d405a58fc6a137dcfc8fff43467de7196ad33c444795c`.
All 365 Java tests and 116 Python tests passed. The exact 49,679-byte schema-9
gallery ZIP is
`21ceec072cc3263a41bdb81874e897d48d5a1ce5e1c7d3ac3c0de3063818ee6c`,
and its 3,314,082-byte `cases.json` is
`75e6ba2f40631a95f20cfa00d7ca952e521bc2c7a4eb155926334a223a945f3a`.

Exact-source review corrected spatial-pylon behavior: an uncached native-axis
scan, capped at 256 pylons, renders every member of a fully observed L- or
T-shaped invalid component as AE2's unformed `NONE` role with BASE plus DIM
materials. Only missing, malformed or capped component observation uses
atomic original-resource fallback.

The prior M3f candidate was withdrawn after owner visual review exposed the
invisible L/T pylon members. It was never accepted. The corrected artifact
supersedes it and was visually accepted on 2026-08-09; the decision is bounded
to the exact JAR, gallery and map archive recorded here.

The isolated enabled, independently M3-completion-disabled, physically
add-on-absent stock and restored-enabled lifecycle passed on 2026-08-09. The
enabled cold, warm and restored reports are byte-identical at 3,783,797 bytes
with SHA-256
`7022a33448dab364cb825a8d67359795560b6a8793b64544ccb0b4c1fda7484e`;
their exact 44-file manifests are byte-identical with SHA-256
`259af4eea91a32acc07d1572e8f3f42e6276b46999496d0380ec009c10970fd8`.
The physical stock pair is byte-identical and correctly contains 38
stock-rendered plus 40 stock-empty M3f anchors. The deterministic 44-entry map
archive was reproduced twice at 20,450,880 bytes with SHA-256
`e66abf203481c5df0fa0fc0062c414876f9ef6428cd637de0795f821496c51a9`.
The candidate aliases `ae2-addon-m3f-candidate.jar`,
`ae2-m3f-gallery-candidate.zip` and
`ae2-m3f-enabled-candidate-map-2026-08-09.tar.gz` remain hash-exact. The
hash-exact accepted aliases are `ae2-addon-m3f-accepted.jar`,
`ae2-m3f-gallery-accepted.zip` and
`ae2-m3f-enabled-accepted-map-2026-08-09.tar.gz`. Neither alias set is a
publication store. At that checkpoint M3f was the latest accepted slice, M3e
was its previous accepted rollback checkpoint, and the withdrawn pre-fix
candidate remained retained only for audit/rollback provenance. Machine
contents, held items, fluids, live or activity-specific state and accurate
Drive LEDs are accepted non-goals, not deferred work.

S1 implements that post-M3 cable-bus structural-completeness step under
profile ID `ae2-cable-bus-structural`. It covers the remaining 29 native face
parts, planes, six P2P types and frequency pixels, legal dense layouts, the
exact bounded facade-state/material contract and 30 native attached-device
connection identities. Its exact All the Mons 1.2.0 isolated lifecycle passed
on 2026-08-11, and the owner visually accepted the exact JAR, gallery and map
archive in BlueMap on 2026-08-11. S1 remains the latest exact human-accepted
local checkpoint and is unreleased.

Two clean Eclipse Temurin `21.0.12+8` / Python `3.13.14` builds reproduced the
All the Mons 1.2.0-retargeted S1 artifact set. Each build ran 448 Java tests (446
passed and two opt-in exporter tests were intentionally skipped) and 167
passing Python tests; all gates passed. Its exact local
identities are:

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

The schema-10 gallery retains the accepted 3,314,082-byte schema-9 cases file
and 49,679-byte gallery ZIP byte-exactly at SHA-256
`75e6ba2f40631a95f20cfa00d7ca952e521bc2c7a4eb155926334a223a945f3a`
and
`21ceec072cc3263a41bdb81874e897d48d5a1ce5e1c7d3ac3c0de3063818ee6c`.
Its 28 appended cases contain 360 anchors: 351 custom anchors, 37,518 custom
triangles, 96 resources, 2,093 material rows and nine zero-triangle
fallbacks. Ten retained schema-9 positions are upgraded through a separate
840-triangle, 21-resource, 70-material-row oracle. The combined structural
closure is therefore 370 positions, 361 custom anchors, 38,358 triangles, 96
resources and 2,163 material rows.

Enabled schema 10 contains 150 cases and 957 anchors: 940 custom anchors own
64,938 custom triangles, 16 fallbacks own zero triangles, 289 resources are
selected and the stone control brings selected output to 64,948 triangles.
Native-structural-disabled mode retains 589 custom anchors, 27,188 custom
triangles, 218 resources, 17 fallbacks and 27,198 selected triangles; exactly
10 appended anchors use the 608-triangle/14-resource predecessor projection,
350 appended anchors are empty and all ten legacy upgrades are empty.
Physical stock selects 1,882 triangles across five resources and leaves 918
anchors empty, including all 360 appended anchors and all ten legacy-upgrade
positions. The 2026-08-11 isolated lifecycle observed every one of these
contracts in byte-identical cold/warm captures. Initial and restored enabled
also matched exactly: their 5,657,463-byte analyzer report has SHA-256
`14aa3b46386bead1f656f9796305c0000e835e5948ae06367d947a3afe837723`,
and their 46-file comparable manifest has SHA-256
`e1e592faabd263e1b9bacce14d56577f330d1b5cbd80336f2bd1563d3f1b2a78`.

Native-structural-disabled cold/warm produced the exact 4,289,919-byte report
`63e528d6aa3c033cd6b2251f7a569cc0e4e7dc4bfba81d75862f8fa7a416e274`
and manifest
`7cb0b48aa938109d8c001d32f01375aba7f29f7f9b8c5f96a927b52335a7df03`.
Physical stock cold/warm produced the exact 993,266-byte report
`a1e148fde5af118def7e379c44a2294ca5d9485a60824ad2764911f1805788a4`
and manifest
`9539d160733a7616ec9092e82086539b4db725e0afab2b04f5111a77e0da66f0`.
The fixture remained at `#m3f_builds=1`, `#s1_builds=1`, `#stable=2` and
`#failures=0` through unload/reload, a full JVM restart, route disablement,
physical removal and restoration. Every capture pod had zero restarts and
every init container exited zero; the restored exact route set activated on
the `-2` BlueMap host and both render workers advanced by 650 and 747 CPU
ticks. Two deterministic 46-member enabled archives reproduced exactly at
20,660,117 bytes with SHA-256
`0f57f33a205124c67069263cce0af8d74fa04343397317c4e491275df41558cb`.
The owner visually accepted that exact JAR, gallery and map archive in BlueMap
on 2026-08-11. The hash-exact candidate aliases
`ae2-addon-s1-candidate.jar`, `ae2-s1-gallery-candidate.zip` and
`ae2-s1-enabled-candidate-map-2026-08-11.tar.gz` remain retained. Their
hash-exact accepted aliases are `ae2-addon-s1-accepted.jar`,
`ae2-s1-gallery-accepted.zip` and
`ae2-s1-enabled-accepted-map-2026-08-11.tar.gz`. Both alias sets were verified
on `data-atm120`; the historical PVC was untouched. The final visual-review
pod was Ready with zero restarts, every initializer exited zero, every exact
route activated and the fixture verified at `#m3f_builds=1`, `#s1_builds=1`,
`#stable=2` and `#failures=0`. `save-all flush` completed before staging was
scaled to zero. These aliases are staging conveniences, not a publication
store.

The S1 non-lighting invariant excludes only environment-derived blocklight
and sunlight. Full observed attribute signatures retain both light channels;
every triangle must have a flat in-range light pair, and the four smart-channel
resources remain exactly fullbright.

M4/M5 is the complete owner-accepted All the Mons 1.2.0 local checkpoint.
Its eight independently fail-closed routes are `appflux`,
`merequester`, `expandedae`, `megacells`, `advanced-ae-quantum`,
`advanced-ae-athena`, `extendedae-matrix`, and `extendedae-planes`. The exact
input set is AppliedFlux `1.21-2.1.5-neoforge`, ME Requester `1.21.1-1.4.3`,
Expanded AE `2.1.1`, MEGA Cells `4.11.0`, Advanced AE `1.6.12-1.21.1`, Athena
`4.0.6`, and ExtendedAE `1.21-2.2.35-neoforge`, all on the exact AE2 and host
tuple above.

The exact verifier locks all eight input artifacts, eleven resource manifests
with 375 rows, and the 67-row MEGA Cells cell-model catalog. The Java runtime
audit is clear. Two byte-identical builds each ran 562 Java tests (560 passed
and two opt-in exporter tests were intentionally skipped) and 180 pre-oracle
Python tests. The frozen-oracle CPython 3.13.14 suite then passed 192/192 tests
in 945.343 seconds with the generator/checksum closure.

The exact accepted local identities are:

| Artifact | Size | SHA-256 |
| --- | ---: | --- |
| Production JAR | 1,207,683 bytes | `6fed7a625b02229213a047788995944f14e7e7fcabe0e0ddc6d9b5e994146e9f` |
| Sources JAR | 532,979 bytes | `4a45c60f8512630c6bd9735e26018d019ebe99d58f2c87fa2f3c46e101b624d8` |
| POM | 1,637 bytes | `967132ef80201099cfb1a798f03ff1ac37e0ac84a551694d7276ac20c7ccc136` |
| Gradle module metadata | 2,861 bytes | `3f0ba24c34ef535c99cbd6dabcd7d6bb0f784ca6ffd032f06faaf9a9b5d7b0b8` |
| Schema-11 gallery ZIP | 94,537 bytes | `c67b4f794092f6e994349a8ee9320c052e2efc87f04e8813faf158c3455fe33b` |
| Schema-11 `cases.json` | 6,017,554 bytes | `914dab6931077521959cf59260a1ffb0cdbe105385f43880763b289f8117ec55` |
| M4/M5 main oracle | 221,769 bytes | `c2ce69bed949306551ca4ff6cdebf7fac88f0f2f2fa7ab294d3312f363e1b448` |
| M4/M5 legacy-upgrade oracle | 2,336 bytes | `2319ecf576ba07b123078c720d941990fac939033d375e5853f51bf98348c3c7` |
| Enabled map archive | 20,821,895 bytes | `44422aa71c2f450951d8433e25e01de7a0b00dbd0d9c4fa4ff74ca98e649a2df` |

The isolated lifecycle passed enabled initial/restored, combined-disabled,
crafting-disabled, and native-structural-disabled cold/warm modes. Initial and
restored enabled output is byte-identical; full-JVM transitions and the
restart, initializer, worker, one-build, and settle gates passed. The exact
candidate aliases remain on `data-atm120` beside hash-exact accepted aliases
`ae2-addon-m5-accepted.jar`, `ae2-m5-gallery-accepted.zip`, and
`ae2-m5-enabled-accepted-map-2026-08-11.tar.gz`. The owner accepted those exact
artifacts in BlueMap and separately authorized publication on 2026-08-12. S1
is the accepted predecessor. Release preparation is authorized, but do not
claim a release, Maven publication, tag, or root integration until each action
has actually completed and been independently verified.

## Project invariants

- This is a plain BlueMap add-on, not a NeoForge mod. Add no Mixins, Minecraft
  registrations, client hooks, payloads or AE2 runtime dependency.
- Never bundle BlueMap, BlueNBT, AE2, Minecraft, NeoForge, modpack resources,
  worlds, screenshots, chunks or third-party JARs.
- Compile against the exact Java 21 BlueMap backport and keep internal calls
  inside `adapter/bluemap522`.
- Unknown BlueMap/AE2 builds stay inactive. Unsupported or typed-malformed
  block data that reaches the add-on callback uses the direct original-resource
  fallback without blocking that tile. Raw NBT corruption before BlueMap's
  typed resolution remains host behavior.
- Register the bounded cable-bus, native Drive, Extended Drive and Crafting
  Monitor DTOs before BlueNBT freezes its resolver, but do not deserialize NBT
  in the add-on entrypoint.
- Registry insertion is verified by object identity; BlueMap 5.22's registry
  return value is not trusted.
- The cable-bus transient policy is `idle-off-unknown`; Drive LEDs use
  `static-offline-unknown`. Never claim live power, channels, connection or
  Drive online state from ordinary Anvil data.
- There is no unload lifecycle. Install, update, disable and remove require a
  JVM restart. Write no required world state.
- Diagnostics are bounded and location-free. Log no NBT, coordinates, paths,
  player data or resource contents.
- M1's smart-cable channel overlays represent the exact zero-channel static
  policy, not recovered live network state.
- M2 supports only the exact `ae2:terminal` byte spins `0..3` on non-dense
  accepted centers and the one same-face canonical stone-facade layout.
  Unsupported combinations discard partial custom output and use stock
  rendering for the complete cable-bus block.
- M3a supports only `ae2:drive`, its 24 exact `facing`/`spin` states and its
  closed 23-ID AE2 cell/item catalog. Treat item components as irrelevant
  bounded payload, render no live contents or online state, and fall back for
  unknown namespaces—including AE2-family extensions—rather than guessing a
  chassis model.
- M3b supports only exact `extendedae:ex_drive`, its 24 states, twenty exact
  slots and the closed 26-ID catalog. Its ExtendedAE route depends on the
  accepted AE2 Drive resource partition but can be disabled or fail without
  disabling the accepted AE2 routes. Dynamic KubeJS cells, other extension
  cells, malformed counts and unknown items use atomic whole-block fallback.
- M3b component-insensitivity compares renderer-controlled geometry,
  materials, UVs, colors and AO while excluding ordinary model blocklight and
  sunlight derived from neighboring world context. The independent per-anchor
  gate still requires every synthetic LED to be geometrically exact, black,
  fullbright and AO one.
- M3c supports only the two exact native quartz-glass IDs. Known non-native
  `getAppearance` proxies are definite disconnections because BlueMap exposes
  no reciprocal client appearance query; missing or malformed native state
  uses atomic original-resource fallback. PRBM cannot preserve AE2's
  `shade=false` quad flag, so pixel-identical directional lighting is not
  claimed. Client particles and item rendering remain out of scope.
- M3d supports only the eight exact native AE2 19.2.17 crafting blocks under
  profile ID `ae2-crafting`. Exact unformed states use stock resources; exact
  formed states use the source-derived cube geometry. Missing or malformed
  native neighbors and the closed known MEGA Cells/Expanded AE compatible set
  use atomic fallback. Other non-native neighbors are disconnected. Monitor
  `paintedColor` is a strict byte `0..16`; the transient displayed stack is
  deliberately omitted. A crafting-route failure must not disable M0-M3c.
- M3e supports only exact `ae2:quantum_link` and `ae2:quantum_ring` under
  profile ID `ae2-quantum-bridge`. Custom output requires one complete,
  unambiguous formed 3x3x1 native bridge in the XZ, XY or YZ plane. Persisted
  `formed` and `waterlogged` state plus exact neighboring block states define
  the route; client-stream-only role, corner, power and particle state is not
  decoded. Emit the source-exact formed geometry, bounds-mapped UVs,
  neighbor-derived ambient occlusion, host world light and deterministic
  `static-off-unknown` materials. Missing, malformed, incomplete or ambiguous
  topology uses atomic original-resource fallback. Do not claim powered
  overlays, particles, NBT power recovery or extension connectors. An M3e
  route failure must not disable M0-M3d.
- M3f supports only exact `ae2:paint`, both native Sky Stone chest IDs,
  `ae2:crank`, `ae2:inscriber` and `ae2:spatial_pylon` under profile ID
  `ae2-m3-completion`. Persisted paint dots, closed chests, neutral crank and
  Inscriber poses, and an uncached native-axis pylon scan capped at 256 define
  the route. Fully observed invalid bend or branch components render AE2's
  unformed BASE-plus-DIM role; missing, malformed or capped observation uses
  atomic original-resource fallback. An M3f route failure must not disable
  M0-M3e.
- S1 uses independently fail-closed profile ID
  `ae2-cable-bus-structural`. It renders only source-locked static
  off/inactive/unlocked structure for the 29 native face parts, bounded P2P
  frequencies, legal dense layouts, exact supported facade states and exact
  native endpoint-side/state contracts. Unknown compatible extension
  endpoints, malformed retained input and unsupported layouts use atomic
  whole-cable-bus fallback. Disabling or failing S1 must preserve the accepted
  M0-M3f routes. Its exact All the Mons 1.2.0 isolated lifecycle passed on
  2026-08-11, and the owner visually accepted its exact JAR, gallery and map
  archive that day. Bind that acceptance to those exact identities and do not
  treat it as publication authorization or broader compatibility.
- M4/M5 routes are exact and independently fail closed. `appflux` owns its
  exact Drive-cell and Flux Accessor part support; `merequester` owns its
  exact requester block and terminal; `expandedae` owns its exact I/O Port,
  face parts and 21 formed-crafting blocks; `megacells` owns its eight formed-
  crafting blocks, three generic parts, Cell Dock and 67-item cell catalog;
  `advanced-ae-quantum` owns the eight quantum-computer blocks;
  `advanced-ae-athena` owns the Athena-backed quantum-alloy CTM;
  `extendedae-matrix` owns the six Assembler Matrix blocks; and
  `extendedae-planes` owns the two cable-bus planes. A route-local artifact,
  resource, registration, bake, callback or linkage failure disables only its
  owner. An AE2 core failure blocks all M4/M5 routes; an S1 native-structural
  failure additionally blocks only `extendedae-planes`. The canonical
  `extendedae` operator switch disables both ExtendedAE M4/M5 routes.
- All M4/M5 output is a static world projection. Preserve only exact durable
  block state, bounded retained NBT and observed topology. Live power,
  channels, activity, jobs, displayed stacks, machine/cell contents, fluids,
  animation and accurate LEDs remain non-goals. Missing, malformed, unknown
  or unsupported observations use atomic original-resource fallback; never
  infer transient state to fill a visual gap.
- Keep source under `LGPL-3.0-only` unless a reviewed file records another
  compatible SPDX and provenance lane.

## Validation

The seven `m45*Jar` properties and `verifyM45PinnedArtifacts` task are the
current All the Mons 1.2.0 M4/M5 gate. The `extendedAeJar`, `advancedAeJar`
and `verifyCraftingPinnedArtifact` inputs below preserve historical accepted
M3b/M3d evidence. The four `nativeStructural*Jar` properties are the current
All the Mons 1.2.0 S1 inputs and must not be substituted for the historical
properties.

```bash
python3 -m unittest discover -s tools/tests -p 'test_*.py' -v
./gradlew --no-daemon clean check build
./gradlew --no-daemon generatePomFileForAddonPublication \
  generateMetadataFileForAddonPublication verifyPublicationPom
./gradlew --no-daemon \
  -Pae2Jar=/absolute/path/appliedenergistics2-19.2.17.jar \
  verifyPinnedArtifact
./gradlew --no-daemon \
  -PextendedAeJar=/absolute/path/ExtendedAE-1.21-2.2.33-neoforge.jar \
  verifyExtendedAePinnedArtifact
./gradlew --no-daemon \
  -Pae2Jar=/absolute/path/appliedenergistics2-19.2.17.jar \
  verifyQuartzGlassPinnedArtifact
./gradlew --no-daemon \
  -Pae2Jar=/absolute/path/appliedenergistics2-19.2.17.jar \
  -PmegaCellsJar=/absolute/path/megacells-4.11.0.jar \
  -PexpandedAeJar=/absolute/path/expandedae-2.1.1.jar \
  -PadvancedAeJar=/absolute/path/advancedae-1.6.11-1.21.1.jar \
  -PextendedAeJar=/absolute/path/ExtendedAE-1.21-2.2.33-neoforge.jar \
  verifyCraftingPinnedArtifact
./gradlew --no-daemon \
  -Pae2Jar=/absolute/path/appliedenergistics2-19.2.17.jar \
  -Pae2SourcesJar=/absolute/path/appliedenergistics2-19.2.17-sources.jar \
  verifyQuantumBridgePinnedArtifact
./gradlew --no-daemon \
  -Pae2Jar=/absolute/path/appliedenergistics2-19.2.17.jar \
  -Pae2SourcesJar=/absolute/path/appliedenergistics2-19.2.17-sources.jar \
  verifyM3CompletionPinnedArtifact
./gradlew --no-daemon \
  -Pae2Jar=/absolute/path/appliedenergistics2-19.2.17.jar \
  -Pae2SourcesJar=/absolute/path/appliedenergistics2-19.2.17-sources.jar \
  -PmegaCellsJar=/absolute/path/megacells-4.11.0.jar \
  -PexpandedAeJar=/absolute/path/expandedae-2.1.1.jar \
  -PnativeStructuralAdvancedAeJar=/absolute/path/AdvancedAE-1.6.12-1.21.1.jar \
  -PnativeStructuralExtendedAeJar=/absolute/path/ExtendedAE-1.21-2.2.35-neoforge.jar \
  -PnativeStructuralGlassentialJar=/absolute/path/Glassential-renewed-1.21.1-3.4.5.jar \
  -PnativeStructuralBlueMapJar=/absolute/path/bluemap-5.22-agent.backport-5.22-mc1.21.1-2-neoforge.jar \
  -PminecraftClientJar=/absolute/path/minecraft-1.21.1-client.jar \
  verifyNativeStructuralPinnedArtifact
./gradlew --no-daemon \
  -Pae2Jar=/absolute/path/appliedenergistics2-19.2.17.jar \
  -Pm45AppFluxJar=/absolute/path/AppliedFlux-1.21-2.1.5-neoforge.jar \
  -Pm45MeRequesterJar=/absolute/path/merequester-neoforge-1.21.1-1.4.3.jar \
  -Pm45ExpandedAeJar=/absolute/path/expandedae-2.1.1.jar \
  -Pm45MegaCellsJar=/absolute/path/megacells-4.11.0.jar \
  -Pm45AdvancedAeJar=/absolute/path/AdvancedAE-1.6.12-1.21.1.jar \
  -Pm45AthenaJar=/absolute/path/athena-neoforge-1.21.1-4.0.6.jar \
  -Pm45ExtendedAeJar=/absolute/path/ExtendedAE-1.21-2.2.35-neoforge.jar \
  verifyM45PinnedArtifacts
```

Also validate `provenance/upstreams.json`, `tools/analyzer-upstreams.json`,
`gallery/SHA256SUMS`, and the production JAR. `check` also runs the
dependency-free exact schema-11 PRBM evidence analyzer fixture, its frozen
schema-10 predecessor and earlier regressions, and the retained S1 and M4/M5
oracles. Record no runtime,
visual or performance result unless that exact gate ran and its evidence was
retained.
