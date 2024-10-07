package io.redspace.ironsjewelry.core.parameters;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.redspace.ironsjewelry.core.IBonusParameterType;
import net.minecraft.world.item.enchantment.effects.EnchantmentEntityEffect;

import java.util.Optional;

public class EnchantmentRunnableParameter implements IBonusParameterType<EnchantmentRunnableParameter.EnchantmentRunnable> {
    public record EnchantmentRunnable(EnchantmentEntityEffect enchantment, boolean targetSelf, int effectiveLevel, String verbTranslation) {
    }

    public static final Codec<EnchantmentRunnableParameter.EnchantmentRunnable> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            EnchantmentEntityEffect.CODEC.fieldOf("effect").forGetter(EnchantmentRunnable::enchantment),
            Codec.BOOL.fieldOf("targetSelf").forGetter(EnchantmentRunnable::targetSelf),
            Codec.INT.fieldOf("effectiveLevel").forGetter(EnchantmentRunnable::effectiveLevel),
            Codec.STRING.fieldOf("verbTranslation").forGetter(EnchantmentRunnable::verbTranslation)
    ).apply(builder, EnchantmentRunnable::new));

    @Override
    public Codec<EnchantmentRunnableParameter.EnchantmentRunnable> codec() {
        return CODEC;
    }

    @Override
    public Optional<String> getValueDescriptionId(EnchantmentRunnable value) {
        return Optional.empty();
    }
}
