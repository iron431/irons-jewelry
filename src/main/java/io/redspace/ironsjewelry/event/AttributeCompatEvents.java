package io.redspace.ironsjewelry.event;

import io.redspace.ironsjewelry.IronsJewelry;
import io.redspace.ironsjewelry.compat.CompatHandler;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ItemAttributeModifierEvent;
import top.theillusivec4.curios.api.event.CurioAttributeModifierEvent;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(modid = IronsJewelry.MODID)
public class AttributeCompatEvents {
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onItemAttributeModifiers(ItemAttributeModifierEvent event) {
        List<ItemAttributeModifiers.Entry> modifiers = new ArrayList<>(event.getModifiers());
        CompatHandler.APOTH_PROXY.handleAttributeEvent(modifiers,
                (attribute, modifier) -> event.removeModifier(attribute, modifier.id()),
                (attribute, modifier) -> event.addModifier(attribute, modifier, slotFor(modifiers, attribute, modifier)));
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onCurioAttributeModifiers(CurioAttributeModifierEvent event) {
        var modifiers = event.getModifiers().entries().stream()
                .map(entry -> new ItemAttributeModifiers.Entry(entry.getKey(), entry.getValue(), EquipmentSlotGroup.ANY))
                .toList();
        CompatHandler.APOTH_PROXY.handleAttributeEvent(modifiers, event::removeModifier, event::addModifier);
    }

    private static EquipmentSlotGroup slotFor(List<ItemAttributeModifiers.Entry> modifiers, Holder<Attribute> attribute, AttributeModifier modifier) {
        return modifiers.stream()
                .filter(entry -> entry.attribute().equals(attribute) && entry.modifier().equals(modifier))
                .map(ItemAttributeModifiers.Entry::slot)
                .findFirst()
                .orElse(EquipmentSlotGroup.ANY);
    }
}
