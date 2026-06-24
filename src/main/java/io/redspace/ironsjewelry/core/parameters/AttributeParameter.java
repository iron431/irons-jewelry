package io.redspace.ironsjewelry.core.parameters;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.redspace.ironsjewelry.core.data.AttributeInstance;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.component.ItemAttributeModifiers;

import java.util.Optional;

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

    @Override
    public Optional<String> getValueDescriptionId(AttributeInstance value) {
        return Optional.of(value.attribute().value().getDescriptionId());
    }

    @Override
    public Optional<Component> getSimpleDescription(AttributeInstance value) {
        Holder<Attribute> attribute = value.attribute();
        double d0 = value.amount();
        double d1;
        if (value.operation() == AttributeModifier.Operation.ADD_MULTIPLIED_BASE
                || value.operation() == AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL) {
            d1 = d0 * 100.0;
        } else if (attribute.is(Attributes.KNOCKBACK_RESISTANCE)) {
            d1 = d0 * 10.0;
        } else {
            d1 = d0;
        }
        if (d0 >= 0.0) {
            return Optional.of(Component.translatable(
                    "attribute.modifier.plus." + value.operation().id(),
                    ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(d1),
                    Component.translatable(attribute.value().getDescriptionId())
            ));
        } else {
            return Optional.of(
                    Component.translatable(
                            "attribute.modifier.take." + value.operation().id(),
                            ItemAttributeModifiers.ATTRIBUTE_MODIFIER_FORMAT.format(-d1),
                            Component.translatable(attribute.value().getDescriptionId())
                    ));
        }
    }
}
