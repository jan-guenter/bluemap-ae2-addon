/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.profile;

import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

/** Operator-controlled, restart-scoped profile disablement. */
public final class ProfileDisablement {

    public static final String SYSTEM_PROPERTY = "bluemap.ae2.disabledProfiles";
    public static final String ENVIRONMENT_VARIABLE = "BLUEMAP_AE2_DISABLED_PROFILES";
    private static final Pattern PROFILE_ID = Pattern.compile("[a-z0-9_.-]+");

    private final Set<String> disabledProfiles;

    private ProfileDisablement(Set<String> disabledProfiles) {
        this.disabledProfiles = Set.copyOf(disabledProfiles);
    }

    public static ProfileDisablement current() {
        return from(
                System.getProperty(SYSTEM_PROPERTY),
                System.getenv(ENVIRONMENT_VARIABLE)
        );
    }

    public static ProfileDisablement from(String propertyValue, String environmentValue) {
        TreeSet<String> profiles = new TreeSet<>();
        addCsv(profiles, propertyValue);
        addCsv(profiles, environmentValue);
        return new ProfileDisablement(profiles);
    }

    public boolean isDisabled(String profileId) {
        Objects.requireNonNull(profileId, "profileId");
        return disabledProfiles.contains(profileId.toLowerCase(Locale.ROOT));
    }

    public Set<String> disabledProfiles() {
        return disabledProfiles;
    }

    private static void addCsv(Set<String> output, String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            return;
        }
        for (String token : rawValue.split(",", -1)) {
            String normalized = token.trim().toLowerCase(Locale.ROOT);
            if (PROFILE_ID.matcher(normalized).matches()) {
                output.add(normalized);
            }
        }
    }
}
