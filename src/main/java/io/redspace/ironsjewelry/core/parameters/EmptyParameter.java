package io.redspace.ironsjewelry.core.parameters;

import com.mojang.serialization.Codec;
import io.redspace.ironsjewelry.core.IBonusParameterType;

public class EmptyParameter implements IBonusParameterType<Void> {
    public static final Codec<Void> CODEC = Codec.unit(() -> null);

    @Override
    public Codec<Void> codec() {
        return CODEC;
    }
}
