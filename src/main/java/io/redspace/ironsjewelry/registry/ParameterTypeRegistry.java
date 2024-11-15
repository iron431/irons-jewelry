package io.redspace.ironsjewelry.registry;

import io.redspace.ironsjewelry.IronsJewelry;
import io.redspace.ironsjewelry.core.IBonusParameterType;
import io.redspace.ironsjewelry.core.parameters.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ParameterTypeRegistry {
    private static final DeferredRegister<IBonusParameterType<?>> PARAMETER_TYPES = DeferredRegister.create(IronsJewelryRegistries.PARAMETER_TYPE_REGISTRY, IronsJewelry.MODID);

    public static final Supplier<IBonusParameterType<?>> EMPTY = PARAMETER_TYPES.register("empty", EmptyParameter::new);
    public static final Supplier<AttributeParameter> ATTRIBUTE_PARAMETER = PARAMETER_TYPES.register("attribute", AttributeParameter::new);
    public static final Supplier<EffectParameter> POSITIVE_EFFECT_PARAMETER = PARAMETER_TYPES.register("positive_effect", EffectParameter::new);
    public static final Supplier<EffectParameter> NEGATIVE_EFFECT_PARAMETER = PARAMETER_TYPES.register("negative_effect", EffectParameter::new);
    public static final Supplier<EnchantmentRunnableParameter> ENCHANTMENT_PARAMETER = PARAMETER_TYPES.register("enchantment_runnable", EnchantmentRunnableParameter::new);
    public static final Supplier<ActionParameter> ACTION_PARAMETER = PARAMETER_TYPES.register("action", ActionParameter::new);

    public static void register(IEventBus eventBus) {
        PARAMETER_TYPES.register(eventBus);
    }
}
