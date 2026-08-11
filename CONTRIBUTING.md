# Contributing

Read `AGENTS.md` and the documents under `docs/` before changing this exact-
version renderer. Preserve the plain BlueMap add-on boundary, fail-closed
activation and direct original-resource fallback.

Run the full validation contract in `AGENTS.md`. Never commit third-party
JARs, assets, source archives, worlds, screenshots, credentials or generated
runtime output. Every behavior change needs a bounded test and an updated
provenance record when its source-use inputs change.

Every version increase after the initial public snapshot must enter `main`
through a pull request. Release operations are documented in
`docs/RELEASING.md`.
