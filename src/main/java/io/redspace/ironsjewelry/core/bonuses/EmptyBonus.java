package io.redspace.ironsjewelry.core.bonuses;

import com.mojang.serialization.MapCodec;
import io.redspace.ironsjewelry.core.IBonus;

public class EmptyBonus implements IBonus {
    public static final MapCodec<EmptyBonus> CODEC = MapCodec.unit(new EmptyBonus());
    @Override
    public MapCodec<EmptyBonus> codec() {
        return CODEC;
    }
}
