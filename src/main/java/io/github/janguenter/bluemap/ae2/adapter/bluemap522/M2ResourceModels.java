/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.adapter.bluemap522;

import com.flowpowered.math.vector.Vector3f;
import de.bluecolored.bluemap.core.resources.ResourcePath;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.Variant;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.VariantSet;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.Variants;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.model.Element;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.model.Face;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.model.Model;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.texture.Texture;
import de.bluecolored.bluemap.core.util.Direction;
import de.bluecolored.bluemap.core.util.Key;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** Structural gates for operator-installed terminal and stone resources. */
final class M2ResourceModels {

    static final ResourcePath<Model> DISPLAY_BASE = new ResourcePath<>(
            "ae2:part/display_base"
    );
    static final ResourcePath<Model> TERMINAL_OFF = new ResourcePath<>(
            "ae2:part/terminal_off"
    );
    static final ResourcePath<Model> DISPLAY_STATUS_OFF = new ResourcePath<>(
            "ae2:part/display_status_off"
    );
    static final Key STONE = Key.parse("minecraft:stone");
    static final Key STONE_TEXTURE = Key.parse("minecraft:block/stone");

    static final Set<Key> TERMINAL_TEXTURES = Set.of(
            Key.parse("ae2:part/monitor_sides"),
            Key.parse("ae2:part/monitor_sides_status"),
            Key.parse("ae2:part/monitor_back"),
            Key.parse("ae2:part/monitor_front"),
            Key.parse("ae2:part/monitor_sides_status_off"),
            Key.parse("ae2:part/terminal_bright"),
            Key.parse("ae2:part/terminal_medium"),
            Key.parse("ae2:part/terminal_dark")
    );

    private M2ResourceModels() {
    }

    static Set<Key> requiredTextures() {
        Set<Key> textures = new LinkedHashSet<>(TERMINAL_TEXTURES);
        textures.add(STONE_TEXTURE);
        return Set.copyOf(textures);
    }

    static boolean terminalModelsSupported(ResourcePack resourcePack) {
        Model base = resourcePack.getModels().get(DISPLAY_BASE);
        Model terminal = resourcePack.getModels().get(TERMINAL_OFF);
        Model status = resourcePack.getModels().get(DISPLAY_STATUS_OFF);
        return exactUntintedModel(base, 2, 11, Set.of(
                "ae2:part/monitor_sides",
                "ae2:part/monitor_sides_status",
                "ae2:part/monitor_back",
                "ae2:part/monitor_front"
        ))
                && exactTerminalLayers(terminal)
                && exactUntintedModel(status, 4, 8, Set.of(
                        "ae2:part/monitor_sides_status_off"
                ));
    }

    static Key resolveStoneTexture(ResourcePack resourcePack) {
        de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.BlockState state =
                resourcePack.getBlockStates().get(STONE);
        if (state == null || state.getMultipart() != null) {
            return null;
        }
        Variants variants = state.getVariants();
        if (variants == null || variants.getVariants().length != 0) {
            return null;
        }
        VariantSet defaults = variants.getDefaultVariant();
        if (defaults == null || defaults.getVariants().length != 4) {
            return null;
        }

        Key selected = null;
        for (Variant variant : defaults.getVariants()) {
            if (variant.getX() != 0 || variant.getZ() != 0
                    || (variant.getY() != 0 && variant.getY() != 180)
                    || variant.isUvlock() || Double.compare(variant.getWeight(), 1D) != 0) {
                return null;
            }
            Model model = variant.getModel().getResource(resourcePack.getModels()::get);
            Key texture = fullCubeStoneTexture(model);
            if (texture == null) {
                return null;
            }
            if (selected == null) {
                selected = texture;
            } else if (!selected.equals(texture)) {
                return null;
            }
        }
        if (!STONE_TEXTURE.equals(selected)) {
            return null;
        }
        Texture texture = resourcePack.getTextures().get(selected);
        if (texture == null || texture.isHalfTransparent()
                || texture.getAnimation() != null
                || texture.getColorStraight().a < 1F) {
            return null;
        }
        return selected;
    }

    private static boolean exactUntintedModel(
            Model model,
            int expectedElements,
            int expectedFaces,
            Set<String> expectedTextures
    ) {
        if (model == null || model.getParent() != null || model.getElements() == null
                || model.getElements().length != expectedElements) {
            return false;
        }
        int faces = 0;
        Set<String> textures = new LinkedHashSet<>();
        for (Element element : model.getElements()) {
            if (element == null || !zeroRotation(element)) {
                return false;
            }
            for (Face face : element.getFaces().values()) {
                if (face == null || face.getTintindex() != -1 || face.getCullface() != null) {
                    return false;
                }
                ResourcePath<Texture> texture = face.getTexture().getTexturePath(
                        model.getTextures()::get
                );
                if (texture == null) {
                    return false;
                }
                textures.add(texture.getFormatted());
                faces++;
            }
        }
        return faces == expectedFaces && textures.equals(expectedTextures);
    }

    private static boolean exactTerminalLayers(Model model) {
        if (model == null || model.getParent() != null || model.getElements() == null
                || model.getElements().length != 3) {
            return false;
        }
        List<Integer> tints = new ArrayList<>(3);
        List<String> textures = new ArrayList<>(3);
        for (Element element : model.getElements()) {
            if (element == null || !zeroRotation(element)
                    || element.getFaces().size() != 1) {
                return false;
            }
            Face face = element.getFaces().get(Direction.NORTH);
            if (face == null || face.getCullface() != null) {
                return false;
            }
            ResourcePath<Texture> texture = face.getTexture().getTexturePath(
                    model.getTextures()::get
            );
            if (texture == null) {
                return false;
            }
            tints.add(face.getTintindex());
            textures.add(texture.getFormatted());
        }
        return tints.equals(List.of(3, 2, 1))
                && textures.equals(List.of(
                        "ae2:part/terminal_bright",
                        "ae2:part/terminal_medium",
                        "ae2:part/terminal_dark"
                ));
    }

    private static Key fullCubeStoneTexture(Model model) {
        if (model == null || model.getParent() != null || model.getElements() == null
                || model.getElements().length != 1 || !model.isAmbientocclusion()) {
            return null;
        }
        Element element = model.getElements()[0];
        if (element == null || !Vector3f.ZERO.equals(element.getFrom())
                || !new Vector3f(16, 16, 16).equals(element.getTo())
                || !zeroRotation(element) || element.getLightEmission() != 0
                || element.getFaces().size() != Direction.values().length) {
            return null;
        }
        Key selected = null;
        for (Direction direction : Direction.values()) {
            Face face = element.getFaces().get(direction);
            if (face == null || face.getTintindex() != -1) {
                return null;
            }
            ResourcePath<Texture> texture = face.getTexture().getTexturePath(
                    model.getTextures()::get
            );
            if (texture == null) {
                return null;
            }
            if (selected == null) {
                selected = texture;
            } else if (!selected.equals(texture)) {
                return null;
            }
        }
        return selected;
    }

    private static boolean zeroRotation(Element element) {
        return element.getRotation().getX() == 0
                && element.getRotation().getY() == 0
                && element.getRotation().getZ() == 0
                && element.getRotation().getAngle() == 0
                && !element.getRotation().isRescale();
    }
}
