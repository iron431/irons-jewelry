package io.redspace.ironsjewelry.compat;

// ISS stub: requires compileOnly irons_spellbooks API dep
// Uncomment when the dep is wired in build.gradle

//import io.redspace.ironsjewelry.core.ICooldownHandler;
//import io.redspace.ironsjewelry.core.data.QualityScalar;
//import io.redspace.ironsspellbooks.api.util.Utils;
//import net.minecraft.world.entity.LivingEntity;
//
//import static io.redspace.ironsspellbooks.api.registry.AttributeRegistry.COOLDOWN_REDUCTION;
//
//public class ISSCooldownHandler implements ICooldownHandler {
//    @Override
//    public int getCooldown(LivingEntity wearer, QualityScalar baseCooldown, double quality) {
//        double playerCooldownModifier = wearer == null ? 1 : wearer.getAttributeValue(COOLDOWN_REDUCTION);
//        return (int) (baseCooldown.sample(quality) * (2 - Utils.softCapFormula(playerCooldownModifier)));
//    }
//}

import io.redspace.ironsjewelry.core.ICooldownHandler;
import io.redspace.ironsjewelry.core.data.QualityScalar;
import net.minecraft.world.entity.LivingEntity;

public class ISSCooldownHandler implements ICooldownHandler {
    @Override
    public int getCooldown(LivingEntity wearer, QualityScalar baseCooldown, double quality) {
        // Fallback: ISS API unavailable, ignore cooldown reduction attribute
        return (int) baseCooldown.sample(quality);
    }
}
