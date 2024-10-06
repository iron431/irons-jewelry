package io.redspace.ironsjewelry.gameplay.block.jewelcrafting_station;

import io.redspace.ironsjewelry.IronsJewelry;
import io.redspace.ironsjewelry.client.DynamicModel;
import io.redspace.ironsjewelry.core.IBonusParameterType;
import io.redspace.ironsjewelry.core.data.BonusSource;
import io.redspace.ironsjewelry.core.data.PartDefinition;
import io.redspace.ironsjewelry.core.data.PartIngredient;
import io.redspace.ironsjewelry.core.data.PatternDefinition;
import io.redspace.ironsjewelry.core.data_registry.MaterialDataHandler;
import io.redspace.ironsjewelry.core.data_registry.PatternDataHandler;
import io.redspace.ironsjewelry.network.packets.SetJewelcraftingStationPattern;
import io.redspace.ironsjewelry.network.packets.SyncJewelcraftingSlotStates;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.InventoryMenu;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public class JewelcraftingStationScreen extends AbstractContainerScreen<JewelcraftingStationMenu> {
    public void handleSlotSync(SyncJewelcraftingSlotStates packet) {
        this.menu.handleClientSideSlotSync(packet.slotStates());
    }

    static class PatternButton extends Button {
        PatternDefinition patternDefinition;

        public PatternButton(PatternDefinition patternDefinition, int pX, int pY, int pWidth, int pHeight, OnPress pOnPress) {
            super(pX, pY, pWidth, pHeight, Component.empty(), pOnPress, DEFAULT_NARRATION);
            this.patternDefinition = patternDefinition;
        }

        public void renderWidget(GuiGraphics guiGraphics, boolean isHovering, boolean selected) {
            var sprite = isHovering ? RECIPE_SPRITE_HOVERING : selected ? RECIPE_SPRITE_SELECTED : RECIPE_SPRITE;
            guiGraphics.blitSprite(sprite, this.getX(), this.getY(), this.width, this.height);
            var parts = patternDefinition.partTemplate();
            for (PartIngredient part : parts) {
                guiGraphics.blit(this.getX() + 1, this.getY() + 1, 0, 16, 16, getMenuSprite(part.part(), selected || isHovering));
            }
        }
    }

    private static TextureAtlasSprite getMenuSprite(PartDefinition partDefinition, boolean bright) {
        return Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(DynamicModel.atlasResourceLocaction(partDefinition, bright ? "menu_bright" : "menu"));
    }

    public static final ResourceLocation BACKGROUND_TEXTURE = IronsJewelry.id("textures/gui/jewelcrafting_station.png");
    private static final ResourceLocation SCROLLER_SPRITE = IronsJewelry.id("jewelcrafting_station/scroller");
    private static final ResourceLocation SCROLLER_DISABLED_SPRITE = IronsJewelry.id("jewelcrafting_station/scroller_disabled");
    private static final ResourceLocation RECIPE_SPRITE_SELECTED = IronsJewelry.id("jewelcrafting_station/recipe_selected");
    private static final ResourceLocation RECIPE_SPRITE_HOVERING = IronsJewelry.id("jewelcrafting_station/recipe_highlighted");
    private static final ResourceLocation RECIPE_SPRITE = IronsJewelry.id("jewelcrafting_station/recipe");
    private static final ResourceLocation INPUT_SLOT = IronsJewelry.id("jewelcrafting_station/input_slot");
    private static final ResourceLocation LORE_PAGE = IronsJewelry.id("jewelcrafting_station/lore_page");
    private static final int MAX_PATTERNS = 8;

    int scrollOff;
    int selectedPattern;
    List<PatternDefinition> availablePatterns;
    List<PatternButton> patternButtons;

    public JewelcraftingStationScreen(JewelcraftingStationMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
        this.imageWidth = 206;
        this.inventoryLabelX += menu.SCROLL_AREA_OFFSET;
        this.inventoryLabelY += 2;
        this.titleLabelY -= 2;
        this.selectedPattern = -1;
        this.scrollOff = 0;

        this.availablePatterns = PatternDataHandler.patterns().stream().filter(PatternDefinition::unlockedByDefault).toList();

    }


    private void positionPatternButtons() {
        int x = leftPos + 5;
        int y = topPos + SCROLL_BAR_Y_OFFSET;
        for (int i = 0; i < patternButtons.size(); i++) {
            patternButtons.get(i).setPosition(x, y + (-scrollOff + i) * 18);
            patternButtons.get(i).active = (i - scrollOff) >= 0 && (i - scrollOff) < MAX_PATTERNS;
        }
    }

    @Override
    protected void init() {
        super.init();
        patternButtons = new ArrayList<>();
        for (int i = 0; i < availablePatterns.size(); i++) {
            int index = i;
            patternButtons.add(this.addWidget(new PatternButton(availablePatterns.get(i), 0, 0, 18, 18, (button) -> {
                IronsJewelry.LOGGER.debug("pattern button pressed: {}", index);
                selectedPattern = index;
                PacketDistributor.sendToServer(new SetJewelcraftingStationPattern(this.menu.containerId, availablePatterns.get(selectedPattern)));
            })));
        }
        positionPatternButtons();
    }

    @Override
    protected void renderTooltip(GuiGraphics pGuiGraphics, int mouseX, int mouseY) {
        super.renderTooltip(pGuiGraphics, mouseX, mouseY);
        if (this.menu.getCarried().isEmpty() && this.hoveredSlot == null) {
            for (PatternButton button : this.patternButtons) {
                if (button.active && isHovering(mouseX, mouseY, button.getX(), button.getY(), button.getWidth(), button.getHeight())) {
                    pGuiGraphics.renderTooltip(this.font, button.patternDefinition.getFullPatternTooltip().stream().map(component -> FormattedCharSequence.forward(component.getString(), component.getStyle())).toList(), mouseX, mouseY);
                    break;
                }
            }
        }
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float pPartialTick, int pMouseX, int pMouseY) {
        guiGraphics.blit(BACKGROUND_TEXTURE, leftPos, topPos, 0, 0, this.imageWidth, this.imageHeight);
        for (int i = 0; i < menu.workspaceSlots.size(); i++) {
            var slot = menu.workspaceSlots.get(i);
            if (!slot.isActive()) {
                break;
            }
            guiGraphics.blitSprite(INPUT_SLOT, leftPos + slot.x - 3, topPos + slot.y - 3, 22, 22);
            if (!slot.hasItem()) {
                if (selectedPattern >= 0) {
                    var pattern = availablePatterns.get(selectedPattern);
                    var parts = pattern.partTemplate();
                    if (i < parts.size()) {
                        guiGraphics.blit(leftPos + slot.x, topPos + slot.y, 0, 16, 16, getMenuSprite(parts.get(i).part(), false));
                    }
                }
            }
        }
        //todo: lore page
        //guiGraphics.blitSprite(LORE_PAGE, leftPos + imageWidth, topPos, 80, 165);
    }


    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderSidebar(guiGraphics, mouseX, mouseY);

        renderLorePage(guiGraphics, mouseX, mouseY);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    private boolean isHovering(int mouseX, int mouseY, int xmin, int ymin, int width, int height) {
        return mouseX > xmin && mouseX < xmin + width && mouseY > ymin && mouseY < ymin + height;
    }

    private void renderLorePage(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (true) {
            return;
        }
        if (selectedPattern < 0) {
            return;
        }
        /*
        Example page:

        Gemset Ring
        Band (0/4)
        - Empty
        Gem (1/1)
         - Diamond: Max Health

         Bonus:
         +2 Max Health
         */
        var pattern = availablePatterns.get(selectedPattern);
        int lorePageWidth = 80;
        int lineHeight = font.lineHeight;
        AtomicInteger lineY = new AtomicInteger(3);
        int maxWidth = lorePageWidth - 15;
        int leftMargin = leftPos + imageWidth + 2;
        var title = Component.translatable(pattern.getDescriptionId()).withStyle(ChatFormatting.UNDERLINE);
        font.split(title, maxWidth).forEach(text -> guiGraphics.drawCenteredString(font, text, leftPos + imageWidth + lorePageWidth / 2, topPos + lineY.getAndAdd(lineHeight), 0xFFFFFF));
        lineY.addAndGet(lineHeight / 2);
        for (int i = 0; i < pattern.partTemplate().size(); i++) {
            List<Component> bonusEntries = new ArrayList<>();
            Optional<Component> qualityEntry = Optional.empty();
            var part = pattern.partTemplate().get(i);
            String materialOrEmptyKey = "tooltip.irons_jewelry.empty";
            if (i < menu.workspaceSlots.size()) {
                var slot = menu.workspaceSlots.get(i);
                if (slot.isActive()) {
                    var stack = slot.getItem();
                    var material = MaterialDataHandler.getMaterialForIngredient(stack);
                    if (material.isPresent() && part.part().canUseMaterial(material.get().materialType())) {
                        materialOrEmptyKey = material.get().getDescriptionId();
                        //is for bonus?
                        var bonusForPart = pattern.bonuses().stream().filter(source -> source.parameterOrSource().right().isPresent() && source.parameterOrSource().right().get().equals(part.part())).toList();
                        for (BonusSource source : bonusForPart) {
                            IBonusParameterType type = source.bonus().getParameterType();
                            var value = type.resolve(material.get().bonusParameters());
                            if (value.isPresent()) {
                                Optional<String> string = type.getValueDescriptionId(value.get());
                                if (string.isPresent()) {
                                    bonusEntries.add(Component.literal(" ").append(Component.translatable("tooltip.irons_jewelry.bonus_to_source", Component.translatable(source.bonus().getDescriptionId()), Component.translatable(string.get()))));
                                }
                            }
                        }
                        //is for quality?
                        var qualityForPart = pattern.bonuses().stream().filter(source -> source.qualityOrSource().right().isPresent() && source.qualityOrSource().right().get().equals(part.part())).findFirst();
                        if (qualityForPart.isPresent()) {
                            qualityEntry = Optional.of(Component.literal(" ").append(Component.translatable("tooltip.irons_jewelry.quality_to_source", material.get().quality())));
                        }
                    }
                }
            }
            int current = getMaterialCount(i, part.part());
            int cost = part.materialCost();
            String counter = String.format("(%s/%s)", current, cost);
            var partHeader = Component.translatable(part.part().getDescriptionId()).append(": ").append(Component.translatable(materialOrEmptyKey)).append(" ").append(Component.literal(counter).withStyle(current >= cost ? ChatFormatting.GREEN : ChatFormatting.RED));
            //var partEntry = Component.literal("- ").append(Component.translatable(materialOrEmptyKey));

            guiGraphics.drawString(font, partHeader, leftMargin, topPos + lineY.getAndAdd(lineHeight), 0xFFFFFF);
            //guiGraphics.drawString(font, partEntry, leftMargin, topPos + lineY.getAndAdd(lineHeight), 0xFFFFFF);
            bonusEntries.forEach(component -> guiGraphics.drawString(font, component, leftMargin, topPos + lineY.getAndAdd(lineHeight), 0xFFFFFF));
            qualityEntry.ifPresent(component -> guiGraphics.drawString(font, component, leftMargin, topPos + lineY.getAndAdd(lineHeight), 0xFFFFFF));
        }
    }

    private int getMaterialCount(int index, PartDefinition forPart) {
        if (index >= 0 && index < menu.workspaceSlots.size()) {
            var slot = menu.workspaceSlots.get(index);
            if (slot.isActive()) {
                var stack = slot.getItem();
                var material = MaterialDataHandler.getMaterialForIngredient(stack);
                if (material.isPresent() && forPart.canUseMaterial(material.get().materialType())) {
                    return stack.getCount();
                }
            }
        }
        return 0;
    }

    private static final int SCROLL_BAR_X_OFFSET = 24;
    private static final int SCROLL_BAR_Y_OFFSET = 14;
    private static final int SCROLL_BAR_WIDTH = 6;
    private static final int SCROLL_BAR_HEIGHT = 27;
    private static final int SCROLL_BAR_CHANNEL_LENGTH = 144;

    private void renderSidebar(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int barX = leftPos + SCROLL_BAR_X_OFFSET;
        int y = topPos + SCROLL_BAR_Y_OFFSET;
        for (int i = scrollOff; i < patternButtons.size() && i < MAX_PATTERNS + scrollOff; i++) {
            var button = patternButtons.get(i);
            button.renderWidget(guiGraphics, isHovering(mouseX, mouseY, button.getX(), button.getY(), button.getWidth(), button.getHeight()), i == selectedPattern);
        }

        int i = availablePatterns.size() + 1 - MAX_PATTERNS;
        if (i > 1) {
            var i1 = getCurrentScrollBarYOffset(i);
            guiGraphics.blitSprite(SCROLLER_SPRITE, barX, y + i1, 0, 6, 27);
        } else {
            guiGraphics.blitSprite(SCROLLER_DISABLED_SPRITE, barX, y, 0, 6, 27);
        }
    }

    private int getCurrentScrollBarYOffset(int patternsPastMaxPatterns) {
        int j = SCROLL_BAR_CHANNEL_LENGTH - (SCROLL_BAR_HEIGHT + (patternsPastMaxPatterns - 1) * 139 / patternsPastMaxPatterns);
        int k = 1 + j / patternsPastMaxPatterns + SCROLL_BAR_CHANNEL_LENGTH / patternsPastMaxPatterns;
        int l = SCROLL_BAR_CHANNEL_LENGTH - SCROLL_BAR_HEIGHT;
        int i1 = Math.min(l, this.scrollOff * k);
        if (this.scrollOff == patternsPastMaxPatterns - 1) {
            i1 = l;
        }
        return i1;
    }

    private boolean canScroll(int count) {
        return count > MAX_PATTERNS;
    }

    private boolean isDragging;

    @Override
    public boolean mouseScrolled(double pMouseX, double pMouseY, double pScrollX, double pScrollY) {
        int i = this.availablePatterns.size();
        if (this.canScroll(i)) {
            int j = i - MAX_PATTERNS;
            this.scrollOff = Mth.clamp((int) ((double) this.scrollOff - pScrollY), 0, j);
            positionPatternButtons();
        }

        return true;
    }

    /**
     * Called when the mouse is dragged within the GUI element.
     * <p>
     *
     * @param pMouseX the X coordinate of the mouse.
     * @param pMouseY the Y coordinate of the mouse.
     * @param pButton the button that is being dragged.
     * @param pDragX  the X distance of the drag.
     * @param pDragY  the Y distance of the drag.
     * @return {@code true} if the event is consumed, {@code false} otherwise.
     */
    @Override
    public boolean mouseDragged(double pMouseX, double pMouseY, int pButton, double pDragX, double pDragY) {
        int i = this.availablePatterns.size();
        if (this.isDragging) {
            int j = this.topPos + 18;
            int k = j + 139;
            int l = i - MAX_PATTERNS;
            float f = ((float) pMouseY - (float) j - 13.5F) / ((float) (k - j) - 27.0F);
            f = f * (float) l + 0.5F;
            this.scrollOff = Mth.clamp((int) f, 0, l);
            positionPatternButtons();
            return true;
        } else {
            return super.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY);
        }
    }

    /**
     * Called when a mouse button is clicked within the GUI element.
     * <p>
     *
     * @param pMouseX the X coordinate of the mouse.
     * @param pMouseY the Y coordinate of the mouse.
     * @param pButton the button that was clicked.
     * @return {@code true} if the event is consumed, {@code false} otherwise.
     */
    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        this.isDragging = this.canScroll(this.availablePatterns.size())
                && isHovering((int) pMouseX, (int) pMouseY,
                leftPos + SCROLL_BAR_X_OFFSET,
                topPos + SCROLL_BAR_Y_OFFSET + getCurrentScrollBarYOffset(availablePatterns.size() + 1 - MAX_PATTERNS),
                SCROLL_BAR_WIDTH,
                SCROLL_BAR_HEIGHT
        );

        return super.mouseClicked(pMouseX, pMouseY, pButton);
    }
}
