package io.redspace.ironsjewelry.core;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.redspace.ironsjewelry.core.data.PartDefinition;
import io.redspace.ironsjewelry.data_registry.PartDataHandler;

public record BonusSource(PartDefinition partSource, PartDefinition qualitySource) {
    public static final Codec<BonusSource> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            PartDataHandler.CODEC.fieldOf("partSource").forGetter(BonusSource::partSource),
            PartDataHandler.CODEC.fieldOf("qualitySource").forGetter(BonusSource::qualitySource)
    ).apply(builder, BonusSource::new));

    @Override
    public int hashCode() {
        return partSource.hashCode() * 31 + qualitySource().hashCode();
    }
}


