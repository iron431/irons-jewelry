package io.redspace.ironsjewelry.registry;

import io.redspace.ironsjewelry.IronsJewelry;
import io.redspace.ironsjewelry.core.data.JewelryType;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegistryBuilder;

import java.util.function.Supplier;

public class JewelryTypeRegistry {
    public static final ResourceKey<Registry<JewelryType>> JEWELRY_TYPE_KEY = ResourceKey.createRegistryKey(IronsJewelry.id("jewelry_type"));
    public static final Registry<JewelryType> JEWELRY_TYPE_REGISTRY = new RegistryBuilder<>(JEWELRY_TYPE_KEY).defaultKey(IronsJewelry.id("empty")).create();
    private static final DeferredRegister<JewelryType> JEWELRY_TYPES = DeferredRegister.create(JEWELRY_TYPE_REGISTRY, IronsJewelry.MODID);

    public static final Supplier<JewelryType> RING = JEWELRY_TYPES.register("ring", () -> new JewelryType(ItemRegistry.RING));
    public static final Supplier<JewelryType> NECKLACE = JEWELRY_TYPES.register("necklace", () -> new JewelryType(ItemRegistry.NECKLACE));

    public static void registerRegistry(NewRegistryEvent event) {
        event.register(JEWELRY_TYPE_REGISTRY);
    }

    public static void register(IEventBus eventBus) {
        JEWELRY_TYPES.register(eventBus);
    }
}
