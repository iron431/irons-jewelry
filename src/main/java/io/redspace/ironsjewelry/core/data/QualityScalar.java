package io.redspace.ironsjewelry.core.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Optional;

public record QualityScalar(double baseAmount, double qualityScalar, double min, Optional<Double> max) {

    public static final Codec<QualityScalar> DIRECT_CODEC = RecordCodecBuilder.create(builder -> builder.group(
            Codec.DOUBLE.fieldOf("base").forGetter(QualityScalar::baseAmount),
            Codec.DOUBLE.fieldOf("scalar").forGetter(QualityScalar::qualityScalar),
            Codec.DOUBLE.optionalFieldOf("min", 0d).forGetter(QualityScalar::min),
            Codec.DOUBLE.optionalFieldOf("max").forGetter(QualityScalar::max)
    ).apply(builder, QualityScalar::new));
    public static final Codec<QualityScalar> CONSTANT_CODEC = Codec.DOUBLE.xmap(d -> new QualityScalar(d, 0, 0, Optional.empty()), scalar -> scalar.baseAmount);

    public static final Codec<QualityScalar> CODEC = Codec.withAlternative(DIRECT_CODEC, CONSTANT_CODEC);

    /**
     * @param quality
     * @return A value that scales with the quality. At quality = 1.0, returns base value. Quality < 1 returns < base value
     */
    public double sample(double quality) {
        double d = baseAmount + (quality - 1) * qualityScalar;
        if (baseAmount < 0) {
            d = Math.min(d, min);
            if (max.isPresent()) {
                d = Math.max(d, max.get());
            }
        } else {
            d = Math.max(d, min);
            if (max.isPresent()) {
                d = Math.min(d, max.get());
            }
        }
        return d;
    }
}
