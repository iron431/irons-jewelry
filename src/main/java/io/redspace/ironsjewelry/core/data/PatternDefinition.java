package io.redspace.ironsjewelry.core.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;

/**
 * A pattern represents a piece of jewelry that can be crafted, and contains data for what components are required to craft it and what the resulting item can do
 * @param partTemplate
 * @param bonuses
 * @param unlockedByDefault
 * @param qualityMultiplier
 */
public record PatternDefinition(List<PartIngredient> partTemplate, List<BonusSource> bonuses, boolean unlockedByDefault, double qualityMultiplier) {
    public static final Codec<PatternDefinition> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            Codec.list(PartIngredient.CODEC).fieldOf("parts").forGetter(PatternDefinition::partTemplate),
            Codec.list(BonusSource.CODEC).fieldOf("bonuses").forGetter(PatternDefinition::bonuses),
            Codec.BOOL.optionalFieldOf("unlockedByDefault", true).forGetter(PatternDefinition::unlockedByDefault),
            Codec.DOUBLE.optionalFieldOf("qualityMultiplier", 1d).forGetter(PatternDefinition::qualityMultiplier)
    ).apply(builder, PatternDefinition::new));

    @Override
    public int hashCode() {
        return (partTemplate.hashCode() * 31 + bonuses().hashCode()) * 10 + (unlockedByDefault ? 1 : 0);
    }
}
