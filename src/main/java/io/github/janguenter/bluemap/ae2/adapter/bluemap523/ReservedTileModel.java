/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.adapter.bluemap523;

import de.bluecolored.bluemap.core.map.hires.TileModel;
import de.bluecolored.bluemap.core.util.math.MatrixM3f;
import de.bluecolored.bluemap.core.util.math.MatrixM4f;

import java.util.Objects;

/**
 * A bounded local view over one up-front reservation in a host tile model.
 *
 * <p>The constructor performs the only host {@link TileModel#add(int)} call.
 * Renderers can then use ordinary local indices without exposing a partially
 * appended prefix when that reservation exceeds the host capacity.</p>
 */
final class ReservedTileModel implements TileModel {

    private final TileModel delegate;
    private final int start;
    private final int capacity;
    private int size;
    private boolean finished;

    ReservedTileModel(TileModel delegate, int capacity) {
        this.delegate = Objects.requireNonNull(delegate, "delegate");
        if (capacity < 0) {
            throw new IllegalArgumentException("capacity is negative");
        }
        this.capacity = capacity;
        this.start = delegate.add(capacity);
    }

    int capacity() {
        return capacity;
    }

    void commit() {
        requireOpen();
        requireUnchangedHost();
        delegate.reset(start + size);
        finished = true;
    }

    void rollback() {
        if (!finished) {
            requireUnchangedHost();
            delegate.reset(start);
            finished = true;
        }
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public int add(int count) {
        requireOpen();
        if (count < 0 || count > capacity - size) {
            throw new IllegalStateException("reserved tile-model capacity exceeded");
        }
        int localStart = size;
        size += count;
        return localStart;
    }

    @Override
    public TileModel setPositions(
            int face,
            float x1, float y1, float z1,
            float x2, float y2, float z2,
            float x3, float y3, float z3
    ) {
        delegate.setPositions(index(face), x1, y1, z1, x2, y2, z2, x3, y3, z3);
        return this;
    }

    @Override
    public TileModel setUvs(
            int face,
            float u1, float v1,
            float u2, float v2,
            float u3, float v3
    ) {
        delegate.setUvs(index(face), u1, v1, u2, v2, u3, v3);
        return this;
    }

    @Override
    public TileModel setAOs(int face, float ao1, float ao2, float ao3) {
        delegate.setAOs(index(face), ao1, ao2, ao3);
        return this;
    }

    @Override
    public TileModel setColor(int face, float red, float green, float blue) {
        delegate.setColor(index(face), red, green, blue);
        return this;
    }

    @Override
    public TileModel setSunlight(int face, int sunlight) {
        delegate.setSunlight(index(face), sunlight);
        return this;
    }

    @Override
    public TileModel setBlocklight(int face, int blocklight) {
        delegate.setBlocklight(index(face), blocklight);
        return this;
    }

    @Override
    public TileModel setMaterialIndex(int face, int material) {
        delegate.setMaterialIndex(index(face), material);
        return this;
    }

    @Override
    public TileModel invertOrientation(int face) {
        delegate.invertOrientation(index(face));
        return this;
    }

    @Override
    public TileModel rotate(
            int localStart,
            int count,
            float angle,
            float axisX,
            float axisY,
            float axisZ
    ) {
        range(localStart, count);
        delegate.rotate(start + localStart, count, angle, axisX, axisY, axisZ);
        return this;
    }

    @Override
    public TileModel rotateXYZ(
            int localStart,
            int count,
            float pitch,
            float yaw,
            float roll
    ) {
        range(localStart, count);
        delegate.rotateXYZ(start + localStart, count, pitch, yaw, roll);
        return this;
    }

    @Override
    public TileModel rotateZYX(
            int localStart,
            int count,
            float pitch,
            float yaw,
            float roll
    ) {
        range(localStart, count);
        delegate.rotateZYX(start + localStart, count, pitch, yaw, roll);
        return this;
    }

    @Override
    public TileModel rotateYXZ(
            int localStart,
            int count,
            float pitch,
            float yaw,
            float roll
    ) {
        range(localStart, count);
        delegate.rotateYXZ(start + localStart, count, pitch, yaw, roll);
        return this;
    }

    @Override
    public TileModel rotateByQuaternion(
            int localStart,
            int count,
            double qx,
            double qy,
            double qz,
            double qw
    ) {
        range(localStart, count);
        delegate.rotateByQuaternion(start + localStart, count, qx, qy, qz, qw);
        return this;
    }

    @Override
    public TileModel scale(
            int localStart,
            int count,
            float scaleX,
            float scaleY,
            float scaleZ
    ) {
        range(localStart, count);
        delegate.scale(start + localStart, count, scaleX, scaleY, scaleZ);
        return this;
    }

    @Override
    public TileModel translate(
            int localStart,
            int count,
            float x,
            float y,
            float z
    ) {
        range(localStart, count);
        delegate.translate(start + localStart, count, x, y, z);
        return this;
    }

    @Override
    public TileModel transform(int localStart, int count, MatrixM3f transform) {
        range(localStart, count);
        delegate.transform(start + localStart, count, transform);
        return this;
    }

    @Override
    public TileModel transform(
            int localStart,
            int count,
            float m00, float m01, float m02,
            float m10, float m11, float m12,
            float m20, float m21, float m22
    ) {
        range(localStart, count);
        delegate.transform(
                start + localStart, count,
                m00, m01, m02,
                m10, m11, m12,
                m20, m21, m22
        );
        return this;
    }

    @Override
    public TileModel transform(int localStart, int count, MatrixM4f transform) {
        range(localStart, count);
        delegate.transform(start + localStart, count, transform);
        return this;
    }

    @Override
    public TileModel transform(
            int localStart,
            int count,
            float m00, float m01, float m02, float m03,
            float m10, float m11, float m12, float m13,
            float m20, float m21, float m22, float m23,
            float m30, float m31, float m32, float m33
    ) {
        range(localStart, count);
        delegate.transform(
                start + localStart, count,
                m00, m01, m02, m03,
                m10, m11, m12, m13,
                m20, m21, m22, m23,
                m30, m31, m32, m33
        );
        return this;
    }

    @Override
    public TileModel reset(int newSize) {
        requireOpen();
        if (newSize < 0 || newSize > size) {
            throw new IllegalArgumentException("invalid reserved tile-model size");
        }
        size = newSize;
        return this;
    }

    @Override
    public TileModel clear() {
        return reset(0);
    }

    @Override
    public void sort() {
        throw new UnsupportedOperationException("a reserved tile-model cannot be sorted");
    }

    private int index(int face) {
        requireOpen();
        if (face < 0 || face >= size) {
            throw new IndexOutOfBoundsException("face outside reserved tile-model");
        }
        return start + face;
    }

    private void range(int localStart, int count) {
        requireOpen();
        if (localStart < 0 || count < 0 || localStart > size - count) {
            throw new IndexOutOfBoundsException("range outside reserved tile-model");
        }
    }

    private void requireOpen() {
        if (finished) {
            throw new IllegalStateException("reserved tile-model is already finished");
        }
    }

    private void requireUnchangedHost() {
        if (delegate.size() != start + capacity) {
            throw new IllegalStateException("host tile-model changed during reservation");
        }
    }
}
