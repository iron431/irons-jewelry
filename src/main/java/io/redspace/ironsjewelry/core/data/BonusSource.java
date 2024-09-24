package io.redspace.ironsjewelry.core.data;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.redspace.ironsjewelry.IronsJewelry;
import io.redspace.ironsjewelry.core.IBonus;
import io.redspace.ironsjewelry.core.IBonusParameterType;
import io.redspace.ironsjewelry.core.Utils;
import io.redspace.ironsjewelry.core.data_registry.PartDataHandler;

import java.util.Map;

public record BonusSource(IBonus bonus, Either<Map<IBonusParameterType<?>, Object>, PartDefinition> parameterOrSource,
                          Either<Double, PartDefinition> qualityOrSource) {
    public static final Codec<BonusSource> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            IBonus.DIRECT_CODEC.fieldOf("bonus").forGetter(BonusSource::bonus),
            Codec.either(IBonusParameterType.BONUS_TO_INSTANCE_CODEC, Utils.idCodec(PartDataHandler::getSafe, PartDefinition::id)).fieldOf("parameter").forGetter(BonusSource::parameterOrSource),
            Codec.either(Codec.DOUBLE, Utils.idCodec(PartDataHandler::getSafe, PartDefinition::id)).fieldOf("quality").forGetter(BonusSource::qualityOrSource)
    ).apply(builder, BonusSource::new));

    public BonusInstance getBonusFor(JewelryData data) {
        return new BonusInstance(
                bonus,
                qualityOrSource.left().orElse(qualityFromPart(qualityOrSource.right().get(), data)),
                parameterOrSource.left().orElse(parameterFromPart(parameterOrSource.right().get(), data))
        );
    }

    private double qualityFromPart(PartDefinition part, JewelryData data) {
        return data.parts().containsKey(part) ? data.parts().get(part).quality() : 1d;
    }

    private Map<IBonusParameterType<?>, Object> parameterFromPart(PartDefinition part, JewelryData data) {
        var targetParameter = bonus.getParameter();
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
//TODO:
//    @Override
//    public int hashCode() {
//        return partForBonus.hashCode() * 31 + partForQuality().hashCode();
//    }


}


