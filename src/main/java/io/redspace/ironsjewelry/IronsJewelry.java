package io.redspace.ironsjewelry;

import com.mojang.logging.LogUtils;
import io.redspace.ironsjewelry.client.DynamicModel;
import io.redspace.ironsjewelry.core.data_registry.MaterialDataHandler;
import io.redspace.ironsjewelry.core.data_registry.PartDataHandler;
import io.redspace.ironsjewelry.core.data_registry.PatternDataHandler;
import io.redspace.ironsjewelry.registry.ParameterTypeRegistry;
import io.redspace.ironsjewelry.registry.BonusRegistry;
import io.redspace.ironsjewelry.registry.ComponentRegistry;
import io.redspace.ironsjewelry.registry.ItemRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(IronsJewelry.MODID)
public class IronsJewelry {
    // Define mod patternId in a common place for everything to reference
    public static final String MODID = "irons_jewelry";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();

//    // Creates a creative tab with the patternId "examplemod:example_tab" for the example item, that is placed after the combat tab
//    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> EXAMPLE_TAB = CREATIVE_MODE_TABS.register("example_tab", () -> CreativeModeTab.builder()
//            .title(Component.translatable("itemGroup.examplemod")) //The language key for the title of your CreativeModeTab
//            .withTabsBefore(CreativeModeTabs.COMBAT)
//            .icon(() -> EXAMPLE_ITEM.get().getDefaultInstance())
//            .displayItems((parameters, output) -> {
//                output.accept(EXAMPLE_ITEM.get()); // Add the example item to the tab. For your own tabs, this method is preferred over the event
//            }).build());

    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameterOrSource types like IEventBus or ModContainer and pass them in automatically.
    public IronsJewelry(IEventBus modEventBus, ModContainer modContainer) {
        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::registerModelLoader);
        modEventBus.addListener(BonusRegistry::registerRegistry);
        modEventBus.addListener(ParameterTypeRegistry::registerRegistry);
        NeoForge.EVENT_BUS.addListener(this::registerReloadListeners);

        ComponentRegistry.register(modEventBus);
        BonusRegistry.register(modEventBus);
        ParameterTypeRegistry.register(modEventBus);
        ItemRegistry.register(modEventBus);

        // Register ourselves for server and other game events we are interested in.
        // Note that this is necessary if and only if we want *this* class (ExampleMod) to respond directly to events.
        // Do not add this line if there are no @SubscribeEvent-annotated functions in this class, like onServerStarting() below.
        //NeoForge.EVENT_BUS.register(this);

        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        // Some common setup code
        LOGGER.info("HELLO FROM COMMON SETUP");

        if (Config.logDirtBlock)
            LOGGER.info("DIRT BLOCK >> {}", BuiltInRegistries.BLOCK.getKey(Blocks.DIRT));

        LOGGER.info(Config.magicNumberIntroduction + Config.magicNumber);

        Config.items.forEach((item) -> LOGGER.info("ITEM >> {}", item.toString()));
    }

    // Add the example block item to the building blocks tab
//    private void addCreative(BuildCreativeModeTabContentsEvent event) {
//        if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS)
//            event.accept(EXAMPLE_BLOCK_ITEM);
//    }
    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @EventBusSubscriber(modid = MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            // Some client setup code
            LOGGER.info("HELLO FROM CLIENT SETUP");
            LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
        }
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
