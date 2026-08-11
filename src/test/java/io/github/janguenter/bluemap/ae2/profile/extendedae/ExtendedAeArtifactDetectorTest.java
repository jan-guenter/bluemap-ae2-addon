/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.profile.extendedae;

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

class ExtendedAeArtifactDetectorTest {

    private static final String EXTENDEDAE_DESCRIPTOR = """
            modLoader="javafml"
            [[mods]]
            modId="extendedae"
            version="1.21-2.2.33-neoforge"
            """;

    @TempDir
    Path temporaryDirectory;

    @Test
    void detectsOneExactArtifactWithoutDependingOnItsFilename() throws Exception {
        Path jar = writeJar("renamed-resource-root.jar", EXTENDEDAE_DESCRIPTOR);
        ExtendedAeArtifactDetector.Detection detection = exactDetectorFor(jar)
                .detect(List.of(jar));

        assertTrue(detection.exact());
        assertEquals(ExtendedAe2233Profile.EXACT_REASON, detection.reason());
    }

    @Test
    void acceptsTheProvenResourceEquivalent2235Identity() throws Exception {
        Path jar = writeJar(
                "ExtendedAE-1.21-2.2.35-neoforge.jar",
                EXTENDEDAE_DESCRIPTOR.replace("2.2.33", "2.2.35")
        );
        ExtendedAeArtifactDetector.Detection detection =
                new ExtendedAeArtifactDetector(
                        ExtendedAeArtifactDetector.sha256(jar),
                        Files.size(jar),
                        ExtendedAe2235ArtifactIdentity.EXACT_REASON
                ).detect(List.of(jar));

        assertTrue(detection.exact());
        assertEquals(ExtendedAe2235ArtifactIdentity.EXACT_REASON, detection.reason());
    }

    @Test
    void reportsAbsentAndUnsupportedArtifacts() throws Exception {
        Path ordinary = temporaryDirectory.resolve("ordinary.txt");
        Files.writeString(ordinary, "not a jar", StandardCharsets.UTF_8);
        ExtendedAeArtifactDetector.Detection absent = new ExtendedAeArtifactDetector()
                .detect(List.of(ordinary));
        assertFalse(absent.exact());
        assertEquals("extendedae-artifact-not-found", absent.reason());

        Path jar = writeJar("extendedae.jar", EXTENDEDAE_DESCRIPTOR);
        ExtendedAeArtifactDetector.Detection unsupported =
                new ExtendedAeArtifactDetector("0".repeat(64), Files.size(jar))
                        .detect(List.of(jar));
        assertFalse(unsupported.exact());
        assertEquals("unsupported-extendedae-artifact", unsupported.reason());
    }

    @Test
    void rejectsMultipleDistinctArtifactsAndDeduplicatesOneRealPath() throws Exception {
        Path first = writeJar("first.jar", EXTENDEDAE_DESCRIPTOR);
        Path second = writeJar(
                "second.jar",
                EXTENDEDAE_DESCRIPTOR + "\ndescription=\"second\"\n"
        );
        ExtendedAeArtifactDetector.Detection multiple = exactDetectorFor(first)
                .detect(List.of(first, second));
        assertFalse(multiple.exact());
        assertEquals("multiple-extendedae-artifacts", multiple.reason());

        assertTrue(exactDetectorFor(first).detect(List.of(first, first, first)).exact());
    }

    @Test
    void filenameCommentsAndMultilineStringsCannotForgeDeclaration() throws Exception {
        String descriptor = "[[mods]]\n"
                + "modId=\"decoy\"\n"
                + "description=\"\"\"\n"
                + "[[mods]]\n"
                + "modId=\"extendedae\"\n"
                + "\"\"\"\n"
                + "# [[mods]]\n"
                + "# modId=\"extendedae\"\n";
        Path decoy = writeJar("ExtendedAE-1.21-2.2.33-neoforge.jar", descriptor);
        ExtendedAeArtifactDetector.Detection detection = new ExtendedAeArtifactDetector()
                .detect(List.of(decoy));

        assertFalse(detection.exact());
        assertEquals("extendedae-artifact-not-found", detection.reason());
    }

    @Test
    void quotedTomlNamesAreAccepted() throws Exception {
        Path jar = writeJar(
                "quoted.jar",
                "[[\"mods\"]]\n\"modId\" = \"extendedae\" # exact declaration\n"
        );
        assertTrue(exactDetectorFor(jar).detect(List.of(jar)).exact());
    }

    @Test
    void rootEnumerationInterruptsAndConstructorsAreStrict() {
        Path missing = temporaryDirectory.resolve("missing.jar");
        assertThrows(
                IOException.class,
                () -> new ExtendedAeArtifactDetector().detect(
                        Collections.nCopies(4_097, missing)
                )
        );

        try {
            Thread.currentThread().interrupt();
            assertThrows(
                    InterruptedException.class,
                    () -> new ExtendedAeArtifactDetector().detect(List.of(missing))
            );
        } finally {
            Thread.interrupted();
        }

        assertThrows(
                NullPointerException.class,
                () -> new ExtendedAeArtifactDetector(null, 1)
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> new ExtendedAeArtifactDetector("abc", 1)
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> new ExtendedAeArtifactDetector("0".repeat(64), -1)
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> new ExtendedAeArtifactDetector.Detection(
                        true,
                        "unsupported-extendedae-artifact"
                )
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> new ExtendedAeArtifactDetector.Detection(
                        false,
                        ExtendedAe2233Profile.EXACT_REASON
                )
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> new ExtendedAeArtifactDetector(
                        "0".repeat(64),
                        1,
                        "exact-unreviewed-version"
                )
        );
    }

    private ExtendedAeArtifactDetector exactDetectorFor(Path jar) throws IOException {
        return new ExtendedAeArtifactDetector(
                ExtendedAeArtifactDetector.sha256(jar),
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
