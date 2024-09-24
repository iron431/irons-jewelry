package io.redspace.ironsjewelry.core;

import com.mojang.serialization.Codec;
import io.redspace.ironsjewelry.registry.ParameterTypeRegistry;

import java.util.Map;

public interface IBonusParameterType<T> {
    public static final Codec<Map<IBonusParameterType<?>, Object>> BONUS_TO_INSTANCE_CODEC = Codec.dispatchedMap(
            ParameterTypeRegistry.PARAMETER_TYPE_REGISTRY.byNameCodec(),
            IBonusParameterType::codec
    );
    Codec<IBonusParameterType<?>> REGISTRY_CODEC = ParameterTypeRegistry.PARAMETER_TYPE_REGISTRY.byNameCodec();

    Codec<T> codec();

    default boolean isEmpty() {
        return this == ParameterTypeRegistry.EMPTY.get();
    }
}
