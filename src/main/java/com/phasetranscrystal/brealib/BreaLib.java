package com.phasetranscrystal.brealib;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

import com.phasetranscrystal.brealib.api.registry.registrate.BreaRegistrate;
import com.phasetranscrystal.brealib.dev.DevBreaLib;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Main mod class.
 * <p>
 * An example for blocks is in the `blocks` package of this mod.
 */
@Mod(BreaLib.MOD_ID)
public class BreaLib {

    public static final String MOD_ID = "brealib";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    public BreaLib(ModContainer container, IEventBus modEventBus) {
        BreaRegistrate.Brea.registerEventListeners(modEventBus);
        if (BreaUtility.isDev()) {
            new DevBreaLib(container, modEventBus);
        }
    }
}
