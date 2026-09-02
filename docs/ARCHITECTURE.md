# Architecture

## Boundary

This project is a plain BlueMap add-on. It is not a NeoForge mod and has no
runtime linkage to AE2-family mod, Athena, or Minecraft classes. The only
production integration surface is the exact BlueMap 5.23 feature-backport renderer, resource-
pack and block-entity registries, contained under `adapter/bluemap523`.

The current packages have deliberately narrow duties:

| Package | Duty |
| --- | --- |
| `api` | Bounded data-only registration for soft-dependent add-ons: immutable cable-bus part and native-Drive cell definitions plus isolated route state |
| `activation` | Family-wide core activation plus independently fail-closed accepted routes, eight published M4/M5 routes, and one unreleased AppMek route |
| `adapter/bluemap523` | Exact BlueMap 5.23 registration, bounded cable-bus/Drive/Crafting Monitor/M3-completion/extension DTOs, native and extension neighborhood projection, resource routing, world access, geometry emission and original-resource fallback |
| `diagnostics` | Bounded, location-free reason counters and one-time warnings |
| `model` | BlueMap-independent cable-bus/native-structural/Drive/glass/crafting/quantum/M3-completion and extension catalogs/snapshots, strict decoders, topology, bay mapping, position RNG and neutral geometry |
| `profile` | Exact AE2 19.2.17 plus exact All the Mons 1.2.0 AppliedFlux, ME Requester, Expanded AE, MEGA Cells, Advanced AE/Athena, ExtendedAE, Applied Mekanistics and Mekanism artifact/resource identities, operator disablement and milestone-bounded activation |

`tools/analyze_prbm.py` is a development-evidence consumer, not production
add-on code. It has no Java or server runtime linkage and is never packaged in
the add-on JAR. Its parser is fail-closed against the exact non-indexed,
little-endian, seven-attribute PRBM v1 dialect emitted by BlueMap 5.22. Reads
are bounded while streaming: ordinary JSON is limited to 16 MiB, gzip input
to 128 MiB and decompressed gzip payloads to 256 MiB.

AE2-family extensions live in separate internal profile packages in this one
repository and JAR. An extension route may consume a small neutral core
capability but cannot make the core profile depend on extension classes.

External add-ons use only the public `api` package. Registration accepts no
callbacks or renderer objects, is atomic, rejects built-in and cross-add-on ID
collisions, and freezes before either AE2 resource extension starts rendering.
Each returned route starts inactive. Its owner may activate it only after its
own resource/artifact gate passes; a route-local failure disables only that
route. With no external registrations, the frozen catalog is empty and the
published profiles take their existing paths unchanged. The complete consumer
contract is in [EXTENSION_API.md](EXTENSION_API.md).

## Activation sequence

1. `BlueMapAe2Addon` uses the shared Adapter API to check the audited BlueMap
   5.23 feature-backport ABI before
   loading the adapter. An unknown or incomplete ABI remains inactive.
2. `BlueMap523Adapter` registers the bounded `ae2:cable_bus`, `ae2:drive`,
   `extendedae:ex_drive` and Crafting Monitor DTOs plus the block-only
   M3c/M3d/M3e/M3f renderers, the S1 native-structural cable-bus route and
   their bounded DTOs before any
   add-on action can cause BlueNBT to freeze its type resolver. Discovery does
   not deserialize world NBT.
3. The adapter preflights namespace-disjoint renderer/resource-extension
   keys, registers them and verifies object identity. Registry collisions or
   partial probes leave the route inactive.
4. On resource-pack load, `ExactArtifactDetector` locates the one JAR declaring
   AE2 and validates its complete identity. `ExtendedAeArtifactDetector`
   independently locates the one JAR declaring ExtendedAE and validates either
   its exact historical 5,573,972-byte 2.2.33 identity or its exact current
   5,578,031-byte 2.2.35 identity. Synthetic dispatch and operator-installed
   resources are checked before each route activates. The AE2 verifier proves
   SHA-1/SHA-256/SHA-512, metadata, 85 cable IDs, one face-part ID,
   `ae2:drive`, the closed 23-item Drive catalog, 158 AE2 textures and the
   complete 196-resource digest closure. The disjoint M3c verifier separately
   proves both property-free glass blockstates, their dynamic model identity
   and the exact 22-resource/19-texture closure without changing that accepted
   196-resource partition. The disjoint M3d verifier proves all eight native
   crafting blockstates, seven formed models, fifteen textures and the exact
   30-resource/6,177-byte closure without changing an accepted route
   partition. The disjoint M3e verifier proves both native quantum blockstates,
   their three source models, six source textures plus two animation metadata
   files, and the exact 13-resource/3,798-byte closure. The static-off route
   emits four of those six audited textures. The ExtendedAE verifier proves the
   exact metadata, `extendedae:ex_drive`, its three built-in cell IDs and the
   disjoint 15-resource/eight-texture closure, while reusing the already pinned
   26-resource AE2 Drive partition. The effective vanilla stone graph remains
   operator supplied and is checked structurally. The disjoint M3f verifier
   proves six native block IDs, five block-entity identities, exact crank and
   Inscriber model semantics, 17 source textures and the exact 33-resource/
   22,491-byte closure recorded by a 3,738-byte manifest; its static
   projection emits 15 textures.
   The disjoint S1 verifier additionally locks all 29 ordered native face-part
   identities, nine reporting-spin parts, both planes, six P2P frequency
   parts, legal dense layouts, exact facade state/material inputs, 30 native
   endpoint identities and the separately audited compatible-unknown Expanded
   AE control. It also proves the exact operator-installed AE2, Minecraft,
   Advanced AE 1.6.12, ExtendedAE 2.2.35 and Glassential 3.4.5 evidence used
   by the current All the Mons 1.2.0 route.
   The expanded M45 verifier proves the exact AE2 artifact plus nine
   pack-pinned AppliedFlux 2.1.5, ME Requester 1.4.3, Expanded AE 2.1.1,
   MEGA Cells 4.11.0, Advanced AE 1.6.12, Athena 4.0.6, and ExtendedAE 2.2.35
   artifacts, including Applied Mekanistics 1.6.3 and Mekanism 10.7.19. It
   checks complete-artifact digests and twelve exact resource manifests with
   381 rows before a dependent route can activate. The published alpha.1 predecessor remains
   the historical eight-artifact/eleven-manifest M4/M5 acceptance record.
5. The `ae2` profile remains inactive when it is absent, disabled by the
   operator, unsupported, malformed or blocked by a failed core capability.
6. Only a complete exact-profile pass enables its resource routing. The
   ExtendedAE route additionally requires active exact AE2 core and native
   Drive capabilities, but ExtendedAE absence, mismatch, operator disablement
   or later route failure does not disable the accepted AE2 routes. The M3c
   route independently requires active exact AE2 core plus its synthetic state
   and resources; its collision, disablement or failure leaves M0-M3b routes
   unchanged. The `ae2-crafting` route has the same route-local activation
   boundary and leaves every accepted M0-M3c route active when its profile,
   resources, DTO registration or renderer fails. The
   `ae2-quantum-bridge` route independently requires the exact AE2 core,
   synthetic state, resources and renderer identity. Its profile, topology,
   resource, registration or callback failure leaves every M0-M3d route
   unchanged. The `ae2-m3-completion` route independently requires the exact
   AE2 core, bounded DTO retention, synthetic state, exact resources/models
   and renderer identity. Its profile, topology, resource, registration or
   callback failure leaves every M0-M3e route unchanged.
   The `ae2-cable-bus-structural` route independently requires the accepted
   AE2 core plus exact profile, DTO retention, semantic resources and endpoint
   state/side contracts. Its operator disablement or failure preserves the
   accepted M0-M3f routes and delegates every affected cable bus to the
   predecessor/original-resource path.
   M4/M5 then activates eight route-local profiles: `appflux`, `merequester`,
   `expandedae`, `megacells`, `advanced-ae-quantum`,
   `advanced-ae-athena`, `extendedae-matrix`, and `extendedae-planes`. Every
   route requires the exact active AE2 core. `advanced-ae-athena` additionally
   requires exact Athena 4.0.6, and `extendedae-planes` additionally requires
   active S1 native structural support. Artifact detection, required-resource
   checks, synthetic dispatch, DTO retention, texture baking, and callback
   containment are route local. An AE2 core failure blocks all eight; an S1
   failure blocks only the ExtendedAE planes among them. The canonical
   `extendedae` operator switch maps to both ExtendedAE M4/M5 routes.

BlueMap exposes no transactional multi-registry update and no add-on unload
lifecycle. Preflight reduces the partial-registration risk, but installation,
update, disablement and removal still require a full JVM restart.

## Persisted-data boundary

The BlueNBT DTO retains only the bounded values required to decide whether the
current profile may render safely:

- the center `cable` field's shape/status and its direct string `id`, when
  present;
- each present face part's direct string `id`, optional byte `spin` and
  optional short `freq` from the six directional fields;
- each present facade's direct block-state `Name` and bounded optional string
  `Properties` map from the six `facade<Direction>` fields;
- `hasRedstone`, solely as bounded scalar compatible input.

The separate `ae2:drive` DTO retains only:

- the direct `inv` compound;
- its exact `item0` through `item9` slot compounds;
- each slot's direct string `id` and integral `count`; and
- no component data: at most one `components` compound is accepted and
  bounded-skipped during decoding because M3a cell appearance is
  component-insensitive.

The separate `extendedae:ex_drive` DTO follows the same bounded streaming
contract but retains exactly `item0` through `item19`. It accepts at most one
bounded-skipped `components` compound per slot and rejects an unexpected
`item20`, duplicate retained fields or malformed declarations.

Explicit BlueNBT 3.5.1 field deserializers replace the generic `Object`
deserializer for cable, face, facade and both inventory DTOs. They stream past
irrelevant values without constructing nested maps, lists or arrays. Each retained field
is independently limited to 16 container levels, 512 visited tags, 256 entries
per list, 4,096 elements per array and 65,536 aggregate array bytes. Center
and part IDs are limited to 256 characters, as is a facade block ID, and
direct retained-compound field names are rejected after 64 characters.
BlueNBT exposes array lengths to a custom
deserializer only through consuming buffer APIs, so array limits are checked
after payload consumption into reusable zero-length buffers, without allocating
an array proportional to the declaration. A duplicate retained key, excessive
declaration or bounded-reader violation rejects that typed DTO; BlueMap's
block-entity resolver then uses its ordinary base block-entity fallback. A
structurally well-formed but unsupported retained value reaches the strict
decoder and delegates to that route's original block resource instead. BlueNBT's
type resolver raw-copies
the complete compound before that recovery path and performs a separate base
parse. Invalid tag IDs, premature EOF and other corruption encountered there
remain host behavior outside the add-on's replacement boundary and may abort
that block-entity or chunk parse before the typed reader runs.

`CableBusDecoder` receives only these tiny map-shaped projections, never the
source compounds. It recognizes the exact 85 center IDs formed by 17 AE2
colors and the glass, covered, smart, covered-dense and smart-dense families.
The accepted M2 predecessor recognizes only `ae2:terminal` with an exact byte
spin in `0..3` plus one canonical same-face property-free stone facade. S1's
independent structural decoder expands that bounded projection to the exact 29
native face-part IDs, including byte spins only for the nine reporting parts
and unsigned-short frequency only for the six P2P parts. It also accepts the
exact profiled facade state schemas and resolves only source-locked native
endpoint-side/state observations. Missing, malformed, unknown or unsupported
fields and layouts remain whole-cable-bus fallback. The raw NBT object is
neither retained nor logged.

`DriveDecoder` receives an immutable ten-slot projection plus BlueMap's
persisted `facing` and `spin` block-state properties. It accepts all six
facings, spin values `0..3`, empty slots and exactly 23 AE2 19.2.17 item IDs.
Every occupied accepted slot must have count one. Missing or malformed
inventory/state, an unknown AE2 item or any other namespace—including an
extension cell—delegates the complete block to the original Drive resource.
Components do not affect the result and are never retained as a generic
object graph.

`ExtendedAeDriveDecoder` receives an immutable twenty-slot projection. It
accepts the same six facings and spins, count-one occupied slots, the accepted
23 native AE2 items and exactly three built-in ExtendedAE cells. Slots `0..9`
map to the front; `10..19` map to the rear using the opposite facing and the
same spin. Dynamic KubeJS cells, MEGA Cells, every other unknown item and
malformed state delegate the complete block to the original Extended Drive
resource. Components remain render-insensitive bounded payload rather than a
retained object graph.

Ordinary Anvil data does not preserve live cable connection directions,
channel count, online state, power state or Drive LED status. Cable rendering
therefore applies the named `idle-off-unknown` policy, while Drive LEDs apply
`static-offline-unknown`. No route synthesizes or claims live network
telemetry.

The M3c quartz-glass blocks have no persisted properties and no block entity.
Their render snapshot consists only of the exact center ID and six direct
neighbor classifications read from BlueMap's world view; no client model data
or additional world state is retained.

The M3d Crafting Monitor DTO retains only one optional `paintedColor` byte.
Absence maps to AE2's transparent ordinal 16; exact values `0..16` are
accepted, while wrong type, duplicates or out-of-range values reject the
whole block. Other crafting blocks require no DTO. Stable blockstate input is
exactly `formed` and `powered`, plus `facing` and `spin` for monitors. The
client-only transient displayed `GenericStack` is not persisted reliably and
is deliberately omitted.

M3e retains no quantum block-entity payload. It requires only the exact block
entity ID `ae2:quantum_ring` on every member of a complete structure and reads
the stable `formed` and `waterlogged` block-state properties. Persisted power
does not provide a reliable client animation state, so the route deliberately
uses `static-off-unknown`; it does not decode, synthesize or claim quantum
network state.

M3f retains only the bounded persisted input needed by its six exact native
blocks: the 256-byte paint `dots` payload, the Sky Chest block entity identity,
the crank block entity identity plus its attachment-facing state, the
Inscriber's facing/spin identity without item contents, and the spatial
pylon's exact block entity plus an uncached native-axis scan capped at 256
pylons. It does not
retain or interpret chest contents, crank held items, Inscriber inventories or
animation, spatial-network power, fluids or other live machine state. Missing,
duplicate or malformed retained input rejects the route locally and uses the
original block resource. A scan that reaches its cap also falls back; a fully
observed invalid bend or branch instead renders every component member as
AE2's unformed `NONE` role.

M4/M5 reuses the bounded cable-bus and Drive readers and adds only the exact
extension fields needed by a static world projection:

- AppliedFlux accepts its exact Flux Accessor part and 20 exact FE cell item
  IDs. The part's `fast` value and all live status are visually irrelevant;
  cells use the existing `static-offline-unknown` Drive policy.
- ME Requester reads the requester's persisted `active`, `facing`, and
  vertical `spin` state and normalizes its nonstandard `ae2:z` model rotation.
  Its terminal part keeps exact face/spin structure but uses the offline pose;
  requested items, jobs, and terminal activity are not retained.
- Expanded AE accepts exact formed/powered crafting state, exact I/O Port
  facing/push direction, and exact part identity/spin/color fields. Machine
  contents and patterns are omitted. `expandedae:colorable_drive` deliberately
  retains atomic stock fallback because the exact upstream artifact exposes no
  bounded world visual to reproduce.
- MEGA Cells accepts exact formed/powered crafting state, the bounded Crafting
  Monitor paint byte, three generic part IDs, and Cell Dock `cell`/`spin`.
  Priority, cell status, stored contents, displayed stacks, and activity are
  ignored or rejected according to the closed contract. Its 67 exact cells
  share one route across Cell Dock, native Drive, and Extended Drive output.
- Advanced AE accepts the four exact persisted quantum-computer properties
  (`formed`, `powered`, `multiblocked`, and `light_level`) plus the exact block
  entity and complete 3-by-3-by-3 appearance observation. The static projection
  forces power off, light zero, no emission, and animation frame zero. Quantum-
  alloy CTM has no retained block entity and matches exact whole block states.
- ExtendedAE accepts exact Assembler Matrix `formed`/`powered` state and frame
  `shape`, plus exact block-entity identity and the bounded matrix-glass
  appearance neighborhood. It preserves formed structure and shape but forces
  power off. Active Formation and Smart Annihilation plane parts retain face
  and connection mask only; activity, power, and spin are normalized away.

No M4/M5 route retains arbitrary NBT, inventories, fluids, live grid state, or
machine output. Missing, duplicate, malformed, unknown, or unsupported retained
input falls back atomically to the original resource for the owning block or
cable bus.

## Topology and rendering

The resource extension must route at the block-ID level because the resource
key does not contain block-entity NBT. When the exact profile is active,
`ae2:cable_bus` enters `CableBusRenderer`; the renderer then either emits the
supported geometry or immediately delegates to the captured original AE2
resource.

For a recognized center cable, a direct six-neighbor `ae2:cable_bus` may
connect when its decoded center family and color are compatible. All five
families can connect to one another. Two non-transparent colors connect only
when equal; the `fluix` persisted ID maps to AE2's transparent cable color and
can connect to every color. The effective connection size is the least of the
two AE2 cable types, while the visible half-arm remains constrained by the
local family. A different non-transparent color is a definite disconnection.
Devices and incomplete neighbors are ambiguous and cause whole-block fallback.
A supported terminal on the neighbor-facing side is a definite obstruction;
unknown or malformed neighbor parts remain ambiguous and fall back atomically.

The neutral M1 geometry targets the exact AE2 19.2.17 cable rules:

- family-specific glass `(6..10)`, covered/smart `(5..11)` and dense
  `(3..13)` cores in sixteenths;
- glass, covered/smart-sized and dense connection geometry selected from the
  exact local/effective family matrix;
- the exact audited face winding and UV projection;
- a single full-span prism, with both axial caps omitted, for exactly two
  opposite connections only when both effective types equal the local family;
- distinct color/family core and connection texture identifiers resolved from
  the operator-installed AE2 resource pack; and
- the exact dark and bright zero-channel overlays for smart and smart-dense
  cables under `idle-off-unknown`.

M2 composes the operator-installed `display_base`, `terminal_off` and
`display_status_off` models into the static reporting-terminal pose. The exact
AE2 face orientation and four persisted spin rotations provide 24 supported
face/spin combinations. Terminal attachment geometry participates in cable
topology, so a terminal-bearing two-ended cable does not use the cable-only
straight simplification. Multiple terminals on different faces are supported
when the non-dense center and every part are otherwise exact.

The bounded facade lane emits a thin stone ring on the terminal face with an
exact `2..14` opening. Its material is resolved only after the effective
vanilla stone blockstate/models collapse to one opaque, non-animated full-cube
texture. Client-only weighted stone UV orientation is not persisted, so the
fidelity claim is geometry and material rather than pixel-identical randomized
UV orientation. No general facade clipping or arbitrary block-state support is
implied.

S1 supersedes that narrow cable-bus structural decision only when its separate
`ae2-cable-bus-structural` route is exact and active. It composes the 29
source-locked native part chassis on any installed face, applies the nine
reporting-part spins, derives all sixteen coplanar masks for annihilation and
formation planes, and projects six P2P types with their persisted unsigned-
short frequency pixels. Cable anchors are the only dense-capable part; legal
dense, multipart and part-only layouts use exact family/core/collar geometry.
The route remains static off/inactive/unlocked and does not infer live grid
state.

The S1 facade lane ports AE2's bounded clamp, UV reinterpolation, face
stripping, corner kicking and short-stilt behavior for all 64 facade masks.
Only exact profile-locked block-state schemas and resource-stack outcomes are
accepted; malformed, non-whitelisted or unresolved material/state input falls
back atomically. Exact native attached-device state and observed-side rules
select covered, smart or dense-smart connection families for 30 endpoint
identities. A known compatible endpoint whose behavior cannot be established,
including the pinned Expanded AE control, remains `UNKNOWN` and forces
whole-cable-bus fallback rather than a guessed connection.

S1 geometry signatures treat only environment-derived blocklight and sunlight
as non-invariant. Full observed attribute signatures still retain them, every
triangle must have a flat light pair in `0..15`, and the four smart/smart-dense
channel resources are exact fullbright. Geometry, winding, UVs, resolved
material, normals, RGB and AO remain exact invariant fields.

M3a routes `ae2:drive` independently from `ae2:cable_bus`. It composes the
operator-installed `drive_base` with the occupied chassis model selected by
each accepted item ID, then rotates the complete result for the exact
`facing`/`spin` state. The 21 native storage/portable/creative IDs
select their exact chassis model; matter cannon and color applicator select
AE2's generic occupied-cell model. Each occupied slot receives a black
offline-unknown LED at its client-derived two-column/five-row origin. An empty
Drive emits 90 base triangles and each occupied slot adds six chassis plus ten
LED triangles, so the exact total is `90 + 16N`. No stored contents, capacity,
online state or activity are rendered.

M3b routes exact `extendedae:ex_drive` independently from both accepted AE2
renderers. It composes the operator-installed 116-triangle Extended Drive base
with the model selected from the closed 26-item catalog. The 23 AE2 cells keep
their accepted native chassis; the three built-in ExtendedAE cells add their
exact infinity or void models. Front slots use the persisted facing and rear
slots use the opposite facing at the same spin. Each occupied face-local bay
adds six chassis triangles and the same ten-triangle static offline-unknown
LED geometry, yielding `116 + 16N` for `N` occupied slots. ExtendedAE's source
LED renderer uses textureless position/color geometry; BlueMap storage needs a
material ordinal, so the add-on uses the already pinned
`ae2:block/drive/drive_front` material solely as a writer/analyzer proxy while
retaining exact black, fullbright, AO-one LED attributes.

M3c routes exact property-free `ae2:quartz_glass` and
`ae2:quartz_vibrant_glass` through one block-only renderer. The two native IDs
cross-connect across the six axial neighbors, suppress their shared face and
derive each remaining face's four-bit frame mask in AE2's face-local order.
The exact Minecraft block-position seed initializes the legacy LCG; its three
draws select one of four base textures and asymmetric high-UV clamps. Masks
`0001..1111` select the corresponding frame texture, while `0000` emits no
frame. Each visible layer uses AE2's exact corners and two-triangle winding,
white CUTOUT color and AO one. Vibrant glass applies emission floor 15;
ordinary blocklight and all sunlight use BlueMap's center/outward maximum,
cave and map-color policies. Geometry is reserved atomically, and
`MaxCapacityReachedException` remains host control flow.

M3d routes only the exact native `ae2:crafting_unit`,
`ae2:crafting_accelerator`, five crafting-storage blocks and
`ae2:crafting_monitor`. Exact unformed states delegate to their original
resources without consuming a fallback diagnostic. For formed centers the
renderer snapshots all six direct neighbors before planning any output.
Exact native crafting neighbors connect regardless of their transient formed
value; missing or malformed native states and the closed known compatible
MEGA Cells/Expanded AE IDs force atomic stock fallback. Every other non-native
neighbor is disconnected unless its ordinary BlueMap properties cull the
outward face.

The neutral geometry ports AE2 19.2.17 `CraftingCube`/`CubeBuilder` rings,
corners, stripes, `2.99..13.01` inner bounds, standard UVs and winding. Isolated
unit, accelerator/storage and monitor blocks emit 108, 120 and 114 triangles.
Connected faces are absent. Unit/base/light/monitor layers use the exact
operator-installed fifteen-texture closure; powered overlays alone become
fullbright, and the monitor front uses exact bright/medium/dark painted tints.
Facing controls its front while spin is intentionally mesh-invariant. World
faces use BlueMap's maximum of center/outward light, top-only, cave, culling
and map-color policies. The complete plan uses one capacity reservation;
capacity exhaustion propagates without fallback or route disablement.

M3e routes only exact `ae2:quantum_link` and `ae2:quantum_ring` states. An
unformed exact state remains stock. A formed center is custom-rendered only
when it belongs to an isolated, complete `3x3x1` bridge in one of the XZ, XY
or YZ planes: the link is the center with four in-plane connections; corners
have two perpendicular connections; edges have three connections with one
opposite pair; all nine members are formed and carry the exact expected block
entity; and the six exterior face slabs contain no additional native quantum
block. Missing neighbor data, an incomplete or crossed structure, malformed
state or the wrong block entity triggers atomic whole-block stock fallback.

The neutral quantum geometry ports AE2 19.2.17's float-source expressions,
left-associative arithmetic, `CubeBuilder` bounds-mapped UVs and winding. A
link emits 108 triangles, each corner or edge ring emits 36, and a complete
bridge emits 396. The route uses the exact link, ring, transparent-glass and
transparent-covered textures; the two animated light sprites are source-
audited but deliberately not emitted by `static-off-unknown`. White color,
neighbor-derived AO and BlueMap's host center/outward light, cave, top-only,
culling and map-color policies are preserved. Quantum particles and item
rendering remain out of scope.

M3f routes only exact `ae2:paint`, `ae2:sky_stone_chest`,
`ae2:smooth_sky_stone_chest`, `ae2:crank`, `ae2:inscriber` and
`ae2:spatial_pylon`. Paint reconstructs persisted splotches from the exact
bounded payload and position seed. Both chest variants render their exact
closed structural model for each horizontal facing. Crank and Inscriber render
only neutral poses across all accepted transforms; their exact JSON-derived
shell geometry preserves host neighbor AO, while manual paint, chest,
Inscriber-stamp and pylon primitives use AO one. Spatial pylons infer only
isolated or straight local axis/start/middle/end topology through an uncached
axis scan capped at 256. A fully observed invalid L/T bend or branch component
renders unformed BASE-plus-DIM geometry for every member. Missing, malformed
or capped observation uses one atomic original-resource fallback per block.

M4/M5 composes existing neutral primitives with exact extension-owned
resources rather than bundling upstream models or textures. AppliedFlux and
MEGA Cells extend both Drive catalogs only while their owning route is active;
each occupied slot retains the existing black offline-unknown LED contract.
AppliedFlux's Flux Accessor, ME Requester's terminal, Expanded AE's two parts,
MEGA Cells' three generic parts and Cell Dock enter the accepted cable-bus
pipeline through owner-tagged part definitions, so a part failure disables only
its extension route. ExtendedAE planes reuse S1's exact plane mask, center
connection and collar geometry; they remain inactive when S1 is inactive.

The Expanded AE and MEGA Cells formed-crafting sets use the same source-derived
crafting-cube geometry as native AE2 while selecting extension-owned texture
partitions. Exact active cross-owner AE2/Expanded/MEGA neighbors connect.
Inactive-owner, missing, or malformed known crafting neighbors make the whole
center fall back instead of emitting a partial cluster. Crafting Monitor
displayed stacks remain omitted.

Advanced AE's seven internal quantum-computer roles and one structure role use
the exact complete 3-by-3-by-3 appearance observation. Unformed and standalone
core states use their exact JSON models; formed internal and structure roles
use connected geometry. Persisted formation and multiblock structure survive,
but power, light, emission, machine contents, and animation are normalized off.
The separate Athena route applies five-texture same-state CTM to exact
`advanced_ae:quantum_alloy_block`; exact animated 16-by-32 source strips are
validated, cropped atomically to synthetic 16-by-16 frame-zero textures, and
never emitted as live animation.

ExtendedAE's Assembler Matrix route covers frame, wall, glass, pattern,
crafter, and speed roles. Non-glass roles select exact off models while
preserving formation and frame shape. Matrix glass derives its connected
appearance from a complete bounded 3-by-3-by-3 observation. Missing,
malformed, or incompatible observations use atomic original-resource fallback.
The canonical `extendedae` switch controls both matrix and plane routes while
the pre-existing Extended Drive route remains independently fail closed.

AE2's client connection test is broader because it performs reciprocal
`getAppearance` calls and can connect a cross-mod proxy that appears as
`QuartzGlassBlock`. BlueMap 5.22 exposes neither that Minecraft query nor
client model data, so this bounded route treats every known non-native block
as disconnected. Only missing state or a malformed native glass state invokes
whole-block original-resource fallback. Client particles and item rendering
are outside this world-block route.

AE2's `GlassBakedModel` also leaves each baked quad's `shade` flag false.
BlueMap 5.22's seven-attribute PRBM format has no shade field, and the web
shader always derives directional darkening from the stored normal and AO.
Consequently the add-on can preserve geometry, material, UV, cutout, AO and
host light attributes but cannot claim pixel-identical client directional
lighting. This is a BlueMap host-format limitation, not a fallback or renderer
failure.

The cable-bus implementation uses a fixed set of 48 immutable geometry components
(three core sizes, 30 direction/family arms and 15 axis/family straight
components). It does not grow a cache per block topology. This is a source
design property. The exact final M1 SNAPSHOT passed the isolated lifecycle,
two-thread and coarse dense-fixture observations on 2026-08-04 and was human
visually accepted on 2026-08-07. Those results do not establish a renderer-only
memory budget or production sizing claim and do not transfer to M2. The M2
implementation has passed its reproducible source/build gates and its initial
enabled and stock cold/warm renders. Both configured render threads accumulated
nonzero CPU time, but this is only a concurrency smoke, not a renderer-only
performance measurement. The restored exact JAR and enabled configuration
reproduced the initial report and 44 relevant web files byte-for-byte, closing
the M2 technical lifecycle. The owner visually accepted the exact 203,599-byte
M2 JAR and bounded gallery on 2026-08-07, completing M2.

The independent M3a Drive route passed two reproducible clean builds and its
delayed, chunk-unload/reload, full-JVM, enabled cold/warm and physically
absent stock phases. The final restored-enabled phase verified without
rebuilding and reproduced its initial report and all 44 comparable web files
byte-for-byte, closing the technical lifecycle. Both configured render threads
accumulated CPU time; this remains a concurrency smoke rather than a
renderer-only performance measurement. The owner visually accepted the exact
M3a artifact and gallery on 2026-08-07.

The M3b Extended Drive route then passed 217 Java and 56 Python tests, two
reproducible clean builds and its complete enabled/extension-disabled/
physically-absent-stock/restored lifecycle. The cumulative fixture remained at
zero failures and one build through actual unload/reload and full JVM
restarts. Enabled cold, warm and restored reports and all 44 comparable web
files were byte-identical; extension-disabled retained the accepted M3a
regression with all 36 M3b anchors empty. Both render threads accumulated 420
and 418 scheduler jiffies, which is only a concurrency smoke. On 2026-08-07
the owner visually accepted the exact M3b JAR
`f02123cb602bb7b6466d1529c5518e45862f53f413ce9a75ecc067d1a30607d1`,
gallery ZIP
`69bdb99d9c8f6838c3b8d5847c32702761cfa77b263ee95384ca24357c84cf92`
and map archive
`c73844990847148d9cd3d315832085e49776e9253c5c8eca6f0b7659d73c4285`.
M3c connected quartz glass then passed reproducible build gates, 247 Java and
71 Python tests, and the complete isolated enabled/extension-disabled/glass-
disabled/physically-absent-stock/restored lifecycle on 2026-08-08. The exact
375,558-byte candidate JAR has SHA-256
`4c1b557ae4c79c738005b74e2f0c89ca4fbe503dd6ef0ba614fae34d8e449d47`.
Its 11-case/47-anchor schema-6 gallery remains representative; Java tests cover
all 64 direct-neighbor masks. Enabled cold, warm and restored reports plus
their 44-file manifests were byte-identical. Route-specific disablement
emptied only its intended anchors, stock retained only the stone control, and
restoration reproduced the initial enabled evidence without rebuilding the
one-build, zero-failure fixture. Both render threads accumulated CPU time and
all accepted capture pods had zero restarts. One operator Ctrl-C while
detaching the console caused a restart attempt during pre-lifecycle setup; it
preceded the accepted captures and was not an add-on restart. On 2026-08-08 the
owner visually accepted the exact 375,558-byte production JAR, 38,929-byte
gallery ZIP and 20,376,253-byte map archive, with SHA-256 identities
`4c1b557ae4c79c738005b74e2f0c89ca4fbe503dd6ef0ba614fae34d8e449d47`,
`0839009fe6a4f4785f864f33bc97fef28b8418f077d5d66a20efc3e8eeb4edab`
and
`3fb5fb174f23c0f2d8ce9f98e8c12feb8b12c444060e35b9d8e0036d8ec165e5`.
That completes M3c. M3d then passed two reproducible clean builds, 285 Java
and 85 Python tests, and the complete enabled/extension-disabled/glass-
disabled/crafting-disabled/physically-absent-stock/restored technical
lifecycle. Its exact 448,915-byte JAR has SHA-256
`ca057f025338150255ea916402c08bc8b614f9398a063e7433bbe468808c93ee`.
The one-build fixture retained zero failures and two stable checks through
delayed verification, actual unload/reload and full JVM restarts. Every
cold/warm pair was byte-identical, and restored enabled output reproduced the
initial report and 44-file manifest. Both render threads advanced by 313 and
520 scheduler jiffies; capture pods had zero restarts. One operator sequencing
attempt failed during init before Minecraft started and was not an add-on or
capture restart. On 2026-08-08 the owner visually accepted that exact JAR,
the 44,201-byte gallery ZIP with SHA-256
`4a18b45f2c03c8d1d3c49a731df2c2503745952faccf9ba06ec8f301909b81f3`
and the 20,417,822-byte map archive with SHA-256
`672cdffaf5135f34c4b10c24638056540dcaadbb5fd2d78b3096897436d8a2c6`.
M3d is complete as a bounded, human-accepted slice. M3e then passed two
byte-identical clean builds, 316 Java tests in 53 suites, 98 Python tests and
the complete enabled/extension-disabled/glass-disabled/crafting-disabled/
quantum-disabled/physically-absent-stock/restored technical lifecycle on
2026-08-08. The one-build fixture retained two stable checks and zero failures
through delayed verification, unload/reload, complete JVM restarts and every
mode. Enabled and restored evidence was byte-identical, all capture pods had
zero restarts, and the two render threads advanced by 587 and 245 scheduler
jiffies. The exact 513,674-byte candidate JAR has SHA-256
`98ff55eaba609fc894b01e0c4d922b47f1871c324945f88f7a34864cf48b124f`;
the deterministic 20,424,799-byte, 44-entry map archive has SHA-256
`9e145fffbe87205651ed7cc6b4cb706b7dcbe394ac26e7ce2eb1d6d55ea411a7`.
The owner visually accepted that exact M3e JAR, gallery and map archive on
2026-08-08; hash-exact accepted aliases exist while candidate aliases remain.
M3e became the accepted rollback slice at that checkpoint. M3f is implemented,
technically complete and human accepted under profile ID
`ae2-m3-completion`; its exact 623,591-byte JAR,
49,679-byte schema-9 gallery and 20,450,880-byte map archive passed the bounded
source, analyzer, persistence, enabled, route-disabled, physical-stock and
restored gates on 2026-08-09. The owner visually accepted those exact three
artifacts on the same date, completing M3. Machine contents, held items,
fluids, live or activity-specific state and accurate Drive LEDs are accepted
non-goals.

The prior M3f candidate was withdrawn after owner visual review exposed
invisible L/T pylon members. It was never accepted. The corrected candidate
described here supersedes it and alone received the bounded M3f acceptance.
At that checkpoint M3f was the latest accepted slice and M3e was its previous
accepted rollback.

S1 now implements that post-M3 cable-bus structural-completeness step as an
All the Mons 1.2.0-retargeted accepted local checkpoint. Two clean Temurin
`21.0.12+8` / Python `3.13.14` builds reproduced its exact 855,833-byte JAR
`5dad1cf654c13b5b0aa5411264104ff2f17b942b7d4c5def698d24c476951c39`.
Each build ran 448 Java tests (446 passed and two opt-in exporter tests were
intentionally skipped) and 167 passing Python tests; all gates passed. Its
exact All the Mons 1.2.0 isolated enabled/native-structural-disabled/physical-
stock/restored lifecycle then passed on 2026-08-11, including one-build
persistence, exact cold/warm rerenders, zero-restart pods, exact restored
route activation and both render workers advancing. The owner visually
accepted the exact 855,833-byte JAR, 70,925-byte gallery and 20,660,117-byte
map archive in BlueMap on 2026-08-11. At that historical checkpoint S1 was the
latest exact human-accepted local result.

M4/M5 then became the complete owner-accepted local checkpoint. Its
clear Java runtime audit and exact eight-artifact M45 verifier passed. Two
byte-identical builds each ran 562 Java tests (560 passed and two intentional
exporter tests were skipped) and 180 pre-oracle Python tests; the final frozen-
oracle CPython 3.13.14 suite passed 192/192 tests in 945.343 seconds. The exact
production JAR, schema-11 gallery, and deterministic enabled map archive are
respectively 1,207,683, 94,537, and 20,821,895 bytes with SHA-256 values
`6fed7a625b02229213a047788995944f14e7e7fcabe0e0ddc6d9b5e994146e9f`,
`c67b4f794092f6e994349a8ee9320c052e2efc87f04e8813faf158c3455fe33b`,
and `44422aa71c2f450951d8433e25e01de7a0b00dbd0d9c4fa4ff74ca98e649a2df`.

The isolated enabled initial/restored, combined-disabled, crafting-disabled,
and native-structural-disabled cold/warm modes passed with exact report and
79-row manifest equality. Full-JVM transitions and the restart, initializer,
worker, one-build, and settle gates also passed. Candidate aliases remain on
`data-atm120` beside hash-exact accepted aliases for the JAR, gallery, and map
archive. The owner accepted those exact artifacts and separately authorized
publication on 2026-08-12. S1 remains the accepted predecessor. Release status
must still be claimed only after the non-SNAPSHOT artifact, tag, package, and
Release gates have actually passed.

No AE2 or ExtendedAE model, texture, precomputed mesh or client capture is
embedded. The model package emits independent numeric geometry at render time.

## Failure and diagnostic policy

Once BlueMap has delivered a block entity and entered the add-on callback,
unsupported or typed-malformed block data uses the direct original-resource
renderer for the complete block. A native Drive-route failure disables only
native Drive; an Extended Drive failure disables only Extended Drive; an M3c
failure disables only connected quartz glass; an M3d failure disables only
formed crafting; an M3e failure disables only quantum bridges; and an M3f
failure disables only M3-completion structures. An S1 failure disables only
native cable-bus structural expansion and restores the accepted predecessor or
original-resource decision. The
independently activated routes remain available, and an ExtendedAE, M3c, M3d,
M3e or M3f failure cannot disable accepted AE2 routes. A core profile or adapter
failure disables dependent routes.

The eight M4/M5 routes use owner-tagged failure boundaries. An AppFlux failure
cannot disable ME Requester, Expanded AE, MEGA Cells, Advanced AE, or
ExtendedAE; the same route-local rule applies symmetrically to every owner.
The two Advanced AE routes are independent, as are the two ExtendedAE routes.
Only loss of the shared exact AE2 core blocks all eight. Loss of S1 native
structural support blocks `extendedae-planes` but leaves the other seven
unchanged. A callback that has emitted partial M4/M5 output resets the tile
before invoking the captured original resource.

The unreleased AppMek profile adds only `appmek-drive-cells`. It requires the
exact AppMek+Mekanism tuple and active native Drive support. Ten AppMek IDs map
to five exact installed chassis models in `ae2:drive`; unknown, malformed,
inactive, or semantically mismatched selections restore the complete original
Drive. A thrown AppMek model callback restores that Drive and disables only
the AppMek route. Chemical P2P, Extended Drive, ME Chest, and MEGA Cell Dock
integration are outside this candidate. Schema-12 seam regressions exercise
the already-supported AE2 storage-bus and Mekanism pressurized-tube paths and
introduce no production renderer.

Each tile callback must continue for contained data, runtime and linkage
failures. BlueMap's
`MaxCapacityReachedException` remains
host render-task control flow and is rethrown without disabling the profile.
Raw syntactic NBT corruption encountered
before BlueMap's typed resolution remains host behavior as described above and
is outside this callback-containment guarantee.

Diagnostics are bounded by stable reason and contain no coordinates, NBT,
resource contents, file paths or player data. Unit tests may assert reason
codes, but runtime logs are not an evidence store.

PRBM itself retains geometry attributes and material ordinals, but no block
ID, mod ID, block-entity data or renderer identity. The evidence analyzer
therefore assigns a triangle to a gallery anchor using an inward-biased
geometric centroid: it moves the centroid a tiny distance opposite the
triangle winding normal, then floors the result to a Minecraft block cell.
This distinguishes a block's boundary face from the support block below, but
is explicitly a spatial inference rather than proof of the emitting renderer.
Schema 11 is the exact M4/M5 technical-review layer. It retains the accepted
schema-10 projection and appends eight extension cases/409 anchors. Its exact
221,769-byte main oracle and 2,336-byte legacy-upgrade oracle have SHA-256
`c2ce69bed949306551ca4ff6cdebf7fac88f0f2f2fa7ab294d3312f363e1b448`
and `2319ecf576ba07b123078c720d941990fac939033d375e5853f51bf98348c3c7`.
The isolated live-map reports now validate this layer. PRBM still cannot prove
renderer provenance or pixel fidelity, and no schema-11 visual acceptance is
claimed.

The accepted schema 10 embeds and verifies the accepted schema-9 view byte-
exactly, appends
28 S1 cases/360 anchors and separately reanalyzes ten retained schema-9
positions whose output is upgraded by S1. The 198,162-byte appended oracle
`ac9a54cee9a20be18e71d6c9fe4f16b894827d43bb49cb4d0e56c673280cec39`
locks 351 custom anchors, 37,518 triangles, 96 resources and 2,093 material
rows. The 6,155-byte legacy oracle
`cf0d86c440d1f89fc13f2b131f4f1534fb42363ebdc92580af826058297eb3d0`
locks ten anchors, 840 triangles, 21 resources and 70 rows. Their union is 370
positions, 361 custom anchors, 38,358 triangles, 96 resources and 2,163 rows.

Enabled schema 10 has 940 custom anchors/64,938 custom triangles, 16
zero-triangle fallbacks and 289 resources across 957 anchors; the stone
control yields 64,948 selected triangles. Native-structural-disabled retains
589 custom anchors/27,188 custom triangles, 17 fallbacks and 218 resources; its
appended predecessor projection is ten rendered/350 empty anchors with 608
triangles/14 resources, and all ten legacy upgrades are empty. Physical stock
selects 1,882 triangles across five resources and leaves 918 anchors empty,
including every appended and legacy-upgrade position. These static analyzer
contracts were also observed exactly in the 2026-08-11 isolated runtime
lifecycle. Initial and restored enabled cold/warm output was byte-identical at
report SHA-256
`14aa3b46386bead1f656f9796305c0000e835e5948ae06367d947a3afe837723`
and 46-file manifest SHA-256
`e1e592faabd263e1b9bacce14d56577f330d1b5cbd80336f2bd1563d3f1b2a78`.
The owner completed the bounded BlueMap visual comparison on 2026-08-11 for
the exact accepted S1 artifact set. This is not a production-readiness or
broader-compatibility claim.

Schema 9 embeds and verifies the exact accepted schema-8 view, then adds seven
M3f cases and 78 anchors. It checks persisted paint geometry; closed chest,
neutral crank and neutral Inscriber transforms; straight pylon roles and the
complete-component unformed projection for invalid L/T topology; exact
materials, UVs, winding, AO and host light; and 78 custom anchors with 2,822
triangles and no M3f fallback. Its 17 pylon anchors own 408 triangles.
M3-completion-disabled mode selects the exact stock projection at all 78
anchors. Physical stock renders 38 M3f anchors with 1,872 triangles and leaves
40 M3f anchors empty, while the
518 frozen legacy anchors stay empty and the stone control retains ten
triangles.

Schema 8 embeds and verifies the exact accepted schema-7 view, then adds three
M3e cases and 27 anchors for one complete formed bridge in each of the XZ, XY
and YZ planes. It checks all inferred link/corner/edge roles, exact
float-source bounds and UVs, winding, materials, white color,
neighbor-derived AO, host light and the `static-off-unknown` texture subset.
The M3e slice owns 1,188 custom triangles and four emitted resources.
`--quantum-disabled` requires those 27 anchors empty while reproducing the
accepted M0-M3d geometry. The physically absent stock contract requires only
the ten-triangle stone control and 518 empty anchors. The schema-7 projection
is frozen at SHA-256
`c60d2afff5a1f92da4972963fcb926c38093f43bb6d7f550799f104349728a38`.

Schema 7 embeds and verifies the exact accepted schema-6 view, then adds nine
M3d cases and 86 anchors. It validates visible-face topology, winding,
normals, UVs, materials, painted monitor tints, AO, powered fullbright
overlays and world-derived light. The fully enclosed center's zero geometry is
explicitly not renderer-provenance-distinguishable in PRBM. Mutually exclusive
crafting-disabled mode requires all 86 M3d anchors empty while reproducing the
accepted M0-M3c enabled totals. Java tests remain authoritative for all 64
crafting connection masks, malformed/compatible-neighbor fallback and
BlueMap light/cave/top-only/map-color behavior.

Schema 6 retains the exact accepted schema-5 M3b contract and adds the 47
M3c anchors. It validates face planes, winding-derived normals, position-seeded
asymmetric UVs, base/frame materials, all frame masks present in the fixture,
shared-face absence, white color, AO one and the vibrant blocklight-15 floor.
Ordinary blocklight and both variants' sunlight remain world-derived and must
be consistent across one face's base/frame triangles. Light stays in actual
attribute evidence but is excluded from topology and matched-variant
signatures. `--glass-disabled` requires all 47 M3c anchors empty while the
accepted M3b slice remains exact. Java tests, rather than the representative
gallery, are authoritative for all 64 masks and BlueMap cave/light behavior.

Schema 5 retains the exact accepted schema-4 M3a contract and additionally
validates Extended Drive orientation, twenty-slot front/rear mapping, occupied
models, face-local origins, mirror behavior and exact static LED attributes.
Its component-insensitivity comparison excludes ordinary model blocklight and
sunlight because BlueMap derives them from neighboring world context, while
retaining geometry, materials, UVs, colors and AO. Separate per-anchor checks
still require each LED to be black, fullbright, AO one and geometrically exact.
Extension-disabled mode requires all 36 M3b anchors to be empty while the M3a
regression stays exact. Schema 4 retains the schema-3 terminal checks and
additionally validates native Drive orientation, occupied chassis models,
slot origins and static LED attributes.

Schema 3 validates the declared terminal face/spin from boundary
planes and UV-up vectors, the three static tint layers, the stone facade ring,
the 278 custom-anchor contract and 11 zero-triangle atomic fallbacks. The first
M2 enabled cold/warm reports met that complete contract with identical
signatures, and the physically add-on-absent stock cold/warm reports met the
corresponding one-control/289-empty contract. These stored-map results remain
spatial evidence, not renderer-provenance proof.

## Shared-component boundary

No cross-repository production toolkit is extracted through S1. The
AE2/FramedBlocks comparison defines only neutral normalized-block-state,
material-outcome,
atomic-fallback and portable-test-vector specifications. AE2 persistence,
terminal/facade topology, slab/aperture geometry, connected-glass RNG/mask,
formed-crafting cube geometry and quantum multiblock inference stay
family-private. A
third independently implemented consumer is required before production shared
code can be reconsidered. See [ADR 0001](ADR-0001-M0-SHARED-EXTRACTION.md)
and [ADR 0002](ADR-0002-M2-FACADE-MATERIAL-BOUNDARY.md).
