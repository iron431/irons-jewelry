package io.redspace.ironsjewelry.registry;

import io.redspace.ironsjewelry.IronsJewelry;
import io.redspace.ironsjewelry.core.data.JewelryType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class JewelryTypeRegistry {
    private static final DeferredRegister<JewelryType> JEWELRY_TYPES = DeferredRegister.create(IronsJewelryRegistries.JEWELRY_TYPE_REGISTRY, IronsJewelry.MODID);

    public static final Supplier<JewelryType> RING = JEWELRY_TYPES.register("ring", () -> new JewelryType(ItemRegistry.RING));
    public static final Supplier<JewelryType> NECKLACE = JEWELRY_TYPES.register("necklace", () -> new JewelryType(ItemRegistry.NECKLACE));

    public static void register(IEventBus eventBus) {
        JEWELRY_TYPES.register(eventBus);
    }
}
