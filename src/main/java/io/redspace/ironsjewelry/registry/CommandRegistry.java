package io.redspace.ironsjewelry.registry;


import io.redspace.ironsjewelry.gameplay.command.IronsDebugCommand;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@EventBusSubscriber
public class CommandRegistry {
    @SubscribeEvent
    public static void registerCommand(RegisterCommandsEvent event) {
        IronsDebugCommand.register(event.getDispatcher());
    }
}
