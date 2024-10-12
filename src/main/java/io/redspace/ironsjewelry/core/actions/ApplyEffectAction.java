package io.redspace.ironsjewelry.core.actions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.redspace.ironsjewelry.core.IAction;
import io.redspace.ironsjewelry.core.Utils;
import io.redspace.ironsjewelry.core.data.BonusInstance;
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

public record ApplyEffectAction(int duration, int amplifier, Holder<MobEffect> effect) implements IAction {
    public static final MapCodec<ApplyEffectAction> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            //TODO: some kind of cool number provider system
            Codec.INT.fieldOf("duration").forGetter(ApplyEffectAction::duration),
            Codec.INT.fieldOf("amplifier").forGetter(ApplyEffectAction::amplifier),
            BuiltInRegistries.MOB_EFFECT.holderByNameCodec().fieldOf("effect").forGetter(ApplyEffectAction::effect)
    ).apply(builder, ApplyEffectAction::new));

    @Override
    public void apply(ServerLevel serverLevel, double quality, boolean applyToSelf, ServerPlayer wearer, Entity entity) {

        var mobEffectInstance = new MobEffectInstance(effect, modifyTicks(quality), modifyAmplifier(quality));
        if (applyToSelf) {
            wearer.addEffect(mobEffectInstance);
        } else if (entity instanceof LivingEntity livingEntity) {
            livingEntity.addEffect(mobEffectInstance);
        }
    }

    private int modifyTicks(double quality) {
        return (int) (this.duration * quality);
    }

    private int modifyAmplifier(double quality) {
        return (int) ((this.amplifier + 1) * quality) - 1;
    }

    @Override
    public Component formatTooltip(BonusInstance bonusInstance, boolean applyToSelf) {
        int amp = modifyAmplifier(bonusInstance.quality());
        int ticks = modifyTicks(bonusInstance.quality());
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
