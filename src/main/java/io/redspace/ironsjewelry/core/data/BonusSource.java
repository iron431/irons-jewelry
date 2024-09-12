package io.redspace.ironsjewelry.core.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.redspace.ironsjewelry.core.Utils;
import io.redspace.ironsjewelry.core.data_registry.PartDataHandler;

public record BonusSource(PartDefinition partForBonus, PartDefinition partForQuality) {
    public static final Codec<BonusSource> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            Utils.idCodec(PartDataHandler::getSafe, PartDefinition::id).fieldOf("bonusSource").forGetter(BonusSource::partForBonus),
            Utils.idCodec(PartDataHandler::getSafe, PartDefinition::id).fieldOf("qualitySource").forGetter(BonusSource::partForQuality)
    ).apply(builder, BonusSource::new));

    @Override
    public int hashCode() {
        return partForBonus.hashCode() * 31 + partForQuality().hashCode();
    }
}


