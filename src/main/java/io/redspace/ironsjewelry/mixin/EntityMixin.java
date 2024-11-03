package io.redspace.ironsjewelry.mixin;

import io.redspace.ironsjewelry.core.actions.IgnorableExplosion;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Explosion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class EntityMixin {
    @Inject(method = "ignoreExplosion", at = @At(value = "RETURN"), cancellable = true)
    void cancelExplosion(Explosion pExplosion, CallbackInfoReturnable<Boolean> cir) {
        if (pExplosion instanceof IgnorableExplosion ignorableExplosion) {
            if (ignorableExplosion.shouldIgnore((Entity) (Object) this)) {
                cir.setReturnValue(true);
            }
        }
    }
}
