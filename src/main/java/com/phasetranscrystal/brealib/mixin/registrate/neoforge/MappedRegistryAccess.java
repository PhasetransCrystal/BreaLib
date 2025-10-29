package com.phasetranscrystal.brealib.mixin.registrate.neoforge;

import net.minecraft.core.Holder;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(value = MappedRegistry.class, remap = false)
public interface MappedRegistryAccess<T> {

    @Accessor("byId")
    ObjectList<Holder.Reference<T>> getById();

    @Accessor("toId")
    Reference2IntMap<T> getToId();

    @Accessor("byLocation")
    Map<ResourceLocation, Holder.Reference<T>> getByLocation();

    @Accessor("byKey")
    Map<ResourceKey<T>, Holder.Reference<T>> getByKey();

    @Accessor("byValue")
    Map<T, Holder.Reference<T>> getByValue();

    @Accessor("registrationInfos")
    Map<ResourceKey<T>, RegistrationInfo> getRegistrationInfos();
}
