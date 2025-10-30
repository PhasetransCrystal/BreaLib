package com.phasetranscrystal.brealib.data.misc;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import com.phasetranscrystal.brealib.BreaLib;
import com.phasetranscrystal.brealib.BreaUtility;
import com.phasetranscrystal.brealib.api.registry.registrate.BreaRegistrate;
import com.phasetranscrystal.brealib.data.item.BreaItems;
import com.tterrag.registrate.util.entry.RegistryEntry;
import org.jetbrains.annotations.NotNull;

import static com.phasetranscrystal.brealib.api.registry.registrate.BreaRegistrate.Brea;

public class BreaCreativeModeTabs {

    public static RegistryEntry<CreativeModeTab, CreativeModeTab> MATERIAL_FLUID = Brea.defaultCreativeTab("material_fluid",
            builder -> builder.displayItems(new RegistrateDisplayItemsGenerator("material_fluid", Brea))
                    .icon(() -> BreaItems.TestItem.asStack())
                    .title(Brea.addLang("itemGroup", BreaUtility.id("material_fluid"),
                            BreaLib.NAME + " Material Fluid Containers"))
                    .build())
            .register();

    public static void init() {}

    public record RegistrateDisplayItemsGenerator(String name,
                                                  BreaRegistrate registrate)
            implements CreativeModeTab.DisplayItemsGenerator {

        @Override
        public void accept(@NotNull CreativeModeTab.ItemDisplayParameters itemDisplayParameters,
                           @NotNull CreativeModeTab.Output output) {
            var tab = registrate.get(name, Registries.CREATIVE_MODE_TAB);
            for (var entry : registrate.getAll(Registries.BLOCK)) {
                if (!registrate.isInCreativeTab(entry, tab))
                    continue;
                Item item = entry.get().asItem();
                if (item == Items.AIR)
                    continue;
            }
            for (var entry : registrate.getAll(Registries.ITEM)) {
                if (!registrate.isInCreativeTab(entry, tab))
                    continue;
                Item item = entry.get();
                output.accept(item);
            }
        }
    }
}
