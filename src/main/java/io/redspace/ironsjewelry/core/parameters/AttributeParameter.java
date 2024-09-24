package io.redspace.ironsjewelry.core.parameters;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.redspace.ironsjewelry.core.IBonusParameterType;
import io.redspace.ironsjewelry.core.data.AttributeInstance;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public class AttributeParameter implements IBonusParameterType<AttributeInstance> {
    public static final Codec<AttributeInstance> CODEC = RecordCodecBuilder.create(
            p_349989_ -> p_349989_.group(
                            BuiltInRegistries.ATTRIBUTE.holderByNameCodec().fieldOf("attribute").forGetter(AttributeInstance::attribute),
                            Codec.DOUBLE.fieldOf("amount").forGetter(AttributeInstance::amount),
                            AttributeModifier.Operation.CODEC.fieldOf("operation").forGetter(AttributeInstance::operation)
                    )
                    .apply(p_349989_, AttributeInstance::new)
    );

    @Override
    public Codec<AttributeInstance> codec() {
        return CODEC;
    }
}
