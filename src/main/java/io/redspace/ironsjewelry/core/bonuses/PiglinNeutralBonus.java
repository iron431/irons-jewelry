package io.redspace.ironsjewelry.core.bonuses;

import io.redspace.ironsjewelry.core.Bonus;
import io.redspace.ironsjewelry.core.IBonusParameterType;
import io.redspace.ironsjewelry.core.data.BonusInstance;
import io.redspace.ironsjewelry.registry.ParameterTypeRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import java.util.List;

public class PiglinNeutralBonus extends Bonus {
    @Override
    public IBonusParameterType<?> getParameterType() {
        return ParameterTypeRegistry.EMPTY.get();
    }

    @Override
    public List<Component> getTooltipDescription(BonusInstance bonus) {
        return List.of(Component.literal(" ").append(Component.translatable(getDescriptionId() + ".description").withStyle(ChatFormatting.YELLOW)));
    }
}
