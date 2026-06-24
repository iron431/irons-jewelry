package io.redspace.ironsjewelry.item.book;

import net.minecraft.client.gui.GuiGraphics;
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

    public void renderBottomLeft(GuiGraphics guiGraphics, int x, int y, float scale) {
        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(scale, scale, scale);
        guiGraphics.renderItem(get(), (int) (x / scale), (int) (y / scale - 16));
        guiGraphics.pose().popPose();
    }
}
