package com.phasetranscrystal.brealib.api.machine;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.items.IItemHandlerModifiable;

import com.phasetranscrystal.brealib.BreaLib;
import com.phasetranscrystal.brealib.BreaUtility;
import com.phasetranscrystal.brealib.api.block.BlockProperties;
import com.phasetranscrystal.brealib.api.block.IMachineBlock;
import com.phasetranscrystal.brealib.api.block.MetaMachineBlock;
import com.phasetranscrystal.brealib.api.blockentities.MetaMachineBlockEntity;
import com.phasetranscrystal.brealib.utils.RelativeDirection;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class MetaMachine {

    @Setter
    @Getter
    @Nullable
    private UUID ownerUUID;
    @Getter
    public final IMachineBlockEntity holder;

    private final List<TickableSubscription> serverTicks;
    private final List<TickableSubscription> waitingToAdd;

    public MetaMachine(IMachineBlockEntity holder) {
        this.holder = holder;
        this.serverTicks = new ArrayList<>();
        this.waitingToAdd = new ArrayList<>();
    }

    //////////////////////////////////////
    // ***** Initialization ******//

    /// ///////////////////////////////////

    public @Nullable Level getLevel() {
        return holder.level();
    }

    public BlockPos getPos() {
        return holder.pos();
    }

    public BlockState getBlockState() {
        return holder.self().getBlockState();
    }

    public boolean isRemote() {
        return getLevel() == null ? BreaUtility.isClientThread() : getLevel().isClientSide;
    }

    public void notifyBlockUpdate() {
        holder.notifyBlockUpdate();
    }

    public void scheduleRenderUpdate() {
        holder.scheduleRenderUpdate();
    }

    public void scheduleNeighborShapeUpdate() {
        Level level = getLevel();
        BlockPos pos = getPos();

        if (level == null || pos == null)
            return;

        level.getBlockState(pos).updateNeighbourShapes(level, pos, Block.UPDATE_ALL);
    }

    public long getOffsetTimer() {
        return holder.getOffsetTimer();
    }

    public void markDirty() {
        holder.self().setChanged();
    }

    public boolean isInValid() {
        return holder.self().isRemoved();
    }

    public void onUnload() {
        for (TickableSubscription serverTick : serverTicks) {
            serverTick.unsubscribe();
        }
        serverTicks.clear();
    }

    public void onLoad() {}

    public void applyImplicitComponents(MetaMachineBlockEntity.ExDataComponentInput componentInput) {}

    public void collectImplicitComponents(DataComponentMap.Builder components) {}

    //////////////////////////////////////
    // ***** Tickable Manager ****//
    //////////////////////////////////////

    /**
     * For initialization. To get level and property fields after auto sync, you can subscribe it in {@link #onLoad()}
     * event.
     */
    @Nullable
    public TickableSubscription subscribeServerTick(Runnable runnable) {
        if (!isRemote()) {
            var subscription = new TickableSubscription(runnable);
            waitingToAdd.add(subscription);
            var blockState = getBlockState();
            if (!blockState.getValue(BlockProperties.SERVER_TICK)) {
                if (getLevel() instanceof ServerLevel serverLevel) {
                    blockState = blockState.setValue(BlockProperties.SERVER_TICK, true);
                    holder.self().setBlockState(blockState);
                    serverLevel.getServer().schedule(new TickTask(0, () -> {
                        if (!isInValid()) {
                            serverLevel.setBlockAndUpdate(getPos(),
                                    getBlockState().setValue(BlockProperties.SERVER_TICK, true));
                        }
                    }));
                }
            }
            return subscription;
        }
        /*
         * else if (getLevel() instanceof DummyWorld) {
         * var subscription = new TickableSubscription(runnable);
         * waitingToAdd.add(subscription);
         * return subscription;
         * }
         **/
        return null;
    }

    public void unsubscribe(@Nullable TickableSubscription current) {
        if (current != null) {
            current.unsubscribe();
        }
    }

    public final void serverTick() {
        executeTick();
        if (serverTicks.isEmpty() && waitingToAdd.isEmpty() && !isInValid()) {
            getLevel().setBlockAndUpdate(getPos(), getBlockState().setValue(BlockProperties.SERVER_TICK, false));
        }
    }

    public boolean isFirstDummyWorldTick = true;

    public void clientTick() {
        /*
         * if (getLevel() instanceof DummyWorld) {
         * if (isFirstDummyWorldTick) {
         * isFirstDummyWorldTick = false;
         * onLoad();
         * }
         * executeTick();
         * }
         */
    }

    private void executeTick() {
        if (!waitingToAdd.isEmpty()) {
            serverTicks.addAll(waitingToAdd);
            waitingToAdd.clear();
        }
        var iter = serverTicks.iterator();
        while (iter.hasNext()) {
            var tickable = iter.next();
            if (tickable.isStillSubscribed()) {
                tickable.run();
            }
            if (isInValid()) break;
            if (!tickable.isStillSubscribed()) {
                iter.remove();
            }
        }
    }

    //////////////////////////////////////
    // ******* Interaction *******//
    //////////////////////////////////////

    //////////////////////////////////////
    // ********** MISC ***********//

    /// ///////////////////////////////////

    @Nullable
    public static MetaMachine getMachine(BlockGetter level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof IMachineBlockEntity machineBlockEntity) {
            return machineBlockEntity.getMetaMachine();
        }
        return null;
    }

    public void clearInventory(IItemHandlerModifiable inventory) {
        for (int i = 0; i < inventory.getSlots(); i++) {
            ItemStack stackInSlot = inventory.getStackInSlot(i);
            if (!stackInSlot.isEmpty()) {
                inventory.setStackInSlot(i, ItemStack.EMPTY);
                Block.popResource(getLevel(), getPos(), stackInSlot);
            }
        }
    }

    public MachineDefinition getDefinition() {
        return holder.getDefinition();
    }

    /**
     * Called to obtain list of AxisAlignedBB used for collision testing, highlight rendering
     * and ray tracing this meta tile entity's block in world
     */
    public void addCollisionBoundingBox(List<VoxelShape> collisionList) {
        collisionList.add(Shapes.block());
    }

    public boolean canSetIoOnSide(@Nullable Direction direction) {
        return !hasFrontFacing() || getFrontFacing() != direction;
    }

    public static @NotNull Direction getFrontFacing(@Nullable MetaMachine machine) {
        return machine == null ? Direction.NORTH : machine.getFrontFacing();
    }

    public Direction getFrontFacing() {
        var blockState = getBlockState();
        if (blockState.getBlock() instanceof MetaMachineBlock machineBlock) {
            return machineBlock.getFrontFacing(blockState);
        }
        return Direction.NORTH;
    }

    public final boolean hasFrontFacing() {
        var blockState = getBlockState();
        if (blockState.getBlock() instanceof MetaMachineBlock machineBlock) {
            return machineBlock.getRotationState() != RotationState.NONE;
        }
        return false;
    }

    public boolean isFacingValid(Direction facing) {
        if (allowExtendedFacing()) {
            return true;
        }
        if (hasFrontFacing() && facing == getFrontFacing()) return false;
        var blockState = getBlockState();
        if (blockState.getBlock() instanceof MetaMachineBlock metaMachineBlock) {
            return metaMachineBlock.rotationState.test(facing);
        }
        return false;
    }

    public void setFrontFacing(Direction facing) {
        var oldFacing = getFrontFacing();

        if (allowExtendedFacing()) {
            var newUpwardsFacing = RelativeDirection.simulateAxisRotation(facing, oldFacing, getUpwardsFacing());
            setUpwardsFacing(newUpwardsFacing);
        }

        var blockState = getBlockState();
        if (blockState.getBlock() instanceof MetaMachineBlock metaMachineBlock && isFacingValid(facing)) {
            getLevel().setBlockAndUpdate(getPos(),
                    blockState.setValue(metaMachineBlock.rotationState.property, facing));
        }

        if (getLevel() != null && !getLevel().isClientSide) {
            notifyBlockUpdate();
            markDirty();
        }
    }

    public static @NotNull Direction getUpwardFacing(@Nullable MetaMachine machine) {
        return machine == null || !machine.allowExtendedFacing() ? Direction.NORTH :
                machine.getBlockState().getValue(IMachineBlock.UPWARDS_FACING_PROPERTY);
    }

    public Direction getUpwardsFacing() {
        return this.allowExtendedFacing() ? this.getBlockState().getValue(IMachineBlock.UPWARDS_FACING_PROPERTY) :
                Direction.NORTH;
    }

    public void setUpwardsFacing(@NotNull Direction upwardsFacing) {
        if (!getDefinition().isAllowExtendedFacing()) {
            return;
        }
        if (upwardsFacing.getAxis() == Direction.Axis.Y) {
            BreaLib.LOGGER.error("Tried to set upwards facing to invalid facing {}! Skipping", upwardsFacing);
            return;
        }
        var blockState = getBlockState();
        if (blockState.getBlock() instanceof MetaMachineBlock &&
                blockState.getValue(IMachineBlock.UPWARDS_FACING_PROPERTY) != upwardsFacing) {
            getLevel().setBlockAndUpdate(getPos(),
                    blockState.setValue(IMachineBlock.UPWARDS_FACING_PROPERTY, upwardsFacing));
            if (getLevel() != null && !getLevel().isClientSide) {
                notifyBlockUpdate();
                markDirty();
            }
        }
    }

    public void onRotated(Direction oldFacing, Direction newFacing) {}

    public boolean allowExtendedFacing() {
        return getDefinition().isAllowExtendedFacing();
    }

    public void onNeighborChanged(Block block, BlockPos fromPos, boolean isMoving) {}

    public void animateTick(RandomSource random) {}

    //////////////////////////////////////
    // ****** Ownership ********//
    //////////////////////////////////////

    //////////////////////////////////////
    // ****** Capability ********//
    //////////////////////////////////////
}
