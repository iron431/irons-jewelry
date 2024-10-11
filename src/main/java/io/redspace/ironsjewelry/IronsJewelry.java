package io.redspace.ironsjewelry;

import com.mojang.logging.LogUtils;
import io.redspace.ironsjewelry.client.DynamicModel;
import io.redspace.ironsjewelry.core.data_registry.MaterialDataHandler;
import io.redspace.ironsjewelry.core.data_registry.PartDataHandler;
import io.redspace.ironsjewelry.core.data_registry.PatternDataHandler;
import io.redspace.ironsjewelry.registry.*;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import org.slf4j.Logger;

@Mod(IronsJewelry.MODID)
public class IronsJewelry {
    public static final String MODID = "irons_jewelry";
    public static final Logger LOGGER = LogUtils.getLogger();

    public IronsJewelry(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::registerModelLoader);
        modEventBus.addListener(BonusRegistry::registerRegistry);
        modEventBus.addListener(ParameterTypeRegistry::registerRegistry);
        modEventBus.addListener(CreativeTabRegistry::addCreative);
        NeoForge.EVENT_BUS.addListener(this::registerReloadListeners);

        ComponentRegistry.register(modEventBus);
        BonusRegistry.register(modEventBus);
        ParameterTypeRegistry.register(modEventBus);
        ItemRegistry.register(modEventBus);
        BlockRegistry.register(modEventBus);
        MenuRegistry.register(modEventBus);
        CommandArgumentRegistry.register(modEventBus);
        DataAttachmentRegistry.register(modEventBus);
        CreativeTabRegistry.register(modEventBus);

        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    public void registerReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new MaterialDataHandler());
        event.addListener(new PartDataHandler());
        //Pattern Data References Part/Material data and must be run last
        event.addListener(new PatternDataHandler());
    }

    public void registerModelLoader(ModelEvent.RegisterGeometryLoaders event) {
        event.register(id("dynamic_model"), DynamicModel.Loader.INSTANCE);
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(IronsJewelry.MODID, path);
    }
}
