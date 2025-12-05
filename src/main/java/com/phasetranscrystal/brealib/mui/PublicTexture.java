package com.phasetranscrystal.brealib.mui;

import com.phasetranscrystal.brealib.BreaLib;
import com.phasetranscrystal.brealib.utils.BreaUtil;

import net.minecraft.resources.ResourceLocation;

import icyllis.modernui.graphics.Image;

import java.util.HashMap;
import java.util.Map;

public class PublicTexture {

    public static final ResourceLocation ROOT_LEFT_DEC = ResourceLocation.fromNamespaceAndPath(BreaLib.MOD_ID, "ui_public/left_dec.png");
    public static final ResourceLocation ROOT_NAME_DEC = ResourceLocation.fromNamespaceAndPath(BreaLib.MOD_ID, "ui_public/name_dec.png");
    public static final ResourceLocation ROOT_CLOSE_BUTTON = ResourceLocation.fromNamespaceAndPath(BreaLib.MOD_ID, "ui_public/close.png");
    public static final ResourceLocation ROOT_INSIDE_DEC1 = ResourceLocation.fromNamespaceAndPath(BreaLib.MOD_ID, "ui_public/dec1.png");

    public static final ResourceLocation ICON_BACKPACK = ResourceLocation.fromNamespaceAndPath(BreaLib.MOD_ID, "ui_public/icon/backpack.png");

    private static final Map<ResourceLocation, Image> PUBLIC_TEXTURES = new HashMap<>();

    public static Image getPublicImage(ResourceLocation texture) {
        if (!BreaUtil.isClientSide()) return null;

        Image image = PUBLIC_TEXTURES.get(texture);
        if (image != null && !image.isClosed()) return image;

        image = Image.create(texture.getNamespace(), texture.getPath());
        PUBLIC_TEXTURES.put(texture, image);
        return image;
    }
}
