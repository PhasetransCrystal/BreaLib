package com.phasetranscrystal.brealib.mui.factory;

import com.phasetranscrystal.brealib.mui.modular.IUIHolder;
import com.phasetranscrystal.brealib.mui.modular.ModularUI;
import com.phasetranscrystal.brealib.utils.BreaUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import lombok.Getter;

public class HeldItemUIFactory extends UIFactory<HeldItemUIFactory.HeldItemHolder> {

    public static final HeldItemUIFactory INSTANCE = new HeldItemUIFactory();

    public HeldItemUIFactory() {
        super(BreaUtil.byPath("held_item"));
    }

    public final boolean openUI(ServerPlayer player, InteractionHand hand) {
        return openUI(new HeldItemHolder(player, hand), player);
    }

    @Override
    protected ModularUI createUITemplate(HeldItemHolder holder, Player entityPlayer) {
        return holder.createUI(entityPlayer);
    }

    @Override
    protected HeldItemHolder readHolderFromSyncData(RegistryFriendlyByteBuf syncData) {
        Player player = Minecraft.getInstance().player;
        return player == null ? null : new HeldItemHolder(player, syncData.readEnum(InteractionHand.class));
    }

    @Override
    protected void writeHolderToSyncData(RegistryFriendlyByteBuf syncData, HeldItemHolder holder) {
        syncData.writeEnum(holder.hand);
    }

    @Getter
    public static class HeldItemHolder implements IUIHolder {

        public Player player;
        public InteractionHand hand;
        public ItemStack held;

        public HeldItemHolder(Player player, InteractionHand hand) {
            this.player = player;
            this.hand = hand;
            held = player.getItemInHand(hand);
        }

        @Override
        public ModularUI createUI(Player entityPlayer) {
            if (held.getItem() instanceof IHeldItemUIHolder itemUIHolder) {
                return itemUIHolder.createUI(entityPlayer, this);
            }
            return null;
        }

        @Override
        public boolean isInvalid() {
            return !ItemStack.isSameItemSameComponents(player.getItemInHand(hand), held);
        }

        @Override
        public boolean isRemote() {
            return player.level().isClientSide;
        }

        @Override
        public void markAsDirty() {}
    }

    @Deprecated
    public interface IHeldItemUIHolder extends IUIHolder.Item {

    }
}
