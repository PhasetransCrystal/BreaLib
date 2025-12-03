package com.phasetranscrystal.brealib.utils;

import com.phasetranscrystal.brealib.BreaLib;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.data.loading.DatagenModLoader;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import org.apache.logging.log4j.util.Strings;

import java.nio.file.Path;

public final class BreaUtil {

    private static final ResourceLocation TEMPLATE_LOCATION = ResourceLocation.fromNamespaceAndPath(BreaLib.Core_ID, "");

    public static ResourceLocation byPath(String path) {
        if (Strings.isBlank(path)) {
            return TEMPLATE_LOCATION;
        }
        return TEMPLATE_LOCATION.withPath(FormattingUtil.toLowerCaseUnder(path));
    }

    public static String appendIdString(String id) {
        return id.indexOf(':') == -1 ? (BreaLib.Core_ID + ":" + id) : id;
    }

    public static ResourceLocation appendId(String id) {
        String[] strings = new String[] { BreaLib.Core_ID, id };
        int i = id.indexOf(':');
        if (i >= 0) {
            strings[1] = id.substring(i + 1);
            if (i >= 1) {
                strings[0] = id.substring(0, i);
            }
        }
        return ResourceLocation.fromNamespaceAndPath(strings[0], strings[1]);
    }

    public static boolean isProd() {
        return FMLLoader.isProduction();
    }

    public static boolean isDev() {
        return !isProd();
    }

    public static boolean isDataGen() {
        return DatagenModLoader.isRunningDataGen();
    }

    public static boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }

    public static boolean isClientSide() {
        return FMLEnvironment.dist == Dist.CLIENT;
    }

    public static boolean isClientThread() {
        return isClientSide() && Minecraft.getInstance().isSameThread();
    }

    public static boolean canGetServerLevel() {
        if (isClientSide()) {
            return Minecraft.getInstance().level != null;
        }
        var server = getMinecraftServer();
        return server != null &&
                !(server.isStopped() || server.isShutdown() || !server.isRunning() || server.isCurrentlySaving());
    }

    public static MinecraftServer getMinecraftServer() {
        return ServerLifecycleHooks.getCurrentServer();
    }

    public static String platformName() {
        return "NeoForge";
    }

    public static boolean isForge() {
        return true;
    }

    public static Path getGamePath() {
        return FMLLoader.getGamePath();
    }
}
