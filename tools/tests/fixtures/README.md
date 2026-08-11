# PRBM analyzer fixture

`test_analyze_prbm.py` constructs a bounded, single-member-gzip fixture for
all 597 anchors in `gallery/cases.json`. The fixture uses BlueMap 5.22's exact
seven-attribute, non-indexed, little-endian PRBM v1 byte layout. Ordinary
resource-model anchors have at least one interior triangle and a deliberately
unselected support-top triangle where applicable. M3c glass uses its exact
six face planes, and the case-11 opaque stone contributes ten context-only
triangles at a position that is deliberately not an anchor.

The complete retained M1 slice remains 48 cases, 269 anchors, 266 custom
anchors, 7,576 custom triangles and 140 selected resources. M2 adds 14 cases,
21 anchors, 12 custom anchors and 1,000 custom triangles. The combined custom
contract is 278 anchors, 8,576 triangles and 149 resolved materials: 148 AE2
textures plus `minecraft:block/stone` for the bounded facade lane. M3a adds 14
cases, 33 anchors, 32 custom Drive anchors, 3,856 triangles and 10 new AE2
materials. The full schema-4 contract is 310 custom anchors, 12,432 custom
triangles, 159 resolved materials and 12 atomic zero-triangle fallbacks.
M3b adds 16 cases and 36 Extended Drive anchors: 32 custom anchors, four
atomic fallbacks, 5,056 triangles, eight new resources, all 20 slots on both
faces, all 24 facing/spin states, the exact 26-ID accepted catalog and all 15
occupied models. The schema-5 total is 342 custom anchors, 17,488 custom
triangles, 167 resolved materials and 16 atomic fallbacks.

M3c adds 11 representative cases and 47 custom anchors for the two native AE2
quartz-glass variants. Their exact 776 triangles select four base and 15 frame
resources, cover every nonzero local frame mask, retain two mask-`0000`
base-only faces, omit shared glass faces and cull the opaque-neighbor face. The
schema-6 cumulative total is 389 custom anchors, 18,264 custom triangles, 186
resolved materials and the same 16 atomic fallbacks. The frozen first 92 cases
have canonical SHA-256
`a022d0e75aab44d75692cc0a8848eb3eaecb26d4afe939da7d4797edf7dcb08e`.

M3d adds nine cases and 86 formed-crafting anchors: 85 custom anchors, 4,306
custom triangles, 15 resources and one new atomic fallback. Of the custom
anchors, 84 are nonzero and the fully enclosed center is explicitly zero
geometry. The accepted schema-7 total is 474 custom anchors, 22,570 custom
triangles, 201 resources and 17 fallbacks; its canonical `cases.json` SHA-256
is
`c60d2afff5a1f92da4972963fcb926c38093f43bb6d7f550799f104349728a38`.

M3e adds three complete XZ/XY/YZ quantum bridges and 27 custom anchors. Each
bridge owns 396 static-off triangles: 108 at its link and 36 at each of four
corner and four edge rings. The schema-8 total is 115 cases, 519 anchors, 501
custom anchors, 23,758 custom triangles, 203 resources, 17 empty fallbacks and
one ten-triangle stone control. The M3e slice owns 1,188 triangles and emits
four resources. The 3,123,572-byte schema-8 `cases.json` has SHA-256
`93963dd0bb60a276e1a17c6dd1f4eb916cd92bef4ef30a2e8bdc7a2bfa818b3e`.

M3f adds seven cases and 78 custom anchors, 2,822 custom triangles, 15 emitted
resources and no M3f fallback. Its 17 pylon anchors include the ten
isolated/straight axis-role anchors plus every member of complete invalid
three-block L and four-block T components. The schema-9 cumulative total is
122 cases, 597 anchors, 579 custom anchors, 26,580 custom triangles, 218
selected resources and 17 fallbacks. Its fixture
uses exact source-derived paint, closed chest, neutral crank, neutral
Inscriber and two-layer pylon triangles rather than generic count-only meshes.

Smart overlays carry their exact tint and fullbright attributes. Terminal
layers carry exact non-emissive tints, boundary-face placement and UV-derived
spin orientation; facade triangles must occupy the declared thin face ring
without filling its terminal opening. Corruption tests independently reject a
wrong terminal tint, wrong spin, wrong facade face and leaked fallback mesh.
Drive tests require every `90+16N` total, all 24 facing/spin transforms, exact
slot bounds and model UV regions, six chassis triangles and ten black,
fullbright, AO-one LED triangles per occupied slot. They also prove that the
component-bearing and component-free pair has one normalized mesh and reject
orientation, translation, UV, AO, component-sensitivity and fallback leaks. A
corresponding Extended Drive lane requires `116+16N`, slot-specific front/back
orientation, material-specific chassis meshes, a normalized front/back mirror
pair and a component-insensitivity pair. The Extended component comparison
normalizes renderer-controlled geometry, materials, UVs, colors, and AO while
excluding ordinary model blocklight/sunlight, which BlueMap derives from each
anchor's world neighbors. Per-anchor validation still requires every static
offline LED to be exactly black, fullbright, AO one, and in its declared
geometry. Dedicated regressions vary only ordinary world light and separately
prove that an LED light mutation still fails closed. M3c regressions enforce
the exact asymmetric base UV crop, face planes, winding/normals, materials and
masks, white RGB, AO 255, same-face base/frame light equality and vibrant
blocklight 15. A legal ordinary-glass world-light change must preserve
geometry and non-light topology signatures while changing the attribute
signature. Symmetric-inset UVs, shared-face leakage and every tested geometry,
material, attribute or lighting mutation fail closed.
The frame mask uses AE2's numeric bit contract: absent local neighbor `i` sets
`1 << i`, followed by four-bit binary formatting. A dedicated source-locking
regression requires the up/down-connected anchor at `(214,100,290)` to select
`frame1010` and rejects the former reversed `frame0101` interpretation.

M3d regressions validate exact cuboids, connected-face removal, winding,
materials, monitor tints, powered fullbright overlays, world-derived light and
the 4,306-triangle closure. M3e regressions validate complete-plane
link/corner/edge topology, exact source-float cuboids, bounds-mapped UVs,
winding, four emitted static-off resources, white RGB, air-isolated
neighbor-derived AO 255 and ordinary host light. Separate mutations prove
that geometry, winding, UV, material, color, AO, light, role/state/topology,
power policy, particle policy or animated-light-resource drift fails closed.
M3f regressions compare exact q16 position/UV meshes with preserved winding,
encoded normals, RGB, AO and face-consistent world light. Independent
hard-coded goldens cover paint clamp/layer coordinates, the non-cubic chest
lock UV strip, crank face/UV/bounds, Inscriber shell/stamp coordinates and all
ten pylon axis-role signatures. Every geometry/attribute hook is invoked,
including family-specific bounds, texture/material, orientation, stamp,
pylon X/Y/Z UV, invalid-component unformed projection, AO and light
corruptions.

The extension-disabled mode requires all 36 M3b anchors to be empty while M3c
through M3f remain active. Glass-, crafting- and quantum-disabled independently
empty their own route while retaining M3f. M3-completion-disabled projects all
78 M3f anchors to their exact original resource models while preserving the
accepted schema-8 custom slice. A separate opt-in test adds all
1,024 retained dense cells and their 63,488
triangles. Reordered texture galleries prove that semantic signatures
do not depend on material ordinals. The embedded 290-anchor M2 regression and
explicit schema-3 compatibility test retain the accepted v3 normalized
material, shape, geometry and attribute signatures exactly.

A separate dependency-free stock fixture contains only the texture-gallery
entries needed for the baseline. It locks the vanilla stone control plus 38
stock-rendered M3f machines at 1,882 triangles/five resources; 40 M3f anchors
and all 518 pre-M3f non-control anchors are empty. Regressions cover leaked
geometry, the wrong stone count/material and invalid dense-mode combination.

The generated binary map is deliberately not committed. `expected-report.json`
locks manifest-derived fixture counts and deterministic semantic signatures.
It pins schema-8 `cases.json` at SHA-256
`93963dd0bb60a276e1a17c6dd1f4eb916cd92bef4ef30a2e8bdc7a2bfa818b3e`
and these frozen schema-8 regression pretty-report identities. The schema-9
M3f contract is checked independently by hard-coded geometry, topology and
mode assertions:

| Mode | Size | SHA-256 |
| --- | ---: | --- |
| Enabled | 3,514,458 bytes | `abe455cd331de73aa80dda591f65c28ac177077e9a05b45f9b336bf176605f5d` |
| Extension-disabled | 3,185,763 bytes | `e6afc1f85308ebd719246269037a5ebbb43591030253100afdc62d5f75c2624b` |
| Glass-disabled | 3,332,743 bytes | `8276c321ddc32d54a9810d3547e415fa0f769c5f6e718d9eadf20297fab36056` |
| Crafting-disabled | 1,827,577 bytes | `9e5ef3281c606e82138e5a4d21fa31978c4e372a8eea56d80775ad011324a611` |
| Stock | 522,831 bytes | `6f26509016a7b59256b9c4541161a1796f1f19d135d97eba6eb4b80809765100` |

Quantum-disabled is rebuilt twice in its focused test and must reproduce the
frozen schema-7 projection while leaving all 27 M3e anchors empty. These are
test goldens and deterministic synthetic checks, not observed BlueMap runtime
results. The expected-report file itself is 17,025 bytes with SHA-256
`f56b68d6a3a518eebb4e03d4f32076e3e820178819db479f6fea175b627c2631`.

`exact-writer-one-triangle.prbm.b64` is different: the Java test constructs its
one triangle with the exact pinned BlueMap `ArrayTileModel` and `PRBMWriter`,
then requires byte-for-byte equality with this golden payload. The Python test
parses the same bytes and asserts its vertices, UVs, material group, padding
and group-partition validation. This couples the analyzer contract to the real
writer without packaging BlueMap code or a third-party binary in the add-on.

`native-structural-goldens.json` contains source-derived S1 acceptance facts,
not rows copied from the production renderer or its oracle exporter. It fixes
the AE2 19.2.17 facade thickness and collision-box cutouts, short-anchor
no-hole rule, transparent inset and opaque corner kick, plane-mask expansion,
P2P pixel geometry/nibble order, smart-requesting emitter behavior, exact
opposite-endpoint straight rules, and vanilla oak-log model variants. Python
tests exercise these facts against independently constructed records and
mutations before the compiled-runtime oracle is allowed to act as a
subordinate regression snapshot.
