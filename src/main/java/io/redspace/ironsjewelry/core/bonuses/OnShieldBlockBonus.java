package io.redspace.ironsjewelry.core.bonuses;

import io.redspace.ironsjewelry.core.Bonus;
import io.redspace.ironsjewelry.core.data.BonusInstance;
import io.redspace.ironsjewelry.core.parameters.ActionParameter;
import io.redspace.ironsjewelry.registry.ParameterTypeRegistry;
import net.minecraft.network.chat.Component;

import java.util.List;

public class OnShieldBlockBonus extends Bonus {
    @Override
    public ActionParameter getParameterType() {
        return ParameterTypeRegistry.ACTION_PARAMETER.get();
    }

    @Override
    public List<Component> getTooltipDescription(BonusInstance bonus) {
        var param = getParameterType().resolve(bonus);
        if (param.isPresent()) {
            var enchant = param.get();
            return ParameterTypeRegistry.ACTION_PARAMETER.get().getActionTooltip(getTooltipDescriptionId(), enchant, bonus);
        }
        return super.getTooltipDescription(bonus);
    }
}
