package io.redspace.ironsjewelry.item.book.buttons;

import io.redspace.ironsjewelry.item.book.GuideBookState;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;

public interface GuideBookButton {
    ScreenRectangle boundingBox();

    default ScreenRectangle boundingBox(int leftPos, int topPos) {
        var box = boundingBox();
        return new ScreenRectangle(new ScreenPosition(box.position().x() + leftPos, box.position().y() + topPos), box.width(), box.height());
    }

    void render(GuiGraphicsExtractor guiGraphics, boolean selected, float partialTick);

    /**
     * whether state was changed
     */
    boolean onClick(GuideBookState state);

    default SoundEvent getSound() {
        return SoundEvents.BOOK_PAGE_TURN;
    }
}
