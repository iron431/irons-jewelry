package io.redspace.ironsjewelry.core.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.redspace.ironsjewelry.core.BonusType;
import io.redspace.ironsjewelry.core.IBonusParameterType;
import io.redspace.ironsjewelry.registry.IronsJewelryRegistries;

import java.util.Map;
import java.util.Optional;

public record Bonus(BonusType bonusType,
                    double qualityMultiplier,
                    Optional<QualityScalar> cooldown,
                    Map<IBonusParameterType<?>, Object> parameterValue) {
    public static final Codec<Bonus> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            IronsJewelryRegistries.BONUS_TYPE_REGISTRY.byNameCodec().fieldOf("bonusType").forGetter(Bonus::bonusType),
            Codec.DOUBLE.optionalFieldOf("qualityMultiplier", 1d).forGetter(Bonus::qualityMultiplier),
            QualityScalar.CODEC.optionalFieldOf("cooldown").forGetter(Bonus::cooldown),
            IBonusParameterType.BONUS_TO_INSTANCE_CODEC.optionalFieldOf("parameterValue", Map.of()).forGetter(Bonus::parameterValue)
    ).apply(builder, Bonus::new));

    public Bonus(BonusType bonusType, double qualityMultiplier) {
       this(bonusType, qualityMultiplier, Optional.empty(), Map.of());
    }
}


