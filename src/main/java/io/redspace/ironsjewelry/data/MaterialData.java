package io.redspace.ironsjewelry.data;

import io.redspace.ironsjewelry.core.AbstractBonus;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;

public record MaterialData(ResourceLocation materialId, Ingredient ingredient, ResourceLocation paletteLocation, AbstractBonus bonus, double quality) {
}
