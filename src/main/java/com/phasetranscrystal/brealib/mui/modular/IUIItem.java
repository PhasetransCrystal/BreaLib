package com.phasetranscrystal.brealib.mui.modular;

import com.phasetranscrystal.brealib.mui.factory.HeldItemUIFactory;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;

public interface IUIItem {

    default boolean shouldOpenUI(Player player, InteractionHand hand) {
        return hand == InteractionHand.MAIN_HAND && !player.level().isClientSide;
    }

    default InteractionResult tryOpenUI(Player player, InteractionHand hand) {
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.displayClientMessage(Component.literal("§7<DEBUG>§r Try Open UI"), false);
            return HeldItemUIFactory.INSTANCE.openUI(serverPlayer, hand) ?
                    InteractionResult.SUCCESS : InteractionResult.PASS;
        }
        return InteractionResult.CONSUME;
    }
}
