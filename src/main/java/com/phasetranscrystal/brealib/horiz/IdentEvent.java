package com.phasetranscrystal.brealib.horiz;

import net.neoforged.bus.api.Event;

import java.util.Objects;
import java.util.function.Consumer;

public record IdentEvent<T extends Event>(Class<T> event, Consumer<T> listener, boolean handleCancelled) {

    public IdentEvent(Class<T> event, Consumer<T> listener) {
        this(event, listener, false);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        IdentEvent<?> that = (IdentEvent<?>) o;
        return Objects.equals(event, that.event) && Objects.equals(listener, that.listener);
    }

    @Override
    public int hashCode() {
        return Objects.hash(event, listener);
    }
}
