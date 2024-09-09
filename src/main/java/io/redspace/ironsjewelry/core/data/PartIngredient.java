package io.redspace.ironsjewelry.core.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.redspace.ironsjewelry.data_registry.PartDataHandler;

public record PartIngredient(PartDefinition part, int materialCost) {
    public static final Codec<PartIngredient> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            PartDataHandler.CODEC.fieldOf("id").forGetter(PartIngredient::part),
            Codec.INT.fieldOf("materialCost").forGetter(PartIngredient::materialCost)
    ).apply(builder, PartIngredient::new));

    @Override
    public int hashCode() {
        return part.hashCode();
    }
}
