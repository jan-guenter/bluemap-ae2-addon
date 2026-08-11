/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.profile.merequester;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Closed block and part catalog proven for ME Requester 1.21.1-1.4.3. */
public final class MeRequester143Catalog {

    public static final String REQUESTER_BLOCK = "merequester:requester";
    public static final String REQUESTER_BLOCK_ENTITY = "merequester:requester";
    public static final String REQUESTER_MODEL = "merequester:block/requester";
    public static final String REQUESTER_ACTIVE_MODEL =
            "merequester:block/requester_active";
    public static final String REQUESTER_TERMINAL_PART =
            "merequester:requester_terminal";
    public static final String REQUESTER_TERMINAL_OFF_MODEL =
            "merequester:part/requester_terminal_off";
    public static final String REQUESTER_TERMINAL_ON_MODEL =
            "merequester:part/requester_terminal_on";
    public static final String ACTIVE_PROPERTY = "active";
    public static final String FACING_PROPERTY = "facing";
    public static final String SPIN_PROPERTY = "spin";
    public static final String NAMESPACED_Z_KEY = "ae2:z";
    public static final int NO_SPIN = -1;
    public static final int MIN_SPIN = 0;
    public static final int MAX_SPIN = 3;
    public static final String TERMINAL_STATUS_POLICY = "static-offline-unknown";

    private static final Set<String> FACINGS = Set.of(
            "down", "up", "north", "south", "west", "east"
    );
    private static final List<String> TERMINAL_OFF_MODEL_STACK = List.of(
            "ae2:part/display_base",
            REQUESTER_TERMINAL_OFF_MODEL,
            "ae2:part/display_status_off"
    );
    private static final Map<String, RequesterVariant> REQUESTER_VARIANTS =
            buildRequesterVariants();

    private MeRequester143Catalog() {
    }

    /** Exact 24 saved-blockstate variants, including AE2's nonstandard Z rotation. */
    public static Map<String, RequesterVariant> requesterVariants() {
        return REQUESTER_VARIANTS;
    }

    public static RequesterVariant variantForState(String stateKey) {
        return REQUESTER_VARIANTS.get(stateKey);
    }

    public static Set<String> facings() {
        return FACINGS;
    }

    /** Deterministic persisted-state projection used for the terminal face part. */
    public static List<String> terminalOffModelStack() {
        return TERMINAL_OFF_MODEL_STACK;
    }

    private static Map<String, RequesterVariant> buildRequesterVariants() {
        Map<String, RequesterVariant> result = new LinkedHashMap<>();
        for (boolean active : List.of(false, true)) {
            add(result, new RequesterVariant(
                    active, "north", NO_SPIN, model(active), 0, 0, 0
            ));
            add(result, new RequesterVariant(
                    active, "east", NO_SPIN, model(active), 0, 90, 0
            ));
            add(result, new RequesterVariant(
                    active, "south", NO_SPIN, model(active), 0, 180, 0
            ));
            add(result, new RequesterVariant(
                    active, "west", NO_SPIN, model(active), 0, 270, 0
            ));

            int[] upZ = {180, 90, 0, 270};
            int[] downZ = {0, 270, 180, 90};
            for (int spin = MIN_SPIN; spin <= MAX_SPIN; spin++) {
                add(result, new RequesterVariant(
                        active, "up", spin, model(active), 270, 0, upZ[spin]
                ));
                add(result, new RequesterVariant(
                        active, "down", spin, model(active), 90, 0, downZ[spin]
                ));
            }
        }
        if (result.size() != 24
                || result.values().stream().filter(RequesterVariant::requiresAe2Z).count()
                != 12) {
            throw new IllegalStateException("invalid exact ME Requester block variant catalog");
        }
        return Collections.unmodifiableMap(result);
    }

    private static void add(
            Map<String, RequesterVariant> variants,
            RequesterVariant variant
    ) {
        if (variants.put(variant.stateKey(), variant) != null) {
            throw new IllegalStateException("duplicate exact ME Requester block state");
        }
    }

    private static String model(boolean active) {
        return active ? REQUESTER_ACTIVE_MODEL : REQUESTER_MODEL;
    }

    /** Exact blockstate model transform after normalizing {@code ae2:z} to {@code z}. */
    public record RequesterVariant(
            boolean active,
            String facing,
            int spin,
            String modelId,
            int xRotation,
            int yRotation,
            int zRotation
    ) {

        public RequesterVariant {
            if (!FACINGS.contains(facing)
                    || !(REQUESTER_MODEL.equals(modelId)
                    || REQUESTER_ACTIVE_MODEL.equals(modelId))
                    || !quarterTurn(xRotation)
                    || !quarterTurn(yRotation)
                    || !quarterTurn(zRotation)) {
                throw new IllegalArgumentException("invalid exact ME Requester variant");
            }
            boolean vertical = "up".equals(facing) || "down".equals(facing);
            if ((vertical && (spin < MIN_SPIN || spin > MAX_SPIN))
                    || (!vertical && spin != NO_SPIN)) {
                throw new IllegalArgumentException("invalid ME Requester spin domain");
            }
        }

        public String stateKey() {
            String base = ACTIVE_PROPERTY + "=" + active + "," + FACING_PROPERTY
                    + "=" + facing;
            return spin == NO_SPIN ? base : base + "," + SPIN_PROPERTY + "=" + spin;
        }

        public boolean requiresAe2Z() {
            return zRotation != 0;
        }

        private static boolean quarterTurn(int degrees) {
            return degrees >= 0 && degrees < 360 && degrees % 90 == 0;
        }
    }
}
