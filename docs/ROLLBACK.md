# Rollback

The add-on writes no required world, chunk or AE2 network state. Rollback is a
JVM lifecycle operation:

1. stop the server process completely;
2. remove or replace only the `bluemap-ae2-addon` JAR in BlueMap's packs
   directory;
3. leave the world, AE2 JAR and BlueMap data source unchanged;
4. restart the JVM;
5. render the separately named stock map and verify that the original AE2
   resource path is active.

`/bluemap reload` is not installation, disablement, upgrade or rollback:
BlueMap has no add-on unload lifecycle.

For a narrower emergency rollback, stop the JVM, add `ae2` to the documented
disabled-profile property/environment value, and restart. The exact add-on JAR
may remain installed, but its AE2 routes must stay inactive. Unknown values do
not act as compatibility overrides.

For an M3d-only rollback, disable profile ID `ae2-crafting` and restart. This
must leave the human-accepted M0-M3c routes active while every formed-crafting
anchor follows stock rendering. The exact independently retained
crafting-disabled cold/warm report and 44-file manifest SHA-256 values are
`a8e44c392c585df611f0c818767cba3a4ed20bd83e717ae7c1f3c9de09e9df0b`
and
`ec89cca1a43fdc2764ee667609ced69cec182d9d298072d219fdb567c9545e25`.

For an M3e-only rollback, disable profile ID `ae2-quantum-bridge` and
restart. This leaves the human-accepted M0-M3d routes active while all 27
quantum anchors follow stock rendering. The independently retained
quantum-disabled cold/warm report and 44-file manifest SHA-256 values are
`13c4ca3676868418f9f6797e5b4b4816a38d7ed2c518e1cbb329063ff005e20b`
and
`f26045142b4aa0df1f262abe95302e7341cb2ceca88fed60d2340d39269178b6`.
The physically add-on-absent stock control is the 522,872-byte report with
SHA-256
`0c3e4333782c244779a55201ab7a6362942e763c858bfdca2348b48204ce5e40`
and manifest SHA-256
`1cf7a62eb02a3cc6af5fb3b550c9aee40cfaebf521ededf41b2db95018f39c53`;
this exact M3e-era control contains only the ten-triangle stone control and
518 empty anchors.

For an M3f-only rollback, disable profile ID `ae2-m3-completion` and restart.
This retains the human-accepted M0-M3e routes while the 78 M3f anchors use
their exact original-resource results. The independently retained M3-
completion-disabled cold/warm report is byte-identical at 3,646,167 bytes with
SHA-256
`c9391aacb0fa7e268b67bc4723ffa8958593acbf06e529542c63fc7fa59ba707`;
its exact 44-file manifest SHA-256 is
`34bad6b250e18217485d68dfb989cfbc6d9b53f480be71c534f91675dc3c6a3e`.
The route-disabled result renders stock models at 38 M3f anchors and leaves 40
M3f anchors empty while retaining the accepted M0-M3e custom slice.

The current physically add-on-absent M3f stock control is the byte-identical
614,214-byte cold/warm report with SHA-256
`30439e78593dacc43a0d3822f039ebbbd058d83be64023650471dab07179c4bc`
and exact 44-file manifest SHA-256
`b66f1e00f658d8fbd6ed842aae614b5db7bc94580215b2ad22a2a769e4d73efa`.
It renders 38 M3f anchors and leaves 40 M3f anchors empty. Together with the
ten-triangle stone control, stock owns 1,882 triangles across five resources;
the other 558 anchors are empty.

For an S1-only route rollback, disable profile ID
`ae2-cable-bus-structural` and restart. The schema-10 analyzer contract must
then retain the accepted M0-M3f routes and the predecessor result: 589 custom
anchors, 27,188 custom triangles, 218 resources, 17 fallbacks and 27,198
selected triangles. Exactly ten of the 360 appended anchors render the
608-triangle/14-resource predecessor projection, the other 350 appended
anchors are empty and all ten legacy-upgrade positions are empty. A physically
add-on-absent schema-10 stock result must select 1,882 triangles across five
resources and leave 918 anchors empty, including all 360 appended and all ten
legacy-upgrade positions.

The 2026-08-11 All the Mons 1.2.0 lifecycle demonstrated both S1 rollback
paths. Native-structural-disabled cold/warm produced the exact 4,289,919-byte
report SHA-256
`63e528d6aa3c033cd6b2251f7a569cc0e4e7dc4bfba81d75862f8fa7a416e274`
and 46-file manifest SHA-256
`7cb0b48aa938109d8c001d32f01375aba7f29f7f9b8c5f96a927b52335a7df03`.
Physically add-on-absent stock cold/warm produced the exact 993,266-byte report
SHA-256
`a1e148fde5af118def7e379c44a2294ca5d9485a60824ad2764911f1805788a4`
and 46-file manifest SHA-256
`9539d160733a7616ec9092e82086539b4db725e0afab2b04f5111a77e0da66f0`.
Restoring the exact 855,833-byte candidate JAR
`5dad1cf654c13b5b0aa5411264104ff2f17b942b7d4c5def698d24c476951c39`
then reproduced initial enabled cold/warm output exactly, at report SHA-256
`14aa3b46386bead1f656f9796305c0000e835e5948ae06367d947a3afe837723`
and 46-file manifest SHA-256
`e1e592faabd263e1b9bacce14d56577f330d1b5cbd80336f2bd1563d3f1b2a78`.
The fixture remained at one M3f build, one S1 build, two stable checks and zero
failures throughout, and the lab was saved and scaled to zero after capture.

The owner visually accepted the exact S1 JAR, gallery and map archive in
BlueMap on 2026-08-11. Candidate aliases `ae2-addon-s1-candidate.jar`,
`ae2-s1-gallery-candidate.zip` and
`ae2-s1-enabled-candidate-map-2026-08-11.tar.gz` remain hash-exact to accepted
aliases `ae2-addon-s1-accepted.jar`, `ae2-s1-gallery-accepted.zip` and
`ae2-s1-enabled-accepted-map-2026-08-11.tar.gz` on `data-atm120`; the
historical PVC was untouched. S1 is therefore the latest exact accepted local
rollback/checkpoint for the All the Mons 1.2.0 tuple. M3f remains the preceding
historical All the Mons 1.1.1 / NeoForge 21.1.234 accepted checkpoint, not a
current-pack compatibility claim.

The isolated release gate must then restore the exact tested JAR and
enabled configuration with another full restart and compare the re-enabled
render to the exact applicable baseline. For M3d that is the human-accepted
2026-08-08 baseline bound to the exact JAR, gallery and map-archive identities
recorded in `STAGING.md`.
For M3e technical restoration, the unchanged candidate JAR and enabled
configuration reproduced the initial 3,519,405-byte report and its 44-file
manifest exactly, at SHA-256
`d306aff2a0f2eca2882a3d52426140b8caf98aeda76d0722dd14b59ea8a5e9e9`
and
`b2d61c1dea7bb10dbfbb07a62fd27d1a1a380ca2fbf1b49e06a9f4f232400ca5`.
This cleared the M3e technical rollback path. The owner subsequently accepted
the exact M3e JAR, gallery and archive on 2026-08-08; candidate aliases remain
byte-identical to `ae2-addon-m3e-accepted.jar`,
`ae2-m3e-gallery-accepted.zip` and
`ae2-m3e-enabled-accepted-map-2026-08-08.tar.gz`. M3e was the current
acceptance baseline at that checkpoint and M3d remained its previous accepted
rollback checkpoint.

The earlier M3f candidate was withdrawn after owner visual review exposed
invisible members in the L/T pylon arrangements. It was never accepted and is
not a rollback target. The corrected artifact below supersedes it and was
visually accepted on 2026-08-09.

For accepted M3f restoration, restore the exact 623,591-byte JAR
with SHA-256
`ca67c0fc433e43f8e0801ed8d2cccfe47aae317fbc329c099bc8cd741ec3b42b`
and the exact 49,679-byte schema-9 gallery with SHA-256
`21ceec072cc3263a41bdb81874e897d48d5a1ce5e1c7d3ac3c0de3063818ee6c`,
then restart without rebuilding the fixture. The zero-restart restored pod
reproduced the initial enabled 3,783,797-byte report and its 44-file manifest
exactly, at SHA-256
`7022a33448dab364cb825a8d67359795560b6a8793b64544ccb0b4c1fda7484e`
and
`259af4eea91a32acc07d1572e8f3f42e6276b46999496d0380ec009c10970fd8`.
Packaging the restored files twice produced byte-identical 20,450,880-byte,
44-entry archives with SHA-256
`e66abf203481c5df0fa0fc0062c414876f9ef6428cd637de0795f821496c51a9`.
The M3f candidate aliases are `ae2-addon-m3f-candidate.jar`,
`ae2-m3f-gallery-candidate.zip` and
`ae2-m3f-enabled-candidate-map-2026-08-09.tar.gz`. The hash-exact accepted
aliases are `ae2-addon-m3f-accepted.jar`, `ae2-m3f-gallery-accepted.zip` and
`ae2-m3f-enabled-accepted-map-2026-08-09.tar.gz`. M3e's candidate and accepted
JAR, gallery and map-archive aliases remain unchanged. The exact corrected
M3f JAR, gallery and map archive were visually accepted on 2026-08-09,
completing M3. At that checkpoint M3f was the acceptance baseline and M3e was
its previous accepted rollback checkpoint. S1 subsequently passed its All the
Mons 1.2.0 rollback lifecycle and bounded owner visual review and is now the
latest exact accepted local rollback/checkpoint. It remains unreleased.
A successful staging lifecycle is not a production deployment recommendation.
