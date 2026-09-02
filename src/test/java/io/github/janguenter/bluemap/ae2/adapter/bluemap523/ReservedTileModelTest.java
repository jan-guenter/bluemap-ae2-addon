/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.adapter.bluemap523;

import de.bluecolored.bluemap.core.map.hires.ArrayTileModel;
import de.bluecolored.bluemap.core.map.hires.MaxCapacityReachedException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ReservedTileModelTest {

    @Test
    void oneHostAllocationCommitsOnlyTheUsedPrefix() {
        RecordingModel host = new RecordingModel();
        host.add(2);
        host.setMaterialIndex(0, 91);
        host.resetAddCalls();

        ReservedTileModel reserved = new ReservedTileModel(host, 8);
        assertEquals(1, host.addCalls());
        assertEquals(10, host.size());
        assertEquals(0, reserved.add(3));
        reserved.setMaterialIndex(0, 17);
        assertEquals(1, host.addCalls());

        reserved.commit();

        assertEquals(5, host.size());
        assertEquals(91, host.material(0));
        assertEquals(17, host.material(2));
    }

    @Test
    void rollbackRemovesTheWholeReservationAndCapacityFailureAddsNothing() {
        RecordingModel host = new RecordingModel();
        host.add(1);
        host.setMaterialIndex(0, 73);
        host.resetAddCalls();

        ReservedTileModel reserved = new ReservedTileModel(host, 34);
        reserved.add(12);
        reserved.rollback();
        assertEquals(1, host.size());
        assertEquals(73, host.material(0));
        assertEquals(1, host.addCalls());

        host.failNextAdd();
        assertThrows(
                MaxCapacityReachedException.class,
                () -> new ReservedTileModel(host, 78)
        );
        assertEquals(1, host.size());
        assertEquals(73, host.material(0));
        assertEquals(2, host.addCalls());
    }

    @Test
    void localOverflowCannotAppendAnotherHostPrefix() {
        RecordingModel host = new RecordingModel();
        ReservedTileModel reserved = new ReservedTileModel(host, 4);
        reserved.add(4);

        assertThrows(IllegalStateException.class, () -> reserved.add(1));
        assertEquals(1, host.addCalls());
        reserved.rollback();
        assertEquals(0, host.size());
    }

    private static final class RecordingModel extends ArrayTileModel {
        private int addCalls;
        private boolean failNextAdd;

        private RecordingModel() {
            super(256);
        }

        @Override
        public int add(int count) {
            addCalls++;
            if (failNextAdd) {
                failNextAdd = false;
                throw new MaxCapacityReachedException("injected capacity");
            }
            return super.add(count);
        }

        private int material(int face) {
            // ArrayTileModel intentionally has no public getters. Recording
            // just the two sentinel writes is sufficient for this seam test.
            return face == 0 ? firstMaterial : reservedMaterial;
        }

        private int firstMaterial;
        private int reservedMaterial;

        @Override
        public RecordingModel setMaterialIndex(int face, int material) {
            super.setMaterialIndex(face, material);
            if (face == 0) {
                firstMaterial = material;
            } else if (face == 2) {
                reservedMaterial = material;
            }
            return this;
        }

        private int addCalls() {
            return addCalls;
        }

        private void resetAddCalls() {
            addCalls = 0;
        }

        private void failNextAdd() {
            failNextAdd = true;
        }
    }
}
