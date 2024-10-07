package io.redspace.ironsjewelry.registry;

import io.redspace.ironsjewelry.IronsJewelry;
import io.redspace.ironsjewelry.core.Bonus;
import io.redspace.ironsjewelry.core.bonuses.*;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegistryBuilder;

import java.util.function.Supplier;

public class BonusRegistry {
    public static final ResourceKey<Registry<Bonus>> BONUS_REGISTRY_KEY = ResourceKey.createRegistryKey(IronsJewelry.id("bonus"));
    public static final Registry<Bonus> BONUS_REGISTRY = new RegistryBuilder<>(BONUS_REGISTRY_KEY).defaultKey(IronsJewelry.id("empty")).create();
    private static final DeferredRegister<Bonus> BONUSES = DeferredRegister.create(BONUS_REGISTRY, IronsJewelry.MODID);

    public static final Supplier<Bonus> EMPTY = BONUSES.register("empty", EmptyBonus::new);
    //public static final Supplier<IBonus> DEATH = BONUSES.register("death", () -> DeathBonus.CODEC);
    public static final Supplier<Bonus> PIGLIN_NEUTRAL_BONUS = BONUSES.register("piglin_neutral_bonus", PiglinNeutralBonus::new);
    public static final Supplier<Bonus> ATTRIBUTE_BONUS = BONUSES.register("attribute_bonus", AttributeBonus::new);
    public static final Supplier<Bonus> EFFECT_ON_HIT_BONUS = BONUSES.register("effect_on_hit_bonus", EffectOnHitBonus::new);
    public static final Supplier<EffectOnProjectileHitBonus> EFFECT_ON_PROJECTILE_HIT_BONUS = BONUSES.register("effect_on_projectile_hit_bonus", EffectOnProjectileHitBonus::new);
    public static final Supplier<Bonus> EFFECT_IMMUNITY_BONUS = BONUSES.register("effect_immunity_bonus", EffectImmunityBonus::new);

    public static void registerRegistry(NewRegistryEvent event) {
        event.register(BONUS_REGISTRY);
    }

    public static void register(IEventBus eventBus){
        BONUSES.register(eventBus);
    }
}
