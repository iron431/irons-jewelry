package io.redspace.ironsjewelry.core.bonuses;

import io.redspace.ironsjewelry.core.Bonus;
import io.redspace.ironsjewelry.core.data.BonusInstance;
import io.redspace.ironsjewelry.core.parameters.ActionParameter;
import io.redspace.ironsjewelry.registry.ParameterTypeRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import java.util.List;

public class OnShieldBlockBonus extends Bonus {
    @Override
    public ActionParameter getParameterType() {
        return ParameterTypeRegistry.DEFENSIVE_ACTION_PARAMETER.get();
    }

    @Override
    public List<Component> getTooltipDescription(BonusInstance bonus) {
        var param = getParameterType().resolve(bonus);
        if (param.isPresent()) {
            var enchant = param.get();
            return List.of(
                    Component.literal(" ").append(Component.translatable(getDescriptionId() + ".description",
                            Component.translatable(enchant.verbTranslation()).withStyle(ChatFormatting.GREEN),
                            Component.translatable(enchant.targetSelf() ? "tooltip.irons_jewelry.self" : "tooltip.irons_jewelry.attacker").withStyle(ChatFormatting.YELLOW)
                    )).withStyle(ChatFormatting.YELLOW)
            );
        }
        return super.getTooltipDescription(bonus);
    }
}
