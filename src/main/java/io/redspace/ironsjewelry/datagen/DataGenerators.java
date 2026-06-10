package io.redspace.ironsjewelry.datagen;

import io.redspace.ironsjewelry.IronsJewelry;
import net.minecraft.data.DataProvider;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.Set;

@EventBusSubscriber(modid = IronsJewelry.MODID)
public class DataGenerators {
    @SubscribeEvent
    public static void gatherData(GatherDataEvent.Client event) {
        // we run both of these in the client side because thats where we locally run it? And this doesn't actually get called during gameplay?
        // https://docs.neoforged.net/primer/docs/1.21.4/neo/
        var generator = event.getGenerator();
        generator.addProvider(true, (DataProvider.Factory<DatapackBuiltinEntriesProvider>) output -> new DatapackBuiltinEntriesProvider(
                output,
                event.getLookupProvider(),
                JewelryDataRegistryGenerator.builder,
                Set.of(IronsJewelry.MODID)
        ));
        generator.addProvider(true, (DataProvider.Factory<ItemModelDataGenerator>) ItemModelDataGenerator::new);

    }

    // TODO: Item model datagen removed — ItemModelProvider no longer exists in NeoForge 26.1.2.
    // Item models are now handled via ITEM_MODEL data component or resource pack JSON directly.
}