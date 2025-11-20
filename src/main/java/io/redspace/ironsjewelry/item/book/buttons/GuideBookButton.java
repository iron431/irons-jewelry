package io.redspace.ironsjewelry.item.book.buttons;

import io.redspace.ironsjewelry.item.book.GuideBookState;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.client.gui.navigation.ScreenRectangle;

public interface GuideBookButton {
    ScreenRectangle boundingBox();

    default ScreenRectangle boundingBox(int leftPos, int topPos) {
        var box = boundingBox();
        return new ScreenRectangle(new ScreenPosition(box.position().x() + leftPos, box.position().y() + topPos), box.width(), box.height());
    }

    void render(GuiGraphics guiGraphics, boolean selected, float partialTick);

    /**
     * whether state was changed
     */
    boolean onClick(GuideBookState state);
}
