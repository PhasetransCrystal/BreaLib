package com.phasetranscrystal.brealib.mui.modular;

import com.phasetranscrystal.brealib.mui.factory.HeldItemUIFactory;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;

public interface IUIBlock {

    default boolean shouldOpenUI(Player player, InteractionHand hand, BlockHitResult hit) {
        return hand == InteractionHand.MAIN_HAND && !player.level().isClientSide;
    }

    default InteractionResult tryToOpenUI(Player player, InteractionHand hand, BlockHitResult hit) {
        if (shouldOpenUI(player, hand, hit)) {
            if (player instanceof ServerPlayer serverPlayer) {
                serverPlayer.displayClientMessage(Component.literal("§7<DEBUG>§r Try Open UI"), false);
                HeldItemUIFactory.INSTANCE.openUI(serverPlayer, hand);
            }
            return InteractionResult.PASS;
        }
        return player.level().isClientSide ? InteractionResult.SUCCESS : InteractionResult.CONSUME;
    }
}
