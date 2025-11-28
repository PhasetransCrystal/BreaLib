package com.phasetranscrystal.brealib;

import com.phasetranscrystal.brealib.horiz.BreaHoriz;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


@Mod(BreaLib.MOD_ID)
public class BreaLib {

    public static final String MOD_ID = "brealib";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);
    public static final String NAME = "BreakdownCore";

    @Getter
    private static ModContainer modContainer;
    @Getter
    private static IEventBus modEventBus;

    public BreaLib(IEventBus modEventBus) {
        BreaHoriz.bootstrap(modEventBus);
    }

    public static ResourceLocation byPath(String path) {
        return ResourceLocation.fromNamespaceAndPath(BreaLib.MOD_ID, path);
    }

    public static Logger loggerByModule(String module) {
        return LogManager.getLogger("BreaLib:" + module);
    }
}
