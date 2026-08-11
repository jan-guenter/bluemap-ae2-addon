/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.adapter.bluemap522;

import com.flowpowered.math.vector.Vector3f;
import com.flowpowered.math.vector.Vector4f;
import de.bluecolored.bluemap.core.resources.ResourcePath;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.model.Element;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.model.Face;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.model.Model;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.texture.Texture;
import de.bluecolored.bluemap.core.util.Direction;
import de.bluecolored.bluemap.core.util.Key;
import io.github.janguenter.bluemap.ae2.model.DriveCellCatalog;
import io.github.janguenter.bluemap.ae2.profile.Ae219217DriveProfile;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Map;
import java.util.Set;

/** Structural gates for the exact operator-installed AE2 19.2.17 drive resources. */
final class M3DriveResourceModels {

    static final ResourcePath<Model> DRIVE_BASE = model(DriveCellCatalog.BASE_MODEL);
    static final ResourcePath<Model> EMPTY_CELL = model(DriveCellCatalog.EMPTY_CELL_MODEL);
    static final Key LED_TEXTURE = Key.parse(Ae219217DriveProfile.LED_TEXTURE);
    static final Key CELL_TEXTURE = Key.parse("ae2:block/drive/drive_cells");

    private static final Set<Key> REQUIRED_TEXTURES = buildRequiredTextures();
    // Canonical semantic signatures calculated from the exact AE2 19.2.17
    // JSON after BlueMap parent application. The signed form includes every
    // output-relevant coordinate, UV, rotation, face, texture and cull field.
    private static final Map<String, String> EXPECTED_MODEL_SIGNATURES = Map.ofEntries(
            Map.entry(
                    DriveCellCatalog.BASE_MODEL,
                    "286ead086f7b61d4f62bd4f200e28f2898c9f842ebb15a8bb8c96c7bbe6b424c"
            ),
            Map.entry(
                    DriveCellCatalog.EMPTY_CELL_MODEL,
                    "3ef0644e420c7edf97107dda7b33feab4673215706c53844e0bac5a181931781"
            ),
            Map.entry(
                    DriveCellCatalog.GENERIC_CELL_MODEL,
                    "67662442c5577e996936dc62ab784e87edb171c838baec96565ba053255f26a9"
            ),
            Map.entry(
                    "ae2:block/drive/cells/1k_item_cell",
                    "5babe83a038ff11d908aca00e6e8ad62e51efd8e62fb84299639e0029959c1f3"
            ),
            Map.entry(
                    "ae2:block/drive/cells/4k_item_cell",
                    "cb4eda3047a6b2a228e018d179f2fc548bf24f5bd0f92cc33483f139fe668283"
            ),
            Map.entry(
                    "ae2:block/drive/cells/16k_item_cell",
                    "845f203df8dff90d187b2f39c0df822f76f27ff62bab88578e58ec7dedeeb3c5"
            ),
            Map.entry(
                    "ae2:block/drive/cells/64k_item_cell",
                    "cfd1c646bcd770e833bfa4ef8ee59eadc36247780fdbfd794315026afbdad98b"
            ),
            Map.entry(
                    "ae2:block/drive/cells/256k_item_cell",
                    "88fafa5ec194fc770bf718cdc27e92881c8fd11e676645ccbf7872475ab73e7b"
            ),
            Map.entry(
                    "ae2:block/drive/cells/1k_fluid_cell",
                    "bc7152880ec4083126a676b501e84de190c4c9aa983ff23456f88eeac2dbd8cd"
            ),
            Map.entry(
                    "ae2:block/drive/cells/4k_fluid_cell",
                    "f356de2411f998e088f2719235677cf2d3a2f947c215a99cd95957eecf58be96"
            ),
            Map.entry(
                    "ae2:block/drive/cells/16k_fluid_cell",
                    "48578c00cda44176c7199805507dc4e7b1d729376359650693c760c6e9494da0"
            ),
            Map.entry(
                    "ae2:block/drive/cells/64k_fluid_cell",
                    "c92039be9d426a6a26b1a7fcfa9ba4aa2704946f8f21bdd623ae078fa095432c"
            ),
            Map.entry(
                    "ae2:block/drive/cells/256k_fluid_cell",
                    "7dcd3ea478dace1aeb3ee2a15ae0fcabe38f2245b234f5b37b6ae437a56d3878"
            ),
            Map.entry(
                    "ae2:block/drive/cells/creative_cell",
                    "c1b9004cf517487dce2c7433e63f6ce75d2a11db96078ae9140d66127960b224"
            )
    );

    private M3DriveResourceModels() {
    }

    static ResourcePath<Model> model(String path) {
        return new ResourcePath<>(path);
    }

    static Set<Key> requiredTextures() {
        return REQUIRED_TEXTURES;
    }

    static Map<String, String> expectedModelSignatures() {
        return EXPECTED_MODEL_SIGNATURES;
    }

    static boolean resourcesSupported(ResourcePack resourcePack) {
        if (resourcePack == null || !texturesSupported(resourcePack)) {
            return false;
        }
        for (String modelPath : Ae219217DriveProfile.models()) {
            if (!exactModel(resourcePack.getModels().get(model(modelPath)), modelPath)) {
                return false;
            }
        }
        return true;
    }

    static boolean texturesSupported(ResourcePack resourcePack) {
        for (Key key : REQUIRED_TEXTURES) {
            if (ResourcePack.MISSING_TEXTURE.equals(key)
                    || resourcePack.getTextures().get(key) == null) {
                return false;
            }
        }
        Texture led = resourcePack.getTextures().get(LED_TEXTURE);
        return led != null
                && !led.isHalfTransparent()
                && led.getAnimation() == null
                && led.getColorStraight().a >= 1F;
    }

    static boolean exactModel(Model model, String modelPath) {
        String expected = EXPECTED_MODEL_SIGNATURES.get(modelPath);
        return expected != null
                && model != null
                && model.getParent() == null
                && model.getElements() != null
                && expected.equals(semanticSignature(model));
    }

    static String semanticSignature(Model model) {
        StringBuilder contract = new StringBuilder(8_192);
        contract.append("ao=").append(model.isAmbientocclusion() ? '1' : '0');
        Element[] elements = model.getElements();
        contract.append(";elements=").append(elements == null ? -1 : elements.length);
        if (elements != null) {
            for (int index = 0; index < elements.length; index++) {
                Element element = elements[index];
                contract.append(";e=").append(index);
                if (element == null) {
                    contract.append(":null");
                    continue;
                }
                appendVector(contract, element.getFrom());
                appendVector(contract, element.getTo());
                appendVector(contract, element.getRotation().getOrigin());
                appendFloat(contract, element.getRotation().getX());
                appendFloat(contract, element.getRotation().getY());
                appendFloat(contract, element.getRotation().getZ());
                contract.append(':').append(element.getRotation().getAxis());
                appendFloat(contract, element.getRotation().getAngle());
                contract.append(':').append(element.getRotation().isRescale() ? '1' : '0');
                contract.append(':').append(element.isShade() ? '1' : '0');
                contract.append(':').append(element.getLightEmission());
                for (Direction direction : Direction.values()) {
                    contract.append(";f=").append(direction.name()).append(':');
                    Face face = element.getFaces().get(direction);
                    if (face == null) {
                        contract.append("null");
                        continue;
                    }
                    appendVector(contract, face.getUv());
                    ResourcePath<Texture> texture = face.getTexture().getTexturePath(
                            model.getTextures()::get
                    );
                    contract.append(':').append(
                            texture == null ? "null" : texture.getFormatted()
                    );
                    contract.append(':').append(
                            face.getCullface() == null ? "null" : face.getCullface().name()
                    );
                    contract.append(':').append(face.getRotation());
                    contract.append(':').append(face.getTintindex());
                }
            }
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(
                    contract.toString().getBytes(StandardCharsets.UTF_8)
            ));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }

    private static void appendVector(StringBuilder target, Vector3f vector) {
        appendFloat(target, vector.getX());
        appendFloat(target, vector.getY());
        appendFloat(target, vector.getZ());
    }

    private static void appendVector(StringBuilder target, Vector4f vector) {
        appendFloat(target, vector.getX());
        appendFloat(target, vector.getY());
        appendFloat(target, vector.getZ());
        appendFloat(target, vector.getW());
    }

    private static void appendFloat(StringBuilder target, float value) {
        target.append(':').append(Integer.toHexString(Float.floatToIntBits(value)));
    }

    private static Set<Key> buildRequiredTextures() {
        return Ae219217DriveProfile.textures().stream()
                .map(Key::parse)
                .collect(java.util.stream.Collectors.toUnmodifiableSet());
    }

}
