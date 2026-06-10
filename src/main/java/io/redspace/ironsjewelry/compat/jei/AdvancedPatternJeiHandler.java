package io.redspace.ironsjewelry.compat.jei;

import io.redspace.ironsjewelry.core.data.PatternDefinition;
import io.redspace.ironsjewelry.core.data.StoredPatternData;
import io.redspace.ironsjewelry.registry.ItemRegistry;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.advanced.ISimpleRecipeManagerPlugin;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Optional;

public class AdvancedPatternJeiHandler implements ISimpleRecipeManagerPlugin<PatternDefinition> {
    @Override
    public boolean isHandledInput(ITypedIngredient<?> input) {
        return input.getItemStack().orElse(ItemStack.EMPTY).is(ItemRegistry.RECIPE);
    }

    @Override
    public boolean isHandledOutput(ITypedIngredient<?> output) {
        return false;
    }

    @Override
    public List<PatternDefinition> getRecipesForInput(ITypedIngredient<?> input) {
        return input.getItemStack()
                .flatMap(stack -> Optional.ofNullable(StoredPatternData.get(stack))
                        .map(pattern -> List.of(pattern.value())))
                .orElse(List.of());
    }

    @Override
    public List<PatternDefinition> getRecipesForOutput(ITypedIngredient<?> output) {
        return List.of();
    }

    @Override
    public List<PatternDefinition> getAllRecipes() {
        return List.of();
    }
}
