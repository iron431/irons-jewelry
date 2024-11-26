package io.redspace.ironsjewelry.core.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.redspace.ironsjewelry.core.BonusType;
import io.redspace.ironsjewelry.registry.IronsJewelryRegistries;

import java.util.Optional;

public record Bonus(BonusType bonusType,
                    double qualityMultiplier,
                    Optional<QualityScalar> cooldown) {
    public static final Codec<Bonus> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            IronsJewelryRegistries.BONUS_TYPE_REGISTRY.byNameCodec().fieldOf("bonusType").forGetter(Bonus::bonusType),
            Codec.DOUBLE.optionalFieldOf("qualityMultiplier", 1d).forGetter(Bonus::qualityMultiplier),
            QualityScalar.CODEC.optionalFieldOf("cooldown").forGetter(Bonus::cooldown)
    ).apply(builder, Bonus::new));
}


