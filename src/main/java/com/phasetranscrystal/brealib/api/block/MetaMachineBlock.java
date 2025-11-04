package com.phasetranscrystal.brealib.api.block;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

import com.phasetranscrystal.brealib.api.machine.MachineDefinition;
import com.phasetranscrystal.brealib.api.machine.RotationState;
import lombok.Getter;

public class MetaMachineBlock extends AppearanceBlock implements IMachineBlock {

    @Getter
    public final MachineDefinition definition;
    @Getter
    public final RotationState rotationState;

    public MetaMachineBlock(Properties properties, MachineDefinition definition) {
        super(properties);
        this.definition = definition;
        this.rotationState = RotationState.get();
        if (rotationState != RotationState.NONE) {
            BlockState defaultState = this.defaultBlockState().setValue(rotationState.property,
                    rotationState.defaultDirection);
            if (definition.isAllowExtendedFacing()) {
                defaultState = defaultState.setValue(IMachineBlock.UPWARDS_FACING_PROPERTY, Direction.NORTH);
            }
            registerDefaultState(defaultState);
        }
    }

    public Direction getFrontFacing(BlockState state) {
        return getRotationState() == RotationState.NONE ? Direction.NORTH : state.getValue(getRotationState().property);
    }
}
