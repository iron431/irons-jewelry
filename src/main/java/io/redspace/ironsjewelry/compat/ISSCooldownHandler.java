package io.redspace.ironsjewelry.compat;

import io.redspace.ironsjewelry.core.ICooldownHandler;
import io.redspace.ironsjewelry.core.data.QualityScalar;
import io.redspace.ironsspellbooks.api.util.Utils;
import net.minecraft.world.entity.LivingEntity;

import static io.redspace.ironsspellbooks.api.registry.AttributeRegistry.COOLDOWN_REDUCTION;

public class ISSCooldownHandler implements ICooldownHandler {
    @Override
    public int getCooldown(LivingEntity wearer, QualityScalar baseCooldown, double quality) {
        double playerCooldownModifier = wearer == null ? 1 : wearer.getAttributeValue(COOLDOWN_REDUCTION);
        return (int) (baseCooldown.sample(quality) * (2 - Utils.softCapFormula(playerCooldownModifier)));
    }
}
