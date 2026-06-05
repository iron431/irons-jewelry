package io.redspace.ironsjewelry.event;

import io.redspace.ironsjewelry.compat.CompatHandler;
import io.redspace.ironsjewelry.compat.attribute.AttributeRemapRegistry;
import io.redspace.ironsjewelry.registry.AttributeRegistry;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
public class ModSetupEvents {

    @SubscribeEvent
    public static void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            CompatHandler.init();
            AttributeRemapRegistry.register(AttributeRegistry.ARMOR_PIERCE, ResourceLocation.parse("apothic_attributes:armor_pierce"));
            AttributeRemapRegistry.register(AttributeRegistry.MINING_SPEED, ResourceLocation.parse("apothic_attributes:mining_speed"));
            AttributeRemapRegistry.register(AttributeRegistry.EXPERIENCE_GAINED, ResourceLocation.parse("apothic_attributes:experience_gained"));
            AttributeRemapRegistry.register(AttributeRegistry.ARROW_DAMAGE, ResourceLocation.parse("apothic_attributes:arrow_damage"));
            AttributeRemapRegistry.register(AttributeRegistry.CRIT_DAMAGE, ResourceLocation.parse("apothic_attributes:crit_damage"));
            AttributeRemapRegistry.register(AttributeRegistry.DODGE_CHANCE, ResourceLocation.parse("apothic_attributes:dodge_chance"));
        });
    }
}
