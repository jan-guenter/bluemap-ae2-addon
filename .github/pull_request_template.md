## Summary

Describe the behavior and exact family profile affected.

## Validation

- [ ] `gallery/generate.py --check` and `gallery/SHA256SUMS` pass.
- [ ] `clean check build` passes on Java 21.
- [ ] POM and Gradle module metadata generate and the semantic POM gate passes.
- [ ] The exact AE2 19.2.17 artifact gate passes, when applicable.
- [ ] Runtime or visual claims below were actually observed and evidence was retained.

## Compatibility and provenance

- [ ] Unknown or unsupported input still fails closed to the original resource.
- [ ] Profile failure is isolated and operator disablement remains restart-based.
- [ ] No third-party JARs/assets, captures, worlds, credentials, logs or build output are committed.
- [ ] Provenance, notices, coverage, rollback and changelog documentation are updated where required.

## Observed evidence and open limitations

List what was observed and what remains untested. A structural fixture or
technical capture is not human visual acceptance.
