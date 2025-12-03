package com.phasetranscrystal.brealib.utils.reference;

/** 引用工厂，用于创建新对象 */
@FunctionalInterface
public interface ReferenceFactory<T extends IReference> {

    T newInstance();
}
