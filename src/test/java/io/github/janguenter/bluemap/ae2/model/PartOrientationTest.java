/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.model;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PartOrientationTest {

    private static final Map<Direction6, List<PartOrientation>> EXPECTED = Map.of(
            Direction6.DOWN, List.of(
                    orientation(90, 0, 0),
                    orientation(90, 0, 270),
                    orientation(90, 0, 180),
                    orientation(90, 0, 90)
            ),
            Direction6.UP, List.of(
                    orientation(270, 0, 180),
                    orientation(270, 0, 90),
                    orientation(270, 0, 0),
                    orientation(270, 0, 270)
            ),
            Direction6.NORTH, List.of(
                    orientation(0, 0, 0),
                    orientation(0, 0, 270),
                    orientation(0, 0, 180),
                    orientation(0, 0, 90)
            ),
            Direction6.SOUTH, List.of(
                    orientation(0, 180, 0),
                    orientation(0, 180, 90),
                    orientation(0, 180, 180),
                    orientation(0, 180, 270)
            ),
            Direction6.WEST, List.of(
                    orientation(0, 270, 0),
                    orientation(0, 270, 270),
                    orientation(0, 270, 180),
                    orientation(0, 270, 90)
            ),
            Direction6.EAST, List.of(
                    orientation(0, 90, 0),
                    orientation(0, 90, 270),
                    orientation(0, 90, 180),
                    orientation(0, 90, 90)
            )
    );

    @Test
    void matchesAllTwentyFourExactAe2AngleTriples() {
        for (Direction6 face : Direction6.values()) {
            for (int spin = 0; spin < 4; spin++) {
                assertEquals(
                        EXPECTED.get(face).get(spin),
                        PartOrientation.forPart(face, spin),
                        face + " spin " + spin
                );
            }
        }
    }

    @Test
    void southSpinProgressionUsesTheMirroredRegressionOrder() {
        assertEquals(orientation(0, 180, 0), PartOrientation.forPart(Direction6.SOUTH, 0));
        assertEquals(orientation(0, 180, 90), PartOrientation.forPart(Direction6.SOUTH, 1));
        assertEquals(orientation(0, 180, 180), PartOrientation.forPart(Direction6.SOUTH, 2));
        assertEquals(orientation(0, 180, 270), PartOrientation.forPart(Direction6.SOUTH, 3));
    }

    @Test
    void rejectsMissingFacesAndOutOfRangeSpins() {
        assertThrows(NullPointerException.class, () -> PartOrientation.forPart(null, 0));
        assertThrows(
                IllegalArgumentException.class,
                () -> PartOrientation.forPart(Direction6.NORTH, -1)
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> PartOrientation.forPart(Direction6.NORTH, 4)
        );
    }

    private static PartOrientation orientation(float x, float y, float z) {
        return new PartOrientation(x, y, z);
    }
}
