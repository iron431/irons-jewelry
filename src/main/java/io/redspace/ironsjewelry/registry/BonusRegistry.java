package io.redspace.ironsjewelry.registry;

import io.redspace.ironsjewelry.IronsJewelry;
import io.redspace.ironsjewelry.core.IBonus;
import io.redspace.ironsjewelry.core.bonuses.AttributeBonus;
import io.redspace.ironsjewelry.core.bonuses.EffectOnHitBonus;
import io.redspace.ironsjewelry.core.bonuses.EmptyBonus;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegistryBuilder;

import java.util.function.Supplier;

public class BonusRegistry {
    public static final ResourceKey<Registry<IBonus>> BONUS_REGISTRY_KEY = ResourceKey.createRegistryKey(IronsJewelry.id("bonus"));
    public static final Registry<IBonus> BONUS_REGISTRY = new RegistryBuilder<>(BONUS_REGISTRY_KEY).defaultKey(IronsJewelry.id("empty")).create();
    private static final DeferredRegister<IBonus> BONUSES = DeferredRegister.create(BONUS_REGISTRY, IronsJewelry.MODID);

    public static final Supplier<IBonus> EMPTY = BONUSES.register("empty", EmptyBonus::new);
    //public static final Supplier<IBonus> DEATH = BONUSES.register("death", () -> DeathBonus.CODEC);
    public static final Supplier<IBonus> ATTRIBUTE_BONUS = BONUSES.register("attribute_bonus", AttributeBonus::new);
    public static final Supplier<IBonus> EFFECT_ON_HIT_BONUS = BONUSES.register("effect_on_hit", EffectOnHitBonus::new);


    public static void registerRegistry(NewRegistryEvent event) {
        event.register(BONUS_REGISTRY);
    }

    public static void register(IEventBus eventBus){
        BONUSES.register(eventBus);
    }
}