/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.adapter.bluemap523;

import de.bluecolored.bluemap.core.resources.pack.PackVersion;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.texture.Texture;
import de.bluecolored.bluemap.core.util.Key;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Loads AppMek test resources only from the operator-supplied pinned artifacts.
 *
 * <p>No upstream model, texture, or pixel payload is represented in this test
 * source tree. The decoded Drive texture is inserted explicitly because the
 * bare BlueMap resource loader only retains textures selected by its normal
 * collection lifecycle.</p>
 */
final class AppMekExternalResourceTestSupport {

    private AppMekExternalResourceTestSupport() {
    }

    static ResourcePack exactResources() throws Exception {
        Path ae2Jar = requiredPath("bluemapAe2.testAe2Jar");
        Path appMekJar = requiredPath("bluemapAe2.testAppMekJar");
        Path blueMapSource = requiredPath("bluemapAe2.testBlueMapSourcePath");
        ResourcePack resources = new ResourcePack(new PackVersion(34, 0));
        resources.loadResources(List.of(
                blueMapSource.resolve("core/src/main/resourceExtensions"),
                ae2Jar,
                appMekJar
        ));
        putTexture(resources, appMekJar, AppMekResourceModels.DRIVE_TEXTURE);
        return resources;
    }

    static void putExactAe2Textures(ResourcePack resources, Iterable<Key> keys)
            throws Exception {
        Path ae2Jar = requiredPath("bluemapAe2.testAe2Jar");
        for (Key key : keys) {
            putTexture(resources, ae2Jar, key);
        }
    }

    private static void putTexture(ResourcePack resources, Path jar, Key key)
            throws Exception {
        String path = "assets/" + key.getNamespace() + "/textures/"
                + key.getValue() + ".png";
        try (ZipFile archive = new ZipFile(jar.toFile())) {
            ZipEntry entry = archive.getEntry(path);
            assertNotNull(entry, path);
            BufferedImage image = ImageIO.read(archive.getInputStream(entry));
            assertNotNull(image, path);
            resources.getTextures().put(key, Texture.from(key, image));
        }
    }

    private static Path requiredPath(String property) {
        String value = System.getProperty(property, "");
        assertFalse(value.isBlank(), property);
        Path path = Path.of(value);
        assertTrue(Files.exists(path), path.toString());
        return path;
    }
}
