package io.redspace.ironsjewelry.core.actions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.redspace.ironsjewelry.core.IAction;
import io.redspace.ironsjewelry.core.Utils;
import io.redspace.ironsjewelry.core.data.BonusInstance;
import io.redspace.ironsjewelry.core.data.QualityScalar;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

public record ApplyFreezeAction(QualityScalar amount,
                                boolean instaFreeze) implements IAction {
    public static final MapCodec<ApplyFreezeAction> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            QualityScalar.CODEC.fieldOf("amount").forGetter(ApplyFreezeAction::amount),
            Codec.BOOL.optionalFieldOf("instaFreeze", false).forGetter(ApplyFreezeAction::instaFreeze)
    ).apply(builder, ApplyFreezeAction::new));

    @Override
    public void apply(ServerLevel serverLevel, double quality, boolean applyToSelf, ServerPlayer wearer, Entity entity) {

        var target = applyToSelf ? wearer : entity;
        target.setTicksFrozen(target.getTicksFrozen() + getFreezeTicks(target, quality));
    }

    public int getFreezeTicks(@Nullable Entity entity, double quality) {
        var base = 140; // default ticks required to freeze
        if (entity != null) {
            base = entity.getTicksRequiredToFreeze();
        }
        if (instaFreeze && (entity == null || !entity.isFullyFrozen())) {
            return base + (int) amount.sample(quality);
        } else {
            return (int) amount.sample(quality);
        }
    }

    @Override
    public Component formatTooltip(BonusInstance bonusInstance, boolean applyToSelf) {
        String translation = "action.irons_jewelry.apply_freeze";
        translation += instaFreeze ? ".freeze" : ".add";
        translation += applyToSelf ? ".self" : ".entity";
        return Component.translatable(translation, Utils.timeFromTicks(getFreezeTicks(null, bonusInstance.quality()), 0));
    }

    @Override
    public MapCodec<? extends IAction> codec() {
        return CODEC;
    }
}
