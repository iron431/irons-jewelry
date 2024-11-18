package io.redspace.ironsjewelry.event;

import io.redspace.ironsjewelry.core.Utils;
import io.redspace.ironsjewelry.core.bonuses.EffectOnHitBonus;
import io.redspace.ironsjewelry.core.data.BonusInstance;
import io.redspace.ironsjewelry.core.data.JewelryData;
import io.redspace.ironsjewelry.core.parameters.ActionParameter;
import io.redspace.ironsjewelry.network.packets.SyncPlayerDataPacket;
import io.redspace.ironsjewelry.registry.BonusRegistry;
import io.redspace.ironsjewelry.registry.DataAttachmentRegistry;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingShieldBlockEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

@EventBusSubscriber
public class ServerEvents {

//    @SubscribeEvent
//    public static void testEvent(SetupJewelcraftingResultEvent event) {
//        if (!FMLLoader.isProduction()) {
//            var stack = event.getResult();
//            if (!stack.isEmpty()) {
//                var data = JewelryData.get(stack);
//                var gold = IronsJewelryRegistries.materialRegistry(event.getPlayer().registryAccess()).getHolderOrThrow(ResourceKey.create(IronsJewelryRegistries.Keys.MATERIAL_REGISTRY_KEY, IronsJewelry.id("gold")));
//                if (data.parts().containsValue(gold)) {
//                    var map = data.parts();
//                    map.replaceAll((p, v) -> gold);
//                    JewelryData jewelryData = new JewelryData(data.pattern(), map);
//                    event.getResult().set(ComponentRegistry.JEWELRY_COMPONENT, jewelryData);
//                    event.getResult().set(DataComponents.CUSTOM_NAME, Component.literal("Midas Touch"));
//                }
//            }
//        }
//    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            PacketDistributor.sendToPlayer(serverPlayer, new SyncPlayerDataPacket(serverPlayer.getData(DataAttachmentRegistry.PLAYER_DATA)));
        }
    }

    @SubscribeEvent
    public static void onShieldBlock(LivingShieldBlockEvent event) {
        if (event.getBlocked() && event.getEntity() instanceof ServerPlayer player && event.getDamageSource().getEntity() instanceof LivingEntity livingAttacker) {
            var items = Utils.getEquippedJewelry(player);
            for (ItemStack stack : items) {
                JewelryData.ifPresent(stack, jewelryData -> {
                    jewelryData.forBonuses(BonusRegistry.ON_SHIELD_BLOCK_BONUS.get(), ActionParameter.ActionRunnable.class, (bonus, action) -> {
                        action.action().handleAction(player.serverLevel(), bonus, action.targetSelf(), bonus.cooldown(), player, livingAttacker);
                    });
                });
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            serverPlayer.getData(DataAttachmentRegistry.PLAYER_DATA).tickCooldowns(1);
        }
    }

    @SubscribeEvent
    public static void onLivingDamaged(LivingIncomingDamageEvent event) {
        var damageSource = event.getSource();
        @Nullable var attacker = event.getSource().getEntity();
        var victim = event.getEntity();
        /*
        Attacker Effects
         */
        if (attacker instanceof ServerPlayer player) {
            var bonuses = Utils.getEquippedBonuses(player);
            for (BonusInstance instance : bonuses) {
                /*
                Action on projectile hit Bonus
                 */
                if (instance.bonus().equals(BonusRegistry.ON_PROJECTILE_HIT_BONUS.get())) {
                    if (damageSource.getDirectEntity() instanceof Projectile) {
                        BonusRegistry.ON_PROJECTILE_HIT_BONUS.get().getParameterType().resolve(instance).ifPresent(
                                action -> action.action().handleAction(player.serverLevel(), instance, action.targetSelf(), instance.cooldown(), player, victim));
                    }
                }
                /*
                Action on Hit Bonus
                 */
                else if (instance.bonus().equals(BonusRegistry.ON_ATTACK_BONUS.get())) {
                    if (damageSource.isDirect()) {
                        BonusRegistry.ON_ATTACK_BONUS.get().getParameterType().resolve(instance).ifPresent(
                                action -> action.action().handleAction(player.serverLevel(), instance, action.targetSelf(), instance.cooldown(), player, victim));
                    }
                }
            }
        }
        /*
        Victim Effects
         */
        if (victim instanceof ServerPlayer player) {
            var bonuses = Utils.getEquippedBonuses(player);
            for (BonusInstance instance : bonuses) {
                /*
                Effect when hit Bonus
                 */
                if (instance.bonus() instanceof EffectOnHitBonus effectOnHitBonus) {
                    effectOnHitBonus.getParameterType().resolve(instance.parameter()).ifPresent(effect ->
                            player.addEffect(new MobEffectInstance(effect, effectOnHitBonus.durationInTicks(effect, instance.quality())))
                    );
                }
                /*
                Action on Take Damage Bonus
                 */
                else if (instance.bonus().equals(BonusRegistry.ON_TAKE_DAMAGE_BONUS.get()) && attacker != null) {
                    //TODO: create map of bonus to consumer or something?
                    BonusRegistry.ON_TAKE_DAMAGE_BONUS.get().getParameterType().resolve(instance).ifPresent(
                            effect -> effect.action().handleAction(player.serverLevel(), instance, effect.targetSelf(), instance.cooldown(), player, attacker));
                }
            }
        }
    }

    @SubscribeEvent
    public static void onEffectApplication(MobEffectEvent.Applicable event) {
        if (event.getEntity() instanceof Player player) {
            Utils.getEquippedJewelry(player).forEach(stack ->
                    JewelryData.ifPresent(stack, data -> data.forBonuses(BonusRegistry.EFFECT_IMMUNITY_BONUS.get(), Holder.class,
                            (bonus, param) -> {
                                if (event.getEffectInstance() != null && event.getEffectInstance().getEffect().equals(param)) {
                                    event.setResult(MobEffectEvent.Applicable.Result.DO_NOT_APPLY);
                                }
                            }))
            );
        }
    }
}
