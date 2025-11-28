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

```java
public class MyLoginListener(String content) extends SavableEventConsumerData<PlayerEvent.PlayerLoggedInEvent> {
    
    @Override
    public Class<PlayerEvent.PlayerLoggedInEvent> getEventClass() {
        return PlayerEvent.PlayerLoggedInEvent.class;
    }

    @Override
    protected void consumeEvent(PlayerEvent.PlayerLoggedInEvent event) {
        // 处理玩家登录事件
        event.getEntity().sendSystemMessage(Component.literal(content));
    }

    //配置是否允许处理取消的事件
    @Override
    public boolean handleCancelled() {
        return false;
    }

    @Override
    public MapCodec<SavableEventConsumerData<?>> getCodec() {
        // 返回对应的编解码器
        return ModRegisty.MY_LOGIN_LISTENER;
    }
}
```

## 事件类型支持

系统支持多种实体事件，包括但不限于：

### 战斗事件

- `EntityAttackEvent.Income/Pre/Post` - 实体攻击事件
- `LivingDamageEvent.Pre/Post` - 伤害计算事件
- `LivingDeathEvent` - 实体死亡事件
- `EntityKillEvent.Pre/Post` - 实体击杀事件

### 交互事件

- `PlayerInteractEvent` 系列 - 玩家交互事件
- `ItemEntityPickupEvent.Pre/Post` - 物品拾取事件
- `AttackEntityEvent` - 攻击实体事件

### 状态事件

- `PlayerEvent` 系列 - 玩家相关事件
- `LivingEntityUseItemEvent` - 物品使用事件
- `MobEffectEvent` 系列 - 状态效果事件

## 路径管理

### 路径操作示例

```java
// 添加带路径的监听器
distributor.add(eventClass, listener, 
    new ResourceLocation("mymod", "combat"), 
    new

ResourceLocation("sword_effects"));

// 移除特定路径下的所有监听器
        distributor.

removeInPath(new ResourceLocation("mymod", "combat"));

// 移除精确路径的监听器
        distributor.

removeAtPath(new ResourceLocation("mymod", "combat","sword_effects"));
```

## 高级特性

### 1. 事件取消处理

```java
// 设置监听器处理已取消的事件
distributor.add(eventClass, listener, true,path);
```

### 2. 批量操作

```java
// 移除所有监听器
distributor.removeAll();

// 按事件类移除
distributor.

removeByClass(PlayerInteractEvent .class);
```

### 3. 树形结构遍历

```java
// 遍历所有监听器及其路径
distributor.markedListeners.forEach((path, identEvent) ->{
        System.out.

println("路径: "+path +", 事件: "+identEvent.event());
        });
```

## 注册和初始化

### 自动注册

系统会自动注册所需的事件监听器，包括：

- 实体加入世界时的分发器初始化
- 战斗事件的自定义转发逻辑
- 各种实体事件的监听器设置

### 手动初始化

```java
// 在主模组初始化中调用
BreaHoriz.bootstrap(modEventBus);
```

## 注意事项

1. **事件哈希缓存**: 系统使用哈希缓存防止同一事件被重复处理
2. **线程安全**: 使用监听器副本避免并发修改异常
3. **序列化限制**: 可序列化监听器需要在注册表中注册对应的编解码器
4. **性能考虑**: 大量监听器可能影响性能，建议合理组织路径结构

## 扩展开发

开发者可以通过以下方式扩展系统功能：

1. **实现自定义事件**: 创建继承自 `EntityEvent` 的自定义事件
2. **扩展序列化支持**: 实现新的 `SavableEventConsumerData` 子类
3. **集成其他系统**: 将现有事件系统与 BreaHoriz 集成

## 故障排除

### 常见问题

1. **监听器不触发**: 检查事件类是否正确，路径是否有效
2. **序列化错误**: 确保可序列化监听器的编解码器已正确注册
3. **性能问题**: 优化监听器逻辑，避免在事件处理中执行耗时操作

### 调试建议

启用调试日志查看事件分发详情：

```java
BreaHoriz.LOGGER.debug("事件分发详情: {}",eventDetails);
```
