package io.redspace.ironsjewelry.compat.jei;

import io.redspace.ironsjewelry.block.jewelcrafting_station.JewelcraftingStationScreen;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Rect2i;

import java.util.List;

public class JewelcraftingJeiGuiHandler implements IGuiContainerHandler<JewelcraftingStationScreen> {
    @Override
    public List<Rect2i> getGuiExtraAreas(JewelcraftingStationScreen containerScreen) {
        if (containerScreen.selectedPattern < 0 || containerScreen.INFO_PAGE_CACHE.isEmpty()) {
            return List.of();
        }
        var tooltip = containerScreen.INFO_PAGE_CACHE;
        var font = Minecraft.getInstance().font;
        int width = font.width(tooltip.stream().sorted((a, b) ->
                        Integer.compareUnsigned(font.width(b), font.width(a)) // sort in reverse to get the highest width at index 0
                ).findFirst().get()
        ) + 4;
        int height = font.lineHeight * (tooltip.size() + 1);
        int x = containerScreen.getGuiLeft() + containerScreen.getXSize();
        int y = containerScreen.getGuiTop();
        return List.of(new Rect2i(x, y, width, height));
    }
}
