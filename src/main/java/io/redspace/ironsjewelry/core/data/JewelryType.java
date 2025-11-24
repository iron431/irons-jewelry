package io.redspace.ironsjewelry.core.data;

import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.Optional;

public record JewelryType(Holder<Item> item) {
    public Optional<String> getCuriosSlotIdentifier() {
        var tags = CuriosApi.getCuriosHelper().getCurioTags(item.value());
        return tags.stream().findFirst();
    }
}
