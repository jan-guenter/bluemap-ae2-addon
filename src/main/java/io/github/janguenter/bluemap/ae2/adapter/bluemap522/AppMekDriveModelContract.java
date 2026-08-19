/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.adapter.bluemap522;

import com.flowpowered.math.vector.Vector3f;
import com.flowpowered.math.vector.Vector4f;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.model.Element;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.model.Face;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.model.Model;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.texture.Texture;
import de.bluecolored.bluemap.core.resources.ResourcePath;
import de.bluecolored.bluemap.core.util.Direction;
import de.bluecolored.bluemap.core.util.math.Axis;
import io.github.janguenter.bluemap.ae2.profile.appmek.AppMek163Profile;

import java.util.Map;
import java.util.Set;

/** Exact resolved-model shape shared by all five AppMek 1.6.3 cell tiers. */
final class AppMekDriveModelContract {

    private static final Vector3f FROM = Vector3f.ZERO;
    private static final Vector3f TO = new Vector3f(6F, 2F, 2F);
    private static final Set<Direction> FACES = Set.of(
            Direction.NORTH,
            Direction.UP,
            Direction.DOWN
    );
    private static final String TEXTURE = AppMek163Profile.DRIVE_TEXTURE;
    private static final Map<String, String> EXPECTED_SIGNATURES =
            AppMek163Profile.DRIVE_MODEL_SEMANTIC_SHA256;

    private AppMekDriveModelContract() {
    }

    static boolean supported(Model model, String modelId) {
        String expectedSignature = EXPECTED_SIGNATURES.get(modelId);
        if (model == null || model.getParent() != null || model.getElements() == null
                || model.getElements().length != 1 || model.isAmbientocclusion()
                || expectedSignature == null) {
            return false;
        }
        Element element = model.getElements()[0];
        if (element == null || !FROM.equals(element.getFrom()) || !TO.equals(element.getTo())
                || !FACES.equals(element.getFaces().keySet())
                || !new Vector3f(9F, 8F, 8F).equals(element.getRotation().getOrigin())
                || element.getRotation().getAxis() != Axis.Y
                || Float.compare(element.getRotation().getX(), 0F) != 0
                || Float.compare(element.getRotation().getY(), 0F) != 0
                || Float.compare(element.getRotation().getZ(), 0F) != 0
                || Float.compare(element.getRotation().getAngle(), 0F) != 0
                || element.getRotation().isRescale()
                || !element.isShade()
                || element.getLightEmission() != 0) {
            return false;
        }
        Map<Direction, Face> faces = element.getFaces();
        return expectedSignature.equals(M3DriveResourceModels.semanticSignature(model))
                && exactFace(model, faces.get(Direction.NORTH), Direction.NORTH)
                && exactFace(model, faces.get(Direction.UP), Direction.NORTH)
                && exactFace(model, faces.get(Direction.DOWN), Direction.NORTH)
                && exactTierUvs(faces, modelId);
    }

    private static boolean exactFace(Model model, Face face, Direction cullface) {
        ResourcePath<Texture> texture = face == null || face.getTexture() == null
                ? null : face.getTexture().getTexturePath(model.getTextures()::get);
        return face != null
                && face.getCullface() == cullface
                && face.getRotation() == 0
                && face.getTintindex() == -1
                && texture != null
                && TEXTURE.equals(texture.getFormatted());
    }

    private static boolean exactTierUvs(Map<Direction, Face> faces, String modelId) {
        Vector4f north = faces.get(Direction.NORTH).getUv();
        Vector4f up = faces.get(Direction.UP).getUv();
        Vector4f down = faces.get(Direction.DOWN).getUv();
        if (north == null || up == null || down == null
                || Float.compare(north.getX(), 0F) != 0
                || Float.compare(north.getZ(), 6F) != 0
                || Float.compare(up.getX(), 6F) != 0
                || Float.compare(up.getZ(), 0F) != 0
                || !up.equals(down)) {
            return false;
        }
        float rowStart = switch (modelId.substring(modelId.lastIndexOf('_') + 1)) {
            case "1k" -> 0F;
            case "4k" -> 2F;
            case "16k" -> 4F;
            case "64k" -> 6F;
            case "256k" -> 8F;
            default -> Float.NaN;
        };
        return Float.compare(north.getW(), rowStart + 2F) == 0
                && Float.compare(north.getY(), rowStart) == 0
                && Float.compare(up.getY(), rowStart) == 0
                && Float.compare(up.getW(), rowStart + 2F) == 0
                && !Float.isNaN(rowStart);
    }
}
