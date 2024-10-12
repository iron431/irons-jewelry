package io.redspace.ironsjewelry.registry;

import io.redspace.ironsjewelry.IronsJewelry;
import io.redspace.ironsjewelry.core.IBonusParameterType;
import io.redspace.ironsjewelry.core.parameters.*;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegistryBuilder;

import java.util.function.Supplier;

public class ParameterTypeRegistry {
    public static final ResourceKey<Registry<IBonusParameterType<?>>> PARAMETER_REGISTRY_KEY = ResourceKey.createRegistryKey(IronsJewelry.id("bonus_parameter"));
    public static final Registry<IBonusParameterType<?>> PARAMETER_TYPE_REGISTRY = new RegistryBuilder<>(PARAMETER_REGISTRY_KEY).defaultKey(IronsJewelry.id("empty")).create();
    private static final DeferredRegister<IBonusParameterType<?>> PARAMETER_TYPES = DeferredRegister.create(PARAMETER_TYPE_REGISTRY, IronsJewelry.MODID);

    public static final Supplier<IBonusParameterType<?>> EMPTY = PARAMETER_TYPES.register("empty", EmptyParameter::new);
    public static final Supplier<AttributeParameter> ATTRIBUTE_PARAMETER = PARAMETER_TYPES.register("attribute", AttributeParameter::new);
    public static final Supplier<EffectParameter> POSITIVE_EFFECT_PARAMETER = PARAMETER_TYPES.register("positive_effect", EffectParameter::new);
    public static final Supplier<EffectParameter> NEGATIVE_EFFECT_PARAMETER = PARAMETER_TYPES.register("negative_effect", EffectParameter::new);
    public static final Supplier<EnchantmentRunnableParameter> ENCHANTMENT_PARAMETER = PARAMETER_TYPES.register("enchantment_runnable", EnchantmentRunnableParameter::new);
    public static final Supplier<ActionParameter> DEFENSIVE_ACTION_PARAMETER = PARAMETER_TYPES.register("defensive_action", ActionParameter::new);


    public static void registerRegistry(NewRegistryEvent event) {
        event.register(PARAMETER_TYPE_REGISTRY);
    }

    public static void register(IEventBus eventBus) {
        PARAMETER_TYPES.register(eventBus);
    }
}
