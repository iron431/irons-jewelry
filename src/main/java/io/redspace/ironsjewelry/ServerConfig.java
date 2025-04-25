package io.redspace.ironsjewelry;

import io.redspace.ironsjewelry.loot.LootInjectionHandler;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

@EventBusSubscriber(modid = IronsJewelry.MODID, bus = EventBusSubscriber.Bus.MOD)
public class ServerConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec.ConfigValue<Boolean> ENABLE_DYNAMIC_JEWELRY_LOOT;
    public static final ModConfigSpec.ConfigValue<Integer> JEWELER_HOUSE_WEIGHT;
    public static final ModConfigSpec SPEC;

    static {
        {
            BUILDER.push("Worldgen");
            ENABLE_DYNAMIC_JEWELRY_LOOT = BUILDER.worldRestart()
                    .comment("Whether to include dynamically inserted jewelry items into loot tables based on the loot tables contents.")
                    .comment("(i.e. loot tables with gems and equipment can automatically generate jewelry items) (Default: true)")
                    .define("enableDynamicJewelryLoot", true);
            JEWELER_HOUSE_WEIGHT = BUILDER.worldRestart()
                    .comment("The weight of the Jeweler's House in village generation. (Default: 2)")
                    .define("jewelerHouseWeight", 2); // two is common weight for artisan building
            BUILDER.pop();
        }

        SPEC = BUILDER.build();
    }

    @SubscribeEvent
    static void onLoad(final ModConfigEvent.Reloading event) {

    }
}
