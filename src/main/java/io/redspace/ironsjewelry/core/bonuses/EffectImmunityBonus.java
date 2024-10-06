package io.redspace.ironsjewelry.core.bonuses;

import io.redspace.ironsjewelry.core.Bonus;
import io.redspace.ironsjewelry.core.data.BonusInstance;
import io.redspace.ironsjewelry.core.parameters.EffectParameter;
import io.redspace.ironsjewelry.registry.ParameterTypeRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import java.util.List;

public class EffectImmunityBonus extends Bonus {
    @Override
    public EffectParameter getParameterType() {
        return ParameterTypeRegistry.NEGATIVE_EFFECT_PARAMETER.get();
    }

    @Override
    public List<Component> getTooltipDescription(BonusInstance bonus) {
        var param = getParameterType().resolve(bonus);
        if (param.isPresent()) {
            var effect = param.get();
            return List.of(
                    Component.literal(" ").append(Component.translatable(getDescriptionId() + ".description",
                            Component.translatable(effect.value().getDescriptionId()).withStyle(ChatFormatting.RED)
                    )).withStyle(ChatFormatting.YELLOW)
            );
        }
        return super.getTooltipDescription(bonus);
    }
}
