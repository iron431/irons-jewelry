package io.redspace.ironsjewelry.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import io.redspace.ironsjewelry.core.Utils;
import io.redspace.ironsjewelry.core.bonuses.AttributeBonus;
import io.redspace.ironsjewelry.core.bonuses.PiglinNeutralBonus;
import io.redspace.ironsjewelry.core.data.BonusInstance;
import io.redspace.ironsjewelry.core.data.JewelryData;
import io.redspace.ironsjewelry.registry.BonusRegistry;
import io.redspace.ironsjewelry.registry.ComponentRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurio;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CurioBaseItem extends Item implements ICurioItem {
    String slotIdentifier = "";

    public CurioBaseItem(Item.Properties properties, String slot) {
        super(properties);
        this.slotIdentifier = slot;
    }

    public boolean isEquippedBy(@Nullable LivingEntity entity) {
        return entity != null && CuriosApi.getCuriosInventory(entity).map(inv -> inv.findFirstCurio(this).isPresent()).orElse(false);
    }

    @NotNull
    @Override
    public ICurio.SoundInfo getEquipSound(SlotContext slotContext, ItemStack stack) {
        return new ICurio.SoundInfo(SoundEvents.ARMOR_EQUIP_CHAIN.value(), 1.0f, 1.0f);
    }

    @Override
    public List<Component> getAttributesTooltip(List<Component> tooltips, ItemStack stack) {
        var tooltip = ICurioItem.super.getAttributesTooltip(tooltips, stack);
        boolean needHeader = tooltip.isEmpty();
        JewelryData.ifPresent(stack, (jewelryData) -> {
            var bonuses = jewelryData.getBonuses();
            if (needHeader && !bonuses.isEmpty()) {
                tooltips.add(Component.empty());
                tooltips.add(Component.translatable("curios.modifiers." + slotIdentifier).withStyle(ChatFormatting.GOLD));
            }
            bonuses.forEach(bonus -> tooltips.addAll(bonus.getTooltipDescription()));
        });
        return tooltip;
    }

    @Override
    public Component getName(ItemStack itemStack) {
        //TODO: cache or use actual item name component entry
        return JewelryData.get(itemStack).getItemName();
    }

    @Override
    public boolean makesPiglinsNeutral(ItemStack stack, LivingEntity wearer) {
        //TODO: cache
        return wearer instanceof Player player && Utils.getEquippedBonuses(player).stream().map(BonusInstance::bonus).anyMatch(bonus -> bonus instanceof PiglinNeutralBonus);
    }

    @Override
    public void onEquip(SlotContext slotContext, ItemStack prevStack, ItemStack stack) {
        ICurioItem.super.onEquip(slotContext, prevStack, stack);
        JewelryData.ifPresent(stack, data -> data.forBonuses(BonusRegistry.EFFECT_IMMUNITY_BONUS.get(), Holder.class, (bonus, param) -> slotContext.entity().removeEffect(param)));
    }

    @Override
    public Multimap<Holder<Attribute>, AttributeModifier> getAttributeModifiers(SlotContext slotContext, ResourceLocation id, ItemStack stack) {
        JewelryData data = stack.get(ComponentRegistry.JEWELRY_COMPONENT);
        //TODO: cache these in the stack's attribute component for as long as index hasn't changed?
        if (data != null && slotContext.identifier().equals(this.slotIdentifier)) {
            var bonuses = data.getBonuses();
            // We want to combine like modifiers by operation, so instead of two "+2 health"'s, we get one "+4 health"
            Map<Holder<Attribute>, Map<AttributeModifier.Operation, AttributeModifier>> collapsedModifiers = new HashMap<>();
            for (BonusInstance instance : bonuses) {
                if (instance.bonus() instanceof AttributeBonus attributeBonus) {
                    attributeBonus.getParameterType().resolve(instance.parameter()).ifPresent(
                            attributeInstance -> {
                                // If this is the first modifier of this attribute and operation, store it.
                                // If it is not, get the previous modifier, and create a new modifier of previous value + additional value
                                var byOperation = collapsedModifiers.computeIfAbsent(attributeInstance.attribute(), (x) -> new HashMap<>());
                                var modifier = attributeBonus.modifier(attributeInstance, slotContext, instance.quality());
                                var operation = modifier.operation();
                                if (byOperation.containsKey(operation)) {
                                    var oldModifier = byOperation.get(operation);
                                    var jointModifier = new AttributeModifier(oldModifier.id(), oldModifier.amount() + modifier.amount(), operation);
                                    byOperation.put(operation, jointModifier);
                                } else {
                                    byOperation.put(operation, modifier);
                                }
                            });
                }
            }
            ImmutableMultimap.Builder<Holder<Attribute>, AttributeModifier> builder = ImmutableMultimap.builder();
            for (Map.Entry<Holder<Attribute>, Map<AttributeModifier.Operation, AttributeModifier>> entry : collapsedModifiers.entrySet()) {
                builder.putAll(entry.getKey(), entry.getValue().values());
            }
            return builder.build();
        }
        return ICurioItem.super.getAttributeModifiers(slotContext, id, stack);
    }
}
