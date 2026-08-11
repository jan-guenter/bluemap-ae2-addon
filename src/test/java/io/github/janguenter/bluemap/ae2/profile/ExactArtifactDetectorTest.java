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
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExactArtifactDetectorTest {

    private static final String AE2_DESCRIPTOR = """
            modLoader="javafml"
            [[mods]]
            modId="ae2"
            version="19.2.17"
            """;

    @TempDir
    Path temporaryDirectory;

    @Test
    void detectsOneExactArtifactWithoutDependingOnItsFilename() throws Exception {
        Path jar = writeJar("renamed-resource-root.jar", AE2_DESCRIPTOR);
        ExactArtifactDetector detector = exactDetectorFor(jar);

        ExactArtifactDetector.Detection detection = detector.detect(List.of(jar));

        assertTrue(detection.exact());
        assertEquals(Ae219217Profile.EXACT_REASON, detection.reason());
    }

    @Test
    void reportsAbsentArtifact() throws Exception {
        Path ordinary = temporaryDirectory.resolve("ordinary.txt");
        Files.writeString(ordinary, "not a jar", StandardCharsets.UTF_8);

        ExactArtifactDetector.Detection detection = new ExactArtifactDetector()
                .detect(List.of(ordinary));

        assertFalse(detection.exact());
        assertEquals("ae2-artifact-not-found", detection.reason());
    }

    @Test
    void rejectsWrongSizeOrDigestAfterDescriptorMatch() throws Exception {
        Path jar = writeJar("ae2.jar", AE2_DESCRIPTOR);
        ExactArtifactDetector detector = new ExactArtifactDetector("0".repeat(64), Files.size(jar));

        ExactArtifactDetector.Detection detection = detector.detect(List.of(jar));

        assertFalse(detection.exact());
        assertEquals("unsupported-ae2-artifact", detection.reason());
    }

    @Test
    void rejectsTwoDistinctAe2ArtifactsBeforeSelectingEither() throws Exception {
        Path first = writeJar("first.jar", AE2_DESCRIPTOR);
        Path second = writeJar("second.jar", AE2_DESCRIPTOR + "\ndescription=\"second\"\n");

        ExactArtifactDetector.Detection detection = exactDetectorFor(first)
                .detect(List.of(first, second));

        assertFalse(detection.exact());
        assertEquals("multiple-ae2-artifacts", detection.reason());
    }

    @Test
    void repeatedReferenceToTheSameRealJarIsDeduplicated() throws Exception {
        Path jar = writeJar("ae2.jar", AE2_DESCRIPTOR);

        ExactArtifactDetector.Detection detection = exactDetectorFor(jar)
                .detect(List.of(jar, jar, jar));

        assertTrue(detection.exact());
    }

    @Test
    void filenameAloneNeverSubstitutesForTheModDescriptor() throws Exception {
        Path decoy = writeJar(
                "appliedenergistics2-19.2.17.jar",
                "[[mods]]\nmodId=\"not_ae2\"\n"
        );

        ExactArtifactDetector.Detection detection = new ExactArtifactDetector()
                .detect(List.of(decoy));

        assertFalse(detection.exact());
        assertEquals("ae2-artifact-not-found", detection.reason());
    }

    @Test
    void commentsAndMultilineStringsCannotForgeAnAe2Declaration() throws Exception {
        String descriptor = "[[mods]]\n"
                + "modId=\"decoy\"\n"
                + "description=\"\"\"\n"
                + "[[mods]]\n"
                + "modId=\"ae2\"\n"
                + "\"\"\"\n"
                + "# [[mods]]\n"
                + "# modId=\"ae2\"\n";
        Path decoy = writeJar("decoy.jar", descriptor);

        ExactArtifactDetector.Detection detection = new ExactArtifactDetector()
                .detect(List.of(decoy));

        assertFalse(detection.exact());
        assertEquals("ae2-artifact-not-found", detection.reason());
    }

    @Test
    void quotedTomlNamesAndTrailingCommentsAreAccepted() throws Exception {
        String descriptor = """
                [["mods"]]
                "modId" = "ae2" # exact declaration
                """;
        Path jar = writeJar("quoted.jar", descriptor);

        ExactArtifactDetector.Detection detection = exactDetectorFor(jar)
                .detect(List.of(jar));

        assertTrue(detection.exact());
    }

    @Test
    void rootEnumerationAndInterruptsAreStrictlyBounded() {
        Path missing = temporaryDirectory.resolve("missing.jar");
        assertThrows(
                IOException.class,
                () -> new ExactArtifactDetector().detect(
                        Collections.nCopies(4_097, missing)
                )
        );

        try {
            Thread.currentThread().interrupt();
            assertThrows(
                    InterruptedException.class,
                    () -> new ExactArtifactDetector().detect(List.of(missing))
            );
        } finally {
            Thread.interrupted();
        }
    }

    @Test
    void constructorRejectsAmbiguousExpectedIdentities() {
        assertThrows(NullPointerException.class, () -> new ExactArtifactDetector(null, 1));
        assertThrows(IllegalArgumentException.class, () -> new ExactArtifactDetector("abc", 1));
        assertThrows(
                IllegalArgumentException.class,
                () -> new ExactArtifactDetector("0".repeat(64), -1)
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> new ExactArtifactDetector.Detection(true, "unsupported-ae2-artifact")
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> new ExactArtifactDetector.Detection(false, Ae219217Profile.EXACT_REASON)
        );
    }

    private ExactArtifactDetector exactDetectorFor(Path jar) throws IOException {
        return new ExactArtifactDetector(
                ExactArtifactDetector.sha256(jar),
                Files.size(jar)
        );
    }

    private Path writeJar(String name, String descriptor) throws IOException {
        Path jar = temporaryDirectory.resolve(name);
        try (ZipOutputStream output = new ZipOutputStream(Files.newOutputStream(jar))) {
            output.putNextEntry(new ZipEntry("META-INF/neoforge.mods.toml"));
            output.write(descriptor.getBytes(StandardCharsets.UTF_8));
            output.closeEntry();
            output.putNextEntry(new ZipEntry("marker.txt"));
            output.write(name.getBytes(StandardCharsets.UTF_8));
            output.closeEntry();
        }
        return jar;
    }
}
