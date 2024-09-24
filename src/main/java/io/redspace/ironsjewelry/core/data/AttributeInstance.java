package io.redspace.ironsjewelry.core.data;

import net.minecraft.core.Holder;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public record AttributeInstance(Holder<Attribute> attribute, double amount, AttributeModifier.Operation operation) {
}
