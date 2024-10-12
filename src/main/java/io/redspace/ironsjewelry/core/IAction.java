package io.redspace.ironsjewelry.core;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import io.redspace.ironsjewelry.core.data.BonusInstance;
import io.redspace.ironsjewelry.core.data.PlayerData;
import io.redspace.ironsjewelry.registry.ActionRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

import java.util.function.Function;

public interface IAction {
    Codec<IAction> CODEC = ActionRegistry.ACTION_REGISTRY
            .byNameCodec()
            .dispatch(IAction::codec, Function.identity());

    void apply(ServerLevel serverLevel, double quality, boolean applyToSelf, ServerPlayer wearer, Entity entity);

    default void handleAction(ServerLevel serverLevel, BonusInstance bonusInstance, boolean applyToSelf, int cooldownTicks, ServerPlayer wearer, Entity entity) {
        var playerData = PlayerData.get(wearer);
        if (cooldownTicks <= 0 || !playerData.isOnCooldown(bonusInstance.bonus())) {
            apply(serverLevel, bonusInstance.quality(), applyToSelf, wearer, entity);
            if (cooldownTicks > 0) {
                playerData.addCooldown(bonusInstance.bonus(), cooldownTicks);
            }
        }

    }
    Component formatTooltip(BonusInstance bonusInstance, boolean applyToSelf);

    MapCodec<? extends IAction> codec();
}
