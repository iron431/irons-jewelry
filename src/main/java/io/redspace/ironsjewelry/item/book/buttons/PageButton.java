package io.redspace.ironsjewelry.item.book.buttons;

import io.redspace.ironsjewelry.item.book.GuideBookState;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenAxis;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.resources.Identifier;

import java.util.function.Function;

public class PageButton implements GuideBookButton {
    final int x, y, width, height;
    final Identifier sprite, spriteHighlighted;
    final Function<GuideBookState, Boolean> onClick;

    public PageButton(int x, int y, int width, int height, Identifier sprite, Identifier spriteHighlighted, Function<GuideBookState, Boolean> onClick) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.sprite = sprite;
        this.spriteHighlighted = spriteHighlighted;
        this.onClick = onClick;
    }

    @Override
    public ScreenRectangle boundingBox() {
        return ScreenRectangle.of(ScreenAxis.HORIZONTAL, x, y, width, height);
    }

    @Override
    public void render(GuiGraphics guiGraphics, boolean selected, float partialTick) {
        Identifier sprite = selected ? this.spriteHighlighted : this.sprite;
        guiGraphics.blitSprite(sprite, x, y, width, height);
    }

    @Override
    public boolean onClick(GuideBookState state) {
        return onClick.apply(state);
    }

}
