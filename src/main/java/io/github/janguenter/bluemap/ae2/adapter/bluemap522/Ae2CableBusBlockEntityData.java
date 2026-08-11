/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.adapter.bluemap522;

import de.bluecolored.bluemap.core.world.mca.blockentity.MCABlockEntity;
import de.bluecolored.bluenbt.NBTDeserializer;
import de.bluecolored.bluenbt.NBTName;

import java.util.Map;
import java.util.Collections;
import java.util.EnumMap;

import io.github.janguenter.bluemap.ae2.model.Direction6;

/** BlueNBT DTO retaining exactly the bounded AE2 cable-bus structural fields. */
public final class Ae2CableBusBlockEntityData extends MCABlockEntity {

    @NBTName("hasRedstone")
    private Integer hasRedstone;

    @NBTDeserializer(Ae2CablePartDeserializer.class)
    private Object cable;

    @NBTDeserializer(Ae2FacePartDeserializer.class)
    private Object down;

    @NBTDeserializer(Ae2FacePartDeserializer.class)
    private Object up;

    @NBTDeserializer(Ae2FacePartDeserializer.class)
    private Object north;

    @NBTDeserializer(Ae2FacePartDeserializer.class)
    private Object south;

    @NBTDeserializer(Ae2FacePartDeserializer.class)
    private Object west;

    @NBTDeserializer(Ae2FacePartDeserializer.class)
    private Object east;

    @NBTName("facadeDown")
    @NBTDeserializer(Ae2FacadeDeserializer.class)
    private Object facadeDown;

    @NBTName("facadeUp")
    @NBTDeserializer(Ae2FacadeDeserializer.class)
    private Object facadeUp;

    @NBTName("facadeNorth")
    @NBTDeserializer(Ae2FacadeDeserializer.class)
    private Object facadeNorth;

    @NBTName("facadeSouth")
    @NBTDeserializer(Ae2FacadeDeserializer.class)
    private Object facadeSouth;

    @NBTName("facadeWest")
    @NBTDeserializer(Ae2FacadeDeserializer.class)
    private Object facadeWest;

    @NBTName("facadeEast")
    @NBTDeserializer(Ae2FacadeDeserializer.class)
    private Object facadeEast;

    public Ae2CableBusBlockEntityData() {
    }

    public Integer getHasRedstone() {
        return hasRedstone;
    }

    public Object getCable() {
        return cable;
    }

    public boolean hasAttachmentsOrFacades() {
        return down != null || up != null || north != null || south != null
                || west != null || east != null
                || facadeDown != null || facadeUp != null || facadeNorth != null
                || facadeSouth != null || facadeWest != null || facadeEast != null;
    }

    public Map<Direction6, Object> getFaceParts() {
        EnumMap<Direction6, Object> values = new EnumMap<>(Direction6.class);
        putPresent(values, Direction6.DOWN, down);
        putPresent(values, Direction6.UP, up);
        putPresent(values, Direction6.NORTH, north);
        putPresent(values, Direction6.SOUTH, south);
        putPresent(values, Direction6.WEST, west);
        putPresent(values, Direction6.EAST, east);
        return Collections.unmodifiableMap(values);
    }

    public Map<Direction6, Object> getFacades() {
        EnumMap<Direction6, Object> values = new EnumMap<>(Direction6.class);
        putPresent(values, Direction6.DOWN, facadeDown);
        putPresent(values, Direction6.UP, facadeUp);
        putPresent(values, Direction6.NORTH, facadeNorth);
        putPresent(values, Direction6.SOUTH, facadeSouth);
        putPresent(values, Direction6.WEST, facadeWest);
        putPresent(values, Direction6.EAST, facadeEast);
        return Collections.unmodifiableMap(values);
    }

    boolean retainsProbeFields() {
        return Integer.valueOf(2).equals(hasRedstone)
                && hasId(cable, "ae2:fluix_glass_cable")
                && hasPart(down, "ae2:terminal", 0)
                && hasPart(up, "ae2:terminal", 1)
                && hasPart(north, "ae2:terminal", 2)
                && hasPart(south, "ae2:terminal", 3)
                && hasPart(west, "ae2:terminal", 0)
                && hasPart(east, "ae2:terminal", 1)
                && hasFacade(facadeDown, "minecraft:stone")
                && hasFacade(facadeUp, "minecraft:stone")
                && hasFacade(facadeNorth, "minecraft:stone")
                && hasFacade(facadeSouth, "minecraft:stone")
                && hasFacade(facadeWest, "minecraft:stone")
                && hasFacade(facadeEast, "minecraft:stone");
    }

    boolean retainsNativeStructuralProbeFields() {
        return Integer.valueOf(2).equals(hasRedstone)
                && hasId(cable, "ae2:fluix_glass_cable")
                && hasPart(down, "ae2:terminal", 0)
                && hasFrequencyPart(up, "ae2:me_p2p_tunnel", (short) -1)
                && hasFacadeState(
                        facadeNorth,
                        "minecraft:oak_log",
                        Map.of("axis", "x")
                );
    }

    private static boolean hasId(Object value, String expected) {
        return value instanceof Map<?, ?> map && expected.equals(map.get("id"));
    }

    private static boolean hasPart(Object value, String expectedId, int expectedSpin) {
        return value instanceof Map<?, ?> map
                && expectedId.equals(map.get("id"))
                && Byte.valueOf((byte) expectedSpin).equals(map.get("spin"));
    }

    private static boolean hasFacade(Object value, String expectedName) {
        return value instanceof Map<?, ?> map
                && expectedName.equals(map.get("Name"))
                && !map.containsKey("Properties");
    }

    private static boolean hasFrequencyPart(
            Object value,
            String expectedId,
            short expectedFrequency
    ) {
        return value instanceof Map<?, ?> map
                && expectedId.equals(map.get("id"))
                && Short.valueOf(expectedFrequency).equals(map.get("freq"));
    }

    private static boolean hasFacadeState(
            Object value,
            String expectedName,
            Map<String, String> expectedProperties
    ) {
        return value instanceof Map<?, ?> map
                && expectedName.equals(map.get("Name"))
                && expectedProperties.equals(map.get("Properties"));
    }

    private static void putPresent(
            EnumMap<Direction6, Object> values,
            Direction6 direction,
            Object value
    ) {
        if (value != null) {
            values.put(direction, value);
        }
    }

}
