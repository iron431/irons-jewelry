package io.redspace.ironsjewelry.core.actions;

import com.mojang.serialization.Codec;
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

public record ApplyFreezeAction(QualityScalar amount,
                                boolean instaFreeze) implements IAction {
    public static final MapCodec<ApplyFreezeAction> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            QualityScalar.CODEC.fieldOf("amount").forGetter(ApplyFreezeAction::amount),
            Codec.BOOL.optionalFieldOf("instaFreeze", false).forGetter(ApplyFreezeAction::instaFreeze)
    ).apply(builder, ApplyFreezeAction::new));

    @Override
    public void apply(ServerLevel serverLevel, double quality, boolean applyToSelf, ServerPlayer wearer, Entity entity) {
        var target = applyToSelf ? wearer : entity;
        var freezeTicks = getFreezeTicks(quality) * 2;
        var maxTicks = freezeTicks * 2;
        var instaFreeze = target.isFullyFrozen() ? 0 : target.getTicksRequiredToFreeze();
        target.setTicksFrozen(instaFreeze + Math.min(target.getTicksFrozen() + freezeTicks, maxTicks));
    }

    public int getFreezeTicks(double quality) {
        return (int) amount.sample(quality);
    }

    @Override
    public Component formatTooltip(BonusInstance bonusInstance, boolean applyToSelf) {
        String translation = "action.irons_jewelry.apply_freeze";
        translation += instaFreeze ? ".freeze" : ".add";
        translation += applyToSelf ? ".self" : ".entity";
        return Component.translatable(translation, Component.literal(Utils.timeFromTicks(getFreezeTicks(bonusInstance.quality()), 0)).withStyle(applyToSelf ? ChatFormatting.RED : ChatFormatting.GREEN));
    }

    @Override
    public MapCodec<? extends IAction> codec() {
        return CODEC;
    }
}
