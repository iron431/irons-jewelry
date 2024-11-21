package io.redspace.ironsjewelry.block.jewelcrafting_station;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.math.Axis;
import io.redspace.ironsjewelry.IronsJewelry;
import io.redspace.ironsjewelry.client.DynamicModel;
import io.redspace.ironsjewelry.core.MinecraftInstanceHelper;
import io.redspace.ironsjewelry.core.Utils;
import io.redspace.ironsjewelry.core.data.*;
import io.redspace.ironsjewelry.event.SetupJewelcraftingResultEvent;
import io.redspace.ironsjewelry.item.CurioBaseItem;
import io.redspace.ironsjewelry.network.packets.SetJewelcraftingStationPattern;
import io.redspace.ironsjewelry.network.packets.SyncJewelcraftingSlotStates;
import io.redspace.ironsjewelry.registry.ComponentRegistry;
import io.redspace.ironsjewelry.registry.IronsJewelryRegistries;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.TooltipRenderUtil;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Stream;

public class JewelcraftingStationScreen extends AbstractContainerScreen<JewelcraftingStationMenu> {
    public void handleSlotSync(SyncJewelcraftingSlotStates packet) {
        this.menu.handleClientSideSlotSync(packet.slotStates());
    }

    static class PatternButton extends Button {
        Holder<PatternDefinition> patternDefinition;

        public PatternButton(Holder<PatternDefinition> patternDefinition, int pX, int pY, int pWidth, int pHeight, OnPress pOnPress) {
            super(pX, pY, pWidth, pHeight, Component.empty(), pOnPress, DEFAULT_NARRATION);
            this.patternDefinition = patternDefinition;
        }

        public void renderWidget(GuiGraphics guiGraphics, boolean isHovering, boolean selected) {
            var sprite = isHovering ? RECIPE_SPRITE_HOVERING : selected ? RECIPE_SPRITE_SELECTED : RECIPE_SPRITE;
            guiGraphics.blitSprite(sprite, this.getX(), this.getY(), this.width, this.height);
            var parts = patternDefinition.value().partTemplate();
            for (PartIngredient part : parts) {
                guiGraphics.blit(this.getX() + 1, this.getY() + 1, 0, 16, 16, getMenuSprite(part.part(), selected || isHovering));
            }
        }
    }

    private static TextureAtlasSprite getMenuSprite(Holder<PartDefinition> partDefinition, boolean bright) {
        return IronsJewelry.JEWELRY_ATLAS.getSprite(DynamicModel.atlasResourceLocaction(partDefinition, bright ? "menu_bright" : "menu"));
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
    List<Holder<PatternDefinition>> availablePatterns;
    List<PatternButton> patternButtons;

    public JewelcraftingStationScreen(JewelcraftingStationMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
        this.imageWidth = 206;
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
        leftPos -= 31/2;// recenter based on list addition
        patternButtons = new ArrayList<>();
        for (int i = 0; i < availablePatterns.size(); i++) {
            int index = i;
            patternButtons.add(this.addWidget(new PatternButton(availablePatterns.get(i), 0, 0, 18, 18, (button) -> {
                selectedPattern = index;
                PacketDistributor.sendToServer(new SetJewelcraftingStationPattern(this.menu.containerId, availablePatterns.get(selectedPattern)));
            })));
        }
        positionPatternButtons();
    }

    @Override
    protected void renderTooltip(GuiGraphics pGuiGraphics, int mouseX, int mouseY) {
        super.renderTooltip(pGuiGraphics, mouseX, mouseY);
        if (this.menu.getCarried().isEmpty()) {
            if (this.hoveredSlot == null) {
                for (PatternButton button : this.patternButtons) {
                    if (button.active && isHovering(mouseX, mouseY, button.getX(), button.getY(), button.getWidth(), button.getHeight())) {
                        pGuiGraphics.renderTooltip(this.font, Utils.rasterizeComponentList(button.patternDefinition.value().getFullPatternTooltip()), mouseX, mouseY);
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
                            IronsJewelryRegistries.materialRegistry(Minecraft.getInstance().level.registryAccess()).stream().filter(materialDefinition -> !materialDefinition.ingredient().hasNoItems() && part.part().value().canUseMaterial(materialDefinition.materialType()))
                                    .forEach(material -> tooltip.add(Component.literal(" ").append(Component.translatable(material.descriptionId())).withStyle(ChatFormatting.GRAY)));
                            pGuiGraphics.renderTooltip(this.font, Utils.rasterizeComponentList(tooltip), mouseX, mouseY);
                        }
                    }
                }
            }
        }
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float pPartialTick, int pMouseX, int pMouseY) {
        guiGraphics.blit(BACKGROUND_TEXTURE, leftPos, topPos, 0, 0, this.imageWidth, this.imageHeight);
        renderItemPreview(guiGraphics, pPartialTick, pMouseX, pMouseY);
        for (int i = 0; i < menu.workspaceSlots.size(); i++) {
            var slot = menu.workspaceSlots.get(i);
            if (!slot.isActive()) {
                break;
            }
            guiGraphics.blitSprite(INPUT_SLOT, leftPos + slot.x - 3, topPos + slot.y - 3, 200, 22, 22);
            if (!slot.hasItem()) {
                if (selectedPattern >= 0) {
                    var pattern = availablePatterns.get(selectedPattern).value();
                    var parts = pattern.partTemplate();
                    if (i < parts.size()) {
                        guiGraphics.blit(leftPos + slot.x, topPos + slot.y, 200, 16, 16, getMenuSprite(parts.get(i).part(), false));
                    }
                }
            }
        }
    }

    private void renderItemPreview(GuiGraphics guiGraphics, float pPartialTick, int pMouseX, int pMouseY) {
        if (selectedPattern >= 0) {
            var holder = availablePatterns.get(selectedPattern);
            var pattern = holder.value();
            var parts = new HashMap<Holder<PartDefinition>, Holder<MaterialDefinition>>();
            var requiredIngredients = pattern.partTemplate();
            for (int i = 0; i < requiredIngredients.size(); i++) {
                var ingredient = requiredIngredients.get(i);
                var input = menu.workspaceSlots.get(i).getItem();
                var material = Utils.getMaterialForIngredient(Minecraft.getInstance().player.level.registryAccess(), input);
                if (material.isPresent() && ingredient.part().value().canUseMaterial(material.get().value().materialType())) {
                    parts.put(ingredient.part(), material.get());
                    //var texture = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(DynamicModel.atlasResourceLocaction(ingredient.part(), material.get().value().paletteLocation().getPath()));
                }
            }

            var tooltip = CurioBaseItem.getShiftDescription(pattern, parts, Optional.of(menu.workspaceSlots.stream().map(slot -> slot.getItem().getCount()).toList()));
            tooltip.add(0, Component.translatable(pattern.descriptionId()).withStyle(ChatFormatting.UNDERLINE));
            int baseLines = tooltip.size();
            float scale = 3;
            int additionalLines = (int) (16 * scale / font.lineHeight) + 1;
            int topBuffer = 4;
            for (int i = 0; i < additionalLines; i++) {
                tooltip.add(Component.empty());
            }
            renderTooltipInternal(guiGraphics, this.font, tooltip, leftPos + imageWidth + 4, topPos + topBuffer);
            if (parts.isEmpty()) {
                return;
            }

            JewelryData jewelryData = JewelryData.renderable(holder, parts);
            ItemStack stack = new ItemStack(pattern.jewelryType().item());
            stack.set(ComponentRegistry.JEWELRY_COMPONENT, jewelryData);
            //Event posting
            var event = new SetupJewelcraftingResultEvent(holder, MinecraftInstanceHelper.getPlayer(), stack);
            if (NeoForge.EVENT_BUS.post(event).isCanceled()) {
                stack = ItemStack.EMPTY;
            } else {
                stack = event.getResult();
            }
            var pose = guiGraphics.pose();
            int width = 0;
            for (Component component : tooltip) {
                int i = font.width(component.getString());
                if (i > width) {
                    width = i;
                }
            }
            pose.pushPose();
            //pose.translate(leftPos + 61 + 95 / 2f, topPos + 13 + 60 / 2f, 100);
            pose.translate(leftPos + imageWidth + width / 2f/* + 9 * scale + 16*/, topPos + 8 * scale + (baseLines + 1) * font.lineHeight + topBuffer + 4, 100);
            pose.scale(16 * scale, -16 * scale, 16 * scale);
            pose.mulPose(Axis.YP.rotationDegrees((Minecraft.getInstance().player.tickCount + pPartialTick) * 1.25f));
            Lighting.setupForFlatItems();
            Minecraft.getInstance().getItemRenderer().renderStatic(stack, ItemDisplayContext.GUI, LightTexture.FULL_BLOCK, OverlayTexture.NO_OVERLAY, pose, guiGraphics.bufferSource(), null, 0);
            guiGraphics.flush();
            pose.popPose();
        }
    }

    private void renderTooltipInternal(GuiGraphics guiGraphics, Font pFont, List<Component> components, int x, int y) {
        if (!components.isEmpty()) {
            int i = 0;
            int j = components.size() == 1 ? -2 : 0;
            var pComponents = components.stream().map(c -> ClientTooltipComponent.create(c.getVisualOrderText())).toList();
            for (ClientTooltipComponent clienttooltipcomponent : pComponents) {
                int k = clienttooltipcomponent.getWidth(pFont);
                if (k > i) {
                    i = k;
                }
                j += clienttooltipcomponent.getHeight();
            }
            int i2 = i;
            int j2 = j;
            var poseStack = guiGraphics.pose();
            poseStack.pushPose();
            var bgstart = 0xb4260f0c;//0xf0511d17;//0xf0100010;
            var bgend = bgstart;//0xf0361d17;//bgstart;
            var borderstart = 0x50e0ca9f;//0x505000FF;
            var borderend = 0x50a09172;//0x5028007f;
            guiGraphics.drawManaged(() -> TooltipRenderUtil.renderTooltipBackground(guiGraphics, x, y, i2, j2, 0, bgstart, bgend, borderstart, borderend));
            int k1 = y;

            for (int l1 = 0; l1 < pComponents.size(); l1++) {
                ClientTooltipComponent clienttooltipcomponent1 = pComponents.get(l1);
                clienttooltipcomponent1.renderText(font, x, k1, poseStack.last().pose(), guiGraphics.bufferSource());
                k1 += clienttooltipcomponent1.getHeight() + (l1 == 0 ? 2 : 0);
            }
            k1 = y;
            for (int k2 = 0; k2 < pComponents.size(); k2++) {
                ClientTooltipComponent clienttooltipcomponent2 = pComponents.get(k2);
                clienttooltipcomponent2.renderImage(font, x, k1, guiGraphics);
                k1 += clienttooltipcomponent2.getHeight() + (k2 == 0 ? 2 : 0);
            }
            poseStack.popPose();
        }
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

    private int getMaterialCount(int index, Holder<PartDefinition> forPart) {
        if (index >= 0 && index < menu.workspaceSlots.size()) {
            var slot = menu.workspaceSlots.get(index);
            if (slot.isActive()) {
                var stack = slot.getItem();
                var material = Utils.getMaterialForIngredient(Minecraft.getInstance().level.registryAccess(), stack);
                if (material.isPresent() && forPart.value().canUseMaterial(material.get().value().materialType())) {
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
