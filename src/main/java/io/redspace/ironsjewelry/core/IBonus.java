package io.redspace.ironsjewelry.core;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import io.redspace.ironsjewelry.registry.BonusRegistry;

import java.util.function.Function;

public interface IBonus {
    Codec<IBonus> DIRECT_CODEC = BonusRegistry.BONUS_REGISTRY.byNameCodec()
            .dispatch(IBonus::codec, Function.identity());
    MapCodec<? extends IBonus> codec();

    IBonusParameterType<?> getParameter();
}
