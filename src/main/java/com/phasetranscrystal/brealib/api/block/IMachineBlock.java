package com.phasetranscrystal.brealib.api.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

import com.phasetranscrystal.brealib.api.machine.IMachineBlockEntity;
import com.phasetranscrystal.brealib.api.machine.MachineDefinition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IMachineBlock extends EntityBlock {

    EnumProperty<Direction> UPWARDS_FACING_PROPERTY = EnumProperty.create("upwards_facing", Direction.class, Direction.Plane.HORIZONTAL);

    default Block self() {
        return (Block) this;
    }

    MachineDefinition getDefinition();

    @Nullable
    @Override
    default BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return getDefinition().getBlockEntityType().create(pos, state);
    }

    @Nullable
    @Override
    default <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                   BlockEntityType<T> blockEntityType) {
        if (blockEntityType == getDefinition().getBlockEntityType()) {
            if (state.getValue(BlockProperties.SERVER_TICK) && !level.isClientSide) {
                return (pLevel, pPos, pState, pTile) -> {
                    if (pTile instanceof IMachineBlockEntity metaMachine) {
                        metaMachine.getMetaMachine().serverTick();
                    }
                };
            }
            if (level.isClientSide) {
                return (pLevel, pPos, pState, pTile) -> {
                    if (pTile instanceof IMachineBlockEntity metaMachine) {
                        metaMachine.getMetaMachine().clientTick();
                    }
                };
            }
        }
        return null;
    }

    default void attachCapabilities(RegisterCapabilitiesEvent event) {}
}
