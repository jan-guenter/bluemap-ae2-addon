/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.profile.advancedae;

import io.github.janguenter.bluemap.ae2.profile.ExactResourceManifest;

import java.util.Map;
import java.util.Set;

/** Independent exact Advanced AE quantum-alloy/Athena CTM route contract. */
public final class AdvancedAe1612AthenaProfile {

    public static final String PROFILE_ID = "advanced-ae-athena";
    public static final String BLOCK = AdvancedAe1612Catalog.QUANTUM_ALLOY_BLOCK;
    public static final String BLOCKSTATE =
            "assets/advanced_ae/blockstates/quantum_alloy_block.json";
    public static final String MODEL =
            "assets/advanced_ae/models/block/quantum_alloy_block.json";
    public static final String CONNECT_POLICY = "whole-blockstate-identity";
    public static final String ANIMATION_POLICY = "static-frame-zero";
    public static final String RESOURCE_MANIFEST_SHA256 =
            "feffb74dcbceeb483c9a52d028e6e464a4080e73b976deac310ddcf996387e62";
    public static final int RESOURCE_COUNT = 12;
    public static final int TEXTURE_COUNT = 5;
    public static final long RESOURCE_BYTES = 4_389L;

    private static final String RESOURCE_MANIFEST =
            "/bluemap-ae2/profiles/advancedae/1.6.12/athena-required-resources.tsv";
    private static final Set<String> EXPECTED_RESOURCES = Set.of(
            BLOCKSTATE,
            MODEL,
            texture("quantum_alloy_block.png"),
            texture("quantum_alloy_block.png.mcmeta"),
            texture("quantum_alloy_block_center.png"),
            texture("quantum_alloy_block_center.png.mcmeta"),
            texture("quantum_alloy_block_empty.png"),
            texture("quantum_alloy_block_empty.png.mcmeta"),
            texture("quantum_alloy_block_h.png"),
            texture("quantum_alloy_block_h.png.mcmeta"),
            texture("quantum_alloy_block_v.png"),
            texture("quantum_alloy_block_v.png.mcmeta")
    );
    private static final ExactResourceManifest.Data MANIFEST = ExactResourceManifest.load(
            AdvancedAe1612AthenaProfile.class,
            RESOURCE_MANIFEST,
            RESOURCE_MANIFEST_SHA256,
            EXPECTED_RESOURCES,
            RESOURCE_COUNT,
            RESOURCE_BYTES
    );

    private AdvancedAe1612AthenaProfile() {
    }

    public static Map<String, String> requiredResources() {
        return MANIFEST.digests();
    }

    public static Map<String, Long> requiredResourceSizes() {
        return MANIFEST.sizes();
    }

    public static boolean acceptsArtifacts(
            long advancedAeBytes,
            String advancedAeSha256,
            long athenaBytes,
            String athenaSha256
    ) {
        return AdvancedAe1612Profile.acceptsArtifact(advancedAeBytes, advancedAeSha256)
                && athenaBytes == Athena406ArtifactIdentity.JAR_BYTES
                && Athena406ArtifactIdentity.JAR_SHA256.equals(athenaSha256);
    }

    private static String texture(String filename) {
        return "assets/advanced_ae/textures/block/" + filename;
    }
}
