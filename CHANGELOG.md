# Changelog

## Unreleased

## [0.1.0-alpha.4] - 2026-09-02

- Migrate the exact adapter boundary to the BlueMap 5.23 feature backport at
  commit `7e07f4e74ec1e92a6ead9aa1e66054af3e133aac`, retaining the exact
  BlueMapAPI commit `285c9a60eff3ac2b0cab308ce1058d1565be0971`.
- Consume the shared BlueMap 5.23 Adapter API at commit
  `e81f08bc4bfbf02d810ec8949a019130e2e61634` for runtime admission,
  identity-safe registry insertion, and resource-extension factories.
- Preserve the accepted AE2 and extension route behavior, profiles, and
  gallery contract. This is an unpublished integration-review candidate, not
  an owner-accepted or releasable artifact.

## [0.1.0-alpha.3] - 2026-08-25

- Add a bounded data-only registration API for soft-dependent BlueMap add-ons
  to contribute exact cable-bus part definitions and native Drive-cell model
  mappings. The API accepts no renderer or resource callbacks.
- Reject duplicate or reserved route, part, and item IDs atomically; freeze
  definitions before resource rendering; keep owner-controlled route state
  mutable after freeze; and isolate terminal failures to the owning route.
- Add exact neutral P2P support for persisted `Short` frequencies and retain
  existing output unchanged when no external definition is registered.
- Document the exact Ars Energistique `2.1.1-beta` registration for three part
  types and five source-cell mappings. All the Mons `1.2.0` staging loaded it
  with AE2 `19.2.17` and Ars Nouveau `5.13.0`; the owner accepted 25 cells,
  including all three parts across six faces and all five source cells in one
  Drive, on 2026-08-25. This is bounded staging evidence, not a production
  claim.

## [0.1.0-alpha.2] - 2026-08-19

- Record exact schema-12 runtime validation and owner visual acceptance of the
  Applied Mekanistics review gallery. The seven anchors rendered exactly
  `250/106/106/106/74/74/18` triangles; the naturally refreshed pressurized
  tube retained its east acceptor arm against the full-block ME Interface.
- Authorize publication of the exact 1,224,691-byte production JAR with
  SHA-256
  `ef4dfac36af57bfd161c701db1bf87ee024465db0952d33ca87a9933fb3d1182`.

- Add one exact, independently gated Applied Mekanistics 1.6.3 chemical-cell
  route against Mekanism 10.7.19: ten cell IDs map to five chassis models in
  native AE2 Drives only.
- Preserve route-local fail-closed behavior for missing, overridden, malformed,
  inactive, or callback-failing third-party resources; no upstream classes or
  assets are bundled.
- Extend schema-12 evidence tooling with a seven-anchor review lane covering
  four AppMek Drives and three existing parent-renderer seam regressions while
  retaining the byte-exact schema-11 predecessor projection.

## [0.1.0-alpha.1] - 2026-08-12

- Publish immutable prerelease `v0.1.0-alpha.1` from the protected tag. The
  workflow reproduced all four release outputs twice, verified the exact six
  release assets, attested the production and sources JARs, and published
  Maven coordinate `io.github.jan-guenter:bluemap-ae2-addon:0.1.0-alpha.1`.
  The release production JAR is 1,207,650 bytes with SHA-256
  `8372b4f043f3091f6aa1fed6a76006e5e04cbf462647472beb392c9789da9e03`.

- Record owner BlueMap visual acceptance on 2026-08-12 of the exact M4/M5
  production JAR, schema-11 gallery, and deterministic enabled map archive.
  Bind hash-exact accepted aliases `ae2-addon-m5-accepted.jar`,
  `ae2-m5-gallery-accepted.zip`, and
  `ae2-m5-enabled-accepted-map-2026-08-11.tar.gz` beside the retained candidate
  aliases. The owner separately authorized first publication; the exact
  non-SNAPSHOT release-version gate subsequently passed.

- Complete the All the Mons `1.2.0` M4/M5 implementation as a local review
  candidate for exact AppliedFlux `1.21-2.1.5-neoforge`, ME Requester
  `1.21.1-1.4.3`, Expanded AE `2.1.1`, MEGA Cells `4.11.0`, Advanced AE
  `1.6.12-1.21.1` with Athena `4.0.6`, and ExtendedAE
  `1.21-2.2.35-neoforge`. The eight independently fail-closed routes are
  `appflux`, `merequester`, `expandedae`, `megacells`,
  `advanced-ae-quantum`, `advanced-ae-athena`, `extendedae-matrix`, and
  `extendedae-planes`.
- Add exact static world projections for AppliedFlux Drive cells and its Flux
  Accessor part; the ME Requester block and requester-terminal part; Expanded
  AE formed crafting, I/O Port and two face parts; MEGA Cells formed crafting,
  three generic parts, Cell Dock and exact Drive-cell catalog; Advanced AE's
  eight quantum-computer blocks and Athena-backed quantum-alloy connected
  texture; and ExtendedAE's six Assembler Matrix blocks and two cable-bus
  planes. Retain the current exact Extended Drive behavior for ExtendedAE
  `2.2.35`.
- Keep M4/M5 output deliberately static: live power, channels, activity,
  displayed stacks, jobs, machine or cell contents, fluids, animations and
  accurate LEDs remain non-goals. Preserve exact formed/topology/orientation
  state where it is durable, normalize transient visual state off, and use
  atomic original-resource fallback for malformed, missing or unsupported
  observations.
- Add exact whole-artifact and resource verification through
  `verifyM45PinnedArtifacts`. It locks AE2 plus the seven M4/M5 input JARs,
  eleven resource manifests with 375 rows, and the 67-row MEGA Cells cell
  catalog. The Java runtime audit is clear.
- Reproduce the final M4/M5 build artifacts in two byte-identical builds. Each ran
  562 Java tests (560 passed and two opt-in exporter tests were intentionally
  skipped) and 180 pre-oracle Python tests. After freezing the runtime oracles,
  the exact CPython 3.13.14 suite passed all 192 tests in 945.343 seconds, and
  the generator/checksum closure passed.
- Finalize the 1,207,683-byte production JAR at SHA-256
  `6fed7a625b02229213a047788995944f14e7e7fcabe0e0ddc6d9b5e994146e9f`,
  the 532,979-byte sources JAR at
  `4a45c60f8512630c6bd9735e26018d019ebe99d58f2c87fa2f3c46e101b624d8`,
  the 1,637-byte POM at
  `967132ef80201099cfb1a798f03ff1ac37e0ac84a551694d7276ac20c7ccc136`,
  and the 2,861-byte module metadata at
  `3f0ba24c34ef535c99cbd6dabcd7d6bb0f784ca6ffd032f06faaf9a9b5d7b0b8`.
- Finalize the 94,537-byte schema-11 gallery at SHA-256
  `c67b4f794092f6e994349a8ee9320c052e2efc87f04e8813faf158c3455fe33b`,
  its 6,017,554-byte cases file at
  `914dab6931077521959cf59260a1ffb0cdbe105385f43880763b289f8117ec55`,
  and its exact 221,769-byte main and 2,336-byte legacy oracles at
  `c2ce69bed949306551ca4ff6cdebf7fac88f0f2f2fa7ab294d3312f363e1b448`
  and `2319ecf576ba07b123078c720d941990fac939033d375e5853f51bf98348c3c7`.
- Complete the isolated M4/M5 technical lifecycle. Initial and restored enabled
  cold/warm output is byte-identical, the combined-disabled, crafting-disabled,
  and native-structural-disabled cold/warm modes are independently stable, and
  full-JVM, restart, initializer, worker, one-build, and settle gates pass. The
  deterministic 20,821,895-byte enabled map archive has SHA-256
  `44422aa71c2f450951d8433e25e01de7a0b00dbd0d9c4fa4ff74ca98e649a2df`.
  Candidate and hash-exact accepted aliases now coexist on `data-atm120`.
  Owner visual acceptance and the separate publication decision were recorded
  on 2026-08-12; the non-SNAPSHOT release was published later that day.
- Retarget the local S1 checkpoint to All the Mons `1.2.0` at pack commit
  `c7bb230f21d14d26859d0b92548f089b3a493ad9`, NeoForge `21.1.248`, the
  canonical 6,467,235-byte BlueMap
  `5.22-agent.backport-5.22-mc1.21.1-2` NeoForge JAR
  `749f7647fa29764cea113114a7ab3259271bab3da22720989f2bd9fd1f3ba150`,
  Advanced AE `1.6.12-1.21.1`, ExtendedAE `1.21-2.2.35-neoforge` and
  Glassential `3.4.5`. Preserve the human-accepted M0-M3f identities as
  historical All the Mons `1.1.1` / NeoForge `21.1.234` evidence; this
  retarget does not transfer their runtime or visual acceptance.
- Implement the independently fail-closed S1
  `ae2-cable-bus-structural` route for all 29 ordered native face parts,
  annihilation/formation planes, six P2P types and frequency pixels, legal
  dense layouts, exact bounded facade states/materials and 30 native
  attached-device connection identities. Keep the representation static
  off/inactive/unlocked and retain atomic whole-cable-bus fallback for unknown
  compatible extension endpoints, malformed input and unsupported layouts.
- Add the schema-10 28-case/360-anchor append while retaining the exact
  accepted 3,314,082-byte schema-9 `cases.json`
  `75e6ba2f40631a95f20cfa00d7ca952e521bc2c7a4eb155926334a223a945f3a`
  and 49,679-byte gallery ZIP
  `21ceec072cc3263a41bdb81874e897d48d5a1ce5e1c7d3ac3c0de3063818ee6c`
  immutably. The appended slice has 351 custom anchors, 37,518 triangles, 96
  resources, 2,093 material rows and nine zero-triangle fallbacks. Ten
  retained schema-9 positions gain 840 triangles across 21 resources and 70
  material rows, yielding a combined 370-position/361-custom-anchor,
  38,358-triangle, 96-resource, 2,163-material-row structural closure.
- Freeze the 198,162-byte appended S1 schema-2 oracle at SHA-256
  `ac9a54cee9a20be18e71d6c9fe4f16b894827d43bb49cb4d0e56c673280cec39`
  and the 6,155-byte legacy-upgrade oracle at SHA-256
  `cf0d86c440d1f89fc13f2b131f4f1534fb42363ebdc92580af826058297eb3d0`.
  Enabled schema 10 now has 150 cases/957 anchors, 940 custom anchors, 64,938
  custom triangles, 289 selected resources, 16 zero-triangle fallbacks and
  64,948 selected triangles including the stone control.
- Lock native-structural-disabled expectations at 589 custom anchors, 27,188
  custom triangles, 218 resources, 17 fallbacks and 27,198 selected triangles.
  Its appended predecessor projection renders ten anchors/608 triangles/14
  resources, leaves 350 appended anchors empty and leaves all ten legacy
  upgrades empty. Physical stock selects 1,882 triangles across five
  resources and leaves 918 anchors empty, including every appended and legacy-
  upgrade position. The exact 2026-08-11 cold/warm isolated captures observed
  each contract byte-for-byte.
- Make the S1 non-lighting invariant exclude only world-derived blocklight and
  sunlight while retaining both in full observed attribute signatures. Require
  a flat in-range light pair on every triangle and exact fullbright output for
  all four smart-channel resources.
- Reproduce two clean builds with exact Eclipse Temurin `21.0.12+8` and Python
  `3.13.14`; each ran 448 Java tests (446 passed and two opt-in exporter tests
  were intentionally skipped) and 167 passing Python tests, with all gates
  passing. Bind the
  resulting local artifact set to the 855,833-byte production JAR
  `5dad1cf654c13b5b0aa5411264104ff2f17b942b7d4c5def698d24c476951c39`,
  388,206-byte sources JAR
  `9e294bcd04614132fd15270650d1f9e369a9491e2d13dd00d82f8e8060c2dcf2`,
  1,637-byte POM
  `967132ef80201099cfb1a798f03ff1ac37e0ac84a551694d7276ac20c7ccc136`,
  2,859-byte Gradle module metadata
  `8753a96f4d79e924058a17d8fa92c26e13ff7cf89ca2c1156c6f75f95d038c02`,
  70,925-byte gallery ZIP
  `66253309fd2cbe6b48c4ff621b71efa573b90a8d14b199205397df4d85d305e5`
  and 4,207,895-byte `cases.json`
  `389a9b2b82dd16e3f4af82f9836e593770e404995a153218937908528c17dcee`.
  The S1 restart/route-isolation/physical-stock/restoration lifecycle then
  passed on 2026-08-11. Initial and restored enabled cold/warm captures share
  the exact 5,657,463-byte report SHA-256
  `14aa3b46386bead1f656f9796305c0000e835e5948ae06367d947a3afe837723`
  and 46-file manifest SHA-256
  `e1e592faabd263e1b9bacce14d56577f330d1b5cbd80336f2bd1563d3f1b2a78`.
  Native-structural-disabled cold/warm share report/manifest SHA-256
  `63e528d6aa3c033cd6b2251f7a569cc0e4e7dc4bfba81d75862f8fa7a416e274`
  / `7cb0b48aa938109d8c001d32f01375aba7f29f7f9b8c5f96a927b52335a7df03`;
  physical-stock cold/warm share
  `a1e148fde5af118def7e379c44a2294ca5d9485a60824ad2764911f1805788a4`
  / `9539d160733a7616ec9092e82086539b4db725e0afab2b04f5111a77e0da66f0`.
  The fixture remained at one M3f build, one S1 build, two stable checks and
  zero failures; every pod had zero restarts, all init containers exited zero,
  the restored route set activated, and both render workers advanced by 650
  and 747 CPU ticks. Two exact deterministic 46-member map archives reproduced
  at 20,660,117 bytes with SHA-256
  `0f57f33a205124c67069263cce0af8d74fa04343397317c4e491275df41558cb`.
  The owner visually accepted that exact JAR, gallery and map archive in
  BlueMap on 2026-08-11. Hash-exact candidate aliases
  `ae2-addon-s1-candidate.jar`, `ae2-s1-gallery-candidate.zip` and
  `ae2-s1-enabled-candidate-map-2026-08-11.tar.gz` remain alongside accepted
  aliases `ae2-addon-s1-accepted.jar`, `ae2-s1-gallery-accepted.zip` and
  `ae2-s1-enabled-accepted-map-2026-08-11.tar.gz`; both sets were verified on
  `data-atm120` without touching the historical PVC. The final review pod was
  Ready with zero restarts and zero initializer failures, all exact routes
  active, verifier counters `1/1/2/0`, and a completed `save-all flush` before
  scale-to-zero. At that historical checkpoint S1 was the latest exact human-
  accepted local result, remained unreleased, and did not by itself authorize
  publication.
- Complete the independently fail-closed M3f `ae2-m3-completion` route for
  persisted paint splotches, both closed Sky Stone chest variants, neutral
  crank and structural Inscriber poses, and locally inferred static/offline
  spatial-pylon topology. Preserve host AO for the exact crank/Inscriber JSON
  geometry and use AO one only for the manual primitives. Exact-source review
  corrected invalid-component behavior: an uncached native-axis scan capped at
  256 renders every fully observed L/T member unformed with BASE plus DIM;
  missing, malformed or capped observation falls back atomically without
  disabling M0-M3e.
- Add the schema-9 seven-case/78-anchor M3f gallery and analyzer contract while
  embedding the accepted schema-8 M0-M3e view byte-exactly. Across 122 cases
  and 597 anchors, 579 custom anchors own 26,580 triangles and select 218
  resources; the stone control brings selected output to 26,590 triangles and
  17 atomic fallbacks own zero triangles. The M3f slice contributes
  78 custom anchors, 2,822 triangles and 15 emitted static resources; its 17
  pylon anchors own 408 triangles.
- Pin the exact 9,405-byte M3-completion profile at SHA-256
  `281a335d3024ebbb97c6268e768826c467d6f7ea660989fd3dae204c6c03abf3`
  and its 3,738-byte, 33-resource manifest at SHA-256
  `3faf7f29e2878f5525541bad855cbc66b6d45786dc8fc6ee29a6fbbf4878cca1`.
- Pass all 365 Java tests and 116 Python tests, both Java checkstyle gates,
  generated-profile/gallery checks, exact AE2 runtime/source verification and
  deterministic gallery packaging. Bind the final candidate artifacts to:
  623,591-byte production JAR
  `ca67c0fc433e43f8e0801ed8d2cccfe47aae317fbc329c099bc8cd741ec3b42b`,
  276,986-byte sources JAR
  `2a3bb3713ff56731992d405a58fc6a137dcfc8fff43467de7196ad33c444795c`,
  2,859-byte module metadata
  `106df734036c9f72f0463a03e2a282d430653edab5e761b165b51fe99f937d7a`,
  49,679-byte gallery ZIP
  `21ceec072cc3263a41bdb81874e897d48d5a1ce5e1c7d3ac3c0de3063818ee6c`
  and 3,314,082-byte `cases.json`
  `75e6ba2f40631a95f20cfa00d7ca952e521bc2c7a4eb155926334a223a945f3a`.
  The 1,637-byte POM remains
  `967132ef80201099cfb1a798f03ff1ac37e0ac84a551694d7276ac20c7ccc136`.
- Bind the corrected 9,684-byte packaged support matrix to
  `71ec7977c7990678b9f34b27b976a0a1381b85a292afd409556f8190cee88863`
  and the 84,990-byte packaged provenance manifest to
  `dd3bda10236288c5a8e745978e7f55467961bf3aab1a4cb8ab527e8b1eec6e50`.
- Withdraw the prior M3f candidate after owner visual review exposed invisible
  L/T pylon members. It was never accepted; the corrected candidate supersedes
  it and is the only M3f artifact later accepted below.
- Complete the M3f isolated technical lifecycle on 2026-08-09 with exactly one
  M3f fixture build, zero verifier failures and two stable checks across
  persistence, full JVM restarts, enabled, independently M3-completion-disabled,
  physically add-on-absent stock and restored-enabled phases. Enabled cold,
  warm and restored reports are byte-identical at 3,783,797 bytes and SHA-256
  `7022a33448dab364cb825a8d67359795560b6a8793b64544ccb0b4c1fda7484e`;
  their 44-file manifests are byte-identical at SHA-256
  `259af4eea91a32acc07d1572e8f3f42e6276b46999496d0380ec009c10970fd8`.
- Validate byte-identical M3-completion-disabled cold/warm reports at
  3,646,167 bytes and SHA-256
  `c9391aacb0fa7e268b67bc4723ffa8958593acbf06e529542c63fc7fa59ba707`
  with manifest SHA-256
  `34bad6b250e18217485d68dfb989cfbc6d9b53f480be71c534f91675dc3c6a3e`,
  and physical-stock cold/warm reports at 614,214 bytes and SHA-256
  `30439e78593dacc43a0d3822f039ebbbd058d83be64023650471dab07179c4bc`
  with manifest SHA-256
  `b66f1e00f658d8fbd6ed842aae614b5db7bc94580215b2ad22a2a769e4d73efa`.
  Both stock-style modes render 38 M3f anchors and leave 40 M3f anchors empty.
- Reproduce the exact 20,450,880-byte, 44-entry M3f enabled candidate map
  archive twice with SHA-256
  `e66abf203481c5df0fa0fc0062c414876f9ef6428cd637de0795f821496c51a9`.
  Retain the exact candidate aliases `ae2-addon-m3f-candidate.jar`,
  `ae2-m3f-gallery-candidate.zip` and
  `ae2-m3f-enabled-candidate-map-2026-08-09.tar.gz` and leave every M3e
  candidate/accepted alias unchanged.
- Record owner visual acceptance on 2026-08-09 of the exact corrected
  623,591-byte M3f production JAR
  `ca67c0fc433e43f8e0801ed8d2cccfe47aae317fbc329c099bc8cd741ec3b42b`,
  exact 49,679-byte schema-9 gallery
  `21ceec072cc3263a41bdb81874e897d48d5a1ce5e1c7d3ac3c0de3063818ee6c`
  and exact 20,450,880-byte map archive
  `e66abf203481c5df0fa0fc0062c414876f9ef6428cd637de0795f821496c51a9`.
  Bind the hash-exact accepted aliases `ae2-addon-m3f-accepted.jar`,
  `ae2-m3f-gallery-accepted.zip` and
  `ae2-m3f-enabled-accepted-map-2026-08-09.tar.gz` while retaining the
  candidate aliases. This bounded decision completes M3, makes M3f the latest
  accepted slice and keeps M3e as the previous accepted rollback checkpoint.
- Schedule the complete cable-bus structural remainder as one post-M3
  implementation step: remaining native face-part chassis, planes and P2P
  parts, dense part layouts, every valid facade layout and cable-attached-
  device structural connections. Keep contents, held items, fluids,
  live/activity state and accurate Drive LEDs as accepted non-goals; retain the
  existing M4/M5 extension order afterward.
- Record owner visual acceptance on 2026-08-08 of the exact M3e production
  JAR, schema-8 gallery and enabled map archive. Retain the candidate aliases
  and bind the hash-exact accepted aliases
  `ae2-addon-m3e-accepted.jar`, `ae2-m3e-gallery-accepted.zip` and
  `ae2-m3e-enabled-accepted-map-2026-08-08.tar.gz` without rebuilding the
  accepted JAR.
- Select M3f as the one combined remaining-M3 candidate under profile ID
  `ae2-m3-completion`: paint, both Sky Stone chests, neutral crank, neutral
  structural Inscriber and locally inferred static/offline spatial pylons.
  Record machine contents, held items, fluids, live/activity-specific state
  and accurate Drive LEDs as accepted non-goals rather than deferred work.
  Schedule one post-M3 cable-bus structural-completeness step before the
  unchanged M4/M5 extension work.
- Implement the independently fail-closed M3e `ae2-quantum-bridge` renderer,
  synthetic resource route, exact profile and verifier lane for
  `ae2:quantum_link` and `ae2:quantum_ring`. Bind the three valid isolated
  3x3x1 planes, exact center/corner/edge roles, saved
  `formed`/`waterlogged` state, atomic topology fallback and the
  static-off-unknown policy for unavailable client-stream constructed,
  corner and powered bits. Preserve the exact bounds-mapped UVs, outward
  winding, cutout materials, enabled neighbor-derived ambient occlusion and
  host world-light semantics. Exclude powered overlays, particles and
  extension connector compatibility claims.
- Add the schema-8 quantum synthetic gallery/analyzer contract and the
  independently disabled quantum-route mode while freezing the accepted
  schema-7 projection. The exact schema-8 gallery has 115 cases and 519
  anchors: 501 custom anchors, 23,758 custom triangles, 203 selected resources
  and 17 zero-triangle fallbacks. Its three M3e cases contain 27 custom
  anchors and 1,188 triangles across complete XZ, XY and YZ formed bridges.
- Pin the exact 13-resource, 3,798-byte closure at SHA-256
  `717eed1ada75fb43c1324792c147cd8c2308d8c73ee82bf52d8de6bad4f74ed9`
  and generated profile at SHA-256
  `21afa152e3f56d8bdde9f602748c0efbca52a2c55d5dd7a836adca267c65480e`.
  Keep eleven route-only paths and two cable-texture paths shared with the
  frozen main profile. Audit six source texture identifiers while emitting
  only the four non-powered identifiers.
- Require both the exact 8,230,896-byte AE2 runtime JAR and exact
  3,814,167-byte official sources JAR for the M3e pinned-artifact verifier;
  lock 12 runtime classes, ten source files, all prior accepted generated
  outputs, and no-third-party production/sources-JAR boundaries.
- Reproduce the exact 513,674-byte M3e candidate JAR with SHA-256
  `98ff55eaba609fc894b01e0c4d922b47f1871c324945f88f7a34864cf48b124f`,
  234,963-byte sources JAR with SHA-256
  `2bc749373eeb29bd30b9edb58006c7248da1cc09a6abdc7abb404b86a4045a1e`,
  1,637-byte POM with SHA-256
  `967132ef80201099cfb1a798f03ff1ac37e0ac84a551694d7276ac20c7ccc136`
  and 2,859-byte module metadata with SHA-256
  `d1da10c42393c8a9cb79b77ad67a0b3d15140ef58fef593b20f25710fc8b0e02`
  across two byte-identical clean builds. Pass all 316 Java tests in 53 suites
  and all 98 Python tests.
- Reproduce the exact 45,009-byte schema-8 gallery ZIP with SHA-256
  `498bac2f82b78451eb24da416ded1d625e5785cc5d3e5910b4c34bfecc05c390`
  and 3,123,572-byte `cases.json` with SHA-256
  `93963dd0bb60a276e1a17c6dd1f4eb916cd92bef4ef30a2e8bdc7a2bfa818b3e`.
  Preserve its frozen schema-7 projection at SHA-256
  `c60d2afff5a1f92da4972963fcb926c38093f43bb6d7f550799f104349728a38`.
- Complete the one-build M3e technical lifecycle on 2026-08-08 with zero
  verifier failures and two stable checks across delayed verification, actual
  unload/reload, full JVM restarts, enabled, extension-disabled,
  glass-disabled, crafting-disabled, quantum-disabled, physically add-on-
  absent stock and restored-enabled modes. Every cold/warm report and exact
  44-file manifest pair is byte-identical; restored enabled reproduces initial
  enabled exactly. All capture pods have zero restarts, and both render
  threads advance by 587 and 245 scheduler jiffies.
- Bind the observed report SHA-256 values to enabled/restored
  `d306aff2a0f2eca2882a3d52426140b8caf98aeda76d0722dd14b59ea8a5e9e9`,
  extension-disabled
  `d871f95fd67e9e805829c37260e3dc43558cc4e388332d9771d388857a088f28`,
  glass-disabled
  `9a5c1103b55d6637d2b954c681dc15f333d944d2f59d983da2d1bf2645ad405c`,
  crafting-disabled
  `3b103035582b4db56dd81e08a3e20b7c80f839246f7d6a380a542b26994e5205`,
  quantum-disabled
  `13c4ca3676868418f9f6797e5b4b4816a38d7ed2c518e1cbb329063ff005e20b`
  and stock
  `0c3e4333782c244779a55201ab7a6362942e763c858bfdca2348b48204ce5e40`.
- Reproduce the exact 20,424,799-byte, 44-entry enabled candidate map archive
  twice with SHA-256
  `9e145fffbe87205651ed7cc6b4cb706b7dcbe394ac26e7ce2eb1d6d55ea411a7`.
  At the technical checkpoint, retain only candidate PVC aliases
  `ae2-addon-m3e-candidate.jar`,
  `ae2-m3e-gallery-candidate.zip` and
  `ae2-m3e-enabled-candidate-map-2026-08-08.tar.gz`; create no accepted alias
  before human review. At that checkpoint M3d remained the latest accepted
  slice and every publication/remote/tag/Maven/root-integration gate remained
  blocked. The later M3e acceptance and accepted aliases are recorded above.

- Complete the bounded M3b `extendedae:ex_drive` implementation for exact
  ExtendedAE `1.21-2.2.33-neoforge` while preserving the accepted M3a native
  Drive route.
- Record owner visual acceptance on 2026-08-07 for the exact 323,416-byte M3b
  SNAPSHOT JAR with SHA-256
  `f02123cb602bb7b6466d1529c5518e45862f53f413ce9a75ecc067d1a30607d1`,
  exact 37,614-byte gallery ZIP with SHA-256
  `69bdb99d9c8f6838c3b8d5847c32702761cfa77b263ee95384ca24357c84cf92`
  and exact 20,351,418-byte accepted map archive with SHA-256
  `c73844990847148d9cd3d315832085e49776e9253c5c8eca6f0b7659d73c4285`.
  This completes M3b only.
- Add the disjoint exact-AE2-19.2.17 M3c renderer/profile/resource/verifier
  lane for
  `ae2:quartz_glass` and `ae2:quartz_vibrant_glass`: 19 operator-installed
  textures and 22 resources, manifest SHA-256
  `b51c708e7c4d26093c1b6f85b88d0be50572d3cfa76dbf802720f6ad79c7a7fa`
  and generated-profile SHA-256
  `548e5bc00ef07c6d6b93b346422b596882ec11ca03de006065fa45fecb991200`.
  Preserve the accepted M3a 196-resource main profile and manifest
  byte-identically. Implement exact six-neighbor topology, client-compatible
  position-seeded base selection and asymmetric UVs, all 15 frame textures,
  CUTOUT/no-AO attributes, vibrant emission 15, BlueMap-native
  center/outward light/cave/culling/map-color behavior and an independent
  fail-closed route. Unsupported cross-mod `getAppearance` proxies are treated
  as disconnected; only missing or malformed native-neighbor data uses atomic
  original-resource fallback.
- Add the schema-6 representative M3c gallery/analyzer slice: 11 cases, 47
  anchors, 776 triangles and all 19 selected glass resources, while retaining
  the accepted schema-5 M3b contract. Add mutually exclusive
  `--glass-disabled`, extension-disabled and stock comparisons. Java tests
  remain authoritative for all 64 connection masks; the gallery does not claim
  to visualize every mask.
- Record the BlueMap 5.22 host limitation that PRBM cannot encode AE2's client
  `BakedQuad shade=false` flag. The add-on therefore makes no pixel-identical
  directional-lighting claim; client particles/items are out of scope.
- Reproduce the exact 375,558-byte M3c production JAR with SHA-256
  `4c1b557ae4c79c738005b74e2f0c89ca4fbe503dd6ef0ba614fae34d8e449d47`,
  186,080-byte sources JAR with SHA-256
  `6b011719264229629fb1011f4c6de566bf16b13603a6c2412b4ed4ced70a4036`,
  1,637-byte POM with SHA-256
  `967132ef80201099cfb1a798f03ff1ac37e0ac84a551694d7276ac20c7ccc136`
  and 2,859-byte module metadata with SHA-256
  `5922575b3002d2cc48e1e5e2fa6a795b8b7f11349b1bb0d04f17db7c0b182876`.
  Pass all 247 Java and 71 Python tests and reproduce the 38,929-byte gallery
  ZIP with SHA-256
  `0839009fe6a4f4785f864f33bc97fef28b8418f077d5d66a20efc3e8eeb4edab`.
- Validate byte-identical enabled cold/warm/restored schema-6 reports at
  1,582,336 bytes and SHA-256
  `e81b8e6eed2047629a933a21e0e345c4880db2b12e264455fa84ef59b63d824f`,
  plus byte-identical 44-file manifests at SHA-256
  `3257a86e895956d7701f056ef46f3188fb4fca2f704b8c7e67641164666221f3`.
- Independently validate extension-disabled cold/warm report SHA-256
  `bd540b437d67830a4899c4437eb396b7229104cc2afe89e5c0a4a7e8d5cbfe32`
  and manifest SHA-256
  `6ac6e2578374d34908cdddf2a451706ac7bc7d1d2831a66757fe6c652015614e`,
  glass-disabled report SHA-256
  `34a488beb4cc1d4ce6dfee2183e61839304b626ad9b5360f6e302687c4d2b442`
  and manifest SHA-256
  `9d7c9f196eb3e89acadf7eb7bfb1cf4ad85da6d8a4403f7ea4d4e9810ad7250b`,
  and physically add-on-absent stock report SHA-256
  `08436ad0d03e37a3578552935efc1a6dd5743f5ed8de75d923d2fdf518ff2b0d`
  and manifest SHA-256
  `738d29ddceaa000c867d0616fa9a389010a19672404e76b9f9644368cd4d15fa`.
  Restore the exact candidate and reproduce the enabled report and manifest
  without rebuilding.
- Retain one fixture build, zero verifier failures and all six critical NBT
  probes through delayed verification, actual unload/reload, full JVM restarts,
  all route modes and restoration. Reproduce the exact 20,376,253-byte enabled
  map archive twice with SHA-256
  `3fb5fb174f23c0f2d8ce9f98e8c12feb8b12c444060e35b9d8e0036d8ec165e5`.
  Every accepted capture pod had zero restarts and both render threads
  accumulated CPU time. Separately record one operator Ctrl-C while detaching
  the console during pre-lifecycle setup; it caused a restart attempt, preceded
  the accepted captures and was not an add-on restart.
  The M3c technical lifecycle passed on 2026-08-08.
- Record owner visual acceptance on 2026-08-08 for the exact 375,558-byte M3c
  production JAR with SHA-256
  `4c1b557ae4c79c738005b74e2f0c89ca4fbe503dd6ef0ba614fae34d8e449d47`,
  exact 38,929-byte gallery ZIP with SHA-256
  `0839009fe6a4f4785f864f33bc97fef28b8418f077d5d66a20efc3e8eeb4edab`
  and exact 20,376,253-byte map archive with SHA-256
  `3fb5fb174f23c0f2d8ce9f98e8c12feb8b12c444060e35b9d8e0036d8ec165e5`.
  This bounded decision completes M3c.
- Retain the candidate PVC aliases and add hash-exact accepted aliases
  `ae2-addon-m3c-accepted.jar`, `ae2-m3c-gallery-accepted.zip` and
  `ae2-m3c-enabled-accepted-map-2026-08-08.tar.gz`.
- Add the independently fail-closed M3d `ae2-crafting` route for the eight
  native AE2 19.2.17 crafting blocks, exact formed/powered state gates,
  source-derived cube geometry, all 64 six-neighbor masks, strict Crafting
  Monitor painted-color ordinals `0..16`, powered overlays and atomic fallback
  for malformed native or known compatible unsupported extension topology.
  Exact unformed states retain their stock models, and the transient monitor
  displayed stack remains deliberately omitted.
- Add the schema-7 nine-case/86-anchor M3d gallery and frozen schema-6 M0-M3c
  regression. Across 112 cases and 492 anchors, 474 custom anchors own 22,570
  triangles and select 201 resources; 17 fallback anchors own zero triangles
  and the stone control retains ten triangles.
- Reproduce the exact 448,915-byte production JAR with SHA-256
  `ca057f025338150255ea916402c08bc8b614f9398a063e7433bbe468808c93ee`
  and 213,004-byte sources JAR with SHA-256
  `3e95cabf3e9dcf4ab5c8c2b6d6661ba6464a6cb3e6abd6d33fcfb904b5197c4f`
  across two clean builds. Pass all 285 Java and 85 Python tests. Reproduce the
  exact 44,201-byte gallery ZIP with SHA-256
  `4a18b45f2c03c8d1d3c49a731df2c2503745952faccf9ba06ec8f301909b81f3`
  and 3,030,512-byte `cases.json` with SHA-256
  `c60d2afff5a1f92da4972963fcb926c38093f43bb6d7f550799f104349728a38`.
- Complete the one-build, zero-failure, two-stable-check M3d technical
  lifecycle across initial/delayed verification, actual chunk unload/reload,
  full JVM restarts, enabled, extension-disabled, glass-disabled,
  crafting-disabled, physically add-on-absent stock and restored-enabled
  phases. Every cold/warm report and 44-file manifest pair is byte-identical;
  restored enabled cold and warm reproduce the initial enabled evidence.
- Reproduce the exact 20,417,822-byte enabled candidate map archive with
  SHA-256
  `672cdffaf5135f34c4b10c24638056540dcaadbb5fd2d78b3096897436d8a2c6`.
  Both render threads advanced by 313 and 520 scheduler jiffies; capture pods
  had zero restarts. One operator sequencing attempt failed during init before
  Minecraft started and was not an add-on or capture restart.
- Retain exact candidate PVC aliases `ae2-addon-m3d-candidate.jar`,
  `ae2-m3d-gallery-candidate.zip` and
  `ae2-m3d-enabled-candidate-map-2026-08-08.tar.gz`.
- Record owner visual acceptance on 2026-08-08 for the exact 448,915-byte M3d
  production JAR with SHA-256
  `ca057f025338150255ea916402c08bc8b614f9398a063e7433bbe468808c93ee`,
  exact 44,201-byte schema-7 gallery ZIP with SHA-256
  `4a18b45f2c03c8d1d3c49a731df2c2503745952faccf9ba06ec8f301909b81f3`
  and exact 20,417,822-byte map archive with SHA-256
  `672cdffaf5135f34c4b10c24638056540dcaadbb5fd2d78b3096897436d8a2c6`.
  This bounded decision completes M3d.
- Add hash-exact accepted aliases `ae2-addon-m3d-accepted.jar`,
  `ae2-m3d-gallery-accepted.zip` and
  `ae2-m3d-enabled-accepted-map-2026-08-08.tar.gz`, while retaining the
  candidate aliases. At that checkpoint M3d was the latest human-accepted
  slice. M3 remains
  incomplete, and publication, remote creation, tags, Maven and root-submodule
  integration remain blocked through complete implemented and human-accepted
  M0-M5 plus a separate owner decision.
- Add an independently fail-closed ExtendedAE route, a bounded twenty-slot
  projection, all 24 `facing`/`spin` states, exact front slots `0..9`, rear
  slots `10..19` using opposite-facing/same-spin orientation, and the closed
  23-AE2-plus-three-ExtendedAE item catalog mapped to 15 occupied models.
- Render the operator-installed 116-triangle Extended Drive base plus six
  chassis and ten black fullbright offline-unknown LED triangles per occupied
  slot, for exact `116 + 16N` accounting. Dynamic KubeJS cells, other
  extensions, invalid counts and malformed states fall back atomically.
- Add the exact 15-resource/eight-texture ExtendedAE partition with manifest
  SHA-256
  `5e72f79f45a3b120a89cf8b7a1fa15ce41bebaae62a63c6f3305ef40bd5d24ee`,
  profile SHA-256
  `eab467f46c27974e1f7d54fe749366b92eacd8d63d57bad1e8f3e452d82ad1df`
  and dependency on the accepted AE2 Drive partition.
- Add the schema-5 92-case/359-anchor gallery. Its 342 custom anchors own
  17,488 triangles and select 167 resources; 16 atomic fallback anchors own
  zero triangles. The 1,143,610-byte `cases.json` has SHA-256
  `5a1297668b6922b03ae3f2ab089b643aa9521060e61607bbbae353a80d8494fc`.
- Reproduce the exact 323,416-byte production JAR with SHA-256
  `f02123cb602bb7b6466d1529c5518e45862f53f413ce9a75ecc067d1a30607d1`,
  166,516-byte sources JAR with SHA-256
  `8574cf425d5e76c527e02eeeda1f3805845cfe9e6cad6f6fa2f6457aa2157909`,
  1,637-byte POM with SHA-256
  `967132ef80201099cfb1a798f03ff1ac37e0ac84a551694d7276ac20c7ccc136`
  and 2,859-byte module metadata with SHA-256
  `5a739c3cd624ad3c2313bc49c77bfe66d54df6851a321f46ce6f1d94084e5386`
  across two clean builds; pass all 217 Java and 56 Python tests. Reproduce
  the 37,614-byte gallery ZIP with SHA-256
  `69bdb99d9c8f6838c3b8d5847c32702761cfa77b263ee95384ca24357c84cf92`.
- Build the cumulative fixture exactly once and retain zero verifier failures,
  `#builds = 1` and all six critical NBT probes through delayed verification,
  actual unload/reload, datapack reload and full JVM restarts without rebuild.
- Validate byte-identical enabled cold/warm/restored schema-5 reports at
  1,335,214 bytes and SHA-256
  `e5e466d9f207beba2032775b66acf091c5c3bf647b40bc811b62a714dae6dd8e`,
  plus byte-identical 44-file manifests at SHA-256
  `1b1db461fc8e76d4e988bd60c403af8601c51cf4d6fac0bfb4e748df5c4c60e9`.
- Validate the independent extension-disabled route: all 36 M3b anchors are
  empty while the accepted M3a regression remains exact. Its cold/warm report
  is 1,006,824 bytes with SHA-256
  `2947419d53cf7e2e2f8a4a3cacecc2515eb6aec2382f328e48fb596a98c9d754`
  and its 44-file manifest SHA-256 is
  `f82b43aa87d2f5b8e8e856f769bf138584b242cc53c99a73c5a6752be2de7fa1`.
- Validate the physically add-on-absent stock cold/warm pair: only the stone
  control owns ten triangles. Its 365,850-byte report has SHA-256
  `630c96be1406504267e27871e6113b0d98d23da522c9358c31f2f61efaff6f1a`
  and its 44-file manifest SHA-256 is
  `3ad852e739f4cacceee2fbc275a0ee3c6a231623a3a429fe2d0821915bb4b592`.
- Reproduce the deterministic 20,351,418-byte enabled map archive twice with
  SHA-256
  `c73844990847148d9cd3d315832085e49776e9253c5c8eca6f0b7659d73c4285`.
  Both render threads accumulated 420 and 418 scheduler jiffies. All accepted
  capture pods had zero restarts and no players; one pre-runtime init
  sequencing attempt ended before Minecraft started and caused no add-on
  restart.
- Make schema-5 component-insensitivity ignore only ordinary model blocklight
  and sunlight derived from world context while retaining geometry, material,
  UV, color and AO comparison plus exact per-anchor black/fullbright LED gates.
  The full M3b technical lifecycle passed on 2026-08-07 before the subsequent
  exact visual acceptance recorded above; at that checkpoint M3 remained
  incomplete.
- Record human visual acceptance on 2026-08-07 for the exact 259,005-byte M3a
  SNAPSHOT JAR with SHA-256
  `55a11805373aebfde821e5009723ec7d672fb290127dbc60131ffa344c99518a`
  and its bounded native-Drive gallery. M3a was the then-latest accepted slice.
- Begin active M3 with the bounded M3a native-Drive slice while preserving M2
  as the then-latest human-accepted artifact.
- Add independent fail-closed `ae2:drive` activation, bounded ten-slot Anvil
  inventory retention, all 24 `facing`/`spin` states and a closed catalog of
  23 exact AE2 19.2.17 item IDs mapped to 12 occupied chassis models.
- Render the operator-installed Drive base plus occupied chassis and black
  static offline-unknown LEDs with exact `90 + 16N` triangle accounting.
  Treat components as render-insensitive bounded payload and atomically use
  the original resource for malformed state or an unknown/extension cell.
- Expand the exact profile to 158 AE2 textures and 196 required resources,
  with profile SHA-256
  `2c27976a718834dbc97b3eb7cac6543c4ad2a966737c7bccbadb2b1c39c837e8`
  and resource-manifest SHA-256
  `408297def444f1392b7b87fdc4b8520099513b4c57c63a4176b808ce61b4e1be`.
- Add the schema-4 76-case/323-anchor gallery. Its 310 custom anchors own
  12,432 triangles and select 159 runtime materials; 12 atomic fallback
  anchors require zero triangles and the stone control remains separate.
- Reproduce the exact 259,005-byte production JAR with SHA-256
  `55a11805373aebfde821e5009723ec7d672fb290127dbc60131ffa344c99518a`
  and 136,798-byte sources JAR with SHA-256
  `40a06a77768f085d4680cb5eae435b35b6115482f231874cd4f13fac73b6edea`
  across two clean builds; pass all 173 Java and 45 Python tests. Reproduce
  the 26,633-byte gallery ZIP with SHA-256
  `91057d94890bc3bd063fb7c4bf951f9a70b62a00ee01a9b00deb0fd8f674bb2b`.
- Pass delayed, actual chunk-unload/reload and full-JVM-restart verification
  without rebuilding, with zero verifier failures and the four critical M3a
  NBT states preserved.
- Validate byte-identical enabled and physically add-on-absent stock cold/warm
  schema-4 captures. Enabled selected 12,442 triangles across 323 anchors;
  stock selected only the 10-triangle stone control. Both pairs reproduced
  all 44 comparable non-live/non-`rstate` files. Accepted capture phases had
  zero pod restarts and no unexpected/add-on restart; one operator-timing
  transition caused a separate restart attempt outside those phases.
- Restore the exact M3a JAR with the M3 namespace as the only AE2 gallery,
  reactivate both exact routes, and retain zero verifier failures and all four
  critical NBT states without calling `build`. Reproduce the initial
  939,040-byte report plus all 44
  comparable web files byte-for-byte. Reproduce the deterministic
  20,320,750-byte candidate map archive with SHA-256
  `18cd08c9f0de6bc132bbf9f5cbf1d692d475864ca9aa818eed6166d892a94dce`
  twice. This closed the M3a technical lifecycle before its subsequent human
  visual acceptance recorded above; at that checkpoint M3 remained incomplete.
- Record human visual acceptance on 2026-08-07 for the exact 161,930-byte M1
  SNAPSHOT JAR with SHA-256
  `e02beee7fdafeba9c3ef0ea42deda0a7709cc70df23d4778cfb7a72b1fdaf2e1`.
- Add the local exact-AE2-19.2.17 M2 implementation candidate: static idle/off
  `ae2:terminal` rendering for byte spins `0..3`, multiple orientations and
  parts, and one same-face canonical property-free `minecraft:stone` facade.
- Retain strict atomic whole-block stock fallback for every unsupported M2
  part, spin, dense-part topology, facade state/layout or ambiguous endpoint.
- Expand the exact input closure to 85 center IDs, one face-part ID, 148 AE2
  textures and 170 resources, and add the schema-3 62-case/290-anchor gallery
  contract. Its 278 custom anchors total 8,576 triangles and 149 runtime
  materials; 11 anchors require zero-triangle stock fallback.
- Reproduce the exact 203,599-byte M2 production JAR with SHA-256
  `fc11af62359746990a2b35470c1da66e606b13a36be33a5b854d343eebb108d2`
  across two clean builds, alongside reproducible sources, POM, Gradle-module
  and 20,510-byte gallery artifacts; pass all 120 Java and 38 Python tests.
- Pass delayed, chunk-unload/reload and full-JVM-restart fixture verification
  with zero failures and the critical supported and unsupported NBT states
  preserved.
- Validate the initial enabled and physically add-on-absent stock cold/warm
  schema-3 captures. Enabled selected 8,586 triangles across 290 anchors with
  278 custom anchors, all 149 runtime materials and zero leakage into 11
  fallback anchors; stock selected only the 10-triangle stone control while
  289 anchors remained empty. Each pair had byte-identical reports and 44
  relevant web files.
- Restore the exact M2 JAR and `ae2_staging` configuration, reactivate the
  profile, retain zero verifier failures and all four critical NBT states
  without rebuilding, and reproduce the initial enabled report plus all 44
  relevant web files byte-for-byte. This passes the M2 technical/lifecycle
  candidate.
- Record human visual acceptance on 2026-08-07 for the exact 203,599-byte M2
  SNAPSHOT JAR with SHA-256
  `fc11af62359746990a2b35470c1da66e606b13a36be33a5b854d343eebb108d2`
  and its bounded 62-case/290-anchor behavior. This completes M2 and unblocks
  M3 planning/implementation only. M3 had not started at that checkpoint;
  publication remains blocked through implemented and human-accepted M5.
- Record the final exact M1 technical/lifecycle candidate: 161,930-byte
  production JAR with SHA-256
  `e02beee7fdafeba9c3ef0ea42deda0a7709cc70df23d4778cfb7a72b1fdaf2e1`.
- Pass the 48-case/269-anchor M1 lifecycle, including 266 custom anchors,
  7,576 custom triangles, all 140 resources, deterministic cold/warm/stock/
  restored output and the two-render-thread smoke.
- Pass the opt-in 1,024-cell dense fixture with 63,488 triangles, then clear
  it and reproduce the compact M1 result.
- Record human visual acceptance on 2026-08-04 for the exact bounded M0
  runtime JAR
  `84a3b972d86a49a723e56a820ff3b59039654e2108d8ba965493d2302a5b1e41`.
- Begin the exact AE2 19.2.17 M1 local profile for 85 cable IDs, 140 runtime
  textures and a 158-resource verification closure.
- Add family/color compatibility, mixed-family connection sizing,
  family-specific geometry and zero-channel smart-cable overlay design while
  retaining whole-block fallback for parts, facades and ambiguous endpoints.
- Block publication, remote creation, tags and releases until every agreed AE2
  milestone M0-M5 is implemented and human accepted.
- Establish the exact AE2 19.2.17 and BlueMap 5.22 M0 profile.
- Add bounded `ae2:fluix_glass_cable` decoding, topology and deterministic
  idle/off geometry with original-resource fallback.
- Bound nested NBT disposal by depth, tag count, list/array declarations,
  cumulative array bytes and retained-string size without materializing
  generic nested containers.
- Contain runtime and linkage failures from the original-resource fallback so
  an unsupported block cannot abort the BlueMap render task.
- Add exact artifact, build, publication and isolated runtime gates.
- Add a fail-closed two-pass release-promotion contract for exact JAR, sources,
  POM and Gradle-module outputs, Maven sidecars/metadata and draft-Release
  assets.
- Add a dependency-free exact BlueMap 5.22 PRBM v1 analyzer with a 17-anchor
  deterministic fixture, strict gzip/material validation and semantic
  geometry/attribute signatures.
