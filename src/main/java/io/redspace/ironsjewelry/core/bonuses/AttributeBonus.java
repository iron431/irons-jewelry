package io.redspace.ironsjewelry.core.bonuses;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.redspace.ironsjewelry.IronsJewelry;
import io.redspace.ironsjewelry.core.IBonus;
import io.redspace.ironsjewelry.core.IBonusParameterType;
import io.redspace.ironsjewelry.registry.ParameterTypeRegistry;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import top.theillusivec4.curios.api.SlotContext;

public record AttributeBonus(Holder<Attribute> attribute, double amount,
                             AttributeModifier.Operation operation) implements IBonus {
    public static final MapCodec<AttributeBonus> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            Attribute.CODEC.fieldOf("attribute").forGetter(AttributeBonus::attribute),
            Codec.DOUBLE.fieldOf("amount").forGetter(AttributeBonus::amount),
            AttributeModifier.Operation.CODEC.fieldOf("operation").forGetter(AttributeBonus::operation)
    ).apply(builder, AttributeBonus::new));

    @Override
    public MapCodec<? extends IBonus> codec() {
        return CODEC;
    }

    @Override
    public IBonusParameterType<?> getParameter() {
        return ParameterTypeRegistry.ATTRIBUTE_PARAMETER.get();
    }

    public AttributeModifier modifier(SlotContext context, double quality) {
        var attr = attribute.getKey().location();
        return new AttributeModifier(IronsJewelry.id(String.format("%s_%s_%s_%s", attr.getNamespace(), attr.getPath(), context.identifier(), context.index())), amount * quality, operation);
    }
}
