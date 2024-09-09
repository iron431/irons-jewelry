package io.redspace.ironsjewelry.core.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

public record PartIngredient(ResourceLocation partId, int materialCost) {
    public static final Codec<PartIngredient> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            ResourceLocation.CODEC.fieldOf("id").forGetter(PartIngredient::partId),
            Codec.INT.fieldOf("materialCost").forGetter(PartIngredient::materialCost)
    ).apply(builder, PartIngredient::new));

    @Override
    public int hashCode() {
        return partId.hashCode();
    }
}
