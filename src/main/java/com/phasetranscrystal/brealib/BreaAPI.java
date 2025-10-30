package com.phasetranscrystal.brealib;

import net.neoforged.fml.ModLoader;
import net.neoforged.neoforge.registries.RegisterEvent;

import com.phasetranscrystal.brealib.api.registry.BreaRegistry;
import com.phasetranscrystal.brealib.mixin.registrate.neoforge.RegisterEventAccessor;
import org.jetbrains.annotations.ApiStatus;

public class BreaAPI {

    /**
     * Post the register event for a specific (GT) registry. Internal use only, do not attempt to call this.
     */
    @ApiStatus.Internal
    public static <T> void postRegisterEvent(BreaRegistry<T> registry) {
        RegisterEvent registerEvent = RegisterEventAccessor.create(registry.key(), registry);
        ModLoader.postEventWrapContainerInModOrder(registerEvent);
    }
}
