package com.phasetranscrystal.brealib.api.registry;

import net.minecraft.core.*;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.VarInt;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModLoadingContext;

import com.google.common.collect.*;
import com.mojang.serialization.Lifecycle;
import com.phasetranscrystal.brealib.BreaLib;
import com.phasetranscrystal.brealib.mixin.registrate.neoforge.MappedRegistryAccess;
import com.phasetranscrystal.brealib.mixin.registrate.neoforge.ResourceKeyAccessor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.*;

public class BreaRegistry<T> extends MappedRegistry<T> {

    public BreaRegistry(ResourceLocation registryName) {
        this(ResourceKeyAccessor.callCreate(BreaRegistries.ROOT_REGISTRY_NAME, registryName));
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public BreaRegistry(ResourceKey<? extends Registry<T>> registryKey) {
        super(registryKey, Lifecycle.stable());
        if (!registryKey.location().equals(BreaRegistries.ROOT_REGISTRY_NAME)) {
            BreaRegistries.ROOT.register((ResourceKey) registryKey, this, RegistrationInfo.BUILT_IN);
        }
    }

    @Override
    public @NotNull Registry<T> freeze() {
        if (!checkActiveModContainerIsOwner()) {
            return this;
        }
        return super.freeze();
    }

    @Override
    public void unfreeze(boolean clearTags) {
        if (!checkActiveModContainerIsOwner()) {
            return;
        }
        super.unfreeze(clearTags);
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean checkActiveModContainerIsOwner() {
        ModContainer container = ModLoadingContext.get().getActiveContainer();
        return container != null && (container.getModId().equals(this.key().location().getNamespace()) ||
                container.getModId().equals(BreaLib.MOD_ID) ||
                container.getModId().equals("minecraft")); // check for minecraft in case of datagen or a mishap
    }

    public <V extends T> V register(ResourceKey<T> key, V value) {
        if (containsKey(key)) {
            throw new IllegalStateException(
                    "[BreaRegistry] registry %s contains key %s already".formatted(key().location(), key.location()));
        }
        return this.registerOrOverride(key, value);
    }

    public <V extends T> V register(ResourceLocation key, V value) {
        return this.register(ResourceKey.create(this.key(), key), value);
    }

    @Nullable
    public <V extends T> V replace(ResourceLocation key, V value) {
        return this.replace(ResourceKey.create(this.key(), key), value);
    }

    @Nullable
    public <V extends T> V replace(ResourceKey<T> key, V value) {
        if (!containsKey(key)) {
            BreaLib.LOGGER.warn("[BreaRegistry] couldn't find key {} in registry {}", key, key().location());
        }
        registerOrOverride(key, value);
        return value;
    }

    public <V extends T> V registerOrOverride(ResourceLocation key, V value) {
        return this.registerOrOverride(ResourceKey.create(this.key(), key), value);
    }

    public <V extends T> V registerOrOverride(ResourceKey<T> key, V value) {
        super.register(key, value, RegistrationInfo.BUILT_IN);
        return value;
    }

    @UnmodifiableView
    @Override
    public @NotNull Set<Map.Entry<ResourceKey<T>, T>> entrySet() {
        return super.entrySet();
    }

    @UnmodifiableView
    public Set<Map.Entry<ResourceLocation, T>> entries() {
        return Collections.unmodifiableSet(Maps.transformValues(((MappedRegistryAccess<T>) this).getByLocation(), Holder::value).entrySet());
    }

    @UnmodifiableView
    public Map<ResourceLocation, T> registry() {
        return Collections.unmodifiableMap(Maps.transformValues(((MappedRegistryAccess<T>) this).getByLocation(), Holder::value));
    }

    public T getOrDefault(ResourceLocation name, T defaultValue) {
        var value = get(name);
        return value.map(Holder::value).orElse(defaultValue);
    }

    public ResourceLocation getOrDefaultKey(T value, ResourceLocation defaultKey) {
        ResourceLocation key = getKey(value);
        return key != null ? key : defaultKey;
    }

    public boolean remove(ResourceLocation key) {
        return remove(ResourceKey.create(this.key(), key));
    }

    public boolean remove(ResourceKey<T> key) {
        Holder<T> holder = this.getHolder(key).orElse(null);
        if (holder == null) {
            return false;
        }
        return remove(holder);
    }

    public Optional<Holder.Reference<T>> getHolder(ResourceKey<T> key) {
        return Optional.ofNullable(((MappedRegistryAccess<T>) this).getByKey().get(this.resolve(key)));
    }

    public boolean remove(Holder<T> holder) {
        MappedRegistryAccess<T> mixin;
        try {
            mixin = (MappedRegistryAccess<T>) this;
        } catch (Exception e) {
            return false;
        }
        ResourceKey<T> key = holder.getKey();
        boolean removed = true;
        removed &= mixin.getByKey().remove(key) != null;
        removed &= mixin.getByLocation().remove(key.location()) != null;
        removed &= mixin.getByValue().remove(holder.value()) != null;
        removed &= mixin.getById().remove(holder);
        removed &= mixin.getToId().removeInt(holder.value()) != -1;
        removed &= mixin.getRegistrationInfos().remove(key) != null;
        removed &= mixin.getRegistrationInfos().remove(key) != null;

        return removed;
    }

    public StreamCodec<RegistryFriendlyByteBuf, T> streamCodec() {
        return new StreamCodec<>() {

            public @NotNull T decode(@NotNull RegistryFriendlyByteBuf buffer) {
                return BreaRegistry.this.byIdOrThrow(VarInt.read(buffer));
            }

            public void encode(@NotNull RegistryFriendlyByteBuf buffer, @NotNull T value) {
                VarInt.write(buffer, BreaRegistry.this.getIdOrThrow(value));
            }
        };
    }

    @Override
    public void clear(boolean full) {
        super.clear(full);
    }
}
