package com.phasetranscrystal.brealib.utils.reference;

import org.jetbrains.annotations.NotNull;

public record ReferencePoolInfo(Class<?> type, int unusedCount, int usingCount, int acquireCount,
                                int releaseCount, int addCount, int removeCount) {

    @Override
    public @NotNull String toString() {
        return String.format("%s[unused=%d,using=%d,acquire=%d,release=%d,add=%d,remove=%d]",
                type.getSimpleName(), unusedCount, usingCount,
                acquireCount, releaseCount, addCount, removeCount);
    }
}
