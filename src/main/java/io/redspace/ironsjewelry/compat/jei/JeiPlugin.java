package io.redspace.ironsjewelry.compat.jei;

import io.redspace.ironsjewelry.IronsJewelry;
import io.redspace.ironsjewelry.block.jewelcrafting_station.JewelcraftingStationScreen;
import io.redspace.ironsjewelry.core.data.JewelryData;
import io.redspace.ironsjewelry.core.data.PatternDefinition;
import io.redspace.ironsjewelry.core.data.StoredPatternData;
import io.redspace.ironsjewelry.registry.BlockRegistry;
import io.redspace.ironsjewelry.registry.IronsJewelryRegistries;
import io.redspace.ironsjewelry.registry.ItemRegistry;
import io.redspace.ironsjewelry.utils.Utils;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.ingredients.subtypes.ISubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.registration.IAdvancedRegistration;
import mezz.jei.api.registration.IExtraIngredientRegistration;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@mezz.jei.api.JeiPlugin
public class JeiPlugin implements IModPlugin {
    private static final Identifier ID = IronsJewelry.id("jei_plugin");

    @Override
    public Identifier getPluginUid() {
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
        registration.addSimpleRecipeManagerPlugin(JewelcraftingJeiRecipeCategory.RECIPE_TYPE, new AdvancedPatternJeiHandler());
    }

    public static final ISubtypeInterpreter<ItemStack> JEWELRY_INTERPRETER = new ISubtypeInterpreter<ItemStack>() {
        @Override
        public @Nullable Object getSubtypeData(ItemStack ingredient, UidContext context) {
            var data = JewelryData.get(ingredient);
            if (data.isValid()) {
                var pattern = data.pattern().getKey();
                if (pattern != null) {
                    return pattern.identifier().toString();
                }
            }
            return null;
        }
    };
    public static final ISubtypeInterpreter<ItemStack> PATTERN_INTERPRETER = new ISubtypeInterpreter<ItemStack>() {
        @Override
        public @Nullable Object getSubtypeData(ItemStack ingredient, UidContext context) {
            var pattern = StoredPatternData.get(ingredient);
            if (pattern != null && pattern.getKey() != null) {
                return pattern.getKey().identifier().toString();
            }

            return null;
        }
    };
}

