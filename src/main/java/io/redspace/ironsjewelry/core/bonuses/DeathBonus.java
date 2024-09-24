package io.redspace.ironsjewelry.core.bonuses;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.redspace.ironsjewelry.core.IBonus;
import io.redspace.ironsjewelry.core.IBonusParameterType;
import io.redspace.ironsjewelry.registry.ParameterTypeRegistry;

public class DeathBonus implements IBonus {
    public static final MapCodec<DeathBonus> CODEC = RecordCodecBuilder.mapCodec(builder ->
            builder.group(Codec.BOOL.fieldOf("deathBonus").forGetter(o -> true)).apply(builder, (b) -> new DeathBonus()));

    @Override
    public MapCodec<DeathBonus> codec() {
        return CODEC;
    }

    @Override
    public IBonusParameterType<?> getParameter() {
        return ParameterTypeRegistry.EMPTY.get();
    }
}
