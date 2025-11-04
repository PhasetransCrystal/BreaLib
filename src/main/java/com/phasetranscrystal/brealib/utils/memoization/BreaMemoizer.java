package com.phasetranscrystal.brealib.utils.memoization;

import net.minecraft.world.level.block.Block;

import java.util.function.Supplier;

public class BreaMemoizer {

    public static <T> MemoizedSupplier<T> memoize(Supplier<T> delegate) {
        return new MemoizedSupplier<>(delegate);
    }

    public static <T extends Block> MemoizedBlockSupplier<T> memoizeBlockSupplier(Supplier<T> delegate) {
        return new MemoizedBlockSupplier<>(delegate);
    }
}
