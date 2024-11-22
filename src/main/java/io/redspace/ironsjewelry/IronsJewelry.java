package io.redspace.ironsjewelry;

import com.mojang.logging.LogUtils;
import io.redspace.ironsjewelry.api.internal.DynamicModel;
import io.redspace.ironsjewelry.client.ClientData;
import io.redspace.ironsjewelry.client.JewelryAtlas;
import io.redspace.ironsjewelry.core.MaterialModiferDataHandler;
import io.redspace.ironsjewelry.registry.*;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import org.slf4j.Logger;

@Mod(IronsJewelry.MODID)
public class IronsJewelry {
    public static final String MODID = "irons_jewelry";
    public static final Logger LOGGER = LogUtils.getLogger();

    public IronsJewelry(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::registerModelLoader);
        modEventBus.addListener(this::registerAtlas);
        modEventBus.addListener(CreativeTabRegistry::addCreative);
        modEventBus.addListener(IronsJewelryRegistries::registerRegistries);
        modEventBus.addListener(IronsJewelryRegistries::registerDatapackRegistries);
        NeoForge.EVENT_BUS.addListener(IronsJewelry::registerReloadListeners);

        ComponentRegistry.register(modEventBus);
        BonusRegistry.register(modEventBus);
        ParameterTypeRegistry.register(modEventBus);
        ItemRegistry.register(modEventBus);
        BlockRegistry.register(modEventBus);
        MenuRegistry.register(modEventBus);
        CommandArgumentRegistry.register(modEventBus);
        DataAttachmentRegistry.register(modEventBus);
        CreativeTabRegistry.register(modEventBus);
        JewelryTypeRegistry.register(modEventBus);
        ActionRegistry.register(modEventBus);
        LootRegistry.register(modEventBus);
        VillagerRegistry.register(modEventBus);
        SoundRegistry.register(modEventBus);

        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    public static void registerReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new MaterialModiferDataHandler());
    }

    public void registerAtlas(RegisterClientReloadListenersEvent event){
        ClientData.JEWELRY_ATLAS = new JewelryAtlas(Minecraft.getInstance().getTextureManager());
        event.registerReloadListener(ClientData.JEWELRY_ATLAS);
    }
    public void registerModelLoader(ModelEvent.RegisterGeometryLoaders event) {
        event.register(id("dynamic_model"), DynamicModel.Loader.INSTANCE);
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(IronsJewelry.MODID, path);
    }

}
