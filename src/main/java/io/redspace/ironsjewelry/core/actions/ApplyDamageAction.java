package io.redspace.ironsjewelry.core.actions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.redspace.ironsjewelry.core.DamageHelper;
import io.redspace.ironsjewelry.core.IAction;
import io.redspace.ironsjewelry.core.Utils;
import io.redspace.ironsjewelry.core.data.BonusInstance;
import io.redspace.ironsjewelry.core.data.QualityScalar;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;

import java.util.Optional;

public record ApplyDamageAction(Holder<DamageType> damageType, QualityScalar amount,
                                Optional<Double> maximumDamage,
                                Optional<Holder<SoundEvent>> soundEvent) implements IAction {
    public static final MapCodec<ApplyDamageAction> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            DamageType.CODEC.fieldOf("damageType").forGetter(ApplyDamageAction::damageType),
            QualityScalar.CODEC.fieldOf("amount").forGetter(ApplyDamageAction::amount),
            Codec.DOUBLE.optionalFieldOf("maximum").forGetter(ApplyDamageAction::maximumDamage),
            SoundEvent.CODEC.optionalFieldOf("sound").forGetter(ApplyDamageAction::soundEvent)
    ).apply(builder, ApplyDamageAction::new));

    @Override
    public void apply(ServerLevel serverLevel, double quality, boolean applyToSelf, ServerPlayer wearer, Entity entity) {
        var damageSource = new DamageSource(this.damageType, null, wearer, wearer.position());
        var damage = getDamage(quality);
        var target = applyToSelf ? wearer : entity;
        if (applyToSelf) {
            DamageHelper.ignoreNextKnockback(wearer);
        }
        target.hurt(damageSource, damage);
        this.soundEvent.ifPresent(sound -> target.playSound(sound.value()));
    }

    public float getDamage(double quality) {
        return maximumDamage.map(max -> (float) Math.min(this.amount.sample(quality), max)).orElseGet(() -> (float) this.amount.sample(quality));
    }

    @Override
    public Component formatTooltip(BonusInstance bonusInstance, boolean applyToSelf) {
        var damage = Utils.stringTruncation(getDamage(bonusInstance.quality()), 1);
        var key = this.damageType.getKey();
        Component typeComponent = Component.empty();
        if (key != null) {
            var location = key.location();
            typeComponent = Component.translatable(String.format("damage_type.%s.%s", location.getNamespace(), location.getPath()));
        }
        return Component.translatable((applyToSelf ? "action.irons_jewelry.apply_damage.self" : "action.irons_jewelry.apply_damage.entity"), Component.literal(damage).withStyle(ChatFormatting.RED), typeComponent);
    }

    @Override
    public MapCodec<? extends IAction> codec() {
        return CODEC;
    }
}
