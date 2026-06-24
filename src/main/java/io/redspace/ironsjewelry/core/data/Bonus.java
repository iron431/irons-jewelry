package io.redspace.ironsjewelry.core.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.redspace.ironsjewelry.core.bonuses.BonusType;
import io.redspace.ironsjewelry.core.parameters.IBonusParameterType;
import io.redspace.ironsjewelry.registry.IronsJewelryRegistries;

import java.util.Map;
import java.util.Optional;

/**
 * The template bonus that a {@link PartIngredient} holds as a member of a pattern's part template
 *
 * @param bonusType         Type of bonus granted
 * @param qualityMultiplier Inherent quality factor from this part
 * @param cooldown
 * @param parameterValue    Optional predefined and fixed bonus parameter
 */
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


