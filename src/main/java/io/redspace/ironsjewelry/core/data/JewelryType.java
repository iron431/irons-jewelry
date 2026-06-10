package io.redspace.ironsjewelry.core.data;

import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosSlotTypes;

import java.util.Optional;

public record JewelryType(Holder<Item> item) {
    public Optional<String> getCuriosSlotIdentifier() {
        var slotTypes = CuriosSlotTypes.getItemSlotTypes(new ItemStack(item.value()), false);
        return slotTypes.keySet().stream().findFirst();
    }
}
