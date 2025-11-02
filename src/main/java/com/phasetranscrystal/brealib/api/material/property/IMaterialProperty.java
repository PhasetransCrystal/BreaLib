package com.phasetranscrystal.brealib.api.material.property;

/// 材料的不同类型属性需继承该接口
@FunctionalInterface
public interface IMaterialProperty {

    void verifyProperty(MaterialProperties properties);
}
