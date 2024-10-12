package io.redspace.ironsjewelry.core;

import com.mojang.serialization.Codec;
import io.redspace.ironsjewelry.core.data.BonusInstance;
import io.redspace.ironsjewelry.registry.BonusRegistry;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;

import java.util.List;

public abstract class Bonus {
    private String descriptionId;
    public static final Codec<Bonus> REGISTRY_CODEC = BonusRegistry.BONUS_REGISTRY.byNameCodec();

    public abstract IBonusParameterType<?> getParameterType();

    public List<Component> getTooltipDescription(BonusInstance bonus) {
        return List.of();
    }

    public final String getDescriptionId() {
        if (descriptionId == null) {
            descriptionId = Util.makeDescriptionId("bonus", BonusRegistry.BONUS_REGISTRY.getKey(this));
        }
        return descriptionId;
    }

    public final String getTooltipDescriptionId() {
        return String.format("%s.description", getDescriptionId());
    }
}
