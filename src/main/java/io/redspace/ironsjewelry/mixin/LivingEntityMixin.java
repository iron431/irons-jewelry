package io.redspace.ironsjewelry.mixin;

import io.redspace.ironsjewelry.registry.AttributeRegistry;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
    @Redirect(
            method = "getDamageAfterArmorAbsorb",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getArmorValue()I")
    )
    private int irons_jewelry$applyArmorPierce(LivingEntity target, DamageSource damageSource, float damageAmount) {
        int armor = target.getArmorValue();
        if (!(damageSource.getEntity() instanceof LivingEntity attacker) || armor <= 0) {
            return armor;
        }

        double armorPierce = attacker.getAttributeValue(AttributeRegistry.ARMOR_PIERCE);
        if (armorPierce <= 0) {
            return armor;
        }

        return Math.max(0, (int) Math.floor(armor - armorPierce));
    }
}
