# BreaHoriz - 实体事件分发系统

## 概述

BreaHoriz 是 BreaLib 库的一个模块，提供了一个基于树形结构的高级实体事件分发系统。
该系统允许将事件监听器附加到 Minecraft 实体上，并通过路径标记来组织和管理监听器，支持普通监听器和可序列化监听器两种类型。

请注意：该组件在实体死亡时不会自动复制，可能需要手动处理状态设置。

## 核心特性

- 自动将实体相关事件转发到对应实体的监听器
- 默认支持neoforge提供的绝大多数多种实体事件类型（攻击、伤害、死亡、交互等），可自行兼容其它事件
- 基于路径的事件监听器组织，支持层级化的事件监听器管理，提供批量操作和精确路径操作
- 可序列化特殊事件监听器以持久化存储，支持自定义可序列化事件消费者
- 该功能高度模块化，可便利迁移至维度等对象

## 快速开始

### 基本用法

```java
void method() {
    // 获取实体的EventDistributor
    EventDistributor distributor = entity.getData(BreaHoriz.EVENT_DISTRIBUTOR);

    // 添加事件监听器
    distributor.add(PlayerInteractEvent.RightClickBlock.class, event -> {
        // 处理右键方块事件
        System.out.println("玩家右键了方块: " + event.getPos());
    }, new ResourceLocation("mymod", "interactions"));

    // 添加可序列化监听器（需要实现SavableEventConsumerData）
    distributor.add(PlayerEvent.PlayerLoggedInEvent.class,
            new MyLoginListener(),
            new ResourceLocation("mymod", "login_handlers"));
}
```

### 自定义可序列化监听器

通过创建`SavableEventConsumerData`的子类以自定义可持久化的事件监听器。

如下展示了一个例子，用于在玩家进入世界时发送问候消息。

```java
public static class LoginListener extends SavableEventConsumerData<PlayerEvent.PlayerLoggedInEvent> {
    public static final MapCodec<LoginListener> CODEC =
            Codec.STRING.xmap(LoginListener::new, LoginListener::getContent).fieldOf("text");
    @Getter
    public final String content;

    public LoginListener(String content) {
        this.content = content;
    }

    @Override
    public Class<PlayerEvent.PlayerLoggedInEvent> getEventClass() {
        return PlayerEvent.PlayerLoggedInEvent.class;
    }

    @Override
    protected void consumeEvent(PlayerEvent.PlayerLoggedInEvent event) {
        // 处理玩家登录事件
        event.getEntity().displayClientMessage(Component.literal(content), false);
    }

    //配置是否允许处理取消的事件
    @Override
    public boolean handleCancelled() {
        return false;
    }

    @Override
    public MapCodec<LoginListener> getCodec() {
        return LOGIN_SHOW_TEXT.get();
    }
}

public static final DeferredRegister<MapCodec<? extends SavableEventConsumerData<?>>> REGISTER =
        DeferredRegister.create(SAVABLE_EVENT_CONSUMER_TYPE, BreaLib.MOD_ID);

public static final DeferredHolder<MapCodec<? extends SavableEventConsumerData<?>>, MapCodec<LoginListener>> LOGIN_SHOW_TEXT =
        REGISTER.register("horiz/login_show_text", () -> LoginListener.CODEC);

@SubscribeEvent
public static void bingingToPlayer(EntityDistributorInit.GatherEntityDistributeEvent event) {
    if (event.getEntity() instanceof Player) {
        event.getEntity().getData(EVENT_DISTRIBUTOR).add(new LoginListener("HELLO PLAYER!"), BreaLib.byPath("testing"));
    }
}
```

## 事件类型支持

系统支持多种实体事件。

可以在[`BreaHoriz.EntityDistributorInit.bootstrapConsumer`](BreaHoriz.java)下查看内置支持的事件

## 路径管理

相关内容可以查询[`Tree`](Tree.java)与[`EventDistributor`](EventDistributor.java)内的javadoc。
在此只展示一些基本用法。

### 路径操作示例

```java
import net.minecraft.resources.ResourceLocation;

void inSomewhere() {
    ResourceLocation COMBAT = ResourceLocation.fromNamespaceAndPath("mymod", "combat");
    ResourceLocation SWORD = ResourceLocation.fromNamespaceAndPath("mymod", "sword");
    // 添加带路径的监听器
    distributor.add(eventClass, listener, COMBAT, SWORD);
    
    // 移除特定路径下的所有监听器
    distributor.removeInPath(COMBAT);

    // 移除精确路径位置的监听器
    distributor.removeAtPath(COMBAT, SWORD);
}
```

### 事件取消处理

```java
void inSomewhere(){
    // 设置监听器处理已取消的事件
    distributor.add(eventClass, listener, true,path);
}
```

### 批量操作

```java
void inSomeplace() {
    // 移除所有监听器
    distributor.removeAll();

    // 按事件类移除
    distributor.removeByClass(PlayerInteractEvent.class);
}
```

### 树形结构遍历

```java
void inSomeplace() {
// 遍历所有监听器及其路径
    distributor.markedListeners.forEach((path, identEvent) -> {
        System.out.println("路径: " + path + ", 事件: " + identEvent.event());
    });
}
```

## 额外事件注册

如果需要将其它模组或自定义的与实体有关的事件注册，
可以参考[`BreaHoriz.EntityDistributorInit.bootstrapConsumer`](BreaHoriz.java)直接监听对应事件进行注册。