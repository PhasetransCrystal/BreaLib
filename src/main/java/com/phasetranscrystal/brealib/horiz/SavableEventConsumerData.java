package com.phasetranscrystal.brealib.horiz;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import lombok.Getter;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.Event;

import java.util.List;
import java.util.function.Consumer;

/**
 * 可序列化的事件消费者数据抽象基类。
 * <p>
 * 该类提供了事件监听器的序列化支持，允许将事件监听器数据持久化保存并在游戏重启后恢复。
 * 继承该类可以创建自定义的可序列化事件监听器。
 * </p>
 *
 * @param <T> 监听的事件类型，必须继承自 {@link Event}
 */
public abstract class SavableEventConsumerData<T extends Event> {
    public static final Codec<SavableEventConsumerData<?>> CODEC =
            BreaHoriz.SAVABLE_EVENT_CONSUMER_TYPE.byNameCodec().dispatch(SavableEventConsumerData::getCodec, i -> i);

    //都不用持久化
    @Getter
    private List<ResourceLocation> path;
    private Consumer<T> consumerCache;

    /**
     * 构造一个新的可序列化事件消费者数据实例。
     */
    public SavableEventConsumerData() {
    }

    /**
     * 获取此监听器监听的事件类型。
     *
     * @return 事件类的 {@link Class} 对象
     */
    public abstract Class<T> getEventClass();

    /**
     * 获取此监听器对应的编解码器。
     * <p>
     * 每个子类必须实现此方法以提供自身的序列化支持。
     * </p>
     *
     * @return 该子类对应的 {@link MapCodec} 实例
     */
    public abstract MapCodec<? extends SavableEventConsumerData<?>> getCodec();

    /**
     * 处理接收到的事件。
     * <p>
     * 子类必须实现此方法来定义具体的事件处理逻辑。
     * </p>
     *
     * @param event 接收到的事件对象
     */
    protected abstract void consumeEvent(T event);

    /**
     * 判断此监听器是否应该处理已被取消的事件。
     * 请保证同个实例每次返回的结果都一样。
     * <p>
     * 如果返回 {@code true}，即使事件已被标记为取消，监听器仍会收到该事件。
     * 如果返回 {@code false}，则只会在事件未被取消时收到通知。
     * </p>
     *
     * @return 如果应该处理已取消的事件返回 {@code true}，否则返回 {@code false}
     */
    public abstract boolean handleCancelled();

    /**
     * 获取此监听器的消费者实例。
     * <p>
     * 首次调用时会创建消费者缓存，后续调用直接返回缓存实例。
     * </p>
     *
     * @return 包装了 {@link #consumeEvent(Event)} 方法的 {@link Consumer} 实例
     */
    public Consumer<T> getConsumer() {
        if (this.consumerCache == null) this.consumerCache = this::consumeEvent;
        return this.consumerCache;
    }

    /**
     * 获取消费者缓存实例。
     * <p>
     * 主要用于内部使用，获取已缓存的消费者实例。
     * </p>
     *
     * @return 缓存的消费者实例，如果尚未创建则返回 {@code null}
     */
    Consumer<T> getConsumerCache() {
        return this.consumerCache;
    }

    /**
     * 设置监听器的路径信息。
     * <p>
     * <b>注意：</b>此方法由 {@link EventDistributor} 在添加监听器时自动调用，
     * 不应手动调用此方法。
     * </p>
     *
     * @param path 监听器的完整路径信息
     * @throws IllegalStateException 如果路径已被设置过
     */
    void setPath(List<ResourceLocation> path) {
        if (this.path != null) {
            throw new IllegalStateException("Cannot change path once it has been set");
        }
        this.path = path;
    }
}
