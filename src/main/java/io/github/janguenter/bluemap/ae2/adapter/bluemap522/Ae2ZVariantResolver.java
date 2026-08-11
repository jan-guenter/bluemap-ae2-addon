/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.adapter.bluemap522;

import de.bluecolored.bluemap.core.resources.ResourcePath;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.Variant;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.model.Model;
import de.bluecolored.bluemap.core.world.BlockState;
import io.github.janguenter.bluemap.ae2.profile.expandedae.ExpandedAe211Catalog;
import io.github.janguenter.bluemap.ae2.profile.merequester.MeRequester143Catalog;

import java.util.Locale;
import java.util.Map;

/**
 * Exact normalization of AE2's namespaced {@code ae2:z} blockstate transform.
 *
 * <p>BlueMap 5.22 already applies a regular {@code z} value in {@link Variant},
 * but its Gson blockstate reader intentionally ignores the namespaced member.
 * Only the two exact ATM 1.2.0 blocks proven to use that member are accepted
 * here; malformed or future state shapes fail closed.</p>
 */
final class Ae2ZVariantResolver {

    private Ae2ZVariantResolver() {
    }

    static Variant resolve(BlockState state) {
        if (state == null || state.getId() == null) {
            return null;
        }
        String id = state.getId().getFormatted();
        return switch (id) {
            case MeRequester143Catalog.REQUESTER_BLOCK -> requester(state.getProperties());
            case ExpandedAe211Catalog.IO_PORT_BLOCK -> ioPort(state.getProperties());
            default -> null;
        };
    }

    private static Variant requester(Map<String, String> properties) {
        if (!properties.keySet().equals(expectedKeys(properties, "active", false))) {
            return null;
        }
        String active = properties.get("active");
        if (!("false".equals(active) || "true".equals(active))) {
            return null;
        }
        Orientation orientation = orientation(properties, false);
        if (orientation == null) {
            return null;
        }
        String model = Boolean.parseBoolean(active)
                ? MeRequester143Catalog.REQUESTER_ACTIVE_MODEL
                : MeRequester143Catalog.REQUESTER_MODEL;
        return variant(model, orientation);
    }

    private static Variant ioPort(Map<String, String> properties) {
        if (!properties.keySet().equals(expectedKeys(properties, "powered", true))) {
            return null;
        }
        String powered = properties.get("powered");
        if (!("false".equals(powered) || "true".equals(powered))) {
            return null;
        }
        Orientation orientation = orientation(properties, true);
        if (orientation == null) {
            return null;
        }
        String model = Boolean.parseBoolean(powered)
                ? "expandedae:block/exp_io_port_on"
                : "expandedae:block/exp_io_port";
        return variant(model, orientation);
    }

    private static java.util.Set<String> expectedKeys(
            Map<String, String> properties,
            String booleanProperty,
            boolean horizontalSpinRequired
    ) {
        String facing = properties.get("facing");
        boolean vertical = "up".equals(facing) || "down".equals(facing);
        return vertical || horizontalSpinRequired
                ? java.util.Set.of(booleanProperty, "facing", "spin")
                : java.util.Set.of(booleanProperty, "facing");
    }

    private static Orientation orientation(
            Map<String, String> properties,
            boolean horizontalSpinRequired
    ) {
        String facing = properties.get("facing");
        if (facing == null) {
            return null;
        }
        boolean vertical = "up".equals(facing) || "down".equals(facing);
        int spin = 0;
        if (vertical || horizontalSpinRequired) {
            String rawSpin = properties.get("spin");
            if (rawSpin == null || rawSpin.length() != 1
                    || rawSpin.charAt(0) < '0' || rawSpin.charAt(0) > '3') {
                return null;
            }
            spin = rawSpin.charAt(0) - '0';
        } else if (properties.containsKey("spin")) {
            return null;
        }

        return switch (facing.toLowerCase(Locale.ROOT)) {
            case "north" -> new Orientation(0, 0, turn(-90 * spin));
            case "east" -> new Orientation(0, 90, turn(-90 * spin));
            case "south" -> new Orientation(0, 180, turn(90 * spin));
            case "west" -> new Orientation(0, 270, turn(-90 * spin));
            case "up" -> new Orientation(270, 0, turn(180 - 90 * spin));
            case "down" -> new Orientation(90, 0, turn(-90 * spin));
            default -> null;
        };
    }

    private static int turn(int degrees) {
        return Math.floorMod(degrees, 360);
    }

    private static Variant variant(String modelId, Orientation orientation) {
        return new Variant(
                new ResourcePath<Model>(modelId),
                orientation.x(),
                orientation.y(),
                orientation.z()
        );
    }

    record Orientation(int x, int y, int z) {

        Orientation {
            if (!quarterTurn(x) || !quarterTurn(y) || !quarterTurn(z)) {
                throw new IllegalArgumentException("orientation must use quarter turns");
            }
        }

        private static boolean quarterTurn(int value) {
            return value >= 0 && value < 360 && value % 90 == 0;
        }
    }
}
