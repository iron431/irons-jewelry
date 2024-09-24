package io.redspace.ironsjewelry.core.bonuses;

import io.redspace.ironsjewelry.core.IBonus;
import io.redspace.ironsjewelry.core.parameters.PositiveEffectParameter;
import io.redspace.ironsjewelry.registry.ParameterTypeRegistry;

public class EffectOnHitBonus implements IBonus {
    @Override
    public PositiveEffectParameter getParameter() {
        return ParameterTypeRegistry.POSITIVE_EFFECT_PARAMETER.get();
    }
}
