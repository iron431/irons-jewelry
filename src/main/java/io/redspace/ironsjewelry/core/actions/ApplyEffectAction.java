package io.redspace.ironsjewelry.core.actions;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.redspace.ironsjewelry.core.IAction;
import io.redspace.ironsjewelry.core.Utils;
import io.redspace.ironsjewelry.core.data.BonusInstance;
import io.redspace.ironsjewelry.core.data.QualityScalar;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public record ApplyEffectAction(QualityScalar duration, QualityScalar amplifier,
                                Holder<MobEffect> effect) implements IAction {
    public static final MapCodec<ApplyEffectAction> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            QualityScalar.CODEC.fieldOf("duration").forGetter(ApplyEffectAction::duration),
            QualityScalar.CODEC.fieldOf("amplifier").forGetter(ApplyEffectAction::amplifier),
            BuiltInRegistries.MOB_EFFECT.holderByNameCodec().fieldOf("effect").forGetter(ApplyEffectAction::effect)
    ).apply(builder, ApplyEffectAction::new));

    @Override
    public void apply(ServerLevel serverLevel, double quality, boolean applyToSelf, ServerPlayer wearer, Entity entity) {

        var mobEffectInstance = new MobEffectInstance(effect, ticks(quality), amplifier(quality));
        if (applyToSelf) {
            wearer.addEffect(mobEffectInstance);
        } else if (entity instanceof LivingEntity livingEntity) {
            livingEntity.addEffect(mobEffectInstance);
        }
    }

    private int ticks(double quality) {
        return (int) duration.sample(quality);
    }

    private int amplifier(double quality) {
        return (int) amplifier.sample(quality);
    }

    @Override
    public Component formatTooltip(BonusInstance bonusInstance, boolean applyToSelf) {
        int amp = amplifier(bonusInstance.quality());
        int ticks = ticks(bonusInstance.quality());
        MutableComponent effectComponent = Component.translatable(effect.value().getDescriptionId());
        var color = applyToSelf ? ChatFormatting.GREEN : ChatFormatting.RED;
        if (amp > 0) {
            effectComponent = Component.translatable(
                    "potion.withAmplifier", effectComponent, Component.translatable("potion.potency." + amp)
            ).withStyle(color);
        }
        var timeComponenet = effect.value().isInstantenous() ? Component.literal("") : Component.literal(Utils.digitalTimeFromTicks(ticks, true)).withStyle(color);
        return Component.translatable((applyToSelf ? "action.irons_jewelry.apply_effect.self" : "action.irons_jewelry.apply_effect.entity"), effectComponent, timeComponenet);
    }

    @Override
    public MapCodec<? extends IAction> codec() {
        return CODEC;
    }
}
