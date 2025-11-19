package io.redspace.ironsjewelry.item.book;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.redspace.ironsjewelry.IronsJewelry;
import io.redspace.ironsjewelry.core.data.MaterialDefinition;
import io.redspace.ironsjewelry.core.parameters.IBonusParameterType;
import io.redspace.ironsjewelry.registry.IronsJewelryRegistries;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.PageButton;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.awt.print.Book;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class GuideBookScreen extends Screen {
    public static final ResourceLocation BOOK_LOCATION = IronsJewelry.id("textures/gui/jewelcrafting_guide.png");

    private PageButton forwardButton;
    private PageButton backButton;
    private PageButton homeButton;

    static final BookSection GUIDEBOOK = new BookSection(null, List.of());

    static final int IMAGE_WIDTH = 267;
    static final int IMAGE_HEIGHT = 210;
    static final int MILIS_PER_ITEM = 2000;
    static final int XM = 15;
    static final int YM = 15;
    protected int leftPos;
    protected int topPos;
    protected int ingredientIndex;

    long lastItemDisplayMilis = 0; //todo: likely should not be stored here
    MaterialPage currentMaterial; // todo: temporary
    BookSection currentSection = GUIDEBOOK; //todo: bookmark would be cool
    @Nullable Page currentPage;

    interface Page {
    }

    static class Guidebook {
        List<BookSection> sections;
        int sectionIndex;
        int localPageIndex;

        public Page getCurrentPage() {
            return sections.get(sectionIndex).pages.get(localPageIndex);
        }

        /**
         * @return Whether page successfully turned
         */
        public boolean incrementPage() {
            BookSection currentSection = sections.get(sectionIndex);
            localPageIndex++;
            if (localPageIndex >= currentSection.pages.size()) {
                // finished all current pages, try to advance to next section
                if (sectionIndex < sections.size() - 1) {
                    localPageIndex = 0;
                    sectionIndex++;
                    return true; // we successfully advanced section and reset page counter
                }
                return false; // unable to advance, no sections remaining
            } else {
                return true; // we have more pages remaining
            }
        }

        /**
         * @return Whether page successfully turned
         */
        public boolean decrementPage() {
            localPageIndex--;
            if (localPageIndex == -1) {
                // finished with  current section, try to go back to previous section
                if (sectionIndex > 0) {
                    sectionIndex--;
                    localPageIndex = sections.get(sectionIndex).pages.size() - 1;
                    return true; // we successfully went to previous section and set page counter to final page
                }
                localPageIndex = 0;
                return false; // we have no more sections to go back to, clamp page index back to 0
            } else {
                return true; // we have pages to fall back to
            }
        }

        public int getGlobalPageNumber() {
            int page = 0;
            for (int i = 0; i < sectionIndex; i++) {
                page += sections.get(i).pages.size();
            }
            page += localPageIndex + 1;
            return page;
        }

        public int getMaxPageCount() {
            int pages = 0;
            for (int i = 0; i < sections.size(); i++) {
                pages += sections.get(i).pages.size();
            }
            return pages;
        }

    }

    record BookSection(@Nullable BookSection parent, List<Page> pages) {
    }

    record MaterialPage(Holder<MaterialDefinition> material, int cachedTextColor,
                        List<ItemStack> cachedIngredients) implements Page {
    }

    public GuideBookScreen(Component title) {
        super(title);
        this.minecraft = Minecraft.getInstance();
    }

    @Override
    protected void init() {
        this.leftPos = (this.width - IMAGE_WIDTH) / 2;
        this.topPos = (this.height - IMAGE_HEIGHT) / 2;
        chooseMaterial(IronsJewelryRegistries.materialRegistry(Minecraft.getInstance().level.registryAccess())
                .getHolderOrThrow(ResourceKey.create(IronsJewelryRegistries.Keys.MATERIAL_REGISTRY_KEY, IronsJewelry.id("ruby"))));
    }

    protected void chooseMaterial(Holder<MaterialDefinition> materialDefinitionHolder) {
        this.currentMaterial = new MaterialPage(materialDefinitionHolder, generateTextColor(materialDefinitionHolder.value().paletteLocation()), List.of(materialDefinitionHolder.value().ingredient().getItems()));
        this.ingredientIndex = 0;
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.blit(BOOK_LOCATION, leftPos, topPos, 0, 0, IMAGE_WIDTH, IMAGE_HEIGHT, 512, 256);
    }

    private int getMaxTitleWidth() {
        // could calculate this based on image width, but I like this stylistically
        int averageCharSize = 5;
        int maxCharCount = 15;
        return averageCharSize * maxCharCount;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        if (currentMaterial == null) {
            return;
        }
        var materialHolder = currentMaterial.material();

        MaterialDefinition material = materialHolder.value();
        Component name = Component.translatable(material.descriptionId());
        var currentIngredients = currentMaterial.cachedIngredients();
        if (currentIngredients.isEmpty()) {
            return;
        }
        if (System.currentTimeMillis() > lastItemDisplayMilis + MILIS_PER_ITEM) {
            ingredientIndex = (ingredientIndex + 1) % currentIngredients.size();
            lastItemDisplayMilis = System.currentTimeMillis();
            chooseMaterial(List.of(
                    IronsJewelryRegistries.materialRegistry(Minecraft.getInstance().level.registryAccess())
                            .getHolderOrThrow(ResourceKey.create(IronsJewelryRegistries.Keys.MATERIAL_REGISTRY_KEY, IronsJewelry.id("ruby"))),
                    IronsJewelryRegistries.materialRegistry(Minecraft.getInstance().level.registryAccess())
                            .getHolderOrThrow(ResourceKey.create(IronsJewelryRegistries.Keys.MATERIAL_REGISTRY_KEY, IronsJewelry.id("netherite"))),
                    IronsJewelryRegistries.materialRegistry(Minecraft.getInstance().level.registryAccess())
                            .getHolderOrThrow(ResourceKey.create(IronsJewelryRegistries.Keys.MATERIAL_REGISTRY_KEY, IronsJewelry.id("sapphire")))
            ).get((int)(Math.random() * 3)));
        }
        var poseStack = guiGraphics.pose();

        /*
        Draw Page Title: Ingredient Icon and Material Name
         */
        float itemScale = 2;
        int titleX = leftPos + XM - 5;
        int titleBottomY = topPos + YM / 2 + (int) (16 * itemScale);
        ItemStack item = currentIngredients.get(ingredientIndex);
        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(itemScale, itemScale, itemScale);
        guiGraphics.renderItem(/*Items.IRON_SWORD.getDefaultInstance()*/item, (int) (titleX / itemScale), (int) (titleBottomY / itemScale - 16));
        guiGraphics.pose().popPose();

        var font = Minecraft.getInstance().font;
        float textScale = 2;
        int textcolor = currentMaterial.cachedTextColor();
        poseStack.pushPose();
        textScale *= Math.clamp(getMaxTitleWidth() / (float) font.width(name), 0, 1);
        poseStack.scale(textScale, textScale, textScale);
        guiGraphics.drawString(font, name, (int) ((titleX + 17 * itemScale) / textScale), (int) ((titleBottomY - (2 * itemScale)) / textScale) - font.lineHeight, textcolor, true);
        poseStack.popPose();

        int lineLength = (int) (16 * itemScale + (font.width(name) + 32) * textScale);
        int lineThickness = 2;
        drawLine(guiGraphics, lineThickness, titleX, titleBottomY, titleX + lineLength, titleBottomY, textcolor, textcolor & 0x00FFFFFF);

        Component quality = Component.translatable("tooltip.irons_jewelry.quality_multiplier", material.quality());
        List<MutableComponent> bonusTypes = material.bonusParameters().keySet().stream().map(param -> Component.translatable(param.getDescriptionId())).toList();
        List<Component> bonusValues = material.bonusParameters().entrySet().stream().map(entry ->
                ((IBonusParameterType) entry.getKey()).getSimpleDescription(entry.getValue())).filter(Optional::isPresent).map(opt -> (Component) opt.get()).toList();

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
            var type = bonusTypes.get(i)/*.withStyle(ChatFormatting.UNDERLINE)*//*.withColor(currentMaterial.cachedTextColor())*/;
            var value = bonusValues.get(i);
            int lightColor = scaleColor(currentMaterial.cachedTextColor(), 1.75f);
            int darkColor = scaleColor(currentMaterial.cachedTextColor(), 0.25f);
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
//            guiGraphics.drawString(font, type, leftPos + XM, ypos, 0x0, false);
//            ypos += font.lineHeight;
//            guiGraphics.drawString(font, Component.literal(" ")/*.withStyle(ChatFormatting.DARK_GRAY)*/.append(value.copy().withStyle(ChatFormatting.BLACK)), leftPos + XM, ypos, 0x0, false);
        }
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
}
