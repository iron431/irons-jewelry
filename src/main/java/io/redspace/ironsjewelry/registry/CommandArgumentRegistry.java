package io.redspace.ironsjewelry.registry;

import io.redspace.ironsjewelry.IronsJewelry;
import io.redspace.ironsjewelry.gameplay.command.PatternCommandArgument;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class CommandArgumentRegistry {
    private static final DeferredRegister<ArgumentTypeInfo<?, ?>> ARGUMENT_TYPES = DeferredRegister.create(Registries.COMMAND_ARGUMENT_TYPE, IronsJewelry.MODID);

    private static final Supplier<SingletonArgumentInfo<PatternCommandArgument>> SPELL_COMMAND_ARGUMENT_TYPE = ARGUMENT_TYPES.register("pattern", () -> ArgumentTypeInfos.registerByClass(PatternCommandArgument.class, SingletonArgumentInfo.contextFree(PatternCommandArgument::patternArgument)));

    public static void register(IEventBus modEventBus) {
        ARGUMENT_TYPES.register(modEventBus);
    }
}