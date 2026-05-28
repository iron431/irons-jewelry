package io.redspace.ironsjewelry.compat.apothic_attributes;

import net.minecraft.core.Holder;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.component.ItemAttributeModifiers;

import java.util.List;
import java.util.function.BiConsumer;

@FunctionalInterface
public interface ApothicAttributesProxy {
    void handleAttributeEvent(List<ItemAttributeModifiers.Entry> modifiers, BiConsumer<Holder<Attribute>, AttributeModifier> remove, BiConsumer<Holder<Attribute>, AttributeModifier> add);

    ApothicAttributesProxy NOOP = (modifiers, remove, add) -> {
    };

    static ItemAttributeModifiers.Entry entry(Holder<Attribute> attribute, AttributeModifier modifier) {
        return new ItemAttributeModifiers.Entry(attribute, modifier, EquipmentSlotGroup.ANY);
    }
}
