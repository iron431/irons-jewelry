package io.redspace.ironsjewelry.registry;

import com.mojang.serialization.MapCodec;
import io.redspace.ironsjewelry.IronsJewelry;
import io.redspace.ironsjewelry.core.IAction;
import io.redspace.ironsjewelry.core.actions.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

import static io.redspace.ironsjewelry.registry.IronsJewelryRegistries.ACTION_REGISTRY;

public class ActionRegistry {
    private static final DeferredRegister<MapCodec<? extends IAction>> ACTIONS = DeferredRegister.create(ACTION_REGISTRY, IronsJewelry.MODID);

    public static final Supplier<MapCodec<? extends IAction>> KNOCKBACK = ACTIONS.register("knockback", () -> KnockbackAction.CODEC);
    public static final Supplier<MapCodec<? extends IAction>> IGNITE = ACTIONS.register("ignite", () -> IgniteAction.CODEC);
    public static final Supplier<MapCodec<? extends IAction>> APPLY_EFFECT = ACTIONS.register("apply_effect", () -> ApplyEffectAction.CODEC);
    public static final Supplier<MapCodec<? extends IAction>> APPLY_DAMAGE = ACTIONS.register("apply_damage", () -> ApplyDamageAction.CODEC);
    public static final Supplier<MapCodec<? extends IAction>> APPLY_FREEZE = ACTIONS.register("apply_freeze", () -> ApplyFreezeAction.CODEC);
    public static final Supplier<MapCodec<? extends IAction>> HEAL = ACTIONS.register("heal", () -> HealAction.CODEC);

    public static void register(IEventBus eventBus) {
        ACTIONS.register(eventBus);
    }
}
