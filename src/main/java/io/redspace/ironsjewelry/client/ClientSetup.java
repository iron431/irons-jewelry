package io.redspace.ironsjewelry.client;

import io.redspace.ironsjewelry.IronsJewelry;
import io.redspace.ironsjewelry.block.jewelcrafting_station.JewelcraftingStationScreen;
import io.redspace.ironsjewelry.core.IMinecraftInstanceHelper;
import io.redspace.ironsjewelry.core.MinecraftInstanceHelper;
import io.redspace.ironsjewelry.registry.MenuRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import org.jetbrains.annotations.Nullable;

@EventBusSubscriber(modid = IronsJewelry.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {
    @SubscribeEvent
    public static void registerMenuScreens(RegisterMenuScreensEvent event) {
        event.register(MenuRegistry.JEWELCRAFTING_MENU.get(), JewelcraftingStationScreen::new);
    }

    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MinecraftInstanceHelper.INSTANCE = new IMinecraftInstanceHelper() {
                @Nullable
                @Override
                public Player player() {
                    return Minecraft.getInstance().player;
                }
            };
        });
    }
}
