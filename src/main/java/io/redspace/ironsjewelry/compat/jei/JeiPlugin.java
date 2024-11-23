package io.redspace.ironsjewelry.compat.jei;

import io.redspace.ironsjewelry.IronsJewelry;
import io.redspace.ironsjewelry.block.jewelcrafting_station.JewelcraftingStationScreen;
import io.redspace.ironsjewelry.core.data.JewelryData;
import io.redspace.ironsjewelry.registry.BlockRegistry;
import io.redspace.ironsjewelry.registry.IronsJewelryRegistries;
import io.redspace.ironsjewelry.registry.ItemRegistry;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.ingredients.subtypes.IIngredientSubtypeInterpreter;
import mezz.jei.api.registration.*;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

@mezz.jei.api.JeiPlugin
public class JeiPlugin implements IModPlugin {
    private static final ResourceLocation ID = IronsJewelry.id("jei_plugin");

    @Override
    public ResourceLocation getPluginUid() {
        return ID;
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addGenericGuiContainerHandler(JewelcraftingStationScreen.class, new JewelcraftingJeiGuiHandler());
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new JewelcraftingJeiRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registration.addRecipes(JewelcraftingJeiRecipeCategory.RECIPE_TYPE, IronsJewelryRegistries.patternRegistry(Minecraft.getInstance().level.registryAccess()).stream().toList());
    }

    @Override
    public void registerItemSubtypes(ISubtypeRegistration registration) {
        registration.registerSubtypeInterpreter(ItemRegistry.RING.get(), JEWELRY_INTERPRETER);
        registration.registerSubtypeInterpreter(ItemRegistry.NECKLACE.get(), JEWELRY_INTERPRETER);
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(BlockRegistry.JEWELCRAFTING_STATION_BLOCK.get()), JewelcraftingJeiRecipeCategory.RECIPE_TYPE);
    }

    public static final IIngredientSubtypeInterpreter<ItemStack> JEWELRY_INTERPRETER = (stack, context) -> {
        return JewelryData.get(stack).pattern().getKey().location().toString();
    };
}

