package io.redspace.ironsjewelry.core.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.redspace.ironsjewelry.registry.IronsJewelryRegistries;
import net.minecraft.core.Holder;

import java.util.List;

public record PartIngredient(Holder<PartDefinition> part, int materialCost, int drawOrder,
                             List<Bonus> bonuses) {
    public static final Codec<PartIngredient> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            IronsJewelryRegistries.Codecs.PART_REGISTRY_CODEC.fieldOf("id").forGetter(PartIngredient::part),
            Codec.INT.fieldOf("materialCost").forGetter(PartIngredient::materialCost),
            Codec.INT.fieldOf("drawOrder").forGetter(PartIngredient::drawOrder),
            Codec.list(Bonus.CODEC).optionalFieldOf("bonuses", List.of()).forGetter(PartIngredient::bonuses)
    ).apply(builder, PartIngredient::new));

}
