package io.redspace.ironsjewelry.core.data;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.redspace.ironsjewelry.IronsJewelry;
import io.redspace.ironsjewelry.core.Bonus;
import io.redspace.ironsjewelry.core.IBonusParameterType;
import io.redspace.ironsjewelry.core.Utils;
import io.redspace.ironsjewelry.core.data_registry.PartDataHandler;
import io.redspace.ironsjewelry.registry.BonusRegistry;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Function;

public record BonusSource(Bonus bonus, Either<Map<IBonusParameterType<?>, Object>, PartDefinition> parameterOrSource,
                          Either<Double, PartDefinition> qualityOrSource, double qualityMultiplier) {
    public static final Codec<BonusSource> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            BonusRegistry.BONUS_REGISTRY.byNameCodec().fieldOf("bonus").forGetter(BonusSource::bonus),
            Codec.either(IBonusParameterType.BONUS_TO_INSTANCE_CODEC, Utils.byIdCodec(PartDataHandler::getSafe, PartDefinition::id)).fieldOf("parameter").forGetter(BonusSource::parameterOrSource),
            Codec.either(Codec.DOUBLE, Utils.byIdCodec(PartDataHandler::getSafe, PartDefinition::id)).fieldOf("quality").forGetter(BonusSource::qualityOrSource),
            Codec.DOUBLE.optionalFieldOf("qualityMultiplier", 1d).forGetter(BonusSource::qualityMultiplier)
    ).apply(builder, BonusSource::new));

    public BonusInstance getBonusFor(JewelryData data) {
        return new BonusInstance(
                bonus,
                mapEither(qualityOrSource, Function.identity(), (right) -> qualityFromPart(right, data)) * data.pattern().qualityMultiplier()  * qualityMultiplier,
                mapEither(parameterOrSource, Function.identity(), (right) -> parameterFromPart(right, data))
        );
    }

    private <L, R, T> T mapEither(Either<L, R> either, Function<L, T> leftToValue, Function<R, T> rightToValue) {
        if (either.left().isPresent()) {
            return leftToValue.apply(either.left().get());
        } else if (either.right().isPresent()) {
            return rightToValue.apply(either.right().get());
        } else {
            throw new NoSuchElementException("Neither right nor left present in either");
        }
    }

    private double qualityFromPart(PartDefinition part, JewelryData data) {
        return data.parts().containsKey(part) ? data.parts().get(part).quality() : 1d;
    }

    private Map<IBonusParameterType<?>, Object> parameterFromPart(PartDefinition part, JewelryData data) {
        var targetParameter = bonus.getParameterType();
        if (targetParameter.isEmpty()) {
            return Map.of();
        }
        var material = data.parts().get(part);
        if (material != null) {
            var params = material.bonusParameters();
            var param = params.get(targetParameter);
            if (param != null) {
                return Map.of(targetParameter, param);
            }
        }
        IronsJewelry.LOGGER.warn("Part {} unable to find bonus parameter {} in jewelry: {}", part, targetParameter, data);
        return Map.of();
    }
//    @Override
//    public int hashCode() {
//        return partForBonus.hashCode() * 31 + partForQuality().hashCode();
//    }


}


