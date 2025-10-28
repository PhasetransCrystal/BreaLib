package com.phasetranscrystal.brealib.api.registrate;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;

import com.tterrag.registrate.Registrate;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class BreaRegistrate extends Registrate {

    private static final Map<String, BreaRegistrate> EXISTING_REGISTRATES = new Object2ObjectOpenHashMap<>();

    private final AtomicBoolean registered = new AtomicBoolean(false);

    public static BreaRegistrate create(String modId) {
        if (EXISTING_REGISTRATES.containsKey(modId)) {
            return EXISTING_REGISTRATES.get(modId);
        }
        var registrate = new BreaRegistrate(modId);
        ModList.get().getModContainerById(modId)
                .map(ModContainer::getEventBus)
                .ifPresent(registrate::registerEventListeners);

        EXISTING_REGISTRATES.put(modId, registrate);
        return registrate;
    }

    @Override
    public BreaRegistrate registerEventListeners(IEventBus bus) {
        if (!this.registered.getAndSet(true)) {
            super.registerEventListeners(bus);
        }
        return (BreaRegistrate) this.self();
    }

    protected BreaRegistrate(String modId) {
        super(modId);
    }

    public static BreaRegistrate Brea = create("breanonatomic");

    static {
        Brea.defaultCreativeTab((ResourceKey<CreativeModeTab>) null);
    }
}
