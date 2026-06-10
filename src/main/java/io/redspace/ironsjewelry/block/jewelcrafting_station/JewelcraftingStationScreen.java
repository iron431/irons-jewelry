package io.redspace.ironsjewelry.block.jewelcrafting_station;

import io.redspace.ironsjewelry.IronsJewelry;
import io.redspace.ironsjewelry.core.data.JewelryData;
import io.redspace.ironsjewelry.core.data.MaterialDefinition;
import io.redspace.ironsjewelry.core.data.PartDefinition;
import io.redspace.ironsjewelry.core.data.PartIngredient;
import io.redspace.ironsjewelry.core.data.PatternDefinition;
import io.redspace.ironsjewelry.core.data.PlayerData;
import io.redspace.ironsjewelry.event.SetupJewelcraftingResultEvent;
import io.redspace.ironsjewelry.item.CurioBaseItem;
import io.redspace.ironsjewelry.network.packets.SetJewelcraftingStationPattern;
import io.redspace.ironsjewelry.network.packets.SyncJewelcraftingSlotStates;
import io.redspace.ironsjewelry.registry.AssetHandlerRegistry;
import io.redspace.ironsjewelry.registry.IronsJewelryRegistries;
import io.redspace.ironsjewelry.utils.MinecraftInstanceHelper;
import io.redspace.ironsjewelry.utils.Utils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class JewelcraftingStationScreen extends AbstractContainerScreen<JewelcraftingStationMenu> implements ContainerListener {
    boolean tooltipDirty = true;
    public final List<Component> INFO_PAGE_CACHE = new ArrayList<>();

    public void handleSlotSync(SyncJewelcraftingSlotStates packet) {
        tooltipDirty = true;
        this.menu.handleClientSideSlotSync(packet.slotStates());
    }

    @Override
    public void slotChanged(AbstractContainerMenu pContainerToSend, int pDataSlotIndex, ItemStack pStack) {
        if (pDataSlotIndex <= 10) {
            tooltipDirty = true;
        }
    }

    @Override
    public void dataChanged(AbstractContainerMenu pContainerMenu, int pDataSlotIndex, int pValue) {

    }

    @Override
    public void removed() {
        super.removed();
        this.menu.removeSlotListener(this);
    }

    static class PatternButton extends Button {
        Holder<PatternDefinition> patternDefinition;

        public PatternButton(Holder<PatternDefinition> patternDefinition, int pX, int pY, int pWidth, int pHeight, OnPress pOnPress) {
            super(pX, pY, pWidth, pHeight, Component.empty(), pOnPress, DEFAULT_NARRATION);
            this.patternDefinition = patternDefinition;
        }

        @Override
        protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        }

        public void renderWidget(GuiGraphicsExtractor guiGraphics, boolean isHovering, boolean selected) {
            var sprite = isHovering ? RECIPE_SPRITE_HOVERING : selected ? RECIPE_SPRITE_SELECTED : RECIPE_SPRITE;
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, this.getX(), this.getY(), this.width, this.height);
            var parts = patternDefinition.value().partTemplate();
            for (PartIngredient part : parts) {
                guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, getMenuSprite(part.part(), selected || isHovering), this.getX() + 1, this.getY() + 1, 16, 16);
            }
        }
    }

    private static TextureAtlasSprite getMenuSprite(Holder<PartDefinition> partDefinition, boolean bright) {
        return AssetHandlerRegistry.JEWELRY_HANDLER.get().getSprite(AssetHandlerRegistry.JEWELRY_HANDLER.get().getMenuSpriteLocation(partDefinition, bright));
    }

    public static final Identifier BACKGROUND_TEXTURE = IronsJewelry.id("textures/gui/jewelcrafting_station.png");
    private static final Identifier SCROLLER_SPRITE = IronsJewelry.id("jewelcrafting_station/scroller");
    private static final Identifier SCROLLER_DISABLED_SPRITE = IronsJewelry.id("jewelcrafting_station/scroller_disabled");
    private static final Identifier RECIPE_SPRITE_SELECTED = IronsJewelry.id("jewelcrafting_station/recipe_selected");
    private static final Identifier RECIPE_SPRITE_HOVERING = IronsJewelry.id("jewelcrafting_station/recipe_highlighted");
    private static final Identifier RECIPE_SPRITE = IronsJewelry.id("jewelcrafting_station/recipe");
    private static final Identifier INPUT_SLOT = IronsJewelry.id("jewelcrafting_station/input_slot");
    private static final Identifier LORE_PAGE = IronsJewelry.id("jewelcrafting_station/lore_page");
    private static final int MAX_PATTERNS = 8;

    public int scrollOff;
    public int selectedPattern;
    public List<Holder<PatternDefinition>> availablePatterns;
    private List<PatternButton> patternButtons;

    public JewelcraftingStationScreen(JewelcraftingStationMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle, 206, 166);
        this.inventoryLabelX += menu.SCROLL_AREA_OFFSET;
        this.inventoryLabelY += 2;
        this.titleLabelY -= 2;
        this.selectedPattern = -1;
        this.scrollOff = 0;

        if (Minecraft.getInstance().player != null) {
            var registry = IronsJewelryRegistries.patternRegistry(pPlayerInventory.player.registryAccess());
            this.availablePatterns = Stream.concat(registry.stream().filter(PatternDefinition::unlockedByDefault).map(registry::wrapAsHolder),
                    PlayerData.get(pPlayerInventory.player).getLearnedPatterns().stream()).distinct().sorted(Comparator.comparingDouble(patternholder -> patternholder.value().qualityMultiplier())).toList();
        }
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
        leftPos -= 31 / 2;// recenter based on list addition
        patternButtons = new ArrayList<>();
        for (int i = 0; i < availablePatterns.size(); i++) {
            int index = i;
            patternButtons.add(this.addWidget(new PatternButton(availablePatterns.get(i), 0, 0, 18, 18, (button) -> {
                selectedPattern = index;
                ClientPacketDistributor.sendToServer(new SetJewelcraftingStationPattern(this.menu.containerId, availablePatterns.get(selectedPattern)));
            })));
        }
        positionPatternButtons();
        this.menu.addSlotListener(this);
    }

    @Override
    protected void extractTooltip(GuiGraphicsExtractor pGuiGraphics, int mouseX, int mouseY) {
        super.extractTooltip(pGuiGraphics, mouseX, mouseY);
        if (this.menu.getCarried().isEmpty()) {
            if (this.hoveredSlot == null) {
                for (PatternButton button : this.patternButtons) {
                    if (button.active && isHovering(mouseX, mouseY, button.getX(), button.getY(), button.getWidth(), button.getHeight())) {
                        pGuiGraphics.setTooltipForNextFrame(Utils.rasterizeComponentList(button.patternDefinition.value().getFullPatternTooltip()), mouseX, mouseY);
                        break;
                    }
                }
            } else if (Minecraft.getInstance().level != null) {
                if (!hoveredSlot.hasItem() && menu.isWorkspaceSlot(this.hoveredSlot)) {
                    int i = this.hoveredSlot.getSlotIndex();
                    if (selectedPattern >= 0) {
                        var pattern = availablePatterns.get(selectedPattern).value();
                        if (i < pattern.partTemplate().size()) {
                            var part = pattern.partTemplate().get(i);
                            List<Component> tooltip = new ArrayList<>();
                            tooltip.add(Component.translatable(part.part().value().descriptionId()).withStyle(ChatFormatting.YELLOW, ChatFormatting.UNDERLINE));
                            tooltip.add(Component.literal(String.format(" (0/%s)", part.materialCost())).withStyle(ChatFormatting.RED));
                            tooltip.add(Component.translatable("tooltip.irons_jewelry.applicable_materials").withStyle(ChatFormatting.YELLOW, ChatFormatting.UNDERLINE));
                            IronsJewelryRegistries.materialRegistry(Minecraft.getInstance().level.registryAccess()).listElements().filter(material -> !material.value().ingredient().isEmpty() && part.part().value().canUseMaterial(material))
                                    .forEach(material -> tooltip.add(Component.literal(" ").append(Component.translatable(material.value().descriptionId())).withStyle(ChatFormatting.GRAY)));
                            pGuiGraphics.setTooltipForNextFrame(Utils.rasterizeComponentList(tooltip), mouseX, mouseY);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void extractContents(@NotNull GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND_TEXTURE, leftPos, topPos, 0f, 0f, this.imageWidth, this.imageHeight, 256, 256);
        renderItemPreview(guiGraphics, partialTick, mouseX, mouseY);
        for (int i = 0; i < menu.workspaceSlots.size(); i++) {
            var slot = menu.workspaceSlots.get(i);
            if (!slot.isActive()) {
                break;
            }
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, INPUT_SLOT, leftPos + slot.x - 3, topPos + slot.y - 3, 22, 22);
            if (!slot.hasItem()) {
                if (selectedPattern >= 0) {
                    var pattern = availablePatterns.get(selectedPattern).value();
                    var parts = pattern.partTemplate();
                    if (i < parts.size()) {
                        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, getMenuSprite(parts.get(i).part(), false), leftPos + slot.x, topPos + slot.y, 16, 16);
                    }
                }
            }
        }

        super.extractContents(guiGraphics, mouseX, mouseY, partialTick);
        renderSidebar(guiGraphics, mouseX, mouseY);
        this.extractTooltip(guiGraphics, mouseX, mouseY);
    }


    private void renderItemPreview(GuiGraphicsExtractor guiGraphics, float pPartialTick, int pMouseX, int pMouseY) {
        List<Component> tooltip = new ArrayList<>();
        if (selectedPattern >= 0) {
            var holder = availablePatterns.get(selectedPattern);
            var pattern = holder.value();
            var parts = new HashMap<Holder<PartDefinition>, Holder<MaterialDefinition>>();
            var requiredIngredients = pattern.partTemplate();
            for (int i = 0; i < requiredIngredients.size(); i++) {
                var ingredient = requiredIngredients.get(i);
                var input = menu.workspaceSlots.get(i).getItem();
                var material = Utils.getMaterialForIngredient(Minecraft.getInstance().player.level.registryAccess(), input);
                if (material.isPresent() && ingredient.part().value().canUseMaterial(material.get())) {
                    parts.put(ingredient.part(), material.get());
                }
            }

            tooltip = CurioBaseItem.getShiftDescription(pattern, parts, Optional.of(menu.workspaceSlots.stream().map(slot -> slot.getItem().getCount()).toList()));
            tooltip.add(0, Component.translatable(pattern.descriptionId()).withStyle(ChatFormatting.UNDERLINE));
            int baseLines = tooltip.size();
            float scale = 3;
            int additionalLines = (int) (16 * scale / font.lineHeight) + 1;
            int topBuffer = 4;
            for (int i = 0; i < additionalLines; i++) {
                tooltip.add(Component.empty());
            }
            renderTooltipInternal(guiGraphics, this.font, tooltip, leftPos + imageWidth + 4, topPos + topBuffer);
            if (!parts.isEmpty()) {
                JewelryData jewelryData = JewelryData.renderable(holder, parts);
                ItemStack stack = new ItemStack(pattern.jewelryType().item());
                JewelryData.set(stack, jewelryData);
                var event = new SetupJewelcraftingResultEvent(holder, MinecraftInstanceHelper.getPlayer(), stack);
                if (NeoForge.EVENT_BUS.post(event).isCanceled()) {
                    stack = ItemStack.EMPTY;
                } else {
                    stack = event.getResult();
                }
                int width = 0;
                for (Component component : tooltip) {
                    int i = font.width(component.getString());
                    if (i > width) {
                        width = i;
                    }
                }
                var pose = guiGraphics.pose();
                pose.pushMatrix();
                float renderX = leftPos + imageWidth + width / 2f;
                float renderY = topPos + (baseLines + 1) * font.lineHeight + topBuffer + 4;
                pose.translate(renderX, renderY);
                pose.scale(scale, scale);
                guiGraphics.item(stack, -8, -8);
                pose.popMatrix();
            }
        }
        if (tooltipDirty) {
            INFO_PAGE_CACHE.clear();
            INFO_PAGE_CACHE.addAll(tooltip);
            tooltipDirty = false;
        }
    }

    private void renderTooltipInternal(GuiGraphicsExtractor guiGraphics, Font pFont, List<Component> components, int x, int y) {
        if (!components.isEmpty()) {
            int maxWidth = 0;
            for (Component component : components) {
                int w = pFont.width(component);
                if (w > maxWidth) {
                    maxWidth = w;
                }
            }
            int totalHeight = 0;
            for (int i = 0; i < components.size(); i++) {
                totalHeight += pFont.lineHeight + (i == 0 && components.size() > 1 ? 2 : 0);
            }

            int bgColor = 0xb4260f0c;
            int borderColor = 0x50e0ca9f;

            guiGraphics.fill(x - 3, y - 4, x + maxWidth + 3, y + totalHeight + 4, bgColor);
            guiGraphics.fill(x - 3, y - 4, x + maxWidth + 3, y - 3, borderColor);
            guiGraphics.fill(x - 3, y + totalHeight + 3, x + maxWidth + 3, y + totalHeight + 4, borderColor);
            guiGraphics.fill(x - 4, y - 3, x - 3, y + totalHeight + 3, borderColor);
            guiGraphics.fill(x + maxWidth + 3, y - 3, x + maxWidth + 4, y + totalHeight + 3, borderColor);

            int currentY = y;
            for (int i = 0; i < components.size(); i++) {
                guiGraphics.text(pFont, components.get(i), x, currentY, -1, true);
                currentY += pFont.lineHeight + (i == 0 ? 2 : 0);
            }
        }
    }


    private boolean isHovering(int mouseX, int mouseY, int xmin, int ymin, int width, int height) {
        return mouseX > xmin && mouseX < xmin + width && mouseY > ymin && mouseY < ymin + height;
    }

    private int getMaterialCount(int index, Holder<PartDefinition> forPart) {
        if (index >= 0 && index < menu.workspaceSlots.size()) {
            var slot = menu.workspaceSlots.get(index);
            if (slot.isActive()) {
                var stack = slot.getItem();
                var material = Utils.getMaterialForIngredient(Minecraft.getInstance().level.registryAccess(), stack);
                if (material.isPresent() && forPart.value().canUseMaterial(material.get())) {
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

    private void renderSidebar(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
        int barX = leftPos + SCROLL_BAR_X_OFFSET;
        int y = topPos + SCROLL_BAR_Y_OFFSET;
        for (int i = scrollOff; i < patternButtons.size() && i < MAX_PATTERNS + scrollOff; i++) {
            var button = patternButtons.get(i);
            button.renderWidget(guiGraphics, isHovering(mouseX, mouseY, button.getX(), button.getY(), button.getWidth(), button.getHeight()), i == selectedPattern);
        }

        int i = availablePatterns.size() + 1 - MAX_PATTERNS;
        if (i > 1) {
            var i1 = getCurrentScrollBarYOffset(i);
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, SCROLLER_SPRITE, barX, y + i1, 6, 27);
        } else {
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, SCROLLER_DISABLED_SPRITE, barX, y, 6, 27);
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

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
        double pMouseY = event.y();
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
            return super.mouseDragged(event, dx, dy);
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double pMouseX = event.x();
        double pMouseY = event.y();
        this.isDragging = this.canScroll(this.availablePatterns.size())
                && isHovering((int) pMouseX, (int) pMouseY,
                leftPos + SCROLL_BAR_X_OFFSET,
                topPos + SCROLL_BAR_Y_OFFSET + getCurrentScrollBarYOffset(availablePatterns.size() + 1 - MAX_PATTERNS),
                SCROLL_BAR_WIDTH,
                SCROLL_BAR_HEIGHT
        );

        return super.mouseClicked(event, doubleClick);
    }

    @Override
    protected void handleSlotStateChanged(int pSlotId, int pContainerId, boolean pNewState) {
        super.handleSlotStateChanged(pSlotId, pContainerId, pNewState);
    }
}
