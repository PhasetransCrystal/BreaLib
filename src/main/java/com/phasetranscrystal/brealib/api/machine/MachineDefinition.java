package com.phasetranscrystal.brealib.api.machine;

import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import com.phasetranscrystal.brealib.api.block.IMachineBlock;
import com.phasetranscrystal.brealib.api.item.MetaMachineItem;
import com.phasetranscrystal.brealib.utils.ShapeUtil;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.*;

public class MachineDefinition implements Supplier<IMachineBlock> {

    @Getter
    private final ResourceLocation id;
    // This is only stored here for KJS use.
    @Getter
    @Setter
    @Nullable
    private String langValue;
    @Setter
    private Supplier<? extends Block> blockSupplier;
    @Setter
    private Supplier<? extends MetaMachineItem> itemSupplier;
    @Setter
    private Supplier<BlockEntityType<? extends BlockEntity>> blockEntityTypeSupplier;
    @Setter
    private Function<IMachineBlockEntity, MetaMachine> machineSupplier;
    @Getter
    @Setter
    private int tier;
    @Getter
    @Setter
    private int defaultPaintingColor;
    @Getter
    @Setter
    private boolean alwaysTryModifyRecipe;
    @Getter
    @Setter
    private boolean regressWhenWaiting = true;
    /**
     * Whether this machine can be rotated or face upwards.
     */
    @Getter
    @Setter
    private boolean allowExtendedFacing;
    @Setter
    private VoxelShape shape;
    @Getter
    @Setter
    private boolean renderWorldPreview;
    @Getter
    @Setter
    private boolean renderXEIPreview;
    private final Map<Direction, VoxelShape> cache = new EnumMap<>(Direction.class);
    @Getter
    @Setter
    private BiConsumer<ItemStack, List<Component>> tooltipBuilder;
    @Getter
    @Setter
    private Supplier<BlockState> appearance;

    protected MachineDefinition(ResourceLocation id) {
        this.id = id;
    }

    public static MachineDefinition createDefinition(ResourceLocation id) {
        return new MachineDefinition(id);
    }

    public Block getBlock() {
        return blockSupplier.get();
    }

    public MetaMachineItem getItem() {
        return itemSupplier.get();
    }

    public BlockEntityType<? extends BlockEntity> getBlockEntityType() {
        return blockEntityTypeSupplier.get();
    }

    public MetaMachine createMetaMachine(IMachineBlockEntity blockEntity) {
        return machineSupplier.apply(blockEntity);
    }

    public ItemStack asStack() {
        return new ItemStack(getItem());
    }

    public ItemStack asStack(int count) {
        return new ItemStack(getItem(), count);
    }

    public VoxelShape getShape(Direction direction) {
        if (shape.isEmpty() || shape == Shapes.block() || direction == Direction.NORTH) return shape;
        return this.cache.computeIfAbsent(direction, dir -> ShapeUtil.rotate(shape, dir));
    }

    @Override
    public IMachineBlock get() {
        return (IMachineBlock) blockSupplier.get();
    }

    public String getName() {
        return id.getPath();
    }

    @Override
    public String toString() {
        return "[Definition: %s]".formatted(id);
    }

    public String getDescriptionId() {
        return getBlock().getDescriptionId();
    }

    public BlockState defaultBlockState() {
        return getBlock().defaultBlockState();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MachineDefinition that = (MachineDefinition) o;

        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    static final ThreadLocal<MachineDefinition> STATE = new ThreadLocal<>();

    public static MachineDefinition getBuilt() {
        return STATE.get();
    }

    public static void setBuilt(MachineDefinition state) {
        STATE.set(state);
    }

    public static void clearBuilt() {
        STATE.remove();
    }
}
