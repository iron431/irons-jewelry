package io.redspace.ironsjewelry.registry;

import com.mojang.serialization.MapCodec;
import io.redspace.ironsjewelry.IronsJewelry;
import io.redspace.ironsjewelry.core.IAction;
import io.redspace.ironsjewelry.core.actions.ApplyEffectAction;
import io.redspace.ironsjewelry.core.actions.IgniteAction;
import io.redspace.ironsjewelry.core.actions.KnockbackAction;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegistryBuilder;

import java.util.function.Supplier;

public class ActionRegistry {
    public static final ResourceKey<Registry<MapCodec<? extends IAction>>> ACTION_REGISTRY_KEY = ResourceKey.createRegistryKey(IronsJewelry.id("action"));
    public static final Registry<MapCodec<? extends IAction>> ACTION_REGISTRY = new RegistryBuilder<>(ACTION_REGISTRY_KEY).defaultKey(IronsJewelry.id("empty")).create();
    private static final DeferredRegister<MapCodec<? extends IAction>> ACTIONS = DeferredRegister.create(ACTION_REGISTRY, IronsJewelry.MODID);

    public static final Supplier<MapCodec<? extends IAction>> KNOCKBACK = ACTIONS.register("knockback", () -> KnockbackAction.CODEC);
    public static final Supplier<MapCodec<? extends IAction>> IGNITE = ACTIONS.register("ignite", () -> IgniteAction.CODEC);
    public static final Supplier<MapCodec<? extends IAction>> APPLY_EFFECT = ACTIONS.register("apply_effect", () -> ApplyEffectAction.CODEC);

    public static void registerRegistry(NewRegistryEvent event) {
        event.register(ACTION_REGISTRY);
    }

    public static void register(IEventBus eventBus){
        ACTIONS.register(eventBus);
    }
}
