package io.redspace.ironsjewelry.event;

import io.redspace.ironsjewelry.core.bonuses.EffectOnHitBonusType;
import io.redspace.ironsjewelry.core.data.BonusInstance;
import io.redspace.ironsjewelry.core.data.JewelryData;
import io.redspace.ironsjewelry.core.parameters.ActionParameter;
import io.redspace.ironsjewelry.network.packets.SyncPlayerDataPacket;
import io.redspace.ironsjewelry.registry.BonusTypeRegistry;
import io.redspace.ironsjewelry.registry.DataAttachmentRegistry;
import io.redspace.ironsjewelry.utils.Utils;
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
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            serverPlayer.getData(DataAttachmentRegistry.PLAYER_DATA).tickCooldowns(1);
        }
    }
}
