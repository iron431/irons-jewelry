package io.redspace.ironsjewelry.registry;

import io.redspace.ironsjewelry.IronsJewelry;
import io.redspace.ironsjewelry.core.Bonus;
import io.redspace.ironsjewelry.core.bonuses.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class BonusRegistry {
    private static final DeferredRegister<Bonus> BONUSES = DeferredRegister.create(IronsJewelryRegistries.BONUS_REGISTRY, IronsJewelry.MODID);

    public static final Supplier<Bonus> EMPTY = BONUSES.register("empty", EmptyBonus::new);
    //public static final Supplier<IBonus> DEATH = BONUSES.register("death", () -> DeathBonus.CODEC);
    public static final Supplier<Bonus> PIGLIN_NEUTRAL_BONUS = BONUSES.register("piglin_neutral_bonus", PiglinNeutralBonus::new);
    public static final Supplier<Bonus> ATTRIBUTE_BONUS = BONUSES.register("attribute_bonus", AttributeBonus::new);
    public static final Supplier<Bonus> EFFECT_ON_HIT_BONUS = BONUSES.register("effect_on_hit_bonus", EffectOnHitBonus::new);
    public static final Supplier<OnProjectileHitBonus> ON_PROJECTILE_HIT_BONUS = BONUSES.register("on_projectile_hit_bonus", OnProjectileHitBonus::new);
    public static final Supplier<OnAttackBonus> ON_ATTACK_BONUS = BONUSES.register("on_attack", OnAttackBonus::new);
    public static final Supplier<Bonus> EFFECT_IMMUNITY_BONUS = BONUSES.register("effect_immunity_bonus", EffectImmunityBonus::new);
    public static final Supplier<OnShieldBlockBonus> ON_SHIELD_BLOCK_BONUS = BONUSES.register("on_shield_block_bonus", OnShieldBlockBonus::new);
    public static final Supplier<OnTakeDamageBonus> ON_TAKE_DAMAGE_BONUS = BONUSES.register("on_take_damage_bonus", OnTakeDamageBonus::new);

    public static void register(IEventBus eventBus){
        BONUSES.register(eventBus);
    }
}
