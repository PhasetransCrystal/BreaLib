package com.phasetranscrystal.brealib.api.tag;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

import com.google.common.collect.Table;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.phasetranscrystal.brealib.api.material.Material;
import com.phasetranscrystal.brealib.utils.FormattingUtil;
import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

@SuppressWarnings("unused")
@Accessors(chain = true, fluent = true)
public class BreaTagPrefix {

    public final static Map<String, BreaTagPrefix> PREFIXES = new HashMap<>();
    // public static final Map<BreaTagPrefix, OreType> ORES = new Object2ObjectLinkedOpenHashMap<>();

    public static final Codec<BreaTagPrefix> CODEC = Codec.STRING.flatXmap(
            str -> Optional.ofNullable(get(str)).map(DataResult::success)
                    .orElseGet(() -> DataResult.error(() -> "invalid TagPrefix: " + str)),
            prefix -> DataResult.success(prefix.name));

    public static void init() {
        // AddonFinder.getAddonList().forEach(IGTAddon::registerTagPrefixes);
    }

    public static BreaTagPrefix get(String name) {
        return PREFIXES.get(name);
    }

    public boolean isEmpty() {
        return this == NULL_PREFIX;
    }

    public static final BreaTagPrefix NULL_PREFIX = new BreaTagPrefix("null");

    public static class Conditions {}

    public record OreType(Supplier<BlockState> stoneType, Supplier<Material> material,
                          Supplier<BlockBehaviour.Properties> template, ResourceLocation baseModelLocation,
                          boolean isDoubleDrops, boolean isSand, boolean shouldDropAsItem) {}

    public record BlockProperties(Supplier<Supplier<RenderType>> renderType,
                                  UnaryOperator<BlockBehaviour.Properties> properties) {}

    @Getter
    public final String name;
    @Getter
    @Setter
    private String idPattern;

    // protected final List<TagType> tags = new ArrayList<>();
    @Setter
    @Getter
    public String langValue;

    @Getter
    @Setter
    private long materialAmount = -1;

    @Setter
    @Getter
    private boolean unificationEnabled;
    @Setter
    @Getter
    private boolean generateRecycling = false;
    @Setter
    private boolean generateItem;
    @Setter
    private boolean generateBlock;
    // @Getter
    // @Setter
    // private BlockProperties blockProperties = new BlockProperties(() -> RenderType::translucent,
    // UnaryOperator.identity());

    @Getter
    @Setter
    private @Nullable Predicate<Material> generationCondition;

    // @Nullable
    // @Getter
    // @Setter
    // private MaterialIconType materialIconType;

    @Setter
    private Supplier<Table<BreaTagPrefix, Material, ? extends Supplier<? extends ItemLike>>> itemTable;

    @Nullable
    @Getter
    @Setter
    private BiConsumer<Material, List<Component>> tooltip;

    private final Map<Material, Supplier<? extends ItemLike>[]> ignoredMaterials = new HashMap<>();
    private final Object2FloatMap<Material> materialAmounts = new Object2FloatOpenHashMap<>();

    @Getter
    @Setter
    private int maxStackSize = 64;

    // @Getter
    // private final List<MaterialStack> secondaryMaterials = new ArrayList<>();

    @Getter
    protected final Set<TagKey<Block>> miningToolTag = new HashSet<>();

    public BreaTagPrefix(String name) {
        this.name = name;
        String lowerCaseUnder = FormattingUtil.toLowerCaseUnder(name);
        this.idPattern = "%s_" + lowerCaseUnder;
        this.langValue = "%s " + FormattingUtil.toEnglishName(lowerCaseUnder);
        PREFIXES.put(name, this);
    }
}
