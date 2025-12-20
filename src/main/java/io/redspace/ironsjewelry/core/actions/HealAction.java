package io.redspace.ironsjewelry.core.actions;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.redspace.ironsjewelry.core.data.BonusInstance;
import io.redspace.ironsjewelry.core.data.QualityScalar;
import io.redspace.ironsjewelry.utils.Utils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

import java.util.Map;
import java.util.Optional;

public record HealAction(QualityScalar amount) implements IAction {
    public static final MapCodec<HealAction> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            QualityScalar.CODEC.fieldOf("amount").forGetter(HealAction::amount)
    ).apply(builder, HealAction::new));

    @Override
    public void apply(ServerLevel serverLevel, double quality, boolean applyToSelf, ServerPlayer wearer, Entity entity) {
        wearer.heal((float) amount.sample(quality));
    }

    @Override
    public Component formatTooltip(BonusInstance bonusInstance, boolean applyToSelf) {
        String translation = "action.irons_jewelry.heal";
        return Component.translatable(translation, Component.literal(Utils.stringTruncation(amount.sample(bonusInstance.quality()), 1)).withStyle(ChatFormatting.GREEN));
    }

    @Override
    public Component simpleDescription(MutableComponent actionName) {
        return formatTooltip(new BonusInstance(null, 1.0, Map.of(), Optional.empty()), false);
    }

    @Override
    public MapCodec<? extends IAction> codec() {
        return CODEC;
    }
}
