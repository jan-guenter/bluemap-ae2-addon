/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.adapter.bluemap523;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;

/** Conservative resource adapter for AE2's namespaced blockstate Z rotation. */
final class Ae2ZRotationRenderer {

    static final String AE2_Z_KEY = "ae2:z";
    static final String BLUEMAP_Z_KEY = "z";
    private static final int MAX_BLOCKSTATE_CHARACTERS = 1024 * 1024;

    private Ae2ZRotationRenderer() {
    }

    /**
     * Rewrites object-property keys named {@code ae2:z} to BlueMap's supported
     * {@code z} field without changing strings, numbers, ordering, or whitespace.
     */
    static String normalizeBlockstateJson(String json) {
        return normalize(json).json();
    }

    static Normalization normalize(String json) {
        Objects.requireNonNull(json, "json");
        if (json.isBlank() || json.length() > MAX_BLOCKSTATE_CHARACTERS) {
            throw new IllegalArgumentException("invalid AE2 blockstate JSON size");
        }
        int first = firstNonWhitespace(json);
        int last = lastNonWhitespace(json);
        if (first < 0 || json.charAt(first) != '{' || json.charAt(last) != '}') {
            throw new IllegalArgumentException("AE2 blockstate JSON must be an object");
        }

        StringBuilder result = new StringBuilder(json.length());
        Deque<Container> containers = new ArrayDeque<>();
        int replacements = 0;
        boolean rootStarted = false;
        boolean rootClosed = false;
        for (int index = 0; index < json.length();) {
            char character = json.charAt(index);
            if (character == '"') {
                if (containers.isEmpty()) {
                    throw new IllegalArgumentException("JSON string outside root object");
                }
                int end = stringEnd(json, index);
                String token = json.substring(index, end + 1);
                int next = nextNonWhitespace(json, end + 1);
                if (next < json.length()
                        && json.charAt(next) == ':'
                        && !containers.isEmpty()
                        && containers.peek().type == '{') {
                    String key = decodeJsonString(token.substring(1, token.length() - 1));
                    if (AE2_Z_KEY.equals(key) || BLUEMAP_Z_KEY.equals(key)) {
                        Container object = containers.peek();
                        if (object.sawZRotation) {
                            throw new IllegalArgumentException(
                                    "duplicate AE2/BlueMap Z rotation in one variant"
                            );
                        }
                        object.sawZRotation = true;
                        if (AE2_Z_KEY.equals(key)) {
                            result.append('"').append(BLUEMAP_Z_KEY).append('"');
                            replacements++;
                        } else {
                            result.append(token);
                        }
                    } else {
                        result.append(token);
                    }
                } else {
                    result.append(token);
                }
                index = end + 1;
                continue;
            }
            if (character == '{' || character == '[') {
                if (containers.isEmpty()) {
                    if (rootStarted || rootClosed || character != '{') {
                        throw new IllegalArgumentException("multiple JSON root values");
                    }
                    rootStarted = true;
                }
                containers.push(new Container(character));
            } else if (character == '}' || character == ']') {
                requireMatchingContainer(containers, character);
                containers.pop();
                if (containers.isEmpty()) {
                    rootClosed = true;
                }
            } else if (character < 0x20 && !Character.isWhitespace(character)) {
                throw new IllegalArgumentException("control character in AE2 blockstate JSON");
            } else if (containers.isEmpty() && !Character.isWhitespace(character)) {
                throw new IllegalArgumentException("JSON token outside root object");
            }
            result.append(character);
            index++;
        }
        if (!containers.isEmpty() || !rootStarted || !rootClosed) {
            throw new IllegalArgumentException("unclosed AE2 blockstate JSON container");
        }
        return new Normalization(result.toString(), replacements);
    }

    private static int stringEnd(String json, int start) {
        boolean escaped = false;
        for (int index = start + 1; index < json.length(); index++) {
            char character = json.charAt(index);
            if (character < 0x20) {
                throw new IllegalArgumentException("control character in JSON string");
            }
            if (escaped) {
                escaped = false;
            } else if (character == '\\') {
                escaped = true;
            } else if (character == '"') {
                return index;
            }
        }
        throw new IllegalArgumentException("unterminated AE2 blockstate JSON string");
    }

    private static String decodeJsonString(String raw) {
        StringBuilder decoded = new StringBuilder(raw.length());
        for (int index = 0; index < raw.length(); index++) {
            char character = raw.charAt(index);
            if (character != '\\') {
                decoded.append(character);
                continue;
            }
            if (++index >= raw.length()) {
                throw new IllegalArgumentException("truncated JSON escape");
            }
            char escaped = raw.charAt(index);
            switch (escaped) {
                case '"', '\\', '/' -> decoded.append(escaped);
                case 'b' -> decoded.append('\b');
                case 'f' -> decoded.append('\f');
                case 'n' -> decoded.append('\n');
                case 'r' -> decoded.append('\r');
                case 't' -> decoded.append('\t');
                case 'u' -> {
                    if (index + 4 >= raw.length()) {
                        throw new IllegalArgumentException("truncated JSON unicode escape");
                    }
                    int value = 0;
                    for (int offset = 1; offset <= 4; offset++) {
                        int digit = Character.digit(raw.charAt(index + offset), 16);
                        if (digit < 0) {
                            throw new IllegalArgumentException("invalid JSON unicode escape");
                        }
                        value = value * 16 + digit;
                    }
                    decoded.append((char) value);
                    index += 4;
                }
                default -> throw new IllegalArgumentException("invalid JSON escape");
            }
        }
        return decoded.toString();
    }

    private static void requireMatchingContainer(
            Deque<Container> containers,
            char closing
    ) {
        char expected = closing == '}' ? '{' : '[';
        if (containers.isEmpty() || containers.peek().type != expected) {
            throw new IllegalArgumentException("mismatched AE2 blockstate JSON container");
        }
    }

    private static int firstNonWhitespace(String value) {
        for (int index = 0; index < value.length(); index++) {
            if (!Character.isWhitespace(value.charAt(index))) {
                return index;
            }
        }
        return -1;
    }

    private static int lastNonWhitespace(String value) {
        for (int index = value.length() - 1; index >= 0; index--) {
            if (!Character.isWhitespace(value.charAt(index))) {
                return index;
            }
        }
        return -1;
    }

    private static int nextNonWhitespace(String value, int start) {
        for (int index = start; index < value.length(); index++) {
            if (!Character.isWhitespace(value.charAt(index))) {
                return index;
            }
        }
        return value.length();
    }

    record Normalization(String json, int replacements) {

        Normalization {
            Objects.requireNonNull(json, "json");
            if (replacements < 0) {
                throw new IllegalArgumentException("replacements must be nonnegative");
            }
        }
    }

    private static final class Container {

        private final char type;
        private boolean sawZRotation;

        private Container(char type) {
            this.type = type;
        }
    }
}
