package io.redspace.ironsjewelry.client;

import net.minecraft.client.gui.GuiGraphicsExtractor;

/**
 * 1.21.1 bridge for rendering colored tooltip backgrounds the old fashioned way
 */
public final class TooltipRenderUtil {
    public static final int PADDING_LEFT = 3;
    public static final int PADDING_RIGHT = 3;
    public static final int PADDING_TOP = 3;
    public static final int PADDING_BOTTOM = 3;

    private TooltipRenderUtil() {
    }

    public static void renderTooltipBackground(
            GuiGraphicsExtractor guiGraphics,
            int x,
            int y,
            int width,
            int height,
            int backgroundTop,
            int backgroundBottom,
            int borderTop,
            int borderBottom
    ) {
        int backgroundX = x - PADDING_LEFT;
        int backgroundY = y - PADDING_TOP;
        int backgroundWidth = width + PADDING_LEFT + PADDING_RIGHT;
        int backgroundHeight = height + PADDING_TOP + PADDING_BOTTOM;

        renderHorizontalLine(guiGraphics, backgroundX, backgroundY - 1, backgroundWidth, backgroundTop);
        renderHorizontalLine(guiGraphics, backgroundX, backgroundY + backgroundHeight, backgroundWidth, backgroundBottom);
        renderRectangle(guiGraphics, backgroundX, backgroundY, backgroundWidth, backgroundHeight, backgroundTop, backgroundBottom);
        renderVerticalLineGradient(guiGraphics, backgroundX - 1, backgroundY, backgroundHeight, backgroundTop, backgroundBottom);
        renderVerticalLineGradient(guiGraphics, backgroundX + backgroundWidth, backgroundY, backgroundHeight, backgroundTop, backgroundBottom);
        renderFrameGradient(guiGraphics, backgroundX, backgroundY + 1, backgroundWidth, backgroundHeight, borderTop, borderBottom);
    }

    private static void renderFrameGradient(
            GuiGraphicsExtractor guiGraphics,
            int x,
            int y,
            int width,
            int height,
            int topColor,
            int bottomColor
    ) {
        renderVerticalLineGradient(guiGraphics, x, y, height - 2, topColor, bottomColor);
        renderVerticalLineGradient(guiGraphics, x + width - 1, y, height - 2, topColor, bottomColor);
        renderHorizontalLine(guiGraphics, x, y - 1, width, topColor);
        renderHorizontalLine(guiGraphics, x, y - 1 + height - 1, width, bottomColor);
    }

    private static void renderVerticalLineGradient(
            GuiGraphicsExtractor guiGraphics,
            int x,
            int y,
            int length,
            int topColor,
            int bottomColor
    ) {
        guiGraphics.fillGradient(x, y, x + 1, y + length, topColor, bottomColor);
    }

    private static void renderHorizontalLine(GuiGraphicsExtractor guiGraphics, int x, int y, int length, int color) {
        guiGraphics.fill(x, y, x + length, y + 1, color);
    }

    private static void renderRectangle(GuiGraphicsExtractor guiGraphics, int x, int y, int width, int height, int color, int colorTo) {
        guiGraphics.fillGradient(x, y, x + width, y + height, color, colorTo);
    }
}
