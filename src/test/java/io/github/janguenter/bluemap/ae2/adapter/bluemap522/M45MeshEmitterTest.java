/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.adapter.bluemap522;

import de.bluecolored.bluemap.core.util.Key;
import io.github.janguenter.bluemap.ae2.model.Direction6;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class M45MeshEmitterTest {

    @Test
    void validatesCanonicalSurfaceRowsBeforeHostRendering() {
        List<M45MeshEmitter.Vertex> vertices = List.of(
                vertex(0, 16, 0, 0, 0),
                vertex(0, 16, 16, 0, 16),
                vertex(16, 16, 16, 16, 16),
                vertex(16, 16, 0, 16, 0)
        );
        M45MeshEmitter.Quad quad = M45MeshEmitter.Quad.outward(
                Direction6.UP,
                Key.parse("example:block/surface"),
                vertices,
                false,
                true
        );

        assertEquals(Direction6.UP, quad.lightFace());
        assertEquals(vertices, quad.vertices());
        assertThrows(
                IllegalArgumentException.class,
                () -> vertex(17, 0, 0, 0, 0)
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> new M45MeshEmitter.Quad(
                        Direction6.NORTH,
                        Direction6.NORTH,
                        Key.parse("example:block/surface"),
                        vertices.subList(0, 3),
                        false,
                        true,
                        true
                )
        );
    }

    private static M45MeshEmitter.Vertex vertex(
            double x,
            double y,
            double z,
            double u,
            double v
    ) {
        return new M45MeshEmitter.Vertex(x, y, z, u, v);
    }
}
