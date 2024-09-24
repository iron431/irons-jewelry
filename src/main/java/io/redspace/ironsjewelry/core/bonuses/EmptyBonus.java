package io.redspace.ironsjewelry.core.bonuses;

import io.redspace.ironsjewelry.core.IBonus;
import io.redspace.ironsjewelry.core.IBonusParameterType;
import io.redspace.ironsjewelry.registry.ParameterTypeRegistry;

public class EmptyBonus implements IBonus {
//    public static final MapCodec<EmptyBonus> CODEC = MapCodec.unit(new EmptyBonus());
//
//    @Override
//    public MapCodec<EmptyBonus> codec() {
//        return CODEC;
//    }

    @Override
    public IBonusParameterType<?> getParameter() {
        return ParameterTypeRegistry.EMPTY.get();
    }
}
