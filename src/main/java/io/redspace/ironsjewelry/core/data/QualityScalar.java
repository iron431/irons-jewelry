package io.redspace.ironsjewelry.core.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record QualityScalar(double baseAmount, double qualityScalar) {

    public static final Codec<QualityScalar> DIRECT_CODEC = RecordCodecBuilder.create(builder -> builder.group(
            Codec.DOUBLE.fieldOf("base").forGetter(QualityScalar::baseAmount),
            Codec.DOUBLE.fieldOf("scalar").forGetter(QualityScalar::qualityScalar)
    ).apply(builder, QualityScalar::new));
    public static final Codec<QualityScalar> CONSTANT_CODEC = Codec.DOUBLE.xmap(d -> new QualityScalar(d, d), scalar -> scalar.qualityScalar);

    public static final Codec<QualityScalar> CODEC = Codec.withAlternative(DIRECT_CODEC, CONSTANT_CODEC);

    /**
     * @param quality
     * @return A value that scales with the quality. At quality = 1.0, returns base value. Quality < 1 returns < base value
     */
    public double sample(double quality) {
        double d = baseAmount + (quality - 1) * qualityScalar;
        return baseAmount < 0 ? Math.min(d, 0) : Math.max(d, 0);
    }
}
