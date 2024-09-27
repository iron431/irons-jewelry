package io.redspace.ironsjewelry.core.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.redspace.ironsjewelry.core.IBonusParameterType;
import io.redspace.ironsjewelry.core.data_registry.MaterialDataHandler;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.List;
import java.util.Map;

/**
 * @param ingredient      item to be used to craft this material
 * @param paletteLocation location to material's color palette
 * @param materialType classification of this material, ie [gem], [metal, gold]
 * @param bonusParameters   entries of values for bonus paramaters
 * @param quality         effectiveness multiplier to bonuses of jewelry made from this material
 */
public record MaterialDefinition(Ingredient ingredient, List<String> materialType, ResourceLocation paletteLocation,
                                 Map<IBonusParameterType<?>, Object> bonusParameters,
                                 double quality) {

    public static final Codec<MaterialDefinition> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            Ingredient.CODEC.fieldOf("ingredient").forGetter(MaterialDefinition::ingredient),
            Codec.list(Codec.STRING).fieldOf("materialType").forGetter(MaterialDefinition::materialType),
            net.minecraft.resources.ResourceLocation.CODEC.fieldOf("paletteLocation").forGetter(MaterialDefinition::paletteLocation),
            IBonusParameterType.BONUS_TO_INSTANCE_CODEC.fieldOf("bonusParameters").forGetter(MaterialDefinition::bonusParameters),

            Codec.DOUBLE.fieldOf("quality").forGetter(MaterialDefinition::quality)
    ).apply(builder, MaterialDefinition::new));

    public ResourceLocation id() {
        return MaterialDataHandler.getKey(this);
    }

    public String getDescriptionId() {
        var id = id();
        if (id == null) {
            return "unregistered_material";
        } else {
            return Util.makeDescriptionId("material", id);
        }
    }
}
