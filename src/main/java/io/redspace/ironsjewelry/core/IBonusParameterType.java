package io.redspace.ironsjewelry.core;

import com.mojang.serialization.Codec;
import io.redspace.ironsjewelry.IronsJewelry;
import io.redspace.ironsjewelry.registry.ParameterTypeRegistry;

import java.util.Map;
import java.util.Optional;

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

    default Optional<T> resolve(Map<IBonusParameterType<?>, Object> parameters) {
        var param = parameters.get(this);
        //TODO: instanceof T?
        try {
            return param == null ? Optional.empty() : Optional.of((T) param);
        } catch (ClassCastException e) {
            IronsJewelry.LOGGER.error("Invalid parameter data association found: {} to {}", this, param);
            return Optional.empty();
        }
    }
}
