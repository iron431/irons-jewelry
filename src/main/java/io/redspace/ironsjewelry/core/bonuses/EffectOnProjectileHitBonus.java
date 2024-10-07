package io.redspace.ironsjewelry.core.bonuses;

import io.redspace.ironsjewelry.core.Bonus;
import io.redspace.ironsjewelry.core.Utils;
import io.redspace.ironsjewelry.core.data.BonusInstance;
import io.redspace.ironsjewelry.core.parameters.EffectParameter;
import io.redspace.ironsjewelry.registry.ParameterTypeRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;

import java.util.List;

public class EffectOnProjectileHitBonus extends Bonus {
    @Override
    public EffectParameter getParameterType() {
        return ParameterTypeRegistry.NEGATIVE_EFFECT_PARAMETER.get();
    }

    @Override
    public List<Component> getTooltipDescription(BonusInstance bonus) {
        var param = getParameterType().resolve(bonus.parameter());
        if (param.isPresent()) {
            var effect = param.get();
            return List.of(
                    Component.literal(" ").append(Component.translatable(getDescriptionId() + ".description", Component.translatable(effect.value().getDescriptionId()).withStyle(ChatFormatting.RED), Component.literal(Utils.digitalTimeFromTicks(durationInTicks(effect, bonus.quality()))).withStyle(ChatFormatting.GREEN)).withStyle(ChatFormatting.YELLOW))
            );
        }
        return super.getTooltipDescription(bonus);
    }

    public int durationInTicks(Holder<MobEffect> effect, double quality) {
        return effect.value().isInstantenous() ? 0 : (int) (100 + 50 * quality);
    }
}
