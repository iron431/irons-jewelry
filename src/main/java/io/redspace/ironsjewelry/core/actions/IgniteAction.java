package io.redspace.ironsjewelry.core.actions;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.redspace.ironsjewelry.core.IAction;
import io.redspace.ironsjewelry.core.data.BonusInstance;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.entity.Entity;

public record IgniteAction(IntProvider tickDuration) implements IAction {
    public static final MapCodec<IgniteAction> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            IntProvider.CODEC.fieldOf("tickDuration").forGetter(IgniteAction::tickDuration)
    ).apply(builder, IgniteAction::new));

    @Override
    public void apply(ServerLevel serverLevel, double quality, boolean applyToSelf, ServerPlayer wearer, Entity entity) {
        int ticks = this.tickDuration.sample(serverLevel.random);
        (applyToSelf ? wearer : entity).igniteForTicks((int) (ticks * quality));
    }

    @Override
    public Component formatTooltip(BonusInstance bonusInstance, boolean applyToSelf) {
        Component target = applyToSelf ? Component.translatable("tooltip.irons_jewelry.self").withStyle(ChatFormatting.RED) :  Component.translatable("tooltip.irons_jewelry.attacker").withStyle(ChatFormatting.RED);
        return Component.translatable("action.irons_jewelry.ignite", target);
    }

    @Override
    public MapCodec<? extends IAction> codec() {
        return CODEC;
    }
}
