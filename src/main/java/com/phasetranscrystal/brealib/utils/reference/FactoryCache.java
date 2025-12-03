package com.phasetranscrystal.brealib.utils.reference;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 默认无参构造函数工厂缓存，减少重复反射。
 * 若类型没有无参构造，可在外部手动提供工厂再调用 {@link ReferencePool#acquire(Class)}。
 */
final class FactoryCache {

    private static final ConcurrentHashMap<Class<?>, ReferenceFactory<?>> CACHE = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    static <T extends IReference> ReferenceFactory<T> get(Class<T> clazz) {
        return (ReferenceFactory<T>) CACHE.computeIfAbsent(clazz, k -> () -> {
            try {
                return clazz.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                throw new RuntimeException("Cannot create instance of " + clazz.getName() +
                        ", please provide a custom ReferenceFactory", e);
            }
        });
    }
}
