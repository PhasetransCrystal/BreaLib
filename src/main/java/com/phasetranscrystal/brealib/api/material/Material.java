package com.phasetranscrystal.brealib.api.material;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

import com.phasetranscrystal.brealib.BreaAPI;
import com.phasetranscrystal.brealib.api.material.property.IMaterialProperty;
import com.phasetranscrystal.brealib.api.material.property.MaterialProperties;
import com.phasetranscrystal.brealib.api.material.property.PropertyKey;
import com.phasetranscrystal.brealib.api.tag.BreaTagPrefix;
import com.phasetranscrystal.brealib.utils.FormattingUtil;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class Material implements Comparable<Material> {

    /**
     * 材料的基本信息
     * 
     * @see MaterialInfo
     */
    @NotNull
    @Getter
    private final MaterialInfo materialInfo;

    /**
     * 材料的不同属性的实例类型
     * 
     * @see MaterialProperties
     */
    @NotNull
    @Getter
    private final MaterialProperties properties;

    /**
     * 从现有配置复制实例
     */
    private Material(@NotNull MaterialInfo materialInfo, @NotNull MaterialProperties properties) {
        this.materialInfo = materialInfo;
        this.properties = properties;
        this.properties.setMaterial(this);
        verifyMaterial();
    }

    /**
     * 创建新材料
     * 
     * @param resourceLocation
     */
    protected Material(ResourceLocation resourceLocation) {
        materialInfo = new MaterialInfo(resourceLocation);
        properties = new MaterialProperties();
    }

    /**
     * 将材料添加到注册队列
     */
    protected void registerMaterial() {
        BreaAPI.materialManager.register(this);
    }

    public String getName() {
        return materialInfo.resourceLocation.getPath();
    }

    public String getModid() {
        return materialInfo.resourceLocation.getNamespace();
    }

    /**
     * 材料是否为单一元素材料
     * 
     * @return
     */
    public boolean isElement() {
        return materialInfo.element != null;
    }

    @Nullable
    public Element getElement() {
        return materialInfo.element;
    }

    public void setMaterialARGB(int materialRGB) {
        materialInfo.colors.set(0, materialRGB);
    }

    public void setMaterialSecondaryARGB(int materialRGB) {
        materialInfo.colors.set(1, materialRGB);
    }

    public int getLayerARGB(int layerIndex) {
        // get 2nd digit as positive if emissive layer
        if (layerIndex < -100) {
            layerIndex = (Math.abs(layerIndex) % 100) / 10;
        }
        if (layerIndex > materialInfo.colors.size() - 1 || layerIndex < 0) return -1;
        int layerColor = getMaterialARGB(layerIndex);
        if (layerColor != -1 || layerIndex == 0) return layerColor;
        else return getMaterialARGB(0);
    }

    public int getMaterialARGB() {
        return materialInfo.colors.getInt(0) | 0xff000000;
    }

    public int getMaterialSecondaryARGB() {
        return materialInfo.colors.getInt(1) | 0xff000000;
    }

    /**
     * Gets a specific color layer in ARGB.
     *
     * @param index the index of the layer [0,10). will crash if you pass values > 10.
     * @return Gets a specific color layer.
     */
    public int getMaterialARGB(int index) {
        return materialInfo.colors.getInt(index) | 0xff000000;
    }

    public int getMaterialRGB() {
        return materialInfo.colors.getInt(0);
    }

    /**
     * Gets a specific color layer.
     *
     * @param index the index of the layer [0,10). will crash if you pass values > 10.
     * @return Gets a specific color layer.
     */
    public int getMaterialRGB(int index) {
        return materialInfo.colors.getInt(index);
    }

    public int getMaterialSecondaryRGB() {
        return materialInfo.colors.getInt(1);
    }

    public boolean hasFluidColor() {
        return materialInfo.hasFluidColor;
    }

    public String toCamelCaseString() {
        return FormattingUtil.lowerUnderscoreToUpperCamel(getName());
    }

    @NotNull
    public ResourceLocation getResourceLocation() {
        return materialInfo.resourceLocation;
    }

    public String getUnlocalizedName() {
        return materialInfo.resourceLocation.toLanguageKey("material");
    }

    public MutableComponent getLocalizedName() {
        return Component.translatable(getUnlocalizedName());
    }

    @Override
    public int compareTo(Material material) {
        return toString().compareTo(material.toString());
    }

    @Override
    public String toString() {
        return materialInfo.resourceLocation.toString();
    }

    public <T extends IMaterialProperty> boolean hasProperty(PropertyKey<T> key) {
        return getProperty(key) != null;
    }

    public <T extends IMaterialProperty> T getProperty(PropertyKey<T> key) {
        return properties.getProperty(key);
    }

    public <T extends IMaterialProperty> void setProperty(PropertyKey<T> key, IMaterialProperty property) {
        if (!BreaAPI.materialManager.canModifyMaterials()) {
            throw new IllegalStateException("Cannot add properties to a Material when registry is frozen!");
        }
        properties.setProperty(key, property);
        properties.verify();
    }

    /**
     * 验证材料定义
     */
    public void verifyMaterial() {
        properties.verify();
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Material material))
            return false;

        return Objects.equals(this.getResourceLocation(), material.getResourceLocation());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.getResourceLocation());
    }

    @SuppressWarnings("UnusedReturnValue")
    public static class Builder {

        private final MaterialInfo materialInfo;
        private final MaterialProperties properties;
        private Set<BreaTagPrefix> ignoredTagPrefixes = null;

        private String formula = null;

        /*
         * The temporary list of components for this Material.
         */
        // private List<MaterialStack> composition = new ArrayList<>();
        // private List<MaterialStackWrapper> compositionSupplier;

        /*
         * Temporary value to use to determine how to calculate default RGB
         */
        private boolean averageRGB = false;

        /**
         * Constructs a {@link Material}. This Builder replaces the old constructors, and
         * no longer uses a class hierarchy, instead using a {@link MaterialProperties} system.
         *
         * @param resourceLocation The Name of this Material. Will be formatted as
         *                         "material.<name>" for the Translation Key.
         * @since GTCEu 2.0.0
         */
        public Builder(ResourceLocation resourceLocation) {
            String name = resourceLocation.getPath();
            if (name.charAt(name.length() - 1) == '_')
                throw new IllegalArgumentException("Material name cannot end with a '_'!");
            materialInfo = new MaterialInfo(resourceLocation);
            properties = new MaterialProperties();
        }
    }

    /**
     * 保存材料的基本信息,例如名称、颜色、ID 等。
     */
    @SuppressWarnings("UnusedReturnValue")
    @Accessors(chain = true)
    private static class MaterialInfo {

        /**
         * The modid and unlocalized name of this Material.
         * <p>
         * Required.
         */
        private final ResourceLocation resourceLocation;

        /**
         * The colors of this Material.
         * if any past index 0 are -1, they aren't used.
         * <p>
         * Default: 0xFFFFFF if no Components, otherwise it will be the average of Components.
         */
        @Getter
        @Setter
        private IntList colors = new IntArrayList(List.of(-1, -1));

        /**
         * The color of this Material.
         * <p>
         * Default: 0xFFFFFF if no Components, otherwise it will be the average of Components.
         */
        @Getter
        @Setter
        private boolean hasFluidColor = true;

        /**
         * The IconSet of this Material.
         * <p>
         * Default: - GEM_VERTICAL if it has GemProperty.
         * - DULL if has DustProperty or IngotProperty.
         */
        // @Getter
        // @Setter
        // private MaterialIconSet iconSet;

        /**
         * The components of this Material.
         * <p>
         * Default: none.
         */
        // @Getter
        // @Setter
        // private ImmutableList<MaterialStack> componentList;

        /**
         * The Element of this Material, if it is a direct Element.
         * <p>
         * Default: none.
         */
        @Getter
        @Setter
        private Element element;

        private MaterialInfo(ResourceLocation resourceLocation) {
            this.resourceLocation = resourceLocation;
        }
    }
}
