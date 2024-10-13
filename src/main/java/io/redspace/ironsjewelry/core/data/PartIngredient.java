package io.redspace.ironsjewelry.core.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.redspace.ironsjewelry.registry.JewelryDataRegistries;
import net.minecraft.core.Holder;

public record PartIngredient(Holder<PartDefinition> part, int materialCost, int drawOrder) {
    public static final Codec<PartIngredient> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            JewelryDataRegistries.PART_REGISTRY_CODEC.fieldOf("id").forGetter(PartIngredient::part),
            Codec.INT.fieldOf("materialCost").forGetter(PartIngredient::materialCost),
            Codec.INT.fieldOf("drawOrder").forGetter(PartIngredient::drawOrder)
    ).apply(builder, PartIngredient::new));

}
