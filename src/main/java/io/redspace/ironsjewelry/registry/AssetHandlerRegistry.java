package io.redspace.ironsjewelry.registry;

import io.redspace.atlasapi.api.AssetHandler;
import io.redspace.ironsjewelry.IronsJewelry;
import io.redspace.ironsjewelry.client.JewelryAssetHandler;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class AssetHandlerRegistry {
    private static final DeferredRegister<AssetHandler> HANDLERS = DeferredRegister.create(io.redspace.atlasapi.api.AssetHandlerRegistry.ASSET_HANDLER_REGISTRY_KEY, IronsJewelry.MODID);

    public static final Supplier<JewelryAssetHandler> JEWELRY_HANDLER = HANDLERS.register("jewelry", JewelryAssetHandler::new);

    public static void register(IEventBus eventBus) {
        HANDLERS.register(eventBus);
    }
}
