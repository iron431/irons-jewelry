package io.redspace.ironsjewelry.api;

import io.redspace.ironsjewelry.IronsJewelry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegistryBuilder;

public class ModelTypeRegistry {

    public static final ResourceKey<Registry<ModelType>> MODEL_TYPE_REGISTRY_KEY = ResourceKey.createRegistryKey(IronsJewelry.id("model_type"));
    public static final Registry<ModelType> MODEL_TYPE_REGISTRY = new RegistryBuilder<>(MODEL_TYPE_REGISTRY_KEY).defaultKey(IronsJewelry.id("empty")).create();

    public static void registerRegistries(NewRegistryEvent event) {
        event.register(MODEL_TYPE_REGISTRY);
    }
}
