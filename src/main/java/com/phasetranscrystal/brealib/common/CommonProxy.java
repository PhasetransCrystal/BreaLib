package com.phasetranscrystal.brealib.common;

import com.phasetranscrystal.brealib.BreaLib;
import com.phasetranscrystal.brealib.api.registry.registrate.BreaRegistrate;
import com.phasetranscrystal.brealib.client.ClientProxy;
import com.phasetranscrystal.brealib.data.block.BreaBlocks;
import com.phasetranscrystal.brealib.data.item.BreaItems;
import com.phasetranscrystal.brealib.data.misc.BreaCreativeModeTabs;

public class CommonProxy {

    public CommonProxy() {
        var modEventBus = BreaLib.getModEventBus();
        modEventBus.register(ClientProxy.class);
        BreaRegistrate.Brea.registerEventListeners(modEventBus);
        init();
    }

    public void init() {
        initMaterials();
        BreaCreativeModeTabs.init();
        BreaBlocks.init();
        BreaItems.init();
    }

    private static void initMaterials() {}
}
