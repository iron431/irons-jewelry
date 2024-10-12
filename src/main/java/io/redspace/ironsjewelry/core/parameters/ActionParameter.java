package io.redspace.ironsjewelry.core.parameters;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.redspace.ironsjewelry.core.IAction;
import io.redspace.ironsjewelry.core.IBonusParameterType;

import java.util.Optional;

public class ActionParameter implements IBonusParameterType<ActionParameter.ActionRunnable> {
    public record ActionRunnable(IAction action, int cooldownTicks, boolean targetSelf, String verbTranslation) {
    }

    public static final Codec<ActionParameter.ActionRunnable> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            IAction.CODEC.fieldOf("action").forGetter(ActionRunnable::action),
            Codec.INT.optionalFieldOf("cooldownTicks", 0).forGetter(ActionRunnable::cooldownTicks),
            Codec.BOOL.fieldOf("targetSelf").forGetter(ActionRunnable::targetSelf),
            Codec.STRING.fieldOf("verbTranslation").forGetter(ActionRunnable::verbTranslation)
    ).apply(builder, ActionRunnable::new));

    @Override
    public Codec<ActionParameter.ActionRunnable> codec() {
        return CODEC;
    }

    @Override
    public Optional<String> getValueDescriptionId(ActionParameter.ActionRunnable value) {
        return Optional.empty();
    }
}
