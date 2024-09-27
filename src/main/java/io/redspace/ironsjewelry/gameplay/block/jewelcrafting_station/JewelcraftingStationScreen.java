package io.redspace.ironsjewelry.gameplay.block.jewelcrafting_station;

import io.redspace.ironsjewelry.IronsJewelry;
import io.redspace.ironsjewelry.core.data.PatternDefinition;
import io.redspace.ironsjewelry.core.data_registry.PatternDataHandler;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class JewelcraftingStationScreen extends AbstractContainerScreen<JewelcraftingStationMenu> {
    static class PatternButton extends Button/*ImageButton*/ {
        public PatternButton(int pX, int pY, int pWidth, int pHeight, OnPress pOnPress) {
            super(pX, pY, pWidth, pHeight, Component.empty(), pOnPress, DEFAULT_NARRATION);
        }

        public void renderWidget(GuiGraphics pGuiGraphics, boolean isHovering, boolean selected) {
            var sprite = isHovering ? RECIPE_SPRITE_HOVERING : selected ? RECIPE_SPRITE_SELECTED : RECIPE_SPRITE;
            pGuiGraphics.blitSprite(sprite, this.getX(), this.getY(), this.width, this.height);
        }
    }

    public static final ResourceLocation BACKGROUND_TEXTURE = IronsJewelry.id("textures/gui/jewelcrafting_station.png");
    private static final ResourceLocation SCROLLER_SPRITE = IronsJewelry.id("jewelcrafting_station/scroller");
    private static final ResourceLocation SCROLLER_DISABLED_SPRITE = IronsJewelry.id("jewelcrafting_station/scroller_disabled");
    private static final ResourceLocation RECIPE_SPRITE_SELECTED = IronsJewelry.id("jewelcrafting_station/recipe_selected");
    private static final ResourceLocation RECIPE_SPRITE_HOVERING = IronsJewelry.id("jewelcrafting_station/recipe_highlighted");
    private static final ResourceLocation RECIPE_SPRITE = IronsJewelry.id("jewelcrafting_station/recipe");

    int scrollOff;
    int selectedPattern;
    List<PatternDefinition> availablePatterns;
    List<PatternButton> patternButtons;

    public JewelcraftingStationScreen(JewelcraftingStationMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
        this.imageWidth = 206;

    }

    private void positionPatternButtons() {
        int maxPatterns = 7;
        int x = leftPos + 5;
        int y = topPos + 18;
        for (int i = 0; i < patternButtons.size(); i++) {
            patternButtons.get(i).setPosition(x, y + (scrollOff + i) * 18);
            patternButtons.get(i).active = (i - scrollOff) >= 0 && (i - scrollOff) < maxPatterns;
        }
    }

    @Override
    protected void init() {
        super.init();
        this.selectedPattern = -1;
        this.scrollOff = 0;

        this.availablePatterns = PatternDataHandler.patterns().stream().filter(PatternDefinition::unlockedByDefault).toList();
        patternButtons = new ArrayList<>();
        for (int i = 0; i < availablePatterns.size(); i++) {
            int index = i;
            patternButtons.add(this.addRenderableWidget(new PatternButton(0, 0, 18, 18, (button) -> {
                IronsJewelry.LOGGER.debug("pattern button pressed: {}", index);
                selectedPattern = index;
            })));
        }
        positionPatternButtons();
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
        for (int i = scrollOff; i < patternButtons.size() && i < maxPatterns; i++) {
            var button = patternButtons.get(i);
            button.renderWidget(guiGraphics, isHovering(mouseX, mouseY, button.getX(), button.getY(), button.getWidth(), button.getHeight()), i == selectedPattern);
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