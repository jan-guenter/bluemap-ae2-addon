/*
 * SPDX-License-Identifier: LGPL-3.0-only
 */
package io.github.janguenter.bluemap.ae2.model;

/** Exact AE2 19.2.17 cable colors and static rendering tint variants. */
public enum CableColor {

    WHITE("white", "white", 0xb4b4b4, 0xe0e0e0, 0xf9f9f9),
    LIGHT_GRAY("light_gray", "light_gray", 0x7e7e7e, 0xa09fa0, 0xc4c4c4),
    GRAY("gray", "gray", 0x4f4f4f, 0x6c6b6c, 0x949294),
    BLACK("black", "black", 0x131313, 0x272727, 0x3b3b3b),
    LIME("lime", "lime", 0x4ec04e, 0x70e259, 0xb3f86d),
    YELLOW("yellow", "yellow", 0xffcf40, 0xffe359, 0xf4ff80),
    ORANGE("orange", "orange", 0xd9782f, 0xeca23c, 0xf2ba49),
    BROWN("brown", "brown", 0x6e4a12, 0x7e5c16, 0x8e6e1a),
    RED("red", "red", 0xaa212b, 0xd73e42, 0xf07665),
    PINK("pink", "pink", 0xd86eaa, 0xff99bb, 0xfbcad5),
    MAGENTA("magenta", "magenta", 0xc15189, 0xd5719c, 0xe69ebf),
    PURPLE("purple", "purple", 0x6e5cb8, 0x915dcd, 0xb06fdd),
    BLUE("blue", "blue", 0x337ff0, 0x3894ff, 0x40c1ff),
    LIGHT_BLUE("light_blue", "light_blue", 0x69b9ff, 0x70d2ff, 0x80f7ff),
    CYAN("cyan", "cyan", 0x22b0ae, 0x2fccb7, 0x65e8c9),
    GREEN("green", "green", 0x079b6b, 0x17b86d, 0x32d850),
    TRANSPARENT("fluix", "transparent", 0x5a479e, 0x915dcd, 0xe2a3e3);

    private final String registryPrefix;
    private final String textureName;
    private final int darkRgb;
    private final int mediumRgb;
    private final int brightRgb;

    CableColor(
            String registryPrefix,
            String textureName,
            int darkRgb,
            int mediumRgb,
            int brightRgb
    ) {
        this.registryPrefix = registryPrefix;
        this.textureName = textureName;
        this.darkRgb = darkRgb;
        this.mediumRgb = mediumRgb;
        this.brightRgb = brightRgb;
    }

    public String registryPrefix() {
        return registryPrefix;
    }

    public String textureName() {
        return textureName;
    }

    public int darkRgb() {
        return darkRgb;
    }

    public int brightRgb() {
        return brightRgb;
    }

    public int mediumRgb() {
        return mediumRgb;
    }

    /** AE2 grid colors connect when either side is fluix/transparent or both match. */
    public boolean connectsTo(CableColor other) {
        return this == TRANSPARENT || other == TRANSPARENT || this == other;
    }
}
