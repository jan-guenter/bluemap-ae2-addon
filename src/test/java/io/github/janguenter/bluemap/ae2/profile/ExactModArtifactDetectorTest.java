/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.profile;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExactModArtifactDetectorTest {

    @TempDir
    Path temporary;

    @Test
    void detectsOnlyTheExactDeclaredArtifact() throws Exception {
        Path jar = jar("expandedae", "payload");
        ExactModArtifactDetector detector = detector("expandedae", jar);

        ExactModArtifactDetector.Detection detection = detector.detect(java.util.List.of(jar));

        assertTrue(detection.exact());
        assertEquals("exact-2.1.1", detection.reason());
    }

    @Test
    void ignoresOtherModsAndRejectsDuplicatesAndMutations() throws Exception {
        Path exact = jar("megacells", "payload");
        ExactModArtifactDetector detector = detector("megacells", exact);
        Path other = jar("othermod", "payload");
        assertTrue(detector.detect(java.util.List.of(other, exact)).exact());

        Path duplicate = temporary.resolve("duplicate.jar");
        Files.copy(exact, duplicate);
        assertEquals(
                ExactModArtifactDetector.Failure.MULTIPLE_ARTIFACTS,
                detector.detect(java.util.List.of(exact, duplicate)).failure()
        );

        Path mutated = jar("megacells", "changed");
        assertEquals(
                ExactModArtifactDetector.Failure.MISMATCH,
                detector.detect(java.util.List.of(mutated)).failure()
        );
        assertFalse(detector.detect(java.util.List.of(other)).exact());
    }

    private ExactModArtifactDetector detector(String modId, Path jar) throws IOException {
        return new ExactModArtifactDetector(new ExactModArtifactDetector.Identity(
                modId,
                "2.1.1",
                Files.size(jar),
                sha256(jar),
                "exact-2.1.1"
        ));
    }

    private Path jar(String modId, String payload) throws IOException {
        Path jar = temporary.resolve(modId + "-" + payload + ".jar");
        try (ZipOutputStream output = new ZipOutputStream(Files.newOutputStream(jar))) {
            output.putNextEntry(new ZipEntry("META-INF/neoforge.mods.toml"));
            output.write(("[[mods]]\nmodId=\"" + modId + "\"\n")
                    .getBytes(StandardCharsets.UTF_8));
            output.closeEntry();
            output.putNextEntry(new ZipEntry("payload.txt"));
            output.write(payload.getBytes(StandardCharsets.UTF_8));
            output.closeEntry();
        }
        return jar;
    }

    private static String sha256(Path path) throws IOException {
        try {
            return HexFormat.of().formatHex(
                    MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(path))
            );
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(exception);
        }
    }
}
