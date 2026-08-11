/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.adapter.bluemap522;

import de.bluecolored.bluemap.core.world.BlockEntity;
import de.bluecolored.bluemap.core.world.mca.MCAUtil;
import de.bluecolored.bluemap.core.world.mca.blockentity.MCABlockEntity;
import de.bluecolored.bluenbt.BlueNBT;
import de.bluecolored.bluenbt.NBTWriter;
import de.bluecolored.bluenbt.TagType;
import io.github.janguenter.bluemap.ae2.model.CableBusDecodeResult;
import io.github.janguenter.bluemap.ae2.model.CableBusDecoder;
import io.github.janguenter.bluemap.ae2.model.NativeStructuralCableBusDecoder;
import io.github.janguenter.bluemap.ae2.model.NativeStructuralDecodeResult;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Ae2CableBusBlockEntityDataDeserializationTest {

    @Test
    void exactFixtureRetainsOnlyTheCenterIdAndBaseFields() throws Exception {
        byte[] nbt = blockEntity(writer -> {
            writer.name("hasRedstone").value(2);
            writer.name("cable").beginCompound();
            writer.name("visual").beginCompound();
            writer.name("powered").value((byte) 1);
            writer.name("samples").beginList(3, TagType.INT);
            writer.value(1);
            writer.value(2);
            writer.value(3);
            writer.endList();
            writer.name("opaque").value(new byte[]{1, 2, 3, 4});
            writer.endCompound();
            writer.name("id").value("ae2:fluix_glass_cable");
            writer.endCompound();
        });

        Ae2CableBusBlockEntityData data = readDto(nbt);

        assertEquals("ae2:cable_bus", data.getId().toString());
        assertEquals(17, data.getX());
        assertEquals(-23, data.getY());
        assertEquals(41, data.getZ());
        assertEquals(2, data.getHasRedstone());
        assertEquals(Map.of("id", "ae2:fluix_glass_cable"), data.getCable());
        assertFalse(data.hasAttachmentsOrFacades());

        CableBusDecodeResult result = new CableBusDecoder().decode(
                data.getCable(),
                data.hasAttachmentsOrFacades()
        );
        assertTrue(result.isSupported());
    }

    @Test
    void retainsOnlyTheBoundedTerminalAndPlainStoneFacadeProjection() throws Exception {
        byte[] nbt = blockEntity(writer -> {
            writeCable(writer);
            writer.name("north").beginCompound();
            writer.name("id").value("ae2:terminal");
            writer.name("spin").value((byte) 1);
            writer.name("config").beginCompound();
            writer.name("items").beginList(2, TagType.COMPOUND);
            writer.beginCompound();
            writer.name("slot").value(0);
            writer.endCompound();
            writer.beginCompound();
            writer.name("slot").value(1);
            writer.endCompound();
            writer.endList();
            writer.endCompound();
            writer.endCompound();
            writer.name("facadeNorth").beginCompound();
            writer.name("Name").value("minecraft:stone");
            writer.endCompound();
        });

        Ae2CableBusBlockEntityData data = readDto(nbt);

        assertEquals(Map.of("id", "ae2:fluix_glass_cable"), data.getCable());
        assertTrue(data.hasAttachmentsOrFacades());
        assertEquals(
                Map.of("id", "ae2:terminal", "spin", (byte) 1),
                data.getFaceParts().get(io.github.janguenter.bluemap.ae2.model.Direction6.NORTH)
        );
        assertEquals(
                Map.of("Name", "minecraft:stone"),
                data.getFacades().get(io.github.janguenter.bluemap.ae2.model.Direction6.NORTH)
        );

        CableBusDecodeResult result = new CableBusDecoder().decode(
                data.getCable(),
                data.getFaceParts(),
                data.getFacades()
        );
        assertTrue(result.isSupported());
    }

    @Test
    void missingWrongTypeAndOutOfRangeSpinsReachTypedFailClosedStatuses()
            throws Exception {
        for (SpinWriter spin : List.<SpinWriter>of(
                writer -> { },
                writer -> writer.name("spin").value("one"),
                writer -> writer.name("spin").value((byte) 4)
        )) {
            byte[] nbt = blockEntity(writer -> {
                writeCable(writer);
                writer.name("north").beginCompound();
                writer.name("id").value("ae2:terminal");
                spin.write(writer);
                writer.endCompound();
            });

            Ae2CableBusBlockEntityData data = readDto(nbt);
            CableBusDecodeResult result = new CableBusDecoder().decode(
                    data.getCable(),
                    data.getFaceParts(),
                    data.getFacades()
            );

            assertFalse(result.isSupported());
            assertEquals(
                    CableBusDecodeResult.Status.INVALID_FACE_PART_SPIN,
                    result.status()
            );
        }
    }

    @Test
    void propertyBearingFacadeIsRetainedButOutsideThePlainStonePolicy()
            throws Exception {
        byte[] nbt = blockEntity(writer -> {
            writeCable(writer);
            writer.name("south").beginCompound();
            writer.name("id").value("ae2:terminal");
            writer.name("spin").value((byte) 0);
            writer.endCompound();
            writer.name("facadeSouth").beginCompound();
            writer.name("Name").value("minecraft:stone");
            writer.name("Properties").beginCompound();
            writer.name("future").value("false");
            writer.endCompound();
            writer.endCompound();
        });

        Ae2CableBusBlockEntityData data = readDto(nbt);
        CableBusDecodeResult result = new CableBusDecoder().decode(
                data.getCable(),
                data.getFaceParts(),
                data.getFacades()
        );

        assertEquals(
                Map.of(
                        "Name", "minecraft:stone",
                        "Properties", Map.of("future", "false")
                ),
                data.getFacades().get(
                        io.github.janguenter.bluemap.ae2.model.Direction6.SOUTH
                )
        );
        assertEquals(
                CableBusDecodeResult.Status.UNSUPPORTED_FACADE_STATE,
                result.status()
        );
    }

    @Test
    void retainsUnsignedP2pShortAndPersistedFacadePropertiesOnly() throws Exception {
        byte[] nbt = blockEntity(writer -> {
            writer.name("hasRedstone").value(2);
            writeCable(writer);
            writer.name("down").beginCompound();
            writer.name("id").value("ae2:terminal");
            writer.name("spin").value((byte) 0);
            writer.endCompound();
            writer.name("up").beginCompound();
            writer.name("id").value("ae2:me_p2p_tunnel");
            writer.name("freq").value((short) -1);
            writer.name("live").value((byte) 1);
            writer.name("locked").value((byte) 1);
            writer.name("items").beginList(0, TagType.COMPOUND);
            writer.endList();
            writer.endCompound();
            writer.name("facadeNorth").beginCompound();
            writer.name("Name").value("minecraft:oak_log");
            writer.name("Properties").beginCompound();
            writer.name("axis").value("x");
            writer.endCompound();
            writer.endCompound();
        });

        Ae2CableBusBlockEntityData data = readDto(nbt);
        assertEquals(
                Map.of("id", "ae2:me_p2p_tunnel", "freq", (short) -1),
                data.getFaceParts().get(
                        io.github.janguenter.bluemap.ae2.model.Direction6.UP
                )
        );
        assertEquals(
                Map.of(
                        "Name", "minecraft:oak_log",
                        "Properties", Map.of("axis", "x")
                ),
                data.getFacades().get(
                        io.github.janguenter.bluemap.ae2.model.Direction6.NORTH
                )
        );
        assertTrue(data.retainsNativeStructuralProbeFields());
    }

    @Test
    void distinguishesEmptyOccupiedAndMalformedCellDockStacks() throws Exception {
        byte[] nbt = blockEntity(writer -> {
            writeCable(writer);
            writer.name("north").beginCompound();
            writer.name("id").value("megacells:cell_dock");
            writer.name("spin").value((byte) 1);
            writer.name("cell").beginCompound();
            writer.endCompound();
            writer.endCompound();
            writer.name("south").beginCompound();
            writer.name("id").value("megacells:cell_dock");
            writer.name("spin").value((byte) 2);
            writer.name("cell").beginCompound();
            writer.name("id").value("megacells:item_storage_cell_1m");
            writer.name("count").value(1);
            writer.name("components").beginCompound();
            writer.name("ignored").value(7);
            writer.endCompound();
            writer.endCompound();
            writer.endCompound();
            writer.name("west").beginCompound();
            writer.name("id").value("megacells:cell_dock");
            writer.name("spin").value((byte) 0);
            writer.name("cell").beginCompound();
            writer.name("count").value(1);
            writer.endCompound();
            writer.endCompound();
        });

        Ae2CableBusBlockEntityData data = readDto(nbt);
        assertEquals(
                Map.of("id", "megacells:cell_dock", "spin", (byte) 1, "cell", Map.of()),
                data.getFaceParts().get(
                        io.github.janguenter.bluemap.ae2.model.Direction6.NORTH
                )
        );
        assertEquals(
                Map.of(
                        "id", "megacells:cell_dock",
                        "spin", (byte) 2,
                        "cell", Map.of("id", "megacells:item_storage_cell_1m")
                ),
                data.getFaceParts().get(
                        io.github.janguenter.bluemap.ae2.model.Direction6.SOUTH
                )
        );
        assertEquals(
                Boolean.FALSE,
                ((Map<?, ?>) data.getFaceParts().get(
                        io.github.janguenter.bluemap.ae2.model.Direction6.WEST
                )).get("cell")
        );
        NativeStructuralDecodeResult result = new NativeStructuralCableBusDecoder(
                route -> "megacells".equals(route)
        ).decode(data.getCable(), data.getFaceParts(), data.getFacades());
        assertEquals(NativeStructuralDecodeResult.Status.MALFORMED_FACE_PART, result.status());
    }

    @Test
    void duplicateRetainedPartFieldsAndUnknownFacadeFieldsUseBaseFallback()
            throws Exception {
        byte[] duplicatePart = blockEntity(writer -> {
            writeCable(writer);
            writer.name("north").beginCompound();
            writer.name("id").value("ae2:terminal");
            writer.name("spin").value((byte) 0);
            writer.name("spin").value((byte) 1);
            writer.endCompound();
        });
        byte[] unknownFacade = blockEntity(writer -> {
            writeCable(writer);
            writer.name("facadeNorth").beginCompound();
            writer.name("Name").value("minecraft:stone");
            writer.name("future").value(1);
            writer.endCompound();
        });

        assertRejectedAndResolverFallsBack(duplicatePart);
        assertRejectedAndResolverFallsBack(unknownFacade);
    }

    @Test
    void faceAndFacadeIdentifierLengthLimitsFailClosed() throws Exception {
        byte[] longPart = blockEntity(writer -> {
            writeCable(writer);
            writer.name("north").beginCompound();
            writer.name("id").value("a".repeat(
                    Ae2FacePartDeserializer.MAX_PART_ID_CHARS + 1
            ));
            writer.name("spin").value((byte) 0);
            writer.endCompound();
        });
        byte[] longFacade = blockEntity(writer -> {
            writeCable(writer);
            writer.name("facadeNorth").beginCompound();
            writer.name("Name").value("a".repeat(
                    Ae2FacadeDeserializer.MAX_BLOCK_ID_CHARS + 1
            ));
            writer.endCompound();
        });

        assertRejectedAndResolverFallsBack(longPart);
        assertRejectedAndResolverFallsBack(longFacade);
    }

    @Test
    void exactDepthLimitPassesAndTheFirstExcessiveLevelUsesBaseFallback()
            throws Exception {
        byte[] exact = nestedCable(BoundedNbtSkipper.MAX_NESTING_DEPTH - 1);
        byte[] oneOver = nestedCable(BoundedNbtSkipper.MAX_NESTING_DEPTH);

        assertEquals(
                Map.of("id", "ae2:fluix_glass_cable"),
                readDto(exact).getCable()
        );
        assertRejectedAndResolverFallsBack(oneOver);
    }

    @Test
    void exactListLimitPassesAndOneAdditionalEntryUsesBaseFallback()
            throws Exception {
        byte[] exact = cableWithIntList(BoundedNbtSkipper.MAX_LIST_ENTRIES);
        byte[] oneOver = cableWithIntList(BoundedNbtSkipper.MAX_LIST_ENTRIES + 1);

        assertEquals(
                Map.of("id", "ae2:fluix_glass_cable"),
                readDto(exact).getCable()
        );
        assertRejectedAndResolverFallsBack(oneOver);
    }

    private static byte[] nestedCable(int nestedCompounds) throws IOException {
        return blockEntity(writer -> {
            writer.name("cable").beginCompound();
            writeId(writer);
            for (int depth = 0; depth < nestedCompounds; depth++) {
                writer.name("nested").beginCompound();
            }
            writer.name("leaf").value(1);
            for (int depth = 0; depth < nestedCompounds; depth++) {
                writer.endCompound();
            }
            writer.endCompound();
        });
    }

    private static byte[] cableWithIntList(int length) throws IOException {
        return blockEntity(writer -> {
            writer.name("cable").beginCompound();
            writeId(writer);
            writer.name("list").beginList(length, TagType.INT);
            for (int index = 0; index < length; index++) {
                writer.value(index);
            }
            writer.endList();
            writer.endCompound();
        });
    }

    @Test
    void everyOversizedArrayTypeFailsClosedWithoutGenericArrayMaterialization()
            throws Exception {
        int length = BoundedNbtSkipper.MAX_ARRAY_ELEMENTS + 1;

        byte[] byteArray = blockEntity(writer -> {
            writer.name("cable").beginCompound();
            writeId(writer);
            writer.name("oversized").value(new byte[length]);
            writer.endCompound();
        });
        byte[] intArray = blockEntity(writer -> {
            writer.name("cable").beginCompound();
            writeId(writer);
            writer.name("oversized").value(new int[length]);
            writer.endCompound();
        });
        byte[] longArray = blockEntity(writer -> {
            writer.name("cable").beginCompound();
            writeId(writer);
            writer.name("oversized").value(new long[length]);
            writer.endCompound();
        });

        assertRejectedAndResolverFallsBack(byteArray);
        assertRejectedAndResolverFallsBack(intArray);
        assertRejectedAndResolverFallsBack(longArray);
    }

    @Test
    void oversizedFaceAndFacadePayloadsAlsoFailClosed() throws Exception {
        byte[] face = blockEntity(writer -> {
            writeCable(writer);
            writer.name("north").beginCompound();
            writer.name("oversized").beginList(
                    BoundedNbtSkipper.MAX_LIST_ENTRIES + 1,
                    TagType.BYTE
            );
            for (int index = 0; index <= BoundedNbtSkipper.MAX_LIST_ENTRIES; index++) {
                writer.value((byte) index);
            }
            writer.endList();
            writer.endCompound();
        });
        byte[] facade = blockEntity(writer -> {
            writeCable(writer);
            writer.name("facadeSouth").beginCompound();
            writer.name("oversized").value(
                    new long[BoundedNbtSkipper.MAX_ARRAY_ELEMENTS + 1]
            );
            writer.endCompound();
        });

        assertRejectedAndResolverFallsBack(face);
        assertRejectedAndResolverFallsBack(facade);
    }

    @Test
    void exactVisitedTagBudgetPassesAndOneAdditionalTagFailsClosed() throws Exception {
        byte[] exact = blockEntity(writer -> {
            writer.name("cable").beginCompound();
            writeId(writer);
            for (int index = 0; index < BoundedNbtSkipper.MAX_VISITED_TAGS - 2; index++) {
                writer.name("ignored_" + index).value(index);
            }
            writer.endCompound();
        });
        byte[] oneOver = blockEntity(writer -> {
            writer.name("cable").beginCompound();
            writeId(writer);
            for (int index = 0; index < BoundedNbtSkipper.MAX_VISITED_TAGS - 1; index++) {
                writer.name("ignored_" + index).value(index);
            }
            writer.endCompound();
        });

        assertEquals(
                Map.of("id", "ae2:fluix_glass_cable"),
                readDto(exact).getCable()
        );
        assertRejectedAndResolverFallsBack(oneOver);
    }

    @Test
    void exactCumulativeArrayByteBudgetPassesAndOneMoreArrayFailsClosed()
            throws Exception {
        int[] withinPerArrayLimit = new int[BoundedNbtSkipper.MAX_ARRAY_ELEMENTS];
        byte[] exact = blockEntity(writer -> {
            writer.name("cable").beginCompound();
            writeId(writer);
            for (int index = 0; index < 4; index++) {
                writer.name("ignored_" + index).value(withinPerArrayLimit);
            }
            writer.endCompound();
        });
        byte[] oneOver = blockEntity(writer -> {
            writer.name("cable").beginCompound();
            writeId(writer);
            for (int index = 0; index < 5; index++) {
                writer.name("ignored_" + index).value(withinPerArrayLimit);
            }
            writer.endCompound();
        });

        assertEquals(
                Map.of("id", "ae2:fluix_glass_cable"),
                readDto(exact).getCable()
        );
        assertRejectedAndResolverFallsBack(oneOver);
    }

    @Test
    void overlongDirectCableFieldNameFailsClosedAfterBlueNbtReadsIt() throws Exception {
        byte[] nbt = blockEntity(writer -> {
            writer.name("cable").beginCompound();
            writeId(writer);
            writer.name("n".repeat(Ae2CablePartDeserializer.MAX_DIRECT_FIELD_NAME_CHARS + 1))
                    .value(1);
            writer.endCompound();
        });

        assertRejectedAndResolverFallsBack(nbt);
    }

    @Test
    void pathologicalDeclarationsAndMalformedTagIdsAvoidDeclaredContainerMaterialization()
            throws Exception {
        assertThrows(IOException.class, () -> readDto(pathologicalListDeclaration()));
        assertThrows(IOException.class, () -> readDto(pathologicalArrayDeclaration()));
        assertThrows(IOException.class, () -> readDto(invalidTagId()));
    }

    @Test
    void negativeLengthsAndNonzeroEndListsAreRejectedAsMalformed() throws Exception {
        assertThrows(IOException.class, () -> readDto(listDeclaration(-1, TagType.INT)));
        assertThrows(IOException.class, () -> readDto(arrayDeclaration(-1, TagType.BYTE_ARRAY)));
        assertThrows(IOException.class, () -> readDto(arrayDeclaration(-1, TagType.INT_ARRAY)));
        assertThrows(IOException.class, () -> readDto(arrayDeclaration(-1, TagType.LONG_ARRAY)));
        assertThrows(IOException.class, () -> readDto(listDeclaration(1, TagType.END)));
    }

    @Test
    void duplicateAndOversizedCenterIdsFailClosed() throws Exception {
        byte[] duplicate = blockEntity(writer -> {
            writer.name("cable").beginCompound();
            writeId(writer);
            writeId(writer);
            writer.endCompound();
        });
        byte[] oversized = blockEntity(writer -> {
            writer.name("cable").beginCompound();
            writer.name("id").value("a".repeat(Ae2CablePartDeserializer.MAX_CENTER_ID_CHARS + 1));
            writer.endCompound();
        });

        assertRejectedAndResolverFallsBack(duplicate);
        assertRejectedAndResolverFallsBack(oversized);
    }

    private static void assertRejectedAndResolverFallsBack(byte[] nbt) throws IOException {
        assertThrows(IOException.class, () -> readDto(nbt));

        assertTrue(BlueMap522Adapter.install());
        BlockEntity resolved = freshBlueNbt().read(
                new ByteArrayInputStream(nbt),
                BlockEntity.class
        );
        MCABlockEntity base = assertInstanceOf(MCABlockEntity.class, resolved);
        assertFalse(base instanceof Ae2CableBusBlockEntityData);
        assertEquals("ae2:cable_bus", base.getId().toString());
        assertEquals(17, base.getX());
        assertEquals(-23, base.getY());
        assertEquals(41, base.getZ());
    }

    private static Ae2CableBusBlockEntityData readDto(byte[] nbt) throws IOException {
        return freshBlueNbt().read(
                new ByteArrayInputStream(nbt),
                Ae2CableBusBlockEntityData.class
        );
    }

    private static BlueNBT freshBlueNbt() {
        return MCAUtil.addCommonNbtSettings(new BlueNBT());
    }

    private static byte[] blockEntity(WriterAction body) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (NBTWriter writer = new NBTWriter(bytes)) {
            writer.beginCompound();
            writer.name("id").value("ae2:cable_bus");
            writer.name("x").value(17);
            writer.name("y").value(-23);
            writer.name("z").value(41);
            body.write(writer);
            writer.endCompound();
        }
        return bytes.toByteArray();
    }

    private static void writeCable(NBTWriter writer) throws IOException {
        writer.name("cable").beginCompound();
        writeId(writer);
        writer.endCompound();
    }

    private static void writeId(NBTWriter writer) throws IOException {
        writer.name("id").value("ae2:fluix_glass_cable");
    }

    private static byte[] pathologicalListDeclaration() throws IOException {
        return listDeclaration(Integer.MAX_VALUE, TagType.INT);
    }

    private static byte[] pathologicalArrayDeclaration() throws IOException {
        return arrayDeclaration(Integer.MAX_VALUE, TagType.BYTE_ARRAY);
    }

    private static byte[] listDeclaration(int length, TagType elementType)
            throws IOException {
        return malformedCablePayload(output -> {
            output.writeByte(TagType.LIST.getId());
            output.writeUTF("oversized");
            output.writeByte(elementType.getId());
            output.writeInt(length);
        });
    }

    private static byte[] arrayDeclaration(int length, TagType arrayType)
            throws IOException {
        return malformedCablePayload(output -> {
            output.writeByte(arrayType.getId());
            output.writeUTF("oversized");
            output.writeInt(length);
        });
    }

    private static byte[] invalidTagId() throws IOException {
        return malformedCablePayload(output -> output.writeByte(99));
    }

    private static byte[] malformedCablePayload(OutputAction payload) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (DataOutputStream output = new DataOutputStream(bytes)) {
            output.writeByte(TagType.COMPOUND.getId());
            output.writeUTF("");
            writeString(output, "id", "ae2:cable_bus");
            writeInt(output, "x", 17);
            writeInt(output, "y", -23);
            writeInt(output, "z", 41);
            output.writeByte(TagType.COMPOUND.getId());
            output.writeUTF("cable");
            writeString(output, "id", "ae2:fluix_glass_cable");
            payload.write(output);
        }
        return bytes.toByteArray();
    }

    private static void writeString(DataOutputStream output, String name, String value)
            throws IOException {
        output.writeByte(TagType.STRING.getId());
        output.writeUTF(name);
        output.writeUTF(value);
    }

    private static void writeInt(DataOutputStream output, String name, int value)
            throws IOException {
        output.writeByte(TagType.INT.getId());
        output.writeUTF(name);
        output.writeInt(value);
    }

    @FunctionalInterface
    private interface WriterAction {
        void write(NBTWriter writer) throws IOException;
    }

    @FunctionalInterface
    private interface OutputAction {
        void write(DataOutputStream output) throws IOException;
    }

    @FunctionalInterface
    private interface SpinWriter {
        void write(NBTWriter writer) throws IOException;
    }
}
