package io.redspace.ironsjewelry.core.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.redspace.ironsjewelry.core.IBonus;
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
public record MaterialDefinition(Ingredient ingredient, ResourceLocation paletteLocation, List<IBonus> bonuses, double quality) {
    public static final Codec<MaterialDefinition> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            Ingredient.CODEC.fieldOf("ingredient").forGetter(MaterialDefinition::ingredient),
            ResourceLocation.CODEC.fieldOf("paletteLocation").forGetter(MaterialDefinition::paletteLocation),
            Codec.list(IBonus.DIRECT_CODEC).fieldOf("bonuses").forGetter(MaterialDefinition::bonuses),
            Codec.DOUBLE.fieldOf("quality").forGetter(MaterialDefinition::quality)
    ).apply(builder, MaterialDefinition::new));
}
