package io.redspace.ironsjewelry.core.parameters;

import com.mojang.serialization.Codec;
import net.minecraft.network.chat.Component;

import java.util.Optional;

public class EmptyParameter implements IBonusParameterType<Void> {
    public static final Codec<Void> CODEC = Codec.unit(() -> null);

    @Override
    public Codec<Void> codec() {
        return CODEC;
    }

    @Override
    public Optional<String> getValueDescriptionId(Void v) {
        return Optional.empty();
    }

    @Override
    public Optional<Component> getSimpleDescription(Void value) {
        return Optional.empty();
    }
}
