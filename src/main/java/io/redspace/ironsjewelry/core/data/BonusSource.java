package io.redspace.ironsjewelry.core.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

public record BonusSource(ResourceLocation partIdForBonus, ResourceLocation partIdForQuality) {
    public static final Codec<BonusSource> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            ResourceLocation.CODEC.fieldOf("bonusSource").forGetter(BonusSource::partIdForBonus),
            ResourceLocation.CODEC.fieldOf("qualitySource").forGetter(BonusSource::partIdForQuality)
    ).apply(builder, BonusSource::new));

    @Override
    public int hashCode() {
        return partIdForBonus.hashCode() * 31 + partIdForQuality().hashCode();
    }
}


