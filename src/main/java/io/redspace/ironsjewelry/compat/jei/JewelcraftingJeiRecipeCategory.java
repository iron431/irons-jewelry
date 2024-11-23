package io.redspace.ironsjewelry.compat.jei;

import io.redspace.ironsjewelry.IronsJewelry;
import io.redspace.ironsjewelry.core.data.JewelryData;
import io.redspace.ironsjewelry.core.data.MaterialDefinition;
import io.redspace.ironsjewelry.core.data.PartIngredient;
import io.redspace.ironsjewelry.core.data.PatternDefinition;
import io.redspace.ironsjewelry.registry.BlockRegistry;
import io.redspace.ironsjewelry.registry.ComponentRegistry;
import io.redspace.ironsjewelry.registry.IronsJewelryRegistries;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.TooltipRenderUtil;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class JewelcraftingJeiRecipeCategory implements IRecipeCategory<PatternDefinition> {
    public static final RecipeType<PatternDefinition> RECIPE_TYPE = RecipeType.create(IronsJewelry.MODID, "jewelcrafting", PatternDefinition.class);

    private final IDrawable background;
    private final IDrawable icon;
    private static final int buffer = 32;
    private static final int width = 127;
    private static final int height = 60;

    public JewelcraftingJeiRecipeCategory(IGuiHelper guiHelper) {
        background = guiHelper.drawableBuilder(IronsJewelry.id("textures/gui/sprites/jewelcrafting_station/jei_bg.png"), 0, 0, width, height)
                .addPadding(0, 0, buffer, 0)
                .setTextureSize(width, height)
                .build();
        icon = guiHelper.createDrawableItemStack(new ItemStack(BlockRegistry.JEWELCRAFTING_STATION_BLOCK.get()));
    }

    @Override
    public RecipeType<PatternDefinition> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("container.irons_jewelry.jewelcrafting_station");
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public @Nullable IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, PatternDefinition recipe, IFocusGroup focuses) {
        var materialRegistry = IronsJewelryRegistries.materialRegistry(Minecraft.getInstance().level.registryAccess());
        ItemStack output = new ItemStack(recipe.jewelryType().item());
        Holder<MaterialDefinition> iron = materialRegistry.getHolder(IronsJewelry.id("platinum")).get();
        var parts = recipe.partTemplate().stream().map(PartIngredient::part).collect(Collectors.toMap(Function.identity(),
                (p) -> iron));
        JewelryData jewelryData = JewelryData.renderable(IronsJewelryRegistries.patternRegistry(Minecraft.getInstance().level.registryAccess()).wrapAsHolder(recipe), parts);
        output.set(ComponentRegistry.JEWELRY_COMPONENT, jewelryData);
        IRecipeSlotBuilder outputSlot = builder.addSlot(RecipeIngredientRole.OUTPUT, buffer + 105, 22)
                .addItemStacks(List.of(output))
                .setSlotName("output");
        int totalWidth = 95;
        int count = parts.size();
        int widthPer = 20;
        int leftPos = (totalWidth - count * widthPer) / 2;
        var template = recipe.partTemplate();
        for (int i = 0; i < template.size(); i++) {
            var partIngredient = template.get(i);
            var part = partIngredient.part().value();
            var stacks = materialRegistry.stream().filter(material -> part.canUseMaterial(material.materialType())).map(MaterialDefinition::ingredient).filter(ingr -> !ingr.hasNoItems()).flatMap(ingredient -> Arrays.stream(ingredient.getItems())).toList();
            stacks.forEach(stack -> stack.setCount(partIngredient.materialCost()));
            builder.addSlot(RecipeIngredientRole.INPUT, buffer + leftPos + i * widthPer, 9 + 5 + 6)
                    .addItemStacks(stacks)
                    .setSlotName("input" + i);
        }
    }

    @Override
    public void draw(PatternDefinition recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        IRecipeCategory.super.draw(recipe, recipeSlotsView, guiGraphics, mouseX, mouseY);
        Component title = Component.translatable(recipe.descriptionId()).withStyle(ChatFormatting.UNDERLINE);
        int width = Minecraft.getInstance().font.width(title);
        int x = buffer + 47 - width / 2;
        int y = 4;
        var bgstart = 0xb4260f0c;//0xf0511d17;//0xf0100010;
        var bgend = bgstart;//0xf0361d17;//bgstart;
        var borderstart = 0x50e0ca9f;//0x505000FF;
        var borderend = 0x50a09172;//0x5028007f;
        guiGraphics.drawManaged(() -> TooltipRenderUtil.renderTooltipBackground(guiGraphics, x, y, width, 9, 0, bgstart, bgend, borderstart, borderend));
        guiGraphics.drawString(Minecraft.getInstance().font, title, x, y, 0xFFFFFF, true);
    }
}
