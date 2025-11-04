package com.phasetranscrystal.brealib.api.material;

import net.minecraft.resources.ResourceLocation;

import com.phasetranscrystal.brealib.api.material.property.MaterialProperties;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class MaterialDefinition implements Comparable<MaterialDefinition> {

    @Getter
    private final MaterialInfo materialInfo;
    @Getter
    private final MaterialProperties materialProperties;

    public MaterialDefinition(MaterialInfo info, MaterialProperties properties) {
        this.materialInfo = info;
        this.materialProperties = properties;
        this.materialProperties.setMaterial(this);
    }

    @Override
    public int compareTo(@NotNull MaterialDefinition material) {
        return this.materialInfo.location.compareTo(material.materialInfo.location);
    }

    @Accessors(chain = true, fluent = true)
    public static class MaterialInfo {

        @Getter
        private final ResourceLocation location;
        @Getter
        private @Nullable Element element;
        private final Map<Element, Integer> componentList;

        public MaterialInfo(ResourceLocation location) {
            this.location = location;
            componentList = new HashMap<>();
        }

        public void setElement(@NotNull Element element) {
            this.element = element;
            this.componentList.clear();
            this.componentList.put(element, 1);
        }

        public boolean isElement() {
            return element != null;
        }

        public void setComponentList(@NotNull Map<Element, Integer> componentList) {
            this.componentList.clear();
            this.componentList.putAll(componentList);
        }
    }
}
