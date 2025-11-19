package io.redspace.ironsjewelry.item.book.buttons;

import io.redspace.ironsjewelry.item.book.GuideBookState;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenRectangle;

public interface GuideBookButton {
    ScreenRectangle boundingBox();

    void render(GuiGraphics guiGraphics, boolean selected, float partialTick);

    /**
     *  whether state was changed
     */
    boolean onClick(GuideBookState state);
}
