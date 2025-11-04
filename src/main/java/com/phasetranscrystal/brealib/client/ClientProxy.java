package com.phasetranscrystal.brealib.client;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

import com.phasetranscrystal.brealib.BreaLib;
import com.phasetranscrystal.brealib.common.CommonProxy;

public class ClientProxy extends CommonProxy {

    public ClientProxy() {
        super();
        var modEventBus = BreaLib.getModEventBus();
        modEventBus.register(ClientProxy.class);
    }

    @Override
    public void init() {
        super.init();
    }

    @SubscribeEvent
    private static void onCommonSetup(FMLCommonSetupEvent event) {}
}
