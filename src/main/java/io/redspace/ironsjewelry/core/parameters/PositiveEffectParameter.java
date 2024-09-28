package io.redspace.ironsjewelry.core.parameters;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.redspace.ironsjewelry.core.IBonusParameterType;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;

import java.util.Optional;
import java.util.function.Function;

public class PositiveEffectParameter implements IBonusParameterType<Holder<MobEffect>> {

    public static final Codec<Holder<MobEffect>> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            BuiltInRegistries.MOB_EFFECT.holderByNameCodec().fieldOf("effect").forGetter(Function.identity())
    ).apply(builder, Function.identity()));

    @Override
    public Codec<Holder<MobEffect>> codec() {
        return CODEC;
    }

    @Override
    public Optional<String> getValueDescriptionId(Holder<MobEffect> value) {
        return Optional.of(value.value().getDescriptionId());
    }
}
