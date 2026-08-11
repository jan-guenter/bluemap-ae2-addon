/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.model;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/** Neutral Cell Dock model composition ready for a host-specific renderer adapter. */
public record MegaCellDockModel(
        String bodyModelId,
        PartOrientation bodyOrientation,
        Optional<ModelPlacement> cellChassis,
        List<LedPlacement> offlineLeds,
        int nominalTriangleCount
) {

    public static final String BODY_MODEL = "megacells:part/cell_dock";

    public MegaCellDockModel {
        Objects.requireNonNull(bodyModelId, "bodyModelId");
        Objects.requireNonNull(bodyOrientation, "bodyOrientation");
        cellChassis = Objects.requireNonNull(cellChassis, "cellChassis");
        offlineLeds = List.copyOf(Objects.requireNonNull(offlineLeds, "offlineLeds"));
        if (cellChassis.isEmpty() != offlineLeds.isEmpty()) {
            throw new IllegalArgumentException("Cell Dock chassis and LEDs must be present together");
        }
        if (!offlineLeds.isEmpty() && offlineLeds.size() != 2) {
            throw new IllegalArgumentException("an occupied Cell Dock must have two LEDs");
        }
        if (nominalTriangleCount < MegaCellDockGeometry.BODY_NOMINAL_TRIANGLES) {
            throw new IllegalArgumentException("invalid Cell Dock nominal triangle count");
        }
    }

    static MegaCellDockModel from(MegaCellDockSnapshot snapshot) {
        Objects.requireNonNull(snapshot, "snapshot");
        Optional<ModelPlacement> chassis = snapshot.cell().map(cell -> new ModelPlacement(
                cell.modelId(),
                MegaCellDockGeometry.cellTransform(snapshot.side(), snapshot.spin()),
                cell.chassisKind().nominalTriangles()
        ));
        List<LedPlacement> leds = chassis.isEmpty() ? List.of() : List.of(
                new LedPlacement(
                        MegaCellDockGeometry.firstLedTransform(snapshot.side(), snapshot.spin()),
                        MegaCellDockGeometry.offlineUnknownLed(),
                        MegaCellDockGeometry.OFFLINE_UNKNOWN_LED_RGB
                ),
                new LedPlacement(
                        MegaCellDockGeometry.secondLedTransform(snapshot.side(), snapshot.spin()),
                        MegaCellDockGeometry.offlineUnknownLed(),
                        MegaCellDockGeometry.OFFLINE_UNKNOWN_LED_RGB
                )
        );
        return new MegaCellDockModel(
                BODY_MODEL,
                snapshot.bodyOrientation(),
                chassis,
                leds,
                MegaCellDockGeometry.nominalTriangleCount(snapshot)
        );
    }

    public record ModelPlacement(
            String modelId,
            MegaCellDockGeometry.Transform transform,
            int nominalTriangles
    ) {

        public ModelPlacement {
            Objects.requireNonNull(modelId, "modelId");
            Objects.requireNonNull(transform, "transform");
            if (nominalTriangles <= 0) {
                throw new IllegalArgumentException("model placement must contain triangles");
            }
        }
    }

    public record LedPlacement(
            MegaCellDockGeometry.Transform transform,
            List<MegaCellDockGeometry.LedQuad> quads,
            int rgb
    ) {

        public LedPlacement {
            Objects.requireNonNull(transform, "transform");
            quads = List.copyOf(Objects.requireNonNull(quads, "quads"));
            if (quads.size() != 5) {
                throw new IllegalArgumentException("Cell Dock LED must contain five quads");
            }
            if (rgb < 0 || rgb > 0xffffff) {
                throw new IllegalArgumentException("LED RGB must be in [0, 0xffffff]");
            }
        }
    }
}
