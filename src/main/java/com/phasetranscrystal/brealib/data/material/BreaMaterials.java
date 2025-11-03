package com.phasetranscrystal.brealib.data.material;

import com.phasetranscrystal.brealib.api.material.MaterialDefinition;
import com.phasetranscrystal.brealib.api.material.registrate.MaterialEntry;
import com.phasetranscrystal.brealib.data.misc.BreaCreativeModeTabs;

import static com.phasetranscrystal.brealib.api.registry.registrate.BreaRegistrate.Brea;

public class BreaMaterials {

    static {
        Brea.defaultCreativeTab(BreaCreativeModeTabs.MATERIAL.getKey());
    }

    public static MaterialEntry<MaterialDefinition> Iron = Brea.material("iron")
            .setElement(BreaElements.Fe)
            .register();
    public static MaterialEntry<MaterialDefinition> Gold = Brea.material("gold")
            .setElement(BreaElements.Au)
            .register();

    public static MaterialEntry<MaterialDefinition> Electrum = Brea.material("electrum")
            .setComponents(BreaElements.Au, 1, BreaElements.Ag, 1)
            .ingot()
            .dust()
            .block("%s_block")
            .register();

    public static void init() {}
}
