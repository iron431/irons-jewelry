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
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.Vec3;

public record KnockbackAction(QualityScalar strength) implements IAction {
    public static final MapCodec<KnockbackAction> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            QualityScalar.CODEC.fieldOf("strength").forGetter(KnockbackAction::strength)
    ).apply(builder, KnockbackAction::new));

    @Override
    public void apply(ServerLevel serverLevel, double quality, boolean applyToSelf, ServerPlayer wearer, Entity entity) {
        double strength = this.strength.sample(quality);
        if (!applyToSelf && strength < 0) {
            //we are pulling attacker in, limit strength so that they don't overshoot
            float maxStrength = -wearer.distanceTo(entity) * 0.5714f; // (x/1.75)
            if (strength < maxStrength) {
                strength = maxStrength;
            }
        }
        Vec3 direction = entity.getBoundingBox().getCenter().subtract(wearer.getBoundingBox().getCenter());
        direction = direction.normalize().scale(strength).scale(applyToSelf ? -1 : 1);
        var target = applyToSelf ? wearer : entity;
        double resistance = target instanceof LivingEntity livingEntity ? Mth.clamp(1 - livingEntity.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE), .5f, 1f) : 1;
        target.setDeltaMovement(target.getDeltaMovement().add(direction.add(0, 0.3, 0).scale(resistance)));
        target.hurtMarked = true;
    }

    @Override
    public Component formatTooltip(BonusInstance bonusInstance, boolean applyToSelf) {
        Component target = applyToSelf ? Component.translatable("tooltip.irons_jewelry.self").withStyle(ChatFormatting.GREEN) : Component.translatable("tooltip.irons_jewelry.attacker").withStyle(ChatFormatting.RED);
        var strengthText = Utils.stringTruncation(Math.abs(strength().sample(bonusInstance.quality())), 1);
        boolean push = (applyToSelf && strength.baseAmount() > 0) || (!applyToSelf && strength.baseAmount() > 0);
        return Component.translatable((push ? "action.irons_jewelry.knockback.push" : "action.irons_jewelry.knockback.pull"), target, strengthText);
    }

    @Override
    public MapCodec<? extends IAction> codec() {
        return CODEC;
    }
}
