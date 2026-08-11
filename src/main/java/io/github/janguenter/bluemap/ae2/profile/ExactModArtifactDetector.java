/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.profile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/** Bounded exact-artifact detector shared by independently activated extension profiles. */
public final class ExactModArtifactDetector {

    private static final int BUFFER_SIZE = 64 * 1024;
    private static final int MAX_RESOURCE_ROOTS = 4_096;
    private static final int MAX_DESCRIPTOR_BYTES = 1024 * 1024;
    private static final String MOD_DESCRIPTOR = "META-INF/neoforge.mods.toml";
    private static final Pattern WIRE_ID = Pattern.compile("[a-z0-9][a-z0-9_.-]*");
    private static final Pattern SHA256 = Pattern.compile("[0-9a-f]{64}");
    private static final Pattern MODS_TABLE = Pattern.compile(
            "^\\[\\[\\s*(?:mods|\\\"mods\\\"|'mods')\\s*\\]\\]$"
    );

    private final Identity identity;
    private final Pattern modIdStatement;

    public ExactModArtifactDetector(Identity identity) {
        this.identity = Objects.requireNonNull(identity, "identity");
        this.modIdStatement = Pattern.compile(
                "^(?:modId|\\\"modId\\\"|'modId')\\s*=\\s*(?:\\\""
                        + Pattern.quote(identity.modId())
                        + "\\\"|'" + Pattern.quote(identity.modId()) + "')$"
        );
    }

    public Detection detect(Iterable<Path> roots) throws IOException, InterruptedException {
        Objects.requireNonNull(roots, "roots");
        Set<Path> inspected = new HashSet<>();
        Path candidate = null;
        int rootCount = 0;
        for (Path root : roots) {
            if (Thread.interrupted()) {
                throw new InterruptedException(
                        "Interrupted while identifying " + identity.modId() + " resources"
                );
            }
            if (++rootCount > MAX_RESOURCE_ROOTS) {
                throw new IOException("Too many resource roots while identifying "
                        + identity.modId());
            }
            if (root == null || !Files.isRegularFile(root)) {
                continue;
            }
            Path name = root.getFileName();
            if (name == null || !name.toString().toLowerCase(Locale.ROOT).endsWith(".jar")) {
                continue;
            }
            Path real = root.toRealPath();
            if (!inspected.add(real) || !declaresMod(real)) {
                continue;
            }
            if (candidate != null) {
                return Detection.failure(identity.modId(), Failure.MULTIPLE_ARTIFACTS);
            }
            candidate = real;
        }
        if (candidate == null) {
            return Detection.failure(identity.modId(), Failure.NOT_FOUND);
        }
        if (Files.size(candidate) != identity.bytes()
                || !identity.sha256().equals(sha256(candidate))) {
            return Detection.failure(identity.modId(), Failure.MISMATCH);
        }
        return Detection.exact(identity);
    }

    private boolean declaresMod(Path jar) throws IOException {
        try (ZipFile zip = new ZipFile(jar.toFile())) {
            ZipEntry descriptor = zip.getEntry(MOD_DESCRIPTOR);
            if (descriptor == null || descriptor.isDirectory()) {
                return false;
            }
            if (descriptor.getSize() > MAX_DESCRIPTOR_BYTES) {
                throw new IOException("NeoForge mod descriptor exceeds the inspection limit");
            }
            byte[] bytes;
            try (InputStream input = zip.getInputStream(descriptor)) {
                bytes = input.readNBytes(MAX_DESCRIPTOR_BYTES + 1);
            }
            if (bytes.length > MAX_DESCRIPTOR_BYTES) {
                throw new IOException("NeoForge mod descriptor exceeds the inspection limit");
            }
            return descriptorDeclaresMod(decodeUtf8(bytes));
        }
    }

    private boolean descriptorDeclaresMod(String descriptor) {
        String normalized = descriptor.startsWith("\ufeff")
                ? descriptor.substring(1) : descriptor;
        LexicalState state = LexicalState.NORMAL;
        boolean inModsTable = false;
        for (String line : normalized.split("\\R", -1)) {
            SanitizedLine sanitized = sanitizeTomlLine(line, state);
            state = sanitized.state();
            String statement = sanitized.text().trim();
            if (statement.isEmpty()) {
                continue;
            }
            if (statement.startsWith("[")) {
                inModsTable = MODS_TABLE.matcher(statement).matches();
                continue;
            }
            if (inModsTable && modIdStatement.matcher(statement).matches()) {
                return true;
            }
        }
        return false;
    }

    private static String decodeUtf8(byte[] bytes) throws IOException {
        try {
            return StandardCharsets.UTF_8.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT)
                    .decode(ByteBuffer.wrap(bytes))
                    .toString();
        } catch (CharacterCodingException exception) {
            throw new IOException("NeoForge mod descriptor is not valid UTF-8", exception);
        }
    }

    private static SanitizedLine sanitizeTomlLine(String line, LexicalState initialState) {
        StringBuilder result = new StringBuilder(line.length());
        LexicalState state = initialState;
        boolean basicString = false;
        boolean literalString = false;
        for (int index = 0; index < line.length();) {
            if (state != LexicalState.NORMAL) {
                String delimiter = state == LexicalState.MULTILINE_BASIC ? "\"\"\"" : "'''";
                if (line.startsWith(delimiter, index)
                        && (state != LexicalState.MULTILINE_BASIC
                        || !isEscaped(line, index))) {
                    state = LexicalState.NORMAL;
                    index += delimiter.length();
                } else {
                    index++;
                }
                continue;
            }
            char character = line.charAt(index);
            if (basicString) {
                result.append(character);
                if (character == '"' && !isEscaped(line, index)) {
                    basicString = false;
                }
                index++;
                continue;
            }
            if (literalString) {
                result.append(character);
                if (character == '\'') {
                    literalString = false;
                }
                index++;
                continue;
            }
            if (line.startsWith("\"\"\"", index)) {
                result.append("\"\"\"");
                state = LexicalState.MULTILINE_BASIC;
                index += 3;
                continue;
            }
            if (line.startsWith("'''", index)) {
                result.append("'''");
                state = LexicalState.MULTILINE_LITERAL;
                index += 3;
                continue;
            }
            if (character == '#') {
                break;
            }
            result.append(character);
            if (character == '"') {
                basicString = true;
            } else if (character == '\'') {
                literalString = true;
            }
            index++;
        }
        return new SanitizedLine(result.toString(), state);
    }

    private static boolean isEscaped(String line, int index) {
        int backslashes = 0;
        for (int cursor = index - 1;
                cursor >= 0 && line.charAt(cursor) == '\\';
                cursor--) {
            backslashes++;
        }
        return backslashes % 2 != 0;
    }

    private static String sha256(Path file) throws IOException {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 unavailable", exception);
        }
        byte[] buffer = new byte[BUFFER_SIZE];
        try (InputStream input = Files.newInputStream(file)) {
            int read;
            while ((read = input.read(buffer)) != -1) {
                digest.update(buffer, 0, read);
            }
        }
        return HexFormat.of().formatHex(digest.digest());
    }

    public enum Failure {
        NONE,
        NOT_FOUND,
        MULTIPLE_ARTIFACTS,
        MISMATCH
    }

    public record Identity(
            String modId,
            String version,
            long bytes,
            String sha256,
            String exactReason
    ) {

        public Identity {
            Objects.requireNonNull(modId, "modId");
            Objects.requireNonNull(version, "version");
            Objects.requireNonNull(sha256, "sha256");
            Objects.requireNonNull(exactReason, "exactReason");
            if (!WIRE_ID.matcher(modId).matches()
                    || !WIRE_ID.matcher(exactReason).matches()
                    || version.isBlank()
                    || bytes <= 0
                    || !SHA256.matcher(sha256).matches()) {
                throw new IllegalArgumentException("invalid exact mod identity");
            }
        }
    }

    public record Detection(
            boolean exact,
            String modId,
            String version,
            String reason,
            Failure failure
    ) {

        public Detection {
            Objects.requireNonNull(modId, "modId");
            Objects.requireNonNull(version, "version");
            Objects.requireNonNull(reason, "reason");
            Objects.requireNonNull(failure, "failure");
            if (exact != (failure == Failure.NONE)) {
                throw new IllegalArgumentException("exact and failure differ");
            }
        }

        private static Detection exact(Identity identity) {
            return new Detection(
                    true,
                    identity.modId(),
                    identity.version(),
                    identity.exactReason(),
                    Failure.NONE
            );
        }

        private static Detection failure(String modId, Failure failure) {
            return new Detection(
                    false,
                    modId,
                    "unknown",
                    modId + "-" + failure.name().toLowerCase(Locale.ROOT).replace('_', '-'),
                    failure
            );
        }
    }

    private record SanitizedLine(String text, LexicalState state) {
    }

    private enum LexicalState {
        NORMAL,
        MULTILINE_BASIC,
        MULTILINE_LITERAL
    }
}
