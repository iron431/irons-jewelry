package io.redspace.ironsjewelry.registry;

import io.redspace.ironsjewelry.IronsJewelry;
import io.redspace.ironsjewelry.gameplay.block.jewelcrafting_station.JewelcraftingStationMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class MenuRegistry {
    private static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, IronsJewelry.MODID);

    public static void register(IEventBus eventBus) {
        MENUS.register(eventBus);
    }

    public static final DeferredHolder<MenuType<?>, MenuType<JewelcraftingStationMenu>> JEWELCRAFTING_MENU = MENUS.register("jewelcrafting_station_menu", () -> new MenuType<>(JewelcraftingStationMenu::new, FeatureFlags.DEFAULT_FLAGS));
}
