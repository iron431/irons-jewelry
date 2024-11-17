package io.redspace.ironsjewelry.core;

import com.mojang.serialization.Codec;
import io.redspace.ironsjewelry.IronsJewelry;
import io.redspace.ironsjewelry.core.data.BonusInstance;
import io.redspace.ironsjewelry.registry.IronsJewelryRegistries;
import io.redspace.ironsjewelry.registry.ParameterTypeRegistry;

import java.util.Map;
import java.util.Optional;

public interface IBonusParameterType<T> {
    /**
     * Codec which relates the Parameter Type to the Data Object associated with that parameter, in a map, via a dispatch map codec
     */
    Codec<Map<IBonusParameterType<?>, Object>> BONUS_TO_INSTANCE_CODEC = Codec.dispatchedMap(
            IronsJewelryRegistries.PARAMETER_TYPE_REGISTRY.byNameCodec(),
            IBonusParameterType::codec
    );
    Codec<IBonusParameterType<?>> REGISTRY_CODEC = IronsJewelryRegistries.PARAMETER_TYPE_REGISTRY.byNameCodec();

    Codec<T> codec();

    Optional<String> getValueDescriptionId(T value);

    default boolean isEmpty() {
        return this == ParameterTypeRegistry.EMPTY.get();
    }

    default Optional<T> resolve(Map<IBonusParameterType<?>, Object> parameters) {
        var param = parameters.get(this);
        try {
            return param == null ? Optional.empty() : Optional.of((T) param);
        } catch (ClassCastException e) {
            IronsJewelry.LOGGER.error("Invalid parameter data association found: {} to {}", this, param);
            return Optional.empty();
        }
    }

    default Optional<T> resolve(BonusInstance bonusInstance) {
        return resolve(bonusInstance.parameter());
    }
}
