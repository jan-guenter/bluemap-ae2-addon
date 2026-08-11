# ADR 0002: M2 facade/material boundary and shared extraction

Status: accepted as a design and provenance decision on 2026-08-07. This ADR
does not claim that M2 implementation, runtime validation, visual comparison,
or human acceptance is complete.

## Context

ADR 0001 deferred shared production code until AE2 supplied a second concrete
persisted-block-state and material-resolution case. The exact AE2 19.2.17 M2
audit now supplies that comparison:

- FramedBlocks retains camouflage block state and substitutes its material
  onto family-owned captured geometry;
- AE2 retains a facade `BlockState.CODEC` value and clamps that material's
  quads into family-owned facade slabs and part openings;
- both must classify an effective BlueMap resource graph before using an
  opaque static material, preserve a named fidelity boundary for weighted
  stone variants, and fall back without aborting BlueMap;
- their persisted schemas, geometry algorithms, topology, licensing history,
  and fallback consequences remain materially different.

The comparison uses exact AE2 19.2.17 runtime artifact SHA-256
`460d779a0609b81409907d9956de8f6f70a1b0912257e3e5c3c7e75ac9630e95`,
exact source commit `79ee2c704ad62941a426c26b1cb1f76ef5b2ee5a`, the official
Minecraft 1.21.1 client-resource artifact identified in `PROVENANCE.md`, and
the exact BlueMap 5.22 backport. Machine-readable source, class, and resource
identities are recorded in `provenance/upstreams.json`.

## Decision

Extract no shared production source, separately versioned runtime library, or
installed provider after M2. Two implementations are enough to define a
neutral specification and portable test-vector boundary, but not enough to
demonstrate that a production API will remain stable for a third mod family.

The following concepts are neutral and may be documented or implemented in an
independently authored, development-only test kit:

1. **Normalized block state**
   - a validated namespace and path;
   - a deterministic, duplicate-free property map ordered by property name;
   - explicit distinction between absent properties, an empty property
     compound, malformed input, and a syntactically valid unsupported state;
   - profile-owned size, character, namespace, and property allowlists.
2. **Material-resolution outcome**
   - either one resolved material descriptor or one bounded rejection reason;
   - texture key, opacity, animation, tint, emission, face-uniformity, and
     full-cube/model-structure facts needed by the caller's accepted lane;
   - explicit classification of single static and uniform-material weighted
     alternatives;
   - an explicit fidelity field when geometry/material are reproducible but a
     client-only randomized UV orientation is not persisted.
3. **Atomic fallback contract**
   - do not let malformed or unsupported data escape the tile callback;
   - discard partial custom output before invoking the family-owned direct
     original-resource delegate;
   - use bounded, location-free reason categories;
   - keep the decision about whole-block versus optional-component omission in
     the owning family profile.
4. **Portable test vectors**
   - small synthetic resource graphs and normalized block-state inputs;
   - expected supported/rejected classification and bounded reason;
   - expected material descriptor and fidelity level when supported;
   - deterministic canonical serialization and digests;
   - no candidate-mod classes, source, assets, captured meshes, or executable
     client rendering code.

The neutral specification does not require the two add-ons to share Java
types, package names, logging implementations, BlueNBT deserializers, caches,
or BlueMap adapter code. Each repository remains independently buildable,
deployable, removable, and releasable.

## Compared boundary

| Concern | FramedBlocks | AE2 M2 | Neutral boundary |
| --- | --- | --- | --- |
| Persisted value | Candidate-specific camouflage NBT | Six root `facade<Direction>` values encoded by Minecraft `BlockState.CODEC` | Validated block ID plus deterministic properties and explicit malformed/unsupported outcomes |
| Initial material lane | Static full cube or bounded weighted alternatives collapsing to one opaque, non-animated material | Exact canonical `{Name:"minecraft:stone"}` with no properties, plus an exact effective stone resource proof | Static opaque material descriptor; callers retain narrower allowlists |
| Geometry consumer | Captured FramedBlocks template faces | AE2 facade slab/ring generation around part collision boxes | Material result only; no shared candidate geometry |
| Weighted stone | Normalized material/UV-orientation approximation because client cache choice is not persisted | Same limitation if AE2 facade stone uses the weighted vanilla blockstate | Named `geometry-and-material`, not `pixel-identical-random-orientation`, fidelity |
| Failure | Direct original FramedBlocks resource after clearing custom output | Whole cable-bus original resource for a core facade failure after clearing cable, part, and facade output | Atomic reset and bounded reason; delegate and component policy remain family-owned |
| Evidence | Exact FramedBlocks artifact/source and Minecraft resource graph | Exact AE2 artifact/source/class/resource evidence and the same Minecraft graph | Hash-addressed test metadata, never redistributed candidate assets |

AE2 deliberately remains stricter than the reusable material vocabulary. M2's
supported facade state is exactly `minecraft:stone` with no `Properties` or
unknown fields. A generic material resolver must not silently broaden that
profile to glass, slabs, stairs, dynamic models, block entities, multipart
models, mod namespaces, or property-bearing states.

## Family-private implementation

The following AE2 behavior is not eligible for neutral extraction:

- `facadeDown` through `facadeEast` persistence and AE2's decode leniency;
- `THIN_THICKNESS`, direction transforms, facade-local coordinates, and
  reporting-part collision boxes;
- intersecting-box union, four-strip aperture construction, vertex clamping,
  UV/color reinterpolation, face stripping, and corner kicking;
- short cable-anchor stilts for facade sides without an attachment;
- center-cable, attachment, facade, neighbor-appearance, and depth-occlusion
  ordering;
- multi-part and multi-facade policy;
- AE2 resource identifiers and exact-version activation rules.

These rules are informed by AE2 LGPL-3.0-or-later source and by the vendored
CodeChickenLib quad-transform sources marked LGPL-2.1-or-later. Any adapted
implementation stays in this LGPL-3.0-only family repository with its exact
provenance. It must not be moved into a permissive neutral toolkit.

FramedBlocks' NBT decoder, captured geometry, modifier/model-data rules,
camouflage-aware neighbor behavior, and family routing likewise remain in its
own LGPL repository.

## Test-vector contract

A later development-only test kit may define a versioned data schema after
both repositories have executable M2 vectors. Its minimum cases should be:

- canonical plain stone and property ordering;
- absent, empty, duplicate, wrong-type, oversized, and unsupported properties;
- missing resources and unresolved parent or texture references;
- canonical full cube, multipart, non-full-cube, translucent, animated,
  emissive, tinted, materially directional, and dynamic-model cases;
- weighted alternatives that either collapse to one material or disagree;
- exact distinction between supported material, syntactically unsupported
  state, malformed data, and resource drift;
- atomic reset/original-delegate behavior tested through a family-supplied
  harness rather than a shared runtime adapter.

Fixtures must be independently authored and either purely numeric/structural
or project-owned synthetic resources. Exact third-party hashes and resource
keys may be metadata; third-party model, texture, source, JAR, or captured mesh
bytes may not be copied into the kit.

## Production promotion gate

Shared production code remains deferred until a third consumer demonstrates
the same boundary. A later ADR must prove all of the following before a source
module or installed provider is created:

1. three independently implemented family profiles consume the proposed
   contract without candidate-specific branches in the neutral API;
2. portable vectors run unchanged in all three repositories;
3. the shared implementation is independently authored and has a reviewed
   license/provenance lane free of candidate-derived algorithms or assets;
4. versioning does not force unrelated family releases for candidate updates;
5. classloader, BlueMap ABI, registry, failure-isolation, removal, and rollback
   behavior is tested with and without the provider;
6. an installed dependency is optional or its operational benefit clearly
   outweighs the additional deployment and upgrade coupling.

Until then, duplication of small normalization and material-classification
code is accepted. Specifications and test vectors are the integration seam.

## Consequences

- AE2 clipping and topology can evolve without releasing FramedBlocks or a
  shared runtime.
- Both add-ons can converge on reason names, fidelity vocabulary, and
  deterministic vectors without sharing candidate-derived code.
- The strict exact-version profiles remain free to reject inputs that a
  neutral descriptor could theoretically represent.
- Documentation of this boundary is not evidence that an M2 JAR has built,
  started, rendered, passed lifecycle tests, or received visual acceptance.
