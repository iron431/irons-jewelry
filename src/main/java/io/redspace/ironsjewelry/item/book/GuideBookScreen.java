package io.redspace.ironsjewelry.item.book;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.NativeImage;
import io.redspace.ironsjewelry.IronsJewelry;
import io.redspace.ironsjewelry.core.data.JewelryData;
import io.redspace.ironsjewelry.core.data.MaterialDefinition;
import io.redspace.ironsjewelry.core.data.PartDefinition;
import io.redspace.ironsjewelry.core.data.PartIngredient;
import io.redspace.ironsjewelry.core.data.PatternDefinition;
import io.redspace.ironsjewelry.core.data.PlayerData;
import io.redspace.ironsjewelry.core.parameters.IBonusParameterType;
import io.redspace.ironsjewelry.item.book.buttons.GuideBookButton;
import io.redspace.ironsjewelry.item.book.buttons.PageButton;
import io.redspace.ironsjewelry.item.book.buttons.TextButton;
import io.redspace.ironsjewelry.network.packets.ServerboundSetBookmarkPacket;
import io.redspace.ironsjewelry.registry.AssetHandlerRegistry;
import io.redspace.ironsjewelry.registry.IronsJewelryRegistries;
import io.redspace.ironsjewelry.registry.ItemRegistry;
import io.redspace.ironsjewelry.registry.ParameterTypeRegistry;
import io.redspace.ironsjewelry.utils.Utils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenAxis;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.jetbrains.annotations.Nullable;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.function.Function;
import java.util.stream.Stream;

public class GuideBookScreen extends Screen {

    public static final Identifier BOOK_LOCATION = IronsJewelry.id("textures/gui/jewelcrafting_guide.png");

    private PageButton forwardButton;
    private PageButton backButton;
    private PageButton homeButton;
    private BookmarkButton bookmarkButton;
    private final List<GuideBookButton> nativeButtons = new ArrayList<>();

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

    public GuideBookScreen() {
        super(Component.empty());
        initPageButtons();
    }

    private void initPageButtons() {
        nativeButtons.clear();
        int travWidth = 23;
        int travHeight = 13;
        this.forwardButton = new PageButton(IMAGE_WIDTH - 10 - travWidth, IMAGE_HEIGHT - 13 - travHeight, travWidth, travHeight,
                IronsJewelry.id("guidebook/page_forward"), IronsJewelry.id("guidebook/page_forward_highlighted"), GuideBookState::incrementPage);
        this.backButton = new PageButton(IMAGE_WIDTH - 10 - travWidth - travWidth - 2, IMAGE_HEIGHT - 13 - travHeight, travWidth, travHeight,
                IronsJewelry.id("guidebook/page_backward"), IronsJewelry.id("guidebook/page_backward_highlighted"), GuideBookState::decrementPage);
        this.homeButton = new PageButton(IMAGE_WIDTH - 10 - travWidth - travWidth - travWidth - 8 - 2, IMAGE_HEIGHT - 13 - travHeight, travWidth, travHeight,
                IronsJewelry.id("guidebook/page_return"), IronsJewelry.id("guidebook/page_return_highlighted"), GuideBookState::returnSection);
        this.bookmarkButton = new BookmarkButton(-10, YM, 9, 48, IronsJewelry.id("guidebook/bookmark"), IronsJewelry.id("guidebook/bookmark_active"));
        nativeButtons.add(forwardButton);
        nativeButtons.add(backButton);
        nativeButtons.add(homeButton);
        nativeButtons.add(bookmarkButton);
    }

    @Override
    protected void init() {
        this.leftPos = (this.width - IMAGE_WIDTH) / 2;
        this.topPos = (this.height - IMAGE_HEIGHT) / 2;
        this.lastPageNumber = -1;
        if (cachedPage == null) {
            // if no cached page exists, we are opening the book for the first time
            // we don't want to navigate to the bookmark whenever the screen is resized
            var playerdata = PlayerData.get(minecraft.player);
            try {
                if (playerdata.hasBookmark()) {
                    this.bookState.navigateToPage(bookState.getGlobalPage(playerdata.getBookmarkIndex()));
                }
            } catch (Exception e) {
                setBookmark(-1);
            }
        }
    }

    public void setBookmark(int index) {
        PlayerData.get(minecraft.player).setBookmarkIndex(index);
        ClientPacketDistributor.sendToServer(new ServerboundSetBookmarkPacket(index));
    }

    protected void chooseMaterial(Holder<MaterialDefinition> materialDefinitionHolder) {
        this.cachedPage = new MaterialPage(materialDefinitionHolder);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, BOOK_LOCATION, leftPos, topPos, 0f, 0f, IMAGE_WIDTH, IMAGE_HEIGHT, 512, 256);
    }

    private int getMaxTitleWidth() {
        int averageCharSize = 5;
        int maxCharCount = 16;
        return averageCharSize * maxCharCount;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(guiGraphics, mouseX, mouseY, partialTick);
        int currentPageNumber = bookState.getGlobalPageNumber();
        if (lastPageNumber != currentPageNumber) {
            lastPageNumber = currentPageNumber;
            cachedPage = bookState.getCurrentPage();
        }
        int titleX = leftPos + XM - 2;
        int titleBottomY = topPos + YM / 2 + (int) (16 * itemScale);
        cachedPage.render(guiGraphics, titleX, titleBottomY, mouseX, mouseY, partialTick);
        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().translate(leftPos, topPos);
        for (GuideBookButton button : nativeButtons) {
            button.render(guiGraphics, button.boundingBox(leftPos, topPos).containsPoint(mouseX, mouseY), partialTick);
        }
        for (GuideBookButton button : cachedPage.extraButtons()) {
            button.render(guiGraphics, button.boundingBox(leftPos, topPos).containsPoint(mouseX, mouseY), partialTick);
        }
        guiGraphics.pose().popMatrix();
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        int mouseX = (int) event.x();
        int mouseY = (int) event.y();
        for (GuideBookButton button : nativeButtons) {
            int prevPage = bookState.getGlobalPageNumber() - 1;
            if (button.boundingBox(leftPos, topPos).containsPoint(mouseX, mouseY) && button.onClick(this.bookState)) {
                Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(button.getSound(), 1f));
                bookState.getGlobalPage(prevPage).onDepart();
                bookState.getCurrentPage().onArrive();
                return true;
            }
        }
        for (GuideBookButton button : cachedPage.extraButtons()) {
            if (button.boundingBox(leftPos, topPos).containsPoint(mouseX, mouseY) && button.onClick(this.bookState)) {
                Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(button.getSound(), 1f));
                return true;
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (super.mouseScrolled(mouseX, mouseY, scrollX, scrollY)) {
            return true;
        }
        if (scrollY < 0) {
            bookState.incrementPage();
        } else {
            bookState.decrementPage();
        }
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN, 1f));
        return true;
    }

    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        InputConstants.Key mouseKey = InputConstants.getKey(event);
        if (super.keyPressed(event)) {
            return true;
        } else if (this.minecraft.options.keyInventory.isActiveAndMatches(mouseKey)) {
            this.onClose();
            return true;
        }
        return false;
    }

    public GuideBookState createGuidebookState() {
        /* Guidebook is made of 3 sections:
            - Table of contents, listing all other sections
            - Materials, listing all registered and non-empty materials loaded into the world
            - Patterns, listing all registered patterns in the world */
        var materialRegistry = IronsJewelryRegistries.materialRegistry(Minecraft.getInstance().level.registryAccess());
        var patternRegistry = IronsJewelryRegistries.patternRegistry(Minecraft.getInstance().level.registryAccess());
        List<Page> materialPages = new ArrayList<>();
        // Construct a material page if the material exists and has valid items to be crafted from
        materialRegistry.listElements().filter(holder -> !holder.value().ingredient().isEmpty()).forEach(holder -> materialPages.add(new MaterialPage(holder)));
        // Effectively curry buttons by using preparation structure. Allows all information to be created now, and the button can be positioned and fit onto the screen later, because it is difficult to position all elements without knowing how many there are
        List<Function<TableOfContentsPage.EntryPreparation, GuideBookButton>> materialTableOfContentsEntries = new ArrayList<>();
        for (int i = 0; i < materialPages.size(); i++) {
            var materialPage = ((MaterialPage) materialPages.get(i));
            var material = materialPage.material;
            materialTableOfContentsEntries.add(preparation ->
                    new TextButton(preparation.x(), preparation.y(), preparation.width(), preparation.height(), Component.translatable(material.value().descriptionId()), 0xFF000000, ChatFormatting.YELLOW.getColor(),
                            material.value().ingredient().items().map(ItemStack::new).toList(), guidebook -> guidebook.navigateToPage(materialPage)));
        }
        materialPages.addAll(0, createTableOfContentsPages(Component.translatable("ui.irons_jewelry.guide_book.table_of_contents_materials"), materialTableOfContentsEntries, 75));

        List<Page> patternPages = new ArrayList<>();
        // Construct a pattern page for all patterns that exist
        patternRegistry.listElements().forEach(holder -> patternPages.add(new PatternPage(holder)));
        List<Function<TableOfContentsPage.EntryPreparation, GuideBookButton>> patternTableOfContentsEntries = new ArrayList<>();
        for (int i = 0; i < patternPages.size(); i++) {
            var patternPage = ((PatternPage) patternPages.get(i));
            var pattern = patternPage.pattern;
            patternTableOfContentsEntries.add(preparation ->
                    new TextButton(preparation.x(), preparation.y(), preparation.width(), preparation.height(), Component.translatable(pattern.value().descriptionId()), 0xFF000000, ChatFormatting.YELLOW.getColor(),
                            List.of(patternPage.itemIcon), guidebook -> guidebook.navigateToPage(patternPage)));
        }
        patternPages.addAll(0, createTableOfContentsPages(Component.translatable("ui.irons_jewelry.guide_book.table_of_contents_patterns"), patternTableOfContentsEntries, 75));

        List<Page> masterTableOfContentsPages = new ArrayList<>();
        // Create master table of content page set which directly links to material and pattern sections
        masterTableOfContentsPages.addAll(createTableOfContentsPages(Component.translatable("ui.irons_jewelry.guide_book.table_of_contents"),
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
                List.of(new GuideBookState.BookSection(masterTableOfContentsPages), new GuideBookState.BookSection(materialPages), new GuideBookState.BookSection(patternPages))
        );
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

    private int scaleColor(int color, float scalar) {
        var r = (int) Math.clamp((color >> 16 & 0xFF) * scalar, 0, 255);
        var g = (int) Math.clamp((color >> 8 & 0xFF) * scalar, 0, 255);
        var b = (int) Math.clamp((color & 0xFF) * scalar, 0, 255);
        return (r << 16) | (g << 8) | b;
    }

    private void drawLine(GuiGraphicsExtractor graphics, int thickness, int startX, int startY, int endX, int endY, int startColor, int endColor) {
        int halfThickness = thickness / 2;
        graphics.fill(startX, startY - halfThickness, endX, startY - halfThickness + Math.max(thickness, 1), startColor);
    }

    private int generateTextColor(Identifier palette) {
        try {
            Optional<Resource> resource = Minecraft.getInstance().getResourceManager().getResource(SpriteSource.TEXTURE_ID_CONVERTER.idToFile(palette));
            if (resource.isPresent()) {
                int[] aint;
                try (
                        InputStream inputstream = resource.get().open();
                        NativeImage nativeimage = NativeImage.read(inputstream);
                ) {
                    aint = nativeimage.getPixels();
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

    abstract class Page {
        abstract void render(GuiGraphicsExtractor guiGraphics, int titleX, int titleBottomY, int mouseX, int mouseY, float partialTick);

        void drawTitle(GuiGraphicsExtractor guiGraphics, Component text, int titleX, int titleBottomY, int color, boolean dropshadow, @Nullable CyclicItemRenderer itemRenderer) {
            var poseStack = guiGraphics.pose();
            poseStack.pushMatrix();
            float textScale = titleScale;
            int xOffset;
            if (itemRenderer == null) {
                xOffset = 3;
            } else {
                xOffset = (int) (18 * itemScale);
                itemRenderer.renderBottomLeft(guiGraphics, titleX, titleBottomY, itemScale);
            }
            textScale *= Math.clamp(getMaxTitleWidth() / (float) font.width(text), 0, 1);
            poseStack.scale(textScale, textScale);
            guiGraphics.text(font, text, (int) ((titleX + xOffset) / textScale), (int) ((titleBottomY - (2 * itemScale)) / textScale) - font.lineHeight, color, dropshadow);
            poseStack.popMatrix();
            int lineLength = Math.min((int) (xOffset + (font.width(text) + 24) * textScale), IMAGE_WIDTH - XM * 2);
            int lineThickness = 2;
            int split = 15;
            int color2 = color & 0x00FFFFFF;
            drawLine(guiGraphics, lineThickness, titleX - 3, titleBottomY, titleX + split, titleBottomY, color2, color);
            drawLine(guiGraphics, lineThickness, titleX + split, titleBottomY, titleX + lineLength, titleBottomY, color, color2);
        }

        void onArrive() {
        }

        void onDepart() {
        }

        List<? extends GuideBookButton> extraButtons() {
            return List.of();
        }
    }

    public class TableOfContentsPage extends Page {
        // fixme: yeah cool and all. get rid of this slop. half these are hardcoded and the other half shouldnt be final
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
        public void render(GuiGraphicsExtractor guiGraphics, int titleX, int titleBottomY, int mouseX, int mouseY, float partialTick) {
            // 0xFF8f756b - paper themed color
            // 0xFF923d34 - book leather themed color
            drawTitle(guiGraphics, title, titleX, titleBottomY, 0xFF8f756b, true, null);
        }

        @Override
        public List<? extends GuideBookButton> extraButtons() {
            return entries;
        }
    }

    public class MaterialPage extends Page {
        final Holder<MaterialDefinition> material;
        final int cachedTextColor;
        final CyclicItemRenderer itemRenderer;
        final List<TableEntry> bonusTable;

        record TableEntry(MutableComponent type, MutableComponent value, MutableComponent tooltip) {
        }

        public MaterialPage(Holder<MaterialDefinition> material) {
            this.material = material;
            this.cachedTextColor = generateTextColor(material.value().paletteLocation());
            this.itemRenderer = new CyclicItemRenderer(material.value().ingredient().items().map(ItemStack::new).toList());
            this.bonusTable = new ArrayList<>();
            bonusTable.add(new TableEntry(
                    Component.translatable("ui.irons_jewelry.quality"),
                    Component.literal("x").append(String.valueOf(material.value().quality())),
                    Component.translatable("ui.irons_jewelry.quality.description",
                            Component.translatable(material.value().descriptionId()).withColor(cachedTextColor),
                            Component.literal(String.valueOf(material.value().quality())).withColor(cachedTextColor)).withStyle(ChatFormatting.GRAY)
            ));
            for (var entry : material.value().bonusParameters().entrySet()) {
                IBonusParameterType param = entry.getKey();
                Object value = entry.getValue();
                Optional<Component> opt = param.getSimpleDescription(value);
                if (opt.isPresent()) {
                    var typeName = Component.translatable(param.getDescriptionId());
                    bonusTable.add(new TableEntry(
                            typeName,
                            opt.get().copy(),
                            Component.translatable("ui.irons_jewelry.bonus_type.description",
                                    typeName.copy().withColor(cachedTextColor),
                                    Component.translatable(material.value().descriptionId()).withColor(cachedTextColor),
                                    Component.literal(opt.get().copy().getString()).withColor(cachedTextColor)).withStyle(ChatFormatting.GRAY)
                    ));
                }
            }
        }

        @Override
        public void render(GuiGraphicsExtractor guiGraphics, int titleX, int titleBottomY, int mouseX, int mouseY, float partialTick) {
            MaterialDefinition material = this.material.value();
            Component name = Component.translatable(material.descriptionId());
            var poseStack = guiGraphics.pose();
            /*
            Draw Page Title: Ingredient Icon and Material Name
             */
            drawTitle(guiGraphics, name, titleX, titleBottomY, cachedTextColor, true, itemRenderer);
            /*
            Draw Page Body
             */
            int ypos = (int) (topPos + YM + font.lineHeight * (1 + itemScale) + font.lineHeight / 2f);
            int bonusTypeMaxWidth = bonusTable.stream().map(TableEntry::type).mapToInt(font::width).max().orElse(0);
            int valueColumMargin = bonusTypeMaxWidth + 8;
            int tooltipIndex = -1;
            int lightColor = scaleColor(cachedTextColor, 1.75f);
            int darkColor = scaleColor(cachedTextColor, 0.2f);
            for (int i = 0; i < bonusTable.size(); i++) {
                var entry = bonusTable.get(i);
                var type = entry.type;
                var value = Component.literal(entry.value.getString());
                int xpos = leftPos + XM;
                for (int x = -1; x <= 1; x++) {
                    for (int y = -1; y <= 1; y++) {
                        guiGraphics.text(font, type, xpos + x, ypos + y, darkColor, false);
                    }
                }
                guiGraphics.text(font, type, xpos, ypos, lightColor, false);
                if (mouseY >= ypos && mouseY <= ypos + font.lineHeight && mouseX >= xpos && mouseX <= xpos + font.width(type)) {
                    tooltipIndex = i;
                }
                int availableInfoWidth = IMAGE_WIDTH - XM * 2 - valueColumMargin;
                for (var line : font.split(value, availableInfoWidth)) {
                    guiGraphics.text(font, line, xpos + valueColumMargin, ypos, 0x0, false);
                    if (mouseY >= ypos && mouseY <= ypos + font.lineHeight &&
                            mouseX >= xpos + valueColumMargin && mouseX <= xpos + valueColumMargin + font.width(line)) {
                        tooltipIndex = i;
                    }
                    ypos += font.lineHeight;
                }
                if (i != bonusTable.size() - 1) {
                    ypos += 1;
                    int color = cachedTextColor;
                    int color2 = color & 0x00FFFFFF;
                    int split = (xpos + valueColumMargin + xpos) / 2;
                    drawLine(guiGraphics, 1, xpos - 10, ypos, split, ypos, color2, color);
                    drawLine(guiGraphics, 1, split, ypos, xpos + valueColumMargin + availableInfoWidth / 2, ypos, color, color2);
                    ypos += 2;
                }
            }
            if (tooltipIndex >= 0) {
                guiGraphics.setTooltipForNextFrame(font.split(bonusTable.get(tooltipIndex).tooltip, 250), mouseX, mouseY);
            }
        }
    }

    public class PatternPage extends Page {
        class PartSelectionButton implements GuideBookButton {
            ScreenRectangle rectangle;
            final int navigateIndex;

            PartSelectionButton(int navigateIndex) {
                this.navigateIndex = navigateIndex;
                this.rectangle = ScreenRectangle.of(ScreenAxis.HORIZONTAL, 0, 0, 0, 0);
            }

            @Override
            public ScreenRectangle boundingBox() {
                return rectangle;
            }

            @Override
            public void render(GuiGraphicsExtractor guiGraphics, boolean selected, float partialTick) {
                return;
            }

            @Override
            public boolean onClick(GuideBookState state) {
                if (selectedPartIndex == this.navigateIndex) {
                    selectedPartIndex = -1;
                } else {
                    selectedPartIndex = this.navigateIndex;
                }
                return true;
            }
        }

        record PartInfo(TextureAtlasSprite sprite,
                        List<FormattedCharSequence> tooltip, MutableComponent name,
                        List<MutableComponent> expandedInfo, boolean primary,
                        PartSelectionButton button) implements GuideBookButton {
            private static final Identifier INPUT_SLOT = IronsJewelry.id("guidebook/guidebook_part_frame");

            void render(GuiGraphicsExtractor guiGraphics, int x, int y, int width, int mouseX, int mouseY, float partialTick) {
                List<FormattedCharSequence> tooltipToRender = null;
                int slotMargin = 4;
                int size = 24;
                guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, INPUT_SLOT, x, y, size, size);
                guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, x + slotMargin, y + slotMargin, 16, 16);
                if (mouseX >= x && mouseX <= x + size && mouseY >= y && mouseY <= y + size) {
                    tooltipToRender = tooltip;
                }
                int textMargin = 3;
                x += size + textMargin;
                var font = Minecraft.getInstance().font;
                int ypos = y;
                // Name
                int textWidth = width - size - textMargin;
                for (var line : font.split(name, textWidth)) {
                    guiGraphics.text(font, line, x, ypos, 0x0, true);
                    ypos += font.lineHeight;
                }
                if (primary) {
                    ypos += 1;
                    for (var line : font.split(Component.translatable("ui.irons_jewelry.primary_part").withStyle(ChatFormatting.ITALIC).withColor(0xFF222233), textWidth)) {
                        guiGraphics.text(font, line, x, ypos, 0x0, false);
                        ypos += font.lineHeight;
                    }
                }
                if (tooltipToRender != null) {
                    guiGraphics.setTooltipForNextFrame(tooltipToRender, mouseX, mouseY);
                }
            }

            @Override
            public ScreenRectangle boundingBox() {
                return button.boundingBox();
            }

            @Override
            public void render(GuiGraphicsExtractor guiGraphics, boolean selected, float partialTick) {
                button.render(guiGraphics, selected, partialTick);
            }

            @Override
            public boolean onClick(GuideBookState state) {
                return button.onClick(state);
            }

            @Override
            public SoundEvent getSound() {
                return SoundEvents.NOTE_BLOCK_HAT.value();
            }
        }

        final static CyclicItemRenderer SCROLL_RENDERER = new CyclicItemRenderer(List.of(ItemRegistry.RECIPE.get().getDefaultInstance()));
        final Holder<PatternDefinition> pattern;
        final ItemStack itemIcon;
        final CyclicItemRenderer itemRenderer;
        final List<PartInfo> partInfo;
        final List<MutableComponent> overviewInfo;
        final List<FormattedCharSequence> patternTooltip;
        final int titleColor;
        int selectedPartIndex = -1;

        public PatternPage(Holder<PatternDefinition> pattern) {
            this.pattern = pattern;
            this.itemIcon = createPatternItem(pattern);
            this.itemRenderer = new CyclicItemRenderer(List.of(itemIcon));
            JewelryData data = JewelryData.get(itemIcon);
            var partKeys = pattern.value().partTemplate().stream().sorted(Comparator.comparingInt(PartIngredient::drawOrder)).toList();
            this.titleColor = generateTextColor(data.parts().get(partKeys.get(0).part()).value().paletteLocation());
            this.partInfo = createPartInfo(data, partKeys);
            this.overviewInfo = createOverviewInfo(pattern, titleColor);
            this.patternTooltip = createPatternTooltip();
        }

        private List<FormattedCharSequence> createPatternTooltip() {
            ArrayList<MutableComponent> tooltip = new ArrayList<>();
            Component name = Component.translatable(pattern.value().descriptionId()).withColor(titleColor);
            if (pattern.value().unlockedByDefault()) {
                tooltip.add(Component.translatable("ui.irons_jewelry.artisan_scroll_description.unlocked", name, Component.translatable("block.irons_jewelry.jewelcrafting_station").withColor(titleColor)).withStyle(ChatFormatting.GRAY));
            } else {
                tooltip.add(Component.translatable("ui.irons_jewelry.artisan_scroll_description.locked", name, Component.translatable("item.irons_jewelry.recipe").withColor(titleColor)).withStyle(ChatFormatting.GRAY));
                tooltip.add(Component.empty());
                String descriptionId = pattern.value().descriptionId() + ".guide";
                if (I18n.exists(descriptionId)) {
                    tooltip.add(Component.translatable(descriptionId).withStyle(ChatFormatting.GRAY));
                } else {
                    tooltip.add(Component.translatable("ui.irons_jewelry.artisan_scroll_description.unknown").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
                }
            }
            return tooltip.stream().flatMap(c -> c.getContents() == PlainTextContents.EMPTY ? Stream.of(FormattedCharSequence.EMPTY) : Minecraft.getInstance().font.split(c, 250).stream()).toList();
        }

        private List<PartInfo> createPartInfo(JewelryData jewelryData, List<PartIngredient> partIngredients) {
            ArrayList<PartInfo> partInfo = new ArrayList<>(pattern.value().partTemplate().size());
            var handler = AssetHandlerRegistry.JEWELRY_HANDLER.get();
            for (int i = 0; i < partIngredients.size(); i++) {
                var partIngredient = partIngredients.get(i);
                var part = partIngredient.part();
                List<Component> tooltip = new ArrayList<>();
                var material = jewelryData.parts().get(part);
                boolean isPrimaryPart = pattern.value().partForQuality().map(holder -> holder == part).orElse(false);
                MutableComponent name = Component.translatable(part.value().descriptionId()).withStyle(ChatFormatting.UNDERLINE).withColor(generateTextColor(material.value().paletteLocation()));
                tooltip.add(name);
                tooltip.add(Component.literal(" ").append(Component.translatable("tooltip.irons_jewelry.material_cost", Component.literal(String.valueOf(partIngredient.materialCost())).withStyle(ChatFormatting.WHITE))).withStyle(ChatFormatting.GRAY));
                tooltip.add(Component.translatable("tooltip.irons_jewelry.applicable_materials").withStyle(ChatFormatting.YELLOW, ChatFormatting.UNDERLINE));
                IronsJewelryRegistries.materialRegistry(Minecraft.getInstance().level.registryAccess()).listElements().filter(candidate -> !candidate.value().ingredient().isEmpty() && part.value().canUseMaterial(candidate))
                        .forEach(m -> tooltip.add(Component.literal(" ").append(Component.translatable(m.value().descriptionId())).withStyle(ChatFormatting.GRAY)));
                List<MutableComponent> partExpandedInfo = new ArrayList<>();
                partExpandedInfo.add(name);
                if (isPrimaryPart) {
                    partExpandedInfo.add(Component.empty());
                    partExpandedInfo.add(Component.translatable("ui.irons_jewelry.primary_part_header").withStyle(ChatFormatting.YELLOW, ChatFormatting.UNDERLINE));
                    partExpandedInfo.add(Component.translatable("ui.irons_jewelry.primary_part.description").withStyle(ChatFormatting.GRAY));
                }
                partExpandedInfo.add(Component.empty());
                partExpandedInfo.add(Component.translatable("tooltip.irons_jewelry.material_cost_header").withStyle(ChatFormatting.YELLOW, ChatFormatting.UNDERLINE));
                partExpandedInfo.add(Component.translatable("tooltip.irons_jewelry.material_cost", partIngredient.materialCost()).withStyle(ChatFormatting.GRAY));
                double patternBonus = pattern.value().qualityMultiplier();
                if (!partIngredient.bonuses().isEmpty()) {
                    partExpandedInfo.add(Component.empty());
                    partExpandedInfo.add(Component.translatable("tooltip.irons_jewelry.bonus_from_part_header").withStyle(ChatFormatting.YELLOW, ChatFormatting.UNDERLINE));
                    for (var bonus : partIngredient.bonuses()) {
                        MutableComponent component;
                        if (bonus.parameterValue().containsKey(bonus.bonusType().getParameterType())) {
                            // preset bonus
                            var bonusType = bonus.bonusType();
                            component = Component.translatable("tooltip.irons_jewelry.bonus_with_direct_source", net.minecraft.network.chat.Component.translatable(bonusType.getDescriptionId()),
                                    bonusType.getParameterType().getSimpleDescriptionCast(bonus.parameterValue().get(bonusType.getParameterType())).orElse(Component.empty()));
                        } else {
                            component = Component.translatable(bonus.bonusType().getDescriptionId());
                        }
                        if (patternBonus * bonus.qualityMultiplier() != 1.0 && bonus.bonusType().getParameterType() != ParameterTypeRegistry.EMPTY.get()) {
                            component = component.append(Component.literal(String.format(" (x%s)", patternBonus * bonus.qualityMultiplier())));
                        }
                        partExpandedInfo.add(component.withStyle(ChatFormatting.GRAY));
                    }
                }
                partInfo.add(new PartInfo(
                        handler.getSprite(handler.getSpriteLocation(part, material)),
                        Utils.rasterizeComponentList(tooltip),
                        name,
                        partExpandedInfo,
                        isPrimaryPart,
                        new PartSelectionButton(i))
                );
            }
            return partInfo;
        }

        private List<MutableComponent> createOverviewInfo(Holder<PatternDefinition> pattern, int titleColor) {
            final List<MutableComponent> overviewInfo;
            overviewInfo = new ArrayList<>();
            overviewInfo.add(Component.translatable("tooltip.irons_jewelry.overview_header").withStyle(ChatFormatting.UNDERLINE).withColor(titleColor));
            var bonusTooltip = this.pattern.value().getPatternBonusesTooltip();
            bonusTooltip.set(0, Component.translatable("tooltip.irons_jewelry.bonus_crafted_header").withStyle(ChatFormatting.YELLOW, ChatFormatting.UNDERLINE)); // replace header
            overviewInfo.add(Component.empty());
            overviewInfo.add(Component.translatable("tooltip.irons_jewelry.jewelry_type_header").withStyle(ChatFormatting.YELLOW, ChatFormatting.UNDERLINE));
            overviewInfo.add(Component.translatable(pattern.value().jewelryType().getCuriosSlotIdentifier().map(s -> String.format("curios.identifier.%s", s)).orElse("Unknown")).withStyle(ChatFormatting.GRAY));
            overviewInfo.add(Component.empty());
            overviewInfo.addAll(bonusTooltip);
            overviewInfo.add(Component.empty());
            overviewInfo.add(Component.empty());
            overviewInfo.add(Component.translatable("ui.irons_jewelry.guide_book.pattern_select_hint").withStyle(ChatFormatting.ITALIC).withColor(titleColor));
            return overviewInfo;
        }

        @Override
        void onDepart() {
            selectedPartIndex = -1;
        }

        @Override
        public void render(GuiGraphicsExtractor guiGraphics, int titleX, int titleBottomY, int mouseX, int mouseY, float partialTick) {
            PatternDefinition pattern = this.pattern.value();
            Component name = Component.translatable(pattern.descriptionId());
            /*
            Draw Page Title: Icon and Name
             */
            drawTitle(guiGraphics, name, titleX, titleBottomY, titleColor, true, itemRenderer);
            /*
            Draw Part Info
             */
            int partSectionX = titleX;
            int partSectionY = titleBottomY + 4;
            int partSectionWidth = 100;
            int titleSpacer = font.lineHeight * 3 / 2;
            guiGraphics.text(font, Component.translatable("tooltip.irons_jewelry.parts_header").withStyle(ChatFormatting.UNDERLINE), partSectionX, partSectionY, 0x0, false);
            int yoff = 0;
            for (int i = 0; i < partInfo.size(); i++) {
                //fixme: this has no y bounding condition
                int x = partSectionX;
                int y = partSectionY + titleSpacer + yoff;
                var info = partInfo.get(i);
                info.button.rectangle = ScreenRectangle.of(ScreenAxis.HORIZONTAL, x - leftPos, y - topPos, partSectionWidth, 24);
                if (i == selectedPartIndex) {
                    int bgColor = 0xBB260f0c;
                    int borderStart = 0xDDe0ca9f;
                    int borderEnd = 0xEEa09172;
                    guiGraphics.fill(x, y, x + partSectionWidth - 5, y + 24, bgColor);
                    guiGraphics.fill(x, y, x + partSectionWidth - 5, y + 1, borderStart);
                    guiGraphics.fill(x, y + 23, x + partSectionWidth - 5, y + 24, borderEnd);
                    guiGraphics.fill(x - 1, y, x, y + 24, borderStart);
                    guiGraphics.fill(x + partSectionWidth - 5, y, x + partSectionWidth - 4, y + 24, borderEnd);
                }
                info.render(guiGraphics, x, y, partSectionWidth, mouseX, mouseY, partialTick);
                yoff += 32;
            }
            /*
            Draw Info Panel
             */
            int infoSectionX = titleX + partSectionWidth + 1;
            int infoSectionY = partSectionY + 3;
            int infoSectionWidth = IMAGE_WIDTH - 11 - (infoSectionX - leftPos);
            int infoSectionHeight = IMAGE_HEIGHT - YM * 2 - (infoSectionY - topPos);
            int bgColor = 0xDD260f0c;
            int borderStart = 0xFFe0ca9f;
            int borderEnd = 0xFFa09172;
            guiGraphics.fill(infoSectionX, infoSectionY + 1, infoSectionX + infoSectionWidth - 2, infoSectionY + infoSectionHeight - 1, bgColor);
            guiGraphics.fill(infoSectionX, infoSectionY + 1, infoSectionX + infoSectionWidth - 2, infoSectionY + 2, borderStart);
            guiGraphics.fill(infoSectionX, infoSectionY + infoSectionHeight - 2, infoSectionX + infoSectionWidth - 2, infoSectionY + infoSectionHeight - 1, borderEnd);
            guiGraphics.fill(infoSectionX - 1, infoSectionY + 1, infoSectionX, infoSectionY + infoSectionHeight - 1, borderStart);
            guiGraphics.fill(infoSectionX + infoSectionWidth - 2, infoSectionY + 1, infoSectionX + infoSectionWidth - 1, infoSectionY + infoSectionHeight - 1, borderEnd);
            List<MutableComponent> infoPage = overviewInfo;
            if (selectedPartIndex >= 0 && selectedPartIndex < partInfo.size()) {
                infoPage = partInfo.get(selectedPartIndex).expandedInfo;
            }
            int emptyMargin = 4;
            TextStack stack = TextStack.textStack(new LinkedList<>(infoPage), emptyMargin);
            float textScale = 1;
            while (textScale > 0.1 && stack.getTotalHeight(font, (int) (infoSectionWidth / textScale)) * textScale > infoSectionHeight - font.lineHeight) {
                textScale -= .1f;
            }
            var poseStack = guiGraphics.pose();
            poseStack.pushMatrix();
            poseStack.translate(infoSectionX, infoSectionY);
            poseStack.scale(textScale, textScale);
            int y = 0;
            for (var component : infoPage) {
                if (component.getContents() == PlainTextContents.EMPTY) {
                    y += emptyMargin;
                } else {
                    for (var line : font.split(component, (int) (infoSectionWidth / textScale))) {
                        guiGraphics.text(font, line, 0, y, -1, true);
                        y += font.lineHeight + 1;
                    }
                }
            }
            poseStack.popMatrix();
            /*
            Artisan Scroll Helper
             */
            poseStack.pushMatrix();
            float scale = itemScale * 0.75f;
            int itemX = (int) (leftPos + IMAGE_WIDTH - XM - 16 * scale);
            SCROLL_RENDERER.renderBottomLeft(guiGraphics, itemX, titleBottomY, scale);
            poseStack.scale(scale, scale);
            guiGraphics.text(font, "?", (int) ((itemX + 22) / scale), (int) ((titleBottomY - font.lineHeight * scale) / scale), 0x0, false);
            poseStack.popMatrix();
            if (mouseX >= itemX && mouseX <= itemX + 22 * scale && mouseY >= titleBottomY - 16 * scale && mouseY <= titleBottomY) {
                guiGraphics.setTooltipForNextFrame(patternTooltip, mouseX, mouseY);
            }
        }

        static abstract class TextStack {
            private static TextStack textStack(Queue<? extends net.minecraft.network.chat.Component> lines, int space) {
                if (lines.isEmpty()) {
                    return null;
                }
                var component = lines.poll();
                TextStack textStack;
                if (component.getContents() == PlainTextContents.EMPTY) {
                    textStack = new Space(space);
                } else {
                    textStack = new Component(component);
                }
                textStack.child = textStack(lines, space);
                return textStack;
            }

            @Nullable TextStack child;

            abstract int getHeight(Font font, int width);

            int getTotalHeight(Font font, int width) {
                return getHeight(font, width) + (child == null ? 0 : child.getTotalHeight(font, width));
            }

            static class Space extends TextStack {
                final int space;

                Space(int space) {
                    this.space = space;
                }

                @Override
                int getHeight(Font font, int width) {
                    return space;
                }
            }

            static class Component extends TextStack {
                final net.minecraft.network.chat.Component component;

                Component(net.minecraft.network.chat.Component component) {
                    this.component = component;
                }

                @Override
                int getHeight(Font font, int width) {
                    return font.split(component, width).size() * font.lineHeight;
                }
            }
        }

        private static ItemStack createPatternItem(Holder<PatternDefinition> pattern) {
            try {
                ItemStack item = new ItemStack(pattern.value().jewelryType().item());
                Holder<MaterialDefinition> renderMaterial = null;
                var materialRegistry = IronsJewelryRegistries.materialRegistry(Minecraft.getInstance().level.registryAccess());
                var metal = materialRegistry.get(IronsJewelry.id("gold")).get();
                var gem = materialRegistry.get(IronsJewelry.id("diamond")).get();
                Map<Holder<PartDefinition>, Holder<MaterialDefinition>> parts = new HashMap<>();
                for (var partIngredient : pattern.value().partTemplate()) {
                    var part = partIngredient.part();
                    if (part.value().canUseMaterial(metal)) {
                        renderMaterial = metal;
                    } else if (part.value().canUseMaterial(gem)) {
                        renderMaterial = gem;
                    } else {
                        for (Holder.Reference<MaterialDefinition> materialDefinition : materialRegistry.listElements().toList()) {
                            if (part.value().canUseMaterial(materialDefinition)) {
                                renderMaterial = materialDefinition;
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

        @Override
        List<? extends GuideBookButton> extraButtons() {
            return (List) partInfo;
        }
    }

    class BookmarkButton implements GuideBookButton {
        final int x, y, width, height;
        final Identifier sprite, spriteActive;
        int bookmark;

        public BookmarkButton(int x, int y, int width, int height, Identifier sprite, Identifier spriteActive) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.sprite = sprite;
            this.spriteActive = spriteActive;
            this.bookmark = PlayerData.get(Minecraft.getInstance().player).getBookmarkIndex();
        }

        @Override
        public ScreenRectangle boundingBox() {
            return ScreenRectangle.of(ScreenAxis.HORIZONTAL, x, y, width, height);
        }

        @Override
        public void render(GuiGraphicsExtractor guiGraphics, boolean selected, float partialTick) {
            boolean active = bookmark >= 0 && bookmark == bookState.getGlobalPageNumber() - 1;
            Identifier sprite = active ? this.spriteActive : this.sprite;
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, x, y, width, height);
        }

        @Override
        public boolean onClick(GuideBookState state) {
            PlayerData data = PlayerData.get(Minecraft.getInstance().player);
            int currentPageIndex = state.getGlobalPageNumber() - 1;
            int bookmarkedIndex = data.getBookmarkIndex();
            if (currentPageIndex != bookmarkedIndex) {
                setBookmark(currentPageIndex);
                bookmark = currentPageIndex;
            } else {
                setBookmark(-1);
                bookmark = -1;
            }
            return true;
        }
    }

}
