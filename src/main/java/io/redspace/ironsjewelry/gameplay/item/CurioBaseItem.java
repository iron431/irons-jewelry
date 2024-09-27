package io.redspace.ironsjewelry.gameplay.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import io.redspace.ironsjewelry.core.bonuses.AttributeBonus;
import io.redspace.ironsjewelry.core.data.BonusInstance;
import io.redspace.ironsjewelry.core.data.JewelryData;
import io.redspace.ironsjewelry.registry.ComponentRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurio;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import java.util.List;

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
            if (needHeader) {
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
    public Multimap<Holder<Attribute>, AttributeModifier> getAttributeModifiers(SlotContext slotContext, ResourceLocation id, ItemStack stack) {

        JewelryData data = stack.get(ComponentRegistry.JEWELRY_COMPONENT);

        if (data != null && slotContext.identifier().equals(this.slotIdentifier)) {
            var pattern = data.pattern();
            var bonuses = data.getBonuses();
            ImmutableMultimap.Builder<Holder<Attribute>, AttributeModifier> builder = ImmutableMultimap.builder();
            for (BonusInstance instance : bonuses) {
                if (instance.bonus() instanceof AttributeBonus attributeBonus) {
                    attributeBonus.getParameterType().resolve(instance.parameter()).ifPresent(
                            param -> builder.put(param.attribute(), attributeBonus.modifier(param, slotContext, instance.quality()))
                    );
                }
            }
            return builder.build();
        }
        return ICurioItem.super.getAttributeModifiers(slotContext, id, stack);
    }
}
