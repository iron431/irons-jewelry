package io.redspace.ironsjewelry.registry;

import com.mojang.serialization.MapCodec;
import io.redspace.ironsjewelry.IronsJewelry;
import io.redspace.ironsjewelry.core.IAction;
import io.redspace.ironsjewelry.core.actions.ApplyEffectAction;
import io.redspace.ironsjewelry.core.actions.IgniteAction;
import io.redspace.ironsjewelry.core.actions.KnockbackAction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

import static io.redspace.ironsjewelry.registry.IronsJewelryRegistries.ACTION_REGISTRY;

public class ActionRegistry {
    private static final DeferredRegister<MapCodec<? extends IAction>> ACTIONS = DeferredRegister.create(ACTION_REGISTRY, IronsJewelry.MODID);

    public static final Supplier<MapCodec<? extends IAction>> KNOCKBACK = ACTIONS.register("knockback", () -> KnockbackAction.CODEC);
    public static final Supplier<MapCodec<? extends IAction>> IGNITE = ACTIONS.register("ignite", () -> IgniteAction.CODEC);
    public static final Supplier<MapCodec<? extends IAction>> APPLY_EFFECT = ACTIONS.register("apply_effect", () -> ApplyEffectAction.CODEC);

    public static void register(IEventBus eventBus){
        ACTIONS.register(eventBus);
    }
}
