package io.redspace.ironsjewelry.registry;

import com.mojang.serialization.MapCodec;
import io.redspace.ironsjewelry.IronsJewelry;
import io.redspace.ironsjewelry.loot.AppendLootModifier;
import io.redspace.ironsjewelry.loot.GenerateJewelryLootFunction;
import io.redspace.ironsjewelry.loot.InjectJewelryLootModifier;
import io.redspace.ironsjewelry.loot.ReplaceLootModifier;
import io.redspace.ironsjewelry.loot.SetHeldPatternLootFunction;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class LootRegistry {
    private static final DeferredRegister<MapCodec<? extends LootItemFunction>> LOOT_FUNCTIONS = DeferredRegister.create(Registries.LOOT_FUNCTION_TYPE, IronsJewelry.MODID);
    private static final DeferredRegister<MapCodec<? extends IGlobalLootModifier>> LOOT_MODIFIER_SERIALIZERS = DeferredRegister.create(NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, IronsJewelry.MODID);

    public static final Supplier<MapCodec<? extends LootItemFunction>> SET_HELD_PATTERN =
            LOOT_FUNCTIONS.register("set_held_pattern", () -> SetHeldPatternLootFunction.MAP_CODEC);
    public static final Supplier<MapCodec<? extends LootItemFunction>> GENERATE_JEWELRY =
            LOOT_FUNCTIONS.register("generate_jewelry", () -> GenerateJewelryLootFunction.MAP_CODEC);

    public static final Supplier<MapCodec<? extends IGlobalLootModifier>> APPEND_LOOT_MODIFIER = LOOT_MODIFIER_SERIALIZERS.register("append_loot", AppendLootModifier.CODEC);
    public static final Supplier<MapCodec<? extends IGlobalLootModifier>> REPLACE_LOOT_MODIFIER = LOOT_MODIFIER_SERIALIZERS.register("replace_loot", ReplaceLootModifier.CODEC);
    public static final Supplier<MapCodec<? extends IGlobalLootModifier>> INJECT_JEWELRY_MODIFIER = LOOT_MODIFIER_SERIALIZERS.register("inject_jewelry", InjectJewelryLootModifier.CODEC);

    public static void register(IEventBus modEventBus) {
        LOOT_FUNCTIONS.register(modEventBus);
        LOOT_MODIFIER_SERIALIZERS.register(modEventBus);
    }
}
