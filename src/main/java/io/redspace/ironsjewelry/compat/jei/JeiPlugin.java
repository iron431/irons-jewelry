package io.redspace.ironsjewelry.compat.jei;

import io.redspace.ironsjewelry.IronsJewelry;
import io.redspace.ironsjewelry.block.jewelcrafting_station.JewelcraftingStationScreen;
import io.redspace.ironsjewelry.core.data.JewelryData;
import io.redspace.ironsjewelry.core.data.MaterialDefinition;
import io.redspace.ironsjewelry.core.data.PartIngredient;
import io.redspace.ironsjewelry.core.data.PatternDefinition;
import io.redspace.ironsjewelry.registry.BlockRegistry;
import io.redspace.ironsjewelry.registry.ComponentRegistry;
import io.redspace.ironsjewelry.registry.IronsJewelryRegistries;
import io.redspace.ironsjewelry.registry.ItemRegistry;
import io.redspace.ironsjewelry.utils.Utils;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.ingredients.subtypes.ISubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.registration.*;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

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
    public void registerExtraIngredients(IExtraIngredientRegistration registration) {
        var materialRegistry = IronsJewelryRegistries.materialRegistry(Minecraft.getInstance().level.registryAccess());
        var patternRegistry = IronsJewelryRegistries.patternRegistry(Minecraft.getInstance().level.registryAccess());
        List<ItemStack> exampleJewelryItems = new ArrayList<>();
        for (PatternDefinition pattern : patternRegistry) {
            exampleJewelryItems.add(Utils.createExampleJewelryItem(Minecraft.getInstance().level.registryAccess(), patternRegistry.wrapAsHolder(pattern)));
        }
        registration.addExtraItemStacks(exampleJewelryItems);
    }

    @Override
    public void registerItemSubtypes(ISubtypeRegistration registration) {
        registration.registerSubtypeInterpreter(ItemRegistry.RING.get(), JEWELRY_INTERPRETER);
        registration.registerSubtypeInterpreter(ItemRegistry.NECKLACE.get(), JEWELRY_INTERPRETER);
        registration.registerSubtypeInterpreter(ItemRegistry.RECIPE.get(), PATTERN_INTERPRETER);
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(BlockRegistry.JEWELCRAFTING_STATION_BLOCK.get()), JewelcraftingJeiRecipeCategory.RECIPE_TYPE);
    }

    @Override
    public void registerAdvanced(IAdvancedRegistration registration) {
        registration.addTypedRecipeManagerPlugin(JewelcraftingJeiRecipeCategory.RECIPE_TYPE, new AdvancedPatternJeiHandler());
    }

    public static final ISubtypeInterpreter<ItemStack> JEWELRY_INTERPRETER = new ISubtypeInterpreter<ItemStack>() {
        @Override
        public @Nullable String getSubtypeData(ItemStack ingredient, UidContext context) {
            var data = JewelryData.get(ingredient);
            if (data.isValid()) {
                var pattern = data.pattern().getKey();
                if (pattern != null) {
                    return pattern.location().toString();
                }
            }
            return null;
        }

        @Override
        public String getLegacyStringSubtypeInfo(ItemStack ingredient, UidContext context) {
            return getSubtypeData(ingredient, context);
        }
    };
    public static final ISubtypeInterpreter<ItemStack> PATTERN_INTERPRETER = new ISubtypeInterpreter<ItemStack>() {
        @Override
        public @Nullable String getSubtypeData(ItemStack ingredient, UidContext context) {
            var pattern = ingredient.get(ComponentRegistry.STORED_PATTERN);
            if (pattern != null && pattern.getKey() != null) {
                return pattern.getKey().location().toString();
            }

            return null;
        }

        @Override
        public String getLegacyStringSubtypeInfo(ItemStack ingredient, UidContext context) {
            return getSubtypeData(ingredient, context);
        }
    };
}

