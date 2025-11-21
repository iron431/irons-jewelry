package io.redspace.ironsjewelry.item.book;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.redspace.ironsjewelry.IronsJewelry;
import io.redspace.ironsjewelry.core.data.*;
import io.redspace.ironsjewelry.core.parameters.IBonusParameterType;
import io.redspace.ironsjewelry.item.book.buttons.GuideBookButton;
import io.redspace.ironsjewelry.item.book.buttons.PageButton;
import io.redspace.ironsjewelry.item.book.buttons.TextButton;
import io.redspace.ironsjewelry.registry.AssetHandlerRegistry;
import io.redspace.ironsjewelry.registry.IronsJewelryRegistries;
import io.redspace.ironsjewelry.registry.ItemRegistry;
import io.redspace.ironsjewelry.utils.Utils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.io.InputStream;
import java.util.*;
import java.util.function.Function;

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

    GuideBookState bookState = createGuidebookState();
    int lastPageNumber;
    Page cachedPage;

    public GuideBookState createGuidebookState() {
//        GuideBookState.BookSection tableOfContents = new GuideBookState.BookSection(new TableOfContentsPage(Component.translatable("ui.irons_jewelry.guide_book.table_of_contents")));
        var materialRegistry = IronsJewelryRegistries.materialRegistry(Minecraft.getInstance().level.registryAccess());
        var patternRegistry = IronsJewelryRegistries.patternRegistry(Minecraft.getInstance().level.registryAccess());
        List<Page> materialPages = new ArrayList<>();
        materialRegistry.holders().filter(holder -> !holder.value().ingredient().hasNoItems()).forEach(holder -> materialPages.add(new MaterialPage(holder)));
        List<Function<TableOfContentsPage.EntryPreparation, GuideBookButton>> materialTableOfContentsEntries = new ArrayList<>();
        for (int i = 0; i < materialPages.size(); i++) {
            var materialPage = ((MaterialPage) materialPages.get(i));
            var material = materialPage.material;
            materialTableOfContentsEntries.add(preparation ->
                    new TextButton(preparation.x(), preparation.y(), preparation.width(), preparation.height(), Component.translatable(material.value().descriptionId()), 0xFF000000, ChatFormatting.YELLOW.getColor(),
                            List.of(material.value().ingredient().getItems()), guidebook -> guidebook.navigateToPage(materialPage)));
        }
        materialPages.addAll(0, createTableOfContentsPages(Component.translatable("ui.irons_jewelry.guide_book.table_of_contents_materials"), materialTableOfContentsEntries, 75));

        List<Page> patternPages = new ArrayList<>();
        patternRegistry.holders().forEach(holder -> patternPages.add(new PatternPage(holder)));
        List<Function<TableOfContentsPage.EntryPreparation, GuideBookButton>> patternTableOfContentsEntries = new ArrayList<>();
        for (int i = 0; i < patternPages.size(); i++) {
            var patternPage = ((PatternPage) patternPages.get(i));
            var pattern = patternPage.pattern;
            patternTableOfContentsEntries.add(preparation ->
                    new TextButton(preparation.x(), preparation.y(), preparation.width(), preparation.height(), Component.translatable(pattern.value().descriptionId()), 0xFF000000, ChatFormatting.YELLOW.getColor(),
                            List.of(patternPage.itemIcon), guidebook -> guidebook.navigateToPage(patternPage)));
        }
        patternPages.addAll(0, createTableOfContentsPages(Component.translatable("ui.irons_jewelry.guide_book.table_of_contents_patterns"), patternTableOfContentsEntries, 75));

        List<Page> tableOfContentsPages = new ArrayList<>();
        tableOfContentsPages.addAll(createTableOfContentsPages(Component.translatable("ui.irons_jewelry.guide_book.table_of_contents"),
                List.of(
                        // Materials
                        preparation -> new TextButton(preparation.x(), preparation.y(), preparation.width(), preparation.height(),
                                Component.translatable("ui.irons_jewelry.guide_book.table_of_contents_materials"), 0xFF000000, ChatFormatting.YELLOW.getColor(),
                                ItemRegistry.items().stream().filter(holder -> holder.is(Tags.Items.GEMS)).map(DeferredHolder::get).map(Item::getDefaultInstance).toList(), guidebook -> guidebook.navigateToPage(materialPages.get(0))),
                        // Patterns
                        preparation -> new TextButton(preparation.x(), preparation.y(), preparation.width(), preparation.height(),
                                Component.translatable("ui.irons_jewelry.guide_book.table_of_contents_patterns"), 0xFF000000, ChatFormatting.YELLOW.getColor(),
                                List.of(ItemRegistry.RECIPE.get().getDefaultInstance()), guidebook -> guidebook.navigateToPage(patternPages.get(0)))
                ), 75));

        return new GuideBookState(
                List.of(new GuideBookState.BookSection(tableOfContentsPages), new GuideBookState.BookSection(materialPages), new GuideBookState.BookSection(patternPages))
        );
    }

    abstract class Page {
        abstract void render(GuiGraphics guiGraphics, int titleX, int titleBottomY, int mouseX, int mouseY, float partialTick);

        void drawTitle(GuiGraphics guiGraphics, Component text, int titleX, int titleBottomY, int color, @Nullable CyclicItemRenderer itemRenderer) {
            var poseStack = guiGraphics.pose();
            poseStack.pushPose();
            float textScale = titleScale;
            int xOffset;
            if (itemRenderer == null) {
                xOffset = 3;
            } else {
                xOffset = (int) (18 * itemScale);
                itemRenderer.renderBottomLeft(guiGraphics, titleX, titleBottomY, itemScale);
            }
            textScale *= Math.clamp(getMaxTitleWidth() / (float) font.width(text), 0, 1);
            poseStack.scale(textScale, textScale, textScale);
            guiGraphics.drawString(font, text, (int) ((titleX + xOffset) / textScale), (int) ((titleBottomY - (2 * itemScale)) / textScale) - font.lineHeight, color, true);
            poseStack.popPose();
            int lineLength = Math.min((int) (xOffset + (font.width(text) + 24) * textScale), IMAGE_WIDTH - XM * 2);
            int lineThickness = 2;
            int split = 15;
            int color2 = color & 0x00FFFFFF;
            drawLine(guiGraphics, lineThickness, titleX - 3, titleBottomY, titleX + split, titleBottomY, color2, color);
            drawLine(guiGraphics, lineThickness, titleX + split, titleBottomY, titleX + lineLength, titleBottomY, color, color2);
        }

        List<GuideBookButton> extraButtons() {
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
        this.forwardButton = new PageButton(IMAGE_WIDTH - 10 - travWidth, IMAGE_HEIGHT - 13 - travHeight, travWidth, travHeight,
                ResourceLocation.withDefaultNamespace("widget/page_forward"), ResourceLocation.withDefaultNamespace("widget/page_forward_highlighted"), GuideBookState::incrementPage);
        this.backButton = new PageButton(IMAGE_WIDTH - 10 - travWidth - travWidth - 2, IMAGE_HEIGHT - 13 - travHeight, travWidth, travHeight,
                ResourceLocation.withDefaultNamespace("widget/page_backward"), ResourceLocation.withDefaultNamespace("widget/page_backward_highlighted"), GuideBookState::decrementPage);
        this.homeButton = new PageButton(10, IMAGE_HEIGHT - 13 - travHeight, travWidth, travHeight,
                ResourceLocation.withDefaultNamespace("widget/page_backward"), ResourceLocation.withDefaultNamespace("widget/page_backward_highlighted"), GuideBookState::returnSection);
        pageButtons.add(forwardButton);
        pageButtons.add(backButton);
        pageButtons.add(homeButton);
    }

    @Override
    protected void init() {
        this.leftPos = (this.width - IMAGE_WIDTH) / 2;
        this.topPos = (this.height - IMAGE_HEIGHT) / 2;
//        initPageButtons();
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
        int maxCharCount = 16;
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
        int titleX = leftPos + XM - 2;
        int titleBottomY = topPos + YM / 2 + (int) (16 * itemScale);
        cachedPage.render(guiGraphics, titleX, titleBottomY, mouseX, mouseY, partialTick);
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(leftPos, topPos, 0);
        for (GuideBookButton button : pageButtons) {
            button.render(guiGraphics, button.boundingBox(leftPos, topPos).containsPoint(mouseX, mouseY), partialTick);
        }
        for (GuideBookButton button : cachedPage.extraButtons()) {
            button.render(guiGraphics, button.boundingBox(leftPos, topPos).containsPoint(mouseX, mouseY), partialTick);
        }
        guiGraphics.pose().popPose();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseAction) {
        for (GuideBookButton button : pageButtons) {
            if (button.boundingBox(leftPos, topPos).containsPoint((int) mouseX, (int) mouseY) && button.onClick(this.bookState)) {
                Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN, 1f));
                return true;
            }
        }
        for (GuideBookButton button : cachedPage.extraButtons()) {
            if (button.boundingBox(leftPos, topPos).containsPoint((int) mouseX, (int) mouseY) && button.onClick(this.bookState)) {
                Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN, 1f));
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

    private List<TableOfContentsPage> createTableOfContentsPages(Component title, List<Function<TableOfContentsPage.EntryPreparation, GuideBookButton>> entries, int width) {
        int startingX = XM;
        int availableWidth = IMAGE_WIDTH - startingX - XM;
        int startingY = YM / 2 + (int) (16 * itemScale) + 10; // todo: not hardcode this? (copy of bottomTitleY)
        int availableHeight = IMAGE_HEIGHT - startingY - YM * 2;
        int entryWidth = width;
        int entryHeight = 16;
        int xMargin = Math.max(3, (availableWidth - entryWidth * 3) / 2 - 1);
        int yMargin = 1;
        List<TableOfContentsPage> pages = new ArrayList<>();
        List<GuideBookButton> workingButtons = new ArrayList<>();
        int workingX = 0, workingY = 0;
        for (int i = 0; i < entries.size(); i++) {
            //assume all parameters are valid at the top of the loop
            workingButtons.add(entries.get(i).apply(new TableOfContentsPage.EntryPreparation(workingX + startingX, workingY + startingY, entryWidth, entryHeight)));
            workingY += entryHeight + yMargin;
            if (workingY + entryHeight >= availableHeight) {
                // try to start next column
                workingY = 0;
                workingX += entryWidth + xMargin;
                if (workingX + entryWidth >= availableWidth) {
                    workingX = 0;
                    // no more room for columns, start new page
                    pages.add(new TableOfContentsPage(title, workingButtons));
                    workingButtons.clear();
                }
            }
        }
        if (!workingButtons.isEmpty()) {
            pages.add(new TableOfContentsPage(title, workingButtons));
        }
        return pages;
    }

    public class TableOfContentsPage extends Page {
        public record EntryPreparation(int x, int y, int width, int height) {
        }

        final Component title;
        final List<GuideBookButton> entries;


        public TableOfContentsPage(Component title, List<GuideBookButton> entries) {
            this.title = title;
            this.entries = new ArrayList<>();
            this.entries.addAll(entries);
        }

        @Override
        public void render(GuiGraphics guiGraphics, int titleX, int titleBottomY, int mouseX, int mouseY, float partialTick) {
            int color = 0xFF808080;
            var text = title.copy().withColor(color);
            drawTitle(guiGraphics, text, titleX, titleBottomY, color, null);
        }

        @Override
        public List<GuideBookButton> extraButtons() {
            return entries;
        }
    }

    public class MaterialPage extends Page {
        final Holder<MaterialDefinition> material;
        final int cachedTextColor;
        final CyclicItemRenderer itemRenderer;
        final List<MutableComponent> bonusTypes;
        final List<MutableComponent> bonusValues;
        final List<MutableComponent> bonusTooltip;

        public MaterialPage(Holder<MaterialDefinition> material) {
            this.material = material;
            this.cachedTextColor = generateTextColor(material.value().paletteLocation());
            this.itemRenderer = new CyclicItemRenderer(List.of(material.value().ingredient().getItems()));
            this.bonusTypes = new ArrayList<>();
            this.bonusValues = new ArrayList<>();
            this.bonusTooltip = new ArrayList<>();
            bonusTypes.add(Component.translatable("ui.irons_jewelry.quality"));
            bonusValues.add(Component.literal("x").append(String.valueOf(material.value().quality())));
            bonusTooltip.add(Component.translatable("ui.irons_jewelry.quality.description"));
            for (var entry : material.value().bonusParameters().entrySet()) {
                IBonusParameterType param = entry.getKey();
                Object value = entry.getValue();
                Optional<Component> opt = param.getSimpleDescription(value);
                if (opt.isPresent()) {
                    var typeName = Component.translatable(param.getDescriptionId());
                    bonusTypes.add(typeName);
                    bonusValues.add(opt.get().copy());
                    bonusTooltip.add(Component.translatable("ui.irons_jewelry.bonus_type.description",
                            typeName.copy().withStyle(ChatFormatting.WHITE),
                            Component.translatable(material.value().descriptionId()).withStyle(ChatFormatting.WHITE),
                            opt.get().copy().withStyle(ChatFormatting.WHITE)).withStyle(ChatFormatting.GRAY));
                }
            }
//            material.value().bonusParameters().keySet().stream().map(param -> Component.translatable(param.getDescriptionId())).forEach(bonusTypes::add);
//            material.value().bonusParameters().entrySet().stream().map(entry ->
//                    ((IBonusParameterType) entry.getKey()).getSimpleDescription(entry.getValue())).filter(Optional::isPresent).map(opt -> ((Component) opt.get()).plainCopy()).forEach(bonusValues::add);
        }

        @Override
        public void render(GuiGraphics guiGraphics, int titleX, int titleBottomY, int mouseX, int mouseY, float partialTick) {
            MaterialDefinition material = this.material.value();
            Component name = Component.translatable(material.descriptionId());
            var poseStack = guiGraphics.pose();
            /*
            Draw Page Title: Ingredient Icon and Material Name
             */
            drawTitle(guiGraphics, name, titleX, titleBottomY, cachedTextColor, itemRenderer);
            /*
            Draw Page Body
             */
            int ypos = (int) (topPos + YM + font.lineHeight * (1 + itemScale) + font.lineHeight / 2f);
            int bonusTypeMaxWidth = 0;
            for (var t : bonusTypes) {
                var w = font.width(t);
                if (w > bonusTypeMaxWidth) {
                    bonusTypeMaxWidth = w;
                }
            }
            int valueColumMargin = bonusTypeMaxWidth + 8;
            int tooltipIndex = -1;
            for (int i = 0; i < bonusTypes.size(); i++) {
                var type = bonusTypes.get(i);
                var value = Component.literal(bonusValues.get(i).getString());
                int lightColor = scaleColor(cachedTextColor, 1.75f);
                int darkColor = scaleColor(cachedTextColor, 0.2f);
                for (int x = -1; x <= 1; x++) {
                    for (int y = -1; y <= 1; y++) {
                        guiGraphics.drawString(font, type, leftPos + XM + x, ypos + y, darkColor, false);
                    }
                }
                guiGraphics.drawString(font, type, leftPos + XM, ypos, lightColor, false);
                if (mouseX >= leftPos + XM && mouseX <= leftPos + XM + font.width(type) && mouseY >= ypos && mouseY <= ypos + font.lineHeight) {
                    tooltipIndex = i;
                }
                int availableInfoWidth = IMAGE_WIDTH - XM * 2 - valueColumMargin;
                for (var line : font.split(value, availableInfoWidth)) {
                    guiGraphics.drawString(font, line, leftPos + XM + valueColumMargin, ypos, 0x0, false);
                    ypos += font.lineHeight;
                }
                ypos += 1;
            }
            if (tooltipIndex >= 0) {
                guiGraphics.renderTooltip(font, font.split(bonusTooltip.get(tooltipIndex), 150), mouseX, mouseY);
            }
        }
    }

    public class PatternPage extends Page {
        record PartInfo(TextureAtlasSprite sprite,/*ItemStack preview,*//* Component name,*/
                        List<? extends FormattedCharSequence> tooltip) {
            private static final ResourceLocation INPUT_SLOT = IronsJewelry.id("jewelcrafting_station/guidebook_part_frame");

            void render(GuiGraphics guiGraphics, int x, int y, int mouseX, int mouseY, float partialTick) {
                int slotMargin = 4;
                int size = 24;
                guiGraphics.blitSprite(INPUT_SLOT, x, y, 200, size, size);
                guiGraphics.blit(x + slotMargin, y + slotMargin, 200, 16, 16, sprite);
                if (mouseX >= x && mouseX <= x + size && mouseY >= y && mouseY <= y + size) {
                    guiGraphics.renderTooltip(Minecraft.getInstance().font, tooltip, mouseX, mouseY);
                }
            }
        }

        final Holder<PatternDefinition> pattern;
        final ItemStack itemIcon;
        final CyclicItemRenderer itemRenderer;
        final List<PartInfo> partInfo;
        final List<MutableComponent> bonusInfo;
        final int titleColor;

        public PatternPage(Holder<PatternDefinition> pattern) {
            this.pattern = pattern;
            this.itemIcon = createPatternItem(pattern);
            this.itemRenderer = new CyclicItemRenderer(List.of(itemIcon));
            this.partInfo = new ArrayList<>(pattern.value().partTemplate().size());
            JewelryData data = JewelryData.get(itemIcon);
            var handler = AssetHandlerRegistry.JEWELRY_HANDLER.get();
            var partKeys = pattern.value().partTemplate().stream().sorted(Comparator.comparingInt(PartIngredient::drawOrder)).toList();
            for (var partIngredient : partKeys) {
                var part = partIngredient.part();
                List<Component> tooltip = new ArrayList<>();
                var material = data.parts().get(part);
                tooltip.add(Component.translatable(part.value().descriptionId()).withStyle(ChatFormatting.UNDERLINE).withColor(generateTextColor(material.value().paletteLocation())));
                tooltip.add(Component.literal(" ").append(Component.translatable("tooltip.irons_jewelry.material_cost", Component.literal(String.valueOf(partIngredient.materialCost())).withStyle(ChatFormatting.WHITE))).withStyle(ChatFormatting.GRAY));
                tooltip.add(Component.translatable("tooltip.irons_jewelry.applicable_materials").withStyle(ChatFormatting.YELLOW, ChatFormatting.UNDERLINE));
                IronsJewelryRegistries.materialRegistry(Minecraft.getInstance().level.registryAccess()).stream().filter(materialDefinition -> !materialDefinition.ingredient().hasNoItems() && part.value().canUseMaterial(materialDefinition.materialType()))
                        .forEach(m -> tooltip.add(Component.literal(" ").append(Component.translatable(m.descriptionId())).withStyle(ChatFormatting.GRAY)));
                partInfo.add(new PartInfo(handler.getSprite(handler.getSpriteLocation(part, material)), Utils.rasterizeComponentList(tooltip)));
            }
            this.titleColor = generateTextColor(data.parts().get(partKeys.get(0).part()).value().paletteLocation());
            this.bonusInfo = new ArrayList<>();
            for (var component : pattern.value().getPatternBonusesTooltip()) {
                component = component.withColor(0x0);
                this.bonusInfo.add(component);
            }
        }

        @Override
        public void render(GuiGraphics guiGraphics, int titleX, int titleBottomY, int mouseX, int mouseY, float partialTick) {
            PatternDefinition pattern = this.pattern.value();
            Component name = Component.translatable(pattern.descriptionId());
            var poseStack = guiGraphics.pose();
            /*
            Draw Page Title: Icon and Name
             */
            drawTitle(guiGraphics, name, titleX, titleBottomY, titleColor, itemRenderer);
            /*
            Draw Part Info
             */
            int partSectionX = titleX;
            int partSectionY = titleBottomY + 10;
            int partSectionWidth = 48;
            int titleSpacer = font.lineHeight * 3 / 2;
            guiGraphics.drawString(font, Component.translatable("tooltip.irons_jewelry.parts_header").withStyle(ChatFormatting.UNDERLINE), partSectionX, partSectionY, 0x0, false);
            for (int i = 0; i < partInfo.size(); i++) {
                int x = partSectionX;
                int y = partSectionY + titleSpacer + i * 26;
                if (i >= 4) {
                    x += 26;
                    y -= 26 * 4;
                    partSectionWidth = 72;
                }
                partInfo.get(i).render(guiGraphics, x, y, mouseX, mouseY, partialTick);
            }
            /*
            Draw Bonus Info
             */
            int bonusSectionX = titleX + partSectionWidth;
            int bonusSectionY = partSectionY;
            int bonusSectionWidth = 120;
            guiGraphics.drawString(font, Component.translatable("tooltip.irons_jewelry.bonus_crafted_header").withStyle(ChatFormatting.UNDERLINE), bonusSectionX, bonusSectionY, 0x0, false);
            int yOffset = 0;
            for (int i = 1; i < bonusInfo.size(); i++) {
                var lines = font.split(bonusInfo.get(i), bonusSectionWidth);
                boolean indent = false;
                for (var line : lines) {
                    guiGraphics.drawString(font, line, bonusSectionX + (indent ? 5 : 0), bonusSectionY + yOffset + titleSpacer, 0x0, false);
                    yOffset += font.lineHeight;
                    indent = true;
                }
            }
        }

        private static ItemStack createPatternItem(Holder<PatternDefinition> pattern) {
            try {
                ItemStack item = new ItemStack(pattern.value().jewelryType().item());
                Holder<MaterialDefinition> renderMaterial = null;
                var materialRegistry = IronsJewelryRegistries.materialRegistry(Minecraft.getInstance().level.registryAccess());
                var metal = materialRegistry.getHolder(IronsJewelry.id("gold")).get();
                var gem = materialRegistry.getHolder(IronsJewelry.id("diamond")).get();
                Map<Holder<PartDefinition>, Holder<MaterialDefinition>> parts = new HashMap<>();
                for (var partIngredient : pattern.value().partTemplate()) {
                    var part = partIngredient.part();
                    if (part.value().canUseMaterial("metal")) {
                        renderMaterial = metal;
                    } else if (part.value().canUseMaterial("gem")) {
                        renderMaterial = gem;
                    } else {
                        for (MaterialDefinition materialDefinition : materialRegistry) {
                            if (part.value().canUseMaterial(materialDefinition.materialType())) {
                                renderMaterial = materialRegistry.wrapAsHolder(materialDefinition);
                                break;
                            }
                        }
                        Objects.requireNonNull(renderMaterial, "No valid material found for part \"" + part.getKey() + "\"");
                    }
                    parts.put(part, renderMaterial);
                }
                JewelryData data = JewelryData.renderable(pattern, parts);
                JewelryData.set(item, data);
                return item;
            } catch (Exception e) {
                IronsJewelry.LOGGER.error("Failed to generate guidebook pattern preview: {}", e.getMessage());
                return ItemStack.EMPTY;
            }
        }
    }
}
