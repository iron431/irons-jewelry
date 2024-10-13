package io.redspace.ironsjewelry.core.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.redspace.ironsjewelry.core.Utils;
import io.redspace.ironsjewelry.core.data_registry.PartDataHandler;

public record PartIngredient(PartDefinition part, int materialCost, int drawOrder) {
    public static final Codec<PartIngredient> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            Utils.byIdCodec(PartDataHandler::getSafe,PartDefinition::id).fieldOf("id").forGetter(PartIngredient::part),
            Codec.INT.fieldOf("materialCost").forGetter(PartIngredient::materialCost),
            Codec.INT.fieldOf("drawOrder").forGetter(PartIngredient::drawOrder)
    ).apply(builder, PartIngredient::new));

}
