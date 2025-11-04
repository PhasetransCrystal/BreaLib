package com.phasetranscrystal.brealib.api.material.registrate;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredHolder;

import com.phasetranscrystal.brealib.api.material.Element;
import com.phasetranscrystal.brealib.api.material.MaterialDefinition;
import com.phasetranscrystal.brealib.api.material.property.MaterialProperties;
import com.phasetranscrystal.brealib.api.material.property.PropertyKey;
import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.builders.AbstractBuilder;
import com.tterrag.registrate.builders.BuilderCallback;
import com.tterrag.registrate.util.entry.RegistryEntry;
import com.tterrag.registrate.util.nullness.NonnullType;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

import static com.phasetranscrystal.brealib.api.registry.BreaRegistries.MATERIAL_RESOURCEKEY;

@Accessors(chain = true, fluent = true)
public class MaterialBuilder<DEF extends MaterialDefinition, P> extends AbstractBuilder<MaterialDefinition, DEF, P, MaterialBuilder<DEF, P>> {

    @FunctionalInterface
    public interface MaterialDefinitionFactory<DEF extends MaterialDefinition> {

        DEF apply(MaterialDefinition.MaterialInfo info, MaterialProperties properties);

        default DEF createNew(ResourceLocation location) {
            return apply(new MaterialDefinition.MaterialInfo(location), new MaterialProperties());
        }
    }

    public static <DEF extends MaterialDefinition, P> MaterialBuilder<DEF, P> create(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback, MaterialDefinitionFactory<DEF> factory) {
        return new MaterialBuilder<>(owner, parent, name, callback, factory)
                .defaultProperty();
    }

    protected MaterialBuilder(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback, MaterialDefinitionFactory<DEF> factory) {
        super(owner, parent, name, callback, MATERIAL_RESOURCEKEY);
        this.materialDefinitionFactory = factory;
    }

    @Getter
    private final MaterialDefinitionFactory<DEF> materialDefinitionFactory;
    private @Nullable Element element;

    public MaterialBuilder<DEF, P> setElement(Element ele) {
        components.clear();
        element = ele;
        return this;
    }

    private final Map<Element, Integer> components = new HashMap<>();

    public MaterialBuilder<DEF, P> setComponents(Object... elements) {
        if (elements.length % 2 != 0) throw new IllegalArgumentException("Need pairs of Element and count");
        components.clear();
        element = null;
        var index = 0;
        while (index < elements.length) {
            var a1 = elements[index];
            var a2 = elements[index + 1];
            if ((a1 instanceof Element ele) && (a2 instanceof Integer cou)) {
                components.put(ele, cou);
            } else {
                throw new IllegalArgumentException("Need pairs of Element and count");
            }
            index += 2;
        }
        return this;
    }

    private final Map<PropertyKey, ItemLike> ignores = new HashMap<>();

    public MaterialBuilder<DEF, P> addIgnore(PropertyKey property, ItemLike item) {
        ignores.put(property, item);
        return this;
    }

    public MaterialBuilder<DEF, P> defaultProperty() {
        return this;
    }

    public ResourceLocation getLocation() {
        return ResourceLocation.fromNamespaceAndPath(getOwner().getModid(), getName());
    }

    private DEF instance;

    @Override
    protected @NonnullType @NotNull DEF createEntry() {
        return instance;
    }

    @Override
    public @NotNull MaterialEntry<DEF> register() {
        var materialInfo = new MaterialDefinition.MaterialInfo(getLocation());
        var materialProperties = new MaterialProperties();
        instance = materialDefinitionFactory.apply(materialInfo, materialProperties);

        if (element != null) {
            materialInfo.setElement(element);
        } else {
            materialInfo.setComponentList(components);
        }

        return (MaterialEntry<DEF>) super.register();
    }

    private final Map<PropertyKey, RegistryEntry> propertyStorage = new HashMap<>();

    @Override
    protected @NotNull MaterialEntry<DEF> createEntryWrapper(@NotNull DeferredHolder<MaterialDefinition, DEF> delegate) {
        var me = new MaterialEntry<>(this.getOwner(), delegate);
        me.getPropertyStorage().putAll(propertyStorage);
        return me;
    }

    public MaterialBuilder<DEF, P> fluid(String format) {
        var fluidName = format.formatted(getName());
        return getOwner().fluid(this, fluidName).build();
    }

    public MaterialBuilder<DEF, P> liquid() {
        return fluid("liquid_%s");
    }

    public MaterialBuilder<DEF, P> item(String format) {
        return getOwner().item(this, format.formatted(getName()), prop -> new Item(prop)).build();
    }

    public MaterialBuilder<DEF, P> ingot() {
        return this.item("%s_ingot");
    }

    public MaterialBuilder<DEF, P> dust() {
        return this.item("%s_dust");
    }

    public MaterialBuilder<DEF, P> plate() {
        return this.item("%s_plate");
    }

    public MaterialBuilder<DEF, P> block(String format) {
        return getOwner().block(this, format.formatted(getName()), prop -> new Block(prop)).simpleItem().build();
    }
}
