package io.redspace.ironsjewelry.core.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.redspace.ironsjewelry.core.AbstractBonus;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.List;

/**
 *
 * @param ingredient item to be used to craft this materialId
 * @param paletteLocation location to materialId's color palette
 * @param bonuses list of bonuses using this materialId could provide
 * @param quality effectiveness multiplier to bonuses of jewelry made from this materialId
 */
public record MaterialData(Ingredient ingredient, ResourceLocation paletteLocation, List<AbstractBonus> bonuses, double quality) {
    //TODO: figure out how to record bonuses
    public static final Codec<MaterialData> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            Ingredient.CODEC.fieldOf("ingredient").forGetter(MaterialData::ingredient),
            ResourceLocation.CODEC.fieldOf("paletteLocation").forGetter(MaterialData::paletteLocation),
            //Codecs.li.CODEC.fieldOf("paletteLocation").forGetter(MaterialData::bonuses),
            Codec.DOUBLE.fieldOf("quality").forGetter(MaterialData::quality)
    ).apply(builder, (ingredient, paletteLocation, quality) -> new MaterialData(ingredient, paletteLocation, List.of(), quality)));
}
