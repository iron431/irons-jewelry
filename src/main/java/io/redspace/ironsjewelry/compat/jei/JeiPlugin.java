package io.redspace.ironsjewelry.compat.jei;

import io.redspace.ironsjewelry.IronsJewelry;
import io.redspace.ironsjewelry.block.jewelcrafting_station.JewelcraftingStationScreen;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import net.minecraft.resources.ResourceLocation;

@mezz.jei.api.JeiPlugin
public class JeiPlugin implements IModPlugin {
    private static final ResourceLocation ID = IronsJewelry.id("jei_plugin");

    @Override
    public ResourceLocation getPluginUid() {
        return ID;
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addGenericGuiContainerHandler(JewelcraftingStationScreen.class, new JewelcraftingJeiGuiHandler());
    }
}
