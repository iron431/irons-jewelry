package io.redspace.ironsjewelry.gameplay.block.jewelcrafting_station;

import io.redspace.ironsjewelry.IronsJewelry;
import io.redspace.ironsjewelry.core.data.PatternDefinition;
import io.redspace.ironsjewelry.core.data_registry.PatternDataHandler;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class JewelcraftingStationScreen extends AbstractContainerScreen<JewelcraftingStationMenu> {
    public static final ResourceLocation BACKGROUND_TEXTURE = IronsJewelry.id("textures/gui/jewelcrafting_station.png");
    private static final ResourceLocation SCROLLER_SPRITE = IronsJewelry.id("jewelcrafting_station/scroller");
    private static final ResourceLocation SCROLLER_DISABLED_SPRITE = IronsJewelry.id("jewelcrafting_station/scroller_disabled");
    private static final ResourceLocation RECIPE_SPRITE_SELECTED = IronsJewelry.id("jewelcrafting_station/recipe_selected");
    private static final ResourceLocation RECIPE_SPRITE_HOVERING = IronsJewelry.id("jewelcrafting_station/recipe_highlighted");
    private static final ResourceLocation RECIPE_SPRITE = IronsJewelry.id("jewelcrafting_station/recipe");

    int scrollOff;
    List<PatternDefinition> availablePatterns;

    public JewelcraftingStationScreen(JewelcraftingStationMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
        this.imageWidth = 206;
        this.availablePatterns = PatternDataHandler.patterns().stream().filter(PatternDefinition::unlockedByDefault).toList();
    }

    @Override
    protected void renderBg(GuiGraphics pGuiGraphics, float pPartialTick, int pMouseX, int pMouseY) {
        int i = this.leftPos;
        int j = this.topPos;
        pGuiGraphics.blit(BACKGROUND_TEXTURE, i, j, 0, 0, this.imageWidth, this.imageHeight);
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderSidebar(guiGraphics, mouseX, mouseY);


        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    private boolean isHovering(int mouseX, int mouseY, int xmin, int ymin, int width, int height) {
        return mouseX > xmin && mouseX < xmin + width && mouseY > ymin && mouseY < ymin + height;
    }

    private void renderSidebar(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int barX = leftPos + 24;
        int iconX = leftPos + 5;
        int y = topPos + 18;
        int maxPatterns = 7;
        for (int i = scrollOff; i < availablePatterns.size() && i < maxPatterns; i++) {
            int iy = y + i * 18;
            var sprite = isHovering(mouseX, mouseY, iconX, iy, 18, 18) ? RECIPE_SPRITE_HOVERING : RECIPE_SPRITE;
            guiGraphics.blitSprite(sprite, iconX, iy, 0, 18, 18);
        }

        int i = availablePatterns.size() + 1 - maxPatterns;
        if (i > 1) {
            int j = 139 - (27 + (i - 1) * 139 / i);
            int k = 1 + j / i + 139 / i;
            int l = 113;
            int i1 = Math.min(113, this.scrollOff * k);
            if (this.scrollOff == i - 1) {
                i1 = 113;
            }

            guiGraphics.blitSprite(SCROLLER_SPRITE, barX, y + i1, 0, 6, 27);
        } else {
            guiGraphics.blitSprite(SCROLLER_DISABLED_SPRITE, barX, y, 0, 6, 27);
        }
    }
}
