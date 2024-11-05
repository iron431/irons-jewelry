package io.redspace.ironsjewelry.core;

import io.redspace.ironsjewelry.core.data.QualityScalar;
import net.minecraft.world.entity.LivingEntity;

public interface ICooldownHandler {
    int getCooldown(LivingEntity wearer, QualityScalar baseCooldown, double quality);
}
