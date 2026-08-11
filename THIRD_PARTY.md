# Third-party software

The production JAR contains the project's LGPL-covered adaptation of the
source behavior recorded below, but no third-party classes, binaries,
textures, models, source files, or JARs.

| Project | Use | Version/evidence | License | Bundled |
| --- | --- | --- | --- | --- |
| BlueMap | Compile/runtime host through internal interfaces | upstream `5.22`; current backport `5.22-agent.backport-5.22-mc1.21.1-2`, commit `9be321df995a1103808621d529eb72773e719d4d`, 6,467,235-byte NeoForge JAR SHA-256 `749f7647fa29764cea113114a7ab3259271bab3da22720989f2bd9fd1f3ba150` | MIT | No |
| BlueNBT | Runtime NBT deserialization supplied by BlueMap | `3.5.1` | MIT | No |
| Applied Energistics 2 | Installed resources, persisted cable/facade/Drive/crafting/quantum/paint formats, adapted cable/native-Drive behavior, exact M2 facade evidence, exact M3c connected-quartz-glass evidence, exact M3d formed-crafting evidence, exact M3e quantum evidence, exact M3f paint/Sky Stone chest/crank/inscriber/spatial-pylon evidence, and exact S1 native face-part/facade/attached-device structural evidence | `19.2.17`, runtime SHA-256 `460d779a0609b81409907d9956de8f6f70a1b0912257e3e5c3c7e75ac9630e95`, sources SHA-256 `d2f451203cb61c2d21fae52c683083d2f72441ca7d26725f4df5934290492e6a`; tag commit `79ee2c704ad62941a426c26b1cb1f76ef5b2ee5a` | implementation LGPL-3.0-or-later; API MIT; assets CC BY-NC-SA 3.0; text CC0 | No |
| AppliedFlux | Exact M4 Flux Accessor part plus 20-item/ten-model Drive integration evidence and installed resources | `1.21-2.1.5-neoforge`, 345,117 bytes, SHA-256 `57e6a2c0f38e660c9e8416f9081d8c515f5ad096d6793d7b7f039e8e210d245b` | runtime metadata declares LGPL-3.0 family | No |
| ME Requester | Exact M4 requester block transform and offline requester-terminal evidence and installed resources | `1.21.1-1.4.3`, 184,517 bytes, SHA-256 `68f3c861a802d48afeb6e3a48e8ee4f8633904340ac3f89f17493dc84490e385` | runtime metadata declares LGPL-3.0 family | No |
| ExtendedAE | Installed Ex Drive resources and exact current Drive behavior; M5 six-block Assembler Matrix and two-plane evidence/resources | Current: `1.21-2.2.35-neoforge`, 5,578,031 bytes, SHA-256 `14a2860fa2c747e9dda2279b8933fac6311fecfee166c765171022b902591c65`, tag commit `3776bc854458301bbcc9a44a8238d70a0e3dc00d`; historical accepted M3b/M3d: `1.21-2.2.33-neoforge`, SHA-256 `6652ed1ea4b71f585d48c05a195a77594a7a2bd1ecea0fc805db2122aafad734`, tag commit `90005ee29839fb9fa83bbe6544919c722f8b0dc6` | upstream declares LGPL-3.0 and includes the LGPLv3 text | No |
| MEGA Cells | Exact M5 eight-block formed-crafting, three generic parts, Cell Dock, and 67-item/37-model cell evidence/resources | `4.11.0`, 1,137,276 bytes, SHA-256 `a386bbf12afb11729b0dcf77f64221893d250f22e6185a4d728b9799b230bc55` | runtime metadata declares LGPLv3.0 | No |
| Expanded AE | Exact M4 I/O Port, two face parts, and 21 formed-crafting-block evidence/resources | `2.1.1`, 496,713 bytes, SHA-256 `f39c0eb9c6271f54a44ffee092a29520f53000d1005849e6afada3ad9dffba14` | runtime metadata declares GNU LGPL v3.0 | No |
| Advanced AE | Exact M5 eight-role quantum-computer and quantum-alloy connected-texture evidence/resources | Current: `1.6.12-1.21.1`, 4,791,255 bytes, SHA-256 `a01d9718667ac13899013e91c5b0b7708b9b9db1da9b8e380772dde54bbe8f41`; historical M3d/S1 audit used `1.6.11-1.21.1` | runtime metadata declares LGPL-3.0 | No |
| Athena | Exact M5 connected-texture selection and animation-metadata evidence for Advanced AE quantum alloy | `4.0.6`, 99,944 bytes, SHA-256 `43699885bbce3343916d4c5c4940cf0e3f9f6f02fdeb46e8655e121b42282ec5`; author metadata: ThatGravyBoat | MIT | No |
| Glassential Renewed | Exact All the Mons 1.2.0 S1 effective facade-material resource evidence | `3.4.5`, SHA-256 `1f0c8f7533bf3b2002575219ba795fd32a44cc5085c2710624ebbf69e6121471` | runtime metadata declares MIT | No |
| CodeChickenLib quad transforms vendored in AE2 | Exact facade clamp, reinterpolation, face-strip, and corner-kick design evidence; no source or classes copied | Exact Git blobs and runtime class digests inside AE2 `19.2.17`, recorded in `provenance/upstreams.json` | LGPL-2.1-or-later | No |
| Minecraft client resources | Exact 1.21.1 stone blockstate/model/texture evidence only | 26,836,906-byte client JAR; SHA-256 `499f6897d1837516680f3114072d8106e11c9adcd933fe5cf051b551089b0c99` | Mojang/Microsoft terms; not an open-source license | No |
| JetBrains Java Annotations | Compile-only BlueMap class-path dependency | `23.0.0` | Apache-2.0 | No |
| JUnit | Test framework | `5.11.4` | EPL-2.0 | No |
| Checkstyle | Source-style verification | `10.18.2` | LGPL-2.1-or-later | No |
| Gradle Wrapper | Build bootstrap | `9.4.0` | Apache-2.0 | Repository tooling only |

The human-accepted M0-M3f runtime record used the historical BlueMap
backport commit `fe79cf5b9f4d8ca28f4e41c2aeb9ef792e336a8d`; the current host row applies
to the All the Mons 1.2.0 S1 checkpoint and M4/M5 review candidate.

`LICENSES/LGPL-3.0-only.txt` is the project license. The corresponding
GPL-3.0 text is included because LGPLv3 incorporates GPLv3 by reference.
`LICENSES/MIT.txt` preserves BlueMap's complete MIT notice. Athena declares
MIT; because no Athena source or assets are copied, its exact declaration and
author credit are recorded here without adding another upstream license file.
The repository contains no copy
of AE2's separately licensed assets or text. AE2 facade models and textures
remain operator-installed CC BY-NC-SA 3.0 resources; only identifiers and
cryptographic digests are recorded. Minecraft stone resources are also
operator supplied and only their identifiers and digests are retained.

The CodeChickenLib-derived files audited inside AE2 declare
LGPL-2.1-or-later. The M2 ADR keeps their facade-specific behavior in this
LGPL-3.0-only family boundary and excludes it from any permissive shared
toolkit. The exact AE2 M3a and ExtendedAE M3b artifacts have completed their
bounded technical and human visual acceptance. The exact AE2 M3c artifact,
schema-6 gallery and map archive completed bounded technical and human
acceptance on 2026-08-08; BlueMap 5.22 PRBM still cannot encode AE2's client
`shade=false` quad flag, so no pixel-identical directional-lighting claim is
made, and client particles plus cross-mod `getAppearance` proxies remain
outside that route. M3d covers eight native AE2 formed-crafting blocks, 30
exact resources, and a closed 29-ID compatible-but-unsupported connector set
proved from exact MEGA Cells and Expanded AE bytecode; its exact artifact,
schema-7 gallery and map archive completed bounded technical and human
acceptance on 2026-08-08. M3e covers only the two native quantum block IDs and
a 13-resource exact closure. It uses the
saved formed/waterlogged state, treats constructed role/corner/power as
unavailable client-stream data, renders static-off-unknown, excludes particles
and makes no extension connector compatibility claim. Its exact artifact,
schema-8 gallery, and map archive completed bounded technical and human
acceptance on 2026-08-08. M3f is the accepted local completion route for six
native block IDs: paint, two Sky Stone chest variants, crank, inscriber, and
spatial pylon. It emits persisted paint splotches and static structural poses;
contents, items, fluids, machine animation, particles, pylon global validity
beyond a bounded local component, online state, and power are excluded. Pylon
roles use an uncached native axis-line scan capped at 256 pylons; bounded
locally invalid bend or branch components render AE2's unformed BASE-plus-DIM
appearance, while missing, malformed, or capped observations fall back
atomically. Its exact 33-resource closure is identifier/digest evidence; no
third-party resource is bundled. The exact corrected M3f artifact, schema-9
gallery, and map archive completed bounded technical and human acceptance on
2026-08-09. Those accepted M0-M3f results remain historical All the Mons
1.1.1 / NeoForge 21.1.234 evidence. The accepted S1 checkpoint instead binds
All the Mons 1.2.0 / NeoForge 21.1.248 and the current version rows above. S1 adds
the exact 29-part/30-endpoint structural catalog, all 64
facade masks, per-instance valid static BlockState materials, the exact 24-ID
facade whitelist neutral/default-state and `isSolidRender` table, and a
41-root/43-JSON/56-PNG/99-resource closure. The eleven AE2-native facade
entries are only the resource-pinned normalization subset. Optional-tag and
ordinary materials are bounded to the admitted static full-cube-witness model
topology: one unrotated full-cube witness is required, while bounded extra
static elements and multipart source quads remain admitted under all semantic
gates. Otherwise-valid complex models fall back atomically.
All 24 explicit whitelist families have an exact complete persisted property
schema and blockstate-resource digest; missing, extra, or out-of-domain
properties fall back atomically, while valid vanilla states remain preserved.
The exact source contract establishes `isSolidRender` and same-state
`skipRendering` as family-invariant across all 554 valid explicit-whitelist
states, so the neutral/default-row booleans apply to each whole family.
Plane masks preserve logical front-view bits separately from
`PlaneBakedModel` visual-local bounds, `QuadRotator` installed-world output,
and the face-aware installed-face-local collision axes used by
`PlaneConnectionHelper` and `BusCollisionHelper`.
Facade layers accept no tint or one shared nonnegative source tint index;
mixed nonnegative tint indexes fall back atomically at the host projection.
UV reinterpolation is bounded to AE2's exact nominal-face two-dimensional
`InterpHelper` grid; incompatible projected quads fall back atomically.
Cardinal block-state variant geometry/vectors and uvlock coordinates use exact
signed-permutation quarter-turns before that source-exact grid check to avoid
host floating-point matrix drift.
Exact stone retains the accepted M2 geometry/material host projection; other
weighted sets require equivalent geometry, material and UVs across every
alternative or fall back atomically.
The exact 24-entry same-state `skipRendering` table is true only for both AE2
quartz-glass IDs and honey; quartz/vibrant cross-family culling follows the
source render-shape rule. Gallery controls pin glass true and oak log/leaves
false. Other ordinary/tag states use a documented BlueMap host projection.
Corner tolerance is `0.00001` in source/analyzer block units and `0.00016` in
runtime sixteenth units. Reversed facade-strip endpoints retain Minecraft
`AABB` min/max normalization.
Facade AO uses the source face transformed only by the block-state variant;
element rotation changes geometry but not AO direction. Element emission is
retained for triangle blocklight and excluded from structural map-color
illumination, which uses the original center/outward world-light samples.
All 30 native endpoints require exact complete persisted property domains and serialized
block-entity IDs, including ALL-side endpoints. A closed 67-entry catalog
across the four exact extension artifacts forces UNKNOWN atomic fallback for
known non-native grid-node hosts without claiming their rendering support. A
catalog block is admitted compatible only with its exact serialized BE; a
missing/wrong BE is malformed UNKNOWN, and an unrelated block carrying that BE
remains disconnected.
Parsed 43-model and decoded 56-texture-plus-animation semantic signatures,
plus the separate decoded nineteen-texture quartz-facade dependency, reject
same-count resource-pack drift. BlueMap 5.22 cannot encode AE2's per-quad shade
bit, so no pixel-identical directional-lighting claim is made. It packages
only identifiers, sizes, and digests. S1 machine contents, held items, fluids,
activity, and accurate Drive LED states are excluded; missing, malformed,
capped, transient-only, or unsupported observations fall back atomically. Its
exact technical runtime lifecycle and human acceptance completed on
2026-08-11.

M4/M5 adds exact static support for the six AE2-family extension rows plus the
Athena dependency row above through eight independently fail-closed routes.
The implementation resolves every model, texture, and animation metadata file
from the operator-installed exact JAR and packages only local code,
identifiers, sizes, digests, and semantic catalogs. No upstream resource,
source, class, capture, precomputed mesh, or binary is bundled. The LGPL-3.0-
family projects reuse the retained LGPLv3/GPLv3 texts; Athena reuses the
retained MIT text. The M45 exact-artifact/resource gate and bounded Java tests
passed, but final artifacts, schema-11 identities, isolated lifecycle, live-
map evidence, and human visual acceptance remain pending.
