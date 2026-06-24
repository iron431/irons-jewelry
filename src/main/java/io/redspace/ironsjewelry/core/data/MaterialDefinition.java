package io.redspace.ironsjewelry.core.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.redspace.ironsjewelry.core.parameters.IBonusParameterType;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import javax.swing.text.html.Option;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * @param ingredient      item to be used to craft this material
 * @param paletteLocation location to material's color palette
 * @param bonusParameters entries of values for bonus paramaters
 * @param quality         effectiveness multiplier to bonuses of jewelry made from this material
 */
public record MaterialDefinition(String descriptionId, Optional<Ingredient> ingredient,
                                 ResourceLocation paletteLocation,
                                 Map<IBonusParameterType<?>, Object> bonusParameters,
                                 double quality) {

    public static final Codec<MaterialDefinition> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            Codec.STRING.fieldOf("descriptionId").forGetter(MaterialDefinition::descriptionId),
            Ingredient.CODEC_NONEMPTY.optionalFieldOf("ingredient").forGetter(MaterialDefinition::ingredient),
            ResourceLocation.CODEC.fieldOf("paletteLocation").forGetter(MaterialDefinition::paletteLocation),
            IBonusParameterType.BONUS_TO_INSTANCE_CODEC.fieldOf("bonusParameters").forGetter(MaterialDefinition::bonusParameters),
            Codec.DOUBLE.fieldOf("quality").forGetter(MaterialDefinition::quality)
    ).apply(builder, MaterialDefinition::new));


    public MaterialDefinition(
            String descriptionId,
            Ingredient ingredient,
            ResourceLocation paletteLocation,
            Map<IBonusParameterType<?>, Object> bonusParameters,
            double quality) {
        this(descriptionId, Optional.of(ingredient), paletteLocation, bonusParameters, quality);
    }

    public Stream<Holder<Item>> getIngredientItems() {
        return ingredient.map(ingr -> Arrays.stream(ingr.getItems()).map(ItemStack::getItemHolder)).orElseGet(Stream::of);
    }

    public boolean isIngredientEmpty() {
        return ingredient.isEmpty() || ingredient.get().hasNoItems();
    }
}
