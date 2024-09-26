package io.redspace.ironsjewelry.core.bonuses;

import io.redspace.ironsjewelry.core.Bonus;
import io.redspace.ironsjewelry.core.IBonusParameterType;
import io.redspace.ironsjewelry.registry.ParameterTypeRegistry;

public class DeathBonus extends Bonus {
//    public static final MapCodec<DeathBonus> CODEC = RecordCodecBuilder.mapCodec(builder ->
//            builder.group(Codec.BOOL.fieldOf("deathBonus").forGetter(o -> true)).apply(builder, (b) -> new DeathBonus()));
//
//    @Override
//    public MapCodec<DeathBonus> codec() {
//        return CODEC;
//    }

    @Override
    public IBonusParameterType<?> getParameterType() {
        return ParameterTypeRegistry.EMPTY.get();
    }
}
