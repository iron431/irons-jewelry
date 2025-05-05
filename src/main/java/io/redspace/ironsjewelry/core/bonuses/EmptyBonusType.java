package io.redspace.ironsjewelry.core.bonuses;

import io.redspace.ironsjewelry.core.parameters.IBonusParameterType;
import io.redspace.ironsjewelry.registry.ParameterTypeRegistry;

public class EmptyBonusType extends BonusType {
    @Override
    public IBonusParameterType<?> getParameterType() {
        return ParameterTypeRegistry.EMPTY.get();
    }
}
