package com.phasetranscrystal.brealib;

import com.phasetranscrystal.brealib.mui.TestingFragment;
import com.phasetranscrystal.brealib.utils.BreaUtil;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import icyllis.modernui.mc.MuiModApi;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(BreaLib.MOD_ID)
public class BreaLib {

    public static final String MOD_ID = "brealib";
    public static final String Core_ID = "breacore";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);
    public static final String DISPLAY_NAME = "BreaLib";

    public BreaLib(IEventBus event) {
        if (BreaUtil.isDev()) {
            ITEM_REG.register(event);
        }
    }

    public static final DeferredRegister.Items ITEM_REG = DeferredRegister.createItems(MOD_ID);

    public static final DeferredItem<TestingMUIItem> TESTING_MUI_ITEM = ITEM_REG.register("mui_test", TestingMUIItem::new);

    public static class TestingMUIItem extends Item {

        public TestingMUIItem(ResourceLocation key) {
            super(new Properties().setId(ResourceKey.create(Registries.ITEM, key)));
        }

        @Override
        public InteractionResult use(Level level, Player player, InteractionHand hand) {
            if (hand == InteractionHand.MAIN_HAND && level.isClientSide) {
                MuiModApi.openScreen(new TestingFragment());
            }
            return InteractionResult.PASS;
        }
    }
}
