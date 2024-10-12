package io.redspace.ironsjewelry.core.actions;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.redspace.ironsjewelry.core.IAction;
import io.redspace.ironsjewelry.core.data.BonusInstance;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.valueproviders.FloatProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public record KnockbackAction(FloatProvider strength) implements IAction {
    public static final MapCodec<KnockbackAction> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            FloatProvider.CODEC.fieldOf("strength").forGetter(KnockbackAction::strength)
    ).apply(builder, KnockbackAction::new));

    @Override
    public void apply(ServerLevel serverLevel, double quality, boolean applyToSelf, ServerPlayer wearer, Entity entity) {
        float strength = this.strength.sample(serverLevel.random);
        Vec3 direction = entity.getBoundingBox().getCenter().subtract(wearer.getBoundingBox().getCenter());
        direction = direction.normalize().scale(strength * quality);
        if (applyToSelf) {
            wearer.setDeltaMovement(wearer.getDeltaMovement().add(direction.scale(-1)));
            wearer.hurtMarked = true;
        } else {
            entity.setDeltaMovement(wearer.getDeltaMovement().add(direction));
            entity.hurtMarked = true;
        }
    }

    @Override
    public Component formatTooltip(BonusInstance bonusInstance, boolean applyToSelf) {
        Component target = applyToSelf ? Component.translatable("tooltip.irons_jewelry.self").withStyle(ChatFormatting.GREEN) :  Component.translatable("tooltip.irons_jewelry.attacker").withStyle(ChatFormatting.RED);
        //fixme: i dont think this works. with the current translation wording, i dont think it can work
        boolean push = (applyToSelf && strength.getMinValue() > 0) || (!applyToSelf && strength.getMinValue() > 0);
        return Component.translatable((push ? "action.irons_jewelry.knockback.push" : "action.irons_jewelry.knockback.pull"), target);
    }

    @Override
    public MapCodec<? extends IAction> codec() {
        return CODEC;
    }
}
