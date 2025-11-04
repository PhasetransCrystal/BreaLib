package com.phasetranscrystal.brealib.api.material.registrate;

import net.neoforged.neoforge.registries.DeferredHolder;

import com.phasetranscrystal.brealib.api.material.MaterialDefinition;
import com.phasetranscrystal.brealib.api.material.property.PropertyKey;
import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.util.entry.RegistryEntry;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

public class MaterialEntry<DEF extends MaterialDefinition> extends RegistryEntry<MaterialDefinition, DEF> {

    @Getter
    private final String name;

    public MaterialEntry(AbstractRegistrate<?> owner, DeferredHolder<MaterialDefinition, DEF> key) {
        super(owner, key);
        name = key.getId().getPath();
    }

    @Getter
    private final Map<PropertyKey, RegistryEntry> propertyStorage = new HashMap<>();
}
