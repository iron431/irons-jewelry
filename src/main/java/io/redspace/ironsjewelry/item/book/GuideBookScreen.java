package io.redspace.ironsjewelry.item.book;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.redspace.ironsjewelry.IronsJewelry;
import io.redspace.ironsjewelry.core.data.MaterialDefinition;
import io.redspace.ironsjewelry.core.parameters.IBonusParameterType;
import io.redspace.ironsjewelry.item.book.buttons.GuideBookButton;
import io.redspace.ironsjewelry.item.book.buttons.PageButton;
import io.redspace.ironsjewelry.registry.IronsJewelryRegistries;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class GuideBookScreen extends Screen {
    public static final ResourceLocation BOOK_LOCATION = IronsJewelry.id("textures/gui/jewelcrafting_guide.png");

    private PageButton forwardButton;
    private PageButton backButton;
    private PageButton homeButton;
    private final List<PageButton> pageButtons = new ArrayList<>();

    static final int IMAGE_WIDTH = 267;
    static final int IMAGE_HEIGHT = 210;
    static final int XM = 15;
    static final int YM = 15;
    protected int leftPos;
    protected int topPos;

    float itemScale = 2f;
    float titleScale = 2f;

    GuideBookState bookState = new GuideBookState(
            List.of(new GuideBookState.BookSection(null, List.of(
                    new TableOfContentsPage(),
                    new MaterialPage(
                            IronsJewelryRegistries.materialRegistry(Minecraft.getInstance().level.registryAccess())
                                    .getHolderOrThrow(ResourceKey.create(IronsJewelryRegistries.Keys.MATERIAL_REGISTRY_KEY, IronsJewelry.id("ruby")))
                    ),
                    new MaterialPage(
                            IronsJewelryRegistries.materialRegistry(Minecraft.getInstance().level.registryAccess())
                                    .getHolderOrThrow(ResourceKey.create(IronsJewelryRegistries.Keys.MATERIAL_REGISTRY_KEY, IronsJewelry.id("sapphire")))
                    ),
                    new MaterialPage(
                            IronsJewelryRegistries.materialRegistry(Minecraft.getInstance().level.registryAccess())
                                    .getHolderOrThrow(ResourceKey.create(IronsJewelryRegistries.Keys.MATERIAL_REGISTRY_KEY, IronsJewelry.id("netherite")))
                    )
            )))
    );
    int lastPageNumber;
    Page cachedPage;

    public interface Page {
        void render(GuiGraphics guiGraphics, int titleX, int titleBottomY, int mouseX, int mouseY, float partialTick);

        default List<GuideBookButton> extraButtons() {
            return List.of();
        }
    }


    public GuideBookScreen(Component title) {
        super(title);
        this.minecraft = Minecraft.getInstance();
        this.font = minecraft.font;
        initPageButtons();
    }

    private void initPageButtons() {
        pageButtons.clear();
        int travWidth = 23;
        int travHeight = 13;
        this.forwardButton = new PageButton(leftPos + IMAGE_WIDTH - 10 - travWidth, topPos + IMAGE_HEIGHT - 13 - travHeight, travWidth, travHeight,
                ResourceLocation.withDefaultNamespace("widget/page_forward"), ResourceLocation.withDefaultNamespace("widget/page_forward_highlighted"), GuideBookState::incrementPage);
        this.backButton = new PageButton(leftPos + IMAGE_WIDTH - 10 - travWidth - travWidth - 2, topPos + IMAGE_HEIGHT - 13 - travHeight, travWidth, travHeight,
                ResourceLocation.withDefaultNamespace("widget/page_backward"), ResourceLocation.withDefaultNamespace("widget/page_backward_highlighted"), GuideBookState::decrementPage);
        this.homeButton = new PageButton(leftPos + 10, topPos + IMAGE_HEIGHT - 13 - travHeight, travWidth, travHeight,
                ResourceLocation.withDefaultNamespace("widget/page_backward"), ResourceLocation.withDefaultNamespace("widget/page_backward_highlighted"), GuideBookState::returnSection);
        pageButtons.add(forwardButton);
        pageButtons.add(backButton);
        pageButtons.add(homeButton);
    }

    @Override
    protected void init() {
        this.leftPos = (this.width - IMAGE_WIDTH) / 2;
        this.topPos = (this.height - IMAGE_HEIGHT) / 2;
        initPageButtons();
        this.lastPageNumber = -1;
//        chooseMaterial(IronsJewelryRegistries.materialRegistry(Minecraft.getInstance().level.registryAccess())
//                .getHolderOrThrow(ResourceKey.create(IronsJewelryRegistries.Keys.MATERIAL_REGISTRY_KEY, IronsJewelry.id("topaz"))));
    }

    protected void chooseMaterial(Holder<MaterialDefinition> materialDefinitionHolder) {
        this.cachedPage = new MaterialPage(materialDefinitionHolder);
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.blit(BOOK_LOCATION, leftPos, topPos, 0, 0, IMAGE_WIDTH, IMAGE_HEIGHT, 512, 256);
    }

    private int getMaxTitleWidth() {
        int averageCharSize = 5;
        int maxCharCount = 15;
        return averageCharSize * maxCharCount;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        int currentPageNumber = bookState.getGlobalPageNumber();
        if (lastPageNumber != currentPageNumber) {
            lastPageNumber = currentPageNumber;
            cachedPage = bookState.getCurrentPage();
        }
        int titleX = leftPos + XM - 5;
        int titleBottomY = topPos + YM / 2 + (int) (16 * itemScale);
        cachedPage.render(guiGraphics, titleX, titleBottomY, mouseX, mouseY, partialTick);
        for (GuideBookButton button : pageButtons) {
            button.render(guiGraphics, button.boundingBox().containsPoint(mouseX, mouseY), partialTick);
        }
        for (GuideBookButton button : cachedPage.extraButtons()) {
            button.render(guiGraphics, button.boundingBox().containsPoint(mouseX, mouseY), partialTick);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseAction) {
        for (GuideBookButton button : pageButtons) {
            if (button.boundingBox().containsPoint((int) mouseX, (int) mouseY) && button.onClick(this.bookState)) {
                return true;
            }
        }
        for (GuideBookButton button : cachedPage.extraButtons()) {
            if (button.boundingBox().containsPoint((int) mouseX, (int) mouseY) && button.onClick(this.bookState)) {
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, mouseAction);
    }

    private int scaleColor(int color, float scalar) {
        var r = (int) Math.clamp((color >> 16 & 0xFF) * scalar, 0, 255);
        var g = (int) Math.clamp((color >> 8 & 0xFF) * scalar, 0, 255);
        var b = (int) Math.clamp((color & 0xFF) * scalar, 0, 255);
        return (r << 16) | (g << 8) | b;
    }

    private void drawLine(GuiGraphics graphics, int thickness, int startX, int startY, int endX, int endY, int startColor, int endColor) {
        graphics.drawManaged(() -> {
            VertexConsumer consumer = graphics.bufferSource().getBuffer(RenderType.gui());
            Vec3 startV = new Vec3(startX, startY, 0);
            Vec3 endV = new Vec3(endX, endY, 0);
            Vec3 line = endV.subtract(startV);
            Vec3 volume = line.normalize().cross(new Vec3(0, 0, 1)).scale(thickness / 2.0);

            Vec3[] corners = {startV.add(volume), startV.subtract(volume), endV.subtract(volume), endV.add(volume)};
            Matrix4f matrix4f = graphics.pose().last().pose();
            consumer.addVertex(matrix4f, (float) corners[0].x, (float) corners[0].y, 0).setColor(startColor);
            consumer.addVertex(matrix4f, (float) corners[1].x, (float) corners[1].y, 0).setColor(startColor);
            consumer.addVertex(matrix4f, (float) corners[2].x, (float) corners[2].y, 0).setColor(endColor);
            consumer.addVertex(matrix4f, (float) corners[3].x, (float) corners[3].y, 0).setColor(endColor);
        });
    }

    private int generateTextColor(ResourceLocation palette) {
        try {
            Optional<Resource> resource = Minecraft.getInstance().getResourceManager().getResource(SpriteSource.TEXTURE_ID_CONVERTER.idToFile(palette));
            if (resource.isPresent()) {
                int[] aint;
                try (
                        InputStream inputstream = resource.get().open();
                        NativeImage nativeimage = NativeImage.read(inputstream);
                ) {
                    aint = nativeimage.getPixelsRGBA();
                }
                aint = Arrays.copyOf(aint, 5); // exclude brightest two pixels, which often contain pure white
                // for some reason, these ints are AGBR
                int r = Arrays.stream(aint).map(i -> i & 0xFF).sum() / aint.length;
                int g = Arrays.stream(aint).map(i -> i >> 8 & 0xFF).sum() / aint.length;
                int b = Arrays.stream(aint).map(i -> i >> 16 & 0xFF).sum() / aint.length;
                int max = Math.max(Math.max(r, g), b);
                if (max < 128) {
                    // ensure darker palettes still create bright, legible text colors
                    int factor = 128 / max;
                    r *= factor;
                    b *= factor;
                    g *= factor;
                }
                return 0xFF000000 + (r << 16) + (g << 8) + b;
            }
        } catch (Exception exception) {
            IronsJewelry.LOGGER.error("Failed to generate guidebook coloration for material palette: \"{}\"", palette);
        }
        return 0xFFFFFF;
    }

    public boolean isPauseScreen() {
        return false;
    }

    public class TableOfContentsPage implements Page {
        @Override
        public void render(GuiGraphics guiGraphics, int titleX, int titleBottomY, int mouseX, int mouseY, float partialTick) {
            var poseStack = guiGraphics.pose();
            int color = 0xFF808080;
            float textScale = titleScale;
            Component title = Component.translatable("ui.irons_jewelry.guide_book.table_of_contents").withColor(color);
            poseStack.pushPose();
            textScale *= Math.clamp(getMaxTitleWidth() / (float) font.width(title), 0, 1);
            poseStack.scale(textScale, textScale, textScale);
            guiGraphics.drawString(font, title, (int) (titleX / textScale), (int) ((titleBottomY - (2 * itemScale)) / textScale) - font.lineHeight, color, true);
            poseStack.popPose();
            int lineLength = (int) ((font.width(title) + 32) * textScale);
            int lineThickness = 2;
            drawLine(guiGraphics, lineThickness, titleX, titleBottomY, titleX + lineLength, titleBottomY, color, color & 0x00FFFFFF);
        }
    }

    public class MaterialPage implements Page {
        final Holder<MaterialDefinition> material;
        final int cachedTextColor;
        final CyclicItemRenderer itemRenderer;

        public MaterialPage(Holder<MaterialDefinition> material) {
            this.material = material;
            this.cachedTextColor = generateTextColor(material.value().paletteLocation());
            this.itemRenderer = new CyclicItemRenderer(List.of(material.value().ingredient().getItems()));
        }

        @Override
        public void render(GuiGraphics guiGraphics, int titleX, int titleBottomY, int mouseX, int mouseY, float partialTick) {
            MaterialDefinition material = this.material.value();
            Component name = Component.translatable(material.descriptionId());
            var poseStack = guiGraphics.pose();
            /*
            Draw Page Title: Ingredient Icon and Material Name
             */
            this.itemRenderer.renderBottomLeft(guiGraphics, titleX, titleBottomY, itemScale);
            var font = Minecraft.getInstance().font;
            float textScale = titleScale;
            poseStack.pushPose();
            textScale *= Math.clamp(getMaxTitleWidth() / (float) font.width(name), 0, 1);
            poseStack.scale(textScale, textScale, textScale);
            guiGraphics.drawString(font, name, (int) ((titleX + 17 * itemScale) / textScale), (int) ((titleBottomY - (2 * itemScale)) / textScale) - font.lineHeight, cachedTextColor, true);
            poseStack.popPose();
            int lineLength = (int) (16 * itemScale + (font.width(name) + 32) * textScale);
            int lineThickness = 2;
            drawLine(guiGraphics, lineThickness, titleX, titleBottomY, titleX + lineLength, titleBottomY, cachedTextColor, cachedTextColor & 0x00FFFFFF);

            Component quality = Component.translatable("tooltip.irons_jewelry.quality_multiplier", material.quality());
            List<MutableComponent> bonusTypes = material.bonusParameters().keySet().stream().map(param -> Component.translatable(param.getDescriptionId())).toList();
            List<MutableComponent> bonusValues = material.bonusParameters().entrySet().stream().map(entry ->
                    ((IBonusParameterType) entry.getKey()).getSimpleDescription(entry.getValue())).filter(Optional::isPresent).map(opt -> ((Component) opt.get()).copy()).toList();

            int ypos = (int) (topPos + YM + font.lineHeight * (1 + itemScale));
            guiGraphics.drawString(font, quality, leftPos + XM, ypos, 0x0, false);
            ypos += font.lineHeight * 2;
            int bonusTypeMaxWidth = 0;
            for (var t : bonusTypes) {
                var w = font.width(t);
                if (w > bonusTypeMaxWidth) {
                    bonusTypeMaxWidth = w;
                }
            }
            int valueColumMargin = bonusTypeMaxWidth + 8;
            for (int i = 0; i < bonusTypes.size(); i++) {
                //fixme: this fails to wipe formatting from embedded components
                var type = bonusTypes.get(i).setStyle(Style.EMPTY);
                var value = bonusValues.get(i).setStyle(Style.EMPTY);
                int lightColor = scaleColor(cachedTextColor, 1.75f);
                int darkColor = scaleColor(cachedTextColor, 0.25f);
                for (int x = -1; x <= 1; x++) {
                    for (int y = -1; y <= 1; y++) {
                        guiGraphics.drawString(font, type, leftPos + XM + x, ypos + y, darkColor, false);
                    }
                }
                guiGraphics.drawString(font, type, leftPos + XM, ypos, lightColor, false);
                int availableInfoWidth = IMAGE_WIDTH - XM - valueColumMargin;
                for (var line : font.split(value, availableInfoWidth)) {
                    guiGraphics.drawString(font, line, leftPos + XM + valueColumMargin, ypos, 0x0, false);
                    ypos += font.lineHeight;
                }
                ypos += 1;
            }
        }
    }
}
