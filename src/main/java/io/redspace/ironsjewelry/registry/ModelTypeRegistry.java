package io.redspace.ironsjewelry.registry;

import io.redspace.atlasapi.api.ModelType;
import io.redspace.ironsjewelry.IronsJewelry;
import io.redspace.ironsjewelry.client.JewelryModelType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModelTypeRegistry {
    private static final DeferredRegister<ModelType> MODEL_TYPES = DeferredRegister.create(io.redspace.atlasapi.api.ModelTypeRegistry.MODEL_TYPE_REGISTRY_KEY, IronsJewelry.MODID);

    public static final Supplier<JewelryModelType> JEWELRY_MODEL_TYPE = MODEL_TYPES.register("jewelry", JewelryModelType::new);

    public static void register(IEventBus eventBus) {
        MODEL_TYPES.register(eventBus);
    }
}
