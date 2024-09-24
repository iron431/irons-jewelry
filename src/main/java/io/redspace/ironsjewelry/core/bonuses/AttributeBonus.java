package io.redspace.ironsjewelry.core.bonuses;

import io.redspace.ironsjewelry.IronsJewelry;
import io.redspace.ironsjewelry.core.IBonus;
import io.redspace.ironsjewelry.core.data.AttributeInstance;
import io.redspace.ironsjewelry.core.parameters.AttributeParameter;
import io.redspace.ironsjewelry.registry.ParameterTypeRegistry;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import top.theillusivec4.curios.api.SlotContext;

public record AttributeBonus() implements IBonus {
//    public static final MapCodec<AttributeBonus> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
//            Attribute.CODEC.fieldOf("attribute").forGetter(AttributeBonus::attribute),
//            Codec.DOUBLE.fieldOf("amount").forGetter(AttributeBonus::amount),
//            AttributeModifier.Operation.CODEC.fieldOf("operation").forGetter(AttributeBonus::operation)
//    ).apply(builder, AttributeBonus::new));
//
//    @Override
//    public MapCodec<? extends IBonus> codec() {
//        return CODEC;
//    }

    @Override
    public AttributeParameter getParameter() {
        return ParameterTypeRegistry.ATTRIBUTE_PARAMETER.get();
    }

    public AttributeModifier modifier(AttributeInstance value, SlotContext context, double quality) {
        var attribute = value.attribute();
        var amount = value.amount();
        var operation = value.operation();
        var attr = attribute.getKey().location();
        return new AttributeModifier(IronsJewelry.id(String.format("%s_%s_%s_%s", attr.getNamespace(), attr.getPath(), context.identifier(), context.index())), amount * quality, operation);
    }
}
