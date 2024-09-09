package io.redspace.ironsjewelry.core.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

public record PartDefinition(ResourceLocation baseTextureLocation) {
    public static final Codec<PartDefinition> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            ResourceLocation.CODEC.fieldOf("baseTextureLocation").forGetter(PartDefinition::baseTextureLocation)
    ).apply(builder, PartDefinition::new));

    @Override
    public int hashCode() {
        return baseTextureLocation.hashCode();
    }
}
