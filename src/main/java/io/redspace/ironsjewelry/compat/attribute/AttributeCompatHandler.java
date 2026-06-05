package io.redspace.ironsjewelry.compat.attribute;

import net.minecraft.core.Holder;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.component.ItemAttributeModifiers;

import java.util.List;
import java.util.function.BiConsumer;

public final class AttributeCompatHandler {
    public static void handleAttributeEvent(
            List<ItemAttributeModifiers.Entry> modifiers,
            BiConsumer<Holder<Attribute>, AttributeModifier> remove,
            BiConsumer<Holder<Attribute>, AttributeModifier> add
    ) {
        modifiers.forEach(entry -> {
            Holder<Attribute> attribute = entry.attribute();
            AttributeModifier modifier = entry.modifier();
            AttributeRemapRegistry.findTarget(attribute).ifPresent(remapped -> {
                remove.accept(attribute, modifier);
                add.accept(remapped, modifier);
            });
        });
    }
}
