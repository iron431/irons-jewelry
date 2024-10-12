package io.redspace.ironsjewelry.core.actions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.redspace.ironsjewelry.core.IAction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
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
        int ticks = (int) (this.duration * quality);
        int amplifier = (int) ((this.amplifier + 1) * quality) - 1;
        var mobEffectInstance = new MobEffectInstance(effect, ticks, amplifier);
        if (applyToSelf) {
            wearer.addEffect(mobEffectInstance);
        } else if(entity instanceof LivingEntity livingEntity){
            livingEntity.addEffect(mobEffectInstance);
        }
    }

    @Override
    public MapCodec<? extends IAction> codec() {
        return CODEC;
    }
}
