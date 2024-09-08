package io.redspace.ironsjewelry.core.data;

import io.redspace.ironsjewelry.core.AbstractBonus;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.List;

/**
 *
 * @param ingredient item to be used to craft this material
 * @param paletteLocation location to material's color palette
 * @param bonuses list of bonuses using this material could provide
 * @param quality effectiveness multiplier to bonuses of jewelry made from this material
 */
public record MaterialData(Ingredient ingredient, ResourceLocation paletteLocation, List<AbstractBonus> bonuses, double quality) {
}
