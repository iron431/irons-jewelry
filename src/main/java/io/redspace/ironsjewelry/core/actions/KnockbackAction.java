package io.redspace.ironsjewelry.core.actions;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.redspace.ironsjewelry.core.IAction;
import io.redspace.ironsjewelry.core.data.BonusInstance;
import io.redspace.ironsjewelry.core.data.QualityScalar;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public record KnockbackAction(QualityScalar strength) implements IAction {
    public static final MapCodec<KnockbackAction> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            QualityScalar.CODEC.fieldOf("strength").forGetter(KnockbackAction::strength)
    ).apply(builder, KnockbackAction::new));

    @Override
    public void apply(ServerLevel serverLevel, double quality, boolean applyToSelf, ServerPlayer wearer, Entity entity) {
        double strength = this.strength.sample(quality);
        Vec3 direction = entity.getBoundingBox().getCenter().subtract(wearer.getBoundingBox().getCenter());
        direction = direction.normalize().scale(strength).scale(applyToSelf ? -1 : 1);
        var target = applyToSelf ? wearer : entity;
        target.setDeltaMovement(target.getDeltaMovement().add(direction.add(0, 0.3, 0)));
        target.hurtMarked = true;
    }

    @Override
    public Component formatTooltip(BonusInstance bonusInstance, boolean applyToSelf) {
        Component target = applyToSelf ? Component.translatable("tooltip.irons_jewelry.self").withStyle(ChatFormatting.GREEN) : Component.translatable("tooltip.irons_jewelry.attacker").withStyle(ChatFormatting.RED);
        //fixme: i dont think this works. with the current translation wording, i dont think it can work
        boolean push = (applyToSelf && strength.baseAmount() > 0) || (!applyToSelf && strength.baseAmount() > 0);
        return Component.translatable((push ? "action.irons_jewelry.knockback.push" : "action.irons_jewelry.knockback.pull"), target);
    }

    @Override
    public MapCodec<? extends IAction> codec() {
        return CODEC;
    }
}
