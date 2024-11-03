package io.redspace.ironsjewelry.core.parameters;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.redspace.ironsjewelry.core.IAction;
import io.redspace.ironsjewelry.core.IBonusParameterType;
import io.redspace.ironsjewelry.core.ICooldownHandler;
import io.redspace.ironsjewelry.core.Utils;
import io.redspace.ironsjewelry.core.data.BonusInstance;
import io.redspace.ironsjewelry.core.data.QualityScalar;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Optional;

public class ActionParameter implements IBonusParameterType<ActionParameter.ActionRunnable> {
    public record ActionRunnable(IAction action, QualityScalar cooldownTicks, boolean targetSelf) {
    }

    public static final Codec<ActionParameter.ActionRunnable> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            IAction.CODEC.fieldOf("action").forGetter(ActionRunnable::action),
            QualityScalar.CODEC.optionalFieldOf("cooldownTicks", new QualityScalar(0, 0, 0, Optional.empty())).forGetter(ActionRunnable::cooldownTicks),
            Codec.BOOL.fieldOf("targetSelf").forGetter(ActionRunnable::targetSelf)
    ).apply(builder, ActionRunnable::new));

    @Override
    public Codec<ActionParameter.ActionRunnable> codec() {
        return CODEC;
    }

    @Override
    public Optional<String> getValueDescriptionId(ActionParameter.ActionRunnable value) {
        return Optional.empty();
    }

    public List<Component> getActionTooltip(String prefixDescriptionId, ActionParameter.ActionRunnable param, BonusInstance bonusInstance) {
        var desc = Component.literal(" ").append(Component.translatable(prefixDescriptionId, param.action.formatTooltip(bonusInstance, param.targetSelf()))).withStyle(ChatFormatting.YELLOW);
        var cooldown = getCooldownDescriptor(bonusInstance, param);
        return cooldown.map(component -> List.of(desc, (Component) Component.literal(" ").append(component))).orElseGet(() -> List.of(desc));
    }

    public Optional<Component> getCooldownDescriptor(BonusInstance bonusInstance, ActionParameter.ActionRunnable param) {
        var cooldown = ICooldownHandler.INSTANCE.getCooldown(param.cooldownTicks, bonusInstance.quality());
        if (cooldown > 0) {
            return Optional.of(Component.translatable("tooltip.irons_jewelry.cooldown", Component.literal(Utils.timeFromTicks(cooldown, 1)).withStyle(ChatFormatting.YELLOW)).withStyle(ChatFormatting.GREEN));
        } else {
            return Optional.empty();
        }
    }
}
