package io.redspace.ironsjewelry.item.book;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class CyclicItemRenderer {
    static final int MILIS_PER_ITEM = 2000;
    final List<ItemStack> items;
    long lastItemDisplayMilis = 0;
    protected int index;

    public CyclicItemRenderer(List<ItemStack> items) {
        this.items = items;
    }

    public ItemStack get() {
        if (System.currentTimeMillis() > lastItemDisplayMilis + MILIS_PER_ITEM) {
            index = (index + 1) % items.size();
            lastItemDisplayMilis = System.currentTimeMillis();
        }
        return items.get(index);
    }

    public void renderBottomLeft(GuiGraphicsExtractor guiGraphics, int x, int y, float scale) {
        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().scale(scale, scale);
        guiGraphics.item(get(), (int) (x / scale), (int) (y / scale - 16));
        guiGraphics.pose().popMatrix();
    }
}
