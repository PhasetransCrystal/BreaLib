package com.phasetranscrystal.brealib.horiz;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.phasetranscrystal.brealib.BreaLib;
import lombok.Getter;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.*;
import net.neoforged.neoforge.event.entity.item.ItemExpireEvent;
import net.neoforged.neoforge.event.entity.item.ItemTossEvent;
import net.neoforged.neoforge.event.entity.living.*;
import net.neoforged.neoforge.event.entity.player.*;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.registries.*;
import org.apache.logging.log4j.Logger;

import java.util.function.Consumer;

@EventBusSubscriber(modid = BreaLib.MOD_ID)
public class BreaHoriz {
    public static final String MODULE_ID = "horiz";
    public static final Logger LOGGER = BreaLib.loggerByModule("Horiz");

    public static void bootstrap(IEventBus bus) {
        REGISTER.register(bus);
        EntityDistributorInit.bootstrapConsumer();
//        Test.REGISTER.register(bus); //FOR TEST
    }

    public static final ResourceKey<Registry<MapCodec<? extends SavableEventConsumerData<?>>>> SAVABLE_EVENT_CONSUMER_TYPE_KEY =
            ResourceKey.createRegistryKey(BreaLib.byPath("horiz/savable_event_consumer"));

    public static final Registry<MapCodec<? extends SavableEventConsumerData<?>>> SAVABLE_EVENT_CONSUMER_TYPE =
            new RegistryBuilder<>(SAVABLE_EVENT_CONSUMER_TYPE_KEY).create();

    @SubscribeEvent
    public static void newRegistry(NewRegistryEvent event) {
        event.register(SAVABLE_EVENT_CONSUMER_TYPE);
    }

    public static final DeferredRegister<AttachmentType<?>> REGISTER = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, BreaLib.MOD_ID);

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<EventDistributor>> EVENT_DISTRIBUTOR =
            REGISTER.register("horiz/event_distributor", () -> AttachmentType.builder(holder -> new EventDistributor()).serialize(EventDistributor.CODEC).build());

    @EventBusSubscriber(modid = BreaLib.MOD_ID)
    public static class EntityDistributorInit {
        public static final Consumer<EntityEvent> consumer = event -> event.getEntity().getExistingData(EVENT_DISTRIBUTOR).ifPresent(d -> d.post(event));

        public static void bootstrapConsumer() {
//        addListener(EntityJoinLevelEvent.class); 信息在该事件低优先级被收集，该事件不会被触发。
            addListener(EntityTickEvent.Post.class);

            addListener(LivingIncomingDamageEvent.class);
            addListener(LivingDamageEvent.Pre.class);
            addListener(LivingDamageEvent.Post.class);
            addListener(EntityAttackEvent.Income.class);
            addListener(EntityAttackEvent.Pre.class);
            addListener(EntityAttackEvent.Post.class);
            addListener(LivingDeathEvent.class);
            addListener(EntityKillEvent.Pre.class);
            addListener(EntityKillEvent.Post.class);

            addListener(ItemTossEvent.class);
            addListener(ItemExpireEvent.class);
            addListener(AnimalTameEvent.class);
            addListener(ArmorHurtEvent.class);
            addListener(LivingChangeTargetEvent.class);
            addListener(LivingDestroyBlockEvent.class);
            addListener(LivingEntityUseItemEvent.Start.class);
            addListener(LivingEntityUseItemEvent.Stop.class);
            addListener(LivingEntityUseItemEvent.Finish.class);
            addListener(LivingEquipmentChangeEvent.class);
            addListener(LivingGetProjectileEvent.class);
            addListener(LivingHealEvent.class);
            addListener(MobEffectEvent.Applicable.class);
            addListener(MobEffectEvent.Added.class);
            addListener(MobEffectEvent.Expired.class);
            addListener(MobEffectEvent.Remove.class);
            addListener(AttackEntityEvent.class);
            addListener(CanPlayerSleepEvent.class);
            addListener(CanContinueSleepingEvent.class);
            addListener(CriticalHitEvent.class);
            NeoForge.EVENT_BUS.addListener(ItemEntityPickupEvent.Pre.class, e -> e.getPlayer().getData(EVENT_DISTRIBUTOR).post(e));
            NeoForge.EVENT_BUS.addListener(ItemEntityPickupEvent.Post.class, e -> e.getPlayer().getData(EVENT_DISTRIBUTOR).post(e));
            addListener(PlayerEvent.BreakSpeed.class);
            addListener(PlayerEvent.HarvestCheck.class);
            addListener(PlayerEvent.Clone.class);
            addListener(PlayerEvent.PlayerLoggedInEvent.class);
            addListener(PlayerEvent.PlayerLoggedOutEvent.class);
            addListener(PlayerInteractEvent.EntityInteractSpecific.class);
            addListener(PlayerInteractEvent.LeftClickBlock.class);
            addListener(PlayerInteractEvent.LeftClickEmpty.class);
            addListener(PlayerInteractEvent.RightClickBlock.class);
            addListener(PlayerInteractEvent.RightClickEmpty.class);
            addListener(PlayerInteractEvent.RightClickItem.class);
            addListener(PlayerWakeUpEvent.class);
            addListener(PlayerXpEvent.LevelChange.class);
            addListener(PlayerXpEvent.PickupXp.class);
            addListener(PlayerXpEvent.XpChange.class);
            NeoForge.EVENT_BUS.addListener(UseItemOnBlockEvent.class, e -> {
                if (e.getPlayer() != null)
                    e.getPlayer().getData(EVENT_DISTRIBUTOR).post(e);
            });
            addListener(EntityInvulnerabilityCheckEvent.class);
            addListener(EntityMountEvent.class);
            addListener(EntityStruckByLightningEvent.class);
            addListener(EntityTeleportEvent.class);
            addListener(ProjectileImpactEvent.class);
        }

        @SuppressWarnings("unchecked")
        public static <T extends EntityEvent> void addListener(Class<T> eventType) {
            NeoForge.EVENT_BUS.addListener(eventType, (Consumer<T>) consumer);
        }

        @SubscribeEvent(priority = EventPriority.LOWEST)
        public static void init(EntityJoinLevelEvent event) {
            if (!event.getLevel().isClientSide()) {
                NeoForge.EVENT_BUS.post(new GatherEntityDistributeEvent(event.getEntity()));
            }
        }

        @SubscribeEvent(priority = EventPriority.HIGHEST)
        public static void postAttackIncome(LivingIncomingDamageEvent event) {
            boolean cancelFlag = false;
            if (event.getSource().getEntity() != null)
                cancelFlag = NeoForge.EVENT_BUS.post(new EntityAttackEvent.Income(event.getSource().getEntity(), event, false)).isCanceled();
            if (!event.getSource().isDirect() && event.getSource().getDirectEntity() != null)
                cancelFlag = NeoForge.EVENT_BUS.post(new EntityAttackEvent.Income(event.getSource().getDirectEntity(), event, true)).isCanceled() || cancelFlag;

            event.setCanceled(cancelFlag);
        }

        @SubscribeEvent(priority = EventPriority.HIGHEST)
        public static void postAttackPre(LivingDamageEvent.Pre event) {
            if (event.getSource().getEntity() != null) {
                NeoForge.EVENT_BUS.post(new EntityAttackEvent.Pre(event.getSource().getEntity(), event, false));
            }
            if (!event.getSource().isDirect() && event.getSource().getDirectEntity() != null) {
                NeoForge.EVENT_BUS.post(new EntityAttackEvent.Pre(event.getSource().getDirectEntity(), event, true));
            }
        }

        @SubscribeEvent(priority = EventPriority.HIGHEST)
        public static void postAttackPost(LivingDamageEvent.Post event) {
            if (event.getSource().getEntity() != null) {
                NeoForge.EVENT_BUS.post(new EntityAttackEvent.Post(event.getSource().getEntity(), event, false));
            }
            if (!event.getSource().isDirect() && event.getSource().getDirectEntity() != null) {
                NeoForge.EVENT_BUS.post(new EntityAttackEvent.Post(event.getSource().getDirectEntity(), event, true));
            }
        }

        @SubscribeEvent(priority = EventPriority.HIGHEST)
        public static void preKill(LivingDeathEvent event) {
            if (event.getSource().getEntity() != null) {
                NeoForge.EVENT_BUS.post(new EntityKillEvent.Pre(event.getSource().getEntity(), event, false));
            }
            if (!event.getSource().isDirect() && event.getSource().getDirectEntity() != null) {
                NeoForge.EVENT_BUS.post(new EntityKillEvent.Pre(event.getSource().getDirectEntity(), event, true));
            }
        }

        @SubscribeEvent(priority = EventPriority.LOWEST)
        public static void postKill(LivingDeathEvent event) {
            if (event.getSource().getEntity() != null) {
                NeoForge.EVENT_BUS.post(new EntityKillEvent.Post(event.getSource().getEntity(), event, false));
            }
            if (!event.getSource().isDirect() && event.getSource().getDirectEntity() != null) {
                NeoForge.EVENT_BUS.post(new EntityKillEvent.Post(event.getSource().getDirectEntity(), event, true));
            }
            event.setCanceled(false);//不允许阻止事件
        }

        /**
         * 在实体攻击时触发，转发自{@link net.neoforged.neoforge.common.damagesource.DamageContainer 伤害序列}系列事件。<p>
         * 用于适应实体事件转发系统。
         * <p>
         * Fired when entity attack, distributed from events related to {@link net.neoforged.neoforge.common.damagesource.DamageContainer damage sequence}.<p>
         * Used to adapt to entity events distribute system.
         */
        public abstract static class EntityAttackEvent extends EntityEvent {
            //如果主体实体为中间实体，此值为true。
            //true if the entity is a intermediate entity.
            public final boolean isIntermediateEntity;

            public EntityAttackEvent(Entity entity, boolean isIntermediateEntity) {
                super(entity);
                this.isIntermediateEntity = isIntermediateEntity;
            }

            /**
             * 在实体尝试对目标造成伤害时触发。转发自{@link LivingIncomingDamageEvent}最高优先级，可取消。<p>
             * Fired when entity attempted to cause damage to a target. Distributed from {@link LivingIncomingDamageEvent}
             * with the highest priority and is cancellable.
             */
            public static class Income extends EntityAttackEvent implements ICancellableEvent {
                public final LivingIncomingDamageEvent origin;

                public Income(Entity entity, LivingIncomingDamageEvent event, boolean isInBetweenEntity) {
                    super(entity, isInBetweenEntity);
                    this.origin = event;
                }
            }

            /**
             * 在实体对目标造成的伤害被计算前触发。转发自{@link LivingDamageEvent.Pre}最高优先级。<p>
             * Fired before damage value is calculated. Distributed from {@link LivingDamageEvent.Pre} with the highest priority.
             */
            public static class Pre extends EntityAttackEvent {
                public final LivingDamageEvent.Pre origin;

                public Pre(Entity entity, LivingDamageEvent.Pre event, boolean isInBetweenEntity) {
                    super(entity, isInBetweenEntity);
                    this.origin = event;
                }
            }

            /**
             * 在实体对目标造成的伤害被计算后触发。转发自{@link LivingDamageEvent.Post}最高优先级。<p>
             * Fired after damage value is calculated. Distributed from {@link LivingDamageEvent.Post} with the highest priority.
             */
            public static class Post extends EntityAttackEvent {
                public final LivingDamageEvent.Post origin;

                public Post(Entity entity, LivingDamageEvent.Post event, boolean isInBetweenEntity) {
                    super(entity, isInBetweenEntity);
                    this.origin = event;
                }
            }
        }

        /**
         * 在实体杀死目标时被触发，分别转发自{@link LivingDeathEvent}的最高与最低优先级。<p>
         * 用于适应实体事件转发系统。
         * <p>
         * Fired when entity kill the target, distributed from {@link LivingDeathEvent}'s highest and lowest priority.<p>
         * Used to adapt to entity events distribute system.
         */
        public abstract static class EntityKillEvent extends EntityEvent {
            public final LivingDeathEvent origin;
            public final boolean isIntermediateEntity;

            public EntityKillEvent(Entity entity, LivingDeathEvent origin, boolean isIntermediateEntity) {
                super(entity);
                this.origin = origin;
                this.isIntermediateEntity = isIntermediateEntity;
            }

            public static class Pre extends EntityKillEvent implements ICancellableEvent {
                public Pre(Entity entity, LivingDeathEvent origin, boolean isInBetweenEntity) {
                    super(entity, origin, isInBetweenEntity);
                }
            }

            public static class Post extends EntityKillEvent {
                public Post(Entity entity, LivingDeathEvent origin, boolean isInBetweenEntity) {
                    super(entity, origin, isInBetweenEntity);
                }
            }
        }

        /**
         * 在实体加入世界后触发，转发自{@link GatherEntityDistributeEvent}的最低优先级。<p>
         * 可以在此事件中初始化监听器内容。
         * <p>
         * Fired after entity join the world, distributed from {@link GatherEntityDistributeEvent} with the lowest priority.<p>
         * init your entity event listeners in this event.
         */
        public static class GatherEntityDistributeEvent extends EntityEvent {
            public GatherEntityDistributeEvent(Entity entity) {
                super(entity);
            }
        }
    }

//    @EventBusSubscriber(modid = BreaLib.MOD_ID)
    public static class Test {
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
    }
}
