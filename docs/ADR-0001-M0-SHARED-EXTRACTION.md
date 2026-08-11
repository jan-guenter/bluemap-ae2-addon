# ADR 0001: no production shared-component extraction in AE2 M0

Status: accepted for M0 on 2026-08-03. Revisited and continued by
[`ADR 0002`](ADR-0002-M2-FACADE-MATERIAL-BOUNDARY.md) on 2026-08-07.

## Context

FramedBlocks and AE2 M0 both need exact-version gates, bounded persisted-data
decoding, deterministic meshes, stock fallback, isolated lifecycle fixtures
and evidence analyzers. Moving those similarities into a runtime library after
only two profiles would create an installed dependency before the stable
cross-family boundary is known.

AE2 M0 also has family-specific semantics: cable-bus NBT retention,
same-center-ID topology and AE2 texture/resource interpretation. Those do not
belong in a neutral shared runtime.

## Decision

M0 reuses specifications and development patterns, but extracts no production
code or installed runtime. Build/release conventions, fixture contracts,
bounded-diagnostic expectations and analyzer test patterns may be propagated
between repositories without coupling their artifacts.

The decision is reconsidered after the exact AE2 M2 facade/material audit. That
audit provides a concrete comparison with FramedBlocks camo/material
resolution and enough evidence to define a neutral specification and test-
vector boundary. It does not yet demonstrate a stable production runtime API.

ADR 0002 records the result: independently authored development-only test
vectors may be extracted after executable vectors exist in both family
repositories, but shared production source and an installed provider remain
deferred until a third consumer passes the promotion gate. This reconsideration
does not claim M2 implementation, runtime validation, or acceptance.

## Consequences

- The AE2 family remains independently releasable and does not update when an
  unrelated mod-family renderer changes.
- Candidate-derived decoding and resource semantics stay inside their owning
  family and license boundary.
- Small duplication is accepted until a third proven consumer and two
  independent implementations demonstrate a stable shared contract.
- Any future extraction requires its own ADR, license/provenance audit,
  deterministic compatibility tests and a no-mandatory-runtime-dependency
  deployment review.
