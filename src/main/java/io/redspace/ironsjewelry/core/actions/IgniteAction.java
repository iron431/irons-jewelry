package io.redspace.ironsjewelry.core.actions;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.redspace.ironsjewelry.core.IAction;
import io.redspace.ironsjewelry.core.Utils;
import io.redspace.ironsjewelry.core.data.BonusInstance;
import io.redspace.ironsjewelry.core.data.QualityScalar;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

public record IgniteAction(QualityScalar tickDuration) implements IAction {
    public static final MapCodec<IgniteAction> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            QualityScalar.CODEC.fieldOf("tickDuration").forGetter(IgniteAction::tickDuration)
    ).apply(builder, IgniteAction::new));

    @Override
    public void apply(ServerLevel serverLevel, double quality, boolean applyToSelf, ServerPlayer wearer, Entity entity) {
        int ticks = (int) this.tickDuration.sample(quality);
        (applyToSelf ? wearer : entity).igniteForTicks(ticks);
    }

    @Override
    public Component formatTooltip(BonusInstance bonusInstance, boolean applyToSelf) {
        Component target = applyToSelf ? Component.translatable("tooltip.irons_jewelry.self").withStyle(ChatFormatting.RED) : Component.translatable("tooltip.irons_jewelry.attacker").withStyle(ChatFormatting.RED);
        return Component.translatable("action.irons_jewelry.ignite", target, Component.literal(Utils.digitalTimeFromTicks((int) tickDuration.sample(bonusInstance.quality()))));
    }

    @Override
    public MapCodec<? extends IAction> codec() {
        return CODEC;
    }
}
