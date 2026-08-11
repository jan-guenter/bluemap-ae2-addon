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
import de.bluecolored.bluemap.core.resources.pack.resourcepack.model.Rotation;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.texture.AnimationMeta;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.texture.Texture;
import de.bluecolored.bluemap.core.util.Direction;
import de.bluecolored.bluemap.core.util.Key;
import io.github.janguenter.bluemap.ae2.profile.Ae219217NativeStructuralProfile;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.List;

/** Parsed semantic signatures that reject higher-priority resource-pack drift. */
final class NativeStructuralSemanticResources {

    private static final String EXPECTED_MODEL_SIGNATURE =
            Ae219217NativeStructuralProfile.LIVE_MODEL_SEMANTIC_SIGNATURE_SHA256;
    private static final String EXPECTED_TEXTURE_SIGNATURE =
            Ae219217NativeStructuralProfile.LIVE_TEXTURE_SEMANTIC_SIGNATURE_SHA256;
    private static final List<String> REQUIRED_MODELS = requiredModelPaths();

    private NativeStructuralSemanticResources() {
    }

    static boolean supports(ResourcePack resourcePack) {
        Signatures signatures = signatures(resourcePack);
        return signatures != null
                && EXPECTED_MODEL_SIGNATURE.equals(signatures.models())
                && EXPECTED_TEXTURE_SIGNATURE.equals(signatures.textures());
    }

    static boolean supports(ResourcePack resourcePack, Signatures expected) {
        Signatures actual = signatures(resourcePack);
        return expected != null && expected.equals(actual);
    }

    static Signatures signatures(ResourcePack resourcePack) {
        if (resourcePack == null) {
            return null;
        }
        try {
            MessageDigest models = sha256();
            for (String path : REQUIRED_MODELS) {
                Model model = resourcePack.getModels().get(new ResourcePath<>(path));
                if (model == null) {
                    return null;
                }
                model.applyParent(resourcePack.getModels());
                update(models, "M|");
                update(models, path);
                update(models, "|ao=");
                update(models, model.isAmbientocclusion() ? "1\n" : "0\n");
                Element[] elements = model.getElements();
                update(models, "elements=");
                update(models, elements == null ? "null\n" : elements.length + "\n");
                if (elements == null) {
                    continue;
                }
                for (int index = 0; index < elements.length; index++) {
                    Element element = elements[index];
                    if (element == null) {
                        update(models, "E|");
                        update(models, Integer.toString(index));
                        update(models, "|null\n");
                        continue;
                    }
                    appendElement(models, index, element, model);
                }
            }

            String textures = textureSignature(
                    resourcePack,
                    NativeStructuralResourceModels.requiredTextures()
            );
            return textures == null
                    ? null : new Signatures(hex(models.digest()), textures);
        } catch (RuntimeException exception) {
            return null;
        }
    }

    static String textureSignature(
            ResourcePack resourcePack,
            Collection<Key> requiredTextures
    ) {
        if (resourcePack == null || requiredTextures == null) {
            return null;
        }
        try {
            MessageDigest textures = sha256();
            List<Key> sorted = requiredTextures.stream()
                    .sorted(Comparator.comparing(Key::getFormatted))
                    .toList();
            if (sorted.size() != requiredTextures.size()) {
                return null;
            }
            for (Key key : sorted) {
                Texture texture = resourcePack.getTextures().get(key);
                if (texture == null || ResourcePack.MISSING_TEXTURE.equals(key)) {
                    return null;
                }
                appendTexture(textures, key, texture);
            }
            return hex(textures.digest());
        } catch (IOException | RuntimeException exception) {
            return null;
        }
    }

    static List<String> requiredModels() {
        return REQUIRED_MODELS;
    }

    private static void appendElement(
            MessageDigest digest,
            int index,
            Element element,
            Model model
    ) {
        update(digest, "E|");
        update(digest, Integer.toString(index));
        appendVector(digest, "|from=", element.getFrom());
        appendVector(digest, "|to=", element.getTo());
        Rotation rotation = element.getRotation();
        appendVector(digest, "|origin=", rotation.getOrigin());
        update(digest, "|rot=");
        appendFloat(digest, rotation.getX());
        appendFloat(digest, rotation.getY());
        appendFloat(digest, rotation.getZ());
        update(digest, rotation.getAxis().name());
        update(digest, ",");
        appendFloat(digest, rotation.getAngle());
        update(digest, rotation.isRescale() ? "1" : "0");
        update(digest, element.isShade() ? "|shade=1" : "|shade=0");
        update(digest, "|light=");
        update(digest, Integer.toString(element.getLightEmission()));
        update(digest, "\n");
        for (Direction direction : Direction.values()) {
            update(digest, "F|");
            update(digest, direction.name());
            Face face = element.getFaces().get(direction);
            if (face == null) {
                update(digest, "|null\n");
                continue;
            }
            Vector4f uv = face.getUv();
            update(digest, "|uv=");
            appendFloat(digest, uv.getX());
            appendFloat(digest, uv.getY());
            appendFloat(digest, uv.getZ());
            appendFloat(digest, uv.getW());
            ResourcePath<Texture> texture = face.getTexture().getTexturePath(
                    model.getTextures()::get
            );
            update(digest, "|texture=");
            update(digest, texture == null ? "null" : texture.getFormatted());
            update(digest, "|cull=");
            update(digest, face.getCullface() == null
                    ? "null" : face.getCullface().name());
            update(digest, "|rotation=");
            update(digest, Integer.toString(face.getRotation()));
            update(digest, "|tint=");
            update(digest, Integer.toString(face.getTintindex()));
            update(digest, "\n");
        }
    }

    private static void appendTexture(
            MessageDigest digest,
            Key key,
            Texture texture
    ) throws IOException {
        BufferedImage image = texture.getTextureImage();
        if (image == null) {
            throw new IOException("decoded texture is null");
        }
        update(digest, "T|");
        update(digest, key.getFormatted());
        update(digest, "|size=");
        update(digest, image.getWidth() + "x" + image.getHeight());
        AnimationMeta animation = texture.getAnimation();
        if (animation == null) {
            update(digest, "|animation=null");
        } else {
            update(digest, animation.isInterpolate() ? "|animation=1," : "|animation=0,");
            update(digest, animation.getWidth() + "," + animation.getHeight() + ","
                    + animation.getFrametime());
            List<AnimationMeta.FrameMeta> frames = animation.getFrames();
            if (frames == null) {
                update(digest, ",frames=null");
            } else {
                update(digest, ",frames=");
                for (AnimationMeta.FrameMeta frame : frames) {
                    update(digest, frame.getIndex() + ":" + frame.getTime() + ";");
                }
            }
        }
        update(digest, "|pixels=");
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                update(digest, String.format(java.util.Locale.ROOT, "%08x", image.getRGB(x, y)));
            }
        }
        update(digest, "\n");
    }

    private static void appendVector(
            MessageDigest digest,
            String label,
            Vector3f vector
    ) {
        update(digest, label);
        appendFloat(digest, vector.getX());
        appendFloat(digest, vector.getY());
        appendFloat(digest, vector.getZ());
    }

    private static void appendFloat(MessageDigest digest, float value) {
        update(digest, String.format(
                java.util.Locale.ROOT,
                "%08x,",
                Float.floatToIntBits(value)
        ));
    }

    private static List<String> requiredModelPaths() {
        List<String> paths = new ArrayList<>(
                Ae219217NativeStructuralProfile.TRANSITIVE_JSON_RESOURCE_COUNT
        );
        for (String resource : Ae219217NativeStructuralProfile.requiredResources().keySet()) {
            if (!resource.startsWith("assets/") || !resource.endsWith(".json")) {
                continue;
            }
            String relative = resource.substring("assets/".length(), resource.length() - 5);
            int separator = relative.indexOf("/models/");
            if (separator <= 0) {
                throw new IllegalStateException("invalid structural model path " + resource);
            }
            paths.add(relative.substring(0, separator) + ":"
                    + relative.substring(separator + "/models/".length()));
        }
        if (paths.size() != Ae219217NativeStructuralProfile.TRANSITIVE_JSON_RESOURCE_COUNT) {
            throw new IllegalStateException("structural model closure count mismatch");
        }
        return paths.stream().sorted().toList();
    }

    private static MessageDigest sha256() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 unavailable", exception);
        }
    }

    private static void update(MessageDigest digest, String value) {
        digest.update(value.getBytes(StandardCharsets.UTF_8));
    }

    private static String hex(byte[] value) {
        return HexFormat.of().formatHex(value);
    }

    record Signatures(String models, String textures) {
    }
}
