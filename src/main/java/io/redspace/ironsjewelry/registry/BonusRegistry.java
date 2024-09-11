package io.redspace.ironsjewelry.registry;

import com.mojang.serialization.MapCodec;
import io.redspace.ironsjewelry.IronsJewelry;
import io.redspace.ironsjewelry.core.IBonus;
import io.redspace.ironsjewelry.core.bonuses.AttributeBonus;
import io.redspace.ironsjewelry.core.bonuses.EmptyBonus;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegistryBuilder;

import java.util.function.Supplier;

public class BonusRegistry {
    public static final ResourceKey<Registry<MapCodec<? extends IBonus>>> BONUS_REGISTRY_KEY = ResourceKey.createRegistryKey(IronsJewelry.id("bonus"));
    public static final Registry<MapCodec<? extends IBonus>> BONUS_REGISTRY = new RegistryBuilder<>(BONUS_REGISTRY_KEY).defaultKey(IronsJewelry.id("empty")).create();
    private static final DeferredRegister<MapCodec<? extends IBonus>> BONUSES = DeferredRegister.create(BONUS_REGISTRY, IronsJewelry.MODID);

    public static final Supplier<MapCodec<? extends IBonus>> EMPTY = BONUSES.register("empty", () -> EmptyBonus.CODEC);
    //public static final Supplier<MapCodec<? extends IBonus>> DEATH = BONUSES.register("death", () -> DeathBonus.CODEC);
    public static final Supplier<MapCodec<? extends IBonus>> ATTRIBUTE_BONUS = BONUSES.register("attribute_bonus", () -> AttributeBonus.CODEC);


    public static void registerRegistry(NewRegistryEvent event) {
        event.register(BONUS_REGISTRY);
    }

    public static void register(IEventBus eventBus){
        BONUSES.register(eventBus);
    }
}
