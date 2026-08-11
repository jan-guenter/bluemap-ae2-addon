/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.profile;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

/** Strict loader for additive, evidence-locked extension resource partitions. */
public final class ExactResourceManifest {

    private static final Pattern RESOURCE_PATH = Pattern.compile(
            "assets/[a-z0-9_.-]+/[a-z0-9_./-]+"
    );
    private static final Pattern SIZE = Pattern.compile("[1-9][0-9]*");
    private static final Pattern SHA256 = Pattern.compile("[0-9a-f]{64}");

    private ExactResourceManifest() {
    }

    /** Loads and completely validates one exact resource manifest. */
    public static Data load(
            Class<?> anchor,
            String resourcePath,
            String expectedManifestSha256,
            Set<String> expectedPaths,
            int expectedCount,
            long expectedTotalBytes
    ) {
        Objects.requireNonNull(anchor, "anchor");
        Objects.requireNonNull(resourcePath, "resourcePath");
        Objects.requireNonNull(expectedManifestSha256, "expectedManifestSha256");
        Objects.requireNonNull(expectedPaths, "expectedPaths");
        if (!resourcePath.startsWith("/")
                || !SHA256.matcher(expectedManifestSha256).matches()
                || expectedCount < 1
                || expectedTotalBytes < 1
                || expectedPaths.size() != expectedCount) {
            throw new IllegalArgumentException("invalid exact resource manifest contract");
        }

        byte[] raw = read(anchor, resourcePath);
        requireDigest(raw, expectedManifestSha256);
        Data data = parse(raw);
        if (data.digests().size() != expectedCount
                || data.sizes().size() != expectedCount
                || !data.digests().keySet().equals(expectedPaths)
                || !data.sizes().keySet().equals(expectedPaths)
                || data.sizes().values().stream().mapToLong(Long::longValue).sum()
                != expectedTotalBytes) {
            throw new IllegalStateException("invalid exact resource closure");
        }
        return data;
    }

    private static byte[] read(Class<?> anchor, String resourcePath) {
        try (InputStream input = anchor.getResourceAsStream(resourcePath)) {
            if (input == null) {
                throw new IllegalStateException("missing exact resource manifest");
            }
            return input.readAllBytes();
        } catch (IOException exception) {
            throw new IllegalStateException("failed to read exact resource manifest", exception);
        }
    }

    private static Data parse(byte[] raw) {
        Map<String, String> digests = new LinkedHashMap<>();
        Map<String, Long> sizes = new LinkedHashMap<>();
        String previousPath = null;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                new ByteArrayInputStream(raw),
                StandardCharsets.UTF_8
        ))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    throw new IllegalStateException("blank exact resource manifest row");
                }
                String[] fields = line.split("\\t", -1);
                if (fields.length != 3
                        || !RESOURCE_PATH.matcher(fields[0]).matches()
                        || !SIZE.matcher(fields[1]).matches()
                        || !SHA256.matcher(fields[2]).matches()) {
                    throw new IllegalStateException("malformed exact resource manifest row");
                }
                String path = fields[0];
                if (previousPath != null && previousPath.compareTo(path) >= 0) {
                    throw new IllegalStateException("unsorted exact resource manifest");
                }
                long size;
                try {
                    size = Long.parseLong(fields[1]);
                } catch (NumberFormatException exception) {
                    throw new IllegalStateException("invalid exact resource size", exception);
                }
                if (digests.put(path, fields[2]) != null || sizes.put(path, size) != null) {
                    throw new IllegalStateException("duplicate exact resource path");
                }
                previousPath = path;
            }
        } catch (IOException exception) {
            throw new IllegalStateException("failed to parse exact resource manifest", exception);
        }
        return new Data(
                Collections.unmodifiableMap(digests),
                Collections.unmodifiableMap(sizes)
        );
    }

    private static void requireDigest(byte[] raw, String expectedSha256) {
        byte[] expected = java.util.HexFormat.of().parseHex(expectedSha256);
        byte[] actual;
        try {
            actual = MessageDigest.getInstance("SHA-256").digest(raw);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 unavailable", exception);
        }
        if (!MessageDigest.isEqual(expected, actual)) {
            throw new IllegalStateException("exact resource manifest digest mismatch");
        }
    }

    /** Immutable paths, SHA-256 digests, and byte sizes from one exact partition. */
    public record Data(Map<String, String> digests, Map<String, Long> sizes) {

        public Data {
            Objects.requireNonNull(digests, "digests");
            Objects.requireNonNull(sizes, "sizes");
            if (digests.isEmpty() || !digests.keySet().equals(sizes.keySet())) {
                throw new IllegalArgumentException("manifest maps must be nonempty and aligned");
            }
            digests = Collections.unmodifiableMap(new LinkedHashMap<>(digests));
            sizes = Collections.unmodifiableMap(new LinkedHashMap<>(sizes));
        }
    }
}
