package io.redspace.ironsjewelry.core;

import com.mojang.serialization.Codec;
import io.redspace.ironsjewelry.core.data.BonusInstance;
import io.redspace.ironsjewelry.registry.IronsJewelryRegistries;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;

import java.util.List;

public abstract class BonusType {
    private String descriptionId;
    public static final Codec<BonusType> REGISTRY_CODEC = IronsJewelryRegistries.BONUS_TYPE_REGISTRY.byNameCodec();

    public abstract IBonusParameterType<?> getParameterType();

    public List<Component> getTooltipDescription(BonusInstance bonus) {
        return List.of();
    }

    public final String getDescriptionId() {
        if (descriptionId == null) {
            descriptionId = Util.makeDescriptionId("bonus", IronsJewelryRegistries.BONUS_TYPE_REGISTRY.getKey(this));
        }
        return descriptionId;
    }

    public final String getTooltipDescriptionId() {
        return String.format("%s.description", getDescriptionId());
    }
}
