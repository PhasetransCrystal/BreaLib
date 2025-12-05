package com.phasetranscrystal.brealib.test;

import com.phasetranscrystal.brealib.BreaLib;
import com.phasetranscrystal.brealib.utils.BreaUtil;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

import com.tterrag.registrate.Registrate;
import com.tterrag.registrate.util.entry.ItemEntry;
import com.tterrag.registrate.util.entry.RegistryEntry;

@Mod(BreaLib.MOD_ID)
public class TestInit {

    public static Registrate REGISTRATE = Registrate.create(BreaLib.Core_ID);

    static {
        REGISTRATE.defaultCreativeTab((ResourceKey<CreativeModeTab>) null);
    }

    public TestInit(IEventBus modEventBus, ModContainer container) {
        REGISTRATE.registerEventListeners(modEventBus);
    }

    public static RegistryEntry<CreativeModeTab, CreativeModeTab> DebugTab;
    public static ItemEntry<MuiTestItem> UITest;

    static {
        DebugTab = REGISTRATE.defaultCreativeTab("debug_tab", builder -> builder.icon(Items.COMMAND_BLOCK::getDefaultInstance)
                .build())
                .lang(t -> "Debug Tab")
                .register();
        REGISTRATE.defaultCreativeTab(TestInit.DebugTab.getKey());
        UITest = REGISTRATE.item("ui_test", MuiTestItem::new)
                .tag(TagKey.create(Registries.ITEM, BreaUtil.byPath("ui_test")))
                .lang("UITest")
                .register();
    }
}
