package io.redspace.ironsjewelry.item.book.buttons;

import io.redspace.ironsjewelry.item.book.CyclicItemRenderer;
import io.redspace.ironsjewelry.item.book.GuideBookState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenAxis;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Function;

public class TextButton implements GuideBookButton {
    final int x, y, width, height;
    final int color, selectedColor;
    @Nullable
    final CyclicItemRenderer itemRenderer;
    final Component text;
    final Function<GuideBookState, Boolean> onClick;

    public TextButton(int x, int y, int width, int height, Component text, int color, int selectedColor, List<ItemStack> itemIcons, Function<GuideBookState, Boolean> onClick) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.color = color;
        this.selectedColor = selectedColor;
        this.text = text;
        if (itemIcons.isEmpty()) {
            itemRenderer = null;
        } else {
            itemRenderer = new CyclicItemRenderer(itemIcons);
        }
        this.onClick = onClick;
    }

    @Override
    public ScreenRectangle boundingBox() {
        return ScreenRectangle.of(ScreenAxis.HORIZONTAL, x, y, width, height);
    }

    @Override
    public void render(GuiGraphics guiGraphics, boolean selected, float partialTick) {
        int maxWidth = this.width;
        int textX = x;
        int middleY = y + height / 2;
        if (itemRenderer != null) {
            maxWidth -= 18;
            textX += 18;
            itemRenderer.renderBottomLeft(guiGraphics, x, middleY + 16 / 2, 1f);
        }
        var font = Minecraft.getInstance().font;
        int textWidth = font.width(text);
        float textScale = Math.clamp(maxWidth / (float) textWidth, 0, 1);
        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(textScale, textScale, textScale);
        float textYBottom = middleY + font.lineHeight / 2f + 2;
        guiGraphics.drawString(font, text, (int) (textX / textScale), (int) ((textYBottom) / textScale - font.lineHeight), selected ? selectedColor : color, selected);
        guiGraphics.pose().popPose();
    }

    @Override
    public boolean onClick(GuideBookState state) {
        return onClick.apply(state);
    }
}
