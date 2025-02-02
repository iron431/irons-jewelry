package io.redspace.ironsjewelry.registry;

import io.redspace.ironsjewelry.IronsJewelry;
import io.redspace.ironsjewelry.core.data.PatternDefinition;
import io.redspace.ironsjewelry.item.PatternRecipeItem;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

public class CreativeTabRegistry {
    private static final DeferredRegister<CreativeModeTab> CREATIVE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, IronsJewelry.MODID);

    public static void register(IEventBus eventBus) {
        CREATIVE_TABS.register(eventBus);
    }

    public static void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            event.accept(ItemRegistry.JEWELCRAFTING_STATION_BLOCK_ITEM.get());
        } else if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.accept(ItemRegistry.RUBY.get());
            event.accept(ItemRegistry.SAPPHIRE.get());
            event.accept(ItemRegistry.TOPAZ.get());
            event.accept(ItemRegistry.MOONSTONE.get());
            event.accept(ItemRegistry.PERIDOT.get());
            event.accept(ItemRegistry.ONYX.get());
            event.accept(ItemRegistry.GARNET.get());

            event.getParameters().holders().lookup(IronsJewelryRegistries.Keys.PATTERN_REGISTRY_KEY).ifPresent(patternRegistry->{
                patternRegistry.listElements().filter(pattern->pattern.isBound() && !pattern.value().unlockedByDefault()).forEach(pattern->event.accept(PatternRecipeItem.of(pattern)));
            });
        }
    }
}
