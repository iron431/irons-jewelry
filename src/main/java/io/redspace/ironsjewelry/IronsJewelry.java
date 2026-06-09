package io.redspace.ironsjewelry;

import com.mojang.logging.LogUtils;
import io.redspace.ironsjewelry.core.MaterialModifierDataHandler;
import io.redspace.ironsjewelry.registry.ActionRegistry;
import io.redspace.ironsjewelry.registry.AssetHandlerRegistry;
import io.redspace.ironsjewelry.registry.BlockRegistry;
import io.redspace.ironsjewelry.registry.BonusTypeRegistry;
import io.redspace.ironsjewelry.registry.CommandArgumentRegistry;
import io.redspace.ironsjewelry.registry.ComponentRegistry;
import io.redspace.ironsjewelry.registry.CreativeTabRegistry;
import io.redspace.ironsjewelry.registry.DataAttachmentRegistry;
import io.redspace.ironsjewelry.registry.IronsJewelryRegistries;
import io.redspace.ironsjewelry.registry.ItemRegistry;
import io.redspace.ironsjewelry.registry.JewelryTypeRegistry;
import io.redspace.ironsjewelry.registry.LootRegistry;
import io.redspace.ironsjewelry.registry.MenuRegistry;
import io.redspace.ironsjewelry.registry.ParameterTypeRegistry;
import io.redspace.ironsjewelry.registry.SoundRegistry;
import io.redspace.ironsjewelry.registry.VillagerRegistry;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import org.slf4j.Logger;

@Mod(IronsJewelry.MODID)
public class IronsJewelry {
    public static final String MODID = "irons_jewelry";
    public static final Logger LOGGER = LogUtils.getLogger();

    public IronsJewelry(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(CreativeTabRegistry::addCreative);
        modEventBus.addListener(IronsJewelryRegistries::registerRegistries);
        modEventBus.addListener(IronsJewelryRegistries::registerDatapackRegistries);
        NeoForge.EVENT_BUS.addListener(IronsJewelry::registerReloadListeners);

        ComponentRegistry.register(modEventBus);
        BonusTypeRegistry.register(modEventBus);
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
        AssetHandlerRegistry.register(modEventBus);

        modContainer.registerConfig(ModConfig.Type.SERVER, ServerConfig.SPEC);
    }

    public static void registerReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new MaterialModifierDataHandler());
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(IronsJewelry.MODID, path);
    }

}
