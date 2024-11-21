package io.redspace.ironsjewelry.core;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import io.redspace.ironsjewelry.core.data.BonusInstance;
import io.redspace.ironsjewelry.core.data.PlayerData;
import io.redspace.ironsjewelry.core.data.QualityScalar;
import io.redspace.ironsjewelry.registry.IronsJewelryRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

import java.util.Optional;
import java.util.function.Function;

public interface IAction {
    Codec<IAction> CODEC = IronsJewelryRegistries.ACTION_REGISTRY
            .byNameCodec()
            .dispatch(IAction::codec, Function.identity());

    void apply(ServerLevel serverLevel, double quality, boolean applyToSelf, ServerPlayer wearer, Entity entity);

    default void handleAction(ServerLevel serverLevel, BonusInstance bonusInstance, boolean applyToSelf, Optional<QualityScalar> cooldown, ServerPlayer wearer, Entity entity) {
        var playerData = PlayerData.get(wearer);
        int cooldownTicks = cooldown.map(scalar -> CooldownHandler.INSTANCE.getCooldown(wearer, scalar, bonusInstance.quality())).orElse(0);
        if (cooldownTicks <= 0 || !playerData.isOnCooldown(bonusInstance.bonus())) {
            apply(serverLevel, bonusInstance.quality(), applyToSelf, wearer, entity);
//            wearer.level.playSound(null, wearer.blockPosition(), SoundRegistry.GENERIC_ACTION.get(), SoundSource.PLAYERS);
            if (cooldownTicks > 0) {
                playerData.addCooldown(bonusInstance.bonus(), cooldownTicks);
            }
        }

    }

    Component formatTooltip(BonusInstance bonusInstance, boolean applyToSelf);

    MapCodec<? extends IAction> codec();
}
