package com.phasetranscrystal.brealib.test;

import com.phasetranscrystal.brealib.BreaLib;
import com.phasetranscrystal.brealib.utils.BreaUtil;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

import com.tterrag.registrate.Registrate;
import com.tterrag.registrate.util.entry.BlockEntry;
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
    public static BlockEntry<MuiTestBlock> BlockUITest;
    public static TagKey<Item> DebugItem = TagKey.create(Registries.ITEM, BreaUtil.byPath("debug"));

    static {
        DebugTab = REGISTRATE.defaultCreativeTab("debug_tab", builder -> builder.icon(Items.COMMAND_BLOCK::getDefaultInstance)
                .build())
                .lang(t -> "Debug Tab")
                .register();
        REGISTRATE.defaultCreativeTab(TestInit.DebugTab.getKey());
        UITest = REGISTRATE.item("ui_test", MuiTestItem::new)
                .tag(DebugItem)
                .lang("UI Test")
                .register();
        BlockUITest = REGISTRATE.block("block_ui_test", MuiTestBlock::new)
                .blockstate(() -> (ctx, prov) -> {
                    prov.create(ctx.getEntry(), prov.modLoc("block/controller_block_online"));
                })
                .lang("Block UI Test")
                .item()
                .model(() -> (ctx, prov) -> {
                    prov.createWithExistingModel(ctx.getEntry(), prov.modLoc("block/controller_block_offline"));
                })
                .tag(DebugItem)
                .build()
                .register();
    }
}
