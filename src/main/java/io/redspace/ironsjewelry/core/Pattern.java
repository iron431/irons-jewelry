package io.redspace.ironsjewelry.core;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.redspace.ironsjewelry.core.data.PartIngredient;

import java.util.List;

public record Pattern(List<PartIngredient> partTemplate, List<BonusSource> bonuses, boolean unlockedByDefault) {
    public static final Codec<Pattern> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            Codec.list(PartIngredient.CODEC).fieldOf("parts").forGetter(Pattern::partTemplate),
            Codec.list(BonusSource.CODEC).fieldOf("bonuses").forGetter(Pattern::bonuses),
            Codec.BOOL.fieldOf("unlockedByDefault").forGetter(Pattern::unlockedByDefault)
    ).apply(builder, Pattern::new));
    @Override
    public int hashCode() {
        return (partTemplate.hashCode() * 31 + bonuses().hashCode()) * 10 + (unlockedByDefault ? 1 : 0);
    }
}
