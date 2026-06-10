package io.redspace.ironsjewelry.compat.jei;

import io.redspace.ironsjewelry.IronsJewelry;
import io.redspace.ironsjewelry.core.data.MaterialDefinition;
import io.redspace.ironsjewelry.core.data.PatternDefinition;
import io.redspace.ironsjewelry.registry.BlockRegistry;
import io.redspace.ironsjewelry.registry.IronsJewelryRegistries;
import io.redspace.ironsjewelry.utils.Utils;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.AbstractRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.Arrays;
import java.util.List;

public class JewelcraftingJeiRecipeCategory extends AbstractRecipeCategory<PatternDefinition> {
    public static final IRecipeType<PatternDefinition> RECIPE_TYPE = IRecipeType.create(IronsJewelry.MODID, "jewelcrafting", PatternDefinition.class);

    private final IDrawable background;
    private static final int buffer = 32;
    private static final int WIDTH = 127;
    private static final int HEIGHT = 60;

    public JewelcraftingJeiRecipeCategory(IGuiHelper guiHelper) {
        super(
                RECIPE_TYPE,
                Component.translatable("container.irons_jewelry.jewelcrafting_station"),
                guiHelper.createDrawableItemStack(new ItemStack(BlockRegistry.JEWELCRAFTING_STATION_BLOCK.get())),
                WIDTH + buffer,
                HEIGHT
        );
        background = guiHelper.drawableBuilder(IronsJewelry.id("textures/gui/sprites/jewelcrafting_station/jei_bg.png"), 0, 0, WIDTH, HEIGHT)
                .addPadding(0, 0, buffer, 0)
                .setTextureSize(WIDTH, HEIGHT)
                .build();
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, PatternDefinition recipe, IFocusGroup focuses) {
        var materialRegistry = IronsJewelryRegistries.materialRegistry(Minecraft.getInstance().level.registryAccess());
        ItemStack output = Utils.createExampleJewelryItem(Minecraft.getInstance().level.registryAccess(), IronsJewelryRegistries.patternRegistry(Minecraft.getInstance().level.registryAccess()).wrapAsHolder(recipe));
        IRecipeSlotBuilder outputSlot = builder.addSlot(RecipeIngredientRole.OUTPUT, buffer + 105, 22)
                .addItemStacks(List.of(output))
                .setSlotName("output");

        int totalWidth = 95;
        int count = recipe.partTemplate().size();
        int widthPer = 20;
        int leftPos = (totalWidth - count * widthPer) / 2;
        var template = recipe.partTemplate();
        for (int i = 0; i < template.size(); i++) {
            var partIngredient = template.get(i);
            var part = partIngredient.part().value();
            var stacks = materialRegistry.listElements().filter(part::canUseMaterial).map(Holder::value).map(MaterialDefinition::ingredient).filter(ingr -> !ingr.isEmpty()).flatMap(ingredient -> ingredient.items().map(ItemStack::new)).toList();
            stacks.forEach(stack -> stack.setCount(partIngredient.materialCost()));
            builder.addSlot(RecipeIngredientRole.INPUT, buffer + leftPos + i * widthPer, 9 + 5 + 6)
                    .addItemStacks(stacks)
                    .setSlotName("input" + i);
        }
    }

    @Override
    public void draw(PatternDefinition recipe, IRecipeSlotsView recipeSlotsView, GuiGraphicsExtractor guiGraphics, double mouseX, double mouseY) {
        background.draw(guiGraphics);
        Component title = Component.translatable(recipe.descriptionId()).withStyle(ChatFormatting.UNDERLINE);
        int titleWidth = Minecraft.getInstance().font.width(title);
        int x = buffer + 47 - titleWidth / 2;
        int y = 4;
        int bgColor = 0xb4260f0c;
        int borderColor = 0x50e0ca9f;
        guiGraphics.fill(x - 2, y - 1, x + titleWidth + 2, y + 10, bgColor);
        guiGraphics.fill(x - 3, y - 1, x - 2, y + 10, borderColor);
        guiGraphics.fill(x + titleWidth + 2, y - 1, x + titleWidth + 3, y + 10, borderColor);
        guiGraphics.fill(x - 2, y - 2, x + titleWidth + 2, y - 1, borderColor);
        guiGraphics.fill(x - 2, y + 10, x + titleWidth + 2, y + 11, borderColor);
        guiGraphics.text(Minecraft.getInstance().font, title, x, y, 0xFFFFFF, true);
    }
}
