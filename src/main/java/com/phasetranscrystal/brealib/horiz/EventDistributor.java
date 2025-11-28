package com.phasetranscrystal.brealib.horiz;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.mojang.serialization.MapCodec;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Minecraft实体事件分发系统，基于Tree结构实现的事件监听器管理。
 * 该系统通过DataAttachment附加在实体上，在相关实体事件发生时进行调度执行。
 * 支持普通监听器和可序列化监听器两种类型，并提供基于路径标记的事件管理。
 *
 * <p>主要特性：
 * <ul>
 *   <li>支持基于路径的事件监听器组织和管理</li>
 *   <li>提供可序列化的事件消费者数据持久化</li>
 *   <li>防止事件循环调用的哈希缓存机制</li>
 * </ul>
 */
public class EventDistributor {
    public static final MapCodec<EventDistributor> CODEC =
            Tree.createCodec(ResourceLocation.CODEC, SavableEventConsumerData.CODEC)
                    .xmap(EventDistributor::new, ins -> ins.savableListeners)
                    .fieldOf("savable");

    /**
     * 事件监听器多值映射，存储事件类与对应消费者的关系。
     * <p><b>警告：</b>不要直接修改此映射，请使用提供的add和remove方法。
     */
    public final Multimap<Class<? extends Event>, Consumer<? extends Event>> listeners = HashMultimap.create();

    /**
     * 带标记的事件监听器树，用于按路径组织和管理监听器。
     */
    public final Tree<ResourceLocation, IdentEvent<?>> markedListeners = new Tree<>();

    /**
     * 可保存的事件消费者数据树，支持序列化和持久化。
     */
    public final Tree<ResourceLocation, SavableEventConsumerData<?>> savableListeners;

    /**
     * 事件哈希缓存，用于防止同一事件被重复处理。
     * 键为事件类，值为事件对象的哈希码。
     */
    public final HashMap<Class<? extends Event>, Integer> eventHashCache = new HashMap<>();

    /**
     * 记录那些即使事件在调度周期内被取消也要执行的消费器。
     */
    public final HashSet<Consumer<? extends Event>> handleCancelledEvent = new HashSet<>();

    /**
     * 构造一个空的事件分发器实例。
     */
    public EventDistributor() {
        this.savableListeners = new Tree<>();
    }

    /**
     * 从已有的可保存监听器树构造事件分发器。
     * 如果传入的树不为空，会自动加载所有监听器到当前分发器。
     *
     * @param tree 包含可保存监听器数据的树结构
     */
    public EventDistributor(Tree<ResourceLocation, SavableEventConsumerData<?>> tree) {
        this.savableListeners = tree;
        if (!this.savableListeners.isEmpty()) {
            this.savableListeners.forEach((path, data) -> {
                data.setPath(path);
                listeners.put(data.getEventClass(), data.getConsumer());
            });
        }
    }

    /**
     * 添加带标记的事件监听器。
     *
     * @param <T>      事件类型，必须继承自Event
     * @param event    要监听的事件类
     * @param listener 事件消费者（监听器）
     * @param path     监听器的路径标记，用于组织和管理
     * @return 如果监听器成功添加返回true，如果已存在返回false
     * @throws IllegalArgumentException 如果参数无效
     */
    public <T extends Event> boolean add(Class<T> event, Consumer<T> listener, ResourceLocation... path) {
        return add(event, listener, false, path);
    }

    public <T extends Event> boolean add(Class<T> event, Consumer<T> listener, boolean handleCancelled, ResourceLocation... path) {
        boolean result = listeners.put(event, listener);
        if (result) {
            markedListeners.add(new IdentEvent<>(event, listener, handleCancelled), path);
            if (handleCancelled) {
                handleCancelledEvent.add(listener);
            }
        }
        return result;
    }

    public <T extends Event> boolean add(IdentEvent<?> identEvent, ResourceLocation... path) {
        boolean result = listeners.put(identEvent.event(), identEvent.listener());
        if (result) {
            markedListeners.add(identEvent, path);
            if (identEvent.handleCancelled()) {
                handleCancelledEvent.add(identEvent.listener());
            }
        }
        return result;
    }

    /**
     * 添加可保存的事件监听器数据。
     *
     * @param <T>   事件类型，必须继承自Event
     * @param data  可保存的事件消费者数据
     * @param path  数据的存储路径
     * @return 如果数据成功添加返回true
     * @throws IllegalArgumentException 如果SavableEventConsumerData的路径已存在，即已被注册。
     */
    public <T extends Event> boolean add(SavableEventConsumerData<T> data, ResourceLocation... path) {
        if (data.getPath() != null)
            throw new IllegalArgumentException("Savable event consumer's path is exist. Don't registry one instance multi.");
        boolean result = listeners.put(data.getEventClass(), data.getConsumer());
        if (result) {
            data.setPath(List.of(path));
            savableListeners.add(data, path);
            if (data.handleCancelled()) {
                handleCancelledEvent.add(data.getConsumer());
            }
        }
        return result;
    }

    /**
     * 移除指定的事件监听器。
     * 会同时从普通监听器集合和标记监听器树中移除。
     * 不要用这个方法移除可保存事件监听器的注册。请使用{@link EventDistributor#removeSavable(Class, SavableEventConsumerData)}。
     *
     * @param <T>      事件类型
     * @param event    要移除监听器的事件类
     * @param listener 要移除的监听器
     * @return 如果至少从一个集合中移除了监听器返回true
     */
    public <T extends Event> boolean remove(Class<T> event, Consumer<T> listener) {
        boolean removedFromListeners = listeners.remove(event, listener);
        boolean removedFromMarked = markedListeners.removeAny(new IdentEvent<>(event, listener));
        handleCancelledEvent.remove(listener);
        return removedFromListeners || removedFromMarked;
    }

    /**
     * 通过标识事件对象移除监听器。
     *
     * @param <T>   事件类型
     * @param ident 标识事件对象，包含事件类和监听器
     * @return 如果监听器被成功移除返回true
     */
    public <T extends Event> boolean remove(IdentEvent<T> ident) {
        boolean removedFromListeners = listeners.remove(ident.event(), ident.listener());
        boolean removedFromMarked = markedListeners.removeAny(ident);
        handleCancelledEvent.remove(ident.listener());
        return removedFromListeners || removedFromMarked;
    }

    /**
     * 移除可保存的事件监听器数据。
     *
     * @param <T>   事件类型
     * @param event 事件类
     * @param data  可保存的事件消费者数据
     * @return 如果数据被成功移除返回true
     */
    public <T extends Event> boolean removeSavable(Class<T> event, SavableEventConsumerData<T> data) {
        if (data.getPath() == null || data.getConsumerCache() == null) return false;
        boolean removedFromListeners = listeners.remove(event, data.getConsumer());
        boolean removedFromMarked = savableListeners.removeAny(data);
        handleCancelledEvent.remove(data.getConsumer());
        return removedFromListeners || removedFromMarked;
    }

    /**
     * 移除指定事件类的所有监听器。
     * 会同时清除普通监听器、标记监听器和可保存监听器。
     *
     * @param <T>   事件类型
     * @param event 要清除监听器的事件类
     * @return 如果移除了任何监听器返回true
     */
    public <T extends Event> boolean removeByClass(Class<T> event) {
        if (listeners.containsKey(event)) {
            handleCancelledEvent.removeAll(listeners.removeAll(event));
            markedListeners.removeAll(ident -> ident.event().equals(event));
            savableListeners.removeAll(data -> data.getEventClass().equals(event));
            return true;
        }
        return false;
    }

    /**
     * 移除所有监听器。
     * 相当于调用{@code removeInPath()}清空根路径下的所有监听器。
     *
     * @return 如果移除了任何监听器返回true
     */
    public boolean removeAll() {
        return this.removeInPath();
    }

    /**
     * 移除指定路径及其子路径下的所有监听器。
     *
     * @param path 要清除的路径
     * @return 如果移除了任何监听器返回true
     */
    public boolean removeInPath(ResourceLocation... path) {
        boolean flag = false;

        Collection<IdentEvent<?>> co = markedListeners.removeInPath(path);
        if (!co.isEmpty()) {
            co.forEach((ident) -> {
                listeners.remove(ident.event(), ident.listener());
                handleCancelledEvent.remove(ident.listener());
            });
            flag = true;
        }

        Collection<SavableEventConsumerData<?>> co2 = savableListeners.removeInPath(path);
        if (!co2.isEmpty()) {
            co2.forEach((data) -> {
                listeners.remove(data.getEventClass(), data.getConsumer());
                handleCancelledEvent.remove(data.getConsumer());
            });
            flag = true;
        }

        return flag;
    }

    /**
     * 移除指定路径下的监听器（不包含子路径）。
     *
     * @param path 要清除的精确路径
     * @return 如果移除了任何监听器返回true
     */
    public boolean removeAtPath(ResourceLocation... path) {
        boolean flag = false;

        Collection<IdentEvent<?>> co = markedListeners.removeAtPath(path);
        if (!co.isEmpty()) {
            co.forEach((ident) -> {
                listeners.remove(ident.event(), ident.listener());
                handleCancelledEvent.remove(ident.listener());
            });
            flag = true;
        }

        Collection<SavableEventConsumerData<?>> co2 = savableListeners.removeAtPath(path);
        if (!co2.isEmpty()) {
            co2.forEach((data) -> {
                listeners.remove(data.getEventClass(), data.getConsumer());
                handleCancelledEvent.remove(data.getConsumer());
            });
            flag = true;
        }

        return flag;
    }

    /**
     * 分发和处理事件。
     * 该方法会检查事件哈希缓存，防止同一事件被重复处理。
     * 使用线程安全的副本机制避免在事件处理过程中修改监听器集合导致的异常。
     *
     * @param <T>   事件类型
     * @param event 要处理的事件对象
     */
    public <T extends Event> void post(T event) {
        if (!listeners.containsKey(event.getClass()) ||
                (eventHashCache.containsKey(event.getClass()) && eventHashCache.get(event.getClass()).equals(event.hashCode()))
        ) return;
        // 创建监听器副本防止在消费过程中修改集合导致异常
        for (Consumer<? extends Event> consumer : List.copyOf(listeners.get(event.getClass()))) {
            if (!(event instanceof ICancellableEvent cancellable) || !cancellable.isCanceled() || handleCancelledEvent.contains(consumer)) {
                ((Consumer<T>) consumer).accept(event);
            }
        }
        eventHashCache.put(event.getClass(), event.hashCode());
    }
}