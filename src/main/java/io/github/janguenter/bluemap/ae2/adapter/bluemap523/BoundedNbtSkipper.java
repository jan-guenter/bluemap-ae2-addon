/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.adapter.bluemap523;

import de.bluecolored.bluenbt.NBTReader;
import de.bluecolored.bluenbt.TagType;

import java.io.IOException;

/** Fail-closed traversal that does not materialize nested NBT containers. */
final class BoundedNbtSkipper {

    static final int MAX_NESTING_DEPTH = 16;
    static final int MAX_VISITED_TAGS = 512;
    static final int MAX_LIST_ENTRIES = 256;
    static final int MAX_ARRAY_ELEMENTS = 4096;
    static final long MAX_TOTAL_ARRAY_BYTES = 65_536L;

    private static final byte[] EMPTY_BYTES = new byte[0];
    private static final int[] EMPTY_INTS = new int[0];
    private static final long[] EMPTY_LONGS = new long[0];

    private BoundedNbtSkipper() {
    }

    static void skip(NBTReader reader) throws IOException {
        skip(reader, new Budget(), 0);
    }

    static void skip(NBTReader reader, Budget budget, int depth) throws IOException {
        budget.visit();
        TagType type = reader.peek();
        switch (type) {
            case BYTE, SHORT, INT, LONG, FLOAT, DOUBLE, STRING -> reader.skip();
            case BYTE_ARRAY -> {
                int length = reader.nextByteArray(EMPTY_BYTES);
                budget.array(length, Byte.BYTES);
            }
            case INT_ARRAY -> {
                int length = reader.nextIntArray(EMPTY_INTS);
                budget.array(length, Integer.BYTES);
            }
            case LONG_ARRAY -> {
                int length = reader.nextLongArray(EMPTY_LONGS);
                budget.array(length, Long.BYTES);
            }
            case LIST -> skipList(reader, budget, depth);
            case COMPOUND -> skipCompound(reader, budget, depth);
            case END -> throw rejected();
            default -> throw rejected();
        }
    }

    private static void skipList(NBTReader reader, Budget budget, int depth) throws IOException {
        requireContainerDepth(depth);
        int length = reader.beginList();
        if (length < 0 || length > MAX_LIST_ENTRIES) {
            throw rejected();
        }
        if (length > 0 && reader.peek() == TagType.END) {
            throw rejected();
        }
        for (int index = 0; index < length; index++) {
            skip(reader, budget, depth + 1);
        }
        if (reader.peek() != TagType.END) {
            throw rejected();
        }
        reader.endList();
    }

    private static void skipCompound(NBTReader reader, Budget budget, int depth)
            throws IOException {
        requireContainerDepth(depth);
        reader.beginCompound();
        while (reader.peek() != TagType.END) {
            skip(reader, budget, depth + 1);
        }
        reader.endCompound();
    }

    private static void requireContainerDepth(int depth) throws IOException {
        if (depth >= MAX_NESTING_DEPTH) {
            throw rejected();
        }
    }

    static IOException rejected() {
        return new IOException("Rejected malformed or oversized AE2 retained NBT");
    }

    static IOException rejected(Exception cause) {
        return new IOException("Rejected malformed or oversized AE2 retained NBT", cause);
    }

    static final class Budget {

        private int visitedTags;
        private long totalArrayBytes;

        void visit() throws IOException {
            visitedTags++;
            if (visitedTags > MAX_VISITED_TAGS) {
                throw rejected();
            }
        }

        void array(int length, int bytesPerElement) throws IOException {
            if (length < 0 || length > MAX_ARRAY_ELEMENTS) {
                throw rejected();
            }
            totalArrayBytes += (long) length * bytesPerElement;
            if (totalArrayBytes > MAX_TOTAL_ARRAY_BYTES) {
                throw rejected();
            }
        }
    }
}
