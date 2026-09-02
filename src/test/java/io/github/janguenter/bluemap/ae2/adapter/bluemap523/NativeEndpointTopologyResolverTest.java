/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.adapter.bluemap523;

import de.bluecolored.bluemap.core.map.hires.RenderSettings;
import de.bluecolored.bluemap.core.map.mask.Mask;
import de.bluecolored.bluemap.core.resources.pack.PackVersion;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.util.Key;
import de.bluecolored.bluemap.core.world.BlockEntity;
import de.bluecolored.bluemap.core.world.BlockState;
import de.bluecolored.bluemap.core.world.DimensionType;
import de.bluecolored.bluemap.core.world.LightData;
import de.bluecolored.bluemap.core.world.biome.Biome;
import de.bluecolored.bluemap.core.world.block.BlockAccess;
import de.bluecolored.bluemap.core.world.block.ExtendedBlock;
import io.github.janguenter.bluemap.ae2.model.Direction6;
import io.github.janguenter.bluemap.ae2.model.NativeEndpointCatalog;
import io.github.janguenter.bluemap.ae2.profile.Ae219217NativeStructuralProfile;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NativeEndpointTopologyResolverTest {

    private static final Position ORIGIN = new Position(0, 80, 0);
    private static final Key WRONG_ENTITY = Key.parse("test:wrong_entity");

    @Test
    void exhaustsAllFiveHundredThirtyFourEndpointStatesAcrossAllSixSides() {
        int states = 0;
        int observations = 0;
        for (NativeEndpointCatalog.Definition definition
                : NativeEndpointCatalog.definitions()) {
            List<Map<String, String>> endpointStates = cartesianStates(
                    Ae219217NativeStructuralProfile.endpointStateSchema(
                            definition.blockId()
                    )
            );
            for (Map<String, String> properties : endpointStates) {
                BlockState state = new BlockState(
                        Key.parse(definition.blockId()),
                        properties
                );
                for (Direction6 endpointSide : Direction6.values()) {
                    Fixture fixture = fixture(definition, endpointSide, state);
                    assertEquals(
                            expected(definition, endpointSide, state),
                            NativeEndpointTopologyResolver.resolve(
                                    fixture.endpoint(),
                                    definition,
                                    endpointSide
                            ),
                            definition.blockId() + "/" + properties + "/" + endpointSide
                    );
                    observations++;
                }
                states++;
            }
        }
        assertEquals(534, states);
        assertEquals(
                Ae219217NativeStructuralProfile.ENDPOINT_STATE_COMBINATION_COUNT,
                states
        );
        assertEquals(3_204, observations);
        assertEquals(
                Ae219217NativeStructuralProfile.ENDPOINT_STATE_SIDE_COMBINATION_COUNT,
                observations
        );
    }

    @Test
    void missingOrWrongBlockEntityAndMalformedRequiredStateAreUnknown() {
        NativeEndpointCatalog.Definition all = definition("ae2:controller");
        Fixture missing = fixture(all, Direction6.WEST, true);
        missing.entities().remove(ORIGIN);
        assertEquals(
                NativeEndpointTopologyResolver.Status.UNKNOWN,
                resolve(missing, all, Direction6.WEST)
        );

        Fixture wrong = fixture(all, Direction6.WEST, true);
        wrong.entities().put(ORIGIN, entity(WRONG_ENTITY, ORIGIN));
        assertEquals(
                NativeEndpointTopologyResolver.Status.UNKNOWN,
                resolve(wrong, all, Direction6.WEST)
        );

        Fixture nullId = fixture(all, Direction6.WEST, true);
        nullId.entities().put(ORIGIN, entity(null, ORIGIN));
        assertEquals(
                NativeEndpointTopologyResolver.Status.UNKNOWN,
                resolve(nullId, all, Direction6.WEST)
        );

        NativeEndpointCatalog.Definition wireless = definition(
                "ae2:wireless_access_point"
        );
        Fixture malformed = fixture(wireless, Direction6.WEST, true);
        malformed.states().put(ORIGIN, BlockState.fromString(
                "ae2:wireless_access_point[facing=east,state=future,waterlogged=false]"
        ));
        assertEquals(
                NativeEndpointTopologyResolver.Status.UNKNOWN,
                resolve(malformed, wireless, Direction6.WEST)
        );
    }

    @Test
    void exactUnformedAndKnownSideExclusionsAreDisconnected() {
        NativeEndpointCatalog.Definition crafting = definition("ae2:crafting_unit");
        Fixture unformed = fixture(crafting, Direction6.NORTH, false);
        assertEquals(
                NativeEndpointTopologyResolver.Status.DISCONNECTED,
                resolve(unformed, crafting, Direction6.NORTH)
        );

        NativeEndpointCatalog.Definition ring = definition("ae2:quantum_ring");
        Fixture unformedRing = fixture(ring, Direction6.EAST, false);
        assertEquals(
                NativeEndpointTopologyResolver.Status.DISCONNECTED,
                resolve(unformedRing, ring, Direction6.EAST)
        );

        NativeEndpointCatalog.Definition drive = definition("ae2:drive");
        Fixture front = fixture(drive, Direction6.EAST, true);
        assertEquals(
                NativeEndpointTopologyResolver.Status.DISCONNECTED,
                resolve(front, drive, Direction6.EAST)
        );

        NativeEndpointCatalog.Definition provider = definition("ae2:pattern_provider");
        Fixture pushSide = fixture(provider, Direction6.EAST, true);
        assertEquals(
                NativeEndpointTopologyResolver.Status.DISCONNECTED,
                resolve(pushSide, provider, Direction6.EAST)
        );
    }

    @Test
    void malformedOrIncompleteFormedTopologyIsUnknownAtomically() {
        NativeEndpointCatalog.Definition ring = definition("ae2:quantum_ring");
        Fixture incompleteBridge = fixture(ring, Direction6.EAST, true);
        Position requiredMember = ORIGIN.plus(-2, 1, 0);
        incompleteBridge.states().put(requiredMember, BlockState.MISSING);
        incompleteBridge.entities().remove(requiredMember);
        assertEquals(
                NativeEndpointTopologyResolver.Status.UNKNOWN,
                resolve(incompleteBridge, ring, Direction6.EAST)
        );

        NativeEndpointCatalog.Definition pylon = definition("ae2:spatial_pylon");
        Fixture malformedPylon = fixture(pylon, Direction6.EAST, true);
        Position neighbor = ORIGIN.plus(0, 1, 0);
        malformedPylon.entities().put(neighbor, entity(WRONG_ENTITY, neighbor));
        assertEquals(
                NativeEndpointTopologyResolver.Status.UNKNOWN,
                resolve(malformedPylon, pylon, Direction6.EAST)
        );

        Fixture nullIdPylon = fixture(pylon, Direction6.EAST, true);
        nullIdPylon.entities().put(neighbor, entity(null, neighbor));
        assertEquals(
                NativeEndpointTopologyResolver.Status.UNKNOWN,
                resolve(nullIdPylon, pylon, Direction6.EAST)
        );

        Position distant = ORIGIN.plus(0, 2, 0);
        Fixture distantNullIdPylon = fixture(pylon, Direction6.EAST, true);
        distantNullIdPylon.states().put(distant, BlockState.fromString(
                "ae2:spatial_pylon[powered_on=false]"
        ));
        distantNullIdPylon.entities().put(distant, entity(null, distant));
        assertEquals(
                NativeEndpointTopologyResolver.Status.UNKNOWN,
                resolve(distantNullIdPylon, pylon, Direction6.EAST)
        );
    }

    @Test
    void everyEndpointRejectsExtraAndOutOfDomainPersistedState() {
        int schemas = 0;
        for (NativeEndpointCatalog.Definition definition
                : NativeEndpointCatalog.definitions()) {
            Fixture extra = fixture(definition, Direction6.WEST, true);
            Map<String, String> extraProperties = new HashMap<>(
                    extra.states().get(ORIGIN).getProperties()
            );
            extraProperties.put("future", "false");
            extra.states().put(ORIGIN, new BlockState(
                    Key.parse(definition.blockId()),
                    extraProperties
            ));
            assertEquals(
                    NativeEndpointTopologyResolver.Status.UNKNOWN,
                    resolve(extra, definition, Direction6.WEST),
                    definition.blockId()
            );

            Map<String, java.util.List<String>> schema =
                    Ae219217NativeStructuralProfile.endpointStateSchema(
                            definition.blockId()
                    );
            if (!schema.isEmpty()) {
                String mutatedKey = schema.keySet().iterator().next();
                Fixture missing = fixture(definition, Direction6.WEST, true);
                Map<String, String> missingProperties = new HashMap<>(
                        missing.states().get(ORIGIN).getProperties()
                );
                missingProperties.remove(mutatedKey);
                missing.states().put(ORIGIN, new BlockState(
                        Key.parse(definition.blockId()),
                        missingProperties
                ));
                assertEquals(
                        NativeEndpointTopologyResolver.Status.UNKNOWN,
                        resolve(missing, definition, Direction6.WEST),
                        definition.blockId()
                );

                Fixture invalid = fixture(definition, Direction6.WEST, true);
                Map<String, String> invalidProperties = new HashMap<>(
                        invalid.states().get(ORIGIN).getProperties()
                );
                invalidProperties.put(mutatedKey, "not_valid");
                invalid.states().put(ORIGIN, new BlockState(
                        Key.parse(definition.blockId()),
                        invalidProperties
                ));
                assertEquals(
                        NativeEndpointTopologyResolver.Status.UNKNOWN,
                        resolve(invalid, definition, Direction6.WEST),
                        definition.blockId()
                );
            }
            schemas++;
        }
        assertEquals(30, schemas);
    }

    private static NativeEndpointTopologyResolver.Status expected(
            NativeEndpointCatalog.Definition definition,
            Direction6 endpointSide,
            BlockState state
    ) {
        Map<String, String> properties = state.getProperties();
        Direction6 facing = properties.containsKey("facing")
                ? Direction6.valueOf(properties.get("facing").toUpperCase(
                        java.util.Locale.ROOT
                )) : null;
        return switch (definition.sidePolicy()) {
            case ALL, VALID_STRAIGHT_PYLON ->
                    NativeEndpointTopologyResolver.Status.CONNECTED;
            case BACK -> endpointSide == facing.opposite()
                    ? NativeEndpointTopologyResolver.Status.CONNECTED
                    : NativeEndpointTopologyResolver.Status.DISCONNECTED;
            case NO_FRONT -> endpointSide == facing
                    ? NativeEndpointTopologyResolver.Status.DISCONNECTED
                    : NativeEndpointTopologyResolver.Status.CONNECTED;
            case FRONT_BACK -> endpointSide == facing
                    || endpointSide == facing.opposite()
                    ? NativeEndpointTopologyResolver.Status.CONNECTED
                    : NativeEndpointTopologyResolver.Status.DISCONNECTED;
            case PUSH_DIRECTION -> "all".equals(properties.get("push_direction"))
                    || endpointSide != Direction6.valueOf(
                            properties.get("push_direction").toUpperCase(
                                    java.util.Locale.ROOT
                            )
                    ) ? NativeEndpointTopologyResolver.Status.CONNECTED
                    : NativeEndpointTopologyResolver.Status.DISCONNECTED;
            case FORMED_CRAFTING -> "true".equals(properties.get("formed"))
                    ? NativeEndpointTopologyResolver.Status.CONNECTED
                    : NativeEndpointTopologyResolver.Status.DISCONNECTED;
            case FORMED_QUANTUM -> "false".equals(properties.get("formed"))
                    ? NativeEndpointTopologyResolver.Status.DISCONNECTED
                    : definition.blockId().equals("ae2:quantum_ring")
                    ? NativeEndpointTopologyResolver.Status.CONNECTED
                    : NativeEndpointTopologyResolver.Status.DISCONNECTED;
        };
    }

    private static Fixture fixture(
            NativeEndpointCatalog.Definition definition,
            Direction6 endpointSide,
            boolean formed
    ) {
        BlockState targetState = switch (definition.sidePolicy()) {
            case FORMED_QUANTUM -> BlockState.fromString(
                    definition.blockId() + "[formed=" + formed + ",waterlogged=false]"
            );
            case VALID_STRAIGHT_PYLON -> BlockState.fromString(
                    definition.blockId() + "[powered_on=false]"
            );
            default -> state(definition, formed);
        };
        return fixture(definition, endpointSide, targetState);
    }

    private static Fixture fixture(
            NativeEndpointCatalog.Definition definition,
            Direction6 endpointSide,
            BlockState targetState
    ) {
        Map<Position, BlockState> states = new HashMap<>();
        Map<Position, BlockEntity> entities = new HashMap<>();
        switch (definition.sidePolicy()) {
            case FORMED_QUANTUM -> quantum(
                    states,
                    entities,
                    definition.blockId().equals("ae2:quantum_ring"),
                    endpointSide,
                    targetState
            );
            case VALID_STRAIGHT_PYLON -> pylon(
                    states,
                    entities,
                    endpointSide,
                    targetState
            );
            default -> {
                states.put(ORIGIN, targetState);
                entities.put(ORIGIN, entity(
                        Key.parse(definition.blockEntityId()),
                        ORIGIN
                ));
            }
        }
        TestBlockAccess access = new TestBlockAccess(states, entities);
        ExtendedBlock endpoint = new ExtendedBlock(
                access,
                new ResourcePack(new PackVersion(34, 0)),
                TEST_SETTINGS,
                DimensionType.OVERWORLD
        );
        endpoint.set(ORIGIN.x(), ORIGIN.y(), ORIGIN.z());
        return new Fixture(states, entities, endpoint);
    }

    private static BlockState state(
            NativeEndpointCatalog.Definition definition,
            boolean formed
    ) {
        Map<String, java.util.List<String>> schema =
                Ae219217NativeStructuralProfile.endpointStateSchema(
                        definition.blockId()
                );
        Map<String, String> properties = new HashMap<>();
        schema.forEach((key, values) -> properties.put(key, values.getFirst()));
        putIfPresent(properties, "facing", definition.sidePolicy()
                == NativeEndpointCatalog.SidePolicy.FORMED_CRAFTING
                ? "north" : "east");
        if ("ae2:wireless_access_point".equals(definition.blockId())) {
            putIfPresent(properties, "state", "off");
        } else if ("ae2:controller".equals(definition.blockId())) {
            putIfPresent(properties, "state", "offline");
        }
        putIfPresent(properties, "waterlogged", "false");
        putIfPresent(properties, "spin", "0");
        putIfPresent(properties, "powered", "false");
        putIfPresent(properties, "formed", Boolean.toString(formed));
        putIfPresent(properties, "push_direction", "east");
        return switch (definition.sidePolicy()) {
            case BACK, NO_FRONT, FRONT_BACK, PUSH_DIRECTION, FORMED_CRAFTING,
                    ALL -> new BlockState(Key.parse(definition.blockId()), properties);
            case FORMED_QUANTUM, VALID_STRAIGHT_PYLON -> throw new AssertionError();
        };
    }

    private static void putIfPresent(
            Map<String, String> properties,
            String key,
            String value
    ) {
        if (properties.containsKey(key)) {
            properties.put(key, value);
        }
    }

    private static void quantum(
            Map<Position, BlockState> states,
            Map<Position, BlockEntity> entities,
            boolean targetIsRing,
            Direction6 endpointSide,
            BlockState targetState
    ) {
        boolean formed = "true".equals(targetState.getProperties().get("formed"));
        if (!formed) {
            states.put(ORIGIN, targetState);
            entities.put(ORIGIN, entity(Key.parse("ae2:quantum_ring"), ORIGIN));
            return;
        }

        Vector first;
        Vector second;
        Position link;
        if (targetIsRing) {
            first = Vector.of(endpointSide);
            second = perpendicular(first);
            link = ORIGIN.plus(-first.x(), -first.y(), -first.z());
        } else {
            Vector normal = Vector.of(endpointSide);
            first = perpendicular(normal);
            second = perpendicular(first, normal);
            link = ORIGIN;
        }
        for (int a = -1; a <= 1; a++) {
            for (int b = -1; b <= 1; b++) {
                Position position = link.plus(
                        a * first.x() + b * second.x(),
                        a * first.y() + b * second.y(),
                        a * first.z() + b * second.z()
                );
                boolean ring = a != 0 || b != 0;
                states.put(position, BlockState.fromString(
                        (ring ? "ae2:quantum_ring" : "ae2:quantum_link")
                                + "[formed=true,waterlogged=false]"
                ));
                entities.put(position, entity(Key.parse("ae2:quantum_ring"), position));
            }
        }
        states.put(ORIGIN, targetState);
    }

    private static void pylon(
            Map<Position, BlockState> states,
            Map<Position, BlockEntity> entities,
            Direction6 endpointSide,
            BlockState targetState
    ) {
        Vector axis = perpendicular(Vector.of(endpointSide));
        for (int distance = -1; distance <= 1; distance++) {
            Position position = ORIGIN.plus(
                    distance * axis.x(),
                    distance * axis.y(),
                    distance * axis.z()
            );
            states.put(position, BlockState.fromString(
                    "ae2:spatial_pylon[powered_on=false]"
            ));
            entities.put(position, entity(Key.parse("ae2:spatial_pylon"), position));
        }
        states.put(ORIGIN, targetState);
    }

    private static List<Map<String, String>> cartesianStates(
            Map<String, List<String>> schema
    ) {
        List<Map<String, String>> states = new ArrayList<>();
        states.add(new LinkedHashMap<>());
        for (Map.Entry<String, List<String>> property : schema.entrySet()) {
            List<Map<String, String>> expanded = new ArrayList<>();
            for (Map<String, String> state : states) {
                for (String value : property.getValue()) {
                    LinkedHashMap<String, String> candidate = new LinkedHashMap<>(state);
                    candidate.put(property.getKey(), value);
                    expanded.add(candidate);
                }
            }
            states = expanded;
        }
        return states.stream().map(state ->
                java.util.Collections.unmodifiableMap(new LinkedHashMap<>(state))
        ).toList();
    }

    private static Vector perpendicular(Vector vector) {
        return vector.x() == 0 ? new Vector(1, 0, 0) : new Vector(0, 1, 0);
    }

    private static Vector perpendicular(Vector first, Vector second) {
        return new Vector(
                first.y() * second.z() - first.z() * second.y(),
                first.z() * second.x() - first.x() * second.z(),
                first.x() * second.y() - first.y() * second.x()
        );
    }

    private static NativeEndpointCatalog.Definition definition(String blockId) {
        return NativeEndpointCatalog.find(blockId).orElseThrow();
    }

    private static NativeEndpointTopologyResolver.Status resolve(
            Fixture fixture,
            NativeEndpointCatalog.Definition definition,
            Direction6 endpointSide
    ) {
        return NativeEndpointTopologyResolver.resolve(
                fixture.endpoint(),
                definition,
                endpointSide
        );
    }

    private static BlockEntity entity(Key id, Position position) {
        return new TestBlockEntity(id, position.x(), position.y(), position.z());
    }

    private static final RenderSettings TEST_SETTINGS = new RenderSettings() {
        @Override
        public int getRemoveCavesBelowY() {
            return Integer.MIN_VALUE;
        }

        @Override
        public int getCaveDetectionOceanFloor() {
            return 0;
        }

        @Override
        public boolean isCaveDetectionUsesBlockLight() {
            return false;
        }

        @Override
        public float getAmbientLight() {
            return 0F;
        }

        @Override
        public boolean isRenderEdges() {
            return false;
        }

        @Override
        public Mask getRenderMask() {
            return Mask.ALL;
        }

        @Override
        public boolean isSaveHiresLayer() {
            return false;
        }

        @Override
        public boolean isRenderTopOnly() {
            return false;
        }
    };

    private static final class TestBlockAccess implements BlockAccess {
        private final Map<Position, BlockState> states;
        private final Map<Position, BlockEntity> entities;
        private int x;
        private int y;
        private int z;

        private TestBlockAccess(
                Map<Position, BlockState> states,
                Map<Position, BlockEntity> entities
        ) {
            this.states = states;
            this.entities = entities;
        }

        @Override
        public void set(int newX, int newY, int newZ) {
            x = newX;
            y = newY;
            z = newZ;
        }

        @Override
        public BlockAccess copy() {
            return new TestBlockAccess(states, entities);
        }

        @Override
        public int getX() {
            return x;
        }

        @Override
        public int getY() {
            return y;
        }

        @Override
        public int getZ() {
            return z;
        }

        @Override
        public BlockState getBlockState() {
            return states.getOrDefault(new Position(x, y, z), BlockState.AIR);
        }

        @Override
        public LightData getLightData() {
            return new LightData(15, 0);
        }

        @Override
        public Biome getBiome() {
            return Biome.DEFAULT;
        }

        @Override
        public BlockEntity getBlockEntity() {
            return entities.get(new Position(x, y, z));
        }

        @Override
        public boolean hasOceanFloorY() {
            return false;
        }

        @Override
        public int getOceanFloorY() {
            return 0;
        }
    }

    private record TestBlockEntity(Key id, int x, int y, int z) implements BlockEntity {
        @Override
        public Key getId() {
            return id;
        }

        @Override
        public int getX() {
            return x;
        }

        @Override
        public int getY() {
            return y;
        }

        @Override
        public int getZ() {
            return z;
        }

        @Override
        public boolean isKeepPacked() {
            return false;
        }
    }

    private record Fixture(
            Map<Position, BlockState> states,
            Map<Position, BlockEntity> entities,
            ExtendedBlock endpoint
    ) {
    }

    private record Position(int x, int y, int z) {
        private Position plus(int dx, int dy, int dz) {
            return new Position(x + dx, y + dy, z + dz);
        }
    }

    private record Vector(int x, int y, int z) {
        private static Vector of(Direction6 direction) {
            return new Vector(direction.stepX(), direction.stepY(), direction.stepZ());
        }
    }
}
