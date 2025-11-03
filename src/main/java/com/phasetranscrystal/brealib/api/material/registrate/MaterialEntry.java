package com.phasetranscrystal.brealib.api.material.registrate;

import net.neoforged.neoforge.registries.DeferredHolder;

import com.phasetranscrystal.brealib.api.material.MaterialDefinition;
import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.util.entry.RegistryEntry;

public class MaterialEntry<DEF extends MaterialDefinition> extends RegistryEntry<MaterialDefinition, DEF> {

    public MaterialEntry(AbstractRegistrate<?> owner, DeferredHolder<MaterialDefinition, DEF> key) {
        super(owner, key);
    }
}
