package io.redspace.ironsjewelry.item;

import io.redspace.ironslib.util.TooltipUtils;
import io.redspace.ironsjewelry.core.bonuses.AttributeBonusType;
import io.redspace.ironsjewelry.core.bonuses.PiglinNeutralBonusType;
import io.redspace.ironsjewelry.core.data.BonusInstance;
import io.redspace.ironsjewelry.core.data.JewelryData;
import io.redspace.ironsjewelry.core.data.MaterialDefinition;
import io.redspace.ironsjewelry.core.data.PartDefinition;
import io.redspace.ironsjewelry.core.data.PatternDefinition;
import io.redspace.ironsjewelry.core.parameters.IBonusParameterType;
import io.redspace.ironsjewelry.registry.BonusTypeRegistry;
import io.redspace.ironsjewelry.utils.Utils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.theillusivec4.curios.api.CurioAttributeModifiers;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurio;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class CurioBaseItem extends Item implements ICurioItem {
    String slotIdentifier;

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
    public List<Component> getAttributesTooltip(List<Component> tooltips, TooltipContext tooltipContext, ItemStack stack) {
        var jewelryData = JewelryData.get(stack);
        if (!jewelryData.isValid()) {
            return List.of();
        }
        var shiftTooltip = new ArrayList<Component>();
        TooltipUtils.addShiftTooltip(
                shiftTooltip::add,
                Component.translatable("tooltip.irons_jewelry.hold_shift", Component.translatable("key.keyboard.left.shift").withStyle(ChatFormatting.DARK_GRAY)).withStyle(ChatFormatting.GRAY),
                Optional.of(Component.translatable("tooltip.irons_jewelry.hold_shift", Component.translatable("key.keyboard.left.shift").withStyle(ChatFormatting.WHITE)).withStyle(ChatFormatting.GRAY)),
                getShiftDescription(jewelryData.pattern().value(), jewelryData.parts(), Optional.empty())
        );
        var attrTooltip = ICurioItem.super.getAttributesTooltip(tooltips, tooltipContext, stack);
        boolean needHeader = attrTooltip.isEmpty();
        var bonuses = jewelryData.getBonuses();
        if (needHeader && !bonuses.isEmpty()) {
            attrTooltip.add(Component.empty());
            attrTooltip.add(Component.translatable("curios.modifiers." + slotIdentifier).withStyle(ChatFormatting.GOLD));
        }
        bonuses.forEach(bonus -> attrTooltip.addAll(bonus.getTooltipDescription()));

        shiftTooltip.addAll(attrTooltip);
        return shiftTooltip;
    }

    @Override
    public @NotNull Component getName(ItemStack itemStack) {
        if (JewelryData.has(itemStack)) {
            var data = JewelryData.get(itemStack);
            if (data.isValid()) {
                return data.getItemName();
            }
            return Component.translatable("item.irons_jewelry.invalid_jewelry");
        }
        return super.getName(itemStack);
    }

    public static List<Component> getShiftDescription(PatternDefinition pattern, Map<Holder<PartDefinition>, Holder<MaterialDefinition>> parts, Optional<List<Integer>> materialCost) {
        List<Component> components = new ArrayList<>();
        for (int i = 0; i < pattern.partTemplate().size(); i++) {
            var partIngredient = pattern.partTemplate().get(i);
            var currentPart = partIngredient.part();
            var partComponent = Component.translatable(currentPart.value().descriptionId());
            MutableComponent materialComponent;
            Optional<Component> bonusComponent = Optional.empty();
            Optional<Component> qualityComponent = Optional.empty();
            int i2 = i;
            Optional<MutableComponent> costComponent = materialCost.map(list -> {
                var count = list.size() > i2 && parts.containsKey(currentPart) && currentPart.value().canUseMaterial(parts.get(currentPart)) ? list.get(i2) : 0;
                String cost = String.format("(%s/%s)", count, partIngredient.materialCost());
                return Optional.of(Component.literal("  * ").append(Component.literal(cost).withStyle(count >= partIngredient.materialCost() ? ChatFormatting.GREEN : ChatFormatting.RED)).withStyle(ChatFormatting.DARK_GRAY));
            }).orElse(Optional.empty());
            if (!parts.containsKey(currentPart)) {
                materialComponent = Component.translatable("tooltip.irons_jewelry.empty").withStyle(ChatFormatting.RED);
            } else {
                var mat = parts.get(currentPart);
                materialComponent = Component.translatable(mat.value().descriptionId()).withStyle(ChatFormatting.DARK_AQUA);
                var bonusContribution = pattern.bonuses().stream().filter(tuple ->
                        !tuple.getB().parameterValue().containsKey(tuple.getB().bonusType().getParameterType()) && tuple.getA().part().equals(currentPart)).findFirst();
                var qualityContribution = pattern.partForQuality().filter(partThatDrivesQuality -> partThatDrivesQuality.equals(currentPart));
                if (bonusContribution.isPresent()) {
                    // if bonusContribution is present, the tuple's PartIngredient's #parameterValue is not
                    var tuple = bonusContribution.get();
                    IBonusParameterType type = tuple.getB().bonusType().getParameterType();
                    var value = type.resolve(mat.value().bonusParameters());
                    if (value.isPresent()) {
                        Optional<String> string = type.getValueDescriptionId(value.get());
                        if (string.isPresent()) {
                            //bonusEntries.add(Component.literal(" ").append(Component.translatable("tooltip.irons_jewelry.bonus_to_source", Component.translatable(source.bonus().getDescriptionId()), Component.translatable(string.get()))));
                            bonusComponent = Optional.of(Component.literal("  * ").append(Component.translatable(string.get()).withStyle(ChatFormatting.WHITE)).withStyle(ChatFormatting.DARK_GRAY));
                        }
                    }
                }
                if (qualityContribution.isPresent()) {
                    var quality = parts.get(qualityContribution.get()).value().quality();
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
    public boolean makesPiglinsNeutral(SlotContext slotContext, ItemStack stack) {
        return JewelryData.get(stack).getBonuses().stream().map(BonusInstance::bonusType).anyMatch(bonus -> bonus instanceof PiglinNeutralBonusType);
    }

    @Override
    public void onEquip(SlotContext slotContext, ItemStack prevStack, ItemStack stack) {
        ICurioItem.super.onEquip(slotContext, prevStack, stack);
        JewelryData.ifPresent(stack, data -> data.forBonuses(BonusTypeRegistry.EFFECT_IMMUNITY_BONUS.get(), Holder.class, (bonus, param) -> slotContext.entity().removeEffect(param)));
    }

    @Override
    public CurioAttributeModifiers getDefaultCurioAttributeModifiers(ItemStack stack) {
        JewelryData data = JewelryData.getNullable(stack);
        if (data == null || !data.isValid()) {
            return CurioAttributeModifiers.EMPTY;
        }

        var builder = CurioAttributeModifiers.builder();
        var slotContext = new SlotContext(this.slotIdentifier, null, -1, false, true);
        for (Map.Entry<Holder<Attribute>, Map<AttributeModifier.Operation, AttributeModifier>> entry : buildCollapsedAttributeModifiers(data, slotContext).entrySet()) {
            for (AttributeModifier modifier : entry.getValue().values()) {
                builder.addModifier(entry.getKey(), modifier, this.slotIdentifier);
            }
        }
        return builder.build();
    }

    private static Map<Holder<Attribute>, Map<AttributeModifier.Operation, AttributeModifier>> buildCollapsedAttributeModifiers(JewelryData data, SlotContext slotContext) {
        Map<Holder<Attribute>, Map<AttributeModifier.Operation, AttributeModifier>> collapsedModifiers = new HashMap<>();
        for (BonusInstance instance : data.getBonuses()) {
            if (instance.bonusType() instanceof AttributeBonusType attributeBonus) {
                attributeBonus.getParameterType().resolve(instance.parameter()).ifPresent(attributeInstance -> {
                    var byOperation = collapsedModifiers.computeIfAbsent(attributeInstance.attribute(), ignored -> new HashMap<>());
                    var modifier = attributeBonus.modifier(attributeInstance, slotContext, instance.quality());
                    var operation = modifier.operation();
                    if (byOperation.containsKey(operation)) {
                        var oldModifier = byOperation.get(operation);
                        byOperation.put(operation, new AttributeModifier(oldModifier.id(), oldModifier.amount() + modifier.amount(), operation));
                    } else {
                        byOperation.put(operation, modifier);
                    }
                });
            }
        }
        return collapsedModifiers;
    }
}
