/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/** Closed exact catalog of all 85 AE2 19.2.17 cable part IDs. */
public final class Ae2CableCatalog {

    public static final String FLUIX_GLASS_CABLE = "ae2:fluix_glass_cable";
    public static final String SMART_CHANNELS_OFF_ODD =
            "ae2:part/cable/smart/channels_00";
    public static final String SMART_CHANNELS_OFF_EVEN =
            "ae2:part/cable/smart/channels_10";
    public static final String DENSE_SMART_CHANNELS_OFF_ODD =
            "ae2:part/cable/dense_smart/channels_00";
    public static final String DENSE_SMART_CHANNELS_OFF_EVEN =
            "ae2:part/cable/dense_smart/channels_10";

    private static final List<CableDefinition> DEFINITIONS = buildDefinitions();
    private static final Map<String, CableDefinition> BY_ID = indexById();
    private static final Set<String> IDS = Collections.unmodifiableSet(
            new LinkedHashSet<>(BY_ID.keySet())
    );
    private static final Set<String> TEXTURES = buildTextures();

    private Ae2CableCatalog() {
    }

    public static Optional<CableDefinition> find(String id) {
        return Optional.ofNullable(BY_ID.get(id));
    }

    public static CableDefinition require(String id) {
        CableDefinition definition = BY_ID.get(id);
        if (definition == null) {
            throw new IllegalArgumentException("unsupported AE2 cable ID: " + id);
        }
        return definition;
    }

    public static List<CableDefinition> definitions() {
        return DEFINITIONS;
    }

    public static Set<String> ids() {
        return IDS;
    }

    public static Set<String> textures() {
        return TEXTURES;
    }

    private static List<CableDefinition> buildDefinitions() {
        List<CableDefinition> definitions = new ArrayList<>(
                CableFamily.values().length * CableColor.values().length
        );
        for (CableFamily family : CableFamily.values()) {
            for (CableColor color : CableColor.values()) {
                definitions.add(new CableDefinition(
                        "ae2:" + color.registryPrefix() + "_" + family.idSuffix(),
                        family,
                        color
                ));
            }
        }
        return List.copyOf(definitions);
    }

    private static Map<String, CableDefinition> indexById() {
        Map<String, CableDefinition> definitions = new LinkedHashMap<>();
        for (CableDefinition definition : DEFINITIONS) {
            if (definitions.put(definition.id(), definition) != null) {
                throw new IllegalStateException("duplicate AE2 cable ID " + definition.id());
            }
        }
        return Collections.unmodifiableMap(definitions);
    }

    private static Set<String> buildTextures() {
        Set<String> textures = new LinkedHashSet<>();
        for (CableFamily family : List.of(
                CableFamily.GLASS,
                CableFamily.COVERED,
                CableFamily.DENSE_SMART
        )) {
            for (CableColor color : CableColor.values()) {
                textures.add("ae2:part/cable/core/"
                        + family.coreTextureFolder() + "/" + color.textureName());
            }
        }
        for (CableFamily family : CableFamily.values()) {
            for (CableColor color : CableColor.values()) {
                textures.add("ae2:part/cable/"
                        + family.connectionTextureFolder() + "/" + color.textureName());
            }
        }
        textures.add(SMART_CHANNELS_OFF_ODD);
        textures.add(SMART_CHANNELS_OFF_EVEN);
        textures.add(DENSE_SMART_CHANNELS_OFF_ODD);
        textures.add(DENSE_SMART_CHANNELS_OFF_EVEN);
        return Collections.unmodifiableSet(textures);
    }
}
