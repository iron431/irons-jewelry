package io.redspace.ironsjewelry.registry;

import io.redspace.ironsjewelry.IronsJewelry;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.PercentageAttribute;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

@EventBusSubscriber
public class AttributeRegistry {
    private static final DeferredRegister<Attribute> ATTRIBUTES = DeferredRegister.create(Registries.ATTRIBUTE, IronsJewelry.MODID);

    public static final DeferredHolder<Attribute, Attribute> ARMOR_PIERCE = registerRanged("armor_pierce");
    public static final DeferredHolder<Attribute, Attribute> MINING_SPEED = registerPercent("mining_speed");
    public static final DeferredHolder<Attribute, Attribute> EXPERIENCE_GAINED = registerPercent("experience_gained");
    public static final DeferredHolder<Attribute, Attribute> ARROW_DAMAGE = registerPercent("arrow_damage");
    public static final DeferredHolder<Attribute, Attribute> CRIT_DAMAGE = registerPercent("crit_damage");
    public static final DeferredHolder<Attribute, Attribute> DODGE_CHANCE = registerPercent("dodge_chance");
    public static final DeferredHolder<Attribute, Attribute> HEALING_RECEIVED = registerPercent("healing_received");

    private static DeferredHolder<Attribute, Attribute> registerPercent(String name) {
        String descriptionId = "attribute.name.irons_jewelry." + name;
        return ATTRIBUTES.register(name, () -> new PercentageAttribute(descriptionId, 1, 0, 1024).setSyncable(true));
    }

    private static DeferredHolder<Attribute, Attribute> registerRanged(String name) {
        String descriptionId = "attribute.name.irons_jewelry." + name;
        return ATTRIBUTES.register(name, () -> new RangedAttribute(descriptionId, 0, 0, 1024).setSyncable(true));
    }

    public static void register(IEventBus eventBus) {
        ATTRIBUTES.register(eventBus);
    }

    @SubscribeEvent
    public static void modifyEntityAttributes(EntityAttributeModificationEvent e) {
        e.getTypes().forEach(entity -> ATTRIBUTES.getEntries().forEach(attribute -> e.add(entity, attribute)));
    }
}
