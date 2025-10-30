package com.phasetranscrystal.brealib.data.item;

import net.minecraft.world.item.Item;

import com.phasetranscrystal.brealib.data.misc.BreaCreativeModeTabs;
import com.tterrag.registrate.util.entry.ItemEntry;

import static com.phasetranscrystal.brealib.api.registry.registrate.BreaRegistrate.Brea;

public class BreaItems {

    static {
        Brea.defaultCreativeTab(BreaCreativeModeTabs.MATERIAL_FLUID.getKey());
    }

    public static ItemEntry<Item> TestItem = Brea.item("test_item", Item::new)
            .lang("Test Item")
            .register();

    public static void init() {}
}
