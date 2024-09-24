package io.redspace.ironsjewelry.core;

import com.mojang.serialization.Codec;
import io.redspace.ironsjewelry.registry.BonusRegistry;

public interface IBonus {
    //    Codec<IBonus> DIRECT_CODEC = BonusRegistry.BONUS_REGISTRY.byNameCodec()
//            .dispatch(IBonus::codec, Function.identity());
//    MapCodec<? extends IBonus> codec();
    public static final Codec<IBonus> REGISTRY_CODEC = BonusRegistry.BONUS_REGISTRY.byNameCodec();

    IBonusParameterType<?> getParameter();
}
