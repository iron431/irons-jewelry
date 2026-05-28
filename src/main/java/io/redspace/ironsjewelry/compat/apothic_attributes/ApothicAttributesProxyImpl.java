package io.redspace.ironsjewelry.compat.apothic_attributes;

import dev.shadowsoffire.apothic_attributes.api.ALObjects;
import io.redspace.ironsjewelry.registry.AttributeRegistry;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.component.ItemAttributeModifiers;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public class ApothicAttributesProxyImpl implements ApothicAttributesProxy {
    private static final Map<Holder<Attribute>, Holder<Attribute>> ATTRIBUTE_REMAP = Map.of(
            AttributeRegistry.ARMOR_PIERCE, ALObjects.Attributes.ARMOR_PIERCE,
            AttributeRegistry.MINING_SPEED, ALObjects.Attributes.MINING_SPEED,
            AttributeRegistry.EXPERIENCE_GAINED, ALObjects.Attributes.EXPERIENCE_GAINED,
            AttributeRegistry.ARROW_DAMAGE, ALObjects.Attributes.ARROW_DAMAGE,
            AttributeRegistry.CRIT_DAMAGE, ALObjects.Attributes.CRIT_DAMAGE,
            AttributeRegistry.DODGE_CHANCE, ALObjects.Attributes.DODGE_CHANCE
    );

    @Override
    public void handleAttributeEvent(List<ItemAttributeModifiers.Entry> modifiers, BiConsumer<Holder<Attribute>, AttributeModifier> remove, BiConsumer<Holder<Attribute>, AttributeModifier> add) {
        modifiers.forEach(entry -> {
            Holder<Attribute> attribute = entry.attribute();
            AttributeModifier modifier = entry.modifier();
            Holder<Attribute> remapped = ATTRIBUTE_REMAP.get(attribute);
            if (remapped != null) {
                remove.accept(attribute, modifier);
                add.accept(remapped, modifier);
            }
        });
    }
}
