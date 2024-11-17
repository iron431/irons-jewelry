package io.redspace.ironsjewelry.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import io.redspace.ironsjewelry.client.ClientEvents;
import io.redspace.ironsjewelry.core.IBonusParameterType;
import io.redspace.ironsjewelry.core.Utils;
import io.redspace.ironsjewelry.core.bonuses.AttributeBonus;
import io.redspace.ironsjewelry.core.bonuses.PiglinNeutralBonus;
import io.redspace.ironsjewelry.core.data.*;
import io.redspace.ironsjewelry.registry.BonusRegistry;
import io.redspace.ironsjewelry.registry.ComponentRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurio;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import java.util.*;

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
        if (!itemStack.has(DataComponents.ITEM_NAME)) {
            itemStack.set(DataComponents.ITEM_NAME, JewelryData.get(itemStack).getItemName());
        }
        return Optional.ofNullable(itemStack.get(DataComponents.ITEM_NAME)).orElse(super.getName(itemStack));
    }

    @Override
    public void appendHoverText(ItemStack pStack, TooltipContext pContext, List<Component> pTooltipComponents, TooltipFlag pTooltipFlag) {
        super.appendHoverText(pStack, pContext, pTooltipComponents, pTooltipFlag);
        var jewelryData = JewelryData.get(pStack);
        if (jewelryData.isValid()) {
            if (ClientEvents.isIsShiftKeyDown()) {
                pTooltipComponents.add(Component.translatable("tooltip.irons_jewelry.hold_shift", Component.translatable("key.keyboard.left.shift").withStyle(ChatFormatting.WHITE)).withStyle(ChatFormatting.GRAY));
                pTooltipComponents.addAll(getShiftDescription(jewelryData.pattern().value(), jewelryData.parts(), Optional.empty()));
            } else {
                pTooltipComponents.add(Component.translatable("tooltip.irons_jewelry.hold_shift", Component.translatable("key.keyboard.left.shift").withStyle(ChatFormatting.DARK_GRAY)).withStyle(ChatFormatting.GRAY));
            }
        }
    }

    public static List<Component> getShiftDescription(PatternDefinition pattern, Map<Holder<PartDefinition>, Holder<MaterialDefinition>> parts, Optional<List<Integer>> materialCost) {
        List<Component> components = new ArrayList<>();
        for (int i = 0; i < pattern.partTemplate().size(); i++) {
            var partIngredient = pattern.partTemplate().get(i);
            var part = partIngredient.part();
            var partComponent = Component.translatable(part.value().descriptionId());
            MutableComponent materialComponent;
            Optional<Component> bonusComponent = Optional.empty();
            Optional<Component> qualityComponent = Optional.empty();
            int i2 = i;
            Optional<MutableComponent> costComponent = materialCost.map(list -> {
                var count = list.size() > i2 && parts.containsKey(part) && part.value().canUseMaterial(parts.get(part).value().materialType()) ? list.get(i2) : 0;
                String cost = String.format("(%s/%s)", count, partIngredient.materialCost());
                return Optional.of(Component.literal("  * ").append(Component.literal(cost).withStyle(count >= partIngredient.materialCost() ? ChatFormatting.GREEN : ChatFormatting.RED)).withStyle(ChatFormatting.DARK_GRAY));
            }).orElse(Optional.empty());
            if (!parts.containsKey(part)) {
                materialComponent = Component.translatable("tooltip.irons_jewelry.empty").withStyle(ChatFormatting.RED);
            } else {
                var mat = parts.get(part);
                materialComponent = Component.translatable(mat.value().descriptionId()).withStyle(ChatFormatting.DARK_AQUA);
                var bonusContribution = pattern.bonuses().stream().filter(bonus -> bonus.parameterOrSource().right().isPresent() && bonus.parameterOrSource().right().get().equals(part)).findFirst();
                var qualityContribution = pattern.bonuses().stream().filter(bonus -> bonus.qualityOrSource().right().isPresent() && bonus.qualityOrSource().right().get().equals(part)).findFirst();
                if (bonusContribution.isPresent()) {
                    IBonusParameterType type = bonusContribution.get().bonus().getParameterType();
                    var value = type.resolve(mat.value().bonusParameters());
                    //var bonus = bonusContribution.get().getBonusFor(jewelryData);
                    if (value.isPresent()) {
                        Optional<String> string = type.getValueDescriptionId(value.get());
                        if (string.isPresent()) {
                            //bonusEntries.add(Component.literal(" ").append(Component.translatable("tooltip.irons_jewelry.bonus_to_source", Component.translatable(source.bonus().getDescriptionId()), Component.translatable(string.get()))));
                            bonusComponent = Optional.of(Component.literal("  * ").append(Component.translatable(string.get()).withStyle(ChatFormatting.WHITE)).withStyle(ChatFormatting.DARK_GRAY));
                        }
                    }
                }
                if (qualityContribution.isPresent()) {
                    var quality = parts.get(qualityContribution.get().qualityOrSource().right().get()).value().quality();
                    qualityComponent = Optional.of(Component.literal("  * ").append(Component.translatable("tooltip.irons_jewelry.quality_multiplier", quality).withStyle(ChatFormatting.WHITE)).withStyle(ChatFormatting.DARK_GRAY));
                }
            }
            components.add(Component.literal("> ").append(Component.translatable("tooltip.irons_jewelry.part_to_material", partComponent, materialComponent)).withStyle(ChatFormatting.GRAY));
            costComponent.ifPresent(components::add);
            bonusComponent.ifPresent(components::add);
            qualityComponent.ifPresent(components::add);
        }
        return components;
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
