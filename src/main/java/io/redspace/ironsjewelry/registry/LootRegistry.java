package io.redspace.ironsjewelry.registry;

import io.redspace.ironsjewelry.IronsJewelry;
import io.redspace.ironsjewelry.loot.SetHeldPatternLootFunction;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class LootRegistry {
    private static final DeferredRegister<LootItemFunctionType<?>> LOOT_FUNCTIONS = DeferredRegister.create(Registries.LOOT_FUNCTION_TYPE, IronsJewelry.MODID);

    public static final Supplier<LootItemFunctionType<?>> SET_HELD_PATTERN =
            LOOT_FUNCTIONS.register("set_held_pattern", () -> new LootItemFunctionType<>(SetHeldPatternLootFunction.CODEC));

    public static void register(IEventBus modEventBus) {
        LOOT_FUNCTIONS.register(modEventBus);
    }
}
