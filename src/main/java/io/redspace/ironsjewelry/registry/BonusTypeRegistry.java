package io.redspace.ironsjewelry.registry;

import io.redspace.ironsjewelry.IronsJewelry;
import io.redspace.ironsjewelry.core.BonusType;
import io.redspace.ironsjewelry.core.bonuses.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class BonusTypeRegistry {
    private static final DeferredRegister<BonusType> BONUSES = DeferredRegister.create(IronsJewelryRegistries.BONUS_TYPE_REGISTRY, IronsJewelry.MODID);

    public static final Supplier<BonusType> EMPTY = BONUSES.register("empty", EmptyBonusType::new);
    public static final Supplier<BonusType> PIGLIN_NEUTRAL_BONUS = BONUSES.register("piglin_neutral_bonus", PiglinNeutralBonusType::new);
    public static final Supplier<BonusType> ATTRIBUTE_BONUS = BONUSES.register("attribute_bonus", AttributeBonusType::new);
    public static final Supplier<BonusType> EFFECT_ON_HIT_BONUS = BONUSES.register("effect_on_hit_bonus", EffectOnHitBonusType::new);
    public static final Supplier<OnProjectileHitBonusType> ON_PROJECTILE_HIT_BONUS = BONUSES.register("on_projectile_hit_bonus", OnProjectileHitBonusType::new);
    public static final Supplier<OnAttackBonusType> ON_ATTACK_BONUS = BONUSES.register("on_attack", OnAttackBonusType::new);
    public static final Supplier<BonusType> EFFECT_IMMUNITY_BONUS = BONUSES.register("effect_immunity_bonus", EffectImmunityBonusType::new);
    public static final Supplier<OnShieldBlockBonusType> ON_SHIELD_BLOCK_BONUS = BONUSES.register("on_shield_block_bonus", OnShieldBlockBonusType::new);
    public static final Supplier<OnTakeDamageBonusType> ON_TAKE_DAMAGE_BONUS = BONUSES.register("on_take_damage_bonus", OnTakeDamageBonusType::new);

    public static void register(IEventBus eventBus){
        BONUSES.register(eventBus);
    }
}
